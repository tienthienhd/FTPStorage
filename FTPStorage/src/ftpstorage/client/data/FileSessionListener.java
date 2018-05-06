/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorage.client.data;

import javax.swing.JProgressBar;

/**
 *
 * @author HP Zbook 15
 */
public interface FileSessionListener {
    public void transferComplete(FileSession file);
    
    public JProgressBar[] getProgressBars();
    
}
