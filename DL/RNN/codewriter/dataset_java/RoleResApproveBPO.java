package com.dw.role.addrole.addroleapprove;

import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class RoleResApproveBPO extends BPO{

	public DataObject getRoleResFolder(DataObject para) throws AppException {
		
		String folderid = para.getString("folderid");
		String piid = para.getString("piid");
		
		de.clearSql();
		de.addSql(" select a.pdid||'.'||a.dpid resourceid, a.pdlabel||'.'||a.dptdlabel resourcename ,'流程' zylx ");
		de.addSql(" from odssuws.duty_process_dp a ");
		de.addSql(" where a.piid = :piid ");
		de.addSql(" and a.pdid in (select m.pdid from odssu.pd_resfolder m where m.folderid = :folderid )");
		de.addSql(" order by a.pdid, a.dpid ");
		de.setString("piid", piid);
		de.setString("folderid", folderid);
		DataStore lcrwnocodeds = de.query();
		
		de.clearSql();
		de.addSql(" select distinct e.functionid resourceid,e.functionname resourcename,'功能' zylx ");
		de.addSql("   from odssuws.duty_function d, ");
		de.addSql("		   odssu.appfunction e ");
		de.addSql("  where d.piid = :piid ");
		de.addSql("	   and d.functionid = e.functionid ");
		de.addSql("    and d.functionid in (select m.fnid from odssu.fn_resfolder m where m.folderid = :folderid )");
		de.addSql("	 order by e.functionid ");
		de.setString("piid", piid);
		de.setString("folderid", folderid);
		DataStore gnrwds = de.query();
		
		lcrwnocodeds.combineDatastore(gnrwds);
		
		DataObject result = DataObject.getInstance();
		result .put("resourceds", lcrwnocodeds);
		
		return result;
	}
	public DataObject addRoleAllResource(DataObject para) throws AppException {
		
		String label = para.getString("label");
		String piid = para.getString("piid");
		
		de.clearSql();
		de.addSql(" select a.pdid||'.'||a.dpid resourceid, a.pdlabel||'.'||a.dptdlabel resourcename ,'流程' zylx ");
		de.addSql(" from odssuws.duty_process_dp a ");
		de.addSql(" where a.piid = :piid ");
		de.addSql(" order by a.pdid, a.dpid ");
		de.setString("piid", piid);
		DataStore lcrwnocodeds = de.query();
		
		de.clearSql();
		de.addSql(" select distinct e.functionid resourceid,e.functionname resourcename,'功能' zylx ");
		de.addSql("   from odssuws.duty_function d, ");
		de.addSql("		   odssu.appfunction e ");
		de.addSql("  where d.piid = :piid ");
		de.addSql("	   and d.functionid = e.functionid ");
		de.addSql("	 order by e.functionid ");
		de.setString("piid", piid);
		DataStore gnrwds = de.query();
		
		lcrwnocodeds.combineDatastore(gnrwds);
		if(label != null&& !"".equals(label) ) {
			lcrwnocodeds = lcrwnocodeds.cloneFulfill("resourceid like "+label+" or resourcename like "+label);
		}
		
		
		DataObject result = DataObject.getInstance();
		result .put("resourceds", lcrwnocodeds);
		
		return result;
	}
	public DataObject modRoleAllResource(DataObject para) throws AppException {
		
		String piid = para.getString("piid");
		String label = para.getString("label");
		
		de.clearSql();
		de.addSql(" select a.pdid||'.'||a.dptdid resourceid , a.pdlabel||'.'||a.dptdlabel resourcename , '流程' zylx , b.differ ");
		de.addSql("   from odssuws.duty_all_process_dp a ,odssuws.differ_duty_lcrw b ");
		de.addSql("  where a.piid = :piid ");
		de.addSql("    and a.piid = b.piid ");
		de.addSql("    and a.pdid = b.pdid ");
		de.addSql("    and a.dptdid = b.dpid ");
		de.addSql("    and a.toccode = b.toccode ");
		de.setString("piid", piid);
		DataStore modpdds = de.query();
		
		de.clearSql();
		de.addSql(" select a.pdid||'.'||a.dpid resourceid, a.pdlabel||'.'||a.dptdlabel resourcename ,'流程' zylx , '2' differ ");
		de.addSql(" from odssuws.duty_process_dp a ");
		de.addSql(" where a.piid = :piid ");
		de.addSql(" and not exists ( select 1 from odssuws.differ_duty_lcrw b  ");
		de.addSql("          where b.piid = :piid and a.pdid = b.pdid and a.dpid = b.dpid and a.toccode = b.toccode ) ");
		de.setString("piid", piid);
		DataStore hispdds = de.query();
		
		de.clearSql();
		de.addSql(" select e.functionid resourceid,e.functionname resourcename,'功能' zylx ,a.differ ");
		de.addSql("  from odssuws.differ_duty_gnrw a ,odssu.appfunction e ");
		de.addSql(" where a.piid = :piid ");
		de.addSql("   and a.functionid = e.functionid ");
		de.setString("piid", piid);
		DataStore modfnds = de.query();
		
		de.clearSql();
		de.addSql(" select distinct e.functionid resourceid,e.functionname resourcename,'功能' zylx , '2' differ ");
		de.addSql("   from odssuws.duty_function d, ");
		de.addSql("		   odssu.appfunction e ");
		de.addSql("  where d.piid = :piid ");
		de.addSql("	   and d.functionid = e.functionid ");
		de.addSql("    and d.functionid not in ( select n.functionid from odssuws.differ_duty_gnrw n where n.piid = :piid  )");
		de.addSql("	 order by e.functionid ");
		de.setString("piid", piid);
		DataStore hisfnds = de.query();
		
		modpdds.combineDatastore(hispdds);
		modpdds.combineDatastore(modfnds);
		modpdds.combineDatastore(hisfnds);
		
		if(label != null&& !"".equals(label) ) {
			modpdds = modpdds.cloneFulfill("resourceid like "+label+" or resourcename like "+label);
		}
		modpdds.sort("differ");
		
		DataObject result = DataObject.getInstance();
		result .put("resourceds", modpdds);
		
		return result;
	}
	public DataObject getRoleResWFP(DataObject para) throws AppException {
		
		String piid = para.getString("piid");
		
		de.clearSql();
		de.addSql(" select a.pdid||'.'||a.dpid resourceid, a.pdlabel||'.'||a.dptdlabel resourcename ,'流程' zylx ");
		de.addSql(" from odssuws.duty_process_dp a ");
		de.addSql(" where a.piid = :piid ");
		de.addSql(" and not exists (select 1 from odssu.pd_resfolder m where m.pdid = a.pdid )");
		de.addSql(" order by a.pdid, a.dpid ");
		de.setString("piid", piid);
		DataStore lcrwnocodeds = de.query();
		
		de.clearSql();
		de.addSql(" select distinct e.functionid resourceid,e.functionname resourcename,'功能' zylx ");
		de.addSql("   from odssuws.duty_function d, ");
		de.addSql("		   odssu.appfunction e ");
		de.addSql("  where d.piid = :piid ");
		de.addSql("	   and d.functionid = e.functionid ");
		de.addSql("    and not exists (select 1 from odssu.fn_resfolder m where m.fnid = d.functionid )");
		de.addSql("	 order by e.functionid ");
		de.setString("piid", piid);
		DataStore gnrwds = de.query();
		
		lcrwnocodeds.combineDatastore(gnrwds);
		
		DataObject result = DataObject.getInstance();
		result .put("resourceds", lcrwnocodeds);
		
		return result;
	}
	
	public DataObject getRoleResStatistic(DataObject para) throws AppException {
		
		String piid = para.getString("piid");
		
		de.clearSql();
		de.addSql("select rolename from odssuws.roleadd where piid = :piid ");
		de.setString("piid", piid);
		DataStore rolenameds = de.query();
		
		String rolename = rolenameds.getString(0, "rolename");
		
		de.clearSql();
		de.addSql("select count(1) countdp  ");
		de.addSql("  from odssuws.duty_process_dp b ");
		de.addSql(" where b.piid = :piid  ");
		de.setString("piid", piid);
		DataStore roledpds = de.query();
		
		de.clearSql();
		de.addSql("select count(1) countfn ");
		de.addSql("  from odssuws.duty_function b ");
		de.addSql(" where b.piid = :piid ");
		de.setString("piid", piid);
		DataStore rolefnds = de.query();
		
		int dpcount = roledpds.getInt(0, "countdp");
		int fncount = rolefnds.getInt(0, "countfn");
		int allresource = dpcount+fncount;
		
		String label = "角色["+rolename+"]拥有"+allresource+"个资源，分别为"+dpcount+"个流程资源，"+fncount+"个功能资源。";
		
		DataObject result = DataObject.getInstance();
		result.put("label", label);
		return result;
	}
	public DataObject queryRoleInfoForSP(DataObject para) throws AppException,BusinessException{
		String piid = para.getString("piid");
        DE de = DE.getInstance();
        de.clearSql();
        de.addSql("select a.rolename,a.appid ,b.appname "); 
        de.addSql("  from odssuws.roleadd a,odssu.appinfo b "); 
        de.addSql(" where a.piid = :piid "); 
        de.addSql("   and a.appid = b.appid  ");
        de.setString("piid", piid);
        DataStore rolenameds = de.query();
        if (rolenameds == null || rolenameds.rowCount() <= 0) {
            throw new AppException("未查询到业务流水号为["+piid+"]的工单信息！");
        }
          
        de.clearSql();
        de.addSql("select c.typename orgtype "); 
        de.addSql("  from odssuws.roleadd_orgtype b, odssu.org_type c "); 
        de.addSql(" where b.orgtypeno = c.typeno "); 
        de.addSql("   and b.piid = :piid "); 
        de.setString("piid", piid);
        DataStore roletypeds = de.query();
        if (roletypeds == null || roletypeds.rowCount() <= 0) {
            throw new AppException("未查询到编号为["+piid+"]的新增角色工单信息！");
        }

        StringBuilder typename = new StringBuilder();
        
        for (int i = 0; i < roletypeds.rowCount(); i++) {
        	if(i == roletypeds.rowCount()-1) {
        		typename.append(roletypeds.getString(i, "orgtype"));
        	}else {
        		typename.append(roletypeds.getString(i, "orgtype") + ",");
			}
		}
        
        
        DataObject orgtypes = DataObject.getInstance();
        orgtypes.put("orgtype", typename.toString());
        
        roletypeds.clear();
		roletypeds.addRow(orgtypes);
        
        DataObject vdo = DataObject.getInstance();
        vdo.put("rolenameds", rolenameds);
        vdo.put("orgtypeds", roletypeds);
        return vdo;
	}
	public DataObject getModRoleResFolder(DataObject para) throws AppException {
		
		String folderid = para.getString("folderid");
		String piid = para.getString("piid");
		
		de.clearSql();
		de.addSql(" select a.pdid||'.'||a.dptdid resourceid , a.pdlabel||'.'||a.dptdlabel resourcename , '流程' zylx , b.differ ");
		de.addSql("   from odssuws.duty_all_process_dp a ,odssuws.differ_duty_lcrw b ");
		de.addSql("  where a.piid = :piid ");
		de.addSql("    and a.piid = b.piid ");
		de.addSql("    and a.pdid = b.pdid ");
		de.addSql("    and a.dptdid = b.dpid ");
		de.addSql("    and a.toccode = b.toccode ");
		de.addSql("    and a.pdid in ( select m.pdid from odssu.pd_resfolder m where m.folderid = :folderid ) ");
		de.setString("piid", piid);
		de.setString("folderid", folderid);
		DataStore modpdds = de.query();
		
		de.clearSql();
		de.addSql(" select a.pdid||'.'||a.dpid resourceid, a.pdlabel||'.'||a.dptdlabel resourcename ,'流程' zylx , '2' differ ");
		de.addSql(" from odssuws.duty_process_dp a ");
		de.addSql(" where a.piid = :piid ");
		de.addSql(" and a.pdid in (select m.pdid from odssu.pd_resfolder m where m.folderid = :folderid )");
		de.addSql(" and not exists ( select 1 from odssuws.differ_duty_lcrw b  ");
		de.addSql("          where b.piid = :piid and a.pdid = b.pdid and a.dpid = b.dpid and a.toccode = b.toccode ) ");
		de.setString("piid", piid);
		de.setString("folderid", folderid);
		DataStore hispdds = de.query();
		
		de.clearSql();
		de.addSql(" select e.functionid resourceid,e.functionname resourcename,'功能' zylx ,a.differ ");
		de.addSql("  from odssuws.differ_duty_gnrw a ,odssu.appfunction e ");
		de.addSql(" where a.piid = :piid ");
		de.addSql("   and a.functionid = e.functionid ");
		de.addSql("   and a.functionid in ( select b.fnid from odssu.fn_resfolder b where b.folderid = :folderid )");
		de.setString("piid", piid);
		de.setString("folderid", folderid);
		DataStore modfnds = de.query();
		
		de.clearSql();
		de.addSql(" select distinct e.functionid resourceid,e.functionname resourcename,'功能' zylx , '2' differ ");
		de.addSql("   from odssuws.duty_function d, ");
		de.addSql("		   odssu.appfunction e ");
		de.addSql("  where d.piid = :piid ");
		de.addSql("	   and d.functionid = e.functionid ");
		de.addSql("    and d.functionid in (select m.fnid from odssu.fn_resfolder m where m.folderid = :folderid )");
		de.addSql("    and d.functionid not in ( select n.functionid from odssuws.differ_duty_gnrw n where n.piid = :piid  )");
		de.addSql("	 order by e.functionid ");
		de.setString("piid", piid);
		de.setString("folderid", folderid);
		DataStore hisfnds = de.query();
		
		modpdds.combineDatastore(hispdds);
		modpdds.combineDatastore(modfnds);
		modpdds.combineDatastore(hisfnds);
		
		DataObject result = DataObject.getInstance();
		result .put("resourceds", modpdds);
		
		return result;
	}
	public DataObject getModRoleResWFP(DataObject para) throws AppException {
		
		String piid = para.getString("piid");
		
		de.clearSql();
		de.addSql(" select a.pdid||'.'||a.dptdid resourceid , a.pdlabel||'.'||a.dptdlabel resourcename , '流程' zylx , b.differ ");
		de.addSql("   from odssuws.duty_all_process_dp a ,odssuws.differ_duty_lcrw b ");
		de.addSql("  where a.piid = :piid ");
		de.addSql("    and a.piid = b.piid ");
		de.addSql("    and a.pdid = b.pdid ");
		de.addSql("    and a.dptdid = b.dpid ");
		de.addSql("    and a.toccode = b.toccode ");
		de.addSql("    and a.pdid not in ( select m.pdid from odssu.pd_resfolder m ) ");
		de.setString("piid", piid);
		DataStore modpdds = de.query();
		
		de.clearSql();
		de.addSql(" select a.pdid||'.'||a.dpid resourceid, a.pdlabel||'.'||a.dptdlabel resourcename ,'流程' zylx , '2' differ ");
		de.addSql(" from odssuws.duty_process_dp a ");
		de.addSql(" where a.piid = :piid ");
		de.addSql(" and a.pdid not in (select m.pdid from odssu.pd_resfolder m )");
		de.addSql(" and not exists ( select 1 from odssuws.differ_duty_lcrw b  ");
		de.addSql("          where b.piid = :piid and a.pdid = b.pdid and a.dpid = b.dpid and a.toccode = b.toccode ) ");
		de.setString("piid", piid);
		DataStore hispdds = de.query();
		
		de.clearSql();
		de.addSql(" select e.functionid resourceid,e.functionname resourcename,'功能' zylx ,a.differ ");
		de.addSql("  from odssuws.differ_duty_gnrw a ,odssu.appfunction e ");
		de.addSql(" where a.piid = :piid ");
		de.addSql("   and a.functionid = e.functionid ");
		de.addSql("   and a.functionid not in ( select b.fnid from odssu.fn_resfolder b )");
		de.setString("piid", piid);
		DataStore modfnds = de.query();
		
		de.clearSql();
		de.addSql(" select distinct e.functionid resourceid,e.functionname resourcename,'功能' zylx , '2' differ ");
		de.addSql("   from odssuws.duty_function d, ");
		de.addSql("		   odssu.appfunction e ");
		de.addSql("  where d.piid = :piid ");
		de.addSql("	   and d.functionid = e.functionid ");
		de.addSql("    and d.functionid not in (select m.fnid from odssu.fn_resfolder m )");
		de.addSql("    and d.functionid not in ( select n.functionid from odssuws.differ_duty_gnrw n where n.piid = :piid  )");
		de.addSql("	 order by e.functionid ");
		de.setString("piid", piid);
		DataStore hisfnds = de.query();
		
		modpdds.combineDatastore(hispdds);
		modpdds.combineDatastore(modfnds);
		modpdds.combineDatastore(hisfnds);
		
		DataObject result = DataObject.getInstance();
		result .put("resourceds", modpdds);
		
		return result;
	}
}
