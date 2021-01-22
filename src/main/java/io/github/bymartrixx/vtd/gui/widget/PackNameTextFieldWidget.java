package io.github.bymartrixx.vtd.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.io.File;
import java.util.regex.Pattern;

public class PackNameTextFieldWidget extends TextFieldWidget {
    public static final String fileNameRegex = "^[\\w,.\\s-]+$";
    private static final Pattern reservedWindowsName = Pattern.compile("^(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?$");
    private static final Pattern invalidWindowsName = Pattern.compile("^.*\\.$");

    private final File directory;
    private NameValidity nameValidity = NameValidity.VALID;
    private final OnNameUpdate onNameUpdate;
    private final TooltipSupplier tooltipSupplier;

    public PackNameTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text, File directory, OnNameUpdate onNameUpdate, TooltipSupplier tooltipSupplier) {
        super(textRenderer, x, y, width, height, text);
        this.directory = directory;
        this.onNameUpdate = onNameUpdate;

        this.tooltipSupplier = tooltipSupplier;

        this.setChangedListener(this::onChange);
    }

    public PackNameTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text, File directory, OnNameUpdate onNameUpdate, TooltipSupplier tooltipSupplier, String text2) {
        this(textRenderer, x, y, width, height, text, directory, onNameUpdate, tooltipSupplier);
        this.setText(text2);
    }

    private void onChange(String newText) {
        if (newText.equals("")) {
            this.nameValidity = NameValidity.VALID;
        } else if (reservedWindowsName.matcher(newText).matches()) {
            this.nameValidity = NameValidity.RESERVED_WINDOWS;
        } else if (invalidWindowsName.matcher(newText).matches()) {
            this.nameValidity = NameValidity.INVALID_WINDOWS;
        } else if (!newText.matches(fileNameRegex)) {
            this.nameValidity = NameValidity.REGEX_DOESNT_MATCH;
        } else if (new File(this.directory, (newText + ".zip").trim()).exists()) {
            this.nameValidity = NameValidity.FILE_EXISTS;
        } else {
            this.nameValidity = NameValidity.VALID;
        }

        this.onNameUpdate.onNameUpdate();
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);

        this.tooltipSupplier.onTooltip(this, this.nameValidity, matrices, mouseX, mouseY);
    }

    public boolean isNameValid() {
        return this.nameValidity == NameValidity.VALID;
    }

    public interface TooltipSupplier {
        void onTooltip(PackNameTextFieldWidget textField, NameValidity nameValidity, MatrixStack matrices, int mouseX, int mouseY);
    }

    public interface OnNameUpdate {
        void onNameUpdate();
    }

    public enum NameValidity {
        VALID,
        RESERVED_WINDOWS,
        INVALID_WINDOWS,
        REGEX_DOESNT_MATCH,
        FILE_EXISTS
    }
}
