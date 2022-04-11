package com.dw.org;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;
import com.dareway.framework.dbengine.DE;

public class OrgONPBPO extends BPO{
	
	public DataObject getOrgInfo(DataObject para) throws Exception{
		String orgno = para.getString("orgno");
		if (orgno == null || orgno.trim().isEmpty()) {
			bizException("机构编号为空！");
		}
		
		String orgname = "", sfwrsxtyj = "", sfyxczxjjg = "", yxjtjxx = "", jglx = "";
		int orgcount = 0;
		DataObject vdo = DataObject.getInstance();
		vdo.put("orgno", orgno);//机构编号
		
		//0.查询机构基本信息
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql(" select a.orgno, a.orgname, a.orgtype, b.typename ");
		de.addSql(" from odssu.orginfor a, odssu.org_type b ");
		de.addSql(" where a.orgtype = b.typeno ");
		de.addSql(" and a.orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore vds = de.query();
		if (vds != null && vds.rowCount() > 0) {
			orgname = vds.getString(0, "orgname");
			jglx = vds.getString(0, "typename");
			String orgtype = vds.getString(0, "orgtype");
			//1.判断是否为人设系统
			if(orgtype.equals("HSDOMAIN_DSRSXT") || orgtype.equals("HSDOMAIN_SZRSXT")){
				sfwrsxtyj = "1";//是人社系统
				
				//2.1是，查询下属机构数量
				de.clearSql();
				de.addSql(" select t.orgno, t.orgname from odssu.orginfor t where t.belongorgno= :orgno ");
				de.setString("orgno", orgno);
				DataStore xsjgorgds = de.query();
				orgcount = xsjgorgds.rowCount();
				//2.1.1查询各下属机构下的科室、人社所、经办机构的数量
				DataStore rsxxorgds = DataStore.getInstance();
				for(int i = 0; i < orgcount; i++) {
					String xsjgorgno = xsjgorgds.getString(i, "orgno");
					String xsjgorgname = xsjgorgds.getString(i, "orgname");
					rsxxorgds.put(i, "xsjgorgno", xsjgorgno);
					rsxxorgds.put(i, "xsjgorgname", xsjgorgname);

					de.clearSql();
					de.addSql(" select t.orgno from odssu.orginfor t ");
					de.addSql(" where t.sleepflag = '0' ");
					de.addSql(" and t.orgtype in ('HS_DS_YWJG','HS_QX_YWJG','HS_ST_YWJG') ");
					de.addSql(" and t.belongorgno=:xsjgorgno ");
					de.setString("xsjgorgno", xsjgorgno);
					DataStore jbjgds = de.query();/*经办机构*/
					int jbjgsl = jbjgds.rowCount();

					de.clearSql();
					de.addSql(" select t.orgno from odssu.orginfor t ");
					de.addSql(" where t.sleepflag = '0' ");
					de.addSql(" and t.orgtype = 'HSDOMAIN_RSCKS' ");
					de.addSql(" and t.belongorgno=:xsjgorgno ");
					de.setString("xsjgorgno", xsjgorgno);
					DataStore ksds = de.query();/*科室*/
					int kssl = ksds.rowCount();

					de.clearSql();
					de.addSql(" select t.orgno from odssu.orginfor t ");
					de.addSql(" where t.sleepflag = '0' ");
					de.addSql(" and t.orgtype = 'HSDOMAIN_SBS' ");
					de.addSql(" and t.belongorgno=:xsjgorgno ");
					de.setString("xsjgorgno", xsjgorgno);
					DataStore rssds = de.query();/*人社所*/
					int rsssl = rssds.rowCount();

					de.clearSql();
					de.addSql(" select t.orgno from odssu.orginfor t ");
					de.addSql(" where t.sleepflag = '0' ");
					de.addSql(" and t.orgtype = 'HSDOMAIN_SBZ' ");
					de.addSql(" and t.belongorgno=:xsjgorgno ");
					de.setString("xsjgorgno", xsjgorgno);
					DataStore rszds = de.query();/*人社站*/
					int rszsl = rszds.rowCount();

					de.clearSql();
					de.addSql(" select distinct t.orgno ");
					de.addSql(" from odssu.orginfor t, odssu.org_type a ");
					de.addSql(" where t.sleepflag = '0' and t.ORGTYPE = a.typeno ");
					de.addSql(" and a.typeno like '%_EJDW' and t.belongorgno=:xsjgorgno ");
					de.setString("xsjgorgno", xsjgorgno);
					DataStore ejdwds = de.query();/*二级单位*/
					int ejdwsl = ejdwds.rowCount();

					rsxxorgds.put(i, "jbjgsl", jbjgsl);
					rsxxorgds.put(i, "kssl", kssl);
					rsxxorgds.put(i, "rsssl", rsssl);
					rsxxorgds.put(i, "rszsl", rszsl);
					rsxxorgds.put(i, "ejdwsl", ejdwsl);
				}
				vdo.put("rsxxorgds", rsxxorgds);
			}else{
				sfwrsxtyj = "0";//否
				//2.2否，查询是否允许新增下级机构sfyxczxjjg
				de.clearSql();
				de.addSql(" select a.orgno, c.typename from odssu.orginfor a , odssu.ir_org_type b , odssu.org_type c ");
				de.addSql(" where a.orgno= :orgno ");
				de.addSql(" and a.orgtype=b.suptypeno ");
				de.addSql(" and b.subtypeno=c.typeno ");
				de.addSql(" and c.yxzjjgbz='1' ");
				de.setString("orgno", orgno);
				DataStore xjjgds = de.query();
				if(xjjgds.rowCount() > 0) {
					sfyxczxjjg = "1";
					//2.2.1是，查询机构下属的机构类型和各机构类型、下级机构信息yxjtjxx

					de.clearSql();
					de.addSql(" SELECT count(1) sl, t.typename FROM ");
					de.addSql(" ( select a.orgno, a.orgname , b.typeno, b.typename , b.sn ");
					de.addSql("   from odssu.orginfor a , odssu.org_type b ");
					de.addSql("   where a.belongorgno= :orgno ");
					de.addSql("   and b.typeno=a.orgtype ");
					de.addSql("   order by b.sn ) t ");
					de.addSql(" GROUP BY t.typeno, t.typename ");
					de.setString("orgno", orgno);
					DataStore yxjtjorgds = de.query();//统计信息
					int yxjtjcount = yxjtjorgds.rowCount();
					if(yxjtjcount == 0) {
						yxjtjxx = "无下级机构";
					}else {
						yxjtjxx = "下共有";
					}
					for(int j = 0; j <yxjtjcount; j++) {
						yxjtjxx += yxjtjorgds.getInt(j, "sl") + "个" + yxjtjorgds.getString(j, "typename");
						if(j < yxjtjcount - 1) {
							yxjtjxx += "，";
						}
					}

					de.clearSql();
					de.addSql(" select a.orgno, a.orgname , b.typeno, b.typename , b.sn ");
					de.addSql(" from odssu.orginfor a , odssu.org_type b ");
					de.addSql(" where a.belongorgno= :orgno ");
					de.addSql(" and b.typeno=a.orgtype ");
					de.addSql(" order by b.sn ");
					de.setString("orgno", orgno);
					DataStore yxjorgds = de.query();
					vdo.put("yxjorgds", yxjorgds);
				}else {
					sfyxczxjjg = "0";
					//2.2.2否，直接返回
				}
			}
		}else {
			bizException("根据机构编号["+orgno+"]未查询到机构信息！");
		}
		
		vdo.put("orgname", orgname);//机构名称
		vdo.put("jglx", jglx);//机构类型
		vdo.put("sfwrsxtyj", sfwrsxtyj);//是否为人社系统
		vdo.put("orgcount", orgcount + "");//下属机构数量
		vdo.put("sfyxczxjjg", sfyxczxjjg);//是否允许新增下级机构sfyxczxjjg
		vdo.put("yxjtjxx", yxjtjxx);//下级机构统计信息
		return vdo;
	}
	
	public DataObject getOrgList(DataObject para) throws AppException {
		String belongorgno = para.getString("supOrgno");
		String typeno = para.getString("orgtype");// 机构类型编号

		DE de = DE.getInstance();
		DataObject vdo = DataObject.getInstance();
		DataStore vds;

		de.clearSql();
  		de.addSql("select a.orgno,a.displayname,a.orgname,c.typename,c.typenature,    ");
  		de.addSql("       b.orgname belongorgname                                     ");
  		de.addSql("  from odssu.orginfor a,                                           ");
  		de.addSql("       odssu.orginfor b,                                           ");
  		de.addSql("       odssu.org_type c                                            ");
  		de.addSql(" where a.belongorgno= b.orgno                                      ");
  		de.addSql("   and a.orgtype = c.typeno                                        ");
  		de.addSql("   and a.belongorgno = :belongorgno                                           ");
  		de.addSql("   and a.sleepflag = '0' ");
  		de.addSql("   and a.orgtype = :typeno                                               ");
  		de.addSql(" order by a.orgsn,a.orgno ");
		de.setString("belongorgno", belongorgno);
		de.setString("typeno", typeno);
		vds = de.query();
		vdo.put("vds", vds);
		return vdo;
	}
	public DataObject getRSZList(DataObject para) throws AppException {
		String belongorgno = para.getString("orgno");
		DE de = DE.getInstance();
		DataObject vdo = DataObject.getInstance();
		DataStore vds;
		
		de.clearSql();
  		de.addSql("select a.orgno rszno,a.displayname rszdisplayname,a.orgname rszname  ");
  		de.addSql("  from odssu.orginfor a      ");
  		de.addSql(" where a.belongorgno = :belongorgno      ");
  		de.addSql("   and a.orgtype = 'HSDOMAIN_SBZ'");
  		de.addSql("   and a.sleepflag = '0' ");
  		de.addSql(" order by a.orgsn,a.orgno ");
		de.setString("belongorgno", belongorgno);
		vds = de.query();
		vdo.put("rszvds", vds);
		return vdo;
	}

	
	/**
	 * 描述：socket同步
	 * author: sjn
	 * date: 2017年8月8日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws IOException 
	 * @throws Exception 
	 */
	public DataObject synchSocket(DataObject para) throws AppException, Exception {
		DataObject messagedo = DataObject.getInstance();
		// 生成报文头
		String header = "",body = "";
		// 接口类型
		String jylx = "6010";
		// 业务系统编码，目前使用HSU的
		String ywxtbm = "330599010";
		// 获取验证码

		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" select a.csz ");
  		de.addSql("   from be3u.sys_para a ");
  		de.addSql("  where a.csbh = :csbh ");
  		de.addSql("    and a.jbjgid = :jbjgid ");
		de.setString("csbh", "si3u1110");
		de.setString("jbjgid", "33052201");
		DataStore yzmds = de.query();
		String si3u1110 = yzmds.getString(0, "csz");
		String[] yzmStr = si3u1110.split(",");
		String yzm = "";
		for (int i = 0, n = yzmStr.length; i < n; i++) {
			if (yzmStr[i].indexOf(jylx) >= 0) {
				yzm = yzmStr[i].substring(yzmStr[i].indexOf(":") + 1);
			}
		}

		// 操作用户，目前写死admin
		String userid = "admin";
		// 行政区划
		String xzqh = "330522";
		// 附件标志
		String fjbz = "0";
		
		de.clearSql();
  		de.addSql("select a.orgno,a.orgname from odssu.orginfor a where a.orgtype = :orgtype ");
		de.setString("orgtype","HSDOMAIN_SBZ");
		DataStore orgds = de.query();
		if (orgds == null || orgds.rowCount() <= 0) {
			messagedo.put("msg", "没有找到社区信息。");
			return messagedo;
		}
		
		// 5、解析ip地址，目前写死长兴HSU全民参保的地址
		de.clearSql();
  		de.addSql("select csz ");
  		de.addSql("  from be3u.system_para ");
  		de.addSql(" where csbh = :csbh ");
  		de.addSql("   and dbid = :dbid ");
  		de.addSql("   and appid = :appid ");
		de.setString("csbh", "QMCBADDRESS");
		de.setString("dbid", "227");
		de.setString("appid", "HSU");
		DataStore ipvds = de.query();
		String csz = ipvds.getString(0, "csz");
		String[] address = csz.split(":");
		String ip = address[0];
		int port = Integer.parseInt(address[1]);
		
		// 6、获取socket进行传输
		Socket socket = new Socket(ip, port);
		// 连接超时处理
		socket.setSoTimeout(3600000);// 设置读操作超时时间六分钟 s
		//获取服务端的输出流，为了向服务端输出数据  
        OutputStream out = socket.getOutputStream();
        //获取服务端的输入流，为了获取服务端输入的数据  
        InputStream in=socket.getInputStream();
        PrintWriter bufw = new PrintWriter(out,true);  
        BufferedReader bufr = new BufferedReader(new InputStreamReader(in));  
        
        String line=null;  
		DataStore logds = DataStore.getInstance();
		// 循环调用socket传输社区数据
		for (int i = 0; i < orgds.rowCount(); i++) {
			
			String orgno = orgds.getString(i, "orgno");
			String orgname = orgds.getString(i, "orgname");
			// 获取9位随机数sequence
//			String id = DBUtil.getSequence("BE3U.SQ_DJID").substring(2);// 原来11位截为9
			String id = de.getNextVal("sq_djid").substring(2);// 原来11位截为9
			// 业务系统编号（9）+ 日期（8）+序号（9）
			String jylsh = ywxtbm+ DateUtil.dateToString(DateUtil.getDBTime(), "yyyyMMdd") + id;
			// 经办时间
			String jbsj = DateUtil.FormatDate(DateUtil.getDBTime(), "yyyy-MM-dd hh24:mm:ss");

			// 1、生成文件头
			// #### 0010 交易的编号 业务系统编码 验证码 交易流水号 经办人 经办时间 附件标志
			header = "####" + "0010" + "|" + jylx + "|" + ywxtbm + "|"
					+ yzm + "|" + jylsh + "|" + userid + "|" + jbsj
					+ "|" + jbsj + "|" + fjbz;
			
			// 2、生成报文体，目前是写死的（行政区划~业务系统编号+单位编号~单位编号~单位名称~参保类型~~参保状态~~~~~~~~~~~~~~~~~~~~~~~~~~~~~）
			body = xzqh + "~" + ywxtbm + orgno + "~" + orgno + "~" + orgname + "~" + "99" + "~~" + "1" + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
			
			// 3、生成报文尾
			String end = "$$$$";
			
			// 4、组装报文
			String bw = header + "~" + body + end;
			
			logds.put(i, "logorgname", "同步社区："+orgname);
			logds.put(i, "requestbw", "请求报文："+bw);
			try {
				line=bufr.readLine();//读取服务端传来的数据  
				logds.put(i, "log", "返回报文："+line);
				bufw.println(bw);
				bufw.flush();
			} catch (Exception e) {
				logds.put(i, "log", "异常信息：SOCKET连接失败："+e.getMessage());
			} 
		}
		//关闭socket和流
		in.close();
		out.close();
		socket.close();

		messagedo.put("msg", "同步完成");
		String start = "--------------------------";
		String logstr = "";
		for (int j = 0; j < logds.rowCount(); j++) {
			String logorgname = logds.getString(j, "logorgname");
			String requestbw = logds.getString(j, "requestbw");
			String log = logds.getString(j, "log");
			logstr = logstr + start + "\r\n" + logorgname + "\r\n" + requestbw + "\r\n" + log + "\r\n";
		}
		messagedo.put("logstr", logstr);
		return messagedo;
	}

}
