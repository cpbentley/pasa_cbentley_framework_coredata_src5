package pasa.cbentley.framework.coredata.src5.engine;

import pasa.cbentley.framework.coredata.src4.ctx.CoreDataCtx;
import pasa.cbentley.framework.coredata.src4.ex.StoreException;
import pasa.cbentley.framework.coredata.src4.ex.StoreFullException;
import pasa.cbentley.framework.coredata.src4.ex.StoreNotFoundException;
import pasa.cbentley.framework.coredata.src4.interfaces.IRMSCreator;
import pasa.cbentley.framework.coredata.src4.interfaces.IRecordStore;

/**
 * Bentley Framework visible side of the backend
 * <br>
 * Is used in Android and J2SE implementations.
 * <br>
 * @author Charles Bentley
 *
 */
public class RMSCreator implements IRMSCreator {

   private CoreDataCtx dd;

   /**
    * 
    */
   IRecordStoreManager   rsm;

   public RMSCreator(CoreDataCtx dd, IRecordStoreManager br) {
      this.dd = dd;
      rsm = br;
   }

   public void deleteRecordStore(String recordStoreName) throws StoreException {
      rsm.deleteRecordStore(recordStoreName);
   }

   public int getBase() {
      return rsm.getBase();
   }

   public String[] listRecordStores() {
      return rsm.listRecordStores();
   }

   public IRecordStore openRecordStore(String recordStoreName, boolean createIfNecessary) throws StoreException, StoreFullException, StoreNotFoundException {
      return rsm.openRecordStore(recordStoreName, createIfNecessary);
   }

}
