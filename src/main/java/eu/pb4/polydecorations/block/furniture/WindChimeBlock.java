package eu.pb4.polydecorations.block.furniture;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.block.DecorationsBlocks;
import eu.pb4.polydecorations.util.ResourceUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import xyz.nucleoid.packettweaker.PacketContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static eu.pb4.polydecorations.ModInit.id;

public class WindChimeBlock extends BaseEntityBlock implements FactoryBlock {
    public WindChimeBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        var upState = world.getBlockState(pos.relative(Direction.UP));
        return upState.isFaceSturdy(world, pos.above(), Direction.DOWN, SupportType.CENTER) || upState.is(DecorationsBlocks.ROPE);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return Blocks.BARRIER.defaultBlockState();
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.SPRUCE_PLANKS.defaultBlockState();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WindChimeBlockEntity(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    public static final class Model extends BlockModel implements WindChimeBlockEntity.ColorSetter {
        private static final Vec3[] CHIME_POSITIONS;
        private final ItemDisplayElement main;
        private final ItemDisplayElement[] chimes = new ItemDisplayElement[5];

        private static final SoundEvent CHIME = SoundEvent.createFixedRangeEvent(id("block.wind_chime.chime"), 7);
        private IntList color;

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSolid(id("block/wind_chime/base"));
            this.main.setDisplaySize(1, 1);
            this.addElement(this.main);

            for (var i = 0; i < 5; i++) {
                this.chimes[i] = ItemDisplayElementUtil.createSolid(id("block/wind_chime/chime_" + i));
                this.chimes[i].setViewRange(1);
                this.chimes[i].setDisplaySize(1, 1);
                this.chimes[i].setInterpolationDuration(5);
                this.chimes[i].setItemDisplayContext(ItemDisplayContext.NONE);
                this.chimes[i].setOffset(CHIME_POSITIONS[i]);
                this.addElement(chimes[i]);
            }
        }

        @Override
        protected void onTick() {
            var t = getTick() + (int) this.getPos().distanceToSqr(Vec3.ZERO);
            var q = new Quaternionf();

            var isOutside = this.getAttachment().getWorld().getBrightness(LightLayer.SKY, this.blockPos()) > 7;

            var stormSpeedMult = 1;// + this.getAttachment().getWorld().getRainGradient(1) / 5;
            var stormStrengthMult = 1 + this.getAttachment().getWorld().getRainLevel(1);


            if (isOutside) {
                q.rotateX(Mth.sin((float) (t / 30f * stormSpeedMult + this.getPos().z / 240f)) * 0.12f * stormStrengthMult);
                q.rotateZ(Mth.sin((float) (t / 30f * stormSpeedMult + this.getPos().x / 131f)) * 0.12f * stormStrengthMult);
            }
            var rand = RandomSource.create(123);

            for (var i = 0; i < 5; i++) {
                var q2 = new Quaternionf(q);
                if (isOutside) {
                    q2.rotateX(Mth.sin((float) (t / 8f * stormSpeedMult + rand.nextGaussian() + this.getPos().z / 10f)) * 0.03f * stormStrengthMult);
                    q2.rotateZ(Mth.sin((float) (t / 8f * stormSpeedMult + rand.nextGaussian() + this.getPos().x / 10f)) * 0.03f * stormStrengthMult);
                }
                this.chimes[i].setLeftRotation(q2);
                this.chimes[i].startInterpolationIfDirty();
            }

            rand = RandomSource.create();
            if (isOutside && t % 20 == 0 && rand.nextFloat() > 0.3) {
                this.getAttachment().getWorld().playSound(null, this.blockPos(), CHIME, SoundSource.BLOCKS, rand.nextFloat() / 1.5f, (rand.nextFloat() - 0.5f) * 0.5f + 1);
            }


            super.onTick();
        }

        @Override
        public void setColors(IntList colors) {
            if (colors.equals(this.color)) {
                return;
            }
            this.color = colors;
            if (colors.isEmpty()) {
                for (var i = 0; i < 5; i++) {
                    this.chimes[i].setItem(ItemDisplayElementUtil.getSolidModel(id("block/wind_chime/chime_" + i)));
                }
            } else {
                for (var i = 0; i < 5; i++) {
                    var model = ItemDisplayElementUtil.getSolidModelCopy(id("block/wind_chime/chime_" + i));
                    model.set(DataComponents.CUSTOM_MODEL_DATA,
                            new CustomModelData(List.of(), List.of(), List.of(), IntList.of(colors.getInt(i % colors.size()))));
                    this.chimes[i].setItem(model);
                }
            }
        }

        static {
            var list = new Vec3[]{Vec3.ZERO,Vec3.ZERO,Vec3.ZERO,Vec3.ZERO,Vec3.ZERO};
            try {
                var s = Model.class.getResourceAsStream("/wind_chime_offsets.json");
                list = Vec3.CODEC.listOf().decode(JsonOps.INSTANCE, JsonParser.parseString(new String(s.readAllBytes(), StandardCharsets.UTF_8))).getOrThrow().getFirst()
                        .stream().map(x -> x.scale(1 / 16f).subtract(0.5)).toArray(Vec3[]::new);
                s.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }

            CHIME_POSITIONS = list;
        }
    }
}
