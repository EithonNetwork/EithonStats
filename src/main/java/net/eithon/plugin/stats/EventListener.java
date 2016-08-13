package net.eithon.plugin.stats;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
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
		try {
			this._controller.startPlayer(event.getPlayer());
		} catch (FatalException | TryAgainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onEithonPlayerMoveOneBlockEvent(EithonPlayerMoveOneBlockEvent event) {
		try {
			this._controller.playerMoved(event.getPlayer());
		} catch (FatalException | TryAgainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) return;
		try {
			this._controller.addChatActivity(event.getPlayer());
		} catch (FatalException | TryAgainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		try {
			this._controller.addBlocksBroken(event.getPlayer(), 1);
		} catch (FatalException | TryAgainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		if (event.isCancelled()) return;
		try {
			this._controller.addBlocksCreated(event.getPlayer(), 1);
		} catch (FatalException | TryAgainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;
		this._controller.removePlayer(null, player);
	}
}
