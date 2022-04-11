package com.dw.log;

import org.apache.commons.lang3.StringUtils;

import com.dareway.framework.common.BusinessNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.CurrentUser;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

public class QueryLogBPO extends BPO {
	
	public DataObject getEmpInfor(DataObject para) throws AppException {
		String empno = para.getString("empno");
		String logid = para.getString("logid","");
		String emptype = para.getString("emptype");
		
		if("manager".equals(emptype)) {
			de.clearSql();
			de.addSql(" select distinct a.managerno empno,a.managername empname,b.loginname ");
			de.addSql("   from odssu.loginfo a,odssu.empinfor b ");
			de.addSql("    where   a.managerno = b.empno        ");
		}else if("spr".equals(emptype)) {
			de.clearSql();
			de.addSql(" select distinct a.sprno empno,a.sprname empname,b.loginname ");
			de.addSql("   from odssu.loginfo a,odssu.empinfor b ");
			de.addSql("    where   a.sprno = b.empno            ");
		}else if("operate".equals(emptype)) {
			de.clearSql();
			de.addSql(" select distinct a.empno,a.empname,b.loginname ");
			de.addSql("   from odssu.log_emp a,odssu.empinfor b ");
			de.addSql("    where   a.empno = b.empno            ");

		}

		if(StringUtils.isNotBlank(logid)) {
			de.addSql("   and  a.logid = :logid                    ");
			de.setString("logid", logid);
		}
		if(StringUtils.isNotBlank(empno)) {
			de.addSql(" and (b.empno like :empno or upper(b.empname) like :empno) ");
			de.setString("empno", "%"+empno.toUpperCase()+"%");
		}
		de.addSql("   order by b.loginname	 ");
		DataStore ds = de.query();
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("empds", ds);
		return vdo;
	}
	public DataObject getRoleInfor(DataObject para) throws AppException {
		String roleno = para.getString("roleno");
		String logid = para.getString("logid","");
		String operatetype = para.getString("operatetype");
		
		if("EmpRole".equals(operatetype)){
			de.clearSql();
			de.addSql(" select distinct a.roleno,a.rolename,a.orgname  ");
			de.addSql("  from odssu.log_emp_role_detail a ");
			de.addSql("   where 1=1          ");
		}else if("RoleResource".equals(operatetype)) {
			de.clearSql();
			de.addSql(" select distinct a.roleno,a.rolename  ");
			de.addSql("  from odssu.log_role_fn a   ");
			de.addSql("   where 1=1          ");
		}
		if(StringUtils.isNotBlank(logid)) {
			de.addSql("   and a.logid = :logid    ");
			de.setString("logid", logid);
		}
		if(StringUtils.isNotBlank(roleno)) {
			de.addSql(" and ( a.roleno like :roleno or upper(a.rolename) like :roleno) ");
			de.setString("roleno", "%"+roleno.toUpperCase()+"%");
		}
		DataStore ds = de.query();
		
		if("RoleResource".equals(operatetype)) {
			de.clearSql();
			de.addSql(" select distinct a.roleno,a.rolename        ");
			de.addSql("  from odssu.log_role_dp a         ");
			de.addSql("   where 1=1          ");
			if(StringUtils.isNotBlank(logid)) {
				de.addSql("   and a.logid = :logid    ");
				de.setString("logid", logid);
				de.setString("logid", logid);

			}
			if(StringUtils.isNotBlank(roleno)) {
				de.addSql(" and ( a.roleno like :roleno or upper(a.rolename) like :roleno) ");
				de.setString("roleno", "%"+roleno.toUpperCase()+"%");
			}
			DataStore roleds = de.query();
			ds.combineDatastore(roleds);
			ds.remainDistinct();
		}
		ds.sort("rolename");
		DataObject vdo = DataObject.getInstance();
		vdo.put("roleds", ds);
 		return vdo;
	}
	public DataObject getOrgInfor(DataObject para) throws AppException {
		String orgno = para.getString("orgno");
		String logid = para.getString("logid");
		
		de.clearSql();
		de.addSql(" select a.orgno,a.orgname ");
		de.addSql("  from odssu.log_emp_role_detail a ");
		de.addSql("   where   a.logid = :logid                          ");
		de.setString("logid", logid);
	
		if(StringUtils.isNotBlank(orgno)) {
			de.addSql(" and (a.orgno like :orgno or upper(a.orgname) like :orgno) ");
			de.setString("orgno", "%"+orgno.toUpperCase()+"%");
		}
		de.addSql("   order by a.orgno	 ");
		DataStore ds = de.query();
		ds.remainDistinct();
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("orgds", ds);
		return vdo;
	}
	public DataObject getSrInfor(DataObject para) throws AppException {
		String srid = para.getString("srid");
		String logid = para.getString("logid");
		
		de.clearSql();
		de.addSql(" select a.functionid srid,a.functionname srname,'功能' srtype    ");
		de.addSql("  from odssu.log_role_fn a     ");
		de.addSql("   where a.logid = :logid      ");
		de.setString("logid", logid);
		
		if(StringUtils.isNotBlank(srid)) {
			de.addSql(" and (upper(a.functionid) like :srid or upper(a.functionname) like :srid) ");
			de.setString("srid", "%"+srid.toUpperCase()+"%");
		}
		DataStore ds = de.query();
		
		de.clearSql();
		de.addSql(" select a.pdid srid,a.pdlabel srname,'流程' srtype ");
		de.addSql("  from odssu.log_role_dp a     ");
		de.addSql("   where a.logid = :logid      ");
		de.setString("logid", logid);
		
		if(StringUtils.isNotBlank(srid)) {
			de.addSql(" and (upper(a.pdid) like :srid or upper(a.pdlabel) like :srid) ");
			de.setString("srid", "%"+srid.toUpperCase()+"%");
		}
		DataStore dpds = de.query();
		
		ds.combineDatastore(dpds);
		ds.remainDistinct();
		ds.sort("srid");
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("srds", ds);
		return vdo;
	}
	
	/**
	 * 描述：获取人员信息
	 * author: sjn
	 * date: 2018年6月5日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject getUserInfor(DataObject para) throws AppException,BusinessException{
		String userid = para.getString("userid");
		DataStore vds = DataStore.getInstance();
		String useridinsql = "%" + userid.toUpperCase() + "%";
		if (userid == null || "".equals(userid)) {
			de.clearSql();
  			de.addSql(" select distinct a.empno userid, a.empname username  ");
  			de.addSql("    from odssu.empinfor a ");
  			de.addSql("   where a.sleepflag = '0' ");
  			de.addSql("   order by a.empno	 ");
			vds = this.de.query();
		}else {
			de.clearSql();
  			de.addSql(" select distinct a.empno userid, a.empname username  ");
  			de.addSql("    from odssu.empinfor a ");
  			de.addSql("     where (upper(a.empno) like :useridinsql or upper(a.empname) like :useridinsql or upper(a.rname) like :useridinsql or upper(a.loginname) like :useridinsql) ");
  			de.addSql("       and a.sleepflag = '0'  ");
  			de.addSql("   order by userid	 ");
			this.de.setString("useridinsql", useridinsql);
			vds = this.de.query();
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("userinfor", vds);
		return vdo;
	}
	
	/**
	 * 描述：获取ods的流程信息
	 * author: sjn
	 * date: 2018年6月6日
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject getPDInfor(DataObject para) throws AppException{
		String pdid = para.getString("pdid");
		CurrentUser user = this.getUser();
		String dbid = GlobalNames.DEBUGMODE ?(String)user.getValue("dbid"):BusinessNames.DBID;
		DataStore vds = DataStore.getInstance();
		String pdidinsql = "%" + pdid.toUpperCase() + "%";
		if (pdid == null || "".equals(pdid)) {
			de.clearSql();
  			de.addSql("select distinct a.pdid, a.pdlabel, b.table_name ");
  			de.addSql("  from bpzone.process_define_in_activiti a, bpzone.ws_table b ");
  			de.addSql(" where a.pdaid = b.pdaid ");
  			de.addSql("   and b.is_mastertable = '1' ");
  			de.addSql("   and a.wso_appid = 'ODSSU' ");
  			de.addSql("   and not exists (select 1 ");
  			de.addSql("          from bpzone.pd_customed c ");
  			de.addSql("         where a.pdid = c.standard_pdid ");
  			de.addSql("           and c.dbid = :dbid) ");
  			de.addSql("   and a.pdid not in ('HsuOdsFUkey','BFKey','FUkey','setOpenedFnid','czdlmjmm','Lswttwdg','Lswtdgcx','xzksnpgw','xzkswpgw','xgksnpgw','xgkswpgwzn') ");
  			de.addSql(" order by a.pdlabel ");
			this.de.setString("dbid",dbid);
			vds = this.de.query();
		}else {
			de.clearSql();
  			de.addSql("select distinct a.pdid, a.pdlabel, b.table_name ");
  			de.addSql("  from bpzone.process_define_in_activiti a, bpzone.ws_table b ");
  			de.addSql(" where a.pdaid = b.pdaid ");
  			de.addSql("   and b.is_mastertable = '1' ");
  			de.addSql("   and a.wso_appid = 'ODSSU' ");
  			de.addSql("   and not exists (select 1 ");
  			de.addSql("          from bpzone.pd_customed c ");
  			de.addSql("         where a.pdid = c.standard_pdid ");
  			de.addSql("           and c.dbid = :dbid) ");
  			de.addSql("   and a.pdid not in ('HsuOdsFUkey','BFKey','FUkey','setOpenedFnid','czdlmjmm','Lswttwdg','Lswtdgcx','xzksnpgw','xzkswpgw','xgksnpgw','xgkswpgwzn') ");
  			de.addSql("   and (upper(a.pdid) like :pdidinsql ");
  			de.addSql("    or upper(a.pdlabel) like :pdidinsql) ");
  			de.addSql(" order by a.pdlabel ");
			this.de.setString("dbid",dbid);
			this.de.setString("pdidinsql",pdidinsql);
			vds = this.de.query();
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("pdinfor", vds);
		return vdo;
	}
	
	/**
	 * 描述：获取日志信息
	 * author: sjn
	 * date: 2018年6月6日
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject getLogInfor(DataObject para) throws AppException{
		CurrentUser user = this.getUser();
		String dbid = GlobalNames.DEBUGMODE ?(String)user.getValue("dbid"):BusinessNames.DBID;
		String pdid = para.getString("pdid", "");
		String table_name = para.getString("table_name", "");
		if (pdid != null && "jgcxhf".equals(pdid)) {
			table_name = "odssuws.jgjbxxxzwzb";
		}

		DataStore vds = DataStore.getInstance();
		
		if (pdid == null || "".equals(pdid) || table_name == null || "".equals(table_name)) {
			de.clearSql();
  			de.addSql("select distinct a.pdid, b.table_name ");
  			de.addSql("  from bpzone.process_define_in_activiti a, bpzone.ws_table b ");
  			de.addSql(" where a.pdaid = b.pdaid ");
  			de.addSql("   and b.is_mastertable = '1' ");
  			de.addSql("   and a.wso_appid = 'ODSSU' ");
  			de.addSql("   and not exists (select 1 ");
  			de.addSql("          from bpzone.pd_customed c ");
  			de.addSql("         where a.pdid = c.standard_pdid ");
  			de.addSql("           and c.dbid = :dbid) ");
  			de.addSql("   and a.pdid not in ('HsuOdsFUkey','BFKey','FUkey','setOpenedFnid','czdlmjmm','Lswttwdg','Lswtdgcx','xzksnpgw','xzkswpgw','xgksnpgw','xgkswpgwzn') ");
  			de.addSql(" order by a.pdid ");
			this.de.setString("dbid",dbid);
			DataStore tabelvds = this.de.query();
			for (int i = 0; i < tabelvds.rowCount(); i++) {
				pdid = tabelvds.getString(i, "pdid");
				table_name = tabelvds.getString(i, "table_name");
				DataStore tablevds = this.querylogds(pdid, table_name, para);
				vds.combineDatastore(tablevds);
			}
		}else {
			vds = this.querylogds(pdid, table_name, para);
		}
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("logvds", vds);
		return vdo;
	}
	
	/**
	 * 描述：查询工单主表及审批表中的日志信息
	 * author: sjn
	 * date: 2018年6月6日
	 * @param table_name
	 * @param para
	 * @return
	 * @throws AppException
	 */
	private DataStore querylogds(String pdid, String table_name, DataObject para) throws AppException{
		String operatorid = para.getString("operatorid", "");
		String operationtimebegin = para.getString("operationtimebegin", "");
		String operationtimeend = para.getString("operationtimeend", "");
		String sprid = para.getString("sprid", "");
		String spsjbegin = para.getString("spsjbegin", "");
		String spsjend = para.getString("spsjend", "");
		String sfjz = para.getString("sfjz", "");
		DataStore vds = DataStore.getInstance();
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select a.piid, ");
  		de.addSql("       a.pdid, ");
  		de.addSql("       a.pdlabel, ");
  		de.addSql("       f.psoid, ");
  		de.addSql("       d.empname operator, ");
  		de.addSql("       b.operationtime, ");
  		de.addSql("       e.empname spr, ");
  		de.addSql("       c.spsj, ");
  		de.addSql("       case when b.jzbz = '0' then '未记账' when b.jzbz = '1' then '已记账' end jzbz ");
  		de.addSql("  from bpzone.pi_hi_view_with_define_info a ");
  		de.addSql("  left outer join bpzone.pi_hi_pso f on a.piid = f.piid, ");
  		de.addSql("  "+ table_name +" b ");
  		de.addSql("  left outer join odssuws.spinfor c ");
  		de.addSql("    on b.piid = c.piid ");
  		de.addSql("  left outer join odssu.empinfor e ");
  		de.addSql("    on c.spr = e.empno, odssu.empinfor d ");
  		de.addSql(" where a.piid = b.piid ");
  		de.addSql("   and b.operator = d.empno ");
  		de.addSql("   and a.pdid = :pdid ");
		if (sfjz != null && sfjz.equals("true")) {
  			de.addSql("   and b.jzbz = '1' ");
		}
		if (operatorid != null && !"".equals(operatorid)) {
  			de.addSql("   and b.operator = :operatorid");
  			de.setString("operatorid", operatorid);
		}
		if (!StringUtils.isBlank(operationtimebegin) && StringUtils.isBlank(operationtimeend)) {
  			de.addSql("   and c2n(d2c(b.operationtime, 'yyyyMMddhh24miss')) >= c2n(:operationtimebegin) ");
  			de.setString("operationtimebegin", operationtimebegin);
		}else if (StringUtils.isBlank(operationtimebegin) && !StringUtils.isBlank(operationtimeend)) {
  			de.addSql("   and c2n(d2c(b.operationtime, 'yyyyMMddhh24miss')) <= c2n(:operationtimeend) ");
  			de.setString("operationtimeend", operationtimeend);
		}else if (!StringUtils.isBlank(operationtimebegin) && !StringUtils.isBlank(operationtimeend)) {
  			de.addSql("   and c2n(d2c(b.operationtime, 'yyyyMMddhh24miss')) >= c2n(:operationtimebegin) ");
  			de.setString("operationtimebegin", operationtimebegin);
  			de.addSql("   and c2n(d2c(b.operationtime, 'yyyyMMddhh24miss')) <= c2n(:operationtimeend)  ");
  			de.setString("operationtimeend", operationtimeend);
		}
		if (sprid != null && !"".equals(sprid)) {
  			de.addSql("   and c.spr = :sprid");
  			de.setString("sprid", sprid);
		}
		if (!StringUtils.isBlank(spsjbegin) && StringUtils.isBlank(spsjend)) {
  			de.addSql("   and c2n(d2c(c.spsj, 'yyyyMMddhh24miss')) >= c2n(:spsjbegin) ");
  			de.setString("spsjbegin", spsjbegin);
		}else if (StringUtils.isBlank(spsjbegin) && !StringUtils.isBlank(spsjend)) {
  			de.addSql("   and c2n(d2c(c.spsj, 'yyyyMMddhh24miss')) <= c2n(:spsjend) ");
  			de.setString("spsjend", spsjend);
		}else if (!StringUtils.isBlank(spsjbegin) && !StringUtils.isBlank(spsjend)) {
  			de.addSql("   and c2n(d2c(c.spsj, 'yyyyMMddhh24miss')) >= c2n(:spsjbegin) ");
  			de.setString("spsjbegin", spsjbegin);
  			de.addSql("   and c2n(d2c(c.spsj, 'yyyyMMddhh24miss')) <= c2n(:spsjend)  ");
  			de.setString("spsjend", spsjend);
		}
  		de.addSql(" order by b.operationtime desc,c.spsj desc ");
		de.setString("pdid", pdid);
		vds = de.query();
		// 拼polabel
		if (vds != null && vds.rowCount() > 0) {
			for (int i = 0; i < vds.rowCount(); i++) {
				String piid = vds.getString(i, "piid");
				DataStore tmpvds = vds.findAll(" piid == " + piid);
				String polabel = "";
				if (tmpvds.rowCount() > 1) {
					for (int j = 0; j < tmpvds.rowCount(); j++) {
						if (j == 0) {
							String poid = tmpvds.getString(j, "psoid");
							if (StringUtils.isBlank(poid)) {
								continue;
							}
							polabel = OdssuUtil.getPOLabel(poid);
						}else {
							String poid = tmpvds.getString(j, "psoid");
							if (StringUtils.isBlank(poid)) {
								continue;
							}
							polabel = polabel + "/" + OdssuUtil.getPOLabel(poid);
							vds.remove(tmpvds.get(j));
						}
					}
					vds.put(i, "polabel", polabel);
				}else {
					String poid = vds.getString(i, "psoid");
					if (StringUtils.isBlank(poid)) {
						continue;
					}
					polabel = OdssuUtil.getPOLabel(poid);
					vds.put(i, "polabel", polabel);
				}
			}
		}
		
		return vds;
	}
	
	/**
	 * 描述：获取日志详细信息URL
	 * author: sjn
	 * date: 2018年6月7日
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject getLogDetailURL(DataObject para) throws AppException{
		String pdid = para.getString("pdid");
		String logDetailURL = "";
		DataObject vdo = DataObject.getInstance();
		if (StringUtils.isBlank(pdid)) {
			vdo.put("logDetailURL", logDetailURL);
			return vdo;
		}
		DataStore vds = DataStore.getInstance();
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select b.pequrl ");
  		de.addSql("  from bpzone.process_define a, bpzone.process_define_in_activiti b ");
  		de.addSql(" where a.pdaid = b.pdaid ");
  		de.addSql("   and b.pdid = :pdid ");
		de.setString("pdid",pdid);
		vds = de.query();
		if (vds == null || vds.rowCount() <= 0) {
			vdo.put("logDetailURL", logDetailURL);
			return vdo;
		}
		logDetailURL = vds.getString(0, "pequrl");
		vdo.put("logDetailURL", logDetailURL);
		return vdo;
	}
	
}
