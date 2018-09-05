package net.c0f3.zookeeper;

import org.apache.log4j.Logger;
import org.apache.zookeeper.inspector.logger.LoggerFactory;
import org.apache.zookeeper.inspector.manager.ZooInspectorManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class ZKTreeExportDialogForm extends JDialog {
  private static Logger log = LoggerFactory.getLogger();

  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JComboBox exportFormatSelector;
  private JProgressBar exportProgressBar;
  private JButton selectFolderButton;
  private JTextField destinationFolderPathTextField;
  private JLabel statusLabel;
  private final JFileChooser fc = new JFileChooser();
  // ----------------------------
  private final String connectString;
  private File choosenFile = null;

  public ZKTreeExportDialogForm(String connectString) {
    this.connectString = connectString;
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);

    buttonOK.addActionListener(e -> onOK());
    buttonCancel.addActionListener(e -> onCancel());

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(
        e -> onCancel(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
    );
    selectFolderButton.addActionListener(e -> {
      int returnVal = fc.showOpenDialog(ZKTreeExportDialogForm.this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        choosenFile = fc.getSelectedFile();
        destinationFolderPathTextField.setText(choosenFile.getAbsolutePath());
        log.debug("Opening: " + choosenFile.getName() );
      } else {
        log.debug("Open command cancelled by user.");
      }
    });
  }

  private void onOK() {
    if(exportFormatSelector.getSelectedItem()==null) {
      return;
    }
    String choosenExportFormat = exportFormatSelector.getSelectedItem().toString();
    if(choosenFile==null || choosenExportFormat==null) {
      statusLabel.setText("You have to choose destination file and export format");
      return;
    }
    this.setEnabled(false);
    ZKTreeExport treeExport = new ZKTreeExport(
        choosenExportFormat, choosenFile, connectString
    );
    treeExport.perform(this::dispose);
  }

  private void onCancel() {
    dispose();
  }

  public static void main(String[] args) {
    ZKTreeExportDialogForm dialog = new ZKTreeExportDialogForm("fedora-dev:2181");
    dialog.pack();
    dialog.setVisible(true);
    System.exit(0);
  }

}
