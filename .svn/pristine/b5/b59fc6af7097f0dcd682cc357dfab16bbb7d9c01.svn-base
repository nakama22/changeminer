package changeminer.HandlerForCollector;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itplus.cm.ce.addon.common.custom.HandlerForCollector;
import com.itplus.cm.ce.util.DBUtil;
import com.itplus.cm.ce.util.FileUtil;
/**
 * Handler for Collector.
 *
 * @author Copyright (c) 2005, ITPlus Inc. All rights reserved.
 */
public class COLLECT_TABLE_FILES extends HandlerForCollector
{

    public COLLECT_TABLE_FILES() {
        // TODO Auto-generated constructor stub
    }

    public int doPreJobForCollect() {
    	Connection con = null;

    	try {
    		con = DBUtil.getConnection(false);
			File base_dir = new File(getBaseDir(con));
			String user_options = getUserOptions(con);

			log.debug("Collect Base Directory", base_dir);
			log.debug("User Options", user_options);
			if(user_options==null) {
				log.error("user options is not setted.", "", new IllegalArgumentException("user-define-option is not setted."));
				return 0;
			}
			if(base_dir.exists()==false) {
			} else {
			//	FileUtil.removeDirectory(base_dir);
				log.debug("remove dir", base_dir);
			//	base_dir.mkdirs();
			}
			// analyze_unit=23434, analyze_unit=23434
			String splits[] = user_options.split("[,;]");
			for(int i=0; i<splits.length;i++) {
				String o = splits[i].trim();
				if(o.length()==0) {
					continue ;
				}
				long analyze_type_id = 0L;
				if(o.indexOf("=")!=-1) {
					analyze_type_id = Long.parseLong(o.split("=")[1].trim());
				} else {
					analyze_type_id = Long.parseLong(o);
				}
				log.debug("Analyze type id", analyze_type_id+"");
				int compiler_code = getCompilerCode(con, analyze_type_id);



				// ZOS Cobol, MF Cobol
				if(compiler_code==1111 || compiler_code==1115 ) {
					makeTableFileForZosCobol(con, analyze_type_id, base_dir.getAbsolutePath());
				}
				// JCL
				else {
				     //makeDSNFileForJCL(con, analyze_type_id, base_dir.getAbsolutePath());
					 //makeDatFileForJCL(con, analyze_type_id, base_dir.getAbsolutePath());
				}
			}

		} catch (Exception e) {
			log.error("COLLECT_DATA_LAYOUT", "", e);
		} finally {
			if(con!=null) {
				DBUtil.closeResource(con, null, null);
			}
		}
    	return RETURN_CONTINUE ;
    }



	private void makeDatFileForZosCobol(Connection con, long analyze_type_id, String base_dir) throws SQLException, IOException {

		log.debug("makeDatFileForZosCobol", "start");

    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	try {
    		ps = con.prepareStatement("SELECT A.SRC_ID, A.SNAME, B.DATA_NAME, B.RECORD_NAME, B.CHECK_SUM, F.LINE_NUM "+
    				"FROM CM_ANALYZE_TYPE_FILTER  E, CM_SRC A, CM_OBJ_DPD C, CM_RECORD B, CM_OBJ_DPD_LINE F "+
    				"WHERE E.ANALYZE_TYPE_ID = ? "+
					"AND a.ANALYZE_FILTER_ID = e.ANALYZE_FILTER_ID " +
    				"AND A.SRC_ID = C.SRC_ID "+
    				"AND C.DPD_ID = B.DPD_ID "+
    				"AND F.DPD_ID = B.DPD_ID "+
    				"GROUP BY A.SRC_ID, A.SNAME, B.DATA_NAME, B.RECORD_NAME, B.CHECK_SUM, F.LINE_NUM  "+
    				"ORDER BY B.DATA_NAME, B.RECORD_NAME, A.SRC_ID, B.CHECK_SUM")
    				;

    		ps.setLong(1, analyze_type_id);
    		rs = ps.executeQuery();
    		String current_data_name = "";
			List dat_data = new ArrayList(); // RECORD_NAME
			while(rs.next()) {
				String src_id = rs.getString("SRC_ID");
				String check_sum = rs.getString("CHECK_SUM");
				String data_name = rs.getString("DATA_NAME");
				String record_name = rs.getString("RECORD_NAME");
				String sname = rs.getString("SNAME");
				int line = rs.getInt("LINE_NUM");

				data_name = getDFileName(sname, data_name);

				if(current_data_name.equals(data_name)) {
				} else if("".equals(current_data_name)==false){
					saveDatFile(base_dir, dat_data, current_data_name);
					dat_data.clear();
				}
				if(dat_data.contains(src_id + "/" + record_name + "/" + check_sum + "/" + sname + "/" + line)==false) {
					dat_data.add(src_id + "/" + record_name + "/" + check_sum + "/" + sname + "/" + line);
				}
				current_data_name = data_name;
    		}
			// �������� ����� �Ƌf�Ԑ��f�m....
			if("".equals(current_data_name)==false) {
				saveDatFile(base_dir, dat_data, current_data_name);
			}
    	} catch(SQLException e) {
    		log.error("COLLECT_DATA_LAYOUT", "", e);
    	} finally {
    		DBUtil.closeResource(ps, rs);
    	}
	}

	private void makeTableFileForZosCobol(Connection con, long analyze_type_id, String base_dir) throws SQLException, IOException {

		log.debug("makeDatFileForZosCobol", "start");

    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	try {
    		ps = con.prepareStatement("SELECT A.SRC_ID, A.SNAME, C.CALL_TARGET, C.DPD_TYPE_ID, F.LINE_NUM "+
    								  "FROM "+
    								  "CM_ANALYZE_TYPE_FILTER  E, CM_SRC A, CM_OBJ_DPD C, CM_OBJ_DPD_LINE F "+
    								  "WHERE "+
    								  "E.ANALYZE_TYPE_ID = ? "+
    								  "AND a.ANALYZE_FILTER_ID = e.ANALYZE_FILTER_ID "+
    								  "AND A.SRC_ID = C.SRC_ID "+
    								  "AND c.DPD_ID = f.DPD_ID "+
    								  "AND c.DPD_TYPE_ID = 1001001 "+
    								  "GROUP BY A.SRC_ID, A.SNAME, C.CALL_TARGET, C.DPD_TYPE_ID, F.LINE_NUM "+
    								  "ORDER BY A.SRC_ID");
    				;

    		ps.setLong(1, analyze_type_id);
    		rs = ps.executeQuery();

    		HashMap<String, ArrayList<String>> tables = new HashMap<String, ArrayList<String>>();

    		Pattern p = Pattern.compile("([.+])\\.([.+])");

			while(rs.next()) {
				String target = rs.getString("CALL_TARGET");

				Matcher m = p.matcher(target);

				if (m.find()){
					String schema = m.group(1);//splitedStr[0];
					String table = m.group(2);//splitedStr[1];
					String fileName = schema.replaceAll("\\s", "_").replaceAll("\\*", "#");
					System.out.println("Check Data >> " + schema  + " : " + table +" : " + fileName);

					ArrayList<String> sub = null;
					if(tables.containsKey(fileName)){
						sub = tables.get(fileName);
						if(!sub.contains(table)){
							sub.add(table);
							//tables.replace(fileName, sub);
							tables.remove(fileName);
							tables.put(fileName, sub);
							System.out.println("	Add >> " + fileName + "  :  " + table);
						}
					}else{
						sub = new ArrayList<String>();
						sub.add(table);
						tables.put(fileName, sub);
						System.out.println("	Add >> " + fileName + "  :  " + table);
					}

				}else{

					System.out.println("splitedStr length is not 2 >> " + target);
				}




				for(Map.Entry<String, ArrayList<String>> e : tables.entrySet()) {

					String key = e.getKey();
					ArrayList<String> values = e.getValue();

					StringBuffer buffer = new StringBuffer();
					buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
					buffer.append("<SCHEMA xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n\n");

					for(int i=0; i<values.size(); i++) {



						String table = values.get(i);
						buffer.append("\t<TABLE "
								+ "name=\""+ table +"\" "
								+ "/>\n");




					}

					buffer.append("\n</SCHEMA>");
					//String tbfFile = base_dir + "/" + key + "." + values.get(i);
					//FileUtil.writeToFile(buffer.toString(), "utf-8", tbfFile);

				}

    		}



    	} catch(SQLException e) {
    		log.error("COLLECT_DATA_LAYOUT", "", e);
    	} finally {
    		DBUtil.closeResource(ps, rs);
    	}
	}


	private String getDFileName(String cbl, String data_name) {
		String name = "";
		if(cbl.indexOf(".")!=-1) {
			name += cbl.substring(0, cbl.lastIndexOf(".")) ;
		} else {
			name += cbl;
		}
		return name + "." + data_name ;
	}

	public static void main(String[] args) {
		String sname = "abcd.ef.txt";
		System.out.println(sname.substring(0, sname.lastIndexOf(".")));
	}


	private void saveDatFile(String baseDir, List dat_data, String data_name) throws IOException {
    	StringBuffer sb = new StringBuffer();
    	sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
    	sb.append("<FILE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n");
//    	for(int i=0; i<dat_data.size(); i++) {
//    		// src_id + "/" + record_name + "/" + check_sum
//    		String splits[] = dat_data.get(i).toString().split("/");
//    		sb.append("\t<RELATED_COBOL_SRC_ID "
//    				+ "src_id=\""+ splits[0] +"\" "
//    				+ "record_file=\""+ splits[1] +"\" "
//    				+ "record_src_id=\""+ 0 +"\" "
//    				+ "record_id=\""+ splits[2] +"\" "
//    				+ "sname=\""+ splits[3] +"\" "
//    				+ "line_num=\""+ splits[4] +"\" "
//    						+ "/>\n");
//    	}
    	sb.append("</FILE>\n");
    	log.debug("making DAT file", baseDir + "/" + data_name + ".DAT");
		FileUtil.writeToFile(sb.toString(), "utf-8", baseDir + "/" + data_name + ".DAT");
	}
	private String conv_dsn_name(String str) {
		return  str.replaceAll("[\\\\/:\\*\\?\"<>|]", "-");
	}
	private void makeDSNFileForJCL(Connection con, long analyze_type_id, String baseDir) throws SQLException, IOException {
		String query = "SELECT B.SRC_ID, B.SNAME, D.META_NAME, C.CALL_TARGET DSNNAME, D.META_VALUE DDNAME, G.META_VALUE CALL_PGM, H.META_VALUE CALL_PROC, I.LINE_NUM "+
				"FROM CM_ANALYZE_TYPE_FILTER A, CM_SRC B, CM_OBJ_DPD C, CM_OBJ_DPD_META D, CM_OBJ_DPD F, CM_OBJ_DPD_META G, CM_OBJ_DPD_META H, CM_OBJ_DPD_LINE I  "+
				"WHERE A.ANALYZE_TYPE_ID = ? "+
				"AND B.ANALYZE_FILTER_ID = A.ANALYZE_FILTER_ID "+
				"AND C.SRC_ID = B.SRC_ID  "+
				"AND D.DPD_ID(+) = C.DPD_ID  "+
				"AND D.META_NAME(+) = 1311567  "+
				"AND F.CALLER_OBJ_ID(+) = C.CALLER_OBJ_ID "+
				"AND F.DPD_TYPE_ID(+) = 1300008 "+
				"AND F.CALL_TARGET(+) = C.CALL_TARGET "+
				"AND G.DPD_ID(+) = F.DPD_ID  "+
				"AND G.META_NAME(+) = 1311569 "+
				"AND H.DPD_ID(+) = F.DPD_ID "+
				"AND H.META_NAME(+) = 1311570  "+
				"AND I.DPD_ID = F.DPD_ID " +
				"GROUP BY B.SRC_ID, B.SNAME, D.META_NAME, C.CALL_TARGET , D.META_VALUE , G.META_VALUE, H.META_VALUE, I.LINE_NUM "+
				"ORDER BY  C.CALL_TARGET "
			;
		PreparedStatement ps = null;
		ResultSet rs = null;

		// DSN Map �m��.
		Map dsnMap = new TreeMap();
		try {
			ps = con.prepareStatement(query);
			ps.setLong(1, analyze_type_id);
			rs = ps.executeQuery();

			String current_dsn = "";
			while(rs.next()) {
				DSN dsn = new DSN();
				dsn.SRC_ID = rs.getLong("SRC_ID");

				//if(dsn.SRC_ID == 26356) { continue; }

				dsn.SNAME = rs.getString("SNAME");
				dsn.LINE_NUM = rs.getInt("LINE_NUM");
				dsn.DSNNAME = rs.getString("DSNNAME");
				dsn.DDNAME = rs.getString("DDNAME");
				dsn.CALL_PGM = rs.getString("CALL_PGM");
				dsn.CALL_PROC = rs.getString("CALL_PROC");

				/*
				System.out.println("************************************");
				System.out.println("SRC_ID >> " + dsn.SRC_ID);
				System.out.println("LINE_NUM >> " + dsn.LINE_NUM);
				System.out.println("DSNNAME >> " + dsn.DSNNAME);
				System.out.println("DDNAME >> " + dsn.DDNAME);
				System.out.println("CALL_PGM >> " + dsn.CALL_PGM);
				System.out.println("CALL_PROC >> " + dsn.CALL_PROC);
				*/

				List list = null ;
				if(dsnMap.containsKey(dsn.DSNNAME)) {
					list = (List) dsnMap.get(dsn.DSNNAME);
				} else {
					list = new ArrayList();
					dsnMap.put(dsn.DSNNAME, list);
				}
				list.add(dsn);
			}

		} catch(SQLException e) {
			throw e;
		} finally {
			DBUtil.closeResource(ps, rs);
		}




		// DSN �͌m ����.
		Iterator iter = dsnMap.keySet().iterator();
		while(iter.hasNext()) {
			String dsnname = (String) iter.next();
			List list = (List) dsnMap.get(dsnname);
			StringBuffer buffer = new StringBuffer();
			buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
			buffer.append("<DSN xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n\n");
			for(int i=0; i<list.size(); i++) {
				DSN dsn = (DSN) list.get(i);
				buffer.append("\t<JCL_MAPPING "
						+ "src_id=\""+ dsn.SRC_ID +"\" "
						+ "sname=\""+ dsn.SNAME +"\" "
						+ "line_num=\""+ dsn.LINE_NUM+"\" "
						+ "call_pgm=\""+ (dsn.CALL_PGM==null?"":dsn.CALL_PGM) +"\" "
						+ "call_proc=\"" + (dsn.CALL_PROC==null?"":dsn.CALL_PROC) + "\" "
						+ "dsnname=\""+ dsn.DSNNAME +"\" "
						+ "ddname=\""+ (dsn.DDNAME==null?"":dsn.DDNAME) +"\" "
						+ "/>\n");
			}
			buffer.append("\n</DSN>");
			String dsn_file = baseDir + "/" + conv_dsn_name(dsnname) + ".DSN";

			//System.out.println("************************************Dsn Map Check");
			//System.out.println(buffer.toString());


			log.debug("make DAT", dsn_file);

			FileUtil.writeToFile(buffer.toString(), "utf-8", dsn_file);
		}

	}

	private class DSN {
		long SRC_ID = 0;
		String SNAME = null;
		int LINE_NUM = 0;
		String DSNNAME = null;
		String DDNAME = null;
		String CALL_PGM = null;
		String CALL_PROC = null;
	}

	private void makeDatFileForJCL(Connection con, long analyze_type_id, String baseDir) throws SQLException, IOException {

		log.debug("makeDatFileForJCL", "start");

		// DAT �͐� �ʍk �W��n�� ������ �Ȑ�.
		PreparedStatement ps = null;
		ResultSet rs = null;

		ResultSet rs_use_dsn = null;
		// CBL �� �Ԋ� �䐌
		PreparedStatement ps_use_dsn_1 = null;
		// CBL �� ���� �䐌
		PreparedStatement ps_use_dsn_2 = null;


		try {
//			String query = " SELECT D.META_VALUE "+
//					"FROM CM_ANALYZE_TYPE_FILTER A, CM_SRC B, CM_OBJ_DPD C, CM_OBJ_DPD_META D "+
//					"WHERE A.ANALYZE_TYPE_ID = ?  "+
//					"AND B.ANALYZE_FILTER_ID = A.ANALYZE_FILTER_ID "+
//					"AND C.SRC_ID = B.SRC_ID "+
//					"AND D.DPD_ID = C.DPD_ID "+
//					"AND D.META_NAME = 1311567 "+
//					"GROUP BY D.META_VALUE"
//					;
			// 1300007 �ܓW�� ����T USE DD �K���� �͐� �ԏ�� �g�׈� ...
//			String query = "SELECT C.CALLER_OBJ_ID, D.META_VALUE CBL, C.CALL_TARGET TGT, B.SRC_ID, B.SNAME "+
//					"FROM CM_ANALYZE_TYPE_FILTER A, CM_SRC B, CM_OBJ_DPD C, CM_OBJ_DPD_META D  "+
//					"WHERE A.ANALYZE_TYPE_ID = ? "+
//					"AND B.ANALYZE_FILTER_ID = A.ANALYZE_FILTER_ID "+
//					"AND C.SRC_ID = B.SRC_ID  "+
//					"AND C.DPD_TYPE_ID = 1300007 "+
//					"AND D.DPD_ID(+) = C.DPD_ID  "+
//					"AND D.META_NAME(+) IN (1311569, 1311570) "+
//					"GROUP BY C.CALLER_OBJ_ID, D.META_VALUE , C.CALL_TARGET , B.SRC_ID, B.SNAME "+
//					"ORDER BY C.CALL_TARGET "
					;
            String query = "SELECT C.CALLER_OBJ_ID, D.META_VALUE CBL, C.CALL_TARGET TGT, B.SRC_ID, B.SNAME " +
                            "  FROM CM_ANALYZE_TYPE_FILTER A, CM_SRC B, CM_OBJ_DPD C, " +
                            "       (SELECT DPD_ID, META_NAME, META_VALUE " +
                            "          FROM CM_OBJ_DPD_META " +
                            "          WHERE META_NAME IN (1311569, 1311570)) D " +
                            " WHERE A.ANALYZE_TYPE_ID = ? " +
                            "          AND B.ANALYZE_FILTER_ID = A.ANALYZE_FILTER_ID " +
                            "   AND C.SRC_ID = B.SRC_ID " +
                            "   AND C.DPD_TYPE_ID = 1300007 " +
                            "   AND D.DPD_ID(+) = C.DPD_ID " +
                            " GROUP BY C.CALLER_OBJ_ID, D.META_VALUE , C.CALL_TARGET , B.SRC_ID, B.SNAME " +
                            " ORDER BY C.CALL_TARGET ";


			String q_use_dsn_1 = "SELECT A.SRC_ID, A.CALL_TARGET DSN, D.LINE_NUM, C.META_NAME PGM_OR_PROC "+
					"FROM CM_OBJ_DPD A, CM_OBJ_DPD_META B, CM_OBJ_DPD_META C, CM_OBJ_DPD_LINE D "+
					"WHERE A.CALLER_OBJ_ID = ?  "+
					"AND A.DPD_TYPE_ID = 1300008 "+
					"AND B.DPD_ID = A.DPD_ID "+
					"AND B.META_NAME = 1311567 "+
					"AND B.META_VALUE = ? "+
					"AND C.DPD_ID = B.DPD_ID "+
					"AND C.META_NAME IN (1311569, 1311570) "+
					"AND C.META_VALUE = ? "+
					"AND C.DPD_ID = D.DPD_ID"
					;
			String q_use_dsn_2 = "SELECT A.SRC_ID, A.CALL_TARGET DSN, D.LINE_NUM, 0 PGM_OR_PROC "+
					"FROM CM_OBJ_DPD A, CM_OBJ_DPD_META B, CM_OBJ_DPD_LINE D "+
					"WHERE A.CALLER_OBJ_ID = ? "+
					"AND A.DPD_TYPE_ID = 1300008 "+
					"AND B.DPD_ID = A.DPD_ID "+
					"AND B.META_NAME = 1311567 "+
					"AND B.META_VALUE = ? "+
					"AND A.DPD_ID = D.DPD_ID"
					;

			ps = con.prepareStatement(query);
			ps_use_dsn_1 = con.prepareStatement(q_use_dsn_1);
			ps_use_dsn_2 = con.prepareStatement(q_use_dsn_2);

			ps.setLong(1, analyze_type_id);
			System.out.println("sql="+query);
			System.out.println("id="+analyze_type_id);
			rs = ps.executeQuery();
			// �v�� �Č@�l ���� �Z����.

			while(rs.next()) {

				String cbl = rs.getString("CBL") ;
				String tgt = rs.getString("TGT");
				long caller_obj_id = rs.getLong("CALLER_OBJ_ID");
				long src_id = rs.getLong("SRC_ID");
				String sname = rs.getString("SNAME");

				String dat_file_name = baseDir + "/"
							+ (cbl==null ? "" : cbl +".")
						+ tgt
						+ ".DAT";
				File dat_file = new File(dat_file_name);
				List contents = new ArrayList();
				contents.add("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
				contents.add("<FILE xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");

				if(dat_file.exists()==true) {
					readDsnMappingFromFile(contents, dat_file);
					dat_file.delete();
				}

				// Use DSN // DSN_MAPPING
				if(cbl==null||cbl.length()==0) {
					ps_use_dsn_2.clearParameters();
					ps_use_dsn_2.setLong(1, caller_obj_id);
					ps_use_dsn_2.setString(2, tgt);
					rs_use_dsn = ps_use_dsn_2.executeQuery();
				} else {
					ps_use_dsn_1.clearParameters();
					ps_use_dsn_1.setLong(1, caller_obj_id);
					ps_use_dsn_1.setString(2, tgt);
					ps_use_dsn_1.setString(3, cbl);
					rs_use_dsn = ps_use_dsn_1.executeQuery();
				}
				while(rs_use_dsn.next()) {
					String dsn = rs_use_dsn.getString("DSN");
					int line_num = rs_use_dsn.getInt("LINE_NUM");
					long pgm_or_proc = rs_use_dsn.getLong("PGM_OR_PROC");
					String call_pgm = "";
					String call_proc = "";

					if(pgm_or_proc==1311569) {
						call_pgm = cbl;
					} else if(pgm_or_proc==1311570) {
						call_proc = cbl;
					}
					String dsn_mapping = "\t" + "<DSN_MAPPING "
							+ "src_id=\"" + src_id + "\" "
							+ "sname=\"" + sname + "\" "
							+ "line_num=\"" + line_num + "\" "
							+ "call_pgm=\"" + call_pgm + "\" "
							+ "call_proc=\"" + call_proc +"\" "
							+ "dsnname=\"" + dsn + "\" "
							+ "ddname=\"" + tgt + "\" />"
							;
					if(contents.contains(dsn_mapping)==false) {
						contents.add(dsn_mapping);
					}
				}
				rs_use_dsn.close();

				contents.add("</FILE>");
				StringBuffer sb = new StringBuffer();
				for(int i=0; i<contents.size(); i++) {
					sb.append(contents.get(i) + "\n");
				}
				log.debug("make DAT", dat_file_name);
				try {

					if(new File(dat_file_name).exists()){
						new File(dat_file_name).delete();
					}
					FileUtil.writeToFile(sb.toString(), "utf-8", dat_file_name);
					/*
					FileUtil.writeToFile(sb.toString(), "utf-8", dat_file_name);

					String testFile =  dat_file_name;
					File ts = new File(testFile);
					while(ts.exists()){
						testFile += "1";
						ts = new File(testFile);
					}

					FileUtil.writeToFile(sb.toString(), "utf-8", testFile);
					*/

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch(SQLException e) {
			throw e;
		} finally {
			DBUtil.closeResource(ps, rs);
			DBUtil.closeResource(ps_use_dsn_1, rs_use_dsn);
			DBUtil.closeResource(ps_use_dsn_2, null);
		}

	}


	private void readDsnMappingFromFile(List contents, File dat_file) throws IOException {
		String lines[] = FileUtil.readFromFile(dat_file.getAbsolutePath(), "utf-8").split("\n");
		for(int i=0; i<lines.length; i++) {
			String line = lines[i].trim();
			if(line.trim().startsWith("<DSN_MAPPING")) {
				contents.add("\t" + line);
			}
		}
	}

	private String getUserOptions(Connection con) throws SQLException {
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	try {
    		ps = con.prepareStatement("SELECT OPTION_VALUE "
    				+ "FROM CM_COLLECT_OPTION "
    				+ "WHERE COLLECT_ID = ? "
    				+ "AND OPTION_NAME = 'user-define-option'");

    		ps.setLong(1, this.target_id);
    		rs = ps.executeQuery();
    		if(rs.next()){
    			return rs.getString("OPTION_VALUE");
    		}
    	} catch(SQLException e) {
    		throw e;
    	} finally {
    		DBUtil.closeResource(ps, rs);
    	}

		return null;
	}

	private String getBaseDir(Connection con) throws SQLException {
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	try {
    		ps = con.prepareStatement("SELECT OPTION_VALUE "
    				+ "FROM CM_COLLECT_OPTION "
    				+ "WHERE COLLECT_ID = ? "
    				+ "AND OPTION_NAME = 'base-directory'");

    		ps.setLong(1, this.target_id);
    		rs = ps.executeQuery();
    		if(rs.next()){
    			return rs.getString("OPTION_VALUE");
    		}
    	} catch(SQLException e) {
    		throw e;
    	} finally {
    		DBUtil.closeResource(ps, rs);
    	}

		return null;
	}

	private int getCompilerCode(Connection con, long analyze_type_id) throws SQLException {
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	try {
    		ps = con.prepareStatement("SELECT COMPILER_CODE "
    				+ "FROM CM_ANALYZE_TYPE "
    				+ "WHERE ANALYZE_TYPE_ID = ? ");

    		ps.setLong(1, analyze_type_id);
    		rs = ps.executeQuery();
    		if(rs.next()){
    			return rs.getInt("COMPILER_CODE");
    		}
    	} catch(SQLException e) {
    		throw e;
    	} finally {
    		DBUtil.closeResource(ps, rs);
    	}

		return 0;
	}

	public String getName() {
        return this.getClass().getName();
    }
}