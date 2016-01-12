package changeminer.HandlerForRA;
/*
 * 2014.10.09
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import extractor.common.tobj.TDpd;
import extractor.common.tobj.TLocation;
import extractor.common.tobj.TObj;

public class ExtractorXML {
	public int _ROOT_TYPE = 0;
	public int _TEXT_TYPE = 1;
	public int _NODE_TYPE = 2;
	private TObj rootnode=null;
	private String file_name;
	local_debug locallog = new local_debug();

	private String charset = System.getProperty("file.encoding");

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
//			ExtractorXML xml = new ExtractorXML("./TLf6PpaaExclBrkdnD-sqlMap.xml", "utf-8");
			ExtractorXML xml = new ExtractorXML("D:/tmp/test.xml","utf-8");


			TObj nd = xml.doAnalyze();

			xml.dump("c:\\a.out");
/*			for ( int i = 0; ; i ++) {
			if (xml.getAttributeValue("/sqlMap/insert[" + i +"]", "id") == null)
				break;
			System.out.println(xml.getAttributeValue("/sqlMap/insert[" + i +"]", "id"));
			}*/

		//System.out.println("!!" + rootnode.getNodeValue("/navigation/action[" + 3 +"]"));

			System.out.println("root="+xml.getRootName());
//		ArrayList arr = node_list.get(3).sublist;

/*		for(int i = 0; i < arr.size() ; i ++){

			node_data ndd = (node_data)arr.get(i);
			if(ndd.nodename.equals("command") ){
				//System.out.println(ndd.tlocation.getStartLine() + " :: " + ndd.nodename + " :: " + ndd.tlocation.getEndLine());
				for(int j =0; j < ndd.sublist.size() ; j ++){
					System.out.println(ndd.sublist.get(j).nodename);
				}
			}
		}
*/
		/*	for(int j =0 ; j < node_list.size(); j ++){

			node_data nd = (node_data)node_list.get(j);
			System.out.println(nd.keylist.get(0).value + "  TLOCATION start :: " + nd.tlocation.getStartLine() + "  TLOCATION end :: " + nd.tlocation.getEndLine());
		}
		System.out.println(rootnode.sublist.get(0).tlocation.getStartLine() + "  "+ rootnode.sublist.get(0).tlocation.getEndLine());
		 */
			// xml.log.close();
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}

	public ExtractorXML(String fname, String charset) {
		file_name = fname;
		this.charset = charset ;
	}
	public ExtractorXML(String fname) {
		file_name = fname;
		this.charset = System.getProperty("file.encoding");
	}
	boolean brokenflag = false;
	public TObj doAnalyze() throws Exception {
		brokenflag =false;
		ArrayList ar = loadfile(file_name);
		rootnode = new TObj(_ROOT_TYPE,  "", "ROOT", 100, new TLocation(1,ar.size(),0,0,file_name));
		analyzeNode(rootnode, ar , "", 0);  // 2014.09.09
		return rootnode;
	}
	public TObj doAnalyze(boolean convflag) throws Exception {
		return doAnalyze(convflag,false);
	}
	public TObj doAnalyze(boolean convflag, boolean brokenflag) throws Exception {
		this.brokenflag = brokenflag;
		ArrayList ar = loadfile(file_name);
		rootnode = new TObj(_ROOT_TYPE,  "", "ROOT", 100, new TLocation(1,ar.size(),0,0,file_name));
		analyzeNode(rootnode, ar , "" ,0);  // 2014.09.09
		if(convflag) {
			conv_gid(rootnode, "", 0); // 2014.04.02
		}
		return rootnode;
	}


	private void conv_gid(TObj rootnode, String base, int depth) {
		if(rootnode == null)
			return  ;
		TObj[] subobj = rootnode.getTObjList();
		Hashtable ht = new Hashtable();
		for ( int i = 0; i < subobj.length; i ++) {
			String key = subobj[i].getName();
			int no = 0;
			if(!ht.containsKey(key)) {
				Integer  val = new Integer(no);
				ht.put(key, val);
			} else {
				Integer  val = (Integer)ht.get(key);
				no = val.intValue() + 1;
				ht.put(key, new Integer(no));
			}
			String gid = base + "/" + key + "[" +no + "]";
			subobj[i].setGID(gid);
			conv_gid(subobj[i], gid, depth + 1);

		}
	}

	private int analyzeNode( TObj parrentNode, ArrayList ar , String prefix, int stpos){
		locallog.debug("#####  path", prefix + " , size = " + ar.size());
		String nodeName = "";
		String endNodeName = null;
		String startNodeName = null;
		int nodeNameCnt = 0;


		String line_data = "";
		ArrayList node_block = new ArrayList();
		 boolean sflag = false; //2014.09.09
		boolean blockend = false;

		int i = stpos;
		int line_seq = 0;
		int find_seq = 0;

		TObj nodedata = null;
		boolean cmtflag = false;
		boolean cdataflag = false;
		String cmtstr = "";

		while ( i < ar.size()) {
			node_line_data nddata = (node_line_data)ar.get(i);
			line_data = nddata.line_data.trim();
			line_seq = nddata.seq;

			if(cmtflag) {
				int cidx2 = line_data.indexOf(cmtstr);
				if (cidx2 >= 0) {
					line_data = line_data.substring(cidx2 + cmtstr.length());
					cmtflag = false;
				} else {
					line_data = "";
				}
			}
			if(cdataflag) { // 2014.09.16
				int cdataidx2 =  line_data.indexOf("]]>");
				if (cdataidx2 > -1) {
					String tmp = line_data.substring(0, cdataidx2);
					node_line_data nd = new node_line_data(tmp, line_seq);
					node_block.add(nd);

					int seq = 0;
					int end_seq = 0;
					StringBuffer sb = new StringBuffer();
					for(int j =0 ; j < node_block.size() ; j ++){
						node_line_data text = (node_line_data)node_block.get(j);
						if (j == 0)
							seq = text.seq;
						if(j == node_block.size()-1)
							end_seq = text.seq;
						sb.append(text.line_data).append("\n");
						// System.out.println("    TEXT ::: " + text.line_data);
					}
					node_block.clear();
					TObj newnodedata = new TObj(_TEXT_TYPE, make_path(prefix,"text"), sb.toString(), 100, new TLocation((seq+1), (end_seq+1),0,0,"") );
					parrentNode.add(newnodedata);

					line_data = line_data.substring(cdataidx2 + "]]>".length());
					node_line_data newnd = new node_line_data(line_data, line_seq);
					ar.set(i, newnd);
					cdataflag = false;
					continue ;
				}
				// 2014.01.03 cdata line add
				//if(line_data.trim().length() > 0) {
					node_line_data nd = new node_line_data(line_data, line_seq);
					node_block.add(nd);
				//}
				i++;
				continue ;
			}

			locallog.debug("line sflag=" + sflag,"["+line_seq+"]" + line_data);
		//}
			int leftidx = find_right(line_data,'<');
			if( leftidx > -1 ) { // 2014.09.16

				int cidx1 = line_data.indexOf("<!--",leftidx);
				if(cidx1 == leftidx) { // 2014.09.16
					cmtstr = "-->";
				} else {
					cidx1 = -1;
					int cidx11 = line_data.indexOf("<?xml",leftidx);
					if(cidx11 == leftidx) { // 2014.09.16
						cmtstr = "?>";
						cidx1= cidx11;
						get_encoding(line_data, parrentNode);
					} else {
						int cidx111 = line_data.indexOf("<!DOCTYPE",leftidx);
						if(cidx111 == leftidx) { // 2014.09.16
							cmtstr = ">";
							cidx1 = cidx111;
						} else {
							cmtstr ="";
						}
					}
				}
				if(cidx1 > -1) {
					int cidx2 = line_data.indexOf(cmtstr, cidx1);
					if (cidx2 > -1) {
						String tmp1 = line_data.substring(0, cidx1);
						locallog.trace("xxxxxxxxxxxx", tmp1);
						String tmp2 = line_data.substring(cidx2 + cmtstr.length());
						locallog.trace("xxxxxxxxxxxx", tmp2);
						line_data = tmp1 + " " + tmp2;
					} else {
						line_data = line_data.substring(0, cidx1);
						cmtflag = true;
					}

					node_line_data newnd = new node_line_data(line_data, line_seq);
					ar.set(i, newnd);
					continue ;
				}
				//int cdataidx1 = line_data.indexOf("<![CDATA[");
				//if(cdataidx1 > -1){
				int cdataidx1= line_data.indexOf("<![CDATA[",leftidx);
				if(cdataidx1 == leftidx) {  // 2014.09.16
					locallog.debug("xxxxxx", line_data);
					String tmp1 = line_data.substring(0, cdataidx1);
					if(tmp1.length() > 0) {
						node_line_data nd = new node_line_data(tmp1, line_seq);
						node_block.add(nd);
					}
					if(node_block.size() > 0) {
						make_string(node_block, parrentNode, prefix);
					}
					int cdataidx2 = line_data.indexOf("]]>", cdataidx1);
					if (cdataidx2 > -1) {
						String tmp2 = line_data.substring(cdataidx1 + "<![CDATA[".length(), cdataidx2);
						TObj newnodedata = new TObj(_TEXT_TYPE, make_path(prefix, "text"), tmp2, 100, new TLocation((line_seq+1), (line_seq+1) , 0,0,"") );
						parrentNode.add(newnodedata);
						line_data = line_data.substring(cdataidx2 + "]]>".length());
						node_line_data newnd = new node_line_data(line_data, line_seq);
						ar.set(i, newnd);
						continue ;
					} else {
						cdataflag = true;
						String tmp = line_data.substring(cdataidx1 + "<![CDATA[".length());
						//if(tmp.length() == 0) tmp = "x";
						//System.out.println("tmp"+tmp+", line="+line_seq);
						node_line_data nd = new node_line_data(tmp, line_seq);
						node_block.add(nd);
						i++;
						continue;
					}
				}
				String substr = line_data.substring(leftidx);
				String [] nodeNameList = getNodeName(substr);
				if(nodeNameList != null && substr.startsWith(nodeNameList[0])) { // 2014.09.16
					nodeName = nodeNameList[1];
					find_seq = line_seq;
					// System.out.println("============================" + nodeName + "============================");
					blockend = false;
					sflag = true;
					endNodeName = "</" + nodeName + ">";
					startNodeName = nodeName;
					nodeNameCnt = 1;
					locallog.debug("start node = " + startNodeName , "cnt = " + nodeNameCnt);
					// 2014.01.03
					int nodeidx = line_data.indexOf(nodeNameList[0]);
					String tmpstr = "";
					if(nodeidx > 0) {
						tmpstr = line_data.substring(0, nodeidx);
					}
					//if(tmpstr.trim().length() > 0) {
						node_line_data nd = new node_line_data(tmpstr, line_seq);
						node_block.add(nd);
					//}
					if(node_block.size() > 0) {
						make_string(node_block, parrentNode, prefix);
					}
					// 2014.09.23
					while ( i < ar.size()) {
						int rightidx = find_right(line_data,'>');
						if (rightidx > 0) {
							if (line_data.charAt(rightidx - 1) == '/') {     // case <a ....  />
								sflag = false;
							}
							locallog.trace("yyyyyyyyyy", line_data);
							String tmp = line_data.substring(0,rightidx+1);
							node_line_data nd2 = new node_line_data(tmp, line_seq);
							node_block.add(nd2);
							// make node data (attribute ����)
							nodedata = make_node_data(node_block, startNodeName, find_seq, prefix); // 2014.04.07
							parrentNode.add(nodedata);
							node_block.clear();
							line_data = line_data.substring(rightidx+1);
							node_line_data newnd = new node_line_data(line_data, line_seq);
							ar.set(i, newnd);
							if(sflag) {
								i = analyzeNode(nodedata,ar, make_path(prefix, nodedata.getName()), i);
							}
							sflag = false;
							break;
						} else {
							i ++;
							if (i >= ar.size() )  // 2014.09.26 index
								break;
							nddata = (node_line_data)ar.get(i);
							line_data = line_data + " " +  nddata.line_data.trim();
							locallog.debug("xxxxxxxx", line_data);
						}
					}
					continue ;
				} else {
					// 2015.01.16 if a <
					if (( leftidx +1) < line_data.length() && line_data.charAt(leftidx + 1) == '/') {     // </aaa>
						String tmp = line_data.substring(0,leftidx);
						node_line_data nd = new node_line_data(tmp, line_seq);
						node_block.add(nd);
						if(node_block.size() > 0) {
							make_string(node_block, parrentNode, prefix);
						}
						int idx2 = line_data.indexOf('>', leftidx);
						line_data = line_data.substring(idx2+1);
						node_line_data newnd = new node_line_data(line_data, line_seq);
						ar.set(i, newnd);
						return i;
					}
				}
			}
			// 2014.01.03 node_block
			if(line_data.trim().length() > 0 || node_block.size() > 0) {

				node_line_data nd = new node_line_data(line_data, line_seq);
				node_block.add(nd);
			}
			i++;
		}
		if(node_block.size() > 0){
			make_string(node_block, parrentNode, prefix);
		}
		return i;
	}

	private int find_right(String line_data , char findchr) {
		boolean flag = false;
		int findpos = -1;

		int cnt = 0;
		char qchr = '\"';
		boolean firstflag = true;
		for ( int i = 0; i < line_data.length(); i++) {
			char chr = line_data.charAt(i);
			if (chr == '\"' || chr == '\'') {
				if(firstflag) {
					qchr = chr;
					firstflag = false;
				}
			}
			if (firstflag == false && chr == qchr) {
				cnt ++;
				if (flag) {
					if(brokenflag && line_data.charAt(i-1) == '=') { // 2014.06.27
						cnt++;
					} else {
						flag = false;
					}
				} else {
					flag = true;
				}
			}
			if(chr == findchr)
				findpos = i;
			if(flag == false && chr == findchr) {  //
				return i;
			}
		}
		if(cnt == 1 && findpos != -1 )  //
			return findpos;
		return -1;
	}
	private String make_path(String prefix , String str) {
		//if (prefix.length() > 0) {
			return prefix + "/" + str;
		//} else {
		//	return str;
		//}
	}
	// <?xml version="1.0" encoding="UTF-8" ?>

	// 2014.01.10 encodeing
	private void get_encoding(String  str , TObj parrentNode) {
		if (parrentNode.getType() != _ROOT_TYPE)
			return ;
		String p_name = "encoding[\\s]*=[\\s]*(\"|')";
		Pattern p = Pattern.compile(p_name);
		Matcher m = p.matcher(str);
		if(m.find()){
			String qstr =  m.group(1);
			if(qstr.equals("\""))
				qstr = "\\" + qstr;
			locallog.trace("get_encoding :: " , m.group(1));
			String pstr =  "encoding[\\s]*=[\\s]*"+qstr + "([^"+qstr+"]+)";
			Pattern p1 = Pattern.compile(pstr);
			locallog.trace("NODE PSTR :: " ,pstr);
			Matcher m1 = p1.matcher(str);
			if(m1.find()) {
				locallog.trace("NODE PSTR VALUE :: " ,m1.group(1));
				parrentNode.setName( m1.group(1) );
			}
		}
	}
	/* method name : make_string
	 * paramether  : node_block
	 *
	 * */
	private void make_string(ArrayList node_block , TObj parrentNode, String prefix) {
		int seq = 0;
		int end_seq = 0;
		int prev_seq = 0;
		StringBuffer sb = new StringBuffer();
		for(int j =0 ; j < node_block.size() ; j ++) {
			node_line_data text = (node_line_data)node_block.get(j);
			if (j == 0) {
				seq = text.seq;
				prev_seq = text.seq; // 2014.09.16
			}
			if(j == node_block.size()-1)
				end_seq = text.seq;
			//sb.append(text.line_data.trim()).append("\n");
			// 2014.5.21

			if(prev_seq != text.seq) { // 2014.09.16
				prev_seq = text.seq;
				sb.append("\n");
			}
			sb.append(text.line_data.trim());

/*			if(j < node_block.size() -1 ) {
				sb.append("\n");
			}*/




			locallog.trace("    TEXT ::: " , text.line_data);
		}
		// 2014.01.08
		if(sb.toString().trim().length() > 0) {
			TObj newnodedata = new TObj(_TEXT_TYPE, make_path(prefix, "text"), sb.toString(), 100, new TLocation(seq+1, end_seq+1, 0,0, "") );
			parrentNode.add(newnodedata);
		}
		node_block.clear();
	}
	/* method name : getNodeName
	 * paramether  : nodeData = <nodename .... > line_data
	 * �� �� : nodename �� ���� �� ��ȯ
	 * return : nodename, null
	 * */
	private String[] getNodeName(String nodeData){
		//String p_name = "<[\\s]*([a-zA-Z0-9_\\-:]+)[\\s]*";  // 2014.10.09
		String p_name = "<([a-zA-Z0-9_\\-:]+)[\\s]*";
		Pattern p = Pattern.compile(p_name);
		Matcher m = p.matcher(nodeData);
		if(m.find()) {
			locallog.debug("getNodeName :: " , m.group(1));
			String ret[] = {m.group(0), m.group(1) };
			return ret;
		}
		return null;
	}
	/* method name : make_node_data
	 * paramether  : arlist
	 *
	 * return : nddata
	 * */
	private TObj make_node_data(ArrayList arlist, String nodename, int seq, String prefix)
	{
		// String p_value = "([a-zA-Z0-9_:]+)[\\s]*=[\\s]*\"?([a-zA-Z0-9_\\-:\\\\.��-��]+)";
		// 2013.12.17
		String p_value1 = "([a-zA-Z0-9-_:]+)[\\s]*=[\\s]*[\"]([^\"]+)";
		String p_value2 = "([a-zA-Z0-9-_:]+)[\\s]*=[\\s]*[']([^']+)";
		String p_value3 = "([a-zA-Z0-9-_:]+)[\\s]*=[\\s]*([a-zA-Z0-9-_:\\.]+)";
		Pattern  p1 = Pattern.compile(p_value1);
		Pattern  p2 = Pattern.compile(p_value2);
		Pattern  p3 = Pattern.compile(p_value3);
		TObj nddata = new TObj(_NODE_TYPE, make_path(prefix , nodename), nodename, 100, new TLocation( seq+1, seq+1,0,0,"") );
		for ( int i = 0; i < arlist.size(); i ++) {
			node_line_data nd = (node_line_data )arlist.get(i);
			String nodeData = nd.line_data;
			Matcher m1 = p1.matcher(nodeData);
			boolean findflag =false;
			locallog.trace("--", nodeData);
			while(m1.find()){
				locallog.debug("KEY :: " ,  m1.group(1) + " VALUE :: " + m1.group(2));
				if(brokenflag && m1.group(2).endsWith("=")) {
					return make_node_data_fix(arlist, nodename, seq, prefix);
				}
				String tmp = m1.group(2).trim();
				if (tmp.endsWith("\""))
					tmp = tmp.substring(0, tmp.length() - 1);

				TDpd nodekey = new TDpd(1, tmp, m1.group(1), 100, new TLocation(nd.seq+1));
				nddata.add(nodekey);
				findflag = true;
			}
			// 2014.04.11 "", ''
//			 2014.01.10 aaa='ssss
			Matcher m2 = p2.matcher(nodeData);
			while(m2.find()){
				locallog.debug("KEY :: " ,  m2.group(1) + " VALUE :: " + m2.group(2));
				if(brokenflag && m2.group(2).endsWith("=")) {
					return make_node_data_fix(arlist, nodename, seq, prefix);
				}
				String tmp = m2.group(2).trim();
				if (tmp.endsWith("\""))
					tmp = tmp.substring(0, tmp.length() - 1);

				TDpd nodekey = new TDpd(1, tmp, m2.group(1), 100, new TLocation(nd.seq+1));
				nddata.add(nodekey);
				findflag = true;
			}
			if(findflag == false) {
				// 2014.01.23 aaa= ssss
				Matcher m3 = p3.matcher(nodeData);

				while(m3.find()){
					locallog.debug("KEY :: " ,  m3.group(1) + " VALUE :: " + m3.group(2));
					if(brokenflag && m2.group(2).endsWith("=")) {
						return make_node_data_fix(arlist, nodename, seq, prefix);
					}
					String tmp = m3.group(2).trim();
					if (tmp.endsWith("\""))
						tmp = tmp.substring(0, tmp.length() - 1);

					TDpd nodekey = new TDpd(1, tmp, m3.group(1), 100, new TLocation(nd.seq+1));
					nddata.add(nodekey);
					findflag = true;
				}

			}
		}
		return nddata;
	}
	private TObj make_node_data_fix(ArrayList arlist, String nodename, int seq, String prefix)
	{

		String p_value1 = "([a-zA-Z0-9-_:]+)[\\s]*=[\\s]*([\"'][^\"'\\s/>]+)";
		Pattern  p1 = Pattern.compile(p_value1);

		TObj nddata = new TObj(_NODE_TYPE, make_path(prefix , nodename), nodename, 100, new TLocation( seq+1, seq+1,0,0,"") );
		for ( int i = 0; i < arlist.size(); i ++) {
			node_line_data nd = (node_line_data )arlist.get(i);
			String nodeData = nd.line_data;
			Matcher m1 = p1.matcher(nodeData);
			boolean findflag =false;
			locallog.trace("--", nodeData);
			while(m1.find()){
				locallog.debug("KEY :: " ,  m1.group(1) + " VALUE :: " + m1.group(2));
				String tmp = m1.group(2).trim();
				if (tmp.startsWith("\"") || tmp.startsWith("'"))
					tmp = tmp.substring(1, tmp.length());
				if (tmp.endsWith("\"") || tmp.endsWith("'"))
					tmp = tmp.substring(0, tmp.length() - 1);

				TDpd nodekey = new TDpd(1, tmp, m1.group(1), 100, new TLocation(nd.seq+1));
				nddata.add(nodekey);
				findflag = true;
			}
		}
		return nddata;
	}
	/* method name : loadfile
	 * paramether  : file_path =
	 *
	 * return : filedata
	 * */
	private ArrayList loadfile(String file_path) throws Exception{
		FileInputStream fis = null;
		BufferedReader br = null;
		ArrayList filedata = new ArrayList();
		try {
			File file = new File(file_path);
			fis = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fis, charset));
			int i = 0;
			while (br.ready()) {
				String line_data = br.readLine();
				if (line_data == null) {
					continue;
				}
				node_line_data nd = new node_line_data(line_data, i++);
				filedata.add(nd);
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
//	 printStruct
	public void dump(){
		int depth = 0;
		printStruct(rootnode, depth, null);
	}
	// 201.01.10
	public void dump(String file_name){
		int depth = 0;
		File file = new File(file_name);
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(file.getPath(), false)), true);
			printStruct(rootnode, depth, out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(out != null)
				out.close();
		}

	}
	// xml���� ���
	public void printStruct(TObj node, int depth, PrintWriter out){
		String depthString = "";
		for(int k = 0; k < depth ; k ++){
			depthString = depthString + "\t";
		}
		if(out != null) {
			out.println(depthString + "|" + "===============================================================");
			out.println(depthString + "|" + " DEPTH        :: " + depth);
			out.println(depthString + "|" + " TYPE        :: " + node.getType());
			out.println(depthString + "|" + " NODE GID   :: " + node.getGID());
			out.println(depthString + "|" + " NODE NAME  (" + ":" + node.getTLocation().getStartLine() + ") :: " + node.getName() );
			out.println(depthString + "|" + " SUBLISTSIZE :: " + node.getTObjList().length);

			TDpd dpd[] = node.getTDpdList();
			for(int i = 0; i <dpd.length ; i ++){
				TDpd temp = dpd[i];
				out.println(depthString + "|" + " ATTRIBUTE[" + i + "] NAME (" + temp.getTLocation().getStartLine() +") :: " + temp.getName() + " VALUE :: " + temp.getGID()  );
			}
			out.println(depthString + "|" + "===============================================================");
		}
		System.out.println(depthString + "|" + "===============================================================");
		System.out.println(depthString + "|" + " DEPTH        :: " + depth);
		System.out.println(depthString + "|" + " TYPE        :: " + node.getType());
		System.out.println(depthString + "|" + " NODE GID   :: " + node.getGID());
		System.out.println(depthString + "|" + " NODE NAME  (" + ":" + node.getTLocation().getStartLine() + ") :: " + node.getName() );
		System.out.println(depthString + "|" + " SUBLISTSIZE :: " + node.getTObjList().length);

		TDpd dpd[] = node.getTDpdList();
		for(int i = 0; i <dpd.length ; i ++){
			TDpd temp = dpd[i];
			System.out.println(depthString + "|" + " ATTRIBUTE[" + i + "] NAME (" + temp.getTLocation().getStartLine() +") :: " + temp.getName() + " VALUE :: " + temp.getGID()  );
		}
		System.out.println(depthString + "|" + "===============================================================");
		depth++;
		TObj objlist[] = node.getTObjList();
		for(int j = 0 ; j < objlist.length; j ++){
			printStruct(objlist[j], depth, out);
		}
		depth--;
	}


	public String getAttributeValue(String nodePath, String attrName){
		if (!nodePath.startsWith("/"))
			nodePath = "/" + nodePath;
		String node_list[] = nodePath.split("/");
		return find_node(node_list, 1, rootnode.getTObjList(), attrName);
	}

	public String getAttributeValue(TObj node, String attrName){
		if (node != null) {
			TDpd dpdlist[] = node.getTDpdList();
			for ( int j = 0 ; j < dpdlist.length; j ++) {
				TDpd knd = dpdlist[j];
				if (knd.getName().equals(attrName))
					return knd.getGID();
			}
		}
		return null;
	}

	public TObj getNode(String nodePath){
		if (!nodePath.startsWith("/"))
			nodePath = "/" + nodePath;
		String node_list[] = nodePath.split("/");
		return find_obj_node(node_list, 1, rootnode.getTObjList());
	}


	public String getNodeValue(String nodePath){
		if (!nodePath.startsWith("/"))
			nodePath = "/" + nodePath;
		String node_list[] = nodePath.split("/");
/*		for ( int i = 0; i < node_list.length; i ++) {
			System.out.println("noad[" +i +"]" + node_list[i]);
		}*/
		return conv_cdata(find_node_text(node_list, 1, rootnode.getTObjList()));  // 2014.04.15
	}
	// 2014.06.26
	public String getNodeValueNoTrim(String nodePath){
		if (!nodePath.startsWith("/"))
			nodePath = "/" + nodePath;
		String node_list[] = nodePath.split("/");
/*		for ( int i = 0; i < node_list.length; i ++) {
			System.out.println("noad[" +i +"]" + node_list[i]);
		}*/
		return conv_cdata_notrim(find_node_text(node_list, 1, rootnode.getTObjList()));  // 2014.04.15
	}
	// 2014.04.15
	private String conv_cdata(String str) {
		if(str == null)
			return str;
		if(str.indexOf("<![CDATA[") != -1) {
			str = str.replaceAll("<!\\[CDATA\\[", "");
			str = str.replaceAll("\\]\\]>", "");
		}
		return str.trim(); // 2014.5.21
	}
	private String conv_cdata_notrim(String str) {
		if(str == null)
			return str;
		if(str.indexOf("<![CDATA[") != -1) {
			str = str.replaceAll("<!\\[CDATA\\[", "");
			str = str.replaceAll("\\]\\]>", "");
		}
		return str; // 2014.6.26
	}
	// 2014.01.07
	public int getNodeValueLine(String nodePath){
		if (!nodePath.startsWith("/"))
			nodePath = "/" + nodePath;
		String node_list[] = nodePath.split("/");
/*		for ( int i = 0; i < node_list.length; i ++) {
			System.out.println("noad[" +i +"]" + node_list[i]);
//		}*/
		return find_node_text_line(node_list, 1, rootnode.getTObjList());
	}

	// nodePath
	public String getNodeValue(TObj nd){
		if (nd != null) {
			TObj objlist[] = nd.getTObjList();
			for ( int j = 0 ; j < objlist.length; j ++) {
				TObj knd = objlist[j];
				if (knd.getType() == _TEXT_TYPE){
					//System.out.println(knd.tlocation.getStartLine() + " &&&&&&&& " + knd.tlocation.getEndLine());
					return conv_cdata(knd.getName());  // 2014.06.26 conv_cdata
				}
			}
		}
		return null;
	}
	// 	2014.06.26
	public String getNodeValueNoTrim(TObj nd){
	if (nd != null) {
		TObj objlist[] = nd.getTObjList();
		for ( int j = 0 ; j < objlist.length; j ++) {
			TObj knd = objlist[j];
			if (knd.getType() == _TEXT_TYPE){
				//System.out.println(knd.tlocation.getStartLine() + " &&&&&&&& " + knd.tlocation.getEndLine());
				return conv_cdata_notrim(knd.getName());
			}
		}
	}
	return null;
}
	// nodePath
	public int getNodeValueLine(TObj nd){
		if (nd != null) {
			TObj objlist[] = nd.getTObjList();
			for ( int j = 0 ; j < objlist.length; j ++) {
				TObj knd = objlist[j];
				if (knd.getType() == _TEXT_TYPE){
					//System.out.println(knd.tlocation.getStartLine() + " &&&&&&&& " + knd.tlocation.getEndLine());
					return knd.getTLocation().getStartLine();
				}
			}
		}
		return -1;
	}

	public TObj getRootNode() {
		// 2014.01.23 getRootNode()
/*		if(rootnode.getTObjList().length > 0){
			TObj[] objlist = rootnode.getTObjList();
			return objlist[0];
		}
*/
		if(rootnode.getTObjList().length > 0){
			TObj[] objlist = rootnode.getTObjList();
			// 2013.01.10
			for ( int i = 0; i < objlist.length; i ++) {
				if(objlist[i].getType() ==_NODE_TYPE )
					return objlist[i];
			}
		}
		return null;
	}
	public int getEndLine() {
		return rootnode.getTLocation().getEndLine();
	}

	public String getRootName(){
		if(rootnode.getTObjList().length > 0){
			TObj[] objlist = rootnode.getTObjList();
			// 2013.01.10 r
			for ( int i = 0; i < objlist.length; i ++) {
				if(objlist[i].getType() ==_NODE_TYPE )
					return objlist[i].getName();
			}
		}
		return null;
	}
	// findNodeList
	public ArrayList getNodeList(String targetNode){
		ArrayList node_list = new ArrayList();
		return findNodeList( rootnode, targetNode, node_list);
	}

	private ArrayList findNodeList(TObj node, String targetNode, ArrayList node_list){
		String node_name = "";
		TObj objlist[] = node.getTObjList();
		for(int i = 0 ; i < objlist.length; i++){
			TObj nd = objlist[i];
			node_name = nd.getName();
			if(targetNode.equals(node_name)){
				// System.out.println("FIND NODE target : " + targetNode + " node name : " + node_name );
				node_list.add(nd);
			}
			if(nd.getTObjList().length> 0){
				findNodeList(nd, targetNode, node_list);
			}
		}
		return node_list;
	}
	//
	private TObj find_obj_node(String node_list[] , int depth, TObj[]  slist) {
		TObj nd = find_node_path(node_list , depth, slist);
		if (nd != null) {
			return nd;
		}
		return null;
	}
	//
	private String find_node(String node_list[] , int depth, TObj  slist[], String attrName) {
		TObj nd = find_node_path(node_list , depth, slist);
		if (nd != null) {
			TDpd dpdlist[] = nd.getTDpdList();
			for ( int j = 0 ; j < dpdlist.length; j ++) {
				TDpd knd = dpdlist[j];
				if (knd.getName().equals(attrName))
					return knd.getGID();
			}
		}
		return null;
	}
	//
	private String find_node_text(String node_list[] , int depth, TObj  slist[]) {
		TObj nd = find_node_path(node_list , depth, slist);
		String str = null; // 2014.04.21 text n
		if (nd != null) {
			TObj objlist[] = nd.getTObjList();
			for ( int j = 0 ; j < objlist.length; j ++) {
				TObj knd = objlist[j];
				// System.out.println("type=" + knd.getType() + ",name=" + knd.getName());
				if (knd.getType() == _TEXT_TYPE){
					//System.out.println(knd.tlocation.getStartLine() + " &&&&&&&& " + knd.tlocation.getEndLine());
					//return knd.getName().trim();
					if(str == null)
						str = knd.getName();
					else
						str =  str + knd.getName();
				}
			}
		}
		return str;	//
	}
	//
	//
	private int find_node_text_line(String node_list[] , int depth, TObj  slist[]) {
		TObj nd = find_node_path(node_list , depth, slist);
		if (nd != null) {
			TObj objlist[] = nd.getTObjList();
			for ( int j = 0 ; j < objlist.length; j ++) {
				TObj knd = objlist[j];
				// System.out.println("type=" + knd.getType() + ",name=" + knd.getName());
				if (knd.getType() == _TEXT_TYPE){
					//System.out.println(knd.tlocation.getStartLine() + " &&&&&&&& " + knd.tlocation.getEndLine());
					return knd.getTLocation().getStartLine();
				}
			}
		}
		return -1;
	}

	private TObj find_node_path(String node_list[] , int depth, TObj  slist[]) {
/*		for ( int i = 0; i < node_list.length; i++) {
			System.out.println("xxxxx[" + i + "]" + node_list[i] );
		}
		System.out.println("yyyy" + depth );*/
		String str = node_list[depth];
		//System.out.println("str = " + str);
		int idx1 = str.indexOf("[");
		int pos= 0;
		int fcnt = 0;
		if (idx1 > 0) { // xxxx[1]
			int idx2 = str.indexOf("]", idx1);
			String tmp = str.substring(idx1+1, idx2);
			pos = Integer.parseInt(tmp);
			str = str.substring(0, idx1);
		}
		for ( int i = 0; i < slist.length; i ++) {
			TObj  nd = slist[i];
			//System.out.println("nd.getName() +" + nd.getName() + " , str = " + str);
			if (nd.getName().equals(str)) {
				if (fcnt == pos) {
					if ( (depth+1) == node_list.length) { //
						return nd;
					} else {
						return find_node_path(node_list, depth + 1, nd.getTObjList());
					}
				}
				fcnt ++;
			}
		}
		return null;
	}

	class node_line_data {
		String line_data;
		int seq;
		node_line_data(String data, int seq) {
			line_data = data;
			this.seq = seq;
		}
	}
	class local_debug {
		void debug(String a, String b) {
		//	 System.out.println(a + " -" + b);
		}
		void trace(String a, String b) {
		//	 System.out.println(a + " -" + b);
		}
	}
}
