package sample.color;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class ColorGroup {
    private final int DIGITS_TO_PRINT = 5;

    private final List<ColorWrapper> colors = new ArrayList<>();
    //how much the specific color-parameters are allowed to differ so a given color is still in this group
    public final double dHue;
    public final double dSat;
    public final double dBri;
    public final double dOpa;

    private Color groupColor;

    //dHue and so on is the difference a new color may have to still belong to this group
    public ColorGroup(ColorWrapper cw, double dHue, double dSat, double dBri, double dOpa) {
        groupColor = cw.color;
        colors.add(cw);

        this.dHue = 360 * dHue;  //multiplied with 360 because hue is not between 0.0 and 1.0, but between 0.0 and 360.0
        this.dSat = dSat;
        this.dBri = dBri;
        this.dOpa = dOpa;
    }

    public Color getGroupColor() {
        return groupColor;
    }

    public boolean add(ColorWrapper cw) {
        final double hgc = groupColor.getHue();
        final double hcw = cw.color.getHue();
        if(Math.abs(hgc - hcw) <= dHue) {

            final double sgc = groupColor.getSaturation();
            final double scw = cw.color.getSaturation();
            if(Math.abs(sgc - scw) <= dSat) {

                final double bgc = groupColor.getBrightness();
                final double bcw = cw.color.getBrightness();
                if(Math.abs(bgc - bcw) <= dBri) {

                    final double ogc = groupColor.getOpacity();
                    final double ocw = cw.color.getOpacity();
                    if(Math.abs(ogc - ocw) <= dOpa) {
                        colors.add(cw);
                        groupColor = updateGroupColor();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Color updateGroupColor() {
        double hue = 0.0;
        double sat = 0.0;
        double bri = 0.0;
        double opa = 0.0;

        for(ColorWrapper cw : colors) {
            hue += cw.color.getHue();
            sat += cw.color.getSaturation();
            bri += cw.color.getBrightness();
            opa += cw.color.getOpacity();
        }

        final int size = colors.size();
        return Color.hsb(hue / size, sat / size, bri / size, opa / size);
    }

    @Override
    public String toString() {
        String h = Double.toString(groupColor.getHue());
        String s = Double.toString(groupColor.getSaturation());
        String b = Double.toString(groupColor.getBrightness());
        String o = Double.toString(groupColor.getOpacity());

        h = h.substring(0, h.indexOf('.'));
        if(s.length() > DIGITS_TO_PRINT) s = s.substring(0, DIGITS_TO_PRINT);
        if(b.length() > DIGITS_TO_PRINT) b = b.substring(0, DIGITS_TO_PRINT);
        if(o.length() > DIGITS_TO_PRINT) o = o.substring(0, DIGITS_TO_PRINT);

        return h + " | " + s + " | " + b + " | " + o;
    }
}
