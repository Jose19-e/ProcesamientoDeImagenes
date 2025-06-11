

/**
 *
 * @author jose_
 */
package com.mycompany.programa4;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

public class Programa4 {
    private static float zoom = 1.0f;
    private static BufferedImage imagen;
    private static JLabel imageLabel;
    private static JFrame frame;
    private static JPanel infoPanel;
    private static JScrollPane scrollPane;
    private static JLabel pixelInfoLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Visor de Imagen con Inspector de Píxeles");
            frame.setLayout(new BorderLayout());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Panel superior con información de la imagen y píxel
            JPanel headerPanel = new JPanel(new GridLayout(2, 1));
            infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pixelInfoLabel = new JLabel("Posición: (0,0) - RGB: (0,0,0)");
            pixelInfoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            headerPanel.add(infoPanel);
            headerPanel.add(pixelInfoLabel);
            frame.add(headerPanel, BorderLayout.NORTH);

            // Panel de controles
            JPanel controlPanel = new JPanel();
            JButton btnAbrir = new JButton("Abrir Imagen");
            btnAbrir.addActionListener(e -> abrirImagen());

            JButton btnZoomIn = new JButton("Zoom In (+)");
            JButton btnZoomOut = new JButton("Zoom Out (-)");
            JButton btnReset = new JButton("Reset Zoom");

            btnZoomIn.addActionListener(e -> aplicarZoom(1.25f));
            btnZoomOut.addActionListener(e -> aplicarZoom(0.8f));
            btnReset.addActionListener(e -> resetearZoom());

            controlPanel.add(btnAbrir);
            controlPanel.add(btnZoomIn);
            controlPanel.add(btnZoomOut);
            controlPanel.add(btnReset);

            frame.add(controlPanel, BorderLayout.SOUTH);

            // Área de imagen con JScrollPane
            imageLabel = new JLabel("", SwingConstants.CENTER);
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            scrollPane = new JScrollPane(imageLabel);

            // MouseMotionListener para inspección de píxeles
            imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    if (imagen != null) {
                        int labelWidth = imageLabel.getWidth();
                        int labelHeight = imageLabel.getHeight();

                        int imageWidth = (int)(imagen.getWidth() * zoom);
                        int imageHeight = (int)(imagen.getHeight() * zoom);

                        int offsetX = (labelWidth - imageWidth) / 2;
                        int offsetY = (labelHeight - imageHeight) / 2;

                        int mouseX = e.getX() - offsetX;
                        int mouseY = e.getY() - offsetY;

                        int imgX = (int)(mouseX / zoom);
                        int imgY = (int)(mouseY / zoom);

                        if (imgX >= 0 && imgX < imagen.getWidth() &&
                            imgY >= 0 && imgY < imagen.getHeight()) {
                            try {
                                int[] pixels = new int[1];
                                PixelGrabber grabber = new PixelGrabber(imagen, imgX, imgY, 1, 1, pixels, 0, 1);
                                if (grabber.grabPixels()) {
                                    int pixel = pixels[0];
                                    int red = (pixel >> 16) & 0xff;
                                    int green = (pixel >> 8) & 0xff;
                                    int blue = pixel & 0xff;

                                    pixelInfoLabel.setText(String.format(
                                        "Posición: (%d,%d) - RGB: (%d,%d,%d)",
                                        imgX, imgY, red, green, blue));
                                }
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            pixelInfoLabel.setText("Posición: (-,-) - RGB: (0,0,0)");
                        }
                    }
                }
            });

            frame.add(scrollPane, BorderLayout.CENTER);
            frame.setSize(800, 600);
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
            JViewport viewport = scrollPane.getViewport();
            Dimension viewSize = viewport.getExtentSize();
            Dimension imageSize = imageLabel.getPreferredSize();

            int x = (imageSize.width - viewSize.width) / 2;
            int y = (imageSize.height - viewSize.height) / 2;

            x = Math.max(0, x);
            y = Math.max(0, y);

            viewport.setViewPosition(new Point(x, y));
        });
    }

    private static void actualizarInfo(File archivo) {
        infoPanel.removeAll();

        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();
        long pixeles = (long) ancho * alto;

        infoPanel.add(new JLabel("Archivo: " + archivo.getName() + " | "));
        infoPanel.add(new JLabel("Tamaño: " + ancho + "×" + alto + " px | "));
        infoPanel.add(new JLabel("Píxeles: " + String.format("%,d", pixeles)));

        infoPanel.revalidate();
        infoPanel.repaint();
    }
}
