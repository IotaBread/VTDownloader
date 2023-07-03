package me.bymartrixx.vtd.gui.widget;

import net.minecraft.text.Text;

public class DebugButtonWidget extends ReloadButtonWidget {
    private static final Text ICON = Text.literal("\uD83D\uDC1B"); // Bug 🐛

    public DebugButtonWidget(int x, int y, Text message, PressAction onPress) {
        super(x, y, message, onPress);
    }

    @Override
    protected Text getIconText() {
        return ICON;
    }
}
