package me.bymartrixx.vtd.gui.widget;

import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.data.Pack;

public class PackSelectionData {
    private final Pack pack;
    private final Category category;

    private boolean selected = false;

    protected PackSelectionData(Pack pack, Category category) {
        this.pack = pack;
        this.category = category;
    }

    protected boolean toggleSelection() {
        this.selected = !this.selected;
        return this.selected;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public Pack getPack() {
        return this.pack;
    }

    public Category getCategory() {
        return this.category;
    }
}
