/**
 *
 */
package changeminer.HandlerForRA;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
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


public class XML_WEBSERVICE_HANDLER extends HandlerForRA
{

	//public String objectIdRex = "<object\\s+id=\\\"([^\\s]+)\\\"\\s+plant=\\\"([^\\s]+)\\\"\\s+type=\\\"([^\\s]+),\\s+([^\\s]+)\\\"\\s+version=\\\"\\\"\\s+lang=\\\"([^\\s]+)\\\"\\s+singleton=\\\"([^\\s]+)\\\"\\s*/>";
	public final String idRex = "id=\\\"([^\\s]+)\\\"";
	public final String typeRex = "type=\\\"([^\\s]+),\\s+([^\\s]+)\\\"";
	public final String langRex = "lang=\\\"([^\\s]+)\\\"";

	public final int OBJECT_ID_OBJ = 821172;
	public final int USE_MPP_VB_DPD = 8211010;
	public final int USE_MPP_COBOL_DPD = 8211011;
	public final int USE_MPP_C_DPD = 8211012;
	public final int MPP_COBOL_WRAPPER = 8211013;

	public final String vbLang = "VB";
	public final String cblLang = "CBL";
	public final String cLang = "C";


    public XML_WEBSERVICE_HANDLER() {

    }

    public void checkValue(String value) throws Exception{
		if(value == null || value.trim().length() == 0){
			throw new Exception("Value is Null");
		}
	}

    public String getObjectId(String line) throws Exception {
    	String value ="";
    	Pattern p = Pattern.compile(idRex);
		Matcher m = p.matcher(line);

		if(m.find()){
			value = m.group(1);
			checkValue(value);
		}

		return value;
    }

    public String getLangType(String line) {
    	String value ="";
    	Pattern p = Pattern.compile(langRex);
		Matcher m = p.matcher(line);

		if(m.find()){
			value = m.group(1);
		}

		return value;
    }

    public String[] getType(String line) throws Exception {
    	String[] values = new String[2];
    	Pattern p = Pattern.compile(typeRex);
		Matcher m = p.matcher(line);

		if(m.find()){
			String targetDllNamespace = m.group(1);
			String targetDllWrapper = m.group(2);
			checkValue(targetDllNamespace);
			checkValue(targetDllWrapper);
			values[0] = targetDllNamespace;
			values[1] = targetDllWrapper;
		}

		return values;
    }

    public boolean checkData(String objId, String[] type){
    	boolean isOK = false;
    	if((objId != null && objId.trim().length() != 0) &&
    			(type[0] != null && type[0].trim().length() != 0) &&
    			(type[0] != null && type[0].trim().length() != 0)){
    		isOK = true;
    	}

    	return isOK;

    }

    public String getName() {
        return this.getClass().getName();
    }


    public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
		return RETURN_CONTINUE;
    }


    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
		String targetFile = Environment.getSourceDir() + cm_src.getCOLLECT_ID() + cm_src.getSPATH() + cm_src.getSNAME();
    	System.out.println("Start Handler File == >> " + cm_src.getSNAME());

    	TObj obj_root = tresult.getTObjList()[0];


    	FileInputStream fis = null;
		BufferedReader br = null;

		int line = 0;

		try {
			fis = new FileInputStream(targetFile);
			br = new BufferedReader(new InputStreamReader(fis));
			String line_data = "";

			while (br.ready()) {

				line++;

				line_data = br.readLine();

				if(line_data.trim().startsWith("<!--") || line_data.trim().length() == 0){
					continue;
				}


				String objectId = getObjectId(line_data);
				String[] types = getType(line_data);

				if(checkData(objectId, types)){

					TObj objectTobj = new TObj(OBJECT_ID_OBJ, objectId, objectId, 100, new TLocation(line));

					String langType = getLangType(line_data);


					if(langType.equals(vbLang)){

						objectTobj.add(new TDpd(USE_MPP_VB_DPD, types[0], types[0], 100,  new TLocation(line)));
					}else if(langType.equals(cblLang)){
						//dpdType = USE_MPP_COBOL_DPD;
						//target = objectId + "Cbl";
						String targetCbl = objectId + "Cbl";
						objectTobj.add(new TDpd(USE_MPP_COBOL_DPD, targetCbl, targetCbl, 100,  new TLocation(line)));
						objectTobj.add(new TDpd(MPP_COBOL_WRAPPER, types[0], types[0], 100,  new TLocation(line)));
					}else if(langType.equals(cLang)){

						String targetC = types[1].replace("Wrap", ".pc");
						objectTobj.add(new TDpd(USE_MPP_C_DPD, targetC, targetC, 100,  new TLocation(line)));
					}

					obj_root.add(objectTobj);

				}

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
        return RETURN_CONTINUE;
    }


    public int doTObj(int depth, CM_SRC cm_src, TObj tobj, long parent_object_id) {
        return RETURN_CONTINUE;
    }


    public int doTDpd(int depth, TDpd tdpd, CM_SRC cm_src, CM_OBJ cm_obj, int seq) throws SQLException {
        return RETURN_CONTINUE;
    }

    public long generateGID(String prefix, TObj tobj) {
    	if(tobj.getType() == OBJECT_ID_OBJ){
			return FileUtil.getGID("<FILE>", tobj.getGID());
    	}
        return 0L;
    }

    public long generateGID(String prefix, TDpd tdpd) {
    	if(tdpd.getType() == USE_MPP_VB_DPD){
			return FileUtil.getGID("<.NET>", tdpd.getGID());
    	} else if(tdpd.getType() == MPP_COBOL_WRAPPER){
			return FileUtil.getGID("<.NET>", tdpd.getGID());
    	} else if(tdpd.getType() == USE_MPP_COBOL_DPD){
			System.out.println("USE_MPP_COBOL_DPD >> " + tdpd.getGID());
    		return FileUtil.getGID("<COBOL>", tdpd.getGID().toUpperCase());
    	}else if(tdpd.getType() == USE_MPP_C_DPD){
			System.out.println("USE_MPP_C_DPD >> " + tdpd.getGID());
    		return FileUtil.getGID("<EC>", "/"+tdpd.getGID());
    	}
        return 0L;
    }

    public void addAnalyzeStepOnError(CM_SRC cm_src, TResult tresult) {
    }
}
