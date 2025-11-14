package com.gameengine.demo.still;


import com.gameengine.constant.MyConst;
import com.gameengine.constant.Util;
import com.gameengine.demo.SceneObj;
import com.gameengine.graphics.IRenderer;
import com.gameengine.graphics.GPUTexture;
import com.gameengine.graphics.MyImage;

//import java.awt.image.BufferedImage;

public class Tree extends StillObj{
    protected static final MyImage s_image;
    private static final int I_W = MyConst.TREE_WIDTH, I_H=MyConst.TREE_HEIGHT;
    static{
//        s_image = Util.LoadImageFromFile("/img/tree.png");
        s_image = new GPUTexture("/img/tree.png");
    }
    public Tree(int x,int y){
        super("Tree",x,y);
    }

    @Override
    public void render(){
        IRenderer render= SceneObj.s_scene.getRenderer() ;
        render.drawImage(s_image,posX-I_W/2,posY -I_H/2,0,0,I_W,I_H);
    }

    public int boundW(){
        return I_W;
    }
    public int boundH(){
        return I_H;
    }
}
