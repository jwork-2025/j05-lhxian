package com.gameengine.demo.moveable;

import com.gameengine.components.RenderComponent;
import com.gameengine.core.GameObject;
import com.gameengine.demo.Jsonable;
import com.gameengine.demo.still.StillObj;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;

import java.util.List;

public class  Bullet extends Moveable implements Jsonable {
    protected static final int PLAYER_HURT =55, ENEMY_HURT =55;
    protected static enum State{
        BOOT, YOUNG,VANISH, EXPLODE , BEGIN_EXPLODE, STOP
    }
    public RenderComponent.Color getColor(){
        return new RenderComponent.Color(self_color.r,self_color.g,self_color.b,self_color.a);
    }
    protected static class MyColor{
        public float r,g,b,a;

        public MyColor(float r,float g,float b,float a){
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }
    }
    public void reSet(float living_time,boolean friend){
        setActive(true);
        this.ID =getNextID();
        this.friend = friend;
        this.living_time =living_time;
        state = State.BOOT;
        cur_radius = 2.5f;
        frame_still_time= 0.f;
        exploded = false;
        if(friend) self_color =s_color[1];
        else self_color =s_color[0];
    }
    protected static MyColor[] s_color;
    static {
        // 0 enemy bullet color, 1 player bullet color;
        s_color = new MyColor[]{new MyColor(0.8f,0.f,0.f,1.f),new MyColor(0.5f,0.3f,0.8f,1.f)};
    }
    protected static final float CHANGE_TIME=0.3f, MAX_RADIUS =6.f,ADD_SIZE =1.25f;
    protected static final float VANISH_TIME =1.5f, MAX_EXPLODE_R =12.f, ADD_EXPLODE_SIZE =4.f;

    protected MyColor self_color;
    protected State state;

    public void setLiving_time(float living_time) {
        this.living_time = living_time;
    }

    private float living_time;

    public float getCur_radius() {
        return cur_radius;
    }

    private float cur_radius;
    private float frame_still_time;

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

    private boolean friend;

    public boolean isExploded() {
        return exploded;
    }

    private boolean exploded;
    public Bullet(float living_time,boolean friend){
        super("Bullet");
        this.living_time = living_time;
        this.friend = friend;
        state = State.BOOT;
        cur_radius = 2.5f;
        frame_still_time= 0.f;
        exploded = false;
        if(friend) self_color =s_color[1];
        else self_color =s_color[0];
    }
    @Override
    public void update(float deltaTime){
        super.update(deltaTime);
        living_time = Math.max(living_time - deltaTime,0.f);
        frame_still_time += deltaTime;
        if(living_time <=0.f) destroy();
        if(state == State.BEGIN_EXPLODE){
            state =State.EXPLODE;
            removeFromTree();
        }else if(state == State.STOP) {
            destroy();
        }
    }
    private static final int segment =360;
    @Override
    public void render(){
//        super.render();
        Vector2 pos = getPos();
        IRenderer renderer = s_scene.getRenderer();
        switch(state){
            case BOOT:{
                if(frame_still_time >= CHANGE_TIME){
                    frame_still_time =0.f;
                    cur_radius += ADD_SIZE;
                    if(cur_radius >= MAX_RADIUS) state =State.YOUNG;
                }
                renderer.fillCircle(pos.x,pos.y,cur_radius,segment,self_color.r,self_color.g,self_color.b,self_color.a);
            }break;
            case YOUNG:{
                if(living_time <=VANISH_TIME){
                    state =State.VANISH;
                    frame_still_time =0.f;
                }
                renderer.fillCircle(pos.x,pos.y,cur_radius,segment,self_color.r,self_color.g,self_color.b,self_color.a);
            }break;
            case VANISH:{
                if(frame_still_time >= CHANGE_TIME){
                    frame_still_time =0.f;
                    cur_radius -= ADD_SIZE;
                }
                renderer.fillCircle(pos.x,pos.y,cur_radius,segment,self_color.r,self_color.g,self_color.b,self_color.a);
            }break;
            case EXPLODE: case BEGIN_EXPLODE:{
                // TODO;
                if(cur_radius < MAX_EXPLODE_R && frame_still_time >= CHANGE_TIME){
                    cur_radius += ADD_EXPLODE_SIZE;
                    frame_still_time =0.f;
                }
                if(friend){
                    renderer.fillCircle(pos.x,pos.y,cur_radius,segment,1.f,0.75f,0.79f,0.5f);
                    renderer.drawCircle(pos.x,pos.y,cur_radius+1.f,segment,0.8f,0.8f,0.8f,1.f);
                }else{
                    renderer.fillCircle(pos.x,pos.y,cur_radius,segment,0.8f,0.8f,0.f,0.5f);
                    renderer.drawCircle(pos.x,pos.y,cur_radius+1.f,segment,0.8f,0.f,0.f,1.f);
                }
            }break;
            case STOP: break;
            default: throw new IllegalStateException("error bullet state");
        }
        renderer.fillCircle(pos.x,pos.y,1.f,segment,0.f,0.f,0.f,1.f);
    }
    public boolean isFriend() {
        return friend;
    }
    @Override
    public boolean Outable(){return true;}
    public void explode(){
        exploded = true;
        state = State.BEGIN_EXPLODE;
        living_time =1.75f;
        frame_still_time =0.f;
        setVelocity(new Vector2(0.f,0.f));
    }
    public boolean canExplode(){
        return state == State.BOOT || state == State.YOUNG;
    }
    protected static final float EXPLODE_DIS_STILL =24.f,EXPLODE_DIS_MOVE=8.f;
    @Override
    public void handleCollision(List<GameObject> neighbors){
        // TODO
//        if(!canExplode()) return;
        if(state == State.EXPLODE)  return;
        Vector2 self_pos = getPos();
        Vector2 v_dir = getVelocity().normalize();
        Moveable hurt_target =null;
        StillObj hurt_still =null;
        float near = 1e8f, near_still =1e8f;
        for(GameObject obj: neighbors){
            if(!obj.isActive()) continue;
            if(obj instanceof Moveable){
                Moveable move = (Moveable)obj;
                Vector2 target_pos =move.getPos();
                Vector2 dire =target_pos.subtract(self_pos).normalize();
                if(dire.dot(v_dir) < 0.65) continue;
                float dis = target_pos.distance(self_pos);

                if(friend && move instanceof Enemy && dis <=EXPLODE_DIS_MOVE){
                    // hurt the enemy;
                    if(near > dis){
                        near = dis;
                        hurt_target = move;
                    }
                }else if(!friend && move instanceof Player && dis <=EXPLODE_DIS_MOVE){
                    // hurt the player
                    if(near > dis){
                        near = dis;
                        hurt_target = move;
                    }
                }
            }
            else{
                StillObj still =(StillObj)obj;
                Vector2 target_pos = still.getPos();
                Vector2 dire =target_pos.subtract(self_pos).normalize();
                if(dire.dot(v_dir) < 0.5) continue;
                float dis = target_pos.distance(self_pos);
                if(dis < EXPLODE_DIS_STILL && dis < near_still) {
                    near_still =dis;
                    hurt_still = still;
                }
            }
        }
        if(near < near_still && state != State.VANISH){
            if(hurt_target instanceof Player){
                Player player =(Player)hurt_target;
                player.Hurt(ENEMY_HURT);
                explode();
            }else if(hurt_target instanceof Enemy){
                ((Enemy)hurt_target).Hurt(PLAYER_HURT);
                explode();
            }

        }else if(hurt_still != null){
            if(state == State.VANISH) state =State.STOP;
            else explode();
        }
    }

    /**
     * don't need to remove the
     */
    @Override
    public void destroy(){
        if(exploded) directDestroy();
        else super.destroy();
    }


    @Override
    public String toJson(){
        return "type:";
    }
}
