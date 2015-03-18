package jk_5.nailed.server.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.minecraft.server.dedicated.PropertyManager;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

//TODO: do some mixin magic in here
public class MinecraftConfig extends PropertyManager {

    private static final Logger logger = LogManager.getLogger();
    private final File file = new File("settings.conf");
    private Config config;

    public MinecraftConfig() {
        super(null);

        logger.info("Loading minecraft config");

        if(!file.exists() || file.length() == 0){
            ReadableByteChannel in = null;
            FileChannel out = null;
            try{
                in = Channels.newChannel(this.getClass().getResourceAsStream("/reference.conf"));
                out = new FileOutputStream(file).getChannel();
                out.transferFrom(in, 0, Long.MAX_VALUE);
            }catch(IOException e){
                logger.error("Error while creating default config file", e);
            }finally{
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
        Config defaults = ConfigFactory.defaultReference().withOnlyPath("minecraft");
        Config conf = ConfigFactory.parseFile(file).withFallback(defaults);
        try{
            config = conf.getConfig("minecraft");
        }catch(Throwable e){
            logger.warn("Failed to load minecraft config, using defaults.", e);
            config = defaults.getConfig("minecraft");
        }
    }

    private String remap(String key){
        switch(key){
            case "enable-query": return "query.enabled";
            case "enable-rcon": return "rcon.enabled";
            default: return key;
        }
    }

    @Override
    public void saveProperties(){
        logger.debug("Attempted to save minecraft config");
    }

    @Override
    public File getPropertiesFile() {
        return null;
    }

    @Override
    public String getStringProperty(String key, String defaultValue) {
        if(config.hasPath(remap(key))){
            return config.getString(remap(key));
        }else{
            logger.warn("Attempted to get minecraft config value \'{0}\', but it was not found in the configuration. Returning default value (\'{1}\')", remap(key), defaultValue);
            return defaultValue;
        }
    }

    @Override
    public int getIntProperty(String key, int defaultValue) {
        if(config.hasPath(remap(key))){
            return config.getInt(remap(key));
        }else{
            logger.warn("Attempted to get minecraft config value \'{0}\', but it was not found in the configuration. Returning default value (\'{1}\')", remap(key), defaultValue);
            return defaultValue;
        }
    }

    @Override
    public long getLongProperty(String key, long defaultValue) {
        if(config.hasPath(remap(key))){
            return config.getLong(remap(key));
        }else{
            logger.warn("Attempted to get minecraft config value \'{0}\', but it was not found in the configuration. Returning default value (\'{1}\')", remap(key), defaultValue);
            return defaultValue;
        }
    }

    @Override
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        if(config.hasPath(remap(key))){
            return config.getBoolean(remap(key));
        }else{
            logger.warn("Attempted to get minecraft config value \'{0}\', but it was not found in the configuration. Returning default value (\'{1}\')", remap(key), defaultValue);
            return defaultValue;
        }
    }

    @Override
    public void setProperty(String key, Object value) {

    }

    @Override
    public void generateNewProperties() {

    }
}
