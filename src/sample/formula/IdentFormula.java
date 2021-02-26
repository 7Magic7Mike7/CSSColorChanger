package sample.formula;

public class IdentFormula extends Formula {
    @Override
    public double apply(double d) {
        return d;
    }

    @Override
    protected String getName() {
        return "Ident";
    }
}
