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

import jk_5.nailed.server.tweaker.remapping.{NailedRemappingAdapter, NameRemapper}
import net.minecraft.launchwrapper.{IClassNameTransformer, IClassTransformer}
import org.objectweb.asm.{ClassReader, ClassWriter}

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
