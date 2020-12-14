package pasa.cbentley.framework.coredata.src5.ctx;

import pasa.cbentley.byteobjects.src4.ctx.ConfigAbstractBO;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;

public class ConfigCoreData5Default extends ConfigAbstractBO implements IConfigCoreData5 {

   private boolean isVolatile;

   private String  rootDirectory;

   private String  rootPath;

   public ConfigCoreData5Default(UCtx uc) {
      super(uc);
      isVolatile = false;
      rootDirectory = "coredata5";
      rootPath = System.getProperty("user.home");
   }

   public String getRMSPath() {
      return rootPath;
   }

   public String getAppWorkingDirectory() {
      return rootDirectory;
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
