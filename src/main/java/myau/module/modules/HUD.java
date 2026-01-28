/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiChat
 *  net.minecraft.client.gui.ScaledResolution
 *  net.minecraft.client.renderer.GlStateManager
 */
package myau.module.modules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import myau.Myau;
import myau.enums.BlinkModules;
import myau.enums.ChatColors;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.Render2DEvent;
import myau.events.TickEvent;
import myau.mixin.IAccessorGuiChat;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ColorProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.ColorUtil;
import myau.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class HUD
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private List<Module> activeModules = new ArrayList<Module>();
    public final ModeProperty colorMode = new ModeProperty("color", 3, new String[]{"RAINBOW", "CHROMA", "ASTOLFO", "CUSTOM1", "CUSTOM12", "CUSTOM123"});
    public final FloatProperty colorSpeed = new FloatProperty("color-speed", Float.valueOf(1.0f), Float.valueOf(0.5f), Float.valueOf(1.5f));
    public final PercentProperty colorSaturation = new PercentProperty("color-saturation", 50);
    public final PercentProperty colorBrightness = new PercentProperty("color-brightness", 100);
    public final ColorProperty custom1 = new ColorProperty("custom-color-1", Color.WHITE.getRGB(), () -> (Integer)this.colorMode.getValue() == 3 || (Integer)this.colorMode.getValue() == 4 || (Integer)this.colorMode.getValue() == 5);
    public final ColorProperty custom2 = new ColorProperty("custom-color-2", Color.WHITE.getRGB(), () -> (Integer)this.colorMode.getValue() == 4 || (Integer)this.colorMode.getValue() == 5);
    public final ColorProperty custom3 = new ColorProperty("custom-color-3", Color.WHITE.getRGB(), () -> (Integer)this.colorMode.getValue() == 5);
    public final ModeProperty posX = new ModeProperty("position-x", 0, new String[]{"LEFT", "RIGHT"});
    public final ModeProperty posY = new ModeProperty("position-y", 0, new String[]{"TOP", "BOTTOM"});
    public final IntProperty offsetX = new IntProperty("offset-x", 2, 0, 255);
    public final IntProperty offsetY = new IntProperty("offset-y", 2, 0, 255);
    public final FloatProperty scale = new FloatProperty("scale", Float.valueOf(1.0f), Float.valueOf(0.5f), Float.valueOf(1.5f));
    public final PercentProperty background = new PercentProperty("background", 25);
    public final BooleanProperty showBar = new BooleanProperty("bar", true);
    public final BooleanProperty shadow = new BooleanProperty("shadow", true);
    public final BooleanProperty suffixes = new BooleanProperty("suffixes", true);
    public final BooleanProperty lowerCase = new BooleanProperty("lower-case", false);
    public final BooleanProperty chatOutline = new BooleanProperty("chat-outline", true);
    public final BooleanProperty blinkTimer = new BooleanProperty("blink-timer", true);
    public final BooleanProperty toggleSound = new BooleanProperty("toggle-sounds", true);
    public final BooleanProperty toggleAlerts = new BooleanProperty("toggle-alerts", false);

    private String getModuleName(Module module) {
        String moduleName = module.getName();
        if (((Boolean)this.lowerCase.getValue()).booleanValue()) {
            moduleName = moduleName.toLowerCase(Locale.ROOT);
        }
        return moduleName;
    }

    private String[] getModuleSuffix(Module module) {
        String[] moduleSuffix = module.getSuffix();
        if (((Boolean)this.lowerCase.getValue()).booleanValue()) {
            for (int i = 0; i < moduleSuffix.length; ++i) {
                moduleSuffix[i] = moduleSuffix[i].toLowerCase();
            }
        }
        return moduleSuffix;
    }

    private int getModuleWidth(Module module) {
        return this.calculateStringWidth(this.getModuleName(module), this.getModuleSuffix(module));
    }

    private int calculateStringWidth(String string, String[] arr) {
        int width = HUD.mc.field_71466_p.func_78256_a(string);
        if (((Boolean)this.suffixes.getValue()).booleanValue()) {
            for (String str : arr) {
                width += 3 + HUD.mc.field_71466_p.func_78256_a(str);
            }
        }
        return width;
    }

    private float getColorCycle(long long3, long long4) {
        long speed = (long)(3000.0 / Math.pow(Math.min(Math.max(0.5f, ((Float)this.colorSpeed.getValue()).floatValue()), 1.5f), 3.0));
        return 1.0f - (float)(Math.abs(long3 - long4 * 300L) % speed) / (float)speed;
    }

    public HUD() {
        super("HUD", true, true);
    }

    public Color getColor(long time) {
        return this.getColor(time, 0L);
    }

    public Color getColor(long time, long offset) {
        Color color = Color.white;
        switch ((Integer)this.colorMode.getValue()) {
            case 0: {
                color = ColorUtil.fromHSB(this.getColorCycle(time, offset), 1.0f, 1.0f);
                break;
            }
            case 1: {
                color = ColorUtil.fromHSB(this.getColorCycle(time / 3L, 0L), 1.0f, 1.0f);
                break;
            }
            case 2: {
                float cycle = this.getColorCycle(time, offset);
                if (cycle % 1.0f < 0.5f) {
                    cycle = 1.0f - cycle % 1.0f;
                }
                color = ColorUtil.fromHSB(cycle, 1.0f, 1.0f);
                break;
            }
            case 3: {
                color = new Color((Integer)this.custom1.getValue());
                break;
            }
            case 4: {
                double cycle1 = this.getColorCycle(time, offset);
                color = ColorUtil.interpolate((float)(2.0 * Math.abs(cycle1 - Math.floor(cycle1 + 0.5))), new Color((Integer)this.custom1.getValue()), new Color((Integer)this.custom2.getValue()));
                break;
            }
            case 5: {
                double cycle2 = this.getColorCycle(time, offset);
                float floor = (float)(2.0 * Math.abs(cycle2 - Math.floor(cycle2 + 0.5)));
                color = floor <= 0.5f ? ColorUtil.interpolate(floor * 2.0f, new Color((Integer)this.custom1.getValue()), new Color((Integer)this.custom2.getValue())) : ColorUtil.interpolate((floor - 0.5f) * 2.0f, new Color((Integer)this.custom2.getValue()), new Color((Integer)this.custom3.getValue()));
            }
        }
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(hsb[0], hsb[1] * (((Integer)this.colorSaturation.getValue()).floatValue() / 100.0f), hsb[2] * (((Integer)this.colorBrightness.getValue()).floatValue() / 100.0f));
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.POST) {
            this.activeModules = Myau.moduleManager.modules.values().stream().filter(module -> module.isEnabled() && !module.isHidden()).sorted(Comparator.comparingInt(this::getModuleWidth).reversed()).collect(Collectors.toList());
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (((Boolean)this.chatOutline.getValue()).booleanValue() && HUD.mc.field_71462_r instanceof GuiChat) {
            String text = ((IAccessorGuiChat)HUD.mc.field_71462_r).getInputField().func_146179_b().trim();
            if (Myau.commandManager != null && Myau.commandManager.isTypingCommand(text)) {
                RenderUtil.enableRenderState();
                RenderUtil.drawOutlineRect(2.0f, HUD.mc.field_71462_r.field_146295_m - 14, HUD.mc.field_71462_r.field_146294_l - 2, HUD.mc.field_71462_r.field_146295_m - 2, 1.5f, 0, this.getColor(System.currentTimeMillis()).getRGB());
                RenderUtil.disableRenderState();
            }
        }
        if (this.isEnabled() && !HUD.mc.field_71474_y.field_74330_P) {
            long movementPacketSize;
            BlinkModules blinkingModule;
            float height = (float)HUD.mc.field_71466_p.field_78288_b - 1.0f;
            float x = (float)((Integer)this.offsetX.getValue()).intValue() + (1.0f + (((Boolean)this.showBar.getValue()).booleanValue() ? (((Boolean)this.shadow.getValue()).booleanValue() ? 2.0f : 1.0f) : 0.0f)) * ((Float)this.scale.getValue()).floatValue();
            float y = (float)((Integer)this.offsetY.getValue()).intValue() + 1.0f * ((Float)this.scale.getValue()).floatValue();
            if ((Integer)this.posX.getValue() == 1) {
                x = (float)new ScaledResolution(mc).func_78326_a() - x;
            }
            if ((Integer)this.posY.getValue() == 1) {
                y = (float)new ScaledResolution(mc).func_78328_b() - y - height * ((Float)this.scale.getValue()).floatValue();
            }
            GlStateManager.func_179094_E();
            GlStateManager.func_179152_a((float)((Float)this.scale.getValue()).floatValue(), (float)((Float)this.scale.getValue()).floatValue(), (float)0.0f);
            long l = System.currentTimeMillis();
            long offset = 0L;
            for (Module module : this.activeModules) {
                String moduleName = this.getModuleName(module);
                String[] moduleSuffix = this.getModuleSuffix(module);
                float totalWidth = this.calculateStringWidth(moduleName, moduleSuffix) - ((Boolean)this.shadow.getValue() != false ? 0 : 1);
                int color = this.getColor(l, offset).getRGB();
                RenderUtil.enableRenderState();
                if ((Integer)this.background.getValue() > 0) {
                    RenderUtil.drawRect(x / ((Float)this.scale.getValue()).floatValue() - 1.0f - ((Integer)this.posX.getValue() == 0 ? 0.0f : totalWidth), y / ((Float)this.scale.getValue()).floatValue() - ((Integer)this.posY.getValue() == 0 ? (offset == 0L ? 1.0f : 0.0f) : ((Boolean)this.shadow.getValue() != false ? 1.0f : 0.0f)), x / ((Float)this.scale.getValue()).floatValue() + 1.0f + ((Integer)this.posX.getValue() == 0 ? totalWidth : 0.0f), y / ((Float)this.scale.getValue()).floatValue() + height + ((Integer)this.posY.getValue() == 0 ? (((Boolean)this.shadow.getValue()).booleanValue() ? 1.0f : 0.0f) : (offset == 0L ? 1.0f : 0.0f)), new Color(0.0f, 0.0f, 0.0f, ((Integer)this.background.getValue()).floatValue() / 100.0f).getRGB());
                }
                if (((Boolean)this.showBar.getValue()).booleanValue()) {
                    if (((Boolean)this.shadow.getValue()).booleanValue()) {
                        RenderUtil.drawRect(x / ((Float)this.scale.getValue()).floatValue() + ((Integer)this.posX.getValue() == 0 ? -3.0f : 1.0f), y / ((Float)this.scale.getValue()).floatValue() - ((Integer)this.posY.getValue() == 0 ? (offset == 0L ? 1.0f : 0.0f) : 1.0f), x / ((Float)this.scale.getValue()).floatValue() + ((Integer)this.posX.getValue() == 0 ? -2.0f : 2.0f), y / ((Float)this.scale.getValue()).floatValue() + height + ((Integer)this.posY.getValue() == 0 ? 1.0f : (offset == 0L ? 1.0f : 0.0f)), color);
                        RenderUtil.drawRect(x / ((Float)this.scale.getValue()).floatValue() + ((Integer)this.posX.getValue() == 0 ? -2.0f : 2.0f), y / ((Float)this.scale.getValue()).floatValue() - ((Integer)this.posY.getValue() == 0 ? (offset == 0L ? 1.0f : 0.0f) : 1.0f), x / ((Float)this.scale.getValue()).floatValue() + ((Integer)this.posX.getValue() == 0 ? -1.0f : 3.0f), y / ((Float)this.scale.getValue()).floatValue() + height + ((Integer)this.posY.getValue() == 0 ? 1.0f : (offset == 0L ? 1.0f : 0.0f)), (color & 0xFCFCFC) >> 2 | color & 0xFF000000);
                    } else {
                        RenderUtil.drawRect(x / ((Float)this.scale.getValue()).floatValue() + ((Integer)this.posX.getValue() == 0 ? -2.0f : 1.0f), y / ((Float)this.scale.getValue()).floatValue() - ((Integer)this.posY.getValue() == 0 ? (offset == 0L ? 1.0f : 0.0f) : 0.0f), x / ((Float)this.scale.getValue()).floatValue() + ((Integer)this.posX.getValue() == 0 ? -1.0f : 2.0f), y / ((Float)this.scale.getValue()).floatValue() + height + ((Integer)this.posY.getValue() == 0 ? 0.0f : (offset == 0L ? 1.0f : 0.0f)), color);
                    }
                }
                RenderUtil.disableRenderState();
                GlStateManager.func_179097_i();
                if (((Boolean)this.shadow.getValue()).booleanValue()) {
                    HUD.mc.field_71466_p.func_175063_a(moduleName, x / ((Float)this.scale.getValue()).floatValue() - ((Integer)this.posX.getValue() == 1 ? totalWidth : 0.0f), y / ((Float)this.scale.getValue()).floatValue(), color);
                } else {
                    HUD.mc.field_71466_p.func_175065_a(moduleName, x / ((Float)this.scale.getValue()).floatValue() - ((Integer)this.posX.getValue() == 1 ? totalWidth : 0.0f), y / ((Float)this.scale.getValue()).floatValue() + ((Integer)this.posY.getValue() == 1 ? 1.0f : 0.0f), color, false);
                }
                if (((Boolean)this.suffixes.getValue()).booleanValue() && moduleSuffix.length > 0) {
                    float width = (float)HUD.mc.field_71466_p.func_78256_a(moduleName) + 3.0f;
                    for (String string : moduleSuffix) {
                        if (((Boolean)this.shadow.getValue()).booleanValue()) {
                            HUD.mc.field_71466_p.func_175063_a(string, x / ((Float)this.scale.getValue()).floatValue() - ((Integer)this.posX.getValue() == 1 ? totalWidth : 0.0f) + width, y / ((Float)this.scale.getValue()).floatValue(), ChatColors.GRAY.toAwtColor());
                        } else {
                            HUD.mc.field_71466_p.func_175065_a(string, x / ((Float)this.scale.getValue()).floatValue() - ((Integer)this.posX.getValue() == 1 ? totalWidth : 0.0f) + width, y / ((Float)this.scale.getValue()).floatValue() + ((Integer)this.posY.getValue() == 1 ? 1.0f : 0.0f), ChatColors.GRAY.toAwtColor(), false);
                        }
                        width += (float)HUD.mc.field_71466_p.func_78256_a(string) + ((Boolean)this.shadow.getValue() != false ? 3.0f : 2.0f);
                    }
                }
                y += (height + ((Boolean)this.shadow.getValue() != false ? 1.0f : 0.0f)) * ((Float)this.scale.getValue()).floatValue() * ((Integer)this.posY.getValue() == 0 ? 1.0f : -1.0f);
                ++offset;
            }
            if (((Boolean)this.blinkTimer.getValue()).booleanValue() && (blinkingModule = Myau.blinkManager.getBlinkingModule()) != BlinkModules.NONE && blinkingModule != BlinkModules.AUTO_BLOCK && (movementPacketSize = Myau.blinkManager.countMovement()) > 0L) {
                GlStateManager.func_179147_l();
                GlStateManager.func_179112_b((int)770, (int)771);
                HUD.mc.field_71466_p.func_175065_a(String.valueOf(movementPacketSize), (float)new ScaledResolution(mc).func_78326_a() / 2.0f / ((Float)this.scale.getValue()).floatValue() - (float)HUD.mc.field_71466_p.func_78256_a(String.valueOf(movementPacketSize)) / 2.0f, (float)new ScaledResolution(mc).func_78328_b() / 5.0f * 3.0f / ((Float)this.scale.getValue()).floatValue(), this.getColor(l, offset).getRGB() & 0xFFFFFF | 0xBF000000, ((Boolean)this.shadow.getValue()).booleanValue());
                GlStateManager.func_179084_k();
            }
            GlStateManager.func_179126_j();
            GlStateManager.func_179121_F();
        }
    }
}

