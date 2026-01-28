/*
 * Decompiled with CFR 0.152.
 */
package myau.ui.dataset;

public abstract class Slider {
    public abstract double getInput();

    public abstract double getMin();

    public abstract double getMax();

    public abstract void setValue(double var1);

    public abstract void setValueString(String var1);

    public abstract String getName();

    public abstract String getValueString();

    public abstract String getValueColorString();

    public abstract double getIncrement();

    public abstract boolean isVisible();

    public abstract void stepping(boolean var1);
}

