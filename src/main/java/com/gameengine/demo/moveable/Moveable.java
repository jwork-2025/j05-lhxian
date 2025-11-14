package com.gameengine.demo.moveable;

import com.gameengine.components.PhysicsComponent;
import com.gameengine.components.TransformComponent;
import com.gameengine.core.GameObject;
import com.gameengine.demo.SceneObj;
import com.gameengine.demo.still.StillObj;
import com.gameengine.ds.QuadTree;
import com.gameengine.math.Vector2;

import java.util.List;

abstract public class Moveable extends SceneObj {
    private static int cur_id =0;
    protected static final float stillVelocity=0.15f; // if the velocity is less than it, mark as still
    protected float walkedDis;
    protected int crossDir;
    protected boolean idel;
    synchronized protected static int getNextID(){
        return cur_id++;
    }

    public int getID() {
        return ID;
    }
    public int getHP(){ return 0;}

    public void setID(int ID) {
        this.ID = ID;
    }

    protected int ID;
    public static final float STILL_SAFE_DIS =32.f;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    protected boolean checked=false;
    public enum Direction{
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
    public Moveable(String name){
        super(name);
        walkedDis =0.f;
        crossDir  =0;
        idel=false;
        ID = getNextID();
    }
    public Vector2 getPos(){
        TransformComponent transform =getComponent(TransformComponent.class);
        return transform.getPosition();
    }
    public Vector2 getVelocity(){
        PhysicsComponent physics =getComponent(PhysicsComponent.class);
        return physics.getVelocity();
    }
    public void setVelocity(Vector2 v){
        PhysicsComponent physics = getComponent(PhysicsComponent.class);
        physics.setVelocity(v);
    }
    public float getVelocitySize(){
        return getVelocity().magnitude();
    }
    public void speedUp(float acc){
        PhysicsComponent physics =getComponent(PhysicsComponent.class);
//        if(physics == null) return;
        Vector2 v =physics.getVelocity();
        Vector2 dire =v.normalize();
        physics.setVelocity(v.add(dire.multiply(acc)));
    }

    /**
     *
     * @param deltaTime
     * for direction :
     *
     */
    @Override
    public void update(float deltaTime){
//        super.update(deltaTime);
        Vector2 v =getVelocity();
//        System.out.println("moveable update");
        // add walking distance
        float v_size =v.magnitude();
        float walking = v_size * deltaTime;
        if(v_size < stillVelocity)  idel=true;
        else idel =false;
        walkedDis += walking;
        // update the direction
        if(Math.abs(v.x) > Math.abs(v.y)){
            // set direction to left or right;
            crossDir =v.x >=0.f? Direction.RIGHT.ordinal(): Direction.LEFT.ordinal();
        }else{
            crossDir =v.y >=0.f? Direction.DOWN.ordinal(): Direction.UP.ordinal();
        }
    }
    @Override
    public void render(){
        super.render();
    }

    abstract public void handleCollision(List<GameObject> neighbors);

    protected void removeFromTree(){
        QuadTree<GameObject> tree =s_scene.getQuadTree();
        Vector2 pos =getPos();
        boolean res =tree.remove(this,(int)pos.x,(int)pos.y);
        if(!res){
            throw new IllegalStateException("error");
        }

    }

    @Override
    public void destroy(){
        if(!isActive()) return;
        removeFromTree();
        super.destroy();
    }

    /**
     * this method doesn't remove the obj from quadtree as the obj should be removed before destroy, such as {@link Bullet}
     * because when {@link Bullet} exploded, we don't need to care the positon of the bullet as neighbor.
     */
    public void directDestroy(){
        super.destroy();
    }
    public float getDis(Moveable move){
        return getPos().distance(move.getPos());
    }
    protected static final float OUT_PADDING =12.f, PADDING =8.f;
    protected static final float TURN_BACK_FACTOR =0.25f;

    public void checkBounding(float left,float right,float top,float bottom){
        TransformComponent transform = getComponent(TransformComponent.class);
        Vector2 pos = transform.getPosition();
        Vector2 v = getVelocity();
        if(Outable()){
            if(pos.x <left -OUT_PADDING || pos.x >= right+ OUT_PADDING) destroy();
            else if(pos.y < top- OUT_PADDING || pos.y >= bottom + OUT_PADDING) destroy();
        }else{
            boolean flag = false;
            if(pos.x < left+PADDING && v.x <0f){
                flag = true;
                v.x = -TURN_BACK_FACTOR *v.x;
            }else if(pos.x > right-PADDING  && v.x >0f){
                flag= true;
                v.x = -TURN_BACK_FACTOR *v.x;
            }

            if(pos.y < top+PADDING && v.y <0.f){
                flag= true;
                v.y = -TURN_BACK_FACTOR *v.y;
            }else if(pos.y >= bottom-PADDING && v.y >0.f){
                flag = true;
                v.y = -TURN_BACK_FACTOR *v.y;
            }
            if(flag) setVelocity(v);
            if (pos.x < left+PADDING) pos.x = left+PADDING;
            else if (pos.x > right-PADDING) pos.x = right-PADDING;
            if (pos.y < top +PADDING) pos.y = top+PADDING;
            else if (pos.y > bottom -PADDING) pos.y = bottom-PADDING;
        }
        transform.setPosition(pos);

    }
    protected void turnBack(){
        Vector2 cur_v =getVelocity();
        cur_v.x = -TURN_BACK_FACTOR*cur_v.x;
        cur_v.y = -TURN_BACK_FACTOR*cur_v.y;
        setVelocity(cur_v);
    }

    protected void selectVelocity(int self_code, int res_mask){
        int self_mask =1 << self_code;
        if((self_mask & res_mask) !=0) {
//            System.out.println(String.format("can continue:%x,%x",self_mask,res_mask));
            return;
        }
        // else should select a new velocity;
        Vector2 cur_v= getVelocity();
        // check the reverse direction
        int reverse_mask = 1 <<(7-self_code);
        if((reverse_mask & res_mask) !=0){
            // set velocity to reverse
            cur_v.x = -cur_v.x;
            cur_v.y = -cur_v.y;
            setVelocity(cur_v);
        }else{
            System.out.println("todo");
            // find feasible direction
        }
    }


}
