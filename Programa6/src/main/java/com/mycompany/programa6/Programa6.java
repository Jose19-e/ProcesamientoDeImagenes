/**
 *
 * @author jose_
 */
package com.mycompany.programa6;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

public class Programa6 {
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
            frame = new JFrame("Programa6 - Visor de Imágenes Avanzado");
            frame.setLayout(new BorderLayout());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Panel superior
            infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pixelInfoLabel = new JLabel("Posición: (0,0) - RGB: (0,0,0)");
            pixelInfoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            frame.add(new JPanel(new GridLayout(2, 1)) {{
                add(infoPanel);
                add(pixelInfoLabel);
            }}, BorderLayout.NORTH);

            // Panel de controles
            frame.add(new JPanel(new GridLayout(2, 1)) {{
                add(new JPanel() {{
                    add(new JLabel("Filtro: "));
                    add(filterComboBox = new JComboBox<>(new String[]{"Original", "Negativo", "Escala de Grises"}));
                    filterComboBox.addActionListener(e -> aplicarFiltro());
                }});
                add(new JPanel() {{
                    add(new JLabel("Brillo: "));
                    add(brightnessSlider = new JSlider(-100, 100, 0));
                    brightnessSlider.setMajorTickSpacing(50);
                    brightnessSlider.setMinorTickSpacing(10);
                    brightnessSlider.setPaintTicks(true);
                    brightnessSlider.setPaintLabels(true);
                    brightnessSlider.addChangeListener(e -> aplicarBrillo());
                }});
            }}, BorderLayout.EAST);

            // Botones
            frame.add(new JPanel() {{
                add(new JButton("Abrir Imagen") {{ addActionListener(e -> abrirImagen()); }});
                add(new JButton("Zoom In (+)") {{ addActionListener(e -> aplicarZoom(1.25f)); }});
                add(new JButton("Zoom Out (-)") {{ addActionListener(e -> aplicarZoom(0.8f)); }});
                add(new JButton("Reset Zoom") {{ addActionListener(e -> resetearZoom()); }});
                add(new JButton("Guardar Imagen") {{ addActionListener(e -> guardarImagen()); }});
            }}, BorderLayout.SOUTH);

            // Área de imagen
            imageLabel = new JLabel("", SwingConstants.CENTER);
            imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    if (imagen != null) {
                        BufferedImage img = imagenModificada != null ? imagenModificada : imagen;
                        Point p = calcularPosicionPixel(e.getPoint(), img);
                        if (p.x >= 0 && p.y >= 0 && p.x < img.getWidth() && p.y < img.getHeight()) {
                            Color c = new Color(img.getRGB(p.x, p.y));
                            pixelInfoLabel.setText(String.format("Posición: (%d,%d) - RGB: (%d,%d,%d)", p.x, p.y, c.getRed(), c.getGreen(), c.getBlue()));
                        } else pixelInfoLabel.setText("Posición: (-,-) - RGB: (0,0,0)");
                    }
                }
            });
            frame.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
            frame.setSize(1000, 700);
            frame.setVisible(true);
        });
    }

    private static Point calcularPosicionPixel(Point mousePos, BufferedImage img) {
        int labelW = imageLabel.getWidth(), labelH = imageLabel.getHeight();
        int imgW = (int)(img.getWidth() * zoom), imgH = (int)(img.getHeight() * zoom);
        int offsetX = (labelW - imgW) / 2, offsetY = (labelH - imgH) / 2;
        return new Point((int)((mousePos.x - offsetX) / zoom), (int)((mousePos.y - offsetY) / zoom));
    }

    private static void abrirImagen() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) try {
            archivoActual = fc.getSelectedFile();
            imagen = ImageIO.read(archivoActual);
            if (imagen == null) throw new IOException("Formato no soportado");
            imagenModificada = null;
            zoom = 1.0f;
            actualizarImagen();
            actualizarInfo(archivoActual);
            centrarImagen();
            resetearControles();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error al cargar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            actualizarInfo(archivoActual); // Actualizamos la información del zoom
            centrarImagen();
        }
    }

    private static void resetearZoom() {
        if (imagen != null) {
            zoom = 1.0f;
            actualizarImagen();
            actualizarInfo(archivoActual); // Actualizamos la información del zoom
            centrarImagen();
        }
    }

    private static void actualizarImagen() {
        BufferedImage img = imagenModificada != null ? imagenModificada : imagen;
        imageLabel.setIcon(new ImageIcon(img.getScaledInstance((int)(img.getWidth() * zoom), (int)(img.getHeight() * zoom), Image.SCALE_SMOOTH)));
        frame.revalidate();
    }

    private static void centrarImagen() {
        SwingUtilities.invokeLater(() -> {
            JViewport vp = ((JScrollPane)frame.getContentPane().getComponent(2)).getViewport();
            Dimension vs = vp.getExtentSize(), is = imageLabel.getPreferredSize();
            vp.setViewPosition(new Point(Math.max(0, (is.width - vs.width) / 2), Math.max(0, (is.height - vs.height) / 2)));
        });
    }

    private static void actualizarInfo(File f) {
        infoPanel.removeAll();
        infoPanel.add(new JLabel("Archivo: " + f.getName() + " | "));
        infoPanel.add(new JLabel("Tamaño: " + imagen.getWidth() + "×" + imagen.getHeight() + " px | "));
        infoPanel.add(new JLabel("Píxeles: " + String.format("%,d", (long)imagen.getWidth() * imagen.getHeight()) + " | "));
        infoPanel.add(new JLabel("Zoom: " + String.format("%.0f%%", zoom * 100))); // Esta línea muestra el porcentaje de zoom
        infoPanel.revalidate();
    }

    private static void aplicarFiltro() {
        if (imagen == null) return;
        String filtro = (String)filterComboBox.getSelectedItem();
        if (filtro.equals("Original")) imagenModificada = null;
        else {
            imagenModificada = new BufferedImage(imagen.getWidth(), imagen.getHeight(), imagen.getType());
            Graphics2D g = imagenModificada.createGraphics();
            g.drawImage(imagen, 0, 0, null);
            g.dispose();
            if (filtro.equals("Negativo")) aplicarNegativo();
            else if (filtro.equals("Escala de Grises")) aplicarEscalaDeGrises();
        }
        actualizarImagen();
    }

    private static void aplicarNegativo() {
        for (int y = 0; y < imagenModificada.getHeight(); y++)
            for (int x = 0; x < imagenModificada.getWidth(); x++) {
                Color c = new Color(imagenModificada.getRGB(x, y));
                imagenModificada.setRGB(x, y, new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()).getRGB());
            }
    }

    private static void aplicarEscalaDeGrises() {
        for (int y = 0; y < imagenModificada.getHeight(); y++)
            for (int x = 0; x < imagenModificada.getWidth(); x++) {
                Color c = new Color(imagenModificada.getRGB(x, y));
                int gris = (int)(c.getRed() * 0.299 + c.getGreen() * 0.587 + c.getBlue() * 0.114);
                imagenModificada.setRGB(x, y, new Color(gris, gris, gris).getRGB());
            }
    }

    private static void aplicarBrillo() {
        if (imagen == null) return;
        float factor = 1 + (brightnessSlider.getValue() / 100f);
        imagenModificada = new BufferedImage(imagen.getWidth(), imagen.getHeight(), imagen.getType());
        Graphics2D g = imagenModificada.createGraphics();
        g.drawImage(imagen, 0, 0, null);
        g.dispose();
        for (int y = 0; y < imagenModificada.getHeight(); y++)
            for (int x = 0; x < imagenModificada.getWidth(); x++) {
                Color c = new Color(imagenModificada.getRGB(x, y));
                int r = Math.max(0, Math.min(255, (int)(c.getRed() * factor)));
                int gb = Math.max(0, Math.min(255, (int)(c.getGreen() * factor)));
                int b = Math.max(0, Math.min(255, (int)(c.getBlue() * factor)));
                imagenModificada.setRGB(x, y, new Color(r, gb, b).getRGB());
            }
        actualizarImagen();
    }

    private static void guardarImagen() {
        if (imagenModificada == null) {
            JOptionPane.showMessageDialog(frame, "No hay imagen modificada para guardar", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar imagen modificada");

        // Configurar nombre por defecto con sufijo numérico si ya existe
        String nombreOriginal = archivoActual.getName();
        String nombreBase = nombreOriginal.substring(0, nombreOriginal.lastIndexOf('.'));
        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));

        // Buscar un nombre disponible
        File archivoPropuesto = new File(nombreBase + "_modificado" + extension);
        int contador = 1;
        while (archivoPropuesto.exists()) {
            archivoPropuesto = new File(nombreBase + "_modificado_" + contador + extension);
            contador++;
        }
        fc.setSelectedFile(archivoPropuesto);

        if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File archivoGuardar = fc.getSelectedFile();

            // Asegurar que tenga extensión
            String nombreArchivo = archivoGuardar.getName();
            if (!nombreArchivo.contains(".")) {
                archivoGuardar = new File(archivoGuardar.getAbsolutePath() + extension);
            }

            // Determinar formato basado en extensión (usamos el mismo que la original por defecto)
            String formato = extension.substring(1).toLowerCase();
            if (nombreArchivo.toLowerCase().endsWith(".png")) {
                formato = "png";
            } else if (nombreArchivo.toLowerCase().endsWith(".gif")) {
                formato = "gif";
            } else if (nombreArchivo.toLowerCase().endsWith(".bmp")) {
                formato = "bmp";
            }

            try {
                ImageIO.write(imagenModificada, formato, archivoGuardar);
                JOptionPane.showMessageDialog(frame, 
                    "Imagen guardada exitosamente como:\n" + archivoGuardar.getName(), 
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, 
                    "Error al guardar la imagen: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}