package io.github.bymartrixx.vtd.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.io.File;
import java.util.regex.Pattern;

public class PackNameTextFieldWidget extends TextFieldWidget {
    public static final String fileNameRegex = "^[\\w,\\s-]+$";
    private static final Pattern reservedWindowsName = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?");

    private final File directory;
    private final OnNameUpdate onNameUpdate;
    private final TooltipSupplier reservedTooltipSupplier; // Text is a reserved windows filename
    private final TooltipSupplier regexTooltipSupplier; // Text doesn't match regex
    private final TooltipSupplier fileTooltipSupplier; // File with name already exists
    private boolean renderReservedTooltip = false;
    private boolean renderRegexTooltip = false;
    private boolean renderFileTooltip = false;

    public PackNameTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text, File directory, OnNameUpdate onNameUpdate, TooltipSupplier reservedTooltipSupplier, TooltipSupplier regexTooltipSupplier, TooltipSupplier fileTooltipSupplier) {
        super(textRenderer, x, y, width, height, text);
        this.directory = directory;
        this.onNameUpdate = onNameUpdate;

        this.reservedTooltipSupplier = reservedTooltipSupplier;
        this.regexTooltipSupplier = regexTooltipSupplier;
        this.fileTooltipSupplier = fileTooltipSupplier;
    }

    @Override
    public void write(String string) {
        super.write(string);
        if (this.getText().equals("")) {
            this.renderReservedTooltip = false;
            this.renderRegexTooltip = false;
            this.renderFileTooltip = false;
        } else {
            if (!reservedWindowsName.matcher(this.getText()).matches()) {
                this.renderReservedTooltip = true;
            } else if (this.getText().matches(fileNameRegex)) {
                this.renderRegexTooltip = true;
            } else if (new File(this.directory, this.getText()).exists()) {
                this.renderFileTooltip = true;
            }
        }

        this.onNameUpdate.onNameUpdate();
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);

        if (this.renderReservedTooltip) {
            this.reservedTooltipSupplier.onTooltip(this, matrices, mouseX, mouseY);
        } else if (this.renderRegexTooltip) {
            this.regexTooltipSupplier.onTooltip(this, matrices, mouseX, mouseY);
        } else if (this.renderFileTooltip) {
            this.fileTooltipSupplier.onTooltip(this, matrices, mouseX, mouseY);
        }
    }

    public boolean isNameValid() {
        return !(this.renderReservedTooltip || this.renderRegexTooltip || this.renderFileTooltip);
    }

    public interface TooltipSupplier {
        void onTooltip(PackNameTextFieldWidget textField, MatrixStack matrices, int mouseX, int mouseY);
    }

    public interface OnNameUpdate {
        void onNameUpdate();
    }
}
