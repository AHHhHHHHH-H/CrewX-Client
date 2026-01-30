/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C0BPacketEntityAction
 *  net.minecraft.network.play.client.C0BPacketEntityAction$Action
 *  net.minecraft.util.MathHelper
 *  net.minecraft.util.MovingObjectPosition$MovingObjectType
 */
package myau.module.modules;

import myau.event.EventTarget;
import myau.events.AttackEvent;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ModeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;

public class MoreKB
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"LEGIT", "LEGIT_FAST", "LESS_PACKET", "PACKET", "DOUBLE_PACKET"});
    public final BooleanProperty intelligent = new BooleanProperty("intelligent", false);
    public final BooleanProperty onlyGround = new BooleanProperty("only-ground", true);
    private boolean shouldSprintReset = false;
    private EntityLivingBase target = null;

    public MoreKB() {
        super("MoreKB", false);
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        Entity targetEntity = event.getTarget();
        if (targetEntity != null && targetEntity instanceof EntityLivingBase) {
            this.target = (EntityLivingBase)targetEntity;
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if ((Integer)this.mode.getValue() == 1) {
            if (this.target != null && this.isMoving()) {
                if (((Boolean)this.onlyGround.getValue()).booleanValue() && MoreKB.mc.field_71439_g.field_70122_E || !((Boolean)this.onlyGround.getValue()).booleanValue()) {
                    MoreKB.mc.field_71439_g.field_71157_e = 0;
                }
                this.target = null;
            }
            return;
        }
        EntityLivingBase entity = null;
        if (MoreKB.mc.field_71476_x != null && MoreKB.mc.field_71476_x.field_72313_a == MovingObjectPosition.MovingObjectType.ENTITY && MoreKB.mc.field_71476_x.field_72308_g instanceof EntityLivingBase) {
            entity = (EntityLivingBase)MoreKB.mc.field_71476_x.field_72308_g;
        }
        if (entity == null) {
            return;
        }
        double x = MoreKB.mc.field_71439_g.field_70165_t - entity.field_70165_t;
        double z = MoreKB.mc.field_71439_g.field_70161_v - entity.field_70161_v;
        float calcYaw = (float)(Math.atan2(z, x) * 180.0 / Math.PI - 90.0);
        float diffY = Math.abs(MathHelper.func_76142_g((float)(calcYaw - entity.field_70759_as)));
        if (((Boolean)this.intelligent.getValue()).booleanValue() && diffY > 120.0f) {
            return;
        }
        if (entity.field_70737_aN == 10) {
            switch ((Integer)this.mode.getValue()) {
                case 0: {
                    this.shouldSprintReset = true;
                    if (MoreKB.mc.field_71439_g.func_70051_ag()) {
                        MoreKB.mc.field_71439_g.func_70031_b(false);
                        MoreKB.mc.field_71439_g.func_70031_b(true);
                    }
                    this.shouldSprintReset = false;
                    break;
                }
                case 2: {
                    if (MoreKB.mc.field_71439_g.func_70051_ag()) {
                        MoreKB.mc.field_71439_g.func_70031_b(false);
                    }
                    mc.func_147114_u().func_147297_a((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.field_71439_g, C0BPacketEntityAction.Action.START_SPRINTING));
                    MoreKB.mc.field_71439_g.func_70031_b(true);
                    break;
                }
                case 3: {
                    MoreKB.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.field_71439_g, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    MoreKB.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.field_71439_g, C0BPacketEntityAction.Action.START_SPRINTING));
                    MoreKB.mc.field_71439_g.func_70031_b(true);
                    break;
                }
                case 4: {
                    MoreKB.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.field_71439_g, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    MoreKB.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.field_71439_g, C0BPacketEntityAction.Action.START_SPRINTING));
                    MoreKB.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.field_71439_g, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    MoreKB.mc.field_71439_g.field_71174_a.func_147297_a((Packet)new C0BPacketEntityAction((Entity)MoreKB.mc.field_71439_g, C0BPacketEntityAction.Action.START_SPRINTING));
                    MoreKB.mc.field_71439_g.func_70031_b(true);
                }
            }
        }
    }

    private boolean isMoving() {
        return MoreKB.mc.field_71439_g.field_70701_bs != 0.0f || MoreKB.mc.field_71439_g.field_70702_br != 0.0f;
    }

    @Override
    public String[] getSuffix() {
        return new String[]{((Integer)this.mode.getValue()).toString()};
    }
}

