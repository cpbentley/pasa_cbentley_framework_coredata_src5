package pasa.cbentley.framework.coredata.src5.ctx;

import java.io.File;

import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.ctx.BOCtx;
import pasa.cbentley.byteobjects.src4.ctx.IConfigBO;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.coredata.src4.ctx.CoreDataCtx;
import pasa.cbentley.framework.coredata.src4.ctx.IConfigCoreData;
import pasa.cbentley.framework.coredata.src4.db.IByteRecordStoreFactory;
import pasa.cbentley.framework.coredata.src5.interfaces.IRecordStoreManager;
import pasa.cbentley.framework.coredata.src5.rsm.RMSCreator;
import pasa.cbentley.framework.coredata.src5.rsm.RSMFileSequential;
import pasa.cbentley.framework.coredata.src5.rsm.RSMPureMemory;

public class CoreData5Ctx extends CoreDataCtx {

   private static final int        CTX_ID = 789;

   private IByteRecordStoreFactory rsFactory;

   public CoreData5Ctx(IConfigCoreData5 config, BOCtx boc) {
      super(config == null ? new ConfigCoreData5Default(boc.getUC()) : config, boc);

      if (this.getClass() == CoreData5Ctx.class) {
         this.a_Init();
      }

      //#debug
      toDLog().pInit("Created", this, CoreData5Ctx.class, "CoreData5Ctx", LVL_05_FINE, true);
   }

   protected void applySettings(ByteObject settingsNew, ByteObject settingsOld) {

   }

   public IConfigCoreData5 getConfigCoreData5() {
      return (IConfigCoreData5) config;
   }

   /**
    * Creates an {@link IByteRecordStoreFactory} based on the {@link IConfigCoreData} parameters.
    * @param config
    * @return
    */
   private IByteRecordStoreFactory createRMSFactory() {
      IConfigCoreData5 config = getConfigCoreData5();
      IRecordStoreManager rsm = null;
      //depends on launchvalues
      if (config.isVolatileData()) {
         rsm = new RSMPureMemory(this);
      } else {
         String path = config.getRMSPath();
         File suitFolder = new File(path, config.getAppWorkingDirectory());

         if (suitFolder.exists()) {
            //#debug
            toDLog().pInit("Folder " + suitFolder.getAbsolutePath() + " exists. No need to create it.", this, CoreData5Ctx.class, "createRMSFactory", LVL_05_FINE, true);
         } else {
            //#debug
            toDLog().pInit("Folder " + suitFolder.getAbsolutePath() + " does not exists.", this, CoreData5Ctx.class, "createRMSFactory", LVL_05_FINE, true);
         }

         rsm = new RSMFileSequential(this, suitFolder);
      }
      return new RMSCreator(this, rsm);
   }

   public int getBOCtxSettingSize() {
      return IBOCtxSettingCoreData5.CTX_COREDATA_BASIC_SIZE;
   }

   /**
    * If you want different kinds of {@link IByteRecordStoreFactory}, you have to create new {@link CoreData5Ctx}.
    * 
    * It some older framework, it would have been a global static singleton.
    * Here it is encapsulated in a code context configured by a {@link IConfigCoreData}
    */
   public IByteRecordStoreFactory getByteRecordStoreFactory() {
      if (rsFactory == null) {
         rsFactory = createRMSFactory();
      }
      return rsFactory;
   }

   public int getCtxID() {
      return CTX_ID;
   }

   protected void matchConfig(IConfigBO config, ByteObject settings) {
      // TODO Auto-generated method stub

   }

   public void setByteRecordStoreFactory(IByteRecordStoreFactory fac) {
      if (fac != null) {
         rsFactory = fac;
      }
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, CoreData5Ctx.class, 100);
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, CoreData5Ctx.class);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx dc) {

   }

   //#enddebug

}
