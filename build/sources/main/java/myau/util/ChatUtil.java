/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.util.ChatComponentText
 *  net.minecraft.util.IChatComponent
 */
package myau.util;

import myau.enums.ChatColors;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class ChatUtil {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public static void send(IChatComponent iChatComponent) {
        if (ChatUtil.mc.field_71439_g != null) {
            ChatUtil.mc.field_71439_g.func_145747_a(iChatComponent);
        }
    }

    public static void sendFormatted(String string) {
        ChatUtil.send((IChatComponent)new ChatComponentText(ChatColors.formatColor(string)));
    }

    public static void sendRaw(String string) {
        ChatUtil.send((IChatComponent)new ChatComponentText(string));
    }

    public static void sendMessage(String string) {
        if (ChatUtil.mc.field_71439_g != null) {
            ChatUtil.mc.field_71439_g.func_71165_d(string);
        }
    }
}

