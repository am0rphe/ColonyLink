package com.colonylink.colonylink;

// Serialized by ordinal in ColonyLinkPacket and CraftRequestPacket:
// new constants MUST be appended at the end, never inserted.
public enum ResourceStatus
{
    AVAILABLE,      // Blue  - already in ME, ready to send
    CRAFTABLE,      // Green - pattern available OR domum materials available
    NO_PATTERN,     // Red   - no pattern, no way to get it
    CRAFTING,       // Orange - craft in progress
    MISSING,        // Brown - domum: materials missing but craftable via AE2
    SENT_PENDING    // Grey  - sent to warehouse, awaiting courier delivery (WAREHOUSE mode only)
}
