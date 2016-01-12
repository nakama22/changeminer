package changeminer.HandlerForRA;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class RESOURCE_XML_HANDLER extends HandlerForRA {

	public enum RESOURCE_VALUES_TYPE{
		UNKOWN,
		ARRAYS,
		ATTRS,
		COLORS,
		IDS,
		STRINGS,
		STYLES
	}

	public enum RESOURCE_TYPE{
		UNKOWN,
		ANIMATION,
		ANIM,
		COLOR,
		DRAWABLE,
		LAYOUT,
		MENU,
		RAW,
		VALUES,
		XML
	}

	public static void main(String[] args) {

		//String xmlpath = "C:\\Sample\\Android\\Android2.3.3\\ApiDemos\\res\\values\\arrays.xml";
		String xmlpath = "C:\\Sample\\Android\\Android2.3.3\\ApiDemos\\res\\values\\attrs.xml";
		//String xmlpath = "C:\\Sample\\Android\\Android2.3.3\\ApiDemos\\res\\values\\colors.xml";
		//String xmlpath = "C:\\Sample\\Android\\Android2.3.3\\ApiDemos\\res\\values\\ids.xml";
		//String xmlpath = "C:\\Sample\\Android\\Android2.3.3\\ApiDemos\\res\\values\\strings.xml";
		//String xmlpath = "C:\\Sample\\Android\\Android2.3.3\\ApiDemos\\res\\values\\styles.xml";
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document = null;

		try {

			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse( new File(xmlpath) );

			RESOURCE_VALUES_TYPE vType = checkValuesType(document);
			RESOURCE_TYPE rType = checkResourceType(xmlpath);

			System.out.println(" type >> " + vType +" rType >> " + rType);

			if(rType == RESOURCE_TYPE.VALUES){

				if(vType == RESOURCE_VALUES_TYPE.STRINGS){
					NodeList nodes = XPathAPI.selectNodeList(document, "/resources/string");
					for(int i = 0; i < nodes.getLength(); i++){
						String values = nodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
						System.out.println("STRINGS " + i + " : " + values);
					}

				}else if(vType == RESOURCE_VALUES_TYPE.ARRAYS){
					NodeList nodes = XPathAPI.selectNodeList(document, "/resources/string-array");
					for(int i = 0; i < nodes.getLength(); i++){
						String values = nodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
						System.out.println("ARRAYS " + i + " : " + values);
					}

				}else if(vType == RESOURCE_VALUES_TYPE.ATTRS){
					NodeList nodes = XPathAPI.selectNodeList(document, "/resources/declare-styleable");
					for(int i = 0; i < nodes.getLength(); i++){
						String values = nodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
						System.out.println("ATTRS " + i + " : " + values);
					}

				}else if(vType == RESOURCE_VALUES_TYPE.COLORS){
					NodeList nodes = XPathAPI.selectNodeList(document, "/resources/drawable");
					for(int i = 0; i < nodes.getLength(); i++){
						String values = nodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
						System.out.println("COLORS drawable " + i + " : " + values);
					}

					nodes = XPathAPI.selectNodeList(document, "/resources/color");
					for(int i = 0; i < nodes.getLength(); i++){
						String values = nodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
						System.out.println("COLORS " + i + " : " + values);
					}

				}else if(vType == RESOURCE_VALUES_TYPE.IDS){
					NodeList nodes = XPathAPI.selectNodeList(document, "/resources/item");
					for(int i = 0; i < nodes.getLength(); i++){
						String values = nodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
						System.out.println("IDS " + i + " : " + values);
					}

				}else if(vType == RESOURCE_VALUES_TYPE.STYLES){
					NodeList nodes = XPathAPI.selectNodeList(document, "/resources/style");
					for(int i = 0; i < nodes.getLength(); i++){
						String values = nodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
						System.out.println("STYLES " + i + " : " + values);
					}

				}
			}








		} catch (Exception e) {
			e.printStackTrace();

		} finally {
	        }
	}

	private static RESOURCE_TYPE checkResourceType(String xmlpath) {

		Path path = Paths.get(xmlpath);
		String resourceFile = path.getName(path.getNameCount()-1).toString().toLowerCase(); // fileName
		String resourceType = path.getName(path.getNameCount()-2).toString().toLowerCase(); // type
		String resourceDir = path.getName(path.getNameCount()-3).toString().toLowerCase(); // res dir

		if(resourceDir.equals("res")){
			if(resourceType.equals("animator")){
				return RESOURCE_TYPE.ANIMATION;
			}else if(resourceType.equals("anim")){
				return RESOURCE_TYPE.ANIM;
			}else if(resourceType.equals("color")){
				return RESOURCE_TYPE.COLOR;
			}else if(resourceType.equals("drawable")){
				return RESOURCE_TYPE.DRAWABLE;
			}else if(resourceType.equals("layout")){
				return RESOURCE_TYPE.LAYOUT;
			}else if(resourceType.equals("menu")){
				return RESOURCE_TYPE.MENU;
			}else if(resourceType.equals("raw")){
				return RESOURCE_TYPE.RAW;
			}else if(resourceType.equals("values")){
				return RESOURCE_TYPE.VALUES;
			}else if(resourceType.equals("xml")){
				return RESOURCE_TYPE.XML;
			}
		}

		return RESOURCE_TYPE.UNKOWN;
	}


	private static RESOURCE_VALUES_TYPE checkValuesType(Document document) {

		Node check = null;

		try{

			check = XPathAPI.selectSingleNode(document,  "/resources/string");

			if(check != null){
				return RESOURCE_VALUES_TYPE.STRINGS;
			}
			check = XPathAPI.selectSingleNode(document,  "/resources/string-array");
			if(check != null){
				return RESOURCE_VALUES_TYPE.ARRAYS;
			}
			check = XPathAPI.selectSingleNode(document,  "/resources/declare-styleable");
			if(check != null){
				return RESOURCE_VALUES_TYPE.ATTRS;
			}
			check = XPathAPI.selectSingleNode(document,  "/resources/drawable");
			if(check != null){
				return RESOURCE_VALUES_TYPE.COLORS;
			}
			check = XPathAPI.selectSingleNode(document,  "/resources/color");
			if(check != null){
				return RESOURCE_VALUES_TYPE.COLORS;
			}
			check = XPathAPI.selectSingleNode(document,  "/resources/item");
			if(check != null){
				return RESOURCE_VALUES_TYPE.IDS;
			}
			check = XPathAPI.selectSingleNode(document,  "/resources/style");
			if(check != null){
				return RESOURCE_VALUES_TYPE.STYLES;
			}

		}catch(Exception e){

		}
		return RESOURCE_VALUES_TYPE.UNKOWN;
	}

	public RESOURCE_XML_HANDLER(){

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
