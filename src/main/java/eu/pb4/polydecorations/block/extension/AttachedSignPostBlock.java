package eu.pb4.polydecorations.block.extension;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.QuickWaterloggable;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import eu.pb4.polydecorations.model.SignLikeText;
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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

public class AttachedSignPostBlock extends BlockWithEntity implements PolymerBlock, BlockWithElementHolder, QuickWaterloggable {
    public static final Map<Block, AttachedSignPostBlock> MAP = new Reference2ObjectOpenHashMap<>();

    private final Block baseBlock;
    private final float radius;

    public AttachedSignPostBlock(AbstractBlock.Settings settings, Block block, int pixelSideLength) {
        super(settings.nonOpaque().lootTable(block.getLootTableKey()));
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
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return this.baseBlock.getDefaultState().withIfExists(WATERLOGGED, state.get(WATERLOGGED));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof SignPostBlockEntity be && player.canModifyBlocks()) {
            return be.onUse(player, hit.getPos().getY() - (int) hit.getPos().getY() >= 0.5, hit);
        }

        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        tickWater(state, world, tickView, pos);
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
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
        private final SignLikeText upperText;
        private final ItemDisplayElement lowerBack;
        private final SignLikeText lowerText;


        public Model() {
            this.upperBack = ItemDisplayElementUtil.createSimple();
            this.upperBack.setScale(new Vector3f(2));
            this.upperBack.setDisplaySize(1, 1);

            this.upperText = new SignLikeText();
            this.upperText.setViewRange(0.6f);
            this.upperText.setScale(new Vector3f(0.5f));
            this.upperText.setDisplaySize(1, 1);

            this.lowerBack = LodItemDisplayElement.createSimple();
            this.lowerBack.setScale(new Vector3f(2));
            this.lowerBack.setDisplaySize(1, 1);

            this.lowerText = new SignLikeText();
            this.lowerText.setViewRange(0.6f);
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
            var zOffsetText = (r + 1.05f / 16f);

            this.upperText.setText(upperText.getUncoloredText(), upperText.dye(), upperText.glowing());
            this.upperText.setTranslation(new Vector3f(0, 3.5f / 16f, zOffsetText));
            this.upperText.setYaw(upperText.yaw());
            this.upperText.tick();

            this.upperBack.setTeleportDuration(0);
            this.upperBack.setYaw(upperText.flip() ? 180 + upperText.yaw() : upperText.yaw());
            this.upperBack.setTranslation(new Vector3f(0, 4 / 16f, zOffset));
            this.upperBack.setItem(ItemDisplayElementUtil.getModel(upperText.item()));
            this.upperBack.tick();
            this.upperBack.setTeleportDuration(1);
            this.upperBack.tick();

        }
        public void updateLower(SignPostBlockEntity.Sign lowerText) {
            var cos = MathHelper.abs(MathHelper.cos(lowerText.yaw() * MathHelper.RADIANS_PER_DEGREE));
            var sin = MathHelper.abs(MathHelper.sin(lowerText.yaw() * MathHelper.RADIANS_PER_DEGREE));
            var max = Math.min(cos, sin);

            var r = MathHelper.sqrt(MathHelper.square(radius * max) + MathHelper.square(radius));

            var zOffset = (lowerText.flip() ? -1 : 1) * (r + 0.5f / 16f);
            var zOffsetText = (r + 1.05f / 16f);

            this.lowerText.setText(lowerText.getUncoloredText(), lowerText.dye(), lowerText.glowing());
            this.lowerText.setYaw(lowerText.yaw());
            this.lowerText.setTranslation(new Vector3f(0, -4.5f / 16f, zOffsetText));
            this.lowerText.tick();

            this.lowerBack.setTeleportDuration(0);
            this.lowerBack.setYaw(lowerText.flip() ? 180 + lowerText.yaw() : lowerText.yaw());
            this.lowerBack.setTranslation(new Vector3f(0, -4 / 16f, zOffset));
            this.lowerBack.setItem(ItemDisplayElementUtil.getModel(lowerText.item()));
            this.lowerBack.tick();
            this.lowerBack.setTeleportDuration(1);
            this.lowerBack.tick();
        }
    }
}
