// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.authx.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.util.UuidUtils;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import sh.auxuji.authx.AuthX;

public class AddCommand implements SimpleCommand {
	private final AuthX plugin;

	public AddCommand(AuthX plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(final Invocation invocation) {
		CommandSource source = invocation.source();
		String[] args = invocation.arguments();

		if (args.length < 2) {
			source.sendMessage(Identity.nil(), Component.text("Usage: authx-add [username] [password]"));
			return;
		}

		String username = args[0];
		String password = args[1];

		if (this.plugin.getOfflineAuth().checkEntryExists(UuidUtils.generateOfflinePlayerUuid(username))) {
			source.sendMessage(Identity.nil(), Component.text("authx-add: User already exists!"));
			return;
		}

		source.sendMessage(Identity.nil(), Component.text(String.format("authx-add: Add user %s", username)));
		this.plugin.getOfflineAuth().addEntry(username, password);
	}

	@Override
	public boolean hasPermission(final Invocation invocation) {
		return invocation.source().hasPermission("auxuji-authx.add");
	}
}
