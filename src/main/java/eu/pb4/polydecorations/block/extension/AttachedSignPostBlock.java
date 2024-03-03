package eu.pb4.polydecorations.block.extension;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.QuickWaterloggable;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Map;

public class AttachedSignPostBlock extends BlockWithEntity implements PolymerBlock, BlockWithElementHolder, QuickWaterloggable {
    public static final Map<Block, AttachedSignPostBlock> MAP = new Reference2ObjectOpenHashMap<>();

    private final Block baseBlock;
    private final float radius;

    public AttachedSignPostBlock(Block block, int pixelSideLength) {
        super(AbstractBlock.Settings.copy(block).nonOpaque().dropsLike(block));
        this.baseBlock = block;
        this.radius = pixelSideLength / 16f / 2f;
        MAP.put(block, this);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    public MutableText getName() {
        return Text.translatable("block.polydecorations.sign_post_typed", this.baseBlock.getName());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return this.baseBlock;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.getPolymerBlock(state).getDefaultState().withIfExists(WATERLOGGED, state.get(WATERLOGGED));
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

    public Block getBacking() {
        return this.baseBlock;
    }

    public final class Model extends BlockModel {
        private final ItemDisplayElement upperBack;
        private final TextDisplayElement upperText;
        private final ItemDisplayElement lowerBack;
        private final TextDisplayElement lowerText;


        public Model() {
            this.upperBack = ItemDisplayElementUtil.createSimple();
            this.upperBack.setScale(new Vector3f(2));
            this.upperBack.setDisplaySize(1, 1);

            this.upperText = new TextDisplayElement();
            this.upperText.setBackground(0);
            this.upperText.setScale(new Vector3f(0.5f));
            this.upperText.setDisplaySize(1, 1);
            this.upperText.setInvisible(true);

            this.lowerBack = LodItemDisplayElement.createSimple();
            this.lowerBack.setScale(new Vector3f(2));
            this.lowerBack.setDisplaySize(1, 1);

            this.lowerText = new TextDisplayElement();
            this.lowerText.setInvisible(true);
            this.lowerText.setBackground(0);
            this.lowerText.setScale(new Vector3f(0.5f));
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
            var cos = MathHelper.abs(MathHelper.cos(upperText.yaw() * MathHelper.RADIANS_PER_DEGREE));
            var sin = MathHelper.abs(MathHelper.sin(upperText.yaw() * MathHelper.RADIANS_PER_DEGREE));
            var max = Math.min(cos, sin);

            var r = MathHelper.sqrt(MathHelper.square(radius * max) + MathHelper.square(radius));

            var zOffset = (upperText.flip() ? -1 : 1) * (r + 0.5f / 16f);
            var zOffsetText = (upperText.flip() ? -1 : 1) * (r + 0.65f / 16f);

            this.upperText.setText(upperText.getText());
            this.upperText.setBrightness(upperText.text().isGlowing() ? new Brightness(15, 15) : null);
            this.upperText.setTranslation(new Vector3f(0, 4 / 16f, zOffsetText));
            this.upperText.setYaw(upperText.yaw());
            this.upperText.tick();

            this.upperBack.setYaw(upperText.flip() ? 180 + upperText.yaw() : upperText.yaw());
            this.upperBack.setTranslation(new Vector3f(0, 4 / 16f, zOffset));
            this.upperBack.setItem(ItemDisplayElementUtil.getModel(upperText.item()));
            this.upperBack.tick();
        }
        public void updateLower(SignPostBlockEntity.Sign lowerText) {
            var cos = MathHelper.abs(MathHelper.cos(lowerText.yaw() * MathHelper.RADIANS_PER_DEGREE));
            var sin = MathHelper.abs(MathHelper.sin(lowerText.yaw() * MathHelper.RADIANS_PER_DEGREE));
            var max = Math.min(cos, sin);

            var r = MathHelper.sqrt(MathHelper.square(radius * max) + MathHelper.square(radius));

            var zOffset = (lowerText.flip() ? -1 : 1) * (r + 0.5f / 16f);
            var zOffsetText = (lowerText.flip() ? -1 : 1) * (r + 0.65f / 16f);

            this.lowerText.setText(lowerText.getText());
            this.lowerText.setYaw(lowerText.yaw());
            this.lowerText.setBrightness(lowerText.text().isGlowing() ? new Brightness(15, 15) : null);
            this.lowerText.setTranslation(new Vector3f(0, -4 / 16f, zOffsetText));
            this.lowerText.tick();

            this.lowerBack.setYaw(lowerText.flip() ? 180 + lowerText.yaw() : lowerText.yaw());
            this.lowerBack.setTranslation(new Vector3f(0, -4 / 16f, zOffset));
            this.lowerBack.setItem(ItemDisplayElementUtil.getModel(lowerText.item()));
            this.lowerBack.tick();
        }
    }
}
