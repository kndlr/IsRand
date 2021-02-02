package com.coinkr.isrand.isrand;

import org.bukkit.Bukkit;
import org.bukkit.*;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.Listener;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public final class IsRand extends JavaPlugin {
    static String worldName = "world";
    static World baseworld = Bukkit.getWorld(worldName);

    @Override
    public void onEnable() {
        getLogger().info("활성화 되었습니다.");

        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(new PlayerJoin(this), this);

        this.getCommand("spawn").setExecutor(new CommandEvent(this));
        this.getCommand("location").setExecutor(new CommandEvent(this));
        this.getCommand("roulette").setExecutor(new CommandEvent(this));

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

        //랜덤 위치 생성
        Random rand = new Random();
        int posX = rand.nextInt(500) - 1000;
        int posZ = rand.nextInt(500) - 1000;

        //플레이어의 스폰 위치
        Location spawn = new Location(plugin.baseworld, posX, 61, posZ);

        //베드락 생성, 스폰포인트 설정 후 TP
        plugin.baseworld.getBlockAt(posX, 60, posZ).setType(Material.BEDROCK);
        player.setBedSpawnLocation(spawn, true);
        player.teleport(spawn.add(0.5, 0, 0.5));

        plugin.getConfig().set(player.getUniqueId().toString() + ".x", spawn.getX());
        plugin.getConfig().set(player.getUniqueId().toString() + ".z", spawn.getZ());

        plugin.saveConfig();
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
                    if (target == null) {
                        executor.sendMessage(ChatColor.RED + args[0] + "님의 섬을 찾을 수 없습니다!");
                        return false;
                    }
                } else {
                    target = executor;
                }
                int posX = plugin.getConfig().getInt(target.getUniqueId().toString() + ".x");
                int posZ = plugin.getConfig().getInt(target.getUniqueId().toString() + ".z");
                Location spawn = new Location(plugin.baseworld, posX, 61, posZ);

                executor.teleport(spawn.subtract(0.5, 0, 0.5));

                executor.sendMessage(ChatColor.GOLD + target.getName() + ChatColor.RESET + "님의 섬으로 이동했습니다!");
            }

            //다른 사람의 스폰 가져오기
            else if (cmd.getName().equalsIgnoreCase("location")) {
                Player target = Bukkit.getPlayer(args[0]);

                int posX = plugin.getConfig().getInt(target.getUniqueId().toString() + ".x");
                int posZ = plugin.getConfig().getInt(target.getUniqueId().toString() + ".z");

                executor.sendMessage(ChatColor.GOLD + target.getName() + ChatColor.RESET + String.format("님의 섬은 %d, %d에 있습니다", posX, posZ));
            }

            else if (cmd.getName().equalsIgnoreCase("roulette")) {
                executor.sendMessage(executor.getName() + "님의 위치는 ");
            }
        }
        return false;
    }
}