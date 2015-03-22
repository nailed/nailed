package jk_5.nailed.server.tweaker.patcher;

import LZMA.LzmaInputStream;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.nothome.delta.GDiffPatcher;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.regex.Pattern;

public class BinPatchManager {

    private static final boolean dumpPatched = System.getProperty("nailed.dumpPatchedClasses", "false").equalsIgnoreCase("true");
    private static final boolean ignorePatchDiscrepancies = System.getProperty("nailed.ignorePatchDiscrepancies", "false").equalsIgnoreCase("true");
    private static final Logger logger = LogManager.getLogger();
    private static final BinPatchManager INSTANCE = new BinPatchManager();

    private final GDiffPatcher patcher = new GDiffPatcher();

    private Multimap<String, ClassPatch> patches;
    private Map<String, byte[]> cache;
    private File tempDir;

    public BinPatchManager() {
        if(dumpPatched){
            //try{
                tempDir = Files.createTempDir();
                logger.info("Dumping patched classes to {}", tempDir.getAbsolutePath());
            //}catch(IOException e){
            //}
        }
    }

    public byte[] getPatchedResource(String name, String mappedName, LaunchClassLoader loader) throws IOException {
        return applyPatch(name, mappedName, loader.getClassBytes(name));
    }

    public byte[] applyPatch(String name, String mappedName, byte[] inputData){
        if(this.patches == null){
            return inputData;
        }
        if(this.cache.containsKey(name)){
            return this.cache.get(name);
        }
        Collection<ClassPatch> list = patches.get(name);
        if(list.isEmpty()){
            return inputData;
        }

        for(ClassPatch patch : list){
            if(!patch.targetClassName.equals(mappedName) && !patch.sourceClassName.equals(name)){
                logger.warn("Binary patch found %s for wrong class %s", patch.targetClassName, mappedName);
            }
            if(!patch.existsAtTarget && (inputData == null || inputData.length == 0)){
                inputData = new byte[0];
            }else if(!patch.existsAtTarget){
                logger.warn("Patcher expecting empty class data file for %s, but received non-empty", patch.targetClassName);
            }else{
                int inputChecksum = Hashing.adler32().hashBytes(inputData).asInt();
                if(patch.inputChecksum != inputChecksum){
                    logger.fatal("There is a binary discrepency between the expected input class %s (%s) and the actual class. Checksum on disk is %x, in patch %x. Things are probably about to go very wrong. Did you put something into the jar file?", mappedName, name, inputChecksum, patch.inputChecksum);
                    if(!ignorePatchDiscrepancies){
                        logger.fatal("Server is shutting down now! (You can try doing -Dnailed.ignorePatchDiscrepancies=true to ignore this error)");
                        System.exit(1);
                    }else{
                        logger.warn("We are going to ignore this error. Chances are that the server won't be able to load properly");
                        continue;
                    }
                }
            }
            synchronized(patcher){
                try{
                    inputData = patcher.patch(inputData, patch.patch);
                }catch (IOException e){
                    logger.error("Encountered a problem while runtime patching class " + name, e);
                }
            }
        }

        if(dumpPatched){
            try{
                Files.write(inputData, new File(tempDir,mappedName));
            }catch(IOException e){
                logger.error("Failed to write patched class " + mappedName + " to " + tempDir.getAbsolutePath(), e);
            }
        }
        cache.put(name, inputData);
        return inputData;
    }

    public void setup(){
        logger.info("Loading binary patches...");
        Pattern binpatchMatcher = Pattern.compile("binpatch/server/.*.binpatch");
        JarInputStream jis = null;
        try{
            InputStream compressed = this.getClass().getResourceAsStream("/binpatches.pack.lzma");
            if(compressed == null){
                logger.warn("Was not able to find binary patches. Assuming development environment");
                return;
            }
            LzmaInputStream decompressed = new LzmaInputStream(compressed);
            ByteArrayOutputStream jarBytes = new ByteArrayOutputStream();
            JarOutputStream jos = new JarOutputStream(jarBytes);
            Pack200.newUnpacker().unpack(decompressed, jos);
            jis = new JarInputStream(new ByteArrayInputStream(jarBytes.toByteArray()));
        }catch(Exception e){
            logger.error("Error occurred while reading binary patches", e);
            throw new RuntimeException(e);
        }
        this.patches = ArrayListMultimap.create();

        while(true){
            try{
                JarEntry entry = jis.getNextJarEntry();
                if(entry == null){
                    break;
                }
                if(binpatchMatcher.matcher(entry.getName()).matches()){
                    ClassPatch patch = readPatch(entry, jis);
                    if(patch != null){
                        patches.put(patch.sourceClassName, patch);
                    }
                }else{
                    jis.closeEntry();
                }
            }catch(IOException e){
            }
        }
        logger.info("Successfully loaded {} binary patches", patches.size());
        cache.clear();
    }

    private ClassPatch readPatch(JarEntry entry, JarInputStream jis){
        ByteArrayDataInput input;
        try{
            input = ByteStreams.newDataInput(ByteStreams.toByteArray(jis));
        }catch(IOException e){
            logger.warn("Unable to read binpatch file {}. Ignoring it", entry.getName());
            return null;
        }
        String name = input.readUTF();
        String sourceName = input.readUTF();
        String targetName = input.readUTF();
        boolean exists = input.readBoolean();
        int inputChecksum = exists ? input.readInt() : 0;
        int patchLength = input.readInt();
        byte[] patchBytes = new byte[patchLength];
        input.readFully(patchBytes);

        return new ClassPatch(name, sourceName, targetName, exists, inputChecksum, patchBytes);
    }

    public static BinPatchManager instance(){
        return INSTANCE;
    }
}
