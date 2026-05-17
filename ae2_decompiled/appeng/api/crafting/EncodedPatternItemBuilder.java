/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  org.jetbrains.annotations.Nullable
 */
package appeng.api.crafting;

import appeng.api.crafting.EncodedPatternDecoder;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.InvalidPatternTooltipStrategy;
import appeng.crafting.pattern.EncodedPatternItem;
import java.util.Objects;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public final class EncodedPatternItemBuilder<T extends IPatternDetails> {
    private final EncodedPatternDecoder<? extends T> decoder;
    @Nullable
    private InvalidPatternTooltipStrategy invalidPatternDescription;
    private Item.Properties properties = new Item.Properties().stacksTo(1);

    EncodedPatternItemBuilder(EncodedPatternDecoder<? extends T> decoder) {
        this.decoder = Objects.requireNonNull(decoder, "decoder");
    }

    public EncodedPatternItemBuilder<T> invalidPatternTooltip(InvalidPatternTooltipStrategy strategy) {
        this.invalidPatternDescription = strategy;
        return this;
    }

    public EncodedPatternItemBuilder<T> itemProperties(Item.Properties properties) {
        this.properties = properties;
        return this;
    }

    public Item build() {
        return new EncodedPatternItem<T>(this.properties, this.decoder, this.invalidPatternDescription);
    }
}

