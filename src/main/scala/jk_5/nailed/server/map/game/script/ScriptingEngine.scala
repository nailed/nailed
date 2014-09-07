package jk_5.nailed.server.map.game.script

import java.io.InputStreamReader

import jk_5.nailed.api.chat._
import jk_5.nailed.server.map.game.NailedGameManager
import org.apache.logging.log4j.LogManager
import org.mozilla.javascript
import org.mozilla.javascript._

/**
 * No description given
 *
 * @author jk-5
 */
object ScriptingEngine {

  def getScopes(engine: ScriptingEngine): (Context, ScriptableObject, ScriptableObject) = {

    val context = Context.enter()
    val scope = context.initStandardObjects()
    val libraryScope = new NativeObject
    libraryScope.setParentScope(scope)

    scope.delete("Packages")
    scope.delete("getClass")
    scope.delete("JavaAdapter")
    scope.delete("JavaImporter")
    scope.delete("Continuation")
    scope.delete("java")
    scope.delete("javax")
    scope.delete("org")
    scope.delete("com")
    scope.delete("edu")
    scope.delete("net")
    scope.delete("eval")

    scope.put("map", scope, new javascript.NativeJavaObject(scope, new ScriptMapApi(engine.manager.map), classOf[ScriptMapApi]))
    scope.put("sleep", scope, new NativeJavaMethod(classOf[Thread].getDeclaredMethod("sleep", java.lang.Long.TYPE), "sleep"))
    scope.put("BaseComponent", scope, new NativeJavaClass(scope, classOf[BaseComponent]))
    scope.put("ChatColor", scope, new NativeJavaClass(scope, classOf[ChatColor]))
    scope.put("ClickEvent", scope, new NativeJavaClass(scope, classOf[ClickEvent]))
    scope.put("ComponentBuilder", scope, new NativeJavaClass(scope, classOf[ComponentBuilder]))
    scope.put("HoverEvent", scope, new NativeJavaClass(scope, classOf[HoverEvent]))
    scope.put("TextComponent", scope, new NativeJavaClass(scope, classOf[TextComponent]))
    scope.put("TranslatableComponent", scope, new NativeJavaClass(scope, classOf[TranslatableComponent]))

    libraryScope.defineProperty("module", new ScriptableObject() {
      override def getClassName = "LibraryModule"
      this.defineProperty("exports", new NativeObject, 0)
    }, ScriptableObject.DONTENUM | ScriptableObject.READONLY | ScriptableObject.PERMANENT)

    scope.defineProperty("onevent", new BaseFunction{
      override def call(cx: Context, scope: Scriptable, thisObj: Scriptable, args: Array[AnyRef]): AnyRef = {
        if(args.length == 2){
          val n = args(0).asInstanceOf[String]
          val action = args(1).asInstanceOf[Function]
          action.call(cx, scope, thisObj, Array[AnyRef](n))
        }
        java.lang.Boolean.TRUE
      }
    }, ScriptableObject.DONTENUM)

    scope.defineProperty("require", new BaseFunction{
      override def call(cx: Context, scope: Scriptable, thisObj: Scriptable, args: Array[AnyRef]): AnyRef = {
        val stream = engine.fileSystem.openAsInputStream(args(0).toString)
        val libScope = new NativeObject
        libScope.setParentScope(libraryScope)
        var s: Script = null
        try{
          s = context.compileReader(new InputStreamReader(stream), args(0).toString, 0, null)
        }finally{
          stream.close()
        }
        s.exec(context, libScope)
      }
    }, ScriptableObject.DONTENUM)

    (context, scope, libraryScope)
  }
}
class ScriptingEngine(val manager: NailedGameManager) {

  val logger = LogManager.getLogger
  var fileSystem: FileSystem = null
  var context: Context = null
  var scope: ScriptableObject = null
  var libraryScope: ScriptableObject = null
  var script: Script = null
  var thread: Thread = null

  def start(): Boolean = {
    if(fileSystem != null){
      fileSystem.unload()
      fileSystem = new FileSystem
    }else fileSystem = new FileSystem

    logger.info("Initializing ScriptingEngine for " + manager.map.toString)
    fileSystem.mount("", manager.map.mappack.getMappackMount)

    if(!fileSystem.exists("game.js")){
      return false
    }

    val gameScript = fileSystem.openAsInputStream("game.js")
    if(gameScript == null){
      return false
    }

    thread = newThread(new Runnable {
      override def run(){
        val s = ScriptingEngine.getScopes(ScriptingEngine.this)
        context = s._1
        scope = s._2
        libraryScope = s._3
        script = context.compileReader(new InputStreamReader(gameScript), "game.js", 0, null)
        gameScript.close()
        var success = false
        try{
          script.exec(context, scope)
          success = true
        }catch{
          case e: Exception =>
            val map = manager.map
            map.broadcastChatMessage(new ComponentBuilder("The script engine has crashed. The game will be stopped").color(ChatColor.RED).create())
            logger.fatal("Exception while executing game script. Script engine crashed", e)
        }finally{
          manager.onEnded(success)
        }
      }
    })
    thread.start()
    true
  }

  def kill(){
    thread.stop()
    fileSystem.unload()
    fileSystem = null
  }

  private def newThread(r: Runnable): Thread = {
    val t = new Thread(r)
    t.setName("ScriptEngine-" + manager.map.id + (if(manager.map.mappack != null) "-" + manager.map.mappack.getId else ""))
    t.setDaemon(true)
    t.setPriority(3)
    t
  }
}
