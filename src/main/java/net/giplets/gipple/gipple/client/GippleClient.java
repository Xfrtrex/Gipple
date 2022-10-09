package net.giplets.gipple.gipple.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.giplets.gipple.gipple.Gipple;
import net.giplets.gipple.gipple.common.gipple.GippleEntityRenderer;
import net.giplets.gipple.gipple.common.mega_gipple.MegaGippleEntityRenderer;

@Environment(EnvType.CLIENT)
public class GippleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Gipple.GIPPLE, GippleEntityRenderer::new);
        EntityRendererRegistry.register(Gipple.MEGA_GIPPLE, MegaGippleEntityRenderer::new);
    }
}
