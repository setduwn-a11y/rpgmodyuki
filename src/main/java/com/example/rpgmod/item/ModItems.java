package com.example.rpgmod.item;

import com.example.rpgmod.RpgMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, RpgMod.MOD_ID);
        
    public static final DeferredRegister<CreativeModeTab> TABS = 
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RpgMod.MOD_ID);

    // --- LAS 5 ARMAS LEGENDARIAS ---
    // Mandoble (Guerrero): 9 de daño base
    public static final RegistryObject<Item> MANDOBLE_GUERRERO = ITEMS.register("mandoble_guerrero",
        () -> new SwordItem(Tiers.DIAMOND, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.DIAMOND, 5, -3.0F))));

    // Báculo (Mago)
    public static final RegistryObject<Item> BACULO_MAGO = ITEMS.register("baculo_mago",
        () -> new Item(new Item.Properties().stacksTo(1)));

    // Daga (Pícaro): Muy rápida
    public static final RegistryObject<Item> DAGA_PICARO = ITEMS.register("daga_picaro",
        () -> new SwordItem(Tiers.IRON, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.IRON, 2, -1.0F))));

    // Arco de viento (Arquero)
    public static final RegistryObject<Item> ARCO_ARQUERO = ITEMS.register("arco_arquero",
        () -> new Item(new Item.Properties().stacksTo(1)));

    // Maza (Clérigo): Fuerte contra no-muertos
    public static final RegistryObject<Item> MAZA_CLERIGO = ITEMS.register("maza_clerigo",
        () -> new SwordItem(Tiers.IRON, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.IRON, 4, -2.6F))));

    // --- PESTAÑA PROPIA EN EL MENÚ CREATIVO ---
    public static final RegistryObject<CreativeModeTab> TAB_RPG = TABS.register("rpg_tab",
        () -> CreativeModeTab.builder()
            .title(Component.literal("RPG Mod - Armas"))
            .icon(() -> new ItemStack(MANDOBLE_GUERRERO.get()))
            .displayItems((params, output) -> {
                output.accept(MANDOBLE_GUERRERO.get());
                output.accept(BACULO_MAGO.get());
                output.accept(DAGA_PICARO.get());
                output.accept(ARCO_ARQUERO.get());
                output.accept(MAZA_CLERIGO.get());
            })
            .build()
    );

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
    }
}
