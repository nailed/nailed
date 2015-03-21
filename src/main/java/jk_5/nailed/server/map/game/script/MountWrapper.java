package jk_5.nailed.server.map.game.script;

import jk_5.nailed.api.mappack.filesystem.IMount;
import jk_5.nailed.api.util.Checks;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

final class MountWrapper {

    private final String location;
    private final IMount mount;

    public MountWrapper(@Nonnull String location, @Nonnull IMount mount) {
        Checks.notNull(location, "Location is null");
        Checks.notNull(mount, "Mount is null");

        this.location = location;
        this.mount = mount;
    }

    public boolean exists(String path) throws FileSystemException {
        String p = toLocal(path);
        try{
            return this.mount.exists(p);
        }catch(IOException e){
            throw new FileSystemException(e.getMessage());
        }
    }

    public boolean isDirectory(String path) throws FileSystemException {
        String p = toLocal(path);
        try{
            return this.mount.exists(p) && this.mount.isDirectory(p);
        }catch(IOException e) {
            throw new FileSystemException(e.getMessage());
        }
    }

    public void list(String path, List<String> contents) throws FileSystemException {
        String p = toLocal(path);
        try{
            if(this.mount.exists(p) && this.mount.isDirectory(p)){
                this.mount.list(p, contents);
            }else{
                throw new FileSystemException("Not a directory");
            }
        }catch(IOException e){
            throw new FileSystemException(e.getMessage());
        }
    }

    public long getSize(String path) throws FileSystemException {
        String p = toLocal(path);
        try{
            if(this.mount.exists(p)){
                if(this.mount.isDirectory(p)){
                    return 0L;
                }
                return this.mount.getSize(p);
            }
            throw new FileSystemException("No such file");
        }catch(IOException e){
            throw new FileSystemException(e.getMessage());
        }
    }

    public InputStream openForRead(String path) throws FileSystemException {
        String p = toLocal(path);
        try{
            if(this.mount.exists(p) && !this.mount.isDirectory(p)){
                return this.mount.openForRead(p);
            }
            throw new FileSystemException("No such file");
        }catch(IOException e){
            throw new FileSystemException(e.getMessage());
        }
    }

    private String toLocal(String path){
        return FileSystem.toLocal(path, this.location);
    }
}
