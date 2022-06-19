// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.authx.listener;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import sh.auxuji.authx.AuthX;

public class ProxyListener {
	private final AuthX plugin;

	private HttpClient client;

	public ProxyListener(AuthX plugin) {
		this.plugin = plugin;
		this.client = HttpClient.newHttpClient();
	}

	public void sendBlockedMessage(Player player) {
		player.sendMessage(Component.text("Access Denied!"));
	}

	@Subscribe(order = PostOrder.FIRST)
	public void onCommandExecute(CommandExecuteEvent event, Continuation continuation) {
		if (!(event.getCommandSource() instanceof Player)) {
			continuation.resume();
			return;
		}

		Player player = (Player) event.getCommandSource();
		if (this.plugin.getConfig().isOfflineAllowed() && this.plugin.isPlayerLogged(player.getUniqueId())) {
			continuation.resume();
			return;
		}

		if (event.getCommand().startsWith("authx-login")) {
			continuation.resume();
			return;
		}

		this.sendBlockedMessage(player);
		event.setResult(CommandExecuteEvent.CommandResult.denied());

		continuation.resume();
	}

	@Subscribe
	public EventTask onDisconnect(DisconnectEvent event) {
		return EventTask.async(() -> this.plugin.logoutPlayer(event.getPlayer().getUniqueId()));
	}

	@Subscribe(order = PostOrder.FIRST)
	public void onGameProfileRequest(GameProfileRequestEvent event) {
		if (this.plugin.getFloodgateInstance().isFloodgatePlayer(event.getOriginalProfile().getId())) {
			this.plugin.loginFloodgatePlayer(event.getOriginalProfile().getId());
			return;
		}
	}

	@Subscribe(order = PostOrder.FIRST)
	public void onPlayerChat(PlayerChatEvent event) {
		if (!this.plugin.isPlayerLogged(event.getPlayer().getUniqueId()))
			event.setResult(PlayerChatEvent.ChatResult.denied());
	}

	@Subscribe(order = PostOrder.LAST)
	public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event, Continuation continuation) {
		Player player = event.getPlayer();
		if (player.isOnlineMode()) {
			this.plugin.getLogger().info(String.format("Premium player joined: %s [%s]", player.getUsername(), player.getUniqueId()));
			this.plugin.loginPremiumPlayer(player.getUniqueId());

			Optional<ServerConnection> server = player.getCurrentServer();

			if (server.isPresent())
				this.plugin.sendLoginMessage(server.get(), player.getUniqueId());

			continuation.resume();
			return;
		}

		if (!this.plugin.getConfig().isOfflineAllowed()) {
			player.disconnect(Component.text("Cannot join Auth server!"));
			continuation.resume();
			return;
		}

		continuation.resume();
	}

	@Subscribe
	public void onPreLogin(PreLoginEvent event) {
		String usernamelower = event.getUsername().toLowerCase(Locale.ROOT);

		try {
			int statusCode = this.client.send(HttpRequest.newBuilder().uri(URI.create(String.format("https://api.mojang.com/users/profiles/minecraft/%s", URLEncoder.encode(usernamelower, StandardCharsets.UTF_8)))).build(), HttpResponse.BodyHandlers.ofString()).statusCode();
			boolean premium = statusCode == 200;

			if (premium) {
				event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
			} else if (statusCode != 429) {
				if (this.plugin.getConfig().isOfflineAllowed()) {
					event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
					return;
				}

				event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("Cannot join Auth server!")));
			}
		} catch (Exception e) {
			event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("Error while authenticating! Please try again later")));
		}
	}

	@Subscribe(order = PostOrder.FIRST)
	public EventTask onTabComplete(TabCompleteEvent event) {
		return EventTask.async(() -> {
			if (!this.plugin.isPlayerLogged(event.getPlayer().getUniqueId()))
				event.getSuggestions().clear();
		});
	}

	@Subscribe(order = PostOrder.EARLY)
	public void onServerPreConnect(ServerPreConnectEvent event, Continuation continuation) {
		Player player = event.getPlayer();

		if (this.plugin.getConfig().isOfflineAllowed()) {
			event.setResult(ServerPreConnectEvent.ServerResult.allowed(event.getOriginalServer()));
			continuation.resume();
			return;
		}

		event.setResult(ServerPreConnectEvent.ServerResult.denied());
		continuation.resume();
	}

	@Subscribe
	public void onServerPostConnect(ServerPostConnectEvent event) {
		Player player = event.getPlayer();
		if (!this.plugin.isPlayerLogged(player.getUniqueId()))
			return;

		Optional<ServerConnection> server = player.getCurrentServer();

		// good to check right?
		if (server.isPresent())
			this.plugin.sendLoginMessage(server.get(), player.getUniqueId());
	}
}

