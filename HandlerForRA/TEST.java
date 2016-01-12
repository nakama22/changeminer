
package changeminer.HandlerForRA;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itplus.cm.parser.common.CMParserCommonData;
import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.data.DataForAnalyzer;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.Environment;

import extractor.common.tobj.TDpd;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;


public class TEST extends HandlerForRA
{


	public static HashMap<String, String> valueMap = null;
	/*
	 * シェル全体で使用される変数の値が格納されているファイルパス
	 */
	public static String envFilePath = "C:\\Users\\ito-motoi\\Desktop\\NISSAN_HANDLER\\MCATS_run.env";
	public static String exportRex = "export\\s+([^\\s]+)=([^\\s]+)";
	public static String subValueRex = "\\$\\{([^\\{\\s\\}]+)\\}";
	/*
	 * Inclde Shell
	 * rex : [.][space][shell_name][.][sh]
	 * sample : <. FUNCLIB.sh>
	 * 抽出 : shell_name.sh
	 * シェルから他のシェルをImportするパタン
	 */
	private static String includeRex = "\\.\\s(.+\\.sh)";
	private static String includeEnv = "\\.\\s(.*/)([^\\s]+\\.env)";
	/*
	 * Oracle Export Dump File
	 * rex : [space+][exp][space+][$ORAUSER][space+]
	 * sample : exp $ORAUSER \
     *   			tables=<Table_Name>            \
     *   			file=${DD_EXPFL}      1>>$tempout  2>>$temperr
     * 抽出 : Table_Name
     * expコマンドで対象テーブルのDUMPファイルを作成するパタン
	 */
	private static String expRex = "\\s*exp\\s+\\$ORAUSER\\s+";
	private static String expTableRex = "\\s+tables=(.+)\\s+";
	/*
	 * Run Cobol
	 * rex : [space*][RunCobol][space+][Value1][space+][Value2][space+][Cobol_Name][space+][Value4][space+][Value5][space+][Value6][space+]
	 * sample : RunCobol value1 value2 <COBOL_NAME> value4 1>>value5  2>>value6
	 * 抽出 : COBOL_NAME
	 * RunCobol関数から実行されるCOBOLのパタン
	 */
	private static String runCobolRex = "[\\s]*RunCobol\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s*";
	/*
	 * Run Shell
	 * rex : [Shell_Name][.][sh]
	 * sample : <xxxxxxxxxx.sh>
	 * 抽出 : Shell_Name
	 * シェルから他のシェルを実行するパタン
	 * シェル名にはパスやスペースを除くもの
	 */
	private static String runShellRex = "^[^¥¥.]\\s+([^¥¥s/]+\\.sh)$";
	/*
	 * Export File
	 * rex : [tar][space+][-][option][space+][ExportFile][space+][Value2][space+][Value3][space+][Value4][space*]
	 *       [gzip][space+][Value1] // [space+][Value2][space+][Value3][space*]
	 * sample : tar -Option <ExportFile>  Value2  1>>Value3 2>>\Value4
	 *          gzip <ExportFile>  1>>Value2  2>>Value3
	 * 抽出 : ExportFile
	 * シェルから外部にOUTPUTするパタン
	 */
	private static String tarRex = "tar\\s+(-[^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s*";
	private static String gzipRex = "gzip\\s+([^\\s]+)\\s*";//"gzip\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s*";
	/*
	 * Sort Shell
	 * rex : [space*][RunSort][space+][param1][space+][pstsm3 name][space+][param3]
	 * sample : RunSort <${DD_SORTPARM}> 1>>${tempout}  2>>${temperr}
	 * 抽出 : $DD_SORTPARAM = コード上Shell名がある
	 * ソート処理に作用されるシェル呼び出すパタン
	 */
	private static String runSortRex = "\\s*RunSort\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)";
	/*
	 * Csv
	 * rex : [space*][ojob_cp][space+]["][CSV]["][space*]
	 * sample : ojob_cp "${MDCDOWNLOAD}/<NO4_Firm_Oeder.csv>"
	 * 抽出 : NO4_Firm_Oeder.csv
	 * ojob_cp関数でのCSVファイル
	 */
	private static String csvRex = "\\s*ojob_cp\\s+\\\"([^\\s]+\\.csv)\\\"\\s*";



	public static void main(String[] args){


		String Path = "C:\\Users\\ito-motoi\\Desktop\\NISSAN_HANDLER\\N4b\\DN4B11JM.sh";
		//String Path = "C:\\Users\\ito-motoi\\Desktop\\NISSAN_HANDLER\\N4B\\DN4B11LO.sh";
		//String Path = "C:\\Users\\ito-motoi\\Desktop\\NISSAN_HANDLER\\N4B\\DN4B11SO.sh";
		//envファイル対応
		initEnvFile();

		if(TEST.valueMap == null){
			System.out.println("Init Failed Env File");
			return;
		}


		FileInputStream fis = null;
		BufferedReader br = null;

		int line = 0;
		boolean isExpCommand = false;

		try {
			fis = new FileInputStream(Path);
			br = new BufferedReader(new InputStreamReader(fis));
			String line_data = "";
			HashMap<Integer, String> lineMap = new HashMap<Integer, String>();
			while (br.ready()) {

				line++;

				line_data = br.readLine();

				if(line_data.startsWith("#")){
					continue;
				}

				lineMap.put(line, line_data);

				checkIncludeSH(line_data);
				checkIncludeEnv(line_data);

				if(!isExpCommand){
					isExpCommand = checkExp(line_data);
				}else{
					isExpCommand = checkExpTable(line_data);

					if(isExpCommand){
						isExpCommand = false;
					}
				}

				checkRunCobol(line_data);
				checkRunShell(line_data, lineMap, line);
				checkTar(line_data, lineMap, line);
				checkGzip(line_data, lineMap, line);
				checkSortShell(line_data, lineMap, line);
				checkCsv(line_data, lineMap, line);
				//System.out.println("LINE " + line + " >> " + line_data);


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

	public static void checkValue(String value) throws Exception{
		if(value == null){
			throw new Exception("Value is Null");
		}
	}

	public static void checkIncludeSH(String lineStr) throws Exception{



		Pattern p = Pattern.compile(includeRex);
		Matcher m = p.matcher(lineStr);

		while(m.find()){
			//String targetStr = m.group();
			//targetStr = targetStr.substring(targetStr.indexOf(" "));
			//System.out.println("checkIncludeSH : "+ targetStr);
			String value = m.group(1);
			checkValue(value);
			System.out.println("checkIncludeSH : "+ value);


		}
	}

	public static void checkIncludeEnv(String lineStr) throws Exception{
		Pattern p = Pattern.compile(includeEnv);
		Matcher m = p.matcher(lineStr);

		while(m.find()){
			//String targetStr = m.group();
			//targetStr = targetStr.substring(targetStr.indexOf(" "));
			//System.out.println("checkIncludeSH : "+ targetStr);
			String value = m.group(1);
			String value2 = m.group(2);
			checkValue(value);
			checkValue(value2);
			System.out.println("checkIncludeEnv1 : "+ value);
			System.out.println("checkIncludeEnv2 : "+ value2);



		}
	}

	public static boolean checkExp(String lineStr) {



		Pattern p = Pattern.compile(expRex);
		Matcher m = p.matcher(lineStr);

		if(m.find()){
			return true;
		}
			/*
		while(m.find()){
			String targetStr = m.group();

			System.out.println("checkExp : "+ targetStr);
		}
		*/

		return false;
	}

	public static boolean checkExpTable(String lineStr) throws Exception{

		boolean isFind = false;

		Pattern p = Pattern.compile(expTableRex);
		Matcher m = p.matcher(lineStr);

		if(m.find()){
			String value = m.group(1);
			checkValue(value);
			value = value.trim();

			System.out.println("checkExpTable : "+ value);
			isFind = true;
		}
			/*
		while(m.find()){
			String targetStr = m.group();

			System.out.println("checkExp : "+ targetStr);
		}
		*/
		return isFind;

	}

	public static void checkRunCobol(String lineStr) throws Exception{
		
		/* echo記述での対応 */
		if(!lineStr.trim().startsWith("RunCobol")){
			return;
		}

		Pattern p = Pattern.compile(runCobolRex);
		Matcher m = p.matcher(lineStr);

		while(m.find()){

			//System.out.println("checkRunCobol : "+ m.group()); match str
			//System.out.println("checkRunCobol : "+ m.group(1)); jclname
			//System.out.println("checkRunCobol : "+ m.group(2)); ??
			//System.out.println("checkRunCobol : "+ m.group(3)); cobol
			//System.out.println("checkRunCobol : "+ m.group(4)); null
			//System.out.println("checkRunCobol : "+ m.group(5)); tempout
			//System.out.println("checkRunCobol : "+ m.group(6)); temperr
			String value = m.group(3);
			checkValue(value);
			value = value.replaceAll("\"", "");

			//System.out.println("checkRunCobol : "+ m.group(3).replaceAll("\"", ""));
			System.out.println("checkRunCobol : "+ value);

		}
	}

	public static void checkRunShell(String lineStr, HashMap<Integer, String> lineMap, int currentLine) throws Exception{


		Pattern p = Pattern.compile(runShellRex);
		Matcher m = p.matcher(lineStr);


		if(m.find()){
			//System.out.println("checkRunShell : "+ lineStr);
			String value = m.group(1);
			//System.out.println("checkRunShell : "+ value);
			checkValue(value);
			value = value.trim();

			if(isParamValue(value)){

				/* DN4BCARMAIND047 case */
				value = getParamValue(value);
				System.out.println("checkRunShell : "+ value);


				/* envから値を検索するかどうか */
				String paramRex = value + "=([^\\s]+)";

				Pattern p2 = Pattern.compile(paramRex);
				Matcher m2 = null;
				for(int i = currentLine+1 ; i != 1 ; i--){
					String lineValue = lineMap.get(i);
					if(lineValue == null) { continue;}
					m2 =  p2.matcher(lineValue);

					if(m2.find()){

						String shellName = m2.group(1);
						checkValue(shellName);
						shellName = shellName.replace(";", "");
						value = shellName+".sh";
						break;
					}
				}
			}

			System.out.println("checkRunShell : "+ value);
		}
	}

	public static void checkTar(String lineStr, HashMap<Integer, String> lineMap, int currentLine) throws Exception{


		Pattern p = Pattern.compile(tarRex);
		Matcher m = p.matcher(lineStr);


		if(m.find()){
			//System.out.println("checkTar : "+ lineStr);
			//System.out.println("checkTar : "+ m.group(1));
			String value = m.group(2);
			checkValue(value);

			/*
			String reValueMap = checkValueFromMap(value);

			if(reValueMap == null){

				value = value.replace("$", "").replace("{", "").replace("}", "");
				String tarFileRex = value+"=([^\\s]+\\.tar)\\s*";

				Pattern tarP = Pattern.compile(tarFileRex);

				for(int i = currentLine+1 ; i != 1 ; i--){
					String lineValue = lineMap.get(i);
					if(lineValue == null) { continue;}
					Matcher tarM = tarP.matcher(lineValue);

					if(tarM.find()){
						System.out.println("Line : " + tarM.group(1) );
						value = convertValueFromMap(tarM.group(1));
						break;
					}
				}

			}else{
				value = reValueMap;
			}
			*/

			System.out.println("checkTar : "+ value);

		}
	}

	/*
	 * ValueMapにデータがあるかを確認
	 */
	public static String checkValueFromMap(String reValue){

		String newValue = null;

		Pattern p = Pattern.compile(TEST.subValueRex);
		Matcher m = p.matcher(reValue);


		if(m.find()){
			reValue = reValue.replace("$", "").replace("{", "").replace("}", "");
			String mapValue = TEST.valueMap.get(reValue);
			if(mapValue != null){
				newValue = mapValue;
			}
		}

		return newValue;
	}

	public static void checkGzip(String lineStr, HashMap<Integer, String> lineMap, int currentLine) throws Exception{


		Pattern p = Pattern.compile(gzipRex);
		Matcher m = p.matcher(lineStr);


		while(m.find()){
			//System.out.println("checkGzip : "+ lineStr);
			String value = m.group(1);
			checkValue(value);

			/*
			String reValueMap = checkValueFromMap(value);

			if(reValueMap == null){

				value = value.replace("$", "").replace("{", "").replace("}", "");
				String gzipFileRex = value+"=([^\\s]+\\.tar)\\s*";

				Pattern tarP = Pattern.compile(gzipFileRex);

				for(int i = currentLine+1 ; i != 1 ; i--){
					String lineValue = lineMap.get(i);
					if(lineValue == null) { continue;}
					Matcher tarM = tarP.matcher(lineValue);

					if(tarM.find()){
						System.out.println("Line : " + tarM.group(1) );
						value = convertValueFromMap(tarM.group(1));
						break;
					}
				}

			}else{
				value = reValueMap;
			}
			*/
			System.out.println("checkGzip : "+value);
			//System.out.println("checkGzip : "+ m.group(2));
			//System.out.println("checkGzip : "+ m.group(3));

		}
	}

	public static void checkSortShell(String lineStr, HashMap<Integer, String> lineMap, int currentLine) throws Exception{

		Pattern p = Pattern.compile(runSortRex);
		Matcher m = p.matcher(lineStr);

		if(m.find()){

			String value = m.group(1);
			checkValue(value);

			if(isParamValue(value)){
				value = getParamValue(value);
				String paramRex = value+"=([^\\s]*)/([^\\s]+)";

				Pattern p2 = Pattern.compile(paramRex);
				Matcher m2 = null;
				for(int i = currentLine+1 ; i != 1 ; i--){
					String lineValue = lineMap.get(i);
					if(lineValue == null) { continue;}
					m2 =  p2.matcher(lineValue);

					if(m2.find()){

						String shellName = m2.group(2);
						checkValue(shellName);
						shellName = shellName.replace(";", "");
						value = shellName+".sh";
						break;
					}else{

					}


				}



			}else{

			}


			System.out.println("checkSortShell : "+value);


		}
	}

	public static void checkCsv(String lineStr, HashMap<Integer, String> lineMap, int currentLine) throws Exception{


		Pattern p = Pattern.compile(csvRex);
		Matcher m = p.matcher(lineStr);


		while(m.find()){
			//System.out.println("checkGzip : "+ lineStr);
			String value = m.group(1);
			checkValue(value);

			String paramRex = "([^\\s]*)/([^\\s]+\\.csv)";

			Pattern p2 = Pattern.compile(paramRex);
			Matcher m2 = p2.matcher(value);
			if(m2.find()){
				value = m2.group(2);
			}


			/*
			String reValueMap = checkValueFromMap(value);

			if(reValueMap == null){

				value = value.replace("$", "").replace("{", "").replace("}", "");
				String gzipFileRex = value+"=([^\\s]+\\.tar)\\s*";

				Pattern tarP = Pattern.compile(gzipFileRex);

				for(int i = currentLine+1 ; i != 1 ; i--){
					String lineValue = lineMap.get(i);
					if(lineValue == null) { continue;}
					Matcher tarM = tarP.matcher(lineValue);

					if(tarM.find()){
						System.out.println("Line : " + tarM.group(1) );
						value = convertValueFromMap(tarM.group(1));
						break;
					}
				}

			}else{
				value = reValueMap;
			}
			*/
			System.out.println("checkCsv : "+value);
			//System.out.println("checkGzip : "+ m.group(2));
			//System.out.println("checkGzip : "+ m.group(3));

		}
	}

	public static String getParamValue(String valueStr){

		String isParamValue = valueStr;

		Pattern p = Pattern.compile(subValueRex);
		Matcher m = p.matcher(valueStr);

		if(m.find()){
			isParamValue = m.group(1);
		}


		return isParamValue;
	}

	public static boolean isParamValue(String valueStr){

		boolean isParamValue = false;

		Pattern p = Pattern.compile(subValueRex);
		Matcher m = p.matcher(valueStr);


		isParamValue = m.find();

		return isParamValue;
	}

	public static void initEnvFile(){

		if(TEST.valueMap != null){
			return;
		}


		Pattern p = Pattern.compile(exportRex);
		Pattern subP = Pattern.compile(subValueRex);

		FileInputStream fis = null;
		BufferedReader br = null;

		try {
			fis = new FileInputStream(TEST.envFilePath);
			br = new BufferedReader(new InputStreamReader(fis));
			String line_data = "";


			HashMap<String, String> valueMap = new HashMap<String, String>();
			while (br.ready()) {


				line_data = br.readLine();

				if(line_data.startsWith("#")){
					continue;
				}


				Matcher m = p.matcher(line_data);


				if(m.find()){

					String key = m.group(1);

					String value = m.group(2);

					//System.out.println(key+" | " + value);


					Matcher subM = subP.matcher(value);



					while(subM.find()){

						String targetKey = subM.group(1);
						//System.out.println("			Find  " + targetKey);
						String targetValue = "";

						try{
							targetValue = valueMap.get(targetKey);
							if(targetValue == null){
								targetValue = "";
							}
							//System.out.println("			Get   " + targetValue);


							String replaceStr = "${" + targetKey + "}";
							value = value.replace(replaceStr, targetValue);
							//System.out.println("			Value  " + value);
						}catch(Exception e){
							System.out.println(e.getMessage());
						}
					}


					if(!valueMap.containsKey(key)){
						//System.out.println("Put Key Value == " + key + " | " + value);
						valueMap.put(key, value);
					}else{
						String reValue = valueMap.get(key);
						reValue += value;
						valueMap.replace(key, reValue);
					}

				}

			}

			/*
			for(Map.Entry<String, String> entry : valueMap.entrySet()){
				System.out.println("Key == " + entry.getKey() + "  Value = " + entry.getValue());
			}
			*/
			TEST.valueMap = valueMap;


		} catch(Exception e) {
			e.printStackTrace();
			TEST.valueMap = null;
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


	public static String convertValueFromMap(String valueStr) throws Exception{

		Pattern subP = Pattern.compile(subValueRex);
		Matcher subM = subP.matcher(valueStr);

		while(subM.find()){

			String targetKey = subM.group(1);
			//System.out.println("			Find  " + targetKey);
			String targetValue = "";


			targetValue = valueMap.get(targetKey);
			if(targetValue == null){
				targetValue = "${" + targetKey + "}";
			}
			//System.out.println("			Get   " + targetValue);


			String replaceStr = "${" + targetKey + "}";
			valueStr = valueStr.replace(replaceStr, targetValue);
			//System.out.println("			Value  " + value);


		}
		return valueStr;
	}


    public TEST() {

    }


    public String getName() {
        return this.getClass().getName();
    }


    public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
        return RETURN_CONTINUE;
    }


    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {

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
        // TODO Auto-generated method stub
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
}
