package pasa.cbentley.framework.core.data.src5.engine;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.Vector;

import pasa.cbentley.framework.core.data.src4.ex.StoreInvalidIDException;
import pasa.cbentley.framework.core.data.src4.ex.StoreNotOpenException;
import pasa.cbentley.framework.core.data.src4.interfaces.IRecordComparator;
import pasa.cbentley.framework.core.data.src4.interfaces.IRecordEnumeration;
import pasa.cbentley.framework.core.data.src4.interfaces.IRecordFilter;
import pasa.cbentley.framework.core.data.src4.interfaces.IRecordListener;
import pasa.cbentley.framework.core.data.src4.interfaces.IRecordStore;

public class RecordEnumerationVector implements IRecordEnumeration {

   /**
    * 
    */
   private RecordStoreHashMap        recordStoreImpl;

   private IRecordFilter             filter;

   private IRecordComparator         comparator;

   private boolean                   keepUpdated;

   private Vector<EnumerationRecord> enumerationRecords = new Vector<EnumerationRecord>();

   private int                       currentRecord;

   private IRecordListener           recordListener     = new IRecordListener() {

                                                           public void recordAdded(IRecordStore recordStore, int recordId) {
                                                              rebuild();
                                                           }

                                                           public void recordChanged(IRecordStore recordStore, int recordId) {
                                                              rebuild();
                                                           }

                                                           public void recordDeleted(IRecordStore recordStore, int recordId) {
                                                              rebuild();
                                                           }

                                                        };

   public RecordEnumerationVector(RecordStoreHashMap recordStoreImpl, IRecordFilter filter, IRecordComparator comparator, boolean keepUpdated) {
      this.recordStoreImpl = recordStoreImpl;
      this.filter = filter;
      this.comparator = comparator;
      this.keepUpdated = keepUpdated;

      rebuild();

      if (keepUpdated) {
         recordStoreImpl.addRecordListener(recordListener);
      }
   }

   public int numRecords() {
      return enumerationRecords.size();
   }

   public byte[] nextRecord() throws StoreInvalidIDException, StoreNotOpenException {
      if (!recordStoreImpl.isOpen()) {
         throw new StoreNotOpenException();
      }

      if (currentRecord >= numRecords()) {
         throw new StoreInvalidIDException();
      }

      byte[] result = enumerationRecords.elementAt(currentRecord).value;
      currentRecord++;

      return result;
   }

   public int nextRecordId() throws StoreInvalidIDException {
      if (currentRecord >= numRecords()) {
         throw new StoreInvalidIDException();
      }

      int result = enumerationRecords.elementAt(currentRecord).recordId;
      currentRecord++;

      return result;
   }

   public byte[] previousRecord() throws StoreInvalidIDException, StoreNotOpenException {
      if (!recordStoreImpl.isOpen()) {
         throw new StoreNotOpenException();
      }
      if (currentRecord < 0) {
         throw new StoreInvalidIDException();
      }

      byte[] result = enumerationRecords.elementAt(currentRecord).value;
      currentRecord--;

      return result;
   }

   public int previousRecordId() throws StoreInvalidIDException {
      if (currentRecord < 0) {
         throw new StoreInvalidIDException();
      }

      int result = enumerationRecords.elementAt(currentRecord).recordId;
      currentRecord--;

      return result;
   }

   public boolean hasNextElement() {
      if (currentRecord == numRecords()) {
         return false;
      } else {
         return true;
      }
   }

   public boolean hasPreviousElement() {
      if (currentRecord == 0) {
         return false;
      } else {
         return true;
      }
   }

   public void reset() {
      currentRecord = 0;
   }

   public void rebuild() {
      enumerationRecords.removeAllElements();
      //
      // filter
      //
      Set<Integer> set = recordStoreImpl.records.keySet();
      for (Integer key : set) {
         byte[] data = (byte[]) recordStoreImpl.records.get(key);
         if (filter != null && !filter.matches(data)) {
            continue;
         }
         enumerationRecords.add(new EnumerationRecord(((Integer) key).intValue(), data));
      }

      // sort
      if (comparator != null) {
         Collections.sort(enumerationRecords, new Comparator<EnumerationRecord>() {

            public int compare(EnumerationRecord lhs, EnumerationRecord rhs) {
               int compare = comparator.compare(lhs.value, rhs.value);
               if (compare == IRecordComparator.EQUIVALENT)
                  return 0;
               else if (compare == IRecordComparator.FOLLOWS)
                  return 1;
               else
                  return -1;

            }
         });
      }
   }

   public void keepUpdated(boolean keepUpdated) {
      if (keepUpdated) {
         if (!this.keepUpdated) {
            rebuild();
            recordStoreImpl.addRecordListener(recordListener);
         }
      } else {
         recordStoreImpl.removeRecordListener(recordListener);
      }

      this.keepUpdated = keepUpdated;
   }

   public boolean isKeptUpdated() {
      return keepUpdated;
   }

   public void destroy() {
   }

   class EnumerationRecord {
      int    recordId;

      byte[] value;

      EnumerationRecord(int recordId, byte[] value) {
         this.recordId = recordId;
         this.value = value;
      }
   }

}
