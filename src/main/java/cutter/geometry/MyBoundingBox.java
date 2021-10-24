package cutter.geometry;

public class MyBoundingBox {

    private MyPoint upperLeft;
    private MyPoint lowerRight;


    public MyBoundingBox(MyPoint upperLeft, MyPoint lowerRight) {
        this.upperLeft = upperLeft;
        this.lowerRight = lowerRight;
    }

    public MyBoundingBox(int x1, int y1, int x2, int y2) {
        this(new MyPoint(x1, y1), new MyPoint(x2, y2));
    }

    public MyPoint getUpperLeft() {
        return upperLeft;
    }

    public void setUpperLeft(MyPoint upperLeft) {
        this.upperLeft = upperLeft;
    }

    public MyPoint getLowerRight() {
        return lowerRight;
    }

    public void setLowerRight(MyPoint lowerRight) {
        this.lowerRight = lowerRight;
    }

    public double getArea() {
        MyPoint upperRight = new MyPoint(lowerRight.getX(), upperLeft.getY());

        double rightSide = MyPoint.Length(upperRight, lowerRight);
        double upperSide = MyPoint.Length(upperLeft, upperRight);

        return rightSide * upperSide;
    }

    @Override
    public String toString() {
        return "BoundingBox{" +
                "upperLeft=" + upperLeft +
                ", lowerRight=" + lowerRight +
                '}';
    }
}
