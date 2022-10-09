package net.giplets.gipple.gipple.init;

import net.giplets.gipple.gipple.Gipple;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

public class GippleTags {


    public static final TagKey<Item> GIPPLE_FOOD = itemTag("gipple_food");

    public static final TagKey<Biome> ALLOWS_GIPPLE_SPAWNS = biomeTag("allows_gipple_spawns");

    private static TagKey<EntityType<?>> entityTag(String id) {
        return TagKey.of(Registry.ENTITY_TYPE_KEY, new Identifier(Gipple.MOD_ID, id));
    }
    private static TagKey<Item> itemTag(String id) {
        return TagKey.of(Registry.ITEM_KEY, new Identifier(Gipple.MOD_ID, id));
    }

    private static TagKey<Biome> biomeTag(String id) {
        return TagKey.of(Registry.BIOME_KEY, new Identifier(Gipple.MOD_ID, id));
    }

    public static void init(){}
}
