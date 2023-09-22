package immersive_architects.client.gui;

import immersive_architects.block.ArchitectTableBlockEntity;
import immersive_architects.cobalt.network.NetworkHandler;
import immersive_architects.network.c2s.StructureBlockUpdateMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

@Environment(value = EnvType.CLIENT)
public class ArchitectScreen extends Screen {
    private static final Component STRUCTURE_NAME_TEXT = Component.translatable("structure_block.structure_name");
    private static final Component POSITION_TEXT = Component.translatable("structure_block.position");
    private static final Component SIZE_TEXT = Component.translatable("structure_block.size");
    private static final Component DETECT_SIZE_TEXT = Component.translatable("structure_block.detect_size");
    private static final Component SHOW_AIR_TEXT = Component.translatable("structure_block.show_air");
    private static final Component SHOW_BOUNDING_BOX_TEXT = Component.translatable("structure_block.show_boundingbox");

    private final ArchitectTableBlockEntity table;

    // Original values
    private Mirror originalMirror = Mirror.NONE;
    private Rotation originalRotation = Rotation.NONE;
    private ArchitectTableBlockEntity.Mode mode = ArchitectTableBlockEntity.Mode.LOAD;
    private boolean originalShowAir;
    private boolean originalShowBoundingBox;

    private EditBox inputName;
    private EditBox inputPosX;
    private EditBox inputPosY;
    private EditBox inputPosZ;
    private EditBox inputSizeX;
    private EditBox inputSizeY;
    private EditBox inputSizeZ;
    private Button buttonSave;
    private Button buttonLoad;
    private Button buttonRotate0;
    private Button buttonRotate90;
    private Button buttonRotate180;
    private Button buttonRotate270;
    private Button buttonDetect;
    private CycleButton<Mirror> buttonMirror;
    private CycleButton<Boolean> buttonShowAir;
    private CycleButton<Boolean> buttonShowBoundingBox;

    public ArchitectScreen(ArchitectTableBlockEntity table) {
        super(Component.translatable(Blocks.STRUCTURE_BLOCK.getDescriptionId()));
        this.table = table;
    }

    @Override
    public void tick() {
        inputName.tick();
        inputPosX.tick();
        inputPosY.tick();
        inputPosZ.tick();
        inputSizeX.tick();
        inputSizeY.tick();
        inputSizeZ.tick();
    }

    private void done() {
        updateStructureBlock(ArchitectTableBlockEntity.Action.UPDATE_DATA);
        getMinecraft().setScreen(null);
    }

    private void cancel() {
        table.setMirror(originalMirror);
        table.setRotation(originalRotation);
        table.setMode(mode);
        table.setShowAir(originalShowAir);
        table.setShowBoundingBox(originalShowBoundingBox);
        getMinecraft().setScreen(null);
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> done()).bounds(width / 2 - 4 - 150, 210, 150, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> cancel()).bounds(width / 2 + 4, 210, 150, 20).build());

        // Store original values
        originalMirror = table.getMirror();
        originalRotation = table.getRotation();
        mode = table.getMode();
        originalShowAir = table.shouldShowAir();
        originalShowBoundingBox = table.shouldShowBoundingBox();

        buttonSave = addRenderableWidget(Button.builder(Component.translatable("structure_block.button.save"), button -> {
            if (table.getMode() == ArchitectTableBlockEntity.Mode.SAVE) {
                updateStructureBlock(ArchitectTableBlockEntity.Action.SAVE_AREA);
                getMinecraft().setScreen(null);
            }
        }).bounds(width / 2 + 4 + 100, 185, 50, 20).build());

        buttonLoad = addRenderableWidget(Button.builder(Component.translatable("structure_block.button.load"), button -> {
            if (table.getMode() == ArchitectTableBlockEntity.Mode.LOAD) {
                updateStructureBlock(ArchitectTableBlockEntity.Action.LOAD_AREA);
                getMinecraft().setScreen(null);
            }
        }).bounds(width / 2 + 4 + 100, 185, 50, 20).build());

        buttonDetect = addRenderableWidget(Button.builder(Component.translatable("structure_block.button.detect_size"), button -> {
            if (table.getMode() == ArchitectTableBlockEntity.Mode.SAVE) {
                updateStructureBlock(ArchitectTableBlockEntity.Action.SCAN_AREA);
                getMinecraft().setScreen(null);
            }
        }).bounds(width / 2 + 4 + 100, 120, 50, 20).build());

        this.addRenderableWidget(
                CycleButton.builder(structureMode -> Component.translatable("structure_block.mode." + ((ArchitectTableBlockEntity.Mode) structureMode).getSerializedName()))
                        .withValues(ArchitectTableBlockEntity.Mode.values())
                        .displayOnlyValue()
                        .withInitialValue(mode)
                        .create(this.width / 2 - 4 - 150, 185, 50, 20, Component.literal("MODE"), (cycleButton, structureMode) -> {
                            table.setMode((ArchitectTableBlockEntity.Mode) structureMode);
                        }));
        buttonMirror = addRenderableWidget(CycleButton.builder(Mirror::symbol).withValues(Mirror.values()).displayOnlyValue().withInitialValue(originalMirror).create(width / 2 - 20, 185, 40, 20, Component.literal("MIRROR"), (button, mirror) -> table.setMirror(mirror)));
        buttonShowAir = addRenderableWidget(CycleButton.onOffBuilder(table.shouldShowAir()).displayOnlyValue().create(width / 2 + 4 + 100, 80, 50, 20, SHOW_AIR_TEXT, (button, showAir) -> table.setShowAir(showAir)));
        buttonShowBoundingBox = addRenderableWidget(CycleButton.onOffBuilder(table.shouldShowBoundingBox()).displayOnlyValue().create(width / 2 + 4 + 100, 80, 50, 20, SHOW_BOUNDING_BOX_TEXT, (button, showBoundingBox) -> table.setShowBoundingBox(showBoundingBox)));

        buttonRotate0 = addRenderableWidget(Button.builder(Component.literal("0°"), button -> {
            table.setRotation(Rotation.NONE);
            updateRotationButton();
        }).bounds(width / 2 - 1 - 40 - 1 - 40 - 20, 185, 40, 20).build());
        buttonRotate90 = addRenderableWidget(Button.builder(Component.literal("90°"), button -> {
            table.setRotation(Rotation.CLOCKWISE_90);
            updateRotationButton();
        }).bounds(width / 2 - 1 - 40 - 20, 185, 40, 20).build());
        buttonRotate180 = addRenderableWidget(Button.builder(Component.literal("180°"), button -> {
            table.setRotation(Rotation.CLOCKWISE_180);
            updateRotationButton();
        }).bounds(width / 2 + 1 + 20, 185, 40, 20).build());
        buttonRotate270 = addRenderableWidget(Button.builder(Component.literal("270°"), button -> {
            table.setRotation(Rotation.COUNTERCLOCKWISE_90);
            updateRotationButton();
        }).bounds(width / 2 + 1 + 40 + 1 + 20, 185, 40, 20).build());

        inputName = new EditBox(font, width / 2 - 152, 40, 300, 20, Component.translatable("structure_block.structure_name")) {
            @Override
            public boolean charTyped(char chr, int modifiers) {
                if (!isValidCharacterForName(getValue(), chr, getCursorPosition())) {
                    return false;
                }
                return super.charTyped(chr, modifiers);
            }
        };
        inputName.setMaxLength(128);
        inputName.setValue(table.getStructureName());
        addWidget(inputName);

        BlockPos blockPos = table.getOffset();
        inputPosX = new EditBox(font, width / 2 - 152, 80, 80, 20, Component.translatable("structure_block.position.x"));
        inputPosX.setMaxLength(15);
        inputPosX.setValue(Integer.toString(blockPos.getX()));
        addWidget(inputPosX);

        inputPosY = new EditBox(font, width / 2 - 72, 80, 80, 20, Component.translatable("structure_block.position.y"));
        inputPosY.setMaxLength(15);
        inputPosY.setValue(Integer.toString(blockPos.getY()));
        addWidget(inputPosY);

        inputPosZ = new EditBox(font, width / 2 + 8, 80, 80, 20, Component.translatable("structure_block.position.z"));
        inputPosZ.setMaxLength(15);
        inputPosZ.setValue(Integer.toString(blockPos.getZ()));
        addWidget(inputPosZ);

        Vec3i vec3i = table.getSize();
        inputSizeX = new EditBox(font, width / 2 - 152, 120, 80, 20, Component.translatable("structure_block.size.x"));
        inputSizeX.setMaxLength(15);
        inputSizeX.setValue(Integer.toString(vec3i.getX()));
        addWidget(inputSizeX);

        inputSizeY = new EditBox(font, width / 2 - 72, 120, 80, 20, Component.translatable("structure_block.size.y"));
        inputSizeY.setMaxLength(15);
        inputSizeY.setValue(Integer.toString(vec3i.getY()));
        addWidget(inputSizeY);

        inputSizeZ = new EditBox(font, width / 2 + 8, 120, 80, 20, Component.translatable("structure_block.size.z"));
        inputSizeZ.setMaxLength(15);
        inputSizeZ.setValue(Integer.toString(vec3i.getZ()));
        addWidget(inputSizeZ);

        updateRotationButton();
        updateWidgets(mode);
        setInitialFocus(inputName);
    }

    @Override
    public void resize(@NotNull Minecraft client, int width, int height) {
        String string = inputName.getValue();
        String string2 = inputPosX.getValue();
        String string3 = inputPosY.getValue();
        String string4 = inputPosZ.getValue();
        String string5 = inputSizeX.getValue();
        String string6 = inputSizeY.getValue();
        String string7 = inputSizeZ.getValue();
        init(client, width, height);
        inputName.setValue(string);
        inputPosX.setValue(string2);
        inputPosY.setValue(string3);
        inputPosZ.setValue(string4);
        inputSizeX.setValue(string5);
        inputSizeY.setValue(string6);
        inputSizeZ.setValue(string7);
    }

    private void updateRotationButton() {
        buttonRotate0.active = true;
        buttonRotate90.active = true;
        buttonRotate180.active = true;
        buttonRotate270.active = true;
        switch (table.getRotation()) {
            case NONE -> buttonRotate0.active = false;
            case CLOCKWISE_180 -> buttonRotate180.active = false;
            case COUNTERCLOCKWISE_90 -> buttonRotate270.active = false;
            case CLOCKWISE_90 -> buttonRotate90.active = false;
        }
    }

    private void updateWidgets(ArchitectTableBlockEntity.Mode mode) {
        inputName.setVisible(false);
        inputPosX.setVisible(false);
        inputPosY.setVisible(false);
        inputPosZ.setVisible(false);
        inputSizeX.setVisible(false);
        inputSizeY.setVisible(false);
        inputSizeZ.setVisible(false);
        buttonSave.visible = false;
        buttonLoad.visible = false;
        buttonDetect.visible = false;
        buttonMirror.visible = false;
        buttonRotate0.visible = false;
        buttonRotate90.visible = false;
        buttonRotate180.visible = false;
        buttonRotate270.visible = false;
        buttonShowAir.visible = false;
        buttonShowBoundingBox.visible = false;
        switch (mode) {
            case SAVE -> {
                inputName.setVisible(true);
                inputPosX.setVisible(true);
                inputPosY.setVisible(true);
                inputPosZ.setVisible(true);
                inputSizeX.setVisible(true);
                inputSizeY.setVisible(true);
                inputSizeZ.setVisible(true);
                buttonSave.visible = true;
                buttonDetect.visible = true;
                buttonShowAir.visible = true;
            }
            case LOAD -> {
                inputName.setVisible(true);
                inputPosX.setVisible(true);
                inputPosY.setVisible(true);
                inputPosZ.setVisible(true);
                buttonLoad.visible = true;
                buttonMirror.visible = true;
                buttonRotate0.visible = true;
                buttonRotate90.visible = true;
                buttonRotate180.visible = true;
                buttonRotate270.visible = true;
                buttonShowBoundingBox.visible = true;
                updateRotationButton();
            }
            case CORNER -> inputName.setVisible(true);
        }
    }

    private void updateStructureBlock(ArchitectTableBlockEntity.Action action) {
        BlockPos blockPos = new BlockPos(parseInt(inputPosX.getValue()), parseInt(inputPosY.getValue()), parseInt(inputPosZ.getValue()));
        Vec3i vec3i = new Vec3i(parseInt(inputSizeX.getValue()), parseInt(inputSizeY.getValue()), parseInt(inputSizeZ.getValue()));
        table.setSize(vec3i);
        table.setOffset(blockPos);
        NetworkHandler.sendToServer(new StructureBlockUpdateMessage(table.getBlockPos(), action, table.getUpdateTag()));
    }

    private int parseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    @Override
    public void onClose() {
        cancel();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            done();
            return true;
        }
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        ArchitectTableBlockEntity.Mode structureBlockMode = table.getMode();
        context.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);

        if (mode != ArchitectTableBlockEntity.Mode.DATA) {
            context.drawString(this.font, STRUCTURE_NAME_TEXT, this.width / 2 - 153, 30, 0xA0A0A0);
            this.inputName.render(context, mouseX, mouseY, delta);
        }

        if (mode == ArchitectTableBlockEntity.Mode.LOAD || mode == ArchitectTableBlockEntity.Mode.SAVE) {
            context.drawString(this.font, POSITION_TEXT, this.width / 2 - 153, 70, 10526880);
            this.inputPosX.render(context, mouseX, mouseY, delta);
            this.inputPosY.render(context, mouseX, mouseY, delta);
            this.inputPosZ.render(context, mouseX, mouseY, delta);
        }

        if (mode == ArchitectTableBlockEntity.Mode.SAVE) {
            context.drawString(this.font, SIZE_TEXT, this.width / 2 - 153, 110, 10526880);
            this.inputSizeX.render(context, mouseX, mouseY, delta);
            this.inputSizeY.render(context, mouseX, mouseY, delta);
            this.inputSizeZ.render(context, mouseX, mouseY, delta);

            context.drawString(this.font, SHOW_AIR_TEXT, this.width / 2 + 154 - this.font.width(SHOW_AIR_TEXT), 70, 10526880);
        }

        if (mode == ArchitectTableBlockEntity.Mode.LOAD) {
            context.drawString(this.font, SHOW_BOUNDING_BOX_TEXT, this.width / 2 + 154 - this.font.width(SHOW_BOUNDING_BOX_TEXT), 70, 10526880);
        }

        if (structureBlockMode == ArchitectTableBlockEntity.Mode.SAVE) {
            context.drawString(font, SIZE_TEXT, width / 2 - 153, 110, 0xA0A0A0);
            inputSizeX.render(context, mouseX, mouseY, delta);
            inputSizeY.render(context, mouseX, mouseY, delta);
            inputSizeZ.render(context, mouseX, mouseY, delta);
            context.drawString(font, DETECT_SIZE_TEXT, width / 2 + 154 - font.width(DETECT_SIZE_TEXT), 110, 0xA0A0A0);
            context.drawString(font, SHOW_AIR_TEXT, width / 2 + 154 - font.width(SHOW_AIR_TEXT), 70, 0xA0A0A0);
        }
        context.drawString(font, structureBlockMode.getDisplayName(), width / 2 - 153, 174, 0xA0A0A0);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public Minecraft getMinecraft() {
        assert minecraft != null;
        return minecraft;
    }
}

