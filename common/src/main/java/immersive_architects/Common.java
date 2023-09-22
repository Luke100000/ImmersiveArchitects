package immersive_architects;

import immersive_architects.network.NetworkManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Common {
    public static final String SHORT_MOD_ID = "ic_ip";
    public static final String MOD_ID = "immersive_architects";
    public static final Logger LOGGER = LogManager.getLogger();
    public static NetworkManager networkManager;

    public static ResourceLocation locate(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
