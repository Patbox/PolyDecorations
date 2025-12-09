package eu.pb4.polydecorations.item;

import com.mojang.datafixers.util.Pair;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.polydecorations.block.extension.AttachedSignPostBlock;
import eu.pb4.polydecorations.block.extension.SignPostBlockEntity;
import eu.pb4.polydecorations.block.furniture.BenchBlock;
import eu.pb4.polydecorations.block.item.*;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HammerItem extends SimplePolymerItem {
    public static List<Pair<Class<? extends Block>, Action>> ACTIONS_BY_CLASS = new ArrayList<>();
    public static List<Pair<Block, Action>> ACTIONS_BY_TYPE = new ArrayList<>();
    public HammerItem(Properties settings) {
        super(settings);
        AttackBlockCallback.EVENT.register(this::onBlockAttacked);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return apply(context.getPlayer(), context.getLevel(), context.getClickedPos(), context.getClickLocation(), false) ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
    }

    private boolean apply(Player player, Level world, BlockPos blockPos, Vec3 hitPos, boolean reverse) {
        if (!CommonProtection.canBreakBlock(world, blockPos, player.getGameProfile(), player) || !player.mayBuild()) {
            return false;
        }

        var state = world.getBlockState(blockPos);

        Action action = null;

        for (var entry : ACTIONS_BY_TYPE) {
            if (state.is(entry.getFirst())) {
                action = entry.getSecond();
                break;
            }
        }
        if (action == null) {
            for (var entry : ACTIONS_BY_CLASS) {
                if (entry.getFirst().isAssignableFrom(state.getBlock().getClass())) {
                    action = entry.getSecond();
                    break;
                }
            }
        }

        if (action == null) {
            return false;
        }

        var newState = action.apply(state, world, blockPos, hitPos, reverse);

        if (newState == state) {
            return false;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            var group = Objects.requireNonNullElse(newState, state).getSoundType();
            serverPlayer.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(group.getPlaceSound()),
                        SoundSource.BLOCKS, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5,
                        (group.getVolume() + 1.0F) / 2.0F, group.getPitch() * 0.8F, player.getRandom().nextLong()));

        }
        if (newState != null) {
            world.setBlockAndUpdate(blockPos, newState);
        }
        return true;
    }

    private InteractionResult onBlockAttacked(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        if (player.getItemInHand(hand).is(this)) {
            var cast = player.pick(player.blockInteractionRange(), 0, false);
            return apply(player, world, pos, cast.getLocation(), false) ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    static {
        ACTIONS_BY_CLASS.add(Pair.of(RotatedPillarBlock.class, Action.cycleState(RotatedPillarBlock.AXIS)));
        ACTIONS_BY_CLASS.add(Pair.of(SlabBlock.class, Action.cycleLimitedState(SlabBlock.TYPE, List.of(SlabType.TOP, SlabType.BOTTOM))));
        ACTIONS_BY_CLASS.add(Pair.of(TrapDoorBlock.class, Action.cycleState(TrapDoorBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(FenceGateBlock.class, Action.cycleState(FenceGateBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(BambooStalkBlock.class, Action.cycleState(BambooStalkBlock.LEAVES)));
        ACTIONS_BY_CLASS.add(Pair.of(MailboxBlock.class, Action.cycleState(MailboxBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(ToolRackBlock.class, Action.cycleState(ToolRackBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(BenchBlock.class, Action.cycleStateAndUpdate(BenchBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(DisplayCaseBlock.class, Action.cycleState(DisplayCaseBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(GlobeBlock.class, Action.cycleState(GlobeBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(ShelfBlock.class, Action.cycleState(ShelfBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(TrashCanBlock.class, Action.cycleState(TrashCanBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(AttachedSignPostBlock.class, (state, world, pos, hitResult, reverse) -> {
            if (world.getBlockEntity(pos) instanceof SignPostBlockEntity be) {
                var up = hitResult.subtract(Vec3.atCenterOf(pos)).y > 0;
                if (be.getText(up).isEmpty()) {
                    return state;
                }
                be.setText(up, be.getText(up).withYawAdded(reverse ? -15 : 15));
                return null;
            }

            return state;
        }));
        ACTIONS_BY_CLASS.add(Pair.of(StairBlock.class, new Action() {
            final Action rotate = Action.cycleState(StairBlock.FACING);
            final Action shape = Action.cycleState(StairBlock.SHAPE);
            final Action half = Action.cycleState(StairBlock.HALF);
            @Override
            public BlockState apply(BlockState state, Level world, BlockPos pos, Vec3 hitResult, boolean reverse) {
                var halfValue = state.getValue(StairBlock.HALF);
                var off = hitResult.subtract(Vec3.atCenterOf(pos));
                if ((halfValue == Half.TOP && off.y < 0) || (halfValue == Half.BOTTOM && off.y > 0)) {
                    return shape.apply(state, world, pos, hitResult, reverse);
                } else if (Mth.equal(off.y, 0)) {
                    return rotate.apply(state, world, pos, hitResult, reverse);
                }

                return half.apply(state, world, pos, hitResult, reverse);
            }
        }));

    }

    public interface Action {
        BlockState apply(BlockState state, Level world, BlockPos pos, Vec3 hitResult, boolean reverse);

        static <T extends Comparable<T>> Action cycleState(Property<T> property) {
            return (state, world, pos, hitResult, reverse) -> {
                var curr = state.getValue(property);
                var list = List.copyOf(property.getPossibleValues());
                return state.setValue(property, list.get((list.size() + list.indexOf(curr) + (reverse ? -1 : 1)) % list.size()));
            };
        }

        static <T extends Comparable<T>> Action cycleLimitedState(Property<T> property, List<T> available) {
            return (state, world, pos, hitResult, reverse) -> {
                var curr = state.getValue(property);
                if (!available.contains(curr)) {
                    return state;
                }
                return state.setValue(property, available.get((available.size() + available.indexOf(curr) + (reverse ? -1 : 1)) % available.size()));
            };
        }

        static <T extends Comparable<T>> Action cycleStateAndUpdate(Property<T> property) {
            return cycleState(property).then((state, world, pos, hitResult, reverse) -> {
                for (var dir : Direction.values()) {
                    var offPos = pos.relative(dir);
                    state = state.updateShape(world, world, pos, dir, offPos, world.getBlockState(offPos), world.random);
                }
                return state;
            });
        }

        default Action then(Action action) {
            return (state, world, pos, hitResult, reverse) -> action.apply(this.apply(state, world, pos, hitResult, reverse), world, pos, hitResult, reverse);
        };
    }
}
