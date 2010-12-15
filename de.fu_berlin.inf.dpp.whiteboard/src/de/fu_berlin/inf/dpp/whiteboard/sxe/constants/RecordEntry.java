package de.fu_berlin.inf.dpp.whiteboard.sxe.constants;

/**
 * <p>
 * Enumeration for all possible attributes of any record respective SXE. Used in
 * the RecordDataObject.
 * </p>
 * 
 * <p>
 * Each constant is enhanced by its XML tag to be used when serialized.
 * Accessible by the toString()-method.</br>
 * 
 * The static utility method fromString(String) facilitates deserialization.
 * </p>
 * 
 * @author jurke
 * 
 */
public enum RecordEntry {

	RID("rid"), TARGET("target"), TYPE("type"), VERSION("version"), RECORD_TYPE(
			"record-type"), TARGET_VERSION("target-version"), PARENT("parent"), PRIMARY_WEIGHT(
			"primary-weight"), NS("ns"), NAME("name"), CHDATA("chdata"), CREATOR(
			"creator"), LAST_MODIFIED_BY("last-modified-by");

	private String type;

	private RecordEntry(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

	public static RecordEntry fromString(String raw) {
		for (RecordEntry t : RecordEntry.values())
			if (t.toString().equals(raw))
				return t;
		return null;
	}
}
