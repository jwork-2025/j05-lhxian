package com.gameengine.core;

import com.gameengine.components.TransformComponent;
import com.gameengine.components.PhysicsComponent;
import com.gameengine.core.GameObject;
import com.gameengine.input.InputManager;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

import java.util.List;

/**
 * 游戏逻辑类，处理具体的游戏规则
 */
public class GameLogic {
    protected Scene scene;
    protected InputManager inputManager;
    
    public GameLogic(Scene scene) {
        this.scene = scene;
        this.inputManager = InputManager.getInstance();
    }
    
    /**
     * 处理玩家输入
     */

    public void handlePlayerInput() {
        List<GameObject> players = scene.findGameObjectsByComponent(TransformComponent.class);
        if (players.isEmpty()) return;
        
        GameObject player = players.get(0);
        TransformComponent transform = player.getComponent(TransformComponent.class);
        PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
        
        if (transform == null || physics == null) return;
        
        Vector2 movement = new Vector2();
        
        // W / UpArrow (AWT=38, GLFW=265)
        if (inputManager.isKeyPressed(87) || inputManager.isKeyPressed(38) || inputManager.isKeyPressed(265)) {
            movement.y -= 1;
        }
        // S / DownArrow (AWT=40, GLFW=264)
        if (inputManager.isKeyPressed(83) || inputManager.isKeyPressed(40) || inputManager.isKeyPressed(264)) {
            movement.y += 1;
        }
        // A / LeftArrow (AWT=37, GLFW=263)
        if (inputManager.isKeyPressed(65) || inputManager.isKeyPressed(37) || inputManager.isKeyPressed(263)) {
            movement.x -= 1;
        }
        // D / RightArrow (AWT=39, GLFW=262)
        if (inputManager.isKeyPressed(68) || inputManager.isKeyPressed(39) || inputManager.isKeyPressed(262)) {
            movement.x += 1;
        }

        if (movement.magnitude() > 0) {
            movement = movement.normalize().multiply(80);
            physics.setVelocity(movement);
        }
        
    }
    /*
    public void handlePlayerInput(float deltaTime) {
        if (gameOver) return;

        GameObject player = getUserPlayer();
        if (player == null) return;

        TransformComponent transform = player.getComponent(TransformComponent.class);
        PhysicsComponent physics = player.getComponent(PhysicsComponent.class);

        if (transform == null || physics == null) return;

        Vector2 movement = new Vector2();

        // W / UpArrow (AWT=38, GLFW=265)
        if (inputManager.isKeyPressed(87) || inputManager.isKeyPressed(38) || inputManager.isKeyPressed(265)) {
            movement.y -= 1;
        }
        // S / DownArrow (AWT=40, GLFW=264)
        if (inputManager.isKeyPressed(83) || inputManager.isKeyPressed(40) || inputManager.isKeyPressed(264)) {
            movement.y += 1;
        }
        // A / LeftArrow (AWT=37, GLFW=263)
        if (inputManager.isKeyPressed(65) || inputManager.isKeyPressed(37) || inputManager.isKeyPressed(263)) {
            movement.x -= 1;
        }
        // D / RightArrow (AWT=39, GLFW=262)
        if (inputManager.isKeyPressed(68) || inputManager.isKeyPressed(39) || inputManager.isKeyPressed(262)) {
            movement.x += 1;
        }

        if (movement.magnitude() > 0) {
            movement = movement.normalize().multiply(200);
            physics.setVelocity(movement);
        }

        Vector2 pos = transform.getPosition();
        int screenW = gameEngine != null && gameEngine.getRenderer() != null ? gameEngine.getRenderer().getWidth() : 1920;
        int screenH = gameEngine != null && gameEngine.getRenderer() != null ? gameEngine.getRenderer().getHeight() : 1080;
        if (pos.x < 0) pos.x = 0;
        if (pos.y < 0) pos.y = 0;
        if (pos.x > screenW - 20) pos.x = screenW - 20;
        if (pos.y > screenH - 20) pos.y = screenH - 20;
        transform.setPosition(pos);
    }
     */

    /**
     * 更新物理系统
     */
    public void updatePhysics() {
        System.out.println("should not implement");
        List<PhysicsComponent> physicsComponents = scene.getComponents(PhysicsComponent.class);
        for (PhysicsComponent physics : physicsComponents) {
            // 边界反弹
            TransformComponent transform = physics.getOwner().getComponent(TransformComponent.class);
            if (transform != null) {
                Vector2 pos = transform.getPosition();
                Vector2 velocity = physics.getVelocity();
                
                if (pos.x <= 0 || pos.x >= 800 - 15) {
                    velocity.x = -velocity.x;
                    physics.setVelocity(velocity);
                }
                if (pos.y <= 0 || pos.y >= 600 - 15) {
                    velocity.y = -velocity.y;
                    physics.setVelocity(velocity);
                }
                
                // 确保在边界内
                if (pos.x < 0) pos.x = 0;
                if (pos.y < 0) pos.y = 0;
                if (pos.x > 800 - 15) pos.x = 800 - 15;
                if (pos.y > 600 - 15) pos.y = 600 - 15;
                transform.setPosition(pos);
            }
        }
    }
    
    /**
     * 检查碰撞
     */
    public void checkCollisions() {
        // 直接查找玩家对象
        List<GameObject> players = scene.findGameObjectsByComponent(TransformComponent.class);
        if (players.isEmpty()) return;
        
        GameObject player = players.get(0);
        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
        if (playerTransform == null) return;
        
        // 直接查找所有游戏对象，然后过滤出敌人
        for (GameObject obj : scene.getGameObjects()) {
            if (obj.getName().equals("Enemy")) {
                TransformComponent enemyTransform = obj.getComponent(TransformComponent.class);
                if (enemyTransform != null) {
                    float distance = playerTransform.getPosition().distance(enemyTransform.getPosition());
                    if (distance < 25) {
                        // 碰撞！重置玩家位置
                        playerTransform.setPosition(new Vector2(400, 300));
                        break;
                    }
                }
            }
        }
    }
}
