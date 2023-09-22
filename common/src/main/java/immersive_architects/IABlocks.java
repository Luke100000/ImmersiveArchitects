package immersive_architects;

import immersive_architects.cobalt.registration.Registration;
import immersive_architects.block.ArchitectTableBlock;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public interface IABlocks {
    Supplier<Block> ARCHITECT_TABLE = register("architect_table", () -> new ArchitectTableBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_ACACIA_WOOD).noOcclusion()));

    static void bootstrap() {
        // nop
    }

    static <T extends Block> Supplier<T> register(String name, Supplier<T> block) {
        return Registration.register(BuiltInRegistries.BLOCK, Common.locate(name), block);
    }
}
