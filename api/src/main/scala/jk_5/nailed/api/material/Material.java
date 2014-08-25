/*
 * Nailed, a Minecraft PvP server framework
 * Copyright (C) jk-5 <http://github.com/jk-5/>
 * Copyright (C) Nailed team and contributors <http://github.com/nailed/>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the MIT License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the MIT License along with
 * this program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package jk_5.nailed.api.material;

import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.Maps;

import jk_5.nailed.api.util.Checks;

/**
 * No description given
 *
 * @author jk-5
 */
public enum Material {
    AIR(0, "air", true, 0),
    STONE(1, "stone", true),
    GRASS(2, "grass", true),
    DIRT(3, "dirt", true),
    COBBLESTONE(4, "cobblestone", true),
    PLANKS(5, "planks", true),
    SAPLING(6, "sapling", true),
    BEDROCK(7, "bedrock", true),
    FLOWING_WATER(8, "flowing_water", true),
    WATER(9, "water", true),
    FLOWING_LAVA(10, "flowing_lava", true),
    LAVA(11, "lava", true),
    SAND(12, "sand", true),
    GRAVEL(13, "gravel", true),
    GOLD_ORE(14, "gold_ore", true),
    IRON_ORE(15, "iron_ore", true),
    COAL_ORE(16, "coal_ore", true),
    LOG(17, "log", true),
    LEAVES(18, "leaves", true),
    SPONGE(19, "sponge", true),
    GLASS(20, "glass", true),
    LAPIS_ORE(21, "lapis_ore", true),
    LAPIS_BLOCK(22, "lapis_block", true),
    DISPENSER(23, "dispenser", true),
    SANDSTONE(24, "sandstone", true),
    NOTE_BLOCK(25, "noteblock", true),
    BED_BLOCK(26, "bed", true),
    POWERED_RAIL(27, "golden_rail", true),
    DETECTOR_RAIL(28, "detector_rail", true),
    STICKY_PISTON_BASE(29, "sticky_piston", true),
    WEB(30, "web", true),
    TALL_GRASS(31, "tallgrass", true),
    DEAD_BUSH(32, "deadbush", true),
    PISTON_BASE(33, "piston", true),
    PISTON_EXTENSION(34, "piston_head", true),
    WOOL(35, "wool", true),
    PISTON_MOVING_BLOCK(36, "piston_extension", true),
    YELLOW_FLOWER(37, "yellow_flower", true),
    RED_FLOWER(38, "red_flower", true),
    BROWN_MUSHROOM(39, "brown_mushroom", true),
    RED_MUSHROOM(40, "red_mushroom", true),
    GOLD_BLOCK(41, "gold_block", true),
    IRON_BLOCK(42, "iron_block", true),
    DOUBLE_SLAB(43, "double_stone_slab", true),
    SLAB(44, "stone_slab", true),
    BRICK_BLOCK(45, "brick_block", true),
    TNT(46, "tnt", true),
    BOOKSHELF(47, "bookshelf", true),
    MOSSY_COBBLESTONE(48, "mossy_cobblestone", true),
    OBSIDIAN(49, "obsidian", true),
    TORCH(50, "torch", true),
    FIRE(51, "fire", true),
    MOB_SPAWNER(52, "mob_spawner", true),
    WOOD_STAIRS(53, "oak_stairs", true), //WOOD/OAK ?
    CHEST(54, "chest", true),
    REDSTONE_WIRE(55, "redstone_wire", true),
    DIAMOND_ORE(56, "diamond_ore", true),
    DIAMOND_BLOCK(57, "diamond_block", true),
    CRAFTING_TABLE(58, "crafting_table", true),
    WHEAT_BLOCK(59, "wheat", true),
    FARMLAND(60, "farmland", true),
    FURNACE(61, "furnace", true),
    BURNING_FURNACE(62, "lit_furnace", true),
    SIGN_POST(63, "standing_sign", true),
    WOODEN_DOOR(64, "wooden_door", true),
    LADDER(65, "ladder", true),
    RAIL(66, "rail", true),
    COBBLESTONE_STAIRS(67, "stone_stairs", true),
    WALL_SIGN(68, "wall_sign", true),
    LEVER(69, "lever", true),
    STONE_PRESSURE_PLATE(70, "stone_pressure_plate", true),
    IRON_DOOR_BLOCK(71, "iron_door", true),
    WOOD_PLATE(72, "wood_pressure_plate", true),
    REDSTONE_ORE(73, "redstone_ore", true),
    GLOWING_REDSTONE_ORE(74, "lit_redstone_ore", true),
    REDSTONE_TORCH_OFF(75, "unlit_redstone_torch", true),
    REDSTONE_TORCH_ON(76, "redstone_torch", true),
    STONE_BUTTON(77, "stone_button", true),
    SNOW(78, "snow_layer", true),
    ICE(79, "ice", true),
    SNOW_BLOCK(80, "snow", true),
    CACTUS(81, "cactus", true),
    CLAY(82, "clay", true),
    SUGAR_CANE_BLOCK(83, "reeds", true),
    JUKEBOX(84, "jukebox", true),
    FENCE(85, "fence", true),
    PUMPKIN(86, "pumpkin", true),
    NETHERRACK(87, "netherrack", true),
    SOUL_SAND(88, "soul_sand", true),
    GLOWSTONE(89, "glowstone", true),
    PORTAL(90, "portal", true),
    JACK_O_LANTERN(91, "lit_pumpkin", true),
    CAKE_BLOCK(92, "cake", true),
    REPEATER_BLOCK_OFF(93, "unpowered_repeater", true),
    REPEATER_BLOCK_ON(94, "powered_repeater", true),
    STAINED_GLASS(95, "stained_glass", true),
    TRAP_DOOR(96, "trapdoor", true),
    MONSTER_EGG_BLOCK(97, "monster_egg", true),
    STONE_BRICK(98, "stonebrick", true),
    HUGE_BROWN_MUSHROOM(99, "brown_mushroom_block", true),
    HUGE_RED_MUSHROOM(100, "red_mushroom_block", true),
    IRON_BARS(101, "iron_bars", true),
    GLASS_PANE(102, "glass_pane", true),
    MELON_BLOCK(103, "melon_block", true),
    PUMPKIN_STEM(104, "pumpkin_stem", true),
    MELON_STEM(105, "melon_stem", true),
    VINE(106, "vine", true),
    FENCE_GATE(107, "fence_gate", true),
    BRICK_STAIRS(108, "brick_stairs", true),
    STONE_BRICK_STAIRS(109, "stone_brick_stairs", true),
    MYCELIUM(110, "mycelium", true),
    WATER_LILY(111, "waterlily", true),
    NETHER_BRICK(112, "nether_brick", true),
    NETHER_BRICK_FENCE(113, "nether_brick_fence", true),
    NETHER_BRICK_STAIRS(114, "nether_brick_stairs", true),
    NETHER_WART_BLOCK(115, "nether_wart", true),
    ENCHANTMENT_TABLE(116, "enchanting_table", true),
    BREWING_STAND(117, "brewing_stand", true),
    CAULDRON(118, "cauldron", true),
    END_PORTAL(119, "end_portal", true),
    ENDER_PORTAL_FRAME(120, "end_portal_frame", true),
    ENDER_STONE(121, "end_stone", true),
    DRAGON_EGG(122, "dragon_egg", true),
    REDSTONE_LAMP_OFF(123, "redstone_lamp", true),
    REDSTONE_LAMP_ON(124, "lit_redstone_lamp", true),
    WOOD_DOUBLE_SLAB(125, "double_wooden_slab", true),
    WOOD_SLAB(126, "wooden_slab", true),
    COCOA(127, "cocoa", true),
    SANDSTONE_STAIRS(128, "sandstone_stairs", true),
    EMERALD_ORE(129, "emerald_ore", true),
    ENDER_CHEST(130, "ender_chest", true),
    TRIPWIRE_HOOK(131, "tripwire_hook", true),
    TRIPWIRE(132, "tripwire", true),
    EMERALD_BLOCK(133, "emerald_block", true),
    SPRUCE_WOOD_STAIRS(134, "spruce_stairs", true),
    BIRCH_WOOD_STAIRS(135, "birch_stairs", true),
    JUNGLE_WOOD_STAIRS(136, "jungle_stairs", true),
    COMMAND_BLOCK(137, "command_block", true),
    BEACON(138, "beacon", true),
    COBBLESTONE_WALL(139, "cobblestone_wall", true),
    FLOWER_POT_BLOCK(140, "flower_pot", true),
    CARROT_BLOCK(141, "carrots", true),
    POTATO_BLOCK(142, "potatoes", true),
    WOOD_BUTTON(143, "wooden_button", true),
    SKULL(144, "skull", true),
    ANVIL(145, "anvil", true),
    TRAPPED_CHEST(146, "trapped_chest", true),
    GOLD_PRESSURE_PLATE(147, "light_weighted_pressure_plate", true),
    IRON_PRESSURE_PLATE(148, "heavy_weighted_pressure_plate", true),
    REDSTONE_COMPARATOR_OFF(149, "unpowered_comparator", true),
    REDSTONE_COMPARATOR_ON(150, "powered_comparator", true),
    DAYLIGHT_DETECTOR(151, "daylight_detector", true),
    REDSTONE_BLOCK(152, "redstone_block", true),
    QUARTZ_ORE(153, "quartz_ore", true),
    HOPPER(154, "hopper", true),
    QUARTZ_BLOCK(155, "quartz_block", true),
    QUARTZ_STAIRS(156, "quartz_stairs", true),
    ACTIVATOR_RAIL(157, "activator_rail", true),
    DROPPER(158, "dropper", true),
    STAINED_CLAY(159, "stained_hardened_clay", true),
    STAINED_GLASS_PANE(160, "stained_glass_pane", true),
    LEAVES_2(161, "leaves2", true),
    LOG_2(162, "log2", true),
    ACACIA_STAIRS(163, "acacia_stairs", true),
    DARK_OAK_STAIRS(164, "dark_oak_stairs", true),
    HAY_BALE(170, "hay_block", true),
    CARPET(171, "carpet", true),
    HARDENED_CLAY(172, "hardened_clay", true),
    COAL_BLOCK(173, "coal_block", true),
    PACKED_ICE(174, "packed_ice", true),
    DOUBLE_PLANT(175, "double_plant", true),
    //------ END BLOCKS ------ START ITEMS ------
    IRON_SHOVEL(256, "iron_shovel", false, 1, 250),
    IRON_PICKAXE(257, "iron_pickaxe", false, 1, 250),
    IRON_AXE(258, "iron_axe", false, 1, 250),
    FLINT_AND_STEEL(259, "flint_and_steel", false, 1, 64),
    APPLE(260, "apple", false),
    BOW(261, "bow", false, 1, 384),
    ARROW(262, "arrow", false),
    COAL(263, "coal", false),
    DIAMOND(264, "diamond", false),
    IRON_INGOT(265, "iron_ingot", false),
    GOLD_INGOT(266, "gold_ingot", false),
    IRON_SWORD(267, "iron_sword", false, 1, 250),
    WOOD_SWORD(268, "wooden_sword", false, 1, 59),
    WOOD_SHOVEL(269, "wooden_shovel", false, 1, 59),
    WOOD_PICKAXE(270, "wooden_pickaxe", false, 1, 59),
    WOOD_AXE(271, "wooden_axe", false, 1, 59),
    STONE_SWORD(272, "stone_sword", false, 1, 131),
    STONE_SHOVEL(273, "stone_shovel", false, 1, 131),
    STONE_PICKAXE(274, "stone_pickaxe", false, 1, 131),
    STONE_AXE(275, "stone_axe", false, 1, 131),
    DIAMOND_SWORD(276, "diamond_sword", false, 1, 1561),
    DIAMOND_SHOVEL(277, "diamond_shovel", false, 1, 1561),
    DIAMOND_PICKAXE(278, "diamond_pickaxe", false, 1, 1561),
    DIAMOND_AXE(279, "diamond_axe", false, 1, 1561),
    STICK(280, "stick", false),
    BOWL(281, "bowl", false),
    MUSHROOM_SOUP(282, "mushroom_stew", false, 1),
    GOLD_SWORD(283, "golden_sword", false, 1, 32),
    GOLD_SHOVEL(284, "golden_shovel", false, 1, 32),
    GOLD_PICKAXE(285, "golden_pickaxe", false, 1, 32),
    GOLD_AXE(286, "golden_axe", false, 1, 32),
    STRING(287, "string", false),
    FEATHER(288, "feather", false),
    GUNPOWDER(289, "gunpowder", false),
    WOOD_HOE(290, "wooden_hoe", false, 1, 59),
    STONE_HOE(291, "stone_hoe", false, 1, 131),
    IRON_HOE(292, "iron_hoe", false, 1, 250),
    DIAMOND_HOE(293, "diamond_hoe", false, 1, 1561),
    GOLD_HOE(294, "golden_hoe", false, 1, 32),
    WHEAT_SEEDS(295, "wheat_seeds", false),
    WHEAT(296, "wheat", false),
    BREAD(297, "bread", false),
    LEATHER_HELMET(298, "leather_helmet", false, 1, 55),
    LEATHER_CHESTPLATE(299, "leather_chestplate", false, 1, 80),
    LEATHER_LEGGINGS(300, "leather_leggings", false, 1, 75),
    LEATHER_BOOTS(301, "leather_boots", false, 1, 65),
    CHAINMAIL_HELMET(302, "chainmail_helmet", false, 1, 165),
    CHAINMAIL_CHESTPLATE(303, "chainmail_chestplate", false, 1, 240),
    CHAINMAIL_LEGGINGS(304, "chainmail_leggings", false, 1, 225),
    CHAINMAIL_BOOTS(305, "chainmail_boots", false, 1, 195),
    IRON_HELMET(306, "iron_helmet", false, 1, 165),
    IRON_CHESTPLATE(307, "iron_chestplate", false, 1, 240),
    IRON_LEGGINGS(308, "iron_leggings", false, 1, 225),
    IRON_BOOTS(309, "iron_boots", false, 1, 195),
    DIAMOND_HELMET(310, "diamond_helmet", false, 1, 363),
    DIAMOND_CHESTPLATE(311, "diamond_chestplate", false, 1, 528),
    DIAMOND_LEGGINGS(312, "diamond_leggings", false, 1, 495),
    DIAMOND_BOOTS(313, "diamond_boots", false, 1, 429),
    GOLD_HELMET(314, "golden_helmet", false, 1, 77),
    GOLD_CHESTPLATE(315, "golden_chestplate", false, 1, 112),
    GOLD_LEGGINGS(316, "golden_leggings", false, 1, 105),
    GOLD_BOOTS(317, "golden_boots", false, 1, 91),
    FLINT(318, "flint", false),
    PORK(319, "porkchop", false),
    GRILLED_PORK(320, "cooked_porkchop", false),
    PAINTING(321, "painting", false),
    GOLDEN_APPLE(322, "golden_apple", false),
    SIGN(323, "sign", false, 16),
    WOOD_DOOR(324, "wooden_door", false, 1),
    BUCKET(325, "bucket", false, 16),
    WATER_BUCKET(326, "water_bucket", false, 1),
    LAVA_BUCKET(327, "lava_bucket", false, 1),
    MINECART(328, "minecart", false, 1),
    SADDLE(329, "saddle", false, 1),
    IRON_DOOR(330, "iron_door", false, 1),
    REDSTONE(331, "redstone", false),
    SNOW_BALL(332, "snowball", false, 16),
    BOAT(333, "boat", false, 1),
    LEATHER(334, "leather", false),
    MILK_BUCKET(335, "milk_bucket", false, 1),
    BRICK(336, "brick", false),
    CLAY_BALL(337, "clay_ball", false),
    SUGAR_CANE(338, "reeds", false),
    PAPER(339, "paper", false),
    BOOK(340, "book", false),
    SLIME_BALL(341, "slime_ball", false),
    STORAGE_MINECART(342, "chest_minecart", false, 1),
    POWERED_MINECART(343, "furnace_minecart", false, 1),
    EGG(344, "egg", false, 16),
    COMPASS(345, "compass", false),
    FISHING_ROD(346, "fishing_rod", false, 1, 64),
    CLOCK(347, "clock", false),
    GLOWSTONE_DUST(348, "glowstone_dust", false),
    RAW_FISH(349, "fish", false),
    COOKED_FISH(350, "cooked_fished", false),
    DYE(351, "dye", false),
    BONE(352, "bone", false),
    SUGAR(353, "sugar", false),
    CAKE(354, "cake", false, 1),
    BED(355, "bed", false, 1),
    REPEATER(356, "repeater", false),
    COOKIE(357, "cookie", false),
    MAP(358, "filled_map", false),
    SHEARS(359, "shears", false, 1, 238),
    MELON(360, "melon", false),
    PUMPKIN_SEEDS(361, "pumpkin_seeds", false),
    MELON_SEEDS(362, "melon_seeds", false),
    RAW_BEEF(363, "beef", false),
    COOKED_BEEF(364, "cooked_beef", false),
    RAW_CHICKEN(365, "chicken", false),
    COOKED_CHICKEN(366, "cooked_chicken", false),
    ROTTEN_FLESH(367, "rotten_flesh", false),
    ENDER_PEARL(368, "ender_pearl", false, 16),
    BLAZE_ROD(369, "blaze_rod", false),
    GHAST_TEAR(370, "ghast_tear", false),
    GOLD_NUGGET(371, "gold_nugget", false),
    NETHER_WART(372, "nether_wart", false),
    POTION(373, "potion", false, 1),
    GLASS_BOTTLE(374, "glass_bottle", false),
    SPIDER_EYE(375, "spider_eye", false),
    FERMENTED_SPIDER_EYE(376, "fermented_spider_eye", false),
    BLAZE_POWDER(377, "blaze_powder", false),
    MAGMA_CREAM(378, "magma_cream", false),
    BREWING_STAND_ITEM(379, "brewing_stand", false),
    CAULDRON_ITEM(380, "cauldron", false),
    EYE_OF_ENDER(381, "ender_eye", false),
    SPECKLED_MELON(382, "speckled_melon", false),
    SPAWN_EGG(383, "spawn_egg", false),
    EXPERIENCE_BOTTLE(384, "experience_bottle", false),
    FIRE_CHARGE(385, "fire_charge", false),
    BOOK_AND_QUILL(386, "writable_book", false, 1),
    WRITTEN_BOOK(387, "written_book", false, 16),
    EMERALD(388, "emerald", false),
    ITEM_FRAME(389, "item_frame", false),
    FLOWER_POT(390, "flower_pot", false),
    CARROT(391, "carrot", false),
    POTATO(392, "potato", false),
    BAKED_POTATO(393, "baked_potato", false),
    POISONOUS_POTATO(394, "poisonous_potato", false),
    EMPTY_MAP(395, "map", false),
    GOLDEN_CARROT(396, "golden_carrot", false),
    SKULL_ITEM(397, "skull", false),
    CARROT_ON_A_STICK(398, "carrot_on_a_stick", false, 1, 25),
    NETHER_STAR(399, "nether_star", false),
    PUMPKIN_PIE(400, "pumpkin_pie", false),
    FIREWORK(401, "fireworks", false),
    FIREWORK_CHARGE(402, "firework_charge", false),
    ENCHANTED_BOOK(403, "enchanted_book", false, 1),
    REDSTONE_COMPARATOR(404, "comparator", false),
    NETHER_BRICK_ITEM(405, "netherbrick", false),
    QUARTZ(406, "quartz", false),
    TNT_MINECART(407, "tnt_minecart", false, 1),
    HOPPER_MINECART(408, "hopper_minecart", false, 1),
    IRON_HORSE_ARMOR(417, "iron_horse_armor", false, 1),
    GOLD_HORSE_ARMOR(418, "gold_horse_armor", false, 1),
    DIAMOND_HORSE_ARMOR(419, "diamond_horse_armor", false, 1),
    LEAD(420, "lead", false),
    NAME_TAG(421, "name_tag", false),
    COMMAND_MINECART(422, "command_block_minecart", false, 1),
    RECORD_13(2256, "record_13", false, 1),
    RECORD_CAT(2257, "record_cat", false, 1),
    RECORD_BLOCKS(2258, "record_blocks", false, 1),
    RECORD_CHIRP(2259, "record_chirp", false, 1),
    RECORD_FAR(2260, "record_far", false, 1),
    RECORD_MALL(2261, "record_mall", false, 1),
    RECORD_MELLOHI(2262, "record_mellohi", false, 1),
    RECORD_STAL(2263, "record_stal", false, 1),
    RECORD_STRAD(2264, "record_strad", false, 1),
    RECORD_WARD(2265, "record_ward", false, 1),
    RECORD_11(2266, "record_11", false, 1),
    RECORD_WAIT(2267, "record_wait", false, 1),
    ;

    private final int legacyId;
    private final String id;
    private final boolean isBlock;
    private final int maxStackSize;
    private final int durability;

    private static Material[] byId = new Material[383];
    private static final Map<String, Material> BY_NAME = Maps.newHashMap();

    static {
        for (Material material : values()) {
            if (byId.length > material.legacyId) {
                byId[material.legacyId] = material;
            } else {
                byId = Arrays.copyOfRange(byId, 0, material.legacyId + 2);
                byId[material.legacyId] = material;
            }
            BY_NAME.put(material.name(), material);
        }
    }

    private Material(int legacyId, String id, boolean isBlock){
        this(legacyId, id, isBlock, 64);
    }

    private Material(int legacyId, String id, boolean isBlock, int maxStack){
        this(legacyId, id, isBlock, maxStack, 0);
    }

    private Material(int legacyId, String id, boolean isBlock, int maxStack, int durability){
        this.legacyId = legacyId;
        this.id = id;
        this.isBlock = isBlock;
        this.maxStackSize = maxStack;
        this.durability = durability;
    }

    /**
     * Gets the item ID or block ID of this Material
     *
     * @return ID of this material
     * @deprecated It is a magic value that will be removed in mc1.8
     */
    @Deprecated
    public int getLegacyId() {
        return legacyId;
    }

    /**
     * Gets the new style ID (name) for this Material
     *
     * @return ID of this material
     */
    public String getId() {
        return id;
    }

    /**
     * Checks if this Material is a placable block
     *
     * @return true if this material is a block
     */
    public boolean isBlock() {
        return isBlock;
    }

    /**
     * Gets the maximum amount of this material that can be held in a stack
     *
     * @return Maximum stack size for this material
     */
    public int getMaxStackSize() {
        return maxStackSize;
    }

    /**
     * Gets the maximum durability of this material
     *
     * @return Maximum durability for this material
     */
    public int getMaxDurability() {
        return durability;
    }

    /**
     * Attempts to get the Material with the given ID
     *
     * @param id ID of the material to get
     * @return Material if found, or null
     * @deprecated Magic value
     */
    @Deprecated
    public static Material getMaterial(final int id) {
        if (byId.length > id && id >= 0) {
            return byId[id];
        } else {
            return null;
        }
    }

    /**
     * Attempts to get the Material with the given name.
     * <p>
     * This is a normal lookup, names must be the precise name they are given
     * in the enum.
     *
     * @param name Name of the material to get
     * @return Material if found, or null
     */
    public static Material getMaterial(final String name) {
        return BY_NAME.get(name);
    }

    /**
     * Attempts to match the Material with the given name.
     * <p>
     * This is a match lookup; names will be converted to uppercase, then
     * stripped of special characters in an attempt to format it like the
     * enum.
     * <p>
     * Using this for match by ID is deprecated.
     *
     * @param name Name of the material to get
     * @return Material if found, or null
     */
    public static Material matchMaterial(final String name) {
        Checks.notNull(name, "Name cannot be null");

        Material result = null;

        try {
            result = getMaterial(Integer.parseInt(name));
        }catch(NumberFormatException ignored) {}

        if(result == null){
            String filtered = name.toUpperCase();

            filtered = filtered.replaceAll("\\s+", "_").replaceAll("\\W", "");
            result = BY_NAME.get(filtered);
        }

        return result;
    }

    /**
     * Checks if this Material is edible.
     *
     * @return true if this Material is edible.
     */
    public boolean isEdible() {
        switch (this) {
            case BREAD:
            case CARROT:
            case BAKED_POTATO:
            case POTATO:
            case POISONOUS_POTATO:
            case GOLDEN_CARROT:
            case PUMPKIN_PIE:
            case COOKIE:
            case MELON:
            case MUSHROOM_SOUP:
            case RAW_CHICKEN:
            case COOKED_CHICKEN:
            case RAW_BEEF:
            case COOKED_BEEF:
            case RAW_FISH:
            case COOKED_FISH:
            case PORK:
            case GRILLED_PORK:
            case APPLE:
            case GOLDEN_APPLE:
            case ROTTEN_FLESH:
            case SPIDER_EYE:
                return true;
            default:
                return false;
        }
    }

    /**
     * @return True if this material represents a playable music disk.
     */
    public boolean isRecord() {
        switch(this){
            case RECORD_11:
            case RECORD_13:
            case RECORD_BLOCKS:
            case RECORD_CAT:
            case RECORD_CHIRP:
            case RECORD_FAR:
            case RECORD_MALL:
            case RECORD_MELLOHI:
            case RECORD_STAL:
            case RECORD_STRAD:
            case RECORD_WAIT:
            case RECORD_WARD:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the material is a block and solid (cannot be passed through by
     * a player)
     *
     * @return True if this material is a block and solid
     */
    public boolean isSolid() {
        if (!isBlock() || this.legacyId == 0) {
            return false;
        }
        switch (this) {
            case STONE:
            case GRASS:
            case DIRT:
            case COBBLESTONE:
            case PLANKS:
            case BEDROCK:
            case SAND:
            case GRAVEL:
            case GOLD_ORE:
            case IRON_ORE:
            case COAL_ORE:
            case LOG:
            case LEAVES:
            case SPONGE:
            case GLASS:
            case LAPIS_ORE:
            case LAPIS_BLOCK:
            case DISPENSER:
            case SANDSTONE:
            case NOTE_BLOCK:
            case BED_BLOCK:
            case STICKY_PISTON_BASE:
            case PISTON_BASE:
            case PISTON_EXTENSION:
            case WOOL:
            case PISTON_MOVING_BLOCK:
            case GOLD_BLOCK:
            case IRON_BLOCK:
            case DOUBLE_SLAB:
            case SLAB:
            case BRICK:
            case TNT:
            case BOOKSHELF:
            case MOSSY_COBBLESTONE:
            case OBSIDIAN:
            case MOB_SPAWNER:
            case WOOD_STAIRS:
            case CHEST:
            case DIAMOND_ORE:
            case DIAMOND_BLOCK:
            case CRAFTING_TABLE:
            case FARMLAND:
            case FURNACE:
            case BURNING_FURNACE:
            case SIGN_POST:
            case WOODEN_DOOR:
            case COBBLESTONE_STAIRS:
            case WALL_SIGN:
            case STONE_PRESSURE_PLATE:
            case IRON_DOOR_BLOCK:
            case WOOD_PLATE:
            case REDSTONE_ORE:
            case GLOWING_REDSTONE_ORE:
            case ICE:
            case SNOW_BLOCK:
            case CACTUS:
            case CLAY:
            case JUKEBOX:
            case FENCE:
            case PUMPKIN:
            case NETHERRACK:
            case SOUL_SAND:
            case GLOWSTONE:
            case JACK_O_LANTERN:
            case CAKE_BLOCK:
            case STAINED_GLASS:
            case TRAP_DOOR:
            case MONSTER_EGG_BLOCK:
            case STONE_BRICK:
            case HUGE_BROWN_MUSHROOM:
            case HUGE_RED_MUSHROOM:
            case IRON_BARS:
            case GLASS_PANE:
            case MELON_BLOCK:
            case FENCE_GATE:
            case BRICK_STAIRS:
            case STONE_BRICK_STAIRS:
            case MYCELIUM:
            case NETHER_BRICK:
            case NETHER_BRICK_FENCE:
            case NETHER_BRICK_STAIRS:
            case ENCHANTMENT_TABLE:
            case BREWING_STAND:
            case CAULDRON:
            case ENDER_PORTAL_FRAME:
            case ENDER_STONE:
            case DRAGON_EGG:
            case REDSTONE_LAMP_OFF:
            case REDSTONE_LAMP_ON:
            case WOOD_DOUBLE_SLAB:
            case WOOD_SLAB:
            case SANDSTONE_STAIRS:
            case EMERALD_ORE:
            case ENDER_CHEST:
            case EMERALD_BLOCK:
            case SPRUCE_WOOD_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case COMMAND_BLOCK:
            case BEACON:
            case COBBLESTONE_WALL:
            case ANVIL:
            case TRAPPED_CHEST:
            case GOLD_PRESSURE_PLATE:
            case IRON_PRESSURE_PLATE:
            case DAYLIGHT_DETECTOR:
            case REDSTONE_BLOCK:
            case QUARTZ_ORE:
            case HOPPER:
            case QUARTZ_BLOCK:
            case QUARTZ_STAIRS:
            case DROPPER:
            case STAINED_CLAY:
            case HAY_BALE:
            case HARDENED_CLAY:
            case COAL_BLOCK:
            case STAINED_GLASS_PANE:
            case LEAVES_2:
            case LOG_2:
            case ACACIA_STAIRS:
            case DARK_OAK_STAIRS:
            case PACKED_ICE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the material is a block and does not block any light
     *
     * @return True if this material is a block and does not block any light
     */
    public boolean isTransparent() {
        if (!isBlock()) {
            return false;
        }
        switch (this) {
            case AIR:
            case SAPLING:
            case POWERED_RAIL:
            case DETECTOR_RAIL:
            case TALL_GRASS:
            case DEAD_BUSH:
            case YELLOW_FLOWER:
            case RED_FLOWER:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case TORCH:
            case FIRE:
            case REDSTONE_WIRE:
            case WHEAT_BLOCK:
            case LADDER:
            case RAIL:
            case LEVER:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
            case STONE_BUTTON:
            case SNOW:
            case SUGAR_CANE_BLOCK:
            case PORTAL:
            case REPEATER_BLOCK_OFF:
            case REPEATER_BLOCK_ON:
            case PUMPKIN_STEM:
            case MELON_STEM:
            case VINE:
            case WATER_LILY:
            case NETHER_WART:
            case END_PORTAL:
            case COCOA:
            case TRIPWIRE_HOOK:
            case TRIPWIRE:
            case FLOWER_POT:
            case CARROT:
            case POTATO:
            case WOOD_BUTTON:
            case SKULL:
            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON:
            case ACTIVATOR_RAIL:
            case CARPET:
            case DOUBLE_PLANT:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the material is a block and can catch fire
     *
     * @return True if this material is a block and can catch fire
     */
    public boolean isFlammable() {
        if (!isBlock()) {
            return false;
        }
        switch (this) {
            case PLANKS:
            case LOG:
            case LEAVES:
            case NOTE_BLOCK:
            case BED_BLOCK:
            case TALL_GRASS:
            case DEAD_BUSH:
            case WOOL:
            case TNT:
            case BOOKSHELF:
            case WOOD_STAIRS:
            case CHEST:
            case CRAFTING_TABLE:
            case SIGN_POST:
            case WOODEN_DOOR:
            case WALL_SIGN:
            case WOOD_PLATE:
            case JUKEBOX:
            case FENCE:
            case TRAP_DOOR:
            case HUGE_BROWN_MUSHROOM:
            case HUGE_RED_MUSHROOM:
            case VINE:
            case FENCE_GATE:
            case WOOD_DOUBLE_SLAB:
            case WOOD_SLAB:
            case SPRUCE_WOOD_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case TRAPPED_CHEST:
            case DAYLIGHT_DETECTOR:
            case CARPET:
            case LEAVES_2:
            case LOG_2:
            case ACACIA_STAIRS:
            case DARK_OAK_STAIRS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the material is a block and can burn away
     *
     * @return True if this material is a block and can burn away
     */
    public boolean isBurnable() {
        if (!isBlock()) {
            return false;
        }
        switch (this) {
            case PLANKS:
            case LOG:
            case LEAVES:
            case TALL_GRASS:
            case WOOL:
            case YELLOW_FLOWER:
            case RED_FLOWER:
            case TNT:
            case BOOKSHELF:
            case WOOD_STAIRS:
            case FENCE:
            case VINE:
            case WOOD_DOUBLE_SLAB:
            case WOOD_SLAB:
            case SPRUCE_WOOD_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case HAY_BALE:
            case COAL_BLOCK:
            case LEAVES_2:
            case LOG_2:
            case CARPET:
            case DOUBLE_PLANT:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the material is a block and completely blocks vision
     *
     * @return True if this material is a block and completely blocks vision
     */
    public boolean isOccluding() {
        if (!isBlock()) {
            return false;
        }
        switch (this) {
            case STONE:
            case GRASS:
            case DIRT:
            case COBBLESTONE:
            case PLANKS:
            case BEDROCK:
            case SAND:
            case GRAVEL:
            case GOLD_ORE:
            case IRON_ORE:
            case COAL_ORE:
            case LOG:
            case SPONGE:
            case LAPIS_ORE:
            case LAPIS_BLOCK:
            case DISPENSER:
            case SANDSTONE:
            case NOTE_BLOCK:
            case WOOL:
            case GOLD_BLOCK:
            case IRON_BLOCK:
            case DOUBLE_SLAB:
            case BRICK:
            case BOOKSHELF:
            case MOSSY_COBBLESTONE:
            case OBSIDIAN:
            case MOB_SPAWNER:
            case DIAMOND_ORE:
            case DIAMOND_BLOCK:
            case CRAFTING_TABLE:
            case FURNACE:
            case BURNING_FURNACE:
            case REDSTONE_ORE:
            case GLOWING_REDSTONE_ORE:
            case SNOW_BLOCK:
            case CLAY:
            case JUKEBOX:
            case PUMPKIN:
            case NETHERRACK:
            case SOUL_SAND:
            case JACK_O_LANTERN:
            case MONSTER_EGG_BLOCK:
            case STONE_BRICK:
            case HUGE_BROWN_MUSHROOM:
            case HUGE_RED_MUSHROOM:
            case MELON_BLOCK:
            case MYCELIUM:
            case NETHER_BRICK:
            case ENDER_PORTAL_FRAME:
            case ENDER_STONE:
            case REDSTONE_LAMP_OFF:
            case REDSTONE_LAMP_ON:
            case WOOD_DOUBLE_SLAB:
            case EMERALD_ORE:
            case EMERALD_BLOCK:
            case COMMAND_BLOCK:
            case QUARTZ_ORE:
            case QUARTZ_BLOCK:
            case DROPPER:
            case STAINED_CLAY:
            case HAY_BALE:
            case HARDENED_CLAY:
            case COAL_BLOCK:
            case LOG_2:
            case PACKED_ICE:
                return true;
            default:
                return false;
        }
    }

    /**
     * @return True if this material is affected by gravity.
     */
    public boolean hasGravity() {
        if (!isBlock()) {
            return false;
        }
        switch (this) {
            case SAND:
            case GRAVEL:
            case ANVIL:
                return true;
            default:
                return false;
        }
    }
}
