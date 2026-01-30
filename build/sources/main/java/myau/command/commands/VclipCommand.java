/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package myau.command.commands;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import myau.Myau;
import myau.command.Command;
import myau.util.ChatUtil;
import net.minecraft.client.Minecraft;

public class VclipCommand
extends Command {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private static final DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

    public VclipCommand() {
        super(new ArrayList<String>(Collections.singletonList("vclip")));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    @Override
    public void runCommand(ArrayList<String> args) {
        // Check if the user actually provided a distance argument
        if (args.size() < 2) {
            ChatUtil.sendFormatted(String.format("%sUsage: .%s <&odistance&r>&r", Myau.clientName, args.get(0).toLowerCase(Locale.ROOT)));
            return;
        }

        double distance = 0.0;
        try {
            // Attempt to parse the distance from the second argument
            distance = Double.parseDouble(args.get(1));

            // The teleport logic: current X, current Y + distance, current Z
            VclipCommand.mc.field_71439_g.func_70634_a(
                    VclipCommand.mc.field_71439_g.field_70165_t,
                    VclipCommand.mc.field_71439_g.field_70163_u + distance,
                    VclipCommand.mc.field_71439_g.field_70161_v
            );

            ChatUtil.sendFormatted(String.format("%sClipped (%s blocks)", Myau.clientName, df.format(distance)));

        } catch (NumberFormatException e) {
            // If the user typed letters instead of a number
            ChatUtil.sendFormatted(Myau.clientName + "Invalid distance. Please enter a number.");
        } catch (Exception e) {
            // Catch-all for any other weird errors (like the player being null)
            e.printStackTrace();
        }
    }
}

