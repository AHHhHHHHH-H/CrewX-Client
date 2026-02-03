package myau.module.modules;

import myau.event.EventTarget;
import myau.events.*;
import myau.mixin.ItemRendererAccessor;
import myau.module.Module;
import myau.property.properties.ModeProperty;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.util.Utils;
import myau.util.AnimationsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public class Animations extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final BooleanProperty swingWhileDigging = new BooleanProperty("Swing while digging", true);
    public final BooleanProperty clientSide = new BooleanProperty("Client side (visual 1.7)", true);

    public final ModeProperty blockAnimation = new ModeProperty("Block animation", 0, new String[]{"None", "1.7", "Smooth", "Exhibition", "Stab", "Spin", "Sigma", "Wood", "Swong", "Chill", "Komorebi", "Rhys", "Allah"});
    public final ModeProperty swingAnimation = new ModeProperty("Swing Animation", 0, new String[]{"None", "1.9+", "Smooth", "Punch", "Shove"});
    public final ModeProperty otherAnimation = new ModeProperty("Other animation", 0, new String[]{"None", "1.7"});

    public final BooleanProperty modifyAnimations = new BooleanProperty("Customize Animations", false);
    public final FloatProperty staticStartSwingProgress = new FloatProperty("Starting Swing Progress", 0.0F, -1.0F, 2.5F, this.modifyAnimations::getValue);
    public final IntProperty swingSpeed = new IntProperty("Swing Speed", 5, -200, 5, this.modifyAnimations::getValue);
    public final IntProperty swingSpeedWhileBlocking = new IntProperty("Swing Speed while blocking", 5, -200, 5, this.modifyAnimations::getValue);

    public final FloatProperty translatex = new FloatProperty("Translate X", 0.0F, -4.0F, 4.0F);
    public final FloatProperty translatey = new FloatProperty("Translate Y", 0.0F, -2.0F, 2.0F);
    public final FloatProperty translatez = new FloatProperty("Translate Z", 0.0F, -10.0F, 10.0F);

    public final BooleanProperty customRotation = new BooleanProperty("Custom Rotation", false);
    public final FloatProperty rotatex = new FloatProperty("Rotation X", 0.0F, -180.0F, 180.0F, this.customRotation::getValue);
    public final FloatProperty rotatey = new FloatProperty("Rotation Y", 0.0F, -180.0F, 180.0F, this.customRotation::getValue);
    public final FloatProperty rotatez = new FloatProperty("Rotation Z", 0.0F, -180.0F, 180.0F, this.customRotation::getValue);

    private int swing;
    private float staticStartSwingProgressFloat;

    public Animations() {
        super("Animations", false);
    }

    @EventTarget
    public void onSendPacket(SendPacketEvent event) {
        if (this.isEnabled() && swingWhileDigging.getValue() && clientSide.getValue()
                && event.getPacket() instanceof C0APacketAnimation && mc.thePlayer.isUsingItem()) {
            event.setCanceled(true);
        }
    }

    @EventTarget
    public void onRenderItem(@NotNull RenderItemEvent event) {
        if (!this.isEnabled()) return;

        try {
            if (event.getItemToRender() == null || event.getItemToRender().getItem() instanceof ItemMap) {
                return;
            }

            final EnumAction itemAction = event.getEnumAction();
            final ItemRendererAccessor itemRenderer = (ItemRendererAccessor) mc.getItemRenderer();
            final float animationProgression = event.getAnimationProgression();
            float swingProgress = event.getSwingProgress();
            final float convertedProgress = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);

            staticStartSwingProgressFloat = modifyAnimations.getValue() ? staticStartSwingProgress.getValue() : 0.0F;

            GlStateManager.pushMatrix();

            // Aplica as customizações GLOBAIS aqui apenas se não houver item em uso,
            // ou deixe para aplicar dentro de cada caso para maior precisão de pivô.

            if (event.isUseItem()) {
                switch (itemAction) {
                    case NONE:
                        itemRenderer.invoketransformFirstPersonItem(animationProgression, otherAnimation.getValue() == 0 ? staticStartSwingProgressFloat : swingProgress);
                        this.applyCustomTransforms();
                        break;
                    case BLOCK:
                        int blockMode = blockAnimation.getValue();
                        switch (blockMode) {
                            case 0: // None
                                itemRenderer.invoketransformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                this.applyCustomTransforms();
                                AnimationsUtil.blockTransformation();
                                break;
                            case 1: // 1.7
                                itemRenderer.invoketransformFirstPersonItem(animationProgression, swingProgress);
                                this.applyCustomTransforms();
                                AnimationsUtil.blockTransformation();
                                break;
                            case 2: // Smooth
                                itemRenderer.invoketransformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                this.applyCustomTransforms();
                                final float y = -convertedProgress * 2.0F;
                                GlStateManager.translate(0.0F, y / 10.0F + 0.1F, 0.0F);
                                GlStateManager.rotate(y * 10.0F, 0.0F, 1.0F, 0.0F);
                                GlStateManager.rotate(250, 0.2F, 1.0F, -0.6F);
                                GlStateManager.rotate(-10.0F, 1.0F, 0.5F, 1.0F);
                                GlStateManager.rotate(-y * 20.0F, 1.0F, 0.5F, 1.0F);
                                break;
                            case 3: // Exhibition
                                itemRenderer.invoketransformFirstPersonItem(animationProgression / 2.0F, staticStartSwingProgressFloat);
                                this.applyCustomTransforms();
                                GlStateManager.translate(0.0F, 0.3F, -0.0F);
                                GlStateManager.rotate(-convertedProgress * 31.0F, 1.0F, 0.0F, 2.0F);
                                GlStateManager.rotate(-convertedProgress * 33.0F, 1.5F, (convertedProgress / 1.1F), 0.0F);
                                AnimationsUtil.blockTransformation();
                                break;
                            case 4: // Stab
                                final float spin = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                                itemRenderer.invoketransformFirstPersonItem(0.0F, 0.0f);
                                this.applyCustomTransforms();
                                GlStateManager.translate(0.6f, 0.3f, -0.6f + -spin * 0.7);
                                GlStateManager.rotate(6090, 0.0f, 0.0f, 0.1f);
                                GlStateManager.rotate(6085, 0.0f, 0.1f, 0.0f);
                                GlStateManager.rotate(6110, 0.1f, 0.0f, 0.0f);
                                AnimationsUtil.blockTransformation();
                                break;
                            case 5: // Spin
                                itemRenderer.invoketransformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                this.applyCustomTransforms();
                                GlStateManager.translate(0, 0.2F, -1);
                                GlStateManager.rotate(-59, -1, 0, 3);
                                GlStateManager.rotate(-(System.currentTimeMillis() / 2 % 360), 1, 0, 0.0F);
                                GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
                                break;
                            case 6: // Sigma
                                itemRenderer.invoketransformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                this.applyCustomTransforms();
                                GlStateManager.translate(0.0f, 0.1F, 0.0F);
                                AnimationsUtil.blockTransformation();
                                GlStateManager.rotate(convertedProgress * 35.0F / 2.0F, 0.0F, 1.0F, 1.5F);
                                GlStateManager.rotate(-convertedProgress * 135.0F / 4.0F, 1.0f, 1.0F, 0.0F);
                                break;
                            case 7: // Wood
                                itemRenderer.invoketransformFirstPersonItem(animationProgression / 2.0F, staticStartSwingProgressFloat);
                                this.applyCustomTransforms();
                                GlStateManager.translate(0.0F, 0.3F, -0.0F);
                                GlStateManager.rotate(-convertedProgress * 30.0F, 1.0F, 0.0F, 2.0F);
                                GlStateManager.rotate(-convertedProgress * 44.0F, 1.5F, (convertedProgress / 1.2F), 0.0F);
                                AnimationsUtil.blockTransformation();
                                break;
                            case 8: // Swong
                                itemRenderer.invoketransformFirstPersonItem(animationProgression / 2.0F, swingProgress);
                                this.applyCustomTransforms();
                                GlStateManager.rotate(convertedProgress * 30.0F / 2.0F, -convertedProgress, -0.0F, 9.0F);
                                GlStateManager.rotate(convertedProgress * 40.0F, 1.0F, -convertedProgress / 2.0F, -0.0F);
                                GlStateManager.translate(0.0F, 0.2F, 0.0F);
                                AnimationsUtil.blockTransformation();
                                break;
                            case 9: // Chill
                                itemRenderer.invoketransformFirstPersonItem(-0.25F, 1.0F + convertedProgress / 10.0F);
                                this.applyCustomTransforms();
                                GL11.glRotated(-convertedProgress * 25.0F, 1.0F, 0.0F, 0.0F);
                                AnimationsUtil.blockTransformation();
                                break;
                            case 10: // Komorebi
                                itemRenderer.invoketransformFirstPersonItem(animationProgression, swingProgress);
                                this.applyCustomTransforms();
                                GlStateManager.translate(0.41F, -0.25F, -0.5555557F);
                                GlStateManager.rotate(35.0F, 0f, 1.5F, 0.0F);
                                final float A = MathHelper.sin(swingProgress * swingProgress / 64 * (float) Math.PI);
                                GlStateManager.rotate(A * -5.0F, 0.0F, 0.0F, 0.0F);
                                GlStateManager.rotate(convertedProgress * -12.0F, 0.0F, 0.0F, 1.0F);
                                GlStateManager.rotate(convertedProgress * -65.0F, 1.0F, 0.0F, 0.0F);
                                AnimationsUtil.blockTransformation();
                                break;
                            case 11: // Rhys
                                itemRenderer.invoketransformFirstPersonItem(animationProgression, swingProgress);
                                this.applyCustomTransforms();
                                AnimationsUtil.blockTransformation();
                                GlStateManager.translate(-0.3F, -0.1F, -0.0F);
                                break;
                            case 12: // Allah
                                itemRenderer.invoketransformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                this.applyCustomTransforms();
                                AnimationsUtil.blockTransformation();
                                break;
                        }
                        break;
                    case EAT:
                    case DRINK:
                        func_178104_a(mc.thePlayer.getHeldItem(), mc.thePlayer, event.getPartialTicks());
                        itemRenderer.invoketransformFirstPersonItem(animationProgression, otherAnimation.getValue() == 0 ? 0.0F : swingProgress);
                        this.applyCustomTransforms();
                        break;
                    case BOW:
                        itemRenderer.invoketransformFirstPersonItem(animationProgression, otherAnimation.getValue() == 0 ? 0.0F : swingProgress);
                        this.applyCustomTransforms();
                        func_178098_a(mc.thePlayer.getHeldItem(), event.getPartialTicks(), mc.thePlayer);
                        break;
                }
            } else {
                int swingMode = swingAnimation.getValue();
                switch (swingMode) {
                    case 0: // None
                        func_178105_d(swingProgress);
                        itemRenderer.invoketransformFirstPersonItem(animationProgression, swingProgress);
                        this.applyCustomTransforms();
                        break;
                    case 1: // 1.9+
                        func_178105_d(swingProgress);
                        itemRenderer.invoketransformFirstPersonItem(animationProgression, swingProgress);
                        this.applyCustomTransforms();
                        GlStateManager.translate(0, -((swing - 1) - (swing == 0 ? 0 : Utils.getTimer().renderPartialTicks)) / 5f, 0);
                        break;
                    case 2: // Smooth
                        itemRenderer.invoketransformFirstPersonItem(animationProgression, swingProgress);
                        this.applyCustomTransforms();
                        func_178105_d(animationProgression);
                        break;
                    case 3: // Punch
                        itemRenderer.invoketransformFirstPersonItem(animationProgression, swingProgress);
                        this.applyCustomTransforms();
                        func_178105_d(swingProgress);
                        break;
                    case 4: // Shove
                        itemRenderer.invoketransformFirstPersonItem(animationProgression, animationProgression);
                        this.applyCustomTransforms();
                        func_178105_d(swingProgress);
                        break;
                }
            }

            mc.getItemRenderer().renderItem(mc.thePlayer, event.getItemToRender(), null);

            GlStateManager.popMatrix();
            event.setCancelled(true);

        } catch (Exception ignored) {}
    }

    /**
     * Aplica as transformações de Translate e Rotate definidas pelo usuário.
     * Chamado dentro de cada caso após o posicionamento base da mão.
     */
    private void applyCustomTransforms() {
        GlStateManager.translate(translatex.getValue(), translatey.getValue(), translatez.getValue());
        if (customRotation.getValue()) {
            this.rotate(rotatex.getValue(), rotatey.getValue(), rotatez.getValue());
        }
    }

    @EventTarget
    public void onPreMotion(PreMotionEvent event) {
        if (!this.isEnabled()) return;
        try {
            if (mc.thePlayer.swingProgressInt == 1) {
                swing = 9;
            } else {
                swing = Math.max(0, swing - 1);
            }
        } catch (Exception ignore) {}
    }

    @EventTarget
    public void onSwingAnimation(@NotNull SwingAnimationEvent event) {
        if (!this.isEnabled()) return;
        if ((mc.thePlayer.getItemInUseCount() == 1 || mc.thePlayer.isUsingItem()) && modifyAnimations.getValue()) {
            event.setAnimationEnd((int) (event.getAnimationEnd() * ((-swingSpeedWhileBlocking.getValue() / 100.0) + 1.0)));
        } else if (modifyAnimations.getValue()) {
            event.setAnimationEnd((int) (event.getAnimationEnd() * ((-swingSpeed.getValue() / 100.0) + 1.0)));
        }
    }

    private void rotate(float rx, float ry, float rz) {
        GlStateManager.rotate(rx, 1, 0, 0);
        GlStateManager.rotate(ry, 0, 1, 0);
        GlStateManager.rotate(rz, 0, 0, 1);
    }

    private void func_178105_d(float swingProgress) {
        float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
        float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F * 2.0F);
        float f2 = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
        GlStateManager.translate(f, f1, f2);
    }

    private void func_178104_a(ItemStack itemToRender, @NotNull AbstractClientPlayer player, float partialTicks) {
        if (itemToRender == null) return;
        float f = (float) player.getItemInUseCount() - partialTicks + 1.0F;
        float f1 = f / (float) itemToRender.getMaxItemUseDuration();
        float f2 = MathHelper.abs(MathHelper.cos(f / 4.0F * 3.1415927F) * 0.1F);
        if (f1 >= 0.8F) f2 = 0.0F;
        GlStateManager.translate(0.0F, f2, 0.0F);
        float f3 = 1.0F - (float) Math.pow(f1, 27.0);
        GlStateManager.translate(f3 * 0.6F, f3 * -0.5F, f3 * 0.0F);
        GlStateManager.rotate(f3 * 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f3 * 10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f3 * 30.0F, 0.0F, 0.0F, 1.0F);
    }

    private void func_178098_a(@NotNull ItemStack itemToRender, float partialTicks, @NotNull AbstractClientPlayer player) {
        GlStateManager.rotate(-18.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-12.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-8.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-0.9F, 0.2F, 0.0F);
        float f = (float) itemToRender.getMaxItemUseDuration() - ((float) player.getItemInUseCount() - partialTicks + 1.0F);
        float f1 = f / 20.0F;
        f1 = (f1 * f1 + f1 * 2.0F) / 3.0F;
        if (f1 > 1.0F) f1 = 1.0F;
        if (f1 > 0.1F) {
            float f2 = MathHelper.sin((f - 0.1F) * 1.3F);
            float f3 = f1 - 0.1F;
            float f4 = f2 * f3;
            GlStateManager.translate(f4 * 0.0F, f4 * 0.01F, f4 * 0.0F);
        }
        GlStateManager.translate(f1 * 0.0F, f1 * 0.0F, f1 * 0.1F);
        GlStateManager.scale(1.0F, 1.0F, 1.0F + f1 * 0.2F);
    }
}