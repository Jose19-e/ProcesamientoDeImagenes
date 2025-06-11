package com.mycompany.programa8;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

public class Programa8 {
    private static float zoom = 1.0f;
    private static BufferedImage imagen, imagenModificada;
    private static JLabel imageLabel, pixelInfoLabel;
    private static JFrame frame;
    private static JPanel infoPanel;
    private static JComboBox<String> filterComboBox, sizeComboBox;
    private static JSlider brightnessSlider;
    private static File archivoActual;
    private static JTable kernelTable;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Programa8::configurarInterfaz);
    }

    private static void configurarInterfaz() {
        frame = new JFrame("Programa8 - Procesamiento de Imágenes");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLayout(new BorderLayout());

        // Componentes de información
        infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pixelInfoLabel = new JLabel("Posición: (0,0) - RGB: (0,0,0)");
        pixelInfoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Configuración de la interfaz
        frame.add(crearHeaderPanel(), BorderLayout.NORTH);
        frame.add(crearPanelControles(), BorderLayout.EAST);
        frame.add(crearPanelBotones(), BorderLayout.SOUTH);

        // Área de visualización de imagen
        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                actualizarInfoPixel(e.getPoint());
            }
        });
        frame.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
        
        frame.setVisible(true);
    }

    private static void inicializarTablaKernel(int size) {
        DefaultTableModel model = (DefaultTableModel) kernelTable.getModel();
        model.setRowCount(size);
        model.setColumnCount(size);
        
        // Valores por defecto para kernels conocidos
        if (size == 3) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    model.setValueAt((i == 1 && j == 1) ? "8" : "-1", i, j);
                }
            }
        } else {
            // Para otros tamaños, inicializar con ceros
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    model.setValueAt("0", i, j);
                }
            }
        }
    }

    private static JPanel crearHeaderPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(infoPanel);
        panel.add(pixelInfoLabel);
        return panel;
    }

    private static JPanel crearPanelControles() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(crearPanelFiltros());
        panel.add(crearPanelBrillo());
        return panel;
    }

    private static JPanel crearPanelFiltros() {
        JPanel panel = new JPanel(new GridLayout(4, 1));

        // Selector de filtro
        JPanel p1 = new JPanel();
        p1.add(new JLabel("Filtro: "));
        filterComboBox = new JComboBox<>(new String[]{
            "Original", "Negativo", "Escala de Grises",
            "Borde", "Sobel H", "Sobel V",
            "Prewitt H", "Prewitt V", "Laplaciano", "Personalizado"
        });
        filterComboBox.addActionListener(e -> aplicarFiltrosYBrillo());
        p1.add(filterComboBox);
        panel.add(p1);

        // Selector de tamaño de kernel
        JPanel p2 = new JPanel();
        p2.add(new JLabel("Tamaño Kernel: "));
        sizeComboBox = new JComboBox<>(new String[]{"3x3", "5x5", "7x7", "9x9"});
        sizeComboBox.addActionListener(e -> {
            int size = Integer.parseInt(((String) sizeComboBox.getSelectedItem()).split("x")[0]);
            inicializarTablaKernel(size);
        });
        p2.add(sizeComboBox);
        panel.add(p2);

        // Tabla para kernel personalizado
        kernelTable = new JTable(new DefaultTableModel(3, 3));
        kernelTable.setRowHeight(25);
        inicializarTablaKernel(3);
        
        JScrollPane scrollKernel = new JScrollPane(kernelTable);
        scrollKernel.setBorder(BorderFactory.createTitledBorder("Matriz del Kernel"));
        panel.add(scrollKernel);

        // Botón para aplicar filtro personalizado
        JButton applyCustomFilterButton = new JButton("Aplicar Filtro Personalizado");
        applyCustomFilterButton.addActionListener(e -> aplicarFiltroPersonalizado());
        panel.add(applyCustomFilterButton);

        return panel;
    }

    private static JPanel crearPanelBrillo() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Brillo: "));
        brightnessSlider = new JSlider(-100, 100, 0);
        brightnessSlider.setMajorTickSpacing(50);
        brightnessSlider.setMinorTickSpacing(10);
        brightnessSlider.setPaintTicks(true);
        brightnessSlider.setPaintLabels(true);
        brightnessSlider.addChangeListener(e -> aplicarFiltrosYBrillo());
        panel.add(brightnessSlider);
        return panel;
    }

    private static JPanel crearPanelBotones() {
        JPanel panel = new JPanel();
        
        String[] botones = {"Abrir Imagen", "Zoom In (+)", "Zoom Out (-)", "Reset Zoom", "Guardar"};
        ActionListener[] acciones = {
            e -> abrirImagen(),
            e -> aplicarZoom(1.25f),
            e -> aplicarZoom(0.8f),
            e -> resetearZoom(),
            e -> guardarImagen()
        };
        
        for (int i = 0; i < botones.length; i++) {
            JButton btn = new JButton(botones[i]);
            btn.addActionListener(acciones[i]);
            panel.add(btn);
        }
        
        return panel;
    }

    private static void actualizarInfoPixel(Point mousePos) {
        if (imagen == null) return;
        
        BufferedImage img = imagenModificada != null ? imagenModificada : imagen;
        Point p = calcularPosicionPixel(mousePos, img);
        
        if (p.x >= 0 && p.y >= 0 && p.x < img.getWidth() && p.y < img.getHeight()) {
            Color c = new Color(img.getRGB(p.x, p.y));
            pixelInfoLabel.setText(String.format("Posición: (%d,%d) - RGB: (%d,%d,%d)",
                p.x, p.y, c.getRed(), c.getGreen(), c.getBlue()));
        } else {
            pixelInfoLabel.setText("Posición: (-,-) - RGB: (0,0,0)");
        }
    }

    private static Point calcularPosicionPixel(Point mousePos, BufferedImage img) {
        int labelW = imageLabel.getWidth();
        int labelH = imageLabel.getHeight();
        int imgW = (int)(img.getWidth() * zoom);
        int imgH = (int)(img.getHeight() * zoom);
        int offsetX = (labelW - imgW) / 2;
        int offsetY = (labelH - imgH) / 2;
        
        int x = (int)((mousePos.x - offsetX) / zoom);
        int y = (int)((mousePos.y - offsetY) / zoom);
        
        return new Point(x, y);
    }

    private static void abrirImagen() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                archivoActual = fc.getSelectedFile();
                imagen = ImageIO.read(archivoActual);
                if (imagen == null) throw new IOException("Formato no soportado");

                zoom = 1.0f;
                resetearControles();
                aplicarFiltrosYBrillo();
                actualizarInfo(archivoActual);
                centrarImagen();
            } catch (IOException ex) {
                mostrarError("Error al cargar: " + ex.getMessage());
            }
        }
    }

    private static void resetearControles() {
        filterComboBox.setSelectedIndex(0);
        brightnessSlider.setValue(0);
        sizeComboBox.setSelectedIndex(0);
        inicializarTablaKernel(3);
    }

    private static void aplicarZoom(float factor) {
        if (imagen == null) return;
        
        zoom *= factor;
        actualizarImagen();
        actualizarInfo(archivoActual);
        centrarImagen();
    }

    private static void resetearZoom() {
        if (imagen == null) return;
        
        zoom = 1.0f;
        actualizarImagen();
        actualizarInfo(archivoActual);
        centrarImagen();
    }

    private static void actualizarImagen() {
        if (imagenModificada == null) return;
        
        Image imgEscalada = imagenModificada.getScaledInstance(
            (int)(imagenModificada.getWidth() * zoom),
            (int)(imagenModificada.getHeight() * zoom),
            Image.SCALE_SMOOTH
        );
        imageLabel.setIcon(new ImageIcon(imgEscalada));
        frame.revalidate();
    }

    private static void centrarImagen() {
        if (imageLabel == null || imageLabel.getIcon() == null) return;
        
        SwingUtilities.invokeLater(() -> {
            JScrollPane jsp = (JScrollPane) frame.getContentPane().getComponent(2);
            JViewport viewport = jsp.getViewport();
            Dimension viewSize = viewport.getExtentSize();
            Dimension imgSize = imageLabel.getPreferredSize();
            
            int x = Math.max(0, (imgSize.width - viewSize.width) / 2);
            int y = Math.max(0, (imgSize.height - viewSize.height) / 2);
            
            viewport.setViewPosition(new Point(x, y));
        });
    }

    private static void actualizarInfo(File archivo) {
        infoPanel.removeAll();
        infoPanel.add(new JLabel("Archivo: " + archivo.getName() + " | "));
        infoPanel.add(new JLabel("Tamaño: " + imagen.getWidth() + "×" + imagen.getHeight() + " px | "));
        infoPanel.add(new JLabel("Píxeles: " + String.format("%,d", (long)imagen.getWidth() * imagen.getHeight()) + " | "));
        infoPanel.add(new JLabel("Zoom: " + String.format("%.0f%%", zoom * 100)));
        infoPanel.revalidate();
    }

    private static void aplicarFiltrosYBrillo() {
        if (imagen == null) return;

        // Crear copia de la imagen original
        imagenModificada = copiarImagen(imagen);

        // Aplicar filtro seleccionado
        String filtro = (String) filterComboBox.getSelectedItem();
        switch (filtro) {
            case "Negativo":
                aplicarNegativo(imagenModificada);
                break;
            case "Escala de Grises":
                aplicarEscalaDeGrises(imagenModificada);
                break;
            case "Borde":
                aplicarConvolucion(imagenModificada, new int[][]{{-1,-1,-1},{-1,8,-1},{-1,-1,-1}});
                break;
            case "Sobel H":
                aplicarConvolucion(imagenModificada, new int[][]{{-1,-2,-1},{0,0,0},{1,2,1}});
                break;
            case "Sobel V":
                aplicarConvolucion(imagenModificada, new int[][]{{-1,0,1},{-2,0,2},{-1,0,1}});
                break;
            case "Prewitt H":
                aplicarConvolucion(imagenModificada, new int[][]{{-1,-1,-1},{0,0,0},{1,1,1}});
                break;
            case "Prewitt V":
                aplicarConvolucion(imagenModificada, new int[][]{{-1,0,1},{-1,0,1},{-1,0,1}});
                break;
            case "Laplaciano":
                aplicarConvolucion(imagenModificada, new int[][]{{0,-1,0},{-1,4,-1},{0,-1,0}});
                break;
            case "Personalizado":
                aplicarFiltroPersonalizado();
                break;
        }

        // Aplicar brillo si es necesario
        int brillo = brightnessSlider.getValue();
        if (brillo != 0) aplicarBrillo(imagenModificada, brillo);

        actualizarImagen();
    }

    private static BufferedImage copiarImagen(BufferedImage original) {
        BufferedImage copia = new BufferedImage(
            original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = copia.createGraphics();
        g2.drawImage(original, 0, 0, null);
        g2.dispose();
        return copia;
    }

    private static void aplicarFiltroPersonalizado() {
        if (imagen == null) return;
        
        try {
            int size = Integer.parseInt(((String) sizeComboBox.getSelectedItem()).split("x")[0]);
            DefaultTableModel model = (DefaultTableModel) kernelTable.getModel();
            
            if (model.getRowCount() != size || model.getColumnCount() != size) {
                throw new Exception("El tamaño de la tabla no coincide con el seleccionado");
            }
            
            int[][] kernel = new int[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    Object value = model.getValueAt(i, j);
                    if (value == null || value.toString().trim().isEmpty()) {
                        throw new Exception("Celda vacía en la fila " + (i+1) + ", columna " + (j+1));
                    }
                    kernel[i][j] = Integer.parseInt(value.toString());
                }
            }

            imagenModificada = copiarImagen(imagen);
            aplicarConvolucion(imagenModificada, kernel);
            actualizarImagen();

        } catch (Exception ex) {
            mostrarError("Error en el kernel: " + ex.getMessage());
        }
    }

    private static void aplicarNegativo(BufferedImage img) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                Color c = new Color(img.getRGB(x, y));
                img.setRGB(x, y, new Color(
                    255 - c.getRed(),
                    255 - c.getGreen(),
                    255 - c.getBlue()
                ).getRGB());
            }
        }
    }

    private static void aplicarEscalaDeGrises(BufferedImage img) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                Color c = new Color(img.getRGB(x, y));
                int gris = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                img.setRGB(x, y, new Color(gris, gris, gris).getRGB());
            }
        }
    }

    private static void aplicarBrillo(BufferedImage img, int brillo) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                Color c = new Color(img.getRGB(x, y));
                int r = ajustarRango(c.getRed() + brillo);
                int g = ajustarRango(c.getGreen() + brillo);
                int b = ajustarRango(c.getBlue() + brillo);
                img.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
    }

    private static int ajustarRango(int valor) {
        return Math.min(255, Math.max(0, valor));
    }

    private static void aplicarConvolucion(BufferedImage img, int[][] kernel) {
        int w = img.getWidth(), h = img.getHeight();
        int m = kernel.length / 2;
        BufferedImage copia = copiarImagen(img);

        for (int y = m; y < h - m; y++) {
            for (int x = m; x < w - m; x++) {
                int rr = 0, gg = 0, bb = 0;
                
                for (int ky = -m; ky <= m; ky++) {
                    for (int kx = -m; kx <= m; kx++) {
                        Color c = new Color(copia.getRGB(x + kx, y + ky));
                        int peso = kernel[ky + m][kx + m];
                        
                        rr += c.getRed() * peso;
                        gg += c.getGreen() * peso;
                        bb += c.getBlue() * peso;
                    }
                }
                
                rr = ajustarRango(rr);
                gg = ajustarRango(gg);
                bb = ajustarRango(bb);
                
                img.setRGB(x, y, new Color(rr, gg, bb).getRGB());
            }
        }
    }

    private static void guardarImagen() {
        if (imagenModificada == null) return;
        
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                File archivo = fc.getSelectedFile();
                String nombre = archivo.getName();
                String ext = obtenerExtensionValida(nombre);
                
                if (ext == null) {
                    ext = "png";
                    archivo = new File(archivo.getAbsolutePath() + ".png");
                }
                
                if (!ImageIO.write(imagenModificada, ext, archivo)) {
                    mostrarError("No se pudo guardar en formato: " + ext);
                }
            } catch (IOException ex) {
                mostrarError("Error al guardar: " + ex.getMessage());
            }
        }
    }

    private static String obtenerExtensionValida(String nombreArchivo) {
        int i = nombreArchivo.lastIndexOf('.');
        if (i > 0 && i < nombreArchivo.length() - 1) {
            String ext = nombreArchivo.substring(i + 1).toLowerCase();
            for (String formato : ImageIO.getWriterFormatNames()) {
                if (formato.equalsIgnoreCase(ext)) return ext;
            }
        }
        return null;
    }

    private static void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(frame, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}