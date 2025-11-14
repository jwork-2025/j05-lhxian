package com.gameengine.graphics;

import com.gameengine.constant.Util;

import java.awt.image.BufferedImage;

public class GPUTexture extends MyImage{
    public int getTextureID() {
        return textureID;
    }

    private int textureID;
    public GPUTexture(String path){
        super(IMAGE_TYPE.GPU_TEXTURE);
        BufferedImage image = Util.LoadImageFromFile(path);
        textureID = Util.BufferedImageToGLTexture(image);
        width = image.getWidth();
        height =image.getHeight();
    }
    public GPUTexture(BufferedImage image){
        super(IMAGE_TYPE.GPU_TEXTURE);
        textureID = Util.BufferedImageToGLTexture(image);
        width = image.getWidth();
        height =image.getHeight();
    }
}
