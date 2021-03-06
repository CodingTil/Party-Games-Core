package com.tilmohr.marioparty;

import java.util.ArrayList;

import com.tilmohr.marioparty.formatting.ChatRecord;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

public class GameManager extends Manager {

	private GameWorld world;
	private LobbyManager lobbyManager;
	private EndManager endManager;

	private ArrayList<GamePlayer> players;
	private ArrayList<GamePlayer> spectators;

	boolean running;

	public GameManager(App plugin) {
		super(plugin);
		this.world = new GameWorld(plugin);
		this.lobbyManager = new LobbyManager(plugin, world, new BukkitRunnable() {
			@Override
			public void run() {
				runA();
			}
		});
		this.endManager = new EndManager(plugin);
		this.players = new ArrayList<>();
		this.spectators = new ArrayList<>();
		this.running = false;
	}

	@Override
	public boolean start() {
		if (running) {
			return false;
		}
		running = true;
		lobbyManager.start();
		return true;
	}

	public void runA() {
		lobbyManager.unregister();
		register();
		// TODO: TP to field 0
		// TODO: Entire Loop: Roll Dice -> TP Players -> Check Special Fields -> TP
		// Players -> Select Minigame -> TP to Minigame -> Run Minigame -> TP back from
		// Minigame
	}

	@Override
	public boolean stop() {
		if (!running) {
			return false;
		}
		running = false;
		unregister();
		endManager.register();
		return true;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		GamePlayer player = GamePlayer.playerIn(players, p);
		String message;
		if (player != null) {
			message = plugin.messages.getString("game.player_rejoin");
		} else {
			message = plugin.messages.getString("game.player_join");
			spectators.add(new GamePlayer(plugin, p));
		}
		ChatRecord cR = new ChatRecord(message).player(p);
		message = plugin.formatter.format(cR);
		e.setJoinMessage(message);
		plugin.getLogger().info(message);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		ChatRecord cR = new ChatRecord(plugin.messages.getString("game.player_quit")).player(p)
				.numPlayers(Bukkit.getOnlinePlayers().size() - 1);
		String message = plugin.formatter.format(cR);
		plugin.getLogger().info(message);

		GamePlayer player = GamePlayer.playerIn(players, p);
		if (player != null) {
			e.setQuitMessage(message); // No QuitMessage when a spectator leaves.
		}

	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		if (e.toWeatherState()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onThunderChange(ThunderChangeEvent e) {
		if (e.toThunderState()) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (e.getSpawnReason() == SpawnReason.NATURAL) {
			e.setCancelled(true);
		}
	}
}
