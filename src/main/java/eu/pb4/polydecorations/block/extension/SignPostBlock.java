package eu.pb4.polydecorations.block.extension;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.QuickWaterloggable;
import eu.pb4.factorytools.api.virtualentity.BaseModel;
import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Map;

public class SignPostBlock extends BlockWithEntity implements PolymerBlock, BlockWithElementHolder, QuickWaterloggable {
    public static final Map<Block, SignPostBlock> MAP = new Reference2ObjectOpenHashMap<>();

    private final FenceBlock fence;

    public SignPostBlock(FenceBlock block) {
        super(AbstractBlock.Settings.copy(block).nonOpaque().dropsLike(block));
        this.fence = block;
        MAP.put(block, this);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return this.fence;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.getPolymerBlock(state).getDefaultState().with(WATERLOGGED, state.get(WATERLOGGED));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.MAIN_HAND && world.getBlockEntity(pos) instanceof SignPostBlockEntity be) {
            return be.onUse(player, hit.getPos().getY() - (int) hit.getPos().getY() >= 0.5, hit);
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        tickWater(state, world, pos);
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof SignPostBlockEntity be) {
            ItemScatterer.spawn(world, pos, DefaultedList.copyOf(ItemStack.EMPTY, be.lowerText().item().getDefaultStack(), be.upperText().item().getDefaultStack()));
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SignPostBlockEntity(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model();
    }

    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    public FenceBlock getBacking() {
        return this.fence;
    }

    public static final class Model extends BaseModel {
        private final LodItemDisplayElement upperBack;
        private final TextDisplayElement upperText;
        private final LodItemDisplayElement lowerBack;
        private final TextDisplayElement lowerText;


        public Model() {
            this.upperBack = LodItemDisplayElement.createSimple();
            this.upperBack.setScale(new Vector3f(2));
            this.upperBack.setDisplaySize(1, 1);

            this.upperText = new TextDisplayElement();
            this.upperText.setBackground(0);
            this.upperText.setScale(new Vector3f(0.5f));
            this.upperText.setTranslation(new Vector3f(0, 3.5f / 16f, 3.1f / 16f));
            this.upperText.setDisplaySize(1, 1);
            this.upperText.setInvisible(true);

            this.lowerBack = LodItemDisplayElement.createSimple();
            this.lowerBack.setScale(new Vector3f(2));
            this.lowerBack.setDisplaySize(1, 1);

            this.lowerText = new TextDisplayElement();
            this.lowerText.setInvisible(true);
            this.lowerText.setBackground(0);
            this.lowerText.setScale(new Vector3f(0.5f));
            this.lowerText.setTranslation(new Vector3f(0, -4.5f / 16f, 3.1f / 16f));
            this.lowerText.setDisplaySize(1, 1);

            this.addElement(this.upperBack);
            this.addElement(this.upperText);
            this.addElement(this.lowerBack);
            this.addElement(this.lowerText);
        }

        public void update(SignPostBlockEntity.Sign upperText, SignPostBlockEntity.Sign lowerText) {
            updateUpper(upperText);
            updateLower(lowerText);
        }

        public void updateUpper(SignPostBlockEntity.Sign upperText) {
            this.upperText.setText(upperText.getText());
            this.upperText.setBrightness(upperText.text().isGlowing() ? new Brightness(15, 15) : null);
            this.upperText.setYaw(upperText.yaw());
            this.upperText.tick();

            this.upperBack.setYaw(upperText.flip() ? 180 + upperText.yaw() : upperText.yaw());
            this.upperBack.setTranslation(new Vector3f(0, 4 / 16f, (upperText.flip() ? -2.5f : 2.5f) / 16f));
            this.upperBack.setItem(LodItemDisplayElement.getModel(upperText.item()));
            this.upperBack.tick();
        }
        public void updateLower(SignPostBlockEntity.Sign lowerText) {
            this.lowerText.setText(lowerText.getText());
            this.lowerText.setYaw(lowerText.yaw());
            this.lowerText.setBrightness(lowerText.text().isGlowing() ? new Brightness(15, 15) : null);
            this.lowerText.tick();

            this.lowerBack.setYaw(lowerText.flip() ? 180 + lowerText.yaw() : lowerText.yaw());
            this.lowerBack.setTranslation(new Vector3f(0, -4 / 16f, (lowerText.flip() ? -2.5f : 2.5f) / 16f));
            this.lowerBack.setItem(LodItemDisplayElement.getModel(lowerText.item()));
            this.lowerBack.tick();
        }
    }
}
