package net.minecraftforge.fml.relauncher;

/**
 * Just a little stupid dummy class to make the mixin system shut up about unknown environment
 */
public class FMLLaunchHandler {

    public static enum Side {
        SERVER;
    }

    public static Side side(){
        return Side.SERVER;
    }
}
