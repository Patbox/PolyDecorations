package eu.pb4.polydecorations.block.furniture;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.util.LazyItemStack;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.util.DecorationsUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Brightness;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import static eu.pb4.polydecorations.ModInit.id;

public class PolymerCampfireBlock extends CampfireBlock implements FactoryBlock, PolymerTexturedBlock {
    public PolymerCampfireBlock(boolean emitsParticles, int fireDamage, Properties settings) {
        super(emitsParticles, fireDamage, settings);
        BlockEntityTypes.CAMPFIRE.addValidBlock(this);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        if (blockState.getValue(LIT)) {
            return blockState.getValue(WATERLOGGED) ? DecorationsUtil.CAMPFIRE_WATERLOGGED_STATE : DecorationsUtil.CAMPFIRE_STATE;
        } else {
            return Blocks.CAMPFIRE.withPropertiesOf(blockState);
        }
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    public static final class Model extends BlockModel {
        private final RandomSource randomSource = RandomSource.create();
        private final ItemDisplayElement main;

        private static final LazyItemStack MODEL = ItemDisplayElementUtil.getModel(id("block/copper_campfire"));

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(state.getValue(LIT) ? MODEL.get() : ItemStack.EMPTY);
            this.main.setDisplaySize(1, 1);
            this.main.setYaw(state.getValue(FACING).toYRot() + 180);
            this.main.setBrightness(state.getValue(LIT) ? new Brightness(15, 15) : null);
            this.main.setScale(new Vector3f(2));
            this.addElement(this.main);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                this.main.setBrightness(state.getValue(LIT) ? new Brightness(15, 15) : null);
                this.main.setItem(state.getValue(LIT) ? MODEL.get() : ItemStack.EMPTY);
                this.main.setYaw(state.getValue(FACING).toYRot() + 180);

                this.tick();
            }
        }
        @Override
        protected void onTick() {
            var state = this.blockState();
            var blockPos = this.blockPos();

            if (state.getValue(CampfireBlock.LIT)) {
                if (randomSource.nextFloat() < 0.11F) {
                    var smoke = state.getValue(CampfireBlock.SIGNAL_FIRE) ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;

                    for(int i = 0; i < randomSource.nextInt(2) + 2; ++i) {
                        addParticle(smoke, true, (double)blockPos.getX() + (double)0.5F + randomSource.nextDouble() / (double)3.0F * (double)(randomSource.nextBoolean() ? 1 : -1), (double)blockPos.getY() + randomSource.nextDouble() + randomSource.nextDouble(), (double)blockPos.getZ() + (double)0.5F + randomSource.nextDouble() / (double)3.0F * (double)(randomSource.nextBoolean() ? 1 : -1), (double)0.0F, 0.07, (double)0.0F);
                    }
                }
                if (randomSource.nextInt(64) == 0) {
                    if (randomSource.nextInt(10) == 0) {
                        playLocalSound((double) blockPos.getX() + (double) 0.5F, (double) blockPos.getY() + (double) 0.5F, (double) blockPos.getZ() + (double) 0.5F, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + randomSource.nextFloat(), randomSource.nextFloat() * 0.7F + 0.6F, false);
                    }

                    if (randomSource.nextInt(5) == 0) {
                        for (int i = 0; i < randomSource.nextInt(1) + 1; ++i) {
                            addParticle(ParticleTypes.LAVA, false, (double) blockPos.getX() + (double) 0.5F, (double) blockPos.getY() + (double) 0.5F, (double) blockPos.getZ() + (double) 0.5F, (double) (randomSource.nextFloat() / 2.0F), 5.0E-5, (double) (randomSource.nextFloat() / 2.0F));
                        }
                    }
                }
            }

            super.onTick();
        }

        private void addParticle(ParticleOptions type, boolean alwaysVisible, double x, double y, double z, double dx, double dy, double dz) {
            if (this.getAttachment() != null) {
                this.getAttachment().getWorld().sendParticles(type, false, alwaysVisible, x, y, z, 0, dx, dy, dz, 1);
            }
        }

        private void playLocalSound(double x, double y, double z, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, boolean force) {
            if (this.getAttachment() != null) {
                this.getAttachment().getWorld().playSound(null, x, y, z, soundEvent, soundSource, volume, pitch);
            }
        }
    }
}
