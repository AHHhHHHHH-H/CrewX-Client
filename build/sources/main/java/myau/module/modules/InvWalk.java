/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.CaseFormat
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.inventory.GuiContainer
 *  net.minecraft.client.gui.inventory.GuiContainerCreative
 *  net.minecraft.client.gui.inventory.GuiInventory
 *  net.minecraft.client.settings.KeyBinding
 *  net.minecraft.inventory.ContainerPlayer
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.Packet
 *  net.minecraft.network.play.client.C0DPacketCloseWindow
 *  net.minecraft.network.play.client.C0EPacketClickWindow
 *  net.minecraft.network.play.client.C16PacketClientStatus
 *  net.minecraft.network.play.client.C16PacketClientStatus$EnumState
 */
package myau.module.modules;

import com.google.common.base.CaseFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.PacketEvent;
import myau.events.TickEvent;
import myau.events.UpdateEvent;
import myau.mixin.IAccessorC0DPacketCloseWindow;
import myau.module.Module;
import myau.module.modules.Sprint;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.ModeProperty;
import myau.ui.ClickGui;
import myau.util.KeyBindUtil;
import myau.util.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;

public class InvWalk
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final Queue<C0EPacketClickWindow> clickQueue = new ConcurrentLinkedQueue<C0EPacketClickWindow>();
    private boolean keysPressed = false;
    private C16PacketClientStatus pendingStatus = null;
    private int delayTicks = 0;
    private int openDelayTicks = -1;
    private int closeDelayTicks = -1;
    private final Map<KeyBinding, Boolean> movementKeys = new HashMap<KeyBinding, Boolean>(8){
        {
            this.put(mc.field_71474_y.field_74351_w, false);
            this.put(mc.field_71474_y.field_74368_y, false);
            this.put(mc.field_71474_y.field_74370_x, false);
            this.put(mc.field_71474_y.field_74366_z, false);
            this.put(mc.field_71474_y.field_74314_A, false);
            this.put(mc.field_71474_y.field_74311_E, false);
            this.put(mc.field_71474_y.field_151444_V, false);
        }
    };
    public final ModeProperty mode = new ModeProperty("mode", 1, new String[]{"VANILLA", "LEGIT", "HYPIXEL", "LEGIT+"});
    public final BooleanProperty guiEnabled = new BooleanProperty("click-gui", true);
    public final IntProperty openDelay = new IntProperty("open-delay", 0, 0, 20, () -> (Integer)this.mode.getValue() == 3);
    public final IntProperty closeDelay = new IntProperty("close-delay", 4, 0, 20, () -> (Integer)this.mode.getValue() == 3);
    public final BooleanProperty lockMoveKey = new BooleanProperty("lock-move-dey", false);

    public InvWalk() {
        super("InvWalk", false);
    }

    public void pressMovementKeys(boolean skipSneak) {
        this.movementKeys.keySet().stream().filter(key -> !skipSneak || key != InvWalk.mc.field_71474_y.field_74311_E).forEach(key -> KeyBindUtil.updateKeyState(key.func_151463_i()));
        if (Myau.moduleManager.modules.get(Sprint.class).isEnabled()) {
            KeyBindUtil.setKeyBindState(InvWalk.mc.field_71474_y.field_151444_V.func_151463_i(), true);
        }
        this.keysPressed = true;
    }

    public void resetMovementKeys() {
        this.movementKeys.replaceAll((k, v) -> false);
    }

    public boolean isSetMovementKeys() {
        return this.movementKeys.values().stream().anyMatch(Boolean::booleanValue);
    }

    public void storeMovementKeys() {
        this.movementKeys.replaceAll((k, v) -> KeyBindUtil.isKeyDown(k.func_151463_i()));
    }

    public void restoreMovementKeys() {
        for (Map.Entry<KeyBinding, Boolean> keyBinding : this.movementKeys.entrySet()) {
            KeyBindUtil.setKeyBindState(keyBinding.getKey().func_151463_i(), keyBinding.getValue());
        }
        if (Myau.moduleManager.modules.get(Sprint.class).isEnabled()) {
            KeyBindUtil.setKeyBindState(InvWalk.mc.field_71474_y.field_151444_V.func_151463_i(), true);
        }
        this.keysPressed = true;
    }

    public boolean canInvWalk() {
        if (!(InvWalk.mc.field_71462_r instanceof GuiContainer)) {
            return false;
        }
        if (InvWalk.mc.field_71462_r instanceof GuiContainerCreative) {
            return false;
        }
        switch ((Integer)this.mode.getValue()) {
            case 0: {
                return true;
            }
            case 1: {
                if (!(InvWalk.mc.field_71462_r instanceof GuiInventory)) {
                    return false;
                }
                return this.pendingStatus != null && this.clickQueue.isEmpty();
            }
            case 2: {
                return this.delayTicks == 0 && this.clickQueue.isEmpty();
            }
            case 3: {
                if (!(InvWalk.mc.field_71462_r instanceof GuiInventory)) {
                    return false;
                }
                return this.closeDelayTicks == -1 && this.clickQueue.isEmpty();
            }
        }
        return false;
    }

    public boolean temporaryStackIsEmpty() {
        if (InvWalk.mc.field_71439_g.field_71071_by.func_70445_o() != null) {
            return false;
        }
        if (InvWalk.mc.field_71439_g.field_71069_bz instanceof ContainerPlayer) {
            ContainerPlayer containerPlayer = (ContainerPlayer)InvWalk.mc.field_71439_g.field_71069_bz;
            for (int i = 0; i < containerPlayer.field_75181_e.func_70302_i_(); ++i) {
                ItemStack stack = containerPlayer.field_75181_e.func_70301_a(i);
                if (stack == null) continue;
                return false;
            }
        }
        return true;
    }

    @EventTarget(value=4)
    public void onTick(TickEvent event) {
        if (event.getType() == EventType.PRE) {
            if (this.openDelayTicks >= 0) {
                --this.openDelayTicks;
                return;
            }
            while (!this.clickQueue.isEmpty()) {
                PacketUtil.sendPacketNoEvent((Packet)this.clickQueue.poll());
            }
            if (this.closeDelayTicks > 0) {
                if (this.temporaryStackIsEmpty()) {
                    --this.closeDelayTicks;
                }
            } else if (this.closeDelayTicks == 0) {
                if (InvWalk.mc.field_71462_r instanceof GuiInventory) {
                    PacketUtil.sendPacketNoEvent(new C0DPacketCloseWindow(0));
                }
                this.closeDelayTicks = -1;
            }
        }
    }

    @EventTarget(value=4)
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled() || event.getType() != EventType.PRE) {
            return;
        }
        if (InvWalk.mc.field_71462_r instanceof ClickGui && ((Boolean)this.guiEnabled.getValue()).booleanValue()) {
            this.pressMovementKeys(true);
            return;
        }
        if (this.canInvWalk()) {
            if (this.isSetMovementKeys() && ((Boolean)this.lockMoveKey.getValue()).booleanValue()) {
                this.restoreMovementKeys();
            } else {
                this.pressMovementKeys(true);
            }
        } else {
            if (this.keysPressed) {
                if (InvWalk.mc.field_71462_r != null) {
                    KeyBinding.func_74506_a();
                } else if (this.isSetMovementKeys()) {
                    this.resetMovementKeys();
                    this.pressMovementKeys(false);
                }
                this.keysPressed = false;
            }
            if (this.pendingStatus != null) {
                PacketUtil.sendPacketNoEvent(this.pendingStatus);
                this.pendingStatus = null;
            }
            if (this.delayTicks > 0) {
                --this.delayTicks;
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!this.isEnabled() || event.getType() != EventType.SEND) {
            return;
        }
        if (event.getPacket() instanceof C16PacketClientStatus) {
            C16PacketClientStatus packet;
            this.storeMovementKeys();
            if (((Integer)this.mode.getValue() == 1 || (Integer)this.mode.getValue() == 3) && (packet = (C16PacketClientStatus)event.getPacket()).func_149435_c() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
                event.setCancelled(true);
                if ((Integer)this.mode.getValue() == 1) {
                    this.pendingStatus = packet;
                }
            }
        } else if (!(event.getPacket() instanceof C0EPacketClickWindow)) {
            if (event.getPacket() instanceof C0DPacketCloseWindow) {
                C0DPacketCloseWindow packet = (C0DPacketCloseWindow)event.getPacket();
                if (((IAccessorC0DPacketCloseWindow)packet).getWindowId() == 0) {
                    if ((Integer)this.mode.getValue() == 3) {
                        if (!this.clickQueue.isEmpty()) {
                            this.clickQueue.clear();
                        }
                        if (this.openDelayTicks >= 0) {
                            this.openDelayTicks = -1;
                        }
                        if (this.closeDelayTicks >= 0) {
                            this.closeDelayTicks = -1;
                        } else {
                            event.setCancelled(true);
                        }
                    } else if (this.pendingStatus != null) {
                        this.pendingStatus = null;
                        event.setCancelled(true);
                    }
                } else {
                    if (!this.clickQueue.isEmpty()) {
                        this.clickQueue.clear();
                    }
                    if (this.openDelayTicks >= 0) {
                        this.openDelayTicks = -1;
                    }
                    if (this.closeDelayTicks >= 0) {
                        this.closeDelayTicks = -1;
                    }
                }
            }
        } else {
            C0EPacketClickWindow packet = (C0EPacketClickWindow)event.getPacket();
            switch ((Integer)this.mode.getValue()) {
                case 1: {
                    if (packet.func_149548_c() != 0) break;
                    if ((packet.func_149542_h() == 3 || packet.func_149542_h() == 4) && packet.func_149544_d() == -999) {
                        event.setCancelled(true);
                        return;
                    }
                    if (this.pendingStatus == null) break;
                    KeyBinding.func_74506_a();
                    event.setCancelled(true);
                    this.clickQueue.offer(packet);
                    break;
                }
                case 2: {
                    if ((packet.func_149542_h() == 3 || packet.func_149542_h() == 4) && packet.func_149544_d() == -999) {
                        event.setCancelled(true);
                        break;
                    }
                    KeyBinding.func_74506_a();
                    event.setCancelled(true);
                    this.clickQueue.offer(packet);
                    this.delayTicks = 8;
                    break;
                }
                case 3: {
                    if (packet.func_149548_c() != 0) break;
                    if ((packet.func_149542_h() == 3 || packet.func_149542_h() == 4) && packet.func_149544_d() == -999) {
                        event.setCancelled(true);
                        return;
                    }
                    KeyBinding.func_74506_a();
                    event.setCancelled(true);
                    this.clickQueue.offer(packet);
                    if (this.closeDelayTicks < 0 && this.openDelayTicks < 0) {
                        this.pendingStatus = new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT);
                        this.openDelayTicks = (Integer)this.openDelay.getValue();
                    }
                    this.closeDelayTicks = (Integer)this.closeDelay.getValue();
                }
            }
            if (this.pendingStatus != null) {
                PacketUtil.sendPacketNoEvent(this.pendingStatus);
                this.pendingStatus = null;
            }
        }
    }

    @Override
    public void onDisabled() {
        if (this.keysPressed) {
            if (InvWalk.mc.field_71462_r != null) {
                KeyBinding.func_74506_a();
            }
            this.keysPressed = false;
        }
        if (this.pendingStatus != null) {
            PacketUtil.sendPacketNoEvent(this.pendingStatus);
            this.pendingStatus = null;
        }
        this.delayTicks = 0;
    }

    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
    }
}

