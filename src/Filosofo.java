
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.RenderingHints;

import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author jgimitola, jhanu, anietom
 */
public class Filosofo implements Runnable {

    /* Modelo */
    private final int id;
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
    private static int TIEMPO_ACTIVIDAD = 5000;
    private RotateLabel sprite;
    private int tiempoEspera = 5000;

    public Filosofo(int id, Semaphore comedor, Tenedor izquierdo, Tenedor derecho, EstadoFilosofo estado, int x, int y, double angulo, MainFrame mainFrame) {
        this.id = id;
        this.comedor = comedor;
        this.derecho = derecho;
        this.izquierdo = izquierdo;
        this.VECES_A_COMER = 1 + (int) (Math.random() * ((2 - 1) + 1)); // TODO: Cambiar 2 por 10 cuando esté listo.
        this.estado = estado;

        this.x = x;
        this.y = y;
        this.angulo = angulo;
        this.mainFrame = mainFrame;

        /* -----  Para pintar con labels ----- */
        sprite = new RotateLabel("", angulo, this);
//        mainFrame.getJPanelMesa().add(sprite);
//        sprite.setBounds(x, y, 91, 91);
        /*------------------------------------ */
    }

    public void pintar() {
        mainFrame.actualizarJPanelMesa();
        /*
        ImageIcon image = null;
        System.out.println("Esta pintando");
        try {
            switch (estado) {
                case COMIENDO:
                    image = new ImageIcon("imagenes/sentado_comiendo.png");
                    break;
                case PENSANDO:
                    image = new ImageIcon("imagenes/pensando.png");
                    break;
                case ESPERANDO_SILLA:
                    //De pie
                    image = new ImageIcon("imagenes/silla_vacia.png");
                    break;
                case ESPERANDO_TENEDOR:
                    image = new ImageIcon("imagenes/sentado_esperando.png");
                    break;
                case SACIADO:
                    image = new ImageIcon("imagenes/saciado.png");
                    break;
                default:
                    throw new AssertionError();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        sprite.setIcon(image);
        tenedores[izq].pintar();
        tenedores[der].pintar();
         */

 /*
        System.out.println("Esta pintando");

        AffineTransform at = AffineTransform.getTranslateInstance(0, 0);
        at.concatenate(AffineTransform.getRotateInstance(angulo));
        Graphics2D g2 = (Graphics2D) g.create();
        g2.transform(at);
        g.drawImage(image.getImage(), 0, 0, sprite);
         */
    }

    @Override
    public void run() {
        int i = 0;

        // Quitar estas variables cuando ya no se imprima en consola.
        int der = derecho.getId();
        int izq = izquierdo.getId();

        while (i < VECES_A_COMER) {
            try {
                System.out.printf("F-%d está esperando sentarse.%n", id);
                System.out.flush();
                mainFrame.agregarTextTo(String.format("F-%d está esperando sentarse.%n", id));
                estado = EstadoFilosofo.ESPERANDO_SILLA;
                pintar();

                comedor.acquire();

                System.out.printf("F-%d está pensando.%n", id);
                mainFrame.agregarTextTo(String.format("F-%d está pensando.%n", id));
                estado = EstadoFilosofo.PENSANDO;
                pintar();

                System.out.printf("F-%d quiere tomar D-T-%d.%n", id, derecho.getId());
                mainFrame.agregarTextTo(String.format("F-%d quiere tomar D-T-%d.%n", id, derecho.getId()));
                derecho.getSemaforo().acquire();
                System.out.printf("F-%d tomó D-T-%d.%n", id, der);
                mainFrame.agregarTextTo(String.format("F-%d tomó D-T-%d.%n", id, der));
                derecho.setEstado(EstadoTenedor.TOMADO_DERECHA);
                pintar();

                System.out.printf("F-%d quiere tomar I-T-%d.%n", id, izq);
                mainFrame.agregarTextTo(String.format("F-%d quiere tomar I-T-%d.%n", id, izq));
                izquierdo.getSemaforo().acquire();
                System.out.printf("F-%d tomó I-T-%d.%n", id, izq);
                mainFrame.agregarTextTo(String.format("F-%d tomó I-T-%d.%n", id, izq));
                izquierdo.setEstado(EstadoTenedor.TOMADO_IZQUIERDA);
                pintar();

            } catch (InterruptedException ex) {
                System.out.println("Exception: " + ex);
            }

            try {
                System.out.printf("F-%d está comiendo.%n", id);
                mainFrame.agregarTextTo(String.format("F-%d está comiendo.%n", id));
                estado = EstadoFilosofo.COMIENDO;

                derecho.setEstado(EstadoTenedor.TOMADO_DERECHA);
                izquierdo.setEstado(EstadoTenedor.TOMADO_IZQUIERDA);

                System.out.printf("Antes F-%d I-T-%d = %s.%n", id, izq, izquierdo.getEstado());
                System.out.printf("Antes F-%d D-T-%d = %s.%n", id, der, derecho.getEstado());

                pintar();

                System.out.printf("Despues F-%d I-T-%d = %s.%n", id, izq, izquierdo.getEstado());
                System.out.printf("Despues F-%d D-T-%d = %s.%n", id, der, derecho.getEstado());

                pera(2500);

            } finally {
                System.out.printf("F-%d terminó de comer.%n", id);
                mainFrame.agregarTextTo(String.format("F-%d terminó de comer.%n", id));

                izquierdo.getSemaforo().release();
                System.out.printf("F-%d suelta I-T-%d.%n", id, izq);
                mainFrame.agregarTextTo(String.format("F-%d suelta I-T-%d.%n", id, izq));
                izquierdo.setEstado(EstadoTenedor.SUELTO);
                pintar();

                derecho.getSemaforo().release();
                System.out.printf("F-%d suelta D-T-%d.%n", id, der);
                mainFrame.agregarTextTo(String.format("F-%d suelta D-T-%d.%n", id, der));
                derecho.setEstado(EstadoTenedor.SUELTO);
                pintar();

                comedor.release();
                System.out.printf("F-%d se para.%n", id);
                mainFrame.agregarTextTo(String.format("F-%d se para.%n", id));
                estado = EstadoFilosofo.ESPERANDO_SILLA;
                pintar();

            }
            i++;
        }
        System.out.printf("F-%d comió %d veces de %d.%n", id, i, VECES_A_COMER);
        mainFrame.agregarTextTo(String.format("F-%d comió %d veces de %d.%n", id, i, VECES_A_COMER));
        estado = EstadoFilosofo.SACIADO;
        pintar();
    }

    public void pera(long time) {
        try {
            Thread.sleep(tiempoEspera);
        } catch (InterruptedException ex) {
            Logger.getLogger(Filosofo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public EstadoFilosofo getEstado() {
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
