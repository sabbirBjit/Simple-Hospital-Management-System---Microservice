package com.hms.appointment.model;

public enum AppointmentType {
    CONSULTATION("Consultation"),
    FOLLOW_UP("Follow-up"),
    EMERGENCY("Emergency"),
    CHECK_UP("Check-up"),
    PROCEDURE("Procedure"),
    SURGERY("Surgery"),
    VACCINATION("Vaccination"),
    THERAPY("Therapy");
    
    private final String displayName;
    
    AppointmentType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
