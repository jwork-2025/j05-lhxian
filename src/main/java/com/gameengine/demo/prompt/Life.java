package com.gameengine.demo.prompt;

import com.gameengine.constant.Util;
import com.gameengine.graphics.IRenderer;
import com.gameengine.graphics.GPUTexture;
import com.gameengine.graphics.MyImage;


public final class Life extends Prompt{
    private static final MyImage s_image;
    private static final int FRAME_CNT =4,I_W=16,I_H=16;
    private static final float CHANGE_TIME =0.25f;

    static {
        s_image = new GPUTexture("/img/life.png");
    }
    private int frame_idx;
    private float frame_time;
    public Life(int x,int y){
        super("Life",x,y);
        frame_idx =0;
        frame_time =0f;
    }

    @Override
    public void update(float deltaTime){
        frame_time += deltaTime;
        if(frame_time >= CHANGE_TIME){
            frame_time =0.f;
            if(++frame_idx >= FRAME_CNT) frame_idx =0;
        }
    }

    @Override
    public void render(){
        IRenderer renderer =s_scene.getRenderer();
        int dx =posX-I_W/2, dy =posY -I_H/2;
        renderer.drawImage(s_image,dx,dy,frame_idx * I_W,0,I_W,I_H);
    }


}
