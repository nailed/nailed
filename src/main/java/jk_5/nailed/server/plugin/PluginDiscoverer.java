package jk_5.nailed.server.plugin;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginDiscoverer {

    private static final Logger logger = LogManager.getLogger();

    private static Set<DiscoveredPlugin> discovered = new HashSet<DiscoveredPlugin>();

    public static void clearDiscovered(){
        discovered = new HashSet<DiscoveredPlugin>();
    }

    public static Set<DiscoveredPlugin> getDiscovered(){
        return discovered;
    }

    //TODO: Generalize the file searching thing from below and use it in the jar loader

    public static void discoverJarPlugins(File pluginsDir){
        logger.info("Discovering plugins from the plugins folder...");
        for(File file : pluginsDir.listFiles(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        })){
            try{
                readJarFile(file, discovered);
            }catch(IOException e){
            }
        }
    }

    public static void discoverClasspathPlugins(){
        logger.info("Discovering plugins from the classpath...");

        URL[] jars = ((URLClassLoader) PluginDiscoverer.class.getClassLoader()).getURLs();
        for(URL jar : jars){
            try{
                File file = new File(jar.toURI());
                if(file.isDirectory()){
                    logger.debug("Examining classPath directory " + file + " for potential plugins");
                    recurseChildren(file);
                }else if(file.isFile()){
                    readJarFile(file, discovered);
                }
            }catch(Exception e){
            }
        }

        logger.info("Discovered " + discovered.size() + " plugins");
        for(DiscoveredPlugin d : discovered){
            logger.trace("  - {} ({} version: {}) -> {}", d.id, d.name, d.version, d.className);
        }
    }

    private static void recurseChildren(File fi) throws IOException {
        for(File f : fi.listFiles()){
            if(f.isFile() && f.getName().endsWith(".class")){
                FileInputStream fis = null;
                try{
                    fis = new FileInputStream(f);
                    analizePotentialPlugins(fis, discovered, true, null);
                }finally{
                    IOUtils.closeQuietly(fis);
                }
            }else if(f.isDirectory()){
                recurseChildren(f);
            }
        }
    }

    private static void readJarFile(File file, Set<DiscoveredPlugin> discovered) throws IOException {
        JarFile jar = new JarFile(file);
        String jarName = jar.getName().toLowerCase();
        if(!jarName.contains("jre") && !jarName.contains("jdk") && !jarName.contains("/rt.jar")){
            logger.debug("Examining jar file {} for potential plugins", jar.getName());
            Enumeration<JarEntry> entries = jar.entries();
            while(entries.hasMoreElements()){
                JarEntry entry = entries.nextElement();
                if(entry != null && entry.getName().endsWith(".class")) { //Filter out junk we don't need. We only need class files
                    analizePotentialPlugins(jar.getInputStream(entry), discovered, false, file);
                }
            }
        }else{
            logger.debug("Ignoring JRE jar " + jar.getName());
        }
    }

    private static void analizePotentialPlugins(InputStream input, Set<DiscoveredPlugin> discovered, boolean isClasspath, File file) throws IOException {
        DataInputStream in;
        if(input instanceof DataInputStream){
            in = ((DataInputStream) input);
        }else{
            in = new DataInputStream(input);
        }

        ClassFile classFile = new ClassFile(in);

        AnnotationsAttribute annotations = ((AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag));
        if(annotations == null){
            return;
        }
        for(Annotation annotation : annotations.getAnnotations()){
            String annName = annotation.getTypeName();
            if(annName.equals("jk_5.nailed.api.plugin.Plugin")){
                StringMemberValue idValue = ((StringMemberValue) annotation.getMemberValue("id"));
                String id = idValue == null ? null : idValue.getValue();
                StringMemberValue nameValue = ((StringMemberValue) annotation.getMemberValue("name"));
                String name = nameValue == null ? null : nameValue.getValue();
                StringMemberValue versionValue = ((StringMemberValue) annotation.getMemberValue("version"));
                String version = versionValue == null ? "unknown" : versionValue.getValue();
                discovered.add(new DiscoveredPlugin(classFile.getName(), id, name, version, isClasspath, file));
            }
        }
    }

    static class DiscoveredPlugin {

        public String className;
        public String id;
        public String name;
        public String version;
        public boolean isClasspath;
        public File file;

        public DiscoveredPlugin(String className, String id, String name, String version, boolean isClasspath, File file) {
            this.className = className;
            this.id = id;
            this.name = name;
            this.version = version;
            this.isClasspath = isClasspath;
            this.file = file;
        }
    }
}
