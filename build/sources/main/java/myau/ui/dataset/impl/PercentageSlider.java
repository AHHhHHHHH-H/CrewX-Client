/*
 * Decompiled with CFR 0.152.
 */
package myau.ui.dataset.impl;

import myau.enums.ChatColors;
import myau.property.properties.PercentProperty;
import myau.ui.dataset.Slider;

public class PercentageSlider
        extends Slider {
    private final PercentProperty property;

    public PercentageSlider(PercentProperty property) {
        this.property = property;
    }

    @Override
    public double getInput() {
        return ((Integer)this.property.getValue()).intValue();
    }

    @Override
    public double getMin() {
        return this.property.getMinimum().intValue();
    }

    @Override
    public double getMax() {
        return this.property.getMaximum().intValue();
    }

    @Override
    public void setValue(double value) {
        this.property.setValue(new Double(value).intValue());
    }

    @Override
    public void setValueString(String value) {
        try {
            this.property.setValue(Integer.parseInt(value));
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
        return ((Integer)this.property.getValue()).toString();
    }

    @Override
    public String getValueColorString() {
        return ChatColors.formatColor(this.property.formatValue());
    }

    @Override
    public double getIncrement() {
        return 1.0;
    }

    @Override
    public boolean isVisible() {
        return this.property.isVisible();
    }

    @Override
    public void stepping(boolean increment) {
        if (increment) {
            if ((Integer)this.property.getValue() >= this.property.getMaximum()) {
                return;
            }
            this.property.setValue((Integer)this.property.getValue() + 1);
        } else {
            if ((Integer)this.property.getValue() <= this.property.getMinimum()) {
                return;
            }
            this.property.setValue((Integer)this.property.getValue() - 1);
        }
    }
}

