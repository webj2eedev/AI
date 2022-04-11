package com.dw.vap.role;

import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;
import com.dareway.framework.dbengine.DE;

public class RoleVAPBPO extends BPO{

	/**
	 * 转向角色基本信息界面
	 * 
	 * @author liuy
	 * @date 创建时间2014-05-06
	 * @since V1.0
	 */
	public DataObject fwPageRoleInfo(final DataObject para) throws Exception {
		DE de = DE.getInstance();
		DataObject vdo = DataObject.getInstance();
		DataStore roleds;
		String roleno, roletype, typename = "", deforgno, deforgname = "", sleepflag = "";

		roleno = para.getString("roleno");

		de.clearSql();
  		de.addSql("select roleno,rolename,displayname,isshowinorg,roletype,deforgno,sleepflag,rolenature ");
  		de.addSql("  from odssu.roleinfor  ");
  		de.addSql(" where roleno = :roleno		 ");
		// sqlBF.append("   and sleepflag = '0' ");

		de.setString("roleno", roleno);
		roleds = de.query();
		if (roleds.rowCount() == 0) {
			throw new BusinessException("没有找到此角色的信息，请检查！");
		} else if (roleds.rowCount() == 1) {
			roletype = roleds.getString(0, "roletype");
			deforgno = roleds.getString(0, "deforgno");
			sleepflag = roleds.getString(0, "sleepflag");

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
		vdo.put("sleepflag", sleepflag);

		return vdo;
	}

	/**
	 * 角色上级角色
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-17
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageRoleSupRoleJsp(final DataObject para) throws Exception {
		String roleno = para.getString("roleno");
		DE de = DE.getInstance();
		de.clearSql();

  		de.addSql("select * 			 ");
  		de.addSql("  from odssu.roleinfor  ");
  		de.addSql(" where roleno = :roleno		 ");
		// sqlBF.append("   and sleepflag = '0' ");

		de.setString("roleno", roleno);
		DataStore roleds = de.query();
		if (roleds.rowCount() == 0) {
			this.bizException("没有找到此角色的信息，请检查！");
		}
		String sleepflag = roleds.getString(0, "sleepflag");

		de.clearSql();
  		de.addSql(" select r.roleno,r.rolename,r.displayname,r.rolenature,t.typeno,t.typename ");
  		de.addSql(" from odssu.roleinfor r, ");
  		de.addSql("      odssu.ir_role i, ");
  		de.addSql("      odssu.role_type t ");
  		de.addSql(" where i.subroleno = :roleno and i.suproleno = r.roleno  ");
  		de.addSql("       and t.typeno = r.roletype and r.sleepflag= '0' ");
  		de.addSql(" order by r.roleno ");
		de.setString("roleno", roleno);
		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("roleds", vds);
		vdo.put("sleepflag", sleepflag);
		return vdo;
	}

	/*
	 * 角色下级角色
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-17
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageRoleSubRoleJsp(final DataObject para) throws Exception {
		String roleno = para.getString("roleno");
		DE de = DE.getInstance();

		de.clearSql();
  		de.addSql("select * 			 ");
  		de.addSql("  from odssu.roleinfor  ");
  		de.addSql(" where roleno = :roleno		 ");
		// sqlBF.append("   and sleepflag = '0' ");

		de.setString("roleno", roleno);
		DataStore roleds = de.query();
		if (roleds.rowCount() == 0) {
			throw new BusinessException("没有找到此角色的信息，请检查！");
		}
		String sleepflag = roleds.getString(0, "sleepflag");

		de.clearSql();
  		de.addSql(" select r.roleno,r.rolename,r.displayname,r.rolenature,t.typeno,t.typename ");
  		de.addSql(" from odssu.roleinfor r, ");
  		de.addSql("      odssu.ir_role i, ");
  		de.addSql("      odssu.role_type t ");
  		de.addSql(" where i.suproleno = :roleno and i.subroleno = r.roleno ");
  		de.addSql("       and t.typeno = r.roletype and r.sleepflag= '0' ");
  		de.addSql(" order by r.roleno ");
		de.setString("roleno", roleno);
		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("roleds", vds);
		vdo.put("sleepflag", sleepflag);
		return vdo;
	}

	/**
	 * 通过角色编号查找业务受理单位ID
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-28
	 * @param para
	 * @return
	 */
	public DataObject getPbizByRoleno(DataObject para) throws Exception {
		String roleno = para.getString("roleno");

		if (roleno == null || roleno.trim().isEmpty()) {
			this.bizException("角色编号为空！");
		}

		String pBiz = OdssuUtil.getYwbljgByRoleno(roleno);

		DataObject vdo = DataObject.getInstance();

		vdo.put("pBiz", pBiz);

		return vdo;
	}

	/**
	 * debug -- 角色基本信息
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-1
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject getDebugRoleInfor(DataObject para) throws Exception {
		String roleno = para.getString("roleno");

		if (roleno == null || roleno.trim().isEmpty()) {
			this.bizException("角色编号为空！");
		}
		DE de = DE.getInstance();
		de.clearSql();
    	de.addSql(" select  *  ");
  		de.addSql("  from odssu.roleinfor ");
  		de.addSql(" where  roleno = :roleno ");
		de.setString("roleno", roleno);

		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();

		vdo.put("roleds", vds);

		return vdo;
	}

	/**
	 * debug -- 角色的上级角色
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-1
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageDebugRoleSupRoleJsp(final DataObject para) throws Exception {
		String roleno = para.getString("roleno");
		DE de = DE.getInstance();
		
		de.clearSql();
  		de.addSql("select * 			 ");
  		de.addSql("  from odssu.roleinfor  ");
  		de.addSql(" where roleno = :roleno		 ");
		// sqlBF.append("   and sleepflag = '0' ");

		de.setString("roleno", roleno);
		DataStore roleds = de.query();
		if (roleds.rowCount() == 0) {
			this.bizException("没有找到此角色的信息，请检查！");
		}

		de.clearSql();
  		de.addSql(" select i.suproleno ir_role_suproleno  ,r.rolename role_rolename ");
  		de.addSql(" from odssu.ir_role i left outer join odssu.roleinfor r ");
  		de.addSql("      on i.suproleno = r.roleno  ");
  		de.addSql(" where i.subroleno = :roleno ");
		de.setString("roleno", roleno);

		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("roleds", vds);
		return vdo;
	}

	/**
	 * debug--角色下级角色
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-1
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageDebugRoleSubRoleJsp(final DataObject para) throws Exception {
		String roleno = para.getString("roleno");

		if (roleno == null || roleno.trim().isEmpty()) {
			this.bizException("角色编号为空！");
		}
		DE de = DE.getInstance();
		
		de.clearSql();
  		de.addSql(" select i.subroleno ir_role_subroleno , r.rolename roleinfor_rolename ");
  		de.addSql(" from odssu.ir_role i left outer join odssu.roleinfor r ");
  		de.addSql("      on i.subroleno = r.roleno ");
  		de.addSql(" where i.suproleno = :roleno  ");
		de.setString("roleno", roleno);
		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("roleds", vds);
		return vdo;
	}

	/**
	 * 该角色被哪些人员作为对哪些机构的干系角色
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-12
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject fwPageDebugAllEmpOuterOrgOwnRoleInfor(
			final DataObject para) throws Exception {
		String roleno = para.getString("roleno");

		if (roleno == null || roleno.trim().isEmpty()) {
			this.bizException("角色编号为空！");
		}
		DE de = DE.getInstance();

		de.clearSql();
  		de.addSql(" select i.empno ir_emp_org_all_role_empno  , r.empname empinfor_empname , i.orgno ir_emp_org_all_role_orgno , o.orgname orginfor_orgname , i.rolenature ");
  		de.addSql(" from odssu.ir_emp_org_all_role i left outer join odssu.empinfor r ");
  		de.addSql("      on i.empno = r.empno, ");
  		de.addSql(" 	  odssu.ir_emp_org_all_role i1 left outer join odssu.orginfor o ");
  		de.addSql("      on i1.orgno = o.orgno ");
  		de.addSql(" where i.roleno = :roleno and i.rolenature = '5'   ");
		de.setString("roleno", roleno);
		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("orgempds", vds);
		return vdo;
	}

	public DataObject fwPageOrgtypeJsp(final DataObject para) throws AppException {
		String roleno = para.getString("roleno");

		if (roleno == null || roleno.trim().isEmpty()) {
			throw new AppException("传入的角色编号为空");
		}
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" select c.typeno,c.typename,c.comments");
  		de.addSql("   from odssu.roleinfor a ,");
  		de.addSql("        odssu.ir_org_role_type b,");
  		de.addSql("        odssu.org_type  c");
  		de.addSql(" where  a.roleno = :roleno");
  		de.addSql("   and  a.roletype = b.roletypeno");
  		de.addSql("   and b.orgtypeno = c.typeno");
  		de.addSql("   order by c.typeno ");
		de.setString("roleno", roleno);
		DataStore vds = de.query();

        DataObject vdo = DataObject.getInstance();
        vdo.put("jglbds", vds);
        
		return vdo;
	}
}
