package eu.pb4.polydecorations.block.extension;

import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.factorytools.api.block.BlockEntityExtraListener;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SignChangingItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SignPostBlockEntity extends BlockEntity implements BlockEntityExtraListener {
    private Sign upperText = Sign.of();
    private Sign lowerText = Sign.of();

    private AttachedSignPostBlock.Model model;

    public SignPostBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DecorationsBlockEntities.SIGN_POST, blockPos, blockState);
    }

    public Sign upperText() {
        return upperText;
    }

    public Sign lowerText() {
        return lowerText;
    }

    public void setLowerText(Sign lowerText) {
        this.lowerText = lowerText;
        this.markDirty();
        if (this.model != null) {
            this.model.updateLower(lowerText);
        }
    }

    public void setUpperText(Sign upperText) {
        this.upperText = upperText;
        this.markDirty();
        if (this.model != null) {
            this.model.updateUpper(upperText);
        }
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.put("upper", Sign.CODEC, this.upperText);
        view.put("lower", Sign.CODEC, this.lowerText);
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);
        this.upperText = view.read("upper",  Sign.CODEC).orElseGet(Sign::of);
        this.lowerText = view.read("lower",  Sign.CODEC).orElseGet(Sign::of);

        if (this.upperText.text.getMessage(1, false).getContent() instanceof PlainTextContent.Literal(String string) && string.equals("\"\"")) {
            var tmp = this.upperText.text;
            this.upperText = this.upperText.withText(new SignText()
                    .withColor(tmp.getColor())
                    .withGlowing(tmp.isGlowing())
                    .withMessage(0, TextCodecs.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(tmp.getMessage(0, false).getString()))
                            .result().map(Pair::getFirst).orElse(tmp.getMessage(0, false)))
            );
        }

        if (this.lowerText.text.getMessage(1, false).getContent() instanceof PlainTextContent.Literal(String string) && string.equals("\"\"")) {
            var tmp = this.lowerText.text;
            this.lowerText = this.lowerText.withText(new SignText()
                    .withColor(tmp.getColor())
                    .withGlowing(tmp.isGlowing())
                    .withMessage(0, TextCodecs.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(tmp.getMessage(0, false).getString()))
                            .result().map(Pair::getFirst).orElse(tmp.getMessage(0, false)))            );
        }

        if (this.model != null) {
            this.model.update(this.upperText, this.lowerText);
        }
    }

    @Override
    public void onListenerUpdate(WorldChunk chunk) {
        try {
            this.model = (AttachedSignPostBlock.Model) BlockAwareAttachment.get(chunk, this.getPos()).holder();
            this.model.update(this.upperText, this.lowerText);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public void setText(boolean upper, Sign sign) {
        if (upper) {
            this.setUpperText(sign);
        } else {
            this.setLowerText(sign);
        }
    }

    public Sign getText(boolean upper) {
        if (upper) {
            return this.upperText();
        } else {
            return this.lowerText();
        }
    }

    public void openText(boolean upper, ServerPlayerEntity player) {
        new Gui(player, () -> getText(upper), (t) -> setText(upper, t));
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        super.onBlockReplaced(pos, oldState);
        if (this.world != null) {
            ItemScatterer.spawn(world, pos, DefaultedList.copyOf(ItemStack.EMPTY, this.lowerText().item().getDefaultStack(), this.upperText().item().getDefaultStack()));
        }
    }

    public ActionResult onUse(PlayerEntity player, boolean upper, BlockHitResult hit) {
        if (this.getText(upper).item == Items.AIR || this.getText(upper).waxed() || MathHelper.angleBetween(player.getYaw() + 180, this.getText(upper).yaw) > 90) {
            return ActionResult.PASS;
        } else if (player.isSneaking()) {
            if (player.getMainHandStack().isEmpty()) {
                this.setText(upper, this.getText(upper).withFlip());
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        }

        if (player.getMainHandStack().getItem() instanceof SignChangingItem item) {
            if (item.canUseOnSignText(getText(upper).text, player)) {
                var fake = Fake.INSTANCE;
                fake.setText(getText(upper).text, false);
                fake.setWaxed(getText(upper).waxed);
                try {
                    if (item.useOnSign(world, fake, false, player)) {
                        if (!player.isCreative()) {
                            player.getMainHandStack().decrement(1);
                        }
                        this.setText(upper, getText(upper).withText(fake.getText(false)).withWaxed(fake.isWaxed()));
                        return ActionResult.SUCCESS;
                    };
                } catch (Throwable e) {
                }
            }
        } else if (player.getMainHandStack().isIn(ItemTags.AXES)) {
            var sign = this.getText(upper);
            this.setText(upper, Sign.of());
            if (this.lowerText.item == Items.AIR && this.upperText.item == Items.AIR) {
                player.getWorld().setBlockState(pos, ((AttachedSignPostBlock) this.getCachedState().getBlock()).getBacking()
                        .getDefaultState().withIfExists(Properties.WATERLOGGED, getCachedState().get(Properties.WATERLOGGED)));
            }
            ItemScatterer.spawn(player.getWorld(), pos, DefaultedList.copyOf(ItemStack.EMPTY, sign.item.getDefaultStack()));
            return ActionResult.SUCCESS;
        } else if (player instanceof ServerPlayerEntity serverPlayer) {
            openText(upper, serverPlayer);

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public record Sign(SignText text, Item item, float yaw, boolean waxed, boolean flip) {
        public static final Codec<Sign> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                SignText.CODEC.optionalFieldOf("text", new SignText()).forGetter(Sign::text),
                Registries.ITEM.getCodec().optionalFieldOf("item", Items.AIR).forGetter(Sign::item),
                Codec.FLOAT.optionalFieldOf("yaw", 0f).forGetter(Sign::yaw),
                Codec.BOOL.optionalFieldOf("waxed", false).forGetter(Sign::waxed),
                Codec.BOOL.optionalFieldOf("flip", false).forGetter(Sign::flip)
        ).apply(instance, Sign::new));
        public static Sign of() {
            return new Sign(new SignText(), Items.AIR, 0, false, false);
        }

        public static Sign of(Item item, float yaw, boolean flip) {
            return new Sign(new SignText(), item, yaw, false, flip);
        }

        public Sign withText(SignText text) {
            return new Sign(text, item, yaw, waxed, flip);
        }

        public Sign withItem(Item item) {
            return new Sign(text, item, yaw, waxed, flip);
        }

        public Sign withYaw(float yaw) {
            return new Sign(text, item, yaw, waxed, flip);
        }

        public Sign withWaxed(boolean waxed) {
            return new Sign(text, item, yaw, waxed, flip);
        }

        public Text getText() {
            return Text.empty().append(this.text.getMessage(0, false)).withColor(text.getColor().getSignColor());
        }

        public Text getUncoloredText() {
            return this.text.getMessage(0, false);
        }

        public Sign withFlip(boolean flip) {
            return new Sign(text, item, yaw, waxed, flip);
        }

        public Sign withFlip() {
            return new Sign(text, item, yaw, waxed, !flip);
        }

        public boolean isEmpty() {
            return this.item == Items.AIR;
        }

        public Sign withYawAdded(float yawAdded) {
            return new Sign(text, item, yaw + yawAdded, waxed, flip);
        }

        public DyeColor dye() {
            return this.text.getColor();
        }

        public boolean glowing() {
            return this.text.isGlowing();
        }
    }

    public static final class Fake extends SignBlockEntity {
        public static final Fake INSTANCE = new Fake();
        private boolean waxed;

        public Fake() {
            super(BlockPos.ORIGIN, Blocks.OAK_SIGN.getDefaultState());
        }

        @Override
        public boolean setWaxed(boolean waxed) {
            if (this.waxed != waxed) {
                this.waxed = waxed;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean isWaxed() {
            return this.waxed;
        }
    }

    private class Gui extends SignGui {
        private final Consumer<Sign> setter;
        private final Supplier<Sign> getter;

        public Gui(ServerPlayerEntity player, Supplier<Sign> getter, Consumer<Sign> setter) {
            super(player);
            this.getter = getter;
            this.setter = setter;
            var sign = getter.get();
            this.setColor(sign.text().getColor());
            var txt = Text.empty().append(Text.literal("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")).formatted(Formatting.BLACK, Formatting.STRIKETHROUGH);
            this.setLine(0, sign.text.getMessage(0, false));
            this.setLine(1, txt);
            this.setLine(2, txt);
            this.setLine(3, txt);
            this.setSignType(Blocks.BIRCH_SIGN);
            this.open();
        }

        @Override
        public void onClose() {
            setter.accept(getter.get().withText(getter.get().text.withMessage(0, this.getLine(0))));
            super.onClose();
        }

        @Override
        public void onTick() {
            if (player.getPos().squaredDistanceTo(Vec3d.ofCenter(SignPostBlockEntity.this.pos)) > (18 * 18)) {
                this.close();
            }
            super.onTick();
        }
    }
}
