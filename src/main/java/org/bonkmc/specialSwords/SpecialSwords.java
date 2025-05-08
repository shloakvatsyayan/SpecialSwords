package org.bonkmc.specialSwords;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class SpecialSwords extends JavaPlugin implements Listener {
    private static final Key SMASHER_MODEL = Key.key("special", "smasher");
    private static final Key LIFTED_MODEL  = Key.key("special", "lifted_smasher");

    private static final long COOLDOWN_MS = 15_000;

    private final Map<UUID, Long> lastUseTime = new HashMap<>();
    private final Map<UUID, Deque<Long>> clickTimes = new HashMap<>();
    private final Set<UUID> awaitingLeft = new HashSet<>();
    private final Map<UUID, Integer> rechargeDots = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("SpecialSwords enabled!");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : getServer().getWorlds()) {
                    for (org.bukkit.entity.Item dropped : world.getEntitiesByClass(org.bukkit.entity.Item.class)) {
                        ItemStack stack = dropped.getItemStack();
                        if (stack != null
                                && stack.getType() == Material.MACE
                                && stack.hasData(DataComponentTypes.ITEM_MODEL)
                                && LIFTED_MODEL.equals(stack.getData(DataComponentTypes.ITEM_MODEL))) {
                            dropped.remove();
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 2L);

        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (Player p : getServer().getOnlinePlayers()) {
                    UUID id = p.getUniqueId();
                    ItemStack hand = p.getInventory().getItemInMainHand();
                    boolean holdingSmasher = hand.hasData(DataComponentTypes.ITEM_MODEL)
                            && SMASHER_MODEL.equals(hand.getData(DataComponentTypes.ITEM_MODEL));

                    if (holdingSmasher) {
                        boolean cooling = lastUseTime.containsKey(id)
                                && now - lastUseTime.get(id) < COOLDOWN_MS;

                        if (cooling) {
                            int state = rechargeDots.getOrDefault(id, 0);
                            state = (state + 1) % 3;
                            rechargeDots.put(id, state);
                            String dots = new String(new char[state + 1]).replace('\0', '.');

                            p.sendActionBar(Component.text("Recharging" + dots)
                                    .color(NamedTextColor.RED));
                        } else {
                            p.sendActionBar(Component.text("Right click three times to use SMASH attack")
                                    .color(NamedTextColor.GREEN));
                            rechargeDots.remove(id);
                        }
                    } else {
                        rechargeDots.remove(id);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 10L);
    }

    @Override
    public void onDisable() {
        getLogger().info("SpecialSwords disabled!");
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent ev) {
        if (ev.getHand() != EquipmentSlot.HAND) return;
        Action act = ev.getAction();
        if (act != Action.RIGHT_CLICK_AIR && act != Action.RIGHT_CLICK_BLOCK) return;

        Player p = ev.getPlayer();
        UUID id = p.getUniqueId();

        if (awaitingLeft.contains(id)) return;
        if (p.getInventory().getItemInOffHand().getType() != Material.AIR) return;

        ItemStack hand = p.getInventory().getItemInMainHand();
        if (!hand.hasData(DataComponentTypes.ITEM_MODEL)) return;
        if (!SMASHER_MODEL.equals(hand.getData(DataComponentTypes.ITEM_MODEL))) return;

        long now = System.currentTimeMillis();
        if (lastUseTime.containsKey(id) && now - lastUseTime.get(id) < COOLDOWN_MS) return;

        Deque<Long> times = clickTimes.computeIfAbsent(id, k -> new ArrayDeque<>());
        times.addLast(now);
        while (!times.isEmpty() && now - times.peekFirst() > 1_500) {
            times.removeFirst();
        }

        int count = times.size();
        float pitch = 1.0f + (count - 1) * 0.5f;
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitch);

        if (count >= 3) {
            times.clear();
            lastUseTime.put(id, now);

            int slot = p.getInventory().getHeldItemSlot();
            p.getInventory().setItem(slot, createLiftedMace());

            Location loc = p.getLocation().add(0, 1, 0);
            p.getWorld().spawnParticle(Particle.CLOUD, loc, 20, 0.5, 0.5, 0.5, 0.02);
            p.getWorld().spawnParticle(Particle.FLAME, loc, 10, 0.3, 0.3, 0.3, 0.02);

            p.setVelocity(new Vector(0, 2.5, 0));

            awaitingLeft.add(id);
        }
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent ev) {
        if (ev.getHand() != EquipmentSlot.HAND) return;
        Action act = ev.getAction();
        if (act != Action.LEFT_CLICK_AIR && act != Action.LEFT_CLICK_BLOCK) return;

        Player p = ev.getPlayer();
        UUID id = p.getUniqueId();
        if (!awaitingLeft.contains(id)) return;

        ItemStack hand = p.getInventory().getItemInMainHand();
        if (!hand.hasData(DataComponentTypes.ITEM_MODEL)) return;
        if (!LIFTED_MODEL.equals(hand.getData(DataComponentTypes.ITEM_MODEL))) return;

        if (act == Action.LEFT_CLICK_AIR) {
            double groundY = p.getWorld().getHighestBlockYAt(p.getLocation());
            if (p.getLocation().getY() - groundY > 2.0) {
                Vector forward = p.getLocation().getDirection().normalize().multiply(1.5);
                p.setVelocity(forward);
                triggerSlam(p, id);
                return;
            }
        }

        triggerSlam(p, id);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent ev) {
        Player p = ev.getPlayer();
        UUID id = p.getUniqueId();
        if (!awaitingLeft.contains(id)) return;

        if (p.isOnGround() && p.getFallDistance() > 0) {
            triggerSlam(p, id);
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent ev) {
        if (awaitingLeft.contains(ev.getPlayer().getUniqueId())) {
            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent ev) {
        ItemStack dropped = ev.getItemDrop().getItemStack();
        if (dropped.hasData(DataComponentTypes.ITEM_MODEL)
                && LIFTED_MODEL.equals(dropped.getData(DataComponentTypes.ITEM_MODEL))) {
            ev.setCancelled(true);
        }
    }

    private void triggerSlam(Player p, UUID id) {
        awaitingLeft.remove(id);

        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerInventory inv = p.getInventory();
                for (int i = 0; i < inv.getSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (stack != null
                            && stack.getType() == Material.MACE
                            && stack.hasData(DataComponentTypes.ITEM_MODEL)
                            && LIFTED_MODEL.equals(stack.getData(DataComponentTypes.ITEM_MODEL)))
                    {
                        inv.clear(i);
                    }
                }
                inv.setItem(inv.getHeldItemSlot(), createSmasher());
            }
        }.runTaskLater(this, 6);
    }

    private ItemStack createSmasher() {
        ItemStack sword = ItemStack.of(Material.NETHERITE_SWORD);
        sword.setData(DataComponentTypes.ITEM_MODEL, SMASHER_MODEL);
        return sword;
    }

    private ItemStack createLiftedMace() {
        ItemStack mace = ItemStack.of(Material.MACE);
        mace.setData(DataComponentTypes.ITEM_MODEL, LIFTED_MODEL);

        ItemMeta meta = mace.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.DENSITY, 8, true);
            meta.addEnchant(Enchantment.BREACH, 5, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

            mace.setItemMeta(meta);
        }
        return mace;
    }
}
