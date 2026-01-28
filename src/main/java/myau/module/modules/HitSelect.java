/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.projectile.EntityLargeFireball
 *  net.minecraft.network.play.client.C02PacketUseEntity
 *  net.minecraft.network.play.client.C02PacketUseEntity$Action
 *  net.minecraft.network.play.client.C0BPacketEntityAction
 *  net.minecraft.util.Vec3
 *  net.minecraft.world.World
 */
package myau.module.modules;

import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.PacketEvent;
import myau.events.UpdateEvent;
import myau.module.Module;
import myau.module.modules.KeepSprint;
import myau.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class HitSelect
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"SECOND", "CRITICALS", "W_TAP"});
    private boolean sprintState = false;
    private boolean set = false;
    private double savedSlowdown = 0.0;
    private int blockedHits = 0;
    private int allowedHits = 0;

    public HitSelect() {
        super("HitSelect", false);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (event.getType() == EventType.POST) {
            this.resetMotion();
        }
    }

    @EventTarget(value=0)
    public void onPacket(PacketEvent event) {
        if (!this.isEnabled() || event.getType() != EventType.SEND || event.isCancelled()) {
            return;
        }
        if (event.getPacket() instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction packet = (C0BPacketEntityAction)event.getPacket();
            switch (packet.func_180764_b()) {
                case START_SPRINTING: {
                    this.sprintState = true;
                    break;
                }
                case STOP_SPRINTING: {
                    this.sprintState = false;
                }
            }
            return;
        }
        if (event.getPacket() instanceof C02PacketUseEntity) {
            C02PacketUseEntity use = (C02PacketUseEntity)event.getPacket();
            if (use.func_149565_c() != C02PacketUseEntity.Action.ATTACK) {
                return;
            }
            Entity target = use.func_149564_a((World)HitSelect.mc.field_71441_e);
            if (target == null || target instanceof EntityLargeFireball) {
                return;
            }
            if (!(target instanceof EntityLivingBase)) {
                return;
            }
            EntityLivingBase living = (EntityLivingBase)target;
            boolean allow = true;
            switch ((Integer)this.mode.getValue()) {
                case 0: {
                    allow = this.prioritizeSecondHit((EntityLivingBase)HitSelect.mc.field_71439_g, living);
                    break;
                }
                case 1: {
                    allow = this.prioritizeCriticalHits((EntityLivingBase)HitSelect.mc.field_71439_g);
                    break;
                }
                case 2: {
                    allow = this.prioritizeWTapHits((EntityLivingBase)HitSelect.mc.field_71439_g, this.sprintState);
                }
            }
            if (!allow) {
                event.setCancelled(true);
                ++this.blockedHits;
            } else {
                ++this.allowedHits;
            }
        }
    }

    private boolean prioritizeSecondHit(EntityLivingBase player, EntityLivingBase target) {
        if (target.field_70737_aN != 0) {
            return true;
        }
        if (player.field_70737_aN <= player.field_70738_aO - 1) {
            return true;
        }
        double dist = player.func_70032_d((Entity)target);
        if (dist < 2.5) {
            return true;
        }
        if (!this.isMovingTowards(target, player, 60.0)) {
            return true;
        }
        if (!this.isMovingTowards(player, target, 60.0)) {
            return true;
        }
        this.fixMotion();
        return false;
    }

    private boolean prioritizeCriticalHits(EntityLivingBase player) {
        if (player.field_70122_E) {
            return true;
        }
        if (player.field_70737_aN != 0) {
            return true;
        }
        if (player.field_70143_R > 0.0f) {
            return true;
        }
        this.fixMotion();
        return false;
    }

    private boolean prioritizeWTapHits(EntityLivingBase player, boolean sprinting) {
        if (player.field_70123_F) {
            return true;
        }
        if (!HitSelect.mc.field_71474_y.field_74351_w.func_151470_d()) {
            return true;
        }
        if (sprinting) {
            return true;
        }
        this.fixMotion();
        return false;
    }

    private void fixMotion() {
        if (this.set) {
            return;
        }
        KeepSprint keepSprint = (KeepSprint)Myau.moduleManager.modules.get(KeepSprint.class);
        if (keepSprint == null) {
            return;
        }
        try {
            this.savedSlowdown = ((Integer)keepSprint.slowdown.getValue()).doubleValue();
            if (!keepSprint.isEnabled()) {
                keepSprint.toggle();
            }
            keepSprint.slowdown.setValue(0);
            this.set = true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetMotion() {
        if (!this.set) {
            return;
        }
        KeepSprint keepSprint = (KeepSprint)Myau.moduleManager.modules.get(KeepSprint.class);
        if (keepSprint == null) {
            return;
        }
        try {
            keepSprint.slowdown.setValue((int)this.savedSlowdown);
            if (keepSprint.isEnabled()) {
                keepSprint.toggle();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.set = false;
        this.savedSlowdown = 0.0;
    }

    private boolean isMovingTowards(EntityLivingBase source, EntityLivingBase target, double maxAngle) {
        Vec3 currentPos = source.func_174791_d();
        Vec3 lastPos = new Vec3(source.field_70142_S, source.field_70137_T, source.field_70136_U);
        Vec3 targetPos = target.func_174791_d();
        double mx = currentPos.field_72450_a - lastPos.field_72450_a;
        double mz = currentPos.field_72449_c - lastPos.field_72449_c;
        double movementLength = Math.sqrt(mx * mx + mz * mz);
        if (movementLength == 0.0) {
            return false;
        }
        mx /= movementLength;
        mz /= movementLength;
        double tx = targetPos.field_72450_a - currentPos.field_72450_a;
        double tz = targetPos.field_72449_c - currentPos.field_72449_c;
        double targetLength = Math.sqrt(tx * tx + tz * tz);
        if (targetLength == 0.0) {
            return false;
        }
        double dotProduct = mx * (tx /= targetLength) + mz * (tz /= targetLength);
        return dotProduct >= Math.cos(Math.toRadians(maxAngle));
    }

    @Override
    public void onDisabled() {
        this.resetMotion();
        this.sprintState = false;
        this.set = false;
        this.savedSlowdown = 0.0;
        this.blockedHits = 0;
        this.allowedHits = 0;
    }

    @Override
    public String[] getSuffix() {
        return new String[]{this.mode.getModeString()};
    }
}

