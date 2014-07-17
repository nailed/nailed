package jk_5.nailed.server.tweaker.transformer

import jk_5.nailed.server.tweaker.patcher.BinPatchManager
import net.minecraft.launchwrapper.IClassTransformer

/**
 * No description given
 *
 * @author jk-5
 */
class PatchingTransformer extends IClassTransformer {
  override def transform(name: String, mappedName: String, bytes: Array[Byte]): Array[Byte] = BinPatchManager.applyPatch(name, mappedName, bytes)
}
