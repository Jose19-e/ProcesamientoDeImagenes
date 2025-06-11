package com.mycompany.programa3;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Programa3 {
    private static float zoom = 1.0f;
    private static BufferedImage imagen;
    private static JLabel imageLabel;
    private static JFrame frame;
    private static JPanel infoPanel;
    private static JScrollPane scrollPane;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Visor de Imagen");
            frame.setLayout(new BorderLayout());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // Configurar panel de información
            infoPanel = new JPanel(new GridLayout(0, 1));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            frame.add(infoPanel, BorderLayout.SOUTH);
            
            // Panel de controles
            JPanel controlPanel = new JPanel();
            JButton btnAbrir = new JButton("Abrir Imagen");
            btnAbrir.addActionListener(e -> abrirImagen());
            controlPanel.add(btnAbrir);
            
            // Botones de zoom
            JButton btnZoomIn = new JButton("Zoom In (+)");
            JButton btnZoomOut = new JButton("Zoom Out (-)");
            JButton btnReset = new JButton("Reset Zoom");
            
            btnZoomIn.addActionListener(e -> aplicarZoom(1.25f));
            btnZoomOut.addActionListener(e -> aplicarZoom(0.8f));
            btnReset.addActionListener(e -> resetearZoom());
            
            controlPanel.add(btnZoomIn);
            controlPanel.add(btnZoomOut);
            controlPanel.add(btnReset);
            
            frame.add(controlPanel, BorderLayout.NORTH);
            
            // Configuración del área de imagen con centrado
            imageLabel = new JLabel("", SwingConstants.CENTER);
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            scrollPane = new JScrollPane(imageLabel);
            scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
            frame.add(scrollPane, BorderLayout.CENTER);
            
            frame.setSize(600, 500);
            frame.setVisible(true);
        });
    }

    private static void abrirImagen() {
        JFileChooser selector = new JFileChooser();
        if (selector.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                File archivo = selector.getSelectedFile();
                imagen = ImageIO.read(archivo);
                
                if (imagen == null) {
                    throw new IOException("Formato de imagen no soportado");
                }
                
                zoom = 1.0f;
                actualizarImagen();
                actualizarInfo(archivo);
                centrarImagen();
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, 
                    "Error al cargar la imagen: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void aplicarZoom(float factor) {
        if (imagen != null) {
            zoom *= factor;
            actualizarImagen();
            centrarImagen();
        }
    }

    private static void resetearZoom() {
        if (imagen != null) {
            zoom = 1.0f;
            actualizarImagen();
            centrarImagen();
        }
    }

    private static void actualizarImagen() {
        Image imgEscalada = imagen.getScaledInstance(
            (int)(imagen.getWidth() * zoom), 
            (int)(imagen.getHeight() * zoom), 
            Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(imgEscalada));
        frame.revalidate();
    }

    private static void centrarImagen() {
        SwingUtilities.invokeLater(() -> {
            // Obtener el área visible del JScrollPane
            JViewport viewport = scrollPane.getViewport();
            Dimension viewSize = viewport.getExtentSize();
            Dimension imageSize = imageLabel.getPreferredSize();
            
            // Calcular nueva posición centrada
            int x = (imageSize.width - viewSize.width) / 2;
            int y = (imageSize.height - viewSize.height) / 2;
            
            // Asegurarse que las coordenadas no sean negativas
            x = Math.max(0, x);
            y = Math.max(0, y);
            
            // Establecer la posición del viewport
            viewport.setViewPosition(new Point(x, y));
        });
    }

    private static void actualizarInfo(File archivo) {
        infoPanel.removeAll();
        
        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();
        long pixeles = (long) ancho * alto;
        
        infoPanel.add(new JLabel("Archivo: " + archivo.getName()));
        infoPanel.add(new JLabel("Ancho original: " + ancho + " px"));
        infoPanel.add(new JLabel("Alto original: " + alto + " px"));
        infoPanel.add(new JLabel("Total píxeles: " + String.format("%,d", pixeles)));
        
        infoPanel.revalidate();
        infoPanel.repaint();
    }
}