package net.eithon.plugin.stats;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.stats.logic.Controller;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class EventListener implements Listener {
	
	private Controller _controller;
	
	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.isCancelled()) return;
		if (event.getAction() != Action.PHYSICAL) return;
	}
}
