package eu.pb4.polydecorations.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Set;

public class SeatEntity extends Entity implements PolymerEntity {
    private Direction direction = Direction.UP;

    public static boolean create(World world, BlockPos pos, double yOffset, Direction direction, Entity player) {
        if (!world.getEntitiesByClass(SeatEntity.class, new Box(pos), x -> true).isEmpty()) {
            return false;
        }

        var entity = new SeatEntity(DecorationsEntities.SEAT, world);
        entity.direction = direction;
        entity.setPosition(pos.getX() + 0.5, pos.getY() + 0.5 + yOffset, pos.getZ() + 0.5);
        entity.setYaw(direction.getPositiveHorizontalDegrees());
        world.spawnEntity(entity);
        player.setSprinting(false);
        player.startRiding(entity);
        /*if (MathHelper.angleBetween(direction.getPositiveHorizontalDegrees(), player.getYaw()) > 90) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw() - 180, 0,
                        Set.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z, PositionFlag.X_ROT));
            }
            player.setYaw(player.getYaw() - 180);
        }*/

        return true;
    }
    public SeatEntity(EntityType<?> type, World world) {
        super(type, world);
        this.setInvisible(true);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {

    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.hasPassengers()) {
            this.discard();
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readCustomData(ReadView view) {

    }

    @Override
    protected void writeCustomData(WriteView view) {

    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.BLOCK_DISPLAY;
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        var box = passenger.getBoundingBox(EntityPose.STANDING);
        var curr = Vec3d.ofBottomCenter(this.getBlockPos()).offset(this.direction, 1);
        if (this.getWorld().isSpaceEmpty(box.offset(curr))) {
           return curr;
        }
        curr = Vec3d.ofBottomCenter(this.getBlockPos()).offset(Direction.UP, 1);
        if (this.getWorld().isSpaceEmpty(box.offset(curr))) {
            return curr;
        }
        return Vec3d.ofBottomCenter(this.getBlockPos());
    }
}
