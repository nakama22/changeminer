package changeminer.HandlerForEndAnalyzer;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.itplus.cm.ce.internal.job.HandlerForEndAnalyzer;
import com.itplus.cm.ce.internal.job.JobInfo;
import com.itplus.cm.ce.util.DBUtil;
import com.itplus.cm.ce.util.Environment;
import com.itplus.cm.ce.util.FileUtil;
import com.itplus.cm.ce.util.LogManager;

public class NS_REPORT_EXPORT extends HandlerForEndAnalyzer
{
	PreparedStatement updatePstmt = null;
	HashMap<String, Boolean> exportReport = new HashMap<String, Boolean>();
	HashMap<String, String> exportReportQuery = new HashMap<String, String>();
	String rootPath = Environment.getSourceDir() + "/ReportExport/";
	String sqlPath = Environment.getSourceDir() + "/ReportExport/Sql/";

	public NS_REPORT_EXPORT() { }

	public void initTargetReport(){
		exportReport.put("ShellFile_Assign_List(Full).csv", true);
		exportReportQuery.put("ShellFile_Assign_List(Full).csv", "ShellFile_Assign_List(Full).sql");

	}


	public  void doPrePostTask(JobInfo job_info, LogManager log) {

		initTargetReport();

		File rootDirectory = new File(rootPath);
		if(!rootDirectory.exists()){
			rootDirectory.mkdir();
		}
		File rootSqlDirectory = new File(sqlPath);
		if(!rootSqlDirectory.exists()){
			rootSqlDirectory.mkdir();
		}

		for(Map.Entry<String, Boolean> entry : exportReport.entrySet()){
            String reportFile = entry.getKey();
            boolean isExport = entry.getValue();

            if(isExport){
            	File exportFile = new File(rootPath + reportFile);
            	if(exportFile.exists()){
            		exportFile.delete();
            	}


            }

        }
	}


	public  void doAfterPostTask(JobInfo job_info, LogManager log) {

		Connection conn = null;
		try {
			conn = DBUtil.getConnection(true);
			initStatement(conn);




			checkTableDpds(conn, job_info.getAnalyzerTypeID(), log);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if(conn != null){
				try {
					if(!conn.isClosed()){
						conn.close();
					}
				} catch (SQLException e) {

				}
			}
		}

		conn = null;

	}

	private void initStatement(Connection conn) throws Exception{

		/*
		StringBuffer SqlBuffer = new StringBuffer();

		SqlBuffer.append(" UPDATE CM_OBJ_DPD   ");
		SqlBuffer.append(" SET GID = ? ");
		SqlBuffer.append(" WHERE DPD_ID = ? ");
		updatePstmt = conn.prepareStatement(SqlBuffer.toString());
		*/
	}

	private void initSqlString(){

	}

	private String readSql(String sqlPath){

		return "";
	}

	private void checkTableDpds(Connection conn, long analyze_type_id , LogManager log) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int cnt = 0;
		try {
			StringBuffer SqlBuffer = new StringBuffer();

			SqlBuffer.append(" SELECT a.SNAME, b.DPD_ID, b.CALL_TARGET, b.GID   ");
			SqlBuffer.append(" FROM CM_SRC a, CM_OBJ_DPD b, CM_ANALYZE_TYPE ty, CM_ANALYZE_TYPE_FILTER fi ");
			SqlBuffer.append(" WHERE ty.ANALYZE_TYPE_ID = fi.ANALYZE_TYPE_ID ");
			SqlBuffer.append(" AND ty.ANALYZE_TYPE_ID = ? ");
			SqlBuffer.append(" AND a.ANALYZE_FILTER_ID = fi.ANALYZE_FILTER_ID  "); // -- Call Function
			SqlBuffer.append(" AND a.SRC_ID = b.SRC_ID ");
			SqlBuffer.append(" AND b.DPD_TYPE_ID = 1001001 ");


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
				updateGid(conn, dpd_id, callTarget);



			}


		} catch(Exception e) {
			System.out.println(e.getMessage());
			throw new Exception(e);
		} finally {
			DBUtil.closeResource(null, pstmt, rs);
		}
	}

	private void updateGid(Connection conn, long dpd_id, String target) throws Exception{


		updatePstmt.setLong(1,  FileUtil.getGID("<DB_TBL>", target) );
		updatePstmt.setLong(2,  dpd_id );
		int result = updatePstmt.executeUpdate();
		System.out.println("Update dpd id >> " + dpd_id);
		System.out.println("Update GID >> " + FileUtil.getGID("<DB_TBL>", target));


	}






}