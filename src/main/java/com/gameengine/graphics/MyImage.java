package com.gameengine.graphics;

abstract public class MyImage {
    public static enum IMAGE_TYPE{
        CPU_IMAGE, GPU_TEXTURE
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    protected int width,height;
    protected IMAGE_TYPE image_type;
    public MyImage(IMAGE_TYPE image_type){
        this.image_type= image_type;
    }
}
