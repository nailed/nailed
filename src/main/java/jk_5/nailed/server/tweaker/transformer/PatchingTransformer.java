package jk_5.nailed.server.tweaker.transformer;

import jk_5.nailed.server.tweaker.patcher.BinPatchManager;
import net.minecraft.launchwrapper.IClassTransformer;

public class PatchingTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String mappedName, byte[] bytes) {
        return BinPatchManager.applyPatch(name, mappedName, bytes);
    }
}
