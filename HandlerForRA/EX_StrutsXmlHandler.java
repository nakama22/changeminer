/**
 *
 */
package changeminer.HandlerForRA;

import java.io.File;
import java.sql.SQLException;

import com.itplus.cm.parser.common.CMParserCommonData;
import com.itplus.cm.ce.addon.common.custom.HandlerForRA;

import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;

import extractor.common.tobj.TDpd;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;

// addon
import extractor.common.tobj.TLocation;

import com.itplus.cm.ce.util.Environment;
import com.itplus.cm.ce.util.FileUtil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

//import org.apache.xpath.*; // jdk 1.4
import com.sun.org.apache.xpath.internal.*; // jdk 1.5


/**
 * Struts XML parsing handler
 *
 *
 *
 */
public class EX_StrutsXmlHandler extends HandlerForRA
{
    /**
     *
     */
    private static final long serialVersionUID = -3303948929891991953L;

	/* ----------------------------------------------
 	STRUTS_XML_XXX ??? ????.
 	----------------------------------------------*/
	final int TOBJ_STRUTS_XML_REQUEST_NAME = 821214;
	final int TDPD_STRUTS_XML_REQUEST_ACTIONCLASS = 8212003;
	final int TDPD_STRUTS_XML_REQUEST_FULL_PAGE = 8212004;


    public EX_StrutsXmlHandler() {

    }

   /**
	*
	**/
    public String getName() {
        return this.getClass().getName();
    }

   /**
	*
	**/
    public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {

	String file_name = Environment.getSourceDir() + cm_src.getCOLLECT_ID() + cm_src.getSPATH() + cm_src.getSNAME();
	log.debug("EX_StrutsXmlHandler", "doExtractor : " + file_name);
		TObj obj_root = tresult.getTObjList()[0];

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document = null;

		try {

			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse( new File(file_name) );

			// XML ??? ???? :
			NodeList request_nodelist = XPathAPI.selectNodeList(document, "//request");
			int count = request_nodelist.getLength();
			for(int i=0 ; i < count ; i++) {

				Node requestNode = request_nodelist.item(i);
				Node actionNode = XPathAPI.selectSingleNode(requestNode, "action-class/text()");
				Node pageNode = XPathAPI.selectSingleNode(requestNode, "page/text()");

				String request = requestNode.getAttributes().getNamedItem("name").getNodeValue();
				String actionClass = (actionNode == null ? "" : actionNode.getNodeValue());
				String page = (pageNode == null ? "" : pageNode.getNodeValue());

				log.debug("STRUTS_XML_HD","request-------"+request ) ;
				log.debug("STRUTS_XML_HD","actionClass-------"+actionClass ) ;
				log.debug("STRUTS_XML_HD","page-------"+page ) ;

				TObj requestObj = new TObj ( TOBJ_STRUTS_XML_REQUEST_NAME, request,request,100, new TLocation());
				if(actionClass.length() > 0)
					requestObj.add(new TDpd(TDPD_STRUTS_XML_REQUEST_ACTIONCLASS, actionClass,actionClass,100, new TLocation()));
				if(page.length() > 0)
					requestObj.add(new TDpd(TDPD_STRUTS_XML_REQUEST_FULL_PAGE, page, page, 100, new TLocation()));
				obj_root.add(requestObj);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
	        }


        return RETURN_CONTINUE;
    }

   /**
	*
	**/
    public int doTObj(int depth, TObj tobj, long parent_object_id) {
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
        return 0L;
    }

   /**
	*
	**/
    public long generateGID(String prefix, TDpd tdpd) {

        if (tdpd.getType() == TDPD_STRUTS_XML_REQUEST_ACTIONCLASS) {
		//System.out.println("########################################"+tdpd.getGID());
			return FileUtil.getGID("<JAVA>", tdpd.getGID());// ｪｳｪｳｪﾏｪｺｪｷｪﾆﾝﾂ牴ｪｷｪｿｪ・ｪ・ﾆﾝﾂ牴ｪ筱ｷｪﾆﾌｸｪﾆｪｯｪﾀｪｵｪ､｡｣
        }
		return 0;
    }

}