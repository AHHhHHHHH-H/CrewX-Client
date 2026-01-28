/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.play.client.C0DPacketCloseWindow
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
 */
package myau.mixin;

import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SideOnly(value=Side.CLIENT)
@Mixin(value={C0DPacketCloseWindow.class})
public interface IAccessorC0DPacketCloseWindow {
    @Accessor
    public int getWindowId();
}

