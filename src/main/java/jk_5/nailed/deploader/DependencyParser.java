/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package jk_5.nailed.deploader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jk_5.nailed.deploader.maven.MavenDependency;
import jk_5.nailed.deploader.maven.MavenResolver;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

        DepLoader.log("INFO", "Done downloading libraries");
        DepLoader.log("INFO", "Generating start.sh script");

        StringBuilder builder = new StringBuilder();
        builder.append("#! /bin/sh\n");
        builder.append("java -Xmx1g -Xms1g -XX:PermSize=128m -server -Djava.awt.headless=true -cp ");
        builder.append(DependencyParser.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        for(MavenDependency dep : dependencies){
            builder.append(':');
            builder.append(dep.getLibraryLocation(libDir).getPath());
        }

        builder.append(" net.minecraft.launchwrapper.Launch --tweakClass jk_5.nailed.server.tweaker.NailedTweaker");
        builder.append("\n");

        DepLoader.log("INFO", "Generated start.sh script");
        File startFile = new File("start.sh");
        PrintWriter p = new PrintWriter(startFile);
        p.append(builder.toString());
        p.flush();
        p.close();

        startFile.setExecutable(true);

        DepLoader.log("INFO", "Wrote start.sh script");
        DepLoader.log("INFO", "Done!");
        System.exit(0);
    }
}
