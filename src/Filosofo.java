
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jgimitola, jhanu, anietom
 */
public class Filosofo implements Runnable {

    /* Modelo */
    private final int id;
    private int consumido;
    private final Semaphore comedor;
    private Tenedor derecho;
    private Tenedor izquierdo;
    private final int VECES_A_COMER;
    private EstadoFilosofo estado;

    /* Grafico */
    private int x;
    private int y;
    private double angulo;
    public MainFrame mainFrame;
    private int tiempoEspera = 2100;

    public Filosofo(int id, Semaphore comedor, Tenedor izquierdo, Tenedor derecho, EstadoFilosofo estado, int x, int y, double angulo, MainFrame mainFrame) {
        this.id = id;
        this.consumido = 0;
        this.comedor = comedor;
        this.derecho = derecho;
        this.izquierdo = izquierdo;
        this.VECES_A_COMER = 1 + (int) (Math.random() * ((10 - 1) + 1));
        this.estado = estado;

        this.x = x;
        this.y = y;
        this.angulo = angulo;
        this.mainFrame = mainFrame;
    }

    public void pintar() {
        mainFrame.actualizarJPanelMesa();
    }

    @Override
    public void run() {

        int der = derecho.getId();
        int izq = izquierdo.getId();

        while (consumido < VECES_A_COMER) {
            try {
                mainFrame.agregarTextTo(String.format("F-%d está esperando sentarse.%n", id));
                estado = EstadoFilosofo.ESPERANDO_SILLA;
                pintar();
                espera();

                comedor.acquire();

                mainFrame.agregarTextTo(String.format("F-%d está pensando.%n", id));
                estado = EstadoFilosofo.PENSANDO;
                pintar();
                espera();

                mainFrame.agregarTextTo(String.format("F-%d quiere tomar D-T-%d.%n", id, derecho.getId()));
                derecho.getSemaforo().acquire();

                mainFrame.agregarTextTo(String.format("F-%d tomó D-T-%d.%n", id, der));
                derecho.setEstado(EstadoTenedor.TOMADO_DERECHA);
                pintar();

                mainFrame.agregarTextTo(String.format("F-%d quiere tomar I-T-%d.%n", id, izq));
                izquierdo.getSemaforo().acquire();

                mainFrame.agregarTextTo(String.format("F-%d tomó I-T-%d.%n", id, izq));
                izquierdo.setEstado(EstadoTenedor.TOMADO_IZQUIERDA);
                pintar();

            } catch (InterruptedException ex) {
                System.out.println("Exception: " + ex);
            }

            try {

                mainFrame.agregarTextTo(String.format("F-%d está comiendo.%n", id));
                estado = EstadoFilosofo.COMIENDO;

                derecho.setEstado(EstadoTenedor.TOMADO_DERECHA);
                izquierdo.setEstado(EstadoTenedor.TOMADO_IZQUIERDA);

                pintar();
                espera();

            } finally {

                mainFrame.agregarTextTo(String.format("F-%d terminó de comer.%n", id));

                izquierdo.getSemaforo().release();
                mainFrame.agregarTextTo(String.format("F-%d suelta I-T-%d.%n", id, izq));
                izquierdo.setEstado(EstadoTenedor.SUELTO);
                pintar();

                derecho.getSemaforo().release();
                mainFrame.agregarTextTo(String.format("F-%d suelta D-T-%d.%n", id, der));
                derecho.setEstado(EstadoTenedor.SUELTO);
                pintar();

                comedor.release();
                mainFrame.agregarTextTo(String.format("F-%d se para.%n", id));
                estado = EstadoFilosofo.ESPERANDO_SILLA;
                pintar();

            }
            consumido++;
        }
        mainFrame.agregarTextTo(String.format("F-%d comió %d veces de %d.%n", id, consumido, VECES_A_COMER));
        estado = EstadoFilosofo.SACIADO;
        pintar();
    }

    public void espera() {
        try {
            Thread.sleep(tiempoEspera);
        } catch (InterruptedException ex) {
            Logger.getLogger(Filosofo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public EstadoFilosofo getEstado() {
        return estado;
    }

    public int getId() {
        return id;
    }

    public int getConsumido() {
        return consumido;
    }

    public int getVECES_A_COMER() {
        return VECES_A_COMER;
    }

    public double getAngulo() {
        return angulo;
    }

}
