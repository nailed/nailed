package jk_5.nailed.server.mixin.core;

public class MixinTarget {

    public static void main(String[] args){
        new MixinTarget().something();
    }

    public void something(){
        System.out.println("Something 1");
    }
}
