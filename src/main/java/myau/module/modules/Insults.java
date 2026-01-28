/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.play.server.S02PacketChat
 */
package myau.module.modules;

import myau.event.EventTarget;
import myau.events.PacketEvent;
import myau.events.Render2DEvent;
import myau.module.Module;
import myau.property.properties.FloatProperty;
import myau.property.properties.IntProperty;
import myau.property.properties.TextProperty;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S02PacketChat;

public class Insults
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final TimerUtil timer = new TimerUtil();
    private int charOffset = 19968;
    public final TextProperty text = new TextProperty("text", "meow");
    public final FloatProperty delay = new FloatProperty("delay", Float.valueOf(3.5f), Float.valueOf(0.0f), Float.valueOf(3600.0f));
    public final IntProperty random = new IntProperty("random", 0, 0, 10);

    public Insults() {
        super("Insults", false);
    }

    @EventTarget
    public void onRender(Render2DEvent render2DEvent) {
    }

    @EventTarget
    public void onPacket(PacketEvent packetEvent) {
        if (this.isEnabled() && packetEvent.getPacket() instanceof S02PacketChat) {
            String string;
            Object object = (S02PacketChat)packetEvent.getPacket();
            String string2 = object.func_148915_c().func_150260_c();
            String string3 = Insults.mc.field_71439_g.func_70005_c_();
            if ((string2.contains("morreu para ") || string2.contains("morreu no void por ") || string2.contains("foi jogado no void por ") || string2.contains("foi morto por ") || string2.contains("foi empurrado para o void por ")) && string2.contains(string3) && !(string = string2.split(" ")[0]).equalsIgnoreCase(string3)) {
                int n = (int)(System.nanoTime() % 16L);
                if (n == 0) {
                    String string4 = String.format("/g [%s] N\u00e3o se esque\u00e7a de se inscrever em @CrowlyAC @VioleetHax & Romeu no YouTube!", string);
                    Insults.mc.field_71439_g.func_71165_d(string4);
                } else if (n == 1) {
                    object = String.format("/g T\u00e1 bom %s antes que reclame a Crack CREW ir\u00e1 te fazer ganhar v\u00e1rias ( Ao menos que tenha Q.I )", string);
                    Insults.mc.field_71439_g.func_71165_d((String)object);
                } else if (n == 2) {
                    object = String.format("/g CrewX Client mandou um abra\u00e7o %s", string);
                    Insults.mc.field_71439_g.func_71165_d((String)object);
                } else if (n == 3) {
                    object = String.format("/g \"Mam\u00e3e me d\u00e1 dinheiro pra eu comprar vip no mush pro meu personagem chamado %s\"", string);
                    Insults.mc.field_71439_g.func_71165_d((String)object);
                } else if (n == 4) {
                    object = String.format("/g MOGGED %s", string);
                    Insults.mc.field_71439_g.func_71165_d((String)object);
                } else if (n == 5) {
                    object = String.format("/g Opera\u00e7\u00e3o do rio de janeiro n\u00e3o deu t\u00e3o certo.. Acabei de matar outro favelado %s", string);
                    Insults.mc.field_71439_g.func_71165_d((String)object);
                } else if (n == 6) {
                    object = String.format("/g Favelado achando que vai ter futuro no Minecraft kkkkk desista %s", string);
                    Insults.mc.field_71439_g.func_71165_d((String)object);
                } else if (n == 7) {
                    object = String.format("/g Quer fazer a mesma coisa que eu acabei de fazer com voc\u00ea? %s .gg-crackcrew", string);
                    Insults.mc.field_71439_g.func_71165_d((String)object);
                } else if (n == 8) {
                    object = String.format("/g Por favor cometa su|c\u00eddio %s", string);
                    Insults.mc.field_71439_g.func_71165_d((String)object);
                } else if (n == 9) {
                    object = String.format("/g %s Quando for reclamar no Discord n\u00e3o se esque\u00e7a \"O hacker estava usando CrewX Client\"", string);
                    Insults.mc.field_71439_g.func_71165_d((String)object);
                } else if (n == 10) {
                    object = String.format("/g Voc\u00ea e a raz\u00e3o de eu odiar LGBTS %s", string);
                    Insults.mc.field_71439_g.func_71165_d((String)object);
                } else if (n == 11) {
                    object = String.format("/g At\u00e9 a tran-s da Alessia sentiu vergonha de voc\u00ea %s", string);
                    Insults.mc.field_71439_g.func_71165_d((String)object);
                } else if (n == 12) {
                    object = String.format("/g Estrup&m esse neguinho %s", string);
                    Insults.mc.field_71439_g.func_71165_d((String)object);
                } else if (n == 13) {
                    object = String.format("/g Voc\u00ea \u00e9 ruim %s lembre-se disso pelo resto da sua vida", string);
                    Insults.mc.field_71439_g.func_71165_d((String)object);
                } else if (n == 14) {
                    object = String.format("/g Agora ferrou 500 cavalos g0zand na boca do %s", string);
                    Insults.mc.field_71439_g.func_71165_d((String)object);
                } else if (n == 15) {
                    object = String.format("/g %s Pinte o cabelo de rosa e v\u00e1 at\u00e9 o twitt-er criticar o anti cheat do MushMC", string);
                    Insults.mc.field_71439_g.func_71165_d((String)object);
                }
            }
        }
    }
}

