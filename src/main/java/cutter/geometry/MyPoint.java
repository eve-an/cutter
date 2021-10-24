package cutter.geometry;

public class MyPoint {

    private double x;
    private double y;

    public MyPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public MyPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static double Length(MyPoint a, MyPoint b) {
        return  Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    public org.opencv.core.Point toOpenCV() {
        return new org.opencv.core.Point(x, y);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
