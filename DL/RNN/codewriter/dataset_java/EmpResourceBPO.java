package com.dw.emp.empProcessTreeView;


import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class EmpResourceBPO extends BPO{

	public DataObject getEmpOrgResource(DataObject para) throws AppException {
		
		String folderid = para.getString("folderid");
		String empno = para.getString("empno");
		
		de.clearSql();
		de.addSql(" select  d.pdid||'.'||d.dptdid resourceid,d.pdlabel||'.'||d.dptdlabel resourcename, a.ROLENO,b.orgno, b.orgname,c.rolename ,'流程' zylx ");
		de.addSql(" from    odssu.ir_emp_org_all_role a, ");
		de.addSql("			odssu.orginfor b, ");
		de.addSql("			odssu.roleinfor c, ");
		de.addSql("			odssu.dutyposition_task_role d ");
		de.addSql("where    a.orgno = b.orgno ");
		de.addSql("       	and a.roleno = c.roleno ");
		de.addSql("       	and a.empno = :empno");
		de.addSql("			and d.roleno = a.roleno ");
		de.addSql("         and d.pdid in (select m.pdid from odssu.pd_resfolder m where m.folderid = :folderid ) ");
		de.addSql("         and c.roleno not in (select n.roleno from odssu.njjs_filter n ) ");
		de.addSql("         and c.sleepflag = '0' ");
		de.addSql("order by b.orgno,d.pdid");
		de.setString("empno", empno);
		de.setString("folderid", folderid);
		DataStore tempdpds = de.query();
		
	
		
		de.clearSql();
		de.addSql(" select e.functionid resourceid,e.functionname resourcename, ");
		de.addSql("        a.roleno, b.orgno,b.orgname,c.rolename ,'功能' zylx ");
		de.addSql("   from odssu.ir_emp_org_all_role a, ");
		de.addSql("        odssu.orginfor b, ");
		de.addSql("        odssu.roleinfor c, ");
		de.addSql("        odssu.role_function_manual d, ");
		de.addSql("        odssu.appfunction e ");
		de.addSql("  where a.orgno = b.orgno ");
		de.addSql("    and a.roleno = c.roleno ");
		de.addSql("    and e.functionid = d.functionid ");
		de.addSql("    and d.roleno = a.roleno ");
		de.addSql("    and a.roleno not in (select n.roleno from odssu.njjs_filter n ) ");
		de.addSql("    and c.sleepflag = '0' ");
		de.addSql("    and a.empno = :empno ");
		de.addSql("    and d.functionid in (select m.fnid from odssu.fn_resfolder m where m.folderid = :folderid ) ");
		de.addSql("	 order by b.orgno, e.functionid, a.roleno ");
		de.setString("empno", empno);
		de.setString("folderid", folderid);
		DataStore tempfnds = de.query();
		
		DataStore resourceds = dealDpDataStore(tempdpds);
		
		resourceds.combineDatastore(dealFnDataStore(tempfnds));
		
		DataObject result = DataObject.getInstance();
		result.put("resourceds", resourceds);
		
		return result;
	}
	public DataObject getEmpOrgResWfp(DataObject para) throws AppException {
		
		String empno = para.getString("empno");
		
		de.clearSql();
		de.addSql(" select  d.pdid||'.'||d.dptdid resourceid,d.pdlabel||'.'||d.dptdlabel resourcename, ");
		de.addSql("         a.ROLENO,b.orgno, b.orgname,c.rolename ,'流程' zylx ");
		de.addSql(" from    odssu.ir_emp_org_all_role a, ");
		de.addSql("			odssu.orginfor b, ");
		de.addSql("			odssu.roleinfor c, ");
		de.addSql("			odssu.dutyposition_task_role d ");
		de.addSql("where    a.orgno = b.orgno ");
		de.addSql("       	and a.roleno = c.roleno ");
		de.addSql("       	and a.empno = :empno");
		de.addSql("			and d.roleno = a.roleno ");
		de.addSql("         and not exists (select 1 from odssu.pd_resfolder m where m.pdid = d.pdid ) ");
		de.addSql("         and c.roleno not in (select n.roleno from odssu.njjs_filter n ) ");
		de.addSql("         and c.sleepflag = '0' ");
		de.addSql("order by b.orgno,d.pdid");
		de.setString("empno", empno);
		DataStore tempdpds = de.query();
		
		de.clearSql();
		de.addSql(" select e.functionid resourceid,e.functionname resourcename, ");
		de.addSql("        a.roleno, b.orgno,b.orgname,c.rolename ,'功能' zylx ");
		de.addSql("   from odssu.ir_emp_org_all_role a, ");
		de.addSql("        odssu.orginfor b, ");
		de.addSql("        odssu.roleinfor c, ");
		de.addSql("        odssu.role_function_manual d, ");
		de.addSql("        odssu.appfunction e ");
		de.addSql("  where a.orgno = b.orgno ");
		de.addSql("    and a.roleno = c.roleno ");
		de.addSql("    and d.roleno = a.roleno ");
		de.addSql("    and d.functionid = e.functionid ");
		de.addSql("    and a.roleno not in (select n.roleno from odssu.njjs_filter n ) ");
		de.addSql("    and c.sleepflag = '0' ");
		de.addSql("    and a.empno = :empno ");
		de.addSql("    and not exists (select 1 from odssu.fn_resfolder m where m.fnid = d.functionid )");
		de.addSql("	 order by b.orgno, e.functionid, a.roleno ");
		de.setString("empno", empno);
		DataStore tempfnds = de.query();
		
		DataStore resourceds = dealDpDataStore(tempdpds);
		
		resourceds.combineDatastore(dealFnDataStore(tempfnds));
		
		DataObject result = DataObject.getInstance();
		result.put("resourceds", resourceds);
		
		return result;
	}
	
	public DataStore dealFnDataStore(DataStore gnrwds) throws AppException {
		// 空数据集，用来存储最后的结果
		DataStore showds = DataStore.getInstance();
		for (int i = 0; i < gnrwds.rowCount(); i++) {
			String roleno = gnrwds.getString(i, "roleno");
			String rolename = gnrwds.getString(i, "rolename");
			String orgno = gnrwds.getString(i, "orgno");
			String orgname = gnrwds.getString(i, "orgname");
			String resourceid = gnrwds.getString(i, "resourceid");
			String resourcename = gnrwds.getString(i, "resourcename");

			int locate = showds.find("resourceid == " + resourceid + " and  orgno == " + orgno);
			if (locate >= 0) {
				DataObject showdo = showds.get(locate);
				String aggrolename = showdo.getString("aggrolename");
				if (aggrolename.indexOf(rolename) == -1) {
					showdo.put("aggrolename", aggrolename + "," + rolename);
				}
			} else {
				DataObject showdo = DataObject.getInstance();
				showdo.put("resourceid", resourceid);
				showdo.put("resourcename", resourcename);
				showdo.put("zylx", "功能");
				showdo.put("orgno", orgno);
				showdo.put("orgname", orgname);
				showdo.put("roleno", roleno);
				showdo.put("rolename", rolename);
				showdo.put("aggrolename", rolename);
				showds.addRow(showdo);
			}
		}

		return showds;
	}
	public DataStore dealDpDataStore(DataStore lcrwds) throws AppException {
		// 空数据集，用来存储最后的结果
		DataStore showds = DataStore.getInstance();
		for (int i = 0; i < lcrwds.rowCount(); i++) {
			String resourceid = lcrwds.getString(i, "resourceid");
			String orgno = lcrwds.getString(i, "orgno");
			String resourcename = lcrwds.getString(i, "resourcename");
			String orgname = lcrwds.getString(i, "orgname");
			String roleno = lcrwds.getString(i, "roleno");
			String rolename = lcrwds.getString(i, "rolename");

			int locate = showds.find("resourceid == " + resourceid + " and  orgno == " + orgno);
			if (locate >= 0) {
				DataObject showdo = showds.get(locate);
				String aggrolename = showdo.getString("aggrolename");
				if (aggrolename.indexOf(rolename) == -1) {
					showdo.put("aggrolename", aggrolename + "," + rolename);
				}
			} else {
				DataObject showdo = DataObject.getInstance();
				showdo.put("resourceid", resourceid);
				showdo.put("resourcename", resourcename);
				showdo.put("orgno", orgno);
				showdo.put("orgname", orgname);
				showdo.put("roleno", roleno);
				showdo.put("rolename", rolename);
				showdo.put("aggrolename", rolename);
				showdo.put("zylx", "流程");
				showds.addRow(showdo);
			}
		}

		return showds;
	}
	public DataObject getEmpResStatistic(DataObject para) throws AppException {
		
		String empno = para.getString("empno");
		
		de.clearSql();
		de.addSql("select empname from odssu.empinfor where empno = :empno ");
		de.setString("empno", empno);
		DataStore empnameds = de.query();
		
		String empname = empnameds.getString(0, "empname");
		
		de.clearSql();
		de.addSql(" select distinct a.orgno  ");
		de.addSql("   from odssu.ir_emp_org_all_role a ");
		de.addSql("  where a.empno = :empno ");
		de.addSql("    and a.roleno not in (select n.roleno from odssu.njjs_filter n )  ");
		de.setString("empno", empno);
		DataStore orgnumds = de.query();
		
		de.clearSql();
		de.addSql("select distinct b.pdid,b.dptdid  ");
		de.addSql("  from odssu.dutyposition_task_role b ");
		de.addSql(" where exists ( select 1  ");
		de.addSql("         from odssu.ir_emp_org_all_role c ");
		de.addSql("        where c.roleno = b.roleno ");
		de.addSql("          and c.roleno not in (select n.roleno from odssu.njjs_filter n ) ");
		de.addSql("          and c.empno = :empno ) ");
		de.setString("empno", empno);
		DataStore empdpds = de.query();
		
		de.clearSql();
		de.addSql("select count(1) countfn ");
		de.addSql("  from odssu.role_function_manual b ");
		de.addSql(" where exists ( select 1 ");
		de.addSql("         from odssu.ir_emp_org_all_role c ");
		de.addSql("        where b.roleno = c.roleno ");
		de.addSql("          and c.roleno not in (select n.roleno from odssu.njjs_filter n ) ");
		de.addSql("          and c.empno = :empno ) ");
		de.setString("empno", empno);
		DataStore empfnds = de.query();
		
		int orgnum = orgnumds.rowCount();
		int dpcount = empdpds.rowCount();
		int fncount = empfnds.getInt(0, "countfn");
		int allresource = dpcount+fncount;
		
		String label = "操作员["+empname+"]在"+orgnum+"个机构下拥有资源\n"+
						"操作员["+empname+"]拥有"+allresource+"个资源，分别为"+dpcount+"个流程资源，"+fncount+"个功能资源。";
		
		DataObject result = DataObject.getInstance();
		result.put("label", label);
		return result;
	}
	public DataObject setUserViewMode(DataObject para) throws AppException {
		String viewmode = para.getString("viewmode","grid");
		String userid = this.getUser().getUserid();
		
		de.clearSql();
		de.addSql(" select a.viewmode from odssu.emp_personalization a where a.empno = :userid ");
		de.setString("userid", userid);
		DataStore emp_personalization = de.query();
		
		if(emp_personalization == null || emp_personalization.rowCount() ==0 ) {
			de.clearSql();
			de.addSql(" insert into odssu.emp_personalization (empno,viewmode)");
			de.addSql(" values (:userid,:viewmode)");
			de.setString("userid", userid);
			de.setString("viewmode", viewmode);
			de.update();
		}else {
			de.clearSql();
			de.addSql(" update odssu.emp_personalization set viewmode = :viewmode where empno = :userid ");
			de.setString("userid", userid);
			de.setString("viewmode", viewmode);
			de.update();
		}
		
		return null;
	}
}
