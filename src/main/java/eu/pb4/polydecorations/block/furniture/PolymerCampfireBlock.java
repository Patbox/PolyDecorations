package eu.pb4.polydecorations.block.furniture;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.util.DecorationsUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import static eu.pb4.polydecorations.ModInit.id;

public class PolymerCampfireBlock extends CampfireBlock implements FactoryBlock, PolymerTexturedBlock {
    public PolymerCampfireBlock(boolean emitsParticles, int fireDamage, Settings settings) {
        super(emitsParticles, fireDamage, settings);
        BlockEntityType.CAMPFIRE.addSupportedBlock(this);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        if (blockState.get(LIT)) {
            return blockState.get(WATERLOGGED) ? DecorationsUtil.CAMPFIRE_WATERLOGGED_STATE : DecorationsUtil.CAMPFIRE_STATE;
        } else {
            return Blocks.CAMPFIRE.getStateWithProperties(blockState);
        }
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public static final class Model extends BlockModel {
        private final ItemDisplayElement main;

        private static final ItemStack MODEL = ItemDisplayElementUtil.getModel(id("block/copper_campfire"));

        public Model(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(state.get(LIT) ? MODEL : ItemStack.EMPTY);
            this.main.setDisplaySize(1, 1);
            this.main.setYaw(state.get(FACING).getPositiveHorizontalDegrees() + 180);
            this.main.setBrightness(state.get(LIT) ? new Brightness(15, 15) : null);
            this.main.setScale(new Vector3f(2));
            this.addElement(this.main);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockState();
                this.main.setBrightness(state.get(LIT) ? new Brightness(15, 15) : null);
                this.main.setItem(state.get(LIT) ? MODEL : ItemStack.EMPTY);
                this.main.setYaw(state.get(FACING).getPositiveHorizontalDegrees() + 180);

                this.tick();
            }
        }
    }
}
