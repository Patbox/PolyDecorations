package eu.pb4.polydecorations.block.item;

import com.mojang.authlib.GameProfile;
import eu.pb4.factorytools.api.block.BlockEntityExtraListener;
import eu.pb4.factorytools.api.block.OwnedBlockEntity;
import eu.pb4.factorytools.api.block.entity.LockableBlockEntity;
import eu.pb4.factorytools.api.util.LegacyNbtHelper;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.ui.GuiTextures;
import eu.pb4.polydecorations.util.DecorationsUtil;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;

public class MailboxBlockEntity extends LockableBlockEntity implements OwnedBlockEntity, BlockEntityExtraListener {

    private MailboxBlock.Model model;
    private int lastDirty = 0;
    private GameProfile owner = null;
    protected final HashMap<UUID, SimpleInventory> inventories = new HashMap<>();

    public MailboxBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DecorationsBlockEntities.MAILBOX, blockPos, blockState);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        if (this.owner != null) {
            nbt.put("owner", LegacyNbtHelper.writeGameProfile(new NbtCompound(), this.owner));
        }

        var inv = new NbtList();
        for (var x : inventories.entrySet()) {
            var cpd = new NbtCompound();
            Inventories.writeNbt(cpd, x.getValue().heldStacks, lookup);
            cpd.put("uuid", NbtHelper.fromUuid(x.getKey()));
            inv.add(cpd);
        }
        nbt.put("inventory", inv);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        if (nbt.contains("owner")) {
            this.owner = LegacyNbtHelper.toGameProfile(nbt.getCompound("owner"));
        } else {
            this.owner = null;
        }
        for (var x : this.inventories.values()) {
            x.clear();
        }
        this.inventories.clear();
        for (var x : nbt.getList("inventory", NbtElement.COMPOUND_TYPE)) {
            var cpd = (NbtCompound) x;
            var uuid = NbtHelper.toUuid(Objects.requireNonNull(cpd.get("uuid")));
            var inv = createInventory();
            Inventories.readNbt(cpd, inv.heldStacks, lookup);
            this.inventories.put(uuid, inv);
        }
        if (model != null) {
            model.setHasMail(!inventories.isEmpty());
        }
    }

    @Override
    protected Text getContainerName() {
        return DecorationsUtil.someones(owner, getCachedState().getBlock().getName());
    }

    private SimpleInventory createInventory() {
        return new SimpleInventory(6) {
            @Override
            public void markDirty() {
                super.markDirty();
                MailboxBlockEntity.this.markDirty();
            }
        };
    }


    public ActionResult onUse(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {

            if (((this.owner != null && player.getUuid().equals(this.owner.getId()))
                    || (this.getContainerLock() != ContainerLock.EMPTY && this.checkUnlocked(player, false)))
                    && !(player.isCreativeLevelTwoOp() && player.getStackInHand(Hand.MAIN_HAND).isOf(Items.DEBUG_STICK))) {
                new SelectorGui(serverPlayer);
            } else {
                new InventoryGui(serverPlayer, serverPlayer.getUuid(), true);
            }
        }

        return ActionResult.SUCCESS_SERVER;
    }

    @Override
    public GameProfile getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(GameProfile profile) {
        this.owner = profile;
        this.markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.lastDirty++;
    }

    @Override
    public void onListenerUpdate(WorldChunk chunk) {
        this.model = (MailboxBlock.Model) BlockAwareAttachment.get(chunk, pos).holder();

        this.model.setHasMail(!this.inventories.isEmpty());
    }

    private class SelectorGui extends SimpleGui {
        private static final int ENTRIES_PER_PAGE = 9 * 3;
        private final List<GuiElement> elements = new ArrayList<>();
        private int page;
        private int pageCount = 1;

        private int lastDirty = 0;

        public SelectorGui(ServerPlayerEntity player) {
            super(ScreenHandlerType.GENERIC_9X4, player, false);
            this.setTitle(GuiTextures.MAILBOX_SELECT.apply(getName()));
            this.update();
            this.lastDirty = MailboxBlockEntity.this.lastDirty;
            this.open();
        }

        private void update() {
            this.updateElements();
            this.drawUi();

        }

        private void drawUi() {
            var start = this.page * ENTRIES_PER_PAGE;
            var end = Math.min((this.page + 1) * ENTRIES_PER_PAGE, this.elements.size());

            int i = start;
            for (; i < end; i++) {
                this.setSlot(i, this.elements.get(i));
            }
            for (; i < ENTRIES_PER_PAGE; i++) {
                this.clearSlot(i);
            }

            if (this.page > 0) {
                this.setSlot(9 * 5 + 3, GuiTextures.PREVIOUS_PAGE_BUTTON.get()
                        .setName(Text.translatable("spectatorMenu.previous_page").formatted(Formatting.WHITE))
                        .setCallback((a, b, c) -> {
                            this.page -= 1;
                            this.drawUi();
                            clickSound();
                        })
                );
            } else if (this.page + 1 < this.pageCount) {
                this.setSlot(9 * 5 + 3, GuiTextures.NEXT_PAGE_BUTTON.get()
                        .setName(Text.translatable("spectatorMenu.next_page").formatted(Formatting.WHITE))
                        .setCallback((a, b, c) -> {
                            this.page += 1;
                            this.drawUi();
                            clickSound();
                        })
                );
            }
        }

        private void clickSound() {
            player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.5f, 1);
        }

        private void updateElements() {
            this.elements.clear();
            for (var entry : inventories.entrySet()) {
                var b = new GuiElementBuilder();
                var profile = this.player.server.getUserCache().getByUuid(entry.getKey());

                if (profile.isPresent()) {
                    b.setItem(Items.PLAYER_HEAD);
                    b.setSkullOwner(profile.get(), null);
                    b.setName(Text.literal(profile.get().getName()));
                } else {
                    b.setItem(Items.SKELETON_SKULL);
                    b.setName(Text.literal(entry.getKey().toString()).formatted(Formatting.DARK_RED));
                }

                for (var stack : entry.getValue().heldStacks) {
                    if (!stack.isEmpty()) {
                        b.addLoreLine(Text.translatable("container.shulkerBox.itemCount", stack.getName(), String.valueOf(stack.getCount())).formatted(Formatting.GRAY));
                    }
                }

                b.setCallback((a, g, c) -> {
                    clickSound();
                    if (inventories.get(entry.getKey()) != null) {
                        new InventoryGui(player, entry.getKey(), false);
                    } else {
                        this.update();
                    }
                });

                this.elements.add(b.build());
            }

            this.pageCount = (this.elements.size() / ENTRIES_PER_PAGE) + (((this.elements.size() % ENTRIES_PER_PAGE) == 0) ? 0 : 1);
        }

        @Override
        public void onTick() {
            if (isRemoved() || player.getPos().squaredDistanceTo(Vec3d.ofCenter(MailboxBlockEntity.this.pos)) > (18 * 18)) {
                this.close();
                return;
            }

            if (this.lastDirty != MailboxBlockEntity.this.lastDirty) {
                this.lastDirty = MailboxBlockEntity.this.lastDirty;
                this.update();
            }

            super.onTick();
        }
    }
    private class InventoryGui extends SimpleGui {
        private final SimpleInventory inventory;
        private final UUID target;

        public InventoryGui(ServerPlayerEntity player, UUID target, boolean canPutItems) {
            super(ScreenHandlerType.GENERIC_9X2, player, false);
            this.setTitle(GuiTextures.SHELF_2.apply(DecorationsUtil.someones(owner, getCachedState().getBlock().getName())));
            this.target = target;
            this.inventory = inventories.computeIfAbsent(target, (x) -> createInventory());
            this.setSlotRedirect(3, createSlot(inventory, 0, canPutItems));
            this.setSlotRedirect(4, createSlot(inventory, 1, canPutItems));
            this.setSlotRedirect(5, createSlot(inventory, 2, canPutItems));
            this.setSlotRedirect(3 + 9, createSlot(inventory, 3, canPutItems));
            this.setSlotRedirect(4 + 9, createSlot(inventory, 4, canPutItems));
            this.setSlotRedirect(5 + 9, createSlot(inventory, 5, canPutItems));

            var curr = GuiHelpers.getCurrentGui(player);

            if (curr instanceof SelectorGui selectorGui) {
                this.setSlot(9 + 8, GuiTextures.BACK_BUTTON.get().setName(ScreenTexts.BACK)
                        .setCallback((a, b, c) -> {
                            selectorGui.clickSound();
                            this.close(true);
                            selectorGui.open();
                        }));
            }

            this.open();
        }

        private Slot createSlot(SimpleInventory inventory, int i, boolean canPutItems) {
            return new Slot(inventory, i, i, 0) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return super.canInsert(stack) && canPutItems;
                };
            };
        }

        @Override
        public void onClose() {
            if (inventory.isEmpty()) {
                inventories.remove(this.target, inventory);
            } else if (inventories.get(this.target) == null) {
                inventories.put(this.target, inventory);
            } else if (inventories.get(this.target) != inventory) {
                ItemScatterer.spawn(getWorld(), getPos(), inventory);
            }
            if (model != null) {
                model.setHasMail(!inventories.isEmpty());
            }
            super.onClose();
        }

        @Override
        public void onTick() {
            if (isRemoved() || player.getPos().squaredDistanceTo(Vec3d.ofCenter(MailboxBlockEntity.this.pos)) > (18 * 18)) {
                this.close();
                return;
            }

            super.onTick();
        }
    }
}
