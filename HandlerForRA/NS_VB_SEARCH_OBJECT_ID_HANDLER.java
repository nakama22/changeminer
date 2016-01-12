/**
 *
 */
package changeminer.HandlerForRA;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
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


public class NS_VB_SEARCH_OBJECT_ID_HANDLER extends HandlerForRA
{
	public String vbObjIdGetRex = "gGetTransCode[\\s]+=[\\s]+\\\"([^\\s]+)\\\"";
	public String vbObjIdPutRex = "gPutTransCode[\\s]+=[\\s]+\\\"([^\\s]+)\\\"";
	public String reportTypeRex = "LibCrystalReport.Print\\(.+\\)";
	public String callMqFunctionRex = "[\\s]+fnMachData_Set\\([\\s]*(.+)[\\s]*,[\\s]*(.+)[\\s]*\\)";
	public String callMqParamRex = "^[\\s]*gMQExecPGM([\\s]*)=([\\s]*)True";
	public String callMqParamRex2 = "[\\s]*gUI.MyToolCode([\\s]*)=(.+)MCT600([^\\s]+)";
	public String funcTypeRex = "gPF\\([0-9]+\\)\\s+=\\s+(cPFExcel|cPFExcelCurrent|cPFExcelUpload|cPFExcelUploadReplace)";

	public final int CALL_OBJECT_ID = 2511022;
	public final int USE_EXCEL = 2511023;
	public final int USE_PRINT = 2511024;
	public final int CALL_MQ = 2511025;
	public final int UNKNOWN_TYPE = 2511026;

	public final int FIELDINFO_ROOT = 251135;
	public final int FIELDINFO_INDEX = 251136;
	public final int FIELDINFO_KEY = 251137;
	public final int DETAIL_VALUE = 2511027;


	public static HashMap<String,String> CATS_DLL = null;
	public static HashMap<String,String> CATS_DLL_LOCAL = null;
	public static ArrayList<String> FILTER_DLL = null;
	public boolean debugMode = true;

	public int[] checkTargetType = new int[]{ 2511006, 2511007, 2511008, 2511011, 2511012};
	public CM_SRC fileInfo = null;
	public TreeMap<Integer, TreeMap<String, String>> fieldInfoMap = new TreeMap<Integer, TreeMap<String, String>>();

    public NS_VB_SEARCH_OBJECT_ID_HANDLER() {

    }

    public void debugLog(String msg){
    	if(debugMode){
    		System.out.println(msg);
    	}
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

    public void checkGetObjId(TObj obj_root, String lineStr, int line) throws Exception{

    	String value = getValue(lineStr, vbObjIdGetRex, 1);

    	if(value.trim().length() != 0){
    		debugLog("checkGetObjId >> " + value);
    		obj_root.add(new TDpd(CALL_OBJECT_ID, value,value,100, new TLocation(line)));
    	}

    }

    public void checkPutObjId(TObj obj_root, String lineStr, int line) throws Exception{

    	String value = getValue(lineStr, vbObjIdPutRex, 1);

    	if(value.trim().length() != 0){
    		debugLog("checkPutObjId >> " + value);
    		obj_root.add(new TDpd(CALL_OBJECT_ID, value,value,100, new TLocation(line)));
    	}

    }

    public void checkPrint(TObj obj_root, String lineStr, int line) throws Exception{

    	Pattern p = Pattern.compile(reportTypeRex);
		Matcher m = p.matcher(lineStr);

		if(m.find()){
			debugLog("checkPrint >> " + lineStr);
    		obj_root.add(new TDpd(USE_PRINT, "LibCrystalReport", "LibCrystalReport",100, new TLocation(line)));
		}


    }

    public void checkMQCall(TObj obj_root, String lineStr, int line) throws Exception{
    	if(lineStr.contains("Function")){
    		return;
    	}
    	Pattern p = Pattern.compile(callMqFunctionRex);
		Matcher m = p.matcher(lineStr);

		if(m.find()){
			debugLog("checkMQCall >> " + lineStr);
			obj_root.add(new TDpd(CALL_MQ, "MQ", "MQ",100, new TLocation(line)));
		}
    }

    public void checkMQCallParam(TObj obj_root, String lineStr, int line) throws Exception{

    	Pattern p = Pattern.compile(callMqParamRex);
		Matcher m = p.matcher(lineStr);

		if(m.find()){
			debugLog("checkMQCallParam >> " + lineStr);
			obj_root.add(new TDpd(CALL_MQ, "MQ", "MQ",100, new TLocation(line)));
		}
    }

    public void checkMQCallParam2(TObj obj_root, String lineStr, int line) throws Exception{

    	Pattern p = Pattern.compile(callMqParamRex2);
		Matcher m = p.matcher(lineStr);

		if(m.find()){
			debugLog("checkMQCallParam >> " + lineStr);
			obj_root.add(new TDpd(CALL_MQ, "MQ", "MQ",100, new TLocation(line)));
		}
    }

    public void checkFiledInfo(TObj obj_root, String lineStr, int line) throws Exception{

    	if(lineStr.indexOf("FieldInfo") == -1) { return; }

    	String filedInfoRex = "FieldInfo\\(([0-9]+)\\)\\.([^\\s]+)[\\s]*=[\\s]*([^\\s]+)([\\s]+'[^\\s]+|[\\s]*)";

    	ArrayList<String> params = PatternUtil.getValues(lineStr, filedInfoRex);

    	if(params.size() != 0){
    		if(params.size() == 4){
    			createFieldInfo(params, line);
    		}else{
    			debugLog("** check Param index is >> " + params.size());
    		}
    	}
    }

    public void createFieldInfo(ArrayList<String> params, int line){
    	int index = Integer.parseInt(params.get(0));
    	String property = params.get(1);
    	String value = params.get(2).replace("\"", "");
    	String comment = params.get(3).trim().replace("'", "");
    	debugLog(String.format("FieldInfo(%d).%s = %s    %s", index, property,value,comment));

    	if(fieldInfoMap != null){
    		int key = index;
    		TreeMap<String,String> detail = new TreeMap<String,String>();
    		if(fieldInfoMap.containsKey(key)){
    			detail = fieldInfoMap.get(key);
    		}

    		detail.put(property, value);
    		String commentStr = detail.get("comment");
    		if(commentStr == null || commentStr.isEmpty()){
    			detail.put("comment", comment);
    		}else{
    			if(commentStr.equals(comment)){
    				commentStr += "," + comment;
    				detail.put(property, value);
    			}
    		}

    		fieldInfoMap.put(key, detail);

    	}

    }

    public void checkOutputType(TObj obj_root, String lineStr, int line) throws Exception{

    	String value = getValue(lineStr, funcTypeRex, 1);

    	if(value.trim().length() != 0){
    		debugLog("checkOutputType >> " + value);
    		if(value.equals("cPFPrint")){
    			//obj_root.add(new TDpd(USE_PRINT, value,value,100, new TLocation(line)));
    		}else{
    			obj_root.add(new TDpd(USE_EXCEL, value,value,100, new TLocation(line)));
    		}

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

    public int addCheck(CMParserCommonData data, CM_SRC cm_src, TResult tresult, String targetFile) throws Exception {

    	TObj obj_root = new TObj(1, "", "", 100, new TLocation() );
    	debugLog("Start >> " + targetFile);
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

				if(line_data.trim().startsWith("'")){
					continue;
				}

				checkGetObjId(obj_root, line_data, line);
				checkPutObjId(obj_root, line_data, line);
				checkOutputType(obj_root, line_data, line);
				checkPrint(obj_root, line_data, line);
				checkMQCall(obj_root, line_data, line);
				checkMQCallParam(obj_root, line_data, line);
				checkMQCallParam2(obj_root, line_data, line);
				checkFiledInfo(obj_root, line_data, line);




			}

			for(Entry<Integer, TreeMap<String, String>> entry : fieldInfoMap.entrySet()){
                int key = entry.getKey();
                TreeMap<String, String> detail = entry.getValue();
                debugLog("Key >> FieldInfo(" + key + ")");
                for(Entry<String, String> entry2 : detail.entrySet()){
                    String key2 = entry2.getKey();
                    String value = entry2.getValue();
                    debugLog("	key2 : " + key2 + " value :"  + value);
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

		return 0;
    }

    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {

    	initDllMap();

    	fileInfo = cm_src;

		String targetFile = Environment.getSourceDir() + cm_src.getCOLLECT_ID() + cm_src.getSPATH() + cm_src.getSNAME();
;
		if(cm_src.getSNAME().toLowerCase().contains(".vbproj")) { return RETURN_CONTINUE; }

    	debugLog("Start Handler File == >> " + cm_src.getSNAME());

    	TObj obj_root = tresult.getTObjList()[0];

    	FileInputStream fis = null;
		BufferedReader br = null;

		int line = 0;


		if(cm_src.getSNAME().toLowerCase().equals("fieldinfo.vb")){

			debugLog("FieldInfo Start");
			try {
				fis = new FileInputStream(targetFile);
				br = new BufferedReader(new InputStreamReader(fis));
				String line_data = "";

				fieldInfoMap.clear();
				fieldInfoMap = new TreeMap<Integer, TreeMap<String, String>>();

				while (br.ready()) {

					line++;

					line_data = br.readLine();

					if(line_data.trim().startsWith("'")){
						continue;
					}

					checkFiledInfo(obj_root, line_data, line);

				}

					TObj fieldInfoObj = new TObj(FIELDINFO_ROOT, "FieldInfo", "FieldInfo", 100, new TLocation(line) );
					debugLog("1 >> CreateRootObj");
					for(Entry<Integer, TreeMap<String, String>> entry : fieldInfoMap.entrySet()){
		                int key = entry.getKey();
		                TreeMap<String, String> detail = entry.getValue();
		                String fieldInfoIndexName = "FieldInfo(" + key + ")";
		                debugLog("2 >>" + fieldInfoIndexName);
		                TObj fieldInfoIndexObj = new TObj(FIELDINFO_INDEX, fieldInfoIndexName, fieldInfoIndexName, 100, new TLocation(line) );

		                for(Entry<String, String> entry2 : detail.entrySet()){
		                    String infoKey = entry2.getKey();
		                    String infoValue = entry2.getValue();

		                    TObj fieldInfoKeyObj = new TObj(FIELDINFO_KEY, infoKey, infoKey, 100, new TLocation(line));

		                    if(infoValue != null && !infoValue.isEmpty()){
		                    	fieldInfoKeyObj.add(new TDpd(DETAIL_VALUE, infoValue, infoValue, 100,  new TLocation(line) ));

		                    }
		                    fieldInfoIndexObj.add(fieldInfoKeyObj);
		                    debugLog("3 >> insert " + infoKey + " , " + infoValue);
		                }

		                fieldInfoObj.add(fieldInfoIndexObj);
		            }

					obj_root.add(fieldInfoObj);




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
    	}else{
    		try {
    			fis = new FileInputStream(targetFile);
    			br = new BufferedReader(new InputStreamReader(fis));
    			String line_data = "";

    			while (br.ready()) {

    				line++;

    				line_data = br.readLine();

    				if(line_data.trim().startsWith("'")){
    					continue;
    				}

    				checkGetObjId(obj_root, line_data, line);
    				checkPutObjId(obj_root, line_data, line);
    				checkOutputType(obj_root, line_data, line);
    				checkPrint(obj_root, line_data, line);
    				checkMQCall(obj_root, line_data, line);
    				checkMQCallParam(obj_root, line_data, line);
    				checkMQCallParam2(obj_root, line_data, line);
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



		return RETURN_CONTINUE;
    }


    public int doTObj(int depth, CM_SRC cm_src, TObj tobj, long parent_object_id) {
        return RETURN_CONTINUE;
    }


    public int doTDpd(int depth, TDpd tdpd, CM_SRC cm_src, CM_OBJ cm_obj, int seq) throws SQLException {


    	//if(tdpd.getType() == 2511006 || tdpd.getType() == 2511007 || tdpd.getType() == 2511008 || tdpd.getType() == 2511011){
    	if(isTargetType(tdpd.getType())){
    		chackDLLS(tdpd);

    	}

        return RETURN_CONTINUE;
    }

    public long generateGID(String prefix, TObj tobj) {
        return 0L;
    }

    public long generateGID(String prefix, TDpd tdpd) {
        if(tdpd.getType() == CALL_OBJECT_ID){
			return FileUtil.getGID("<FILE>", tdpd.getGID());
    	}else if(tdpd.getType() == USE_EXCEL){
			return FileUtil.getGID("<FILE>", tdpd.getGID());
    	}else if(tdpd.getType() == USE_PRINT){
			return FileUtil.getGID("<FILE>", "LibCrystalReport");
    	}else if(tdpd.getType() == CALL_MQ){
    		return FileUtil.getGID("<FILE>", tdpd.getGID());
    	}

		return 0L;
    }

    public void addAnalyzeStepOnError(CM_SRC cm_src, TResult tresult) {
    }

    public boolean isTargetType(int type){

    	boolean isTarget = false;

    	for(int i = 0; i < checkTargetType.length ; i ++){
    		if(type == checkTargetType[i]){

    			isTarget = true;
    			break;
    		}
    	}

    	return isTarget;

    }

    public void initDllMap(){
    	if(CATS_DLL == null){
    		CATS_DLL = new HashMap<String,String>();
    		CATS_DLL.put("unknown.ApplicationExit()", "Biz.Nissan.Cats.CORE.UI.LibInterface.ApplicationExit()");
    		CATS_DLL.put("unknown.ChangeControlByScreenState()", "Biz.Nissan.Cats.BaseForm.ChangeControlByScreenState()");
    		CATS_DLL.put("unknown.ClassLoad()", "Biz.Nissan.Cats.CORE.UI.LibInterface.ClassLoad()");
    		CATS_DLL.put("unknown.ClientFormLoad()", "Biz.Nissan.Cats.BaseForm.ChangeControlByScreenState()");
    		CATS_DLL.put("unknown.GetSystemInformation()", "Biz.Nissan.Cats.FormType01.GetSystemInformation()");
    		CATS_DLL.put("unknown.Init2()", "Biz.Nissan.Cats.CORE.UI.LibInterface.Init2()");
    		CATS_DLL.put("unknown.Kitty_Start(System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.Kitty_Start(System.String)");
    		CATS_DLL.put("unknown.PostScreenState()", "Biz.Nissan.Cats.BaseForm.PostScreenState()");
    		CATS_DLL.put("unknown.SetASearchClear()", "Biz.Nissan.Cats.BaseForm.SetASearchClear()");
    		CATS_DLL.put("unknown.SetStatusBarInfo()", "Biz.Nissan.Cats.BaseForm.SetStatusBarInfo()");
    		CATS_DLL.put("unknown.SpecialTextGotFocus()", "Biz.Nissan.Cats.BaseForm.SpecialTextGotFocus()");
    		CATS_DLL.put("unknown.lgRecordTotal()", "Biz.Nissan.Cats.CORE.UI.LibInterface.lgRecordTotal()");
    		CATS_DLL.put("unknown.FileDLTotalCount()", "Biz.Nissan.Cats.CORE.UI.LibInterface.FileDLTotalCount()");
    		CATS_DLL.put("unknown.gDBNext()", "Biz.Nissan.Cats.CORE.UI.LibInterface.gDBNext()");
    		CATS_DLL.put("unknown.GetActionRowTop()", "Biz.Nissan.Cats.CORE.UI.LibInterface.GetActionRowTop()");
    		CATS_DLL.put("unknown.SetColumnHeader()", "Biz.Nissan.Cats.BaseForm.SetColumnHeader()");
    		CATS_DLL.put("unknown.ShowErrorMessage()", "Biz.Nissan.Cats.BaseForm.ShowErrorMessage()");
    		CATS_DLL.put("unknown.CommonPostProcess()", "Biz.Nissan.Cats.BaseForm.CommonPostProcess()");
    		CATS_DLL.put("unknown.ZHiSSGet(System.Integer(),System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZHiSSGet(System.Integer(),unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZHiSSGet(System.Integer,N61T03.N61T03_BAS.cols)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZHiSSGet(System.Integer,N6BAL.N6BAL_BAS.cols)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZHiSSGet(System.Integer,N6BAM.N6BAM_BAS.cols)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZHiSSGet(System.Integer,N6BAP.N6BAP_BAS.cols)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZHiSSGet(System.Integer,System.Integer()())", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZHiSSGet(System.Integer,System.Integer())", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZHiSSGet(System.Integer,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZHiSSGet(System.Integer,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZHiSSGet(UNKOWN,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZHiSSGet(unknown,N6BAM.N6BAM_BAS.cols)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZHiSSGet(unknown,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZHiSSGet(unknown,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZHiSSSet(System.Integer(),System.Integer,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZHiSSSet(System.Integer(),System.Integer,System.Void)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZHiSSSet(System.Integer(),System.Integer,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZHiSSSet(System.Integer,System.Integer(),System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZHiSSSet(System.Integer,System.Integer(),unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZHiSSSet(System.Integer,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZHiSSSet(System.Integer,System.Integer,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZHiSSSet(System.Integer,System.Integer,System.Void)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZHiSSSet(System.Integer,System.Integer,System.Windows.Forms.Format)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZHiSSSet(System.Integer,System.Integer,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZHiSSSet(System.Integer,unknown,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZHiSSSet(System.Integer,unknown,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZHiSSSet(UNKOWN,System.Integer,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZHiSSSet(unknown,System.Integer,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZHiSSSet(unknown,System.Integer,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZHiSSSet(unknown,unknown,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZHiSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZEbkCmp(System.String,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZEbkCmp(System.String,System.String,System.Integer)");
    		CATS_DLL.put("unknown.ZEbkCmp(System.String,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZEbkCmp(System.String,System.String,System.Integer)");
    		CATS_DLL.put("unknown.ZEbkCmp(unknown,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZEbkCmp(System.String,System.String,System.Integer)");
    		CATS_DLL.put("unknown.ZEbkCmp(unknown,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZEbkCmp(System.String,System.String,System.Integer)");
    		CATS_DLL.put("unknown.ZGet(System.Integer(),System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZGet(System.Integer,System.Integer())", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZGet(System.Integer,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZGet(System.Integer,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZGet(unknown,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZLeft(System.String,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZLeft(System.String,System.Long)");
    		CATS_DLL.put("unknown.ZLeft(System.String,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZLeft(System.String,System.Long)");
    		CATS_DLL.put("unknown.ZLeft(unknown,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZLeft(System.String,System.Long)");
    		CATS_DLL.put("unknown.ZLeft(unknown,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZLeft(System.String,System.Long)");
    		CATS_DLL.put("unknown.ZLen(System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZLen(System.String)");
    		CATS_DLL.put("unknown.ZLen(System.Void)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZLen(System.String)");
    		CATS_DLL.put("unknown.ZLen(System.Windows.Forms.Format)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZLen(System.String)");
    		CATS_DLL.put("unknown.ZLen(unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZLen(System.String)");
    		CATS_DLL.put("unknown.ZMid(System.Integer,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZMid(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZMid(System.String,System.Integer(),System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZMid(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZMid(System.String,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZMid(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZMid(System.String,System.Integer,System.Integer())", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZMid(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZMid(System.String,System.Integer,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZMid(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZMid(System.String,System.Integer,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZMid(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZMid(System.String,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZMid(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZMid(System.String,unknown,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZMid(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZMid(System.Void,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZMid(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZMid(System.Void,System.Integer,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZMid(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZMid(System.Windows.Forms.VisualStyles.Standard,System.Integer,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZMid(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZMid(unknown,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZMid(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZMid(unknown,System.Integer,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZMid(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZMid(unknown,unknown,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZMid(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZRight(System.String,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZRight(System.String,System.Long)");
    		CATS_DLL.put("unknown.ZRight(System.String,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZRight(System.String,System.Long)");
    		CATS_DLL.put("unknown.ZRight(System.Void,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZRight(System.String,System.Long)");
    		CATS_DLL.put("unknown.ZRight(unknown,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZRight(System.String,System.Long)");
    		CATS_DLL.put("unknown.ZSSGet(System.Integer(),System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZSSGet(System.Integer,System.Integer())", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZSSGet(System.Integer,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZSSGet(System.Integer,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZSSGet(unknown,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZSSGet(unknown,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZSSGet(System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.ZSSSet(System.Integer,System.Integer(),System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZSSSet(System.Integer,System.Integer,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZSSSet(System.Integer,System.Integer,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZSSSet(unknown,System.Integer,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZSSSet(unknown,System.Integer,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ZSSSet(unknown,unknown,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ZSSSet(System.Integer,System.Integer,System.String)");
    		CATS_DLL.put("unknown.ChangeGetTransCode(System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ChangeGetTransCode(System.String)");
    		CATS_DLL.put("unknown.ChangePutTransCode(System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ChangePutTransCode(System.String)");
    		CATS_DLL.put("unknown.ChangePutTransType(System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ChangePutTransType(System.Integer)");
    		CATS_DLL.put("unknown.ChangePutTransType(unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ChangePutTransType(System.Integer)");
    		CATS_DLL.put("unknown.ChangeSuffixFlagForGetTransCode(System.Boolean)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ChangeSuffixFlagForGetTransCode(System.Boolean)");
    		CATS_DLL.put("unknown.ChangeSuffixFlagForPutTransCode(System.Boolean)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ChangeSuffixFlagForPutTransCode(System.Boolean)");
    		CATS_DLL.put("unknown.CharSpls(System.String,System.Integer,System.String,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.CharSpls(System.String,System.Integer,System.String,System.Integer)");
    		CATS_DLL.put("unknown.CharSpls(System.String,unknown,System.String,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.CharSpls(System.String,System.Integer,System.String,System.Integer)");
    		CATS_DLL.put("unknown.CharSpls(System.Void,System.Integer,System.String,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.CharSpls(System.String,System.Integer,System.String,System.Integer)");
    		CATS_DLL.put("unknown.CharSpls(System.Windows.Forms.Format,System.Integer,System.String,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.CharSpls(System.String,System.Integer,System.String,System.Integer)");
    		CATS_DLL.put("unknown.CharSpls(unknown,System.Integer,System.String,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.CharSpls(System.String,System.Integer,System.String,System.Integer)");
    		CATS_DLL.put("unknown.ChkDate(System.String,System.String,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ChkDate(System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.ChkDate(System.String,System.String,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ChkDate(System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.ChkDate(System.String,unknown,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ChkDate(System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.ChkDate(System.String,unknown,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ChkDate(System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.ChkDate(unknown,System.String,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ChkDate(System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.ChkDate(unknown,System.String,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ChkDate(System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.ChkDate(unknown,unknown,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ChkDate(System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.ChkDate(unknown,unknown,unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ChkDate(System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.StartTextKeyPress(System.Windows.Forms.TextBox,System.Integer,System.String)", "StartTextKeyPress(System.Windows.Forms.TextBox,System.Integer,System.String)");
    		CATS_DLL.put("unknown.StartTextKeyDown(System.Windows.Forms.TextBox,unknown,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.StartTextKeyDown(System.Windows.Forms.TextBox,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.StartTextKeyDown(System.Windows.Forms.TextBox,System.Integer,System.Integer)", "Biz.Nissan.Cats.CORE.UI.LibInterface.StartTextKeyDown(System.Windows.Forms.TextBox,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.StartTextGotFocus(System.Windows.Forms.TextBox)", "Biz.Nissan.Cats.CORE.UI.LibInterface.StartTextGotFocus(System.Windows.Forms.TextBox)");
    		CATS_DLL.put("unknown.SpecialTextKeyPress(System.Integer,System.String)", "Biz.Nissan.Cats.BaseForm.SpecialTextKeyPress(System.Integer,System.String)");
    		CATS_DLL.put("unknown.SpecialTextKeyUp(unknown,System.Windows.Forms.KeyEventArgs)", "Biz.Nissan.Cats.BaseForm.SpecialTextKeyUp(System.Integer,System.Windows.Forms.KeyEventArgs)");
    		CATS_DLL.put("unknown.SpecialTextKeyUp(System.Integer,System.Windows.Forms.KeyEventArgs)", "Biz.Nissan.Cats.BaseForm.SpecialTextKeyUp(System.Integer,System.Windows.Forms.KeyEventArgs)");
    		CATS_DLL.put("unknown.SpecialTextKeyDown(unknown,System.Windows.Forms.KeyEventArgs)", "Biz.Nissan.Cats.BaseForm.SpecialTextKeyDown(System.Integer,System.Windows.Forms.KeyEventArgs)");
    		CATS_DLL.put("unknown.SpecialTextKeyDown(System.Integer,System.Windows.Forms.KeyEventArgs)", "Biz.Nissan.Cats.BaseForm.SpecialTextKeyDown(System.Integer,System.Windows.Forms.KeyEventArgs)");
    		CATS_DLL.put("unknown.SpecialSpreadKeyPress(System.Integer,System.String,unknown)", "Biz.Nissan.Cats.BaseForm.SpecialSpreadKeyPress(System.Integer,System.String,FpSpread)");
    		CATS_DLL.put("unknown.SpecialSpreadKeyDown(unknown,System.Windows.Forms.KeyEventArgs)", "Biz.Nissan.Cats.BaseForm.SpecialSpreadKeyDown(System.Integer,System.Windows.Forms.KeyEventArgs)");
    		CATS_DLL.put("unknown.SpecialFormKeyDown(unknown,System.Windows.Forms.KeyEventArgs)", "Biz.Nissan.Cats.BaseForm.SpecialFormKeyDown(System.Integer,System.Windows.Forms.KeyEventArgs)");
    		CATS_DLL.put("unknown.ChangeSuffixFlagForGetTransCode(System.Boolean)", "Biz.Nissan.Cats.CORE.UI.LibInterface.ChangeSuffixFlagForGetTransCode(System.Boolean)");
    		CATS_DLL.put("unknown.Mnt_Header(unknown,unknown,System.String,System.String,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.Mnt_Header(System.String,System.String,System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.Mnt_Header(unknown,System.String,System.String,System.String,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.Mnt_Header(System.String,System.String,System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.Mnt_Header(System.String,unknown,System.String,System.String,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.Mnt_Header(System.String,System.String,System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.Mnt_Header(System.String,System.Void,System.String,System.String,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.Mnt_Header(System.String,System.String,System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.Mnt_Header(System.String,System.String,unknown,unknown,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.Mnt_Header(System.String,System.String,System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.Mnt_Header(System.String,System.String,unknown,System.String,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.Mnt_Header(System.String,System.String,System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.Mnt_Header(System.String,System.String,System.String,unknown,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.Mnt_Header(System.String,System.String,System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.Mnt_Header(System.String,System.String,System.String,System.String,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.Mnt_Header(System.String,System.String,System.String,System.String,System.String)");
    		CATS_DLL.put("unknown.MidS(unknown,System.Integer,System.Integer)", "Mach_Common.MidS(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.MidS(System.String,System.Integer,unknown)", "Mach_Common.MidS(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.MidS(System.String,System.Integer,System.Integer)", "Mach_Common.MidS(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.MidS(System.String,System.Integer)", "Mach_Common.MidS(System.String,System.Integer,System.Integer)");
    		CATS_DLL.put("unknown.Make_Header(System.String,System.Long)", "Biz.Nissan.Cats.CORE.UI.LibInterface.Make_Header(System.String,System.Long)");
    		CATS_DLL.put("unknown.Make_Header(System.String,System.Integer)", "");
    		CATS_DLL.put("unknown.JumpSuccess(System.String,System.String,System.Integer(),System.Boolean,System.Web.UI.MobileControls.List)", "Biz.Nissan.Cats.BaseForm.JumpSuccess(System.String,System.String,System.Integer(),System.Boolean,System.Web.UI.MobileControls.List)");
    		CATS_DLL.put("unknown.SpecialSpreadKeyPress(System.Integer,System.String,unknown)", "Biz.Nissan.Cats.BaseForm.SpecialSpreadKeyPress(System.Integer,System.String,FpSpread)");
    		CATS_DLL.put("unknown.SpecialTextKeyDown(System.Integer,System.Windows.Forms.KeyEventArgs)", "Biz.Nissan.Cats.BaseForm.SpecialTextKeyDown(System.Integer,System.Windows.Forms.KeyEventArgs)");
    		CATS_DLL.put("unknown.SpecialTextKeyDown(unknown,System.Windows.Forms.KeyEventArgs)", "Biz.Nissan.Cats.BaseForm.SpecialTextKeyDown(System.Integer,System.Windows.Forms.KeyEventArgs)");
    		CATS_DLL.put("unknown.SearchKeyCheck(unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.SearchKeyCheck(System.Windows.Forms.ControlCollection)");
    		CATS_DLL.put("unknown.StartFormActivate(unknown)", "Biz.Nissan.Cats.CORE.UI.LibInterface.StartFormActivate(System.Windows.Forms.Panel,System.Integer)");
    		CATS_DLL.put("unknown.Data_Set2(System.String,System.String)", "Biz.Nissan.Cats.CORE.UI.LibInterface.Data_Set2(System.String,System.String)");
    		CATS_DLL.put("unknown.Kitty_Err(System.Long)", "Biz.Nissan.Cats.CORE.UI.LibInterface.Kitty_Err(System.Long)");

    	}

    	if(CATS_DLL_LOCAL == null){
    		CATS_DLL_LOCAL = new HashMap<String,String>();

    	}

    	if(FILTER_DLL == null){
    		FILTER_DLL = new ArrayList<String>();
    		FILTER_DLL.add("unknown.Action(System.Integer)");
    		FILTER_DLL.add("unknown.AnsSpcCheck(System.String,System.Integer)");
    		FILTER_DLL.add("unknown.AnsSpcCheck(unknown,System.Integer)");
    		FILTER_DLL.add("unknown.YMDCheck(System.Integer,System.Integer)");
    		FILTER_DLL.add("unknown.initUSS5()");
    		FILTER_DLL.add("unknown.initUSS4()");
    		FILTER_DLL.add("unknown.initUSS3()");
    		FILTER_DLL.add("unknown.initUSS2()");
    		FILTER_DLL.add("unknown.initUSS1()");
    		FILTER_DLL.add("unknown.initSpr2()");
    		FILTER_DLL.add("unknown.initSpr1()");
    		FILTER_DLL.add("unknown.initSpr()");
    		FILTER_DLL.add("unknown.initSS2()");
    		FILTER_DLL.add("unknown.initSS1()");
    		FILTER_DLL.add("unknown.initPSS()");
    		FILTER_DLL.add("unknown.initHSS5()");
    		FILTER_DLL.add("unknown.initHSS4()");
    		FILTER_DLL.add("unknown.initHSS3()");
    		FILTER_DLL.add("unknown.initHSS2()");
    		FILTER_DLL.add("unknown.initHSS1()");
    		FILTER_DLL.add("unknown.initHSS()");
    		FILTER_DLL.add("unknown.initDSS()");
    	}

    }

    public void chackDLLS(TDpd tdpd){

    	String local = fileInfo.getSNAME().replace(".vb", "");
    	String localDpd = local+"."+local+".";
    	String targetDpdStr = tdpd.getGID().replace(localDpd, "");

    	if(CATS_DLL_LOCAL.containsKey(targetDpdStr)){
    		debugLog("Target is local >>> " + targetDpdStr);
			tdpd.setType(UNKNOWN_TYPE);
			tdpd.setKeyValidation(0);
    	}


    	if(CATS_DLL.containsKey(tdpd.getGID())){

    		debugLog("Before generate GID >>>>>>>>>>> " + tdpd.getGID());


    		String target = CATS_DLL.get(tdpd.getGID());


    		if(target != null && !target.isEmpty()){
    			debugLog("Target is >>> " + target);
    			tdpd.setGID(target);
            	tdpd.setName(target);
            	tdpd.setKeyValidation(100);
    		}else{
    			debugLog("												"+tdpd.getGID() + " is empty target ");
    		}


        	debugLog("After generate GID >>>>>>>>>>> " + tdpd.getGID());
    	}

    	if(FILTER_DLL.contains(tdpd.getGID())){
    		debugLog("Target is FILTER_DLL >>> " + tdpd.getGID());
			tdpd.setType(UNKNOWN_TYPE);
			tdpd.setKeyValidation(0);
    	}
    }

    public static class PatternUtil{

    	public static String check(String str, String pattern, int returnIndex){
    		String returnValue = "";
    		Pattern p = Pattern.compile(pattern);
    		Matcher m = p.matcher(str);
    		while(m.find()){
    			for(int i = 0 ; i < m.groupCount() ; i++){
    				int cnt = i+1;
    				String param = m.group(cnt);
    				if(returnIndex == cnt){
    					returnValue = param;
    				}
    			}
    		}
    		return returnValue;
    	}

    	public static ArrayList<String> getValues(String str, String pattern){
    		ArrayList<String> results = new ArrayList<String>();
    		Pattern p = Pattern.compile(pattern);
    		Matcher m = p.matcher(str);
    		while(m.find()){
    			for(int i = 0 ; i < m.groupCount() ; i++){
    				String param = m.group(i+1);
    				results.add(param);
    			}
    		}
    		return results;
    	}

    	public static ArrayList<String> getValues(String str, String pattern, String[] filters){
    		ArrayList<String> results = new ArrayList<String>();
    		Pattern p = Pattern.compile(pattern);
    		Matcher m = p.matcher(str);
    		while(m.find()){
    			for(int i = 0 ; i < m.groupCount() ; i++){
    				String param = m.group(i+1);
    				if(isMatched(param, filters)){
    					continue;
    				}
    				results.add(param);
    			}
    		}
    		return results;
    	}

    	public static boolean isMatched(String str, String[] patterns){
    		boolean isMatched = false;
    		for(int i = 0; i < patterns.length; i++){
    			Pattern p = Pattern.compile(patterns[i]);
    			Matcher m = p.matcher(str);
    			if(m.find()){
    				isMatched = true;
    				break;
    			}
    		}
    		return isMatched;
    	}


    }


    public static void main(String[] args) {
		// TODO Auto-generated method stub
    	NS_VB_SEARCH_OBJECT_ID_HANDLER aa = new NS_VB_SEARCH_OBJECT_ID_HANDLER();
		String common1 = "C:\\Users\\ito-motoi\\Desktop\\PJ\\NISSAN\\Source\\PCS_TAI\\PCS_Resource\\リソース\\SCREEN\\UI\\SC\\CATS\\N6A\\MNT2\\N6AA1\\FieldInfo.vb";
		String common2 = "C:\\Users\\ito-motoi\\Desktop\\PJ\\NISSAN\\Source\\PCS_TAI\\PCS_Resource\\リソース\\SCREEN\\UI\\SC\\MACH\\N6B\\MNT\\N6BB7\\N6BB7_BAS.vb";
		String common3 = "C:\\Users\\ito-motoi\\Desktop\\PJ\\NISSAN\\Source\\PCS_TAI\\PCS_Resource\\リソース\\SCREEN\\UI\\SC\\MACH\\N6B\\MNT\\N6BBA\\N6BBA_BAS.vb";
		String common4 = "C:\\Users\\ito-motoi\\Desktop\\PJ\\NISSAN\\Source\\PCS_TAI\\PCS_Resource\\リソース\\SCREEN\\UI\\SC\\CATS\\E62\\MNT2\\E62D1\\E62D1_CLS.vb";
		String common5 = "C:\\Users\\ito-motoi\\Desktop\\PJ\\NISSAN\\Source\\PCS_TAI\\PCS_Resource\\リソース\\SCREEN\\UI\\SC\\CATS\\N61\\MNT2\\N61D4\\N61D4_UserResearchUAction.vb";
		try {


			aa.addCheck(null, null, null, common1);
			//aa.addCheck(null, null, null, common2);
			//aa.addCheck(null, null, null, common3);
			//aa.addCheck(null, null, null, common4);
			//aa.addCheck(null, null, null, common5);


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



}
