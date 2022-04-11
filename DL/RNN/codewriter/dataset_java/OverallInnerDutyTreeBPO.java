package com.dw.hsuods.vap.org;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.hsuods.ws.org.util.FnDataSource;

public class OverallInnerDutyTreeBPO extends BPO{



	/**
	 * @描述：查询科室下岗位的有权办理人员
	 * @param para
	 * @return
	 * @throws Exception 2015-6-6
	 */
	public DataObject queryKsgwry(DataObject para) throws Exception {
		String orgtype = para.getString("orgtype");
		String pdid = para.getString("pdid");
		String dpid = para.getString("dpid");
		DataObject result = DataObject.getInstance();
		de.clearSql();
  		de.addSql(" select  b.roleno,b.rolename dutyname");
  		de.addSql("   from bpzone.dutyposition_task_role a,");
  		de.addSql("        odssu.roleinfor    b,");
  		de.addSql("        odssu.ir_org_role_type c");
  		de.addSql("  where a.roleid = b.roleno");
  		de.addSql("    and b.jsgn = :jsgn");
  		de.addSql("    and b.roletype  = c.roletypeno");
  		de.addSql("     and c.orgtypeno = :orgtype");
  		de.addSql("     and a.pdid = :pdid");
  		de.addSql("     and a.dptdid = :dpid ");
  		de.addSql("     order by  b.rolename ");
  		this.de.setString("jsgn", OdssuContants.JSGN_INNERDUTY);
  		this.de.setString("orgtype", orgtype);
		this.de.setString("pdid", pdid);
		this.de.setString("dpid", dpid);
		DataStore dsreturn = de.query();
		result.put("dslcrwxggwry", dsreturn);// 流程任务相关岗位人员
		return result;
	}



	/**
	 * 方法简介：点击Fn目录的节点，展示Fn 郑海杰 2015-7-29
	 */
	public DataObject queryGnrwByFolder(DataObject para) throws Exception {
		String folderid = para.getString("folderid");
		String orgtype = para.getString("orgtype");
		String dbid = GlobalNames.DEBUGMODE?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;

		de.clearSql();
  		de.addSql("  select a.functionid,a.functionname,a.fnfolderid folderid,d.folderlabel");
  		de.addSql("    from odsv.notbpfn_view a,");
  		de.addSql("         odssu.fn_roletype b,");
  		de.addSql("         odssu.db_appfunction c,");
  		de.addSql("         odssu.fn_folder      d,");
  		de.addSql("         odssu.ir_org_role_type      e,");
  		de.addSql("         odssu.role_type      g");
  		de.addSql("  where a.functionid = b.functionid");
  		de.addSql("    and b.roletypeno = e.roletypeno");
  		de.addSql("    and e.orgtypeno = :orgtype ");
  		de.addSql("    and c.functionid = a.functionid");
  		de.addSql("    and c.dbid = :dbid ");
  		de.addSql("    and a.fnfolderid = d.fnfolderid ");
  		de.addSql("    and e.roletypeno = g.typeno ");
  		de.addSql("    and g.jsgn = :jsgn");
  		de.addSql("    and a.fnfolderid = :folderid        ");
		de.setString("orgtype", orgtype);
		de.setString("dbid", dbid);
		de.setString("jsgn", OdssuContants.JSGN_INNERDUTY);
		de.setString("folderid", folderid);
		DataStore dsfn = this.de.query();
		DataObject result = DataObject.getInstance();
		result.put("dsfn", dsfn);
		return result;
	}


	/**
	 * 方法简介：查询流程任务 郑海杰 2015-7-29
	 */
	public DataObject queryGnrwLike(DataObject para) throws Exception {
		String roletypeno = para.getString("roletypeno", "");
		String label = para.getString("label", "");
		
		/**
         * 获取 dbid
         * modi by fandq
         */
        String dbid = GlobalNames. DEBUGMODE ?(String)this.getUser().getValue("dbid"):OdssuNames.DBID;

		if (roletypeno == null || "".equals(roletypeno)) {
			roletypeno = "%";
		} else {
			roletypeno = "%" + roletypeno + "%";
		}

		if (label == null || "".equals(label)) {
			label = "%";
		} else {
			label = "%" + label + "%";
		}
		FnDataSource fn = new FnDataSource();
		DataStore dsfn = fn.initFnByRoleTypeAndFunctioname(roletypeno, label,dbid);
		DataObject result = DataObject.getInstance();
		result.put("dsfn", dsfn);
		return result;
	}

	/**
	 * 方法简介：点击Fn,展示Fn的有权处理角色 郑海杰 2015-7-29
	 */
	public DataObject queryGngwry(DataObject para) throws Exception {
		String orgtype = para.getString("orgtype");
		String functionid = para.getString("functionid");

		DataObject result = DataObject.getInstance();
		DataStore dsreturn = DataStore.getInstance();

		de.clearSql();
  		de.addSql(" select b.roleno,b.rolename dutyname");
  		de.addSql("   from odssu.role_function_manual a,");
  		de.addSql("        odssu.roleinfor b,");
  		de.addSql("        odssu.ir_org_role_type c");
  		de.addSql("   where a.functionid = :functionid");
  		de.addSql("     and a.roleno = b.roleno ");
  		de.addSql("     and b.jsgn = :jsgn");
  		de.addSql("     and b.roletype = c.roletypeno ");
  		de.addSql("     and c.orgtypeno = :orgtype");
  		de.addSql("     order by b.rolename ");
		de.setString("functionid", functionid);
		de.setString("jsgn", OdssuContants.JSGN_INNERDUTY);
		de.setString("orgtype", orgtype);
		dsreturn = de.query();
		result.put("dsfngwry", dsreturn);
		return result;
	}

}
