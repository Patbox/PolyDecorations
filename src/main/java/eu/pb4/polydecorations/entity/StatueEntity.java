package eu.pb4.polydecorations.entity;

import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.item.DecorationsItems;
import eu.pb4.polydecorations.item.StatueItem;
import eu.pb4.polydecorations.util.DecorationsUtil;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import net.minecraft.core.Rotations;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;
import java.util.function.Consumer;

import static eu.pb4.polydecorations.ModInit.id;

public class StatueEntity extends ArmorStand implements PolymerEntity {
    private static final Rotations DEFAULT_HEAD_ROTATION = new Rotations(0.0F, 0.0F, 0.0F);
    private static final Rotations DEFAULT_BODY_ROTATION = new Rotations(0.0F, 0.0F, 0.0F);
    private static final Rotations DEFAULT_LEFT_ARM_ROTATION = new Rotations(-10.0F, 0.0F, -10.0F);
    private static final Rotations DEFAULT_RIGHT_ARM_ROTATION = new Rotations(-15.0F, 0.0F, 10.0F);
    private static final Rotations DEFAULT_LEFT_LEG_ROTATION = new Rotations(-1.0F, 0.0F, -1.0F);
    private static final Rotations DEFAULT_RIGHT_LEG_ROTATION = new Rotations(1.0F, 0.0F, 1.0F);

    private final Model model;
    private ItemStack stack;
    private StatueItem item;

    public StatueEntity(EntityType<? extends ArmorStand> entityType, Level world) {
        super(entityType, world);
        this.model = new Model(this);
        this.setStack(DecorationsItems.OTHER_STATUE.get(Type.STONE).getDefaultInstance());
        EntityAttachment.of(this.model, this);
        this.setShowArms(true);
        this.setNoGravity(true);
    }

    @Override
    public void onEntityTrackerTick(Set<ServerPlayerConnection> listeners) {
        this.model.tick();
    }

    @Override
    public ItemStack getPickResult() {
        return this.stack.copy();
    }

    @Override
    protected void brokenByPlayer(ServerLevel world, DamageSource damageSource) {
        ItemStack itemStack = this.stack.copy();
        if (this.hasCustomName()) {
            itemStack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        }

        Block.popResource(world, this.blockPosition(), itemStack);
        this.brokenByAnything(world, damageSource);
    }

    @Override
    public void tick() {
        this.model.setScale(this.getScale());
        super.tick();
    }

    @Override
    protected void showBreakingParticles() {
        if (this.level() instanceof ServerLevel) {
            ((ServerLevel)this.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.item.getType().block().defaultBlockState()), this.getX(), this.getY(0.6666666666666666), this.getZ(), 10, (double)(this.getBbWidth() / 4.0F), (double)(this.getBbHeight() / 4.0F), (double)(this.getBbWidth() / 4.0F), 0.05);
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        super.addAdditionalSaveData(view);
        if (!this.stack.isEmpty()) {
            view.store("stack", ItemStack.OPTIONAL_CODEC, this.stack);
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput view) {
        super.readAdditionalSaveData(view);
        setStack(view.read("stack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
    }

    @Override
    public void setYRot(float yaw) {
        super.setYRot(yaw);
        if (this.model != null) {
            this.model.setYaw(yaw);
        }
    }

    @Override
    public void setGlowingTag(boolean glowing) {
        super.setGlowingTag(glowing);
        this.model.setGlowing(glowing);
    }

    @Override
    protected void setSharedFlag(int index, boolean value) {
        super.setSharedFlag(index, value);

        if (index == FLAG_GLOWING) {
            this.model.setGlowing(value);
        }
    }

    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        if (!this.isRemoved()) {
            if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                this.kill(world);
                return false;
            } else if (!this.isInvulnerableTo(world, source) && !this.isInvisible() && !this.isMarker()) {
                if (source.is(DamageTypeTags.IS_EXPLOSION)) {
                    this.brokenByAnything(world, source);
                    this.kill(world);
                    return false;
                } else if (source.is(DamageTypeTags.IGNITES_ARMOR_STANDS) && !this.item.getType().fireproof()) {
                    if (this.isOnFire()) {
                        this.causeDamage(world, source, 0.15F);
                    } else {
                        this.igniteForSeconds(5);
                    }

                    return false;
                } else if (source.is(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5F && !this.item.getType().fireproof()) {
                    this.causeDamage(world, source, 4.0F);
                    return false;
                } else {
                    boolean bl = source.is(DamageTypeTags.CAN_BREAK_ARMOR_STAND);
                    boolean bl2 = source.is(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS);
                    if (!bl && !bl2) {
                        return false;
                    } else {
                        Entity var6 = source.getEntity();
                        if (var6 instanceof Player) {
                            Player playerEntity = (Player)var6;
                            if (!playerEntity.getAbilities().mayBuild) {
                                return false;
                            }
                        }

                        if (source.isCreativePlayer()) {
                            this.playBrokenSound();
                            this.showBreakingParticles();
                            this.kill(world);
                            return true;
                        } else {
                            long l = this.level().getGameTime();
                            if (l - this.lastHit > 5L && !bl2) {
                                this.level().broadcastEntityEvent(this, (byte)32);
                                this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
                                this.lastHit = l;
                            } else {
                                this.brokenByPlayer(world, source);
                                this.showBreakingParticles();
                                this.kill(world);
                            }

                            return true;
                        }
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    protected Component getTypeName() {
        return this.stack.getHoverName();
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.ARMOR_STAND;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        var sendFlags = initial;
        for (var i = 0; i < data.size(); i++) {
            var x = data.get(i);
            if (x.id() == EntityTrackedData.FLAGS.id()) {
                data.set(i, SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, (byte) (((byte) x.value()) | (1 << EntityTrackedData.INVISIBLE_FLAG_INDEX))));
                sendFlags = false;
            }
        }

        if (initial && sendFlags) {
            data.add(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, (byte) (((byte) this.entityData.get(DATA_SHARED_FLAGS_ID)) | (1 << EntityTrackedData.INVISIBLE_FLAG_INDEX))));
        }
    }

    @Override
    protected void playBrokenSound() {
        this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), this.item.getType().soundGroup().getBreakSound(), this.getSoundSource(), 1.0F, 1.0F);
    }

    @Override
    public Fallsounds getFallSounds() {
        return new Fallsounds(this.item.getType().soundGroup().getStepSound(), this.item.getType().soundGroup().getFallSound());
    }

    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.item.getType().soundGroup().getHitSound();
    }

    @Nullable
    protected SoundEvent getDeathSound() {
        return this.item.getType().soundGroup().getBreakSound();
    }

    @Override
    public void setHeadPose(Rotations angle) {
        super.setHeadPose(angle);
        this.model.head.updateAngle(angle);
    }

    @Override
    public void setLeftArmPose(Rotations angle) {
        super.setLeftArmPose(angle);
        this.model.leftArm.updateAngle(angle);
    }

    @Override
    public void setRightArmPose(Rotations angle) {
        super.setRightArmPose(angle);
        this.model.rightArm.updateAngle(angle);
    }

    @Override
    public void setLeftLegPose(Rotations angle) {
        super.setLeftLegPose(angle);
        this.model.leftLeg.updateAngle(angle);
    }

    @Override
    public void setRightLegPose(Rotations angle) {
        super.setRightLegPose(angle);
        this.model.rightLeg.updateAngle(angle);
    }

    @Override
    public void setBodyPose(Rotations angle) {
        super.setBodyPose(angle);
        this.model.body.updateAngle(angle);
    }

    @Override
    protected void setSmall(boolean small) {
        super.setSmall(small);
        this.model.setSmall(small);
    }


    public void setStack(ItemStack itemStack) {
        this.stack = itemStack;
        if (this.stack.getItem() instanceof StatueItem statueItem) {
            this.item = statueItem;
            this.model.setType(statueItem.getType());
        }
    }

    public record Type(String type, ItemStack head, ItemStack body, ItemStack leftArm, ItemStack rightArm, ItemStack leftLeg, ItemStack rightLeg, Block block, boolean fireproof) {
        public static final List<Type> NON_WOOD = new ArrayList<>();
        public static final Type STONE = nonWood("stone", Blocks.STONE);
        public static final Type DEEPSLATE = nonWood("deepslate", Blocks.DEEPSLATE);
        public static final Type BLACKSTONE = nonWood("blackstone", Blocks.BLACKSTONE);
        public static final Type PRISMARINE = nonWood("prismarine", Blocks.PRISMARINE);
        public static final Type SANDSTONE = nonWood("sandstone", Blocks.SANDSTONE);
        public static final Type RED_SANDSTONE = nonWood("red_sandstone", Blocks.RED_SANDSTONE);
        public static final Type QUARTZ = nonWood("quartz", Blocks.QUARTZ_BLOCK);
        public static final Type ANDESITE = nonWood("andesite", Blocks.ANDESITE);
        public static final Type DIORITE = nonWood("diorite", Blocks.DIORITE);
        public static final Type GRANITE = nonWood("granite", Blocks.GRANITE);
        public static final Type TUFF = nonWood("tuff", Blocks.TUFF);
        public static final Type PACKED_MUD = nonWood("packed_mud", Blocks.PACKED_MUD);
        public static final Type STONE_BRICKS = nonWood("stone_bricks", Blocks.STONE_BRICKS);
        public static final Type TUFF_BRICKS = nonWood("tuff_bricks", Blocks.TUFF_BRICKS);
        public static final Type TERRACOTTA = nonWood("terracotta", Blocks.TERRACOTTA);
        public static final Map<DyeColor, Type> COLORED_TERRACOTTA = Util.make(new HashMap<>(), (x) -> {
            for (var color : DecorationsUtil.COLORS_CREATIVE) {
                x.put(color, nonWood(color.getSerializedName() + "_terracotta", BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(color.getSerializedName() + "_terracotta"))));
            }
        });
        public static final Map<DyeColor, Type> COLORED_WOOL = Util.make(new HashMap<>(), (x) -> {
            for (var color : DecorationsUtil.COLORS_CREATIVE) {
                x.put(color, burnableNonWood(color.getSerializedName() + "_wool", BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(color.getSerializedName() + "_wool"))));
            }
        });

        public static Type burnableNonWood(String name, Block block) {
            var x = of(name, block, false);
            NON_WOOD.add(x);
            return x;
        }

        public static Type nonWood(String name, Block block) {
            var x = of(name, block, true);
            NON_WOOD.add(x);
            return x;
        }

        public static Type of(String type, Block block, boolean fireproof) {
            return new Type(type, requestModel(type, "head"), requestModel(type, "body"), requestModel(type, "left_arm"),
                    requestModel(type, "right_arm"), requestModel(type, "left_leg"), requestModel(type, "right_leg"), block, fireproof);
        }

        private static ItemStack requestModel(String type, String head) {
            return ItemDisplayElementUtil.getSolidModel(id("block/statue/" + type + "/" + head));
        }

        public SoundType soundGroup() {
            return block.defaultBlockState().getSoundType();
        }
    }

    public static final class Model extends ElementHolder {
        public final Bone head = Bone.from(new Vector3f(0, (24 / 16f), 0), StatueEntity.DEFAULT_HEAD_POSE);
        public final Bone body = Bone.from(new Vector3f(0, (24 / 16f), 0), StatueEntity.DEFAULT_BODY_POSE);
        public final Bone leftArm = Bone.from(new Vector3f(5/16f, (22 / 16f), 0), StatueEntity.DEFAULT_LEFT_ARM_POSE);
        public final Bone rightArm = Bone.from(new Vector3f(-5/16f, (22 / 16f), 0), StatueEntity.DEFAULT_RIGHT_ARM_POSE);
        public final Bone leftLeg = Bone.from(new Vector3f(2/16f, (12 / 16f), 0), StatueEntity.DEFAULT_LEFT_LEG_POSE);
        public final Bone rightLeg = Bone.from(new Vector3f(-2/16f, (12 / 16f), 0), StatueEntity.DEFAULT_RIGHT_LEG_POSE);
        private final StatueEntity entity;
        private boolean small = false;
        private float baseScale = 1f;

        public Model(StatueEntity entity) {
            this.entity = entity;
            this.addElement(this.head.display);
            this.addElement(this.body.display);
            this.addElement(this.leftLeg.display);
            this.addElement(this.rightLeg.display);
            this.addElement(this.leftArm.display);
            this.addElement(this.rightArm.display);

            VirtualEntityUtils.addVirtualPassenger(entity, this.head.display.getEntityId());
            VirtualEntityUtils.addVirtualPassenger(entity, this.body.display.getEntityId());
            VirtualEntityUtils.addVirtualPassenger(entity, this.leftLeg.display.getEntityId());
            VirtualEntityUtils.addVirtualPassenger(entity, this.rightLeg.display.getEntityId());
            VirtualEntityUtils.addVirtualPassenger(entity, this.leftArm.display.getEntityId());
            VirtualEntityUtils.addVirtualPassenger(entity, this.rightArm.display.getEntityId());
        }

        public void setYaw(float yaw) {
            this.head.setYaw(yaw);
            this.body.setYaw(yaw);
            this.leftArm.setYaw(yaw);
            this.rightArm.setYaw(yaw);
            this.leftLeg.setYaw(yaw);
            this.rightLeg.setYaw(yaw);
        }

        public void setSmall(boolean small) {
            if (this.small != small) {
                this.small = small;
                this.updateScale();
            }
        }

        public void setScale(float scale) {
            if (this.baseScale != scale) {
                this.baseScale = scale;
                this.updateScale();
            }
        }

        private void updateScale() {
            var scale = (small ? 0.5f : 1f) * this.baseScale;
            this.head.setScale(scale, (small ? (12 / 16f) : 1f) * this.baseScale);
            this.body.setScale(scale, scale);
            this.leftArm.setScale(scale, scale);
            this.rightArm.setScale(scale, scale);
            this.leftLeg.setScale(scale, scale);
            this.rightLeg.setScale(scale, scale);
        }

        public void setType(Type type) {
            this.head.display.setItem(type.head);
            this.body.display.setItem(type.body);
            this.leftArm.display.setItem(type.leftArm);
            this.rightArm.display.setItem(type.rightArm);
            this.leftLeg.display.setItem(type.leftLeg);
            this.rightLeg.display.setItem(type.rightLeg);
        }

        @Override
        protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {
            super.startWatchingExtraPackets(player, packetConsumer);
            packetConsumer.accept(new ClientboundSetPassengersPacket(this.entity));
        }

        @Override
        protected void notifyElementsOfPositionUpdate(Vec3 newPos, Vec3 delta) {}

        public void setGlowing(boolean glowing) {
            this.head.display.setGlowing(glowing);
            this.body.display.setGlowing(glowing);
            this.leftArm.display.setGlowing(glowing);
            this.rightArm.display.setGlowing(glowing);
            this.leftLeg.display.setGlowing(glowing);
            this.rightLeg.display.setGlowing(glowing);
        }

        public record Bone(ItemDisplayElement display, Vector3f offset, MutableObject<Rotations> angle) {
            public static Bone from(Vector3f offset, Rotations angle) {
                var d = new ItemDisplayElement();
                d.setItemDisplayContext(ItemDisplayContext.GUI);
                d.setOffset(new Vec3(0, EntityType.ARMOR_STAND.getHeight(), 0));
                d.setInvisible(true);
                d.setSendPositionUpdates(false);
                d.setTeleportDuration(2);
                d.setTranslation(offset.sub(0, EntityType.ARMOR_STAND.getHeight(), 0));
                d.setLeftRotation(new Quaternionf().rotationZYX(-angle.z() * Mth.DEG_TO_RAD,
                        -angle.y()  * Mth.DEG_TO_RAD, angle.x() * Mth.DEG_TO_RAD));
                return new Bone(d, offset, new MutableObject<>(angle));
            }

            public boolean updateAngle(Rotations angle) {
                if (this.angle.getValue().equals(angle)) {
                    return false;
                }
                this.angle.setValue(angle);
                display.setLeftRotation(new Quaternionf().rotationZYX(-angle.z() * Mth.DEG_TO_RAD,
                        -angle.y()  * Mth.DEG_TO_RAD, angle.x() * Mth.DEG_TO_RAD));
                display.tick();
                return true;
            }
            public void setScale(float position, float element) {
                this.display.setScale(new Vector3f(element));
                this.display.setTranslation(new Vector3f(this.offset).mul(position));
            }

            public void setYaw(float yaw) {
                this.display.setYaw(yaw);
            }
        }
    }
}
