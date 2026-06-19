package eu.pb4.polydecorations.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.data.EntityData;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.Consumers;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.List;
import java.util.function.Consumer;

public class FirstLeashFenceKnotEntity extends LeashFenceKnotEntity implements Leashable, PolymerEntity {
    private final ElementHolder holder;
    private final LeadAttachmentElement leadAttachment;
    @Nullable
    private LeashData leashData;

    public FirstLeashFenceKnotEntity(EntityType<? extends FirstLeashFenceKnotEntity> entityType, Level level) {
        super(entityType, level);
        this.holder = new ElementHolder();
        this.leadAttachment = this.holder.addElement(new LeadAttachmentElement());
        this.leadAttachment.setInteractionHandler(VirtualElement.InteractionHandler.redirect(this));
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public void dropItem(ServerLevel serverLevel, @Nullable Entity entity) {
        super.dropItem(serverLevel, entity);
        //this.spawnAtLocation(serverLevel, Items.LEAD);
    }

    @Override
    protected Component getTypeName() {
        return EntityTypes.LEASH_KNOT.getDescription();
    }

    @Override
    public void tick() {
        if (this.level() instanceof ServerLevel level) {
            Leashable.tickLeash(level, this);
            if (this.leashData == null) {
                this.kill(level);
            }
        }
        super.tick();
    }

    @Override
    public @Nullable LeashData getLeashData() {
        return this.leashData;
    }

    @Override
    public void setLeashData(@Nullable LeashData leashData) {
        this.leashData = leashData;
    }

    @Override
    public void remove(Entity.RemovalReason removalReason) {
        if (!this.level().isClientSide() && removalReason.shouldDestroy() && this.isLeashed()) {
            this.dropLeash();
        }

        super.remove(removalReason);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        this.writeLeashData(valueOutput, this.leashData);
    }

    @Override
    public void onEntityPacketSent(Consumer<Packet<?>> consumer, Packet<?> packet) {
        if (packet instanceof ClientboundSetEntityLinkPacket linkPacket) {
            consumer.accept(VirtualEntityUtils.createClientboundSetEntityLinkPacket(this.leadAttachment.getEntityId(), linkPacket.getDestId()));
        } else {
            consumer.accept(packet);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.readLeashData(valueInput);
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityTypes.LEASH_KNOT;
    }

    private class LeadAttachmentElement extends GenericEntityElement {
        public LeadAttachmentElement() {
            this.syncedData.set(EntityData.SILENT, true);
            this.syncedData.set(EntityData.NO_GRAVITY, true);
            this.setOffset(new Vec3(0, EntityTypes.LEASH_KNOT.getHeight() / 2 - EntityTypes.SLIME.getDimensions().eyeHeight(), 0));
            this.syncedData.set(EntityData.FLAGS, (byte) ((1 << EntityData.INVISIBLE_FLAG_INDEX)));
        }


        @Override
        public void startWatching(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {
            super.startWatching(player, packetConsumer);
            var scale = new AttributeInstance(Attributes.SCALE, Consumers.nop());
            scale.setBaseValue(EntityTypes.LEASH_KNOT.getWidth() / EntityTypes.SLIME.getWidth());
            packetConsumer.accept(new ClientboundUpdateAttributesPacket(this.getEntityId(), List.of(
                    scale
            )));

            var d = FirstLeashFenceKnotEntity.this.leashData;
            if (d != null && d.leashHolder != null) {
                packetConsumer.accept(VirtualEntityUtils.createClientboundSetEntityLinkPacket(this.getEntityId(), d.leashHolder.getId()));
            }
        }

        @Override
        protected EntityType<? extends Entity> getEntityType() {
            return EntityTypes.SLIME;
        }
    }
}
