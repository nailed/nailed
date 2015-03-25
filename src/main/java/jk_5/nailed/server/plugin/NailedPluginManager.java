package jk_5.nailed.server.plugin;

import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import jk_5.nailed.api.event.plugin.RegisterAdditionalEventHandlersEvent;
import jk_5.nailed.api.plugin.*;
import jk_5.nailed.server.NailedPlatform;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

public class NailedPluginManager implements PluginManager {

    private static final NailedPluginManager INSTANCE = new NailedPluginManager();

    private final Set<PluginContainer> plugins = new HashSet<PluginContainer>();
    private final LaunchClassLoader classLoader = Launch.classLoader;
    private final Logger logger = LogManager.getLogger();

    private File pluginDir;

    @Nullable
    @Override
    public PluginContainer getPlugin(Object identifier) {
        if(identifier instanceof PluginContainer){
            return ((PluginContainer) identifier);
        }else if(identifier instanceof PluginIdentifier){
            for (PluginContainer plugin : plugins) {
                if(plugin.getIdentifier() == identifier){
                    return plugin;
                }
            }
        }else if(identifier instanceof String){
            for (PluginContainer plugin : plugins) {
                if(plugin.getId().equals(((String) identifier))){
                    return plugin;
                }
            }
        }else{
            for (PluginContainer plugin : plugins) {
                if(plugin.getInstance() == identifier){
                    return plugin;
                }
            }
        }
        return null;
    }

    @Override
    public Collection<PluginContainer> getPlugins() {
        return ImmutableSet.copyOf(plugins);
    }

    public void loadPlugins(File dir){
        this.pluginDir = dir;
        PluginDiscoverer.clearDiscovered();
        PluginDiscoverer.discoverClasspathPlugins();
        PluginDiscoverer.discoverJarPlugins(dir);
        Set<PluginDiscoverer.DiscoveredPlugin> discovered = PluginDiscoverer.getDiscovered();
        logger.info("Discovered " + discovered.size() + " plugins");
        for(PluginDiscoverer.DiscoveredPlugin d : discovered){
            logger.trace("  - {} ({} version: {}) -> {}", d.id, d.name, d.version, d.className);
        }
        for (PluginDiscoverer.DiscoveredPlugin p : discovered) {
            try{
                if(!p.isClasspath){
                    classLoader.addURL(p.file.toURI().toURL());
                }
                Class<?> cl = classLoader.loadClass(p.className);
                Plugin annotation = cl.getAnnotation(Plugin.class);
                Object instance = cl.newInstance();
                DefaultPluginContainer container = new DefaultPluginContainer(annotation, instance, !p.isClasspath ? p.file : null);
                boolean success = injectPluginIdentifiers(container) && injectConfiguration(container);
                if(success){
                    logger.info("Successfully loaded plugin " + p.name + " (id: " + p.id + " , version: " + p.version + ")");
                    plugins.add(container);
                }else{
                    logger.warn("Skipping plugin " + p.name);
                }
            }catch(InstantiationException e){
                logger.warn("Plugin " + p.id + " (" + p.name + ", version: " + p.version + ") could not be loaded");
                logger.warn(" Could not create an instance of the plugin. Is there a default constructor (no arguments)?");
                logger.trace(" Full exception:", e);
            }catch(IllegalAccessException e){
                logger.warn("Plugin ${p.id} (" + p.name + ", version: " + p.version + ") could not be loaded");
                logger.warn(" The default constructor was found, but it is not public");
                logger.trace(" Full exception:", e);
            }catch(ClassNotFoundException e){
                logger.warn("Plugin " + p.id + " (" + p.name + ", version: " + p.version + ") could not be loaded");
                logger.warn(" Plugin class was discovered, but does not exist. Should not be possible in a normal environment");
                logger.trace(" Full exception:", e);
            }catch(Exception e){
                logger.error("Plugin " + p.id + " (" + p.name + ", version: " + p.version + ") could not be loaded");
                logger.error(" An unknown error has occurred:", e);
            }
        }
        for (PluginContainer p : plugins) {
            DefaultPluginContainer pl = (DefaultPluginContainer) p;
            pl.fireEvent(new RegisterAdditionalEventHandlersEvent(pl.getEventBus(), NailedPlatform.instance().getEventBus()));
        }
    }

    private boolean injectPluginIdentifiers(PluginContainer container){
        Class<?> cl = container.getInstance().getClass();
        try{
            for(Field f : cl.getDeclaredFields()){
                for(Annotation a : f.getDeclaredAnnotations()){
                    if(a.annotationType() == PluginIdentifier.Instance.class){
                        f.setAccessible(true);
                        f.set(container.getInstance(), container);
                        logger.trace("Successfully injected PluginIdentifier into " + cl.getName() + "." + f.getName());
                    }
                }
            }
            return true;
        }catch(Exception e){
            logger.warn("Unknown exception while injecting plugin identifiers into " + container.getId(), e);
            return false;
        }
    }

    private boolean injectConfiguration(DefaultPluginContainer container){
        Class<?> cl = container.getInstance().getClass();
        try{
            for(Field f : cl.getDeclaredFields()){
                for(Annotation a : f.getDeclaredAnnotations()){
                    if(a.annotationType() == Configuration.class){
                        Configuration ann = (Configuration) a;
                        File pluginDir = new File(this.pluginDir, container.getId());
                        Config config;
                        try{
                            config = initPluginConfig(container, new File(pluginDir, ann.filename()), ann.defaults());
                        }catch(RuntimeException e){
                            logger.error("");
                            logger.error("Error while injecting plugin config for " + container.getName() + ":");
                            logger.error(e.getMessage());
                            logger.error("This plugin will not be loaded!");
                            logger.error("");
                            config = null;
                        }
                        if(config == null){
                            return false;
                        }
                        f.setAccessible(true);
                        f.set(container.getInstance(), config);
                        logger.trace("Successfully injected plugin config into " + cl.getName() + "." + f.getName());
                    }
                }
            }
            return true;
        }catch(Exception e){
            logger.warn("Unknown exception while injecting plugin configuration into " + container.getId(), e);
            return false;
        }
    }

    public void enablePlugins(){

    }

    private Config initPluginConfig(DefaultPluginContainer container, File configLocation, String defaultPath){
        InputStream defaultInput;
        if(container.hasLocation()){
            defaultInput = getConfigFromJar(container.getLocation(), defaultPath);
        }else{
            defaultInput = NailedPluginManager.class.getResourceAsStream("/" + defaultPath);
        }
        try{
            if(defaultInput == null){
                throw new RuntimeException("Default configuration path " + defaultPath + " could not be found");
            }
            if(!configLocation.exists() || configLocation.length() == 0){
                configLocation.getParentFile().mkdirs();
                ReadableByteChannel inChannel = Channels.newChannel(defaultInput);
                FileChannel out = new FileOutputStream(configLocation).getChannel();
                out.transferFrom(inChannel, 0, Long.MAX_VALUE);
                out.close();
            }
            if(container.hasLocation()){
                defaultInput = getConfigFromJar(container.getLocation(), defaultPath);
            }else{
                defaultInput = NailedPluginManager.class.getResourceAsStream("/" + defaultPath);
            }
            Config defaults = ConfigFactory.parseReader(new InputStreamReader(defaultInput));
            return ConfigFactory.parseFile(configLocation).withFallback(defaults);
        }catch(Exception e){
        }finally{
            IOUtils.closeQuietly(defaultInput);
        }
        return null;
    }

    private InputStream getConfigFromJar(File jar, String entry){
        try{
            JarFile jarFile = new JarFile(jar);
            return jarFile.getInputStream(jarFile.getEntry(entry));
        }catch(Exception e){
            return null;
        }
    }

    public static NailedPluginManager instance(){
        return INSTANCE;
    }
}
