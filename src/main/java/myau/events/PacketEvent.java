package myau.events;

import myau.event.events.callables.EventCancellable;
import myau.event.types.EventType;
import net.minecraft.network.Packet;

public class PacketEvent extends EventCancellable {

    private final EventType type;
    private final Packet<?> packet;

    public PacketEvent(EventType type, Packet<?> packet) {
        this.type = type;
        this.packet = packet;
    }

    public EventType getType() {
        return this.type;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }

    // Continua igual (Backtrack usa isso)
    public boolean isSending() {
        return this.type == EventType.PRE;
    }

    public boolean isReceiving() {
        return this.type == EventType.POST;
    }

    /**
     * 🔴 ISSO SALVA SEU SSD
     * Backtrack só pode tocar em S->C
     */
    public boolean isServerPacket() {
        return packet.getClass().getName()
                .startsWith("net.minecraft.network.play.server");
    }
}
