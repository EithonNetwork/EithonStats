package net.eithon.plugin.stats;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.time.AlarmTrigger;
import net.eithon.library.time.IRepeatable;
import net.eithon.plugin.stats.logic.Controller;

import org.bukkit.event.Listener;

public final class Plugin extends EithonPlugin {
	Controller _controller;

	@Override
	public void onEnable() {
		super.onEnable();
		Config.load(this);
		this._controller = new Controller(this);
		CommandHandler commandHandler = new CommandHandler(this, this._controller);
		Listener eventListener = new EventListener(this, this._controller);
		setRepeatedSave();
		super.activate(commandHandler, eventListener);
	}

	@Override
	public void onDisable() {
		this._controller.saveDelta();
		super.onDisable();
		this._controller = null;
	}

	private void setRepeatedSave() {
		final Plugin thisObject = this;
		AlarmTrigger.get().repeat("Save player statistics", Config.V.secondsBetweenSave, 
				new IRepeatable() {
			@Override
			public boolean repeat() {
				if (thisObject._controller == null) return false;
				thisObject._controller.saveDelta();
				return true;
			}
		});
	}
}
