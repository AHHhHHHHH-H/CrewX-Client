package myau.mixin;

import myau.event.EventManager;
import myau.events.RenderItemEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Shadow
    private ItemStack itemToRender;

    @Shadow
    private float equippedProgress;

    @Shadow
    private float prevEquippedProgress;

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"), cancellable = true)
    private void injectRenderItemEvent(float partialTicks, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        float f = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
        float swingProgress = mc.thePlayer.getSwingProgress(partialTicks);
        boolean isUseItem = itemToRender != null && mc.thePlayer.getItemInUseCount() > 0;
        EnumAction action = itemToRender != null ? itemToRender.getItemUseAction() : EnumAction.NONE;

        RenderItemEvent event = new RenderItemEvent(
                action,
                isUseItem,
                f,
                partialTicks,
                swingProgress,
                this.itemToRender
        );

        EventManager.call(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}