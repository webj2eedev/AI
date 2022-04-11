package com.dw.odssu.ws.role.jscx;

import java.util.Date;

import com.dareway.apps.process.ProcessBPO;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;
import com.dareway.framework.dbengine.DE;


/**
 * 机构撤銷 类描述
 * 
 * @author liuy
 * @version 1.0 创建时间 2014-05-13
 */
public final class JscxBPO extends BPO{

	public final DataObject fwPageRoleDelApply(DataObject para) throws Exception {
		// 定义变量
		DataObject vdo = DataObject.getInstance(), result = DataObject.getInstance(), rdo;
		DataStore gdxxds = DataStore.getInstance(), dstmp;
		DE de = DE.getInstance();
		String piid, roleno;
		// 流程开始获取piid
		piid = para.getString("piid");
		String userid = this.getUser().getUserid();
		Date sysdate = DateUtil.getDBTime();

		// 查询工单信息
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");

		// 如果无工单，创建工单
		if (gdxxds.rowCount() == 0) {
			para.put("piid", piid);
			BPO ibpo = this.newBPO(ProcessBPO.class);
			result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
			roleno = result.getString("roleno");

			DataObject mdo = getRolexx(roleno);// 获取角色信息
			gdxxds = mdo.getDataStore("roleds");
			gdxxds.put(0, "piid", piid);

			// 创建工单表
			de.clearSql();
  			de.addSql("insert into odssuws.jscx ");
  			de.addSql("  ( piid, roleno, operator, operationtime ) 	");
  			de.addSql(" values(:piid, :roleno, :userid, :sysdate) 		");
			de.setString("piid", piid);
			de.setString("roleno", roleno);
			de.setString("userid", userid);
			de.setDateTime("sysdate", sysdate);
			de.update();

			gdxxds.put(0, "piid", piid);
			vdo.put("roleds", gdxxds);
			return vdo;
		}

		// 从工单表中查出角色编号信息
		de.clearSql();
  		de.addSql("select * 			 ");
  		de.addSql("  from odssuws.jscx  ");
  		de.addSql(" where piid = :piid	   ");
		de.setString("piid", piid);
		dstmp = de.query();
		if(dstmp == null || dstmp.rowCount() == 0){
			this.bizException("获取工单编号为【" + piid + "】的工单相关信息时出错！");
		}
		roleno = dstmp.getString(0, "roleno");

		// 根据角色编号获取角色基本信息
		DataObject mdo = getRolexx(roleno);
		gdxxds = mdo.getDataStore("roleds");
		// 将流程实例ID放入角色信息表中，并返回
		gdxxds.put(0, "piid", piid);

		vdo.put("roleds", gdxxds);
		return vdo;
	}

	/**
	 * 查询工单信息 .
	 */
	public final DataObject getGdxx(String piid) throws Exception {
		DE de = DE.getInstance();

		DataObject vdo = DataObject.getInstance();
		DataStore gdxxds;

		de.clearSql();
  		de.addSql("select * 			 ");
  		de.addSql("  from odssuws.jscx  ");
  		de.addSql(" where piid = :piid	   ");
		de.setString("piid", piid);
		gdxxds = de.query();

		vdo.put("gdxxds", gdxxds);
		return vdo;
	}

	/**
	 * 方法简介：查询角色信息
	 * 
	 * @author liuy
	 * @date 创建时间 2015年8月20日
	 */
	public final DataObject getRolexx(String roleno) throws Exception {
		DE de = DE.getInstance();
		DataObject vdo = DataObject.getInstance();
		DataStore roleds;
		String roletype, typename = "", deforgno, deforgname = "";

		de.clearSql();
  		de.addSql("select * 			 ");
  		de.addSql("  from odssu.roleinfor  ");
  		de.addSql(" where roleno = :roleno		 ");
  		de.addSql("   and sleepflag = '0' ");
		de.setString("roleno", roleno);
		roleds = de.query();
		if (roleds.rowCount() == 0) {
			throw new BusinessException("没有找到此角色的信息，请检查！");
		} else if (roleds.rowCount() == 1) {
			roletype = roleds.getString(0, "roletype");
			deforgno = roleds.getString(0, "deforgno");

			if (!"".equals(roletype) && roletype != null) {
				typename = OdssuUtil.getRoleTypeNameByTypeNo(roletype);
			}

			if (!"".equals(deforgno) && deforgno != null) {
				deforgname = OdssuUtil.getOrgNameByOrgno(deforgno);
			}

			roleds.put(0, "typename", typename);
			roleds.put(0, "deforgname", deforgname);

		}

		vdo.put("roleds", roleds);

		return vdo;
	}

	/**
	 * 注销角色申请
	 * @author liuy
	 * @version 1.0 创建时间 2014-05-13
	 */
	public final DataObject saveRoleDelApply(DataObject para) throws Exception {
		// 定义变量
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		String piid, roleno;

		// 从前台传来的参数中获取流程实例ID、角色编号
		piid = para.getString("piid");
		roleno = para.getString("roleno");

		// 检查是否有该角色信息

		de.clearSql();
		de.clearSql();
  		de.addSql(" select 1 from odssu.roleinfor ");
  		de.addSql(" where roleno = :roleno ");
		de.setString("roleno", roleno);
		DataStore vds = de.query();
		if (vds.rowCount() == 0) {
			this.bizException("没有找到编号为【" + roleno + "】的角色信息！");
		}

		// 检查角色是否已经注销
		de.clearSql();
  		de.addSql(" select 1 from odssu.roleinfor ");
  		de.addSql(" where roleno = :roleno ");
  		de.addSql("   and sleepflag = '1' ");
		de.setString("roleno", roleno);
		vds = de.query();
		if (vds.rowCount() > 0) {
			this.bizException("编号为【" + roleno + "】的角色已经被撤销！");
		}

		// 更新工单操作人信息
		de.clearSql();
  		de.addSql(" update odssuws.jscx ");
  		de.addSql("    set reviewer = null , reviewtime = null , spyj = null , spsm = null ");
  		de.addSql(" where piid = :piid ");
		de.setString("piid", piid);
		int result = de.update();

		if (result == 0) {
			throw new Exception("工单信息更新失败!");
		}

		return vdo;
	}

	/**
	 * 跳转到审批界面
	 * 
	 * @author liuy
	 * @version 1.0 创建时间 2014-05-13
	 */
	public final DataObject fwPageRoleDelApproval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore gdxxds = DataStore.getInstance(), dstmp;
		DE de = DE.getInstance();
		String piid, roleno;
		// 流程开始获取piid
		piid = para.getString("piid");
		
		//获取工单表基本信息
		DataStore yjds = getGdxx(piid).getDataStore("gdxxds");

		yjds.put(0, "spr", yjds.getString(0, "reviewer"));
		yjds.put(0, "sprq", yjds.getDate(0, "reviewtime"));

		de.clearSql();
  		de.addSql("select * 			");
  		de.addSql("  from odssuws.jscx  ");
  		de.addSql(" where piid = :piid	   ");
		de.setString("piid", piid);
		dstmp = de.query();
		if(dstmp == null || dstmp.rowCount() == 0){
			this.bizException("获取工单编号为【" + piid + "】的工单相关信息时出错！");
		}
		roleno = dstmp.getString(0, "roleno");

		DataObject mdo = getRolexx(roleno);
		gdxxds = mdo.getDataStore("roleds");
		gdxxds.put(0, "piid", piid);

		vdo.put("yjds", yjds);
		vdo.put("roleds", gdxxds);
		return vdo;
	}

	/*
	 * 保存审批意见
	 * @author liuy
	 * @version 1.0 创建时间 2014-05-13
	 */
	public final DataObject saveRoleDelApproval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		String piid, spr, spyj, spsm;
		Date sprq;

		//获取基本数据流程实例ID、审批人、审批意见、审批说明、审批日期
		piid = para.getString("piid");
		spr = para.getString("spr");
		spyj = para.getString("spyj");
		spsm = para.getString("spsm");
		sprq = para.getDate("sprq");

		//判断审批信息是否为空
		if (piid == null || "".equals(piid.trim())) {
			this.bizException("piid为空");
		}
		if (spr == null || "".equals(spr.trim())) {
			this.bizException("审批人信息为空！");
		}
		if (spyj == null || "".equals(spyj.trim())) {
			this.bizException("审批意见为空！");
		}
		if (sprq == null) {
			this.bizException("审批日期为空！");
		}

		// para.put("_user", CurrentUser.getInstance());
		// this.executeBKO(JscxBKO.class.getName(), "saveRoleDelApproval",
		// para);
		//
		// 更新工单操作人信息
		de.clearSql();
  		de.addSql(" update  odssuws.jscx ");
  		de.addSql("    set  reviewer = :spr ,reviewtime = :sprq , spyj = :spyj , spsm = :spsm ");
  		de.addSql("  where  piid = :piid ");
		de.setString("spr", spr);
		de.setDateTime("sprq", sprq);
		de.setString("spyj", spyj);
		de.setString("spsm", spsm);
		de.setString("piid", piid);
		int result = de.update();

		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}
		
		// 保存一条公共审批
		de.clearSql();
  		de.addSql("delete from odssuws.spinfor ");
  		de.addSql("  where piid = :piid and splbdm = :splbdm");
		de.setString("piid", piid);
		de.setString("splbdm", "rylz");
		de.update();
		
		String spyjdm = "pass";
		if (spyj.equals("1")) {
			spyjdm = "reject";
		}else if (spyj.equals("2")) {
			spyjdm = "revise";
		}
		
		de.clearSql();
  		de.addSql("insert into odssuws.spinfor (piid,splbdm,spyjdm,spr,spsj,spsm)");
  		de.addSql("  values (:piid,:para2,:spyjdm,:para4,:sprq,:spsm)");
		de.setString("piid", piid);
		de.setString("para2", "rylz");
		de.setString("spyjdm", spyjdm);
		de.setString("para4", this.getUser().getUserid());
		de.setDateTime("sprq", sprq);
		de.setString("spsm", spsm);
		de.update();

		return vdo;
	}

}
