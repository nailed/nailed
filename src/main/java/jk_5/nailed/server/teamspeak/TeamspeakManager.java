package jk_5.nailed.server.teamspeak;

import com.typesafe.config.Config;
import jk_5.nailed.server.NailedPlatform;
import jk_5.nailed.server.teamspeak.api.JTS3ServerQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class TeamspeakManager {

    private static final Config config = NailedPlatform.config().getConfig("teamspeak");
    private static final boolean enabled = config.getBoolean("enabled");
    private static final String host = config.getString("host");
    private static final int port = config.getInt("port");
    private static final String username = config.getString("username");
    private static final String password = config.getString("password");
    private static boolean connected = false;
    private static final Logger logger = LogManager.getLogger();
    private static final JTS3ServerQuery server = new JTS3ServerQuery();

    public static void start(){
        if(!enabled){
            logger.info("Teamspeak integration is disabled in the config");
            return;
        }
        logger.info("Starting teamspeak integration");
        if(!server.connectTS3Query(host, port)){
            displayError();
        }
    }

    private static void displayError(){
        String error = server.getLastError();
        if(error != null){
            //TODO: proper logging
            System.out.println("Teamspeak error:");
            System.out.println(error);
            if(server.getLastErrorPermissionID() != -1) {
                HashMap<String, String> permInfo = server.getPermissionInfo(server.getLastErrorPermissionID());
                if(permInfo != null){
                    System.out.println("Missing Permission: " + permInfo.get("permname"));
                }
            }
        }
    }
}
