package jk_5.nailed.server.map;

import io.netty.util.concurrent.Promise;
import jk_5.nailed.api.mappack.Mappack;
import jk_5.nailed.api.mappack.filesystem.IMount;
import jk_5.nailed.api.mappack.metadata.MappackMetadata;
import jk_5.nailed.server.mappack.metadata.xml.XmlMappackMetadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public class DummyLobbyMappack implements Mappack {

    private static final DummyLobbyMappack INSTANCE = new DummyLobbyMappack();
    private final MappackMetadata metadata;

    public DummyLobbyMappack() {
        try{
            this.metadata = XmlMappackMetadata.fromResource("dummy-mappack.xml");
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    @Override
    public String getId() {
        return "nailed/lobby";
    }

    @Nonnull
    @Override
    public MappackMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void prepareWorld(@Nonnull File destinationDirectory, @Nonnull Promise<Void> promise) {
        promise.setSuccess(null);
    }

    @Nullable
    @Override
    public IMount getMappackMount() {
        return null;
    }

    public static Mappack instance(){
        return INSTANCE;
    }
}
