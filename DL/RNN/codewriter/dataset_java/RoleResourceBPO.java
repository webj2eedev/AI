package com.dw.hsuods.vap.role.roleResTreeView;


import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class RoleResourceBPO extends BPO{

	public DataObject getRoleResFolder(DataObject para) throws AppException {
		
		String folderid = para.getString("folderid");
		String roleno = para.getString("roleno");
		
		de.clearSql();
		de.addSql(" select a.pdid||'.'||a.dptdid resourceid, a.pdlabel||'.'||a.dptdlabel resourcename ,'流程' zylx ");
		de.addSql(" from odssu.dutyposition_task_role a ");
		de.addSql(" where a.roleno = :roleno ");
		de.addSql(" and a.pdid in (select m.pdid from odssu.pd_resfolder m where m.folderid = :folderid )");
		de.addSql(" order by a.pdid, a.dptdid ");
		de.setString("roleno", roleno);
		de.setString("folderid", folderid);
		DataStore lcrwds = de.query();
		
		de.clearSql();
		de.addSql(" select distinct e.functionid resourceid,e.functionname resourcename,'功能' zylx ");
		de.addSql("   from odssu.role_function_manual d, odssu.appfunction e ");
		de.addSql("  where d.roleno = :roleno ");
		de.addSql("	   and e.functionid = d.functionid ");
		de.addSql("    and d.functionid in (select m.fnid from odssu.fn_resfolder m where m.folderid = :folderid )");
		de.addSql("	 order by e.functionid ");
		de.setString("roleno", roleno);
		de.setString("folderid", folderid);
		DataStore gnrwds = de.query();
		
		lcrwds.combineDatastore(gnrwds);
		
		DataObject result = DataObject.getInstance();
		result .put("resourceds", lcrwds);
		
		return result;
	}
	public DataObject getRoleResWFP(DataObject para) throws AppException {
		
		String roleno = para.getString("roleno");
		
		de.clearSql();
		de.addSql(" select a.pdid||'.'||a.dptdid resourceid, a.pdlabel||'.'||a.dptdlabel resourcename ,'流程' zylx ");
		de.addSql("   from odssu.dutyposition_task_role a ");
		de.addSql("  where a.roleno = :roleno ");
		de.addSql("    and not exists (select 1 from odssu.pd_resfolder m where m.pdid = a.pdid )");
		de.addSql("  order by a.pdid, a.dptdid ");
		de.setString("roleno", roleno);
		DataStore lcrwnocodeds = de.query();
		
		de.clearSql();
		de.addSql(" select distinct e.functionid resourceid,e.functionname resourcename,'功能' zylx ");
		de.addSql("   from odssu.role_function_manual d, ");
		de.addSql("		   odssu.appfunction e ");
		de.addSql("  where d.roleno = :roleno ");
		de.addSql("	   and e.functionid = d.functionid ");
		de.addSql("    and not exists (select 1 from odssu.fn_resfolder m where m.fnid = d.functionid )");
		de.addSql("	 order by e.functionid ");
		de.setString("roleno", roleno);
		DataStore gnrwds = de.query();
		
		lcrwnocodeds.combineDatastore(gnrwds);
		
		DataObject result = DataObject.getInstance();
		result .put("resourceds", lcrwnocodeds);
		
		return result;
	}
	
	public DataObject getRoleResStatistic(DataObject para) throws AppException {
		
		String roleno = para.getString("roleno");
		
		de.clearSql();
		de.addSql("select rolename from odssu.roleinfor where roleno = :roleno ");
		de.setString("roleno", roleno);
		DataStore rolenameds = de.query();
		
		String rolename = rolenameds.getString(0, "rolename");
		
		de.clearSql();
		de.addSql("select distinct b.pdid,b.dptdid  ");
		de.addSql("  from odssu.dutyposition_task_role b ");
		de.addSql(" where b.roleno = :roleno  ");
		de.setString("roleno", roleno);
		DataStore roledpds = de.query();
		
		de.clearSql();
		de.addSql("select count(1) countfn ");
		de.addSql("  from odssu.role_function_manual b ");
		de.addSql(" where b.roleno = :roleno ");
		de.setString("roleno", roleno);
		DataStore rolefnds = de.query();
		
		int dpcount = roledpds.rowCount();
		int fncount = rolefnds.getInt(0, "countfn");
		int allresource = dpcount+fncount;
		
		String label = "角色["+rolename+"]拥有"+allresource+"个资源，分别为"+dpcount+"个流程资源，"+fncount+"个功能资源。";
		
		DataObject result = DataObject.getInstance();
		result.put("label", label);
		return result;
	}
	
}
