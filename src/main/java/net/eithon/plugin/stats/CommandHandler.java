package net.eithon.plugin.stats;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ICommandHandler;
import net.eithon.plugin.stats.logic.Controller;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements ICommandHandler {
	private static final String PLAYER_COMMAND = "/stats player <player>";
	private static final String START_COMMAND = "/stats start <player>";
	private static final String STOP_COMMAND = "/stats stop <player>";
	private static final String SAVE_COMMAND = "/stats save";
	private static final String ALL_COMMAND = "/stats all";

	private EithonPlugin _eithonPlugin = null;
	private Controller _controller;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
	}

	void disable() {
		this._controller.saveDelta();
	}

	@Override
	public boolean onCommand(CommandParser commandParser) {
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1)) return true;
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return true;

		String command = commandParser.getArgumentStringAsLowercase();
		if (command.equalsIgnoreCase("player")) {
			playerCommand(commandParser);
		} else if (command.equalsIgnoreCase("all")) {
			allCommand(commandParser);
		} else if (command.equalsIgnoreCase("start")) {
			startCommand(commandParser);
		} else if (command.equalsIgnoreCase("stop")) {
			stopCommand(commandParser);
		} else if (command.equalsIgnoreCase("save")) {
			saveCommand(commandParser);
		} else {
			commandParser.showCommandSyntax();
		}
		return true;
	}

	void playerCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.player")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 2)) return;

		Player player = commandParser.getArgumentPlayer(commandParser.getPlayer());
		
		this._controller.showStats(commandParser.getSender(), player);
	}

	void allCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.player")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;
		
		this._controller.showStats(commandParser.getSender());
	}

	void startCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.start")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 2)) return;

		Player player = commandParser.getArgumentPlayer(commandParser.getPlayer());
		
		this._controller.startPlayer(player);
		Config.M.playerStarted.sendMessage(commandParser.getSender(), player.getName());
	}

	void stopCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.stop")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 2)) return;

		Player player = commandParser.getArgumentPlayer(commandParser.getPlayer());
		
		this._controller.stopPlayer(player);
		Config.M.playerStopped.sendMessage(commandParser.getSender(), player.getName());
	}

	void saveCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.save")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;
		
		this._controller.saveDelta();
		Config.M.saved.sendMessage(commandParser.getSender());
	}

	@Override
	public void showCommandSyntax(CommandSender sender, String command) {

		if (command.equals("player")) {
			sender.sendMessage(PLAYER_COMMAND);
		} else if (command.equalsIgnoreCase("start")) {
			sender.sendMessage(START_COMMAND);
		} else if (command.equalsIgnoreCase("stop")) {
			sender.sendMessage(STOP_COMMAND);
		} else if (command.equalsIgnoreCase("save")) {
			sender.sendMessage(SAVE_COMMAND);
		} else if (command.equalsIgnoreCase("all")) {
			sender.sendMessage(ALL_COMMAND);
		} else {
			sender.sendMessage(String.format("Unknown command: %s.", command));
		}	
	}
}
