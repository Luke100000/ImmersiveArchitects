package immersive_architects;

import immersive_architects.network.ClientNetworkManager;

public class ClientMain {
    public static void postLoad() {
        Common.networkManager = new ClientNetworkManager();
    }
}
