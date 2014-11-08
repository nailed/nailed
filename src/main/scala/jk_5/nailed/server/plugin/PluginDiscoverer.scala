package jk_5.nailed.server.plugin

import java.io._
import java.net.URLClassLoader
import java.util.jar.JarFile
import javassist.bytecode._
import javassist.bytecode.annotation.StringMemberValue

import org.apache.logging.log4j.LogManager

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object PluginDiscoverer {

  private val logger = LogManager.getLogger
  private var discovered = mutable.HashSet[DiscoveredPlugin]()

  @inline def clearDiscovered() = discovered = mutable.HashSet[DiscoveredPlugin]()
  def getDiscovered = discovered

  //Generalize the file searching thing from below and use it in the jar loader

  def discoverJarPlugins(pluginsDir: File){
    logger.info("Discovering plugins from the plugins folder...")
    for(file <- pluginsDir.listFiles(new FilenameFilter {
      override def accept(dir: File, name: String) = name.endsWith(".jar")
    })){
      readJarFile(file, discovered)
    }
  }

  def discoverClasspathPlugins(){
    logger.info("Discovering plugins from the classpath...")

    val jars = this.getClass.getClassLoader.asInstanceOf[URLClassLoader].getURLs
    for(jarUrl <- jars){
      val file = new File(jarUrl.toURI)
      if(file.isDirectory){
        logger.debug(s"Examining classPath directory $file for potential plugins")

        def recurseChildren(fi: File) {
          for(f <- fi.listFiles()) {
            if(f.isFile && f.getName.endsWith(".class")) {
              var fis: FileInputStream = null
              try{
                fis = new FileInputStream(f)
                analizePotentialPlugins(fis, discovered, true, null)
              }finally{
                if(fis != null) try{fis.close()}catch{case e: Exception => /* Ignore */}
              }
            }else if(f.isDirectory) {
              recurseChildren(f)
            }
          }
        }

        recurseChildren(file)
      }else if(file.isFile){
        readJarFile(file, discovered)
      }
    }

    logger.info(s"Discovered ${discovered.size} plugins")
    discovered.foreach(d => logger.trace(s"  - ${d.id} (${d.name} version: ${d.version}) -> ${d.className}"))
  }

  private def readJarFile(file: File, discovered: mutable.HashSet[DiscoveredPlugin]){
    val jar = new JarFile(file)
    val jarName = jar.getName.toLowerCase
    if(!jarName.contains("jre") && !jarName.contains("jdk") && !jarName.contains("/rt.jar")){
      logger.debug(s"Examining jar file ${jar.getName} for potential plugins")
      val entries = jar.entries()
      while(entries.hasMoreElements){
        val entry = entries.nextElement()
        if(entry != null && entry.getName.endsWith(".class")) { //Filter out junk we don't need. We only need class files
          analizePotentialPlugins(jar.getInputStream(entry), discovered, false, file)
        }
      }
    }else{
      logger.debug(s"Ignoring JRE jar ${jar.getName}")
    }
  }

  private def analizePotentialPlugins(input: InputStream, discovered: mutable.HashSet[DiscoveredPlugin], isClasspath: Boolean, file: File){
    val in = input match{ case i: DataInputStream => i; case i => new DataInputStream(i) }
    val classFile = new ClassFile(in)

    val annotations = classFile.getAttribute(AnnotationsAttribute.visibleTag).asInstanceOf[AnnotationsAttribute]
    if(annotations == null) return
    for(annotation <- annotations.getAnnotations){
      val annName = annotation.getTypeName
      if(annName == "jk_5.nailed.api.plugin.Plugin"){
        val idValue = annotation.getMemberValue("id").asInstanceOf[StringMemberValue]
        val id = if(idValue == null) null else idValue.getValue
        val nameValue = annotation.getMemberValue("name").asInstanceOf[StringMemberValue]
        val name = if(nameValue == null) null else nameValue.getValue
        val versionValue = annotation.getMemberValue("version").asInstanceOf[StringMemberValue]
        val version = if(versionValue == null) "unknown" else versionValue.getValue
        discovered += new DiscoveredPlugin(classFile.getName, id, name, version, isClasspath, file)
      }
    }
  }

  case class DiscoveredPlugin(
    className: String,
    id: String,
    name: String,
    version: String,
    isClasspath: Boolean,
    file: File
  )
}
