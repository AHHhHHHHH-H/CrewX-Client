package myau.module.modules;

import myau.events.*;
import myau.mixin.ItemRendererAccessor;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.util.*;
import myau.dependencias.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public class Animations extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static final BooleanProperty swingWhileDigging = new BooleanProperty("Swing while digging", true); // Esse if
    public static final BooleanProperty clientSide = new BooleanProperty("Client side (visual 1.7)", true); // Colocar um if
    private final ModeProperty blockAnimation = new ModeProperty("Block animation", 0, new String[]{"None", "1.7", "Smooth", "Exhibition", "Stab", "Spin", "Sigma", "Wood", "Swong", "Chill", "Komorebi", "Rhys", "Allah"});
    private final ModeProperty swingAnimation = new ModeProperty("Swing animation", 0, new String[]{"None", "1.9+", "Smooth", "Punch", "Shove"});
    private final ModeProperty otherAnimation = new ModeProperty("Other animation", 0, new String[]{"None", "1.7"});
    private final BooleanProperty fakeSlotReset = new BooleanProperty("Fake slot reset", false);

    private final BooleanProperty modifyAnimations = new BooleanProperty("Customize Animations", false); // Esse if
    private final FloatProperty staticStartSwingProgress = new FloatProperty("Starting Swing Progress", 0.0f, -1.0f, 2.5f); // colocar um if
    private final IntProperty swingSpeed = new IntProperty("Swing speed", 0, -200, 50); // colocar um if
    private final IntProperty swingSpeedWhileBlocking = new IntProperty("Swing speed while blocking", 0, -200, 50); // Colocar um if
    //translation
    private final FloatProperty translatex = new FloatProperty("X", 0f, -4f, 4f);
    private final FloatProperty translatey = new FloatProperty("Y", 0f, -2f, 2f);
    private final FloatProperty translatez = new FloatProperty("Z", 0f, -10f, 10f);

    private final BooleanProperty precustomtranslation = new BooleanProperty("Custom Translation (pre)", false); // esse if
    private final FloatProperty pretranslatex = new FloatProperty("Pre-X", 0f, -4f, 4f); // Colocar um if
    private final FloatProperty pretranslatey = new FloatProperty("Pre-Y", 0f, -2f, 2f); // colocar um if
    private final FloatProperty pretranslatez = new FloatProperty("Pre-Z", 0f, -6f, 3f); // colocar um if

    private final BooleanProperty customscaling = new BooleanProperty("Custom Scaling", false);
    private final FloatProperty scalex = new FloatProperty("ScaleX", 1f, -1f, 5f); // colocar um if
    private final FloatProperty scaley = new FloatProperty("ScaleY", 1f, -1f, 5f); // colocar um if
    private final FloatProperty scalez = new FloatProperty("ScaleZ", 1f, -1f, 5f); // colocar um if

    private final BooleanProperty customrotation = new BooleanProperty("Custom Rotation", false);
    private final IntProperty rotatex = new IntProperty("rotation x", 0, -180, 180); // colocar um if
    private final IntProperty rotatey = new IntProperty("rotation y", 0, -180, 180); // colocar um if
    private final IntProperty rotatez = new IntProperty("rotation z", 0, -180, 180); // colocar um if

    private void blockTransformation(ItemRenderer renderer) {
        // Aqui fazemos o cast duplo (Double Cast) para o erro sumir
        ItemRendererAccessor acc = (ItemRendererAccessor) (Object) renderer;
        acc.invokeTransformFirstPersonItem(1.0F, 1.0F);
    }

    public Animations() {
        super("Animations", false);
        // "Animations" é o nome do módulo
        // false provavelmente indica se ele começa ativado ou não
    }


    private int swing;

    private static final double staticscalemultiplier_x = 1;
    private static final double staticscalemultiplier_y = 1;
    private static final double staticscalemultiplier_z = 1;
    float staticStartSwingProgressFloat;

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (Utils.nullCheck()
                && swingWhileDigging.getValue()
                && clientSide.getValue()
                && event.getPacket() instanceof C0APacketAnimation
                && mc.thePlayer.isUsingItem()
        )
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (Utils.nullCheck()
                && fakeSlotReset.getValue()
        ) {
            mc.getItemRenderer().resetEquippedProgress();
        }
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @SubscribeEvent
    public void onRenderItem(@NotNull RenderItemEvent event) {
        try {
            if (event.getItemToRender().getItem() instanceof ItemMap) {
                return;
            }

            final EnumAction itemAction = event.getEnumAction();
            final ItemRenderer itemRenderer = mc.getItemRenderer();
            final ItemRendererAccessor acc = (ItemRendererAccessor) itemRenderer;
            final float animationProgression = event.getAnimationProgression();
            float swingProgress = event.getSwingProgress();
            final float convertedProgress = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);


            if (modifyAnimations.getValue()) {
                staticStartSwingProgressFloat = ((float) staticStartSwingProgress.getValue());
            } else {
                staticStartSwingProgressFloat = 0.0F;
            }

            if (precustomtranslation.getValue()) {
                this.pretranslate(pretranslatex.getValue(), pretranslatey.getValue(), pretranslatez.getValue());

            }

            if (customrotation.getValue()) {
                this.rotate((float) rotatex.getValue(), (float) rotatey.getValue(), (float) rotatez.getValue());

            }

            if (customscaling.getValue()) {
                this.scale(1, 1, 1);
            }


            this.translate(translatex.getValue(), translatey.getValue(), translatez.getValue());


            if (event.isUseItem()) {
                switch (itemAction) {
                    case NONE:
                        switch ((int) otherAnimation.getValue()) {
                            case 0:
                                //none
                                acc.invokeTransformFirstPersonItem(animationProgression, swingProgress);
                                break;
                            case 1:
                                //1.7
                                acc.invokeTransformFirstPersonItem(animationProgression, swingProgress);
                                break;
                        }
                        break;
                    case BLOCK:
                        switch ((int) blockAnimation.getValue()) {
                            case 0:
                                //none
                                acc.invokeTransformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                this.blockTransformation(itemRenderer);
                                break;

                            case 1:
                                //1.7
                                acc.invokeTransformFirstPersonItem(animationProgression, swingProgress);
                                this.blockTransformation(itemRenderer);
                                break;

                            case 2:
                                //smooth
                                acc.invokeTransformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                final float y = -convertedProgress * 2.0F;
                                this.translate(0.0F, y / 10.0F + 0.1F, 0.0F);
                                GlStateManager.rotate(y * 10.0F, 0.0F, 1.0F, 0.0F);
                                GlStateManager.rotate(250, 0.2F, 1.0F, -0.6F);
                                GlStateManager.rotate(-10.0F, 1.0F, 0.5F, 1.0F);
                                GlStateManager.rotate(-y * 20.0F, 1.0F, 0.5F, 1.0F);
                                break;

                            case 3:
                                //exhibition
                                acc.invokeTransformFirstPersonItem(animationProgression / 2.0F, staticStartSwingProgressFloat);
                                this.translate(0.0F, 0.3F, -0.0F);
                                GlStateManager.rotate(-convertedProgress * 31.0F, 1.0F, 0.0F, 2.0F);
                                GlStateManager.rotate(-convertedProgress * 33.0F, 1.5F, (convertedProgress / 1.1F), 0.0F);
                                this.blockTransformation(itemRenderer);
                                break;

                            case 4:
                                //stab
                                final float spin = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);

                                this.translate(0.6f, 0.3f, -0.6f + -spin * 0.7);
                                GlStateManager.rotate(6090, 0.0f, 0.0f, 0.1f);
                                GlStateManager.rotate(6085, 0.0f, 0.1f, 0.0f);
                                GlStateManager.rotate(6110, 0.1f, 0.0f, 0.0f);
                                acc.invokeTransformFirstPersonItem(0.0F, 0.0f);
                                this.blockTransformation(itemRenderer);
                                break;

                            case 5:
                                //spin
                                acc.invokeTransformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                this.translate(0, 0.2F, -1);
                                GlStateManager.rotate(-59, -1, 0, 3);
                                // Don't cast as float
                                GlStateManager.rotate(-(System.currentTimeMillis() / 2 % 360), 1, 0, 0.0F);
                                GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
                                break;

                            case 6:
                                //sigma
                                acc.invokeTransformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                this.translate(0.0f, 0.1F, 0.0F);
                                this.blockTransformation(itemRenderer);
                                GlStateManager.rotate(convertedProgress * 35.0F / 2.0F, 0.0F, 1.0F, 1.5F);
                                GlStateManager.rotate(-convertedProgress * 135.0F / 4.0F, 1.0f, 1.0F, 0.0F);
                                break;

                            case 7:
                                //wood
                                acc.invokeTransformFirstPersonItem(animationProgression / 2.0F, staticStartSwingProgressFloat);
                                this.translate(0.0F, 0.3F, -0.0F);
                                GlStateManager.rotate(-convertedProgress * 30.0F, 1.0F, 0.0F, 2.0F);
                                GlStateManager.rotate(-convertedProgress * 44.0F, 1.5F, (convertedProgress / 1.2F), 0.0F);
                                this.blockTransformation(itemRenderer);

                                break;

                            case 8:
                                //swong
                                acc.invokeTransformFirstPersonItem(animationProgression / 2.0F, swingProgress);
                                GlStateManager.rotate(convertedProgress * 30.0F / 2.0F, -convertedProgress, -0.0F, 9.0F);
                                GlStateManager.rotate(convertedProgress * 40.0F, 1.0F, -convertedProgress / 2.0F, -0.0F);
                                this.translate(0.0F, 0.2F, 0.0F);
                                this.blockTransformation(itemRenderer);

                                break;

                            case 9:
                                //chill
                                acc.invokeTransformFirstPersonItem(-0.25F, 1.0F + convertedProgress / 10.0F);
                                GL11.glRotated(-convertedProgress * 25.0F, 1.0F, 0.0F, 0.0F);
                                this.blockTransformation(itemRenderer);

                                break;

                            case 10:
                                //komorebi
                                this.translate(0.41F, -0.25F, -0.5555557F);
                                this.translate(0.0F, 0, 0.0F);
                                GlStateManager.rotate(35.0F, 0f, 1.5F, 0.0F);

                                final float racism = MathHelper.sin(swingProgress * swingProgress / 64 * (float) Math.PI);

                                GlStateManager.rotate(racism * -5.0F, 0.0F, 0.0F, 0.0F);
                                GlStateManager.rotate(convertedProgress * -12.0F, 0.0F, 0.0F, 1.0F);
                                GlStateManager.rotate(convertedProgress * -65.0F, 1.0F, 0.0F, 0.0F);
                                this.blockTransformation(itemRenderer);

                                break;

                            case 11:
                                //rhys
                                acc.invokeTransformFirstPersonItem(animationProgression, swingProgress);
                                this.blockTransformation(itemRenderer);
                                this.translate(-0.3F, -0.1F, -0.0F);
                                break;

                            case 12:
                                //Allah
                                acc.invokeTransformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                this.blockTransformation(itemRenderer);
                                break;
                        }
                        break;
                    case EAT:
                    case DRINK:
                        switch ((int) otherAnimation.getValue()) {
                            case 0:
                                //none
                                func_178104_a(mc.thePlayer.getHeldItem(), mc.thePlayer, event.getPartialTicks());
                                acc.invokeTransformFirstPersonItem(animationProgression, 0.0F);
                                break;
                            case 1:
                                //1.7
                                func_178104_a(mc.thePlayer.getHeldItem(), mc.thePlayer, event.getPartialTicks());
                                acc.invokeTransformFirstPersonItem(animationProgression, swingProgress);
                                break;
                        }
                        break;
                    case BOW:
                        switch ((int) otherAnimation.getValue()) {
                            case 0:
                                //none
                                acc.invokeTransformFirstPersonItem(animationProgression, 0.0F);
                                func_178098_a(mc.thePlayer.getHeldItem(), event.getPartialTicks(), mc.thePlayer);
                                break;
                            case 1:
                                //1.7
                                acc.invokeTransformFirstPersonItem(animationProgression, swingProgress);
                                func_178098_a(mc.thePlayer.getHeldItem(), event.getPartialTicks(), mc.thePlayer);
                                break;
                        }
                        break;
                }

                event.setCanceled(true);

            } else {
                switch ((int) swingAnimation.getValue()) {
                    case 0:
                        //none
                        func_178105_d(swingProgress);
                        acc.invokeTransformFirstPersonItem(animationProgression, swingProgress);
                        break;

                    case 1:
                        //1.9+
                        func_178105_d(swingProgress);
                        acc.invokeTransformFirstPersonItem(animationProgression, swingProgress);
                        this.translate(0, -((swing - 1) -
                                (swing == 0 ? 0 : Utils.getTimer().renderPartialTicks)) / 5f, 0);
                        break;

                    case 2:
                        //smooth
                        acc.invokeTransformFirstPersonItem(animationProgression, swingProgress);
                        func_178105_d(animationProgression);
                        break;

                    case 3:
                        //punch
                        acc.invokeTransformFirstPersonItem(animationProgression, swingProgress);
                        func_178105_d(swingProgress);
                        break;

                    case 4:
                        //shove
                        acc.invokeTransformFirstPersonItem(animationProgression, animationProgression);
                        func_178105_d(swingProgress);
                        break;
                }

                event.setCanceled(true);
            }
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        try {
            if (mc.thePlayer.swingProgressInt == 1) {
                swing = 9;
            } else {
                swing = Math.max(0, swing - 1);
            }
        } catch (Exception ignore) {
        }
    }

    @SubscribeEvent
    public void onSwingAnimation(@NotNull SwingAnimationEvent event) {

        if ((mc.thePlayer.getItemInUseCount() == 1 || mc.thePlayer.isUsingItem()) && modifyAnimations.getValue()) {
            event.setAnimationEnd((int) (event.getAnimationEnd() * ((-swingSpeedWhileBlocking.getValue() / 100) + 1)));
        } else {
            event.setAnimationEnd((int) (event.getAnimationEnd() * ((-swingSpeed.getValue() / 100) + 1)));
        }
    }


    private void translate(double x, double y, double z) {
        GlStateManager.translate(
                x + this.translatex.getValue(),
                y + this.translatey.getValue(),
                z + this.translatez.getValue()
        );
    }

    private void pretranslate(double x, double y, double z) {
        GlStateManager.translate(
                x + this.pretranslatex.getValue(),
                y + this.pretranslatey.getValue(),
                z + this.pretranslatez.getValue()
        );
    }

    private void scale(double staticscalemultiplier_x, double staticscalemultiplier_y, double staticscalemultiplier_z) {
        GlStateManager.scale(
                staticscalemultiplier_x * this.scalex.getValue(),
                staticscalemultiplier_y * this.scaley.getValue(),
                staticscalemultiplier_z * this.scalez.getValue()
        );
    }

    private void rotate(float rotatex, float rotatey, float rotatez) {
        //x rotation
        GlStateManager.rotate(
                (float) this.rotatex.getValue(),
                1,
                0,
                0
        );

        //y rotation
        GlStateManager.rotate(
                (float) this.rotatey.getValue(),
                0,
                1,
                0
        );

        //z rotation
        GlStateManager.rotate(
                (float) this.rotatez.getValue(),
                0,
                0,
                1
        );
    }

    /**
     * //* @see net.minecraft.client.renderer.ItemRenderer#func_178105_d(float swingProgress)
     */
    private void func_178105_d(float swingProgress) {
        float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
        float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F * 2.0F);
        float f2 = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
        this.translate(f, f1, f2);
    }

    /**
     * //* @see net.minecraft.client.renderer.ItemRenderer#func_178104_a(AbstractClientPlayer player, float swingProgress)
     */
    private void func_178104_a(ItemStack itemToRender, @NotNull AbstractClientPlayer p_178104_1_, float p_178104_2_) {
        if (itemToRender == null) return;

        float f = (float) p_178104_1_.getItemInUseCount() - p_178104_2_ + 1.0F;
        float f1 = f / (float) itemToRender.getMaxItemUseDuration();
        float f2 = MathHelper.abs(MathHelper.cos(f / 4.0F * 3.1415927F) * 0.1F);
        if (f1 >= 0.8F) {
            f2 = 0.0F;
        }

        this.translate(0.0F, f2, 0.0F);
        float f3 = 1.0F - (float) Math.pow(f1, 27.0);
        this.translate(f3 * 0.6F, f3 * -0.5F, f3 * 0.0F);
        GlStateManager.rotate(f3 * 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f3 * 10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f3 * 30.0F, 0.0F, 0.0F, 1.0F);
    }

    /**
     * //* @see net.minecraft.client.renderer.ItemRenderer#func_178098_a(float, AbstractClientPlayer)
     */
    private void func_178098_a(@NotNull ItemStack itemToRender, float p_178098_1_, @NotNull AbstractClientPlayer p_178098_2_) {
        GlStateManager.rotate(-18.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-12.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-8.0F, 1.0F, 0.0F, 0.0F);
        this.translate(-0.9F, 0.2F, 0.0F);
        float f = (float) itemToRender.getMaxItemUseDuration() - ((float) p_178098_2_.getItemInUseCount() - p_178098_1_ + 1.0F);
        float f1 = f / 20.0F;
        f1 = (f1 * f1 + f1 * 2.0F) / 3.0F;
        if (f1 > 1.0F) {
            f1 = 1.0F;
        }

        if (f1 > 0.1F) {
            float f2 = MathHelper.sin((f - 0.1F) * 1.3F);
            float f3 = f1 - 0.1F;
            float f4 = f2 * f3;
            this.translate(f4 * 0.0F, f4 * 0.01F, f4 * 0.0F);
        }

        this.translate(f1 * 0.0F, f1 * 0.0F, f1 * 0.1F);
        GlStateManager.scale(1.0F, 1.0F, 1.0F + f1 * 0.2F);
    }
}
