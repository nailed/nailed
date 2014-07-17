package jk_5.nailed.deploader;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jk_5.nailed.deploader.maven.MavenDependency;
import jk_5.nailed.deploader.maven.MavenResolver;

/**
 * No description given
 *
 * @author jk-5
 */
public class DependencyParser {

    private static final JsonParser parser = new JsonParser();
    private static final File libDir = new File("libraries");
    private static final MavenResolver resolver = new MavenResolver();

    public static void load(InputStream is) throws Exception {
        JsonObject json = parser.parse(new InputStreamReader(is)).getAsJsonObject();
        is.close();

        for(JsonElement e : json.getAsJsonArray("repositories")){
            resolver.addRepository(e.getAsString());
        }

        Set<MavenDependency> dependencies = new HashSet<MavenDependency>();

        for(JsonElement e : json.getAsJsonArray("dependencies")){
            dependencies.add(resolver.dependency(e.getAsString()));
        }

        dependencies.add(new MinecraftDependency(json.get("mcversion").getAsString()));

        final CountDownLatch finished = new CountDownLatch(dependencies.size());
        Executor executor = Executors.newCachedThreadPool();

        for(final MavenDependency dep : dependencies){
            executor.execute(new Runnable(){
                @Override
                public void run() {
                    dep.download(dep.getLibraryLocation(libDir));
                    finished.countDown();
                }
            });
        }

        finished.await();

        PrintWriter p = new PrintWriter(new File("start.sh"));
        p.println("#! /bin/sh");
        p.print("java -Xmx1g -Xms1g -XX:PermSize=128m -server -Djava.awt.headless=true -cp ");
        p.print(DependencyParser.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        for(MavenDependency dep : dependencies){
            p.print(':');
            p.print(dep.getLibraryLocation(libDir).getPath());
        }

        p.print(" net.minecraft.launchwrapper.Launch --tweakClass jk_5.nailed.server.tweaker.NailedTweaker");
        p.println();

        p.flush();
        p.close();
    }
}
