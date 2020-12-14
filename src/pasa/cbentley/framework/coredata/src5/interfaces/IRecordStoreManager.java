package pasa.cbentley.framework.coredata.src5.interfaces;

import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.framework.coredata.src4.ex.StoreException;
import pasa.cbentley.framework.coredata.src4.ex.StoreNotFoundException;
import pasa.cbentley.framework.coredata.src4.ex.StoreNotOpenException;
import pasa.cbentley.framework.coredata.src4.interfaces.ExtendedRecordListener;
import pasa.cbentley.framework.coredata.src4.interfaces.IRecordStore;
import pasa.cbentley.framework.coredata.src5.engine.RecordStoreHashMap;
import pasa.cbentley.framework.coredata.src5.rsm.RSMFileBased;
import pasa.cbentley.framework.coredata.src5.rsm.RSMFileSequential;

/**
 * Interface used by the bridge to access {@link IRecordStore} evil static methods
 * <br>
 * Access to the {@link IRecordStoreManager} that will create {@link IRecordStore}.
 * <br>
 * It basically implements the static methods of the MIDP {@link IRecordStore} class.
 * <br>
 * <br>
 * Currently a reference abstract implementation by {@link RSMFileBased}.
 * <br>
 * 
 * @author Charles-Philip Bentley
 * @see RSMFileBased
 * @see RSMFileSequential
 * 
 */
public interface IRecordStoreManager extends IStringable {

   /**
    * 
    * @return
    */
   public String getName();

   /**
    * 
    * @param recordStoreName
    * @throws StoreNotFoundException
    * @throws StoreException
    */
   public void deleteRecordStore(String recordStoreName) throws StoreNotFoundException, StoreException;

   /**
    * 
    * @param recordStoreName
    * @param createIfNecessary
    * @return
    * @throws StoreException
    */
   public IRecordStore openRecordStore(String recordStoreName, boolean createIfNecessary) throws StoreException;

   /**
    * 
    * @return
    */
   public String[] listRecordStores();

   /**
    * 
    * @param recordStoreImpl
    * @throws StoreNotOpenException
    * @throws StoreException
    */
   public void saveChanges(RecordStoreHashMap recordStoreImpl) throws StoreNotOpenException, StoreException;

   /**
    * 
    * @param recordStoreImpl
    * @return
    */
   public int getSizeAvailable(RecordStoreHashMap recordStoreImpl);

   /**
    * Delete all record stores.
    */
   public void deleteStores();

   /**
    * 
    * @param recordListener
    */
   public void setRecordListener(ExtendedRecordListener recordListener);

   /**
    * 
    * @param type
    * @param recordStoreName
    */
   public void fireRecordStoreListener(int type, String recordStoreName);

   public int getBase();

}
