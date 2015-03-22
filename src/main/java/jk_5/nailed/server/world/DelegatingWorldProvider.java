package jk_5.nailed.server.world;

import jk_5.nailed.api.map.Map;
import jk_5.nailed.api.util.Location;
import jk_5.nailed.api.world.WorldProvider;
import jk_5.nailed.server.world.voidgen.ChunkProviderVoid;
import jk_5.nailed.server.world.voidgen.WorldChunkManagerVoid;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.*;

public class DelegatingWorldProvider extends net.minecraft.world.WorldProvider {

    private final WorldProvider wrapped;

    private int dimId;
    private jk_5.nailed.api.world.World world;
    private Map map;

    public DelegatingWorldProvider(WorldProvider wrapped){
        this.wrapped = wrapped;
    }

    @Override
    public String getDimensionName() {
        return "DIM" + wrapped.getId();
    }

    @Override
    public void setDimension(int dimensionId) {
        this.dimId = dimensionId;
        wrapped.setId(dimensionId);
        super.setDimension(dimensionId);
    }

    @Override
    public IChunkProvider createChunkGenerator() {
        if(wrapped.getType().equals("overworld")){
            return new ChunkProviderGenerate(this.worldObj, this.worldObj.getSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled(), ""); //TODO: is the "" correct? Extra options
        }else if(wrapped.getType().equals("void")){
            return new ChunkProviderVoid(this.worldObj);
        }else if(wrapped.getType().equals("nether")){
            return new ChunkProviderHell(this.worldObj, false, this.worldObj.getSeed()); //True to generate nether fortresses
        }else if(wrapped.getType().equals("end")){
            return new ChunkProviderEnd(this.worldObj, this.worldObj.getSeed());
        }else if(wrapped.getType().equals("flat")){
            return new ChunkProviderFlat(this.worldObj, this.worldObj.getSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.wrapped.getOptions());
        }else{
            throw new IllegalArgumentException("Unknown world type " + this.wrapped.getType());
        }
    }

    @Override
    protected void registerWorldChunkManager() {
        if(wrapped.getType().equals("overworld")){
            this.worldChunkMgr = new WorldChunkManager(this.worldObj);
        }else if(wrapped.getType().equals("void")){
            this.worldChunkMgr = new WorldChunkManagerVoid(this.worldObj);
        }else if(wrapped.getType().equals("nether")){
            this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.hell, 0.0F);
            this.isHellWorld = true;
            this.hasNoSky = true;
        }else if(wrapped.getType().equals("end")){
            this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.sky, 0.0F);
            this.hasNoSky = true;
        }else if(wrapped.getType().equals("flat")){
            FlatGeneratorInfo info = FlatGeneratorInfo.createFlatGeneratorFromString(this.wrapped.getOptions());
            this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.getBiome(info.getBiome()), 0.5F);
        }else{
            throw new IllegalArgumentException("Unknown world type " + this.wrapped.getType());
        }
    }

    @Override
    public Location getSpawnPoint() {
        //TODO: check worldborder interference
        return this.world.getConfig().spawnPoint();
    }

    @Override
    public String getInternalNameSuffix() {
        return "";
    }

    public jk_5.nailed.api.world.World getWorld(){
        if(this.world == null){
            this.world = NailedDimensionManager.instance().getWorld(this.dimId);
        }
        return this.world;
    }

    public Map getMap(){
        if(this.map == null){
            this.map = this.getWorld().getMap();
        }
        return this.map;
    }

    public WorldProvider getWrapped() {
        return wrapped;
    }
}
