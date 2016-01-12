package changeminer.HandlerForRA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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

public class SHELL_ANALYZER_HANDLER extends HandlerForRA
{

	public boolean showLog = true; //Log flag
	public boolean isStartMainProcess = false;
	public boolean isStartProcess = false;
	public boolean isFunctionStart = false;
	/** SHELL-DPD_TYPE **/
	final static int INCLUDE_SHELL = 8211002; //Include Shell File
	final static int INCLUDE_ENV  = 8211003; //Include Env File
	final static int OUTPUT_DUMP = 8211004; //Oracle Table Dump File
	final static int RUN_COBOL = 8211005; //Run Cobol File
	final static int RUN_SHELL = 8211006; //Run Shell File
	final static int OUTPUT_TAR = 8211007; //tar or gzip Output File
	final static int SORT_SHELL = 8211008; //Sort Shell File
	final static int OUTPUT_CSV = 8211009; //csv Output File
	final static int GENERAL_FILE = 820001; //General File

	//シェルの格納場所 GID生成時にFILEとの関連を作成するため
	//public String SHELL_DIR_PATH = null;

    public SHELL_ANALYZER_HANDLER() {

    }

    public static void main(String[] args){
    	SHELL_ANALYZER_HANDLER sa = new SHELL_ANALYZER_HANDLER();
    	try {
			sa.addAnalyzeStep(null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {


    	//String shellPath = Environment.getSourceDir() + cm_src.getCOLLECT_ID() + cm_src.getSPATH();
    	//String shellFile = shellPath + cm_src.getSNAME();

    	//String shellFile = "C:\\Users\\ito-motoi\\Desktop\\NISSAN_HANDLER\\N4b\\DN4B11JM.sh";
    	//tring shellFile = "C:\\Users\\ito-motoi\\Desktop\\NISSAN_HANDLER\\init.sh";
    	String shellFile = "C:\\Users\\ito-motoi\\Desktop\\NISSAN_HANDLER\\FUNCLIB.sh";

    	/*
    	if(SHELL_DIR_PATH == null){
    		SHELL_DIR_PATH = shellPath;
    	}
		*/

    	TObj obj_root = null;//tresult.getTObjList()[0];

    	//wol("****** Start SHELL_ANALYZER_HANDLER Target >> "+ cm_src.getSNAME()  + "******");
    	wol("****** Start SHELL_ANALYZER_HANDLER Target >> "+ shellFile  + "******");

    	FileInputStream fis = null;
		BufferedReader br = null;

		int line = 0;
		boolean isExpCommand = false;


		try {
			fis = new FileInputStream(shellFile);
			br = new BufferedReader(new InputStreamReader(fis));
			String line_data = "";
			HashMap<Integer, String> lineMap = new HashMap<Integer, String>();
			while (br.ready()) {

				line++;

				line_data = br.readLine();

				if(line_data.trim().startsWith("#")){
					continue;
				}

				lineMap.put(line, line_data);

				if(checkSTRT_MSGLG(line_data, strt_MSGLG)){
					isStartMainProcess = true;
					wol("checkSTRT_MSGLG strt_MSGLG  = " + isStartMainProcess + " LINE >> " + line);
				}else{
					//wol("continue  = " + isStartMSGLG);
				}

				if(checkSTRT_LOG(line_data, strt_LOG)){
					isStartProcess = true;
					wol("	checkSTRT_LOG strt_LOG  = " + isStartProcess + " LINE >> " + line);
				}

				if(isFunctionStart){
					
				}else{
					
				}
				
				
				if(checkFunction(line_data)){
					wol("checkFunction >> " + line_data + " >> LINE " + line);
				}

				checkIncludeSH(line_data, obj_root, line);
				checkIncludeEnv(line_data, obj_root, line);

				if(!isExpCommand){
					isExpCommand = checkExp(line_data);
				}else{
					//isExpCommand = checkExpTable(line_data, obj_root, line);
					boolean isFind = false;

					isFind = checkExpTable(line_data, obj_root, line);

					if(!isFind){
						isExpCommand = checkExpFile(line_data, obj_root, lineMap, line);
						if(isExpCommand){
							isExpCommand = false;
						}
					}
				}

				checkRunCobol(line_data, obj_root, line);
				checkRunShell(line_data, obj_root, lineMap, line);
				checkTar(line_data, obj_root, lineMap, line);
				checkGzip(line_data, obj_root, lineMap, line);
				checkSortShell(line_data, obj_root, lineMap, line);
				checkCsv(line_data, obj_root, lineMap, line);
				checkCp(line_data, obj_root, line);
				checkCopyFile(line_data, obj_root, line);
				checkRm(line_data, obj_root, line);
				//System.out.println("LINE " + line + " >> " + line_data);

				if(checkSTRT_LOG(line_data, end_LOG)){
					isStartMainProcess = false;
					wol("	checkSTRT_LOG end_LOG = " + isStartMainProcess+ " LINE >> " + line);
				}else{
					//wol("continue  = " + isStartMSGLG);
				}

				if(checkSTRT_MSGLG(line_data, end_MSGLG)){
					isStartProcess = false;
					wol("checkSTRT_MSGLG end_MSGLG = " + isStartProcess + " LINE >> " + line);
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

    public TResult getTTresult() {

        return null;
    }

    public long generateGID(String prefix, TObj tobj) {
    	long gid = 0L;

    	try{
	    	if(tobj.getType() == GENERAL_FILE){
	    			String name = new File(tobj.getGID()).getName(); //GeneralFileオブジェクトをFile名だけにしてGID生成
	        		gid = FileUtil.getGID("<FILE>", name);
	    	}

    	}catch(Exception e){
			gid = 0L;
			e.printStackTrace();
		}
        return gid;
    }

    public long generateGID(String prefix, TDpd tdpd) {
    	/*
    	final static int INCLUDE_SHELL = 8211002; //Include Shell File
		final static int INCLUDE_ENV  = 8211003; //Include Env File
		final static int OUTPUT_DUMP = 8211004; //Oracle Table Dump File
		final static int RUN_COBOL = 8211005; //Run Cobol File
		final static int RUN_SHELL = 8211006; //Run Shell File
		final static int OUTPUT_TAR = 8211007; //tar or gzip Output File
		final static int SORT_SHELL = 8211008; //Sort Shell File
		final static int OUTPUT_CSV = 8211009; //csv Output File
    	*/
    	long newGid = 0L;

    	switch(tdpd.getType()){
	    	case INCLUDE_SHELL :
	    	    newGid = FileUtil.getGID("<FILE>", tdpd.getGID());
	    		break;
	    	case INCLUDE_ENV :
	    		newGid = FileUtil.getGID("<FILE>", tdpd.getGID());
	    		break;
	    	case OUTPUT_DUMP :
	    		break;
	    	case RUN_COBOL :
	    		newGid = FileUtil.getGID("<COBOL>", tdpd.getGID());
	    		break;
	    	case RUN_SHELL :
	    		newGid = FileUtil.getGID("<FILE>", tdpd.getGID());
	    		break;
	    	case OUTPUT_TAR :
	    		break;
	    	case SORT_SHELL :
	    		newGid = FileUtil.getGID("<FILE>", tdpd.getGID());
	    		break;
	    	case OUTPUT_CSV :
	    		break;
	    	default :
    	}


        return newGid;
    }


    public String getObjName(boolean is_file, CM_SRC cm_src, String full_object_name)
    {
    	return null;
    }


    public void addAnalyzeStepOnError(CM_SRC cm_src, TResult tresult) {
    }


    /*************************** Handler-Rex Expression ********************************/
    /*
     * Check Value Type
     * rex : [$][{][value][}]
     * シェル変数かどうか
     */
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
	private static String expFileRex = "\\s+file=(.+)\\s+";
	/*
	 * Run Cobol
	 * rex : [space*][RunCobol][space+][Value1][space+][Value2][space+][Cobol_Name][space+][Value4][space+][Value5][space+][Value6][space+]
	 * sample : RunCobol value1 value2 <COBOL_NAME> value4 1>>value5  2>>value6
	 * 抽出 : COBOL_NAME
	 * RunCobol関数から実行されるCOBOLのパタン
	 */
	private static String runCobolRex = "\\s*RunCobol\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s*";
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
	/*
	 * START_MSGLG, END_MSGLG
	 * rex : [STRT_MSGLG] [END_MSGLG]
	 * sample : <STRT_MSGLG> ${JCLNAME} ${NOTIFY} ${PID}
	 *        : <END_MSGLG> ${JCLNAME} ${NOTIFY} ${PID}
	 * STRT_MSGLG関数、END_MSGLG関数確認
	 */
	private static String strt_MSGLG = "\\s*STRT_MSGLG\\s+";
	private static String end_MSGLG = "\\s*END_MSGLG\\s+";
	/*
	 * STRTLOG、ENDLOG
	 * rex : [STRTLOG] [ENDLOG]
	 * sample : <STRTLOG> $NOTIFY ${JCLNAME} "S040"
	 *        : <ENDLOG> ${NOTIFY} ${JCLNAME} "S040" ${COND_CODE} "UTILS"
	 * STRTLOG関数、ENDLOG関数確認
	 */
	private static String strt_LOG = "\\s*STRTLOG\\s+";
	private static String end_LOG = "\\s*ENDLOG\\s+";
	/*
	 * cp file1 file2
	 * rex : [cp][space+][file1][space+][file2][space+]
	 * sample : cp <${DATA}/${CMNPLANTCD}N4B931S>  <${DATA}/${MODULENAME}.${JCLNAME}.BACKUP.${CMNPLANTCD}N4B931S>  1>>$tempout  2>>$temperr
	 * cp file1 file2
	 */
	private static String cpRex = "\\s*cp\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s*";
	/*
	 * COPYFILE
	 * rex : [COPYFILE][space+][param1][space+][param2][space+][param3][src][space+][target][space*]]
	 * sample : COPYFILE ${NOTIFY} ${JCLNAME} S00B <${DATA}/${CMNPLANTCD}N4B931S> <${DATA}/${MODULENAME}.${JCLNAME}.BACKUP.${CMNPLANTCD}N4B931S>
	 * COPYFILE関数のSRCとTargetを抽出
	 */
	private static String copyFileRex = "\\s*COPYFILE\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s*";
	/*
	 * rm
	 * rex : [rm][space+][target][space*]
	 * sample : rm ${TEMP}/${PID}.*
	 * rm target 抽出
	 */
	private static String rmRex = "\\s*rm\\s+([^\\s]+)\\s*";
	/*
	 * function
	 * rex : [space*][function名][(][)][space*][{*]
	 * sample : <SaveHigherSeverityStatus>(){ or <SaveHigherSeverityStatus>()
	 * function抽出
	 */
	private static String functionRex = "\\s*([^\\s]+)\\(\\)\\s*[\\{]*";
	/*************************** Handler-Method ********************************/
    private void checkIncludeSH(String lineStr, TObj rootObj, int line) throws Exception{
		Pattern p = Pattern.compile(includeRex);
		Matcher m = p.matcher(lineStr);

		while(m.find()){
			String value = m.group(1);
			checkValue(value);

			//rootObj.add(new TDpd(INCLUDE_SHELL, value,value,100, new TLocation(line)));
			wol("		checkIncludeSH = " + value + " LINE >> " + line);
		}
	}

    public void checkIncludeEnv(String lineStr, TObj rootObj, int line) throws Exception{
		Pattern p = Pattern.compile(includeEnv);
		Matcher m = p.matcher(lineStr);

		while(m.find()){
			//String value = m.group(1); // path
			String value2 = m.group(2); // env file
			//checkValue(value);
			checkValue(value2);

			//rootObj.add(new TDpd(INCLUDE_ENV, value2,value2,100, new TLocation(line)));
			wol("		checkIncludeEnv = " + value2 + " LINE >> " + line);
		}
	}

    public boolean checkExp(String lineStr) {
		Pattern p = Pattern.compile(expRex);
		Matcher m = p.matcher(lineStr);

		if(m.find()){
			return true;
		}

		return false;
	}

    public boolean checkExpTable(String lineStr, TObj rootObj, int line) throws Exception{

		boolean isFind = false;

		Pattern p = Pattern.compile(expTableRex);
		Matcher m = p.matcher(lineStr);

		if(m.find()){
			String value = m.group(1);
			checkValue(value);
			value = value.trim();
			isFind = true;
			//rootObj.add(new TDpd(OUTPUT_DUMP, value,value,100, new TLocation(line)));
			wol("		checkExpTable = " + value + " LINE >> " + line);
		}
		return isFind;
	}

    public boolean checkExpFile(String lineStr, TObj rootObj, HashMap<Integer, String> lineMap, int currentLine) throws Exception{

		boolean isFind = false;

		Pattern p = Pattern.compile(expFileRex);
		Matcher m = p.matcher(lineStr);

		if(m.find()){
			String value = m.group(1);
			checkValue(value);
			value = value.trim();


			if(isParamValue(value)){
				wol("		isParamValue >> " + value);
				value = getParamValue(value);
				String paramRex = value + "=([^\\s]+\\.dmp)";
				Pattern p2 = Pattern.compile(paramRex);
				Matcher m2 = null;
				for(int i = currentLine+1 ; i != 1 ; i--){
					String lineValue = lineMap.get(i);
					if(lineValue == null) { continue;}
					m2 =  p2.matcher(lineValue);
					if(m2.find()){
						String dumpName = m2.group(1);
						checkValue(dumpName);
						value = dumpName;
						//rootObj.add(new TDpd(RUN_SHELL, value,value,100, new TLocation(currentLine)));
						wol("		isParamValue = " + value + " LINE >> " + currentLine);
						break;
					}
				}
			}else{
				wol("		is not ParamValue >> " + value);
			}
			//rootObj.add(new TDpd(OUTPUT_DUMP, value,value,100, new TLocation(line)));
			wol("		checkExpFile = " + value + " LINE >> " + currentLine);
		}
		return isFind;
	}

    public void checkRunCobol(String lineStr, TObj rootObj, int line) throws Exception{

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
			//rootObj.add(new TDpd(RUN_COBOL, value,value,100, new TLocation(line)));
			wol("		checkRunCobol = " + value + " LINE >> " + line);
		}
	}

    public void checkRunShell(String lineStr, TObj rootObj, HashMap<Integer, String> lineMap, int currentLine) throws Exception{
		Pattern p = Pattern.compile(runShellRex);
		Matcher m = p.matcher(lineStr);
		if(m.find()){
			String value = m.group(1);
			checkValue(value);
			value = value.trim();

			// 実行対象のシェルが変数として設定されている場合 (DN4BCARMAIND047 case)
			if(isParamValue(value)){
				value = getParamValue(value);
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
						//rootObj.add(new TDpd(RUN_SHELL, value,value,100, new TLocation(currentLine)));
						wol("		checkRunShell = " + value + " LINE >> " + currentLine);
						break;
					}
				}
			}else{
				//rootObj.add(new TDpd(RUN_SHELL, value,value,100, new TLocation(currentLine)));
				wol("		checkRunShell = " + value + " LINE >> " + currentLine);
			}
		}
	}

    public void checkTar(String lineStr, TObj rootObj, HashMap<Integer, String> lineMap, int currentLine) throws Exception{
		Pattern p = Pattern.compile(tarRex);
		Matcher m = p.matcher(lineStr);

		if(m.find()){
			String value = m.group(2);
			checkValue(value);
			//rootObj.add(new TDpd(OUTPUT_TAR, value,value,100, new TLocation(currentLine)));
			wol("		checkTar = " + value + " LINE >> " + currentLine);
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
		}
	}

    public void checkGzip(String lineStr, TObj rootObj, HashMap<Integer, String> lineMap, int currentLine) throws Exception{
		Pattern p = Pattern.compile(gzipRex);
		Matcher m = p.matcher(lineStr);
		while(m.find()){
			String value = m.group(1);
			checkValue(value);
			//rootObj.add(new TDpd(OUTPUT_TAR, value,value,100, new TLocation(currentLine)));
			wol("		checkGzip = " + value + " LINE >> " + currentLine);
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
		}
	}

    public void checkSortShell(String lineStr, TObj rootObj, HashMap<Integer, String> lineMap, int currentLine) throws Exception{
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
						//rootObj.add(new TDpd(SORT_SHELL, value,value,100, new TLocation(currentLine)));
						wol("		checkSortShell = " + value + " LINE >> " + currentLine);
						break;
					}
				}
			}else{
				//rootObj.add(new TDpd(SORT_SHELL, value,value,100, new TLocation(currentLine)));
				wol("		checkSortShell = " + value + " LINE >> " + currentLine);
			}
		}
	}

    public void checkCsv(String lineStr, TObj rootObj, HashMap<Integer, String> lineMap, int currentLine) throws Exception{
		Pattern p = Pattern.compile(csvRex);
		Matcher m = p.matcher(lineStr);
		if(m.find()){
			String value = m.group(1);
			checkValue(value);
			String paramRex = "([^\\s]*)/([^\\s]+\\.csv)";
			Pattern p2 = Pattern.compile(paramRex);
			Matcher m2 = p2.matcher(value);
			if(m2.find()){
				value = m2.group(2);
				checkValue(value);
				//rootObj.add(new TDpd(OUTPUT_CSV, value,value,100, new TLocation(currentLine)));
				wol("		checkCsv = " + value + " LINE >> " + currentLine);
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
		}
	}

    public boolean checkSTRT_MSGLG(String lineStr, String rex) {
		Pattern p = Pattern.compile(rex);
		Matcher m = p.matcher(lineStr);

		if(m.find()){
			return true;
		}

		return false;
	}

    public boolean checkSTRT_LOG(String lineStr, String rex) {
		Pattern p = Pattern.compile(rex);
		Matcher m = p.matcher(lineStr);

		if(m.find()){
			return true;
		}

		return false;
	}

    public void checkCp(String lineStr, TObj rootObj, int line) throws Exception{

    	Pattern p = Pattern.compile(cpRex);
		Matcher m = p.matcher(lineStr);

		while(m.find()){
			String value = m.group(1);
			String value2 = m.group(2);
			checkValue(value);
			checkValue(value2);

			//rootObj.add(new TDpd(RUN_COBOL, value,value,100, new TLocation(line)));
			wol("		checkCp file1 = " + value + " LINE >> " + line);
			wol("		checkCp file2 = " + value2 + " LINE >> " + line);
		}
	}

    public void checkCopyFile(String lineStr, TObj rootObj, int line) throws Exception{

    	Pattern p = Pattern.compile(copyFileRex);
		Matcher m = p.matcher(lineStr);

		while(m.find()){
			String value = m.group(4);
			String value2 = m.group(5);
			checkValue(value);
			checkValue(value2);

			//rootObj.add(new TDpd(RUN_COBOL, value,value,100, new TLocation(line)));
			wol("		checkCopyFile file1 = " + value + " LINE >> " + line);
			wol("		checkCopyFile file2 = " + value2 + " LINE >> " + line);
		}
	}

    public void checkRm(String lineStr, TObj rootObj, int line) throws Exception{

    	Pattern p = Pattern.compile(rmRex);
		Matcher m = p.matcher(lineStr);

		while(m.find()){
			String value = m.group(1);
			checkValue(value);
			//rootObj.add(new TDpd(RUN_COBOL, value,value,100, new TLocation(line)));
			wol("		checkRm path = " + value + " LINE >> " + line);

		}
	}

    public boolean checkFunction(String lineStr) {
		Pattern p = Pattern.compile(functionRex);
		Matcher m = p.matcher(lineStr);

		if(m.find()){
			wol("value >> " + m.group(1));
			return true;
		}

		return false;
	}
    /*************************** Handler-Common ********************************/
    public void checkValue(String value) throws Exception{
		if(value == null){
			throw new Exception("Value is Null");
		}
	}

    public String getParamValue(String valueStr){

		String isParamValue = valueStr;

		Pattern p = Pattern.compile(subValueRex);
		Matcher m = p.matcher(valueStr);

		if(m.find()){
			isParamValue = m.group(1);
		}


		return isParamValue;
	}

	public boolean isParamValue(String valueStr){

		boolean isParamValue = false;

		Pattern p = Pattern.compile(subValueRex);
		Matcher m = p.matcher(valueStr);


		isParamValue = m.find();

		return isParamValue;
	}

    public void wol(String message){
    	if(showLog){
    		System.out.println(message);
    	}
    }



}
