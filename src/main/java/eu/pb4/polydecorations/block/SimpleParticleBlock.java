package eu.pb4.polydecorations.block;

import eu.pb4.factorytools.api.block.CustomBreakingParticleBlock;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.IdentityHashMap;
import java.util.Map;

public interface SimpleParticleBlock extends CustomBreakingParticleBlock {
    Map<Block, ParticleOptions> PARTICLE_OPTIONS_MAP = new IdentityHashMap<>();

    @Override
    default ParticleOptions getBreakingParticle(BlockState blockState) {
        return PARTICLE_OPTIONS_MAP.computeIfAbsent(blockState.getBlock(), this::computeParticle);
    }

    default ParticleOptions computeParticle(Block block) {
        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(block.asItem()));
    }
}
