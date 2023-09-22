package immersive_architects.block;

import immersive_architects.BlockEntities;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ArchitectTableBlockEntity extends BlockEntity {
    private ResourceLocation structureName;

    // general settings
    private BlockPos offset = new BlockPos(0, 0, 0);
    private Vec3i size = new Vec3i(8, 8, 8);

    // building mode
    private Mirror mirror = Mirror.NONE;
    private Rotation rotation = Rotation.NONE;

    // visual settings
    private Mode mode = Mode.LOAD;
    private boolean showAir;
    private boolean showBoundingBox = true;

    public ArchitectTableBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntities.ARCHITECT_TABLE.get(), blockPos, blockState);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putString("name", this.getStructureName());
        nbt.putInt("posX", this.offset.getX());
        nbt.putInt("posY", this.offset.getY());
        nbt.putInt("posZ", this.offset.getZ());
        nbt.putInt("sizeX", this.size.getX());
        nbt.putInt("sizeY", this.size.getY());
        nbt.putInt("sizeZ", this.size.getZ());
        nbt.putString("rotation", this.rotation.toString());
        nbt.putString("mirror", this.mirror.toString());
        nbt.putString("mode", this.mode.toString());
        nbt.putBoolean("showair", this.showAir);
        nbt.putBoolean("showboundingbox", this.showBoundingBox);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.setStructureName(nbt.getString("name"));
        int i = Mth.clamp(nbt.getInt("posX"), -48, 48);
        int j = Mth.clamp(nbt.getInt("posY"), -48, 48);
        int k = Mth.clamp(nbt.getInt("posZ"), -48, 48);
        this.offset = new BlockPos(i, j, k);
        int l = Mth.clamp(nbt.getInt("sizeX"), 0, 48);
        int m = Mth.clamp(nbt.getInt("sizeY"), 0, 48);
        int n = Mth.clamp(nbt.getInt("sizeZ"), 0, 48);
        this.size = new Vec3i(l, m, n);
        try {
            this.rotation = Rotation.valueOf(nbt.getString("rotation"));
        } catch (IllegalArgumentException illegalArgumentException) {
            this.rotation = Rotation.NONE;
        }
        try {
            this.mirror = Mirror.valueOf(nbt.getString("mirror"));
        } catch (IllegalArgumentException illegalArgumentException) {
            this.mirror = Mirror.NONE;
        }
        try {
            this.mode = Mode.valueOf(nbt.getString("mode"));
        } catch (IllegalArgumentException illegalArgumentException) {
            this.mode = Mode.DATA;
        }
        this.showAir = nbt.getBoolean("showair");
        this.showBoundingBox = nbt.getBoolean("showboundingbox");
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public String getStructureName() {
        return this.structureName == null ? "" : this.structureName.toString();
    }

    public String getStructurePath() {
        return this.structureName == null ? "" : this.structureName.getPath();
    }

    public boolean hasStructureName() {
        return this.structureName != null;
    }

    public void setStructureName(@Nullable String name) {
        this.setStructureName(name == null || StringUtils.isEmpty(name) ? null : ResourceLocation.tryParse(name));
    }

    public void setStructureName(@Nullable ResourceLocation structureName) {
        this.structureName = structureName;
    }

    public BlockPos getOffset() {
        return this.offset;
    }

    public void setOffset(BlockPos offset) {
        this.offset = offset;
    }

    public Vec3i getSize() {
        return this.size;
    }

    public void setSize(Vec3i size) {
        this.size = size;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public void setMirror(Mirror mirror) {
        this.mirror = mirror;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public Mode getMode() {
        return this.mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public boolean detectStructureSize() {
        if (this.mode != Mode.SAVE) {
            return false;
        }
        BlockPos blockPos = this.getBlockPos();
        BlockPos blockPos2 = new BlockPos(blockPos.getX() - 80, this.level.getMinBuildHeight(), blockPos.getZ() - 80);
        BlockPos blockPos3 = new BlockPos(blockPos.getX() + 80, this.level.getMaxBuildHeight() - 1, blockPos.getZ() + 80);
        Stream<BlockPos> stream = this.streamCornerPos(blockPos2, blockPos3);
        return ArchitectTableBlockEntity.getStructureBox(blockPos, stream).filter(box -> {
            int i = box.maxX() - box.minX();
            int j = box.maxY() - box.minY();
            int k = box.maxZ() - box.minZ();
            if (i > 1 && j > 1 && k > 1) {
                this.offset = new BlockPos(box.minX() - blockPos.getX() + 1, box.minY() - blockPos.getY() + 1, box.minZ() - blockPos.getZ() + 1);
                this.size = new Vec3i(i - 1, j - 1, k - 1);
                this.setChanged();
                BlockState blockState = this.level.getBlockState(blockPos);
                this.level.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_ALL);
                return true;
            }
            return false;
        }).isPresent();
    }

    /**
     * Streams positions of {@link Mode#CORNER} mode structure blocks with matching names.
     */
    private Stream<BlockPos> streamCornerPos(BlockPos start, BlockPos end) {
        return BlockPos.betweenClosedStream(start, end).filter(pos -> this.level.getBlockState(pos).is(Blocks.STRUCTURE_BLOCK)).map(this.level::getBlockEntity).filter(blockEntity -> blockEntity instanceof ArchitectTableBlockEntity).map(blockEntity -> (ArchitectTableBlockEntity) blockEntity).filter(blockEntity -> blockEntity.mode == Mode.CORNER && Objects.equals(this.structureName, blockEntity.structureName)).map(BlockEntity::getBlockPos);
    }

    private static Optional<BoundingBox> getStructureBox(BlockPos pos, Stream<BlockPos> corners) {
        Iterator<BlockPos> iterator = corners.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        BlockPos blockPos = iterator.next();
        BoundingBox blockBox = new BoundingBox(blockPos);
        if (iterator.hasNext()) {
            iterator.forEachRemaining(blockBox::encapsulate);
        } else {
            blockBox.encapsulate(pos);
        }
        return Optional.of(blockBox);
    }

    public boolean saveStructure() {
        return this.saveStructure(true);
    }

    public boolean saveStructure(boolean bl) {
        StructureTemplate structure;
        if (this.mode != Mode.SAVE || this.level.isClientSide || this.structureName == null) {
            return false;
        }
        BlockPos blockPos = this.getBlockPos().offset(this.offset);
        ServerLevel serverWorld = (ServerLevel) this.level;
        StructureTemplateManager structureManager = serverWorld.getStructureManager();
        try {
            structure = structureManager.getOrCreate(this.structureName);
        } catch (ResourceLocationException invalidIdentifierException) {
            return false;
        }
        structure.fillFromWorld(this.level, blockPos, this.size, false, Blocks.STRUCTURE_VOID);
        if (bl) {
            try {
                return structureManager.save(this.structureName);
            } catch (ResourceLocationException invalidIdentifierException) {
                return false;
            }
        }
        return true;
    }

    public boolean loadStructure(ServerLevel world) {
        return this.loadStructure(world, true);
    }

    private static RandomSource createRandom(long seed) {
        if (seed == 0L) {
            return RandomSource.create(Util.getMillis());
        }
        return RandomSource.create(seed);
    }

    public boolean loadStructure(ServerLevel world, boolean bl) {
        Optional<StructureTemplate> optional;
        if (this.mode != Mode.LOAD || this.structureName == null) {
            return false;
        }
        StructureTemplateManager structureManager = world.getStructureManager();
        try {
            optional = structureManager.get(this.structureName);
        } catch (ResourceLocationException invalidIdentifierException) {
            return false;
        }
        return optional.filter(structureTemplate -> this.place(world, bl, structureTemplate)).isPresent();
    }

    public boolean place(ServerLevel world, boolean bl, StructureTemplate structure) {
        Vec3i vec3i;
        boolean bl2;
        BlockPos blockPos = this.getBlockPos();
        StringUtil.isNullOrEmpty(structure.getAuthor());
        if (!(bl2 = this.size.equals(vec3i = structure.getSize()))) {
            this.size = vec3i;
            this.setChanged();
            BlockState blockState = world.getBlockState(blockPos);
            world.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_ALL);
        }
        if (!bl || bl2) {
            StructurePlaceSettings structurePlacementData = new StructurePlaceSettings().setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(true);
            BlockPos blockPos2 = blockPos.offset(this.offset);
            structure.placeInWorld(world, blockPos2, blockPos2, structurePlacementData, ArchitectTableBlockEntity.createRandom(1), Block.UPDATE_CLIENTS);
            return true;
        }
        return false;
    }

    public void unloadStructure() {
        if (this.structureName == null) {
            return;
        }
        ServerLevel serverWorld = (ServerLevel) this.level;
        StructureTemplateManager structureManager = serverWorld.getStructureManager();
        structureManager.remove(this.structureName);
    }

    public boolean isStructureAvailable() {
        if (this.mode != Mode.LOAD || this.level.isClientSide || this.structureName == null) {
            return false;
        }
        ServerLevel serverWorld = (ServerLevel) this.level;
        StructureTemplateManager structureManager = serverWorld.getStructureManager();
        try {
            return structureManager.get(this.structureName).isPresent();
        } catch (ResourceLocationException invalidIdentifierException) {
            return false;
        }
    }

    public boolean shouldShowAir() {
        return this.showAir;
    }

    public void setShowAir(boolean showAir) {
        this.showAir = showAir;
    }

    public boolean shouldShowBoundingBox() {
        return this.showBoundingBox;
    }

    public void setShowBoundingBox(boolean showBoundingBox) {
        this.showBoundingBox = showBoundingBox;
    }

    public enum Action {
        UPDATE_DATA,
        SAVE_AREA,
        LOAD_AREA,
        SCAN_AREA

    }

    public enum Mode implements StringRepresentable {
        SAVE("save"),
        LOAD("load"),
        CORNER("corner"),
        DATA("data");

        private final String name;
        private final Component displayName;

        Mode(String string2) {
            this.name = string2;
            this.displayName = Component.translatable("structure_block.mode_info." + string2);
        }

        public String getSerializedName() {
            return this.name;
        }

        public Component getDisplayName() {
            return this.displayName;
        }
    }
}

