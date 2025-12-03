import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;

public class CalendarGridPanel extends JPanel {
    
    // 셀 렌더링 전략 (어떻게 꾸밀지 외부에서 결정)
    public interface CellRenderer {
        void render(JPanel cellPanel, LocalDate date);
    }

    // 날짜 클릭 이벤트
    public interface DateClickListener {
        void onDateClick(LocalDate date);
    }

    public CalendarGridPanel() {
        setLayout(new GridLayout(0, 7, 0, 0)); // 7열 그리드
        setOpaque(false);
    }

    public void updateCalendar(int year, int month, LocalDate selectedDate, 
                               CellRenderer renderer, DateClickListener clickListener) {
        removeAll();

        // 1. 요일 헤더
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        for (String d : days) {
            JLabel lbl = new JLabel(d, JLabel.CENTER);
            lbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            lbl.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
            add(lbl);
        }

        // 2. 날짜 계산
        LocalDate firstDay = LocalDate.of(year, month, 1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; // 일=0, 월=1 ...
        int daysInMonth = firstDay.lengthOfMonth();

        // 3. 빈 칸 채우기
        for (int i = 0; i < startDayOfWeek; i++) {
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            empty.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            add(empty);
        }

        // 4. 날짜 셀 채우기
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate current = LocalDate.of(year, month, day);
            
            JPanel cell = new JPanel();
            cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
            cell.setOpaque(false);
            cell.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));

            // 날짜 라벨
            JLabel dateLabel = new JLabel(day + "일");
            dateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            if (selectedDate != null && current.equals(selectedDate)) {
                dateLabel.setForeground(Color.RED);
                cell.setOpaque(true); // 선택된 날짜 배경 불투명 처리 필요시
            }
            cell.add(dateLabel);

            // 외부 렌더러 호출 (내용 채우기)
            if (renderer != null) {
                renderer.render(cell, current);
            }

            // 클릭 리스너 연결
            cell.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (clickListener != null) clickListener.onDateClick(current);
                }
            });

            add(cell);
        }

        revalidate();
        repaint();
    }
}