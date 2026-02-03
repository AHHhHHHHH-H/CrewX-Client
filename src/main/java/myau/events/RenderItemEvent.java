package myau.events;

import myau.event.events.Event;
import myau.event.events.Cancellable;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;

public class RenderItemEvent implements Event, Cancellable {
    private final EnumAction enumAction;
    private final boolean useItem;
    private final float animationProgression;
    private final float partialTicks;
    private final float swingProgress;
    private final ItemStack itemToRender;
    private boolean cancelled;

    public RenderItemEvent(EnumAction enumAction, boolean useItem, float animationProgression, float partialTicks, float swingProgress, ItemStack itemToRender) {
        this.enumAction = enumAction;
        this.useItem = useItem;
        this.animationProgression = animationProgression;
        this.partialTicks = partialTicks;
        this.swingProgress = swingProgress;
        this.itemToRender = itemToRender;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean state) {
        this.cancelled = state;
    }

    public EnumAction getEnumAction() { return enumAction; }
    public boolean isUseItem() { return useItem; }
    public float getAnimationProgression() { return animationProgression; }
    public float getPartialTicks() { return partialTicks; }
    public float getSwingProgress() { return swingProgress; }
    public ItemStack getItemToRender() { return itemToRender; }
}