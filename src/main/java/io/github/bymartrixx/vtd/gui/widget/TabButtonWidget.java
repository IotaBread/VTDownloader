package io.github.bymartrixx.vtd.gui.widget;

import io.github.bymartrixx.vtd.object.PackCategory;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TabButtonWidget extends ButtonWidget {
    private final PackCategory<?> category;

    public TabButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress,
                           PackCategory<?> category) {
        super(x, y, width, height, message, onPress);
        this.category = category;
    }

    public int getRight() {
        return this.x + this.width;
    }
}
