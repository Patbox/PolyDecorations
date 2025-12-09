package eu.pb4.polydecorations.entity;

import eu.pb4.polydecorations.util.DecorationsGamerules;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Objects;
import java.util.Set;

public class SeatEntity extends Entity implements PolymerEntity {
    @Nullable
    private Direction direction = Direction.UP;

    public static boolean create(Level world, BlockPos pos, double yOffset, @Nullable Direction direction, Entity player) {
        if (!(world instanceof ServerLevel serverWorld) || !world.getEntitiesOfClass(SeatEntity.class, new AABB(pos), x -> true).isEmpty()) {
            return false;
        }

        var timeout = serverWorld.getGameRules().get(DecorationsGamerules.SEAT_USE_COOLDOWN);

        if (player.getVehicle() instanceof SeatEntity seatEntity && seatEntity.tickCount < timeout) {
            return false;
        }


        var entity = new SeatEntity(DecorationsEntities.SEAT, world);
        entity.direction = direction;
        entity.setPos(pos.getX() + 0.5, pos.getY() + 0.5 + yOffset, pos.getZ() + 0.5);
        entity.setYRot(direction != null ? direction.toYRot() : player.getYRot() + 180);
        world.addFreshEntity(entity);
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
    public SeatEntity(EntityType<?> type, Level world) {
        super(type, world);
        this.setInvisible(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isVehicle()) {
            this.discard();
        } else {
            this.setYRot(Objects.requireNonNull(this.getFirstPassenger()).getYRot());
        }
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {

    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.BLOCK_DISPLAY;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        var box = passenger.getLocalBoundsForPose(Pose.STANDING);
        var dir = this.direction == null ? passenger.getDirection() : this.direction;
        var curr = Vec3.atBottomCenterOf(this.blockPosition()).relative(dir  , 1);
        if (this.level().noCollision(box.move(curr))) {
           return curr;
        }
        curr = Vec3.atBottomCenterOf(this.blockPosition()).relative(Direction.UP, 1);
        if (this.level().noCollision(box.move(curr))) {
            return curr;
        }
        return Vec3.atBottomCenterOf(this.blockPosition());
    }
}
