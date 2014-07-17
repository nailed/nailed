package jk_5.nailed.server.tweaker.remapping

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.commons.RemappingClassAdapter

/**
 * No description given
 *
 * @author jk-5
 */
class NailedRemappingAdapter(visitor: ClassVisitor) extends RemappingClassAdapter(visitor, NameRemapper) {

}
