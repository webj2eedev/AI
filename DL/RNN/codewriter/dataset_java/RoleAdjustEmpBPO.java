package com.dw.role.roleadjustemp;
import java.util.HashMap;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.log.LogHandler;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class RoleAdjustEmpBPO extends BPO{
	/*先选机构（单选）再选操作员（多选）*/
	//确定机构
	public DataObject queryFirstChooseOrg(DataObject para) throws AppException {
 		String piid = para.getString("piid");
		String roleno = para.getString("roleno");
		DE de = DE.getInstance();

		//角色信息
  		de.clearSql();
  		de.addSql(" SELECT DISTINCT a.roleno, b.orgtypeno, a.rolename, a.roletype, c.typename, :piid piid ");
  		de.addSql(" FROM odssu.roleinfor a ");
  		de.addSql(" LEFT OUTER JOIN odssu.role_orgtype b ON a.roleno = b.roleno ");
  		de.addSql(" LEFT OUTER JOIN odssu.org_type c ON b.orgtypeno = c.typeno ");
  		de.addSql(" WHERE a.sleepflag = '0' ");
  		de.addSql(" AND a.roleno = :roleno ");
		de.setString("roleno", roleno);
		de.setString("piid", piid);
		DataStore roleds = de.query();
		if(roleds.rowCount() == 0) {
			LogHandler.log("根据roleno["+roleno+"]未获取到对应的角色信息");
		}
		roleds = dealFirstOrgType(roleds);

		DataObject result = DataObject.getInstance();
		result.put("roleds", roleds);
		return result;
	}
	//对角色所适用的机构类型用逗号进行分隔
	public DataStore dealFirstOrgType(DataStore ds) throws AppException{
		if(ds == null) {
			return DataStore.getInstance();
		}
		if(ds != null && ds.rowCount() ==0) {
			return ds;
		}
		HashMap<String,Integer> rolesExists = new HashMap<String,Integer>();
		DataStore orgtypeds = DataStore.getInstance(ds.rowCount());
		StringBuffer typename = new StringBuffer();
		for(int i = 0;i<ds.size();i++){
			String roleno = ds.getString(i, "roleno");
			Integer index = rolesExists.get(roleno);
			if(index != null){
				typename.setLength(0);
				typename.append(ds.getString(i, "typename"));
				typename.append(","+orgtypeds.getString(index, "typename"));
				orgtypeds.put(index, "typename", typename.toString());
			}else{
				orgtypeds.addRow(ds.get(i));
				rolesExists.put(roleno, orgtypeds.rowCount()-1);
			}
		}
		return orgtypeds;
	}
	/**
	 * 点击一个机构下面显示的数据
	 */
	public DataStore queryFirstAllOrg(DataObject para) throws AppException {
		String roletype1 = para.getString("roletype1","");
		String dbid = GlobalNames.DEBUGMODE ?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		String orgno=para.getString("belongorgno",""); 

		de.clearSql();
		de.addSql(" select o.orgno,o.displayname,o.orgname,o.sleepflag,oy.typename  ");
		de.addSql(" from   odssu.ir_org_closure i, ");
		de.addSql("        odssu.orginfor o, ");
		de.addSql("       odssu.org_type oy ");
		de.addSql(" where  i.orgno = o.orgno  ");
		de.addSql("     and o.orgtype = oy.typeno ");
		de.addSql("     and o.sleepflag = '0'  ");
		de.addSql("     and( i.belongorgno in (select db.orgno from odssu.ir_dbid_org db where db.dbid = :dbid )");
		de.addSql("      or o.orgno = :orgroot  )");
		de.addSql("     and  oy.typename   in  ( "+roletype1 +")");
		if(orgno !=""){
			de.addSql(" and exists (select 1 from odssu.ir_org_closure a where a.orgno=o.orgno and a.belongorgno= :belongorgno )");
		}
		if(OdssuContants.ORGROOT.equals(orgno)) {
			de.addSql(" and o.orgno = :orgroot");
		}
		de.addSql("   		order by oy.sn, o.orgno   ");
		
		de.setString("dbid", dbid);
		de.setString("orgroot", OdssuContants.ORGROOT);
		if(orgno!=""){
			de.setString("belongorgno", orgno);
		}
		DataStore orgds = de.query();
		return orgds;
	}
	/**
	 * 获取某一个点击的机构下面的全部机构信息，包括已选中的机构进行处理
	 */
	public DataObject dealFirstOrgData(DataObject para) throws AppException {
 		String piid = para.getString("piid");
		String roleno = para.getString("roleno");
		String belongorg = para.getString("orgno").toString();
		String roletype = para.getString("roletype");
		para.put("belongorgno", belongorg);
		//已选中的机构信息
		DataStore orgchoosedds = initFirstOrgTree(piid,roleno,roletype);
		orgchoosedds = getSelectRow(orgchoosedds);
		//全部的机构信息
		String [] roletypearr = roletype.split(",");
		String roletype1 = "";
		for(int i=0;i<roletypearr.length-1;i++){
			roletype1 = roletype1+"'"+roletypearr[i]+"',";
		}
		roletype1 = roletype1+"'"+roletypearr[roletypearr.length-1]+"'";
		para.put("roletype1", roletype1);
		DataStore roledsbeforedel = queryFirstAllOrg(para);
		DataStore orgds = getFirstSelectRowROle(roledsbeforedel,orgchoosedds,roleno,roletype);
		DataObject result = DataObject.getInstance();
		result.put("orgds", orgds);
		return result;
	}
	/**
	 * 在机构页面选择结束之后，对树种的节点的勾选框进行处理
	 */
	public DataObject roleFirstOrgChoosed(DataObject para) throws AppException{
		String roleno = para.getString("roleno");
		String roletype = para.getString("roletype");
		String piid = para.getString("piid");
		DataStore orgds = initFirstOrgTree(piid,roleno,roletype);
		orgds = getSelectRow(orgds);
		DataObject result = DataObject.getInstance();
		result.put("orgds", orgds);
		return result;
	}
	/**
	 * 初始化已选择的机构页面
	 */
	public DataStore initFirstOrgTree(String piid,String roleno,String roletype) throws AppException{
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql(" select DISTINCT a.orgno, c.orgname, d.typename, 'true' row_selected, :roleno roleno , :roletype roletype "); 
		de.addSql(" FROM odssuws.role4emp_detl a, odssu.orginfor c, odssu.org_type d "); 
		de.addSql(" WHERE a.epflag = '(+)'  AND a.orgno = c.orgno "); 
		de.addSql(" AND c.orgtype = d.typeno  AND a.roleno = :roleno "); 
		de.addSql(" AND a.piid = :piid  AND c.sleepflag = '0' "); 
		de.addSql(" ORDER BY a.epflag DESC "); 
		de.setString("roleno", roleno);
		de.setString("piid", piid);
		de.setString("roletype", roletype);
		DataStore rolechoosedds = de.query();
		//账表中除了删除之后的数据
		de.clearSql();
		de.addSql(" select DISTINCT a.orgno, c.orgname, d.typename , 'true' row_selected, :roleno roleno, :roletype roletype "); 
		de.addSql(" from odssu.ir_emp_org_all_role a, odssu.orginfor c, odssu.org_type d "); 
		de.addSql(" where a.orgno = c.orgno and  c.sleepflag = '0' "); 
		de.addSql(" and not EXISTS( select 1 "); 
		de.addSql("      from odssuws.role4emp_detl b "); 
		de.addSql("      where a.empno = b.empno "); 
		de.addSql("      and a.roleno = b.roleno ");
		de.addSql("      and b.piid = :piid "); 
		de.addSql("      and a.orgno = b.orgno "); 
		de.addSql("      and b.epflag = '(-)' ) "); 
		de.addSql(" and a.roleno = :roleno ");
		de.addSql(" and c.orgtype = d.typeno ");
		de.setString("piid", piid);
		de.setString("roleno", roleno);
		de.setString("roletype", roletype);
		DataStore vds = de.query();
		//全部的已选择的数据
		rolechoosedds.combineDatastore(vds);
		//查询所有的角色信息
		return rolechoosedds;
	}
	/**
	 * 对已选的勾选框进行处理
	 */
	public DataStore getFirstSelectRowROle(DataStore roledsbeforedel,DataStore rolechoosedds,String roleno,String roletype) throws AppException{
		DataStore roleds = DataStore.getInstance();
		for(int i =0 ;i<roledsbeforedel.size();i++){
			DataObject vds = roledsbeforedel.get(i);
			String orgno1 = roledsbeforedel.getString(i, "orgno");
			vds.put("_row_selected", false);
			vds.put("roleno", roleno);
			vds.put("roletype", roletype);
			for (int j=0;j<rolechoosedds.size();j++){
				String orgno2 = rolechoosedds.getString(j, "orgno");
				if(orgno1.equals(orgno2)){
					vds.put("_row_selected", true);
					break;
				} 
			}
			roleds.addRow(vds);
		}
		roleds.sortdesc("_row_selected");
		return roleds;
	}
	/**
	 * 已过滤 ：点击一个机构下面显示的数据
	 */
	public DataStore queryFirstAllUncheckOrg(DataObject para) throws AppException {
		String querylabel = para.getString("querylabel","");
		String empno = this.getUser().getUserid();
		String roleno = para.getString("roleno");
		if(roleno == null && "".equals(roleno)) {
			throw new AppException("查询机构信息时未获取到roleno，请检查");
		}
		String dbid = GlobalNames.DEBUGMODE ?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		String belongorgno=para.getString("belongorgno",""); 
		DE de = DE.getInstance();
		querylabel = "%"+querylabel+"%";
		//首先判断当前操作员是否是orgroot上的系统管理员
		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql("   from odssu.ir_emp_org_all_role ");
  		de.addSql("  where empno = :empno ");
  		de.addSql("    and orgno = :orgno ");
  		de.addSql("    and roleno = :roleno ");
  		de.addSql("    and rolenature = :rolenature ");
		de.setString("empno", empno);
		de.setString("orgno", OdssuContants.ORGROOT);
		de.setString("roleno", OdssuContants.ROLE_ODS_SYSADMIN);
		de.setString("rolenature", OdssuContants.ROLENATURE_GXJS);
		DataStore sysAdminVds = de.query();
		if(sysAdminVds.size() > 0){
			de.clearSql();
  			de.addSql(" select DISTINCT o.orgno,o.displayname,o.orgname,o.sleepflag,oy.sn, oy.typename  ");
  			de.addSql(" from   odssu.ir_org_closure i, ");
  			de.addSql("        odssu.orginfor o, ");
  			de.addSql("       odssu.org_type oy ");
  			de.addSql(" where  i.orgno = o.orgno  ");
  			de.addSql("     and o.orgtype = oy.typeno ");
  			de.addSql("        and o.sleepflag = '0' and i.belongorgno in (select db.orgno from odssu.ir_dbid_org db where db.dbid = :dbid ) and");
  			de.addSql("        (o.orgno like :querylabel ");
  			de.addSql("        or o.orgname like :querylabel  ");
  			de.addSql("        or o.displayname like :querylabel  ");
  			de.addSql("        or o.fullname like :querylabel ");
  			de.addSql("        or upper(o.orgnamepy) like :querylabel  "); 
  			de.addSql("        or upper(o.fullnamepy) like :querylabel  ");
  			de.addSql("        or upper(o.displaynamepy) like :querylabel  or oy.typename like :querylabel)  ");
  			de.addSql("        and o.orgtype not in (select typeno from odssu.org_type where  typenature = 'A') ");
  			de.addSql(" and o.orgtype not in (select DISTINCT n.orgtypeno orgtype from odssu.role_orgtype n where n.roleno = :roleno )");
  			de.addSql(" 	and oy.typeno NOT LIKE '%_ORGROOT' ");
  			if(belongorgno !=""){
  				de.addSql(" and exists (select 1 from odssu.ir_org_closure a where a.orgno=o.orgno and a.belongorgno= :belongorgno )");
  			}
  			de.addSql("   		order by oy.sn, o.orgno   ");
  			de.setString("dbid", dbid);
  			de.setString("querylabel", querylabel);
  			de.setString("roleno", roleno);
  			if(belongorgno!=""){
  				de.setString("belongorgno", belongorgno);
  			}
			DataStore orgds = de.query();
			return orgds;
		}
		
		//非orgroot上的系统管理员则获取该操作员是单位管理员的机构以及下级机构
		DataStore vds = DataStore.getInstance();
		de.clearSql();
  		de.addSql("select distinct o.orgno, o.displayname, o.orgname, o.sleepflag, oy.sn, oy.typename");
  		de.addSql("  from (select org.orgno, ");
  		de.addSql("               org.displayname, ");
  		de.addSql("               org.orgname, ");
  		de.addSql("               org.sleepflag, ");
  		de.addSql("               org.fullname, ");
  		de.addSql("               org.orgnamepy, ");
  		de.addSql("               org.fullnamepy, ");
  		de.addSql("               org.displaynamepy, ");
  		de.addSql("               org.orgtype ");
  		de.addSql("          from odssu.orginfor       org, ");
  		de.addSql("               odssu.ir_org_closure iro, ");
  		de.addSql("               odssu.org_type       ot ");
  		de.addSql("         where org.orgno = iro.orgno ");
  		de.addSql("           and org.orgtype = ot.typeno ");
  		de.addSql("           and ot.typenature in ('B', 'C') ");
  		de.addSql("           and org.sleepflag = '0' ");
		de.addSql(" 		  and ot.typeno NOT LIKE '%_ORGROOT' ");
  		de.addSql("           and iro.belongorgno in ");
  		de.addSql("               (select b.orgno ");
  		de.addSql("                  from odssu.ir_emp_org_all_role a, odssu.orginfor b ");
  		de.addSql("                 where a.empno = :empno ");
  		de.addSql("                   and a.roleno = '_ODS_ORGADMIN' ");
  		de.addSql("                   and a.orgno = b.orgno)) o, ");
  		de.addSql("       odssu.org_type oy ");
  		de.addSql(" where (o.orgno like :querylabel or o.orgname like :querylabel or o.displayname like :querylabel or ");
  		de.addSql("       o.fullname like :querylabel or o.orgnamepy like :querylabel or o.fullnamepy like :querylabel or ");
  		de.addSql("       o.displaynamepy like :querylabel or oy.typename like :querylabel) ");
  		de.addSql("   and o.orgtype = oy.typeno ");
  		de.addSql(" and o.orgtype not in (select DISTINCT n.orgtypeno orgtype from odssu.role_orgtype n where n.roleno = :roleno )");
		de.addSql(" and oy.typeno NOT LIKE '%_ORGROOT' ");
		if(belongorgno !=""){
			de.addSql(" and exists (select 1 from odssu.ir_org_closure c where c.orgno=o.orgno and c.belongorgno= :belongorgno )");
		}
		de.addSql("   		order by oy.sn, o.orgno   ");
		de.setString("empno",empno);
		de.setString("querylabel",querylabel);
		de.setString("roleno", roleno);
		if(belongorgno!=""){
			de.setString("belongorgno", belongorgno);
		}
		vds = de.query();
		return vds;
	}
	/**
	 * 获取某一个点击的机构下面的全部机构信息，包括已选中的机构进行处理
	 */
	public DataObject dealFirstUncheckOrgData(DataObject para) throws AppException {
		String belongorg = para.getString("orgno").toString();
		para.put("belongorgno", belongorg);
		//全部的机构信息
		DataStore orgds = queryFirstAllUncheckOrg(para);
		//已选中的机构信息
		DataObject result = DataObject.getInstance();
		result.put("orgds", orgds);
		return result;
	}
	//确定操作员
	public DataObject queryAfterOrgChooseEmp(DataObject para) throws AppException{
		String querylabel = para.getString("querylabel","");
		String querylabeluppercase = querylabel.toUpperCase();
		querylabel = ((querylabel == null || "".equals(querylabel)) ? "%" : "%" + querylabel + "%");
		querylabeluppercase = ((querylabeluppercase == null || "".equals(querylabeluppercase)) ? "%" : "%" + querylabeluppercase + "%");

		de.clearSql();
		de.addSql(" select a.empno, a.empname, a.loginname, a.hrbelong, b.orgname ");
		de.addSql(" from odssu.empinfor a, odssu.orginfor b ");
		de.addSql(" where a.hrbelong = b.orgno ");
		de.addSql(" and a.sleepflag = '0' and b.sleepflag = '0' ");
  		de.addSql(" and (a.empno like :empname or a.empname like :empname or a.empname like :empnameuppercase or upper(empname) like :empnameuppercase ");
  		de.addSql(" or a.loginname like :empname or a.loginname like :empnameuppercase or upper(loginname) like :empnameuppercase) ");
		de.setString("empname", querylabel);
		de.setString("empnameuppercase", querylabeluppercase);
		DataStore empds = de.query();
		
		//查询已选择操作员信息
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String roleno = para.getString("roleno");
		
		//查询新增操作员信息
		de.clearSql();
		de.addSql(" SELECT DISTINCT a.empno, a.empname, a.loginname, a.hrbelong, b.orgname, 'true' row_selected ");
		de.addSql(" FROM odssu.empinfor a, odssu.orginfor b, odssuws.role4emp_detl m  ");
		de.addSql(" WHERE a.hrbelong = b.orgno  AND a.sleepflag = '0'  ");
		de.addSql(" AND b.sleepflag = '0'  AND a.empno = m.empno AND m.epflag = '(+)' ");
		de.addSql(" AND m.piid = :piid AND m.roleno = :roleno AND m.orgno = :orgno ");
		de.setString("piid", piid);
		de.setString("orgno", orgno);
		de.setString("roleno", roleno);
		DataStore empchoosedds = de.query();
		//查询删除以后的原操作员信息
		de.clearSql();
		de.addSql(" SELECT DISTINCT a.empno, a.empname, a.loginname, a.hrbelong, b.orgname, 'true' row_selected  ");
		de.addSql(" FROM odssu.empinfor a, odssu.orginfor b, odssu.ir_emp_org_all_role m  ");
		de.addSql(" WHERE a.hrbelong = b.orgno AND a.sleepflag = '0' AND b.sleepflag = '0'  ");
		de.addSql(" 	AND m.empno = a.empno AND m.roleno = :roleno AND m.orgno = :orgno ");
		de.addSql(" 	AND NOT EXISTS ( SELECT 1 FROM odssuws.role4emp_detl n  ");
		de.addSql(" 	WHERE n.empno = m.empno AND n.roleno = m.roleno AND n.orgno = m.orgno  ");
		de.addSql(" 	AND n.epflag = '(-)' AND n.piid = :piid ) ");
		de.setString("piid", piid);
		de.setString("orgno", orgno);
		de.setString("roleno", roleno);
		DataStore vds = de.query();
		empchoosedds.combineDatastore(vds);
		
		//设置已选择
		empds = getSelectRowEmp(empds, empchoosedds);

		DataObject result = DataObject.getInstance();
		result.put("empds", empds);
		return result;
	}
	public DataStore getSelectRowEmp(DataStore empds,DataStore empchoosedds) throws AppException{
		DataStore resultds = DataStore.getInstance();
		for(int i =0 ;i<empds.size();i++){
			DataObject vds = empds.get(i);
			String empno1 = empds.getString(i, "empno");
			vds.put("_row_selected", false);
			for (int j=0;j<empchoosedds.size();j++){
				String empno2 = empchoosedds.getString(j, "empno");
				if(empno1.equals(empno2)){
					vds.put("_row_selected", true);
					break;
				} 
			}
			resultds.addRow(vds);
		}
		resultds.sortdesc("_row_selected");
		return resultds;
	}

	/*先选操作员（单选）再选机构（多选）*/
	//确定操作员
	public DataObject queryChooseEmp(DataObject para) throws AppException{
		String querylabel = para.getString("querylabel","");
		String querylabeluppercase = querylabel.toUpperCase();
		querylabel = ((querylabel == null || "".equals(querylabel)) ? "%" : "%" + querylabel + "%");
		querylabeluppercase = ((querylabeluppercase == null || "".equals(querylabeluppercase)) ? "%" : "%" + querylabeluppercase + "%");

		
		DataStore empds = DataStore.getInstance();
		de.clearSql();
		de.addSql(" select a.empno, a.empname, a.loginname, a.hrbelong, b.orgname ");
		de.addSql(" from odssu.empinfor a, odssu.orginfor b ");
		de.addSql(" where a.hrbelong = b.orgno ");
		de.addSql(" and a.sleepflag = '0' and b.sleepflag = '0' ");
  		de.addSql(" and (a.empno like :empname or a.empname like :empname or a.empname like :empnameuppercase or upper(empname) like :empnameuppercase ");
  		de.addSql(" or a.loginname like :empname or a.loginname like :empnameuppercase or upper(loginname) like :empnameuppercase) ");
		de.setString("empname", querylabel);
		de.setString("empnameuppercase", querylabeluppercase);
		empds = de.query();
		
		//查询已选择操作员信息
		String piid = para.getString("piid");
		String orgno = para.getString("orgno","");
		String roleno = para.getString("roleno");
		//查询新增操作员信息
		de.clearSql();
		de.addSql(" SELECT DISTINCT a.empno, a.empname, a.loginname, a.hrbelong, b.orgname, 'true' row_selected ");
		de.addSql(" FROM odssu.empinfor a, odssu.orginfor b, odssuws.role4emp_detl m  ");
		de.addSql(" WHERE a.hrbelong = b.orgno  AND a.sleepflag = '0'  ");
		de.addSql(" AND b.sleepflag = '0'  AND a.empno = m.empno AND m.epflag = '(+)' ");
		de.addSql(" AND m.piid = :piid AND m.roleno = :roleno AND m.orgno = :orgno ");
		de.setString("piid", piid);
		de.setString("orgno", orgno);
		de.setString("roleno", roleno);
		DataStore empchoosedds = de.query();
		//查询删除以后的原操作员信息
		de.clearSql();
		de.addSql(" SELECT DISTINCT a.empno, a.empname, a.loginname, a.hrbelong, b.orgname, 'true' row_selected  ");
		de.addSql(" FROM odssu.empinfor a, odssu.orginfor b, odssu.ir_emp_org_all_role m  ");
		de.addSql(" WHERE a.hrbelong = b.orgno AND a.sleepflag = '0' AND b.sleepflag = '0'  ");
		de.addSql(" 	AND m.empno = a.empno AND m.roleno = :roleno AND m.orgno = :orgno ");
		de.addSql(" 	AND NOT EXISTS ( SELECT 1 FROM odssuws.role4emp_detl n  ");
		de.addSql(" 	WHERE n.empno = m.empno AND n.roleno = m.roleno AND n.orgno = m.orgno  ");
		de.addSql(" 	AND n.epflag = '(-)' AND n.piid = :piid ) ");
		de.setString("piid", piid);
		de.setString("orgno", orgno);
		de.setString("roleno", roleno);
		DataStore vds = de.query();
		empchoosedds.combineDatastore(vds);
		
		//设置已选择
		empds = getSelectRowEmp(empds, empchoosedds);
		DataObject result = DataObject.getInstance();
		result.put("empds", empds);
		return result;
	}
	//确定机构
	public DataObject queryChooseOrg(DataObject para) throws AppException {
 		String piid = para.getString("piid");
		String roleno = para.getString("roleno");
		String empno = para.getString("empno","");
		if(empno != null && !empno.equals("")) {
  	  		de.addSql(" , :empno empno  ");
  		}
		DE de = DE.getInstance();

		//角色信息
  		de.clearSql();
  		de.addSql(" select a.roleno, b.orgtypeno, a.rolename, a.roletype, c.typename, :piid piid, :empno empno ");
  		de.addSql("   from odssu.roleinfor a , odssu.role_orgtype b, odssu.org_type c ");
  		de.addSql("  where a.sleepflag = '0' ");
  		de.addSql("    and b.orgtypeno = c.typeno ");
  		de.addSql("    and a.roleno = b.roleno ");
  		de.addSql("    and a.roleno = :roleno ");
		de.setString("roleno", roleno);
		de.setString("piid", piid);
  		de.setString("empno", empno);
		DataStore roleds = de.query();
		if(roleds.rowCount() == 0) {
			LogHandler.log("根据roleno["+roleno+"]未获取到对应的角色信息");
		}
		roleds = dealOrgType(roleds);

		DataObject result = DataObject.getInstance();
		result.put("roleds", roleds);
		return result;
	}
	//对角色所适用的机构类型用逗号进行分隔
	public DataStore dealOrgType(DataStore ds) throws AppException{
		
		if(ds == null) {
			return DataStore.getInstance();
		}
		if(ds != null && ds.rowCount() ==0) {
			return ds;
		}
		
		HashMap<String,Integer> rolesExists = new HashMap<String,Integer>();
		DataStore orgtypeds = DataStore.getInstance(ds.rowCount());
		StringBuffer typename = new StringBuffer();
		for(int i = 0;i<ds.size();i++){
			String roleno = ds.getString(i, "roleno");
			Integer index = rolesExists.get(roleno);
			if(index != null){
				typename.setLength(0);
				typename.append(ds.getString(i, "typename"));
				typename.append(","+orgtypeds.getString(index, "typename"));
				orgtypeds.put(index, "typename", typename.toString());
			}else{
				orgtypeds.addRow(ds.get(i));
				rolesExists.put(roleno, orgtypeds.rowCount()-1);
			}
		}
		return orgtypeds;
	}
	/**
	 * 点击一个机构下面显示的数据
	 */
	public DataStore queryAllOrg(DataObject para) throws AppException {
		String roletype1 = para.getString("roletype1","");
		String dbid = GlobalNames.DEBUGMODE ?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		String orgno=para.getString("belongorgno",""); 

		de.clearSql();
		de.addSql(" select o.orgno,o.displayname,o.orgname,o.sleepflag,oy.typename  ");
		de.addSql(" from   odssu.ir_org_closure i, ");
		de.addSql("        odssu.orginfor o, ");
		de.addSql("       odssu.org_type oy ");
		de.addSql(" where  i.orgno = o.orgno  ");
		de.addSql("     and o.orgtype = oy.typeno ");
		de.addSql("     and o.sleepflag = '0'  ");
		de.addSql("     and( i.belongorgno in (select db.orgno from odssu.ir_dbid_org db where db.dbid = :dbid )");
		de.addSql("      or o.orgno = :orgroot  )");
		de.addSql("     and  oy.typename   in  ( "+roletype1 +")");
		if(orgno !=""){
			de.addSql(" and exists (select 1 from odssu.ir_org_closure a where a.orgno=o.orgno and a.belongorgno= :belongorgno )");
		}
		if(OdssuContants.ORGROOT.equals(orgno)) {
			de.addSql(" and o.orgno = :orgroot");
		}
		de.addSql("   		order by oy.sn, o.orgno   ");
		
		de.setString("dbid", dbid);
		de.setString("orgroot", OdssuContants.ORGROOT);
		if(orgno!=""){
			de.setString("belongorgno", orgno);
		}
		DataStore orgds = de.query();
		return orgds;
	}
	/**
	 * 获取某一个点击的机构下面的全部机构信息，包括已选中的机构进行处理
	 */
	public DataObject dealOrgData(DataObject para) throws AppException {
 		String piid = para.getString("piid");
		String roleno = para.getString("roleno");
		String empno = para.getString("empno");
		String belongorg = para.getString("orgno");
		String roletype = para.getString("roletype");
		String [] roletypearr = roletype.split(",");
		String roletype1 = "";
		for(int i=0;i<roletypearr.length-1;i++){
			roletype1 = roletype1+"'"+roletypearr[i]+"',";
		}
		roletype1 = roletype1+"'"+roletypearr[roletypearr.length-1]+"'";
		para.put("roletype1", roletype1);
		para.put("belongorgno", belongorg);
		//已选中的机构信息
		DataStore orgchoosedds = initOrgTree(piid,roleno,empno,roletype);
		orgchoosedds = getSelectRow(orgchoosedds);
		//全部的机构信息
		DataStore roledsbeforedel = queryAllOrg(para);
		DataStore orgds = getSelectRowROle(roledsbeforedel,orgchoosedds,roleno,empno,roletype);
		DataObject result = DataObject.getInstance();
		result.put("orgds", orgds);
		return result;
	}
	/**
	 * 在机构页面选择结束之后，对树种的节点的勾选框进行处理
	 */
	public DataObject roleOrgChoosed(DataObject para) throws AppException{
		String roleno = para.getString("roleno");
		String roletype = para.getString("roletype");
		String piid = para.getString("piid");
		String empno = para.getString("empno");
		DataStore orgds = initOrgTree(piid,roleno,empno,roletype);
		orgds = getSelectRow(orgds);
		DataObject result = DataObject.getInstance();
		result.put("orgds", orgds);
		return result;
	}
	/**
	 * 初始化已选择的机构页面，即获取某人在某机构下拥有的角色信息
	 */
	public DataStore initOrgTree(String piid,String roleno,String empno,String roletype) throws AppException{
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql(" select DISTINCT a.orgno, c.orgname, d.typename, 'true' row_selected, :empno empno, :roleno roleno , :roletype roletype "); 
		de.addSql(" FROM odssuws.role4emp_detl a, odssu.orginfor c, odssu.org_type d "); 
		de.addSql(" WHERE a.epflag = '(+)'  AND a.orgno = c.orgno "); 
		de.addSql(" AND c.orgtype = d.typeno  AND a.roleno = :roleno "); 
		de.addSql(" AND a.piid = :piid  AND c.sleepflag = '0' "); 
		de.addSql(" AND a.empno = :empno "); 
		de.addSql(" ORDER BY a.epflag DESC "); 
		de.setString("roleno", roleno);
		de.setString("empno", empno);
		de.setString("piid", piid);
		de.setString("roletype", roletype);
		DataStore rolechoosedds = de.query();
		//账表中除了删除之后的数据
		de.clearSql();
		de.addSql(" select DISTINCT a.orgno, c.orgname, d.typename , 'true' row_selected, :empno empno, :roleno roleno, :roletype roletype "); 
		de.addSql(" from odssu.ir_emp_org_all_role a, odssu.orginfor c, odssu.org_type d "); 
		de.addSql(" where a.orgno = c.orgno and  c.sleepflag = '0' "); 
		de.addSql(" and not EXISTS( select 1 "); 
		de.addSql("      from odssuws.role4emp_detl b "); 
		de.addSql("      where a.empno = b.empno "); 
		de.addSql("      and a.roleno = b.roleno ");
		de.addSql("      and b.piid = :piid "); 
		de.addSql("      and a.orgno = b.orgno "); 
		de.addSql("      and b.epflag = '(-)' ) "); 
		de.addSql(" and a.roleno = :roleno ");
		de.addSql(" and c.orgtype = d.typeno ");
		de.addSql(" and a.empno = :empno "); 
		de.setString("piid", piid);
		de.setString("empno", empno);
		de.setString("roleno", roleno);
		de.setString("roletype", roletype);
		DataStore vds = de.query();
		//全部的已选择的数据
		rolechoosedds.combineDatastore(vds);
		//查询所有的角色信息
		return rolechoosedds;
	}
	/**
	 * 对已选的勾选框进行处理
	 */
	public DataStore getSelectRowROle(DataStore roledsbeforedel,DataStore rolechoosedds,String roleno,String empno,String roletype) throws AppException{
		DataStore roleds = DataStore.getInstance();
		for(int i =0 ;i<roledsbeforedel.size();i++){
			DataObject vds = roledsbeforedel.get(i);
			String orgno1 = roledsbeforedel.getString(i, "orgno");
			vds.put("_row_selected", false);
			vds.put("empno", empno);
			vds.put("roleno", roleno);
			vds.put("roletype", roletype);
			for (int j=0;j<rolechoosedds.size();j++){
				String orgno2 = rolechoosedds.getString(j, "orgno");
				if(orgno1.equals(orgno2)){
					vds.put("_row_selected", true);
					break;
				} 
			}
			roleds.addRow(vds);
		}
		roleds.sortdesc("_row_selected");
		return roleds;
	}
	public DataStore getSelectRow(DataStore orgds) throws AppException{
		for (int i=0;i<orgds.size();i++){
			orgds.put(i,"_row_selected", true);
		}
		return orgds;
	}
	/**
	 * 已过滤 ：点击一个机构下面显示的数据
	 */
	public DataStore queryAllUncheckOrg(DataObject para) throws AppException {
		String querylabel = para.getString("querylabel","");
		String empno = this.getUser().getUserid();
		String roleno = para.getString("roleno");
		if(roleno == null && "".equals(roleno)) {
			throw new AppException("查询机构信息时未获取到roleno，请检查");
		}
		String dbid = GlobalNames.DEBUGMODE ?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		String belongorgno=para.getString("belongorgno",""); 
		DE de = DE.getInstance();
		querylabel = "%"+querylabel+"%";
		//首先判断当前操作员是否是orgroot上的系统管理员
		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql("   from odssu.ir_emp_org_all_role ");
  		de.addSql("  where empno = :empno ");
  		de.addSql("    and orgno = :orgno ");
  		de.addSql("    and roleno = :roleno ");
  		de.addSql("    and rolenature = :rolenature ");
		de.setString("empno", empno);
		de.setString("orgno", OdssuContants.ORGROOT);
		de.setString("roleno", OdssuContants.ROLE_ODS_SYSADMIN);
		de.setString("rolenature", OdssuContants.ROLENATURE_GXJS);
		DataStore sysAdminVds = de.query();
		if(sysAdminVds.size() > 0){
			de.clearSql();
  			de.addSql(" select DISTINCT o.orgno,o.displayname,o.orgname,o.sleepflag,oy.sn, oy.typename  ");
  			de.addSql(" from   odssu.ir_org_closure i, ");
  			de.addSql("        odssu.orginfor o, ");
  			de.addSql("       odssu.org_type oy ");
  			de.addSql(" where  i.orgno = o.orgno  ");
  			de.addSql("     and o.orgtype = oy.typeno ");
  			de.addSql("        and o.sleepflag = '0' and i.belongorgno in (select db.orgno from odssu.ir_dbid_org db where db.dbid = :dbid ) and");
  			de.addSql("        (o.orgno like :querylabel ");
  			de.addSql("        or o.orgname like :querylabel  ");
  			de.addSql("        or o.displayname like :querylabel  ");
  			de.addSql("        or o.fullname like :querylabel ");
  			de.addSql("        or upper(o.orgnamepy) like :querylabel  "); 
  			de.addSql("        or upper(o.fullnamepy) like :querylabel  ");
  			de.addSql("        or upper(o.displaynamepy) like :querylabel  or oy.typename like :querylabel)  ");
  			de.addSql("        and o.orgtype not in (select typeno from odssu.org_type where  typenature = 'A') ");
  			de.addSql(" and o.orgtype not in (select DISTINCT n.orgtypeno orgtype from odssu.role_orgtype n where n.roleno = :roleno )");
  			de.addSql(" 	and oy.typeno NOT LIKE '%_ORGROOT' ");
  			if(belongorgno !=""){
  				de.addSql(" and exists (select 1 from odssu.ir_org_closure a where a.orgno=o.orgno and a.belongorgno= :belongorgno )");
  			}
  			de.addSql("   		order by oy.sn, o.orgno   ");
  			de.setString("dbid", dbid);
  			de.setString("querylabel", querylabel);
  			de.setString("roleno", roleno);
  			if(belongorgno!=""){
  				de.setString("belongorgno", belongorgno);
  			}
			DataStore orgds = de.query();
			return orgds;
		}
		
		//非orgroot上的系统管理员则获取该操作员是单位管理员的机构以及下级机构
		DataStore vds = DataStore.getInstance();
		de.clearSql();
  		de.addSql("select distinct o.orgno, o.displayname, o.orgname, o.sleepflag, oy.sn, oy.typename");
  		de.addSql("  from (select org.orgno, ");
  		de.addSql("               org.displayname, ");
  		de.addSql("               org.orgname, ");
  		de.addSql("               org.sleepflag, ");
  		de.addSql("               org.fullname, ");
  		de.addSql("               org.orgnamepy, ");
  		de.addSql("               org.fullnamepy, ");
  		de.addSql("               org.displaynamepy, ");
  		de.addSql("               org.orgtype ");
  		de.addSql("          from odssu.orginfor       org, ");
  		de.addSql("               odssu.ir_org_closure iro, ");
  		de.addSql("               odssu.org_type       ot ");
  		de.addSql("         where org.orgno = iro.orgno ");
  		de.addSql("           and org.orgtype = ot.typeno ");
  		de.addSql("           and ot.typenature in ('B', 'C') ");
  		de.addSql("           and org.sleepflag = '0' ");
		de.addSql(" 	      and ot.typeno NOT LIKE '%_ORGROOT' ");
  		de.addSql("           and iro.belongorgno in ");
  		de.addSql("               (select b.orgno ");
  		de.addSql("                  from odssu.ir_emp_org_all_role a, odssu.orginfor b ");
  		de.addSql("                 where a.empno = :empno ");
  		de.addSql("                   and a.roleno = '_ODS_ORGADMIN' ");
  		de.addSql("                   and a.orgno = b.orgno)) o, ");
  		de.addSql("       odssu.org_type oy ");
  		de.addSql(" where (o.orgno like :querylabel or o.orgname like :querylabel or o.displayname like :querylabel or ");
  		de.addSql("       o.fullname like :querylabel or o.orgnamepy like :querylabel or o.fullnamepy like :querylabel or ");
  		de.addSql("       o.displaynamepy like :querylabel or oy.typename like :querylabel) ");
  		de.addSql("   and o.orgtype = oy.typeno ");
  		de.addSql(" and o.orgtype not in (select DISTINCT n.orgtypeno orgtype from odssu.role_orgtype n where n.roleno = :roleno )");
		de.addSql(" 	and oy.typeno NOT LIKE '%_ORGROOT' ");
		if(belongorgno !=""){
			de.addSql(" and exists (select 1 from odssu.ir_org_closure c where c.orgno=o.orgno and c.belongorgno= :belongorgno )");
		}
		de.addSql("   		order by oy.sn, o.orgno   ");
		de.setString("empno",empno);
		de.setString("querylabel",querylabel);
		de.setString("roleno",roleno);
		if(belongorgno!=""){
			de.setString("belongorgno", belongorgno);
		}
		vds = de.query();
		return vds;
	}
	/**
	 * 获取某一个点击的机构下面的全部机构信息，包括已选中的机构进行处理
	 */
	public DataObject dealUncheckOrgData(DataObject para) throws AppException {
		String belongorg = para.getString("orgno").toString();
		para.put("belongorgno", belongorg);
		//全部的机构信息
		DataStore orgds = queryAllUncheckOrg(para);
		//已选中的机构信息
		DataObject result = DataObject.getInstance();
		result.put("orgds", orgds);
		return result;
	}
	public DataObject getname(DataObject para) throws AppException{
		DE de = DE.getInstance();
		String empname = "",rolename = "";
		String empno = para.getString("empno","");
		if(empno == null || "".equals(empno)) {
			de.clearSql();
	  		de.addSql(" select empname  ");
	  		de.addSql(" from   odssu.empinfor t ");
	  		de.addSql(" where  t.empno = :empno ");
			de.setString("empno", empno);
			DataStore ds = de.query();
			if(ds.rowCount() > 0) {
				empname = ds.getString(0, "empname");
			}
		}
		String roleno = para.getString("roleno");
		de.clearSql();
  		de.addSql(" select rolename  ");
  		de.addSql(" from   odssu.roleinfor t ");
  		de.addSql(" where  t.roleno = :roleno ");
		de.setString("roleno", roleno);
		DataStore ds1 = de.query();
		if(ds1.rowCount() > 0) {
			rolename = ds1.getString(0, "rolename");
		}
		DataObject result = DataObject.getInstance();
		result.put("rolename", rolename);
		result.put("empname", empname);
		return result;
	}

	//删除机构信息
	public void delRoleOrgEmp(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String roleno = para.getString("roleno");
		String empno = para.getString("empno");
		DE de = DE.getInstance();
		DataStore ds = epflag(piid,roleno,orgno,empno);
		//如果工单表中有数据，证明是新增的，直接删除；如果工单表中无数据，证明是账表中的删除，所以新增一条为-的数据
		if(ds.isEmpty()){
			LogHandler.log("无对应信息可删除");
		}else {
			String epflag = ds.getString(0, "epflag");
			if(epflag == null || epflag.equals("")) {
				de.clearSql();
				de.addSql(" update odssuws.role4emp_detl set epflag = '(-)' ");
				de.addSql(" where piid = :piid and orgno = :orgno  and roleno = :roleno  and empno = :empno ");
				de.setString("piid", piid);
				de.setString("orgno", orgno);
				de.setString("roleno", roleno);
				de.setString("empno", empno);
				de.update();
			}else if(epflag.equals("(+)")){
				de.clearSql();
				de.addSql(" delete from odssuws.role4emp_detl ");
				de.addSql(" where piid = :piid and orgno = :orgno  and roleno = :roleno  and empno = :empno ");
				de.setString("piid", piid);
				de.setString("orgno", orgno);
				de.setString("roleno", roleno);
				de.setString("empno", empno);
				de.update();
			}else {
				LogHandler.log("不可重复删除");
			}
		}
	}
	//添加机构信息
	public void addRoleOrgEmp(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String roleno = para.getString("roleno");
		String empno = para.getString("empno");
		DE de = DE.getInstance();
		DataStore ds = epflag(piid,roleno,orgno,empno);
		//如果数据是原有被删除的，则将符号设置为空，如果数据是没有的，新添加的，则将符号设置为+
		if(ds.isEmpty()){
			de.clearSql();
			de.addSql(" insert into odssuws.role4emp_detl(piid,orgno,roleno,epflag,empno) ");
			de.addSql(" values(:piid, :orgno , :roleno , '(+)',:empno) ");
			de.setString("piid", piid);
			de.setString("orgno", orgno);
			de.setString("roleno", roleno);
			de.setString("empno", empno);
			de.update();
		}else{
			String epflag = ds.getString(0, "epflag");
			if(epflag == null || epflag.equals("")) {
				LogHandler.log("已有信息，不可新增");
			}else if(epflag.equals("(-)")) {
				de.clearSql();
				de.addSql(" update odssuws.role4emp_detl set epflag = null ");
				de.addSql(" where piid = :piid and orgno = :orgno  and roleno = :roleno  and empno = :empno ");
				de.setString("piid", piid);
				de.setString("orgno", orgno);
				de.setString("roleno", roleno);
				de.setString("empno", empno);
				de.update();
			}else{
				LogHandler.log("不可重复新增");
			}
		}
	}

	//确定机构后，批量删除操作员信息
	public void delAllRoleOrgEmp(DataObject para) throws AppException, BusinessException{
		DE de = DE.getInstance();
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String roleno = para.getString("roleno");
		DataStore empgrid = para.getDataStore("empgrid");
		for(int i = 0; i < empgrid.rowCount(); i++) {
			String empno = empgrid.getString(i, "empno");
			DataStore ds = epflag(piid,roleno,orgno,empno);
			//如果工单表中有数据，证明是新增的，直接删除；如果工单表中无数据，证明是账表中的删除，所以新增一条为-的数据
			if(ds.isEmpty()){
				LogHandler.log("无对应信息可删除");
			}else {
				String epflag = ds.getString(0, "epflag");
				if(epflag == null || epflag.equals("")) {
					de.clearSql();
					de.addSql(" update odssuws.role4emp_detl set epflag = '(-)' ");
					de.addSql(" where piid = :piid and orgno = :orgno  and roleno = :roleno  and empno = :empno ");
					de.setString("piid", piid);
					de.setString("orgno", orgno);
					de.setString("roleno", roleno);
					de.setString("empno", empno);
					de.update();
				}else if(epflag.equals("(+)")){
					de.clearSql();
					de.addSql(" delete from odssuws.role4emp_detl ");
					de.addSql(" where piid = :piid and orgno = :orgno  and roleno = :roleno  and empno = :empno ");
					de.setString("piid", piid);
					de.setString("orgno", orgno);
					de.setString("roleno", roleno);
					de.setString("empno", empno);
					de.update();
				}else {
					LogHandler.log("不可重复删除");
				}
			}
		}
	}
	//确定机构后，批量添加操作员信息
	public void addAllRoleOrgEmp(DataObject para) throws AppException, BusinessException{
		DE de = DE.getInstance();
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String roleno = para.getString("roleno");
		DataStore empgrid = para.getDataStore("empgrid");
		for(int i = 0; i < empgrid.rowCount(); i++) {
			String empno = empgrid.getString(i, "empno");
			DataStore ds = epflag(piid,roleno,orgno,empno);
			//如果数据是原有被删除的，则将符号设置为空，如果数据是没有的，新添加的，则将符号设置为+
			if(ds.isEmpty()){
				de.clearSql();
				de.addSql(" insert into odssuws.role4emp_detl(piid,orgno,roleno,epflag,empno) ");
				de.addSql(" values(:piid, :orgno , :roleno , '(+)',:empno) ");
				de.setString("piid", piid);
				de.setString("orgno", orgno);
				de.setString("roleno", roleno);
				de.setString("empno", empno);
				de.update();
			}else{
				String epflag = ds.getString(0, "epflag");
				if(epflag == null || epflag.equals("")) {
					LogHandler.log("已有信息，不可新增");
				}else if(epflag.equals("(-)")) {
					de.clearSql();
					de.addSql(" update odssuws.role4emp_detl set epflag is null ");
					de.addSql(" where piid = :piid and orgno = :orgno  and roleno = :roleno  and empno = :empno ");
					de.setString("piid", piid);
					de.setString("orgno", orgno);
					de.setString("roleno", roleno);
					de.setString("empno", empno);
					de.update();
				}else{
					LogHandler.log("不可重复新增");
				}
			}
		}
	}

	//确定操作员后，批量删除机构信息
	public void delAllRoleEmpOrg(DataObject para) throws AppException, BusinessException{
		DE de = DE.getInstance();
		String piid = para.getString("piid");
		DataStore orggrid = para.getDataStore("orggrid");
		for(int i = 0; i < orggrid.rowCount(); i++) {
			String empno = orggrid.getString(i, "empno");
			String roleno = orggrid.getString(i, "roleno");
			String orgno = orggrid.getString(i, "orgno");
			DataStore ds = epflag(piid,roleno,orgno,empno);
			//如果工单表中有数据，证明是新增的，直接删除；如果工单表中无数据，证明是账表中的删除，所以新增一条为-的数据
			if(ds.isEmpty()){
				LogHandler.log("无对应信息可删除");
			}else {
				String epflag = ds.getString(0, "epflag");
				if(epflag == null || epflag.equals("")) {
					de.clearSql();
					de.addSql(" update odssuws.role4emp_detl set epflag = '(-)' ");
					de.addSql(" where piid = :piid and orgno = :orgno  and roleno = :roleno  and empno = :empno ");
					de.setString("piid", piid);
					de.setString("orgno", orgno);
					de.setString("roleno", roleno);
					de.setString("empno", empno);
					de.update();
				}else if(epflag.equals("(+)")){
					de.clearSql();
					de.addSql(" delete from odssuws.role4emp_detl ");
					de.addSql(" where piid = :piid and orgno = :orgno  and roleno = :roleno  and empno = :empno ");
					de.setString("piid", piid);
					de.setString("orgno", orgno);
					de.setString("roleno", roleno);
					de.setString("empno", empno);
					de.update();
				}else {
					LogHandler.log("不可重复删除");
				}
			}
		}
	}
	//确定操作员后，批量添加机构信息
	public void addAllRoleEmpOrg(DataObject para) throws AppException, BusinessException{
		DE de = DE.getInstance();
		String piid = para.getString("piid");
		DataStore orggrid = para.getDataStore("orggrid");
		for(int i = 0; i < orggrid.rowCount(); i++) {
			String empno = orggrid.getString(i, "empno");
			String roleno = orggrid.getString(i, "roleno");
			String orgno = orggrid.getString(i, "orgno");
			DataStore ds = epflag(piid,roleno,orgno,empno);
			//如果数据是原有被删除的，则将符号设置为空，如果数据是没有的，新添加的，则将符号设置为+
			if(ds.isEmpty()){
				de.clearSql();
				de.addSql(" insert into odssuws.role4emp_detl(piid,orgno,roleno,epflag,empno) ");
				de.addSql(" values(:piid, :orgno , :roleno , '(+)',:empno) ");
				de.setString("piid", piid);
				de.setString("orgno", orgno);
				de.setString("roleno", roleno);
				de.setString("empno", empno);
				de.update();
			}else{
				String epflag = ds.getString(0, "epflag");
				if(epflag == null || epflag.equals("")) {
					LogHandler.log("已有信息，不可新增");
				}else if(epflag.equals("(-)")) {
					de.clearSql();
					de.addSql(" update odssuws.role4emp_detl set epflag = null ");
					de.addSql(" where piid = :piid and orgno = :orgno  and roleno = :roleno  and empno = :empno ");
					de.setString("piid", piid);
					de.setString("orgno", orgno);
					de.setString("roleno", roleno);
					de.setString("empno", empno);
					de.update();
				}else{
					LogHandler.log("不可重复新增");
				}
			}
		}
	}
	
	/**
	 * 查看工单表中的标记，根据标记判断直接添加，还是修改标记
	 */
	public DataStore epflag(String piid,String roleno,String orgno,String empno) throws AppException{
		DE de =DE.getInstance();
		de.clearSql();
		de.addSql("select * from odssuws.role4emp_detl where piid = :piid  and roleno = :roleno   ");
		if(""!=orgno  && null!=orgno){
			de.addSql("   and  orgno = :orgno");
			de.setString("orgno",orgno);
		}
		if(""!=empno  && null!=empno){
			de.addSql("   and  empno = :empno");
			de.setString("empno",empno);
		}
		de.setString("piid",piid);
		de.setString("roleno",roleno);
		DataStore ds = de.query();
		return ds;
	}
}