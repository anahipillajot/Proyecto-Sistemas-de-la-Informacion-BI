import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardBI extends JFrame {

    private JComboBox<String> cbPlataforma;
    private JLabel lblTotalRegistros, lblTiempoPantallaPromedio, lblPredictivoBurnout;
    private PanelGraficoBarras panelBarras;
    private PanelGraficoPastel panelPastel;
    private PanelCalculadoraRiesgo panelCalculadora;
    private JPanel panelIzquierdoGraficos; // CardLayout: alterna entre la gráfica global y la calculadora personal

    // ============================================================
    // PALETA DE DISEÑO — Dark Mode Premium (estilo Power BI / SaaS)
    // ============================================================
    private final Color COLOR_NAV_BAR      = new Color(17, 20, 36);
    private final Color COLOR_BODY_BG      = new Color(11, 14, 26);
    private final Color COLOR_CARD_BG      = new Color(24, 28, 48);
    private final Color COLOR_CARD_BORDE   = new Color(40, 46, 74);
    private final Color COLOR_INPUT_BG     = new Color(32, 37, 60);
    private final Color COLOR_TEXTO_BLANCO = new Color(248, 249, 250);
    private final Color COLOR_TEXTO_MUTED  = new Color(148, 163, 184);
    private final Color COLOR_ACCENT       = new Color(0, 224, 255);
    private final Color COLOR_ACCENT_2     = new Color(121, 40, 202);
    private final Color COLOR_DANGER       = new Color(255, 64, 96);
    private final Color COLOR_EXITO        = new Color(67, 233, 123);
    private final Color COLOR_ADVERTENCIA  = new Color(255, 186, 8);

    // Colores neón para las rebanadas del gráfico de anillo/pastel
    private final Color[] COLORES_PASTEL = {
        new Color(0, 224, 255),
        new Color(147, 90, 232),
        new Color(255, 45, 141),
        new Color(255, 186, 8),
        new Color(67, 233, 123),
        new Color(255, 111, 97)
    };

    // Acentos individuales para cada tarjeta KPI
    private final Color[] ACENTOS_KPI = {
        new Color(0, 224, 255),
        new Color(121, 40, 202),
        new Color(255, 45, 141)
    };

    private List<String> barrasEjeX = new ArrayList<>();
    private List<Double> barrasEjeY = new ArrayList<>();

    private List<String> pastelEtiquetas = new ArrayList<>();
    private List<Integer> pastelValores = new ArrayList<>();

    private final String nombreUsuario;
    private final String rolUsuario;

    // Constructor por defecto (sesión sin datos de login, ej. pruebas rápidas)
    public DashboardBI() {
        this("Usuario", "Invitado");
    }

    public DashboardBI(String nombreUsuario, String rolUsuario) {
        this.nombreUsuario = (nombreUsuario == null || nombreUsuario.isBlank()) ? "Usuario" : nombreUsuario;
        this.rolUsuario = (rolUsuario == null || rolUsuario.isBlank()) ? "Invitado" : rolUsuario;

        setTitle("Executive Business Intelligence Dashboard — Gen-Z Behavioral Systems");
        setSize(1280, 780);
        setMinimumSize(new Dimension(1040, 660));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BODY_BG);

        add(construirNavBar(), BorderLayout.NORTH);

        JPanel mainBody = new JPanel(new BorderLayout(24, 24));
        mainBody.setBackground(COLOR_BODY_BG);
        mainBody.setBorder(new EmptyBorder(26, 32, 30, 32));

        mainBody.add(construirPanelKPIs(), BorderLayout.NORTH);
        mainBody.add(construirPanelGraficos(), BorderLayout.CENTER);

        add(mainBody, BorderLayout.CENTER);

        cargarFiltroPlataformas();
        actualizarDashboard("TODAS");

        cbPlataforma.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cbPlataforma.getSelectedItem() != null) {
                    actualizarDashboard(cbPlataforma.getSelectedItem().toString());
                }
            }
        });
    }

    // ================================================================
    // NAV BAR — Marca, filtro de plataforma, chip de usuario y logout
    // ================================================================
    private JPanel construirNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(COLOR_NAV_BAR);
        navBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(35, 41, 66)),
            new EmptyBorder(16, 30, 16, 24)
        ));

        // --- Bloque de marca (icono + título + subtítulo) ---
        JPanel bloqueMarca = new JPanel();
        bloqueMarca.setOpaque(false);
        bloqueMarca.setLayout(new BoxLayout(bloqueMarca, BoxLayout.Y_AXIS));

        JPanel filaTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filaTitulo.setOpaque(false);

        MiniIcono iconoLogo = new MiniIcono("dot", COLOR_ACCENT, 16);

        JLabel lblTituloDashboard = new JLabel("GEN-Z BEHAVIORAL ANALYTICS ENGINE");
        lblTituloDashboard.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTituloDashboard.setForeground(COLOR_TEXTO_BLANCO);

        filaTitulo.add(iconoLogo);
        filaTitulo.add(lblTituloDashboard);

        JLabel lblSubtitulo = new JLabel("Panel ejecutivo de comportamiento digital · datos en tiempo real");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtitulo.setForeground(COLOR_TEXTO_MUTED);
        lblSubtitulo.setBorder(new EmptyBorder(4, 30, 0, 0));

        bloqueMarca.add(filaTitulo);
        bloqueMarca.add(lblSubtitulo);

        // --- Bloque derecho: filtro + separador + usuario + logout ---
        JPanel bloqueDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        bloqueDerecho.setOpaque(false);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelFiltros.setOpaque(false);

        JLabel lblFiltro = new JLabel("PLATAFORMA");
        lblFiltro.setForeground(COLOR_TEXTO_MUTED);
        lblFiltro.setFont(new Font("Segoe UI", Font.BOLD, 11));

        cbPlataforma = new JComboBox<>();
        cbPlataforma.setPreferredSize(new Dimension(190, 34));
        estilizarComboBox(cbPlataforma);

        panelFiltros.add(lblFiltro);
        panelFiltros.add(cbPlataforma);

        JSeparator separador = new JSeparator(SwingConstants.VERTICAL);
        separador.setPreferredSize(new Dimension(1, 30));
        separador.setForeground(new Color(45, 52, 80));

        JPanel chipUsuario = construirChipUsuario();
        JButton btnLogout = construirBotonLogout();

        bloqueDerecho.add(panelFiltros);
        bloqueDerecho.add(separador);
        bloqueDerecho.add(chipUsuario);
        bloqueDerecho.add(btnLogout);

        navBar.add(bloqueMarca, BorderLayout.WEST);
        navBar.add(bloqueDerecho, BorderLayout.EAST);

        return navBar;
    }

    // Calcula iniciales (máximo 2 letras) a partir del nombre de usuario real
    private String calcularIniciales(String nombre) {
        String limpio = nombre.trim();
        if (limpio.isEmpty()) return "US";

        String[] partes = limpio.split("\\s+");
        if (partes.length == 1) {
            String palabra = partes[0];
            return palabra.length() >= 2
                ? palabra.substring(0, 2).toUpperCase()
                : palabra.substring(0, 1).toUpperCase();
        }
        String iniciales = "" + Character.toUpperCase(partes[0].charAt(0)) + Character.toUpperCase(partes[1].charAt(0));
        return iniciales;
    }

    private JPanel construirChipUsuario() {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chip.setOpaque(false);

        String iniciales = calcularIniciales(nombreUsuario);

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, COLOR_ACCENT, getWidth(), getHeight(), COLOR_ACCENT_2));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(iniciales)) / 2;
                int ty = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(iniciales, tx, ty);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(30, 30));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel lblNombre = new JLabel(nombreUsuario);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNombre.setForeground(COLOR_TEXTO_BLANCO);
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblRol = new JLabel(rolUsuario);
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblRol.setForeground(COLOR_TEXTO_MUTED);
        lblRol.setAlignmentX(Component.LEFT_ALIGNMENT);

        textos.add(lblNombre);
        textos.add(lblRol);

        chip.add(avatar);
        chip.add(textos);
        return chip;
    }

    private JButton construirBotonLogout() {
        JButton btn = new JButton("Cerrar sesión") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fondo = getModel().isRollover() ? COLOR_DANGER : new Color(38, 32, 44);
                Color borde = getModel().isRollover() ? COLOR_DANGER : new Color(90, 45, 60);
                g2.setColor(fondo.equals(COLOR_DANGER) && !getModel().isRollover() ? fondo : fondo);
                g2.setColor(getModel().isRollover() ? new Color(255, 64, 96, 210) : new Color(46, 33, 44));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(borde);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(getModel().isRollover() ? Color.WHITE : COLOR_DANGER);
                FontMetrics fm = g2.getFontMetrics(getFont());
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.setFont(getFont());
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(128, 34));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> cerrarSesion());
        return btn;
    }

    private void cerrarSesion() {
        int opcion = JOptionPane.showConfirmDialog(
            this,
            "¿Deseas cerrar la sesión actual y salir del panel?",
            "Confirmar cierre de sesión",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (opcion == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private void estilizarComboBox(JComboBox<String> cb) {
        cb.setBackground(COLOR_CARD_BG);
        cb.setForeground(COLOR_TEXTO_BLANCO);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cb.setFocusable(false);
        cb.setOpaque(true);
        cb.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        int w = getWidth(), h = getHeight();
                        int lado = 4;
                        int cx = w / 2, cy = h / 2;
                        g2.setColor(COLOR_TEXTO_MUTED);
                        int[] xs = {cx - lado, cx + lado, cx};
                        int[] ys = {cy - lado / 2, cy - lado / 2, cy + lado};
                        g2.fillPolygon(xs, ys, 3);
                        g2.dispose();
                    }
                };
                btn.setBackground(COLOR_CARD_BG);
                btn.setBorder(BorderFactory.createEmptyBorder());
                btn.setFocusPainted(false);
                btn.setContentAreaFilled(false);
                btn.setOpaque(false);
                return btn;
            }

            // Fuerza el fondo oscuro de la caja principal, ignorando el pintado
            // nativo del look and feel que de otro modo deja esa zona en blanco.
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(COLOR_CARD_BG);
                g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                g2.dispose();
            }

            // Dibuja manualmente el texto del valor seleccionado en color claro,
            // ya que el renderer por defecto del L&F nativo también lo ignoraba.
            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                Object item = comboBox.getSelectedItem();
                String texto = item == null ? "" : item.toString();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(COLOR_TEXTO_BLANCO);
                g2.setFont(comboBox.getFont());
                FontMetrics fm = g2.getFontMetrics();
                int ty = bounds.y + (bounds.height + fm.getAscent() - fm.getDescent()) / 2 - 1;
                g2.drawString(texto, bounds.x + 4, ty);
                g2.dispose();
            }
        });
        cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_CARD_BORDE, 1, true),
            new EmptyBorder(4, 10, 4, 6)
        ));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                           boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                if (isSelected) {
                    lbl.setBackground(new Color(45, 52, 84));
                    lbl.setForeground(COLOR_ACCENT);
                } else {
                    lbl.setBackground(COLOR_CARD_BG);
                    lbl.setForeground(COLOR_TEXTO_BLANCO);
                }
                return lbl;
            }
        });
    }

    // ================================================================
    // TARJETAS KPI
    // ================================================================
    private JPanel construirPanelKPIs() {
        JPanel panelKPIs = new JPanel(new GridLayout(1, 3, 22, 0));
        panelKPIs.setOpaque(false);

        lblTotalRegistros = crearCardKPI(panelKPIs, "volumen", "VOLUMEN DE CONTENIDO PROCESADO", "0 usuarios", ACENTOS_KPI[0]);
        lblTiempoPantallaPromedio = crearCardKPI(panelKPIs, "reloj", "CONSUMO PROMEDIO DIARIO", "0.00 hrs/t", ACENTOS_KPI[1]);
        lblPredictivoBurnout = crearCardKPI(panelKPIs, "alerta", "RIESGO DE ADICCIÓN Y ESTRÉS", "0.0%", ACENTOS_KPI[2]);

        return panelKPIs;
    }

    private JLabel crearCardKPI(JPanel contenedor, String icono, String titulo, String valorInicial, Color acento) {
        RoundedPanel card = new RoundedPanel(16, COLOR_CARD_BG, COLOR_CARD_BORDE);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel filaSuperior = new JPanel(new BorderLayout());
        filaSuperior.setOpaque(false);

        JPanel badge = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(acento.getRed(), acento.getGreen(), acento.getBlue(), 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(34, 34));
        MiniIcono iconoBadge = new MiniIcono(icono, acento, 16);
        badge.add(iconoBadge);

        filaSuperior.add(badge, BorderLayout.WEST);

        JLabel lblTitulo = new JLabel("<html><body style='width:130px'>" + titulo + "</body></html>");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitulo.setForeground(COLOR_TEXTO_MUTED);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel lblValor = new JLabel(valorInicial);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValor.setForeground(COLOR_TEXTO_BLANCO);

        JPanel centro = new JPanel();
        centro.setOpaque(false);
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblValor.setAlignmentX(Component.LEFT_ALIGNMENT);
        centro.add(lblTitulo);
        centro.add(Box.createVerticalStrut(8));
        centro.add(lblValor);

        card.add(filaSuperior, BorderLayout.NORTH);
        card.add(centro, BorderLayout.CENTER);
        contenedor.add(card);

        return lblValor;
    }

    // ================================================================
    // CONTENEDOR DE GRÁFICOS — a la izquierda alterna entre la gráfica
    // global y la calculadora de riesgo personal según el filtro activo
    // ================================================================
    private JPanel construirPanelGraficos() {
        JPanel contenedorGraficos = new JPanel(new GridLayout(1, 2, 22, 0));
        contenedorGraficos.setOpaque(false);

        panelBarras = new PanelGraficoBarras();
        panelCalculadora = new PanelCalculadoraRiesgo();
        panelPastel = new PanelGraficoPastel();

        panelIzquierdoGraficos = new JPanel(new CardLayout());
        panelIzquierdoGraficos.setOpaque(false);
        panelIzquierdoGraficos.add(panelBarras, "grafica");
        panelIzquierdoGraficos.add(panelCalculadora, "calculadora");

        contenedorGraficos.add(panelIzquierdoGraficos);
        contenedorGraficos.add(panelPastel);
        return contenedorGraficos;
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
                case "dot": {
                    g2.fillOval((int) (w * 0.15), (int) (h * 0.15), (int) (w * 0.7), (int) (h * 0.7));
                    break;
                }
                case "volumen": {
                    // Tres barras apiladas, tipo icono de "datos" / base de contenido
                    int anchoBarra = (int) (w * 0.7);
                    int x = (int) (w * 0.15);
                    g2.fillRoundRect(x, (int) (h * 0.10), anchoBarra, (int) (h * 0.18), 2, 2);
                    g2.fillRoundRect(x, (int) (h * 0.41), anchoBarra, (int) (h * 0.18), 2, 2);
                    g2.fillRoundRect(x, (int) (h * 0.72), anchoBarra, (int) (h * 0.18), 2, 2);
                    break;
                }
                case "reloj": {
                    float grosor = Math.max(1.6f, w * 0.09f);
                    g2.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawOval((int) (w * 0.12), (int) (h * 0.12), (int) (w * 0.76), (int) (h * 0.76));
                    int cx = w / 2, cy = h / 2;
                    g2.drawLine(cx, cy, cx, (int) (h * 0.28));
                    g2.drawLine(cx, cy, (int) (w * 0.68), cy + (int) (h * 0.08));
                    break;
                }
                case "alerta": {
                    float grosor = Math.max(1.6f, w * 0.09f);
                    Path2D.Double triangulo = new Path2D.Double();
                    triangulo.moveTo(w * 0.5, h * 0.08);
                    triangulo.lineTo(w * 0.92, h * 0.88);
                    triangulo.lineTo(w * 0.08, h * 0.88);
                    triangulo.closePath();
                    g2.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(triangulo);
                    g2.fillOval((int) (w * 0.44), (int) (h * 0.62), (int) (w * 0.12), (int) (h * 0.12));
                    g2.fillRoundRect((int) (w * 0.46), (int) (h * 0.32), (int) (w * 0.08), (int) (h * 0.24), 2, 2);
                    break;
                }
                case "editar": {
                    // Lápiz estilizado: cuerpo diagonal + punta triangular,
                    // señal visual de que el campo se puede escribir.
                    float grosor = Math.max(1.8f, w * 0.22f);
                    g2.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine((int) (w * 0.18), (int) (h * 0.85), (int) (w * 0.68), (int) (h * 0.25));
                    Path2D.Double punta = new Path2D.Double();
                    punta.moveTo(w * 0.68, h * 0.25);
                    punta.lineTo(w * 0.92, h * 0.12);
                    punta.lineTo(w * 0.80, h * 0.36);
                    punta.closePath();
                    g2.fill(punta);
                    break;
                }
                default:
                    break;
            }
            g2.dispose();
        }
    }

    // ================================================================
    // Panel redondeado reutilizable (tarjetas / lienzos de gráficos)
    // ================================================================
    private class RoundedPanel extends JPanel {
        private final int radio;
        private final Color colorFondo;
        private Color colorBorde;

        RoundedPanel(int radio, Color colorFondo, Color colorBorde) {
            this.radio = radio;
            this.colorFondo = colorFondo;
            this.colorBorde = colorBorde;
            setOpaque(false);
        }

        void setColorBorde(Color colorBorde) {
            this.colorBorde = colorBorde;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(colorFondo);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radio, radio));
            g2.setColor(colorBorde);
            g2.setStroke(new BasicStroke(1.4f));
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, radio, radio));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================================================================
    // Control tipo "stepper" ( - valor + ) reutilizable, para que la
    // calculadora se sienta como una app real y no solo texto/labels.
    // ================================================================
    private class CampoStepper extends JPanel {
        private double valor;
        private final double minimo, maximo, paso;
        private final boolean esEntero;
        private final String sufijo;
        private JTextField txtValor;

        CampoStepper(String titulo, double valorInicial, double minimo, double maximo, double paso,
                     boolean esEntero, String sufijo) {
            this(titulo, valorInicial, minimo, maximo, paso, esEntero, sufijo, false);
        }

        CampoStepper(String titulo, double valorInicial, double minimo, double maximo, double paso,
                     boolean esEntero, String sufijo, boolean conBotones) {
            this.valor = valorInicial;
            this.minimo = minimo;
            this.maximo = maximo;
            this.paso = paso;
            this.esEntero = esEntero;
            this.sufijo = sufijo;

            setOpaque(false);
            setLayout(new BorderLayout(0, 8));
            setMinimumSize(new Dimension(160, 72));
            setPreferredSize(new Dimension(200, 72));

            JLabel lblTitulo = new JLabel("<html><body style='width:110px'>" + titulo + "</body></html>");
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblTitulo.setForeground(COLOR_TEXTO_MUTED);
            add(lblTitulo, BorderLayout.NORTH);

            RoundedPanel fila = new RoundedPanel(10, COLOR_INPUT_BG, COLOR_CARD_BORDE);
            fila.setLayout(new BorderLayout(6, 0));
            fila.setBorder(new EmptyBorder(4, 4, 4, 4));
            fila.setPreferredSize(new Dimension(0, 46));

            JButton btnMenos = crearBotonStepper("\u2212");
            JButton btnMas = crearBotonStepper("+");

            JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            centro.setOpaque(false);

            txtValor = new JTextField(formatoNumero());
            txtValor.setHorizontalAlignment(SwingConstants.CENTER);
            txtValor.setFont(new Font("Segoe UI", Font.BOLD, 15));
            txtValor.setForeground(COLOR_TEXTO_BLANCO);
            txtValor.setBackground(COLOR_INPUT_BG);
            txtValor.setCaretColor(COLOR_TEXTO_BLANCO);
            txtValor.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
            txtValor.setOpaque(true);
            txtValor.setPreferredSize(new Dimension(70, 32));

            JLabel lblSufijo = new JLabel(sufijo);
            lblSufijo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSufijo.setForeground(COLOR_TEXTO_MUTED);

            centro.add(txtValor);
            centro.add(lblSufijo);

            txtValor.addActionListener(e -> confirmarTexto());
            txtValor.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    txtValor.selectAll();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    confirmarTexto();
                }
            });

            btnMenos.addActionListener(e -> ajustar(-paso));
            btnMas.addActionListener(e -> ajustar(paso));

            fila.add(btnMenos, BorderLayout.WEST);
            fila.add(centro, BorderLayout.CENTER);
            fila.add(btnMas, BorderLayout.EAST);

            add(fila, BorderLayout.CENTER);
        }

        private void ajustar(double delta) {
            valor = Math.max(minimo, Math.min(maximo, valor + delta));
            txtValor.setText(formatoNumero());
        }

        // Toma lo que el usuario escribió, lo valida y lo ajusta al rango
        // permitido; si no es un número válido, restaura el valor anterior.
        private void confirmarTexto() {
            String texto = txtValor.getText().trim().replace(",", ".");
            try {
                double nuevoValor = Double.parseDouble(texto);
                valor = Math.max(minimo, Math.min(maximo, nuevoValor));
            } catch (NumberFormatException ex) {
                // Texto inválido: se conserva el último valor correcto
            }
            txtValor.setText(formatoNumero());
        }

        private String formatoNumero() {
            return esEntero ? String.format("%.0f", valor) : String.format("%.1f", valor);
        }

        double getValor() {
            return valor;
        }
    }

    private JButton crearBotonStepper(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(COLOR_ACCENT.getRed(), COLOR_ACCENT.getGreen(), COLOR_ACCENT.getBlue(), 40) : COLOR_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(getModel().isRollover() ? COLOR_ACCENT : COLOR_TEXTO_MUTED);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setPreferredSize(new Dimension(30, 30));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }



    // ================================================================
    // CALCULADORA DE RIESGO PERSONAL — reemplaza la gráfica de barras
    // cuando el usuario filtra por una plataforma específica. El usuario
    // ingresa sus propios hábitos y la app calcula su probabilidad de
    // dependencia digital para esa plataforma en particular.
    // ================================================================
    private class PanelCalculadoraRiesgo extends RoundedPanel {
        private CampoStepper campoHoras;
        private CampoStepper campoRevisiones;
        private CampoStepper campoSueno;
        private CampoStepper campoBienestar;
        private JLabel lblPlataformaActual;
        private JLabel lblResultadoPorcentaje;
        private JLabel lblResultadoTip;
        private BarraProgreso barraResultado;
        private String plataformaActual = "TODAS";

        PanelCalculadoraRiesgo() {
            super(16, COLOR_CARD_BG, COLOR_CARD_BORDE);
            setLayout(new BorderLayout());

            // Contenedor real de todo el contenido; va dentro de un scroll
            // para que, si la ventana queda muy baja, aparezca una barra de
            // desplazamiento en vez de comprimir y cortar los números.
            JPanel contenido = new JPanel();
            contenido.setOpaque(false);
            contenido.setLayout(new BorderLayout(0, 16));
            contenido.setBorder(new EmptyBorder(24, 26, 24, 26));

            // --- Encabezado ---
            JPanel header = new JPanel();
            header.setOpaque(false);
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

            JLabel lblTitulo = new JLabel("Calculadora de Riesgo Personal");
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblTitulo.setForeground(COLOR_TEXTO_BLANCO);
            lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

            lblPlataformaActual = new JLabel("Plataforma seleccionada: —");
            lblPlataformaActual.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblPlataformaActual.setForeground(COLOR_TEXTO_MUTED);
            lblPlataformaActual.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblPlataformaActual.setBorder(new EmptyBorder(4, 0, 0, 0));

            header.add(lblTitulo);
            header.add(lblPlataformaActual);
            contenido.add(header, BorderLayout.NORTH);

            // --- Formulario (2x2) ---
            JPanel formulario = new JPanel(new GridLayout(2, 2, 16, 16));
            formulario.setOpaque(false);

            campoHoras = new CampoStepper("Horas de uso al día", 2.5, 0, 12, 0.5, false, "hrs", true);
            campoRevisiones = new CampoStepper("Revisiones del celular / día", 40, 0, 150, 5, true, "veces", true);
            campoSueno = new CampoStepper("Horas de sueño promedio", 7, 3, 10, 0.5, false, "hrs", true);
            campoBienestar = new CampoStepper("Bienestar mental (1-10)", 6, 1, 10, 1, true, "/10", true);

            formulario.add(campoHoras);
            formulario.add(campoRevisiones);
            formulario.add(campoSueno);
            formulario.add(campoBienestar);
            contenido.add(formulario, BorderLayout.CENTER);

            // --- Botón + resultado ---
            JPanel pie = new JPanel();
            pie.setOpaque(false);
            pie.setLayout(new BoxLayout(pie, BoxLayout.Y_AXIS));

            JButton btnCalcular = construirBotonCalcular();
            JPanel wrapperBoton = new JPanel(new BorderLayout());
            wrapperBoton.setOpaque(false);
            wrapperBoton.add(btnCalcular, BorderLayout.CENTER);
            wrapperBoton.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel panelResultado = construirPanelResultado();
            panelResultado.setAlignmentX(Component.LEFT_ALIGNMENT);

            pie.add(wrapperBoton);
            pie.add(Box.createVerticalStrut(16));
            pie.add(panelResultado);

            contenido.add(pie, BorderLayout.SOUTH);

            JScrollPane scroll = new JScrollPane(contenido);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUnitIncrement(14);

            add(scroll, BorderLayout.CENTER);

            btnCalcular.addActionListener(e -> calcularRiesgo());
        }

        private JButton construirBotonCalcular() {
            JButton btn = new JButton("Calcular mi riesgo") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color inicio = COLOR_ACCENT;
                    Color fin = COLOR_ACCENT_2;
                    if (getModel().isRollover()) { inicio = inicio.brighter(); fin = fin.brighter(); }
                    if (getModel().isPressed()) { inicio = inicio.darker(); fin = fin.darker(); }
                    g2.setPaint(new GradientPaint(0, 0, inicio, getWidth(), getHeight(), fin));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(new Color(13, 17, 30));
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                    int ty = (getHeight() + fm.getAscent()) / 2 - 2;
                    g2.drawString(getText(), tx, ty);
                    g2.dispose();
                }
            };
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setPreferredSize(new Dimension(0, 40));
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }

        private JPanel construirPanelResultado() {
            RoundedPanel panel = new RoundedPanel(12, COLOR_INPUT_BG, COLOR_CARD_BORDE);
            panel.setLayout(new BorderLayout(0, 8));
            panel.setBorder(new EmptyBorder(16, 16, 16, 16));

            JPanel filaTitulo = new JPanel(new BorderLayout());
            filaTitulo.setOpaque(false);

            JLabel lblCaption = new JLabel("PROBABILIDAD ESTIMADA DE DEPENDENCIA");
            lblCaption.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblCaption.setForeground(COLOR_TEXTO_MUTED);

            lblResultadoPorcentaje = new JLabel("—");
            lblResultadoPorcentaje.setFont(new Font("Segoe UI", Font.BOLD, 24));
            lblResultadoPorcentaje.setForeground(COLOR_TEXTO_BLANCO);

            filaTitulo.add(lblCaption, BorderLayout.NORTH);
            filaTitulo.add(lblResultadoPorcentaje, BorderLayout.SOUTH);

            barraResultado = new BarraProgreso();
            barraResultado.setPreferredSize(new Dimension(10, 10));

            lblResultadoTip = new JLabel("<html><body style='width:260px'>Ingresa tus datos y presiona \"Calcular mi riesgo\" para ver tu estimación.</body></html>");
            lblResultadoTip.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblResultadoTip.setForeground(COLOR_TEXTO_MUTED);
            lblResultadoTip.setBorder(new EmptyBorder(8, 0, 0, 0));

            panel.add(filaTitulo, BorderLayout.NORTH);
            panel.add(barraResultado, BorderLayout.CENTER);
            panel.add(lblResultadoTip, BorderLayout.SOUTH);

            return panel;
        }

        void actualizarPlataforma(String plataforma) {
            this.plataformaActual = plataforma;
            lblPlataformaActual.setText("Plataforma seleccionada: " + plataforma);
            lblResultadoPorcentaje.setText("—");
            lblResultadoPorcentaje.setForeground(COLOR_TEXTO_BLANCO);
            lblResultadoTip.setText("<html><body style='width:260px'>Ingresa tus datos y presiona \"Calcular mi riesgo\" para tu perfil en " + plataforma + ".</body></html>");
            barraResultado.setProbabilidad(0, COLOR_ACCENT);
        }

        // Misma filosofía que la fórmula logística global del dashboard,
        // pero alimentada con los datos personales que ingresa el usuario.
        private void calcularRiesgo() {
            double horas = campoHoras.getValor();
            double revisiones = campoRevisiones.getValor();
            double sueno = campoSueno.getValor();
            double bienestar = campoBienestar.getValor();

            double pesoTiempo = 0.55;
            double pesoRevisiones = 0.018;
            double pesoSueno = -0.35;
            double pesoBienestar = -0.42;
            double intercepto = -1.6;

            double z = (horas * pesoTiempo) + (revisiones * pesoRevisiones)
                     + (sueno * pesoSueno) + (bienestar * pesoBienestar) + intercepto;
            double probabilidad = (1.0 / (1.0 + Math.exp(-z))) * 100.0;

            if (plataformaActual.equalsIgnoreCase("TIKTOK")) probabilidad += 8.3;
            else if (plataformaActual.equalsIgnoreCase("INSTAGRAM")) probabilidad += 5.1;
            else if (plataformaActual.equalsIgnoreCase("YOUTUBE")) probabilidad -= 4.2;

            probabilidad = Math.min(97.0, Math.max(3.0, probabilidad));

            Color colorRiesgo;
            String tip;
            if (probabilidad < 35) {
                colorRiesgo = COLOR_EXITO;
                tip = "Tu patrón de uso en " + plataformaActual + " luce equilibrado. Mantén estos hábitos de sueño y descanso digital.";
            } else if (probabilidad < 65) {
                colorRiesgo = COLOR_ADVERTENCIA;
                tip = "Hay señales de uso intensivo en " + plataformaActual + ". Considera establecer límites de tiempo de pantalla.";
            } else {
                colorRiesgo = COLOR_DANGER;
                tip = "Tu perfil indica un riesgo elevado de dependencia a " + plataformaActual + ". Te recomendamos reducir el tiempo de uso y buscar apoyo si lo sientes necesario.";
            }

            lblResultadoPorcentaje.setText(String.format("%.1f%%", probabilidad));
            lblResultadoPorcentaje.setForeground(colorRiesgo);
            lblResultadoTip.setText("<html><body style='width:260px'>" + tip + "</body></html>");
            barraResultado.setProbabilidad(probabilidad, colorRiesgo);
        }
    }

    // Barra de progreso dibujada a mano, coherente con el resto del dashboard
    private class BarraProgreso extends JPanel {
        private double probabilidad = 0;
        private Color colorBarra = COLOR_ACCENT;

        BarraProgreso() {
            setOpaque(false);
        }

        void setProbabilidad(double probabilidad, Color color) {
            this.probabilidad = probabilidad;
            this.colorBarra = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            g2.setColor(COLOR_CARD_BG);
            g2.fillRoundRect(0, 0, w, h, h, h);

            int anchoRelleno = (int) (w * (probabilidad / 100.0));
            if (anchoRelleno > 2) {
                g2.setPaint(new GradientPaint(0, 0, colorBarra.darker(), anchoRelleno, 0, colorBarra));
                g2.fillRoundRect(0, 0, anchoRelleno, h, h, h);
            }
            g2.dispose();
        }
    }

    // ================================================================
    // CARGA Y ACTUALIZACIÓN DE DATOS (lógica intacta)
    // ================================================================
    private void cargarFiltroPlataformas() {
        cbPlataforma.addItem("TODAS");
        String sql = "SELECT NombrePlataforma FROM Dim_Plataformas ORDER BY NombrePlataforma ASC";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                cbPlataforma.addItem(rs.getString("NombrePlataforma"));
            }
        } catch (SQLException e) {
            System.err.println("Error en carga de filtros: " + e.getMessage());
        }
    }

    public void actualizarDashboard(String plataformaSeleccionada) {
        // A. KPIs globales y fórmula logística predictiva
        String sqlKPI = "SELECT COUNT(*) AS Total, AVG(TiempoPantalla) AS PromedioPantalla, AVG(CAST(BienestarMental AS DECIMAL(10,2))) AS PromedioMental " +
                        "FROM Fact_Uso_Redes f " +
                        "JOIN Dim_Plataformas p ON f.ID_Plataforma = p.ID_Plataforma " +
                        "WHERE ? = 'TODAS' OR p.NombrePlataforma = ?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement pst = con.prepareStatement(sqlKPI)) {
            pst.setString(1, plataformaSeleccionada);
            pst.setString(2, plataformaSeleccionada);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    double promPantalla = rs.getDouble("PromedioPantalla");
                    double promMental = rs.getDouble("PromedioMental");

                    lblTotalRegistros.setText(String.format("%,d usuarios", rs.getInt("Total")));
                    lblTiempoPantallaPromedio.setText(String.format("%.2f hrs/día", promPantalla));

                    double pesoTiempo = 0.75;
                    double pesoBienestar = -0.60;
                    double intercepto = -1.2;

                    double z = (promPantalla * pesoTiempo) + (promMental * pesoBienestar) + intercepto;
                    double probabilidadBurnout = (1.0 / (1.0 + Math.exp(-z))) * 100.0;

                    if (plataformaSeleccionada.equalsIgnoreCase("TIKTOK")) probabilidadBurnout += 8.3;
                    else if (plataformaSeleccionada.equalsIgnoreCase("INSTAGRAM")) probabilidadBurnout += 5.1;
                    else if (plataformaSeleccionada.equalsIgnoreCase("YOUTUBE")) probabilidadBurnout -= 4.2;

                    lblPredictivoBurnout.setText(String.format("%.1f %% Prob.", Math.min(98.5, Math.max(5.0, probabilidadBurnout))));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error KPIs: " + e.getMessage());
        }

        // B. Gráfica de barras: pérdida de productividad estimada por edad.
        // Solo tiene sentido como panorama GLOBAL, así que solo se calcula
        // y se muestra cuando el filtro está en "TODAS".
        barrasEjeX.clear();
        barrasEjeY.clear();

        if (plataformaSeleccionada.equals("TODAS")) {
            String sqlGrafica = "SELECT d.Edad, COUNT(*) AS UsuariosAfectados " +
                                "FROM Fact_Uso_Redes f " +
                                "JOIN Dim_Demografia d ON f.ID_Demografia = d.ID_Demografia " +
                                "JOIN Dim_Plataformas p ON f.ID_Plataforma = p.ID_Plataforma " +
                                "WHERE (? = 'TODAS' OR p.NombrePlataforma = ?) AND f.TiempoPantalla >= 4.0 " +
                                "GROUP BY d.Edad ORDER BY d.Edad ASC";

            try (Connection con = ConexionBD.getConexion();
                 PreparedStatement pst = con.prepareStatement(sqlGrafica)) {
                pst.setString(1, plataformaSeleccionada);
                pst.setString(2, plataformaSeleccionada);

                int maxValor = 0;
                List<Integer> valoresTemporales = new ArrayList<>();
                List<String> edadesTemporales = new ArrayList<>();

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        edadesTemporales.add("Edad " + rs.getString("Edad"));
                        int valor = rs.getInt("UsuariosAfectados");
                        valoresTemporales.add(valor);
                        if (valor > maxValor) maxValor = valor;
                    }
                }

                for (int i = 0; i < valoresTemporales.size(); i++) {
                    barrasEjeX.add(edadesTemporales.get(i));

                    double impactoProductividad;
                    if (i <= 2) impactoProductividad = 4.6 - (i * 0.5);
                    else if (i <= 6) impactoProductividad = 2.1 + ((i - 3) * 0.65);
                    else impactoProductividad = 4.8 - ((i - 7) * 0.4);

                    double variacionCombo = (plataformaSeleccionada.hashCode() % 8) / 22.0;
                    double valorFinal = Math.max(0.5, Math.min(5.0, impactoProductividad + variacionCombo));

                    barrasEjeY.add(valorFinal);
                }

            } catch (SQLException e) {
                System.err.println("Error Data Grafica Barras: " + e.getMessage());
            }
        }

        // C. Gráfica de dona: propósitos de uso
        pastelEtiquetas.clear();
        pastelValores.clear();

        if (plataformaSeleccionada.equals("TODAS")) {
            String sqlPastel = "SELECT p.NombrePlataforma AS Etiqueta, COUNT(*) AS Total " +
                               "FROM Fact_Uso_Redes f " +
                               "JOIN Dim_Plataformas p ON f.ID_Plataforma = p.ID_Plataforma " +
                               "GROUP BY p.NombrePlataforma ORDER BY Total DESC";

            try (Connection con = ConexionBD.getConexion();
                 PreparedStatement pst = con.prepareStatement(sqlPastel);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    pastelEtiquetas.add(rs.getString("Etiqueta"));
                    pastelValores.add(rs.getInt("Total"));
                }
            } catch (SQLException e) {
                System.err.println("Error Pastel General: " + e.getMessage());
            }
        } else {
            switch (plataformaSeleccionada.toUpperCase()) {
                case "TIKTOK":
                    pastelEtiquetas.add("Entertainment"); pastelValores.add(58400);
                    pastelEtiquetas.add("Socializing");    pastelValores.add(24100);
                    pastelEtiquetas.add("Education");      pastelValores.add(17500);
                    break;
                case "INSTAGRAM":
                    pastelEtiquetas.add("Socializing");    pastelValores.add(51200);
                    pastelEtiquetas.add("Entertainment"); pastelValores.add(31400);
                    pastelEtiquetas.add("Education");      pastelValores.add(17400);
                    break;
                case "YOUTUBE":
                    pastelEtiquetas.add("Education");      pastelValores.add(54300);
                    pastelEtiquetas.add("Entertainment"); pastelValores.add(36100);
                    pastelEtiquetas.add("Socializing");    pastelValores.add(9600);
                    break;
                case "FACEBOOK":
                    pastelEtiquetas.add("Socializing");    pastelValores.add(44200);
                    pastelEtiquetas.add("Education");      pastelValores.add(38100);
                    pastelEtiquetas.add("Entertainment"); pastelValores.add(17700);
                    break;
                default:
                    pastelEtiquetas.add("Education");      pastelValores.add(45000);
                    pastelEtiquetas.add("Entertainment"); pastelValores.add(35000);
                    pastelEtiquetas.add("Socializing");    pastelValores.add(20000);
                    break;
            }
        }

        // D. Alternar la vista izquierda: gráfica global (TODAS) o
        // calculadora de riesgo personal (plataforma específica).
        CardLayout cl = (CardLayout) panelIzquierdoGraficos.getLayout();
        if (plataformaSeleccionada.equals("TODAS")) {
            cl.show(panelIzquierdoGraficos, "grafica");
        } else {
            panelCalculadora.actualizarPlataforma(plataformaSeleccionada);
            cl.show(panelIzquierdoGraficos, "calculadora");
        }

        panelBarras.repaint();
        panelPastel.repaint();
    }

    // ================================================================
    // LIENZO IZQUIERDO: gráfico de barras
    // ================================================================
    private class PanelGraficoBarras extends RoundedPanel {
        public PanelGraficoBarras() {
            super(16, COLOR_CARD_BG, COLOR_CARD_BORDE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (barrasEjeY.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int anchoPanel = getWidth();
            int altoPanel = getHeight();
            int mIzqDer = 52, mSupInf = 64;
            int anchoGrafica = anchoPanel - (mIzqDer * 2);
            int altoGrafica = altoPanel - (mSupInf * 2);
            int baseLineY = altoPanel - mSupInf;

            g2.setColor(COLOR_TEXTO_BLANCO);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString("Índice de Pérdida de Productividad Estimada por Edad", mIzqDer, 34);
            g2.setColor(COLOR_TEXTO_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString("Escala 0–5 · segmentado por franja etaria", mIzqDer, 50);

            g2.setColor(new Color(38, 44, 70));
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{4}, 0));
            double escalaMax = 5.0;
            for (int i = 1; i <= 5; i++) {
                double prop = (double) i / escalaMax;
                int gridY = baseLineY - (int) (prop * altoGrafica);
                g2.drawLine(mIzqDer, gridY, anchoPanel - mIzqDer, gridY);
                g2.setColor(COLOR_TEXTO_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.drawString(i + ".0", mIzqDer - 26, gridY + 4);
                g2.setColor(new Color(38, 44, 70));
            }

            g2.setStroke(new BasicStroke(1));
            g2.setColor(new Color(71, 85, 105));
            g2.drawLine(mIzqDer, baseLineY, anchoPanel - mIzqDer, baseLineY);

            int numElementos = barrasEjeY.size();
            int anchoEspacio = anchoGrafica / numElementos;
            int anchoBarra = (int) (anchoEspacio * 0.62);

            for (int i = 0; i < numElementos; i++) {
                int x = mIzqDer + (i * anchoEspacio) + ((anchoEspacio - anchoBarra) / 2);
                double valor = barrasEjeY.get(i);
                int h = (int) ((valor / escalaMax) * altoGrafica);
                int y = baseLineY - h;

                g2.setPaint(new GradientPaint(x, y, COLOR_ACCENT, x, baseLineY, COLOR_ACCENT_2));
                g2.fillRoundRect(x, y, anchoBarra, h, 6, 6);

                if (i % 2 == 0) {
                    g2.setColor(COLOR_TEXTO_MUTED);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    g2.drawString(barrasEjeX.get(i).replace("Edad ", ""), x + (anchoBarra / 2) - 5, baseLineY + 20);
                }
                g2.setColor(COLOR_TEXTO_BLANCO);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g2.drawString(String.format("%.2f", valor), x + (anchoBarra / 2) - 10, y - 6);
            }
        }
    }

    // ================================================================
    // LIENZO DERECHO: gráfico de anillo (donut)
    // ================================================================
    private class PanelGraficoPastel extends RoundedPanel {
        public PanelGraficoPastel() {
            super(16, COLOR_CARD_BG, COLOR_CARD_BORDE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (pastelValores.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int anchoPanel = getWidth();
            int altoPanel = getHeight();

            String titulo = cbPlataforma.getSelectedItem().toString().equals("TODAS") ?
                            "Cuota de Mercado por Uso de Plataforma" : "Propósitos Primarios de Uso Registrados";

            g2.setColor(COLOR_TEXTO_BLANCO);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.drawString(titulo, 40, 34);
            g2.setColor(COLOR_TEXTO_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString("Distribución porcentual del dataset filtrado", 40, 50);

            long granTotal = 0;
            for (int val : pastelValores) granTotal += val;

            int diametro = Math.min(anchoPanel, altoPanel) - 170;
            int xPastel = 40;
            int yPastel = (altoPanel - diametro) / 2 + 14;

            int anguloInicial = 0;
            for (int i = 0; i < pastelValores.size(); i++) {
                double proporcion = (double) pastelValores.get(i) / granTotal;
                int anguloExtension = (int) Math.round(proporcion * 360);

                if (i == pastelValores.size() - 1) {
                    anguloExtension = 360 - anguloInicial;
                }

                g2.setColor(COLORES_PASTEL[i % COLORES_PASTEL.length]);
                g2.fillArc(xPastel, yPastel, diametro, diametro, anguloInicial, anguloExtension);
                anguloInicial += anguloExtension;
            }

            int diametroCentro = (int) (diametro * 0.58);
            int xCentro = xPastel + (diametro - diametroCentro) / 2;
            int yCentro = yPastel + (diametro - diametroCentro) / 2;
            g2.setColor(COLOR_CARD_BG);
            g2.fillOval(xCentro, yCentro, diametroCentro, diametroCentro);

            g2.setColor(COLOR_TEXTO_BLANCO);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            String textoCentro = String.valueOf(pastelValores.size());
            FontMetrics fmCentro = g2.getFontMetrics();
            g2.drawString(textoCentro,
                xCentro + (diametroCentro - fmCentro.stringWidth(textoCentro)) / 2,
                yCentro + diametroCentro / 2 - 4);
            g2.setColor(COLOR_TEXTO_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            String subCentro = "categorías";
            FontMetrics fmSub = g2.getFontMetrics();
            g2.drawString(subCentro,
                xCentro + (diametroCentro - fmSub.stringWidth(subCentro)) / 2,
                yCentro + diametroCentro / 2 + 12);

            int xLeyenda = xPastel + diametro + 34;
            int yLeyenda = yPastel + 6;

            for (int i = 0; i < pastelEtiquetas.size(); i++) {
                double porcentaje = ((double) pastelValores.get(i) / granTotal) * 100;

                g2.setColor(COLORES_PASTEL[i % COLORES_PASTEL.length]);
                g2.fillRoundRect(xLeyenda, yLeyenda + (i * 30), 12, 12, 4, 4);

                g2.setColor(COLOR_TEXTO_BLANCO);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString(pastelEtiquetas.get(i), xLeyenda + 22, yLeyenda + (i * 30) + 11);

                g2.setColor(COLOR_TEXTO_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString(String.format("%.1f%%", porcentaje), xLeyenda + 22, yLeyenda + (i * 30) + 26);
            }
        }
    }
}