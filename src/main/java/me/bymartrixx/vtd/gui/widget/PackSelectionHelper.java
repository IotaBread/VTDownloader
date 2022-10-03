package me.bymartrixx.vtd.gui.widget;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import me.bymartrixx.vtd.VTDMod;
import me.bymartrixx.vtd.data.Category;
import me.bymartrixx.vtd.data.Pack;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PackSelectionHelper {
    public static final int DEFAULT_SELECTION_COLOR = -0x10000000;
    private static final List<Integer> INCOMPATIBLE_SELECTION_COLORS = List.of(0xFF007EA7, 0xFFFE7F2D,
            0xFF00FFC5, 0xFFA50021, 0xFFEF7B45, 0xFF98CE00, 0xFF16E0BD, 0xFFF6AE2D, 0xFFDE0D92, 0xFFD14545);

    private final List<String> selection = new ArrayList<>();
    private final List<IncompatibilityGroup> allIncompatibilityGroups = new ArrayList<>();
    private final Multimap<String, IncompatibilityGroup> incompatibilityGroups = LinkedHashMultimap.create();
    @VisibleForTesting
    protected final Map<IncompatibilityGroup, Integer> usedColors = new HashMap<>();
    private SelectionChangeCallback selectionChangeCallback = (pack, category, selected) -> {};

    public void buildIncompatibilityGroups(List<Category> categories) {
        this.allIncompatibilityGroups.clear();
        this.incompatibilityGroups.clear();
        this.usedColors.clear();

        // Create all incompatibility groups
        for (Category c : categories) {
            if (c.isHardIncompatible()) {
                this.allIncompatibilityGroups.add(new CategoryIncompatibilityGroup(c));
                continue;
            }

            for (Pack pack : c.getPacks()) {
                int i;
                // noinspection SuspiciousMethodCalls DefaultIncompatibilityGroup#equals also works for packs
                if ((i = this.allIncompatibilityGroups.indexOf(pack)) == -1) {
                    this.allIncompatibilityGroups.add(new DefaultIncompatibilityGroup(pack));
                } else {
                    ((DefaultIncompatibilityGroup) this.allIncompatibilityGroups.get(i)).bases.add(pack.getId());
                }
            }
        }

        // Assign the groups to their packs
        for (IncompatibilityGroup group : this.allIncompatibilityGroups) {
            for (String id : group.getIds()) {
                this.incompatibilityGroups.put(id, group);
            }
        }
    }

    public void toggleSelection(PackSelectionListWidget.PackEntry entry) {
        Pack pack = entry.getPack();
        PackSelectionData data = entry.selectionData;

        boolean selected;
        if (this.selection.remove(pack.getId())) {
            selected = false;
        } else {
            this.selection.add(pack.getId());
            selected = true;
        }

        this.selectionChangeCallback.onSelectionChanged(pack, data.getCategory(), selected);

        if (selected != data.isSelected()) {
            data.toggleSelection();
        }

        // Remove color for empty incompatibility groups
        for (IncompatibilityGroup group : this.incompatibilityGroups.get(pack.getId())) {
            if (this.usedColors.containsKey(group) && !group.hasIncompatibility(this.selection)) {
                this.usedColors.remove(group);
            }
        }
    }

    public int getSelectionColor(Pack pack) {
        Collection<IncompatibilityGroup> groups = this.incompatibilityGroups.get(pack.getId());

        return groups.stream().sorted(Comparator.comparingInt(IncompatibilityGroup::size))
                .filter(group -> group.hasIncompatibility(this.selection)).findFirst()
                .map(group -> this.usedColors.computeIfAbsent(group, g -> this.getNextColor()))
                .orElse(DEFAULT_SELECTION_COLOR);
    }

    private int getNextColor() {
        for (int color : INCOMPATIBLE_SELECTION_COLORS) {
            if (!this.usedColors.containsValue(color)) {
                return color;
            }
        }

        return INCOMPATIBLE_SELECTION_COLORS.get(0);
    }

    public void addCallback(SelectionChangeCallback callback) {
        SelectionChangeCallback prev = this.selectionChangeCallback;
        this.selectionChangeCallback = (pack, category, selected) -> {
            prev.onSelectionChanged(pack, category, selected);
            callback.onSelectionChanged(pack, category, selected);
        };
    }

    protected void setSelectionChangeCallback(SelectionChangeCallback callback) {
        this.selectionChangeCallback = callback;
    }

    public boolean isSelected(Pack pack) {
        return this.selection.contains(pack.getId());
    }

    protected List<String> getSelection() {
        return this.selection;
    }

    public boolean hasSelection() {
        return !this.selection.isEmpty();
    }

    public void setSelection(List<String> selection) {
        this.selection.clear();
        this.selection.addAll(selection);
    }

    public void cleanUpSelection() {
        List<String> validPacks = this.selection.stream().map(VTDMod.rpCategories::findPack)
                .filter(Objects::nonNull).map(Pack::getId).toList();
        this.selection.retainAll(validPacks);
    }

    public Map<Category, List<Pack>> getSelectedPacks() {
        return this.selection.stream().map(VTDMod.rpCategories::findPack)
                .filter(Objects::nonNull)
                .map(p -> new AbstractMap.SimpleEntry<>(VTDMod.rpCategories.getCategory(p), p))
                .collect(Collectors.groupingBy(Map.Entry::getKey, LinkedHashMap::new,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    public Map<String, List<String>> getSelectedPacksPrimitive() {
        return this.selection.stream().map(VTDMod.rpCategories::findPack)
                .filter(Objects::nonNull)
                .map(p -> new AbstractMap.SimpleEntry<>(VTDMod.rpCategories.getCategory(p), p))
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey().getId(), e.getValue().getId()))
                .collect(Collectors.groupingBy(Map.Entry::getKey, LinkedHashMap::new,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    public interface SelectionChangeCallback {
        void onSelectionChanged(Pack pack, Category category, boolean selected);
    }

    protected interface IncompatibilityGroup {
        Set<String> getIds();

        boolean hasIncompatibility(List<String> packs);

        int size();

        default int count(List<String> packs) {
            List<String> match = new ArrayList<>(this.getIds());
            match.retainAll(packs);
            return match.size();
        }
    }

    protected static class DefaultIncompatibilityGroup implements IncompatibilityGroup {
        private final Set<String> ids = new HashSet<>();
        private final Set<String> bases = new HashSet<>();

        private DefaultIncompatibilityGroup(Pack pack) {
            this.ids.add(pack.getId());
            this.ids.addAll(pack.getIncompatiblePacks());

            this.bases.add(pack.getId());
        }

        @Override
        public Set<String> getIds() {
            return this.ids;
        }

        private int countBases(List<String> packs) {
            List<String> match = new ArrayList<>(this.bases);
            match.retainAll(packs);
            return match.size();
        }

        @Override
        public boolean hasIncompatibility(List<String> packs) {
            return this.countBases(packs) >= 1 && this.count(packs) > 1;
        }

        @Override
        public int size() {
            return this.ids.size();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj instanceof DefaultIncompatibilityGroup group) {
                return ids.equals(group.ids);
            } else if (obj instanceof Pack pack) {
                // Check this group is the pack + its incompatiblePacks
                List<String> packs = pack.getIncompatiblePacks();
                return ids.contains(pack.getId()) && ids.containsAll(packs) && ids.size() == packs.size() + 1;
            }

            return super.equals(obj);
        }

        @Override
        public String toString() {
            return this.ids.toString();
        }
    }

    protected static class CategoryIncompatibilityGroup implements IncompatibilityGroup {
        private final String category;
        private final Set<String> ids = new HashSet<>();

        private CategoryIncompatibilityGroup(Category category) {
            this.category = category.getName();

            this.ids.addAll(category.getPackIds());
        }

        @Override
        public Set<String> getIds() {
            return this.ids;
        }

        @Override
        public boolean hasIncompatibility(List<String> packs) {
            return this.count(packs) > 1;
        }

        @Override
        public int size() {
            return this.ids.size();
        }

        @Override
        public String toString() {
            return "(Category) " + this.category;
        }
    }
}
