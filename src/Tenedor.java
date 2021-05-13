
import java.util.concurrent.Semaphore;

/**
 *
 * @author jgimitola, jhanu, anietom
 *
 */
public class Tenedor {

    //Modelo
    private final int id;
    private final Semaphore semaforo;
    private EstadoTenedor estado;

    //Grafico    
    private double angulo;
    private MainFrame mainFrame;

    public Tenedor(int id, Semaphore semaforo, double angulo, EstadoTenedor estado, MainFrame mainFrame) {
        this.id = id;
        this.semaforo = semaforo;
        this.angulo = angulo;
        this.estado = estado;
        this.mainFrame = mainFrame;
    }

    public void setEstado(EstadoTenedor estado) {
        this.estado = estado;
    }

    public EstadoTenedor getEstado() {
        return estado;
    }

    public double getAngulo() {
        return angulo;
    }

    public int getId() {
        return id;
    }

    public Semaphore getSemaforo() {
        return semaforo;
    }

}
