package org.bonkmc.specialSwords;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

public class SpecialSwords extends JavaPlugin implements Listener {

    // ← change these to match your resource-pack's CustomModelData values
    private static final int SMASHER_MODEL_DATA        = /* e.g. */ 1001;
    private static final int LIFTED_SMASHER_MODEL_DATA = /* e.g. */ 1002;

    // ← how long (in seconds) before they can do the triple-right-click again
    private static final long COOLDOWN_SECONDS = 15;

    // time window to collect three clicks (in ticks; 20 ticks = 1s)
    private static final long CLICK_RESET_TICKS = 40; // 2 seconds

    private final Map<UUID, Integer> clickCount     = new HashMap<>();
    private final Map<UUID, Long>    lastUseTime    = new HashMap<>();
    private final Set<UUID>          awaitingLeftClick = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("SpecialSwords enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SpecialSwords disabled!");
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        // only main-hand right clicks
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action a = event.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;

        Player p = event.getPlayer();
        // off-hand must be empty
        if (p.getInventory().getItemInOffHand().getType() != Material.AIR) return;

        ItemStack inHand = p.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType() != Material.NETHERITE_SWORD) return;
        ItemMeta m = inHand.getItemMeta();
        if (m == null || !m.hasCustomModelData() || m.getCustomModelData() != SMASHER_MODEL_DATA) return;

        UUID id = p.getUniqueId();
        long now = System.currentTimeMillis();
        if (lastUseTime.containsKey(id) && now - lastUseTime.get(id) < COOLDOWN_SECONDS * 1000) {
            return; // still cooling down
        }

        // count clicks
        int cnt = clickCount.getOrDefault(id, 0) + 1;
        clickCount.put(id, cnt);
        // reset counter after CLICK_RESET_TICKS
        new BukkitRunnable() {
            @Override public void run() {
                clickCount.remove(id);
            }
        }.runTaskLater(this, CLICK_RESET_TICKS);

        if (cnt == 3) {
            // perform the special move
            clickCount.remove(id);
            lastUseTime.put(id, now);

            // 1) swap to the "lifted_smasher" mace
            int slot = p.getInventory().getHeldItemSlot();
            p.getInventory().setItem(slot, createLiftedMace());

            // 2) launch them upward
            p.setVelocity(new Vector(0, 2.5, 0));

            // wait for their next left-click
            awaitingLeftClick.add(id);
        }
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event) {
        // only main-hand left clicks
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action a = event.getAction();
        if (a != Action.LEFT_CLICK_AIR && a != Action.LEFT_CLICK_BLOCK) return;

        Player p = event.getPlayer();
        UUID id = p.getUniqueId();
        if (!awaitingLeftClick.remove(id)) return;

        // must still be holding the lifted mace
        ItemStack inHand = p.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType() != Material.NETHERITE_SWORD) return;
        ItemMeta m = inHand.getItemMeta();
        if (m == null || !m.hasCustomModelData() || m.getCustomModelData() != LIFTED_SMASHER_MODEL_DATA) {
            return;
        }

        // wait 1.5 seconds (30 ticks), then clear all mace items and give back the sword
        new BukkitRunnable() {
            @Override public void run() {
                PlayerInventory inv = p.getInventory();
                // clear every lifted_smasher in their inventory
                for (int i = 0; i < inv.getSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (stack != null && stack.getType() == Material.NETHERITE_SWORD) {
                        ItemMeta mm = stack.getItemMeta();
                        if (mm != null
                                && mm.hasCustomModelData()
                                && mm.getCustomModelData() == LIFTED_SMASHER_MODEL_DATA) {
                            inv.clear(i);
                        }
                    }
                }
                // put the original sword back in their current hand slot
                int slot = p.getInventory().getHeldItemSlot();
                inv.setItem(slot, createSmasher());
            }
        }.runTaskLater(this, 30);
    }

    private ItemStack createSmasher() {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta m = sword.getItemMeta();
        m.setCustomModelData(SMASHER_MODEL_DATA);
        sword.setItemMeta(m);
        return sword;
    }

    private ItemStack createLiftedMace() {
        ItemStack mace = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta m = mace.getItemMeta();
        m.setCustomModelData(LIFTED_SMASHER_MODEL_DATA);

        // apply your custom enchants (must be registered elsewhere)
        Enchantment density   = Enchantment.getByKey(new NamespacedKey(this, "density"));
        Enchantment breach    = Enchantment.getByKey(new NamespacedKey(this, "breach"));
        Enchantment windburst = Enchantment.getByKey(new NamespacedKey(this, "windburst"));

        if (density   != null) m.addEnchant(density,   5, true);
        if (breach    != null) m.addEnchant(breach,    5, true);
        if (windburst != null) m.addEnchant(windburst, 4, true);

        mace.setItemMeta(m);
        return mace;
    }
}
