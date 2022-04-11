package com.dw.hsuods.op;

import cn.hsa.cep.auc.dto.SysBizrolQueryDTO;
import cn.hsa.cep.auc.dto.SysUactDTO;
import cn.hsa.cep.auc.role.dto.BizrolDTO;
import cn.hsa.cep.auc.role.service.BizrolService;
import cn.hsa.cep.auc.service.SysBizrolService;
import cn.hsa.hsaf.core.framework.util.PageResult;
import cn.hsa.hsaf.core.framework.web.WrapperResponse;
import com.dareway.apps.odssu.OdssuContants;
import com.dareway.apps.odssu.OdssuNames;
import com.dareway.apps.process.ACBridge.ACBridge;
import com.dareway.apps.process.ACBridge.ACBridgeClient;
import com.dareway.apps.process.engine.SEWEngine;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dareway.spring.SpringBeanUtil;
import com.dw.log.LogManageAPI;
import com.dw.odssu.ws.emp.ryjbxxxzkjb.HttpJSONFrowardClient;
import com.dw.util.OdssuUtil;

import java.util.List;

public class HsuOdsOPBPO extends BPO{
	public DataObject getODSWorkDispatcherDeptMenuJson(DataObject para) throws AppException{
		String userId = this.getUser().getUserid();
  		de.clearSql();
  		de.addSql(" select distinct a.orgno,b.orgname ");
  		de.addSql("   from odssu.ir_emp_org_all_role a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where a.empno = :userid ");
  		de.addSql("    and a.orgno = b.orgno ");
  		de.addSql("    and (a.roleno = :roleno or a.roleno = :roleno_) ");
		this.de.setString("userid", userId);
		this.de.setString("roleno", OdssuContants.ROLE_ODS_WORK_DISPATCHER);
		this.de.setString("roleno_", OdssuContants.ROLE_ODS_WORK_DISPATCH_);
		StringBuffer jsonBuffer = new StringBuffer();
		DataStore orgVds = this.de.query();
		if(orgVds != null && orgVds.rowCount() !=0){
			for(int i = 0;i < orgVds.size(); i++){
				String orgno = orgVds.getString(i, "orgno");
				String orgname = orgVds.getString(i, "orgname");
				jsonBuffer.append(orgname+":goWorkDispatcherDept('"+orgno+"');");
			}
		}
		jsonBuffer.deleteCharAt(jsonBuffer.length()-1);
		DataObject result = DataObject.getInstance();
		result.put("menujson", jsonBuffer.toString());
		return result;
	}
	public DataObject getODSWorkDispatcherDept(DataObject para) throws AppException, BusinessException{
		String userId = this.getUser().getUserid();
  		de.clearSql();
  		de.addSql(" select distinct a.orgno ");
  		de.addSql("   from odssu.ir_emp_org_all_role a ");
  		de.addSql("  where a.empno = :userid ");
  		de.addSql("    and (a.roleno = :roleno or a.roleno = :roleno_) ");
		this.de.setString("userid", userId);
		this.de.setString("roleno", OdssuContants.ROLE_ODS_WORK_DISPATCHER);
		this.de.setString("roleno_", OdssuContants.ROLE_ODS_WORK_DISPATCH_);
		DataStore orgVds = this.de.query();
		if(orgVds == null || orgVds.rowCount() ==0){
			this.bizException("获取人员-机构关系时出错，DataStore为空。");
		}
		DataObject result = DataObject.getInstance();
		result.put("orgno", orgVds.getString(0, "orgno"));
		return result;
	}
	public DataObject oDSWorkDispatcherCountAndLsdgFlag(DataObject para) throws AppException{
		String userId = this.getUser().getUserid();
//  		de.clearSql();
//  		de.addSql(" select distinct a.orgno ");
//  		de.addSql("   from odssu.ir_emp_org_all_role a ");
//  		de.addSql("  where a.empno = :userid ");
//  		de.addSql("    and (a.roleno = :roleno or a.roleno = :roleno_) ");
//		this.de.setString("userid", userId);
//		this.de.setString("roleno", OdssuContants.ROLE_ODS_WORK_DISPATCHER);
//		this.de.setString("roleno_", OdssuContants.ROLE_ODS_WORK_DISPATCH_);
//		DataStore orgVds = this.de.query();
		de.clearSql();
  		de.addSql(" select csz ");
  		de.addSql("   from odssu.sys_para ");
  		de.addSql("  where csm = 'lsdg' ");
		DataStore lsdgParaVds = this.de.query();
		String lsdg = "false";
		if(lsdgParaVds != null && lsdgParaVds.size() > 0){
			lsdg = lsdgParaVds.getString(0, "csz");
		}
		DataObject result = DataObject.getInstance();

		result.put("workdispatchercount", 0);

		result.put("lsdg", lsdg);
		return result;
	}
	/**
	 * 通过roleno获取到oipLabel zwh  2019-12-17
	 */
	public DataObject getRoleManageOipLabel(DataObject para) throws Exception{
		String roleno = para.getString("roleno");
		String rolename = OdssuUtil.getRoleNameByRoleno(roleno);
		DataObject result = DataObject.getInstance();
		result.put("oiplabel", rolename);
		return result;
	}
	/**
	 * 方法简介：通过empno获取到 oipLabel
	 * 郑海杰  2015-7-13
	 */
	public DataObject getEmpOipLabel(DataObject para) throws Exception{
		String empno = para.getString("empno");
		String empname = OdssuUtil.getEmpNameByEmpno(empno);
		DataObject result = DataObject.getInstance();
		result.put("oiplabel", empname);
		return result;
	}
	/**
	 * 方法简介：通过orgno获取到 oipLabel
	 * 郑海杰  2015-7-13
	 */
	public DataObject getOrgOipLabel(DataObject para) throws Exception{
		String orgno = para.getString("orgno");
		String orgname = OdssuUtil.getOrgNameByOrgno(orgno);
		DataObject result = DataObject.getInstance();
		result.put("oiplabel", orgname);
		return result;
	}
	/**
	 * 方法简介：通过roleno获取到oipLabel
	 * 郑海杰  2015-7-13
	 */
	public DataObject getRoleOipLabel(DataObject para) throws Exception{
		String roleno = para.getString("roleno");
		String rolename = OdssuUtil.getRoleNameByRoleno(roleno);
		DataObject result = DataObject.getInstance();
		result.put("oiplabel", rolename);
		return result;
	}
	/**
	 *
	 * 方法简介.通过pdid获取到oipLabel
	 * @author fandq
	 * @date 创建时间 2015年8月14日
	 */
	public DataObject getPdOipLabel(DataObject para) throws Exception{
		String pdid = para.getString("pdid");
		String pdalias = OdssuUtil.getPdaliasByPdid(pdid);
		DataObject result = DataObject.getInstance();
		result.put("pdalias", pdalias);
		return result;
	}
	/**
	 *
	 * 方法简介.通过functionid获取到folderLabel
	 * @author fandq
	 * @date 创建时间 2015年8月14日
	 */
	public DataObject getFnOipLabel(DataObject para) throws Exception{
		String functionid = para.getString("functionid");

		//根据functionID从数据库中获取对应的folderlabel

		de.clearSql();
  		de.addSql(" select b.functionname               ");
  		de.addSql(" from odssu.appfunction b           ");
  		de.addSql(" where b.functionid = :functionid             ");
		this.de.setString("functionid", functionid);
		DataStore vds = this.de.query();
		if(vds == null || vds.rowCount() ==0){
			this.bizException("无法获取FunctionID为【"+functionid+"】的任务信息。");
		}
		String functionname = vds.getString(0, "functionname");

		DataObject result = DataObject.getInstance();

		result.put("oiplabel", functionname);
		return result;
	}

	public DataObject getRoleOIPB(DataObject para) throws Exception{
		DE de = DE.getInstance();
		DataObject vdo = DataObject.getInstance();
		String roleno = para.getString("roleno");

		//流程任务数
		de.clearSql();
		de.addSql(" select 1  ");
		de.addSql("   from odssu.dutyposition_task_role a ");
		de.addSql("  where a.roleno = :roleno ");
		de.setString("roleno", roleno);
		DataStore lcrwds = de.query();

		int lcrwcount = lcrwds.rowCount();
		//功能任务数
		de.clearSql();
		de.addSql(" select a.roleno, a.functionid, b.functionname ");
		de.addSql(" from odssu.role_function_manual a, odssu.appfunction b ");
		de.addSql(" where a.FUNCTIONID = b.FUNCTIONID ");
		de.addSql(" and a.ROLENO = :roleno ");
		de.setString("roleno", roleno);
		DataStore gnrwds = de.query();
		int gnrwcount = gnrwds.rowCount();
		//操作员数
		de.clearSql();
		de.addSql(" select DISTINCT a.empno, b.loginname, b.empname ");
		de.addSql(" from odssu.ir_emp_org_all_role a, odssu.empinfor b ");
		de.addSql(" where a.EMPNO = b.EMPNO and b.sleepflag='0' ");
		de.addSql(" and a.ROLENO = :roleno ");
		de.setString("roleno", roleno);
		DataStore empds = de.query();
		int empcount = empds.rowCount();
		for(int i = 0; i < empcount; i++) {
			String ORGNO = "", ORGNAME = "";
			String empno = empds.getString(i, "empno");
			de.clearSql();
			de.addSql(" select a.empno, a.orgno, c.orgname ");
			de.addSql(" from odssu.ir_emp_org_all_role a, odssu.orginfor c ");
			de.addSql(" where a.ORGNO = c.orgno ");
			de.addSql(" and a.ROLENO = :roleno and a.EMPNO = :empno ");
			de.setString("roleno", roleno);
			de.setString("empno", empno);
			DataStore jgds = de.query();
			int jgcount = jgds.rowCount();
			if(jgcount >= 1) {
				for(int j = 0; j < jgcount; j++) {
					if(j == 0) {
						ORGNO = jgds.getString(j, "orgno");
						ORGNAME = jgds.getString(j, "orgname");
					}else {
						ORGNO = ORGNO + "," + jgds.getString(j, "orgno");
						ORGNAME = ORGNAME + "," + jgds.getString(j, "orgname");
					}
				}
			}
			empds.put(i,"orano", ORGNO);
			empds.put(i,"orgname", ORGNAME);
		}

  		de.clearSql();
  		de.addSql(" select roleno, rolename, deforgno, folderno ");
  		de.addSql(" from odssu.roleinfor ");
  		de.addSql(" where roleno = :roleid ");
  		de.addSql(" and sleepflag = '0' ");
		de.setString("roleid", roleno);
		DataStore roleds = de.query();
		String rolename = "", deforgno = "",folderno="";
		if(roleds.rowCount() > 0) {
			rolename = roleds.getString(0, "rolename");
			deforgno = roleds.getString(0, "deforgno");
			folderno = roleds.getString(0, "folderno");
		}
		String typename = "";
  		de.clearSql();
  		de.addSql(" select b.TYPENO, b.typename ");
  		de.addSql(" from odssu.org_type b, odssu.role_orgtype c ");
  		de.addSql(" where b.TYPENO = c.orgtypeno ");
  		de.addSql(" and c.roleno = :roleid ");
  		de.setString("roleid", roleno);
		DataStore typeds = de.query();
		int typecount = typeds.rowCount();
		if(typecount >= 1) {
			for(int j = 0; j < typecount; j++) {
				if(j == 0) {
					typename = typeds.getString(j, "typename");
				}else {
					typename = typename + "," + typeds.getString(j, "typename");
				}
			}
		}
		//判断角色是否是内建角色
		de.clearSql();
		de.addSql("select 1 from odssu.roleinfor a where a.jsgn = '2' and a.roleno = :roleno  ");
		de.setString("roleno", roleno);
		DataStore isnjjsds = de.query();

		String isnjjs;
		if(isnjjsds == null || isnjjsds.rowCount() == 0) {
			isnjjs = "false";
		}else {
			isnjjs = "true";
		}

		vdo.put("gnrwds", gnrwds);
		vdo.put("lcrwds", lcrwds);
		vdo.put("empds", empds);
		vdo.put("gnrwcount", gnrwcount + "");
		vdo.put("lcrwcount", lcrwcount + "");
		vdo.put("empcount", empcount + "");
		vdo.put("rolename", rolename);
		vdo.put("typename", typename);
		vdo.put("deforgno", deforgno);
		vdo.put("folderno", folderno);
		vdo.put("isnjjs", isnjjs);
		return vdo;
	}

	public DataObject getRoleDpOIPB(DataObject para) throws Exception{
		DE de = DE.getInstance();
		DataObject vdo = DataObject.getInstance();
		String roleno = para.getString("roleno");
		String taskno = para.getString("taskno");
		taskno = ((taskno == null || "".equals(taskno)) ? "%" : "%" + taskno.toLowerCase() + "%");

		//流程任务数
		de.clearSql();
		de.addSql(" SELECT a.roleid, a.pdid, a.dptdid, b.pdlabel, c.dptdlabel  ");
		de.addSql(" FROM bpzone.dutyposition_task_role a, bpzone.process_define b, ");
		de.addSql(" 	 bpzone.dutyposition_task c  ");
		de.addSql(" WHERE a.PDID = b.PDID AND a.PDID = c.PDID AND a.DPTDID = c.DPTDID ");
		de.addSql(" AND a.TOCCODE = '-' AND c.TOCDMBH = '-'  ");
		de.addSql(" AND a.ROLEID = :roleno ");
		de.addSql(" AND (lower(a.pdid) like :taskno or lower(a.dptdid) like :taskno ");
		de.addSql("  or lower(b.pdlabel) like :taskno or lower(c.dptdlabel) like :taskno ) ");
		de.addSql(" ORDER BY a.ROLEID, a.PDID, a.DPTDID ");
		de.setString("roleno", roleno);
		de.setString("taskno", taskno);
		DataStore lcrwnocodeds = de.query();

		de.clearSql();
		de.addSql(" SELECT a.roleid, a.pdid, a.dptdid, b.pdlabel, c.dptdlabel||d.name dptdlabel ");
		de.addSql(" FROM bpzone.dutyposition_task_role a, bpzone.process_define b, ");
		de.addSql(" 	bpzone.dutyposition_task c, bpzone.syscode d ");
		de.addSql(" WHERE a.PDID = b.PDID AND a.PDID = c.PDID AND a.DPTDID = c.DPTDID ");
		de.addSql(" AND a.TOCCODE = d.value AND c.TOCDMBH = d.code  ");
		de.addSql(" AND a.ROLEID = :roleno ");
		de.addSql(" AND (lower(a.pdid) like :taskno or lower(a.dptdid) like :taskno ");
		de.addSql("  or lower(b.pdlabel) like :taskno or lower(c.dptdlabel) like :taskno ) ");
		de.addSql(" ORDER BY a.ROLEID, a.PDID, a.DPTDID ");
		de.setString("roleno", roleno);
		de.setString("taskno", taskno);
		DataStore lcrwcodeds = de.query();

		lcrwnocodeds.combineDatastore(lcrwcodeds);

		vdo.put("lcrwds", lcrwnocodeds);
		return vdo;
	}

	public DataObject getRoleFnOIPB(DataObject para) throws Exception{
		DE de = DE.getInstance();
		DataObject vdo = DataObject.getInstance();
		String roleno = para.getString("roleno");
		String functionid = para.getString("functionno");
		functionid = (functionid == null || "".equals(functionid)) ? "%" : "%" + functionid.toLowerCase() + "%";

		//功能任务数
		de.clearSql();
		de.addSql(" select distinct e.functionname,e.functionid  ");
		de.addSql("   from odssu.role_function_manual d, ");
		de.addSql("		   odssu.appfunction e ");
		de.addSql("  where d.roleno = :roleno ");
		de.addSql("	   and e.functionid = d.functionid ");
		de.addSql("	   and (lower(e.functionid) like :functionid or lower(e.functionname) like :functionid)");
		de.addSql("	   and d.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.addSql("	   and d.roleno in (select r.roleno from odssu.roleinfor r where r.sleepflag = '0') ");
		de.addSql("	 order by e.functionid ");
		de.setString("roleno", roleno);
		de.setString("functionid", functionid);
		DataStore gnrwds = de.query();

		vdo.put("gnrwds", gnrwds);
		return vdo;
	}
	public DataObject getRoleEmpOIPB(DataObject para) throws Exception{
		DE de = DE.getInstance();
		DataObject vdo = DataObject.getInstance();
		String roleno = para.getString("roleno");
		String czyno = para.getString("empno");
		czyno = (czyno == null || "".equals(czyno)) ? "%" : "%" + czyno.toLowerCase() + "%";

		//操作员数
		de.clearSql();
		de.addSql(" select DISTINCT a.empno, b.loginname, b.empname ");
		de.addSql(" from odssu.ir_emp_org_all_role a, odssu.empinfor b ");
		de.addSql(" where a.EMPNO = b.EMPNO and b.sleepflag='0' ");
		de.addSql(" and a.ROLENO = :roleno ");
		de.addSql(" AND (lower(a.empno) like :czyno or lower(b.loginname) like :czyno or lower(b.empname) like :czyno)");
		de.setString("roleno", roleno);
		de.setString("czyno", czyno);
		DataStore empds = de.query();
		int empcount = empds.rowCount();
		for(int i = 0; i < empcount; i++) {
			String ORGNO = "", ORGNAME = "";
			String empno = empds.getString(i, "empno");
			de.clearSql();
			de.addSql(" select a.empno, a.orgno, c.orgname ");
			de.addSql(" from odssu.ir_emp_org_all_role a, odssu.orginfor c ");
			de.addSql(" where a.ORGNO = c.orgno ");
			de.addSql(" and a.ROLENO = :roleno and a.EMPNO = :empno ");
			de.setString("roleno", roleno);
			de.setString("empno", empno);
			DataStore jgds = de.query();
			int jgcount = jgds.rowCount();
			if(jgcount >= 1) {
				for(int j = 0; j < jgcount; j++) {
					if(j == 0) {
						ORGNO = jgds.getString(j, "orgno");
						ORGNAME = jgds.getString(j, "orgname");
					}else {
						ORGNO = ORGNO + "," + jgds.getString(j, "orgno");
						ORGNAME = ORGNAME + "," + jgds.getString(j, "orgname");
					}
				}
			}
			empds.put(i,"orgno", ORGNO);
			empds.put(i,"orgname", ORGNAME);
		}

		vdo.put("empds", empds);
		return vdo;
	}

	/**
	 * 查询机构的基本信息
	 * @author zwh
	 * @date 2020-1-3
	 */
	public DataObject queryOrgBaseInfor(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String orgtype="", sleepflag = "", dbdjg = "";
		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
		DataObject vdo = DataObject.getInstance();

		//0.查询机构基本信息
  		de.clearSql();
  		de.addSql(" select o.orgno,o.orgname,o.displayname,o.fullname, ");
  		de.addSql("        o.sleepflag,o.orgtype,ot.typename,o.regionid, ");
  		de.addSql("        o.belongorgno,o1.orgname belongorgname,case when o.ywjbjgbz='0' then '×' when o.ywjbjgbz='1' then '√' end ywjbjgbz,o.jgtz,o.xzqhdm ");
  		de.addSql(" from   odssu.orginfor o left outer join odssu.orginfor o1 on o.belongorgno = o1.orgno ,");
  		de.addSql("        odssu.org_type ot ");
  		de.addSql(" where  o.orgno = :orgno  and o.orgtype = ot.typeno ");
		de.setString("orgno", orgno);
		DataStore orgbaseds = de.query();
		boolean isorgyxzj = false;//判断机构是否允许自建
		if (orgbaseds != null && orgbaseds.rowCount() > 0) {
			orgtype = orgbaseds.getString(0, "orgtype");
			sleepflag = orgbaseds.getString(0, "sleepflag");
			isorgyxzj = OdssuUtil.isOrgYxzj(orgtype);
		}
		vdo.put("orgds", orgbaseds);
		vdo.put("sleepflag", sleepflag);
		vdo.put("isOrgYxzj", isorgyxzj);

		//1.查询操作员信息
		de.clearSql();
  		de.addSql(" SELECT DISTINCT m.orgno, b.empname, b.empno, b.loginname username, m.ishrbelong ");
  		de.addSql(" FROM odssu.empinfor b, odssu.ir_emp_org m  ");
  		de.addSql(" WHERE b.sleepflag = '0' AND m.orgno = :orgno ");
  		de.addSql("  and m.empno = b.empno ");
  		de.addSql("order by b.empno");
		de.setString("orgno", orgno);
		DataStore zsempds = de.query();//直属操作员
		de.clearSql();
  		de.addSql(" SELECT DISTINCT a.orgno, b.empname, a.empno, b.loginname username, '-1' ishrbelong ");
  		de.addSql(" FROM odssu.ir_emp_org_all_role a, odssu.empinfor b ");
  		de.addSql(" WHERE a.empno = b.empno AND b.sleepflag = '0' AND a.orgno = :orgno ");
  		de.addSql(" AND not exists ( SELECT 1 FROM odssu.ir_emp_org m WHERE m.empno = a.empno AND a.orgno = m.orgno)   ");
  		de.addSql("  and a.roleno not in (select r.roleno from odssu.roleinfor r where r.sleepflag = '1') ");
  		de.addSql("  and a.roleno not in (select n.roleno from odssu.njjs_filter n )");
  		de.addSql("order by a.empno");
		de.setString("orgno", orgno);
		DataStore gxrds = de.query();//干系人
		gxrds.combineDatastore(zsempds);
		gxrds.sort("username");
		gxrds.sortdesc("ishrbelong");

		for(int i = 0; i < gxrds.rowCount(); i++) {
			String ishrbelong = gxrds.getString(i, "ishrbelong");
			if(ishrbelong.equals("1")) {
				ishrbelong = "人事隶属";
			}else if(ishrbelong.equals("0")) {
				ishrbelong = "隶属";
			}else {
				ishrbelong = "无隶属关系";
			}
			gxrds.put(i, "ishrbelong", ishrbelong);
			//查询对应的角色信息
			String empno = gxrds.getString(i, "empno");
			de.clearSql();
			de.addSql(" SELECT DISTINCT b.rolename, b.roleno ,b.rolesn");
			de.addSql(" FROM odssu.ir_emp_org_all_role a, odssu.roleinfor b ");
			de.addSql(" WHERE a.roleno = b.roleno AND b.sleepflag = '0' ");
			de.addSql(" 	AND a.empno = :empno AND a.orgno = :orgno ");
			de.addSql("  and b.roleno not in (select n.roleno from odssu.njjs_filter n )");
			de.addSql(" ORDER BY b.rolesn, b.roleno ");
			de.setString("empno", empno);
			de.setString("orgno", orgno);
			DataStore roleds = de.query();
			StringBuffer roleNameBF = new StringBuffer();
			roleNameBF.append("");
			for (int j = 0; j < roleds.size(); j++) {
				String roleName = roleds.getString(j, "rolename");
				roleNameBF.append(roleName + ",");
			}
			if (roleNameBF.length() > 0) {
				roleNameBF.deleteCharAt(roleNameBF.length() - 1);
			}
			gxrds.put(i, "rolename", roleNameBF.toString());
		}
		vdo.put("gxrds", gxrds);

		DataStore xxds = DataStore.getInstance();
		DataStore dwds = DataStore.getInstance();
        DataStore yyds = DataStore.getInstance();
        String dbd_lx ="";
        String interFaceName = "";

        de.clearSql();
        de.addSql("select dbd_lx, dbd_interface ");
        de.addSql("  from odssu.ir_dbd_interface a ");
        de.addSql("  where a.org_type = :orgtype ");
        this.de.setString("orgtype",orgtype);
        DataStore dbd_ds = this.de.query();
        if(dbd_ds.size()!=0) {
            dbd_lx = dbd_ds.getString(0, "dbd_lx");
            interFaceName = dbd_ds.getString(0, "dbd_interface");
        }
        String rsjno = "";
		//3.判断是否为代办点类的机构
        if(dbd_lx.equals("dbd_xx")){
            dbdjg = "1";
            //3.1学校类机构，下级学校信息
            //取单位代办点与xxid的关系
            de.clearSql();
            de.addSql("select xxid ");
            de.addSql("  from odssu.ir_dbd_xx a ");
            de.addSql("  where a.orgno = :orgno ");
            de.addSql("  order by xxid");
            this.de.setString("orgno",orgno);
            DataStore vds = this.de.query();
            //取单位代办点所属人社局
            rsjno = OdssuUtil.getBelongRsjNoByOrgNo(orgno);
            if(rsjno==null || "".equals(rsjno)) {
                throw new AppException("学校代办点["+rsjno+"]对应的人社局为空，请检查。");
            }
            //取人社局下所有的学校信息
            DataObject getxx_para = DataObject.getInstance();
            getxx_para.put("rsjid", rsjno);
            GetSubOrgForDBDInterface getXXInfo = (GetSubOrgForDBDInterface) SpringBeanUtil.getBean(interFaceName);
            DataStore allxx_ds = getXXInfo.getXXOrgList(orgno, rsjno);
            for(int i=0; i<vds.rowCount(); i++) {
                String xxid = vds.getString(i, "xxid");
                int locate_xx = allxx_ds.find("xxid == " + xxid);
                if (locate_xx >= 0) {
                    xxds.addRow(allxx_ds.get(locate_xx));
                    continue;
                }
                DataObject xxdo = vds.get(i);
                xxdo.put("xxbh", "");
                xxdo.put("xxmc", "");
                xxdo.put("txdz", "");
                xxds.addRow(xxdo);
            }
        }else if(dbd_lx.equals("dbd_dw")){
            dbdjg = "2";
            //3.2单位类机构，下级单位信息
            de.clearSql();
            de.addSql("select b.dwdjid, b.dwbh, b.cbdwmc ");
            de.addSql("  from odssu.ir_dbd_dw a, odsv.dw_view b ");
            de.addSql(" where a.dwdjid = b.dwdjid ");
            de.addSql("   and a.orgno = :orgno ");
            de.addSql("  order by b.dwbh");
            this.de.setString("orgno",orgno);
            dwds = this.de.query();
        }
        else if(dbd_lx.equals("dbd_yy")){
            dbdjg = "3";


            de.clearSql();
            de.addSql("select yyid ");
            de.addSql("  from odssu.ir_dbd_yy a ");
            de.addSql("  where a.orgno = :orgno ");
            de.addSql("  order by yyid");
            this.de.setString("orgno",orgno);
            DataStore vds = this.de.query();

            for(int i = 0; i<vds.rowCount(); i++) {
            	String yyid = vds.getString(i, "yyid");
                GetSubOrgForDBDInterface getYYInfo = (GetSubOrgForDBDInterface) SpringBeanUtil.getBean(interFaceName);
                DataStore yy_ds = getYYInfo.getYYOrgList(orgno, rsjno, yyid);
                DataObject yydo = DataObject.getInstance();
                yydo.put("yybh",yyid);
                yydo.put("yymc", yy_ds.getString(0, "yymc"));
                yydo.put("yygjjtybm", yy_ds.getString(0, "yygjjtybm"));
                yyds.addRow(yydo);

            }
        }
		vdo.put("xxds", xxds);
		vdo.put("dwds", dwds);
        vdo.put("yyds", yyds);
		vdo.put("dbdjg", dbdjg);
		return vdo;
	}
	public DataObject checkEmpUnderRole(DataObject para) throws AppException{
		String roleno = para.getString("roleno");
		DataObject result = DataObject.getInstance();
		DE de = DE.getInstance();
		de.addSql("select 1   ");
		de.addSql("  from odssu.ir_emp_org_all_role t ");
		de.addSql(" where t.roleno=:roleno");
		de.addSql("   and t.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("roleno", roleno);
		DataStore ds = de.query();
		if(ds.isEmpty()){
			result.put("flag", "false");
		}else{
			result.put("flag", "true");
		}
		return result;

	}
	public void delRole(DataObject para) throws AppException{
		String roleno = para.getString("roleno");
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql("select rolename from odssu.roleinfor t where t.roleno = :roleno ");
		de.setString("roleno",roleno );
		DataStore roleds = de.query();
		String rolename = roleds.getString(0, "rolename");

		ACBridge bridge = ACBridgeClient.getInstance(DE.getInstance(OdssuContants.getBPZoneDB()));
		DataStore lcrwds = bridge.getPDDPListByRoleid(roleno);

		DataStore loginfods = DataStore.getInstance();
		for(int i = 0;i<lcrwds.rowCount();i++) {
			DataObject temppd = DataObject.getInstance();
			temppd.put("roleno", roleno);
			temppd.put("rolename",rolename );
			temppd.put("pdid",lcrwds.getString(i, "pdid") );
			temppd.put("dptdid", lcrwds.getString(i, "dptdid"));
			temppd.put("pdlabel", lcrwds.getString(i, "pdlabel"));
			temppd.put("dptdlabel", lcrwds.getString(i, "dptdlabel"));
			temppd.put("toccode", lcrwds.getString(i, "toccode"));
			temppd.put("opflag", "0");

			loginfods.addRow(temppd);
		}

		de.clearSql();
		de.addSql(" select a.roleno, :rolename rolename ,                      ");
		de.addSql("       a.functionid, b.functionname, '0' opflag             ");
		de.addSql("   from odssu.role_function_manual a, odssu.appfunction b   ");
		de.addSql("  where a.functionid = b.functionid                         ");
		de.addSql("    and a.roleno = :roleno                                  ");
		de.setString("roleno",roleno );
		de.setString("rolename",rolename );
		DataStore logfnds = de.query();

		DataObject logdo = DataObject.getInstance();
		String pdid = "deleteRole";
		String managerno = this.getUser().getUserid();
		logdo.put("managerno",managerno);
		logdo.put("sprno",managerno);
		logdo.put("pdid",pdid);
		logdo.put("logroleds", loginfods);
		logdo.put("logfnds", logfnds);
		LogManageAPI LogAPI = new LogManageAPI();
		LogAPI.addAdjustRoleWithResourceLog(logdo);

		de.clearSql();
		de.addSql("delete from odssu.roleinfor t   where  t.roleno = :roleno ");
		de.setString("roleno", roleno);
		de.update();
		de.clearSql();
		de.addSql("delete from odssu.role_orgtype t where t.roleno= :roleno");
		de.setString("roleno", roleno);
		de.update();
		de.clearSql();
		de.addSql("delete from odssu.ir_role_closure t where t.roleno = :roleno ");
		de.setString("roleno", roleno);
		de.update();
		de.clearSql();
		de.addSql("delete from odssu.role_function_manual t where t.roleno =  :roleno ");
		de.setString("roleno", roleno);
		de.update();

		SEWEngine.getInstance().removeRole(roleno);


	}
	/**
	 * 方法简介 ：获取新增机构的办理机构个
	 */
	public DataObject getRoleBljg(DataObject para) throws AppException{
			DataObject result = DataObject.getInstance();
			String folderid = para.getString("folderid");

			de.clearSql();
			de.addSql("select a.deforgno from odssu.role_folder a where a.folderid = :folderid ");
			de.setString("folderid", folderid);
			DataStore vds = de.query();

			String bljgid = vds.getString(0, "deforgno");
			result.put("bljgid", bljgid);
			return result;
	}

	public DataObject checkRole(DataObject para) throws Exception{
		DataObject result = DataObject.getInstance();
		String xtgly = "0";
		String userid = this.getUser().getUserid();
		de.clearSql();
		de.addSql(" select 1 ");
		de.addSql("   from odssu.ir_emp_org_all_role ");
		de.addSql("  where empno = :userid ");
		de.addSql("    and orgno = :orgno ");
		de.addSql("    and roleno = :roleno ");
		de.setString("userid", userid);
		de.setString("orgno", OdssuContants.ORGROOT);
		de.setString("roleno", OdssuContants.ROLE_ODS_SYSADMIN);
		DataStore sysAdminVds = de.query();
		if(sysAdminVds != null && sysAdminVds.size() > 0){
			xtgly = "1";
		}

		result.put("xtgly", xtgly);
		return result;
	}
	public DataObject getFolderRsxt(DataObject para) throws Exception{

		String folderid = para.getString("folderid");

		de.clearSql();
		de.addSql("select a.deforgno from odssu.role_folder a where a.folderid = :folderid ");
		de.setString("folderid", folderid);
		DataStore vds = de.query();

		if(vds == null || vds.rowCount() == 0) {
			throw new AppException("获取目录信息失败！folderid为："+folderid);
		}
		String rsxtid = vds.getString(0, "deforgno");
		DataObject result = DataObject.getInstance();
		result.put("rsxtid", rsxtid);
		return result;
	}

	public DataObject checkModRoleTree(DataObject para) throws AppException{
			DataObject result = DataObject.getInstance();
			String folderid = para.getString("folderid");
			String sfwgml = "0";

			if(folderid.startsWith("rootkey") || folderid.startsWith("rootquery")) {
				sfwgml = "1";
			}else {
				de.clearSql();
				de.addSql("select 1 from odssu.role_folder a where a.collect_query = '1' and a.folderid = :folderid ");
				de.setString("folderid", folderid);
				DataStore vds = de.query();
				if(vds.rowCount() > 0) {
					sfwgml = "1";
				}
			}

			result.put("sfwgml", sfwgml);
			return result;
	}
	public DataObject saveModRoleTree(DataObject para) throws AppException,BusinessException{
		String roleno = para.getString("roleno");
		if(roleno == null || roleno.equals("")) {
			throw new AppException("未获取到roleno！");
		}
		String folderid = para.getString("folderid");
		if(folderid == null || folderid.equals("")) {
			throw new AppException("未获取到目录节点！");
		}

		de.clearSql();
		de.addSql(" select 1 ");
		de.addSql("   from odssu.emp_rolefolder a ");
		de.addSql("  where a.empno = :empno ");
		de.addSql("    and a.folderid in (select b.pfolderid ");
		de.addSql("                         from odssu.ir_rolefolder_closure b ");
		de.addSql("                        where b.folderid in (select c.folderno from odssu.roleinfor c ");
		de.addSql("                                              where c.roleno = :roleno) )             ");
		de.setString("empno", this.getUser().getUserid());
		de.setString("folderid", folderid);
		de.setString("roleno", roleno);
		DataStore oldfolderauthds = de.query();
		if(oldfolderauthds == null || oldfolderauthds.rowCount() == 0 ) {
			throw new BusinessException("您没有当前角色调整目录的权限！");
		}


		de.clearSql();
		de.addSql(" select 1 ");
		de.addSql("   from odssu.emp_rolefolder a ");
		de.addSql("  where a.empno = :empno ");
		de.addSql("    and a.folderid in (select b.pfolderid ");
		de.addSql("                         from odssu.ir_rolefolder_closure b ");
		de.addSql("                        where b.folderid = :folderid ) ");
		de.setString("empno", this.getUser().getUserid());
		de.setString("folderid", folderid);
		DataStore newfolderauthds = de.query();

		if(newfolderauthds == null || newfolderauthds.rowCount() == 0 ) {
			throw new BusinessException("您拥有为当前角色调整目录的权限，但缺少目标目录的权限！");
		}

		de.clearSql();
		de.addSql("update odssu.roleinfor set folderno = :folderno where roleno = :roleno ");
		de.setString("roleno", roleno);
		de.setString("folderno", folderid);
		de.update();

		return null;
	}

	public DataObject queryModRoleTree(DataObject para) throws AppException{
			DataObject result = DataObject.getInstance();
			String roleno = para.getString("roleno");
			String folderno = "";

			de.clearSql();
			de.addSql("select folderno from odssu.roleinfor where roleno = :roleno ");
			de.setString("roleno", roleno);
			DataStore vds = de.query();
			if(vds.rowCount() > 0) {
				folderno = vds.getString(0, "folderno");
			}

			result.put("folderno", folderno);
			return result;
	}

	public DataObject queryBizRole(DataObject para) throws AppException{
		DataObject result = DataObject.getInstance();
		de.clearSql();
		de.addSql("select bizroleid,bizrolecode,bizrolename,dscr from odssu.hsa_bizrole ");
		DataStore vds = de.query();
		result.put("bizroleList", vds);
		return result;
	}

	public DataObject queryOrgAndType(DataObject para) throws AppException{
		DataObject result = DataObject.getInstance();
		de.clearSql();
		de.addSql("select typeno,typename from odssu.org_type where typeno <> 'ODSSU_ORGROOT' ");
		DataStore vds = de.query();

		de.clearSql();
		de.addSql("select orgno,orgname from odssu.orginfor where orgtype in ('YB_DSYBXT','YB_SYBXT','YB_SZYBXT') ");
		DataStore vds2 = de.query();

		result.put("orgtypelist", vds);
		result.put("orglist", vds2);
		return result;
	}
	public DataObject saveHsaBizRole(DataObject para) throws AppException, BusinessException {
		String bizroleid = para.getString("bizroleid");
		String dscr = para.getString("dscr","");
		String bizrolecode = para.getString("bizrolecode");
		String bizrolename = para.getString("bizrolename");


		DataStore orgtypelist = para.getDataStore("orgtypelist");
		DataStore orglist = para.getDataStore("orglist");

		//校验角色是否已经存在
		de.clearSql();
		de.addSql("select 1 from odssu.hsa_bizrole where bizroleid = :bizroleid ");
		de.setString("bizroleid",bizroleid);
		DataStore vds = de.query();
		if(vds != null && vds.size() != 0){
			this.bizException("角色已经添加，无需重复添加。");
		}

		de.clearSql();
		de.addSql("insert into odssu.hsa_bizrole(bizroleid,bizrolecode,bizrolename,dscr) ");
		de.addSql("values(:bizroleid,:bizrolecode,:bizrolename,:dscr) ");
		de.setString("bizroleid",bizroleid);
		de.setString("bizrolecode",bizrolecode);
		de.setString("bizrolename",bizrolename);
		de.setString("dscr",dscr);
		de.update();

		de.clearSql();
		de.addSql("insert into odssu.hsa_bizrole_orgtype(bizroleid,typeno) ");
		de.addSql("values(:bizroleid,:typeno) ");
		de.setString("bizroleid",bizroleid);
		de.batchUpdate(orgtypelist,"typeno");

		de.clearSql();
		de.addSql("insert into odssu.hsa_bizrole_org(bizroleid,orgno) ");
		de.addSql("values(:bizroleid,:orgno) ");
		de.setString("bizroleid",bizroleid);
		de.batchUpdate(orglist,"orgno");

		return DataObject.getInstance();
	}
	public DataObject deleteHsaBizRole(DataObject para) throws AppException, BusinessException {
		String bizroleid = para.getString("bizroleid");

		de.clearSql();
		de.addSql("delete from odssu.hsa_bizrole where bizroleid = :bizroleid  ");
		de.setString("bizroleid",bizroleid);
		de.update();

		de.clearSql();
		de.addSql(" delete from odssu.hsa_bizrole_orgtype where bizroleid = :bizroleid  ");
		de.setString("bizroleid",bizroleid);
		de.update();

		de.clearSql();
		de.addSql(" delete from odssu.hsa_bizrole_org where bizroleid = :bizroleid  ");
		de.setString("bizroleid",bizroleid);
		de.update();
		return DataObject.getInstance();
	}
	public DataObject getHsaBizRoleList(DataObject para) throws AppException, BusinessException {
		String bizrolecode = para.getString("bizrolecode", "");

		if (bizrolecode == null || bizrolecode.equals("")) {
			this.bizException("角色编码不能为空，请根据角色编码查询。");
		}

		String dbid = OdssuNames.DBID;
		//当前的dbid对应的根节点
		if ("178".equals(dbid)) {
			BizrolService bizrolService = (BizrolService) SpringBeanUtil.getBean(BizrolService.class);
			WrapperResponse<List<BizrolDTO>> result = bizrolService.queryBizRoleByCodg(bizrolecode);
			String type = result.getType();
			DataObject resultdo = DataObject.getInstance();
			if (type.equals("success")) {
				DataStore resultds = DataStore.getInstance();
				List<BizrolDTO> bizrolDTOs = result.getData();
				for (int i = 0; i < bizrolDTOs.size(); i++) {
					BizrolDTO bizrolDTO = bizrolDTOs.get(i);
					resultds.put(i, "bizroleid", bizrolDTO.getBizRoleId());
					resultds.put(i, "bizrolecode", bizrolDTO.getRoleCodg());
					resultds.put(i, "bizrolename", bizrolDTO.getRoleName());
					resultds.put(i, "dscr", bizrolDTO.getDscr());
				}
				resultdo.put("biarolelov", resultds);
			} else {
				this.bizException("调用认证中心获取业务角色信息失败:" + result.getMessage());
			}
			return resultdo;
		}
		if ("189".equals(dbid)|| OdssuNames.nova_fsjz) {
			DataObject empdo = HttpJSONFrowardClient.invokeService("bizrolService", "queryBizRoleByCodg", bizrolecode);
			DataStore ds = DataStore.getInstance();
			DataObject vdo = DataObject.getInstance();
			if (empdo.get("type").equals("success")) {
				DataStore tsds = empdo.getDataStore("data");
				for (int i = 0; i < tsds.rowCount(); i++) {
					ds.put(i, "bizroleid", tsds.getString(i, "bizroleid"));
					ds.put(i, "bizrolecode", tsds.getString(i, "roleCodg"));
					ds.put(i, "bizrolename", tsds.getString(i, "roleName"));
					ds.put(i, "dscr", tsds.getString(i, "dscr"));
				}
				vdo.put("biarolelov", ds);
			} else {
				this.bizException("调用认证中心获取业务角色信息失败");
			}
			return vdo;
		}
		return null;
	}
	public DataObject getHsaBizRoleInfo(DataObject para) throws AppException, BusinessException {
		String bizroleid = para.getString("bizroleid","");
		String bizrolecode = para.getString("bizrolecode","");
		String bizrolename = para.getString("bizrolename","");
		DataObject resultdo = DataObject.getInstance();

		if(bizroleid == null || bizroleid.equals("")){
			throw new AppException("获取角色信息出错，角色ID不能为空。");
		}


		//获取角色下机构和机构类型
		de.clearSql();
		de.addSql("select wm_concat(a.orgname) orgno from odssu.orginfor a,odssu.hsa_bizrole_org b where a.orgno = b.orgno and b.bizroleid = :bizroleid");
		de.setString("bizroleid",bizroleid);
		DataStore vds = de.query();

		de.clearSql();
		de.addSql("select wm_concat(a.typename) orgtype from odssu.org_type a,odssu.hsa_bizrole_orgtype b where a.typeno = b.typeno and b.bizroleid = :bizroleid");
		de.setString("bizroleid",bizroleid);
		DataStore vds1 = de.query();

		DataStore bizroleinfo = DataStore.getInstance();
		bizroleinfo.put(0,"orgtype","");
		bizroleinfo.put(0,"orgno","");

		if(vds != null && vds.size() != 0){
			bizroleinfo.put(0,"orgno",vds.getString(0,"orgno"));
		}

		if(vds1 != null && vds1.size() != 0){
			bizroleinfo.put(0,"orgtype",vds1.getString(0,"orgtype"));
		}

		resultdo.put("bizroleinfo",bizroleinfo);


//		SysBizrolService sysBizrolService = (SysBizrolService)SpringBeanUtil.getBean(SysBizrolService.class);
//		SysBizrolQueryDTO sysBizrolQueryDTO = new SysBizrolQueryDTO();
//		sysBizrolQueryDTO.setBizRoleId(bizroleid);
//		sysBizrolQueryDTO.setRoleCodg(bizrolecode);
//		sysBizrolQueryDTO.setValiFlag("1");
//		sysBizrolQueryDTO.setRoleName(bizrolename);
//		WrapperResponse<PageResult<SysUactDTO>> result = sysBizrolService.getUsersByBusiRoleId(sysBizrolQueryDTO);
//
//		String type = result.getType();
//		if(type.equals("success")){
//			PageResult<SysUactDTO> pageResult = result.getData();
//			List<SysUactDTO> listUactDTO= pageResult.getData();
//
//			DataStore resultds = DataStore.getInstance();
//			for(int i = 0;i<listUactDTO.size();i++){
//				SysUactDTO uactDTO = listUactDTO.get(i);
//				resultds.put(i,"uact",uactDTO.getUact());
//				resultds.put(i,"uactid",uactDTO.getUactId());
//				resultds.put(i,"username",uactDTO.getUserName());
//				resultds.put(i,"orgname",uactDTO.getOrgName());
//				resultds.put(i,"orgCodg",uactDTO.getOrgCodg());
//				resultds.put(i,"admdvs",uactDTO.getAdmdvs());
//			}
//
//			resultdo.put("bizroleUserList",resultds);
			resultdo.put("bizroleUserList",DataStore.getInstance());
//		}else{
//			this.bizException("调用认证中心获取业务角色信息失败:"+result.getMessage());
//		}
		return resultdo;
	}
}
