package pasa.cbentley.framework.coredata.src5.rsm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Hashtable;

import pasa.cbentley.framework.coredata.src4.ex.StoreException;
import pasa.cbentley.framework.coredata.src4.ex.StoreNotFoundException;
import pasa.cbentley.framework.coredata.src4.ex.StoreNotOpenException;
import pasa.cbentley.framework.coredata.src4.interfaces.ExtendedRecordListener;
import pasa.cbentley.framework.coredata.src4.interfaces.IRecordStore;
import pasa.cbentley.framework.coredata.src5.ctx.CoreData5Ctx;
import pasa.cbentley.framework.coredata.src5.engine.FilenameFilterRecordStore;
import pasa.cbentley.framework.coredata.src5.engine.RecordStoreHashMap;
import pasa.cbentley.framework.coredata.src5.interfaces.IRecordStoreManager;

/**
 * A {@link RSMFileSequential} write to disk.
 * <br>
 * <br>
 * This could be a problem in applets. So Application launcher will create a Secure FileRecordStoreManager
 * for suchs environments.
 * <br>
 * <br>
 * @author Charles-Philip Bentley
 *
 */
public class RSMFileSequential extends RSMFileBased implements IRecordStoreManager {


   private static final int          BASE_RECORD         = 0;

   private File                                  baseFolder;

   private FilenameFilter                        filter           = new FilenameFilterRecordStore();

   private ExtendedRecordListener                recordListener   = null;

   private Hashtable<String, RecordStoreHashMap> openRecordStores = new Hashtable<String, RecordStoreHashMap>();

   public RSMFileSequential(CoreData5Ctx hoc, File suitFolder) {
      super(hoc,suitFolder);
      baseFolder = suitFolder;
   }

   public int getBase() {
      return RSMFileSequential.BASE_RECORD;
   }

   public void deleteRecordStore(final String recordStoreName) throws StoreNotFoundException, StoreException {
      final File storeFile = getStoreFile(recordStoreName);

      RecordStoreHashMap recordStoreImpl = (RecordStoreHashMap) openRecordStores.get(storeFile.getName());
      if (recordStoreImpl != null && recordStoreImpl.isOpen()) {
         throw new StoreException();
      }

      try {
         recordStoreImpl = loadFromDiskSecure(storeFile);
      } catch (FileNotFoundException ex) {
         throw new StoreNotFoundException(recordStoreName);
      }

      storeFile.delete();
      fireRecordStoreListener(ExtendedRecordListener.RECORDSTORE_DELETE, recordStoreName);
   }

   public void deleteStores() {
      String[] stores = listRecordStores();
      for (int i = 0; i < stores.length; i++) {
         String store = stores[i];
         try {
            deleteRecordStore(store);
         } catch (StoreException e) {
            e.printStackTrace();
         }
      }
   }

   public void fireRecordStoreListener(int type, String recordStoreName) {
      if (recordListener != null) {
         recordListener.recordStoreEvent(type, System.currentTimeMillis(), recordStoreName);
      }
   }

   public String getName() {
      return "File RecordStore Manager";
   }

   /**
    * Size available for the file
    */
   public int getSizeAvailable(RecordStoreHashMap recordStoreImpl) {
      // FIXME should return free space on device
      return 1024 * 1024;
   }

  

   public void init() {
   }

   public String[] listRecordStores() {
      String[] result = baseFolder.list(filter);
      if (result != null) {
         if (result.length == 0) {
            result = null;
         } else {
            for (int i = 0; i < result.length; i++) {
               result[i] = fileName2RecordStoreName(result[i]);
            }
         }
      }
      return result;
   }

   private RecordStoreHashMap loadFromDiskSecure(File recordStoreFile) throws FileNotFoundException {
      RecordStoreHashMap store = null;
      try {
         DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(recordStoreFile)));
         store = new RecordStoreHashMap(hoc,this, dis);
         dis.close();
      } catch (FileNotFoundException e) {
         throw e;
      } catch (IOException e) {
         e.printStackTrace();
         System.out.println("RecordStore.loadFromDisk: ERROR reading " + recordStoreFile.getName());
      }
      return store;
   }

   public IRecordStore openRecordStore(String recordStoreName, boolean createIfNecessary) throws StoreException {
      File storeFile = getStoreFile(recordStoreName);

      RecordStoreHashMap recordStoreImpl;
      try {
         recordStoreImpl = loadFromDiskSecure(storeFile);
      } catch (FileNotFoundException e) {
         if (!createIfNecessary) {
            throw new StoreNotFoundException(recordStoreName);
         }
         recordStoreImpl = new RecordStoreHashMap(hoc,this, recordStoreName);
         saveToDiskSecure(storeFile, recordStoreImpl);
      }
      recordStoreImpl.setOpen(true);
      if (recordListener != null) {
         recordStoreImpl.addRecordListener(recordListener);
      }

      openRecordStores.put(storeFile.getName(), recordStoreImpl);

      fireRecordStoreListener(ExtendedRecordListener.RECORDSTORE_OPEN, recordStoreName);

      return recordStoreImpl;
   }

   /**
    * Write the complete {@link IRecordStore} to file.
    * <br>
    * <br>
    * <li> {@link RecordStoreHashMap#addRecord(byte[], int, int)}
    * <li> {@link RecordStoreHashMap#setRecord(int, byte[], int, int)}
    * <li> {@link RecordStoreHashMap#deleteRecord(int)}
    */
   public void saveChanges(RecordStoreHashMap recordStoreImpl) throws StoreNotOpenException, StoreException {
      File storeFile = getStoreFile(recordStoreImpl.getName());
      saveToDiskSecure(storeFile, recordStoreImpl);
   }

   /**
    * Writes the file to disk
    * @param recordStoreFile
    * @param recordStore
    * @throws StoreException
    */
   private void saveToDiskSecure(final File recordStoreFile, final RecordStoreHashMap recordStore) throws StoreException {
      if (!recordStoreFile.getParentFile().exists()) {
         if (!recordStoreFile.getParentFile().mkdirs()) {
            throw new StoreException("Unable to create recordStore directory");
         }
      }
      try {
         DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(recordStoreFile)));
         recordStore.write(dos);
         dos.close();
      } catch (IOException e) {
         e.printStackTrace();
         System.out.println("RecordStore.saveToDisk: ERROR writting object to " + recordStoreFile.getName());
         throw new StoreException(e.getMessage());
      }
   }

   public void setRecordListener(ExtendedRecordListener recordListener) {
      this.recordListener = recordListener;
   }
}
