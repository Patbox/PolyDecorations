package eu.pb4.polydecorations.entity;

import com.mojang.serialization.Codec;
import eu.pb4.common.protection.api.CommonProtection;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.core.PlayerCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.api.utils.VirtualDisplay;
import eu.pb4.polydecorations.canvas.CanvasData;
import eu.pb4.polydecorations.canvas.CanvasPixels;
import eu.pb4.polydecorations.item.DecorationsDataComponents;
import eu.pb4.polydecorations.item.DecorationsItemTags;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polydecorations.mixin.TagValueInputAccessor;
import eu.pb4.polydecorations.util.DecorationsSoundEvents;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CanvasEntity extends HangingEntity implements PolymerEntity {
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType"})
    @Nullable
    private static Optional<DataComponentType<Integer>> POLYFACTORY_COLOR = null;
    private static final Codec<Direction> LEGACY_DIRECTION_CODEC = Codec.withAlternative(Direction.CODEC, Codec.INT, Direction::from2DDataValue);
    private final PlayerCanvas canvas;
    private final Set<ServerPlayer> players = new HashSet<>();
    private final DrawableCanvas selfCanvas = new WrappingCanvas(this);
    private CanvasPixels data = new CanvasPixels();
    private VirtualDisplay display;
    private boolean glowing;
    private boolean waxed;
    private boolean cut;
    private Optional<CanvasColor> background = Optional.empty();
    private Rotation rotation = Rotation.NONE;
    @Nullable
    private Component name;

    public CanvasEntity(EntityType<? extends HangingEntity> entityType, Level world) {
        super(entityType, world);
        this.canvas = DrawableCanvas.create();

        CanvasUtils.clear(this.canvas, CanvasColor.OFF_WHITE_NORMAL);
    }

    public static Optional<CanvasColor> getColor(ItemStack stack) {
        //noinspection OptionalAssignedToNull
        if (POLYFACTORY_COLOR == null) {
            //noinspection unchecked
            POLYFACTORY_COLOR = Optional.ofNullable((DataComponentType<Integer>) BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(Identifier.parse("polyfactory:color")));
        }
        if (stack.getItem() instanceof DyeItem dyeItem) {
            return Optional.ofNullable(CanvasColor.from(dyeItem.getDyeColor().getMapColor(), MapColor.Brightness.NORMAL));
        } else if (POLYFACTORY_COLOR.isPresent() && stack.has(POLYFACTORY_COLOR.get())) {
            //noinspection DataFlowIssue
            return Optional.ofNullable(CanvasUtils.findClosestColor(stack.get(POLYFACTORY_COLOR.get())));
        }
        return Optional.empty();
    }

    public static CanvasEntity create(Level world, Direction side, BlockPos pos, float yaw) {
        var entity = new CanvasEntity(DecorationsEntities.CANVAS, world);
        entity.pos = pos;
        entity.setDirection(side);
        if (side == Direction.UP) {
            entity.rotation = switch (Direction.fromYRot(yaw)) {
                case NORTH -> Rotation.NONE;
                case SOUTH -> Rotation.CLOCKWISE_180;
                case EAST -> Rotation.CLOCKWISE_90;
                case WEST -> Rotation.COUNTERCLOCKWISE_90;
                default -> entity.rotation;
            };
        } else if (side == Direction.DOWN) {
            entity.rotation = switch (Direction.fromYRot(yaw)) {
                case NORTH -> Rotation.NONE;
                case SOUTH -> Rotation.CLOCKWISE_180;
                case EAST -> Rotation.COUNTERCLOCKWISE_90;
                case WEST -> Rotation.CLOCKWISE_90;
                default -> entity.rotation;
            };
        }
        return entity;
    }

    @Override
    protected void setDirection(Direction facing) {
        Validate.notNull(facing);
        super.setDirectionRaw(facing);
        if (facing.getAxis().isHorizontal()) {
            this.setXRot(0.0F);
            this.setYRot((float) (facing.get2DDataValue() * 90));
        } else {
            this.setXRot((float) (-90 * facing.getAxisDirection().getStep()));
            this.setYRot(0.0F);
        }

        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos pos, Direction side) {
        Vec3 vec3d = Vec3.atCenterOf(pos).relative(side, -0.46875);
        Direction.Axis axis = side.getAxis();
        double d = axis == Direction.Axis.X ? 0.0625 : 1;
        double e = axis == Direction.Axis.Y ? 0.0625 : 1;
        double g = axis == Direction.Axis.Z ? 0.0625 : 1;
        return AABB.ofSize(vec3d, d, e, g);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        if (this.display == null) {
            this.setupDisplay();
        }
        this.players.add(player);
        this.canvas.addPlayer(player);
        this.display.addPlayer(player);
    }

    private void setupDisplay() {
        this.display = VirtualDisplay.builder(this.canvas, this.pos, this.getDirection())
                .glowing(this.glowing)
                .invisible(this.cut)
                .rotation(this.rotation)
                .interactionCallback(this::onUsed)
                .build();
    }

    private void rebuildDisplay() {
        if (this.display != null) {
            this.display.destroy();
            setupDisplay();
            this.players.forEach(this.display::addPlayer);
        }
    }

    private void fromCanvasPixels(CanvasPixels canvasPixels) {
        byte[] data;
        if (canvasPixels.data().length == 0) {
            data = new byte[16 * 16];
        } else {
            data = Arrays.copyOf(canvasPixels.data(), canvasPixels.data().length);
        }

        this.data = new CanvasPixels(data);
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

    private void onUsed(ServerPlayer serverPlayer, VirtualDisplay.ClickType clickType, int x, int y) {
        if (clickType.isLeft()) {
            if (CommonProtection.canDamageEntity(this.level(), this, serverPlayer.getGameProfile(), serverPlayer)) {
                serverPlayer.attack(this);
            }
            return;
        }

        if (this.waxed || !CommonProtection.canInteractEntity(this.level(), this, serverPlayer.getGameProfile(), serverPlayer)) {
            return;
        }

        x = x / 8;
        y = y / 8;

        int radius = 0;

        CanvasColor color = null;
        var stack = serverPlayer.getMainHandItem();

        if (stack.is(Items.BRUSH)) {
            radius = 1;
            stack = serverPlayer.getOffhandItem();
        }
        DrawableCanvas canvas;

        if (x - radius >= 0 && x + radius < 16 && y - radius >= 0 && y + radius < 16) {
            canvas = this.selfCanvas;
        } else {
            x += 16;
            y += 16;

            var arr = new DrawableCanvas[9];
            var direction = this.getDirection();
            int i = 0;
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                for (int xOffset = -1; xOffset <= 1; xOffset++) {
                    int xa, ya, za;
                    if (direction.getAxis().isHorizontal()) {
                        xa = xOffset * direction.getStepZ();
                        ya = -yOffset;
                        za = -xOffset * direction.getStepX();
                    } else {
                        xa = xOffset;
                        ya = 0;
                        za = yOffset * direction.getStepY();

                        if (this.rotation == Rotation.CLOCKWISE_180) {
                            xa = -xa;
                            za = -za;
                        } else if (this.rotation == Rotation.CLOCKWISE_90) {
                            var tmp = xa;
                            xa = -za * direction.getStepY();
                            za = tmp * direction.getStepY();
                        } else if (this.rotation == Rotation.COUNTERCLOCKWISE_90) {
                            var tmp = xa;
                            xa = za * direction.getStepY();
                            za = -tmp * direction.getStepY();
                        }
                    }

                    var ents = this.level().getEntities(EntityTypeTest.forExactClass(CanvasEntity.class),
                            this.getBoundingBox().deflate(0.1f).move(xa, ya, za), c -> c.getDirection() == direction && c.rotation == this.rotation);

                    if (ents.isEmpty()) {
                        arr[i++] = VoidCanvas.INSTANCE;
                    } else {
                        arr[i++] = ents.getFirst().selfCanvas;
                    }
                }
            }
            canvas = new MergedCanvas(arr, 3, 3);
        }
        /*{
            var g = CanvasUtils.getGraphics(canvas);
            g.clearRect(0, 0, 16 * 3, 16 * 3);
            g.setColor(Color.CYAN);
            g.drawLine(0, 0, 16 * 3, 16 * 3);
            g.setColor(Color.GREEN);
            g.drawLine(0, 16 * 3, 16 * 3, 0);
            g.setColor(Color.RED);
            g.drawLine(8, 8, 8, 8 * 5);
            g.drawLine(8 * 5, 8, 8 * 5, 8 * 5);
            g.setColor(Color.YELLOW);
            g.drawLine(8, 8, 8 * 5, 8);
            g.drawLine(8, 8 * 5, 8 * 5, 8 * 5);
            g.dispose();
        }*/

        var raw = canvas.get(x, y);

        if (this.cut && raw == CanvasColor.CLEAR_FORCE) {
            return;
        }

        if (stack.is(ConventionalItemTags.DYES)) {
            var a = getColor(stack);
            if (a.isPresent()) {
                color = a.get();
            }
        } else if (stack.is(DecorationsItemTags.CANVAS_CLEAR_PIXELS)) {
            color = CanvasColor.CLEAR;
        } else if (stack.is(DecorationsItemTags.CANVAS_DARKEN_PIXELS) && raw.getColor() != MapColor.NONE) {
            if (raw.getBrightness() != MapColor.Brightness.LOWEST) {
                color = CanvasColor.from(raw.getColor(), switch (raw.getBrightness()) {
                    case HIGH -> MapColor.Brightness.NORMAL;
                    case NORMAL -> MapColor.Brightness.LOW;
                    default -> MapColor.Brightness.LOWEST;
                });
            }
        } else if (stack.is(DecorationsItemTags.CANVAS_LIGHTEN_PIXELS) && raw.getColor() != MapColor.NONE) {
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

        for (var xi = x - radius; xi <= x + radius; xi++) {
            for (var yi = y - radius; yi <= y + radius; yi++) {
                if (xi < 0 || xi >= canvas.getWidth() || yi < 0 || yi >= canvas.getHeight()) {
                    continue;
                }
                if (canvas.getRaw(xi, yi) == 1) {
                    continue;
                }

                canvas.set(xi, yi, color);
            }
        }

        serverPlayer.swing(InteractionHand.MAIN_HAND, true);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        this.canvas.removePlayer(player);
        if (this.display != null) {
            this.display.removePlayer(player);
        }
        this.players.remove(player);
    }

    @Override
    public void onClientRemoval() {
        if (this.display != null) {
            this.display.destroy();
            this.display = null;
        }

        super.onClientRemoval();
    }

    public void loadFromStack(ItemStack stack) {
        if (!stack.has(DecorationsDataComponents.CANVAS_DATA)) {
            return;
        }
        var comp = stack.getOrDefault(DecorationsDataComponents.CANVAS_DATA, CanvasData.DEFAULT);

        this.loadFromComponent(comp);
        this.name = stack.get(DataComponents.CUSTOM_NAME);
    }

    private void loadFromComponent(CanvasData comp) {
        this.background = comp.background();
        this.glowing = comp.glowing();
        this.waxed = comp.waxed();
        this.cut = comp.cut();
        if (comp.image().isPresent()) {
            this.fromCanvasPixels(comp.image().get());
        } else {
            CanvasUtils.clear(this.canvas, this.background.orElse(CanvasColor.OFF_WHITE_NORMAL));
        }
        this.rebuildDisplay();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.canvas.isDirty()) {
            this.canvas.sendUpdates();
        }
    }

    private CanvasData getCanvasData() {
        return new CanvasData(data.isColored() ? Optional.of(data) : Optional.empty(), this.background, this.glowing, this.waxed, this.cut);
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        if (view instanceof TagValueInputAccessor nbtReadViewAccessor) {
            var nbt = nbtReadViewAccessor.getInput();
            if (nbt.get("data") instanceof ByteArrayTag) {
                nbt.put("image", nbt.get("data"));
                nbt.remove("data");
            }
        }

        super.readAdditionalSaveData(view);
        this.setDirection(view.read("facing", LEGACY_DIRECTION_CODEC).orElse(this.getDirection()));
        this.glowing = view.getBooleanOr("glowing", false);
        this.waxed = view.getBooleanOr("waxed", false);
        this.cut = view.getBooleanOr("cut", false);

        var backgroundByte = view.getByteOr("background", (byte) 0);
        this.background = backgroundByte == 0 ? Optional.empty() : Optional.ofNullable(CanvasColor.getFromRaw(backgroundByte));

        fromCanvasPixels(view.read("image", CanvasPixels.CODEC).orElseGet(CanvasPixels::new));

        this.name = view.read("name", ComponentSerialization.CODEC).orElse(null);

        this.rotation = view.read("block_rotation", Rotation.CODEC).orElse(Rotation.NONE);

        if (name != null && this.name.getSiblings().isEmpty() && this.name.getContents() instanceof PlainTextContents.LiteralContents literal
                && literal.text().length() >= 2 && literal.text().charAt(0) == '"' && literal.text().charAt(literal.text().length() - 1) == '"') {
            this.name = Component.literal(literal.text().substring(1, literal.text().length() - 1));
        }

        this.rebuildDisplay();
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        view.store("facing", LEGACY_DIRECTION_CODEC, this.getDirection());
        view.store("image", CanvasPixels.CODEC, this.data);
        view.putBoolean("glowing", this.glowing);
        view.putBoolean("waxed", this.waxed);
        view.putBoolean("cut", this.cut);
        view.store("block_rotation", Rotation.CODEC, this.rotation);
        this.background.ifPresent(canvasColor -> view.putByte("background", canvasColor.getRenderColor()));
        if (this.name != null) {
            view.store("name", ComponentSerialization.CODEC, this.name);
        }
    }

    @Override
    public void playPlacementSound() {
        this.playSound(DecorationsSoundEvents.CANVAS_PLACE, 1.0F, 1.0F);
    }

    @Override
    public void dropItem(ServerLevel serverWorld, @Nullable Entity entity) {
        this.playSound(DecorationsSoundEvents.CANVAS_BREAK, 1.0F, 1.0F);

        var stack = this.toStack();
        if (entity instanceof Player player && player.isCreative()
                && (stack.getOrDefault(DecorationsDataComponents.CANVAS_DATA, CanvasData.DEFAULT).image().isEmpty())) {
            return;
        }

        this.spawnAtLocation(serverWorld, stack);
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        return toStack();
    }

    private ItemStack toStack() {
        var stack = new ItemStack(DecorationsItems.CANVAS);
        stack.set(DecorationsDataComponents.CANVAS_DATA, getCanvasData());

        if (this.name != null) {
            stack.set(DataComponents.CUSTOM_NAME, this.name);
        }

        return stack;
    }


    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.MARKER;
    }

    private record VoidCanvas() implements DrawableCanvas {
        public static final VoidCanvas INSTANCE = new VoidCanvas();

        @Override
        public byte getRaw(int x, int y) {
            return 1;
        }

        @Override
        public void setRaw(int x, int y, byte color) {

        }

        @Override
        public int getHeight() {
            return 16;
        }

        @Override
        public int getWidth() {
            return 16;
        }
    }

    private record WrappingCanvas(CanvasEntity entity) implements DrawableCanvas {
        @Override
        public byte getRaw(int x, int y) {
            return entity.data.getRaw(x, y);
        }

        @Override
        public void setRaw(int x, int y, byte color) {
            entity.data.setRaw(x, y, color);
            CanvasUtils.fill(entity.canvas, x * 8, y * 8, (x + 1) * 8, (y + 1) * 8, entity.getColor(color));
        }

        @Override
        public int getHeight() {
            return 16;
        }

        @Override
        public int getWidth() {
            return 16;
        }
    }

    private record MergedCanvas(DrawableCanvas[] canvas, int width, int height) implements DrawableCanvas {
        @Override
        public byte getRaw(int x, int y) {
            return getCanvas(x, y).getRaw(x % 16, y % 16);
        }

        private DrawableCanvas getCanvas(int x, int y) {
            var i = x / 16 + y / 16 * width;
            return this.canvas[i];
        }

        @Override
        public void setRaw(int x, int y, byte color) {
            getCanvas(x, y).setRaw(x % 16, y % 16, color);
        }

        @Override
        public int getHeight() {
            return 16 * height;
        }

        @Override
        public int getWidth() {
            return 16 * width;
        }
    }
}
