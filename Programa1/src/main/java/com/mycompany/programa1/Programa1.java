package com.mycompany.programa1;

/**
 *
 * @author jose_
 */
// Programa 1 PDI
// Este programa únicamente abre una imagen que se encuentra en el disco duro en raíz
// de C:. Puedes cambiar la ruta. También muestra información de las diagonales invertidas
// indirectamente. Las diagonales se utilizarán con '/' en lugar de '\'.
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;

public class Programa1 extends Frame {
    public Image imagen;
    int Ancho, Largo;
    String sAncho, sLargo;

    public Programa1() {
        this.setTitle("Aplicación 1 PDI");
        this.setSize(600, 600);
        this.setVisible(true);

        imagen = Toolkit.getDefaultToolkit().getImage("C:/ferrari.jpg");

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        new Programa1();
    }

    public void paint(Graphics g) {
        NumberFormat convertir = NumberFormat.getCurrencyInstance();
        Largo = imagen.getHeight(this);
        Ancho = imagen.getWidth(this);
        g.drawImage(imagen, 10, 10, Ancho, Largo, this);
    }
}

