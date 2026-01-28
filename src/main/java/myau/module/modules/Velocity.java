/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.CaseFormat
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.INetHandlerPlayClient
 *  net.minecraft.network.play.server.S12PacketEntityVelocity
 *  net.minecraft.network.play.server.S19PacketEntityStatus
 *  net.minecraft.network.play.server.S27PacketExplosion
 *  net.minecraft.potion.Potion
 *  net.minecraft.world.World
 */
package myau.module.modules;

import com.google.common.base.CaseFormat;
import myau.Myau;
import myau.enums.DelayModules;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.KnockbackEvent;
import myau.events.LivingUpdateEvent;
import myau.events.LoadWorldEvent;
import myau.events.PacketEvent;
import myau.events.UpdateEvent;
import myau.mixin.IAccessorEntity;
import myau.module.Module;
import myau.module.modules.KillAura;
import myau.module.modules.LongJump;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.ChatUtil;
import myau.util.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;

public class Velocity
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private int chanceCounter = 0;
    private int delayChanceCounter = 0;
    private boolean pendingExplosion = false;
    private boolean allowNext = true;
    private boolean jumpFlag = false;
    private boolean reverseFlag = false;
    private boolean delayActive = false;
    private boolean shouldJump = false;
    private int jumpCooldown = 0;
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"VANILLA", "JUMP", "DELAY", "REVERSE", "LEGIT_TEST"});
    public final IntProperty delayTicks = new IntProperty("delay-ticks", 3, 1, 20, () -> (Integer)this.mode.getValue() == 2);
    public final PercentProperty delayChance = new PercentProperty("delay-chance", 100, () -> (Integer)this.mode.getValue() == 2);
    public final PercentProperty chance = new PercentProperty("chance", 100);
    public final PercentProperty horizontal = new PercentProperty("horizontal", 0);
    public final PercentProperty vertical = new PercentProperty("vertical", 100);
    public final PercentProperty explosionHorizontal = new PercentProperty("explosions-horizontal", 100);
    public final PercentProperty explosionVertical = new PercentProperty("explosions-vertical", 100);
    public final BooleanProperty fakeCheck = new BooleanProperty("fake-check", true);
    public final BooleanProperty debugLog = new BooleanProperty("debug-log", false);

    private boolean isInLiquidOrWeb() {
        return Velocity.mc.field_71439_g.func_70090_H() || Velocity.mc.field_71439_g.func_180799_ab() || ((IAccessorEntity)Velocity.mc.field_71439_g).getIsInWeb();
    }

    private boolean canDelay() {
        KillAura killAura = (KillAura)Myau.moduleManager.modules.get(KillAura.class);
        return Velocity.mc.field_71439_g.field_70122_E && (!killAura.isEnabled() || !killAura.shouldAutoBlock());
    }

    public Velocity() {
        super("Velocity", false);
    }

    @EventTarget
    public void onKnockback(KnockbackEvent event) {
        if (!this.isEnabled() || event.isCancelled()) {
            this.pendingExplosion = false;
            this.allowNext = true;
        } else if (!this.allowNext || !((Boolean)this.fakeCheck.getValue()).booleanValue()) {
            this.allowNext = true;
            if (this.pendingExplosion) {
                this.pendingExplosion = false;
                if ((Integer)this.explosionHorizontal.getValue() > 0) {
                    event.setX(event.getX() * (double)((Integer)this.explosionHorizontal.getValue()).intValue() / 100.0);
                    event.setZ(event.getZ() * (double)((Integer)this.explosionHorizontal.getValue()).intValue() / 100.0);
                } else {
                    event.setX(Velocity.mc.field_71439_g.field_70159_w);
                    event.setZ(Velocity.mc.field_71439_g.field_70179_y);
                }
                if ((Integer)this.explosionVertical.getValue() > 0) {
                    event.setY(event.getY() * (double)((Integer)this.explosionVertical.getValue()).intValue() / 100.0);
                } else {
                    event.setY(Velocity.mc.field_71439_g.field_70181_x);
                }
            } else {
                this.chanceCounter = this.chanceCounter % 100 + (Integer)this.chance.getValue();
                if (this.chanceCounter >= 100) {
                    this.jumpFlag = ((Integer)this.mode.getValue() == 1 || (Integer)this.mode.getValue() == 2) && event.getY() > 0.0;
                    boolean bl = this.delayActive = (Integer)this.mode.getValue() == 3;
                    if ((Integer)this.horizontal.getValue() > 0) {
                        event.setX(event.getX() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                        event.setZ(event.getZ() * (double)((Integer)this.horizontal.getValue()).intValue() / 100.0);
                    } else {
                        event.setX(Velocity.mc.field_71439_g.field_70159_w);
                        event.setZ(Velocity.mc.field_71439_g.field_70179_y);
                    }
                    if ((Integer)this.vertical.getValue() > 0) {
                        event.setY(event.getY() * (double)((Integer)this.vertical.getValue()).intValue() / 100.0);
                    } else {
                        event.setY(Velocity.mc.field_71439_g.field_70181_x);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (event.getType() == EventType.POST) {
            if (this.reverseFlag && (this.canDelay() || this.isInLiquidOrWeb() || Myau.delayManager.getDelay() >= (long)((Integer)this.delayTicks.getValue()).intValue())) {
                Myau.delayManager.setDelayState(false, DelayModules.VELOCITY);
                this.reverseFlag = false;
            }
            if (this.delayActive) {
                MoveUtil.setSpeed(MoveUtil.getSpeed(), MoveUtil.getMoveYaw());
                this.delayActive = false;
            }
            if ((Integer)this.mode.getValue() == 4) {
                int hurtTime = Velocity.mc.field_71439_g.field_70737_aN;
                if (hurtTime >= 8) {
                    if (this.jumpCooldown <= 0) {
                        this.shouldJump = true;
                        this.jumpCooldown = 2;
                    }
                } else if (hurtTime <= 1) {
                    this.shouldJump = false;
                    this.jumpCooldown = 0;
                }
                if (this.shouldJump && Velocity.mc.field_71439_g.field_70122_E && this.jumpCooldown <= 0) {
                    Velocity.mc.field_71439_g.func_70664_aZ();
                    this.shouldJump = false;
                }
                if (this.jumpCooldown > 0) {
                    --this.jumpCooldown;
                }
            }
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.jumpFlag) {
            this.jumpFlag = false;
            if (Velocity.mc.field_71439_g.field_70122_E && Velocity.mc.field_71439_g.func_70051_ag() && !Velocity.mc.field_71439_g.func_70644_a(Potion.field_76430_j) && !this.isInLiquidOrWeb()) {
                Velocity.mc.field_71439_g.field_71158_b.field_78901_c = true;
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (this.isEnabled() && event.getType() == EventType.RECEIVE && !event.isCancelled()) {
            if (event.getPacket() instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity packet = (S12PacketEntityVelocity)event.getPacket();
                if (packet.func_149412_c() == Velocity.mc.field_71439_g.func_145782_y()) {
                    LongJump longJump = (LongJump)Myau.moduleManager.modules.get(LongJump.class);
                    if (!((Integer)this.mode.getValue() != 2 || this.reverseFlag || this.canDelay() || this.isInLiquidOrWeb() || this.pendingExplosion || this.allowNext && ((Boolean)this.fakeCheck.getValue()).booleanValue() || longJump.isEnabled() && longJump.canStartJump())) {
                        this.delayChanceCounter = this.delayChanceCounter % 100 + (Integer)this.delayChance.getValue();
                        if (this.delayChanceCounter >= 100) {
                            Myau.delayManager.setDelayState(true, DelayModules.VELOCITY);
                            Myau.delayManager.delayedPacket.offer((Packet<INetHandlerPlayClient>)packet);
                            event.setCancelled(true);
                            this.reverseFlag = true;
                            return;
                        }
                    }
                    if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                        ChatUtil.sendFormatted(String.format("%sVelocity (&otick: %d, x: %.2f, y: %.2f, z: %.2f&r)&r", Myau.clientName, Velocity.mc.field_71439_g.field_70173_aa, (double)packet.func_149411_d() / 8000.0, (double)packet.func_149410_e() / 8000.0, (double)packet.func_149409_f() / 8000.0));
                    }
                }
            } else if (!(event.getPacket() instanceof S27PacketExplosion)) {
                S19PacketEntityStatus packet;
                Entity entity;
                if (event.getPacket() instanceof S19PacketEntityStatus && (entity = (packet = (S19PacketEntityStatus)event.getPacket()).func_149161_a((World)Velocity.mc.field_71441_e)) != null && entity.equals((Object)Velocity.mc.field_71439_g) && packet.func_149160_c() == 2) {
                    this.allowNext = false;
                }
            } else {
                S27PacketExplosion packet = (S27PacketExplosion)event.getPacket();
                if (packet.func_149149_c() != 0.0f || packet.func_149144_d() != 0.0f || packet.func_149147_e() != 0.0f) {
                    this.pendingExplosion = true;
                    if ((Integer)this.explosionHorizontal.getValue() == 0 || (Integer)this.explosionVertical.getValue() == 0) {
                        event.setCancelled(true);
                    }
                    if (((Boolean)this.debugLog.getValue()).booleanValue()) {
                        ChatUtil.sendFormatted(String.format("%sExplosion (&otick: %d, x: %.2f, y: %.2f, z: %.2f&r)&r", Myau.clientName, Velocity.mc.field_71439_g.field_70173_aa, Velocity.mc.field_71439_g.field_70159_w + (double)packet.func_149149_c(), Velocity.mc.field_71439_g.field_70181_x + (double)packet.func_149144_d(), Velocity.mc.field_71439_g.field_70179_y + (double)packet.func_149147_e()));
                    }
                }
            }
        }
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        this.onDisabled();
    }

    @Override
    public void onDisabled() {
        this.pendingExplosion = false;
        this.allowNext = true;
        this.shouldJump = false;
        this.jumpCooldown = 0;
    }

    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
    }
}

