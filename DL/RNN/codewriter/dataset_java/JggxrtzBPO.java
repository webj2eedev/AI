package com.dw.odssu.ws.org.jggxrtz;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

public class JggxrtzBPO extends BPO{
	/**
	 * 方法简介：必须 在orgno所属的人社局是单位管理员
	 * 			或是在ORGROOT是系统管理员的操作员
	 * 			才有权限调整orgno对应机构的干系人
	 * 郑海杰  2015-11-20
	 */
	public DataObject hasJggxrtzRight(DataObject para) throws AppException{
		String orgno = para.getString("orgno");
		if(orgno == null || "".equals(orgno)){
			throw new AppException("机构编号为空,无法调整干系人！");
		}
  		de.clearSql();
		boolean hasRight = false;
		String bljgid = "";
		//检查在ORGROOT是否为系统管理员
		if(OdssuContants.ORGROOT.equals(orgno) || OdssuUtil.isRsxt(orgno)){
  			de.addSql(" select 1 ");
  			de.addSql("   from odssu.ir_emp_org_all_role a ");
  			de.addSql("  where a.empno = :empno ");
  			de.addSql("    and a.orgno = :orgno ");
  			de.addSql("    and a.roleno = :roleno ");
			this.de.setString("empno", this.getUser().getUserid());
			this.de.setString("orgno", OdssuContants.ORGROOT);
			this.de.setString("roleno", OdssuContants.ROLE_ODS_SYSADMIN);
			DataStore irEmpOrgRoleVds = this.de.query();
			if(irEmpOrgRoleVds == null || irEmpOrgRoleVds.size() == 0){
				hasRight =  false;
				bljgid = "";
			}
			else{
				hasRight = true;
				bljgid = OdssuContants.ORGROOT;
			}
		}else{
			//在orgno 的上级单位中是单位管理员
			de.clearSql();
  			de.addSql(" select distinct a.orgno ");
  			de.addSql("   from odssu.ir_emp_org_all_role a, ");
  			de.addSql("        odssu.ir_org_closure b, ");
  			de.addSql("        odssu.orginfor c, ");
  			de.addSql("        odssu.org_type d ");
  			de.addSql("  where b.belongorgno = a.orgno ");
  			de.addSql("    and b.belongorgno = c.orgno ");
  			de.addSql("    and c.orgtype = d.typeno ");
  			de.addSql("    and d.typenature = 'B' ");
  			de.addSql("    and b.orgno = :orgno ");
  			de.addSql("    and a.empno = :empno ");
  			de.addSql("    and a.roleno = :roleno ");
			this.de.setString("orgno", orgno);
			this.de.setString("empno", this.getUser().getUserid());
			this.de.setString("roleno", OdssuContants.ROLE_ODS_ORGADMIN);
			DataStore irEmpOrgRoleVds = this.de.query();
			if(irEmpOrgRoleVds == null || irEmpOrgRoleVds.size() == 0){
				hasRight = false;
				bljgid = "";
			}else{
				hasRight = true;
				bljgid = irEmpOrgRoleVds.getString(0, "orgno");
			}
		}
		
		DataObject result = DataObject.getInstance();
		result.put("hasright", hasRight);
		result.put("bljgid", bljgid);
		return result;
	}
	/**
	 * 方法简介：获取可选干系角色的数量
	 * 郑海杰  2015-11-20
	 */
	public DataObject getOptionRoleCount(DataObject para) throws AppException{
		DataStore optionRoleVds = getOptionRoleDS(para);
		DataObject result = DataObject.getInstance();
		result.put("jscount", optionRoleVds.size());
		return result;
	}
	/**
	 * 方法简介：获得单个可选的角色编号
	 * 郑海杰  2015-11-20
	 */
	public DataObject getOptionRoleOne(DataObject para) throws AppException{
		DataStore optionRoleDS = getOptionRoleDS(para);
		if(optionRoleDS == null || optionRoleDS.size() == 0){
			throw new AppException("未取到合适的干系角色");
		}
		String roleno = optionRoleDS.getString(0, "roleno");
		String rolename = optionRoleDS.getString(0, "rolename");
		DataObject result = DataObject.getInstance();
		result.put("roleno", roleno);
		result.put("rolename", rolename);
		return result;
	}
	/**
	 * 方法简介：获得多个可选的角色DataStore
	 * 郑海杰  2015-11-20
	 */
	public DataObject getOptionRoleVds(DataObject para) throws AppException{
		DataStore optionRoleVds = getOptionRoleDS(para);
		DataObject result = DataObject.getInstance();
		result.put("optionrolevds", optionRoleVds);
		return result;
	}
	/**
	 * 方法简介：获得可选干系角色DS
	 * 郑海杰  2015-11-20
	 */
	private DataStore getOptionRoleDS(DataObject para) throws AppException{
		String orgno = para.getString("orgno");
  		de.clearSql();
  		de.addSql(" select c.sleepflag,c.roleno,c.rolename ");
  		de.addSql("   from odssu.orginfor a, ");
  		de.addSql("        odssu.ir_org_role_type b, ");
  		de.addSql("        odssu.roleinfor c ");
  		de.addSql("  where a.orgtype = b.orgtypeno ");
  		de.addSql("    and b.roletypeno = c.roletype ");
  		de.addSql("    and c.jsgn in ('1','2') ");
  		de.addSql("    and c.rolenature = '5' ");
  		de.addSql("    and c.roleno <> 'MEMBER' ");
  		de.addSql("    and a.orgno = :orgno ");
		this.de.setString("orgno", orgno);
		DataStore optionRoleVds = this.de.query();
		return optionRoleVds;
	}
	
}
