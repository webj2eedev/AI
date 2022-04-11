package com.dw.vap.role;

import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dareway.framework.dbengine.DE;

public class RoleTypeVAPTreeBPO extends BPO {

	/**
	 * 描述:查询角色类型基本信息
	 * author:YeJun
	 * 2014-9-4 上午11:42:15
	 * @param para
	 * @return
	 * @throws Exception
	 * DataObject
	 */
	public final DataObject fwPageRoletype(final DataObject para) throws Exception {
		DE de = DE.getInstance();
		de.clearSql();
		DataStore roleTypeInfor;
		DataObject vdo = DataObject.getInstance();
		String typeno = para.getString("typeno").trim();
		if (typeno == null || "".equals(typeno)) {
			this.bizException("获取角色类型编号时为空！");
		}

		de.clearSql();
  		de.addSql("select * from odssu.role_type where typeno = :typeno ");
		de.setString("typeno", typeno);
		roleTypeInfor = de.query();
		if (roleTypeInfor.rowCount() == 0) {
			this.bizException("编号为" + typeno + "的角色类型信息不存在");
		}
		vdo.put("roletypeinfor", roleTypeInfor);
		return vdo;
	}

	/**
	 * 描述:适用的机构类型
	 * author:YeJun
	 * 2014-9-4 下午02:27:30
	 * @param para
	 * @return
	 * @throws Exception
	 * DataObject
	 */
	public final DataObject fwPageOrgTypeOfOrgType(final DataObject para) throws Exception {
		String typeno = para.getString("typeno").trim();
		if (typeno == null || "".equals(typeno)) {
			this.bizException("获取编号时为空！");
		}
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select '1' from odssu.role_type where typeno = :typeno ");
		de.setString("typeno", typeno);
		DataStore exists = de.query();
		if (exists.rowCount() == 0) {
			this.bizException("编号为" + typeno + "的角色类型信息不存在");
		}
		//查询适用的机构类型
		DataObject vdo = DataObject.getInstance();
		DataStore orgType;
		de.clearSql();
  		de.addSql("  select a.typeno,a.typename,a.typenature,a.comments ");
  		de.addSql("  from odssu.org_type a,                             ");
  		de.addSql("       odssu.ir_org_role_type b                      ");
  		de.addSql(" where a.typeno = b.orgtypeno                        ");
  		de.addSql("   and b.roletypeno = :typeno                              ");
  		de.addSql(" order by a.typeno ");
		de.setString("typeno", typeno);
		orgType = de.query();
		vdo.put("orgtype", orgType);
		return vdo;
	}

	/**
	 * 描述:本类型的角色
	 * author:YeJun
	 * 2014-9-4 下午02:48:39
	 * @param para
	 * @return
	 * @throws Exception
	 * DataObject
	 */
	public final DataObject fwPageRoleOfType(final DataObject para) throws Exception {
		String typeno = para.getString("typeno").trim();
		if (typeno == null || "".equals(typeno)) {
			this.bizException("获取编号时为空！");
		}
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select '1' from odssu.role_type where typeno = :typeno ");
		de.setString("typeno", typeno);
		DataStore exists = de.query();
		if (exists.rowCount() == 0) {
			this.bizException("编号为" + typeno + "的角色类型信息不存在");
		}
		//查询本类型的角色
		DataObject vdo = DataObject.getInstance();
		DataStore roleInfor;
		de.clearSql();
  		de.addSql("  select a.roleno,a.displayname,a.rolename ");
  		de.addSql("  from odssu.roleinfor a,                  ");
  		de.addSql("       odssu.role_type b                   ");
  		de.addSql(" where a.roletype = b.typeno               ");
  		de.addSql("   and b.typeno = :typeno                        ");
  		de.addSql(" order by a.roleno ");
		de.setString("typeno", typeno);
		roleInfor = de.query();
		vdo.put("roleinfor", roleInfor);
		return vdo;
	}
}
