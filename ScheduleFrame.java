import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;

public class ScheduleFrame extends JFrame {
    private boolean editMode = false;
    private int year, month;
    private LocalDate selectedDate;
    private final ScheduleService service;
    private final ProjectManager projectManager;
    
    // 리팩토링된 컴포넌트 사용
    private CalendarGridPanel calendarPanel;
    private JPanel schedulePanel;
    private JLabel monthLabel;

    private JPanel achievementPanel;
    private JLabel achievementLabel;
    private JProgressBar achievementBar;

    private CardLayout centerLayout;
    private JPanel centerPanel;
    private RepeatFrame repeatPanel;
    private ProjectFrame projectPanel;
    private JButton calendarBtn;
    private JButton repeatBtn;
    private JButton projectBtn;
    private JButton addButton;

    public ScheduleFrame(ScheduleService service, ProjectManager projectManager) {
        this.service = service;
        this.projectManager = projectManager;

        LocalDate now = LocalDate.now();
        year = now.getYear();
        month = now.getMonthValue();
        selectedDate = now;

        setTitle("일정 관리 시스템");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 1100);

        BackgroundPanel background = new BackgroundPanel("/image/background.png");
        background.setLayout(new BorderLayout());
        setContentPane(background);

        // --- 상단 패널 ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel navCenterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        navCenterPanel.setOpaque(false);

        JButton prevButton = ScheduleSwingDesign.JimageButton("/image/left_button.png");
        JButton nextButton = ScheduleSwingDesign.JimageButton("/image/right_button.png");
        
        monthLabel = new JLabel("", JLabel.CENTER);
        monthLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));

        navCenterPanel.add(prevButton);
        navCenterPanel.add(monthLabel);
        navCenterPanel.add(nextButton);
        topPanel.add(navCenterPanel, BorderLayout.CENTER);

        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        topRightPanel.setOpaque(false);
        JButton loadButton = ScheduleSwingDesign.JimageButton("/image/file_load_button.png");
        JButton saveButton = ScheduleSwingDesign.JimageButton("/image/file_save_button.png");
        loadButton.addActionListener(e -> loadAllData());
        saveButton.addActionListener(e -> saveAllData());
        topRightPanel.add(loadButton);
        topRightPanel.add(saveButton);
        topPanel.add(topRightPanel, BorderLayout.EAST);

        background.add(topPanel, BorderLayout.NORTH);

        prevButton.addActionListener(e -> changeMonth(-1));
        nextButton.addActionListener(e -> changeMonth(1));

        // --- 달력 패널 (리팩토링됨) ---
        calendarPanel = new CalendarGridPanel();
        calendarPanel.setPreferredSize(new Dimension(0, 600));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 130, 10, 60));

        // --- 일정 패널 ---
        schedulePanel = ScheduleSwingDesign.JImagePanel("/image/schedulepanel.png");
        schedulePanel.setLayout(new BoxLayout(schedulePanel, BoxLayout.Y_AXIS));

        // --- 달성률 패널 ---
        achievementPanel = createAchievementPanel();

        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setOpaque(false);
        JPanel wrapper = ScheduleSwingDesign.createScrollWrapper(schedulePanel);
        wrapper.setBorder(BorderFactory.createEmptyBorder(150, 0, 10, 30));
        rightContainer.add(wrapper, BorderLayout.CENTER);
        rightContainer.add(achievementPanel, BorderLayout.SOUTH);

        // 캘린더 화면 조립
        JPanel calendarViewPanel = new JPanel(new BorderLayout());
        calendarViewPanel.setOpaque(false);
        calendarViewPanel.add(calendarPanel, BorderLayout.CENTER);
        calendarViewPanel.add(rightContainer, BorderLayout.EAST);

        // 반복/프로젝트 패널
        repeatPanel = new RepeatFrame(service);
        repeatPanel.setOpaque(false);
        JPanel repeatContainer = new JPanel(new BorderLayout());
        repeatContainer.setOpaque(false);
        repeatContainer.setBorder(BorderFactory.createEmptyBorder(50, 80, 50, 80));
        repeatContainer.add(repeatPanel, BorderLayout.CENTER);

        projectPanel = new ProjectFrame(projectManager, service);
        projectPanel.setOpaque(false);
        JPanel projectContainer = new JPanel(new BorderLayout());
        projectContainer.setOpaque(false);
        projectContainer.setBorder(BorderFactory.createEmptyBorder(50, 80, 50, 80));
        projectContainer.add(projectPanel, BorderLayout.CENTER);

        // CardLayout
        centerLayout = new CardLayout();
        centerPanel = new JPanel(centerLayout);
        centerPanel.setOpaque(false);
        centerPanel.add(calendarViewPanel, "CALENDAR");
        centerPanel.add(repeatContainer, "REPEAT");
        centerPanel.add(projectContainer, "PROJECT");
        background.add(centerPanel, BorderLayout.CENTER);

        // --- 사이드바 ---
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(70, 0));

        ImageIcon topIcon = new ImageIcon(getClass().getResource("/image/profile.png"));
        JLabel topImageLabel = new JLabel(topIcon);
        topImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(topImageLabel);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(Box.createVerticalStrut(20));

        calendarBtn = ScheduleSwingDesign.JimageButton("/image/calendar_on.png");
        calendarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        calendarBtn.addActionListener(e -> showCalendarView());
        sidebar.add(calendarBtn);
        sidebar.add(Box.createVerticalStrut(20));

        repeatBtn = ScheduleSwingDesign.JimageButton("/image/repeat_off.png");
        repeatBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        repeatBtn.addActionListener(e -> showRepeatView());
        sidebar.add(repeatBtn);
        sidebar.add(Box.createVerticalStrut(20));

        projectBtn = ScheduleSwingDesign.JimageButton("/image/project_off.png");
        projectBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        projectBtn.addActionListener(e -> showProjectView());
        sidebar.add(projectBtn);
        sidebar.add(Box.createVerticalGlue());
        background.add(sidebar, BorderLayout.WEST);

        // --- 하단 추가 버튼 ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        addButton = ScheduleSwingDesign.JimageButton("/image/plus_button.png");
        ScheduleSwingDesign.disableShape(addButton);
        bottomPanel.add(addButton);
        background.add(bottomPanel, BorderLayout.SOUTH);
        addButton.addActionListener(e -> openAddDialog(null));

        updateCalendar();
        setVisible(true);
    }

    // 파일 로드/저장 메서드는 기존 로직 유지
    private void loadAllData() {
        int confirm = JOptionPane.showConfirmDialog(this, "불러오시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.getManager().loadSchedulesFromCsv();
                updateCalendar();
                repeatPanel.refresh();
                projectPanel.refresh();
                JOptionPane.showMessageDialog(this, "로드 완료");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void saveAllData() {
        int confirm = JOptionPane.showConfirmDialog(this, "저장하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.getManager().saveSchedulesToCsv();
                projectManager.saveProjects();
                JOptionPane.showMessageDialog(this, "저장 완료");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void showCalendarView() {
        updateCalendar();
        centerLayout.show(centerPanel, "CALENDAR");
        calendarBtn.setIcon(new ImageIcon(getClass().getResource("/image/calendar_on.png")));
        repeatBtn.setIcon(new ImageIcon(getClass().getResource("/image/repeat_off.png")));
        projectBtn.setIcon(new ImageIcon(getClass().getResource("/image/project_off.png")));
        monthLabel.setVisible(true); monthLabel.getParent().setVisible(true); addButton.setVisible(true);
    }
    private void showRepeatView() {
        repeatPanel.refresh();
        centerLayout.show(centerPanel, "REPEAT");
        calendarBtn.setIcon(new ImageIcon(getClass().getResource("/image/calendar_off.png")));
        repeatBtn.setIcon(new ImageIcon(getClass().getResource("/image/repeat_on.png")));
        projectBtn.setIcon(new ImageIcon(getClass().getResource("/image/project_off.png")));
        monthLabel.setVisible(false); monthLabel.getParent().setVisible(false); addButton.setVisible(false);
    }
    private void showProjectView() {
        projectPanel.refresh();
        centerLayout.show(centerPanel, "PROJECT");
        calendarBtn.setIcon(new ImageIcon(getClass().getResource("/image/calendar_off.png")));
        repeatBtn.setIcon(new ImageIcon(getClass().getResource("/image/repeat_off.png")));
        projectBtn.setIcon(new ImageIcon(getClass().getResource("/image/project_on.png")));
        monthLabel.setVisible(false); monthLabel.getParent().setVisible(false); addButton.setVisible(false);
    }

    private JPanel createAchievementPanel() {
        JPanel panel = ScheduleSwingDesign.JImagePanel("/image/achievement_bg.png");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        achievementLabel = new JLabel("이달의 달성률: 0%");
        achievementLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        achievementLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(achievementLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        achievementBar = new JProgressBar(0, 100);
        achievementBar.setValue(0);
        achievementBar.setStringPainted(false);
        achievementBar.setPreferredSize(new Dimension(230, 30));
        achievementBar.setMaximumSize(new Dimension(230, 30));
        achievementBar.setForeground(new Color(45, 206, 137));
        achievementBar.setBackground(new Color(230, 230, 230));
        achievementBar.setOpaque(false);
        achievementBar.setBorderPainted(false);
        achievementBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(achievementBar);
        return panel;
    }

    private void changeMonth(int delta) {
        month += delta;
        if (month < 1) { month = 12; year--; }
        else if (month > 12) { month = 1; year++; }
        updateCalendar();
    }

    // ✅ 리팩토링된 달력 업데이트 로직
    public void updateCalendar() {
        monthLabel.setText(year + "년 " + month + "월");
        service.setCurrentMonth(java.time.YearMonth.of(year, month));

        calendarPanel.updateCalendar(year, month, selectedDate,
            (cell, date) -> {
                // 셀 렌더러 로직
                var schedules = service.getManager().getSchedulesOn(date);
                int dailyRate = calculateDailyAchievement(schedules);
                Color c = getAchievementColor(dailyRate);
                if (c != null) {
                    cell.setBackground(c);
                    cell.setOpaque(true);
                }

                int maxDisplay = 2;
                for (int i = 0; i < Math.min(maxDisplay, schedules.size()); i++) {
                    JLabel lbl = new JLabel("• " + schedules.get(i).getTodo());
                    lbl.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                    lbl.setForeground(Color.DARK_GRAY);
                    lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                    cell.add(lbl);
                }
                if (schedules.size() > maxDisplay) {
                    JLabel more = new JLabel("그 외 " + (schedules.size() - maxDisplay) + "개");
                    more.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                    more.setForeground(Color.GRAY);
                    more.setAlignmentX(Component.LEFT_ALIGNMENT);
                    cell.add(more);
                }
            },
            (clickedDate) -> {
                selectedDate = clickedDate;
                updateCalendar();
            }
        );

        updateSchedulePanel();
        updateAchievementPanel();
    }

    // ✅ 리팩토링된 일정 패널 업데이트 로직
    private void updateSchedulePanel() {
        schedulePanel.removeAll();
        if (selectedDate == null) return;

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setMaximumSize(new Dimension(250, 40));
        JLabel titleLabel = new JLabel(selectedDate.getMonthValue() + "월 " + selectedDate.getDayOfMonth() + "일 일정");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        JButton editButton = ScheduleSwingDesign.JimageButton(editMode ? "/image/edit_mode_on.png" : "/image/edit_mode_off.png");
        ScheduleSwingDesign.disableShape(editButton);
        editButton.addActionListener(e -> { editMode = !editMode; updateSchedulePanel(); });
        titlePanel.add(editButton, BorderLayout.EAST);
        schedulePanel.add(titlePanel);

        var schedules = service.getManager().getSchedulesOn(selectedDate);
        if (schedules.isEmpty()) {
            JLabel emptyLabel = new JLabel("일정이 없습니다.");
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            schedulePanel.add(emptyLabel);
        } else {
            for (Schedule s : schedules) {
                // TaskItemPanel 사용하여 중복 제거
                TaskItemPanel item = new TaskItemPanel(
                    s.toString(), 
                    s.completed(),
                    (e) -> { s.setCompleted(((JCheckBox)e.getSource()).isSelected()); updateCalendar(); },
                    editMode ? (e) -> openAddDialog(s) : null
                );
                schedulePanel.add(item);
                schedulePanel.add(Box.createVerticalStrut(8));
            }
        }
        schedulePanel.revalidate();
        schedulePanel.repaint();
    }
    
    // 헬퍼 메서드들
    private int calculateDailyAchievement(ArrayList<Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) return -1;
        long completed = schedules.stream().filter(Schedule::completed).count();
        return (int) Math.round((completed * 100.0) / schedules.size());
    }

    private Color getAchievementColor(int rate) {
        if (rate == 100) return new Color(220, 245, 210);
        else if (rate >= 51) return new Color(255, 250, 210);
        else if (rate >= 1) return new Color(255, 220, 220);
        else return null;
    }
    
    private void updateAchievementPanel() {
        // 월 전체 달성률 계산 로직 (기존과 동일)
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
        int total = 0, completed = 0;
        for (LocalDate d = firstDay; !d.isAfter(lastDay); d = d.plusDays(1)) {
            var list = service.getManager().getSchedulesOn(d);
            for(Schedule s : list) {
                total++;
                if(s.completed()) completed++;
            }
        }
        int rate = (total == 0) ? 0 : (int)Math.round((completed * 100.0)/total);
        achievementLabel.setText(month + "월 달성률: " + rate + "%");
        achievementBar.setValue(rate);
    }

    // ✅ 통합 다이얼로그 호출
    private void openAddDialog(Schedule target) {
        new ScheduleFormDialog(this, year, month, selectedDate.getDayOfMonth(), target, service, this::updateCalendar).setVisible(true);
    }
}