package immersive_architects.client.render.entity.renderer;

import immersive_architects.entity.ArchitectEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ArchitectEntityRenderer<T extends ArchitectEntity> extends EntityRenderer<T> {
    public ArchitectEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return null;
    }
}