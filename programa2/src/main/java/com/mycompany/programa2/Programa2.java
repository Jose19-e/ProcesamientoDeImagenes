package com.mycompany.programa2;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Programa2 {
    public static void main(String[] args) {
        JFileChooser selector = new JFileChooser();
        if (selector.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                File archivo = selector.getSelectedFile();
                BufferedImage imagen = ImageIO.read(archivo);
                
                // Crear componentes
                JLabel imageLabel = new JLabel(new ImageIcon(imagen));
                JScrollPane scrollPane = new JScrollPane(imageLabel);
                
                // Calcular información
                int ancho = imagen.getWidth();
                int alto = imagen.getHeight();
                long pixeles = (long) ancho * alto;
                
                // Crear ventana
                JFrame frame = new JFrame("Visor de Imagen");
                frame.setLayout(new BorderLayout());
                frame.add(scrollPane, BorderLayout.CENTER);
                
                // Panel de información
                JPanel infoPanel = new JPanel(new GridLayout(0, 1));
                infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                infoPanel.add(new JLabel("Archivo: " + archivo.getName()));
                infoPanel.add(new JLabel("Ancho: " + ancho + " px"));
                infoPanel.add(new JLabel("Alto: " + alto + " px"));
                infoPanel.add(new JLabel("Total píxeles: " + String.format("%,d", pixeles)));
                
                frame.add(infoPanel, BorderLayout.SOUTH);
                frame.setSize(600, 500);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error al cargar la imagen", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}