package immersive_architects.network;

import immersive_architects.network.s2c.OpenGuiRequest;

public class ClientNetworkManager implements NetworkManager {
    @Override
    public void handleOpenGuiRequest(OpenGuiRequest request) {
        if (request.gui == OpenGuiRequest.Type.ARCHITECT) {

        }
    }
}
