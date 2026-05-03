package com.colonylink.colonylink;

public enum ResourceStatus
{
    AVAILABLE,      // Blue  - already in ME, ready to send
    CRAFTABLE,      // Green - pattern available OR domum materials available
    NO_PATTERN,     // Red   - no pattern, no way to get it
    CRAFTING,       // Orange - craft in progress
    MISSING         // Brown - domum: materials missing but craftable via AE2
}