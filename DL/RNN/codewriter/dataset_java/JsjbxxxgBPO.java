package com.dw.odssu.ws.role.jsjbxxxg;

import java.util.Date;

import com.dareway.apps.process.ProcessBPO;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

/**
 * 机构基本信息修改 类描述
 * 
 * @author liuy
 * @version 1.0 创建时间 2014-05-13
 */
public final class JsjbxxxgBPO extends BPO{
	/**
	 * 查询工单信息 .
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	public final DataObject fwPageRoleMsgAdjust(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), result = DataObject.getInstance(), rdo;
		DataStore gdxxds = DataStore.getInstance();
		DE de = DE.getInstance();
		String piid, roleno;

		// 流程开始获取piid
		piid = para.getString("piid");

		// 查询工单信息
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");

		// 如果无工单，创建工单
		if (gdxxds.rowCount() == 0) {

			para.put("piid", piid);
			BPO ibpo = this.newBPO(ProcessBPO.class);
			result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);

			roleno = result.getString("roleno");

			// 查询角色信息
			de.clearSql();
  			de.addSql("select  roleno , rolename , displayname , rolenature , isshowinorg   ");
  			de.addSql("  from  odssu.roleinfor ");
  			de.addSql(" where  roleno=:roleno ");
			de.setString("roleno", roleno);
			DataStore roleds = de.query();

			if (roleds.rowCount() == 0) {
				this.bizException("没有找到编号为【" + roleno + "】的角色的信息");
			}

			String rolename = roleds.getString(0, "rolename");
			String displayname = roleds.getString(0, "displayname");
			String rolenature = roleds.getString(0, "rolenature");
			String isshowinorg = roleds.getString(0, "isshowinorg");
			// 向工单表插入数据

			de.clearSql();
  			de.addSql("insert into odssuws.jsjbxxxg ");
  			de.addSql("  ( piid, roleno, oldrolename, newrolename, oldrolenature, newrolenature, ");
  			de.addSql("                  olddisplayname, newdisplayname, oldisshowinorg, newisshowinorg  ) ");
  			de.addSql(" values(:piid, :roleno, :rolename, :rolename, :rolenature, :rolenature, :displayname, :displayname ,:isshowinorg, :isshowinorg ) ");
			de.setString("piid", piid);
			de.setString("roleno", roleno);
			de.setString("rolename", rolename);
			de.setString("rolenature", rolenature);
			de.setString("displayname", displayname);
			de.setString("isshowinorg", isshowinorg);
			de.update();

			roleds.put(0, "piid", piid);
			roleds.put(0, "staticrolename", rolename);
			
			boolean flag=true;
			if("0".equals(isshowinorg)){
				flag=false;
			}
			roleds.put(0, "isshowinorg", flag);
			vdo.put("roleds", roleds);
			vdo.put("roleno", roleno);

			return vdo;

		} else {
			gdxxds.put(0, "staticrolename", gdxxds.getString(0, "oldrolename"));
			gdxxds.put(0, "rolename", gdxxds.getString(0, "newrolename"));
			gdxxds.put(0, "displayname", gdxxds.getString(0, "newdisplayname"));
			gdxxds.put(0, "rolenature", gdxxds.getString(0, "newrolenature"));
			String newisshowinorg=gdxxds.getString(0, "newisshowinorg");
			boolean flag = true;
			if("0".equals(newisshowinorg)){
				flag=false;
			}
			
			gdxxds.put(0, "isshowinorg", flag);
			vdo.put("roleds", gdxxds);
			
			return vdo;
		}

	}

	/**
	 * 查询工单信息 .
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	public final DataObject getGdxx(String piid) throws Exception {
		DataObject rdo = DataObject.getInstance();
		DataStore gdxxds = DataStore.getInstance();
		DE de = DE.getInstance();


		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from odssuws.jsjbxxxg ");
  		de.addSql(" where piid=:piid ");
		de.setString("piid", piid);
		gdxxds = de.query();
		
		rdo.put("gdxxds", gdxxds);
		gdxxds = null;

		return rdo;
	}

	/*
	 * 保存修改的角色信息
	 * @author liuy
	 * @version 1.0 创建时间 2014-05-13
	 */
	public final DataObject saveRoleMsgAdjust(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		String piid, rolename, displayname, rolenature, isshowinorg, roleno;

		piid = para.getString("piid");
		roleno = para.getString("roleno");
		rolename = para.getString("rolename");
		rolenature = para.getString("rolenature");
		displayname = para.getString("displayname");
		isshowinorg = para.getString("isshowinorg");

		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空!");
		}
		if (roleno == null || roleno.trim().isEmpty()) {
			this.bizException("角色编号为空！");
		}
		if (rolename == null || rolename.trim().isEmpty()) {
			this.bizException("角色姓名为空！");
		}
		if (rolenature == null || rolenature.trim().isEmpty()) {
			this.bizException("角色性质为空！");
		}
		if (displayname == null || displayname.trim().isEmpty()) {
			this.bizException("角色展示姓名为空！");
		}
		
		if("true".equals(isshowinorg)){
			isshowinorg="1";
		}else if("false".equals(isshowinorg)){
			isshowinorg="0";
		}else{
			throw new AppException("isshowinorg的值【"+isshowinorg+"】非法");
		}
    	de.addSql(" select * from odssu.roleinfor where roleno  = :roleno ");
		de.setString("roleno", roleno);

		DataStore oldRoleds = de.query();

		if (oldRoleds.rowCount() == 0) {
			this.bizException("没有找到编号为【" + roleno + "】的角色的信息！");
		}

		de.clearSql();
  		de.addSql(" update  odssuws.jsjbxxxg ");
  		de.addSql("    set newrolename = :rolename , newrolenature = :rolenature , ");
  		de.addSql("        newdisplayname = :displayname , newisshowinorg = :isshowinorg , ");
  		de.addSql("        reviewer = null, reviewtime = null , spyj = null , spsm = null ");
  		de.addSql("  where  piid = :piid ");
		de.setString("rolename", rolename);
		de.setString("rolenature", rolenature);
		de.setString("displayname", displayname);
		de.setString("isshowinorg", isshowinorg);
		de.setString("piid", piid);

		int result = de.update();

		if (result == 0) {
			this.bizException("工单更新失败");
		}

		return vdo;
	}

	/**
	 * 第二个节点跳转页面
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	public final DataObject fwPageRoleAdjustApproval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), rdo;
		DataStore gdxxds = DataStore.getInstance(), xgds = DataStore.getInstance();
		String piid;
		// 流程开始获取piid
		piid = para.getString("piid");

		// 查询工单信息
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");

		if (gdxxds.rowCount() == 0) {
			this.bizException("没有找到编号为【" + piid + "】的流程的工单信息！");
		}

		String roleno = gdxxds.getString(0, "roleno");

		String oldrolename = gdxxds.getString(0, "oldrolename");
		String newrolename = gdxxds.getString(0, "newrolename");

		String olddisplayname = gdxxds.getString(0, "olddisplayname");
		String newdisplayname = gdxxds.getString(0, "newdisplayname");

		String oldrolenature = gdxxds.getString(0, "oldrolenature");
		String newrolenature = gdxxds.getString(0, "newrolenature");

		String oldisshowinorg = gdxxds.getString(0, "oldisshowinorg");
		String newisshowinorg = gdxxds.getString(0, "newisshowinorg");

		String spr = gdxxds.getString(0, "reviewer");
		String sprq = gdxxds.getDateToString(0, "reviewtime", "yyyy-mm-dd");
		String spyj = gdxxds.getString(0, "spyj");
		String spsm = gdxxds.getString(0, "spsm");

		DataStore yjds = DataStore.getInstance();
		yjds.put(0, "spyj", spyj);
		yjds.put(0, "spsm", spsm);
		yjds.put(0, "spr", spr);
		yjds.put(0, "sprq", sprq);

		//将修改项保存
		if (!oldrolename.equals(newrolename)) {
			xgds.put(xgds.rowCount(), "xgx", "角色标识名称");
			xgds.put(xgds.rowCount() - 1, "yz", oldrolename);
			xgds.put(xgds.rowCount() - 1, "xz", newrolename);
		}
		if (!olddisplayname.equals(newdisplayname)) {
			xgds.put(xgds.rowCount(), "xgx", "角色简称");
			xgds.put(xgds.rowCount() - 1, "yz", olddisplayname);
			xgds.put(xgds.rowCount() - 1, "xz", newdisplayname);
		}
		if (!oldrolenature.equals(newrolenature)) {
			oldrolenature = OdssuUtil.getCodeNameByCode(oldrolenature, "ROLENATURE");
			newrolenature = OdssuUtil.getCodeNameByCode(newrolenature, "ROLENATURE");
			xgds.put(xgds.rowCount(), "xgx", "角色性质");
			xgds.put(xgds.rowCount() - 1, "yz", oldrolenature);
			xgds.put(xgds.rowCount() - 1, "xz", newrolenature);
		}
		if (!oldisshowinorg.equals(newisshowinorg)) {
			oldisshowinorg = OdssuUtil.getCodeNameByCode(oldisshowinorg, "BZ");
			newisshowinorg = OdssuUtil.getCodeNameByCode(newisshowinorg, "BZ");
			xgds.put(xgds.rowCount(), "xgx", "名片中是否展示");
			xgds.put(xgds.rowCount() - 1, "yz", oldisshowinorg);
			xgds.put(xgds.rowCount() - 1, "xz", newisshowinorg);
		}
		DE de = DE.getInstance();
		de.clearSql();
    	de.addSql(" select * from odssu.roleinfor ");
  		de.addSql("  where roleno = :roleno ");
		de.setString("roleno", roleno);

		DataStore roleds = de.query();

		if (roleds.rowCount() == 0) {
			this.bizException("没有找到编号为【" + roleno + "】的角色信息！");
		}

		roleds.put(0, "piid", piid);
		vdo.put("roleds", roleds);
		vdo.put("xgds", xgds);
		vdo.put("yjds", yjds);

		return vdo;
	}

	/*
	 * 保存修改的角色信息--保存审批意见
	 * @author liuy
	 * @version 1.0 创建时间 2014-05-13
	 */
	public final DataObject saveRoleAdjustApproval(DataObject para) throws Exception {

		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
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
		if (spyj == null || spyj.trim().isEmpty()) {
			this.bizException("审批意见为空！");
		}
		// 保存审批意见
		de.clearSql();
  		de.addSql(" update odssuws.jsjbxxxg ");
  		de.addSql("    set spyj = :spyj  ,spsm = :spsm,reviewer = :spr,reviewtime  =:sprq   ");
  		de.addSql("  where piid = :piid 	         ");
		de.setString("spyj", spyj);
		de.setString("spsm", spsm);

		de.setString("spr", spr);
		de.setDateTime("sprq", sprq);
		de.setString("piid", piid);
		int result = de.update();

		if (result == 0) {
			this.bizException("将审批意见更新到工单表中时出错，请联系开发人员！");
		}
		return vdo;
	}

	/**
	 * 检测角色名称是否可用
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-14
	 * @param paras
	 * @return
	 * @throws Exception
	 */
	public final DataObject checkRoleName(DataObject paras) throws Exception {

		String rolename;
		String roleno;
		String flag = "false";

		rolename = paras.getString("rolename");
		roleno = paras.getString("roleno");

		if (rolename == null) {
			this.bizException("角色名称为空，请重新输入！！");
		}
		if (roleno == null) {
			this.bizException("角色编号为空，检查代码！！");
		}
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" select * from odssu.roleinfor ");
  		de.addSql("  where  rolename = :rolename and roleno != :roleno ");
		de.setString("rolename", rolename);
		de.setString("roleno", roleno);
		DataStore roleds = de.query();

		if (roleds.rowCount() == 0) {
			flag = "true";
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("flag", flag);

		return vdo;
	}
}
