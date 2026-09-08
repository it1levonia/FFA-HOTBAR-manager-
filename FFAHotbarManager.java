/* Source reconstructed from the supplied JAR with CFR 0.152. See PROVENANCE.md. */
package dev.opitz.ffahotbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class FFAHotbarManager
extends JavaPlugin
implements Listener {
    private static final String GUI_TITLE = ChatColor.DARK_GRAY + "FFA Hotbar Manager";
    private static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "FFA" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
    private final Map<UUID, EditorState> editors = new HashMap<UUID, EditorState>();

    public void onEnable() {
        this.saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents((Listener)this, (JavaPlugin)this);
        Bukkit.getScheduler().runTaskTimer((JavaPlugin)this, new Runnable(){

            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!FFAHotbarManager.this.isBuildFFAWorld(player)) continue;
                    FFAHotbarManager.this.applyLayout(player);
                }
            }
        }, 20L, 20L);
        this.getLogger().info("FFAHotbarManager enabled. Use /ffa hotbar");
    }

    public void onDisable() {
        this.saveConfig();
        this.editors.clear();
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent playerCommandPreprocessEvent) {
        String string = playerCommandPreprocessEvent.getMessage().trim();
        String string2 = string.toLowerCase(Locale.ENGLISH);
        if (!string2.equals("/ffa hotbar") && !string2.equals("/ffa hotbar reset")) {
            return;
        }
        playerCommandPreprocessEvent.setCancelled(true);
        Player player = playerCommandPreprocessEvent.getPlayer();
        if (string2.endsWith(" reset")) {
            this.saveSlots(player, new int[]{0, 1, 2, 3});
            this.applyLayoutLater(player, 1L);
            player.sendMessage(PREFIX + ChatColor.GREEN + "Hotbar reset to default.");
            return;
        }
        this.openEditor(player);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onTeleport(PlayerTeleportEvent playerTeleportEvent) {
        this.applyLayoutLater(playerTeleportEvent.getPlayer(), 2L);
        this.applyLayoutLater(playerTeleportEvent.getPlayer(), 6L);
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        this.applyLayoutLater(playerJoinEvent.getPlayer(), 10L);
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {
        Player player;
        if (inventoryCloseEvent.getPlayer() instanceof Player && this.editors.remove((player = (Player)inventoryCloseEvent.getPlayer()).getUniqueId()) != null) {
            this.applyLayoutLater(player, 1L);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onEditorClick(InventoryClickEvent inventoryClickEvent) {
        if (!GUI_TITLE.equals(inventoryClickEvent.getInventory().getTitle())) {
            return;
        }
        inventoryClickEvent.setCancelled(true);
        if (!(inventoryClickEvent.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player)inventoryClickEvent.getWhoClicked();
        EditorState editorState = this.editors.get(player.getUniqueId());
        if (editorState == null) {
            return;
        }
        int n = inventoryClickEvent.getRawSlot();
        if (n == 13) {
            this.saveSlots(player, editorState.slots);
            this.applyLayout(player);
            player.closeInventory();
            player.sendMessage(PREFIX + ChatColor.GREEN + "Hotbar saved and applied!");
            return;
        }
        if (n == 15) {
            editorState.selected = -1;
            editorState.slots[0] = 0;
            editorState.slots[1] = 1;
            editorState.slots[2] = 2;
            editorState.slots[3] = 3;
            this.render(player, inventoryClickEvent.getInventory(), editorState);
            player.sendMessage(PREFIX + "Layout reset in the editor. Click Save & Apply.");
            return;
        }
        if (n < 18 || n > 26) {
            return;
        }
        int n2 = n - 18;
        int n3 = this.keyIndexAt(editorState.slots, n2);
        if (editorState.selected == -1) {
            if (n3 == -1) {
                return;
            }
            editorState.selected = n3;
            this.render(player, inventoryClickEvent.getInventory(), editorState);
            player.sendMessage(PREFIX + "Selected " + Key.values()[n3].display + ChatColor.GRAY + ". Click another hotbar slot.");
            return;
        }
        int n4 = editorState.selected;
        int n5 = editorState.slots[n4];
        if (n2 == n5) {
            editorState.selected = -1;
            this.render(player, inventoryClickEvent.getInventory(), editorState);
            return;
        }
        if (n3 >= 0) {
            editorState.slots[n3] = n5;
        }
        editorState.slots[n4] = n2;
        editorState.selected = -1;
        this.saveSlots(player, editorState.slots);
        this.render(player, inventoryClickEvent.getInventory(), editorState);
    }

    private void openEditor(Player player) {
        int[] nArray = this.loadSlots(player);
        EditorState editorState = new EditorState(nArray);
        this.editors.put(player.getUniqueId(), editorState);
        Inventory inventory = Bukkit.createInventory(null, (int)27, (String)GUI_TITLE);
        this.render(player, inventory, editorState);
        player.openInventory(inventory);
        player.sendMessage(PREFIX + "Click an item, then click the slot where you want it.");
    }

    private void render(Player player, Inventory inventory, EditorState editorState) {
        inventory.clear();
        ItemStack itemStack = this.named(new ItemStack(Material.BOOK), ChatColor.AQUA + "How to use", Arrays.asList(ChatColor.GRAY + "1. Click Sword/Blocks/Shears/Pickaxe", ChatColor.GRAY + "2. Click the target hotbar slot", ChatColor.GRAY + "3. Click Save & Apply"));
        inventory.setItem(11, itemStack);
        inventory.setItem(13, this.named(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Save & Apply", Collections.singletonList(ChatColor.GRAY + "Saves permanently.")));
        inventory.setItem(15, this.named(new ItemStack(Material.REDSTONE), ChatColor.RED + "Reset Layout", Collections.singletonList(ChatColor.GRAY + "Back to Sword, Blocks, Shears, Pickaxe.")));
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack2 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
            itemStack2 = this.named(itemStack2, ChatColor.DARK_GRAY + "Hotbar Slot " + (i + 1), Collections.singletonList(ChatColor.GRAY + "Click here as a target slot."));
            inventory.setItem(18 + i, itemStack2);
        }
        Key[] keyArray = Key.values();
        for (int i = 0; i < keyArray.length; ++i) {
            Key key = keyArray[i];
            ItemStack itemStack3 = this.findCurrentItem(player, key.material);
            itemStack3 = itemStack3 == null ? new ItemStack(key.material) : itemStack3.clone();
            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(ChatColor.GRAY + "Current slot: " + ChatColor.WHITE + (editorState.slots[i] + 1));
            if (editorState.selected == i) {
                arrayList.add(ChatColor.GREEN + "SELECTED - choose destination");
            } else {
                arrayList.add(ChatColor.YELLOW + "Click to move/swap");
            }
            itemStack3 = this.named(itemStack3, key.display, arrayList);
            inventory.setItem(18 + editorState.slots[i], itemStack3);
        }
    }

    private ItemStack named(ItemStack itemStack, String string, List<String> list) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(string);
            itemMeta.setLore(list);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    private int keyIndexAt(int[] nArray, int n) {
        for (int i = 0; i < nArray.length; ++i) {
            if (nArray[i] != n) continue;
            return i;
        }
        return -1;
    }

    private int[] loadSlots(Player player) {
        String string = "players." + player.getUniqueId().toString() + ".";
        int[] nArray = new int[]{this.getConfig().getInt(string + "sword", 0), this.getConfig().getInt(string + "blocks", 1), this.getConfig().getInt(string + "shears", 2), this.getConfig().getInt(string + "pickaxe", 3)};
        if (!this.valid(nArray)) {
            return new int[]{0, 1, 2, 3};
        }
        return nArray;
    }

    private void saveSlots(Player player, int[] nArray) {
        String string = "players." + player.getUniqueId().toString() + ".";
        this.getConfig().set(string + "sword", (Object)nArray[0]);
        this.getConfig().set(string + "blocks", (Object)nArray[1]);
        this.getConfig().set(string + "shears", (Object)nArray[2]);
        this.getConfig().set(string + "pickaxe", (Object)nArray[3]);
        this.saveConfig();
    }

    private boolean valid(int[] nArray) {
        HashSet<Integer> hashSet = new HashSet<Integer>();
        for (int n : nArray) {
            if (n >= 0 && n <= 8 && hashSet.add(n)) continue;
            return false;
        }
        return true;
    }

    private boolean isBuildFFAWorld(Player player) {
        String string = this.getConfig().getString("world", "buildffa");
        return player.getWorld() != null && player.getWorld().getName().equalsIgnoreCase(string);
    }

    private void applyLayoutLater(final Player player, long l) {
        Bukkit.getScheduler().runTaskLater((JavaPlugin)this, new Runnable(){

            @Override
            public void run() {
                if (player != null && player.isOnline()) {
                    FFAHotbarManager.this.applyLayout(player);
                }
            }
        }, l);
    }

    private void applyLayout(Player player) {
        if (player == null || !player.isOnline() || !this.isBuildFFAWorld(player)) {
            return;
        }
        int[] nArray = this.loadSlots(player);
        PlayerInventory playerInventory = player.getInventory();
        EnumMap<Key, Object> enumMap = new EnumMap<Key, Object>(Key.class);
        EnumMap<Key, Integer> enumMap2 = new EnumMap<Key, Integer>(Key.class);
        block0: for (int i = 0; i < 9; ++i) {
            ItemStack object = playerInventory.getItem(i);
            if (object == null || object.getType() == Material.AIR) continue;
            for (Key key : Key.values()) {
                if (enumMap.containsKey((Object)key) || object.getType() != key.material) continue;
                enumMap.put(key, object);
                enumMap2.put(key, i);
                continue block0;
            }
        }
        if (enumMap.isEmpty()) {
            return;
        }
        for (Object object : enumMap2.values()) {
            playerInventory.setItem(((Integer)object).intValue(), null);
        }
        Key[] keyArray = Key.values();
        for (int i = 0; i < keyArray.length; ++i) {
            int n;
            ItemStack itemStack = (ItemStack)enumMap.get((Object)keyArray[i]);
            if (itemStack == null) continue;
            int n2 = nArray[i];
            ItemStack itemStack2 = playerInventory.getItem(n2);
            if (itemStack2 != null && itemStack2.getType() != Material.AIR && (n = this.firstFree(playerInventory, nArray)) >= 0) {
                playerInventory.setItem(n, itemStack2);
            }
            playerInventory.setItem(n2, itemStack);
        }
        player.updateInventory();
    }

    private int firstFree(PlayerInventory playerInventory, int[] nArray) {
        HashSet<Integer> hashSet = new HashSet<Integer>();
        for (int n : nArray) {
            hashSet.add(n);
        }
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack;
            if (hashSet.contains(i) || (itemStack = playerInventory.getItem(i)) != null && itemStack.getType() != Material.AIR) continue;
            return i;
        }
        return -1;
    }

    private ItemStack findCurrentItem(Player player, Material material) {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = player.getInventory().getItem(i);
            if (itemStack == null || itemStack.getType() != material) continue;
            return itemStack;
        }
        return null;
    }

    private static final class EditorState {
        final int[] slots;
        int selected = -1;

        EditorState(int[] nArray) {
            this.slots = nArray;
        }
    }

    private static enum Key {
        SWORD(Material.STONE_SWORD, ChatColor.RED + "Sword"),
        BLOCKS(Material.WOOL, ChatColor.AQUA + "Blocks"),
        SHEARS(Material.SHEARS, ChatColor.YELLOW + "Shears"),
        PICKAXE(Material.IRON_PICKAXE, ChatColor.GOLD + "Pickaxe");

        final Material material;
        final String display;

        private Key(Material material, String string2) {
            this.material = material;
            this.display = string2;
        }
    }
}

