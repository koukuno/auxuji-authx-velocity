// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.authx.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.util.UuidUtils;
import java.util.UUID;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import sh.auxuji.authx.AuthX;

public class DeleteCommand implements SimpleCommand {
	private final AuthX plugin;

	public DeleteCommand(AuthX plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(final Invocation invocation) {
		CommandSource source = invocation.source();
		String[] args = invocation.arguments();

		if (args.length < 1) {
			source.sendMessage(Identity.nil(), Component.text("Usage: authx-delete [username]"));
			return;
		}

		UUID uuid = UuidUtils.fromUndashed(args[0]);

		source.sendMessage(Identity.nil(), Component.text(String.format("authx-delete: Delete user %s", uuid)));
		this.plugin.getOfflineAuth().deleteEntry(uuid);
	}

	@Override
	public boolean hasPermission(final Invocation invocation) {
		return invocation.source().hasPermission("auxuji-authx.delete");
	}
}
