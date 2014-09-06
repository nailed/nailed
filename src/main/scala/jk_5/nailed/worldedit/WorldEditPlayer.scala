package jk_5.nailed.worldedit

import com.google.common.base.Preconditions.checkNotNull
import com.sk89q.worldedit.entity.BaseEntity
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor
import com.sk89q.worldedit.extent.inventory.BlockBag
import com.sk89q.worldedit.internal.LocalWorldAdapter
import com.sk89q.worldedit.internal.cui.CUIEvent
import com.sk89q.worldedit.util.Location
import com.sk89q.worldedit.world.World
import com.sk89q.worldedit.{LocalSession, Vector, WorldEdit, WorldVector}
import io.netty.util.CharsetUtil
import jk_5.nailed.api.chat.{ChatColor, ComponentBuilder}
import jk_5.nailed.api.player.Player
import jk_5.nailed.server.NailedServer
import jk_5.nailed.server.player.NailedPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.{Item, ItemStack}

/**
 * No description given
 *
 * @author jk-5
 */
object WorldEditPlayer {
  def wrap(player: EntityPlayerMP): WorldEditPlayer = {
    checkNotNull(player)
    new WorldEditPlayer(NailedServer.getPlayerFromEntity(player))
  }

  def getSession(player: EntityPlayerMP): LocalSession = {
    checkNotNull(player)
    WorldEdit.getInstance.getSessionManager.get(wrap(player))
  }
}

class WorldEditPlayer(p: Player) extends AbstractPlayerActor {
  val np = p.asInstanceOf[NailedPlayer]

  override def getUniqueId = p.getUniqueId
  override def getName = np.getName
  override def getState: BaseEntity = throw new UnsupportedOperationException("Cannot create a state from this object")

  override def getItemInHand: Int = {
    val is = np.getEntity.getCurrentEquippedItem
    if (is == null) 0 else Item.getIdFromItem(is.getItem)
  }

  override def getLocation: Location = {
    val loc = p.getLocation
    val position: Vector = new Vector(loc.getX, loc.getY, loc.getZ)
    new Location(getWorld, position, loc.getYaw, loc.getPitch)
  }

  override def getPosition: WorldVector = {
    val loc = p.getLocation
    new WorldVector(LocalWorldAdapter.adapt(WorldEditWorld.getWorld(np.getEntity.worldObj)), loc.getX, loc.getY, loc.getZ)
  }

  override def getWorld: World = WorldEditWorld.getWorld(np.getEntity.worldObj)
  override def getPitch = np.getEntity.rotationPitch
  override def getYaw = np.getEntity.rotationYaw

  override def giveItem(typ: Int, amt: Int) {
    np.getEntity.inventory.addItemStackToInventory(new ItemStack(Item.getItemById(typ), amt, 0))
  }

  override def dispatchCUIEvent(event: CUIEvent){
    val params = event.getParameters
    var send = event.getTypeId
    if(params.length > 0){
      send = send + "|" + params.mkString("|")
    }
    np.sendPluginMessage(NailedWorldEditPlugin.instance, "WECUI", send.getBytes(CharsetUtil.UTF_8))
  }

  override def printRaw(msg: String){
    msg.split("\n").foreach(p => this.np.sendMessage(new ComponentBuilder(p).create))
  }

  override def printDebug(msg: String){
    msg.split("\n").foreach(p => this.np.sendMessage(new ComponentBuilder(p).color(ChatColor.GRAY).create))
  }

  override def print(msg: String){
    msg.split("\n").foreach(p => this.np.sendMessage(new ComponentBuilder(p).color(ChatColor.LIGHT_PURPLE).create))
  }

  override def printError(msg: String){
    msg.split("\n").foreach(p => this.np.sendMessage(new ComponentBuilder(p).color(ChatColor.RED).create))
  }

  override def setPosition(pos: Vector, pitch: Float, yaw: Float) {
    this.np.netHandler.setPlayerLocation(pos.getX, pos.getY, pos.getZ, yaw, pitch)
  }

  override def getGroups = new Array[String](0)
  override def getInventoryBlockBag: BlockBag = null
  override def hasPermission(perm: String) = p.hasPermission(perm)
  override def getFacet[T](cls: Class[_ <: T]): T = null.asInstanceOf[T]
  override def getSessionKey = new DefaultSessionKey(np)
}
