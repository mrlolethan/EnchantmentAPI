package com.rit.sucy.anvil;

import java.util.Hashtable;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.rit.sucy.EUpdateTask;

public class AnvilListener implements Listener {

    private final Plugin plugin;

    private final Hashtable<UUID, AnvilTask> tasks = new Hashtable<UUID, AnvilTask>();

    public AnvilListener(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Opens the custom inventory instead of the default anvil inventory
     *
     * @param event event details
     */
    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.ANVIL) {
            Player player = (Player) event.getPlayer();

            if (plugin.getServer().getVersion().contains("MC: 1.7.2")) {
                MainAnvil anvil = new MainAnvil(plugin, event.getInventory(), player);
                tasks.put(player.getUniqueId(), new AnvilTask(plugin, anvil));
            }
            else {
                event.setCancelled(true);
                CustomAnvil anvil = new CustomAnvil(plugin, player);
                tasks.put(player.getUniqueId(), new AnvilTask(plugin, anvil));
            }
        }
    }

    /**
     * Gives back any items when the inventory is closed
     *
     * @param event event details
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (tasks.containsKey(event.getPlayer().getUniqueId())) {
            tasks.get(event.getPlayer().getUniqueId()).getView().close();
            tasks.get(event.getPlayer().getUniqueId()).cancel();
            tasks.remove(event.getPlayer().getUniqueId());
        }
    }

    /**
     * Handles anvil transactions
     *
     * @param event event details
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        // Make sure the inventory is the custom inventory
        if (tasks.containsKey(player.getUniqueId()) && !plugin.getServer().getVersion().contains("MC: 1.7.2")) {
            if (tasks.get(player.getUniqueId()).getView().getInventory().getName().equals(event.getInventory().getName())) {
                AnvilView view = tasks.get(player.getUniqueId()).getView();
                ItemStack[] inputs = view.getInputSlots();
                boolean top = event.getRawSlot() < view.getInventory().getSize();
                if (event.getSlot() == -999) return;

                // Don't allow clicking in other slots in the anvil
                if (top && !view.isInputSlot(event.getSlot()) && event.getSlot() != view.getResultSlotID()) {
                    event.setCancelled(true);
                }
                // Don't allow shift clicking into the product slot
                else if (!top && event.isShiftClick()) {
                    event.setCancelled(true);
                }
                else if (event.isLeftClick()) {

                    // Same as shift-clicking out the product
                    if (event.getRawSlot() == view.getResultSlotID() && !isFilled(event.getCursor()) && isFilled(view.getResultSlot())) {
                        if (player.getGameMode() != GameMode.CREATIVE && (view.getRepairCost() > player.getLevel() || view.getRepairCost() >= 40)) {
                            event.setCancelled(true);
                        }
                        else {
                            view.clearInputs();
                            if (player.getGameMode() != GameMode.CREATIVE)
                                player.setLevel(player.getLevel() - view.getRepairCost());
                        }
                    }
                }
                else {
                    if (event.getRawSlot() == view.getResultSlotID() && isFilled(view.getResultSlot())) {
                    	if (player.getGameMode() != GameMode.CREATIVE && (view.getRepairCost() > player.getLevel() || view.getRepairCost() >= 40)) {
                        	event.setCancelled(true);
                    	}
                    	else {
                        	view.clearInputs();
                        	if (player.getGameMode() != GameMode.CREATIVE)
                        	    player.setLevel(player.getLevel() - view.getRepairCost());
                    	}
                	}
                }

                // Update the inventory manually after the click has happened
                new EUpdateTask(plugin, player);
            }
        }
    }

    private boolean isFilled(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    private boolean areFilled(ItemStack item1, ItemStack item2) {
        return isFilled(item1) && isFilled(item2);
    }
}
