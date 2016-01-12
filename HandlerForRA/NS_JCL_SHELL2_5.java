
package changeminer.HandlerForRA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import changeminer.HandlerForRA.NS_JCL_SHELL2.PatternUtil;

import com.itplus.cm.parser.common.CMParserCommonData;
import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.DBUtil;
import com.itplus.cm.ce.util.Environment;
import com.itplus.cm.ce.util.FileUtil;

import extractor.common.tobj.TDpd;
import extractor.common.tobj.TLocation;
import extractor.common.tobj.TMeta;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;

public class NS_JCL_SHELL2_5 extends HandlerForRA
{
    private final static int CALL_PGM = 1300002;
    private final static int USE_DSN = 1300008;
    private final static int USE_DD = 1300007;
    private final static int JOB_ID = 130003;
    private final static int JOB_STEP = 130005;
    private final static int JOB_STEP_DD = 130006;

    private final static int COMMAND = 821173; // ERE}EiEh
    private final static int USE_COMMAND = 8211103; // Use Command
    private final static int RUN_SORT = 8211104; // Run Sort
    private final static int USE_ASSIGN = 8211105; // Use assign file
    private final static int DD_DETAIL = 8211102;  // use DD File(detail)
    private final static int CALL_SHELL = 8211106;  // Call Shell
    private final static int CALL_C_MODULE = 8211107; //Call C Module
    private final static int USE_PARAM = 8211108;

    private final String skipshell[]= {"JOBINIT.sh", "init.sh", "FUNCLIB.sh" };

    String paramlist[] = {"DD_SYSPRM", "FILEI1","FILEO1","DD_SYSOUT","SYSIN","FILEI2",
    "LIST,","LIST_PRM","DD_PRMRDR","DD_PRTINP","DD_PRINTR","DD_SYSIN","DD_SYSUT1"
    ,"DD_SYSUT2" };

    private String JCLNAME = "NONE";
    private String STEPNAME = "NONE";
    private final String  __shell_run_output = "__RUN_SHELL_OUTPUT__";

    // 1 = Main, 2 = Sub, 3 = Core
    public static int SHELL_TYPE = -1;
    public static MainShellUtil mainShellUtil;
    public static SubShellUtil subShellUtil;
    public static CModuleInfo cModuleUtil;
    public static HashMap<String, String> cobolFileList;
    public Hashtable<String, String>  ht;
    public static Hashtable<String, String>  htOrigin;
    public HashMap<String, String> moduleMap = new HashMap<String, String>();
    public boolean DebugMode = true;
    public String currentShellFile = null;
    String timingStr = "";

    public NS_JCL_SHELL2_5() {}

    public String getName() { return this.getClass().getName(); }

    public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
        return RETURN_CONTINUE;
    }

    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
        String file_name = Environment.getSourceDir() + "/" + cm_src.getCOLLECT_ID() + cm_src.getSPATH() + cm_src.getSNAME();
        //log.debug("NS_BATCH_SHELL", "addAnalyzeStep : " + file_name);
        TObj obj_root = tresult.getTObjList()[0];
        JCLNAME = "NONE";
        STEPNAME = "NONE";

        debugLog("collect id >> " + cm_src.getCOLLECT_ID());
        debugLog("filter id >> " + cm_src.getANALYZE_FILTER_ID());

        initEnv(cm_src);
        /* EVEFEai?eOE^ECEvC?i?C?CE */
        initShellType(cm_src);
        /* C ModuleEaEXEgeEia */
        initCModuleInfos(cm_src);

        /* COBOL */
        initCobolFileList(cm_src);
        currentShellFile = cm_src.getSNAME();

        addstep(file_name, obj_root, ht);
        return RETURN_CONTINUE;
    }

    public void initEnv(CM_SRC src) throws Exception{

    	if(htOrigin == null){
    		Connection con = null;
    		try{
    			con = DBUtil.getConnection(false);
    			long envId = getAnalyzeTypeIdFromTitle(src.getCOLLECT_ID(), src.getANALYZE_FILTER_ID(), con,  "shellenv");
    			if(envId > 0){
                    debugLog(">>>>>Start initEnv");
                    String envFilePath = getEnvFile(envId, con);
                    init_ht(envFilePath);
                    debugLog(">>>>>End initEnv");
                }else{
                    debugLog(">>>>>No data initEnv");
                }
    		}catch(Exception e){
    			throw e;
    		}finally{
    			if(con != null){
            		if(!con.isClosed()){
            			con.close();
            		}
            		con = null;
            	}
    		}
    	}

    	ht = new Hashtable<String, String>();
		for(Map.Entry<String, String> entry : htOrigin.entrySet()){
		    String key = entry.getKey();
		    String value = entry.getValue();
		    ht.put(key, value);
		}
    }

    public void initCobolFileList(CM_SRC src) throws Exception{

        if(cobolFileList == null){
            Connection con = null;//DBUtil.getConnection(false);
            try{
            	con = DBUtil.getConnection(false);
            	long cobolId = getAnalyzeTypeIdFromTitle(src.getCOLLECT_ID(), src.getANALYZE_FILTER_ID(), con,  "cobol");
                if(cobolId > 0){
                    debugLog(">>>>>Start initCobolFileList");
                    cobolFileList = getCobolList(cobolId, con);
                    debugLog(">>>>>End initCobolFileList");
                }else{
                    debugLog(">>>>>No data initCobolFileList");
                    cobolFileList = new HashMap<String, String>();
                }
            }catch(Exception e){
            	throw e;
            }finally{
            	if(con != null){
            		if(!con.isClosed()){
            			con.close();
            		}
            		con = null;
            	}
            }
        }
    }

    //i?eOiUCAi?eOiPa?E^ECEvC?iaif
    public void initShellType(CM_SRC src) throws Exception{
        if(SHELL_TYPE < 0){
            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection con = null;
            try {
                con = DBUtil.getConnection(false);
                ps = con.prepareStatement("SELECT " +
                                          "cato.ANALYZE_OPTION_VALUE " +
                                          "FROM " +
                                          "CM_ANALYZE_TYPE_FILTER catf, " +
                                          "CM_ANALYZE_TYPE_OPTION cato " +
                                          "WHERE " +
                                          "catf.ANALYZE_TYPE_ID = cato.ANALYZE_TYPE_ID AND " +
                                          "catf.COLLECT_ID = ? AND " +
                                          "cato.ANALYZE_OPTION_NAME = 'user-define-option' AND " +
                                          "catf.IS_DELETE = 'N' ");
                ps.setLong(1, src.getCOLLECT_ID());
                rs = ps.executeQuery();

                if(rs.next()){
                    //return rs.getString("OPTION_VALUE");
                    String type = rs.getString("ANALYZE_OPTION_VALUE");
                    if(type == null || type.isEmpty()){
                        type = "";
                    }
                    type = type.trim().toLowerCase();
                    if(type.equals("main")){
                        SHELL_TYPE = 1;
                    }else if(type.equals("sub")){
                        SHELL_TYPE = 2;
                    }else if(type.equals("core")){
                        SHELL_TYPE = 3;
                    }else{
                        debugLog("i?eOEIEvEVEaEiE^ECEvC?e?iEC?CIC?C￠C?CπCOAB ");
                    }

                	initMainCallShellInfo(src.getCOLLECT_ID(), src.getANALYZE_FILTER_ID(), con);
                    initSubCallShellInfo(src.getCOLLECT_ID(), src.getANALYZE_FILTER_ID(), con);
                    debugLog("Check >> " + SHELL_TYPE);
                }
            } catch(SQLException e) {
                throw e;
            } finally {
                DBUtil.closeResource(con, ps, rs);
            }
        }
    }

    //MainEVEFEaCcCAAACall Shella÷oAAAEAE^EfA[E^C?eEia
    public void initMainCallShellInfo(long collectId, long filterId, Connection conn) throws Exception{

        long mainShell = getAnalyzeTypeIdFromTitle(collectId, filterId, conn, "main");
        if(mainShell > 0){
            debugLog(">>>>>Start initMainCallShellInfo");
            HashMap<String, ArrayList<String>> callInfos = getMainCallShellList(mainShell, conn);
            HashMap<String, HashMap<String,String>> metaInfos = getMainMetaList(mainShell, conn);
            mainShellUtil = new MainShellUtil(callInfos, metaInfos);
            debugLog(">>>>>OK");
        }else{
            debugLog(">>>>>No data initMainCallShellInfo");
            HashMap<String, ArrayList<String>> callInfos = new HashMap<String, ArrayList<String>>();
            HashMap<String, HashMap<String,String>> metaInfos = new HashMap<String, HashMap<String,String>>();
            mainShellUtil = new MainShellUtil(callInfos, metaInfos);
        }
    }

    //SubEVEFEaCcCAAACall Shella÷oAAAEAE^EfA[E^C?eEia
    public void initSubCallShellInfo(long collectId, long filterId, Connection con) throws Exception{

        long subShell = getAnalyzeTypeIdFromTitle(collectId, filterId, con, "sub");
        if(subShell > 0){
            debugLog(">>>>>Start initSubCallShellInfo");
            HashMap<String, ArrayList<String>> callInfos = getSubCallShellList(subShell, con);
            subShellUtil = new SubShellUtil(callInfos);
            debugLog(">>>>>OK");
        }else{
            debugLog(">>>>>No data initSubCallShellInfo");
            HashMap<String, ArrayList<String>> callInfos = new HashMap<String, ArrayList<String>>();
            subShellUtil = new SubShellUtil(callInfos);
        }
    }

    public void initCModuleInfos(CM_SRC src) throws Exception{

        if(cModuleUtil == null){
            Connection con = null;//DBUtil.getConnection(false);
            try{
            	con = DBUtil.getConnection(false);
            	long cModuleId = getAnalyzeTypeIdFromTitle(src.getCOLLECT_ID(), src.getANALYZE_FILTER_ID(), con,  "c(module)");
                if(cModuleId > 0){
                    debugLog(">>>>>Start initCModuleInfos");
                    HashMap<String, String> moduleInfo = getCModuleList(cModuleId, con);
                    cModuleUtil = new CModuleInfo(moduleInfo);
                    debugLog(">>>>>OK");
                }else{
                    debugLog(">>>>>No data initCModuleInfos");
                    HashMap<String, String> moduleInfo = new HashMap<String, String>();
                    cModuleUtil = new CModuleInfo(moduleInfo);
                }
            }catch(Exception e){
            	throw e;
            }finally{
            	if(con != null){
            		if(!con.isClosed()){
            			con.close();
            		}
            		con = null;
            	}
            }
        }
    }

    public String getEnvFile(long analyzeTypeId, Connection con) throws Exception{

        PreparedStatement ps = null;
        ResultSet rs = null;
        String filePath = null;

        try {
        	ps = con.prepareStatement("SELECT " +
        							  "ci.REMOTE_FILE_PATH || src.SPATH || src.SNAME AS PATH " +
        							  "FROM " +
        							  "CM_ANALYZE_TYPE_FILTER  filter, CM_SRC src, CM_COLLECT_INFO ci " +
        							  "WHERE " +
        							  "filter.ANALYZE_TYPE_ID = ? AND " +
        							  "filter.IS_DELETE = 'N' AND " +
        							  "src.CHANGE_REASON_CODE < 9000 AND " +
        							  "src.COLLECT_ID = ci.COLLECT_ID AND " +
        							  "filter.COLLECT_ID = ci.COLLECT_ID AND " +
        							  "src.SNAME = ? "
        							  );
        	ps.setLong(1, analyzeTypeId);
        	ps.setString(2, "MCATS_run.env");
            rs = ps.executeQuery();
            if(rs.next()){
            	filePath = rs.getString("PATH");
            }
        } catch(SQLException e) {
            throw e;
        } finally {
            DBUtil.closeResource(ps, rs);
        }
        return filePath;
    }

    public long getAnalyzeTypeIdFromTitle(long collectId, long filterId, Connection con, String targetTitle) throws Exception{

        PreparedStatement ps = null;
        ResultSet rs = null;
        long targetAnalyzeId = -1;

        try {
            ps = con.prepareStatement("SELECT " +
                                      "filter.COLLECT_ID, filter.FILTER_STRING, filter.ANALYZE_FILTER_ID, cat.ANALYZE_TYPE_ID, cat.ANALYZE_TYPE_TITLE, catr.REF_ANALYZE_TYPE_ID, ty.ANALYZE_TYPE_TITLE AS TITLE " +
                                      "FROM " +
                                      "CM_ANALYZE_TYPE_FILTER filter, CM_ANALYZE_TYPE cat, CM_ANALYZE_TYPE_REF catr, CM_ANALYZE_TYPE ty " +
                                      "WHERE " +
                                      "filter.ANALYZE_TYPE_ID = cat.ANALYZE_TYPE_ID AND " +
                                      "catr.REF_ANALYZE_TYPE_ID = ty.ANALYZE_TYPE_ID AND " +
                                      "cat.ANALYZE_TYPE_ID = catr.ANALYZE_TYPE_ID AND " +
                                      "catr.REF_TYPE = 3 AND " +
                                      "filter.IS_DELETE = 'N' AND " +
                                      "collect_id = ? AND " +
                                      "analyze_filter_id = ? ");
            ps.setLong(1, collectId);
            ps.setLong(2, filterId);
            rs = ps.executeQuery();

            while(rs.next()){
                long ids = rs.getLong("REF_ANALYZE_TYPE_ID");
                String title = rs.getString("TITLE").toLowerCase().trim();
                if(title.contains(targetTitle.toLowerCase())){
                    targetAnalyzeId = ids;
                    break;
                }
            }
            debugLog("getAnalyzeTypeIdFromTitle == " + targetTitle);
            debugLog("collect id >> " + collectId);
            debugLog("filter id >> " + filterId);
            debugLog("targetAnalyzeId id >> " + targetAnalyzeId);
        } catch(SQLException e) {
            throw e;
        } finally {
            DBUtil.closeResource(ps, rs);
        }
        return targetAnalyzeId;
    }

    /* Main shellCACall_Shella?eoeOiOC?eEia */
    public HashMap<String, ArrayList<String>> getMainCallShellList(long analyzeTypeId, Connection con) throws Exception{

        PreparedStatement ps = null;
        ResultSet rs = null;
        HashMap<String, ArrayList<String>> callInfo = new HashMap<String, ArrayList<String>>();
        try {
            ps = con.prepareStatement("SELECT " +
                                      "DISTINCT filter.FILTER_STRING, src.SNAME, obj.OBJ_NAME, dpd.CALL_TARGET, dpd.DPD_TYPE_ID, ty.TYPE_NAME " +
                                      "FROM " +
                                      "CM_ANALYZE_TYPE_FILTER  filter, CM_SRC src, CM_OBJ obj,  CM_OBJ_DPD dpd, CM_TYPE ty " +
                                      "WHERE " +
                                      "filter.ANALYZE_TYPE_ID = ? AND " +
                                      "src.ANALYZE_FILTER_ID = filter.ANALYZE_FILTER_ID AND " +
                                      "filter.IS_DELETE = 'N' AND " +
                                      "src.SRC_ID = obj.SRC_ID AND " +
                                      "src.CHANGE_REASON_CODE < 9000 AND " +
                                      "obj.OBJ_ID = dpd.CALLER_OBJ_ID AND " +
                                      "dpd.DPD_TYPE_ID = ty.TYPE_ID AND " +
                                      "dpd.DPD_TYPE_ID = 8211106 " +
                                      "ORDER BY dpd.CALL_TARGET "
                                      );
            ps.setLong(1, analyzeTypeId);
            rs = ps.executeQuery();

            while(rs.next()){
                String mainSh = rs.getString("SNAME");
                String targetSh = rs.getString("CALL_TARGET");
                ArrayList<String> mainShs = new ArrayList<String>();
                if(callInfo.containsKey(targetSh)){
                    mainShs = callInfo.get(targetSh);
                    callInfo.remove(targetSh);
                }
                if(!mainShs.contains(mainSh)){
                    mainShs.add(mainSh);
                }
                callInfo.put(targetSh, mainShs);
            }
        } catch(SQLException e) {
            throw e;
        } finally {
            DBUtil.closeResource(ps, rs);
        }
        return callInfo;
    }

    /* Sub shellCACall_Shella?eoeOiOC?eEia */
    public HashMap<String, ArrayList<String>> getSubCallShellList(long analyzeTypeId, Connection con) throws Exception{

        PreparedStatement ps = null;
        ResultSet rs = null;
        HashMap<String, ArrayList<String>> callInfo = new HashMap<String, ArrayList<String>>();
        try {
            ps = con.prepareStatement("SELECT " +
                                      "DISTINCT filter.FILTER_STRING, src.SNAME, obj.OBJ_NAME, dpd.CALL_TARGET, dpd.DPD_TYPE_ID, ty.TYPE_NAME " +
                                      "FROM " +
                                      "CM_ANALYZE_TYPE_FILTER  filter, CM_SRC src, CM_OBJ obj,  CM_OBJ_DPD dpd, CM_TYPE ty " +
                                      "WHERE " +
                                      "filter.ANALYZE_TYPE_ID = ? AND " +
                                      "src.ANALYZE_FILTER_ID = filter.ANALYZE_FILTER_ID AND " +
                                      "filter.IS_DELETE = 'N' AND " +
                                      "src.SRC_ID = obj.SRC_ID AND " +
                                      "src.CHANGE_REASON_CODE < 9000 AND " +
                                      "obj.OBJ_ID = dpd.CALLER_OBJ_ID AND " +
                                      "dpd.DPD_TYPE_ID = ty.TYPE_ID AND " +
                                      "dpd.DPD_TYPE_ID = 8211106 " +
                                      "ORDER BY dpd.CALL_TARGET "
                                      );
            ps.setLong(1, analyzeTypeId);
            rs = ps.executeQuery();

            while(rs.next()){
                //return rs.getString("OPTION_VALUE");
                String subSh = rs.getString("SNAME");
                String targetSh = rs.getString("CALL_TARGET");
                ArrayList<String> subShs = new ArrayList<String>();
                if(callInfo.containsKey(targetSh)){
                    subShs = callInfo.get(targetSh);
                    callInfo.remove(targetSh);
                }
                if(!subShs.contains(subSh)){
                    subShs.add(subSh);
                }
                callInfo.put(targetSh, subShs);
            }
        } catch(SQLException e) {
            throw e;
        } finally {
            DBUtil.closeResource(ps, rs);
        }
        return callInfo;
    }
    /* Main shellCAmetaeOiOC?eEia */
    public HashMap<String, HashMap<String,String>> getMainMetaList(long analyzeTypeId, Connection con) throws Exception{

        PreparedStatement ps = null;
        ResultSet rs = null;
        //HashMap<String, ArrayList<String>> callInfo = new HashMap<String, ArrayList<String>>();
        HashMap<String, HashMap<String,String>> metaInfos = new HashMap<String, HashMap<String,String>>();
        try {
            ps = con.prepareStatement("SELECT " +
                                      "DISTINCT filter.FILTER_STRING, src.SNAME, meta.META_NAME, meta.META_VALUE " +
                                      "FROM " +
                                      "CM_ANALYZE_TYPE_FILTER  filter, CM_SRC src, CM_OBJ obj, CM_OBJ_META meta " +
                                      "WHERE " +
                                      "filter.ANALYZE_TYPE_ID = ? AND " +
                                      "src.ANALYZE_FILTER_ID = filter.ANALYZE_FILTER_ID AND " +
                                      "filter.IS_DELETE = 'N' AND " +
                                      "src.SRC_ID = obj.SRC_ID AND " +
                                      "obj.OBJ_ID = meta.OBJ_ID AND " +
                                      "src.CHANGE_REASON_CODE < 9000 AND " +
                                      "obj.OBJ_TYPE_ID = 821174 " +
                                      "ORDER BY src.SNAME "
                                      );
            ps.setLong(1, analyzeTypeId);
            rs = ps.executeQuery();

            while(rs.next()){
                String mainSh = rs.getString("SNAME");
                String metaName = rs.getString("META_NAME");
                String metaValue = rs.getString("META_VALUE");
                HashMap<String,String> metas = new HashMap<String,String>();
                if(metaInfos.containsKey(mainSh)){
                    metas = metaInfos.get(mainSh);
                    metaInfos.remove(mainSh);
                }
                if(metas.containsKey(metaName)){
                    metas.remove(metaName);
                }
                metas.put(metaName, metaValue);
                metaInfos.put(mainSh, metas);
            }
        } catch(SQLException e) {
            throw e;
        } finally {
            DBUtil.closeResource(ps, rs);
        }

        return metaInfos;
    }
    /* C ModuleCAE\A[EXeOiOC?eEia */
    public HashMap<String, String> getCModuleList(long analyzeTypeId, Connection con) throws Exception{

        PreparedStatement ps = null;
        ResultSet rs = null;

        HashMap<String, String> moduleInfos = new HashMap<String, String>();
        try {
            ps = con.prepareStatement("SELECT "	+
                                      "DISTINCT src.SPATH, src.SNAME, obj.FULL_OBJ_NAME " +
                                      "FROM " +
                                      "CM_ANALYZE_TYPE_FILTER  filter, CM_SRC src, CM_OBJ obj,  CM_TYPE ty " +
                                      "WHERE " +
                                      "filter.ANALYZE_TYPE_ID = ? AND " +
                                      "src.ANALYZE_FILTER_ID = filter.ANALYZE_FILTER_ID AND " +
                                      "filter.IS_DELETE = 'N' AND " +
                                      "src.SRC_ID = obj.SRC_ID AND " +
                                      "src.CHANGE_REASON_CODE < 9000 AND " +
                                      "obj.OBJ_TYPE_ID = ty.TYPE_ID AND " +
                                      "obj.OBJ_TYPE_ID = 511101 " +
                                      "ORDER BY src.SPATH, src.SNAME"
                                      );
            ps.setLong(1, analyzeTypeId);
            rs = ps.executeQuery();

            @SuppressWarnings("unused")
			String checkFileName = "";
            HashMap<String, String> duplicatedCheck = new HashMap<String, String>();

            while(rs.next()){
                String moduleSpath = rs.getString("SPATH").toLowerCase();
                String moduleSname = rs.getString("SNAME");
                String moduleFullObjStr = rs.getString("FULL_OBJ_NAME");
                if(moduleSpath.contains("common") || moduleSpath.contains("lib") ||moduleSpath.contains("tools")){ continue; }
                String checkName = moduleSname.replace(".c", "").replace(".pc", "");
                String checkDir = checkName.toLowerCase();
                if(duplicatedCheck.containsKey(checkName)){
                    checkFileName = duplicatedCheck.get(checkName);
                    if(moduleSname.contains(".pc")) {
                        debugLog("					remove data;");
                        duplicatedCheck.remove(checkName);
                        moduleInfos.remove(checkName);
                    }
                }
                duplicatedCheck.put(checkName, moduleSname);
                if(moduleSpath.contains(checkDir)){
                    if(!moduleInfos.containsKey(checkName)){
                        moduleInfos.put(checkName, moduleFullObjStr);
                    }
                }
            }

        } catch(SQLException e) {
            throw e;
        } finally {
            DBUtil.closeResource(ps, rs);
        }

        return moduleInfos;
    }

    public HashMap<String, String> getCobolList(long analyzeTypeId, Connection con) throws Exception{

        PreparedStatement ps = null;
        ResultSet rs = null;

        HashMap<String, String> fileList = new HashMap<String, String>();
        try {
            ps = con.prepareStatement("SELECT "	+
                                      "src.SNAME, obj.FULL_OBJ_NAME " +
                                      "FROM " +
                                      "CM_ANALYZE_TYPE_FILTER  filter, CM_SRC src, CM_OBJ obj,  CM_TYPE ty " +
                                      "WHERE " +
                                      "filter.ANALYZE_TYPE_ID = ? AND " +
                                      "src.ANALYZE_FILTER_ID = filter.ANALYZE_FILTER_ID AND " +
                                      "filter.IS_DELETE = 'N' AND " +
                                      "src.SRC_ID = obj.SRC_ID AND " +
                                      "src.CHANGE_REASON_CODE < 9000 AND " +
                                      "obj.OBJ_TYPE_ID = ty.TYPE_ID AND " +
                                      "obj.OBJ_TYPE_ID = 110001 " +
                                      "ORDER BY src.SPATH, src.SNAME"
                                      );
            ps.setLong(1, analyzeTypeId);
            rs = ps.executeQuery();

            while(rs.next()){
                String cobolSname = rs.getString("SNAME");
                String cobolFileName = cobolSname.toLowerCase().replace(".cbl", "").replace(".pco", "");
                String cobolFullObjSname = rs.getString("FULL_OBJ_NAME");
                fileList.put(cobolFileName, cobolFullObjSname);
            }
        } catch(SQLException e) {
            throw e;
        } finally {
            DBUtil.closeResource(ps, rs);
        }

        return fileList;
    }

    // EnvEtE@ECEaCcCAAAi?eieOiOC?eEia
    private void init_ht(String file_name) throws Exception {

        FileInputStream fis = null;
        BufferedReader br = null;

        try {
            // OPEN FILE
            File file = new File(file_name);
            fis = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(fis));
            htOrigin = new Hashtable<String, String>();

            while (br.ready()) {
                String line_data = br.readLine();
                if (line_data == null) continue;
                line_data = line_data.trim();
                if (line_data.startsWith("#")) continue;
                if (line_data.startsWith("echo")) continue;
                line_data = change_line_data(line_data, htOrigin);
                if (line_data.indexOf('=') > 0) {
                    find_assing_value(line_data, htOrigin);
                }
            }

        }  finally {
            try {
                br.close();
                fis.close();
            }  catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    public int addstep(String file_name , TObj obj_root, Hashtable<String, String> ht) throws Exception {
        FileInputStream fis = null;
        BufferedReader br = null;
        try {
            // OPEN FILE
            File file = new File(file_name);
            fis = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(fis));
            int line_cnt =0;
            TObj job_obj = obj_root;
            TObj step_obj = obj_root;

            Hashtable<String, TObj> objht = new Hashtable<String, TObj>();
            String mline = "";
            int lineseq = 0;
            boolean findcontinue = false;
            boolean eofcontinue = false;
            HashMap<String, String> timings = new HashMap<String, String>();

            boolean isIfLine = false;
            int ifDepth = 0;

            //CECEWEOA[Eaa?eo
            while (br.ready()) {
                String line_data = br.readLine();
                if (line_data == null) continue;
                line_data = line_data.trim();
                line_cnt++;
                if (line_data.startsWith("#")) continue;
                if (line_data.startsWith("echo ")) continue;
                //if (line_data.startsWith("if ")) continue;
                line_data = conv_doller(line_data);
                if(line_data.endsWith("\\")) {
                    line_data = line_data.substring(0,line_data.length() - 1);
                    if(findcontinue == false) {
                        mline = line_data;
                        lineseq = line_cnt;
                        findcontinue= true;
                    } else {
                        mline += line_data;
                    }
                    continue ;

                }
                else if(line_data.endsWith("<<EOF")) {  // cat >${DATA}/${PID}.S152.SYSIN<<EOF, sqlplus
                    mline = line_data;
                    lineseq = line_cnt;
                    eofcontinue= true;
                    continue ;
                }
                else {
                    if(findcontinue) {
                        findcontinue = false;
                        mline += line_data;
                    }
                    else if(eofcontinue) { // EOF
                        if(line_data.equals("EOF")) {
                            eofcontinue = false;
                            line_data = "";
                        } else {
                            mline += "\n" + line_data;
                            continue ;
                        }
                    }
                    else {
                        mline = line_data;
                        lineseq = line_cnt;
                    }
                }

                if(SHELL_TYPE == 3){
                	if(mline.trim().startsWith("if") && mline.trim().contains("TIMING")){
                    	isIfLine = true;
                    	debugLog(lineseq + "  +++++ if Line Start >> " + mline);

                    	String checkLineStr = mline.replace("\"", "");
                    	ArrayList<String> result1 = PatternUtil.getValues(checkLineStr, "[\\s]*\\$[\\{]*TIMING[\\}]*[\\s]*(!=|=)[\\s]*([^\\s]+)[\\s]*");
                    	for(int i = 0 ; i < result1.size() ; i++){
                    		if(i % 2 == 0){
                    			//key
                    			debugLog(" i >> " + i);
                    			String value = result1.get(i);
                    			String key = result1.get(i+1);
                    			if(!timings.containsKey(key)){
                    				timings.put(key, value);
                    			}
                    		}

                    	}
                    	ArrayList<String> result2 = PatternUtil.getValues(checkLineStr, "[\\s]*\\$[\\{]*TIMING[\\}]*[\\s]*(!=|=)[\\s]*([^\\s]+)[\\s]*-[A-Za-z]+[\\s]*([^\\s\\$]+)[\\s]*");
                    	for(int i = 0 ; i < result2.size() ; i++){
                    		if(i % 3 == 1){
                    			String value = result2.get(i-1);
                    			String key = result2.get(i);
                    			if(!timings.containsKey(key)){
                    				timings.put(key, value);
                    			}
                    		}else if(i % 3 == 2){
                    			String value = result2.get(i-2);
                    			String key = result2.get(i);
                    			if(!timings.containsKey(key)){
                    				timings.put(key, value);
                    			}
                    		}
                    	}

                    	 for(Map.Entry<String, String> entry : timings.entrySet()){
                             String key = entry.getKey();
                             String value = entry.getValue();
                             debugLog("	key : value >> " + key + " , " + value);
                         }
                    }

                    if(isIfLine){
                    	if(mline.trim().startsWith("if")){
                    		ifDepth++;
                    	}else if(mline.equals("then")){
                    		debugLog(lineseq + "  +++++  >> " + mline);
                    		if(ifDepth == 1){
                    			if(timingStr.isEmpty()){
                        			for(Map.Entry<String, String> entry : timings.entrySet()){
                                        String key = entry.getKey();
                                        String value = entry.getValue();
                                        if(value.equals("=")){
                                        	timingStr += key+",";
                                        }
                                    }

                        			if(timings.entrySet().size() != 0 && !timingStr.isEmpty()){
                        				timingStr = timingStr.substring(0, timingStr.length()-1);
                        			}

                        		}
                        		debugLog("	then >> " + timingStr);
                    		}
                    	}else if(mline.equals("else")){
                    		debugLog(lineseq + "  +++++  >> " + mline);

                    		if(ifDepth == 1){
                    			timingStr = "";
                    			for(Map.Entry<String, String> entry : timings.entrySet()){
                                    String key = entry.getKey();
                                    String value = entry.getValue();
                                    if(value.equals("!=")){
                                    	timingStr += key+",";
                                    }
                                }

                    			if(timings.entrySet().size() != 0 && !timingStr.isEmpty()){
                    				timingStr = timingStr.substring(0, timingStr.length()-1);
                    			}

                    			debugLog("	else >> " + timingStr);

                    		}
                    	}else if(mline.equals("fi")){
                    		ifDepth--;
                    		if(ifDepth < 1){
                    			ifDepth = 0;
                    			isIfLine = false;
                    			timings.clear();
                    			timings = new HashMap<String, String>();
                    			timingStr = "";
                    			debugLog(lineseq + "  +++++ if Line End >> " + mline);
                    		}else{
                    			debugLog(lineseq + "  +++++ if Line if  >> " + ifDepth);
                    		}
                		}
                    }
                }

                String key = null;

                if ((mline.indexOf('=') > 0) && !mline.trim().startsWith("if")) {
                //if (mline.indexOf('=') > 0) {
                	key = find_assing_value(mline, ht);
                    if(key != null) {
                        if(JCLNAME.equals("NONE") && key.equals("JCLNAME")) {
                            JCLNAME = ht.get(key);
                            job_obj = new TObj(JOB_ID, JCLNAME, JCLNAME, 100, new TLocation(lineseq) );
                            obj_root.add(job_obj);
                            step_obj = job_obj;
                        }

                        dd_add(key, step_obj, ht, lineseq, objht);


                        if(SHELL_TYPE == 1){
                            //8212305 modulename meta meta
                            //8212305 org_jclname meta
                            if(key.equals(mainShellUtil.hasMeta(MainShellUtil.MODULENAME))){
                                TMeta shellMeta = new TMeta(1000, MainShellUtil.MODULENAME, (String)ht.get(key), new TLocation(line_cnt));
                                obj_root.add(shellMeta);
                            }else if(key.equals(mainShellUtil.hasMeta(MainShellUtil.ORG_JCLNAME))){
                                TMeta shellMeta = new TMeta(1000, MainShellUtil.ORG_JCLNAME, (String)ht.get(key), new TLocation(line_cnt));
                                obj_root.add(shellMeta);
                            }
                        }
                        if(cModuleUtil.isEnable && cModuleUtil.findCModule(ht.get(key))){
                            debugLog("");
                            debugLog("");
                            debugLog("****** find_cmodules  ******************************");
                            debugLog("callCModuleParam >> " + key);
                            debugLog("callCModule >> " + ht.get(key));
                            //callCModule = ht.get(key);
                            //callCModuleParam = key;
                            if(moduleMap.containsKey(key)){ //
                                moduleMap.remove(key);
                            }
                            moduleMap.put(key, ht.get(key));
                            debugLog("put >> TEMP");
                        }
                    }
                }

                TObj objtmp = find_start_step(mline, lineseq, job_obj, obj_root, ht);
                if (objtmp != null) {
                    step_obj = objtmp;
                }
                if (find_end_step(mline, lineseq, obj_root, ht)) {
                	step_obj = job_obj; // step eae?au e?aOaA jobe@ c?e?aC e?iw assign

                    debugLog("***** objht clear start ************************");
                    for(Map.Entry<String, TObj> entry : objht.entrySet()){
                        String htKey = entry.getKey();
                        TObj obj = entry.getValue();
                        debugLog("Key : " + htKey + " , Value : " + obj.getGID());
                    }
                    debugLog("***** objht clear end ************************");
                    objht.clear();
                }

                if (step_obj != null) {
                    // individual case

                	find_runcobol(mline, step_obj, ht, lineseq, objht); // ok
                    find_runsort(mline, step_obj, ht, lineseq, objht); // ok
                    find_exp(mline, step_obj, ht, lineseq, objht); //ok
                    find_imp(mline, step_obj, ht, lineseq, objht); //ok
                    find_expdp(mline, step_obj, ht, lineseq, objht); //ok
                    find_impdp(mline, step_obj, ht, lineseq, objht); //ok
                    find_sh(mline, step_obj, ht, lineseq, objht);//ok
                    find_sqlplus(mline, step_obj, ht, lineseq, objht); //ok
                    find_cat(mline, step_obj, ht, lineseq, objht); //ok

                    // timer cmd
                    find_timercmd(mline, step_obj, ht, lineseq, objht);

                    // parameter case 1
                    /* パラメータ複数対応 */

                    find_tar(mline, step_obj, ht, lineseq, objht);
                    find_gzip(mline, step_obj, ht, lineseq, objht);
                    find_rm(mline, step_obj, ht, lineseq, objht);
                    find_cp(mline, step_obj, ht, lineseq, objht);
                    find_mv(mline, step_obj, ht, lineseq, objht);
                    find_sqlldr(mline, step_obj, ht, lineseq, objht);
                    find_ojob_cp(mline, step_obj, ht, lineseq, objht);
                    find_ojob_rm(mline, step_obj, ht, lineseq, objht);
                    find_MCT010(line_data, step_obj, ht, lineseq, objht);
                    find_MCT011(line_data, step_obj, ht, lineseq, objht);
                    find_DCPSCPY_VL(line_data, step_obj, ht, lineseq, objht);


                	//find_mv(mline, step_obj, ht, lineseq, objht);
                    //CECEWEOA[EaCAa?eoe?au
                    //3 Pattern 1 i?eiC?egopC∑CEEpE^A[Ei
                    //			2 ECEWEOA[EanoCAC?CAEpE^A[Ei
                    //			3 ECEWEOA[EanoC?EpEaEAA[E^C?egopC∑CEEpE^A[Ei

                    find_cmodules(line_data, step_obj, ht, lineseq, objht);

                }
            }
            showTObjTree(step_obj, 0, true);

        }  finally {
            try {
                br.close();
                fis.close();
                ht.clear();
                ht = null;

            }  catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return RETURN_CONTINUE;

    }
    // $xxxx -> ${xxx} C…i?a∑
    private String conv_doller(String line_data) {
        int idx = 0;
        while (true) {
            idx = line_data.indexOf('$', idx);
            if(idx  == -1)
                return line_data;
            if(idx + 1 >= line_data.length())
                return line_data;
            if(line_data.charAt(idx+1) != '{')
                break;
            idx = idx + 1;
        }

        //debugLog("conv ***** >> " + line_data);
        //debugLog("conv ***** >> " + idx);

        char[] cList = line_data.toCharArray();
        String str = "";

        for (int k = 0; k < cList.length; k++) {
            if(cList[k] == '\"' &&  k+2 < cList.length && cList[k+1] == '$') { // "$xxx" iuiaa? c≠i≠ inen e≫ea
                str += cList[k];
                str += cList[k+1];
                k += 1;

          //      debugLog("		1" + str);
            }
            else if(cList[k] == '$' && k+2 < cList.length && cList[k+1] == '{' )  {
                int endpos = -1;
                for (int j = k; j < cList.length; j++) {
                    str += cList[j];
                    if(cList[j] == '}') {
                        endpos = j;
                        break;
                    }
                }
                if(endpos == -1)
                    endpos = cList.length;
                k = endpos;

            } else if(cList[k] == '$' && k+2 < cList.length && cList[k+1] >= 'A' && cList[k+1] <= 'Z' )  {  // // $$ ,$1,$2
                String key = "";
                int endpos = -1;
                for (int j = k+1; j < cList.length; j++) {
                    if(cList[j] == ' ' || cList[j] == '/' || cList[j] == '>' || cList[j] == '|') {
                        endpos = j;
                        str +="${" + key + "}";
                        str += cList[j];
                       // debugLog("		2" + str);
                        break;
                    }
                    key += cList[j];
                }
                if(endpos == -1) {
                    str +="${" + key + "}";
                    endpos = cList.length;
                }
                k = endpos;
            } 	else {
                str += cList[k];
            //   debugLog("		3" + str);
            }
        }
        ////log.debug("doller 222", str);
        return str;
    }
    //    cat ${FILEI1} >  ${FILEO1},  cat ${FILEI2} >> ${FILEO1}
    // cat > xxx
    // a
    // b
    // EOF
    // cp -p ${DD_SYSUT1} ${DD_SYSUT2} 1>>$tempout  2>>$temperr
    private void find_tar(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        //   tar -cvf ${DD_TAPE01}  ${DATA}/${MODULENAME}  1>>$tempout 2>>$temperr
        String cmd = "tar";
        if(line_data.indexOf(cmd) == -1) return ;
        //String patternstr = "tar([\\s]+-[a-z]*[\\s]+|[\\s]+)([^\\s]+)[\\s]+(.+)[\\s]+1>>\\$tempout[\\s]+2>>\\$temperr";

        String patternstr   = "^[\\s]*tar([\\s]+-[a-z]+[\\s]+|[\\s]+)([^\\s]+)[\\s]+(.+)([\\s]*|[\\s]+1>>[^\\s]+[\\s]+2>>[^\\s]+)";
        String rePatternstr = "^[\\s]*tar([\\s]+-[a-z]+[\\s]+|[\\s]+)([^\\s]+)[\\s]+([^\\s]+)";

        ArrayList<Integer> ar = new ArrayList<Integer>();
        ar.add(new Integer(3));

        Pattern p = Pattern.compile(patternstr);
        Matcher m = p.matcher(line_data);


        if(m.find()){
			String msg = m.group(3);
			ArrayList<String> params = PatternUtil.getValues(msg, "([^\\s]+)", new String[]{"(1>>[^\\s]+)","(2>>[^\\s]+)","(1>[^\\s]+)","(2>>[^\\s]+)"});//subParam(msg); //str.startsWith("1>>") || str.startsWith("2>>") || str.startsWith("1>") || str.startsWith("2>")
			int paramCnt = 4;

			for(int i = 0; i < params.size() ; i++){
				ar.add(new Integer(paramCnt));
				rePatternstr += "[\\s]+([^\\s]+)";
				paramCnt++;
			}
			find_sub(cmd, rePatternstr, ar,
	                 line_data, step_obj,  ht, line_count, objht );
		}

    }

    private void find_gzip(String line_data, TObj step_obj, Hashtable<String,String>  ht, int line_count, Hashtable<String, TObj> objht) {
        //     gzip ${DD_TAPE01}
        String cmd = "gzip";
        if(line_data.indexOf(cmd) == -1) return ;
        String patternstr = "^[\\s]*gzip([\\s]+-[a-z]*[\\s]+|[\\s]+)(.+)";
        String rePatternstr = "^[\\s]*gzip([\\s]+-[a-z]*[\\s]+|[\\s]+)";

        String gzipValue = PatternUtil.check(line_data, patternstr, 2);

        if(!gzipValue.isEmpty()){
        	ArrayList<Integer> ar = new ArrayList<Integer>();


            int cnt = 2;
            ArrayList<String> params = PatternUtil.getValues(gzipValue, "([^\\s]+)");

            for(int i = 0 ; i < params.size() ; i++){
				String value = params.get(i);
				if(value.equals("<") || value.equals(">") || value.equals("&") || value.isEmpty() || PatternUtil.isMatched(value, new String[]{"(-[A-Za-z]+)"})
						|| PatternUtil.isMatched(value, new String[]{"([0-9]+>>[^\\s]+)"}) || value.equals(">>") || PatternUtil.isMatched(value, new String[]{"([0-9]+>[^\\s]+)"})){
					if(value.equals("<")){

					}else if(value.equals(">")){

					}else if(value.equals("&")){
						value = "\\&";
					}else if(value.isEmpty()){

					}else if(PatternUtil.isMatched(value, new String[]{"(-[A-Za-z]+)"})){
					}else if(PatternUtil.isMatched(value, new String[]{"([0-9]+>>[^\\s]+)"})){
						value = value.replace("$", "\\$");
					}
					rePatternstr +=  value;
				}else{
					rePatternstr += "([^\\s]+)";
					ar.add(new Integer(cnt));
					cnt++;
				}

				if(i + 1 == params.size()){
					rePatternstr += "[\\s]*";
				}else{
					rePatternstr += "[\\s]+";
				}
			}

            debugLog("	gzip rex >> " + rePatternstr);
            debugLog("	gzip cnt >> " + cnt);
            find_sub(cmd, rePatternstr, ar,
                    line_data, step_obj,  ht, line_count, objht );
        }




        /* 修正前
        String cmd = "gzip";
        String patternstr = "gzip([\\s]+-[a-z]*[\\s]+|[\\s]+)([^\\s]+)";

        ArrayList<Integer> ar = new ArrayList<Integer>();
        ar.add(new Integer(2));
        find_sub(cmd, patternstr, ar,
                 line_data, step_obj,  ht, line_count, objht );
        */
    }

    private void find_sqlldr(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        //   sqlldr ${ORAUSER} control=${JOB_PATH}/BOI_GROSS_RATE.ctl direct=true rows=10000 DATA=${DATA}/${MODULENAME}.${JCLNAME}.S040.FILEO1 LOG=${DATA}/BOI_GROSS_RATE.log  1>>$tempout  2>>$temperr
        String sqlldr = "sqlldr";

        if(line_data.indexOf(sqlldr) == -1) return ;

        String patternstr = "^[\\s]*sqlldr[\\s]+([^\\s]+)[\\s]+(.+)";
        String rePatternstr = "^[\\s]*sqlldr[\\s]+([^\\s]+)[\\s]+";

        line_data = line_data.replace("\n", "");
        String sqlldrParam = PatternUtil.check(line_data, patternstr, 2);
        debugLog("sqlldrParam >> " + sqlldrParam);

        ArrayList<Integer> ar = new ArrayList<Integer>();

        int cnt = 1;
        ArrayList<String> subValues = PatternUtil.getValues(sqlldrParam, "([^\\s]+)");

        for(int i = 0; i < subValues.size() ; i++){
			String subValue = subValues.get(i);


			String parfile = PatternUtil.check(subValue, "(parfile|PARFILE)=([^\\s]+)", 2);
			String control = PatternUtil.check(subValue, "(control|CONTROL)=([^\\s]+)", 2);
			String data = PatternUtil.check(subValue, "(data|DATA)=([^\\s]+)", 2);


			if(!parfile.isEmpty()){
				rePatternstr += "(parfile|PARFILE)=([^\\s]+)";
				cnt += 2;
				ar.add(new Integer(cnt));
			}else if(!control.isEmpty()){
				rePatternstr += "(control|CONTROL)=([^\\s]+)";
				cnt += 2;
				ar.add(new Integer(cnt));
			}else if(!data.isEmpty()){
				rePatternstr += "(data|DATA)=([^\\s]+)";
				cnt += 2;
				ar.add(new Integer(cnt));
			}else{
				subValue = subValue.replace("$", "\\$");
				subValue = subValue.replace("{", "\\{");
				subValue = subValue.replace("}", "\\}");
				rePatternstr += subValue;
			}

			if(i+1 == subValues.size()){
				rePatternstr += "[\\s]*";
			}else{
				rePatternstr += "[\\s]+";
			}
		}

        find_sub(sqlldr, rePatternstr, ar,
                line_data, step_obj,  ht, line_count, objht );



        /* 修正前
        String cmd = "sqlldr";
        String patternstr = "sqlldr[\\s]+.*control=([^\\s]+)[\\s]+([^\\s]+).*DATA=([^\\s]+)";
        ArrayList<Integer> ar = new ArrayList<Integer>();
        ar.add(new Integer(1));
        ar.add(new Integer(3));
        find_sub(cmd, patternstr, ar,
                 line_data, step_obj,  ht, line_count, objht );
        */
    }
    private void find_MCT010(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        // MCT010 ${DATA}/${MODULENAME}.${JCLNAME}.S230.SYS023 ${PARAML}/${CMNREGIONCD}${JCLNAME}.S230.SYS023
        String cmd = "MCT010";
        String patternstr = "MCT010[\\s]+([^\\s]+)[\\s]+([^\\s]+)";
        ArrayList<Integer> ar = new ArrayList<Integer>();
        ar.add(new Integer(1));
        ar.add(new Integer(2));
        find_sub(cmd, patternstr, ar,
                 line_data, step_obj,  ht, line_count, objht );
    }
    private void find_DCPSCPY_VL(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        // MCT011 ${DATA}/${MODULENAME}.${JCLNAME}.S080.PRINTR ${PARAML}/${CMNREGIONCD}${JCLNAME}.S080.PRINTR
        String cmd = "DCPSCPY_VL";
        String patternstr = "DCPSCPY_VL[\\s]+([^\\s]+)[\\s]+([^\\s]+)";
        ArrayList<Integer> ar = new ArrayList<Integer>();
        ar.add(new Integer(1));
        ar.add(new Integer(2));
        find_sub(cmd, patternstr, ar,
                 line_data, step_obj,  ht, line_count, objht );
    }
    private void find_MCT011(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        // MCT011 ${DATA}/${MODULENAME}.${JCLNAME}.S080.PRINTR ${PARAML}/${CMNREGIONCD}${JCLNAME}.S080.PRINTR
        String cmd = "MCT011";
        String patternstr = "MCT011[\\s]+([^\\s]+)[\\s]+([^\\s]+)";
        ArrayList<Integer> ar = new ArrayList<Integer>();
        ar.add(new Integer(1));
        ar.add(new Integer(2));
        find_sub(cmd, patternstr, ar,
                 line_data, step_obj,  ht, line_count, objht );
    }
    private void find_ojob_rm(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        // ojob_rm ${DATA}/DN4BCARSENDD020.${CMNPLANTCD}DN4B21HM.CN4B2101        1>>$tempout  2>>$temperr
        String cmd = "ojob_rm";
        if(PatternUtil.isMatched(line_data, new String[]{"ojob_rm([\\s]+-[a-z]*[\\s]+|[\\s]+)"})){
        	debugLog("ojob_rm Start check doble qua Matched >> " + line_data);
        	String reStr = "";
        	if(line_data.contains("\"")){
        		ArrayList<String> paramList = PatternUtil.getValues(line_data, "ojob_rm([\\s]+-[a-z]*[\\s]+|[\\s]+)[\\\"]+(.+)[\\\"]+");
        		if(paramList.size() != 0){
        			reStr = "ojob_rm";
        			for(int i = 0 ; i < paramList.size() ; i++){
            			String paramStr = paramList.get(i);
            			if(paramStr.trim().isEmpty()){
            				continue;
            			}
            			reStr += " " + paramStr.trim();
            		}
        		}
        	}

        	if(!reStr.isEmpty()){
        		line_data = reStr;
        		debugLog("	ojob_rm Start check doble qua reString >> " + line_data);
        	}

        	String patternstr = "ojob_rm([\\s]+-[a-z]*[\\s]+|[\\s]+)([^\\s]+)";
            ArrayList<Integer> ar = new ArrayList<Integer>();
            ar.add(new Integer(2));
            find_sub(cmd, patternstr, ar,
                         line_data, step_obj,  ht, line_count, objht );
        }

    }
    private void find_ojob_cp(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        //   ojob_cp  "${DATA}/${MODULENAME}.${CMNPLANTCD}DN4B112M.PARAMETER_61.BACKUP"              "${DATA}/${MODULENAME}/." 1>>$tempout  2>>$temperr
        String cmd = "ojob_cp";
        if(PatternUtil.isMatched(line_data, new String[]{"ojob_cp([\\s]+-[a-z]*[\\s]+|[\\s]+)"})){
        	debugLog("ojob_cp Start check doble qua Matched >> " + line_data);
        	String reStr = "";
        	if(line_data.contains("\"")){
        		ArrayList<String> paramList = PatternUtil.getValues(line_data, "ojob_cp([\\s]+-[a-z]*[\\s]+|[\\s]+)[\\\"]+(.+)[\\\"]+[\\s]+[\\\"]+(.+)[\\\"]+");
        		if(paramList.size() != 0){
        			reStr = "ojob_cp";
        			for(int i = 0 ; i < paramList.size() ; i++){
            			String paramStr = paramList.get(i);
            			if(paramStr.trim().isEmpty()){
            				continue;
            			}
            			reStr += " " + paramStr.trim();
            		}
        		}
        	}

        	if(!reStr.isEmpty()){
        		line_data = reStr;
        		debugLog("	ojob_cp Start check doble qua reString >> " + line_data);
        	}

        	String patternstr = "ojob_cp([\\s]+-[a-z]*[\\s]+|[\\s]+)([^\\s]+)[\\s]+([^\\s]+)";
            ArrayList<Integer> ar = new ArrayList<Integer>();
            ar.add(new Integer(2));
            ar.add(new Integer(3));
            find_sub(cmd, patternstr, ar,
                         line_data, step_obj,  ht, line_count, objht );
        }


    }
    private void find_rm(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        //     rm ${DATA}/${MODULENAME}/*  1>>$tempout  2>>$temperr
        String cmd = "rm";
        String patternstr = "rm([\\s]+-[a-z]*[\\s]+|[\\s]+)([^\\s]+)";
        ArrayList<Integer> ar = new ArrayList<Integer>();
        ar.add(new Integer(2));
        find_sub(cmd, patternstr, ar,
                 line_data, step_obj,  ht, line_count, objht );
    }

    private void find_cp(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        //       cp ${DD_SYSUT1} ${DD_SYSUT2}
        //debugLog("Find Cp >>>>>>>>>>>>>>>>>>");
        /*
    	String cmd = "cp";
        String patternstr = "cp([\\s]+-[a-z]*[\\s]+|[\\s]+)([^\\s]+)[\\s]+([^\\s]+)";
        ArrayList<Integer> ar = new ArrayList<Integer>();
        ar.add(new Integer(2));
        ar.add(new Integer(3));
        find_sub(cmd, patternstr, ar,
                 line_data, step_obj,  ht, line_count, objht );
        */
    	String cmd = "cp";
        String patternstr = "cp([\\s]+-[a-z]*[\\s]+|[\\s]+)([^\\s]+)[\\s]+(.+)";
        String newPatternstr = "cp([\\s]+-[a-z]*[\\s]+|[\\s]+)([^\\s]+)[\\s]+";
        ArrayList<Integer> ar = new ArrayList<Integer>();
        ar.add(new Integer(2));

        Pattern p = Pattern.compile(patternstr);
		Matcher m = p.matcher(line_data);

		if(m.find()){
			String msg = m.group(3);
			ArrayList<String> params = PatternUtil.getValues(msg, "([^\\s]+)", new String[]{"(1>>[^\\s]+)","(2>>[^\\s]+)","(1>[^\\s]+)","(2>>[^\\s]+)"});//subParam(msg);
			int paramCnt = 3;
			for(int i = 0; i < params.size() ; i++){

				ar.add(new Integer(paramCnt));
				//newPatternstr+="([^\\s]+)[\\s]+";
				if(i+1 == params.size()){
					newPatternstr+="([^\\s]+)[\\s]*";
				}else{
					newPatternstr+="([^\\s]+)[\\s]+";
				}
				paramCnt++;
			}

			debugLog("cp pattern >> " + newPatternstr);

			find_sub(cmd, newPatternstr, ar,
	                 line_data, step_obj,  ht, line_count, objht );
		}


    }

    private void find_mv(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        String cmd = "mv";
        String patternstr = "mv([\\s]+-[a-z]*[\\s]+|[\\s]+)([^\\s]+)[\\s]+(.+)";
        String newPatternstr = "mv([\\s]+-[a-z]*[\\s]+|[\\s]+)([^\\s]+)[\\s]+";
        ArrayList<Integer> ar = new ArrayList<Integer>();
        ar.add(new Integer(2));

        Pattern p = Pattern.compile(patternstr);
		Matcher m = p.matcher(line_data);

		if(m.find()){
			String msg = m.group(3);
			ArrayList<String> params = PatternUtil.getValues(msg, "([^\\s]+)", new String[]{"(1>>[^\\s]+)","(2>>[^\\s]+)","(1>[^\\s]+)","(2>>[^\\s]+)"});//subParam
			int paramCnt = 3;
			for(int i = 0; i < params.size() ; i++){
				String value = params.get(i);
				if(PatternUtil.isMatched(value, new String[]{"(>>)", "([0-9]>\\&[0-9])"})){
					value = value.replace("&", "\\&");
					newPatternstr+=value;
				}else{
					ar.add(new Integer(paramCnt));
					newPatternstr+="([^\\s]+)";
					paramCnt++;
				}


				if( i + 1 == params.size()){
					newPatternstr+="[\\s]*";
				}else{
					newPatternstr+="[\\s]+";
				}

			}

			debugLog("find_mv	" + newPatternstr);
			find_sub(cmd, newPatternstr, ar,
	                 line_data, step_obj,  ht, line_count, objht );
		}

		/*
        String cmd = "mv";
        String patternstr = "mv([\\s]+-[a-z]*[\\s]+|[\\s]+)([^\\s]+)[\\s]+(.+)";
        String newPatternstr = "mv([\\s]+-[a-z]*[\\s]+|[\\s]+)([^\\s]+)[\\s]+";
        ArrayList<Integer> ar = new ArrayList<Integer>();
        ar.add(new Integer(2));

        Pattern p = Pattern.compile(patternstr);
		Matcher m = p.matcher(line_data);

		if(m.find()){
			String msg = m.group(3);
			ArrayList<String> params = PatternUtil.getValues(msg, "([^\\s]+)", new String[]{"(1>>[^\\s]+)","(2>>[^\\s]+)","(1>[^\\s]+)","(2>>[^\\s]+)"});//subParam
			int paramCnt = 3;
			for(int i = 0; i < params.size() ; i++){

				ar.add(new Integer(paramCnt));
				if( i + 1 == params.size()){
					newPatternstr+="([^\\s]+)[\\s]*";
				}else{
					newPatternstr+="([^\\s]+)[\\s]+";
				}

				paramCnt++;
			}

			debugLog("find_mv	" + newPatternstr);
			find_sub(cmd, newPatternstr, ar,
	                 line_data, step_obj,  ht, line_count, objht );
		}
		*/
    }

    /*
    public boolean checkContinue(String str, String cmd){
    	boolean isContinue = true;

    	if(cmd.equals("rm")){
    		if(str.equals(">>")){
    			isContinue = false;
        	}
    	}else if(cmd.equals("subParam")){
    		if(str.startsWith("1>>") || str.startsWith("2>>") || str.startsWith("1>") || str.startsWith("2>")){
    			isContinue = false;
			}
    	}

    	return isContinue;
    }
    */

    /*
    public ArrayList<String> subParam(String str){

		String subParam = "([^\\s]+)";
		ArrayList<String> params = new ArrayList<String>();

		Pattern p = Pattern.compile(subParam);
		Matcher m = p.matcher(str);

		while(m.find()){

			for(int i = 0 ; i < m.groupCount() ; i++){
				String param = m.group(i);
				if(!checkContinue(param, "subParam")){
					continue;
				}
				params.add(param);
			}
		}
		return params;
	}
	*/

	private void find_sub(String cmd, String patternstr, ArrayList<Integer> poslist,
    					   String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {

    	if(!line_data.startsWith(cmd)) { return ; }

    	debugLog("Start find_sub ##################################");
    	debugLog(cmd + " Start");
    	String arglist[] = get_regstr(line_data, patternstr, poslist);
    	if(arglist.length > 0){

    		//cmd obj 生成
    		TObj cmdObj = createCommand(cmd, line_count, step_obj);
    		debugLog("arglist size == " + arglist.length);

    		for(int cnt = 0; cnt < arglist.length; cnt++){

    			String param = arglist[cnt];
    			String beforeParam = null;
    			if(cnt != 0){
    				beforeParam = arglist[cnt-1];
    			}


    			makeAssignDpd(cnt, param, cmd, cmdObj, line_count, ht, objht, step_obj);
    		}

    	}else{
    		//not matched
    	}


    }

    private void dd_addObj(String objhtKey, String cmdName, TObj cmdObj, Hashtable<String, TObj> objht, TObj stepObj){

    	debugLog("					dd_addObj >> objhtKey : " + objhtKey + " , cmdName : " + cmdName + " , cmdObj : " + cmdObj.getGID() + " , stepObj : " + stepObj.getGID() );
    	TObj obj = objht.get(objhtKey);
        if(obj == null) {
            debugLog("not found key " + objhtKey);
            return ;
        }
        String ddname = obj.getName();
        String gid = stepObj.getName() + "." + cmdName + "." +  ddname;
        // obj.setName(gid);
        debugLog("change dd name " + "DD fullname before="+obj.getGID() + ",after="+gid);
        obj.setGID(gid);
        TDpd dpdlist[] = obj.getTDpdList();
        if(dpdlist.length == 0) { debugLog("					obj has no dpds"); }
        for ( int i = 0; i < dpdlist.length; i ++) {
            //copy_dpd(dpd_type_id, cmdobj, dpdlist[i]);
        	debugLog("							Dpd List not copy >> " + dpdlist[i].getGID());
        }
        objht.put(objhtKey, obj);
        cmdObj.add(obj);
    }

    private TObj createCommand(String cmdName, int line, TObj rootObj){

    	String objname = rootObj.getGID() + "." + cmdName;
        TObj cmdobj = new TObj(COMMAND, objname, objname, 100, new TLocation(line) );
        rootObj.add(cmdobj);
        debugLog("createCommand obj >> " + objname);

        return cmdobj;
    }


    private ArrayList<String> convModuleParam(String paramStr){

    	String convParam = paramStr;
    	ArrayList<String> convParamList = new ArrayList<String>();
    	if(mainShellUtil.checkParams(convParam)){

            boolean hasMainShell = false;
            boolean hasSubShell = false;
            debugLog("CModuleParam Target Params Exist ****");
            //Main Shell Check
            debugLog("CModuleParam 	GetShellMeta Type == 1 or 2 ****");
            ArrayList<MainShellInfo> metaList = mainShellUtil.getMainShellInfos(currentShellFile);
            for(int ii = 0 ; ii < metaList.size() ; ii++){
                MainShellInfo valuei = metaList.get(ii);
                String newValue = mainShellUtil.getStringConvMeta(valuei, paramStr);
                convParamList.add(newValue);
                debugLog("CModuleParam 	MainShell[" + valuei.getShellName()+"] Call >>>>>>> " + currentShellFile);
                debugLog("CModuleParam 		add USE_ASSIGN conv newValue >> " + newValue);
                hasMainShell = true;
            }


            debugLog("CModuleParam GetShellMeta Type == 3 ****");
            ArrayList<String> subShells = subShellUtil.getSubList(currentShellFile);
            for(int i = 0 ; i < subShells.size() ; i++){
                String subShell = subShells.get(i);
                ArrayList<MainShellInfo> mainShells = mainShellUtil.getMainShellInfos(subShell);
                for(int ii = 0 ; ii < mainShells.size() ; ii++){
                    MainShellInfo valuei = mainShells.get(ii);
                    String newValue = mainShellUtil.getStringConvMeta(valuei, paramStr);
                    convParamList.add(newValue);
                    debugLog("CModuleParam 	MainShell[" + valuei.getShellName()+"]  >>>>>>> SubShell[" + subShell + "] >>>>>>> Core|SubShell[" + valuei.getShellName() + "]");
                    debugLog("CModuleParam 		add USE_ASSIGN conv newValue >> " + newValue);
                    hasSubShell = true;
                }
            }


            if(!hasMainShell && !hasSubShell){

            	debugLog("CModuleParam 	has no mainShell and subShell  == 0 ****");
                debugLog("CModuleParam 	add USE_ASSIGN >> " + paramStr);
                convParamList.add(paramStr);
            }

        }else{
        	convParamList.add(paramStr);
        }

    	return convParamList;
    }


    private void find_cmodules(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht){
        String cmd = "CModule";

        if(!cModuleUtil.isEnable) { return; }

        ArrayList<String> keys = new ArrayList<String>();

        for(Map.Entry<String, String> entry : moduleMap.entrySet()){
            String key = entry.getKey(); //paramname
            String value = entry.getValue(); //modulename

            String rex = "^(\\$" + key + "|\\$\\{" + key + "\\})(\\s+)(.+)";
            Pattern p = Pattern.compile(rex);
            Matcher m = p.matcher(line_data);
            if(m.find()){

                keys.add(key);

                debugLog("");
                debugLog("****** find_cmodules type 1 *****************");
                debugLog("		line_data >> "	+ line_data);
                debugLog("		modulename	>> " + value);
                debugLog("		moduleParam	>> " + m.group(3));

                String convLineData = line_data.replace(m.group(1), value);
                String convParamData = change_line_data(m.group(3), ht);
                convLineData = change_line_data(convLineData, ht);
                String moduleFullpath = cModuleUtil.getModuleGid(value);
                debugLog("		conv param >> " + convParamData);
                debugLog("		conv line >> " + convLineData);
                debugLog("		moduelFullPath >> " + moduleFullpath);

                ArrayList<String> params = convModuleParam(convParamData);

                debugLog("convParam >> " + convParamData);


                TDpd targetModule = new TDpd(CALL_C_MODULE, moduleFullpath, moduleFullpath, 100, new TLocation(line_count));
                //TDpd targetParam = new TDpd(USE_PARAM, convParamData, convParamData, 100, new TLocation(line_count));

                //String objname = STEPNAME + "." + cmd;
                String objname = STEPNAME + "." + cmd;
                String newObjName = objname + "(" + value + ")";
                TObj cmdobj = new TObj(COMMAND, objname, newObjName, 100, new TLocation(line_count) );
                cmdobj.add(targetModule);
                for(int i = 0 ; i < params.size() ; i++){
                	debugLog("*****>>>>>> params index : " + i);
                	debugLog("*****convdata >>  " + params.get(i));
                	TDpd targetParam = new TDpd(USE_PARAM, params.get(i), params.get(i), 100, new TLocation(line_count));
                	cmdobj.add(targetParam);
                }
                step_obj.add(cmdobj);
                debugLog("");
                showTObjTree(cmdobj, 0, true);
            }
        }

        if(keys.size() != 0){
            for(int i = 0 ; i < keys.size() ; i++){
                if(moduleMap.containsKey(keys.get(i))){
                    moduleMap.remove(keys.get(i));
                    debugLog("Remove keys >> " + keys.get(i));
                }
            }
        }

        String targets = cModuleUtil.ModuleListRex;//"(DN63TE)";

        //String type2Rex = "^" + targets + "([\\s]*)";
        String type2Rex = "^" + targets + "$";
        String type3Rex = "^" + targets + "(\\s+)(.+)";
        String type4Rex = "^RUN_PROC(\\s+)" + targets+"([\\s]*)";
        Pattern type2P = Pattern.compile(type2Rex);
        Pattern type3P = Pattern.compile(type3Rex);
        Pattern type4P = Pattern.compile(type4Rex);
        Matcher type2M = type2P.matcher(line_data);
        Matcher type3M = type3P.matcher(line_data);
        Matcher type4M = type4P.matcher(line_data);

        if(type2M.find()){
            debugLog("****** find_cmodules type 2 *****************");
            debugLog("		line_data >> "	+ line_data);
            debugLog("		moduleName >> " + type2M.group(1));

            String moduleFullpath = cModuleUtil.getModuleGid(type2M.group(1));

            TDpd targetModule = new TDpd(CALL_C_MODULE, moduleFullpath, moduleFullpath, 100, new TLocation(line_count));

            String objname = STEPNAME + "." + cmd;
            String newObjName = objname + "(" + type2M.group(1) + ")";
            TObj cmdobj = new TObj(COMMAND, objname, newObjName, 100, new TLocation(line_count) );
            cmdobj.add(targetModule);
            step_obj.add(cmdobj);

            debugLog("");
            showTObjTree(cmdobj, 0, true);
        }

        if(type3M.find()){
        	debugLog("****** find_cmodules type 3 *****************");
        	debugLog("		line_data >> "	+ line_data);
        	debugLog("		moduleName >> " +  type3M.group(1));
        	debugLog("		moduleParam >> " +  type3M.group(3));

            String convParamData = change_line_data(type3M.group(3), ht);
            String moduleFullpath = cModuleUtil.getModuleGid(type3M.group(1));

            debugLog("		convParam >> " + convParamData);

            ArrayList<String> params = convModuleParam(convParamData);

            debugLog("convParam >> " + convParamData);


            TDpd targetModule = new TDpd(CALL_C_MODULE, moduleFullpath, moduleFullpath, 100, new TLocation(line_count));
            //TDpd targetParam = new TDpd(USE_PARAM, convParamData, convParamData, 100, new TLocation(line_count));

            String objname = STEPNAME + "." + cmd;
            String newObjName = objname + "(" + type3M.group(1) + ")";
            TObj cmdobj = new TObj(COMMAND, objname, newObjName, 100, new TLocation(line_count) );
            cmdobj.add(targetModule);

            for(int i = 0 ; i < params.size() ; i++){
            	debugLog("*****>>>>>> params index : " + i);
            	debugLog("*****convdata >>  " + params.get(i));
            	TDpd targetParam = new TDpd(USE_PARAM, params.get(i), params.get(i), 100, new TLocation(line_count));
            	cmdobj.add(targetParam);
            }
            step_obj.add(cmdobj);
            debugLog("");
            showTObjTree(cmdobj, 0, true);
        }

        if(type4M.find()){
            debugLog("****** find_cmodules type 4 *****************");
            debugLog("		line_data >> "	+ line_data);
            debugLog("		moduleName >> " +  type4M.group(2));

            String moduleFullpath = cModuleUtil.getModuleGid(type4M.group(2));

            TDpd targetModule = new TDpd(CALL_C_MODULE, moduleFullpath, moduleFullpath, 100, new TLocation(line_count));

            String objname = STEPNAME + "." + cmd;
            String newObjName = objname + "(" + type4M.group(2) + ")";
            TObj cmdobj = new TObj(COMMAND, objname, newObjName, 100, new TLocation(line_count) );
            cmdobj.add(targetModule);
            step_obj.add(cmdobj);

            debugLog("");
            showTObjTree(cmdobj, 0, true);
        }

    }

    private String[] get_regstr(String line_data, String patternstr, ArrayList<Integer> poslist) {
        Pattern p = Pattern.compile(patternstr);
        ArrayList<String> ar = new ArrayList<String>();
        Matcher m = p.matcher(line_data);
        if (m.find())
        {
            for ( int i = 0 ; i < poslist.size(); i ++) {
                Integer aa = poslist.get(i);
                String data = m.group(aa.intValue());
                if(data.indexOf('`') != -1) {

                    String data1 = data.substring(0, data.indexOf('`'));
                    String data2 = data.substring(data.indexOf('`')+1);
                    Pattern p2 = Pattern.compile("(`[^`]+`[^\\s]*)");
                    Matcher m2 = p2.matcher(line_data);
                    // xxx`yyy bbb`zzz
                    // data  -> xxx`yyy   data1 --> xxx,  data2 --> `yyy
                    while (m2.find())
                    {
                        String value  = m2.group(1); // `yyy bbb`zzz
                        if(value.indexOf(data2) != -1) {
                            //log.debug("`````="+data,  data1 + value);
                            data = data1 + value;
                            break;
                        }
                    }
                }
                ar.add(data);
            }
        }
        return (String[])ar.toArray(new String[0]);
    }

    private void find_cat(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {

        String cat = "cat";
        if(line_data.indexOf(cat) == -1) return ;
        //log.debug("cat", line_data);
        debugLog("############# Start find_cat ###################");
        debugLog("line data >> " + line_data);
        debugLog("line count >> " + line_count);


        String catParams = PatternUtil.check(line_data, "^[\\s]*cat([\\s]+-[a-z]*[\\s]+|[\\s]+)(.+)", 2);

        if(!catParams.isEmpty()){
        	//check1(value, "([^>]+)\\s*[>>]+\\s*([^\\s]+)");
        	debugLog("		Cat pattern >> " + catParams);
            ArrayList<String> assigns = new ArrayList<String>();
        	Pattern p = Pattern.compile("([^>]+)\\s*[>>]+\\s*([^\\s]+)");
    		Matcher m = p.matcher(catParams);
    		if(m.find()){
    			debugLog("		Cat pattern 1");
    			/*
    			String objname = STEPNAME + "." + cat;
    	        TObj cmdobj = new TObj(COMMAND, objname, objname, 100, new TLocation(line_count) );
    	        step_obj.add(cmdobj);
				*/
    			TObj cmdObj = createCommand(cat, line_count, step_obj);

            	String param = m.group(1);
            	debugLog("		param >> " + param);
    			//ArrayList<String> result = PatternUtil.getValues(param, "([^\\s]+)", new String[]{"(sed|wc|fold|tr|_EOF_|<<|g|\\|)", "(-[A-Za-z])", "([0-9]+)"});
            	ArrayList<String> result = PatternUtil.getValues(param, "([^\\s]+)", new String[]{"(sed|wc|fold|tr|_EOF_|<<|g|-w|-l|-d)", "(s/[^\\s]+/)", "(\\|)", "'[^\\s]+'"});

    			//, new String[]{"|", "tr" , "-d" , "'\\n'", "<<" , "_EOF_"}
    			//if(check(str, "(sed|wc|fold|tr|_EOF_|<<|g|\\|)") || check(str, "(-[A-Za-z])") || check(str, "([0-9]+)")){
            	assigns.addAll(result);
				assigns.add(m.group(2));
				for(int cnt = 0 ; cnt < assigns.size() ; cnt++){
					/*
	    			add_param(cnt, assigns.get(cnt), cat, cmdobj, step_obj, ht, line_count, objht );
	    			*/
					debugLog("addAll >>>> " + assigns.get(cnt));

					makeAssignDpd(cnt, assigns.get(cnt), cat, cmdObj, line_count, ht, objht, step_obj);

	    		}
    		}
        }

        if(!catParams.isEmpty()){
        	//check1(value, "([^>]+)\\s*[>>]+\\s*([^\\s]+)");
        	Pattern p = Pattern.compile(">([^\\s]+)<<([^\\s]+)");
    		Matcher m = p.matcher(catParams);
    		if(m.find()){
    			debugLog("		Cat pattern 2");
    			/*
    			String objname = STEPNAME + "." + cat;
    	        TObj cmdobj = new TObj(COMMAND, objname, objname, 100, new TLocation(line_count) );
    	        step_obj.add(cmdobj);
				*/
    			TObj cmdObj = createCommand(cat, line_count, step_obj);
    			makeAssignDpd(0, m.group(1), cat, cmdObj, line_count, ht, objht, step_obj);


    			if(!m.group(2).equals("EOF")){
    				makeAssignDpd(1, m.group(2), cat, cmdObj, line_count, ht, objht, step_obj);
    			}
    			/*
            	add_param(0, m.group(1), cat, cmdobj, step_obj, ht, line_count, objht );
            	if(!m.group(2).equals("EOF")){
            		add_param(1, m.group(2), cat, cmdobj, step_obj, ht, line_count, objht );
    			}
    			*/
    		}
        }

        if(!catParams.isEmpty()){
        	//check1(value, "([^>]+)\\s*[>>]+\\s*([^\\s]+)");
        	Pattern p = Pattern.compile("^([^\\s<>]+)$");
    		Matcher m = p.matcher(catParams);
    		if(m.find()){
    			debugLog("		Cat pattern 3");
    			/*
    			String objname = STEPNAME + "." + cat;
    	        TObj cmdobj = new TObj(COMMAND, objname, objname, 100, new TLocation(line_count) );
    	        step_obj.add(cmdobj);
				*/
    			TObj cmdObj = createCommand(cat, line_count, step_obj);
    			makeAssignDpd(0, m.group(1), cat, cmdObj, line_count, ht, objht, step_obj);
    			//add_param(0, m.group(1), cat, cmdobj, step_obj, ht, line_count, objht );

    		}
        }

        //
        //check1(value, "([^>]+)\\s*[>>]+\\s*([^\\s]+)");


        /*
        String fname1 = null;
        String fname2 = null;
        if(line_data.indexOf("<<EOF") != -1) {
            debugLog("	<<EOF start ----");
            Pattern p = Pattern.compile("cat[\\s]+[>]+[\\s]*([^<\\s]+)");
            // i?ine@ aoaO e“aa cMeyeLeπ e≪a?a?.
            Matcher m = p.matcher(line_data);
            if (m.find())
            {
                fname1 = null;
                fname2 = m.group(1);
                debugLog("		matcher found fname2 >> " + m.group(1));

            }else{
                debugLog("	not matched  >> " + "cat[\\s]+[>]+[\\s]*([^<\\s]+)");
            }
        }
        else {
            debugLog("	else start ----");
            Pattern p = Pattern.compile("cat[\\s]+([^>]+)[>]+[\\s]*([^<\\s]+)");
            // i?ine@ aoaO e“aa cMeyeLeπ e≪a?a?.
            Matcher m = p.matcher(line_data);
            if (m.find())
            {
                fname1 = m.group(1);
                fname2 = m.group(2);
                debugLog("		matcher found fname1 : fname2 >> " + m.group(1) + " : " + m.group(2));
            }else{
                debugLog("	not matched  >> " + "cat[\\s]+([^>]+)[>]+[\\s]*([^<\\s]+)");
            }
        }
        if(fname2 != null) {
            String objname = STEPNAME + "." + cat;
            TObj cmdobj = new TObj(COMMAND, objname, objname, 100, new TLocation(line_count) );
            step_obj.add(cmdobj);
            debugLog("add cmdobj to step_obj >> " + objname);
            int paramIndex = -1;
            if(fname1 != null) {
                fname1= fname1.trim();
                String strlist[] = fname1.split("[\\s]+");
                debugLog("	splitted strlist length >> " + strlist.length);
                for ( int j = 0; j < strlist.length; j ++) {
                    //debugLog("[" + j + "] = [" + strlist[j]+"]");
                    debugLog("	splitted strlist[" + j +"] >> " + strlist[j]);

                   //修正前
                   //paramIndex = j;
                   //String data = strlist[j];
                   //add_param(paramIndex, data, cat,cmdobj, step_obj, ht, line_count, objht );

                    String data = strlist[j];
                    if(!checkContinue(data, "cat")){
                    	continue;
                    }

                    paramIndex++;// = j;

                    add_param(paramIndex, data, cat,cmdobj, step_obj, ht, line_count, objht );
                }
            }
            if(paramIndex < 0){
                paramIndex = 0;
            }else{
                paramIndex++;
            }
            add_param(paramIndex, fname2, cat,cmdobj, step_obj, ht, line_count, objht );
        }else{
            debugLog("fname2 is null");
        }
        */

        debugLog("############# End find_cat ###################");
    }

    private void find_sqlplus(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        // sqlplus $ORAUSER @${DBFILE} ${LSOUTPUT}/${LSOUTPUTFILE}
        String sqlplus = "sqlplus";
        if(line_data.indexOf(sqlplus) == -1) return ;
        String newline_data = change_line_data(line_data, ht);
        //log.debug("sqlplus", line_data);

        String sqlplusValue = PatternUtil.check(newline_data, "^[\\s]*[^\\s]*sqlplus\\s+(.+)", 1);

        if(!sqlplusValue.isEmpty()){

        	debugLog("***** sqlplus Start *****");
        	/*
        	String objname = STEPNAME + "." + sqlplus;
            TObj cmdobj = new TObj(COMMAND, objname, objname, 100, new TLocation(line_count) );
            step_obj.add(cmdobj);
			*/
        	TObj cmdObj = createCommand(sqlplus, line_count, step_obj);

        	if(sqlplusValue.contains("EOF")){

        		debugLog("			EOF STR >> " + newline_data);
        		String eofEnd = newline_data.substring(newline_data.indexOf("EOF"));

				debugLog("			EOF >> " + eofEnd);
				get_sql(eofEnd, cmdObj, line_count);

			}

        	String sqlplusParams = PatternUtil.check(sqlplusValue, "(\\s*-[A-Za-z]+\\s+)*([^\\s]+)\\s+(.+)", 3);

        	if(!sqlplusParams.isEmpty()){

        		ArrayList<String> values = new ArrayList<String>();
        		Pattern p = Pattern.compile("([^\\s]+)");
        		Matcher m = p.matcher(sqlplusParams);


        		while(m.find()){

        			for(int i = 0 ; i < m.groupCount() ; i++){
        				String param = m.group(i+1);
        				if(param.startsWith("<") && param.length() > 1){
        					param = param.substring(1);
        				}
        				if(PatternUtil.isMatched(param, new String[]{"(&*[1-9]+)>(&*[1-9]+)", "([1-9]+)>>\\s*([^\\s]+)", "(<)", "(>>)"})){
        					continue;
        				}

        				debugLog("			add param >> " + param);
        				values.add(param);
        			}

        		}

        		for(int cnt = 0 ; cnt < values.size() ; cnt++){
        			String paramValue = values.get(cnt);

        			makeAssignDpd(cnt, paramValue, sqlplus, cmdObj, line_count, ht, objht, step_obj);
        			/*
        			if(paramValue.indexOf('`') != -1) {
        				paramValue = __shell_run_output;
                    }
                    if(paramValue.indexOf("/") != -1) {
                    	paramValue = paramValue.substring(paramValue.lastIndexOf('/')+1);
                    }

                    TDpd dpd = new TDpd(USE_ASSIGN, paramValue, paramValue, 100,  new TLocation(line_count) );
                    cmdobj.add(dpd);
                    TMeta meta = new TMeta(1000, "8211201", values.get(cnt), new TLocation(line_count));
                    dpd.add(meta);

                    debugLog("				dpd add >> " + paramValue);
                    */

        		}

        	}


        	debugLog("***** sqlplus End *****");
        }


        /* 修正前
        String sqlfile = null;
        String dmpfile = null;
        if(line_data.indexOf("@") != -1) {
            ArrayList<Integer> poslist = new ArrayList<Integer>();
            poslist.add(new Integer(1));
            poslist.add(new Integer(2));

            String arglist[] = get_regstr(line_data, "sqlplus.*@([^\\s]+)[\\s]*([^\\s]*)", poslist);
            if(arglist.length > 0) {
                sqlfile =arglist[0];
                if(sqlfile.length() > 1)
                    dmpfile = arglist[1];
            }

        } else if(line_data.indexOf("<<EOF") != -1) {
            sqlfile = "";
        }
        else if(line_data.indexOf("<") != -1) {
            Pattern p = Pattern.compile("sqlplus.*<[\\s]*([^\\s]+)");
            // i?ine@ aoaO e“aa cMeyeLeπ e≪a?a?.
            Matcher m = p.matcher(line_data);
            if (m.find())
            {
                sqlfile = m.group(1);
            }
        }
        if(sqlfile != null) {
            String objname = STEPNAME + "." + sqlplus;
            TObj cmdobj = new TObj(COMMAND, objname, objname, 100, new TLocation(line_count) );
            step_obj.add(cmdobj);
            if(sqlfile.length() > 0) {
                String value = sqlfile;
                if(value.indexOf('`') != -1) {
                    value = __shell_run_output;
                }
                if(value.indexOf("/") != -1) {
                    value = value.substring(value.lastIndexOf('/')+1);
                }
                //log.debug("sqlfile 1", sqlfile);
                TDpd dpd = new TDpd(USE_ASSIGN, value, value, 100,  new TLocation(line_count) );
                cmdobj.add(dpd);
                TMeta meta2 = new TMeta(1000, "8211201", sqlfile, new TLocation(line_count));
                dpd.add(meta2);
                if(dmpfile != null && dmpfile.length() > 0 ) {
                    value = dmpfile;
                    if(value.indexOf('`') != -1) {
                        value = __shell_run_output;
                    }
                    if(value.indexOf("/") != -1) {
                        value = value.substring(value.lastIndexOf('/')+1);
                    }
                    //log.debug("dmpfile ", dmpfile);
                    TDpd dpd2 = new TDpd(USE_ASSIGN, value, value, 100,  new TLocation(line_count) );
                    cmdobj.add(dpd2);
                    TMeta meta3 = new TMeta(1000, "8211201", dmpfile, new TLocation(line_count));
                    dpd2.add(meta3);
                }
            }
            if(line_data.indexOf("<<EOF") != -1) {
                String sql = line_data.substring(line_data.indexOf("<<EOF") + 5);
                get_sql(sql, cmdobj, line_count);
            }
        } */
    }

    private void get_sql(String sqlstr, TObj cmdobj, int line_count) {
        // TRUNCATE  TABLE  F_SKKR;
        Pattern p = Pattern.compile("(TRUNCATE[\\s]+TABLE[\\s]+([^;]+))");
        // i?ine@ aoaO e“aa cMeyeLeπ e≪a?a?.
        Matcher m = p.matcher(sqlstr);
        while (m.find())
        {
            for ( int j = 0; j <= m.groupCount(); j ++) {
                String sql = m.group(1);
                debugLog("				add table >> " + sql);
                TDpd dpd = new TDpd(1009001, sql, sql, 100,  new TLocation(line_count) );
                cmdobj.add(dpd);
            }
        }
    }

    private void find_sh(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        // ${MAIN_SHELL}.sh
        String shell = ".sh";
        if(line_data.indexOf(shell) == -1) return ;
        ////log.debug(".shell", line_data);

        Pattern p = Pattern.compile("(^|[\\s]+)([\\$\\{\\}\\./A-Za-z_\\-0-9]+.sh)");
        // i?ine@ aoaO e“aa cMeyeLeπ e≪a?a?.
        Matcher m = p.matcher(line_data);

        if (m.find())
        {

            String fullname = m.group(2);
            fullname = change_line_data(fullname, ht);
            String value = fullname;
            if(value.lastIndexOf('/') != -1) {
                value = value.substring(value.lastIndexOf('/')+1);
            }

            // a§i?EVEFEaCOEXELEbEv
            for ( int i = 0 ; i < skipshell.length; i ++) {
                if(value.equals(skipshell[i])) {
                    return ; // common shell skip
                }
            }

            String objname = STEPNAME + "." + "sh";
            String newObjName = objname + "(" + value + ")";
            TObj cmdobj = new TObj(COMMAND, objname, newObjName, 100, new TLocation(line_count) );
            step_obj.add(cmdobj);

            ////log.debug("shell  = " ,fullname);
            TDpd dpd = new TDpd(CALL_SHELL, value, value, 100,  new TLocation(line_count) );
            cmdobj.add(dpd);
            fullname = checkStr(fullname);
            TMeta meta2 = new TMeta(1000, "8211201", fullname, new TLocation(line_count));
            dpd.add(meta2);
        }
    }

    private void find_exp(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        //           exp $ORAUSER    tables=SAP_UNITPRICE_MASTER      file=${DD_EXPFL}
        String exp = "exp";
        if(line_data.indexOf(exp) == -1) return ;
        ////log.debug("exp", line_data);

        String targetStr = PatternUtil.check(line_data, "^[\\s]*exp[\\s]+(.+)", 1);

		if(!targetStr.isEmpty()){

			debugLog("***** exp Start *****");
			/*
			String objname = STEPNAME + "." + exp;
            TObj cmdobj = new TObj(COMMAND, objname, objname, 100, new TLocation(line_count) );
            step_obj.add(cmdobj);
            */
			TObj cmdObj =createCommand(exp, line_count, step_obj);

			String tables = PatternUtil.check(targetStr, "(tables|TABLES)=([^\\s]+)", 2);//(tables|TABLES)=([^\\s]+)
			if(!tables.isEmpty()){
				ArrayList<String> tablesParam = PatternUtil.getValues(tables, "([^\\s,]+)");

				for(int i = 0; i < tablesParam.size(); i++){
					String tablename = "select * from " + tablesParam.get(i);
					TDpd dpd = new TDpd(1009001, tablename, tablename, 100,  new TLocation(line_count) );
					cmdObj.add(dpd);
		            debugLog("	exp add Tables >> " + tablename);
				}

			}

			String dd_expfl = PatternUtil.check(targetStr, "(file|FILE)=([^\\s]+)", 2); //(file|FILE)=([^\\s]+)
			if(!dd_expfl.isEmpty()){

				//dd_expfl = change_line_data(dd_expfl, ht);
				debugLog("	 dd_expfl >> " + dd_expfl);
				makeAssignDpd(0, dd_expfl, exp, cmdObj, line_count, ht, objht, step_obj);

				/*
	            String keylist[] = conv_keys(dd_expfl);
	            for ( int i = 0; i < keylist.length; i ++) {
	                add_dd_obj(objht, cmdObj, exp, step_obj, keylist[i]);
	            }
	            */
			}
			debugLog("***** exp End *****");
		}



        /* 修正前
        Pattern p = Pattern.compile(exp + "[\\s]+([^\\s]+)[\\s]+tables[\\s]*=[\\s]*([^\\s]+)[\\s]+file[\\s]*=[\\s]*([^\\s]+)");
        // i?ine@ aoaO e“aa cMeyeLeπ e≪a?a?.
        Matcher m = p.matcher(line_data);
        if (m.find())
        {

        	debugLog(">>>> exp Start >>>>");
        	debugLog(">>>> " + line_data);
            String objname = STEPNAME + "." + exp;
            TObj cmdobj = new TObj(COMMAND, objname, objname, 100, new TLocation(line_count) );
            step_obj.add(cmdobj);

            String tablename = "select * from " + m.group(2);
            ////log.debug("exp table = " ,tablename);
            TDpd dpd = new TDpd(1009001, tablename, tablename, 100,  new TLocation(line_count) );
            cmdobj.add(dpd);


            String DD_EXPFL = m.group(3);
            // ${xxx} -> xxx
            String keylist[] = conv_keys(DD_EXPFL);
            for ( int i = 0; i < keylist.length; i ++) {
                add_dd_obj(objht, cmdobj, exp, step_obj, keylist[i]);
            }
        }
        */
    }

    private void makeAssignDpd(int index, String targetAssign, String cmdStr, TObj cmdObj, int line_count, Hashtable<String, String>  ht, Hashtable<String, TObj> objht, TObj step_obj){


    	if(targetAssign.contains("/")){
    		String assignFileFullStr = change_line_data(targetAssign, ht);
			debugLog("		changeParam >> " + assignFileFullStr);
			String assignFileName = getAssignFileName(assignFileFullStr);
            debugLog("		changeParam2 >> " + assignFileName);

            String dirPath = assignFileFullStr.substring(0, assignFileFullStr.lastIndexOf("/"));

            if(mainShellUtil.checkParams(dirPath)){
            	debugLog("			dirPath has module param " + dirPath);
            	if(assignFileName.isEmpty()){
            		/* 修正前
            		assignFileFullStr = dirPath;
            		*/


            		assignFileFullStr = dirPath;
            		//xxxxx/xxxxx/xxxxx/ のケース　/xxxxx/xxxxx/xxxxx + empty

            	}
            	String indexStr = "$" + (index+1);
            	TObj dd = new TObj(JOB_STEP_DD, cmdObj.getGID() + "." + indexStr, indexStr, 100, new TLocation(line_count) );

            	ArrayList<String> convStrs = checkMainShellConvStrs(assignFileFullStr);

            	for(int cnt = 0 ; cnt < convStrs.size() ; cnt++){
            		String targetStr = convStrs.get(cnt);
            		debugLog("			add convTarget : " + targetStr);
            		ArrayList<TDpd> dpds = checkMainShellParam(targetStr, line_count, dd, cnt);
            		debugLog("			add convTarget dpds : " + dpds.size());
            		for(int i = 0 ; i < dpds.size() ; i++){
            			debugLog("				copy dpds : " + dpds.get(i).getGID());
                		copy_dpd(-1, cmdObj, dpds.get(i));
                	}
            	}

            	objht.put(indexStr, dd);
            	cmdObj.add(dd);

            }else{
            	debugLog("	dirpath is not contains module params ");
           		dd_addUsingIndex(index, assignFileFullStr, cmdObj, ht, line_count, objht);
            }

			debugLog("					 cmdObj sub obj and dpd - " + cmdObj.getGID());
			showTObjTree(cmdObj, 0, true);
		}else{
			//1 change line using ht
			String assignFileFullStr = change_line_data(targetAssign, ht);
			debugLog("addAll >>>> 4");
			debugLog("		change 1 >> " + assignFileFullStr);
			//2 change line check objht
			String paramKey = PatternUtil.check(targetAssign, "\\$[\\{]*([^\\s\\}]+)[\\}]*", 1);
			if(!paramKey.isEmpty()){
				debugLog("		check Objht >> " + paramKey);
				if(objht.containsKey(paramKey)){
					debugLog("			has Objht >> " + paramKey + "  value(GID) >> " + objht.get(paramKey).getGID());
					dd_addObj(paramKey, cmdStr, cmdObj, objht, step_obj);
					checkMainShellParam(assignFileFullStr, line_count, cmdObj, -1);
				}else{
					debugLog("			has no Objht >> " + paramKey);
					dd_add(paramKey, cmdObj, ht, line_count, objht);
					dd_addObj(paramKey, cmdStr, cmdObj, objht, step_obj);
					ArrayList<TDpd> dpds = checkMainShellParam(assignFileFullStr, line_count, cmdObj, -1);
					for(int a = 0 ; a < dpds.size() ; a++){
	                    debugLog("					 added dpds	" + dpds.get(a).getGID());
	                }
					debugLog("					 cmdObj sub obj and dpd - " + cmdObj.getGID());
					showTObjTree(cmdObj, 0, true);
				}

			}else{
				debugLog("		target is filePath");
				dd_addUsingIndex(index, assignFileFullStr, cmdObj, ht, line_count, objht);
				showTObjTree(cmdObj, 0, true);
			}
		}

    }

    private void find_imp(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        //           exp $ORAUSER    tables=SAP_UNITPRICE_MASTER      file=${DD_EXPFL}
        String imp = "imp";
        if(line_data.indexOf(imp) == -1) return ;
        ////log.debug("exp", line_data);

        String targetStr = PatternUtil.check(line_data, "^[\\s]*imp[\\s]+(.+)", 1);

		if(!targetStr.isEmpty()){

			debugLog("***** imp Start *****");
			/*
			String objname = STEPNAME + "." + imp;
            TObj cmdobj = new TObj(COMMAND, objname, objname, 100, new TLocation(line_count) );
            step_obj.add(cmdobj);
			*/
			TObj cmdObj = createCommand(imp, line_count, step_obj);

			String tables = PatternUtil.check(targetStr, "(tables|TABLES)=([^\\s]+)", 2);//(tables|TABLES)=([^\\s]+)
			if(!tables.isEmpty()){
				ArrayList<String> tablesParam = PatternUtil.getValues(tables, "([^\\s,]+)");

				for(int i = 0; i < tablesParam.size(); i++){
					String tablename = "select * from " + tablesParam.get(i);
					TDpd dpd = new TDpd(1009001, tablename, tablename, 100,  new TLocation(line_count) );
					cmdObj.add(dpd);
		            debugLog("	imp add Tables >> " + tablename);
				}

			}

			String dd_expfl = PatternUtil.check(targetStr, "(file|FILE)=([^\\s]+)", 2); //(file|FILE)=([^\\s]+)
			if(!dd_expfl.isEmpty()){

				makeAssignDpd(0, dd_expfl, imp, cmdObj, line_count, ht, objht, step_obj);
	            /*
				String keylist[] = conv_keys(dd_expfl);
	            for ( int i = 0; i < keylist.length; i ++) {
	                add_dd_obj(objht, cmdobj, imp, step_obj, keylist[i]);
	            }
	            */
			}

			debugLog("***** imp End *****");
		}

    }

    private void find_expdp(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        //           exp $ORAUSER    tables=SAP_UNITPRICE_MASTER      file=${DD_EXPFL}
        String expdp = "expdp";
        if(line_data.indexOf(expdp) == -1) return ;
        ////log.debug("exp", line_data);
        String targetStr = PatternUtil.check(line_data, "^[\\s]*expdp[\\s]+(.+)", 1);

		if(!targetStr.isEmpty()){

			debugLog("***** expdp Start *****");
			/*
			String objname = STEPNAME + "." + expdp;
            TObj cmdobj = new TObj(COMMAND, objname, objname, 100, new TLocation(line_count) );
            step_obj.add(cmdobj);
			*/
			TObj cmdObj = createCommand(expdp, line_count, step_obj );

			String tables = PatternUtil.check(targetStr, "(tables|TABLES)=([^\\s]+)", 2);//(tables|TABLES)=([^\\s]+)
			if(!tables.isEmpty()){
				ArrayList<String> tablesParam = PatternUtil.getValues(tables, "([^\\s,]+)");

				for(int i = 0; i < tablesParam.size(); i++){
					String tablename = "select * from " + tablesParam.get(i);
					TDpd dpd = new TDpd(1009001, tablename, tablename, 100,  new TLocation(line_count) );
		            cmdObj.add(dpd);
		            debugLog("	imp add Tables >> " + tablename);
				}
			}

			String dumpFile = PatternUtil.check(targetStr, "(dumpfile|DUMPFILE)=([^\\s]+)", 2); //(file|FILE)=([^\\s]+)
			if(!dumpFile.isEmpty()){


				makeAssignDpd(0, dumpFile, expdp, cmdObj, line_count, ht, objht, step_obj);
				/*
	            String keylist[] = conv_keys(dumpFile);
	            for ( int i = 0; i < keylist.length; i ++) {
	                add_dd_obj(objht, cmdobj, expdp, step_obj, keylist[i]);
	            }
				*/
			}

			String paramFile = PatternUtil.check(targetStr, "(parfile|PARFILE)=([^\\s]+)", 2); //(file|FILE)=([^\\s]+)
			if(!paramFile.isEmpty()){

				makeAssignDpd(0, paramFile, expdp, cmdObj, line_count, ht, objht, step_obj);
				/*
	            String keylist[] = conv_keys(paramFile);
	            for ( int i = 0; i < keylist.length; i ++) {
	                add_dd_obj(objht, cmdobj, expdp, step_obj, keylist[i]);
	            }
				*/

			}

			debugLog("***** expdp End *****");
		}

    }

    private void find_impdp(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        //           exp $ORAUSER    tables=SAP_UNITPRICE_MASTER      file=${DD_EXPFL}
        String impdp = "impdp";
        if(line_data.indexOf(impdp) == -1) return ;
        ////log.debug("exp", line_data);
        String targetStr = PatternUtil.check(line_data, "^[\\s]*impdp[\\s]+(.+)", 1);

		if(!targetStr.isEmpty()){

			debugLog("***** impdp Start *****");
			/*
			String objname = STEPNAME + "." + impdp;
            TObj cmdobj = new TObj(COMMAND, objname, objname, 100, new TLocation(line_count) );
            step_obj.add(cmdobj);
			*/
			TObj cmdObj = createCommand(impdp, line_count, step_obj);

			String tables = PatternUtil.check(targetStr, "(tables|TABLES)=([^\\s]+)", 2);//(tables|TABLES)=([^\\s]+)
			if(!tables.isEmpty()){
				ArrayList<String> tablesParam = PatternUtil.getValues(tables, "([^\\s,]+)");

				for(int i = 0; i < tablesParam.size(); i++){
					String tablename = "select * from " + tablesParam.get(i);
					TDpd dpd = new TDpd(1009001, tablename, tablename, 100,  new TLocation(line_count) );
		            cmdObj.add(dpd);
		            debugLog("	impdp add Tables >> " + tablename);
				}
			}

			String dumpFile = PatternUtil.check(targetStr, "(dumpfile|DUMPFILE)=([^\\s]+)", 2); //(file|FILE)=([^\\s]+)
			if(!dumpFile.isEmpty()){
				/*
	            String keylist[] = conv_keys(dumpFile);
	            for ( int i = 0; i < keylist.length; i ++) {
	                add_dd_obj(objht, cmdobj, impdp, step_obj, keylist[i]);
	            }
	            */
				makeAssignDpd(0, dumpFile, impdp, cmdObj, line_count, ht, objht, step_obj);

			}

			String paramFile = PatternUtil.check(targetStr, "(parfile|PARFILE)=([^\\s]+)", 2); //(file|FILE)=([^\\s]+)
			if(!paramFile.isEmpty()){
				/*
	            String keylist[] = conv_keys(paramFile);
	            for ( int i = 0; i < keylist.length; i ++) {
	                add_dd_obj(objht, cmdobj, impdp, step_obj, keylist[i]);
	            }
	            */
				makeAssignDpd(0, paramFile, impdp, cmdObj, line_count, ht, objht, step_obj);

			}

			debugLog("***** impdp End *****");
		}

    }

    private void find_timercmd(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        //       RunSort "${DD_SORTPARM}" 1>>$tempout  2>>$temperr
        String timercmd = "TIMERCMD";
        if(line_data.indexOf(timercmd) == -1) return ;
        //line_data = change_line_data(line_data, ht);
        Pattern p = Pattern.compile("\\$\\{*TIMERCMD\\}*[\\s]+([^\\s]+)");
        // i?ine@ aoaO e“aa cMeyeLeπ e≪a?a?.
        Matcher m = p.matcher(line_data);

        debugLog("cmd >> "  +timercmd);
        if (m.find())
        {
            String fullname = m.group(1);
            String value = fullname;
            if(fullname.indexOf("/") != -1) {
                value = fullname.substring(fullname.lastIndexOf('/')+1);
            }

            value =  change_line_data(value, ht);
            /* TIMERCMD 対応 : TIMERCMDに実際のモジュール名を表示 */
            //String objname = STEPNAME + "." + timercmd;
            String objname = STEPNAME + "." + timercmd + "(" + value + ")";
            debugLog("objName >> " + objname);
            TObj cmdobj = new TObj(COMMAND, objname, objname, 100, new TLocation(line_count) );
            step_obj.add(cmdobj);

            for ( int i = 0 ; i < paramlist.length; i ++) {
                add_dd_obj(objht, cmdobj, timercmd, step_obj, paramlist[i]);
            }
            //add_alldd_obj(objht, cmdobj, timercmd, step_obj);



            TDpd dpd = new TDpd(USE_COMMAND, value, value, 100,  new TLocation(line_count) );
            cmdobj.add(dpd);
            fullname = checkStr(fullname);
            TMeta meta2 = new TMeta(1000, "8211201", fullname, new TLocation(line_count));
            dpd.add(meta2);
        }
    }

    private void find_runsort(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        //       RunSort "${DD_SORTPARM}" 1>>$tempout  2>>$temperr
        String RunSort = "RunSort";
        if(line_data.indexOf(RunSort) == -1) return ;
        line_data = change_line_data(line_data, ht);
        Pattern p = Pattern.compile(RunSort + "[\\s]+([^\\s]+)");

        Matcher m = p.matcher(line_data);
        if (m.find())
        {
            debugLog("***** find_runsort Start **********************************");
            /* 変数に "" で囲まれたケースがあるため */
            String fullname = m.group(1);
            fullname = fullname.replace("\"", "") + ".sh";
            String value = fullname;

            debugLog("	fullname >> " + fullname + " : value >> " + value);


            //String value = fullname;
            if(fullname.indexOf("/") != -1) {
                value = fullname.substring(fullname.lastIndexOf('/')+1);
            }

            ////log.debug("find_runsort shell = " ,fullname);
            String objname = STEPNAME + "." + RunSort;
            String newObjName = objname + "(" + value + ")";
            TObj cmdobj = new TObj(COMMAND, objname, newObjName, 100, new TLocation(line_count) );
            step_obj.add(cmdobj);

            add_dd_obj(objht, cmdobj, RunSort, step_obj, "DD_SORTIN");
            add_dd_obj(objht, cmdobj, RunSort, step_obj, "DD_SORTOUT");

            TDpd dpd = new TDpd(RUN_SORT, value, value, 100,  new TLocation(line_count) );
            cmdobj.add(dpd);
            fullname = checkStr(fullname);
            TMeta meta2 = new TMeta(1000, "8211201", fullname, new TLocation(line_count));
            dpd.add(meta2);

            showTObjTree(cmdobj, 0, true);
            debugLog("***** find_runsort end ************************************");
        }
    }
    private boolean check_paramlist(String key) {
        for ( int i = 0; i < paramlist.length; i ++) {
            if(paramlist[i].equals(key))
                return true;
        }
        return false;
    }

    private void find_runcobol(String line_data, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
        //  RunCobol "${JCLNAME}" "S020" "DE310805" "NULL" 1>>$tempout  2>>$temperr
        String RunCobol = "RunCobol";
        if(line_data.indexOf(RunCobol) == -1) return ;

        Pattern p = Pattern.compile(RunCobol + "[\\s]+([^\\s]+)[\\s]+([^\\s]+)[\\s]+\"([^\"]+)");
        // i?ine@ aoaO e“aa cMeyeLeπ e≪a?a?.
        Matcher m = p.matcher(line_data);
        if (m.find())
        {

            String cobol = m.group(3);
            if(cobol.indexOf(' ') != -1)
                cobol = cobol.substring(0, cobol.indexOf(' '));
            ////log.debug("find_runcobol cobol = " ,cobol);
            debugLog("			*****	COBOL	CHECK	Start	*****");
            debugLog("			cobol matched Str >> " + cobol);
            String cobolGid = cobol;
            String targetCobol = cobol.toLowerCase();
            if(cobolFileList.containsKey(targetCobol)){
                cobolGid = cobolFileList.get(targetCobol);
                debugLog("				Contains cobolFileList key : " + targetCobol + "  value : " + cobolGid);
            }

            debugLog("				Add CALL_PGM : GID >> " +  cobolGid);
            debugLog("				Add CALL_PGM : OBJ >> " +  cobol);
            TDpd dpd = new TDpd(CALL_PGM, cobolGid, cobol, 100,  new TLocation(line_count) );

            String objname = STEPNAME + "." + RunCobol;
            String newObjName = objname+"("+ cobol + ")";
            TObj cmdobj = new TObj(COMMAND, objname, newObjName, 100, new TLocation(line_count) );
            step_obj.add(cmdobj);

            cmdobj.add(dpd);

            debugLog("			*****	COBOL	CHECK	End	*****");
            /* 2015.10.02 #4 a?eEe?au */

            Iterator<String> iterator = objht.keySet().iterator();

            while(iterator.hasNext()) {
                String key = iterator.next();
                if(!key.startsWith("DD_") && !check_paramlist(key))
                    continue ; // DD_ am ebe? afa? a‰eae@a∂ i~au eoiA , DD_ e?i￢ a? i~au

                TObj obj = objht.get(key);
                cmdobj.add(obj);
                String ddname = key;
                if(ddname.indexOf("_") != -1)
                    ddname = ddname.substring(ddname.indexOf("_") + 1);
                obj.setName(ddname);
                String gid = step_obj.getName() + "." + RunCobol + "." +  ddname;
                obj.setGID(gid);
                String datname = cobol + "." + ddname;
                TDpd dddpd = new TDpd(USE_DD, datname, ddname, 100,  obj.getTLocation() );

                TMeta ddmeta = new TMeta(1000, "1311569", cobol, obj.getTLocation());
                dddpd.add(ddmeta);
                cmdobj.add(dddpd);
                TDpd dlist[] = obj.getTDpdList();
                for ( int j = 0; j < dlist.length; j ++) {

                    dlist[j].setType(USE_DSN);
                    TMeta meta = new TMeta(1000, "1311569", cobol, new TLocation(line_count));
                    dlist[j].add(meta);
                    TMeta meta2 = new TMeta(1000, "1311567", ddname, new TLocation(line_count));
                    dlist[j].add(meta2);

                    String dddetail = datname + "." + dlist[j].getName();
                    TDpd newddd = new TDpd(DD_DETAIL, dddetail, dddetail, 100,  dlist[j].getTLocation() );
                    TMeta meta5 = new TMeta(1000, "1311568",  new String(dlist[j].getName()),  new TLocation(dlist[j].getTLocation().getStartLine()) );
                    newddd.add(meta5);
                    cmdobj.add(newddd);
                    //}
                    copy_dpd(-1, cmdobj, dlist[j]);
                }
            }
            debugLog("		runcobol	cmdObj - " + cmdobj.getGID());
            showTObjTree(cmdobj, 0, true);
        }
    }
    private void copy_dpd(int dpd_type_id, TObj obj, TDpd dpd) {
        if (dpd_type_id == -1)
            dpd_type_id = dpd.getType();
        TDpd newdpd = new TDpd(dpd_type_id, dpd.getGID(), dpd.getName(), 100,  dpd.getTLocation() );

        TMeta m[] = dpd.getTMetaList();
        for (int i = 0; i < m.length; i ++) {
            TMeta meta = new TMeta(m[i].getType(), m[i].getName(), m[i].getValue() , m[i].getTLocation() );
            newdpd.add(meta);
        }
        obj.add(newdpd);
    }

    private void add_dd_obj(Hashtable<String, TObj> objht, TObj cmdobj, String cmdname , TObj step_obj, String key) {
        add_dd_obj(-1, objht, cmdobj, cmdname,step_obj, key);
    }
    private void add_dd_obj(int dpd_type_id, Hashtable<String, TObj> objht, TObj cmdobj, String cmdname , TObj step_obj, String key) {
        TObj obj = objht.get(key);
        if(obj == null) {
            debugLog("not found key " + key);
            return ;
        }
        String ddname = obj.getName();
        String gid = step_obj.getName() + "." + cmdname + "." +  ddname;
        debugLog("change dd name "  + "DD fullname before="+obj.getGID() + ",after="+gid);
        obj.setGID(gid);
        TDpd dpdlist[] = obj.getTDpdList();

        for ( int i = 0; i < dpdlist.length; i ++) {
            copy_dpd(dpd_type_id, cmdobj, dpdlist[i]);
        }
        cmdobj.add(obj);
    }

    private void dd_add(String keystr, TObj step_obj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
    	if(step_obj == null) { return; }

    	TObj dd  = null;

    	String ddname = keystr.substring(keystr.indexOf("_")+1);
        if(objht.containsKey(keystr)) {
            dd = objht.get(keystr);
        } else {
            dd = new TObj(JOB_STEP_DD, step_obj.getGID() + "." + keystr, keystr, 100, new TLocation(line_count) );
            objht.put(keystr, dd);
            debugLog("																put dd key >> " + keystr);
            debugLog("																put dd value >> " + dd.getGID());
        }
        String assignFileFullStr = ht.get(keystr);
        if(assignFileFullStr != null){
        	debugLog("																dd fullNamePath >> " + assignFileFullStr);
        	checkMainShellParam(assignFileFullStr, line_count, dd, -1);
        }else{
        	//変数が定義されてない場合
        	ht.put(keystr, "${"+ keystr +"}");
        	debugLog("																dd put >> ( " +  keystr + " , " + "${"+ keystr +"}" + " )");
        	dd_add(keystr, step_obj, ht, line_count, objht);
        	return;
        }
    }

    private void dd_addUsingIndex(int index, String assignPath, TObj cmdObj, Hashtable<String, String>  ht, int line_count, Hashtable<String, TObj> objht) {
    	if(cmdObj == null) { return; }
    	debugLog("				dd_addUsingIndex index : " + index);
    	debugLog("				dd_addUsingIndex path : " + assignPath);
    	debugLog("				dd_addUsingIndex cmdObj : " + cmdObj.getGID());

    	String indexStr = "$"+(index+1);
    	TObj dd  = new TObj(JOB_STEP_DD, cmdObj.getGID() + "." + indexStr, indexStr, 100, new TLocation(line_count));
    	objht.put(indexStr, dd);
    	ArrayList<TDpd> dpds = checkMainShellParam(assignPath, line_count, dd, -1);
    	for(int i = 0 ; i < dpds.size() ; i++){
    		copy_dpd(-1, cmdObj, dpds.get(i));
    	}
    	cmdObj.add(dd);
    }

    private String getAssignFileName(String assignFileFullStr){

    	String result = assignFileFullStr;
    	if(assignFileFullStr.startsWith("\"")) {
    		assignFileFullStr = assignFileFullStr.substring(1);
        }
        if(assignFileFullStr.endsWith("\"")) {
        	assignFileFullStr = assignFileFullStr.substring(0, assignFileFullStr.length() - 1);
        }
        result = assignFileFullStr;
        if(result.indexOf('`') != -1) {
        	debugLog("<SHELL_OUTPUT>  " + result);
        	result = __shell_run_output;
        }
        if(result.indexOf("/") != -1) {
        	String tempStr = result.substring(result.lastIndexOf('/')+1);
        	if(tempStr.isEmpty()){
        		String tempStrDir = result.substring(0, result.lastIndexOf('/'));
        		String tempStrDir2 = getAssignFileName(tempStrDir);
        		debugLog("tempStrDri >> " + tempStrDir);
        		debugLog("tempStrDri2 >> " + tempStrDir2);
        	}else{
        		result = tempStr;
        	}

        }
    	return result;
    }

    private String getAssignDirName(String assignFileFullStr){

    	if(assignFileFullStr.indexOf("/") == -1){
    		return "";
    	}

    	String result = assignFileFullStr;
    	if(assignFileFullStr.startsWith("\"")) {
    		assignFileFullStr = assignFileFullStr.substring(1);
        }
        if(assignFileFullStr.endsWith("\"")) {
        	assignFileFullStr = assignFileFullStr.substring(0, assignFileFullStr.length() - 1);
        }
        result = assignFileFullStr;
        if(result.indexOf('`') != -1) {
        	debugLog("<SHELL_OUTPUT> DIR  " + result);
        	result = __shell_run_output;
        }
        if(result.indexOf("/") != -1) {
        	result = result.substring(0, result.lastIndexOf('/'));
        }
    	return result;
    }

    private ArrayList<TDpd> checkMainShellParam(String assignStrFull, int line_count, TObj ddObj, int currentIndex){
    //private ArrayList<TDpd> checkMainShellParam(String assignStrFull, int line_count, TObj ddObj){

    	debugLog("Start checkMainShellParam *********************************************");

    	TDpd assignDpd = null;
    	ArrayList<TDpd> assignDpds = new ArrayList<TDpd>();
		if(mainShellUtil.checkParams(assignStrFull)){

			boolean hasMainShell = false;
            boolean hasSubShell = false;
            debugLog("Target Params Exist ****");
            debugLog("	GetShellMeta Type == 1 or 2 ****");
            ArrayList<MainShellInfo> metaList = mainShellUtil.getMainShellInfos(currentShellFile);
            for(int ii = 0 ; ii < metaList.size() ; ii++){
                MainShellInfo valuei = metaList.get(ii);
                String newValue = mainShellUtil.getStringConvMeta(valuei, assignStrFull);
                String fileName = getAssignFileName(newValue);
            	fileName = getDsnName(fileName, assignStrFull);
            	newValue = checkStr(newValue);

                assignDpd = new TDpd(USE_ASSIGN, fileName, fileName, 100,  new TLocation(line_count) );
                TMeta meta2 = new TMeta(1000, "8211201", newValue, new TLocation(line_count));
                if(!timingStr.isEmpty()){
                	TMeta timingMeta = new TMeta(1000, "8212307", timingStr, new TLocation(line_count));
                	assignDpd.add(timingMeta);
                }
                assignDpd.add(meta2);
                ddObj.add(assignDpd);
                assignDpds.add(assignDpd);




                debugLog("	MainShell[" + valuei.getShellName()+"] Call >>>>>>> " + currentShellFile);
                debugLog("		add USE_ASSIGN conv newValue >> " + newValue);
                debugLog("		add USE_ASSIGN conv fineName >> " + fileName);
                hasMainShell = true;
            }

            //Sub ShellCAeIca
            if(SHELL_TYPE == 3){
                debugLog("GetShellMeta Type == 3 ****");
                ArrayList<String> subShells = subShellUtil.getSubList(currentShellFile);
                for(int i = 0 ; i < subShells.size() ; i++){
                    String subShell = subShells.get(i);
                    ArrayList<MainShellInfo> mainShells = mainShellUtil.getMainShellInfos(subShell);
                    for(int ii = 0 ; ii < mainShells.size() ; ii++){
                        MainShellInfo valuei = mainShells.get(ii);
                        String newValue = mainShellUtil.getStringConvMeta(valuei, assignStrFull);
                        //newValue >> to get filename
                        String fileName = getAssignFileName(newValue);
                    	fileName = getDsnName(fileName, assignStrFull);
                    	newValue = checkStr(newValue);

                        assignDpd = new TDpd(USE_ASSIGN, fileName, fileName, 100,  new TLocation(line_count) );
                        TMeta meta2 = new TMeta(1000, "8211201", newValue, new TLocation(line_count));
                        if(!timingStr.isEmpty()){
                        	TMeta timingMeta = new TMeta(1000, "8212307", timingStr, new TLocation(line_count));
                        	assignDpd.add(timingMeta);
                        }
                        assignDpd.add(meta2);
                        ddObj.add(assignDpd);
                        assignDpds.add(assignDpd);
                        debugLog("	MainShell[" + valuei.getShellName()+"]  >>>>>>> SubShell[" + subShell + "] >>>>>>> Core|SubShell[" + valuei.getShellName() + "]");
                        debugLog("		add USE_ASSIGN conv newValue >> " + newValue);
                        debugLog("		add USE_ASSIGN conv fineName >> " + fileName);
                        hasSubShell = true;
                    }
                }
            }
            if(!hasMainShell && !hasSubShell){
            	String fileName = getAssignFileName(assignStrFull);
        		fileName = getDsnName(fileName, assignStrFull);
        		assignStrFull = checkStr(assignStrFull);

            	assignDpd = new TDpd(USE_ASSIGN, fileName, fileName, 100,  new TLocation(line_count) );
                TMeta meta2 = new TMeta(1000, "8211201", assignStrFull, new TLocation(line_count));
                if(!timingStr.isEmpty()){
                	TMeta timingMeta = new TMeta(1000, "8212307", timingStr, new TLocation(line_count));
                	assignDpd.add(timingMeta);
                }
                assignDpd.add(meta2);
                ddObj.add(assignDpd);
                assignDpds.add(assignDpd);
                debugLog("	has no mainShell and subShell  == 0 ****");
                debugLog("	add USE_ASSIGN >> " + assignStrFull);
                debugLog("	add USE_ASSIGN conv fineName >> " + fileName);
            }
        }else{
        	String fileName = getAssignFileName(assignStrFull);
        	fileName = getDsnName(fileName, assignStrFull);

    		assignStrFull = checkStr(assignStrFull);

    		assignDpd = new TDpd(USE_ASSIGN, fileName, fileName, 100,  new TLocation(line_count) );
            TMeta meta2 = new TMeta(1000, "8211201", assignStrFull, new TLocation(line_count));
            if(!timingStr.isEmpty()){
            	TMeta timingMeta = new TMeta(1000, "8212307", timingStr, new TLocation(line_count));
            	assignDpd.add(timingMeta);
            }
            assignDpd.add(meta2);
            ddObj.add(assignDpd);
            assignDpds.add(assignDpd);
            debugLog("	Target Params is not exist ****");
            debugLog("	add USE_ASSIGN >> " + assignStrFull);
            debugLog("	add USE_ASSIGN filename >> " + fileName);
        }
		return assignDpds;
    }

    private String getDsnName(String filaName, String assignFull){

    	String result = filaName;
    	String tempFileName = checkFileName(filaName, assignFull);
    	if(!tempFileName.isEmpty()){
    		result = tempFileName;
    	}
    	if(assignFull.endsWith("/")){
    		String tempStrDir = assignFull.substring(0, assignFull.lastIndexOf('/'));
    		String dirName = getAssignFileName(tempStrDir);
    		result = dirName + "/";
    	}

    	return result;
    }

    private String checkStr(String str){
    	String result = str;
    	if(str.startsWith("\"") && str.endsWith("\"")){
    		result = str.substring(1, str.lastIndexOf("\""));
    	}

    	return result;
    }

    private String checkFileName(String fileName, String assignfullName){
    	String result = "";

    	String dir = getAssignDirName(assignfullName);

    	debugLog("CHECK FILE >> " + dir);
    	if((fileName.equals(".") || fileName.equals("*")) && !dir.isEmpty()){
    		dir = getAssignFileName(dir);
    		dir += "/" + fileName;
    		result = dir;
    	}


    	return result;
    }

    private ArrayList<String> checkMainShellConvStrs(String assignStrFull){

    	debugLog("Start checkMainShellConvStrs *********************************************");

    	ArrayList<String> assignStrs = new ArrayList<String>();
		if(mainShellUtil.checkParams(assignStrFull)){

			boolean hasMainShell = false;
            boolean hasSubShell = false;
            debugLog("Target Params Exist ****");
            //Main Shell Check
            debugLog("	GetShellMeta Type == 1 or 2 ****");
            ArrayList<MainShellInfo> metaList = mainShellUtil.getMainShellInfos(currentShellFile);
            for(int ii = 0 ; ii < metaList.size() ; ii++){
                MainShellInfo valuei = metaList.get(ii);
                debugLog("     " + valuei.shellName);
                String newValue = mainShellUtil.getStringConvMeta(valuei, assignStrFull);
                assignStrs.add(newValue);
                debugLog("     " + newValue);
                hasMainShell = true;
            }

            //Sub ShellCAeIca
            if(SHELL_TYPE == 3){
                debugLog("GetShellMeta Type == 3 ****");
                ArrayList<String> subShells = subShellUtil.getSubList(currentShellFile);
                for(int i = 0 ; i < subShells.size() ; i++){
                    String subShell = subShells.get(i);
                    ArrayList<MainShellInfo> mainShells = mainShellUtil.getMainShellInfos(subShell);
                    for(int ii = 0 ; ii < mainShells.size() ; ii++){
                        MainShellInfo valuei = mainShells.get(ii);
                        String newValue = mainShellUtil.getStringConvMeta(valuei, assignStrFull);
                        assignStrs.add(newValue);
                        hasSubShell = true;
                    }
                }
            }
            if(!hasMainShell && !hasSubShell){
            	String fileName = getAssignFileName(assignStrFull);
            	assignStrs.add(assignStrFull);
                debugLog("	has no mainShell and subShell  == 0 ****");
                debugLog("	add USE_ASSIGN >> " + assignStrFull);
                debugLog("	add USE_ASSIGN conv fineName >> " + fileName);
            }

        }else{
        	String fileName = getAssignFileName(assignStrFull);
        	assignStrs.add(assignStrFull);
            debugLog("	Target Params is not exist ****");
            debugLog("	add USE_ASSIGN >> " + assignStrFull);
            debugLog("	add USE_ASSIGN filename >> " + fileName);
        }
		return assignStrs;
    }

    private String change_line_data(String line_data, Hashtable<String, String>  ht) {
        char[] cList = line_data.toCharArray();
        String str = "";
        for (int k = 0; k < cList.length; k++) {
            if(k+2 < cList.length &&  (cList[k] == '$' && cList[k+1] == '{' ) )  {
                String key = "";
                int endpos = -1;
                for (int j = k+2; j < cList.length; j++) {
                    if(cList[j] == '}') {
                        endpos = j;
                        break;
                    }
                    key += cList[j];
                }
                if(ht.containsKey(key)) {
                    String value = ht.get(key);
                    str += value;
                } else {
                    if(endpos == -1)
                        endpos = cList.length - 1;
                    for (int x = k; x <= endpos; x++) {
                        str += cList[x];
                    }
                }
                k = endpos;
            } else {
                str += cList[k];
            }
        }
        return str;
    }
    public boolean find_end_step(String line_data, int line_count, TObj obj_root, Hashtable<String, String> ht) {
        if(line_data.indexOf("ENDLOG") == -1) return false;
        line_data = change_line_data(line_data, ht);
        Pattern p = Pattern.compile("ENDLOG[\\s]+([^\\s]+)[\\s]+([^\\s]+)[\\s]+\"([^\"]+)");
        Matcher m = p.matcher(line_data);
        if (m.find())
        {
            STEPNAME = JCLNAME;
            return true;
        }
        return false;
    }

    public TObj find_block_start(String line_data, int line_count, TObj job_obj, TObj obj_root, Hashtable<String, String> ht) {
        //  if [ "${COND_CODE}" -eq 0 ]
        if(line_data.indexOf("if") == -1) return null;

        line_data = change_line_data(line_data, ht);
        Pattern p = Pattern.compile("if[\\s]+ ");
        Matcher m = p.matcher(line_data);

        if (m.find())
        {
            STEPNAME = JCLNAME + "." + m.group(3);
            TObj obj = new TObj(JOB_STEP, STEPNAME, STEPNAME, 100, new TLocation(line_count) );
            if(job_obj == null)
                obj_root.add(obj);
            else
                job_obj.add(obj);
            return obj;
        }
        return null;
    }

    public TObj find_start_step(String line_data, int line_count, TObj job_obj, TObj obj_root, Hashtable<String, String> ht) {
        //  STRTLOG ${NOTIFY} DN4B111M "S00A"
        if(line_data.indexOf("STRTLOG") == -1) return null;

        line_data = change_line_data(line_data, ht);
        Pattern p = Pattern.compile("STRTLOG[\\s]+([^\\s]+)[\\s]+([^\\s]+)[\\s]+\"([^\"]+)");
        Matcher m = p.matcher(line_data);
        if (m.find())
        {
            STEPNAME = JCLNAME + "." + m.group(3);
            TObj obj = new TObj(JOB_STEP, STEPNAME, STEPNAME, 100, new TLocation(line_count) );
            if(job_obj == null)
                obj_root.add(obj);
            else
                job_obj.add(obj);
            return obj;
        }
        return null;
    }

    private String find_assing_value(String line_data, Hashtable<String, String> ht) {
        //Pattern p = Pattern.compile("(^[\\s]*|[\\s]+)([a-zA-Z0-9_]+)[\\s]*=[\\s]*(.*)");
    	//Pattern p = Pattern.compile("(^[\\s]*|[\\s]+)([a-zA-Z0-9_]+)[\\s]*=[\\s]*([^\\s]+)");
    	Pattern p = Pattern.compile("(^[\\s]*|[\\s]+)([a-zA-Z0-9_]+)=([^\\s]+)");
        Matcher m = p.matcher(line_data);
        if (m.find())
        {
            String key = m.group(2);
            String value = m.group(3);
            value = change_line_data(value, ht);

            //debugLog("<PRE_SHELL_OUTPUT_START> key : " + key + " , value : " + value);

            if(value.contains("#") || value.contains("%")){
            	ArrayList<String> params = PatternUtil.getValues(value, "(\\$\\{[^\\s\\}]+\\})");
                for(int cnt = 0; cnt < params.size(); cnt++){
            		String paramStr = params.get(cnt);
            		if(paramStr.contains("#") || paramStr.contains("%")){
            			debugLog("Param has  # or #");
            			debugLog("Param : " + paramStr);
            			String convParam = PatternUtil.check(paramStr, "\\$\\{([^\\s\\}]+)\\}", 1);
            			if(!convParam.isEmpty()){
            				String fParam = PatternUtil.check(convParam, "([^¥¥s#%]+)([^¥¥s]+)", 1);
            				fParam = "${" + fParam + "}";
            				String convParamcng = change_line_data(fParam, ht);
            				debugLog("Param conv : " + convParamcng);
            				String bParam = PatternUtil.check(convParam, "([^¥¥s#%]+)([^¥¥s]+)", 2);
            				debugLog("Param f : " + fParam + " bParam : " + bParam);
            				debugLog("Type >>> " + PatternUtil.check(bParam, "([#%]+)([^¥¥s]+)", 1));
            				debugLog("Patter >>> " + PatternUtil.check(bParam, "([#%]+)([^¥¥s]+)", 2));
            				String type = PatternUtil.check(bParam, "([#%]+)([^¥¥s]+)", 1);
            				String pattern = PatternUtil.check(bParam, "([#%]+)([^¥¥s]+)", 2);
            				String rexStr = "";
            				String targetStr = "";
            				rexStr = pattern.replace("*", "");

            				boolean isPattern = (rexStr.contains("[") || rexStr.contains("]")) ? true : false;
            				debugLog("isPattern >> " + isPattern);
            				if(isPattern) {
            					value = value.replace(paramStr, convParamcng);
            					continue;
            				}

            				if(type.equals("##")){
        						debugLog("pattern ##  , rex : " + rexStr);
        						int len = rexStr.length();
        						targetStr = convParamcng.substring(convParamcng.lastIndexOf(rexStr)+len);
        						debugLog(targetStr);

        					}else if(type.equals("#")){
            					debugLog("pattern #  , rex : " + rexStr);
        						int len = rexStr.length();
        						targetStr = convParamcng.substring(convParamcng.indexOf(rexStr)+len);
        						debugLog(targetStr);
        					}else if(type.equals("%%")){
            					//int len = rexStr.length();
        						targetStr = convParamcng.substring(0, convParamcng.indexOf(rexStr));
        						debugLog(targetStr);
        					}else if(type.equals("%")){
            					targetStr = convParamcng.substring(0, convParamcng.lastIndexOf(rexStr));
        						debugLog(targetStr);
        					}
    						if(!targetStr.isEmpty()){
    							value = value.replace(paramStr, targetStr);
    						}
            			}
            		}else{
            		}
            	}
            }
            /* 修正前
            if(value.indexOf('#') > 0){
            	debugLog("find_assing_value >> " + value);
            	value = value.substring(0,value.indexOf('#'));
            }
        	*/
            if(value.contains("/")){
            	ArrayList<String> params = PatternUtil.getValues(value, "(\\$\\{[^\\s\\}]+\\})");
            	for(int cnt = 0; cnt < params.size(); cnt++){
            		String paramStr = params.get(cnt);
            		if(paramStr.contains("/")){
            			debugLog("Param has  /");
            			debugLog("Param : " + paramStr);
            			String convParam = PatternUtil.check(paramStr, "\\$\\{([^\\s\\}]+)\\}", 1);
            			debugLog("convParam : " + convParam);
            			if(!convParam.isEmpty()){
            				String reParam = PatternUtil.check(convParam, "([^¥¥s/]+)([/]+)([^\\s/]+)([/]+)([^¥¥s]*)", 1);
            				String type = PatternUtil.check(convParam, "([^¥¥s/]+)([/]+)([^\\s/]+)([/]+)([^¥¥s]*)", 2);
            				String targetStr = PatternUtil.check(convParam, "([^¥¥s/]+)([/]+)([^\\s/]+)([/]+)([^¥¥s]*)", 3);
            				//String type2 = PatternUtil.check(convParam, "([^¥¥s/]+)([/]+)([^\\s/]+)([/]+)([^¥¥s]*)", 4);
            				String convTargetStr = PatternUtil.check(convParam, "([^¥¥s/]+)([/]+)([^\\s/]+)([/]+)([^¥¥s]*)", 5);
            				debugLog(String.format("Params >> %s , %s , %s , %s , %s", reParam,type,targetStr,"", convTargetStr));

            				String newParam = "${" + reParam + "}";
            				newParam = change_line_data(newParam, ht);
            				String resultStr = "";
            				debugLog("conv >> " + newParam);
            				if(type.equals("//")){
            					resultStr = newParam.replace(targetStr, convTargetStr);
            				}else if(type.equals("/")){
            					resultStr = newParam.replaceFirst(targetStr, convTargetStr);
            				}

            				debugLog("Result >>>> " + resultStr);
            				if(!resultStr.isEmpty()){
            					value = value.replace(paramStr, resultStr);
            				}else{
            					value = value.replace(paramStr, newParam);
            				}


            				debugLog("Result 2 >> " + value);
            			}
            		}else{
            		}
            	}
            }

            if(value.indexOf(';') > 0){
            	value = value.substring(0,value.indexOf(';'));
            }

            value = value.trim();

            if(value.indexOf('`') == -1){
            	value = value.replaceAll("\"", "");
            }else{

            }


            ht.put(key, value);

            //debugLog("<PRE_SHELL_OUTPUT_END> conv value == " + value);
            //debugLog("");
            return key;
        }
        return null;
    }



    public int doTObj(int depth, CM_SRC cm_src, TObj tobj, long parent_object_id) {
        return RETURN_CONTINUE;
    }

    public int doTDpd(int depth, TDpd tdpd, CM_SRC cm_src, CM_OBJ cm_obj, int seq) throws SQLException {
        if(tdpd.getName() == null || tdpd.getName().length() == 0)
            return RETURN_BREAK;
        return RETURN_CONTINUE;
    }

    public long generateGID(String prefix, TObj tobj) {
        if(tobj.getName().endsWith(".sh")) {
            String name = tobj.getName();
            if(name.lastIndexOf('/')  != -1){
                name = name.substring(name.lastIndexOf('/')+1);
            }
            return FileUtil.getGID(prefix, name );
        }
        return 0L;
    }

    public long generateGID(String prefix, TDpd tdpd) {
        if (tdpd.getType() == CALL_PGM   ) {
            String preFix = "<COBOL>";
            return FileUtil.getGID(preFix, tdpd.getGID());//tdpd.getName());
        } else if (tdpd.getType() == USE_DSN ||  tdpd.getType() ==  USE_ASSIGN ) {
        	if(tdpd.getName().toLowerCase().endsWith(".ctl") || tdpd.getName().toLowerCase().endsWith(".sql")){
        		String preFix = "<FILE>";
                return FileUtil.getGID(preFix, tdpd.getName());
        	}else{
        		String preFix = "<DS>";
                return FileUtil.getGID(preFix, tdpd.getName());
        	}
        } else if (tdpd.getType() == CALL_C_MODULE){
            String preFix = "<EC>";
            return FileUtil.getGID(preFix, tdpd.getGID());
        }
        return 0L;
    }

    public void debugLog(String msg){
        if(DebugMode){
            System.out.println(msg);
        }
    }

    public void showTObjTree(TObj targetObj, int depth, boolean isRoot){

    	if(DebugMode){

    		if(isRoot){
        		debugLog("TObj Tree Start *******************************");
        		debugLog("TObj Gid 		:	" + targetObj.getGID());
        	}
    //
        	String objStr = "";
        	String dpdStr = "";
        	String metaStr = "";
        	int strDepth = depth+1;

        	for(int i = 0; i < strDepth ; i++){
        		objStr += "	";
        		dpdStr += "	";
        		metaStr += "	";
        		if(i+1 == strDepth){
        			objStr += "- ";
            		dpdStr += "┗ ";
            		metaStr += "	@ ";
        		}
        	}

        	if(targetObj.getTObjList().length != 0){
        		for(int i = 0 ; i < targetObj.getTObjList().length ; i++){
        			TObj tempObj = targetObj.getTObjList()[i];
        			debugLog(objStr + tempObj.getGID());
        			showTObjTree(tempObj, depth+1, false);
        		}
        		for(int i = 0 ; i < targetObj.getTDpdList().length ; i++){
        			TDpd tempDpd = targetObj.getTDpdList()[i];
        			debugLog(dpdStr + tempDpd.getGID());
        			for(int i2 = 0 ; i2 < tempDpd.getTMetaList().length ; i2++){
        				debugLog(metaStr+ " Name : " + tempDpd.getTMetaList()[i2].getName() + " Value : " + tempDpd.getTMetaList()[i2].getValue());
        				/*
        				if(tempDpd.getTMetaList()[i2].getName().equals("8212307")){
        					debugLog(metaStr+  tempDpd.getTMetaList()[i2].getValue());
        				}
        				*/
        			}
        		}
        	}else{
        		for(int i = 0 ; i < targetObj.getTDpdList().length ; i++){
        			TDpd tempDpd = targetObj.getTDpdList()[i];
        			debugLog(dpdStr + tempDpd.getGID());

        			for(int i2 = 0 ; i2 < tempDpd.getTMetaList().length ; i2++){
        				debugLog(metaStr+ " Name : " + tempDpd.getTMetaList()[i2].getName() + " Value : " + tempDpd.getTMetaList()[i2].getValue());
        				/*
        				if(tempDpd.getTMetaList()[i2].getName().equals("8212307")){
        					debugLog(metaStr+  tempDpd.getTMetaList()[i2].getValue());
        				}
        				*/
        			}

        		}
        	}


        	if(isRoot){
        		debugLog("TObj Tree End *******************************");
        	}
        }
    }







    public class MainShellUtil{

        public static final String MODULENAME = "8212305";
        public static final String ORG_JCLNAME = "8212306";
        public String[] TARGET_PARAMS = new String[]{ "${MODULENAME}", "${ORG_JCLNAME}"};
        public String[] TARGET_PARAMS_NAME = new String[]{ "MODULENAME", "ORG_JCLNAME"};

        //sub or CoreEVEFEaC?a?eoCμC?C￠CEMainEVEFEaEaEXEg
        private HashMap<String, ArrayList<String>> callInfos = new HashMap<String, ArrayList<String>>();
        //EAECEiEVEFEaCAEAE^eOiO
        private HashMap<String, HashMap<String,String>> metaInfos = new HashMap<String, HashMap<String,String>>();
        //sub or CoreEVEFEaC?a?eoCμC?C￠CEMainEVEFEaeOiO
        private HashMap<String, ArrayList<MainShellInfo>> infos = new HashMap<String, ArrayList<MainShellInfo>>();

        public MainShellUtil(HashMap<String, ArrayList<String>> infos, HashMap<String, HashMap<String,String>> metas){
            callInfos = infos;
            metaInfos = metas;
            initData();
            //log();
        }

        private void log(){
            debugLog(">>>>>>>>> CallInfos Log");
            for(Map.Entry<String, ArrayList<String>> entry : callInfos.entrySet()){
                String targetSh = entry.getKey();
                ArrayList<String> mainShells = entry.getValue();
                for(int i = 0 ; i < mainShells.size() ; i++){
                    String mainShellName = mainShells.get(i);
                    debugLog(targetSh + " <<< " + mainShellName);
                }
            }
            debugLog(">>>>>>>>> MetaInfos Log");
            for(Map.Entry<String, HashMap<String,String>> entry : metaInfos.entrySet()){
                String mainSh = entry.getKey();
                HashMap<String,String> metas = entry.getValue();
                for(Map.Entry<String, String> entry2 : metas.entrySet()){
                    String key = entry2.getKey();
                    String value = entry2.getValue();
                    debugLog(mainSh + " has meta key : " + key + " value : " + value);
                }
            }

            debugLog(">>>>>>>>> MainShellInfo Log");
            for(Map.Entry<String, ArrayList<String>> entry : callInfos.entrySet()){
                String targetSh = entry.getKey();
                ArrayList<String> mainShells = entry.getValue();
                for(int i = 0 ; i < mainShells.size() ; i++){
                    String mainShellName = mainShells.get(i);
                    if(metaInfos.containsKey(mainShellName)){
                        HashMap<String, String> metaMap = metaInfos.get(mainShellName);
                        for(Map.Entry<String, String> entry2 : metaMap.entrySet()){
                            String key = entry2.getKey();
                            String value = entry2.getValue();
                            debugLog(targetSh + " <<< " + mainShellName + " meta >> " + " key : " + key + " value : " + value);
                        }
                    }
                }
            }
        }

        private void initData(){

            for(Map.Entry<String, ArrayList<String>> entry : callInfos.entrySet()){
                String targetSh = entry.getKey();
                ArrayList<String> mainShells = entry.getValue();
                ArrayList<MainShellInfo> msInfo = new ArrayList<MainShellInfo>();
                if(mainShells.size() == 0) { continue; }
                for(int i = 0 ; i < mainShells.size() ; i++){
                    String mainShellName = mainShells.get(i);
                    MainShellInfo info = getMetaInfo(mainShellName);
                    if(info != null) {
                        msInfo.add(info);
                    }
                }
                infos.put(targetSh, msInfo);
            }
        }

        public ArrayList<String> getMetaList(String targetSh, String metaName){
            ArrayList<String> metas = new ArrayList<String>();
            debugLog("getMetaList param >> " + targetSh + "  :  " + metaName);
            if(infos.containsKey(targetSh)){
                debugLog("getMetaList 1");
                if(infos == null) { debugLog("getMetaList infos is null"); }
                ArrayList<MainShellInfo> mInfo = infos.get(targetSh);
                for(int i = 0 ; i < mInfo.size() ; i++){
                    metas.add(mInfo.get(i).getMeta(metaName));
                }
            }
            return metas;
        }

        public ArrayList<MainShellInfo> getMainShellInfos(String targetShellName){
            ArrayList<MainShellInfo> ms = new ArrayList<MainShellInfo>();
            if(infos.containsKey(targetShellName)){
                ms = infos.get(targetShellName);
            }
            return ms;
        }

        public String getStringConvMeta(MainShellInfo mainShell, String paramStr){

            String newValue = paramStr;
            String module = mainShell.getMeta(MainShellUtil.MODULENAME);
            String org_jclname = mainShell.getMeta(MainShellUtil.ORG_JCLNAME);
            if(!module.isEmpty()){
                newValue = newValue.replace("${MODULENAME}", module);
            }
            if(!org_jclname.isEmpty()){
                newValue = newValue.replace("${ORG_JCLNAME}", org_jclname);
            }
            return newValue;
        }

        private MainShellInfo getMetaInfo(String mainShellName){

            MainShellInfo ms = null;
            if(metaInfos.containsKey(mainShellName)){
                ms = new MainShellInfo(mainShellName);
                HashMap<String, String> metaMap = metaInfos.get(mainShellName);
                for(Map.Entry<String, String> entry : metaMap.entrySet()){
                    String key = entry.getKey();
                    String value = entry.getValue();
                    ms.addMeta(key, value);
                }
            }
            return ms;
        }

        public boolean checkParams(String data){

            boolean isTarget = false;
            for(int i = 0 ; i < TARGET_PARAMS.length ; i++){
                if(data.contains(TARGET_PARAMS[i])){
                    isTarget = true;
                    break;
                }
            }
            return isTarget;
        }

        public String hasMeta(String meta){
            String result = "";
            if(meta.equals(MainShellUtil.MODULENAME)){
                result = TARGET_PARAMS_NAME[0];
            }else if(meta.equals(MainShellUtil.ORG_JCLNAME)){
                result = TARGET_PARAMS_NAME[1];
            }
            return result;
        }
    }

    public class SubShellUtil{
        //CoreEVEFEaC?a?eoCμC?C￠CESubEVEFEaEaEXEg
        private HashMap<String, ArrayList<String>> callInfos = new HashMap<String, ArrayList<String>>();

        public SubShellUtil(HashMap<String, ArrayList<String>> infos){
            callInfos = infos;
            //log();
        }

        public ArrayList<String> getSubList(String targetShellName){
            ArrayList<String> subShells = new ArrayList<String>();
            if(callInfos.containsKey(targetShellName)){
                subShells = callInfos.get(targetShellName);
            }
            return subShells;
        }

        private void log(){
            debugLog(">>>>>>>>> CallInfos Log");
            for(Map.Entry<String, ArrayList<String>> entry : callInfos.entrySet()){
                String targetSh = entry.getKey();
                ArrayList<String> mainShells = entry.getValue();
                for(int i = 0 ; i < mainShells.size() ; i++){
                    String mainShellName = mainShells.get(i);
                    debugLog(targetSh + " <<< " + mainShellName);
                }
            }
        }
    }

    public class MainShellInfo{

        private HashMap<String, String> metas = null;
        private String shellName = null;
        public String getShellName(){
            return shellName;
        }

        public MainShellInfo(String sh){
            shellName = sh;
            metas = new HashMap<String, String>();
        }

        public void addMeta(String key, String value){
            if(metas.containsKey(key)){
                metas.remove(key);
            }
            metas.put(key, value);
        }

        public String getMeta(String key){
            String value = "";
            if(metas.containsKey(key)){
                value = metas.get(key);
            }
            return value;
        }
    }

    public class CModuleInfo{

        private HashMap<String,String> moduleInfos = null;
        public String ModuleListRex = "";
        public boolean isEnable = false;

        public CModuleInfo(HashMap<String,String> infos){
            moduleInfos = infos;
            init();
            //log();
        }

        public void init(){

            ArrayList<String> keys = new ArrayList<String>();
            for(Map.Entry<String, String> entry : moduleInfos.entrySet()){
                String key = entry.getKey();
                keys.add(key);
            }
            String tempRex = "";
            if(keys.size() != 0){
            	for(int i = 0 ; i < keys.size() ; i++){
                    tempRex += keys.get(i);
                    if(i+1 != keys.size()){
                        tempRex += "|";
                    }
                }
                ModuleListRex = "(" + tempRex + ")";
                debugLog("init CModuleInfo >> " + tempRex);
                isEnable = true;
            }

        }

        public boolean findCModule(String moduleName){
            return moduleInfos.containsKey(moduleName);
        }

        public String getModuleGid(String moduleName){
            return moduleInfos.get(moduleName);
        }

        public void log(){
            debugLog(">>>>>>>>> CModuleInfo Log");
            for(Map.Entry<String, String> entry : moduleInfos.entrySet()){
                String key = entry.getKey();
                String fullPath = entry.getValue();
                debugLog(key + " >> " + fullPath);
            }
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
    	NS_JCL_SHELL2_5 aa = new NS_JCL_SHELL2_5();
        SHELL_TYPE = -1;
        TObj obj_root = new TObj(1, "", "", 100, new TLocation() );

        try {

            htOrigin = new Hashtable<String, String>();
            aa.testInit();

            aa.init_ht("C:\\Users\\ito-motoi\\Desktop\\PJ\\NISSAN\\Source\\PCS_TAI\\ASP2_6_PV_SHELL.tar\\SHELL\\MCATS_run.env");
            //aa.init_ht("/Users/itomotoi/Desktop/MCATS_run.env");


            aa.ht = new Hashtable<String, String>();
    		for(Map.Entry<String, String> entry : htOrigin.entrySet()){
    		    String key = entry.getKey();
    		    String value = entry.getValue();
    		    aa.ht.put(key, value);
    		}

    		//aa.addstep("/Users/itomotoi/Desktop/sub/DN4B116M_S.sh" , obj_root, htOrigin);
    		//aa.addstep("C:\\Users\\ito-motoi\\Desktop\\PJ\\NISSAN\\Source\\PCS_TAI\\ASP2_6_PV_SHELL.tar\\SHELL\\sort\\B533220S.sh" , obj_root, htOrigin);
            aa.addstep("C:\\Users\\ito-motoi\\Desktop\\PJ\\NISSAN\\Source\\PCS_TAI\\ASP2_6_PV_SHELL.tar\\SHELL\\sub\\DN4B11WO.sh" , obj_root, htOrigin);
            //aa.addstep("C:\\Users\\ito-motoi\\Desktop\\PJ\\NISSAN\\Source\\PCS_TAI\\ASP2_6_PV_SHELL.tar\\SHELL\\sub\\TL1_DN4BD1BM.sh" , obj_root, htOrigin);
            //TL1_DN4BD1BM
            //aa.init_ht("/Users/itomotoi/Desktop/MCATS_run.env");
            //aa.addstep("/Users/itomotoi/Desktop/DN31C1KM.sh" , obj_root, htOrigin);
            //SHELL\MAIN\DMQEXECN6BTG001.sh
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testInit(){
    	currentShellFile = "DN31C1KM.sh";
        HashMap<String, ArrayList<String>> callInfos = new HashMap<String, ArrayList<String>>();
        ArrayList<String> mainShells = new ArrayList<String>();
        mainShells.add("mainshell1.sh");
        mainShells.add("mainshell2.sh");
        callInfos.put(currentShellFile, mainShells);
        //main shell : meta info
        HashMap<String, HashMap<String,String>> metaInfos = new HashMap<String, HashMap<String,String>>();
        HashMap<String, String> testMap = new HashMap<String,String>();
        testMap.put(MainShellUtil.MODULENAME, "MainModule1");
        testMap.put(MainShellUtil.ORG_JCLNAME, "MainOrgJcl1");
        metaInfos.put("mainshell1.sh", testMap);
        testMap = new HashMap<String,String>();
        testMap.put(MainShellUtil.MODULENAME, "MainModule2");
        testMap.put(MainShellUtil.ORG_JCLNAME, "MainOrgJcl2");
        metaInfos.put("mainshell2.sh", testMap);
        mainShellUtil = new MainShellUtil(callInfos, metaInfos);
        HashMap<String, ArrayList<String>> callInfoss = new HashMap<String, ArrayList<String>>();
        subShellUtil = new SubShellUtil(callInfoss);
        HashMap<String, String> test = new HashMap<String, String>();
        test.put("DN6BTG", ">>>>>>>>>>>>>>>>>>>>>>>DN6BTG");
        test.put("DN6BTG_pre", "----------------------DN6BTG_pre");
        test.put("DN63TE4", "/Test/Test/DN63TE223");
        test.put("DN63TE5", "/Test/Test/DN63TE233");
        test.put("DN63TE7", "/Test/Test/DN63TE2211");
        cModuleUtil = new CModuleInfo(test);
        cobolFileList = new HashMap<String, String>();

    }





}
