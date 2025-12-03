import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 반복 일정 작성 다이얼로그 (Single Responsibility Principle)
 * 반복 일정 패턴 입력 UI만 담당
 */
public class RepeatEditorDialog extends JDialog {

    private JTextField titleField;
    private JComboBox<DayOfWeek> dayCombo;
    private JSpinner startSpinner;
    private JSpinner endSpinner;
    private DefaultListModel<String> ruleListModel = new DefaultListModel<>();
    private JList<String> ruleList = new JList<>(ruleListModel);
    private List<Rule> rules = new ArrayList<>();
    private List<RepeatSchedule> results;

    /**
     * 내부적으로만 쓰는 요일/시간 패턴 구조체
     * Single Responsibility: 패턴 데이터만 보관
     */
    private static class Rule {
        DayOfWeek dow;
        LocalTime start;
        LocalTime end;

        Rule(DayOfWeek d, LocalTime s, LocalTime e) {
            this.dow = d;
            this.start = s;
            this.end = e;
        }
    }

    public RepeatEditorDialog(JFrame owner) {
        super(owner, "반복 일정 작성", true);
        setSize(500, 420);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new javax.swing.border.EmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        // 상단: 제목 입력
        root.add(createTitlePanel(), BorderLayout.NORTH);

        // 가운데: 패턴 입력 + 목록
        root.add(createCenterPanel(), BorderLayout.CENTER);

        // 하단: 저장/취소
        root.add(createButtonPanel(), BorderLayout.SOUTH);
    }

    /**
     * 제목 입력 패널 생성
     */
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JLabel("일정명"), BorderLayout.WEST);

        titleField = new JTextField();
        panel.add(titleField, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 중앙 패널 생성 (패턴 입력 + 목록)
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        // 패턴 입력 영역
        panel.add(createPatternInputPanel(), BorderLayout.NORTH);

        // 패턴 목록
        ruleList.setVisibleRowCount(6);
        panel.add(new JScrollPane(ruleList), BorderLayout.CENTER);

        return panel;
    }

    /**
     * 패턴 입력 영역 생성
     */
    private JPanel createPatternInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // 요일 콤보
        dayCombo = new JComboBox<>(new DayOfWeek[]{
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
        });

        // 시작/종료 시간 스피너
        SpinnerDateModel sm1 = new SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE);
        SpinnerDateModel sm2 = new SpinnerDateModel(
                new java.util.Date(System.currentTimeMillis() + 60L * 60L * 1000L),
                null, null, java.util.Calendar.MINUTE);

        startSpinner = new JSpinner(sm1);
        endSpinner = new JSpinner(sm2);

        JSpinner.DateEditor ed1 = new JSpinner.DateEditor(startSpinner, "HH:mm");
        JSpinner.DateEditor ed2 = new JSpinner.DateEditor(endSpinner, "HH:mm");
        startSpinner.setEditor(ed1);
        endSpinner.setEditor(ed2);

        JPanel timePanel = new JPanel(new GridLayout(1, 4, 4, 4));
        timePanel.add(new JLabel("시작"));
        timePanel.add(startSpinner);
        timePanel.add(new JLabel("종료"));
        timePanel.add(endSpinner);

        // 레이아웃 배치
        int r = 0;
        gc.gridx = 0; gc.gridy = r; panel.add(new JLabel("요일"), gc);
        gc.gridx = 1; gc.gridy = r; panel.add(dayCombo, gc); r++;

        gc.gridx = 0; gc.gridy = r; panel.add(new JLabel("시간"), gc);
        gc.gridx = 1; gc.gridy = r; panel.add(timePanel, gc); r++;

        JButton addRuleBtn = ScheduleSwingDesign.JimageButton("/image/repeat_button_week_add.png");
        addRuleBtn.addActionListener(e -> addRule());
        gc.gridx = 1; gc.gridy = r; panel.add(addRuleBtn, gc);

        return panel;
    }

    /**
     * 버튼 패널 생성
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton cancelBtn = ScheduleSwingDesign.JimageButton("/image/button_dialog_cancel.png");
        JButton saveBtn = ScheduleSwingDesign.JimageButton("/image/button_dialog_add.png");

        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> onSave());

        panel.add(cancelBtn);
        panel.add(saveBtn);

        return panel;
    }

    /**
     * 패턴 하나 추가 (요일 + 시작/종료 시간)
     * Single Responsibility: 패턴 검증과 추가만 담당
     */
    private void addRule() {
        DayOfWeek dow = (DayOfWeek) dayCombo.getSelectedItem();
        java.util.Date sDate = (java.util.Date) startSpinner.getValue();
        java.util.Date eDate = (java.util.Date) endSpinner.getValue();

        LocalTime start = sDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime().withSecond(0).withNano(0);
        LocalTime end = eDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime().withSecond(0).withNano(0);

        if (!end.isAfter(start)) {
            JOptionPane.showMessageDialog(this,
                    "종료 시간은 시작 시간보다 늦어야 합니다.",
                    "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 목록에 데이터 추가
        rules.add(new Rule(dow, start, end));

        // 목록 표시용 문자열
        String text = formatRuleText(dow, start, end);
        ruleListModel.addElement(text);
    }

    /**
     * 패턴을 읽기 쉬운 문자열로 포맷팅
     */
    private String formatRuleText(DayOfWeek dow, LocalTime start, LocalTime end) {
        java.time.format.DateTimeFormatter tf =
                java.time.format.DateTimeFormatter.ofPattern("HH:mm");

        String dowLabel = switch (dow) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };

        return dowLabel + " " + start.format(tf) + " ~ " + end.format(tf);
    }

    /**
     * 저장 버튼 처리: rules 리스트를 RepeatSchedule 리스트로 변환
     * Open-Closed: 새로운 스케줄 타입 추가 시 확장 가능
     */
    private void onSave() {
        String title = titleField.getText().trim();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "일정명을 입력하세요.",
                    "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (rules.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "최소 한 개의 요일/시간 패턴을 추가하세요.",
                    "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<RepeatSchedule> out = new ArrayList<>();

        for (Rule rule : rules) {
            try {
                // 기준일: 오늘 기준 첫 번째 해당 요일
                LocalDate base = calculateBaseDate(rule.dow);

                out.add(new RepeatSchedule(title, rule.dow, rule.start, rule.end, base));
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        this.results = out;
        dispose();
    }

    /**
     * 기준 날짜 계산: 오늘 이후 첫 번째 해당 요일
     */
    private LocalDate calculateBaseDate(DayOfWeek targetDow) {
        LocalDate base = LocalDate.now();
        while (base.getDayOfWeek() != targetDow) {
            base = base.plusDays(1);
        }
        return base;
    }

    /**
     * 여러 요일/시간 패턴에 대한 RepeatSchedule 리스트 반환
     */
    public List<RepeatSchedule> getResults() {
        return results;
    }
}