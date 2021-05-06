/* Kulki z mapą zderzeń - Wicha Maciej */
package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;


public class Panel extends JPanel {

    /* Kontener (kolekcja) na kulki */
    private ArrayList<Kula> listaKul;
    private int size = 20;                                                                                              // rozmiar tworzonych kulek

    /* Animacja ruchu kulek - timer */
    private Timer timer;
    private final int DELAY = 33;                                                                                       // dla 30fps - 33, d;a 60fps 16;

    /*** *** *** *** *** *** *** *** *** *** MAPA ZDERZEŃ *** *** *** *** *** *** *** *** *** ***/
    /* Lista kolizji */
    private ArrayList<Kolizje> listaKolizji = new ArrayList<>();     // Lista kolizji
    Kolizje kolizja;                                                 // Obiekt z informacjami o kolizji (x1, x1, size1) i (x2, y2, size2)
    JFrame mapa = null;                                              // PUSTY wskaznik do obiektu nowego okna
    Plik plik;                                                       // Uchwyt do pliku

    {
        try {
            plik = new Plik("dane.txt");                    // Przypisanie uchwytu do pliku
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    final int bufor = 20;                                         // co ile zapisywac
    int liczbaKolizji = 0;                                        // licznik kolizji
    int max_kul = 20;                                             // od ilu kul zapisywac kolizje w paczkach

    /*** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***/

    /* Konstruktor klasy Panel */
    public Panel() {
        listaKul = new ArrayList<>();                                                                                   // Inicjalizacja listy
        setBackground(Color.BLACK);                                                                                     // ustawienie koloru tla

        /* Dodanie interfejsow */
        addMouseListener(new Listener());
        addMouseMotionListener(new Listener());
        addMouseWheelListener(new Listener());

        /* Inicjalizacja i wystarotowanie timera */
        timer = new Timer(DELAY, new Listener());
        timer.start();
    }

    /* Rysowanie elementu na ekranie */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        /* Rysowanie wszystkich kulek z listy */
        for (Kula k : listaKul) {
            g.setColor(k.kolor);                                                                                        // kolor "pedzla" bedzie kolorem naszej kuli
            g.fillOval(k.x, k.y, k.size, k.size);                                                                       // wyrysowanie kola w osrodku x, y i "dlugosci i wyokosci kola" size
        }

        /* Licznik kul */
        g.setColor(Color.WHITE);                                                                                        // zmiana koloru napisy
        g.drawString("Liczba kulek:", 10, 15);
        g.drawString(Integer.toString(listaKul.size()), 100, 15);                                                //co i gdzie wyswietlic (x, y)
        g.drawString("Rozmiar: ", 10, 30);                                                                   // aktualny ozmiar kulek
        g.drawString(Integer.toString(size), 100, 30);
    }

    /* Obsluga myszki poprzez implementacje interfejsu */
    protected class Listener implements MouseListener, ActionListener, MouseMotionListener, MouseWheelListener {        // Lab 6

        @Override
        public void mouseClicked(MouseEvent mouseEvent) {

        }

        /* Tworzenie kulek po kliknieciu myszka */
        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            listaKul.add(new Kula(mouseEvent.getX(), mouseEvent.getY(), size));                                         // (dodanie kuli do listy) w miejscu kursora postawnie kula
            repaint();                                                                                                  // nie bezposrednie wywolanie metody paintComponent, narysowanie kulki po kadym jej dodaniu
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
        }

        /* Kursor wewnatrz - animacja ruchoma */
        @Override
        public void mouseEntered(MouseEvent mouseEvent) {
            timer.start();
            /*** *** *** *** *** *** *** *** *** *** MAPA ZDERZEŃ *** *** *** *** *** *** *** *** *** ***/
            if (mapa != null) {                                 // Okno musi zostac UTWORZONE
                if (mapa.isActive()) {                          // tylko gdy jest aktywne (uruchomione)
                    mapa.dispose();                             // Zamknij tylko okno z mapa kolizji
                    System.out.println("Zamykam okno kolizji.");
                }
            }
            /*** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***/
        }

        /* Kursor na zewnatrz - animacja zatrzymana*/
        @Override
        public void mouseExited(MouseEvent mouseEvent) {
            timer.stop();
            /*** *** *** *** *** *** *** *** *** *** MAPA ZDERZEŃ *** *** *** *** *** *** *** *** *** ***/
            System.out.println("Tworzę nowe okno.");
            if (listaKolizji.size() != 0) {                                     // Nie otwieraj okna gdy nie zarejestrowano kolizji
                mapa = new JFrame("Mapa zderzeń");                         // Utworzenie okna
                mapa.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                try {
                    mapa.getContentPane().add(new Mapa(mapa, plik));            // utworzenie okna z uzyciem klasy Mapa -> przekazuje uchwyt do pliku i wskaznik do okna
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                mapa.setPreferredSize(new Dimension(600, 600));
                mapa.setLocation(800, 0);                                // wyświetlenie okna obok
                mapa.pack();
                mapa.setVisible(true);
            }
            /*** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***/

        }

        /* Metoda z intefejsu ActionListener */
        @Override
        public void actionPerformed(ActionEvent actionEvent) {

            /* Zmiana pozycji dla kazdej kulki, co jakis czas */
            int i = 0;
            for (Kula k : listaKul) {
                k.update();
                k.kolizja(i);                                                                                           // Wykrywanie kolizji
                i++;                                                                                                    // inkrementacja numeru kulki
            }
            repaint();                                                                                                  // "odswiez ekran", wymaluj na nowo
        }

        /* Metody z interfejsu MouseMotionListener */
        @Override
        public void mouseDragged(MouseEvent mouseEvent) {
            listaKul.add(new Kula(mouseEvent.getX(), mouseEvent.getY(), size));                                         // kopia z funkcji klikniecia myszka

            /*  Usuwanie przylepionych kulek */
            int lastIndex = listaKul.size() - 1;                                                                        // Usunieci kulek zbyt blisko siebie
            Kula nowa = listaKul.get(lastIndex);
            Kula temp;
            double odleglosc, suma_promieni;

            for (int i = 0; i < lastIndex; i++) {
                temp = listaKul.get(i);
                odleglosc = Math.sqrt(Math.pow(nowa.x - temp.x, 2) + Math.pow(nowa.y - temp.y, 2));
                suma_promieni = nowa.promien + temp.promien;

                if (odleglosc <= suma_promieni + 2) {
                    //System.out.println(" --> " + i + ", " + lastIndex + ", ZA BLISKO");
                    listaKul.remove(lastIndex);
                    i = lastIndex;
                }
            }
            /* Oszukanie uzytkownika -> kulki sie zlepiaja ale uzytkownik tego nie widzi :D*/
            try {                                                                                                       // "zamrozenie ekranu na krotka chwile"
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            repaint();
        }

        /* Tutaj mozna dodac efekt podnoszenia kulek */
        @Override
        public void mouseMoved(MouseEvent mouseEvent) {

        }

        /* Metoda interfejsu MouseWheelMove - obsluga scrolla */
        @Override
        public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
            /* Pobranie ilosci "klikniec, przesuniec" scrolla i dodanie ich do obecnej wielkosci */
            size += mouseWheelEvent.getWheelRotation();
            if (size <= 0)
                size = 1;                                                                                    // zabezpieczenie przed ujemnymi wielkosciami kulek
        }
    }

    /* Klasa odpowiedzialna za tworzenie i obsluge kul */
    private class Kula {
        /* Deklarache zmiennych */
        public int x, y, size, xspeed, yspeed;                                                                          // wspolrzedne kulu, promien (rozmiar), predkosci poziome i pionowe obiektu
        public Color kolor;                                                                                             // kolor kuli
        private final int MAX_SPEED = 5;                                                                                // maksymalna predkosc kuli

        // Wartosci przydatne w pozniejszym etapie
        public double promien, sr_x, sr_y;

        /* Konstruktor klasy */
        public Kula(int x, int y, int size) {
            this.size = size;
            this.x = x;
            this.y = y;

            /* Wyliczenie podstawowych informacji o kuli */
            promien = (double) this.size / 2;
            sr_x = (double) x - promien;
            sr_y = (double) y - promien;
            //System.out.println("Size: " + this.size + ", X: " + this.x + ", Y: " + this.y + ",    promien: " + this.promien + ", sr_x: " + this.sr_x + ", sr_y: " + this.sr_y);     //Wydruk kontrolny na konsoli

            /* Wylosowanie koloru */
            kolor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());                     // wylosowanie koloru RGB

            /* Wylosowanie predkosci kuli */
            xspeed = (int) (Math.random() * MAX_SPEED * 2 - MAX_SPEED);                                                 //funkcja random losuje <0,1>
            yspeed = (int) (Math.random() * MAX_SPEED * 2 - MAX_SPEED);                                                 // my losujemy od <-MAX_SPEED, MAX_SPEED>

            /* Zapobiegniecie wylosowaniu nieruchomej kulki */
            if (xspeed == 0 && yspeed == 0) {
                Random losowanie = new Random();

                /* Wylosowanie predksoci dla x (dodatniej lub ujemnej) */
                if (Math.random() >= 0.5) {
                    xspeed = losowanie.nextInt(MAX_SPEED - 1) + 1;
                } else {
                    xspeed = (losowanie.nextInt(MAX_SPEED - 1) + 1) - (MAX_SPEED + 1);
                }

                /* Wylsoowanie predkosci dla y (dodatniej lub ujemnej) */
                if (Math.random() >= 0.5) {
                    yspeed = losowanie.nextInt(MAX_SPEED - 1) + 1;
                } else {
                    yspeed = (losowanie.nextInt(MAX_SPEED - 1) + 1) - (MAX_SPEED + 1);
                }
            }
        }

        /* Aktualizacja polozenia kuli */
        public void update() {
            x += xspeed;
            y += yspeed;
            sr_x += xspeed;
            sr_y += yspeed;

            // Wykrycie odbicia od ramki, i ewentualna korekta polozenia
            if (x <= 0) {
                xspeed *= (-1);
                x += 3;
                sr_x += 3;
            } else if (x >= getWidth() - this.size) {
                xspeed *= (-1);
                x -= 3;
                sr_x -= 3;
            }
            if (y <= 0) {
                yspeed *= (-1);
                y += 3;
                sr_y += 3;
            } else if (y >= getHeight() - this.size) {
                yspeed *= (-1);
                y -= 3;
                sr_y -= 3;
            }
        }

        /* Wykrywanie i reakcja na kolizje */
        public void kolizja(int numer) {
            Kula badana = listaKul.get(numer);              // LAB 6
            for (int i = numer + 1; i < listaKul.size(); i++) {                                                             // sprawdzamy tylko jeszcze nie sprawdzone kule (np. 1,2 -> nie sprawdzamy 2, 1);
                Kula k = listaKul.get(i);
                Wektor WektorOdleglosci = new Wektor(this.sr_x - k.sr_x, this.sr_y - k.sr_y);                        // Wektor odleglosci - kulka badana -> kulka unieruchomiona (ktora wywolala funkcje)
                Wektor WektorPredkosciK = new Wektor(k.xspeed - this.xspeed, k.yspeed - this.yspeed);                // Wektor predkosci kuli K wzgledem kuli z "this"

                if (WektorOdleglosci.dlugosc <= (this.promien + k.promien)) {                                               // Sa styczne (numerycznie nierealne dlatego '<=')

                    if (WektorPredkosciK.iloczynSkalarnyWspolrzedne(WektorOdleglosci) > 0) {                                // Przeksztalcenie wzoru na wyliczenie kata pomiedzy wektorami (zderzenie nie nastapi dla (90, 270);

                        if (WektorOdleglosci.x != 0) {                                                                      // Zderzenie idealnie pionowe -> potrzebne dla fizyki, inaczej beda  /0 <- !
                            /* Bez fizyki */
                            this.xspeed *= (-1);
                            this.yspeed *= (-1);
                            k.xspeed *= (-1);
                            k.yspeed *= (-1);

                            System.out.println("***ZDERZENIE***");                                            // Wydruk kontrolny

                            /*** *** *** *** *** *** *** MAPA ZDERZEŃ *** *** *** *** *** *** ***/
                            kolizja = new Kolizje(badana, k);                       // zapisanie informacji o kolizji do obiektu
                            listaKolizji.add(kolizja);                              // dodanie kolizji do listy
                            if (listaKolizji.size() > max_kul) liczbaKolizji++;     // jezeli liczba kulek jest duza, to licz ile kolizji juz nastapilo
                            try {
                                kolizja.zapiszKolizje(listaKolizji);                // zapisz kolizje do pliku
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }


    /* Klasa obliczen na wektorach */                                                                                       // Klasa na potrzeby fizyki i zderzen
    private class Wektor {
        double x, y;
        double dlugosc;

        public Wektor(double x, double y) {
            this.x = x;
            this.y = y;
            this.dlugosc = Math.sqrt(x * x + y * y);
        }

        public double iloczynSkalarnyWspolrzedne(Wektor v) {
            double iloczyn = this.x * v.x + this.y * v.y;
            return iloczyn;
        }
    }


    /*** *** *** *** *** *** *** *** *** *** *** *** *** MAPA ZDERZEŃ *** *** *** *** *** *** *** *** *** *** *** *** ***/
    /* Klasa zapamietujaca kolizje */
    protected class Kolizje {
        public int x1, y1, x2, y2, size1, size2;
        public Color kolor1, kolor2;

        public Kolizje(Kula k1, Kula k2) {                                                                              // Dodanie informacji o kolizji
            this.x1 = k1.x;
            this.y1 = k1.y;
            this.size1 = k1.size;
            this.kolor1 = k1.kolor;
            this.x2 = k2.x;
            this.y2 = k2.y;
            this.size2 = k2.size;
            this.kolor2 = k2.kolor;
        }

        public Kolizje() {
        }

        public void zapiszKolizje(ArrayList<Kolizje> lista) throws IOException {
            String dane = new String();
            Kolizje k;

            if (listaKul.size() < max_kul) {                                // Pojedynczy zapis
                k = lista.get(lista.size() - 1);
                dane = k.x1 + " " + k.y1 + " " + k.size1 + " " + k.x2 + " " + k.y2 + " " + k.size2 + "\n";
                plik.zapiszDoPliku(dane);

                System.out.print("***Pojedynczy zapis: " + dane);

            } else {                                                        // Zapis hurtowy (>20 kulek)
                if (liczbaKolizji == bufor) {                               // jezeli wystapilo 20 kolizji to zapisz do pliku
                    System.out.println("*** ZAPISUJE PACZKE DANYCH ***");
                    for (int i = 0; i < bufor; i++) {
                        k = lista.get(lista.size() - bufor + i);
                        dane = k.x1 + " " + k.y1 + " " + k.size1 + " " + k.x2 + " " + k.y2 + " " + k.size2 + "\n";
                        plik.zapiszDoPliku(dane);

                        System.out.print(dane);
                    }
                    liczbaKolizji = 0;
                }
            }
        }
    }

    protected class Plik {
        File file;
        String sciezka;

        public Plik(String sciezka) throws IOException {
            this.sciezka = sciezka;
            this.file = new File(sciezka);

            if (!file.exists()) {                                                                                         // Czy plik istnieje?
                file.createNewFile();
                System.out.println("Nie wykryto pliku! Utworzono go!");
            }

            if (file.length() != 0) {                                                                                   // Czy istnial juz wczesniej?
                FileWriter zapisz = new FileWriter(sciezka);
                zapisz.write("");
                System.out.println("Plik nie jest pusty, nadpisuje go!");
            }
        }

        public void zapiszDoPliku(String tekst) throws IOException {
            FileWriter zapisz = new FileWriter(this.sciezka, true);                                             // Umożliwienie nadpisania pliku
            zapisz.append(tekst);
            zapisz.close();
        }

//        public void odczytajPlik(ArrayList<Kolizje> lista) throws FileNotFoundException {                                // Odczytanie danych z pliku
//            Scanner odczytaj = new Scanner(this.file);                                                                  // Docelowo ta funkcja ma wczytywac kolizje,
//            Kolizje k = new Kolizje();                                                                                  // Ale aktualnie nic nie robi
//
//            while (odczytaj.hasNext()){
//                k.x1 = Integer.parseInt(odczytaj.nextLine());
//                k.y1 = Integer.parseInt(odczytaj.nextLine());
//                k.size1 = Integer.parseInt(odczytaj.nextLine());
//                k.x2 = Integer.parseInt(odczytaj.nextLine());
//                k.y2 = Integer.parseInt(odczytaj.nextLine());
//                k.size2 = Integer.parseInt(odczytaj.nextLine());
//                lista.add(k);
//            }
//
//            odczytaj.close();
//        }
    }


    public class Mapa extends JPanel {              // klasa dt. okna z mapa zdarzeń
        JFrame okno;                                // Uchwyt (wskaźnik) do okna
        Plik plik;                                  // uchwyt do pliku
        ArrayList<Kolizje> wczytaneKolizje;         // lista na wczytane kolizje


        public Mapa(JFrame okno, Plik plik) throws FileNotFoundException {
            wczytaneKolizje = new ArrayList<>();                            // inicjalizacja listy
            setBackground(Color.WHITE);
            this.okno = okno;
            this.plik = plik;

            wczytaneKolizje=wczytajKolizje();                               // wczytanie i zwrocenie listy kolizji
            repaint();                                                      // wyrysowanie kolizji

        }
        /****/
        public ArrayList<Kolizje> wczytajKolizje() throws FileNotFoundException {         // wczytywanie kolizji (generalnie tą funkcje można wrzucić do klasy PLIK ale nie będę już kombinował)
            ArrayList<Kolizje> lista = new ArrayList<>();
            StringTokenizer tokenizer;                                                    // Tokenizer to wychwywtywania elementów z ciągu znaków (wczytanej lini)
            Scanner temp = new Scanner(plik.file);
            Kula k1, k2;                                                                  // Obiekty kul

            int i=0;
            while (temp.hasNextLine()){
                if(i < lista.size()-1){                                                 // Nie wczytuj tych samych kul wielokrotnie
                    i++;
                } else{
                    tokenizer = new StringTokenizer(temp.nextLine());                   // Wczytanie lini z pliku
                    k1 = new Kula(Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()));   // Utworzenie nowej kuli o wczytanych paramterach (x, y, size)
                    k2 = new Kula(Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()), Integer.parseInt(tokenizer.nextToken()));
                    lista.add(new Kolizje(k1, k2));         // Dodanie do listy NOWEGO obiektu klasy Kolizje  wczytanych wcześniej kul
                    System.out.println("Dane o kolizji: " + k1.x + " " + k1.y + " " + k1.size + " " + k2.x + " " + k2.y + " " + k2.size);
                }
            }
            temp.close();                                   // zamknięcie strumienia do pliku
            return lista;
        }
        /****/

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            /* Rysowanie miejsc zderzenia */
            for (Kolizje k : wczytaneKolizje) {
                g.setColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
                g.fillOval(k.x1, k.y1, k.size1, k.size1);
                g.fillOval(k.x2, k.y2, k.size2, k.size2);
            }

            /* Informacje */
            g.setColor(Color.BLUE);
            g.drawString("Liczba kolizji: ", 10, 15);
            g.drawString(Integer.toString(wczytaneKolizje.size()), 100, 15);

        }
    }
}

