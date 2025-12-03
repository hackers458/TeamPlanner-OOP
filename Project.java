import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 프로젝트 도메인 모델 (Single Responsibility Principle)
 * 프로젝트 정보와 할일 목록 관리만 담당
 */
public class Project {
    private String name;
    private final LocalDate createdDate;
    private final List<ProjectTask> tasks;

    public Project(String name) {
        this.name = name;
        this.createdDate = LocalDate.now();
        this.tasks = new ArrayList<>();
    }

    // ===== Getters and Setters =====

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("프로젝트명은 비어있을 수 없습니다.");
        }
        this.name = name;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    /**
     * 읽기 전용 태스크 목록 반환 (Encapsulation)
     */
    public List<ProjectTask> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    // ===== Task 관리 메서드 =====

    /**
     * 할일 추가
     */
    public void addTask(ProjectTask task) {
        if (task == null) {
            throw new IllegalArgumentException("Task는 null일 수 없습니다.");
        }
        tasks.add(task);
    }

    /**
     * 할일 제거
     */
    public void removeTask(ProjectTask task) {
        tasks.remove(task);
    }

    /**
     * 특정 날짜의 할일 목록 조회
     * Open-Closed: 필터링 로직 확장 가능
     */
    public List<ProjectTask> getTasksOn(LocalDate date) {
        return tasks.stream()
                .filter(t -> t.getDate().equals(date))
                .toList();
    }

    /**
     * 날짜 범위의 할일 목록 조회
     */
    public List<ProjectTask> getTasksBetween(LocalDate start, LocalDate end) {
        return tasks.stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .toList();
    }

    // ===== 진척도 계산 =====

    /**
     * 전체 진척도 계산 (Single Responsibility)
     * 0 ~ 100 사이의 정수 반환
     */
    public int getProgress() {
        if (tasks.isEmpty()) {
            return 0;
        }
        long completed = countCompletedTasks();
        return (int) Math.round((completed * 100.0) / tasks.size());
    }

    /**
     * 완료된 할일 개수
     */
    public long countCompletedTasks() {
        return tasks.stream()
                .filter(ProjectTask::isCompleted)
                .count();
    }

    /**
     * 미완료된 할일 개수
     */
    public long countIncompleteTasks() {
        return tasks.size() - countCompletedTasks();
    }

    /**
     * 특정 날짜의 진척도
     */
    public int getProgressOn(LocalDate date) {
        List<ProjectTask> dateTasks = getTasksOn(date);
        if (dateTasks.isEmpty()) {
            return 0;
        }
        long completed = dateTasks.stream()
                .filter(ProjectTask::isCompleted)
                .count();
        return (int) Math.round((completed * 100.0) / dateTasks.size());
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Project project = (Project) obj;
        return name.equals(project.name) && createdDate.equals(project.createdDate);
    }

    @Override
    public int hashCode() {
        return name.hashCode() + createdDate.hashCode();
    }
}