package myau.module.modules;

import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.AttackEvent;
import myau.events.PacketEvent;
import myau.events.Render3DEvent;
import myau.events.TickEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.util.PacketUtil;
import myau.dependencias.Cold;
import myau.dependencias.TimedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Backtrack extends Module {

    private final IntProperty minLatency = new IntProperty("min-latency", 50, 10, 1000);
    private final IntProperty maxLatency = new IntProperty("max-latency", 100, 10, 1000);
    private final FloatProperty minDistance = new FloatProperty("min-distance", 0.0F, 0.0F, 3.0F);
    private final FloatProperty maxDistance = new FloatProperty("max-distance", 6.0F, 0.0F, 10.0F);
    private final IntProperty stopOnTargetHurtTime = new IntProperty("stop-on-target-hurttime", 10, -1, 10);
    private final IntProperty stopOnSelfHurtTime = new IntProperty("stop-on-self-hurttime", 10, -1, 10);
    private final BooleanProperty drawRealPosition = new BooleanProperty("draw-real-position", true);

    private final Queue<TimedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private final List<Packet<?>> skipPackets = new ArrayList<>();

    // Simple internal animation handling
    private SimpleAnimation animationX;
    private SimpleAnimation animationY;
    private SimpleAnimation animationZ;

    private Vec3 realTargetPos;
    private EntityPlayer target;
    private int currentLatency = 0;

    public Backtrack() {
        super("Backtrack", false);
    }

    @Override
    public String[] getSuffix() {
        return new String[]{(currentLatency == 0 ? maxLatency.getValue() : currentLatency) + "ms"};
    }

    @Override
    public void verifyValue(String mode) {
        if (minLatency.getName().equals(mode) && minLatency.getValue() > maxLatency.getValue()) {
            maxLatency.setValue(minLatency.getValue());
        } else if (maxLatency.getName().equals(mode) && maxLatency.getValue() < minLatency.getValue()) {
            minLatency.setValue(maxLatency.getValue());
        }

        if (minDistance.getName().equals(mode) && minDistance.getValue() > maxDistance.getValue()) {
            maxDistance.setValue(minDistance.getValue());
        } else if (maxDistance.getName().equals(mode) && maxDistance.getValue() < minDistance.getValue()) {
            minDistance.setValue(maxDistance.getValue());
        }
    }

    @Override
    public void onEnabled() {
        packetQueue.clear();
        skipPackets.clear();
        realTargetPos = null;
        target = null;
    }

    @Override
    public void onDisabled() {
        releaseAll();
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getType() != EventType.PRE) return;

        // Logic to clear latency if target is out of range
        if (target != null && realTargetPos != null) {
            try {
                final double distance = realTargetPos.distanceTo(Minecraft.getMinecraft().thePlayer.getPositionVector());
                if (distance > maxDistance.getValue() || distance < minDistance.getValue()) {
                    currentLatency = 0;
                }
            } catch (Exception ignored) {}
        }

        // Process Packet Queue
        while (!packetQueue.isEmpty()) {
            try {
                TimedPacket timedPacket = packetQueue.element();
                if (timedPacket.getCold().getCum(currentLatency)) {
                    Packet<?> packet = packetQueue.remove().getPacket();
                    skipPackets.add(packet);
                    PacketUtil.receivePacket(packet);
                } else {
                    break;
                }
            } catch (Exception ignored) {
                packetQueue.poll(); // Safety removal
            }
        }

        if (packetQueue.isEmpty() && target != null) {
            realTargetPos = new Vec3(target.posX, target.posY, target.posZ);
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (target == null || realTargetPos == null || target.isDead || !drawRealPosition.getValue())
            return;

        final Vec3 currentPos = currentLatency > 0 ? realTargetPos : target.getPositionVector();

        if (animationX == null || animationY == null || animationZ == null) {
            animationX = new SimpleAnimation(currentPos.xCoord);
            animationY = new SimpleAnimation(currentPos.yCoord);
            animationZ = new SimpleAnimation(currentPos.zCoord);
        }

        // Animate towards the current packet position
        animationX.animate(currentPos.xCoord, 0.3);
        animationY.animate(currentPos.yCoord, 0.3);
        animationZ.animate(currentPos.zCoord, 0.3);

        double x = animationX.getValue() - Minecraft.getMinecraft().getRenderManager().viewerPosX;
        double y = animationY.getValue() - Minecraft.getMinecraft().getRenderManager().viewerPosY;
        double z = animationZ.getValue() - Minecraft.getMinecraft().getRenderManager().viewerPosZ;

        // Draw Box
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.color(0.28F, 0.49F, 0.89F, 0.3F); // Color 72, 125, 227

        double width = target.getEntityBoundingBox().maxX - target.getEntityBoundingBox().minX;
        double height = target.getEntityBoundingBox().maxY - target.getEntityBoundingBox().minY;

        AxisAlignedBB bb = new AxisAlignedBB(x - width / 2, y, z - width / 2, x + width / 2, y + height, z + width / 2);
        RenderGlobal.drawSelectionBoundingBox(bb);

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (event.getTarget() instanceof EntityPlayer) {
            EntityPlayer newTarget = (EntityPlayer) event.getTarget();
            Vec3 targetPos = newTarget.getPositionVector();

            if (target == null || newTarget != target) {
                realTargetPos = targetPos;
                if (animationX != null) {
                    animationX.setValue(targetPos.xCoord);
                    animationY.setValue(targetPos.yCoord);
                    animationZ.setValue(targetPos.zCoord);
                }
            }
            target = newTarget;

            double distance = targetPos.distanceTo(Minecraft.getMinecraft().thePlayer.getPositionVector());
            if (distance > maxDistance.getValue() || distance < minDistance.getValue())
                return;

            // Randomize latency within bounds
            double min = minLatency.getValue();
            double max = maxLatency.getValue();
            currentLatency = (int) (Math.random() * (max - min) + min);
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) return;

        // Only handle Receive packets
        if (event.isSending()) return;

        Packet<?> p = event.getPacket();

        if (skipPackets.contains(p)) {
            skipPackets.remove(p);
            return;
        }

        // Logic checks
        if (target != null && stopOnTargetHurtTime.getValue() != -1 && target.hurtTime == stopOnTargetHurtTime.getValue()) {
            releaseAll();
            return;
        }
        if (stopOnSelfHurtTime.getValue() != -1 && Minecraft.getMinecraft().thePlayer.hurtTime == stopOnSelfHurtTime.getValue()) {
            releaseAll();
            return;
        }

        try {
            if (Minecraft.getMinecraft().thePlayer.ticksExisted < 20) {
                packetQueue.clear();
                return;
            }

            if (target == null) {
                releaseAll();
                return;
            }

            if (p instanceof S19PacketEntityStatus
                    || p instanceof S02PacketChat
                    || p instanceof S0BPacketAnimation
                    || p instanceof S06PacketUpdateHealth) {
                return;
            }

            if (p instanceof S08PacketPlayerPosLook || p instanceof S40PacketDisconnect) {
                releaseAll();
                target = null;
                realTargetPos = null;
                return;
            } else if (p instanceof S13PacketDestroyEntities) {
                S13PacketDestroyEntities wrapper = (S13PacketDestroyEntities) p;
                for (int id : wrapper.getEntityIDs()) {
                    if (id == target.getEntityId()) {
                        target = null;
                        realTargetPos = null;
                        releaseAll();
                        return;
                    }
                }
            } else if (p instanceof S14PacketEntity) {
                S14PacketEntity wrapper = (S14PacketEntity) p;
                Entity entity = wrapper.getEntity(Minecraft.getMinecraft().theWorld);
                if (entity != null && entity.getEntityId() == target.getEntityId()) {
                    realTargetPos = realTargetPos.addVector(wrapper.func_149062_c() / 32.0D, wrapper.func_149061_d() / 32.0D, wrapper.func_149064_e() / 32.0D);
                }
            } else if (p instanceof S18PacketEntityTeleport) {
                S18PacketEntityTeleport wrapper = (S18PacketEntityTeleport) p;
                if (wrapper.getEntityId() == target.getEntityId()) {
                    realTargetPos = new Vec3(wrapper.getX() / 32.0D, wrapper.getY() / 32.0D, wrapper.getZ() / 32.0D);
                }
            }

            // Queue the packet and cancel original processing
            packetQueue.add(new TimedPacket(p));
            event.setCancelled(true);

        } catch (Exception ignored) {}
    }

    private void releaseAll() {
        if (!packetQueue.isEmpty()) {
            for (TimedPacket timedPacket : packetQueue) {
                Packet<?> packet = timedPacket.getPacket();
                skipPackets.add(packet);
                PacketUtil.receivePacket(packet);
            }
            packetQueue.clear();
        }
    }

    // Inner class for Simple Animation to avoid external dependencies
    private static class SimpleAnimation {
        private double value;

        public SimpleAnimation(double value) {
            this.value = value;
        }

        public void animate(double target, double speed) {
            this.value = this.value + (target - this.value) * speed;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }
    }
}