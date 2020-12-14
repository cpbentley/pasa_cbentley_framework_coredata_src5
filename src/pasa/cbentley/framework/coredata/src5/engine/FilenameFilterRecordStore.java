package pasa.cbentley.framework.coredata.src5.engine;

import java.io.File;
import java.io.FilenameFilter;

import pasa.cbentley.framework.coredata.src5.interfaces.ITechRSM;

public final class FilenameFilterRecordStore implements FilenameFilter {
   public boolean accept(File dir, String name) {
      if (name.endsWith(ITechRSM.RECORD_STORE_SUFFIX)) {
         return true;
      } else {
         return false;
      }
   }
}