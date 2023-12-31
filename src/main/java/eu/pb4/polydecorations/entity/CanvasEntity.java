package eu.pb4.polydecorations.entity;

import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.core.PlayerCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.api.utils.VirtualDisplay;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.block.MapColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static eu.pb4.polydecorations.ModInit.id;

public class CanvasEntity extends AbstractDecorationEntity implements PolymerEntity {
    private byte[] data = new byte[16 * 16];
    private final PlayerCanvas canvas;
    private VirtualDisplay display;
    private boolean glowing;
    private boolean waxed;

    @Nullable
    private Text name;

    public CanvasEntity(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
        super(entityType, world);
        this.canvas = DrawableCanvas.create();

        CanvasUtils.clear(this.canvas, CanvasColor.OFF_WHITE_NORMAL);
    }

    public static CanvasEntity create(World world, Direction side, BlockPos pos) {
        var entity = new CanvasEntity(DecorationsEntities.CANVAS, world);
        entity.attachmentPos = pos;
        entity.setFacing(side);
        return entity;
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        if (this.display == null) {
            this.display = VirtualDisplay.builder(this.canvas, this.attachmentPos, this.getHorizontalFacing())
                    .glowing(this.glowing)
                    .callback(this::onUsed)
                    .build();
        }
        this.canvas.addPlayer(player);
        this.display.addPlayer(player);
    }

    private void fromByteArray(byte[] data) {
        if (data.length == 0) {
            data = new byte[16 * 16];
        } else {
            data = Arrays.copyOf(data, data.length);
        }

        this.data = data;
        if (data.length == 16 * 16) {
            for (var x = 0; x < 16; x++) {
                for (var y = 0; y < 16; y++) {
                    CanvasUtils.fill(this.canvas, x * 8, y * 8, (x + 1) * 8, (y + 1) * 8, getColor(data[x + y * 16]));
                }
            }
        } else {
            DefaultFonts.VANILLA.drawText(this.canvas, "Invalid data!\nBytes found: " + data.length + "\nRequired: " + (16 * 16), 16, 16, 8, CanvasColor.RED_HIGH);
        }
    }

    private CanvasColor getColor(byte color) {
        if (color == 0) {
            return CanvasColor.OFF_WHITE_NORMAL;
        }
        return CanvasColor.getFromRaw(color);
    }

    private void onUsed(ServerPlayerEntity serverPlayerEntity, ClickType clickType, int x, int y) {
        if (clickType == ClickType.LEFT) {
            if (CommonProtection.canDamageEntity(this.getWorld(), this, serverPlayerEntity.getGameProfile(), serverPlayerEntity)) {
                serverPlayerEntity.attack(this);
            }
            return;
        }
        if (this.waxed || !CommonProtection.canInteractEntity(this.getWorld(), this, serverPlayerEntity.getGameProfile(), serverPlayerEntity)) {
            return;
        }

        x = x / 8;
        y = y / 8;

        int radius = 0;

        CanvasColor color = null;
        var stack = serverPlayerEntity.getMainHandStack();

        if (stack.isOf(Items.BRUSH)) {
            radius = 1;
            stack = serverPlayerEntity.getOffHandStack();
        }

        if (stack.isIn(ConventionalItemTags.DYES)) {
            if (stack.getItem() instanceof DyeItem dyeItem) {
                color = CanvasColor.from(dyeItem.getColor().getMapColor(), MapColor.Brightness.NORMAL);
            } else if (stack.hasNbt() && stack.getNbt().contains("color", NbtElement.INT_TYPE)) {
                color = CanvasUtils.findClosestColor(stack.getNbt().getInt("color"));
            }
        } else if (stack.isOf(Items.SPONGE)) {
            color = CanvasColor.CLEAR;
        } else if (stack.isOf(Items.COAL)) {
            var raw = CanvasColor.getFromRaw(this.data[x + y * 16]);
            if (raw.getBrightness() != MapColor.Brightness.LOWEST) {
                color = CanvasColor.from(raw.getColor(), switch (raw.getBrightness()) {
                    case HIGH -> MapColor.Brightness.NORMAL;
                    case NORMAL -> MapColor.Brightness.LOW;
                    default -> MapColor.Brightness.LOWEST;
                });
            }
        } else if (stack.isOf(Items.BONE_MEAL)) {
            var raw = CanvasColor.getFromRaw(this.data[x + y * 16]);
            if (raw.getBrightness() != MapColor.Brightness.HIGH) {
                color = CanvasColor.from(raw.getColor(), switch (raw.getBrightness()) {
                    case LOWEST -> MapColor.Brightness.LOW;
                    case LOW -> MapColor.Brightness.NORMAL;
                    default -> MapColor.Brightness.HIGH;
                });
            }
        }

        if (color == null) {
            return;
        }
        var tColor = getColor(color.getRenderColor());

        for (var xi = x - radius; xi <= x + radius; xi++) {
            for (var yi = y - radius; yi <= y + radius; yi++) {
                if (xi < 0 || xi >= 16 || yi < 0 || yi >= 16) {
                    continue;
                }

                CanvasUtils.fill(this.canvas, xi * 8, yi * 8, (xi + 1) * 8, (yi + 1) * 8, tColor);
                this.data[xi + yi * 16] = color.getRenderColor();
            }
        }


        serverPlayerEntity.swingHand(Hand.MAIN_HAND, true);

        if (this.display != null) {
            this.canvas.sendUpdates();
        }
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        this.canvas.removePlayer(player);
        if (this.display != null) {
            this.display.removePlayer(player);
        }
    }

    @Override
    public void onRemoved() {
        if (this.display != null) {
            this.display.destroy();
            this.display = null;
        }

        super.onRemoved();
    }

    public void loadFromStack(ItemStack stack) {
        if (!stack.hasNbt()) {
            return;
        }

        if (stack.getNbt().contains("data", NbtElement.BYTE_ARRAY_TYPE)) {
            this.fromByteArray(stack.getNbt().getByteArray("data"));
        }

        this.glowing = stack.getNbt().getBoolean("glowing");
        this.waxed = stack.getNbt().getBoolean("waxed");
        this.name = stack.hasCustomName() ? stack.getName() : null;
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.facing = Direction.fromHorizontal(nbt.getByte("facing"));
        fromByteArray(nbt.getByteArray("data"));
        this.glowing = nbt.getBoolean("glowing");
        this.waxed = nbt.getBoolean("waxed");
        if (nbt.contains("name")) {
            this.name = Text.Serialization.fromLenientJson(nbt.getString("name"));
        }
        this.setFacing(this.facing);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putByte("facing", (byte)this.facing.getHorizontal());
        nbt.putByteArray("data", Arrays.copyOf(this.data, this.data.length));
        nbt.putBoolean("glowing", this.glowing);
        nbt.putBoolean("waxed", this.waxed);
        if (this.name != null) {
            nbt.putString("name", Text.Serialization.toJsonString(this.name));
        }
    }

    @Override
    public int getWidthPixels() {
        return 16;
    }

    @Override
    public int getHeightPixels() {
        return 16;
    }

    @Override
    public void onPlace() {
        this.playSound(SoundEvent.of(id("entity.canvas.place")), 1.0F, 1.0F);
    }
    @Override
    public void onBreak(@Nullable Entity entity) {
        this.playSound(SoundEvent.of(id("entity.canvas.break")), 1.0F, 1.0F);

        var stack = this.toStack();
        if (entity instanceof PlayerEntity player && player.isCreative() && (!stack.hasNbt() || !stack.getNbt().contains("data"))) {
            return;
        }

        this.dropStack(stack);
    }

    @Nullable
    @Override
    public ItemStack getPickBlockStack() {
        return toStack();
    }

    private ItemStack toStack() {
        var stack = new ItemStack(DecorationsItems.CANVAS);
        for (byte b : this.data) {
            if (b != 0) {
                stack.getOrCreateNbt().putByteArray("data", Arrays.copyOf(this.data, this.data.length));
                break;
            }
        }

        if (this.waxed) {
            stack.getOrCreateNbt().putBoolean("waxed", true);
        }

        if (this.glowing) {
            stack.getOrCreateNbt().putBoolean("glowing", true);
        }

        if (this.name != null) {
            stack.setCustomName(this.name);
        }

        return stack;
    }


    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
        return EntityType.MARKER;
    }
}
