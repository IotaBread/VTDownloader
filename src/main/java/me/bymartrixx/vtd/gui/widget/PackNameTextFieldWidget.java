package me.bymartrixx.vtd.gui.widget;

import me.bymartrixx.vtd.util.RenderUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class PackNameTextFieldWidget extends TextFieldWidget {
    public static final String FILE_NAME_REGEX = "^[\\w,.\\s-]+$";
    private static final Pattern RESERVED_WINDOWS_NAME = Pattern.compile("^(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern INVALID_WINDOWS_NAME = Pattern.compile("^.*\\.$");

    private static final Text FILE_EXISTS_TEXT = Text.translatable("vtd.fileNameValidity.fileExists");
    private static final Text INVALID_WINDOWS_NAME_TEXT = Text.translatable("vtd.fileNameValidity.invalidWindows");
    private static final Text RESERVED_WINDOWS_NAME_TEXT = Text.translatable("vtd.fileNameValidity.reservedWindows");
    private static final Text REGEX_DOESNT_MATCH_TEXT = Text.translatable("vtd.fileNameValidity.regexDoesntMatch", FILE_NAME_REGEX);

    public static final int MAX_LENGTH = 64;
    private static final int ERROR_COLOR = 0xFFEA5146;
    private static final int ERROR_FOCUSED_COLOR = 0xFFFF6666;
    private static final int WARNING_COLOR = 0xFFF2B50C;
    private static final int WARNING_FOCUSED_COLOR = 0xFFFFEF0F;

    private final TextRenderer textRenderer; // TextFieldWidget#textRenderer is private
    private final Path directory;
    private final String defaultName;
    private NameStatus nameStatus = NameStatus.VALID;

    public PackNameTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable String copyText, Text text, Path directory, @Nullable String defaultName) {
        super(textRenderer, x, y, width, height, text);
        this.textRenderer = textRenderer;
        this.directory = directory;
        this.defaultName = defaultName;

        super.setChangedListener(this::onChange);
        this.setMaxLength(MAX_LENGTH);

        if (copyText != null) {
            this.setText(copyText);
        }
    }

    @Override
    public void setChangedListener(Consumer<String> changedListener) {
        super.setChangedListener(((Consumer<String>) this::onChange).andThen(changedListener));
    }

    private void onChange(String text) {
        if (!text.isBlank()) {
            if (RESERVED_WINDOWS_NAME.matcher(text).matches()) {
                this.nameStatus = NameStatus.RESERVED_WINDOWS;
            } else if (INVALID_WINDOWS_NAME.matcher(text).matches()) {
                this.nameStatus = NameStatus.INVALID_WINDOWS;
            } else if (!text.matches(FILE_NAME_REGEX)) {
                this.nameStatus = NameStatus.REGEX_DOESNT_MATCH;
            } else if (Files.exists(this.directory.resolve(text + ".zip"))) {
                this.nameStatus = NameStatus.FILE_EXISTS;
            } else {
                this.nameStatus = NameStatus.VALID;
            }
        } else {
            this.nameStatus = NameStatus.VALID;
        }
    }

    public boolean isBlank() {
        return this.getText().isBlank();
    }

    public boolean canUseName() {
        return !this.nameStatus.isError();
    }

    private boolean isNewName() {
        return this.defaultName == null || !this.defaultName.equals(this.getText());
    }

    @Nullable
    public Text getTooltipText() {
        return switch (this.nameStatus) {
            case FILE_EXISTS -> this.isNewName() ? FILE_EXISTS_TEXT : null;
            case RESERVED_WINDOWS -> RESERVED_WINDOWS_NAME_TEXT;
            case INVALID_WINDOWS -> INVALID_WINDOWS_NAME_TEXT;
            case REGEX_DOESNT_MATCH -> REGEX_DOESNT_MATCH_TEXT;
            case VALID -> null;
        };
    }

    @Override
    public void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.drawWidget(graphics, mouseX, mouseY, delta);

        if (this.isVisible()) {
            if (this.getText().isEmpty()) {
                int x = this.getX() + 4;
                int y = this.getY() + (this.height - 8) / 2;
                graphics.drawShadowedText(this.textRenderer, this.getMessage(), x, y, 0x707070);
            }

            this.renderOutline(graphics);
        }
    }

    private void renderOutline(GuiGraphics graphics) {
        int color = -1;
        if (this.nameStatus.isError()) {
            color = this.isFocused() ? ERROR_FOCUSED_COLOR : ERROR_COLOR;
        } else if (this.nameStatus == NameStatus.FILE_EXISTS && this.isNewName()) {
            color = this.isFocused() ? WARNING_FOCUSED_COLOR : WARNING_COLOR;
        }

        if (color != -1) {
            RenderUtil.drawOutline(graphics, this.getX(), this.getY(), this.width, this.height, 1, color);
        }
    }

    public enum NameStatus {
        VALID(false),
        FILE_EXISTS(false),
        RESERVED_WINDOWS(true),
        INVALID_WINDOWS(true),
        REGEX_DOESNT_MATCH(true);

        private final boolean error;

        NameStatus(boolean error) {
            this.error = error;
        }

        public boolean isError() {
            return this.error;
        }
    }
}
