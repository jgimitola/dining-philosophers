
import java.util.concurrent.Semaphore;
import javax.swing.ImageIcon;

/**
 *
 * @author JhanU
 */
public class Tenedor {

    //Modelo
    private final int id;
    private final Semaphore semaforo;
    private EstadoTenedor estado;

    //Grafico
    private int x;
    private int y;
    private double angulo;
    private MainFrame mainFrame;
    private RotateLabel sprite;

    public Tenedor(int id, Semaphore semaforo) {
        this.id = id;
        this.semaforo = semaforo;
    }

    public Tenedor(int id, Semaphore semaforo, int x, int y, double angulo, EstadoTenedor estado, MainFrame mainFrame) {
        this.id = id;
        this.semaforo = semaforo;
        this.x = x;
        this.y = y;
        this.angulo = angulo;
        this.estado = estado;
        this.mainFrame = mainFrame;
        this.sprite = new RotateLabel("", angulo, this);
        /*
        mainFrame.getJPanelMesa().add(sprite);
        sprite.setBounds(x, y, 32, 32);
        sprite.setIcon(new ImageIcon("imagenes/fork.png"));
         */
    }

    public void pintar() {
        try {
            switch (estado) {
                case SUELTO:
                    angulo = angulo - mainFrame.DIFERENCIA / 2;
                    break;
                case TOMADO_DERECHA:
                    angulo = angulo - (mainFrame.DIFERENCIA * 0.15);
                    break;
                case TOMADO_IZQUIERDA:
                    angulo = angulo - (mainFrame.DIFERENCIA * 0.85);
                    break;
            }
            x = (int) (mainFrame.CENTROX + (Math.sin(angulo) * mainFrame.RADIO_FILOSOFOS));
            y = (int) (mainFrame.CENTROY - (Math.cos(angulo) * mainFrame.RADIO_FILOSOFOS));
        } catch (Exception e) {
            System.out.println(e);
        }
        sprite.setBounds(x, y, 32, 32);
    }

    public void setEstado(EstadoTenedor estado) {
        this.estado = estado;
    }

    public EstadoTenedor getEstado() {
        return estado;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getAngulo() {
        return angulo;
    }

    public void setAngulo(double angulo) {
        this.angulo = angulo;
    }

    public int getId() {
        return id;
    }

    public Semaphore getSemaforo() {
        return semaforo;
    }

}
