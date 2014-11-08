package jk_5.nailed.server.tweaker.mixin

import java.util

import jk_5.nailed.server.tweaker.transformer.TreeTransformer
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.tree._
import org.objectweb.asm.{Opcodes, Type}

import scala.collection.convert.wrapAsScala._

object MixinTransformer {
  private def hasFlag(method: MethodNode, flag: Int): Boolean = (method.access & flag) == flag
  private def hasFlag(field: FieldNode, flag: Int): Boolean = (field.access & flag) == flag
}

class MixinTransformer extends TreeTransformer {

  val logger = LogManager.getLogger

  private val config = MixinConfig("mixins.json")

  override def transform(name: String, transformedName: String, bytes: Array[Byte]): Array[Byte] = {
    if(transformedName != null && transformedName.startsWith(this.config.getMixinPackage)){
      throw new RuntimeException(s"$transformedName is a mixin class and cannot be referenced directly")
    }

    if(this.config.hasMixinsFor(transformedName)){
      try {
        return this.applyMixins(transformedName, bytes)
      }catch{
        case e: InvalidMixinException => logger.warn(s"Class mixin failed: ${e.getClass.getName} ${e.getMessage}", e)
      }
    }
    bytes
  }

  private def applyMixins(name: String, bytes: Array[Byte]): Array[Byte] = {
    val targetClass = this.readClass(bytes, cacheReader = true)
    val mixins = this.config.getMixinsFor(name)
    util.Collections.sort(mixins)

    for(mixin <- mixins){
      logger.info("Applying mixin {} to {}", mixin.getClassName, name)
      this.applyMixin(targetClass, mixin.getData)
    }

    this.postTransform(name, targetClass, mixins)

    this.writeClass(targetClass)
  }

  protected def postTransform(name: String, targetClass: ClassNode, mixins: util.List[MixinInfo]){
    //TODO
  }

  protected def applyMixin(targetClass: ClassNode, mixin: MixinData){
    try{
      this.verifyClasses(targetClass, mixin)
      this.applyMixinInterfaces(targetClass, mixin)
      this.applyMixinAttributes(targetClass, mixin)
      this.applyMixinFields(targetClass, mixin)
      this.applyMixinMethods(targetClass, mixin)
    }catch{
      case e: Exception => throw new InvalidMixinException("Unexpecteded error whilst applying the mixin class", e)
    }
  }

  protected def verifyClasses(targetClass: ClassNode, mixin: MixinData){
    val superName = mixin.getClassNode.superName
    if(targetClass.superName == null || superName == null || targetClass.superName != superName){
      throw new InvalidMixinException("Mixin classes must have the same superclass as their target class")
    }
  }

  private def applyMixinInterfaces(targetClass: ClassNode, mixin: MixinData){
    for(interfaceName <- mixin.getClassNode.interfaces){
      if(!targetClass.interfaces.contains(interfaceName)){
        targetClass.interfaces.add(interfaceName)
      }
    }
  }

  private def applyMixinAttributes(targetClass: ClassNode, mixin: MixinData){
    if(this.config.shouldSetSourceFile){
      targetClass.sourceFile = mixin.getClassNode.sourceFile
    }
  }

  private def applyMixinFields(targetClass: ClassNode, mixin: MixinData){
    for(field <- mixin.getClassNode.fields){
      if(MixinTransformer.hasFlag(field, Opcodes.ACC_STATIC) && !MixinTransformer.hasFlag(field, Opcodes.ACC_PRIVATE)){
        throw new InvalidMixinException(s"Mixin classes cannot contain visible static methods or fields, found ${field.name}")
      }

      val target = this.findTargetField(targetClass, field)
      if(target == null){
        val isShadow = ASMHelper.getVisibleAnnotation(field, classOf[Shadow]) != null
        if(isShadow){
          throw new InvalidMixinException(s"Shadow field ${field.name} was not located in the target class")
        }
        targetClass.fields.add(field)
      }else{
        if(target.desc != field.desc){
          throw new InvalidMixinException(s"The field ${field.name} in the target class has a conflicting signature")
        }
      }
    }
  }

  private def applyMixinMethods(targetClass: ClassNode, mixin: MixinData){
    for(mixinMethod <- mixin.getClassNode.methods){
      this.transformMethod(mixinMethod, mixin.getClassNode.name, targetClass.name)

      val isShadow = ASMHelper.getVisibleAnnotation(mixinMethod, classOf[Shadow]) != null
      val isOverwrite = ASMHelper.getVisibleAnnotation(mixinMethod, classOf[Overwrite]) != null
      val isAbstract = MixinTransformer.hasFlag(mixinMethod, Opcodes.ACC_ABSTRACT)

      if(isShadow || isAbstract){
        val target = this.findTargetMethod(targetClass, mixinMethod)
        if(target == null){
          throw new InvalidMixinException(s"Shadow method ${mixinMethod.name} was not located in the target class")
        }
      }else if(!mixinMethod.name.startsWith("<")){
        if(MixinTransformer.hasFlag(mixinMethod, Opcodes.ACC_STATIC) && !MixinTransformer.hasFlag(mixinMethod, Opcodes.ACC_PRIVATE) && !isOverwrite){
          throw new InvalidMixinException(s"Mixin classes cannot contain visible static methods or fields, found ${mixinMethod.name}")
        }

        val target = this.findTargetMethod(targetClass, mixinMethod)
        if(target != null){
          targetClass.methods.remove(target)
        }else if(isOverwrite){
          throw new InvalidMixinException(s"Overwrite target ${mixinMethod.name} was not located in the target class")
        }
        targetClass.methods.add(mixinMethod)
      }else if("<clinit>" == mixinMethod.name){
        this.appendInsns(targetClass, mixinMethod.name, mixinMethod)
      }
    }
  }

  private def transformMethod(method: MethodNode, fromClass: String, toClass: String){
    val iter = method.instructions.iterator()
    while(iter.hasNext){
      val insn = iter.next()
      insn match {
        case i: MethodInsnNode if i.owner == fromClass => i.owner = toClass
        case i: FieldInsnNode if i.owner == fromClass => i.owner = toClass
        case _ =>
      }
    }
  }

  private def appendInsns(targetClass: ClassNode, targetMethodName: String, sourceMethod: MethodNode){
    var targetMethodN = targetMethodName
    if(Type.getReturnType(sourceMethod.desc) != Type.VOID_TYPE){
      throw new IllegalArgumentException("Attempted to merge insns into a method which does not return void")
    }

    if(targetMethodN == null || targetMethodN.length() == 0){
      targetMethodN = sourceMethod.name
    }

    for(method <- targetClass.methods){
      if(targetMethodN == method.name && sourceMethod.desc.equals(method.desc)){
        var returnNode: AbstractInsnNode = null
        val findReturnIter = method.instructions.iterator()
        var stop = false
        while(findReturnIter.hasNext && !stop){
          val insn = findReturnIter.next()
          if(insn.getOpcode == Opcodes.RETURN){
            returnNode = insn
            stop = true
          }
        }

        val injectIter = sourceMethod.instructions.iterator()
        while(injectIter.hasNext){
          val insn = injectIter.next()
          if(!insn.isInstanceOf[LineNumberNode] && insn.getOpcode != Opcodes.RETURN){
            method.instructions.insertBefore(returnNode, insn)
          }
        }
      }
    }
  }

  private def findTargetField(targetClass: ClassNode, searchFor: FieldNode): FieldNode = {
    for(target <- targetClass.fields){
      if(target.name.equals(searchFor.name)) return target
    }
    null
  }

  private def findTargetMethod(targetClass: ClassNode, searchFor: MethodNode): MethodNode = {
    for(target <- targetClass.methods){
      if(target.name == searchFor.name && target.desc == searchFor.desc) return target
    }
    null
  }
}
