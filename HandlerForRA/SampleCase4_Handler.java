package changeminer.HandlerForRA;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itplus.cm.parser.common.CMParserCommonData;
import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.data.DataForAnalyzer;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.Environment;
import com.itplus.cm.ce.util.FileUtil;

import extractor.common.tobj.TDpd;
import extractor.common.tobj.TLocation;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;
public class SampleCase4_Handler extends HandlerForRA
{
    private final int USE_SH_FILE_DPD = 6411006;
    public SampleCase4_Handler() {}
    public String getName() {return this.getClass().getName();}

    public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
        return RETURN_CONTINUE;
    }

    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {

    	String file_name = Environment.getSourceDir() + cm_src.getCOLLECT_ID() + cm_src.getSPATH() + cm_src.getSNAME();
		TObj obj_root = tresult.getTObjList()[0];

		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(file_name)));
			String lineStr = null;
			int cnt = 0;
			while((lineStr = br.readLine()) != null){

				cnt++;
				String checkStr = checkRex(lineStr);
				if(checkStr != null){
					String targetName = "/jp1/lifesystem/TsBatch_PT5/terabatch/script/"+checkStr+".sh";
					obj_root.add(new TDpd(USE_SH_FILE_DPD, targetName, targetName, 100, new TLocation(cnt)));
				}
			}

			br.close();

		}catch(Exception e){
			System.out.println(e.getMessage());
		}

        return RETURN_CONTINUE;
    }

    public int doTObj(int depth, CM_SRC cm_src, TObj tobj, long parent_object_id) {
        log.trace("HANDLER", depth + " : " + tobj.getName() + " : " + tobj.getTempMap());
        return RETURN_CONTINUE;
    }
    public int doTDpd(int depth, TDpd tdpd, CM_SRC cm_src, CM_OBJ cm_obj, int seq) throws SQLException {
        log.trace("HANDLER", depth + " : " + tdpd.getName() + " : " + tdpd.getTempMap());
        return RETURN_CONTINUE;
    }
    public TResult getTTresult() {
        return null;
    }
    public long generateGID(String prefix, TObj tobj) {
        return 0L;
    }
    public long generateGID(String prefix, TDpd tdpd) {
    	if(tdpd.getType() == USE_SH_FILE_DPD){
    		return FileUtil.getGID("<FILE>", tdpd.getGID());
    	}
        return 0L;
    }
    public String getObjName(boolean is_file, CM_SRC cm_src, String full_object_name)
    {
    	return null;
    }
    public void addAnalyzeStepOnError(CM_SRC cm_src, TResult tresult) {
    }

    private String checkRex(String str){

    	String value = null;
		String rex = "te=\"([A-Z0-9]*).sh\"";
		Pattern p = Pattern.compile(rex);
		Matcher m = p.matcher(str);
		if (m.find()){
			value = m.group(1);
		}else{

		}
		return value;
    }
}
