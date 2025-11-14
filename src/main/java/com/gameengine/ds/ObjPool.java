package com.gameengine.ds;


import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

public class ObjPool<T> {
    private final ConcurrentLinkedDeque<T> deque=new ConcurrentLinkedDeque<>();
    private final Supplier<T> supplier;
    public ObjPool(Supplier<T> supplier){
        this.supplier = supplier;
    }
//    public T borrow() {
//        return pool.isEmpty() ? supplier.get(): pool.pop();
//    }

    public T borrow() {
        T obj = deque.poll();
        if (obj == null) {
            obj = supplier.get();
        }
        return obj;
    }

    public void release(T p) {
//        pool.push(p);
        deque.add(p);
    }
}
