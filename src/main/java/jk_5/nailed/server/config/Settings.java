package jk_5.nailed.server.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class Settings {

    private static Config config;
    private static final Logger logger = LogManager.getLogger();

    public static Config load(){
        File file = new File("settings.conf");
        logger.info("Loading config");
        if(!file.exists() || file.length() == 0){
            ReadableByteChannel in = null;
            FileChannel out = null;
            try{
                in = Channels.newChannel(Settings.class.getResourceAsStream("/reference.conf"));
                out = new FileOutputStream(file).getChannel();
                out.transferFrom(in, 0, Long.MAX_VALUE);
            }catch(IOException e){
                logger.error("Error while creating default config file", e);
            }finally{
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
        Config defaults = ConfigFactory.defaultReference().withOnlyPath("nailed");
        Config conf = ConfigFactory.parseFile(file).withFallback(defaults);
        try{
            config = conf.getConfig("nailed");
        }catch(Throwable e){
            logger.warn("Failed to load nailed config, using defaults.", e);
            config = defaults.getConfig("nailed");
        }
        return config;
    }
}
