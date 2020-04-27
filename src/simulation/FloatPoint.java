package simulation;

public class FloatPoint {
    public double x = 0;
    public double y = 0;

    public double radius = 5d;
    
    public FloatPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public FloatPoint(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "{X = " + x + ", Y = " + y + ", R = " + radius + "}";
    }
}
