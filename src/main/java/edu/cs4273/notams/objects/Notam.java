package edu.cs4273.notams.objects;

public class Notam {
	private String notamId;
	private String notamText;
	private String location;
	private String startTime;
	private String endTime;
	private String number;
	private String classification;

	public Notam(String notamId, String notamText, String location,
				 String startTime, String endTime) {
		this(notamId, notamText, location, startTime, endTime, "", "");
	}

	public Notam(String notamId, String notamText, String location,
				 String startTime, String endTime, String number,
				 String classification) {
		this.notamId = notamId;
		this.notamText = notamText;
		this.location = location;
		this.startTime = startTime;
		this.endTime = endTime;
		this.number = number;
		this.classification = classification;
	}

	public String getNotamId() {
		return notamId;
	}

	public String getNotamText() {
		return notamText;
	}

	public String getLocation() {
		return location;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getNumber() {
		return number;
	}

	public String getClassification() {
		return classification;
	}

	private String type = "";
	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}
	public boolean isCancellation() {
		return "C".equals(type);
	}
}