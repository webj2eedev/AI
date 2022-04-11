package com.dw.res.fnTask;

import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class FnTaskBPO extends BPO{
	
	public DataObject queryFn(DataObject para) throws Exception{
		
		String folderid = para.getString("folderid");
		String key = para.getString("key");
		
		if("".equals(folderid)) {
			folderid = "%";
		}
		if("*根据功能任务模糊查询".equals(key)||"".equals(key)) {
			key = "%";
		}else {
			key = "%"+key+"%";
		}
		
		de.clearSql();
		de.addSql("select a.functionid ,a.functionname ,a.pdid,a.appid ");
		de.addSql("  from odssu.appfunction a ");
		de.addSql(" where a.fnfolderid like :folderid ");
		de.addSql("   and (lower(a.functionid) like :key ");
		de.addSql("    or lower(a.functionname) like :key ) ");
		de.setString("folderid", folderid);
		de.setString("key", key);
		DataStore vds = de.query();
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("fnlist", vds);
		return vdo;
	}
	

	/**************************Fn查询分页*********************************************/
	public DataObject fnQueryGetRowCount(DataObject para) throws Exception {
		String folderid = para.getString("folderid");
		String key = para.getString("key");
		if("".equals(folderid)) {
			folderid = "%";
		}
		if("*根据功能任务模糊查询".equals(key)||"".equals(key)) {
			key = "%";
		}else {
			key = "%"+key+"%";
		}
		
		de.clearSql();
		de.addSql("select  count(*) row_count ");
		de.addSql("from  (select a.functionid ,a.functionname ,a.pdid,a.appid, ");
		de.addSql(" 	(select n.appname from odssu.appinfo n where a.appid = n.appid ) appname ");
		de.addSql("  from odssu.appfunction a ");
		de.addSql(" where a.fnfolderid like :folderid ");
		de.addSql("   and (lower(a.functionid) like :key ");
		de.addSql("    or lower(a.functionname) like :key ) ");
		de.addSql("   and a.pdid is null ) row ");
		de.setString("folderid", folderid);
		de.setString("key", key);
		DataStore ds = de.query();
		return ds.getRow(0);
	}

	public DataObject fnQueryGetPageRows(DataObject para) throws Exception {
		int g_startRowNumber = para.getInt("g_startRowNumber");
		int g_endRowNumber = para.getInt("g_endRowNumber");	
		DataObject vdo = DataObject.getInstance();
		String folderid = para.getString("folderid");
		String key = para.getString("key");
		if("".equals(folderid)) {
			folderid = "%";
		}
		if("*根据功能任务模糊查询".equals(key)||"".equals(key)) {
			key = "%";
		}else {
			key = "%"+key+"%";
		}
		
		de.clearSql();
		de.addSql("select a.functionid ,a.functionname ,a.pdid,a.appid, ");
		de.addSql(" 	(select n.appname from odssu.appinfo n where a.appid = n.appid ) appname ");
		de.addSql("  from odssu.appfunction a ");
		de.addSql(" where a.fnfolderid like :folderid ");
		de.addSql("   and (lower(a.functionid) like :key ");
		de.addSql("    or lower(a.functionname) like :key ) ");
		de.addSql("   and a.pdid is null  ");
		de.setString("folderid", folderid);
		de.setString("key", key);
		DataStore vds = de.query();
		if (vds.isEmpty()) {
			vdo.put("vds", null);
		} else {
			DataStore subvds = vds.subDataStore(g_startRowNumber - 1, g_endRowNumber);
			String sqlStr;
			sqlStr = "and ( a.functionid = '" + subvds.getString(0, "functionid")+ "'";
			// 将任务功能进行切块
			for (int i = 1; i < subvds.rowCount() - 1; i++) {
				String newfunctionid = subvds.getString(i, "functionid");
				sqlStr += "    or  a.functionid = '" + newfunctionid  + "'";
			}
			sqlStr += "  or a.functionid = '" + subvds.getString(subvds.rowCount() - 1, "functionid") + "')";
			// 获取全部的任务功能以及机构，角色信息
			de.clearSql();
			de.addSql("select a.functionid ,a.functionname ,a.pdid,a.appid, ");
			de.addSql(" 	(select n.appname from odssu.appinfo n where a.appid = n.appid ) appname ");
			de.addSql("  from odssu.appfunction a ");
			de.addSql(" where a.fnfolderid like :folderid ");
			de.addSql(sqlStr);
			de.addSql("   and (lower(a.functionid) like :key ");
			de.addSql("    or lower(a.functionname) like :key ) ");
			de.addSql("   and a.pdid is null ");
			de.setString("folderid", folderid);
			de.setString("key", key);
			DataStore vds1 = de.query();
			vdo.put("vds", vds1);
		}
		
		return vdo;
	}

	public DataObject fnQueryGetAllRows(DataObject para) throws Exception {
		String folderid = para.getString("folderid");
		String key = para.getString("key");
		if("".equals(folderid)) {
			folderid = "%";
		}
		if("*根据功能任务模糊查询".equals(key)||"".equals(key)) {
			key = "%";
		}else {
			key = "%"+key+"%";
		}
		
		de.clearSql();
		de.addSql("select a.functionid ,a.functionname ,a.pdid,a.appid, ");
		de.addSql(" 	(select n.appname from odssu.appinfo n where a.appid = n.appid ) appname ");
		de.addSql("  from odssu.appfunction a ");
		de.addSql(" where a.fnfolderid like :folderid ");
		de.addSql("   and (lower(a.functionid) like :key ");
		de.addSql("    or lower(a.functionname) like :key ) ");
		de.addSql("   and a.pdid is null ");
		de.setString("folderid", folderid);
		de.setString("key", key);
		DataStore vds = de.query();
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("fnlist", vds);
		return vdo;
	}
	
	
	public DataObject getResourceInfo(DataObject para) throws AppException{
		String functionid = para.getString("functionid");
		String functionname = getFunctionLabel(para).getString("functionname");
		String strorgtypename = "";
		String isnjjs;
		
		de.clearSql();
		de.addSql(" select t.roleno ");
		de.addSql("   from odssu.role_function_manual t ");
		de.addSql("  where t.functionid =:functionid ");
		de.addSql("    and exists (select 1 from odssu.roleinfor a where t.roleno = a.roleno and a.sleepflag = '0' ) ");
		de.addSql("    and t.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("functionid", functionid);
		DataStore roleds = de.query();
		
		de.clearSql(); 
		de.addSql(" select distinct r.empno ");
		de.addSql("   from odssu.role_function_manual t, ");
		de.addSql("        odssu.ir_emp_org_all_role r ");
		de.addSql("  where t.functionid=:functionid ");
		de.addSql("    and t.roleno = r.roleno ");
		de.addSql("    and exists (select 1 from odssu.roleinfor b where r.roleno = b.roleno and b.sleepflag = '0') ");
		de.addSql("    and exists(select 1 from odssu.empinfor a where a.empno = r.empno and a.sleepflag = '0') ");
		de.addSql("    and r.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("functionid", functionid);
		DataStore operatords = de.query();
		
		de.clearSql();
		de.addSql("select c.typename orgtypename,b.orgtypeno  ");
		de.addSql("  from odssu.fn_roletype a,odssu.ir_org_role_type b,odssu.org_type c ");
		de.addSql(" where a.roletypeno = b.roletypeno ");
		de.addSql("   and b.ORGTYPENO = c.TYPENO ");
		de.addSql("   and a.functionid=:functionid  ");
		de.setString("functionid", functionid);
		DataStore orgtypename = de.query();
		if(orgtypename !=null && orgtypename.rowCount() >0) {
			strorgtypename = orgtypename.getString(0,"orgtypename");
			for(int i=1;i<orgtypename.rowCount();i++) {
				strorgtypename = strorgtypename+"，"+orgtypename.getString(i,"orgtypename");
			}
		}
		de.clearSql();
		de.addSql(" select 1 from odssu.fn_roletype a where a.functionid = :functionid ");
		de.setString("functionid", functionid);
		DataStore fnroletypeds = de.query();
		if(fnroletypeds == null || fnroletypeds.rowCount() ==0) {
			isnjjs = "true";
		}else {
			isnjjs = "false";
		}

		String appid = "", appname = "";
		de.clearSql();
		de.addSql("select a.appid ");
		de.addSql("  from odssu.appfunction a ");
		de.addSql(" where a.functionid = :functionid   ");
		de.setString("functionid", functionid);
		DataStore appidds = de.query();
		if(appidds !=null && appidds.rowCount() >0) {
			appid = appidds.getString(0,"appid");
			if(appid != null && !appid.equals("")) {
				de.clearSql();
				de.addSql("select a.appname ");
				de.addSql("  from odssu.appinfo a ");
				de.addSql(" where a.appid = :appid   ");
				de.setString("appid", appid);
				DataStore appnameds = de.query();
				if(appnameds !=null && appnameds.rowCount() >0) {
					appname = appnameds.getString(0,"appname");
				}
			}
		}
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("rolenum", roleds.rowCount()+"");
		vdo.put("operatornum", operatords.rowCount()+"");
		vdo.put("orgtypename", strorgtypename);
		vdo.put("orgtypeno", orgtypename);
		vdo.put("functionname", functionname);
		vdo.put("isnjjs", isnjjs);
		vdo.put("appid", appid);
		vdo.put("appname", appname);
		return vdo;
	}
	public DataObject getAllRole(DataObject para) throws AppException{
		
		String functionid = para.getString("functionid");
		
		de.clearSql();
		de.addSql(" select t.roleno ,r.rolename  ");
		de.addSql("   from odssu.role_function_manual t ,");
		de.addSql("        odssu.roleinfor r ");
		de.addSql("  where t.functionid=:functionid ");
		de.addSql("    and t.roleno = r.roleno ");
		de.addSql("    and r.sleepflag = '0'   ");
		de.addSql("    and r.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("functionid", functionid);
		DataStore roleds = de.query();
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("rolelist", roleds);
		return vdo;
	}
	public DataObject getTreeKey(DataObject para) throws AppException{
		int nodenum = para.getInt("sn"); 
		de.clearSql();
		de.addSql("select a.fnfolderid,a.folderlabel ");
		de.addSql("  from odssu.fn_folder a ");
		de.addSql(" where a.pfnfolderid is null ");
		DataStore folderds = de.query();
		
		if(folderds == null || folderds.rowCount() <nodenum +1) {
			throw new AppException("未获取到树节点！");
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("key", folderds.getString(nodenum, "fnfolderid"));
		return vdo;
	}
	public DataObject getEMPInfo(DataObject para) throws AppException{
		
		String roleid = para.getString("roleid");
		
		de.clearSql();
		de.addSql(" select b.empno,b.LOGINNAME,b.EMPNAME ,a.ROLENO,r.rolename,a.ORGNO,t.ORGNAME ");
		de.addSql("   from odssu.ir_emp_org_all_role a left join odssu.roleinfor r on a.ROLENO = r.roleno ,  ");
		de.addSql("        odssu.empinfor b , odssu.orginfor t ");
		de.addSql("  where a.empno = b.empno ");
		de.addSql("    and a.ORGNO = t.orgno ");
		de.addSql("    and b.sleepflag = '0' ");
		de.addSql("    and t.sleepflag = '0' ");
		de.addSql("    and a.roleno = :roleid ");
		de.addSql("    and r.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("roleid", roleid);
		DataStore vds = de.query();
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("emplist", combineDSCol(vds));
		return vdo;
	}
	public DataStore combineDSCol(DataStore para) throws AppException {
		DataStore newDS = DataStore.getInstance();
		for(DataObject empdo :para) {
			String empno = empdo.getString("empno");
			String orgno = empdo.getString("orgno");
			String orgname = empdo.getString("orgname");
			int index = newDS.find("empno == "+empno);
			
			if(index>=0) {
				String temporgno = newDS.getString(index, "orgno");
				String temporgname = newDS.getString(index, "orgname");
				
				newDS.put(index, "orgno", temporgno+','+orgno);
				newDS.put(index, "orgname", temporgname+','+orgname);
			}else {
				newDS.addRow(empdo);
			}
		}
		return newDS;
	}
	public DataObject getFunctionLabel(DataObject para)throws AppException{
		
		String functionid = para.getString("functionid");
		
		de.clearSql();
		de.addSql(" select a.functionname from odssu.appfunction a where a.functionid = :functionid ");
		de.setString("functionid", functionid);
		DataStore vds = de.query();
		
		if(vds == null || vds.rowCount() ==0) {
			throw new AppException("获取功能任务名称失败，functionid："+functionid);
		}
		return vds.get(0);
	}
}
