package eu.pb4.polydecorations.block.item;

import eu.pb4.factorytools.api.block.BlockEntityExtraListener;
import eu.pb4.factorytools.api.block.entity.LockableBlockEntity;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.block.DecorationsBlockTags;
import eu.pb4.polydecorations.item.DecorationsItemTags;
import eu.pb4.polydecorations.ui.GuiTextures;
import eu.pb4.polydecorations.util.MinimalInventory;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.sgui.api.gui.SimpleGui;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ToolRackBlockEntity extends LockableBlockEntity implements MinimalInventory, WorldlyContainer, BlockEntityExtraListener {
    private static final int[] SLOTS = IntStream.range(0, 4).toArray();
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private ToolRackBlock.Model model;

    public ToolRackBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DecorationsBlockEntities.TOOL_RACK, blockPos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ContainerHelper.saveAllItems(view, this.items);
    }

    @Override
    public void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        ContainerHelper.loadAllItems(view, this.items);
        if (this.model != null) {
            this.model.updateItems(this.getStacks());
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.model != null) {
            this.model.updateItems(this.getStacks());
        }
    }

    @Override
    public NonNullList<ItemStack> getStacks() {
        return this.items;
    }

    @Override
    public void onListenerUpdate(LevelChunk chunk) {
        try {
            this.model = (ToolRackBlock.Model) BlockAwareAttachment.get(chunk, this.getBlockPos()).holder();
            this.model.updateItems(this.items);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }


    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return stack.is(DecorationsItemTags.TOOL_RACK_ACCEPTABLE);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getStacks()));
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.getStacks());
    }

    @Override
    public void removeComponentsFromTag(ValueOutput view) {
        super.removeComponentsFromTag(view);
        view.discard("Items");
    }
}
