
package changeminer.HandlerForRA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.Environment;
import com.itplus.cm.ce.util.FileUtil;
import com.itplus.cm.parser.common.CMParserCommonData;

import extractor.common.tobj.TDpd;
import extractor.common.tobj.TLocation;
import extractor.common.tobj.TMeta;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;

/**
 *
 */
public class NS_CTL_HANDLER extends HandlerForRA {

	private final int USE_SQL = 1009001;
	private final int CTL_FILE = 821151;
	private final int GENERAL_FILE = 820001;

	public NS_CTL_HANDLER() {}

	public String getName() {
		return this.getClass().getName();
	}


	public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
		return RETURN_CONTINUE;
	}


	public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {

		TObj rootObj = tresult.getTObjList()[0];
		TObj root_obj = tresult.getTObjList()[0];
		String file_name = tresult.getTObjList()[0].getName();

		if(file_name.endsWith(".sql")) { return RETURN_CONTINUE; }

		addStep(file_name, root_obj);

		return RETURN_CONTINUE;
	}

	private void addStep(String file_name, TObj root_obj) throws Exception{

		FileInputStream fis = null;
		File file = new File(file_name);
		fis = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line_data = new String();
		TDpd obj_TDpd = null;
		String tblNm = null;
		int line_no = 0;
		int dpdLineNum = 0;
		boolean bCol = false;
		StringBuffer sbSql = new StringBuffer();

		Pattern p1 = Pattern.compile("INTO TABLE(.*)");


		while (br.ready()) {
			line_data = br.readLine();
			line_no++;
			Matcher m1 = p1.matcher(line_data);

			if (m1.find()) {
				tblNm = m1.group(1);
				sbSql.append("Insert Into ");
				sbSql.append(tblNm);
				dpdLineNum = line_no;
			}

			if (line_data.startsWith("(")) {

				bCol = true;
				sbSql.append("(");
				continue;
			}

			if (bCol) {

				Pattern p2 = Pattern.compile("\\b(.*)\\s(POSITION|FILLER|CONSTANT|SYSDATE|EXPRESSION)");
				Matcher m2 = p2.matcher(line_data);
				Pattern p3 = Pattern.compile("\\b(.*)\\s(CHAR|DATE)");
				Matcher m3 = p3.matcher(line_data);
				Pattern p4 = Pattern.compile("\\b(.*)\\s(\")");
				Matcher m4 = p4.matcher(line_data);

				if (m2.find()) {
					sbSql.append(m2.group(1).trim());
					sbSql.append(",");

				} else if (m3.find()) {
					sbSql.append(m3.group(1).trim());
					sbSql.append(",");

				} else if (m4.find()) {
					sbSql.append(m4.group(1).trim());
					sbSql.append(",");

				} else {

					if (line_data.startsWith(")")) {
						bCol = false;
						sbSql.deleteCharAt(sbSql.length() - 1);
						sbSql.append(")");

						obj_TDpd = new TDpd(USE_SQL, sbSql.toString(), sbSql.toString(), 100, new TLocation(dpdLineNum));
						System.out.println("UseSql >> " + sbSql.toString());
						root_obj.add(obj_TDpd);

					} else if (line_data.startsWith("--")) {

					} else if ("".equals(line_data.trim())) {

					} else {
						sbSql.append(line_data.replace(",", "").trim());
						sbSql.append(",");
					}
				}

			}
		}
		br.close();
	}


	public int doTObj(int depth, CM_SRC cm_src, TObj tobj, long parent_object_id) {
		log.trace("HANDLER", depth + " : " + tobj.getName() + " : " + tobj.getTempMap());
		return RETURN_CONTINUE;
	}


	public int doTDpd(int depth, TDpd tdpd, CM_SRC cm_src, CM_OBJ cm_obj, int seq) throws SQLException {
		return RETURN_CONTINUE;
	}


	public long generateGID(String prefix, TObj tobj) {
		// ■STEP1:GID生成。
		if (tobj.getType() == CTL_FILE || tobj.getType() == GENERAL_FILE) {
			String gid = tobj.getGID();
			gid = gid.substring(gid.lastIndexOf("/") + 1);

			return FileUtil.getGID("<FILE>", gid);
		}
		return 0L;
	}


	public long generateGID(String prefix, TDpd tdpd) {
		return 0L;
	}


	public String getObjName(boolean is_file, CM_SRC cm_src, String full_object_name) {
		return null;
	}


	public void addAnalyzeStepOnError(CM_SRC cm_src, TResult tresult) {
	}

	public static void main(String[] args) {
		try{
			NS_CTL_HANDLER a = new NS_CTL_HANDLER();
			TObj obj_root = new TObj(1, "", "", 100, new TLocation() );
			String file_name = "C:\\Users\\ito-motoi\\Desktop\\PJ\\NISSAN\\Source\\ctl_sql\\BOI_GROSS_RATE.ctl";
			a.addStep(file_name, obj_root);
		}catch(Exception e){

		}
	}
}
