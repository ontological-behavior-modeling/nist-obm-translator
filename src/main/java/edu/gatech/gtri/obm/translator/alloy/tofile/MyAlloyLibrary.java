package edu.gatech.gtri.obm.translator.alloy.tofile;

import java.io.File;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.parser.CompModule;
import edu.mit.csail.sdg.parser.CompUtil;


public class MyAlloyLibrary {


  public static CompModule importAlloyModule(File aFile) {
    // String directory = "C:\\Users\\ashinjo3\\Documents\\Alloy\\obm\\";
    // String path = directory + filename;
    return CompUtil.parseEverything_fromFile(new A4Reporter(), null, aFile.getAbsolutePath());
  }

  public static String removeSlash(String sig) {
    if (sig.contains("/")) {
      int index = sig.lastIndexOf('/');
      return sig.substring(index + 1, sig.length());
    }

    return sig;
  }
}
