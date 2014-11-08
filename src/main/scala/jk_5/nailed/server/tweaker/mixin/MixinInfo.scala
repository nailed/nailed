package jk_5.nailed.server.tweaker.mixin

import java.io.IOException
import java.util

import net.minecraft.launchwrapper.Launch
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.{ClassReader, Type}

import scala.collection.convert.wrapAsScala._

private[mixin] object MixinInfo {

  private[mixin] var mixinOrder = 0
}

private[mixin] class MixinInfo(
  private val className: String,
  private val runTransformers: Boolean
) extends Comparable[MixinInfo] {

  @transient private val logger = LogManager.getLogger
  @transient private val classRef: String = className.replace('.', '/')
  @transient private val order: Int = {MixinInfo.mixinOrder += 1; MixinInfo.mixinOrder - 1}
  @transient private final val mixinBytes: Array[Byte] = this.loadMixinClass(className, runTransformers)

  private var classNode = this.getClassNode(0)
  private val targetClasses = this.readTargetClasses(classNode)
  private val priority = this.readPriority(classNode)
  classNode = null

  private def readTargetClasses(classNode: ClassNode): Seq[String] = {
    val mixin = ASMHelper.getInvisibleAnnotation(classNode, classOf[Mixin])
    if(mixin == null){
      throw new InvalidMixinException(s"The mixin '$className' is missing an @Mixin annotation")
    }
    val targetClasses = ASMHelper.getAnnotationValue[util.List[Type]](mixin)
    var targetClassNames = Seq[String]()
    for(targetClass <- targetClasses){
      targetClassNames = targetClassNames :+ targetClass.getClassName :+ targetClass.getClassName
    }
    targetClassNames
  }

  private def readPriority(classNode: ClassNode): Int = {
    val mixin = ASMHelper.getInvisibleAnnotation(classNode, classOf[Mixin])
    if(mixin == null){
      throw new InvalidMixinException(s"The mixin '$className' is missing an @Mixin annotation")
    }
    val priority = ASMHelper.getAnnotationValue[java.lang.Integer](mixin, "priority")
    if(priority == null) 1000 else priority.intValue
  }

  def getClassName = this.className
  def getClassRef = this.classRef
  def getClassBytes = this.mixinBytes
  def getTargetClasses = this.targetClasses
  def getPriority = this.priority
  def getData = new MixinData(this)

  def getClassNode(flags: Int): ClassNode = {
    val classNode: ClassNode = new ClassNode
    val classReader: ClassReader = new ClassReader(this.mixinBytes)
    classReader.accept(classNode, flags)
    classNode
  }

  private def loadMixinClass(mixinClassName: String, runTransformers: Boolean): Array[Byte] = {
    var mixinBytes: Array[Byte] = null
    try{
      mixinBytes = this.getClassBytes(mixinClassName)
      if(mixinBytes == null){
        throw new InvalidMixinException(s"The specified mixin '$mixinClassName' was not found")
      }
      if(runTransformers){
        mixinBytes = this.applyTransformers(mixinClassName, mixinBytes)
      }
    }catch{
      case ex: IOException =>
        logger.warn(s"Failed to load mixin $mixinClassName, the specified mixin will not be applied")
        throw new InvalidMixinException("An error was encountered whilst loading the mixin class", ex)
    }
    mixinBytes
  }

  @inline private def getClassBytes(mixinClassName: String): Array[Byte] = Launch.classLoader.getClassBytes(mixinClassName)

  private def applyTransformers(name: String, b: Array[Byte]): Array[Byte] = {
    var basicClass = b
    val transformers = Launch.classLoader.getTransformers
    for(transformer <- transformers){
      if(!transformer.isInstanceOf[MixinTransformer]){
        basicClass = transformer.transform(name, name, basicClass)
      }
    }
    basicClass
  }

  def compareTo(other: MixinInfo): Int = {
    if(other == null) return 0
    if(other.priority == this.priority)  return this.order - other.order
    this.priority - other.priority
  }
}
