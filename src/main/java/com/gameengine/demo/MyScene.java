package com.gameengine.demo;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import com.gameengine.components.PhysicsComponent;
import com.gameengine.components.TransformComponent;
import com.gameengine.constant.MyConst;
import com.gameengine.core.GameEngine;
import com.gameengine.core.GameObject;
import com.gameengine.core.ParticleSystem;
import com.gameengine.demo.moveable.Moveable;
import com.gameengine.demo.moveable.Bullet;
import com.gameengine.demo.moveable.Enemy;
import com.gameengine.demo.moveable.Player;
import com.gameengine.demo.prompt.Coin;
import com.gameengine.demo.still.Spring;
import com.gameengine.demo.still.StillObj;
import com.gameengine.demo.still.Stone;
import com.gameengine.demo.still.Tree;
import com.gameengine.ds.ObjPool;
import com.gameengine.ds.QuadTree;
import com.gameengine.ds.Rect;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

public class MyScene extends Scene {

    protected final int width;
    protected final int height;
    protected final Vector2 central;
    protected final GameEngine engine;

    private ParticleSystem playerParticles = null;
    private List<ParticleSystem> collisionParticles;
    private Map<GameObject, ParticleSystem> enemyParticles;
    protected boolean waitingReturn = false;
    private float waitInputTimer=0.f;
    private final float inputCooldown = 0.25f;
    private final float freezeDelay = 0.20f;


    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    private IRenderer renderer;
    private Random random;
    private float time;
    private MyLogic gameLogic;
    private Player curPlayer = null;
    public static final int moveableAreaX = MyConst.WATER_WIDTH, moveableAreaY = MyConst.WATER_HEIGHT;
    protected int moveableAreaEndX, moveableAreaEndY;

    protected final ObjPool<Bullet> bullet_pool;

    public QuadTree<GameObject> getQuadTree() {
        return quadTree;
    }

    protected final QuadTree<GameObject> quadTree;
    protected GameMap gameMap;

    public int getMoveableAreaEndX() {
        return moveableAreaEndX;
    }

    public int getMoveableAreaEndY() {
        return moveableAreaEndY;
    }

    public MyScene(GameEngine engine, int w, int h) {
        super("MyScene");
        width = w;
        height = h;
        moveableAreaEndX = width - MyConst.WATER_WIDTH;
        moveableAreaEndY = height - MyConst.WATER_HEIGHT;
        this.renderer =engine.getRenderer();
        this.engine = engine;
        createMap(w, h);
        quadTree = new QuadTree<>(width, height);
        bullet_pool = new ObjPool<>(() -> new Bullet(0.f, false));
        central = new Vector2(width / 2.f, height / 2.f);
    }


    public Player getCurPlayer() {
        return curPlayer;
    }

    public Vector2 getPlayerPos() {
        if (curPlayer != null) return curPlayer.Position();
        return null;
    }

    public void renderGameMap() {
        gameMap.render();
    }

    public void createMap(int w, int h) {
        gameMap = new GameMap(this, w, h);
//        GameMap gamemap = new GameMap(this,w,h);
//        addGameObject(gamemap);
    }

    public <T extends Moveable> void createMoveable(Class<T> clazz, int x, int y, Vector2 v) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void createBullet(boolean friend, Vector2 pos, Vector2 v) {
        Bullet bullet = bullet_pool.borrow();
        bullet.reSet(4.f, friend);
        TransformComponent transform = bullet.getComponent(TransformComponent.class);
        if (transform == null) transform = bullet.addComponent(new TransformComponent(pos));
        else transform.setPosition(pos);

        PhysicsComponent physics = bullet.getComponent(PhysicsComponent.class);
        if (physics == null) physics = bullet.addComponent(new PhysicsComponent(0.5f));
        physics.setFriction(1.f);
        physics.setVelocity(v);
        addGameObject(bullet);
        quadTree.add(bullet, (int) pos.x, (int) pos.y);
    }

    public void releaseBullet(Bullet bullet) {
        bullet_pool.release(bullet);
    }

    public void createPlayer() {
        int playerX = 144, playerY = height / 2;
        Player player = new Player();
        this.curPlayer = player;
        TransformComponent transform = player.addComponent(new TransformComponent(new Vector2(playerX, playerY)));

        PhysicsComponent physics = player.addComponent(new PhysicsComponent(1.0f));
        physics.setFriction(0.98f);

        addGameObject(player);
        quadTree.add(player, playerX, playerY);
        if (playerParticles == null) playerParticles = new ParticleSystem(renderer, player.getPos());

    }

    public void createEnemy() {
//        int enemyX= (int)(random.nextFloat() * (moveableAreaEndX - moveableAreaX)+ moveableAreaX),
//            enemyY=(int)(random.nextFloat() * (moveableAreaEndY - moveableAreaY) + moveableAreaY);
        int enemyX = random.nextInt(moveableAreaEndX - moveableAreaX) + moveableAreaX;
        int enemyY = 200;
        Enemy enemy = new Enemy();
        Vector2 position = new Vector2(
                enemyX, enemyY
        );

        TransformComponent transform = enemy.addComponent(new TransformComponent(position));
        final int max_vx = 70, max_vy = 70;
        PhysicsComponent physics = enemy.addComponent(new PhysicsComponent(0.5f));
        physics.setVelocity(new Vector2(
                random.nextInt(max_vx),
                random.nextInt(max_vy)
        ));
        physics.setFriction(0.98f);

        addGameObject(enemy);
        quadTree.add(enemy, enemyX, enemyY);

    }

    public void createCoin(int x, int y) {
        Coin coin = new Coin(x, y);
        addGameObject(coin);
    }

    public void UserAct(UserAction type) {
        switch (type) {
            case ATTACK:
                curPlayer.Attack();
                break;
        }
    }

    protected <T extends StillObj> void createStill(Class<T> clazz, int x, int y) {
        try {
            T still = clazz.getDeclaredConstructor(int.class, int.class).newInstance(x, y);
            addGameObject(still);
            quadTree.add(still, x, y);
        } catch (InstantiationException e) {
            throw new RuntimeException("error");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("error: no such method");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("error: illegal access");
        } catch (InvocationTargetException e) {
            throw new RuntimeException("error: invocation target");
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        this.random = new Random();
        this.time = 0;
        this.gameLogic = new MyLogic(this);

        collisionParticles = new ArrayList<>();
        enemyParticles = new HashMap<>();

        // TODO

//        createCoin(200,500);
        final int tree_padding = 32 * 3;
        for (int i = 0; i < 3; ++i)
            createStill(Tree.class, moveableAreaX + tree_padding + i * MyConst.TREE_WIDTH, moveableAreaY + tree_padding);
        for (int i = 0; i < 2; ++i)
            createStill(Tree.class, moveableAreaX + tree_padding, moveableAreaY + tree_padding + (i + 1) * MyConst.TREE_HEIGHT);

        for (int i = 0; i < 3; ++i)
            createStill(Tree.class, moveableAreaEndX - tree_padding - (i - 1) * MyConst.TREE_WIDTH, moveableAreaY + tree_padding);
        for (int i = 0; i < 2; ++i)
            createStill(Tree.class, moveableAreaEndX - tree_padding + MyConst.TREE_WIDTH, moveableAreaY + tree_padding + (i + 1) * MyConst.TREE_HEIGHT);

        for (int i = 0; i < 3; ++i)
            createStill(Tree.class, moveableAreaX + tree_padding + i * MyConst.TREE_WIDTH, moveableAreaEndY - MyConst.TREE_HEIGHT - tree_padding);
        for (int i = 0; i < 2; ++i)
            createStill(Tree.class, moveableAreaX + tree_padding, moveableAreaEndY - MyConst.TREE_HEIGHT - tree_padding - (i + 1) * MyConst.TREE_HEIGHT);

        for (int i = 0; i < 3; ++i)
            createStill(Tree.class, moveableAreaEndX - tree_padding - (i - 1) * MyConst.TREE_WIDTH, moveableAreaEndY - MyConst.TREE_HEIGHT - tree_padding);
        for (int i = 0; i < 2; ++i)
            createStill(Tree.class, moveableAreaEndX - tree_padding + MyConst.TREE_WIDTH, moveableAreaEndY - MyConst.TREE_HEIGHT - tree_padding - (i + 1) * MyConst.TREE_HEIGHT);
//        for(int i=0;i<4;++i) createStill(Stone.class,500+i*32,300);
        for (int y = 300; y < 700; y += MyConst.STONE_WIDTH) createStill(Stone.class, width / 2, y);
        for (int i = 0; i < 3; ++i) {
            createStill(Spring.class, width / 2 + 64 + i * MyConst.SPRING_WIDTH, height / 2);
            createStill(Spring.class, width / 2 + 64 + i * MyConst.SPRING_WIDTH, height / 2 + 2 * MyConst.SPRING_HEIGHT);
        }
        createStill(Spring.class, width / 2 + 64, height / 2 + MyConst.SPRING_HEIGHT);
        createStill(Spring.class, width / 2 + 64 + 2 * MyConst.SPRING_WIDTH, height / 2 + MyConst.SPRING_HEIGHT);
        createPlayer();
        for (int i = 0; i < 20; ++i) createEnemy();

    }

    public void setRenderer(IRenderer renderer) {
        this.renderer = renderer;
    }

    public Vector2 getCentral() {
        return central;
    }

    @Override
    public void update(float deltaTime) {
        if(gameLogic.isGameOver()){
//            System.out.println("game should be over!!!");
            waitInputTimer += deltaTime;
            if(gameLogic.isAnyKeyPressed()){
                engine.setScene(new MenuScene(engine,"MainMenu"));
                engine.disableRecording();
                System.out.println("should back to main menu");
                SceneObj.setS_scene(null);
            }
            return;
        }
        if (getCurPlayer().isDead()) {
            gameLogic.setGameOver(true);
            waitingReturn =true;
            return;
        }
        gameLogic.handlePlayerInput();
        // concurrent
        gameLogic.checkCollisions();
        gameLogic.updatePhysicsAll(deltaTime);
        // naive
//        gameLogic.checkCollisionNaive();
//        gameLogic.updatePhysicsAllNaive(deltaTime);

        super.update(deltaTime);

        Iterator<GameObject> iterator = gameObjects.iterator();
        while (iterator.hasNext()) {
            GameObject obj = iterator.next();
            if (!obj.isActive()) {
                if (obj instanceof Bullet) releaseBullet((Bullet) obj);
                else if (obj instanceof Enemy) {
                    enemyParticles.remove(obj);
                }
                iterator.remove();
            }
        }
        gameLogic.updateObjAll(getGameObjectsNotCopy(), deltaTime);
        updateParticles(deltaTime);
//        gameLogic.updateObjAllNaive(getGameObjectsNotCopy(),deltaTime);
        time += deltaTime;

        if (time > 8.f) {
            createEnemy();
            time = 0;
        }
    }

    private void updateParticles(float deltaTime) {
        if (gameLogic.isGameOver()) {

            return;
        }
        if (playerParticles != null) {
            Player player = getCurPlayer();
            if (player != null) {
                Vector2 playerPos = player.getPos();
                playerParticles.setPosition(playerPos);
            }
            playerParticles.update(deltaTime);
        }
        List<GameObject> objs = getGameObjectsNotCopy();
        for (GameObject obj : objs) {
            if (obj.isActive() && obj instanceof Enemy) {
                Enemy enemy = (Enemy) obj;
                ParticleSystem particle_sys = enemyParticles.get(enemy);
                Vector2 enemy_pos = enemy.getPos();
                if (particle_sys == null) {
                    particle_sys = new ParticleSystem(renderer, enemy_pos, ParticleSystem.Config.enemyConfig());
                    particle_sys.setActive(true);
                    enemyParticles.put(enemy, particle_sys);
                } else {
                    particle_sys.setPosition(enemy_pos);
                }
                particle_sys.update(deltaTime);

            }
        }
    }

    private void renderParticles() {
        if (playerParticles != null) {
            int count = playerParticles.getParticleCount();
            if (count > 0) {
                playerParticles.render();
            }
        }

        for (ParticleSystem ps : enemyParticles.values()) {
            if (ps != null && ps.getParticleCount() > 0) {
                ps.render();
            }
        }

        for (ParticleSystem ps : collisionParticles) {
            if (ps != null && ps.getParticleCount() > 0) {
                ps.render();
            }
        }

    }

    private void drawMeshRecur(QuadTree.Node<GameObject> cur_node) {
        if (cur_node.isLeaf()) {
            Rect rect = cur_node.rect;
            renderer.drawRect(cur_node.rect.x, cur_node.rect.y, cur_node.rect.w, cur_node.rect.h, 0.5f, 0.5f, 0.5f, 0.8f);
            if (cur_node.getTotal_ele() != cur_node.getContent().size()) {
                throw new IllegalStateException("error in leaf");
            }
            return;
        }
        int cur_cnt = 0;
        for (int i = 0; i < QuadTree.CHILD_CNT; ++i) {
            if (cur_node.getTotal_ele() <= QuadTree.THRESHOLD) {
                throw new IllegalStateException("error in quad tree in total ele cnt");
            }
            QuadTree.Node<GameObject> child = cur_node.getChild(i);
            drawMeshRecur(child);
            cur_cnt += child.getTotal_ele();
        }
        if (cur_cnt != cur_node.getTotal_ele()) {
            throw new IllegalStateException("error");
        }
    }

    @Override
    public void render() {
        renderGameMap();
        renderParticles();
        super.render();
        // render the mesh of the quadTree
        drawMeshRecur(quadTree.getRoot());
        renderer.drawText(width / 2f, 100.f, "hello", 0.f, 1.f, 0.f, 1.f);
        if (gameLogic.isGameOver()) {
            float cx = renderer.getWidth() / 2.0f;
            float cy = renderer.getHeight() / 2.0f;
            renderer.fillRect(0, 0, renderer.getWidth(), renderer.getHeight(), 0.0f, 0.0f, 0.0f, 0.35f);
            renderer.fillRect(cx - 200, cy - 60, 400, 120, 0.0f, 0.0f, 0.0f, 0.7f);
            renderer.drawText(cx - 100, cy - 10, "GAME OVER", 1.0f, 1.0f, 1.0f, 1.0f);
            renderer.drawText(cx - 180, cy + 30, "PRESS ANY KEY TO RETURN", 0.8f, 0.8f, 0.8f, 1.0f);
        }

    }

    public float randomFloat() {
        return random.nextFloat();
    }

    public IRenderer getRenderer() {
        return renderer;
    }

    @Override
    public void clear() {
        gameLogic.cleanup();
        super.clear();
    }
}
