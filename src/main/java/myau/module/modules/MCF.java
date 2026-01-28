/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.util.MovingObjectPosition$MovingObjectType
 */
package myau.module.modules;

import myau.Myau;
import myau.event.EventTarget;
import myau.events.KeyEvent;
import myau.module.Module;
import myau.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;

public class MCF
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public MCF() {
        super("MCF", false, true);
    }

    @EventTarget
    public void onKey(KeyEvent event) {
        if (this.isEnabled() && event.getKey() == -98 && MCF.mc.field_71476_x != null && MCF.mc.field_71476_x.field_72313_a == MovingObjectPosition.MovingObjectType.ENTITY && MCF.mc.field_71476_x.field_72308_g instanceof EntityPlayer) {
            String hitName = MCF.mc.field_71476_x.field_72308_g.func_70005_c_();
            if (!Myau.friendManager.isFriend(hitName)) {
                Myau.friendManager.add(hitName);
                ChatUtil.sendFormatted(String.format("%sAdded &o%s&r to your friend list&r", Myau.clientName, hitName));
            } else {
                Myau.friendManager.remove(hitName);
                ChatUtil.sendFormatted(String.format("%sRemoved &o%s&r from your friend list&r", Myau.clientName, hitName));
            }
        }
    }
}

