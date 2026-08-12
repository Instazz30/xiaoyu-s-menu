package com.xiaxiaoyu.xingbangmenu.dto;

public class InspectionUpdateRequest {

    private String location;
    private String reason;
    private String measure;

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getMeasure() { return measure; }
    public void setMeasure(String measure) { this.measure = measure; }
}
