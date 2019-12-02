package pasa.cbentley.framework.coredata.src5.engine;

import java.io.File;
import java.io.FilenameFilter;

public final class FnFilter implements FilenameFilter {
   public boolean accept(File dir, String name) {
      if (name.endsWith(RSMFileSequential.RECORD_STORE_SUFFIX)) {
         return true;
      } else {
         return false;
      }
   }
}