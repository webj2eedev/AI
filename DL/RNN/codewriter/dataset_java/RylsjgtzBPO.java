package com.dw.odssu.ws.emp.rylsjgtz;

import java.util.Date;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.apps.process.ProcessBPO;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

/**
 * 人员隶属关系调整 类描述
 * 
 * @author liuy
 * @version 1.0 创建时间 2014-05-07
 */
public final class RylsjgtzBPO extends BPO{
	/**
	 * 人员隶属机构调整流程 方法简介
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-10-13
	 * @param para
	 * @return
	 * @throws Exception
	 */
	
	public final DataObject fwPageEmpOrgAdjust(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), result = DataObject.getInstance(), rdo;
		DataStore gdxxds, empds = DataStore.getInstance(), belongds, roleds;
  		de.clearSql();
		String piid, hrorgno = "", orgno;
		String empno, empname, roleno;

		// 流程开始获取piid
		piid = para.getString("piid");

		// 查询工单信息
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");
		empno = rdo.getString("empno", "");
		empname = rdo.getString("empname", "");

		// 如果无工单，创建工单
		if (gdxxds.rowCount() == 0) {
			para.put("piid", piid);
			BPO ibpo = this.newBPO(ProcessBPO.class);
			result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);

			empno = result.getString("empno");
			empname = result.getString("empname");

			// 查询界面需要的数据
			DataObject pdo = this.getRyjbxx(piid, empno);
			belongds = pdo.getDataStore("belongds");
			roleds = pdo.getDataStore("roleds");
			hrorgno = pdo.getString("hrorgno");

			// ----生成工單信息 begin
			addNewMainGdxx(piid, empno, hrorgno);// 工单主表
			for (int i = 0; i < belongds.rowCount(); i++) {
				orgno = belongds.getString(i, "orgno");
				addNewOrgDetlGdxx(piid, orgno);// 工单附表,隶属机构表

				de.clearSql();
  				de.addSql("select a.roleno  ");
  				de.addSql("  from odssu.ir_emp_org_all_role a   ");
  				de.addSql(" where a.empno = :empno ");
  				de.addSql("   and a.orgno = :orgno ");
  				de.addSql("   and a.rolenature = '4' ");
				this.de.setString("empno", empno);
				this.de.setString("orgno", orgno);
				roleds = this.de.query();

				for (int j = 0; j < roleds.rowCount(); j++) {
					roleno = roleds.getString(j, "roleno");
					addNewOrgRoleDetlGdxx(piid, orgno, roleno);// 工单附表,隶属机构角色表
				}
			}
			// ----生成工單信息 end

			empds.put(0, "piid", piid);
			empds.put(0, "empno", empno);
			empds.put(0, "empname", empname);
			vdo.put("empds", empds);
			vdo.put("gdxxds", belongds);

			gdxxds = null;
			empds = null;
			return vdo;
		}

		empds.put(0, "piid", piid);
		empds.put(0, "empno", empno);
		empds.put(0, "empname", empname);
		vdo.put("empds", empds);
		vdo.put("gdxxds", gdxxds);

		return vdo;
	}

	/**
	 * 查询需要的数据
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 **/
	
	private final DataObject getRyjbxx(final String piid, final String empno) throws Exception {
  		de.clearSql();
		DataObject vdo = DataObject.getInstance();
		DataStore belongds, dstmp, roleds = DataStore.getInstance(), empds = DataStore.getInstance();
		String empname, orgno, roleno, rolename, hrorgno = "", ishrbelongflag, flag, rolenono, rolenamename;
		int i, j;

		if (empno == null || empno.trim().isEmpty() == true) {
			throw new BusinessException("获取人员编号失败！");
		}

		de.clearSql();
  		de.addSql("select * 			 ");
  		de.addSql("  from odssu.empinfor  ");
  		de.addSql(" where empno = :empno		 ");
  		de.addSql("   and sleepflag = '0' ");
		this.de.setString("empno", empno);
		dstmp = this.de.query();
		if (dstmp.rowCount() == 0) {
			throw new BusinessException("没有找到此人员的信息，请检查！");
		}
		empname = dstmp.getString(0, "empname");

		empds.put(0, "empno", empno);
		empds.put(0, "empname", empname);
		empds.put(0, "piid", piid);

		de.clearSql();
  		de.addSql("select distinct a.orgno, b.orgname, a.ishrbelong ");
  		de.addSql("  from odssu.ir_emp_org a, ");
  		de.addSql("       odssu.orginfor b ");
  		de.addSql(" where a.orgno = b.orgno");
  		de.addSql("   and a.empno = :empno	  ");
  		de.addSql("   and b.sleepflag = '0'	  ");
		this.de.setString("empno", empno);
		belongds = this.de.query();

		for (i = 0; i < belongds.rowCount(); i++) {
			orgno = belongds.getString(i, "orgno");
			ishrbelongflag = "";
			ishrbelongflag = belongds.getString(i, "ishrbelong");
			roleno = "";
			rolename = "";
			rolenono = "";
			rolenamename = "";
			flag = "";

			if ("1".equals(ishrbelongflag)) {
				flag = "√";
				hrorgno = orgno;
			}

			de.clearSql();
  			de.addSql("select a.roleno, b.rolename  ");
  			de.addSql("  from odssu.ir_emp_org_all_role a, ");
  			de.addSql("       odssu.roleinfor b   ");
  			de.addSql(" where a.roleno = b.roleno  ");
  			de.addSql("   and a.empno = :empno ");
  			de.addSql("   and a.orgno = :orgno ");
  			de.addSql("   and a.rolenature = '4' ");
  			de.addSql("   and b.sleepflag = '0' ");
			this.de.setString("empno", empno);
			this.de.setString("orgno", orgno);
			roleds = this.de.query();
			for (j = 0; j < roleds.rowCount(); j++) {
				roleno = roleds.getString(j, "roleno");
				rolename = roleds.getString(j, "rolename");

				rolenono = rolenono + "," + roleno;
				rolenamename = rolenamename + "," + rolename;
			}
			if (!"".equals(rolenono)) {
				rolenono = rolenono.substring(1, rolenono.length());
			}
			if (!"".equals(rolenamename)) {
				rolenamename = rolenamename.substring(1, rolenamename.length());
			}

			belongds.put(i, "roleno", rolenono);
			belongds.put(i, "rolename", rolenamename);
			belongds.put(i, "flag", flag);
		}

		// 返回
		vdo.put("belongds", belongds);
		vdo.put("roleds", roleds);
		vdo.put("empds", empds);
		vdo.put("hrorgno", hrorgno);
		return vdo;
	}

	/**
	 * 生成人员隶属关系调整工单 在工单主表中查入一条数据
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	
	private final void addNewMainGdxx(final String piid, final String empno,
			final String hrorgno) throws Exception {
		StringBuilder sb = new StringBuilder();
    		de.addSql(" insert into odssuws.rylsjgtz ");
  		de.addSql("             (piid, empno, hrbelong) ");
  		de.addSql("      values (:piid, :empno, :hrorgno) ");
		de.setString("piid", piid);
		de.setString("empno", empno);
		de.setString("hrorgno", hrorgno);
		de.update();
	}

	/**
	 * 生成人员隶属关系调整工单 工单副表，隶属机构信息表
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	
	private final void addNewOrgDetlGdxx(final String piid, final String orgno) throws Exception {
		StringBuilder sb = new StringBuilder();
    		de.addSql(" insert into odssuws.rylsjgtz_org_detl ");
  		de.addSql("             (piid, orgno, orginaccount) ");
  		de.addSql("      values (:piid, :orgno, '1') ");
		de.setString("piid", piid);
		de.setString("orgno", orgno);
		de.update();
	}

	/**
	 * 生成人员隶属关系调整工单 工单副表，隶属机构角色信息表
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	private final void addNewOrgRoleDetlGdxx(final String piid,
			final String orgno, final String roleno) throws Exception {
		StringBuilder sb = new StringBuilder();
    		de.addSql(" insert into odssuws.rylsjgtz_org_role_detl ");
  		de.addSql("             (piid, orgno,rolewithinorg, roleinaccount) ");
  		de.addSql("      values (:piid, :orgno, :roleno, '1') ");
		de.setString("piid", piid);
		de.setString("orgno", orgno);
		de.setString("roleno", roleno);
		de.update();
	}

	/**
	 * 查询工单信息
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-10-13
	 * @param piid
	 * @return
	 * @throws Exception
	 */
	public final DataObject getGdxx(String piid) throws Exception {
		DataObject rdo = DataObject.getInstance();
		DataStore gdxxds = DataStore.getInstance(), dstmp, orgds = DataStore.getInstance();
		String empno = "", empname = "", orgno, hrbelongorg, orgflag;
		String orgname, flag;

		de.clearSql();
  		de.addSql("select empno,hrbelong,null orgno,null orgname,null orgflag,null roleno, null rolename,null flag ");
  		de.addSql("  from odssuws.rylsjgtz ");
  		de.addSql(" where piid=:piid ");
		this.de.setString("piid", piid);
		gdxxds = this.de.query();
		
		if (gdxxds.rowCount() > 0) {
			empno = gdxxds.getString(0, "empno");
			hrbelongorg = gdxxds.getString(0, "hrbelong");
			if (hrbelongorg == null) {
				hrbelongorg = "";
			}

			de.clearSql();
  			de.addSql("select empname ");
  			de.addSql("  from odssu.empinfor ");
  			de.addSql(" where empno=:empno ");
			this.de.setString("empno", empno);
			dstmp = this.de.query();
			if (dstmp.rowCount() > 0) {
				empname = dstmp.getString(0, "empname");
			}
			// 查询隶属机构信息
			de.clearSql();
  			de.addSql("select a.orgno, b.orgname ,a.orgflag , ' ' jobname ,:empno  empno , :hrbelongorg  ishrbelong ,' ' flag ");
  			de.addSql("  from odssuws.rylsjgtz_org_detl a, ");
  			de.addSql("       odssu.orginfor b  ");
  			de.addSql(" where a.orgno = b.orgno ");
  			de.addSql("   and a.piid = :piid 		");
  			de.addSql(" order by orgflag desc,orgno ");
			this.de.setString("piid", piid);
			this.de.setString("empno", empno);
			this.de.setString("hrbelongorg", hrbelongorg);
			orgds = this.de.query();
			//查询账表中除了工单表中-的职务信息
			de.clearSql();
			de.addSql("select a.empno,a.jobno,a.orgno,b.jobname,b.joborder   "); 
			de.addSql("from odssu.emp_job a,odssu.jobinfor b  "); 
			de.addSql("where not exists (  ");
			de.addSql("           select *  from odssuws.emp_job c         ");
			de.addSql("           where c.opflag='(-)'   and  c.piid = :piid  and c.empno=a.empno   and  c.jobno = a.jobno and c.orgno = a.orgno)");
			de.addSql("     and a.jobno = b.jobno "); 
			de.addSql("     and  a.empno = :empno");
			this.de.setString("empno", empno);
			this.de.setString("piid", piid);
			DataStore zwzb = this.de.query();
			//查询工单表中+的职务信息
			de.clearSql();
			de.addSql("select a.empno,a.jobno,a.orgno,b.jobname,b.joborder   "); 
			de.addSql("from odssuws.emp_job a,odssu.jobinfor b  "); 
			de.addSql("where a.jobno = b.jobno "); 
			de.addSql("     and  a.empno = :empno");
			de.addSql("     and  a.piid = :piid");
			de.addSql("     and  a.opflag='(+)'");
			this.de.setString("empno", empno);
			this.de.setString("piid", piid);
			DataStore zwgdb = this.de.query();
			zwzb.combineDatastore(zwgdb);
			for(int i=0;i<zwzb.size();i++){
				String zworgno = zwzb.getString(i, "orgno");
				String zwjobname = zwzb.getString(i, "jobname");
				int index = orgds.find("orgno == "+zworgno);
				if(index>=0){
					String orgjobname = orgds.getString(index, "jobname");
					if(" ".equals(orgjobname)){
						orgds.get(index).put("jobname", zwjobname);
					}else{
						orgds.get(index).put("jobname", orgjobname+","+zwjobname);
					}
				} 
			}
			if(!("".equals(hrbelongorg))&&!(null ==hrbelongorg) ){
				int local = orgds.find("orgno == "+hrbelongorg);
				if(local>=0){
					orgds.get(local).put("flag", "√");
				}
			}
			orgds.sortdesc("flag");
			orgds.sort("orgflag");
			gdxxds = orgds;
		}
		for(DataObject tempdo : gdxxds) {
			if("(-)".equals(tempdo.getString("orgflag"))) {
				tempdo.put("gridbtn", "撤销删除");
			}else {
				tempdo.put("gridbtn", "删除");
			}
		}
		rdo.put("piid", piid);
		rdo.put("gdxxds", gdxxds);
		rdo.put("orgds", orgds);
		rdo.put("piid", piid);
		rdo.put("empno", empno);
		rdo.put("empname", empname);

		gdxxds = null;

		return rdo;
	}

	/**
	 * 刷新Grid数据
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-10-13
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject refreshGrid(DataObject para) throws Exception {
		String piid = para.getString("piid");

		DataObject vdo = getGdxx(piid);
		DataStore gdxxds = vdo.getDataStore("gdxxds");

		DataObject vdo1 = DataObject.getInstance();
		vdo1.put("gdxxds", gdxxds);
		vdo1.put("piid", piid);
		return vdo1;
	}

	/**
	 * 转向调整机构界面,查询此人拥有的机构权限
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	public final DataObject fwResOrgAdjust(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore empds = DataStore.getInstance();
		String empno, empname, piid;
		empno = para.getString("empno");
		empname = para.getString("empname");
		piid = para.getString("piid");
		empds.put(0, "piid", piid);
		empds.put(0, "empno", empno);
		empds.put(0, "empname", empname);
		vdo.put("empds", empds);
		return vdo;
	}

	/**
	 * 新增机构，查询此机构(单位)所有下级机构
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-8
	 * @param para
	 * @return
	 */
	public DataObject lovForSubOrgByOrgNo(DataObject para) throws Exception {

		String orgno = para.getString("orgno");
		orgno = ((orgno == null || "".equals(orgno)) ? "%" : "%" + orgno + "%");

		// 获取业务隶属机构编号
		BPO ibpo = this.newBPO(ProcessBPO.class);
		DataObject varvdo = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
		String lsjgid = varvdo.getString("_process_biz");
    		de.clearSql();
  		de.addSql(" select a.orgno , b.orgname ,b.displayname,b.sleepflag ");
  		de.addSql("  from odssu.ir_org_closure a , odssu.orginfor b ");
  		de.addSql(" where a.belongorgno = :lsjgid and b.orgno = a.orgno and  ");
  		de.addSql("         b.orgtype <> 'HSDOMAIN_QSRSYWJBJG'    and    ");
  		de.addSql("   ( b.orgno like :orgno or b.orgname like :orgno or b.orgnamepy like :orgno or b.displayname like :orgno  ");
  		de.addSql("        or b.displaynamepy like :orgno or b.fullname like :orgno or b.fullnamepy like :orgno ) ");
  		de.addSql("  order by b.sleepflag,a.orgno");
		this.de.setString("lsjgid", lsjgid);
		this.de.setString("orgno", orgno);

		DataStore orgds = this.de.query();

		DataObject vdo = DataObject.getInstance();

		vdo.put("orgds", orgds);
		return vdo;
	}

	/**
	 * 保存机构调整信息到工单odssu.rylsjgtz_org_detl
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	public void saveOrgAdjust(DataObject para) throws Exception {
		StringBuilder sb = new StringBuilder();
		DataStore belongds;
		String orgno, piid, orginaccount, orgflag;

		orgno = para.getString("orgno");
		piid = para.getString("piid");

		de.clearSql();
  		de.addSql("select a.orgflag, a.orginaccount  ");
  		de.addSql("  from odssuws.rylsjgtz_org_detl a ");
  		de.addSql(" where a.piid = :piid   ");
  		de.addSql("   and a.orgno = :orgno  ");
		this.de.setString("piid", piid);
		this.de.setString("orgno", orgno);
		belongds = this.de.query();

		if (belongds.rowCount() == 1) {
			orginaccount = belongds.getString(0, "orginaccount");
			orgflag = belongds.getString(0, "orgflag");
			if (orginaccount == null) {
				orginaccount = "";
			}
			if (orgflag == null) {
				orgflag = "";
			}

			if ("1".equals(orginaccount)) {// 此机构在账中存在,新增则更新其标志为空
				de.clearSql();
  				de.addSql(" update odssuws.rylsjgtz_org_detl   ");
  				de.addSql("    set orgflag =null 	");
  				de.addSql("   where piid = :piid    ");
  				de.addSql("     and orgno = :orgno   ");
  				de.addSql("     and orginaccount = '1'   ");
				this.de.setString("piid", piid);
				this.de.setString("orgno", orgno);
				this.de.update();
			} else if ("0".equals(orginaccount)) {// 此机构在账中不存在,处理同一机构新增多次的情况
				de.clearSql();
  				de.addSql(" update odssuws.rylsjgtz_org_detl   ");
  				de.addSql("    set orgflag ='(+)'	");
  				de.addSql("   where piid = :piid    ");
  				de.addSql("     and orgno = :orgno   ");
  				de.addSql("     and orginaccount = '0'   ");
				this.de.setString("piid", piid);
				this.de.setString("orgno", orgno);
				this.de.update();
			}
		} else {
			de.clearSql();
  			de.addSql(" insert into odssuws.rylsjgtz_org_detl   ");
  			de.addSql("             (piid, orgno, orgflag, orginaccount) ");
  			de.addSql("      values (:piid, :orgno, '(+)', '0')    ");
			this.de.setString("piid", piid);
			this.de.setString("orgno", orgno);
			this.de.update();
		}
	}

	/**
	 * 检查操作员操作的机构与biz是否相符
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-22
	 * @param para
	 * @return
	 */
	public final DataObject checkBljg(DataObject para) throws Exception {
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String flag = "true";

		DataObject orgvdo = DataObject.getInstance();
		orgvdo.put("piid", piid);
		orgvdo.put("orgno", "");

		orgvdo = lovForSubOrgByOrgNo(orgvdo);

		DataStore orgds = orgvdo.getDataStore("orgds");

		int j = orgds.find("orgno == " + orgno);

		if (j < 0) {
			flag = "false";
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("flag", flag);
		return vdo;
	}

	/**
	 * 删除机构
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	public final DataObject delOrg(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		StringBuilder sb = new StringBuilder();
		DataStore orginaccountds;
		String orgno, piid, orginaccount = "";

		orgno = para.getString("orgno");
		piid = para.getString("piid");

		// 判断orgno在账中是否存在
		de.clearSql();
  		de.addSql("select  orginaccount");
  		de.addSql("  from odssuws.rylsjgtz_org_detl a ");
  		de.addSql(" where a.piid = :piid      ");
  		de.addSql("   and a.orgno = :orgno      ");
		this.de.setString("piid", piid);
		this.de.setString("orgno", orgno);
		orginaccountds = this.de.query();

		if (orginaccountds.rowCount() > 0) {
			orginaccount = orginaccountds.getString(0, "orginaccount");
//			orginaccount="1";
		}
		// 判定是否为人事隶属，若是，删除人事隶属
		de.clearSql();
		de.addSql(" select 1 from odssuws.rylsjgtz where piid = :piid and hrbelong = :orgno ");
		this.de.setString("piid", piid);
		this.de.setString("orgno", orgno);

		DataStore hrorgds = this.de.query();

		if (hrorgds.rowCount() == 1) {
			de.clearSql();
			de.addSql(" update odssuws.rylsjgtz set hrbelong = null where piid = :piid  ");
			this.de.setString("piid", piid);
			int result = 0;
			result = this.de.update();
			if (result != 1) {
				this.bizException("删除人事隶属机构失败！");
			}
		}
		
		if ("1".equals(orginaccount)) {// 在账中存在
			de.clearSql();
  			de.addSql("update odssuws.rylsjgtz_org_detl ");
  			de.addSql("   set orgflag = '(-)'  ");
  			de.addSql(" where piid = :piid       ");
  			de.addSql("   and orgno = :orgno      ");
			this.de.setString("piid", piid);
			this.de.setString("orgno", orgno);
			this.de.update();
		} else {
			de.clearSql();
  			de.addSql("delete from odssuws.rylsjgtz_org_detl ");
  			de.addSql(" where piid = :piid       ");
  			de.addSql("   and orgno = :orgno      ");
			this.de.setString("piid", piid);
			this.de.setString("orgno", orgno);
			this.de.update();
		}

		return vdo;
	}

	/**
	 * 取消删除机构
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	public final DataObject delOrgAbort(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		StringBuilder sb = new StringBuilder();
		DataStore orginaccountds;
		String orgno, piid, orginaccount = "";

		orgno = para.getString("orgno");
		piid = para.getString("piid");

		// 判断orgno在账中是否存在
		de.clearSql();
  		de.addSql("select 1 ");
  		de.addSql("  from odssuws.rylsjgtz_org_detl a ");
  		de.addSql(" where a.piid = :piid      ");
  		de.addSql("   and a.orgno = :orgno      ");
		this.de.setString("piid", piid);
		this.de.setString("orgno", orgno);
		orginaccountds = this.de.query();

		if (orginaccountds.rowCount() > 0) {
			orginaccount = orginaccountds.getString(0, "orginaccount");
//			orginaccount="1";
		}
		if ("1".equals(orginaccount)) {// 在账中存在
			de.clearSql();
  			de.addSql("update odssuws.rylsjgtz_org_detl ");
  			de.addSql("   set orgflag = null  ");
  			de.addSql(" where piid = :piid       ");
  			de.addSql("   and orgno = :orgno      ");
			this.de.setString("piid", piid);
			this.de.setString("orgno", orgno);
			this.de.update();
		}

		BPO ibpo = this.newBPO(ProcessBPO.class);
		DataObject result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);

		String empno = result.getString("empno");
		de.clearSql();
  		de.addSql(" select 1 from odssu.ir_emp_org where empno = :empno and  orgno = :orgno and ishrbelong = '1' ");
		de.setString("empno", empno);
		de.setString("orgno", orgno);

		DataStore empds = de.query();

		if (empds.rowCount() > 0) {
			de.clearSql();
  			de.addSql("update odssuws.rylsjgtz  ");
  			de.addSql("   set hrbelong = :orgno  ");
  			de.addSql(" where piid = :piid       ");
			this.de.setString("orgno", orgno);
			this.de.setString("piid", piid);
			this.de.update();
		}

		return vdo;
	}

	/**
	 * 转向调整机构中角色调整界面，查询此人拥有的角色权限
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	public final DataObject fwResRoleAdjust(DataObject para) throws Exception {

		DataObject vdo = DataObject.getInstance();
		StringBuilder sb = new StringBuilder();
		DataStore roleds, empds = DataStore.getInstance();
		String empno, empname, piid, orgno, orgname;

		empno = para.getString("empno");
		empname = para.getString("empname");
		orgno = para.getString("orgno");
		orgname = para.getString("orgname");
		piid = para.getString("piid");

		de.clearSql();
  		de.addSql(" select b.roleno value, b.rolename content");
  		de.addSql("    from (select a.belongorgno    ");
  		de.addSql("            from odssu.ir_org_closure a, ");
  		de.addSql("                 odssu.orginfor e, ");
  		de.addSql("                 odssu.org_type f  ");
  		de.addSql("          where a.orgno = :orgno ");
  		de.addSql("            and a.belongorgno = e.orgno ");
  		de.addSql("            and e.orgtype = f.typeno ");
  		de.addSql("            and f.typenature <> 'A') temp,");
  		de.addSql("         odssu.roleinfor b   ");
  		de.addSql("   where b.deforgno = temp.belongorgno");
  		de.addSql("     and b.roletype in (select c.roletypeno roletype");
  		de.addSql("                          from odssu.ir_org_role_type c, odssu.orginfor d");
  		de.addSql("                         where c.orgtypeno = d.orgtype");
  		de.addSql("                           and d.orgno = :orgno)");
  		de.addSql("     and b.rolenature = '4' ");
		this.de.setString("orgno", orgno);
		roleds = this.de.query();

		empds.put(0, "piid", piid);
		empds.put(0, "empno", empno);
		empds.put(0, "empname", empname);
		empds.put(0, "orgno", orgno);
		empds.put(0, "orgname", orgname);

		vdo.put("roleds", roleds);
		vdo.put("empds", empds);
		roleds = null;
		empds = null;

		return vdo;
	}

	// lovForRoleNoOnOrg
	public DataObject lovForRoleNoOnOrg(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String roleno = para.getString("roleno");
		String roleUpperCase = roleno.toUpperCase();
		this.de.clearSql();
		this.de.addSql(" select b.roleno, b.rolename, b.displayname, b.sleepflag");
		this.de.addSql("   from (select a.belongorgno");
		this.de.addSql("           from odssu.ir_org_closure a");
		this.de.addSql("          where a.orgno = :orgno ) temp,");
		this.de.addSql("        odssu.roleinfor b");
		this.de.addSql("  where b.deforgno = temp.belongorgno");
		this.de.addSql("    and b.roletype in (select c.roletypeno roletype");
		this.de.addSql("                         from odssu.ir_org_role_type c, odssu.orginfor d");
		this.de.addSql("                        where c.orgtypeno = d.orgtype");
		this.de.addSql("                          and d.orgno = :orgno)");
		this.de.addSql("    and b.rolenature = '4'");
		this.de.addSql("    and b.sleepflag = '0'");
		this.de.addSql("    and b.roleno <>:role_memeber   ");
		this.de.addSql("    and b.jsgn in(:jsgn_sysmanage,:jsgn_post)  ");
  		this.de.addSql("    and (b.roleno like :roleno or b.rolename like :rolename or b.displayname like :displayname or");
  		this.de.addSql("        upper(b.rolenamepy) like :para6 or upper(b.displaynamepy) like :para7)");
  		this.de.addSql("  order by b.sleepflag, b.roleno ");
  		
		this.de.setString("role_memeber", OdssuContants.ROLE_MEMEBER);
		this.de.setString("jsgn_sysmanage", OdssuContants.JSGN_SYSMANAGE);
		this.de.setString("jsgn_post", OdssuContants.JSGN_POST);
		this.de.setString("orgno", orgno);
		this.de.setString("roleno", "%" + roleno + "%");
		this.de.setString("rolename", "%" + roleno + "%");
		this.de.setString("displayname", "%" + roleno + "%");
		this.de.setString("para6", "%" + roleUpperCase + "%");
		this.de.setString("para7", "%" + roleUpperCase + "%");

		DataStore roleds = this.de.query();

		DataObject vdo = DataObject.getInstance();

		vdo.put("roleds", roleds);

		return vdo;
	}

	/**
	 * 保存机构中角色调整信息到工单odssu.rylsjgtz_org_role_detl
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	public void saveRoleAdjust(DataObject para) throws Exception {
		StringBuilder sb = new StringBuilder();
		DataStore dstmp;
		String orgno, roleno, piid;
		String roleflag, roleinaccount;

		orgno = para.getString("orgno");
		roleno = para.getString("roleno");
		piid = para.getString("piid");

		de.clearSql();
  		de.addSql("select a.roleflag, a.roleinaccount ");
  		de.addSql("  from odssuws.rylsjgtz_org_role_detl a ");
  		de.addSql(" where a.piid = :piid      ");
  		de.addSql("   and a.orgno = :orgno      ");
  		de.addSql("   and a.rolewithinorg = :roleno      ");
		this.de.setString("piid", piid);
		this.de.setString("orgno", orgno);
		this.de.setString("roleno", roleno);
		dstmp = this.de.query();

		if (dstmp.rowCount() == 1) {
			roleflag = dstmp.getString(0, "roleflag");
			if (roleflag == null) {
				roleflag = "";
			}
			roleinaccount = dstmp.getString(0, "roleinaccount");
			if (roleinaccount == null) {
				roleinaccount = "";
			}

			if ("1".equals(roleinaccount)) {// 在账中存在，新增，直接更新标识为空
				de.clearSql();
  				de.addSql(" update odssuws.rylsjgtz_org_role_detl   ");
  				de.addSql("    set roleflag = null ");
  				de.addSql("  where piid = :piid      ");
  				de.addSql("    and orgno = :orgno     ");
  				de.addSql("    and rolewithinorg = :roleno    ");
  				de.addSql("    and roleinaccount = '1'    ");
				de.setString("piid", piid);
				de.setString("orgno", orgno);
				de.setString("roleno", roleno);
				de.update();
			} else if ("0".equals(roleinaccount)) {// 在账中不存在，新增了多次的情况
				de.clearSql();
  				de.addSql(" update odssuws.rylsjgtz_org_role_detl   ");
  				de.addSql("    set roleflag = '+' ");
  				de.addSql("  where piid = :piid      ");
  				de.addSql("    and orgno = :orgno     ");
  				de.addSql("    and rolewithinorg = :roleno    ");
  				de.addSql("    and roleinaccount = '0'    ");
				de.setString("piid", piid);
				de.setString("orgno", orgno);
				de.setString("roleno", roleno);
				de.update();
			}
		} else {
			de.clearSql();
  			de.addSql(" insert into odssuws.rylsjgtz_org_role_detl   ");
  			de.addSql("             (piid, orgno, rolewithinorg, roleflag, roleinaccount ) ");
  			de.addSql("      values (:piid, :orgno, :roleno, '+', '0')    ");
			de.setString("piid", piid);
			de.setString("orgno", orgno);
			de.setString("roleno", roleno);
			de.update();
		}

	}

	/**
	 * 转向删除机构中角色调整界面
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	public final DataObject fwResRoleDel(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		StringBuilder sb = new StringBuilder();
		DataStore roleds, empds = DataStore.getInstance();
		String orgno, orgname, empno, empname, piid;
		empno = para.getString("empno");
		empname = para.getString("empname");
		orgno = para.getString("orgno");
		orgname = para.getString("orgname");
		piid = para.getString("piid");

		de.clearSql();
  		de.addSql("select a.roleflag flag ,a.rolewithinorg roleno, b.rolename ");
  		de.addSql("  from odssuws.rylsjgtz_org_role_detl a, ");
  		de.addSql("       odssu.roleinfor b 	 ");
  		de.addSql(" where a.rolewithinorg = b.roleno	 ");
  		de.addSql("   and a.piid = :piid   	  		 ");
  		de.addSql("   and a.orgno = :orgno    		 ");
		this.de.setString("piid", piid);
		this.de.setString("orgno", orgno);
		roleds = this.de.query();

		empds.put(0, "piid", piid);
		empds.put(0, "empno", empno);
		empds.put(0, "empname", empname);
		empds.put(0, "orgno", orgno);
		empds.put(0, "orgname", orgname);

		vdo.put("roleds", roleds);
		vdo.put("empds", empds);

		empds = null;
		roleds = null;
		return vdo;
	}

	/**
	 * 删除机构中的角色信息
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	public void delRoleAdjust(DataObject para) throws Exception {
		StringBuilder sb = new StringBuilder();
		String orgno, flag, roleno, piid;

		piid = para.getString("piid");
		orgno = para.getString("orgno");
		roleno = para.getString("roleno");
		flag = para.getString("flag", "");

		if (!"+".equals(flag)) {// 为空说明在账中存在
			de.clearSql();
  			de.addSql("update odssuws.rylsjgtz_org_role_detl ");
  			de.addSql("   set  roleflag = '-'	");
  			de.addSql(" where piid = :piid      	");
  			de.addSql("   and orgno = :orgno 		");
  			de.addSql("   and rolewithinorg = :roleno ");
			this.de.setString("piid", piid);
			this.de.setString("orgno", orgno);
			this.de.setString("roleno", roleno);
			int result = this.de.update();

			if (result == 0) {
				de.clearSql();
  				de.addSql("insert into odssuws.rylsjgtz_org_role_detl ");
  				de.addSql("   (piid, orgno, rolewithinorg, roleflag, roleinaccount)");
  				de.addSql("  values(:piid,:orgno,:roleno,'-','1')      	");
				this.de.setString("piid", piid);
				this.de.setString("orgno", orgno);
				this.de.setString("roleno", roleno);
				this.de.update();
			}

		} else {
			de.clearSql();
  			de.addSql("delete from odssuws.rylsjgtz_org_role_detl ");
  			de.addSql(" where piid = :piid      	");
  			de.addSql("   and orgno = :orgno 		");
  			de.addSql("   and rolewithinorg = :roleno ");
			this.de.setString("piid", piid);
			this.de.setString("orgno", orgno);
			this.de.setString("roleno", roleno);
			this.de.update();
		}

	}

	/**
	 * 更新工单主表的经办人信息 方法简介.
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字 说明
	 * @return 关键字 说明
	 * @throws 异常说明 发生条件
	 * @author liuy
	 * @date 创建时间 2014-05-08
	 * @since V1.0
	 */
	public final DataObject saveEmpOrgAdjust(final DataObject para) throws Exception {
		DataObject rdo = DataObject.getInstance();
  		de.clearSql();
		String piid;
		// 接受参数
		piid = para.getString("piid");

		de.addSql(" select orgflag , orgno from odssuws.rylsjgtz_org_detl where piid = :piid ");
  		de.addSql(" order by orgflag desc ");
		de.setString("piid", piid);
		DataStore orgds = de.query();

		if (orgds.rowCount() == 0) {
			this.bizException("不存在【piid=" + piid + "】所对应的机构修改信息！");
		}

		int flag = 0;

		for (int i = 0; i < orgds.rowCount(); i++) {
			String orgflag = orgds.getString(i, "orgflag");
			if (orgflag == null || "(+)".equals(orgflag)) {
				break;
			} else {
				flag++;
			}
		}

		if (flag == orgds.rowCount()) {
			this.bizException("您已删除所有的机构，请新增机构或撤销机构删除后在进行下一步操作！！");
		}

		//检测是否勾选人事隶属机构
		DataObject temp=DataObject.getInstance();
		temp.put("piid", piid);
		checkHrBelong(temp);

		return rdo;
	}


	/**
	 * 转向指定人事隶属机构界面,查询出该人员所隶属的机构
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-08
	 * @since V1.0
	 */
	public final DataObject fwPageAssignHrbelong(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		StringBuilder sb = new StringBuilder();
		DataStore orgds, gsdstmp, empds = DataStore.getInstance();
		String orgno, piid, orgflag, belongorgno = "", empno = "", empname = "";

		piid = para.getString("piid");

		// 查询出该人所隶属的机构
		de.clearSql();
  		de.addSql("select a.orgno, b.orgname, a.orgflag 	");
  		de.addSql("  from odssuws.rylsjgtz_org_detl a,     ");
  		de.addSql("       odssu.orginfor b      ");
  		de.addSql(" where a.orgno = b.orgno	 	");
  		de.addSql("   and a.piid = :piid   	  	    ");
		this.de.setString("piid", piid);
		orgds = this.de.query();

		// 查询人事隶属机构
		de.clearSql();
  		de.addSql("select a.hrbelong belongorgno, a.empno, b.empname  	");
  		de.addSql("  from odssuws.rylsjgtz a,    ");
  		de.addSql("       odssu.empinfor b  	 ");
  		de.addSql(" where a.empno = b.empno   	 ");
  		de.addSql("   and a.piid = :piid   	    	 ");
		this.de.setString("piid", piid);
		gsdstmp = this.de.query();

		if (gsdstmp.rowCount() > 0) {
			belongorgno = gsdstmp.getString(0, "belongorgno");
			if (belongorgno == null) {
				belongorgno = "";
			}
			empno = gsdstmp.getString(0, "empno");
			empname = gsdstmp.getString(0, "empname");
		}

		// 标识出人事隶属
		for (int i = 0; i < orgds.rowCount(); i++) {
			orgflag = "";// 机构调整标识
			orgflag = orgds.getString(i, "orgflag");
			if (orgflag == null) {
				orgflag = "";
			}
			if (!"".equals(orgflag)) {
				orgflag = "(" + orgflag + ")";
			}
			orgds.put(i, "orgflag", orgflag);

			orgno = orgds.getString(i, "orgno");
			if (belongorgno.equals(orgno)) {
				orgds.put(i, "flag", "√");
			} else {
				orgds.put(i, "flag", "");
			}
		}

		empds.put(0, "piid", piid);
		empds.put(0, "empno", empno);
		empds.put(0, "empname", empname);

		vdo.put("empds", empds);
		vdo.put("orgds", orgds);

		empds = null;
		orgds = null;
		return vdo;
	}

	/**
	 * 指定人事隶属机构信息保存到工单 方法简介.
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字 说明
	 * @return 关键字 说明
	 * @throws 异常说明 发生条件
	 * @author liuy
	 * @date 创建时间 2014-05-08
	 * @since V1.0
	 */
	public final DataObject saveAssignHrBelongGd(final DataObject para) throws Exception {
		DataObject rdo = DataObject.getInstance();
  		de.clearSql();
		String piid, orgno;
		int result;

		// 接受参数
		piid = para.getString("piid");
		orgno = para.getString("orgno");

		de.clearSql();
  		de.addSql("update odssuws.rylsjgtz  ");
  		de.addSql("   set hrbelong = :orgno");
  		de.addSql(" where piid = :piid    ");
		this.de.setString("orgno", orgno); 
		this.de.setString("piid", piid);
		result = this.de.update();
		if (result < 1) {
			this.bizException("更新工单信息失败，更新记录为0条,piid=" + piid);
		}
	
		return rdo;
	}

	/**
	 *检测是否指定人事隶属机构
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-1
	 * @param para
	 * @return
	 */
	public final DataObject checkHrBelong(DataObject para) throws Exception {
		String piid = para.getString("piid");

		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空！");
		}
		de.clearSql();
		de.addSql(" select piid ,hrbelong from odssuws.rylsjgtz where piid = :piid ");
		this.de.setString("piid", piid);

		DataStore hrds = this.de.query();

		if (hrds.rowCount() == 0) {
			this.bizException("工单中不存在【piid=" + piid + "】的信息！");
		}

		String hrbelongorg = hrds.getString(0, "hrbelong");

		if (hrbelongorg == null || hrbelongorg.trim().isEmpty()) {
			this.bizException("人事隶属机构为空，请指定人事隶属机构在进行下一步操作！");
		}

		return null;
	}

	/**
	 * 转向调整审批,查询人员所隶属的机构和角色以及调整信息
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-08
	 * @since V1.0
	 */

	public final DataObject fwPageEmpOrgApproval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		StringBuilder sb = new StringBuilder();
		DataStore orgds, gddstmp, roleds, empds = DataStore.getInstance(), spds = DataStore.getInstance();
		String orgno, piid, belongorgno = "", orgflag, roleflag;
		String roleno, rolename, rolenamename, rolenono, empno = "", empname = "";

		piid = para.getString("piid");

		// 检测有没有指定人事隶属机构

		DataObject temp=DataObject.getInstance();
		temp.put("piid", piid);
		checkHrBelong(temp);

		// 查询出该人所隶属的机构
		de.clearSql();
  		de.addSql("select a.orgno, b.orgname, a.ishrbelong	");
  		de.addSql("  from odssuws.rylsjgtz_org_detl a,  ");
  		de.addSql("       odssu.orginfor b      ");
  		de.addSql(" where a.orgno = b.orgno	 	");
  		de.addSql("   and a.piid = :piid   	  	    ");
		this.de.setString("piid", piid);
		orgds = this.de.query();

		// 查询出工单中人事隶属机构
		de.clearSql();
  		de.addSql("select * 	");
  		de.addSql("  from odssuws.rylsjgtz a ,  ");
  		de.addSql("       odssu.empinfor b      ");
  		de.addSql(" where a.empno = b.empno	    ");
  		de.addSql("   and a.piid = :piid   	   		");
		this.de.setString("piid", piid);
		gddstmp = this.de.query();

		if (gddstmp.rowCount() > 0) {
			belongorgno = gddstmp.getString(0, "hrbelong");
			empno = gddstmp.getString(0, "empno");
			empname = gddstmp.getString(0, "empname");

			spds.put(0, "spr", gddstmp.getString(0, "reviewer"));
			spds.put(0, "spyj", gddstmp.getString(0, "spyj"));
			spds.put(0, "spsm", gddstmp.getString(0, "spsm"));
			spds.put(0, "sprq", gddstmp.getDate(0, "reviewtime"));
		}

		for (int i = 0; i < orgds.rowCount(); i++) {
			orgno = "";
			rolenono = "";
			rolenamename = "";
			orgflag = "";
			orgflag = orgds.getString(i, "orgflag");
			if (orgflag == null) {
				orgflag = "";
			}
			if (!"".equals(orgflag)) {
				orgflag = "(" + orgflag + ")";
			}
			orgds.put(i, "orgflag", orgflag);

			// 标识出人事隶属
			orgno = orgds.getString(i, "orgno");
			if (belongorgno.equals(orgno)) {
				orgds.put(i, "flag", "√");// flag:人事隶属机构
			} else {
				orgds.put(i, "flag", "");
			}

			// 查询出该人所隶属的机构的角色,拼成串
			de.clearSql();
  			de.addSql("select a.rolewithinorg roleno, b.rolename, a.roleflag	");
  			de.addSql("  from odssuws.rylsjgtz_org_role_detl a,  ");
  			de.addSql("       odssu.roleinfor b     ");
  			de.addSql(" where a.rolewithinorg = b.roleno 	");
  			de.addSql("   and a.piid = :piid   	  	    ");
  			de.addSql("   and a.orgno = :orgno   	    ");
			this.de.setString("piid", piid);
			this.de.setString("orgno", orgno);
			roleds = this.de.query();

			for (int j = 0; j < roleds.rowCount(); j++) {
				roleno = "";
				rolename = "";
				roleno = roleds.getString(j, "roleno");
				rolename = roleds.getString(j, "rolename");
				roleflag = roleds.getString(j, "roleflag");
				if (roleflag == null) {
					roleflag = "";
				}
				if (!"".equals(roleflag)) {
					roleflag = "(" + roleflag + ")";
				}

				rolenono = rolenono + "," + roleno + roleflag;
				rolenamename = rolenamename + "," + rolename + roleflag;
			}

			if (!"".equals(rolenono)) {
				rolenono = rolenono.substring(1, rolenono.length());
			}
			if (!"".equals(rolenamename)) {
				rolenamename = rolenamename.substring(1, rolenamename.length());
			}

			orgds.put(i, "roleno", rolenono);
			orgds.put(i, "rolename", rolenamename);
		}
		empds.put(0, "piid", piid);
		empds.put(0, "empno", empno);
		empds.put(0, "empname", empname);

		vdo.put("empds", empds);
		vdo.put("orgds", orgds);
		vdo.put("yjds", spds);
		orgds = null;
		empds = null;

		return vdo;
	}

	/**
	 * 审批通过方法 人员隶属机构调整信息保存到账中 方法简介.
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字 说明
	 * @return 关键字 说明
	 * @throws 异常说明 发生条件
	 * @author liuy
	 * @date 创建时间 2014-05-08
	 * @since V1.0
	 */
	public final DataObject saveEmpOrgApproval(final DataObject para) throws Exception {
		DataObject rdo = DataObject.getInstance();
  		de.clearSql();
		String piid, spyj, spsm, spr;
		Date sprq;
		DataStore vds;
		// 接受参数
		piid = para.getString("piid");
		spr = para.getString("spr");
		spyj = para.getString("spyj");
		spsm = para.getString("spsm");
		sprq = para.getDate("sprq");

		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql("   from odssuws.rylsjgtz  ");
  		de.addSql("  where piid = :piid  ");
		this.de.setString("piid", piid);
		vds = this.de.query();

		if (vds.rowCount() < 1) {
			throw new BusinessException("未取到工单信息" + piid);
		}

		// para.put("_user", CurrentUser.getInstance());
		// this.executeBKO(RylsjgtzBKO.class.getName(),
		// "saveEmpOrgApproval",para);

		// 工单记录记账标识
		de.clearSql();
  		de.addSql(" update odssuws.rylsjgtz  ");
  		de.addSql("    set hrbelongjzbz = '1',");
  		de.addSql("        reviewer = :spr, reviewtime = :sprq , spyj = :spyj , spsm = :spsm ");
  		de.addSql("  where piid = :piid  ");
		this.de.setString("spr", spr);
		this.de.setDateTime("sprq", sprq);
		this.de.setString("spyj", spyj);
		this.de.setString("spsm", spsm);
		this.de.setString("piid", piid);
		this.de.update();
		
		// 保存一条公共审批
		de.clearSql();
  		de.addSql("delete from odssuws.spinfor ");
  		de.addSql("  where piid = :piid and splbdm = :splbdm");
		this.de.setString("piid", piid);
		this.de.setString("splbdm", "ryfz");
		this.de.update();
		
		String spyjdm = "pass";
		if (spyj.equals("1")) {
			spyjdm = "reject";
		}else if (spyj.equals("2")) {
			spyjdm = "revise";
		}
		
		de.clearSql();
  		de.addSql("insert into odssuws.spinfor (piid,splbdm,spyjdm,spr,spsj,spsm)");
  		de.addSql("  values (:piid,:para2,:spyjdm,:para4,:sprq,:spsm)");
		this.de.setString("piid", piid);
		this.de.setString("para2", "ryfz");
		this.de.setString("spyjdm", spyjdm);
		this.de.setString("para4", this.getUser().getUserid());
		this.de.setDateTime("sprq", sprq);
		this.de.setString("spsm", spsm);
		this.de.update();

		return rdo;
	}
	public DataObject getTzzwOp (DataObject para) throws AppException{
		String empname = para.getString("empname","");
		String orgname = para.getString("orgname","");
		String empno = para.getString("empno","");
		String orgno = para.getString("orgno","");
		String piid = para.getString("piid","");
		DataStore jbxxds = DataStore.getInstance();
		jbxxds.put(0, "empname", empname);
		jbxxds.put(0, "orgname", orgname);
		jbxxds.put(0, "empno", empno);
		jbxxds.put(0, "orgno", orgno);
		jbxxds.put(0, "piid", piid);
		DataObject result = DataObject.getInstance();
		result.put("jbxxds", jbxxds);
		
		DE de = DE.getInstance();
		de.clearSql();
		//已拥有的职务信息
		//账表中拥有的职务
		de.addSql("select a.jobno,a.jobname,a.jobdetail,a.joborder,b.orgno,b.empno    ");
		de.addSql("from   odssu.jobinfor  a , odssu.emp_job   b   ");
		de.addSql("where b.orgno=:orgno  ");
		de.addSql("     and b.empno =:empno    ");
		de.addSql("     and  a.jobno = b.jobno ");
		de.addSql("     and  not EXISTS ( ");
		de.addSql("          select 1  from  odssuws.emp_job  c   ");
		de.addSql("          where  c.piid = :piid     ");
		de.addSql("                 and   c.empno  = :empno     ");
		de.addSql("                 and   c.orgno  = :orgno    ");
		de.addSql("                 and  c.opflag = '(-)'  )    ");
		de.addSql("    order by a.joborder      ");
		de.setString("piid", piid);
		de.setString("empno", empno);
		de.setString("orgno", orgno);
		DataStore zbds = de.query();
		
		de.clearSql();
		//工单表中的职务信息
		de.addSql("select a.jobno,a.jobname,a.jobdetail,a.joborder ,b.orgno,b.empno   ");
		de.addSql("from  odssu.jobinfor  a , odssuws.emp_job   b   ");
		de.addSql("where b.orgno=:orgno  ");
		de.addSql("     and b.empno =:empno    ");
		de.addSql("     and  a.jobno = b.jobno ");
		de.addSql("     and  b.piid = :piid  ");
		de.addSql("     and  b.opflag = '(+)'   ");
		de.addSql("order  by  a.joborder    ");
		de.setString("piid", piid);
		de.setString("empno", empno);
		de.setString("orgno", orgno);
		DataStore gdbds = de.query();
		zbds.combineDatastore(gdbds);
		
		//全部的职务信息
		de.clearSql();
		de.addSql("select  a.jobno,a.jobname,a.jobdetail,a.joborder  from  odssu.jobinfor  a    order by a.joborder");
		DataStore alljobinfor  = de.query();
		
		DataStore jobds  = getSelectRowROle(alljobinfor,zbds);
		result.put("jobds", jobds);
		return result;
		
	}
	public DataStore getSelectRowROle(DataStore jobdsbeforedel,DataStore jobchoosedds) throws AppException{
		DataStore jobds = DataStore.getInstance();
		for(int i =0 ;i<jobdsbeforedel.size();i++){
			DataObject vds = jobdsbeforedel.get(i);
			String jobno1 = jobdsbeforedel.getString(i, "jobno");
			vds.put("_row_selected", false);
			for (int j=0;j<jobchoosedds.size();j++){
				String jobno2 = jobchoosedds.getString(j, "jobno");
				if(jobno1.equals(jobno2)){
					vds.put("_row_selected", true);
					break;
				} 
			}
			jobds.addRow(vds);
		}
		jobds.sortdesc("_row_selected");
		return jobds;
	}
	
	/**
	 * 删除职务信息
	 * @param para
	 * @throws AppException
	 * @throws BusinessException
	 */
	//删除职务信息
	public void delJob(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String jobno = para.getString("jobno");
		String empno = para.getString("empno");
		DE de = DE.getInstance();
		DataStore ds = opflagflag(piid,empno,orgno,jobno);
		//如果工单表中有数据，证明是新增的，直接删除；如果工单表中无数据，证明是账表中的删除，所以新增一条为-的数据
		if(ds.isEmpty()){
			de.clearSql();
			de.addSql("insert  into  odssuws.emp_job(piid,empno,jobno,orgno,opflag) ");
			de.addSql("                               values(:piid, :empno , :jobno ,:orgno, '(-)'  )");
			de.setString("piid", piid);
			de.setString("orgno", orgno);
			de.setString("jobno", jobno);
			de.setString("empno", empno);
			de.update();
		}else{
			de.clearSql();
			de.addSql("delete from odssuws.emp_job ");
			de.addSql("where piid = :piid   and orgno = :orgno  and jobno = :jobno  and empno = :empno");
			de.setString("piid", piid);
			de.setString("orgno", orgno);
			de.setString("jobno", jobno);
			de.setString("empno", empno);
			de.update();
		}
	}
	/**
	 * 添加职务信息
	 * @param para
	 * @throws AppException
	 * @throws BusinessException
	 */
	//添加职务信息
	public void addJob(DataObject para) throws AppException, BusinessException{
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String empno = para.getString("empno");
		String jobno = para.getString("jobno");
		DE de = DE.getInstance();
		DataStore ds = opflagflag(piid,empno,orgno,jobno);
		//如果数据是原有被删除的，则将符号设置为空，如果数据是没有的，新添加的，则将符号设置为+
		if(ds.isEmpty()){
			de.clearSql();
			de.addSql("insert  into  odssuws.emp_job(piid,empno,jobno,orgno,opflag) ");
			de.addSql("                               values(:piid, :empno , :jobno ,:orgno, '(+)'  )");
			de.setString("piid", piid);
			de.setString("orgno", orgno);
			de.setString("jobno", jobno);
			de.setString("empno", empno);
			de.update();
		}else{
			de.clearSql();
			de.addSql("delete  from     odssuws.emp_job   ");
			de.addSql("where piid = :piid   and orgno = :orgno  and jobno = :jobno  and empno = :empno");
			de.setString("piid", piid);
			de.setString("orgno", orgno);
			de.setString("jobno", jobno);
			de.setString("empno", empno);
			de.update();
		}
		
	}
	
	/**
	 * 查看工单表中的标记，根据标记判断直接添加，还是修改标记
	 * @param piid
	 * @param orgno
	 * @param roleno
	 * @return
	 * @throws AppException
	 */
	//判断是删除后再添加，还是直接添加
	public DataStore opflagflag(String piid,String empno,String orgno,String jobno) throws AppException{
		DE de =DE.getInstance();
		de.clearSql();
		de.addSql("select * from odssuws.emp_job where piid = :piid  and orgno = :orgno    and   empno = :empno ");
		if(""!=jobno  && null!=jobno){
			de.addSql("   and  jobno = :jobno");
			de.setString("jobno",jobno);
		}
		de.setString("piid",piid);
		de.setString("orgno",orgno);
		de.setString("empno",empno);
		DataStore ds = de.query();
		return ds;
		
	}

}