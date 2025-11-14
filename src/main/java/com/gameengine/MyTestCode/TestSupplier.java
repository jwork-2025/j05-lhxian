package com.gameengine.MyTestCode;

import java.util.ArrayList;
import java.util.function.Supplier;

public class TestSupplier {
    public static class A{
        String name;
        int val;
        public A(){
            name ="hello";
            val =100;
        }
        @Override
        public String toString(){
            return name+"," +val;
        }
    }
    public static void main(String[] args) {
        ArrayList<String> a =new ArrayList<>(20);
        System.out.println(a.size());
    }
}
