/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
 */
package myau.mixin;

import myau.Myau;
import myau.event.EventManager;
import myau.events.StrafeEvent;
import myau.management.RotationState;
import myau.mixin.MixinEntity;
import myau.module.modules.Jesus;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(value=Side.CLIENT)
@Mixin(value={EntityLivingBase.class}, priority=9999)
public abstract class MixinEntityLivingBase
extends MixinEntity {
    @ModifyVariable(method={"jump"}, at=@At(value="STORE"), ordinal=0)
    private float jump(float float1) {
        return (Entity)this instanceof EntityPlayerSP && RotationState.isActived() ? RotationState.getSmoothedYaw() * ((float)Math.PI / 180) : float1;
    }

    @Redirect(method={"moveEntityWithHeading"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/EntityLivingBase;moveFlying(FFF)V"))
    private void moveEntityWithHeading(EntityLivingBase entityLivingBase, float float2, float float3, float float4) {
        if ((Entity)this instanceof EntityPlayerSP) {
            StrafeEvent event = new StrafeEvent(float2, float3, float4);
            EventManager.call(event);
            float2 = event.getStrafe();
            float3 = event.getForward();
            float4 = event.getFriction();
            boolean actived = RotationState.isActived();
            float yaw = this.field_70177_z;
            if (actived) {
                this.field_70177_z = RotationState.getSmoothedYaw();
            }
            entityLivingBase.func_70060_a(float2, float3, float4);
            if (actived) {
                this.field_70177_z = yaw;
            }
        } else {
            entityLivingBase.func_70060_a(float2, float3, float4);
        }
    }

    @ModifyVariable(method={"moveEntityWithHeading"}, name={"f3"}, at=@At(value="STORE"))
    private float moveEntityWithHeading(float float1) {
        if ((EntityLivingBase)this instanceof EntityPlayerSP && float1 == (float)EnchantmentHelper.func_180318_b((Entity)((EntityLivingBase)this))) {
            if (Myau.moduleManager == null) {
                return float1;
            }
            Jesus jesus = (Jesus)Myau.moduleManager.modules.get(Jesus.class);
            if (jesus.isEnabled() && (!((Boolean)jesus.groundOnly.getValue()).booleanValue() || this.field_70122_E)) {
                return Math.max(float1, ((Float)jesus.speed.getValue()).floatValue());
            }
        }
        return float1;
    }
}

