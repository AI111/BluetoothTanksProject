package com.example.bluetoothtanks.framework;

/**
 * Created by Sasha on 08.09.13.
 */
public class Circle {
    public final MyVector2 center = new MyVector2();
    public float radius;

    /**
     * @param x centre X
     * @param y centre Y
     * @param radius
     */
    public Circle(float x, float y, float radius) {
        this.center.set(x,y);
        this.radius = radius;
    }
}
