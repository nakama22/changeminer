/**
 *
 */
package changeminer.HandlerForRA;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itplus.cm.parser.common.CMParserCommonData;
import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.Environment;
import com.itplus.cm.ce.util.FileUtil;

import extractor.common.tobj.TDpd;
import extractor.common.tobj.TLocation;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;


public class NS_MPP_C_HANDLER extends HandlerForRA
{

	public static final int C_CALL_MQ = 5111001;

	//public String callMqRex = "it_stgopn\\([\\s]*(.+)[\\s]*,[\\s]*(.+)[\\s]*,[\\s]*(.+)[\\s]*,[\\s]*(.+)[\\s]*,[\\s]*(.+)[\\s]*\\)";
	public String callMqRex = "it_stgopn\\(([^\\)]+)\\)";

	public NS_MPP_C_HANDLER() {

    }

    public String getValue(String lineStr, String rex, int index) throws Exception {


    	String value = "";
		Pattern p = Pattern.compile(rex);
		Matcher m = p.matcher(lineStr);



		if(m.find()){
			value = m.group(index);
			checkValue(value);
		}

		return value;
	}


    public void checkMQCall(TObj obj_root, String lineStr, int line) throws Exception{

    	Pattern p = Pattern.compile(callMqRex);
		Matcher m = p.matcher(lineStr);

		//System.out.println(line +  " : line >> " + lineStr);
		if(m.find()){
			System.out.println("		checkMQCall >> " + lineStr + "   " + line);
			//System.out.println("		checkMQCall param >> " + m.group(1) );
			TDpd dpd = new TDpd(C_CALL_MQ, "MQ", "MQ", 100, new TLocation(line));
			Map tmpMap = dpd.getTempMap();
			tmpMap.put("DETAIL_CD", "100");

			obj_root.add(dpd);
		}
    }




    public void checkValue(String value) throws Exception{
		if(value == null){
			throw new Exception("Value is Null");
		}
	}

    public String getName() {
        return this.getClass().getName();
    }


    public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
		return RETURN_CONTINUE;
    }

    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {

		String targetFile = Environment.getSourceDir() + cm_src.getCOLLECT_ID() + cm_src.getSPATH() + cm_src.getSNAME();
;
		if(cm_src.getSNAME().toLowerCase().contains(".c")) { return RETURN_CONTINUE; }


    	System.out.println("Start Handler File == >> " + cm_src.getSNAME());

    	TObj obj_root = tresult.getTObjList()[0];

    	obj_root.setGID("/" + cm_src.getSNAME());
    	System.out.println("Update Gid FileGID >> " + obj_root.getGID());

    	addStep(targetFile, obj_root);


		return RETURN_CONTINUE;
    }

    public void addStep(String srcPath, TObj obj_root){


    	FileInputStream fis = null;
		BufferedReader br = null;

		int line = 0;

		try {
			fis = new FileInputStream(srcPath);
			br = new BufferedReader(new InputStreamReader(fis));
			String line_data = "";
			while (br.ready()) {

				line++;

				line_data = br.readLine();
				String trimData = line_data.trim();
				if(trimData.startsWith("//") || (trimData.startsWith("/*") && trimData.endsWith("*/"))){
					continue;
				}

				checkMQCall(obj_root, line_data, line);


			}

		} catch(Exception e) {
			e.printStackTrace();
		} finally{

			if(br != null){

				try{
					br.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			if(fis != null){

				try{
					fis.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
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

    	if(tdpd.getType() == C_CALL_MQ){

    		System.out.println("TYPE >> " + tdpd.getType());
    		System.out.println("GID >> " + tdpd.getGID());

			return FileUtil.getGID("<FILE>", tdpd.getGID());
    	}

		return 0L;
    }

    public void addAnalyzeStepOnError(CM_SRC cm_src, TResult tresult) {
    }

    public static void main(String[] args) {

    	NS_MPP_C_HANDLER handler = new NS_MPP_C_HANDLER();
    	TObj cmdobj = new TObj(11111, "TEST", "TEST", 100, new TLocation(1) );

    	String path = "C:\\Users\\ito-motoi\\Desktop\\PJ\\NISSAN\\Source\\PCS_TAI\\NISSAN\\01_SCREEN\\02_MPP\\MPP_C\\MPP_C\\MPP\\N6BTH\\N6BTH\\N6BTH.pc";

    	handler.addStep(path, cmdobj);

    }



}
