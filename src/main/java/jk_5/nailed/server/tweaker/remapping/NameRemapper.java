package jk_5.nailed.server.tweaker.remapping;

import LZMA.LzmaInputStream;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import jk_5.nailed.server.tweaker.patcher.BinPatchManager;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class NameRemapper extends Remapper {

    private static final NameRemapper INSTANCE = new NameRemapper();
    private static final Logger logger = LogManager.getLogger();

    private LaunchClassLoader classLoader = Launch.classLoader;
    private BiMap<String, String> classNameMap = ImmutableBiMap.of();;

    private Map<String,Map<String,String>> rawFieldMaps;
    private Map<String,Map<String,String>> rawMethodMaps;
    private Map<String,Map<String,String>> fieldNameMaps;
    private Map<String,Map<String,String>> methodNameMaps;
    private Map<String,Map<String,String>> fieldDescriptors = new HashMap<String, Map<String, String>>();

    // Cache null values so we don't waste time trying to recompute classes with no field or method maps
    private Set<String> negativeCacheMethods = new HashSet<String>();
    private Set<String> negativeCacheFields = new HashSet<String>();

    public void init(){
        logger.info("Loading deobfuscation data...");
        InputStream data = this.getClass().getResourceAsStream("/deobfuscation_data.lzma");
        if(data == null){
            logger.warn("Was not able to find deobfuscation data. Assuming development environment");
            return;
        }
        try{
            BufferedReader stream = new BufferedReader(new InputStreamReader(new LzmaInputStream(data)));
            String line = stream.readLine();
            Splitter splitter = Splitter.on(CharMatcher.anyOf(": ")).omitEmptyStrings().trimResults();
            ImmutableBiMap.Builder<String, String> builder = ImmutableBiMap.builder();
            rawFieldMaps = new HashMap<String, Map<String, String>>();
            rawMethodMaps = new HashMap<String, Map<String, String>>();
            while(line != null){
                String[] parts = Iterables.toArray(splitter.split(line), String.class);
                String typ = parts[0];
                if(typ.equals("MD")){
                    parseMethod(parts);
                }else if(typ.equals("FD")){
                    parseField(parts);
                }else if(typ.equals("CL")){
                    parseClass(builder, parts);
                }
                line = stream.readLine();
            }
            classNameMap = builder.build();
        }catch(IOException e){
            logger.error("An error has occurred while loading the deobfuscation data", e);
        }finally{
            IOUtils.closeQuietly(data);
            methodNameMaps = new HashMap<String, Map<String, String>>();
            fieldNameMaps = new HashMap<String, Map<String, String>>();
        }
    }

    private void parseField(String[] parts){
        String oldSrg = parts[1];
        int lastOld = oldSrg.lastIndexOf('/');
        String cl = oldSrg.substring(0, lastOld);
        String oldName = oldSrg.substring(lastOld + 1);
        String newSrg = parts[2];
        int lastNew = newSrg.lastIndexOf('/');
        String newName = newSrg.substring(lastNew + 1);
        if(!rawFieldMaps.containsKey(cl)){
            rawFieldMaps.put(cl, new HashMap<String, String>());
        }
        rawFieldMaps.get(cl).put(oldName + ":" + getFieldType(cl, oldName), newName);
        rawFieldMaps.get(cl).put(oldName + ":null", newName);
    }

    private String getFieldType(String owner, String name){
        if(this.fieldDescriptors.containsKey(owner)){
            return fieldDescriptors.get(owner).get("name");
        }
        synchronized (this.fieldDescriptors){
            try{
                byte[] bytes = BinPatchManager.instance().getPatchedResource(owner, map(owner).replace('/', '.'), classLoader);
                if(bytes == null) return null;
                ClassReader cr = new ClassReader(bytes);
                ClassNode cnode = new ClassNode();
                cr.accept(cnode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                Map<String, String> resMap = new HashMap<String, String>();
                for (FieldNode field : cnode.fields) {
                    resMap.put(field.name, field.desc);
                }
                fieldDescriptors.put(owner, resMap);
                return resMap.get(name);
            }catch(IOException e){
                logger.error("An exception occured while reading class file " + owner, e);
            }
            return null;
        }
    }

    private void parseClass(ImmutableBiMap.Builder<String, String> builder, String[] parts){
        builder.put(parts[1], parts[2]);
    }

    private void parseMethod(String[] parts){
        String oldSrg = parts[1];
        int lastOld = oldSrg.lastIndexOf('/');
        String cl = oldSrg.substring(0, lastOld);
        String oldName = oldSrg.substring(lastOld + 1);
        String sig = parts[2];
        String newSrg = parts[3];
        int lastNew = newSrg.lastIndexOf('/');
        String newName = newSrg.substring(lastNew + 1);
        if(!rawMethodMaps.containsKey(cl)){
            rawMethodMaps.put(cl, new HashMap<String, String>());
        }
        rawMethodMaps.get(cl).put(oldName + sig, newName);
    }

    @Override
    public String mapFieldName(String owner, String name, String desc) {
        if(classNameMap == null || classNameMap.isEmpty()){
            return name;
        }
        Map<String, String> fieldMap = this.getFieldMap(owner);
        if(fieldMap != null && fieldMap.containsKey(name + ":" + desc)){
            return fieldMap.get(name + ":" + desc);
        }else{
            return name;
        }
    }

    @Override
    public String map(String typeName) {
        if(classNameMap == null || classNameMap.isEmpty()){
            return typeName;
        }
        if(classNameMap.containsKey(typeName)){
            return classNameMap.get(typeName);
        }
        int dollarIdx = typeName.lastIndexOf('$');
        if(dollarIdx > -1){
            return map(typeName.substring(0, dollarIdx)) + "$" + typeName.substring(dollarIdx + 1);
        }
        return typeName;
    }

    public String unmap(String typeName){
        if(classNameMap == null || classNameMap.isEmpty()){
            return typeName;
        }
        if(classNameMap.containsValue(typeName)){
            return classNameMap.inverse().get(typeName);
        }
        int dollarIdx = typeName.lastIndexOf('$');
        if(dollarIdx > -1){
            return unmap(typeName.substring(0, dollarIdx)) + "$" + typeName.substring(dollarIdx + 1);
        }
        return typeName;
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        if(classNameMap == null || classNameMap.isEmpty()){
            return name;
        }
        Map<String, String> methodMap = this.getMethodMap(owner);
        String methodDesc = name + desc;
        if(methodMap != null && methodMap.containsKey(methodDesc)){
            return methodMap.get(methodDesc);
        }else{
            return name;
        }
    }

    private Map<String, String> getFieldMap(String className){
        if(!this.fieldNameMaps.containsKey(className) && !negativeCacheFields.contains(className)){
            findAndMergeSuperMaps(className);
            if(!fieldNameMaps.containsKey(className)){
                negativeCacheFields.add(className);
                return null;
            }
        }
        return fieldNameMaps.get(className);
    }

    private Map<String, String> getMethodMap(String className){
        if(!this.methodNameMaps.containsKey(className) && !negativeCacheMethods.contains(className)){
            findAndMergeSuperMaps(className);
            if(!methodNameMaps.containsKey(className)){
                negativeCacheMethods.add(className);
                return null;
            }
        }
        return methodNameMaps.get(className);
    }

    private void findAndMergeSuperMaps(String name){
        try{
            String superName = null;
            String[] interfaces = new String[0];
            byte[] bytes = BinPatchManager.instance().getPatchedResource(name, map(name), classLoader);
            if(bytes != null){
                ClassReader cr = new ClassReader(bytes);
                superName = cr.getSuperName();
                interfaces = cr.getInterfaces();
            }
            mergeSuperMaps(name, superName, interfaces);
        }catch(IOException e){
            logger.error("An exception has occurred while finding super maps", e);
        }
    }

    public void mergeSuperMaps(String name, String superName, String[] interfaces){
        if(classNameMap == null || classNameMap.isEmpty()){
            return;
        }
        if(Strings.isNullOrEmpty(superName)){
            return;
        }
        List<String> allParents = ImmutableList.<String>builder().add(superName).addAll(Arrays.asList(interfaces)).build();

        for(String parent : allParents){
            if(!methodNameMaps.containsKey(parent)){
                findAndMergeSuperMaps(parent);
            }
        }

        Map<String, String> methodMap = new HashMap<String, String>();
        Map<String, String> fieldMap = new HashMap<String, String>();

        for(String parent : allParents){
            if(methodNameMaps.containsKey(parent)){
                methodMap.putAll(methodNameMaps.get(parent));
            }
            if(fieldNameMaps.containsKey(parent)){
                fieldMap.putAll(fieldNameMaps.get(parent));
            }
        }

        if(rawMethodMaps.containsKey(name)){
            methodMap.putAll(rawMethodMaps.get(name));
        }
        if(rawFieldMaps.containsKey(name)){
            fieldMap.putAll(rawFieldMaps.get(name));
        }

        methodNameMaps.put(name, ImmutableMap.copyOf(methodMap));
        fieldNameMaps.put(name, ImmutableMap.copyOf(fieldMap));
    }

    public Set<String> getObfuscatedClasses(){
        return ImmutableSet.copyOf(classNameMap.keySet());
    }

    public String getStaticFieldType(String oldType, String oldName, String newType, String newName){
        String fType = getFieldType(oldType, oldName);
        if(oldType.equals(newType)){
            return fType;
        }
        Map<String,String> newClassMap = fieldDescriptors.get(newType);
        if(newClassMap == null){
            newClassMap = new HashMap<String, String>();
            fieldDescriptors.put(newType, newClassMap);
        }
        newClassMap.put(newName, fType);
        return fType;
    }

    public boolean isRemappedClass(String name){
        return !map(name).equals(name);
    }

    public static NameRemapper instance(){
        return INSTANCE;
    }
}
