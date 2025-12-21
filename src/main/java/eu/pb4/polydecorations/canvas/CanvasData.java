package eu.pb4.polydecorations.canvas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.mapcanvas.api.core.CanvasColor;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public record CanvasData(Optional<CanvasPixels> image, Optional<CanvasColor> background, boolean glowing, boolean waxed, boolean cut) {
        private static final CanvasPixels EMPTY_IMAGE = new CanvasPixels(new byte[0]);
        public static final Codec<CanvasData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                CanvasPixels.CODEC.optionalFieldOf("image").forGetter(CanvasData::image),
                Codec.BYTE.xmap(CanvasColor::getFromRaw, CanvasColor::getRenderColor).optionalFieldOf("background").forGetter(CanvasData::background),
                Codec.BOOL.optionalFieldOf("glowing", false).forGetter(CanvasData::glowing),
                Codec.BOOL.optionalFieldOf("waxed", false).forGetter(CanvasData::waxed),
                Codec.BOOL.optionalFieldOf("cut", false).forGetter(CanvasData::cut)
        ).apply(instance, CanvasData::new));

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            CanvasData data = (CanvasData) object;


            return glowing == data.glowing && waxed == data.waxed && cut == data.cut
                    && Arrays.equals(image.orElse(EMPTY_IMAGE).data(), data.image.orElse(EMPTY_IMAGE).data())
                    && Objects.equals(background, data.background);
        }

        @Override
        public int hashCode() {
            return Objects.hash(image, background, glowing, waxed, cut);
        }

        public static final CanvasData DEFAULT = new CanvasData(Optional.empty(),  Optional.empty(), false, false, false);
    }