package jk_5.nailed.server.tweaker.mixin

import java.io.PrintWriter
import java.lang.annotation.Annotation
import java.util

import org.objectweb.asm.ClassWriter.{COMPUTE_FRAMES, COMPUTE_MAXS}
import org.objectweb.asm.tree.{AbstractInsnNode, AnnotationNode, ClassNode, FieldNode, InsnList, InsnNode, IntInsnNode, LdcInsnNode, MethodInsnNode, MethodNode, VarInsnNode}
import org.objectweb.asm.util.CheckClassAdapter
import org.objectweb.asm.{ClassReader, ClassWriter, Opcodes, Type}

import scala.collection.convert.wrapAsScala._

object ASMHelper {

  private final val intConstants = IndexedSeq[Int](Opcodes.ICONST_0, Opcodes.ICONST_1, Opcodes.ICONST_2, Opcodes.ICONST_3, Opcodes.ICONST_4, Opcodes.ICONST_5)

  def generateBooleanMethodConst(clazz: ClassNode, name: String, retval: Boolean) {
    val method = new MethodNode(Opcodes.ASM5, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name, "()Z", null, null)
    val code = method.instructions
    code.add(pushIntConstant(if (retval) 1 else 0))
    code.add(new InsnNode(Opcodes.IRETURN))
    clazz.methods.add(method)
  }

  def generateIntegerMethodConst(clazz: ClassNode, name: String, retval: Short) {
    val method = new MethodNode(Opcodes.ASM5, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name, "()I", null, null)
    val code = method.instructions
    code.add(pushIntConstant(retval))
    code.add(new InsnNode(Opcodes.IRETURN))
    clazz.methods.add(method)
  }

  def generateSelfForwardingMethod(clazz: ClassNode, name: String, forwardname: String, rettype: Type) {
    val method = new MethodNode(Opcodes.ASM5, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name, "()" + rettype.getDescriptor, null, null)
    populateSelfForwardingMethod(method, forwardname, rettype, Type.getObjectType(clazz.name))
    clazz.methods.add(method)
  }

  def generateStaticForwardingMethod(clazz: ClassNode, name: String, forwardname: String, rettype: Type, argtype: Type) {
    val method = new MethodNode(Opcodes.ASM5, Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name, "()" + rettype.getDescriptor, null, null)
    populateSelfForwardingMethod(method, forwardname, rettype, argtype)
    clazz.methods.add(method)
  }

  def generateForwardingToStaticMethod(clazz: ClassNode, name: String, forwardname: String, rettype: Type, fowardtype: Type) {
    val method = new MethodNode(Opcodes.ASM5, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name, "()" + rettype.getDescriptor, null, null)
    populateForwardingToStaticMethod(method, forwardname, rettype, Type.getObjectType(clazz.name), fowardtype)
    clazz.methods.add(method)
  }

  def generateForwardingToStaticMethod(clazz: ClassNode, name: String, forwardname: String, rettype: Type, fowardtype: Type, thistype: Type) {
    val method = new MethodNode(Opcodes.ASM5, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name, "()" + rettype.getDescriptor, null, null)
    populateForwardingToStaticMethod(method, forwardname, rettype, thistype, fowardtype)
    clazz.methods.add(method)
  }

  def replaceSelfForwardingMethod(method: MethodNode, forwardname: String, thistype: Type) {
    val methodType: Type = Type.getMethodType(method.desc)
    method.instructions.clear()
    populateSelfForwardingMethod(method, forwardname, methodType.getReturnType, thistype)
  }

  def generateForwardingMethod(clazz: ClassNode, name: String, forwardname: String, rettype: Type, argtype: Type) {
    val method = new MethodNode(Opcodes.ASM5, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name, "()" + rettype.getDescriptor, null, null)
    populateForwardingMethod(method, forwardname, rettype, argtype, Type.getObjectType(clazz.name))
    clazz.methods.add(method)
  }

  def replaceForwardingMethod(method: MethodNode, forwardname: String, thistype: Type) {
    val methodType: Type = Type.getMethodType(method.desc)
    method.instructions.clear()
    populateForwardingMethod(method, forwardname, methodType.getReturnType, methodType.getArgumentTypes()(0), thistype)
  }

  def populateForwardingToStaticMethod(method: MethodNode, forwardname: String, rettype: Type, thistype: Type, forwardtype: Type) {
    val code: InsnList = method.instructions
    code.add(new VarInsnNode(thistype.getOpcode(Opcodes.ILOAD), 0))
    code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, forwardtype.getInternalName, forwardname, Type.getMethodDescriptor(rettype, thistype), false))
    code.add(new InsnNode(rettype.getOpcode(Opcodes.IRETURN)))
  }

  def populateSelfForwardingMethod(method: MethodNode, forwardname: String, rettype: Type, thistype: Type) {
    val code: InsnList = method.instructions
    code.add(new VarInsnNode(thistype.getOpcode(Opcodes.ILOAD), 0))
    code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, thistype.getInternalName, forwardname, "()" + rettype.getDescriptor, false))
    code.add(new InsnNode(rettype.getOpcode(Opcodes.IRETURN)))
  }

  def populateForwardingMethod(method: MethodNode, forwardname: String, rettype: Type, argtype: Type, thistype: Type) {
    val code: InsnList = method.instructions
    code.add(new VarInsnNode(argtype.getOpcode(Opcodes.ILOAD), 1))
    code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, argtype.getInternalName, forwardname, "()" + rettype.getDescriptor, false))
    code.add(new InsnNode(rettype.getOpcode(Opcodes.IRETURN)))
  }

  def pushIntConstant(c: Int): AbstractInsnNode = {
    if (c == -1) new InsnNode(Opcodes.ICONST_M1)
    else if (c >= 0 && c <= 5) new InsnNode(intConstants(c))
    else if (c >= Byte.MinValue && c <= Byte.MaxValue) new IntInsnNode(Opcodes.BIPUSH, c)
    else if (c >= Short.MinValue && c <= Short.MaxValue) new IntInsnNode(Opcodes.SIPUSH, c)
    else new LdcInsnNode(c)
  }

  def findMethod(clazz: ClassNode, name: String, desc: String): MethodNode = {
    val i = clazz.methods.iterator
    while(i.hasNext){
      val m = i.next
      if(m.name == name && m.desc == desc) return m
    }
    null
  }

  def addAndReplaceMethod(clazz: ClassNode, method: MethodNode) {
    val m = findMethod(clazz, method.name, method.desc)
    if(m != null){
      clazz.methods.remove(m)
    }
    clazz.methods.add(method)
  }

  def dumpClass(classNode: ClassNode) {
    val cw: ClassWriter = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES)
    classNode.accept(cw)
    dumpClass(cw.toByteArray)
  }

  def dumpClass(bytes: Array[Byte]) {
    val cr: ClassReader = new ClassReader(bytes)
    CheckClassAdapter.verify(cr, true, new PrintWriter(System.out))
  }

  def getVisibleAnnotation(field: FieldNode, annotationClass: Class[_ <: Annotation]): AnnotationNode = {
    ASMHelper.getAnnotation(field.visibleAnnotations, Type.getDescriptor(annotationClass))
  }

  def getInvisibleAnnotation(field: FieldNode, annotationClass: Class[_ <: Annotation]): AnnotationNode = {
    ASMHelper.getAnnotation(field.invisibleAnnotations, Type.getDescriptor(annotationClass))
  }

  def getVisibleAnnotation(method: MethodNode, annotationClass: Class[_ <: Annotation]): AnnotationNode = {
    ASMHelper.getAnnotation(method.visibleAnnotations, Type.getDescriptor(annotationClass))
  }

  def getInvisibleAnnotation(method: MethodNode, annotationClass: Class[_ <: Annotation]): AnnotationNode = {
    ASMHelper.getAnnotation(method.invisibleAnnotations, Type.getDescriptor(annotationClass))
  }

  def getVisibleAnnotation(classNode: ClassNode, annotationClass: Class[_ <: Annotation]): AnnotationNode = {
    ASMHelper.getAnnotation(classNode.visibleAnnotations, Type.getDescriptor(annotationClass))
  }

  def getInvisibleAnnotation(classNode: ClassNode, annotationClass: Class[_ <: Annotation]): AnnotationNode = {
    ASMHelper.getAnnotation(classNode.invisibleAnnotations, Type.getDescriptor(annotationClass))
  }

  def getAnnotation(annotations: util.List[AnnotationNode], annotationType: String): AnnotationNode = {
    if(annotations == null){
      return null
    }
    for(annotation <- annotations){
      if(annotationType == annotation.desc){
        return annotation
      }
    }
    null
  }

  def getAnnotationValue[T](annotation: AnnotationNode, key: String = "value"): T = {
    var getNextValue = false
    if(annotation.values == null) return null.asInstanceOf[T]
    for(value <- annotation.values){
      if(getNextValue) return value.asInstanceOf[T]
      if(value == key) getNextValue = true
    }
    null.asInstanceOf[T]
  }
}
