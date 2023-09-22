package immersive_architects;

import immersive_architects.client.render.entity.renderer.ArchitectEntityRenderer;
import immersive_architects.cobalt.registration.Registration;

public class Renderer {
    public static void bootstrap() {
        Registration.registerEntityRenderer(Entities.ARCHITECT.get(), ArchitectEntityRenderer::new);
    }
}
