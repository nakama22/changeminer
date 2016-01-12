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
public class DB_FILE_HANDLER extends HandlerForRA
{
    private final int DB_FILE_TABLE_OBJ = 820001;
    public DB_FILE_HANDLER() {}
    public String getName() {return this.getClass().getName();}

    public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
        return RETURN_CONTINUE;
    }

    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {

    	/*
    	String file_name = Environment.getSourceDir() + cm_src.getCOLLECT_ID() + cm_src.getSPATH() + cm_src.getSNAME();
    	String convFileName = cm_src.getSNAME().replace(".tbf", "").replaceAll("_", " ").replaceAll("#", "*");
		TObj obj_root = tresult.getTObjList()[0];

		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(file_name)));
			String lineStr = null;
			int cnt = 0;

			Pattern p = Pattern.compile("<TABLE\\sname=\\\"([^\\s]+)\\\"");

			while((lineStr = br.readLine()) != null){

				cnt++;

				Matcher m = p.matcher(lineStr);

				if (m.find())
				{
					String table = m.group(1);

					TObj tbl = new TObj(DB_FILE_TABLE_OBJ, convFileName+"."+table, table, 100, new TLocation(cnt));
					obj_root.add(tbl);

				}


			}

			br.close();

		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		*/
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
    	if(tobj.getType() == DB_FILE_TABLE_OBJ){
    		return FileUtil.getGID("<TB>", "/TB/"+tobj.getGID());


    	}

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
