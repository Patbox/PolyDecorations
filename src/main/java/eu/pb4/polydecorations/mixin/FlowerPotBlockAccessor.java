package eu.pb4.polydecorations.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;

@Mixin(FlowerPotBlock.class)
public interface FlowerPotBlockAccessor {
    @Accessor
    static Map<Block, Block> getPOTTED_BY_CONTENT() {
        throw new UnsupportedOperationException();
    }
}
