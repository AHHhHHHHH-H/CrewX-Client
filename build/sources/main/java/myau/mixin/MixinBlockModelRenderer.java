/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.client.renderer.BlockModelRenderer
 *  net.minecraft.client.renderer.WorldRenderer
 *  net.minecraft.client.resources.model.IBakedModel
 *  net.minecraft.util.BlockPos
 *  net.minecraft.world.IBlockAccess
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
 */
package myau.mixin;

import myau.Myau;
import myau.module.modules.Xray;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(value=Side.CLIENT)
@Mixin(value={BlockModelRenderer.class}, priority=9999)
public abstract class MixinBlockModelRenderer {
    @Shadow
    public boolean func_178265_a(IBlockAccess iBlockAccess, IBakedModel iBakedModel, Block block, BlockPos blockPos, WorldRenderer worldRenderer, boolean boolean6) {
        return false;
    }

    @Inject(method={"renderModel(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/resources/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockPos;Lnet/minecraft/client/renderer/WorldRenderer;Z)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderModel(IBlockAccess iBlockAccess, IBakedModel iBakedModel, IBlockState iBlockState, BlockPos blockPos, WorldRenderer worldRenderer, boolean boolean6, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (Myau.moduleManager != null && Myau.moduleManager.modules.get(Xray.class).isEnabled()) {
            callbackInfoReturnable.setReturnValue(this.func_178265_a(iBlockAccess, iBakedModel, iBlockState.func_177230_c(), blockPos, worldRenderer, boolean6));
        }
    }
}

