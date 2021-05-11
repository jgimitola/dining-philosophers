
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author jgimitola, jhanu, anietom
 */
public class Filosofo implements Runnable {

    private final int id;
    private final Semaphore comedor;
    private final Tenedor[] tenedores;
    private final int VECES_A_COMER;
    private Estado estado;
    //Grafico
    private int x;
    private int y;
    private double angulo;
    MainFrame mainFrame;

    public Filosofo(int id, Semaphore comedor, Tenedor[] tenedores) {
        this.id = id;
        this.comedor = comedor;
        this.tenedores = tenedores;
        this.VECES_A_COMER = 1 + (int) (Math.random() * ((2 - 1) + 1));
    }

    public Filosofo(int id, Semaphore comedor, Tenedor[] tenedores, Estado estado, int x, int y, double angulo, MainFrame mainFrame) {
        this.id = id;
        this.comedor = comedor;
        this.tenedores = tenedores;
        this.VECES_A_COMER = 1 + (int) (Math.random() * ((2 - 1) + 1));
        this.estado = estado;
        this.x = x;
        this.y = y;
        this.angulo = angulo;
        this.mainFrame = mainFrame;
    }

    public void pensar() {
        try {
            System.out.printf("El filosofo %d está pensanding.%n", id);
            estado = Estado.PENSANDO;
            mainFrame.actualizarJPanelMesa();
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Filosofo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void comer() {
        try {
            estado = Estado.COMIENDO;
            System.out.printf("El filosofo %d está comiendo.%n", id);
            mainFrame.actualizarJPanelMesa();
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Filosofo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        int i = 0;
        while (i < VECES_A_COMER) {
            try {
                boolean imprimido = false;
                while (!comedor.tryAcquire()) {
                    if (!imprimido) {
                        estado = Estado.ESPERANDO_SILLA;
                        System.out.printf("El filosofo %d está esperando sentarse.%n", id);
                        imprimido = true;
                        mainFrame.actualizarJPanelMesa();
                    }
                }

                pensar();
                tenedores[id].getSemaforo().acquire();
                tenedores[(id + 1) % Restaurante.NUM_FILOSOFOS].getSemaforo().acquire();
            } catch (InterruptedException ex) {
                System.out.println("Exception: " + ex);
            }

            try {
                comer();
            } finally {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Filosofo.class.getName()).log(Level.SEVERE, null, ex);
                }
                tenedores[(id + 1) % Restaurante.NUM_FILOSOFOS].getSemaforo().release();
                tenedores[id].getSemaforo().release();
                comedor.release();
            }
            i++;
        }
        estado = Estado.SACIADO;
        
        mainFrame.actualizarJPanelMesa();
        System.out.printf("El filosofo %d comió %d veces de las %d que debía.%n", id, i, VECES_A_COMER);
    }

    public Estado getEstado() {
        return estado;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getId() {
        return id;
    }

    public Semaphore getComedor() {
        return comedor;
    }

    public Tenedor[] getTenedores() {
        return tenedores;
    }

    public int getVECES_A_COMER() {
        return VECES_A_COMER;
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
}
