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

import java.io.File
import java.util

import com.google.common.base.{Charsets, Splitter}
import com.google.common.collect.{ArrayListMultimap, Iterables, Lists}
import com.google.common.io.{CharSource, Resources}
import jk_5.nailed.server.tweaker.transformer.AccessTransformer.Modifier
import net.minecraft.launchwrapper.IClassTransformer
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.tree.{ClassNode, MethodInsnNode, MethodNode}
import org.objectweb.asm.{ClassReader, ClassWriter, Opcodes}

import scala.collection.convert.wrapAsScala._

/**
 * No description given
 *
 * @author jk-5
 */
object AccessTransformer {

  private val modifiers = ArrayListMultimap.create[String, Modifier]()
  private val logger = LogManager.getLogger

  private class Modifier {
    var name = ""
    var desc = ""
    var oldAccess = 0
    var newAccess = 0
    var targetAccess = 0
    var changeFinal = false
    var markFinal = false
    var modifyClassVisibility = false

    def setTargetAccess(name: String){
      if(name.startsWith("public")) targetAccess = Opcodes.ACC_PUBLIC
      else if(name.startsWith("private")) targetAccess = Opcodes.ACC_PRIVATE
      else if(name.startsWith("protected")) targetAccess = Opcodes.ACC_PROTECTED

      if(name.endsWith("-f")){
        changeFinal = true
        markFinal = false
      }else if(name.endsWith("+f")){
        changeFinal = true
        markFinal = true
      }
    }
  }

  def readConfig(path: String){
    val file = new File(path)
    val rulesResource = if(file.exists()){
        file.toURI.toURL
      }else{
        Resources.getResource(path)
      }
    processFile(Resources.asCharSource(rulesResource, Charsets.UTF_8))
    logger.info(s"Loaded ${modifiers.size()} rules from AccessTransformer config file $path")
  }

  private def processFile(resource: CharSource){
    val lines = resource.readLines()
    for(input <- lines){
      val line = Iterables.getFirst(Splitter.on('#').limit(2).split(input), "").trim
      if(line.length != 0){
        val parts = Lists.newArrayList(Splitter.on(' ').trimResults().split(line))
        if(parts.size() > 3){
          throw new RuntimeException("Illegal AT configfile line: " + input)
        }
        val m = new Modifier
        m.setTargetAccess(parts.get(0))

        if(parts.size() == 2) m.modifyClassVisibility = true
        else{
          val nameReference = parts.get(2)
          val parenIdx = nameReference.indexOf('(')
          if(parenIdx > 0){
            m.desc = nameReference.substring(parenIdx)
            m.name = nameReference.substring(0,parenIdx)
          }else{
            m.name = nameReference
          }
        }
        val className = parts.get(1).replace('/', '.')
        modifiers.put(className, m)
      }
    }
  }
}

class AccessTransformer extends IClassTransformer {

  override def transform(name: String, mappedName: String, bytes: Array[Byte]): Array[Byte] = {
    if(bytes == null) return null
    if(!AccessTransformer.modifiers.containsKey(mappedName)) return bytes

    val cnode = new ClassNode
    val creader = new ClassReader(bytes)
    creader.accept(cnode, 0)

    var stop = false
    for(m <- AccessTransformer.modifiers.get(mappedName) if !stop){
      if(m.modifyClassVisibility){
        cnode.access = modifyAccess(cnode.access, m)
        //continue
      }else if(m.desc.isEmpty){
        for(n <- cnode.fields){
          if(n.name == m.name || m.name == "*"){
            n.access = modifyAccess(n.access, m)
            if(m.name != "*") stop = true //break
          }
        }
      }else{
        val changeInvoke = Lists.newArrayList[MethodNode]()
        for(n <- cnode.methods){
          if((n.name == m.name && n.desc == m.desc) || m.name == "*"){
            n.access = modifyAccess(n.access, m)

            if(!n.name.equals("<init>")){
              //If we change a method from private to something else, we need to replace all INVOKESPECIAL to it with INVOKEVIRTUAL
              //Otherwise overridden methods won't be called.
              //We only need to scan this class for that, because the method was private before, and there are no external references to it
              val wasPrivate = (m.oldAccess & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE
              val isNowPrivate = (m.newAccess & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE

              if(wasPrivate && !isNowPrivate) changeInvoke.add(n)
            }

            if(m.name != "*") stop = true //break
          }
        }

        replaceInvokeSpecial(cnode, changeInvoke)
      }
    }
    val writer = new ClassWriter(ClassWriter.COMPUTE_MAXS)
    cnode.accept(writer)
    writer.toByteArray
  }

  private def replaceInvokeSpecial(cnode: ClassNode, toReplace: util.List[MethodNode]){
    for(method <- cnode.methods){
      val it = method.instructions.iterator()
      while(it.hasNext){
        val insn = it.next()
        if(insn.getOpcode == Opcodes.INVOKESPECIAL){
          val minsn = insn.asInstanceOf[MethodInsnNode]
          var stop = false
          for(n <- toReplace if !stop){
            if(n.name == minsn.name && n.desc == minsn.desc){
              minsn.setOpcode(Opcodes.INVOKEVIRTUAL)
              stop = true
            }
          }
        }
      }
    }
  }

  private def modifyAccess(access: Int, target: Modifier): Int = {
    target.oldAccess = access
    val t = target.targetAccess
    var ret = access & ~7

    access & 7 match {
      case Opcodes.ACC_PRIVATE => ret |= t
      case 0 => ret |= (if(t != Opcodes.ACC_PRIVATE) t else 0)
      case Opcodes.ACC_PROTECTED => ret |= (if(t != Opcodes.ACC_PRIVATE && t != 0) t else Opcodes.ACC_PROTECTED)
      case Opcodes.ACC_PUBLIC => ret |= (if(t != Opcodes.ACC_PRIVATE && t != 0 && t != Opcodes.ACC_PROTECTED) t else Opcodes.ACC_PUBLIC)
      case _ => throw new Error("Unknown access type (Should be impossible)") //Ehm... What?
    }

    if(target.changeFinal){
      if(target.markFinal) ret |= Opcodes.ACC_FINAL
      else ret &= ~Opcodes.ACC_FINAL
    }
    target.newAccess = ret
    ret
  }
}
