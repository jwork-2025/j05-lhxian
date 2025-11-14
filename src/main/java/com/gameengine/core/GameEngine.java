package com.gameengine.core;

//import com.gameengine.graphics.CPURenderer;
import com.gameengine.graphics.GPURenderer;
import com.gameengine.graphics.IRenderer;
import com.gameengine.graphics.RenderBackend;
import com.gameengine.graphics.RendererFactory;
import com.gameengine.input.InputManager;
import com.gameengine.scene.Scene;
//import javax.swing.Timer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 游戏引擎
 */
public class GameEngine {
    private IRenderer renderer;
    private InputManager inputManager;
    private Scene currentScene;
    private boolean running;
    private float targetFPS;
    private float deltaTime;
    private long lastTime;
    private String title;
//    private Timer gameTimer;
    private float realFPS;
    private com.gameengine.recording.RecordingService recordingService;

    private BufferedWriter writer=null;

    public GameEngine(int width, int height, String title, RenderBackend backend, boolean record) {
        this.title = title;
//        this.renderer = new GPURenderer(width, height, title);
        this.renderer = RendererFactory.createRenderer(backend,width,height,title);
        this.inputManager = InputManager.getInstance();
        this.running = false;
        this.targetFPS = 1000.0f;
        this.deltaTime = 0.0f;
        this.lastTime = System.nanoTime();
        realFPS =0.f;

        if(record){
            try{
                writer = new BufferedWriter(new FileWriter("fps.txt",true));
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 初始化游戏引擎
     */
    public boolean initialize() {
        return true; // Swing渲染器不需要特殊初始化
    }
    
    /**
     * 运行游戏引擎
     */

    public void run() {
        if (!initialize()) {
            System.err.println("游戏引擎初始化失败");
            return;
        }

        running = true;

        if (currentScene != null) {
            currentScene.initialize();

        }

        long lastFrameTime = System.nanoTime();
        long frameTimeNanos = (long)(1_000_000_000.0 / targetFPS);

        while (running) {
            long currentTime = System.nanoTime();

            if (currentTime - lastFrameTime >= frameTimeNanos) {
                update();
                if (running) {
                    render();
                }
                lastFrameTime = currentTime;
            }
            renderer.pollEvents();
            if (running && renderer.shouldClose()) {
                running = false;
                cleanup();
            }


//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException e) {
//                break;
//            }
        }
        System.out.println("engine stop");
    }

    /**
     * 更新游戏逻辑
     */

    private void update() {
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - lastTime) / 1_000_000_000.0f;
        lastTime = currentTime;
        realFPS= 1.f/ deltaTime;
        renderer.setTitle(String.format("GameEngine,FPS:%.2f",realFPS));

        renderer.pollEvents();



        if (currentScene != null) {
            currentScene.update(deltaTime);
        }

//        if (physicsSystem != null) {
//            physicsSystem.update(deltaTime);
//        }
//
        if (recordingService != null && recordingService.isRecording()) {
            recordingService.update(deltaTime, currentScene, inputManager);
        }

        inputManager.update();

        if (inputManager.isKeyPressed(27)) {
            running = false;
            cleanup();
        }

        if (renderer.shouldClose() && running) {
            running = false;
            cleanup();
        }
    }
//    private void update() {
//        // 计算时间间隔
//        long currentTime = System.nanoTime();
//        deltaTime = (currentTime - lastTime) / 1_000_000_000.0f; // 转换为秒
//        lastTime = currentTime;
//
//        realFPS= 1.f/ deltaTime;
//        renderer.setTitle(String.format("GameEngine,FPS:%.2f",realFPS));
//        if(writer != null){
//            try{
//                String r = String.format("%.4f %.2f",currentTime /1_000_000_000.f,realFPS);
//                writer.write(r);
//                writer.newLine();
//                writer.flush();
//            }catch(IOException e){
//                e.printStackTrace();
//            }
//        }
//        // 更新输入
//        inputManager.update();
//
//        // 更新场景
//        if (currentScene != null) {
//            currentScene.update(deltaTime);
//        }
//
//        // 处理事件
//        renderer.pollEvents();
//
//        // 检查退出条件
//        if (inputManager.isKeyPressed(27)) { // ESC键
//            running = false;
//            gameTimer.stop();
//            renderer.cleanup();
//        }
//
//        // 检查窗口是否关闭
//        if (renderer.shouldClose()) {
//            running = false;
//            gameTimer.stop();
//        }
//
//
//    }
    public void closeFile(){
        if(writer == null) return;
        try{
            writer.flush();
            writer.close();
            System.out.println("close file");
            writer = null;
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 渲染游戏
     */
    private void render() {
        renderer.beginFrame();
        
        // 渲染场景
        if (currentScene != null) {
            currentScene.render();
        }
        
        renderer.endFrame();
    }
    
    /**
     * 设置当前场景
     */
    public void setScene(Scene scene) {
        this.currentScene = scene;
        if (scene != null && running) {
            scene.initialize();
        }
    }
    
    /**
     * 获取当前场景
     */
    public Scene getCurrentScene() {
        return currentScene;
    }
    
    /**
     * 停止游戏引擎
     */
    public void stop() {
        running = false;
//        if (gameTimer != null) {
//            gameTimer.stop();
//        }
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        if (currentScene != null) {
            currentScene.clear();
        }
        renderer.cleanup();
    }
    
    /**
     * 获取渲染器
     */
    public IRenderer getRenderer() {
        return renderer;
    }
    
    /**
     * 获取输入管理器
     */
    public InputManager getInputManager() {
        return inputManager;
    }
    
    /**
     * 获取时间间隔
     */
    public float getDeltaTime() {
        return deltaTime;
    }
    
    /**
     * 设置目标帧率
     */
    public void setTargetFPS(float fps) {
        this.targetFPS = fps;
//        if (gameTimer != null) {
//            gameTimer.setDelay((int) (1000 / fps));
//        }
    }
    
    /**
     * 获取目标帧率
     */
    public float getTargetFPS() {
        return targetFPS;
    }
    
    /**
     * 检查引擎是否正在运行
     */
    public boolean isRunning() {
        return running;
    }
    // 可选：外部启用录制（按需调用）
    public void enableRecording(com.gameengine.recording.RecordingService service) {
        this.recordingService = service;
        try {
            if (service != null && currentScene != null) {
                service.start(currentScene, renderer.getWidth(), renderer.getHeight());
            }
        } catch (Exception e) {
            System.err.println("录制启动失败: " + e.getMessage());
        }
        throw new UnsupportedOperationException();
    }

    public void disableRecording() {
        if (recordingService != null && recordingService.isRecording()) {
            try { recordingService.stop(); } catch (Exception ignored) {}
        }
        recordingService = null;
    }
}
