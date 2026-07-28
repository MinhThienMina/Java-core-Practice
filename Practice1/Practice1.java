import java.util.ArrayList;
import java.util.List;


abstract class Shape {
    protected double width;
    protected double height;

    public Shape(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public abstract double getArea();

    public abstract double getPerimeter();

    public void display() {
        System.out.printf("%s -> Area = %.2f, Perimeter = %.2f%n",
                this.getClass().getSimpleName(), getArea(), getPerimeter());
    }
}

class Rectangle extends Shape {

    public Rectangle(double width, double height) {
        super(width, height);
    }

    @Override
    public double getArea() {
        // w * l
        return width * height;
    }

    @Override
    public double getPerimeter() {
        // 2(l+w)
        return 2 * (width + height);
    }
}

class Circle extends Shape {
    private double radius;

    public Circle(double radius) {
        super(2 * radius, 2 * radius);
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }

    public double getDiameter() {
        return width; 
    }

    @Override
    public double getArea() {
        // pi * r^2
        return Math.PI * radius * radius;
    }

    @Override
    public double getPerimeter() {
        return getCircumference();
    }

    public double getCircumference() {
        return getDiameter() * 3.14;
    }
}

public class Practice1 {
    public static void main(String[] args) {
        List<Shape> shapes = new ArrayList<>();
        shapes.add(new Rectangle(5, 10));
        shapes.add(new Circle(4));
        shapes.add(new Rectangle(3, 7));
        shapes.add(new Circle(2.5));

        System.out.println("Shape information");
        for (Shape s : shapes) {
            s.display();
        }

        System.out.println("\nDetailed information");
        for (Shape s : shapes) {
            if (s instanceof Rectangle r) {
                System.out.printf("Rectangle[width=%.2f, height=%.2f] Area=%.2f Perimeter=%.2f%n",
                        r.getWidth(), r.getHeight(), r.getArea(), r.getPerimeter());
            } else if (s instanceof Circle c) {
                System.out.printf("Circle[radius=%.2f] Area=%.2f Circumference=%.2f%n",
                        c.getRadius(), c.getArea(), c.getCircumference());
            }
        }
    }
}
