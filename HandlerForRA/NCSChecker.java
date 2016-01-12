
package changeminer.HandlerForRA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itplus.cm.parser.common.CMParserCommonData;
import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.DBUtil;
import com.itplus.cm.ce.util.Environment;
import com.itplus.cm.ce.util.FileUtil;

import extractor.common.tobj.TDpd;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;


public class NCSChecker extends HandlerForRA
{
    public static void main(String[] args){
        try{
            NCSChecker nc = new NCSChecker();
            String filePath = "C:\\Users\\ito-motoi\\Desktop\\作業フォルダ\\lim\\pv.csv";
            //String filePath = "/Users/itomotoi/Desktop/pv.csv";
            nc.addStep(filePath, null);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }


    public NCSUtil util = new NCSUtil();
    public static ArrayList<ResultData>  mainList;
    public static ArrayList<ResultData>  subList;
    public static ArrayList<ResultData>  coreList;
    public static ArrayList<ResultData>  tempList;
    String outputPath = "C:\\Users\\ito-motoi\\Desktop\\Source\\result\\";
    //String outputPath = "C:\\Users\\ito-motoi\\Desktop\\作業フォルダ\\lim\\result\\result\\";
    public NCSChecker() {

    }

    public void addStep(String fileName, CM_SRC src) throws Exception{




        Connection con = null;

        try{

            initCSV(fileName);

            //util.log();
            //initCSVTemp("");

            if(src != null){

                con = DBUtil.getConnection(false);



                if(mainList == null){
                    /*
                     long mainlist = getAnalyzeTypeIdFromTitle(src.getCOLLECT_ID(), src.getANALYZE_FILTER_ID(), con, "main");
                     System.out.println("mainlist >> " + mainlist);
                     if(mainlist >= 0){
                     mainList = compareData(mainlist, con);
                     System.out.println("mainlist >> " + mainList.size());
                     }
                     */
                }

                if(subList == null){
                    long sublist = getAnalyzeTypeIdFromTitle(src.getCOLLECT_ID(), src.getANALYZE_FILTER_ID(), con, "sub");
                    System.out.println("subList >> " + sublist);
                    if(sublist >= 0){
                        subList = compareData(sublist, con);
                        System.out.println("subList >> " + subList.size());
                    }
                }

                if(coreList == null){
                    long corelist = getAnalyzeTypeIdFromTitle(src.getCOLLECT_ID(), src.getANALYZE_FILTER_ID(), con, "core");
                    System.out.println("corelist >> " + corelist);
                    if(corelist >= 0){
                        coreList = compareData(corelist, con);
                        System.out.println("mainlist >> " + coreList.size());
                    }
                }
            }

            int cnt = 0;


            ArrayList<String> allData = new ArrayList<String>();
            for(Map.Entry<String, ArrayList<StepInfo>> entry : util.dataList.entrySet()){
                String shellName = entry.getKey() + ".sh";
                ArrayList<StepInfo> steps = entry.getValue();

               if(!shellName.equals("D5335NJO.sh")){ continue; }

                System.out.println("");
                System.out.println("-----------------------------------------------------------------------------");
                System.out.println(String.format("NCS TargetSubShell : %s, shell has %d steps", shellName, steps.size()));

                cnt++;

                ArrayList<ResultData> allSub = new ArrayList<ResultData>();
                ArrayList<ResultData> targetDatasSub = getAssignDatas(shellName, subList);
                ArrayList<ResultData> targetDatasCore = getAssignDatas(shellName, coreList);

                allSub.addAll(targetDatasSub);
                allSub.addAll(targetDatasCore);

                //allSub = tempList;
                System.out.println(String.format("CM Target Data : %d", allSub.size()));
                ArrayList<String> csv = new ArrayList<String>();

                for(int i = 0; i < steps.size() ; i++){
                	StepInfo step = steps.get(i);
                	String stepStr = String.format("%s.%s.%s", entry.getKey(), step.stepName, step.cmdName);

                	ArrayList<ResultData> cmData = checkData(entry.getKey(), step, allSub);

                	System.out.println(String.format("NCS Shell %s, Step %s, Cmd %s", entry.getKey(), step.stepName, step.cmdName));

                	for(int i2 = 0 ; i2 < step.ncsDatas.size() ; i2++){

                		NCSData targetNcsData = step.ncsDatas.get(i2);
                		ArrayList<ResultData> result = checkMatchData(step.cmdName, targetNcsData, cmData);

                		String dir = targetNcsData.getCsvValue(NCSDataItem.DIR).replace(targetNcsData.getCsvValue(NCSDataItem.COL2), "");
                        String dsn = targetNcsData.getCsvValue(NCSDataItem.DSN).replace("SWATPID", "${PID}");
                        String assignPath = "Empty";
                        if(dir.isEmpty() && dsn.isEmpty()){
                        }else{
                            assignPath = dir + "/" + dsn;
                        }
                        assignPath = assignPath.replace("//", "/");
                		String line = "";

                		if(result.size() != 0){
                			for(int k = 0 ; k < result.size() ; k++){
                    			ResultData data = result.get(k);
                    			System.out.println(String.format("		Matched CM data dd : %s ,  dsn : %s , assignPath : %s", data.ddName, data.callTarget, data.assignData));
                    			//shellname,step,cmd,dd,dsn,assign,

                    			line = String.format("%s,%s,%s,%s,%s,%s,%s,%s", entry.getKey(),
                    															  step.stepName,
    							    											  step.cmdName,
    							    											  targetNcsData.getCsvValue(NCSDataItem.DD),
    							    											  dsn,
    							    											  assignPath,
    							    											  data.assignData,
    							    											  "OK");
                    			System.out.println("Result >> "+line);


                    		}
                		}else{
                			line = String.format("%s,%s,%s,%s,%s,%s,%s,%s", entry.getKey(),
									  step.stepName,
									  step.cmdName,
									  targetNcsData.getCsvValue(NCSDataItem.DD),
									  dsn,
									  assignPath,
									  "",
									  "NG");
                			System.out.println("Result >> "+line);
                		}

                		csv.add(line);
                		allData.add(line);
                    }
                }

                StringBuffer buffer = new StringBuffer();
                buffer.append("SHELL,STEP,CMD,DD,DSN,ASSIGN_PATH,CM_ASSIGN_PATH,RESULT");
                for(int i = 0; i < csv.size() ; i++){
                	//System.out.println(csv.get(i));
                	buffer.append("\n");
                	buffer.append(csv.get(i));

                }

                String outpath = outputPath+shellName.replace(".sh", ".csv");
                File check = new File(outpath);

                if(check.exists()){
                    check.delete();
                }

                FileUtil.writeToFile(buffer.toString(), "utf-8", outpath);
            }


            StringBuffer buffer = new StringBuffer();
            buffer.append("SHELL,STEP,CMD,DD,DSN,ASSIGN_PATH,CM_ASSIGN_PATH,RESULT");
            for(int i = 0; i < allData.size() ; i++){
            	//System.out.println(csv.get(i));
            	buffer.append("\n");
            	buffer.append(allData.get(i));

            }

            String outpath = outputPath+"AllData.csv";
            File check = new File(outpath);

            if(check.exists()){
                check.delete();
            }

            FileUtil.writeToFile(buffer.toString(), "utf-8", outpath);


        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(con != null){
                if(!con.isClosed()){
                    con.close();
                }

                con = null;
            }
        }
    }

    public ArrayList<ResultData> checkMatchData(String cmdName, NCSData ncsData, ArrayList<ResultData> cmData){

    	ArrayList<ResultData> result = new ArrayList<ResultData>();

    	String seq = ncsData.getCsvValue(NCSDataItem.SEQ);
        String io = ncsData.getCsvValue(NCSDataItem.IO);
        String pgm = ncsData.getCsvValue(NCSDataItem.PGMID);
        String dd = ncsData.getCsvValue(NCSDataItem.DD);
        dd = dd.isEmpty() ? "Empty" : dd;
        String dir = ncsData.getCsvValue(NCSDataItem.DIR).replace(ncsData.getCsvValue(NCSDataItem.COL2), "");
        String dsn = ncsData.getCsvValue(NCSDataItem.DSN).replace("SWATPID", "${PID}");
        String assignPath = "Empty";
        String mainShell = ncsData.getCsvValue(NCSDataItem.MAINSHELL);
        if(dir.isEmpty() && dsn.isEmpty()){

        }else{
            assignPath = dir + "/" + dsn;
        }
        assignPath = assignPath.replace("//", "/");

        /*
        System.out.println("");
        System.out.println("checkMatchData-----------------------------------Level1");
        System.out.println("seq : " + seq);
        System.out.println("cmdName : " + cmdName);
        System.out.println("dd : " + dd);
        System.out.println("io : " + io);
        System.out.println("dir : " + dir);
        System.out.println("dsn : " +  dsn);
        System.out.println("assignPath : " + assignPath);
        System.out.println("mainShell : " + mainShell);
*/
        //String line = String.format("%s,%s,%s(%s),%s,%s,%s,%s", seq, cmdName, dd, io, dir, dsn, assignPath, mainShell);

        //System.out.println("						" + line);
        System.out.println("	checkMatchData-----------------------------------");
        if(cmData.size() == 0){

        }else{
        	for(int i = 0; i < cmData.size() ; i++){
            	ResultData data = cmData.get(i);
        		String stepStr2 = String.format("%s.%s.%s",	data.shellName.replace(".sh", ""), data.stepType.equals("STEP") ? data.stepName : data.jclName, data.stepType.equals("CMD") ? data.stepName : data.cmdName);

        		String step2 = data.stepName;
                int seq2 = data.objLineNum;
                String cmd2 = data.cmdName;
                String dd2 = data.ddName;
                String dsn2 = data.callTarget;
                String assignPath2 = data.assignData;
                String line2 = null;

                if(cmd2.startsWith("RunCobol")){
                	dd2 = "DD_"+dd2;
                	if(dsn2.equals("null")){
                		dsn2 = "/dev/null";
                	}
                }
                if(cmd2.startsWith("RunSort")){

                }

                System.out.println(String.format("seq : %s >> %d", seq, seq2));
                System.out.println(String.format("cmn : %s", cmdName));
                System.out.println(String.format("dd  : %s >> %s", dd, dd2));
                System.out.println(String.format("dsn  : %s >> %s", dsn, dsn2));
                System.out.println(String.format("AsP  : %s >> %s", assignPath, assignPath2));

                if(dd.equals(dd2) || dd.equals("Empty")){
                	if(dsn.equals(dsn2)){
                		line2 = String.format("CM,%s,%d,%s,%s,%s,%s,%s,%s", stepStr2, seq2, cmd2, dd2, "-", dsn2, assignPath2, "-");
                    	System.out.println("		>>>>>>>>>>>>>>>>>>>>>OK");
                    	data.type = NCSDataType.DATA_OK;
                    	result.add(data);
                	}else{
                		System.out.println("		>>>>>>>>>>>>>>>>>>>>>NG Not dsn");

                	}
                }else{

                	System.out.println("		>>>>>>>>>>>>>>>>>>>>>NG Not dd");
                }



            }
        }



    	return result;
    }

    public ArrayList<ResultData> checkData(String shName, StepInfo step, ArrayList<ResultData> allSub){

    	String stepStr = String.format("%s.%s.%s", shName, step.stepName, step.cmdName);
    	ArrayList<ResultData> returnData = new ArrayList<ResultData>();

    	for(int i = 0; i < allSub.size() ; i++){
    		ResultData data = allSub.get(i);
    		String cmStepStr = "";
    		if(data.cmdName.startsWith("RunSort")){
    			cmStepStr = String.format("%s.%s.%s",	data.shellName.replace(".sh", ""), data.stepName, "RunSort");
    		}else{
    			cmStepStr = String.format("%s.%s.%s",	data.shellName.replace(".sh", ""), data.stepName, data.cmdName.replace("RunCobol", "").replace("(", "").replace("", "").replace(")", "").replace("", ""));
    		}
    		//

    		if(stepStr.equals(cmStepStr)){
    			//System.out.println("													equal>>");
    			System.out.println(String.format("Check OK %s : %s", stepStr, cmStepStr));
    			returnData.add(data);
    		}else{
    			System.out.println(String.format("Check NO %s : %s", stepStr, cmStepStr));
    		}
    	}

    	for(int i = 0; i < returnData.size() ; i++){
    		ResultData data = returnData.get(i);
    		String stepStr2 = String.format("%s.%s.%s",	data.shellName.replace(".sh", ""), data.stepType.equals("STEP") ? data.stepName : data.jclName, data.stepType.equals("CMD") ? data.stepName : data.cmdName);
            String step2 = data.stepName;
            int seq = data.objLineNum;
            String cmd = data.cmdName;
            String dd = data.ddName;
            String dsn = data.callTarget;
            String assignPath = data.assignData;
            String line = null;
            if(data.stepType.equals("STEP")){
                //System.out.println(String.format("				 >>  step : %s , cmd : %s , dd : %s , dsn : %s , assign : %s ", step, cmd, dd, dsn, assignPath));
                line = String.format("1 CM,%s,%d,%s,%s,%s,%s,%s,%s", stepStr2, seq, cmd, dd, "-", dsn, assignPath, "-");
                //System.out.println("						" + line);

              //ncs step seq dd dir dsn assign mainshell
            }else{
                //System.out.println(String.format("				 >>  step : - , cmd : %s , dd : %s , dsn : %s , assign : %s ", step, cmd, dd, assignPath));
                line = String.format("2 CM,%s,%d,%s,%s,%s,%s,%s,%s", stepStr2, seq, step2, cmd, "-", dsn, assignPath, "-");
                //System.out.println("						" + line);


            }

           System.out.println("		 " + line);


    	}

    	return returnData;


    }



    public ArrayList<ResultData> getAssignDatas(String shellName, ArrayList<ResultData> resultList){

        ArrayList<ResultData> targetDatas = new ArrayList<ResultData>();

        if(resultList != null){

            for(int i = 0 ; i < resultList.size() ;i++){
                ResultData temp = resultList.get(i);

                if(temp.shellName.equals(shellName)){
                    targetDatas.add(temp);
                }
            }
        }

        return targetDatas;
    }

    public void initCSV(String fileName){


        FileInputStream fis = null;
        BufferedReader br = null;
        try {
            // OPEN FILE
            String fileStr = fileName;
            File file = new File(fileStr);
            fis = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(fis));
            int cnt = 0;
            String subShellName = "";
            int subShellSeq = 0;
            String step = "";
            String pgmId = "";

            ArrayList<NCSData> datas = new ArrayList<NCSData>();
            while (br.ready()) {
                cnt++;

                String line_data = br.readLine();

                NCSData data = new NCSData(line_data);
                String subShellNameTemp = data.getCsvValue(NCSDataItem.SUB_SHELL);
                int subShellSeqTemp = 0;
                if(!data.getCsvValue(NCSDataItem.SEQ).isEmpty()){
                    subShellSeqTemp = Integer.parseInt(data.getCsvValue(NCSDataItem.SEQ));
                }
                String stepTemp = data.getCsvValue(NCSDataItem.STEP);
                String pgmIdTemp = data.getCsvValue(NCSDataItem.PGMID);

                if(subShellName.isEmpty()){
                    subShellName = data.getCsvValue(NCSDataItem.SUB_SHELL);

                    subShellSeq = 0;
                    if(!data.getCsvValue(NCSDataItem.SEQ).isEmpty()){
                        subShellSeq = Integer.parseInt(data.getCsvValue(NCSDataItem.SEQ));
                    }
                    step = data.getCsvValue(NCSDataItem.STEP);
                    pgmId = data.getCsvValue(NCSDataItem.PGMID);
                    datas.add(data);
                }else{

                    if(subShellName.equals(subShellNameTemp)){
                        //同じファイル
                        //stepが変更
                        if(subShellSeq == subShellSeqTemp){
                            subShellSeq = 0;
                            if(!data.getCsvValue(NCSDataItem.SEQ).isEmpty()){
                                subShellSeq = Integer.parseInt(data.getCsvValue(NCSDataItem.SEQ));
                            }
                            step = data.getCsvValue(NCSDataItem.STEP);
                            pgmId = data.getCsvValue(NCSDataItem.PGMID);
                            datas.add(data);
                        }else{
                            //stepが変更
                            util.add(subShellName, step, pgmId, datas);
                            datas = new ArrayList<NCSData>();

                            subShellName = subShellNameTemp;
                            subShellSeq = subShellSeqTemp;
                            step = stepTemp;
                            pgmId = pgmIdTemp;
                            datas.add(data);
                        }
                    }else{
                        //ファイル名が変更
                        util.add(subShellName, step, pgmId, datas);
                        datas = new ArrayList<NCSData>();
                        subShellName = subShellNameTemp;
                        subShellSeq = subShellSeqTemp;
                        step = stepTemp;
                        pgmId = pgmIdTemp;
                        datas.add(data);
                    }

                }


            }

            //util.log();

        } catch (Exception e) {
            e.printStackTrace();
        }  finally {
            try {
                br.close();
                fis.close();
            }  catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public void initCSVTemp(String fileName){
        tempList = new ArrayList<ResultData>();

        ResultData data1 = new ResultData();
        data1.shellName = "DN4B33EM.sh";
        data1.jclName = "6DN4B33EM";
        data1.stepType = "STEP";
        data1.stepName = "S00A";
        data1.objLineNum = 26;
        data1.cmdType = "CMD";
        data1.cmdName = "exp";
        data1.ddName = "DD_EXPFL";
        data1.callTarget = "DN4BCARMAINW003.6DN4B33EM.S00A.dmp";
        data1.assignData = "/ASP20002/APPL-DATA/batchdata/DN4BCARMAINW003.6DN4B33EM.S00A.dmp";
        tempList.add(data1);

        data1 = new ResultData();
        data1.shellName = "DN4B33EM.sh";
        data1.jclName = "6DN4B33EM";
        data1.stepType = "STEP";
        data1.stepName = "S010";
        data1.objLineNum = 42;
        data1.cmdType = "CMD";
        data1.cmdName = "RunCobol(N42350)";
        data1.ddName = "SYS011";
        data1.callTarget = "DN4BCARLISTW002.6DN4B33DM_L.SN4B0004";
        data1.assignData = "/ASP20002/APPL-DATA/batchdata/DN4BCARLISTW002.6DN4B33DM_L.SN4B0004";
        tempList.add(data1);

        data1 = new ResultData();
        data1.shellName = "DN4B33EM.sh";
        data1.jclName = "6DN4B33EM";
        data1.stepType = "STEP";
        data1.stepName = "S010";
        data1.objLineNum = 42;
        data1.cmdType = "CMD";
        data1.cmdName = "RunCobol(N42350)";
        data1.ddName = "SYS011";
        data1.callTarget = "DN4BCARMAINW003.6DN4B33EM.S010.SYS015";
        data1.assignData = "/ASP20002/APPL-DATA/batchdata/DN4BCARMAINW003.6DN4B33EM.S010.SYS015";
        tempList.add(data1);

        data1 = new ResultData();
        data1.shellName = "DN4B33EM.sh";
        data1.jclName = "6DN4B33EM";
        data1.stepType = "STEP";
        data1.stepName = "S010";
        data1.objLineNum = 42;
        data1.cmdType = "CMD";
        data1.cmdName = "RunCobol(N42350)";
        data1.ddName = "SYS015";
        data1.callTarget = "DN4BCARMAINW003.6DN4B33EM.S010.SYS015";
        data1.assignData = "/ASP20002/APPL-DATA/batchdata/DN4BCARMAINW003.6DN4B33EM.S010.SYS015";
        tempList.add(data1);

        data1 = new ResultData();
        data1.shellName = "DN4B33EM.sh";
        data1.jclName = "6DN4B33EM";
        data1.stepType = "STEP";
        data1.stepName = "S010";
        data1.objLineNum = 42;
        data1.cmdType = "CMD";
        data1.cmdName = "RunCobol(N42350)";
        data1.ddName = "SYS015";
        data1.callTarget = "DN4BCARLISTW002.6DN4B33DM_L.SN4B0004";
        data1.assignData = "/ASP20002/APPL-DATA/batchdata/DN4BCARLISTW002.6DN4B33DM_L.SN4B0004";
        tempList.add(data1);

        data1 = new ResultData();
        data1.shellName = "DN4B33EM.sh";
        data1.jclName = "6DN4B33EM";
        data1.stepType = "CMD";
        data1.stepName = "rm";
        data1.objLineNum = 57;
        data1.cmdType = "DD";
        data1.cmdName = "$1";
        data1.ddName = "${PID}.*";
        data1.callTarget = null;
        data1.assignData = "/ASP20002/APPL-DATA/batchdata/${PID}.*";
        tempList.add(data1);

        data1 = new ResultData();
        data1.shellName = "DN4B33EM.sh";
        data1.jclName = "6DN4B33EM";
        data1.stepType = "CMD";
        data1.stepName = "rm";
        data1.objLineNum = 61;
        data1.cmdType = "DD";
        data1.cmdName = "$1";
        data1.ddName = "${PID}.*";
        data1.callTarget = null;
        data1.assignData = "/ASP20002/APPL-TEMP/CATS/${PID}.*";
        tempList.add(data1);

        data1 = new ResultData();
        data1.shellName = "DN4B33EM.sh";
        data1.jclName = "6DN4B33EM";
        data1.stepType = "CMD";
        data1.stepName = "rm";
        data1.objLineNum = 65;
        data1.cmdType = "DD";
        data1.cmdName = "$1";
        data1.ddName = "*_${PID}";
        data1.callTarget = null;
        data1.assignData = "/ASP20002/APPL-TEMP/CATS/*_${PID}";
        tempList.add(data1);
    }


    public long getAnalyzeTypeIdFromTitle(long collectId, long filterId, Connection con, String targetTitle) throws Exception{

        PreparedStatement ps = null;
        ResultSet rs = null;
        long targetAnalyzeId = -1;

        try {
            //Connection con = DBUtil.getConnection(false);



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
                //return rs.getString("OPTION_VALUE");

                long ids = rs.getLong("REF_ANALYZE_TYPE_ID");
                String title = rs.getString("TITLE").toLowerCase().trim();

                if(title.contains(targetTitle.toLowerCase())){
                    targetAnalyzeId = ids;
                    break;
                }

            }

        } catch(SQLException e) {
            throw e;
        } finally {
            DBUtil.closeResource(ps, rs);
        }

        return targetAnalyzeId;

    }

    public ArrayList<ResultData> compareData(long analyzeTypeId, Connection con) throws Exception{

        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<ResultData> results = new ArrayList<ResultData>();

        long targetAnalyzeId = -1;

        try {

        	StringBuffer sb = new StringBuffer();
        	sb.append("SELECT ");
        	sb.append("src.SNAME, ");
        	sb.append("obj2.OBJ_NAME AS JCL_NAME, ");
        	sb.append("CASE WHEN obj3.OBJ_TYPE_ID = 130005 THEN 'STEP' WHEN obj3.OBJ_TYPE_ID = 821173 THEN 'CMD' END STEP_TYPE, ");
        	sb.append("obj3.OBJ_NAME AS STEP_NAME, obj3.OBJ_TYPE_ID, ");
        	sb.append("obj3.START_LINE_NUM, ");
        	sb.append("CASE WHEN obj4.OBJ_TYPE_ID = 821173 THEN 'CMD' WHEN obj4.OBJ_TYPE_ID = 130006 THEN 'DD' END CMD_TYPE, ");
        	sb.append("obj4.OBJ_NAME AS CMD_NAME, ");
        	sb.append("obj5.OBJ_NAME AS DD, ");
        	sb.append("dpd.CALL_TARGET, ");
        	sb.append("meta.META_VALUE AS ASSIGN_PATH ");
        	sb.append("FROM ");
        	sb.append("CM_ANALYZE_TYPE_FILTER  filter, CM_SRC src, CM_OBJ obj, CM_OBJ obj2, CM_OBJ obj3, CM_OBJ obj4, CM_OBJ obj5, CM_OBJ_DPD dpd,  CM_OBJ_DPD_META meta, CM_OBJ_DPD_LINE line, CM_TYPE ty ");
        	sb.append("WHERE ");
        	sb.append("filter.ANALYZE_TYPE_ID = ? AND ");
        	sb.append("src.ANALYZE_FILTER_ID = filter.ANALYZE_FILTER_ID AND ");
        	sb.append("filter.IS_DELETE = 'N' AND ");
        	sb.append("src.SRC_ID = obj.SRC_ID AND ");
        	sb.append("obj.OBJ_ID = obj2.PRT_OBJ_ID(+) AND ");
        	sb.append("obj2.OBJ_ID = obj3.PRT_OBJ_ID(+) AND ");
        	sb.append("obj3.OBJ_ID = obj4.PRT_OBJ_ID(+) AND ");
        	sb.append("obj4.OBJ_ID = obj5.PRT_OBJ_ID(+) AND ");
        	sb.append("obj5.OBJ_ID = dpd.CALLER_OBJ_ID(+) AND ");
        	sb.append("dpd.DPD_ID = meta.DPD_ID(+) AND ");
        	sb.append("dpd.DPD_ID = line.DPD_ID(+) AND ");
        	sb.append("src.CHANGE_REASON_CODE < 9000 AND ");
        	sb.append("obj.OBJ_TYPE_ID = ty.TYPE_ID AND ");
        	sb.append("filter.FILTER_STRING = '**.sh' AND ");
        	sb.append("obj2.OBJ_TYPE_ID = 130003 AND ");
        	sb.append("obj3.OBJ_TYPE_ID = 130005 AND ");
        	sb.append("meta.META_NAME = 8211201 ");
        	sb.append("ORDER BY 1,2,6,7,8,9");
        	/*
        	sb.append(" UNION ");
        	sb.append("SELECT ");
        	sb.append("src.SNAME, ");
        	sb.append("obj2.OBJ_NAME AS JCL_NAME, ");
        	sb.append("CASE WHEN obj3.OBJ_TYPE_ID = 130005 THEN 'STEP' WHEN obj3.OBJ_TYPE_ID = 821173 THEN 'CMD' END STEP_TYPE, ");
        	sb.append("obj3.OBJ_NAME AS STEP_NAME, obj3.OBJ_TYPE_ID, ");
        	sb.append("obj3.START_LINE_NUM, ");
        	sb.append("CASE WHEN obj4.OBJ_TYPE_ID = 821173 THEN 'CMD' WHEN obj4.OBJ_TYPE_ID = 130006 THEN 'DD' END CMD_TYPE, ");
        	sb.append("obj4.OBJ_NAME AS CMD_NAME, ");
        	sb.append("obj5.OBJ_NAME AS DD, ");
        	sb.append("dpd.CALL_TARGET, ");
        	sb.append("meta.META_VALUE AS ASSIGN_PATH ");
        	sb.append("FROM ");
        	sb.append("CM_ANALYZE_TYPE_FILTER  filter, CM_SRC src, CM_OBJ obj, CM_OBJ obj2, CM_OBJ obj3, CM_OBJ obj4, CM_OBJ obj5, CM_OBJ_DPD dpd,  CM_OBJ_DPD_META meta, CM_OBJ_DPD_LINE line, CM_TYPE ty ");
        	sb.append("WHERE ");
        	sb.append("filter.ANALYZE_TYPE_ID = ? AND ");
        	sb.append("src.ANALYZE_FILTER_ID = filter.ANALYZE_FILTER_ID AND ");
        	sb.append("filter.IS_DELETE = 'N' AND ");
        	sb.append("src.SRC_ID = obj.SRC_ID AND ");
        	sb.append("obj.OBJ_ID = obj2.PRT_OBJ_ID(+) AND ");
        	sb.append("obj2.OBJ_ID = obj3.PRT_OBJ_ID(+) AND ");
        	sb.append("obj3.OBJ_ID = obj4.PRT_OBJ_ID(+) AND ");
        	sb.append("obj4.OBJ_ID = obj5.PRT_OBJ_ID(+) AND ");
        	sb.append("obj4.OBJ_ID = dpd.CALLER_OBJ_ID(+) AND ");
        	sb.append("dpd.DPD_ID = meta.DPD_ID(+) AND ");
        	sb.append("dpd.DPD_ID = line.DPD_ID(+) AND ");
        	sb.append("src.CHANGE_REASON_CODE < 9000 AND ");
        	sb.append("obj.OBJ_TYPE_ID = ty.TYPE_ID AND ");
        	sb.append("filter.FILTER_STRING = '**.sh' AND ");
        	sb.append("obj2.OBJ_TYPE_ID = 130003 AND ");
        	sb.append("obj3.OBJ_TYPE_ID = 821173 AND ");
        	sb.append("meta.META_NAME = 8211201 ");
        	sb.append("ORDER BY 1,2,6,7,8,9");
			*/

            ps = con.prepareStatement(sb.toString());


            ps.setLong(1, analyzeTypeId);
            //ps.setLong(2, analyzeTypeId);
            //ps.setLong(2, filterId);
            rs = ps.executeQuery();


            while(rs.next()){

                ResultData rd = new ResultData();

                rd.shellName = rs.getString(1);//sname
                rd.jclName = rs.getString(2);//jclname
                rd.stepType = rs.getString(3);//steptype
                rd.stepName = rs.getString(4);//step
                rd.cmdName = rs.getString(4);//cmd
                rd.objLineNum = rs.getInt(6);//linenum
                rd.ddName = rs.getString(6);//dd
                rd.cmdType = rs.getString(7);
                rd.cmdName = rs.getString(8);
                rd.ddName = rs.getString(9);
                rd.callTarget = rs.getString(10);//target
                rd.assignData = rs.getString(11);//assigndata

                results.add(rd);

            }



        } catch(SQLException e) {
            throw e;
        } finally {
            DBUtil.closeResource(ps, rs);
        }

        return results;

    }

    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {

        String file_name = Environment.getSourceDir() + "/" + cm_src.getCOLLECT_ID() + cm_src.getSPATH() + cm_src.getSNAME();
        TObj obj_root = tresult.getTObjList()[0];
        System.out.println("File >>>>>>>>>>>>>>>>>>>>>> " + cm_src.getSNAME());
        addStep(file_name, cm_src);

        return RETURN_CONTINUE;
    }

    public int doTObj(int depth, CM_SRC cm_src, TObj tobj, long parent_object_id) {
        return RETURN_CONTINUE;
    }

    public int doTDpd(int depth, TDpd tdpd, CM_SRC cm_src, CM_OBJ cm_obj, int seq) throws SQLException {
        return RETURN_CONTINUE;
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



    public class ResultData{

        public String shellName;
        public String jclName;
        public String stepType;
        public String stepName;
        public int objLineNum;
        public String cmdType;
        public String cmdName;
        public String ddName;
        public String callTarget;
        public String assignData;
        public NCSDataType type;

        public ResultData(){}

    }

    public class NCSUtil{

        public HashMap<String,ArrayList<StepInfo>> dataList = new HashMap<String,ArrayList<StepInfo>>();

        public NCSUtil(){

        }



        public void log() {


            HashMap<String, Integer> fileInfo = new HashMap<String, Integer>();// file : cmd count
            HashMap<String, String> cmdList = new HashMap<String, String>(); // cmd list


            System.out.println("File Count >> " + dataList.entrySet().size());
            int cmds = 0;
            for(Map.Entry<String, ArrayList<StepInfo>> entry : dataList.entrySet()){
                String fileName = entry.getKey();
                int fileCmdCnt = 0;
                ArrayList<StepInfo> steps = entry.getValue();
                for(int i = 0 ; i < steps.size() ; i++){
                    cmds++;
                    fileCmdCnt++;
                    if(cmdList.containsKey(steps.get(i).cmdName)){

                    }else{
                        cmdList.put(steps.get(i).cmdName, "");
                    }

                }

                if(fileInfo.containsKey(fileName)){
                    Integer cn = fileInfo.get(fileName);
                    int newValue = fileCmdCnt + cn.intValue();
                    fileInfo.put(fileName, new Integer(newValue));
                    //System.out.println("Exists filename key");

                }else{
                    fileInfo.put(fileName, new Integer(fileCmdCnt));
                }

            }

            System.out.println("Command Count >> " + cmds);
            System.out.println("FileInfo >>>>>>>>>>>>>>>>>>");
            for(Map.Entry<String, Integer> entry : fileInfo.entrySet()){
                String fileName = entry.getKey();
                int cnt = entry.getValue().intValue();
                System.out.println("	" + fileName + " has " + cnt + "cmds");
            }

            System.out.println("CmdInfo >>>>>>>>>>>>>>>>>>");
            String cmdsStr = "	\n";
            int maxLen = 0;
            int lncnt = 10;
            for(Map.Entry<String, String> entry : cmdList.entrySet()){
                String cmdName = entry.getKey();

                if(maxLen < cmdName.length()){
                    maxLen = cmdName.length();
                    if(maxLen == 19){
                        System.out.println("************* " + cmdName);
                    }
                }

                cmdsStr += "  " + cmdName;
                lncnt--;

                if(lncnt == 0 ){
                    cmdsStr+="\n";
                    lncnt = 10;
                }


            }

            System.out.println("	Cmds >>>>>>>>>>>>> \n" + cmdsStr);
            System.out.println("	Cmds Max Len >>>>>>>>>>>>> " + maxLen);

        }

        public void add(String fileName, String stepName, String cmdName, ArrayList<NCSData> data){
            ArrayList<StepInfo> stepInfos = null;
            StepInfo stepInfo = new StepInfo(stepName, cmdName, data);
            if(dataList.containsKey(fileName)){
                stepInfos = dataList.get(fileName);
                stepInfos.add(stepInfo);
                //stepInfo.add(new StepInfo(stepName, cmdName, data));
                dataList.put(fileName, stepInfos);
            }else{
                stepInfos = new ArrayList<StepInfo>();
                stepInfos.add(stepInfo);
                dataList.put(fileName, stepInfos);
            }
        }

    }

    public enum NCSDataItem{
        COL1,COL2,
        SUB_SHELL,
        SEQ,
        JCLNAME,
        STEP,
        PGMID,
        IO,
        DD,
        DIR,
        DSN,
        COL12,COL13,COL14,COL15,COL16,COL17,COL18,COL19,COL20,COL21,
        MAINSHELL,
        COL23,COL24,COL25,
        TIMING //only length 26
    }

    public enum NCSDataType{
    	ONLY_NCS,
    	DATA_OK,
    	ONLY_CM,
    	DATA_NG
    }


    public class StepInfo{

        public String stepName = "";
        public String cmdName = "";
        public ArrayList<NCSData> ncsDatas = new ArrayList<NCSData>();

        public StepInfo(String step, String cmd, ArrayList<NCSData> datas){
            stepName = step;
            cmdName = cmd;
            ncsDatas = datas;
        }

    }

    public static class NCSData{

        public String lineData = "";

        private ArrayList<String> csvData = new ArrayList<String>();


        public NCSData(String lineStr){
            lineData = lineStr;
            init();
        }

        public void init(){
            if(!lineData.isEmpty()){
                String[] splited = lineData.split(",");
                for(int i = 0; i < splited.length ; i++){
                    csvData.add(splited[i]);
                }
            }
        }

        public String getCsvValue(NCSDataItem item){

            int index = item.ordinal();
            String value = "";

            if(csvData.size() > index){
                value = csvData.get(index);
            }

            return value;
        }

    }


}
