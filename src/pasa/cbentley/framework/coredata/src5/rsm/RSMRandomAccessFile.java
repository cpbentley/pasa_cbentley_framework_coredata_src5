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
import java.io.RandomAccessFile;
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
 * A {@link RSMRandomAccessFile} write to disk.
 * <br>
 * <br>
 * This could be a problem in applets. So Application launcher will create a Secure FileRecordStoreManager
 * for suchs environments.
 * <br>
 * <br>
 * Uses {@link RandomAccessFile}.
 * Access mode is really important to know whether to sync the data.
 * <br>
 * <br>
 * 
 * When using many reads, a buffer is the way to go
 * RandomAccessFile raf = ...
   FileInputStream fis = new FileInputStream(raf.getFD());
   BufferedInputStream bis = new BufferedInputStream(fis);

   //do some reads with buffer
   bis.read(...);
   bis.read(...);

   //seek to a a different section of the file, so discard the previous buffer
   raf.seek(...);
   bis = new BufferedInputStream(fis);
   bis.read(...);
   bis.read(...);
   <br>
   <br>
   
   Creating
   <li>"r"  Open for reading only. Invoking any of the write methods of the resulting object will cause an IOException to be thrown.
<li>"rw"    Open for reading and writing. If the file does not already exist then an attempt will be made to create it.
<li>"rws"   Open for reading and writing, as with "rw", and also require that every update to the file's content or metadata be written synchronously to the underlying storage device.
<li>"rwd"       Open for reading and writing, as with "rw", and also require that every update to the file's content be written synchronously to the underlying storage device. 

 * @author Charles-Philip Bentley
 *
 */
public class RSMRandomAccessFile extends RSMFileBased implements IRecordStoreManager {

   private static final int                      BASE_RECORD      = 0;

   private FilenameFilter                        filter           = new FilenameFilterRecordStore();

   private ExtendedRecordListener                recordListener   = null;

   private Hashtable<String, RecordStoreHashMap> openRecordStores = new Hashtable<String, RecordStoreHashMap>();

   public RSMRandomAccessFile(CoreData5Ctx hoc, File suitFolder) {
      super(hoc, suitFolder);
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

   public int getBase() {
      return RSMRandomAccessFile.BASE_RECORD;
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

   public File getStoreFile(String recordStoreName) {
      return new File(baseFolder, recordStoreName2FileName(recordStoreName));
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
         store = new RecordStoreHashMap(cdc, this, dis);
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
         recordStoreImpl = new RecordStoreHashMap(cdc, this, recordStoreName);
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
    * TODO implement with Random Access file?
    */
   public void saveChanges(RecordStoreHashMap recordStoreImpl) throws StoreNotOpenException, StoreException {
      File storeFile = getStoreFile(recordStoreImpl.getName());
      saveToDiskSecure(storeFile, recordStoreImpl);
   }

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
