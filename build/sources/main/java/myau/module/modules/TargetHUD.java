/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.GuiChat
 *  net.minecraft.client.gui.ScaledResolution
 *  net.minecraft.client.network.NetworkPlayerInfo
 *  net.minecraft.client.renderer.GlStateManager
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.item.EntityArmorStand
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.network.play.client.C02PacketUseEntity
 *  net.minecraft.network.play.client.C02PacketUseEntity$Action
 *  net.minecraft.util.ResourceLocation
 *  net.minecraft.world.World
 */
package myau.module.modules;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import myau.Myau;
import myau.enums.ChatColors;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.PacketEvent;
import myau.events.Render2DEvent;
import myau.module.Module;
import myau.module.modules.HUD;
import myau.module.modules.KillAura;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.ColorUtil;
import myau.util.RenderUtil;
import myau.util.TeamUtil;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class TargetHUD
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private static final DecimalFormat healthFormat = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
    private static final DecimalFormat diffFormat = new DecimalFormat("+0.0;-0.0", new DecimalFormatSymbols(Locale.US));
    private final TimerUtil lastAttackTimer = new TimerUtil();
    private final TimerUtil animTimer = new TimerUtil();
    private EntityLivingBase lastTarget = null;
    private EntityLivingBase target = null;
    private ResourceLocation headTexture = null;
    private float oldHealth = 0.0f;
    private float newHealth = 0.0f;
    private float maxHealth = 0.0f;
    public final ModeProperty color = new ModeProperty("color", 0, new String[]{"DEFAULT", "HUD"});
    public final ModeProperty posX = new ModeProperty("position-x", 1, new String[]{"LEFT", "MIDDLE", "RIGHT"});
    public final ModeProperty posY = new ModeProperty("position-y", 1, new String[]{"TOP", "MIDDLE", "BOTTOM"});
    public final FloatProperty scale = new FloatProperty("scale", Float.valueOf(1.0f), Float.valueOf(0.5f), Float.valueOf(1.5f));
    public final IntProperty offX = new IntProperty("offset-x", 0, -255, 255);
    public final IntProperty offY = new IntProperty("offset-y", 40, -255, 255);
    public final PercentProperty background = new PercentProperty("background", 25);
    public final BooleanProperty head = new BooleanProperty("head", true);
    public final BooleanProperty indicator = new BooleanProperty("indicator", true);
    public final BooleanProperty outline = new BooleanProperty("outline", false);
    public final BooleanProperty animations = new BooleanProperty("animations", true);
    public final BooleanProperty shadow = new BooleanProperty("shadow", true);
    public final BooleanProperty kaOnly = new BooleanProperty("ka-only", true);
    public final BooleanProperty chatPreview = new BooleanProperty("chat-preview", false);

    private EntityLivingBase resolveTarget() {
        KillAura killAura = (KillAura)Myau.moduleManager.modules.get(KillAura.class);
        if (killAura.isEnabled() && killAura.isAttackAllowed() && TeamUtil.isEntityLoaded((Entity)killAura.getTarget())) {
            return killAura.getTarget();
        }
        if (!((Boolean)this.kaOnly.getValue()).booleanValue() && !this.lastAttackTimer.hasTimeElapsed(1500L) && TeamUtil.isEntityLoaded((Entity)this.lastTarget)) {
            return this.lastTarget;
        }
        return (Boolean)this.chatPreview.getValue() != false && TargetHUD.mc.field_71462_r instanceof GuiChat ? TargetHUD.mc.field_71439_g : null;
    }

    private ResourceLocation getSkin(EntityLivingBase entityLivingBase) {
        NetworkPlayerInfo playerInfo;
        if (entityLivingBase instanceof EntityPlayer && (playerInfo = mc.func_147114_u().func_175104_a(entityLivingBase.func_70005_c_())) != null) {
            return playerInfo.func_178837_g();
        }
        return null;
    }

    private Color getTargetColor(EntityLivingBase entityLivingBase) {
        if (entityLivingBase instanceof EntityPlayer) {
            if (TeamUtil.isFriend((EntityPlayer)entityLivingBase)) {
                return Myau.friendManager.getColor();
            }
            if (TeamUtil.isTarget((EntityPlayer)entityLivingBase)) {
                return Myau.targetManager.getColor();
            }
        }
        switch ((Integer)this.color.getValue()) {
            case 0: {
                if (!(entityLivingBase instanceof EntityPlayer)) {
                    return new Color(-1);
                }
                return TeamUtil.getTeamColor((EntityPlayer)entityLivingBase, 1.0f);
            }
            case 1: {
                int rgb = ((HUD)Myau.moduleManager.modules.get(HUD.class)).getColor(System.currentTimeMillis()).getRGB();
                return new Color(rgb);
            }
        }
        return new Color(-1);
    }

    public TargetHUD() {
        super("TargetHUD", false, true);
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (this.isEnabled() && TargetHUD.mc.field_71439_g != null) {
            EntityLivingBase entityLivingBase = this.target;
            this.target = this.resolveTarget();
            if (this.target != null) {
                ResourceLocation resourceLocation;
                float health = (TargetHUD.mc.field_71439_g.func_110143_aJ() + TargetHUD.mc.field_71439_g.func_110139_bj()) / 2.0f;
                float abs = this.target.func_110139_bj() / 2.0f;
                float heal = this.target.func_110143_aJ() / 2.0f + abs;
                if (this.target != entityLivingBase) {
                    this.headTexture = null;
                    this.animTimer.setTime();
                    this.oldHealth = heal;
                    this.newHealth = heal;
                }
                if (!((Boolean)this.animations.getValue()).booleanValue() || this.animTimer.hasTimeElapsed(150L)) {
                    this.oldHealth = this.newHealth;
                    this.newHealth = heal;
                    this.maxHealth = this.target.func_110138_aP() / 2.0f;
                    if (this.oldHealth != this.newHealth) {
                        this.animTimer.reset();
                    }
                }
                if ((resourceLocation = this.getSkin(this.target)) != null) {
                    this.headTexture = resourceLocation;
                }
                float elapsedTime = Math.min(Math.max(this.animTimer.getElapsedTime(), 0L), 150L);
                float healthRatio = Math.min(Math.max(RenderUtil.lerpFloat(this.newHealth, this.oldHealth, elapsedTime / 150.0f) / this.maxHealth, 0.0f), 1.0f);
                Color targetColor = this.getTargetColor(this.target);
                Color healthBarColor = (Integer)this.color.getValue() == 0 ? ColorUtil.getHealthBlend(healthRatio) : targetColor;
                float healthDeltaRatio = Math.min(Math.max((health - heal + 1.0f) / 2.0f, 0.0f), 1.0f);
                Color healthDeltaColor = ColorUtil.getHealthBlend(healthDeltaRatio);
                ScaledResolution scaledResolution = new ScaledResolution(mc);
                String targetNameText = ChatColors.formatColor(String.format("&r%s&r", TeamUtil.stripName((Entity)this.target)));
                int targetNameWidth = TargetHUD.mc.field_71466_p.func_78256_a(targetNameText);
                String healthText = ChatColors.formatColor(String.format("&r&f%s%s\u2764&r", healthFormat.format(heal), abs > 0.0f ? "&6" : "&c"));
                int healthTextWidth = TargetHUD.mc.field_71466_p.func_78256_a(healthText);
                String statusText = ChatColors.formatColor(String.format("&r&l%s&r", heal == health ? "D" : (heal < health ? "W" : "L")));
                int statusTextWidth = TargetHUD.mc.field_71466_p.func_78256_a(statusText);
                String healthDiffText = ChatColors.formatColor(String.format("&r%s&r", heal == health ? "0.0" : diffFormat.format(health - heal)));
                int healthDiffWidth = TargetHUD.mc.field_71466_p.func_78256_a(healthDiffText);
                float barContentWidth = Math.max((float)targetNameWidth + ((Boolean)this.indicator.getValue() != false ? 2.0f + (float)statusTextWidth + 2.0f : 0.0f), (float)healthTextWidth + ((Boolean)this.indicator.getValue() != false ? 2.0f + (float)healthDiffWidth + 2.0f : 0.0f));
                float headIconOffset = (Boolean)this.head.getValue() != false && this.headTexture != null ? 25.0f : 0.0f;
                float barTotalWidth = Math.max(headIconOffset + 70.0f, headIconOffset + 2.0f + barContentWidth + 2.0f);
                float posX = ((Integer)this.offX.getValue()).floatValue() / ((Float)this.scale.getValue()).floatValue();
                switch ((Integer)this.posX.getValue()) {
                    case 1: {
                        posX += (float)scaledResolution.func_78326_a() / ((Float)this.scale.getValue()).floatValue() / 2.0f - barTotalWidth / 2.0f;
                        break;
                    }
                    case 2: {
                        posX *= -1.0f;
                        posX += (float)scaledResolution.func_78326_a() / ((Float)this.scale.getValue()).floatValue() - barTotalWidth;
                    }
                }
                float posY = ((Integer)this.offY.getValue()).floatValue() / ((Float)this.scale.getValue()).floatValue();
                switch ((Integer)this.posY.getValue()) {
                    case 1: {
                        posY += (float)scaledResolution.func_78328_b() / ((Float)this.scale.getValue()).floatValue() / 2.0f - 13.5f;
                        break;
                    }
                    case 2: {
                        posY *= -1.0f;
                        posY += (float)scaledResolution.func_78328_b() / ((Float)this.scale.getValue()).floatValue() - 27.0f;
                    }
                }
                GlStateManager.func_179094_E();
                GlStateManager.func_179152_a((float)((Float)this.scale.getValue()).floatValue(), (float)((Float)this.scale.getValue()).floatValue(), (float)0.0f);
                GlStateManager.func_179109_b((float)posX, (float)posY, (float)-450.0f);
                RenderUtil.enableRenderState();
                int backgroundColor = new Color(0.0f, 0.0f, 0.0f, (float)((Integer)this.background.getValue()).intValue() / 100.0f).getRGB();
                int outlineColor = (Boolean)this.outline.getValue() != false ? targetColor.getRGB() : new Color(0, 0, 0, 0).getRGB();
                RenderUtil.drawOutlineRect(0.0f, 0.0f, barTotalWidth, 27.0f, 1.5f, backgroundColor, outlineColor);
                RenderUtil.drawRect(headIconOffset + 2.0f, 22.0f, barTotalWidth - 2.0f, 25.0f, ColorUtil.darker(healthBarColor, 0.2f).getRGB());
                RenderUtil.drawRect(headIconOffset + 2.0f, 22.0f, headIconOffset + 2.0f + healthRatio * (barTotalWidth - 2.0f - headIconOffset - 2.0f), 25.0f, healthBarColor.getRGB());
                RenderUtil.disableRenderState();
                GlStateManager.func_179097_i();
                GlStateManager.func_179147_l();
                GlStateManager.func_179112_b((int)770, (int)771);
                TargetHUD.mc.field_71466_p.func_175065_a(targetNameText, headIconOffset + 2.0f, 2.0f, -1, ((Boolean)this.shadow.getValue()).booleanValue());
                TargetHUD.mc.field_71466_p.func_175065_a(healthText, headIconOffset + 2.0f, 12.0f, -1, ((Boolean)this.shadow.getValue()).booleanValue());
                if (((Boolean)this.indicator.getValue()).booleanValue()) {
                    TargetHUD.mc.field_71466_p.func_175065_a(statusText, barTotalWidth - 2.0f - (float)statusTextWidth, 2.0f, healthDeltaColor.getRGB(), ((Boolean)this.shadow.getValue()).booleanValue());
                    TargetHUD.mc.field_71466_p.func_175065_a(healthDiffText, barTotalWidth - 2.0f - (float)healthDiffWidth, 12.0f, ColorUtil.darker(healthDeltaColor, 0.8f).getRGB(), ((Boolean)this.shadow.getValue()).booleanValue());
                }
                if (((Boolean)this.head.getValue()).booleanValue() && this.headTexture != null) {
                    GlStateManager.func_179124_c((float)1.0f, (float)1.0f, (float)1.0f);
                    mc.func_110434_K().func_110577_a(this.headTexture);
                    Gui.func_152125_a((int)2, (int)2, (float)8.0f, (float)8.0f, (int)8, (int)8, (int)23, (int)23, (float)64.0f, (float)64.0f);
                    Gui.func_152125_a((int)2, (int)2, (float)40.0f, (float)8.0f, (int)8, (int)8, (int)23, (int)23, (float)64.0f, (float)64.0f);
                    GlStateManager.func_179124_c((float)1.0f, (float)1.0f, (float)1.0f);
                }
                GlStateManager.func_179084_k();
                GlStateManager.func_179126_j();
                GlStateManager.func_179121_F();
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getType() == EventType.SEND && event.getPacket() instanceof C02PacketUseEntity) {
            C02PacketUseEntity packet = (C02PacketUseEntity)event.getPacket();
            if (packet.func_149565_c() != C02PacketUseEntity.Action.ATTACK) {
                return;
            }
            Entity entity = packet.func_149564_a((World)TargetHUD.mc.field_71441_e);
            if (entity instanceof EntityLivingBase) {
                if (entity instanceof EntityArmorStand) {
                    return;
                }
                this.lastAttackTimer.reset();
                this.lastTarget = (EntityLivingBase)entity;
            }
        }
    }
}

