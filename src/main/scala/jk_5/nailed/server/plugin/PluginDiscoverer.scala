package jk_5.nailed.server.plugin

import java.io._
import java.util.jar.JarFile
import javassist.bytecode._
import javassist.bytecode.annotation.StringMemberValue

import net.minecraft.launchwrapper.Launch
import org.apache.logging.log4j.LogManager

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
object PluginDiscoverer {

  private val logger = LogManager.getLogger

  def discoverClasspathPlugins(){
    logger.info("Discovering plugins from the classpath...")

    val discovered = mutable.ArrayBuffer[DiscoveredPlugin]()
    val jars = Launch.classLoader.getURLs
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
                analizePotentialPlugins(fis, discovered)
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
        val jar = new JarFile(file)
        val jarName = jar.getName.toLowerCase
        if(!jarName.contains("jre") && !jarName.contains("jdk") && !jarName.contains("/rt.jar")){
          logger.debug(s"Examining jar file ${jar.getName} for potential plugins")
          val entries = jar.entries()
          while(entries.hasMoreElements){
            val entry = entries.nextElement()
            if(entry != null && entry.getName.endsWith(".class")) { //Filter out junk we don't need. We only need class files
              analizePotentialPlugins(jar.getInputStream(entry), discovered)
            }
          }
        }else{
          logger.debug(s"Ignoring JRE jar ${jar.getName}")
        }
      }
    }

    logger.info(s"Discovered ${discovered.size} plugins")
    discovered.foreach(d => logger.trace(s"  - ${d.id} (${d.name} version: ${d.version}) -> ${d.className}"))
  }

  private def analizePotentialPlugins(input: InputStream, discovered: mutable.ArrayBuffer[DiscoveredPlugin]){
    val in = input match{ case i: DataInputStream => i; case i => new DataInputStream(i) }
    val classFile = new ClassFile(in)

    val annotations = classFile.getAttribute(AnnotationsAttribute.visibleTag).asInstanceOf[AnnotationsAttribute]

    for(annotation <- annotations.getAnnotations){
      val annName = annotation.getTypeName
      if(annName == "jk_5.nailed.api.plugin.Plugin"){
        val id = annotation.getMemberValue("id").asInstanceOf[StringMemberValue].getValue
        val name = annotation.getMemberValue("name").asInstanceOf[StringMemberValue].getValue
        val version = annotation.getMemberValue("version").asInstanceOf[StringMemberValue].getValue
        discovered += new DiscoveredPlugin(classFile.getName, id, name, version)
      }
    }
  }

  private case class DiscoveredPlugin(
    className: String,
    id: String,
    name: String,
    version: String
  )
}
