package myau.mixin;

import net.minecraft.client.renderer.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.renderer.entity.RenderItem;

@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor {

    @Invoker("transformFirstPersonItem")
    void invokeTransformFirstPersonItem(float equipProgress, float swingProgress);
}
