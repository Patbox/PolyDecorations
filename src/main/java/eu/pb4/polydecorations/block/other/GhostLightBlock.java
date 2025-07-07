package eu.pb4.polydecorations.block.other;

import eu.pb4.factorytools.api.block.CustomBreakingParticleBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class GhostLightBlock extends Block implements FactoryBlock, CustomBreakingParticleBlock {
    private final ParticleEffect effect;
    private final int rate;
    private final int count;
    private final float speed;

    public GhostLightBlock(Settings settings, int rate, int count, float speed, ParticleEffect effect) {
        super(settings);
        this.effect = effect;
        this.rate = rate;
        this.count = count;
        this.speed = speed;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.STRUCTURE_VOID.getDefaultState();
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Emitter();
    }

    @Override
    public boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Override
    public ParticleEffect getBreakingParticle(BlockState blockState) {
        return this.effect;
    }

    private class Emitter extends BlockModel {
        @Override
        public void tick() {
            super.tick();
            if (this.getTick() % rate == 0) {
                this.sendPacket(new ParticleS2CPacket(effect, false, false, this.getPos().x, this.getPos().y, this.getPos().z,
                        2 / 16f, 2 / 16f, 2 / 16f, speed, count));
            }
        }
    }
}
