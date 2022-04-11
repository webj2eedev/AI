package com.dw.odssu.res;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.dareway.apps.odssu.OdssuNames;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.CurrentUser;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

public class RESBPO extends BPO{
	
	/**
	 * 方法简介 ：机构查询ONP 
	 *@author 郑海杰   2016年4月19日
	 */
	public final DataObject resForOrgInfo(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String orgno = para.getString("orgno");
		String orgnoUpperCase = para.getString("orgno").toUpperCase();
		
		boolean showsleep = para.getBoolean("showsleep");
		
		orgno = ((orgno == null || "".equals(orgno)) ? "%" : "%" + orgno + "%");
		orgnoUpperCase = ((orgnoUpperCase == null || "".equals(orgnoUpperCase)) ? "%" : "%" + orgnoUpperCase + "%");
		de.clearSql();
    	de.addSql("  select * ");
  		de.addSql("    from (select a.sleepflag ,a.orgno , a.displayname , a.orgname ,a.orgtype, b.typename orgtypename ");
  		de.addSql("            from odssu.orginfor a, ");
  		de.addSql("                 odssu.org_type b ");
  		de.addSql("           where (orgno like :orgno or orgname like :orgno or upper(orgnamepy) like :orgnouppercase or displayname like :orgnouppercase  ");
  		de.addSql("                  or upper(displaynamepy) like :orgnouppercase or fullname like :orgnouppercase or upper(fullnamepy) like :orgnouppercase) ");
  		de.addSql("             and a.orgtype = b.typeno ");
  		de.addSql(" 			and b.typeno not like '%_ORGROOT' ");
		if (!showsleep) {
  			de.addSql("         and a.sleepflag = '0' ");
		}
  		de.addSql("          order by sleepflag,b.sn,a.orgsn,orgno,displayname) temp  ");
		this.de.setString("orgno", orgno);
		this.de.setString("orgnouppercase", orgnoUpperCase);
		this.de.setQueryScope(100);
		DataStore orgds = this.de.query();

		vdo.put("orgds", orgds);
		return vdo;
	}
	
	/**
	 * 描述：机构查询ONP省直客户化
	 * author: sjn
	 * date: 2017年1月10日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject resForOrgInfo_379900(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();
		String orgtype, orgtypename;
		String orgno = para.getString("orgno");
		String orgnoUpperCase = para.getString("orgno").toUpperCase();
		
		boolean showsleep = para.getBoolean("showsleep");
		
		orgno = ((orgno == null || "".equals(orgno)) ? "%" : "%" + orgno + "%");
		orgnoUpperCase = ((orgnoUpperCase == null || "".equals(orgnoUpperCase)) ? "%" : "%" + orgnoUpperCase + "%");
    		de.addSql("  select * ");
  		de.addSql("    from (select a.sleepflag ,a.orgno , a.displayname , a.orgname ,a.orgtype ");
  		de.addSql("            from odssu.orginfor a, ");
  		de.addSql("                 odssu.org_type b ");
  		de.addSql("           where (orgno like :orgno or orgname like :orgnouppercase or upper(orgnamepy) like :orgnouppercase or displayname like :orgnouppercase  ");
  		de.addSql("                  or upper(displaynamepy) like :orgnouppercase or fullname like :orgnouppercase or upper(fullnamepy) like :orgnouppercase) ");
  		de.addSql("             and a.orgtype = b.typeno ");
		if (!OdssuUtil.isSysAdmin(this.getUser().getUserid())) {
  			de.addSql(" 		   and a.orgno in " + OdssuUtil.getUserQuerableOrg(this.getUser().getUserid()));
  			
		}
		if (!showsleep) {
  			de.addSql("         and a.sleepflag = '0' ");
		}
  		de.addSql("          order by b.sn,a.orgsn,sleepflag,orgno,displayname)  ");
		this.de.setString("orgno", orgno);
		this.de.setString("orgnouppercase", orgnoUpperCase);
		this.de.setQueryScope(100);
		DataStore orgds = this.de.query();
		for (int i = 0; i < orgds.rowCount(); i++) {
			orgtypename = "";
			orgtype = orgds.getString(i, "orgtype");
			if (!"".equals(orgtype) && orgtype != null) {
				orgtypename = OdssuUtil.getOrgTypeNameByTypeNo(orgtype);
			}
			orgds.put(i, "orgtypename", orgtypename);
		}

		vdo.put("orgds", orgds);
		return vdo;
	}
	/**************************机构查询分页*********************************************/
	public DataObject pageOrgGetRowCount(DataObject para) throws Exception {
		// TODO 可以获取【开发人员自定义参数】
		String orgno = para.getString("orgno");
		String orgnoUpperCase = para.getString("orgno").toUpperCase();
		boolean showsleep = para.getBoolean("showsleep");
		orgno = ((orgno == null || "".equals(orgno)) ? "%" : "%" + orgno + "%");
		orgnoUpperCase = ((orgnoUpperCase == null || "".equals(orgnoUpperCase)) ? "%" : "%" + orgnoUpperCase + "%");
		// TODO 这里放置业务逻辑
		// 1. 计算出分页GRID总行数
		// 2. 自定义其它的汇总信息
		de.clearSql();
		de.addSql("select a.sleepflag ,a.orgno , a.displayname , a.orgname ,a.orgtype, b.typename orgtypename, ");
		de.addSql("       b.sn , a.orgsn ");
  		de.addSql("  from odssu.orginfor a, odssu.org_type b ");
  		de.addSql(" where (orgno like :orgno or orgname like :orgno or upper(orgnamepy) like :orgnouppercase or displayname like :orgnouppercase  ");
  		de.addSql("        or upper(displaynamepy) like :orgnouppercase or upper(fullname) like :orgnouppercase or upper(fullnamepy) like :orgnouppercase) ");
  		de.addSql("   and a.orgtype = b.typeno ");
  		de.addSql("   and b.typeno not like '%_ORGROOT' ");
		if (!showsleep) {
  			de.addSql("         and a.sleepflag = '0' ");
		}
  		de.addSql("          order by a.sleepflag,b.sn,a.orgsn,a.orgno,a.displayname ");
		de.setString("orgno", orgno);
		de.setString("orgnouppercase", orgnoUpperCase);
		DataStore ds = de.query();
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("row_count", ds.rowCount() );
		// 注意：这里返回的DataObject中必须包含以【row_count】为关键字，指明的总行数；
		return vdo;
	}

	public DataObject pageOrgGetPageRows(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String orgnoUpperCase = para.getString("orgno").toUpperCase();
		boolean showsleep = para.getBoolean("showsleep");
		orgno = ((orgno == null || "".equals(orgno)) ? "%" : "%" + orgno + "%");
		orgnoUpperCase = ((orgnoUpperCase == null || "".equals(orgnoUpperCase)) ? "%" : "%" + orgnoUpperCase + "%");
		// TODO 从请求中获取框架预定义的参数
		int g_startRowNumber = para.getInt("g_startRowNumber");
		int g_endRowNumber = para.getInt("g_endRowNumber");
		DataObject result = DataObject.getInstance();
		// TODO 这里放业务逻辑，负责计算GRID总行数
		de.clearSql();
		// 获取全部的任务功能
		de.addSql(" select a.sleepflag ,a.orgno , a.displayname , a.orgname ,a.orgtype, b.typename orgtypename ");
  		de.addSql("            from odssu.orginfor a, odssu.org_type b ");
  		de.addSql("           where (orgno like :orgno or orgname like :orgno or upper(orgnamepy) like :orgnouppercase or displayname like :orgnouppercase  ");
  		de.addSql("                  or upper(displaynamepy) like :orgnouppercase or fullname like :orgnouppercase or upper(fullnamepy) like :orgnouppercase) ");
  		de.addSql("             and a.orgtype = b.typeno ");
  		de.addSql(" 			and b.typeno not like '%_ORGROOT' ");
		if (!showsleep) {
  			de.addSql("         and a.sleepflag = '0' ");
		}
  		de.addSql("          order by sleepflag,b.sn,a.orgsn,orgno,displayname ");
		de.setString("orgno", orgno);
		de.setString("orgnouppercase", orgnoUpperCase);
		DataStore vds = de.query();
		if (vds.isEmpty()) {
			result.put("vds", null);
		} else {
			DataStore subvds = vds.subDataStore(g_startRowNumber - 1, g_endRowNumber);
			String sqlStr;
			sqlStr = "and ( a.orgno = '" + subvds.getString(0, "orgno")+ "'";
			// 将任务功能进行切块
			for (int i = 1; i < subvds.rowCount() - 1; i++) {
				String neworgno = subvds.getString(i, "orgno");
				sqlStr += "    or  a.orgno = '" + neworgno  + "'";
			}
			sqlStr += "  or a.orgno = '" + subvds.getString(subvds.rowCount() - 1, "orgno") + "')";
			// 获取全部的任务功能以及机构，角色信息
			de.clearSql();
			de.addSql(" select a.sleepflag ,a.orgno , a.displayname , a.orgname ,a.orgtype, b.typename orgtypename ");
			de.addSql("            from odssu.orginfor a, ");
			de.addSql("                 odssu.org_type b ");
			de.addSql("           where (orgno like :orgno or orgname like :orgno or upper(orgnamepy) like :orgnouppercase or displayname like :orgnouppercase  ");
			de.addSql("                  or upper(displaynamepy) like :orgnouppercase or fullname like :orgnouppercase or upper(fullnamepy) like :orgnouppercase) ");
			de.addSql("             and a.orgtype = b.typeno ");
			de.addSql(" 			and b.typeno not like '%_ORGROOT' ");
			de.addSql(sqlStr);
			if (!showsleep) {
				de.addSql("         and a.sleepflag = '0' ");
			}
			de.addSql("          order by sleepflag,b.sn,a.orgsn,orgno,displayname ");
			de.setString("orgno", orgno);
			de.setString("orgnouppercase", orgnoUpperCase);
			DataStore vds1 = de.query();
			result.put("vds", vds1);
		}
		return result;
	}

	public DataObject pageOrgGetAllRows(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String orgno = para.getString("orgno");
		String orgnoUpperCase = para.getString("orgno").toUpperCase();
		boolean showsleep = para.getBoolean("showsleep");
		orgno = ((orgno == null || "".equals(orgno)) ? "%" : "%" + orgno + "%");
		orgnoUpperCase = ((orgnoUpperCase == null || "".equals(orgnoUpperCase)) ? "%" : "%" + orgnoUpperCase + "%");
		
		de.clearSql();
		de.addSql("select a.sleepflag ,a.orgno , a.displayname , a.orgname ,a.orgtype, b.typename orgtypename ");
  		de.addSql("            from odssu.orginfor a, ");
  		de.addSql("                 odssu.org_type b ");
  		de.addSql("           where (orgno like :orgno or orgname like :orgno or upper(orgnamepy) like :orgnouppercase or displayname like :orgnouppercase  ");
  		de.addSql("                  or upper(displaynamepy) like :orgnouppercase or fullname like :orgnouppercase or upper(fullnamepy) like :orgnouppercase) ");
  		de.addSql("             and a.orgtype = b.typeno ");
  		de.addSql(" 			and b.typeno not like '%_ORGROOT' ");
		if (!showsleep) {
  			de.addSql("         and a.sleepflag = '0' ");
		}
  		de.addSql("          order by sleepflag,b.sn,a.orgsn,orgno,displayname      ");
		de.setString("orgno", orgno);
		de.setString("orgnouppercase", orgnoUpperCase);
		DataStore vds = de.query();
		vdo.put("orgds", vds);
		return vdo;
	}
	
	 /**
	  * 人员查询ONP
	  */
	/**************************人员查询分页*********************************************/
	public DataObject pagerEmpGetRowCount(DataObject para) throws Exception {
		String username = para.getString("username");
		boolean showsleep = para.getBoolean("showsleep");

		CurrentUser user = this.getUser();
        String dbid = GlobalNames.DEBUGMODE?(String)user.getValue("dbid"):OdssuNames.DBID;

		de.clearSql();
		de.addSql(" select  count(1) row_count  ");
	    de.addSql("   from  odssu.empinfor a    ");
	    de.addSql("  where  1=1  ");
	    if(StringUtils.isNotBlank(username)) {
	    	de.addSql("    and  (a.loginname like :username or a.idcardno like :username or a.empnamepy like :username  ");
	    	de.addSql(" 	or a.empname like :username or a.rname like :username  ");
	    	de.addSql("     or  a.rnamepy like :username or a.mphone like :username or a.email like :username or a.officetel like :username)  ");
	    	de.setString("username", "%"+username.toUpperCase()+"%");
	    }
		if (!showsleep) {
			de.addSql("            and a.sleepflag = '0' ");
		}
		DataStore ds = de.query();
        if("265".equals(dbid)) {
            de.clearSql();
            de.addSql(" select count(1) row_count from odssu.ir_emp_gzryinfor a");
            de.addSql("  where a.dwbh like :username or a.gzrybh like :username");
            this.de.setString("username", "%"+username.toUpperCase()+"%");
            DataStore vdsgzry = de.query();
            if (vdsgzry != null && vdsgzry.rowCount() > 0) {
                int gzry_row = vdsgzry.getInt(0,"row_count");
                ds.put(0,"row_count",gzry_row+ds.getInt(0,"row_count"));
            }
        }

		return ds.getRow(0);
	}

	public DataObject pagerEmpGetPageRows(DataObject para) throws Exception {
		String username = para.getString("username");
		boolean showsleep = para.getBoolean("showsleep");
		CurrentUser user = this.getUser();
		String dbid = GlobalNames.DEBUGMODE?(String)user.getValue("dbid"):OdssuNames.DBID;

		// TODO 从请求中获取框架预定义的参数
		int g_startRowNumber = para.getInt("g_startRowNumber");
		int g_endRowNumber = para.getInt("g_endRowNumber");
		DataObject result = DataObject.getInstance();

		this.de.clearSql();
		this.de.addSql(" select a.empno,a.idcardno,a.empname,a.gender,a.sleepflag, a.loginname,    ");
		if("178".equals(dbid)) {
			this.de.addSql("    u.uactid,u.uact,u.username uactusername, ");
		}
		this.de.addSql("        c.orgname,d.jobno,d.jobname ,d.joborder ,e.orgname  belongname  "); 
		this.de.addSql("   from odssu.empinfor a    "); 
		this.de.addSql("        left outer join odssu.emp_job b on a.empno = b.empno        "); 
		this.de.addSql("	    left outer join   odssu.orginfor c   on  b.orgno = c.orgno            "); 
		this.de.addSql("	    left outer join   odssu.jobinfor d   on  b.jobno = d.jobno             "); 
		this.de.addSql("        left outer  join  odssu.orginfor  e    on     a.hrbelong = e.orgno      "); 
		if("178".equals(dbid)) {
			this.de.addSql("   left outer  join  odssu.uactinfor u on a.uactid = u.uactid   "); 
		}
		this.de.addSql("  where 1=1 ");
		if(StringUtils.isNotBlank(username)) {
			
			this.de.addSql("    and (a.loginname like :username or a.idcardno like :username or a.empnamepy like :username ");
			this.de.addSql("     or a.empname like :username or a.rname like :username     ");
			this.de.addSql("     or a.rnamepy like :username or a.mphone like :username  ");
			this.de.addSql("     or a.email like :username or a.officetel like :username) ");
			this.de.setString("username", "%"+username.toUpperCase()+"%");
		}
		if (!showsleep) {
			this.de.addSql("            and a.sleepflag = '0' ");
		}

		this.de.addSql("         order by a.sleepflag,a.empno,a.empname,d.joborder      ");
		DataStore vds = this.de.query();

        if("265".equals(dbid)) {
            de.clearSql();
            de.addSql(" select empno from odssu.ir_emp_gzryinfor a");
            de.addSql("  where a.dwbh like :username or a.gzrybh like :username");
            this.de.setString("username", "%"+username.toUpperCase()+"%");
            DataStore vdsgzry = de.query();
            DataStore vdsemp = DataStore.getInstance();
            if (vdsgzry != null && vdsgzry.rowCount() > 0) {
                for(int i = 0; i < vdsgzry.rowCount(); i++ ){
                    de.clearSql();
                    this.de.addSql(" select a.empno,a.idcardno,a.empname,a.gender,a.sleepflag, a.loginname,    ");
                    this.de.addSql("        c.orgname,d.jobno,d.jobname ,d.joborder ,e.orgname  belongname  ");
                    this.de.addSql("   from odssu.empinfor a    ");
                    this.de.addSql("        left outer join odssu.emp_job b on a.empno = b.empno        ");
                    this.de.addSql("	    left outer join   odssu.orginfor c   on  b.orgno = c.orgno            ");
                    this.de.addSql("	    left outer join   odssu.jobinfor d   on  b.jobno = d.jobno             ");
                    this.de.addSql("        left outer  join  odssu.orginfor  e    on     a.hrbelong = e.orgno      ");
                    this.de.addSql("  where 1=1 ");
                    de.addSql("  and a.empno = :empno");
                    this.de.setString("empno", vdsgzry.getString(i,"empno"));
                    vdsemp.combineDatastore(de.query());
                }
                if (vdsemp != null && vdsemp.rowCount() > 0) {
                    vds.combineDatastore(vdsemp);
                }
            }
        }
		vds = dealempds(vds);
		
		result.put("vds", vds.subDataStore(g_startRowNumber-1, g_endRowNumber));
		return result;
	}

	public DataObject pagerEmpGetAllRows(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String username = para.getString("username");
		boolean showsleep = para.getBoolean("showsleep");
        CurrentUser user = this.getUser();
        String dbid = GlobalNames.DEBUGMODE?(String)user.getValue("dbid"):OdssuNames.DBID;

		this.de.clearSql();
		this.de.addSql(" select a.empno,a.empname,a.empnamepy,a.rnamepy,a.gender,a.sleepflag, b.orgname "); 
		this.de.addSql("        a.loginname,d.jobno,d.jobname ,d.joborder ,e.orgname  belongname "); 
		this.de.addSql("   from odssu.empinfor a    "); 
		this.de.addSql("        left join odssu.emp_job b on a.empno = b.empno ");
		this.de.addSql("	    left join odssu.orginfor c on b.orgno = c.orgno "); 
		this.de.addSql("	    left join odssu.jobinfor d on b.jobno = d.jobno    "); 
		this.de.addSql("        left join odssu.orginfor e on b.orgno = e.orgno "); 
		this.de.addSql("  where 1=1   ");
		
		if(StringUtils.isNotBlank(username)) {
			this.de.addSql("    and (a.loginname like :username or a.idcardno like :username ");
			this.de.addSql("     or a.empnamepy like :username or a.empname like :username or a.rname like :username");
			this.de.addSql("     or a.rnamepy like :username or a.mphone like :username or a.email like :username or a.officetel like :username) ");
			this.de.setString("username", "%"+username.toUpperCase()+"%");
		}
		if (!showsleep) {
			this.de.addSql("            and a.sleepflag = '0' ");
		}
		this.de.addSql("         order by a.sleepflag,a.empno,a.empname,d.joborder      ");
		DataStore vds = this.de.query();
		
		DataStore empds = dealempds(vds);
		
		vdo.put("empds", empds);
		return vdo;
	}
	public DataStore dealempds(DataStore vds) throws AppException{
		
		if(vds == null) {
			return DataStore.getInstance();
		}
		if(vds != null && vds.rowCount() ==0) {
			return vds;
		}
		
		DataStore empds =DataStore.getInstance(vds.rowCount());
		HashMap<String,Integer> empExists = new HashMap<String,Integer>();
		
		for (int i = 0; i < vds.rowCount();i++) {
			String empno = vds.getString(i, "empno");
			String orgname = vds.getString(i, "orgname");
			String jobname = vds.getString(i, "jobname");
			Integer locate = empExists.get(empno);
			if(locate != null) {
				String aggjobname = empds.getString(locate,"aggjobname");
				if(aggjobname.indexOf(orgname+":"+jobname)==-1){
					empds.put(locate,"aggjobname", aggjobname+","+orgname+":"+jobname);
				}
			}else {
				DataObject showdo = vds.getRow(i);
				if(null==jobname||"".equals(jobname)){
					showdo.put("aggjobname", "");
				}else{
					showdo.put("aggjobname", orgname+":"+jobname);
				}
				empds.addRow(showdo);
				empExists.put(empno, empds.rowCount()-1);
			}
		}
		return empds;
	}
	/**
	 * 描述：人员查询ONP省直客户化
	 * author: sjn
	 * date: 2017年1月10日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject resForEmpInfo_379900(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();

		//用户名 大小写不敏感，统一转成大写去比较
		String username = para.getString("username");
		boolean showsleep = para.getBoolean("showsleep");
		username = ((username == null || "".equals(username)) ? "%" : "%" + username + "%");
		username = username.toUpperCase();
		DataStore empds = DataStore.getInstance();
		if(!showsleep){
			de.clearSql();
  			de.addSql(" select * ");
  			de.addSql("   from (select a.empno,a.idcardno,a.empname,a.rname,a.empnamepy,a.rnamepy,a.gender,a.emptype,         ");
  			de.addSql("         a.sleepflag,a.officetel,a.mphone,a.email,a.hrbelong,a.loginname,b.orgname                 ");
  			de.addSql("           from odssu.empinfor a left outer join odssu.orginfor b	 ");
  			de.addSql("                on a.hrbelong = b.orgno   ");
  			de.addSql("         where (upper(a.loginname) like :username or a.idcardno like :username or upper(a.empnamepy) like :username or a.empname like :username or a.rname like :username ");
  			de.addSql("            or  upper(a.rnamepy) like :username ) ");
  			de.addSql("           and a.sleepflag = '0' ");
			if (!OdssuUtil.isSysAdmin(this.getUser().getUserid())) {
  				de.addSql("           and b.orgno in " + OdssuUtil.getUserQuerableOrg(this.getUser().getUserid()));
			}
  			de.addSql("         order by a.sleepflag,a.empno,a.empname) tem    ");
			this.de.setString("username", username);
			this.de.setQueryScope(100);
			empds=this.de.query();
			
		}else{
			de.clearSql();
  			de.addSql(" select * ");
  			de.addSql("   from (select a.sleepflag,a.empno,a.loginname,a.empname,b.orgname                 ");
  			de.addSql("           from odssu.empinfor a left outer join odssu.orginfor b	 ");
  			de.addSql("                on a.hrbelong = b.orgno   ");
  			de.addSql("         where (upper(a.loginname) like :username or a.idcardno like :username or upper(a.empnamepy) like :username or a.empname like :username or a.rname like :username ");
  			de.addSql("            or  upper(a.rnamepy) like :username ) ");
  			de.addSql("           and a.sleepflag = '0' ");
			if (!OdssuUtil.isSysAdmin(this.getUser().getUserid())) {
  				de.addSql("           and b.orgno in " + OdssuUtil.getUserQuerableOrg(this.getUser().getUserid()));
			}
  			de.addSql("         order by a.sleepflag,a.empno,a.empname) tem1     ");
  			de.setQueryScope(80);
  			DataStore empds1 = de.query();
  			
  			de.clearSql();
  			de.addSql(" select * ");
  			de.addSql("   from (select a.sleepflag,a.empno,a.loginname,a.empname, ' ' orgname ");
  			de.addSql("  		 from odssu.empinfor a ");
  			de.addSql("          where a.sleepflag = '1' ");
  			de.addSql("            and (upper(a.loginname) like :username or a.idcardno like :username or upper(a.empnamepy) like :username or a.empname like :username or a.rname like :username ");
  			de.addSql("            or  upper(a.rnamepy) like :username )) tem2");
			this.de.setString("username", username);
			de.setQueryScope(20);
			DataStore empds2 = de.query();
			
			DataStore empds3 = DataStore.getInstance();
			for (int i = 0; i < empds2.rowCount(); i++) {
				String empno = empds2.getString(i, "empno");
				if(empds1.find("empno =="+ empno)<0) {
					empds3.addRow(empds2.getRow(i));
				}
			}
			
			empds1.combineDatastore(empds3);
			empds = empds1.clone();
		}

		vdo.put("empds", empds);
		return vdo;
	}
	
	/**
	 * 方法简介 ：角色查询ONP 
	 *@author 郑海杰   2016年4月19日
	 */
	public final DataObject resForRoleInfo(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();
		String roleno = para.getString("roleno");
		String jsgn = para.getString("jsgn","0");
		
		boolean showsleep = para.getBoolean("showsleep");
		
		String rolenoUpperCase = para.getString("roleno").toUpperCase();
		roleno = ((roleno == null || "".equals(roleno)) ? "%" : "%" + roleno + "%");
		rolenoUpperCase = ((rolenoUpperCase == null || "".equals(rolenoUpperCase)) ? "%" : "%" + rolenoUpperCase + "%");
  		de.addSql(" select * ");
  		de.addSql(" from odssu.roleinfor ");
  		de.addSql(" where (roleno like :roleno or rolename like :rolenouppercase or upper(rolenamepy) like :rolenouppercase) ");
		if (!showsleep) {
  			de.addSql(" and sleepflag = '0' ");
		}
  		de.addSql(" and jsgn = :jsgn ");
  		de.addSql(" order by sleepflag, roleno, rolename ");
		this.de.setString("jsgn", jsgn);
		this.de.setString("roleno", roleno);
		this.de.setString("rolenouppercase", rolenoUpperCase);
		this.de.setQueryScope(100);
		DataStore roleds = this.de.query();

		vdo.put("roleds", roleds);
		return vdo;
	}
	
	/**
	 * 方法简介：查询查询角色信息 赵伟华  2019-12-16
	 */
	public final DataObject resForNewRoleInfo(final DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String deforgno = "", collect_query = "";
		String folderid = para.getString("folderid","");
		if(folderid==null || "".equals(folderid)) {
			throw new AppException("入参folderid为空，请检查。");
		}
		String role_condition_upper = para.getString("roleno","").toUpperCase();
		role_condition_upper = ((role_condition_upper == null || "".equals(role_condition_upper)) ? "%" : "%" + role_condition_upper + "%");
		DataStore roleds =  DataStore.getInstance();
		//角色查询，不限定folderid，不限定defineorgno
		if("role_query".equals(folderid)) {
			de.clearSql();
	  		de.addSql(" select roleno, displayname, rolename, sleepflag, folderno ,'调整目录' tzml ");//displayname
	  		de.addSql(" from odssu.roleinfor ");
	  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper or upper(rolenamepy) like :role_condition_upper) ");
	  		de.addSql(" and sleepflag = '0' ");
	  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
	  		de.addSql(" order by  rolename, roleno ");
			de.setString("role_condition_upper", role_condition_upper);
			roleds = de.query();
		}else if (folderid.startsWith("njjs_query")){
			//内建角色，只查询内建角色
			deforgno = "ORGROOT";
			de.clearSql();
	  		de.addSql(" select roleno, displayname, rolename, sleepflag, folderno ,a.appid ,b.appname,'调整目录' tzml ");//displayname
	  		de.addSql(" from odssu.roleinfor a ,odssu.appinfo b ");
	  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper or upper(rolenamepy) like :role_condition_upper) ");
	  		de.addSql(" and sleepflag = '0' ");
	  		de.addSql(" and a.appid = b.appid ");
	  		de.addSql(" and jsgn = '2' ");
	  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
	  		de.addSql(" order by  roleno, rolename ");
			de.setString("role_condition_upper", role_condition_upper);
			roleds = de.query();
		}else {
			//查询当前folder是否为汇总查询，如人社系统
			de.clearSql();
	  		de.addSql(" select t.folderid, t.deforgno, t.collect_query ");
	  		de.addSql(" from odssu.role_folder t ");
	  		de.addSql(" where t.folderid = :folderid ");
			de.setString("folderid", folderid);
			DataStore fnfolderds = de.query();
			if(fnfolderds==null || fnfolderds.rowCount() == 0) {
				throw new Exception("编号为["+folderid+"]的目录不存在，请检查。");
			}else if(fnfolderds.rowCount() > 1){
				throw new Exception("存在多个编号为["+folderid+"]的目录，请检查。");
			}else {
				deforgno = fnfolderds.getString(0, "deforgno");
				collect_query = fnfolderds.getString(0, "collect_query");
			}
			if("1".equals(collect_query)) {//汇总查询，根据defineorgno查询
				de.clearSql();
		  		de.addSql(" select roleno, displayname, rolename, sleepflag, folderno ");//displayname
		  		de.addSql(" from odssu.roleinfor ");
		  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper or upper(rolenamepy) like :role_condition_upper) ");
		  		de.addSql(" and sleepflag = '0' ");
		  		de.addSql(" and (deforgno = :deforgno or deforgno='ORGROOT') ");
		  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
		  		de.addSql(" order by  roleno, rolename ");
				de.setString("role_condition_upper", role_condition_upper);
				de.setString("deforgno", deforgno);
				roleds = de.query();
			}else { //非汇总查询，根据folderno查询
				de.clearSql();
		  		de.addSql(" select roleno, displayname, rolename, sleepflag, folderno ,a.appid ,b.appname,'调整目录' tzml ");//displayname
		  		de.addSql(" from odssu.roleinfor a ,odssu.appinfo b ");
		  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper or upper(rolenamepy) like :role_condition_upper) ");
		  		de.addSql(" and sleepflag = '0' ");
		  		de.addSql(" and a.appid = b.appid ");
		  		de.addSql(" and folderno = :folderno ");
		  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
		  		de.addSql(" order by  roleno, rolename ");
				de.setString("role_condition_upper", role_condition_upper);
				de.setString("folderno", folderid);
				roleds = de.query();
			}
		}
		
		int rolecount = roleds.rowCount();
		for(int i = 0; i < rolecount; i++) {
			String TYPENAME = "";
			String ROLENO = roleds.getString(i, "roleno");
	  		de.clearSql();
	  		de.addSql(" select b.TYPENO, b.TYPENAME ");
	  		de.addSql(" from odssu.org_type b, odssu.role_orgtype c ");
	  		de.addSql(" where b.TYPENO = c.orgtypeno ");
	  		de.addSql(" and c.roleno = :roleno ");
	  		de.setString("roleno", ROLENO);
			DataStore typeds = de.query();
			int typecount = typeds.rowCount();
			if(typecount >= 1) {
				for(int j = 0; j < typecount; j++) {
					if(j == 0) {
						TYPENAME = typeds.getString(j, "TYPENAME");
					}else {
						TYPENAME = TYPENAME + "," + typeds.getString(j, "TYPENAME");
					}
				}
			}
			roleds.put(i,"TYPENAME", TYPENAME);
		}

		vdo.put("deforgno", deforgno);
		vdo.put("roleds", roleds);
		return vdo;
	}
	
	/**************************角色查询分页*********************************************/
	public DataObject pageRoleGetRowCount(DataObject para) throws Exception {
		String deforgno = "", collect_query = "";
		String folderid = para.getString("folderid","");
		if(folderid==null || "".equals(folderid)) {
			throw new AppException("入参folderid为空，请检查。");
		}
		String role_condition_upper = para.getString("roleno","").toUpperCase();
		role_condition_upper = ((role_condition_upper == null || "".equals(role_condition_upper)) ? "%" : "%" + role_condition_upper + "%");
		DataStore roleds =  DataStore.getInstance();
		DataStore njjsds = DataStore.getInstance();
		DataObject vdo = DataObject.getInstance();
		//角色查询，不限定folderid，不限定defineorgno
		if("role_query".equals(folderid)) {
			de.clearSql();
	  		de.addSql("select roleno, displayname, rolename, sleepflag, folderno ,'null' rsxtid ");//displayname
	  		de.addSql(" from odssu.roleinfor a, odssu.appinfo b  ");
	  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper ");
	  		de.addSql("  or upper(rolenamepy) like :role_condition_upper or upper(b.appname) like :role_condition_upper ) ");
	  		de.addSql(" and sleepflag = '0' ");
	  		de.addSql(" and a.appid = b.appid ");
	  		de.addSql(" and jsgn <> '2' ");
	  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
	  		de.addSql(" order by  roleno, rolename ");
			de.setString("role_condition_upper", role_condition_upper);
			roleds = de.query();
			
			//查询内建角色
			de.clearSql();
	  		de.addSql("select a.roleno, a.displayname, a.rolename||'('||b.orgname||')' rolename, a.sleepflag, a.folderno , b.orgno rsxtid ");//displayname
	  		de.addSql(" from odssu.roleinfor a ,odssu.orginfor b ,odssu.ir_org_type c ,odssu.appinfo d ");
	  		de.addSql(" where (upper(a.roleno) like :role_condition_upper or upper(a.rolename) like :role_condition_upper  ");
	  		de.addSql("  or upper(a.rolenamepy) like :role_condition_upper or upper(a.rolename||b.orgname) like :role_condition_upper   ");
	  		de.addSql("  or upper(d.appname) like :role_condition_upper  ) ");
	  		de.addSql(" and a.sleepflag = '0' ");
	  		de.addSql(" and a.jsgn = '2'  ");
	  		de.addSql(" and b.orgtype = c.subtypeno ");
	  		de.addSql(" and c.suptypeno = 'ODSSU_ORGROOT' ");
	  		de.addSql(" and b.sleepflag = '0' ");
	  		de.addSql(" and a.appid = d.appid ");
	  		de.addSql(" and a.roleno not in (select n.roleno from odssu.njjs_filter n )");
	  		de.addSql(" order by  a.roleno, a.rolename ");
			de.setString("role_condition_upper", role_condition_upper);
			njjsds = de.query();
			
		}else if (folderid.startsWith("njjs_query")){
			//内建角色，只查询内建角色
			deforgno = "ORGROOT";
			de.clearSql();
			de.addSql("select roleno, displayname, rolename, sleepflag, folderno ");//displayname
	  		de.addSql(" from odssu.roleinfor ");
	  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper or upper(rolenamepy) like :role_condition_upper) ");
	  		de.addSql(" and sleepflag = '0' ");
	  		de.addSql(" and  deforgno='ORGROOT' ");
	  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
	  		de.addSql(" order by  roleno, rolename ");
			de.setString("role_condition_upper", role_condition_upper);
			roleds = de.query();
		}else {
			//查询当前folder是否为汇总查询，如人社系统
			de.clearSql();
	  		de.addSql(" select t.folderid, t.deforgno, t.collect_query ");
	  		de.addSql(" from odssu.role_folder t ");
	  		de.addSql(" where t.folderid = :folderid ");
			de.setString("folderid", folderid);
			DataStore fnfolderds = de.query();
			if(fnfolderds==null || fnfolderds.rowCount() == 0) {
				throw new Exception("编号为["+folderid+"]的目录不存在，请检查。");
			}else if(fnfolderds.rowCount() > 1){
				throw new Exception("存在多个编号为["+folderid+"]的目录，请检查。");
			}else {
				deforgno = fnfolderds.getString(0, "deforgno");
				collect_query = fnfolderds.getString(0, "collect_query");
			}
			if("1".equals(collect_query)) {//汇总查询，根据defineorgno查询
				de.clearSql();
				de.addSql("select roleno, displayname, rolename, sleepflag, folderno ");//displayname
		  		de.addSql(" from odssu.roleinfor a,odssu.appinfo b  ");
		  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper ");
		  		de.addSql("  or upper(rolenamepy) like :role_condition_upper or upper(b.appname) like :role_condition_upper ) ");
		  		de.addSql(" and sleepflag = '0' ");
		  		de.addSql(" and a.appid = b.appid ");
		  		de.addSql(" and sleepflag = '0' ");
		  		de.addSql(" and (deforgno = :deforgno or deforgno='ORGROOT') ");
		  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
		  		de.addSql(" order by  roleno, rolename ");
				de.setString("role_condition_upper", role_condition_upper);
				de.setString("deforgno", deforgno);
				roleds = de.query();
			}else { //非汇总查询，根据folderno查询
				de.clearSql();
				de.addSql("select roleno, displayname, rolename, sleepflag, folderno ");//displayname
		  		de.addSql(" from odssu.roleinfor ");
		  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper or upper(rolenamepy) like :role_condition_upper) ");
		  		de.addSql(" and sleepflag = '0' ");
		  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
		  		de.addSql(" and folderno = :folderno ");
		  		de.addSql(" order by  roleno, rolename ");
				de.setString("role_condition_upper", role_condition_upper);
				de.setString("folderno", folderid);
				roleds = de.query();
			}
		}
		int roledscount = roleds.rowCount()+njjsds.rowCount();
		vdo.put("row_count",roledscount);
		return vdo;
	}

	public DataObject pageRoleGetPageRows(DataObject para) throws Exception {
		// TODO 从请求中获取框架预定义的参数
		int g_startRowNumber = para.getInt("g_startRowNumber");
		int g_endRowNumber = para.getInt("g_endRowNumber");
		DataObject vdo = DataObject.getInstance();
		String deforgno = "", collect_query = "";
		String folderid = para.getString("folderid","");
		if(folderid==null || "".equals(folderid)) {
			throw new AppException("入参folderid为空，请检查。");
		}
		String role_condition_upper = para.getString("roleno","").toUpperCase();
		role_condition_upper = ((role_condition_upper == null || "".equals(role_condition_upper)) ? "%" : "%" + role_condition_upper + "%");
		DataStore roleds =  DataStore.getInstance();
		//角色查询，不限定folderid，不限定defineorgno
		if("role_query".equals(folderid)) {
			de.clearSql();
	  		de.addSql(" select roleno, displayname, rolename, sleepflag, folderno ,'null' rsxtid ,a.appid,b.appname ");//displayname
	  		de.addSql(" from odssu.roleinfor a ,odssu.appinfo b");
	  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper ");
	  		de.addSql("    or upper(rolenamepy) like :role_condition_upper or upper(b.appname) like :role_condition_upper ) ");
	  		de.addSql(" and sleepflag = '0' ");
	  		de.addSql(" and jsgn <> '2' ");
	  		de.addSql(" and a.appid = b.appid  ");
	  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
	  		de.addSql(" order by  roleno, rolename ");
			de.setString("role_condition_upper", role_condition_upper);
			roleds = de.query();
			
			//查询内建角色
			de.clearSql();
	  		de.addSql(" select a.roleno, a.displayname, a.rolename||'('||b.orgname||')' rolename, a.sleepflag, a.folderno ,b.orgno rsxtid,a.appid,d.appname");//displayname
	  		de.addSql(" from odssu.roleinfor a ,odssu.orginfor b ,odssu.ir_org_type c ,odssu.appinfo d ");
	  		de.addSql(" where (upper(a.roleno) like :role_condition_upper or upper(a.rolename) like :role_condition_upper  ");
	  		de.addSql("  or upper(a.rolenamepy) like :role_condition_upper or upper(a.rolename||b.orgname) like :role_condition_upper ");
	  		de.addSql("  or upper(d.appname) like :role_condition_upper  ) ");
	  		de.addSql(" and a.sleepflag = '0' ");
	  		de.addSql(" and a.jsgn = '2'  ");
	  		de.addSql(" and b.orgtype = c.subtypeno ");
	  		de.addSql(" and c.suptypeno = 'ODSSU_ORGROOT' ");
	  		de.addSql(" and b.sleepflag = '0' ");
	  		de.addSql(" and a.appid = d.appid  ");
	  		de.addSql(" and a.roleno not in (select n.roleno from odssu.njjs_filter n )");
	  		de.addSql(" order by  a.roleno, a.rolename ");
			de.setString("role_condition_upper", role_condition_upper);
			DataStore njjsds = de.query();
			
			roleds.combineDatastore(njjsds);
			
			
			
			if (roleds.isEmpty()) {
				//roleds为空
			} else {
				DataStore subvds = roleds.subDataStore(g_startRowNumber - 1, g_endRowNumber);
				String sqlStr, sqlStr1;
				sqlStr = "and ( roleno = '" + subvds.getString(0, "roleno")+ "'";
				// 将任务功能进行切块
				for (int i = 1; i < subvds.rowCount() - 1; i++) {
					String newroleno = subvds.getString(i, "roleno");
					sqlStr += "    or  roleno = '" + newroleno  + "'";
				}
				sqlStr += "  or roleno = '" + subvds.getString(subvds.rowCount() - 1, "roleno") + "')";
				sqlStr1 = "and ( roleno = '" + subvds.getString(0, "roleno")+ "'";;
				// 将任务功能进行切块
				for (int i = 1; i < subvds.rowCount()-1; i++) {
					String newroleno = subvds.getString(i, "roleno");
					String newrsxtid = subvds.getString(i, "rsxtid");
					sqlStr1 += "    or ( roleno = '" + newroleno  + "' and b.orgno = '" + newrsxtid  + "' ) ";
				}
				sqlStr1 += "  )";
				// 获取全部的任务功能以及机构，角色信息
				roleds = DataStore.getInstance();
				njjsds = DataStore.getInstance();
				de.clearSql();
		  		de.addSql(" select roleno, displayname, rolename, sleepflag, folderno ,'null' rsxtid ,a.appid ,b.appname ");//displayname
		  		de.addSql(" from odssu.roleinfor a,odssu.appinfo b ");
		  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper or upper(rolenamepy) like :role_condition_upper) ");
		  		de.addSql(" and sleepflag = '0' ");
		  		de.addSql(sqlStr);
		  		de.addSql(" and jsgn <> '2' ");
		  		de.addSql(" and a.appid = b.appid  ");
		  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
		  		de.addSql(" order by  roleno, rolename ");
				de.setString("role_condition_upper", role_condition_upper);
				roleds = de.query();
				
				//查询内建角色
				de.clearSql();
		  		de.addSql(" select a.roleno, a.displayname, a.rolename||'('||b.orgname||')' rolename, a.sleepflag, a.folderno , b.orgno rsxtid ,a.appid ,d.appname");//displayname
		  		de.addSql(" from odssu.roleinfor a ,odssu.orginfor b ,odssu.ir_org_type c ,odssu.appinfo d ");
		  		de.addSql(" where (upper(a.roleno) like :role_condition_upper or upper(a.rolename) like :role_condition_upper  ");
		  		de.addSql("  or upper(a.rolenamepy) like :role_condition_upper or upper(a.rolename||b.orgname) like :role_condition_upper ");
		  		de.addSql("  or upper(d.appname) like :role_condition_upper  ) ");
		  		de.addSql(" and a.sleepflag = '0' ");
		  		de.addSql(sqlStr1);
		  		de.addSql(" and a.jsgn = '2'  ");
		  		de.addSql(" and a.appid = d.appid ");
		  		de.addSql(" and b.orgtype = c.subtypeno ");
		  		de.addSql(" and c.suptypeno = 'ODSSU_ORGROOT' ");
		  		de.addSql(" and b.sleepflag = '0' ");
		  		de.addSql(" and a.roleno not in (select n.roleno from odssu.njjs_filter n )");
		  		de.addSql(" order by  a.roleno, a.rolename ");
				de.setString("role_condition_upper", role_condition_upper);
				njjsds = de.query();
				
				roleds.combineDatastore(njjsds);
			}
		}else if (folderid.startsWith("njjs_query")){
			//内建角色，只查询内建角色
			deforgno = "ORGROOT";
			de.clearSql();
	  		de.addSql(" select roleno, displayname, rolename, sleepflag, folderno ");//displayname
	  		de.addSql(" from odssu.roleinfor ");
	  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper or upper(rolenamepy) like :role_condition_upper) ");
	  		de.addSql(" and sleepflag = '0' ");
	  		de.addSql(" and  deforgno='ORGROOT' ");
	  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
	  		de.addSql(" order by  roleno, rolename ");
			de.setString("role_condition_upper", role_condition_upper);
			roleds = de.query();
			if (roleds.isEmpty()) {
				//roleds为空
			} else {
				DataStore subvds = roleds.subDataStore(g_startRowNumber - 1, g_endRowNumber);
				String sqlStr;
				sqlStr = "and ( roleno = '" + subvds.getString(0, "roleno")+ "'";
				// 将任务功能进行切块
				for (int i = 1; i < subvds.rowCount() - 1; i++) {
					String newroleno = subvds.getString(i, "roleno");
					sqlStr += "    or  roleno = '" + newroleno  + "'";
				}
				sqlStr += "  or roleno = '" + subvds.getString(subvds.rowCount() - 1, "roleno") + "')";
				// 获取全部的任务功能以及机构，角色信息
				roleds = DataStore.getInstance();
				de.clearSql();
		  		de.addSql(" select roleno, displayname, rolename, sleepflag, folderno ");//displayname
		  		de.addSql(" from odssu.roleinfor ");
		  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper or upper(rolenamepy) like :role_condition_upper) ");
		  		de.addSql(" and sleepflag = '0' ");
		  		de.addSql(sqlStr);
		  		de.addSql(" and deforgno <> 'ORGROOT' ");
		  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
		  		de.addSql(" order by  roleno, rolename ");
				de.setString("role_condition_upper", role_condition_upper);
				roleds = de.query();
				
				//查询内建角色
				de.clearSql();
		  		de.addSql(" select a.roleno, a.displayname, b.orgname||a.rolename rolename, a.sleepflag, a.folderno ");//displayname
		  		de.addSql(" from odssu.roleinfor a ,odssu.orginfor b ,odssu.ir_org_type c  ");
		  		de.addSql(" where (upper(a.roleno) like :role_condition_upper or upper(a.rolename) like :role_condition_upper  ");
		  		de.addSql("  or upper(a.rolenamepy) like :role_condition_upper)   ");
		  		de.addSql(" and a.sleepflag = '0' ");
		  		de.addSql(sqlStr);
		  		de.addSql(" and a.deforgno = 'ORGROOT'  ");
		  		de.addSql(" and b.orgtype = c.subtypeno ");
		  		de.addSql(" and c.suptypeno = 'ODSSU_ORGROOT' ");
		  		de.addSql(" and b.sleepflag = '0' ");
		  		de.addSql(" and a.roleno not in (select n.roleno from odssu.njjs_filter n )");
		  		de.addSql(" order by  a.roleno, a.rolename ");
				de.setString("role_condition_upper", role_condition_upper);
				DataStore njjsds = de.query();
				
				roleds.combineDatastore(njjsds);
				
			}
		}else {
			//查询当前folder是否为汇总查询，如人社系统
			de.clearSql();
	  		de.addSql(" select t.folderid, t.deforgno, t.collect_query ");
	  		de.addSql(" from odssu.role_folder t ");
	  		de.addSql(" where t.folderid = :folderid ");
			de.setString("folderid", folderid);
			DataStore fnfolderds = de.query();
			if(fnfolderds==null || fnfolderds.rowCount() == 0) {
				throw new Exception("编号为["+folderid+"]的目录不存在，请检查。");
			}else if(fnfolderds.rowCount() > 1){
				throw new Exception("存在多个编号为["+folderid+"]的目录，请检查。");
			}else {
				deforgno = fnfolderds.getString(0, "deforgno");
				collect_query = fnfolderds.getString(0, "collect_query");
			}
			if("1".equals(collect_query)) {//汇总查询，根据defineorgno查询
				de.clearSql();
		  		de.addSql(" select roleno, displayname, rolename, sleepflag, folderno,a.appid ,b.appname ");//displayname
		  		de.addSql(" from odssu.roleinfor a ,odssu.appinfo b ");
		  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper ");
		  		de.addSql("  or upper(rolenamepy) like :role_condition_upper or upper(b.appname) like :role_condition_upper ) ");
		  		de.addSql(" and sleepflag = '0' ");
		  		de.addSql(" and a.appid = b.appid ");
		  		de.addSql(" and (deforgno = :deforgno or deforgno='ORGROOT') ");
		  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
		  		de.addSql(" order by  roleno, rolename ");
				de.setString("role_condition_upper", role_condition_upper);
				de.setString("deforgno", deforgno);
				roleds = de.query();
				if (roleds.isEmpty()) {
					//roleds为空
				} else {
					DataStore subvds = roleds.subDataStore(g_startRowNumber - 1, g_endRowNumber);
					String sqlStr;
					sqlStr = "and ( roleno = '" + subvds.getString(0, "roleno")+ "'";
					// 将任务功能进行切块
					for (int i = 1; i < subvds.rowCount() - 1; i++) {
						String newroleno = subvds.getString(i, "roleno");
						sqlStr += "    or  roleno = '" + newroleno  + "'";
					}
					sqlStr += "  or roleno = '" + subvds.getString(subvds.rowCount() - 1, "roleno") + "')";
					// 获取全部的任务功能以及机构，角色信息
					de.clearSql();
					de.addSql(" select roleno, displayname, rolename, sleepflag, folderno,a.appid ,b.appname ");//displayname
					de.addSql(" from odssu.roleinfor a,odssu.appinfo b ");
					de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper ");
			  		de.addSql("  or upper(rolenamepy) like :role_condition_upper or upper(b.appname) like :role_condition_upper ) ");
					de.addSql(sqlStr);
					de.addSql(" and a.appid = b.appid ");
					de.addSql(" and sleepflag = '0' ");
					de.addSql(" and (deforgno = :deforgno or deforgno='ORGROOT') ");
					de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
					de.addSql(" order by  roleno, rolename ");
					de.setString("role_condition_upper", role_condition_upper);
					de.setString("deforgno", deforgno);
					roleds = de.query();
				}
			}else { //非汇总查询，根据folderno查询
				de.clearSql();
		  		de.addSql(" select roleno, displayname, rolename, sleepflag, folderno ");//displayname
		  		de.addSql(" from odssu.roleinfor ");
		  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper or upper(rolenamepy) like :role_condition_upper) ");
		  		de.addSql(" and sleepflag = '0' ");
		  		de.addSql(" and folderno = :folderno ");
		  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
		  		de.addSql(" order by  roleno, rolename ");
				de.setString("role_condition_upper", role_condition_upper);
				de.setString("folderno", folderid);
				roleds = de.query();
				if (roleds.isEmpty()) {
					//roleds为空
				} else {
					DataStore subvds = roleds.subDataStore(g_startRowNumber - 1, g_endRowNumber);
					String sqlStr;
					sqlStr = "and ( roleno = '" + subvds.getString(0, "roleno")+ "'";
					// 将任务功能进行切块
					for (int i = 1; i < subvds.rowCount() - 1; i++) {
						String newroleno = subvds.getString(i, "roleno");
						sqlStr += "    or  roleno = '" + newroleno  + "'";
					}
					sqlStr += "  or roleno = '" + subvds.getString(subvds.rowCount() - 1, "roleno") + "')";
					// 获取全部的任务功能以及机构，角色信息
					de.clearSql();
					de.addSql(" select roleno, displayname, rolename, sleepflag, folderno ");//displayname
					de.addSql(" from odssu.roleinfor ");
					de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper or upper(rolenamepy) like :role_condition_upper) ");
					de.addSql(sqlStr);
					de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
					de.addSql(" and sleepflag = '0' ");
					de.addSql(" and folderno = :folderno ");
					de.addSql(" order by  roleno, rolename ");
					de.setString("role_condition_upper", role_condition_upper);
					de.setString("folderno", folderid);
					roleds = de.query();
				}
			}
		}
		
		roleds = dealroleds(roleds);
		vdo.put("deforgno", deforgno);
		vdo.put("vds", roleds);
		return vdo;
	}

	public DataObject pageRoleGetAllRows(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String deforgno = "", collect_query = "";
		String folderid = para.getString("folderid","");
		if(folderid==null || "".equals(folderid)) {
			throw new AppException("入参folderid为空，请检查。");
		}
		String role_condition_upper = para.getString("roleno","").toUpperCase();
		role_condition_upper = ((role_condition_upper == null || "".equals(role_condition_upper)) ? "%" : "%" + role_condition_upper + "%");
		DataStore roleds =  DataStore.getInstance();
		//角色查询，不限定folderid，不限定defineorgno
		if("role_query".equals(folderid)) {
			de.clearSql();
	  		de.addSql(" select roleno, displayname, rolename, sleepflag, folderno ,'null' rsxtid ");//displayname
	  		de.addSql(" from odssu.roleinfor a,odssu.appinfo b ");
	  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper  ");
	  		de.addSql(" or upper(rolenamepy) like :role_condition_upper or upper(b.appname) like :role_condition_upper )");
	  		de.addSql(" and sleepflag = '0' ");
	  		de.addSql(" and a.appid = b.appid ");
	  		de.addSql(" and jsgn <> '2' ");
	  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
	  		de.addSql(" order by  roleno, rolename ");
			de.setString("role_condition_upper", role_condition_upper);
			roleds = de.query();
			
			//查询内建角色
			de.clearSql();
	  		de.addSql(" select a.roleno, a.displayname, a.rolename||'('||b.orgname||')' rolename, a.sleepflag, a.folderno , b.orgno rsxtid ");//displayname
	  		de.addSql(" from odssu.roleinfor a ,odssu.orginfor b ,odssu.ir_org_type c ,odssu.appinfo d ");
	  		de.addSql(" where (upper(a.roleno) like :role_condition_upper or upper(a.rolename) like :role_condition_upper  ");
	  		de.addSql("  or upper(a.rolenamepy) like :role_condition_upper or upper(a.rolename||b.orgname) like :role_condition_upper  ");
	  		de.addSql("  or upper(d.appname) like :role_condition_upper  ) ");
	  		de.addSql(" and a.appid = d.appid ");
	  		de.addSql(" and a.sleepflag = '0' ");
	  		de.addSql(" and a.jsgn = '2'  ");
	  		de.addSql(" and b.orgtype = c.subtypeno ");
	  		de.addSql(" and c.suptypeno = 'ODSSU_ORGROOT' ");
	  		de.addSql(" and b.sleepflag = '0' ");
	  		de.addSql(" and a.roleno not in (select n.roleno from odssu.njjs_filter n )");
	  		de.addSql(" order by  a.roleno, a.rolename ");
			de.setString("role_condition_upper", role_condition_upper);
			DataStore njjsds = de.query();
			
			roleds.combineDatastore(njjsds);
		}else if (folderid.startsWith("njjs_query")){
			//内建角色，只查询内建角色
			deforgno = "ORGROOT";
			de.clearSql();
	  		de.addSql(" select roleno, displayname, rolename, sleepflag, folderno ");//displayname
	  		de.addSql(" from odssu.roleinfor ");
	  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper or upper(rolenamepy) like :role_condition_upper) ");
	  		de.addSql(" and sleepflag = '0' ");
	  		de.addSql(" and  deforgno='ORGROOT' ");
	  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
	  		de.addSql(" order by  roleno, rolename ");
			de.setString("role_condition_upper", role_condition_upper);
			roleds = de.query();
		}else {
			//查询当前folder是否为汇总查询，如人社系统
			de.clearSql();
	  		de.addSql(" select t.folderid, t.deforgno, t.collect_query ");
	  		de.addSql(" from odssu.role_folder t ");
	  		de.addSql(" where t.folderid = :folderid ");
			de.setString("folderid", folderid);
			DataStore fnfolderds = de.query();
			if(fnfolderds==null || fnfolderds.rowCount() == 0) {
				throw new Exception("编号为["+folderid+"]的目录不存在，请检查。");
			}else if(fnfolderds.rowCount() > 1){
				throw new Exception("存在多个编号为["+folderid+"]的目录，请检查。");
			}else {
				deforgno = fnfolderds.getString(0, "deforgno");
				collect_query = fnfolderds.getString(0, "collect_query");
			}
			if("1".equals(collect_query)) {//汇总查询，根据defineorgno查询
				de.clearSql();
		  		de.addSql(" select roleno, displayname, rolename, sleepflag, folderno ");//displayname
		  		de.addSql(" from odssu.roleinfor a ,odssu.appinfo b ");
		  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper ");
		  		de.addSql("  or upper(rolenamepy) like :role_condition_upper or upper(b.appname) like :role_condition_upper ) ");
		  		de.addSql(" and sleepflag = '0' ");
		  		de.addSql(" and a.appid = b.appid ");
		  		de.addSql(" and (deforgno = :deforgno or deforgno='ORGROOT') ");
		  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
		  		de.addSql(" order by  roleno, rolename ");
				de.setString("role_condition_upper", role_condition_upper);
				de.setString("deforgno", deforgno);
				roleds = de.query();
			}else { //非汇总查询，根据folderno查询
				de.clearSql();
		  		de.addSql(" select roleno, displayname, rolename, sleepflag, folderno ");//displayname
		  		de.addSql(" from odssu.roleinfor ");
		  		de.addSql(" where (upper(roleno) like :role_condition_upper or upper(rolename) like :role_condition_upper or upper(rolenamepy) like :role_condition_upper) ");
		  		de.addSql(" and sleepflag = '0' ");
		  		de.addSql(" and folderno = :folderno ");
		  		de.addSql(" and roleno not in (select n.roleno from odssu.njjs_filter n )");
		  		de.addSql(" order by  roleno, rolename ");
				de.setString("role_condition_upper", role_condition_upper);
				de.setString("folderno", folderid);
				roleds = de.query();
			}
		}
		
		roleds = dealroleds(roleds);
		vdo.put("deforgno", deforgno);
		vdo.put("roleds", roleds);
		return vdo;
	}
	
	public DataStore dealroleds(DataStore roleds) throws AppException{
		int rolecount = roleds.rowCount();
		for(int i = 0; i < rolecount; i++) {
			String TYPENAME = "";
			String ROLENO = roleds.getString(i, "roleno");
	  		de.clearSql();
	  		de.addSql(" select b.TYPENO, b.TYPENAME ");
	  		de.addSql(" from odssu.org_type b, odssu.role_orgtype c ");
	  		de.addSql(" where b.TYPENO = c.orgtypeno ");
	  		de.addSql(" and c.roleno = :roleno ");
	  		de.setString("roleno", ROLENO);
			DataStore typeds = de.query();
			int typecount = typeds.rowCount();
			if(typecount >= 1) {
				for(int j = 0; j < typecount; j++) {
					if(j == 0) {
						TYPENAME = typeds.getString(j, "TYPENAME");
					}else {
						TYPENAME = TYPENAME + "," + typeds.getString(j, "TYPENAME");
					}
				}
			}
			roleds.put(i,"TYPENAME", TYPENAME);
		}
		return roleds;
	}
}
