package com.gameengine.ds;

public class Point {
    public int x,y;

    public void setActive(boolean active) {
        this.active = active;
    }

    private boolean active=false;
    public Point(int x,int y){
        this.x=x;
        this.y=y;
    }
    @Override
    public boolean equals(Object obj){
        if(obj == this) return true;
        if(obj instanceof Point) {
            Point point =(Point)obj;
            return point.x == this.y && point.y == this.y;
        }
        return false;
    }
}
