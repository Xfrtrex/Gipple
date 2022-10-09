package net.giplets.gipple.gipple.init;

import net.giplets.gipple.gipple.Gipple;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class GippleItems {

    public static final Item GIPPLE_SPAWN_EGG = register("gipple_spawn_egg", new SpawnEggItem(Gipple.GIPPLE, 13558777, 11642584,
            (new Item.Settings()).group(ItemGroup.MISC)));
    public static final Item MEGA_GIPPLE_SPAWN_EGG = register("mega_gipple_spawn_egg", new SpawnEggItem(Gipple.MEGA_GIPPLE, 13558777, 9864682,
            (new Item.Settings()).group(ItemGroup.MISC)));


    public static final Item GELATIN = register("gelatin", new Item ((new Item.Settings())
            .group(ItemGroup.FOOD)
            .food(GippleFoodComponents.GELATIN)));

    public static final Item GELATIN_SOUP = register("gelatin_soup", new Item ((new Item.Settings())
            .group(ItemGroup.FOOD)
            .maxCount(1)
            .food(GippleFoodComponents.GELATIN_SOUP)));


    private static Item register(String name, Item item) {
        return Registry.register(Registry.ITEM, new Identifier(Gipple.MOD_ID, name), item);
    }

    public static void init(){

    }
}
