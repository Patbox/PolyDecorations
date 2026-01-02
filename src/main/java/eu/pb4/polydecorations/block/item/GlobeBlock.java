package eu.pb4.polydecorations.block.item;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import eu.pb4.polydecorations.block.other.GenericSingleItemBlockEntity;
import eu.pb4.polydecorations.item.DecorationsItemTags;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import static eu.pb4.polydecorations.util.DecorationsUtil.id;

public class GlobeBlock extends BaseEntityBlock implements FactoryBlock, BarrierBasedWaterloggable {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WORLD_BOUND = BooleanProperty.create("worldbound");

    public GlobeBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false).setValue(WORLD_BOUND, false));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING, WORLD_BOUND);
    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return waterLog(ctx, this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite()));
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown() && world.getBlockEntity(pos) instanceof GenericSingleItemBlockEntity be) {
            if (player.getMainHandItem().is(DecorationsItemTags.GLOBE_REPLACEMENT) && be.getTheItem().isEmpty() && player.mayBuild()) {
                be.dropReplaceItem(player, player.getMainHandItem(), InteractionHand.MAIN_HAND);
            } else if (player.getMainHandItem().is(ItemTags.AXES) && !be.getTheItem().isEmpty() && player.mayBuild()) {
                be.dropReplaceItem(player, ItemStack.EMPTY, null);
            } else {
                var delta = state.getValue(WORLD_BOUND) ? 0.05f : 0.5f;
                var model = (Model) BlockAwareAttachment.get(world, pos).holder();
                var axisDir = state.getValue(FACING).getClockWise();
                var axis = hit.getLocation().get(axisDir.getAxis());

                model.spin(hit.getDirection().getAxis() == axisDir.getAxis() ? delta * (hit.getDirection().getAxisDirection() == axisDir.getAxisDirection() ? -1 : 1) : Mth.clamp(-((float) (axis - (int) axis) - 0.5f) * 20, -delta, delta));
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.SPRUCE_PLANKS.defaultBlockState();
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        tickWater(state, world, tickView, pos);
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        Containers.updateNeighboursAfterDestroy(state, world, pos);
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, initialBlockState);
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return GenericSingleItemBlockEntity.globe(pos, state);
    }

    public static final class Model extends BlockModel implements GenericSingleItemBlockEntity.ItemSetter {
        public static final ItemStack GLOBE_BASE = ItemDisplayElementUtil.getSolidModel(id("block/globe_base"));
        public static final ItemStack GLOBE_EARTH = ItemDisplayElementUtil.getSolidModel(id("block/globe_earth"));

        public static final ItemStack TATER = ItemDisplayElementUtil.getSolidModel(id("block/tiny_potato"));
        private final ServerLevel world;
        private final ItemDisplayElement main;
        private final LodItemDisplayElement rotating;
        private boolean worldBound;
        private float velocity = 0;
        private float rotation = 0;
        private float offset = 0;
        private float scale = 1;

        public Model(ServerLevel world, BlockState state) {
            this.worldBound = state.getValue(WORLD_BOUND);
            this.world = world;
            var direction = state.getValue(FACING).toYRot();

            this.main = ItemDisplayElementUtil.createSimple(GLOBE_BASE);
            this.main.setScale(new Vector3f(2));
            this.main.setYaw(direction);
            this.addElement(this.main);

            this.rotating = LodItemDisplayElement.createSimple();
            this.rotating.setScale(new Vector3f(2));
            this.rotating.setYaw(direction);
            this.rotating.setInterpolationDuration(1);
            this.rotating.setItemDisplayContext(ItemDisplayContext.NONE);
            setItem(ItemStack.EMPTY);
            this.addElement(this.rotating);
        }

        @Override
        protected void onTick() {
            if (this.velocity != 0 || this.worldBound) {
                if (this.worldBound) {
                    if (this.velocity != 0) {
                        this.world.setDayTime((long) (this.world.getDayTime() + this.velocity / Mth.TWO_PI * SharedConstants.TICKS_PER_GAME_DAY));
                        var packet = new ClientboundSetTimePacket(world.getGameTime(), world.getDayTime(), true);
                        for (var player : world.players()) {
                            player.connection.send(packet);
                        }
                    }

                    this.rotation = (this.world.getDayTime() * Mth.TWO_PI / SharedConstants.TICKS_PER_GAME_DAY) % Mth.TWO_PI;
                } else {
                    this.rotation = (this.rotation + this.velocity) % Mth.TWO_PI;
                }

                updateAngle();

                this.velocity = this.velocity < 0 ? Math.min(this.velocity + 0.01f, 0) : Math.max(this.velocity - 0.01f, 0);
            }
        }

        private void updateAngle() {
            this.rotating.setTransformation(mat()
                    .rotateZ(-Mth.HALF_PI / 4)
                    .translate(-1.5f / 16f, (6.5f + this.offset) / 16f + 1 / 6f / 16f, 0)
                    .rotateY(this.rotation).scale(this.scale)
            );

            this.rotating.startInterpolation();
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                this.worldBound = state.getValue(WORLD_BOUND);
                var direction = state.getValue(FACING).toYRot();
                this.main.setYaw(direction);
                this.rotating.setYaw(direction);
            }
        }

        public void spin(float velocity) {
            this.velocity = Mth.clamp(this.velocity + velocity, -Mth.HALF_PI, Mth.HALF_PI);
        }

        public void setItem(ItemStack item) {
            ItemStack model;
            if (item.isEmpty()) {
                model = GLOBE_EARTH;
                this.scale = 1f;
                this.offset = 0.5f;
            } else if (item.is(Items.POTATO)) {
                model = TATER;
                this.offset = 0;
                this.scale = 1;
            } else {
                if (item.is(DecorationsItemTags.UNSCALED_DISPLAY_CASE)) {
                    this.scale = 1f;
                    this.offset = 0.5f;
                } else {
                    this.scale = 0.5f;
                    this.offset = -3.5f;
                }
                model = item;
            }
            updateAngle();
            this.rotating.setItem(model);
            this.rotating.setItemDisplayContext(item.is(DecorationsItemTags.FORCE_FIXED_MODEL) ? ItemDisplayContext.FIXED : ItemDisplayContext.NONE);
            this.velocity = 0;
        }
    }
}
