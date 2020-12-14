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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import pasa.cbentley.framework.coredata.src4.ex.StoreException;
import pasa.cbentley.framework.coredata.src4.ex.StoreNotFoundException;
import pasa.cbentley.framework.coredata.src4.ex.StoreNotOpenException;
import pasa.cbentley.framework.coredata.src4.interfaces.ExtendedRecordListener;
import pasa.cbentley.framework.coredata.src4.interfaces.IRecordStore;
import pasa.cbentley.framework.coredata.src5.ctx.CoreData5Ctx;
import pasa.cbentley.framework.coredata.src5.engine.FilenameFilterRecordStore;
import pasa.cbentley.framework.coredata.src5.engine.RecordStoreHashMap;
import pasa.cbentley.framework.coredata.src5.interfaces.IRecordStoreManager;

public abstract class RSMFileBased extends RSMAbstract implements IRecordStoreManager {

   final static String          RECORD_STORE_SUFFIX = ".rs";

   protected final List<String> replaceChars        = new Vector<String>();

   protected String escapeCharacter(String charcter) {
      return "_%%" + (int) (charcter.charAt(0)) + "%%_";
   }

   public String fileName2RecordStoreName(String fileName) {
      for (Iterator<String> iterator = replaceChars.iterator(); iterator.hasNext();) {
         String c = (String) iterator.next();
         String newValue = escapeCharacter(c);
         if (c.equals("\\")) {
            c = "\\\\";
         }
         fileName = fileName.replaceAll(newValue, c);
      }
      return fileName.substring(0, fileName.length() - RECORD_STORE_SUFFIX.length());
   }

   public String recordStoreName2FileName(String recordStoreName) {
      for (Iterator<String> iterator = replaceChars.iterator(); iterator.hasNext();) {
         String c = (String) iterator.next();
         String newValue = escapeCharacter(c);
         if (c.equals("\\")) {
            c = "\\\\";
         }
         c = "[" + c + "]";
         recordStoreName = recordStoreName.replaceAll(c, newValue);
      }
      return recordStoreName + RECORD_STORE_SUFFIX;
   }

   protected File                                  baseFolder;

   protected FilenameFilter                        filter           = new FilenameFilterRecordStore();

   protected ExtendedRecordListener                recordListener   = null;

   protected Hashtable<String, RecordStoreHashMap> openRecordStores = new Hashtable<String, RecordStoreHashMap>();

   public RSMFileBased(CoreData5Ctx hoc, File suitFolder) {
      super(hoc);
      baseFolder = suitFolder;
      replaceChars.add(":");
      replaceChars.add("*");
      replaceChars.add("?");
      replaceChars.add("=");
      replaceChars.add("|");
      replaceChars.add("/");
      replaceChars.add("\\");
      replaceChars.add("\"");
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
         store = new RecordStoreHashMap(hoc, this, dis);
         dis.close();
      } catch (FileNotFoundException e) {
         throw e;
      } catch (IOException e) {
         e.printStackTrace();
         System.out.println("RecordStore.loadFromDisk: ERROR reading " + recordStoreFile.getName());
      }
      return store;
   }

   /**
    * Open the {@link IRecordStore} by reading the whole file
    */
   public IRecordStore openRecordStore(String recordStoreName, boolean createIfNecessary) throws StoreException {
      File storeFile = getStoreFile(recordStoreName);

      RecordStoreHashMap recordStoreImpl;
      try {
         recordStoreImpl = loadFromDiskSecure(storeFile);
      } catch (FileNotFoundException e) {
         if (!createIfNecessary) {
            throw new StoreNotFoundException(recordStoreName);
         }
         recordStoreImpl = new RecordStoreHashMap(hoc, this, recordStoreName);
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
