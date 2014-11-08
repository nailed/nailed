package jk_5.nailed.server.tweaker.mixin

import java.util

import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.{AnnotationNode, MethodInsnNode}

import scala.collection.convert.wrapAsScala._

object MixinData {

  private def getAnnotationValue(annotation: AnnotationNode, key: String, annotationClass: Class[_]): String = {
    var value = ASMHelper.getAnnotationValue[String](annotation, key)
    if(value == null){
      try{
        value = classOf[Shadow].getDeclaredMethod(key).getDefaultValue.asInstanceOf[String]
      }catch{
        case e: NoSuchMethodException =>
      }
    }
    value
  }
}

class MixinData(info: MixinInfo) {

  private val classNode = info.getClassNode(ClassReader.EXPAND_FRAMES)
  private val renamedMethods = new util.HashMap[String, String]()
  this.prepare()

  def getClassNode = this.classNode

  private def prepare(){
    this.findRenamedMethods()
    this.transformMethods()
  }

  private def findRenamedMethods(){
    for(mixinMethod <- this.classNode.methods){
      val shadowAnnotation = ASMHelper.getVisibleAnnotation(mixinMethod, classOf[Shadow])
      if(shadowAnnotation != null){
        val prefix = MixinData.getAnnotationValue(shadowAnnotation, "prefix", classOf[Shadow])
        if(mixinMethod.name.startsWith(prefix)){
          val newName = mixinMethod.name.substring(prefix.length())
          this.renamedMethods.put(mixinMethod.name + mixinMethod.desc, newName)
          mixinMethod.name = newName
        }
      }
    }
  }

  private def transformMethods(){
    for(mixinMethod <- this.classNode.methods){
      val iter = mixinMethod.instructions.iterator()
      while(iter.hasNext){
        val insn = iter.next()
        insn match {
          case i: MethodInsnNode =>
            val newName = this.renamedMethods.get(i.name + i.desc)
            if(newName != null) i.name = newName
          case _ =>
        }
      }
    }
  }
}
