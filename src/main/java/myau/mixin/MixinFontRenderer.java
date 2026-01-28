/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
 */
package myau.mixin;

import myau.Myau;
import myau.module.modules.AntiObfuscate;
import myau.module.modules.NickHider;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@SideOnly(value=Side.CLIENT)
@Mixin(value={FontRenderer.class}, priority=9999)
public abstract class MixinFontRenderer {
    @ModifyVariable(method={"renderString"}, at=@At(value="HEAD"), ordinal=0, argsOnly=true)
    private String renderString(String string) {
        NickHider nickHider;
        if (Myau.moduleManager == null) {
            return string;
        }
        AntiObfuscate antiObfuscate = (AntiObfuscate)Myau.moduleManager.modules.get(AntiObfuscate.class);
        if (antiObfuscate.isEnabled()) {
            string = antiObfuscate.stripObfuscated(string);
        }
        return (nickHider = (NickHider)Myau.moduleManager.modules.get(NickHider.class)).isEnabled() ? nickHider.replaceNick(string) : string;
    }

    @ModifyVariable(method={"getStringWidth"}, at=@At(value="HEAD"), ordinal=0, argsOnly=true)
    private String getStringWidth(String string) {
        NickHider nickHider;
        if (Myau.moduleManager == null) {
            return string;
        }
        AntiObfuscate antiObfuscate = (AntiObfuscate)Myau.moduleManager.modules.get(AntiObfuscate.class);
        if (antiObfuscate.isEnabled()) {
            string = antiObfuscate.stripObfuscated(string);
        }
        return (nickHider = (NickHider)Myau.moduleManager.modules.get(NickHider.class)).isEnabled() ? nickHider.replaceNick(string) : string;
    }

    @Redirect(method={"getStringWidth"}, at=@At(value="INVOKE", target="Ljava/lang/String;charAt(I)C", ordinal=1))
    private char getStringWidth(String string, int index) {
        char charAt = string.charAt(index);
        return charAt != '0' && charAt != '1' && charAt != '2' && charAt != '3' && charAt != '4' && charAt != '5' && charAt != '6' && charAt != '7' && charAt != '8' && charAt != '9' && charAt != 'a' && charAt != 'A' && charAt != 'b' && charAt != 'B' && charAt != 'c' && charAt != 'C' && charAt != 'd' && charAt != 'D' && charAt != 'e' && charAt != 'E' && charAt != 'f' && charAt != 'F' ? charAt : (char)'r';
    }
}

