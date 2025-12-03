import javax.swing.*;

/**
 * 메인 애플리케이션 진입점 (Single Responsibility Principle)
 * 애플리케이션 초기화와 실행만 담당
 */
public class Main {
    public static void main(String[] args) {
        // Swing 애플리케이션은 EDT(Event Dispatch Thread)에서 실행
        SwingUtilities.invokeLater(() -> {
            try {
                // 애플리케이션 초기화
                ApplicationContext context = initializeApplication();

                // UI 생성
                createAndShowUI(context);

            } catch (Exception e) {
                showErrorAndExit("애플리케이션 시작 중 오류가 발생했습니다.", e);
            }
        });
    }

    /**
     * 애플리케이션 컨텍스트 초기화
     * Dependency Inversion: 인터페이스에 의존
     */
    private static ApplicationContext initializeApplication() {
        System.out.println("=== 프로그램 시작: 데이터 로드 중 ===");

        // 1. Manager 생성
        ScheduleManager scheduleManager = new ScheduleManager();
        ProjectManager projectManager = new ProjectManager();

        // 2. Service 생성 (Dependency Injection)
        ScheduleService scheduleService = new ScheduleService(scheduleManager);

        // 3. 데이터 로드
        loadData(scheduleManager, projectManager);

        System.out.println("=== 데이터 로드 완료 ===\n");

        return new ApplicationContext(scheduleService, projectManager);
    }

    /**
     * 모든 데이터 로드 (Single Responsibility)
     */
    private static void loadData(ScheduleManager scheduleManager, ProjectManager projectManager) {
        // 일반 일정 + 반복 일정 패턴 + 반복일정 파생 일정 로드
        scheduleManager.loadSchedulesFromCsv();

        // 프로젝트는 ProjectManager 생성자에서 자동 로드됨
        System.out.println("프로젝트 " + projectManager.count() + "개 로드 완료");
    }

    /**
     * UI 생성 및 표시
     */
    private static void createAndShowUI(ApplicationContext context) {
        SwingUtilities.invokeLater(() ->
                new ScheduleFrame(context.getScheduleService(), context.getProjectManager())
        );
    }

    /**
     * 오류 표시 및 프로그램 종료
     */
    private static void showErrorAndExit(String message, Exception e) {
        System.err.println(message);
        e.printStackTrace();

        JOptionPane.showMessageDialog(null,
                message + "\n\n" + e.getMessage(),
                "오류",
                JOptionPane.ERROR_MESSAGE);

        System.exit(1);
    }

    /**
     * 애플리케이션 컨텍스트 클래스
     * Single Responsibility: 애플리케이션 전역 객체 보관만 담당
     */
    private static class ApplicationContext {
        private final ScheduleService scheduleService;
        private final ProjectManager projectManager;

        public ApplicationContext(ScheduleService scheduleService, ProjectManager projectManager) {
            this.scheduleService = scheduleService;
            this.projectManager = projectManager;
        }

        public ScheduleService getScheduleService() {
            return scheduleService;
        }

        public ProjectManager getProjectManager() {
            return projectManager;
        }
    }
}