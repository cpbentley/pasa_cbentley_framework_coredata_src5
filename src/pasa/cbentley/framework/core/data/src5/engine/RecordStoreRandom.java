package pasa.cbentley.framework.core.data.src5.engine;

import java.io.RandomAccessFile;

import pasa.cbentley.framework.core.data.src4.engine.Request;
import pasa.cbentley.framework.core.data.src4.ex.StoreException;
import pasa.cbentley.framework.core.data.src4.ex.StoreInvalidIDException;
import pasa.cbentley.framework.core.data.src4.ex.StoreNotOpenException;
import pasa.cbentley.framework.core.data.src5.ctx.CoreData5Ctx;

/**
 * Record the size of header.
 * 
 * When records have the same size, reading is easy
 * <br>
 * When records don't have the same size, 
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class RecordStoreRandom {
   private Request          r = new Request();

   private RandomAccessFile raf;

   private boolean isOpen;

   /**
    * Header read
    */
   private boolean isInit;
   
   
   /**
    * Init
    * @param raf
    */
   public RecordStoreRandom(CoreData5Ctx cdc, RandomAccessFile raf) {

      this.raf = raf;
      
      //init by reading the header for the record sizes.
      
   }

   private long getPos(int rid) {
      return 0;
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

   private void request(Request r, int rid) throws StoreNotOpenException, StoreInvalidIDException, StoreException {
      if (!isOpen) {
         throw new StoreNotOpenException();
      }
      r.recordid = rid;
      r.key = new Integer(rid);
      r.contains = false;
      //compute size of record
      //raf.seek(getPos(rid));
      //raf.readFully(b);
      //r.data = records.get(r.key);
   }
}
