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

        // Panel superior
        infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pixelInfoLabel = new JLabel("Posición: (0,0) - RGB: (0,0,0)");
        pixelInfoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        frame.add(crearHeaderPanel(), BorderLayout.NORTH);

        // Panel de controles
        frame.add(crearPanelControles(), BorderLayout.EAST);

        // Botones
        frame.add(crearPanelBotones(), BorderLayout.SOUTH);

        // Área de imagen
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
            filterComboBox.addActionListener(e -> aplicarFiltro());
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
            brightnessSlider.addChangeListener(e -> aplicarBrillo());
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
                
                imagenModificada = null;
                zoom = 1.0f;
                actualizarImagen();
                actualizarInfo(archivoActual);
                centrarImagen();
                resetearControles();
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
        BufferedImage img = imagenModificada != null ? imagenModificada : imagen;
        Image imgEscalada = img.getScaledInstance(
            (int)(img.getWidth() * zoom),
            (int)(img.getHeight() * zoom),
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

    private static void aplicarFiltro() {
        if (imagen == null) return;
        
        String filtro = (String) filterComboBox.getSelectedItem();
        int brilloActual = brightnessSlider.getValue();
        
        if (filtro.equals("Original")) {
            imagenModificada = null;
        } else {
            // Crear nueva imagen solo si es necesario
            if (imagenModificada == null || 
                imagenModificada.getWidth() != imagen.getWidth() || 
                imagenModificada.getHeight() != imagen.getHeight()) {
                
                imagenModificada = new BufferedImage(
                    imagen.getWidth(), 
                    imagen.getHeight(), 
                    BufferedImage.TYPE_INT_RGB);
            }
            
            // Copiar imagen original
            Graphics2D g = imagenModificada.createGraphics();
            g.drawImage(imagen, 0, 0, null);
            g.dispose();

            // Aplicar filtro
            switch (filtro) {
                case "Negativo":
                    aplicarNegativo();
                    break;
                case "Escala de Grises":
                    aplicarEscalaDeGrises();
                    break;
                case "Borde":
                    aplicarConvolucion(new int[][]{{-1,-1,-1}, {-1,8,-1}, {-1,-1,-1}});
                    break;
                case "Sobel H":
                    aplicarConvolucion(new int[][]{{-1,-2,-1}, {0,0,0}, {1,2,1}});
                    break;
                case "Sobel V":
                    aplicarConvolucion(new int[][]{{-1,0,1}, {-2,0,2}, {-1,0,1}});
                    break;
                case "Prewitt H":
                    aplicarConvolucion(new int[][]{{-1,-1,-1}, {0,0,0}, {1,1,1}});
                    break;
                case "Prewitt V":
                    aplicarConvolucion(new int[][]{{-1,0,1}, {-1,0,1}, {-1,0,1}});
                    break;
                case "Laplaciano":
                    aplicarConvolucion(new int[][]{{0,-1,0}, {-1,4,-1}, {0,-1,0}});
                    break;
            }
        }
        
        // Reaplicar brillo si es necesario
        if (brilloActual != 0) {
            aplicarBrillo();
        } else {
            actualizarImagen();
        }
    }

    private static void aplicarNegativo() {
        int width = imagenModificada.getWidth();
        int height = imagenModificada.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenModificada.getRGB(x, y);
                imagenModificada.setRGB(x, y, ~rgb & 0x00FFFFFF);
            }
        }
    }

    private static void aplicarEscalaDeGrises() {
        int width = imagenModificada.getWidth();
        int height = imagenModificada.getHeight();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(imagenModificada.getRGB(x, y));
                int gris = (int)(color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114);
                imagenModificada.setRGB(x, y, new Color(gris, gris, gris).getRGB());
            }
        }
    }

    private static void aplicarConvolucion(int[][] kernel) {
        int width = imagenModificada.getWidth();
        int height = imagenModificada.getHeight();
        BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int r = 0, g = 0, b = 0;
                
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        Color color = new Color(imagenModificada.getRGB(x + kx, y + ky));
                        r += color.getRed() * kernel[ky + 1][kx + 1];
                        g += color.getGreen() * kernel[ky + 1][kx + 1];
                        b += color.getBlue() * kernel[ky + 1][kx + 1];
                    }
                }
                
                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));
                
                temp.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        
        // Copiar resultado
        Graphics2D g = imagenModificada.createGraphics();
        g.drawImage(temp, 0, 0, null);
        g.dispose();
    }

    private static void aplicarBrillo() {
        if (imagen == null) return;
        
        BufferedImage imagenBase = (imagenModificada != null) ? imagenModificada : imagen;
        float factor = 1 + (brightnessSlider.getValue() / 100f);
        
        // Crear nueva imagen para el brillo
        imagenModificada = new BufferedImage(
            imagenBase.getWidth(), 
            imagenBase.getHeight(), 
            BufferedImage.TYPE_INT_RGB);
        
        // Aplicar brillo
        for (int y = 0; y < imagenBase.getHeight(); y++) {
            for (int x = 0; x < imagenBase.getWidth(); x++) {
                Color color = new Color(imagenBase.getRGB(x, y));
                int r = ajustarValor((int)(color.getRed() * factor));
                int g = ajustarValor((int)(color.getGreen() * factor));
                int b = ajustarValor((int)(color.getBlue() * factor));
                imagenModificada.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        
        actualizarImagen();
    }

    private static int ajustarValor(int valor) {
        return Math.max(0, Math.min(255, valor));
    }

    private static void guardarImagen() {
        if (imagenModificada == null) {
            JOptionPane.showMessageDialog(frame, 
                "No hay imagen modificada para guardar", 
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar imagen modificada");
        
        String nombreOriginal = archivoActual.getName();
        String nombreBase = nombreOriginal.substring(0, nombreOriginal.lastIndexOf('.'));
        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));
        
        // Generar nombre único
        File archivoPropuesto = new File(nombreBase + "_modificado" + extension);
        int contador = 1;
        while (archivoPropuesto.exists()) {
            archivoPropuesto = new File(nombreBase + "_modificado_" + (contador++) + extension);
        }
        fc.setSelectedFile(archivoPropuesto);

        if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File archivoGuardar = fc.getSelectedFile();
            String nombreArchivo = archivoGuardar.getName().toLowerCase();
            
            // Asegurar extensión
            if (!nombreArchivo.contains(".")) {
                archivoGuardar = new File(archivoGuardar.getAbsolutePath() + extension);
            }
            
            // Determinar formato
            String formato = extension.substring(1).toLowerCase();
            if (nombreArchivo.endsWith(".png")) formato = "png";
            else if (nombreArchivo.endsWith(".gif")) formato = "gif";
            else if (nombreArchivo.endsWith(".bmp")) formato = "bmp";

            try {
                ImageIO.write(imagenModificada, formato, archivoGuardar);
                JOptionPane.showMessageDialog(frame, 
                    "Imagen guardada como: " + archivoGuardar.getName(), 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, 
                    "Error al guardar: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}