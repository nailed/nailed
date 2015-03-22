package jk_5.nailed.server.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public class NBTUtils {

    public static void addLore(ItemStack is, String ... lore){
        NBTTagCompound tag = getItemNBT(is);
        NBTTagCompound display = getOrCreateTagCompound(tag, "display");

        NBTTagList loreTag = new NBTTagList();
        for (String l : lore) {
            loreTag.appendTag(new NBTTagString(l));
        }
        display.setTag("Lore", loreTag);
    }

    public static void setDisplayName(ItemStack is, String name){
        NBTTagCompound tag = getItemNBT(is);
        NBTTagCompound display = getOrCreateTagCompound(tag, "display");
        if(display.hasKey("Name")){
            display.removeTag("Name");
        }
        display.setString("Name", name);
    }

    public static NBTTagCompound getItemNBT(ItemStack is){
        if(is.hasTagCompound()){
            return is.getTagCompound();
        }
        NBTTagCompound tag = new NBTTagCompound();
        is.setTagCompound(tag);
        return tag;
    }

    public static NBTTagCompound getOrCreateTagCompound(NBTTagCompound base, String name){
        if(base.hasKey(name)){
            return base.getCompoundTag(name);
        }else{
            NBTTagCompound t = new NBTTagCompound();
            base.setTag(name, t);
            return t;
        }
    }
}
