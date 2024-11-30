package eu.pb4.polydecorations.item;

import eu.pb4.polydecorations.entity.DecorationsEntities;
import eu.pb4.polydecorations.entity.StatueEntity;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.function.Consumer;

public class StatueItem extends SimplePolymerItem {
    private final StatueEntity.Type type;

    public StatueItem(StatueEntity.Type type, Item.Settings settings) {
        super(settings);
        this.type = type;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        Direction direction = context.getSide();
        if (direction == Direction.DOWN) {
            return ActionResult.FAIL;
        } else {
            World world = context.getWorld();
            ItemPlacementContext itemPlacementContext = new ItemPlacementContext(context);
            BlockPos blockPos = itemPlacementContext.getBlockPos();
            ItemStack itemStack = context.getStack();
            Vec3d vec3d = Vec3d.ofBottomCenter(blockPos);
            Box box = DecorationsEntities.STATUE.getDimensions().getBoxAt(vec3d.getX(), vec3d.getY(), vec3d.getZ());
            if (world.isSpaceEmpty(null, box) && world.getOtherEntities(null, box).isEmpty()) {
                if (world instanceof ServerWorld serverWorld) {
                    Consumer<StatueEntity> consumer = EntityType.copier(serverWorld, itemStack, context.getPlayer());
                    var statueEntity = DecorationsEntities.STATUE.create(serverWorld, consumer, blockPos, SpawnReason.MOB_SUMMONED, true, true);
                    if (statueEntity == null) {
                        return ActionResult.FAIL;
                    }
                    statueEntity.setStack(itemStack.copyWithCount(1));
                    float f = (float) MathHelper.floor((MathHelper.wrapDegrees(context.getPlayerYaw() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
                    statueEntity.refreshPositionAndAngles(statueEntity.getX(), statueEntity.getY(), statueEntity.getZ(), f, 0.0F);
                    serverWorld.spawnEntityAndPassengers(statueEntity);
                    world.playSound(
                            null, statueEntity.getX(), statueEntity.getY(), statueEntity.getZ(), type.block().getDefaultState().getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 0.75F, 0.8F
                    );
                    statueEntity.emitGameEvent(GameEvent.ENTITY_PLACE, context.getPlayer());
                }

                itemStack.decrement(1);
                return ActionResult.SUCCESS_SERVER;
            } else {
                return ActionResult.FAIL;
            }
        }
    }

    public StatueEntity.Type getType() {
        return type;
    }
}