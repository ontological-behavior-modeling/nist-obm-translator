package edu.gatech.gtri.obm.translator.alloy.userinterface;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
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
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
/**
 * Creates an interface to select an XMI file and class(es) within that file to translate.
 * 
 */
public class UserInterface {

  public static JFrame frmObmAlloyTranslator;
  private final JPanel btmPanel = new JPanel();
  private final JPanel topPanel = new JPanel();
  private JPanel btmButtons = new JPanel();
  private JPanel topTitle;
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
  private JButton btnCancel;
  private JButton btnRefresh;
  private final JCheckBox chckbxName = new JCheckBox("Auto-name Output File(s)");
  private boolean refresh = true;
  
  private ExecutorService executor = Executors.newFixedThreadPool(10);
  
  private Callable<String[]> findXmiClasses = () -> {
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

  /**
   * Launch the application.
   * @throws UMLModelErrorException 
   * @throws FileNotFoundException 
   */
  public static void main(String[] args) throws FileNotFoundException, UMLModelErrorException {
    try {
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
        | UnsupportedLookAndFeelException e) {
      e.printStackTrace();
    }
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
    
    ImageIcon img = new ImageIcon(getImage("OBM.png"));
    frmObmAlloyTranslator.setIconImage(img.getImage());
    
//    try {
//      obm = new OBMXMI2Alloy("src/main/resources");
//    } catch (FileNotFoundException | UMLModelErrorException e) {
//      e.printStackTrace();
//    }
    
    lblTop = new JLabel("Select File to Translate");
    lblTop.setHorizontalAlignment(SwingConstants.CENTER);
    lblTop.setFont(new Font("Tahoma", Font.BOLD, 20));
    
    scrollPane = new JScrollPane();
    
    list = new JList<String>();
    scrollPane.setViewportView(list);
    
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
    
    chckbxName.setHorizontalAlignment(SwingConstants.CENTER);
    
    splitPaneFile = new JSplitPane();
    splitPaneFile.setDividerSize(0);
    
    btnOpen = new JButton("Open XMI File");
    btnOpen.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        boolean b = chooseXMI();
        if (b) {
          String path = xmiFile.getAbsolutePath();
          String sub = path.substring(path.lastIndexOf("\\"));
          Popup p = new Popup("Loading File");
          Future<String[]> future = executor.submit(findXmiClasses);
          try {
            allClassNames = future.get();
          } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
          }
          List<String> listClassNames = Arrays.asList(allClassNames);
          String longest = listClassNames.stream().max(Comparator.comparingInt(String::length)).get();
          AffineTransform affinetransform = new AffineTransform();     
          FontRenderContext frc = new FontRenderContext(affinetransform,true,true);     
          Font font = new Font("Tahoma", Font.BOLD, 12);
          int textwidth = (int)(font.getStringBounds(longest, frc).getWidth());
          list.setListData(allClassNames);
          btnOpen.setText("Open New XMI");
          lblTop.setText("Select Class to Translate");
          splitPaneFile.setLeftComponent(btnOpen);
          lblFileName.setText(" ..." + sub);
          lblTop.setVisible(true);
          btmPanel.setVisible(true);
          btnRefresh.setVisible(true);
          scrollPane.setVisible(true);
          frmObmAlloyTranslator.setMinimumSize(new Dimension(textwidth + 20, 450));
          frmObmAlloyTranslator.pack();
          p.getDialog().dispose();
        }
      }
    });
    splitPaneFile.setLeftComponent(btnOpen);
    
    lblFileName = new JLabel("  No File Selected");
    splitPaneFile.setRightComponent(lblFileName);
  
    ImageIcon icon = new ImageIcon(getImage("refresh.png"));
    btnRefresh = new JButton(icon);
    btnRefresh.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        File newXmiFile = new File(xmiFile.getAbsolutePath());
        xmiFile = newXmiFile;
        Popup p = new Popup("Refreshing, Please Wait");
        Future<String[]> future = executor.submit(findXmiClasses);
        try {
          allClassNames = future.get();
        } catch (InterruptedException | ExecutionException e1) {
          e1.printStackTrace();
        }
        list.setListData(allClassNames);
        p.getDialog().dispose();
      }
    });
    
    btnCancel = new JButton("Cancel");
    btnCancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
          System.exit(0);
      }});
    
    btnOk = new JButton("OK");
    btnOk.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String location = xmiFile.getAbsolutePath();
        int slash = location.lastIndexOf("\\");
        String path = xmiFile.getAbsolutePath().substring(0, slash);
        copyResources(path);
        if (list.getSelectedValue() != null) {
          Popup p = new Popup("Generating File");
          int i = 1;
          OBMXMI2Alloy obm = null;
          try {
            obm = new OBMXMI2Alloy(path + "/OBM Alloy Resources");
          } catch (FileNotFoundException | UMLModelErrorException e1) {
            e1.printStackTrace();
            p.getDialog().setTitle("Error" + e1);
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
                if (alsFile == null) {
                  JOptionPane.showMessageDialog(frmObmAlloyTranslator, "No File Name Selected. Alloy file generation canceled");
                  
                } else
                  obm.createAlloyFile(xmiFile, c, alsFile);                 
              } catch (FileNotFoundException | UMLModelErrorException | NullPointerException e1) {
                JOptionPane.showMessageDialog(frmObmAlloyTranslator, "File name not entered. \nTranslation Canceled.");
                break;
              }       
            } else {
              location = location.substring(0, slash + 1);
              String name = c.substring(c.lastIndexOf(":") + 1);
              
              DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
              Date date = new Date();
              String dt = dateFormat.format(date);
              alsFile = new File(location + "OBM Alloy Resources/" + name + "_" + dt + ".als");
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
          JOptionPane.showMessageDialog(frmObmAlloyTranslator, "Please Select One or More Options\nfrom the List");
        }
      }
      
    });
  
        
    topTitle = new JPanel();
    topTitle.setLayout(new FlowLayout(1, 20, 5));
    topTitle.add(lblTop);
    topTitle.add(btnRefresh);
    topPanel.setLayout(new BorderLayout());
    topPanel.add(topTitle, BorderLayout.PAGE_START);
    topPanel.add(splitPaneFile, BorderLayout.PAGE_END);
    frmObmAlloyTranslator.getContentPane().add(topPanel, BorderLayout.PAGE_START);
    
    frmObmAlloyTranslator.getContentPane().add(scrollPane, BorderLayout.CENTER);
    
    btmPanel.setLayout(new BorderLayout(15, 5));
    btmPanel.add(chckbxName, BorderLayout.PAGE_START);
    btmButtons.setLayout(new FlowLayout(1, 80, 5));
    btmButtons.add(btnOk, BorderLayout.LINE_START);
    btmButtons.add(btnCancel, BorderLayout.LINE_END);
    btmPanel.add(btmButtons, BorderLayout.PAGE_END);
    frmObmAlloyTranslator.getContentPane().add(btmPanel, BorderLayout.PAGE_END);
    
    btmPanel.setVisible(false);
    btnRefresh.setVisible(false);
    scrollPane.setVisible(false);
    frmObmAlloyTranslator.pack();
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
      result = j.showOpenDialog(frmObmAlloyTranslator);
    } else {
      j.setCurrentDirectory(xmiFile);
      result = j.showOpenDialog(frmObmAlloyTranslator);
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
  
  public static Image getImage(final String pathAndFileName) {
    final URL url = Thread.currentThread().getContextClassLoader().getResource(pathAndFileName);
    return Toolkit.getDefaultToolkit().getImage(url);
}
  
  /**
   * Add necessary .als resources to xmiFile location
   */
  private void copyResources(String path) {
    path = path + "/OBM Alloy Resources";
    File alloyOBM = new File(path);
    alloyOBM.mkdir();
    URL transferUrl = Thread.currentThread().getContextClassLoader().getResource("Transfer.als");
    File transferDest = new File(path + "/Transfer.als");
    URL relUrl = Thread.currentThread().getContextClassLoader().getResource("utilities/types/relation.als");
    File relDest = new File(path + "/utilities/types/relation.als");
    try {
      FileUtils.copyURLToFile(transferUrl, transferDest);
      FileUtils.copyURLToFile(relUrl, relDest);
    } catch (IOException e) {
      JOptionPane.showMessageDialog(frmObmAlloyTranslator, "Error finding file\n" + e);
    }

  }
}
