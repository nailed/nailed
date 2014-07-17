package jk_5.nailed.server.tweaker.transformer

import jk_5.nailed.server.tweaker.remapping.{NameRemapper, NailedRemappingAdapter}
import net.minecraft.launchwrapper.{IClassNameTransformer, IClassTransformer}
import org.objectweb.asm.{ClassWriter, ClassReader}

/**
 * No description given
 *
 * @author jk-5
 */
class RemappingTransformer extends IClassTransformer with IClassNameTransformer {

  override def transform(name: String, mappedName: String, bytes: Array[Byte]): Array[Byte] = {
    if(bytes == null) return null
    val reader = new ClassReader(bytes)
    val writer = new ClassWriter(ClassWriter.COMPUTE_MAXS)
    val remapper = new NailedRemappingAdapter(writer)
    reader.accept(remapper, ClassReader.EXPAND_FRAMES)
    writer.toByteArray
  }

  override def remapClassName(name: String): String = NameRemapper.map(name.replace('.','/')).replace('/', '.')
  override def unmapClassName(name: String): String = NameRemapper.unmap(name.replace('.', '/')).replace('/','.')
}
