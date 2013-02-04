package brutes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 *
 * @author Thiktak
 */
public class Console {

    private final BufferedReader br;
    private final PrintStream out;
    private String line;

    public Console(InputStream in, OutputStream out) {
        this.out = new PrintStream(out);
        this.br = new BufferedReader(new InputStreamReader(in));
    }
    
    public void printMenu() {
        this.out.println("TP-Brutes CONSOLE");
        this.out.println(" [options]    return informations about res/options.xml");
        this.out.println(" [start]      start the server");
        this.out.println(" [kill]       kill the server");
        this.out.println(" [server]     return informations about the server");
        this.out.println(" [populate]   populate the server with datas into res/*.xml");
        this.out.println(" [unpopulate] unpopulate the server");
        this.out.println(" [exit]       kill the server and exit");
        this.out.println();
    }
    
    public String readLine() throws IOException {
        return this.readLine("");
    }
    
    public String readLine(String ask) throws IOException {
        ask = ask.isEmpty() ? "" : ask + "\n";
        this.out.print("\n" + ask + "> ");
        return (this.line = this.br.readLine());
    }

    public String getLine() {
        return this.line;
    }
    
    public String getCmd() {
        return this.line.split(" ", 1)[0];
    }

    public void close() throws IOException {
        this.br.close();
    }
}
