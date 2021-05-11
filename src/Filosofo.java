
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jgimitola, jhanu, anietom
 */
public class Filosofo implements Runnable {

    private final int id;
    private final Semaphore comedor;
    private final Semaphore[] tenedores;
    private final int VECES_A_COMER;
    private Estado estado;

    public Filosofo(int id, Semaphore comedor, Semaphore[] tenedores) {
        this.id = id;
        this.comedor = comedor;
        this.tenedores = tenedores;
        this.VECES_A_COMER = 1 + (int) (Math.random() * ((2 - 1) + 1));
    }

    public void pensar() {
        try {
            System.out.printf("El filosofo %d está pensanding.%n", id);
            estado = Estado.PENSANDO;
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Filosofo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Estado getEstado() {
        return estado;
    }

    public void comer() {
        try {
            estado = Estado.COMIENDO;
            System.out.printf("El filosofo %d está comiendo.%n", id);
            Thread.sleep(1000);
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
                    }
                }

                pensar();
                tenedores[id].acquire();
                tenedores[(id + 1) % Restaurante.NUM_FILOSOFOS].acquire();
            } catch (InterruptedException ex) {
                System.out.println("Exception: " + ex);
            }

            try {
                comer();
            } finally {
                tenedores[(id + 1) % Restaurante.NUM_FILOSOFOS].release();
                tenedores[id].release();
                comedor.release();
            }
            i++;
        }
        estado = Estado.SACIADO;
        System.out.printf("El filosofo %d comió %d veces de las %d que debía.%n", id, i, VECES_A_COMER);
    }
}
