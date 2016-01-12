/*
��� Ȯ�� : 
SELECT a.spath, a.sname, b.obj_name FROM CM_SRC a, CM_OBJ b
WHERE a.SRC_ID = b.SRC_ID
AND a.SNAME LIKE '%.xml'
AND a.SRC_ID != b.obj_id
AND NOT EXISTS (
	SELECT 1
    FROM CM_OBJ_DPD x1, CM_SQL x2
    WHERE x1.CALLER_OBJ_ID = b.OBJ_ID
    AND x2.SEQ = 1
    AND x2.SUCCESS_YN = 'Y'
);    
 
insert into cm_type( type_id,type_gubun,type_name,display_yn,compiler_code,type_desc,is_file,is_target,ltl_tgt_id,lang_code,callee_type_id,display_name,is_fa_target,is_classdiagram,is_lifeline,is_activation,is_message_line,is_fd_object,display_check_yn,is_ct_target ) values (821171,0,'SQL XML File','Y',8211,'SQL�������� (XML)','Y','Y',null,null,null,'SQL XML File','N',null,null,null,null,null,'Y',null);
insert into cm_type( type_id,type_gubun,type_name,display_yn,compiler_code,type_desc,is_file,is_target,ltl_tgt_id,lang_code,callee_type_id,display_name,is_fa_target,is_classdiagram,is_lifeline,is_activation,is_message_line,is_fd_object,display_check_yn,is_ct_target ) values (821184,0,'XML - SQL ID','Y',8211,null,'N','N',null,null,null,null,'N',null,null,null,null,null,'Y',null);
insert into cm_type( type_id,type_gubun,type_name,display_yn,compiler_code,type_desc,is_file,is_target,ltl_tgt_id,lang_code,callee_type_id,display_name,is_fa_target,is_classdiagram,is_lifeline,is_activation,is_message_line,is_fd_object,display_check_yn,is_ct_target ) values (8211920,'Result Java Class','Y',8211,null,'N','N',null,null,null,null,'N',null,null,null,null,null,'Y',null);

 */ 

package changeminer.HandlerForRA;

/* 
 * 2013.06.24 property �� null �� ��쿡 ���� ó�� �߰�
 * 2013.06.18 getNode_Text_Replace ��
 * 2013.06.05 compare null �� ��� ���� ����
 * 2013.04.23 �� namespace�� object �� �߰� �Ͽ� ������� ���... namespace�� �߰� ���� ����
 * 2012.11.19 Dom �ļ����� ExtractorXML �ļ��� ���� �۾� 
 * 2012.06.27 DPD null ó�� 
 * 2012.06.29 ���ǹ��� ������ �б� SQL�� ������ ����
 *            qry1 + cond1 -> qry1 �� ù��° ���� , qry1 + cond1 �� �ι�° ������ ����.
 * 2012.05.14 compareValue �߰�
 * 2012.05.04 include ������ �ܺ� ���� ȣ�� �ϴ� ��찡 �־� �̿� ���� �߰�
 * 2012.04.12���� sql���� include�ϴ� ���
 * 2010.06.28 list_copyadd ��ƾ ���� ���� Ű���� ���� ��� ���� ��尡 copy �Ǵ� ���� ����
 * 2010.04.29 {call dosp_vp_ass_070telno_pkg.dosp_process_070_did(?,?,?,?,?,?,?,?,?,?,?,?,?)} �̷��� �� �κ���
 *            {} ����
 * 2010.04.28 SQLFILE_NODE ����
 *            SqlMap �� ���� ó�� �߰�.
 *            select, insert, update, delete, procedure ó�� �߰�.
 * 2010.03.17 SQL ������ �߸� �����Ǵ� ���� ����.
 *  ����� �ô� FileLogOut log = new FileLogOut();, main�� log.open , log.close �ּ� ����
 * IBATIS �����ӿ����� ������� SQL�� ���� ���� �۾�
 * SQL ���� ������ �߿��ϳ� xmlHandler ������ �� �������� �� ���
 * ���� dom�� �̿� -> �̷� ���� SQL ������ ������� ����....
 * xml �ڵ鷯�� �ٽ� ������ ���⼭ ������ ���� 
 * �� �ӵ��� 2���� �ļ��� ������ �ϹǷ� ���� ���� �ӵ��� ���� ��...

-> property , compare, condition ���� üũ
-> pseq �ִ� ���� ã�� �ִ� ������ ������� üũ 
   property ������ ������ ������ �ű� �̹Ƿ� list �� ��� add
                          �ְ� compare 1. ���� ������  not ���ǿ� �ش��ϴ� �κ��� ���� (���� ���  ��� ����)
                                                   �űԷ� ���ο� list ���� �� �� ��常 add

                         2. ������ �ش� list���� add

                         3. �� isNotEqual�̸�
                                                        ������� node �������� not equal �� ���ݵǴ� ��� ����
 */

import java.io.File; 
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

// import org.xml.sax.InputSource;

import com.itplus.cm.ce.addon.common.custom.HandlerForRA;
import com.itplus.cm.ce.internal.meta.CM_SRC;
import com.itplus.cm.ce.util.DBUtil;
import com.itplus.cm.ce.util.Environment;
import com.itplus.cm.ce.util.FileUtil;
import com.itplus.cm.parser.common.CMParserCommonData;
//1.5 ����import
//import com.sun.org.apache.xpath.internal.XPathAPI;
//1.4 ���� import
//import org.apache.xpath.XPathAPI; 

import extractor.common.tobj.TDpd;
import extractor.common.tobj.TLocation;
import extractor.common.tobj.TObj;
import extractor.common.tobj.TResult;  


public class HD_COM_IBATIS_V3_XML  extends HandlerForRA {

// 	FileLogOut log = new FileLogOut();

	// FILE id
	protected final int  SQLXML_FILE = 821171;

	// obj
	protected final int  STATMENT_ID = 821184;
	// OBJECT id
	//protected final int  SQLFILE_NODE = 821175;

	// dpd id
	final int JAVA_RESULT_CLASS = 8211920; // Call Result Class 
//	final int PROP_CODE = 8211900;//prop ��� ������Ʈ�ڵ� ex)<prop key="/committee/pointConversionRule.do">pointConversionRule</prop>
//	final int PROP_CLASS = 8211900;//�޼��� �����ڵ� ex)class.pointConversionRule;
	final int SQL = 1009001; 

	int seqcount;

	/***************************************************************
	 * IBATIS SQLMAP
	 ***************************************************************/	
	final static String IS_EQUAL = "isEqual";
	final static String IS_NOT_EQUAL = "isNotEqual";
	final static String IS_EMPTY = "isEmpty";
	final static String IS_NOT_EMPTY = "isNotEmpty";
	final static String IS_ITERATE = "iterate";
	final static String IS_TEXT = "isText";
	//2012.04.12���� sql���� include�ϴ� ���
	final static String IS_INCLUDE = "include";
	final static String IS_TEXT_PROPERTY = "__text__property";

	boolean serverflag = false; 
	HashMap hmParameterClass = null;
	HashMap rmParameterId = null;
	String NameSpace = null;
	String Root_node = "";

	// 2012.05.04 include ������ �ܺ� ���� ȣ�� �ϴ� ��찡 �־� �̿� ���� �߰�
	HashMap xmllist = null; 
	long collect_id = 0L;



	public HD_COM_IBATIS_V3_XML() {
		hmParameterClass = new HashMap();
		rmParameterId = new HashMap();
	}

	/**
	 *
	 **/ 
	public String getName() {
		return this.getClass().getName();
	}


	public int addAnalyzeStep(CMParserCommonData data, CM_SRC cm_src, TResult tresult) throws Exception {
		collect_id = cm_src.getCOLLECT_ID();
		String file_name = Environment.getSourceDir() + collect_id + cm_src.getSPATH() + cm_src.getSNAME();
		//if (!cm_src.getSNAME().equals("rsltinptdao_sqls.xml")) {
		TObj obj_root = tresult.getTObjList()[0];
		//2012.04.12���� sql���� include�ϴ� ���
		ExtractorXML xml = new ExtractorXML(file_name);
		xml.doAnalyze();
		String nodename  = xml.getRootNode().getName();
		if( nodename.equals("sqlMap") ){ 
			addstep(cm_src, nodename,  xml,  file_name, obj_root, true);
		}
	

		return RETURN_BREAK;
	}  

	// 2012.05.04 include ������ �ܺ� ���� ȣ�� �ϴ� ��찡 �־� �̿� ���� �߰�
	/*
	 * <include refid="RealtimeAsset.assetClss"/>
	 */
	public void read_xmllist() {
		if (xmllist != null)
			return ;
		xmllist = new HashMap();
		String sql = "SELECT  spath || sname fpath, SUBSTR(sname,1,INSTR(sname,'.')-1) NAME FROM CM_SRC a "
			+ " WHERE sname LIKE '%.xml' "
			+ " AND a.CHANGE_REASON_CODE < 9000"
			+ " AND a.collect_id = ? ";
		Connection conn = null;

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection(true);  
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, collect_id);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				String fpath = rs.getString(1);
				String name = rs.getString(2);
				xmllist.put(name, fpath);
				log.debug("push", "name=" + name + ",fpath="+fpath);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			DBUtil.closeResource(conn, pstmt, rs);
		}
	}
	/*
	 * dom �ļ��� ���� �м� ���� ��ƾ.
	 */
	public void addstep(CM_SRC cm_src, String root_node, ExtractorXML xml, String filename, TObj obj_root, boolean sflag) throws Exception {
		Root_node = root_node;
		cm_src.setTYPE_ID(SQLXML_FILE);
		obj_root.setType(SQLXML_FILE);
		serverflag = sflag;
		try{
			hmParameterClass.clear();
			rmParameterId.clear();
			NameSpace = null;

			doExtractRoot(xml,  "/" + Root_node , obj_root);
			doExtractTypeAlias(xml,  "/" + Root_node + "/typeAlias", obj_root);
			doExtractTypeAlias(xml,  "/" + Root_node + "/resultMap", obj_root);
			doExtractStatement(xml,  "/" + Root_node + "/select", obj_root);
			doExtractStatement(xml,  "/" + Root_node + "/insert", obj_root); 
			doExtractStatement(xml,  "/" + Root_node + "/update", obj_root);
			doExtractStatement(xml,  "/" + Root_node + "/delete", obj_root);
			doExtractStatement(xml,  "/" + Root_node + "/procedure", obj_root);
			doExtractStatement(xml,  "/" + Root_node + "/statement", obj_root);

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	/********************************************************************************
	 * Root ������ ����...
	 * 	<sqlMap namespace="VCMT0100P">
	 ********************************************************************************/
	private void doExtractRoot (ExtractorXML xml,  String xPathEnv, TObj obj_root) throws Exception {
		log.debug("xPathEnv", xPathEnv);
		String namespace = xml.getAttributeValue(xPathEnv, "namespace" );
		if (namespace != null) {
			NameSpace = namespace;
		}
	}
	/********************************************************************************
	 * typeAlias ���� ������ ���� ����
	 * 	<typeAlias alias="VCMT0100P1" type="bora.VCMT0100P.model.VCMT0100P_out1" />
	 ********************************************************************************/
	private void doExtractTypeAlias (ExtractorXML xml, String xPathEnv, TObj obj_root) throws Exception {

		
		for(int i=0 ;  ; i++) {
			String str = xPathEnv+"[" + i + "]";
			TObj obj = xml.getNode(str);
			if (obj == null)
				break;
			if( obj.getName().equals("resultMap") ){
				String rmClass =  xml.getAttributeValue(obj, "class");
				String rmId = xml.getAttributeValue(obj, "id");
				rmParameterId.put(rmId, rmClass);
			}else{
				String alias =   xml.getAttributeValue(obj, "alias" );
				String type =   xml.getAttributeValue(obj, "type" );

				hmParameterClass.put(alias, type);
			}
		}//for i	

	}
	//2012.04.12���� sql���� include�ϴ� ���
	private void includeSql (int pseq, int depth, ExtractorXML xml, String Refid, ArrayList node) throws Exception{
		String name1, name2;
		if (Refid.indexOf(".") >0) {
			name1 = Refid.substring(0, Refid.indexOf("."));
			name2 = Refid.substring(Refid.indexOf(".")+1);
			log.debug("name1", name1);
			log.debug("name2", name2);
			// �ڽ��� namespace �̸� namespace ���� 
			if (name1.equals(NameSpace)) {
				;
			} else {
				read_xmllist();
				if(xmllist.containsKey(name1)) {
					String file_name = Environment.getSourceDir() + collect_id + xmllist.get(name1);
					log.debug("new read file", file_name);
					ExtractorXML newxml = new ExtractorXML(file_name);
					newxml.doAnalyze();

					
					for(int i=0; ;i++){
						String str = "/" + Root_node + "/sql[" + i + "]";
						String statement_id = newxml.getAttributeValue(str, "id");
						if (statement_id == null)
							break;
						if( name2.equals(statement_id)){
							TObj obj = newxml.getNode(str);
							doExtractStatement_SubObjects(pseq, depth, obj.getName(), obj.getTObjList(), node, newxml);
							return ;
						}
					}
				} else {
					log.debug("not found id", name1);
				}
			}

		} else {
			name2 = Refid;
		}


		for(int i=0; ;i++){
			String str = "/" + Root_node + "/sql[" + i + "]";
			String statement_id = xml.getAttributeValue(str, "id");
			if (statement_id == null)
				break;
			if( name2.equals(statement_id)){
				TObj nd = xml.getNode(str);
				doExtractStatement_SubObjects(pseq, depth,nd.getName(), nd.getTObjList(), node, xml);
				return ;
			}
		}

		return ;
	}


	/********************************************************************************
	 * Statement ���� ���� ������ ���� ����
	 ********************************************************************************/
	private void doExtractStatement (ExtractorXML xml, String xPathEnv, TObj obj_root) throws Exception {
		for(int i=0 ;  ; i++) {
			String statement_id = xml.getAttributeValue(xPathEnv+"[" + i + "]", "id");
			if (statement_id == null)
				break;
			TObj nd = xml.getNode(xPathEnv+"[" + i + "]");
				// ---------------------------------------------------------------------------
				// xml �ڵ鷯�� ���ؼ� ���� ��ȣ�� ���´�. 
				TLocation tloc = nd.getTLocation();
				if (tloc == null) {
					tloc = new TLocation();
				} else {
					log.trace("Start Location ===================" , "" + tloc.getStartLine());
				}
				// -------------------------------------------------------------------------

				log.debug("$$$$$$$$$$$$$statment id =" , statement_id );
				TObj obj = null;
				String extern_target = extern_target_make(NameSpace, statement_id);
				if(extern_target != null) {
					obj = new TObj(STATMENT_ID, extern_target, extern_target, 100, tloc );
				} else { 
					if (NameSpace != null) {
						if (statement_id.startsWith(NameSpace + "." )) { // 2013.04.23 �� namespace�� object �� �߰� �Ͽ� ������� ���...
							obj = new TObj(STATMENT_ID, statement_id, statement_id, 100, tloc );
						}
						else {
							obj = new TObj(STATMENT_ID, NameSpace + "." + statement_id, NameSpace + "." + statement_id, 100, tloc);
						}
					} else {
						obj = new TObj(STATMENT_ID, statement_id, statement_id, 100, tloc );
					}
				}
				obj_root.add(obj);

				// <select id="init1VCMT0100P" resultClass="VCMT0100P1" parameterClass="java.util.HashMap">
				// ���̻�� parameterClass ====>>parameterMap resultClass  ===> resultMap
				String parameterClass =  xml.getAttributeValue(nd, "parameterClass" );
				String resultClass =  xml.getAttributeValue(nd,"resultClass" );
				String resultClassId =  (String)rmParameterId.get(resultClass);
				// 2013.03.07 parameterClass != null  �߰�
				if(parameterClass != null && parameterClass.trim().length() > 0 && hmParameterClass.get(parameterClass) != null){
					log.debug( "<<-----[typeAlias obj:" + statement_id + "] " , "[parameterClass dpd:" + hmParameterClass.get(parameterClass) + "]----->>" );
					//hmParameterClass.get(parameterClass);
					TDpd dpd_class = new TDpd(JAVA_RESULT_CLASS, (String)hmParameterClass.get(parameterClass), (String)hmParameterClass.get(parameterClass), 100, tloc);
					//System.out.println("dpd_parameter_class :::" + dpd_class.getGID() + "-" + dpd_class.getName());
					obj.add(dpd_class);
				} 
				// 2013.03.07 resultClass != null  �߰�
				if(resultClass != null && resultClass.trim().length() > 0 && hmParameterClass.get(resultClassId) != null){
					log.debug( "<<-----[typeAlias obj:" + statement_id + "] " , "[resultClass dpd:" + hmParameterClass.get(resultClass) + "]----->>" );
					//hmParameterClass.get(resultClass);
					TDpd dpd_class = new TDpd(JAVA_RESULT_CLASS, (String)hmParameterClass.get(resultClassId), (String)hmParameterClass.get(resultClassId), 100, tloc);
					//System.out.println("dpd_result_class :::" + dpd_class.getGID() + "-" + dpd_class.getName());
					obj.add(dpd_class);
				}

				/** Statement����  ���� ������Ʈ ����**/
				ArrayList node = new ArrayList();
				seqcount = 0;
				doExtractStatement_SubObjects( seqcount, 0, nd.getName(), nd.getTObjList(), node, xml );
				make_sql(node, obj, tloc);
			}
	}
	// id ���� �ʿ�� ������ ���
	public String extern_target_make(String namespace, String statmentid) {
		return null; 
	}
	
	/*
	 * 
1. ������ ���� property��  ������ + 
2. ������ ���� property�� ������
	if EQ �����̸�
		compare =
			+ ����
		compare <>
			list���� property �� �ش��ϴ� ���� ���� +
			�ڽ� + 
	if ~EQ �����̸�
		compare '-' �������� ����.

seq �� ���� �Ǵ� ��� pseq �� ã��
pseq ���� seq�� ã�� �ٽ� pseq�� ã�� �ݺ�... ����
	 */

	private void make_sql(ArrayList nodedata, TObj obj, TLocation tloc) throws Exception {

		for ( int i = 0; i < nodedata.size(); i ++) {
			Node_Data  nd = (Node_Data)nodedata.get(i);
			log.trace("",  makespace(nd.depth) + "seq="  + nd.seq  
					+ ", pseq="  + nd.pseq 
					+ ", property="  + nd.property  
					+ ", compare="  + nd.compare  
					+ ", condition="  + nd.condition  
					+ ", prepend="  + nd.prepend  
					+ ", depth="  + nd.depth 
					+ ", data="  + nd.data );  

		}

		Node_List ndlist = new Node_List();
		ndlist.make_sql(nodedata);

		ArrayList list = ndlist.getlist();
		log.trace("list size", "" + list.size());

		for ( int i = 0; i < list.size() && i < 20; i ++) {
			ArrayList node = (ArrayList)list.get(i);
			StringBuffer sb = new StringBuffer();
			for ( int j = 0; j < node.size(); j ++) {
				Node_Data nd = (Node_Data)node.get(j);
				log.trace("[" + i + "]", "seq=" + nd.seq + ",pseq="+nd.pseq +",p=" + nd.property + ",c="+nd.compare+",s="+nd.condition );
				// prepend �� �ְ� ���� �����Ͱ� ������ prepend�� ���� �ش�.
				if (nd.prepend != null && nd.prepend.length() > 0 && (j+1) < node.size()) {
					log.trace("ADD PREPAND[" + i + "]", "prepend="+nd.prepend );
					sb.append(" " + nd.prepend+ " ");
				}
				sb.append(nd.data);
				sb.append("\r\n");  
			}
			String sql = getNode_Text_Replace(sb.toString());
			sql = sql.replaceAll("&gt;", ">");
			sql = sql.replaceAll("&lt;", "<");
			log.debug("ADD SQL["+i+"] " + obj.getName(), sql); 
			if (serverflag) {
				if (sql.trim().length() > 0) {
					TDpd dpd = new TDpd(SQL, sql, sql, 100, tloc );
					obj.add(dpd);
				}
			}
		}

	}


	/*
	 * CDDATA, TEXT ���� ������ ����� ������ ���� ��� ó��
	 */
	//boolean parFlag = false; //prepend �Ӽ��� AND�� ��� �Ʒ� �ڽĳ��� value�� AND�� �߰��ϱ����� flag��
	//int parCount = 0;  //prepend �Ӽ��� AND�� ��� �Ʒ� �ڽĳ��� value�� AND�� �߰��ϱ����� int��
	private void doExtractStatement_SubObjects (int pseq, int depth, 
			String ndname, TObj  subobj[],  ArrayList node, ExtractorXML xml) throws Exception {

	// System.out.println("pseq=" + pseq + ", depth=" + depth + ",nodename = " + nodename);

		if( subobj==null ) return ;

		int count = subobj.length;

		depth ++;
		for(int i=0 ; i < count ; i++) {
			TObj nd = subobj[i];
			int ndType = nd.getType();

			if( ndType== xml._TEXT_TYPE) { // SQL ���� �Ϻ�

				String addstr = nd.getName();
				if (addstr.trim().length() == 0)
					continue ; 
				seqcount++;
				Node_Data node_data = new Node_Data(seqcount, pseq, depth, IS_TEXT_PROPERTY, "", IS_TEXT, addstr );

				node.add(node_data);

				/* -----------------------------------

				String addstr = nd.getNodeValue();

				if( i==parCount-1 && parCount!=1 ){
					parFlag=false;//prepend ����� �ڽĳ���� �ش� AND�� �߰� �Ǿ�� �ϴ�
					parCount =0; //������ ������峪 �ؽ�Ʈ �ϰ�� �ʱ�ȭ ����.
				}

				if (addstr.trim().length() == 0){
					continue ;
				}

				if( parFlag==true && ndType==TEXT_NODE ){
					if(i!=0){
						if(ndList.item(i-1).getNodeType()==CDATA_SECTION_NODE){
							log.debug("Before CDATA", addstr);
						}else{
							addstr = "AND "+addstr;
							if(parCount ==1){
								parFlag=false;
							}
						}
					}else{
						addstr="AND "+addstr;
						if(parCount==1){
							parFlag=false;
						}
					}
				}else{
					log.debug("NOT ADD AND", nd.getNodeValue());
				}


				seqcount++;
				Node_Data node_data = new Node_Data(seqcount, pseq, depth, IS_TEXT_PROPERTY, "", IS_TEXT, addstr );

				node.add(node_data);

				----------------------------------------------------- */

			} else if( ndType== xml._NODE_TYPE ) { // ���� ��
				/*[Map �Ӽ����� ���]*/		
				String name = nd.getName();
				if (name.equals(IS_ITERATE)) {
					// <iterate property="execdeptcd" open="AND xop.execdeptcd IN (" close=")" conjunction=",">#execdeptcd[]#</iterate>
					String property =  xml.getAttributeValue(nd, "property" );
					String open_str =  xml.getAttributeValue(nd, "open" );
					String close_str =  xml.getAttributeValue(nd, "close" );
					String str = open_str + " ? " + close_str;
					seqcount++;
					Node_Data node_data = new Node_Data(seqcount, pseq, depth, property, "", IS_ITERATE, str );
					node.add(node_data);
				}else if( name.equals(IS_INCLUDE) ){ 
					String refid = xml.getAttributeValue(nd,  "refid");
					//seqcount++;
					//Node_Data node_data = new Node_Data(seqcount, pseq, depth, refid, "include", IS_EQUAL , "" );

					//node.add(node_data);
					includeSql(pseq, depth, xml, refid, node); 

				}else {
					// <isEqual property = "reptflag" compare = "02">
					// <isNotEqual property = "reptflag" compare = "02"> 
					String property =  xml.getAttributeValue(nd, "property" );
					String compare =  xml.getAttributeValue(nd, "compare" );
					if (compare == null || compare.trim().length() == 0)
						compare =  xml.getAttributeValue(nd, "compareValue" );
					
					String prepend =  xml.getAttributeValue(nd, "prepend" );
					/*
					if(prepend.equals("AND")){
						parFlag=true;
						parCount=nd.getChildNodes().getLength();
						prepend=null;
					}
					 */
					seqcount++;
					Node_Data node_data = new Node_Data(seqcount, pseq, depth, property, compare, name, "" , prepend);

					node.add(node_data);

					doExtractStatement_SubObjects(seqcount, depth+1,    
							nd.getName(), nd.getTObjList(), node, xml);
				}
			} 

		}//for i

	}  


	class Node_Data {
		int seq; // seq : �ڽ��� seq ��ȣ
		int pseq; // pseq : ���� seq ��ȣ
		int depth; // depth 
		String property; // property 
		String compare; // compare
		String condition; // condtion 
		String prepend;
		String data; // sql data

		public Node_Data(int seq, int pseq, int depth, String property, String compare, String condition, String data) {
			this.seq = seq;
			this.pseq  = pseq;
			this.depth = depth;
			if (property == null) property = "";
			this.property = property;
			if (compare == null) compare = "";
			this.compare = compare; 
			if (condition == null) condition = "";
			this.condition = condition; 
			this.prepend = "";
			this.data = data;
		}
		public Node_Data(int seq, int pseq, int depth, String property, String compare, String condition, String data, String prepend) {
			this.seq = seq;
			this.pseq  = pseq;
			this.depth = depth;
			if (property == null) property = "";
			this.property = property;
			if (compare == null) compare = "";
			this.compare = compare;
			if (condition == null) condition = "";
			this.condition = condition;  
			if (prepend == null) prepend = "";
			this.prepend = prepend;
			this.data = data;
		}
	}


	class Node_List {
		ArrayList list;
		public Node_List () {
			list = new ArrayList();
		}
		// property �� �̹� ������ �ִ��� üũ
		private boolean find_property(ArrayList seqlist, Node_Data newnd) {
			for (int i = 0;i < list.size(); i ++) {
				ArrayList node = (ArrayList)list.get(i);
				//  ������ node�� �θ� ��� ���� ã�� ��.
				if (check_parent_node(seqlist, i)) {
					for ( int j = 0; j < node.size(); j ++) {
						Node_Data nd = (Node_Data)node.get(j);
						if (nd.property.equals(newnd.property)) 
							return true;
					}
				}
			}
			return false;
		}
		// property, compare�� ���Ѱ��� ������ �ű� �������� üũ
		private boolean find_allcompare(ArrayList seqlist, Node_Data newnd) {
			for (int i = 0;i < list.size(); i ++) {
				ArrayList node = (ArrayList)list.get(i);
				if (check_parent_node(seqlist, i)) {
					for ( int j = 0; j < node.size(); j ++) {
						Node_Data nd = (Node_Data)node.get(j);
						if ( data_compare(nd, newnd, true) ) 
							return true;
					}
				}
			}
			return false;
		}
		// 2���� ���ǿ� ���� ��
		// SQL �б⿡ ���� ������ �Ǵ��ϴ� �������� ���� �߿� ��.
		private boolean data_compare(Node_Data nd, Node_Data newnd, boolean existflag) {
			if (nd.property.equals(newnd.property)) {
				log.debug("nd " + nd.property, "" + nd.compare);
				
				// nd.compare != null ���� �߰� 2013.3.7  
				if (nd.compare != null && nd.compare.equals(newnd.compare) &&
						nd.condition.equals(newnd.condition))  {
					log.trace("data_compare", "all same");
					return true; // ���� �����̸� true
				}
				
				if (newnd.condition.equals(IS_EMPTY)) {
					if (nd.condition.equals(IS_EMPTY)) {
						log.trace("IS_EMPTY", "IS_EMPTY");
						return true;
					}
					
				}
				else if (newnd.condition.equals(IS_NOT_EMPTY)) {
					if (nd.condition.equals(IS_NOT_EMPTY)) {
						log.trace("IS_NOT_EMPTY", "IS_NOT_EMPTY");
						return true;						
					}
				}
				else if (newnd.condition.equals(IS_EQUAL)) { 
					if(!existflag) {
						if (nd.condition.equals(IS_NOT_EMPTY)) {
							log.trace("IS_EQUAL", "IS_NOT_EMPTY");
							return true;						
						}
					}
					if (nd.condition.equals(IS_EQUAL) && nd.compare.equals(newnd.compare)) {
						log.trace("IS_EQUAL", "IS_EQUAL compare "+ newnd.compare);
						return true;	
					}
					if (nd.condition.equals(IS_NOT_EQUAL) && !nd.compare.equals(newnd.compare))  {
						log.trace("IS_EQUAL", "IS_NOT_EQUAL compare my= "+ newnd.compare+ ", you= " + nd.compare);
						return true;									
					}
				}
				else if (newnd.condition.equals(IS_NOT_EQUAL)) { 
					if(!existflag) {
						if (nd.condition.equals(IS_NOT_EQUAL)) {
							log.trace("IS_NOT_EQUAL", "IS_NOT_EQUAL");
							return true;						
						}
					}
					if (nd.condition.equals(IS_NOT_EQUAL) && nd.compare.equals(newnd.compare)) {
						log.trace("IS_NOT_EQUAL", "IS_NOT_EQUAL compare my= "+ newnd.compare+ ", you= " + nd.compare);
						return true;	
					}
					else if (nd.condition.equals(IS_EQUAL) && nd.compare.equals(newnd.compare))  {
						log.trace("IS_NOT_EQUAL", "IS_EQUAL compare my= "+ newnd.compare+ ", you= " + nd.compare);
						return true;
					}
				}
			}
			/*
			if (nd.property.equals(newnd.property)) {
				if (nd.compare.equals(newnd.compare) &&
						nd.condition.equals(newnd.condition))  {
					return true; // ���� �����̸� true
				}

				if (nd.condition.equals(IS_EMPTY)) {
					if (newnd.condition.equals(IS_EMPTY)) // empty, empty =
						return true;
					return false;
				} else if (nd.condition.equals(IS_NOT_EMPTY)) { // not empty �ε� ���� empty �����̸�
					if (newnd.condition.equals(IS_EMPTY))
						return false;
					return true;
				}
				if (newnd.condition.equals(IS_EQUAL)) { 
					if (nd.compare.equals(newnd.compare)) 
						return true;
					if (!existflag) { // copy ���ǿ����� ������. ���� ���ο����� üũ ��󿡼� ����
						if (nd.condition.equals(IS_NOT_EQUAL) && !nd.compare.equals(newnd.compare)) 
							return true;
					}
				}
				else if (newnd.condition.equals(IS_NOT_EQUAL)) {  

					if (nd.condition.equals(IS_NOT_EQUAL) && !nd.compare.equals(newnd.compare)) 
						return false;		
					if (!nd.compare.equals(newnd.compare) ) {
						return true;
					}					
				}
			}
			*/
			return false;
		}
		
		// condtion ���ǿ� ���� compare ��
		private boolean find_compare(ArrayList node, Node_Data newnd) {
			for ( int j = 0; j < node.size(); j ++) {
				Node_Data nd = (Node_Data)node.get(j);
				if ( data_compare(nd, newnd, true) ) 
					return true;
			}
			return false;
		}
		
		// seq �� �ش��ϴ� �����Ͱ� ���� ������� üũ
		private boolean find_delpseq(ArrayList delseq, int findseq) {
			for ( int k = 0; k < delseq.size(); k ++) {
				Integer ii=  (Integer)delseq.get(k);
				int seq = ii.intValue();
				// System.out.println("check seq=" + seq +", findseq=" + findseq);
				if (findseq == seq) 
					return true; 
			}
			return false;
		}
		// node�� range�� ���ϴ��� üũ
		private boolean check_parent_node(ArrayList seqlist, int nodeseq) {
			for ( int k = 0; k < seqlist.size(); k ++) {
				Integer ii=  (Integer)seqlist.get(k);
				int seq = ii.intValue();
				// System.out.println("check seq=" + seq +", findseq=" + nodeseq);
				if (nodeseq == seq) 
					return true; 
			}
			return false;		
		}		
		// �ߺ��� �����ϰ� findseq�� add
		private void add_seq(ArrayList seqlist, int findseq) {
			for ( int i = 0; i < seqlist.size(); i ++) {
				Integer ii=  (Integer)seqlist.get(i);
				if (ii.intValue() == findseq)
					return ;
			}
			Integer ii = new Integer(findseq);
			log.trace("arraylist push =" , "" +  findseq);
			seqlist.add(ii);
		}
		// node �߿��� pseq �� �ش��ϴ� �����Ͱ� �ִ��� üũ
		private boolean find_pseq(ArrayList node, int pseq) {
			//log.trace("find_pseq", "" + pseq);
			if (pseq == 0) // �ֻ��� �̸� ������ ok
				return true;
			for ( int j = 0; j < node.size(); j ++) {
				Node_Data nd = (Node_Data)node.get(j);
				if (nd.seq == pseq) {
					return true;
				}
			}
			return false;
		}
//		newnd �� pseq�� �ش��ϴ� �� �θ� ã�� �θ��� node id�� seqlist�� �����Ѵ�
		private void get_parent_node_seq(ArrayList seqlist, Node_Data newnd) {
			for (int i = 0;i < list.size(); i ++) {
				ArrayList node = (ArrayList)list.get(i);	
				//log.trace("find range ", "" + i);
				if (find_pseq(node, newnd.pseq)) {
					log.trace("get_allnode_range node size = " + list.size() , "no = " +  i);
					add_seq(seqlist, i);
				}
			}			
		}


		// list �� �� ��忡 add
		private void list_add(Node_Data newnd) {
			log.trace("list_add" , "" +  newnd.seq);					
			if (list.size() == 0) {
				ArrayList node = new ArrayList();
				node.add(newnd);
				list.add(node); 
				return ;
			}
			for ( int i = 0; i < list.size(); i ++) {
				ArrayList node = (ArrayList)list.get(i);
				if (find_pseq(node, newnd.pseq)) 
					node.add(newnd);
			}
		}

		// property �� �ٸ� �κ��� ���� ����� ���� �� add
		private void make_new_node(ArrayList node, Node_Data newnd) {
			log.trace("new list_copyadd seq=" + newnd.seq,  "property=" + newnd.property + ", condition=" + newnd.condition + ", compare=" + newnd.compare);			
			ArrayList delseq = new ArrayList();
			ArrayList newnode = new ArrayList();
			for ( int j = 0; j < node.size(); j ++) {
				Node_Data nd = (Node_Data)node.get(j);
				//log.trace("nd", "seq="+nd.seq +", pseq="+nd.pseq + ",property="+nd.property +",compare=" + nd.compare +",conditon=" + nd.condition);
				//log.trace("new nd", "seq="+newnd.seq +", pseq="+newnd.pseq + ",property="+newnd.property +",compare=" + newnd.compare +",conditon=" + newnd.condition);
				if (find_delpseq(delseq, nd.pseq)) { 
					;// ���� seq�� ���� ����̸� ������ ��� ����...
					add_seq(delseq, nd.seq);
				} else {
					
					if ( data_compare(nd, newnd, false)) {
						//log.trace("new copy =" , "" +  nd.seq);
						newnode.add(nd);
					} else {
						if (nd.condition.equals(IS_TEXT) || !nd.property.equals(newnd.property)) {
							//log.trace("new copy text =" , "" + nd.seq);
							newnode.add(nd);
						} else {
							add_seq(delseq, nd.seq);
							//log.trace("delete =" , "" + nd.seq);
						}
					}
				}
			}
			newnode.add(newnd);
			list.add(newnode);
			//log.trace("list size  ", " add = " + list.size());
		}

		private void list_addtext(Node_Data newnd) {
			if (list.size() == 0) {
				ArrayList node = new ArrayList();
				node.add(newnd);
				list.add(node);				
				return ;
			}

			for (int i = 0;i < list.size(); i ++) {
				ArrayList node = (ArrayList)list.get(i);
				// pseq�� 
				if (find_pseq(node, newnd.pseq)) {
					log.trace("text add (list_addtext)" , "pseq="+newnd.pseq +",seq=" + newnd.seq);
					node.add(newnd);
				}
			}
		}

		// notequal ������ �Ǵ� ��� ���� ��忡�� compare�� ���� ������ ������ ���� ó��.
		private void del_notequal_node(ArrayList node, Node_Data newnd) {
			log.trace("list_notequal_del seq=" + newnd.seq,  "property=" + newnd.property + ", condition=" + newnd.condition + ", compare=" + newnd.compare);			
			ArrayList delseq = new ArrayList();
			ArrayList newnode = new ArrayList();
			for ( int j = 0; j < node.size(); j ++) {
				Node_Data nd = (Node_Data)node.get(j);
				if (nd.property.equals(newnd.property) &&
						nd.compare.equals(newnd.compare) &&
						!nd.condition.equals(newnd.condition)) {
					add_seq(delseq, nd.seq); // ���� ��� �߰�
				} else {
					if (find_delpseq(delseq, nd.pseq)) { 
						;// ���� seq�� ���� ����̸� ������ ��� ����...
						add_seq(delseq, nd.seq); // ���� ��� �߰�
					} 
					else { // property �� ���� ���ǵ鸸 ���� ���� ����.
						//log.trace("list_notequal_del new copy =" , "" +  nd.seq);
						newnode.add(nd);
					}
				}
			}
			node.clear();
			for ( int i = 0; i < newnode.size(); i ++)
				node.add(newnode.get(i));
		}		
		private void push(Node_Data newnd) {
			log.trace("push" , newnd.property +", seq = " + newnd.seq);
			if (newnd.condition.equals(IS_TEXT)) {
				list_addtext(newnd);
				return ;
			}
			// ù ������ ���ǹ� ���� �����ϴ� ���
			if(list.size() == 0) {
				ArrayList node = new ArrayList();
				node.add(newnd);
				list.add(node);	
				return ; 
			}
			ArrayList seqlist = new ArrayList();
			// pseq�� �ִ� node ����Ʈ�� get 

			// newnd �� pseq�� �ش��ϴ� �� �θ� ã�� �θ��� node id�� seqlist�� �����Ѵ�.
			get_parent_node_seq(seqlist, newnd);
			// �ű� list ������ �ʿ����� üũ 
			/*
			if (find_property(seqlist, newnd) == false) { // �ű� property �̸� ������ �߰�.
				list_add(newnd);
				return ;
			}
			*/
			int nodelen = list.size(); 

			boolean newflag = find_allcompare(seqlist, newnd);
			log.trace("count=", "" + nodelen + ", newflag =" + newflag);
			for (int i = 0;i < nodelen; i ++) {
				ArrayList node = (ArrayList)list.get(i);
				//  ������ node�� �θ� ��� ���� ã�� ��.
				if (check_parent_node(seqlist, i)) {
					if (find_pseq(node, newnd.pseq)) {
						if (find_compare(node, newnd)) { // ���� compare ������ ������..
							if (newnd.condition.equals(IS_NOT_EQUAL)) { // not equal �̹Ƿ� equal ������ ������ ����
								del_notequal_node(node, newnd);
							}
							log.trace("add", "" + newnd.seq);
							node.add(newnd); 
						} 
						else if (!newflag) { // ��ü���� ������ ���� ����.
							make_new_node(node, newnd);
							break;
						} 
					} 
				}
			}    		
		}   
		public ArrayList getlist() {
			return list;
		}
		public void make_sql(ArrayList nodedata) {
			list.clear();
			for ( int i = 0; i < nodedata.size(); i ++) {
				Node_Data  nd = (Node_Data)nodedata.get(i);
				push(nd);
			}
		}
	}  


	String makespace(int depth) {
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < depth * 4; i ++) {
			sb.append(' ');
		}
		return sb.toString();
	}

	/*
	 * SQL ���� �߿� #, $ �� ���ڿ��� ���� ġȯ �۾�.
	 * '' �ȿ� #,$ �� �ִ� ��쿡�� ġȯ���� �ʵ��� �߰� �۾�
	 * 
	 */
	protected String getNode_Text_Replace(String data ) {

		//  char[] mappings = new char[] {'#', '$'};

		boolean bStartMapping = false;
		boolean qflag = false;
		StringBuffer tmpbuf = new StringBuffer();
		data = data.trim();
		// {call dosp_vp_ass_070telno_pkg.dosp_process_070_gen(?,?,?,?,?,null,null,null,null,null,null,?,?)}
		// {} ����.
		if (data.startsWith("{")) {
				data = data.substring(1, data.length());
				if (data.endsWith("}")) data = data.substring(0, data.length() - 1);
		} 
		char[] cList = data.toCharArray();
		String key="";	
		boolean commaflag = false;
		boolean errorflag = false;
		String line = "";
		for (int k = 0; k < cList.length; k++) {
			//log.debug("xx", "["+k+"]" + cList[k]);
			if (cList[k] == '\'') {
				commaflag = false;
				if (qflag == true) {
					// '   ''  '�����ΰ��  '' ���ڴ� skip �� �� �ְ�... 
					if (k + 1< cList.length && cList[k+1] == '\'') {
						line +=cList[k];
						k++;
					} else 
						qflag = false;
				}
				else
					qflag = true;
				line +=cList[k];
				continue; 
			}
			if (qflag) { // '' ���� ���ڴ� �׳� ����.
				line +=cList[k];
				continue;
			}

			
			if( cList[k]=='#' || cList[k] == '$' ) { // #{xxx} 
				//log.debug("getNode_Text_Replace start  " , "----" + cList[k]); 
				if (bStartMapping == true) {
					if(key.indexOf(",") > 0) {
						key = key.substring(0,key.indexOf(","));
					}
					// $aa$ $bb$ ���·� �Ǿ� ������ :aa, :bb�� �ٲ��ֱ����� , �߰� 
					if (commaflag) {
						errorflag = true;
						line +=", :" + key + " ";
						log.debug("error ", line);
					} else {
						line +=" :" + key + " ";
					}
					bStartMapping = false;
					commaflag = true;
				}
				else {
					bStartMapping = true; 
					key= "";
				}
				continue;
			}
			if (bStartMapping == false) {
				if (cList[k] > ' ') // 
					commaflag = false;
				line +=cList[k];
			}
			else {
				if(cList[k] == '{') // #{xxx}  -> { ��ȣ ���� 
					continue;
				else if(cList[k] == '}')  { 
					//log.debug("getNode_Text_Replace }  " , key); 
					if(key.indexOf(",") > 0) {
						key = key.substring(0,key.indexOf(","));
					}
					// $aa$ $bb$ ���·� �Ǿ� ������ :aa, :bb�� �ٲ��ֱ����� , �߰� 
					if (commaflag) {
						errorflag = true;
						line +=", :" + key + " ";
						log.debug("error ", line);
					} else {
						line +=" :" + key + " ";
					}
					log.debug("getNode_Text_Replace key conv  " , line);
					bStartMapping = false;
					commaflag = true;
					continue;
				}				
				else
					key += cList[k];
			}
			if(cList[k] == 0xa) {
				if(errorflag) {
					tmpbuf.append("\r\n");
					log.debug("skip ", line);
				} else
					tmpbuf.append(line);
				line = "";
				errorflag = false;
				commaflag = false;
			}
		}
		if(line.length() >0) {
			if(errorflag) {
				tmpbuf.append("\r\n");
				log.debug("skip ", line);
			} else
				tmpbuf.append(line);
		}


		return tmpbuf.toString();
	}	




	public long generateGID(String prefix, TObj tobj) {
		switch (tobj.getType()) {
		//case SQLFILE_NODE:
		//	return FileUtil.getGID("<FILE>", tobj.getGID());
		case STATMENT_ID:
			return FileUtil.getGID("<SQL_KEY>", tobj.getGID());
		default : 
			return super.generateGID(prefix, tobj);
		}
	}

	public long generateGID(String prefix, TDpd tdpd) {
		switch (tdpd.getType()) {
		case JAVA_RESULT_CLASS:
			return FileUtil.getGID("<JAVA>", tdpd.getGID());
		default : 
			return super.generateGID(prefix, tdpd);
		}
	}
	/**
	 * @param args
	 */
	
	public static void main(String[] args) { 
		// TODO Auto-generated method stub 
		try { 
			HD_COM_IBATIS_V3_XML xml = new HD_COM_IBATIS_V3_XML();
			TObj obj_root = new TObj(1, "","", 100, new TLocation());
	    	//xml.log.open("D:/work/data" + ".log");
	    	ExtractorXML xmlTest = new ExtractorXML("D:\\work\\�ϳ�ĳ��Ż\\TGa0AcsrAccItemC-sqlMap.xml");
	     	xmlTest.doAnalyze();
	     	String root_node = xmlTest.getRootNode().getName();
	     	CM_SRC cm_src = new CM_SRC(0);
	    	xml.addstep(cm_src, root_node, xmlTest, "", obj_root, false);
			//xml.log.close(); 
		} catch (Exception e) { 
			e.printStackTrace(); 
			// TODO: handle exception
		}
	} 
	

	logtest log = new logtest();
	class logtest {
		public void debug(String a, String b) {
			System.out.println(a + "-" + b);
		}
		public void trace(String a, String b) {
			System.out.println(a + "-" + b); 
		}
	}
		 
	
	

}