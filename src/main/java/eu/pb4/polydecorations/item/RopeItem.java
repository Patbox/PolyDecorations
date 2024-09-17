package eu.pb4.polydecorations.item;

import eu.pb4.factorytools.api.item.FactoryBlockItem;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class RopeItem extends FactoryBlockItem {
	public  <T extends Block & PolymerBlock> RopeItem(T block, Settings settings) {
		super(block, settings);
	}

	@Nullable
	@Override
	public ItemPlacementContext getPlacementContext(ItemPlacementContext context) {
		var blockPos = context.getBlockPos().mutableCopy().move(context.getSide().getOpposite());
		var world = context.getWorld();
		var blockState = world.getBlockState(blockPos);
		Block block = this.getBlock();
		if (!blockState.isOf(block) || context.shouldCancelInteraction()) {
			return block.getDefaultState().canPlaceAt(context.getWorld(), context.getBlockPos()) ? context : null;
		} else {
			while (blockState.isOf(block)) {
				blockPos.move(Direction.DOWN);
				blockState = world.getBlockState(blockPos);
			}

			if (blockState.isReplaceable() || blockState.isAir()) {
				context = new ItemPlacementContext(context.getWorld(), context.getPlayer(), context.getHand(), context.getStack(),
						new BlockHitResult(Vec3d.ofCenter(blockPos.move(Direction.UP)), Direction.DOWN, blockPos.toImmutable(), false));
			}

			return block.getDefaultState().canPlaceAt(context.getWorld(), context.getBlockPos()) ? context : null;
		}
	}

	@Override
	protected boolean checkStatePlacement() {
		return false;
	}
}
