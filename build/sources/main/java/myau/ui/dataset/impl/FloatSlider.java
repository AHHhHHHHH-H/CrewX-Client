/*
 * Decompiled with CFR 0.152.
 */
package myau.ui.dataset.impl;

import myau.enums.ChatColors;
import myau.property.properties.FloatProperty;
import myau.ui.dataset.Slider;

public class FloatSlider
        extends Slider {
    private final FloatProperty property;

    public FloatSlider(FloatProperty property) {
        this.property = property;
    }

    @Override
    public double getInput() {
        return ((Float)this.property.getValue()).floatValue();
    }

    @Override
    public double getMin() {
        return this.property.getMinimum().floatValue();
    }

    @Override
    public double getMax() {
        return this.property.getMaximum().floatValue();
    }

    @Override
    public void setValue(double value) {
        this.property.setValue(Float.valueOf(new Double(value).floatValue()));
    }

    @Override
    public void setValueString(String value) {
        try {
            this.property.setValue(Float.valueOf(Float.parseFloat(value)));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Override
    public String getName() {
        return this.property.getName().replace("-", " ");
    }

    @Override
    public String getValueString() {
        return ((Float)this.property.getValue()).toString();
    }

    @Override
    public String getValueColorString() {
        return ChatColors.formatColor(this.property.formatValue());
    }

    @Override
    public double getIncrement() {
        return 0.1;
    }

    @Override
    public boolean isVisible() {
        return this.property.isVisible();
    }

    @Override
    public void stepping(boolean increment) {
        if (increment) {
            if (((Float)this.property.getValue()).floatValue() >= this.property.getMaximum().floatValue()) {
                return;
            }
            this.property.setValue(Float.valueOf((float)Math.round(((Float)this.property.getValue()).floatValue() * 10.0f + 1.0f) / 10.0f));
        } else {
            if (((Float)this.property.getValue()).floatValue() <= this.property.getMinimum().floatValue()) {
                return;
            }
            this.property.setValue(Float.valueOf((float)Math.round(((Float)this.property.getValue()).floatValue() * 10.0f - 1.0f) / 10.0f));
        }
    }
}

