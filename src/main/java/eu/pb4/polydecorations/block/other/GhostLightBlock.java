package eu.pb4.polydecorations.block.other;

import eu.pb4.factorytools.api.block.CustomBreakingParticleBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class GhostLightBlock extends Block implements FactoryBlock, CustomBreakingParticleBlock {
    private final ParticleOptions effect;
    private final int rate;
    private final int count;
    private final float speed;

    public GhostLightBlock(Properties settings, int rate, int count, float speed, ParticleOptions effect) {
        super(settings);
        this.effect = effect;
        this.rate = rate;
        this.count = count;
        this.speed = speed;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.STRUCTURE_VOID.defaultBlockState();
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Emitter();
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Override
    public ParticleOptions getBreakingParticle(BlockState blockState) {
        return this.effect;
    }

    private class Emitter extends BlockModel {
        @Override
        public void tick() {
            super.tick();
            if (this.getTick() % rate == 0) {
                this.sendPacket(new ClientboundLevelParticlesPacket(effect, false, false, this.getPos().x, this.getPos().y, this.getPos().z,
                        2 / 16f, 2 / 16f, 2 / 16f, speed, count));
            }
        }
    }
}
