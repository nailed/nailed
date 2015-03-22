package jk_5.nailed.server.utils;

import jk_5.nailed.api.item.ItemStack;
import jk_5.nailed.api.item.Material;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemStackConverter {

    public ItemStack toNailed(net.minecraft.item.ItemStack is){
        if(is == null){
            return null;
        }
        ItemStack ret = new ItemStack(Material.getMaterial(Item.itemRegistry.getIDForObject(is.getItem())), is.stackSize, (short) is.getMetadata());
        NBTTagCompound tag = is.getTagCompound();
        if(tag != null){
            //noinspection unchecked
            for (String t : ((Set<String>) tag.getKeySet())) {
                if(tag.getTag(t) instanceof NBTTagString){
                    ret.setTag(t, ((NBTTagString) tag.getTag(t)).getString());
                }
            }
            if(tag.hasKey("display")){
                NBTTagCompound disp = tag.getCompoundTag("display");
                if(disp.hasKey("Name")){
                    ret.setDisplayName(disp.getString("Name"));
                }
                if(disp.hasKey("Lore")){
                    NBTTagList list = disp.getTagList("Lore", 8);
                    for(int i = 0; i < list.tagCount(); i++){
                        ret.addLore(list.getStringTagAt(i));
                    }
                }
            }
        }
        return ret;
    }

    public net.minecraft.item.ItemStack toVanilla(ItemStack is){
        if(is == null){
            return null;
        }
        net.minecraft.item.ItemStack ret = new net.minecraft.item.ItemStack(((Item) Item.itemRegistry.getObjectById(is.getMaterial().getLegacyId())), is.getAmount(), is.getDamage());
        if(is.getDisplayName() != null){
            NBTUtils.setDisplayName(ret, is.getDisplayName());
        }
        List<String> lore = is.getLore();
        NBTUtils.addLore(ret, lore.toArray(new String[lore.size()]));
        NBTTagCompound nbt = NBTUtils.getItemNBT(ret);
        for (Map.Entry<String, String> entry : is.getTags().entrySet()) {
            nbt.setString(entry.getKey(), entry.getValue());
        }
        return ret;
    }
}
