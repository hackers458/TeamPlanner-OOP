import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 프로젝트 관리자 (Single Responsibility Principle)
 * 프로젝트 목록 관리와 파일 I/O만 담당
 */
public class ProjectManager {
    private final List<Project> projects = new ArrayList<>();

    // 파일 경로 상수
    private static final String DATA_FILE = "project_schedules.txt";
    private static final String PROJECT_LIST_FILE = "projects.txt";

    public ProjectManager() {
        loadProjects(); // 프로그램 시작 시 데이터 로드
    }

    // ===== CRUD 연산 =====

    /**
     * 프로젝트 추가
     */
    public void add(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project는 null일 수 없습니다.");
        }
        if (findByName(project.getName()) != null) {
            throw new IllegalArgumentException("동일한 이름의 프로젝트가 이미 존재합니다.");
        }
        projects.add(project);
        saveProjects(); // 추가 시 저장
    }

    /**
     * 프로젝트 제거
     */
    public void remove(Project project) {
        projects.remove(project);
        saveProjects(); // 삭제 시 저장
    }

    /**
     * 모든 프로젝트 조회 (읽기 전용)
     */
    public List<Project> getAll() {
        return Collections.unmodifiableList(projects);
    }

    /**
     * 이름으로 프로젝트 찾기
     */
    public Project findByName(String name) {
        return projects.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * 프로젝트 개수
     */
    public int count() {
        return projects.size();
    }

    // ===== 파일 I/O =====

    /**
     * 프로젝트 데이터 로드
     * Open-Closed: 로드 방식 변경 시 이 메서드만 수정
     */
    private void loadProjects() {
        projects.clear();

        // 1단계: 프로젝트 목록 로드
        loadProjectList();

        // 2단계: 각 프로젝트의 할일 로드
        loadProjectTasks();

        System.out.println("프로젝트 " + projects.size() + "개 로드 완료");
    }

    /**
     * 프로젝트 목록만 로드 (할일이 없는 프로젝트도 로드)
     */
    private void loadProjectList() {
        try (BufferedReader br = new BufferedReader(new FileReader(PROJECT_LIST_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;

                String projectName = line.trim();
                Project project = new Project(projectName);
                projects.add(project);
            }
            System.out.println("프로젝트 목록 로드 완료: " + projects.size() + "개");
        } catch (FileNotFoundException e) {
            System.out.println("프로젝트 목록 파일(" + PROJECT_LIST_FILE + ")을 찾을 수 없습니다.");
        } catch (Exception e) {
            System.err.println("프로젝트 목록 로드 중 오류: " + e.getMessage());
        }
    }

    /**
     * 각 프로젝트의 할일 로드
     */
    private void loadProjectTasks() {
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|", 4);

                if (parts.length != 4) {
                    System.err.println("경고: 잘못된 프로젝트 데이터 라인: " + line);
                    continue;
                }

                String projectName = parts[0].trim();
                LocalDate date = LocalDate.parse(parts[1].trim());
                String content = parts[2].trim();
                boolean completed = Boolean.parseBoolean(parts[3].trim());

                // 해당 프로젝트 찾기
                Project currentProject = findByName(projectName);
                if (currentProject == null) {
                    // 프로젝트 목록에 없으면 새로 생성
                    currentProject = new Project(projectName);
                    projects.add(currentProject);
                }

                // ProjectTask 객체 생성 및 해당 프로젝트에 추가
                ProjectTask task = new ProjectTask(date, content);
                task.setCompleted(completed);
                currentProject.addTask(task);
            }
            System.out.println("프로젝트 할일 로드 완료");
        } catch (FileNotFoundException e) {
            System.out.println("프로젝트 할일 파일(" + DATA_FILE + ")을 찾을 수 없습니다.");
        } catch (Exception e) {
            System.err.println("프로젝트 할일 로드 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 현재 프로젝트 목록과 할일을 파일에 저장
     */
    public void saveProjects() {
        saveProjectList();
        saveProjectTasks();
    }

    /**
     * 프로젝트 목록 저장 (할일이 없어도 저장됨)
     */
    private void saveProjectList() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PROJECT_LIST_FILE))) {
            bw.write("# 프로젝트 목록");
            bw.newLine();
            bw.write("# 각 줄에 프로젝트 이름 하나씩");
            bw.newLine();
            bw.write("# ---------------------------------------------------------------");
            bw.newLine();

            for (Project project : projects) {
                bw.write(project.getName());
                bw.newLine();
            }

            System.out.println("프로젝트 목록 " + projects.size() + "개 저장 완료");
        } catch (IOException e) {
            System.err.println("프로젝트 목록 저장 중 오류: " + e.getMessage());
        }
    }

    /**
     * 프로젝트 할일 저장
     */
    private void saveProjectTasks() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE))) {
            int saveCount = 0;

            bw.write("# 프로젝트 할일 데이터 파일");
            bw.newLine();
            bw.write("# 형식: [프로젝트명]|[날짜(YYYY-MM-DD)]|[내용]|[완료 여부(true/false)]");
            bw.newLine();
            bw.write("# ----------------------------------------------------------------------");
            bw.newLine();

            for (Project project : projects) {
                // 프로젝트의 모든 Task를 순회하며 저장
                for (ProjectTask task : project.getTasks()) {
                    String line = String.format("%s|%s|%s|%b",
                            project.getName(),
                            task.getDate().toString(),
                            task.getContent().replace('|', ' '), // | 문자 제거
                            task.isCompleted());
                    bw.write(line);
                    bw.newLine();
                    saveCount++;
                }
            }
            System.out.println("프로젝트 할일 " + saveCount + "개 저장 완료");
        } catch (IOException e) {
            System.err.println("프로젝트 할일 저장 중 오류: " + e.getMessage());
        }
    }

    // ===== 통계 메서드 =====

    /**
     * 전체 프로젝트의 평균 진척도
     */
    public int getAverageProgress() {
        if (projects.isEmpty()) {
            return 0;
        }
        int sum = projects.stream()
                .mapToInt(Project::getProgress)
                .sum();
        return sum / projects.size();
    }

    /**
     * 완료된 프로젝트 개수 (진척도 100%)
     */
    public long countCompletedProjects() {
        return projects.stream()
                .filter(p -> p.getProgress() == 100)
                .count();
    }
}