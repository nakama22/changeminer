package changeminer.HandlerForRA;

import java.sql.SQLException;

import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.FileUtil;
import com.itplus.cm.parser.common.CMParserCommonData;

import extractor.common.tobj.TDpd;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;

public class JCL_RA_HANDLER  extends HandlerForRA{


		public final int JCL_CALL_PROGRAM = 1300002;
		public final int JCL_TO_UTIL = 1311006;

	    public JCL_RA_HANDLER() {

	    }


	    public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
	        return RETURN_CONTINUE;
	    }

	   public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
	        return RETURN_CONTINUE;
	    }

	   public int doTObj(int depth, CM_SRC cm_src, TObj tobj, long parent_object_id) {
		   int tdpdType = 0;
		   String tdpdName = "";
			for(int i=0; i<tobj.getTDpdList().length; i++) {
				tdpdName = tobj.getTDpdList()[0].getName();
				tdpdType =  tobj.getTDpdList()[0].getType();
				if(tdpdType == JCL_CALL_PROGRAM) {
					if(tdpdName.startsWith("JDJ") || tdpdName.startsWith("JSD") ||  tdpdName.startsWith("IEB")) {
						tobj.add(new TDpd (JCL_TO_UTIL, tdpdName ,tdpdName,100,  tobj.getTLocation()));
						break;
					}
				}
			}
	        return RETURN_CONTINUE;
	    }

	   public int doTDpd(int depth, TDpd tdpd, CM_SRC cm_src, CM_OBJ cm_obj, int seq) throws SQLException {
		
	        return RETURN_CONTINUE;
	    }



	   public long generateGID(String prefix, TObj tobj) {
	        return 0L;
	    }

	   public long generateGID(String prefix, TDpd tdpd) {
	    	if (tdpd.getType() == JCL_CALL_PROGRAM) {
	    		return FileUtil.getGID("<COBOL>", tdpd.getGID());

	    	}else if (tdpd.getType() == JCL_TO_UTIL){
	    		return FileUtil.getGID("<FILE>", "/"+tdpd.getGID());
	    	}
	        return 0L;
	    }



}
