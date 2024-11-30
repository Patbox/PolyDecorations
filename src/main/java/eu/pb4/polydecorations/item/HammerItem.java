package eu.pb4.polydecorations.item;

import com.mojang.datafixers.util.Pair;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.polydecorations.block.extension.AttachedSignPostBlock;
import eu.pb4.polydecorations.block.extension.SignPostBlockEntity;
import eu.pb4.polydecorations.block.furniture.BenchBlock;
import eu.pb4.polydecorations.block.item.*;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HammerItem extends SimplePolymerItem {
    public static List<Pair<Class<? extends Block>, Action>> ACTIONS_BY_CLASS = new ArrayList<>();
    public static List<Pair<Block, Action>> ACTIONS_BY_TYPE = new ArrayList<>();
    public HammerItem(Settings settings) {
        super(settings);
        AttackBlockCallback.EVENT.register(this::onBlockAttacked);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return apply(context.getPlayer(), context.getWorld(), context.getBlockPos(), context.getHitPos(), false) ? ActionResult.SUCCESS_SERVER : ActionResult.FAIL;
    }

    private boolean apply(PlayerEntity player, World world, BlockPos blockPos, Vec3d hitPos, boolean reverse) {
        if (!CommonProtection.canBreakBlock(world, blockPos, player.getGameProfile(), player)) {
            return false;
        }

        var state = world.getBlockState(blockPos);

        Action action = null;

        for (var entry : ACTIONS_BY_TYPE) {
            if (state.isOf(entry.getFirst())) {
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

        if (player instanceof ServerPlayerEntity serverPlayer) {
            var group = Objects.requireNonNullElse(newState, state).getSoundGroup();
            serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(group.getPlaceSound()),
                        SoundCategory.BLOCKS, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5,
                        (group.getVolume() + 1.0F) / 2.0F, group.getPitch() * 0.8F, player.getRandom().nextLong()));

        }
        if (newState != null) {
            world.setBlockState(blockPos, newState);
        }
        return true;
    }

    private ActionResult onBlockAttacked(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        if (player.getStackInHand(hand).isOf(this)) {
            var cast = player.raycast(player.getBlockInteractionRange(), 0, false);
            return apply(player, world, pos, cast.getPos(), false) ? ActionResult.SUCCESS_SERVER : ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    static {
        ACTIONS_BY_CLASS.add(Pair.of(PillarBlock.class, Action.cycleState(PillarBlock.AXIS)));
        ACTIONS_BY_CLASS.add(Pair.of(SlabBlock.class, Action.cycleLimitedState(SlabBlock.TYPE, List.of(SlabType.TOP, SlabType.BOTTOM))));
        ACTIONS_BY_CLASS.add(Pair.of(TrapdoorBlock.class, Action.cycleState(TrapdoorBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(FenceGateBlock.class, Action.cycleState(FenceGateBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(BambooBlock.class, Action.cycleState(BambooBlock.LEAVES)));
        ACTIONS_BY_CLASS.add(Pair.of(MailboxBlock.class, Action.cycleState(MailboxBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(ToolRackBlock.class, Action.cycleState(ToolRackBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(BenchBlock.class, Action.cycleStateAndUpdate(BenchBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(DisplayCaseBlock.class, Action.cycleState(DisplayCaseBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(GlobeBlock.class, Action.cycleState(GlobeBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(ShelfBlock.class, Action.cycleState(ShelfBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(TrashCanBlock.class, Action.cycleState(TrashCanBlock.FACING)));
        ACTIONS_BY_CLASS.add(Pair.of(AttachedSignPostBlock.class, (state, world, pos, hitResult, reverse) -> {
            if (world.getBlockEntity(pos) instanceof SignPostBlockEntity be) {
                var up = hitResult.subtract(Vec3d.ofCenter(pos)).y > 0;
                if (be.getText(up).isEmpty()) {
                    return state;
                }
                be.setText(up, be.getText(up).withYawAdded(reverse ? -15 : 15));
                return null;
            }

            return state;
        }));
        ACTIONS_BY_CLASS.add(Pair.of(StairsBlock.class, new Action() {
            final Action rotate = Action.cycleState(StairsBlock.FACING);
            final Action shape = Action.cycleState(StairsBlock.SHAPE);
            final Action half = Action.cycleState(StairsBlock.HALF);
            @Override
            public BlockState apply(BlockState state, World world, BlockPos pos, Vec3d hitResult, boolean reverse) {
                var halfValue = state.get(StairsBlock.HALF);
                var off = hitResult.subtract(Vec3d.ofCenter(pos));
                if ((halfValue == BlockHalf.TOP && off.y < 0) || (halfValue == BlockHalf.BOTTOM && off.y > 0)) {
                    return shape.apply(state, world, pos, hitResult, reverse);
                } else if (MathHelper.approximatelyEquals(off.y, 0)) {
                    return rotate.apply(state, world, pos, hitResult, reverse);
                }

                return half.apply(state, world, pos, hitResult, reverse);
            }
        }));

    }

    public interface Action {
        BlockState apply(BlockState state, World world, BlockPos pos, Vec3d hitResult, boolean reverse);

        static <T extends Comparable<T>> Action cycleState(Property<T> property) {
            return (state, world, pos, hitResult, reverse) -> {
                var curr = state.get(property);
                var list = List.copyOf(property.getValues());
                return state.with(property, list.get((list.size() + list.indexOf(curr) + (reverse ? -1 : 1)) % list.size()));
            };
        }

        static <T extends Comparable<T>> Action cycleLimitedState(Property<T> property, List<T> available) {
            return (state, world, pos, hitResult, reverse) -> {
                var curr = state.get(property);
                if (!available.contains(curr)) {
                    return state;
                }
                return state.with(property, available.get((available.size() + available.indexOf(curr) + (reverse ? -1 : 1)) % available.size()));
            };
        }

        static <T extends Comparable<T>> Action cycleStateAndUpdate(Property<T> property) {
            return cycleState(property).then((state, world, pos, hitResult, reverse) -> {
                for (var dir : Direction.values()) {
                    var offPos = pos.offset(dir);
                    state = state.getStateForNeighborUpdate(world, world, pos, dir, offPos, world.getBlockState(offPos), world.random);
                }
                return state;
            });
        }

        default Action then(Action action) {
            return (state, world, pos, hitResult, reverse) -> action.apply(this.apply(state, world, pos, hitResult, reverse), world, pos, hitResult, reverse);
        };
    }
}
