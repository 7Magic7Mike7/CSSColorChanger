package sample.formula;

public class QuadraticFormula extends Formula {
    @Override
    public double apply(double d) {
        return x * d * d + y * d + z;
    }

    @Override
    protected String getName() {
        return "Quadratic";
    }
}
