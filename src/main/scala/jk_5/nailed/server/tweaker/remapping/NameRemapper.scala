package jk_5.nailed.server.tweaker.remapping

import java.io.{BufferedReader, IOException, InputStreamReader}

import LZMA.LzmaInputStream
import com.google.common.base.{CharMatcher, Splitter, Strings}
import com.google.common.collect.ImmutableBiMap.Builder
import com.google.common.collect.{BiMap, ImmutableBiMap, Iterables}
import jk_5.nailed.server.tweaker.NailedVersion
import jk_5.nailed.server.tweaker.patcher.BinPatchManager
import net.minecraft.launchwrapper.Launch
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.ClassReader
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.tree.ClassNode

import scala.collection.convert.wrapAsScala._
import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object NameRemapper extends Remapper {

  private val logger = LogManager.getLogger

  private lazy val classLoader = Launch.classLoader

  private var classNameMap: BiMap[String, String] = ImmutableBiMap.of()

  private var rawFieldMaps: mutable.HashMap[String, mutable.HashMap[String, String]] = _
  private var rawMethodMaps: mutable.HashMap[String, mutable.HashMap[String, String]] = _
  private var fieldNameMaps: mutable.HashMap[String, mutable.HashMap[String, String]] = _
  private var methodNameMaps: mutable.HashMap[String, mutable.HashMap[String, String]] = _
  private val fieldDescriptors = mutable.HashMap[String, mutable.HashMap[String, String]]()

  // Cache null values so we don't waste time trying to recompute classes with no field or method maps
  private val negativeCacheMethods = mutable.HashSet[String]()
  private val negativeCacheFields = mutable.HashSet[String]()

  def init(){
    logger.info("Loading deobfuscation data...")
    val data = this.getClass.getResourceAsStream("/deobfuscation_data-" + NailedVersion.mcversion + ".lzma")
    if(data == null){
      logger.warn("Was not able to find deobfuscation data. Assuming development environment")
      return
    }
    try{
      val stream = new BufferedReader(new InputStreamReader(new LzmaInputStream(data)))
      var line = stream.readLine()
      val splitter = Splitter.on(CharMatcher.anyOf(": ")).omitEmptyStrings().trimResults()
      val builder = ImmutableBiMap.builder[String, String]()
      rawFieldMaps = mutable.HashMap[String, mutable.HashMap[String, String]]()
      rawMethodMaps = mutable.HashMap[String, mutable.HashMap[String, String]]()
      while(line != null){
        val parts = Iterables.toArray(splitter.split(line), classOf[String])
        parts(0) match {
          case "MD" => parseMethod(parts)
          case "FD" => parseField(parts)
          case "CL" => parseClass(builder, parts)
          case _ =>
        }
        line = stream.readLine()
      }
      classNameMap = builder.build()
    }catch{
      case e: IOException =>
        logger.error("An error has occurred while loading the deobfuscation data", e)
    }finally{
      data.close()
      methodNameMaps = mutable.HashMap[String, mutable.HashMap[String, String]]()
      fieldNameMaps = mutable.HashMap[String, mutable.HashMap[String, String]]()
    }
  }

  private def parseField(parts: Array[String]){
    val oldSrg = parts(1)
    val lastOld = oldSrg.lastIndexOf('/')
    val cl = oldSrg.substring(0, lastOld)
    val oldName = oldSrg.substring(lastOld + 1)
    val newSrg = parts(2)
    val lastNew = newSrg.lastIndexOf('/')
    val newName = newSrg.substring(lastNew + 1)
    if(!rawFieldMaps.contains(cl)){
      rawFieldMaps.put(cl, mutable.HashMap[String, String]())
    }
    rawFieldMaps.get(cl).get.put(oldName + ":" + getFieldType(cl, oldName), newName)
    rawFieldMaps.get(cl).get.put(oldName + ":null", newName)
  }

  private def getFieldType(owner: String, name: String): String = {
    if(this.fieldDescriptors.contains(owner)){
      return fieldDescriptors.get(owner).get.get("name").orNull
    }
    this.fieldDescriptors.synchronized{
      try{
        val bytes = BinPatchManager.getPatchedResource(owner, map(owner).replace('/', '.'), classLoader)
        if(bytes == null) return null
        val cr = new ClassReader(bytes)
        val cnode = new ClassNode
        cr.accept(cnode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES)
        val resMap = mutable.HashMap[String, String]()
        for(node <- cnode.fields){
          resMap.put(node.name, node.desc)
        }
        fieldDescriptors.put(owner, resMap)
        return resMap.get(name).orNull
      }catch{
        case e: IOException => logger.error("An exception occured while reading class file " + owner, e)
      }
      null
    }
  }

  private def parseClass(builder: Builder[String, String], parts: Array[String]){
    builder.put(parts(1), parts(2))
  }

  private def parseMethod(parts: Array[String]){
    val oldSrg = parts(1)
    val lastOld = oldSrg.lastIndexOf('/')
    val cl = oldSrg.substring(0, lastOld)
    val oldName = oldSrg.substring(lastOld + 1)
    val sig = parts(2)
    val newSrg = parts(3)
    val lastNew = newSrg.lastIndexOf('/')
    val newName = newSrg.substring(lastNew + 1)
    if(!rawMethodMaps.contains(cl)){
      rawMethodMaps.put(cl, mutable.HashMap[String, String]())
    }
    rawMethodMaps.get(cl).get.put(oldName + sig, newName)
  }

  override def mapFieldName(owner: String, name: String, desc: String): String = {
    if(classNameMap == null || classNameMap.isEmpty) return name
    val fieldMap = this.getFieldMap(owner)
    if(fieldMap != null && fieldMap.contains(name + ":" + desc)){
      fieldMap.get(name + ":" + desc).get
    }else{
      name
    }
  }

  override def map(typeName: String): String = {
    if(classNameMap == null || classNameMap.isEmpty) return typeName
    if(classNameMap.containsKey(typeName)) return classNameMap.get(typeName)
    val dollarIdx = typeName.lastIndexOf('$')
    if(dollarIdx > -1) return map(typeName.substring(0, dollarIdx)) + "$" + typeName.substring(dollarIdx + 1)
    typeName
  }

  def unmap(typeName: String): String = {
    if(classNameMap == null || classNameMap.isEmpty) return typeName
    if(classNameMap.containsValue(typeName)) return classNameMap.inverse().get(typeName)
    val dollarIdx = typeName.lastIndexOf('$')
    if(dollarIdx > -1) return unmap(typeName.substring(0, dollarIdx)) + "$" + typeName.substring(dollarIdx + 1)
    typeName
  }

  override def mapMethodName(owner: String, name: String, desc: String): String = {
    if(classNameMap == null || classNameMap.isEmpty) return name
    val methodMap = this.getMethodMap(owner)
    val methodDesc = name + desc
    if(methodMap != null && methodMap.contains(methodDesc)){
      methodMap.get(methodDesc).get
    }else{
      name
    }
  }

  private def getFieldMap(className: String): mutable.HashMap[String, String] = {
    if(!this.fieldNameMaps.contains(className) && !negativeCacheFields.contains(className)){
      findAndMergeSuperMaps(className)
      if(!fieldNameMaps.contains(className)){
        negativeCacheFields += className
        return null
      }
    }
    fieldNameMaps.get(className).orNull
  }

  private def getMethodMap(className: String): mutable.HashMap[String, String] = {
    if(!this.methodNameMaps.contains(className) && !negativeCacheMethods.contains(className)){
      findAndMergeSuperMaps(className)
      if(!methodNameMaps.contains(className)){
        negativeCacheMethods += className
        return null
      }
    }
    methodNameMaps.get(className).orNull
  }

  private def findAndMergeSuperMaps(name: String){
    try{
      var superName: String = null
      var interfaces = new Array[String](0)
      val bytes = BinPatchManager.getPatchedResource(name, map(name), classLoader)
      if(bytes != null){
        val cr = new ClassReader(bytes)
        superName = cr.getSuperName
        interfaces = cr.getInterfaces
      }
      mergeSuperMaps(name, superName, interfaces)
    }catch{
      case e: IOException => logger.error("An exception has occurred while finding super maps", e)
    }
  }

  def mergeSuperMaps(name: String, superName: String, interfaces: Array[String]){
    if(classNameMap == null || classNameMap.isEmpty) return
    if(Strings.isNullOrEmpty(superName)) return
    val allParents = {
      val b = mutable.ArrayBuffer[String]()
      b += superName
      b ++= interfaces
      b.toSeq
    }
    for(parent <- allParents.filter(!methodNameMaps.contains(_))){
      findAndMergeSuperMaps(parent)
    }

    val methodMap = mutable.HashMap[String, String]()
    val fieldMap = mutable.HashMap[String, String]()

    for(parent <- allParents){
      if(methodNameMaps.contains(parent)){
        methodMap ++= methodNameMaps.get(parent).get
      }
      if(fieldNameMaps.contains(parent)){
        fieldMap ++= fieldNameMaps.get(parent).get
      }
    }

    if(rawMethodMaps.contains(name)){
      methodMap ++= rawMethodMaps.get(name).get
    }
    if(rawFieldMaps.contains(name)){
      fieldMap ++= rawFieldMaps.get(name).get
    }
    methodNameMaps.put(name, methodMap)
    fieldNameMaps.put(name, fieldMap)
  }

  def getObfuscatedClasses = classNameMap.keySet()

  def getStaticFieldType(oldType: String, oldName: String, newType: String, newName: String): String = {
    val ftype = this.getFieldType(oldType, oldName)
    if(oldType == newType) return ftype
    var newClassMap = this.fieldDescriptors.get(newType)
    if(newClassMap.isEmpty){
      newClassMap = Some(mutable.HashMap[String, String]())
      fieldDescriptors.put(newType, newClassMap.get)
    }
    newClassMap.get.put(newName, ftype)
    ftype
  }

  def isRemappedClass(name: String) = map(name) != name
}
