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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
        this.setChanged();
        if (this.model != null) {
            this.model.updateLower(lowerText);
        }
    }

    public void setUpperText(Sign upperText) {
        this.upperText = upperText;
        this.setChanged();
        if (this.model != null) {
            this.model.updateUpper(upperText);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.store("upper", Sign.CODEC, this.upperText);
        view.store("lower", Sign.CODEC, this.lowerText);
    }

    @Override
    public void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.upperText = view.read("upper",  Sign.CODEC).orElseGet(Sign::of);
        this.lowerText = view.read("lower",  Sign.CODEC).orElseGet(Sign::of);

        if (this.upperText.text.getMessage(1, false).getContents() instanceof PlainTextContents.LiteralContents(String string) && string.equals("\"\"")) {
            var tmp = this.upperText.text;
            this.upperText = this.upperText.withText(new SignText()
                    .setColor(tmp.getColor())
                    .setHasGlowingText(tmp.hasGlowingText())
                    .setMessage(0, ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(tmp.getMessage(0, false).getString()))
                            .result().map(Pair::getFirst).orElse(tmp.getMessage(0, false)))
            );
        }

        if (this.lowerText.text.getMessage(1, false).getContents() instanceof PlainTextContents.LiteralContents(String string) && string.equals("\"\"")) {
            var tmp = this.lowerText.text;
            this.lowerText = this.lowerText.withText(new SignText()
                    .setColor(tmp.getColor())
                    .setHasGlowingText(tmp.hasGlowingText())
                    .setMessage(0, ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(tmp.getMessage(0, false).getString()))
                            .result().map(Pair::getFirst).orElse(tmp.getMessage(0, false)))            );
        }

        if (this.model != null) {
            this.model.update(this.upperText, this.lowerText);
        }
    }

    @Override
    public void onListenerUpdate(LevelChunk chunk) {
        try {
            this.model = (AttachedSignPostBlock.Model) BlockAwareAttachment.get(chunk, this.getBlockPos()).holder();
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

    public void openText(boolean upper, ServerPlayer player) {
        new Gui(player, () -> getText(upper), (t) -> setText(upper, t));
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        if (this.level != null) {
            Containers.dropContents(level, pos, NonNullList.of(ItemStack.EMPTY, this.lowerText().item().getDefaultInstance(), this.upperText().item().getDefaultInstance()));
        }
    }

    public InteractionResult onUse(Player player, boolean upper, BlockHitResult hit) {
        if (this.getText(upper).item == Items.AIR || this.getText(upper).waxed() || Mth.degreesDifferenceAbs(player.getYRot() + 180, this.getText(upper).yaw) > 90) {
            return InteractionResult.PASS;
        } else if (player.isShiftKeyDown()) {
            if (player.getMainHandItem().isEmpty()) {
                this.setText(upper, this.getText(upper).withFlip());
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        }

        if (player.getMainHandItem().getItem() instanceof SignApplicator item) {
            if (item.canApplyToSign(getText(upper).text, player)) {
                var fake = Fake.INSTANCE;
                fake.setText(getText(upper).text, false);
                fake.setWaxed(getText(upper).waxed);
                try {
                    if (item.tryApplyToSign(level, fake, false, player)) {
                        if (!player.isCreative()) {
                            player.getMainHandItem().shrink(1);
                        }
                        this.setText(upper, getText(upper).withText(fake.getText(false)).withWaxed(fake.isWaxed()));
                        return InteractionResult.SUCCESS;
                    };
                } catch (Throwable e) {
                }
            }
        } else if (player.getMainHandItem().is(ItemTags.AXES)) {
            var sign = this.getText(upper);
            this.setText(upper, Sign.of());
            if (this.lowerText.item == Items.AIR && this.upperText.item == Items.AIR) {
                player.level().setBlockAndUpdate(worldPosition, ((AttachedSignPostBlock) this.getBlockState().getBlock()).getBacking()
                        .defaultBlockState().trySetValue(BlockStateProperties.WATERLOGGED, getBlockState().getValue(BlockStateProperties.WATERLOGGED)));
            }
            Containers.dropContents(player.level(), worldPosition, NonNullList.of(ItemStack.EMPTY, sign.item.getDefaultInstance()));
            return InteractionResult.SUCCESS;
        } else if (player instanceof ServerPlayer serverPlayer) {
            openText(upper, serverPlayer);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public record Sign(SignText text, Item item, float yaw, boolean waxed, boolean flip) {
        public static final Codec<Sign> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                SignText.DIRECT_CODEC.optionalFieldOf("text", new SignText()).forGetter(Sign::text),
                BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("item", Items.AIR).forGetter(Sign::item),
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

        public Component getText() {
            return Component.empty().append(this.text.getMessage(0, false)).withColor(text.getColor().getTextColor());
        }

        public Component getUncoloredText() {
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
            return this.text.hasGlowingText();
        }
    }

    public static final class Fake extends SignBlockEntity {
        public static final Fake INSTANCE = new Fake();
        private boolean waxed;

        public Fake() {
            super(BlockPos.ZERO, Blocks.OAK_SIGN.defaultBlockState());
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

        public Gui(ServerPlayer player, Supplier<Sign> getter, Consumer<Sign> setter) {
            super(player);
            this.getter = getter;
            this.setter = setter;
            var sign = getter.get();
            this.setColor(sign.text().getColor());
            var txt = Component.empty().append(Component.literal("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")).withStyle(ChatFormatting.BLACK, ChatFormatting.STRIKETHROUGH);
            this.setLine(0, sign.text.getMessage(0, false));
            this.setLine(1, txt);
            this.setLine(2, txt);
            this.setLine(3, txt);
            this.setSignType(Blocks.BIRCH_SIGN);
            this.open();
        }

        @Override
        public void onClose() {
            setter.accept(getter.get().withText(getter.get().text.setMessage(0, this.getLine(0))));
            super.onClose();
        }

        @Override
        public void onTick() {
            if (player.position().distanceToSqr(Vec3.atCenterOf(SignPostBlockEntity.this.worldPosition)) > (18 * 18)) {
                this.close();
            }
            super.onTick();
        }
    }
}
