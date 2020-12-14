package pasa.cbentley.framework.coredata.src5.rsm;

import pasa.cbentley.framework.coredata.src4.db.IByteRecordStoreFactory;
import pasa.cbentley.framework.coredata.src4.ex.StoreException;
import pasa.cbentley.framework.coredata.src4.ex.StoreFullException;
import pasa.cbentley.framework.coredata.src4.ex.StoreNotFoundException;
import pasa.cbentley.framework.coredata.src4.interfaces.IRecordStore;
import pasa.cbentley.framework.coredata.src5.ctx.CoreData5Ctx;
import pasa.cbentley.framework.coredata.src5.interfaces.IRecordStoreManager;

/**
 * Bentley Framework visible side of the backend
 * <br>
 * Is used in Android and J2SE implementations.
 * <br>
 * @author Charles Bentley
 *
 */
public class RMSCreator implements IByteRecordStoreFactory {

   protected final CoreData5Ctx dd;

   /**
    * 
    */
   IRecordStoreManager          rsm;

   public RMSCreator(CoreData5Ctx dd, IRecordStoreManager br) {
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
