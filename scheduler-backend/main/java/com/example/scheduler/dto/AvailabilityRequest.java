package com.example.scheduler.dto;

public class AvailabilityRequest {
    private Integer dayOfWeek; // 1=週一 ~ 5=週五
    private Integer period;    // 1=第1節 ~ 8=第8節

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Integer getPeriod() { return period; }
    public void setPeriod(Integer period) { this.period = period; }
}
