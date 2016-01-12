/**
 * 2013. 05.30 main, sub main As°! A?Ac oIoD?! ´eCN ・IA÷ As°!
 * 2013. 04.15 sub main A3，R As°! TPSVCINFO
 * 2010. 10.13 CheckObjList ?!?- case 4 ?! ´eCN ・cA? o，°-CI?c o，´U A?ERE÷ A￡A≫ ?o AOμμ・I ?oA?
 * 2010. 06.30 static CO?o AI°a?i?! 3≫oI?!?-，， ?￢°uAI 3a?A°O ?oA?.
 */
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

/**
 *
 * 090413 :  C AC system functionA≫ A|°A CI±a A§CO?- doTDpd，| ?oA?
 *           AAAI°a・I|CO?o，i AI・± CuAA・I gid，| ，，μe?i AO?i?s filter °! μ?AU CN´U.
 *
 *
 */
public class HD_COM_C extends HandlerForRA
{
    /**
     *
     */
	// ---------------------?A・! oIoDAo ?iμa1I?!?- As°! CO AO?i?s CO.... ----------
	// OBJ
	// public final static int C_SOURCE_OBJECT = 511110;
	// ---------------------------------------------------------------------
	// OBJ
	// c?!?-´A main AIAO´A °a?i?!´A ´U，\ c?!?- call CO ?o ?oA，1C・I AIoIoDA≫ A3，R CI±a A§CO.

	public final static int C_FUNCTION_DECARE = 510003;
	public final static int C_FUNCTION_OBJECT = 510004;
	// DPD
	public final static int C_FUNCTION_CALL = 5100002; // c CO?o call
	public final static int SQL_CHECK       = 5111102; // sprintf, strcpy?! sql AO´A °a?i A?≫oAuAI sql AIAo A?AcCI?c sql °a?i，， AuAa.

	public boolean mainflag = false;
	public boolean submainflag = false; // main ?aCOA≫ CI´A pgm A，・I 3≫oI CO?o ´A AU?A，， ≫c?e.

	public String filestr;
	public String fileext;
	public Hashtable hfunname = null;
	public Hashtable hStaticfun = null;
	public Hashtable hsubmainname = null;

    private static final long serialVersionUID = -3303948929891991953L;
    public HD_COM_C() {

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
	    		tmpobj.setKeyValidation(100); // As°!，| COAO?i?s obj generate，| A≫μi... 2008.10.23 AI ?oA?
	    		if (objname.equals("main")) {
	    			mainflag = true;
	        		log.debug("find main", tmpobj.getName());
	    		}
	    		else if ( extern_main_flag(objname, fullobjname) == true) {
	    			mainflag = true;
	        		log.debug("extern find main", tmpobj.getName());
	    			 ; // 2013. 05.30 As°!
	    		}
	    		/* --------------- SITE，¶´U o￣°aAI CE?a CO -------------------------- */
	    		/* CASE 1: AAAI，iAo ´e1RAU AI°i CO?o´A ?O1RAU AI，e?- °°Ao AI，§A≫ °!Ao °a?i mainAI AO´A °IA，・I °￡AO.
	    		else if (objname.toUpperCase().equals(filestr.toUpperCase()) ) {
	    			submainflag = true;
	    			if (!hsubmainname.containsKey(objname)) {
	    				//log.debug("push hash", objname);
	    				Integer val = new Integer(0);
	    				hsubmainname.put(objname, val);
	    			}
	        		log.debug("find sub main", tmpobj.getName());
	    		} */
	    		/* CASE 2: AAAI，i°u CO?o°! ´e?O1RAU ±，oDAI °°°i °°Ao AI，§, .c?!´A AAAI，i°u CO?o，iAI °°Ao °IAI AO?iμμ  mainA，・I °￡AO CIAo ?E´A °a?i   */
	    		/*
	    		else if (objname.equals(filestr) && !fileext.equals("c")) {
	    			submainflag = true;
	    			if (!hsubmainname.containsKey(objname)) {
	    				//log.debug("push hash", objname);
	    				Integer val = new Integer(0);
	    				hsubmainname.put(objname, val);
	    			}
	        		log.debug("find sub main", tmpobj.getName());
	    		}
	    		*/
	    		/* CASE 3: AAAI，i°u CO?o°! ´e?O1RAU ±，oDAI °°°i   °°Ao AI，§AI，e mainA，・I °￡AO CI´A °a?i  */
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

	    		/* ---------------------------------------------------------- */
	    		else {

	    			if (!hfunname.containsKey(objname)) {
	    				//log.debug("push hash", objname);
	    				Integer val = new Integer(0);
	    				hfunname.put(objname, val);
	    			}

	    		}
	    	}
	    	// static A，・I ?±?dμE CO?oAIAo A?Ac
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
    // main A，・I °￡AOCO A?Ac As°!°! AO´A °a?i ≫o?O 1T?A As°! ±，Co // 2013. 05.30 As°!
    public boolean extern_main_flag(String objname, String fullobjname) {
    	return false;
    }
    //  sub main A，・I °￡AOCO A?Ac As°!°! AO´A °a?i ≫o?O 1T?A As°! ±，Co // 2013. 05.30 As°!
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
    	// log.debug("doTObj", "type=" + tobj.getType() + ",name=" + tobj.getName());
    	if (depth == 0) {
        	// check_ecc_file(cm_src);

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
    	//if (tobj.getType() == 511101) {
    	//	tobj.add( new TObj(C_SOURCE_OBJECT, filestr, filestr, 100, tobj.getTLocation()) );
    	//}


    	// log.debug("HANDLER", depth + " : " + tobj.getName() + " : " + tobj.getTempMap());
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
        	//log.debug("doTDpd(DPD)=================>>>>","gid="+tdpd.getGID()+",dpd=" + tdpd.getName());
        	if (tdpd.getGID() == null) {

        		if (dpd_name.indexOf('(') > 0) {
        			dpd_name = dpd_name.substring(0, dpd_name.indexOf('(') ).trim();
            		//String fileName = Environment.getSourceDir() + "/" + cm_src.getCOLLECT_ID() + cm_src.getSPATH() + cm_src.getSNAME();
					String fileName = Environment.getSourceDir() + cm_src.getCOLLECT_ID() + cm_src.getSPATH() + cm_src.getSNAME();

            		tdpd.setGID(fileName + "|" + dpd_name);

            		 if (checkSystemFunction(dpd_name)) {
                         log.debug("found system function >> ", dpd_name);
                         return RETURN_CONTINUE;
                     }

                     tdpd.setKeyValidation(100);

            	}
        	}
        	/*
    		if (dpd_name.indexOf('(') > 0) {
    			dpd_name = dpd_name.substring(0, dpd_name.indexOf('(') ).trim();
    		}
    		int cnt = 0;
    		if (hfunname.containsKey(dpd_name)) {
    			Integer val = (Integer)hfunname.get(dpd_name);
    			cnt = val.intValue();
    		}
    		if (mainflag || cnt > 0) {
	    		String str = new String();
	    		str  =  filestr + "." + dpd_name;
	    		Integer val;
	    		long gid = FileUtil.getGID("<EC>", str);
	    		Long lgid = new Long(gid);
	    		tdpd.setGID(lgid.toString());
	    		log.debug("(inside)doTDpd("+lgid.toString()+")=================>>>>",str);
    		} else {
	    		String str = new String();
	    		str  =  dpd_name;
	    		Integer val;
	    		long gid = FileUtil.getGID("<EC>", str);
	    		Long lgid = new Long(gid);
	    		log.debug("(outside)doTDpd("+lgid.toString()+")=================>>>>",str);
	    		tdpd.setGID(lgid.toString());
    		}
    		*/
    	}
       // log.trace("HANDLER", depth + " : " + tdpd.getName() + " : " + tdpd.getTempMap());
        return RETURN_CONTINUE;
    }

   /**
	*
	**/
    public long generateGID(String prefix, TObj tobj) {
    	String obj_name = tobj.getName();
    	/*
    	if (tobj.getType() == C_SOURCE_OBJECT) {
    		String str = new String();
    		str  =  obj_name;
    		//log.debug("generateGID(OBJ)=================>>>>",str);
    		return FileUtil.getGID("<C>", str);
    	}
    	*/
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
    		// 1°3AC CO?o，， AO´A °a?i?!´A ¶oAIoe・￣，R・I o≫´U...
    		// && hfunname.size() > 1 AI ・cA?A≫ Ay?i 3OA≫ ?o ?oA?....
    		// CN°3，， μC?i AO´A °a?iμμ AOA?... ?D.?D

    		if (mainflag) { // || cnt > 0) {
	    		String str = new String();
	    		str  =  filestr + "." + obj_name;
	    		//log.debug("(main)generateGID(OBJ)=================>>>>",str);
	    		tobj.setGID(str);
	    		return FileUtil.getGID("<EC>", str);

    		} else if (submainflag) { // || cnt > 0) {
    			// ?UoI?!?- main ?aCOA≫ CI´A CO?o・I?- AU?AAo CO?o，i，，A，・I gid ≫y?o, sub CO?oμeAo ?-・I ?￢°uAI ?E3a?A°O filestr. obj_nameA，・I A3，R
	    		String str = new String();
	    		if (hsubmainname.containsKey(obj_name)) {
	    			str  =  obj_name;
		    		log.debug("generateGID(OBJ)=================>>>>",str);
		    		tobj.setGID(str);
		    		log.debug("xxxxxxxxxxxxxxx GID", "" + FileUtil.getGID("<EC>", str));
		    		return FileUtil.getGID("<EC>", str);
	    		} else {
		    		str  =  filestr + "." + obj_name;
		    		log.debug("(sub main)generateGID(OBJ)=================>>>>",str);
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

	    		//log.debug("generateGID(OBJ)=================>>>>",str);
	    		tobj.setGID(str);
	    		return FileUtil.getGID("<EC>", str);
    		}
    	}
        return 0L;
    }

   /**
	*
	**/
    public long generateGID(String prefix, TDpd tdpd) {
    	String dpd_name = tdpd.getName();
		System.out.println("dpd_name >> " + dpd_name);
		System.out.println("tdpd.getGID() >> " + tdpd.getGID());
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

    		if (mainflag) { // ?AAU CA・I±×・\
        		if (cnt == 0) { // 3≫oI?! CO?o°! ?oA，，e... ?UoI・I...
    	    		String str = new String();
    	    		str  =  dpd_name;
    	    		tdpd.setGID(str);
					System.out.println("mainFlag >> " + str);
    	    		return FileUtil.getGID("<EC>", str);
        		}
	    		String str = new String();
	    		str  =  filestr + "." + dpd_name;
	    		//log.debug("(inside)generateGID(DPD)=================>>>>",str);
	    		tdpd.setGID(str);
	    		return  FileUtil.getGID("<EC>", str);
    		}
    		else if (submainflag) { // SUB MAIN CA・I±×・\
	    		if (hsubmainname.containsKey(dpd_name)) {
	    			String str = new String();
    	    		str  =  dpd_name;
    	    		tdpd.setGID(str);
					System.out.println("submainFlag >> " + str);
    	    		return FileUtil.getGID("<EC>", str);
	    		}
        		if (cnt == 0) { // 3≫oI?! CO?o°! ?oA，，e... ?UoI・I...
    	    		String str = new String();
    	    		str  =  dpd_name;
    	    		tdpd.setGID(str);
					System.out.println("submainFlag count ==0 >> " + str);
    	    		return FileUtil.getGID("<EC>", str);
        		}
	    		String str = new String();
	    		str  =  filestr + "." + dpd_name;
	    		//log.debug("(inside)generateGID(DPD)=================>>>>",str);
	    		tdpd.setGID(str);
				System.out.println("submainFlag default >> " + str);
	    		return  FileUtil.getGID("<EC>", str);
    		}
    		else {
	    		String str = new String();
    			if(hStaticfun.containsKey(dpd_name)) {
    				str  =  filestr + "." + dpd_name;
    			} else
    	    		str  =  dpd_name;
	    		tdpd.setGID(tdpd.getGID());//str
				System.out.println("else >> " + str);
				System.out.println("else filestr >> " + filestr);
	    		return FileUtil.getGID("<EC>", str);
    		}
    	}
        return 0L;
    }
    /* CO´c AAAIA≫ AD?i?- comment，| ≫eA|CI，c ArrayList?! AuAaCN´U. */
	// 2008.06.20  AO?R As?! *°! μe?i°!´A 1o±× ?oA?
	// /* ~ */, // -- AC AO?RA≫ A|°ACI?c ArrayList ?! AuAa.
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
				// 2010.10.13 <%-- ~ --%> ≫cAI?! /* AO?R A|°A
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

	public boolean checkSystemFunction(String methodName) {

        boolean isSystemFunc = false;
        for (final String funcName : functionList) {

            if (methodName.startsWith(funcName)) {
                isSystemFunc = true;
                break;
            }

        }
        return isSystemFunc;
    }

	public String[] functionList =
            new String[] {"_chmod", "_clear87", "_close", "_control87", "_creat",
                    "_exit", "_fpreset", "_graphfreemem", "_graphgetmem", "_lrotr",
                    "_matherrl", "_open", "_OvrInitEms", "_OvrInitExt", "_read", "_rotl",
                    "_rotr", "_setcursortype", "_status87", "_strerror", "_tolower",
                    "_toupper", "_write", "ab", "abort", "abs", "absread", "abswrite",
                    "accept", "access", "acos", "alarm", "allocmem", "arc", "arg",
                    "asctime", "asin", "assert", "atan", "atan2", "atexit", "atof",
                    "atoi", "atol", "bar", "bar3d", "bdos", "bdosptr", "bind", "binmode",
                    "bioscom", "biosdisk", "biosequip", "bioskey", "biosmemory",
                    "biosprint", "biostime", "bless", "brk", "bsearch", "cabs", "caller",
                    "calloc", "ceil", "cgets", "chdir", "chmod", "chomp", "chop",
                    "chown", "chr", "chroot", "chsize", "circle", "cleardevice",
                    "clearerr", "clearviewport", "clock", "close", "closedir",
                    "closegraph", "clreol", "clrscr", "complex", "conj", "connect",
                    "coreleft", "cos", "cosh", "country", "cprintf", "cputs", "creat",
                    "creatnew", "creattemp", "crypt", "cscanf", "ctime", "ctrlbrk",
                    "ctrncmp", "dbmclose", "dbmopen", "defined", "delay", "delete",
                    "delline", "detectgraph", "die", "difftime", "disable", "div", "do",
                    "dosexterr", "dostounix", "drawpoly", "dump", "dup", "dup2", "each",
                    "ecvt", "ellipse", "enable", "endgrent", "endhostent", "endnetent",
                    "endprotoent", "endpwent", "endservent", "eof", "eval", "exec",
                    "exists", "exit", "exp", "fabs", "farcalloc", "farcoreleft",
                    "farfree", "farmalloc", "farrealloc", "fclose", "fcloseall", "fcntl",
                    "fcvt", "fdopen", "feof", "ferror", "fflush", "fgetc", "fgetchar",
                    "fgetpos", "fgets", "filelength", "fileno", "fillellipse",
                    "fillpoly", "findfirst", "findnext", "flock", "floodfill", "floor",
                    "flushall", "fmod", "fnmerge", "fnsplit", "fopen", "fork",
                    "formline", "FP_OFF", "FP_SEG", "fprintf", "fputc", "fputchar",
                    "fputs", "fread", "free", "freemem", "freopen", "frexp", "fscanf",
                    "fseek", "fsetpos", "fstat", "ftell", "ftime", "fwrite", "gcvt",
                    "geninterrupt", "getarccoords", "getaspectratio", "getbkcolor",
                    "getc", "getcbrk", "getch", "getchar", "getche", "getcolor",
                    "getcurdir", "getcwd", "getdate", "getdefaultpalette", "getdfree",
                    "getdisk", "getdrivername", "getdta", "getenv", "getfat", "getfatd",
                    "getfillpattern", "getfillsettings", "getftime", "getgraphmode",
                    "getgrent", "getgrgid", "getgrname", "gethostbyaddr",
                    "gethostbyname", "gethostent", "getimage", "getlinesettings",
                    "getlogin", "getmaxcolor", "getmaxmode", "getmaxx", "getmaxy",
                    "getmodename", "getmoderange", "getnetbyaddr", "getnetbyname",
                    "getnetent", "getpalette", "getpalettesize", "getpass",
                    "getpeername", "getpgrp", "getpid", "getpixel", "getppid",
                    "getpriority", "getprotobyname", "getprotobynumber", "getprotoent",
                    "getpsp", "getpwent", "getpwnam", "getpwuid", "gets",
                    "getservbyname", "getservbyport", "getservent", "getsockname",
                    "getsockopt", "gettext", "gettextinfo", "gettextsettings", "gettime",
                    "getvect", "getverify", "getviewsettings", "getw", "getx", "gety",
                    "glob", "gmtime", "gotoxy", "graphdefaults", "grapherrormsg",
                    "graphresult", "grep", "harderr", "hardresume", "hardretn",
                    "heapcheck", "heapcheckfree", "heapchecknode", "heapfillfree",
                    "heapwalk", "hex", "highvideo", "hypot", "imag", "imagesize",
                    "import", "index", "initgraph", "inport", "inportb", "insline",
                    "installuserdriver", "installuserfont", "int", "int86", "int86x",
                    "intdos", "intdosx", "intr", "ioctl", "isalnum", "isalpha",
                    "isascii", "isatty", "iscntrl", "isdigit", "isgraph", "islower",
                    "isprint", "ispunct", "isspace", "isupper", "isxdigit", "itoa",
                    "join", "kbhit", "keep", "keys", "kill", "labs", "lc", "lcfirst",
                    "ldexp", "ldiv", "length", "lfind", "line", "linerel", "lineto",
                    "link", "listen", "local", "localeconv", "localtime", "lock", "log",
                    "log10", "longjmp", "lowvideo", "lsearch", "lseek", "lstat", "ltoa",
                    "malloc", "map", "matherr", "max", "memccpy", "memchr", "memcmp",
                    "memcpy", "memicmp", "memmove", "memset", "min", "MK_FP", "mkdir",
                    "mktemp", "modf", "movedata", "moverel", "movetext", "moveto",
                    "movmem", "msgctl", "msgget", "msgrcv", "msgsnd", "my", "norm",
                    "normvideo", "nosound", "oct", "open", "opendir", "ord", "outport",
                    "outportb", "outtext", "outtextxy", "pack", "parsfnm", "peek",
                    "peekb", "perror", "pieslice", "pipe", "poke", "pokeb", "polar",
                    "poly", "pop", "pos", "pow", "pow10", "print", "printf", "push",
                    "putc", "putch", "putchar", "putenv", "putimage", "putpixel", "puts",
                    "puttext", "putw", "q", "qq", "qsort", "quotemeta", "qw", "qx",
                    "raise", "rand", "randbrd", "randbwr", "random", "randomize", "read",
                    "readdir", "readlink", "real", "realloc", "rectangle", "recv", "ref",
                    "registerbgidriver", "registerbgifont", "remove", "rename", "reset",
                    "restorecrtmode", "reverse", "rewind", "rewinddir", "rindex",
                    "rmdir", "sbrk", "scalar", "scanf", "searchpath", "sector", "seek",
                    "seekdir", "segread", "select", "select", "semctl", "semget",
                    "semop", "send", "setactivepage", "setallpalette", "setaspectratio",
                    "setbkcolor", "setblock", "setbuf", "setcbrk", "setcolor", "setdate",
                    "setdisk", "setdta", "setfillpattern", "setfillstyle", "setftime",
                    "setgraphbufsize", "setgraphmode", "setgrent", "sethostent",
                    "setjmp", "setlinestyle", "setlocale", "setmem", "setmode",
                    "setnetent", "setpalette", "setpgrp", "setpriority", "setprotoent",
                    "setpwent", "setrgbpalette", "setservent", "setsockopt",
                    "settextjustify", "settextstyle", "settime", "setusercharsize",
                    "setvbuf", "setvect", "setverify", "setviewport", "setvisualpage",
                    "setwritemode", "shift", "shmctl", "shmget", "shmread", "shmwrite",
                    "shutdown", "signal", "sin", "sinh", "sleep", "socket", "socketpair",
                    "sopen", "sort", "sound", "spawn", "splice", "split", "sprintf",
                    "sqrt", "srand", "sscanf", "stat", "stime", "stpcpy", "strcat",
                    "strchr", "strcmp", "strcmp", "strcmpi", "strcoll", "strcpy",
                    "strcspn", "strdup", "strerror", "stricmp", "strlen", "strlwr",
                    "strncat", "strncmp", "strncmpi", "strncpy", "strnicmp", "strpbrk",
                    "strrchr", "strrev", "strset", "strspn", "strstr", "strtod",
                    "strtok", "strtol", "strtoul", "strupr", "study", "substr", "swab",
                    "symlink", "syscall", "sysopen", "sysread", "system", "syswrite",
                    "tan", "tanh", "tell", "telldir", "textattr", "textbackground",
                    "textcolor", "textheight", "textmode", "textwidth", "tie", "tied",
                    "time", "times", "tmpfile", "tmpnam", "toascii", "tolower",
                    "toupper", "truncate", "tzset", "uc", "ucfirst", "ultoa", "umask",
                    "undef", "ungetc", "ungetch", "unixtodos", "unlink", "unlock",
                    "unpack", "unshift", "untie", "utime", "values", "vec", "wait",
                    "waitpid", "wantarray", "warn", "wherex", "wherey", "window", "write"};

}
