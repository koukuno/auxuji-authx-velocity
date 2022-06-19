// Please read LICENSE.txt for licensing information. (GPLv2-only)
package sh.auxuji.authx.offline;

import java.util.ArrayList;
import java.util.List;

public class OfflineAuthEntriesJSON {
	public List<OfflineAuthEntryJSON> entries;

	public OfflineAuthEntriesJSON() {
		this.entries = new ArrayList<OfflineAuthEntryJSON>();
	}
}
