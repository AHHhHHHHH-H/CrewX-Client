/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiChat
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.gui.ScaledResolution
 *  net.minecraft.client.renderer.GlStateManager
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.item.EntityEnderPearl
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.item.ItemEnderPearl
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.play.server.S02PacketChat
 *  net.minecraft.network.play.server.S08PacketPlayerPosLook
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.MathHelper
 */
package myau.module.modules;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import myau.Myau;
import myau.enums.ChatColors;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.LoadWorldEvent;
import myau.events.PacketEvent;
import myau.events.Render2DEvent;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.TextProperty;
import myau.util.ChatUtil;
import myau.util.ColorUtil;
import myau.util.SoundUtil;
import myau.util.TeamUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class BedTracker
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final LinkedHashMap<String, Long> alertCooldowns = new LinkedHashMap();
    private final LinkedHashSet<EntityEnderPearl> trackedPearls = new LinkedHashSet();
    private final LinkedHashSet<String> whitelistedPlayers = new LinkedHashSet();
    private final Color wBed = new Color(ChatColors.WHITE.toAwtColor());
    private final Color rBed = new Color(ChatColors.RED.toAwtColor());
    private final Color yBed = new Color(ChatColors.YELLOW.toAwtColor());
    private final Color gBed = new Color(ChatColors.GREEN.toAwtColor());
    private BlockPos bedPos = null;
    private long lastMarcoTime = -1L;
    private boolean waiting = false;
    public final BooleanProperty alerts = new BooleanProperty("alerts", true);
    public final IntProperty alertRange = new IntProperty("alerts-range", 48, 8, 128, this.alerts::getValue);
    public final BooleanProperty alertOnPearl = new BooleanProperty("alerts-on-pearl", true);
    public final ModeProperty alertSound = new ModeProperty("alerts-sound", 1, new String[]{"NONE", "MEOW", "ANVIL"}, () -> (Boolean)this.alerts.getValue() != false || (Boolean)this.alertOnPearl.getValue() != false);
    public final IntProperty alertFrequency = new IntProperty("alerts-frequency", 5, 1, 30, () -> (Boolean)this.alerts.getValue() != false || (Boolean)this.alertOnPearl.getValue() != false);
    public final BooleanProperty marco = new BooleanProperty("macro", false);
    public final IntProperty marcoRange = new IntProperty("macro-range", 24, 8, 128, this.marco::getValue);
    public final BooleanProperty marcoOnPreal = new BooleanProperty("macro-on-pearl", false);
    public final TextProperty marcoText = new TextProperty("macro-text", "/lobby", () -> (Boolean)this.marco.getValue() != false || (Boolean)this.marcoOnPreal.getValue() != false);
    public final IntProperty marcoDelay = new IntProperty("macro-delay", 1, 1, 10, () -> (Boolean)this.marco.getValue() != false || (Boolean)this.marcoOnPreal.getValue() != false);
    public final BooleanProperty hud = new BooleanProperty("hud", true);
    public final ModeProperty hudPosX = new ModeProperty("hud-position-x", 0, new String[]{"LEFT", "MIDDLE", "RIGHT"}, this.hud::getValue);
    public final ModeProperty hudPosY = new ModeProperty("hud-position-y", 0, new String[]{"TOP", "MIDDLE", "BOTTOM"}, this.hud::getValue);
    public final IntProperty hudOffX = new IntProperty("hud-offset-x", 2, 0, 255, this.hud::getValue);
    public final IntProperty hudOffY = new IntProperty("hud-offset-y", 2, 0, 255, this.hud::getValue);
    public final FloatProperty hudScale = new FloatProperty("hud-scale", Float.valueOf(1.0f), Float.valueOf(0.5f), Float.valueOf(1.5f), this.hud::getValue);
    public final BooleanProperty hudShadow = new BooleanProperty("hud-shadow", true, this.hud::getValue);

    private void playAlertSound() {
        switch ((Integer)this.alertSound.getValue()) {
            case 1: {
                SoundUtil.playSound("mob.cat.meow");
                break;
            }
            case 2: {
                SoundUtil.playSound("random.anvil_land");
            }
        }
    }

    private Color getHudColor(int distance) {
        if (distance < 0) {
            return this.wBed;
        }
        if (distance <= 100) {
            return this.gBed;
        }
        if (distance <= 114) {
            return ColorUtil.interpolate((float)(114 - distance) / 14.0f, this.yBed, this.gBed);
        }
        return distance <= 128 ? ColorUtil.interpolate((float)(128 - distance) / 14.0f, this.rBed, this.yBed) : this.rBed;
    }

    private boolean isBed(BlockPos blockPos) {
        return blockPos != null && BedTracker.mc.field_71441_e.func_180495_p(blockPos).func_177230_c() == Blocks.field_150324_C;
    }

    public BedTracker() {
        super("BedTracker", false, true);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.POST && this.isBed(this.bedPos)) {
            long millis = System.currentTimeMillis();
            boolean pearl = false;
            boolean marco = false;
            for (Entity entity2 : BedTracker.mc.field_71441_e.field_72996_f) {
                EntityEnderPearl enderPearl;
                if (!(entity2 instanceof EntityEnderPearl) || this.trackedPearls.contains(enderPearl = (EntityEnderPearl)entity2)) continue;
                this.trackedPearls.add(enderPearl);
                if (((Boolean)this.alertOnPearl.getValue()).booleanValue()) {
                    ChatUtil.sendFormatted(String.format("%s%s: &fDetected &5Ender Pearl&r &e&l\u26a0&r", Myau.clientName, this.getName()));
                    pearl = true;
                }
                if (!((Boolean)this.marcoOnPreal.getValue()).booleanValue() || this.lastMarcoTime + (long)((Integer)this.marcoDelay.getValue()).intValue() * 1000L > millis) continue;
                this.lastMarcoTime = millis;
                marco = true;
            }
            for (EntityPlayer player : BedTracker.mc.field_71441_e.field_72996_f.stream().filter(entity -> entity instanceof EntityPlayer).map(entity -> (EntityPlayer)entity).filter(entityPlayer -> !TeamUtil.isBot(entityPlayer) && !this.whitelistedPlayers.contains(entityPlayer.func_70005_c_())).collect(Collectors.toList())) {
                Long cooldown;
                boolean isPearl;
                if (TeamUtil.isSameTeam(player)) {
                    this.whitelistedPlayers.add(player.func_70005_c_());
                    continue;
                }
                double distance = player.func_70011_f((double)this.bedPos.func_177958_n() + 0.5, (double)this.bedPos.func_177956_o() + 0.5, (double)this.bedPos.func_177952_p() + 0.5);
                String name = player.func_70005_c_();
                String text = player.func_145748_c_().func_150254_d();
                ItemStack item = player.func_70694_bm();
                boolean bl = isPearl = item != null && item.func_77973_b() instanceof ItemEnderPearl;
                if (((Boolean)this.alerts.getValue()).booleanValue() && distance < (double)((Integer)this.alertRange.getValue()).intValue() && ((cooldown = this.alertCooldowns.get(name)) == null || cooldown + (long)((Integer)this.alertFrequency.getValue()).intValue() * 1000L <= millis)) {
                    this.alertCooldowns.put(name, millis);
                    ChatUtil.sendFormatted(String.format("%s%s: %s&r &fis %d blocks away from your bed &e&l\u26a0&r", Myau.clientName, this.getName(), text, (int)distance + 1));
                    pearl = true;
                }
                if (((Boolean)this.alertOnPearl.getValue()).booleanValue() && isPearl && ((cooldown = this.alertCooldowns.get(name)) == null || cooldown + (long)((Integer)this.alertFrequency.getValue()).intValue() * 1000L <= millis)) {
                    this.alertCooldowns.put(name, millis);
                    ChatUtil.sendFormatted(String.format("%s%s: %s&r &fhas &5Ender Pearl&r &e&l\u26a0&r", Myau.clientName, this.getName(), text));
                    pearl = true;
                }
                if (!((Boolean)this.marco.getValue() != false && distance < (double)((Integer)this.marcoRange.getValue()).intValue()) && (!((Boolean)this.marcoOnPreal.getValue()).booleanValue() || !isPearl) || this.lastMarcoTime + (long)((Integer)this.marcoDelay.getValue()).intValue() * 1000L > millis) continue;
                this.lastMarcoTime = millis;
                marco = true;
            }
            if (pearl) {
                this.playAlertSound();
            }
            if (marco) {
                ChatUtil.sendRaw(String.format(ChatColors.formatColor("%s%s: &fRunning &6%s&r"), ChatColors.formatColor(Myau.clientName), this.getName(), this.marcoText.getValue()));
                ChatUtil.sendMessage((String)this.marcoText.getValue());
            }
        }
    }

    @EventTarget(value=3)
    public void onRender(Render2DEvent event) {
        GuiScreen currentScreen;
        if (this.isEnabled() && ((Boolean)this.hud.getValue()).booleanValue() && BedTracker.mc.field_71441_e != null && BedTracker.mc.field_71439_g != null && !BedTracker.mc.field_71474_y.field_74330_P && ((currentScreen = BedTracker.mc.field_71462_r) == null || currentScreen instanceof GuiChat)) {
            int distanceSq = 0;
            boolean hasBed = this.isBed(this.bedPos);
            if (hasBed) {
                double xDiff = BedTracker.mc.field_71439_g.field_70165_t - (double)this.bedPos.func_177958_n();
                double zDiff = BedTracker.mc.field_71439_g.field_70161_v - (double)this.bedPos.func_177952_p();
                distanceSq = (int)Math.sqrt(xDiff * xDiff + zDiff * zDiff) + 1;
            }
            Object[] objectArray = new Object[2];
            Object object = objectArray[0] = !hasBed ? "&cfalse&r" : "&atrue&r";
            objectArray[1] = !hasBed ? "" : String.format(" &7| &fDistance: &r%d%s", distanceSq, distanceSq >= 128 ? " &c&l\u26a0&r" : "");
            String text = ChatColors.formatColor(String.format("&fBed: %s%s", objectArray));
            ScaledResolution scaledResolution = new ScaledResolution(mc);
            float width = BedTracker.mc.field_71466_p.func_78256_a(text);
            float height = (float)BedTracker.mc.field_71466_p.field_78288_b - 1.0f;
            float scale = (float)((Integer)this.hudOffX.getValue()).intValue() / ((Float)this.hudScale.getValue()).floatValue();
            switch ((Integer)this.hudPosX.getValue()) {
                case 0: {
                    scale += 1.0f;
                    break;
                }
                case 1: {
                    scale += (float)scaledResolution.func_78326_a() / ((Float)this.hudScale.getValue()).floatValue() / 2.0f - width / 2.0f;
                    break;
                }
                case 2: {
                    scale = (scale + 1.0f) * -1.0f;
                    scale += (float)scaledResolution.func_78326_a() / ((Float)this.hudScale.getValue()).floatValue() - width;
                }
            }
            float offset = (float)((Integer)this.hudOffY.getValue()).intValue() / ((Float)this.hudScale.getValue()).floatValue();
            switch ((Integer)this.hudPosY.getValue()) {
                case 0: {
                    offset += 1.0f;
                    break;
                }
                case 1: {
                    offset += (float)scaledResolution.func_78328_b() / ((Float)this.hudScale.getValue()).floatValue() / 2.0f - height / 2.0f;
                    break;
                }
                case 2: {
                    offset = (offset + 1.0f) * -1.0f;
                    offset += (float)scaledResolution.func_78328_b() / ((Float)this.hudScale.getValue()).floatValue() - height;
                }
            }
            GlStateManager.func_179094_E();
            GlStateManager.func_179152_a((float)((Float)this.hudScale.getValue()).floatValue(), (float)((Float)this.hudScale.getValue()).floatValue(), (float)1.0f);
            GlStateManager.func_179109_b((float)scale, (float)offset, (float)0.0f);
            GlStateManager.func_179097_i();
            GlStateManager.func_179147_l();
            GlStateManager.func_179112_b((int)770, (int)771);
            BedTracker.mc.field_71466_p.func_175065_a(text, 0.0f, 0.0f, this.getHudColor(distanceSq).getRGB(), ((Boolean)this.hudShadow.getValue()).booleanValue());
            GlStateManager.func_179084_k();
            GlStateManager.func_179126_j();
            GlStateManager.func_179121_F();
        }
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        this.waiting = false;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (this.isEnabled()) {
            String msg;
            if (event.getPacket() instanceof S02PacketChat && ((msg = ((S02PacketChat)event.getPacket()).func_148915_c().func_150254_d()).contains("\u00a7e\u00a7lProtect your bed and destroy the enemy bed") || msg.contains("\u00a7e\u00a7lDestroy the enemy bed and then eliminate them"))) {
                this.alertCooldowns.clear();
                this.trackedPearls.clear();
                this.whitelistedPlayers.clear();
                this.bedPos = null;
                this.waiting = true;
            }
            if (event.getPacket() instanceof S08PacketPlayerPosLook && this.waiting) {
                this.waiting = false;
                this.executor.schedule(() -> {
                    int x = MathHelper.func_76128_c((double)BedTracker.mc.field_71439_g.field_70165_t);
                    int y = MathHelper.func_76128_c((double)(BedTracker.mc.field_71439_g.field_70163_u + (double)BedTracker.mc.field_71439_g.func_70047_e()));
                    int z = MathHelper.func_76128_c((double)BedTracker.mc.field_71439_g.field_70161_v);
                    for (int i = x - 25; i <= x + 25; ++i) {
                        for (int j = y - 25; j <= y + 25; ++j) {
                            for (int k = z - 25; k <= z + 25; ++k) {
                                BlockPos blockPos = new BlockPos(i, j, k);
                                if (!this.isBed(blockPos)) continue;
                                this.bedPos = blockPos;
                                ChatUtil.sendFormatted(String.format("%s%s: &fWhitelisted your bed at (%d, %d, %d) &a&l\u2714&r", Myau.clientName, this.getName(), this.bedPos.func_177958_n(), this.bedPos.func_177956_o(), this.bedPos.func_177952_p()));
                                SoundUtil.playSound("note.pling");
                                return;
                            }
                        }
                    }
                }, 3000L, TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public void onDisabled() {
        this.alertCooldowns.clear();
        this.trackedPearls.clear();
        this.whitelistedPlayers.clear();
        this.bedPos = null;
    }
}

