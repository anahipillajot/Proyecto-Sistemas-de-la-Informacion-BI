import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginVentana extends JFrame {
    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JButton btnIngresar;

    // ============================================================
    // PALETA DE DISEÑO — coherente con DashboardBI (Dark Mode Premium)
    // ============================================================
    private final Color COLOR_FONDO_IZQ    = new Color(11, 14, 26);
    private final Color COLOR_FONDO_DER    = new Color(17, 20, 36);
    private final Color COLOR_CARD_BG      = new Color(24, 28, 48);
    private final Color COLOR_CARD_BORDE   = new Color(40, 46, 74);
    private final Color COLOR_INPUT_BG     = new Color(30, 35, 58);
    private final Color COLOR_TEXTO_BLANCO = new Color(248, 249, 250);
    private final Color COLOR_TEXTO_MUTED  = new Color(148, 163, 184);
    private final Color COLOR_ACCENT       = new Color(0, 224, 255);
    private final Color COLOR_ACCENT_2     = new Color(121, 40, 202);

    public LoginVentana() {
        setTitle("Portal de Analítica Avanzada — Gen-Z Social Media BI");
        setSize(940, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panelPrincipal = new JPanel(new GridLayout(1, 2));
        panelPrincipal.setBackground(COLOR_FONDO_DER);

        panelPrincipal.add(construirPanelIzquierdo());
        panelPrincipal.add(construirPanelDerecho());

        add(panelPrincipal);
    }

    // ================================================================
    // COLUMNA IZQUIERDA — Branding con degradado y formas decorativas
    // ================================================================
    private JPanel construirPanelIzquierdo() {
        JPanel panelIzquierdo = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fondo degradado diagonal
                g2.setPaint(new GradientPaint(0, 0, new Color(16, 20, 38), getWidth(), getHeight(), new Color(28, 18, 48)));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Círculos decorativos translúcidos (glow)
                g2.setColor(new Color(COLOR_ACCENT.getRed(), COLOR_ACCENT.getGreen(), COLOR_ACCENT.getBlue(), 30));
                g2.fill(new Ellipse2D.Double(getWidth() - 160, -80, 260, 260));

                g2.setColor(new Color(COLOR_ACCENT_2.getRed(), COLOR_ACCENT_2.getGreen(), COLOR_ACCENT_2.getBlue(), 45));
                g2.fill(new Ellipse2D.Double(-100, getHeight() - 180, 300, 300));

                g2.setColor(new Color(255, 45, 141, 20));
                g2.fill(new Ellipse2D.Double(getWidth() - 260, getHeight() - 120, 200, 200));

                g2.dispose();
            }
        };
        panelIzquierdo.setPreferredSize(new Dimension(420, 560));
        panelIzquierdo.setBackground(COLOR_FONDO_IZQ);

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBounds(0, 0, 420, 560);
        contenido.setBorder(new EmptyBorder(70, 44, 50, 44));

        // Insignia / logo
        JPanel badge = new JPanel(new GridBagLayout());
        badge.setOpaque(false);
        badge.setMaximumSize(new Dimension(52, 52));
        badge.setPreferredSize(new Dimension(52, 52));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel insignia = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, COLOR_ACCENT, getWidth(), getHeight(), COLOR_ACCENT_2));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                String s = "GZ";
                g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, (getHeight() + fm.getAscent()) / 2 - 4);
                g2.dispose();
            }
        };
        insignia.setOpaque(false);
        insignia.setPreferredSize(new Dimension(52, 52));
        badge.add(insignia);

        JLabel lblTituloBI = new JLabel("Gen-Z Análisis");
        lblTituloBI.setFont(new Font("Segoe UI", Font.BOLD, 27));
        lblTituloBI.setForeground(COLOR_TEXTO_BLANCO);
        lblTituloBI.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTituloBI.setBorder(new EmptyBorder(22, 0, 2, 0));

        JLabel lblSubtituloBI = new JLabel("Business Intelligence System");
        lblSubtituloBI.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtituloBI.setForeground(COLOR_TEXTO_MUTED);
        lblSubtituloBI.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea txtDescripcion = new JTextArea("Plataforma de procesamiento masivo orientada a la evaluación del impacto psicológico y patrones de conducta digital en la Generación Z.");
        txtDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDescripcion.setForeground(new Color(190, 198, 216));
        txtDescripcion.setEditable(false);
        txtDescripcion.setFocusable(false);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setOpaque(false);
        txtDescripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDescripcion.setMaximumSize(new Dimension(320, 90));
        txtDescripcion.setBorder(new EmptyBorder(24, 0, 0, 0));

        JPanel chipDataset = construirChip("Dataset: 1,000,000 usuarios");
        chipDataset.setAlignmentX(Component.LEFT_ALIGNMENT);

        contenido.add(badge);
        contenido.add(lblTituloBI);
        contenido.add(lblSubtituloBI);
        contenido.add(txtDescripcion);
        contenido.add(Box.createVerticalStrut(26));
        contenido.add(construirListaCaracteristicas());
        contenido.add(Box.createVerticalGlue());
        contenido.add(chipDataset);

        panelIzquierdo.add(contenido);
        return panelIzquierdo;
    }

    private JPanel construirListaCaracteristicas() {
        JPanel lista = new JPanel();
        lista.setOpaque(false);
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] items = {
            "Analítica predictiva de riesgo",
            "Dashboards ejecutivos en vivo",
            "Segmentación por plataforma y edad"
        };

        for (String item : items) {
            JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
            fila.setOpaque(false);
            fila.setAlignmentX(Component.LEFT_ALIGNMENT);

            MiniIcono iconoCheck = new MiniIcono("check", COLOR_ACCENT, 16);

            JLabel lblTexto = new JLabel(item);
            lblTexto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblTexto.setForeground(new Color(206, 212, 230));

            fila.add(iconoCheck);
            fila.add(lblTexto);
            lista.add(fila);
        }
        return lista;
    }

    private JPanel construirChip(String texto) {
        JPanel chip = new RoundedPanel(20, new Color(255, 255, 255, 14), new Color(255, 255, 255, 30));
        chip.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 6));
        chip.setBorder(new EmptyBorder(6, 14, 6, 16));
        chip.setMaximumSize(new Dimension(260, 34));

        MiniIcono iconoChart = new MiniIcono("chart", COLOR_ACCENT, 14);

        JLabel lblTexto = new JLabel(texto);
        lblTexto.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTexto.setForeground(COLOR_TEXTO_BLANCO);

        chip.add(iconoChart);
        chip.add(lblTexto);
        return chip;
    }

    // ================================================================
    // Icono vectorial reutilizable: no depende de fuentes de emoji,
    // se dibuja directamente con Graphics2D para verse igual en cualquier equipo.
    // ================================================================
    private static class MiniIcono extends JComponent {
        private final String tipo;
        private final Color color;

        MiniIcono(String tipo, Color color, int tamano) {
            this.tipo = tipo;
            this.color = color;
            Dimension d = new Dimension(tamano, tamano);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(color);

            switch (tipo) {
                case "check": {
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D.Double palomita = new Path2D.Double();
                    palomita.moveTo(w * 0.12, h * 0.55);
                    palomita.lineTo(w * 0.40, h * 0.80);
                    palomita.lineTo(w * 0.88, h * 0.20);
                    g2.draw(palomita);
                    break;
                }
                case "chart": {
                    int anchoBarra = Math.max(2, w / 5);
                    g2.fillRoundRect((int) (w * 0.05), (int) (h * 0.50), anchoBarra, (int) (h * 0.45), 1, 1);
                    g2.fillRoundRect((int) (w * 0.38), (int) (h * 0.25), anchoBarra, (int) (h * 0.70), 1, 1);
                    g2.fillRoundRect((int) (w * 0.71), (int) (h * 0.05), anchoBarra, (int) (h * 0.90), 1, 1);
                    break;
                }
                default:
                    break;
            }
            g2.dispose();
        }
    }

    // ================================================================
    // COLUMNA DERECHA — Tarjeta de formulario de acceso
    // ================================================================
    private JPanel construirPanelDerecho() {
        JPanel panelDerecho = new JPanel(new GridBagLayout());
        panelDerecho.setBackground(COLOR_FONDO_DER);

        RoundedPanel tarjeta = new RoundedPanel(20, COLOR_CARD_BG, COLOR_CARD_BORDE);
        tarjeta.setLayout(new GridBagLayout());
        tarjeta.setPreferredSize(new Dimension(360, 420));
        tarjeta.setBorder(new EmptyBorder(38, 38, 38, 38));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        JLabel lblLogin = new JLabel("Bienvenido de nuevo");
        lblLogin.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogin.setForeground(COLOR_TEXTO_BLANCO);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        tarjeta.add(lblLogin, gbc);

        JLabel lblAyuda = new JLabel("Ingresa tus credenciales para continuar");
        lblAyuda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAyuda.setForeground(COLOR_TEXTO_MUTED);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 26, 0);
        tarjeta.add(lblAyuda, gbc);

        JLabel lblUser = new JLabel("NOMBRE DE USUARIO");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblUser.setForeground(COLOR_TEXTO_MUTED);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 2, 6, 0);
        tarjeta.add(lblUser, gbc);

        txtUsuario = new JTextField();
        estilizarCampoTexto(txtUsuario);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 18, 0);
        tarjeta.add(txtUsuario, gbc);

        JLabel lblPass = new JLabel("CONTRASEÑA DE ACCESO");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPass.setForeground(COLOR_TEXTO_MUTED);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 2, 6, 0);
        tarjeta.add(lblPass, gbc);

        txtContrasena = new JPasswordField();
        estilizarCampoTexto(txtContrasena);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 30, 0);
        tarjeta.add(txtContrasena, gbc);

        btnIngresar = construirBotonIngresar();
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        tarjeta.add(btnIngresar, gbc);

        JLabel lblSeguridad = new JLabel("Conexión protegida · Sistema de Seguridad BI");
        lblSeguridad.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSeguridad.setForeground(COLOR_TEXTO_MUTED);
        lblSeguridad.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 7;
        gbc.insets = new Insets(22, 0, 0, 0);
        tarjeta.add(lblSeguridad, gbc);

        panelDerecho.add(tarjeta);

        btnIngresar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validarAcceso();
            }
        });

        // Enter en cualquiera de los dos campos también valida el acceso
        ActionListener enterListener = e -> validarAcceso();
        txtUsuario.addActionListener(enterListener);
        txtContrasena.addActionListener(enterListener);

        return panelDerecho;
    }

    private void estilizarCampoTexto(JTextField campo) {
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setPreferredSize(new Dimension(260, 42));
        campo.setBackground(COLOR_INPUT_BG);
        campo.setForeground(COLOR_TEXTO_BLANCO);
        campo.setCaretColor(COLOR_TEXTO_BLANCO);
        campo.setSelectionColor(COLOR_ACCENT);
        campo.setOpaque(true);

        RoundedLineBorder bordeNormal = new RoundedLineBorder(COLOR_CARD_BORDE, 10, 1.2f);
        RoundedLineBorder bordeFoco = new RoundedLineBorder(COLOR_ACCENT, 10, 1.6f);

        campo.setBorder(BorderFactory.createCompoundBorder(bordeNormal, new EmptyBorder(4, 12, 4, 12)));

        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                campo.setBorder(BorderFactory.createCompoundBorder(bordeFoco, new EmptyBorder(4, 12, 4, 12)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                campo.setBorder(BorderFactory.createCompoundBorder(bordeNormal, new EmptyBorder(4, 12, 4, 12)));
            }
        });
    }

    private JButton construirBotonIngresar() {
        JButton btn = new JButton("Acceder al Dashboard") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color inicio = COLOR_ACCENT;
                Color fin = COLOR_ACCENT_2;
                if (getModel().isRollover()) {
                    inicio = inicio.brighter();
                    fin = fin.brighter();
                }
                if (getModel().isPressed()) {
                    inicio = inicio.darker();
                    fin = fin.darker();
                }

                g2.setPaint(new GradientPaint(0, 0, inicio, getWidth(), getHeight(), fin));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));

                g2.setColor(new Color(13, 17, 30));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent()) / 2 - 3;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(260, 44));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ================================================================
    // Panel redondeado reutilizable
    // ================================================================
    private static class RoundedPanel extends JPanel {
        private final int radio;
        private final Color colorFondo;
        private final Color colorBorde;

        RoundedPanel(int radio, Color colorFondo, Color colorBorde) {
            this.radio = radio;
            this.colorFondo = colorFondo;
            this.colorBorde = colorBorde;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(colorFondo);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radio, radio));
            g2.setColor(colorBorde);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, radio, radio));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================================================================
    // Borde redondeado reutilizable para campos de texto
    // ================================================================
    private static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int radio;
        private final float grosor;

        RoundedLineBorder(Color color, int radio, float grosor) {
            this.color = color;
            this.radio = radio;
            this.grosor = grosor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(grosor));
            g2.draw(new RoundRectangle2D.Double(x + 0.6, y + 0.6, width - 1.2, height - 1.2, radio, radio));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(6, 8, 6, 8);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(6, 8, 6, 8);
            return insets;
        }
    }

    // ================================================================
    // LÓGICA DE AUTENTICACIÓN (intacta)
    // ================================================================
    private void validarAcceso() {
        String usuario = txtUsuario.getText();
        String contrasena = new String(txtContrasena.getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor complete todos los campos.", "Campos Vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String query = "SELECT ID_Rol FROM Usuarios WHERE Username = ? AND PasswordHash = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setString(1, usuario);
            pst.setString(2, contrasena);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int idRol = rs.getInt("ID_Rol");
                    String nombreRol = (idRol == 1) ? "Administrador" : "Analista";

                    JOptionPane.showMessageDialog(this, "¡Acceso verificado con éxito!\nRol asignado: " + nombreRol, "Sistema de Seguridad BI", JOptionPane.INFORMATION_MESSAGE);

                    this.dispose();
                    new DashboardBI().setVisible(true);
                    // Próximo paso: Abrir ventana de BI pasando el Rol del usuario

                } else {
                    JOptionPane.showMessageDialog(this, "Credenciales incorrectas o usuario no registrado.", "Fallo de Autenticación", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de comunicación con el clúster de datos SQL Server:\n" + ex.getMessage(), "Error de Infraestructura", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginVentana().setVisible(true);
            }
        });
    }
}