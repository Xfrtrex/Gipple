package net.giplets.gipple.gipple;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.giplets.gipple.gipple.common.gipple.GippleEntity;
import net.giplets.gipple.gipple.common.mega_gipple.MegaGippleEntity;
import net.giplets.gipple.gipple.init.GippleTags;
import net.giplets.gipple.gipple.init.GippleItems;
import net.giplets.gipple.gipple.init.GippleSoundEvents;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gipple implements ModInitializer {
    public static final String MOD_ID = "gipple";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


    public static final EntityType<GippleEntity> GIPPLE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(MOD_ID, "gipple"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GippleEntity::new).dimensions(EntityDimensions.fixed(0.75f, 0.5f)).build()
    );
    public static final EntityType<MegaGippleEntity> MEGA_GIPPLE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(MOD_ID, "mega_gipple"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, MegaGippleEntity::new).dimensions(EntityDimensions.fixed(1.5f, 0.6f)).build()
    );



    @Override
    public void onInitialize() {
        GippleSoundEvents.init();
        GippleItems.init();
        GippleTags.init();

        FabricDefaultAttributeRegistry.register(GIPPLE, GippleEntity.createGippleAttributes());
        FabricDefaultAttributeRegistry.register(MEGA_GIPPLE, MegaGippleEntity.createMegaGippleAttributes());

        BiomeModifications.addSpawn(BiomeSelectors.tag(GippleTags.ALLOWS_GIPPLE_SPAWNS), SpawnGroup.WATER_CREATURE, GIPPLE, 7, 1, 4);
    }
}
