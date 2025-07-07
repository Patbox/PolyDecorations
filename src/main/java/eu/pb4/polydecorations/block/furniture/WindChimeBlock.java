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
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import xyz.nucleoid.packettweaker.PacketContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static eu.pb4.polydecorations.ModInit.id;

public class WindChimeBlock extends BlockWithEntity implements FactoryBlock {
    public WindChimeBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        var upState = world.getBlockState(pos.offset(Direction.UP));
        return upState.isSideSolid(world, pos.up(), Direction.DOWN, SideShapeType.CENTER) || upState.isOf(DecorationsBlocks.ROPE);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return Blocks.BARRIER.getDefaultState();
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.SPRUCE_PLANKS.getDefaultState();
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return super.onUse(state, world, pos, player, hit);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WindChimeBlockEntity(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    @Override
    public boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    public static final class Model extends BlockModel implements WindChimeBlockEntity.ColorSetter {
        private static final Vec3d[] CHIME_POSITIONS;
        private final ItemDisplayElement main;
        private final ItemDisplayElement[] chimes = new ItemDisplayElement[5];

        private static final SoundEvent CHIME = SoundEvent.of(id("block.wind_chime.chime"), 7);
        private IntList color;

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(id("block/wind_chime/base"));
            this.main.setDisplaySize(1, 1);
            this.addElement(this.main);

            for (var i = 0; i < 5; i++) {
                this.chimes[i] = ItemDisplayElementUtil.createSimple(id("block/wind_chime/chime_" + i));
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
            var t = getTick() + (int) this.getPos().squaredDistanceTo(Vec3d.ZERO);
            var q = new Quaternionf();

            var isOutside = this.getAttachment().getWorld().getLightLevel(LightType.SKY, this.blockPos()) > 7;

            var stormSpeedMult = 1;// + this.getAttachment().getWorld().getRainGradient(1) / 5;
            var stormStrengthMult = 1 + this.getAttachment().getWorld().getRainGradient(1);


            if (isOutside) {
                q.rotateX(MathHelper.sin((float) (t / 30f * stormSpeedMult + this.getPos().z / 240f)) * 0.12f * stormStrengthMult);
                q.rotateZ(MathHelper.sin((float) (t / 30f * stormSpeedMult + this.getPos().x / 131f)) * 0.12f * stormStrengthMult);
            }
            var rand = Random.create(123);

            for (var i = 0; i < 5; i++) {
                var q2 = new Quaternionf(q);
                if (isOutside) {
                    q2.rotateX(MathHelper.sin((float) (t / 8f * stormSpeedMult + rand.nextGaussian() + this.getPos().z / 10f)) * 0.03f * stormStrengthMult);
                    q2.rotateZ(MathHelper.sin((float) (t / 8f * stormSpeedMult + rand.nextGaussian() + this.getPos().x / 10f)) * 0.03f * stormStrengthMult);
                }
                this.chimes[i].setLeftRotation(q2);
                this.chimes[i].startInterpolationIfDirty();
            }

            rand = Random.create();
            if (isOutside && t % 20 == 0 && rand.nextFloat() > 0.3) {
                this.getAttachment().getWorld().playSound(null, this.blockPos(), CHIME, SoundCategory.BLOCKS, rand.nextFloat() / 1.5f, (rand.nextFloat() - 0.5f) * 0.5f + 1);
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
                    this.chimes[i].setItem(ItemDisplayElementUtil.getModel(id("block/wind_chime/chime_" + i)));
                }
            } else {
                for (var i = 0; i < 5; i++) {
                    var model = ItemDisplayElementUtil.getModelCopy(id("block/wind_chime/chime_" + i));
                    model.set(DataComponentTypes.CUSTOM_MODEL_DATA,
                            new CustomModelDataComponent(List.of(), List.of(), List.of(), IntList.of(colors.getInt(i % colors.size()))));
                    this.chimes[i].setItem(model);
                }
            }
        }

        static {
            var list = new Vec3d[]{Vec3d.ZERO,Vec3d.ZERO,Vec3d.ZERO,Vec3d.ZERO,Vec3d.ZERO};
            try {
                var s = Model.class.getResourceAsStream("/wind_chime_offsets.json");
                list = Vec3d.CODEC.listOf().decode(JsonOps.INSTANCE, JsonParser.parseString(new String(s.readAllBytes(), StandardCharsets.UTF_8))).getOrThrow().getFirst()
                        .stream().map(x -> x.multiply(1 / 16f).subtract(0.5)).toArray(Vec3d[]::new);
                s.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }

            CHIME_POSITIONS = list;
        }
    }
}
