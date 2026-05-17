/*
 * Decompiled with CFR 0.152.
 */
package appeng.integration.modules.emi;

import appeng.core.localization.LocalizationEnum;
import java.util.Locale;

public enum EmiText implements LocalizationEnum
{
    CATEGORY_CHARGER("Charger"),
    CATEGORY_CONDENSER("Condenser"),
    CATEGORY_ENTROPY_MANIPULATOR("Entropy Manipulator"),
    CATEGORY_INSCRIBER("Inscriber");

    private final String englishText;

    private EmiText(String englishText) {
        this.englishText = englishText;
    }

    @Override
    public String getEnglishText() {
        return this.englishText;
    }

    @Override
    public String getTranslationKey() {
        return "ae2.emi_integration." + this.name().toLowerCase(Locale.ROOT);
    }
}

