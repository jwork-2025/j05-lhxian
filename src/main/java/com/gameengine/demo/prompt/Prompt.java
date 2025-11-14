package com.gameengine.demo.prompt;


import com.gameengine.demo.SceneObj;

/**
 * this class has no position, such as coin, only used to display the game status.
 * it doesn't has physics component or render component.
 */
public class Prompt extends SceneObj {
    protected int posX , posY;
    public Prompt(String name,int x,int y){
        super(name);
        posX =x;
        posY =y;
    }
}
