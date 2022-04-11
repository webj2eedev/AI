package com.dw.odssu.ws.org.jgjbxxxzwzb;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dareway.apps.odssu.OdssuNames;
import com.dareway.apps.process.ProcessBPO;
import com.dareway.apps.process.util.ProcessUtil;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

/**
 * 机构基本信息新增 类描述
 * @author zwh
 * @date 2020-1-4
 */
public final class JgjbxxxzwzbBPO extends BPO{
	/**
	 * 加载新增机构的基本信息 zwh 2020-1-4
	 */
	public final DataObject queryOrgInfo(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), rdo;
		DataStore gdxxds = DataStore.getInstance(), orginfods = DataStore.getInstance(), orgtypeds = DataStore.getInstance(), jgtzds = DataStore.getInstance();
		String piid, belongorgno, typeno = "";
		String fullname = "", displayname = "", jgtz = "";
		String userid = this.getUser().getUserid();
		Date sysdate = DateUtil.getDBTime();
  		de.clearSql();

		// 流程开始获取piid
		piid = para.getString("piid");
		ProcessUtil.setTEEVarByPiid(piid, "managerno", userid);
		
		// 查询工单信息
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");

		// 如果无工单，创建工单
		if (gdxxds.rowCount() == 0) {
			para.put("piid", piid);
			BPO ibpo = this.newBPO(ProcessBPO.class);
			DataObject result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
			belongorgno = result.getString("suporgno");
			
			// 查询机构的上级机构特征
			String belongjgtz = OdssuUtil.getBelongJgtz(belongorgno);
			orginfods.put(0, "belongjgtz", belongjgtz+".");

			// 创建工单表
			de.clearSql();
  			de.addSql("insert into odssuws.jgjbxxxzwzb ");
  			de.addSql("  ( piid, belongorgno, operator, operationtime,orgno)  ");
  			de.addSql(" values(:piid, :belongorgno, :userid, :sysdate,:orgno) ");
			this.de.setString("piid", piid);
			this.de.setString("belongorgno", belongorgno);
			this.de.setString("userid", userid);
			this.de.setDateTime("sysdate", sysdate);
			this.de.setString("orgno", "[自动生成]");
			this.de.update();
			
			// 自动生成默认机构特征
			for (int i = 0; i < 10000; i++) {
				jgtz = belongorgno + i;
				de.clearSql();
  				de.addSql("select 1 from odssu.orginfor a where a.jgtz = :jgtz ");
				this.de.setString("jgtz", belongjgtz+"."+jgtz);
				jgtzds = this.de.query();
				if (jgtzds.rowCount() == 0) {
					break;
				}
			}
			orginfods.put(0,"orgno", "[自动生成]");
			orginfods.put(0, "piid", piid);
			orginfods.put(0, "jgtz", jgtz);
		} else {
			fullname = gdxxds.getString(0, "fullname");
			displayname = gdxxds.getString(0, "displayname");
			belongorgno = gdxxds.getString(0, "belongorgno");
			typeno = gdxxds.getString(0, "orgtype");
			jgtz = gdxxds.getString(0, "jgtz");
			String xzqhdm = gdxxds.getString(0, "xzqhdm");
			String orgno = gdxxds.getString(0, "orgno");
			orginfods.put(0, "xzqhdm", xzqhdm);
			// 查询机构的上级机构特征
			String belongjgtz = OdssuUtil.getBelongJgtz(belongorgno);
			orginfods.put(0, "belongjgtz", belongjgtz+".");
			
			if (typeno != null && typeno.trim().isEmpty() == false) {
				de.clearSql();
  				de.addSql(" select typeno,typename from odssu.org_type where typeno = :typeno ");
				de.setString("typeno", typeno);
				DataStore vdstypeno = de.query();

				if (vdstypeno.rowCount() == 0) {
					this.bizException("没有找到编号为【" + typeno + "】的机构类型！");
				}
				orginfods.put(0, "typeno", vdstypeno.getString(0, "typeno"));
				orginfods.put(0, "typename", vdstypeno.getString(0, "typename"));
			}
			if(orgno == null || "".equals(orgno)) {
				orginfods.put(0,"orgno", "[自动生成]");
			}else {
				orginfods.put(0,"orgno", orgno);
			}
			// 如果工单表中没有机构特征
			if (jgtz == null || jgtz.equals("")) {
				// 自动生成默认机构特征
				for (int i = 0; i < 10000; i++) {
					jgtz = belongorgno + i;
					de.clearSql();
  					de.addSql("select 1 from odssu.orginfor a where a.jgtz = :jgtz and a.sleepflag = '0' ");
					this.de.setString("jgtz", belongjgtz+"."+jgtz);
					jgtzds = this.de.query();
					if (jgtzds.rowCount() == 0) {
						break;
					}
				}
			}
			orginfods.put(0, "fullname", fullname);
			orginfods.put(0, "displayname", displayname);
			orginfods.put(0, "piid", piid);
			orginfods.put(0, "jgtz", jgtz);
		}

		vdo.put("orgtypeds", orgtypeds);
		vdo.put("orginfods", orginfods);
		orginfods = null;
		orgtypeds = null;
		return vdo;
	}

	/**
	 * 新增机构的基本信息的保存方法（点击暂存和下一步时会用） zwh 2020-1-4
	 */
	public final DataObject saveOrgInfo(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String piid, fullname, displayname, orgtype, belongjgtz, jgtz, jgtzinfo;
		String detailtype = para.getString("detailtype");
		String xzqhdm = para.getString("xzqhdm");
		String orgno = para.getString("orgno");
  		de.clearSql();

		piid = para.getString("piid");
		orgtype = para.getString("orgtype", "");
		fullname = para.getString("fullname");
		if (fullname == null || fullname.trim().isEmpty()) {
			this.bizException("机构名称为空！");
		}
		if(fullname.length()>100){
			this.bizException("机构名称的长度过长，请重新输入！（不超过100个字符）");	
		}
		displayname = para.getString("displayname");
		if(displayname.length()>100){
			this.bizException("机构简称的长度过长，请重新输入！（不超过100个字符）");	
		}
		
		// 判断参数合法性
		if (OdssuUtil.isOrgExist(orgno)) {
			this.bizException("编号【" + orgno + "】已存在！无法再次新增");
		}
		de.clearSql();
  		de.addSql("select 1	");
  		de.addSql("  from odssu.org_type   ");
  		de.addSql(" where typeno = :typeno	   ");
		de.setString("typeno", orgtype);
		DataStore orgtypeds = de.query();
		if (orgtypeds.rowCount() <= 0) {
			this.bizException("没找到类型编号为" + orgtype + "的类型信息！");
		}
		
		jgtz = para.getString("jgtz","");
		if(jgtz.length()>20){
			this.bizException("机构特征的长度过长，请重新输入！（不超过20个字符）");
		}
		belongjgtz = para.getString("belongjgtz","");
		jgtzinfo = belongjgtz + jgtz;
		de.clearSql();
  		de.addSql(" select 1 from odssu.orginfor a  where a.jgtz = :jgtzinfo and a.sleepflag = '0' ");
		de.setString("jgtzinfo", jgtzinfo);
		DataStore jgtzds = de.query();
		if (jgtzds.rowCount() > 0) {
			this.bizException("机构特征已存在，请重新输入！");
		}
		
		String typename = para.getString("typename");
		if (orgtype != null && orgtype.trim().isEmpty() == false) {
			de.clearSql();
  			de.addSql(" select 1 from odssu.org_type where typeno = :orgtype and typename = :typename ");
			de.setString("orgtype", orgtype);
			de.setString("typename", typename);
			DataStore vds2 = de.query();
			if (vds2.rowCount() == 0) {
				this.bizException("机构类型编号【" + orgtype + "】和类型名称【" + typename + "】不匹配，保存失败！");
			}
		}
		
		// 保存到工单表
		de.clearSql();
  		de.addSql(" update odssuws.jgjbxxxzwzb ");
  		de.addSql("    set fullname = :fullname, orgname = :fullname, displayname = :displayname, orgtype = :orgtype, jgtz = :jgtz, detailtype = :detailtype,xzqhdm =:xzqhdm ");
  		de.addSql("  where piid = :piid 	       ");
		this.de.setString("fullname", fullname);
		this.de.setString("displayname", displayname);
		this.de.setString("orgtype", orgtype);
		this.de.setString("jgtz", jgtz);
		this.de.setString("piid", piid);
		this.de.setString("detailtype", detailtype);
		this.de.setString("xzqhdm", xzqhdm);
		int result = this.de.update();
		if (result == 0) {
			throw new Exception("工单信息更新失败!");
		}

		DataObject variables = DataObject.getInstance();
		variables.put("fullname", fullname);
		DataObject vdo1 = DataObject.getInstance();
		vdo1.put("piid", piid);
		vdo1.put("variables", variables);
		BPO ibpo = this.newBPO(ProcessBPO.class);
		ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
		return vdo;
	}

	/**
	 * 描述：获取机构信息   zwh 2020-1-4
	 * @throws Exception 
	 */
	public DataObject getOrgInfo(DataObject para) throws Exception{
		String piid = para.getString("piid");
		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空！");
		}
		String orgtype, belongorgno, typename, belongorgname, jgtz, belongjgtz, jgtzinfo,orgno;
		DataStore orginfods = DataStore.getInstance(), yjds = DataStore.getInstance();
		String spr = "", spyj = "", spsm = "";
		String sprq = null;
		de.clearSql();
  		de.addSql("select *	");
  		de.addSql("  from odssuws.jgjbxxxzwzb  ");
  		de.addSql(" where piid = :piid		       ");
		this.de.setString("piid", piid);
		orginfods = this.de.query();
		if (orginfods.rowCount() > 0) {
			orgno = orginfods.getString(0, "orgno");
			orgtype = orginfods.getString(0, "orgtype");
			typename = OdssuUtil.getOrgTypeNameByTypeNo(orgtype);
			belongorgno = orginfods.getString(0, "belongorgno");
			belongorgname = OdssuUtil.getOrgNameByOrgno(belongorgno);
			spr = orginfods.getString(0, "reviewer");
			sprq = orginfods.getDateToString(0, "reviewtime", "yyyy-mm-dd");
			spyj = orginfods.getString(0, "spyj");
			spsm = orginfods.getString(0, "spsm");
			orginfods.put(0, "orgno", orgno);
			orginfods.put(0, "typename", typename);
			orginfods.put(0, "belongorgname", belongorgname);
			
			// 组装完整的机构特征
			belongjgtz = OdssuUtil.getBelongJgtz(belongorgno);
			jgtz = orginfods.getString(0, "jgtz");
			jgtzinfo = belongjgtz + "." + jgtz;
			orginfods.put(0, "jgtz", jgtzinfo);
		}
		yjds.put(0, "spyj", spyj);
		yjds.put(0, "spsm", spsm);
		yjds.put(0, "spr", spr);
		yjds.put(0, "sprq", sprq);

		orginfods.put(0, "piid", piid);

		DataObject vdo = DataObject.getInstance();
		vdo.put("orginfods", orginfods);
		vdo.put("yjds", yjds);
		return vdo;
	}	
	
	/**
	 * 查询机构基本信息新增的工单信息
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-11
	 * @param piid
	 * @return
	 * @throws Exception
	 */
	public final DataObject getGdxx(String piid) throws Exception {
		DataObject rdo = DataObject.getInstance();
		DataStore gdxxds = DataStore.getInstance();
  		de.clearSql();

		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from odssuws.jgjbxxxzwzb ");
  		de.addSql(" where piid=:piid ");
		this.de.setString("piid", piid);
		gdxxds = this.de.query();

		rdo.put("gdxxds", gdxxds);
		gdxxds = null;

		return rdo;
	}

	/**
	 * 跳转到确定新增机构的名称和类型任务界面之前的查询方法
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-11
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageDOrgsInfo(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), rdo;
		DataStore gdxxds = DataStore.getInstance(), orginfods = DataStore.getInstance(), orgtypeds = DataStore.getInstance(), jgtzds = DataStore.getInstance();
		String piid, belongorgno, typeno = "";
		String fullname = "", displayname = "", jgtz = "";
		String userid = this.getUser().getUserid();
		Date sysdate = DateUtil.getDBTime();
  		de.clearSql();

		// 流程开始获取piid
		piid = para.getString("piid");

		// 查询工单信息
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");

		// 如果无工单，创建工单
		if (gdxxds.rowCount() == 0) {
			para.put("piid", piid);
			BPO ibpo = this.newBPO(ProcessBPO.class);
			DataObject result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
			belongorgno = result.getString("suporgno");
			
			// 查询机构的上级机构特征
			String belongjgtz = OdssuUtil.getBelongJgtz(belongorgno);
			orginfods.put(0, "belongjgtz", belongjgtz+".");

			// 创建工单表
			de.clearSql();
  			de.addSql("insert into odssuws.jgjbxxxzwzb ");
  			de.addSql("  ( piid, belongorgno, operator, operationtime)  ");
  			de.addSql(" values(:piid, :belongorgno, :userid, :sysdate) ");
			this.de.setString("piid", piid);
			this.de.setString("belongorgno", belongorgno);
			this.de.setString("userid", userid);
			this.de.setDateTime("sysdate", sysdate);
			this.de.update();
			
			// 自动生成默认机构特征
			for (int i = 0; i < 10000; i++) {
				jgtz = belongorgno + i;
				de.clearSql();
  				de.addSql("select 1 from odssu.orginfor a where a.jgtz = :jgtz ");
				this.de.setString("jgtz", belongjgtz+"."+jgtz);
				jgtzds = this.de.query();
				if (jgtzds.rowCount() == 0) {
					break;
				}
			}

			orginfods.put(0, "piid", piid);
			orginfods.put(0, "jgtz", jgtz);
		} else {
			fullname = gdxxds.getString(0, "fullname");
			displayname = gdxxds.getString(0, "displayname");
			belongorgno = gdxxds.getString(0, "belongorgno");
			typeno = gdxxds.getString(0, "orgtype");
			jgtz = gdxxds.getString(0, "jgtz");
			
			// 查询机构的上级机构特征
			String belongjgtz = OdssuUtil.getBelongJgtz(belongorgno);
			orginfods.put(0, "belongjgtz", belongjgtz+".");

			if (typeno != null && typeno.trim().isEmpty() == false) {
				de.clearSql();
  				de.addSql(" select typeno,typename from odssu.org_type where typeno = :typeno ");
				de.setString("typeno", typeno);
				DataStore vdstypeno = de.query();

				if (vdstypeno.rowCount() == 0) {
					this.bizException("没有找到编号为【" + typeno + "】的机构类型！");
				}
				orginfods.put(0, "typeno", vdstypeno.getString(0, "typeno"));
				orginfods.put(0, "typename", vdstypeno.getString(0, "typename"));
			}
			// 如果工单表中没有机构特征
			if (jgtz == null || jgtz.equals("")) {
				// 自动生成默认机构特征
				for (int i = 0; i < 10000; i++) {
					jgtz = belongorgno + i;
					de.clearSql();
  					de.addSql("select 1 from odssu.orginfor a where a.jgtz = :jgtz ");
					this.de.setString("jgtz", belongjgtz+"."+jgtz);
					jgtzds = this.de.query();
					if (jgtzds.rowCount() == 0) {
						break;
					}
				}
			}
			orginfods.put(0, "fullname", fullname);
			orginfods.put(0, "displayname", displayname);
			orginfods.put(0, "piid", piid);
			orginfods.put(0, "jgtz", jgtz);
		}

		vdo.put("orgtypeds", orgtypeds);
		vdo.put("orginfods", orginfods);
		orginfods = null;
		orgtypeds = null;
		return vdo;
	}

	/**
	 * 展示该新增的机构可以使用那些机构类型
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-9
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwOrgkydOrgTypeLov(DataObject para) throws Exception {

		String belongorgname;
		String belongorgtype;

		String piid = para.getString("piid");
		String typeno = para.getString("typeno");
		para.put("piid", piid);

		BPO ibpo = this.newBPO(ProcessBPO.class);
		DataObject result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
		String belongorgno = result.getString("suporgno");
    		de.clearSql();
		de.clearSql();
  		de.addSql("select orgname, orgtype	");
  		de.addSql("  from odssu.orginfor  ");
  		de.addSql(" where orgno = :belongorgno		  ");
		this.de.setString("belongorgno", belongorgno);
		DataStore dstmp = this.de.query();

		if (dstmp.rowCount() > 0) {
			belongorgname = dstmp.getString(0, "orgname");
			belongorgtype = dstmp.getString(0, "orgtype");
		} else {
			belongorgname = "";
			belongorgtype = "";
			if ("".equals(belongorgname) || belongorgname == "") {
				this.bizException("上级机构名称信息异常!");
			}
			if ("".equals(belongorgtype) || belongorgtype == "") {
				this.bizException("上级机构类型信息异常!");
			}
		}
		typeno = "%" + typeno + "%";

		de.clearSql();
  		de.addSql("select b.subtypeno orgtype,a.typename,a.typename displaytypename, a.comments, null flag , '9' detailtype ");
  		de.addSql("  from odssu.org_type a,         ");
  		de.addSql("       odssu.ir_org_type b       ");
  		de.addSql(" where a.typeno = b.subtypeno    ");
  		de.addSql("   and b.suptypeno = :belongorgtype   and b.subtypeno like :typeno         ");
  		de.addSql("   and a.yxzjjgbz = '1'  ");
  		de.addSql(" order by b.subtypeno ");
		this.de.setString("belongorgtype", belongorgtype);
		this.de.setString("typeno", typeno);
		DataStore orgtypeds = this.de.query();
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("orgtypeds", orgtypeds);
		return vdo;
	}
	
	/**
	 * 展示该新增的机构可以使用那些机构类型
	 * 
	 * @Description:
	 * @author 林志鹏
	 * @date 2018-12-25
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwOrgkydOrgTypeLovforJN(DataObject para) throws Exception {
		
		String belongorgname;
		String belongorgtype;
		
		String piid = para.getString("piid");
		String typeno = para.getString("typeno");
		para.put("piid", piid);
		
		BPO ibpo = this.newBPO(ProcessBPO.class);
		DataObject result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
		String belongorgno = result.getString("suporgno");
		de.clearSql();
		de.clearSql();
		de.addSql("select orgname, orgtype	");
		de.addSql("  from odssu.orginfor  ");
		de.addSql(" where orgno = :belongorgno		  ");
		this.de.setString("belongorgno", belongorgno);
		DataStore dstmp = this.de.query();
		
		if (dstmp.rowCount() > 0) {
			belongorgname = dstmp.getString(0, "orgname");
			belongorgtype = dstmp.getString(0, "orgtype");
		} else {
			belongorgname = "";
			belongorgtype = "";
			if ("".equals(belongorgname) || belongorgname == "") {
				this.bizException("上级机构名称信息异常!");
			}
			if ("".equals(belongorgtype) || belongorgtype == "") {
				this.bizException("上级机构类型信息异常!");
			}
		}
		typeno = "%" + typeno + "%";
		
		de.clearSql();
		de.addSql("select b.subtypeno orgtype,a.typename,a.typename displaytypename, a.comments, null flag,'0' detailtype");
		de.addSql("  from odssu.org_type a,         ");
		de.addSql("       odssu.ir_org_type b       ");
		de.addSql(" where a.typeno = b.subtypeno    ");
		de.addSql("   and b.suptypeno = :belongorgtype   and b.subtypeno like :typeno         ");
		de.addSql("   and a.yxzjjgbz = '1'  ");
		de.addSql(" order by b.subtypeno ");
		this.de.setString("belongorgtype", belongorgtype);
		this.de.setString("typeno", typeno);
		DataStore orgtypeds = this.de.query();
		for(int i = 0 ; i<orgtypeds.size() ; i++) {
			String orgtype = orgtypeds.getString(i, "orgtype");
			String comments = orgtypeds.getString(i, "comments");
			String typename = orgtypeds.getString(i, "typename");
			if(orgtype.equals("HSDOMAIN_SBS")) {
				orgtypeds.put(i, "displaytypename", "乡镇");
				DataObject addRow = DataObject.getInstance();
				addRow.put("orgtype", orgtype);
				addRow.put("typename", typename);
				addRow.put("displaytypename", "街道");
				addRow.put("comments", comments);
				addRow.put("detailtype", "1");
				orgtypeds.addRow(addRow);
				break;
			}
			if(orgtype.equals("HSDOMAIN_SBZ")) {
				orgtypeds.put(i, "displaytypename", "社区");
				DataObject addRow = DataObject.getInstance();
				addRow.put("orgtype", orgtype);
				addRow.put("typename", typename);
				addRow.put("displaytypename", "村");
				addRow.put("comments", comments);
				addRow.put("detailtype", "1");
				orgtypeds.addRow(addRow);
				break;
			}
		}
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("orgtypeds", orgtypeds);
		return vdo;
	}

	/**
	 * 确定机构的名称和类型任务的暂存方法
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-11
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveOrgsInfo(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String piid, fullname, displayname, orgtype, belongjgtz, jgtz, jgtzinfo;
		String detailtype = para.getString("detailtype");
  		de.clearSql();

		piid = para.getString("piid");
		fullname = para.getString("fullname");
		if(fullname.length()>35){
			this.bizException("机构名称的长度过长，请重新输入！（不超过35个字符）");	
		}
		displayname = para.getString("displayname");
		if(displayname.length()>35){
			this.bizException("机构简称的长度过长，请重新输入！（不超过35个字符）");	
		}
		
		jgtz = para.getString("jgtz","");
		if(jgtz.length()>20){
			this.bizException("机构特征的长度过长，请重新输入！（不超过20个字符）");
		}
		belongjgtz = para.getString("belongjgtz","");
		jgtzinfo = belongjgtz + jgtz;
		de.clearSql();
  		de.addSql(" select 1 from odssu.orginfor a  where a.jgtz = :jgtzinfo ");
		de.setString("jgtzinfo", jgtzinfo);
		DataStore jgtzds = de.query();
		if (jgtzds.rowCount() > 0) {
			this.bizException("机构特征已存在，请重新输入！");
		}
		
		orgtype = para.getString("orgtype", "");
		String typename = para.getString("typename");

		if (orgtype != null && orgtype.trim().isEmpty() == false) {
			de.clearSql();
  			de.addSql(" select 1 from odssu.org_type where typeno = :orgtype and typename = :typename ");
			de.setString("orgtype", orgtype);
			de.setString("typename", typename);
			DataStore vds2 = de.query();
			if (vds2.rowCount() == 0) {
				this.bizException("机构类型编号【" + orgtype + "】和类型名称【" + typename
						+ "】不匹配，保存失败！");
			}

		}
		
		// 保存到工单表
		de.clearSql();
  		de.addSql(" update odssuws.jgjbxxxzwzb ");
  		de.addSql("    set fullname = :fullname, displayname = :displayname, orgtype = :orgtype, jgtz = :jgtz  , detailtype = :detailtype ");
  		de.addSql("  where piid = :piid 	       ");
		this.de.setString("fullname", fullname);
		this.de.setString("displayname", displayname);
		this.de.setString("orgtype", orgtype);
		this.de.setString("jgtz", jgtz);
		this.de.setString("piid", piid);
		this.de.setString("detailtype", detailtype);
		int result = this.de.update();

		if (result == 0) {
			throw new Exception("工单信息更新失败!");
		}

		DataObject variables = DataObject.getInstance();
		variables.put("fullname", fullname);
		DataObject vdo1 = DataObject.getInstance();
		vdo1.put("piid", piid);
		vdo1.put("variables", variables);
		BPO ibpo = this.newBPO(ProcessBPO.class);
		ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);

		return vdo;
	}

	/**
	 * 跳转到确定新增机构的标识名称任务界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-11
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageDOrgName(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore orginfods = DataStore.getInstance(), dstmp, orgnameds;
  		de.clearSql();
		String orgname = "", fullname = "";
		String piid = para.getString("piid");

		de.clearSql();
  		de.addSql("select *	");
  		de.addSql("  from odssuws.jgjbxxxzwzb  ");
  		de.addSql(" where piid = :piid		       ");
		this.de.setString("piid", piid);
		dstmp = this.de.query();
		if (dstmp.rowCount() > 0) {
			orgname = dstmp.getString(0, "orgname");
			fullname = dstmp.getString(0, "fullname");
			// displayname = dstmp.getString(0, "displayname");
			if ("".equals(orgname) || orgname == null) {
				orgname = fullname;
			}
		}

		de.clearSql();
  		de.addSql("select orgno, fullname, displayname, orgname	");
  		de.addSql("  from odssu.orginfor  ");
  		de.addSql(" where fullname = :fullname ");
		this.de.setString("fullname", fullname);
		orgnameds = this.de.query();

		orginfods.put(0, "orgname", orgname);
		orginfods.put(0, "piid", piid);

		vdo.put("orginfods", orginfods);
		vdo.put("orgnameds", orgnameds);
		return vdo;
	}

	/**
	 * 在确定新增机构的标识名称任务界面，点击上一步的操作
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-11
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject delorgName(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String piid;
  		de.clearSql();

		piid = para.getString("piid");

		// 保存到工单表
		de.clearSql();
  		de.addSql(" update odssuws.jgjbxxxzwzb ");
  		de.addSql("    set orgname = null   ");
  		de.addSql("  where piid = :piid 	       ");
		this.de.setString("piid", piid);
		this.de.update();

		return vdo;
	}

	/**
	 * 确定新增机构的标识名称任务界面，点击下一步后保存标识名称
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-11
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveOrgName(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String piid, orgname;
  		de.clearSql();
		String flag = "true";

		piid = para.getString("piid");
		orgname = para.getString("orgname");

		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空！");
		}
		if (orgname == null || orgname.trim().isEmpty()) {
			this.bizException("机构名称为空！");
		}
		if (orgname.length()>35){
        	this.bizException("机构标识名称的长度过长，请重新输入！");
        }
		// if (OdssuUtil.isIdnameExist("", orgname)) {
		// this.bizException("标识姓名【" + orgname + "】已存在！");
		// }

  		de.addSql(" select 1 from odssu.orginfor where orgname = :orgname ");
		this.de.setString("orgname", orgname);

		DataStore orgds = this.de.query();

		if (orgds.rowCount() != 0) {
			flag = "repeat";
			vdo.put("flag", flag);

			return vdo;
		}

		// 保存到工单表
		de.clearSql();
  		de.addSql(" update odssuws.jgjbxxxzwzb ");
  		de.addSql("    set orgname = :orgname   ");
  		de.addSql("  where piid = :piid 	       ");
		this.de.setString("orgname", orgname);
		this.de.setString("piid", piid);
		int result = this.de.update();

		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}

		vdo.put("flag", flag);

		return vdo;
	}

	/**
	 * 跳转到确定机构编号的界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-11
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageDOrgNo(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore orginfods = DataStore.getInstance(), dstmp, orgtypeds;
  		de.clearSql();
		String orgno = "", typeno = "";
		String allowletter, allowunderline, qzyfjgbhdt, mes = "",jgtz="";
		int minlength, maxlength;
		String piid = para.getString("piid");

		de.clearSql();
  		de.addSql("select *	");
  		de.addSql("  from odssuws.jgjbxxxzwzb  ");
  		de.addSql(" where piid = :piid		  ");
		this.de.setString("piid", piid);
		dstmp = this.de.query();
		if (dstmp.rowCount() > 0) {
			orgno = dstmp.getString(0, "orgno");
			jgtz = dstmp.getString(0, "jgtz");
			typeno = dstmp.getString(0, "orgtype");
		}
		orginfods.put(0, "jgtz", jgtz);
		orginfods.put(0, "orgno", orgno);
		orginfods.put(0, "piid", piid);

		de.clearSql();
  		de.addSql("select *	");
  		de.addSql("  from odssu.org_type   ");
  		de.addSql(" where typeno = :typeno	   ");
		this.de.setString("typeno", typeno);
		orgtypeds = this.de.query();
		if (orgtypeds.rowCount() <= 0) {
			this.bizException("没找到类型编号为" + typeno + "的类型信息！");
		}
		minlength = orgtypeds.getInt(0, "minlength");// 直接返回
		maxlength = orgtypeds.getInt(0, "maxlength");// 直接返回
		allowletter = orgtypeds.getString(0, "allowletter");
		allowunderline = orgtypeds.getString(0, "allowunderline");
		qzyfjgbhdt = orgtypeds.getString(0, "qzyfjgbhdt");

		mes = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;● 机构编号组成:<br/><br/>";
		mes = mes
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;① 机构编号允许含有数字<br/><br/>";
		if ("0".equals(allowletter)) {
			mes = mes
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;② 机构编号不允许含有字母<br/><br/>";
		} else if ("1".equals(allowletter)) {
			mes = mes
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;② 机构编号允许含有字母<br/><br/>";
		}

		if ("0".equals(allowunderline)) {
			mes = mes
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;③ 机构编号不允许含有下划线<br/><br/>";
		} else if ("1".equals(allowletter)) {
			mes = mes
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;③ 机构编号允许含有下划线<br/><br/>";
		}
		mes = mes
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;④机构编号不允许含有其他特殊字符<br/><br/>";

		if (minlength == maxlength) {
			mes = mes
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;● 机构编号长度为"
					+ minlength + "位;<br/><br/>";
		} else {
			mes = mes
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;● 机构编号长度为"
					+ minlength + "~" + maxlength + "位<br/><br/>";
		}

		String belongorgno = dstmp.getString(0, "belongorgno");
		if ("1".equals(qzyfjgbhdt)) {
			mes = mes
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;● 本机构编号中被强制以上级机构编号【"
					+ belongorgno + "】打头<br/><br/>";
		} else if ("0".equals(qzyfjgbhdt)) {
		}

		vdo.put("mes", mes);
		vdo.put("minlength", minlength);
		vdo.put("maxlength", maxlength);
		vdo.put("orginfods", orginfods);
		return vdo;
	}

	/**
	 * 确定机构的编号任务界面，点击下一步或者暂存的操作
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-11
	 * @param request
	 * @param response
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveOrgNo(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String piid, orgno, allowletter, qzyfjgbhdt, belongorgno = "";
  		de.clearSql();
		DataStore orgtypeds;
		String allowunderline, typeno = "";
		int minlength, maxlength;

		piid = para.getString("piid");
		orgno = para.getString("orgno");

		// 判断参数合法性
		if (OdssuUtil.isOrgExist(orgno)) {
			this.bizException("编号【" + orgno + "】已存在！无法再次新增");
		}
		de.clearSql();
  		de.addSql("select *	");
  		de.addSql("  from odssuws.jgjbxxxzwzb  ");
  		de.addSql(" where piid = :piid		  ");
		this.de.setString("piid", piid);
		DataStore dstmp = this.de.query();
		if (dstmp.rowCount() > 0) {
			typeno = dstmp.getString(0, "orgtype");
			belongorgno = dstmp.getString(0, "belongorgno");
		}

		de.clearSql();
  		de.addSql("select *	");
  		de.addSql("  from odssu.org_type   ");
  		de.addSql(" where typeno = :typeno	   ");
		this.de.setString("typeno", typeno);
		orgtypeds = this.de.query();
		if (orgtypeds.rowCount() <= 0) {
			this.bizException("没找到类型编号为" + typeno + "的类型信息！");
		}
		minlength = orgtypeds.getInt(0, "minlength");// 直接返回
		maxlength = orgtypeds.getInt(0, "maxlength");// 直接返回
		allowletter = orgtypeds.getString(0, "allowletter");
		allowunderline = orgtypeds.getString(0, "allowunderline");
		qzyfjgbhdt = orgtypeds.getString(0, "qzyfjgbhdt");

		if (!"[自动生成]".equals(orgno)) {
			if (orgno.length() >= minlength && orgno.length() <= maxlength) {
				//
			} else {
				this.bizException("机构编号为" + orgno + "的长度不符合要求，请检查！");
			}

			// 判断是否只含有数字、字母、下划线
			if (orgno.matches("[0-9A-Za-z_]*") == false) {
				this.bizException("机构编号中含有除数字、字母、下划线之外的其他字符，保存失败！");
			}

			// 判断机构编号字母、下划线是否合法
			if ("0".equals(allowletter)) {
				Pattern p = Pattern.compile("[a-zA-Z]+");
				Matcher m = p.matcher(orgno);
				if (m.matches() == true) {
					this.bizException("该机构类型决定的机构编号中不能含有字母，请检查！");
				}
			}

			if ("0".equals(allowunderline)) {
				if (orgno.indexOf("_") >= 0) {
					this.bizException("该机构类型决定的机构编号中不能含有下划线，请检查！");
				}
			}

			// 判断机构编号是否被强制以父机构打头
			if ("1".equals(qzyfjgbhdt)) {
				if (orgno.indexOf(belongorgno) < 0) {
					this.bizException("该机构类型决定的机构编号必须以父机构编号打头，保存失败！");
				}
			}
		}
        
		
		// 保存到工单表
		de.clearSql();
  		de.addSql(" update odssuws.jgjbxxxzwzb ");
  		de.addSql("    set orgno = :orgno,reviewer =null,reviewtime = null,spyj = null,spsm = null ");
  		de.addSql("  where piid = :piid ");
		this.de.setString("orgno", orgno);
		this.de.setString("piid", piid);
		int result = this.de.update();

		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}

		return vdo;
	}

	/**
	 * 跳转到审批新增机构任务界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-11
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageDOrgApproval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore orginfods = DataStore.getInstance(), yjds = DataStore.getInstance();
  		de.clearSql();
		String orgtype, belongorgno, typename, belongorgname, jgtz, belongjgtz, jgtzinfo;
		String spr = "", spyj = "", spsm = "";
		String sprq = null;
		String piid = para.getString("piid");

		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空！");
		}
		de.clearSql();
  		de.addSql("select *	");
  		de.addSql("  from odssuws.jgjbxxxzwzb  ");
  		de.addSql(" where piid = :piid		       ");
		this.de.setString("piid", piid);
		orginfods = this.de.query();
		if (orginfods.rowCount() > 0) {
			orgtype = orginfods.getString(0, "orgtype");
			typename = OdssuUtil.getOrgTypeNameByTypeNo(orgtype);
			belongorgno = orginfods.getString(0, "belongorgno");
			belongorgname = OdssuUtil.getOrgNameByOrgno(belongorgno);
			spr = orginfods.getString(0, "reviewer");
			sprq = orginfods.getDateToString(0, "reviewtime", "yyyy-mm-dd");
			spyj = orginfods.getString(0, "spyj");
			spsm = orginfods.getString(0, "spsm");
			orginfods.put(0, "typename", typename);
			orginfods.put(0, "belongorgname", belongorgname);
			
			// 组装完整的机构特征
			belongjgtz = OdssuUtil.getBelongJgtz(belongorgno);
			jgtz = orginfods.getString(0, "jgtz");
			jgtzinfo = belongjgtz + "." + jgtz;
			orginfods.put(0, "jgtz", jgtzinfo);
		}

		yjds.put(0, "spyj", spyj);
		yjds.put(0, "spsm", spsm);
		yjds.put(0, "spr", spr);
		yjds.put(0, "sprq", sprq);

		orginfods.put(0, "piid", piid);

		vdo.put("orginfods", orginfods);
		vdo.put("yjds", yjds);
		return vdo;
	}

	/**
	 * 审批新增机构界面，点击暂存的操作
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-11
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveApproved(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();
		String piid, spyj, spsm, spr;
		Date sprq;

		piid = para.getString("piid");
		spyj = para.getString("spyj");
		spsm = para.getString("spsm", "");
		spr = para.getString("spr");
		sprq = para.getDate("sprq");

		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空！");
		}

		// 保存审批意见
		de.clearSql();
  		de.addSql(" update odssuws.jgjbxxxzwzb ");
  		de.addSql("    set spyj = :spyj  ,spsm = :spsm,reviewer = :spr,reviewtime  =:sprq   ");
  		de.addSql("  where piid = :piid 	         ");
		this.de.setString("spyj", spyj);
		this.de.setString("spsm", spsm);

		this.de.setString("spr", spr);
		this.de.setDateTime("sprq", sprq);
		this.de.setString("piid", piid);
		int result2 = this.de.update();

		if (result2 == 0) {
			this.bizException("将审批意见更新到工单表中时出错，请联系开发人员！");
		}
		
		// 保存一条公共审批
		de.clearSql();
  		de.addSql("delete from odssuws.spinfor ");
  		de.addSql("  where piid = :piid and splbdm = :splbdm");
		this.de.setString("piid", piid);
		this.de.setString("splbdm", "ryfz");
		this.de.update();
		
		String spyjdm = "pass";
		if (spyj.equals("1")) {
			spyjdm = "reject";
		}else if (spyj.equals("2")) {
			spyjdm = "revise";
		}
		
		de.clearSql();
  		de.addSql("insert into odssuws.spinfor (piid,splbdm,spyjdm,spr,spsj,spsm)");
  		de.addSql("  values (:piid,:para2,:spyjdm,:para4,:sprq,:spsm)");
		this.de.setString("piid", piid);
		this.de.setString("para2", "ryfz");
		this.de.setString("spyjdm", spyjdm);
		this.de.setString("para4", this.getUser().getUserid());
		this.de.setDateTime("sprq", sprq);
		this.de.setString("spsm", spsm);
		this.de.update();

		return vdo;
	}

	/**
	 * 检测机构的合法性
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-4
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject orgValidityCheck(DataObject para) throws Exception {

		String orgno = para.getString("orgno");
		String orgname = para.getString("orgname");

		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
		if (orgname == null || orgname.trim().isEmpty()) {
			this.bizException("机构名称为空！");
		}
    		de.clearSql();
  		de.addSql(" select orgname from odssu.orginfor where orgno = :orgno and sleepflag = '0' ");
		this.de.setString("orgno", orgno);

		DataStore orgds = this.de.query();

		if (orgds.rowCount() == 0) {
			this.bizException("机构编号所对应的机构信息不存在或机构已被注销！");
		}

		String orgnameOld = orgds.getString(0, "orgname");
		if (orgnameOld == null || orgnameOld.trim().isEmpty()) {
			this.bizException("机构编号所对应的机构名称为空！");
		}

		if (!orgname.equals(orgnameOld)) {
			this.bizException("机构编号所对应的机构名称出错！");
		}

		return null;
	}

	/**
	 * 检测机构类型及机构类型名称的合法性
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-4
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject orgTypeValidityCheck(DataObject para) throws Exception {

		String typeno = para.getString("typeno");
		String typename = para.getString("typename");
		String jgtz = para.getString("jgtz");

		if (typeno == null || typeno.trim().isEmpty()) {
			this.bizException("机构类型编号为空！");
		}
		if (typename == null || typename.trim().isEmpty()) {
			this.bizException("机构类型名称为空！");
		}
		if (jgtz == null || jgtz.trim().isEmpty()) {
			this.bizException("机构特征为空！");
		}
		
		Pattern p = Pattern.compile("^[a-zA-Z0-9]{1,20}$");
		Matcher m = p.matcher(jgtz);
		if (m.matches() == false) {
			this.bizException("机构特征必须为1~20位的字母或数字，请重新输入！");
		}
    		de.clearSql();
  		de.addSql(" select typename from odssu.org_type where typeno = :typeno  ");
		this.de.setString("typeno", typeno);

		DataStore orgtypeds = this.de.query();

		if (orgtypeds.rowCount() == 0) {
			this.bizException("机构类型编号所对应的机构类型信息不存在或机构已被注销！");
		}

		String typenameOld = orgtypeds.getString(0, "typename");
		if (typenameOld == null || typenameOld.trim().isEmpty()) {
			this.bizException("机构类型编号所对应的机构类型名称为空！");
		}

		if (!typename.equals(typenameOld)) {
			this.bizException("机构类型编号所对应的机构类型名称出错！");
		}

		return null;
	}

	/**
	 * 检测机构编号的合法性
	 * @Author zy
	 * @description
	 * @date 2020年11月9日
	 * @return
	 */
	public final DataObject orgnoValidityCheck(DataObject para) throws Exception {

		String orgno = para.getString("orgno");
		String orgtype = para.getString("orgtype");
		String piid = para.getString("piid");
		BPO ibpo = this.newBPO(ProcessBPO.class);
		DataObject result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
		String belongorgno = result.getString("suporgno");
		
		// 判断参数合法性
		if (OdssuUtil.isOrgExist(belongorgno) == false) {
			this.bizException("隶属机构【" + belongorgno + "】不存在，保存失败！");
		}
		de.clearSql();
  		de.addSql(" select orgtype from odssu.orginfor where orgno = :belongorgno ");
		de.setString("belongorgno", belongorgno);
		DataStore vdsbeltype = de.query();
		if (vdsbeltype.rowCount() == 0) {
			this.bizException("没有找到上级机构的机构类型，保存失败！");
		}
		String beltype = vdsbeltype.getString(0, "orgtype");
		
		de.clearSql();
  		de.addSql(" select 1 from odssu.ir_org_type where suptypeno = :beltype and subtypeno = :orgtype ");
		de.setString("beltype", beltype);
		de.setString("orgtype", orgtype);
		DataStore vds11 = de.query();
		if (vds11.rowCount() == 0) {
			this.bizException("新增机构的类型与其上级机构的机构类型不匹配，保存失败！");
		}
		
		de.clearSql();
  		de.addSql(" select typeno,minlength,maxlength,allowletter,allowunderline,qzyfjgbhdt ");
  		de.addSql(" from odssu.org_type where typeno = :orgtype                             ");
		de.setString("orgtype", orgtype);
		DataStore vds21 = de.query();

		if (vds21.rowCount() == 0) {
			this.bizException("没有找到机构类型编号为【" + orgtype + "】的信息，保存失败！");
		}
		int minlength = vds21.getInt(0, "minlength");
		int maxlength = vds21.getInt(0, "maxlength");
		String allowletter = vds21.getString(0, "allowletter");
		String allowunderline = vds21.getString(0, "allowunderline");
		String qzyfjgbhdt = vds21.getString(0, "qzyfjgbhdt");

		if ("1".equals(qzyfjgbhdt)) {//强制以父机构编号打头（1：是  0：否）
			if(!"[自动生成]".equals(orgno)) {
				if (orgno.indexOf(belongorgno) < 0) {
					this.bizException("该机构类型决定的机构编号必须以父机构编号["+belongorgno+"]打头，保存失败，请重新输入机构编号！");
				}
			}
		}
		if(!"[自动生成]".equals(orgno)) {
			// 判断是否只含有数字、字母、下划线
			if (orgno.matches("[0-9A-Za-z_]*") == false) {
				this.bizException("生成的机构编号["+orgno+"]中含有除数字、字母、下划线之外的其他字符，保存失败！");
			}

			// 判断机构编号的长度合不合法
			if (orgno.length() < minlength || orgno.length() > maxlength) {
				this.bizException("生成的机构编号["+orgno+"]的长度不合法，长度应该在" + minlength + "与" + maxlength
						+ "之间!");
			}
			// 判断机构编号字母是否合法
			if ("0".equals(allowletter)) {
				Pattern p = Pattern.compile("[a-zA-Z]+");
				Matcher m = p.matcher(orgno);
				if (m.matches() == true) {
					this.bizException("该机构类型决定的机构编号中不能含有字母，保存失败！");
				}
			}
			// 判断机构编号下划线是否合法
			if ("0".equals(allowunderline)) {
				if (orgno.indexOf("_") >= 0) {
					this.bizException("该机构类型决定的机构编号中不能含有下划线，保存失败！");
				}
			}
			if (OdssuUtil.isOrgExist(orgno)) {
				this.bizException("输入的机构编号["+orgno+"]已存在，保存失败，请重新输入机构编号！");
			}
		}
		
		// 保存到工单表
		de.clearSql();
  		de.addSql(" update odssuws.jgjbxxxzwzb     ");
  		de.addSql("    set orgno = :orgno          ");
  		de.addSql("  where piid = :piid 	       ");
  		de.setString("orgno", orgno);
  		de.setString("piid", piid);
  		de.update();
		return null;
	}
	/**
	 * 查询标志姓名重复的机构信息
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-4
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject queryRepeatResult(DataObject para) throws Exception {
		String orgname = para.getString("orgname");

		if (orgname == null || orgname.trim().isEmpty()) {
			this.bizException("orgname 为空！");
		}
  		de.clearSql();
  		de.addSql(" select orgno , orgname , displayname  , fullname  from odssu.orginfor where orgname = :orgname ");
		this.de.setString("orgname", orgname);

		DataStore orgds = this.de.query();

		DataObject vdo = DataObject.getInstance();

		vdo.put("orgnameds", orgds);

		return vdo;
	}
}
