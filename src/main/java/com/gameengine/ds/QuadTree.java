package com.gameengine.ds;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class QuadTree<T> {
    // static properties
    public static final int THRESHOLD=4, CHILD_CNT =4;
    protected static int MAX_CHILD =4, MAX_CONTENT_CNT =8;

    protected final ObjPool<Node<T>> node_pool;
    protected final ObjPool<Info<T>> info_pool;

    public static void main(String[] args) {
        int width=800,height =600;
        QuadTree<Integer> tree = new QuadTree<>(width,height);
        Random random = new Random();
        for(int i=0;i<100;++i){
            int x=random.nextInt(width), y = random.nextInt(height);
            tree.add(i,x,y);
        }
    }

    public Node<T> getRoot() {
        return root;
    }

    protected Node<T> root;
    protected int width, height;
    public QuadTree(int w,int h){
        width =w;
        height = h;
        root = new Node<T>(null,0,0,width,height);
        node_pool = new ObjPool<>(Node<T>::new);
        info_pool = new ObjPool<>(Info<T>::new);
    }
    public int size(){
        return root.total_ele;
    }
    public Info<T> createInfo(T ele,int x,int y){
        Info<T> info = info_pool.borrow();
        info.point.x =x;
        info.point.y =y;
        info.val = ele;
        return info;
    }
    public void releaseInfo(Info<T> info){
        info_pool.release(info);
    }
    public Node<T> createNode(Node<T> parent,int x,int y,int w,int h){
        Node<T> node = node_pool.borrow();
        node.parent =parent;
        node.rect.x =x;
        node.rect.y =y;
        node.rect.w =w;
        node.rect.h =h;
        if(!node.content.isEmpty()){
            throw new RuntimeException("error content size");
        }
        return node;
    }
    public void releaseNode(Node<T> node){
        node.child = null;
        node.total_ele =0;
        node.content.clear();
        node_pool.release(node);
    }
    protected void split(Node<T> target_node){
        if(target_node.child != null){
            throw new RuntimeException("logical error");
        }
        Rect rect =target_node.rect;
        if(rect.w <=1 || rect.h <=1) return;
        int half_w=(rect.w+1) /2, half_h =(rect.h+1)/ 2;
        target_node.child = new ArrayList<Node<T>>(CHILD_CNT);
        target_node.child.add(createNode(target_node,rect.x,rect.y,half_w,half_h));
        target_node.child.add(createNode(target_node,rect.x+half_w,rect.y,rect.w-half_w,half_h));
        target_node.child.add(createNode(target_node,rect.x,rect.y+half_h,half_w,rect.h-half_h));
        target_node.child.add(createNode(target_node,rect.x+half_w,rect.y+half_h,rect.w-half_w,rect.h-half_h));

        for(Info<T> info: target_node.content){
            int index = Rect.encode(rect,info.point);
            target_node.child.get(index).directAdd(info);
        }
        target_node.content.clear();

    }
    private void add(Info<T> info){
        Node<T> cur_node = root;
        while(!cur_node.isLeaf()){
            cur_node.total_ele +=1;
            int index = Rect.encode(cur_node.rect,info.point.x,info.point.y) ;
            cur_node =cur_node.child.get(index);
        }
        cur_node.total_ele += 1;
        cur_node.content.add(info);
        if(cur_node.content.size() > THRESHOLD){
//            System.out.println("split");
            split(cur_node);
        }
    }
    public synchronized void add(T ele,int x,int y){
//        Info<T> info = new Info<>(ele,x,y);
        Info<T> info = createInfo(ele,x,y);
        add(info);
    }
    public Node<T> findPos(int x,int y){
        Node<T> res = root;
        while(!res.isLeaf()){
            int index =Rect.encode(res.rect,x,y);
            res = res.child.get(index);
        }
        return res;
    }
    protected void merge(Node<T> cur_node){
        for(int i=0;i<CHILD_CNT;++i){
            Node<T> sub =cur_node.child.get(i);
            cur_node.content.addAll(sub.content);
            releaseNode(sub);
        }
        cur_node.child =null;
    }
    public synchronized boolean remove(Info<T> info){
        Node<T> contain_node =findPos(info.point.x,info.point.y);
        if(contain_node == null) return false;
        int index =contain_node.content.indexOf(info);
        if(index ==-1){
            throw new IllegalStateException("error in remove node");
        }
        Info<T> target_info =contain_node.content.remove(index);
        // release the info
        releaseInfo(target_info);
        contain_node.total_ele -=1;
        Node<T> back = contain_node.parent;
        while(back != null){
            back.total_ele -=1;
            if(back.total_ele <=THRESHOLD){
                // merge
//                System.out.println("merge");
                merge(back);
            }
            back = back.parent;
        }
        return true;

    }
    public synchronized boolean remove(T ele,int x,int y){
        Info<T> tem_info =createInfo(ele,x,y);
        boolean res =remove(tem_info);
        releaseInfo(tem_info);
        return res;

    }
    public synchronized boolean check(T ele,int x,int y){
        Node<T> node = findPos(x,y);
        for(Info<T> info: node.content){
            if(info.val == ele) return true;
        }
        return false;
    }


    public synchronized boolean change(T val,int pre_x,int pre_y,int x,int y){
        // find the target point
        if(pre_x == x && pre_y == y) return false;
        Node<T>  origin_node=findPos(pre_x,pre_y);
        // find the target info;
        Info<T> target_info =null;
        for(Info<T> info : origin_node.content) if(info.val == val) {
            target_info = info;
            break;
        }
        if(target_info == null) {
            System.out.println("error:"+pre_x+","+pre_y+","+x+","+y);
            throw new IllegalStateException("error in change pos");
//            return false;
        }
        if(Rect.pointInRect(origin_node.rect,x,y)){
            target_info.point.x =x;
            target_info.point.y =y;
        }else{
            remove(target_info);
            target_info = createInfo(val,x,y);
//            target_info.point.x =x;
//            target_info.point.y =y;
            // need to change node;

            add(target_info);

        }
        return true;
    }
    public ArrayList<Info<T>> findNeighbor(Rect bounding_rect){
        ArrayList<Info<T>> res =new ArrayList<>(THRESHOLD);
        Node<T> cur_node =root;
        findNeighbor_recur(root,bounding_rect,res);
        return res;
    }
    protected void findNeighbor_recur(Node<T> cur_node,Rect bounding_rect,ArrayList<Info<T>> res){
        if(cur_node.isLeaf()){
            for(Info<T> info: cur_node.content){
                if(Rect.pointInRect(bounding_rect,info.point)) res.add(info);
            }
            return;
        }
        for(int i=0;i<CHILD_CNT;++i){
            Node<T> next_node =cur_node.child.get(i);
            if(Rect.rectInter(next_node.rect,bounding_rect)) findNeighbor_recur(next_node,bounding_rect,res);
        }
    }


    /**
     * class Info
     * @param <T>
     */

    public static class Info<T>{
        public Point point;
        public T val=null;
        public Info(){
            point = new Point(0,0);
            val = null;
        }
        public Info(T ele,int x,int y){
            point = new Point(x,y);
            val =ele;
        }
        @Override
        public boolean equals(Object obj){
            if(obj == this) return true;
            if(obj instanceof Info<?>) return ((Info<?>)obj).val == this.val;
            return false;
        }
    }

    /**
     *
     * class Node
     * @param <T>
     */
    public static class Node<T>{
        public Rect rect;
        protected Node<T> parent;
        protected ArrayList<Node<T>> child;

        public ArrayList<Info<T>> getContent() {
            return content;
        }

        protected ArrayList<Info<T>> content;

        public int getTotal_ele() {
            return total_ele;
        }

        protected int total_ele;

        public boolean isLeaf(){return child == null;}
        public Node(){
            this.parent = null;
            total_ele =0;
            rect =new Rect(0,0,0,0);
            child = null;
            content = new ArrayList<>();
        }
        public Node(Node<T> parent,int x,int y,int w,int h){
            this.parent =parent;
            total_ele =0;
            rect = new Rect(x,y,w,h);
            child= null;
            content = new ArrayList<>();
        }
        public void SetData(Node<T> parent,int x,int y,int w,int h){
            this.parent = parent;
            rect.x =x;
            rect.y =y;
            rect.w =w;
            rect.h =h;
        }
        public Node<T> getChild(int index){
            return child.get(index);
        }
        private void directAdd(Info<T> info){
            total_ele +=1;
            content.add(info);
        }

    }

}

