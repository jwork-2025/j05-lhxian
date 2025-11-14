package com.gameengine.demo;


import com.gameengine.components.TransformComponent;
import com.gameengine.constant.MyConst;
import com.gameengine.core.GameEngine;
import com.gameengine.graphics.IRenderer;
import com.gameengine.graphics.MyImage;
import com.gameengine.input.InputManager;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;
import com.gameengine.demo.EntityFactory;

import java.io.File;
import java.util.*;

public class ReplayScene extends Scene {
    private final GameEngine engine;
    private String recordingPath;
    private IRenderer renderer;
    private InputManager input;
    private float time;
    private boolean DEBUG_REPLAY = false;
    private float debugAccumulator = 0f;

    private static class Keyframe {
        static class EntityInfo {
            Vector2 pos;
            /**
             * for player and enemy, 0 if bullet;
             */
            int blood,order;
            String rt; // RECTANGLE/CIRCLE/LINE/CUSTOM/null
            float w, h;
            float r=0.9f,g=0.9f,b=0.2f,a=1.0f; // 默认颜色
            String id;
        }
        double t;
        java.util.List<EntityInfo> entities = new ArrayList<>();
    }

    private final List<Keyframe> keyframes = new ArrayList<>();
//    private final java.util.List<LightGameObject> objectList = new ArrayList<>();
    private final Map<Integer,LightGameObject> objMap = new TreeMap<>();

    // 如果 path 为 null，则先展示 recordings 目录下的文件列表，供用户选择
    public ReplayScene(GameEngine engine, String path) {
        super("Replay");
//        super(engine, MyConst.WIDTH,MyConst.HEIGHT);
        this.engine = engine;
        this.recordingPath = path;
    }

    private void createStill(){
        final int tree_padding = 32 * 3;
        final int moveableAreaX =MyConst.WATER_WIDTH, moveableAreaY = MyConst.WATER_HEIGHT;
        final int moveableAreaEndX = MyConst.WIDTH - MyConst.WATER_WIDTH, moveableAreaEndY = MyConst.HEIGHT- MyConst.WATER_HEIGHT;
        for (int i = 0; i < 3; ++i)
            addGameObject(EntityFactory.createTree(renderer, moveableAreaX + tree_padding + i * MyConst.TREE_WIDTH, moveableAreaY + tree_padding));
        for (int i = 0; i < 2; ++i)
            addGameObject(EntityFactory.createTree(renderer, moveableAreaX + tree_padding, moveableAreaY + tree_padding + (i + 1) * MyConst.TREE_HEIGHT));

        for (int i = 0; i < 3; ++i)
            addGameObject(EntityFactory.createTree(renderer, moveableAreaEndX - tree_padding - (i - 1) * MyConst.TREE_WIDTH, moveableAreaY + tree_padding));
        for (int i = 0; i < 2; ++i)
            addGameObject(EntityFactory.createTree(renderer, moveableAreaEndX - tree_padding + MyConst.TREE_WIDTH, moveableAreaY + tree_padding + (i + 1) * MyConst.TREE_HEIGHT));

        for (int i = 0; i < 3; ++i)
            addGameObject(EntityFactory.createTree(renderer, moveableAreaX + tree_padding + i * MyConst.TREE_WIDTH, moveableAreaEndY - MyConst.TREE_HEIGHT - tree_padding));
        for (int i = 0; i < 2; ++i)
            addGameObject(EntityFactory.createTree(renderer, moveableAreaX + tree_padding, moveableAreaEndY - MyConst.TREE_HEIGHT - tree_padding - (i + 1) * MyConst.TREE_HEIGHT));

        for (int i = 0; i < 3; ++i)
            addGameObject(EntityFactory.createTree(renderer, moveableAreaEndX - tree_padding - (i - 1) * MyConst.TREE_WIDTH, moveableAreaEndY - MyConst.TREE_HEIGHT - tree_padding));
        for (int i = 0; i < 2; ++i)
            addGameObject(EntityFactory.createTree(renderer, moveableAreaEndX - tree_padding + MyConst.TREE_WIDTH, moveableAreaEndY - MyConst.TREE_HEIGHT - tree_padding - (i + 1) * MyConst.TREE_HEIGHT));
//        for(int i=0;i<4;++i) createStill(Stone.class,500+i*32,300);
        for (int y = 300; y < 700; y += MyConst.STONE_WIDTH) addGameObject( EntityFactory.createStone(renderer, MyConst.WIDTH/ 2, y));
        for (int i = 0; i < 3; ++i) {
            addGameObject(EntityFactory.createSpring(renderer, MyConst.WIDTH/ 2 + 64 + i * MyConst.SPRING_WIDTH, MyConst.HEIGHT/ 2));
            addGameObject(EntityFactory.createSpring(renderer, MyConst.WIDTH/ 2 + 64 + i * MyConst.SPRING_WIDTH, MyConst.HEIGHT/ 2 + 2 * MyConst.SPRING_HEIGHT));
        }
        addGameObject(EntityFactory.createSpring(renderer, MyConst.WIDTH / 2 + 64, MyConst.HEIGHT/ 2 + MyConst.SPRING_HEIGHT));
        addGameObject(EntityFactory.createSpring(renderer, MyConst.WIDTH / 2 + 64 + 2 * MyConst.SPRING_WIDTH, MyConst.HEIGHT/ 2 + MyConst.SPRING_HEIGHT));
//        System.out.println("create still finish");
    }
    @Override
    public void initialize() {
        super.initialize();
        this.renderer = engine.getRenderer();
        this.input = engine.getInputManager();
        // 重置状态，防止从列表进入后残留
        this.time = 0f;
        this.keyframes.clear();
        this.objMap.clear();
        if (recordingPath != null) {
            loadRecording(recordingPath);
            buildObjectsFromFirstKeyframe();
            createStill();
        } else {
            // 仅进入文件选择模式
            this.recordingFiles = null;
            this.selectedIndex = 0;
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        gameObjects.forEach(obj->obj.update(deltaTime));
        objMap.entrySet().removeIf(entry->!entry.getValue().isActive());
//        Iterator<Map.Entry<Integer,LightGameObject>> iterator = objMap.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<Integer,LightGameObject> entry = iterator.next();
//            if (!entry.getValue().isActive()) {
//                iterator.remove();
//            }
//        }
        if (input.isKeyJustPressed(256) || input.isKeyJustPressed(259) || input.isKeyJustPressed(27) || input.isKeyJustPressed(8)) { // ESC/BACK
            engine.setScene(new MenuScene(engine, "MainMenu"));
            return;
        }
        // 文件选择模式
        if (recordingPath == null) {
            handleFileSelection();
            return;
        }

        if (keyframes.size() < 1) return;
        time += deltaTime;
        // 限制在最后关键帧处停止（也可选择循环播放）
        double lastT = keyframes.get(keyframes.size() - 1).t;
        if (time > lastT) {
            time = (float)lastT;
        }

        // 查找区间
        Keyframe a = keyframes.get(0);
        Keyframe b = keyframes.get(keyframes.size() - 1);
        for (int i = 0; i < keyframes.size() - 1; i++) {
            Keyframe k1 = keyframes.get(i);
            Keyframe k2 = keyframes.get(i + 1);
            if (time >= k1.t && time <= k2.t) { a = k1; b = k2; break; }
        }
        double span = Math.max(1e-6, b.t - a.t);
        double u = Math.min(1.0, Math.max(0.0, (time - a.t) / span));
        // 调试输出节流


        updateInterpolatedPositions(a, b, (float)u);
    }

    MyImage gameMap =GameMap.drawMapImage();

    @Override
    public void render() {
        renderer.drawRect(0, 0, renderer.getWidth(), renderer.getHeight(), 0.06f, 0.06f, 0.08f, 1.0f);
        if (recordingPath == null) {
            renderFileList();
            return;
        }
        renderer.drawImage(gameMap,0,0,0,0,gameMap.getWidth(),gameMap.getHeight());
        objMap.forEach((k,v)->v.render());
        // 基于 Transform 手动绘制（回放对象没有附带 RenderComponent）
        super.render();
//        System.out.println("game object cnt:"+gameObjects.size());
        String hint = "REPLAY: ESC to return";
        float w = hint.length() * 12.0f;
        renderer.drawText(renderer.getWidth()/2.0f - w/2.0f, 30, hint, 0.8f, 0.8f, 0.8f, 1.0f);
    }

    private void loadRecording(String path) {
        keyframes.clear();
        com.gameengine.recording.RecordingStorage storage = new com.gameengine.recording.FileRecordingStorage();
        try {
            for (String line : storage.readLines(path)) {
                if (line.contains("\"type\":\"keyframe\"")) {
                    Keyframe kf = new Keyframe();
                    kf.t = com.gameengine.recording.RecordingJson.parseDouble(com.gameengine.recording.RecordingJson.field(line, "t"));
                    // 解析 entities 列表中的若干 {"id":"name","x":num,"y":num}
                    int idx = line.indexOf("\"entities\":[");
                    if (idx >= 0) {
                        int bracket = line.indexOf('[', idx);
                        String arr = bracket >= 0 ? com.gameengine.recording.RecordingJson.extractArray(line, bracket) : "";
                        String[] parts = com.gameengine.recording.RecordingJson.splitTopLevel(arr);
                        for (String p : parts) {
                            Keyframe.EntityInfo ei = new Keyframe.EntityInfo();
                            ei.id = com.gameengine.recording.RecordingJson.stripQuotes(com.gameengine.recording.RecordingJson.field(p, "id"));
                            ei.order = com.gameengine.recording.RecordingJson.parserInt(com.gameengine.recording.RecordingJson.field(p, "order"));
                            double x = com.gameengine.recording.RecordingJson.parseDouble(com.gameengine.recording.RecordingJson.field(p, "x"));
                            double y = com.gameengine.recording.RecordingJson.parseDouble(com.gameengine.recording.RecordingJson.field(p, "y"));
                            ei.pos = new Vector2((float)x, (float)y);
                            //
                            ei.blood = (int)com.gameengine.recording.RecordingJson.parseDouble(com.gameengine.recording.RecordingJson.field(p, "HP"));
                            String rt = com.gameengine.recording.RecordingJson.stripQuotes(com.gameengine.recording.RecordingJson.field(p, "rt"));
                            ei.rt = rt;
                            ei.w = (float)com.gameengine.recording.RecordingJson.parseDouble(com.gameengine.recording.RecordingJson.field(p, "w"));
                            ei.h = (float)com.gameengine.recording.RecordingJson.parseDouble(com.gameengine.recording.RecordingJson.field(p, "h"));
                            String colorArr = com.gameengine.recording.RecordingJson.field(p, "color");
                            if (colorArr != null && colorArr.startsWith("[")) {
                                String c = colorArr.substring(1, Math.max(1, colorArr.indexOf(']', 1)));
                                String[] cs = c.split(",");
                                if (cs.length >= 3) {
                                    try {
                                        ei.r = Float.parseFloat(cs[0].trim());
                                        ei.g = Float.parseFloat(cs[1].trim());
                                        ei.b = Float.parseFloat(cs[2].trim());
                                        if (cs.length >= 4) ei.a = Float.parseFloat(cs[3].trim());
                                    } catch (Exception ignored) {}
                                }
                            }
                            kf.entities.add(ei);
                        }
                    }
                    keyframes.add(kf);
                }
            }
        } catch (Exception e) {

        }
        keyframes.sort(Comparator.comparingDouble(k -> k.t));
    }

    private void buildObjectsFromFirstKeyframe() {
        if (keyframes.isEmpty()) return;
        Keyframe kf0 = keyframes.get(0);
        // 按实体构建对象（使用预制），实现与游戏内一致外观
        objMap.clear();
        clear();
        for (int i = 0; i < kf0.entities.size(); i++) {
            Keyframe.EntityInfo tem_info =kf0.entities.get(i);
            LightGameObject obj = buildObjectFromEntity(tem_info, i);
            objMap.put(tem_info.order,obj);
//            addGameObject(obj);
//            objectSet.add(obj);
//            System.out.println("add:"+tem_info.order);
            objMap.put(tem_info.order,obj);
        }
        time = 0f;
    }

//    private void ensureObjectCount(int n) {
//        while (objectSet.size() < n) {
//            LightGameObject obj = new LightGameObject("RObj#" + objectList.size());
//            obj.addComponent(new TransformComponent(new Vector2(0, 0)));
//            // 为回放对象添加可渲染组件（默认外观，稍后在 refreshRenderFromKeyframe 应用真实外观）
//            addLightGameObject(obj);
//            objectList.add(obj);
//        }
//        while (objectList.size() > n) {
//            LightGameObject obj = objectList.remove(objectList.size() - 1);
//            obj.setActive(false);
//        }
//    }


    private void updateInterpolatedPositions(Keyframe a,Keyframe b, float u) {
//        int n = Math.min(a.entities.size(), b.entities.size());
//        ensureObjectCount(n);
        for(Map.Entry<Integer,LightGameObject> e: objMap.entrySet())  e.getValue().setActive(false);
        // set base position
        for(Keyframe.EntityInfo a_info: a.entities){
            LightGameObject target =objMap.get(a_info.order);
            if(target != null) {
                target.setBlood(a_info.blood);
                target.setBase_pos(a_info.pos);
            }
        }
        for(Keyframe.EntityInfo next_info: b.entities){
            LightGameObject target =objMap.get(next_info.order);
            if(target != null){
                // set obj active
                target.setActive(true);
                // interpolate position
//                Vector2 pa = a.entities.get(i).pos;
//                Vector2 pb = b.entities.get(i).pos;
                Vector2 pa =target.getBase_pos();
                Vector2 pb =next_info.pos;
                float x = (float)((1.0 - u) * pa.x + u * pb.x);
                float y = (float)((1.0 - u) * pa.y + u * pb.y);
                TransformComponent tc = target.getComponent(TransformComponent.class);
                if (tc != null) tc.setPosition(new Vector2(x, y));

            }else{
                // add new object
                LightGameObject new_obj =buildObjectFromEntity(next_info,0);
                new_obj.setBase_pos(next_info.pos);
                objMap.put(next_info.order,new_obj);
                TransformComponent tc = new_obj.getComponent(TransformComponent.class);
                if (tc != null) tc.setPosition(new Vector2(next_info.pos));
            }
        }
//        for (int i = 0; i < n; i++) {
//            Vector2 pa = a.entities.get(i).pos;
//            Vector2 pb = b.entities.get(i).pos;
//            float x = (float)((1.0 - u) * pa.x + u * pb.x);
//            float y = (float)((1.0 - u) * pa.y + u * pb.y);
//            LightGameObject obj = objectList.get(i);
//            TransformComponent tc = obj.getComponent(TransformComponent.class);
//            if (tc != null) tc.setPosition(new Vector2(x, y));
//        }
    }

    private LightGameObject buildObjectFromEntity(Keyframe.EntityInfo ei, int index) {
        LightGameObject obj;
        if ("Player".equalsIgnoreCase(ei.id)) {
            obj = com.gameengine.demo.EntityFactory.createPlayerVisual(renderer,ei.blood);
        } else if ("Enemy".equalsIgnoreCase(ei.id)) {
//            float w2 = (ei.w > 0 ? ei.w : 20);
//            float h2 = (ei.h > 0 ? ei.h : 20);
//            obj = com.gameengine.demo.EntityFactory.createAIVisual(renderer, w2, h2, ei.r, ei.g, ei.b, ei.a);
            obj = com.gameengine.demo.EntityFactory.createEnemyVisual(renderer,ei.blood);
        }else if("Bullet".equalsIgnoreCase(ei.id)){
            if(ei.w < 0.-2f) throw new RuntimeException("error radius");
            obj = EntityFactory.createBullet(renderer,ei.w/2,ei.r,ei.g,ei.b,ei.a);
        } else {
            throw new RuntimeException("error");
        }
        TransformComponent tc = obj.getComponent(TransformComponent.class);
        if (tc == null) obj.addComponent(new TransformComponent(new Vector2(ei.pos)));
        else tc.setPosition(new Vector2(ei.pos));
        return obj;
    }

    // ========== 文件列表模式 ==========
    private List<File> recordingFiles;
    private int selectedIndex = 0;

    private void ensureFilesListed() {
        if (recordingFiles != null) return;
        com.gameengine.recording.RecordingStorage storage = new com.gameengine.recording.FileRecordingStorage();
        recordingFiles = storage.listRecordings();
    }

    private void handleFileSelection() {
        ensureFilesListed();
        if (input.isKeyJustPressed(38) || input.isKeyJustPressed(265)) { // up (AWT 38 / GLFW 265)
            selectedIndex = (selectedIndex - 1 + Math.max(1, recordingFiles.size())) % Math.max(1, recordingFiles.size());
        } else if (input.isKeyJustPressed(40) || input.isKeyJustPressed(264)) { // down (AWT 40 / GLFW 264)
            selectedIndex = (selectedIndex + 1) % Math.max(1, recordingFiles.size());
        } else if (input.isKeyJustPressed(10) || input.isKeyJustPressed(32) || input.isKeyJustPressed(257) || input.isKeyJustPressed(335)) { // enter/space (AWT 10/32, GLFW 257/335)
            if (recordingFiles.size() > 0) {
                String path = recordingFiles.get(selectedIndex).getAbsolutePath();
                this.recordingPath = path;
                clear();
                initialize();
            }
        } else if (input.isKeyJustPressed(27)) { // esc
            engine.setScene(new MenuScene(engine, "MainMenu"));
        }
    }

    private void renderFileList() {
        ensureFilesListed();
        int w = renderer.getWidth();
        int h = renderer.getHeight();
        String title = "SELECT RECORDING";
        float tw = title.length() * 16f;
        renderer.drawText(w/2f - tw/2f, 80, title, 1f,1f,1f,1f);

        if (recordingFiles.isEmpty()) {
            String none = "NO RECORDINGS FOUND";
            float nw = none.length() * 14f;
            renderer.drawText(w/2f - nw/2f, h/2f, none, 0.9f,0.8f,0.2f,1f);
            String back = "ESC TO RETURN";
            float bw = back.length() * 12f;
            renderer.drawText(w/2f - bw/2f, h - 60, back, 0.7f,0.7f,0.7f,1f);
            return;
        }

        float startY = 140f;
        float itemH = 28f;
        for (int i = 0; i < recordingFiles.size(); i++) {
            String name = recordingFiles.get(i).getName();
            float x = 100f;
            float y = startY + i * itemH;
            if (i == selectedIndex) {
                renderer.fillRect(x - 10, y - 6, 600, 24, 0.3f,0.3f,0.4f,0.8f);
            }
            renderer.drawText(x, y, name, 0.9f,0.9f,0.9f,1f);
        }

        String hint = "UP/DOWN SELECT, ENTER PLAY, ESC RETURN";
        float hw = hint.length() * 12f;
        renderer.drawText(w/2f - hw/2f, h - 60, hint, 0.7f,0.7f,0.7f,1f);
    }

    // 解析相关逻辑已移至 RecordingJson
}


