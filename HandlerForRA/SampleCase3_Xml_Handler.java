/**
 *
 */
package changeminer.HandlerForRA;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.itplus.cm.parser.common.CMParserCommonData;
import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.data.DataForAnalyzer;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.Environment;
import com.itplus.cm.ce.util.FileUtil;
import com.sun.org.apache.xpath.internal.XPathAPI;

import extractor.common.tobj.TDpd;
import extractor.common.tobj.TLocation;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;

/**
 *
 */
public class SampleCase3_Xml_Handler extends HandlerForRA
{
    /**
     *
     */
	private final int FORM_BEAN = 821216;
	private final int FORM_BEAN_CLASS_DPD = 8212005;
	private final int ACTION = 821217;
	private final int ACTION_CLASS_DPD = 8212006;
	private final int ACTION_FORWARD_JSP_DPD = 8212007;
	private final int ACTION_USE_FORM_DPD = 8212008;
	private final String JSP_SQL = "SELECT obj.FULL_OBJ_NAME FROM CM_SRC src, CM_OBJ obj WHERE src.sname = ? AND src.SRC_ID = obj.SRC_ID AND obj.OBJ_TYPE_ID = 410001";


    public SampleCase3_Xml_Handler() {

    }

   /**
	*
	**/
    public String getName() {
        return this.getClass().getName();
    }

    /**
    *  ｻ鄲?ﾀﾛｾ｡ ｴ?ﾑ ﾁ､ﾀﾇﾀﾔｴﾏｴﾙ.
    **/
    public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
        return RETURN_CONTINUE;
    }

   /**
	*  ｻ酳ﾄ ﾀﾛｾ｡ ｴ?ﾑ ﾁ､ﾀﾇﾀﾔｴﾏｴﾙ.
	**/
    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {

    	String file_name = Environment.getSourceDir() + cm_src.getCOLLECT_ID() + cm_src.getSPATH() + cm_src.getSNAME();
    	System.out.println("XML File = " + file_name);
		TObj obj_root = tresult.getTObjList()[0];

		String xmlFlag = getXmlFlag(cm_src.getSNAME());

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document = null;
		Document document2 = null;

		try {

			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse( new File(file_name) );
			document2 =  builder.parse( new File(file_name) );

			NodeList formList = XPathAPI.selectNodeList(document, "//form-bean");
			NodeList actionList = XPathAPI.selectNodeList(document2, "//action");

			for(int i=0 ; i < formList.getLength() ; i++) {
				Node formBeanItem = formList.item(i);
				String name = formBeanItem.getAttributes().getNamedItem("name").getNodeValue();
			    String type = formBeanItem.getAttributes().getNamedItem("type").getNodeValue();

			    TObj beanObj = new TObj (FORM_BEAN, name, name,100, new TLocation());
			    beanObj.add(new TDpd(FORM_BEAN_CLASS_DPD, type, type, 100, new TLocation()));
			    obj_root.add(beanObj);

			}


			for(int i = 0; i < actionList.getLength(); i++){
				Node actionItem = actionList.item(i);

				String actionClass = actionItem.getAttributes().getNamedItem("type").getNodeValue();
				String actionClassName = getLastPathOrName(actionClass.split("\\."));
				System.out.println("****** actionClassName : " + actionClassName);
				TObj actionObj = new TObj (ACTION, actionClassName, actionClassName,100, new TLocation());

				if(actionItem.getAttributes().getNamedItem("name") != null){
					String formName = actionItem.getAttributes().getNamedItem("name").getNodeValue();
					actionObj.add(new TDpd(ACTION_USE_FORM_DPD, formName, formName, 100, new TLocation()));
				}
				if(actionItem.hasChildNodes()){
					NodeList child = actionItem.getChildNodes();
					for(int j = 0; j < child.getLength(); j++){

						Node childItem = child.item(j);
						if(childItem instanceof Element){
							String forwardPath = childItem.getAttributes().getNamedItem("path").getNodeValue();
							//forwardPath = getLastPathOrName(forwardPath.split("/"));
							//forwardPath = getJspFullPath(forwardPath);
							forwardPath = xmlFlag + forwardPath;
							if(forwardPath != null){
								actionObj.add(new TDpd(ACTION_FORWARD_JSP_DPD, forwardPath, forwardPath, 100, new TLocation()));
							}
						}
					}
				}else if(actionClass.equalsIgnoreCase("org.apache.struts.actions.ForwardAction")){
					String path = actionItem.getAttributes().getNamedItem("parameter").getNodeValue();
					actionObj.add(new TDpd(ACTION_FORWARD_JSP_DPD, path, path, 100, new TLocation()));
				}

				obj_root.add(actionObj);
			}

		} catch (Exception e) {
			e.printStackTrace();
			//throw new Exception(e.getMessage());
		} finally {


		}
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
    	if(tobj.getType() == FORM_BEAN){
    		return FileUtil.getGID("<XML>", tobj.getGID());
    	}else if(tobj.getType() == ACTION){
    		return FileUtil.getGID("<XML>", tobj.getGID());
    	}
        return 0L;
    }

   /**
	*
	**/
    public long generateGID(String prefix, TDpd tdpd) {
    	if(tdpd.getType() == FORM_BEAN_CLASS_DPD){
    		return FileUtil.getGID("<JAVA>", tdpd.getGID());
    	}else if(tdpd.getType() == ACTION_CLASS_DPD){
    		return FileUtil.getGID("<JAVA>", tdpd.getGID());
    	}else if(tdpd.getType() == ACTION_USE_FORM_DPD){
    		return FileUtil.getGID("<XML>", tdpd.getGID());
    	}else if(tdpd.getType() == ACTION_FORWARD_JSP_DPD){
    		return FileUtil.getGID("<JAVA>", tdpd.getGID());
    	}
        return 0L;
    }

	/**
	*
	**/
    public String getObjName(boolean is_file, CM_SRC cm_src, String full_object_name)
    {
    	return null;
    }

    /**
    *
	**/
    public void addAnalyzeStepOnError(CM_SRC cm_src, TResult tresult) {
    }

    private String getXmlFlag(String sname){
    	String flag = null;

    	String rex = "struts-config-(common|monitor|payment|receipt|report|withdrawal).xml";
		Pattern p = Pattern.compile(rex);
		Matcher m = p.matcher(sname);

		if (m.find()){
		  flag = m.group(1);
		  flag = "/"+flag;
		}

    	return flag;
    }

    private String getLastPathOrName(String[] arr){

    	String value = null;
    	try{
    		value = arr[arr.length-1];
    	}catch(Exception e){
    		e.printStackTrace();
    	}

    	return value;
    }

    private String getJspFullPath(String jspName){

    	String result = null;

	    try{

	        Connection conn = com.itplus.cm.ce.util.DBUtil.getConnection(false);

			PreparedStatement pstmt = conn.prepareStatement(JSP_SQL);
			pstmt.setString(1, jspName);

			ResultSet rs = pstmt.executeQuery();
			while(rs.next()){
				result = rs.getString(1);
			}

			rs.close();
			pstmt.close();
			conn.close();

	    }catch(Exception e){
	    	System.out.print(e.getMessage());
	    	result = null;
	    }

	    return result;
    }
}
