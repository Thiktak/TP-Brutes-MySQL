/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brutes.net;

/**
 *
 * @author Karl
 */
class NetworkException extends Exception {
    public final static String SERVER_UNDEFINED = "server adresse undefined";
    
    public NetworkException(String message) {
        super(message);
    } 
}