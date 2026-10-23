package com.example.rpgmod.network;

import com.example.rpgmod.client.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public record ManaSyncPacket(float mana, int maxMana) {

    public static void encode(ManaSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.mana);
        buf.writeInt(msg.maxMana);
    }

    public static ManaSyncPacket decode(FriendlyByteBuf buf) {
        return new ManaSyncPacket(buf.readFloat(), buf.readInt());
    }

    public static void handle(ManaSyncPacket msg, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientHandler.actualizarMana(msg.mana(), msg.maxMana());
            }
        });
        ctx.setPacketHandled(true);
    }
}
