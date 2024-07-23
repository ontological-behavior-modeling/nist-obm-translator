package obmtest;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import edu.gatech.gtri.obm.alloy.translator.OBMXMI2Alloy;
import edu.umd.omgutil.UMLModelErrorException;

/**
 * A sample usages of OBMXMI2Alloy translator.
 * 
 * @author Wilson Miyako, ASDL, Georgia Tech
 */
public class SampleTranslatorUsage {

  public static void main(String[] args) throws FileNotFoundException, UMLModelErrorException {


    LocalDateTime start = LocalDateTime.now();

    String output_and_testfiles_dir = "src/test/resources";

    String ombmodel_dir = "src/test/resources";
    File xmiFile = new File(ombmodel_dir, "OBMModel.xmi");

    // create OBMXMI2Alloy object
    OBMXMI2Alloy translator = new OBMXMI2Alloy(output_and_testfiles_dir);
    // load xmi File
    translator.loadXmiFile(xmiFile);
    String sysMLClassQualifiedName =
        "Model::4.1 Basic Examples::4.1.1 Time Orderings::AllControl";
    String manualFileName = "4.1.1 Time Orderings - AllControl.als";
    File translatorGeneratedFile =
        new File(output_and_testfiles_dir, manualFileName + "_Generated" + ".als");
    // 1st translation - create a alloy file for a class(name)
    if (!translator.createAlloyFile(sysMLClassQualifiedName, translatorGeneratedFile)) {
      System.err.println(translator.getErrorMessages());
    } else
      System.out.println("1st translation is done");

    sysMLClassQualifiedName = "Model::4.1 Basic Examples::4.1.1 Time Orderings::Decision";
    manualFileName = "4.1.1 Time Orderings - Decision.als";
    translatorGeneratedFile =
        new File(output_and_testfiles_dir, manualFileName + "_Generated" + ".als");
    // 2nd translation
    if (!translator.createAlloyFile(sysMLClassQualifiedName, translatorGeneratedFile)) {
      System.err.println(translator.getErrorMessages());
    } else
      System.out.println("2nd translation is done");
    if (!translator.createAlloyFile(sysMLClassQualifiedName, translatorGeneratedFile)) {
      System.err.println(translator.getErrorMessages());
    } else
      System.out.println("3rd translation is done");
    if (!translator.createAlloyFile(sysMLClassQualifiedName, translatorGeneratedFile)) {
      System.err.println(translator.getErrorMessages());
    } else
      System.out.println("4th translation is done");
    if (!translator.createAlloyFile(sysMLClassQualifiedName, translatorGeneratedFile)) {
      System.err.println(translator.getErrorMessages());
    } else
      System.out.println("5st translation is done");

    // ... some time passes ...
    LocalDateTime end = LocalDateTime.now();

    Duration duration = Duration.between(start, end);
    System.out.println(
        "Took " + duration.getSeconds() + " seconds to a load XMI file and do 5 translations. ");

  }
}
