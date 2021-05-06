/* Lab5 - 03.04.2020 - Wicha Maciej */
package com.company;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    public static void main(String[] args) {

        /* Tworzenie okienka*/
        JFrame frame = new JFrame("Kulki! ");                    // obiekt typu okienko
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);               // "x" będzie zamykał okno

        /* Przywiazanie naszej ramki z klas Panel */
        frame.getContentPane().add(new Panel());

        /* Definicja podstawowych elementow */
        frame.setPreferredSize(new Dimension(600, 600));      // ustawienie podstawowego rozmiaru okna
        frame.pack();                                                       // dopasowanie elementow do okna
        frame.setVisible(true);                                             // ustawienie widocznosci okna


    }
}
