package immersive_architects;

import immersive_architects.cobalt.registration.Registration;
import immersive_architects.block.ArchitectTableBlockEntity;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockEntities {
    Supplier<BlockEntityType<ArchitectTableBlockEntity>> ARCHITECT_TABLE = register("architect_table", ArchitectTableBlockEntity::new, () -> new Block[]{IABlocks.ARCHITECT_TABLE.get()});

    static void bootstrap() {
        // nop
    }

    static <T extends BlockEntity> Supplier<BlockEntityType<T>> register(String name, BiFunction<BlockPos, BlockState, T> factory, Supplier<Block[]> blocks) {
        ResourceLocation id = Common.locate(name);
        return Registration.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, () -> Registration.createBlockEntity(id, factory, blocks));
    }
}
