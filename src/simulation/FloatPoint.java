package simulation;

public class FloatPoint {
    public double x = 0;
    public double y = 0;
    
    public FloatPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "{X = " + x + ", Y = " + y + "}";
    }
}
