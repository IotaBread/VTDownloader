package me.bymartrixx.vtd.gui.widget;

import net.minecraft.network.chat.Component;

public class DebugButtonWidget extends ReloadButtonWidget {
    private static final Component ICON = Component.literal("\uD83D\uDC1B"); // Bug 🐛

    public DebugButtonWidget(int x, int y, Component message, OnPress onPress) {
        super(x, y, message, onPress);
    }

    @Override
    protected Component getIconText() {
        return ICON;
    }
}
