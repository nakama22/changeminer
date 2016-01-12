package changeminer.HandlerForEndAnalyzer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.itplus.cm.ce.internal.job.HandlerForEndAnalyzer;
import com.itplus.cm.ce.internal.job.JobInfo;
import com.itplus.cm.ce.util.DBUtil;
import com.itplus.cm.ce.util.FileUtil;
import com.itplus.cm.ce.util.LogManager;

public class CheckEndVB extends HandlerForEndAnalyzer
{
	PreparedStatement updatePstmt = null;

	public CheckEndVB() {

	}

	public String getName() {
		return this.getClass().getName();
	}

	public  void doPrePostTask(JobInfo job_info, LogManager log) {

	}

	public  void doAfterPostTask(JobInfo job_info, LogManager log) {

		Connection conn = null;
		try {
			conn = DBUtil.getConnection(true);
			makeInit(conn);
			checkDLLDpds(conn, job_info.getAnalyzerTypeID(), log);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

	}

	private void makeInit(Connection conn) throws Exception{
		StringBuffer SqlBuffer = new StringBuffer();

		SqlBuffer.append(" UPDATE CM_OBJ_DPD   ");
		SqlBuffer.append(" SET GID = ? ");
		SqlBuffer.append(" WHERE DPD_ID = ? ");
		updatePstmt = conn.prepareStatement(SqlBuffer.toString());

	}

	private void checkDLLDpds(Connection conn, long analyze_type_id , LogManager log) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int cnt = 0;
		try {
			StringBuffer SqlBuffer = new StringBuffer();

			SqlBuffer.append(" SELECT a.SNAME, b.DPD_ID, b.CALL_TARGET, b.GID   ");
			SqlBuffer.append(" FROM CM_SRC a, CM_OBJ_DPD b, CM_ANALYZE_TYPE ty, CM_ANALYZE_TYPE_FILTER fi ");
			SqlBuffer.append(" WHERE ty.ANALYZE_TYPE_ID = fi.ANALYZE_TYPE_ID ");
			SqlBuffer.append(" AND ty.ANALYZE_TYPE_ID = ? ");
			SqlBuffer.append(" AND a.ANALYZE_FILTER_ID = fi.ANALYZE_FILTER_ID  "); // -- Call Function, Call Sub
			SqlBuffer.append(" AND a.SRC_ID = b.SRC_ID ");
			SqlBuffer.append(" AND b.DPD_TYPE_ID IN (2511006,2511008) ");
			SqlBuffer.append(" AND b.DPD_PCT < 100 ");


			//AND b.DPD_TYPE_ID IN (2511006,2511008)
			//AND b.DPD_PCT < 100


			pstmt = conn.prepareStatement(SqlBuffer.toString());
			System.out.println(SqlBuffer.toString());
			System.out.println(" analyze_type_id = " + analyze_type_id);
			pstmt.setLong(1,  analyze_type_id );
			rs = pstmt.executeQuery();
			while(rs.next()) {

				long dpd_id = rs.getLong( "DPD_ID" );
				String callTarget = rs.getString("CALL_TARGET");
				System.out.println("cnt= " + cnt + ", DPD_ID = " + dpd_id + " CALL_TARGET = " + callTarget);
				cnt ++;

				ArrayList<Long> gids = getNewGid(dpd_id, callTarget);

				
				updateGid(conn, dpd_id, callTarget);



			}


		} catch(Exception e) {
			System.out.println(e.getMessage());
			throw new Exception(e);
		} finally {
			DBUtil.closeResource(null, pstmt, rs);
		}
	}

	private ArrayList<Long> getNewGid(long gid, String target){

		ArrayList<Long> newGids = new ArrayList<Long>();

		addCATSUI(newGids, target);
		
		

		return newGids;
	}
	
	//CommonResourceUtils.GetString(System.String)
	//UI\DLL\CATSUI\CORE\UI\Utils\Resources\CommonResourceUtils.vb(8,18)
	private long addCATSUI(ArrayList<Long> gids, String target){
		
		
		
		return 0;
	}
	
	 

	private void updateGid(Connection conn, long dpd_id, String target) throws Exception{


		updatePstmt.setLong(1,  FileUtil.getGID("<DB_TBL>", target) );
		updatePstmt.setLong(2,  dpd_id );
		int result = updatePstmt.executeUpdate();
		System.out.println("Update dpd id >> " + dpd_id);
		System.out.println("Update GID >> " + FileUtil.getGID("<VB>", target));


	}




}