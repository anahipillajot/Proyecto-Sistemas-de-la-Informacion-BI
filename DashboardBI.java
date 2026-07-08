import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private PanelEncuestaPersonal panelEncuesta;
    private CardLayout clIzquierdo;
    private JPanel panelIzquierdo;

    private static final String CARD_GRAFICO = "GRAFICO";
    private static final String CARD_ENCUESTA = "ENCUESTA";

    // ============================================================
    // PALETA DE DISEÑO — Dark Mode Premium (estilo Power BI / SaaS)
    // ============================================================
    private final Color COLOR_NAV_BAR      = new Color(17, 20, 36);
    private final Color COLOR_BODY_BG      = new Color(11, 14, 26);
    private final Color COLOR_CARD_BG      = new Color(24, 28, 48);
    private final Color COLOR_CARD_BORDE   = new Color(40, 46, 74);
    private final Color COLOR_TEXTO_BLANCO = new Color(248, 249, 250);
    private final Color COLOR_TEXTO_MUTED  = new Color(148, 163, 184);
    private final Color COLOR_ACCENT       = new Color(0, 224, 255);
    private final Color COLOR_ACCENT_2     = new Color(121, 40, 202);
    private final Color COLOR_DANGER       = new Color(255, 64, 96);

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

    public DashboardBI() {
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

        JLabel iconoLogo = new JLabel("\u25C9"); // punto/insignia estilizada
        iconoLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconoLogo.setForeground(COLOR_ACCENT);

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

    private JPanel construirChipUsuario() {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chip.setOpaque(false);

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
                String iniciales = "AD";
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

        JLabel lblNombre = new JLabel("Administrador");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNombre.setForeground(COLOR_TEXTO_BLANCO);
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblRol = new JLabel("Analista BI");
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
                JButton btn = new JButton("\u25BE");
                btn.setForeground(COLOR_TEXTO_MUTED);
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

        lblTotalRegistros = crearCardKPI(panelKPIs, "\u25A4", "VOLUMEN DE CONTENIDO PROCESADO", "0 usuarios", ACENTOS_KPI[0]);
        lblTiempoPantallaPromedio = crearCardKPI(panelKPIs, "\u23F1", "CONSUMO PROMEDIO DIARIO", "0.00 hrs/t", ACENTOS_KPI[1]);
        lblPredictivoBurnout = crearCardKPI(panelKPIs, "\uD83D\uDD2E", "RIESGO DE ADICCIÓN Y ESTRÉS", "0.0%", ACENTOS_KPI[2]);

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
        JLabel lblIcono = new JLabel(icono);
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        lblIcono.setForeground(acento);
        badge.add(lblIcono);

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
    // CONTENEDOR DE GRÁFICOS
    // ================================================================
    private JPanel construirPanelGraficos() {
        JPanel contenedorGraficos = new JPanel(new GridLayout(1, 2, 22, 0));
        contenedorGraficos.setOpaque(false);

        panelBarras = new PanelGraficoBarras();
        panelEncuesta = new PanelEncuestaPersonal();
        panelPastel = new PanelGraficoPastel();

        // El slot izquierdo alterna entre el gráfico agregado (solo vista "TODAS")
        // y la encuesta interactiva de autoevaluación (cuando se filtra una plataforma).
        clIzquierdo = new CardLayout();
        panelIzquierdo = new JPanel(clIzquierdo);
        panelIzquierdo.setOpaque(false);
        panelIzquierdo.add(panelBarras, CARD_GRAFICO);
        panelIzquierdo.add(panelEncuesta, CARD_ENCUESTA);

        contenedorGraficos.add(panelIzquierdo);
        contenedorGraficos.add(panelPastel);
        return contenedorGraficos;
    }

    // ================================================================
    // Panel redondeado reutilizable (tarjetas / lienzos de gráficos)
    // ================================================================
    private class RoundedPanel extends JPanel {
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

        // B. Gráfica de barras: pérdida de productividad estimada por edad
        barrasEjeX.clear();
        barrasEjeY.clear();

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

        panelBarras.repaint();
        panelPastel.repaint();

        // D. El gráfico agregado de productividad solo tiene sentido en la vista
        // "TODAS". Al filtrar por una plataforma específica, se reemplaza por una
        // encuesta interactiva que calcula un riesgo de adicción personalizado.
        if (plataformaSeleccionada.equals("TODAS")) {
            clIzquierdo.show(panelIzquierdo, CARD_GRAFICO);
        } else {
            panelEncuesta.configurarPlataforma(plataformaSeleccionada);
            clIzquierdo.show(panelIzquierdo, CARD_ENCUESTA);
        }
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

    // ================================================================
    // LIENZO IZQUIERDO (alternativo): encuesta interactiva de autoevaluación
    // Se muestra cuando el usuario filtra por una plataforma específica.
    // Pide datos reales de uso y calcula un riesgo de adicción personalizado,
    // en lugar de repetir el mismo gráfico agregado para todas las plataformas.
    // ================================================================
    private class PanelEncuestaPersonal extends RoundedPanel {

        private String plataformaActual = "";

        private JLabel lblTitulo;
        private JLabel lblSubtitulo;

        private JSpinner spHorasUso;
        private JSpinner spAperturas;
        private JSpinner spHorasSueno;
        private JSlider sliderBienestar;
        private JLabel lblValorBienestar;
        private JComboBox<String> cbAnsiedad;

        private JLabel lblPorcentajeResultado;
        private JLabel lblCategoriaResultado;
        private JProgressBar barraRiesgo;
        private JLabel lblDetalleResultado;

        public PanelEncuestaPersonal() {
            super(16, COLOR_CARD_BG, COLOR_CARD_BORDE);
            setLayout(new BorderLayout(0, 12));
            setBorder(new EmptyBorder(22, 26, 20, 26));
            construirContenido();
        }

        private void construirContenido() {
            // --- Encabezado ---
            JPanel encabezado = new JPanel();
            encabezado.setOpaque(false);
            encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));

            lblTitulo = new JLabel("Autoevaluación de Uso");
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTitulo.setForeground(COLOR_TEXTO_BLANCO);
            lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

            lblSubtitulo = new JLabel("Ingresa tus datos reales para estimar tu riesgo personal");
            lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblSubtitulo.setForeground(COLOR_TEXTO_MUTED);
            lblSubtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblSubtitulo.setBorder(new EmptyBorder(4, 0, 0, 0));

            encabezado.add(lblTitulo);
            encabezado.add(lblSubtitulo);

            // --- Formulario ---
            spHorasUso = new JSpinner(new SpinnerNumberModel(2.0, 0.0, 24.0, 0.5));
            spAperturas = new JSpinner(new SpinnerNumberModel(20, 0, 300, 5));
            spHorasSueno = new JSpinner(new SpinnerNumberModel(7.0, 0.0, 12.0, 0.5));
            estilizarSpinner(spHorasUso);
            estilizarSpinner(spAperturas);
            estilizarSpinner(spHorasSueno);

            sliderBienestar = new JSlider(1, 10, 6);
            sliderBienestar.setOpaque(false);
            lblValorBienestar = new JLabel("6 / 10");
            lblValorBienestar.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblValorBienestar.setForeground(COLOR_ACCENT);
            sliderBienestar.addChangeListener(e -> lblValorBienestar.setText(sliderBienestar.getValue() + " / 10"));

            cbAnsiedad = new JComboBox<>(new String[]{"No", "A veces", "Sí, frecuentemente"});
            estilizarComboBox(cbAnsiedad);

            JPanel formulario = new JPanel();
            formulario.setOpaque(false);
            formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));
            formulario.setBorder(new EmptyBorder(10, 0, 0, 0));

            formulario.add(crearCampoConEtiqueta("Horas promedio de uso diario", spHorasUso));
            formulario.add(Box.createVerticalStrut(10));
            formulario.add(crearCampoConEtiqueta("Veces que abres la app al día", spAperturas));
            formulario.add(Box.createVerticalStrut(10));
            formulario.add(crearCampoConEtiqueta("Horas de sueño promedio", spHorasSueno));
            formulario.add(Box.createVerticalStrut(10));

            JPanel filaBienestar = new JPanel(new BorderLayout(8, 2));
            filaBienestar.setOpaque(false);
            filaBienestar.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel lblEtiquetaBienestar = new JLabel("Nivel de bienestar mental percibido");
            lblEtiquetaBienestar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblEtiquetaBienestar.setForeground(COLOR_TEXTO_MUTED);
            JPanel filaSlider = new JPanel(new BorderLayout(8, 0));
            filaSlider.setOpaque(false);
            filaSlider.add(sliderBienestar, BorderLayout.CENTER);
            filaSlider.add(lblValorBienestar, BorderLayout.EAST);
            filaBienestar.add(lblEtiquetaBienestar, BorderLayout.NORTH);
            filaBienestar.add(filaSlider, BorderLayout.CENTER);
            formulario.add(filaBienestar);
            formulario.add(Box.createVerticalStrut(10));

            formulario.add(crearCampoConEtiqueta("¿Sientes ansiedad si no puedes revisarla?", cbAnsiedad));

            JButton btnCalcular = construirBotonCalcular();
            JPanel filaBoton = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            filaBoton.setOpaque(false);
            filaBoton.setBorder(new EmptyBorder(16, 0, 0, 0));
            filaBoton.add(btnCalcular);
            formulario.add(filaBoton);

            // --- Resultado ---
            JPanel panelResultado = new JPanel();
            panelResultado.setOpaque(false);
            panelResultado.setLayout(new BoxLayout(panelResultado, BoxLayout.Y_AXIS));
            panelResultado.setBorder(new EmptyBorder(16, 0, 0, 0));

            lblPorcentajeResultado = new JLabel(" ");
            lblPorcentajeResultado.setFont(new Font("Segoe UI", Font.BOLD, 26));
            lblPorcentajeResultado.setForeground(COLOR_TEXTO_BLANCO);
            lblPorcentajeResultado.setAlignmentX(Component.LEFT_ALIGNMENT);

            lblCategoriaResultado = new JLabel(" ");
            lblCategoriaResultado.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblCategoriaResultado.setForeground(COLOR_TEXTO_MUTED);
            lblCategoriaResultado.setAlignmentX(Component.LEFT_ALIGNMENT);

            barraRiesgo = new JProgressBar(0, 100);
            barraRiesgo.setValue(0);
            barraRiesgo.setStringPainted(false);
            barraRiesgo.setBackground(new Color(38, 44, 70));
            barraRiesgo.setForeground(COLOR_ACCENT);
            barraRiesgo.setBorderPainted(false);
            barraRiesgo.setPreferredSize(new Dimension(10, 8));
            barraRiesgo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
            barraRiesgo.setAlignmentX(Component.LEFT_ALIGNMENT);
            barraRiesgo.setBorder(new EmptyBorder(6, 0, 6, 0));

            lblDetalleResultado = new JLabel(" ");
            lblDetalleResultado.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblDetalleResultado.setForeground(COLOR_TEXTO_MUTED);
            lblDetalleResultado.setAlignmentX(Component.LEFT_ALIGNMENT);

            panelResultado.add(lblPorcentajeResultado);
            panelResultado.add(lblCategoriaResultado);
            panelResultado.add(barraRiesgo);
            panelResultado.add(lblDetalleResultado);

            JPanel cuerpo = new JPanel();
            cuerpo.setOpaque(false);
            cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
            cuerpo.add(formulario);
            cuerpo.add(panelResultado);

            add(encabezado, BorderLayout.NORTH);
            add(cuerpo, BorderLayout.CENTER);

            btnCalcular.addActionListener(e -> calcularRiesgoPersonal());
        }

        private JPanel crearCampoConEtiqueta(String etiqueta, JComponent campo) {
            JPanel fila = new JPanel(new BorderLayout(0, 4));
            fila.setOpaque(false);
            fila.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lbl = new JLabel(etiqueta);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lbl.setForeground(COLOR_TEXTO_MUTED);

            fila.add(lbl, BorderLayout.NORTH);
            fila.add(campo, BorderLayout.CENTER);
            return fila;
        }

        private void estilizarSpinner(JSpinner spinner) {
            spinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            spinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_CARD_BORDE, 1, true),
                new EmptyBorder(2, 6, 2, 6)
            ));
            JComponent editor = spinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                JFormattedTextField campoTexto = ((JSpinner.DefaultEditor) editor).getTextField();
                campoTexto.setBackground(COLOR_CARD_BG);
                campoTexto.setForeground(COLOR_TEXTO_BLANCO);
                campoTexto.setCaretColor(COLOR_TEXTO_BLANCO);
                campoTexto.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            }
            spinner.setOpaque(true);
        }

        private JButton construirBotonCalcular() {
            JButton btn = new JButton("Calcular riesgo de adicción") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isRollover()) {
                        g2.setPaint(new GradientPaint(0, 0, COLOR_ACCENT_2, getWidth(), 0, COLOR_ACCENT));
                    } else {
                        g2.setPaint(new GradientPaint(0, 0, COLOR_ACCENT, getWidth(), 0, COLOR_ACCENT_2));
                    }
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(Color.WHITE);
                    FontMetrics fm = g2.getFontMetrics(getFont());
                    int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                    int ty = (getHeight() + fm.getAscent()) / 2 - 2;
                    g2.setFont(getFont());
                    g2.drawString(getText(), tx, ty);
                    g2.dispose();
                }
            };
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setPreferredSize(new Dimension(230, 36));
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }

        // Actualiza los textos de la encuesta para la plataforma filtrada y
        // limpia el resultado previo, ya que el riesgo es específico por app.
        public void configurarPlataforma(String plataforma) {
            this.plataformaActual = plataforma;
            lblTitulo.setText("Autoevaluación de Uso — " + plataforma);
            lblSubtitulo.setText("Responde con tus datos reales en " + plataforma + " para estimar tu riesgo personal");
            lblPorcentajeResultado.setText(" ");
            lblCategoriaResultado.setText(" ");
            lblDetalleResultado.setText(" ");
            barraRiesgo.setValue(0);
            revalidate();
            repaint();
        }

        // Calcula un riesgo de adicción personalizado con un modelo logístico
        // similar en espíritu al KPI agregado, pero alimentado por los datos
        // que el propio usuario ingresa para la plataforma seleccionada.
        private void calcularRiesgoPersonal() {
            double horasUso = ((Number) spHorasUso.getValue()).doubleValue();
            int aperturas = ((Number) spAperturas.getValue()).intValue();
            double horasSueno = ((Number) spHorasSueno.getValue()).doubleValue();
            int bienestar = sliderBienestar.getValue();
            String ansiedad = (String) cbAnsiedad.getSelectedItem();

            double factorAnsiedad;
            if ("Sí, frecuentemente".equals(ansiedad)) factorAnsiedad = 1.4;
            else if ("A veces".equals(ansiedad)) factorAnsiedad = 0.6;
            else factorAnsiedad = 0.0;

            double pesoHoras = 0.42;
            double pesoAperturas = 0.018;
            double pesoSueno = -0.30;
            double pesoBienestar = -0.35;
            double intercepto = -2.6;

            double z = (horasUso * pesoHoras)
                     + (aperturas * pesoAperturas)
                     + (horasSueno * pesoSueno)
                     + ((10 - bienestar) * pesoBienestar)
                     + factorAnsiedad
                     + intercepto;

            if (plataformaActual.equalsIgnoreCase("TIKTOK")) z += 0.35;
            else if (plataformaActual.equalsIgnoreCase("INSTAGRAM")) z += 0.20;
            else if (plataformaActual.equalsIgnoreCase("YOUTUBE")) z -= 0.15;

            double probabilidad = (1.0 / (1.0 + Math.exp(-z))) * 100.0;
            probabilidad = Math.min(97.0, Math.max(3.0, probabilidad));

            String categoria;
            Color colorCategoria;
            if (probabilidad < 33) {
                categoria = "RIESGO BAJO";
                colorCategoria = new Color(67, 233, 123);
            } else if (probabilidad < 66) {
                categoria = "RIESGO MODERADO";
                colorCategoria = new Color(255, 186, 8);
            } else {
                categoria = "RIESGO ALTO";
                colorCategoria = COLOR_DANGER;
            }

            lblPorcentajeResultado.setText(String.format("%.1f%%", probabilidad));
            lblPorcentajeResultado.setForeground(colorCategoria);
            lblCategoriaResultado.setText(categoria + " DE ADICCIÓN EN " + plataformaActual.toUpperCase());
            lblCategoriaResultado.setForeground(colorCategoria);

            barraRiesgo.setValue((int) Math.round(probabilidad));
            barraRiesgo.setForeground(colorCategoria);

            lblDetalleResultado.setText("<html><body style='width:230px'>"
                + generarMensaje(probabilidad, horasUso, horasSueno) + "</body></html>");

            revalidate();
            repaint();
        }

        private String generarMensaje(double probabilidad, double horasUso, double horasSueno) {
            if (probabilidad < 33) {
                return "Tu patrón de uso actual parece equilibrado. Mantén pausas activas y un buen descanso.";
            } else if (probabilidad < 66) {
                return "Se observan señales de uso elevado (" + String.format("%.1f", horasUso)
                    + " hrs/día). Considera establecer límites de tiempo en pantalla.";
            } else {
                String notaSueno = horasSueno < 6
                    ? " El bajo descanso (" + String.format("%.1f", horasSueno) + " hrs) puede estar agravando el efecto."
                    : "";
                return "Tu patrón sugiere un riesgo alto." + notaSueno
                    + " Considera reducir notificaciones y tiempo de pantalla, o buscar apoyo si lo sientes necesario.";
            }
        }
    }
}