/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.entity.Entity
 *  net.minecraft.world.World
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
 */
package myau.mixin;

import myau.Myau;
import myau.module.modules.Jesus;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(value=Side.CLIENT)
@Mixin(value={World.class}, priority=9999)
public abstract class MixinWorld {
    @Redirect(method={"handleMaterialAcceleration"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;isPushedByWater()Z"))
    private boolean handleMaterialAcceleration(Entity entity) {
        Jesus jesus;
        if (entity instanceof EntityPlayerSP && Myau.moduleManager != null && (jesus = (Jesus)Myau.moduleManager.modules.get(Jesus.class)).isEnabled() && ((Boolean)jesus.noPush.getValue()).booleanValue()) {
            return false;
        }
        return entity.func_96092_aw();
    }
}

