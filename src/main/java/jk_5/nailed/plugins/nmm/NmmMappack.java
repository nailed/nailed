package jk_5.nailed.plugins.nmm;

import io.netty.util.concurrent.Promise;
import jk_5.nailed.api.mappack.Mappack;
import jk_5.nailed.api.mappack.filesystem.DirectoryMount;
import jk_5.nailed.api.mappack.filesystem.IMount;
import jk_5.nailed.api.mappack.metadata.MappackMetadata;
import jk_5.nailed.server.mappack.metadata.xml.XmlMappackMetadata;
import jk_5.nailed.server.utils.ZipUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class NmmMappack implements Mappack {

    private final String path;
    private File dir;
    private MappackMetadata metadata;

    public NmmMappack(String path) {
        this.path = path;
    }

    @Override
    public String getId() {
        return path;
    }

    @Nonnull
    @Override
    public MappackMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void prepareWorld(@Nonnull File destinationDirectory, @Nonnull Promise<Void> promise) {
        destinationDirectory.mkdir();
        HttpClient httpClient = HttpClientBuilder.create().build();
        try{
            String mappack = path.split("/", 2)[1];
            HttpGet request = new HttpGet("http://nmm.jk-5.nl/" + path + "/versions.json");
            HttpResponse response = httpClient.execute(request);
            MappackInfo list = NmmPlugin.gson.fromJson(EntityUtils.toString(response.getEntity(), "UTF-8"), MappackInfo.class);

            HttpGet request2 = new HttpGet("http://nmm.jk-5.nl/" + path + "/" + mappack + "-" + list.latest + ".zip");
            HttpEntity response2 = httpClient.execute(request2).getEntity();
            if(response2 != null){
                File mappackZip = new File(destinationDirectory, "mappack.zip");
                InputStream is = response2.getContent();
                OutputStream os = new FileOutputStream(mappackZip);
                IOUtils.copy(is, os);
                os.close();
                ZipUtils.extract(mappackZip, destinationDirectory);
                mappackZip.delete();
                dir = destinationDirectory;
                File dataDir = new File(destinationDirectory, ".data");
                dataDir.mkdir();
                File metadataLocation = new File(dataDir, "game.xml");
                new File(destinationDirectory, "game.xml").renameTo(metadataLocation);
                new File(destinationDirectory, "scripts").renameTo(new File(dataDir, "scripts"));
                File worldsDir = new File(destinationDirectory, "worlds");
                for(File f : worldsDir.listFiles()){
                    f.renameTo(new File(destinationDirectory, f.getName()));
                }
                worldsDir.delete();
                metadata = XmlMappackMetadata.fromFile(metadataLocation);
                promise.setSuccess(null);
            }else{
                promise.setFailure(new RuntimeException("Got an empty response while downloading mappack " + path + " from nmm.jk-5.nl"));
            }
        }catch(Exception e){
            promise.setFailure(new RuntimeException("Was not able to download mappack " + path + " from nmm.jk-5.nl", e));
        }
    }

    @Nullable
    @Override
    public IMount getMappackMount() {
        return new DirectoryMount(new File(new File(dir, ".data"), "scripts"));
    }
}
