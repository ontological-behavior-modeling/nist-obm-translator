package edu.gatech.gtri.obm.translator.alloy.userinterface;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Popup {

  private JDialog dialog;
  private Timer t;
  private int dot = 0;

  /**
   * Constructor for the Pop-up.
   * @param phrase - The phrase that appears on the dialog
   */
  public Popup(String phrase) {
    initialize(phrase);
  }

  /**
   * Initialize the contents of the Pop-up dialog.
   * @param phrase - The phrase that appears on the dialog
   */
  private void initialize(String phrase) {
    ImageIcon img = new ImageIcon("images/OBM.png");
    dialog = new JDialog(UserInterface.frmObmAlloyTranslator, phrase);
    dialog.setIconImage(img.getImage());
    dialog.setModalityType(JDialog.ModalityType.MODELESS);;
    dialog.setSize(new Dimension(250,15));
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.toFront();
    dialog.setLocationRelativeTo(UserInterface.frmObmAlloyTranslator);
    dialog.setVisible(true);
    t = new Timer();
    t.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        if (dialog.getTitle().chars().filter(ch -> ch == '.').count() < 3)
          dialog.setTitle(dialog.getTitle() + ".");
        else {
          int length = dialog.getTitle().length();
          dialog.setTitle(dialog.getTitle().substring(0, length-3));
        }
      }
    }, new Date(), 1000);
  }

}