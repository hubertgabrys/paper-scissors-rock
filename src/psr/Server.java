package psr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;

public class Server {
    private Vector<String> gracze = new Vector<String>();//Kontener przechowujacy nicki graczy
    private Vector<ObslugaGracza> klienci = new Vector<ObslugaGracza>();//Kontener przechowujacy obiekty klasy ObslugaGracza 

    public static void main(String... args) {
        new Server().stworzSerwer();//Stworzenie obiektu serwera
    }

    private void stworzSerwer() {
        ServerSocket gniazdoSerwera = null;
        try {
            gniazdoSerwera = new ServerSocket(1413); //stworzenie gniazda klienta. laczenie nastepuje przez port 1413. Jesli port jest zajety mozna wpisac inna wartosc. WAzNE: nr portu musi byc taki sam jak w gniezdzie po stronie klienta.
        } catch (IOException ex) {
            System.out.println("Problem with creating server socket: " + ex.getMessage());
        }
        System.out.println("Server is online");
        while (true) {
            Socket gniazdoKlienta = null;
            try {
                gniazdoKlienta = gniazdoSerwera.accept(); //Polaczenia gniezda klienta z gniazdem serwera
            } catch (IOException ex) {
                System.out.println("Problem with connection to Client socket: " + ex.getMessage());
            }
            ObslugaGracza c = null;
            try {
                c = new ObslugaGracza(gniazdoKlienta);
            } catch (Exception ex) {
                System.out.println("Problem with creating object to handle the player: " + ex.getMessage());
            }
            klienci.add(c);//Dodanie nowego gracza do listy klientow
            System.out.println("Added: " + c.gracz_nick);
            System.out.println("Available players: ");
            for (String s : gracze) {//Wypisanie listy zalogowanych graczy
                System.out.println(s);
            }
        }
    }

    /**
     * metoda obsluguje pojedynek gracza z serwerem
     * @param gracz_nick 
     */
    private void pojedynek(String gracz_nick) {
        ObslugaGracza gracz_obsluga = null;
        for (ObslugaGracza c : klienci) {//Znalezienie gracza wsrod klientow
            if (c.zwrocNickGracza().equals(gracz_nick)) {
                gracz_obsluga = c;
            }
        }
        String wyborAI = losujWyborSerwera();//Wylosowanie wyboru AI (papier, nozyce czy kamien)
        gracz_obsluga.wyslijWiadomosc(gracz_nick + " chose " + gracz_obsluga.wyborGracza + ", AI " + wyborAI);//przekazanie informacji graczowi o tym kto co wybral

        String zwyciezca = wyznaczZwyciezce(gracz_obsluga, wyborAI);//wyznaczenie zwyciezcy na podstawie dokonanych wyborow
        if ("Tie".equals(zwyciezca)) {
            gracz_obsluga.wyslijWiadomosc("The match ended in a tie.");//Wyslanie informacji o wyniku do gracza
        } else if (zwyciezca == null) {
            gracz_obsluga.wyslijWiadomosc("Illegal move!");//wyslanie informacji do gracza
        } else {
            gracz_obsluga.wyslijWiadomosc("The winner is " + zwyciezca);//Wyslanie informacji o remisie do gracza
        }

        gracz_obsluga.wyborGracza = null;//Wyzerowanie wczesniejszego wyboru
        gracz_obsluga.wyslijWiadomosc("W");//Wyslanie informacji o wyzerowaniu wczesniejszych wyborow
    }

    /**
     * Metoda obsluguje pojedynek gracza z innym graczem
     * @param gracz_nick
     * @param przeciwnik_nick 
     */
    private void pojedynek(String gracz_nick, String przeciwnik_nick) {
        ObslugaGracza gracz_obsluga = null;
        ObslugaGracza przeciwnik_obsluga = null;
        for (ObslugaGracza c : klienci) {//Znalezienie gracza i przeciwnika
            if (c.zwrocNickGracza().equals(gracz_nick)) {
                gracz_obsluga = c;
            } else if (c.zwrocNickGracza().equals(przeciwnik_nick)) {
                przeciwnik_obsluga = c;
            }
        }
        gracz_obsluga.wyslijWiadomosc(gracz_nick + " chose " + gracz_obsluga.wyborGracza + ", " + przeciwnik_nick + " " + przeciwnik_obsluga.wyborGracza);//wyslanie wiadomosci do graczy o dokonanych wyborach
        przeciwnik_obsluga.wyslijWiadomosc(przeciwnik_nick + " chose " + przeciwnik_obsluga.wyborGracza + ", " + gracz_nick + " " + gracz_obsluga.wyborGracza);//wyslanie wiadomosci do graczy o dokonanych wyborach
        String zwyciezca = wyznaczZwyciezce(gracz_obsluga, przeciwnik_obsluga);//wyznaczenie zwyciezcy
        if ("Tie".equals(zwyciezca)) {
            gracz_obsluga.wyslijWiadomosc("The match ended in a tie.");
            przeciwnik_obsluga.wyslijWiadomosc("The match ended in a tie.");
        } else if (zwyciezca == null) {
            gracz_obsluga.wyslijWiadomosc("Illegal move!");
            przeciwnik_obsluga.wyslijWiadomosc("Illegal move!");
        } else {
            gracz_obsluga.wyslijWiadomosc("The winner is " + zwyciezca);
            przeciwnik_obsluga.wyslijWiadomosc("The winner is " + zwyciezca);
        }
        gracz_obsluga.wyborGracza = null;//wyzerowanie wyborow
        gracz_obsluga.wyslijWiadomosc("W");//przekazanie informacji o wyzerowaniu graczowi
        przeciwnik_obsluga.wyborGracza = null;//wyzerowanie wyborow
        przeciwnik_obsluga.wyslijWiadomosc("W");//przekazanie informacji o wyzerowaniu przeciwnikowi
    }

    /**
     * Metoda wyznacza zwyciezce pojedynku gracza z serwerem
     * @param gracz_obsluga
     * @param wyborAI
     * @return 
     */
    private static String wyznaczZwyciezce(ObslugaGracza gracz_obsluga, String wyborAI) {
        String result = null;
        if ("Paper".equals(gracz_obsluga.wyborGracza)) {
            if ("Paper".equals(wyborAI)) {
                result = "Tie";
            } else if ("Scissors".equals(wyborAI)) {
                result = "AI";
            } else if ("Rock".equals(wyborAI)) {
                result = gracz_obsluga.gracz_nick;
            }
        } else if ("Scissors".equals(gracz_obsluga.wyborGracza)) {
            if ("Paper".equals(wyborAI)) {
                result = gracz_obsluga.gracz_nick;
            } else if ("Scissors".equals(wyborAI)) {
                result = "Tie";
            } else if ("Rock".equals(wyborAI)) {
                result = "AI";
            }
        } else if ("Rock".equals(gracz_obsluga.wyborGracza)) {
            if ("Paper".equals(wyborAI)) {
                result = "AI";
            } else if ("Scissors".equals(wyborAI)) {
                result = gracz_obsluga.gracz_nick;
            } else if ("Rock".equals(wyborAI)) {
                result = "Tie";
            }
        }
        System.out.println("The winner is " + result);
        return result;
    }

    /**
     * Metoda wyznacza zwyciezce pojedynku gracza z innym graczem
     * @param gracz_obsluga
     * @param przeciwnik_obsluga
     * @return 
     */
    private static String wyznaczZwyciezce(ObslugaGracza gracz_obsluga, ObslugaGracza przeciwnik_obsluga) {
        String result = null;
        if ("Paper".equals(gracz_obsluga.wyborGracza)) {
            if ("Paper".equals(przeciwnik_obsluga.wyborGracza)) {
                result = "Tie";
            } else if ("Scissors".equals(przeciwnik_obsluga.wyborGracza)) {
                result = przeciwnik_obsluga.gracz_nick;
            } else if ("Rock".equals(przeciwnik_obsluga.wyborGracza)) {
                result = gracz_obsluga.gracz_nick;
            }
        } else if ("Scissors".equals(gracz_obsluga.wyborGracza)) {
            if ("Paper".equals(przeciwnik_obsluga.wyborGracza)) {
                result = gracz_obsluga.gracz_nick;
            } else if ("Scissors".equals(przeciwnik_obsluga.wyborGracza)) {
                result = "Tie";
            } else if ("Rock".equals(przeciwnik_obsluga.wyborGracza)) {
                result = przeciwnik_obsluga.gracz_nick;
            }
        } else if ("Rock".equals(gracz_obsluga.wyborGracza)) {
            if ("Paper".equals(przeciwnik_obsluga.wyborGracza)) {
                result = przeciwnik_obsluga.gracz_nick;
            } else if ("Scissors".equals(przeciwnik_obsluga.wyborGracza)) {
                result = gracz_obsluga.gracz_nick;
            } else if ("Rock".equals(przeciwnik_obsluga.wyborGracza)) {
                result = "Tie";
            }
        }
        System.out.println("The winner is " + result);
        return result;
    }

    /**
     * Metoda losuje gest wybrany przez serwer
     * @return 
     */
    private static String losujWyborSerwera() {
        Random random = new Random();
        int liczba = random.nextInt(3);//znalezienie losowej liczby calkowitej miedzy 0-2
        String wynik = null;
        if (liczba == 0) {
            wynik = "Paper";
        } else if (liczba == 1) {
            wynik = "Scissors";
        } else if (liczba == 2) {
            wynik = "Rock";
        }
        System.out.println("Server chose: " + wynik);
        return wynik;
    }

    class ObslugaGracza implements Runnable {
        private Thread watek;
        private String gracz_nick;
        private String przeciwnik_nick;
        private String wyborGracza;//pole przechowujace gest wybrany przez gracza
        private BufferedReader br;//pole do obslugi wczytywania strumienia wejsciowego od klienta
        private PrintWriter pw;//pole do obslugi wysylania strumienia wyjsciowego do klienta

        private ObslugaGracza(Socket client) {
            try {
                br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            } catch (IOException ex) {
                System.out.println("Problem ze stworzeniem obiektu klasy BufferedReader do wczytywania strumienia we: " + ex.getMessage());
            }
            try {
                pw = new PrintWriter(client.getOutputStream(), true);
            } catch (IOException ex) {
                System.out.println("Problem ze stworzeniem obiektu klasy PrintWriter do wysylania strumienia wy: " + ex.getMessage());
            }
            try {
                gracz_nick = br.readLine();//wczytanie informacji o nicku gracza
            } catch (IOException ex) {
                System.out.println("Problem z wczytaniem nicku gracza: " + ex.getMessage());
            }
            gracze.add(gracz_nick);//dodanie nicku gracza do listy nickow graczy
            try {
                przeciwnik_nick = br.readLine();//wczytanie informacji o nicku przeciwnika
            } catch (IOException ex) {
                System.out.println("Problem z wczytaniem nicku przeciwnika: " + ex.getMessage());
            }
            wyborGracza = null;
            watek = new Thread(this, gracz_nick);//stworzenie nowego watku
            watek.start();//uruchomienie nowego watku. kontynuuje w metodzie run()
        }

        /**
         * Metoda wysyla obiekt klasy String do klienta
         * @param txt 
         */
        private void wyslijWiadomosc(String txt) {
            pw.println(txt);
        }

        /**
         * Metoda zwraca nick gracza
         * @return 
         */
        private String zwrocNickGracza() {
            return gracz_nick;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    wyborGracza = br.readLine();//Odczyt wyboru gracza
                } catch (IOException ex) {
                    System.out.println("Problem z wczytaniem wyboru(gestu) gracza: " + ex.getMessage());
                }
                if ("Quit".equals(wyborGracza)) {//gracz wybral zakonczenie
                    klienci.remove(this);//usuniecie gracza z listy klientow
                    gracze.remove(gracz_nick);//usuniecie nicku gracza z listy nickow graczy
                    System.out.println("Removed: " + gracz_nick);
                    System.out.println("Available players: ");
                    for (String s : gracze) {//wypisanie listy zalogowanych graczy
                        System.out.println(s);
                    }
                    break;
                } else if ("AI".equals(przeciwnik_nick)) {//gracz zdecydowal zagrac z AI
                    pojedynek(gracz_nick);//rozpoczecie pojedynku
                } else {//gracz zdecydowal zagrac z innym graczem
                    System.out.println("Read player choice: " + gracz_nick);
                    for (ObslugaGracza c : klienci) {
                        if (c.zwrocNickGracza().equals(przeciwnik_nick) && c.wyborGracza != null) {//drugi gracz juz odpowiedzial, mozna przejsc do porownania
                            System.out.println("The match has started");
                            pojedynek(gracz_nick, przeciwnik_nick);//porownanie wyborow obu graczy (rozpoczecie pojedynku)
                        } else if (c.zwrocNickGracza().equals(gracz_nick)) {//drugi gracz jeszcze nie odpowiedzial, czekamy na jego ruch
                            //c.wyslijWiadomosc("Czekamy a ruch " + przeciwnik_nick + "...");//Wyslanie informacji do gracza o czekaniu na ruch drugiego gracza
                        }
                    }
                }
            }
        }
    }
}