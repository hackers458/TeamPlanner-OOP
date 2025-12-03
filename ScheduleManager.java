import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 통합 스케줄 관리자 (Single Responsibility Principle)
 * ISchedule 인터페이스를 통해 일반/반복 스케줄을 통합 관리
 */
public class ScheduleManager {

    // Liskov Substitution Principle: ISchedule 인터페이스로 통합 관리
    private final List<ISchedule> scheduleList = new ArrayList<>();

    // 파일 경로 상수
    private static final String SCHEDULE_FILE = "schedules.txt";
    private static final String REPEAT_FILE = "repeat_schedules.txt";

    /**
     * 스케줄 추가 (일반/반복 모두 가능)
     */
    public void add(ISchedule s) {
        scheduleList.add(s);
    }

    /**
     * 스마트 삭제 메서드 (Open-Closed Principle)
     * 스케줄 타입에 따라 적절한 삭제 로직 적용
     */
    public void removeSchedule(ISchedule s) {
        if (s.getScheduleType() == ISchedule.ScheduleType.REPEAT) {
            // 반복 패턴 삭제: 패턴과 파생된 모든 일정 삭제
            String patternId = s.getId();
            scheduleList.remove(s);
            scheduleList.removeIf(child ->
                    child.getFromRepeatId() != null &&
                            child.getFromRepeatId().equals(patternId)
            );
            System.out.println("반복 패턴과 관련된 모든 일정이 삭제되었습니다.");
        } else {
            // 일반 일정 삭제
            scheduleList.remove(s);
        }
    }

    /**
     * 전체 스케줄 목록 반환 (읽기 전용)
     */
    public List<ISchedule> allSchedules() {
        return new ArrayList<>(scheduleList);
    }

    /**
     * 특정 날짜의 구체적인 일정들만 반환
     * Interface Segregation: getOccurrencesOn() 메서드 활용
     */
    public ArrayList<Schedule> getSchedulesOn(LocalDate date) {
        ArrayList<Schedule> result = new ArrayList<>();
        for (ISchedule s : scheduleList) {
            result.addAll(s.getOccurrencesOn(date));
        }
        return result.stream()
                .sorted(Comparator.comparingInt(x -> x.getStartHour() * 60 + x.getStartMinute()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 일반 스케줄만 필터링하여 반환
     */
    public List<Schedule> getRegularSchedules() {
        return scheduleList.stream()
                .filter(s -> s.getScheduleType() == ISchedule.ScheduleType.REGULAR)
                .map(s -> (Schedule) s)
                .collect(Collectors.toList());
    }

    /**
     * 반복 스케줄 패턴만 필터링하여 반환
     */
    public List<RepeatSchedule> getRepeatSchedules() {
        return scheduleList.stream()
                .filter(s -> s.getScheduleType() == ISchedule.ScheduleType.REPEAT)
                .map(s -> (RepeatSchedule) s)
                .collect(Collectors.toList());
    }

    /**
     * 통합 로드: 일반 일정 + 반복 일정 패턴 + 파생 일정 모두 로드
     */
    public void loadSchedulesFromCsv() {
        scheduleList.clear();

        // 1. 반복 일정 패턴 먼저 로드 (ID 참조를 위해)
        loadRepeatSchedules();

        // 2. 일반 일정 + 반복일정에서 파생된 구체적 일정 로드
        loadRegularSchedules();

        System.out.println("총 " + scheduleList.size() + "개의 일정을 로드했습니다.");
        System.out.println("- 일반 일정: " + getRegularSchedules().size() + "개");
        System.out.println("- 반복 패턴: " + getRepeatSchedules().size() + "개");
    }

    /**
     * 일반 일정 + 반복일정 파생 일정 로드
     * 형식: year,month,day,startHour,startMinute,endHour,endMinute,todo,completed,fromRepeatId
     */
    private void loadRegularSchedules() {
        try (BufferedReader br = new BufferedReader(new FileReader(SCHEDULE_FILE))) {
            String line;
            int count = 0;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 9) {
                    System.err.println("잘못된 일정 형식: " + line);
                    continue;
                }

                try {
                    int year = Integer.parseInt(parts[0].trim());
                    int month = Integer.parseInt(parts[1].trim());
                    int day = Integer.parseInt(parts[2].trim());
                    int startHour = Integer.parseInt(parts[3].trim());
                    int startMinute = Integer.parseInt(parts[4].trim());
                    int endHour = Integer.parseInt(parts[5].trim());
                    int endMinute = Integer.parseInt(parts[6].trim());
                    String todo = parts[7].trim();
                    boolean check = Boolean.parseBoolean(parts[8].trim());

                    String fromRepeatId = null;
                    if (parts.length >= 10 && !parts[9].trim().isEmpty() && !parts[9].trim().equals("null")) {
                        fromRepeatId = parts[9].trim();
                    }

                    Schedule s = new Schedule(year, month, day, startHour, startMinute,
                            endHour, endMinute, todo, fromRepeatId);
                    s.setCompleted(check);
                    scheduleList.add(s);
                    count++;

                } catch (NumberFormatException e) {
                    System.err.println("숫자 파싱 오류: " + line);
                }
            }

            System.out.println("일반 일정 " + count + "개 로드 완료");

        } catch (IOException e) {
            System.out.println("일반 일정 파일을 찾을 수 없습니다: " + SCHEDULE_FILE);
        }
    }

    /**
     * 반복 일정 패턴 로드
     * 형식: title|dayOfWeek|startTime|endTime|baseDate
     */
    private void loadRepeatSchedules() {
        try (BufferedReader br = new BufferedReader(new FileReader(REPEAT_FILE))) {
            String line;
            int count = 0;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 5) {
                    System.err.println("잘못된 반복 일정 형식: " + line);
                    continue;
                }

                try {
                    String title = parts[0].trim();
                    DayOfWeek dayOfWeek = DayOfWeek.valueOf(parts[1].trim());
                    LocalTime startTime = LocalTime.parse(parts[2].trim());
                    LocalTime endTime = LocalTime.parse(parts[3].trim());
                    LocalDate baseDate = LocalDate.parse(parts[4].trim());

                    RepeatSchedule rs = new RepeatSchedule(title, dayOfWeek, startTime, endTime, baseDate);
                    scheduleList.add(rs);
                    count++;

                } catch (Exception e) {
                    System.err.println("반복 일정 파싱 오류: " + line + " - " + e.getMessage());
                }
            }

            System.out.println("반복 일정 패턴 " + count + "개 로드 완료");

        } catch (IOException e) {
            System.out.println("반복 일정 파일을 찾을 수 없습니다: " + REPEAT_FILE);
        }
    }

    /**
     * 통합 저장: 일반 일정, 반복일정 파생 일정, 반복 패턴 모두 저장
     */
    public void saveSchedulesToCsv() {
        saveRegularSchedules();
        saveRepeatSchedules();
    }

    /**
     * 일반 일정 + 반복일정에서 파생된 구체적 일정 저장
     */
    private void saveRegularSchedules() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SCHEDULE_FILE))) {
            int count = 0;

            bw.write("# 일반 일정 데이터 (반복일정 파생 포함)");
            bw.newLine();
            bw.write("# 형식: year,month,day,startHour,startMinute,endHour,endMinute,todo,completed,fromRepeatId");
            bw.newLine();
            bw.write("# ---------------------------------------------------------------");
            bw.newLine();

            for (ISchedule s : scheduleList) {
                // 반복 패턴 자체는 제외
                if (s.getScheduleType() == ISchedule.ScheduleType.REPEAT) continue;

                Schedule schedule = (Schedule) s;
                String fromRepeatId = (schedule.getFromRepeatId() != null) ? schedule.getFromRepeatId() : "";

                String line = String.format("%d,%d,%d,%d,%d,%d,%d,%s,%b,%s",
                        schedule.getYear(), schedule.getMonth(), schedule.getDay(),
                        schedule.getStartHour(), schedule.getStartMinute(),
                        schedule.getEndHour(), schedule.getEndMinute(),
                        schedule.getTodo(), schedule.isChecked(), fromRepeatId);
                bw.write(line);
                bw.newLine();
                count++;
            }

            System.out.println("일반 일정 " + count + "개 저장 완료");

        } catch (IOException e) {
            System.err.println("일반 일정 저장 오류: " + e.getMessage());
        }
    }

    /**
     * 반복 일정 패턴 저장
     */
    private void saveRepeatSchedules() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(REPEAT_FILE))) {
            int count = 0;

            bw.write("# 반복 일정 패턴 데이터");
            bw.newLine();
            bw.write("# 형식: title|dayOfWeek|startTime|endTime|baseDate");
            bw.newLine();
            bw.write("# ---------------------------------------------------------------");
            bw.newLine();

            for (ISchedule s : scheduleList) {
                if (s.getScheduleType() != ISchedule.ScheduleType.REPEAT) continue;

                RepeatSchedule rs = (RepeatSchedule) s;

                String line = String.format("%s|%s|%s|%s|%s",
                        rs.getTodo(),
                        rs.getDayOfWeek().name(),
                        rs.getStartTime().toString(),
                        rs.getEndTime().toString(),
                        rs.getBaseDate().toString());

                bw.write(line);
                bw.newLine();
                count++;
            }

            System.out.println("반복 일정 패턴 " + count + "개 저장 완료");

        } catch (IOException e) {
            System.err.println("반복 일정 저장 오류: " + e.getMessage());
        }
    }

    /**
     * 특정 날짜의 일정 개수 반환
     */
    public long countSchedules(int year, int month, int day) {
        return getSchedulesOn(LocalDate.of(year, month, day)).size();
    }
}