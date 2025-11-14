package com.gameengine.demo;

import com.gameengine.constant.Util;
import com.gameengine.core.GameObject;
import com.gameengine.constant.MyConst;
import com.gameengine.graphics.GPUTexture;
import com.gameengine.graphics.IRenderer;
import com.gameengine.graphics.MyImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Random;

public class GameMap extends GameObject{
//    public static final int grassWidth=32, grassHeight =32, MyConst.GRASS_MAX=32, MyConst.GRASS_MIN=16,
//    MyConst.WATER_WIDTH =64, MyConst.WATER_HEIGHT =32;



    private  int width, height;
    private final MyImage mapImage;
    private final MyScene myscene;
    public static MyImage drawMapImage(){
        return new GPUTexture(drawMapImage(MyConst.WIDTH,MyConst.HEIGHT));
    }
    private static BufferedImage drawMapImage(int width,int height){
        BufferedImage tem_map = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d =tem_map.createGraphics();
        BufferedImage grassImage_1 =Util.LoadImageFromFile("/img/grass_1.png"),
                grassImage_2 =Util.LoadImageFromFile("/img/grass_2.png"),
                waterImage =Util.LoadImageFromFile("/img/water.png");
        BufferedImage grassLT =Util.LoadImageFromFile("/img/grass_angle_1.png"),
                grassRT =Util.LoadImageFromFile("/img/grass_angle_2.png"),
                grassLB =Util.LoadImageFromFile("/img/grass_angle_3.png"),
                grassRB =Util.LoadImageFromFile("/img/grass_angle_4.png");
        BufferedImage grassL =Util.LoadImageFromFile("/img/grass_edge_left.png"),
                grassR =Util.LoadImageFromFile("/img/grass_edge_right.png"),
                grassT =Util.LoadImageFromFile("/img/grass_edge_top.png"),
                grassB =Util.LoadImageFromFile("/img/grass_edge_bottom.png");


        int grassAreaWidth =width -MyConst.WATER_WIDTH*2, grassAreaHeight =height- MyConst.WATER_HEIGHT *2;
//        System.out.println("game map info: column: "+grassAreaWidth+","+grassAreaWidth /MyConst.GRASS_SIZE+"row:"+grassAreaHeight+","+grassAreaHeight/MyConst.GRASS_SIZE);
        // grass boundary
        // 1. draw water
        for(int w =0;w < width ; w+= MyConst.WATER_WIDTH) g2d.drawImage(waterImage,w,0,null);
        for(int w =0;w < width ; w+= MyConst.WATER_WIDTH) g2d.drawImage(waterImage,w,height-MyConst.WATER_HEIGHT,null);
        for(int h =0;h < height;h += MyConst.WATER_HEIGHT) g2d.drawImage(waterImage,0,h,null);
        for(int h =0;h < height;h += MyConst.WATER_HEIGHT) g2d.drawImage(waterImage,width-MyConst.WATER_WIDTH,h,null);

        // 2. draw center grass
        Random tem_random =new Random(100); // fix random seed to generate the same map
        for(int h=MyConst.WATER_HEIGHT;h < height -MyConst.WATER_HEIGHT; h+=MyConst.GRASS_SIZE){
            for(int w =MyConst.WATER_WIDTH; w < width - MyConst.WATER_WIDTH; w +=MyConst.GRASS_SIZE){
                int flag=tem_random.nextInt();
                BufferedImage selectGrass= flag%8 ==0? grassImage_2: grassImage_1;
                g2d.drawImage(selectGrass,w,h,null) ;
            }
        }
        // 3. draw grass angle
        g2d.drawImage(grassLT,MyConst.WATER_WIDTH -MyConst.GRASS_MIN,MyConst.WATER_HEIGHT-MyConst.GRASS_MIN,null);
        g2d.drawImage(grassRT,width-MyConst.WATER_WIDTH,MyConst.WATER_HEIGHT -MyConst.GRASS_MIN,null);
        g2d.drawImage(grassLB,MyConst.WATER_WIDTH -MyConst.GRASS_MIN,height-MyConst.WATER_HEIGHT,null);
        g2d.drawImage(grassRB,width-MyConst.WATER_WIDTH,height -MyConst.WATER_HEIGHT,null);
        // 4. draw grass edge
        for(int w=MyConst.WATER_WIDTH;w < width -MyConst.WATER_WIDTH;w += MyConst.GRASS_MAX) g2d.drawImage(grassT,w,MyConst.WATER_HEIGHT-MyConst.GRASS_MIN,null);
        for(int w=MyConst.WATER_WIDTH;w < width -MyConst.WATER_WIDTH;w += MyConst.GRASS_MAX) g2d.drawImage(grassB,w,height-MyConst.WATER_HEIGHT,null);
        for(int h=MyConst.WATER_HEIGHT;h < height-MyConst.WATER_HEIGHT;h += MyConst.GRASS_MAX) g2d.drawImage(grassL,MyConst.WATER_WIDTH-MyConst.GRASS_MIN,h,null);
        for(int h=MyConst.WATER_HEIGHT;h < height-MyConst.WATER_HEIGHT;h += MyConst.GRASS_MAX) g2d.drawImage(grassR,width-MyConst.WATER_WIDTH,h,null);

        return tem_map;
    }
    private BufferedImage DrawMap(){
        System.out.println("game map width:"+width+", height: "+height);
//        BufferedImage tem_map = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g2d =tem_map.createGraphics();
//        BufferedImage grassImage_1 =Util.LoadImageFromFile("/img/grass_1.png"),
//                grassImage_2 =Util.LoadImageFromFile("/img/grass_2.png"),
//                waterImage =Util.LoadImageFromFile("/img/water.png");
//        BufferedImage grassLT =Util.LoadImageFromFile("/img/grass_angle_1.png"),
//                grassRT =Util.LoadImageFromFile("/img/grass_angle_2.png"),
//                grassLB =Util.LoadImageFromFile("/img/grass_angle_3.png"),
//                grassRB =Util.LoadImageFromFile("/img/grass_angle_4.png");
//        BufferedImage grassL =Util.LoadImageFromFile("/img/grass_edge_left.png"),
//                grassR =Util.LoadImageFromFile("/img/grass_edge_right.png"),
//                grassT =Util.LoadImageFromFile("/img/grass_edge_top.png"),
//                grassB =Util.LoadImageFromFile("/img/grass_edge_bottom.png");
//
//
//        int grassAreaWidth =width -MyConst.WATER_WIDTH*2, grassAreaHeight =height- MyConst.WATER_HEIGHT *2;
////        System.out.println("game map info: column: "+grassAreaWidth+","+grassAreaWidth /MyConst.GRASS_SIZE+"row:"+grassAreaHeight+","+grassAreaHeight/MyConst.GRASS_SIZE);
//        // grass boundary
//        // 1. draw water
//        for(int w =0;w < width ; w+= MyConst.WATER_WIDTH) g2d.drawImage(waterImage,w,0,null);
//        for(int w =0;w < width ; w+= MyConst.WATER_WIDTH) g2d.drawImage(waterImage,w,height-MyConst.WATER_HEIGHT,null);
//        for(int h =0;h < height;h += MyConst.WATER_HEIGHT) g2d.drawImage(waterImage,0,h,null);
//        for(int h =0;h < height;h += MyConst.WATER_HEIGHT) g2d.drawImage(waterImage,width-MyConst.WATER_WIDTH,h,null);
//
//        // 2. draw center grass
//        Random tem_random =new Random(100); // fix random seed to generate the same map
//        for(int h=MyConst.WATER_HEIGHT;h < height -MyConst.WATER_HEIGHT; h+=MyConst.GRASS_SIZE){
//            for(int w =MyConst.WATER_WIDTH; w < width - MyConst.WATER_WIDTH; w +=MyConst.GRASS_SIZE){
//                int flag=tem_random.nextInt();
//                BufferedImage selectGrass= flag%8 ==0? grassImage_2: grassImage_1;
//                g2d.drawImage(selectGrass,w,h,null) ;
//            }
//        }
//        // 3. draw grass angle
//        g2d.drawImage(grassLT,MyConst.WATER_WIDTH -MyConst.GRASS_MIN,MyConst.WATER_HEIGHT-MyConst.GRASS_MIN,null);
//        g2d.drawImage(grassRT,width-MyConst.WATER_WIDTH,MyConst.WATER_HEIGHT -MyConst.GRASS_MIN,null);
//        g2d.drawImage(grassLB,MyConst.WATER_WIDTH -MyConst.GRASS_MIN,height-MyConst.WATER_HEIGHT,null);
//        g2d.drawImage(grassRB,width-MyConst.WATER_WIDTH,height -MyConst.WATER_HEIGHT,null);
//        // 4. draw grass edge
//        for(int w=MyConst.WATER_WIDTH;w < width -MyConst.WATER_WIDTH;w += MyConst.GRASS_MAX) g2d.drawImage(grassT,w,MyConst.WATER_HEIGHT-MyConst.GRASS_MIN,null);
//        for(int w=MyConst.WATER_WIDTH;w < width -MyConst.WATER_WIDTH;w += MyConst.GRASS_MAX) g2d.drawImage(grassB,w,height-MyConst.WATER_HEIGHT,null);
//        for(int h=MyConst.WATER_HEIGHT;h < height-MyConst.WATER_HEIGHT;h += MyConst.GRASS_MAX) g2d.drawImage(grassL,MyConst.WATER_WIDTH-MyConst.GRASS_MIN,h,null);
//        for(int h=MyConst.WATER_HEIGHT;h < height-MyConst.WATER_HEIGHT;h += MyConst.GRASS_MAX) g2d.drawImage(grassR,width-MyConst.WATER_WIDTH,h,null);
//        return tem_map;
        return drawMapImage(width,height);
    }


    public GameMap(MyScene scene,int w,int h){
        width =w;
        height =h;
        myscene = scene;
        // load grass image;

        mapImage = new GPUTexture(DrawMap());

    }
    @Override
    public void update(float deltaTime){
        super.update(deltaTime);
    }
    @Override
    public void render(){
        super.render();
        IRenderer renderer = myscene.getRenderer();
        renderer.drawImage(this.mapImage,0,0,0,0,width,height);
    }
}
