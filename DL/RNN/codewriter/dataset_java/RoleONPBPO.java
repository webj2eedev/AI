package com.dw.role;

import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dareway.apps.odssu.OdssuContants;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.log.LogHandler;

public class RoleONPBPO extends BPO{
	
	public DataObject getRoleInfo(DataObject para) throws Exception{
//		DE de = DE.getInstance();
//		DataObject vdo = DataObject.getInstance();
//		String folderid = para.getString("folderid");
//		String pfolderid = "", folderlabel = "", deforgno = "", collect_query = "";
//		
//		de.clearSql();
//		de.addSql("select t.folderid, t.pfolderid, t.folderlabel, t.deforgno, t.collect_query from odssu.role_folder t where t.FOLDERID = :folderid ");
//		de.setString("folderid", folderid);
//		DataStore folderds = de.query();
//		if (folderds.rowCount() == 0) {
//			throw new Exception("folderid[" + folderid + "]未查到系统信息");
//		}else if(folderds.rowCount() > 1) {
//			throw new Exception("folderid[" + folderid + "]查到系统信息超过一条");
//		}else {
//			pfolderid = folderds.getString(0, "pfolderid");
//			folderlabel = folderds.getString(0, "folderlabel");
//			deforgno = folderds.getString(0, "deforgno");
//			collect_query = folderds.getString(0, "collect_query");
//		}
//		
//		String roleno = para.getString("roleno","");
//		String rolenoUpperCase = roleno.toUpperCase();
//		roleno = ((roleno == null || "".equals(roleno)) ? "%" : "%" + roleno + "%");
//		rolenoUpperCase = ((rolenoUpperCase == null || "".equals(rolenoUpperCase)) ? "%" : "%" + rolenoUpperCase + "%");
//
//  		de.clearSql();
//  		de.addSql(" select roleno, displayname, rolename, sleepflag ");
//  		de.addSql(" from odssu.roleinfor ");
//  		de.addSql(" where (roleno like :roleno or rolename like :rolenouppercase or upper(rolenamepy) like :rolenouppercase) ");
//  		de.addSql(" and sleepflag = '0' ");
//  		de.addSql(" and folderno = :folderid ");
//		if("1".equals(collect_query)) {
//	  		de.addSql(" and deforgno = :deforgno ");
//		}
//  		de.addSql(" order by sleepflag, roleno, rolename ");
//		de.setString("folderid", folderid);
//		de.setString("roleno", roleno);
//		de.setString("rolenouppercase", rolenoUpperCase);
//		if("1".equals(collect_query)) {
//			de.setString("deforgno", deforgno);
//		}
//		DataStore roleds = de.query();
//		
//		int rolecount = roleds.rowCount();
//		for(int i = 0; i < rolecount; i++) {
//			String typename = "";
//			String ROLENO = roleds.getString(i, "roleno");
//	  		de.clearSql();
//	  		de.addSql(" select b.typeno, b.typename ");
//	  		de.addSql(" from odssu.org_type b, odssu.role_orgtype c ");
//	  		de.addSql(" where b.typeno = c.orgtypeno ");
//	  		de.addSql(" and c.roleno = :roleno ");
//	  		de.setString("roleno", ROLENO);
//			DataStore typeds = de.query();
//			int typecount = typeds.rowCount();
//			if(typecount >= 1) {
//				for(int j = 0; j < typecount; j++) {
//					if(j == 0) {
//						typename = typeds.getString(j, "typename");
//					}else {
//						typename = typename + "," + typeds.getString(j, "typename");
//					}
//				}
//			}
//			roleds.put(i,"typename", typename);
//		}
//		
//		vdo.put("roleds", roleds);
//		vdo.put("jscount", rolecount+"");
//		vdo.put("pfolderid", pfolderid);
//		vdo.put("folderlabel", folderlabel);
//		vdo.put("deforgno", deforgno);
//		return vdo;
		return null;
	}
	
	public DataObject getRoleOIPB(DataObject para) throws Exception{
		DE de = DE.getInstance();
		DataObject vdo = DataObject.getInstance();
		String roleno = para.getString("roleno");

		//流程任务数
		de.clearSql();
		de.addSql(" SELECT a.roleid, a.pdid, a.dptdid, b.pdlabel, c.dptdlabel ");
		de.addSql(" FROM bpzone.dutyposition_task_role a, bpzone.process_define b, ");
		de.addSql(" 	bpzone.dutyposition_task c, bpzone.syscode d ");
		de.addSql(" WHERE a.PDID = b.PDID AND a.PDID = c.PDID AND a.DPTDID = c.DPTDID ");
		de.addSql(" AND a.TOCCODE = d.CODE AND c.TOCDMBH = d.NAME  ");
		de.addSql(" AND a.ROLEID = :roleno ");
		de.addSql(" ORDER BY a.ROLEID, a.PDID, a.DPTDID ");
		de.setString("roleno", roleno);
		DataStore lcrwds = de.query();
		int lcrwcount = lcrwds.rowCount();
		//功能任务数
		de.clearSql();
		de.addSql(" select a.roleno, a.functionid, b.functionname ");
		de.addSql(" from odssu.role_function_manual a, odssu.appfunction b ");
		de.addSql(" where a.FUNCTIONID = b.FUNCTIONID ");
		de.addSql(" and a.ROLENO = :roleno ");
		de.setString("roleno", roleno);
		DataStore gnrwds = de.query();
		int gnrwcount = gnrwds.rowCount();
		//操作员数
		de.clearSql();
		de.addSql(" select DISTINCT a.empno, b.loginname, b.empname ");
		de.addSql(" from odssu.ir_emp_org_all_role a, odssu.empinfor b ");
		de.addSql(" where a.EMPNO = b.EMPNO and b.sleepflag='0' ");
		de.addSql(" and a.ROLENO = :roleno ");
		de.setString("roleno", roleno);
		DataStore empds = de.query();
		int empcount = empds.rowCount();
		for(int i = 0; i < empcount; i++) {
			String ORGNO = "", ORGNAME = "";
			String empno = empds.getString(i, "empno");
			de.clearSql();
			de.addSql(" select a.empno, a.orgno, c.orgname ");
			de.addSql(" from odssu.ir_emp_org_all_role a, odssu.orginfor c ");
			de.addSql(" where a.ORGNO = c.orgno ");
			de.addSql(" and a.ROLENO = :roleno and a.EMPNO = :empno ");
			de.setString("roleno", roleno);
			de.setString("empno", empno);
			DataStore jgds = de.query();
			int jgcount = jgds.rowCount();
			if(jgcount >= 1) {
				for(int j = 0; j < jgcount; j++) {
					if(j == 0) {
						ORGNO = jgds.getString(j, "orgno");
						ORGNAME = jgds.getString(j, "orgname");
					}else {
						ORGNO = ORGNO + "," + jgds.getString(j, "orgno");
						ORGNAME = ORGNAME + "," + jgds.getString(j, "orgname");
					}
				}
			}
			empds.put(i,"orano", ORGNO);
			empds.put(i,"orgname", ORGNAME);
		}

  		de.clearSql();
  		de.addSql(" select roleno, rolename, deforgno ");
  		de.addSql(" from odssu.roleinfor ");
  		de.addSql(" where roleno = :roleid ");
  		de.addSql(" and sleepflag = '0' ");
		de.setString("roleid", roleno);
		DataStore roleds = de.query();
		String rolename = "", deforgno="";
		if(roleds.rowCount() > 0) {
			rolename = roleds.getString(0, "rolename");
			deforgno = roleds.getString(0, "deforgno");
		}
		String typename = "";
  		de.clearSql();
  		de.addSql(" select b.TYPENO, b.typename ");
  		de.addSql(" from odssu.org_type b, odssu.role_orgtype c ");
  		de.addSql(" where b.TYPENO = c.orgtypeno ");
  		de.addSql(" and c.roleno = :roleid ");
  		de.setString("roleid", roleno);
		DataStore typeds = de.query();
		int typecount = typeds.rowCount();
		if(typecount >= 1) {
			for(int j = 0; j < typecount; j++) {
				if(j == 0) {
					typename = typeds.getString(j, "typename");
				}else {
					typename = typename + "," + typeds.getString(j, "typename");
				}
			}
		}

		vdo.put("gnrwds", gnrwds);
		vdo.put("lcrwds", lcrwds);
		vdo.put("empds", empds);
		vdo.put("gnrwcount", gnrwcount + "");
		vdo.put("lcrwcount", lcrwcount + "");
		vdo.put("empcount", empcount + "");
		vdo.put("rolename", rolename);
		vdo.put("typename", typename);
		vdo.put("deforgno", deforgno);
		return vdo;
	}
	
	public String queryCs(String folderid) throws Exception{
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select pfolderid ");
  		de.addSql(" from odssu.role_folder ");
  		de.addSql(" where folderid = :folderid ");
  		de.setString("folderid", folderid);
		DataStore folderds = de.query();
		String pfolderid = "";
		if(folderds.rowCount() > 0) {
			pfolderid = folderds.getString(0, "pfolderid");
		}
		return pfolderid;
	}
	public int queryGs(String folderid) throws Exception{
		DE de = DE.getInstance();
  		de.clearSql();
  		de.addSql(" select count(folderid) gs ");
  		de.addSql(" from odssu.role_folder ");
  		de.addSql(" where pfolderid = :pfolderid ");
  		de.setString("pfolderid", folderid);
		DataStore folderds = de.query();
		int gs = 0;
		if(folderds.rowCount() > 0) {
			gs = folderds.getInt(0, "gs");
		}
		return gs;
	}
	//转换跳转模式
	public DataObject roleAdjustFolderMode(DataObject para) throws AppException,BusinessException{
		String userid = this.getUser().getUserid();
		de.clearSql();
		de.addSql(" select 1  ");
		de.addSql("   from odssu.emp_personalization a");
		de.addSql("  where empno = :userid");
		de.setString("userid", userid);
		DataStore flag = de.query();
		if(flag.rowCount()== 0) {
			de.clearSql();
			de.addSql(" insert into odssu.emp_personalization");
			de.addSql(" (empno,adjustfoldermode)");
			de.addSql(" VALUES(:userid,1-adjustfoldermode)");
			de.setString("userid", userid);
			de.update();
			return null;
		}else {
			de.clearSql();
			de.addSql(" update odssu.emp_personalization");
			de.addSql("    set adjustfoldermode = 1-adjustfoldermode");
			de.addSql("  where empno = :userid");
			de.setString("userid", userid);
			de.update();
			return null;
		}
	}

	
	/**
	 * 方法简介：查询页面是否跳转信息
	 */
	public DataObject roleQueryAdjustFolderMode(DataObject para) throws AppException,BusinessException{
		String userid = this.getUser().getUserid();
		DataObject result = DataObject.getInstance();
		
		de.clearSql();
		de.addSql(" select adjustfoldermode  ");
		de.addSql("   from odssu.emp_personalization a");
		de.addSql("  where empno = :userid");
		de.setString("userid", userid);
		DataStore mode=de.query();
		
		if(mode!= null &&mode.rowCount()>0) {
			result = mode.get(0);
		}else {
			result.put("adjustfoldermode", 0);
		}
		return result;

	}
	/**
	 * 方法简介：查询是否有跳转目录的权限
	 */
	public DataObject roleCheckUserPermission(DataObject para) throws AppException{
		DataObject result = DataObject.getInstance();
		String userid = this.getUser().getUserid();
		de.clearSql();
		de.addSql(" select 1 ");
		de.addSql("   from odssu.emp_rolefolder a ");
		de.addSql("  where a.empno = :empno ");
		de.setString("empno", userid);
		DataStore qxds = de.query();
		if(qxds!=null &&qxds.rowCount() > 0) {
			result.put("flag", "true");
		}else {
			result.put("flag", "false");
		}
		return result;
	}
	

}
