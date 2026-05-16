package com.colonylink.colonylink;

import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Renderer HUD client — badge hotbar (#5).
 * Badge hotbar abandonné (problèmes de z-level et GUI scale).
 * L'indicateur visuel reste le fond orange sur les tabs dans le GUI.
 * Classe conservée pour ne pas casser l'enregistrement dans ColonyLink.java.
 */
public class ColonyLinkHudRenderer
{
    public static void onRenderGuiPostStatic(RenderGuiEvent.Post event)
    {
        // No-op intentionnel
    }
}