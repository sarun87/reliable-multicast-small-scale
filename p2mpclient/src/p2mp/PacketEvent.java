/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2mp;

import java.util.EventObject;

/**
 *
 * @author Shyam
 */
public class PacketEvent extends EventObject {

    public PacketEvent(Object source, String message, long time) {
        super(source);

    }
}
