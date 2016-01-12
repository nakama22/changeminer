/**
 * This is Sample Template for HandlerForUSA
 *
 * do not edit package name
 */
package changeminer.HandlerForUSA;

import com.itplus.cm.ce.addon.common.custom.HandlerForUSA;
import com.itplus.cm.ce.internal.meta.CM_OBJ_DPD;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.FileUtil;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import extractor.common.tobj.TDpd;

public class USE_TABLE_USA extends HandlerForUSA
{
    /**
     *
     */
    private static final long serialVersionUID = -55824611275612863L;


   /**
	*
	**/
    public USE_TABLE_USA ()
    {
    }

    /**
     * generate DPD.
     *
     *
     */
    public boolean generateDPD(String PREFIX, CM_SRC cm_src, CM_OBJ_DPD cm_obj_dpd) {
        boolean is_sql = false;
   		if(cm_obj_dpd.getDPD_TYPE_ID() == 1009001){


   			System.out.println("**************************************");
   			System.out.println("**************************************"  + cm_src.getSNAME());
   			
   			
        	String sql = cm_obj_dpd.getCALL_TARGET();
			System.out.println("Call_Target >>>>> "  + sql);
            String regex = "__UNKNOWN_TOP__";
        	Pattern p = Pattern.compile(regex);

        	Matcher m = p.matcher(sql);
        	if (m.find()){
        		String newSql = m.replaceAll("");
        		System.out.println("new Target >>>>> " + newSql);
        		//cm_obj_dpd.setCALL_TARGET("/TB/" +newSql);
				cm_obj_dpd.setCALL_TARGET(newSql);
                cm_obj_dpd.setGID(FileUtil.getGID("/TB/", newSql));


        	}


        }

        return is_sql;
    }

}
