package com.gameengine.demo;

import com.gameengine.core.GameObject;

abstract public class SceneObj extends GameObject {
    public static void setS_scene(MyScene s_scene) {
        SceneObj.s_scene = s_scene;
    }

    protected static MyScene s_scene;
    public static void Init(MyScene my_scene){
        s_scene =my_scene;
    }

    public SceneObj(String name){
        super(name);
    }
    @Override
    public void update(float deltaTime){
        super.update(deltaTime);
    }
    @Override
    public void render(){
        super.render();
    }

    public boolean Outable(){
        return false;
    }
}
