package com.gameengine.MyTestCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Random;
import java.util.ArrayList;


public class Test {
    private static final int THRESHOLD=4, CHILD_CNT =4;
    private static final int SEARCH_WIDTH =100, SEARCH_HEIGHT =100;
    public Test(){}
    private class MyPanel extends JPanel implements MouseMotionListener {
        private Tree  quadTree;
        private Rect focus_rect;
        ArrayList<Point> activePoints=null;
        public MyPanel(Tree tree,int width,int height){
            focus_rect =new Rect(0,0,0,0);
            addMouseMotionListener(this);
            quadTree = tree;
        }
        public void setQuadTree(Tree quadTree) {
            this.quadTree = quadTree;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int x =e.getX(), y=e.getY();
//            focus_rect =new Rect(x-SEARCH_WIDTH/2,y-SEARCH_HEIGHT/2,SEARCH_WIDTH,SEARCH_HEIGHT);
            focus_rect.x =x -SEARCH_WIDTH/2;
            focus_rect.y =y -SEARCH_HEIGHT/2;
            focus_rect.w =SEARCH_WIDTH;
            focus_rect.h =SEARCH_HEIGHT;

            if(activePoints != null){
                for(Point p: activePoints) p.setActive(false);
            }
            activePoints=quadTree.findNeibor(focus_rect);
//            System.out.println(activePoints.size());
            for(Point p: activePoints) p.setActive(true);
//            System.out.println("mouse move:"+x+","+y);
            repaint();
        }
        @Override
        public void mouseDragged(MouseEvent e){
            // TODO
        }


        @Override
        protected void paintComponent(Graphics graphics){
            super.paintComponent(graphics);
            // TODO
//            System.out.println("panel need to paint");
            Graphics2D g2d = (Graphics2D)graphics;
            quadTree.visual(g2d);
            if(focus_rect.w ==SEARCH_WIDTH) {
                g2d.setColor(Color.RED);
                g2d.drawRect(focus_rect.x , focus_rect.y, SEARCH_WIDTH, SEARCH_HEIGHT);
                g2d.setColor(Color.BLACK);
            }
        }
    }

    public static void main(String[] args) {
        // TODO
        int width =800, height =600;
        Test test = new Test();
        Tree quadTree = test.new Tree(width,height);
        Random random = new Random();
        ArrayList<Point> ps=new ArrayList<>();
        for(int i=0;i<100;++i){
            int x =random.nextInt(width), y =random.nextInt(height);
            Point add_point = test.new Point(x,y);
            quadTree.add(add_point);
            ps.add(add_point);
        }
        JFrame frame =new JFrame("quadTree");
        JPanel panel =test.new MyPanel(quadTree,width,height);
        panel.setPreferredSize(new Dimension(width,height));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
//        System.out.println("show quad tree");
//        quadTree.show();
//        System.out.println("begin remove point");
//        for(int i=0;i<20;++i) quadTree.remove(ps.get(i));
//        System.out.println("show quad tree after remove");
//        quadTree.show();
//        System.out.println("end");

    }
    public static int encode(Rect area,Rect target){
        // target must in the area;
        int code =0;
        if(target.x >=area.x+target.w) code |= 1;
        if(target.y >= area.y + target.h) code |=2;
        return code;
    }
    public static int encode(Rect area,Point point){
        int half_w  =(area.w+1) /2, half_h = (area.h /2);
        int code =0;
        if(point.x >= area.x + half_w) code |= 1;
        if(point.y >= area.y + half_h) code |= 2;
        return code;
    }
    public static boolean pointInRect(Rect rect,int x,int y){
         return (x >= rect.x && x < rect.x + rect.w && y >= rect.y && y < rect.y + rect.h);
    }
    public static boolean pointInRect(Rect rect,Point point) {
        return pointInRect(rect,point.x,point.y);
    }
    public static boolean rectInter(Rect r1,Rect r2){
        int max_x =Math.max(r1.x,r2.x);
        int max_y =Math.max(r1.y,r2.y);
        return pointInRect(r1,max_x,max_y) && pointInRect(r2,max_x,max_y);
    }
    public class Point{
        public int x,y;

        public void setActive(boolean active) {
            this.active = active;
        }

        private boolean active=false;
        public Point(int x,int y){
            this.x=x;
            this.y=y;
        }
    }
    public class Rect{
        public int x,y,w,h;
        public Rect(int x,int y,int w,int h){
            this.x =x;
            this.y =y;
            this.w =w;
            this.h =h;
        }
    }





    public class Tree{
        private int width, height;
        Node root;
        private int tab_cnt =0;
        public final int TAB_SIZE =4;

        public Tree(int w,int h){
            width =w;
            height =h;
            root = new Node(null,0,0,w,h);
        }
        public void show(){
            show_recur(root);
        }
        private void resetPoint_recur(Node cur_node){
            if(cur_node.isLeaf()){
                for(Point p: cur_node.content) p.setActive(false);
            }else{
                for(int i=0;i<CHILD_CNT;++i) resetPoint_recur(cur_node.child[i]);
            }
        }
        public void resetPoint(){

        }
        private void show_recur(Node cur_node){
            for(int i=0;i<tab_cnt*TAB_SIZE;++i) System.out.print(' ');
            System.out.println("node rect: "+cur_node.total_ele+","+cur_node.rect.x+","+cur_node.rect.y+","+cur_node.rect.w+","+cur_node.rect.h);
            ++tab_cnt;
            if(cur_node.isLeaf()){
                for(Point p: cur_node.content){
                    for(int i=0;i<tab_cnt * TAB_SIZE;++i) System.out.print(' ');
                    System.out.println("point: "+p.x+","+p.y);
                }
            }else{
                for(int i=0;i<CHILD_CNT;++i) show_recur(cur_node.child[i]);
            }
            --tab_cnt;

        }
        public void visual(Graphics2D g2d){
            visual_recur(root,g2d);
        }
        private  static final int POINT_SIZE =4;
        private void visual_recur(Node cur_node,Graphics2D g2d){
            Rect r= cur_node.rect;
            g2d.drawRect(r.x,r.y,r.w,r.h);
            if(cur_node.isLeaf()){
                for(Point p: cur_node.content){
                    if(p.active) g2d.setColor(Color.GREEN);
                    g2d.fillOval(p.x-POINT_SIZE,p.y-POINT_SIZE,2*POINT_SIZE,2*POINT_SIZE);
                    g2d.setColor(Color.BLACK);
                }
            }else{
                for(int i =0;i<CHILD_CNT;++i) visual_recur(cur_node.child[i],g2d);
            }
        }
        public void add(Point point){
            Node cur_node = root;
            while(!cur_node.isLeaf()){
                cur_node.total_ele +=1;
                int index = encode(cur_node.rect,point) ;
                cur_node =cur_node.child[index];
            }
            cur_node.total_ele += 1;
            cur_node.content.add(point);
            if(cur_node.content.size() > THRESHOLD){
                cur_node.split();
            }
        }
        public Node findPos(Point point){
            Node res = root;
            while(!res.isLeaf()){
                int index =encode(res.rect,point);
                res = res.child[index];
            }
            return res;
        }
        public void merge(Node cur_node){
            for(int i=0;i<CHILD_CNT;++i){
                cur_node.content.addAll(cur_node.child[i].content);
            }
            cur_node.child =null;
        }
        public boolean remove(Point point){
            Node contain_node =findPos(point);
            if(contain_node == null) return false;
            contain_node.total_ele -=1;
            if(!contain_node.content.remove(point)) return false;
            Node back = contain_node.parent;
            while(back != null){
                back.total_ele -=1;
                if(back.total_ele <=THRESHOLD){
                    // merge
                    merge(back);
                }
                back = back.parent;
            }
            return true;

        }
        public boolean change(Point point,int x,int y){
            // find the target point
            Node  origin_node=findPos(point);
            point.x =x;
            point.y =y;
            if(pointInRect(origin_node.rect,x,y)) return false;
            // need to change node;
            remove(point);
            add(point);
            return true;
        }
        public ArrayList<Point> findNeibor(Rect bounding_rect){
            ArrayList<Point> res =new ArrayList<>(THRESHOLD);
            Node cur_node =root;
            findNeibor_recur(root,bounding_rect,res);
            return res;
        }
        public void findNeibor_recur(Node cur_node,Rect bounding_rect,ArrayList<Point> res){
            if(cur_node.isLeaf()){
                for(Point p:cur_node.content){
                    if(pointInRect(bounding_rect,p)) res.add(p);
                }
                return;
            }
            for(int i=0;i<CHILD_CNT;++i){
                Node next_node =cur_node.child[i];
                if(rectInter(next_node.rect,bounding_rect)) findNeibor_recur(next_node,bounding_rect,res);
            }
        }

    }
    public class Node{
        private Rect rect;
        private Node parent;
        private Node[] child;
        private ArrayList<Point> content;
        private int total_ele;
        public boolean isLeaf(){return child == null;}
        public Node(Node parent,int x,int y,int w,int h){
            this.parent =parent;
            total_ele =0;
            rect = new Rect(x,y,w,h);
            child= null;
            content = new ArrayList<>();
        }
        private void directAdd(Point point){
            total_ele +=1;
            content.add(point);

        }
        public boolean split(){
//            System.out.println("split");
            if(child != null){
                System.out.println("logic error");
                return false;
            }
            if(rect.w <=1 || rect.h <=1) return false;
            int half_w=(rect.w+1) /2, half_h =(rect.h+1)/ 2;
            child = new Node[CHILD_CNT];
            child[0] =new Node(this,rect.x,rect.y,half_w,half_h);
            child[1] =new Node(this,rect.x+half_w,rect.y,rect.w-half_w,half_h);
            child[2] =new Node(this,rect.x,rect.y+half_h,half_w,rect.h-half_h);
            child[3] =new Node(this,rect.x+half_w,rect.y+half_h,rect.w-half_w,rect.h-half_h);
            for(Point p: content){
                int index =encode(rect,p);
                child[index].directAdd(p);
            }
            content.clear();
            return true;
        }
    }
}
