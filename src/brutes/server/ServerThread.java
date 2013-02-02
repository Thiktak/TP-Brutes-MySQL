package brutes.server;

import brutes.server.db.DatasManager;
import brutes.server.net.NetworkServer;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Karl
 */
public class ServerThread extends Thread {

    public static final int TIMEOUT_ACCEPT = 10000;
    public static final int TIMEOUT_CLIENT = 2000;
    public static final boolean SERVER_MySQL = true;

    @Override
    public void run() {
        try (ServerSocket sockserv = new ServerSocket(42666)) {
            sockserv.setSoTimeout(ServerThread.TIMEOUT_ACCEPT);

            boolean toPopulate = false;
            if (!ServerThread.SERVER_MySQL) {
                // DEBUG
                (new File("~$bdd.db")).delete();
                toPopulate = true;

                //File file = new File("~$bdd.db");
                //boolean toPopulate = !file.exists();
                DatasManager.getInstance("sqlite", "~$bdd.db");
                System.out.println("SQLite");
            } else {
                DatasManager.getInstance("mysql", "199.16.131.115/olivares_tp_brute", "olivares_brute", "TPc9hBEzVM2S");
                //DatasManager.getInstance("mysql", "sql2.freesqldatabase.com:3306/sql22967", "sql22967", "hI3%yA5*");
                System.out.println("MySQL");
            }

            if (toPopulate) {
                DatasManager.populate();
                System.out.println("populate");
            }

            while (!this.isInterrupted()) {
                try {
                    final Socket sockcli = sockserv.accept();
                    sockcli.setSoTimeout(ServerThread.TIMEOUT_CLIENT);
                    new Thread() {
                        @Override
                        public void run() {
                            try (NetworkServer n = new NetworkServer(sockcli)) {
                                n.read();
                            } catch (Exception ex) {
                                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }.start();
                } catch (SocketTimeoutException ex) {
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
