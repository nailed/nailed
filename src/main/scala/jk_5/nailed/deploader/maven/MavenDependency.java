package jk_5.nailed.deploader.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import jk_5.nailed.deploader.DepLoader;

/**
 * No description given
 *
 * @author jk-5
 */
public class MavenDependency {

    protected final MavenResolver resolver;
    protected final String group;
    protected final String artifact;
    protected final String version;
    protected final String classifier;

    public MavenDependency(MavenResolver resolver, String group, String artifact, String version, String classifier){
        this.resolver = resolver;
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.classifier = classifier;
    }

    public CharSequence getFolderPath(){
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(group.replace('.', '/'));
        pathBuilder.append('/');
        pathBuilder.append(artifact);
        pathBuilder.append('/');
        pathBuilder.append(version);
        pathBuilder.append('/');
        pathBuilder.append(artifact);
        pathBuilder.append('-');
        pathBuilder.append(version);
        if(classifier != null){
            pathBuilder.append('-').append(classifier);
        }
        pathBuilder.append(".jar");
        return pathBuilder;
    }

    public CharSequence getMavenPath(){
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(group);
        pathBuilder.append(':');
        pathBuilder.append(artifact);
        pathBuilder.append(':');
        pathBuilder.append(version);
        if(classifier != null){
            pathBuilder.append(':').append(classifier);
        }
        return pathBuilder;
    }

    public File getLibraryLocation(File baseDir){
        return new File(baseDir, this.getFolderPath().toString());
    }

    public void download(File dest){
        dest.getParentFile().mkdirs();

        if(dest.isFile()) return;

        String p = this.getFolderPath().toString();
        URL dep = null;
        for(String r : this.resolver.repositories){
            try{
                dep = new URL(r + p);
                HttpURLConnection connection = (HttpURLConnection) dep.openConnection();
                connection.setRequestMethod("HEAD");
                int responseCode = connection.getResponseCode();
                if(responseCode == 200) {
                    break;
                }
            }catch(Exception ignored){

            }
        }

        if(dep == null){
            throw new IllegalStateException("Was not able to resolve dependency " + this.toString());
        }

        DepLoader.log("INFO", "Downloading: " + dep.toString());
        try{
            ReadableByteChannel rbc = Channels.newChannel(dep.openStream());
            FileOutputStream fos = new FileOutputStream(dest);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            DepLoader.log("INFO", "Downloaded: " + dep.toString());
        }catch(Exception ignored){

        }
    }

    @Override
    public String toString() {
        return "MavenDependency{" +
                "group='" + group + '\'' +
                ", artifact='" + artifact + '\'' +
                ", version='" + version + '\'' +
                ", classifier='" + classifier + '\'' +
                '}';
    }
}
