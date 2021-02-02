package pl.masarniamc.randomtp;
import icraft.easyhc.Main;
import icraft.gui.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Grenade implements Listener {
    //private static Plugin plugin;
    public static Plugin plugin = Main.getPlugin(Main.class);
    static HashMap<Grenade, Integer> tasks = new HashMap<>();
    static HashMap<Grenade, NamespacedKey> bossbars = new HashMap<>();
    static HashMap<Integer, Item> thrownGrenades = new HashMap<>();

    //private AtomicReference<Boolean> isThrown = new AtomicReference<>(false);

    public Grenade() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        registerRecipes();
    }



    public Grenade(PlayerInteractEvent evt) {
        AtomicReference<PlayerInteractEvent> e = new AtomicReference<>(evt);
        double maxTimeToExplode = 4.2;
        double minTimeToExplode = 3.2;
        double timeToExplode = ((int) (((Math.random() * ((maxTimeToExplode - minTimeToExplode) + 1)) + minTimeToExplode) * 10) / 10.0);
        AtomicReference<Double> remainingTimeToExplode = new AtomicReference<>(timeToExplode);
        NamespacedKey key = null;
        do {
            key = NamespacedKey.minecraft(UUID.randomUUID().toString());
        } while (bossbars.containsValue(key));
        bossbars.put(this, key);
        BossBar bossbar = Bukkit.createBossBar(key, "Granat wybuchnie za: " + timeToExplode + "s", BarColor.RED, BarStyle.SOLID);
        bossbar.addPlayer(e.get().getPlayer());
        tasks.put(this, Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            remainingTimeToExplode.updateAndGet(v -> v - 0.1);
            if (remainingTimeToExplode.get() > 0) {
                bossbar.setProgress(remainingTimeToExplode.get() / timeToExplode);
                bossbar.setTitle("Granat wybuchnie za: " + (((int) (remainingTimeToExplode.get() * 10)) / 10.0) + "s");
            } else {
                Bukkit.removeBossBar(bossbars.get(this));
                bossbar.removeAll();
                if (!thrownGrenades.containsKey(tasks.get(this))) {
                    ItemStack itemStack = new ItemStack(Material.TNT);
                    ItemMeta meta = itemStack.getItemMeta();
                    meta.setDisplayName("§aGranat zaczepny");
                    ArrayList<String> list = new ArrayList<>(Collections.singletonList("§cOdbezpieczony!"));
                    meta.setLore(list);
                    meta.setLocalizedName(tasks.get(this) + "");
                    meta.setCustomModelData(9000001);
                    itemStack.setItemMeta(meta);
                    e.get().getPlayer().getInventory().removeItem(itemStack);
                    e.get().getPlayer().getWorld().createExplosion(e.get().getPlayer().getLocation(), 40F, false);
                } else {
                    Location location = thrownGrenades.get(tasks.get(this)).getLocation();
                    location.getWorld().createExplosion(location, 40F, false);
                }
                Bukkit.getServer().getScheduler().cancelTask(tasks.get(this));
            }
        }, 2L, 2L));
        ArrayList<String> lore = new ArrayList<>();
        lore.add("§cOdbezpieczony!");
        ItemMeta meta = e.get().getItem().getItemMeta();
        meta.setLore(lore);
        meta.setLocalizedName(tasks.get(this) + "");
        e.get().getItem().setItemMeta(meta);
        new Title("Wyrzuc granat zeby nie wybuch ci w dloni!", Title.Type.ACTIONBAR, 5, 30, 5).show(e.get().getPlayer());
    }


    public static void registerRecipes() {
        // Our custom variable which we will be changing around.
        ItemStack item = new ItemStack(Material.TNT);

        // The meta of the diamond sword where we can change the name, and properties of the item.
        ItemMeta meta = item.getItemMeta();

        // We will initialise the next variable after changing the properties of the sword


        // This sets the name of the item.
        // Instead of the § symbol, you can use ChatColor.<color>
        meta.setDisplayName("§aGranat zaczepny");
        meta.setCustomModelData(9000001);

        ArrayList<String> lore = new ArrayList<>();
        lore.add("§6Zabezpieczony");
        meta.setLore(lore);

        // Set the meta of the sword to the edited meta.
        item.setItemMeta(meta);



        NamespacedKey key = new NamespacedKey(NamespacedKey.MINECRAFT, "offensive_grenade");

        ShapedRecipe recipe = new ShapedRecipe(key, item);

        recipe.shape(" F ", "ITI", "IPI");

        recipe.setIngredient('F', Material.FLINT_AND_STEEL);
        recipe.setIngredient('T', Material.TNT);
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('P', Material.PRISMARINE_SHARD);

        Bukkit.addRecipe(recipe);
    }


    @EventHandler
    public void onGrenadeUnlock(PlayerInteractEvent e) {
        if (e.getItem() != null && e.getItem().getItemMeta().getDisplayName().equals("§aGranat zaczepny") && (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR)) {
            if (e.getItem().getAmount() != 1) {
                new Title("Musisz trzymac jeden granat w rece.", Title.Type.ACTIONBAR, 5, 30, 5).show(e.getPlayer());
                return;
            }
            new Grenade(e);
        }
    }

    @EventHandler
    public void onUnlockedGrenadeThrow(PlayerDropItemEvent e){
        if(e.getItemDrop().getItemStack().getType() == Material.TNT && e.getItemDrop().getItemStack().getItemMeta().getDisplayName().equals("§aGranat zaczepny")){
            ItemMeta meta = e.getItemDrop().getItemStack().getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            lore.addAll(meta.getLore());
            if(lore.get(0).equals("§cOdbezpieczony!")) {
                thrownGrenades.put(Integer.parseInt(meta.getLocalizedName()), e.getItemDrop());
                e.getItemDrop().setVelocity(e.getItemDrop().getVelocity().multiply(4));
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e){
        if(e.getBlock().getType() == Material.TNT && e.getItemInHand().getItemMeta().getDisplayName().equals("§aGranat zaczepny")) {
            e.setCancelled(true);
        }
    }



}