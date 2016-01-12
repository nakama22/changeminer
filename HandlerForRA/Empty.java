package changeminer.HandlerForRA;

import java.sql.SQLException;

import com.itplus.cm.parser.common.CMParserCommonData;
import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;

import extractor.common.tobj.TDpd;
import extractor.common.tobj.TLocation;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;

public class Empty extends HandlerForRA
{

    public Empty() {

    }

    public String getName() {
        return this.getClass().getName();
    }

    public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
        return RETURN_CONTINUE;
    }

    public int addCheck(TObj root_obj, String targetFile) throws Exception {
    	return 0;
    }

    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
        return RETURN_CONTINUE;
    }

    public int doTObj(int depth, CM_SRC cm_src, TObj tobj, long parent_object_id) {
        return RETURN_CONTINUE;
    }

    public int doTDpd(int depth, TDpd tdpd, CM_SRC cm_src, CM_OBJ cm_obj, int seq) throws SQLException {
        return RETURN_CONTINUE;
    }

    public TResult getTTresult() {
        return null;
    }

    public long generateGID(String prefix, TObj tobj) {
        return 0L;
    }

    public long generateGID(String prefix, TDpd tdpd) {
        return 0L;
    }

    public String getObjName(boolean is_file, CM_SRC cm_src, String full_object_name)
    {
    	return null;
    }

    public void addAnalyzeStepOnError(CM_SRC cm_src, TResult tresult) {
    }

    public static void main(String[] args) {
		Empty empty = new Empty();
		TObj obj_root = new TObj(1, "", "", 100, new TLocation() );
		String target = "C:\\Users\\ito-motoi\\Desktop\\MQ\\6_APPINFO.csv";
		try {

			empty.addCheck(obj_root, target);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
