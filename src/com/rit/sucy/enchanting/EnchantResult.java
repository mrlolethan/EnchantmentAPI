package com.rit.sucy.enchanting;

import java.util.Map;

import org.bukkit.inventory.ItemStack;

import com.rit.sucy.CustomEnchantment;

public class EnchantResult {

    private ItemStack item;
    private int level = -1;
    private CustomEnchantment firstEnchant;
    Map<CustomEnchantment, Integer> addedEnchants;

    public EnchantResult() {}

    public int getLevel() {
        return level;
    }

    public ItemStack getItem() {
        return item;
    }

    public CustomEnchantment getFirstEnchant() {
        return firstEnchant;
    }

    public Map<CustomEnchantment, Integer> getAddedEnchants() {
        return addedEnchants;
    }

    public void setLevel(int value) {
        if (level == -1)
            level = value;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public void setFirstEnchant(CustomEnchantment enchant) {
        if (firstEnchant == null)
            firstEnchant = enchant;
    }

    public void setAddedEnchants(Map<CustomEnchantment, Integer> enchants) {
        addedEnchants = enchants;
    }
}
