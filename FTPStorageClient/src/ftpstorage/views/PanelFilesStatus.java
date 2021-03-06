/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorage.views;

import ftpstorage.client.IClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JProgressBar;

/**
 *
 * @author HP Zbook 15
 */
public class PanelFilesStatus extends javax.swing.JPanel implements FileStatusListener {

    private IClient client;
    private ArrayList<FileStatus> files = new ArrayList<>();
    private FileStatus fileSelected = null;

    /**
     * Creates new form PanelFilesStatus
     */
    public PanelFilesStatus(IClient client) {
        this.client = client;
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnResume = new javax.swing.JButton();
        btnPause = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        scrollPanelStatus = new javax.swing.JScrollPane();
        pnlFilesStatus = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Status", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        setMaximumSize(new java.awt.Dimension(1260, 250));
        setMinimumSize(new java.awt.Dimension(1260, 250));
        setPreferredSize(new java.awt.Dimension(1260, 250));

        btnResume.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/Play.png"))); // NOI18N
        btnResume.setText("Resume");
        btnResume.setEnabled(false);
        btnResume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResumeActionPerformed(evt);
            }
        });

        btnPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/Pause.png"))); // NOI18N
        btnPause.setText("Pause");
        btnPause.setEnabled(false);
        btnPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPauseActionPerformed(evt);
            }
        });

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/Delete.png"))); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        scrollPanelStatus.setHorizontalScrollBar(null);

        pnlFilesStatus.setBackground(new java.awt.Color(255, 255, 255));
        pnlFilesStatus.setLayout(new javax.swing.BoxLayout(pnlFilesStatus, javax.swing.BoxLayout.Y_AXIS));
        scrollPanelStatus.setViewportView(pnlFilesStatus);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(86, 86, 86)
                .addComponent(btnResume)
                .addGap(18, 18, 18)
                .addComponent(btnPause)
                .addGap(18, 18, 18)
                .addComponent(btnDelete)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPanelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnResume)
                    .addComponent(btnPause)
                    .addComponent(btnDelete))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPanelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnPauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPauseActionPerformed
        if (this.fileSelected == null) {
            System.out.println("Please select file before resume");
            return;
        }
        if (this.fileSelected.isCompleted()) {
            return;
        }
        this.fileSelected.pause();
        try {
            this.client.pause(files.indexOf(this.fileSelected));
        } catch (IOException ex) {
            Logger.getLogger(PanelFilesStatus.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_btnPauseActionPerformed

    private void btnResumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResumeActionPerformed
        if (this.fileSelected == null) {
            System.out.println("Please select file before resume");
            return;
        }
        if (this.fileSelected.isCompleted()) {
            return;
        }
        this.fileSelected.resume();
        try {
            this.client.resume(files.indexOf(this.fileSelected));
        } catch (IOException ex) {
            Logger.getLogger(PanelFilesStatus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnResumeActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int index = this.files.indexOf(this.fileSelected);
        this.fileSelected.delete();
        this.pnlFilesStatus.remove(this.fileSelected);
        this.files.remove(this.fileSelected);
        this.fileSelected = null;
        this.scrollPanelStatus.revalidate();
        this.scrollPanelStatus.repaint();
        this.client.remove(index);
    }//GEN-LAST:event_btnDeleteActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnPause;
    private javax.swing.JButton btnResume;
    private javax.swing.JPanel pnlFilesStatus;
    private javax.swing.JScrollPane scrollPanelStatus;
    // End of variables declaration//GEN-END:variables

    @Override
    public void notifySelected(FileStatus file) {
        this.btnPause.setEnabled(true);
        this.btnResume.setEnabled(true);
        this.btnDelete.setEnabled(true);
        this.fileSelected = file;
        for (FileStatus p : files) {
            if (!p.equals(file)) {
                p.setSelected(false);
            }
        }
    }

    public void addFileStatus(FileStatus fs) {
        this.pnlFilesStatus.add(fs);
        this.files.add(fs);
        fs.addListenner(this);
        this.scrollPanelStatus.revalidate();
    }

    public void addFileStatus(String name, boolean isDownload) {
        FileStatus p = new FileStatus(name, 0, isDownload);
        this.pnlFilesStatus.add(p);
        this.files.add(p);
        p.addListenner(this);
        this.scrollPanelStatus.revalidate();
    }

    public JProgressBar[] getProgressBars() {
        return this.files.get(files.size() - 1).getProgressBars();
    }

    void notifyComplete(int i) {
        this.files.get(i).nottifyComplete();
    }

    boolean isFileDownload(int i) {
        return this.files.get(i).isDownload();
    }

    void removeAllFileStatus() {
        this.fileSelected = null;
        for (int i = 0; i < files.size(); i++) {
            FileStatus fs = files.get(i);
            this.pnlFilesStatus.remove(fs);
            this.files.remove(fs);
        }
        this.scrollPanelStatus.revalidate();
        this.scrollPanelStatus.repaint();
    }

}
