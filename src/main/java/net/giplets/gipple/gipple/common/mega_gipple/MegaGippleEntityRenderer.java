package net.giplets.gipple.gipple.common.mega_gipple;

import net.giplets.gipple.gipple.common.gipple.GippleEntity;
import net.giplets.gipple.gipple.common.gipple.GippleEntityModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class MegaGippleEntityRenderer extends GeoEntityRenderer<MegaGippleEntity> {
    public MegaGippleEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new MegaGippleEntityModel());
    }

}