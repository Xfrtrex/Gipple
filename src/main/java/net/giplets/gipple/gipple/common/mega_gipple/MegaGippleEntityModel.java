package net.giplets.gipple.gipple.common.mega_gipple;

import net.giplets.gipple.gipple.Gipple;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

public class MegaGippleEntityModel extends AnimatedTickingGeoModel<MegaGippleEntity> {
    @Override
    public Identifier getAnimationResource(MegaGippleEntity entity) {
        return new Identifier(Gipple.MOD_ID, "animations/mega_gipple.animation.json");
    }

    @Override
    public Identifier getModelResource(MegaGippleEntity entity) {
        return new Identifier(Gipple.MOD_ID, "geo/mega_gipple.geo.json");
    }

    @Override
    public Identifier getTextureResource(MegaGippleEntity entity) {
        return new Identifier(Gipple.MOD_ID, "textures/entity/mega_gipple.png");
    }

}