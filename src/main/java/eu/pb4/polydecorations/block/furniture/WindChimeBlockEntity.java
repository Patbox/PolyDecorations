package eu.pb4.polydecorations.block.furniture;

import eu.pb4.factorytools.api.block.BlockEntityExtraListener;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.item.DecorationsDataComponents;
import eu.pb4.polydecorations.item.WindChimeItem;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class WindChimeBlockEntity extends BlockEntity implements BlockEntityExtraListener {
    private IntList colors = IntList.of();
    private ColorSetter model;

    public WindChimeBlockEntity(BlockPos pos, BlockState state) {
        super(DecorationsBlockEntities.WIND_CHIME, pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.store("colors", DecorationsDataComponents.WIND_CHIME_COLOR.codec(), this.colors);
    }

    @Override
    public void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.colors = view.read("colors", DecorationsDataComponents.WIND_CHIME_COLOR.codec()).orElse(IntList.of());
        if (this.model != null) {
            this.model.setColors(this.colors);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.colors = components.getOrDefault(DecorationsDataComponents.WIND_CHIME_COLOR, this.colors);
        if (this.model != null) {
            this.model.setColors(this.colors);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DecorationsDataComponents.WIND_CHIME_COLOR, this.colors);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput view) {
        super.removeComponentsFromTag(view);
        view.discard("colors");
    }

    @Override
    public void onListenerUpdate(LevelChunk chunk) {
        this.model = (ColorSetter) BlockAwareAttachment.get(chunk, this.worldPosition).holder();
        this.model.setColors(this.colors);
    }

    public interface ColorSetter {
        void setColors(IntList colors);
    }
}
