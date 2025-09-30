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
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;
import java.util.function.Consumer;

import static eu.pb4.polydecorations.ModInit.id;

public class StatueEntity extends ArmorStandEntity implements PolymerEntity {
    private static final EulerAngle DEFAULT_HEAD_ROTATION = new EulerAngle(0.0F, 0.0F, 0.0F);
    private static final EulerAngle DEFAULT_BODY_ROTATION = new EulerAngle(0.0F, 0.0F, 0.0F);
    private static final EulerAngle DEFAULT_LEFT_ARM_ROTATION = new EulerAngle(-10.0F, 0.0F, -10.0F);
    private static final EulerAngle DEFAULT_RIGHT_ARM_ROTATION = new EulerAngle(-15.0F, 0.0F, 10.0F);
    private static final EulerAngle DEFAULT_LEFT_LEG_ROTATION = new EulerAngle(-1.0F, 0.0F, -1.0F);
    private static final EulerAngle DEFAULT_RIGHT_LEG_ROTATION = new EulerAngle(1.0F, 0.0F, 1.0F);

    private final Model model;
    private ItemStack stack;
    private StatueItem item;

    public StatueEntity(EntityType<? extends ArmorStandEntity> entityType, World world) {
        super(entityType, world);
        this.model = new Model(this);
        this.setStack(DecorationsItems.OTHER_STATUE.get(Type.STONE).getDefaultStack());
        EntityAttachment.of(this.model, this);
        this.setShowArms(true);
        this.setNoGravity(true);
    }

    @Override
    public void onEntityTrackerTick(Set<PlayerAssociatedNetworkHandler> listeners) {
        this.model.tick();
    }

    @Override
    public ItemStack getPickBlockStack() {
        return this.stack.copy();
    }

    @Override
    protected void breakAndDropItem(ServerWorld world, DamageSource damageSource) {
        ItemStack itemStack = this.stack.copy();
        if (this.hasCustomName()) {
            itemStack.set(DataComponentTypes.CUSTOM_NAME, this.getCustomName());
        }

        Block.dropStack(world, this.getBlockPos(), itemStack);
        this.onBreak(world, damageSource);
    }

    @Override
    public void tick() {
        this.model.setScale(this.getScale());
        super.tick();
    }

    @Override
    protected void spawnBreakParticles() {
        if (this.getEntityWorld() instanceof ServerWorld) {
            ((ServerWorld)this.getEntityWorld()).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, this.item.getType().block().getDefaultState()), this.getX(), this.getBodyY(0.6666666666666666), this.getZ(), 10, (double)(this.getWidth() / 4.0F), (double)(this.getHeight() / 4.0F), (double)(this.getWidth() / 4.0F), 0.05);
        }
    }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (!this.stack.isEmpty()) {
            view.put("stack", ItemStack.OPTIONAL_CODEC, this.stack);
        }
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);
        setStack(view.read("stack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
    }

    @Override
    public void setYaw(float yaw) {
        super.setYaw(yaw);
        if (this.model != null) {
            this.model.setYaw(yaw);
        }
    }

    @Override
    public void setGlowing(boolean glowing) {
        super.setGlowing(glowing);
        this.model.setGlowing(glowing);
    }

    @Override
    protected void setFlag(int index, boolean value) {
        super.setFlag(index, value);

        if (index == GLOWING_FLAG_INDEX) {
            this.model.setGlowing(value);
        }
    }

    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (!this.isRemoved()) {
            if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                this.kill(world);
                return false;
            } else if (!this.isInvulnerableTo(world, source) && !this.isInvisible() && !this.isMarker()) {
                if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
                    this.onBreak(world, source);
                    this.kill(world);
                    return false;
                } else if (source.isIn(DamageTypeTags.IGNITES_ARMOR_STANDS) && !this.item.getType().fireproof()) {
                    if (this.isOnFire()) {
                        this.updateHealth(world, source, 0.15F);
                    } else {
                        this.setOnFireFor(5);
                    }

                    return false;
                } else if (source.isIn(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5F && !this.item.getType().fireproof()) {
                    this.updateHealth(world, source, 4.0F);
                    return false;
                } else {
                    boolean bl = source.isIn(DamageTypeTags.CAN_BREAK_ARMOR_STAND);
                    boolean bl2 = source.isIn(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS);
                    if (!bl && !bl2) {
                        return false;
                    } else {
                        Entity var6 = source.getAttacker();
                        if (var6 instanceof PlayerEntity) {
                            PlayerEntity playerEntity = (PlayerEntity)var6;
                            if (!playerEntity.getAbilities().allowModifyWorld) {
                                return false;
                            }
                        }

                        if (source.isSourceCreativePlayer()) {
                            this.playBreakSound();
                            this.spawnBreakParticles();
                            this.kill(world);
                            return true;
                        } else {
                            long l = this.getEntityWorld().getTime();
                            if (l - this.lastHitTime > 5L && !bl2) {
                                this.getEntityWorld().sendEntityStatus(this, (byte)32);
                                this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
                                this.lastHitTime = l;
                            } else {
                                this.breakAndDropItem(world, source);
                                this.spawnBreakParticles();
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
    protected Text getDefaultName() {
        return this.stack.getName();
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.ARMOR_STAND;
    }

    @Override
    public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {
        var sendFlags = initial;
        for (var i = 0; i < data.size(); i++) {
            var x = data.get(i);
            if (x.id() == EntityTrackedData.FLAGS.id()) {
                data.set(i, DataTracker.SerializedEntry.of(EntityTrackedData.FLAGS, (byte) (((byte) x.value()) | (1 << EntityTrackedData.INVISIBLE_FLAG_INDEX))));
                sendFlags = false;
            }
        }

        if (initial && sendFlags) {
            data.add(DataTracker.SerializedEntry.of(EntityTrackedData.FLAGS, (byte) (((byte) this.dataTracker.get(FLAGS)) | (1 << EntityTrackedData.INVISIBLE_FLAG_INDEX))));
        }
    }

    @Override
    protected void playBreakSound() {
        this.getEntityWorld().playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), this.item.getType().soundGroup().getBreakSound(), this.getSoundCategory(), 1.0F, 1.0F);
    }

    @Override
    public FallSounds getFallSounds() {
        return new FallSounds(this.item.getType().soundGroup().getStepSound(), this.item.getType().soundGroup().getFallSound());
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
    public void setHeadRotation(EulerAngle angle) {
        super.setHeadRotation(angle);
        this.model.head.updateAngle(angle);
    }

    @Override
    public void setLeftArmRotation(EulerAngle angle) {
        super.setLeftArmRotation(angle);
        this.model.leftArm.updateAngle(angle);
    }

    @Override
    public void setRightArmRotation(EulerAngle angle) {
        super.setRightArmRotation(angle);
        this.model.rightArm.updateAngle(angle);
    }

    @Override
    public void setLeftLegRotation(EulerAngle angle) {
        super.setLeftLegRotation(angle);
        this.model.leftLeg.updateAngle(angle);
    }

    @Override
    public void setRightLegRotation(EulerAngle angle) {
        super.setRightLegRotation(angle);
        this.model.rightLeg.updateAngle(angle);
    }

    @Override
    public void setBodyRotation(EulerAngle angle) {
        super.setBodyRotation(angle);
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
                x.put(color, nonWood(color.asString() + "_terracotta", Registries.BLOCK.get(Identifier.ofVanilla(color.asString() + "_terracotta"))));
            }
        });
        public static final Map<DyeColor, Type> COLORED_WOOL = Util.make(new HashMap<>(), (x) -> {
            for (var color : DecorationsUtil.COLORS_CREATIVE) {
                x.put(color, burnableNonWood(color.asString() + "_wool", Registries.BLOCK.get(Identifier.ofVanilla(color.asString() + "_wool"))));
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
            return ItemDisplayElementUtil.getModel(id("block/statue/" + type + "/" + head));
        }

        public BlockSoundGroup soundGroup() {
            return block.getDefaultState().getSoundGroup();
        }
    }

    public static final class Model extends ElementHolder {
        public final Bone head = Bone.from(new Vector3f(0, (24 / 16f), 0), StatueEntity.DEFAULT_HEAD_ROTATION);
        public final Bone body = Bone.from(new Vector3f(0, (24 / 16f), 0), StatueEntity.DEFAULT_BODY_ROTATION);
        public final Bone leftArm = Bone.from(new Vector3f(5/16f, (22 / 16f), 0), StatueEntity.DEFAULT_LEFT_ARM_ROTATION);
        public final Bone rightArm = Bone.from(new Vector3f(-5/16f, (22 / 16f), 0), StatueEntity.DEFAULT_RIGHT_ARM_ROTATION);
        public final Bone leftLeg = Bone.from(new Vector3f(2/16f, (12 / 16f), 0), StatueEntity.DEFAULT_LEFT_LEG_ROTATION);
        public final Bone rightLeg = Bone.from(new Vector3f(-2/16f, (12 / 16f), 0), StatueEntity.DEFAULT_RIGHT_LEG_ROTATION);
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
        protected void startWatchingExtraPackets(ServerPlayNetworkHandler player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {
            super.startWatchingExtraPackets(player, packetConsumer);
            packetConsumer.accept(new EntityPassengersSetS2CPacket(this.entity));
        }

        @Override
        protected void notifyElementsOfPositionUpdate(Vec3d newPos, Vec3d delta) {}

        public void setGlowing(boolean glowing) {
            this.head.display.setGlowing(glowing);
            this.body.display.setGlowing(glowing);
            this.leftArm.display.setGlowing(glowing);
            this.rightArm.display.setGlowing(glowing);
            this.leftLeg.display.setGlowing(glowing);
            this.rightLeg.display.setGlowing(glowing);
        }

        public record Bone(ItemDisplayElement display, Vector3f offset, MutableObject<EulerAngle> angle) {
            public static Bone from(Vector3f offset, EulerAngle angle) {
                var d = new ItemDisplayElement();
                d.setItemDisplayContext(ItemDisplayContext.GUI);
                d.setOffset(new Vec3d(0, EntityType.ARMOR_STAND.getHeight(), 0));
                d.setInvisible(true);
                d.setSendPositionUpdates(false);
                d.setTeleportDuration(2);
                d.setTranslation(offset.sub(0, EntityType.ARMOR_STAND.getHeight(), 0));
                d.setLeftRotation(new Quaternionf().rotationZYX(-angle.roll() * MathHelper.RADIANS_PER_DEGREE,
                        -angle.yaw()  * MathHelper.RADIANS_PER_DEGREE, angle.pitch() * MathHelper.RADIANS_PER_DEGREE));
                return new Bone(d, offset, new MutableObject<>(angle));
            }

            public boolean updateAngle(EulerAngle angle) {
                if (this.angle.getValue().equals(angle)) {
                    return false;
                }
                this.angle.setValue(angle);
                display.setLeftRotation(new Quaternionf().rotationZYX(-angle.roll() * MathHelper.RADIANS_PER_DEGREE,
                        -angle.yaw()  * MathHelper.RADIANS_PER_DEGREE, angle.pitch() * MathHelper.RADIANS_PER_DEGREE));
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
