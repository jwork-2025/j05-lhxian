package com.gameengine.demo.still;

import com.gameengine.constant.Util;
import com.gameengine.graphics.IRenderer;
import com.gameengine.graphics.GPUTexture;
import com.gameengine.graphics.MyImage;


public final class Stone extends StillObj{
    private static final MyImage s_image;
    private static final int I_W =32, I_H=32;
    static {
//        s_image = Util.LoadImageFromFile("/img/stone.png");
        s_image =new GPUTexture("/img/stone.png");
    }
    public Stone(int x,int y){
        super("Stone",x,y);
    }
    @Override
    public void update(float deltaTime){}
    @Override
    public void render(){
        IRenderer renderer = s_scene.getRenderer();
        final int dx =posX -I_W/2, dy = posY - I_H/2;
        renderer.drawImage(s_image,dx,dy,0,0,I_W,I_H);
    }

    public int boundW(){
        return I_W;
    }

    public int boundH(){
        return I_H;
    }
}
