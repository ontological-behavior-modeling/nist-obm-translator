package edu.gatech.gtri.obm.translator.alloy;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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


@Getter
@Setter
/**
 * Creates an interface to select an XMI file and class(es) within that file to translate.
 * 
 */
public class UserInterface {

  public static JFrame frmObmAlloyTranslator;
  private JTextField textField;
  private File xmiFile;
  private List<String> mainClass;
  private JButton btnOk;
  private ArrayList<NamedElement> resourceNames;
  private JList<String> list;
  private String[] allClassNames;
  private JLabel lblTop;

  /**
   * Launch the application.
   * @throws UMLModelErrorException 
   * @throws FileNotFoundException 
   */
  public static void main(String[] args) throws FileNotFoundException, UMLModelErrorException {
    OBMXMI2Alloy test = new OBMXMI2Alloy();
    UserInterface ui = new UserInterface();
    ui.getBtnOk().addActionListener(new ActionListener(){
       @Override
       public void actionPerformed(ActionEvent e) {
         if (ui.getList().getSelectedValue() != null) {
           List<String> mainClass = ui.getList().getSelectedValuesList();
           File xmiFile = ui.getXmiFile();
           for (String c : mainClass) {
             try {
               File alsFile = saveALS();
               test.createAlloyFile(xmiFile, c, alsFile);
             } catch (FileNotFoundException | UMLModelErrorException e1) {
               e1.printStackTrace();
             }            
             
           }
         } else {
           JOptionPane.showMessageDialog(frmObmAlloyTranslator, "Please Select One or More Options \n from the List");
         }
       }
    });

  }

  /**
   * Create the application.
   */
  public UserInterface() {
    //TODO xmiFile = chooseXMI();
    xmiFile = new File("src/test/resources/OBMModel_R.xmi");
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
    frmObmAlloyTranslator.setBounds(100, 100, 336, 646);
    frmObmAlloyTranslator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    ImageIcon img = new ImageIcon("images/OBM.png");
    frmObmAlloyTranslator.setIconImage(img.getImage());
    
    lblTop = new JLabel("Select Class to Translate");
    lblTop.setBounds(10, 4, 249, 27);
    lblTop.setHorizontalAlignment(SwingConstants.CENTER);
    lblTop.setFont(new Font("Tahoma", Font.BOLD, 20));
    
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setBounds(13, 33, 300, 536);
    lblTop.setLabelFor(scrollPane);
    
    getModelElements();
    list = new JList<String>(allClassNames);
    scrollPane.setViewportView(list);
    
    btnOk = new JButton("OK");
    btnOk.setBounds(46, 573, 75, 23);
    btnOk.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (list.getSelectedValue() != null)
        mainClass = list.getSelectedValuesList();
      }
    });
    
    JButton btnCancel = new JButton("Cancel");
    btnCancel.setBounds(191, 573, 75, 23);
    btnCancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
          System.exit(0);
      }});
    
    JSplitPane splitPane = new JSplitPane();
    splitPane.setContinuousLayout(true);
    scrollPane.setColumnHeaderView(splitPane);
    splitPane.setDividerSize(0);
    
    textField = new JTextField();
    textField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        String text = textField.getText();
        searchList(text);
      }
    });
    splitPane.setRightComponent(textField);
    textField.setColumns(10);
    
    JLabel lblSearch = new JLabel("Search: ");
    lblSearch.setFont(new Font("Tahoma", Font.BOLD, 15));
    splitPane.setLeftComponent(lblSearch);
    frmObmAlloyTranslator.getContentPane().setLayout(null);
    frmObmAlloyTranslator.getContentPane().add(lblTop);
    frmObmAlloyTranslator.getContentPane().add(scrollPane);
    frmObmAlloyTranslator.getContentPane().add(btnOk);
    frmObmAlloyTranslator.getContentPane().add(btnCancel);
    
    ImageIcon rfsh = new ImageIcon("images/refresh.png");
    JButton btnNewButton = new JButton(rfsh);
    btnNewButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        File newXmiFile = new File(xmiFile.getAbsolutePath());
        xmiFile = newXmiFile;
        try {
          getModelElements();
        } catch (FileNotFoundException e1) {
          e1.printStackTrace();
        }
        list.setListData(allClassNames);
      }
    });
    
    btnNewButton.setBounds(286, 8, 27, 23);
    frmObmAlloyTranslator.getContentPane().add(btnNewButton);
    frmObmAlloyTranslator.setLocationRelativeTo(null);
    frmObmAlloyTranslator.setVisible(true);
  }
  
  /**
   * Get all Class Elements from the selected XMI File
   */
  private void getModelElements() throws FileNotFoundException {
    ResourceSet rs = EMFUtil.createResourceSet(); 
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
    allClassNames = new String[size];
    for (int i = 0; i < size; i++) {
      allClassNames[i] = resourceNames.get(i).getQualifiedName();
    }  
    Arrays.sort(allClassNames);
  }
  
  /**
   * Choose XMI File to translate.
   */
  private File chooseXMI() {
    JFileChooser j = new JFileChooser();
    File dir = new File("C:/Users/jnapolitano7/Documents/GitHub/CastingError.bkpt");
    System.setProperty("user.dir", dir.getAbsolutePath());
    j.setFileFilter(new FileNameExtensionFilter("XMI Files", "xmi"));
    j.setDialogTitle("Choose model .xmi file");  
    j.showOpenDialog(null);
    j.setCurrentDirectory(dir);
    File selected = j.getSelectedFile();
    
    return selected;
  }
  
  /**
   * Enter a filename and choose the location to save the translated .als file.
   */ 
  private static File saveALS() {
    JFileChooser jSave = new JFileChooser();
    jSave.setDialogTitle("Save your ALS file");
    jSave.setFileFilter(new FileNameExtensionFilter("ALS Files", "als"));
    jSave.showSaveDialog(frmObmAlloyTranslator);
    File saved = jSave.getSelectedFile();
    if (FilenameUtils.getExtension(saved.getName()).equalsIgnoreCase("als")) {
  } else {
      saved = new File(saved.toString() + ".als");  // append .als
      saved = new File(saved.getParentFile(), FilenameUtils.getBaseName(saved.getName())+".als"); // ALTERNATIVELY: remove the extension (if any) and replace it with ".xml"
  }
    return saved;
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

}
