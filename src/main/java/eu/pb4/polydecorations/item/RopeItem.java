package eu.pb4.polydecorations.item;

import eu.pb4.factorytools.api.item.FactoryBlockItem;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RopeItem extends FactoryBlockItem {
	public  <T extends Block & PolymerBlock> RopeItem(T block, Properties settings) {
		super(block, settings);
	}

	@Nullable
	@Override
	public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
		var blockPos = context.getClickedPos().mutable().move(context.getClickedFace().getOpposite());
		var world = context.getLevel();
		var blockState = world.getBlockState(blockPos);
		Block block = this.getBlock();
		if (!blockState.is(block) || context.isSecondaryUseActive()) {
			return block.defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos()) ? context : null;
		} else {
			while (blockState.is(block)) {
				blockPos.move(Direction.DOWN);
				blockState = world.getBlockState(blockPos);
			}

			if (blockState.canBeReplaced() || blockState.isAir()) {
				context = new BlockPlaceContext(context.getLevel(), context.getPlayer(), context.getHand(), context.getItemInHand(),
						new BlockHitResult(Vec3.atCenterOf(blockPos.move(Direction.UP)), Direction.DOWN, blockPos.immutable(), false));
			}

			return block.defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos()) ? context : null;
		}
	}

	@Override
	protected boolean mustSurvive() {
		return false;
	}
}
