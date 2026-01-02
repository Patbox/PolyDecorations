package eu.pb4.polydecorations.block.furniture;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polydecorations.mixin.LivingEntityAccessor;
import eu.pb4.polydecorations.util.DecorationsUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class SleepingBagBlock extends BedBlock implements FactoryBlock, PolymerTexturedBlock {
    public SleepingBagBlock(DyeColor color, Properties settings) {
        super(color, settings);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return DecorationsUtil.TRAPDOOR_STATES_REGULAR.get(Direction.UP);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return initialBlockState.getValue(PART) == BedPart.FOOT ? new Model(initialBlockState, pos) : null;
    }

    public static final class Model extends BlockModel {
        private final ItemDisplayElement main;

        public Model(BlockState state, BlockPos pos) {
            this.main = ItemDisplayElementUtil.createSolid(state.getBlock().asItem());
            this.main.setDisplaySize(2, 2);
            this.main.setItemDisplayContext(ItemDisplayContext.NONE);
            //this.main.setTranslation(new Vector3f(0, 0, 0.5f));
            this.main.setYaw(state.getValue(FACING).toYRot());
            this.addElement(this.main);
        }
    }
}
