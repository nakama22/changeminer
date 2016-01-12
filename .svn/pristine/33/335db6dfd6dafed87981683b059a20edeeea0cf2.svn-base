
package changeminer.HandlerForRA;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.regex.Pattern;

import com.itplus.cm.parser.common.CMParserCommonData;
import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.Environment;
import extractor.common.tobj.TDpd;
import extractor.common.tobj.TLocation;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;
import com.itplus.cm.ce.util.FileUtil;

/**
 *
 */
public class SqlFileAnalyze extends HandlerForRA{

	/**
     *
     */
    public SqlFileAnalyze() {

    }

   /**
	*
	**/
    public String getName() {
    	return this.getClass().getName();
    }

    /**
    *  ���? �۾��� �?�� �����Դϴ�.
    **/
    public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
        return RETURN_CONTINUE;
    }

   /**
	*  ���� �۾��� �?�� �����Դϴ�.
	**/
    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
		String file_name = Environment.getSourceDir() + "/" + cm_src.getCOLLECT_ID() + cm_src.getSPATH() + cm_src.getSNAME();
		FileInputStream fis = null;
		BufferedReader br = null;
		TDpd o;
		TObj[] tobj_root = tresult.getTObjList();
		TObj obj_root = tresult.getTObjList()[0];
		StringBuffer buff = new StringBuffer();
		int sqlLine = 0;
		try {
			fis = new FileInputStream(file_name);
			br = new BufferedReader(new InputStreamReader(fis));
			String line_data = "";
			String insertData = "";
			String crudStr = "";
			int lineNum = 0;
			boolean sqlFlg = false;
			while (br.ready()) {
			//	if (line_data == null) continue;
				line_data = br.readLine();

	            lineNum++ ;

				if(!sqlFlg) {
					if(line_data.indexOf("SELECT") >= 0 ||
					line_data.indexOf("INSERT") >= 0 ||
					line_data.indexOf("UPDATE") >= 0 ||
					line_data.indexOf("DELETE") >= 0) {
						sqlFlg = true;
						sqlLine = lineNum;// SQL���̕��͏��A���C�����
					}
				}

				// SQL�����쐬
				if(sqlFlg) {
					Pattern p = Pattern.compile("\\s+");
					String[] gArr = p.split(line_data.trim());
					String tempData = "";
					for(int i=0; i< gArr.length; i++) {
		//				tempData = gArr[i];
						//if(gArr[i].indexOf("ALL")>=0) gArr[i]= gArr[i].replaceAll("ALL", " ");
						if(gArr[i].indexOf("/")>=0) gArr[i] = gArr[i].replaceAll(".*/", " ");
						tempData = tempData  + " " + gArr[i];
					//	System.out.println(gArr[i]);
					}

	            	buff.append(" "+tempData);
				}
			}
			System.out.println(buff.toString());
		} catch(Exception e) {
			e.printStackTrace();
		}
		obj_root.add(new TDpd(1009001, buff.toString(), buff.toString(),100, new TLocation(sqlLine)));// lineNum ���C�����
       return RETURN_CONTINUE;

    }

   /**
	*
	**/
    public int doTObj(int depth, CM_SRC cm_src, TObj tobj, long parent_object_id) {
        log.trace("HANDLER", depth + " : " + tobj.getName() + " : " + tobj.getTempMap());

        return RETURN_CONTINUE;
    }

   /**
	*
	**/
    public int doTDpd(int depth, TDpd tdpd, CM_SRC cm_src, CM_OBJ cm_obj, int seq) throws SQLException {
        log.trace("HANDLER", depth + " : " + tdpd.getName() + " : " + tdpd.getTempMap());
        return RETURN_CONTINUE;
    }

   /**
	*
	**/
    public TResult getTTresult() {
        // TODO Auto-generated method stub
        return null;
    }

   /**
	*
	**/
    public long generateGID(String prefix, TObj tobj) {
    	System.out.println("+++++ obj  name +++++"+tobj.getName()+"+++++++++++++ type ++++++++"+tobj.getType());
		return FileUtil.getGID("<SQL>",tobj.getGID());

       // return 0L;
    }

   /**
	*
	**/
    public long generateGID(String prefix, TDpd tdpd) {
       	System.out.println("+++++ dpd  name +++++"+tdpd.getName()+"+++++++++++++ type ++++++++"+tdpd.getType());
        return 0L;
    }

	/**
	*
	**/
    public String getObjName(boolean is_file, CM_SRC cm_src, String full_object_name)
    {
    	return null;
    }
}