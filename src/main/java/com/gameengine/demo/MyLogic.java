/**
 * @file MyLogic.java
 * 在这个类里面进行多线程处理，可能使用的多线程函数有：
 * {@link com.gameengine.demo.MyLogic#updatePhysicsAll(float)},
 * {@link com.gameengine.demo.MyLogic#checkCollisions()},
 * {@link com.gameengine.demo.MyLogic#updateObjAll(java.util.List, float)}.
 */
package com.gameengine.demo;


import com.gameengine.components.PhysicsComponent;
import com.gameengine.components.TransformComponent;
import com.gameengine.core.GameLogic;
import com.gameengine.core.GameObject;
import com.gameengine.demo.moveable.Bullet;
import com.gameengine.demo.moveable.Moveable;
import com.gameengine.demo.moveable.Player;
import com.gameengine.ds.Point;
import com.gameengine.ds.QuadTree;
import com.gameengine.ds.Rect;
import com.gameengine.math.Vector2;
import com.sun.glass.events.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MyLogic extends GameLogic{
    private final MyScene myScene;
    protected ExecutorService physicsExecutor;
    protected final int threadCnt;

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    private boolean gameOver =false;
    public MyLogic(MyScene scene){
        super(scene);
        this.myScene=scene;
        threadCnt=Math.min(Math.max(2,Runtime.getRuntime().availableProcessors()),8);
        physicsExecutor = Executors.newFixedThreadPool(threadCnt);
//        System.out.println("thread cnt:"+threadCnt);
    }
    public boolean isExitKeyPressed(){
        return inputManager.isKeyJustPressed(27) || inputManager.isKeyJustPressed(8);
    }
    public boolean isAnyKeyPressed(){
        return inputManager.isAnyKeyJustPressed();
    }
    public void cleanup(){
        if(physicsExecutor != null && !physicsExecutor.isShutdown()){
            physicsExecutor.shutdown();
            try{
                if(!physicsExecutor.awaitTermination(1, TimeUnit.SECONDS)){
                    physicsExecutor.shutdownNow();
                }
            }catch(InterruptedException e){
                physicsExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    @Override
    public void handlePlayerInput(){
        super.handlePlayerInput();
//        int width=myScene.getWidth(), height =myScene.getHeight();
        int left=MyScene.moveableAreaX,right=myScene.getMoveableAreaEndX();
        int top = MyScene.moveableAreaY, bottom =myScene.getMoveableAreaEndY();
        Player player = myScene.getCurPlayer();
        TransformComponent playerTransform= player.getComponent(TransformComponent.class);
        Vector2 playerPos = playerTransform.getPosition();
        if (playerPos.x < left)   playerPos.x = left;
        if (playerPos.y < top)    playerPos.y = top;
        if (playerPos.x > right)  playerPos.x = right;
        if (playerPos.y >bottom)  playerPos.y = bottom;
//        System.out.println("pos: "+pos.x+","+pos.y);

        playerTransform.setPosition(playerPos);

        if (inputManager.isKeyPressed(KeyEvent.VK_SPACE)){
            myScene.UserAct(UserAction.ATTACK);
        }
        Vector2 mousePos =inputManager.getMousePosition();
        if(mousePos.distance(playerPos) < 40.f) myScene.getCurPlayer().setEyePoint(mousePos);
    }
    public void handleCollisionSingleObj(GameObject obj,List<GameObject> neibors){
        Moveable move =(Moveable)obj;
        move.handleCollision(neibors);
    }
    @Deprecated
    public void checkCollisionsConcurrent(){
        // TODO
    }
    @Deprecated
    public void checkCollisionSingle(){
        // TODO
    }
    public static final int NEIBOR_WIDTH =64, NEIBOR_HEIGHT =64;
    public void checkCollisionQuadTreeOpt(GameObject obj){
        QuadTree<GameObject> tree =myScene.getQuadTree();
        TransformComponent transform = obj.getComponent(TransformComponent.class);
        if(transform == null) return;
        Vector2 pos = transform.getPosition();
        Rect bounding =new Rect((int)pos.x-NEIBOR_WIDTH/2,(int)pos.y-NEIBOR_HEIGHT/2,NEIBOR_WIDTH,NEIBOR_HEIGHT);
        List<QuadTree.Info<GameObject>> res=tree.findNeighbor(bounding);
        List<GameObject> neighbors =new ArrayList<>();
        res.forEach(i->{neighbors.add(i.val);});

        Moveable move =(Moveable)obj;
        move.handleCollision(neighbors);
    }
    protected void checkCollisionPart(List<GameObject> objs,int start,int end,QuadTree<GameObject> tree){
        for(int i=start;i <end;++i){
            GameObject obj = objs.get(i);
            if(obj instanceof Moveable) checkCollisionQuadTreeOpt(obj);
        }
    }
    public void checkCollisionNaive(){
        List<GameObject> objs =scene.getGameObjects();
        QuadTree<GameObject> tree = myScene.getQuadTree();
        int total_size =objs.size();
        checkCollisionPart(objs,0,total_size,tree);
    }
    @Override
    public void checkCollisions(){
        List<GameObject> objs =scene.getGameObjects();
        QuadTree<GameObject> tree  = myScene.getQuadTree();
        int total_size =objs.size();
        if(total_size > SPLIT_THRESHOLD){
            // tiling
            final int batch_size =Math.max(SPLIT_THRESHOLD,total_size /threadCnt);
            List<Future<?>> futures =new ArrayList<>();
            for(int i=0;i+batch_size < total_size;i += batch_size){
                final int start =i, end =i + batch_size;
                Future<?> future =physicsExecutor.submit(()->{checkCollisionPart(objs,start,end,tree);});
                futures.add(future);
            }
            int rest =total_size % batch_size;
            if(rest >0) checkCollisionPart(objs,total_size -rest,total_size,tree);
            for(Future<?> future: futures){
                try{
                    future.get();
                }catch(Exception e){
                    System.out.println("state: total:"+total_size+"batch_size:"+batch_size);
                    e.printStackTrace();
                    throw new RuntimeException("error");
                }
            }

        }else{
           checkCollisionPart(objs,0,total_size,tree);
        }
//        for(GameObject obj: objs){
//            if(obj instanceof Moveable) checkCollisionQuadTreeOpt(obj);
//        }

    }
    protected static class ReturnDataForPhysicsUpdate{
        public final int start, end;
        public final ArrayList<Point> pre_pos,cur_pos;
        public ReturnDataForPhysicsUpdate(int start,int end){
            this.start= start;
            this.end = end;
            pre_pos =new ArrayList<>((end - start)/2);
            cur_pos =new ArrayList<>((end - start)/2);
        }
        public void addPre(int x,int y){pre_pos.add(new Point(x,y));}
        public void addCur(int x,int y){cur_pos.add(new Point(x,y));}
    }
    private void handleReturnDataForPhysics(List<PhysicsComponent> physicses,ReturnDataForPhysicsUpdate res,QuadTree<GameObject> tree){
        if(res.pre_pos.size() != res.cur_pos.size()){
            throw new RuntimeException("error pos size");
        }
        int index =0, total_size =res.pre_pos.size();
        for(int i=res.start;i<res.end;++i){
            Moveable move = (Moveable)physicses.get(i).getOwner();
            if(!move.isActive()) continue;
            Point pre_pos=res.pre_pos.get(index), cur_pos = res.cur_pos.get(index);
            updateObjPos(move,pre_pos,cur_pos,tree);
            ++index;
        }
    }
    protected ReturnDataForPhysicsUpdate updatePhysicsPart(List<PhysicsComponent> phys, int start,int end,float deltaTime){
//        System.out.println("run part thread:"+start+","+end);
        int left =MyScene.moveableAreaX, right = myScene.getMoveableAreaEndX();
        int top=MyScene.moveableAreaY, bottom =myScene.getMoveableAreaEndY();
        ReturnDataForPhysicsUpdate return_data = new ReturnDataForPhysicsUpdate(start,end);
        for(int i=start; i <end;++i){
            PhysicsComponent physics = phys.get(i);
            Moveable move= (Moveable)physics.getOwner();
            TransformComponent transform = move.getComponent(TransformComponent.class);
            // important
            if(!move.isActive()) continue;
            Vector2 pos =move.getPos();
            int pre_x =(int)pos.x, pre_y =(int)pos.y;
            move.checkBounding(left,right,top,bottom);
            if(!move.isActive())  continue;
            return_data.addPre(pre_x,pre_y);
            physics.update(deltaTime);
            pos = move.getPos();
            int cur_x =(int)pos.x , cur_y =(int)pos.y;
            return_data.addCur(cur_x,cur_y);

        }
        return return_data;
    }
    public void updatePhysicsAllNaive(float deltaTime){
        QuadTree<GameObject> tree = myScene.getQuadTree();
        List<PhysicsComponent> physicsComponents = scene.getComponents(PhysicsComponent.class);
        int total_size = physicsComponents.size();
        ReturnDataForPhysicsUpdate return_data=updatePhysicsPart(physicsComponents,0,total_size,deltaTime);
        handleReturnDataForPhysics(physicsComponents,return_data,tree);
//        for(int i=0;i<total_size;++i){
//            Moveable move =(Moveable)(physicsComponents.get(i).getOwner());
//            updateObjPos(move,return_data.pre_pos.get(i),return_data.cur_pos.get(i),tree);
//        }
    }

    private void updateObjPos(Moveable move,Point pre_pos,Point cur_pos,QuadTree<GameObject> tree){
        if(move instanceof Bullet && ((Bullet)move).isExploded()) ;
        else if(!tree.check(move,pre_pos.x,pre_pos.y)){
            throw new IllegalStateException("error in logic check");
        }
        boolean res =tree.change(move,pre_pos.x,pre_pos.y,cur_pos.x,cur_pos.y);
    }
    protected static final int SPLIT_THRESHOLD =128;
    public void updatePhysicsAll(float deltaTime){
        QuadTree<GameObject> tree = myScene.getQuadTree();
        List<PhysicsComponent> physicsComponents = scene.getComponents(PhysicsComponent.class);
        int total_size = physicsComponents.size();
        if(total_size > SPLIT_THRESHOLD){
            // tiling
            List<Future<ReturnDataForPhysicsUpdate>> futures =new ArrayList<>();
            int batch_size =Math.max(SPLIT_THRESHOLD,total_size /threadCnt);
            for(int i=0;i+batch_size<total_size;i += batch_size){
                final int start =i, end =i+batch_size;
                Future<ReturnDataForPhysicsUpdate> future=physicsExecutor.submit(()->{
                    return updatePhysicsPart(physicsComponents,start,end,deltaTime);
                });
                futures.add(future);
            }
            int rest =total_size %batch_size;
            if(rest >0) {
                ReturnDataForPhysicsUpdate rest_res=updatePhysicsPart(physicsComponents,total_size -rest,total_size,deltaTime);
                handleReturnDataForPhysics(physicsComponents,rest_res,tree);
//                for(int i=rest_res.start; i< rest_res.end;++i) {
//                    Moveable move =(Moveable)(physicsComponents.get(i).getOwner());
//                    int index =i-rest_res.start;
//                    updateObjPos(move,rest_res.pre_pos.get(index),rest_res.cur_pos.get(index),tree);
//                }
            }
            for(Future<ReturnDataForPhysicsUpdate> future: futures){
                try{
                    ReturnDataForPhysicsUpdate return_data=future.get();
                    handleReturnDataForPhysics(physicsComponents,return_data,tree);
//                    for(int i =return_data.start; i <return_data.end;++i){
//                        Moveable move =(Moveable)(physicsComponents.get(i).getOwner());
//                        int index =i-return_data.start;
//                        updateObjPos(move,return_data.pre_pos.get(index),return_data.cur_pos.get(index),tree);
//                    }
                }catch(Exception e){
                    e.printStackTrace();
                    throw new RuntimeException("error");
                }
            }
        }else{
            ReturnDataForPhysicsUpdate res= updatePhysicsPart(physicsComponents,0,total_size,deltaTime);
            handleReturnDataForPhysics(physicsComponents,res,tree);
//            for(int i=0;i<total_size;++i) {
//                Moveable move =(Moveable)(physicsComponents.get(i).getOwner());
//                updateObjPos(move,res.pre_pos.get(i),res.cur_pos.get(i),tree);
//            }
        }
    }
    public void updateObjPart(List<GameObject> objs,int start,int end,float deltaTime){
        for(int i=start;i <end;++i){
            GameObject obj =objs.get(i);
            obj.update(deltaTime);
        }
    }
    public void updateObjAllNaive(List<GameObject> objs,float deltaTime){
        updateObjPart(objs,0,objs.size(),deltaTime);
    }
    public void updateObjAll(List<GameObject> objs,float deltaTime){
        int total_size =objs.size();
        if(total_size > SPLIT_THRESHOLD){
            List<Future<?>> futures =new ArrayList<>();
            int batch_size =Math.max(SPLIT_THRESHOLD,total_size /threadCnt);
            for(int i=0;i+batch_size<total_size;i += batch_size){
                final int start =i, end =i+batch_size;
                Future<?> future=physicsExecutor.submit(()->{
                    updateObjPart(objs,start,end,deltaTime);
                });
                futures.add(future);
            }
            int res =total_size %batch_size;
            if(res >0) updateObjPart(objs,total_size -res,total_size,deltaTime);
            for(Future<?> future: futures){
                try{
                    future.get();
                }catch(Exception e){
                    e.printStackTrace();
                    throw new RuntimeException("error");
                }
            }
        }else updateObjPart(objs,0,total_size,deltaTime);

    }
}
