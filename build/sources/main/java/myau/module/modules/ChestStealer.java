/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.inventory.GuiChest
 *  net.minecraft.client.resources.I18n
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.inventory.Container
 *  net.minecraft.inventory.ContainerChest
 *  net.minecraft.inventory.IInventory
 *  net.minecraft.item.Item
 *  net.minecraft.item.Item$ToolMaterial
 *  net.minecraft.item.ItemArmor
 *  net.minecraft.item.ItemArmor$ArmorMaterial
 *  net.minecraft.item.ItemAxe
 *  net.minecraft.item.ItemBow
 *  net.minecraft.item.ItemPickaxe
 *  net.minecraft.item.ItemSpade
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.ItemSword
 *  net.minecraft.world.WorldSettings$GameType
 *  org.apache.commons.lang3.RandomUtils
 */
package myau.module.modules;

import myau.Myau;
import myau.event.EventTarget;
import myau.event.types.EventType;
import myau.events.UpdateEvent;
import myau.events.WindowClickEvent;
import myau.mixin.IAccessorItemSword;
import myau.module.Module;
import myau.module.modules.InvManager;
import myau.property.properties.BooleanProperty;
import myau.property.properties.IntProperty;
import myau.util.ChatUtil;
import myau.util.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.WorldSettings;
import org.apache.commons.lang3.RandomUtils;

public class ChestStealer
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private int clickDelay = 0;
    private int oDelay = 0;
    private boolean inChest = false;
    private boolean warnedFull = false;
    public final IntProperty minDelay = new IntProperty("min-delay", 1, 0, 20);
    public final IntProperty maxDelay = new IntProperty("max-delay", 2, 0, 20);
    public final IntProperty openDelay = new IntProperty("open-delay", 1, 0, 20);
    public final BooleanProperty autoClose = new BooleanProperty("auto-close", false);
    public final BooleanProperty nameCheck = new BooleanProperty("name-check", true);
    public final BooleanProperty skipTrash = new BooleanProperty("skip-trash", true);
    public final BooleanProperty moreArmor = new BooleanProperty("more-armor", false);
    public final BooleanProperty moreSword = new BooleanProperty("more-sword", false);

    private boolean isValidGameMode() {
        WorldSettings.GameType gameType = ChestStealer.mc.field_71442_b.func_178889_l();
        return gameType == WorldSettings.GameType.SURVIVAL || gameType == WorldSettings.GameType.ADVENTURE;
    }

    private boolean isMoreArmor(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        if (!((Boolean)this.moreArmor.getValue()).booleanValue()) {
            return false;
        }
        if (!(itemStack.func_77973_b() instanceof ItemArmor)) {
            return false;
        }
        ItemArmor.ArmorMaterial armorMaterial = ((ItemArmor)itemStack.func_77973_b()).func_82812_d();
        if (armorMaterial == ItemArmor.ArmorMaterial.DIAMOND) {
            return true;
        }
        return armorMaterial == ItemArmor.ArmorMaterial.IRON && itemStack.func_77948_v();
    }

    private boolean isMoreSword(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        if (!((Boolean)this.moreSword.getValue()).booleanValue()) {
            return false;
        }
        if (!(itemStack.func_77973_b() instanceof ItemSword)) {
            return false;
        }
        Item.ToolMaterial swordMaterial = ((IAccessorItemSword)itemStack.func_77973_b()).getMaterial();
        if (swordMaterial == Item.ToolMaterial.EMERALD) {
            return true;
        }
        if (EnchantmentHelper.func_77506_a((int)Enchantment.field_77334_n.field_77352_x, (ItemStack)itemStack) != 0) {
            return true;
        }
        return swordMaterial == Item.ToolMaterial.IRON && itemStack.func_77948_v();
    }

    private boolean isInvManagerRequire(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        InvManager invManager = (InvManager)Myau.moduleManager.modules.get(InvManager.class);
        if (ItemUtil.ItemType.Block.contains(itemStack)) {
            return !invManager.isEnabled() || ItemUtil.findInventorySlot(ItemUtil.ItemType.Block) < (Integer)invManager.blocks.getValue();
        }
        if (ItemUtil.ItemType.Projectile.contains(itemStack)) {
            return !invManager.isEnabled() || ItemUtil.findInventorySlot(ItemUtil.ItemType.Projectile) < (Integer)invManager.projectiles.getValue();
        }
        if (ItemUtil.ItemType.FishRod.contains(itemStack)) {
            return ItemUtil.findInventorySlot(ItemUtil.ItemType.Projectile) == 0;
        }
        if (ItemUtil.ItemType.Arrow.contains(itemStack)) {
            return !invManager.isEnabled() || ItemUtil.findInventorySlot(ItemUtil.ItemType.Arrow) < (Integer)invManager.arrow.getValue();
        }
        return false;
    }

    private void shiftClick(int windowId, int slotId) {
        ChestStealer.mc.field_71442_b.func_78753_a(windowId, slotId, 0, 1, (EntityPlayer)ChestStealer.mc.field_71439_g);
    }

    public ChestStealer() {
        super("ChestStealer", false);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (event.getType() == EventType.PRE) {
            if (this.clickDelay > 0) {
                --this.clickDelay;
            }
            if (this.oDelay > 0) {
                --this.oDelay;
            }
            if (!(ChestStealer.mc.field_71462_r instanceof GuiChest)) {
                this.inChest = false;
            } else {
                Container container = ((GuiChest)ChestStealer.mc.field_71462_r).field_147002_h;
                if (!(container instanceof ContainerChest)) {
                    this.inChest = false;
                } else {
                    if (!this.inChest) {
                        this.inChest = true;
                        this.warnedFull = false;
                        this.oDelay = (Integer)this.openDelay.getValue() + 1;
                    }
                    if (this.oDelay <= 0 && this.clickDelay <= 0 && this.isEnabled() && this.isValidGameMode()) {
                        String inventoryName;
                        IInventory inventory = ((ContainerChest)container).func_85151_d();
                        if (((Boolean)this.nameCheck.getValue()).booleanValue() && !(inventoryName = inventory.func_70005_c_()).equals(I18n.func_135052_a((String)"container.chest", (Object[])new Object[0])) && !inventoryName.equals(I18n.func_135052_a((String)"container.chestDouble", (Object[])new Object[0]))) {
                            return;
                        }
                        if (ChestStealer.mc.field_71439_g.field_71071_by.func_70447_i() == -1) {
                            if (!this.warnedFull) {
                                ChatUtil.sendFormatted(String.format("%s%s: &cYour inventory is full!&r", Myau.clientName, this.getName()));
                                this.warnedFull = true;
                            }
                            if (((Boolean)this.autoClose.getValue()).booleanValue()) {
                                ChestStealer.mc.field_71439_g.func_71053_j();
                            }
                        } else {
                            if (((Boolean)this.skipTrash.getValue()).booleanValue()) {
                                double bowDamage;
                                float efficiency;
                                float shovelEfficiency;
                                float pickaxeEfficiency;
                                double damage;
                                int bestSword = -1;
                                double bestDamage = 0.0;
                                int[] bestArmorSlots = new int[]{-1, -1, -1, -1};
                                double[] bestArmorProtection = new double[]{0.0, 0.0, 0.0, 0.0};
                                int bestPickaxeSlot = -1;
                                float bestPickaxeEfficiency = 1.0f;
                                int bestShovelSlot = -1;
                                float bestShovelEfficiency = 1.0f;
                                int bestAxeSlot = -1;
                                float bestAxeEfficiency = 1.0f;
                                int bestBow = -1;
                                double bestBowDamage = 0.0;
                                for (int i = 0; i < inventory.func_70302_i_(); ++i) {
                                    double damage2;
                                    if (!container.func_75139_a(i).func_75216_d()) continue;
                                    ItemStack stack = container.func_75139_a(i).func_75211_c();
                                    Item item = stack.func_77973_b();
                                    if (item instanceof ItemSword) {
                                        damage2 = ItemUtil.getAttackBonus(stack);
                                        if (bestSword != -1 && !(damage2 > bestDamage)) continue;
                                        bestSword = i;
                                        bestDamage = damage2;
                                        continue;
                                    }
                                    if (item instanceof ItemArmor) {
                                        int armorType = ((ItemArmor)item).field_77881_a;
                                        double protectionLevel = ItemUtil.getArmorProtection(stack);
                                        if (bestArmorSlots[armorType] != -1 && !(protectionLevel > bestArmorProtection[armorType])) continue;
                                        bestArmorSlots[armorType] = i;
                                        bestArmorProtection[armorType] = protectionLevel;
                                        continue;
                                    }
                                    if (item instanceof ItemPickaxe) {
                                        float efficiency2 = ItemUtil.getToolEfficiency(stack);
                                        if (bestPickaxeSlot != -1 && !(efficiency2 > bestPickaxeEfficiency)) continue;
                                        bestPickaxeSlot = i;
                                        bestPickaxeEfficiency = efficiency2;
                                        continue;
                                    }
                                    if (item instanceof ItemSpade) {
                                        float efficiency3 = ItemUtil.getToolEfficiency(stack);
                                        if (bestShovelSlot != -1 && !(efficiency3 > bestShovelEfficiency)) continue;
                                        bestShovelSlot = i;
                                        bestShovelEfficiency = efficiency3;
                                        continue;
                                    }
                                    if (item instanceof ItemAxe) {
                                        float efficiency4 = ItemUtil.getToolEfficiency(stack);
                                        if (bestAxeSlot != -1 && !(efficiency4 > bestAxeEfficiency)) continue;
                                        bestAxeSlot = i;
                                        bestAxeEfficiency = efficiency4;
                                        continue;
                                    }
                                    if (!(item instanceof ItemBow)) continue;
                                    damage2 = ItemUtil.getBowAttackBonus(stack);
                                    if (bestBow != -1 && !(damage2 > bestBowDamage)) continue;
                                    bestBow = i;
                                    bestBowDamage = damage2;
                                }
                                int swordInInventorySlot = ItemUtil.findSwordInInventorySlot(0, true);
                                double d = damage = swordInInventorySlot != -1 ? ItemUtil.getAttackBonus(ChestStealer.mc.field_71439_g.field_71071_by.func_70301_a(swordInInventorySlot)) : 0.0;
                                if (bestDamage > damage) {
                                    this.shiftClick(container.field_75152_c, bestSword);
                                    return;
                                }
                                for (int i = 0; i < 4; ++i) {
                                    double protectionLevel;
                                    int slot = ItemUtil.findArmorInventorySlot(i, true);
                                    double d2 = protectionLevel = slot != -1 ? ItemUtil.getArmorProtection(ChestStealer.mc.field_71439_g.field_71071_by.func_70301_a(slot)) : 0.0;
                                    if (!(bestArmorProtection[i] > protectionLevel)) continue;
                                    this.shiftClick(container.field_75152_c, bestArmorSlots[i]);
                                    return;
                                }
                                int pickaxeSlot = ItemUtil.findInventorySlot("pickaxe", 0, true);
                                float f = pickaxeEfficiency = pickaxeSlot != -1 ? ItemUtil.getToolEfficiency(ChestStealer.mc.field_71439_g.field_71071_by.func_70301_a(pickaxeSlot)) : 1.0f;
                                if (bestPickaxeEfficiency > pickaxeEfficiency) {
                                    this.shiftClick(container.field_75152_c, bestPickaxeSlot);
                                    return;
                                }
                                int shovelSlot = ItemUtil.findInventorySlot("shovel", 0, true);
                                float f2 = shovelEfficiency = shovelSlot != -1 ? ItemUtil.getToolEfficiency(ChestStealer.mc.field_71439_g.field_71071_by.func_70301_a(shovelSlot)) : 1.0f;
                                if (bestShovelEfficiency > shovelEfficiency) {
                                    this.shiftClick(container.field_75152_c, bestShovelSlot);
                                    return;
                                }
                                int axeSlot = ItemUtil.findInventorySlot("axe", 0, true);
                                float f3 = efficiency = axeSlot != -1 ? ItemUtil.getToolEfficiency(ChestStealer.mc.field_71439_g.field_71071_by.func_70301_a(axeSlot)) : 1.0f;
                                if (bestAxeEfficiency > efficiency) {
                                    this.shiftClick(container.field_75152_c, bestAxeSlot);
                                    return;
                                }
                                int bowSlot = ItemUtil.findBowInventorySlot(0, true);
                                double d3 = bowDamage = bowSlot != -1 ? ItemUtil.getBowAttackBonus(ChestStealer.mc.field_71439_g.field_71071_by.func_70301_a(bowSlot)) : 0.0;
                                if (bestBowDamage > bowDamage) {
                                    this.shiftClick(container.field_75152_c, bestBow);
                                    return;
                                }
                            }
                            for (int i = 0; i < inventory.func_70302_i_(); ++i) {
                                if (!container.func_75139_a(i).func_75216_d()) continue;
                                ItemStack stack = container.func_75139_a(i).func_75211_c();
                                if (((Boolean)this.skipTrash.getValue()).booleanValue() && ItemUtil.isNotSpecialItem(stack) && !this.isMoreArmor(stack) && !this.isMoreSword(stack) && !this.isInvManagerRequire(stack)) continue;
                                this.shiftClick(container.field_75152_c, i);
                                return;
                            }
                            if (((Boolean)this.autoClose.getValue()).booleanValue()) {
                                ChestStealer.mc.field_71439_g.func_71053_j();
                            }
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onWindowClick(WindowClickEvent event) {
        this.clickDelay = RandomUtils.nextInt((int)((Integer)this.minDelay.getValue() + 1), (int)((Integer)this.maxDelay.getValue() + 2));
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

