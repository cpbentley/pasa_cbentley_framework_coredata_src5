package pasa.cbentley.framework.core.data.src5.ctx;

import pasa.cbentley.byteobjects.src4.ctx.ConfigAbstractBO;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.core.data.src5.interfaces.ITechRSM;

public class ConfigCoreData5Default extends ConfigAbstractBO implements IConfigCoreData5 {

   private boolean isVolatile;

   private String  rootDirectory;

   private String  rootPath;

   private String  extension;

   public ConfigCoreData5Default(UCtx uc) {
      super(uc);
      isVolatile = false;
      rootDirectory = "coredata5";
      rootPath = System.getProperty("user.home");
      extension = ITechRSM.RECORD_STORE_SUFFIX;
   }

   public String getRMSPath() {
      return rootPath;
   }

   public String getAppWorkingDirectory() {
      return rootDirectory;
   }

   public String getFileExtension() {
      return extension;
   }

   public boolean isVolatileData() {
      return isVolatile;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, ConfigCoreData5Default.class, "@line5");
      toStringPrivate(dc);
      super.toString(dc.sup());
      dc.nl();
      dc.appendVarWithSpace("rootDirectory", rootDirectory);
      dc.appendVarWithSpace("rootPath", rootPath);
   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("isVolatile", isVolatile);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, ConfigCoreData5Default.class);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug

}
