/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.inventory.GuiContainer
 *  org.lwjgl.input.Mouse
 */
package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.TickEvent;
import myau.mixin.IAccessorGuiScreen;
import myau.module.Module;
import myau.property.properties.IntProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.lwjgl.input.Mouse;

public class InventoryClicker
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final IntProperty triggerTicks = new IntProperty("ticks", 2, 0, 20);
    public int ticks;

    public InventoryClicker() {
        super("InventoryClicker", false);
    }

    @Override
    public String[] getSuffix() {
        return new String[]{((Integer)this.triggerTicks.getValue()).toString() + " ticks"};
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && InventoryClicker.mc.field_71439_g != null && event.getType() == EventType.PRE && InventoryClicker.mc.field_71462_r instanceof GuiContainer) {
            GuiContainer screen = (GuiContainer)InventoryClicker.mc.field_71462_r;
            int mouseX = Mouse.getEventX() * screen.field_146294_l / InventoryClicker.mc.field_71443_c;
            int mouseY = screen.field_146295_m - Mouse.getEventY() * screen.field_146295_m / InventoryClicker.mc.field_71440_d - 1;
            if (Mouse.isButtonDown((int)0)) {
                ++this.ticks;
                if (this.ticks > (Integer)this.triggerTicks.getValue()) {
                    ((IAccessorGuiScreen)screen).callMouseClicked(mouseX, mouseY, 0);
                }
            } else {
                this.ticks = 0;
            }
        }
    }
}

