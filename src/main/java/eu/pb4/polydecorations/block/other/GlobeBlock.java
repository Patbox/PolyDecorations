package eu.pb4.polydecorations.block.other;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.resourcepack.BaseItemProvider;
import eu.pb4.factorytools.api.virtualentity.BaseModel;
import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import eu.pb4.polydecorations.item.DecorationsItemTags;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.apache.logging.log4j.core.jmx.Server;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static eu.pb4.polydecorations.util.DecorationsUtil.id;

public class GlobeBlock extends BlockWithEntity implements FactoryBlock, BarrierBasedWaterloggable {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty WORLD_BOUND = BooleanProperty.of("worldbound");

    public GlobeBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false).with(WORLD_BOUND, false));
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING, WORLD_BOUND);
    }


    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return waterLog(ctx, this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite()));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.MAIN_HAND && !player.isSneaking() && world.getBlockEntity(pos) instanceof GlobeBlockEntity be) {
            if (player.getMainHandStack().isIn(DecorationsItemTags.GLOBE_REPLACEMENT) && be.getStack().isEmpty()) {
                be.replaceItem(player, player.getMainHandStack(), hand);
            } else if (player.getMainHandStack().isIn(ItemTags.AXES) && !be.getStack().isEmpty()) {
                be.replaceItem(player, ItemStack.EMPTY, null);
            } else {
                var delta = state.get(WORLD_BOUND) ? 0.05f : 0.5f;
                var model = (Model) BlockBoundAttachment.get(world, pos).holder();
                var axisDir = state.get(FACING).rotateYClockwise();
                var axis = hit.getPos().getComponentAlongAxis(axisDir.getAxis());

                model.spin(
                        hit.getSide().getAxis() == axisDir.getAxis()
                                ? delta * (hit.getSide().getDirection() == axisDir.getDirection() ? -1 : 1)
                                : MathHelper.clamp(-((float) (axis - (int) axis) - 0.5f) * 20, -delta, delta)
                );
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }


    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        tickWater(state, world, pos);
        return state;
    }

    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        ItemScatterer.onStateReplaced(state, newState, world, pos);
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, initialBlockState);
    }

    @Override
    public boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GlobeBlockEntity(pos, state);
    }

    public static final class Model extends BaseModel {
        public static final ItemStack GLOBE_BASE = BaseItemProvider.requestModel(id("block/globe_base"));
        public static final ItemStack GLOBE_EARTH = BaseItemProvider.requestModel(id("block/globe_earth"));

        public static final ItemStack TATER = BaseItemProvider.requestModel(id("block/tiny_potato"));
        private final ServerWorld world;
        private boolean worldBound;
        private float velocity = 0;
        private float rotation = 0;
        private float offset = 0;
        private float scale = 1;
        private final LodItemDisplayElement main;
        private final LodItemDisplayElement rotating;

        public Model(ServerWorld world, BlockState state) {
            this.worldBound = state.get(WORLD_BOUND);
            this.world = world;
            var direction = state.get(FACING).asRotation();

            this.main = LodItemDisplayElement.createSimple(GLOBE_BASE);
            this.main.setScale(new Vector3f(2));
            this.main.setYaw(direction);
            this.addElement(this.main);

            this.rotating = LodItemDisplayElement.createSimple();
            this.rotating.setScale(new Vector3f(2));
            this.rotating.setYaw(direction);
            this.rotating.setInterpolationDuration(1);
            this.rotating.setModelTransformation(ModelTransformationMode.NONE);
            setItem(TATER);
            this.addElement(this.rotating);
        }

        @Override
        protected void onTick() {
            if (this.velocity != 0 || this.worldBound) {
                if (this.worldBound) {
                    if (this.velocity != 0) {
                        this.world.setTimeOfDay((long) (this.world.getTimeOfDay() + this.velocity / MathHelper.TAU * SharedConstants.TICKS_PER_IN_GAME_DAY));
                        var packet = new WorldTimeUpdateS2CPacket(world.getTime(), world.getTimeOfDay(), true);
                        for (var player : world.getPlayers()) {
                            player.networkHandler.sendPacket(packet);
                        }
                    }

                    this.rotation = (this.world.getTimeOfDay() * MathHelper.TAU / SharedConstants.TICKS_PER_IN_GAME_DAY) % MathHelper.TAU;
                } else {
                    this.rotation = (this.rotation + this.velocity) % MathHelper.TAU;
                }

                updateAngle();

                this.velocity = this.velocity < 0 ? Math.min(this.velocity + 0.01f, 0): Math.max(this.velocity - 0.01f, 0);
            }
        }

        private void updateAngle() {
            this.rotating.setTransformation(
                    mat.identity()
                            .rotateZ(-MathHelper.HALF_PI / 4)
                            .translate(-1.5f / 16f, (6.5f + this.offset) / 16f + 1 / 6f / 16f, 0)
                            .rotateY(this.rotation)
                            .scale(this.scale)
            );

            this.rotating.startInterpolation();
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockBound().getBlockState();
                this.worldBound = state.get(WORLD_BOUND);
                var direction = state.get(FACING).asRotation();
                this.main.setYaw(direction);
                this.rotating.setYaw(direction);
            }
        }

        public void spin(float velocity) {
            this.velocity = MathHelper.clamp(this.velocity + velocity, -MathHelper.HALF_PI, MathHelper.HALF_PI);
        }

        public void setItem(ItemStack item) {
            ItemStack model;
            if (item.isEmpty()) {
                model = GLOBE_EARTH;
                this.scale = 1f;
                this.offset = 0.5f;
            } else if (item.isOf(Items.POTATO)) {
                model = TATER;
                this.offset = 0;
                this.scale = 1;
            } else {
                if (item.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SkullBlock) {
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
            this.velocity = 0;
        }
    }
}
