package jk_5.nailed.server.tweaker.transformer;

import jk_5.eventbus.Event;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class EventSubscriptionTransformer implements IClassTransformer {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if(bytes == null || name.equals("jk_5.eventbus.Event") || name.startsWith("net.minecraft.") || name.indexOf('.') == -1){
            return bytes;
        }
        ClassReader reader = new ClassReader(bytes);
        ClassNode cnode = new ClassNode();
        reader.accept(cnode, 0);
        try{
            if(buildEvents(cnode)){
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                cnode.accept(writer);
                return writer.toByteArray();
            }
            return bytes;
        }catch(ClassNotFoundException e){
        }catch(Exception e){
            logger.error("Error while transforming " + name, e);
        }
        return bytes;
    }

    private boolean buildEvents(ClassNode cnode) throws ClassNotFoundException {
        Class<?> parent = this.getClass().getClassLoader().loadClass(cnode.superName.replace('/', '.'));
        if(!Event.class.isAssignableFrom(parent)){
            return false;
        }

        logger.info("Transforming " + cnode.name);

        boolean hasSetup = false;
        boolean hasGetHandlerList = false;
        boolean hasDefaultConstructor = false;
        Class<?> handlerListClass = Class.forName("jk_5.eventbus.HandlerList", false, getClass().getClassLoader());
        Type tlist = Type.getType(handlerListClass);

        for (MethodNode method : cnode.methods) {
            if(method.name.equals("setup") && method.desc.equals(Type.getMethodDescriptor(Type.VOID_TYPE)) && (method.access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED){
                hasSetup = true;
            }
            if(method.name.equals("getHandlerList") && method.desc.equals(Type.getMethodDescriptor(tlist)) && (method.access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC){
                hasGetHandlerList = true;
            }
            if(method.name.equals("<init>") && method.desc.equals(Type.getMethodDescriptor(Type.VOID_TYPE))){
                hasDefaultConstructor = true;
            }
        }

        if(hasSetup){
            if(!hasGetHandlerList){
                throw new RuntimeException("Event class \"" + cnode.name + "\" defines setup() but does not define getHandlerList!");
            }else{
                return false;
            }
        }

        Type tsuper = Type.getType(cnode.superName);

        cnode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, "HANDLER_LIST", tlist.getDescriptor(), null, null));

        if(!hasDefaultConstructor){
            /* Add:
             *  public void <init>(){
             *      super();
             *  }
             */
            MethodNode m = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
            m.visitVarInsn(Opcodes.ALOAD, 0);
            m.visitMethodInsn(Opcodes.INVOKESPECIAL, tsuper.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
            m.visitInsn(Opcodes.RETURN);
            cnode.methods.add(m);
        }

        {
            /* Add:
             *  protected void setup(){
             *      super.setup();
             *      if(HANDLER_LIST != NULL){
             *          return;
             *      }
             *      HANDLER_LIST = new ListenerList(super.getListenerList());
             *  }
             */
            MethodNode m = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PROTECTED, "setup", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
            m.visitVarInsn(Opcodes.ALOAD, 0);
            m.visitMethodInsn(Opcodes.INVOKESPECIAL, tsuper.getInternalName(), "setup", Type.getMethodDescriptor(Type.VOID_TYPE), false);
            m.visitFieldInsn(Opcodes.GETSTATIC, cnode.name, "HANDLER_LIST", tlist.getDescriptor());
            Label initListener = new Label();
            m.visitJumpInsn(Opcodes.IFNULL, initListener);
            m.visitInsn(Opcodes.RETURN);
            m.visitLabel(initListener);
            m.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            m.visitTypeInsn(Opcodes.NEW, tlist.getInternalName());
            m.visitInsn(Opcodes.DUP);
            m.visitVarInsn(Opcodes.ALOAD, 0);
            m.visitMethodInsn(Opcodes.INVOKESPECIAL, tsuper.getInternalName(), "getHandlerList", Type.getMethodDescriptor(tlist), false);
            m.visitMethodInsn(Opcodes.INVOKESPECIAL, tlist.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, tlist), false);
            m.visitFieldInsn(Opcodes.PUTSTATIC, cnode.name, "HANDLER_LIST", tlist.getDescriptor());
            m.visitInsn(Opcodes.RETURN);
            cnode.methods.add(m);
        }

        {
            /* Add:
             *  public HandlerList getHandlerList(){
             *      return HANDLER_LIST;
             *  }
             */
            MethodNode m = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC, "getHandlerList", Type.getMethodDescriptor(tlist), null, null);
            m.visitFieldInsn(Opcodes.GETSTATIC, cnode.name, "HANDLER_LIST", tlist.getDescriptor());
            m.visitInsn(Opcodes.ARETURN);
            cnode.methods.add(m);
        }

        return true;
    }
}
