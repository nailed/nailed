package jk_5.nailed.server.tweaker.patcher

/**
 * No description given
 *
 * @author jk-5
 */
case class ClassPatch(
  name: String,
  sourceClassName: String,
  targetClassName: String,
  existsAtTarget: Boolean,
  inputChecksum: Int,
  patch: Array[Byte]
)
