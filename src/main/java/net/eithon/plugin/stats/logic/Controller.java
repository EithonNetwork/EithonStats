package net.eithon.plugin.stats.logic;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.PlayerCollection;
import net.eithon.library.plugin.Configuration;

public class Controller {

	private PlayerCollection<PlayerTime> _allPlayerTimes;
	private EithonPlugin _eithonPlugin;

	public Controller(EithonPlugin eithonPlugin){
		this._eithonPlugin = eithonPlugin;
		Configuration config = eithonPlugin.getConfiguration();
		this._allPlayerTimes = new PlayerCollection<PlayerTime>(new PlayerTime(), this._eithonPlugin.getDataFile("playerTimeDeltas"));
		this._allPlayerTimes.consolidateDelta(this._eithonPlugin);
	}

	public void saveDelta() {
		this._allPlayerTimes.saveDelta(this._eithonPlugin);
	}

	public void startPlayer(Player player) {
		PlayerTime time = getOrCreatePlayerTime(player);
		time.start();
	}

	public void stopPlayer(Player player) {
		PlayerTime time = getOrCreatePlayerTime(player);
		time.stop();
	}

	private PlayerTime getOrCreatePlayerTime(Player player) {
		PlayerTime time = this._allPlayerTimes.get(player);
		if (time == null) {
			time = new PlayerTime(player);
			this._allPlayerTimes.put(player, time);
		}
		return time;
	}

	public void showStats(CommandSender sender, Player player) {
		PlayerTime time = getOrCreatePlayerTime(player);
		sender.sendMessage(time.toString());
	}
}
