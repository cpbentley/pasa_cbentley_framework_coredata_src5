package pasa.cbentley.framework.coredata.src5.ctx;

import pasa.cbentley.framework.coredata.src4.ctx.IConfigCoreData;

/**
 * Parameters for configuration of {@link CoreData5Ctx}
 * @author Charles Bentley
 *
 */
public interface IConfigCoreData5 extends IConfigCoreData {

   /**
    * 
    * @return
    */
   public String getRMSPath();

   /**
    * Default is .rs for recordstore
    * @return
    */
   public String getFileExtension();

   /**
    * Data root directory
    * @return
    */
   public String getAppWorkingDirectory();

   /**
    * When true, Database is pure memory.
    * @return
    */
   public boolean isVolatileData();

}
