import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;

public class ProjectTaskFormDialog extends JDialog {

    // target이 null이면 [추가], 있으면 [수정]
    public ProjectTaskFormDialog(JFrame parent, Project project, ProjectTask target,
                                 int year, int month, Integer day, Runnable onComplete) {
        super(parent, target == null ? "할일 추가" : "할일 수정", true);
        setSize(400, 200);
        setLayout(new GridLayout(3, 1, 10, 10));
        setLocationRelativeTo(parent);

        int initDay = (target != null) ? target.getDate().getDayOfMonth() : (day != null ? day : 1);
        String initContent = (target != null) ? target.getContent() : "";

        // 1. 날짜 패널
        JPanel dayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JLabel l1 = new JLabel("날짜:"); l1.setPreferredSize(new Dimension(80, 25));
        dayPanel.add(l1);
        JComboBox<Integer> dayBox = new JComboBox<>();
        for(int i=1; i<=31; i++) dayBox.addItem(i);
        dayBox.setSelectedItem(initDay);
        dayPanel.add(dayBox);

        // 2. 내용 패널
        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JLabel l2 = new JLabel("내용:"); l2.setPreferredSize(new Dimension(80, 25));
        contentPanel.add(l2);
        JTextField contentField = new JTextField(initContent, 20);
        contentPanel.add(contentField);

        // 3. 버튼 패널
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton cancel = ScheduleSwingDesign.JimageButton("/image/schedule_cancel_button.png");
        JButton save = ScheduleSwingDesign.JimageButton(
                target == null ? "/image/schedule_add_button.png" : "/image/schedule_update_button.png"
        );

        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> {
            String content = contentField.getText().trim();
            if(content.isEmpty()) {
                JOptionPane.showMessageDialog(this, "내용을 입력하세요.");
                return;
            }

            LocalDate newDate = LocalDate.of(year, month, (int)dayBox.getSelectedItem());
            
            if (target == null) {
                // 추가
                try {
                    project.addTask(new ProjectTask(newDate, content));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                    return;
                }
            } else {
                // 수정
                target.setDate(newDate);
                target.setContent(content);
            }
            onComplete.run();
            dispose();
        });

        btnPanel.add(cancel);
        btnPanel.add(save);

        // 삭제 버튼 (수정 모드일 때만)
        if (target != null) {
            JButton delete = ScheduleSwingDesign.JimageButton("/image/schedule_delete_button.png");
            delete.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(this, "삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    project.removeTask(target);
                    onComplete.run();
                    dispose();
                }
            });
            btnPanel.add(delete);
        }

        add(dayPanel); add(contentPanel); add(btnPanel);
    }
}