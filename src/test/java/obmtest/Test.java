package obmtest;

import java.io.File;
import edu.gatech.gtri.obm.alloy.translator.OBMXMI2Alloy;

public class Test {

  public static void main(String[] args) {

    String output_and_testfiles_dir = "src/test/resources";

    String ombmodel_dir = "src/test/resources";
    File xmiFile = new File(ombmodel_dir, "OBMModel.xmi");

    OBMXMI2Alloy test = new OBMXMI2Alloy.Builder().alloyLibrary(output_and_testfiles_dir).build();


    String sysMLClassQualifiedName = "Model::4.1 Basic Examples::4.1.1 Time Orderings::AllControl";
    String manualFileName = "4.1.1 Time Orderings - AllControl.als";
    File apiFile = new File(output_and_testfiles_dir, manualFileName + "_Generated" + ".als");
    if (!test.createAlloyFile(xmiFile, sysMLClassQualifiedName, apiFile)) {
      System.out.println(test.getErrorMessages());
    } else
      System.out.println("done1");

    sysMLClassQualifiedName = "Model::4.1 Basic Examples::4.1.1 Time Orderings::Decision";
    manualFileName = "4.1.1 Time Orderings - Decision.als";
    apiFile = new File(output_and_testfiles_dir, manualFileName + "_Generated" + ".als");
    if (!test.createAlloyFile(xmiFile, sysMLClassQualifiedName, apiFile)) {
      System.out.println(test.getErrorMessages());
    } else
      System.out.println("done2");
  }
}
