package me.bymartrixx.vtd.gui.widget;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class MutableMessageButtonWidget extends Button.Plain {
    private final Component defaultMessage;
    private Component currentMessage;

    public MutableMessageButtonWidget(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        this.defaultMessage = message;
        this.currentMessage = message;
    }

    public void resetMessage() {
        this.currentMessage = this.defaultMessage;
    }

    public void setMessage(Component message) {
        this.currentMessage = message;
    }

    @Override
    public Component getMessage() {
        return this.currentMessage;
    }
}
