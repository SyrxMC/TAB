package me.neznamy.tab.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.bossbar.BossBarLine;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

/**
 * The primary API class to get instances of other API classes
 */
public class TABAPI {


	//placeholders registered via API
	public static Map<String, Placeholder> APIPlaceholders = new HashMap<String, Placeholder>();
	
	
	/**
	 * Returns player object from given UUID
	 * @return player object from given UUID
	 * @param id - Player UUID
	 * @since 2.8.3
	 */
	public static TabPlayer getPlayer(UUID id) {
		return TAB.getInstance().getPlayer(id);
	}
	
	
	/**
	 * Returns player object from given name
	 * @return player object from given name
	 * @param name - Player name
	 * @since 2.8.3
	 */
	public static TabPlayer getPlayer(String name) {
		return TAB.getInstance().getPlayer(name);
	}
	
	
	/**
	 * Returns true if enabled, false if disabled
	 * @return Whether unlimited nametag mode is enabled or not
	 * @see enableUnlimitedNameTagModePermanently
	 * @since 2.4.12
	 */
	public static boolean isUnlimitedNameTagModeEnabled() {
		return TAB.getInstance().getFeatureManager().isFeatureEnabled("nametagx");
	}


	/**
	 * Enables unlimited nametag mode permanently in config
	 * @throws IllegalStateException if called from a proxy
	 * @see isUnlimitedNameTagModeEnabled
	 * @since 2.4.12
	 */
	public static void enableUnlimitedNameTagModePermanently() {
		if (isUnlimitedNameTagModeEnabled()) return;
		TAB.getInstance().getConfiguration().config.set("change-nametag-prefix-suffix", true);
		TAB.getInstance().getConfiguration().config.set("unlimited-nametag-prefix-suffix-mode.enabled", true);
		TAB.getInstance().unload();
		TAB.getInstance().load();
	}


	/**
	 * Registers a player placeholder (placeholder with player-specific output)
	 * @param placeholder - Placeholder handler
	 * @since 2.6.5
	 * @see registerServerPlaceholder
	 * @see registerServerConstant
	 */
	public static void registerPlayerPlaceholder(PlayerPlaceholder placeholder) {
		APIPlaceholders.put(placeholder.getIdentifier(), placeholder);
		PlaceholderManager pl = TAB.getInstance().getPlaceholderManager();
		pl.registerPlaceholder(placeholder);
		pl.allUsedPlaceholderIdentifiers.add(placeholder.getIdentifier());
	}


	/**
	 * Registers a server placeholder (placeholder with same output for all players)
	 * @param placeholder - Placeholder handler
	 * @since 2.6.5
	 * @see registerPlayerPlaceholder
	 * @see registerServerConstant
	 */
	public static void registerServerPlaceholder(ServerPlaceholder placeholder) {
		APIPlaceholders.put(placeholder.getIdentifier(), placeholder);
		PlaceholderManager pl = TAB.getInstance().getPlaceholderManager();
		pl.registerPlaceholder(placeholder);
		pl.allUsedPlaceholderIdentifiers.add(placeholder.getIdentifier());
	}
	

	/**
	 * Registers a relational placeholder
	 * @param placeholder - Placeholder handler
	 * @since 2.8.0
	 */
	public static void registerRelationalPlaceholder(RelationalPlaceholder placeholder) {
		APIPlaceholders.put(placeholder.getIdentifier(), placeholder);
		PlaceholderManager pl = TAB.getInstance().getPlaceholderManager();
		pl.registerPlaceholder(placeholder);
		pl.allUsedPlaceholderIdentifiers.add(placeholder.getIdentifier());
	}


	@Deprecated
	public static Scoreboard createScoreboard(String title, List<String> lines) {
		return createScoreboard("Unnamed scoreboard", title, lines);
	}
	
	/**
	 * Creates a new scoreboard
	 * @param name - name of the scoreboard
	 * @param title - the scoreboard title
	 * @param lines - up to 15 lines of text (supports placeholders)
	 * @return The new scoreboard
	 * @since 2.8.8
	 */
	public static Scoreboard createScoreboard(String name, String title, List<String> lines) {
		for (String line : lines) {
			TAB.getInstance().getPlaceholderManager().checkForRegistration(line);
		}
		ScoreboardManager sbm = (ScoreboardManager) TAB.getInstance().getFeatureManager().getFeature("scoreboard");
		if (sbm == null) throw new IllegalStateException("Scoreboard feature is not enabled");
		Scoreboard sb = new me.neznamy.tab.shared.features.scoreboard.Scoreboard(sbm, name, title, lines);
		sbm.APIscoreboards.add(sb);
		return sb;
	}
	
	/**
	 * Registers a custom permission plugin
	 * @param permission - permission plugin provider
	 * @since 2.8.3
	 */
	public static void registerPermissionPlugin(PermissionPlugin permission) {
		TAB.getInstance().setPermissionPlugin(permission);
	}
	
	public static BossBar createBossBar(String name, String title, float progress, BarColor color, BarStyle style) {
		return createBossBar(name, title, progress+"", color.toString(), style.toString());
	}
	
	public static BossBar createBossBar(String name, String title, String progress, String color, String style) {
		me.neznamy.tab.shared.features.bossbar.BossBar feature = (me.neznamy.tab.shared.features.bossbar.BossBar) TAB.getInstance().getFeatureManager().getFeature("bossbar");
		if (feature == null) throw new IllegalStateException("Bossbar feature is not enabled");
		BossBar bar = new BossBarLine(TAB.getInstance(), name, null, color, style, title, progress);
		feature.lines.put(bar.getName(), (BossBarLine) bar);
		return bar;
	}
	
	/**
	 * Returns placeholders registered via API
	 * @return placeholders registered via API
	 */
	public static Map<String, Placeholder> getAPIPlaceholders(){
		return APIPlaceholders;
	}
}