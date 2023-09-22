package immersive_architects.network.s2c;

import immersive_architects.Common;
import immersive_architects.cobalt.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class OpenGuiRequest extends Message {
    public final Type gui;

    public final int entity;

    public OpenGuiRequest(OpenGuiRequest.Type gui, int entity) {
        this.gui = gui;
        this.entity = entity;
    }

    public OpenGuiRequest(FriendlyByteBuf b) {
        this.gui = b.readEnum(OpenGuiRequest.Type.class);
        this.entity = b.readInt();
    }

    @Override
    public void encode(FriendlyByteBuf b) {
        b.writeEnum(gui);
        b.writeInt(entity);
    }

    @Override
    public void receive(Player e) {
        Common.networkManager.handleOpenGuiRequest(this);
    }

    public enum Type {
        ARCHITECT,
    }
}
