import java.awt.*;
import javax.swing.*;

public class ScheduleFormDialog extends JDialog {

    // target이 null이면 [추가], 있으면 [수정] 모드로 동작
    public ScheduleFormDialog(JFrame parent, int year, int month, Integer day,
                              Schedule target, ScheduleService service, Runnable onComplete) {
        super(parent, target == null ? "일정 추가" : "일정 수정", true);
        setSize(400, 320);
        setLayout(new GridLayout(5, 1, 10, 10));
        setLocationRelativeTo(parent);

        // 초기값 설정
        int initDay = (target != null) ? target.getDay() : (day != null ? day : 1);
        String initTodo = (target != null) ? target.getTodo() : "";
        int sH = (target != null) ? target.getStartHour() : 9;
        int sM = (target != null) ? target.getStartMinute() : 0;
        int eH = (target != null) ? target.getEndHour() : 10;
        int eM = (target != null) ? target.getEndMinute() : 0;

        // 1. 날짜 패널
        JPanel dayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        dayPanel.add(createLabel("날짜:"));
        JComboBox<Integer> dayBox = new JComboBox<>();
        for (int i = 1; i <= 31; i++) dayBox.addItem(i);
        dayBox.setSelectedItem(initDay);
        dayPanel.add(dayBox);

        // 2. 시간 패널
        JPanel startPanel = createTimePanel("시작 시간:", sH, sM);
        JPanel endPanel = createTimePanel("종료 시간:", eH, eM);
        
        JComboBox<Integer> startH = (JComboBox<Integer>) startPanel.getComponent(1);
        JComboBox<Integer> startM = (JComboBox<Integer>) startPanel.getComponent(3);
        JComboBox<Integer> endH = (JComboBox<Integer>) endPanel.getComponent(1);
        JComboBox<Integer> endM = (JComboBox<Integer>) endPanel.getComponent(3);

        // 3. 내용 패널
        JPanel todoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        todoPanel.add(createLabel("내용:"));
        JTextField todoField = new JTextField(initTodo, 20);
        todoPanel.add(todoField);

        // 4. 버튼 패널
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton cancel = ScheduleSwingDesign.JimageButton("/image/schedule_cancel_button.png");
        JButton save = ScheduleSwingDesign.JimageButton(
                target == null ? "/image/schedule_add_button.png" : "/image/schedule_update_button.png"
        );

        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> {
            ISchedule newSchedule = new Schedule(
                year, month, (int) dayBox.getSelectedItem(),
                (int) startH.getSelectedItem(), (int) startM.getSelectedItem(),
                (int) endH.getSelectedItem(), (int) endM.getSelectedItem(),
                todoField.getText().trim()
            );

            if (!service.addSchedule(newSchedule)) {
                JOptionPane.showMessageDialog(this, "입력값이 올바르지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 수정 모드였다면 기존 일정 삭제
            if (target != null) {
                service.getManager().removeSchedule(target);
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
                if (JOptionPane.showConfirmDialog(this, "정말 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    service.getManager().removeSchedule(target);
                    onComplete.run();
                    dispose();
                }
            });
            btnPanel.add(delete);
        }

        add(dayPanel); add(startPanel); add(endPanel); add(todoPanel); add(btnPanel);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setPreferredSize(new Dimension(80, 25));
        return l;
    }

    private JPanel createTimePanel(String label, int h, int m) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.add(createLabel(label));
        JComboBox<Integer> hb = new JComboBox<>();
        JComboBox<Integer> mb = new JComboBox<>();
        for(int i=0; i<24; i++) hb.addItem(i);
        for(int i=0; i<60; i+=5) mb.addItem(i);
        hb.setSelectedItem(h);
        mb.setSelectedItem(m);
        p.add(hb); p.add(new JLabel("시")); p.add(mb); p.add(new JLabel("분"));
        return p;
    }
}