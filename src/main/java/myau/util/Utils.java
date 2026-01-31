package myau.util;

import myau.Myau;
import myau.property.properties.FloatProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.scoreboard.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import myau.property.properties.IntProperty;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Timer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class Utils {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Queue<String> delayedMessage = new ConcurrentLinkedQueue<>();

    public static boolean nullCheck() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    public static void sendMessage(String txt) {
        if (!nullCheck()) return;
        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§dR§7]§r " + txt.replace("&", "§")));
    }

    public static void sendMessageAnyWay(String txt) {
        if (nullCheck()) {
            sendMessage(txt);
        } else {
            delayedMessage.add(txt);
        }
    }

    static {
        Myau.getExecutor().scheduleWithFixedDelay(() -> {
            if (nullCheck() && !delayedMessage.isEmpty()) {
                for (String s : delayedMessage) {
                    sendMessage(s);
                }
                delayedMessage.clear();
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public static void sendDebugMessage(String message) {
        if (!nullCheck()) return;
        mc.thePlayer.addChatMessage(new ChatComponentText("§7[§dR§7]§r " + message));
    }

    public static void sendRawMessage(String txt) {
        if (!nullCheck()) return;
        mc.thePlayer.addChatMessage(new ChatComponentText(txt.replace("&", "§")));
    }

    public static boolean inInventory() {
        if (!nullCheck()) return false;
        return mc.currentScreen instanceof GuiInventory
                && mc.thePlayer.inventoryContainer instanceof ContainerPlayer;
    }

    public static void correctValue(FloatProperty min, FloatProperty max) {
        if (min.getValue() > max.getValue()) {
            float tmp = min.getValue();
            min.setValue(max.getValue());
            max.setValue(tmp);
        }
    }
    // Fazem 5 horas que eu to aqui nesse backtrack, e ainda falta alguns erros de render que sa ochatos de resolver, vou terminar amanha
    public static void correctValue(IntProperty min, IntProperty max) {
        if (min.getValue() > max.getValue()) {
            int tmp = min.getValue();
            min.setValue(max.getValue());
            max.setValue(tmp);
        }
    }


    public static boolean isSkyWars() {
        if (!nullCheck()) return false;

        Scoreboard sb = mc.theWorld.getScoreboard();
        if (sb == null) return false;

        ScoreObjective obj = sb.getObjectiveInDisplaySlot(1);
        if (obj == null) return false;

        String name = strip(obj.getDisplayName()).toLowerCase();
        return name.contains("skywars") || name.contains("sky wars");
    }

    public static int getBedwarsStatus() {
        if (!nullCheck()) return -1;

        Scoreboard sb = mc.theWorld.getScoreboard();
        if (sb == null) return -1;

        ScoreObjective obj = sb.getObjectiveInDisplaySlot(1);
        if (obj == null) return -1;

        String title = strip(obj.getDisplayName());
        if (!title.contains("BED WARS") && !title.contains("BedWars")) return -1;

        for (String line : getSidebarLines()) {
            line = strip(line).trim();

            if (line.equalsIgnoreCase("Waiting...") || line.startsWith("Starting"))
                return 1;

            if (line.startsWith("R ") || line.startsWith("B "))
                return 2;
        }
        return -1;
    }

    public static boolean isMoving() {
        if (!nullCheck()) return false;
        return mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0;
    }

    public static boolean overAir() {
        if (!nullCheck()) return false;
        EntityPlayerSP p = mc.thePlayer;
        return mc.theWorld.isAirBlock(
                new BlockPos(p.posX, p.posY - 1.0, p.posZ)
        );
    }

    private static String strip(String s) {
        return s.replaceAll("§.", "");
    }

    private static List<String> getSidebarLines() {
        List<String> lines = new ArrayList<>();
        Scoreboard sb = mc.theWorld.getScoreboard();
        ScoreObjective obj = sb.getObjectiveInDisplaySlot(1);
        for (Score score : sb.getSortedScores(obj)) {
            if (score.getPlayerName() != null && !score.getPlayerName().startsWith("#")) {
                ScorePlayerTeam team = sb.getPlayersTeam(score.getPlayerName());
                lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
            }
        }
        return lines;
    }
    private static Timer timer = null;

    public static Timer getTimer() {
        if (timer == null) {
            timer = Reflection.get(mc, "field_71428_T", Timer.class);
            return timer;
        }
        return timer;
    }
}
