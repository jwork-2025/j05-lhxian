package com.gameengine.demo;


import com.gameengine.constant.MyConst;
import com.gameengine.core.GameEngine;
import com.gameengine.graphics.RenderBackend;

/**
 * 游戏示例
 */
public class Main{
    public static void main(String[] args) {
        System.out.println("启动游戏引擎...");
        
        try {
            // 创建游戏引擎
            GameEngine engine = new GameEngine(MyConst.WIDTH,MyConst.HEIGHT, "GameEngine", RenderBackend.GPU,false);
            
//            MyScene gameScene = new MyScene(engine.getRenderer(),MyConst.WIDTH,MyConst.HEIGHT);
            MenuScene menuScene = new MenuScene(engine,"MainMenu");
            engine.setScene(menuScene);
            // 设置场景
//            SceneObj.Init(gameScene);
//            engine.setScene(gameScene);
//            System.out.println("begin run");
            
            // 运行游戏
            engine.run();
            
        } catch (Exception e) {
            System.err.println("游戏运行出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("游戏结束");
    }
}
