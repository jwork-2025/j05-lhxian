package com.gameengine.graphics;

import com.gameengine.constant.Util;

import java.awt.image.BufferedImage;

public class CPUImage extends MyImage{
    public BufferedImage getImage() {
        return image;
    }

    private BufferedImage image;
    public CPUImage(String path){
        super(IMAGE_TYPE.CPU_IMAGE);
        image = Util.LoadImageFromFile(path);
        width =image.getWidth();
        height = image.getHeight();
    }

}
