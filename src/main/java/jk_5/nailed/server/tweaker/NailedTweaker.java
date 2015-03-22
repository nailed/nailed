package jk_5.nailed.server.tweaker;

import io.netty.util.internal.logging.InternalLoggerFactory;
import jk_5.nailed.server.logging.LoggerOutputStream;
import jk_5.nailed.server.tweaker.patcher.BinPatchManager;
import jk_5.nailed.server.tweaker.remapping.NameRemapper;
import jk_5.nailed.server.tweaker.transformer.AccessTransformer;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class NailedTweaker implements ITweaker {

    private static final Logger logger = LogManager.getLogger();
    public static final LaunchClassLoader classLoader = Launch.classLoader;

    public static File gameDir;
    public static boolean deobf = false;
    public static boolean acceptEula = true;

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        //Step 1 - Parse command line arguments
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();

        parser.accepts("accept-eula", "Accept the EULA, so you don't need to change the eula.txt file");

        OptionSet options;
        try{
            options = parser.parse(args.toArray(new String[args.size()]));
        }catch(OptionException e){
            logger.fatal("Error while parsing arguments: " + e.getLocalizedMessage());
            System.exit(1);
            options = null;
        }

        if(options.has("accept-eula")){
            NailedTweaker.acceptEula = true;
        }

        //Step 2 - Read configuration
        NailedTweaker.gameDir = gameDir;
        NailedVersion.readConfig();

        logger.info("Initializing Nailed version " + NailedVersion.full);
        if(NailedVersion.isSnapshot){
            logger.info("This is a snapshot version. It might be instable/buggy");
        }

        BinPatchManager.setup();
        NameRemapper.init();

        InternalLoggerFactory.getInstance("INITLOGGER"); //Force netty's logger to initialize
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        try{
            byte[] bytes = classLoader.getClassBytes("net.minecraft.world.World");
            if(bytes == null){
                logger.info("Obfuscated environment detected");
                logger.info("Enabling runtime deobfuscation");
                deobf = false;
            }else{
                logger.info("Deobfuscated environment detected");
                deobf = true;
            }
        }catch(IOException e){
            logger.info("Obfuscated environment detected");
            logger.info("Enabling runtime deobfuscation");
            deobf = false;
        }

        classLoader.addClassLoaderExclusion("scala.");
        classLoader.addClassLoaderExclusion("LZMA.");
        classLoader.addClassLoaderExclusion("com.google.");
        classLoader.addClassLoaderExclusion("com.nothome.delta.");
        classLoader.addClassLoaderExclusion("org.apache.");
        classLoader.addClassLoaderExclusion("com.mojang.");
        classLoader.addClassLoaderExclusion("org.fusesource.");
        classLoader.addTransformerExclusion("jk_5.nailed.server.tweaker.transformer.");
        classLoader.registerTransformer("jk_5.nailed.server.tweaker.transformer.PatchingTransformer");
        classLoader.registerTransformer("jk_5.nailed.server.tweaker.transformer.EventSubscribtionTransformer");
        if(!NailedTweaker.deobf) classLoader.registerTransformer("jk_5.nailed.server.tweaker.transformer.RemappingTransformer");
        classLoader.registerTransformer("jk_5.nailed.server.tweaker.transformer.AccessTransformer");

        AccessTransformer.readConfig("nailed_at.cfg");

        // Step 3 - Initialize logging
        System.setOut(new PrintStream(new LoggerOutputStream(LogManager.getLogger("SYSOUT"), Level.INFO), true));
        System.setErr(new PrintStream(new LoggerOutputStream(LogManager.getLogger("SYSERR"), Level.WARN), true));
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.server.MinecraftServer";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}
