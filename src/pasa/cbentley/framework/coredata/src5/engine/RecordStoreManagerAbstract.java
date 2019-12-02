package pasa.cbentley.framework.coredata.src5.engine;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.coredata.src4.ctx.CoreDataCtx;

public abstract class RecordStoreManagerAbstract implements IRecordStoreManager {

   protected final CoreDataCtx hoc;

   public RecordStoreManagerAbstract(CoreDataCtx hoc) {
      this.hoc = hoc;
   }

   //#mdebug
   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "RecordStoreManagerAbstract");
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "RecordStoreManagerAbstract");
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return hoc.getUCtx();
   }

   //#enddebug

}
