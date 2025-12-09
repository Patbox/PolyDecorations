package eu.pb4.polydecorations.block.item;

import com.mojang.authlib.GameProfile;
import eu.pb4.factorytools.api.block.BlockEntityExtraListener;
import eu.pb4.factorytools.api.block.OwnedBlockEntity;
import eu.pb4.factorytools.api.block.entity.LockableBlockEntity;
import eu.pb4.factorytools.api.util.LegacyNbtHelper;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.ui.GuiTextures;
import eu.pb4.polydecorations.ui.GuiUtils;
import eu.pb4.polydecorations.util.DecorationsUtil;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.*;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.LockCode;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public class MailboxBlockEntity extends LockableBlockEntity implements OwnedBlockEntity, BlockEntityExtraListener {

    private MailboxBlock.Model model;
    private int lastDirty = 0;
    private GameProfile owner = null;
    protected final HashMap<UUID, SimpleContainer> inventories = new HashMap<>();

    public MailboxBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DecorationsBlockEntities.MAILBOX, blockPos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        if (this.owner != null) {
            view.store("owner", CompoundTag.CODEC, LegacyNbtHelper.writeGameProfile(new CompoundTag(), this.owner));
        }

        var inv = view.childrenList("inventory");
        for (var x : inventories.entrySet()) {
            var cpd = inv.addChild();
            ContainerHelper.saveAllItems(cpd, x.getValue().items);
            cpd.store("uuid", UUIDUtil.AUTHLIB_CODEC, x.getKey());
        }
    }

    @Override
    public void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.owner = view.read("owner", CompoundTag.CODEC).map(LegacyNbtHelper::toGameProfile).orElse(null);

        for (var x : this.inventories.values()) {
            x.clearContent();
        }
        this.inventories.clear();
        for (var cpd : view.childrenListOrEmpty(  "inventory")) {
            var uuid = cpd.read("uuid", UUIDUtil.AUTHLIB_CODEC).orElse(Util.NIL_UUID);
            var inv = createInventory();
            ContainerHelper.loadAllItems(cpd, inv.items);
            this.inventories.put(uuid, inv);

        }
        if (model != null) {
            model.setHasMail(!inventories.isEmpty());
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        if (this.level != null) {
            for (var value : this.inventories.values()) {
                Containers.dropContents(level, pos, value);
            }
        }
    }

    @Override
    protected Component getContainerName() {
        return DecorationsUtil.someones(owner, getBlockState().getBlock().getName());
    }

    private SimpleContainer createInventory() {
        return new SimpleContainer(6) {
            @Override
            public void setChanged() {
                super.setChanged();
                MailboxBlockEntity.this.setChanged();
            }
        };
    }


    public InteractionResult onUse(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {

            if (((this.owner != null && player.getUUID().equals(this.owner.id()))
                    || (this.getContainerLock() != LockCode.NO_LOCK && this.checkUnlocked(player, false)))
                    && !(player.canUseGameMasterBlocks() && player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.DEBUG_STICK))) {
                new SelectorGui(serverPlayer);
            } else {
                new InventoryGui(serverPlayer, serverPlayer.getUUID(), true);
            }
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public GameProfile getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(GameProfile profile) {
        this.owner = profile;
        this.setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.lastDirty++;
    }

    @Override
    public void onListenerUpdate(LevelChunk chunk) {
        this.model = (MailboxBlock.Model) BlockAwareAttachment.get(chunk, worldPosition).holder();

        this.model.setHasMail(!this.inventories.isEmpty());
    }

    private class SelectorGui extends SimpleGui {
        private static final int ENTRIES_PER_PAGE = 9 * 3;
        private final List<GuiElement> elements = new ArrayList<>();
        private int page;
        private int pageCount = 1;

        private int lastDirty = 0;

        public SelectorGui(ServerPlayer player) {
            super(MenuType.GENERIC_9x4, player, false);
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
                        .setName(Component.translatable("spectatorMenu.previous_page").withStyle(ChatFormatting.WHITE))
                        .setCallback((a, b, c) -> {
                            this.page -= 1;
                            this.drawUi();
                            clickSound();
                        })
                );
            } else if (this.page + 1 < this.pageCount) {
                this.setSlot(9 * 5 + 3, GuiTextures.NEXT_PAGE_BUTTON.get()
                        .setName(Component.translatable("spectatorMenu.next_page").withStyle(ChatFormatting.WHITE))
                        .setCallback((a, b, c) -> {
                            this.page += 1;
                            this.drawUi();
                            clickSound();
                        })
                );
            }
        }

        private void clickSound() {
            GuiUtils.playClickSound(player);
        }

        private void updateElements() {
            this.elements.clear();
            for (var entry : inventories.entrySet()) {
                var b = new GuiElementBuilder();
                var profile = this.player.level().getServer().services().nameToIdCache().get(entry.getKey());

                if (profile.isPresent()) {
                    b.setItem(Items.PLAYER_HEAD);
                    b.setProfile(profile.get().id());
                    b.setName(Component.literal(profile.get().name()));
                } else {
                    b.setItem(Items.SKELETON_SKULL);
                    b.setName(Component.literal(entry.getKey().toString()).withStyle(ChatFormatting.DARK_RED));
                }

                for (var stack : entry.getValue().items) {
                    if (!stack.isEmpty()) {
                        b.addLoreLine(Component.translatable("item.container.item_count", stack.getHoverName(), String.valueOf(stack.getCount())).withStyle(ChatFormatting.GRAY));
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

                this.elements.add(b.hideDefaultTooltip().build());
            }

            this.pageCount = (this.elements.size() / ENTRIES_PER_PAGE) + (((this.elements.size() % ENTRIES_PER_PAGE) == 0) ? 0 : 1);
        }

        @Override
        public void onTick() {
            if (isRemoved() || player.position().distanceToSqr(Vec3.atCenterOf(MailboxBlockEntity.this.worldPosition)) > (18 * 18)) {
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
        private final SimpleContainer inventory;
        private final UUID target;

        public InventoryGui(ServerPlayer player, UUID target, boolean canPutItems) {
            super(MenuType.GENERIC_9x2, player, false);
            this.setTitle(GuiTextures.SHELF_2.apply(DecorationsUtil.someones(owner, getBlockState().getBlock().getName())));
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
                this.setSlot(9 + 8, GuiTextures.BACK_BUTTON.get().setName(CommonComponents.GUI_BACK)
                        .setCallback((a, b, c) -> {
                            selectorGui.clickSound();
                            this.close(true);
                            selectorGui.open();
                        }));
            }

            this.open();
        }

        private Slot createSlot(SimpleContainer inventory, int i, boolean canPutItems) {
            return new Slot(inventory, i, i, 0) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return super.mayPlace(stack) && canPutItems;
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
                Containers.dropContents(getLevel(), getBlockPos(), inventory);
            }
            if (model != null) {
                model.setHasMail(!inventories.isEmpty());
            }
            super.onClose();
        }

        @Override
        public void onTick() {
            if (isRemoved() || player.position().distanceToSqr(Vec3.atCenterOf(MailboxBlockEntity.this.worldPosition)) > (18 * 18)) {
                this.close();
                return;
            }

            super.onTick();
        }
    }
}
