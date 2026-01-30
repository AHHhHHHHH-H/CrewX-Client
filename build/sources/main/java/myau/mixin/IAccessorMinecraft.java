/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.util.Timer
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
 *  org.apache.logging.log4j.Logger
 */
package myau.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SideOnly(value=Side.CLIENT)
@Mixin(value={Minecraft.class})
public interface IAccessorMinecraft {
    @Accessor
    public Logger getLogger();

    @Accessor(value="timer")
    public Timer getTimer();

    @Accessor(value="rightClickDelayTimer")
    public int getRightClickDelayTimer();

    @Accessor(value="rightClickDelayTimer")
    public void setRightClickDelayTimer(int var1);
}

