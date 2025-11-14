package com.gameengine.MyTestCode;

public class TestSynchronized {
    public TestSynchronized(){

    }
    public synchronized void g(){
        System.out.println("int method g");
    }
    public synchronized void f(){
        System.out.println("in method f");
        g();
    }
    public static void main(String[] args) {
        TestSynchronized test =new TestSynchronized();
        test.f();
    }
}
