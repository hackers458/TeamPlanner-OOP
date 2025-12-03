import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 반복 일정 관리 프레임
 * 리팩토링: ISchedule 인터페이스 기반으로 동작
 */
public class RepeatFrame extends JPanel {
    private final ScheduleService service;
    private final JPanel listPanel = new JPanel();
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    public RepeatFrame(ScheduleService service) {
        this.service = service;
        setLayout(new BorderLayout(8,8));
        setBorder(new EmptyBorder(10,10,10,10));

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.add(listPanel);

        JButton addBtn = ScheduleSwingDesign.JimageButton("/image/repeat_new_schedule_button.png");
        addBtn.addActionListener(e -> openEditor());

        JPanel addBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addBtnPanel.setOpaque(false);
        addBtnPanel.add(addBtn);

        container.add(Box.createVerticalStrut(10));
        container.add(addBtnPanel);

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        refresh();
    }

    private void openEditor() {
        JFrame owner = (JFrame) SwingUtilities.getWindowAncestor(this);
        RepeatEditorDialog dlg = new RepeatEditorDialog(owner);
        dlg.setVisible(true);
        List<RepeatSchedule> results = dlg.getResults();
        if (results != null && !results.isEmpty()) {
            for (RepeatSchedule r : results) {
                service.getManager().add(r);
            }
            refresh();
            refreshScheduleFrame();
        }
    }

    public void refresh() {
        listPanel.removeAll();

        // ✅ 리팩토링: getRepeatSchedules() 메서드 사용
        List<RepeatSchedule> all = service.getManager().getRepeatSchedules();

        if (all.isEmpty()) {
            JLabel emptyLabel = new JLabel("등록된 반복 일정이 없습니다.");
            emptyLabel.setOpaque(false);
            listPanel.add(emptyLabel);
        } else {
            // 제목별로 그룹화
            Map<String, List<RepeatSchedule>> grouped =
                    all.stream().collect(Collectors.groupingBy(RepeatSchedule::getTodo));

            for (Map.Entry<String, List<RepeatSchedule>> entry : grouped.entrySet()) {
                String title = entry.getKey();
                List<RepeatSchedule> group = entry.getValue();
                listPanel.add(new RepeatCard(title, group, service, this));
                listPanel.add(Box.createVerticalStrut(8));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void refreshScheduleFrame() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof ScheduleFrame) {
            ((ScheduleFrame) window).updateCalendar();
        }
    }

    private static class RepeatCard extends JPanel {
        private final String title;
        private final List<RepeatSchedule> group;
        private final ScheduleService service;
        private final RepeatFrame owner;

        public RepeatCard(String title, List<RepeatSchedule> group,
                          ScheduleService service, RepeatFrame owner) {
            this.title = title;
            this.group = group;
            this.service = service;
            this.owner = owner;

            int baseHeight = 100;
            int perRuleHeight = 35;
            int calculatedHeight = baseHeight + (group.size() * perRuleHeight);
            int barHeight = Math.max(150, calculatedHeight);

            JPanel backgroundPanel = ScheduleSwingDesign.JImagePanel("/image/repeat_background_middle.png", true);
            backgroundPanel.setLayout(new BorderLayout());

            setLayout(new BorderLayout());
            setBorder(null);
            setOpaque(false);
            setPreferredSize(new Dimension(0, barHeight));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, barHeight));

            backgroundPanel.setPreferredSize(new Dimension(0, barHeight));
            backgroundPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(titleLbl.getFont().deriveFont(Font.BOLD, 15f));
            backgroundPanel.add(titleLbl, BorderLayout.NORTH);

            JPanel rulesPanel = new JPanel();
            rulesPanel.setLayout(new BoxLayout(rulesPanel, BoxLayout.Y_AXIS));
            rulesPanel.setOpaque(false);
            for (RepeatSchedule r : group) {
                rulesPanel.add(createRuleRow(r));
            }
            backgroundPanel.add(rulesPanel, BorderLayout.CENTER);

            JButton applyBtn = ScheduleSwingDesign.JimageButton("/image/repeat_this_month_place_button.png");
            applyBtn.setToolTipText("이 달에 일정 생성");
            applyBtn.addActionListener(e -> applyAll());

            JButton deleteAllBtn = ScheduleSwingDesign.JimageButton("/image/repeat_group_delete_button.png");
            deleteAllBtn.addActionListener(e -> deleteAllInGroup());

            JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            south.setOpaque(false);
            south.add(deleteAllBtn);
            south.add(applyBtn);
            backgroundPanel.add(south, BorderLayout.SOUTH);

            add(backgroundPanel, BorderLayout.CENTER);
        }

        private void deleteAllInGroup() {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "이 제목(" + title + ")의 모든 반복 패턴과\n이미 생성된 일정들을 모두 삭제하시겠습니까?",
                    "전체 삭제 확인",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                for (RepeatSchedule r : new ArrayList<>(group)) {
                    service.getManager().removeSchedule(r);
                }
                owner.refresh();
                owner.refreshScheduleFrame();
            }
        }

        private JPanel createRuleRow(RepeatSchedule r) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.setOpaque(false);

            String dowLabel = r.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREA);
            String text = dowLabel + " " +
                    r.getStartTime().format(TF) + " ~ " + r.getEndTime().format(TF);

            JLabel label = new JLabel(text);
            row.add(label);

            JButton editBtn = ScheduleSwingDesign.JimageButton("/image/repeat_edit_button.png");
            editBtn.addActionListener(e -> editRule(r));

            JButton delBtn = ScheduleSwingDesign.JimageButton("/image/repeat_delete_button.png");
            delBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "이 패턴과 관련된 일정을 삭제하시겠습니까?\n" + text,
                        "삭제 확인", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    service.getManager().removeSchedule(r);
                    owner.refresh();
                    owner.refreshScheduleFrame();
                }
            });

            row.add(editBtn);
            row.add(delBtn);
            return row;
        }

        private void applyAll() {
            YearMonth ym = service.getCurrentMonth();
            int count = 0;
            for (RepeatSchedule r : group) {
                List<LocalDate> days = r.occurrencesInMonth(ym);
                for (LocalDate d : days) {
                    service.getManager().add(r.toConcrete(d));
                    count++;
                }
            }
            JOptionPane.showMessageDialog(this, ym + "에 " + count + "개의 일정이 추가되었습니다.");
            owner.refreshScheduleFrame();
        }

        private void editRule(RepeatSchedule oldRule) {
            JDialog dlg = new JDialog((JFrame)SwingUtilities.getWindowAncestor(owner),
                    "패턴 수정", true);

            dlg.setSize(400, 280);
            dlg.setLocationRelativeTo(owner);
            dlg.setLayout(new BorderLayout(8,8));

            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(4,4,4,4);
            gc.fill = GridBagConstraints.HORIZONTAL;

            JTextField titleField = new JTextField(oldRule.getTodo());
            titleField.setPreferredSize(new Dimension(200, 25));

            JComboBox<DayOfWeek> dayCombo = new JComboBox<>(new DayOfWeek[]{
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
            });
            dayCombo.setSelectedItem(oldRule.getDayOfWeek());

            Date sDate = Date.from(
                    LocalDate.now().atTime(oldRule.getStartTime())
                            .atZone(ZoneId.systemDefault()).toInstant());
            Date eDate = Date.from(
                    LocalDate.now().atTime(oldRule.getEndTime())
                            .atZone(ZoneId.systemDefault()).toInstant());

            SpinnerDateModel sm1 = new SpinnerDateModel(sDate, null, null, Calendar.MINUTE);
            SpinnerDateModel sm2 = new SpinnerDateModel(eDate, null, null, Calendar.MINUTE);
            JSpinner startSpinner = new JSpinner(sm1);
            JSpinner endSpinner = new JSpinner(sm2);
            JSpinner.DateEditor ed1 = new JSpinner.DateEditor(startSpinner, "HH:mm");
            JSpinner.DateEditor ed2 = new JSpinner.DateEditor(endSpinner, "HH:mm");
            startSpinner.setEditor(ed1);
            endSpinner.setEditor(ed2);

            JPanel timePanel = new JPanel(new GridLayout(1,4,4,4));
            timePanel.add(new JLabel("시작"));
            timePanel.add(startSpinner);
            timePanel.add(new JLabel("종료"));
            timePanel.add(endSpinner);

            int r = 0;
            gc.gridx=0; gc.gridy=r; form.add(new JLabel("일정명"), gc);
            gc.gridx=1; gc.gridy=r; form.add(titleField, gc); r++;
            gc.gridx=0; gc.gridy=r; form.add(new JLabel("요일"), gc);
            gc.gridx=1; gc.gridy=r; form.add(dayCombo, gc); r++;
            gc.gridx=0; gc.gridy=r; form.add(new JLabel("시간"), gc);
            gc.gridx=1; gc.gridy=r; form.add(timePanel, gc); r++;

            dlg.add(form, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel = ScheduleSwingDesign.JimageButton("/image/cancel_button.png");
            JButton save = ScheduleSwingDesign.JimageButton("/image/schedule_save_button.png");
            bottom.add(cancel);
            bottom.add(save);
            dlg.add(bottom, BorderLayout.SOUTH);

            cancel.addActionListener(e -> dlg.dispose());
            save.addActionListener(e -> {
                String newTitle = titleField.getText().trim();
                if (newTitle.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "일정명을 입력하세요.",
                            "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Date ns = (Date) startSpinner.getValue();
                Date ne = (Date) endSpinner.getValue();
                LocalTime newStart = ns.toInstant().atZone(ZoneId.systemDefault())
                        .toLocalTime().withSecond(0).withNano(0);
                LocalTime newEnd = ne.toInstant().atZone(ZoneId.systemDefault())
                        .toLocalTime().withSecond(0).withNano(0);

                if (!newEnd.isAfter(newStart)) {
                    JOptionPane.showMessageDialog(dlg, "종료 시간은 시작 시간보다 늦어야 합니다.",
                            "오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                DayOfWeek newDow = (DayOfWeek) dayCombo.getSelectedItem();
                LocalDate base = LocalDate.now();
                while (base.getDayOfWeek() != newDow) base = base.plusDays(1);

                // ✅ 기존 패턴 삭제 (파생 일정도 함께 삭제됨)
                service.getManager().removeSchedule(oldRule);

                // ✅ 새로운 패턴 추가
                RepeatSchedule newRule = new RepeatSchedule(
                        newTitle, newDow, newStart, newEnd, base);
                service.getManager().add(newRule);

                dlg.dispose();
                owner.refresh();
                owner.refreshScheduleFrame();

                JOptionPane.showMessageDialog(owner,
                        "반복 패턴이 수정되었습니다.\n변경된 내용을 캘린더에 적용하려면\n'이 달에 배치' 버튼을 눌러주세요.",
                        "수정 완료",
                        JOptionPane.INFORMATION_MESSAGE);
            });

            dlg.setVisible(true);
        }
    }
}