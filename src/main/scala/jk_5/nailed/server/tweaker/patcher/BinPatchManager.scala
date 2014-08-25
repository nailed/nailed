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

package jk_5.nailed.server.tweaker.patcher

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, IOException}
import java.util.jar.{JarEntry, JarInputStream, JarOutputStream, Pack200}
import java.util.regex.Pattern

import LZMA.LzmaInputStream
import com.google.common.collect.{ArrayListMultimap, ListMultimap}
import com.google.common.hash.Hashing
import com.google.common.io.{ByteArrayDataInput, ByteStreams, Files}
import com.nothome.delta.GDiffPatcher
import net.minecraft.launchwrapper.LaunchClassLoader
import org.apache.logging.log4j.LogManager

import scala.collection.convert.wrapAsScala._
import scala.collection.mutable
import scala.util.Properties

/**
 * No description given
 *
 * @author jk-5
 */
object BinPatchManager {

  private final val dumpPatched = Properties.propOrFalse("nailed.dumpPatchedClasses")
  private final val ignorePatchDiscrepancies = Properties.propOrFalse("nailed.ignorePatchDiscrepancies")

  private final val patcher = new GDiffPatcher
  private var patches: ListMultimap[String, ClassPatch] = _
  private val cache = mutable.HashMap[String, Array[Byte]]()
  private var tempDir: File = _
  private val logger = LogManager.getLogger

  if(dumpPatched){
    tempDir = Files.createTempDir()
    logger.info("Dumping patched classes to {}", tempDir.getAbsolutePath)
  }

  def getPatchedResource(name: String, mappedName: String, loader: LaunchClassLoader): Array[Byte] = {
    applyPatch(name, mappedName, loader.getClassBytes(name))
  }

  def applyPatch(name: String, mappedName: String, inputData: Array[Byte]): Array[Byte] = {
    var input = inputData
    if(this.patches == null) return input
    val _p = this.cache.get(name)
    if(_p.isDefined) return _p.get
    val list = this.patches.get(name)
    if(list.isEmpty) return input

    var ignoredError = false
    var stop = false
    for(patch <- list){
      stop = false
      //TODO: are these comparisions right?
      if(patch.targetClassName != mappedName && patch.sourceClassName != name){
        logger.warn("Binary patch found {} for wrong class {}", patch.targetClassName, mappedName)
      }
      if(!patch.existsAtTarget && (input == null && input.length == 0)){
        input = new Array[Byte](0)
      }else if(!patch.existsAtTarget){
        logger.warn("Patcher expecting empty class data file for {}, but found non-empty", patch.targetClassName)
      }else{
        val inputChecksum = Hashing.adler32().hashBytes(input).asInt()
        if(patch.inputChecksum != inputChecksum){
          logger.fatal("There is a binary discrepency between the expected input class {} ({}) and the actual class. Checksum on disk is {}, in patch {}.", mappedName, name, inputChecksum.toString, patch.inputChecksum.toString)
          if(!ignorePatchDiscrepancies){
            logger.fatal("Server is shutting down now! (You can try doing -Dnailed.ignorePatchDiscrepancies=true to ignore this error)")
            System.exit(1)
          }else{
            logger.warn("We are going to ignore this error. Chances are that the server won't be able to load properly")
            ignoredError = true
            stop = true
          }
        }
      }
      if(!stop){
        patcher.synchronized{
          try{
            input = patcher.patch(input, patch.patch)
          }catch{
            case e: IOException =>
              logger.error("Encountered a problem while runtime patching class " + name, e)
              stop = true
          }
        }
      }
    }
    if(dumpPatched){
      try{
        Files.write(input, new File(tempDir,mappedName))
      }catch{
        case e: IOException =>
          logger.error("Failed to write patched class " + mappedName + " to " + tempDir.getAbsolutePath, e)
      }
    }
    cache.put(name, input)
    input
  }

  def setup(){
    logger.info("Loading binary patches...")
    val binpatchMatcher = Pattern.compile("binpatch/server/.*.binpatch")
    var jis: JarInputStream = null
    try{
      val compressed = this.getClass.getResourceAsStream("/binpatches.pack.lzma")
      if(compressed == null){
        logger.warn("Was not able to find binary patches. Assuming development environment")
        return
      }
      val decompressed = new LzmaInputStream(compressed)
      val jarBytes = new ByteArrayOutputStream
      val jos = new JarOutputStream(jarBytes)
      Pack200.newUnpacker().unpack(decompressed, jos)
      jis = new JarInputStream(new ByteArrayInputStream(jarBytes.toByteArray))
    }catch{
      case e: Exception =>
        logger.error("Error occurred while reading binary patches", e)
    }
    this.patches = ArrayListMultimap.create()

    var stop = false
    while(!stop){
      try{
        val entry = jis.getNextJarEntry
        if(entry == null){
          stop = true
        }else{
          if(binpatchMatcher.matcher(entry.getName).matches()){
            val patch = readPatch(entry, jis)
            if(patch != null){
              patches.put(patch.sourceClassName, patch)
            }
          }else jis.closeEntry()
        }
      }catch{
        case e: IOException => //Meh
      }
    }
    logger.info("Successfully loaded {} binary patches", patches.size().toString)
    cache.clear()
  }

  private def readPatch(entry: JarEntry, jis: JarInputStream): ClassPatch = {
    var input: ByteArrayDataInput = null
    try{
      input = ByteStreams.newDataInput(ByteStreams.toByteArray(jis))
    }catch{
      case e: IOException =>
        logger.warn("Unable to read binpatch file {}. Ignoring it", entry.getName)
        return null
    }
    val name = input.readUTF()
    val sourceName = input.readUTF()
    val targetName = input.readUTF()
    val exists = input.readBoolean()
    val inputChecksum = if(exists) input.readInt else 0
    val patchLength = input.readInt()
    val patchBytes = new Array[Byte](patchLength)
    input.readFully(patchBytes)

    new ClassPatch(name, sourceName, targetName, exists, inputChecksum, patchBytes)
  }
}
