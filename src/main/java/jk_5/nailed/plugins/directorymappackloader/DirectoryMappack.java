package jk_5.nailed.plugins.directorymappackloader;

import com.google.common.base.MoreObjects;
import io.netty.util.concurrent.Promise;
import jk_5.nailed.api.mappack.Mappack;
import jk_5.nailed.api.mappack.filesystem.DirectoryMount;
import jk_5.nailed.api.mappack.filesystem.IMount;
import jk_5.nailed.api.mappack.metadata.MappackMetadata;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

public class DirectoryMappack implements Mappack {

    private final File dir;
    private final MappackMetadata metadata;

    public DirectoryMappack(File dir, MappackMetadata metadata) {
        this.dir = dir;
        this.metadata = metadata;
    }

    @Nonnull
    @Override
    public String getId() {
        return dir.getName();
    }

    @Nonnull
    @Override
    public MappackMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void prepareWorld(@Nonnull File destinationDirectory, @Nonnull Promise<Void> promise) {
        File original = new File(dir, "worlds");
        destinationDirectory.mkdir();
        try{
            FileUtils.copyDirectory(original, destinationDirectory);
            promise.setSuccess(null);
        }catch(IOException e){
            promise.setFailure(e);
        }
    }

    @Nullable
    @Override
    public IMount getMappackMount() {
        return new DirectoryMount(new File(dir, "scripts"));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("dir", dir)
                .add("metadata", metadata)
                .toString();
    }
}
