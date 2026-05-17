/*
 * Decompiled with CFR 0.152.
 */
package appeng.core.localization;

import appeng.core.localization.LocalizationEnum;

public enum InGameTooltip implements LocalizationEnum
{
    Channels("%1$d Channels"),
    ChannelsOf("%1$d of %2$d Channels"),
    Charged("%d%% charged"),
    Contains("Contains: %s"),
    Crafting("Crafting: %s"),
    DeviceMissingChannel("Device Missing Channel"),
    DeviceOffline("Device Offline"),
    DeviceOnline("Device Online"),
    EnchantedWith("Enchanted with:"),
    ErrorControllerConflict("Error: Controller Conflict"),
    ErrorNestedP2PTunnel("Error: Nested P2P Tunnel"),
    ErrorTooManyChannels("Error: Too Many Channels"),
    P2PFrequency("Frequency: %s"),
    P2PMECarriedChannels("Carried Channels: %d"),
    Locked("Locked"),
    NetworkBooting("Network Booting"),
    CraftingLockedByRedstoneSignal("Locked by redstone signal"),
    CraftingLockedByLackOfRedstoneSignal("Locked by lack of redstone signal"),
    CraftingLockedUntilPulse("Waiting for redstone pulse to unlock"),
    CraftingLockedUntilResult("Waiting for %s (%d) to unlock"),
    P2PInputManyOutputs("Linked (Input Side) - %d Outputs"),
    P2PInputOneOutput("Linked (Input Side)"),
    P2POutput("Linked (Output Side)"),
    P2PUnlinked("Unlinked"),
    Showing("Showing"),
    Stored("Stored: %s / %s"),
    Suppressed("Suppressed"),
    Unlocked("Unlocked");

    private final String englishText;

    private InGameTooltip(String englishText) {
        this.englishText = englishText;
    }

    @Override
    public String getTranslationKey() {
        return "waila.ae2." + this.name();
    }

    @Override
    public String getEnglishText() {
        return this.englishText;
    }
}

