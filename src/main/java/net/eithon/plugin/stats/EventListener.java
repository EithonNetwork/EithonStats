package net.eithon.plugin.stats;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.stats.logic.Controller;

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
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) return;
		this._controller.addChatActivity(event.getPlayer());
	}

	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		this._controller.addBlocksDestroyed(event.getPlayer(), 1);
	}
	
	@EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		if (event.isCancelled()) return;
		this._controller.addBlocksCreated(event.getPlayer(), 1);
	}

	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		this._controller.stopPlayer(event.getPlayer(), null);
	}
}
