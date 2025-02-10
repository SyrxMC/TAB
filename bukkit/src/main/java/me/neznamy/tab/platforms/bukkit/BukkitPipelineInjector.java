package me.neznamy.tab.platforms.bukkit;

import io.netty.channel.Channel;
import lombok.Setter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * Pipeline injection for Bukkit 1.8+.
 */
public class BukkitPipelineInjector extends NettyPipelineInjector {

    /** Function for getting player's channel */
    @Setter
    private static FunctionWithException<BukkitTabPlayer, Channel> getChannel;

    /**
     * Constructs new instance
     */
    public BukkitPipelineInjector() {
        super("packet_handler");
    }

    /**
     * Attempts to load required classes, fields and methods and marks class as available.
     * If something fails, an error message is printed and class is not marked as available.
     */
    public static void tryLoad() {
        try {
            Class<?> NetworkManager = BukkitReflection.getClass("network.Connection", "network.NetworkManager", "NetworkManager");
            Class<?> PlayerConnection = BukkitReflection.getClass("server.network.ServerGamePacketListenerImpl",
                    "server.network.PlayerConnection", "PlayerConnection");
            Class<?> EntityPlayer = BukkitReflection.getClass("server.level.ServerPlayer", "server.level.EntityPlayer", "EntityPlayer");
            Field PLAYER_CONNECTION = ReflectionUtils.getOnlyField(EntityPlayer, PlayerConnection);
            Field NETWORK_MANAGER;
            if (BukkitReflection.is1_20_2Plus()) {
                NETWORK_MANAGER = ReflectionUtils.getOnlyField(PlayerConnection.getSuperclass(), NetworkManager);
            } else {
                NETWORK_MANAGER = ReflectionUtils.getOnlyField(PlayerConnection, NetworkManager);
            }
            Field CHANNEL = ReflectionUtils.getOnlyField(NetworkManager, Channel.class);
            getChannel = player -> (Channel) CHANNEL.get(NETWORK_MANAGER.get(PLAYER_CONNECTION.get(player.getHandle())));
        } catch (Exception e) {
            BukkitUtils.compatibilityError(e, "network channel injection", null,
                    "Anti-override for tablist & nametags not working",
                    "Compatibility with nickname plugins changing player names will not work",
                    "Scoreboard will not be checking for other plugins");
        }
    }

    /**
     * Returns {@code true} if pipeline injection is available, {@code false} if not.
     *
     * @return  {@code true} if pipeline injection is available, {@code false} if not
     */
    public static boolean isAvailable() {
        return getChannel != null;
    }

    @Override
    @NotNull
    @SneakyThrows
    protected Channel getChannel(@NotNull TabPlayer player) {
        return getChannel.apply((BukkitTabPlayer) player);
    }
}