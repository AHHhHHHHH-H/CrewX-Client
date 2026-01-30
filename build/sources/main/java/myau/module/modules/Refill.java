/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.CaseFormat
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.inventory.GuiInventory
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Items
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemPotion
 *  net.minecraft.item.ItemStack
 */
package myau.module.modules;

import com.google.common.base.CaseFormat;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;

public class Refill
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final IntProperty delay = new IntProperty("delay", 1, 0, 20);
    public final ModeProperty mode = new ModeProperty("mode", 1, new String[]{"SOUP", "POT"});
    private final TimerUtil time = new TimerUtil();

    public Refill() {
        super("Refill", false);
    }

    @EventTarget
    public void onUpdate(TickEvent event) {
        if (this.isEnabled() && Refill.mc.field_71439_g != null && event.getType() == EventType.PRE) {
            if ((Integer)this.mode.getValue() == 0) {
                this.refill(Items.field_151009_A);
            } else if ((Integer)this.mode.getValue() == 1) {
                this.refill(ItemPotion.func_150899_d((int)373));
            }
        }
    }

    private void refill(Item targetItem) {
        if (Refill.mc.field_71462_r instanceof GuiInventory && !Refill.isHotbarFull() && this.time.hasTimeElapsed((Integer)this.delay.getValue() * 50)) {
            for (int i = 9; i < 36; ++i) {
                ItemStack itemstack = Refill.mc.field_71439_g.field_71069_bz.func_75139_a(i).func_75211_c();
                if (itemstack == null || itemstack.func_77973_b() != targetItem) continue;
                Refill.mc.field_71442_b.func_78753_a(0, i, 0, 1, (EntityPlayer)Refill.mc.field_71439_g);
                break;
            }
            this.time.reset();
        }
    }

    public static boolean isHotbarFull() {
        for (int i = 0; i <= 36; ++i) {
            ItemStack itemstack = Refill.mc.field_71439_g.field_71071_by.func_70301_a(i);
            if (itemstack != null) continue;
            return false;
        }
        return true;
    }

    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
    }
}

