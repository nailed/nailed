package jk_5.nailed.api.mappack.gamerule

/**
 * No description given
 *
 * @author jk-5
 */
trait EditableGameRules extends GameRules {

  def update(key: String, value: String)
}
