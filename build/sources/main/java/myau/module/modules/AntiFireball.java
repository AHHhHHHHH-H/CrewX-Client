/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.projectile.EntityFireball
 *  net.minecraft.network.play.client.C02PacketUseEntity
 *  net.minecraft.network.play.client.C02PacketUseEntity$Action
 *  net.minecraft.network.play.client.C0APacketAnimation
 */
package myau.module.modules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.LoadWorldEvent;
import myau.events.MoveInputEvent;
import myau.events.Render3DEvent;
import myau.events.TickEvent;
import myau.events.UpdateEvent;
import myau.management.RotationState;
import myau.module.Module;
import myau.module.modules.HUD;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.util.ItemUtil;
import myau.util.MoveUtil;
import myau.util.PacketUtil;
import myau.util.PlayerUtil;
import myau.util.RenderUtil;
import myau.util.RotationUtil;
import myau.util.TeamUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0APacketAnimation;

public class AntiFireball
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final ArrayList<EntityFireball> farList = new ArrayList();
    private final ArrayList<EntityFireball> nearList = new ArrayList();
    private EntityFireball target = null;
    public final FloatProperty range = new FloatProperty("range", Float.valueOf(5.0f), Float.valueOf(3.0f), Float.valueOf(8.0f));
    public final IntProperty fov = new IntProperty("fov", 360, 1, 360);
    public final BooleanProperty rotations = new BooleanProperty("rotations", true);
    public final BooleanProperty swing = new BooleanProperty("swing", true);
    public final ModeProperty moveFix = new ModeProperty("move-fix", 1, new String[]{"NONE", "SILENT", "STRICT"});
    public final ModeProperty showTarget = new ModeProperty("show-target", 0, new String[]{"NONE", "DEFAULT", "HUD"});

    private boolean isValidTarget(EntityFireball entityFireball) {
        return !entityFireball.func_174813_aQ().func_181656_b() && RotationUtil.distanceToEntity((Entity)entityFireball) <= (double)((Float)this.range.getValue()).floatValue() + 3.0 && RotationUtil.angleToEntity((Entity)entityFireball) <= (float)((Integer)this.fov.getValue()).intValue();
    }

    private void doAttackAnimation() {
        if (((Boolean)this.swing.getValue()).booleanValue()) {
            AntiFireball.mc.field_71439_g.func_71038_i();
        } else {
            PacketUtil.sendPacket(new C0APacketAnimation());
        }
    }

    public AntiFireball() {
        super("AntiFireball", false);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            List fireballs = AntiFireball.mc.field_71441_e.field_72996_f.stream().filter(entity -> entity instanceof EntityFireball).map(entity -> (EntityFireball)entity).collect(Collectors.toList());
            this.farList.removeIf(entityFireball -> !fireballs.contains(entityFireball));
            this.nearList.removeIf(entityFireball -> !fireballs.contains(entityFireball));
            for (EntityFireball fireball : fireballs) {
                if (this.farList.contains(fireball) || this.nearList.contains(fireball)) continue;
                if (RotationUtil.distanceToEntity((Entity)fireball) > 3.0) {
                    this.farList.add(fireball);
                    continue;
                }
                this.nearList.add(fireball);
            }
            this.target = AntiFireball.mc.field_71439_g.field_71075_bZ.field_75101_c ? null : (EntityFireball)this.farList.stream().filter(this::isValidTarget).min(Comparator.comparingDouble(RotationUtil::distanceToEntity)).orElse(null);
        }
    }

    @EventTarget(value=4)
    public void onUpdate(UpdateEvent event) {
        EntityFireball fireball;
        if (this.isEnabled() && event.getType() == EventType.PRE && TeamUtil.isEntityLoaded((Entity)(fireball = this.target))) {
            float[] rotations = RotationUtil.getRotationsToBox(this.target.func_174813_aQ(), event.getYaw(), event.getPitch(), 180.0f, 0.0f);
            if (((Boolean)this.rotations.getValue()).booleanValue() && !ItemUtil.isHoldingNonEmpty() && !ItemUtil.isUsingBow() && !ItemUtil.hasHoldItem()) {
                event.setRotation(rotations[0], rotations[1], 0);
                event.setPervRotation((Integer)this.moveFix.getValue() != 0 ? rotations[0] : AntiFireball.mc.field_71439_g.field_70177_z, 0);
            }
            if (!(Myau.playerStateManager.attacking || Myau.playerStateManager.digging || Myau.playerStateManager.placing)) {
                this.doAttackAnimation();
                if (RotationUtil.distanceToEntity((Entity)this.target) <= (double)((Float)this.range.getValue()).floatValue()) {
                    PacketUtil.sendPacket(new C02PacketUseEntity((Entity)this.target, C02PacketUseEntity.Action.ATTACK));
                    PlayerUtil.attackEntity((Entity)this.target);
                }
            }
        }
    }

    @EventTarget
    public void onMove(MoveInputEvent event) {
        if (this.isEnabled() && (Integer)this.moveFix.getValue() == 1 && RotationState.isActived() && RotationState.getPriority() == 0.0f && MoveUtil.isForwardPressed()) {
            MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
        }
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
        if (this.isEnabled() && (Integer)this.showTarget.getValue() != 0 && TeamUtil.isEntityLoaded((Entity)this.target)) {
            Color color = new Color(-1);
            switch ((Integer)this.showTarget.getValue()) {
                case 1: {
                    double dist = (this.target.field_70165_t - this.target.field_70142_S) * (AntiFireball.mc.field_71439_g.field_70165_t - this.target.field_70165_t) + (this.target.field_70163_u - this.target.field_70137_T) * (AntiFireball.mc.field_71439_g.field_70163_u + (double)AntiFireball.mc.field_71439_g.func_70047_e() - this.target.field_70163_u - (double)this.target.field_70131_O / 2.0) + (this.target.field_70161_v - this.target.field_70136_U) * (AntiFireball.mc.field_71439_g.field_70161_v - this.target.field_70161_v);
                    if (dist < 0.0) {
                        color = new Color(0xFF5555);
                        break;
                    }
                    color = new Color(0x55FF55);
                    break;
                }
                case 2: {
                    color = ((HUD)Myau.moduleManager.modules.get(HUD.class)).getColor(System.currentTimeMillis());
                }
            }
            RenderUtil.enableRenderState();
            RenderUtil.drawEntityBox((Entity)this.target, color.getRed(), color.getGreen(), color.getBlue());
            RenderUtil.disableRenderState();
        }
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        this.farList.clear();
        this.nearList.clear();
    }
}

