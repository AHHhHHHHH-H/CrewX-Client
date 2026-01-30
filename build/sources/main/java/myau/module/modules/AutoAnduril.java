/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.item.ItemBlock
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.ItemSword
 *  net.minecraft.potion.Potion
 *  net.minecraft.potion.PotionEffect
 *  net.minecraft.util.MovingObjectPosition$MovingObjectType
 */
package myau.module.modules;

import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.TickEvent;
import myau.module.Module;
import myau.module.modules.InvWalk;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.ui.ClickGui;
import myau.util.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovingObjectPosition;

public class AutoAnduril
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private int previousSlot = -1;
    private int currentSlot = -1;
    private int intervalTick = -1;
    private int holdTick = -1;
    public final IntProperty interval = new IntProperty("interval", 40, 0, 100);
    public final IntProperty hold = new IntProperty("hold", 1, 0, 20);
    public final BooleanProperty speedCheck = new BooleanProperty("speed-check", false);
    public final IntProperty debug = new IntProperty("debug", 0, 0, 9);

    public AutoAnduril() {
        super("AutoAnduril", false);
    }

    public boolean canSwap() {
        if (AutoAnduril.mc.field_71476_x != null && AutoAnduril.mc.field_71476_x.field_72313_a == MovingObjectPosition.MovingObjectType.BLOCK && AutoAnduril.mc.field_71474_y.field_74312_F.func_151470_d()) {
            return false;
        }
        ItemStack currentItem = AutoAnduril.mc.field_71439_g.field_71071_by.func_70301_a(AutoAnduril.mc.field_71439_g.field_71071_by.field_70461_c);
        if (currentItem != null) {
            if (currentItem.func_77973_b() instanceof ItemBlock && AutoAnduril.mc.field_71474_y.field_74313_G.func_151470_d()) {
                return false;
            }
            if (!(currentItem.func_77973_b() instanceof ItemSword) && AutoAnduril.mc.field_71439_g.func_71039_bw()) {
                return false;
            }
        }
        InvWalk invWalk = (InvWalk)Myau.moduleManager.modules.get(InvWalk.class);
        return AutoAnduril.mc.field_71462_r == null || AutoAnduril.mc.field_71462_r instanceof ClickGui || invWalk.isEnabled() && invWalk.canInvWalk();
    }

    public boolean hasSpeed() {
        if (!((Boolean)this.speedCheck.getValue()).booleanValue()) {
            return false;
        }
        PotionEffect potionEffect = AutoAnduril.mc.field_71439_g.func_70660_b(Potion.field_76424_c);
        if (potionEffect == null) {
            return false;
        }
        return potionEffect.func_76458_c() > 0;
    }

    @EventTarget(value=4)
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (this.currentSlot != -1 && this.currentSlot != AutoAnduril.mc.field_71439_g.field_71071_by.field_70461_c) {
                this.currentSlot = -1;
                this.previousSlot = -1;
                this.intervalTick = (Integer)this.interval.getValue();
                this.holdTick = -1;
            }
            if (this.intervalTick > 0) {
                --this.intervalTick;
            } else if (this.intervalTick == 0 && this.canSwap() && !this.hasSpeed()) {
                int slot = ItemUtil.findAndurilHotbarSlot(AutoAnduril.mc.field_71439_g.field_71071_by.field_70461_c);
                if ((Integer)this.debug.getValue() != 0 && slot == -1) {
                    slot = (Integer)this.debug.getValue() - 1;
                }
                if (slot != -1 && slot != AutoAnduril.mc.field_71439_g.field_71071_by.field_70461_c) {
                    this.previousSlot = AutoAnduril.mc.field_71439_g.field_71071_by.field_70461_c;
                    this.currentSlot = AutoAnduril.mc.field_71439_g.field_71071_by.field_70461_c = slot;
                    this.intervalTick = -1;
                    this.holdTick = (Integer)this.hold.getValue();
                    return;
                }
                this.intervalTick = (Integer)this.interval.getValue();
                this.holdTick = -1;
            }
            if (this.holdTick > 0) {
                --this.holdTick;
            } else if (this.holdTick == 0 && this.previousSlot != -1 && this.canSwap()) {
                AutoAnduril.mc.field_71439_g.field_71071_by.field_70461_c = this.previousSlot;
                this.previousSlot = -1;
                this.holdTick = -1;
                this.intervalTick = (Integer)this.interval.getValue();
            }
        }
    }

    @Override
    public void onEnabled() {
        this.previousSlot = -1;
        this.currentSlot = -1;
        this.intervalTick = (Integer)this.interval.getValue();
        this.holdTick = -1;
    }

    @Override
    public void onDisabled() {
        this.previousSlot = -1;
        this.currentSlot = -1;
        this.intervalTick = -1;
        this.holdTick = -1;
    }
}

