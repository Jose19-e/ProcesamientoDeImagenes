/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

/**
 *
 * @author jose_
 */
package com.mycompany.programa7;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

public class Programa7 {
    private static float zoom = 1.0f;
    private static BufferedImage imagen, imagenModificada;
    private static JLabel imageLabel, pixelInfoLabel;
    private static JFrame frame;
    private static JPanel infoPanel;
    private static JComboBox<String> filterComboBox;
    private static JSlider brightnessSlider;
    private static File archivoActual;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configurarInterfaz();
            frame.setSize(1000, 700);
            frame.setVisible(true);
        });
    }

    private static void configurarInterfaz() {
        frame = new JFrame("Programa7 - Procesamiento de Imágenes");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pixelInfoLabel = new JLabel("Posición: (0,0) - RGB: (0,0,0)");
        pixelInfoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        frame.add(crearHeaderPanel(), BorderLayout.NORTH);
        frame.add(crearPanelControles(), BorderLayout.EAST);
        frame.add(crearPanelBotones(), BorderLayout.SOUTH);

        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                actualizarInfoPixel(e.getPoint());
            }
        });
        frame.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
    }

    private static JPanel crearHeaderPanel() {
        return new JPanel(new GridLayout(2, 1)) {{
            add(infoPanel);
            add(pixelInfoLabel);
        }};
    }

    private static JPanel crearPanelControles() {
        return new JPanel(new GridLayout(2, 1)) {{
            add(crearPanelFiltros());
            add(crearPanelBrillo());
        }};
    }

    private static JPanel crearPanelFiltros() {
        return new JPanel() {{
            add(new JLabel("Filtro: "));
            add(filterComboBox = new JComboBox<>(new String[]{
                "Original", "Negativo", "Escala de Grises",
                "Borde", "Sobel H", "Sobel V",
                "Prewitt H", "Prewitt V", "Laplaciano"
            }));
            filterComboBox.addActionListener(e -> aplicarFiltrosYBrillo());
        }};
    }

    private static JPanel crearPanelBrillo() {
        return new JPanel() {{
            add(new JLabel("Brillo: "));
            add(brightnessSlider = new JSlider(-100, 100, 0));
            brightnessSlider.setMajorTickSpacing(50);
            brightnessSlider.setMinorTickSpacing(10);
            brightnessSlider.setPaintTicks(true);
            brightnessSlider.setPaintLabels(true);
            brightnessSlider.addChangeListener(e -> aplicarFiltrosYBrillo());
        }};
    }

    private static JPanel crearPanelBotones() {
        return new JPanel() {{
            add(new JButton("Abrir Imagen") {{ addActionListener(e -> abrirImagen()); }});
            add(new JButton("Zoom In (+)") {{ addActionListener(e -> aplicarZoom(1.25f)); }});
            add(new JButton("Zoom Out (-)") {{ addActionListener(e -> aplicarZoom(0.8f)); }});
            add(new JButton("Reset Zoom") {{ addActionListener(e -> resetearZoom()); }});
            add(new JButton("Guardar") {{ addActionListener(e -> guardarImagen()); }});
        }};
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

        return new Point(
            (int)((mousePos.x - offsetX) / zoom),
            (int)((mousePos.y - offsetY) / zoom)
        );
    }

    private static void abrirImagen() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                archivoActual = fc.getSelectedFile();
                imagen = ImageIO.read(archivoActual);

                if (imagen == null) {
                    throw new IOException("Formato no soportado");
                }

                zoom = 1.0f;
                resetearControles();
                aplicarFiltrosYBrillo();
                actualizarInfo(archivoActual);
                centrarImagen();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Error al cargar: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void resetearControles() {
        filterComboBox.setSelectedIndex(0);
        brightnessSlider.setValue(0);
    }

    private static void aplicarZoom(float factor) {
        if (imagen != null) {
            zoom *= factor;
            actualizarImagen();
            actualizarInfo(archivoActual);
            centrarImagen();
        }
    }

    private static void resetearZoom() {
        if (imagen != null) {
            zoom = 1.0f;
            actualizarImagen();
            actualizarInfo(archivoActual);
            centrarImagen();
        }
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
        SwingUtilities.invokeLater(() -> {
            JViewport viewport = ((JScrollPane)frame.getContentPane().getComponent(2)).getViewport();
            Dimension viewSize = viewport.getExtentSize();
            Dimension imageSize = imageLabel.getPreferredSize();

            int x = Math.max(0, (imageSize.width - viewSize.width) / 2);
            int y = Math.max(0, (imageSize.height - viewSize.height) / 2);

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

        BufferedImage resultado = new BufferedImage(imagen.getWidth(), imagen.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resultado.createGraphics();
        g.drawImage(imagen, 0, 0, null);
        g.dispose();

        String filtro = (String) filterComboBox.getSelectedItem();

        switch (filtro) {
            case "Negativo": aplicarNegativo(resultado); break;
            case "Escala de Grises": aplicarEscalaDeGrises(resultado); break;
            case "Borde": aplicarConvolucion(resultado, new int[][]{{-1,-1,-1},{-1,8,-1},{-1,-1,-1}}); break;
            case "Sobel H": aplicarConvolucion(resultado, new int[][]{{-1,-2,-1},{0,0,0},{1,2,1}}); break;
            case "Sobel V": aplicarConvolucion(resultado, new int[][]{{-1,0,1},{-2,0,2},{-1,0,1}}); break;
            case "Prewitt H": aplicarConvolucion(resultado, new int[][]{{-1,-1,-1},{0,0,0},{1,1,1}}); break;
            case "Prewitt V": aplicarConvolucion(resultado, new int[][]{{-1,0,1},{-1,0,1},{-1,0,1}}); break;
            case "Laplaciano": aplicarConvolucion(resultado, new int[][]{{0,-1,0},{-1,4,-1},{0,-1,0}}); break;
        }

        int brillo = brightnessSlider.getValue();
        if (brillo != 0) {
            aplicarBrillo(resultado, brillo);
        }

        imagenModificada = resultado;
        actualizarImagen();
    }

    private static void aplicarNegativo(BufferedImage img) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                Color c = new Color(img.getRGB(x, y));
                img.setRGB(x, y, new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()).getRGB());
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
                int r = Math.min(255, Math.max(0, c.getRed() + brillo));
                int g = Math.min(255, Math.max(0, c.getGreen() + brillo));
                int b = Math.min(255, Math.max(0, c.getBlue() + brillo));
                img.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
    }

    private static void aplicarConvolucion(BufferedImage img, int[][] kernel) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage copia = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int r = 0, g = 0, b = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        Color c = new Color(img.getRGB(x + kx, y + ky));
                        int peso = kernel[ky + 1][kx + 1];
                        r += c.getRed() * peso;
                        g += c.getGreen() * peso;
                        b += c.getBlue() * peso;
                    }
                }
                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));
                copia.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        Graphics2D g2 = img.createGraphics();
        g2.drawImage(copia, 0, 0, null);
        g2.dispose();
    }

    private static void guardarImagen() {
        if (imagenModificada == null) return;
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                File archivo = fc.getSelectedFile();

                // Obtener extensión del archivo
                String nombre = archivo.getName();
                String extension = null;
                int i = nombre.lastIndexOf('.');
                if (i > 0 && i < nombre.length() - 1) {
                    extension = nombre.substring(i + 1).toLowerCase();
                }

                // Si no tiene extensión o la extensión no es válida, asignar png por defecto
                String[] formatosValidos = ImageIO.getWriterFormatNames();
                boolean formatoValido = false;
                if (extension != null) {
                    for (String fmt : formatosValidos) {
                        if (fmt.equalsIgnoreCase(extension)) {
                            formatoValido = true;
                            break;
                        }
                    }
                }

                if (!formatoValido) {
                    extension = "png";
                    archivo = new File(archivo.getAbsolutePath() + "." + extension);
                }

                // Guardar imagen con el formato detectado o asignado
                boolean guardado = ImageIO.write(imagenModificada, extension, archivo);

                if (!guardado) {
                    // En caso que el formato no sea soportado por alguna razón, mostrar error
                    JOptionPane.showMessageDialog(frame,
                        "No se pudo guardar la imagen en el formato: " + extension,
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error al guardar la imagen: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
