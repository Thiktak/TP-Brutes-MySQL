package brutes.server;

import brutes.server.db.DatasManager;
import brutes.server.net.NetworkServer;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author Karl
 */
public class ServerThread extends Thread {

    public static final int TIMEOUT_ACCEPT = 10000;
    public static final int TIMEOUT_CLIENT = 2000;
    private static String SERVER_TYPE = "";
    private static String SERVER_HOST = "";
    private static String SERVER_LOGIN = "";
    private static String SERVER_PASSWORD = "";
    private static boolean SERVER_POPULATE = false;

    @Override
    public void run() {
        // Options
        try {
            if( !(new File("res/options.xml")).exists()) {
                System.out.println("Create the file res/options.xml. See res/options.default.xml for more explanation.");
            }
            
            SAXBuilder sxb = new SAXBuilder();
            Element xmlServer = sxb.build("res/options.xml").getRootElement();
            for (Iterator<Element> i = xmlServer.getChildren("server").iterator(); i.hasNext();) {
                Element current1 = i.next();
                if (current1.getChild("type") != null) {
                    ServerThread.SERVER_TYPE = current1.getChild("type").getText();
                }
                if (current1.getChild("host") != null) {
                    ServerThread.SERVER_HOST = current1.getChild("host").getText();
                }
                if (current1.getChild("login") != null) {
                    ServerThread.SERVER_LOGIN = current1.getChild("login").getText();
                }
                if (current1.getChild("password") != null) {
                    ServerThread.SERVER_PASSWORD = current1.getChild("password").getText();
                }
                if (current1.getChild("populate") != null) {
                    ServerThread.SERVER_POPULATE = current1.getChild("populate").getText().equals("true");
                }
            }
        } catch (JDOMException | IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Server
        try (ServerSocket sockserv = new ServerSocket(42666)) {
            sockserv.setSoTimeout(ServerThread.TIMEOUT_ACCEPT);
            
            // Create database connection
            DatasManager.getInstance(ServerThread.SERVER_TYPE, ServerThread.SERVER_HOST, ServerThread.SERVER_LOGIN, ServerThread.SERVER_PASSWORD);

            if (ServerThread.SERVER_POPULATE) {
                DatasManager.populate();
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
        // */
    }
}
