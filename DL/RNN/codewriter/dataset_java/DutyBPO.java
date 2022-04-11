package com.dw.duty;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;
import com.dw.util.jxkh.PubUtil;
import com.dw.util.multiSortUtil.MultiSortUtil;

public class DutyBPO extends BPO{
	public DataObject getRoleName(DataObject para) throws AppException, BusinessException {
		String roleno = para.getString("roleno", "");
		String inorgno = para.getString("inorgno", "");
		String faceorgno = para.getString("faceorgno", "");
		String isoverall = para.getString("isoverall", "");
		String dutyno = para.getString("dutyno", "");
		String oiplabel = "";
		DataObject result = DataObject.getInstance();

		if (roleno == null || roleno.equals("")) {
			this.bizException("传入的角色编号为空，无法获取岗位名称。");
		}
		if (isoverall == null || isoverall.equals("")) {
			this.bizException("传入的岗位类型为空，无法获取岗位名称。");
		}
		de.clearSql();
		de.addSql(" select a.displayname,a.roletype,a.jsgn,a.sleepflag from odssu.roleinfor a where a.roleno = :roleno ");
		de.setString("roleno", roleno);
		DataStore rolenameds = de.query();
		if(rolenameds == null || rolenameds.rowCount() ==0){
			this.bizException("无法获取角色编号为【"+roleno+"】的角色信息。");
		}
		String rolename = rolenameds.getString(0, "displayname");
		String jsgn = rolenameds.getString(0, "jsgn");
		String sleepflag = rolenameds.getString(0, "sleepflag");
		if(sleepflag.equals("1")){
			this.bizException("该岗位已经删除，请刷新页面！");
		}
		// 全地市统一定义岗位以及人社局定义内岗
		if ((isoverall != null && isoverall.equals("1")&&inorgno.isEmpty()&&faceorgno.isEmpty()) || (inorgno == null || inorgno.equals(""))) {

			oiplabel = "统一定义\n" + rolename;
			result.put("oiplabel", oiplabel);
			return result;

		}

		// 本局以及地市实例化外岗
		if (jsgn.equals("3")) {
			//校验外岗是否存在
			de.clearSql();
			de.addSql("select 1 from odssu.outer_duty a where a.roleno = :roleno and a.faceorgno = :faceorgno and a.inorgno = :inorgno and dutyno = :dutyno ");
			de.setString("roleno", roleno);
			de.setString("faceorgno", faceorgno);
			de.setString("inorgno", inorgno);
			de.setString("dutyno", dutyno);
			DataStore tmpds = de.query();
			if(tmpds == null||tmpds.rowCount() == 0){
				this.bizException("岗位不存在或有多个隶属关系，无法跳转。");
			}
			
  			de.clearSql();
  			de.addSql(" select a.orgname from odssu.orginfor a where a.orgno = :faceorgno");
  			de.setString("faceorgno", faceorgno);
			DataStore orgds = de.query();
			if(orgds == null || orgds.rowCount() ==0){
				this.bizException("无法获取岗位编号为【"+faceorgno+"】的岗位信息。");
			}
			String orgname = orgds.getString(0, "orgname");
			oiplabel = orgname + "\n" + rolename;
		}

		// 内岗
		if (jsgn.equals("4")) {
  			de.clearSql();
  			de.addSql(" select a.orgname from odssu.orginfor a where a.orgno = :inorgno");
  			de.setString("inorgno", inorgno);
			DataStore orgds = de.query();
			if(orgds == null || orgds.rowCount() ==0){
				this.bizException("无法获取岗位编号为【"+inorgno+"】的岗位信息。");
			}
			String orgname = orgds.getString(0, "orgname");
			oiplabel = orgname + "\n" + rolename;
		}

		result.put("oiplabel", oiplabel);
		return result;
	}
	public DataObject getRoletype(DataObject para) throws AppException, BusinessException {
		String roleno = para.getString("roleno", "");
		DataObject result = DataObject.getInstance();
		String roletype = "";
		
		if (roleno == null || roleno.equals("")) {
			this.bizException("传入的角色编号为空，无法获取岗位名称。");
		}
		
		DataObject roleInfo = OdssuUtil.getRoleInforByRoleno(roleno);
		String deforgno = roleInfo.getString("deforgno");
		String jsgn = roleInfo.getString("jsgn");
		
		if (OdssuUtil.isOrgYxdymb(deforgno)) {
			if (jsgn.equals(OdssuContants.JSGN_OUTERDUTY)) {
				roletype = "overallOuterDuty";
			}else if(jsgn.equals(OdssuContants.JSGN_INNERDUTY)){
				roletype = "overallInnerDuty";
			}else {
				this.bizException("不是支持的岗位类型。");
			}
		}else {
			if (jsgn.equals(OdssuContants.JSGN_OUTERDUTY)) {
				roletype = "OuterDuty";
			}else if(jsgn.equals(OdssuContants.JSGN_INNERDUTY)){
				roletype = "InnerDuty";
			}else {
				this.bizException("不是支持的岗位类型。");
			}
		}
		
		result.put("roletype", roletype);
		return result;
	}

	public DataObject queryDuty(DataObject para) throws AppException, BusinessException {
		
		//当前dbid
		String dbid = GlobalNames.DEBUGMODE ? (String) this.getUser().getValue("dbid") : OdssuNames.DBID;
		// 当前的dbid对应的根节点

		de.clearSql();
		de.addSql("select b.orgno from odssu.ir_dbid_org a,odssu.orginfor b where dbid = :dbid and a.orgno = b.orgno ");
		de.setString("dbid", dbid);
		DataStore orgds = de.query();
		if (orgds == null || orgds.rowCount() == 0) {
			this.bizException("无法获取当前DBID对应的机构编号。");
		}
		DataObject result = DataObject.getInstance();
		DataStore resultds = DataStore.getInstance();
		for (int i =0 ;i<orgds.rowCount() ;i++) {
			String rootorgno = orgds.getString(i, "orgno");
			if (rootorgno == null || rootorgno.equals("")) {
				this.bizException("无法获取当前DBID对应的机构编号。");
			}
			para.put("rootorgno", rootorgno);
			
			// 从前台获取查询范围，1：全部；2：只查全地市定义岗位；3：只查本局岗位
			String allowOverall = para.getString("allowOverall", "1");
	
			if (allowOverall == null || allowOverall.equals("")) {
				allowOverall = "1";
			}
	
			DataStore innerdutyDs = DataStore.getInstance(), outerdutyDs = DataStore.getInstance(), overallOuterdutyDs = DataStore.getInstance(), overallInnerdutyDs = DataStore.getInstance();
	
				innerdutyDs = getInnerduty(para);
				outerdutyDs = getOuterduty(para);
	
			if (!allowOverall.equals("3")) {
				overallInnerdutyDs = getOverallInnerduty(para);
				overallOuterdutyDs = getOverallOuterduty(para);
			}
			
			DataStore outerDutyList = DataStore.getInstance();
			overallOuterdutyDs.combineDatastore(outerdutyDs);
			outerDutyList = overallOuterdutyDs.clone();
			outerDutyList.sort("comments_");
			outerDutyList.sort("rolename");
			outerDutyList.sortdesc("isoverall");
			
			DataStore innerDutyList = DataStore.getInstance();
			overallInnerdutyDs.combineDatastore(innerdutyDs);
			innerDutyList = overallInnerdutyDs.clone();
			innerDutyList.sort("comments_");
			innerDutyList.sort("rolename");
			innerDutyList.sortdesc("isoverall");
			
			outerDutyList.combineDatastore(innerDutyList);
			resultds.combineDatastore(outerDutyList);
			
			outerDutyList = null;
			innerDutyList = null;
			overallInnerdutyDs = null;
			overallOuterdutyDs = null;
			innerDutyList = null;
			outerDutyList = null;						
		}
		result.put("dutyds",resultds);
		return result;
	}
	
	public DataObject queryQyqkSub(DataObject para) throws AppException {
		String roletype=para.getString("roletype");
		String roleno=para.getString("roleno");
		String deforgno = para.getString("deforgno");
  		  		de.clearSql();
		de.clearSql();
  		de.addSql("select a.orgno, a.orgname ");
  		de.addSql("  from odssu.orginfor a, odssu.ir_org_closure b ");
  		de.addSql(" where a.orgno = b.orgno ");
  		de.addSql("   and a.orgtype in ( select orgtypeno from odssu.ir_org_role_type  where roletypeno = :roletype) ");
  		de.addSql("   and b.belongorgno = :deforgno ");
		this.de.setString("roletype", roletype);
		this.de.setString("deforgno", deforgno);
		DataStore orgds = de.query();
		if ( null == orgds || orgds.rowCount() == 0) {
			return null;
		}
		
		for (int rowno = 0; rowno < orgds.rowCount();rowno++) {
			String orgno = orgds.getString(rowno, "orgno");
  			de.clearSql();
  			de.addSql(" select count(empno) zrs from  odssu.ir_emp_org where orgno= :orgno ");
			this.de.setString("orgno", orgno);
			DataStore dstemp = de.query();
			if(dstemp != null && dstemp.rowCount()!=0){
				String zrs = String.valueOf(dstemp.getInt(0, "zrs"));
				orgds.put(rowno, "zrs", zrs);
			}
  			de.clearSql();
  			de.addSql(" select count(empno) qyrs from odssu.emp_inner_duty where orgno = :orgno and roleno = :roleno ");
			this.de.setString("orgno", orgno);
			this.de.setString("roleno", roleno);
			dstemp = de.query();
			if(dstemp != null && dstemp.rowCount()!=0){
				String qyrs = String.valueOf(dstemp.getInt(0, "qyrs"));
				orgds.put(rowno, "qyrs", qyrs);
				if ("0".equals(qyrs))
					orgds.put(rowno, "qy", "0");
				else
					orgds.put(rowno, "qy", "1");
			}
		}
		String bz= "共"+orgds.rowCount()+"个下属机构适用该岗位。";
		
		orgds.multiSort("qy:desc,orgname:asc");
		
		DataObject result = DataObject.getInstance();
		result.put("orgds", orgds);
		result.put("bz", bz);
		return result;
	}
	public DataObject queryQyqk(DataObject para) throws AppException {
		String roletype=para.getString("roletype");
		String roleno=para.getString("roleno");
		String deforgno = para.getString("deforgno");
		String orgtype = null;
		if("HS_RSSNPGWL".equals(roletype)){			//人社所内配岗位类 - 人社所
			orgtype ="HSDOMAIN_SBS";
		}else if("HS_RSZNPGWL".equals(roletype)){	//人社站内配岗位类 --人社站
			orgtype ="HSDOMAIN_SBZ";
		}else if("HS_RSJKSNPGWL".equals(roletype)){	//人社局科室内配岗位类--处、科室
			orgtype ="HSDOMAIN_RSCKS";
		}else{
			throw new AppException("岗位类别 不是乡镇街道、村社区、处科室!");
		}
		
  		de.addSql("select a.orgno, a.orgname ");
  		de.addSql("  from odssu.orginfor a, odssu.ir_org_closure b ");
  		de.addSql(" where a.orgno = b.orgno ");
  		de.addSql("   and (a.orgtype = 'HSDOMAIN_QXRSJ' or   a.orgtype = 'HSDOMAIN_DSRSJ') ");
  		de.addSql("   and b.belongorgno = :deforgno ");
  		de.addSql(" order by a.orgno desc ");
		this.de.setString("deforgno", deforgno);
		DataStore orgds = de.query();
		if ( null == orgds || orgds.rowCount() == 0) {
			return null;
		}
		for (int rowno = 0; rowno < orgds.rowCount();rowno++) {
			String belongorgno = orgds.getString(rowno, "orgno");
  			de.clearSql();
  			de.addSql(" select count(b.orgno) zsm ");
  			de.addSql("from odssu.orginfor a, odssu.ir_org_closure b ");
  			de.addSql("where a.orgno = b.orgno   ");
  			de.addSql("and a.orgtype = :orgtype  ");
  			de.addSql("and b.belongorgno = :belongorgno ");
			this.de.setString("orgtype", orgtype);
			this.de.setString("belongorgno", belongorgno);
			DataStore dstemp = de.query();
			if(dstemp != null && dstemp.rowCount()!=0){
				String zsm = String.valueOf(dstemp.getInt(0, "zsm"));
				orgds.put(rowno, "zsm", zsm);
			}
			
  			de.clearSql();
  			de.addSql("    select count(b.orgno) qysm ");
  			de.addSql(" from odssu.orginfor a, odssu.ir_org_closure b ");
  			de.addSql(" where a.orgno = b.orgno   and a.orgtype = :orgtype ");
  			de.addSql("  and b.belongorgno = :belongorgno  and exists(select 1 ");
  			de.addSql(" from odssu.emp_inner_duty c ");
  			de.addSql(" where c.orgno = a.orgno ");
  			de.addSql(" and roleno = :roleno )  ");
			this.de.setString("orgtype", orgtype);
			this.de.setString("belongorgno", belongorgno);
			this.de.setString("roleno", roleno);
			dstemp = de.query();
			if(dstemp != null && dstemp.rowCount()!=0){
				String qysm = String.valueOf(dstemp.getInt(0, "qysm"));
				orgds.put(rowno, "qysm", qysm);
			}
		}
		DataObject result = DataObject.getInstance();
		result.put("orgds", orgds);
		return result;
	}
	private DataStore getOuterduty(DataObject para) throws AppException {
		String allowOverall = para.getString("allowOverall", "1");
		String dutyName = para.getString("dutyname","");
		String inorgno = para.getString("inorgno","");
		String faceorgno = para.getString("faceorgno","");
		String pdid = para.getString("pdid","");
		String dptdid = para.getString("dptdid","");
		String functionid = para.getString("functionid","");
		
		dutyName = "%"+dutyName.toUpperCase()+"%";
		String rootorgno = para.getString("rootorgno");
		
		de.clearSql();
  		de.addSql("select distinct c.yxdymb isoverall, ");
  		de.addSql("       a.roleno, ");
  		de.addSql("       a.deforgno, ");
  		de.addSql("       a.rolename, ");
  		de.addSql("       b.inorgno, ");
  		de.addSql("       b.dutyno, ");
  		de.addSql("       b.faceorgno, ");
  		de.addSql("       e.orgname   deforg, ");
  		de.addSql("       f.orgname   inorg, ");
  		de.addSql("       g.orgname   faceorg, ");
  		de.addSql("       d.typename comments_ ");
		//如果是烟台，则展示自动继承改岗位的职务
		String dbid = GlobalNames.DEBUGMODE ? (String) this.getUser().getValue("dbid") : OdssuNames.DBID;
		if (dbid != null && dbid.equals("105")) {
  			de.addSql("       ,(select listagg(q.rolename, ',' ) within group ( order by q.roleno ) duty ");
  			de.addSql("         from odssu.ir_duty_adapt_role p, odssu.roleinfor q ");
  			de.addSql("         where a.roleno = p.dutyno ");
  			de.addSql("         and p.postno = q.roleno) duty ");
		}
  		de.addSql("  from odssu.roleinfor  a, ");
  		de.addSql("       odssu.outer_duty b, ");
  		de.addSql("       odssu.orginfor   e, ");
  		de.addSql("       odssu.orginfor   f, ");
  		de.addSql("       odssu.orginfor   g, ");
  		de.addSql("       odssu.org_type c,odssu.role_type d ");
  		de.addSql(" where a.roleno = b.roleno ");
  		de.addSql("   and a.deforgno = e.orgno ");
  		de.addSql("   and e.orgtype = c.typeno ");
  		de.addSql("   and a.roletype = d.typeno ");
  		de.addSql("   and a.sleepflag = '0' ");
  		de.addSql("   and b.inorgno = f.orgno ");
  		de.addSql("   and b.faceorgno = g.orgno ");
  		de.addSql("   and (upper(a.rolename) like :dutyname or upper(a.displayname) like :dutyname or ");
  		de.addSql("       upper(a.rolenamepy) like :dutyname or upper(a.displaynamepy) like :dutyname) ");
  		de.addSql("   and exists (select 1 from odssu.ir_org_closure h where h.orgno = a.deforgno and h.belongorgno = :rootorgno)");
		
		if (allowOverall.equals("2")) {
  			de.addSql("   and c.yxdymb = '1' ");
		} else if (allowOverall.equals("3")) {
  			de.addSql("   and c.yxdymb = '0' ");
		}
		
		if (inorgno != null && !inorgno.equals("")) {
  			de.addSql("   and b.inorgno = :inorgno ");
		}
		if (faceorgno != null && !faceorgno.equals("")) {
  			de.addSql("   and b.faceorgno = :faceorgno ");
		}
		
		if (pdid != null && !pdid.equals("") && dptdid != null && !dptdid.equals("")) {
  			de.addSql("   and exists (select 1 ");
  			de.addSql("          from bpzone.dutyposition_task_role m ");
  			de.addSql("         where a.roleno = m.roleid ");
  			de.addSql("           and m.pdid = :pdid ");
  			de.addSql("           and m.dptdid = :dptdid) ");
		}
		if (!functionid.isEmpty()) {
  			de.addSql("   and exists (select 1 ");
  			de.addSql("          from odssu.role_function_manual n ");
  			de.addSql("         where a.roleno = n.roleno ");
  			de.addSql("           and n.functionid = :functionid )");
		};
		de.setString("dutyname", dutyName);
		de.setString("rootorgno", rootorgno);
		if (inorgno != null && !inorgno.equals("")) {
			de.setString("inorgno", inorgno);
		}
		if (faceorgno != null && !faceorgno.equals("")) {
			de.setString("faceorgno", faceorgno);
		}
		if (pdid != null && !pdid.equals("") && dptdid != null && !dptdid.equals("")) {
			de.setString("pdid", pdid);
			de.setString("dptdid", dptdid);
		}
		if (!functionid.isEmpty()) {
			de.setString("functionid", functionid);
		}
		
		DataStore dstemp = de.query();
		
		return dstemp;
	}

	private DataStore getInnerduty(DataObject para) throws AppException {
		String rootorgno = para.getString("rootorgno");
		String allowOverall = para.getString("allowOverall", "1");
		String dutyName = para.getString("dutyname","");
		String inorgno = para.getString("inorgno","");
		String pdid = para.getString("pdid","");
		String dptdid = para.getString("dptdid","");
		String functionid = para.getString("functionid","");
		dutyName = "%"+dutyName.toUpperCase()+"%";
  		de.clearSql();
		String faceorgno = para.getString("faceorgno","");
		if (faceorgno != null && !faceorgno.equals("")) {
			return DataStore.getInstance();
		}
		
		//如果传入的inorg为空，只查询本局定义的抽象内岗
		if (inorgno == null || inorgno.equals("")) {
			
			if (allowOverall.equals("2")) {
				return DataStore.getInstance();
			}
			de.clearSql();
  			de.addSql("select distinct a.roleno, ");
  			de.addSql("       b.yxdymb isoverall, ");
  			de.addSql("       a.deforgno, ");
  			de.addSql("       a.rolename, ");
  			de.addSql("       null inorgno, ");
  			de.addSql("       null faceorgno, ");
  			de.addSql("       null dutyno, ");
  			de.addSql("       e.orgname deforg, ");
  			de.addSql("       null inorg, ");
  			de.addSql("       null faceorg, ");
  			de.addSql("       c.typename comments_ ");
  			de.addSql("  from odssu.roleinfor a, odssu.orginfor e,odssu.org_type b,odssu.role_type c  ");
  			de.addSql(" where a.deforgno = e.orgno ");
  			de.addSql("   and a.jsgn = '4' ");
  			de.addSql("   and a.sleepflag = '0' ");
  			de.addSql("   and a.roletype = c.typeno ");
  			de.addSql("   and e.orgtype = b.typeno ");
  			de.addSql("   and b.yxdyng = '1' ");
  			de.addSql("   and (upper(a.rolename) like :dutyname or upper(a.displayname) like :dutyname or ");
  			de.addSql("       upper(a.rolenamepy) like :dutyname or upper(a.displaynamepy) like :dutyname) ");
  			de.addSql("   and exists (select 1 from odssu.ir_org_closure h where h.orgno = a.deforgno and h.belongorgno = :rootorgno)");
			if (pdid != null && !pdid.equals("") && dptdid != null && !dptdid.equals("")) {
  				de.addSql("   and exists (select 1 ");
  				de.addSql("          from bpzone.dutyposition_task_role f ");
  				de.addSql("         where a.roleno = f.roleid ");
  				de.addSql("           and f.pdid = :pdid ");
  				de.addSql("           and f.dptdid = :dptdid )");
			}
			if (!functionid.isEmpty()) {
  				de.addSql("   and exists (select 1 ");
  				de.addSql("          from odssu.role_function_manual d ");
  				de.addSql("         where a.roleno = d.roleno ");
  				de.addSql("           and d.functionid = :functionid )");
			}
			de.setString("dutyname", dutyName);
			de.setString("rootorgno", rootorgno);
			if (pdid != null && !pdid.equals("") && dptdid != null && !dptdid.equals("")) {
				de.setString("pdid", pdid);
				de.setString("dptdid", dptdid);
			}
			if (!functionid.isEmpty()) {
				de.setString("functionid", functionid);
			}
			DataStore dstemp = de.query();
			
			return dstemp;
		}
		
		//查询内岗(实例化到具体的机构的)
		de.clearSql();
  		de.addSql("select distinct a.roleno, ");
  		de.addSql("       c.yxdymb isoverall, ");
  		de.addSql("       a.deforgno, ");
  		de.addSql("       a.rolename, ");
  		de.addSql("       b.orgno inorgno, ");
  		de.addSql("       null faceorgno, ");
  		de.addSql("       null dutyno, ");
  		de.addSql("       e.orgname deforg, ");
  		de.addSql("       b.orgname inorg, ");
  		de.addSql("       null faceorg, ");
  		de.addSql("       d.typename comments_ ");
  		de.addSql("  from odssu.roleinfor  a, ");
  		de.addSql("       odssu.inner_duty b, ");
  		de.addSql("       odssu.orginfor   e, ");
  		de.addSql("       odssu.orginfor   f, ");
  		de.addSql("       odssu.org_type c,odssu.role_type d ");
  		de.addSql(" where a.deforgno = e.orgno ");
  		de.addSql("   and a.roleno = b.roleno ");
  		de.addSql("   and a.roletype = d.typeno ");
  		de.addSql("   and b.orgno = f.orgno ");
  		de.addSql("   and c.typeno = e.orgtype ");
  		de.addSql("   and a.sleepflag = '0' ");
  		de.addSql("   and (upper(a.rolename) like :dutyname or upper(a.displayname) like :dutyname or ");
  		de.addSql("       upper(a.rolenamepy) like :dutyname or upper(a.displaynamepy) like :dutyname) ");
  		de.addSql("   and exists (select 1 from odssu.ir_org_closure h where h.orgno = a.deforgno and h.belongorgno = :rootorgno) ");
		if (allowOverall.equals("2")) {
  			de.addSql("   and c.yxdymb = '1' ");
		} else if (allowOverall.equals("3")) {
  			de.addSql("   and c.yxdymb = '0' ");
		}
		
		if (inorgno != null && !inorgno.equals("")) {
  			de.addSql("   and b.orgno = :inorgno ");
		}
		if (pdid != null && !pdid.equals("") && dptdid != null && !dptdid.equals("")) {
  			de.addSql("   and exists (select 1 ");
  			de.addSql("          from bpzone.dutyposition_task_role g ");
  			de.addSql("         where a.roleno = g.roleid ");
  			de.addSql("           and g.pdid = :pdid");
  			de.addSql("           and g.dptdid = :dptdid )");
		}
		if (!functionid.isEmpty()) {
  			de.addSql("   and exists (select 1 ");
  			de.addSql("          from odssu.role_function_manual h ");
  			de.addSql("         where a.roleno = h.roleno ");
  			de.addSql("           and h.functionid = :functionid) ");
		};
		de.setString("dutyname", dutyName);
		de.setString("rootorgno", rootorgno);
		if (inorgno != null && !inorgno.equals("")) {
  			de.setString("inorgno", inorgno);
		}
		if (pdid != null && !pdid.equals("") && dptdid != null && !dptdid.equals("")) {
			de.setString("pdid", pdid);
			de.setString("dptdid", dptdid);
		}
		if (!functionid.isEmpty()) {
			de.setString("functionid", functionid);
		}
		DataStore dstemp = de.query();
		
		return dstemp;
	}

	private DataStore getOverallOuterduty(DataObject para) throws AppException {
		String dutyName = para.getString("dutyname","");
		String pdid = para.getString("pdid","");
		String dptdid = para.getString("dptdid","");
		String functionid = para.getString("functionid","");
		dutyName = "%"+dutyName.toUpperCase()+"%";
		String rootorgno = para.getString("rootorgno");
		
		String inorgno = para.getString("inorgno","");
		String faceorgno = para.getString("faceorgno","");
		if (faceorgno != null && !faceorgno.equals("")) {
			return DataStore.getInstance();
		}
		if (inorgno != null && !inorgno.equals("")) {
			return DataStore.getInstance();
		}

		de.clearSql();
  		de.addSql("select distinct a.roleno, ");
  		de.addSql("       '1' isoverall, ");
  		de.addSql("       a.deforgno, ");
  		de.addSql("       a.rolename, ");
  		de.addSql("       null inorgno, ");
  		de.addSql("       null faceorgno, ");
  		de.addSql("       null dutyno, ");
  		de.addSql("       b.orgname   deforg, ");
  		de.addSql("       null   inorg, ");
  		de.addSql("       null   faceorg, ");
  		de.addSql("       d.typename comments_ ");
		//如果是烟台，则展示自动继承改岗位的职务
		String dbid = GlobalNames.DEBUGMODE ? (String) this.getUser().getValue("dbid") : OdssuNames.DBID;
		if (dbid != null && dbid.equals("105")) {
  			de.addSql("       ,(select listagg(q.rolename, ',' ) within group ( order by q.roleno ) duty ");
  			de.addSql("         from odssu.ir_duty_adapt_role p, odssu.roleinfor q ");
  			de.addSql("         where a.roleno = p.dutyno ");
  			de.addSql("         and p.postno = q.roleno) duty ");
		}
  		de.addSql("  from odssu.roleinfor a, odssu.orginfor b,odssu.org_type c,odssu.role_type d ");
  		de.addSql(" where a.jsgn = '3' ");
  		de.addSql("   and a.deforgno = b.orgno ");
  		de.addSql("   and a.sleepflag = '0' ");
  		de.addSql("   and b.orgtype = c.typeno ");
   		de.addSql("   and a.roletype = d.typeno ");
   		de.addSql("   and c.yxdymb = '1' ");
  		de.addSql("   and (upper(a.rolename) like :dutyname or upper(a.displayname) like :dutyname or ");
  		de.addSql("       upper(a.rolenamepy) like :dutyname or upper(a.displaynamepy) like :dutyname) ");
  		de.addSql("   and exists (select 1 from odssu.ir_org_closure h where h.orgno = a.deforgno and h.belongorgno = :rootorgno) ");
		if (pdid != null && !pdid.equals("") && dptdid != null && !dptdid.equals("")) {
  			de.addSql("   and exists (select 1 ");
  			de.addSql("          from bpzone.dutyposition_task_role e ");
  			de.addSql("         where a.roleno = e.roleid ");
  			de.addSql("           and e.pdid = :pdid");
  			de.addSql("           and e.dptdid = :dptdid )");
		}
		if (!functionid.isEmpty()) {
  			de.addSql("   and exists (select 1 ");
  			de.addSql("          from odssu.role_function_manual f ");
  			de.addSql("         where a.roleno = f.roleno ");
  			de.addSql("           and f.functionid = :functionid )");
		}
		de.setString("dutyname", dutyName);
		de.setString("rootorgno", rootorgno);
		if (pdid != null && !pdid.equals("") && dptdid != null && !dptdid.equals("")) {
			de.setString("pdid", pdid);
			de.setString("dptdid", dptdid);
		}
		if (!functionid.isEmpty()) {
			de.setString("functionid", functionid);
		}
		DataStore dstemp = de.query();
		return dstemp;
	}

	private DataStore getOverallInnerduty(DataObject para) throws AppException {
		String dutyName = para.getString("dutyname","");
		String inorgno = para.getString("inorgno","");
		String pdid = para.getString("pdid","");
		String dptdid = para.getString("dptdid","");
		String functionid = para.getString("functionid","");
		dutyName = "%"+dutyName.toUpperCase()+"%";
		String faceorgno = para.getString("faceorgno","");
		String rootorgno = para.getString("rootorgno");
		if (faceorgno != null && !faceorgno.equals("")) {
			return DataStore.getInstance();
		}
		if (inorgno != null && !inorgno.equals("")) {
			return DataStore.getInstance();
		}
  		  		de.clearSql();
		de.clearSql();
  		de.addSql("select distinct a.roleno, ");
  		de.addSql("       '1' isoverall, ");
  		de.addSql("       a.deforgno, ");
  		de.addSql("       a.rolename, ");
  		de.addSql("       null inorgno, ");
  		de.addSql("       null faceorgno, ");
  		de.addSql("       b.orgname   deforg, ");
  		de.addSql("       null   inorg, ");
  		de.addSql("       null   faceorg, ");
  		de.addSql("       d.typename comments_ ");
  		de.addSql("  from odssu.roleinfor a, odssu.orginfor b,odssu.org_type c,odssu.role_type d ");
  		de.addSql(" where a.jsgn = '4' ");
  		de.addSql("   and a.deforgno = b.orgno ");
  		de.addSql("   and a.sleepflag = '0' ");
  		de.addSql("   and b.orgtype = c.typeno ");
   		de.addSql("   and a.roletype = d.typeno ");
   		de.addSql("   and c.yxdymb = '1' ");
  		de.addSql("   and (upper(a.rolename) like :dutyname or upper(a.displayname) like :dutyname or ");
  		de.addSql("       upper(a.rolenamepy) like :dutyname or upper(a.displaynamepy) like :dutyname) ");
  		de.addSql("   and exists (select 1 from odssu.ir_org_closure h where h.orgno = a.deforgno and h.belongorgno = :rootorgno)");
		if (pdid != null && !pdid.equals("") && dptdid != null && !dptdid.equals("")) {
  			de.addSql("   and exists (select 1 ");
  			de.addSql("          from bpzone.dutyposition_task_role e ");
  			de.addSql("         where a.roleno = e.roleid ");
  			de.addSql("           and e.pdid = :pdid");
  			de.addSql("           and e.dptdid = :dptdid )");
		}
		if (!functionid.isEmpty()) {
  			de.addSql("   and exists (select 1 ");
  			de.addSql("          from odssu.role_function_manual f ");
  			de.addSql("         where a.roleno = f.roleno ");
  			de.addSql("           and f.functionid = :functionid )");
		}
		de.setString("dutyname", dutyName);
		de.setString("rootorgno", rootorgno);
		if (pdid != null && !pdid.equals("") && dptdid != null && !dptdid.equals("")) {
			de.setString("pdid", pdid);
			de.setString("dptdid", dptdid);
		}
		if (!functionid.isEmpty()) {
			de.setString("functionid", functionid);
		}
		DataStore dstemp = de.query();
		return dstemp;
	}

	public DataObject queryfn(DataObject para) throws AppException {
		String functionname = para.getString("functionname", "");
		functionname = "%" + functionname + "%";
    		de.clearSql();
		de.clearSql();
  		de.addSql("select a.functionid, a.functionname, a.appid, b.folderlabel ");
  		de.addSql("  from odssu.appfunction a, odssu.fn_folder b ");
  		de.addSql(" where a.fnfolderid = b.fnfolderid ");
  		de.addSql("   and (a.functionname like :functionname or a.functionid like :functionname) ");
  		de.addSql("   and a.pdid is null ");
		de.setString("functionname", functionname);
		DataStore dstemp = de.query();

		DataObject result = DataObject.getInstance();
		result.put("fnds", dstemp);

		return result;
	}
	public DataObject queryDutyInfo(DataObject para) throws AppException, BusinessException {
		String roleno = para.getString("roleno");
		String inorgno = para.getString("inorgno");
		String deforgno = para.getString("deforgno");
		//获得机构名称,填写 “备注”remark
		StringBuffer orgname = new StringBuffer();
		if(OdssuUtil.isOrgYxdymb(deforgno)){			
			orgname.append("统一");
		}else{
  			de.clearSql();
  			de.addSql(" select orgname from odssu.orginfor where orgno = :deforgno  ");
			de.setString("deforgno", deforgno);
			DataStore orgnamevds = de.query();
			if(orgnamevds == null || orgnamevds.rowCount() ==0){
				this.bizException("无法获取岗位编号为【"+deforgno+"】的岗位信息。");
			}
			orgname.append(orgnamevds.getString(0, "orgname"));
		}
		String bz = orgname.toString() + "定义";
		
		//获得岗位类别和岗位名称
		de.clearSql();
  		de.addSql("select b.typename,a.jsgn,a.rolename  ");
  		de.addSql("  ,case when a.onlyforsz = '0' then 'false' when a.onlyforsz = '1' then 'true' end onlyforsz  ");
  		de.addSql("  from odssu.roleinfor a, odssu.role_type b ");
  		de.addSql(" where a.roleno = :roleno ");
  		de.addSql("   and a.roletype = b.typeno ");
		de.setString("roleno",roleno);
		DataStore dstemp = de.query();	
		if(null == dstemp || dstemp.rowCount()==0){
			throw new AppException("无法查询到角色编号为【"+roleno+"】的角色类型!");
		}
		String gwmc = dstemp.getString(0, "rolename");
		String gwlb = dstemp.getString(0, "typename");
		
		//获得隶属机构
		String jsgn = dstemp.getString(0, "jsgn");
		String lsjg = null;
		if(null!=inorgno &&  !"".equals(inorgno)){
  			de.clearSql();
  			de.addSql(" select orgname from odssu.orginfor where orgno = :inorgno  ");
			de.setString("inorgno", inorgno);
			DataStore lsjgorgnamevds = de.query();
			if(null!=lsjgorgnamevds && lsjgorgnamevds.rowCount()!=0)
				lsjg = lsjgorgnamevds.getString(0, "orgname");
		}
		String onlyforsz = dstemp.getString(0, "onlyforsz");
		
		//判断当前登录用户是否为人社系统的业务职能分配人
		boolean showCheckbtn = checkPrivilegeOnRSXT(para);
		
		DataObject result = DataObject.getInstance();
		DataStore dutyInfoDs = DataStore.getInstance();
		dutyInfoDs.addRow();
		dutyInfoDs.put(0, "gwbh", roleno);
		dutyInfoDs.put(0, "gwmc", gwmc);
		dutyInfoDs.put(0, "gwlb", gwlb);
		dutyInfoDs.put(0, "bz", bz);
		dutyInfoDs.put(0, "lsjg", lsjg);
		dutyInfoDs.put(0, "onlyforsz", onlyforsz);
		result.put("dutyinfods", dutyInfoDs);
		result.put("lsjg", lsjg);
		result.put("jsgn", jsgn);
		result.put("showCheckbtn", showCheckbtn);
		return result;
	}
	/**
	 * 方法简介.获取岗位起用情况
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月11日
	 */
	public DataObject queryQyjg(DataObject para) throws Exception {
		// 定义变量
		String roleno, deforgno;
  		de.clearSql();
		DataStore orgds = DataStore.getInstance();// 存放所有机构查询结果
		DataStore ds = DataStore.getInstance();// 存放已经启用了岗位的机构查询结果
		DataStore resultds = DataStore.getInstance();// 存放最后结果
		DataObject result = DataObject.getInstance();// 存放最后的返回结果

		// 获取变量值
		roleno = para.getString("roleno");
		deforgno = para.getString("deforgno");

		String roleType = OdssuUtil.getRoleInforByRoleno(roleno).getString("roletype");
		
		de.clearSql();
  		de.addSql(" select  b.typename roletype,decode(a.yxxjjgsy,'1','√','0','×') yxxjjgsy ");
  		de.addSql("   from odssu.roleinfor a, ");
  		de.addSql("        odssu.role_type b ");
  		de.addSql("  where a.roleno = :roleno ");
  		de.addSql("    and a.roletype = b.typeno ");
		this.de.setString("roleno", roleno);

		DataStore basicInfor = this.de.query();
		
		// 已经启用了岗位的机构信息
		de.clearSql();
  		de.addSql(" select wm_concat(c.dutyno) dutyno, ");
  		de.addSql("        a.orgno faceorgno, ");
  		de.addSql("        a.orgname faceorg, ");
  		de.addSql("        wm_concat(b.orgno) inorgno, ");
  		de.addSql("        wm_concat(b.orgname) inorg ");
  		de.addSql("   from odssu.orginfor a, odssu.orginfor b, odssu.outer_duty c ");
  		de.addSql("  where c.roleno = :roleno ");
  		de.addSql("    and a.orgno = c.faceorgno ");
  		de.addSql("    and b.orgno = c.inorgno ");
  		de.addSql("  group by a.orgno, a.orgname ");
		this.de.setString("roleno", roleno);
		ds = this.de.query();
		for(int i = 0 ; i<ds.rowCount();i++) {
			ds.put(i, "dutyno", ds.getClobAsString(i, "dutyno"));
			ds.put(i, "inorgno", ds.getClobAsString(i, "inorgno"));
			ds.put(i, "inorg", ds.getClobAsString(i, "inorg"));
		}
		// 人社系统的下级经办机构和二级单位暨经办机构
		de.clearSql();
  		de.addSql(" select b.orgno faceorgno,b.orgname faceorg ");
  		de.addSql(" from odssu.ir_org_closure a,   ");
  		de.addSql("      odssu.orginfor b          ");
  		de.addSql(" where a.orgno = b.orgno        ");
  		de.addSql("   and b.orgtype in (select orgtypeno from odssu.ir_org_role_type  where roletypeno = :roletype) ");
  		de.addSql("   and a.belongorgno = :deforgno ");
		this.de.setString("roletype", roleType);
		this.de.setString("deforgno", deforgno);
		orgds = this.de.query();
		// 获取岗位的业务范畴
		de.clearSql();
  		de.addSql(" select scopeno                    ");
  		de.addSql("   from odssu.ir_role_business_scope ");
  		de.addSql("  where roleno = :roleno                  ");
		this.de.setString("roleno", roleno);
		DataStore ywfcds = this.de.query();

		// 获取机构业务范畴，并将其与岗位业务范畴对比，将有交集的机构编码取出
		int j = 0;
		if(orgds != null && orgds.rowCount()!=0){
			for (int i = 0; i < orgds.rowCount(); i++) {
				DataStore orgywfc = DataStore.getInstance();
				orgywfc = OdssuUtil.getYwfcVdsByOrgno(orgds.getString(i, "faceorgno"));
	
				// 如果机构的业务范畴和岗位业务范畴存在交集，则将机构信息放入最终的结果集中
				if (OdssuUtil.getIts(ywfcds, orgywfc)) {
					resultds.put(j, "faceorgno", orgds.getString(i, "faceorgno"));
					resultds.put(j, "faceorg", orgds.getString(i, "faceorg"));
					resultds.put(j, "qy", "0");
					resultds.put(j, "inorg", "");
					resultds.put(j, "inorgno", "");
					resultds.put(j, "ckgwqyjg", "进入业务机构查看");
					j++;
				}
	
			}
		}
		// 将得出的机构最终结果与已经启用该岗位的机构进行对比，修改qy和inorg信息
		resultds = getds(resultds, ds);
		resultds.multiSort("qy:desc,faceorgno:asc");
		result.put("dsqyjg", resultds);
		result.put("basicinfo", basicInfor);
		return result;
	}
	/**
	 * 方法简介.获取岗位起用情况
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月11日
	 */
	public DataObject checkQyjg(DataObject para) throws Exception {
		// 定义变量
		String roleno, deforgno;
		DataStore orgds = DataStore.getInstance();// 存放所有机构查询结果
		DataStore ds = DataStore.getInstance();// 存放已经启用了岗位的机构查询结果
		DataStore resultds = DataStore.getInstance();// 存放最后结果
		DataObject result = DataObject.getInstance();// 存放最后的返回结果

		// 获取变量值
		roleno = para.getString("roleno");
		deforgno = para.getString("deforgno");
		String checkfaceorgno = para.getString("faceorgno");
		String roleType = OdssuUtil.getRoleInforByRoleno(roleno).getString("roletype");
		
		// 已经启用了岗位的机构信息
		this.de.clearSql();
		this.de.addSql(" select c.dutyno,a.orgno faceorgno,b.orgno inorgno,a.orgname faceorg,b.orgname inorg ");
  		this.de.addSql("   from odssu.orginfor a, ");
  		this.de.addSql("        odssu.orginfor b, ");
  		this.de.addSql("        odssu.outer_duty c ");
  		this.de.addSql("  where c.roleno = :roleno ");
  		this.de.addSql("    and a.orgno = c.faceorgno ");
  		this.de.addSql("    and b.orgno = c.inorgno ");
		this.de.setString("roleno", roleno);
		ds = this.de.query();

		// 人社系统的下级经办机构和二级单位暨经办机构
		this.de.clearSql();
		this.de.addSql(" select b.orgno faceorgno,b.orgname faceorg ");
		this.de.addSql(" from odssu.ir_org_closure a,   ");
		this.de.addSql("      odssu.orginfor b          ");
		this.de.addSql(" where a.orgno = b.orgno        ");
		this.de.addSql("   and b.orgtype in (select orgtypeno from odssu.ir_org_role_type  where roletypeno = :roletype) ");
		this.de.addSql("   and a.belongorgno = :deforgno ");
		this.de.setString("roletype", roleType);
		this.de.setString("deforgno", deforgno);
		orgds = this.de.query();
		// 获取岗位的业务范畴
		this.de.clearSql();
		this.de.addSql(" select scopeno                    ");
		this.de.addSql("   from odssu.ir_role_business_scope ");
  		this.de.addSql("  where roleno = :roleno                  ");
		this.de.setString("roleno", roleno);
		DataStore ywfcds = this.de.query();

		// 获取机构业务范畴，并将其与岗位业务范畴对比，将有交集的机构编码取出
		int j = 0;
		if(orgds != null && orgds.rowCount()!=0 ){
			for (int i = 0; i < orgds.rowCount(); i++) {
				DataStore orgywfc = DataStore.getInstance();
				orgywfc = OdssuUtil.getYwfcVdsByOrgno(orgds.getString(i, "faceorgno"));
	
				// 如果机构的业务范畴和岗位业务范畴存在交集，则将机构信息放入最终的结果集中
				if (OdssuUtil.getIts(ywfcds, orgywfc)) {
					resultds.put(j, "faceorgno", orgds.getString(i, "faceorgno"));
					resultds.put(j, "faceorg", orgds.getString(i, "faceorg"));
					resultds.put(j, "qy", "0");
					resultds.put(j, "inorg", "");
					resultds.put(j, "inorgno", "");
					resultds.put(j, "dutyno", "");
					resultds.put(j, "ckgwqyjg", "进入业务机构查看");
					j++;
				}
	
			}
		}

		// 将得出的机构最终结果与已经启用该岗位的机构进行对比，修改qy和inorg信息
		resultds = getStringds(resultds, ds);
		result.put("inorgno", "");
		result.put("dutyno", "");
		for(int i=0;i< resultds.rowCount();i++){
			String faceorgtemp = resultds.getString(i,"faceorgno");
			String qytemp = resultds.getString(i,"qy");
			if(checkfaceorgno.equals(faceorgtemp) && "1".equals(qytemp)){
				result.put("inorgno", resultds.getString(i, "inorgno"));
				result.put("dutyno", resultds.getString(i, "dutyno"));
			}
		}
		return result;
	}
	
	/**
	 * 方法简介.获取岗位是否停用
	 * 
	 * @author linzp
	 * @date 创建时间 2018年8月2日
	 */
	public DataObject checkTyjg(DataObject para) throws Exception {
		// 定义变量
		String roleno, faceorgno;
		DataStore ds = DataStore.getInstance();		//查询机构是否启用
		DataObject result = DataObject.getInstance();// 存放最后的返回结果

		roleno = para.getString("roleno");
		faceorgno = para.getString("faceorgno");
		
		// 查询岗位是否启用
		this.de.clearSql();
		this.de.addSql(" select 1 ");
  		this.de.addSql("   from odssu.outer_duty  ");
  		this.de.addSql("  where roleno = :roleno ");
  		this.de.addSql("    and faceorgno = :faceorgno ");
		this.de.setString("roleno", roleno);
		this.de.setString("faceorgno", faceorgno);
		ds = this.de.query();
		
		result.put("qyflag", "1");
		
		if(ds==null || ds.rowCount()==0) {
			result.put("qyflag", "0");
		}
		return result;
	}
	/**
	 * @描述：查询岗位下的直属人员
	 * @param para
	 * @return
	 * @throws Exception 2015-6-6
	 */
	public DataObject queryZsry(DataObject para) throws Exception {
		String dutyno = para.getString("dutyno");
		String roleno = para.getString("roleno");
		String dutyflag = para.getString("dutyflag");
		String inorgno =  para.getString("inorgno");
		String qyrs = null;
		DataStore allemp = DataStore.getInstance();
		if("outerduty".equals(dutyflag)){
			if (dutyno == null || dutyno.equals("null") || dutyno.equals("")) {
				this.bizException("传入的dutyno为空!");
			}
			String onlyforsz = "0";
			
			de.clearSql();
  			de.addSql("select a.onlyforsz from odssu.roleinfor a where a.roleno = :roleno ");
			this.de.setString("roleno",roleno);
			DataStore onlyforszvds = this.de.query();
			if (onlyforszvds != null && onlyforszvds.rowCount() > 0) {
				onlyforsz = onlyforszvds.getString(0, "onlyforsz");
			}
			
			de.clearSql();
  			de.addSql(" select ei.empname,ei.empno,t.formalflag,ei.loginname username ");
  			de.addSql("   from odssu.emp_outer_duty_view t, ");
  			de.addSql("        odssu.empinfor ei ");
  			de.addSql("  where t.dutyno= :dutyno ");
  			de.addSql("    and t.empno = ei.empno ");
  			de.addSql("  order by ei.empno ");
			this.de.setString("dutyno", dutyno);
			DataStore zsry = this.de.query();
			
			de.clearSql();
  			de.addSql(" select ei.empname,ei.empno,'0' formalflag,ei.loginname username ");
  			de.addSql("   from odssu.empinfor ei,  ");
  			de.addSql("        odssu.ir_emp_org ieo ");
  			de.addSql("  where ieo.orgno=:inorgno ");
  			de.addSql("    and ieo.empno = ei.empno ");
			if (onlyforsz.equals("1")) {
  				de.addSql("    and ei.hrbelong in (select c.orgno ");
  				de.addSql("                         from odssu.ir_org_closure c, odssu.orginfor d ");
  				de.addSql("                        where c.belongorgno = d.orgno ");
  				de.addSql("                          and d.orgtype = 'HSDOMAIN_DSRSJ') ");
			}
  			de.addSql("  order by ei.empno ");
			this.de.setString("inorgno", inorgno);
			allemp = this.de.query();
			if(zsry != null && allemp != null ){
				for (int m = 0; m < allemp.rowCount(); m++) {
					String empnoall = allemp.getString(m, "empno");
					for (int n = 0; n < zsry.rowCount(); n++) {
						String empnozsry = zsry.getString(n, "empno");
						if (empnoall.equals(empnozsry)) {
							allemp.put(m, "formalflag", zsry.getString(n, "formalflag"));
							break;
						}
					}
				}
				// 将已启用的人员补入ds中
				for (int i = 0; i < zsry.rowCount(); i++) {
					String empnoqy = zsry.getString(i, "empno");
					if (allemp.find(" empno == "+ empnoqy) == -1) {
						allemp.addRow(zsry.getRow(i));
					}
				}
				qyrs = "共计"+zsry.rowCount()+"/"+allemp.rowCount()+"个人员拥有岗位";
			}
		}else if("innerduty".equals(dutyflag)){
  			  			de.clearSql();
			de.clearSql();
  			de.addSql(" select ei.empname,ei.empno,t.formalflag ,ei.loginname username");
  			de.addSql("   from odssu.emp_inner_duty t, ");
  			de.addSql("        odssu.empinfor ei ");
  			de.addSql("  where t.roleno= :roleno ");
  			de.addSql("    and t.orgno= :inorgno ");
  			de.addSql("    and t.empno = ei.empno ");
  			de.addSql("  order by ei.empno ");
			this.de.setString("roleno", roleno);
			this.de.setString("inorgno", inorgno);
			DataStore zsry = this.de.query();
				
			de.clearSql();
  			de.addSql(" select ei.empname,ei.empno,'0' formalflag ,ei.loginname username ");
  			de.addSql("   from odssu.empinfor ei,  ");
  			de.addSql("        odssu.ir_emp_org ieo ");
  			de.addSql("  where ieo.orgno=:inorgno ");
  			de.addSql("    and ieo.empno = ei.empno ");
  			de.addSql("  order by ei.empno ");
			this.de.setString("inorgno", inorgno);
			allemp = this.de.query();
			if(zsry != null && allemp != null ){
				for (int m = 0; m < allemp.rowCount(); m++) {
					String empnoall = allemp.getString(m, "empno");
					for (int n = 0; n < zsry.rowCount(); n++) {
						String empnozsry = zsry.getString(n, "empno");
						if (empnoall.equals(empnozsry)) {
							allemp.put(m, "formalflag", zsry.getString(n, "formalflag"));
							break;
						}
					}
				}
				qyrs = "共计"+zsry.rowCount()+"/"+allemp.rowCount()+"个人员拥有岗位";
			}
		}
		for(int i = 0; i < allemp.size(); i++){
			String empnoall = allemp.getString(i, "empno");
  			de.clearSql();
			de.clearSql();
  			de.addSql(" select b.rolename,b.rolesn ");
  			de.addSql("   from odssu.ir_emp_org_all_role a, ");
  			de.addSql("        odssu.roleinfor b ");
  			de.addSql("  where a.empno = :empnoall ");
  			de.addSql("    and a.orgno = :inorgno ");
  			de.addSql("    and a.roleno = b.roleno ");
  			de.addSql("    and a.rolenature = :rolenature");
  			de.addSql("    and a.roleno <> 'MEMBER' ");
  			de.addSql("    and a.jsgn = :jsgn");
			this.de.setString("empnoall", empnoall);
			this.de.setString("inorgno", inorgno);
			this.de.setString("rolenature", OdssuContants.ROLENATURE_CYJS);
			this.de.setString("jsgn", OdssuContants.JSGN_POST);
			DataStore roleNameVds = this.de.query();
			StringBuffer roleNameBF = new StringBuffer();
			roleNameBF.append("");
			int rolesn = 100;
			if(roleNameVds != null && roleNameVds.rowCount() != 0){
				for(int j = 0; j < roleNameVds.size(); j++){
					String roleName = roleNameVds.getString(j, "rolename");
					roleNameBF.append(roleName+",");
					Integer rolesnInt = roleNameVds.getInt(j, "rolesn");
					if(rolesnInt != null && rolesnInt != 0){
						if(rolesnInt < rolesn){
							rolesn = rolesnInt;
						}
					}
				}
			}
			if(roleNameBF.length()>0){
				roleNameBF.deleteCharAt(roleNameBF.length()-1);
			}
			allemp.put(i, "rolename", roleNameBF.toString());
			allemp.put(i, "rolesn", rolesn);
		}
		DataObject result = DataObject.getInstance();
		allemp = MultiSortUtil.multiSortDS(allemp, "rolesn:asc,username:asc,formalflag:desc");
		result.put("lsjg", "  隶属机构: "+OdssuUtil.getOrgNameByOrgno(inorgno));
		result.put("zsry", allemp);
		result.put("qyrs", qyrs);
		return result;
	}
	/**
	 * 方法简介：查询业务范畴,根据roleno查询 郑海杰 2015-7-24
	 */
	public DataObject queryYwfc(DataObject para) throws Exception {
		String roleno = para.getString("roleno");
  		de.clearSql();
  		de.addSql(" select a.scopeno,a.scopename ");
  		de.addSql("   from odssu.business_scope a, ");
  		de.addSql("        odssu.ir_role_business_scope b ");
  		de.addSql("  where b.roleno = :roleno ");
  		de.addSql("    and a.scopeno = b.scopeno ");
		this.de.setString("roleno", roleno);
		DataStore dsfn = this.de.query();

		DataObject result = DataObject.getInstance();
		result.put("dsywfc", dsfn);
		return result;
	}
	private DataStore getStringds(DataStore result, DataStore ds) throws AppException {
		// 定义变量
		String orgno;
		int j = 0;
		// 将result中与ds中对应的信息修改,若result中没有则增加一列
		for (int i = 0; i < ds.rowCount(); i++) {
			orgno = ds.getString(i, "faceorgno");
			j = result.find("faceorgno == " + orgno);
			if (j != -1) {
				result.put(j, "qy", "1");
				result.put(j, "inorg", ds.getString(i, "inorg"));
				result.put(j, "inorgno", ds.getString(i, "inorgno"));
				result.put(j, "dutyno", ds.getString(i, "dutyno"));
			}else {
				ds.put(i, "qy", "1");
				result.addRow(ds.get(i));
			}
		}
		return result;
	}
	
	private DataStore getds(DataStore result, DataStore ds) throws AppException {
		// 定义变量
		String orgno;
		int j = 0;
		// 将result中与ds中对应的信息修改,若result中没有则增加一列
		for (int i = 0; i < ds.rowCount(); i++) {
			orgno = ds.getString(i, "faceorgno");
			j = result.find("faceorgno == " + orgno);
			if (j != -1) {
				result.put(j, "qy", "1");
				result.put(j, "inorg", ds.getClobAsString(i, "inorg"));
				result.put(j, "inorgno", ds.getClobAsString(i, "inorgno"));
				result.put(j, "dutyno", ds.getClobAsString(i, "dutyno"));
			}else {
				ds.put(i, "qy", "1");
				result.addRow(ds.get(i));
			}
		}
		return result;
	}

	public DataObject querypd(DataObject para) throws AppException {
		String dptdlabel = para.getString("dptdlabel", "");
		dptdlabel = "%" + dptdlabel + "%";
    		de.clearSql();
		de.clearSql();
  		de.addSql("select a.dptdid, a.dptdlabel, a.pdid, b.pdlabel ");
  		de.addSql("  from bpzone.dutyposition_task a, bpzone.process_define b ");
  		de.addSql(" where a.pdid = b.pdid ");
  		de.addSql("   and (a.dptdid like :dptdlabel or a.dptdlabel like :dptdlabel or a.pdid like :dptdlabel) ");
		de.setString("dptdlabel", dptdlabel);
		DataStore dstemp = de.query();

		DataObject result = DataObject.getInstance();
		result.put("pddpds", dstemp);

		return result;
	}
	
	public DataObject queryinorg(DataObject para) throws AppException, BusinessException {
		String inorgname = para.getString("inorgname", "");
		
		// 当前dbid
		String dbid = GlobalNames.DEBUGMODE ? (String) this.getUser().getValue("dbid") : OdssuNames.DBID;
		// 当前的dbid对应的根节点

		de.clearSql();
		de.addSql("select b.orgno from odssu.ir_dbid_org a,odssu.orginfor b where dbid = :dbid and a.orgno = b.orgno ");
		de.setString("dbid", dbid);
		DataStore orgds = de.query();
		if (orgds == null || orgds.rowCount() == 0) {
			this.bizException("无法获取当前DBID对应的机构编号。");
		}
		String rootorgno = orgds.getString(0, "orgno");
		if (rootorgno == null || rootorgno.equals("")) {
			this.bizException("无法获取当前DBID对应的机构编号。");
		}
		
		
		String orgnameUpper = "%"+inorgname.toUpperCase()+"%";
		inorgname = "%" + inorgname + "%";
		de.clearSql();
  		de.addSql("select a.orgno, a.orgname, a.displayname, a.fullname,b.typename orgtype");
  		de.addSql("  from odssu.orginfor a,odssu.org_type b ");
  		de.addSql(" where (a.orgno like :inorgname or upper(orgname) like :orgnameupper or upper(orgnamepy) like :orgnameupper or ");
  		de.addSql("       upper(displayname) like :orgnameupper or upper(displaynamepy) like :orgnameupper or ");
  		de.addSql("       upper(fullname) like :orgnameupper or upper(fullnamepy) like :orgnameupper) ");
  		de.addSql("   and  b.yxin = '1' ");
  		de.addSql("   and b.typeno = a.orgtype ");
  		de.addSql("   and a.sleepflag = '0' ");
  		de.addSql("           and exists (select 1 from odssu.ir_org_closure c where c.orgno = a.orgno and c.belongorgno = :rootorgno) ");
  		de.addSql("   order by b.sn asc,a.orgno asc");
		de.setString("inorgname", inorgname);
		de.setString("orgnameupper", orgnameUpper);
		de.setString("rootorgno", rootorgno);
		de.setQueryScope(300);
		DataStore dstemp = de.query();
		
		DataObject result = DataObject.getInstance();
		result.put("ksds", dstemp);
		
		return result;
	}
	public DataObject queryfaceorg(DataObject para) throws AppException, BusinessException {
		String faceorgname = para.getString("faceorgname", "");
		
		// 当前dbid
		String dbid = GlobalNames.DEBUGMODE ? (String) this.getUser().getValue("dbid") : OdssuNames.DBID;
		// 当前的dbid对应的根节点

		de.clearSql();
		de.addSql("select b.orgno from odssu.ir_dbid_org a,odssu.orginfor b where dbid = :dbid and a.orgno = b.orgno ");
		de.setString("dbid", dbid);
		DataStore orgds = de.query();
		if (orgds == null || orgds.rowCount() == 0) {
			this.bizException("无法获取当前DBID对应的机构编号。");
		}
		String rootorgno = orgds.getString(0, "orgno");
		if (rootorgno == null || rootorgno.equals("")) {
			this.bizException("无法获取当前DBID对应的机构编号。");
		}		
		
		String orgnameUpper = "%"+faceorgname.toUpperCase()+"%";
		faceorgname = "%" + faceorgname + "%";
  		  		de.clearSql();
		de.clearSql();
  		de.addSql(" select a.orgno, ");
  		de.addSql("        a.orgname, ");
  		de.addSql("        a.displayname, ");
  		de.addSql("        a.fullname, ");
  		de.addSql("        b.typename orgtype ");
  		de.addSql(" from odssu.orginfor a, odssu.org_type b ");
  		de.addSql(" where (a.orgno like :faceorgname or upper(orgname) like :orgnameupper or ");
  		de.addSql("        upper(orgnamepy) like :orgnameupper or upper(displayname) like :orgnameupper or ");
  		de.addSql("        upper(displaynamepy) like :orgnameupper or upper(fullname) like :orgnameupper or ");
  		de.addSql("        upper(fullnamepy) like :orgnameupper) ");
  		de.addSql(" and b.yxface = '1' ");
  		de.addSql(" and b.typeno = a.orgtype ");
  		de.addSql(" and a.sleepflag = '0' ");
  		de.addSql(" and exists (select 1 from odssu.ir_org_closure c where c.orgno = a.orgno and c.belongorgno = :rootorgno) ");
  		de.addSql(" order by b.sn asc, a.orgno asc ");
		de.setString("faceorgname", faceorgname);
		de.setString("orgnameupper", orgnameUpper);
		de.setString("rootorgno", rootorgno);
		de.setQueryScope(300);
		DataStore dstemp = de.query();
		
		DataObject result = DataObject.getInstance();
		result.put("orgds", dstemp);
		
		return result;
	}
	
	/**
	 * 描述：查询隶属机构省直客户化
	 * author: sjn
	 * date: 2017年1月10日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject queryinorg_379900(DataObject para) throws AppException, BusinessException {
		String inorgname = para.getString("inorgname", "");
		
		// 当前dbid
		String dbid = GlobalNames.DEBUGMODE ? (String) this.getUser().getValue("dbid") : OdssuNames.DBID;
		// 当前的dbid对应的根节点

		de.clearSql();
		de.addSql("select b.orgno from odssu.ir_dbid_org a,odssu.orginfor b where dbid = :dbid and a.orgno = b.orgno ");
		de.setString("dbid", dbid);
		DataStore orgds = de.query();
		if (orgds == null || orgds.rowCount() == 0) {
			this.bizException("无法获取当前DBID对应的机构编号。");
		}
		String rootorgno = orgds.getString(0, "orgno");
		if (rootorgno == null || rootorgno.equals("")) {
			this.bizException("无法获取当前DBID对应的机构编号。");
		}
		
		
		String orgnameUpper = "%"+inorgname.toUpperCase()+"%";
		inorgname = "%" + inorgname + "%";
  		  		de.clearSql();
		de.clearSql();
  		de.addSql("  select * from( ");
  		de.addSql("select a.orgno, a.orgname, a.displayname, a.fullname,b.typename orgtype");
  		de.addSql("  from odssu.orginfor a,odssu.org_type b ");
  		de.addSql(" where (a.orgno like :inorgname or upper(orgname) like :orgnameupper or upper(orgnamepy) like :orgnameupper or ");
  		de.addSql("       upper(displayname) like :orgnameupper or upper(displaynamepy) like :orgnameupper or ");
  		de.addSql("       upper(fullname) like :orgnameupper or upper(fullnamepy) like :orgnameupper) ");
  		de.addSql("   and  b.yxin = '1' ");
  		de.addSql("   and b.typeno = a.orgtype ");
  		de.addSql("   and a.sleepflag = '0' ");
  		de.addSql("           and exists (select 1 from odssu.ir_org_closure c where c.orgno = a.orgno and c.belongorgno = :rootorgno) ");
		if (!OdssuUtil.isSysAdmin(this.getUser().getUserid())) {
  			de.addSql(" 		   and a.orgno in " + OdssuUtil.queryAuthorityInorg(this.getUser().getUserid()));
		}
  		de.addSql("   order by b.sn asc,a.orgno asc");
  		de.addSql("   )  ");
		de.setString("inorgname", inorgname);
		de.setString("orgnameupper", orgnameUpper);
		de.setString("rootorgno", rootorgno);
		de.setQueryScope(300);
		DataStore dstemp = de.query();
		
		DataObject result = DataObject.getInstance();
		result.put("ksds", dstemp);
		
		return result;
	}
	/**
	 * 描述：查询面向机构省直客户化
	 * author: sjn
	 * date: 2017年1月10日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject queryfaceorg_379900(DataObject para) throws AppException, BusinessException {
		String faceorgname = para.getString("faceorgname", "");
		
		// 当前dbid
		String dbid = GlobalNames.DEBUGMODE ? (String) this.getUser().getValue("dbid") : OdssuNames.DBID;
		// 当前的dbid对应的根节点

		de.clearSql();
		de.addSql("select b.orgno from odssu.ir_dbid_org a,odssu.orginfor b where dbid = :dbid and a.orgno = b.orgno ");
		de.setString("dbid", dbid);
		DataStore orgds = de.query();
		if (orgds == null || orgds.rowCount() == 0) {
			this.bizException("无法获取当前DBID对应的机构编号。");
		}
		String rootorgno = orgds.getString(0, "orgno");
		if (rootorgno == null || rootorgno.equals("")) {
			this.bizException("无法获取当前DBID对应的机构编号。");
		}		
		
		String orgnameUpper = "%"+faceorgname.toUpperCase()+"%";
		faceorgname = "%" + faceorgname + "%";
  		  		de.clearSql();
		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from (select a.orgno, ");
  		de.addSql("               a.orgname, ");
  		de.addSql("               a.displayname, ");
  		de.addSql("               a.fullname, ");
  		de.addSql("               b.typename orgtype ");
  		de.addSql("          from odssu.orginfor a, odssu.org_type b ");
  		de.addSql("         where (a.orgno like :faceorgname or upper(orgname) like :orgnameupper or ");
  		de.addSql("               upper(orgnamepy) like :orgnameupper or upper(displayname) like :orgnameupper or ");
  		de.addSql("               upper(displaynamepy) like :orgnameupper or upper(fullname) like :orgnameupper or ");
  		de.addSql("               upper(fullnamepy) like :orgnameupper) ");
  		de.addSql("           and b.yxface = '1' ");
  		de.addSql("           and b.typeno = a.orgtype ");
  		de.addSql("           and a.sleepflag = '0' ");
  		de.addSql("           and exists (select 1 from odssu.ir_org_closure c where c.orgno = a.orgno and c.belongorgno = :rootorgno) ");
		if (!OdssuUtil.isSysAdmin(this.getUser().getUserid())) {
  			de.addSql("           and a.orgno in " + queryAuthorityFaceorg(this.getUser().getUserid()) + "");
		}
  		de.addSql("         order by b.sn asc, a.orgno asc) ");
		de.setString("faceorgname", faceorgname);
		de.setString("orgnameupper", orgnameUpper);
		de.setString("rootorgno", rootorgno);
		de.setQueryScope(300);
		DataStore dstemp = de.query();
		
		DataObject result = DataObject.getInstance();
		result.put("orgds", dstemp);
		
		return result;
	}
	
	public DataObject getDutyno(DataObject para) throws AppException {
		
		String roleno = para.getString("roleno");
		String faceorgno = para.getString("faceorgno");
  		  		de.clearSql();
  		  		de.addSql(" select dutyno from odssu.outer_duty where roleno = :roleno  and faceorgno = :faceorgno ");
		de.setString("roleno", roleno);
		de.setString("faceorgno", faceorgno);
		DataStore dutynods = de.query();
		
		String dutyno = dutynods.getString(0, "dutyno");
		
		DataObject result = DataObject.getInstance();
		
		result.put("dutyno", dutyno);
		return result;
		
	}
	
	/**
	 * 描述：查询有权的（二级单位暨经办机构）∪（上级人社局）∪（直接有权的二级单位及人社局的业务经办机构）
	 *     如果登录人员直接隶属于人社局或直接人社局的权限，则可以看到该人社局及该人社局下的业务机构和二级单位暨经办机构
	 * author: sjn
	 * date: 2017年1月11日
	 * @return
	 */
	private String queryAuthorityFaceorg(String empno) throws AppException {
		de.clearSql();
  		de.addSql(" select a.orgno                                                                ");
  		de.addSql("   from odssu.orginfor a, odssu.ir_emp_org b, odssu.ir_org_closure c           ");
  		de.addSql("  where b.empno = :empno                                                            ");
  		de.addSql("    and b.orgno = c.orgno                                                      ");
  		de.addSql("    and a.orgno = c.belongorgno                                                ");
  		de.addSql("    and a.orgtype in (:dsejdwywjg,:qxejdwywjg,:stejdwywjg,:dsrsj,:qxrsj,:srst) ");
  		de.addSql(" union                                                                         ");
  		de.addSql(" select d.orgno                                                                ");
  		de.addSql("   from odssu.orginfor            d,                                           ");
  		de.addSql("        odssu.ir_emp_org_all_role e,                                           ");
  		de.addSql("        odssu.ir_org_closure      f                                            ");
  		de.addSql("  where e.empno = :empno                                                            ");
  		de.addSql("    and e.orgno = f.orgno                                                      ");
  		de.addSql("    and d.orgno = f.belongorgno                                                ");
  		de.addSql("    and d.orgtype in (:dsejdwywjg,:qxejdwywjg,:stejdwywjg,:dsrsj,:qxrsj,:srst)");
  		de.addSql("    and e.roleno in (:role_ods_orgadmin,:role_ods_work_dispatch_,");
  		de.addSql("                             '_ODS_DUTY_ASSIGNER',                             ");
  		de.addSql("                             '_ODS_DUTY_REVIEWER',                             ");
  		de.addSql("                             '_ODS_WORK_DISPATH_SH')                           ");
  		de.addSql(" union                                                                         ");
  		de.addSql(" select aa.orgno                                                                ");
  		de.addSql("   from odssu.orginfor aa                                                       ");
  		de.addSql("  where aa.belongorgno in                                                       ");
  		de.addSql("        (select ab.orgno                                                        ");
  		de.addSql("           from odssu.orginfor ab, odssu.ir_emp_org bb, odssu.ir_org_closure cb   ");
  		de.addSql("          where bb.empno = :empno                                                    ");
  		de.addSql("            and bb.orgno = cb.orgno                                              ");
  		de.addSql("            and ab.orgno = cb.belongorgno                                        ");
  		de.addSql("            and ab.orgtype in (:dsejdw,:qxejdw,:stejdw,:dsrsj,:qxrsj,:srst)");
  		de.addSql("         union                                                                 ");
  		de.addSql("         select db.orgno                                                        ");
  		de.addSql("           from odssu.orginfor            db,                                   ");
  		de.addSql("                odssu.ir_emp_org_all_role eb,                                   ");
  		de.addSql("                odssu.ir_org_closure      fb                                    ");
  		de.addSql("          where eb.empno = :empno                                                    ");
  		de.addSql("            and eb.orgno = fb.orgno                                              ");
  		de.addSql("            and db.orgno = fb.belongorgno                                        ");
  		de.addSql("            and db.orgtype in (:dsejdw,:qxejdw,:stejdw,:dsrsj,:qxrsj,:srst)");
  		de.addSql("            and eb.roleno in (:role_ods_orgadmin,:role_ods_work_dispatch_,");
  		de.addSql("                             '_ODS_DUTY_ASSIGNER',                             ");
  		de.addSql("                             '_ODS_DUTY_REVIEWER',                             ");
  		de.addSql("                             '_ODS_WORK_DISPATH_SH'))                          ");
  		de.addSql("    and aa.orgtype in ('HS_DS_YWJG', 'HS_QX_YWJG', 'HS_ST_YWJG')                ");
  		de.addSql(" union                                                                         ");
  		de.addSql(" select ac.orgno                                                                ");
  		de.addSql("   from odssu.orginfor ac, odssu.ir_emp_org bc                                   ");
  		de.addSql("  where bc.empno = :empno                                                            ");
  		de.addSql("    and ac.orgno = bc.orgno                                                      ");
  		de.addSql("    and ac.orgtype in (:dsrsj,:qxrsj,:srst)");
  		de.addSql(" union                                                                         ");
  		de.addSql(" select ad.orgno                                                                ");
  		de.addSql("   from odssu.orginfor ad, odssu.ir_emp_org_all_role bd                          ");
  		de.addSql("  where bd.empno = :empno                                                            ");
  		de.addSql("    and ad.orgno = bd.orgno                                                      ");
  		de.addSql("    and bd.roleno in (:role_ods_orgadmin,:role_ods_work_dispatch_,");
  		de.addSql("                             '_ODS_DUTY_ASSIGNER',                             ");
  		de.addSql("                             '_ODS_DUTY_REVIEWER',                             ");
  		de.addSql("                             '_ODS_WORK_DISPATH_SH')                           ");
  		de.addSql(" union                                                                         ");
  		de.addSql(" select de.orgno                                                                ");
  		de.addSql("   from odssu.ir_org_closure ce, odssu.orginfor de                               ");
  		de.addSql("  where ce.belongorgno in                                                       ");
  		de.addSql("        (select af.orgno                                                        ");
  		de.addSql("           from odssu.orginfor af, odssu.ir_emp_org bf                           ");
  		de.addSql("          where bf.empno = :empno                                                    ");
  		de.addSql("            and af.orgno = bf.orgno                                              ");
  		de.addSql("            and af.orgtype in                                                   ");
  		de.addSql("                (:dsrsj,:qxrsj,:srst)");
  		de.addSql("         union                                                                 ");
  		de.addSql("         select ag.orgno                                                        ");
  		de.addSql("           from odssu.orginfor ag, odssu.ir_emp_org_all_role bg                  ");
  		de.addSql("          where bg.empno = :empno                                                    ");
  		de.addSql("            and ag.orgno = bg.orgno                                              ");
  		de.addSql("            and bg.roleno in (:role_ods_orgadmin,:role_ods_work_dispatch_,");
  		de.addSql("                             '_ODS_DUTY_ASSIGNER',                             ");
  		de.addSql("                             '_ODS_DUTY_REVIEWER',                             ");
  		de.addSql("                             '_ODS_WORK_DISPATH_SH'))                           ");
  		de.addSql("    and de.orgtype in (:dsejdwywjg,:qxejdwywjg,:stejdwywjg,:dsywjg,:qxywjg,:stywjg)");
  		de.addSql("   and ce.orgno = de.orgno                                                       ");
		de.setString("empno", empno);
		de.setString("dsejdwywjg", OdssuContants.ORGTYPE_DSEJDWYWJG);
		de.setString("qxejdwywjg", OdssuContants.ORGTYPE_QXEJDWYWJG);
		de.setString("stejdwywjg", OdssuContants.ORGTYPE_STEJDWYWJG);
		de.setString("dsrsj", OdssuContants.ORGTYPE_DSRSJ);
		de.setString("qxrsj", OdssuContants.ORGTYPE_QXRSJ);
		de.setString("srst", OdssuContants.ORGTYPE_SRST);
		de.setString("role_ods_orgadmin", OdssuContants.ROLE_ODS_ORGADMIN);
		de.setString("role_ods_work_dispatch_", OdssuContants.ROLE_ODS_WORK_DISPATCH_);
		de.setString("dsejdw", OdssuContants.ORGTYPE_DSEJDW);
		de.setString("qxejdw", OdssuContants.ORGTYPE_QXEJDW);
		de.setString("stejdw", OdssuContants.ORGTYPE_STEJDW);
		de.setString("dsywjg", OdssuContants.ORGTYPE_DSYWJG);
		de.setString("qxywjg", OdssuContants.ORGTYPE_QXYWJG);
		de.setString("stywjg", OdssuContants.ORGTYPE_STYWJG);
		
		DataStore vds = de.query();
		String authorityFaceorg = PubUtil.VdsToSqlString(vds, "orgno");
		
		return authorityFaceorg;
	}
	
	/**
	 * 描述：查询当前操作员是否是人社系统的业务职能分配人
	 * author: sjn
	 * date: 2018年4月24日
	 * @param para
	 * @return
	 * @throws AppException
	 */
	private boolean checkPrivilegeOnRSXT(DataObject para) throws AppException{
		String empno = this.getUser().getUserid();
		boolean showCheckbtn = true;
  		  		de.clearSql();
		DataStore vds = DataStore.getInstance();

		de.clearSql();
  		de.addSql("select 1 ");
  		de.addSql("  from odssu.ir_emp_org_all_role a, odssu.orginfor b ");
  		de.addSql(" where a.orgno = b.orgno ");
  		de.addSql("   and b.orgtype = 'HSDOMAIN_DSRSXT' ");
  		de.addSql("   and a.roleno = '_ODS_DUTY_ASSIGNER' ");
  		de.addSql("   and a.empno = :empno ");
		this.de.setString("empno",empno);
		vds = this.de.query();
		
		if (vds != null && vds.rowCount() > 0) {
			showCheckbtn = false;
		}
		return showCheckbtn;
	}

	/**
	 * 描述：修改仅供市直使用标识
	 * author: sjn
	 * date: 2018年4月24日
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject setOnlyForSZ(DataObject para) throws AppException{
		String roleno = para.getString("roleno");
		String onlyforsz = para.getString("onlyforsz","0");
		String onlyforszval = "0";
		if (onlyforsz != null && onlyforsz.equals("true")) {
			onlyforszval = "1";
		}
  		  		de.clearSql();

		de.clearSql();
  		de.addSql("update odssu.roleinfor a set onlyforsz = :onlyforszval where a.roleno = :roleno ");
		this.de.setString("onlyforszval",onlyforszval);
		this.de.setString("roleno",roleno);
		this.de.update();
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("msg", "修改成功！");
		
		return vdo;
	}
}
