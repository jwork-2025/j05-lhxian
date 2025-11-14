package com.gameengine.demo.moveable;


import java.awt.image.BufferedImage;
import java.util.List;

import com.gameengine.constant.MyConst;
import com.gameengine.core.GameObject;
import com.gameengine.demo.still.StillObj;
import com.gameengine.graphics.GPUTexture;
import com.gameengine.graphics.IRenderer;
import com.gameengine.graphics.MyImage;
import com.gameengine.math.Vector2;

public class Player extends Moveable{
    private static final int MAX_BLOOD =100,I_W=MyConst.PLAYER_WIDTH,I_H= MyConst.PLAYER_HEIGHT;
    private static final float MAX_CD =0.1f;

    private Vector2 eyeDir;
    private float CD=0.f;
    private int blood,dir_idx,frame_idx;
    private static MyImage s_image=null;
    private static final int[] dire_index_arr;

    private static final float changeFrameDis =5.f;

    private static int getDirectionIndex(int dire){
        return dire_index_arr[dire];
    }

    static{
//        s_image = Util.LoadImageFromFile("/img/walking.png");
        s_image = new GPUTexture("/img/walking.png");
        dire_index_arr = new int[]{3,1,2,0};
    }

    public Player(){
        super("Player");
        this.CD =0;
        this.dir_idx =getDirectionIndex(this.crossDir);
        this.frame_idx =0;
        eyeDir =new Vector2(1.f,0.f);
        blood = MAX_BLOOD;
    }
    @Override
    public void update(float deltaTime){
        super.update(deltaTime);
        CD = Math.max(0,CD -deltaTime);

        int current_index =getDirectionIndex(crossDir);
        if(current_index != dir_idx){
            dir_idx =current_index;
            frame_idx =0;
//            walkedDis =0;
        }

        if(blood <0){
            System.out.println("Player has be destroyed.");
//            Restart();
        }
    }
    @Override
    public void render(){
        final float width =I_W,blood_bar_size =5.f;
        IRenderer renderer = s_scene.getRenderer();
        Vector2 pos = this.Position();
        // renderer.fillRect(pos.x, pos.y, 2*width, 2*width, 0.f, 1.f, 0.f, 1.f);
        int dx =(int)pos.x -I_W/2, dy =(int)pos.y -I_H/2;
        renderer.drawImage(Player.s_image,dx,dy,frame_idx*I_W,dir_idx*I_H,I_W,I_H);
        if(!idel && walkedDis >= changeFrameDis){
            walkedDis =0.f;
            frame_idx++;
            if(frame_idx >=4) frame_idx =0;

        }
        float restWidth =(width*blood) /MAX_BLOOD;
        renderer.fillRect(pos.x-width/2, pos.y-(float)(I_H/2)-blood_bar_size-5, restWidth,blood_bar_size,0.f,0.0f,0.8f,1.f);
        renderer.drawRect(pos.x-width/2-1, pos.y-(float)(I_H/2)-blood_bar_size-6,width+2,blood_bar_size+2,0.8f,0.8f,0.8f,1.f);
        renderer.drawCircle(pos.x+eyeDir.x*20,pos.y+eyeDir.y*20,5.f,16,1.f,1.f,1.f,1.f);
        renderer.drawRect(dx-8,dy,I_W+16,I_H,1.f,1.f,1.f,1.f);
    }
    public void Attack(){
        if(s_scene == null || CD >0) return;
        CD =MAX_CD;
        s_scene.createBullet(true, this.Position(), eyeDir.multiply(200));
//        s_scene.createPlayerBullet1(this.Position(), eyeDir.multiply(50));
    }
    public boolean isDead(){ return blood <= 0;}
    public void Restart(){
        // TODO
//        destroy();
//        TransformComponent playerTransform = getComponent(TransformComponent.class);
//        playerTransform.setPosition(new Vector2(400, 300));
        blood =MAX_BLOOD;
    }
    public synchronized void Hurt(int dec){
        blood = Math.max(0,blood -dec);
    }
    @Override
    public int getHP(){ return blood;}
    public void setEyePoint(Vector2 lookAt){
        eyeDir =lookAt.subtract(this.Position()).normalize();
    }


    @Override
    public void handleCollision(List<GameObject> neighbors){
        // TODO
        Vector2 self_pos = getPos();
        Vector2 self_v_dire = getVelocity().normalize();
        boolean reverse = false;
        for(GameObject obj: neighbors){
            if(obj == this) continue;
            if(obj instanceof Moveable){
                Moveable move =(Moveable)obj;
            }else if(obj instanceof StillObj && !reverse){
                StillObj still =(StillObj)obj;
                Vector2 target_pos = still.getPos();
                if(target_pos.distance(self_pos) < STILL_SAFE_DIS){
                    // cautiously select feasible direction
                    Vector2 dire =target_pos.subtract(self_pos).normalize();
                    float tem =dire.dot(self_v_dire);
//                    System.out.println("tem: "+tem);
                    if( tem> 0.65f){
                        reverse = true;
                    }
                }
            }
        }
        if(reverse){
            turnBack();
        }
    }
}
