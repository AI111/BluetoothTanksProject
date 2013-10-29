package com.example.bluetoothtanks.framework;

/**
 * Created by Sasha on 08.09.13.
 */
public class DynamicGameObject extends GameObject {
    public final MyVector2 velocity;

    public final MyVector2 accel;
    public DynamicGameObject(float x, float y, float width, float height) {
        super(x, y, width, height);
        velocity = new MyVector2();
        accel = new MyVector2();
    }
}