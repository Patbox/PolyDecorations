package eu.pb4.polydecorations.block.furniture;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.resourcepack.BaseItemProvider;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.entity.SeatEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TableBlock extends Block implements FactoryBlock, BarrierBasedWaterloggable {
    public static final Corner NORTH_WEST = Corner.of(Direction.NORTH, Direction.WEST);
    public static final Corner NORTH_EAST = Corner.of(Direction.NORTH, Direction.EAST);
    public static final Corner SOUTH_WEST = Corner.of(Direction.SOUTH, Direction.WEST);
    public static final Corner SOUTH_EAST = Corner.of(Direction.SOUTH, Direction.EAST);
    public static final List<Corner> CORNERS = List.of(NORTH_WEST, NORTH_EAST, SOUTH_WEST, SOUTH_EAST);
    public static final Map<Direction, List<Corner>> DIR_TO_CORNERS = ImmutableMap.<Direction, List<Corner>>builder()
            .put(Direction.NORTH, List.of(NORTH_EAST, NORTH_WEST))
            .put(Direction.SOUTH, List.of(SOUTH_EAST, SOUTH_WEST))
            .put(Direction.EAST, List.of(SOUTH_EAST, NORTH_EAST))
            .put(Direction.WEST, List.of(SOUTH_WEST, NORTH_WEST))
            .build();
    private final Block base;
    private final TableModel model;

    public TableBlock(Identifier identifier, Settings settings, Block planks) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
        this.model = TableModel.of(identifier);
        this.base = planks;
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayerEntity player) {
        return this.base.getDefaultState();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH_EAST.property, NORTH_WEST.property, SOUTH_EAST.property, SOUTH_WEST.property, WATERLOGGED);
    }

    public FluidState getFluidState(BlockState state) {
        return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        var state = this.getDefaultState();

        for (var corner : CORNERS) {
            var val = true;
            for (var off : corner.offsets) {
                if (ctx.getWorld().getBlockState(ctx.getBlockPos().add(off)).isOf(this)) {
                    val = false;
                    break;
                }
            }
            state = state.with(corner.property, val);
        }

        return waterLog(ctx, state);
    }
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        tickWater(state, world, pos);

        var corners = DIR_TO_CORNERS.get(direction);

        if (corners == null) {
            return state;
        }

        for (var corner : corners) {
            var val = true;
            for (var off : corner.offsets) {
                if (world.getBlockState(pos.add(off)).isOf(this)) {
                    val = false;
                    break;
                }
            }
            state = state.with(corner.property, val);
        }

        return state;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public final class Model extends BlockModel {
        private final ItemDisplayElement main;

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(TableBlock.this.model.get(state));
            this.main.setDisplaySize(1, 1);
            this.main.setScale(new Vector3f(2));
            this.main.setYaw(180);
            this.addElement(this.main);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                this.main.setItem(model.get(state));

                this.tick();
            }
        }
    }

    public record Corner(int id, String name, BooleanProperty property, Direction dir1, Direction dir2, List<Vec3i> offsets) {
        private static int gId = 0;
        private static Corner of(Direction dir, Direction dir2) {
            return new Corner(gId++, dir.getName() + "_" + dir2.getName(), BooleanProperty.of(dir.getName() + "_" + dir2.getName() + "_corner"), dir, dir2,
                    List.of(dir.getVector(), dir2.getVector()/*,  dir.getVector().add(dir2.getVector())*/));
        }
    }

    public record TableModel(ItemStack[] models) {
        public static final int COUNT = (int) Math.pow(2, 4);
        private static TableModel of(Identifier identifier) {
            var models = new ItemStack[COUNT];
            models[0] = BaseItemProvider.requestModel(BaseItemProvider.requestModel(), identifier.withPrefixedPath("block/"));

            for (int i = 1; i < COUNT; i++) {
                models[i] = BaseItemProvider.requestModel(BaseItemProvider.requestModel(), identifier.withPrefixedPath("block/").withSuffixedPath("_" + i));
            }

            return new TableModel(models);
        }

        public ItemStack get(BlockState state) {
            int i = 0;
            for (var corner : CORNERS) {
                if (!state.get(corner.property)) {
                    i |= 1 << corner.id;
                }
            }
            return this.models[i];
        }

        public static List<Corner> toCorners(int i) {
            var list = new ArrayList<Corner>();
            for (int a = 0; a < CORNERS.size(); a++) {
                if (((i >> a) & 0x1) == 0) {
                    list.add(CORNERS.get(a));
                }
            }

            return list;
        }

        public static List<String> toCornerNames(int i) {
            var list = new ArrayList<String>();
            for (int a = 0; a < CORNERS.size(); a++) {
                if (((i >> a) & 0x1) == 0) {
                    list.add(CORNERS.get(a).name);
                }
            }

            return list;
        }
    }
}
