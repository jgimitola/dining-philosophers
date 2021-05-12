
import java.awt.*;
import javax.swing.*;

public class RotateLabel extends JLabel {

    double angulo;
    Object elemeneto;

    public RotateLabel(String text, double angulo, Object elemento) {
        super(text);
        // setBounds(x, y, (int) width, (int) height);
        setForeground(Color.YELLOW);
        setBorder(BorderFactory.createLineBorder(Color.YELLOW, 1));
        this.angulo = angulo;
        this.elemeneto = elemento;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D gx = (Graphics2D) g;
        if (elemeneto instanceof Filosofo) {
            gx.rotate(angulo, getWidth() / 2, getHeight());
        } else {
            gx.rotate(angulo, getWidth() / 2, 20);
        }
        super.paintComponent(g);
    }
}
