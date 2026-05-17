/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package appeng.api.integrations.emi;

import appeng.api.integrations.emi.EmiStackConverter;
import com.google.common.collect.ImmutableList;
import java.util.List;

public final class EmiStackConverters {
    private static List<EmiStackConverter> converters = ImmutableList.of();

    private EmiStackConverters() {
    }

    public static synchronized boolean register(EmiStackConverter converter) {
        for (EmiStackConverter existingConverter : converters) {
            if (existingConverter.getKeyType() != converter.getKeyType()) continue;
            return false;
        }
        converters = ImmutableList.builder().addAll(converters).add((Object)converter).build();
        return true;
    }

    public static synchronized List<EmiStackConverter> getConverters() {
        return converters;
    }
}

