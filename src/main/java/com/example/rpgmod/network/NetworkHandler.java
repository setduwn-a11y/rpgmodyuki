package com.example.rpgmod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public final class NetworkHandler {
    public static final SimpleChannel CHANNEL = ChannelBuilder
        .named(ResourceLocation.fromNamespaceAndPath("rpgmod", "main"))
        .networkProtocolVersion(1)
        .simpleChannel();

    public static void init() {
        CHANNEL.messageBuilder(ManaSyncPacket.class, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(ManaSyncPacket::encode)
            .decoder(ManaSyncPacket::decode)
            .consumerMainThread(ManaSyncPacket::handle)
            .add();
    }
}
