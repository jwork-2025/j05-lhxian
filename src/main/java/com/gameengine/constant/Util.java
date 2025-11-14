package com.gameengine.constant;


import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
//import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
//import java.nio.IntBuffer;
import java.util.ArrayList;

public class Util {
    private static final ArrayList<Integer> textures;
    static {
        textures =new ArrayList<>();
    }
    public static BufferedImage LoadImageFromFile(String file_name){
        BufferedImage res =null;
        try(InputStream grass =Util.class.getResourceAsStream(file_name)){
            if(grass == null) throw new IOException("error in load image: "+file_name);
            res =ImageIO.read(grass);
        }catch(IOException e){
            e.printStackTrace();
        }

        return res;
    }
    public static ByteBuffer ioResourceToByteBuffer(InputStream is) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] bytes = baos.toByteArray();
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
            byteBuffer.put(bytes);
            byteBuffer.flip();
            return byteBuffer;
        }

    }

    /*
    public static int[] LoadTextureFromFile(String file_name){
        int[] res = null;
        try(InputStream is=Util.class.getResourceAsStream(file_name)){
            if(is == null) throw new IOException("error in load image: "+file_name);
            // read pixel from image file
            try(MemoryStack stack =MemoryStack.stackPush()){

                IntBuffer width=stack.mallocInt(1), height =stack.mallocInt(1),channels =stack.mallocInt(1);
                ByteBuffer buffer =ioResourceToByteBuffer(is);

                int texID=0,w=0,h=0;
                STBImage.stbi_set_flip_vertically_on_load(true);
                ByteBuffer image= STBImage.stbi_load_from_memory(buffer,width,height,channels,4);
                if(image ==null) throw new RuntimeException("error in load memory:"+file_name);
                // upload to opengl(GPU memory
                w =width.get();
                h =height.get();
                texID = GL11.glGenTextures();
                GL11.glBindTexture(GL11.GL_TEXTURE_2D,texID);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL11.GL_REPEAT);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

                GL11.glTexImage2D(
                        GL11.GL_TEXTURE_2D,
                        0,
                        GL11.GL_RGBA8,
                        w,h,
                        0,GL11.GL_RGBA,
                        GL11.GL_UNSIGNED_BYTE,
                        image
                );

                int error = GL11.glGetError();
                if (error != GL11.GL_NO_ERROR) {
                    System.err.println("纹理上传失败! OpenGL Error: 0x" + Integer.toHexString(error));
                }

                STBImage.stbi_image_free(image);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D,0);
                textures.add(texID);
                res = new int[]{texID,w,h};
            }
        }catch(IOException e){
            e.printStackTrace();
            throw new RuntimeException("error");
        }
        return res;
    }

     */
    public static void FreeTexture(int texId){
        GL11.glDeleteTextures(texId);
    }
    public static int BufferedImageToGLTexture(BufferedImage image){
        int width =image.getWidth(), height = image.getHeight();
         ByteBuffer buffer = BufferUtils.createByteBuffer(width* height* 4);

        for (int y = 0; y <height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // G
                buffer.put((byte) (pixel & 0xFF));         // B
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
            }
        }
        buffer.flip();

        int textureID =GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,textureID);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        textures.add(textureID);
        return textureID;
    }
}
