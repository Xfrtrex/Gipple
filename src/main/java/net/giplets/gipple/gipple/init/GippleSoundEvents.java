package net.giplets.gipple.gipple.init;

import net.giplets.gipple.gipple.Gipple;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class GippleSoundEvents {
    public static final SoundEvent ENTITY_GIPPLE_AMBIENT = register("entity.gipple.gipple_ambient");

    private static SoundEvent register(String id) {
        return Registry.register(Registry.SOUND_EVENT, Gipple.MOD_ID + ":" + id, new SoundEvent(new Identifier(id)));
    }

    public static void init() {
    }
}
