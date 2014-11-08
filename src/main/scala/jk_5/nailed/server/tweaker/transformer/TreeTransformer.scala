package jk_5.nailed.server.tweaker.transformer

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.{ClassReader, ClassWriter}

abstract class TreeTransformer extends IClassTransformer {

  private var classReader: ClassReader = _
  private var classNode: ClassNode = _

  protected final def readClass(cl: Array[Byte], cacheReader: Boolean = true): ClassNode = {
    val creader = new ClassReader(cl)
    if(cacheReader) this.classReader = creader

    val cnode = new ClassNode
    creader.accept(cnode, ClassReader.EXPAND_FRAMES)
    cnode
  }

  protected final def writeClass(cnode: ClassNode): Array[Byte] = {
    if(this.classReader != null && this.classNode == cnode){
      this.classNode = null
      val writer = new ClassWriter(this.classReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)
      this.classReader = null
      cnode.accept(writer)
      return writer.toByteArray
    }

    this.classNode = null

    val writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)
    cnode.accept(writer)
    writer.toByteArray
  }
}
