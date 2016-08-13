package net.eithon.plugin.stats.logic;

import org.bukkit.command.CommandSender;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.PlayerException;
import net.eithon.library.exceptions.TryAgainException;

public class TryHandler {
	public static <T> T handleExceptions(CommandSender sender, ISupplier<T> provider){
		try {
			return provider.doIt();
			
		} catch (TryAgainException e) {
			sender.sendMessage(String.format("Try again later. (%s)", e.getMessage()));
			e.printStackTrace();
		} catch (FatalException e) {
			sender.sendMessage(String.format("Fatal error. (%s)", e.getMessage()));
			e.printStackTrace();
		} catch (PlayerException e) {
			sender.sendMessage(String.format("Player error. (%s)", e.getMessage()));
		} catch (Exception e) {
			sender.sendMessage(String.format("Unexpected error. (%s)", e.getMessage()));
			e.printStackTrace();
		}
		return null;
	}
	
	public static void handleExceptions(IExecutor provider){
		try {
			provider.doIt();
			
		} catch (TryAgainException e) {
			e.printStackTrace();
		} catch (FatalException e) {
			e.printStackTrace();
		} catch (PlayerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void handleExceptions(CommandSender sender, IExecutor provider){
		try {
			provider.doIt();
			
		} catch (TryAgainException e) {
			sender.sendMessage(String.format("Try again later. (%s)", e.getMessage()));
			e.printStackTrace();
		} catch (FatalException e) {
			sender.sendMessage(String.format("Fatal error. (%s)", e.getMessage()));
			e.printStackTrace();
		} catch (PlayerException e) {
			sender.sendMessage(String.format("Player error. (%s)", e.getMessage()));
		} catch (Exception e) {
			sender.sendMessage(String.format("Unexpected error. (%s)", e.getMessage()));
			e.printStackTrace();
		}
	}
}
