package jk_5.nailed.worldedit

import java.util

import com.google.common.base.Preconditions.checkNotNull
import com.sk89q.worldedit.blocks.{BaseBlock, BaseItemStack, LazyBlock}
import com.sk89q.worldedit.entity.{BaseEntity, Entity}
import com.sk89q.worldedit.internal.Constants
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.util.Location
import com.sk89q.worldedit.util.TreeGenerator.TreeType
import com.sk89q.worldedit.world.AbstractWorld
import com.sk89q.worldedit.world.biome.BaseBiome
import com.sk89q.worldedit.{EditSession, Vector, Vector2D, WorldEditException}
import net.minecraft.block.Block
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.EntityList
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.inventory.IInventory
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.gen.ChunkProviderServer
import net.minecraft.world.{ChunkCoordIntPair, World}

import scala.collection.convert.wrapAsScala._
import scala.ref.WeakReference

/**
 * An adapter to Minecraft worlds for WorldEdit.
 *
 * @author jk-5
 */
object WorldEditWorld {

  def getWorld(world: World): WorldEditWorld = {
    checkNotNull(world)
    new WorldEditWorld(world)
  }

  private[worldedit] class WorldReferenceLostException(message: String) extends WorldEditException(message)
}

class WorldEditWorld private[worldedit] (world: World) extends AbstractWorld {
  checkNotNull(world)
  private val worldRef = new WeakReference[World](world)

  private final val logger = NailedWorldEditPlugin.instance.getLogger

  def getWorldChecked: World = worldRef.get match {
    case Some(w) => w
    case None => throw new WorldEditWorld.WorldReferenceLostException("The reference to the world was lost (i.e. the world may have been unloaded)")
  }

  def getWorld: World = worldRef.get match {
    case Some(w) => w
    case None => throw new RuntimeException("The reference to the world was lost (i.e. the world may have been unloaded)")
  }

  override def getName = getWorld.getWorldInfo.getWorldName

  override def setBlock(position: Vector, block: BaseBlock, notifyAndLight: Boolean): Boolean = {
    checkNotNull(position)
    checkNotNull(block)
    val world = getWorldChecked
    val x = position.getBlockX
    val y = position.getBlockY
    val z = position.getBlockZ
    val chunk = world.getChunkFromChunkCoords(x >> 4, z >> 4)
    var previousId = Blocks.air
    if(notifyAndLight) previousId = chunk.getBlock(x & 15, y, z & 15)
    val successful = chunk.setBlockIDWithMetadata(x & 15, y, z & 15, Block.getBlockById(block.getId), block.getData)
    if(successful){
      val tag = block.getNbtData
      if(tag != null){
        val nativeTag = NBTConverter.toNative(tag)
        nativeTag.setString("id", block.getNbtId)
        TileEntityUtils.setTileEntity(getWorld, position, nativeTag)
      }
    }
    if(notifyAndLight){
      world.updateAllLightTypes(x, y, z)
      world.markBlockForUpdate(x, y, z)
      world.notifyBlockChange(x, y, z, previousId)
      val mcBlock = Block.getBlockById(block.getId)
      if(mcBlock != null && mcBlock.hasComparatorInputOverride){
        world.updateNeighborsAboutBlockChange(x, y, z, mcBlock)
      }
    }
    successful
  }

  override def getBlockLightLevel(position: Vector): Int = {
    checkNotNull(position)
    getWorld.getBlockLightValue(position.getBlockX, position.getBlockY, position.getBlockZ)
  }

  override def clearContainerBlockContents(position: Vector): Boolean = {
    checkNotNull(position)
    getWorld.getTileEntity(position.getBlockX, position.getBlockY, position.getBlockZ) match {
      case inv: IInventory =>
        for(i <- 0 until inv.getSizeInventory) inv.setInventorySlotContents(i, null)
        true
      case _ => false
    }
  }

  override def getBiome(position: Vector2D): BaseBiome = {
    checkNotNull(position)
    new BaseBiome(getWorld.getBiomeGenForCoords(position.getBlockX, position.getBlockZ).biomeID)
  }

  override def setBiome(position: Vector2D, biome: BaseBiome): Boolean = {
    checkNotNull(position)
    checkNotNull(biome)
    val chunk = getWorld.getChunkFromBlockCoords(position.getBlockX, position.getBlockZ)
    if(chunk != null && chunk.isChunkLoaded) {
      chunk.getBiomeArray()((position.getBlockZ & 0xF) << 4 | position.getBlockX & 0xF) = biome.getId.toByte
      true
    }else false
  }

  override def dropItem(position: Vector, item: BaseItemStack){
    checkNotNull(position)
    checkNotNull(item)
    if(item.getType == 0) return
    val stack = new ItemStack(Item.getItemById(item.getType), item.getAmount, item.getData)
    for(e <- item.getEnchantments.entrySet){
      stack.addEnchantment(Enchantment.enchantmentsList(e.getKey), e.getValue)
    }
    val entity = new EntityItem(getWorld, position.getX, position.getY, position.getZ, stack)
    entity.delayBeforeCanPickup = 10
    getWorld.spawnEntityInWorld(entity)
  }

  override def regenerate(region: Region, editSession: EditSession): Boolean = {
    val history = new Array[BaseBlock](256 * (getMaxY + 1))
    for(chunk <- region.getChunks){
      val min = new Vector(chunk.getBlockX * 16, 0, chunk.getBlockZ * 16)
      for(x <- 0 until 16) for(y <- 0 until getMaxY + 1) for (z <- 0 until 16){
        val pt = min.add(x, y, z)
        history(y * 16 * 16 + z * 16 + x) = editSession.getBlock(pt)
      }
      try{
        val chunks = region.getChunks
        val provider = getWorld.getChunkProvider
        if(!provider.isInstanceOf[ChunkProviderServer]) return false
        val chunkServer = provider.asInstanceOf[ChunkProviderServer]
        val unloadQueue = chunkServer.droppedChunksSet.asInstanceOf[util.Set[Long]]
        val loadedMap = chunkServer.id2ChunkMap
        val loaded = chunkServer.loadedChunks.asInstanceOf[util.List[Chunk]]
        val chunkProvider = chunkServer.serverChunkGenerator
        for(coord <- chunks){
          val pos = ChunkCoordIntPair.chunkXZ2Int(coord.getBlockX, coord.getBlockZ)
          var mcChunk: Chunk = null
          if(chunkServer.chunkExists(coord.getBlockX, coord.getBlockZ)){
            mcChunk = chunkServer.loadChunk(coord.getBlockX, coord.getBlockZ)
            mcChunk.onChunkUnload()
          }
          unloadQueue.remove(pos)
          loadedMap.remove(pos)
          mcChunk = chunkProvider.provideChunk(coord.getBlockX, coord.getBlockZ)
          loadedMap.add(pos, mcChunk)
          loaded.add(mcChunk)
          if(mcChunk != null){
            mcChunk.onChunkLoad()
            mcChunk.populateChunk(chunkProvider, chunkProvider, coord.getBlockX, coord.getBlockZ)
          }
        }
      }catch {
        case t: Throwable =>
          logger.warn("Failed to generate chunk", t)
          return false
      }
      for(x <- 0 until 16) for(y <- 0 until getMaxY + 1) for (z <- 0 until 16){
        val pt = min.add(x, y, z)
        val index = y * 16 * 16 + z * 16 + x
        if(!region.contains(pt)){
          editSession.smartSetBlock(pt, history(index))
        }else{
          editSession.rememberChange(pt, history(index), editSession.rawGetBlock(pt))
        }
      }
    }
    false
  }

  override def generateTree(typ: TreeType, editSession: EditSession, position: Vector) = false
  override def getWorldData = WorldEditWorldData
  override def isValidBlockType(id: Int) = id == 0 || net.minecraft.block.Block.getBlockById(id) != null

  override def getBlock(position: Vector): BaseBlock = {
    val world = getWorld
    val b = world.getBlock(position.getBlockX, position.getBlockY, position.getBlockZ)
    val data = world.getBlockMetadata(position.getBlockX, position.getBlockY, position.getBlockZ)
    val tile = getWorld.getTileEntity(position.getBlockX, position.getBlockY, position.getBlockZ)
    if(tile != null) new TileEntityBaseBlock(Block.getIdFromBlock(b), data)
    else new BaseBlock(Block.getIdFromBlock(b), data)
  }

  override def getLazyBlock(position: Vector): BaseBlock = {
    val world = getWorld
    val b = world.getBlock(position.getBlockX, position.getBlockY, position.getBlockZ)
    val data = world.getBlockMetadata(position.getBlockX, position.getBlockY, position.getBlockZ)
    new LazyBlock(Block.getIdFromBlock(b), data, this, position)
  }

  override def hashCode = getWorld.hashCode

  override def equals(o: Any): Boolean = o match {
    case null => false
    case w: WorldEditWorld =>
      val otherWorld = w.worldRef.get
      val thisWorld = worldRef.get
      otherWorld.isDefined && thisWorld.isDefined && otherWorld.get == thisWorld.get
    case w: com.sk89q.worldedit.world.World => w.getName == getName
    case _ => false
  }

  override def getEntities(region: Region): util.List[_ <: Entity] = {
    val entities = new util.ArrayList[Entity]
    val world = getWorld
    for(pt <- region.getChunks){
      if(world.getChunkProvider.chunkExists(pt.getBlockX, pt.getBlockZ)){
        val chunk = world.getChunkProvider.provideChunk(pt.getBlockX, pt.getBlockZ)
        for(entitySubList <- chunk.entityLists){
          for(entity <- entitySubList){
            val e = entity.asInstanceOf[net.minecraft.entity.Entity]
            if(region.contains(new Vector(e.posX, e.posY, e.posZ))){
              entities.add(new WorldEditEntity(e))
            }
          }
        }
      }
    }
    entities
  }

  override def getEntities: util.List[_ <: Entity] = {
    val entities = new util.ArrayList[Entity]()
    for(entity <- getWorld.loadedEntityList){
      entities.add(new WorldEditEntity(entity.asInstanceOf[net.minecraft.entity.Entity]))
    }
    entities
  }

  override def createEntity(location: Location, entity: BaseEntity): Entity = {
    val world = getWorld
    val createdEntity = EntityList.createEntityByName(entity.getTypeId, world)
    if(createdEntity != null){
      val nativeTag = entity.getNbtData
      if (nativeTag != null) {
        val tag = NBTConverter.toNative(entity.getNbtData)
        for(name <- Constants.NO_COPY_ENTITY_NBT_FIELDS){
          tag.removeTag(name)
        }
        createdEntity.readFromNBT(tag)
      }
      createdEntity.setLocationAndAngles(location.getX, location.getY, location.getZ, location.getYaw, location.getPitch)
      world.spawnEntityInWorld(createdEntity)
      new WorldEditEntity(createdEntity)
    }else null
  }
}
