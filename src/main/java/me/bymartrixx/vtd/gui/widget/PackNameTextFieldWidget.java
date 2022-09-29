package me.bymartrixx.vtd.gui.widget;

import net.minecraft.client.font.TextRenderer;
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

    private final Path directory;
    private NameStatus nameStatus = NameStatus.VALID;

    public PackNameTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, Text text, Path directory) {
        super(textRenderer, x, y, width, height, copyFrom, text);
        this.directory = directory;

        super.setChangedListener(this::onChange);
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
