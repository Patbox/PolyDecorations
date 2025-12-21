package eu.pb4.polydecorations.canvas;

import com.mojang.serialization.Codec;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;

import java.nio.ByteBuffer;
import java.util.Arrays;

public record CanvasPixels(byte[] data) implements DrawableCanvas {
    public static final Codec<CanvasPixels> CODEC = Codec.BYTE_BUFFER
            .xmap(ByteBuffer::array, ByteBuffer::wrap)
            .xmap(CanvasPixels::new, CanvasPixels::dataCopy);

    public byte[] dataCopy() {
        return Arrays.copyOf(data, data.length);
    }

    public CanvasPixels() {
        this(new byte[16 * 16]);
    }

    @Override
    public byte getRaw(int x, int y) {
        var i = x + y * 16;
        if (i < 0 || i >= this.data.length) {
            return 0;
        }
        return this.data[i];
    }

    @Override
    public void setRaw(int x, int y, byte color) {
        var i = x + y * 16;
        if (i < 0 || i >= this.data.length) {
            return;
        }
        this.data[i] = color;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public int getWidth() {
        return 16;
    }

    public boolean isColored() {
        for (int i = 0; i < this.data.length; i++) {
            if (this.data[i] != 0) {
                return true;
            }
        }
        return false;
    }
}
