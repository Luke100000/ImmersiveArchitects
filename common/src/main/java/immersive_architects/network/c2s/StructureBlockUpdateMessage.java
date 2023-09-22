package immersive_architects.network.c2s;

import immersive_architects.block.ArchitectTableBlockEntity;
import immersive_architects.cobalt.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class StructureBlockUpdateMessage extends Message {
    private final BlockPos blockPos;
    private final ArchitectTableBlockEntity.Action action;
    private final CompoundTag updateTag;

    public StructureBlockUpdateMessage(BlockPos blockPos, ArchitectTableBlockEntity.Action action, CompoundTag updateTag) {
        this.blockPos = blockPos;
        this.action = action;
        this.updateTag = updateTag;
    }

    public StructureBlockUpdateMessage(FriendlyByteBuf b) {
        this.blockPos = b.readBlockPos();
        this.action = b.readEnum(ArchitectTableBlockEntity.Action.class);
        this.updateTag = b.readNbt();
    }

    @Override
    public void encode(FriendlyByteBuf b) {
        b.writeBlockPos(this.blockPos);
        b.writeEnum(this.action);
        b.writeNbt(this.updateTag);
    }

    @Override
    public void receive(Player e) {
        BlockEntity blockEntity = e.level().getBlockEntity(blockPos);
        if (blockEntity instanceof ArchitectTableBlockEntity entity) {
            entity.load(updateTag);

            if (entity.hasStructureName()) {
                String string = entity.getStructureName();
                if (action == ArchitectTableBlockEntity.Action.SAVE_AREA) {
                    if (entity.saveStructure()) {
                        e.sendSystemMessage(Component.translatable("structure_block.save_success", string));
                    } else {
                        e.sendSystemMessage(Component.translatable("structure_block.save_failure", string));
                    }
                } else if (action == ArchitectTableBlockEntity.Action.LOAD_AREA) {
                    if (!entity.isStructureAvailable()) {
                        e.sendSystemMessage(Component.translatable("structure_block.load_not_found", string));
                    } else if (entity.loadStructure((ServerLevel) e.level())) {
                        e.sendSystemMessage(Component.translatable("structure_block.load_success", string));
                    } else {
                        e.sendSystemMessage(Component.translatable("structure_block.load_prepare", string));
                    }
                } else if (action == ArchitectTableBlockEntity.Action.SCAN_AREA) {
                    if (entity.detectStructureSize()) {
                        e.sendSystemMessage(Component.translatable("structure_block.size_success", string));
                    } else {
                        e.sendSystemMessage(Component.translatable("structure_block.size_failure"));
                    }
                }
            } else {
                e.sendSystemMessage(Component.translatable("structure_block.invalid_structure_name", entity.getStructureName()));
            }
            entity.setChanged();
        }
    }
}
