package com.example.bluetoothtanks.framework;

/**
 * Created by Sasha on 08.09.13.
 */
public class GameObject {
    public final MyVector2 position;
    public final Rectangle bounds;
    public GameObject(float x, float y, float width, float height) {
        this.position = new MyVector2(x,y);
        this.bounds = new Rectangle(x-width/2, y-height/2, width, height);
    }
}
