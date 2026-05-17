/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 */
package appeng.core.localization;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public interface LocalizationEnum {
    public String getEnglishText();

    public String getTranslationKey();

    default public MutableComponent text() {
        return Component.translatable((String)this.getTranslationKey());
    }

    default public MutableComponent text(Object ... args) {
        return Component.translatable((String)this.getTranslationKey(), (Object[])args);
    }

    default public MutableComponent withSuffix(String text) {
        return this.text().copy().append(text);
    }

    default public MutableComponent withSuffix(Component text) {
        return this.text().copy().append(text);
    }
}

