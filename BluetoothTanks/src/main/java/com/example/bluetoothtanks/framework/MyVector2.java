package com.example.bluetoothtanks.framework;

/**
 * Created by Sasha on 31.08.13.
 */
import android.util.FloatMath;

public class MyVector2 {
    public static float TO_RADIANS = (1 / 180.0f) * (float) Math.PI;
    public static float TO_DEGREES = (1 / (float) Math.PI) * 180;
    public float x, y;
    public MyVector2() {
    }
    public MyVector2(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public MyVector2(MyVector2 other) {
        this.x = other.x;
        this.y = other.y;
    }
    public MyVector2 cpy(){
        return  new MyVector2(x,y);
    }
    public  MyVector2 set(MyVector2 other){
        this.x=other.x;
        this.y=other.y;
        return this;
    }
    public MyVector2 add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }
    public MyVector2 add(MyVector2 other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }
    public MyVector2 sub(float x, float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }
    public MyVector2 sub(MyVector2 other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }
    public MyVector2 mul(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }
    public float len() {
        return FloatMath.sqrt(x * x + y * y);
    }
    public MyVector2 nor(){
        float len=len();
        if(len!=0){
            this.x/=len;
            this.y/=len;
        }
        return  this;
    }
    public float angle() {
        float angle = (float) Math.atan2(y, x) * TO_DEGREES;
        if (angle < 0)
            angle += 360;
        return angle;
    }
    public MyVector2 rotate(float angle) {
        float rad = angle * TO_RADIANS;
        float cos = FloatMath.cos(rad);
        float sin = FloatMath.sin(rad);
        float newX = this.x * cos - this.y * sin;
        float newY = this.x * sin + this.y * cos;
        this.x = newX;
        this.y = newY;
        return this;
    }
    public float dist(MyVector2 other) {
        float distX = this.x - other.x;
        float distY = this.y - other.y;
        return FloatMath.sqrt(distX * distX + distY * distY);
    }
    public float dist(float x, float y) {
        float distX = this.x - x;
        float distY = this.y - y;
        return FloatMath.sqrt(distX * distX + distY * distY);
    }
    public float distSquared(MyVector2 other) {
        float distX = this.x - other.x;
        float distY = this.y - other.y;
        return distX*distX + distY*distY;
    }
    public float distSquared(float x1,float y1) {
        float distX = this.x - x1;
        float distY = this.y - y1;
        return distX*distX + distY*distY;
    }

    public void set(float x, float y) {
        this.x=x;
        this.y=y;
    }

    @Override
    public String toString() {
        return x+" "+y;
    }
}
