package sample.color;

import javafx.scene.paint.Color;

public class ColorChanges {
    public final Color original;
    public final Color changed;

    public ColorChanges(ColorWrapper original, Color changed) {
        this.original = original.color;
        original.changed();
        this.changed = changed;
    }

    @Override
    public int hashCode() {
        return original.hashCode() + 17 * changed.hashCode();
    }

    @Override
    public String toString() {
        return ColorWrapper.colorToString(original, false) + " -> " + ColorWrapper.colorToString(changed, false);
    }
}
