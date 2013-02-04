package brutes.server;

import brutes.Brutes;
import brutes.Console;
import brutes.server.db.DatasManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *
 * @author Olivares Georges <dev@olivares-georges.fr>
 */
public class ConsoleThread extends Thread {

    public static ServerThread SERVER;

    public static ConsoleThread start(ServerThread server) {
        ConsoleThread.SERVER = server;
        ConsoleThread c = new ConsoleThread();
        c.start();
        return c;
    }

    @Override
    public void run() {

        Console c = new Console(System.in, System.out);
        c.printMenu();

        try {
            while (c.readLine() != null) {
                try {
                    switch (c.getCmd()) {
                        case "options":
                            ConsoleThread.SERVER.printOptions();
                            break;

                        case "start":
                            System.out.println("Start server ...");
                            System.out.println("Server IP = " + this.getIp());
                            ConsoleThread.SERVER.start();
                            System.out.println("Server started !");
                            break;

                        case "kill":
                            if (!ConsoleThread.SERVER.isAlive()) {
                                System.out.println("You must start the server before try to kill it.");
                            } else {
                                System.out.println("Try to kill the server ...");
                                Brutes.exit();
                            }
                            break;

                        case "server":
                            System.out.println("Server is [" + (ConsoleThread.SERVER.isAlive() ? "running" : "shutdown") + "]");
                            System.out.println("Server IP = " + this.getIp());
                            break;

                        case "exit":
                            System.out.println("Try to kill the server ...");
                            Brutes.exit();
                            System.out.println("Bye bye !");
                            System.exit(0);
                            break;

                        case "populate":
                            if (!ConsoleThread.SERVER.isAlive()) {
                                System.out.println("You must start the server before try to kill it.");
                            } else {
                                DatasManager.populate();
                            }
                            break;

                        case "unpopulate":
                            if (!ConsoleThread.SERVER.isAlive()) {
                                System.out.println("You must start the server before try to kill it.");
                            } else {
                                c.readLine("Etes vous sûr ? Cette action est irreversible !\nPour confirmer, entrez l'IP du serveur (" + this.getIp() + ")");

                                if (c.getLine().equals(this.getIp())) {
                                    DatasManager.unpopulate();
                                } else {
                                    System.out.println("Annulé");
                                }

                            }
                            break;

                        default:
                            c.printMenu();
                            break;
                    }

                } catch (IOException e) {
                    System.out.println("Error. Verify if server is started");
                    System.out.println(e.getLocalizedMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            c.close();

        } catch (Exception e) {
            System.out.println("Error while reading line from console : " + e);
        }
    }

    private String getIp() throws IOException {
        URL url = new URL("http://api.exip.org/?call=ip");
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        return in.readLine().trim();
    }
}
