package myau.mixin;

import net.minecraft.client.renderer.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor {
    // Essa porra não funcionou então eu criei a AnimationsUtil
    // @Invoker("func_178103_d")
    // void blockTransformation();

    @Invoker("transformFirstPersonItem")
    void invoketransformFirstPersonItem(float animationProgression, float swingProgress);
}