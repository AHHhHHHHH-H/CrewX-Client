/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.util.MovingObjectPosition$MovingObjectType
 */
package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.TickEvent;
import myau.mixin.IAccessorPlayerControllerMP;
import myau.module.Module;
import myau.property.properties.IntProperty;
import myau.property.properties.PercentProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;

public class SpeedMine
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final PercentProperty speed = new PercentProperty("speed", 15);
    public final IntProperty delay = new IntProperty("delay", 0, 0, 4);

    public SpeedMine() {
        super("SpeedMine", false);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE && !SpeedMine.mc.field_71442_b.func_78758_h() && SpeedMine.mc.field_71476_x != null && SpeedMine.mc.field_71476_x.field_72313_a == MovingObjectPosition.MovingObjectType.BLOCK) {
            float damage;
            float curBlockDamageMP;
            ((IAccessorPlayerControllerMP)SpeedMine.mc.field_71442_b).setBlockHitDelay(Math.min(((IAccessorPlayerControllerMP)SpeedMine.mc.field_71442_b).getBlockHitDelay(), (Integer)this.delay.getValue() + 1));
            if (((IAccessorPlayerControllerMP)SpeedMine.mc.field_71442_b).getIsHittingBlock() && (curBlockDamageMP = ((IAccessorPlayerControllerMP)SpeedMine.mc.field_71442_b).getCurBlockDamageMP()) < (damage = 0.3f * (((Integer)this.speed.getValue()).floatValue() / 100.0f))) {
                ((IAccessorPlayerControllerMP)SpeedMine.mc.field_71442_b).setCurBlockDamageMP(damage);
            }
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{String.format("%d%%", this.speed.getValue())};
    }
}

