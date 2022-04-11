package com.dw.org.orgqueryuncheck;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.dbengine.DE;

public class OrgQueryUnCheckBPO extends BPO{
	/**
	 * 点击一个机构下面显示的数据
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataStore queryHrOrgLov4EmpAdd(DataObject para) throws AppException {
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
		de.addSql("        and o.sleepflag = '0' and i.belongorgno in (select db.orgno from odssu.ir_dbid_org db where db.dbid = :dbid ) ");
		de.addSql("        and o.orgtype not in (select typeno from odssu.org_type where  typenature = 'A') ");
		de.addSql("        and  oy.typename  not in  ( "+roletype1 +")");
		if(orgno !=""){
			de.addSql(" and exists (select 1 from odssu.ir_org_closure a where a.orgno=o.orgno and a.belongorgno= :belongorgno )");
		}
		de.addSql("   		order by oy.sn, o.orgno   ");
		
		de.setString("dbid", dbid);
		if(orgno!=""){
			de.setString("belongorgno", orgno);
		}
		DataStore orgds = de.query();

			return orgds;
	}
	/**
	 * 获取某一个点击的机构下面的全部机构信息，包括已选中的机构进行处理
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject dealOrgData(DataObject para) throws AppException {
		String belongorg = para.getString("orgno").toString();
		String roletype = para.getString("roletype");
		para.put("belongorgno", belongorg);
		String [] roletypearr = roletype.split(",");
		String roletype1 = "";
		for(int i=0;i<roletypearr.length-1;i++){
			roletype1 = roletype1+"'"+roletypearr[i]+"',";
		}
		roletype1 = roletype1+"'"+roletypearr[roletypearr.length-1]+"'";
		para.put("roletype1", roletype1);
		//全部的机构信息
		DataStore orgds = queryHrOrgLov4EmpAdd(para);
		//已选中的机构信息
		DataObject result = DataObject.getInstance();
		result.put("orgds", orgds);
		return result;
	}
	
	public DataObject getname(DataObject para) throws AppException{
		String empno = para.getString("empno");
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" select empname  ");
  		de.addSql(" from   odssu.empinfor t ");
  		de.addSql(" where  t.empno = :empno ");
		de.setString("empno", empno);
		DataStore ds = de.query();
		String empname = ds.getString(0, "empname");
		String roleno = para.getString("roleno");
		de.clearSql();
  		de.addSql(" select rolename  ");
  		de.addSql(" from   odssu.roleinfor t ");
  		de.addSql(" where  t.roleno = :roleno ");
		de.setString("roleno", roleno);
		DataStore ds1 = de.query();
		String rolename = ds1.getString(0, "rolename");
		DataObject result = DataObject.getInstance();
		result.put("rolename", rolename);
		result.put("empname", empname);
		return result;
	}
	

}

