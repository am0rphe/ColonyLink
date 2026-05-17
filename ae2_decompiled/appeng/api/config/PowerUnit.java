/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package appeng.api.config;

import net.minecraft.network.chat.Component;

public enum PowerUnit {
    AE("gui.ae2.units.appliedenergistics", "AE"),
    FE("gui.ae2.units.fe", "FE");

    public final String unlocalizedName;
    public final String symbolName;
    public double conversionRatio = 1.0;

    private PowerUnit(String un, String symbolName) {
        this.unlocalizedName = un;
        this.symbolName = symbolName;
    }

    public double convertTo(PowerUnit target, double value) {
        return value * this.conversionRatio / target.conversionRatio;
    }

    public Component textComponent() {
        return Component.translatable((String)this.unlocalizedName);
    }

    public String getSymbolName() {
        return this.symbolName;
    }
}

