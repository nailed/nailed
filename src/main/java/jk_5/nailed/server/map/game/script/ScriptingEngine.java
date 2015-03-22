package jk_5.nailed.server.map.game.script;

import jk_5.nailed.api.GameMode;
import jk_5.nailed.api.chat.*;
import jk_5.nailed.api.mappack.filesystem.IMount;
import jk_5.nailed.api.scoreboard.DisplayType;
import jk_5.nailed.api.world.Difficulty;
import jk_5.nailed.api.world.WeatherType;
import jk_5.nailed.server.map.NailedMap;
import jk_5.nailed.server.map.game.NailedGameManager;
import jk_5.nailed.server.map.game.script.api.ScriptMapApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozilla.javascript.*;

import java.io.InputStream;
import java.io.InputStreamReader;

public class ScriptingEngine {

    private static final Logger logger = LogManager.getLogger();

    private final NailedGameManager manager;

    private FileSystem fileSystem;
    private Thread thread;
    private Context context = null;
    private ScriptableObject scope;
    private Script script;

    public ScriptingEngine(NailedGameManager manager) {
        this.manager = manager;
    }

    public boolean start(){
        if(fileSystem != null){
            fileSystem.unload();
        }
        fileSystem = new FileSystem();

        IMount mappackMount = manager.getMap().mappack().getMappackMount();

        if(mappackMount == null){
            return false;
        }

        logger.info("Initializing ScriptingEngine for " + manager.getMap().toString());
        try{
            fileSystem.mount("", manager.getMap().mappack().getMappackMount());
        }catch(FileSystemException e){
            throw new ScriptEngineException("Was not able to mount mappack scripts to the game engine", e);
        }

        try {
            if(!fileSystem.exists("game.js")){
                return false;
            }

            final InputStream gameScript = fileSystem.openForBinaryRead("game.js");
            if(gameScript == null){
                return false;
            }

            thread = newThread(new Runnable() {
                @Override
                public void run() {
                    context = Context.enter();
                    scope = context.initStandardObjects();

                    scope.delete("Packages");
                    scope.delete("getClass");
                    scope.delete("JavaAdapter");
                    scope.delete("JavaImporter");
                    scope.delete("Continuation");
                    scope.delete("java");
                    scope.delete("javax");
                    scope.delete("org");
                    scope.delete("com");
                    scope.delete("edu");
                    scope.delete("net");
                    scope.delete("eval");

                    try{
                        scope.put("map", scope, new NativeJavaObject(scope, new ScriptMapApi((NailedMap) manager.getMap(), context, scope), ScriptMapApi.class));
                        scope.put("sleep", scope, new NativeJavaMethod(Thread.class.getDeclaredMethod("sleep", Long.TYPE), "sleep"));
                        scope.put("BaseComponent", scope, new NativeJavaClass(scope, BaseComponent.class));
                        scope.put("ChatColor", scope, new NativeJavaClass(scope, ChatColor.class));
                        scope.put("ClickEvent", scope, new NativeJavaClass(scope, ClickEvent.class));
                        scope.put("ComponentBuilder", scope, new NativeJavaClass(scope, ComponentBuilder.class));
                        scope.put("HoverEvent", scope, new NativeJavaClass(scope, HoverEvent.class));
                        scope.put("TextComponent", scope, new NativeJavaClass(scope, TextComponent.class));
                        scope.put("TranslatableComponent", scope, new NativeJavaClass(scope, TranslatableComponent.class));
                        scope.put("WeatherType", scope, new NativeJavaClass(scope, WeatherType.class));
                        scope.put("GameMode", scope, new NativeJavaClass(scope, GameMode.class));
                        scope.put("Difficulty", scope, new NativeJavaClass(scope, Difficulty.class));
                        scope.put("DisplayType", scope, new NativeJavaClass(scope, DisplayType.class));

                        script = context.compileReader(new InputStreamReader(gameScript), "game.js", 1, null);
                        gameScript.close();

                        boolean success = false;
                        try{
                            script.exec(context, scope);
                            success = true;
                        }catch(Exception e){
                            manager.getMap().broadcastChatMessage(new ComponentBuilder("The script engine has crashed. The game will be stopped").color(ChatColor.RED).create());
                            logger.fatal("Exception while executing game script. Script engine crashed", e);
                        }finally{
                            manager.onEnded(success);
                        }
                    }catch(Exception e){
                        logger.error("Unknown error in gamescript", e);
                    }
                }
            });
            thread.start();
            return true;
        }catch(FileSystemException e){
            throw new ScriptEngineException("FileSystem exception", e);
        }
    }

    public void kill(){
        this.thread.stop();
        this.fileSystem.unload();
        this.fileSystem = null;
    }

    private Thread newThread(Runnable r){
        Thread t = new Thread(r);
        t.setName("ScriptEngine-" + manager.getMap().id());
        t.setDaemon(true);
        t.setPriority(3);
        return t;
    }
}
