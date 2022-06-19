// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.authx.command;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.UuidUtils;
import java.util.UUID;
import java.util.Optional;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import sh.auxuji.authx.AuthX;

public class LoginCommand implements SimpleCommand {
	private final AuthX plugin;

	public LoginCommand(AuthX plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(final Invocation invocation) {
		CommandSource source = invocation.source();
		Player player = (Player) source;
		String[] args = invocation.arguments();

		if (player.isOnlineMode()) {
			source.sendMessage(Identity.nil(), Component.text("You are already a Premium player!"));
			return;
		}

		UUID uuid = player.getUniqueId();
		if (plugin.isPlayerLogged(uuid)) {
			source.sendMessage(Identity.nil(), Component.text("You are already logged in!"));
			return;
		}

		if (args.length < 1) {
			source.sendMessage(Identity.nil(), Component.text("Usage: authx-login [password]"));
			return;
		}

		String password = args[0];

		if (this.plugin.loginOfflinePlayer(uuid, password)) {
			source.sendMessage(Identity.nil(), Component.text("authx-login: Login successful!"));

			Optional<ServerConnection> server = player.getCurrentServer();

			// good to check right?
			if (server.isPresent())
				this.plugin.sendLoginMessage(server.get(), uuid);
		} else {
			source.sendMessage(Identity.nil(), Component.text("authx-login: Failed to login! Please request server operator to add or change password."));
			source.sendMessage(Identity.nil(), Component.text(String.format("authx-login: Your UUID: %s\n", UuidUtils.toUndashed(uuid))));
		}
	}

	@Override
	public boolean hasPermission(final Invocation invocation) {
//		return invocation.source().hasPermission("auxuji-authx.login");
		return invocation.source() instanceof Player;
	}
}
