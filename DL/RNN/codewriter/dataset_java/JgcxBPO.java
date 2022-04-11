package com.dw.odssu.ws.org.jgcx;

import java.util.Date;

import com.dareway.apps.process.ProcessBPO;
import com.dareway.apps.process.util.ProcessUtil;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

/**
 * 机构撤銷 类描述
 * 
 * @author liuy
 * @version 1.0 创建时间 2014-05-13
 */
public final class JgcxBPO extends BPO{
	/**
	 * 跳转进入撤销机构申请界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-23
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageOrgDelApply(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), result = DataObject.getInstance(), rdo;
		DataStore gdxxds = DataStore.getInstance();
  		de.clearSql();
		String piid, orgno;
		String userid = this.getUser().getUserid();
		Date sysdate = DateUtil.getDBTime();
		// 流程开始获取piid
		piid = para.getString("piid");
		ProcessUtil.setTEEVarByPiid(piid, "managerno", userid);
		
		para.put("piid", piid);
		BPO ibpo = this.newBPO(ProcessBPO.class);
		result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
		orgno = result.getString("orgno");

		// 查询工单信息
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");

		// 如果无工单，创建工单
		if (gdxxds.rowCount() == 0) {
			// 创建工单表
			de.clearSql();
  			de.addSql("insert into odssuws.jgcx ");
  			de.addSql("  ( piid, orgno, operator, operationtime ) 	");
  			de.addSql(" values(:piid, :orgno, :userid, :sysdate) 		");
			this.de.setString("piid", piid);
			this.de.setString("orgno", orgno);
			this.de.setString("userid", userid);
			this.de.setDateTime("sysdate", sysdate);
			this.de.update();
		}

		DataObject mdo = getOrgxx(orgno);
		gdxxds = mdo.getDataStore("orgds");
		gdxxds.put(0, "piid", piid);

		// 查询此机构的相关信息
		de.clearSql();
  		de.addSql(" select distinct empno from odssu.empinfor where hrbelong = :orgno ");
		de.setString("orgno", orgno);
		DataStore vdsemp = de.query();

		de.clearSql();
  		de.addSql(" select distinct orgno from odssu.orginfor where belongorgno =  :orgno ");
		de.setString("orgno", orgno);
		DataStore vdsorg = de.query();


		String message = "";
		int i = 1;
		if (vdsemp.rowCount() > 0) {
			message = message
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
					+ i + "、 该机构共有" + vdsemp.rowCount() + "个直属人员。<br/><br/>";
			i = i + 1;
		}
		if (vdsorg.rowCount() > 0) {
			message = message
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
					+ i + "、 该机构共有" + vdsorg.rowCount() + "个下属机构。<br/><br/>";
			i = i + 1;
		}

		vdo.put("orgds", gdxxds);
		vdo.put("mes", message);
		return vdo;
	}

	/**
	 * 跳转进入恢复机构申请界面
	 * 
	 * @Description:
	 * @author 能天宇
	 * @date 2016-9-5
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageOrgRecoverApply(DataObject para) throws Exception {
		DataObject  vardo = DataObject.getInstance();
		DataStore gdxxds = DataStore.getInstance();
  		de.clearSql();
		// 流程开始获取piid
		String piid = para.getString("piid","");
		if (piid == null || "".equals(piid)) {
			this.bizException("para中传入的piid为空！");
		}
		BPO ibpo = this.newBPO(ProcessBPO.class);
		vardo = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
		String orgno = vardo.getString("orgno");
		String belongorgno = vardo.getString("belongorgno");
		if(orgno == null || belongorgno == null || "".equals(orgno) || "".equals(belongorgno)){
			throw new AppException("获取的流程变量orgno、belongorgno为空！");
		}
			
		// 检查上级机构是否存在
		de.clearSql();
  		de.addSql(" select sleepflag,createdate,orgname from odssu.orginfor ");
  		de.addSql(" where orgno = :belongorgno and sleepflag = '0' ");
		de.setString("belongorgno", belongorgno);
		DataStore vds = de.query();
		if (vds == null || vds.rowCount() == 0) {
			throw new AppException("没有找到编号为【" + orgno + "】的上级机构的信息！");
		}
		// 机构存在，获取机构基本信息
		String sleepflag = vds.getString(0, "sleepflag");
		if ("1".equals(sleepflag)) {
			this.bizException("此上级机构已经被注销，无法作为恢复注销机构的上级机构！");
		}
		
		de.clearSql();
  		de.addSql("select *  ");
  		de.addSql("  from odssuws.jgjbxxxzwzb  ");
  		de.addSql(" where piid = :piid	   ");
		this.de.setString("piid", piid);
		gdxxds = this.de.query();
		if (gdxxds == null || gdxxds.rowCount() == 0 ) {
			throw new AppException("没有找到编号为【" + piid + "】的工单信息！");
		}
		gdxxds.put(0, "belongorgname", OdssuUtil.getOrgNameByOrgno(belongorgno));
		gdxxds.put(0, "typename", OdssuUtil.getOrgTypeNameByTypeNo(gdxxds.getString(0, "orgtype")));
		DataObject vdo = DataObject.getInstance();
		//获取xzqhdm
		de.clearSql();
  		de.addSql("select xzqhdm from odssu.orginfor  ");
  		de.addSql(" where orgno = :orgno	   ");
		this.de.setString("orgno", orgno);
		DataStore vdsxzqh = this.de.query();

		String xzqhdm = vdsxzqh.getString(0, "xzqhdm");
		ProcessUtil.setTEEVarByPiid(piid,"xzqhdm",xzqhdm);
		gdxxds.put(0, "xzqhdm", xzqhdm);
		vdo.put("orgds", gdxxds);
		return vdo;
	}
	/**
	 * 获取工单信息
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-23
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject getGdxx(String piid) throws Exception {
  		de.clearSql();
		DataObject vdo = DataObject.getInstance();
		DataStore gdxxds;

		de.clearSql();
  		de.addSql("select * 			 ");
  		de.addSql("  from odssuws.jgcx  ");
  		de.addSql(" where piid = :piid	   ");
		this.de.setString("piid", piid);
		gdxxds = this.de.query();

		vdo.put("gdxxds", gdxxds);
		return vdo;
	}
 
	/**
	 * 获取机构信息
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-23
	 * @param orgno
	 * @return
	 * @throws Exception
	 */
	public final DataObject getOrgxx(String orgno) throws Exception {
  		de.clearSql();
		DataObject vdo = DataObject.getInstance();
		DataStore orgds, typedstmp, belongnodstmp;
		String orgtype, belongorgno, typename = "", rankname = "", belongorgname = "";

		de.clearSql();
  		de.addSql("select * 			 ");
  		de.addSql("  from odssu.orginfor  ");
  		de.addSql(" where orgno = :orgno		 ");
  		de.addSql("   and sleepflag = '0' ");
		this.de.setString("orgno", orgno);
		orgds = this.de.query();
		if (orgds.rowCount() == 0) {
			throw new AppException("没有找到此机构的信息，请检查！");
		}

		orgtype = orgds.getString(0, "orgtype");
		if (!"".equals(orgtype) && orgtype != null) {
			de.clearSql();
  			de.addSql("select typename		 ");
  			de.addSql("  from odssu.org_type  ");
  			de.addSql(" where typeno = :orgtype		 ");
			this.de.setString("orgtype", orgtype);
			typedstmp = this.de.query();
			if (typedstmp.rowCount() > 0) {
				typename = typedstmp.getString(0, "typename");
			}
		}

		belongorgno = orgds.getString(0, "belongorgno");
		if (!"".equals(belongorgno) && belongorgno != null) {
			de.clearSql();
  			de.addSql("select orgname	 ");
  			de.addSql("  from odssu.orginfor  ");
  			de.addSql(" where orgno = :belongorgno and sleepflag = '0'	 ");
			this.de.setString("belongorgno", belongorgno);
			belongnodstmp = this.de.query();
			if (belongnodstmp.rowCount() > 0) {
				belongorgname = belongnodstmp.getString(0, "orgname");
			}
		}

		orgds.put(0, "typename", typename);
		orgds.put(0, "rankname", rankname);
		orgds.put(0, "belongorgname", belongorgname);
		vdo.put("orgds", orgds);

		return vdo;
	}

	/**
	 * 机构撤销申请保存
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-23
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveOrgDelApply(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();
		String piid, orgno;

		piid = para.getString("piid");
		orgno = para.getString("orgno");
    		de.clearSql();
  		de.addSql(" select sleepflag,createdate,orgname from odssu.orginfor ");
  		de.addSql(" where orgno = :orgno and sleepflag = '0' ");
		de.setString("orgno", orgno);
		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			throw new AppException("没有找到编号为【" + orgno + "】的机构的信息！");
		}
		// String orgname = vds.getString(0, "orgname");
		String sleepflag = vds.getString(0, "sleepflag");
		// Date createdate = vds.getDate(0, "createdate");
		// String cdstr = DateUtil.FormatDate(createdate, "yyMMdd");
		// String sdstr = DateUtil.FormatDate(new Date(), "yyMMdd");
		// String lzstr = "（" + cdstr + "-" + sdstr + "）";
		if ("1".equals(sleepflag)) {
			this.bizException("此机构已经被注销，无法再次注销！");
		}

		// 撤销机构前的各种检查
		de.clearSql();
  		de.addSql(" select 1 from odssu.ir_emp_org ");
  		de.addSql(" where orgno = :orgno ");
		de.setString("orgno", orgno);
		DataStore vds1 = de.query();
		if (vds1.rowCount() > 0) {
			this.bizException("该机构还有下属人员，无法注销！");
		}
//		str.setLength(0);
//		str.append(" select 1 from odssu.ir_emp_org_all_role ");
//		str.append(" where orgno = ? ");
//		sql.setSql(str.toString());
//		sql.setString(1, orgno);
//		DataStore vds2 = sql.executeQuery();
//		if (vds2.rowCount() > 0) {
//			this.bizException("还有与该机构有关系的人员，无法注销！");
//		}
		de.clearSql();
  		de.addSql(" select 1 from odssu.orginfor ");
  		de.addSql(" where belongorgno = :orgno and sleepflag = '0' ");
		de.setString("orgno", orgno);
		DataStore vds4 = de.query();
		if (vds4.rowCount() > 0) {
			this.bizException("该机构还有下级机构，无法注销！");
		}

//		str.setLength(0);
//		str.append(" select 1 from odssu.roleinfor ");
//		str.append(" where deforgno = ? ");
//		sql.setSql(str.toString());
//		sql.setString(1, orgno);
//		DataStore vds5 = sql.executeQuery();
//		if (vds5.rowCount() > 0) {
//			this.bizException("该机构还有定义的角色，无法注销！");
//		}

		// 更新工单操作人信息
		de.clearSql();
  		de.addSql(" update odssuws.jgcx ");
  		de.addSql("    set reviewer =null,reviewtime = null,spyj = null,spsm = null ");
  		de.addSql(" where piid = :piid ");
		this.de.setString("piid", piid);
		int result = this.de.update();

		if (result == 0) {
			throw new AppException("工单信息更新失败!");
		}

		return vdo;
	}

	/**
	 * 跳转到审批机构撤销界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-23
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageOrgDelApproval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore gdxxds = DataStore.getInstance(), dstmp, yjds = DataStore.getInstance();
  		de.clearSql();
		String piid, orgno = "";
		String spr = "", spyj = "", spsm = "";
		String sprq = null;
		// 流程开始获取piid
		piid = para.getString("piid");

		de.clearSql();
  		de.addSql("select * 			");
  		de.addSql("  from odssuws.jgcx  ");
  		de.addSql(" where piid = :piid	   ");
		this.de.setString("piid", piid);
		dstmp = this.de.query();

		if (dstmp.rowCount() > 0) {
			orgno = dstmp.getString(0, "orgno");
			spr = dstmp.getString(0, "reviewer");
			sprq = dstmp.getDateToString(0, "reviewtime", "yyyy-mm-dd");
			spyj = dstmp.getString(0, "spyj");
			spsm = dstmp.getString(0, "spsm");
		}
		yjds.put(0, "spyj", spyj);
		yjds.put(0, "spsm", spsm);
		yjds.put(0, "spr", spr);
		yjds.put(0, "sprq", sprq);

		DataObject mdo = getOrgxx(orgno);
		gdxxds = mdo.getDataStore("orgds");
		gdxxds.put(0, "piid", piid);

		vdo.put("orgds", gdxxds);
		vdo.put("yjds", yjds);
		return vdo;
	}
	/**
	 * 跳转到审批机构恢复界面
	 * 
	 * @Description:
	 * @author 能天宇
	 * @date 2016-9-6
	 */
	public final DataObject fwPageOrgRecoverApproval(DataObject para) throws Exception {
		DataStore dstmp, yjds = DataStore.getInstance();
  		de.clearSql();
		String spr = "", spyj = "", spsm = "";
		String sprq = null;
		
		String piid = para.getString("piid");
		if (piid == null || "".equals(piid)) {
			this.bizException("para中传入的piid为空！");
		}
		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from odssuws.jgjbxxxzwzb   ");
  		de.addSql(" where piid = :piid	   ");
		this.de.setString("piid", piid);
		dstmp = this.de.query();
		if(dstmp == null || dstmp.rowCount() == 0 ){
			throw new AppException("获取工单编号为【"+piid +"】的工单信息失败！");
		}
		if ( "1".equals(dstmp.getString(0, "spyj")) ||  "0".equals(dstmp.getString(0, "spyj")) ) {	
			spr = dstmp.getString(0, "reviewer");
			sprq = dstmp.getDateToString(0, "reviewtime", "yyyy-mm-dd");
			spyj = dstmp.getString(0, "spyj");
			spsm = dstmp.getString(0, "spsm");
		} 
		yjds.put(0, "spyj", spyj);
		yjds.put(0, "spsm", spsm);
		yjds.put(0, "spr", spr);
		yjds.put(0, "sprq", sprq);
		
		de.clearSql();
  		de.addSql("select *  ");
  		de.addSql("  from odssuws.jgjbxxxzwzb  ");
  		de.addSql(" where piid = :piid	   ");
		this.de.setString("piid", piid);
		DataStore gdxxds = this.de.query();
		if (gdxxds == null || gdxxds.rowCount() == 0 ) {
			throw new AppException("没有找到编号为【" + piid + "】的工单信息！");
		}
		gdxxds.put(0, "belongorgname", OdssuUtil.getOrgNameByOrgno(gdxxds.getString(0, "belongorgno")));
		gdxxds.put(0, "typename", OdssuUtil.getOrgTypeNameByTypeNo(gdxxds.getString(0, "orgtype")));
		DataObject vdo = DataObject.getInstance();
		vdo.put("orgds", gdxxds);
		vdo.put("yjds", yjds);
		return vdo;
	}

	/**
	 * 审批机构撤销暂存
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-23
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveOrgDelApproval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();
		String piid, spyj, spsm, spr;
		Date sprq;

		piid = para.getString("piid");
		spyj = para.getString("spyj");
		spsm = para.getString("spsm");
		spr = para.getString("spr");
		sprq = para.getDate("sprq");
		if (piid == null || piid.trim().isEmpty()) {
			throw new AppException("piid为空！");
		}
		// 保存审批意见
		de.clearSql();
  		de.addSql(" update odssuws.jgcx ");
  		de.addSql("    set spyj = :spyj  ,spsm = :spsm,reviewer = :spr,reviewtime  =:sprq   ");
  		de.addSql("  where piid = :piid 	         ");
		this.de.setString("spyj", spyj);
		this.de.setString("spsm", spsm);

		this.de.setString("spr", spr);
		this.de.setDateTime("sprq", sprq);
		this.de.setString("piid", piid);
		int result2 = this.de.update();

		if (result2 == 0) {
			throw new AppException("将审批意见更新到工单表中时出错，请联系开发人员！");
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
	 * 审批机构恢复暂存
	 * 
	 * @Description:
	 * @author 能天宇
	 * @date 2016-9-6
	 */
	public final DataObject saveOrgRecoverApproval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();
		String piid, spyj, spsm, spr;
		Date sprq;

		piid = para.getString("piid");
		spyj = para.getString("spyj");
		spsm = para.getString("spsm");
		spr = para.getString("spr");
		sprq = para.getDate("sprq");
		if (piid == null || piid.trim().isEmpty()) {
			throw new AppException("piid为空！");
		}
		if (spyj == null || piid.trim().isEmpty()) {
			this.bizException("审批意见为空！");
		}
		// 保存审批意见
		de.clearSql();
  		de.addSql(" update odssuws.jgjbxxxzwzb ");
  		de.addSql("    set spyj = :spyj  ,spsm = :spsm,reviewer = :spr,reviewtime  =:sprq   ");
  		de.addSql("  where piid = :piid  ");
		this.de.setString("spyj", spyj);
		this.de.setString("spsm", spsm);
		this.de.setString("spr", spr);
		this.de.setDateTime("sprq", sprq);
		this.de.setString("piid", piid);
		int result2 = this.de.update();

		if (result2 == 0) {
			throw new AppException("将审批意见更新到工单表中时出错，请联系开发人员！");
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

		ProcessUtil.setTEEVarByPiid(piid,"approve" , spyj);
		return vdo;
	}
}
