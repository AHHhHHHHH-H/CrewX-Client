/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.inventory.GuiInventory
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.inventory.ContainerPlayer
 *  net.minecraft.item.ItemStack
 *  net.minecraft.world.WorldSettings$GameType
 *  org.apache.commons.lang3.RandomUtils
 */
package myau.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.UpdateEvent;
import myau.events.WindowClickEvent;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.util.ItemUtil;
import myau.util.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldSettings;
import org.apache.commons.lang3.RandomUtils;

public class InvManager
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private int actionDelay = 0;
    private int oDelay = 0;
    private boolean inventoryOpen = false;
    private final TimerUtil autoArmorTime = new TimerUtil();
    public final IntProperty minDelay = new IntProperty("min-delay", 1, 0, 20);
    public final IntProperty maxDelay = new IntProperty("max-delay", 2, 0, 20);
    public final IntProperty openDelay = new IntProperty("open-delay", 1, 0, 20);
    public final BooleanProperty autoArmor = new BooleanProperty("auto-armor", true);
    public final IntProperty autoArmorInterval = new IntProperty("auto-armor-interval", 0, 0, 100, this.autoArmor::getValue);
    public final BooleanProperty dropTrash = new BooleanProperty("drop-trash", false);
    public final BooleanProperty checkDurability = new BooleanProperty("check-durability", true);
    public final IntProperty swordSlot = new IntProperty("sword-slot", 1, 0, 9);
    public final IntProperty pickaxeSlot = new IntProperty("pickaxe-slot", 3, 0, 9);
    public final IntProperty shovelSlot = new IntProperty("shovel-slot", 4, 0, 9);
    public final IntProperty axeSlot = new IntProperty("axe-slot", 5, 0, 9);
    public final IntProperty blocksSlot = new IntProperty("blocks-slot", 2, 0, 9);
    public final IntProperty blocks = new IntProperty("blocks", 128, 64, 2304);
    public final IntProperty projectileSlot = new IntProperty("projectile-slot", 7, 0, 9);
    public final IntProperty projectiles = new IntProperty("projectiles", 64, 16, 2304);
    public final IntProperty goldAppleSlot = new IntProperty("gold-apple-slot", 9, 0, 9);
    public final IntProperty arrow = new IntProperty("arrow", 256, 0, 2304);
    public final IntProperty bowSlot = new IntProperty("bow-slot", 8, 0, 9);

    private boolean isValidGameMode() {
        WorldSettings.GameType gameType = InvManager.mc.field_71442_b.func_178889_l();
        return gameType == WorldSettings.GameType.SURVIVAL || gameType == WorldSettings.GameType.ADVENTURE;
    }

    private int convertSlotIndex(int slot) {
        if (slot >= 36) {
            return 8 - (slot - 36);
        }
        return slot <= 8 ? slot + 36 : slot;
    }

    private void clickSlot(int windowId, int slotId, int mouseButtonClicked, int mode) {
        InvManager.mc.field_71442_b.func_78753_a(windowId, slotId, mouseButtonClicked, mode, (EntityPlayer)InvManager.mc.field_71439_g);
    }

    private int getStackSize(int slot) {
        if (slot == -1) {
            return 0;
        }
        ItemStack stack = InvManager.mc.field_71439_g.field_71071_by.func_70301_a(slot);
        return stack != null ? stack.field_77994_a : 0;
    }

    public InvManager() {
        super("InvManager", false);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (event.getType() == EventType.PRE) {
            if (this.actionDelay > 0) {
                --this.actionDelay;
            }
            if (this.oDelay > 0) {
                --this.oDelay;
            }
            if (!(InvManager.mc.field_71462_r instanceof GuiInventory)) {
                this.inventoryOpen = false;
            } else if (!(((GuiInventory)InvManager.mc.field_71462_r).field_147002_h instanceof ContainerPlayer)) {
                this.inventoryOpen = false;
            } else {
                if (!this.inventoryOpen) {
                    this.inventoryOpen = true;
                    this.oDelay = (Integer)this.openDelay.getValue() + 1;
                    this.autoArmorTime.reset();
                }
                if (this.oDelay <= 0 && this.actionDelay <= 0 && this.isEnabled() && this.isValidGameMode()) {
                    int preferredAxeHotbarSlot;
                    int inventoryAxeSlot;
                    int preferredShovelHotbarSlot;
                    int inventoryShovelSlot;
                    int preferredPickaxeHotbarSlot;
                    int inventoryPickaxeSlot;
                    ArrayList<Integer> equippedArmorSlots = new ArrayList<Integer>(Arrays.asList(-1, -1, -1, -1));
                    ArrayList<Integer> inventoryArmorSlots = new ArrayList<Integer>(Arrays.asList(-1, -1, -1, -1));
                    for (int i = 0; i < 4; ++i) {
                        equippedArmorSlots.set(i, ItemUtil.findArmorInventorySlot(i, true));
                        inventoryArmorSlots.set(i, ItemUtil.findArmorInventorySlot(i, false));
                    }
                    int preferredSwordHotbarSlot = (Integer)this.swordSlot.getValue() - 1;
                    int inventorySwordSlot = ItemUtil.findSwordInInventorySlot(preferredSwordHotbarSlot, (Boolean)this.checkDurability.getValue());
                    if (inventorySwordSlot == -1) {
                        inventorySwordSlot = ItemUtil.findSwordInInventorySlot(preferredSwordHotbarSlot, false);
                    }
                    if ((inventoryPickaxeSlot = ItemUtil.findInventorySlot("pickaxe", preferredPickaxeHotbarSlot = (Integer)this.pickaxeSlot.getValue() - 1, (Boolean)this.checkDurability.getValue())) == -1) {
                        inventoryPickaxeSlot = ItemUtil.findInventorySlot("pickaxe", preferredPickaxeHotbarSlot, false);
                    }
                    if ((inventoryShovelSlot = ItemUtil.findInventorySlot("shovel", preferredShovelHotbarSlot = (Integer)this.shovelSlot.getValue() - 1, (Boolean)this.checkDurability.getValue())) == -1) {
                        inventoryShovelSlot = ItemUtil.findInventorySlot("shovel", preferredShovelHotbarSlot, false);
                    }
                    if ((inventoryAxeSlot = ItemUtil.findInventorySlot("axe", preferredAxeHotbarSlot = (Integer)this.axeSlot.getValue() - 1, (Boolean)this.checkDurability.getValue())) == -1) {
                        inventoryAxeSlot = ItemUtil.findInventorySlot("axe", preferredAxeHotbarSlot, false);
                    }
                    int preferredBlocksHotbarSlot = (Integer)this.blocksSlot.getValue() - 1;
                    int inventoryBlocksSlot = ItemUtil.findInventorySlot(preferredBlocksHotbarSlot, ItemUtil.ItemType.Block);
                    int preferredProjectileHotbarSlot = (Integer)this.projectileSlot.getValue() - 1;
                    int inventoryProjectileSlot = ItemUtil.findInventorySlot(preferredProjectileHotbarSlot, ItemUtil.ItemType.Projectile);
                    if (inventoryProjectileSlot == -1) {
                        inventoryProjectileSlot = ItemUtil.findInventorySlot(preferredProjectileHotbarSlot, ItemUtil.ItemType.FishRod);
                    }
                    int preferredGoldAppleHotbarSlot = (Integer)this.goldAppleSlot.getValue() - 1;
                    int inventoryGoldAppleSlot = ItemUtil.findInventorySlot(preferredGoldAppleHotbarSlot, ItemUtil.ItemType.GoldApple);
                    int preferredBowHotbarSlot = (Integer)this.bowSlot.getValue() - 1;
                    int inventoryBowSlot = ItemUtil.findBowInventorySlot(preferredBowHotbarSlot, (Boolean)this.checkDurability.getValue());
                    if (inventoryBowSlot == -1) {
                        inventoryBowSlot = ItemUtil.findBowInventorySlot(preferredBowHotbarSlot, false);
                    }
                    if (((Boolean)this.autoArmor.getValue()).booleanValue() && this.autoArmorTime.hasTimeElapsed((long)((Integer)this.autoArmorInterval.getValue()).intValue() * 50L)) {
                        for (int i = 0; i < 4; ++i) {
                            int playerArmorSlot;
                            int equippedSlot = equippedArmorSlots.get(i);
                            int inventorySlot = inventoryArmorSlots.get(i);
                            if (equippedSlot == -1 && inventorySlot == -1 || equippedSlot == (playerArmorSlot = 39 - i) || inventorySlot == playerArmorSlot) continue;
                            if (InvManager.mc.field_71439_g.field_71071_by.func_70301_a(playerArmorSlot) != null) {
                                if (InvManager.mc.field_71439_g.field_71071_by.func_70447_i() != -1) {
                                    this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(playerArmorSlot), 0, 1);
                                } else {
                                    this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(playerArmorSlot), 1, 4);
                                }
                            } else {
                                int armorToEquipSlot = equippedSlot != -1 ? equippedSlot : inventorySlot;
                                this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(armorToEquipSlot), 0, 1);
                                this.autoArmorTime.reset();
                            }
                            return;
                        }
                    }
                    LinkedHashSet<Integer> usedHotbarSlots = new LinkedHashSet<Integer>();
                    if (preferredSwordHotbarSlot >= 0 && preferredSwordHotbarSlot <= 8 && inventorySwordSlot != -1) {
                        usedHotbarSlots.add(preferredSwordHotbarSlot);
                        if (inventorySwordSlot != preferredSwordHotbarSlot) {
                            this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(inventorySwordSlot), preferredSwordHotbarSlot, 2);
                            return;
                        }
                    }
                    if (preferredPickaxeHotbarSlot >= 0 && preferredPickaxeHotbarSlot <= 8 && !usedHotbarSlots.contains(preferredPickaxeHotbarSlot) && inventoryPickaxeSlot != -1) {
                        usedHotbarSlots.add(preferredPickaxeHotbarSlot);
                        if (inventoryPickaxeSlot != preferredPickaxeHotbarSlot) {
                            this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(inventoryPickaxeSlot), preferredPickaxeHotbarSlot, 2);
                            return;
                        }
                    }
                    if (preferredShovelHotbarSlot >= 0 && preferredShovelHotbarSlot <= 8 && !usedHotbarSlots.contains(preferredShovelHotbarSlot) && inventoryShovelSlot != -1) {
                        usedHotbarSlots.add(preferredShovelHotbarSlot);
                        if (inventoryShovelSlot != preferredShovelHotbarSlot) {
                            this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(inventoryShovelSlot), preferredShovelHotbarSlot, 2);
                            return;
                        }
                    }
                    if (preferredAxeHotbarSlot >= 0 && preferredAxeHotbarSlot <= 8 && !usedHotbarSlots.contains(preferredAxeHotbarSlot) && inventoryAxeSlot != -1) {
                        usedHotbarSlots.add(preferredAxeHotbarSlot);
                        if (inventoryAxeSlot != preferredAxeHotbarSlot) {
                            this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(inventoryAxeSlot), preferredAxeHotbarSlot, 2);
                            return;
                        }
                    }
                    if (preferredBlocksHotbarSlot >= 0 && preferredBlocksHotbarSlot <= 8 && !usedHotbarSlots.contains(preferredBlocksHotbarSlot) && inventoryBlocksSlot != -1) {
                        usedHotbarSlots.add(preferredBlocksHotbarSlot);
                        if (inventoryBlocksSlot != preferredBlocksHotbarSlot) {
                            this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(inventoryBlocksSlot), preferredBlocksHotbarSlot, 2);
                            return;
                        }
                    }
                    if (preferredProjectileHotbarSlot >= 0 && preferredProjectileHotbarSlot <= 8 && !usedHotbarSlots.contains(preferredProjectileHotbarSlot) && inventoryProjectileSlot != -1) {
                        usedHotbarSlots.add(preferredProjectileHotbarSlot);
                        if (inventoryProjectileSlot != preferredProjectileHotbarSlot) {
                            this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(inventoryProjectileSlot), preferredProjectileHotbarSlot, 2);
                            return;
                        }
                    }
                    if (preferredGoldAppleHotbarSlot >= 0 && preferredGoldAppleHotbarSlot <= 8 && !usedHotbarSlots.contains(preferredGoldAppleHotbarSlot) && inventoryGoldAppleSlot != -1) {
                        usedHotbarSlots.add(preferredGoldAppleHotbarSlot);
                        if (inventoryGoldAppleSlot != preferredGoldAppleHotbarSlot) {
                            this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(inventoryGoldAppleSlot), preferredGoldAppleHotbarSlot, 2);
                            return;
                        }
                    }
                    if (preferredBowHotbarSlot >= 0 && preferredBowHotbarSlot <= 8 && !usedHotbarSlots.contains(preferredBowHotbarSlot) && inventoryBowSlot != -1) {
                        usedHotbarSlots.add(preferredBowHotbarSlot);
                        if (inventoryBowSlot != preferredBowHotbarSlot) {
                            this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(inventoryBowSlot), preferredBowHotbarSlot, 2);
                            return;
                        }
                    }
                    if (((Boolean)this.dropTrash.getValue()).booleanValue()) {
                        int currentBlockCount = this.getStackSize(inventoryBlocksSlot);
                        int currentProjectileCount = this.getStackSize(inventoryProjectileSlot);
                        for (int i = 0; i < 36; ++i) {
                            ItemStack stack;
                            if (equippedArmorSlots.contains(i) || inventoryArmorSlots.contains(i) || inventorySwordSlot == i || inventoryPickaxeSlot == i || inventoryShovelSlot == i || inventoryAxeSlot == i || inventoryBlocksSlot == i || inventoryProjectileSlot == i || inventoryGoldAppleSlot == i || inventoryBowSlot == i || (stack = InvManager.mc.field_71439_g.field_71071_by.func_70301_a(i)) == null) continue;
                            boolean isBlock = ItemUtil.isBlock(stack);
                            boolean isProjectile = ItemUtil.isProjectile(stack);
                            if (isBlock) {
                                currentBlockCount += stack.field_77994_a;
                            }
                            if (isProjectile) {
                                currentProjectileCount += stack.field_77994_a;
                            }
                            if (!ItemUtil.isNotSpecialItem(stack) || (!isBlock || currentBlockCount < (Integer)this.blocks.getValue()) && (!isProjectile || currentProjectileCount < (Integer)this.projectiles.getValue())) continue;
                            this.clickSlot(InvManager.mc.field_71439_g.field_71069_bz.field_75152_c, this.convertSlotIndex(i), 1, 4);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onClick(WindowClickEvent event) {
        this.actionDelay = RandomUtils.nextInt((int)((Integer)this.minDelay.getValue() + 1), (int)((Integer)this.maxDelay.getValue() + 2));
    }

    @Override
    public void verifyValue(String mode) {
        switch (mode) {
            case "min-delay": {
                if ((Integer)this.minDelay.getValue() <= (Integer)this.maxDelay.getValue()) break;
                this.maxDelay.setValue(this.minDelay.getValue());
                break;
            }
            case "max-delay": {
                if ((Integer)this.minDelay.getValue() <= (Integer)this.maxDelay.getValue()) break;
                this.minDelay.setValue(this.maxDelay.getValue());
            }
        }
    }
}

