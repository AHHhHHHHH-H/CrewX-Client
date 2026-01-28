/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.passive.EntityVillager
 *  net.minecraft.util.BlockPos
 */
package myau.module.modules;

import myau.Myau;
import myau.enums.FloatModules;
import myau.event.EventTarget;
import myau.events.LivingUpdateEvent;
import myau.events.PlayerUpdateEvent;
import myau.events.RightClickMouseEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ModeProperty;
import myau.property.properties.PercentProperty;
import myau.util.BlockUtil;
import myau.util.ItemUtil;
import myau.util.PlayerUtil;
import myau.util.TeamUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.BlockPos;

public class NoSlow
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private int lastSlot = -1;
    public final ModeProperty swordMode = new ModeProperty("sword-mode", 1, new String[]{"NONE", "VANILLA"});
    public final PercentProperty swordMotion = new PercentProperty("sword-motion", 100, () -> (Integer)this.swordMode.getValue() != 0);
    public final BooleanProperty swordSprint = new BooleanProperty("sword-sprint", true, () -> (Integer)this.swordMode.getValue() != 0);
    public final ModeProperty foodMode = new ModeProperty("food-mode", 0, new String[]{"NONE", "VANILLA", "FLOAT"});
    public final PercentProperty foodMotion = new PercentProperty("food-motion", 100, () -> (Integer)this.foodMode.getValue() != 0);
    public final BooleanProperty foodSprint = new BooleanProperty("food-sprint", true, () -> (Integer)this.foodMode.getValue() != 0);
    public final ModeProperty bowMode = new ModeProperty("bow-mode", 0, new String[]{"NONE", "VANILLA", "FLOAT"});
    public final PercentProperty bowMotion = new PercentProperty("bow-motion", 100, () -> (Integer)this.bowMode.getValue() != 0);
    public final BooleanProperty bowSprint = new BooleanProperty("bow-sprint", true, () -> (Integer)this.bowMode.getValue() != 0);

    public NoSlow() {
        super("NoSlow", false);
    }

    public boolean isSwordActive() {
        return (Integer)this.swordMode.getValue() != 0 && ItemUtil.isHoldingSword();
    }

    public boolean isFoodActive() {
        return (Integer)this.foodMode.getValue() != 0 && ItemUtil.isEating();
    }

    public boolean isBowActive() {
        return (Integer)this.bowMode.getValue() != 0 && ItemUtil.isUsingBow();
    }

    public boolean isFloatMode() {
        return (Integer)this.foodMode.getValue() == 2 && ItemUtil.isEating() || (Integer)this.bowMode.getValue() == 2 && ItemUtil.isUsingBow();
    }

    public boolean isAnyActive() {
        return NoSlow.mc.field_71439_g.func_71039_bw() && (this.isSwordActive() || this.isFoodActive() || this.isBowActive());
    }

    public boolean canSprint() {
        return this.isSwordActive() && (Boolean)this.swordSprint.getValue() != false || this.isFoodActive() && (Boolean)this.foodSprint.getValue() != false || this.isBowActive() && (Boolean)this.bowSprint.getValue() != false;
    }

    public int getMotionMultiplier() {
        if (ItemUtil.isHoldingSword()) {
            return (Integer)this.swordMotion.getValue();
        }
        if (ItemUtil.isEating()) {
            return (Integer)this.foodMotion.getValue();
        }
        return ItemUtil.isUsingBow() ? (Integer)this.bowMotion.getValue() : 100;
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.isEnabled() && this.isAnyActive()) {
            float multiplier = (float)this.getMotionMultiplier() / 100.0f;
            NoSlow.mc.field_71439_g.field_71158_b.field_78900_b *= multiplier;
            NoSlow.mc.field_71439_g.field_71158_b.field_78902_a *= multiplier;
            if (!this.canSprint()) {
                NoSlow.mc.field_71439_g.func_70031_b(false);
            }
        }
    }

    @EventTarget(value=3)
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (this.isEnabled() && this.isFloatMode()) {
            int item = NoSlow.mc.field_71439_g.field_71071_by.field_70461_c;
            if (this.lastSlot != item && PlayerUtil.isUsingItem()) {
                this.lastSlot = item;
                Myau.floatManager.setFloatState(true, FloatModules.NO_SLOW);
            }
        } else {
            this.lastSlot = -1;
            Myau.floatManager.setFloatState(false, FloatModules.NO_SLOW);
        }
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event) {
        if (this.isEnabled()) {
            if (NoSlow.mc.field_71476_x != null) {
                switch (NoSlow.mc.field_71476_x.field_72313_a) {
                    case BLOCK: {
                        BlockPos blockPos = NoSlow.mc.field_71476_x.func_178782_a();
                        if (!BlockUtil.isInteractable(blockPos) || PlayerUtil.isSneaking()) break;
                        return;
                    }
                    case ENTITY: {
                        Entity entityHit = NoSlow.mc.field_71476_x.field_72308_g;
                        if (entityHit instanceof EntityVillager) {
                            return;
                        }
                        if (!(entityHit instanceof EntityLivingBase) || !TeamUtil.isShop((EntityLivingBase)entityHit)) break;
                        return;
                    }
                }
            }
            if (this.isFloatMode() && !Myau.floatManager.isPredicted() && NoSlow.mc.field_71439_g.field_70122_E) {
                event.setCancelled(true);
                NoSlow.mc.field_71439_g.field_70181_x = 0.42f;
            }
        }
    }
}

