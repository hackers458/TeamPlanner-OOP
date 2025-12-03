import java.time.LocalDate;
import java.util.List;

/**
 * 일반 일정 클래스 (Single Responsibility Principle)
 * 하나의 구체적인 날짜와 시간에 대한 일정만 관리
 */
public class Schedule extends BaseSchedule {
    private int year, month, day;
    private int startHour, startMinute, endHour, endMinute;
    private boolean check;

    /**
     * 일반 일정 생성자
     */
    public Schedule(int year, int month, int day, int startHour, int startMinute,
                    int endHour, int endMinute, String todo) {
        this(year, month, day, startHour, startMinute, endHour, endMinute, todo, null);
    }

    /**
     * 반복 일정에서 파생된 일정 생성자
     */
    public Schedule(int year, int month, int day, int startHour, int startMinute,
                    int endHour, int endMinute, String todo, String fromRepeatId) {
        super(todo, fromRepeatId);
        this.year = year;
        this.month = month;
        this.day = day;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.check = false;
    }

    @Override
    public List<Schedule> getOccurrencesOn(LocalDate date) {
        if (getDate().equals(date)) {
            return List.of(this);
        }
        return List.of();
    }

    @Override
    public ScheduleType getScheduleType() {
        return ScheduleType.REGULAR;
    }

    public LocalDate getDate() {
        return LocalDate.of(year, month, day);
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d ~ %02d:%02d  %s",
                startHour, startMinute, endHour, endMinute, todo);
    }

    // Getters and Setters
    public boolean completed() { return check; }
    public boolean setCompleted(boolean check) { return this.check = check; }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDay() { return day; }
    public int getStartHour() { return startHour; }
    public int getStartMinute() { return startMinute; }
    public int getEndHour() { return endHour; }
    public int getEndMinute() { return endMinute; }
    public boolean isChecked() { return check; }
}