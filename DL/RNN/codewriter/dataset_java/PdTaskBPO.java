package com.dw.res.pdtask;

import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class PdTaskBPO extends BPO{
	
	public DataObject queryPD(DataObject para) throws Exception{
		
		String key = para.getString("key");
		String folderno = para.getString("folderno");
		if("".equals(folderno)) {
			folderno = "%";
		}
		if("*根据业务流程、流程任务模糊查询".equals(key)||"".equals(key)) {
			key = "%";
		}
		
		de.clearSql();
		de.addSql("select t.pdid,t.pdalias,a.dptdid, a.dptdlabel dplabel,'-' tocdmbh,   '-' toccode ");
		de.addSql("  from bpzone.process_define t,bpzone.dutyposition_task a ");
		de.addSql(" where t.CLASSIFICATIONBH like :folderno ");
		de.addSql("   and a.pdid = t.pdid ");
		de.addSql("   and a.tocdmbh='-' ");
		de.addSql("   and t.status <> '2' ");
		de.addSql("   and (lower(t.pdid) like :key  or lower(t.pdalias) like :key  ");
		de.addSql("    or lower(a.dptdid) like :key or lower(a.dptdlabel) like :key )");
		de.addSql(" order by t.pdid ");
		de.setString("folderno", folderno);
		de.setString("key", "%"+key.toLowerCase()+"%");
		DataStore vdsnotoc = de.query();
		
		de.clearSql();
		de.addSql("select t.pdid,t.pdalias,a.dptdid, a.dptdlabel || '(' || b.name ||  ')'  dplabel,a.tocdmbh ,b.name tocname ");
		de.addSql("  from bpzone.process_define t,bpzone.dutyposition_task a , bpzone.syscode b ");
		de.addSql(" where t.CLASSIFICATIONBH like :folderno ");
		de.addSql("   and t.status <> '2' ");
		de.addSql("   and a.tocdmbh <> '-' ");
		de.addSql("   and a.tocdmbh = b.code ");
		de.addSql("   and a.pdid = t.pdid ");
		de.addSql("   and (lower(t.pdid) like :key  or lower(t.pdalias) like :key  ");
		de.addSql("    or lower(a.dptdid) like :key or lower(a.dptdlabel) like :key )");
		de.addSql(" order by t.pdid ");
		de.setString("folderno", folderno);
		de.setString("key", "%"+key.toLowerCase()+"%");
		DataStore vdstoc = de.query();
		
		DataObject vdo = DataObject.getInstance();
		vdsnotoc.combineDatastore(vdstoc);
		
		vdo.put("pdlist", vdsnotoc);
		return vdo;
		
	}
	
	/**************************PD查询分页*********************************************/
	public DataObject pdQueryGetRowCount(DataObject para) throws Exception {
		String key = para.getString("key");
		String folderno = para.getString("folderno");
		if("".equals(folderno)) {
			folderno = "%";
		}
		if("*根据业务流程、流程任务模糊查询".equals(key)||"".equals(key)) {
			key = "%";
		}
		
		de.clearSql();
		de.addSql("select  count(*) row_count ");
		de.addSql("from  ( select DISTINCT c.pdid,c.pdalias,c.dptdid, c.dplabel,c.tocdmbh,c.toccode, c.appid, c.appname from ");
		de.addSql(" (select t.pdid,t.pdalias,a.dptdid, a.dptdlabel dplabel,'-' tocdmbh,   '-' toccode, m.WSO_APPID appid,  ");
		de.addSql(" 	(select n.appname from odssu.appinfo n where m.WSO_APPID = n.appid ) appname ");
		de.addSql("  from bpzone.process_define t,bpzone.dutyposition_task a,   ");
		de.addSql("  	  bpzone.process_define_in_activiti m  ");
		de.addSql(" where t.CLASSIFICATIONBH like :folderno ");
		de.addSql("   and a.pdid = t.pdid  ");
		de.addSql("   and m.pdid = a.pdid  ");
 		de.addSql("  and a.tocdmbh='-'  ");
 		de.addSql("  and (lower(t.pdid) like :key  or lower(t.pdalias) like :key   ");
 		de.addSql("   or lower(a.dptdid) like :key or lower(a.dptdlabel) like :key ) ");
		de.addSql(" UNION ");
		de.addSql(" select tt.pdid,tt.pdalias,aa.dptdid, aa.dptdlabel || '(' || bb.name ||  ')'  dplabel,aa.tocdmbh ,bb.name tocname,   ");
		de.addSql(" 	   mm.WSO_APPID appid,  ");
		de.addSql(" 	(select nn.appname from odssu.appinfo nn where mm.WSO_APPID = nn.appid ) appname ");
		de.addSql("  from bpzone.process_define tt,bpzone.dutyposition_task aa , bpzone.syscode bb,  ");
		de.addSql("  	  bpzone.process_define_in_activiti mm ");
		de.addSql(" where tt.CLASSIFICATIONBH like :folderno  ");
		de.addSql("   and aa.tocdmbh <> '-'  ");
		de.addSql("   and aa.tocdmbh = bb.code  ");
 		de.addSql("  and aa.pdid = tt.pdid  ");
		de.addSql("   and mm.pdid = aa.pdid  ");
 		de.addSql("  and (lower(tt.pdid) like :key  or lower(tt.pdalias) like :key  ");
		de.addSql("    or lower(aa.dptdid) like :key or lower(aa.dptdlabel) like :key ) ) c ");
		de.addSql(" order by c.pdid ) row");
		de.setString("folderno", folderno);
		de.setString("key", "%"+key.toLowerCase()+"%");
		DataStore vds = de.query();
		return vds.getRow(0);
	}

	public DataObject pdQueryGetPageRows(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		int g_startRowNumber = para.getInt("g_startRowNumber");
		int g_endRowNumber = para.getInt("g_endRowNumber");	
		String key = para.getString("key");
		String folderno = para.getString("folderno");
		if("".equals(folderno)) {
			folderno = "%";
		}
		if("*根据业务流程、流程任务模糊查询".equals(key)||"".equals(key)) {
			key = "%";
		}
		
		de.clearSql();
		de.addSql(" select DISTINCT c.pdid,c.pdalias,c.dptdid, c.dplabel,c.tocdmbh,c.toccode, c.appid, c.appname from ");
		de.addSql(" (select t.pdid,t.pdalias,a.dptdid, a.dptdlabel dplabel,'-' tocdmbh,   '-' toccode, m.WSO_APPID appid,  ");
		de.addSql(" 	(select n.appname from odssu.appinfo n where m.WSO_APPID = n.appid ) appname ");
		de.addSql("  from bpzone.process_define t,bpzone.dutyposition_task a,   ");
		de.addSql("  	  bpzone.process_define_in_activiti m  ");
		de.addSql(" where t.CLASSIFICATIONBH like :folderno ");
		de.addSql("   and a.pdid = t.pdid  ");
		de.addSql("   and m.pdid = a.pdid  ");
 		de.addSql("  and a.tocdmbh='-'  ");
 		de.addSql("  and (lower(t.pdid) like :key  or lower(t.pdalias) like :key   ");
 		de.addSql("   or lower(a.dptdid) like :key or lower(a.dptdlabel) like :key ) ");
		de.addSql(" UNION ");
		de.addSql(" select tt.pdid,tt.pdalias,aa.dptdid, aa.dptdlabel || '(' || bb.name ||  ')'  dplabel,aa.tocdmbh ,bb.name tocname,   ");
		de.addSql(" 	   mm.WSO_APPID appid,  ");
		de.addSql(" 	(select nn.appname from odssu.appinfo nn where mm.WSO_APPID = nn.appid ) appname ");
		de.addSql("  from bpzone.process_define tt,bpzone.dutyposition_task aa , bpzone.syscode bb,  ");
		de.addSql("  	  bpzone.process_define_in_activiti mm  ");
		de.addSql(" where tt.CLASSIFICATIONBH like :folderno  ");
		de.addSql("   and aa.tocdmbh <> '-'  ");
		de.addSql("   and aa.tocdmbh = bb.code  ");
 		de.addSql("  and aa.pdid = tt.pdid  ");
		de.addSql("   and mm.pdid = aa.pdid  ");
 		de.addSql("  and (lower(tt.pdid) like :key  or lower(tt.pdalias) like :key  ");
		de.addSql("    or lower(aa.dptdid) like :key or lower(aa.dptdlabel) like :key ) ) c ");
		de.addSql(" order by c.pdid ");
		de.setString("folderno", folderno);
		de.setString("key", "%"+key.toLowerCase()+"%");
		DataStore vds = de.query();
		if (vds.isEmpty()) {
			vdo.put("vds", null);
		} else {
			DataStore subvds = vds.subDataStore(g_startRowNumber - 1, g_endRowNumber);
			String sqlStr, sqlStr1;
			sqlStr = "and ( a.pdid = '" + subvds.getString(0, "pdid")+ "'";
			// 将任务功能进行切块
			for (int i = 1; i < subvds.rowCount() - 1; i++) {
				String pdid = subvds.getString(i, "pdid");
				sqlStr += "    or  a.pdid = '" + pdid  + "'";
			}
			sqlStr += "  or a.pdid = '" + subvds.getString(subvds.rowCount() - 1, "pdid") + "')";
			 
			sqlStr1 = "and ( aa.pdid = '" + subvds.getString(0, "pdid")+ "'";
			// 将任务功能进行切块
			for (int i = 1; i < subvds.rowCount() - 1; i++) {
				String pdid = subvds.getString(i, "pdid");
				sqlStr1 += "    or  aa.pdid = '" + pdid  + "'";
			}
			sqlStr1 += "  or aa.pdid = '" + subvds.getString(subvds.rowCount() - 1, "pdid") + "')";
			// 获取全部的任务功能以及机构，角色信息
			de.clearSql();
			de.addSql(" select DISTINCT c.pdid,c.pdalias,c.dptdid, c.dplabel,c.tocdmbh,c.toccode, c.appid, c.appname from ");
			de.addSql(" (select t.pdid,t.pdalias,a.dptdid, a.dptdlabel dplabel,'-' tocdmbh,   '-' toccode, m.WSO_APPID appid,  ");
			de.addSql(" 	(select n.appname from odssu.appinfo n where m.WSO_APPID = n.appid ) appname ");
			de.addSql("  from bpzone.process_define t,bpzone.dutyposition_task a,   ");
			de.addSql("  	  bpzone.process_define_in_activiti m  ");
			de.addSql(" where t.CLASSIFICATIONBH like :folderno ");
			de.addSql("   and a.pdid = t.pdid  ");
			de.addSql("   and m.pdid = a.pdid  ");
	 		de.addSql("  and a.tocdmbh='-'  ");
			de.addSql(sqlStr);
	 		de.addSql("  and (lower(t.pdid) like :key  or lower(t.pdalias) like :key   ");
	 		de.addSql("   or lower(a.dptdid) like :key or lower(a.dptdlabel) like :key ) ");
			de.addSql(" UNION ");
			de.addSql(" select tt.pdid,tt.pdalias,aa.dptdid, aa.dptdlabel || '(' || bb.name ||  ')'  dplabel,aa.tocdmbh ,bb.name tocname,   ");
			de.addSql(" 	   mm.WSO_APPID appid,  ");
			de.addSql(" 	(select nn.appname from odssu.appinfo nn where mm.WSO_APPID = nn.appid ) appname ");
			de.addSql("  from bpzone.process_define tt,bpzone.dutyposition_task aa , bpzone.syscode bb,  ");
			de.addSql("  	  bpzone.process_define_in_activiti mm  ");
			de.addSql(" where tt.CLASSIFICATIONBH like :folderno  ");
			de.addSql("   and aa.tocdmbh <> '-'  ");
			de.addSql("   and aa.tocdmbh = bb.code  ");
	 		de.addSql("  and aa.pdid = tt.pdid  ");
			de.addSql("   and mm.pdid = aa.pdid  ");
			de.addSql(sqlStr1);
	 		de.addSql("  and (lower(tt.pdid) like :key  or lower(tt.pdalias) like :key  ");
			de.addSql("    or lower(aa.dptdid) like :key or lower(aa.dptdlabel) like :key ) ) c ");
			de.addSql(" order by c.pdid ");
			de.setString("folderno", folderno);
			de.setString("key", "%"+key.toLowerCase()+"%");
			DataStore vds1 = de.query();
			vdo.put("vds", vds1);
		}
		return vdo;
	}

	public DataObject pdQueryGetAllRows(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String key = para.getString("key");
		String folderno = para.getString("folderno");
		if("".equals(folderno)) {
			folderno = "%";
		}
		if("*根据业务流程、流程任务模糊查询".equals(key)||"".equals(key)) {
			key = "%";
		}
		
		de.clearSql();
		de.addSql(" select DISTINCT c.pdid,c.pdalias,c.dptdid, c.dplabel,c.tocdmbh,c.toccode, c.appid, c.appname from ");
		de.addSql(" (select t.pdid,t.pdalias,a.dptdid, a.dptdlabel dplabel,'-' tocdmbh,   '-' toccode, m.WSO_APPID appid,  ");
		de.addSql(" 	(select n.appname from odssu.appinfo n where m.WSO_APPID = n.appid ) appname ");
		de.addSql("  from bpzone.process_define t,bpzone.dutyposition_task a,   ");
		de.addSql("  	  bpzone.process_define_in_activiti m  ");
		de.addSql(" where t.CLASSIFICATIONBH like :folderno ");
		de.addSql("   and a.pdid = t.pdid  ");
		de.addSql("   and m.pdid = a.pdid  ");
 		de.addSql("  and a.tocdmbh='-'  ");
 		de.addSql("  and (lower(t.pdid) like :key  or lower(t.pdalias) like :key   ");
 		de.addSql("   or lower(a.dptdid) like :key or lower(a.dptdlabel) like :key ) ");
		de.addSql(" UNION ");
		de.addSql(" select tt.pdid,tt.pdalias,aa.dptdid, aa.dptdlabel || '(' || bb.name ||  ')'  dplabel,aa.tocdmbh ,bb.name tocname,   ");
		de.addSql(" 	   mm.WSO_APPID appid,  ");
		de.addSql(" 	(select nn.appname from odssu.appinfo nn where mm.WSO_APPID = nn.appid ) appname ");
		de.addSql("  from bpzone.process_define tt,bpzone.dutyposition_task aa , bpzone.syscode bb,  ");
		de.addSql("  	  bpzone.process_define_in_activiti mm  ");
		de.addSql(" where tt.CLASSIFICATIONBH like :folderno  ");
		de.addSql("   and aa.tocdmbh <> '-'  ");
		de.addSql("   and aa.tocdmbh = bb.code  ");
 		de.addSql("  and aa.pdid = tt.pdid  ");
		de.addSql("   and mm.pdid = aa.pdid  ");
 		de.addSql("  and (lower(tt.pdid) like :key  or lower(tt.pdalias) like :key  ");
		de.addSql("    or lower(aa.dptdid) like :key or lower(aa.dptdlabel) like :key ) ) c ");
		de.addSql(" order by c.pdid ");
		de.setString("folderno", folderno);
		de.setString("key", "%"+key.toLowerCase()+"%");
		DataStore vds = de.query();
		
		vdo.put("pdlist", vds);
		return vdo;
	}
	
	
	
	public DataObject getResourceInfo(DataObject para) throws AppException{
		String pdid = para.getString("pdid");
		String dptdid = para.getString("dptdid");
		String toccode = para.getString("toccode");
		String tocdmbh = para.getString("tocdmbh");
		String strorgtypename = "";
		String isnjjs;
		
		de.clearSql();
		de.addSql(" select t.roleid ");
		de.addSql("   from bpzone.dutyposition_task_role t, ");
		de.addSql("        odssu.roleinfor r ");
		de.addSql("  where t.pdid=:pdid ");
		de.addSql("    and t.dptdid=:dptdid  ");
		de.addSql("    and t.toccode= :toccode ");
		de.addSql("    and t.roleid = r.roleno ");
		de.addSql("    and r.sleepflag = '0'   ");
		de.addSql("    and r.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("pdid", pdid);
		de.setString("dptdid", dptdid);
		de.setString("toccode", toccode);
		DataStore roleds = de.query();
		
		de.clearSql(); 
		de.addSql(" select distinct r.empno ");
		de.addSql("   from bpzone.dutyposition_task_role t, ");
		de.addSql("        odssu.ir_emp_org_all_role r ");
		de.addSql("  where t.pdid=:pdid ");
		de.addSql("    and t.dptdid=:dptdid  ");
		de.addSql("    and t.toccode= :toccode ");
		de.addSql("    and t.roleid = r.roleno ");
		de.addSql("    and exists (select 1 from odssu.roleinfor b where r.roleno = b.roleno and b.sleepflag = '0' )");
		de.addSql("    and exists(select 1 from odssu.empinfor a where a.empno = r.empno and a.sleepflag = '0') ");
		de.addSql("    and r.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("pdid", pdid);
		de.setString("dptdid", dptdid);
		de.setString("toccode", toccode);
		DataStore operatords = de.query();
		
		de.clearSql();
		de.addSql("select c.typename orgtypename,b.orgtypeno ");
		de.addSql("  from bpzone.dproletype a,odssu.ir_org_role_type b,odssu.org_type c ");
		de.addSql(" where a.roletypeid = b.roletypeno ");
		de.addSql("   and b.ORGTYPENO = c.TYPENO ");
		de.addSql("   and a.pdid = :pdid   ");
		de.addSql("   and a.dptdid=:dptdid  ");
		de.setString("pdid", pdid);
		de.setString("dptdid", dptdid);
		DataStore orgtypename = de.query();
		if(orgtypename !=null && orgtypename.rowCount() >0) {
			strorgtypename = orgtypename.getString(0,"orgtypename");
			for(int i=1;i<orgtypename.rowCount();i++) {
				strorgtypename = strorgtypename+"，"+orgtypename.getString(i,"orgtypename");
			}
		}
		de.clearSql();
		de.addSql("select 1 from bpzone.dproletype a where a.pdid = :pdid and a.dptdid = :dptdid ");
		de.setString("pdid", pdid);
		de.setString("dptdid", dptdid);
		DataStore dproletypeds = de.query();
		
		if(dproletypeds == null || dproletypeds.rowCount() == 0) {
			isnjjs = "true";
		}else {
			isnjjs = "false";
		}
		
		DataObject label = getProcessInfo(pdid,dptdid,tocdmbh);
		
		String appid = "", appname = "";
		de.clearSql();
		de.addSql("select a.WSO_APPID appid ");
		de.addSql("  from bpzone.process_define_in_activiti a ");
		de.addSql(" where a.pdid = :pdid   ");
		de.addSql("   and a.firstdptdid = :dptdid  ");
		de.setString("pdid", pdid);
		de.setString("dptdid", dptdid);
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
		vdo.put("pdalias", label.getString("pdalias"));
		vdo.put("dplabel", label.getString("dplabel"));
		vdo.put("isnjjs", isnjjs);
		vdo.put("appid", appid);
		vdo.put("appname", appname);
		return vdo;
	}
	public DataObject getProcessInfo(String pdid,String dptdid,String tocdmbh) throws AppException {
		
		DataStore vds = DataStore.getInstance();
		
		if("-".equals(tocdmbh)) {
		
			de.clearSql();
			de.addSql("select t.pdid,t.pdalias,a.dptdid, a.dptdlabel dplabel      ");
			de.addSql("  from bpzone.process_define t,bpzone.dutyposition_task a  ");
			de.addSql(" where a.PDID=:pdid       ");
			de.addSql("   and a.dptdid = :dptdid ");
			de.addSql("   and a.tocdmbh = '-'    ");
			de.addSql("   and t.pdid = a.pdid    ");
			de.setString("pdid", pdid);
			de.setString("dptdid", dptdid);
			vds =  de.query();
		}else {
			de.clearSql();
			de.addSql("select t.pdid,t.pdalias,a.dptdid, a.dptdlabel || '(' || b.name ||  ')'  dplabel ");
			de.addSql("  from bpzone.process_define t,bpzone.dutyposition_task a , bpzone.syscode b ");
			de.addSql(" where a.PDID=:pdid         ");
			de.addSql("   and a.dptdid = :dptdid   ");
			de.addSql("   and a.tocdmbh = :tocdmbh ");
			de.addSql("   and t.pdid = a.pdid      ");
			de.addSql("   and a.tocdmbh = b.code   ");
			de.setString("pdid", pdid);
			de.setString("dptdid", dptdid);
			de.setString("tocdmbh", tocdmbh);
			vds =  de.query();
		}

		if(vds == null || vds.rowCount() ==0) {
			throw new AppException("获取流程任务名称失败，业务流程任务编号："+pdid+"  流程任务编号："+dptdid+"  toc代码编号："+tocdmbh);
		}
		return vds.get(0);
	}
	public DataObject getProcessLabel(DataObject para) throws AppException {
		
		String pdid = para.getString("pdid");
		String dptdid = para.getString("dptdid");
		String tocdmbh = para.getString("tocdmbh");
		
		return getProcessInfo(pdid,dptdid,tocdmbh);
	}
	public DataObject getAllRole(DataObject para) throws AppException{
		
		String pdid = para.getString("pdid");
		String dptdid = para.getString("dptdid");
		String toccode = para.getString("toccode");
		
		de.clearSql();
		de.addSql(" select t.roleid ,r.rolename ");
		de.addSql("   from bpzone.dutyposition_task_role t , ");
		de.addSql("        odssu.roleinfor r  ");
		de.addSql("  where t.pdid=:pdid ");
		de.addSql("    and t.dptdid=:dptdid  ");
		de.addSql("    and t.toccode= :toccode ");
		de.addSql("    and t.roleid = r.roleno ");
		de.addSql("    and r.sleepflag = '0'   ");
		de.addSql("    and r.roleno not in (select n.roleno from odssu.njjs_filter n )");
		de.setString("pdid", pdid);
		de.setString("dptdid", dptdid);
		de.setString("toccode", toccode);
		DataStore roleds = de.query();
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("rolelist", roleds);
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
		de.addSql("    and b.sleepflag = '0'  ");
		de.addSql("    and a.roleno = :roleid ");
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
	/**
	 * 获取当前目录下的资源信息
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject getFolderInfo(DataObject para) throws AppException{
		
		DataObject vdo = DataObject.getInstance();
		String folderid = para.getString("folderno");
		
		String folderpath = getFolderPath(folderid);
		
		DataObject numdo = getPdNum(folderid);
		int pdnum = numdo.getInt("pdnum");
		int dpnum = numdo.getInt("dpnum");
		
		String result = "目录["+folderpath+"]下共有"+pdnum+"项业务流程，共有"+dpnum+"项流程任务";
		
		vdo.put("result", result);
		return vdo;
	}
	/**
	 * 查询当前目录的路径，当前为根目录时返回"";
	 * @param folderid
	 * @return
	 * @throws AppException
	 */
	public String getFolderPath(String folderid) throws AppException{
		
		String folderpath = "";
		DataStore vds = DataStore.getInstance();
		de.clearSql();
		de.addSql("select a.P_CLASSIFICATION_NO,a.CLASSIFICATION_LABEL ");
		de.addSql("  from bpzone.biz_classification a ");
		de.addSql(" where a.CLASSIFICATION_NO = :folderid ");
		de.setString("folderid", folderid);
		vds = de.query();
		if(vds == null || vds.rowCount() ==0) {
			throw new AppException("无效的CLASSIFICATION_NO："+folderid);
		}
		folderpath = vds.getString(0, "CLASSIFICATION_LABEL");
		folderid = vds.getString(0, "P_CLASSIFICATION_NO");
		while(true) {
			vds = DataStore.getInstance();
			de.clearSql();
			de.addSql("select a.P_CLASSIFICATION_NO,a.CLASSIFICATION_LABEL ");
			de.addSql("  from bpzone.biz_classification a ");
			de.addSql(" where a.CLASSIFICATION_NO = :folderid ");
			de.addSql("   and a.CLASSIFICATION_NO <> 'root' ");
			de.setString("folderid", folderid);
			vds = de.query();
			if (vds == null ||vds.rowCount() ==0) {
				return folderpath;
			}
			folderpath = vds.getString(0, "CLASSIFICATION_LABEL")+"→"+folderpath;
			folderid = vds.getString(0, "P_CLASSIFICATION_NO");
		}
	}
	public DataObject getPdNum (String folderid) throws AppException {
		
		int pdnum = 0;
		int dpnum = 0;
		de.clearSql();
		de.addSql("select a.CLASSIFICATION_NO,a.CLASSIFICATION_LABEL ");
		de.addSql("  from bpzone.biz_classification a ");
		de.addSql(" where a.P_CLASSIFICATION_NO = :folderid ");
		de.setString("folderid", folderid);
		DataStore vds = de.query();
		
		if(vds == null || vds.rowCount() == 0) {
			pdnum = pdnum+getCurrentPdNum(folderid);
			dpnum = dpnum+getCurrentDpNum(folderid);
		}else {
			for(int rownum = 0; rownum<vds.rowCount();rownum++) {
				DataObject temp = getPdNum(vds.getString(rownum, "CLASSIFICATION_NO"));
				pdnum = pdnum +temp.getInt("pdnum");
				dpnum = dpnum +temp.getInt("dpnum");
			}
		}
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("pdnum", pdnum);
		vdo.put("dpnum", dpnum);
		return vdo;
	}
	public int getCurrentPdNum(String folderid) throws AppException{
		de.clearSql();
		de.addSql("select t.pdid ");
		de.addSql("  from bpzone.process_define t ");
		de.addSql(" where t.CLASSIFICATIONBH = :folderid ");
		de.setString("folderid", folderid);
		DataStore vds = de.query();
		if(vds == null || vds.rowCount() ==0 ) {
			return 0;
		}else {
			return vds.rowCount();
		}
	}
	public int getCurrentDpNum(String folderid) throws AppException{
		de.clearSql();
		de.addSql("select distinct t.pdid,a.dptdid ");
		de.addSql("  from bpzone.process_define t,bpzone.dutyposition_task a ");
		de.addSql(" where t.CLASSIFICATIONBH like :folderid ");
		de.addSql("   and a.pdid = t.pdid ");
		de.setString("folderid", folderid);
		DataStore vds = de.query();
		if(vds == null || vds.rowCount() ==0 ) {
			return 0;
		}else {
			return vds.rowCount();
		}
	}

}
