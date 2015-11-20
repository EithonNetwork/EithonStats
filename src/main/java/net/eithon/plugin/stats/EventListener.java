package net.eithon.plugin.stats;

import net.eithon.library.bungee.EithonBungeeEvent;
import net.eithon.library.bungee.EithonBungeeJoinEvent;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.move.EithonPlayerMoveOneBlockEvent;
import net.eithon.plugin.stats.logic.Controller;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class EventListener implements Listener {
	
	private Controller _controller;
	
	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		this._controller.startPlayer(event.getPlayer());
	}

	@EventHandler
	public void onEithonPlayerMoveOneBlockEvent(EithonPlayerMoveOneBlockEvent event) {
		this._controller.playerMoved(event.getPlayer());
	}

	@EventHandler
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) return;
		this._controller.addChatActivity(event.getPlayer());
	}

	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		this._controller.addBlocksBroken(event.getPlayer(), 1);
	}
	
	@EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		if (event.isCancelled()) return;
		this._controller.addBlocksCreated(event.getPlayer(), 1);
	}

	// Transfer player statistics to the primary server
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		this._controller.stopPlayer(player, null);
		if (this._controller.isPrimaryBungeeServer()) return;
		this._controller.transferPlayerStatsToPrimaryServer(new EithonPlayer(player), true);
	}

	// Transfer player statistics to the new server, if we are the primary server.
	@EventHandler
	public void onEithonBungeeJoinEvent(EithonBungeeJoinEvent event) {
		if (!this._controller.isPrimaryBungeeServer()) return;
		this._controller.transferPlayerStatsToSlaveServer(event.getPlayer(), event.getThatServerName());
	}

	// Receive an event from another bungee server
	@EventHandler
	public void onEithonBungeeEvent(EithonBungeeEvent event) {
		this._controller.handleEithonBungeeEvent(event);
	}
}
