/**
 *
 */
package changeminer.HandlerForRA;

import java.sql.SQLException;
import com.itplus.cm.parser.common.CMParserCommonData;
import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.data.DataForAnalyzer;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import extractor.common.tobj.TDpd;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;

/**
 *
 */
public class SampleCase2_Handler extends HandlerForRA
{
    /**
     *
     */

    public SampleCase2_Handler() {

    }

   /**
	*
	**/
    public String getName() {
        return this.getClass().getName();
    }

    /**
    *  ｻ鄲?ﾀﾛｾ｡ ｴ?ﾑ ﾁ､ﾀﾇﾀﾔｴﾏｴﾙ.
    **/
    public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
        return RETURN_CONTINUE;
    }

   /**
	*  ｻ酳ﾄ ﾀﾛｾ｡ ｴ?ﾑ ﾁ､ﾀﾇﾀﾔｴﾏｴﾙ.
	**/
    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
        return RETURN_CONTINUE;
    }

   /**
	*
	**/
    public int doTObj(int depth, CM_SRC cm_src, TObj tobj, long parent_object_id) {
        log.trace("HANDLER", depth + " : " + tobj.getName() + " : " + tobj.getTempMap());
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>> " + tobj.getName());
        return RETURN_CONTINUE;
    }

   /**
	*
	**/
    public int doTDpd(int depth, TDpd tdpd, CM_SRC cm_src, CM_OBJ cm_obj, int seq) throws SQLException {
        log.trace("HANDLER", depth + " : " + tdpd.getName() + " : " + tdpd.getTempMap());
        return RETURN_CONTINUE;
    }

   /**
	*
	**/
    public TResult getTTresult() {
        // TODO Auto-generated method stub
        return null;
    }

   /**
	*
	**/
    public long generateGID(String prefix, TObj tobj) {
        return 0L;
    }

   /**
	*
	**/
    public long generateGID(String prefix, TDpd tdpd) {
        return 0L;
    }

	/**
	*
	**/
    public String getObjName(boolean is_file, CM_SRC cm_src, String full_object_name)
    {
    	return null;
    }

    /**
    *
	**/
    public void addAnalyzeStepOnError(CM_SRC cm_src, TResult tresult) {
    }
}
