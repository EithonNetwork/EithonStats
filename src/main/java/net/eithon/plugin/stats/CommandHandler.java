package net.eithon.plugin.stats;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ICommandHandler;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.stats.logic.Controller;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements ICommandHandler {
	private static final String PLAYER_COMMAND = "/stats player <player>";
	private static final String STATUS_COMMAND = "/stats status";
	private static final String START_COMMAND = "/stats start <player>";
	private static final String STOP_COMMAND = "/stats stop <player>";
	private static final String AFK_COMMAND = "/stats afk [<description>]";
	private static final String SAVE_COMMAND = "/stats save";
	private static final String TIME_COMMAND = "/stats time [desc|asc]";
	private static final String BLOCKS_COMMAND = "/stats blocks [desc|asc]";
	private static final String CHAT_COMMAND = "/stats chat [desc|asc]";

	private EithonPlugin _eithonPlugin = null;
	private Controller _controller;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._eithonPlugin = eithonPlugin;
		this._controller = controller;
	}

	void disable() {
		this._controller.saveDelta();
	}

	@Override
	public boolean onCommand(CommandParser commandParser) {
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return true;
		
		String command = commandParser.getArgumentCommand();
		if (command == null) {
			timeCommand(commandParser);
		} else if (command.equalsIgnoreCase("status")) {
			statusCommand(commandParser);
		} else if (command.equalsIgnoreCase("player")) {
			playerCommand(commandParser);
		} else if (command.equalsIgnoreCase("start")) {
			startCommand(commandParser);
		} else if (command.equalsIgnoreCase("stop")) {
			stopCommand(commandParser);
		} else if (command.equalsIgnoreCase("afk")) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "Command afk");
			awayFromKeyboardCommand(commandParser);
		} else if (command.equalsIgnoreCase("save")) {
			saveCommand(commandParser);
		} else if (command.equalsIgnoreCase("time")) {
			timeCommand(commandParser);
		} else if (command.equalsIgnoreCase("blocks")) {
			blocksCommand(commandParser);
		} else if (command.equalsIgnoreCase("chat")) {
			chatCommand(commandParser);
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
		
		this._controller.stopPlayer(player, Config.M.inactivityDetected.getMessage());
		Config.M.playerStopped.sendMessage(commandParser.getSender(), player.getName());
	}

	private void awayFromKeyboardCommand(CommandParser commandParser) {
		if (!commandParser.hasPermissionOrInformSender("stats.afk")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1)) return;

		String description = commandParser.getArgumentRest(Config.M.defaultAfkDescription.getMessage());
		
		this._controller.stopPlayer(commandParser.getPlayer(), description);
		Config.M.playerAwayFromKeyboard.sendMessage(commandParser.getSender(), description);
	}

	void saveCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.save")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;
		
		this._controller.saveDelta();
		Config.M.saved.sendMessage(commandParser.getSender());
	}

	void timeCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.time")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 2)) return;
		
		String direction = commandParser.getArgumentStringAsLowercase("desc");
		boolean ascending = direction.equalsIgnoreCase("asc");
		
		this._controller.showTimeStats(commandParser.getSender(), ascending);
	}

	void blocksCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.blocks")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 2)) return;
		
		String direction = commandParser.getArgumentStringAsLowercase("desc");
		boolean ascending = direction.equalsIgnoreCase("asc");
		
		this._controller.showBlocksStats(commandParser.getSender(), ascending);
	}

	void chatCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.chat")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 2)) return;
		
		String direction = commandParser.getArgumentStringAsLowercase("desc");
		boolean ascending = direction.equalsIgnoreCase("asc");
		
		this._controller.showChatStats(commandParser.getSender(), ascending);
	}

	void statusCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.status")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 2)) return;
		
		String direction = commandParser.getArgumentStringAsLowercase("desc");
		boolean ascending = direction.equalsIgnoreCase("asc");
		
		this._controller.showAfkStatus(commandParser.getSender(), ascending);
	}

	@Override
	public void showCommandSyntax(CommandSender sender, String command) {
		if (command.equals("player")) {
			sender.sendMessage(PLAYER_COMMAND);
		} else if (command.equalsIgnoreCase("start")) {
			sender.sendMessage(START_COMMAND);
		} else if (command.equalsIgnoreCase("stop")) {
			sender.sendMessage(STOP_COMMAND);
		} else if (command.equalsIgnoreCase("afk")) {
			sender.sendMessage(AFK_COMMAND);
		} else if (command.equalsIgnoreCase("save")) {
			sender.sendMessage(SAVE_COMMAND);
		} else if (command.equalsIgnoreCase("time")) {
			sender.sendMessage(TIME_COMMAND);
		} else if (command.equalsIgnoreCase("blocks")) {
			sender.sendMessage(BLOCKS_COMMAND);
		} else if (command.equalsIgnoreCase("status")) {
			sender.sendMessage(STATUS_COMMAND);
		} else if (command.equalsIgnoreCase("chat")) {
			sender.sendMessage(CHAT_COMMAND);
		} else {
			sender.sendMessage(String.format("Unknown command: %s.", command));
		}	
	}
}
