import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

public class TaskItemPanel extends JPanel {
    public TaskItemPanel(String text, boolean isChecked, 
                         ActionListener onCheckAction, ActionListener onEditAction) {
        
        // 배경 이미지 패널 사용
        JPanel background = ScheduleSwingDesign.JImagePanel("/image/schedule_background.png", true);
        background.setLayout(new BorderLayout());
        background.setOpaque(false);
        background.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));
        
        // 레이아웃 설정
        setLayout(new BorderLayout());
        setOpaque(false);
        // 높이 고정, 너비는 유동적
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); 
        setPreferredSize(new Dimension(240, 45));

        // 체크박스 설정
        JCheckBox checkBox = new JCheckBox(text, isChecked);
        checkBox.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        checkBox.setOpaque(false);
        checkBox.setFocusPainted(false);
        
        try {
            checkBox.setIcon(new ImageIcon(getClass().getResource("/image/checkbox_off.png")));
            checkBox.setSelectedIcon(new ImageIcon(getClass().getResource("/image/checkbox_on.png")));
        } catch (Exception e) { 
            // 이미지 로드 실패 시 기본 체크박스 사용 
        }

        checkBox.addActionListener(onCheckAction);
        background.add(checkBox, BorderLayout.CENTER);

        // 수정 버튼 (리스너가 있을 때만 표시)
        if (onEditAction != null) {
            JButton editBtn = ScheduleSwingDesign.JimageButton("/image/button_schedule_edit.png");
            ScheduleSwingDesign.disableShape(editBtn);
            editBtn.setPreferredSize(new Dimension(50, 24));
            editBtn.addActionListener(onEditAction);
            background.add(editBtn, BorderLayout.EAST);
        }

        add(background, BorderLayout.CENTER);
    }
}