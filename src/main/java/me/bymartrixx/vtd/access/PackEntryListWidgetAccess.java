package me.bymartrixx.vtd.access;

import net.minecraft.client.gui.screen.pack.PackScreen;

public interface PackEntryListWidgetAccess {
    boolean vtdownloader$isAvailablePackList();

    int vtdownloader$getItemHeight();

    boolean vtdownloader$isResourcePackList();

    PackScreen vtdownloader$getScreen();
}
