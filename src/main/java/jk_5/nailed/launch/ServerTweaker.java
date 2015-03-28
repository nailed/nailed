package jk_5.nailed.launch;

import io.netty.util.internal.logging.InternalLoggerFactory;
import jk_5.nailed.server.logging.LoggerOutputStream;
import jk_5.nailed.server.tweaker.patcher.BinPatchManager;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.List;

public class ServerTweaker implements ITweaker {

    private static final Logger logger = LogManager.getLogger();

    private static boolean isObfuscated() {
        try {
            return Launch.classLoader.getClassBytes("net.minecraft.world.World") == null;
        } catch (IOException ignored) {
            return true;
        }
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        NailedLauncher.initialize(gameDir);

        System.setOut(new PrintStream(new LoggerOutputStream(LogManager.getLogger("SYSOUT"), Level.INFO), true));
        System.setErr(new PrintStream(new LoggerOutputStream(LogManager.getLogger("SYSERR"), Level.WARN), true));

        InternalLoggerFactory.getInstance("INITLOGGER");
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader loader) {
        logger.info("Initializing nailed");

        loader.addClassLoaderExclusion("scala.");
        loader.addClassLoaderExclusion("LZMA.");
        loader.addClassLoaderExclusion("com.google.");
        loader.addClassLoaderExclusion("com.nothome.delta.");
        loader.addClassLoaderExclusion("org.apache.");
        loader.addClassLoaderExclusion("com.mojang.");
        loader.addClassLoaderExclusion("org.fusesource.");
        loader.addClassLoaderExclusion("io.netty.");
        loader.addClassLoaderExclusion("gnu.trove.");
        loader.addClassLoaderExclusion("joptsimple.");
        loader.addClassLoaderExclusion("jk_5.nailed.server.mixin.");
        loader.addClassLoaderExclusion("jk_5.nailed.launch.");
        loader.addClassLoaderExclusion("jk_5.nailed.server.tweaker.");
        loader.addClassLoaderExclusion("org.spongepowered.tools.");

        BinPatchManager.instance().setup();

        loader.registerTransformer("jk_5.nailed.server.tweaker.transformer.PatchingTransformer");
        loader.registerTransformer("jk_5.nailed.server.tweaker.transformer.EventSubscriptionTransformer");

        logger.info("Applying runtime deobfuscation...");
        if(isObfuscated()){
            Launch.blackboard.put("nailed.deobf-srg", Paths.get("bin", "deobf.srg.gz"));
            loader.registerTransformer("jk_5.nailed.launch.transformers.DeobfuscationTransformer");
            logger.info("Runtime deobfuscation is applied.");
        } else {
            logger.info("Runtime deobfuscation was not applied. Nailed is being loaded in a deobfuscated environment.");
        }

        logger.info("Applying access transformer...");
        Launch.blackboard.put("nailed.at", "nailed_at.cfg");
        loader.registerTransformer("jk_5.nailed.launch.transformers.AccessTransformer");

        logger.info("Initializing Mixin environment...");
        MixinBootstrap.init();
        MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
        env.addConfiguration("mixins.nailed.core.json");
        env.setSide(MixinEnvironment.Side.SERVER);
        loader.registerTransformer(MixinBootstrap.TRANSFORMER_CLASS);

        logger.info("Initialization finished. Starting Minecraft server...");
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.server.MinecraftServer";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[]{"nogui"};
    }
}
