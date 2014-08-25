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

package jk_5.nailed.jstest

import java.io.{File, FileReader}

import org.apache.logging.log4j.LogManager
import org.mozilla.javascript._

/**
 * No description given
 *
 * @author jk-5
 */
object JavascriptTest {

  val logger = LogManager.getLogger

  def main(args: Array[String]) {
    val context = Context.enter()
    val scope = context.initStandardObjects()
    val libraryScope: ScriptableObject = new NativeObject
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
    scope.get("eval")

    /*context.compileString(
      """
        |module = {};
        |module.exports = {};
      """.stripMargin, "bios.js", 0, null).exec(context, libraryScope)*/

    libraryScope.defineProperty("module", new ScriptableObject() {
      override def getClassName = "LibraryModule"
      this.defineProperty("exports", new NativeObject, 0)
    }, ScriptableObject.DONTENUM | ScriptableObject.READONLY | ScriptableObject.PERMANENT)

    scope.defineProperty("log", new BaseFunction{
      override def call(cx: Context, scope: Scriptable, thisObj: Scriptable, args: Array[AnyRef]): AnyRef = {
        if(args.length == 1){
          println("[" + Thread.currentThread().getName + "] [INFO]: " + args(0))
          java.lang.Boolean.TRUE
        }else{
          java.lang.Boolean.FALSE
        }
      }
    }, ScriptableObject.DONTENUM)

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
        val libScope = new NativeObject
        libScope.setParentScope(libraryScope)
        val s = context.compileReader(new FileReader(new File("js/" + args(0).toString)), args(0).toString, 0, null)
        s.exec(context, libScope)
      }
    }, ScriptableObject.DONTENUM)



    val scr = context.compileReader(new FileReader(new File("js/test1.js")), "test1.js", 0, null)

    val result = scr.exec(context, scope)
    println(Context.toString(result))
  }
}
