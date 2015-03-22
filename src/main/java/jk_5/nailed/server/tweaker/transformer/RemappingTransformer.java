package jk_5.nailed.server.tweaker.transformer;

import jk_5.nailed.server.tweaker.remapping.NailedRemappingAdapter;
import jk_5.nailed.server.tweaker.remapping.NameRemapper;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class RemappingTransformer implements IClassTransformer, IClassNameTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if(bytes == null){
            return null;
        }
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        NailedRemappingAdapter remapper = new NailedRemappingAdapter(writer);
        reader.accept(remapper, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    @Override
    public String unmapClassName(String name) {
        return NameRemapper.unmap(name.replace('.', '/')).replace('/','.');
    }

    @Override
    public String remapClassName(String name) {
        return NameRemapper.map(name.replace('.', '/')).replace('/', '.');
    }
}
