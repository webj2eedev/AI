package com.dw.emp.empFunctionTreeView;


import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class EmpFunctionBPO extends BPO{

	public DataObject getEmpFunctionInfo(DataObject para) throws AppException {
		String folderno = para.getString("folderno");
		String empno = para.getString("empno");
		
		de.clearSql();
		de.addSql(" select distinct a.roleno,e.functionname,e.functionid,b.orgno,b.orgname,c.rolename ");
		de.addSql("   from odssu.ir_emp_org_all_role a, ");
		de.addSql("        odssu.orginfor b, ");
		de.addSql("        odssu.roleinfor c, ");
		de.addSql("        odssu.role_function_manual d, ");
		de.addSql("        odssu.appfunction e ");
		de.addSql("  where a.orgno = b.orgno ");
		de.addSql("    and e.functionid = d.functionid ");
		de.addSql("    and a.roleno = c.roleno ");
		de.addSql("    and d.roleno = a.roleno ");
		de.addSql("    and a.roleno not in (select n.roleno from odssu.njjs_filter n ) ");
		de.addSql("    and a.roleno in (select r.roleno from odssu.roleinfor r where r.sleepflag = '0') ");
		de.addSql("    and a.empno = :empno ");
		de.addSql("    and e.fnfolderid = :folderno ");
		de.addSql("	 order by b.orgno, e.functionid, a.roleno ");
		de.setString("empno", empno);
		de.setString("folderno", folderno);
		DataStore vds = de.query();
		
		DataObject result = DataObject.getInstance();
		result.put("functionds", dealFnDataStore(vds));
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
			String functionid = gnrwds.getString(i, "functionid");
			String functionname = gnrwds.getString(i, "functionname");

			int locate = showds.find("functionid == " + functionid + " and  orgno == " + orgno);
			if (locate >= 0) {
				DataObject showdo = showds.get(locate);
				String aggrolename = showdo.getString("aggrolename");
				if (aggrolename.indexOf(rolename) == -1) {
					showdo.put("aggrolename", aggrolename + "," + rolename);
				}
			} else {
				DataObject showdo = DataObject.getInstance();
				showdo.put("functionid", functionid);
				showdo.put("functionname", functionname);
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

}
