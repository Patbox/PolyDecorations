package eu.pb4.polydecorations.block.furniture;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.BarrierBasedWaterloggable;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.block.SimpleParticleBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class TableBlock extends Block implements FactoryBlock, BarrierBasedWaterloggable, SimpleParticleBlock {
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

    public TableBlock(Identifier identifier, Properties settings, Block planks) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
        this.model = TableModel.of(identifier);
        this.base = planks;
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return this.base.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH_EAST.property, NORTH_WEST.property, SOUTH_EAST.property, SOUTH_WEST.property, WATERLOGGED);
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        var state = this.defaultBlockState();

        for (var corner : CORNERS) {
            var val = true;
            for (var off : corner.offsets) {
                if (ctx.getLevel().getBlockState(ctx.getClickedPos().offset(off)).is(this)) {
                    val = false;
                    break;
                }
            }
            state = state.setValue(corner.property, val);
        }

        return waterLog(ctx, state);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        tickWater(state, world, tickView, pos);

        var corners = DIR_TO_CORNERS.get(direction);

        if (corners == null) {
            return state;
        }

        for (var corner : corners) {
            var val = true;
            for (var off : corner.offsets) {
                if (world.getBlockState(pos.offset(off)).is(this)) {
                    val = false;
                    break;
                }
            }
            state = state.setValue(corner.property, val);
        }

        return state;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public record Corner(int id, String name, BooleanProperty property, Direction dir1, Direction dir2,
                         List<Vec3i> offsets) {
        private static int gId = 0;

        private static Corner of(Direction dir, Direction dir2) {
            return new Corner(gId++, dir.getSerializedName() + "_" + dir2.getSerializedName(), BooleanProperty.create(dir.getSerializedName() + "_" + dir2.getSerializedName() + "_corner"), dir, dir2,
                    List.of(dir.getUnitVec3i(), dir2.getUnitVec3i()/*,  dir.getVector().add(dir2.getVector())*/));
        }
    }

    public record TableModel(ItemStack[] models) {
        public static final int COUNT = (int) Math.pow(2, 4);

        private static TableModel of(Identifier identifier) {
            var models = new ItemStack[COUNT];
            models[0] = ItemDisplayElementUtil.getSolidModel(identifier.withPrefix("block/"));

            for (int i = 1; i < COUNT; i++) {
                models[i] = ItemDisplayElementUtil.getSolidModel(identifier.withPrefix("block/").withSuffix("_" + i));
            }

            return new TableModel(models);
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

        public ItemStack get(BlockState state) {
            int i = 0;
            for (var corner : CORNERS) {
                if (!state.getValue(corner.property)) {
                    i |= 1 << corner.id;
                }
            }
            return this.models[i];
        }
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
}
