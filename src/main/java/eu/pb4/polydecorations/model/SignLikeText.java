package eu.pb4.polydecorations.model;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.AbstractElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.function.Consumer;

@SuppressWarnings("FieldCanBeLocal")
public class SignLikeText extends AbstractElement {
    private static final int CENTER = 4;
    private final TextDisplayElement[] displayText = new TextDisplayElement[9];

    private Text text = Text.empty();

    private boolean glow = false;
    private float viewRange = 1;
    private Vector3f translation = new Vector3f();
    private Vector3f scale = new Vector3f(1);

    public SignLikeText() {
        //noinspection ConstantValue
        for (int i = 0; i < this.displayText.length; i++) {
            var text = new TextDisplayElement();
            text.setBackground(0);
            text.setInvisible(true);
            this.displayText[i] = text;
        }
    }

    public void setText(Text text, DyeColor color, boolean glow) {
        this.text = text;
        var brightness = glow ? new Brightness(15, 15) : null;
        this.displayText[CENTER].setText(Text.empty().append(text).withColor(color.getSignColor()));
        this.displayText[CENTER].setBrightness(brightness);

        if (glow) {
            var background = Text.empty().append(text).withColor(getOutlineColor(color));
            for (int i = 0; i < this.displayText.length; i++) {
                if (i != CENTER) {
                    this.displayText[i].setText(background);
                    this.displayText[i].setBrightness(brightness);
                    if (!this.glow && this.getHolder() != null) {
                        this.getHolder().addElement(this.displayText[i]);
                    }
                }
            }
        } else if (this.glow && this.getHolder() != null) {
            for (int i = 0; i < this.displayText.length; i++) {
                if (i != CENTER) {
                    this.getHolder().removeElement(this.displayText[i]);
                }
            }
        }
        this.glow = glow;
    }

    public static int getOutlineColor(DyeColor color) {
        if (color == DyeColor.BLACK) {
            return -988212;
        } else {
            int i = color.getSignColor();
            int j = (int)((double) ColorHelper.getRed(i) * 0.4);
            int k = (int)((double)ColorHelper.getGreen(i) * 0.4);
            int l = (int)((double)ColorHelper.getBlue(i) * 0.4);
            return ColorHelper.getArgb(0, j, k, l);
        }
    }

    @Override
    public void setHolder(ElementHolder holder) {
        var old = this.getHolder();
        super.setHolder(holder);
        if (holder != null) {
            if (this.glow) {
                holder.addElement(this.displayText[CENTER]);
            } else {
                for (var x : this.displayText) {
                    holder.addElement(x);
                }
            }
        } else if (old != null) {
            for (var x : this.displayText) {
                old.removeElement(x);
            }
        }
    }

    @Override
    public void tick() {
        if (this.glow) {
            for (var x : this.displayText) {
                x.tick();
            }
        } else {
            this.displayText[CENTER].tick();
        }
    }

    public void setViewRange(float v) {
        this.viewRange = v;
        for (var x : this.displayText) {
            x.setViewRange(v);
        }
    }

    public void setTranslation(Vector3f vector3f) {
        this.translation = vector3f;
        for (int i = 0; i < this.displayText.length; i++) {
            if (CENTER == i) {
                this.displayText[i].setTranslation(new Vector3f(vector3f).add(0, 0, 0.002f));
            } else {
                int x = i % 3 - 1;
                int y = i / 3 - 1;
                this.displayText[i].setTranslation(new Vector3f(vector3f).add(x / 40f * this.scale.x, y / 40f * this.scale.y, 0));
            }
        }
    }

    public void setYaw(float yaw) {
        for (var x : this.displayText) {
            x.setYaw(yaw);
        }
    }

    public void setScale(Vector3f vector3f) {
        this.scale = vector3f;
        for (var x : this.displayText) {
            x.setScale(vector3f);
        }
    }

    public void setDisplaySize(int x, int y) {
        for (var e : this.displayText) {
            e.setDisplaySize(x, y);
        }
    }


    @Override
    public IntList getEntityIds() {
        return IntList.of();
    }

    @Override
    public void startWatching(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {}

    @Override
    public void stopWatching(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {}

    @Override
    public void notifyMove(Vec3d oldPos, Vec3d currentPos, Vec3d delta) {

    }
}
