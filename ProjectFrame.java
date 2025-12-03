import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProjectFrame extends JPanel {
    private final ProjectManager projectManager;
    private final ScheduleService service;
    private final JPanel listPanel = new JPanel();
    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    public ProjectFrame(ProjectManager projectManager, ScheduleService service) {
        this.projectManager = projectManager;
        this.service = service;

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setOpaque(false);

        // 프로젝트 목록 화면
        JPanel projectListPanel = createProjectListPanel();
        contentPanel.add(projectListPanel, "LIST");

        add(contentPanel, BorderLayout.CENTER);

        refresh();
    }

    private JPanel createProjectListPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        // 리스트 + 버튼을 담는 컨테이너
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        // 리스트 부분
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        container.add(listPanel);

        // 버튼 패널 (가운데 정렬)
        JButton addBtn = ScheduleSwingDesign.JimageButton("/image/project_new_project.png");
        addBtn.addActionListener(e -> openProjectDialog(null));

        JPanel addBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addBtnPanel.setOpaque(false);
        addBtnPanel.add(addBtn);

        // 버튼을 리스트 아래에 붙임
        container.add(Box.createVerticalStrut(10)); // 버튼 위 살짝 여백 주기
        container.add(addBtnPanel);

        // 스크롤에 container를 넣음
        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void openProjectDialog(Project existingProject) {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, existingProject == null ? "프로젝트 생성" : "프로젝트 수정", true);
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(owner);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        inputPanel.add(new JLabel("프로젝트명:"));
        JTextField nameField = new JTextField(20);
        if (existingProject != null) {
            nameField.setText(existingProject.getName());
        }
        inputPanel.add(nameField);
        dialog.add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = ScheduleSwingDesign.JimageButton("/image/button_dialog_cancel.png");
        JButton saveBtn = ScheduleSwingDesign.JimageButton(
                existingProject == null ? "/image/button_dialog_add.png" : "/image/project_edit.png"
        );

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "프로젝트명을 입력하세요.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (existingProject == null) {
                Project project = new Project(name);
                projectManager.add(project);
            } else {
                existingProject.setName(name);
            }
            refresh();
            dialog.dispose();
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    public void refresh() {
        listPanel.removeAll();

        java.util.List<Project> projects = projectManager.getAll();
        if (projects.isEmpty()) {
            JLabel emptyLabel = new JLabel("등록된 프로젝트가 없습니다.");
            emptyLabel.setOpaque(false);
            listPanel.add(emptyLabel);
        } else {
            for (Project project : projects) {
                listPanel.add(new ProjectCard(project));
                listPanel.add(Box.createVerticalStrut(8));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    public void showProjectCalendar(Project project) {
        ProjectCalendarPanel calendarPanel = new ProjectCalendarPanel(project, service, this);
        contentPanel.add(calendarPanel, "CALENDAR_" + project.getName());
        cardLayout.show(contentPanel, "CALENDAR_" + project.getName());
    }

    public void showProjectList() {
        cardLayout.show(contentPanel, "LIST");
        refresh();
    }

    private class ProjectCard extends JPanel {
        private final Project project;

        public ProjectCard(Project project) {
            this.project = project;

            JPanel backgroundPanel = ScheduleSwingDesign.JImagePanel("/image/repeat_background_middle.png", true);
            backgroundPanel.setLayout(new BorderLayout(10, 10));

            backgroundPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

            setLayout(new BorderLayout());
            setBorder(null);
            setOpaque(false);
            setPreferredSize(new Dimension(0, 140));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

            // 상단: 프로젝트명 + 버튼들
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);

            // ✅  프로젝트 아이콘 추가
            JLabel iconLabel = new JLabel(new ImageIcon(getClass().getResource("/image/project_mini.png")));
            JLabel nameLabel = new JLabel(project.getName());
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 18f));

            // ✅  이미지 + 텍스트 묶기
            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            titlePanel.setOpaque(false);
            titlePanel.add(iconLabel);
            titlePanel.add(nameLabel);
            topPanel.add(titlePanel, BorderLayout.WEST);

            // 수정/삭제 버튼
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            buttonPanel.setOpaque(false);

            JButton editBtn = ScheduleSwingDesign.JimageButton("/image/repeat_edit_button.png");
            editBtn.setToolTipText("프로젝트 수정");
            editBtn.addActionListener(e -> openProjectDialog(project));

            JButton deleteBtn = ScheduleSwingDesign.JimageButton("/image/repeat_delete_button.png");
            deleteBtn.setToolTipText("프로젝트 삭제");
            deleteBtn.addActionListener(e -> { int confirm = JOptionPane.showConfirmDialog(this, "프로젝트 '" + project.getName() + "'을(를) 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    projectManager.remove(project);
                    refresh();
                }
            });

            buttonPanel.add(editBtn);
            buttonPanel.add(deleteBtn);
            topPanel.add(buttonPanel, BorderLayout.EAST);
            backgroundPanel.add(topPanel, BorderLayout.NORTH);

            // 중앙: 정보 + 진척도
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setOpaque(false);

            // 정보 라벨
            JLabel infoLabel = new JLabel("생성일: " + project.getCreatedDate() + " | 할 일: " + project.getTasks().size() + "개");
            infoLabel.setFont(infoLabel.getFont().deriveFont(12f));
            infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerPanel.add(infoLabel);
            centerPanel.add(Box.createVerticalStrut(10));

            // 진척도: 숫자 + 바
            JPanel progressPanel = new JPanel();
            progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));
            progressPanel.setOpaque(false);
            progressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            int progress = project.getProgress();

            JLabel progressLabel = new JLabel("진척도: ");
            progressLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            JLabel percentLabel = new JLabel(progress + "%");
            percentLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            percentLabel.setForeground(getProgressColor(progress));

            progressPanel.add(progressLabel);
            progressPanel.add(percentLabel);
            progressPanel.add(Box.createHorizontalStrut(10));

            // 진척도 바 배경
            JPanel progressBarBgPanel = ScheduleSwingDesign.JImagePanel("/image/achievement_bar_bg.png");
            progressBarBgPanel.setLayout(new BorderLayout());
            progressBarBgPanel.setPreferredSize(new Dimension(150, 20));
            progressBarBgPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

            // 실제 막대
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setValue(progress);
            progressBar.setStringPainted(false);
            progressBar.setForeground(new Color(45, 206, 137));
            progressBar.setBackground(new Color(0, 0, 0, 0));
            progressBar.setBorderPainted(false);
            progressBar.setOpaque(false);

            progressBarBgPanel.add(progressBar, BorderLayout.CENTER);
            progressPanel.add(progressBarBgPanel);

            centerPanel.add(progressPanel);

            backgroundPanel.add(centerPanel, BorderLayout.CENTER);

            // ─── 더블클릭으로 달력 열기 ───
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        showProjectCalendar(project);
                    }
                }
            });
            add(backgroundPanel, BorderLayout.CENTER);
        }

        // 진척도 색상 반환
        private Color getProgressColor(int progress) {
            if (progress == 100) return new Color(45, 206, 137);
            if (progress >= 70) return new Color(76, 175, 80);
            if (progress >= 40) return new Color(255, 193, 7);
            if (progress > 0) return new Color(255, 152, 0);
            return new Color(158, 158, 158);
        }
    }
}