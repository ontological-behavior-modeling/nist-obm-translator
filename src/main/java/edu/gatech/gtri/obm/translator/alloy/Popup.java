package edu.gatech.gtri.obm.translator.alloy;

import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Popup {

  private JDialog dialog;

  
//  public static void main(String[] args) {
//    Popup p = new Popup("Test");
//  }

  /**
   * Create the application.
   */
  public Popup(String words) {
    initialize(words);
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize(String words) {
    //final JOptionPane optionPane = new JOptionPane("Refreshing, Please Wait...", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
    //JLabel rfsh = new JLabel(words);
    //rfsh.setHorizontalAlignment(SwingConstants.CENTER);
    //rfsh.setVerticalAlignment(SwingConstants.CENTER);
    ImageIcon img = new ImageIcon("images/OBM.png");
    dialog = new JDialog(UserInterface.frmObmAlloyTranslator, words);
    dialog.setIconImage(img.getImage());
    //dialog.setTitle(words);
    dialog.setModalityType(JDialog.ModalityType.MODELESS);;
    //dialog.setContentPane(optionPane);
    dialog.setBounds(new Rectangle(250,20));
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setAlwaysOnTop(true);
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
  }

}


//
//int response = JOptionPane.showOptionDialog(source, "",
//    "Empty?", JOptionPane.DEFAULT_OPTION,
//    JOptionPane.QUESTION_MESSAGE, null, new Object[] {},
//    null);
