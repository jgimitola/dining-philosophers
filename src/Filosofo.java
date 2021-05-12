
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

    private final int id;
    private final Semaphore comedor;
    private final Tenedor[] tenedores;
    private final int VECES_A_COMER;
    private EstadoFilosofo estado;
    private static int TIEMPO_ACTIVIDAD = 5000;
    private RotateLabel sprite;

    //Grafico
    private int x;
    private int y;
    private double angulo;
    public MainFrame mainFrame;
    private int izq;
    private int der;
    private int tiempoEspera = 20000;

    public Filosofo(int id, Semaphore comedor, Tenedor[] tenedores) {
        this.id = id;
        this.comedor = comedor;
        this.tenedores = tenedores;
        this.VECES_A_COMER = 1 + (int) (Math.random() * ((2 - 1) + 1));

    }

    public Filosofo(int id, Semaphore comedor, Tenedor[] tenedores, EstadoFilosofo estado, int x, int y, double angulo, MainFrame mainFrame) {
        this.id = id;
        this.comedor = comedor;
        this.tenedores = tenedores;
        this.VECES_A_COMER = 1 + (int) (Math.random() * ((2 - 1) + 1));
        this.estado = estado;
        this.x = x;
        this.y = y;
        this.angulo = angulo;
        this.mainFrame = mainFrame;

        sprite = new RotateLabel("", angulo, this);
        /*
        mainFrame.getJPanelMesa().add(sprite);
        sprite.setBounds(x, y, 91, 91);
         */
        izq = (id + 1) % Restaurante.NUM_FILOSOFOS;
        der = id;

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
        pera(2500);
    }

    public void run() {
        int i = 0;
        while (i < VECES_A_COMER) {
            try {
                System.out.printf("F-%d está esperando sentarse.%n", id);
                mainFrame.agregarTextTo(String.format("F-%d está esperando sentarse.%n", id));
                estado = EstadoFilosofo.ESPERANDO_SILLA;
                pintar();

                comedor.acquire();

                System.out.printf("F-%d está pensando.%n", id);
                mainFrame.agregarTextTo(String.format("F-%d está pensando.%n", id));
                estado = EstadoFilosofo.PENSANDO;
                pintar();

                System.out.printf("F-%d quiere tomar D-T-%d.%n", id, der);
                mainFrame.agregarTextTo(String.format("F-%d quiere tomar D-T-%d.%n", id, der));
                tenedores[der].getSemaforo().acquire();
                System.out.printf("F-%d tomó D-T-%d.%n", id, der);
                mainFrame.agregarTextTo(String.format("F-%d tomó D-T-%d.%n", id, der));
                tenedores[der].setEstado(EstadoTenedor.TOMADO_DERECHA);

                System.out.printf("F-%d quiere tomar I-T-%d.%n", id, izq);
                mainFrame.agregarTextTo(String.format("F-%d quiere tomar I-T-%d.%n", id, izq));
                tenedores[izq].getSemaforo().acquire();
                System.out.printf("F-%d tomó I-T-%d.%n", id, izq);
                mainFrame.agregarTextTo(String.format("F-%d tomó I-T-%d.%n", id, izq));
                tenedores[izq].setEstado(EstadoTenedor.TOMADO_IZQUIERDA);
                pintar();

            } catch (InterruptedException ex) {
                System.out.println("Exception: " + ex);
            }

            try {
                System.out.printf("F-%d está comiendo.%n", id);
                mainFrame.agregarTextTo(String.format("F-%d está comiendo.%n", id));
                estado = EstadoFilosofo.COMIENDO;
                pintar();

            } finally {
                System.out.printf("F-%d terminó de comer.%n", id);
                mainFrame.agregarTextTo(String.format("F-%d terminó de comer.%n", id));

                tenedores[izq].getSemaforo().release();
                System.out.printf("F-%d suelta I-T-%d.%n", id, izq);
                mainFrame.agregarTextTo(String.format("F-%d suelta I-T-%d.%n", id, izq));
                tenedores[izq].setEstado(EstadoTenedor.SUELTO);
                pintar();

                tenedores[der].getSemaforo().release();
                System.out.printf("F-%d suelta D-T-%d.%n", id, der);
                mainFrame.agregarTextTo(String.format("F-%d suelta D-T-%d.%n", id, der));
                tenedores[der].setEstado(EstadoTenedor.SUELTO);
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

    public void pintarFilosofo(Filosofo filosofo, Graphics pantallaVirtual) {
        BufferedImage sprite;
        switch (filosofo.getEstado()) {
            case COMIENDO:
                sprite = mainFrame.sentado_comiendo;
                break;
            case PENSANDO:
                sprite = mainFrame.pensando;
                break;
            case ESPERANDO_SILLA:
                //De pie
                sprite = mainFrame.sentado_esperando;
                break;
            case ESPERANDO_TENEDOR:
                sprite = mainFrame.sentado_esperando;
                break;
            case SACIADO:
                sprite = mainFrame.saciado;
                break;
            default:
                throw new AssertionError();
        }
        Graphics2D g2 = mainFrame.rotarEje(0, 0, angulo, pantallaVirtual);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(sprite, 0, 0, null);
        g2.drawString("F-" + filosofo.getId(), -sprite.getWidth() / 2 + 32, -sprite.getHeight());
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
