package pasa.cbentley.framework.coredata.src5.ctx;

import pasa.cbentley.byteobjects.src4.core.interfaces.IBOCtxSettings;

public interface IBOCtxSettingCoreData5 extends IBOCtxSettings {

   public static final int CTX_COREDATA_BASIC_SIZE             = CTX_BASIC_SIZE + 5;

   /**
    * When set, the alias mode defined here overrides any locally defined mode.
    */
   public static final int CTX_COREDATA_FLAG_01_OVERRIDE_ALIAS = 1 << 0;

   public static final int CTX_COREDATA_OFFSET_01_FLAG1        = CTX_BASIC_SIZE;

}
