package net.eithon.plugin.stats;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.time.AlarmTrigger;
import net.eithon.library.time.IRepeatable;
import net.eithon.plugin.stats.logic.Controller;

import org.bukkit.event.Listener;

public final class EithonStatsPlugin extends EithonPlugin {
	Controller _controller;

	@Override
	public void onEnable() {
		super.onEnable();
		Config.load(this);
		try {
			this._controller = new Controller(this);
		} catch (FatalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CommandHandler commandHandler = new CommandHandler(this, this._controller);
		Listener eventListener = new EventListener(this, this._controller);
		autoSave();
		timespanSave();
		super.activate(commandHandler.getCommandSyntax(), eventListener);
		EithonStatsApi.initialize(this._controller);
	}

	@Override
	public void onDisable() {
		try {
			this._controller.save();
		} catch (FatalException | TryAgainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onDisable();
		this._controller = null;
	}

	private void autoSave() {
		final EithonStatsPlugin thisObject = this;
		AlarmTrigger.get().repeat("Save player statistics", Config.V.secondsBetweenSave, 
				new IRepeatable() {
			@Override
			public boolean repeat() {
				if (thisObject._controller == null) return false;
				try {
					thisObject._controller.save();
				} catch (FatalException | TryAgainException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
		});
	}

	private void timespanSave() {
		final EithonStatsPlugin thisObject = this;
		AlarmTrigger.get().repeatEveryHour("TimeSpan player statistics", 0,
				new IRepeatable() {
			@Override
			public boolean repeat() {
				if (thisObject._controller == null) return false;
				try {
					thisObject._controller.timespanSave();
				} catch (FatalException | TryAgainException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
		});
	}
}
