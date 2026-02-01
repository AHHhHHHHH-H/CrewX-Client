package myau.mixin;

import myau.events.RenderItemEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
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

    @Shadow private ItemStack itemToRender;
    @Shadow private float equippedProgress;
    @Shadow private float prevEquippedProgress;

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"), cancellable = true)
    private void onRenderItem(float partialTicks, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        AbstractClientPlayer player = mc.thePlayer;

        // Verifica se há um item na mão
        if (this.itemToRender != null) {
            float swingProgress = player.getSwingProgress(partialTicks);
            EnumAction action = this.itemToRender.getItemUseAction();
            boolean isUsing = player.isUsingItem();

            // Calcula a progressão da animação (subir/descer o item)
            float equipProg = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);

            // DISPARA O SEU EVENTO
            RenderItemEvent event = new RenderItemEvent(
                    action, isUsing, equipProg, partialTicks, swingProgress, this.itemToRender
            );

            // Se o seu EventBus for o do Forge (como parece pelo seu import):
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);

            // Se o evento for cancelado no seu módulo, o Minecraft não desenha o item padrão
            if (event.isCanceled()) {
                ci.cancel();
            }
        }
    }
}