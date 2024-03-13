package pasa.cbentley.framework.coredata.src5.engine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.framework.coredata.src4.ctx.CoreDataCtx;
import pasa.cbentley.framework.coredata.src4.engine.Request;
import pasa.cbentley.framework.coredata.src4.ex.StoreException;
import pasa.cbentley.framework.coredata.src4.ex.StoreFullException;
import pasa.cbentley.framework.coredata.src4.ex.StoreInvalidIDException;
import pasa.cbentley.framework.coredata.src4.ex.StoreNotOpenException;
import pasa.cbentley.framework.coredata.src4.interfaces.ExtendedRecordListener;
import pasa.cbentley.framework.coredata.src4.interfaces.IRecordComparator;
import pasa.cbentley.framework.coredata.src4.interfaces.IRecordEnumeration;
import pasa.cbentley.framework.coredata.src4.interfaces.IRecordFilter;
import pasa.cbentley.framework.coredata.src4.interfaces.IRecordListener;
import pasa.cbentley.framework.coredata.src4.interfaces.IRecordStore;
import pasa.cbentley.framework.coredata.src5.ctx.CoreData5Ctx;
import pasa.cbentley.framework.coredata.src5.interfaces.IRecordStoreManager;

/**
 * Implementation of {@link IRecordStore} using {@link Hashtable}.
 * <br>
 * <br>
 * 
 * @author Charles-Philip Bentley
 *
 */
public class RecordStoreHashMap implements IRecordStore {

   private transient boolean                 isOpen;

   private transient int                     openCount       = 0;

   private long                              lastModified    = 0;

   /**
    * Starts at 
    */
   private int                               nextRecordID    = BASE_RECORD;

   public static final int                   BASE_RECORD     = 0;

   /**
    * Listeners
    */
   private transient Vector<IRecordListener> recordListeners = new Vector<IRecordListener>();

   /**
    * null values are null byte arrays.
    * <br>
    * To check if a record exists, query keys
    */
   protected HashMap<Integer, byte[]>        records         = new HashMap<Integer, byte[]>();

   private transient IRecordStoreManager     recordStoreManager;

   private String                            recordStoreName;

   private int                               version         = 0;

   private static final int                  VER             = 45677;

   protected final CoreData5Ctx                      cdc;

   public RecordStoreHashMap(CoreData5Ctx hoc, IRecordStoreManager recordStoreManager, DataInputStream dis) throws IOException {
      this.cdc = hoc;
      this.recordStoreManager = recordStoreManager;

      this.recordStoreName = dis.readUTF();
      int classVer = dis.readInt();
      if (classVer == 45677) {
         this.version = dis.readInt();
         this.lastModified = dis.readLong();
         this.nextRecordID = dis.readInt();
         try {
            int size = dis.readInt();
            for (int i = 0; i < size; i++) {
               int recordId = dis.readInt();
               int len = dis.readInt();
               byte[] data = null;
               if (len >= 0) {
                  data = new byte[len];
                  dis.read(data, 0, data.length);
               }
               this.records.put(new Integer(recordId), data);
            }
         } catch (EOFException ex) {
         }
      }
   }

   public int getBase() {
      return BASE_RECORD;
   }

   /**
    * Constructor without history
    * <br>
    * @param recordStoreManager
    * @param recordStoreName
    */
   public RecordStoreHashMap(CoreData5Ctx hoc, IRecordStoreManager recordStoreManager, String recordStoreName) {
      this.cdc = hoc;
      this.recordStoreManager = recordStoreManager;
      if (recordStoreName.length() <= 32) {
         this.recordStoreName = recordStoreName;
      } else {
         ///TODO maybe throw an exception?
         this.recordStoreName = recordStoreName;
      }
      this.isOpen = false;
   }

   public int addRecord(byte[] data, int offset, int numBytes) throws StoreNotOpenException, StoreException, StoreFullException {
      if (!isOpen) {
         throw new StoreNotOpenException();
      }
      if (data == null && numBytes > 0) {
         throw new NullPointerException();
      }
      if (numBytes > recordStoreManager.getSizeAvailable(this)) {
         throw new StoreFullException();
      }

      byte[] recordData = new byte[numBytes];
      if (data != null) {
         System.arraycopy(data, offset, recordData, 0, numBytes);
      } else {
         recordData = null;
      }

      int curRecordID;
      synchronized (this) {
         //System.out.println("RecordStoreImpl addRecord " + nextRecordID + " " + MUtils.debugString(recordData, ","));
         records.put(new Integer(nextRecordID), recordData);
         version++;
         curRecordID = nextRecordID;
         nextRecordID++;
         lastModified = System.currentTimeMillis();
      }

      //actually saves the WHOLE file to disk. call back to write(DateOutputStream)
      recordStoreManager.saveChanges(this);

      fireRecordListener(ExtendedRecordListener.RECORD_ADD, curRecordID);

      return curRecordID;
   }

   public void addRecordListener(IRecordListener listener) {
      if (!recordListeners.contains(listener)) {
         recordListeners.addElement(listener);
      }
   }

   public void closeRecordStore() throws StoreNotOpenException {
      //System.out.println("#RecordStoreHashTable Close " + recordStoreName);

      if (!isOpen) {
         throw new StoreNotOpenException();
      }

      if (recordListeners != null) {
         recordListeners.removeAllElements();
      }
      recordStoreManager.fireRecordStoreListener(ExtendedRecordListener.RECORDSTORE_CLOSE, this.getName());
      if (openCount > 0) {
         openCount--;
      }
      if (openCount == 0) {
         isOpen = false;
      }
   }

   public void deleteRecord(int recordId) throws StoreNotOpenException, StoreInvalidIDException, StoreException {
      if (!isOpen) {
         throw new StoreNotOpenException();
      }

      synchronized (this) {
         byte[] data = records.remove(new Integer(recordId));
         if (data == null) {
            throw new StoreInvalidIDException();
         }
         version++;
         lastModified = System.currentTimeMillis();
      }

      recordStoreManager.saveChanges(this);

      fireRecordListener(ExtendedRecordListener.RECORD_DELETE, recordId);
   }

   public IRecordEnumeration enumerateRecords(IRecordFilter filter, IRecordComparator comparator, boolean keepUpdated) throws StoreNotOpenException {
      if (!isOpen) {
         throw new StoreNotOpenException();
      }

      return new RecordEnumerationVector(this, filter, comparator, keepUpdated);
   }

   private void fireRecordListener(int type, int recordId) {
      long timestamp = System.currentTimeMillis();

      if (recordListeners != null) {
         for (Enumeration<IRecordListener> e = recordListeners.elements(); e.hasMoreElements();) {
            IRecordListener l = (IRecordListener) e.nextElement();
            if (l instanceof ExtendedRecordListener) {
               ((ExtendedRecordListener) l).recordEvent(type, timestamp, this, recordId);
            } else {
               switch (type) {
                  case ExtendedRecordListener.RECORD_ADD:
                     l.recordAdded(this, recordId);
                     break;
                  case ExtendedRecordListener.RECORD_CHANGE:
                     l.recordChanged(this, recordId);
                     break;
                  case ExtendedRecordListener.RECORD_DELETE:
                     l.recordDeleted(this, recordId);
               }
            }
         }
      }
   }

   public int getHeaderSize() {
      // TODO fixit
      return recordStoreName.length() + 4 + 8 + 4;
   }

   public long getLastModified() throws StoreNotOpenException {
      if (!isOpen) {
         throw new StoreNotOpenException();
      }

      synchronized (this) {
         return lastModified;
      }
   }

   public String getName() throws StoreNotOpenException {
      if (!isOpen) {
         throw new StoreNotOpenException();
      }

      return recordStoreName;
   }

   public int getNextRecordID() throws StoreNotOpenException, StoreException {
      if (!isOpen) {
         throw new StoreNotOpenException();
      }

      synchronized (this) {
         return nextRecordID;
      }
   }

   public int getNumRecords() throws StoreNotOpenException {
      if (!isOpen) {
         throw new StoreNotOpenException();
      }

      synchronized (this) {
         return records.size();
      }
   }

   /**
    * Returns a copy of the original bytes
    * <br>
    * <br>
    * No caching.
    */
   public byte[] getRecord(int recordId) throws StoreNotOpenException, StoreInvalidIDException, StoreException {
      synchronized (r) {
         request(r, recordId);
         return r.getBytes();
      }
   }

   public int getRecord(int recordId, byte[] buffer, int offset) throws StoreNotOpenException, StoreInvalidIDException, StoreException {
      int recordSize;
      synchronized (r) {
         request(r, recordId);
         recordSize = r.getBytes(buffer, offset);
      }
      //System.out.println("RecordStoreImpl getRecord (" + offset + ") rid=" + recordId + " " + MUtils.debugString(buffer, offset, ","));
      fireRecordListener(ExtendedRecordListener.RECORD_READ, recordId);
      return recordSize;
   }

   private void request(Request r, int recordid) throws StoreNotOpenException, StoreInvalidIDException, StoreException {
      if (!isOpen) {
         throw new StoreNotOpenException();
      }
      r.recordid = recordid;
      r.key = new Integer(recordid);
      r.contains = false;
      //if the record is not inside. record id was never added
      if (!records.containsKey(r.key)) {
         throw new StoreInvalidIDException("Invalid RecordID " + recordid + " Valid Range is [" + getBase() + " " + (nextRecordID - 1) + "]");
      }
      r.data = records.get(r.key);
   }

   private Request r = new Request();

   /**
    * 
    */
   public int getRecordSize(int recordId) throws StoreNotOpenException, StoreInvalidIDException, StoreException {
      if (!isOpen) {
         throw new StoreNotOpenException();
      }
      synchronized (r) {
         request(r, recordId);
         byte[] data = r.data;
         if (data == null) {
            return 0;
         } else {
            return data.length;
         }
      }
   }

   public int getSize() throws StoreNotOpenException {
      if (!isOpen) {
         throw new StoreNotOpenException();
      }
      int size = 0;
      Set<Integer> keys = records.keySet();
      for (Integer integer : keys) {
         size += ((byte[]) records.get(integer)).length;
      }
      return size;
   }

   /**
    * Returns the amount of additional room (in bytes) available for this record store to grow. <br>
    * Note that this is not necessarily the amount of extra MIDlet-level data which can be stored, 
    * as implementations may store additional data structures with each record to support integration with native applications, synchronization, etc. 
    */
   public int getSizeAvailable() throws StoreNotOpenException {
      if (!isOpen) {
         throw new StoreNotOpenException();
      }

      return recordStoreManager.getSizeAvailable(this);
   }

   public int getVersion() throws StoreNotOpenException {
      if (!isOpen) {
         throw new StoreNotOpenException();
      }

      synchronized (this) {
         return version;
      }
   }

   public boolean isOpen() {
      return isOpen;
   }

   public void removeRecordListener(IRecordListener listener) {
      recordListeners.removeElement(listener);
   }

   /**
    * 
    * @param open
    */
   public void setOpen(boolean open) {
      this.isOpen = open;
      openCount++;
   }

   public void setRecord(int recordId, byte[] newData, int offset, int numBytes) throws StoreNotOpenException, StoreInvalidIDException, StoreException, StoreFullException {
      if (!isOpen) {
         throw new StoreNotOpenException();
      }
      // FIXME fixit
      if (numBytes > recordStoreManager.getSizeAvailable(this)) {
         throw new StoreFullException();
      }

      byte[] recordData = new byte[numBytes];
      System.arraycopy(newData, offset, recordData, 0, numBytes);

      synchronized (this) {
         Integer id = new Integer(recordId);
         if (records.remove(id) == null) {
            throw new StoreInvalidIDException("Record ID " + recordId + " is not valid for a put");
         }
         records.put(id, recordData);
         version++;
         lastModified = System.currentTimeMillis();
      }

      recordStoreManager.saveChanges(this);

      fireRecordListener(ExtendedRecordListener.RECORD_CHANGE, recordId);
   }

   /**
    * Actually write the whole stuff to disk
    * <br>
    * <br>
    * 
    * @param dos
    * @throws IOException
    */
   public void write(DataOutputStream dos) throws IOException {
      dos.writeUTF(recordStoreName);
      dos.writeInt(VER);
      dos.writeInt(version);
      dos.writeLong(lastModified);
      dos.writeInt(nextRecordID);

      Set<Integer> en = records.keySet();
      int size = en.size();
      dos.writeInt(size);
      for (Integer key : en) {
         dos.writeInt(key.intValue());
         byte[] data = (byte[]) records.get(key);
         if (data != null) {
            dos.writeInt(data.length);
            dos.write(data);
         } else {
            dos.writeInt(-1);
         }
      }
   }

   public void setMode(int authmode, boolean writable) throws StoreException {
      //can't implement this yet
   }

   //#mdebug
   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "RecordStoreHashMap");
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx sb) {
      sb.append("#RecordStoreHashtable ");
      sb.append("isOpen=" + isOpen);
      sb.append("openCount=" + openCount);
      sb.append("lastModified=" + lastModified);
      sb.append("nextRecordID=" + nextRecordID);
      Set<Integer> en = records.keySet();
      for (Integer str : en) {
         byte[] data = records.get(str);
         sb.nl();
         sb.append(str.toString() + " : " + cdc.getUC().getIU().debugString(data, ",", 10));
      }
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "RecordStoreHashMap");
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return cdc.getUC();
   }

   //#enddebug

}
