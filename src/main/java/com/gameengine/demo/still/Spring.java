package com.gameengine.demo.still;

import com.gameengine.constant.Util;
import com.gameengine.graphics.IRenderer;
import com.gameengine.graphics.GPUTexture;
import com.gameengine.graphics.MyImage;


public final class Spring extends StillObj{
    private static final int I_W=48,I_H=48;
    private static final MyImage s_image;
    private static final float CHANGE_TIME =0.1f;
    private static final int FRAME_CNT =3;

    private float frame_time;
    private int frame_idx;
    static {
//        s_image = Util.LoadImageFromFile("/img/spring.png");
        s_image = new GPUTexture("/img/spring.png");
    }
    public Spring(int x,int y){
        super("Spring",x,y);
        frame_time =0.f;
        frame_idx =0;
    }
    public int boundW(){
        return 0;
    }
    public int boundH(){
        return 0;
    }
    @Override
    public void update(float deltaTime){
        frame_time += deltaTime;
        if(frame_time >= CHANGE_TIME){
            if(++frame_idx >= FRAME_CNT) frame_idx =0;
            frame_time =0.f;
        }
    }
    @Override
    public void render(){
        IRenderer renderer =s_scene.getRenderer();
        int dx =posX -I_W/2, dy = posY -I_H/2;
        renderer.drawImage(s_image,dx,dy,frame_idx * I_H,0,I_W,I_H);
    }
}
