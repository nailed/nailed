package jk_5.nailed.deploader;

import java.io.File;

import jk_5.nailed.deploader.maven.MavenDependency;
import jk_5.nailed.deploader.maven.MavenResolver;

/**
 * No description given
 *
 * @author jk-5
 */
public class MinecraftDependency extends MavenDependency {

    public MinecraftDependency(String version) {
        super(new MavenResolver().addRepository("http://s3.amazonaws.com/Minecraft.Download/versions/"), null, null, version, null);
    }

    @Override
    public CharSequence getFolderPath() {
        return new StringBuilder().append(this.version).append('/').append("minecraft_server.").append(this.version).append(".jar");
    }

    @Override
    public File getLibraryLocation(File baseDir) {
        return new File(baseDir, "net/minecraft/minecraft_server/" + this.version + "/minecraft_server-" + this.version + ".jar");
    }
}
