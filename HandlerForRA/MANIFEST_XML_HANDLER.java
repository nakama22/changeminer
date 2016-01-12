package changeminer.HandlerForRA;

import java.io.File;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.meta.CM_OBJ;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.Environment;
import com.itplus.cm.parser.common.CMParserCommonData;
import com.sun.org.apache.xpath.internal.XPathAPI;

import extractor.common.tobj.TDpd;
import extractor.common.tobj.TLocation;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;

public class MANIFEST_XML_HANDLER extends HandlerForRA {

	public static void main(String[] args) {

		String xmlpath = "C:\\Sample\\Android\\Android2.3.3\\ApiDemos\\AndroidManifest.xml";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document = null;

		try {

			
			
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse( new File(xmlpath) );

			//manifest
			Node manifestNode = XPathAPI.selectSingleNode(document,  "/manifest");
			String packageStr = manifestNode.getAttributes().getNamedItem("package").getNodeValue();
			System.out.println(" manifest >> " + packageStr);


			//applications
			Node applicationNode = XPathAPI.selectSingleNode(document,  "/manifest/application");
			// use_resource
			String nameStr = applicationNode.getAttributes().getNamedItem("android:name").getNodeValue(); // app名
			String iconStr = applicationNode.getAttributes().getNamedItem("android:icon").getNodeValue();
			String labelStr = applicationNode.getAttributes().getNamedItem("android:label").getNodeValue();
			System.out.println(" nameStr >> " + nameStr);
			System.out.println(" iconStr >> " + iconStr);
			System.out.println(" labelStr >> " + labelStr);

			//service
			NodeList services = XPathAPI.selectNodeList(applicationNode, "service");
			for(int i = 0 ; i < services.getLength() ; i++){

				Node node = services.item(i);
				String name = node.getAttributes().getNamedItem("android:name").getNodeValue();
				System.out.println("service " + i + " : "+ name);

				String fullName = name.startsWith(".") ? packageStr + name : packageStr + "." + name;
				System.out.println("		" + " : "+ fullName);
			}

			//receiver
			NodeList receivers = XPathAPI.selectNodeList(applicationNode, "receiver");
			for(int i = 0 ; i < receivers.getLength() ; i++){

				Node node = receivers.item(i);
				String name = node.getAttributes().getNamedItem("android:name").getNodeValue();
				System.out.println("receiver " + i + " : "+ name);

				String fullName = name.startsWith(".") ? packageStr + name : packageStr + "." + name;
				System.out.println("		" + " : "+ fullName);
			}


			//activity
			NodeList activities = XPathAPI.selectNodeList(applicationNode, "activity");
			for(int i = 0 ; i < activities.getLength() ; i++){

				Node node = activities.item(i);
				String name = node.getAttributes().getNamedItem("android:name").getNodeValue();
				Node labelNode = node.getAttributes().getNamedItem("android:label");
				String label = labelNode == null ? null : labelNode.getNodeValue();
				System.out.println("activity " + i + " : "+ (name == null ? "null" : name) + " , " + (label == null ? "null" : label));

				if(label != null){
					if(label.startsWith("@string")){
						System.out.println("		 : use_resource_file");
					}
				}

				String fullName = name.startsWith(".") ? packageStr + name : packageStr + "." + name;
				System.out.println("		" + " : "+ fullName);

				//intent-filter
				Node intentFilter = XPathAPI.selectSingleNode(node, "intent-filter");
				if(intentFilter != null){

					//action
					Node actionNode = XPathAPI.selectSingleNode(intentFilter, "action");
					if(actionNode != null){
						String actionName = actionNode.getAttributes().getNamedItem("android:name").getNodeValue();
						System.out.println("		 : action " + actionName);

					}

					//category
					NodeList categories = XPathAPI.selectNodeList(intentFilter, "category");
					if(categories != null){
						for(int j = 0 ; j < categories.getLength() ; j++){
							Node categoryNode = categories.item(j);
							String categoryStr = categoryNode.getAttributes().getNamedItem("android:name").getNodeValue();
							if(categoryStr.contains("LAUNCHER")){
								System.out.println("		 : **********************************Launcher");
							}
							System.out.println("		 : category " + categoryStr);
						}
					}

				}




			}


			/*
					int count = request_nodelist.getLength();
			for(int i=0 ; i < count ; i++) {

				Node manifestNode = request_nodelist.item(i);
				String packageStr = manifestNode.getAttributes().getNamedItem("package").getNodeValue();
				System.out.println("count >> " + i + " : value >> " + packageStr);


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
			*/
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
	        }
	}

	public MANIFEST_XML_HANDLER(){

	}

	@Override
	public String getName() {
        return this.getClass().getName();
    }

	@Override
	public int doAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {

		return RETURN_CONTINUE;
	}

	@Override
	public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {

		String file_name = Environment.getSourceDir() + cm_src.getCOLLECT_ID() + cm_src.getSPATH() + cm_src.getSNAME();
		log.debug("EX_StrutsXmlHandler", "doExtractor : " + file_name);
		return RETURN_CONTINUE;
	}

	@Override
	public int doTDpd(int depth, TDpd tdpd, CM_SRC cm_src, CM_OBJ cm_obj,int seq) throws SQLException {
		return RETURN_CONTINUE;
	}

	@Override
	public int doTObj(int depth, CM_SRC cm_src, TObj tobj, long parent_object_id) {
		return RETURN_CONTINUE;
	}

	@Override
	public long generateGID(String prefix, TDpd tdpd) {
		return 0L;
	}

	@Override
	public long generateGID(String prefix, TObj tobj) {
		return 0L;
	}





}
