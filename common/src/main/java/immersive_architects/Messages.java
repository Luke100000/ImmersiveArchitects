package immersive_architects;


import immersive_architects.cobalt.network.NetworkHandler;
import immersive_architects.network.c2s.StructureBlockUpdateMessage;
import immersive_architects.network.s2c.OpenGuiRequest;

public class Messages {
    public static void bootstrap() {
        // nop
    }

    static {
        NetworkHandler.registerMessage(StructureBlockUpdateMessage.class, StructureBlockUpdateMessage::new);
        NetworkHandler.registerMessage(OpenGuiRequest.class, OpenGuiRequest::new);
    }
}
