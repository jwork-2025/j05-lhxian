package com.gameengine.ds;

public class Rect {
    public int x,y,w,h;
    public Rect(int x,int y,int w,int h){
        this.x =x;
        this.y =y;
        this.w =w;
        this.h =h;
    }
    @Override
    public String toString(){
        return String.format("[%d,%d,%d,%d]",x,y,w,h);
    }
    public static int encode(Rect area,Rect target){
        // target must in the area;
        int code =0;
        if(target.x >=area.x+target.w) code |= 1;
        if(target.y >= area.y + target.h) code |=2;
        return code;
    }
    public static int encode(Rect area,Point point){
        int half_w  =(area.w+1) /2, half_h = (area.h+1) /2;
        int code =0;
        if(point.x >= area.x + half_w) code |= 1;
        if(point.y >= area.y + half_h) code |= 2;
        return code;
    }
    public static int encode(Rect area,int x,int y){
        int half_w  =(area.w+1) /2, half_h = (area.h+1) /2;
        int code =0;
        if(x >= area.x + half_w) code |= 1;
        if(y >= area.y + half_h) code |= 2;
        return code;

    }
    public static boolean pointInRect(Rect rect,int x,int y){
        return (x >= rect.x && x < rect.x + rect.w && y >= rect.y && y < rect.y + rect.h);
    }
    public static boolean pointInRect(Rect rect,Point point) {
        return pointInRect(rect,point.x,point.y);
    }
    public static boolean rectInter(Rect r1,Rect r2){
        int max_x =Math.max(r1.x,r2.x);
        int max_y =Math.max(r1.y,r2.y);
        return pointInRect(r1,max_x,max_y) && pointInRect(r2,max_x,max_y);
    }
}
