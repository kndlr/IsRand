package com.coinkr.isrand.isrand;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.*;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.EnderChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.Listener;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Random;

public final class IsRand extends JavaPlugin {
    static String worldName = "world";
    static World baseworld = Bukkit.getWorld(worldName);
    public static List<RouletteArg> roulette_inv = new ArrayList<RouletteArg>();
    public static HashMap<String, Long> cooltime = new HashMap<String, Long>();

    int[] roulette_n = new int[] {0, 1, 2, 4, 6, 7, 8};
    Material[] roulette_exc = new Material[] {
            Material.BEDROCK,
            Material.ENDER_CHEST,
            Material.AIR,
            Material.COMMAND_BLOCK,
            Material.CHAIN_COMMAND_BLOCK,
            Material.REPEATING_COMMAND_BLOCK,
            Material.COMMAND_BLOCK_MINECART,
            Material.DEBUG_STICK,
            Material.JIGSAW,
            Material.STRUCTURE_BLOCK,
            Material.STRUCTURE_VOID,
            Material.ELYTRA,
            Material.ENDERMAN_SPAWN_EGG,
            Material.SHULKER_SPAWN_EGG,
            Material.ENDERMITE_SPAWN_EGG,
            Material.VEX_SPAWN_EGG,
            Material.BEE_SPAWN_EGG,
            Material.BLAZE_SPAWN_EGG,
            Material.GHAST_SPAWN_EGG,
            Material.PHANTOM_SPAWN_EGG,
            Material.PARROT_SPAWN_EGG,
            Material.END_PORTAL_FRAME,
            Material.END_PORTAL,
            Material.NETHER_PORTAL,
            Material.DRAGON_EGG};

    @Override
    public void onEnable() {
        getLogger().info("활성화 되었습니다.");

        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(new PlayerJoin(this), this);
        pm.registerEvents(new InventoryClick(this), this);
        pm.registerEvents(new InventoryOpen(this), this);
        pm.registerEvents(new BlockBreak(this), this);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                for (int i = 0; i < roulette_inv.size(); i++) {
                    roulette_inv.get(i).waited++;

                    if (roulette_inv.get(i).waited >= roulette_inv.get(i).delay) {

                        Player executor = roulette_inv.get(i).executor;

                        roulette_inv.get(i).waited = 0;
                        roulette_inv.get(i).delay += (0.2 / roulette_inv.get(i).delay);

                        if (roulette_inv.get(i).delay >= 4) {
                            executor.playSound(executor.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                            if (executor.getInventory().firstEmpty() == -1) {
                                executor.getWorld().dropItem(executor.getLocation(), roulette_inv.get(i).inv.getItem(4));
                            } else {
                                executor.getInventory().addItem(roulette_inv.get(i).inv.getItem(4));
                            }

                            executor.sendMessage(ChatColor.GOLD + roulette_inv.get(i).inv.getItem(4).getType().name().toLowerCase() + ChatColor.RESET + " 아이템을 획득했습니다!");
                            roulette_inv.remove(i);
                            continue;
                        }

                        executor.playSound(executor.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1.0f, 1.0f);

                        for (int j = 6; j >= 1; j--) {
                            ItemStack item = roulette_inv.get(i).inv.getItem(roulette_n[j - 1]);

                            roulette_inv.get(i).inv.setItem(roulette_n[j], item);
                        }

                        Material random = Material.values()[new Random().nextInt(Material.values().length)];

                        while (!random.isItem() || ArrayUtils.contains(roulette_exc, random))
                            random = Material.values()[new Random().nextInt(Material.values().length)];

                        roulette_inv.get(i).inv.setItem(0, new ItemStack(random));
                    }
                }
            }
        }, 1, 1);

        this.getCommand("spawn").setExecutor(new CommandEvent(this));
        this.getCommand("location").setExecutor(new CommandEvent(this));

        getConfig().options().copyDefaults();
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("비활성화 되었습니다.");
    }
}

class PlayerJoin implements Listener {
    IsRand plugin;

    public PlayerJoin(IsRand plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfig().get(player.getUniqueId().toString()) == null) {

            Random rand = new Random();
            // -250 ~ 250 사이의 난수로 랜덤 위치 생성
            int posX = rand.nextInt(500) - 250;
            int posZ = rand.nextInt(500) - 250;

            while (posX <= 100 && posX >= -100 && posZ <= 100 && posZ >= -100) {
                posX = rand.nextInt(500) - 250;
                posZ = rand.nextInt(500) - 250;
            }

            //플레이어의 스폰 위치
            Location spawn = new Location(plugin.baseworld, posX, 60, posZ);

            //베드락 생성, 스폰포인트 설정 후 TP
            plugin.baseworld.getBlockAt(posX, 59, posZ).setType(Material.ENDER_CHEST);
            for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++) {
                    plugin.baseworld.getBlockAt(posX + x, 58, posZ + z).setType(Material.GRASS_BLOCK);
                    plugin.baseworld.getBlockAt(posX + x, 57, posZ + z).setType(Material.DIRT);
                    plugin.baseworld.getBlockAt(posX + x, 56, posZ + z).setType(Material.STONE);
                }
            player.setBedSpawnLocation(spawn, true);
            player.teleport(spawn.add(0.5, 0, 0.5));

            plugin.getConfig().set(player.getUniqueId().toString() + ".x", spawn.getX());
            plugin.getConfig().set(player.getUniqueId().toString() + ".z", spawn.getZ());

            plugin.saveConfig();
        }
    }
}

class CommandEvent implements CommandExecutor {
    IsRand plugin;

    public CommandEvent(IsRand plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player executor = (Player) sender;
            String uuid = executor.getUniqueId().toString();

            //본인 섬의 스폰 위치로 이동
            if (cmd.getName().equalsIgnoreCase("spawn")) {
                Player target;
                if ((args.length != 0) && executor.isOp()) {
                    target = Bukkit.getPlayer(args[0]);
                } else {
                    target = executor;
                }
                if (target == null || plugin.getConfig().get(target.getUniqueId().toString()) == null) {
                    executor.sendMessage(ChatColor.RED + args[0] + "님의 섬을 찾을 수 없습니다!");
                    return false;
                }
                int posX = plugin.getConfig().getInt(target.getUniqueId().toString() + ".x");
                int posZ = plugin.getConfig().getInt(target.getUniqueId().toString() + ".z");
                Location spawn = new Location(plugin.baseworld, posX, 60, posZ);

                executor.teleport(spawn.subtract(0.5, 0, 0.5));

                executor.sendMessage(ChatColor.GOLD + target.getName() + ChatColor.RESET + "님의 섬으로 이동했습니다!");
            }

            //다른 사람의 스폰 가져오기
            else if (cmd.getName().equalsIgnoreCase("location")) {
                Player target;
                if ((args.length != 0) && executor.isOp()) {
                    target = Bukkit.getPlayer(args[0]);
                } else {
                    target = executor;
                }
                if (target == null || plugin.getConfig().get(target.getUniqueId().toString()) == null) {
                    executor.sendMessage(ChatColor.RED + args[0] + "님의 섬을 찾을 수 없습니다!");
                    return false;
                }
                int posX = plugin.getConfig().getInt(target.getUniqueId().toString() + ".x");
                int posZ = plugin.getConfig().getInt(target.getUniqueId().toString() + ".z");

                executor.sendMessage(ChatColor.GOLD + target.getName() + ChatColor.RESET + String.format("님의 섬은 %d, %d에 있습니다", posX, posZ));
            }
        }
        return false;
    }
}

class RouletteArg {
    Player executor;
    Inventory inv;
    float delay;
    int waited;

    RouletteArg(Inventory inv, Player executor) {
        this.executor = executor;
        this.inv = inv;
        this.delay = 1;
        this.waited = 0;
    }
}

class InventoryClick implements Listener {
    IsRand plugin;

    public InventoryClick(IsRand plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();

        if (event.getView().getTitle().equalsIgnoreCase(ChatColor.BLUE + "룰렛")) {
            event.setCancelled(true);
        }
    }
}

class InventoryOpen implements Listener {
    IsRand plugin;

    public InventoryOpen(IsRand plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player executor = (Player) event.getPlayer();

        if (event.getInventory().getType().equals(InventoryType.ENDER_CHEST)) {
            event.setCancelled(true);

            if (plugin.cooltime.containsKey(executor.getUniqueId().toString())) {
                // 쿨타임이 17.5초 미만이라면 반환
                Long time = System.currentTimeMillis() - plugin.cooltime.get(executor.getUniqueId().toString());
                if (time < 17500) {
                    executor.sendMessage(ChatColor.RED + String.format("%s초 기다려주세요!", Math.round((17500 - time) / 100.0) / 10.0));
                    return;
                }
            }

            plugin.cooltime.put(executor.getUniqueId().toString(), System.currentTimeMillis());

            Inventory roulette = Bukkit.getServer().createInventory(executor, 9, ChatColor.BLUE + "룰렛");

            roulette.setItem(3, new ItemStack(Material.WHITE_STAINED_GLASS_PANE));
            roulette.setItem(5, new ItemStack(Material.WHITE_STAINED_GLASS_PANE));

            IsRand.roulette_inv.add(new RouletteArg(roulette, executor));

            executor.openInventory(roulette);

            executor.playSound(executor.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
        }
    }
}

class BlockBreak implements Listener {
    IsRand plugin;

    public BlockBreak(IsRand plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getType() == Material.ENDER_CHEST && player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }
}