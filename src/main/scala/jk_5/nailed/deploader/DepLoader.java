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

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import jk_5.nailed.deploader.maven.MavenDependency;
import jk_5.nailed.deploader.maven.MavenResolver;

/**
 * No description given
 *
 * @author jk-5
 */
public class DepLoader {

    private static final DateFormat logTimeFormat = new SimpleDateFormat("HH:mm:ss");
    private static final File libDir = new File("libraries");
    private static final MavenDependency gsonDep = new MavenResolver().addMavenCentral().dependency("com.google.code.gson:gson:2.2.4");
    private static final URLClassLoader classLoader = (URLClassLoader) DepLoader.class.getClassLoader();
    private static final Method addUrlMethod;

    static {
        try{
            addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addUrlMethod.setAccessible(true);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws MalformedURLException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException {
        log("INFO", "Starting Nailed DepLoader...");
        libDir.mkdir();
        File location = gsonDep.getLibraryLocation(libDir);
        gsonDep.download(location);

        addUrlMethod.invoke(classLoader, location.toURI().toURL());

        Class<?> cl = classLoader.loadClass("jk_5.nailed.deploader.DependencyParser");
        cl.getDeclaredMethod("load", InputStream.class).invoke(null, cl.getResourceAsStream("/nailedversion.json"));
    }

    public static void log(String level, String message, Object... formatting){
        System.out.println("[" + logTimeFormat.format(new Date()) + "] [" + level + "]: " + String.format(message, formatting));
    }
}
