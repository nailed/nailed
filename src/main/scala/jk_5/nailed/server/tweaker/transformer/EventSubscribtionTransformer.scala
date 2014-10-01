/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package jk_5.nailed.server.tweaker.transformer

import jk_5.eventbus.Event
import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm._
import org.objectweb.asm.tree.{ClassNode, FieldNode, MethodNode}

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
class EventSubscribtionTransformer extends IClassTransformer {

  override def transform(name: String, transformedName: String, bytes: Array[Byte]): Array[Byte] = {
    if(bytes == null || name.equals("jk_5.eventbus.Event") || name.startsWith("net.minecraft.") || name.indexOf('.') == -1){
      return bytes
    }
    val reader = new ClassReader(bytes)
    val cnode = new ClassNode()
    reader.accept(cnode, 0)
    try{
      if(buildEvents(cnode)){
        val writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)
        cnode.accept(writer)
        return writer.toByteArray
      }
      return bytes
    }catch{
      case e: ClassNotFoundException =>
      case e: Exception => e.printStackTrace()
    }
    bytes
  }

  private def buildEvents(cnode: ClassNode): Boolean = {
    val parent = this.getClass.getClassLoader.loadClass(cnode.superName.replace('/', '.'))
    if(!classOf[Event].isAssignableFrom(parent)) return false

    println("Transforming " + cnode.name)

    var hasSetup = false
    var hasGetHandlerList = false
    var hasDefaultConstructor = false
    val handlerListClass = Class.forName("jk_5.eventbus.HandlerList", false, getClass.getClassLoader)
    val tlist = Type.getType(handlerListClass)

    for(method <- cnode.methods){
      if(method.name == "setup" && method.desc.equals(Type.getMethodDescriptor(Type.VOID_TYPE)) && (method.access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED){
        hasSetup = true
      }
      if(method.name == "getHandlerList" && method.desc.equals(Type.getMethodDescriptor(tlist)) && (method.access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC){
        hasGetHandlerList = true
      }
      if(method.name == "<init>" && method.desc.equals(Type.getMethodDescriptor(Type.VOID_TYPE))){
        hasDefaultConstructor = true
      }
    }

    if(hasSetup){
      if(!hasGetHandlerList){
        throw new RuntimeException("Event class \"" + cnode.name + "\" defines setup() but does not define getHandlerList!")
      }else{
        return false
      }
    }

    val tsuper = Type.getType(cnode.superName)

    cnode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, "HANDLER_LIST", tlist.getDescriptor, null, null))

    if(!hasDefaultConstructor){
      /* Add:
       *  public void <init>(){
       *      super();
       *  }
       */
      val m = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null)
      m.visitVarInsn(Opcodes.ALOAD, 0)
      m.visitMethodInsn(Opcodes.INVOKESPECIAL, tsuper.getInternalName, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false)
      m.visitInsn(Opcodes.RETURN)
      cnode.methods.add(m)
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
      val m = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PROTECTED, "setup", Type.getMethodDescriptor(Type.VOID_TYPE), null, null)
      m.visitVarInsn(Opcodes.ALOAD, 0)
      m.visitMethodInsn(Opcodes.INVOKESPECIAL, tsuper.getInternalName, "setup", Type.getMethodDescriptor(Type.VOID_TYPE), false)
      m.visitFieldInsn(Opcodes.GETSTATIC, cnode.name, "HANDLER_LIST", tlist.getDescriptor)
      val initListener = new Label
      m.visitJumpInsn(Opcodes.IFNULL, initListener)
      m.visitInsn(Opcodes.RETURN)
      m.visitLabel(initListener)
      m.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
      m.visitTypeInsn(Opcodes.NEW, tlist.getInternalName)
      m.visitInsn(Opcodes.DUP)
      m.visitVarInsn(Opcodes.ALOAD, 0)
      m.visitMethodInsn(Opcodes.INVOKESPECIAL, tsuper.getInternalName, "getHandlerList", Type.getMethodDescriptor(tlist), false)
      m.visitMethodInsn(Opcodes.INVOKESPECIAL, tlist.getInternalName, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, tlist), false)
      m.visitFieldInsn(Opcodes.PUTSTATIC, cnode.name, "HANDLER_LIST", tlist.getDescriptor)
      m.visitInsn(Opcodes.RETURN)
      cnode.methods.add(m)
    }

    {
      /* Add:
       *  public HandlerList getHandlerList(){
       *      return HANDLER_LIST;
       *  }
       */
      val m = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC, "getHandlerList", Type.getMethodDescriptor(tlist), null, null)
      m.visitFieldInsn(Opcodes.GETSTATIC, cnode.name, "HANDLER_LIST", tlist.getDescriptor)
      m.visitInsn(Opcodes.ARETURN)
      cnode.methods.add(m)
    }

    true
  }
}
