package sh.auxuji.authx.hash;

import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Context {
	private MessageDigest md;

	public SHA256Context() {
		try {
			MessageDigest global = MessageDigest.getInstance("SHA-256");
			this.md = (MessageDigest) global.clone();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	// message is converted to UTF-8 for consistent hashing
	public byte[] hashMessage(String message) {
		byte[] mem = message.getBytes(StandardCharsets.UTF_8);

		byte[] hash = this.md.digest(mem);
		this.md.reset();

		return hash;
	}

	public static String hashToString(byte[] hash) {
		if (hash == null)
			return "";

		String hashStr = "";
		for (byte i : hash)
			hashStr += String.format("%02x", i);

		return hashStr;
	}
}
