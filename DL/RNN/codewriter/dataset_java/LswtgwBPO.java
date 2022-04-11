package com.dw.odssu.ws.emp.lswtgw;

import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dareway.message.O2ASingleHandler;
import com.dw.util.SendMsgUtil;

public class LswtgwBPO extends BPO{
	
	public DataObject getRoleByEmpno(DataObject para) throws AppException{
		String grantno = para.getString("grantno");
  		de.clearSql();
  		de.addSql(" select c.rolename, d.orgname  ");
  		de.addSql("   from odssu.emp_wtdg_role a, ");
  		de.addSql("        odssu.roleinfor c,     ");
  		de.addSql("        odssu.orginfor d       ");
  		de.addSql("  where a.grantno = :grantno   ");
  		de.addSql("    and a.roleno = c.roleno    ");
  		de.addSql("    and a.orgno = d.orgno      ");
		de.setString("grantno", grantno);
		DataStore roleds =  de.query();
				
		DataObject result = DataObject.getInstance();
		result.put("roleds", roleds);
		return result;
	}
	public DataObject delRole(DataObject para) throws Exception{
		
		// 收回临时角色首先肯定会删除emp_wtdg和emp_wtdg_role中的相关数据
		// 看是否有其他人授予临时角色
		// 删除角色的数据，不能删除正式的角色，只删除临时的角色

		String grantno = para.getString("grantno");
		String granted_emp = para.getString("granted_emp");
		String granter_emp = para.getString("granter_emp");
		
		de.clearSql();
  		de.addSql("select a.orgno, a.roleno     ");
  		de.addSql("  from odssu.emp_wtdg_role a ");
  		de.addSql(" where a.grantno = :grantno  ");
		de.setString("grantno", grantno);
		DataStore roleds = de.query();

		DataStore iremporgrole = DataStore.getInstance();
		
		// 首先得到即将有哪些角色会被收回，下面检查，这个角色是否也被其他人授予
		int roleCount = roleds.rowCount();
		for (int n = 0; n < roleCount; n++) {
			String orgno = roleds.getString(n, "orgno");
			String roleno = roleds.getString(n, "roleno");
			de.clearSql();
  			de.addSql(" select 1");
  			de.addSql("   from odssu.emp_wtdg a ,    ");
  			de.addSql("        odssu.emp_wtdg_role b ");
  			de.addSql("  where a.grantno = b.grantno ");
  			de.addSql("    and b.orgno =:orgno       ");
  			de.addSql("    and b.roleno = :roleno    ");
  			de.addSql("    and a.granted_empno = :granted_emp  ");
  			de.addSql("    and a.granter_empno <> :granter_emp ");
			de.setString("granted_emp", granted_emp);
			de.setString("orgno", orgno);
			de.setString("roleno", roleno);
			de.setString("granter_emp", granter_emp);
			DataStore other = de.query();
			// 有其他人授予临时角色，不再关注此角色
			if (other.rowCount() > 0) {
				break;
			}
			de.clearSql();
			de.addSql("delete from odssu.ir_emp_org_all_role a ");
			de.addSql(" where a.empno = :granted_emp ");
			de.addSql("   and a.roleno = :roleno ");
			de.addSql("   and a.orgno = :orgno   ");
			de.addSql("   and a.isFormal <> '1'  ");
			de.setString("granted_emp", granted_emp);
			de.setString("orgno", orgno);
			de.setString("roleno", roleno);
			de.update();
			
			//组装消息
			DataObject tempir = DataObject.getInstance();
			tempir.put("empno", granted_emp);
			tempir.put("orgno", orgno);
			tempir.put("roleno", roleno);
			tempir.put("differ", 0);
			
			iremporgrole.addRow(tempir);
			// 主要看empno是否有正式岗位，如果有不删除emp_inner_duty
			/*
			 * if (OdssuUtil.hasFormalInnerDuty(granterempno, orgno, roleno)) { break; }
			 

			de.clearSql();
  			de.addSql(" delete from odssu.emp_inner_duty a");
  			de.addSql(" where a.empno = :grantedempno");
  			de.addSql("   and a.orgno = :orgno");
  			de.addSql("   and a.roleno = :roleno ");
  			de.addSql("   and a.formalflag = '0' ");
			de.setString("grantedempno", grantedempno);
			de.setString("orgno", orgno);
			de.setString("roleno", roleno);
			int m = de.update();
			if (m <= 0) {
				this.bizException("删除失败");
			}

			// 准备删除人员机构角色联系
			DataObject newPara = DataObject.getInstance();

			// 删除人员机构角色联系
			newPara.clear();
			newPara.put("roleno", roleno);
			newPara.put("empno", grantedempno);
			newPara.put("orgno", orgno);

			allroleapo.doEntry(GlobalNames.DEFAULT_BIZ, newPara);
			*/

		}
		de.clearSql();
  		de.addSql(" delete from odssu.emp_wtdg ");
  		de.addSql("  where grantno = :grantno  ");
		de.setString("grantno", grantno);
		de.update();

		de.clearSql();
  		de.addSql(" delete from odssu.emp_wtdg_role ");
  		de.addSql("  where grantno = :grantno       ");
		de.setString("grantno", grantno);
		de.update();
		
		O2ASingleHandler.updateSingleUserWithRole(granted_emp);
		
		if(GlobalNames.DEPLOY_IN_ECO) {
			
			SendMsgUtil.SynEmpAddRole(granted_emp);
		}
		
		return null;
	}
}
