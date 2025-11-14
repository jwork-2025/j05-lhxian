package com.gameengine.demo;

import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;

public class LightGameObject extends GameObject {

    public Vector2 getBase_pos() {
        return base_pos;
    }

    public void setBase_pos(Vector2 base_pos) {
        this.base_pos = base_pos;
    }

    protected Vector2 base_pos;
    public LightGameObject(String name){
        super(name);
    }
    public void setBlood(int blood){}
}
