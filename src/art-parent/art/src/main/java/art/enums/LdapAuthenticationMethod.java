package art.enums;

/**
 * Enum for supported ldap authentication methods
 *
 * @author Timothy Anyona
 */
public enum LdapAuthenticationMethod {

	Simple("Simple"), DigestMD5("Digest-MD5");
	private String value;

	private LdapAuthenticationMethod(String value) {
		this.value = value;
	}

	/**
	 * Get enum value
	 *
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Get enum object based on a string
	 *
	 * @param value
	 * @return
	 */
	public static LdapAuthenticationMethod getEnum(String value) {
		for (LdapAuthenticationMethod v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return Simple; //default
	}
}
