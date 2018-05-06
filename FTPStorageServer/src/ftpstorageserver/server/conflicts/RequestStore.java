/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorageserver.server.conflicts;

import ftpstorageserver.server.Session;
import java.io.IOException;

/**
 *
 * @author HP Zbook 15
 */
public class RequestStore {
    private Session s;
    private String request;
    
    public RequestStore(Session s, String request){
        this.s = s;
        this.request = request;
    }

    public Session getS() {
        return s;
    }

    public void setS(Session s) {
        this.s = s;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
    
    public void resumeProcess() throws IOException{
        s.processStore(request);
    }
    
}
