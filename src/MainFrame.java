
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

        
        this.add(label_Log);
        label_Log.setBounds(ANCHO_JPANEL + 5, ALTO_JPANEL / 2 - 24, 32, 24);

        this.add(scroll2);
        scroll2.setBounds(ANCHO_JPANEL + 5, 0, (int) ((this.getWidth() * 0.37)), label_Log.getY());
        scroll2.setAutoscrolls(true);
        scroll2.setVisible(true);

        

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
        btnIniciar1.setEnabled(false);
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
        boolean completed = true;
        for (int i = 0; i < NUM_FILOSOFOS; i++) {
            pintarFilosofo(filosofos[i], pantallaVirtual);
            pintarTenedor(tenedores[i], pantallaVirtual);
            if (filosofos[i].getEstado() != EstadoFilosofo.SACIADO) {
                completed = false;
            }
        }
        if (completed) {
            btnIniciar1.setEnabled(true);
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
        btnIniciar1 = new javax.swing.JButton();
        txtN1 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        jLabel14 = new javax.swing.JLabel();
        jPanelControles = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jLabel12 = new javax.swing.JLabel();
        txtN = new javax.swing.JTextField();
        btnIniciar = new javax.swing.JButton();

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

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("De pie");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Pensando");

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Comiendo");

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Saciado");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Esperando");

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgs/silla_vacia.png"))); // NOI18N

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgs/pensando.png"))); // NOI18N

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgs/sentado_comiendo.png"))); // NOI18N

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgs/sentado_esperando.png"))); // NOI18N

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imgs/saciado.png"))); // NOI18N

        btnIniciar1.setText("INICIAR NUEVA SIMULACIÓN");
        btnIniciar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciar1ActionPerformed(evt);
            }
        });

        jLabel13.setText("Número de filósofos (n):");

        jTextArea3.setEditable(false);
        jTextArea3.setColumns(20);
        jTextArea3.setLineWrap(true);
        jTextArea3.setRows(5);
        jTextArea3.setText("Problema de la cena de los filósofos comensales restringiendo un máximo de n-1 filósifos sentados al tiempo en la mesa.");
        jTextArea3.setWrapStyleWord(true);
        jScrollPane2.setViewportView(jTextArea3);

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel14.setText("CENA DE LOS FILÓSOFOS COMENSALES");

        javax.swing.GroupLayout convencionPanelLayout = new javax.swing.GroupLayout(convencionPanel);
        convencionPanel.setLayout(convencionPanelLayout);
        convencionPanelLayout.setHorizontalGroup(
            convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(convencionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(convencionPanelLayout.createSequentialGroup()
                        .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(12, 12, 12)
                        .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(convencionPanelLayout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel10))
                            .addGroup(convencionPanelLayout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(convencionPanelLayout.createSequentialGroup()
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtN1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnIniciar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );
        convencionPanelLayout.setVerticalGroup(
            convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, convencionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtN1)
                    .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnIniciar1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(convencionPanelLayout.createSequentialGroup()
                        .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(convencionPanelLayout.createSequentialGroup()
                                .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, convencionPanelLayout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(convencionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2)
                                .addComponent(jLabel5)
                                .addComponent(jLabel4)
                                .addComponent(jLabel3))))
                    .addComponent(jLabel9)
                    .addComponent(jLabel8))
                .addGap(14, 14, 14))
        );

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel11.setText("CENA DE LOS FILÓSOFOS COMENSALES");

        jTextArea2.setEditable(false);
        jTextArea2.setColumns(20);
        jTextArea2.setLineWrap(true);
        jTextArea2.setRows(5);
        jTextArea2.setText("Problema de la cena de los filósofos comensales restringiendo un máximo de n-1 filósifos sentados al tiempo en la mesa.");
        jTextArea2.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextArea2);

        jLabel12.setText("Número de filósofos (n):");

        btnIniciar.setText("INICIAR NUEVA SIMULACIÓN");

        javax.swing.GroupLayout jPanelControlesLayout = new javax.swing.GroupLayout(jPanelControles);
        jPanelControles.setLayout(jPanelControlesLayout);
        jPanelControlesLayout.setHorizontalGroup(
            jPanelControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanelControlesLayout.createSequentialGroup()
                .addGroup(jPanelControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelControlesLayout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtN, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnIniciar, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        jPanelControlesLayout.setVerticalGroup(
            jPanelControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelControlesLayout.createSequentialGroup()
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelControlesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtN, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnIniciar))
                .addContainerGap(13, Short.MAX_VALUE))
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

    private void btnIniciar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIniciar1ActionPerformed
        
    }//GEN-LAST:event_btnIniciar1ActionPerformed

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
    private javax.swing.JButton btnIniciar;
	private javax.swing.JButton btnIniciar1;
    private javax.swing.JPanel convencionPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanelControles;
    private javax.swing.JPanel jPanelMesa;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextField txtN;
    private javax.swing.JTextField txtN1;
    // End of variables declaration//GEN-END:variables
}
