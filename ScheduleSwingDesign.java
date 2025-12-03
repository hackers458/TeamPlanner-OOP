import java.awt.*;
import java.net.URL;
import javax.swing.*;

public class ScheduleSwingDesign {
    public static JPanel createScrollWrapper(JPanel panel) {
    JScrollPane scrollPane = new JScrollPane(panel);
    scrollPane.setPreferredSize(panel.getPreferredSize());
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    scrollPane.setBorder(null);
    scrollPane.setViewportBorder(null);

    JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    wrapper.setOpaque(false);
    wrapper.setBorder(null);
    wrapper.add(scrollPane);

    return wrapper;
}

    // ✅ 이미지 배경 패널 클래스 (이미지 크기 그대로)
public static JPanel JImagePanel(String path) {
    return JImagePanel(path, false);
}

// ✅ 이미지 배경 패널 클래스 (크기 조절 가능)
public static JPanel JImagePanel(String path, boolean scaleToFit) {
    URL url = ScheduleSwingDesign.class.getResource(path);
    if (url == null) {
        System.out.println("이미지 경로 오류: " + path);
        return new JPanel();
    }

    ImageIcon icon = new ImageIcon(url);
    Image backgroundImage = icon.getImage();
    int imageWidth = icon.getIconWidth();
    int imageHeight = icon.getIconHeight();

    JPanel panel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (scaleToFit) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.drawImage(backgroundImage, 0, 0, imageWidth, imageHeight, this);
            }
        }
    };

    panel.setOpaque(false);
    panel.setBorder(null);

    if (!scaleToFit) {
        panel.setPreferredSize(new Dimension(imageWidth, imageHeight));
        panel.setMinimumSize(new Dimension(imageWidth, imageHeight));
        panel.setMaximumSize(new Dimension(imageWidth, imageHeight));
    }

    return panel;
}


    // ✅ 버튼 모양 네모 제거
    public static void disableShape(JButton shape) {
        shape.setBorderPainted(false);
        shape.setContentAreaFilled(false);
        shape.setFocusPainted(false);
    }

    // ✅ 이미지 버튼 생성
    public static JButton JimageButton(String path) {
        URL url = ScheduleSwingDesign.class.getResource(path);
        if (url == null) {
            System.out.println("이미지 경로 오류: " + path);
            return new JButton("이미지 없음");
        }
        ImageIcon useImage = new ImageIcon(url);
        JButton useButton = new JButton(useImage);
        useButton.setPreferredSize(new Dimension(
            useImage.getIconWidth(), useImage.getIconHeight()));
        disableShape(useButton);
        return useButton;
    }

    // ✅ 이미지 라벨 생성
    public static JLabel JImageLabel(String path) {
        URL url = ScheduleSwingDesign.class.getResource(path);
        if (url == null) {
            System.out.println("이미지 경로 오류: " + path);
            return new JLabel("이미지 없음");
        }
        return new JLabel(new ImageIcon(url));
    }


}