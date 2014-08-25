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

package jk_5.nailed.server.tweaker.remapping

import org.objectweb.asm.commons.{Remapper, RemappingClassAdapter, RemappingMethodAdapter}
import org.objectweb.asm.{ClassVisitor, MethodVisitor, Opcodes}

/**
 * No description given
 *
 * @author jk-5
 */
class NailedRemappingAdapter(visitor: ClassVisitor) extends RemappingClassAdapter(visitor, NameRemapper) {
  
  override def visit(version: Int, access: Int, name: String, signature: String, superName: String, interfaces: Array[String]){
    val i = if(interfaces == null) new Array[String](0) else interfaces
    NameRemapper.mergeSuperMaps(name, superName, interfaces)
    super.visit(version, access, name, signature, superName, interfaces)
  }

  override protected def createRemappingMethodAdapter(access: Int, desc: String, mv: MethodVisitor): MethodVisitor = {
    new StaticFixingMethodVisitor(access, desc, mv, remapper)
  }

  class StaticFixingMethodVisitor(access: Int, desc: String, mv: MethodVisitor, remapper: Remapper) extends RemappingMethodAdapter(access, desc, mv, remapper) {
    
    override def visitFieldInsn(opcode: Int, originalType: String, originalName: String, desc: String){
      //This MethodVisitor solves the problem of a static field reference changing type.
      //Chances are big that this change is compatible, however we need to fix up the descriptor to point at the new type
      val typ = remapper.mapType(originalType)
      val fieldName = remapper.mapFieldName(originalType, originalName, desc)
      var newDesc = remapper.mapDesc(desc)
      if(opcode == Opcodes.GETSTATIC && typ.startsWith("net/minecraft/") && newDesc.startsWith("Lnet/minecraft/")){
        val replDesc = NameRemapper.getStaticFieldType(originalType, originalName, typ, fieldName)
        if(replDesc != null){
          newDesc = remapper.mapDesc(replDesc)
        }
      }
      //super.super
      if(mv != null){
        mv.visitFieldInsn(opcode, typ, fieldName, newDesc)
      }
    }
  }
}
