// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.authx.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.util.UuidUtils;
import java.util.UUID;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import sh.auxuji.authx.AuthX;

public class ChangeCommand implements SimpleCommand {
	private final AuthX plugin;

	public ChangeCommand(final AuthX plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(Invocation invocation) {
		CommandSource source = invocation.source();
		String[] args = invocation.arguments();

		if (args.length < 2) {
			source.sendMessage(Identity.nil(), Component.text("Usage: authx-change [username] [password]"));
			return;
		}

		UUID uuid = UuidUtils.fromUndashed(args[0]);
		String password = args[1];

		source.sendMessage(Identity.nil(), Component.text(String.format("authx-change: Change user %s", uuid)));
		this.plugin.getOfflineAuth().changeEntry(uuid, password);
	}

	@Override
	public boolean hasPermission(final Invocation invocation) {
		return invocation.source().hasPermission("auxuji-authx.change");
	}
}
