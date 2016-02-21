package net.eithon.plugin.stats;

import net.eithon.library.command.CommandSyntaxException;
import net.eithon.library.command.EithonCommand;
import net.eithon.library.command.EithonCommandUtilities;
import net.eithon.library.command.ICommandSyntax;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.stats.logic.Controller;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler {
	private EithonPlugin _eithonPlugin = null;
	private Controller _controller;
	private ICommandSyntax _commandSyntax;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._eithonPlugin = eithonPlugin;
		this._controller = controller;

		ICommandSyntax commandSyntax = EithonCommand.createRootCommand("estats");
		commandSyntax.setPermissionsAutomatically();

		try {
			setupForCommand(commandSyntax);
			setupStartCommand(commandSyntax);
			setupStopCommand(commandSyntax);
			setupResetCommand(commandSyntax);
			setupAddCommand(commandSyntax);
			setupRemoveCommand(commandSyntax);
			setupTimeCommand(commandSyntax);
			setupBlocksCommand(commandSyntax);
			setupChatCommand(commandSyntax);
			setupStatusCommand(commandSyntax);
			setupDiffCommand(commandSyntax);
			setupWhoCommand(commandSyntax);
			setupPlayerDiffCommand(commandSyntax);
			setupAfkCommand(commandSyntax);
			setupSaveCommand(commandSyntax);
		} catch (CommandSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this._commandSyntax = commandSyntax;
	}

	void disable() {
	}

	public ICommandSyntax getCommandSyntax() { return this._commandSyntax;	}

	private String getSenderName(EithonCommand command) {
		return command.getSender().getName();
	}

	private ICommandSyntax setupPlayerCommand(ICommandSyntax commandSyntax, String commandName) throws CommandSyntaxException {
		ICommandSyntax cmd = commandSyntax.parseCommandSyntax(commandName + " <player>");
		cmd
		.getParameterSyntax("player")
		.setMandatoryValues(ec -> EithonCommandUtilities.getOfflinePlayerNames(ec))
		.setDefaultGetter(ec -> getSenderName(ec));
		return cmd;
	}

	private void setupForCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		setupPlayerCommand(commandSyntax, "for")
		.setCommandExecutor(eithonCommand -> playerCommand(eithonCommand));
	}

	private void setupStartCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		setupPlayerCommand(commandSyntax, "start")
				.setCommandExecutor(eithonCommand -> startCommand(eithonCommand));
	}

	private void setupStopCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		setupPlayerCommand(commandSyntax, "stop")
				.setCommandExecutor(eithonCommand -> stopCommand(eithonCommand));
	}

	private void setupResetCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		setupPlayerCommand(commandSyntax, "reset")
				.setCommandExecutor(eithonCommand -> resetCommand(eithonCommand));
	}

	private ICommandSyntax setupAddRemoveCommand(ICommandSyntax commandSyntax, String commandName) throws CommandSyntaxException {
		// buy <player> <item> <price> [<amount>]
		ICommandSyntax cmd = commandSyntax.parseCommandSyntax(commandName + " <player> <time : TIME_SPAN>" + 
				" <consecutivedays : INTEGER {_0_,...}>" + 
				" <created : INTEGER {_0_,...}>" + 
				" <broken : INTEGER {_0_,...}>");
		cmd
		.getParameterSyntax("player")
		.setMandatoryValues(ec -> EithonCommandUtilities.getOnlinePlayerNames(ec));
		return cmd;
	}

	private void setupAddCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		setupAddRemoveCommand(commandSyntax, "add")
				.setCommandExecutor(eithonCommand -> addCommand(eithonCommand));
	}

	private void setupRemoveCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		setupAddRemoveCommand(commandSyntax, "remove")
				.setCommandExecutor(eithonCommand -> removeCommand(eithonCommand));
	}

	private void setupWhoCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		commandSyntax.parseCommandSyntax("who")
				.setCommandExecutor(eithonCommand -> whoCommand(eithonCommand));
	}

	private void setupSaveCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		commandSyntax.parseCommandSyntax("save")
				.setCommandExecutor(eithonCommand -> saveCommand(eithonCommand));
	}

	private ICommandSyntax setupListCommand(ICommandSyntax commandSyntax, String commandName) throws CommandSyntaxException {
		// buy <player> <item> <price> [<amount>]
		ICommandSyntax cmd = commandSyntax.parseCommandSyntax(commandName + "<direction {_desc_,asc}> <max-items : INTEGER {_0_, ...}>");
		return cmd;
	}

	private void setupTimeCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		setupListCommand(commandSyntax, "time")
				.setCommandExecutor(eithonCommand -> timeCommand(eithonCommand));
	}

	private void setupBlocksCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		setupListCommand(commandSyntax, "blocks")
				.setCommandExecutor(eithonCommand -> blocksCommand(eithonCommand));
	}

	private void setupChatCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		setupListCommand(commandSyntax, "chat")
				.setCommandExecutor(eithonCommand -> chatCommand(eithonCommand));
	}

	private void setupStatusCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		setupListCommand(commandSyntax, "status")
				.setCommandExecutor(eithonCommand -> statusCommand(eithonCommand));
	}

	private void setupDiffCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		commandSyntax.parseCommandSyntax("diff <days-back : INTEGER {_7_, 14, 30, ...}> <direction {_desc_,asc}> <max-items : INTEGER {_0_, ...}>")
				.setCommandExecutor(eithonCommand -> diffCommand(eithonCommand));
	}

	private void setupPlayerDiffCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		ICommandSyntax cmd = commandSyntax.parseCommandSyntax("playerdiff <player> <days-back : INTEGER {_7_,14,30,...}>")
				.setCommandExecutor(eithonCommand -> playerDiffCommand(eithonCommand));
		cmd
		.getParameterSyntax("player")
		.setMandatoryValues(ec -> EithonCommandUtilities.getOnlinePlayerNames(ec))
		.setDefaultGetter(ec -> getSenderName(ec));
	}

	private void setupAfkCommand(ICommandSyntax commandSyntax) throws CommandSyntaxException {
		commandSyntax.parseCommandSyntax("afk <description : REST>")
				.setCommandExecutor(eithonCommand -> awayFromKeyboardCommand(eithonCommand));
	}

	void playerCommand(EithonCommand eithonCommand)
	{
		EithonPlayer eithonPlayer = eithonCommand.getArgument("player").asEithonPlayer();

		this._controller.showStats(eithonCommand.getSender(), eithonPlayer);
	}

	void addCommand(EithonCommand eithonCommand)
	{
		EithonPlayer eithonPlayer = eithonCommand.getArgument("player").asEithonPlayer();
		CommandSender sender = eithonCommand.getSender();
		long time = eithonCommand.getArgument("time").asSeconds();
		if (time > 0) addTime(time, sender, eithonPlayer);
		int consecutiveDays = eithonCommand.getArgument("consecutivedays").asInteger();
		if (consecutiveDays > 0)
			addConsecutiveDays(consecutiveDays, sender, eithonPlayer);
		int createdBlocks = eithonCommand.getArgument("created").asInteger();
		if (createdBlocks > 0)
			addPlacedBlocks(createdBlocks, sender, eithonPlayer);
		int brokenBlocks = eithonCommand.getArgument("broken").asInteger();
		if (brokenBlocks > 0)
			addBrokenBlocks(brokenBlocks, sender, eithonPlayer);
	}

	private boolean addTime(long playTimeInSeconds, CommandSender sender, EithonPlayer eithonPlayer) {
		long totalPlayTimeInSeconds = this._controller.addPlayTime(sender, eithonPlayer, playTimeInSeconds);
		Config.M.playTimeAdded.sendMessage(
				sender,
				TimeMisc.secondsToString(playTimeInSeconds),
				eithonPlayer.getName(), 
				TimeMisc.secondsToString(totalPlayTimeInSeconds));
		return true;
	}

	private boolean addConsecutiveDays(int consecutiveDays, CommandSender sender, EithonPlayer eithonPlayer) {
		long totalConsecutiveDays = this._controller.addConsecutiveDays(sender, eithonPlayer, consecutiveDays);
		Config.M.consecutiveDaysAdded.sendMessage(
				sender,
				consecutiveDays,
				eithonPlayer.getName(), 
				totalConsecutiveDays);
		return true;
	}

	private boolean addPlacedBlocks(int createdBlocks, CommandSender sender, EithonPlayer eithonPlayer) {
		long totalPlacedBlocks = this._controller.addPlacedBlocks(sender, eithonPlayer, createdBlocks);
		Config.M.placedBlocksAdded.sendMessage(
				sender,
				createdBlocks,
				eithonPlayer.getName(), 
				totalPlacedBlocks);
		return true;
	}

	private boolean addBrokenBlocks(int brokenBlocks, CommandSender sender, EithonPlayer eithonPlayer) {
		long totalBrokenBlocks = this._controller.addBrokenBlocks(sender, eithonPlayer, brokenBlocks);
		Config.M.brokenBlocksAdded.sendMessage(
				sender,
				brokenBlocks,
				eithonPlayer.getName(), 
				totalBrokenBlocks);
		return true;
	}

	void removeCommand(EithonCommand eithonCommand)
	{
		EithonPlayer eithonPlayer = eithonCommand.getArgument("player").asEithonPlayer();
		CommandSender sender = eithonCommand.getSender();
		long time = eithonCommand.getArgument("time").asSeconds();
		if (time > 0) removeTime(time, sender, eithonPlayer);
		int consecutiveDays = eithonCommand.getArgument("consecutivedays").asInteger();
		if (consecutiveDays > 0)
			removeConsecutiveDays(consecutiveDays, sender, eithonPlayer);
		int createdBlocks = eithonCommand.getArgument("created").asInteger();
		if (createdBlocks > 0)
			removePlacedBlocks(createdBlocks, sender, eithonPlayer);
		int brokenBlocks = eithonCommand.getArgument("broken").asInteger();
		if (brokenBlocks > 0)
			removeBrokenBlocks(brokenBlocks, sender, eithonPlayer);
	}

	public boolean removeTime(long playTimeInSeconds, CommandSender sender, EithonPlayer eithonPlayer) {
		long totalPlayTimeInSeconds = this._controller.addPlayTime(sender, eithonPlayer, -playTimeInSeconds);
		Config.M.playTimeRemoved.sendMessage(
				sender,
				TimeMisc.secondsToString(playTimeInSeconds),
				eithonPlayer.getName(), 
				TimeMisc.secondsToString(totalPlayTimeInSeconds));
		return true;
	}

	public boolean removeConsecutiveDays(int consecutiveDays, CommandSender sender, EithonPlayer eithonPlayer) {
		long totalConsecutiveDays = this._controller.addConsecutiveDays(sender, eithonPlayer, -consecutiveDays);
		Config.M.consecutiveDaysRemoved.sendMessage(
				sender,
				consecutiveDays,
				eithonPlayer.getName(), 
				totalConsecutiveDays);
		return true;
	}

	public boolean removePlacedBlocks(int createdBlocks, CommandSender sender, EithonPlayer eithonPlayer) {
		long totalPlacedBlocks = this._controller.addPlacedBlocks(sender, eithonPlayer, -createdBlocks);
		Config.M.placedBlocksRemoved.sendMessage(
				sender,
				createdBlocks,
				eithonPlayer.getName(), 
				totalPlacedBlocks);
		return true;
	}

	public boolean removeBrokenBlocks(int brokenBlocks, CommandSender sender, EithonPlayer eithonPlayer) {
		long totalBrokenBlocks = this._controller.addBrokenBlocks(sender, eithonPlayer, -brokenBlocks);
		Config.M.brokenBlocksRemoved.sendMessage(
				sender,
				brokenBlocks,
				eithonPlayer.getName(), 
				totalBrokenBlocks);
		return true;
	}

	void resetCommand(EithonCommand eithonCommand)
	{
		EithonPlayer eithonPlayer = eithonCommand.getArgument("player").asEithonPlayer();

		this._controller.resetPlayTime(eithonCommand.getSender(), eithonPlayer);
		Config.M.playTimeReset.sendMessage(
				eithonCommand.getSender(),
				eithonPlayer.getName());
	}

	void startCommand(EithonCommand eithonCommand)
	{
		Player player = eithonCommand.getArgument("player").asPlayer();

		this._controller.startPlayer(player);
		Config.M.playerStarted.sendMessage(eithonCommand.getSender(), player.getName());
	}

	void stopCommand(EithonCommand eithonCommand)
	{
		Player player = eithonCommand.getArgument("player").asPlayer();

		this._controller.stopPlayer(player, Config.M.inactivityDetected.getMessage());
		Config.M.playerStopped.sendMessage(eithonCommand.getSender(), player.getName());
	}

	private void awayFromKeyboardCommand(EithonCommand eithonCommand) {
		String description = eithonCommand.getArgument("description").asString();
		if ((description == null) || description.isEmpty()) description = Config.M.defaultAfkDescription.getMessage();

		this._controller.stopPlayer(eithonCommand.getPlayer(), description);
	}

	void saveCommand(EithonCommand eithonCommand)
	{
		this._controller.save();
		Config.M.saved.sendMessage(eithonCommand.getSender());
	}

	void whoCommand(EithonCommand eithonCommand)
	{
		this._controller.who(eithonCommand.getSender());
	}

	void timeCommand(EithonCommand eithonCommand)
	{
		String direction = eithonCommand.getArgument("direction").asString();
		boolean ascending = direction.equalsIgnoreCase("asc");

		int maxItems = eithonCommand.getArgument("max-items").asInteger();

		this._controller.showTimeStats(eithonCommand.getSender(), ascending, maxItems);
	}

	void blocksCommand(EithonCommand eithonCommand)
	{
		String direction = eithonCommand.getArgument("direction").asString();
		boolean ascending = direction.equalsIgnoreCase("asc");

		int maxItems = eithonCommand.getArgument("max-items").asInteger();

		this._controller.showBlocksStats(eithonCommand.getSender(), ascending, maxItems);
	}

	void chatCommand(EithonCommand eithonCommand)
	{
		String direction = eithonCommand.getArgument("direction").asString();
		boolean ascending = direction.equalsIgnoreCase("asc");

		int maxItems = eithonCommand.getArgument("max-items").asInteger();

		this._controller.showChatStats(eithonCommand.getSender(), ascending, maxItems);
	}

	void diffCommand(EithonCommand eithonCommand)
	{
		int daysBack = eithonCommand.getArgument("days-back").asInteger();
		String direction = eithonCommand.getArgument("direction").asString();
		boolean ascending = direction.equalsIgnoreCase("asc");

		int maxItems = eithonCommand.getArgument("max-items").asInteger();

		this._controller.showDiffStats(eithonCommand.getSender(), daysBack, ascending, maxItems);
	}

	void playerDiffCommand(EithonCommand eithonCommand)
	{
		EithonPlayer player = eithonCommand.getArgument("player").asEithonPlayer();
		int daysBack = eithonCommand.getArgument("days-back").asInteger();

		this._controller.showDiffStats(eithonCommand.getSender(), player, daysBack);
	}

	void statusCommand(EithonCommand eithonCommand)
	{
		String direction = eithonCommand.getArgument("direction").asString();
		boolean ascending = direction.equalsIgnoreCase("asc");

		int maxItems = eithonCommand.getArgument("max-items").asInteger();
		
		this._controller.showAfkStatus(eithonCommand.getSender(), ascending, maxItems);
	}
}
