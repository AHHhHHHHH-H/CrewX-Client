/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.item.ItemStack
 */
package myau.command.commands;

import java.util.ArrayList;
import java.util.Arrays;
import myau.Myau;
import myau.command.Command;
import myau.enums.ChatColors;
import myau.util.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ItemCommand
extends Command {
    private static final Minecraft mc = Minecraft.func_71410_x();

    public ItemCommand() {
        super(new ArrayList<String>(Arrays.asList("itemname", "item")));
    }

    @Override
    public void runCommand(ArrayList<String> args) {
        ItemStack stack = ItemCommand.mc.field_71439_g.field_71071_by.func_70448_g();
        if (stack != null) {
            String display = stack.func_82833_r().replace('\u00a7', '&');
            String registryName = stack.func_77973_b().getRegistryName();
            String compound = stack.func_77942_o() ? stack.func_77978_p().toString().replace('\u00a7', '&') : "";
            ChatUtil.sendRaw(String.format("%s%s (%s) %s", ChatColors.formatColor(Myau.clientName), display, registryName, compound));
        }
    }
}

