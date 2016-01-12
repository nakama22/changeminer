/**
 *
 */
package changeminer.HandlerForRA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.itplus.cm.parser.common.CMParserCommonData;
import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.DBUtil;
import com.itplus.cm.ce.util.Environment;
import com.itplus.cm.ce.util.FileUtil;

import extractor.common.tobj.TDpd;
import extractor.common.tobj.TLocation;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;



/**
 *
 */
public class CPP_TO_C99_HANDLER extends HandlerForRA
{
    /**
     *
     */

	final int CPP_CALL_METHOD = 5121002;

	public static HashMap<String, ArrayList<String>> methodGids = null;

    public CPP_TO_C99_HANDLER() {

    }

    public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
        return RETURN_CONTINUE;
    }


    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {

    	if(methodGids == null){
    		ArrayList<String> ids = getAnalyzerRefs(cm_src.getANALYZE_FILTER_ID());

    		if(ids.size() != 0){
    			//HashMap<String, ArrayList<String>> gids = getC99Method(ids);
    			methodGids = getC99Method(ids);
    		}else{
    			methodGids = new HashMap<String, ArrayList<String>>();
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
        return 0L;
    }


    public long generateGID(String prefix, TDpd tdpd) {
    	if(tdpd.getType() == CPP_CALL_METHOD){

    		long newGid = 0L;
			int startIdx = tdpd.getGID().indexOf("|");
			System.out.println("gid >> " + tdpd.getGID());
			String methodName = "";

			if(startIdx < 0){
				methodName = tdpd.getGID();
			}else{
				methodName = tdpd.getGID().substring(startIdx+1);
			}

			System.out.println("method >> " + methodName);

			int startIdx2 = methodName.indexOf("(");
			String cppMehodName = "";

			if(startIdx2 < 0){
				cppMehodName = methodName;
			}else{
				cppMehodName = methodName.substring(0, startIdx2);
			}

			System.out.println("method2 >> " + cppMehodName);
			if(methodGids != null){
				if(methodGids.containsKey(cppMehodName)){

					ArrayList<String> gids = methodGids.get(cppMehodName);
					System.out.println("method3 >> " + gids.get(0));
					newGid = FileUtil.getGID("<EC>", gids.get(0));
				}else{
					System.out.println("method3 >> " + cppMehodName);
				}
			}

			return newGid;

		}
        return 0L;
    }


    public ArrayList<String> getAnalyzerRefs(long analyzeFilterId){

    	Connection conn = null;
		PreparedStatement psmt = null;
		ResultSet result = null;
		StringBuffer sb = new StringBuffer();

		ArrayList<String> refFilterIds = new ArrayList<String>();

        try {
			conn = DBUtil.getConnection(true);
			sb.append("\n SELECT  ");
			sb.append("\n   DISTINCT e.ANALYZE_TYPE_ID, f.REF_TYPE, g.aa REF_TYPE_ID, g.bb REF_TYPE_TITLE, g.cc REF_FILTER_ID ");
			sb.append("\n FROM  ");
			sb.append("\n   CM_SRC c, CM_ANALYZE_TYPE_FILTER d, CM_ANALYZE_TYPE e, CM_ANALYZE_TYPE_REF f, ");
			sb.append("\n   ( ");
			sb.append("\n     SELECT ");
			sb.append("\n       ca.ANALYZE_TYPE_ID aa, ca.ANALYZE_TYPE_TITLE bb, cf.ANALYZE_FILTER_ID cc ");
			sb.append("\n     FROM ");
			sb.append("\n       CM_ANALYZE_TYPE ca, CM_ANALYZE_TYPE_FILTER cf  ");
			sb.append("\n     WHERE ");
			sb.append("\n       ca.IS_DELETE = 'N' AND ");
			sb.append("\n       ca.ANALYZE_TYPE_ID = cf.ANALYZE_TYPE_ID ");
			sb.append("\n   ) g ");
			sb.append("\n WHERE ");
			sb.append("\n   c.ANALYZE_FILTER_ID = d.ANALYZE_FILTER_ID AND ");
			sb.append("\n   d.ANALYZE_TYPE_ID = e.ANALYZE_TYPE_ID AND ");
			sb.append("\n   d.ANALYZE_TYPE_ID = f.ANALYZE_TYPE_ID AND ");
			sb.append("\n   g.aa = f.REF_ANALYZE_TYPE_ID AND ");
			sb.append("\n   f.REF_TYPE = 3 AND ");
			sb.append("\n   c.ANALYZE_FILTER_ID = ? ");

			psmt = conn.prepareStatement(sb.toString());
			psmt.setLong(1, analyzeFilterId);


			result = psmt.executeQuery();

			while (result.next()) {
				refFilterIds.add(result.getString(5));
			}

        }catch(Exception e) {
        	e.printStackTrace();
       }finally{
         	try {
                DBUtil.closeResource(conn, psmt, result);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
       }

        return refFilterIds;

    }

    public HashMap<String,ArrayList<String>> getC99Method(ArrayList<String> filterIds){

    	Connection conn = null;
		PreparedStatement psmt = null;
		ResultSet result = null;
		StringBuffer sb = new StringBuffer();


		HashMap<String,ArrayList<String>> methodGids = new HashMap<String,ArrayList<String>>();
        try {
			conn = DBUtil.getConnection(true);
			sb.append("\n SELECT  ");
			sb.append("\n   a.SPATH, a.SNAME, a.COLLECT_ID, b.GID, b.OBJ_NAME, b.FULL_OBJ_NAME, t.TYPE_ID, t.TYPE_NAME ");
			sb.append("\n FROM  ");
			sb.append("\n   CM_SRC a, CM_OBJ b, CM_TYPE t, CM_COLLECT_INFO ci ");
			sb.append("\n WHERE ");
			sb.append("\n   a.CHANGE_REASON_CODE < 9000 AND ");
			sb.append("\n   a.SRC_ID = b.SRC_ID AND ");
			sb.append("\n   a.COLLECT_ID = ci.COLLECT_ID AND ");
			sb.append("\n   b.OBJ_TYPE_ID = t.TYPE_ID AND ");
			sb.append("\n   b.OBJ_TYPE_ID = 510004 AND ");
			sb.append("\n   a.ANALYZE_FILTER_ID IN ");


			for(int cnt = 0 ; cnt < filterIds.size() ; cnt++){
				if(cnt == 0){
					sb.append("(");

				}

				sb.append(filterIds.get(cnt));

				if(cnt+1 == filterIds.size()){
					sb.append(")");
				}else if(cnt < filterIds.size()){
					sb.append(",");
				}

			}

			psmt = conn.prepareStatement(sb.toString());

			result = psmt.executeQuery();

			ArrayList<String> subValues = new ArrayList<String>();
			while (result.next()) {

				int idx = result.getString(5).indexOf("(");
				String method = idx < 0 ? result.getString(5) : result.getString(5).substring(0, idx);
				String gid = result.getString(1) + result.getString(2) + "|" +method;


				if(methodGids.containsKey(method)){
					subValues = methodGids.get(method);
					subValues.add(gid);

					methodGids.remove(method);
					methodGids.put(method, subValues);
					System.out.println("method >> " + method + "  :  " + "value >> " + gid);
				}else{
					subValues.clear();
					subValues.add(gid);
					methodGids.put(method, subValues);
					System.out.println("method >> " + method + "  :  " + "value >> " + gid);
				}

			}

        }catch(Exception e) {
        	e.printStackTrace();
       }finally{
         	try {
                DBUtil.closeResource(conn, psmt, result);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
       }

        return methodGids;
    }
    
    


}
