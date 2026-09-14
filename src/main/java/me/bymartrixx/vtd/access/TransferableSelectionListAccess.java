package me.bymartrixx.vtd.access;

import net.minecraft.client.gui.screens.packs.PackSelectionScreen;

public interface TransferableSelectionListAccess {
    boolean vtdownloader$isAvailablePackList();

    int vtdownloader$getItemHeight();

    boolean vtdownloader$isResourcePackList();

    PackSelectionScreen vtdownloader$getScreen();
}
