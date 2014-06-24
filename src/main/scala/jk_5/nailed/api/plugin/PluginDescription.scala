package jk_5.nailed.api.plugin

import java.io.File

import scala.collection.mutable

/**
 * No description given
 *
 * @author jk-5
 */
class PluginDescription {

  private var name: String = _
  private var main: String = _
  private var version: String = _
  private var author: String = _
  private var depends: mutable.HashSet[String] = _
  private var softDepends: mutable.HashSet[String] = _
  private var file: File = _
  private var description: String = _

  def setName(name: String) = this.name = name
  def setMain(main: String) = this.main = main
  def setVersion(version: String) = this.version = version
  def setAuthor(author: String) = this.author = author
  def setDepends(depends: mutable.HashSet[String]) = this.depends = depends
  def setSoftDepends(softDepends: mutable.HashSet[String]) = this.softDepends = softDepends
  def setFile(file: File) = this.file = file
  def setDescription(description: String) = this.description = description

  def getName = this.name
  def getMain = this.main
  def getVersion = this.version
  def getAuthor = this.author
  def getDepends = this.depends
  def getSoftDepends = this.softDepends
  def getFile = this.file
  def getDescription = this.description
}
