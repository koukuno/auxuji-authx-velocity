// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.authx;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import org.slf4j.Logger;

public class PluginConfig {
	// Increment if config file structure changes
	private static final long SUPPORTED_CONFIG_VERSION = 1;

	private final Logger logger;
	private final Path dataDir;

	private boolean offlineAllow;
	private String offlineAuthServer;
	private String filesOfflineAuth;

	@Inject
	public PluginConfig(Logger logger, @DataDirectory Path dataDir) {
		this.logger = logger;
		this.dataDir = dataDir;
		this.loadConfig();
	}

	// Load config and do basic config checks (the checks should not be comprehensive)
	public void loadConfig() {
		File dataDirFile = this.dataDir.toFile();
		if (!dataDirFile.exists())
			dataDirFile.mkdir();

		File configFile = new File(dataDirFile, "config.toml");
		if (!configFile.exists()) {
			try {
				// copy the default config from resources into plugin data directory
				InputStream defaultConfigStream = getClass().getResourceAsStream("/config.toml");
				Files.copy(defaultConfigStream, configFile.toPath());
			} catch (Exception e) {
				throw new RuntimeException("Cannot copy default config file!");
			}
		}

		Toml toml = new Toml().read(configFile);

		long configVersion = toml.getLong("config.version");
		if (configVersion != SUPPORTED_CONFIG_VERSION) {
			this.logger.warn(String.format("Unsupported config version: %ld (plugin only supports: %ld)", configVersion, SUPPORTED_CONFIG_VERSION));
			return;
		}

		Boolean offlineAllowValue = toml.getBoolean("offline.allow");
		if (offlineAllowValue == null) {
			this.offlineAllow = false;
		} else {
			this.offlineAllow = offlineAllowValue;
		}

		this.filesOfflineAuth = toml.getString("files.offlineAuth");
		if (this.filesOfflineAuth == null)
			this.logger.warn("Cannot read files.offlineAuth! Persistent storage is disabled for offline auth.");
	}

	public Path getDataDir() {
		return this.dataDir;
	}

	public boolean isOfflineAllowed() {
		return this.offlineAllow;
	}

	public String getOfflineAuthFileName() {
		return this.filesOfflineAuth;
	}
}
