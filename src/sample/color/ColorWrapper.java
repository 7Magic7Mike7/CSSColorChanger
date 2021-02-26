package sample.color;

import javafx.scene.paint.Color;

import java.util.HashMap;

public class ColorWrapper implements Comparable<ColorWrapper> {
    private static final double COLOR_BASE = 255.0;

    public final Color color;
    private final boolean validOpacity;
    private boolean changed = false;

    public ColorWrapper(String[] rgb) {
        double r = 0.0;
        double g = 0.0;
        double b = 0.0;
        double o = 1.0;
        try {
            r = Double.parseDouble(rgb[0]) / COLOR_BASE;
            g = Double.parseDouble(rgb[1]) / COLOR_BASE;
            b = Double.parseDouble(rgb[2]) / COLOR_BASE;
            if(rgb.length > 3) o = Double.parseDouble(rgb[3]);

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        validOpacity = rgb.length > 3;
        color = new Color(r, g, b, o);
    }

    public void changed() {
        changed = true;
    }

    @Override
    public int hashCode() {
        return color.hashCode();
    }

    @Override
    public String toString() {
        String str = colorToString(color, false);
        if(validOpacity) str += "," + color.getOpacity();
        if(changed) str += " X";
        return str;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj instanceof ColorWrapper) {
            final ColorWrapper cw = (ColorWrapper)obj;
            return this.color.equals(cw.color);
        }
        return false;
    }

    @Override
    public int compareTo(ColorWrapper o) {
        final int r = Double.compare(color.getRed(), o.color.getRed());
        if(r == 0) {
            final int g = Double.compare(color.getGreen(), o.color.getGreen());
            if(g == 0) {
                final int b = Double.compare(color.getBlue(), o.color.getBlue());
                if(b == 0) return Double.compare(color.getOpacity(), o.color.getOpacity());
                else return b;
            } else return g;
        } else return r;
    }

    /**
     *
     * @param color
     * @param opacity whether opacity is valid (and part of the String) or not (not part of the String)
     * @return
     */
    public static String colorToString(Color color, boolean opacity) {
        final String r = getString(color.getRed());
        final String g = getString(color.getGreen());
        final String b = getString(color.getBlue());
        final String o = Double.toString(color.getOpacity());

        return r + "," + g + "," + b +      (opacity ? "," + o : "");
    }

    public static String colorToString(ColorWrapper cw, ColorChanges cc) {
        if(cw.validOpacity) {
            final String r = getString(cc.changed.getRed());
            final String g = getString(cc.changed.getGreen());
            final String b = getString(cc.changed.getBlue());
            final String o = Double.toString(cw.color.getOpacity());

            return r + "," + g + "," + b + "," + o;

        } else return colorToString(cw.color, false);
    }

    private static String getString(double c) {
        final String color = Double.toString(c * COLOR_BASE);
        return color.substring(0, color.indexOf('.'));
    }
}
