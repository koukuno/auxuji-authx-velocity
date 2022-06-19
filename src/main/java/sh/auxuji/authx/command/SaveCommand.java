// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.authx.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import sh.auxuji.authx.AuthX;

public class SaveCommand implements SimpleCommand {
	private final AuthX plugin;

	public SaveCommand(AuthX plugin) {
		this.plugin = plugin;
	}

	@Override
	public void execute(final Invocation invocation) {
		CommandSource source = invocation.source();

		source.sendMessage(Identity.nil(), Component.text("authx-save: Save AuthX offline accounts"));
		this.plugin.getOfflineAuth().writeToDisk();
	}

	@Override
	public boolean hasPermission(final Invocation invocation) {
		return invocation.source().hasPermission("auxuji-authx.save");
	}
}
