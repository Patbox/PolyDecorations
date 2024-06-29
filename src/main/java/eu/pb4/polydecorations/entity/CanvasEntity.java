package eu.pb4.polydecorations.entity;

import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.core.PlayerCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.api.utils.VirtualDisplay;
import eu.pb4.polydecorations.item.CanvasItem;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.MapColor;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static eu.pb4.polydecorations.ModInit.id;

public class CanvasEntity extends AbstractDecorationEntity implements PolymerEntity {
    private byte[] data = new byte[16 * 16];
    private final PlayerCanvas canvas;
    private VirtualDisplay display;
    private boolean glowing;
    private boolean waxed;
    private boolean cut;
    private Optional<CanvasColor> background = Optional.empty();
    private final Set<ServerPlayerEntity> players = new HashSet<>();

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType"})
    @Nullable
    private static Optional<ComponentType<Integer>> POLYFACTORY_COLOR = null;

    @Nullable
    private Text name;

    public CanvasEntity(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
        super(entityType, world);
        this.canvas = DrawableCanvas.create();

        CanvasUtils.clear(this.canvas, CanvasColor.OFF_WHITE_NORMAL);
    }

    public static Optional<CanvasColor> getColor(ItemStack stack) {
        //noinspection OptionalAssignedToNull
        if (POLYFACTORY_COLOR == null) {
            //noinspection unchecked
            POLYFACTORY_COLOR = Optional.ofNullable((ComponentType<Integer>) Registries.DATA_COMPONENT_TYPE.get(Identifier.of("polyfactory:color")));
        }
        if (stack.getItem() instanceof DyeItem dyeItem) {
            return Optional.ofNullable(CanvasColor.from(dyeItem.getColor().getMapColor(), MapColor.Brightness.NORMAL));
        } else if (POLYFACTORY_COLOR.isPresent() && stack.contains(POLYFACTORY_COLOR.get())) {
            //noinspection DataFlowIssue
            return Optional.ofNullable(CanvasUtils.findClosestColor(stack.get(POLYFACTORY_COLOR.get())));
        }
        return Optional.empty();
    }

    @Override
    protected Box calculateBoundingBox(BlockPos pos, Direction side) {
        Vec3d vec3d = Vec3d.ofCenter(pos).offset(side, -0.46875);
        Direction.Axis axis = side.getAxis();
        double d = axis == Direction.Axis.X ? 0.0625 : 1;
        double e = axis == Direction.Axis.Y ? 0.0625 : 1;
        double g = axis == Direction.Axis.Z ? 0.0625 : 1;
        return Box.of(vec3d, d, e, g);
    }

    public static CanvasEntity create(World world, Direction side, BlockPos pos) {
        var entity = new CanvasEntity(DecorationsEntities.CANVAS, world);
        entity.attachedBlockPos = pos;
        entity.setFacing(side);
        return entity;
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        if (this.display == null) {
            this.setupDisplay();
        }
        this.players.add(player);
        this.canvas.addPlayer(player);
        this.display.addPlayer(player);
    }

    private void setupDisplay() {
        this.display = VirtualDisplay.builder(this.canvas, this.attachedBlockPos, this.getHorizontalFacing())
                .glowing(this.glowing)
                .invisible(this.cut)
                .callback(this::onUsed)
                .build();
    }

    private void rebuildDisplay() {
        if (this.display != null) {
            this.display.destroy();
            setupDisplay();
            this.players.forEach(this.display::addPlayer);
        }
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
            return this.background.orElse(CanvasColor.OFF_WHITE_NORMAL);
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

        var raw = CanvasColor.getFromRaw(this.data[x + y * 16]);

        if (this.cut && raw == CanvasColor.CLEAR_FORCE) {
            return;
        }

        if (stack.isIn(ConventionalItemTags.DYES)) {
            var a = getColor(stack);
            if (a.isPresent()) {
                color = a.get();
            }
        } else if (stack.isOf(Items.SPONGE)) {
            color = CanvasColor.CLEAR;
        } else if (stack.isOf(Items.COAL) && raw.getColor() != MapColor.CLEAR) {
            if (raw.getBrightness() != MapColor.Brightness.LOWEST) {
                color = CanvasColor.from(raw.getColor(), switch (raw.getBrightness()) {
                    case HIGH -> MapColor.Brightness.NORMAL;
                    case NORMAL -> MapColor.Brightness.LOW;
                    default -> MapColor.Brightness.LOWEST;
                });
            }
        } else if (stack.isOf(Items.BONE_MEAL) && raw.getColor() != MapColor.CLEAR) {
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
                if (this.data[xi + yi * 16] == 1) {
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
        this.players.remove(player);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {

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
        if (!stack.contains(CanvasItem.DATA_TYPE)) {
            return;
        }
        var comp = stack.getOrDefault(CanvasItem.DATA_TYPE, CanvasItem.Data.DEFAULT);

        this.background = comp.background();
        this.glowing = comp.glowing();
        this.waxed = comp.waxed();
        this.cut = comp.cut();
        if (comp.image().isPresent()) {
            this.fromByteArray(comp.image().get());
        } else {
            CanvasUtils.clear(this.canvas, this.background.orElse(CanvasColor.OFF_WHITE_NORMAL));
        }

        this.name = stack.get(DataComponentTypes.CUSTOM_NAME);
        this.rebuildDisplay();
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.facing = Direction.fromHorizontal(nbt.getByte("facing"));
        this.glowing = nbt.getBoolean("glowing");
        this.waxed = nbt.getBoolean("waxed");
        this.cut = nbt.getBoolean("cut");
        if (nbt.contains("background")) {
            this.background = Optional.ofNullable(CanvasColor.getFromRaw(nbt.getByte("background")));
        } else {
            this.background = Optional.empty();
        }

        fromByteArray(nbt.getByteArray("data"));

        if (nbt.contains("name")) {
            this.name = Text.Serialization.fromLenientJson(nbt.getString("name"), this.getRegistryManager());
        }


        this.setFacing(this.facing);
        this.rebuildDisplay();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putByte("facing", (byte)this.facing.getHorizontal());
        nbt.putByteArray("data", Arrays.copyOf(this.data, this.data.length));
        nbt.putBoolean("glowing", this.glowing);
        nbt.putBoolean("waxed", this.waxed);
        nbt.putBoolean("cut", this.cut);
        this.background.ifPresent(canvasColor -> nbt.putByte("background", canvasColor.getRenderColor()));
        if (this.name != null) {
            nbt.putString("name", Text.Serialization.toJsonString(this.name, this.getRegistryManager()));
        }
    }

    @Override
    public void onPlace() {
        this.playSound(SoundEvent.of(id("entity.canvas.place")), 1.0F, 1.0F);
    }
    @Override
    public void onBreak(@Nullable Entity entity) {
        this.playSound(SoundEvent.of(id("entity.canvas.break")), 1.0F, 1.0F);

        var stack = this.toStack();
        if (entity instanceof PlayerEntity player && player.isCreative()
                && (stack.getOrDefault(CanvasItem.DATA_TYPE, CanvasItem.Data.DEFAULT).image().isEmpty())) {
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
        byte[] data = null;
        for (byte b : this.data) {
            if (b != 0) {
                data = Arrays.copyOf(this.data, this.data.length);
                break;
            }
        }

        stack.set(CanvasItem.DATA_TYPE, new CanvasItem.Data(Optional.ofNullable(data), this.background, this.glowing, this.waxed, this.cut));

        if (this.name != null) {
            stack.set(DataComponentTypes.CUSTOM_NAME, this.name);
        }

        return stack;
    }


    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
        return EntityType.MARKER;
    }
}
