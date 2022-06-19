// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.authx;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.util.UuidUtils;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.geysermc.floodgate.api.FloodgateApi;
import sh.auxuji.authx.command.*;
import sh.auxuji.authx.listener.ProxyListener;
import sh.auxuji.authx.offline.OfflineAuth;

@Plugin(
	id = "auxuji-authx",
	name = "AuthX",
	version = "1.0-SNAPSHOT",
	description = "Authorization Plugin for Velocity",
	authors = {"koukuno"},
	dependencies = {
		@Dependency(id = "floodgate")
	}
)	
public class AuthX {
	public static final ChannelIdentifier CHANNEL = MinecraftChannelIdentifier.create("authxv", "main");

	private final Logger logger;
	private final ProxyServer server;
	private final Path dataDir;

	private final EventManager eventManager;
	private final CommandManager commandManager;
	private final PluginManager pluginManager;

	private PluginConfig config;
	private OfflineAuth offlineAuth;
	private Set<UUID> loggedPlayers;

	private FloodgateApi floodgateInstance;

	@Inject
	public AuthX(
		ProxyServer server,
		Logger logger,
		EventManager eventManager,
		CommandManager commandManager,
		PluginManager pluginManager,
		@DataDirectory Path dataDir
	) {
		this.logger = logger;
		this.server = server;
		this.eventManager = eventManager;
		this.commandManager = commandManager;
		this.pluginManager = pluginManager;
		this.dataDir = dataDir;
		this.loggedPlayers = new HashSet<UUID>();
	}

	public CommandMeta buildCommandMeta(String command) {
		return this.commandManager.metaBuilder(command).aliases("/" + command).build();
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
//		this.luckPerms = LuckPermsProvider.get();

		this.config = new PluginConfig(this.logger, this.dataDir);
		this.offlineAuth = new OfflineAuth(this);

		// this plugin only needs to send messages to the backend servers
		this.server.getChannelRegistrar().register(CHANNEL);

		this.eventManager.register(this, new ProxyListener(this));

		this.commandManager.register(this.buildCommandMeta("authx-add"), new AddCommand(this));
		this.commandManager.register(this.buildCommandMeta("authx-change"), new ChangeCommand(this));
		this.commandManager.register(this.buildCommandMeta("authx-delete"), new DeleteCommand(this));
		this.commandManager.register(this.buildCommandMeta("authx-list"), new ListCommand(this));
		this.commandManager.register(this.buildCommandMeta("authx-login"), new LoginCommand(this));
		this.commandManager.register(this.buildCommandMeta("authx-reload"), new ReloadCommand(this));
		this.commandManager.register(this.buildCommandMeta("authx-save"), new SaveCommand(this));

		this.floodgateInstance = FloodgateApi.getInstance();

		this.logger.info("|| Auxuji AuthX Initialized ||");
	}

	@Subscribe
	public void onShutdown(ProxyShutdownEvent event) {
		this.offlineAuth.writeToDisk();
	}

	public void reloadConfig() {
		this.offlineAuth.writeToDisk();
		this.config.loadConfig();
	}

	public PluginConfig getConfig() {
		return this.config;
	}

	public Logger getLogger() {
		return this.logger;
	}

	public OfflineAuth getOfflineAuth() {
		return this.offlineAuth;
	}

	public FloodgateApi getFloodgateInstance() {
		return this.floodgateInstance;
	}

	// XUID -> UUID?
	public boolean loginFloodgatePlayer(UUID uuid) {
		return this.loggedPlayers.add(uuid);
	}

	public boolean loginOfflinePlayer(UUID uuid, String password) {
		if (offlineAuth.checkEntryAuth(uuid, password))
			return this.loggedPlayers.add(uuid);

		return false;
	}

	public boolean loginPremiumPlayer(UUID uuid) {
		return this.loggedPlayers.add(uuid);
	}

	public boolean logoutPlayer(UUID uuid) {
		return this.loggedPlayers.remove(uuid);
	}

	public boolean isPlayerLogged(UUID uuid) {
		return this.loggedPlayers.contains(uuid);
	}

	public void sendLoginMessage(ServerConnection server, UUID uuid) {
		ByteArrayDataOutput stream = ByteStreams.newDataOutput();
		stream.writeUTF(uuid.toString());
		stream.writeUTF("login");
		server.sendPluginMessage(AuthX.CHANNEL, stream.toByteArray());
	}
}
