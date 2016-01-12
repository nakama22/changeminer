package changeminer.HandlerForRA;

import java.sql.SQLException;
import com.itplus.cm.parser.common.CMParserCommonData;
import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.Environment;
import com.itplus.cm.ce.util.FileUtil;
import extractor.common.tobj.TDpd;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;

public class NS_COBOL_UPDATE_GID extends HandlerForRA
{
    public NS_COBOL_UPDATE_GID() {

    }

    public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
        return RETURN_CONTINUE;
    }

    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
    	String file_name = Environment.getSourceDir() + "/" + cm_src.getCOLLECT_ID() + cm_src.getSPATH() + cm_src.getSNAME();
		TObj obj_root = tresult.getTObjList()[0];

		obj_root.setGID( "/" + cm_src.getSNAME());

        return RETURN_CONTINUE;
    }

       public int doTObj(int depth, CM_SRC cm_src, TObj tobj, long parent_object_id) {
        return RETURN_CONTINUE;
    }

    public int doTDpd(int depth, TDpd tdpd, CM_SRC cm_src, CM_OBJ cm_obj, int seq) throws SQLException {
        return RETURN_CONTINUE;
    }

    public long generateGID(String prefix, TObj tobj) {
        return 0L;
    }

    public long generateGID(String prefix, TDpd tdpd) {
        return 0L;
    }

    public void addAnalyzeStepOnError(CM_SRC cm_src, TResult tresult) {
    }
}
