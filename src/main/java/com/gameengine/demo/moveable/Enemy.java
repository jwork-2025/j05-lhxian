package com.gameengine.demo.moveable;

import com.gameengine.components.TransformComponent;
import com.gameengine.constant.MyConst;
import com.gameengine.constant.Util;
import com.gameengine.core.GameObject;
import com.gameengine.demo.SceneObj;
import com.gameengine.demo.still.StillObj;
import com.gameengine.graphics.GPUTexture;
import com.gameengine.graphics.IRenderer;
import com.gameengine.graphics.MyImage;
import com.gameengine.math.Vector2;

import java.util.List;

public class Enemy extends Moveable{
    static final float MIN_IDEL_TIME=4.f, MAX_IDEL_TIME =2.f;
    static final int MAX_BLOOD =100;

    private static MyImage s_image;
    private static final int I_W= MyConst.ENEMY_WIDTH, I_H =MyConst.ENEMY_HEIGHT;
    private static int[] dir_index_arr;
    private static final float changeFrameDis =2.5f;

    private float idelTime;
    private int blood, img_dir_idx, frame_idx;
    private static final int SPEEDUP_CNT =25;
    private int inSpeedUp;

    static{
        s_image =new GPUTexture("/img/enemy.png");
        dir_index_arr = new int[]{3,1,2,0};
    }
    private static int getDirectionIndex(int direct){
        return dir_index_arr[direct];
    }

    public Enemy(){
        super("Enemy");
        idelTime = MAX_IDEL_TIME+4*MAX_IDEL_TIME * s_scene.randomFloat();
        blood =MAX_BLOOD;
        img_dir_idx = getDirectionIndex(crossDir);
        frame_idx =0;
        inSpeedUp =0;
    }
    public void Attack(){
        TransformComponent transform= getComponent(TransformComponent.class);
        Vector2 pos = transform.getPosition();
        Vector2 playerPos = SceneObj.s_scene.getPlayerPos();
        Vector2 dir = playerPos.subtract(pos);
        Vector2 velocity =dir.normalize().multiply(100.f);
        SceneObj.s_scene.createBullet(false,pos,velocity);
    }
    @Override
    public int getHP(){
        return blood;
    }
    private static final float speedup_v =8f,acc =1.5f;
    @Override
    public void update(float deltaTime){
        super.update(deltaTime);
        if(blood <=0) {
            System.out.println("enemy destroy");
            // gen prompt
            destroy();
            return;
        }
        int current_img_dir =getDirectionIndex(crossDir);
        if(current_img_dir != img_dir_idx){
            // update the new image direction
            img_dir_idx =current_img_dir;
            frame_idx =0;
        }
        idelTime -= deltaTime;
        if(idelTime <=0){
            idelTime=MAX_IDEL_TIME+2*MAX_IDEL_TIME* s_scene.randomFloat();
            Attack();
        }
        Vector2 velocity=getVelocity();
        float v =velocity.magnitude();
        if(v < speedup_v){
            inSpeedUp = SPEEDUP_CNT;
        }
        if(inSpeedUp > 0){
            speedUp(acc);
            --inSpeedUp;
        }
    }
    @Override
    public void render(){
        final float blood_padding =5.f, b_width =I_W,b_height =5.f;
        super.render();
        Vector2 pos =this.Position();
        IRenderer renderer = SceneObj.s_scene.getRenderer();
        float restWidth =(b_width*blood) /MAX_BLOOD;
        int dx =(int)pos.x -I_W/2, dy =(int)pos.y -I_H/2;
        renderer.drawImage(s_image,dx,dy,frame_idx* I_W,img_dir_idx *I_H,I_W,I_H);
        renderer.fillRect(pos.x -b_width/2,pos.y-(float)(I_H/2)-blood_padding,restWidth,b_height,0.8f,0f,0f,1f);
        renderer.drawRect(pos.x-b_width/2-1f,pos.y-(float)(I_H/2)-blood_padding-1f,b_width,b_height,0.8f,0.8f,0.8f,1f);
        if(!idel && walkedDis >= changeFrameDis){
            walkedDis =0.f;
            frame_idx++;
            if(frame_idx >=4) frame_idx =0;

        }
        if(idel && frame_idx !=0) frame_idx =0;
    }
    public synchronized void  Hurt(int dec){
        blood =Math.max(blood-dec,0);
    }
    private static final float reject_k =.25f, central_k =0.05f;
    @Override
    public void handleCollision(List<GameObject> neighbors){
        // TODO
        Vector2 self_pos = getPos();
        Vector2 self_v =getVelocity();
        Vector2 self_v_dire = self_v.normalize();
        Vector2 reject_force =new Vector2();
        Vector2 still_dir = null;
        boolean reverse = false;
        for(GameObject obj: neighbors){
            if(obj == this) continue;
            if(obj instanceof Moveable){
                Moveable move =(Moveable)obj;
                if(move instanceof Enemy){
                    // reject
                    Enemy other = (Enemy)move;
                    Vector2 other_pos = other.getPos();
                    Vector2 dire = other_pos.subtract(self_pos);
                    float dis= other_pos.distance(self_pos);
                    reject_force.x += reject_k * dire.x /(dis +1f);
                    reject_force.y += reject_k * dire.y /(dis +1f);
                }
            }else if(obj instanceof StillObj){
                StillObj still =(StillObj)obj;
                Vector2 target_pos = still.getPos();
                if(target_pos.distance(self_pos) < STILL_SAFE_DIS){
                    // cautiously select feasible direction
                    Vector2 dire =target_pos.subtract(self_pos).normalize();
                    float tem =dire.dot(self_v_dire);
//                    System.out.println("tem: "+tem);
                    if( tem> 0.35f){
                        reverse = true;
                        still_dir = dire;
                        break;
                    }
                }
            }
        }
        if(reverse){
//            turnBack();
            // reflect
            Vector2 relect_dire =self_v_dire.subtract(still_dir.multiply(2.f)).normalize();
            float v_size = self_v.magnitude();
            setVelocity(relect_dire.multiply(v_size));
        }else{
//            float random_factor =s_scene.randomFloat()/4.f;
//            reject_force.x *= (random_factor+1.f);
//            reject_force.y *= (random_factor+1.f);
            Vector2 central_dis =self_pos.subtract(s_scene.getCentral());
            Vector2 new_v =null;
            if(central_dis.magnitude() >700.f) {
                new_v =self_v.subtract(reject_force.add(central_dis.multiply(central_k)));
            }else new_v = self_v.subtract(reject_force);
            if(reject_force.magnitude() >=.5f) setVelocity(new_v);
        }
    }

}
