/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorageserver.server;

/**
 *
 * @author HP Zbook 15
 */
public interface SessionListener {

    public void notifyDisconnected(Session s);

    public void notifyLogin(Session s);
}
