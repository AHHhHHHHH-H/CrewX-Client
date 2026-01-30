/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiIngame
 *  net.minecraft.entity.player.InventoryPlayer
 *  net.minecraft.item.ItemStack
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
 */
package myau.mixin;

import myau.Myau;
import myau.module.modules.Scaffold;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(value=Side.CLIENT)
@Mixin(value={GuiIngame.class}, priority=9999)
public abstract class MixinGuiIngame {
    @Redirect(method={"updateTick"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;"))
    private ItemStack updateTick(InventoryPlayer inventoryPlayer) {
        int slot;
        Scaffold scaffold = (Scaffold)Myau.moduleManager.modules.get(Scaffold.class);
        if (scaffold.isEnabled() && ((Boolean)scaffold.itemSpoof.getValue()).booleanValue() && (slot = scaffold.getSlot()) >= 0) {
            return inventoryPlayer.func_70301_a(slot);
        }
        return inventoryPlayer.func_70448_g();
    }
}

