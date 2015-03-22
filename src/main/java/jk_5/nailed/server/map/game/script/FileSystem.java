package jk_5.nailed.server.map.game.script;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jk_5.nailed.api.mappack.filesystem.IMount;
import jk_5.nailed.api.util.Checks;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * No description given
 *
 * @author jk-5
 */
public class FileSystem {

    private Map<String, MountWrapper> mounts = Maps.newHashMap();
    private Set<IMountedFile> openFiles = Sets.newHashSet();

    public void unload() {
        //noinspection SynchronizeOnNonFinalField
        synchronized(this.openFiles){
            while(this.openFiles.size() > 0){
                IMountedFile file = this.openFiles.iterator().next();
                try{
                    file.close();
                }catch(IOException e){
                    this.openFiles.remove(file);
                }
            }
        }
    }

    public synchronized void mount(String location, IMount mount) throws FileSystemException {
        if(mount == null){
            throw new NullPointerException();
        }
        location = sanitizePath(location);
        if(location.contains("..")){
            throw new FileSystemException("Cannot mount below the root");
        }
        mount(new MountWrapper(location, mount));
    }

    private synchronized void mount(MountWrapper wrapper) throws FileSystemException {
        String location = wrapper.getLocation();
        if(this.mounts.containsKey(location)){
            this.mounts.remove(location);
        }
        this.mounts.put(location, wrapper);
    }

    @SuppressWarnings("unused")
    public synchronized void unmount(String path) {
        path = sanitizePath(path);
        if(this.mounts.containsKey(path)){
            this.mounts.remove(path);
        }
    }

    public synchronized String combine(String path, String childPath) {
        path = sanitizePath(path);
        childPath = sanitizePath(childPath);

        if(path.length() == 0){
            return childPath;
        }
        if(childPath.length() == 0){
            return path;
        }
        return sanitizePath(path + '/' + childPath);
    }

    public static String getDirectory(String path) {
        path = sanitizePath(path);
        if(path.length() == 0){
            return "..";
        }

        int lastSlash = path.lastIndexOf('/');
        if(lastSlash >= 0){
            return path.substring(0, lastSlash);
        }
        return "";
    }

    public static String getName(String path) {
        path = sanitizePath(path);
        if(path.length() == 0){
            return "root";
        }

        int lastSlash = path.lastIndexOf('/');
        if(lastSlash >= 0){
            return FilenameUtils.removeExtension(path.substring(lastSlash + 1));
        }
        return FilenameUtils.removeExtension(path);
    }

    public synchronized long getSize(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        return mount.getSize(path);
    }

    public synchronized String[] list(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);

        List<String> list = Lists.newArrayList();
        mount.list(path, list);

        for(MountWrapper otherMount : this.mounts.values()){
            if(getDirectory(otherMount.getLocation()).equals(path)){
                list.add(getName(otherMount.getLocation()));
            }
        }

        String[] array = new String[list.size()];
        list.toArray(array);
        return array;
    }

    private void findIn(String dir, List<String> matches, Pattern wildPattern) throws FileSystemException {
        String[] list = list(dir);
        //noinspection ForLoopReplaceableByForEach
        for(int i = 0; i < list.length; i++){
            String entry = list[i];
            String entryPath = dir + "/" + entry;
            if(wildPattern.matcher(entryPath).matches()){
                matches.add(entryPath);
            }
            if(isDir(entryPath)){
                findIn(entryPath, matches, wildPattern);
            }
        }
    }

    public synchronized String[] find(String wildPath) throws FileSystemException {
        wildPath = sanitizePath(wildPath, true);
        Pattern wildPattern = Pattern.compile("^\\Q" + wildPath.replaceAll("\\*", "\\\\E[^\\\\/]*\\\\Q") + "\\E$");
        List<String> matches = Lists.newArrayList();
        findIn("", matches, wildPattern);
        return matches.toArray(new String[matches.size()]);
    }

    public synchronized boolean exists(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        return mount.exists(path);
    }

    public synchronized boolean isDir(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        return mount.isDirectory(path);
    }

    public synchronized IMountedFileText openForRead(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        InputStream stream = mount.openForRead(path);
        if(stream != null){
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            IMountedFileText file = new IMountedFileText() {
                public String readLine() throws IOException {
                    return reader.readLine();
                }

                public void write(String s, int off, int len, boolean newLine) throws IOException {
                    throw new UnsupportedOperationException();
                }

                public void close() throws IOException {
                    //noinspection SynchronizeOnNonFinalField
                    synchronized(FileSystem.this.openFiles){
                        FileSystem.this.openFiles.remove(this);
                        reader.close();
                    }
                }

                public void flush() throws IOException {
                    throw new UnsupportedOperationException();
                }
            };
            //noinspection SynchronizeOnNonFinalField
            synchronized(this.openFiles){
                this.openFiles.add(file);
            }
            return file;
        }
        return null;
    }

    public synchronized IMountedFileBinary openForBinaryRead(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        final InputStream stream = mount.openForRead(path);
        if(stream != null){
            IMountedFileBinary file = new IMountedFileBinary() {
                public int read() throws IOException {
                    return stream.read();
                }

                public void write(int i) throws IOException {
                    throw new UnsupportedOperationException();
                }

                public void close() throws IOException {
                    //noinspection SynchronizeOnNonFinalField
                    synchronized(FileSystem.this.openFiles){
                        FileSystem.this.openFiles.remove(this);
                        stream.close();
                    }
                }

                public void flush()
                        throws IOException {
                    throw new UnsupportedOperationException();
                }
            };
            //noinspection SynchronizeOnNonFinalField
            synchronized(this.openFiles){
                this.openFiles.add(file);
            }
            return file;
        }
        return null;
    }

    private MountWrapper getMount(String path)
            throws FileSystemException {
        Iterator it = this.mounts.values().iterator();
        MountWrapper match = null;
        int matchLength = 999;
        while(it.hasNext()){
            MountWrapper mount = (MountWrapper) it.next();
            if(contains(mount.getLocation(), path)){
                int len = toLocal(path, mount.getLocation()).length();
                if((match == null) || (len < matchLength)){
                    match = mount;
                    matchLength = len;
                }
            }
        }
        if(match == null){
            throw new FileSystemException("Invalid Path");
        }
        return match;
    }

    private static String sanitizePath(String path) {
        return sanitizePath(path, false);
    }

    private static String sanitizePath(String path, boolean allowWildcards) {
        path = path.replace('\\', '/');

        char[] specialChars = {'"', ':', '<', '>', '?', '|'};

        StringBuilder cleanName = new StringBuilder();
        for(int i = 0; i < path.length(); i++){
            char c = path.charAt(i);
            if((c >= ' ') && (Arrays.binarySearch(specialChars, c) < 0) && (allowWildcards || c != '*')){
                cleanName.append(c);
            }
        }
        path = cleanName.toString();

        String[] parts = path.split("/");
        Stack<String> outputParts = new Stack<String>();
        //noinspection ForLoopReplaceableByForEach
        for(int n = 0; n < parts.length; n++){
            String part = parts[n];
            if((part.length() != 0) && (!".".equals(part))){
                if("..".equals(part)){
                    if(!outputParts.empty()){
                        String top = outputParts.peek();
                        if(!"..".equals(top)){
                            outputParts.pop();
                        }else{
                            outputParts.push("..");
                        }
                    }else{
                        outputParts.push("..");
                    }
                }else if(part.length() >= 255){
                    outputParts.push(part.substring(0, 255));
                }else{
                    outputParts.push(part);
                }
            }
        }

        StringBuilder result = new StringBuilder("");
        Iterator it = outputParts.iterator();
        while(it.hasNext()){
            String part = (String) it.next();
            result.append(part);
            if(it.hasNext()){
                result.append('/');
            }
        }

        return result.toString();
    }

    public static boolean contains(String pathA, String pathB) {
        pathA = sanitizePath(pathA);
        pathB = sanitizePath(pathB);

        return !"..".equals(pathB) && !pathB.startsWith("../") && (pathB.equals(pathA) || pathA.length() == 0 || pathB.startsWith(pathA + "/"));
    }

    public static String toLocal(String path, String location) {
        path = sanitizePath(path);
        location = sanitizePath(location);

        assert contains(location, path);
        String local = path.substring(location.length());
        if(local.startsWith("/")){
            return local.substring(1);
        }
        return local;
    }

    private class MountWrapper {

        private String location;
        private IMount mount;

        public MountWrapper(String location, IMount mount) {
            Checks.notNull(location, "Location is null");
            Checks.notNull(mount, "Mount is null");

            this.location = location;
            this.mount = mount;
        }

        public boolean exists(String path) throws FileSystemException {
            path = toLocal(path);
            try{
                return this.mount.exists(path);
            }catch(IOException e){
                throw new FileSystemException(e.getMessage());
            }
        }

        public boolean isDirectory(String path) throws FileSystemException {
            path = toLocal(path);
            try{
                return (this.mount.exists(path)) && (this.mount.isDirectory(path));
            }catch(IOException e){
                throw new FileSystemException(e.getMessage());
            }
        }

        public void list(String path, List<String> contents) throws FileSystemException {
            path = toLocal(path);
            try{
                if(this.mount.exists(path) && this.mount.isDirectory(path)){
                    this.mount.list(path, contents);
                }else{
                    throw new FileSystemException("Not a directory");
                }
            }catch(IOException e){
                throw new FileSystemException(e.getMessage());
            }
        }

        public long getSize(String path) throws FileSystemException {
            path = toLocal(path);
            try{
                if(this.mount.exists(path)){
                    if(this.mount.isDirectory(path)){
                        return 0L;
                    }

                    return this.mount.getSize(path);
                }

                throw new FileSystemException("No such file");
            }catch(IOException e){
                throw new FileSystemException(e.getMessage());
            }
        }

        public InputStream openForRead(String path) throws FileSystemException {
            path = toLocal(path);
            try{
                if((this.mount.exists(path)) && (!this.mount.isDirectory(path))){
                    return this.mount.openForRead(path);
                }

                throw new FileSystemException("No such file");
            }catch(IOException e){
                throw new FileSystemException(e.getMessage());
            }
        }

        private String toLocal(String path) {
            return FileSystem.toLocal(path, this.location);
        }

        public String getLocation() {
            return this.location;
        }
    }
}
