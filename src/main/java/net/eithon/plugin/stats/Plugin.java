package net.eithon.plugin.stats;

import java.time.LocalDateTime;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.time.AlarmTrigger;
import net.eithon.plugin.stats.logic.Controller;

import org.bukkit.event.Listener;

public final class Plugin extends EithonPlugin {
	private Controller _controller;

	@Override
	public void onEnable() {
		super.onEnable();
		Config.load(this);
		this._controller = new Controller(this);
		CommandHandler commandHandler = new CommandHandler(this, this._controller);
		Listener eventListener = new EventListener(this, this._controller);
		super.activate(commandHandler, eventListener);
	}

	@Override
	public void onDisable() {
		this._controller.saveDelta();
		super.onDisable();
		this._controller = null;
	}
	
	private void setSaveAlarm() {
		LocalDateTime saveTime = LocalDateTime.now().plusSeconds(Config.V.secondsBeforeSave);
		AlarmTrigger.get().setAlarm("Delta save",
				saveTime, 
				new Runnable() {
			public void run() {
				keepOnSaving();
			}
		});
	}

	protected void keepOnSaving() {
		if (this._controller == null) return;
		this._controller.saveDelta();
		setSaveAlarm();
	}
}
