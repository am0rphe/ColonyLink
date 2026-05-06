package com.colonylink.colonylink;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ColonyLinkJadePlugin implements IWailaPlugin
{
    @Override
    public void register(IWailaCommonRegistration registration)
    {
        registration.registerBlockDataProvider(
                RedirectorJadeProvider.INSTANCE,
                ColonyLinkRedirectorBlockEntity.class
        );
    }

    @Override
    public void registerClient(IWailaClientRegistration registration)
    {
        registration.registerBlockComponent(
                RedirectorJadeProvider.INSTANCE,
                ColonyLinkRedirectorBlock.class
        );
    }
}