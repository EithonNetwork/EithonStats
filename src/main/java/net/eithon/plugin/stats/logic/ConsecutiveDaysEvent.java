package net.eithon.plugin.stats.logic;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ConsecutiveDaysEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Player _player;
	private long _consecutiveDays;

	public ConsecutiveDaysEvent(Player player, long consecutiveDays) {
		this._player = player;
		this._consecutiveDays = consecutiveDays;
	}

	public static HandlerList getHandlerList() {
		return handlers;
		}


	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public Player getPlayer() { return this._player; }
	
	public long getConsecutiveDays() { return this._consecutiveDays; }
}
