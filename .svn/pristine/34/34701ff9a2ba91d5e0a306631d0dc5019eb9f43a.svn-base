/**
 * This is Sample Template for HandlerForUSA
 *
 * do not edit package name
 */
package changeminer.HandlerForUSA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.itplus.cm.ce.addon.common.custom.HandlerForUSA;
import com.itplus.cm.ce.internal.meta.CM_OBJ_DPD;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.FileUtil;


public class USA_Handler_SampleCase2 extends HandlerForUSA
{
    /**
     *
     */
    private static final long serialVersionUID = -55824611275612863L;
    private final long JAVA_TO_XML = 3112031;

   /**
	*
	**/
    public USA_Handler_SampleCase2 ()
    {
    }

    /**
     * generate DPD.
     *
     *
     */
    public boolean generateDPD(String PREFIX, CM_SRC cm_src, CM_OBJ_DPD cm_obj_dpd) {
        boolean is_sql = false;

        // cm_obj_dpd.setCALL_TARGET("/ChangeMiner/sql/" + cm_obj_dpd.getCALL_TARGET());
        // cm_obj_dpd.setGID(FileUtil.getGID("<SOME-PREFIX>", cm_obj_dpd.getCALL_TARGET()));
        if(cm_obj_dpd.getDPD_TYPE_ID() == JAVA_TO_XML){
        	//cm_obj_dpd.setCALL_TARGET("<XML>" + cm_obj_dpd.getCALL_TARGET());
        	System.out.println("***************>>>>>>> " +  cm_obj_dpd.getCALL_TARGET() + " ************************* " + cm_obj_dpd.getGID());
            cm_obj_dpd.setGID(FileUtil.getGID("<XML>", cm_obj_dpd.getCALL_TARGET()));
            System.out.println("************************************** " + cm_obj_dpd.getGID());
        }


        return is_sql;
    }
}
