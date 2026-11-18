package edu.cs4273.notams.objects;

// Number, classification and type are empty when the FAA response
// does not include them.
public record Notam(String notamId, String notamText, String location,
					String startTime, String endTime, String number,
					String classification, String type) {

	public Notam(String notamId, String notamText, String location,
				 String startTime, String endTime) {
		this(notamId, notamText, location, startTime, endTime, "", "", "");
	}

	public Notam(String notamId, String notamText, String location,
				 String startTime, String endTime, String number,
				 String classification) {
		this(notamId, notamText, location, startTime, endTime, number,
				classification, "");
	}

	public boolean isCancellation() {
		return "C".equals(type);
	}
}