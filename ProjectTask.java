import java.time.LocalDate;
import java.util.Objects;

/**
 * 프로젝트 할일 도메인 모델 (Single Responsibility Principle)
 * 할일의 날짜, 내용, 완료 상태만 관리
 */
public class ProjectTask {
    private LocalDate date;
    private String content;
    private boolean completed;

    public ProjectTask(LocalDate date, String content) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 null일 수 없습니다.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 비어있을 수 없습니다.");
        }

        this.date = date;
        this.content = content;
        this.completed = false;
    }

    // ===== Getters and Setters =====

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 null일 수 없습니다.");
        }
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 비어있을 수 없습니다.");
        }
        this.content = content;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * 할일 완료 토글
     */
    public void toggleCompleted() {
        this.completed = !this.completed;
    }

    // ===== 비즈니스 로직 =====

    /**
     * 특정 날짜인지 확인
     */
    public boolean isOnDate(LocalDate targetDate) {
        return this.date.equals(targetDate);
    }

    /**
     * 날짜 범위 내에 있는지 확인
     */
    public boolean isBetween(LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * 마감일이 지났는지 확인 (오늘 기준)
     */
    public boolean isOverdue() {
        return !completed && date.isBefore(LocalDate.now());
    }

    /**
     * 오늘 할일인지 확인
     */
    public boolean isToday() {
        return date.equals(LocalDate.now());
    }

    @Override
    public String toString() {
        return content;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProjectTask that = (ProjectTask) obj;
        return date.equals(that.date) && content.equals(that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, content);
    }
}