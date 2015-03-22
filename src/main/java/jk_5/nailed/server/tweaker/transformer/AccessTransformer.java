package jk_5.nailed.server.tweaker.transformer;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AccessTransformer implements IClassTransformer {

    private static final Multimap<String, Modifier> modifiers = ArrayListMultimap.create();
    private static final Logger logger = LogManager.getLogger();

    private static class Modifier {
        String name = "";
        String desc = "";
        int oldAccess = 0;
        int newAccess = 0;
        int targetAccess = 0;
        boolean changeFinal = false;
        boolean markFinal = false;
        boolean modifyClassVisibility = false;

        public void setTargetAccess(String name){
            if(name.startsWith("public")){
                targetAccess = Opcodes.ACC_PUBLIC;
            }else if(name.startsWith("private")){
                targetAccess = Opcodes.ACC_PRIVATE;
            }else if(name.startsWith("protected")){
                targetAccess = Opcodes.ACC_PROTECTED;
            }

            if(name.endsWith("-f")){
                changeFinal = true;
                markFinal = false;
            }else if(name.endsWith("+f")){
                changeFinal = true;
                markFinal = true;
            }
        }
    }

    public static void readConfig(String path){
        File file = new File(path);
        URL rulesResource;
        if(file.exists()){
            try{
                rulesResource = file.toURI().toURL();
            }catch(MalformedURLException e){
                throw new RuntimeException(e); //Impossible
            }
        }else{
            rulesResource = Resources.getResource(path);
        }
        try{
            processFile(Resources.asCharSource(rulesResource, Charsets.UTF_8));
            logger.info("Loaded " + modifiers.size() + " rules from AccessTransformer config file " + path);
        }catch(IOException e){
            logger.error("Was not able to load AccessTransformer " + path, e);
        }
    }

    private static void processFile(CharSource resource) throws IOException {
        List<String> lines = resource.readLines();
        for (String input : lines) {
            String line = Iterables.getFirst(Splitter.on('#').limit(2).split(input), "").trim();
            if(line.length() != 0){
                List<String> parts = Lists.newArrayList(Splitter.on(' ').trimResults().split(line));
                if(parts.size() > 3){
                    throw new RuntimeException("Illegal AccessTransformer line: " + input);
                }
                Modifier m = new Modifier();
                m.setTargetAccess(parts.get(0));

                if(parts.size() == 2){
                    m.modifyClassVisibility = true;
                }else{
                    String nameReference = parts.get(2);
                    int parenIdx = nameReference.indexOf('(');
                    if(parenIdx > 0){
                        m.desc = nameReference.substring(parenIdx);
                        m.name = nameReference.substring(0,parenIdx);
                    }else{
                        m.name = nameReference;
                    }
                }
                String className = parts.get(1).replace('/', '.');
                modifiers.put(className, m);
            }
        }
    }

    @Override
    public byte[] transform(String name, String mappedName, byte[] bytes) {
        if(bytes == null){
            return null;
        }
        if(!AccessTransformer.modifiers.containsKey(mappedName)){
            return bytes;
        }

        ClassNode cnode = new ClassNode();
        ClassReader creader = new ClassReader(bytes);
        creader.accept(cnode, 0);

        for (Modifier m : AccessTransformer.modifiers.get(mappedName)) {
            if(m.modifyClassVisibility){
                cnode.access = modifyAccess(cnode.access, m);
                continue;
            }
            if(m.desc.isEmpty()){
                for (FieldNode n : cnode.fields) {
                    if(n.name.equals(m.name) || m.name.equals("*")){
                        n.access = modifyAccess(n.access, m);
                        if(!m.name.equals("*")) break;
                    }
                }
            }else{
                List<MethodNode> changeInvoke = new ArrayList<MethodNode>();
                for (MethodNode n : cnode.methods) {
                    if((n.name.equals(m.name) && n.desc.equals(m.desc)) || m.name.equals("*")){
                        n.access = modifyAccess(n.access, m);

                        if(!n.name.equals("<init>")){
                            //If we change a method from private to something else, we need to replace all INVOKESPECIAL to it with INVOKEVIRTUAL
                            //Otherwise overridden methods won't be called.
                            //We only need to scan this class for that, because the method was private before, and there are no external references to it
                            boolean wasPrivate = (m.oldAccess & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE;
                            boolean isNowPrivate = (m.newAccess & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE;

                            if(wasPrivate && !isNowPrivate) changeInvoke.add(n);
                        }

                        if(!m.name.equals("*")) break;
                    }
                }
                replaceInvokeSpecial(cnode, changeInvoke);
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cnode.accept(writer);
        return writer.toByteArray();
    }

    private void replaceInvokeSpecial(ClassNode cnode, List<MethodNode> toReplace){
        for (MethodNode method : cnode.methods) {
            Iterator<AbstractInsnNode> it = method.instructions.iterator();
            while(it.hasNext()){
                AbstractInsnNode insn = it.next();
                if(insn.getOpcode() == Opcodes.INVOKESPECIAL){
                    MethodInsnNode minsn = ((MethodInsnNode) insn);
                    for (MethodNode n : toReplace) {
                        if(n.name.equals(minsn.name) && n.desc.equals(minsn.desc)){
                            minsn.setOpcode(Opcodes.INVOKEVIRTUAL);
                            break;
                        }
                    }
                }
            }
        }
    }

    private int modifyAccess(int access, Modifier target){
        target.oldAccess = access;
        int t = target.targetAccess;
        int ret = access & ~7;

        switch(access & 7){
            case Opcodes.ACC_PRIVATE:
                ret |= t;
                break;
            case 0:
                ret |= ((t != Opcodes.ACC_PRIVATE) ? t : 0);
                break;
            case Opcodes.ACC_PROTECTED:
                ret |= ((t != Opcodes.ACC_PRIVATE && t != 0) ? t : Opcodes.ACC_PROTECTED);
                break;
            case Opcodes.ACC_PUBLIC:
                ret |= ((t != Opcodes.ACC_PRIVATE && t != 0 && t != Opcodes.ACC_PROTECTED) ? t : Opcodes.ACC_PUBLIC);
                break;
            default:
                throw new Error("Unknown access type");
        }

        if(target.changeFinal){
            if(target.markFinal){
                ret |= Opcodes.ACC_FINAL;
            }else{
                ret &= ~Opcodes.ACC_FINAL;
            }
        }
        target.newAccess = ret;
        return ret;
    }
}
