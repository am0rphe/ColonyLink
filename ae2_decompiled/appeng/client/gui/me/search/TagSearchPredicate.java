/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 */
package appeng.client.gui.me.search;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.menu.me.common.GridInventoryEntry;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

final class TagSearchPredicate
implements Predicate<GridInventoryEntry> {
    private final String term;
    private final Map<AEKeyType, List<TagKey<?>>> tagCache = new IdentityHashMap();

    public TagSearchPredicate(String term) {
        this.term = term.toLowerCase(Locale.ROOT);
    }

    private List<TagKey<?>> getTagsMatchingTerm(AEKeyType keyType) {
        return keyType.getTagNames().filter(tagKey -> {
            ResourceLocation tagId = tagKey.location();
            if (this.term.contains(":")) {
                return tagId.toString().contains(this.term);
            }
            return tagId.getNamespace().contains(this.term) || tagId.getPath().contains(this.term);
        }).toList();
    }

    @Override
    public boolean test(GridInventoryEntry entry) {
        AEKey what = Objects.requireNonNull(entry.getWhat());
        List tags = this.tagCache.computeIfAbsent(what.getType(), this::getTagsMatchingTerm);
        for (TagKey tag : tags) {
            if (!what.isTagged(tag)) continue;
            return true;
        }
        return false;
    }
}

