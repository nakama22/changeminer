/**
 * This is Sample Template for HandlerForUSA
 *
 * do not edit package name
 */
package changeminer.HandlerForUSA;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itplus.cm.ce.addon.common.custom.HandlerForUSA;
import com.itplus.cm.ce.internal.meta.CM_OBJ_DPD;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.FileUtil;



public class USA_Handler_Case6 extends HandlerForUSA
{
    /**
     *
     */
    private static final long serialVersionUID = -55824611275612863L;


   /**
	*
	**/
    public USA_Handler_Case6 ()
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
        if(cm_obj_dpd.getDPD_TYPE_ID() == 1009001){


        	System.out.println("########################################PREFIX " + PREFIX);
        	System.out.println("########################################getSRC_ID " + cm_src.getSRC_ID());

        	System.out.println("########################################getDPD_ID " + cm_obj_dpd.getDPD_ID());
        	System.out.println("########################################getCALLER_OBJ_ID " + cm_obj_dpd.getCALLER_OBJ_ID());
        	System.out.println("########################################getCALL_TARGET" + cm_obj_dpd.getCALL_TARGET());

        	String sql = cm_obj_dpd.getCALL_TARGET();
        	String regex = "__UNKNOWN_TOP__";
        	Pattern p = Pattern.compile(regex);

        	Matcher m = p.matcher(sql);
        	if (m.find()){
        		String newSql = m.replaceAll("");
        		System.out.println(newSql);
        		cm_obj_dpd.setCALL_TARGET("/TB/" + newSql);
                cm_obj_dpd.setGID(FileUtil.getGID("/TB/", newSql));
        	}

        }

        return is_sql;
    }
}
