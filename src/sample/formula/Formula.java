package sample.formula;

public abstract class Formula {
    protected double x = 0.0;
    protected double y = 0.0;
    protected double z = 0.0;

    public void setParameters(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public abstract double apply(double d);

    protected abstract String getName();

    @Override
    public String toString() {
        return getName();
    }

    public static Formula[] values() {
        return new Formula[] {
                new IdentFormula(),
                new QuadraticFormula()
        };
    }
}
