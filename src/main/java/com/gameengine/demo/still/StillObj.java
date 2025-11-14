package com.gameengine.demo.still;

import com.gameengine.demo.SceneObj;
import com.gameengine.math.Vector2;


/**
 * this class doesn't have physics component and render component;
 */
abstract public class StillObj extends SceneObj {
    protected int posX,posY;
    public StillObj(String name,int x,int y){
        super(name);
        posX =x;
        posY =y;
    }
    public Vector2 getPos(){
        return new Vector2(posX,posY);
    }
    abstract public int boundW();
    abstract public int boundH();
}
