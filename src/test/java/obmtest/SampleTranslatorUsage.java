package obmtest;

import edu.gatech.gtri.obm.alloy.translator.OBMXMI2Alloy;
import edu.umd.omgutil.UMLModelErrorException;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * A sample usages of OBMXMI2Alloy translator.
 *
 * @author Wilson Miyako, ASDL, Georgia Tech
 */
public class SampleTranslatorUsage {

  public static void main(String[] args) throws FileNotFoundException, UMLModelErrorException {

    LocalDateTime start = LocalDateTime.now();

    // *** Note: *** output_and_testfiles_directory and obmmodel_dir do not need to be the same.

    // specify where output file will be written
    String output_and_testfiles_directory = "src/test/resources";
    // where obm xmi file and alloy library (Translator.als and utils/*.als) are locating
    String ombmodel_dir = "src/test/resources";
    File xmiFile = new File(ombmodel_dir, "OBMModel.xmi");

    // create OBMXMI2Alloy object
    OBMXMI2Alloy translator = new OBMXMI2Alloy(ombmodel_dir);
    // load xmi File
    translator.loadXmiFile(xmiFile);

    // 1st translation - load xmi file, perform translation, and write an alloy output file.
    String sysMLClassQualifiedName = "Model::4.1 Basic Examples::4.1.1 Time Orderings::AllControl";
    String manualFileName = "4.1.1 Time Orderings - AllControl.als";
    File translatorGeneratedFile =
        new File(output_and_testfiles_directory, manualFileName + "_Generated" + ".als");
    // 1st translation - create a alloy file for a class(name)
    if (!translator.createAlloyFile(sysMLClassQualifiedName, translatorGeneratedFile)) {
      System.err.println(translator.getErrorMessages());
    } else System.out.println("1st translation is done - xmi file is loaded");

    // 2nd translation - no loading xmi file, perform translation, and write an alloy output file.
    sysMLClassQualifiedName = "Model::4.1 Basic Examples::4.1.1 Time Orderings::Decision";
    manualFileName = "4.1.1 Time Orderings - Decision.als";
    translatorGeneratedFile =
        new File(output_and_testfiles_directory, manualFileName + "_Generated" + ".als");
    // 2nd translation
    if (!translator.createAlloyFile(sysMLClassQualifiedName, translatorGeneratedFile)) {
      System.err.println(translator.getErrorMessages());
    } else System.out.println("2nd translation is done - no loading of xmil file.");

    LocalDateTime end = LocalDateTime.now();
    Duration duration = Duration.between(start, end);
    System.out.println(
        "Took " + duration.getSeconds() + " seconds to a load XMI file and do 2 translations. ");
  }
}
