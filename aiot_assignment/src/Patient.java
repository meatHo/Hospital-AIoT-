package aiot_assignment.src;

import java.util.*;

public class Patient {
    private String patient_ID;
    private String patient_Name;
    private ArrayList<Device> devices = new ArrayList<>();

    // Getter and Setter for patient_ID
    public String getPatient_ID() {
        return patient_ID;
    }

    private void setPatient_ID(String patient_ID) {
        this.patient_ID = patient_ID;
    }

    // Getter and Setter for patient_Name
    public String getPatient_Name() {
        return patient_Name;
    }

    private void setPatient_Name(String patient_Name) {
        this.patient_Name = patient_Name;
    }

    // Getter and Setter for devices
    public ArrayList<Device> getDevices() {
        return devices;
    }

    private void setDevices(ArrayList<Device> devices) {
        this.devices = devices;
    }
}
