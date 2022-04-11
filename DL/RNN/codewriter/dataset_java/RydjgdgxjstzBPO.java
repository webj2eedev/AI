package com.dw.odssu.ws.emp.rydjgdgxjstz;

import java.util.Date;

import com.dareway.apps.process.ProcessBPO;
import com.dareway.apps.process.util.ProcessUtil;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.ASOException;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.taglib.lanePrompt.LanePromptUtil;
import com.dareway.framework.util.CurrentUser;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.util.StringUtil;
import com.dareway.framework.workFlow.ASO;
import com.dareway.framework.workFlow.BPO;
import com.dw.odssu.acc.emp.ryjbxxxg.aso.EmpEditASO;
import com.dw.util.OdssuUtil;
import com.dw.util.multiSortUtil.MultiSortUtil;

/**
 * 人员对机构的干系角色调整 类描述
 * 
 * @author liuy
 * @version 1.0 创建时间 2014-05-22
 */
public final class RydjgdgxjstzBPO extends BPO{
	ASO i_testASO = newASO(EmpEditASO.class);
	
	/**
	 * 人员对机构的干系角色调整申请
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-8-19
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageGxRoleAdjust(DataObject para) throws Exception {
		String piid = para.getString("piid");
		String userid = this.getUser().getUserid();
		Date sysdate = DateUtil.getDBTime();

		DataObject pdo = DataObject.getInstance();
		pdo.put("piid", piid);
		BPO ibpo = this.newBPO(ProcessBPO.class);
		DataObject result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);

		String empno = result.getString("empno");
    		de.clearSql();
  		de.addSql(" select 1 from odssuws.rydjgdgxjstz where piid = :piid ");
		de.setString("piid", piid);
		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			// 这是第一次开启流程
			de.clearSql();
  			de.addSql(" insert into odssuws.rydjgdgxjstz ");
  			de.addSql("             (piid, empno, operator, operationtime) ");
  			de.addSql("      values (:piid, :empno, :userid, :sysdate) ");
			de.setString("piid", piid);
			de.setString("empno", empno);
			de.setString("userid", userid);
			de.setDateTime("sysdate", sysdate);
			de.update();
		}

		// 到此处，开启流程其实已经做完，剩下的工作其实是组在第一个任务节点展示的内容。
		DataObject vdo = queryResult(para);
		return vdo;
	}
	/**
	 * 方法简介： 进入到基于角色选择机构的 调整模式
	 *         进入到 选择要调整的干系角色
	 * 郑海杰  2016-1-18
	 */
	public final DataObject fwPageGxRoleAdjustXzytzdjs(DataObject para) throws AppException{
		String piid = para.getString("piid");
  		de.clearSql();
  		de.addSql(" select a.empno,b.empname,loginname username ");
  		de.addSql("   from odssuws.rydjgdgxjstz a, ");
  		de.addSql("        odssu.empinfor b ");
  		de.addSql("  where a.piid = :piid ");
  		de.addSql("    and a.empno = b.empno ");
		this.de.setString("piid", piid);
		DataStore empVds = this.de.query();
		empVds.put(0, "piid", piid);
		DataObject result = DataObject.getInstance();
		result.put("empds", empVds);
		return result;
	}
	/**
	 * 方法简介：转向选择干系角色的Lov页面
	 * 郑海杰  2016-1-18
	 */
	public final DataObject fwGxRoleAdjustXzytzdgxjsLov(DataObject para) throws AppException{
		String roleno = para.getString("roleno");
  		de.clearSql();
  		de.addSql(" select sleepflag,roleno,rolename,displayname ");
  		de.addSql("   from odssu.roleinfor ");
  		de.addSql("  where (roleno like :roleno or rolename like :rolename ");
  		de.addSql("     or upper(rolenamepy) like :para3  ");
  		de.addSql("     or displayname like :displayname ");
  		de.addSql("     or upper(displaynamepy) like :para5) ");
  		de.addSql("    and rolenature = '5' ");
  		de.addSql("    and jsgn in ('1', '2') ");
  		de.addSql("    and sleepflag = '0' ");
  		de.addSql(" order by roleno ");
		this.de.setString("roleno", "%" + roleno + "%");
		this.de.setString("rolename", "%" + roleno + "%");
		this.de.setString("para3", "%" + roleno.toUpperCase() + "%");
		this.de.setString("displayname", "%" + roleno + "%");
		this.de.setString("para5", "%" + roleno.toLowerCase() + "%");
		DataStore roleVds = this.de.query();
		DataObject result = DataObject.getInstance();
		result.put("roleds", roleVds);
		return result;
	}
	/**
	 * 描述：获取干系角色信息烟台客户化-->只有人事隶属为市直的人可以选择ods自身的干系角色信息
	 * author: sjn
	 * date: 2018年6月22日
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public final DataObject fwGxRoleAdjustXzytzdgxjsLov_3706(DataObject para) throws AppException{
		String roleno = para.getString("roleno");
  		de.clearSql();
		// 判断当前操作员人事隶属是否为市直
		de.clearSql();
  		de.addSql("select 1 ");
  		de.addSql("  from odssu.empinfor a, odssu.ir_org_closure b, odssu.orginfor c ");
  		de.addSql(" where a.hrbelong = b.orgno ");
  		de.addSql("   and b.belongorgno = c.orgno ");
  		de.addSql("   and c.orgtype = 'HSDOMAIN_DSRSJ' ");
  		de.addSql("   and a.empno = :empno ");
		this.de.setString("empno", this.getUser().getUserid());
		DataStore szvds = this.de.query();
		
		de.clearSql();
  		de.addSql(" select sleepflag,roleno,rolename,displayname ");
  		de.addSql("   from odssu.roleinfor ");
  		de.addSql("  where (roleno like :roleno or rolename like :rolename ");
  		de.addSql("     or upper(rolenamepy) like :para3  ");
  		de.addSql("     or displayname like :displayname ");
  		de.addSql("     or upper(displaynamepy) like :para5) ");
  		de.addSql("    and rolenature = '5' ");
  		de.addSql("    and jsgn in ('1', '2') ");
  		de.addSql("    and sleepflag = '0' ");
		if (szvds == null || szvds.rowCount() <= 0) {
  			de.addSql("  and roletype not in ('ODS_DUTYDEF','ODS_ORGMANAGE','ODS_SYSMANAGE','ODS_WORK_DISPATCH') ");
		}
  		de.addSql(" order by roleno ");
		this.de.setString("roleno", "%" + roleno + "%");
		this.de.setString("rolename", "%" + roleno + "%");
		this.de.setString("para3", "%" + roleno.toUpperCase() + "%");
		this.de.setString("displayname", "%" + roleno + "%");
		this.de.setString("para5", "%" + roleno.toLowerCase() + "%");
		DataStore roleVds = this.de.query();
		DataObject result = DataObject.getInstance();
		result.put("roleds", roleVds);
		return result;
	}
	/**
	 * 方法简介：保存要调整的干系角色到流程变量
	 * 郑海杰  2016-1-18
	 */
	public final DataObject doSaveGxRoleAdjustXzytzdgxjs(DataObject para) throws AppException{
		String roleno = para.getString("roleno");
		String roleName = para.getString("rolename");
		String piid = para.getString("piid");
		ProcessUtil.setTEEVarByPiid(piid, "ytzdgxjsno", roleno);
		ProcessUtil.setTEEVarByPiid(piid, "ytzdgxjsname", roleName);
		
		String empno = (String) ProcessUtil.getTEEVarByPiid(piid, "empno");
  		  		de.clearSql();
  		de.addSql(" update odssuws.rydjgdgxjstz ");
  		de.addSql("    set roleno = :roleno, ");
  		de.addSql("        rolename = :rolename ");
  		de.addSql("  where piid = :piid ");
		this.de.setString("roleno", roleno);
		this.de.setString("rolename", roleName);
		this.de.setString("piid", piid);
		this.de.update();
		
		de.clearSql();
  		de.addSql(" insert into odssuws.rydjgdgxjstz_assigned_role_org(piid,orgno) ");
  		de.addSql(" select :piid,a.orgno     ");
  		de.addSql("   from odssu.ir_emp_org_all_role a ");
  		de.addSql("  where a.empno = :empno ");
  		de.addSql("    and a.roleno = :roleno ");
  		de.addSql("    and a.jsgn in ('1','2') ");
  		de.addSql("    and rolenature = '5' ");
  		de.setString("piid",piid);
		de.setString("empno", empno);
		de.setString("roleno", roleno);
		de.update();
		return null;
	}
	/**
	 * 方法简介：转向 干系角色调整
	 *         通过MTree选择干系角色对应的机构页面
	 * 郑海杰  2016-1-18
	 */
	public final DataObject fwPageGxRoleAdjustXzjsdydjg(DataObject para) throws AppException{
		String piid = para.getString("piid");
  		de.clearSql();
  		de.addSql(" select a.empno,b.empname,loginname username ");
  		de.addSql("   from odssuws.rydjgdgxjstz a, ");
  		de.addSql("        odssu.empinfor b ");
  		de.addSql("  where a.piid = :piid ");
  		de.addSql("    and a.empno = b.empno ");
		this.de.setString("piid", piid);
		DataStore empAndRoleVds = this.de.query();
		
		String roleno = (String) ProcessUtil.getTEEVarByPiid(piid, "ytzdgxjsno");
		String rolename = (String) ProcessUtil.getTEEVarByPiid(piid, "ytzdgxjsname");
		empAndRoleVds.put(0, "roleno", roleno);
		empAndRoleVds.put(0, "rolename", rolename);
		DataObject result = DataObject.getInstance();
		result.put("emproleds", empAndRoleVds);
		return result;
	}
	/**
	 * 方法简介： 选择干系角色对应的机构页面 所需数据
	 * 能天宇 2016-9-21
	 */
	public final DataObject resChooseOrgForBatchAddEmpRole(DataObject para) throws AppException{
		String empno = para.getString("empno");
		String roleno = para.getString("roleno");
		if(empno == null || "".equals(empno)){
			throw new AppException("打开机构选择页面前传入的参数empno为空！");
		}
		if(roleno == null || "".equals(roleno)){
			throw new AppException("打开机构选择页面前传入的参数roleno为空！");
		}
  		de.clearSql();
  		de.addSql(" select empno,empname,loginname username ");
  		de.addSql("   from odssu.empinfor ");
  		de.addSql("  where empno = :empno ");
		this.de.setString("empno", empno);
		DataStore empAndRoleVds = this.de.query();

		if(empAndRoleVds == null || empAndRoleVds.rowCount() == 0){
			throw new AppException("无法获取表【odssu.empinfor】中的人员编号为【"+empno+"】的相关数据！");
		}
		String rolename = OdssuUtil.getRoleNameByRoleno(roleno);
		empAndRoleVds.put(0, "roleno", roleno);
		empAndRoleVds.put(0, "rolename", rolename);
		DataObject result = DataObject.getInstance();
		result.put("emproleds", empAndRoleVds);
		return result;
	}
	public final DataObject fillGxRoleYjpzdjgGrid(DataObject para) throws AppException{
		String piid = para.getString("piid");
		String orgname = para.getString("orgname","");
  		de.clearSql();
		
		//查询工单表中 已经配置当前干系角色的 机构

		de.addSql(" select a.orgno,b.orgname ");
  		de.addSql("   from odssuws.rydjgdgxjstz_assigned_role_org a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where piid = :piid ");
  		de.addSql("    and a.orgno = b.orgno ");
  		de.addSql("    and (b.orgno like :orgno ");
  		de.addSql("         or b.orgname like :orgname ");
  		de.addSql("         or upper(b.orgnamepy) like :para4 )");
		this.de.setString("piid", piid);
		this.de.setString("orgno", "%" + orgname +"%");
		this.de.setString("orgname", "%" + orgname +"%");
		this.de.setString("para4", "%" + orgname.toUpperCase() +"%");
		DataStore wsOrgVds = this.de.query();
		
		//查询账表中 已经配置当前干系角色的 机构
		String empno = (String) ProcessUtil.getTEEVarByPiid(piid, "empno");
		String roleno = (String) ProcessUtil.getTEEVarByPiid(piid, "ytzdgxjsno");
		de.clearSql();
  		de.addSql(" select a.orgno,b.orgname ");
  		de.addSql("   from odssu.ir_emp_org_all_role a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where empno = :empno ");
  		de.addSql("    and roleno = :roleno ");
  		de.addSql("    and jsgn in ('1','2') ");
  		de.addSql("    and rolenature = '5' ");
  		de.addSql("    and a.orgno = b.orgno ");
  		de.addSql("    and (b.orgno like :orgno ");
  		de.addSql("         or b.orgname like :orgname ");
  		de.addSql("         or upper(b.orgnamepy) like :para5 )");
		this.de.setString("empno", empno);
		this.de.setString("roleno", roleno);
		this.de.setString("orgno", "%" + orgname +"%");
		this.de.setString("orgname", "%" + orgname +"%");
		this.de.setString("para5", "%" + orgname.toUpperCase() +"%");
		DataStore accOrgVds = this.de.query();
		
		DataStore addAndSurplusVds = dealAddAndSurplusFlag(wsOrgVds, accOrgVds);

		addAndSurplusVds = MultiSortUtil.multiSortDS(addAndSurplusVds, "comments:desc,_row_selected:desc,orgno:asc");
		DataObject result = DataObject.getInstance();
		result.put("orgvds", addAndSurplusVds);
		return result;
	}
	public final DataObject fillGxRoleYjpzdjgGridForBatchAdd(DataObject para) throws AppException{
		String uuid = para.getString("uuid");
		String empno = para.getString("empno");
		String roleno = para.getString("roleno");
		String orgname = para.getString("orgname","");
  		de.clearSql();
		
		if(empno == null || "".equals(empno)){
			throw new AppException("传入的参数empno为空！");
		}
		if(roleno == null || "".equals(roleno)){
			throw new AppException("传入的参数roleno为空！");
		}
		if(uuid == null || "".equals(uuid)){
			throw new AppException("传入的参数uuid为空！");
		}
		
		//查询工单表中 已经配置当前干系角色的 机构

		de.addSql(" select a.orgno,b.orgname ");
  		de.addSql("   from odssuws.batchadd_assigned_role_org a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where a.uuid = :uuid ");
  		de.addSql("    and a.orgno = b.orgno ");
  		de.addSql("    and b.orgtype not in('HSDOMAIN_SBS','HSDOMIN_SBZ') ");
  		de.addSql("    and (b.orgno like :orgno ");
  		de.addSql("         or b.orgname like :orgname ");
  		de.addSql("         or upper(b.orgnamepy) like :para4 )");
		this.de.setString("uuid", uuid);
		this.de.setString("orgno", "%" + orgname +"%");
		this.de.setString("orgname", "%" + orgname +"%");
		this.de.setString("para4", "%" + orgname.toUpperCase() +"%");
		DataStore wsOrgVds = this.de.query();
		
		//查询账表中 已经配置当前干系角色的 机构
		de.clearSql();
  		de.addSql(" select a.orgno,b.orgname ");
  		de.addSql("   from odssu.ir_emp_org_all_role a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where empno = :empno ");
  		de.addSql("    and roleno = :roleno ");
  		de.addSql("    and b.orgtype not in('HSDOMAIN_SBS','HSDOMIN_SBZ') ");
  		de.addSql("    and jsgn in ('1','2') ");
  		de.addSql("    and rolenature = '5' ");
  		de.addSql("    and a.orgno = b.orgno ");
  		de.addSql("    and (b.orgno like :orgno ");
  		de.addSql("         or b.orgname like :orgname ");
  		de.addSql("         or upper(b.orgnamepy) like :para5 )");
		this.de.setString("empno", empno);
		this.de.setString("roleno", roleno);
		this.de.setString("orgno", "%" + orgname +"%");
		this.de.setString("orgname", "%" + orgname +"%");
		this.de.setString("para5", "%" + orgname.toUpperCase() +"%");
		DataStore accOrgVds = this.de.query();
		
		DataStore addAndSurplusVds = dealAddAndSurplusFlag(wsOrgVds, accOrgVds);

		addAndSurplusVds = MultiSortUtil.multiSortDS(addAndSurplusVds, "comments:desc,_row_selected:desc,orgno:asc");
		DataObject result = DataObject.getInstance();
		result.put("orgvds", addAndSurplusVds);
		return result;
	}
	public final DataObject initWSForBatchAdd(DataObject para) throws AppException{
		String uuid = para.getString("uuid");
		String empno = para.getString("empno");
		String roleno = para.getString("roleno");
  		de.clearSql();
		if(empno == null || "".equals(empno)){
			throw new AppException("传入的参数empno为空！");
		}
		if(roleno == null || "".equals(roleno)){
			throw new AppException("传入的参数roleno为空！");
		}
		if(uuid == null || "".equals(uuid)){
			throw new AppException("传入的参数uuid为空！");
		}
		//查询账表中 已经配置当前干系角色的 机构
		de.clearSql();
  		de.addSql(" select orgno ");
  		de.addSql("   from odssu.ir_emp_org_all_role  ");
  		de.addSql("  where empno = :empno ");
  		de.addSql("    and roleno = :roleno ");
  		de.addSql("    and jsgn in ('1','2') ");
  		de.addSql("    and rolenature = '5' ");
		this.de.setString("empno", empno);
		this.de.setString("roleno", roleno);
		DataStore accOrgVds = this.de.query();
		
		//首先清理表数据（batchadd_assigned_role_org）
		de.clearSql();
  		de.addSql(" delete from odssuws.batchadd_assigned_role_org ");
  		de.addSql("  where uuid = :uuid ");
		this.de.setString("uuid", uuid);
		this.de.update();
		
		//把账表中的数据同步
		de.clearSql();
  		de.addSql(" insert into odssuws.batchadd_assigned_role_org (uuid,orgno) values(:uuid,:accorgno) ");
  		DataStore vps = DataStore.getInstance();
		for (int i = 0; i < accOrgVds.rowCount(); i++) {
			String accOrgno = accOrgVds.getString(i, "orgno");
			vps.put(i, "uuid", uuid);
			vps.put(i, "accorgno", accOrgno);
		}
		de.batchUpdate(vps);
		
		DataObject result = DataObject.getInstance();
		return result;
	}
	public final DataObject fillPossibleOrgGrid(DataObject para) throws AppException{
		String piid = para.getString("piid");
		String typeno = para.getString("typeno");
		String orgname = para.getString("orgname","");
  		  		de.clearSql();
		
		//查询工单表中 已经配置当前干系角色的 机构

		de.addSql(" select a.orgno,b.orgname ");
  		de.addSql("   from odssuws.rydjgdgxjstz_assigned_role_org a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where piid = :piid ");
  		de.addSql("    and a.orgno = b.orgno ");
		this.de.setString("piid", piid);
		DataStore wsOrgVds = this.de.query();
		
		//查询账表中 已经配置当前干系角色的 机构
		String empno = (String) ProcessUtil.getTEEVarByPiid(piid, "empno");
		String roleno = (String) ProcessUtil.getTEEVarByPiid(piid, "ytzdgxjsno");
		de.clearSql();
  		de.addSql(" select a.orgno,b.orgname ");
  		de.addSql("   from odssu.ir_emp_org_all_role a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where empno = :empno ");
  		de.addSql("    and roleno = :roleno ");
  		de.addSql("    and jsgn in ('1','2') ");
  		de.addSql("    and rolenature = '5' ");
  		de.addSql("    and a.orgno = b.orgno ");
		this.de.setString("empno", empno);
		this.de.setString("roleno", roleno);
		DataStore accOrgVds = this.de.query();
		
		DataStore addAndSurplusVds = dealAddAndSurplusFlag(wsOrgVds, accOrgVds);
		
		//查询当前机构类型下 可能的机构
		de.clearSql();
  		de.addSql(" select orgno,orgname ");
  		de.addSql("   from odssu.orginfor ");
  		de.addSql("  where orgtype = :typeno ");
  		de.addSql("    and sleepflag = '0' ");
  		de.addSql("    and (orgname like :orgname ");
  		de.addSql("         or orgno like :orgno ");
  		de.addSql("         or upper(orgnamepy) like :para4 )");
		this.de.setString("typeno", typeno);
		this.de.setString("orgname", "%"+ orgname +"%");
		this.de.setString("orgno", "%"+ orgname +"%");
		this.de.setString("para4", "%"+ orgname.toUpperCase() +"%");
		DataStore orgVds = this.de.query();
		
		orgVds = dealRowSelectedFlag(addAndSurplusVds, orgVds);
		
		orgVds = MultiSortUtil.multiSortDS(orgVds, "comments:desc,_row_selected:desc,orgno:asc");
		DataObject result = DataObject.getInstance();
		result.put("orgvds", orgVds);
		return result;
	}
	public final DataObject fillPossibleOrgGridForBatchAdd(DataObject para) throws AppException{
		String uuid = para.getString("uuid");
		String typeno = para.getString("typeno");
		String orgname = para.getString("orgname","");
		String roleno = para.getString("roleno");
		String empno = para.getString("empno");
		
		if(empno == null || "".equals(empno)){
			throw new AppException("传入的参数empno为空！");
		}
		if(typeno == null || "".equals(typeno)){
			throw new AppException("传入的参数empno为空！");
		}
		if(typeno == null || "".equals(typeno)){
			throw new AppException("传入的参数typeno为空！");
		}
		if(uuid == null || "".equals(uuid)){
			throw new AppException("传入的参数uuid为空！");
		}
		if(roleno == null || "".equals(roleno)){
			throw new AppException("传入的参数roleno为空！");
		}
  		de.clearSql();
		//查询工单表中 已经配置当前干系角色的 机构

		de.addSql(" select a.orgno,b.orgname ");
  		de.addSql("   from odssuws.batchadd_assigned_role_org  a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where uuid = :uuid ");
  		de.addSql("    and a.orgno = b.orgno ");
		this.de.setString("uuid", uuid);
		DataStore wsOrgVds = this.de.query();
		
		//查询账表中 已经配置当前干系角色的 机构
		de.clearSql();
  		de.addSql(" select a.orgno,b.orgname ");
  		de.addSql("   from odssu.ir_emp_org_all_role a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where empno = :empno ");
  		de.addSql("    and roleno = :roleno ");
  		de.addSql("    and jsgn in ('1','2') ");
  		de.addSql("    and rolenature = '5' ");
  		de.addSql("    and a.orgno = b.orgno ");
		this.de.setString("empno", empno);
		this.de.setString("roleno", roleno);
		DataStore accOrgVds = this.de.query();
		
		DataStore addAndSurplusVds = dealAddAndSurplusFlag(wsOrgVds, accOrgVds);
		
		//查询当前机构类型下 可能的机构
		de.clearSql();
  		de.addSql(" select orgno,orgname ");
  		de.addSql("   from odssu.orginfor ");
  		de.addSql("  where orgtype = :typeno ");
  		de.addSql("    and sleepflag = '0' ");
  		de.addSql("    and (orgname like :orgname ");
  		de.addSql("         or orgno like :orgno ");
  		de.addSql("         or upper(orgnamepy) like :para4 )");
		this.de.setString("typeno", typeno);
		this.de.setString("orgname", "%"+ orgname +"%");
		this.de.setString("orgno", "%"+ orgname +"%");
		this.de.setString("para4", "%"+ orgname.toUpperCase() +"%");
		DataStore orgVds = this.de.query();
		
		orgVds = dealRowSelectedFlag(addAndSurplusVds, orgVds);
		
		orgVds = MultiSortUtil.multiSortDS(orgVds, "comments:desc,_row_selected:desc,orgno:asc");
		DataObject result = DataObject.getInstance();
		result.put("orgvds", orgVds);
		return result;
	}
	/**
	 * 方法简介：计算加减号逻辑
	 * 郑海杰  2015-11-24
	 */
	private DataStore dealAddAndSurplusFlag(DataStore orgVdsInWS, DataStore orgVdsInAcc) throws AppException{
		DataStore addAndCommonDS = DataStore.getInstance();
		for(int i = 0; i < orgVdsInWS.size(); i++){
			String orgnoInWS = orgVdsInWS.getString(i, "orgno");
			int findIndex = orgVdsInAcc.find("orgno == "+orgnoInWS);
			if(findIndex == -1){
				DataObject temp = orgVdsInWS.get(i);
				temp.put("_row_selected", true);
				temp.put("comments", "（+）");
				addAndCommonDS.addRow(temp);
			}else{
				DataObject temp = orgVdsInWS.get(i);
				temp.put("_row_selected", true);
				temp.put("comments", "");
				addAndCommonDS.addRow(temp);
			}
		}
		DataStore surplusDS = DataStore.getInstance();
		for(int i = 0; i < orgVdsInAcc.size(); i++){
			String orgnoInAcc = orgVdsInAcc.getString(i, "orgno");
			int findIndex = orgVdsInWS.find("orgno == "+orgnoInAcc);
			if(findIndex == -1){
				DataObject temp = orgVdsInAcc.get(i);
				temp.put("_row_selected", false);
				temp.put("comments", "（-）");
				surplusDS.addRow(temp);
			}
		}
		addAndCommonDS.combineDatastore(surplusDS);
		return addAndCommonDS;
	}
	/**
	 * 方法简介：计算加减号逻辑 - ForBatchAdd
	 * 能天宇  2016-9-24
	 */
	private DataStore dealAddAndSurplusFlagForBatchAdd(DataStore orgVdsInWS, DataStore orgVdsInAcc) throws AppException{
		DataStore addAndCommonDS = DataStore.getInstance();
		for(int i = 0; i < orgVdsInWS.size(); i++){
			String orgnoInWS = orgVdsInWS.getString(i, "orgno");
			int findIndex = orgVdsInAcc.find("orgno == "+orgnoInWS);
			if(findIndex == -1){
				DataObject temp = orgVdsInWS.get(i);
				temp.put("_row_selected", true);
				temp.put("comments", "+");
				addAndCommonDS.addRow(temp);
			}else{
				DataObject temp = orgVdsInWS.get(i);
				temp.put("_row_selected", true);
				temp.put("comments", "");
				addAndCommonDS.addRow(temp);
			}
		}
		DataStore surplusDS = DataStore.getInstance();
		for(int i = 0; i < orgVdsInAcc.size(); i++){
			String orgnoInAcc = orgVdsInAcc.getString(i, "orgno");
			int findIndex = orgVdsInWS.find("orgno == "+orgnoInAcc);
			if(findIndex == -1){
				DataObject temp = orgVdsInAcc.get(i);
				temp.put("_row_selected", false);
				temp.put("comments", "-");
				surplusDS.addRow(temp);
			}
		}
		addAndCommonDS.combineDatastore(surplusDS);
		return addAndCommonDS;
	}
	/**
	 * 方法简介：获取批量 新增人员干系角色 每次调用ASO的数据集
	 * 能天宇  2016-9-23
	 */
	private DataStore getBatchAddFinalVds(DataStore gridVds) throws AppException{
		DataStore addAndCommonDS = DataStore.getInstance();
		if( gridVds ==null || gridVds.rowCount() == 0){
			return addAndCommonDS;
		}
		String uuid = gridVds.getString(0, "uuid");
		String empno = gridVds.getString(0, "empno");
		String roleno = gridVds.getString(0, "roleno");
		if(uuid == null || "".equals(uuid)){
			throw new AppException("DataStore中的属性uuid为空");
		}
		if(empno == null || "".equals(empno)){
			throw new AppException("DataStore中的属性empno为空");
		}
		if(roleno == null || "".equals(roleno)){
			throw new AppException("DataStore中的属性roleno为空");
		}
		//查询工单表中 已经配置当前干系角色的 机构

		de.clearSql();
  		de.addSql(" select a.orgno,b.orgname ");
  		de.addSql("   from odssuws.batchadd_assigned_role_org a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where a.uuid = :uuid ");
  		de.addSql("    and a.orgno = b.orgno ");
		this.de.setString("uuid", uuid);
		DataStore wsOrgVds = this.de.query();
		//查询账表中 已经配置当前干系角色的 机构
		de.clearSql();
  		de.addSql(" select a.orgno,b.orgname ");
  		de.addSql("   from odssu.ir_emp_org_all_role a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where empno = :empno ");
  		de.addSql("    and roleno = :roleno ");
  		de.addSql("    and jsgn in ('1','2') ");
  		de.addSql("    and rolenature = '5' ");
  		de.addSql("    and a.orgno = b.orgno ");
		this.de.setString("empno", empno);
		this.de.setString("roleno", roleno);
		DataStore accOrgVds = this.de.query();
		
		DataStore addAndSurplusVds = dealAddAndSurplusFlagForBatchAdd(wsOrgVds, accOrgVds);
		for(int j = 0; j < addAndSurplusVds.size(); j++){
			//每人每角色对应多个机构
			DataObject temp = DataObject.getInstance(gridVds.get(0));
			temp.put("orgno", addAndSurplusVds.getString(j, "orgno") );
			temp.put("czlb", addAndSurplusVds.getString(j, "comments"));
			addAndCommonDS.addRow(temp);
		}
		return addAndCommonDS;
	}
	private DataStore dealRowSelectedFlag(DataStore addAndSurplusFlagVds, DataStore resultVds) throws AppException{
		DataStore rowSelectedFlag = DataStore.getInstance();
		for(int i = 0; i < resultVds.size(); i++){
			String orgnoInResult = resultVds.getString(i, "orgno");
			int findIndex = addAndSurplusFlagVds.find("orgno == "+orgnoInResult);
			if(findIndex == -1){
				DataObject temp = resultVds.get(i);
				temp.put("_row_selected", false);
				temp.put("comments", "");
				rowSelectedFlag.addRow(temp);
			}else{
				DataObject temp = resultVds.get(i);
				temp.put("_row_selected", addAndSurplusFlagVds.getBoolean(findIndex, "_row_selected"));
				temp.put("comments", addAndSurplusFlagVds.getString(findIndex, "comments"));
				rowSelectedFlag.addRow(temp);
			}
		}
		return rowSelectedFlag;
	}
	public final DataObject queryOrgNumber(DataObject para) throws AppException{
		String uuid = para.getString("uuid");
  		de.clearSql();
  		de.addSql(" select count(*) orgnumber");
  		de.addSql("   from odssuws.batchadd_assigned_role_org ");
  		de.addSql("  where uuid = :uuid ");
		this.de.setString("uuid", uuid);
		DataStore countVds = this.de.query();
		if(countVds == null || countVds.size() == 0){
			throw new AppException("无法查找表【odssuws.batchadd_assigned_role_org】中uuid为【"+uuid+"】的机构数量信息！");
		}
		DataObject vdo = DataObject.getInstance();
		
		String orgnumber = String.valueOf(countVds.getInt(0, "orgnumber"));
		vdo.put("orgnumber", orgnumber);
		return vdo;
	}
	/**
	 * 方法描述：检查Grid中的人员填的对不对
	 * @author 能天宇 2016-9-27 
	 */
	public final DataObject checkAndFill(DataObject para) throws AppException{
		DataObject vdo = DataObject.getInstance();
		String empname =  para.getString("empname");
		if(empname != null )
			empname = empname.trim();
		String empno =  para.getString("empno");
		if(empno !=null && !"".equals(empno)){
  				de.clearSql();
  				de.addSql(" select empno ");
  				de.addSql("   from odssu.empinfor ");
  				de.addSql("  where empname =  :empname ");
				this.de.setString("empname", empname);
				DataStore countVds = this.de.query();
				if(countVds == null || countVds.rowCount() == 0){			//此人不存在
					vdo.put("msg", "人员【"+empname+"】不存在." );
					return vdo;
				}else if(countVds.rowCount() == 1){							//人名唯一
					String empnoTemp = countVds.getString(0, "empno") ;
					if( !OdssuUtil.isEmpOnWork(empnoTemp)){					
						vdo.put("msg",  "人员【"+empname+"】已离职." );
						return vdo;
					}
					if( !OdssuUtil.isEmpHaveHrBelong(empnoTemp)){
						vdo.put("msg",  "人员【"+empname+"】没有人事隶属机构." );
						return vdo;
					}
					if(empnoTemp.equals(empno)){
						return vdo;
					}
					vdo.put("empno", empnoTemp);
					return vdo;
				}else {												//重名多个人
					int index = countVds.find("empno == "+empno);
					if( index > -1 ){
						if( !OdssuUtil.isEmpOnWork(empno)){					
							vdo.put("msg",  "人员【"+empname+"】已离职." );
							return vdo;
						}
						if( !OdssuUtil.isEmpHaveHrBelong(empno)){
							vdo.put("msg",  "人员【"+empname+"】没有人事隶属机构." );
							return vdo;
						}
						return vdo;
					}else{
						vdo.put("msg", "人员【"+empname+"】有重名，需要使用LOV确认." );
						return vdo;
					}
				}
		}else{	
				//没有回填过的empno，说明是第一次选择

				de.clearSql();
  				de.addSql(" select empno, empname ");
  				de.addSql("   from odssu.empinfor ");
  				de.addSql("  where empname =  :empname ");
				this.de.setString("empname", empname);
				DataStore countVds = this.de.query();
				if(countVds == null || countVds.rowCount() == 0){
					vdo.put("msg", "人员【"+empname+"】不存在." );
					return vdo;
				}else if(countVds.rowCount() == 1){
					String empnoTemp = countVds.getString(0, "empno") ;
					if( !OdssuUtil.isEmpOnWork(empnoTemp)){					
						vdo.put("msg",  "人员【"+empname+"】已离职." );
						return vdo;
					}
					if( !OdssuUtil.isEmpHaveHrBelong(empnoTemp)){
						vdo.put("msg",  "人员【"+empname+"】没有人事隶属机构." );
						return vdo;
					}
					vdo.put("empno", empnoTemp);
					return vdo;
				}else{
					vdo.put("msg", "人员【"+empname+"】有重名，需要确认." );
					return vdo;
				}	
		}
	}
	public final DataObject checkOrg(DataObject para) throws AppException{
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
  		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql("   from odssuws.rydjgdgxjstz_assigned_role_org a ");
  		de.addSql("  where piid = :piid ");
  		de.addSql("    and orgno = :orgno ");
		this.de.setString("piid", piid);
		this.de.setString("orgno", orgno);
		DataStore empVds = this.de.query();
		if(empVds == null || empVds.size() == 0){
			de.clearSql();
  			de.addSql(" insert into odssuws.rydjgdgxjstz_assigned_role_org(piid,orgno) ");
  			de.addSql("                          values(:piid,  :orgno) ");
			this.de.setString("piid", piid);
			this.de.setString("orgno", orgno);
			this.de.update();
		}
		return null;
	}
	
	public final DataObject unCheckOrg(DataObject para) throws AppException{
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
  		de.clearSql();
		de.clearSql();
  		de.addSql(" delete from odssuws.rydjgdgxjstz_assigned_role_org ");
  		de.addSql("  where piid = :piid and orgno = :orgno ");
		this.de.setString("piid", piid);
		this.de.setString("orgno", orgno);
		this.de.update();
		return null;
	}
	/**
	 * 方法简介：将当前页面上的人员全选为干系人
	 * 郑海杰  2015-11-24
	 */
	public DataObject checkAllOrg(DataObject para) throws AppException{
		String piid = para.getString("piid");
		DataStore orgVds = para.getDataStore("orgGrid");
  		de.clearSql();
  		de.addSql(" delete from odssuws.rydjgdgxjstz_assigned_role_org ");
  		de.addSql("  where piid = :piid ");
  		de.addSql("    and orgno = :orgno ");
		DataStore vps = DataStore.getInstance();
		for(int i = 0; i < orgVds.size(); i++){
			String orgno = orgVds.getString(i, "orgno");
			boolean _row_selected = orgVds.getBoolean(i, "_row_selected");
			if(_row_selected){
				vps.put(i, "piid", piid);
				vps.put(i, "orgno", orgno);
			}
		}
		de.batchUpdate(vps);
		
		de.clearSql();
  		de.addSql(" insert into odssuws.rydjgdgxjstz_assigned_role_org(piid, orgno) ");
  		de.addSql("                          values(:piid,:orgno)  ");
  		vps.clear();
		for(int i = 0; i < orgVds.size(); i++){
			String orgno = orgVds.getString(i, "orgno");
			boolean _row_selected = orgVds.getBoolean(i, "_row_selected");
			if(_row_selected){
				vps.put(i, "piid", piid);
				vps.put(i, "orgno", orgno);
			}
		}
		de.batchUpdate(vps);
		return null;
	}
	/**
	 * 方法简介：将当前页面上的干系人全部删除
	 * 郑海杰  2015-11-24
	 */
	public DataObject unCheckAllOrg(DataObject para) throws AppException{
  		de.clearSql();
		String piid = para.getString("piid");
		DataStore orgVds = para.getDataStore("orgGrid");
  		de.addSql(" delete from odssuws.rydjgdgxjstz_assigned_role_org ");
  		de.addSql("  where piid = :piid ");
  		de.addSql("    and orgno = :orgno ");
		DataStore vps = DataStore.getInstance();
		for(int i = 0; i < orgVds.size(); i++){
			String orgno = orgVds.getString(i, "orgno");
			vps.put(i, "piid", piid);
			vps.put(i, "orgno", orgno);
		}
			de.batchUpdate(vps);
		return null;
	}
	public final DataObject checkOrgForBatchAdd(DataObject para) throws AppException{
		String uuid = para.getString("uuid");
		String orgno = para.getString("orgno");
  		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql("   from odssuws.batchadd_assigned_role_org  a ");
  		de.addSql("  where uuid = :uuid ");
  		de.addSql("    and orgno = :orgno ");
		this.de.setString("uuid", uuid);
		this.de.setString("orgno", orgno);
		DataStore empVds = this.de.query();
		if(empVds == null || empVds.size() == 0){
			de.clearSql();
  			de.addSql(" insert into odssuws.batchadd_assigned_role_org (uuid,orgno) ");
  			de.addSql("                          values(:uuid,  :orgno) ");
			this.de.setString("uuid", uuid);
			this.de.setString("orgno", orgno);
			this.de.update();
		}
		return null;
	}
	
	public final DataObject unCheckOrgForBatchAdd(DataObject para) throws AppException{
		String uuid = para.getString("uuid");
		String orgno = para.getString("orgno");
		de.clearSql();
  		de.addSql(" delete from odssuws.batchadd_assigned_role_org  ");
  		de.addSql("  where uuid = :uuid and orgno = :orgno ");
		this.de.setString("uuid", uuid);
		this.de.setString("orgno", orgno);
		this.de.update();
		return null;
	}
	/**
	 * 方法简介：将当前页面上的人员全选为干系人
	 * 郑海杰  2015-11-24
	 */
	public DataObject checkAllOrgForBatchAdd(DataObject para) throws AppException{
		String uuid = para.getString("uuid");
		DataStore orgVds = para.getDataStore("orgGrid");
  		de.clearSql();
  		de.addSql(" delete from odssuws.batchadd_assigned_role_org ");
  		de.addSql("  where uuid = :uuid ");
  		de.addSql("    and orgno = :orgno ");
		DataStore vps = DataStore.getInstance();
		for(int i = 0; i < orgVds.size(); i++){
			String orgno = orgVds.getString(i, "orgno");
			boolean _row_selected = orgVds.getBoolean(i, "_row_selected");
			if(_row_selected){
				vps.put(i, "uuid", uuid);
				vps.put(i, "orgno", orgno);
			}
		}
		de.batchUpdate(vps);

		
		de.clearSql();
  		de.addSql(" insert into odssuws.batchadd_assigned_role_org (uuid, orgno) ");
  		de.addSql("                          values(:uuid,:orgno)  ");
  		vps.clear();
		for(int i = 0; i < orgVds.size(); i++){
			String orgno = orgVds.getString(i, "orgno");
			boolean _row_selected = orgVds.getBoolean(i, "_row_selected");
			if(_row_selected){
				vps.put(i, "uuid", uuid);
				vps.put(i, "orgno", orgno);
			}
		}
		de.batchUpdate(vps);
		return null;
	}
	/**
	 * 方法简介：将当前页面上的机构全部删除
	 * 郑海杰  2015-11-24
	 */
	public DataObject unCheckAllOrgForBatchAdd(DataObject para) throws AppException{
  		de.clearSql();
		String uuid = para.getString("uuid");
		DataStore orgVds = para.getDataStore("orgGrid");
  		de.addSql(" delete from odssuws.batchadd_assigned_role_org  ");
  		de.addSql("  where uuid = :uuid ");
  		de.addSql("    and orgno = :orgno ");
		DataStore vps = DataStore.getInstance();
		for(int i = 0; i < orgVds.size(); i++){
			String orgno = orgVds.getString(i, "orgno");
			vps.put(i, "uuid", uuid);
			vps.put(i, "orgno", orgno);
		}
		de.batchUpdate(vps);
		return null;
	}
	/**
	 * 人员对机构的干系角色申请界面的查询
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-8-19
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject queryResult(DataObject para) throws Exception {
		String piid = para.getString("piid");

		DataObject pdo = DataObject.getInstance();
		pdo.put("piid", piid);
		BPO ibpo = this.newBPO(ProcessBPO.class);
		DataObject result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);

		String empno = result.getString("empno");
		String empname = result.getString("empname");
    		de.clearSql();
  		de.addSql(" select i.orgno,o.orgname,r.roleno,r.rolename,' ' orgflag ");
  		de.addSql("   from odssu.ir_emp_outer_unduty_role i, ");
  		de.addSql("        odssu.roleinfor r, ");
  		de.addSql("        odssu.orginfor o ");
  		de.addSql("  where r.roleno = i.roleno and i.orgno = o.orgno and i.empno = :empno ");
		de.setString("empno", empno);
		DataStore vdsacc = de.query();

		de.clearSql();
  		de.addSql(" select a.orgno,c.orgname,a.roleno,b.rolename,orgflag ");
  		de.addSql("   from odssuws.rydjgdgxjstz_role a, ");
  		de.addSql("        odssu.roleinfor b, ");
  		de.addSql("        odssu.orginfor c ");
  		de.addSql("  where a.piid = :piid ");
  		de.addSql("    and a.roleno = b.roleno ");
  		de.addSql("    and a.orgno = c.orgno ");
		de.setString("piid", piid);
		DataStore vdsws = de.query();

		for(int i = 0; i < vdsws.rowCount(); i++) {
			String orgno = vdsws.getString(i, "orgno");
			String roleno = vdsws.getString(i, "roleno");
			String orgflag = vdsws.getString(i, "orgflag");
			String rolename = vdsws.getString(i, "rolename");
			String orgname = vdsws.getString(i, "orgname");
			if (orgflag != null && orgflag.trim().isEmpty() == false) {
				orgflag = "（" + orgflag + "）";
			}
			int rowNo = vdsacc.find("orgno == " + orgno + " and roleno == " + roleno);
			if (rowNo < 0) {
				vdsacc.addRow();
				vdsacc.put(vdsacc.rowCount() - 1, "orgno", orgno);
				vdsacc.put(vdsacc.rowCount() - 1, "roleno", roleno);
				vdsacc.put(vdsacc.rowCount() - 1, "orgname", orgname);
				vdsacc.put(vdsacc.rowCount() - 1, "rolename", rolename);
				vdsacc.put(vdsacc.rowCount() - 1, "orgflag", orgflag);
				continue;
			}
			vdsacc.put(rowNo, "orgflag", orgflag);
		}
		vdsacc = MultiSortUtil.multiSortDS(vdsacc, "orgflag:desc,orgno:asc,roleno:asc");
		DataStore empds = DataStore.getInstance();
		empds.addRow();
		empds.put(0, "empno", empno);
		String username = OdssuUtil.getUserNameByEmpno(empno);
		empds.put(0, "username", username);
		empds.put(0, "empname", empname);
		empds.put(0, "piid", piid);
		DataObject vdo = DataObject.getInstance();
		vdo.put("empds", empds);
		vdo.put("gdxxds", vdsacc);
		return vdo;
	}

	/**
	 * 人员对机构的干系角色申请界面中，打开新增窗口，查询人员编号、姓名。
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-8-19
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwResGxRoleAdjust(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore empds = DataStore.getInstance();
		String empno, empname, piid;

		empno = para.getString("empno");
		empname = para.getString("empname");
		piid = para.getString("piid");

		empds.put(0, "piid", piid);
		empds.put(0, "empno", empno);
		empds.put(0, "empname", empname);

		vdo.put("empds", empds);

		empds = null;
		return vdo;
	}
	/**
	 * 
	 * @Description: 批量新增人员干系角色  - 校验grid数据
	 * @author 能天宇
	 * @date 2016-9-23
	 */
	public DataObject checkBatchAddEmpRole(DataObject para) throws Exception{
		DataObject msgvdo = DataObject.getInstance();	//存返回结果
		DataStore gridVds = para.getDataStore("datagrid");	//前台Grid数据
		if(gridVds == null || gridVds.rowCount() == 0){
			this.bizException("没有待处理的数据");
		}
		for(int  i = 0; i < gridVds.rowCount(); i++){
			gridVds.put(i, "checkinfo", "");
	    	String empno = gridVds.getString(i, "empno"); 
	    	String empname = gridVds.getString(i, "empname"); 
	    	String uuid = gridVds.getString(i, "uuid"); 
	    	String roleno = gridVds.getString(i, "roleno"); 
	    	String orgnumber = gridVds.getString(i, "orgnumber"); 
	        if(empname == null || "".equals(empname)){
	        	 gridVds.put(i, "checkinfo", "未填写【人员】");
	        	 continue;
	        }
	        if(empno == null || "".equals(empno)){
	        	gridVds.put(i, "checkinfo", "未取得【empno】，请确认人员【"+empname+"】是否符合要求");
	        	continue;
	        }
	        if(roleno == null || "".equals(roleno)){
	        	gridVds.put(i, "checkinfo", "未选择【角色】");
	        	continue;
	        }
	        if(orgnumber == null || "".equals(orgnumber)|| "?".equals(orgnumber)|| "0".equals(orgnumber)){
	        	gridVds.put(i, "checkinfo", "未选择【机构】"); 
	        	 continue;
	        }
	        if(uuid == null || "".equals(uuid)){
	        	gridVds.put(i, "checkinfo", "未取得【uuid】,请确认机构选择正确");
	        	 continue;
	        }
	        int rowno  = gridVds.find("roleno == " + roleno + " and empname == " + empname 
	        		+ " and empno == " + empno+ " and uuid != " + uuid);
	        if(rowno > -1 && rowno != i){
        		gridVds.put(i, "checkinfo", "含有重复行");
				continue;
	        }
	        //检查了empno与empname是否对应，人员是否在职，人员是否有隶属机构
	        DataObject vdo = checkAndFill(gridVds.getRow(i));
			if(vdo.containsKey("msg")){
				gridVds.put(i, "checkinfo", vdo.getString("msg"));
				continue;
			}else if(vdo.containsKey("empno")){
				gridVds.put(i, "checkinfo", "获取到的人员编号与人员姓名不一致，需要重新选择");
				continue;
			}
	    }
		msgvdo.put("ds", gridVds);
		return msgvdo;
	}
	/**
	 * @Description: 批量新增人员干系角色  - 正式记账
	 * @author 能天宇
	 * @date 2016-9-24
	 */
	public DataObject saveBatchAddEmpRole(DataObject para) throws Exception{
		DataObject msgvdo = DataObject.getInstance();	//存返回结果
		StringBuffer logBF  = new StringBuffer();
		CurrentUser cuser = CurrentUser.getInstance();
		cuser.setUserid("plgn");
		cuser.setUsername("批量分配ODS流程权限");
		DataStore gridVds = para.getDataStore("datagrid");	//前台Grid数据
		if(gridVds == null || gridVds.rowCount() == 0){
			this.bizException("没有待处理的数据!");
		}
		logBF.append(">>>批量分配ODS流程权限-记账\r\n");
		logBF.append(">>>即将为【"+gridVds.rowCount()+"】个用户分配ODS流程权限：\r\n");
		LanePromptUtil.promptToTrace("即将为【"+gridVds.rowCount()+"】个用户分配ODS流程权限：");
		
		//对于grid的每一行调用一次ASO，即每次循环给一个人员在X个机构下增加一个干系角色
		for(int i=0;i<gridVds.rowCount();i++){
			String uuid = StringUtil.getUUID();
			String empno = gridVds.getString(i, "empno");
			String roleno = gridVds.getString(i, "roleno");
			DataStore rowVds = DataStore.getInstance();				
			rowVds.addRow(gridVds.get(i));
			DataStore resultVds = getBatchAddFinalVds(rowVds);

			para.put("pjbh", uuid);
			para.put("empno", empno);
			para.put("emp_org_outer_role", resultVds);
			para.put("_user", cuser);
			para.put("pdid", "rydjgdgxjstz" );
			para.put("objectid", empno);
			para.put("userid", cuser.getUserid());

			try {
				i_testASO.doEntry(para);
			} catch (ASOException e) {
				e.printStackTrace();
				de.rollback();
				logBF.append("【"+(i+1)+"】为用户【"+OdssuUtil.getEmpNameByEmpno(empno)
						+"】新增角色权限【"+OdssuUtil.getRoleNameByRoleno(roleno)+"】时发生错误！数据已回滚！\r\n");
				StringBuffer strBF  = new StringBuffer("涉及机构:");
				for(int j=0;j<resultVds.rowCount();j++){
					strBF.append(OdssuUtil.getOrgNameByOrgno(resultVds.getString(j, "orgno")));
					if(j < resultVds.rowCount()-1){
						strBF.append("、" );
					}
				}
				logBF.append(strBF+"\r\n");
				msgvdo.put("msg", "校验过程中出错！请查看日志。");
				msgvdo.put("logstr", logBF.toString());
				LanePromptUtil.end();
				return msgvdo;
			}

			logBF.append("【"+(i+1)+"】为用户【"+OdssuUtil.getEmpNameByEmpno(empno)
					+"】在【"+resultVds.rowCount()+"】个机构下新增角色权限【"+OdssuUtil.getRoleNameByRoleno(roleno)+"】."+"\r\n");
			LanePromptUtil.promptToTrace("【"+(i+1)+"】为用户【"+OdssuUtil.getEmpNameByEmpno(empno)
					+"】在【"+resultVds.rowCount()+"】个机构下新增角色权限【"+OdssuUtil.getRoleNameByRoleno(roleno)+"】.");
			StringBuffer strBF  = new StringBuffer("涉及机构:");
			for(int j=0;j<resultVds.rowCount();j++){
				strBF.append(OdssuUtil.getOrgNameByOrgno(resultVds.getString(j, "orgno")));
				if(j < resultVds.rowCount()-1){
					strBF.append("、" );
				}
			}
			logBF.append(strBF+"\r\n");
			LanePromptUtil.promptToTrace(strBF.toString());
		}
		logBF.append("批量分配ODS流程权限-记账完成\r\n");
		LanePromptUtil.promptToTrace("批量分配ODS流程权限-记账完成！");
		LanePromptUtil.complete();
		msgvdo.put("msg", "批量分配ODS流程权限完成！");
		msgvdo.put("logstr", logBF.toString());
		return msgvdo;
	}
	/**
	 * 人员对机构的干系角色申请，新增的保存方法
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-8-19
	 * @param para
	 * @throws Exception
	 */
	public void saveGxRoleAdjust(DataObject para) throws Exception {
		StringBuilder sb = new StringBuilder();
		DataStore belongds;
		String orgno, orgname, piid, orgflag, roleno, rolename;
		piid = para.getString("piid");
		orgno = para.getString("orgno");
		orgname = para.getString("orgname");
		roleno = para.getString("roleno");
		rolename = para.getString("rolename");
		String empno = para.getString("empno");

		if ("".equals(orgno) || orgno == null) {
			this.bizException("机构编号不能为空！");
		}
		if ("".equals(orgname) || orgname == null) {
			this.bizException("机构名称不能为空！");
		}
		if ("".equals(roleno) || roleno == null) {
			this.bizException("角色编号不能为空！");
		}
		if ("".equals(rolename) || roleno == null) {
			this.bizException("角色编号不能为空！");
		}
    		de.addSql(" select orgname from odssu.orginfor where orgno = :orgno and sleepflag = '0' ");
		this.de.setString("orgno", orgno);

		DataStore orgds = this.de.query();
		if (orgds.rowCount() == 0) {
			this.bizException("不存在机构编号所对应的机构信息，或者机构已被注销！");
		}
		if (!orgname.trim().equals(orgds.getString(0, "orgname").trim())) {
			this.bizException("机构编号所对应的机构名称出错！");
		}

		de.clearSql();
  		de.addSql(" select rolename from odssu.roleinfor where roleno = :roleno and sleepflag = '0' ");
		this.de.setString("roleno", roleno);

		DataStore roleds = this.de.query();
		if (roleds.rowCount() == 0) {
			this.bizException("不存在角色编号所对应的机构信息，或者角色已被注销！");
		}
		if (!rolename.equals(roleds.getString(0, "rolename"))) {
			this.bizException("角色编号所对应的角色名称出错！");
		}

		de.clearSql();
  		de.addSql("select *  ");
  		de.addSql("  from odssuws.rydjgdgxjstz_role a ");
  		de.addSql(" where a.piid = :piid   ");
  		de.addSql("   and a.orgno = :orgno  ");
  		de.addSql("   and a.roleno = :roleno  ");
		this.de.setString("piid", piid);
		this.de.setString("orgno", orgno);
		this.de.setString("roleno", roleno);
		belongds = this.de.query();

		if (belongds.rowCount() > 0) {
			orgflag = belongds.getString(0, "orgflag");
			if (orgflag == null) {
				orgflag = "";
			}

			if ("-".equals(orgflag)) {// 此机构在账中存在,新增则更新其标志为空

				de.clearSql();
  				de.addSql("delete from odssuws.rydjgdgxjstz_role ");
  				de.addSql(" where piid = :piid       ");
  				de.addSql("   and orgno = :orgno      ");
  				de.addSql("   and roleno = :roleno      ");
				this.de.setString("piid", piid);
				this.de.setString("orgno", orgno);
				this.de.setString("roleno", roleno);
				this.de.update();
			} else if ("+".equals(orgflag)) {// 此机构在账中不存在,处理同一机构新增多次的情况
				de.clearSql();
  				de.addSql(" update odssuws.rydjgdgxjstz_role   ");
  				de.addSql("    set orgflag ='+'	");
  				de.addSql("   where piid = :piid    ");
  				de.addSql("     and orgno = :orgno   ");
  				de.addSql("     and roleno = :roleno   ");
				this.de.setString("piid", piid);
				this.de.setString("orgno", orgno);
				this.de.setString("roleno", roleno);
				this.de.update();
			}
		} else {
			de.clearSql();
  			de.addSql(" select 1 from odssu.ir_emp_outer_unduty_role where empno = :empno and orgno = :orgno and roleno = :roleno ");
			de.setString("empno", empno);
			de.setString("orgno", orgno);
			de.setString("roleno", roleno);
			DataStore vds1 = de.query();

			if (vds1.rowCount() != 0) {
				return;
			}

			de.clearSql();
  			de.addSql(" insert into odssuws.rydjgdgxjstz_role   ");
  			de.addSql("             (piid, orgno, orgflag, roleno) ");
  			de.addSql("      values (:piid, :orgno, '+', :roleno)    ");
			this.de.setString("piid", piid);
			this.de.setString("orgno", orgno);
			this.de.setString("roleno", roleno);
			this.de.update();
		}
	}

	/**
	 * 人员对机构的干系角色申请，删除的方法
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-8-19
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject delOrg(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		StringBuilder sb = new StringBuilder();
		DataStore belongds;
		String orgno, piid, roleno, orgflag;
		piid = para.getString("piid");
		orgno = para.getString("orgno");
		roleno = para.getString("roleno");

		BPO ibpo = this.newBPO(ProcessBPO.class);
		DataObject varvdo = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
		String lsjgid = varvdo.getString("_process_biz");
		String empno = varvdo.getString("empno");

//		de.clearSql();
//  		de.addSql(" select a.orgno , b.orgname ,b.displayname ");
//  		de.addSql("  from odssu.ir_org_closure a , odssu.orginfor b ");
//  		de.addSql(" where a.belongorgno = :lsjgid  and b.orgno = a.orgno ");
//		this.de.setString("lsjgid", lsjgid);
//
//		DataStore orgds = this.de.query();

//		int j = orgds.find("orgno == " + orgno);
//
//		if (j < 0) {
//			this.bizException("您所修改的机构不是当前业务隶属机构的下级机构,请选择符合条件的机构进行相关操作！");
//		}

		// 判断orgno在账中是否存在
		de.clearSql();
  		de.addSql("select a.orgflag ");
  		de.addSql("  from odssuws.rydjgdgxjstz_role a ");
  		de.addSql(" where a.piid = :piid      ");
  		de.addSql("   and a.orgno = :orgno      ");
  		de.addSql("   and a.roleno = :roleno      ");
		this.de.setString("piid", piid);
		this.de.setString("orgno", orgno);
		this.de.setString("roleno", roleno);
		belongds = this.de.query();

		if (belongds.rowCount() > 0) {
			orgflag = belongds.getString(0, "orgflag");
		} else {
			orgflag = "";
		}

		if (belongds.rowCount() == 0) {
			// 工单副表中没有，此时肯定为帐里的数据，验证一下
			de.clearSql();
  			de.addSql(" select 1 from odssu.ir_emp_outer_unduty_role where empno = :empno and orgno = :orgno and roleno = :roleno ");
			de.setString("empno", empno);
			de.setString("orgno", orgno);
			de.setString("roleno", roleno);
			DataStore vds1 = de.query();

			if (vds1.rowCount() == 0) {
				this.bizException("此条信息既不存在与帐表中，也不存在与工单副表中，保存失败！");
			}
			de.clearSql();
  			de.addSql(" insert into odssuws.rydjgdgxjstz_role   ");
  			de.addSql("             (piid, orgno, orgflag, roleno) ");
  			de.addSql("      values (:piid, :orgno, '-', :roleno)    ");
			this.de.setString("piid", piid);
			this.de.setString("orgno", orgno);
			this.de.setString("roleno", roleno);
			this.de.update();

		} else if ("+".equals(orgflag)) {
			// 工单副表中有，且flag为+,此时删除的操作即会把副表中的此条数据删除
			de.clearSql();
  			de.addSql("delete from odssuws.rydjgdgxjstz_role ");
  			de.addSql(" where piid = :piid       ");
  			de.addSql("   and orgno = :orgno      ");
  			de.addSql("   and roleno = :roleno      ");
			this.de.setString("piid", piid);
			this.de.setString("orgno", orgno);
			this.de.setString("roleno", roleno);
			this.de.update();
		} else if ("-".equals(orgflag)) {
			// 工单副表中有，且flag为-，此时再-一遍没有什么意思，故不作处理。
		}

		return vdo;
	}

	/**
	 * 人员对机构的干系角色申请，取消删除
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-8-19
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject delOrgAbort(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		StringBuilder sb = new StringBuilder();
		DataStore belongds;
		String orgno, roleno, piid, orgflag;

		orgno = para.getString("orgno");
		roleno = para.getString("roleno");
		piid = para.getString("piid");

		// 判断orgno在副表中是否存在
		de.clearSql();
  		de.addSql("select a.orgflag ");
  		de.addSql("  from odssuws.rydjgdgxjstz_role a ");
  		de.addSql(" where a.piid = :piid      ");
  		de.addSql("   and a.orgno = :orgno      ");
  		de.addSql("   and a.roleno = :roleno      ");
		this.de.setString("piid", piid);
		this.de.setString("orgno", orgno);
		this.de.setString("roleno", roleno);
		belongds = this.de.query();

		if (belongds.rowCount() > 0) {
			orgflag = belongds.getString(0, "orgflag");
		} else {
			orgflag = "";
		}
		if ("-".equals(orgflag)) {// 在账中存在
			de.clearSql();
  			de.addSql("delete from odssuws.rydjgdgxjstz_role ");
  			de.addSql(" where piid = :piid       ");
  			de.addSql("   and orgno = :orgno      ");
  			de.addSql("   and roleno = :roleno      ");
			this.de.setString("piid", piid);
			this.de.setString("orgno", orgno);
			this.de.setString("roleno", roleno);
			this.de.update();
		}

		return vdo;
	}

	/**
	 * 人员对机构的干系角色申请，下一步的操作，主要目的是验证有没有发生修改，没有修改不允许下一步
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-8-19
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveEmpOrgAdjust(final DataObject para) throws Exception {
		DataObject rdo = DataObject.getInstance();
  		de.clearSql();
		String piid;
		// 接受参数
		piid = para.getString("piid");

		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空！");
		}

		// sqlBF.append(" select 1 from odssuws.rydjgdgxjstz_role where piid = ? and orgflag is not null ");
		// this.sql.setSql(sqlBF.toString());
		// this.sql.setString(1, piid);
		//
		// DataStore changeds = this.sql.executeQuery();
		// if (changeds.rowCount() == 0) {
		// this.bizException("请修改人员对机构的干系角色后再进行保存！");
		// }

		de.clearSql();
  		de.addSql("update odssuws.rydjgdgxjstz  ");
  		de.addSql("   set reviewer =null,reviewtime = null,spyj = null,spsm = null  ");
  		de.addSql(" where piid = :piid    ");
		this.de.setString("piid", piid);
		int result = this.de.update();
		if (result < 1) {
			throw new BusinessException("更新工单信息失败，更新记录为0条,piid=" + piid);
		}

		return rdo;
	}

	/**
	 * 转向调整审批,查询机构和干系角色以及调整信息
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-08
	 * @since V1.0
	 */
	public final DataObject fwPageGxRoleApproval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		StringBuilder sqlBF = new StringBuilder();
		DataStore gdxxds = DataStore.getInstance(), empds = DataStore.getInstance(), dstmp, yjds = DataStore.getInstance();

		String spr = "", spyj = "", spsm = "";
		String sprq = null;

		String piid = para.getString("piid");

		de.clearSql();
  		de.addSql("select *	");
  		de.addSql("  from odssuws.rydjgdgxjstz   ");
  		de.addSql(" where piid = :piid	 		     ");
		this.de.setString("piid", piid);
		dstmp = this.de.query();
		String empno,empname;
		if (dstmp.rowCount() > 0) {
			empno = dstmp.getString(0, "empno");
			empname = OdssuUtil.getEmpNameByEmpno(empno);
		} else {
			throw new BusinessException("未取到工单信息：piid=" + piid);
		}

		spr = dstmp.getString(0, "reviewer");
		sprq = dstmp.getDateToString(0, "reviewtime", "yyyy-mm-dd");
		spyj = dstmp.getString(0, "spyj");
		spsm = dstmp.getString(0, "spsm");

		// 查询出变动的机构和干系角色信息
		de.clearSql();
  		de.addSql("select a.orgno,c.orgname, a.roleno,b.rolename, a.orgflag	");
  		de.addSql("  from odssuws.rydjgdgxjstz_role a, ");
  		de.addSql("       odssu.roleinfor b, ");
  		de.addSql("       odssu.orginfor c ");
  		de.addSql(" where a.piid = :piid ");
  		de.addSql("   and a.orgflag is not null ");
  		de.addSql("   and a.roleno = b.roleno ");
  		de.addSql("   and a.orgno = c.orgno ");
		this.de.setString("piid", piid);
		gdxxds = this.de.query();
		for (int i = 0; i < gdxxds.rowCount(); i++) {
			String orgflag = gdxxds.getString(i, "orgflag");
			String orgname = gdxxds.getString(i, "orgname");
		    String rolename = gdxxds.getString(i, "rolename");
			if (orgflag == null) {
				orgflag = "";
			}
			if (!"".equals(orgflag)) {
				orgflag = "(" + orgflag + ")";
			}
			gdxxds.put(i, "orgname", orgname);
			gdxxds.put(i, "rolename", rolename);
			gdxxds.put(i, "orgflag", orgflag);
		}

		empds.put(0, "piid", piid);
		empds.put(0, "empno", empno);
		empds.put(0, "empname", empname);
		String username = OdssuUtil.getUserNameByEmpno(empno);
		empds.put(0, "username", username);
		
		yjds.put(0, "spyj", spyj);
		yjds.put(0, "spsm", spsm);
		yjds.put(0, "spr", spr);
		yjds.put(0, "sprq", sprq);

		gdxxds = MultiSortUtil.multiSortDS(gdxxds, "orgflag:desc,orgname:asc,rolename:asc");
		vdo.put("empds", empds);
		vdo.put("gdxxds", gdxxds);
		vdo.put("yjds", yjds);
		empds = null;
		return vdo;
	}

	/**
	 * 审批界面暂存方法
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-10
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveGxRoleApproval(final DataObject para) throws Exception {

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
  		de.addSql(" update odssuws.rydjgdgxjstz ");
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
		this.de.setString("splbdm", "rydjgdgxjstz");
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
		this.de.setString("para2", "rydjgdgxjstz");
		this.de.setString("spyjdm", spyjdm);
		this.de.setString("para4", this.getUser().getUserid());
		this.de.setDateTime("sprq", sprq);
		this.de.setString("spsm", spsm);
		this.de.update();

		return vdo;
	}

	/**
	 * 人员对机构的干系角色申请界面，新增弹出窗口中：角色 Lov窗口
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-8-19
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject lovForRoleNoInfo(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();
		String roleno = para.getString("roleno");
		String orgno = para.getString("orgno");
		roleno = ((roleno == null || "".equals(roleno)) ? "%" : "%" + roleno + "%");
		String roleUpperCase = roleno.toUpperCase();
		roleUpperCase = ((roleUpperCase == null || "".equals(roleUpperCase)) ? "%" : "%" + roleUpperCase + "%");
		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
  		de.addSql(" select 1 from odssu.orginfor where orgno = :orgno and sleepflag = '0' ");
		this.de.setString("orgno", orgno);
		DataStore orgds = this.de.query();
		if (orgds.rowCount() == 0) {
			this.bizException("该机构已被注销！");
		}

		de.clearSql();
  		de.addSql(" select b.roleno, b.rolename, b.displayname, b.sleepflag");
  		de.addSql("   from (select a.belongorgno");
  		de.addSql("           from odssu.ir_org_closure a");
  		de.addSql("          where a.orgno = :orgno) temp,");
  		de.addSql("        odssu.roleinfor b");
  		de.addSql("  where b.deforgno = temp.belongorgno");
  		de.addSql("    and b.roletype in (select c.roletypeno roletype");
  		de.addSql("                         from odssu.ir_org_role_type c, ");
  		de.addSql("                              odssu.orginfor d");
  		de.addSql("                        where c.orgtypeno = d.orgtype");
  		de.addSql("                          and d.orgno = :orgno)");
  		de.addSql("    and b.rolenature = '5'");
  		de.addSql("    and b.sleepflag = '0'");
  		de.addSql("    and b.jsgn in ('1', '2')");
  		de.addSql("    and (b.roleno like :roleno or b.displayname like :roleno or rolename like :roleno or");
  		de.addSql("        upper(b.displaynamepy) like :roleuppercase or upper(rolenamepy) like :roleuppercase)");
  		de.addSql("  order by b.sleepflag, b.roleno ");
		this.de.setString("orgno", orgno);
		this.de.setString("orgno", orgno);
		this.de.setString("roleno", roleno);
		this.de.setString("roleno", roleno);
		this.de.setString("roleno", roleno);
		this.de.setString("roleuppercase", roleUpperCase);
		this.de.setString("roleuppercase", roleUpperCase);
		DataStore roleds = this.de.query();

		vdo.put("roleds", roleds);
		return vdo;
	}
	
	/**
	 * 描述：打开role列表烟台客户化-->只有人事隶属为市直的操作员可以调ods内部干系角色
	 * author: sjn
	 * date: 2018年6月20日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject lovForRoleNoInfo_3706(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();
		String roleno = para.getString("roleno");
		String orgno = para.getString("orgno");
		roleno = ((roleno == null || "".equals(roleno)) ? "%" : "%" + roleno + "%");
		String roleUpperCase = roleno.toUpperCase();
		roleUpperCase = ((roleUpperCase == null || "".equals(roleUpperCase)) ? "%" : "%" + roleUpperCase + "%");
		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
  		de.addSql(" select 1 from odssu.orginfor where orgno = :orgno and sleepflag = '0' ");
		this.de.setString("orgno", orgno);
		DataStore orgds = this.de.query();
		if (orgds.rowCount() == 0) {
			this.bizException("该机构已被注销！");
		}
		
		// 判断当前操作员人事隶属是否为市直
		de.clearSql();
  		de.addSql("select 1 ");
  		de.addSql("  from odssu.empinfor a, odssu.ir_org_closure b, odssu.orginfor c ");
  		de.addSql(" where a.hrbelong = b.orgno ");
  		de.addSql("   and b.belongorgno = c.orgno ");
  		de.addSql("   and c.orgtype = 'HSDOMAIN_DSRSJ' ");
  		de.addSql("   and a.empno = :empno ");
		this.de.setString("empno", this.getUser().getUserid());
		DataStore szvds = this.de.query();
		
		de.clearSql();
  		de.addSql(" select b.roleno, b.rolename, b.displayname, b.sleepflag");
  		de.addSql("   from (select a.belongorgno");
  		de.addSql("           from odssu.ir_org_closure a");
  		de.addSql("          where a.orgno = :orgno) temp,");
  		de.addSql("        odssu.roleinfor b");
  		de.addSql("  where b.deforgno = temp.belongorgno");
  		de.addSql("    and b.roletype in (select c.roletypeno roletype");
  		de.addSql("                         from odssu.ir_org_role_type c, ");
  		de.addSql("                              odssu.orginfor d");
  		de.addSql("                        where c.orgtypeno = d.orgtype");
  		de.addSql("                          and d.orgno = :orgno)");
  		de.addSql("    and b.rolenature = '5'");
  		de.addSql("    and b.sleepflag = '0'");
  		de.addSql("    and b.jsgn in ('1', '2')");
  		de.addSql("    and (b.roleno like :roleno or b.displayname like :roleno or rolename like :roleno or");
  		de.addSql("        upper(b.displaynamepy) like :roleuppercase or upper(rolenamepy) like :roleuppercase)");
		if (szvds == null || szvds.rowCount() <= 0) {
  			de.addSql("  and b.roletype not in ('ODS_DUTYDEF','ODS_ORGMANAGE','ODS_SYSMANAGE','ODS_WORK_DISPATCH') ");
		}
  		de.addSql("  order by b.sleepflag, b.roleno ");
		this.de.setString("orgno", orgno);
		this.de.setString("orgno", orgno);
		this.de.setString("roleno", roleno);
		this.de.setString("roleno", roleno);
		this.de.setString("roleno", roleno);
		this.de.setString("roleuppercase", roleUpperCase);
		this.de.setString("roleuppercase", roleUpperCase);
		DataStore roleds = this.de.query();
		
		vdo.put("roleds", roleds);
		return vdo;
	}

	/**
	 * 人员对机构的干系角色申请界面，新增弹出窗口中：机构 Lov窗口
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-8-19
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject lovForOrgNoInfo(final DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String orgnoUpperCase = orgno.toUpperCase();
		orgno = ((orgno == null || "".equals(orgno)) ? "%" : "%" + orgno + "%");
		orgnoUpperCase = ((orgnoUpperCase == null || "".equals(orgnoUpperCase)) ? "%" : "%" + orgnoUpperCase + "%");
		
		// 获取业务隶属机构编号
		BPO ibpo = this.newBPO(ProcessBPO.class);
		DataObject varvdo = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
		String lsjgid = varvdo.getString("_process_biz");
    		de.clearSql();
  		de.addSql(" select a.orgno , b.orgname ,b.displayname,b.sleepflag ");
  		de.addSql("  from odssu.ir_org_closure a , odssu.orginfor b ");
  		de.addSql(" where a.belongorgno = :lsjgid and b.orgno = a.orgno and  ");
  		de.addSql("   ( b.orgno like :orgno or b.orgname like :orgno or upper(b.orgnamepy) like :orgnouppercase or b.displayname like :orgno  ");
  		de.addSql("        or upper(b.displaynamepy) like :orgnouppercase or b.fullname like :orgno or upper(b.fullnamepy) like :orgnouppercase ) ");
  		de.addSql("  order by b.sleepflag,a.orgno");
		this.de.setString("lsjgid", lsjgid);
		this.de.setString("orgno", orgno);
		this.de.setString("orgno", orgno);
		this.de.setString("orgnouppercase", orgnoUpperCase);
		this.de.setString("orgno", orgno);
		this.de.setString("orgnouppercase", orgnoUpperCase);
		this.de.setString("orgno", orgno);
		this.de.setString("orgnouppercase", orgnoUpperCase);

		DataStore orgds = this.de.query();

		DataObject vdo = DataObject.getInstance();

		vdo.put("orgds", orgds);
		return vdo;
	}

}