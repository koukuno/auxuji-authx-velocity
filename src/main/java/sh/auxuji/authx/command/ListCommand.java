// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.authx.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.util.UuidUtils;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import sh.auxuji.authx.AuthX;
import sh.auxuji.authx.hash.SHA256Context;
import sh.auxuji.authx.offline.OfflineAuthEntryJSON;

public class ListCommand implements SimpleCommand {
	private final AuthX plugin;

	public ListCommand(AuthX plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(final Invocation invocation) {
		CommandSource source = invocation.source();

		for (OfflineAuthEntryJSON entry : this.plugin.getOfflineAuth().getEntries())
			source.sendMessage(Identity.nil(), Component.text(String.format("authx-list: UUID:%s Username:%s", UuidUtils.toUndashed(entry.uuid), entry.username)));
	}

	@Override
	public boolean hasPermission(final Invocation invocation) {
		return invocation.source().hasPermission("auxuji-authx.list");
	}
}
