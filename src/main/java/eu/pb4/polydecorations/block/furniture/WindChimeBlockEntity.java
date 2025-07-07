package eu.pb4.polydecorations.block.furniture;

import eu.pb4.factorytools.api.block.BlockEntityExtraListener;
import eu.pb4.polydecorations.block.DecorationsBlockEntities;
import eu.pb4.polydecorations.item.WindChimeItem;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

public class WindChimeBlockEntity extends BlockEntity implements BlockEntityExtraListener {
    private IntList colors = IntList.of();
    private ColorSetter model;

    public WindChimeBlockEntity(BlockPos pos, BlockState state) {
        super(DecorationsBlockEntities.WIND_CHIME, pos, state);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.put("colors", WindChimeItem.WIND_CHIME_COLOR.getCodec(), this.colors);
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);
        this.colors = view.read("colors", WindChimeItem.WIND_CHIME_COLOR.getCodec()).orElse(IntList.of());
        if (this.model != null) {
            this.model.setColors(this.colors);
        }
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        this.colors = components.getOrDefault(WindChimeItem.WIND_CHIME_COLOR, this.colors);
        if (this.model != null) {
            this.model.setColors(this.colors);
        }
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        builder.add(WindChimeItem.WIND_CHIME_COLOR, this.colors);
    }

    @Override
    public void removeFromCopiedStackData(WriteView view) {
        super.removeFromCopiedStackData(view);
        view.remove("colors");
    }

    @Override
    public void onListenerUpdate(WorldChunk chunk) {
        this.model = (ColorSetter) BlockAwareAttachment.get(chunk, this.pos).holder();
        this.model.setColors(this.colors);
    }

    public interface ColorSetter {
        void setColors(IntList colors);
    }
}
