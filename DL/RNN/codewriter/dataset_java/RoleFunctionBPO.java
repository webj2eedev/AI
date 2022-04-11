package com.dw.hsuods.vap.role.roleFunctionTreeView;


import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class RoleFunctionBPO extends BPO{

	public DataObject getFunctionInfo(DataObject para) throws AppException {
		String folderno = para.getString("folderno");
		String roleno = para.getString("roleno");
		
		de.clearSql();
		de.addSql(" select distinct e.functionname,e.functionid ");
		de.addSql("   from odssu.role_function_manual d, ");
		de.addSql("        odssu.appfunction e ");
		de.addSql("  where e.functionid = d.functionid ");
		de.addSql("    and d.roleno not in (select n.roleno from odssu.njjs_filter n ) ");
		de.addSql("    and d.roleno in (select r.roleno from odssu.roleinfor r where r.sleepflag = '0') ");
		de.addSql("    and d.roleno = :roleno ");
		de.addSql("    and e.fnfolderid = :folderno ");
		de.addSql("	 order by e.functionid ");
		de.setString("roleno", roleno);
		de.setString("folderno", folderno);
		DataStore vds = de.query();
		
		DataObject result = DataObject.getInstance();
		result.put("functionds", vds);
		return result;
	}
	public DataObject setUserViewMode(DataObject para) throws AppException {
		String viewmode = para.getString("viewmode","grid");
		this.getUser().setProperty("viewmode", viewmode);
		return null;
	}
}
