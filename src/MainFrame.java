
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 *
 * @author jgimitola, jhanu, anietom
 */
public class MainFrame extends javax.swing.JFrame {

    private int ANCHO_JPANEL = 700;
    private int ALTO_JPANEL = 700;

    private int ANCHO_SPRITE = 91;
    private int ALTO_SPRITE = ANCHO_SPRITE;

    private int NUM_FILOSOFOS = 10;

    private int CENTROX;
    private int CENTROY;

    //Angulo de diferencia entre filosofos
    private double DIFERENCIA;

    private int RADIO_FILOSOFOS = (int) ((NUM_FILOSOFOS * ANCHO_SPRITE) / (2 * Math.PI));

    BufferedImage sentado_comiendo;
    BufferedImage sentado_esperando;
    BufferedImage silla;
    BufferedImage pensando;
    BufferedImage saciado;
    BufferedImage tenedor;

    //Modelo
    Tenedor[] tenedores;
    Semaphore comedor;
    Filosofo[] filosofos;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {

        initComponents();

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        d.setSize(d.width, d.height - 48);
        this.setSize(d);

        ANCHO_JPANEL = (int) (this.getWidth() * 0.6);
        ALTO_JPANEL = this.getHeight();

        CENTROX = ANCHO_JPANEL / 2;
        CENTROY = ALTO_JPANEL / 2;
        DIFERENCIA = (2 * Math.PI) / (NUM_FILOSOFOS);
        initComponent();

    }

    void initComponent() {
        this.add(jPanelMesa);
        jPanelMesa.setBounds(0, 0, ANCHO_JPANEL, ALTO_JPANEL);
        jPanelMesa.setVisible(true);

        try {
            this.sentado_comiendo = ImageIO.read(new File("imagenes/sentado_comiendo.png"));
            this.sentado_esperando = ImageIO.read(new File("imagenes/sentado_esperando.png"));
            this.pensando = ImageIO.read(new File("imagenes/pensando.png"));
            this.silla = ImageIO.read(new File("imagenes/silla_vacia.png"));
            this.saciado = ImageIO.read(new File("imagenes/saciado.png"));
            this.tenedor = ImageIO.read(new File("imagenes/fork.png"));
            comenzar();
        } catch (Exception e) {
            System.out.println("Error cargando imagenes: " + e);
        }

    }

    public Graphics2D rotarEje(int x, int y, double angulo, Graphics graphics) {
        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        at.concatenate(AffineTransform.getRotateInstance(angulo));
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.transform(at);
        return g2;
    }

    public void actualizarJPanelMesa() {
        Image buffer = createImage(jPanelMesa.getWidth(), jPanelMesa.getHeight());
        Graphics2D pantallaVirtual = (Graphics2D) buffer.getGraphics();
        Graphics g = jPanelMesa.getGraphics();
        for (int i = 0; i < NUM_FILOSOFOS; i++) {
            pintarFilosofo(filosofos[i], pantallaVirtual);
        }
        for (int i = 0; i < tenedores.length; i++) {
            pintarTenedor(tenedores[i], pantallaVirtual);
        }
        g.drawImage(buffer, 0, 0, jPanelMesa.getWidth(), jPanelMesa.getHeight(), null);
    }

    public void comenzar() {
        tenedores = new Tenedor[NUM_FILOSOFOS];
        filosofos = new Filosofo[NUM_FILOSOFOS];
        comedor = new Semaphore(NUM_FILOSOFOS - 1);

        IntStream.range(0, NUM_FILOSOFOS)
                .forEach((int i) -> {
                    double angulo = (i * DIFERENCIA) ;
                    int x = (int) (CENTROX + (Math.sin(angulo- DIFERENCIA / 2) * RADIO_FILOSOFOS));
                    int y = (int) (CENTROY - (Math.cos(angulo- DIFERENCIA / 2) * RADIO_FILOSOFOS));
                    tenedores[i] = new Tenedor(i, new Semaphore(1), x, y, angulo);
                });

        //Ejecutamos los filosofos
        ExecutorService executor = Executors.newCachedThreadPool();
        System.out.println("width: " + jPanelMesa.getWidth() + " hei: " + jPanelMesa.getHeight());
        Image buffer = createImage(jPanelMesa.getWidth(), jPanelMesa.getHeight());
        Graphics2D pantallaVirtual = (Graphics2D) buffer.getGraphics();
        Graphics g = jPanelMesa.getGraphics();
        for (int i = 0; i < NUM_FILOSOFOS; i++) {

            double angulo = i * DIFERENCIA;
            int x = (int) (CENTROX + (Math.sin(angulo) * RADIO_FILOSOFOS));
            int y = (int) (CENTROY - (Math.cos(angulo) * RADIO_FILOSOFOS));
            Filosofo f = new Filosofo(i, comedor, tenedores, Estado.ESPERANDO_SILLA, x, y, angulo, this);
            filosofos[i] = f;
            executor.submit(f);
        }
        g.drawImage(buffer, 0, 0, jPanelMesa.getWidth(), jPanelMesa.getHeight(), null);
        executor.shutdown();

    }

    public void pintarFilosofo(Filosofo filosofo, Graphics pantallaVirtual) {
        double angulo = filosofo.getAngulo();
        int x = (int) (CENTROX + (Math.sin(angulo) * RADIO_FILOSOFOS));
        int y = (int) (CENTROY - (Math.cos(angulo) * RADIO_FILOSOFOS));
        BufferedImage sprite;
        switch (filosofo.getEstado()) {
            case COMIENDO:
                sprite = sentado_comiendo;
                break;
            case PENSANDO:
                sprite = pensando;
                break;
            case ESPERANDO_SILLA:
                //De pie
                sprite = sentado_esperando;
                break;
            case ESPERANDO_TENEDOR:
                sprite = sentado_esperando;
                break;
            case SACIADO:
                sprite = saciado;
                break;
            default:
                throw new AssertionError();
        }
        Graphics2D g2 = rotarEje(x, y, angulo, pantallaVirtual);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(sprite, -sprite.getWidth() / 2, -sprite.getHeight(), jPanelMesa);
        g2.drawString("F-" + filosofo.getId(), -sprite.getWidth() / 2 + 32, -sprite.getHeight());
    }

    public void pintarTenedor(Tenedor tenedor, Graphics pantallaVirtual) {
        double angulo = tenedor.getAngulo();

        int x = (int) (CENTROX + (Math.sin(angulo - DIFERENCIA / 2) * RADIO_FILOSOFOS));
        int y = (int) (CENTROY - (Math.cos(angulo - DIFERENCIA / 2) * RADIO_FILOSOFOS));
        Graphics2D g2 = rotarEje(x, y, angulo - DIFERENCIA / 2, pantallaVirtual);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(this.tenedor, -this.tenedor.getWidth() / 2, 20, jPanelMesa);
        g2.drawString("T-" + tenedor.getId(), -this.tenedor.getWidth() / 2 + 6, 20);

    }

    public void pintarJpanel(JPanel jpanel, int width, int height) {
        if (width > 0 && height > 0) {
            Graphics g = jpanel.getGraphics();

            Image buffer = createImage(width, height);
            Graphics2D pantallaVirtual = (Graphics2D) buffer.getGraphics();
            pantallaVirtual.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            pantallaVirtual.setColor(Color.BLACK);
            try {
                double diferencia = (2 * Math.PI) / (NUM_FILOSOFOS);

                int numF = 0;
                for (double i = 0; i < 2 * Math.PI; i = i + diferencia) {
                    int x = (int) (CENTROX + (Math.sin(i) * RADIO_FILOSOFOS));
                    int y = (int) (CENTROY - (Math.cos(i) * RADIO_FILOSOFOS));

                    Graphics2D g2 = rotarEje(x, y, i, pantallaVirtual);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    BufferedImage sprite = saciado;

                    g2.drawImage(sprite, -sprite.getWidth() / 2, -sprite.getHeight(), jpanel);
                    pantallaVirtual.fillOval(x - 5, y - 5, 10, 10); // Puntos
                    //Texto identificador
                    g2.drawString("F-" + numF, -sprite.getWidth() / 2 + 32, -sprite.getHeight());

                    // Dibujar tenedores (se rotan para que queden entre los filosofos)
                    x = (int) (CENTROX + (Math.sin(i - diferencia / 2) * RADIO_FILOSOFOS));
                    y = (int) (CENTROY - (Math.cos(i - diferencia / 2) * RADIO_FILOSOFOS));
                    g2 = rotarEje(x, y, i - diferencia / 2, pantallaVirtual);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.drawImage(tenedor, -tenedor.getWidth() / 2, 20, jpanel);
                    g2.drawString("T-" + numF, -tenedor.getWidth() / 2 + 6, 20);
                    numF = numF + 1;
                }
                pantallaVirtual.drawOval(CENTROX - RADIO_FILOSOFOS, CENTROY - RADIO_FILOSOFOS, RADIO_FILOSOFOS * 2, RADIO_FILOSOFOS * 2);
            } catch (Exception e) {
                System.out.println("Error pintando escena: " + e);
            }
            pantallaVirtual.drawLine(width - 1, 0, width - 1, height);
            g.drawImage(buffer, 0, 0, width, height, null);
        }
    }

    @Override
    public void update(Graphics g) {
        super.update(g);
        actualizarJPanelMesa();
        //pintarJpanel(jPanelMesa, jPanelMesa.getWidth(), jPanelMesa.getHeight());
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        actualizarJPanelMesa();

        // pintarJpanel(jPanelMesa, jPanelMesa.getWidth(), jPanelMesa.getHeight());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelMesa = new javax.swing.JPanel();

        javax.swing.GroupLayout jPanelMesaLayout = new javax.swing.GroupLayout(jPanelMesa);
        jPanelMesa.setLayout(jPanelMesaLayout);
        jPanelMesaLayout.setHorizontalGroup(
            jPanelMesaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 184, Short.MAX_VALUE)
        );
        jPanelMesaLayout.setVerticalGroup(
            jPanelMesaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 117, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 955, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 651, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanelMesa;
    // End of variables declaration//GEN-END:variables
}
