
import java.time.LocalDateTime;

class Patient {
    private String name;
    private String status; // 심박수, 혈압 등 문자열
    private LocalDateTime timestamp;

    public Patient(String name, String status) {
        this.name = name;
        this.status = status;
        this.timestamp = LocalDateTime.now(); // 현재 시간 자동 기록
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + name + ": " + status;
    }
}
