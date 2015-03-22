package jk_5.nailed.server.tweaker.remapping;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.commons.RemappingMethodAdapter;

public class NailedRemappingAdapter extends RemappingClassAdapter {

    public NailedRemappingAdapter(ClassVisitor cv) {
        super(cv, NameRemapper.instance());
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if(interfaces == null){
            interfaces = new String[0];
        }
        NameRemapper.instance().mergeSuperMaps(name, superName, interfaces);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    protected MethodVisitor createRemappingMethodAdapter(int access, String newDesc, MethodVisitor mv) {
        return new StaticFixingMethodVisitor(access, newDesc, mv, remapper);
    }

    private static class StaticFixingMethodVisitor extends RemappingMethodAdapter {

        public StaticFixingMethodVisitor(int access, String desc, MethodVisitor mv, Remapper remapper) {
            super(access, desc, mv, remapper);
        }

        @Override
        public void visitFieldInsn(int opcode, String originalType, String originalName, String desc) {
            //This MethodVisitor solves the problem of a static field reference changing type.
            //Chances are big that this change is compatible, however we need to fix up the descriptor to point at the new type
            String type = remapper.mapType(originalType);
            String fieldName = remapper.mapFieldName(originalType, originalName, desc);
            String newDesc = remapper.mapDesc(desc);
            if(opcode == Opcodes.GETSTATIC && type.startsWith("net/minecraft/") && newDesc.startsWith("Lnet/minecraft/")){
                String replDesc = NameRemapper.instance().getStaticFieldType(originalType, originalName, type, fieldName);
                if(replDesc != null){
                    newDesc = remapper.mapDesc(replDesc);
                }
            }
            // super.super
            if(mv != null){
                mv.visitFieldInsn(opcode, type, fieldName, newDesc);
            }
        }
    }
}
