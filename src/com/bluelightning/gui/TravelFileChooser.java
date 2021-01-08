/**
 * 
 */
package com.bluelightning.gui;

import java.io.File;
import java.util.ArrayList;

import com.bluelightning.RobautoMain;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author NOOK
 */
public class TravelFileChooser extends javax.swing.JDialog {

    /**
     * Creates new form TravelFileChooser
     */
    public TravelFileChooser(java.awt.Frame parent, String where) {
        super(parent, true);
        initComponents(where);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents(String where) {

        jScrollPane1 = new javax.swing.JScrollPane();
        fileList = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Travel Mode File Chooser");
        setModal(true);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        class RobautoListModel extends javax.swing.AbstractListModel {
             	
     			public RobautoListModel(String where) {
                 	filenames = new ArrayList<>();
                 	directoryBase = where;
	     			File d = new File(directoryBase);
	     			for (String filename : d.list()) {
	     				if (filename.endsWith(".robauto")) {
	     					filenames.add( filename.replace(".robauto", ""));
	     				}
	     			}
     			}
                 public int getSize() { return filenames.size(); }
                 public Object getElementAt(int i) { return filenames.get(i); }
        };
        fileList.setModel(new RobautoListModel(where));
        fileList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                int i = evt.getLastIndex();
                if (i >= 0) {
                	selection = new File(directoryBase, filenames.get(i) + ".robauto");
                	TravelFileChooser.this.dispose();
                }
            }
        });
        jScrollPane1.setViewportView(fileList);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>                        

//    private void fileListValueChanged(javax.swing.event.ListSelectionEvent evt) {                                    
//    }             
    
    public File getSelection() {
    	return selection;
    }

    private File selection;
    private String directoryBase;
    private ArrayList<String> filenames;
    private javax.swing.JList fileList;
    private javax.swing.JScrollPane jScrollPane1;

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(TravelFileChooser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(TravelFileChooser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(TravelFileChooser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(TravelFileChooser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	String where = RobautoMain.getDataPath();
                TravelFileChooser dialog = new TravelFileChooser(new javax.swing.JFrame(), where);
                dialog.setVisible(true);
                System.out.println("after visible: " + dialog.getSelection());
            }
        });
    }
}
