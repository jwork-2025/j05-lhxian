package com.gameengine.demo;

import com.gameengine.constant.MyConst;
import com.gameengine.core.GameEngine;
import com.gameengine.graphics.IRenderer;

public final class GameScene extends MyScene{
    public GameScene(GameEngine engine){
        super(engine, MyConst.WIDTH,MyConst.HEIGHT);
        SceneObj.Init(this);
    }
}
