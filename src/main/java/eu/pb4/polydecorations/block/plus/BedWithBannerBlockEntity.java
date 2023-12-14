package eu.pb4.polydecorations.block.plus;

import eu.pb4.factorytools.api.block.BlockEntityExtraListener;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BedPart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

public class BedWithBannerBlockEntity extends BedBlockEntity implements BlockEntityExtraListener {
    private ItemStack banner = ItemStack.EMPTY;
    private BedWithBannerBlock.Model model;

    public BedWithBannerBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, state.getBlock() instanceof BedBlock bedBlock ? bedBlock.getColor() : DyeColor.BLUE);
    }
    public BedWithBannerBlockEntity(BlockPos pos, BlockState state, DyeColor color) {
        super(pos, state, color);
    }

    public ItemStack banner() {
        return banner;
    }

    public void setBanner(ItemStack banner) {
        this.banner = banner;
        if (this.model != null) {
            this.model.main.setItem(this.banner.copy());
            this.model.main.tick();
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("banner", this.banner.writeNbt(new NbtCompound()));
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        setBanner(ItemStack.fromNbt(nbt.getCompound("banner")));
    }

    @Override
    public void onListenerUpdate(WorldChunk chunk) {
        if (this.getCachedState().get(BedBlock.PART) == BedPart.FOOT) {
            this.model = (BedWithBannerBlock.Model) BlockBoundAttachment.get(chunk, this.pos).holder();
            this.model.main.setItem(this.banner.copy());
        }
    }

    @Override
    public BlockEntityType<?> getType() {
        return null;//DecorationsBlockEntities.BANNER_BED;
    }
}
