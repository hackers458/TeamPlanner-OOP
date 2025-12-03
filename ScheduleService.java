import java.time.YearMonth;

/**
 * 스케줄 비즈니스 로직 서비스 (Single Responsibility Principle)
 * 스케줄 유효성 검증과 현재 월 관리 담당
 */
public class ScheduleService {
    private final ScheduleManager manager;
    private YearMonth currentMonth = YearMonth.now();

    public ScheduleService(ScheduleManager manager) {
        this.manager = manager;
    }

    /**
     * 스케줄 추가 (유효성 검증 포함)
     * Dependency Inversion: ISchedule 인터페이스 사용
     */
    public boolean addSchedule(ISchedule s) {
        if (!isValid(s)) return false;
        manager.add(s);
        return true;
    }

    /**
     * 스케줄 유효성 검증
     * Open-Closed: 스케줄 타입별 검증 로직 확장 가능
     */
    public boolean isValid(ISchedule s) {
        // 공통 검증: 내용 확인
        if (s.getTodo() == null || s.getTodo().trim().isEmpty()) {
            return false;
        }

        // 타입별 검증
        if (s.getScheduleType() == ISchedule.ScheduleType.REGULAR) {
            return isValidRegularSchedule((Schedule) s);
        } else if (s.getScheduleType() == ISchedule.ScheduleType.REPEAT) {
            return isValidRepeatSchedule((RepeatSchedule) s);
        }

        return false;
    }

    /**
     * 일반 스케줄 유효성 검증
     */
    private boolean isValidRegularSchedule(Schedule s) {
        // 시간 유효성 검증
        if (s.getEndHour() < s.getStartHour() ||
                (s.getEndHour() == s.getStartHour() && s.getEndMinute() <= s.getStartMinute())) {
            return false;
        }
        return true;
    }

    /**
     * 반복 스케줄 유효성 검증
     */
    private boolean isValidRepeatSchedule(RepeatSchedule s) {
        // 반복 스케줄은 생성자에서 이미 검증됨
        return s.getEndTime().isAfter(s.getStartTime());
    }

    public ScheduleManager getManager() {
        return manager;
    }

    public void setCurrentMonth(YearMonth ym) {
        this.currentMonth = ym;
    }

    public YearMonth getCurrentMonth() {
        return currentMonth;
    }
}