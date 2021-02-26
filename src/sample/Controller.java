package sample;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import sample.color.ColorChanges;
import sample.color.ColorGroup;
import sample.color.ColorWrapper;
import sample.formula.Formula;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.stream.Stream;

public class Controller {
    public TextField oldSheet;
    public ListView<ColorWrapper> foundColors;
    public Circle oldColor;
    public Circle newColor;
    public Slider hueSlider;
    public ColorPicker colorPicker;
    public TextField newSheet;

    //group-tab
    public ListView<ColorGroup> groups;
    public TextField groupHue;
    public TextField groupSat;
    public TextField groupBright;
    public TextField groupOpa;

    //manual-tab
    public ListView<ColorChanges> changedColors;

    //automatic-tab
    public TextField redMul;
    public TextField redAdd;
    public ChoiceBox<Formula> redFormula;
    public TextField redX;
    public TextField redY;
    public TextField redZ;

    public TextField greenMul;
    public TextField greenAdd;
    public ChoiceBox<Formula> greenFormula;
    public TextField greenX;
    public TextField greenY;
    public TextField greenZ;

    public TextField blueMul;
    public TextField blueAdd;
    public ChoiceBox<Formula> blueFormula;
    public TextField blueX;
    public TextField blueY;
    public TextField blueZ;

    public TextField opaMul;
    public TextField opaAdd;
    public ChoiceBox<Formula> opaFormula;
    public TextField opaX;
    public TextField opaY;
    public TextField opaZ;

    private final HashMap<Color, ColorChanges> map = new HashMap<>();

    public void initialize() {
        foundColors.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        foundColors.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if(newValue != null) oldColor.setFill(newValue.color);      //fill the upper circle with the selected color
        }));

        //whenever the slider changes, the hue of the selected color is updated and previewed
        hueSlider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            final Color fc = (Color)oldColor.getFill();//foundColors.getSelectionModel().getSelectedItem().color;
            final double sat = fc.getSaturation();
            final double bright = fc.getBrightness();
            final double opa = fc.getOpacity();

            final double value = newValue.doubleValue() * 3.6;    //between 0.0 and 100.0 * 3.6 so it is between 0 and 360 degree
            final Color newC = Color.hsb(value, sat, bright, opa);
            colorPicker.setValue(newC);
            newColor.setFill(newC);
        }));

        oldSheet.setText(Main.MIST_PATH);

        groups.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if(newValue != null) oldColor.setFill(newValue.getGroupColor());
        }));

        redFormula.setItems(FXCollections.observableArrayList(Formula.values()));
        redFormula.getSelectionModel().selectFirst();

        greenFormula.setItems(FXCollections.observableArrayList(Formula.values()));
        greenFormula.getSelectionModel().selectFirst();

        blueFormula.setItems(FXCollections.observableArrayList(Formula.values()));
        blueFormula.getSelectionModel().selectFirst();

        opaFormula.setItems(FXCollections.observableArrayList(Formula.values()));
        opaFormula.getSelectionModel().selectFirst();
    }

    /**Reads the css-File of the given stylesheet path and searches for all colors in it.
     *
     * @param actionEvent
     */
    public void start(ActionEvent actionEvent) {
        foundColors.getItems().clear();     //clear previous data

        final String sheet = oldSheet.getText();
        try (Stream<String> stream = Files.lines(Paths.get(sheet))) {
            final ArrayList<String> colorText = new ArrayList<>();
            stream  .filter(line -> line.contains("rgb"))       //if rgb (or rgba) is in the line, it contains a color
                    .forEach(line -> {
                        //the line may contain multiple colors so we filter out each one
                        final String[] arr = line.split("rgb");
                        //start with i = 1 because the first split-element has no color
                        for(int i = 1; i < arr.length; i++) {
                            int end = arr[i].indexOf(')');
                            if(end < 0) System.out.println("FAIL at " + arr[i]);
                            else {
                                final int start = (arr[i].startsWith("a") ? 2 : 1);
                                colorText.add(arr[i].substring(start, end));    //add the color values - either in the form "r,g,b" or "r,g,b,a"
                            }
                        }
                    });

            final TreeSet<ColorWrapper> tree = new TreeSet<>();     //we only need to save each color once, so we use a set
            colorText.forEach(color -> {
                final String[] rgb = color.split(",");
                if(rgb.length == 3 || rgb.length == 4) tree.add(new ColorWrapper(rgb));     //create the color from the text-values
                else System.out.println("Fail at " + color);
            });

            tree.forEach(cw -> foundColors.getItems().add(cw));     //add all found colors to the UI
            System.out.println("Number of unique colors = " + tree.size());

        } catch (IOException e) {
            e.printStackTrace();
        }

        //set the path to the new sheet the same as the old stylesheet but with the prefix "new_" in front of the filename
        final int index = sheet.lastIndexOf('\\') + 1;
        final String path = sheet.substring(0, index);
        final String file = "new_" + sheet.substring(index);
        newSheet.setText(path + file);
    }

    public void changeColor(ActionEvent actionEvent) {
        //get the selected original color
        final ColorWrapper cw = foundColors.getSelectionModel().getSelectedItem();
        if(cw == null) return;

        //and the picked new color
        final Color newC = colorPicker.getValue();
        if(newC == null) return;

        //and then add it to the changing colors
        final ColorChanges cc = new ColorChanges(cw, newC);
        changedColors.getItems().add(cc);

        map.put(cw.color, cc);

        foundColors.refresh();      //refresh so the mark (" X")  shows
    }

    public void applyChanges(ActionEvent actionEvent) {
        try(final BufferedWriter bw = new BufferedWriter(new FileWriter(newSheet.getText()))) { //write to new file
            //read from old file because we need to copy everything that is not color-related
            try (Stream<String> stream = Files.lines(Paths.get(oldSheet.getText()))) {
                stream.forEach(line -> {
                    if(line.contains("rgb")) {
                        final String[] arr = line.split("rgb");     //line may contain multiple colors
                        try {
                            final StringBuilder sb = new StringBuilder(arr[0]);   //write unchanged until first "rgb"
                            for(int i = 1; i < arr.length; i++) {   //replace each color in the line
                                int index = arr[i].indexOf(')');
                                if(index < 0) System.out.println("WRITE-FAIL at " + arr[i]);
                                else {
                                    final boolean flag = arr[i].startsWith("a");
                                    if(flag) sb.append("rgba(");
                                    else sb.append("rgb(");

                                    //get the original color
                                    final int start = (flag ? 2 : 1);         //exclude the a if it is there
                                    final int end = arr[i].indexOf(")");
                                    final String color = arr[i].substring(start, end);
                                    final String[] rgb = color.split(",");
                                    final ColorWrapper cw = new ColorWrapper(rgb);

                                    //find the changed color
                                    final ColorChanges cc = map.get(cw.color);
                                    if(cc == null) {    //apply the automatic changes if color wasn't changed manually
                                        sb.append(ColorWrapper.colorToString(applyAutomaticChanges(cw.color), flag));
                                    } else sb.append(ColorWrapper.colorToString(cw, cc));

                                    sb.append(arr[i].substring(end));       //append the rest
                                }
                            }
                            bw.write(sb.toString());
                            bw.newLine();
                            bw.flush();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //lines that don't contain colors remain unchanged
                        try {
                            bw.write(line);
                            bw.newLine();
                            bw.flush();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**Applies the set calculations of the automatic-tab to the given color.
     *
     * @param color
     * @return the transformed color
     */
    private Color applyAutomaticChanges(Color color) {
        final double rMul = Double.parseDouble(redMul.getText());
        final double rAdd = Double.parseDouble(redAdd.getText());
        final Formula rForm = redFormula.getValue();
        double red = rForm.apply(color.getRed() * rMul + rAdd);

        final double gMul = Double.parseDouble(greenMul.getText());
        final double gAdd = Double.parseDouble(greenAdd.getText());
        final Formula gForm = greenFormula.getValue();
        double green = gForm.apply(color.getGreen() * gMul + gAdd);

        final double bMul = Double.parseDouble(blueMul.getText());
        final double bAdd = Double.parseDouble(blueAdd.getText());
        final Formula bForm = blueFormula.getValue();
        double blue = bForm.apply(color.getBlue() * bMul + bAdd);

        final double oMul = Double.parseDouble(opaMul.getText());
        final double oAdd = Double.parseDouble(opaAdd.getText());
        final Formula oForm = opaFormula.getValue();
        double opacity = oForm.apply(color.getOpacity() * oMul + oAdd);

        //make sure all values are between 0.0 and 1.0
        red = Math.min(Math.max(red, 0.0), 1.0);
        green = Math.min(Math.max(green, 0.0), 1.0);
        blue = Math.min(Math.max(blue, 0.0), 1.0);
        opacity = Math.min(Math.max(opacity, 0.0), 1.0);

        return new Color(red, green, blue, opacity);
    }

    /**Colorizes the bottom circle (#newColor) with the chosen color from the colorPicker.
     *
     * @param actionEvent
     */
    public void colorPreview(ActionEvent actionEvent) {
        final Color color = colorPicker.getValue();
        if(color == null) return;

        newColor.setFill(color);
    }

    public void buildGroups(ActionEvent actionEvent) {
        final ArrayList<ColorGroup> list = new ArrayList<>();
        final double hue = Double.parseDouble(groupHue.getText());
        final double sat = Double.parseDouble(groupSat.getText());
        final double bri = Double.parseDouble(groupBright.getText());
        final double opa = Double.parseDouble(groupOpa.getText());

        foundColors.getItems().forEach(cw -> {
            if(list.size() == 0) {
                final ColorGroup cg = new ColorGroup(cw, hue, sat, bri, opa);
                list.add(cg);

            } else {
                boolean flag = true;
                for(ColorGroup cg : list) {
                    if(cg.add(cw)) {
                        flag = false;
                        break;
                    } else {
                        boolean stop = true;
                        cg.add(cw);
                    }
                }
                if(flag) {
                    final ColorGroup group = new ColorGroup(cw, hue, sat, bri, opa);
                    list.add(group);
                }
            }
        });

        System.out.println("Number of groups: " + list.size());

        groups.setItems(FXCollections.observableArrayList(list));
    }

    public void changeGroupColor(ActionEvent actionEvent) {
    }
}
