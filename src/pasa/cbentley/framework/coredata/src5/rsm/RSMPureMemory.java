package pasa.cbentley.framework.coredata.src5.rsm;

import java.util.Enumeration;
import java.util.Hashtable;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.coredata.src4.ex.StoreException;
import pasa.cbentley.framework.coredata.src4.ex.StoreNotFoundException;
import pasa.cbentley.framework.coredata.src4.interfaces.ExtendedRecordListener;
import pasa.cbentley.framework.coredata.src4.interfaces.IRecordStore;
import pasa.cbentley.framework.coredata.src5.ctx.CoreData5Ctx;
import pasa.cbentley.framework.coredata.src5.engine.RecordStoreHashMap;
import pasa.cbentley.framework.coredata.src5.interfaces.IRecordStoreManager;

/**
 * RecordStore loaded when {@link RSMFileSequential} failed to load for any reason.
 * <br>
 * This allows application to work but unable to save its state.
 * <br>
 * <br>
 * @author Charles-Philip Bentley
 *
 */
public class RSMPureMemory extends RSMAbstract implements IRecordStoreManager {

   private ExtendedRecordListener                recordListener = null;

   /**
    * 
    */
   private Hashtable<String, RecordStoreHashMap> recordStores   = new Hashtable<String, RecordStoreHashMap>();

   public RSMPureMemory(CoreData5Ctx hoc) {
      super(hoc);
   }

   public void deleteRecordStore(String recordStoreName) throws StoreNotFoundException, StoreException {
      RecordStoreHashMap recordStoreImpl = (RecordStoreHashMap) recordStores.get(recordStoreName);
      if (recordStoreImpl == null) {
         throw new StoreNotFoundException(recordStoreName);
      }
      if (recordStoreImpl.isOpen()) {
         throw new StoreException();
      }
      recordStores.remove(recordStoreName);

      fireRecordStoreListener(ExtendedRecordListener.RECORDSTORE_DELETE, recordStoreName);
   }

   public void deleteStores() {
      if (recordStores != null)
         recordStores.clear();
   }

   public void fireRecordStoreListener(int type, String recordStoreName) {
      if (recordListener != null) {
         recordListener.recordStoreEvent(type, System.currentTimeMillis(), recordStoreName);
      }
   }

   public int getBase() {
      return RecordStoreHashMap.BASE_RECORD;
   }

   public String getName() {
      return "Memory record store";
   }

   public int getSizeAvailable(RecordStoreHashMap recordStoreImpl) {
      // FIXME returns too much
      return (int) Runtime.getRuntime().freeMemory();
   }

   public void init() {
      deleteStores();
   }

   public String[] listRecordStores() {
      String[] result = null;

      int i = 0;
      for (Enumeration<String> e = recordStores.keys(); e.hasMoreElements();) {
         if (result == null) {
            result = new String[recordStores.size()];
         }
         result[i] = (String) e.nextElement();
         i++;
      }

      return result;
   }

   /**
    * Opening the store x times. means the store must be closed x times as well.
    */
   public IRecordStore openRecordStore(String recordStoreName, boolean createIfNecessary) throws StoreNotFoundException {
      //System.out.println("#MemoryRecordStoreManager Open " + recordStoreName);
      RecordStoreHashMap recordStoreImpl = (RecordStoreHashMap) recordStores.get(recordStoreName);

      if (recordStoreImpl == null) {
         if (!createIfNecessary) {
            throw new StoreNotFoundException(recordStoreName);
         }
         recordStoreImpl = new RecordStoreHashMap(cdc,this, recordStoreName);
         recordStores.put(recordStoreName, recordStoreImpl);
      }
      recordStoreImpl.setOpen(true);
      if (recordListener != null) {
         recordStoreImpl.addRecordListener(recordListener);
      }

      fireRecordStoreListener(ExtendedRecordListener.RECORDSTORE_OPEN, recordStoreName);

      return recordStoreImpl;
   }

   public void saveChanges(RecordStoreHashMap recordStoreImpl) {
   }

   public void setRecordListener(ExtendedRecordListener recordListener) {
      this.recordListener = recordListener;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "RSMPureMemory");
      toStringPrivate(dc);
      super.toString(dc.sup());

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "RSMPureMemory");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx sb) {
      try {
         throw new NullPointerException();
      } catch (Exception e) {
         e.printStackTrace();
      }
      sb.append("#MemoryRecordStoreManager #" + recordStores.size());
      Enumeration e = recordStores.keys();
      String[] ar = new String[recordStores.size()];
      int count = 0;
      while (e.hasMoreElements()) {
         String str = (String) e.nextElement();
         sb.nl();
         sb.append(count + " : " + str);
         ar[count] = str;
         count++;
      }
      sb.append("Now Listing Store Content");
      for (int i = 0; i < ar.length; i++) {
         RecordStoreHashMap rsh = recordStores.get(ar[i]);
         sb.nl();
         sb.append(ar[i] + " = " + rsh.toString());
      }
   }

   //#enddebug

}
