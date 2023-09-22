package immersive_architects.cobalt.registration;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class Registration {
    private static Impl INSTANCE;

    public static <T> Supplier<T> register(Registry<? super T> registry, ResourceLocation id, Supplier<T> obj) {
        return INSTANCE.register(registry, id, obj);
    }

    public static <T extends Entity> void registerEntityRenderer(EntityType<?> type, EntityRendererProvider<T> constructor) {
        //noinspection unchecked
        INSTANCE.registerEntityRenderer((EntityType<T>) type, constructor);
    }

    public static <T extends BlockEntity> BlockEntityType<T> createBlockEntity(ResourceLocation id, BiFunction<BlockPos, BlockState, T> factory, Supplier<Block[]> blocks) {
        return INSTANCE.<T>blockEntity().apply(id, factory, blocks);
    }

    public abstract static class Impl {
        protected Impl() {
            INSTANCE = this;
        }

        public abstract <T> Supplier<T> register(Registry<? super T> registry, ResourceLocation id, Supplier<T> obj);

        public abstract <T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererProvider<T> constructor);

        public abstract <T extends BlockEntity> BlockEntityTypeFactory<T> blockEntity();
    }

    public interface BlockEntityTypeFactory<T extends BlockEntity> {
        BlockEntityType<T> apply(ResourceLocation id, BiFunction<BlockPos, BlockState, T> factory, Supplier<Block[]> blocks);
    }
}
