package eu.pb4.polydecorations.block.extension;

import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.QuickWaterloggable;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import eu.pb4.polydecorations.ModInit;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.block.SimpleParticleBlock;
import eu.pb4.polydecorations.model.SignLikeText;
import eu.pb4.polydecorations.util.ProxyAttachement;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

public class AttachedSignPostBlock extends BaseEntityBlock implements PolymerBlock, BlockWithElementHolder, QuickWaterloggable, SimpleParticleBlock {
    public static final Map<Block, AttachedSignPostBlock> MAP = new Reference2ObjectOpenHashMap<>();

    private final Block baseBlock;
    private final float radius;

    public AttachedSignPostBlock(BlockBehaviour.Properties settings, Block block, int pixelSideLength) {
        super(settings.noOcclusion().overrideLootTable(block.getLootTable()));
        this.baseBlock = block;
        this.radius = pixelSideLength / 16f / 2f;
        MAP.put(block, this);
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
        ModInit.LATE_INIT.add(() -> DecorationsBlockEntities.SIGN_POST.addSupportedBlock(this));
    }

    @Override
    public MutableComponent getName() {
        return Component.translatable("block.polydecorations.sign_post_typed", this.baseBlock.getName());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }
    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return this.baseBlock.defaultBlockState().trySetValue(WATERLOGGED, state.getValue(WATERLOGGED));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof SignPostBlockEntity be && player.mayBuild()) {
            return be.onUse(player, hit.getLocation().y() - (int) hit.getLocation().y() >= 0.5, hit);
        }

        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        tickWater(state, world, tickView, pos);
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SignPostBlockEntity(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, pos);
    }

    @Override
    public ParticleOptions computeParticle(Block block) {
        return SimpleParticleBlock.super.computeParticle(this.baseBlock);
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    public Block getBacking() {
        return this.baseBlock;
    }

    public final class Model extends BlockModel {
        private final ItemDisplayElement upperBack;
        private final SignLikeText upperText;
        private final ItemDisplayElement lowerBack;
        private final SignLikeText lowerText;

        @Nullable
        ProxyAttachement proxyAttachement;


        public Model(ServerLevel world, BlockPos pos) {
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

            if (BlockWithElementHolder.get(baseBlock.defaultBlockState()) instanceof BlockWithElementHolder b) {
                this.proxyAttachement = new ProxyAttachement(this, b.createElementHolder(world, pos, baseBlock.defaultBlockState()), baseBlock::defaultBlockState);
                this.proxyAttachement.holder().setAttachment(this.proxyAttachement);
                this.addElement(this.proxyAttachement);
            }

        }

        public void update(SignPostBlockEntity.Sign upperText, SignPostBlockEntity.Sign lowerText) {
            updateUpper(upperText);
            updateLower(lowerText);
        }

        public void updateUpper(SignPostBlockEntity.Sign upperText) {
            var cos = Mth.abs(Mth.cos(upperText.yaw() * Mth.DEG_TO_RAD));
            var sin = Mth.abs(Mth.sin(upperText.yaw() * Mth.DEG_TO_RAD));
            var max = Math.min(cos, sin);

            var r = Mth.sqrt(Mth.square(radius * max) + Mth.square(radius));

            var zOffset = (upperText.flip() ? -1 : 1) * (r + 0.5f / 16f);
            var zOffsetText = (r + 1.05f / 16f);

            this.upperText.setText(upperText.getUncoloredText(), upperText.dye(), upperText.glowing());
            this.upperText.setTranslation(new Vector3f(0, 3.5f / 16f, zOffsetText));
            this.upperText.setYaw(upperText.yaw());
            this.upperText.tick();

            this.upperBack.setTeleportDuration(0);
            this.upperBack.setYaw(upperText.flip() ? 180 + upperText.yaw() : upperText.yaw());
            this.upperBack.setTranslation(new Vector3f(0, 4 / 16f, zOffset));
            this.upperBack.setItem(ItemDisplayElementUtil.getSolidModel(upperText.item()));
            this.upperBack.tick();
            this.upperBack.setTeleportDuration(1);
            this.upperBack.tick();

        }
        public void updateLower(SignPostBlockEntity.Sign lowerText) {
            var cos = Mth.abs(Mth.cos(lowerText.yaw() * Mth.DEG_TO_RAD));
            var sin = Mth.abs(Mth.sin(lowerText.yaw() * Mth.DEG_TO_RAD));
            var max = Math.min(cos, sin);

            var r = Mth.sqrt(Mth.square(radius * max) + Mth.square(radius));

            var zOffset = (lowerText.flip() ? -1 : 1) * (r + 0.5f / 16f);
            var zOffsetText = (r + 1.05f / 16f);

            this.lowerText.setText(lowerText.getUncoloredText(), lowerText.dye(), lowerText.glowing());
            this.lowerText.setYaw(lowerText.yaw());
            this.lowerText.setTranslation(new Vector3f(0, -4.5f / 16f, zOffsetText));
            this.lowerText.tick();

            this.lowerBack.setTeleportDuration(0);
            this.lowerBack.setYaw(lowerText.flip() ? 180 + lowerText.yaw() : lowerText.yaw());
            this.lowerBack.setTranslation(new Vector3f(0, -4 / 16f, zOffset));
            this.lowerBack.setItem(ItemDisplayElementUtil.getSolidModel(lowerText.item()));
            this.lowerBack.tick();
            this.lowerBack.setTeleportDuration(1);
            this.lowerBack.tick();
        }
    }
}
