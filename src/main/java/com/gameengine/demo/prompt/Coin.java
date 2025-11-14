package com.gameengine.demo.prompt;

import com.gameengine.constant.MyConst;
import com.gameengine.constant.Util;
import com.gameengine.demo.SceneObj;
import com.gameengine.graphics.IRenderer;
import com.gameengine.graphics.GPUTexture;
import com.gameengine.graphics.MyImage;


public class Coin extends Prompt{
    private static MyImage s_image_still =null,s_image_flash;
    private static final int I_W= MyConst.COIN_WIDTH, I_H=MyConst.COIN_HEIGHT;
    private static final float FLASH_ONCE_TIME =0.1f;
    static{
        s_image_still = new GPUTexture("/img/coin_still.png");
        s_image_flash =new GPUTexture("/img/coin_flash.png");
    }

    public void setFlash(boolean flash) {
        this.flash = flash;
    }

    private boolean flash=true;
    private int frame_idx=0;
    private float accumulate_time=0.f;
    public Coin(int x,int y){
        super("Coin",x,y);
    }
    @Override
    public void update(float deltaTime){
        super.update(deltaTime);
        accumulate_time += deltaTime;
    }

    @Override
    public void render(){
        IRenderer render = SceneObj.s_scene.getRenderer();
        int dx =posX-I_W/2,dy =posY -I_H/2;
        if(!flash) render.drawImage(s_image_flash,dx,dy,0,0,I_W,I_H);
        else{
            render.drawImage(s_image_flash,dx,dy,frame_idx*I_W,0,I_W,I_H);
            if(accumulate_time > FLASH_ONCE_TIME){
                if(++frame_idx ==4) frame_idx =0;
                accumulate_time -= FLASH_ONCE_TIME;
            }
        }
    }
}
