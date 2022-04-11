package com.dw.emp.emprolecover;

import org.apache.commons.lang3.StringUtils;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dareway.message.O2ASingleHandler;
import com.dw.util.OdssuUtil;
import com.dw.util.SendMsgUtil;

public class EMPRoleCoverBPO extends BPO{
	
	public DataObject judgeAuth_ORGADMIN(DataObject para) throws Exception {

		String empno = para.getString("empno","");
		if(StringUtils.isBlank(empno)) {
			throw new AppException("为操作员角色覆盖时出错，未获取到人员编号！");
		}
		
		DataObject result = DataObject.getInstance();
		
		de.clearSql();
		de.addSql(" select b.hrbelong , c.orgname ");
		de.addSql("   from odssu.empinfor b , odssu.orginfor c ");
		de.addSql("  where b.empno = :empno ");
		de.addSql("    and b.hrbelong = c.orgno ");
		de.addSql("    and b.sleepflag = '0' ");
		de.addSql("    and c.sleepflag = '0' ");
		de.setString("empno", empno );
		DataStore emphrbelongds = de.query();
		
		if(emphrbelongds == null || emphrbelongds.rowCount() == 0 ) {
			throw new AppException("权限覆盖时出错，当前根据操作员编号【"+empno+"】未获取到操作员人事隶属机构信息！");
		}
		String hrbelong = emphrbelongds.getString(0, "hrbelong"); 
		
		de.clearSql();
		de.addSql("select 1 ");
		de.addSql("  from odssu.ir_emp_org_all_role a  ");
		de.addSql(" where a.empno = :userid ");
		de.addSql("   and a.roleno = :roleno ");
		de.addSql("   and a.orgno = :orgno ");
		de.setString("userid", this.getUser().getUserid());
		de.setString("orgno", hrbelong );
		de.setString("roleno", OdssuContants.ROLE_ODS_ORGADMIN );
		DataStore judgeAuthds = de.query();
		
		if(judgeAuthds == null || judgeAuthds.rowCount() ==0) {
			String orgname = emphrbelongds.getString(0, "orgname"); 
			
			result.put("errflag", "1");
			result.put("errtext", "您没有权限对当前操作员做权限覆盖");
			result.put("todo", "机构【"+orgname+"】的"+OdssuUtil.getRoleNameByRoleno(OdssuContants.ROLE_ODS_ORGADMIN)
								+"有此功能权限");
			result.put("hrbelong", hrbelong);
			return result;
		}
		result.put("errflag", "0");
		result.put("hrbelong", hrbelong);
		return result;
	}
	
	public DataObject queryTargetEMP(DataObject para) throws Exception {
		
		String hrbelong = para.getString("hrbelong");
		String empno = para.getString("empno");
		String searchkey = para.getString("searchkey","");
		
		if(StringUtils.isBlank(searchkey)) {
			searchkey = "%";
		}else {
			searchkey = "%"+searchkey.toUpperCase()+"%";
		}
		de.clearSql();
		de.addSql(" select a.empno , a.empname , a.loginname ");
		de.addSql("   from odssu.empinfor a ");
		de.addSql("  where a.hrbelong = :hrbelong ");
		de.addSql("    and a.sleepflag = '0' ");
		de.addSql("    and a.empno <> :empno ");
		de.addSql("    and (a.empname like :searchkey or a.loginname like :searchkey ");
		de.addSql("     or a.empnamepy like :searchkey or a.empno = :searchkey ) ");
		de.setString("hrbelong", hrbelong);
		de.setString("empno", empno);
		de.setString("searchkey", searchkey);
		DataStore targetempds = de.query();
		
		DataObject result = DataObject.getInstance();
		result.put("empds", targetempds);
		
		return result;
	}
	
	public DataObject saveEMPCopyRole(DataObject para)throws Exception {
		
		String targetempno = para.getString("targetempno");
		
		String empno = para.getString("empno");//为empno赋予targetempno拥有的业务角色
		
		
		de.clearSql();
		de.addSql(" insert into odssu.ir_emp_org_all_role(empno,roleno,orgno,rolenature,jsgn)");
		de.addSql(" select :empno,a.roleno, a.orgno ,'5','3' ");
		de.addSql("   from odssu.ir_emp_org_all_role a ");
		de.addSql("  where a.empno = :targetempno ");
		de.addSql("    and a.isFormal = '1' ");
		de.addSql("    and a.roleno in (select c.roleno from odssu.roleinfor c where c.jsgn = '3' )");
		de.addSql("    and not exists ( select 1 from odssu.ir_emp_org_all_role b ");
		de.addSql("                      where a.orgno = b.orgno ");
		de.addSql("                        and a.roleno = b.roleno  ");
		de.addSql("                        and b.empno = :empno  ) ");
		de.setString("targetempno", targetempno);
		de.setString("empno", empno);
		de.update();
		
		O2ASingleHandler.updateSingleUserWithRole(empno);
		
		SendMsgUtil.SynEmpAddRole(empno);
		
		return null;
	}

}
