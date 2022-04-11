package com.dw.emp;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;
import com.dw.util.multiSortUtil.MultiSortUtil;

public class EmpONPBPO extends BPO {

	public DataObject getempAboutInorg(final DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String empno, empname, gender, email, username;
		String officetel, sleepflag, mphone, ishrbelong, jobno;
		String jobname;
		
		double order,ordermin = 10000;
		de.clearSql(); 
		de.addSql(" select    u.empno,u.empname,u.gender,u.email,u.loginname username,                                                       "); 
		de.addSql("           u.officetel, u.sleepflag,u.mphone,d.jobno,decode(v.ishrbelong,'1','√',null) ishrbelong,d.jobname ,d.joborder   "); 
		de.addSql(" from      odssu.ir_emp_org v LEFT OUTER JOIN  odssu.emp_job c  on v.empno = c.empno and v.orgno = c.orgno                "); 
		de.addSql("           LEFT OUTER JOIN odssu.jobinfor d on c.jobno = d.jobno    ,                                                     "); 
		de.addSql("           odssu.empinfor u                                                                                               "); 
		de.addSql(" where     v.orgno =:orgno and v.empno = u.empno and u.sleepflag = '0'                                                    "); 
	    de.addSql("     order by  d.joborder                                                                                                 "); 
		de.setString("orgno", orgno);
		DataStore vds = de.query();
		DataStore empvds =DataStore.getInstance();
		for (int i = 0; i < vds.rowCount();i++) {
			empno = vds.getString(i, "empno");
			empname = vds.getString(i, "empname");
			gender = vds.getString(i, "gender");
			email = vds.getString(i, "email");
			username = vds.getString(i, "username");
			officetel = vds.getString(i, "officetel");
			sleepflag = vds.getString(i, "sleepflag");
			mphone = vds.getString(i, "mphone");
			ishrbelong = vds.getString(i, "ishrbelong");
			jobno = vds.getString(i, "jobno");
			jobname = vds.getString(i, "jobname");
			order = vds.getDouble(i, "joborder");
			int locate = empvds.find("empno == " + empno);
			if(locate>=0) {
				DataObject showdo = empvds.get(locate);
				String aggjobname = showdo.getString("aggjobname");
				ordermin = showdo.getDouble("ordermin");
				if(ordermin > order&&order!=0){
					showdo.put("ordermin", order);
				}
				if(aggjobname.indexOf(jobname)==-1){
					showdo.put("aggjobname", aggjobname+","+jobname);
				}
			}else {
				DataObject showdo = DataObject.getInstance();
				showdo.put("empno", empno);
				showdo.put("empname", empname);
				showdo.put("gender", gender);
				showdo.put("email", email);
				showdo.put("username", username);
				showdo.put("officetel", officetel);
				showdo.put("sleepflag", sleepflag);
				showdo.put("mphone", mphone);
				showdo.put("ishrbelong", ishrbelong);
				showdo.put("jobno", jobno);
				showdo.put("jobname", jobname);
				showdo.put("joborder", order);
				showdo.put("aggjobname", jobname);
				if(order!=0){
					showdo.put("ordermin", order);
				}else{
					showdo.put("ordermin", 10000);
				}
				empvds.addRow(showdo);
			}
			
		}
		empvds.sort("ishrbelong");
		empvds.sort("ordermin");
		DataObject vdo = DataObject.getInstance();
		vdo.put("empds", empvds);
		int empzscount = empvds.rowCount();
		vdo.put("empzscount", empzscount+"");
		return vdo;
	}
	
	
	/**
	 * 获取该机构的全部下级机构的机构名称，以及某个机构下面的人员的数量
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject getempDownorgInfo(final DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String orgname = OdssuUtil.getOrgNameByOrgno(orgno);
		DataObject vdo = DataObject.getInstance();
		vdo.put("orgname", orgname);
		int empnopcount=0;
		/*
		 * 获取某个机构xiahsu目录下的人员数量
		 */
		de.clearSql();
		de.addSql("select a.orgno, a.orgname, COUNT(distinct c.EMPNO) empnopcount "); 
		de.addSql("FROM odssu.orginfor a, "); 
		de.addSql("     odssu.ir_org_closure b    LEFT OUTER JOIN  odssu.ir_emp_org   c  on b.orgno=c.orgno   "); 
		de.addSql("where a.belongorgno = :orgno "); 
		de.addSql("      and a.orgno=b.belongorgno  "); 
		de.addSql(" 	and a.sleepflag = '0' ");
		de.addSql("group by a.orgno, a.orgname    ");
		de.setString("orgno",orgno);
		DataStore vds = de.query();
		if(!vds.isEmpty()){
			empnopcount = vds.getInt(empnopcount, "empnopcount");
		}
		vdo.put("empnopcount", empnopcount);
		/*
		 * 获取机构下在职和离职人员数量
		 */
		int empbelongcount = 0;
		int emphrbelongcount = 0;
		int empzscount = 0;
		de.clearSql(); 
		de.addSql("select  count(DISTINCT d.empno) empbelongcount "); 
		de.addSql("FROM odssu.orginfor a, "); 
		de.addSql("	   odssu.ir_org_closure b , "); 
		de.addSql("     odssu.ir_emp_org c, "); 
		de.addSql("		 odssu.empinfor d "); 
		de.addSql("where a.belongorgno = :orgno "); 
		de.addSql("      and a.orgno=b.belongorgno  "); 
		de.addSql("      and b.ORGNO=c.ORGNO  "); 
		de.addSql("		  and c.empno = d.empno "); 
		de.addSql("       and d.sleepflag = '0'  ");
		de.addSql(" and a.sleepflag = '0' "); 
		de.setString("orgno",orgno);
		DataStore empbelongorg = de.query();
		empbelongcount = empbelongorg.getInt(0, "empbelongcount");
		de.clearSql(); 
		de.addSql("select  count(DISTINCT d.empno) emphrbelongcount "); 
		de.addSql("FROM odssu.orginfor a, "); 
		de.addSql("	   odssu.ir_org_closure b , "); 
		de.addSql("     odssu.ir_emp_org c, "); 
		de.addSql("		 odssu.empinfor d "); 
		de.addSql("where a.belongorgno = :orgno "); 
		de.addSql("      and a.orgno=b.belongorgno  "); 
		de.addSql("      and b.ORGNO=c.ORGNO  "); 
		de.addSql("		  and c.empno = d.empno "); 
		de.addSql("       and d.sleepflag = '0'  ");
		de.addSql(" and a.sleepflag = '0' "); 
		de.addSql(" and c.ishrbelong = '1' "); 
		de.setString("orgno",orgno);
		DataStore emphrbelongorg = de.query();
		emphrbelongcount = emphrbelongorg.getInt(0, "emphrbelongcount");
		
		for(int i = 0;i<vds.rowCount();i++) {
			String currentorg = vds.getString(i, "orgno");
			int empnumhrbelong = 0;
			
			de.clearSql(); 
			de.addSql("select  count(DISTINCT d.empno) emphrbelongcount "); 
			de.addSql("FROM odssu.orginfor a, "); 
			de.addSql("	   odssu.ir_org_closure b , "); 
			de.addSql("     odssu.ir_emp_org c, "); 
			de.addSql("		 odssu.empinfor d "); 
			de.addSql("where a.orgno = :orgno "); 
			de.addSql("      and a.orgno=b.belongorgno  "); 
			de.addSql("      and b.ORGNO=c.ORGNO  "); 
			de.addSql("		  and c.empno = d.empno "); 
			de.addSql("       and d.sleepflag = '0'  ");
			de.addSql(" and a.sleepflag = '0' "); 
			de.addSql(" and c.ishrbelong = '1' "); 
			de.setString("orgno",currentorg);
			DataStore currentorghrbelongds = de.query();
			if(currentorghrbelongds != null && currentorghrbelongds.rowCount() >0) {
				empnumhrbelong = currentorghrbelongds.getInt(0, "emphrbelongcount");
			}
			DataObject orgempnumdo = vds.get(i);
			int empnumbelongorg = orgempnumdo.getInt("empnopcount");

			DataObject temppara = DataObject.getInstance();
			temppara.put("orgno", currentorg);
			DataObject tempdo = getempAboutInorg(temppara);
			int tmpempzscount = tempdo.getInt("empzscount");
			
			orgempnumdo.put("empnopcount",empnumbelongorg+"（人事隶属"+empnumhrbelong+"人）");
			
			empzscount += tmpempzscount;
		}
		
		vdo.put("empds", vds);
		vdo.put("empzscount", empzscount+"");
		vdo.put("empbelongcount", empbelongcount+"");
		vdo.put("emphrbelongcount", emphrbelongcount+"");
		return vdo;
	
	}
	/**
	 * 获取该机构的全部下级机构的机构名称，以及某个机构下面的人员的数量
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject getempDownorgInfoSBS(final DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String typeno = para.getString("typeno");
		String orgname = OdssuUtil.getOrgNameByOrgno(orgno);
		DataObject vdo = DataObject.getInstance();
		vdo.put("orgname", orgname);
		int empnopcount=0;
		/*
		 * 获取某个机构xiahsu目录下的人员数量
		 */
		de.clearSql();
		de.addSql("select a.orgno, a.orgname, COUNT(c.EMPNO) empnopcount "); 
		de.addSql("FROM odssu.orginfor a, "); 
		de.addSql("     odssu.ir_org_closure b    LEFT OUTER JOIN  odssu.ir_emp_org   c  on b.orgno=c.orgno   "); 
		de.addSql("where a.belongorgno = :orgno "); 
		de.addSql("      and a.orgno=b.belongorgno  "); 
		de.addSql(" 	and a.sleepflag = '0' ");
		de.addSql(" 	and a.orgtype = :orgtype ");
		de.addSql("group by a.orgno, a.orgname    ");
		de.setString("orgno",orgno);
		de.setString("orgtype",typeno);
		DataStore vds = de.query();
		if(!vds.isEmpty()){
			empnopcount = vds.getInt(empnopcount, "empnopcount");
		}
		vdo.put("empnopcount", empnopcount+"");
		
		
		int empbelongcount = 0;
		int emphrbelongcount = 0;
		de.clearSql(); 
		de.addSql("select  count(DISTINCT d.empno) empbelongcount "); 
		de.addSql("FROM odssu.orginfor a, "); 
		de.addSql("	   odssu.ir_org_closure b , "); 
		de.addSql("     odssu.ir_emp_org c, "); 
		de.addSql("		 odssu.empinfor d "); 
		de.addSql("where a.belongorgno = :orgno "); 
		de.addSql("      and a.orgno=b.belongorgno  "); 
		de.addSql("      and b.ORGNO=c.ORGNO  "); 
		de.addSql("		  and c.empno = d.empno "); 
		de.addSql("       and d.sleepflag = '0'  ");
		de.addSql(" and a.sleepflag = '0' "); 
		de.addSql(" and a.orgtype = :orgtype ");
		de.setString("orgno",orgno);
		de.setString("orgtype",typeno);
		DataStore empbelongorg = de.query();
		empbelongcount = empbelongorg.getInt(0, "empbelongcount");
		de.clearSql(); 
		de.addSql("select  count(DISTINCT d.empno) emphrbelongcount "); 
		de.addSql("FROM odssu.orginfor a, "); 
		de.addSql("	   odssu.ir_org_closure b , "); 
		de.addSql("     odssu.ir_emp_org c, "); 
		de.addSql("		 odssu.empinfor d "); 
		de.addSql("where a.belongorgno = :orgno "); 
		de.addSql("      and a.orgno=b.belongorgno  "); 
		de.addSql("      and b.ORGNO=c.ORGNO  "); 
		de.addSql("		  and c.empno = d.empno "); 
		de.addSql("       and d.sleepflag = '0'  ");
		de.addSql(" and a.sleepflag = '0' "); 
		de.addSql(" and c.ishrbelong = '1' "); 
		de.addSql(" 	and a.orgtype = :orgtype ");
		de.setString("orgno",orgno);
		de.setString("orgtype",typeno);
		DataStore emphrbelongorg = de.query();
		emphrbelongcount = emphrbelongorg.getInt(0, "emphrbelongcount");
		
		for(DataObject tempdo : vds) {
			int emphrbelongnum = 0;
			de.clearSql();
			de.addSql("select a.orgno, a.orgname, COUNT(c.EMPNO) empnopcount "); 
			de.addSql("FROM odssu.orginfor a, "); 
			de.addSql("     odssu.ir_org_closure b    LEFT OUTER JOIN  odssu.ir_emp_org   c  on b.orgno=c.orgno   "); 
			de.addSql("where a.orgno = :orgno "); 
			de.addSql("  and a.orgno=b.belongorgno  "); 
			de.addSql("  and a.sleepflag = '0' ");
			de.addSql("  and a.orgtype = :orgtype ");
			de.addSql("group by a.orgno, a.orgname    ");
			de.setString("orgno",orgno);
			de.setString("orgtype",typeno);
			DataStore tempds = de.query();
			if(tempds != null && tempds.rowCount() != 0) {
				emphrbelongnum =  tempds.getInt(0, "empnopcount");
			}
			int empbelongnum = tempdo.getInt("empnopcount") ;
			tempdo.put("empnopcount", empbelongnum+"（人事隶属"+emphrbelongnum+"人）");
		}
		de.clearSql();
		de.addSql("select a.typename from odssu.org_type a where a.typeno = :typeno ");
		de.setString("typeno", typeno);
		DataStore typename = de.query();
		
		vdo.put("empds", vds);
		vdo.put("typename", typename.getString(0, "typename"));
		vdo.put("empbelongcount", empbelongcount+"");
		vdo.put("emphrbelongcount", emphrbelongcount+"");
		return vdo;
		
	}

	public DataObject getempDownorgInfo2(final DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String orgname = OdssuUtil.getOrgNameByOrgno(orgno);
		DataObject vdo = DataObject.getInstance();
		vdo.put("orgname", orgname);
		int empnopcount=0;
		/*
		 * 获取某个机构目录下的在职人员数量
		 */
		de.clearSql();
		de.addSql("select count(DISTINCT d.empno) empnopcount "); 
		de.addSql("FROM odssu.orginfor a, "); 
		de.addSql("	   odssu.ir_org_closure b , "); 
		de.addSql("     odssu.ir_emp_org c , "); 
		de.addSql(" odssu.empinfor d "); 
		de.addSql("where a.orgno = :orgno  "); 
		de.addSql("      and a.orgno=b.belongorgno  "); 
		de.addSql("      and b.ORGNO=c.ORGNO  "); 
		de.addSql("and c.empno = d.empno "); 
		de.addSql("and d.sleepflag = '0' ");
		de.addSql(" and a.sleepflag = '0' "); 
		de.setString("orgno",orgno);
		DataStore vds = de.query();
		vdo.put("empds", vds);
		if(!vds.isEmpty()){
			empnopcount = vds.getInt(empnopcount, "empnopcount");
		}
		//当前机构的在职人员数量
		int empcount = 0;
		de.clearSql(); 
		de.addSql("select  count(DISTINCT d.empno) emphrbelongcount "); 
		de.addSql("FROM odssu.orginfor a, "); 
		de.addSql("	   odssu.ir_org_closure b , "); 
		de.addSql("     odssu.ir_emp_org c, "); 
		de.addSql("		 odssu.empinfor d "); 
		de.addSql("where a.orgno = :orgno "); 
		de.addSql("      and a.orgno=b.belongorgno  "); 
		de.addSql("      and b.ORGNO=c.ORGNO  "); 
		de.addSql("		  and c.empno = d.empno "); 
		de.addSql("       and d.sleepflag = '0'  ");
		de.addSql(" and a.sleepflag = '0' "); 
		de.addSql(" and c.ishrbelong = '1' "); 
		de.setString("orgno",orgno);
		DataStore emphrbelongorg = de.query();
		empcount = emphrbelongorg.getInt(0, "emphrbelongcount");
		
		vdo.put("empcount", empcount+"");
		vdo.put("empnopcount", empnopcount+"");
		return vdo;
	
	}
	
	public DataObject getOrgFromBelongorgno(final DataObject para) throws Exception {
		String belongorgno = para.getString("orgno");
		String typeno = para.getString("orgtype");// 机构类型编号

		de.clearSql();
		DataObject vdo = DataObject.getInstance();
		DataStore vds;

		de.clearSql();
  		de.addSql("select a.orgno,a.displayname,a.orgname,c.typename,c.typenature,    ");
  		de.addSql("       b.orgname belongorgname                                     ");
  		de.addSql("  from odssu.orginfor a,                                           ");
  		de.addSql("       odssu.orginfor b,                                           ");
  		de.addSql("       odssu.org_type c                                            ");
  		de.addSql(" where a.belongorgno= b.orgno                                      ");
  		de.addSql("   and a.orgtype = c.typeno                                        ");
		de.addSql(" and a.sleepflag = '0' ");
		de.addSql(" and b.sleepflag = '0' ");
  		de.addSql("   and a.belongorgno = :belongorgno                                           ");
  		de.addSql("   and a.orgtype = :typeno                                               ");
  		de.addSql(" order by a.orgsn,a.orgno ");
		de.setString("belongorgno", belongorgno);
		de.setString("typeno", typeno);
		vds = de.query();
		vdo.put("vds", vds);
		return vdo;

	}
	
	public DataObject getempAboutInorgPhone(final DataObject para) throws Exception {
		String orgno = para.getString("orgno");
  		  		de.clearSql();
		de.clearSql();
  		de.addSql("select b.typenature ");
  		de.addSql("  from odssu.orginfor a, odssu.org_type b ");
  		de.addSql(" where a.orgtype = b.typeno ");
  		de.addSql("   and a.orgno = :orgno ");
		de.addSql(" and a.sleepflag = '0' ");
		this.de.setString("orgno",orgno);
		DataStore vds = this.de.query();
		String typenature = vds.getString(0, "typenature");
		DataObject vdo = DataObject.getInstance();
		if (typenature.equals("A")||typenature.equals("C")) {
			vdo = getempAboutInorg(para);
			return vdo;
		}else if (typenature.equals("B")) {
			de.clearSql();
  			de.addSql("select a.empno,a.empname,b.gender,b.email,a.loginname username, ");
  			de.addSql("       b.officetel,b.sleepflag,b.mphone,'0' ishrbelong,null rolename,c.ishrbelong,c.empsn ");
  			de.addSql("  from odsv.emp_belong_dw a, odssu.empinfor b,odssu.ir_emp_org c ");
  			de.addSql(" where a.empno = b.empno  ");
  			de.addSql("   and b.empno = c.empno ");
  			de.addSql("   and a.dwbh = c.orgno ");
  			de.addSql("   and a.dwbh = :orgno ");
  			de.addSql("   order by empsn ");
			de.setString("orgno", orgno);
			DataStore empvds = de.query();
			for (int i = 0; i < empvds.size(); i++) {
				String empno = empvds.getString(i, "empno");
				de.clearSql();
  				de.addSql(" select rolename,b.rolesn ");
  				de.addSql("   from odssu.ir_emp_org_all_role a, ");
  				de.addSql("        odssu.roleinfor b ");
  				de.addSql("  where a.empno = :empno ");
  				de.addSql("    and a.orgno = :orgno ");
  				de.addSql("    and a.roleno = b.roleno ");
  				de.addSql("    and a.rolenature = :rolenature");
  				de.addSql("    and a.roleno <> 'MEMBER' ");
  				de.addSql("    and a.jsgn = :jsgn");
				this.de.setString("empno", empno);
				this.de.setString("orgno", orgno);
				this.de.setString("rolenature", OdssuContants.ROLENATURE_CYJS);
				this.de.setString("jsgn", OdssuContants.JSGN_POST);
				DataStore roleNameVds = this.de.query();
				StringBuffer roleNameBF = new StringBuffer();
				roleNameBF.append("");
				int rolesn = 100;
				for (int j = 0; j < roleNameVds.size(); j++) {
					String roleName = roleNameVds.getString(j, "rolename");
					Integer rolesnInt = roleNameVds.getInt(j, "rolesn");
					if (rolesnInt != null && rolesnInt != 0) {
						if (rolesnInt < rolesn) {
							rolesn = rolesnInt;
						}
					}
					roleNameBF.append(roleName + ",");
				}
				if (roleNameBF.length() > 0) {
					roleNameBF.deleteCharAt(roleNameBF.length() - 1);
				}
				empvds.put(i, "rolename", roleNameBF.toString());
				empvds.put(i, "rolesn", rolesn);
			}
			
			empvds.sort("empsn:asc");
			vdo.put("empds", empvds);
			return vdo;
		}else {
			throw new AppException("传入的机构不合法！");
		}
	}

	/**
	 * 描述：人员查询方法高新政务客户化
	 * author: sjn
	 * date: 2017年8月2日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject getempAboutInorg_3701GXGWH(final DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		de.clearSql();
		de.addSql(" select u.empno,u.empname,u.gender,u.email,u.loginname username, ");
		de.addSql("        u.officetel, u.sleepflag,u.mphone,v.ishrbelong,null rolename,nvl(v.empsn,10000) empsn ");
		de.addSql("   from   odssu.empinfor u, ");
		de.addSql("        odssu.ir_emp_org v ");
		de.addSql("  where  v.empno = u.empno ");
		de.addSql("    and v.orgno = :orgno ");
		de.addSql("        order by v.empsn,u.empno ");
		de.setString("orgno", orgno);
		DataStore empvds = de.query();
		for (int i = 0; i < empvds.size(); i++) {
			String empno = empvds.getString(i, "empno");
			de.clearSql();
  			de.addSql(" select rolename,b.rolesn ");
  			de.addSql("   from odssu.ir_emp_org_all_role a, ");
  			de.addSql("        odssu.roleinfor b ");
  			de.addSql("  where a.empno = :empno ");
  			de.addSql("    and a.orgno = :orgno ");
  			de.addSql("    and a.roleno = b.roleno ");
  			de.addSql("    and a.rolenature = :rolenature");
  			de.addSql("    and a.roleno <> 'MEMBER' ");
  			de.addSql("    and a.jsgn = :jsgn");
			this.de.setString("empno", empno);
			this.de.setString("orgno", orgno);
			this.de.setString("rolenature", OdssuContants.ROLENATURE_CYJS);
			this.de.setString("jsgn", OdssuContants.JSGN_POST);
			DataStore roleNameVds = this.de.query();
			StringBuffer roleNameBF = new StringBuffer();
			roleNameBF.append("");
			int rolesn = 100;
			for (int j = 0; j < roleNameVds.size(); j++) {
				String roleName = roleNameVds.getString(j, "rolename");
				Integer rolesnInt = roleNameVds.getInt(j, "rolesn");
				if (rolesnInt != null && rolesnInt != 0) {
					if (rolesnInt < rolesn) {
						rolesn = rolesnInt;
					}
				}
				roleNameBF.append(roleName + ",");
			}
			if (roleNameBF.length() > 0) {
				roleNameBF.deleteCharAt(roleNameBF.length() - 1);
			}
			empvds.put(i, "rolename", roleNameBF.toString());
			empvds.put(i, "rolesn", rolesn);
		}

		DataObject vdo = DataObject.getInstance();
		MultiSortUtil.multiSortDS(empvds, "empsn:asc");
		vdo.put("empds", empvds);
		return vdo;
	}
}
