/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.client.renderer.BlockRendererDispatcher
 *  net.minecraft.client.renderer.WorldRenderer
 *  net.minecraft.util.BlockPos
 *  net.minecraft.util.Vec3i
 *  net.minecraft.world.IBlockAccess
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
 */
package myau.mixin;

import myau.Myau;
import myau.module.modules.Xray;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SideOnly(value=Side.CLIENT)
@Mixin(value={BlockRendererDispatcher.class}, priority=9999)
public abstract class MixinBlockRendererDispatcher {
    @Inject(method={"renderBlock"}, at={@At(value="HEAD")})
    private void renderBlock(IBlockState iBlockState, BlockPos blockPos, IBlockAccess iBlockAccess, WorldRenderer worldRenderer, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        Xray Xray2;
        if (Myau.moduleManager != null && (Xray2 = (Xray)Myau.moduleManager.modules.get(Xray.class)).isEnabled() && Xray2.isXrayBlock(Block.func_149682_b((Block)iBlockState.func_177230_c()))) {
            if (Xray2.checkBlock(blockPos)) {
                Xray2.trackedBlocks.add(new BlockPos((Vec3i)blockPos));
            } else {
                Xray2.trackedBlocks.remove(blockPos);
            }
        }
    }
}

