// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.authx.offline;

import java.util.UUID;

// For serializing/deserializing into JSON.
public class OfflineAuthEntryJSON {
	public String username;
	public byte[] pwHash;
	public UUID uuid;

	public OfflineAuthEntryJSON(String username, byte[] pwHash, UUID uuid) {
		this.username = username;
		this.pwHash = pwHash;
		this.uuid = uuid;
	}
}
