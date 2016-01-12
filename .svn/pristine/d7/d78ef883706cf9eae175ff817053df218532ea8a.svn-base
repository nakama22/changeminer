
package changeminer.HandlerForRA;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
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
import extractor.common.tobj.TMeta;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;


public class TEST_HD_COM_C extends HandlerForRA
{


	public final static int C_FUNCTION_DECARE = 510003;
	public final static int C_FUNCTION_OBJECT = 510004;
	// DPD
	public final static int C_FUNCTION_CALL = 5100002;
	public final static int SQL_CHECK       = 5111102;

	public boolean mainflag = false;
	public boolean submainflag = false;

	public String filestr;
	public String fileext;
	public Hashtable hfunname = null;
	public Hashtable hStaticfun = null;
	public Hashtable hsubmainname = null;

    private static final long serialVersionUID = -3303948929891991953L;
    public TEST_HD_COM_C() {

    }

   /**
	*
	**/
    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
        return RETURN_CONTINUE;
    }

    public void CheckObjList(CM_SRC cm_src, TObj tobj) {
		TObj objlist[] = tobj.getTObjList();
		if (objlist == null) return;
		for ( int i = 0; i < objlist.length; i ++ ) {
			TObj tmpobj = objlist[i];
			CheckObjList(cm_src, tmpobj);
			String objname = tmpobj.getName();
			String fullobjname = tmpobj.getName();
			int idx ;
			if ( (idx = objname.indexOf('(')) > 0) {
				objname = objname.substring(0, idx).trim();
			}
	    	if (tmpobj.getType() == C_FUNCTION_OBJECT) {
	    		tmpobj.setKeyValidation(100);
	    		if (objname.equals("main")) {
	    			mainflag = true;

	    		}
	    		else if ( extern_main_flag(objname, fullobjname) == true) {
	    			mainflag = true;
	        		log.debug("extern find main", tmpobj.getName());

	    		}
	    		else if (fullobjname.indexOf("(TPSVCINFO") > 0) {
	    			submainflag = true;
	    			if (!hsubmainname.containsKey(objname)) {
	    				Integer val = new Integer(0);
	    				hsubmainname.put(objname, val);
	    			}
	        		log.debug("find sub main", tmpobj.getName());
	    		}
	    		else if (extern_sub_flag(objname, fullobjname) == true) {
	    			submainflag = true;
	    			if (!hsubmainname.containsKey(objname)) {
	    				Integer val = new Integer(0);
	    				hsubmainname.put(objname, val);
	    			}
	        		log.debug("extern find sub main", tmpobj.getName());
	    		}

	    		else {

	    			if (!hfunname.containsKey(objname)) {
	    				//log.debug("push hash", objname);
	    				Integer val = new Integer(0);
	    				hfunname.put(objname, val);
	    			}

	    		}
	    	}

	    	if(tmpobj.getType() == C_FUNCTION_DECARE || tmpobj.getType() == C_FUNCTION_OBJECT) {
	    		TMeta mt[] = tmpobj.getTMetaList();
	    		for (int k = 0; k < mt.length; k++) {
	    			if(mt[k].getName().equals("5100410")) {
	    				if (mt[k].getValue().startsWith("static ")) {
	    					hStaticfun.put(objname, objname);
	    				}
	    			}
	    		}
	    	}
		}
    }

    public boolean extern_main_flag(String objname, String fullobjname) {
    	return false;
    }

    public boolean extern_sub_flag(String objname, String fullobjname) {
    	return false;
    }
    public void CheckDPDList(TObj tobj) {
		TObj objlist[] = tobj.getTObjList();
		if (objlist == null) return;
		for ( int i = 0; i < objlist.length; i ++ ) {
			TObj tmpobj = objlist[i];
			CheckDPDList(tmpobj);
			TDpd  dpdlist[] = tmpobj.getTDpdList();
			if (dpdlist == null)
				continue ;
			for ( int j = 0; j < dpdlist.length; j ++) {
				TDpd tmpdpd = dpdlist[j];
				if (tmpdpd == null) continue;

				if (tmpdpd.getType() == C_FUNCTION_CALL) {
					String dpd_name = tmpdpd.getName();
		    		if (dpd_name.indexOf('(') > 0) {
		    			dpd_name = dpd_name.substring(0, dpd_name.indexOf('(') ).trim();
		    		}
		    		if (hfunname.containsKey(dpd_name)) {
		    			Integer val = (Integer)hfunname.get(dpd_name);
		    			int cnt = val.intValue() + 1;
		    			hfunname.put(dpd_name, new Integer(cnt) );
		    			//log.debug("push hash", dpd_name + ", count =" + cnt);
		    		}
				}
			}

		}

    }
   /**
	*
	**/
    public int doTObj(int depth, CM_SRC cm_src, TObj tobj, long parent_object_id) {
/*		if(System.getenv("CPSERVER") == null) {
			return RETURN_CONTINUE;
		}*/
    	if (depth == 0) {

        	filestr = cm_src.getSNAME();
        	fileext = "c"; // default .c
        	int idx = filestr.indexOf('.');
        	if (idx > 0) {
        		filestr = filestr.substring(0, idx);
        		fileext = cm_src.getSNAME().substring(idx+1, cm_src.getSNAME().length());
        	}
    		log.debug("filestr ", filestr);
    		log.debug("fileext ", fileext);

        	mainflag = false;
			if (hfunname == null)
				hfunname = new Hashtable();
			else
				hfunname.clear();

			submainflag = false;
			if (hsubmainname == null)
				hsubmainname = new Hashtable();
			else
				hsubmainname.clear();

			if (hStaticfun == null)
				hStaticfun = new Hashtable();
			else
				hStaticfun.clear();

        	CheckObjList(cm_src, tobj);
        	CheckDPDList(tobj);
    	}
    	        return RETURN_CONTINUE;
    }

   /**
	*
	**/
    public int doTDpd(int depth, TDpd tdpd, CM_SRC cm_src, CM_OBJ cm_obj, int seq) throws SQLException {
    	String dpd_name = tdpd.getName();
/*		if(System.getenv("CPSERVER") == null) {
			return RETURN_CONTINUE;
		}*/
    	if (tdpd.getType() == C_FUNCTION_CALL) {
        	if (tdpd.getGID() == null) {
        		tdpd.setKeyValidation(100);
        		if (dpd_name.indexOf('(') > 0) {
        			dpd_name = dpd_name.substring(0, dpd_name.indexOf('(') ).trim();
            		String fileName = Environment.getSourceDir() + cm_src.getCOLLECT_ID() + cm_src.getSPATH() + cm_src.getSNAME();
            		tdpd.setGID(fileName + "|" + dpd_name);
            	}
        	}

    	}
        return RETURN_CONTINUE;
    }

   public long generateGID(String prefix, TObj tobj) {
    	String obj_name = tobj.getName();
    	if (tobj.getType() == C_FUNCTION_OBJECT) {
    		if (obj_name.indexOf('(') > 0) {
    			obj_name = obj_name.substring(0, obj_name.indexOf('(') ).trim();
    		}
    		obj_name = obj_name.trim();
    		int cnt = 0;
    		if (hfunname.containsKey(obj_name)) {
    			Integer val = (Integer)hfunname.get(obj_name);
    			cnt = val.intValue();
    		}

    		if (mainflag) { // || cnt > 0) {
	    		String str = new String();
	    		str  =  filestr + "." + obj_name;

	    		tobj.setGID(str);
	    		return FileUtil.getGID("<EC>", str);

    		} else if (submainflag) { // || cnt > 0) {
    			String str = new String();
	    		if (hsubmainname.containsKey(obj_name)) {
	    			str  =  obj_name;
		    		tobj.setGID(str);
		    		return FileUtil.getGID("<EC>", str);
	    		} else {
		    		str  =  filestr + "." + obj_name;
		    		tobj.setGID(str);
		    		return FileUtil.getGID("<EC>", str);
	    		}

    		}
    		else {
	    		String str = new String();
    			if(hStaticfun.containsKey(obj_name)) {
    				str  =  filestr + "." + obj_name;
    			}else
    	    		str  =  obj_name;

	    		tobj.setGID(str);
	    		return FileUtil.getGID("<EC>", str);
    		}
    	}
        return 0L;
    }

   public long generateGID(String prefix, TDpd tdpd) {
    	String dpd_name = tdpd.getName();
		if (tdpd.getType() == C_FUNCTION_CALL) {
    		if (dpd_name.indexOf('(') > 0) {
    			dpd_name = dpd_name.substring(0, dpd_name.indexOf('(') ).trim();
    		}
    		dpd_name = dpd_name.trim();
    		int cnt = 0;
    		if (hfunname.containsKey(dpd_name)) {
    			Integer val = (Integer)hfunname.get(dpd_name);
    			cnt = val.intValue();
    		}

    		if (mainflag) {
        		if (cnt == 0) {
    	    		String str = new String();
    	    		str  =  dpd_name;
    	    		tdpd.setGID(str);
		    		return FileUtil.getGID("<EC>", str);
        		}
	    		String str = new String();
	    		str  =  filestr + "." + dpd_name;
	    		tdpd.setGID(str);
	    		return  FileUtil.getGID("<EC>", str);
    		}
    		else if (submainflag) {
	    		if (hsubmainname.containsKey(dpd_name)) {
	    			String str = new String();
    	    		str  =  dpd_name;
    	    		tdpd.setGID(str);
					return FileUtil.getGID("<EC>", str);
	    		}
        		if (cnt == 0) {
    	    		String str = new String();
    	    		str  =  dpd_name;
    	    		tdpd.setGID(str);
					return FileUtil.getGID("<EC>", str);
        		}
	    		String str = new String();
	    		str  =  filestr + "." + dpd_name;

	    		tdpd.setGID(str);
				return  FileUtil.getGID("<EC>", str);
    		}
    		else {
	    		String str = new String();
    			if(hStaticfun.containsKey(dpd_name)) {
    				str  =  filestr + "." + dpd_name;
    			} else
    	    		str  =  dpd_name;
	    		tdpd.setGID(tdpd.getGID());//str
				return FileUtil.getGID("<EC>", str);
    		}
    	}
        return 0L;
    }

   protected ArrayList loadfile(String file_name)  throws Exception
	{
		FileInputStream fis = null;
		BufferedReader br = null;
		ArrayList filedata = new ArrayList();

		try {
			File file = new File(file_name);
			fis = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fis));
			String line_data = new String();
			boolean cmtflag = false;
			boolean vbscriptflag = false;
			boolean scriptcmt = false;
			int linecnt = 0;
			while (br.ready()) {

				line_data = br.readLine();
				if (line_data == null) {
					continue;
				}
				String upperstr = line_data.toUpperCase().trim();
				if (upperstr.indexOf("LANGUAGE")>0 && upperstr.indexOf("VBSCRIPT") > 0) {
					vbscriptflag = true;
				}
				if (vbscriptflag && upperstr.indexOf("/SCRIPT") > 0) {
					vbscriptflag = false;
				}
				if (vbscriptflag && upperstr.startsWith("'"))  { // vbscript commenct
					line_data = "";
				}
				int idx1 = line_data.indexOf("<%--");
				int idx2 = line_data.indexOf("--%>");
				if(cmtflag == false && scriptcmt == false && idx1 >=0) {
					String tmp = line_data.substring(0, idx1);
					if (idx2 >=0) {
						tmp = tmp + " " + line_data.substring(idx2+"--%>".length());
					} else {
						scriptcmt = true;
					}
					line_data = tmp;
				}
				else if (scriptcmt == true) {
					if (idx2 >= 0) {
						line_data = " " + line_data.substring(idx2+"--%>".length());
						scriptcmt = false;
					} else { // 2010.11.26 AI ?oA? AO?RAI ?c・￣ AUAI °a?i ≫eA|CI´A ・cA? ouARAO?i ?oA?
						line_data = "";
					}
				}


				char [] data = line_data.toCharArray();

				boolean qflag = false;
				boolean escape = false;
				String tmp = new String();
				for ( int i = 0; i < data.length ; i++) {
					if (escape) {
						tmp += data[i];
						escape = false;
						continue;
					}
					switch (data[i]) {
					case '/' :
						if (cmtflag) break;
						if(!qflag) {
							if (  i+1 < data.length && data [i+1] == '/') {
								i = data.length;
								break;
							}
						}
						if (i+1 < data.length) {
							if ( data[i+1] == '*') {
								i ++;
								cmtflag = true;
								break;
							}
						}
						tmp += data[i];
						break;
					case '*' :
						if(cmtflag) {
							if (i+1 < data.length) {
								if ( data [i+1] == '/') {
									i ++;
									cmtflag = false;
									break;
								}
							}
						}
						else {
							tmp += data[i];
						}
						break;
					case '"' :
						if (cmtflag) break;
						if (qflag) qflag = false;
						else qflag = true;
						tmp += data[i];
						break;
					case '\\' :
						if (cmtflag) break;
						if(!qflag) {
							tmp += data[i];
							escape = true;
							break;
						}

					default :
						if (!cmtflag)
							tmp += data[i];
					}
				}
				linecnt ++;
				//log.debug("[line][" + (linecnt+1) +"]", tmp);
				filedata.add(new String(tmp.trim()));
			}
			return filedata;
		}
		catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		finally {
			try {
				if (br != null)
					br.close();
				if (fis != null)
					fis.close();
			}
			catch (IOException ex) {
			}
		}
	}

	public TObj getFind_MethodTObj(TObj parent_tobj, int line_seq)
	{
		if(parent_tobj == null)
			return null;
		try
		{
			TObj tobjList[] = parent_tobj.getTObjList();
			if(tobjList == null)
				return null;
			int count = tobjList.length;
			for(int i = 0; i < count; i++)
			{
				int objType = tobjList[i].getType();
				int is = tobjList[i].getTLocation().getStartLine();
				int ie = tobjList[i].getTLocation().getEndLine();
				if(objType == 510004 && is <= line_seq && line_seq <= ie)
					return tobjList[i];
				TObj tobjSubList[] = tobjList[i].getTObjList();
				if(tobjSubList != null)
				{
					TObj findTObj = getFind_MethodTObj(tobjList[i], line_seq);
					if(findTObj != null)
						return findTObj;
				}
			}

		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		return null;
	}

}
