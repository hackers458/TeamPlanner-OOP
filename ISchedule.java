import java.time.LocalDate;
import java.util.List;

/**
 * 모든 스케줄 타입의 기본 인터페이스
 * SOLID 원칙 중 Interface Segregation Principle 적용
 */
public interface ISchedule {
    /**
     * 스케줄의 고유 ID 반환
     */
    String getId();

    /**
     * 스케줄 내용 반환
     */
    String getTodo();

    /**
     * 특정 날짜에 해당하는 스케줄 인스턴스들 반환
     * - 일반 스케줄: 날짜가 일치하면 자기 자신 반환
     * - 반복 스케줄: 해당 날짜가 반복 규칙에 맞으면 구체적 스케줄 생성하여 반환
     */
    List<Schedule> getOccurrencesOn(LocalDate date);

    /**
     * 이 스케줄이 반복 스케줄에서 파생된 것인지 확인
     */
    String getFromRepeatId();

    /**
     * 스케줄 타입 반환 (일반/반복)
     */
    ScheduleType getScheduleType();

    /**
     * 스케줄 타입 열거형
     */
    enum ScheduleType {
        REGULAR,    // 일반 일정
        REPEAT      // 반복 일정 패턴
    }
}