package eu.pb4.polydecorations.block.plus;

import eu.pb4.factorytools.api.resourcepack.BaseItemProvider;
import eu.pb4.factorytools.api.virtualentity.BaseModel;
import eu.pb4.factorytools.api.virtualentity.LodItemDisplayElement;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;


public class BedWithBannerBlock extends BedBlock implements PolymerBlock, BlockWithElementHolder {
    private final BedBlock bedBlock;

    public BedWithBannerBlock(BedBlock bedBlock) {
        super(bedBlock.getColor(), Settings.copy(bedBlock));
        this.bedBlock = bedBlock;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world.getBlockEntity(pos) instanceof BedWithBannerBlockEntity be) {
            be.setBanner(getBanner(itemStack));
        }
    }

    public static ItemStack getBanner(ItemStack itemStack) {
        return ItemStack.fromNbt(itemStack.getOrCreateNbt().getCompound("banner"));
    }


    public static void withBanner(ItemStack itemStack, ItemStack banner) {
        itemStack.getOrCreateNbt().put("banner", banner.writeNbt(new NbtCompound()));
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return this.bedBlock;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.bedBlock.getStateWithProperties(state);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return state.get(PART) == BedPart.FOOT ? new BedWithBannerBlockEntity(pos, state, this.bedBlock.getColor()) : null;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public BedBlock getBacking() {
        return this.bedBlock;
    }

    public final static class Model extends BaseModel {
        public final LodItemDisplayElement main;

        private Model(BlockState state) {
            this.main = LodItemDisplayElement.createSimple();
            this.main.setDisplaySize(2, 2);
            this.main.setModelTransformation(ModelTransformationMode.NONE);
            this.main.setTransformation(mat.identity()
                    .translate(0, 0.1f, 0.5f)
                    .rotateZ(MathHelper.PI)
                    .rotateX((-0.0125F + 0.01F * MathHelper.cos(0)) * (float) Math.PI)
                    .rotateX(-MathHelper.PI/2)
                    .scale(12 / 16f, 12 / 16f, 12 / 16f)
            );
            this.main.setYaw(state.get(FACING).asRotation());
            this.addElement(main);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                var state = this.blockBound().getBlockState();
                this.main.setYaw(state.get(FACING).asRotation());
                this.tick();
            }
        }
    }
}
