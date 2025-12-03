import java.awt.*;
import javax.swing.*;

public class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        // 이미지 로딩
        System.out.println("이미지 경로 확인: " + getClass().getResource(imagePath));
        backgroundImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 이미지 전체 크기로 그리기
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}