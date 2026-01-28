/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package myau.module.modules;

import myau.module.Module;
import net.minecraft.client.Minecraft;

public class ViewClip
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public ViewClip() {
        super("ViewClip", false);
    }

    @Override
    public void onEnabled() {
        if (ViewClip.mc.field_71441_e != null) {
            ViewClip.mc.field_71438_f.func_72712_a();
        }
    }

    @Override
    public void onDisabled() {
        if (ViewClip.mc.field_71441_e != null) {
            ViewClip.mc.field_71438_f.func_72712_a();
        }
    }
}

