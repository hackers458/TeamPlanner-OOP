import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 반복 일정 패턴 클래스 (Single Responsibility Principle)
 * 반복 규칙만 관리하고, 구체적인 일정 생성은 toConcrete() 메서드로 위임
 */
public class RepeatSchedule extends BaseSchedule {
    private final DayOfWeek dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final LocalDate baseDate;

    // 시간 정보를 부모 Schedule 형태로 저장 (호환성 유지)
    private final int year, month, day;
    private final int startHour, startMinute, endHour, endMinute;

    /**
     * 반복 일정 패턴 생성자
     */
    public RepeatSchedule(String title, DayOfWeek dayOfWeek, LocalTime startTime,
                          LocalTime endTime, LocalDate baseDate) {
        super(title, null); // 반복 패턴 자체는 부모가 없음

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("종료 시간이 시작 시간보다 늦어야 합니다.");
        }

        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime.withSecond(0).withNano(0);
        this.endTime = endTime.withSecond(0).withNano(0);
        this.baseDate = baseDate;

        // 부모 클래스 호환성을 위한 필드 초기화
        this.year = baseDate.getYear();
        this.month = baseDate.getMonthValue();
        this.day = baseDate.getDayOfMonth();
        this.startHour = startTime.getHour();
        this.startMinute = startTime.getMinute();
        this.endHour = endTime.getHour();
        this.endMinute = endTime.getMinute();
    }

    @Override
    public List<Schedule> getOccurrencesOn(LocalDate date) {
        // RepeatSchedule 자체는 캘린더에 표시되지 않음
        // '이 달에 배치'를 통해 생성된 구체적 Schedule만 표시됨
        return List.of();
    }

    @Override
    public ScheduleType getScheduleType() {
        return ScheduleType.REPEAT;
    }

    /**
     * 주 단위(+7일) 규칙으로 특정 월(YearMonth)의 모든 발생 날짜 계산
     */
    public List<LocalDate> occurrencesInMonth(YearMonth ym) {
        List<LocalDate> list = new ArrayList<>();
        LocalDate first = ym.atDay(1);
        LocalDate last = ym.atEndOfMonth();

        LocalDate cur = first;
        while (cur.getDayOfWeek() != dayOfWeek) cur = cur.plusDays(1);
        while (cur.isBefore(baseDate)) cur = cur.plusWeeks(1);

        while (!cur.isAfter(last)) {
            list.add(cur);
            cur = cur.plusWeeks(1);
        }
        return list;
    }

    /**
     * 특정 날짜의 발생 여부 확인 및 구체적 일정 반환
     */
    public Schedule getOccurrence(LocalDate date) {
        if (date.getDayOfWeek() != dayOfWeek) return null;
        long daysBetween = ChronoUnit.DAYS.between(baseDate, date);
        if (daysBetween < 0 || daysBetween % 7 != 0) return null;

        return toConcrete(date);
    }

    /**
     * 특정 날짜의 '실제 일정(Schedule)'으로 구체화
     * Dependency Inversion Principle: 구체적인 Schedule 생성을 위임
     */
    public Schedule toConcrete(LocalDate date) {
        return new Schedule(
                date.getYear(), date.getMonthValue(), date.getDayOfMonth(),
                startTime.getHour(), startTime.getMinute(),
                endTime.getHour(), endTime.getMinute(),
                this.getTodo(),
                this.getId() // 부모(이 패턴)의 ID를 자식에게 부여
        );
    }

    // Getters
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public LocalDate getBaseDate() { return baseDate; }

    // 호환성을 위한 Getters
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDay() { return day; }
    public int getStartHour() { return startHour; }
    public int getStartMinute() { return startMinute; }
    public int getEndHour() { return endHour; }
    public int getEndMinute() { return endMinute; }
}