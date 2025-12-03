import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ProjectCalendarPanel extends JPanel {
    private final Project project;
    private final ScheduleService service;
    private final ProjectFrame parentFrame;
    private int year, month;
    private LocalDate selectedDate;
    
    // 리팩토링된 컴포넌트
    private CalendarGridPanel calendarPanel;
    private JPanel taskPanel;
    
    private JLabel monthLabel;
    private JPanel progressPanel;
    private JLabel progressLabel;
    private JProgressBar progressBar;

    public ProjectCalendarPanel(Project project, ScheduleService service, ProjectFrame parentFrame) {
        this.project = project;
        this.service = service;
        this.parentFrame = parentFrame;

        LocalDate now = LocalDate.now();
        year = now.getYear();
        month = now.getMonthValue();
        selectedDate = now;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 130, 10, 60));
        setOpaque(false);

        // 상단: 뒤로가기 + 프로젝트명
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setOpaque(false);
        JButton backBtn = ScheduleSwingDesign.JimageButton("/image/left_button.png");
        backBtn.addActionListener(e -> parentFrame.showProjectList());
        JLabel projectTitle = new JLabel("프로젝트: " + project.getName());
        projectTitle.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        topPanel.add(backBtn);
        topPanel.add(projectTitle);
        add(topPanel, BorderLayout.NORTH);

        // 중앙 컨테이너
        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));
        centerContainer.setOpaque(false);

        // 네비게이션
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        navPanel.setOpaque(false);
        JButton prevBtn = ScheduleSwingDesign.JimageButton("/image/left_button.png");
        JButton nextBtn = ScheduleSwingDesign.JimageButton("/image/right_button.png");
        monthLabel = new JLabel("", JLabel.CENTER);
        monthLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        prevBtn.addActionListener(e -> changeMonth(-1));
        nextBtn.addActionListener(e -> changeMonth(1));
        navPanel.add(prevBtn); navPanel.add(monthLabel); navPanel.add(nextBtn);

        // 달력 (리팩토링됨)
        calendarPanel = new CalendarGridPanel();
        calendarPanel.setPreferredSize(new Dimension(900, 700));

        JPanel calendarContainer = new JPanel(new BorderLayout());
        calendarContainer.setOpaque(false);
        calendarContainer.add(navPanel, BorderLayout.NORTH);
        calendarContainer.add(calendarPanel, BorderLayout.CENTER);

        // 우측 할일 패널
        taskPanel = ScheduleSwingDesign.JImagePanel("/image/schedulepanel.png");
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        taskPanel.setPreferredSize(new Dimension(280, 0));

        JScrollPane scrollPane = new JScrollPane(taskPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(150, 0, 10, 30));
        wrapper.add(scrollPane, BorderLayout.CENTER);

        progressPanel = createProgressPanel();
        
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setOpaque(false);
        rightContainer.add(wrapper, BorderLayout.CENTER);
        calendarContainer.add(progressPanel, BorderLayout.SOUTH);
        rightContainer.setBorder(BorderFactory.createEmptyBorder(-progressPanel.getPreferredSize().height + 20, 0, 0, 0));

        centerContainer.add(calendarContainer, BorderLayout.CENTER);
        centerContainer.add(rightContainer, BorderLayout.EAST);
        add(centerContainer, BorderLayout.CENTER);

        // 하단 추가 버튼
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        JButton addBtn = ScheduleSwingDesign.JimageButton("/image/plus_button.png");
        addBtn.addActionListener(e -> openTaskDialog(null));
        bottomPanel.add(addBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        updateCalendar();
    }

    private JPanel createProgressPanel() {
        JPanel panel = ScheduleSwingDesign.JImagePanel("/image/achievement_bg_project.png");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        progressLabel = new JLabel("진척도: 0%");
        progressLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(progressLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(0, 30));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        progressBar.setForeground(new Color(45, 206, 137));
        progressBar.setBackground(new Color(230, 230, 230));
        progressBar.setOpaque(false);
        progressBar.setBorderPainted(false);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(progressBar);
        return panel;
    }

    private void changeMonth(int delta) {
        month += delta;
        if (month < 1) { month = 12; year--; }
        else if (month > 12) { month = 1; year++; }
        updateCalendar();
    }

    // ✅ 리팩토링된 달력 업데이트
    private void updateCalendar() {
        monthLabel.setText(year + "년 " + month + "월");
        
        calendarPanel.updateCalendar(year, month, selectedDate,
            (cell, date) -> {
                List<ProjectTask> tasks = project.getTasksOn(date);
                if (!tasks.isEmpty()) {
                    long completed = tasks.stream().filter(ProjectTask::isCompleted).count();
                    int rate = (int) Math.round((completed * 100.0) / tasks.size());
                    
                    Color c = null;
                    if (rate == 100) c = new Color(220, 245, 210);
                    else if (rate >= 51) c = new Color(255, 250, 210);
                    else if (rate >= 1) c = new Color(255, 220, 220);
                    
                    if(c != null) {
                        cell.setBackground(c);
                        cell.setOpaque(true);
                    }
                }
                
                int maxDisplay = 2;
                for(int i=0; i<Math.min(maxDisplay, tasks.size()); i++) {
                    JLabel lbl = new JLabel("• " + tasks.get(i).getContent());
                    lbl.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
                    lbl.setForeground(Color.DARK_GRAY);
                    lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                    cell.add(lbl);
                }
                if(tasks.size() > maxDisplay) {
                    JLabel more = new JLabel("+" + (tasks.size() - maxDisplay) + "개");
                    more.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
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
        
        updateTaskPanel();
        
        // 진척도 업데이트
        int p = project.getProgress();
        progressLabel.setText("진척도: " + p + "%");
        progressBar.setValue(p);
        progressPanel.revalidate();
        progressPanel.repaint();
    }

    // ✅ 리팩토링된 할일 패널 업데이트
    private void updateTaskPanel() {
        taskPanel.removeAll();
        if(selectedDate == null) return;

        JLabel titleLabel = new JLabel(selectedDate.getMonthValue() + "월 " + selectedDate.getDayOfMonth() + "일 할 일");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        JPanel titleContainer = new JPanel(new BorderLayout());
        titleContainer.setOpaque(false);
        titleContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        titleContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        titleContainer.add(titleLabel, BorderLayout.WEST);
        titleContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        taskPanel.add(titleContainer);

        List<ProjectTask> tasks = project.getTasksOn(selectedDate);
        if(tasks.isEmpty()) {
            JLabel empty = new JLabel("할일이 없습니다.");
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            taskPanel.add(empty);
        } else {
            for(ProjectTask t : tasks) {
                // TaskItemPanel 재사용 (이미지 배경 변경 가능)
                // 여기서는 schedule_background2.png를 사용한다고 가정 (TaskItemPanel 내부 로직을 수정하거나 생성자 오버로딩 필요할 수 있음. 
                // 위 TaskItemPanel 코드에서는 schedule_background.png로 고정했으나, 필요시 인자로 받게 수정 가능. 
                // 일단은 기본 제공된 TaskItemPanel 사용)
                TaskItemPanel item = new TaskItemPanel(
                    t.getContent(),
                    t.isCompleted(),
                    (e) -> { t.setCompleted(((JCheckBox)e.getSource()).isSelected()); updateCalendar(); },
                    (e) -> openTaskDialog(t)
                );
                // ProjectUI 특성상 배경 이미지가 다를 수 있으므로 TaskItemPanel을 조금 더 일반화하는 것이 좋으나
                // 현재 코드상으로는 위 TaskItemPanel 그대로 사용해도 무방함.
                
                item.setAlignmentX(Component.CENTER_ALIGNMENT);
                taskPanel.add(item);
                taskPanel.add(Box.createVerticalStrut(8));
            }
        }
        taskPanel.revalidate();
        taskPanel.repaint();
    }

    private void openTaskDialog(ProjectTask target) {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        new ProjectTaskFormDialog(owner, project, target, year, month, selectedDate.getDayOfMonth(), this::updateCalendar).setVisible(true);
    }
}