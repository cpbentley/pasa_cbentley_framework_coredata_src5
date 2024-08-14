package pasa.cbentley.framework.core.data.src5.rsm;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.framework.core.data.src5.ctx.CoreData5Ctx;
import pasa.cbentley.framework.core.data.src5.interfaces.IRecordStoreManager;

public abstract class RSMAbstract implements IRecordStoreManager {

   protected final CoreData5Ctx cdc;

   public RSMAbstract(CoreData5Ctx cdc) {
      this.cdc = cdc;
   }


   //#mdebug
   public IDLog toDLog() {
      return toStringGetUCtx().toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, RSMAbstract.class, 28);
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, RSMAbstract.class);
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return cdc.getUC();
   }

   //#enddebug
   


}
