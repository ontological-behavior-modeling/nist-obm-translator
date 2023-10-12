package edu.gatech.gtri.obm.translator.alloy;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JLabel;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.text.*;

import javax.swing.SwingConstants;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JSplitPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.internal.impl.ClassImpl;

import edu.gatech.gtri.obm.translator.alloy.fromxmi.OBMXMI2Alloy;
import edu.umd.omgutil.EMFUtil;
import edu.umd.omgutil.UMLModelErrorException;

import javax.swing.JButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import lombok.Getter;
import lombok.Setter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JCheckBox;
import javax.swing.JDialog;


@Getter
@Setter
/**
 * Creates an interface to select an XMI file and class(es) within that file to translate.
 * 
 */
public class UserInterface {

  public static JFrame frmObmAlloyTranslator;
  private JTextField textField;
  private File xmiFile = null;
  private List<String> mainClass;
  private JButton btnOk;
  private ArrayList<NamedElement> resourceNames;
  private JList<String> list;
  private String[] allClassNames;
  private JLabel lblTop;
  private JSplitPane splitPaneFile;
  private JButton btnOpen;
  private JLabel lblFileName;
  private JScrollPane scrollPane;
  private JLabel wait;
  private final JCheckBox chckbxName = new JCheckBox("Auto-name Output File(s)");
  private final JLabel lblRefresh = new JLabel("Refreshing, Please Wait...");
  private boolean refresh = true;

  /**
   * Launch the application.
   * @throws UMLModelErrorException 
   * @throws FileNotFoundException 
   */
  public static void main(String[] args) throws FileNotFoundException, UMLModelErrorException {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          new UserInterface();
          frmObmAlloyTranslator.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the application.
   */
  public UserInterface() {
    try {
      initialize();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Initialize the contents of the frame.
   * @throws FileNotFoundException 
   */
  private void initialize() throws FileNotFoundException {
    frmObmAlloyTranslator = new JFrame();
    frmObmAlloyTranslator.setTitle("OBM Alloy Translator");
    frmObmAlloyTranslator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    ImageIcon img = new ImageIcon("images/OBM.png");
    frmObmAlloyTranslator.setIconImage(img.getImage());
    
    lblTop = new JLabel("Select Class to Translate");
    lblTop.setBounds(20, 47, 249, 27);
    lblTop.setHorizontalAlignment(SwingConstants.CENTER);
    lblTop.setFont(new Font("Tahoma", Font.BOLD, 20));
    
    scrollPane = new JScrollPane();
    scrollPane.setBounds(20, 85, 300, 536);
    lblTop.setLabelFor(scrollPane);
    
    list = new JList<String>();
    scrollPane.setViewportView(list);
    
    btnOk = new JButton("OK");
    btnOk.setBounds(53, 656, 75, 23);
    btnOk.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (list.getSelectedValue() != null) {
          Popup p = new Popup("Generating File...");
          int i = 1;
          OBMXMI2Alloy obm = null;
          try {
            obm = new OBMXMI2Alloy("src/test/resources");
          } catch (FileNotFoundException | UMLModelErrorException e1) {
            e1.printStackTrace();
          }
          List<String> mainClass = list.getSelectedValuesList();
          int fileNum = mainClass.size();
          String fileList = "File(s) Created";
            for (String c : mainClass) {
              p.getDialog().setTitle("Generating File " + i + "/" + fileNum);
              p.getDialog().setVisible(true);
              File alsFile = null;
              if(!chckbxName.isSelected()) {
              try {
                alsFile = saveALS(c);
                if (alsFile != null) {
                  obm.createAlloyFile(xmiFile, c, alsFile);
                } else {
                  JOptionPane jop = new JOptionPane();
                  jop.setMessageType(JOptionPane.PLAIN_MESSAGE);
                  jop.setMessage("Please Enter a File Name");
                  JOptionPane.showMessageDialog(jop, null);
                }
                  
              } catch (FileNotFoundException | UMLModelErrorException e1) {
                e1.printStackTrace();
              }       
            } else {
              String location = xmiFile.getAbsolutePath();
              location = location.substring(0, location.lastIndexOf("\\") + 1);
              String name = c.substring(c.lastIndexOf(":") + 1);
              
              DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
              Date date = new Date();
              String dt = dateFormat.format(date);
              alsFile = new File(location + name + "_" + dt + ".als");
              try {
               obm.createAlloyFile(xmiFile, c, alsFile);
             } catch (FileNotFoundException | UMLModelErrorException e1) {
               e1.printStackTrace();
             }
            }
            p.getDialog().setVisible(false);
            fileList = fileList + "\n" + alsFile.getAbsolutePath();   
            i++;
          }
            JOptionPane.showMessageDialog(null, fileList);  
        } else {
          JOptionPane.showMessageDialog(frmObmAlloyTranslator, "Please Select One or More Options \n from the List");
        }
      }
      
    });
    
    JButton btnCancel = new JButton("Cancel");
    btnCancel.setBounds(216, 656, 75, 23);
    btnCancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
          System.exit(0);
      }});
    
    JSplitPane splitPaneSearch = new JSplitPane();
    splitPaneSearch.setContinuousLayout(true);
    scrollPane.setColumnHeaderView(splitPaneSearch);
    splitPaneSearch.setDividerSize(0);
    
    textField = new JTextField();
    textField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        String text = textField.getText();
        searchList(text);
      }
    });
    splitPaneSearch.setRightComponent(textField);
    textField.setColumns(10);
    
    JLabel lblSearch = new JLabel("Search: ");
    lblSearch.setFont(new Font("Tahoma", Font.BOLD, 15));
    splitPaneSearch.setLeftComponent(lblSearch);
    frmObmAlloyTranslator.getContentPane().setLayout(null);
    frmObmAlloyTranslator.getContentPane().add(lblTop);
    frmObmAlloyTranslator.getContentPane().add(scrollPane);
    frmObmAlloyTranslator.getContentPane().add(btnOk);
    frmObmAlloyTranslator.getContentPane().add(btnCancel);
    
    ImageIcon icon = new ImageIcon("images/refresh.png");
    Image imgR = icon.getImage() ;  
    Image newimg = imgR.getScaledInstance( 20, 20,  Image.SCALE_SMOOTH ) ;  
    icon = new ImageIcon( newimg );
    JButton btnRefresh = new JButton(icon);
    btnRefresh.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        File newXmiFile = new File(xmiFile.getAbsolutePath());
        xmiFile = newXmiFile;
        Popup p = new Popup("Refreshing, Please Wait...");
        Future<String[]> future = executor.submit(callableTask);
        try {
          allClassNames = future.get();
        } catch (InterruptedException | ExecutionException e1) {
          e1.printStackTrace();
        }
        list.setListData(allClassNames);
        p.getDialog().setVisible(false);
      }
    });
    
    btnRefresh.setBounds(292, 47, 28, 27);
    frmObmAlloyTranslator.getContentPane().add(btnRefresh);
    
    splitPaneFile = new JSplitPane();
    splitPaneFile.setBounds(10, 9, 317, 27);
    splitPaneFile.setDividerSize(0);
    frmObmAlloyTranslator.getContentPane().add(splitPaneFile);
    
    btnOpen = new JButton("Open XMI File");
    btnOpen.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        boolean b = chooseXMI();
        if (b) {
          String path = xmiFile.getAbsolutePath();
          String sub = path.substring(path.lastIndexOf("\\"));
  //        if (path.length() > 70) {
  //          sub = path.substring(path.length() - 35);
  //        } else if (path.length() > 35) {
  //          int remove = path.length() - 35;
  //          sub = path.substring(remove);
  //        }
          Popup p = new Popup("Loading File...");
          Future<String[]> future = executor.submit(callableTask);
          try {
            allClassNames = future.get();
          } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
          }
          list.setListData(allClassNames);
          btnOpen.setText("Open New XMI");
          splitPaneFile.setLeftComponent(btnOpen);
          lblFileName.setText(" ..." + sub);
          lblTop.setVisible(true);
          btnOk.setVisible(true);
          btnCancel.setVisible(true);
          btnRefresh.setVisible(true);
          chckbxName.setVisible(true);
          frmObmAlloyTranslator.setBounds(0,0,350, 725);
          frmObmAlloyTranslator.setLocationRelativeTo(null);
          scrollPane.setVisible(true);
          p.getDialog().setVisible(false);
        }
      }
    });
    splitPaneFile.setLeftComponent(btnOpen);
    
    lblFileName = new JLabel("  No File Selected");
    splitPaneFile.setRightComponent(lblFileName);
    chckbxName.setHorizontalAlignment(SwingConstants.CENTER);
    chckbxName.setBounds(0, 628, 334, 23);
    frmObmAlloyTranslator.getContentPane().add(chckbxName);
    lblRefresh.setHorizontalAlignment(SwingConstants.CENTER);
    lblRefresh.setBounds(0, 343, 334, 31);
    frmObmAlloyTranslator.getContentPane().add(lblRefresh);
    chckbxName.setVisible(false);
    lblTop.setVisible(false);
    scrollPane.setVisible(false);
    btnOk.setVisible(false);
    btnCancel.setVisible(false);
    btnRefresh.setVisible(false);
    frmObmAlloyTranslator.setBounds(0,0,350, 150);
    frmObmAlloyTranslator.setLocationRelativeTo(null);
    frmObmAlloyTranslator.setVisible(true);
  }
  
  /**
   * Choose XMI File to translate.
   */
  private boolean chooseXMI() {
    JFileChooser j = new JFileChooser();
    j.setFileFilter(new FileNameExtensionFilter("XMI Files", "xmi"));
    j.setDialogTitle("Choose model .xmi file"); 
    int result;
    if (xmiFile == null) {
      result = j.showOpenDialog(null);
    } else {
      j.setCurrentDirectory(xmiFile);
      result = j.showOpenDialog(null);
    }
    if (result == JFileChooser.APPROVE_OPTION) {
      File selected = j.getSelectedFile();
      xmiFile = selected;
      return true;
  } else {
    return false;
  }
  }
  
  /**
   * Enter a filename and choose the location to save the translated .als file.
   */ 
  private static File saveALS(String name) {
    JFileChooser jSave = new JFileChooser();
    jSave.setDialogTitle("Save your ALS file for " + name);
    jSave.setFileFilter(new FileNameExtensionFilter("ALS Files", "als"));
    int i = jSave.showSaveDialog(frmObmAlloyTranslator);
    if (i == JFileChooser.APPROVE_OPTION) {
      try {
        File saved = jSave.getSelectedFile();
        if (FilenameUtils.getExtension(saved.getName()).equalsIgnoreCase("als")) {
        } else {
            //saved = new File(saved.toString() + ".als");  // append .als
            saved = new File(saved.getParentFile(), FilenameUtils.getBaseName(saved.getName())+ ".als"); // ALTERNATIVELY: remove the extension (if any) and replace it with ".als"
        }
        return saved;
      } catch(NullPointerException e) {
        //e.printStackTrace();
        return null;
      }
    } else if (i == JFileChooser.CANCEL_OPTION) {
      return null;
    } else {
      return null;
    }
  }
  
  /**
   * Search through the list of all model classes for the text in the search JTextField.
   */
  private void searchList(String field) {
    List<String> updatedList = new ArrayList<String>();
    for (String s : allClassNames) {
      if ((s.toLowerCase()).contains((field.toLowerCase())))
        updatedList.add(s);
    }

    int size = updatedList.size();
    String[] classNames = new String[size];
    for (int i = 0; i < size; i++) {
      classNames[i] = updatedList.get(i);
    }  
    list.setListData(classNames);
  }
  ExecutorService executor = Executors.newFixedThreadPool(5);
  
  Callable<String[]> callableTask = () -> {
    ResourceSet rs = null;
    try {
      rs = EMFUtil.createResourceSet();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } 
    Resource r = EMFUtil.loadResourceWithDependencies(rs, URI.createFileURI(xmiFile.getAbsolutePath()), null);
    
    resourceNames = new ArrayList<NamedElement>();
    TreeIterator<EObject> xmiContent = r.getAllContents();
    while(xmiContent.hasNext()) {
      EObject current = xmiContent.next();
      if(current.getClass().equals(ClassImpl.class)) {
        resourceNames.add((ClassImpl) current);
      }
    }
    int size = resourceNames.size();
    String[] classNames = new String[size];
    for (int i = 0; i < size; i++) {
      classNames[i] = resourceNames.get(i).getQualifiedName();
    }  
    Arrays.sort(classNames);
    return classNames;
  };
  
  Callable<JDialog> callablePopup = () -> {
      Popup pop = new Popup("Refreshing, Please Wait...");
      pop.getDialog().setVisible(true);
      return pop.getDialog();
  };
}
