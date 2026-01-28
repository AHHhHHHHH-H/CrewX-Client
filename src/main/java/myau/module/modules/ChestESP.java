/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockChest
 *  net.minecraft.block.properties.IProperty
 *  net.minecraft.client.Minecraft
 *  net.minecraft.tileentity.TileEntity
 *  net.minecraft.tileentity.TileEntityChest
 *  net.minecraft.tileentity.TileEntityEnderChest
 *  net.minecraft.util.AxisAlignedBB
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.util.Vec3
 */
package myau.module.modules;

import java.awt.Color;
import java.util.stream.Collectors;
import myau.event.EventTarget;
import myau.events.Render3DEvent;
import myau.mixin.IAccessorMinecraft;
import myau.mixin.IAccessorRenderManager;
import myau.module.Module;
import myau.property.properties.BooleanProperty;
import myau.property.properties.ColorProperty;
import myau.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

public class ChestESP
extends Module {
    private static final Minecraft mc = Minecraft.func_71410_x();
    public final ColorProperty chest = new ColorProperty("chest", new Color(255, 170, 0).getRGB());
    public final ColorProperty trappedChest = new ColorProperty("trapped-chest", new Color(255, 43, 0).getRGB());
    public final ColorProperty enderChest = new ColorProperty("ender-chest", new Color(26, 17, 0).getRGB());
    public final BooleanProperty tracers = new BooleanProperty("tracers", false);

    public ChestESP() {
        super("ChestESP", false);
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
        block11: {
            if (!this.isEnabled()) break block11;
            RenderUtil.enableRenderState();
            for (TileEntity chest : ChestESP.mc.field_71441_e.field_147482_g.stream().filter(tileEntity -> tileEntity instanceof TileEntityChest || tileEntity instanceof TileEntityEnderChest).collect(Collectors.toList())) {
                Color color;
                double maxX;
                double maxZ;
                double minX;
                double minZ;
                block13: {
                    block12: {
                        Block block = ChestESP.mc.field_71441_e.func_180495_p(chest.func_174877_v()).func_177230_c();
                        minZ = 0.0625;
                        minX = 0.0625;
                        maxZ = 0.9375;
                        maxX = 0.9375;
                        if (!(block instanceof BlockChest)) break block12;
                        color = block.func_149744_f() ? new Color((Integer)this.trappedChest.getValue(), true) : new Color((Integer)this.chest.getValue(), true);
                        EnumFacing facing = (EnumFacing)ChestESP.mc.field_71441_e.func_180495_p(chest.func_174877_v()).func_177229_b((IProperty)BlockChest.field_176459_a);
                        switch (facing) {
                            case NORTH: {
                                if (ChestESP.mc.field_71441_e.func_180495_p(chest.func_174877_v().func_177974_f()).func_177230_c() == block) break;
                                if (ChestESP.mc.field_71441_e.func_180495_p(chest.func_174877_v().func_177976_e()).func_177230_c() == block) {
                                    minX -= 1.0;
                                }
                                break block13;
                            }
                            case SOUTH: {
                                if (ChestESP.mc.field_71441_e.func_180495_p(chest.func_174877_v().func_177976_e()).func_177230_c() == block) break;
                                if (ChestESP.mc.field_71441_e.func_180495_p(chest.func_174877_v().func_177974_f()).func_177230_c() == block) {
                                    maxX += 1.0;
                                }
                                break block13;
                            }
                            case WEST: {
                                if (ChestESP.mc.field_71441_e.func_180495_p(chest.func_174877_v().func_177978_c()).func_177230_c() == block) break;
                                if (ChestESP.mc.field_71441_e.func_180495_p(chest.func_174877_v().func_177968_d()).func_177230_c() == block) {
                                    maxZ += 1.0;
                                }
                                break block13;
                            }
                            case EAST: {
                                if (ChestESP.mc.field_71441_e.func_180495_p(chest.func_174877_v().func_177968_d()).func_177230_c() == block) break;
                                if (ChestESP.mc.field_71441_e.func_180495_p(chest.func_174877_v().func_177978_c()).func_177230_c() == block) {
                                    minZ -= 1.0;
                                }
                                break block13;
                            }
                        }
                        continue;
                    }
                    color = new Color((Integer)this.enderChest.getValue(), true);
                }
                if (color.getAlpha() == 0) continue;
                AxisAlignedBB aabb = new AxisAlignedBB((double)chest.func_174877_v().func_177958_n() + minX, (double)chest.func_174877_v().func_177956_o() + 0.0, (double)chest.func_174877_v().func_177952_p() + minZ, (double)chest.func_174877_v().func_177958_n() + maxX, (double)chest.func_174877_v().func_177956_o() + 0.875, (double)chest.func_174877_v().func_177952_p() + maxZ).func_72317_d(-((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosX(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosY(), -((IAccessorRenderManager)mc.func_175598_ae()).getRenderPosZ());
                RenderUtil.drawBoundingBox(aabb, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), 1.5f);
                if (!((Boolean)this.tracers.getValue()).booleanValue()) continue;
                Vec3 vec = ChestESP.mc.field_71474_y.field_74320_O == 0 ? new Vec3(0.0, 0.0, 1.0).func_178789_a((float)(-Math.toRadians(RenderUtil.lerpFloat(ChestESP.mc.func_175606_aa().field_70125_A, ChestESP.mc.func_175606_aa().field_70127_C, ((IAccessorMinecraft)ChestESP.mc).getTimer().field_74281_c)))).func_178785_b((float)(-Math.toRadians(RenderUtil.lerpFloat(ChestESP.mc.func_175606_aa().field_70177_z, ChestESP.mc.func_175606_aa().field_70126_B, ((IAccessorMinecraft)ChestESP.mc).getTimer().field_74281_c)))) : new Vec3(0.0, 0.0, 0.0).func_178789_a((float)(-Math.toRadians(RenderUtil.lerpFloat(ChestESP.mc.field_71439_g.field_70726_aT, ChestESP.mc.field_71439_g.field_70727_aS, ((IAccessorMinecraft)ChestESP.mc).getTimer().field_74281_c)))).func_178785_b((float)(-Math.toRadians(RenderUtil.lerpFloat(ChestESP.mc.field_71439_g.field_71109_bG, ChestESP.mc.field_71439_g.field_71107_bF, ((IAccessorMinecraft)ChestESP.mc).getTimer().field_74281_c))));
                vec = new Vec3(vec.field_72450_a, vec.field_72448_b + (double)mc.func_175606_aa().func_70047_e(), vec.field_72449_c);
                float opacity = 1.0f;
                RenderUtil.drawLine3D(vec, (double)chest.func_174877_v().func_177958_n() + 0.5, (double)chest.func_174877_v().func_177956_o() + 0.5, (double)chest.func_174877_v().func_177952_p() + 0.5, (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, opacity, 1.5f);
            }
            RenderUtil.disableRenderState();
        }
    }
}

