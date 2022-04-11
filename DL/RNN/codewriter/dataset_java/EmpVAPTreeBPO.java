package com.dw.vap.emp;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.apps.odssu.OdssuNames;
import com.dareway.apps.process.util.ProcessConstants;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;
import com.dw.util.jxkh.PubUtil;
import com.dw.util.multiSortUtil.MultiSortUtil;

public class EmpVAPTreeBPO extends BPO{
	public DataObject queryHrOrgLov4EmpAdd(DataObject para) throws AppException {
		String querylabel = para.getString("querylabel","");
		String empno = this.getUser().getUserid();
		querylabel = "%"+querylabel.toUpperCase()+"%";
		String dbid = GlobalNames.DEBUGMODE ?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;
		DE de = DE.getInstance();
		DataObject result = DataObject.getInstance();
		
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
  			de.addSql(" select o.orgno,o.displayname,o.orgname,o.sleepflag  ");
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
  			de.addSql("        or upper(o.displaynamepy) like :querylabel)  ");
  			de.addSql("        and o.orgtype not in (select typeno from odssu.org_type where  typenature = 'A')  ");
  			de.addSql("   		order by oy.sn, o.orgno   ");
  			de.setString("dbid", dbid);
  			de.setString("querylabel", querylabel);
			DataStore orgds = de.query();

			result.put("orgds", orgds);
			return result;
		}
		
		//非orgroot上的系统管理员则获取该操作员是单位管理员的机构以及下级机构
		DataStore vds = DataStore.getInstance();
		de.clearSql();
  		de.addSql("select distinct o.orgno, o.displayname, o.orgname, o.sleepflag, oy.sn ");
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
  		de.addSql("           and iro.belongorgno in ");
  		de.addSql("               (select b.orgno ");
  		de.addSql("                  from odssu.ir_emp_org_all_role a, odssu.orginfor b ");
  		de.addSql("                 where a.empno = :empno ");
  		de.addSql("                   and a.roleno = '_ODS_ORGADMIN' ");
  		de.addSql("                   and a.orgno = b.orgno)) o, ");
  		de.addSql("       odssu.org_type oy ");
  		de.addSql(" where (o.orgno like :querylabel or o.orgname like :querylabel or o.displayname like :querylabel or ");
  		de.addSql("       o.fullname like :querylabel or o.orgnamepy like :querylabel or o.fullnamepy like :querylabel or ");
  		de.addSql("       o.displaynamepy like :querylabel) ");
  		de.addSql("   and o.orgtype = oy.typeno ");
  		de.addSql(" order by oy.sn, o.orgno ");
		de.setString("empno",empno);
		de.setString("querylabel",querylabel);
		vds = de.query();

		result.put("orgds", vds);
		return result;
		
	}
	/**
	 * 方法简介 ： 获取复职时候的业务办理机构列表
	 *@author 郑海杰   2016年4月20日
	 */
	public DataObject getFzYwbljgDSByOperator(DataObject para) throws AppException, BusinessException{
		DataObject result = DataObject.getInstance();
		DataStore fzBljgVds = getFzYwbljgVdsByOperator();
		if(fzBljgVds == null || fzBljgVds.size() == 0){
			this.bizException("获取人员离职办理机构列表失败");
		}else{
			result.put("orgds", fzBljgVds);
		}
		return result;
	}
	/**
	 * 方法简介 ： 获取复职时候的业务办理机构编号
	 *@author 郑海杰   2016年4月20日
	 */
	public DataObject getFzYwbljgOneByOperator(DataObject para) throws AppException, BusinessException{
		DataObject result = DataObject.getInstance();
		DataStore fzBljgVds = getFzYwbljgVdsByOperator();
		if(fzBljgVds == null || fzBljgVds.size() == 0){
			this.bizException("获取人员离职办理机构失败");
		}else{
			
			result.put("bljgid", fzBljgVds.getString(0, "orgno"));
		}
		return result;
	}
	/**
	 * 方法简介 ： 获取复职时候的业务办理机构数量
	 *@author 郑海杰   2016年4月20日
	 */
	public DataObject getFzYwbljgCountByOperator(DataObject para) throws AppException{
		int ywbljgCount = getFzYwbljgVdsByOperator().size();
		DataObject result = DataObject.getInstance();
		result.put("ywbljgcount", ywbljgCount+"");
		return result;
	}
	/**
	 * 方法简介 ： 获取复职时候的业务办理机构Vds
	 *@author 郑海杰   2016年4月20日
	 */
	private DataStore getFzYwbljgVdsByOperator() throws AppException{
		DE de = DE.getInstance();
		de.clearSql();
		//获取操作员是 单位管理员的 人社局（厅）和二级单位

		de.addSql(" select b.sleepflag,b.orgno,b.orgname,b.displayname ");
  		de.addSql("   from odssu.ir_emp_org_all_role a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where a.empno = :empno ");
  		de.addSql("    and a.roleno = :roleno ");
  		de.addSql("    and a.orgno = b.orgno ");
  		de.addSql("   order by b.orgno ");
		de.setString("empno", this.getUser().getUserid());
		de.setString("roleno", OdssuContants.ROLE_ODS_ORGADMIN);
		DataStore orgVds = de.query();
		return orgVds;
	} 
	
	/**
	 * 方法简介 ：获取没有权限时的处理方法 
	 *@author 郑海杰   2016年4月20日
	 * @throws AppException 
	 * @throws BusinessException 
	 */
	public DataObject getNoRightDealMethod(DataObject para) throws AppException, BusinessException{
		String empno = para.getString("empno");
		String roleno = para.getString("roleno");
		String rolename = OdssuUtil.getRoleNameByRoleno(roleno);
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select distinct a.orgno ");
  		de.addSql("   from odssu.ir_emp_org a ");
  		de.addSql("  where a.empno = :empno ");
		de.setString("empno", empno);
		DataStore belongOrgVds = de.query();
		String hrBelongOrgnoSqlStr = "";
		if(belongOrgVds == null || belongOrgVds.size() == 0){
			this.bizException("未找到编号为【" + empno +"】人员的人事隶属机构编号");
		}else{
			hrBelongOrgnoSqlStr = PubUtil.VdsToSqlString(belongOrgVds, "orgno");
		}
		de.clearSql();				
  		de.addSql(" select distinct b.orgname,b.orgno ");
  		de.addSql("   from odssu.ir_org_closure a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where a.orgno in "+hrBelongOrgnoSqlStr);
  		de.addSql("    and a.belongorgno = b.orgno ");
  		de.addSql("    order by b.orgno ");
		DataStore orgNameVds = de.query();
		StringBuffer dealMehodBF = new StringBuffer();
		dealMehodBF.append("");
		if(orgNameVds == null || orgNameVds.size() == 0){
			this.bizException("未找到机构【" + hrBelongOrgnoSqlStr + "】所属的人社厅、局、二级单位");
		}else{
			dealMehodBF.append("能办理此业务的有:");
			for(int i = 0; i < orgNameVds.size(); i++){
				String orgname = orgNameVds.getString(i, "orgname");
				dealMehodBF.append("【" + orgname + "】");
				if(i < orgNameVds.size() - 1){
					dealMehodBF.append("或者");
				}
			}
			dealMehodBF.append("的【"+rolename+"】");
		}
		DataObject result = DataObject.getInstance();
		result.put("dealmethod", dealMehodBF.toString());
		return result;
	}
	public DataObject checkAddOrgRight(DataObject para) throws AppException{
		String bljgid = para.getString("bljgid");
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql("   from odssu.ir_emp_org_all_role a ");
  		de.addSql("  where a.empno = :empno ");
  		de.addSql("    and a.orgno = :bljgid ");
  		de.addSql("    and a.roleno = :roleno ");
		de.setString("empno", this.getUser().getUserid());
		de.setString("bljgid", bljgid);
		de.setString("roleno", OdssuContants.ROLE_ODS_ORGADMIN);
		DataStore hasRightVds = de.query();
		boolean hasRight = false;
		if(hasRightVds!=null && hasRightVds.size() > 0){
			hasRight =  true;
		}
		DataObject result = DataObject.getInstance();
		result.put("hasright", hasRight);
		return result;
	}
	public DataObject openOrgAddSelectBljgRES(DataObject para) throws AppException{
		DE de = DE.getInstance();
		de.clearSql();				
  		de.addSql(" select distinct b.orgno,b.orgname,b.displayname,b.sleepflag ");
  		de.addSql("   from odssu.ir_org_closure a, ");
  		de.addSql("        odssu.orginfor b, ");
  		de.addSql("        odssu.org_type ot ");
  		de.addSql("  where a.belongorgno = :belongorgno ");
  		de.addSql("    and a.orgno = b.orgno ");
  		de.addSql("    and b.orgtype = ot.typeno ");
  		de.addSql("    and ot.typenature = 'B' ");
  		de.addSql("  order by b.orgno ");
		de.setString("belongorgno", OdssuContants.ORGROOT);
		DataStore orgAdminVds = de.query();
		DataObject result = DataObject.getInstance();
		result.put("orgds", orgAdminVds);
		return result;
	}
	public DataObject openEmpAddSelectBljgRES(DataObject para) throws AppException{
		String empno = para.getString("empno");
		String roleno = para.getString("roleno");
		DE de = DE.getInstance();
		de.clearSql();				
  		de.addSql(" select distinct a.orgno,b.orgname,b.displayname,b.sleepflag ");
  		de.addSql("   from odssu.ir_emp_org_all_role a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where a.orgno = b.orgno ");
  		de.addSql("    and a.empno = :empno ");
  		de.addSql("    and a.roleno = :roleno ");
		de.setString("empno", empno);
		de.setString("roleno", roleno);
		DataStore orgAdminVds = de.query();
		DataObject result = DataObject.getInstance();
		result.put("orgds", orgAdminVds);
		return result;
	}
	/**
	 * 描述：获取办理机构信息高新政务客户化
	 * author: sjn
	 * date: 2018年6月19日
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject openEmpAddSelectBljgRES_3701GXGWH(DataObject para) throws AppException{
		String empno = para.getString("empno");
		String adjustedempno = para.getString("adjustedempno","");
		String roleno = para.getString("roleno");
		DE de = DE.getInstance();
  		de.clearSql();				//lzpmark
  		de.addSql("select distinct d.orgno, d.orgname, d.displayname, d.sleepflag ");
  		de.addSql("  from odssu.ir_emp_org_all_role a, ");
  		de.addSql("       odssu.empinfor            b, ");
  		de.addSql("       odssu.ir_org_closure      c, ");
  		de.addSql("       odssu.orginfor            d, ");
  		de.addSql("       odssu.org_type            e ");
  		de.addSql(" where a.orgno = c.belongorgno ");
  		de.addSql("   and b.hrbelong = c.orgno ");
  		de.addSql("   and c.belongorgno = d.orgno ");
  		de.addSql("   and d.orgtype = e.typeno ");
  		de.addSql("   and e.typenature = 'B' ");
  		de.addSql("   and a.empno = :empno ");
  		de.addSql("   and a.roleno = :roleno ");
  		de.addSql("   and b.empno = :adjustedempno ");
		de.setString("empno",empno);
		de.setString("roleno",roleno);
		de.setString("adjustedempno",adjustedempno);
		DataStore orgAdminVds = de.query();
		DataObject result = DataObject.getInstance();
		result.put("orgds", orgAdminVds);
		return result;
	}
	/**
	 * 方法简介 ：获取人员新增办理机构个数 
	 *@author 郑海杰   2016年4月20日
	 */
	public DataObject getEmpAddBljgCount(DataObject para) throws AppException{
		String empno = para.getString("empno");
		String roleno = para.getString("roleno");
		DE de = DE.getInstance();
		
		String bljgid = "";
		
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
		DataObject result = DataObject.getInstance();
		if(sysAdminVds.size() > 0){
			result.put("sysadmin", "true");
			result.put("bljgcount", "1");
			de.clearSql();
			de.addSql("select a.hrbelong from odssu.empinfor a where a.empno = :empno ");
			de.setString("empno", empno);
			DataStore belongorgno = de.query();
			bljgid = belongorgno.getString(0, "hrbelong");
			result.put("bljgid", bljgid);
		}else{
			de.clearSql();
  			de.addSql(" select distinct a.orgno ");
  			de.addSql("   from odssu.ir_emp_org_all_role a, ");
  			de.addSql("        odssu.orginfor b ");
  			de.addSql("  where a.orgno = b.orgno ");
  			de.addSql("    and a.empno = :empno ");
  			de.addSql("    and a.roleno = :roleno ");
			de.setString("empno", empno);
			de.setString("roleno", roleno);
			DataStore orgAdminVds = de.query();
			result.put("sysadmin", "false");
			result.put("bljgcount", orgAdminVds.size()+"");
			if(orgAdminVds.size() == 1){
				result.put("bljgid", orgAdminVds.getString(0, "orgno"));
			}
		}
		return result;
	}
	/**
	 * 描述：调整人员隶属机构判断当前操作员是否有被调整人的人事隶属机构的单位管理员
	 * author: sjn
	 * date: 2018年6月19日
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject getEmpAddBljgCount_3701GXGWH(DataObject para) throws AppException{
		String empno = para.getString("empno");
		String adjustedempno = para.getString("adjustedempno","");
		String roleno = para.getString("roleno");
		DE de = DE.getInstance();
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
		DataObject result = DataObject.getInstance();
		if(sysAdminVds.size() > 0){
			result.put("sysadmin", "true");
			result.put("bljgid", OdssuContants.ORGROOT);
			result.put("bljgcount", "1");
		}else{
			de.clearSql();				//lzpmark
  			de.addSql("select distinct d.orgno ");
  			de.addSql("  from odssu.ir_emp_org_all_role a, ");
  			de.addSql("       odssu.empinfor            b, ");
  			de.addSql("       odssu.ir_org_closure      c, ");
  			de.addSql("       odssu.orginfor            d, ");
  			de.addSql("       odssu.org_type            e ");
  			de.addSql(" where a.orgno = c.belongorgno ");
  			de.addSql("   and b.hrbelong = c.orgno ");
  			de.addSql("   and c.belongorgno = d.orgno ");
  			de.addSql("   and d.orgtype = e.typeno ");
  			de.addSql("   and e.typenature = 'B' ");
  			de.addSql("   and a.empno = :empno ");
  			de.addSql("   and a.roleno = :roleno ");
  			de.addSql("   and b.empno = :adjustedempno ");
			de.setString("empno",empno);
			de.setString("roleno",roleno);
			de.setString("adjustedempno",adjustedempno);
			DataStore orgAdminVds = de.query();
			result.put("sysadmin", "false");
			result.put("bljgcount", orgAdminVds.size()+"");
			if(orgAdminVds.size() == 1){
				result.put("bljgid", orgAdminVds.getString(0, "orgno"));
			}
		}
		return result;
	}
	//获取人员新增流程的办理机构id
	public DataObject getEmpAddBljg(DataObject para) throws AppException {
		String orgno = para.getString("orgno");
		DataObject result = DataObject.getInstance();
		result.put("bljgid", orgno);
		
		return result;
	}
	/**
	 * 查询人员的状态和信息
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-21
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject queryEmpStateAndInfor(final DataObject para) throws Exception {
		String empno = para.getString("empno");
		String dbid = GlobalNames.DEBUGMODE ?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;

		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" select sleepflag,rname from odssu.empinfor where empno  =:empno  ");
		de.setString("empno", empno);
		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			throw new Exception("没有找到编号为【" + empno + "】的信息！");
		}
		String sleepflag = vds.getString(0, "sleepflag");
		String rname = vds.getString(0, "rname");
		if (sleepflag == null || sleepflag.trim().isEmpty()) {
			throw new Exception("编号为【" + empno + "】的操作员【"+rname+"】的在职状态为空！");
		}
		de.clearSql();
  		de.addSql(" select e.empno,e.empname,e.rname,e.loginname username, ");
  		de.addSql("  case when e.sleepflag='0' then '在职' when e.sleepflag='1' then '离职' end sleepflag, ");
  		de.addSql("  case when e.gender='1' then '男' when e.gender='2' then '女' end gender, ");
  		de.addSql("        o.orgno,o.orgname,o.displayname,e.idcardno,e.email,e.mphone,e.officetel ");
  		de.addSql(" from   odssu.empinfor e left outer join odssu.orginfor o ");
  		de.addSql("        on e.hrbelong = o.orgno ");
  		de.addSql(" where  empno = :empno ");
		de.setString("empno", empno);

		DataStore vdsemp = de.query();
		//登录模式：0：不使用UKey登录；1：支持一代UKey登录；2：支持二代UKey登录；3：支持一代和二代UKey登录。
		String csz = "";
		if (dbid!=null && dbid.equals("102")) {
			de.clearSql();
  			de.addSql("select a.csz from odssu.sys_para a where a.csm = 'HSU_LOGON_MODE' ");
			DataStore tempVds = de.query();
			if(tempVds.size()>0) csz = tempVds.getString(0, "csz");
		}
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("zzzt", sleepflag);
		vdo.put("empds", vdsemp);
		vdo.put("rname", rname);
		vdo.put("csz", csz);
		return vdo;
	}

	/**
	 * 人员查询树-查询人员隶属机构以及在此机构中的角色
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-22
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageEmpBelongOrgWithRoleJsp(DataObject para) throws Exception {
		String empno = para.getString("empno");
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select v.post,u.orgno,u.orgname,u.displayname ");
  		de.addSql(" from   odssu.orginfor u,  ");
  		de.addSql("        odssu.ir_grant v ");
  		de.addSql(" where  v.pidenttno = u.orgno  ");
  		de.addSql("        and v.cidenttno = :empno ");
  		de.addSql(" order by u.orgno ");
		de.setString("empno", empno);
		DataStore vds = de.query();

		for (int i = 0; i < vds.rowCount(); i++) {
			String orgno = vds.getString(i, "orgno");

			de.clearSql();
  			de.addSql(" select 1 from odssu.empinfor ");
  			de.addSql(" where empno= :empno and hrbelong = :orgno ");
			de.setString("empno", empno);
			de.setString("orgno", orgno);
			DataStore vds1 = de.query();

			if (vds1.rowCount() > 0) {
				vds.put(i, "ishrbelong", "1");
			} else {
				vds.put(i, "ishrbelong", "0");
			}

			de.clearSql();
  			de.addSql(" select r.roleno,r.rolename,r.displayname ");
  			de.addSql(" from   odssu.roleinfor r,  ");
  			de.addSql("        odssu.ir_emp_inner_unduty_role e ");
  			de.addSql(" where r.roleno = e.roleno and empno = :empno and orgno = :orgno  ");
  			de.addSql(" order by r.roleno ");
			de.setString("empno", empno);
			de.setString("orgno", orgno);
			DataStore vds2 = de.query();
			String rolestr = "";
			for (int j = 0; j < vds2.rowCount(); j++) {
				String displayname = vds2.getString(j, "displayname");
				if ("".equals(rolestr) == false) {
					rolestr = rolestr + "，" + displayname;
				} else {
					rolestr = displayname;
				}
			}
			vds.put(i, "post", rolestr);
		}
		de.clearSql();
  		de.addSql("select * 			 ");
  		de.addSql("  from odssu.empinfor  ");
  		de.addSql(" where empno = :empno		 ");
		de.setString("empno", empno);
		DataStore dstmp = de.query();
		if (dstmp.rowCount() == 0) {
			throw new BusinessException("查询人员信息出错!");
		}
		String sleepflag = dstmp.getString(0, "sleepflag");

		DataObject vdo = DataObject.getInstance();
		vdo.put("empds", vds);
		vdo.put("sleepflag", sleepflag);
		return vdo;
	}

	/**
	 * 人员查询树-查询人员对机构的干系角色
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-22
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageIrEmpOutRoleJsp(DataObject para) throws Exception {
		String empno = para.getString("empno");
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" select sleepflag from odssu.empinfor where empno  =:empno  ");
		de.setString("empno", empno);
		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			throw new Exception("没有找到编号为【" + empno + "】的信息！");
		}

		de.clearSql();
  		de.addSql(" select o.orgno,o.orgname,o.displayname orgdisplayname,r.roleno,r.rolename,r.displayname roledisplayname ");
  		de.addSql(" from   odssu.ir_emp_org_all_role i, ");
  		de.addSql("        odssu.orginfor o, ");
  		de.addSql("        odssu.roleinfor r ");
  		de.addSql(" where  i.rolenature = '5' and i.orgno = o.orgno and i.roleno = r.roleno ");
  		de.addSql("      and  i.empno = :empno ");
  		de.addSql(" order by o.orgno ");
		de.setString("empno", empno);
		DataStore vdsemp = de.query();

		de.clearSql();
  		de.addSql("select * 			 ");
  		de.addSql("  from odssu.empinfor  ");
  		de.addSql(" where empno = :empno		 ");
		de.setString("empno", empno);
		DataStore dstmp = de.query();
		if (dstmp.rowCount() == 0) {
			throw new BusinessException("查询人员信息出错!");
		}
		String sleepflag = dstmp.getString(0, "sleepflag");

		DataObject vdo = DataObject.getInstance();
		vdo.put("belongds", vdsemp);
		vdo.put("sleepflag", sleepflag);
		return vdo;
	}

	/**
	 * 根据人员编号获取业务受理单位编号
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-25
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject getYwbljgByEmpno(DataObject para) throws Exception {
		String empno = para.getString("empno");

		if (empno == null || empno.trim().isEmpty()) {
			this.bizException("人员编号【" + empno + "】为空！");
		}
  		de.clearSql();
  		de.addSql(" select 1 from odssu.empinfor where empno = :empno ");
		de.setString("empno", empno);
		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			this.bizException("没有找到编号为【" + empno + "】的人员的基本信息！");
		}

		String orgno = OdssuUtil.getYwbljgByEmpno(empno);

		DataObject vdo = DataObject.getInstance();
		vdo.put("orgno", orgno);
		return vdo;

	}

	/**
	 * 人员查询树-查询人员的基本信息
	 * 
	 * @Description:
	 * @author 张宗泽
	 * @date 2014-7-25
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageIrEmpInforJsp(DataObject para) throws Exception {
		String empno = para.getString("empno");
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select empno,idcardno,empname,rname,empnamepy,rnamepy,gender,  ");
  		de.addSql("        sleepflag,officetel,mphone,email,hrbelong,loginname ");
  		de.addSql(" from odssu.empinfor ");
  		de.addSql(" where empno=:empno");
		de.setString("empno", empno);
		DataStore vds_empinfor = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("vds_empinfor", vds_empinfor);
		return vdo;

	}

	/**
	 * * 人员查询树-查询人员隶属机构信息
	 * 
	 * @Description:
	 * @author 张宗泽
	 * @date 2014-7-25
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageIrEmpOrgJsp(DataObject para) throws Exception {
		String empno = para.getString("empno");
		DE de = DE.getInstance();
		de.clearSql();
    	de.addSql(" select e.empno , e.orgno ir_emp_org, o.orgno orginfor ,o.orgname orgname, e.ishrbelong ");
  		de.addSql(" from odssu.ir_emp_org e left outer join odssu.orginfor o ");
  		de.addSql(" 	  on e.orgno=o.orgno ");
  		de.addSql(" where e.empno=:empno ");
		de.setString("empno", empno);

		DataStore vds_emporginfo = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("vds_emporginfo", vds_emporginfo);
		return vdo;

	}

	/**
	 * debug--人员拥有的干系角色
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-1
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageDebugIrEmpOrgOuterRole(DataObject para) throws Exception {
		String empno = para.getString("empno");
		DE de = DE.getInstance();
		de.clearSql();
    	de.addSql(" select e.orgno ir_emp_org_all_role_orgno , e.roleno ir_emp_org_all_role_roleno , r.rolename  roleinfor_rolename , ");
  		de.addSql("        m.orgname orginfor_orgname , e.rolenature ir_emp_org_all_role_rolenature ");
  		de.addSql(" from odssu.ir_emp_org_all_role e left outer join odssu.roleinfor r  ");
  		de.addSql("  	  on e.roleno=r.roleno, ");
  		de.addSql("  	  odssu.ir_emp_org_all_role e1 left outer join odssu.orginfor m ");
  		de.addSql(" 	  on e1.orgno = m.orgno ");
  		de.addSql(" where e.empno = :empno and e.rolenature = '5'");
		de.setString("empno", empno);

		DataStore vds_emproleinfo = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("vds_emporgouterroleinfo", vds_emproleinfo);
		return vdo;

	}

	/**
	 * 通过操作员编号获取操作员有权限的所有人事单位信息. 新增人员.OHRMANAGER
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-6
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject getYwbljgByEmpnoNew(DataObject para) throws Exception {

		String empno = para.getString("empno");
		if (empno == null || empno.trim().isEmpty()) {
			this.bizException("人员编号为空！");
		}
		DataStore orgds = OdssuUtil.getOrgInforOfEmpnoByRoleNo(empno);
		DataObject vdo = DataObject.getInstance();
		vdo.put("orgds", orgds);
		return vdo;
	}

	/**
	 * debug -- 人员直属上级信息
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-11
	 * @param para
	 * @return
	 * @throws Exception
	 */

	public DataObject fwPageDebugEmpGrantSupInfor(DataObject para) throws Exception {
		String empno = para.getString("empno");

		if (empno == null || empno.trim().isEmpty()) {
			this.bizException("人员编号为空！");
		}
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select  g.pidenttno ir_grant_pidenttno , g.pidentttype ir_grant_pidentttype  , g.post ir_grant_post , g.sn ir_grant_sn , g.ishrbelong ir_grant_ishrbelong ");
  		de.addSql(" from    odssu.ir_grant g ");
  		de.addSql(" where g.cidenttno = :empno ");
		de.setString("empno", empno);
		DataStore vds = de.query();
		// vds.setTypeList("ir_grant_pidenttno:s,ir_grant_pidentttype:s,ir_grant_post:s,ir_grant_sn:n,ir_grant_ishrbelong:s");

		DataObject vdo = DataObject.getInstance();
		vdo.put("supds", vds);
		return vdo;
	}

	/**
	 * 描述:人员在机构下的角色
	 * 
	 * @author 叶军 日期:2014-8-29
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageDebugEmpRoleUnderOrgJsp(final DataObject para) throws Exception {
		String empno = para.getString("empno");
		if (empno == null || "".equals(empno.trim())) {
			this.bizException("人员编号为空");
		}
		DE de = DE.getInstance();
    	de.clearSql();// 初始化SQL语句
		DataObject vdo = DataObject.getInstance();
		DataStore empRoleOrg;
  		de.addSql("select a.whoid,a.roleid,a.whomid,a.whomtype,b.orgname,c.rolename    ");
  		de.addSql("  from odsv.emp_role_under_org_simple_view a left outer join odssu.orginfor  b ");
  		de.addSql("       on a.whomid = b.orgno ,         ");
  		de.addSql("       odsv.emp_role_under_org_simple_view a left outer join odssu.roleinfor c           ");
  		de.addSql("       on a.roleid = c.roleno          ");
  		de.addSql(" where a.whoid = :empno                 ");
  		de.addSql(" order by a.whomid                 ");
		de.setString("empno", empno);

		empRoleOrg = de.query();
		vdo.put("emproleorg", empRoleOrg);
		return vdo;
	}

	/**
	 * 描述:人员在机构上的角色
	 * 
	 * @author 叶军 日期:2014-8-29
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageDebugEmpRoleUponOrgJsp(final DataObject para) throws Exception {
		String empno = para.getString("empno");
		if (empno == null || "".equals(empno.trim())) {
			this.bizException("人员编号为空");
		}
		DE de = DE.getInstance();
    	de.clearSql();// 初始化SQL语句
		DataObject vdo = DataObject.getInstance();
		DataStore empRoleOrg;
  		de.addSql("select a.whoid,a.roleid,a.whomid,a.whomtype,b.orgname,c.rolename    ");
  		de.addSql("  from odsv.emp_role_under_org_simple_view a left outer join odssu.orginfor  b ");
  		de.addSql("       on a.whomid = b.orgno ,         ");
  		de.addSql("       odsv.emp_role_under_org_simple_view a left outer join odssu.roleinfor c           ");
  		de.addSql("       on a.roleid = c.roleno          ");
  		de.addSql(" where a.whoid = :empno                 ");
  		de.addSql(" order by a.whomid                 ");
		de.setString("empno", empno);
		empRoleOrg = de.query();
		vdo.put("emproleorg", empRoleOrg);
		return vdo;
	}

	/**
	 * 描述:人员在机构的角色
	 * 
	 * @author 叶军 日期:2014-8-29
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageDebugEmpRoleAtOrgJsp(final DataObject para) throws Exception {
		String empno = para.getString("empno");
		if (empno == null || "".equals(empno.trim())) {
			this.bizException("人员编号为空");
		}
		DE de = DE.getInstance();
    	de.clearSql();// 初始化SQL语句
		DataObject vdo = DataObject.getInstance();
		DataStore empRoleOrg;
    	de.addSql("select a.whoid,a.roleid,a.whomid,a.whomtype,b.orgname,c.rolename    ");
  		de.addSql("  from odsv.emp_role_under_org_simple_view a left outer join odssu.orginfor  b ");
  		de.addSql("       on a.whomid = b.orgno ,         ");
  		de.addSql("       odsv.emp_role_under_org_simple_view a left outer join odssu.roleinfor c           ");
  		de.addSql("       on a.roleid = c.roleno          ");
  		de.addSql(" where a.whoid = :empno                 ");
  		de.addSql(" order by a.orgno                  ");
		de.setString("empno", empno);
		empRoleOrg = de.query();
		vdo.put("emproleorg", empRoleOrg);

		return vdo;
	}

	/**
	 * 查询人员新增是否记账完成
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-9-1
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject checkEmpAddIfSaveSuccess(DataObject para) throws Exception {
		String piid = para.getString("piid");
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" select ryxzjzbz,empno,empname from odssuws.ryjbxxxzkjb where piid = :piid  ");
		de.setString("piid", piid);
		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			this.bizException("没有找到piid的【" + piid + "】的流程的信息，请检查!");
		}
		String jzbz = vds.getString(0, "ryxzjzbz");
		String empno = vds.getString(0, "empno");
		String empname = vds.getString(0, "empname");

		if ("1".equals(jzbz)) {
			de.clearSql();
  			de.addSql(" select empno from odssu.empinfor where empname = :empname ");
			de.setString("empname", empname);
			DataStore vds1 = de.query();

			if (vds1.rowCount() == 0) {
				this.bizException("没有找到人员【" + empname + "】的信息，请检查！");
			}
			empno = vds1.getString(0, "empno");
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("jzbz", jzbz);
		vdo.put("empno", empno);
		vdo.put("empname", empname);
		return vdo;
	}

	/**
	 * 查询人员复职是否记账完成
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-9-1
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject checkEmpRegainIfSaveSuccess(DataObject para) throws Exception {
		String piid = para.getString("piid");
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" select ryfzjzbz,empno,empname from odssuws.ryfz where piid = :piid  ");
		de.setString("piid", piid);
		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			this.bizException("没有找到piid的【" + piid + "】的流程的信息，请检查!");
		}
		String jzbz = vds.getString(0, "ryfzjzbz");
		String empno = vds.getString(0, "empno");
		String empname = vds.getString(0, "empname");

		if ("1".equals(jzbz)) {
			de.clearSql();
  			de.addSql(" select empno from odssu.empinfor where empname = :empname ");
			de.setString("empname", empname);
			DataStore vds1 = de.query();

			if (vds1.rowCount() == 0) {
				this.bizException("没有找到人员【" + empname + "】的信息，请检查！");
				empno = vds1.getString(0, "empno");
			}
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("jzbz", jzbz);
		vdo.put("empno", empno);
		vdo.put("empname", empname);
		return vdo;
	}

	/**
	 * 描述:当前用户在activiti中的角色 author:YeJun 2014-9-4 上午10:12:48
	 * 
	 * @param para
	 * @return
	 * @throws Exception DataObject
	 */
	public final DataObject fwPageDebugRoleOfActivitiJsp(final DataObject para) throws Exception {
		String empno = para.getString("empno").trim();
		if (empno == null || "".equals(empno)) {
			this.bizException("获取人员编号时为空");
		}
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select '1' from odssu.empinfor where empno = :empno ");
		de.setString("empno", empno);
		DataStore existsEmp = de.query();
		if (existsEmp.rowCount() == 0) {
			this.bizException("不存在编号为" + empno + "的人员信息");
		}
		de.clearSql();
  		de.addSql(" select b.id_,b.name_                ");
  		de.addSql("  from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_user a,       ");
  		de.addSql("       "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_group b,      ");
  		de.addSql("       "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership c  ");
  		de.addSql(" where a.id_ = c.user_id_            ");
  		de.addSql("   and b.id_ = c.group_id_           ");
  		de.addSql("   and a.id_ = :empno                     ");
		de.setString("empno", empno);
		DataStore vds = de.query();
		DataObject vdo = DataObject.getInstance();
		vdo.put("vds", vds);
		return vdo;
	}

	/**
	 * 人员新增时，打开选择办理单位和身份证号的response
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-9-4
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject openEmpAddResp(DataObject para) throws Exception {
		String empno = para.getString("empno");
		if (empno == null || empno.trim().isEmpty()) {
			this.bizException("人员编号为空！");
		}
		DE de = DE.getInstance();
		de.clearSql();					//lzpmark
  		 de.addSql(" select distinct b.orgno value, b.displayname content,b.orgname");
  		 de.addSql("   from odsv.emp_role_upon_org_simple_view a,");
  		 de.addSql("        odssu.orginfor  b,");
  		 de.addSql("        odssu.org_type c");
  		 de.addSql("   where a.whoid = :empno");
  		 de.addSql("   and a.whomid = b.orgno");
  		 de.addSql("   and b.orgtype = c.typeno");
  		 de.addSql("   and c.typenature = 'B' ");
  		 de.addSql("   order by b.orgno ");
		 de.setString("empno", empno);
		 DataStore vds = de.query();

		int rowNum = 0;
		String orgno = "";
		if (vds.rowCount() == 1) {
			rowNum = 1;
			orgno = vds.getString(0, "value");
		} 
		else if(vds.rowCount()==0)
		{
			orgno = "3705";
		}
		else {
			orgno = vds.getString(0, "value");
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("daiweids", vds);
		vdo.put("orgno", orgno);
		vdo.put("rownum", rowNum);

		return vdo;
	}

	/**
	 * 人员新增时，根据身份证号判断是新增还是复职
	 * 
	 * @param request
	 * @param response
	 * @param para
	 * @return
	 * @throws Exception ModelAndView
	 */
	public DataObject checkAddEmpOrRegain(DataObject para) throws Exception {
		String idcardno = para.getString("idcardno");

		if (idcardno == null || idcardno.trim().isEmpty()) {
			this.bizException("传入的身份证号为空，开启流程失败！");
		}
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" select empno,empname,sleepflag,rname from odssu.empinfor  where idcardno = :idcardno ");
		de.setString("idcardno", idcardno);
		DataStore vds = de.query();

		String flag = "0";
		String empno = "";
		String empname = "";
		String rname = "";
		if (vds.rowCount() > 0) {
			String sleepflag = vds.getString(0, "sleepflag");
			if ("1".equals(sleepflag)) {
				flag = "1";
				empno = vds.getString(0, "empno");
				empname = vds.getString(0, "empname");
				rname = vds.getString(0, "rname");
			} else {
				flag = "2";
			}
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("flag", flag);
		vdo.put("empno", empno);
		vdo.put("empname", empname);
		vdo.put("rname", rname);
		return vdo;
	}
	
	/**
	 * 描述：打开人员排序res
	 * author: sjn
	 * date: 2017年8月1日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject sortEmp(final DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql(" select u.empno,u.empname,u.gender,u.email,u.loginname username, ");
		de.addSql("        u.officetel, u.sleepflag,u.mphone,v.ishrbelong,' ' rolename,nvl(v.empsn,10000) empsn ");
		de.addSql("   from   odssu.empinfor u, ");
		de.addSql("        odssu.ir_emp_org v ");
		de.addSql("  where  v.empno = u.empno ");
		de.addSql("    and v.orgno =:orgno ");
		de.addSql("        order by v.empsn,u.empno ");
		de.setString("orgno", orgno);
		DataStore empvds = de.query();
		for (int i = 0; i < empvds.size(); i++) {
			String empno = empvds.getString(i, "empno");
			de.clearSql();
  			de.addSql(" select rolename,b.rolesn ");
  			de.addSql("   from odssu.ir_emp_org_all_role a, ");
  			de.addSql("        odssu.roleinfor b ");
  			de.addSql("  where a.empno = :empno ");
  			de.addSql("    and a.orgno = :orgno ");
  			de.addSql("    and a.roleno = b.roleno ");
  			de.addSql("    and a.rolenature = :cyjs");
  			de.addSql("    and a.roleno <> 'MEMBER' ");
  			de.addSql("    and a.jsgn = :post order by b.rolesn asc");
			de.setString("empno", empno);
			de.setString("orgno", orgno);
			de.setString("cyjs", OdssuContants.ROLENATURE_CYJS);
			de.setString("post", OdssuContants.JSGN_POST);

			
			DataStore roleNameVds = de.query();
			StringBuffer roleNameBF = new StringBuffer();
			roleNameBF.append("");
			int rolesn = 100;
			for (int j = 0; j < roleNameVds.size(); j++) {
				String roleName = roleNameVds.getString(j, "rolename");
				Integer rolesnInt = roleNameVds.getInt(j, "rolesn");
				if (rolesnInt != null && rolesnInt != 0) {
					if (rolesnInt < rolesn) {
						rolesn = rolesnInt;
					}
				}
				roleNameBF.append(roleName + ",");
			}
			if (roleNameBF.length() > 0) {
				roleNameBF.deleteCharAt(roleNameBF.length() - 1);
			}
			empvds.put(i, "rolename", roleNameBF.toString());
			empvds.put(i, "rolesn", rolesn);
		}

		DataObject vdo = DataObject.getInstance();
		MultiSortUtil.multiSortDS(empvds, "empsn:asc");
		//对人员进行重新排序
		for (int i = 0; i < empvds.rowCount(); i++) {
			String empno = empvds.getString(i, "empno");
			de.clearSql();
  			de.addSql(" update odssu.ir_emp_org a set empsn = :empsn where a.orgno = :orgno and a.empno = :empno ");
			de.setInt("empsn", i+1);
			de.setString("orgno", orgno);
			de.setString("empno", empno);
			de.update();
			empvds.put(i, "empsn", i+1);
		}
		vdo.put("emplist", empvds);
		return vdo;
	}
	
	/**
	 * 描述：人员排序
	 * author: sjn
	 * date: 2017年8月1日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject moveEmpsn(DataObject para) throws Exception{
		String orgno = para.getString("orgno");
		String empno = para.getString("empno");
		int empsn = para.getInt("empsn");
		String movetype = para.getString("movetype");
		if (orgno == null || orgno.equals("")) {
			this.bizException("机构编号为空！");
		}
		if (empno == null || empno.equals("")) {
			this.bizException("人员编号编号为空！");
		}
		if (movetype == null || movetype.equals("")) {
			this.bizException("不可识别的排序方式！");
		}
		DataStore vds = DataStore.getInstance();
		DE de = DE.getInstance();  		
		//上移一位
		if (movetype.equals("1")) {
			if (empsn == 1) {
				return null;
			}
			de.clearSql();
  			de.addSql("select a.empno, a.empsn ");
  			de.addSql("  from odssu.ir_emp_org a ");
  			de.addSql(" where a.orgno = :orgno ");
  			de.addSql("   and a.empsn < :empsn ");
  			de.addSql(" order by a.empsn desc ");
			de.setString("orgno",orgno);
			de.setInt("empsn",empsn);
			vds = de.query();
			String nextEmpno = vds.getString(0, "empno");
			int nextEmpsn = vds.getInt(0, "empsn");
			//交换orgsn
			changeEmpsn(orgno,empno,nextEmpno,empsn,nextEmpsn);
		}
		//下移一位
		if (movetype.equals("2")) {
			de.clearSql();
  			de.addSql("select a.empno, a.empsn ");
  			de.addSql("  from odssu.ir_emp_org a ");
  			de.addSql(" where a.orgno = :orgno ");
  			de.addSql("   and a.empsn > :empsn ");
  			de.addSql(" order by a.empsn");
			de.setString("orgno",orgno);
			de.setInt("empsn",empsn);
			vds = de.query();
			if (vds == null || vds.rowCount() == 0) {
				return null;
			} 
			String nextEmpno = vds.getString(0, "empno");
			int nextEmpsn = vds.getInt(0, "empsn");
			//交换orgsn
			changeEmpsn(orgno,empno,nextEmpno,empsn,nextEmpsn);
		}
		//上移到顶
		if (movetype.equals("3")) {
			if (empsn == 1) {
				return null;
			}
			de.clearSql();
  			de.addSql("update odssu.ir_emp_org a set empsn = empsn + 1 where a.orgno = :orgno and a.empsn < :empsn ");
			de.setString("orgno",orgno);
			de.setInt("empsn",empsn);
			de.update();
			de.clearSql();
  			de.addSql("update odssu.ir_emp_org a set empsn = :empsn where a.empno = :empno and a.orgno = :orgno ");
			de.setInt("empsn",1);
			de.setString("empno",empno);
			de.setString("orgno",orgno);
			de.update();
		}
		//下移到底
		if (movetype.equals("4")) {
			de.clearSql();
  			de.addSql("select a.empsn ");
  			de.addSql("  from odssu.ir_emp_org a ");
  			de.addSql(" where a.empsn > :empsn ");
  			de.addSql("   and a.orgno = :orgno ");
  			de.addSql("  order by a.empsn desc ");
			de.setInt("empsn",empsn);
			de.setString("orgno",orgno);
			vds = de.query();
			if (vds == null || vds.rowCount() == 0) {
				return null;
			}
			int maxempsn = vds.getInt(0, "empsn");
			de.clearSql();
  			de.addSql("update odssu.ir_emp_org a set empsn = empsn - 1 where a.orgno = :orgno and a.empsn > :empsn ");
			de.setString("orgno",orgno);
			de.setInt("empsn",empsn);
			de.update();
			de.clearSql();
  			de.addSql("update odssu.ir_emp_org a set empsn = :maxempsn where a.empno = :empno and a.orgno = :orgno ");
			de.setInt("maxempsn",maxempsn);
			de.setString("empno",empno);
			de.setString("orgno",orgno);
			de.update();
		}
		
		return null;
	}
	
	private static void changeEmpsn(String orgno,String empno,String nextEmpno,int empsn,int nextEmpsn) throws Exception {
		DE de = DE.getInstance();
  		de.clearSql();
		//交换orgsn
  		de.addSql("update odssu.ir_emp_org a set empsn = :empsn where a.empno = :nextempno and a.orgno = :orgno ");
		de.setInt("empsn",empsn);
		de.setString("nextempno",nextEmpno);
		de.setString("orgno",orgno);
		de.update();
		de.clearSql();
  		de.addSql("update odssu.ir_emp_org a set empsn = :nextempsn where a.empno = :empno and a.orgno = :orgno ");
		de.setInt("nextempsn",nextEmpsn);
		de.setString("empno",empno);
		de.setString("orgno",orgno);
		de.update();
	}
	
	/**
	 * 描述：刷新emplist
	 * author: sjn
	 * date: 2017年8月2日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject refreshEmpList(final DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" select u.empno,u.empname,u.gender,u.email,u.loginname username, ");
  		de.addSql("        u.officetel, u.sleepflag,u.mphone,v.ishrbelong,' ' rolename,v.empsn ");
  		de.addSql("   from   odssu.empinfor u, ");
  		de.addSql("        odssu.ir_emp_org v ");
  		de.addSql("  where  v.empno = u.empno ");
  		de.addSql("    and v.orgno =:orgno ");
  		de.addSql("        order by v.empsn,u.empno ");
		de.setString("orgno", orgno);
		DataStore empvds = de.query();
		for (int i = 0; i < empvds.size(); i++) {
			String empno = empvds.getString(i, "empno");
			de.clearSql();
  			de.addSql(" select rolename,b.rolesn ");
  			de.addSql("   from odssu.ir_emp_org_all_role a, ");
  			de.addSql("        odssu.roleinfor b ");
  			de.addSql("  where a.empno = :empno ");
  			de.addSql("    and a.orgno = :orgno ");
  			de.addSql("    and a.roleno = b.roleno ");
  			de.addSql("    and a.rolenature = :cyjs");
  			de.addSql("    and a.roleno <> 'MEMBER' ");
  			de.addSql("    and a.jsgn = :post order by b.rolesn asc");
			de.setString("empno", empno);
			de.setString("orgno", orgno);
			de.setString("cyjs", OdssuContants.ROLENATURE_CYJS);
			de.setString("post", OdssuContants.JSGN_POST);
			DataStore roleNameVds = de.query();
			StringBuffer roleNameBF = new StringBuffer();
			roleNameBF.append("");
			int rolesn = 100;
			for (int j = 0; j < roleNameVds.size(); j++) {
				String roleName = roleNameVds.getString(j, "rolename");
				Integer rolesnInt = roleNameVds.getInt(j, "rolesn");
				if (rolesnInt != null && rolesnInt != 0) {
					if (rolesnInt < rolesn) {
						rolesn = rolesnInt;
					}
				}
				roleNameBF.append(roleName + ",");
			}
			if (roleNameBF.length() > 0) {
				roleNameBF.deleteCharAt(roleNameBF.length() - 1);
			}
			empvds.put(i, "rolename", roleNameBF.toString());
			empvds.put(i, "rolesn", rolesn);
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("emplist", empvds);
		return vdo;
	}
}
