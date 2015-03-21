package jk_5.nailed.plugins.directorymappackloader;

import jk_5.eventbus.EventHandler;
import jk_5.nailed.api.event.mappack.RegisterMappacksEvent;
import jk_5.nailed.api.mappack.MappackConfigurationException;
import jk_5.nailed.api.mappack.metadata.MappackMetadata;
import jk_5.nailed.api.mappack.metadata.json.JsonMappackMetadata;
import jk_5.nailed.api.plugin.Plugin;
import jk_5.nailed.server.mappack.metadata.xml.XmlMappackMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Plugin(id = "DirectoryMappackLoader", name = "Directory Mappack Loader")
public class DirectoryMappackLoaderPlugin {

    private static final Logger logger = LogManager.getLogger();

    @EventHandler
    public void registerMappacks(RegisterMappacksEvent event){
        File mappacksDir = new File(event.getPlatform().getRuntimeDirectory(), "mappacks");
        logger.info("Loading directory mappacks");
        if(!mappacksDir.exists()) mappacksDir.mkdir();
        int i = 0;
        for(File file : mappacksDir.listFiles()){
            if(file.isDirectory()){
                File jsonMappackMetadata = new File(file, "mappack.json");
                File xmlMappackMetadata = new File(file, "game.xml");

                try{
                    MappackMetadata metadata = null;
                    if(xmlMappackMetadata.exists() && xmlMappackMetadata.isFile()){
                        logger.trace("Attempting to load xml mappack " + file.getName());
                        metadata = XmlMappackMetadata.fromFile(xmlMappackMetadata);
                    }else if(jsonMappackMetadata.exists() && jsonMappackMetadata.isFile()){
                        logger.trace("Attempting to load json mappack " + file.getName());
                        metadata = JsonMappackMetadata.fromFile(jsonMappackMetadata);
                    }

                    if(metadata != null){
                        DirectoryMappack mappack = new DirectoryMappack(file, metadata);
                        if(event.registerMappack(mappack)){
                            i++;
                        }
                    }
                }catch(MappackConfigurationException e){
                    logger.warn("Configuration for mappack " + file.getName() + " is invalid: " + e.getMessage());
                }
            }
        }
        logger.info("Registered " + i + " DirectoryMappacks");
    }
}
