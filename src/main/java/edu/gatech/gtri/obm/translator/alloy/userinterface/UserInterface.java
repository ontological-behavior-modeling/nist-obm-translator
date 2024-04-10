package edu.gatech.gtri.obm.translator.alloy.userinterface;

import edu.gatech.gtri.obm.translator.alloy.fromxmi.OBMXMI2Alloy;
import edu.umd.omgutil.EMFUtil;
import edu.umd.omgutil.UMLModelErrorException;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.internal.impl.ClassImpl;

// TODO: Auto-generated Javadoc
/** The Class UserInterface. */
@Getter
@Setter
/** Creates an interface to select an XMI file and class(es) within that file to translate. */
public class UserInterface {

  /** The Overall JFrame. */
  public static JFrame frmObmAlloyTranslator;

  /** The bottom panel/section of the GUI. */
  private final JPanel btmPanel = new JPanel();

  /** The top panel/section of the GUI. */
  private final JPanel topPanel = new JPanel();

  /** The bottom buttons in the GUI (OK and Cancel). */
  private JPanel btmButtons = new JPanel();

  /** The top title text bar. */
  private JPanel topTitle;

  /** The text field for searching. */
  private JTextField textField;

  /** The current xmi file. */
  private File xmiFile = null;

  /** The main class(es) chosen in the GUI. */
  private List<String> mainClass;

  /** The OK button. */
  private JButton btnOk;

  /** All of the resource names from the xmi file. */
  private ArrayList<NamedElement> resourceNames;

  /** The GUI component of the list of resource names. */
  private JList<String> list;

  /** All the class names from the xmi file. */
  private String[] allClassNames;

  /** The label at the top of the GUI. */
  private JLabel lblTop;

  /** The split pane component for the search bar. */
  private JSplitPane splitPaneFile;

  /** The button to open the file directory. */
  private JButton btnOpen;

  /** The label component to show the current xmi file. */
  private JLabel lblFileName;

  /** The scroll pane component showing the list of classes. */
  private JScrollPane scrollPane;

  /** The cancel button. */
  private JButton btnCancel;

  /** The refresh button. */
  private JButton btnRefresh;

  /** The checkbox for auto-naming output files. */
  private final JCheckBox chckbxName = new JCheckBox("Auto-name Output File(s)");

  /** The refresh. */
  private boolean refresh = true;

  /** The executor to run the translator functions in the background. */
  private ExecutorService executor = Executors.newFixedThreadPool(10);

  /**
   * A callable method to conglomerate all the class elements within the xmi file.
   *
   * @return classNames A string array of the xmi file class elements
   */
  private Callable<String[]> findXmiClasses =
      () -> {
        ResourceSet rs = null;
        try {
          rs = EMFUtil.createResourceSet();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
        Resource r =
            EMFUtil.loadResourceWithDependencies(
                rs, URI.createFileURI(xmiFile.getAbsolutePath()), null);

        resourceNames = new ArrayList<NamedElement>();
        TreeIterator<EObject> xmiContent = r.getAllContents();
        while (xmiContent.hasNext()) {
          EObject current = xmiContent.next();
          if (current.getClass().equals(ClassImpl.class)) {
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
   *
   * @param args the arguments
   * @throws FileNotFoundException the file not found exception
   * @throws UMLModelErrorException the UML model error exception
   */
  public static void main(String[] args) throws FileNotFoundException, UMLModelErrorException {
    try {
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch (ClassNotFoundException
        | InstantiationException
        | IllegalAccessException
        | UnsupportedLookAndFeelException e) {
      e.printStackTrace();
    }
    EventQueue.invokeLater(
        new Runnable() {
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

  /** Create the User Interface application. */
  public UserInterface() {
    try {
      initialize();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Initialize the contents of the frame.
   *
   * @throws FileNotFoundException the file not found exception
   */
  private void initialize() throws FileNotFoundException {
    frmObmAlloyTranslator = new JFrame();

    frmObmAlloyTranslator.setTitle("OBM Alloy Translator");
    frmObmAlloyTranslator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    ImageIcon img = new ImageIcon(getImage("OBM.png"));
    frmObmAlloyTranslator.setIconImage(img.getImage());
    lblTop = new JLabel("Select File to Translate");
    lblTop.setHorizontalAlignment(SwingConstants.CENTER);
    lblTop.setFont(new Font("Tahoma", Font.BOLD, 20));

    scrollPane = new JScrollPane();

    list = new JList<String>();
    list.setFont(new Font("Tahoma", Font.PLAIN, 16));
    scrollPane.setViewportView(list);

    JSplitPane splitPaneSearch = new JSplitPane();
    splitPaneSearch.setContinuousLayout(true);
    scrollPane.setColumnHeaderView(splitPaneSearch);
    splitPaneSearch.setDividerSize(0);

    textField = new JTextField();
    textField.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyReleased(KeyEvent e) {
            String text = textField.getText();
            searchList(text);
          }
        });
    splitPaneSearch.setRightComponent(textField);
    textField.setColumns(10);

    JLabel lblSearch = new JLabel("Search: ");
    lblSearch.setFont(new Font("Tahoma", Font.BOLD, 16));
    splitPaneSearch.setLeftComponent(lblSearch);
    chckbxName.setFont(new Font("Tahoma", Font.PLAIN, 16));

    chckbxName.setHorizontalAlignment(SwingConstants.CENTER);

    splitPaneFile = new JSplitPane();
    splitPaneFile.setDividerSize(0);

    btnOpen = new JButton("Open XMI File");
    btnOpen.setFont(new Font("Tahoma", Font.PLAIN, 16));
    btnOpen.addMouseListener(
        new MouseAdapter() {
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
              list.setListData(allClassNames);
              btnOpen.setText("Open New XMI");
              lblTop.setText("Select Class to Translate");
              splitPaneFile.setLeftComponent(btnOpen);
              lblFileName.setText(" ..." + sub);
              lblTop.setVisible(true);
              btmPanel.setVisible(true);
              btnRefresh.setVisible(true);
              scrollPane.setVisible(true);
              frmObmAlloyTranslator.setMinimumSize(new Dimension(530, 450));
              frmObmAlloyTranslator.pack();
              p.getDialog().dispose();
            }
          }
        });
    splitPaneFile.setLeftComponent(btnOpen);

    lblFileName = new JLabel("  No File Selected");
    lblFileName.setFont(new Font("Tahoma", Font.PLAIN, 16));
    splitPaneFile.setRightComponent(lblFileName);

    ImageIcon icon = new ImageIcon(getImage("refresh.png"));
    btnRefresh = new JButton(icon);
    btnRefresh.addMouseListener(
        new MouseAdapter() {
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
    btnCancel.setFont(new Font("Tahoma", Font.PLAIN, 16));
    btnCancel.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            System.exit(0);
          }
        });

    btnOk = new JButton("OK");
    btnOk.setFont(new Font("Tahoma", Font.PLAIN, 16));
    btnOk.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            String location = xmiFile.getAbsolutePath();
            int slash = location.lastIndexOf("\\");
            String path = xmiFile.getAbsolutePath().substring(0, slash);
            if (list.getSelectedValue() != null) {
              File transfer = new File(path + "/Transfer.als");
              File relation = new File(path + "/utilities/types/relation.als");
              if (!(transfer.exists() && relation.exists())) {
                copyResources(transfer, relation);
              }
              Popup p = new Popup("Generating File");
              int i = 1;
              OBMXMI2Alloy obm = null;
              try {
                obm = new OBMXMI2Alloy(path);
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
                if (!chckbxName.isSelected()) {
                  try {
                    alsFile = saveALS(c);
                    if (alsFile == null) {
                      JOptionPane.showMessageDialog(
                          frmObmAlloyTranslator,
                          "No File Name Selected. Alloy file generation cancelled");
                    } else obm.createAlloyFile(xmiFile, c, alsFile);
                  } catch (FileNotFoundException
                      | UMLModelErrorException
                      | NullPointerException e1) {
                    JOptionPane.showMessageDialog(
                        frmObmAlloyTranslator,
                        "Selected XMI file does not exist.\nTranslation Canceled.");
                  }
                } else {
                  location = location.substring(0, slash + 1);
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
              JOptionPane.showMessageDialog(frmObmAlloyTranslator, fileList);
            } else {
              JOptionPane.showMessageDialog(
                  frmObmAlloyTranslator, "Please Select One or More Options\nfrom the List");
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
   *
   * @return true, if successful
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
   *
   * @param name A string of the xmi file class name that is being saved
   * @return the file
   */
  private File saveALS(String name) {
    JFileChooser jSave = new JFileChooser();
    jSave.setDialogTitle("Save your ALS file for " + name);
    jSave.setFileFilter(new FileNameExtensionFilter("ALS Files", "als"));
    jSave.setCurrentDirectory(xmiFile);
    int i = jSave.showSaveDialog(frmObmAlloyTranslator);
    if (i == JFileChooser.APPROVE_OPTION) {
      try {
        File saved = jSave.getSelectedFile();
        if (FilenameUtils.getExtension(saved.getName()).equalsIgnoreCase("als")) {
        } else {
          // saved = new File(saved.toString() + ".als");  // append .als
          saved =
              new File(
                  saved.getParentFile(),
                  FilenameUtils.getBaseName(saved.getName())
                      + ".als"); // ALTERNATIVELY: remove the extension (if any) and replace it with
          // ".als"
        }
        return saved;
      } catch (NullPointerException e) {
        // e.printStackTrace();
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
   *
   * @param field the text currently in the search field
   */
  private void searchList(String field) {
    List<String> updatedList = new ArrayList<String>();
    for (String s : allClassNames) {
      if ((s.toLowerCase()).contains((field.toLowerCase()))) updatedList.add(s);
    }

    int size = updatedList.size();
    String[] classNames = new String[size];
    for (int i = 0; i < size; i++) {
      classNames[i] = updatedList.get(i);
    }
    list.setListData(classNames);
  }

  /**
   * Gets the image for use in a .jar.
   *
   * @param pathAndFileName the path and file name
   * @return the image
   */
  public static Image getImage(final String pathAndFileName) {
    final URL url = Thread.currentThread().getContextClassLoader().getResource(pathAndFileName);
    return Toolkit.getDefaultToolkit().getImage(url);
  }

  /**
   * Add necessary .als resources to xmiFile location
   *
   * @param transfer A file for the location where Transfer.als should be
   * @param relation A file for the location where relation.als should be
   */
  private void copyResources(File transfer, File relation) {
    boolean t = false;
    boolean r = false;
    if (!transfer.exists()) {
      URL transferUrl = Thread.currentThread().getContextClassLoader().getResource("Transfer.als");
      try {
        FileUtils.copyURLToFile(transferUrl, transfer);
      } catch (IOException e) {
        JOptionPane.showMessageDialog(frmObmAlloyTranslator, "Error finding file\n" + e);
      }
      t = true;
    }
    if (!relation.exists()) {
      URL relUrl =
          Thread.currentThread()
              .getContextClassLoader()
              .getResource("utilities/types/relation.als");
      try {
        FileUtils.copyURLToFile(relUrl, relation);
      } catch (IOException e) {
        JOptionPane.showMessageDialog(frmObmAlloyTranslator, "Error finding file\n" + e);
      }
      r = true;
    }
    if (t && r) {
      JOptionPane.showMessageDialog(
          frmObmAlloyTranslator,
          "Transfer.als and utilities/types/relation.als have " + "been created in your directory");
    } else if (t) {
      JOptionPane.showMessageDialog(
          frmObmAlloyTranslator, "Transfer.als has " + "been created in your directory");
    } else if (r) {
      JOptionPane.showMessageDialog(
          frmObmAlloyTranslator,
          "utilities/types/relation.als has " + "been created in your directory");
    }
  }
}
