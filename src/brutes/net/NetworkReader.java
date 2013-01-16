/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brutes.net;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Karl
 */
class NetworkReader {
    private InputStream is;

    public NetworkReader(InputStream is) {
        this.is = is;
    }
    
    public int readDiscriminant(){
        try {
            byte[] b = new byte[Protocol.SIZE_DISCRININANT];
            this.is.read(b);
            ByteBuffer bb = ByteBuffer.wrap(b);
            return bb.getInt();
        } catch (IOException ex) {
            Logger.getLogger(NetworkReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Protocol.R_ERROR;
    }
    public short readShortInt(){
        try {
            byte[] b = new byte[Protocol.SIZE_SHORTINT];
            this.is.read(b);
            ByteBuffer bb = ByteBuffer.wrap(b);
            return bb.getShort();
        } catch (IOException ex) {
            Logger.getLogger(NetworkReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    public long readLongInt(){
        try {
            byte[] b = new byte[Protocol.SIZE_LONGINT];
            this.is.read(b);
            ByteBuffer bb = ByteBuffer.wrap(b);
            return bb.getLong();
        } catch (IOException ex) {
            Logger.getLogger(NetworkReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    public String readString(){
        try {
            short length = this.readShortInt();
            byte[] b = new byte[length];
            this.is.read(b);
            return new String(b, "UTF-8");
        } catch (IOException ex) {
            Logger.getLogger(NetworkReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
