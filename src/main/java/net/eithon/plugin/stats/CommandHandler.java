package net.eithon.plugin.stats;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ICommandHandler;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.stats.logic.Controller;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements ICommandHandler {
	private static final String PLAYER_COMMAND = "/stats player <player>";
	private static final String START_COMMAND = "/stats start <player>";
	private static final String STOP_COMMAND = "/stats stop <player>";
	private static final String ADD_COMMAND = "/stats add <player> [time <HH:MM:SS>] [consecutivedays <days>] [placed <blocks>] [broken <blocks>]";
	private static final String TAKE_COMMAND = "/stats remove <player> [time <HH:MM:SS>] [consecutivedays <days>] [placed <blocks>] [broken <blocks>]";
	private static final String RESET_COMMAND = "/stats reset <player>";
	private static final String WHO_COMMAND = "/stats who";
	private static final String AFK_COMMAND = "/stats afk [<description>]";
	private static final String SAVE_COMMAND = "/stats save";
	private static final String TIME_COMMAND = "/stats time [desc|asc] [<maxItems>]";
	private static final String BLOCKS_COMMAND = "/stats blocks [desc|asc] [<maxItems>]";
	private static final String CHAT_COMMAND = "/stats chat [desc|asc] [<maxItems>]";
	private static final String STATUS_COMMAND = "/stats st atus [desc|asc] [<maxItems>]";
	private static final String DIFF_COMMAND = "/stats diff <daysback> [desc|asc] [<maxItems>]";
	private static final String PLAYER_DIFF_COMMAND = "/stats playerdiff <player> <daysback>";

	private EithonPlugin _eithonPlugin = null;
	private Controller _controller;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._eithonPlugin = eithonPlugin;
		this._controller = controller;
	}

	void disable() {
	}

	@Override
	public boolean onCommand(CommandParser commandParser) {
		Player player = commandParser.getPlayerOrInformSender();
		if (player == null) return true;

		this._controller.playerCommand(player);
		String command = commandParser.getArgumentCommand();
		if (command == null) {
			return false;
		} else if (command.equalsIgnoreCase("status")) {
			statusCommand(commandParser);
		} else if (command.equalsIgnoreCase("add")) {
			addCommand(commandParser);
		} else if (command.equalsIgnoreCase("remove")) {
			removeCommand(commandParser);
		} else if (command.equalsIgnoreCase("reset")) {
			resetCommand(commandParser);
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
		} else if (command.equalsIgnoreCase("who")) {
			whoCommand(commandParser);
		} else if (command.equalsIgnoreCase("time")) {
			timeCommand(commandParser);
		} else if (command.equalsIgnoreCase("blocks")) {
			blocksCommand(commandParser);
		} else if (command.equalsIgnoreCase("chat")) {
			chatCommand(commandParser);
		} else if (command.equalsIgnoreCase("diff")) {
			diffCommand(commandParser);
		} else if (command.equalsIgnoreCase("playerdiff")) {
			playerDiffCommand(commandParser);
		} else {
			commandParser.showCommandSyntax();
		}
		return true;
	}

	void playerCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.player")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 2)) return;

		EithonPlayer eithonPlayer = commandParser.getArgumentEithonPlayer(commandParser.getPlayer());

		this._controller.showStats(commandParser.getSender(), eithonPlayer);
	}

	void addCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.add")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(4)) return;

		EithonPlayer eithonPlayer = commandParser.getArgumentEithonPlayer(commandParser.getPlayer());
		while (true) {
			String command = commandParser.getArgumentCommand();
			if (command == null) break;
			boolean success = false;

			if (command.equals("time")) {
				success = addTime(commandParser, eithonPlayer);
			} else if (command.equals("consecutivedays")) {
				success = addConsecutiveDays(commandParser, eithonPlayer);
			} else if (command.equals("placed")) {
				success = addPlacedBlocks(commandParser, eithonPlayer);
			} else if (command.equals("broken")) {
				success = addBrokenBlocks(commandParser, eithonPlayer);
			} 

			if (!success) {
				commandParser.showCommandSyntax();
				break;
			}
		}
	}

	public boolean addTime(CommandParser commandParser, EithonPlayer eithonPlayer) {
		long playTimeInSeconds = commandParser.getArgumentTimeAsSeconds(0);
		long totalPlayTimeInSeconds = this._controller.addPlayTime(commandParser.getSender(), eithonPlayer, playTimeInSeconds);
		Config.M.playTimeAdded.sendMessage(
				commandParser.getSender(),
				TimeMisc.secondsToString(playTimeInSeconds),
				eithonPlayer.getName(), 
				TimeMisc.secondsToString(totalPlayTimeInSeconds));
		return true;
	}

	public boolean addConsecutiveDays(CommandParser commandParser, EithonPlayer eithonPlayer) {
		long consecutiveDays = commandParser.getArgumentInteger(0);
		long totalConsecutiveDays = this._controller.addConsecutiveDays(commandParser.getSender(), eithonPlayer, consecutiveDays);
		Config.M.consecutiveDaysAdded.sendMessage(
				commandParser.getSender(),
				consecutiveDays,
				eithonPlayer.getName(), 
				totalConsecutiveDays);
		return true;
	}

	public boolean addPlacedBlocks(CommandParser commandParser, EithonPlayer eithonPlayer) {
		long placedBlocks = commandParser.getArgumentInteger(0);
		long totalPlacedBlocks = this._controller.addPlacedBlocks(commandParser.getSender(), eithonPlayer, placedBlocks);
		Config.M.placedBlocksAdded.sendMessage(
				commandParser.getSender(),
				placedBlocks,
				eithonPlayer.getName(), 
				totalPlacedBlocks);
		return true;
	}

	public boolean addBrokenBlocks(CommandParser commandParser, EithonPlayer eithonPlayer) {
		long brokenBlocks = commandParser.getArgumentInteger(0);
		long totalBrokenBlocks = this._controller.addBrokenBlocks(commandParser.getSender(), eithonPlayer, brokenBlocks);
		Config.M.brokenBlocksAdded.sendMessage(
				commandParser.getSender(),
				brokenBlocks,
				eithonPlayer.getName(), 
				totalBrokenBlocks);
		return true;
	}

	void removeCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.remove")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(4)) return;

		EithonPlayer eithonPlayer = commandParser.getArgumentEithonPlayer(commandParser.getPlayer());
		while (true) {
			String command = commandParser.getArgumentCommand();
			if (command == null) break;
			boolean success = false;

			if (command.equals("time")) {
				success = removeTime(commandParser, eithonPlayer);
			} else if (command.equals("consecutivedays")) {
				success = removeConsecutiveDays(commandParser, eithonPlayer);
			} else if (command.equals("placed")) {
				success = removePlacedBlocks(commandParser, eithonPlayer);
			} else if (command.equals("broken")) {
				success = removeBrokenBlocks(commandParser, eithonPlayer);
			} 

			if (!success) {
				commandParser.showCommandSyntax();
				break;
			}
		}
	}

	public boolean removeTime(CommandParser commandParser, EithonPlayer eithonPlayer) {
		long playTimeInSeconds = commandParser.getArgumentTimeAsSeconds(0);
		long totalPlayTimeInSeconds = this._controller.addPlayTime(commandParser.getSender(), eithonPlayer, -playTimeInSeconds);
		Config.M.playTimeRemoved.sendMessage(
				commandParser.getSender(),
				TimeMisc.secondsToString(playTimeInSeconds),
				eithonPlayer.getName(), 
				TimeMisc.secondsToString(totalPlayTimeInSeconds));
		return true;
	}

	public boolean removeConsecutiveDays(CommandParser commandParser, EithonPlayer eithonPlayer) {
		long consecutiveDays = commandParser.getArgumentInteger(0);
		long totalConsecutiveDays = this._controller.addConsecutiveDays(commandParser.getSender(), eithonPlayer, -consecutiveDays);
		Config.M.consecutiveDaysRemoved.sendMessage(
				commandParser.getSender(),
				consecutiveDays,
				eithonPlayer.getName(), 
				totalConsecutiveDays);
		return true;
	}

	public boolean removePlacedBlocks(CommandParser commandParser, EithonPlayer eithonPlayer) {
		long placedBlocks = commandParser.getArgumentInteger(0);
		long totalPlacedBlocks = this._controller.addPlacedBlocks(commandParser.getSender(), eithonPlayer, -placedBlocks);
		Config.M.placedBlocksRemoved.sendMessage(
				commandParser.getSender(),
				placedBlocks,
				eithonPlayer.getName(), 
				totalPlacedBlocks);
		return true;
	}

	public boolean removeBrokenBlocks(CommandParser commandParser, EithonPlayer eithonPlayer) {
		long brokenBlocks = commandParser.getArgumentInteger(0);
		long totalBrokenBlocks = this._controller.addBrokenBlocks(commandParser.getSender(), eithonPlayer, -brokenBlocks);
		Config.M.brokenBlocksRemoved.sendMessage(
				commandParser.getSender(),
				brokenBlocks,
				eithonPlayer.getName(), 
				totalBrokenBlocks);
		return true;
	}

	void resetCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.take")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;

		EithonPlayer eithonPlayer = commandParser.getArgumentEithonPlayer(commandParser.getPlayer());

		this._controller.resetPlayTime(commandParser.getSender(), eithonPlayer);
		Config.M.playTimeReset.sendMessage(
				commandParser.getSender(),
				eithonPlayer.getName());
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
	}

	void saveCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.save")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;

		this._controller.save();
		Config.M.saved.sendMessage(commandParser.getSender());
	}

	void whoCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.who")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;

		this._controller.who(commandParser.getSender());
	}

	void timeCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.time")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 3)) return;

		String direction = commandParser.getArgumentStringAsLowercase("desc");
		boolean ascending = direction.equalsIgnoreCase("asc");

		int maxItems = commandParser.getArgumentInteger(0);

		this._controller.showTimeStats(commandParser.getSender(), ascending, maxItems);
	}

	void blocksCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.blocks")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 3)) return;

		String direction = commandParser.getArgumentStringAsLowercase("desc");
		boolean ascending = direction.equalsIgnoreCase("asc");

		int maxItems = commandParser.getArgumentInteger(0);

		this._controller.showBlocksStats(commandParser.getSender(), ascending, maxItems);
	}

	void chatCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.chat")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 3)) return;

		String direction = commandParser.getArgumentStringAsLowercase("desc");
		boolean ascending = direction.equalsIgnoreCase("asc");

		int maxItems = commandParser.getArgumentInteger(0);

		this._controller.showChatStats(commandParser.getSender(), ascending, maxItems);
	}

	void diffCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.diff")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 4)) return;

		int daysBack = commandParser.getArgumentInteger(7);

		String direction = commandParser.getArgumentStringAsLowercase("desc");
		boolean ascending = direction.equalsIgnoreCase("asc");

		int maxItems = commandParser.getArgumentInteger(0);

		this._controller.showDiffStats(commandParser.getSender(), daysBack, ascending, maxItems);
	}

	void playerDiffCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.diff")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(3,5)) return;

		EithonPlayer player = commandParser.getArgumentEithonPlayer(commandParser.getEithonPlayer());
		int daysBack = commandParser.getArgumentInteger(7);

		this._controller.showDiffStats(commandParser.getSender(), player, daysBack);
	}

	void statusCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("stats.status")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 3)) return;

		String direction = commandParser.getArgumentStringAsLowercase("desc");
		boolean ascending = direction.equalsIgnoreCase("asc");

		int maxItems = commandParser.getArgumentInteger(0);

		this._controller.showAfkStatus(commandParser.getSender(), ascending, maxItems);
	}

	@Override
	public void showCommandSyntax(CommandSender sender, String command) {
		if (command.equals("player")) {
			sender.sendMessage(PLAYER_COMMAND);
		} else if (command.equalsIgnoreCase("start")) {
			sender.sendMessage(START_COMMAND);
		} else if (command.equalsIgnoreCase("stop")) {
			sender.sendMessage(STOP_COMMAND);
		} else if (command.equalsIgnoreCase("add")) {
			sender.sendMessage(ADD_COMMAND);
		} else if (command.equalsIgnoreCase("take")) {
			sender.sendMessage(TAKE_COMMAND);
		} else if (command.equalsIgnoreCase("reset")) {
			sender.sendMessage(RESET_COMMAND);
		} else if (command.equalsIgnoreCase("afk")) {
			sender.sendMessage(AFK_COMMAND);
		} else if (command.equalsIgnoreCase("save")) {
			sender.sendMessage(SAVE_COMMAND);
		} else if (command.equalsIgnoreCase("who")) {
			sender.sendMessage(WHO_COMMAND);
		} else if (command.equalsIgnoreCase("time")) {
			sender.sendMessage(TIME_COMMAND);
		} else if (command.equalsIgnoreCase("blocks")) {
			sender.sendMessage(BLOCKS_COMMAND);
		} else if (command.equalsIgnoreCase("status")) {
			sender.sendMessage(STATUS_COMMAND);
		} else if (command.equalsIgnoreCase("chat")) {
			sender.sendMessage(CHAT_COMMAND);
		} else if (command.equalsIgnoreCase("diff")) {
			sender.sendMessage(DIFF_COMMAND);
		} else if (command.equalsIgnoreCase("playerdiff")) {
			sender.sendMessage(PLAYER_DIFF_COMMAND);
		} else {
			sender.sendMessage(String.format("Unknown command: %s.", command));
		}	
	}
}
