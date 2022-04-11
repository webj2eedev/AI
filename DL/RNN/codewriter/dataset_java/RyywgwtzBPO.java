package com.dw.odssu.ws.emp.ryywgwtz;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import com.dareway.apps.process.util.ProcessUtil;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class RyywgwtzBPO extends BPO{
	
	/**
	 * 比较此次流程是否有角色信息发生变化，有一处发生变化即可return；
	 * 若此次流程无任何变动，则弹出businessException，提醒用户。
	 * wjn
     */
	public DataObject checkChange(DataObject para) throws AppException,BusinessException{
		String piid = para.getString("piid");

		de.clearSql();
		de.addSql(" select a.piid, a.roleno, a.orgno, a.empno, a.opflag "); 
		de.addSql("  from odssuws.emp4role_detal a  "); 
		de.addSql(" where a.opflag is not null "); 
		de.addSql("   and a.piid = :piid");
		this.de.setString("piid", piid);
		DataStore zwgdb = this.de.query();
		if (zwgdb != null && zwgdb.rowCount() > 0) {
			return null;
		}
		
		throw new BusinessException("您的本次操作没有对操作员拥有的角色进行调整，建议您作废此流程或对操作员拥有的角色调整后，再提交审批。");
	}
	/**
	 * 方法简介 ：获取没有权限时的处理方法 
	 *@author 郑海杰   2016年4月20日
	 * @throws AppException 
	 * @throws BusinessException 
	 */
	public DataObject getNoRightDealMethod(DataObject para) throws AppException, BusinessException{
		String empno = para.getString("empno");
  		de.clearSql();
  		de.addSql(" select b.orgname ");
  		de.addSql("   from odssu.ir_emp_org a, ");
  		de.addSql("        odssu.orginfor b, ");
  		de.addSql("        odssu.org_type c ");
  		de.addSql("  where a.empno = :empno ");
  		de.addSql("    and a.orgno = b.orgno ");
  		de.addSql("    and b.orgtype = c.typeno ");
  		de.addSql("    order by c.sn ");
		this.de.setString("empno", empno);
		DataStore orgNameVds = this.de.query();
		StringBuffer dealMehodBF = new StringBuffer();
		dealMehodBF.append("");
		if(orgNameVds == null || orgNameVds.size() == 0){
			this.bizException("未找到编号为【" + empno + "】人员的隶属机构");
		}else{
			dealMehodBF.append("能办理此业务的有:");
			for(int i = 0; i < orgNameVds.size(); i++){
				String orgname = orgNameVds.getString(i, "orgname");
				dealMehodBF.append("【" + orgname + "】");
				if(i < orgNameVds.size() - 1){
					dealMehodBF.append("或者");
				}
			}
			dealMehodBF.append("的【工作分配干系人】");
		}
		DataObject result = DataObject.getInstance();
		result.put("dealmethod", dealMehodBF.toString());
		return result;
	}
	/**
	/**
	 * 方法简介：计算有可能调整岗位的机构个数
	 * 郑海杰  2015-11-12
	 */
	public DataObject getYwgwtzJgCount(DataObject para) throws AppException{
		DataStore orgVds = calculateYwgwtzJg(para);
		int jgCount = 0;
		if(orgVds != null && orgVds.size() > 0){
			jgCount = orgVds.size();
		}
		DataObject result = DataObject.getInstance();
		result.put("jgcount", jgCount);
		return result;
	}
	/**
	 * 方法简介：当可能调整岗位的机构只有一个时，
	 *         获取机构编号
	 * 郑海杰  2015-11-12
	 * @throws BusinessException 
	 */
	public DataObject getYwgwtzJgOrgno(DataObject para) throws AppException, BusinessException{
		DataStore orgVds = calculateYwgwtzJg(para);
		if(orgVds == null || orgVds.size() == 0){
			throw new BusinessException("获取可能调整岗位的机构编号失败!");
		}
		String orgno = orgVds.getString(0,"orgno");
		DataObject result = DataObject.getInstance();
		result.put("orgno", orgno);
		return result;
	}
	public DataObject getYwgwtzJgList(DataObject para) throws AppException, BusinessException{
		DataStore orgVds = calculateYwgwtzJg(para);
		if(orgVds == null || orgVds.size() == 0){
			throw new BusinessException("获取可能调整岗位的机构失败!");
		}
		DataObject result = DataObject.getInstance();
		result.put("orgds", orgVds);
		return result;
	}
	/**
	 * 方法简介：计算有可能调整岗位的机构
	 * 郑海杰  2015-11-12
	 */
	private DataStore calculateYwgwtzJg(DataObject para) throws AppException{
		//被调整人编号
		String empno = para.getString("empno");
		//操作员编号
		String userid = this.getUser().getUserid();
		//计算有可能调整岗位的机构
		// 有可能调整岗位的机构 = {被调整人隶属的机构} ∩ {操作员是工作分配（干系）人的机构}

		de.clearSql();
  		de.addSql(" select a.orgno,b.orgname,b.sleepflag,b.displayname ");
  		de.addSql("   from odssu.ir_emp_org a, ");
  		de.addSql("        odssu.orginfor b ");
  		de.addSql("  where a.empno = :empno ");
  		de.addSql("    and a.orgno = b.orgno ");
  		de.addSql("    and exists(select 1 ");
  		de.addSql("                 from odssu.ir_emp_org_all_role c ");
  		de.addSql("                where c.empno = :userid ");
  		de.addSql("                  and c.orgno = a.orgno ");
  		de.addSql("                  and (c.roleno = '_ODS_WORK_DISPATCHER' or c.roleno = '_ODS_WORK_DISPATCH_')) ");
		this.de.setString("empno", empno);
		this.de.setString("userid", userid);
		DataStore orgVds = this.de.query();
		return orgVds;
	}
	/**
	 * 获取工单表（emp4role_detal中的全部数据，即工单表的最终数据信息
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	//获取操作员下的全部角色信息
	public DataObject fwRyywgwtzUTC(DataObject para) throws AppException, BusinessException{
		// 流程开始获取piid
		String piid = para.getString("piid");
		String empno = (String)ProcessUtil.getTEEVarByPiid(piid, "empno");
		DataStore empds = getEmpDetal(empno);
		DE  de = DE.getInstance();
		//工单表中数据
		de.clearSql();
		de.addSql("select  DISTINCT  a.empno ,a.orgno,a.roleno,c.rolename,d.orgname,a.opflag   "); 
		de.addSql("from     odssuws.emp4role_detal  a  ,    "); 
		de.addSql("         odssu.roleinfor   c  ,     "); 
		de.addSql("         odssu.orginfor    d       "); 
		de.addSql("where    a.empno = :empno   "); 
		de.addSql("         and   a.orgno  =  d.orgno      "); 
		de.addSql("         and   a.roleno  =  c.roleno "); 
		de.addSql("         and   a.piid   = :piid    ");
		de.addSql("         and  c.sleepflag = '0' ");
		de.addSql("  and c.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("empno",empno);
		de.setString("piid", piid);
		DataStore roledetalvdo = de.query();

		DataObject result = DataObject.getInstance();
		result.put("roledetalvdo", roledetalvdo);
		result.put("empds", empds);
		return result;
	}
	/**
	 * 获取人员的显示信息
	 * @param empno
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	//获取人员信息
	public DataStore getEmpDetal(String empno) throws AppException, BusinessException{
		de.clearSql();
		de.addSql("select a.empno,a.empname,a.loginname ");  
		de.addSql("  from odssu.empinfor a   "); 
		de.addSql(" where a.empno = :empno "); 
		de.setString("empno", empno);
		DataStore empds = de.query();
		return empds;
	}
	/**
	 * 选择机构之后，显示角色信息
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	//更新工单表
	public DataObject afterChooseOrgAddRole(DataObject para) throws AppException, BusinessException{
		// 流程开始获取piid
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String orgname = para.getString("orgname");
		String empno = para.getString("empno");
		DataStore typeds =  getOrgType(orgno);
		String typename = typeds.getString(0, "typename");
		String orgtypeno = typeds.getString(0, "typeno");
		//获取grid上显示的表单信息
		DataStore empds = getEmpDetal(empno);
		DataObject result = DataObject.getInstance();
		empds.put(0, "orgno", orgno);
		empds.put(0, "orgname", orgname);
		empds.put(0, "typename", typename);
		result.put("empds", empds);
		//获取左侧已选择的角色信息
		//工单表中新增的数据
		DE  de = DE.getInstance();
		de.clearSql();
		de.addSql(" select c.roleno, c.rolename "); 
		de.addSql("   from odssu.roleinfor c "); 
		de.addSql("  where c.roleno in ( select a.roleno  "); 
		de.addSql("           from odssuws.emp4role_detal a ");
		de.addSql("          where a.empno = :empno "); 
		de.addSql("            and a.opflag = '(+)' "); 
		de.addSql("            and a.orgno = :orgno ");
		de.addSql("            and a.piid = :piid )");
		de.addSql("    and c.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("empno",empno);
		de.setString("orgno", orgno);
		de.setString("piid", piid);
		DataStore rolechoosedds = de.query();
		//账表中除了删除之后的数据
		de.clearSql();
		de.addSql(" select c.roleno, c.rolename "); 
		de.addSql("   from odssu.roleinfor c "); 
		de.addSql("  where c.roleno in (select a.roleno "); 
		de.addSql("          from odssu.ir_emp_org_all_role a ");
		de.addSql("         where a.empno = :empno "); 
		de.addSql("           and a.orgno = :orgno ");
		de.addSql("           and a.isFormal = '1' ) ");
		de.addSql("    and c.roleno not in (select b.roleno "); 
		de.addSql("          from odssuws.emp4role_detal b "); 
		de.addSql("         where b.empno = :empno "); 
		de.addSql("           and b.orgno = :orgno "); 
		de.addSql("           and b.opflag = '(-)' "); 
		de.addSql("           and b.piid = :piid ) ");
		de.addSql("    and c.roleno not in (select n.roleno from odssu.njjs_filter n ) ");
		de.setString("empno",empno);
		de.setString("orgno", orgno);
		de.setString("piid", piid);
		DataStore vds = de.query();
		
		//全部的已选择的数据
		rolechoosedds.combineDatastore(vds);
		rolechoosedds.sort("rolename");
		result.put("rolechoosedds", rolechoosedds);
		
		//查询所有的角色信息，右侧显示全部的数据信息
		DataStore roleds = getCanChooseRoles(orgtypeno,orgno);
		getSelectRowRole(roleds,rolechoosedds);
		roleds.multiSort("_row_selected:desc,rolename:asc");
		result.put("roleds", roleds);
		return result;
	}
	public DataObject getChoosedRole(DataObject para) throws AppException, BusinessException{
		// 流程开始获取piid
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String orgname = para.getString("orgname");
		String empno = para.getString("empno");
		DataStore typeds =  getOrgType(orgno);
		String typename = typeds.getString(0, "typename");
		String orgtypeno = typeds.getString(0, "typeno");
		//获取grid上显示的表单信息
		DataStore empds = getEmpDetal(empno);
		DataObject result = DataObject.getInstance();
		empds.put(0, "orgno", orgno);
		empds.put(0, "orgname", orgname);
		empds.put(0, "typename", typename);
		result.put("empds", empds);
		//获取左侧已选择的角色信息
		//工单表中新增的数据
		DE  de = DE.getInstance();
		de.clearSql();
		de.addSql("select  DISTINCT  a.roleno,c.rolename  ,'true'   row_selected                "); 
		de.addSql("from     odssuws.emp4role_detal  a  ,                              "); 
		de.addSql("         odssu.roleinfor   c                                       "); 
		de.addSql("where    a.empno = :empno  										  "); 
		de.addSql("         and   a.opflag = '(+)'  								  "); 
		de.addSql("         and   a.roleno  =  c.roleno 							  "); 
		de.addSql("         and   a.orgno   = :orgno                                  ");
		de.addSql("         and   a.piid   = :piid                                    ");
		de.addSql("         and  c.sleepflag = '0' ");
		de.addSql("    and a.roleno in (select d.roleno from odssu.role_orgtype d where d.orgtypeno = :orgtypeno ) ");
		de.addSql("order by  a.opflag   desc                                          ");
		de.setString("empno",empno);
		de.setString("orgno", orgno);
		de.setString("piid", piid);
		de.setString("orgtypeno", orgtypeno);
		DataStore rolechoosedds = de.query();
		//账表中除了删除之后的数据
		de.clearSql();
		de.addSql("select  DISTINCT  a.roleno,c.rolename ,'true'   row_selected  ,a.isFormal       "); 
		de.addSql("from     odssu.ir_emp_org_all_role  a  ,                   "); 
		de.addSql("         odssu.roleinfor   c                               "); 
		de.addSql("where    a.empno = :empno                                  "); 
		de.addSql("         and  not EXISTS(                                  "); 
		de.addSql("                select  1  from odssuws.emp4role_detal b   "); 
		de.addSql("                where  a.empno = b.empno                   "); 
		de.addSql("                       and   a.roleno  = b.roleno          "); 
		de.addSql("                       and   a.orgno = b.orgno             "); 
		de.addSql("                       and   b.opflag  =  '(-)'            ");
		de.addSql("                       and   b.piid = :piid                ");
		de.addSql("         )                                                 "); 
		de.addSql("         and   a.orgno  = :orgno                           ");
		de.addSql("         and   a.roleno  =  c.roleno 					  "); 
		de.addSql("         and  c.sleepflag = '0' ");
		de.addSql("        and a.isFormal = '1'");
		de.addSql("    and a.roleno in (select d.roleno from odssu.role_orgtype d where d.orgtypeno = :orgtypeno ) ");
		de.setString("empno",empno);
		de.setString("orgno", orgno);
		de.setString("piid", piid);
		de.setString("orgtypeno", orgtypeno);
		DataStore vds = de.query();
		
		//全部的已选择的数据
		rolechoosedds.combineDatastore(vds);
		result.put("rolechoosedds", rolechoosedds);
		return result;
	}
	/**
	 * 获取机构 的机构类型
	 * @param orgno
	 * @return
	 * @throws AppException
	 */
	public DataStore getOrgType(String orgno) throws AppException{
		DE de =DE.getInstance();
		de.clearSql();
		de.addSql("select b.typename,b.typeno  from   odssu.orginfor  a , odssu.org_type b   where  a.orgno = :orgno   and    a.orgtype = b.typeno ");
		de.setString("orgno", orgno);
		DataStore ds = de.query();
		return ds;
	}
	
	public DataStore getCanChooseRoles(String typeno,String orgno) throws AppException{
		de.clearSql();
		de.addSql("select a.roleno, a.rolename,c.typename                              "); 
		de.addSql("  from odssu.roleinfor a , odssu.role_orgtype b , odssu.org_type  c ");
		de.addSql(" where a.roleno in ( select d.roleno                                ");
		de.addSql("                       from odssu.role_orgtype d                    ");
		de.addSql("                      where d.orgtypeno = :typeno                   ");
		de.addSql("   )                                                                "); 
		de.addSql("   and a.roleno = b.roleno                                          ");
		de.addSql("   and b.orgtypeno = c.typeno                                       ");
		de.addSql("   and a.sleepflag = '0'                                            ");
		de.addSql("   and a.roleno not in (select n.roleno from odssu.njjs_filter n )  ");
		de.addSql("   and a.deforgno in ( select e.belongorgno                         ");
		de.addSql(" 						from odssu.ir_org_closure e                ");
		de.addSql(" 		               where e.orgno = :orgno                      ");
		de.addSql("   )                                                                "); 
		de.setString("typeno", typeno);
		de.setString("orgno", orgno);
		DataStore ds = de.query();
		
		ds = dealOrgType(ds);
		
		return ds;
	}
	
	
	/**
	 * 从账表中查询全部的角色信息
	 * @param rolename
	 * @return
	 * @throws AppException
	 */
	//查询所有的角色信息
	public DataStore getAllRoleOrgType(String rolename) throws AppException{
		de.clearSql();
		de.addSql("select a.roleno,b.orgtypeno,a.rolename,c.typename,d.orgname "); 
		de.addSql("  from odssu.roleinfor a  ,   odssu.role_orgtype b  ,  odssu.org_type  c , odssu.orginfor  d");
		de.addSql(" where a.roleno = b.roleno  ");
		de.addSql("   and a.deforgno = d.orgno  ");
		de.addSql("   and b.orgtypeno = c.typeno  ");
		de.addSql("   and a.sleepflag = '0' ");
		de.addSql("  and a.roleno not in (select n.roleno from odssu.njjs_filter n )");
		if(StringUtils.isNotBlank(rolename)) {
			de.addSql(" and (a.roleno like :name or a.rolename like :name or c.typename like :name)");
			de.setString("name", "%"+rolename+"%");
		}
		DataStore ds = de.query();
		
		ds = dealOrgType(ds);
		
		return ds;
	}
	/**
	 * 根据角色名称进行查找全部的角色信息
	 * @param para
	 * @return
	 * @throws AppException
	 */
	//全部角色
	public DataObject getAllRoleOrgTypeByRoleName(DataObject para) throws AppException{
		String rolename = (String)para.getString("rolename");
		DataStore roleds = getAllRoleOrgType(rolename);
		DataObject result = DataObject.getInstance();
		result.put("roleds", roleds);
		return result;
	}
	
	//选择机构后调整角色的查询按钮
	
	public DataObject queryChoosedRole(DataObject para) throws AppException, BusinessException{
		// 流程开始获取piid
		String orgno = (String)para.getString("orgno");
		String querylabel = (String)para.getString("querylabel","");
		String piid = para.getString("piid");
		String orgname = para.getString("orgname");
		String empno = para.getString("empno");
		DataStore typeds =  getOrgType(orgno);
		String typename = typeds.getString(0, "typename");
		String orgtypeno = typeds.getString(0, "typeno");
		//获取grid上显示的表单信息
		DataStore empds = getEmpDetal(empno);
		DataObject result = DataObject.getInstance();
		empds.put(0, "orgno", orgno);
		empds.put(0, "orgname", orgname);
		empds.put(0, "typename", typename);
		result.put("empds", empds);
		//获取左侧已选择的角色信息
		//工单表中新增的数据
		DE  de = DE.getInstance();
		de.clearSql();
		de.addSql(" select c.roleno, c.rolename "); 
		de.addSql("   from odssu.roleinfor c "); 
		de.addSql("  where c.roleno in ( select a.roleno  "); 
		de.addSql("           from odssuws.emp4role_detal a ");
		de.addSql("          where a.empno = :empno "); 
		de.addSql("            and a.opflag = '(+)' "); 
		de.addSql("            and a.orgno = :orgno ");
		de.addSql("            and a.piid = :piid )");
		de.addSql("    and c.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("empno",empno);
		de.setString("orgno", orgno);
		de.setString("piid", piid);
		DataStore rolechoosedds = de.query();
		//账表中除了删除之后的数据
		de.clearSql();
		de.addSql(" select c.roleno, c.rolename "); 
		de.addSql("   from odssu.roleinfor c "); 
		de.addSql("  where c.roleno in (select a.roleno "); 
		de.addSql("          from odssu.ir_emp_org_all_role a ");
		de.addSql("         where a.empno = :empno "); 
		de.addSql("           and a.orgno = :orgno ");
		de.addSql("           and a.isFormal = '1' ) ");
		de.addSql("    and c.roleno not in (select b.roleno "); 
		de.addSql("          from odssuws.emp4role_detal b "); 
		de.addSql("         where b.empno = :empno "); 
		de.addSql("           and b.orgno = :orgno "); 
		de.addSql("           and b.opflag = '(-)' "); 
		de.addSql("           and b.piid = :piid ) ");
		de.addSql("    and c.roleno not in (select n.roleno from odssu.njjs_filter n ) ");
		de.setString("empno",empno);
		de.setString("orgno", orgno);
		de.setString("piid", piid);
		DataStore vds = de.query();
		
		//全部的已选择的数据
		rolechoosedds.combineDatastore(vds);
		rolechoosedds.sort("rolename");
		result.put("rolechoosedds", rolechoosedds);
		
		//查询所有的角色信息，右侧显示全部的数据信息
		DataStore roleds = queryCanChooseRole(orgtypeno,orgno,querylabel);
		getSelectRowRole(roleds,rolechoosedds);
		roleds.multiSort("_row_selected:desc,rolename:asc");
		result.put("roleds", roleds);
		return result;
		
	}
	
	
	public DataStore queryCanChooseRole(String orgtypeno,String orgno ,String querylabel) throws AppException{
		de.clearSql();
		de.addSql("select a.roleno, a.rolename,c.typename                              "); 
		de.addSql("  from odssu.roleinfor a , odssu.role_orgtype b , odssu.org_type  c ");
		de.addSql(" where a.roleno in ( select d.roleno                                ");
		de.addSql("                       from odssu.role_orgtype d                    ");
		de.addSql("                      where d.orgtypeno = :typeno                   ");
		de.addSql("   )                                                                "); 
		de.addSql("   and a.roleno = b.roleno                                          ");
		de.addSql("   and b.orgtypeno = c.typeno                                       ");
		de.addSql("   and a.sleepflag = '0'                                            ");
		de.addSql("   and a.roleno not in (select n.roleno from odssu.njjs_filter n )  ");
		de.addSql("   and a.deforgno in ( select e.belongorgno                         ");
		de.addSql(" 						from odssu.ir_org_closure e                ");
		de.addSql(" 		               where e.orgno = :orgno                      ");
		de.addSql("   )                                                                "); 
		if(StringUtils.isNotBlank(querylabel)) {
			de.addSql(" and (a.roleno like :querylabel or a.rolename like :querylabel or c.typename like :querylabel)");
			de.setString("querylabel","%"+querylabel+"%");
		}
		de.setString("typeno", orgtypeno);
		de.setString("orgno", orgno);
		DataStore ds = de.query();
		
		ds = dealOrgType(ds);
		
		return ds;
	}
	/**
	 * 对数据进行处理，将已选的数据进行勾选
	 * @param roledsbeforedel
	 * @param rolechoosedds
	 * @return
	 * @throws AppException
	 */
	//将全部中已选的数据勾选
	public void getSelectRowRole(DataStore roleds,DataStore rolechoosedds) throws AppException{
		for(int i =0 ;i<roleds.size();i++){
			DataObject currentrole = roleds.get(i);
			String leftroleno = currentrole.getString("roleno");
			
			if(rolechoosedds.find("roleno == "+leftroleno) >=0 ) {
				currentrole.put("_row_selected", true);
			}else {
				currentrole.put("_row_selected", false);
			}
		}
	}
	/**
	 * 对角色使用的机构类型进行逗号分隔
	 * @param ds
	 * @return
	 * @throws AppException
	 */
	//对角色所适用的机构类型用逗号进行分隔
	public DataStore dealOrgType(DataStore ds) throws AppException{
		
		if(ds == null) {
			return DataStore.getInstance();
		}
		if(ds != null && ds.rowCount() ==0) {
			return ds;
		}
		
		HashMap<String,Integer> rolesExists = new HashMap<String,Integer>(ds.rowCount());
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
	 * 删除角色信息
	 * @param para
	 * @throws AppException
	 * @throws BusinessException
	 */
	//删除角色信息
	public void delEmpOrgRole(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String roleno = para.getString("roleno");
		String empno = para.getString("empno");
		DE de = DE.getInstance();
		DataStore ds = getopflag(piid,orgno,roleno);
		//如果工单表中有数据，证明是新增的，直接删除；如果工单表中无数据，先去账表中判断是否存在，账表中存在，新增一条为-的数据
		if(ds.isEmpty()){
			if(!isRoleExists(piid, empno, orgno, roleno)) {
				return;
			}
			de.clearSql();
			de.addSql("insert  into  odssuws.emp4role_detal(piid,orgno,roleno,opflag,empno) ");
			de.addSql("                               values(:piid, :orgno , :roleno , '(-)',:empno)");
			de.setString("piid", piid);
			de.setString("orgno", orgno);
			de.setString("roleno", roleno);
			de.setString("empno", empno);
			de.update();
		}else{
			String opflag = ds.getString(0, "opflag");
			if("(-)".equals(opflag)) {
				return ;
			}
			de.clearSql();
			de.addSql("delete from odssuws.emp4role_detal ");
			de.addSql("where piid = :piid   and orgno = :orgno  and roleno = :roleno  and empno = :empno");
			de.setString("piid", piid);
			de.setString("orgno", orgno);
			de.setString("roleno", roleno);
			de.setString("empno", empno);
			de.update();
		}
	}
	//删除所有角色
	
	public void delAllEmpOrgWithRole(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String empno = para.getString("empno");
		de.clearSql();
		de.addSql(" delete from odssuws.emp4role_detal a  ");
		de.addSql("  where  a.piid = :piid   ");
		de.addSql("    and  a.orgno = :orgno ");
		de.addSql("    and  a.empno = :empno ");
		de.addSql("    and  a.opflag = '(+)' ");
		de.setString("piid", piid);
		de.setString("orgno", orgno);
		de.setString("empno", empno);
		de.update();

		de.clearSql();
		de.addSql(" insert  into odssuws.emp4role_detal(piid,roleno,orgno,empno,opflag) ");
		de.addSql(" select  distinct :piid , a.roleno ,a.orgno,a.empno,'(-)' ");
		de.addSql("   from  odssu.ir_emp_org_all_role a  ");
		de.addSql("  where  a.orgno = :orgno   ");
		de.addSql("    and  a.empno = :empno   ");
		de.addSql("    and  a.roleno not in (select roleno from odssuws.emp4role_detal b    ");
		de.addSql("							              where b.opflag = '(-)'       ");
		de.addSql("							  				and b.piid = :piid         ");
		de.addSql("											and b.orgno = :orgno       ");
		de.addSql("											and b.empno = :empno)      ");
		de.setString("piid", piid);
		de.setString("orgno", orgno);
		de.setString("empno", empno);
		de.update();

	}
	
	//选择角色后选择机构的取消全选
	public void delAllEmpRoleWithOrg(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		String roleno = para.getString("roleno");
		String empno = para.getString("empno");
	
		DataStore orgds = para.getDataStore("grid1891");
		
		for(int i=0;i<orgds.rowCount();++i) {
			String orgno = orgds.getString(i, "orgno");
			DataObject temp = DataObject.getInstance();
			temp.put("orgno",orgno );
			temp.put("piid", piid);
			temp.put("roleno",roleno );
			temp.put("empno", empno);
			delEmpOrgRole(temp);
		}
		
	}
	/**
	 * 添加角色信息
	 * @param para
	 * @throws AppException
	 * @throws BusinessException
	 */
	//添加角色信息
	public void addEmpOrgRole(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String roleno = para.getString("roleno");
		String empno = para.getString("empno");
		DE de = DE.getInstance();
		DataStore ds = getopflag(piid,orgno,roleno);
		//先判断角色是否在工单表中存在，如果工单表中不存在就去判断账表中是否存在。
		if(ds.isEmpty()){
			if(isRoleExists(piid, empno, orgno, roleno)) {
				return;
			}
			de.clearSql();
			de.addSql("insert  into  odssuws.emp4role_detal(piid,orgno,roleno,opflag,empno) ");
			de.addSql("                               values(:piid, :orgno , :roleno , '(+)',:empno)");
			de.setString("piid", piid);
			de.setString("orgno", orgno);
			de.setString("roleno", roleno);
			de.setString("empno", empno);
			de.update();
		}else{
			String opflag = ds.getString(0, "opflag");
			if("(+)".equals(opflag)) {
				return ;
			}
			de.clearSql();
			de.addSql("delete  from     odssuws.emp4role_detal   ");
			de.addSql("where piid = :piid   and orgno = :orgno  and roleno = :roleno  and empno = :empno");
			de.setString("piid", piid);
			de.setString("orgno", orgno);
			de.setString("roleno", roleno);
			de.setString("empno", empno);
			de.update();
		}
		
	}
	public boolean isRoleExists(String piid,String empno,String orgno,String roleno) throws AppException {
		
		de.clearSql();
		de.addSql("select   1       "); 
		de.addSql("from     odssu.ir_emp_org_all_role  a  ,                   "); 
		de.addSql("         odssu.roleinfor   c                               "); 
		de.addSql("where    a.empno = :empno                                  "); 
		de.addSql("         and  not EXISTS(                                  "); 
		de.addSql("                select  1  from odssuws.emp4role_detal b   "); 
		de.addSql("                where  a.empno = b.empno                   "); 
		de.addSql("                       and   a.roleno  = b.roleno          "); 
		de.addSql("                       and   a.orgno = b.orgno             "); 
		de.addSql("                       and   b.opflag  =  '(-)'            ");
		de.addSql("                       and   b.piid = :piid                ");
		de.addSql("         )                                                 "); 
		de.addSql("         and   a.orgno  = :orgno                           ");
		de.addSql("         and   a.roleno  =  c.roleno 					  "); 
		de.addSql("         and   c.sleepflag = '0'                           ");
		de.addSql("         and   c.roleno = :roleno                          ");
		de.addSql("         and   a.isFormal = '1'                            ");
		de.setString("empno",empno);
		de.setString("orgno", orgno);
		de.setString("roleno", roleno);
		de.setString("piid", piid);
		DataStore vds = de.query();
		
		if(vds == null || vds.rowCount() == 0) {
			return false;
		}
		return true;
	}
	
	//全选所有的可选角色
	
	public void addAllEmpOrgWithRole(DataObject para) throws AppException, BusinessException{

		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String empno = para.getString("empno");
		String gridname = para.getString("gridname");
		DataStore roleds = para.getDataStore(gridname);
		DataObject middlepara = DataObject.getInstance();

		for(int i=0;i<roleds.rowCount();i++) {
			
			String roleno = roleds.getString(i, "roleno");
			
			middlepara.put("piid", piid);
			middlepara.put("orgno", orgno);
			middlepara.put("empno", empno);
			middlepara.put("roleno", roleno);
			addEmpOrgRole(middlepara);
			
		}
	}
	
	//选择角色后选择机构的全选
	
	public void addAllEmpRoleWithOrg(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		String roleno = para.getString("roleno");
		String empno = para.getString("empno");
		String gridname = para.getString("gridname");
		DataStore roleds = para.getDataStore(gridname);
		DataObject middlepara = DataObject.getInstance();

		for(int i=0;i<roleds.rowCount();i++) {
			
			String orgno = roleds.getString(i, "orgno");
			middlepara.put("piid", piid);
			middlepara.put("orgno", orgno);
			middlepara.put("empno", empno);
			middlepara.put("roleno", roleno);
			addEmpOrgRole(middlepara);
			
		}	
	}
	/**
	 * 查看工单表中的标记，根据标记判断直接添加，还是修改标记
	 * @param piid
	 * @param orgno
	 * @param roleno
	 * @return
	 * @throws AppException
	 */
	//判断是删除后再添加，还是直接添加
	public DataStore getopflag(String piid,String orgno,String roleno) throws AppException{
		DE de =DE.getInstance();
		de.clearSql();
		de.addSql("select opflag from odssuws.emp4role_detal where piid = :piid  and orgno = :orgno   ");
		de.addSql("   and  roleno = :roleno");
		de.setString("roleno",roleno);
		de.setString("piid",piid);
		de.setString("orgno",orgno);
		DataStore ds = de.query();
		return ds;
		
	}
	/**
	 * 选中角色之后，在选择机构页面上方显示的角色信息
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	//AfterChooseRole
	public DataObject AfterChooseRole(DataObject para) throws AppException, BusinessException{
		// 流程开始获取piid
		String piid = para.getString("piid");
		String roleno = para.getString("roleno");
		String rolename = para.getString("rolename");
		String typename = para.getString("typename");
		String empno = (String)ProcessUtil.getTEEVarByPiid(piid, "empno");
		//获取grid上显示的表单信息
		DataStore roleds = getEmpDetal(empno);
		DataObject result = DataObject.getInstance();
		roleds.put(0, "roleno", roleno);
		roleds.put(0, "rolename", rolename);
		roleds.put(0, "typename", typename);
		result.put("roleds", roleds);
		return result;
		
	}
	/**
	 * 获取已过滤的角色信息
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject haveUncheckRole(DataObject para) throws AppException, BusinessException{
		// 流程开始获取piid
		String orgno = para.getString("orgno");
		DataStore typeds =  getOrgType(orgno);
		String orgtypeno = typeds.getString(0, "typeno");
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql("  select a.roleno, b.orgtypeno, a.rolename, c.typename, e.orgname       ");
		de.addSql("    from odssu.roleinfor    a,                                          ");
		de.addSql("         odssu.role_orgtype b,                                          ");
		de.addSql("         odssu.org_type     c,                                          ");
		de.addSql("         odssu.orginfor     e                                           ");
		de.addSql("   where (a.deforgno not in                                             ");
		de.addSql("         (select f.belongorgno                                          ");
		de.addSql("             from odssu.ir_org_closure f                                ");
		de.addSql("            where f.orgno = :orgno ) or                                 ");
		de.addSql("         a.roleno not in                                                ");
		de.addSql("         (select d.roleno                                               ");
		de.addSql("             from odssu.role_orgtype d                                  ");
		de.addSql("            where d.orgtypeno = :typeno     ))                          ");
		de.addSql("     and a.roleno = b.roleno                                            ");
		de.addSql("     and b.orgtypeno = c.typeno                                         ");
		de.addSql("     and a.sleepflag = '0'                                              ");
		de.addSql("     and e.orgno = a.deforgno                                           ");
		de.addSql("     and a.roleno not in (select n.roleno from odssu.njjs_filter n)     ");
		de.setString("orgno", orgno);
		de.setString("typeno", orgtypeno);
		DataStore roleds = de.query();
		
		roleds = dealOrgType(roleds);
		
		DataObject result = DataObject.getInstance();
		result.put("roleds", roleds);
		return result;
	}
	/**
	 * 在已过滤的角色中搜索某个角色
	 * @Author zy
	 * @description
	 * @date 2020年9月30日
	 * @return
	 */
	public DataObject queryUncheckRole(DataObject para) throws AppException, BusinessException{
		// 流程开始获取piid
		String orgno = para.getString("orgno");
		DataStore typeds =  getOrgType(orgno);
		String orgtypeno = typeds.getString(0, "typeno");
		String queryrole = para.getString("queryrole");
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql("  select a.roleno, b.orgtypeno, a.rolename, c.typename, e.orgname       ");
		de.addSql("    from odssu.roleinfor    a,                                          ");
		de.addSql("         odssu.role_orgtype b,                                          ");
		de.addSql("         odssu.org_type     c,                                          ");
		de.addSql("         odssu.orginfor     e                                           ");
		de.addSql("   where (a.deforgno not in                                             ");
		de.addSql("         (select f.belongorgno                                          ");
		de.addSql("             from odssu.ir_org_closure f                                ");
		de.addSql("            where f.orgno = :orgno ) or                                 ");
		de.addSql("         a.roleno not in                                                ");
		de.addSql("         (select d.roleno                                               ");
		de.addSql("             from odssu.role_orgtype d                                  ");
		de.addSql("            where d.orgtypeno = :typeno     ))                          ");
		de.addSql("     and a.roleno = b.roleno                                            ");
		de.addSql("     and b.orgtypeno = c.typeno                                         ");
		de.addSql("     and a.sleepflag = '0'                                              ");
		de.addSql("     and e.orgno = a.deforgno                                           ");
		de.addSql("     and a.roleno not in (select n.roleno from odssu.njjs_filter n)     ");

		if(StringUtils.isNotBlank(queryrole)) {
			de.addSql("and (upper(a.roleno) like :queryrole or upper(a.rolename) like :queryrole)  ");
			de.setString("queryrole", "%"+queryrole.toUpperCase()+"%");
		}
		de.setString("orgno", orgno);
		de.setString("typeno", orgtypeno);
		DataStore roleds = de.query();
		
		roleds = dealOrgType(roleds);
		
		DataObject result = DataObject.getInstance();
		result.put("roleds", roleds);
		return result;
	}
	
}
