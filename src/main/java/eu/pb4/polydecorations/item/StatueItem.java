package eu.pb4.polydecorations.item;

import eu.pb4.polydecorations.entity.DecorationsEntities;
import eu.pb4.polydecorations.entity.StatueEntity;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.function.Consumer;

public class StatueItem extends SimplePolymerItem {
    private final StatueEntity.Type type;

    public StatueItem(StatueEntity.Type type, Item.Properties settings) {
        super(settings);
        this.type = type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction direction = context.getClickedFace();
        if (direction == Direction.DOWN) {
            return InteractionResult.FAIL;
        } else {
            Level world = context.getLevel();
            BlockPlaceContext itemPlacementContext = new BlockPlaceContext(context);
            BlockPos blockPos = itemPlacementContext.getClickedPos();
            ItemStack itemStack = context.getItemInHand();
            Vec3 vec3d = Vec3.atBottomCenterOf(blockPos);
            AABB box = DecorationsEntities.STATUE.getDimensions().makeBoundingBox(vec3d.x(), vec3d.y(), vec3d.z());
            if (world.noCollision(null, box) && world.getEntities(null, box).isEmpty()) {
                if (world instanceof ServerLevel serverWorld) {
                    Consumer<StatueEntity> consumer = EntityType.createDefaultStackConfig(serverWorld, itemStack, context.getPlayer());
                    var statueEntity = DecorationsEntities.STATUE.create(serverWorld, consumer, blockPos, EntitySpawnReason.MOB_SUMMONED, true, true);
                    if (statueEntity == null) {
                        return InteractionResult.FAIL;
                    }
                    statueEntity.setStack(itemStack.copyWithCount(1));
                    float f = (float) Mth.floor((Mth.wrapDegrees(context.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
                    statueEntity.snapTo(statueEntity.getX(), statueEntity.getY(), statueEntity.getZ(), f, 0.0F);
                    serverWorld.addFreshEntityWithPassengers(statueEntity);
                    world.playSound(
                            null, statueEntity.getX(), statueEntity.getY(), statueEntity.getZ(), type.block().defaultBlockState().getSoundType().getPlaceSound(), SoundSource.BLOCKS, 0.75F, 0.8F
                    );
                    statueEntity.gameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
                }

                itemStack.shrink(1);
                return InteractionResult.SUCCESS_SERVER;
            } else {
                return InteractionResult.FAIL;
            }
        }
    }

    public StatueEntity.Type getType() {
        return type;
    }
}