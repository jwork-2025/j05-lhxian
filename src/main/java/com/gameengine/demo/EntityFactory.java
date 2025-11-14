package com.gameengine.demo;


import com.gameengine.components.TransformComponent;
import com.gameengine.constant.MyConst;
import com.gameengine.graphics.GPUTexture;
import com.gameengine.graphics.IRenderer;
import com.gameengine.graphics.MyImage;
import com.gameengine.math.Vector2;
import javafx.animation.KeyFrame;

public final class EntityFactory {
    private EntityFactory() {}
    private static final MyImage s_player_image, s_enemy_image;
    private static final MyImage s_tree_image , s_stone_image,s_spring_image;

    static {
        s_player_image =new GPUTexture("/img/walking.png");
        s_enemy_image = new GPUTexture("/img/enemy.png");
        s_tree_image = new GPUTexture("/img/tree.png");
        s_stone_image = new GPUTexture("/img/stone.png");
        s_spring_image = new GPUTexture("/img/spring.png");
    }
    public static class ImagedObj extends LightGameObject{
//        protected Vector2 prePos;
//        protected boolean touched;
        protected int dir_idx=0, frame_idx =0;
        public ImagedObj(String name){
            super(name);
        }
        public Vector2 getPos(){
            return getComponent(TransformComponent.class).getPosition();
        }
        public void setPos(Vector2 new_pos){
            getComponent(TransformComponent.class).setPosition(new_pos);
        }
    }
    public static LightGameObject createPlayerVisual(IRenderer renderer,int HP) {
        return new ImagedObj("PlayerVisual"){
            public void setBlood(int blood) {
                this.blood = blood;
            }

            private int blood=HP;
            @Override
            public void update(float deltaTime){
                // TODO
//                if(!touched) active = false;
            }
            @Override
            public void render(){
                Vector2 pos =getPos();
                final int iw =MyConst.PLAYER_WIDTH, ih=MyConst.PLAYER_HEIGHT;
                int dx =(int)pos.x - iw/2, dy =(int)pos.y - ih/2;
                renderer.drawImage(s_player_image,dx,dy,dir_idx *ih,frame_idx*iw,iw,ih);
                renderer.fillRect(dx,dy-5.f,(blood/100.f)*iw,5.f,0.f,0.f,0.8f,1.f);
            }
        };
    }
    public static LightGameObject createEnemyVisual(IRenderer renderer,int HP){
        return new ImagedObj("EnemyVisual"){
            public void setBlood(int blood) {
                this.blood = blood;
            }

            private int blood=HP;
            @Override
            public void update(float deltaTime){
                // TODO
            }
            @Override
            public void render(){
                Vector2 pos =getPos();
                final int iw =MyConst.PLAYER_WIDTH, ih=MyConst.PLAYER_HEIGHT;
                int dx =(int)pos.x - iw/2, dy =(int)pos.y - ih/2;
                renderer.drawImage(s_enemy_image,dx,dy,dir_idx *ih,frame_idx*iw,iw,ih);
                renderer.fillRect(dx,dy-5.f,(blood/100.f)*iw,5.f,0.8f,0.f,0.f,1.f);
            }
        };
    }
    public static LightGameObject createBullet(IRenderer renderer,float radius,float r,float g,float b,float a){
        return new LightGameObject("BulletVisual"){
            @Override
            public void update(float deltaTime){
                // TODO
            }
            public Vector2 getPos(){
                return getComponent(TransformComponent.class).getPosition();
            }
            @Override
            public void render(){
                Vector2 pos =getPos();
                renderer.fillCircle(pos.x,pos.y,radius,360,r,g,b,a);
            }
        };
    }
    public static LightGameObject createTree(IRenderer renderer,int x,int y){
        return new LightGameObject("Tree"){
            @Override
            public void update(float deltaTime){}
            @Override
            public void render(){
                renderer.drawImage(s_tree_image,x-MyConst.TREE_WIDTH/2,y-MyConst.TREE_HEIGHT/2,0,0,MyConst.TREE_WIDTH,MyConst.TREE_HEIGHT);
            }
        };
    }
    public static LightGameObject createStone(IRenderer renderer,int x,int y){
        return new LightGameObject("Stone"){
            @Override
            public void update(float deltaTime){}
            @Override
            public void render(){
                renderer.drawImage(s_stone_image,x-MyConst.STONE_WIDTH/2,y-MyConst.STONE_WIDTH/2,0,0,MyConst.STONE_WIDTH,MyConst.STONE_HEIGHT);
            }
        };
    }
    public static LightGameObject createSpring(IRenderer renderer,int x,int y){
        final float CHANGE_TIME= 0.08f;
        return new ImagedObj("Spring"){
            private float keep_time = 0.f;
            @Override
            public void update(float deltaTime){
                keep_time += deltaTime;
                if(keep_time >= CHANGE_TIME){
                    keep_time = 0.f;
                    if(++frame_idx >=4) frame_idx =0;
                }
            }
            @Override
            public void render(){
                renderer.drawImage(s_spring_image,x-MyConst.SPRING_WIDTH/2,y-MyConst.SPRING_HEIGHT/2,
                        frame_idx*MyConst.SPRING_WIDTH,0,MyConst.SPRING_WIDTH,MyConst.SPRING_HEIGHT);
            }
        };
    }

}


