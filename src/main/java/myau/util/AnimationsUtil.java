package myau.util;

import net.minecraft.client.renderer.GlStateManager;

public class AnimationsUtil {
    // Já que eu não consegui criar no ItemRendererAccessor eu deciid criar aqui
    public static void blockTransformation() {
        GlStateManager.translate(-0.5F, 0.2F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
    }
}