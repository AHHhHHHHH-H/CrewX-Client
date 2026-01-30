/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package myau.module.modules;

import myau.Myau;
import myau.event.EventTarget;
import myau.events.LivingUpdateEvent;
import myau.events.StrafeEvent;
import myau.mixin.IAccessorEntity;
import myau.module.Module;
import myau.module.modules.Scaffold;
import myau.property.properties.FloatProperty;
import myau.property.properties.PercentProperty;
import myau.util.MoveUtil;
import net.minecraft.client.Minecraft;

public class Speed
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final FloatProperty multiplier = new FloatProperty("multiplier", Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(10.0f));
    public final FloatProperty friction = new FloatProperty("friction", Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(10.0f));
    public final PercentProperty strafe = new PercentProperty("strafe", 0);

    private boolean canBoost() {
        Scaffold scaffold = (Scaffold)Myau.moduleManager.modules.get(Scaffold.class);
        return !scaffold.isEnabled() && MoveUtil.isForwardPressed() && Speed.mc.field_71439_g.func_71024_bL().func_75116_a() > 6 && !Speed.mc.field_71439_g.func_70093_af() && !Speed.mc.field_71439_g.func_70090_H() && !Speed.mc.field_71439_g.func_180799_ab() && !((IAccessorEntity)Speed.mc.field_71439_g).getIsInWeb();
    }

    public Speed() {
        super("Speed", false);
    }

    @EventTarget(value=3)
    public void onStrafe(StrafeEvent event) {
        if (this.isEnabled() && this.canBoost()) {
            if (Speed.mc.field_71439_g.field_70122_E) {
                Speed.mc.field_71439_g.field_70181_x = 0.42f;
                MoveUtil.setSpeed(MoveUtil.getJumpMotion() * (double)((Float)this.multiplier.getValue()).floatValue(), MoveUtil.getMoveYaw());
            } else {
                if (((Float)this.friction.getValue()).floatValue() != 1.0f) {
                    event.setFriction(event.getFriction() * ((Float)this.friction.getValue()).floatValue());
                }
                if ((Integer)this.strafe.getValue() > 0) {
                    double speed = MoveUtil.getSpeed();
                    MoveUtil.setSpeed(speed * (double)((float)(100 - (Integer)this.strafe.getValue()) / 100.0f), MoveUtil.getDirectionYaw());
                    MoveUtil.addSpeed(speed * (double)((float)((Integer)this.strafe.getValue()).intValue() / 100.0f), MoveUtil.getMoveYaw());
                    MoveUtil.setSpeed(speed);
                }
            }
        }
    }

    @EventTarget(value=3)
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.isEnabled() && this.canBoost()) {
            Speed.mc.field_71439_g.field_71158_b.field_78901_c = false;
        }
    }
}

