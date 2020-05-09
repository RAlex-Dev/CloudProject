package pkg.model;

import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;

public class DateTime {

    private int index;
    private int hour;
    private int min;
    private String timeOfDay;
    private LocalDate localDate;
    private SimpleStringProperty dayTimeFormat;


    public DateTime(int hour, int min, String timeOfDay, LocalDate localDate) {
        this.hour = hour;
        this.min = min;
        this.timeOfDay = timeOfDay;
        this.localDate = localDate;
    }

    public DateTime() {
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setDayTimeFormat() {
        String tempString = hour + ": " + min + " " + timeOfDay + " on " + localDate;
        this.dayTimeFormat = new SimpleStringProperty(tempString);
    }

    public String getDayTimeFormat() {
        return dayTimeFormat.get();
    }

    public SimpleStringProperty dayTimeFormatProperty() {
        return dayTimeFormat;
    }

    public int getHour() {
        return hour;
    }

    public int getMin() {
        return min;
    }

    public String getTimeOfDay() {
        return timeOfDay;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }
}
