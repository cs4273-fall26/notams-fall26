public class Notam{ 
    private String notamId;
    private String notamText;
    private String location;
    private String startTime;
    private String endTime;
    
    public Notam(String notamId, String notamText, String location, String startTime, String endTime) {
        this.notamId = notamId;
        this.notamText = notamText;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
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
}

