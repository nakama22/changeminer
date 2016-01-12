package changeminer.HandlerForRA;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itplus.cm.parser.common.CMParserCommonData;
import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;

import extractor.common.tobj.TDpd;
import extractor.common.tobj.TLocation;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;

public class NS_MQ_HANDLER extends HandlerForRA
{
	public final int MQINFO = 0;
	public final int APPINFO = 1;
	public final int CALLINOUT = 2;
	public int currentType = -1;
	public String currentCode = "";
	public String inoutFlag = "";


	public boolean DebugMode = true;

    public NS_MQ_HANDLER() {

    }

    public String getName() {
        return this.getClass().getName();
    }

    public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
        return RETURN_CONTINUE;
    }

    public int addCheck(TObj root_obj, String targetFile) throws Exception {

    	checkCurrentType(targetFile);

    	if(currentType == MQINFO){
    		//analyzeMQINFO(root_obj, targetFile);
    	}else if(currentType == APPINFO){
    		analyzeAPPINFO(root_obj, targetFile);
    	}else if(currentType == CALLINOUT){
    		
    	}

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

    public void analyzeMQINFO(TObj root_obj, String filePath){


    	FileInputStream fis = null;
		BufferedReader br = null;

		int line = 0;

		try {
			fis = new FileInputStream(filePath);
			br = new BufferedReader(new InputStreamReader(fis));
			String line_data = "";

			while (br.ready()) {

				line++;

				line_data = br.readLine();

				if(line_data.trim().startsWith("#") || line == 1){
					continue;
				}
				debugLog("-----------------------------------------------------------------------------------------------------------------------------");
				debugLog(line_data);
				ArrayList<String> chunks = PatternUtil.getValues(line_data, "([^,]+)");
				if(chunks.size() == 8){
					String plant_code = chunks.get(0);
					String rcvr_id = chunks.get(1);
					String data_class = chunks.get(2);
					String appl_id = chunks.get(7);
					String convAlant_code = plant_code.replace("\"", "").trim();//.replace("\"", "").replace(" ", "_");
					String convRcvr_id = rcvr_id.replace("\"", "").trim();//replace("\"", "").replace(" ", "_");
					String convData_class = data_class.replace("\"", "").replace(" ", "_");
					String convAppl_id = appl_id.replace("\"", "").trim();//replace("\"", "").replace(" ", "_");

					debugLog(String.format("	plant_code(%s), rcver_id(%s), data_class(%s), appl_id(%s)", convAlant_code, convRcvr_id, convData_class, convAppl_id));
				}else{
					debugLog("chunks size is not 8");
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
    }
    
    public void analyzeAPPINFO(TObj root_obj, String filePath){


    	FileInputStream fis = null;
		BufferedReader br = null;

		int line = 0;

		try {
			fis = new FileInputStream(filePath);
			br = new BufferedReader(new InputStreamReader(fis));
			String line_data = "";

			while (br.ready()) {

				line++;

				line_data = br.readLine();

				if(line_data.trim().startsWith("#") || line == 1){
					continue;
				}
				debugLog("-----------------------------------------------------------------------------------------------------------------------------");
				debugLog(line_data);
				ArrayList<String> chunks = PatternUtil.getValues(line_data, "([^,]+)");
				debugLog("size >> " + chunks.size());
				debugLog(chunks);

				if(chunks.size() != 0){
					String appId = chunks.get(0);
					String runShell = chunks.get(1);
					debugLog(String.format("	appId(%s), runShell(%s)", appId, runShell));
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


    }

    public void checkCurrentType(String filePath){

    	String fileName = "";

    	fileName = filePath.substring(filePath.lastIndexOf('/')+1);

    	if(!fileName.isEmpty()){
    		if(PatternUtil.isMatched(fileName, new String[]{"[A-Za-z0-9]_MQINFO.csv"})){
    			currentType = 0;
    			currentCode = PatternUtil.check(fileName, "([A-Za-z0-9]+)_MQINFO.csv", 1);
    			debugLog(String.format("MQInfo matched >> %s , %d, %s", fileName, currentType, currentCode));
    		}else if(PatternUtil.isMatched(fileName, new String[]{"[A-Za-z0-9]_APPINFO.csv"})){
    			currentType = 1;
    			currentCode = PatternUtil.check(fileName, "([A-Za-z0-9]+)_APPINFO.csv", 1);
    			debugLog(String.format("APPINFO matched >> %s , %d, %s", fileName, currentType, currentCode));
    		}else if(PatternUtil.isMatched(fileName, new String[]{"[A-Za-z0-9]_CALL(IN|OUT).txt"})){
    			currentType = 2;
    			currentCode = PatternUtil.check(fileName, "([A-Za-z0-9]+)_CALL(IN|OUT).txt", 1);
    			inoutFlag = PatternUtil.check(fileName, "([A-Za-z0-9]+)_CALL(IN|OUT).txt", 2);
    			debugLog(String.format("CALL matched >> %s , %d, %s, %s", fileName, currentType, currentCode, inoutFlag));
    		}
    	}

    }

    public void debugLog(String str){
    	if(DebugMode){
    		System.out.println(str);
    	}
    }

    public void debugLog(ArrayList<String> list){
    	if(DebugMode){
    		for(int i = 0; i < list.size(); i++){
    			System.out.println(list.get(i));
    		}

    	}
    }

    public static void main(String[] args) {
    	NS_MQ_HANDLER mq = new NS_MQ_HANDLER();
		TObj obj_root = new TObj(1, "", "", 100, new TLocation() );
		String target = "C:\\Users\\ito-motoi\\Desktop\\MQ\\6_APPINFO.csv";
		String target2 = "C:\\Users\\ito-motoi\\Desktop\\MQ\\6_MQINFO.csv";
		String target3 = "C:\\Users\\ito-motoi\\Desktop\\MQ\\6_CALLIN.txt";
		String target4 = "C:\\Users\\ito-motoi\\Desktop\\MQ\\6_CALLOUT.txt";
		try {

			mq.addCheck(obj_root, target);
			mq.addCheck(obj_root, target2);
			mq.addCheck(obj_root, target3);
			mq.addCheck(obj_root, target4);

		} catch (Exception e) {
			e.printStackTrace();
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
}
