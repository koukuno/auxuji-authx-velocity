// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.authx.offline;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.velocitypowered.api.util.UuidUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import sh.auxuji.authx.AuthX;
import sh.auxuji.authx.PluginConfig;
import sh.auxuji.authx.hash.SHA256Context;

public class OfflineAuth {
	private final Logger logger;
	private final AuthX plugin;

	private OfflineAuthEntriesJSON entries;
	private File file;
	private Gson gson;
	private SHA256Context sha256ctx;

	public OfflineAuth(AuthX plugin) {
		this.logger = plugin.getLogger();
		this.plugin = plugin;

		this.gson = new GsonBuilder().create();

		// create a blank json file if the file does not exist
		this.file = new File(this.plugin.getConfig().getDataDir().toString(), this.plugin.getConfig().getOfflineAuthFileName());
		if (!this.file.exists()) {
			try {
				this.file.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(this.file));
				writer.write(this.gson.toJson(new OfflineAuthEntriesJSON()));
				writer.close();
			} catch (IOException e) {
				this.logger.warn(String.format("Cannot create %s in data directory (permissions?)", this.plugin.getConfig().getOfflineAuthFileName()));
			}
		}

		if (this.file.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(this.file));
				this.entries = this.gson.fromJson(reader, OfflineAuthEntriesJSON.class);
				reader.close();
			} catch (IOException e) {
				this.logger.warn(String.format("Cannot load %s (permissions?)", this.plugin.getConfig().getOfflineAuthFileName()));
			}
		}

		sha256ctx = new SHA256Context();
	}

	public void addEntry(String username, String password) {
		UUID uuid = UuidUtils.generateOfflinePlayerUuid(username);

		for (OfflineAuthEntryJSON entry : this.entries.entries) {
			if (entry.uuid.equals(uuid)) {
				this.logger.warn("Entry already exists!");
				return;
			}
		}

		this.logger.warn(String.format("addEntry: uuid:%s username:%s pwHash:%s", UuidUtils.generateOfflinePlayerUuid(username), username, SHA256Context.hashToString(sha256ctx.hashMessage(password))));
		this.entries.entries.add(new OfflineAuthEntryJSON(username, sha256ctx.hashMessage(password), UuidUtils.generateOfflinePlayerUuid(username)));
	}

	public void changeEntry(UUID uuid, String password) {
		for (OfflineAuthEntryJSON entry : this.entries.entries) {
			if (entry.uuid.equals(uuid)) {
				entry.pwHash = sha256ctx.hashMessage(password);
				break;
			}
		}
	}

	public boolean checkEntryAuth(UUID uuid, String password) {
		byte[] pwHash = sha256ctx.hashMessage(password);

		for (OfflineAuthEntryJSON entry : this.entries.entries) {
			if (entry.uuid.equals(uuid) && Arrays.equals(pwHash, entry.pwHash))
				return true;
		}

		return false;
	}

	public boolean checkEntryExists(UUID uuid) {
		for (OfflineAuthEntryJSON entry : this.entries.entries) {
			if (entry.uuid.equals(uuid))
				return true;
		}

		return false;
	}

	public void deleteEntry(UUID uuid) {
		for (OfflineAuthEntryJSON entry : this.entries.entries) {
			if (entry.uuid.equals(uuid)) {
				this.entries.entries.remove(entry);
				break;
			}
		}
	}

	public void writeToDisk() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(this.file));
			writer.write(this.gson.toJson(this.entries));
			writer.close();
		} catch (IOException e) {
			this.logger.warn(String.format("Cannot write to %s (permissions?)", this.plugin.getConfig().getOfflineAuthFileName()));
		}
	}

	public List<OfflineAuthEntryJSON> getEntries() {
		return this.entries.entries;
	}
}
