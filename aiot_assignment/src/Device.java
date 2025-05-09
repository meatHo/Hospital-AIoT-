package aiot_assignment.src;

public class Device {
    private int device_ID;
    private int device_Name;
    private String data;// ex 심장박동
    private String value;// ex 120bpm

    // Getter and Setter for device_ID
    public int getDevice_ID() {
        return device_ID;
    }

    public void setDevice_ID(int device_ID) {
        this.device_ID = device_ID;
    }

    // Getter and Setter for device_Name
    public int getDevice_Name() {
        return device_Name;
    }

    public void setDevice_Name(int device_Name) {
        this.device_Name = device_Name;
    }

    // Getter and Setter for data
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    // Getter and Setter for value
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
