package jk_5.nailed.server.tweaker;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.compress.utils.IOUtils;

import java.io.InputStream;
import java.io.InputStreamReader;

public class NailedVersion {

    private static final JsonParser jsonParser = new JsonParser();

    public static int major;
    public static int minor;
    public static int revision;
    public static boolean isSnapshot;
    public static String full;
    public static String mcversion;

    public static void readConfig(){
        InputStream is = null;
        JsonObject data = null;
        try{
            is = NailedVersion.class.getClassLoader().getResourceAsStream("nailedversion.json");
            data = jsonParser.parse(new InputStreamReader(is)).getAsJsonObject();
        }finally{
            IOUtils.closeQuietly(is);
        }
        if(data == null){
            throw new RuntimeException("Could not read nailedversion.json");
        }

        JsonObject version = data.getAsJsonObject("version");
        major = version.get("major").getAsInt();
        minor = version.get("minor").getAsInt();
        revision = version.get("revision").getAsInt();
        isSnapshot = version.get("isSnapshot").getAsBoolean();
        full = version.get("full").getAsString();
        mcversion = data.get("mcversion").getAsString();
    }
}
