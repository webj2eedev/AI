package com.dw.odssu.ws.role.jsjbxxxz;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dareway.apps.process.ProcessBPO;
import com.dareway.apps.process.util.ProcessUtil;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

/**
 * 角色基本信息新增 类描述
 * 
 * @author liuy
 * @version 1.0 创建时间 2014-05-13
 */
public final class JsjbxxxzBPO extends BPO{

	/**
	 * 跳转到录入角色基本信息界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-18
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageRoleAdd(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), result = DataObject.getInstance(), rdo;
		DataStore gdxxds = DataStore.getInstance();
		String piid, orgno = "", typeno = "", typename = "";
		String userid = this.getUser().getUserid();
		Date sysdate = DateUtil.getDBTime();
		DE de = DE.getInstance();

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

			orgno = result.getString("orgno");
			// 创建工单表
			de.clearSql();
  			de.addSql("insert into odssuws.jsjbxxxz ");
  			de.addSql("  ( piid, deforgno, operator, operationtime ) ");
  			de.addSql(" values(:piid, :orgno, :userid, :sysdate ) ");
			de.setString("piid", piid);
			de.setString("orgno", orgno);
			de.setString("userid", userid);
			de.setDateTime("sysdate", sysdate);
			de.update();

			gdxxds.put(0, "piid", piid);
			vdo.put("roleds", gdxxds);
			vdo.put("orgno", orgno);

			return vdo;
		} else {
			orgno = gdxxds.getString(0, "deforgno");
		}
		if (gdxxds.rowCount() > 0) {
			typeno = gdxxds.getString(0, "roletype");
			typename = OdssuUtil.getRoleTypeNameByTypeNo(typeno);
			gdxxds.put(0, "typename", typename);

		}

		vdo.put("roleds", gdxxds);
		vdo.put("orgno", orgno);

		return vdo;
	}

	/**
	 * 查询工单信息 .
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-18
	 * @param piid
	 * @return
	 * @throws Exception
	 */
	public final DataObject getGdxx(String piid) throws Exception {
		DataObject rdo = DataObject.getInstance();
		DataStore gdxxds = DataStore.getInstance();
		DE de = DE.getInstance();

		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from odssuws.jsjbxxxz ");
  		de.addSql(" where piid=:piid ");
		de.setString("piid", piid);
		gdxxds = de.query();

		rdo.put("gdxxds", gdxxds);
		if(gdxxds.rowCount()>0){
			String isshowinorg=gdxxds.getString(0, "isshowinorg");
			boolean flag = false;
			if("1".equals(isshowinorg)){
				flag = true;
			}
			gdxxds.put(0, "isshowinorg", flag);
		}
		gdxxds = null;
		return rdo;
	}

	/**
	 * 选择角色类型
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-8-22
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject lovForRoleType(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();

		String typeno = para.getString("roletype");
		typeno = "%" + typeno + "%";

		de.clearSql();
  		de.addSql(" select typeno roletype,typename ");
  		de.addSql(" from odssu.role_type  ");
  		de.addSql(" where jsgn = '1' ");
  		de.addSql("  and (typeno like :typeno or typename like :typeno)  ");
		de.setString("typeno", typeno);
		DataStore vds1 = de.query();

		vdo.put("vds", vds1);
		return vdo;

	}

	/**
	 * 录入角色基本信息暂存方法
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-18
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveRoleAdd(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore namedstmp;
		String piid, rolename, displayname, rolenature, roletype, roletypename, isshowinorg, deforgno;
		DE de = DE.getInstance();

		piid = para.getString("piid");
		rolename = para.getString("rolename");
		displayname = para.getString("displayname");
		roletype = para.getString("roletype");
		rolenature = para.getString("rolenature");
		isshowinorg = para.getString("isshowinorg");
		if("false".equals(isshowinorg)){
           isshowinorg="0";			
		}else if("true".equals(isshowinorg)){
			isshowinorg="1";
		}else{
           throw new AppException("isshowinorg的值【"+isshowinorg+"】非法");			
		}
		
		deforgno = para.getString("deforgno");
		roletypename = para.getString("typename");

		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空！");
		}
		if (rolename == null || rolename.trim().isEmpty()) {
			this.bizException("角色名称为空！");
		}
		if (rolename.length()>25) {
			this.bizException("角色名称的长度过长，请重新输入！");
		}
		if (displayname == null || displayname.trim().isEmpty()) {
			this.bizException("角色展示名称为空！");
		}
		if (roletype == null || roletype.trim().isEmpty()) {
			this.bizException("角色类型为空！");
		}
		if (roletypename == null || roletypename.trim().isEmpty()) {
			this.bizException("角色类型名称为空！");
		}
		if (deforgno == null || deforgno.trim().isEmpty()) {
			this.bizException("角色定义机构为空！");
		}

		if (isshowinorg == null || "".equals(isshowinorg)) {
			isshowinorg = "0";
		}
    	de.addSql(" select typename from odssu.role_type where typeno = :roletype ");
		de.setString("roletype", roletype);

		DataStore typeds =de.query();
		if (typeds.rowCount() == 0) {
			this.bizException("角色类型【" + roletype + "】所对应的角色信息不存在！");
		}

		if (!roletypename.equals(typeds.getString(0, "typename"))) {
			this.bizException("角色编号与角色名称对应信息出错，请重新选择后再保存！");
		}

		// 判断角色名称是否唯一rolename
		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from odssu.roleinfor  ");
  		de.addSql(" where rolename = :rolename  ");
		de.setString("rolename", rolename);
		namedstmp = de.query();
		if (namedstmp.rowCount() > 0) {
			this.bizException("角色标识名称已存在,请检查！");
		}

		// 保存到工单表
		de.clearSql();
  		de.addSql(" update odssuws.jsjbxxxz ");
  		de.addSql("    set rolename =:rolename, displayname =:displayname, isshowinorg =:isshowinorg, roletype =:roletype ,rolenature =:rolenature ,   ");
  		de.addSql("        deforgno = :deforgno  , spyj = null , spsm = null , reviewer = null , reviewtime = null ");
  		de.addSql(" where piid = :piid ");
		de.setString("rolename", rolename);
		de.setString("displayname", displayname);
		de.setString("isshowinorg", isshowinorg);
		de.setString("roletype", roletype);
		de.setString("rolenature", rolenature);
		de.setString("deforgno", deforgno);
		de.setString("piid", piid);
		int result = de.update();

		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}
		
		ProcessUtil.setTEEVarByPiid(piid, "rolename", rolename);

		return vdo;
	}

	/**
	 * 跳转到确定角色编号界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-18
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageEsRoleno(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore orginfods = DataStore.getInstance(), dstmp, orgtypeds;
		DE de = DE.getInstance();
		String roleno = "", typeno = "";
		String allowletter, mes = "";
		int minlength, maxlength;
		String piid = para.getString("piid");

		de.clearSql();
  		de.addSql("select *	");
  		de.addSql("  from odssuws.jsjbxxxz  ");
  		de.addSql(" where piid = :piid		  ");
		de.setString("piid", piid);
		dstmp = de.query();
		if (dstmp.rowCount() > 0) {
			roleno = dstmp.getString(0, "roleno");
			typeno = dstmp.getString(0, "roletype");
		}
		orginfods.put(0, "roleno", roleno);
		orginfods.put(0, "piid", piid);

		de.clearSql();
  		de.addSql("select *	");
  		de.addSql("  from odssu.role_type   ");
  		de.addSql(" where typeno = :typeno	   ");
		de.setString("typeno", typeno);
		orgtypeds = de.query();
		if (orgtypeds.rowCount() <= 0) {
			throw new BusinessException("没找到类型编号为" + typeno + "的类型信息！");
		}
		minlength = orgtypeds.getInt(0, "minlength");// 直接返回
		maxlength = orgtypeds.getInt(0, "maxlength");// 直接返回
		allowletter = orgtypeds.getString(0, "allowletter");

		mes = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;● 角色编号组成:<br/><br/>";
		mes = mes
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;① 角色编号允许含有数字<br/><br/>";
		if ("0".equals(allowletter)) {
			mes = mes
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;② 角色编号不允许含有字母<br/><br/>";
		} else {
			mes = mes
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;② 角色编号允许含有字母<br/><br/>";
		}

		
		mes = mes+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;③ 角色编号不允许含有下划线<br/><br/>";
		
		mes = mes
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;④角色编号不允许含有其他特殊字符<br/><br/>";

		if (minlength == maxlength) {
			mes = mes
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;● 角色编号长度为"
					+ minlength + "位;<br/><br/>";
		} else {
			mes = mes
					+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;● 角色编号长度为"
					+ minlength + "~" + maxlength + "位<br/><br/>";
		}

		vdo.put("mes", mes);
		vdo.put("minlength", minlength);
		vdo.put("maxlength", maxlength);
		vdo.put("roleinfods", orginfods);
		return vdo;
	}

	/**
	 * 保存确定的角色编号
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-18
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveRoleno(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String piid, roleno, allowletter;
		DE de = DE.getInstance();
		DataStore roletypeds;
		String  typeno = "";
		int minlength, maxlength;

		piid = para.getString("piid");
		roleno = para.getString("roleno");

		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空！");
		}

		if (roleno == null || roleno.trim().isEmpty()) {
			this.bizException("角色编号为空！");
		}
		
		if(roleno.startsWith("_")){
			this.bizException("角色编号不能以下划线开头");
		}
		// 判断参数合法性
		if (OdssuUtil.isRoleExist(roleno)) {
			this.bizException("编号【" + roleno + "】已存在！无法再次新增");
		}
		de.clearSql();
  		de.addSql("select *	");
  		de.addSql("  from odssuws.jsjbxxxz  ");
  		de.addSql(" where piid = :piid		  ");
		de.setString("piid", piid);
		DataStore dstmp = de.query();
		if (dstmp.rowCount() > 0) {
			typeno = dstmp.getString(0, "roletype");
		}

		de.clearSql();
  		de.addSql(" select *	");
  		de.addSql("  from odssu.role_type   ");
  		de.addSql(" where typeno = :typeno	   ");
		de.setString("typeno", typeno);
		roletypeds = de.query();
		if (roletypeds.rowCount() <= 0) {
			throw new BusinessException("没找到类型编号为" + typeno + "的类型信息！");
		}
		minlength = roletypeds.getInt(0, "minlength");// 直接返回
		maxlength = roletypeds.getInt(0, "maxlength");// 直接返回
		allowletter = roletypeds.getString(0, "allowletter");

		if (!"[自动生成]".equals(roleno)) {
			if (roleno.length() >= minlength && roleno.length() <= maxlength) {
				//
			} else {
				this.bizException("角色编号为" + roleno + "的长度不符合要求，请检查！");
			}

			// 判断是否只含有数字、字母、下划线
			if (roleno.matches("[0-9A-Za-z_]*") == false) {
				this.bizException("角色编号中含有除数字、字母、下划线之外的其他字符，保存失败！");
			}

			// 判断机构编号字母、下划线是否合法
			if ("0".equals(allowletter)) {
				Pattern p = Pattern.compile("[a-zA-Z]+");
				Matcher m = p.matcher(roleno);
				if (m.matches() == true) {
					this.bizException("该角色类型决定的机构编号中不能含有字母，请检查！");
				}
			}
			
			if(roleno.startsWith("_")){
				this.bizException("自建角色的角色编号不能以下划线开头");
			}

		}

		// 保存到工单表
		de.clearSql();
  		de.addSql(" update odssuws.jsjbxxxz ");
  		de.addSql("    set roleno = :roleno, reviewer = null,reviewtime = null,spyj = null,spsm = null      ");
  		de.addSql("  where piid = :piid ");
		de.setString("roleno", roleno);
		de.setString("piid", piid);
		int result = de.update();

		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}
		
		ProcessUtil.setTEEVarByPiid(piid, "roleno", roleno);

		return vdo;
	}

	/**
	 * 跳转到角色基本信息新增审批界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-18
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageRoleAddApproval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore roleds;
		String piid, typeno;
		String typename = "";
		DE de = DE.getInstance();

		piid = para.getString("piid");

		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from odssuws.jsjbxxxz  ");
  		de.addSql(" where piid = :piid		  ");
		de.setString("piid", piid);
		roleds = de.query();

		DataStore gdds = getGdxx(piid).getDataStore("gdxxds");
		DataStore yjds = DataStore.getInstance();
		yjds.put(0, "spr", gdds.getString(0, "reviewer"));
		yjds.put(0, "spyj", gdds.getString(0, "spyj"));
		yjds.put(0, "spsm", gdds.getString(0, "spsm"));
		yjds.put(0, "sprq", gdds.getDate(0, "reviewtime"));

		if (roleds.rowCount() > 0) {
			typeno = roleds.getString(0, "roletype");
			typename = OdssuUtil.getRoleTypeNameByTypeNo(typeno);
			roleds.put(0, "typename", typename);

		}
		vdo.put("yjds", yjds);
		vdo.put("roleds", roleds);

		return vdo;
	}

	/**
	 * 角色基本信息新增审批暂存
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-18
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveRoleAddApproval(final DataObject para) throws Exception {
		DataObject rdo = DataObject.getInstance();
		DE de = DE.getInstance();
		String piid, spr, spyj, spsm;
		Date sprq;
		DataStore vds;

		// 接受参数
		piid = para.getString("piid");
		spr = para.getString("spr");
		spyj = para.getString("spyj");
		spsm = para.getString("spsm");
		sprq = para.getDate("sprq");

		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql("   from odssuws.jsjbxxxz  ");
  		de.addSql("  where piid = :piid  ");
		de.setString("piid", piid);
		vds = de.query();

		if (vds.rowCount() < 1) {
			this.bizException("未取到工单信息" + piid);
		}

		// para.put("_user", CurrentUser.getInstance());
		// this.executeBKO(JsjbxxxzBKO.class.getName(), "saveRoleAddApproval",
		// para);

		// 工单记录记账标识
		de.clearSql();
  		de.addSql(" update odssuws.jsjbxxxz  ");
  		de.addSql("    set reviewer = :spr, reviewtime =:sprq , spyj = :spyj , spsm = :spsm ");
  		de.addSql("  where piid = :piid  ");
		de.setString("spr", spr);
		de.setDateTime("sprq", sprq);
		de.setString("spyj", spyj);
		de.setString("spsm", spsm);
		de.setString("piid", piid);
		de.update();

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
		
		vds = null;
		return rdo;
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
		String flag = "false";

		rolename = paras.getString("rolename");

		if (rolename == null) {
			this.bizException("角色名称为空！！");
		}
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" select * from odssu.roleinfor ");
  		de.addSql("  where  rolename = :rolename ");
		de.setString("rolename", rolename);
		DataStore roleds = de.query();

		if (roleds.rowCount() == 0) {
			flag = "true";
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("flag", flag);

		return vdo;
	}
}
