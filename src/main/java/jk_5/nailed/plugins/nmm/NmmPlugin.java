package jk_5.nailed.plugins.nmm;

import com.google.gson.Gson;
import jk_5.eventbus.EventHandler;
import jk_5.nailed.api.event.mappack.RegisterMappacksEvent;
import jk_5.nailed.api.plugin.Plugin;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Plugin(id = "Nailed|NMM", name = "Nailed Mappack Manager", version = "1.0.0")
public class NmmPlugin {

    private static final Logger logger = LogManager.getLogger();
    public static final Gson gson = new Gson();
    public static NmmPlugin instance;
    private final File cache = new File("nmm-cache");

    public NmmPlugin() {
        instance = this;
        if(!cache.exists()){
            cache.mkdir();
        }
    }

    @EventHandler
    public void registerMappacks(RegisterMappacksEvent event){
        HttpClient httpClient = HttpClientBuilder.create().build();
        try{
            HttpGet request = new HttpGet("http://nmm.jk-5.nl/mappacks.json");
            HttpResponse response = httpClient.execute(request);
            MappacksList list = gson.fromJson(EntityUtils.toString(response.getEntity(), "UTF-8"), MappacksList.class);
            for(String mappack : list.mappacks){
                event.registerMappack(new NmmMappack(mappack));
            }
            logger.info("Registered " + list.mappacks.length + " nmm mappacks");
        }catch(Exception e){
            logger.warn("Was not able to fetch mappacks from nmm.jk-5.nl", e);
        }
    }
}
