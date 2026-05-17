/*
 * Decompiled with CFR 0.152.
 */
package appeng.core.localization;

import appeng.core.localization.LocalizationEnum;

public enum PlayerMessages implements LocalizationEnum
{
    AmmoDepleted("Ammo Depleted."),
    ChestCannotReadStorageCell("ME Chest cannot read storage cell."),
    ChannelModeSet("Channel mode set to %s. Updated %d grids."),
    ChannelModeCurrent("Current channel mode: %s"),
    ClickToShowDetails("Click to show details"),
    ClickToTeleport("Click to teleport into plot"),
    CommunicationError("Error Communicating with Network."),
    CraftingCpuBusy("This crafting CPU is busy!"),
    DeviceNotLinked("Device is not linked."),
    LinkedNetworkNotFound("Linked network cannot be found"),
    DeviceNotPowered("Device is low on power."),
    MissingBlankPatterns("Not enough blank pattern to restore patterns (missing %d)."),
    MissingUpgrades("Not enough %s to restore upgrades (missing %d)."),
    InvalidMachine("Could not load configuration from an incompatible device."),
    InvalidMachinePartiallyRestored("Partially loaded configuration: %s."),
    LastTransition("Last Transition:"),
    LastTransitionUnknown("Last Transition unknown"),
    LoadedSettings("Loaded device configuration from memory card."),
    MachineNotPowered("Machine is not powered."),
    MinecraftProfile("Minecraft profile (%s)"),
    NoLastTransition("This plot doesn't have a last known transition."),
    NoSpatialIOLevel("The spatial I/O level is missing: %s"),
    NoSpatialIOPlots("There are no spatial I/O plots."),
    NotStorageCell("Storage cell items don't implement the storage cell interface!"),
    NotInSpatialStorageLevel("Must be within the spatial storage level."),
    Origin("Origin"),
    OutOfRange("Wireless Out Of Range."),
    Owner("Owner"),
    PlayerConnected("%s [Connected]"),
    PlayerDisconnected("%s [Disconnected]"),
    PlotID("Plot ID"),
    PlotNotFound("Plot not found: %d"),
    PlotNotFoundForCurrentPosition("Couldn't find a plot for the current position."),
    Plot("Plot"),
    RegionFile("Region file"),
    ResetSettings("New device configuration created and copied to memory card."),
    SavedSettings("Copied current device configuration to memory card."),
    SettingCleared("Memory card cleared."),
    Size("Size"),
    Source("Source"),
    SourceLink("%s - %s to %s"),
    Unknown("Unknown"),
    UnknownAE2Player("Unknown AE2 Player (%s)"),
    When("When"),
    TestWorldNotInCreativeMode("Command can only be used in creative mode."),
    TestWorldNotInSuperflat("A test world can only be set up in a Superflat world!"),
    TestWorldSetupComplete("Test world setup completed in %s"),
    TestWorldSetupFailed("Setting up the test world failed: %s"),
    CompassTestSection("Section [y=%d-%d] %d: %b"),
    isNowLocked("Monitor is now Locked."),
    isNowUnlocked("Monitor is now Unlocked."),
    OnlyEmptyCellsCanBeDisassembled("Only empty storage cells can be disassembled."),
    UnsupportedUpgrade("This upgrade is not supported by this machine."),
    MaxUpgradesOfTypeInstalled("No further upgrade cards of this type can be installed."),
    MaxUpgradesInstalled("The upgrade capacity of this machine has been reached."),
    UnknownHotkey("Unknown Hotkey: "),
    SpecialThanks("Special thanks to %s"),
    FacadePropertySelected("Now cycling through property '%s'."),
    FacadePropertyWrapped("Wrapped around to the first value of '%s'. You can change properties by hitting the facade.");

    private final String englishText;

    private PlayerMessages(String englishText) {
        this.englishText = englishText;
    }

    @Override
    public String getEnglishText() {
        return this.englishText;
    }

    @Override
    public String getTranslationKey() {
        return "chat.ae2." + this.name();
    }
}

