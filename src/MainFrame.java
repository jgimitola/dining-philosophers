
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
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;
import static javax.swing.text.DefaultCaret.ALWAYS_UPDATE;

/**
 *
 * @author jgimitola, jhanu, anietom
 */
public class MainFrame extends javax.swing.JFrame {

    public int ANCHO_JPANEL;
    public int ALTO_JPANEL;

    public int ANCHO_SPRITE = 91;
    public int ALTO_SPRITE = ANCHO_SPRITE;

    public int NUM_FILOSOFOS = 4;

    public int CENTROX;
    public int CENTROY;

    //Angulo de diferencia entre filosofos
    public double DIFERENCIA;

    public int RADIO_FILOSOFOS = (int) ((NUM_FILOSOFOS * ANCHO_SPRITE) / (2 * Math.PI));

    BufferedImage sentado_comiendo;
    BufferedImage sentado_esperando;
    BufferedImage silla;
    BufferedImage pensando;
    BufferedImage saciado;
    BufferedImage tenedor;

    JTextArea jTextArea1;

    //Modelo
    Tenedor[] tenedores;
    Semaphore comedor;
    Filosofo[] filosofos;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {

        initComponents();

        this.setTitle("La cena de los filósofos");

        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        d.setSize(d.width * 0.85, d.height - 48);
        this.setSize(d);

        ANCHO_JPANEL = (int) (this.getWidth() * 0.6);
        ALTO_JPANEL = this.getHeight() - 48;

        CENTROX = ANCHO_JPANEL / 2;
        CENTROY = ALTO_JPANEL / 2;
        DIFERENCIA = (2 * Math.PI) / (NUM_FILOSOFOS);

        initComponent();

    }

    void initComponent() {
        jTextArea1 = new JTextArea();

        DefaultCaret caret = (DefaultCaret) jTextArea1.getCaret();
        caret.setUpdatePolicy(ALWAYS_UPDATE);
        JScrollPane scroll = new JScrollPane(jTextArea1);
        JScrollPane scroll2 = new JScrollPane(convencionPanel);

        JLabel label_Log = new JLabel("Log: ");

        this.add(jPanelMesa);
        jPanelMesa.setBounds(0, 0, ANCHO_JPANEL, ALTO_JPANEL);
        jPanelMesa.setVisible(true);

        this.add(scroll);
        scroll.setBounds(ANCHO_JPANEL + 5, ALTO_JPANEL / 2, (int) ((this.getWidth() * 0.37)), ALTO_JPANEL / 2);
        scroll.setAutoscrolls(true);
        scroll.setVisible(true);

        this.add(scroll2);
        scroll2.setBounds(ANCHO_JPANEL + 5, 0, (int) ((this.getWidth() * 0.37)), 150);
        scroll2.setAutoscrolls(true);
        scroll2.setVisible(true);

        this.add(label_Log);
        label_Log.setBounds(ANCHO_JPANEL + 5, ALTO_JPANEL / 2 - 24, 32, 24);

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

    /**
     * Inicializa la simulación, esto es, crear semaforos y empezar a ejecutar
     * los hilos.
     */
    public void comenzar() {
        tenedores = new Tenedor[NUM_FILOSOFOS];
        filosofos = new Filosofo[NUM_FILOSOFOS];
        comedor = new Semaphore(NUM_FILOSOFOS - 1);

        IntStream.range(0, NUM_FILOSOFOS)
                .forEach((int i) -> {
                    double angulo = (i * DIFERENCIA);
                    tenedores[i] = new Tenedor(i, new Semaphore(1), angulo, EstadoTenedor.SUELTO, this);
                });

        //Ejecutamos los filosofos
        ExecutorService executor = Executors.newFixedThreadPool(NUM_FILOSOFOS);
        for (int i = 0; i < NUM_FILOSOFOS; i++) {
            double angulo = i * DIFERENCIA;
            int x = (int) (CENTROX + (Math.sin(angulo) * RADIO_FILOSOFOS));
            int y = (int) (CENTROY - (Math.cos(angulo) * RADIO_FILOSOFOS));

            Filosofo f = new Filosofo(i,
                    comedor,
                    tenedores[(i + 1) % NUM_FILOSOFOS],
                    tenedores[i],
                    EstadoFilosofo.ESPERANDO_SILLA,
                    x, y, angulo, this);

            filosofos[i] = f;
            executor.submit(f);
        }
        executor.shutdown();
    }

    public JPanel getJPanelMesa() {
        return jPanelMesa;
    }

    public synchronized void agregarTextTo(String text) {
        jTextArea1.append(text);
    }

    public Graphics2D rotarEje(int x, int y, double angulo, Graphics graphics) {

        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        at.concatenate(AffineTransform.getRotateInstance(angulo));
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.transform(at);
        return g2;
    }

    public synchronized void actualizarJPanelMesa() {
        Image buffer = createImage(jPanelMesa.getWidth(), jPanelMesa.getHeight());

        Graphics2D pantallaVirtual = (Graphics2D) buffer.getGraphics();
        pantallaVirtual.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        pantallaVirtual.setColor(Color.white);

        pantallaVirtual.fillOval(CENTROX - RADIO_FILOSOFOS, CENTROY - RADIO_FILOSOFOS, 2 * RADIO_FILOSOFOS, 2 * RADIO_FILOSOFOS);
        pantallaVirtual.setColor(Color.black);
        for (int i = 0; i < NUM_FILOSOFOS; i++) {
            pintarFilosofo(filosofos[i], pantallaVirtual);
            pintarTenedor(tenedores[i], pantallaVirtual);
        }
        pantallaVirtual.drawLine(ANCHO_JPANEL - 1, 0, ANCHO_JPANEL - 1, ALTO_JPANEL);

        Graphics g = jPanelMesa.getGraphics();
        g.drawImage(buffer, 0, 0, jPanelMesa.getWidth(), jPanelMesa.getHeight(), null);
    }

    public void pintarFilosofo(Filosofo filosofo, Graphics pantallaVirtual) {
        double angulo = filosofo.getAngulo();
        int x = filosofo.getX();
        int y = filosofo.getY();
        BufferedImage sprite;
        switch (filosofo.getEstado()) {
            case COMIENDO:
                sprite = sentado_comiendo;
                break;
            case PENSANDO:
                sprite = pensando;
                break;
            case ESPERANDO_SILLA:
                sprite = silla;
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
        g2.drawString(String.format("Consumido %d/%d", filosofo.getConsumido(), filosofo.getVECES_A_COMER()), -sprite.getWidth() / 2, -sprite.getHeight() - 13);
    }

    public void pintarTenedor(Tenedor tenedor, Graphics pantallaVirtual) {
        double angulo = tenedor.getAngulo();
        int x;
        int y;
        switch (tenedor.getEstado()) {
            case SUELTO:
                angulo = angulo - DIFERENCIA / 2;
                break;
            case TOMADO_DERECHA:
                angulo = angulo - (DIFERENCIA * 0.15);
                break;
            case TOMADO_IZQUIERDA:
                angulo = angulo - (DIFERENCIA * 0.85);
                break;
        }
        x = (int) (CENTROX + (Math.sin(angulo) * RADIO_FILOSOFOS));
        y = (int) (CENTROY - (Math.cos(angulo) * RADIO_FILOSOFOS));
        Graphics2D g2 = rotarEje(x, y, angulo, pantallaVirtual);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(this.tenedor, -this.tenedor.getWidth() / 2, 25, jPanelMesa);
        g2.drawString("T-" + tenedor.getId(), -this.tenedor.getWidth() / 2 + 8, 20);
    }

    @Override
    public void update(Graphics g) {
        super.update(g);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        actualizarJPanelMesa();
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
        convencionPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();

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

        jLabel1.setText("De pie");

        jLabel2.setText("Pensando");

        jLabel3.setText("Comiendo");

        jLabel4.setText("Saciado");

        jLabel5.setText("Esperando");

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgs/silla_vacia.png"))); // NOI18N

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgs/pensando.png"))); // NOI18N

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgs/sentado_comiendo.png"))); // NOI18N

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgs/sentado_esperando.png"))); // NOI18N

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgs/saciado.png"))); // NOI18N

        javax.swing.GroupLayout convencionPanelLayout = new javax.swing.GroupLayout(convencionPanel);
        convencionPanel.setLayout(convencionPanelLayout);
        convencionPanelLayout.setHorizontalGroup(
            convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(convencionPanelLayout.createSequentialGroup()
                .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(convencionPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel6))
                    .addGroup(convencionPanelLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel1)))
                .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(convencionPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel7))
                    .addGroup(convencionPanelLayout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jLabel2)))
                .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(convencionPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel8))
                    .addGroup(convencionPanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel3)))
                .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(convencionPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel9)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel10))
                    .addGroup(convencionPanelLayout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(jLabel5)
                        .addGap(54, 54, 54)
                        .addComponent(jLabel4)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        convencionPanelLayout.setVerticalGroup(
            convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, convencionPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, convencionPanelLayout.createSequentialGroup()
                        .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2)
                                .addComponent(jLabel3)))
                        .addGap(3, 3, 3))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, convencionPanelLayout.createSequentialGroup()
                        .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel8)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4))))
                .addContainerGap())
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
    private javax.swing.JPanel convencionPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanelMesa;
    // End of variables declaration//GEN-END:variables
}
