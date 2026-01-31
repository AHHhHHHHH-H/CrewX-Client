package myau.module.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.UpdateEvent;
import myau.module.Module;
import myau.module.Category;
import myau.property.properties.BooleanProperty;
import myau.property.properties.FloatProperty;
import myau.property.properties.TextProperty;
import myau.util.ChatUtil;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class Insults extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private final Map<EntityLivingBase, Long> attackedEntities = new HashMap<EntityLivingBase, Long>();
    public final FloatProperty delay;
    private final TimerUtil delayTimer = new TimerUtil();
    public final BooleanProperty enabled = new BooleanProperty("enabled", Boolean.valueOf(true));
    public final TextProperty messages;

    public Insults() {
        super("Insults", Category.RENDER);
        this.delay = new FloatProperty("delay", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(10.0f));
        this.messages = new TextProperty("messages", "/g Achei fácil viu %s Ruimzao kkkkkkkkkkkk\n/g ChatGPT me informe uma pessoa pior que o %s\n/g O bullying não foi evitado no seu caso %s\n/g Não sabia que o %s tinha um recorde mundial na speedrun de perder o pai\n/g Tranque sua porta antes que eu cague no seu tapete %s\n/g Sempre soube que você tinha medo de uma carteira de trabalho %s\n/g %s Qual foi a última vez que você encostou numa mulher?\n/g %s jogador de free fire no Minecraft Puta Merda\n/g Um aleijado joga melhor que você %s\n/g Até o Boru se sentiria triste de te ver jogando assim... %s\n/g %s, a sua mãe é tão gorda que as pessoas fazem cooper em volta dela!\n/g Você é o tipo de pessoa que usa foto de anime no Discord? XD kkkkkkkkkk\n/g %s não se esqueça do /report 'staff staff bane esse hacker ele tá me atrapalhando!!!!'\n/g trans tem mais direito quê você %s\n/g %s falar 'mds' não vai te ajudar.\n/g %s sabia quê quem faz cheats já garante até emprego em empresas de programação? E você aí sendo CLT pra comprar VIP.\n/g Cris > %s failed inteligence (Prediction) X78.57\n/g Sério %s? Desde quando você aprendeu a xingar.\n/g Até mulheres de 1560 têm mais direitos quê você %s\n/g %s você é a razão de eu odiar LGBTS.\n/g %s tá jogando com mod de habilidade... pena que não instalou o de inteligência.\n/g %s foi visto pesquisando VAPE V4 CRACKED no YouTube viu!!!\n/g %s Não colega, mandar Tell me web ofendendo não vai adiantar.\n/g Até o Creeper tem mais controle emocional que %s.\n/g %s gosta de rebolar lentinho prós cria\n/g Faz o L aí otário %s.\n/g %s tá tão bugado que parece um NPC tentando ser relevante.\n/g %s Quando for me denunciar no fórum não se esqueça de falar pra aleesia que ela é trans!!!!!\n/g %s calaboca e faz o L otário\n/g %s Pinte o cabelo de rosa e vá até o twitter criticar o anti cheat do MushMC");
    }

    private void sendTrashTalkMessage(String targetName) {
        if (!((Boolean)this.enabled.getValue()).booleanValue()) {
            return;
        }
        if (!this.delayTimer.hasTimeElapsed((long)(((Float)this.delay.getValue()).floatValue() * 1000.0f))) {
            return;
        }
        
        String messagesValue = (String)this.messages.getValue();
        String[] messageArray = messagesValue.split("\n");
        ArrayList<String> validMessages = new ArrayList<String>();
        
        for (String msg : messageArray) {
            if (!msg.trim().isEmpty()) {
                validMessages.add(msg.trim());
            }
        }
        
        if (!validMessages.isEmpty()) {
            String selected = validMessages.get(new Random().nextInt(validMessages.size()));
            String finalMsg = selected.replace("%s", targetName);
            ChatUtil.sendMessage(finalMsg);
            this.delayTimer.reset();
        }
    }

    public void onDisabled() {
        this.attackedEntities.clear();
    }

    public void onEnabled() {
        this.attackedEntities.clear();
        this.delayTimer.reset();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            ArrayList<EntityLivingBase> toRemove = new ArrayList<EntityLivingBase>();
            
            for (Map.Entry<EntityLivingBase, Long> entry : this.attackedEntities.entrySet()) {
                EntityLivingBase entity = entry.getKey();
                if (entity.field_70128_L || entity.field_70725_aQ > 0) {
                    if (entity instanceof EntityPlayer) {
                        this.sendTrashTalkMessage(entity.func_70005_c_());
                    }
                    toRemove.add(entity);
                    continue;
                }
                if (!mc.field_71441_e.field_72996_f.contains(entity)) {
                    toRemove.add(entity);
                }
            }
            
            for (EntityLivingBase entity : toRemove) {
                this.attackedEntities.remove(entity);
            }
            
            if (mc.field_71439_g != null && mc.field_71476_x != null && mc.field_71476_x.field_72308_g instanceof EntityLivingBase) {
                EntityLivingBase target = (EntityLivingBase)mc.field_71476_x.field_72308_g;
                if (target instanceof EntityPlayer && !this.attackedEntities.containsKey(target)) {
                    this.attackedEntities.put(target, System.currentTimeMillis());
                }
            }
        }
    }
}
