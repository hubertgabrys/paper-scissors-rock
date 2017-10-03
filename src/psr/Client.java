package psr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Client {
    private String gracz_nick;//Pole klasy string przechowujace nick gracza
    private String przeciwnik_nick;//Pole klasy string przechowujace nickprzeciwnika
    private PrintWriter pw;//Pole klasy printwriter, ktore posluzy do wysylania obiektow typu string na serwer
    private BufferedReader br, brKlawiatura;//Pola typu bufferedreader sluzace do wczytywania typu string
    private Socket gniazdoKlienta;//Gniazdo klienta sluzace do polaczenia sie z serwerem

    public Client(String gracz_nick, String przeciwnik_nick, String serwer_nazwa) {
        this.gracz_nick = gracz_nick;//przypisanie nazwy graczowi
        this.przeciwnik_nick = przeciwnik_nick;//przypisanie nazwy przeciwnikowi
        try {
            gniazdoKlienta = new Socket(serwer_nazwa, 1413);//stworzenie gniazda klienta. laczenie nastepuje przez port 1413. Jesli port jest zajety mozna wpisac inna wartosc. WAzNE: nr portu musi byc taki sam ja gniezdzie po stronie serwera.
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            br = new BufferedReader(new InputStreamReader(gniazdoKlienta.getInputStream()));//stworzenie obiektu przyjmujacego strumien wejsciowy z serwera
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        brKlawiatura = new BufferedReader(new InputStreamReader(System.in));//stworzenie obiektu wczytujacego dane z klawiatury
        try {
            pw = new PrintWriter(gniazdoKlienta.getOutputStream(), true);//stworzenie obiektu wysylajacego dane typu String na serwer
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        pw.println(this.gracz_nick);//wyslanie nicku gracza na serwer
        pw.println(this.przeciwnik_nick);//wyslanie nicku przeciwnika na serwer
        new MessagesThread().start();//uruchomienie nowego watku. Uruchomiona zostaje metoda run()
    }

    public static void main(String... args) {
        String daneGracza[] = null;
        daneGracza = wyswietlMenu(); //wyswietlenie menu i pobranie od gracza jego nicku i nicku przeciwnika
        String servername = "localhost";//ustalenie nazwy serwera
        try {
            new Client(daneGracza[0], daneGracza[1], servername);//stworzenie obiektu marynarzykKlient
        } catch (Exception ex) {
            System.out.println("Problem with creating object Client:" + ex.getMessage());
        }
    }

    class MessagesThread extends Thread {

        private String txt;//Pole klasy String, ktore bedzie przechowywac tekst wpisywany przez gracza

        @Override
        public void run() {
            try {
                while (true) {//petla while pozwala graczowi grac dopoki nie poda instrukcji wyjscia
                    do {
                        System.out.println("Choose: 'Paper', 'Scissors' or 'Rock'. Enter 'Quit' to end the game.");
                        txt = brKlawiatura.readLine();//wczytanie wyboru gracza
                    } while (!"Paper".equals(txt) && !"Scissors".equals(txt) && !"Rock".equals(txt) && !"Quit".equals(txt));//jesli gracz wpisze cos niezgodnego z przepisami gry zostanie poproszony o powtorne wpisanie
                    if ("Quit".equals(txt)) {//Jesli gracz wpisze zakoncz modul klienta zostanie zamkniety
                        pw.println(txt);//wyslanie informacji o wyborze gracza na serwer
                        System.exit(0);//zamkniecie modulu klienta
                    } else {
                        pw.println(txt);//Wyslij wybor na serwer
                        //System.out.println(br.readLine());//Informacja o czekaniu na ruch
                        System.out.println(br.readLine());//Informacja o wyborach obu zawodnikow
                        System.out.println(br.readLine() + '\n');//Informacja o zwyciezcy
                        br.readLine();//Informacja o wyzerowaniu poprzednich wyborow na serwerrze.
                    }
                }
            } catch (IOException ex) {
                System.out.println("Problem with receiving data by Client: " + ex.getMessage());
            }
        }
    }

    private static String[] wyswietlMenu() {
        String[] output = new String[2];//tablica typu string przechowujaca nick gracza i przeciwnika
        BufferedReader brKlawiatura = new BufferedReader(new InputStreamReader(System.in));//obiekt klasy bufferedreader sluzacy do wczytania znakow z klawiatury
        System.out.println("=======================");
        System.out.println("| Paper Scissors Rock |");
        System.out.println("=======================");
        System.out.println("Your name: ");
        try {
            output[0] = brKlawiatura.readLine();//wczytanie nicka gracza z klawiatury
            System.out.println("Your opponent name: (Enter 'AI' to play with PC)");
            output[1] = brKlawiatura.readLine();//wczytanie nicka drugiego gracza z klawiatury
        } catch (IOException ex) {
            System.out.println("Problem with loading player or opponent name: " + ex.getMessage());
        }
        return output;
    }
}