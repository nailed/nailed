package jk_5.nailed.server.utils;

import jk_5.nailed.api.gamerule.GameRuleKey;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.world.Difficulty;
import jk_5.nailed.api.world.World;
import jk_5.nailed.server.NailedPlatform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;

public class NailedFoodStats extends FoodStats {

    private int foodLevel = 20;
    private float saturation = 5F;
    private float exhaustion = 0F;
    private int timer = 0;

    @Override
    public void addStats(int food, float saturation) {
        foodLevel = Math.min(food + foodLevel, 20);
        this.saturation = Math.min(this.saturation + food * saturation * 2.0F, foodLevel);
    }

    @Override
    public void addStats(ItemFood foodItem, ItemStack is) {
        this.addStats(foodItem.getHealAmount(is), foodItem.getSaturationModifier(is));
    }

    @Override
    public void onUpdate(EntityPlayer playerEntity) {
        Player player = NailedPlatform.instance().getPlayerFromEntity(((EntityPlayerMP) playerEntity));
        World world = player.getWorld();

        if(world.getConfig().disableFood()){
            foodLevel = 20;
            return;
        }

        Difficulty difficulty = world.getDifficulty();

        if(exhaustion > 4){
            exhaustion -= 4;
            if(saturation > 0){
                saturation = Math.max(saturation - 1, 0);
            }else if(difficulty != Difficulty.PEACEFUL){
                foodLevel = Math.max(foodLevel - 1, 0);
            }
        }

        if((Boolean) world.getGameRules().<Boolean>get(GameRuleKey.NATURAL_REGENERATION).getValue() && foodLevel >= 18 && playerEntity.shouldHeal()){
            timer += 1;
            if(timer >= 80){
                player.heal(1);
                addExhaustion(3);
                timer = 0;
            }
        }else if(foodLevel <= 0){
            timer += 1;
            if(timer >= 80){
                if(player.getHealth() > 10 || difficulty == Difficulty.HARD || player.getHealth() > 1 && difficulty == Difficulty.NORMAL){
                    playerEntity.attackEntityFrom(DamageSource.starve, 1);
                }
                timer = 0;
            }
        }else{
            timer = 0;
        }
    }

    @Override
    public void readNBT(NBTTagCompound tag) {
        if(tag.hasKey("foodLevel", 99)){
            foodLevel = tag.getInteger("foodLevel");
            timer = tag.getInteger("foodTickTimer");
            saturation = tag.getFloat("saturation");
            exhaustion = tag.getFloat("exhaustion");
        }
    }

    @Override
    public void writeNBT(NBTTagCompound tag) {
        tag.setInteger("foodLevel", foodLevel);
        tag.setInteger("foodTickTimer", timer);
        tag.setFloat("saturation", saturation);
        tag.setFloat("exhaustion", exhaustion);
    }

    @Override
    public int getFoodLevel() {
        return this.foodLevel;
    }

    @Override
    public boolean needFood() {
        return this.foodLevel < 20;
    }

    @Override
    public void addExhaustion(float amount) {
        this.exhaustion = Math.min(this.exhaustion + amount, 40.0F);
    }

    @Override
    public float getSaturationLevel() {
        return this.saturation;
    }

    @Override
    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }
}
