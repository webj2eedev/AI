package com.dw.hsuods.ws.emp.rylsjgtz;

import java.util.Date;

import com.dareway.apps.process.ProcessBPO;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

/**
 * 人员隶属关系调整 类描述
 * 
 * @author liu
 * @version 1.0 创建时间 2014-05-07
 */
public final class HsuOdsRylsjgtzBPO extends BPO{
	/**
	 * 比较此次流程是否有角色信息发生变化，有一处发生变化即可return；
	 * 若此次流程无任何变动，则弹出businessException，提醒用户。
	 * wjn
     */
	public DataObject checkChange(DataObject para) throws AppException,BusinessException{
		String piid = para.getString("piid");

		de.clearSql();
		de.addSql("select 1 "); 
		de.addSql("from odssuws.rylsjgtz_org_detl a  "); 
		de.addSql("where a.orgflag is not null "); 
		de.addSql("  and a.piid = :piid");
		de.setString("piid", piid);
		DataStore jgds = de.query();
		if (jgds != null && jgds.rowCount() > 0) {
			return null;
		}

		de.clearSql();
		de.addSql("select 1  "); 
		de.addSql("from odssuws.emp_job a  "); 
		de.addSql("where a.opflag is not null "); 
		de.addSql("  and a.piid = :piid");
		de.setString("piid", piid);
		DataStore zwgdb = de.query();
		if (zwgdb != null && zwgdb.rowCount() > 0) {
			return null;
		}
		
		de.clearSql();
		de.addSql("select empno, hrbelong  "); 
		de.addSql("from odssuws.rylsjgtz  "); 
		de.addSql("where piid = :piid");
		de.setString("piid", piid);
		DataStore rslsds = de.query();
		if (rslsds == null || rslsds.rowCount() == 0) {
			bizException("根据流程ID"+piid+"未获取到操作员信息");
		}else {
			String empno = rslsds.getString(0, "empno");
			String hrbelong = rslsds.getString(0, "hrbelong");
			
			de.clearSql();
			de.addSql("select hrbelong  "); 
			de.addSql("from odssu.empinfor  "); 
			de.addSql("where empno = :empno");
			de.setString("empno", empno);
			DataStore yrslsds = de.query();
			if (yrslsds == null || yrslsds.rowCount() == 0) {
				bizException("根据操作员ID"+empno+"未获取到操作员信息");
			}else {
				String yhrbelong = yrslsds.getString(0, "hrbelong");
				if(!yhrbelong.equals(hrbelong)) {
					return null;
				}
			}
		}
		
		throw new BusinessException("您的本次操作没有对操作员的隶属机构进行调整，建议您作废此流程或对操作员的隶属机构调整后，再提交审批。");
	}
	public final DataObject fwPageEmpOrgAdjust(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), result = DataObject.getInstance(), rdo;
		DataStore gdxxds, empds = DataStore.getInstance(), belongds;
		String piid, hrorgno = "", orgno;
		String empno, empname;

		// 流程开始获取piid
		piid = para.getString("piid");

		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");
		empno = rdo.getString("empno", "");
		empname = rdo.getString("empname", "");

		empds.put(0, "piid", piid);
		empds.put(0, "empno", empno);
		empds.put(0, "empname", empname);
		String userName = OdssuUtil.getUserNameByEmpno(empno);
		empds.put(0, "username", userName);
		vdo.put("empds", empds);
		vdo.put("gdxxds", gdxxds);

		return vdo;
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
		de.clearSql();
		de.addSql(" insert into odssuws.rylsjgtz_org_detl ");
  		de.addSql("             (piid, orgno) ");
  		de.addSql("      values (:piid, :orgno) ");
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
		de.clearSql();
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
		String empno = "", empname = "",  hrbelongorg;

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
  			de.addSql("select a.orgno, a.ishrbelong,b.orgname ,a.orgflag , ' ' jobname ,:empno  empno , :hrbelongorg  hrbelongorg ,' ' flag ");
  			de.addSql("  from odssuws.rylsjgtz_org_detl a, ");
  			de.addSql("       odssu.orginfor b  ");
  			de.addSql(" where a.orgno = b.orgno ");
  			de.addSql("   and a.piid = :piid 		");
  			de.addSql(" and b.sleepflag = '0' ");
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
		String orgnoUpperCase = orgno.toUpperCase();
		orgnoUpperCase = ((orgnoUpperCase == null || "".equals(orgnoUpperCase)) ? "%" : "%" + orgnoUpperCase + "%");
		orgno = ((orgno == null || "".equals(orgno)) ? "%" : "%" + orgno + "%");

		// 获取业务隶属机构编号
		BPO ibpo = this.newBPO(ProcessBPO.class);
		DataObject varvdo = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
		String lsjgid = varvdo.getString("_process_biz");
		de.clearSql();
  		de.addSql(" select a.orgno orgno , b.orgname ,b.displayname,b.sleepflag ");
  		de.addSql("  from odssu.ir_org_closure a , odssu.orginfor b,odssu.org_type c ");
  		de.addSql(" where a.belongorgno = :lsjgid  and b.orgno = a.orgno and  ");
  		de.addSql("         b.orgtype <> 'HSDOMAIN_RSYWJBJG'    and    ");
  		de.addSql("   ( b.orgno like :orgno or b.orgname like :orgno or upper(b.orgnamepy) like :orgnouppercase or b.displayname like :orgno  ");
  		de.addSql("        or upper(b.displaynamepy) like :orgnouppercase or b.fullname like :orgno or upper(b.fullnamepy) like :orgnouppercase ) ");
  		de.addSql("    and b.orgtype = c.typeno ");
  		de.addSql(" and b.sleepflag = '0' ");
		de.addSql("    and c.typenature <> 'A' ");
  		de.addSql("  order by b.sleepflag,a.orgno");
		this.de.setString("lsjgid", lsjgid);
		this.de.setString("orgno", orgno);
		this.de.setString("orgnouppercase", orgnoUpperCase);

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
  				de.addSql("    set orgflag = null 	");
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
		DataStore orginaccountds;
		String orgno, piid, orginaccount = "";

		orgno = para.getString("orgno");
		piid = para.getString("piid");

//		DataObject orgvdo = DataObject.getInstance();
//		orgvdo.put("piid", piid);
//		orgvdo.put("orgno", "");
//
//		orgvdo = lovForSubOrgByOrgNo(orgvdo);
//
//		DataStore orgds = orgvdo.getDataStore("orgds");
//
//		int j = orgds.find("orgno == " + orgno);
//
//		if (j < 0) {
//			this.bizException("您所修改的机构不是当前业务隶属机构的下级机构,请选择符合条件的机构进行相关操作！");
//		}
		// 判断orgno在账中是否存在
		de.clearSql();
  		de.addSql("select a.orginaccount ");
  		de.addSql("  from odssuws.rylsjgtz_org_detl a ");
  		de.addSql(" where a.piid = :piid      ");
  		de.addSql("   and a.orgno = :orgno      ");
		this.de.setString("piid", piid);
		this.de.setString("orgno", orgno);
		orginaccountds = this.de.query();

		if (orginaccountds.rowCount() > 0) {
			orginaccount = orginaccountds.getString(0, "orginaccount");
		}
		if ("1".equals(orginaccount)) {// 在账中存在
			de.clearSql();
  			de.addSql("update odssuws.rylsjgtz_org_detl ");
  			de.addSql("   set orgflag = '-'  ");
  			de.addSql(" where piid = :piid       ");
  			de.addSql("   and orgno = :orgno      ");
			this.de.setString("piid", piid);
			this.de.setString("orgno", orgno);
			this.de.update();
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
		DataStore orginaccountds;
		String orgno, piid, orginaccount = "";

		orgno = para.getString("orgno");
		piid = para.getString("piid");

		// 判断orgno在账中是否存在
		de.clearSql();
  		de.addSql("select a.orginaccount ");
  		de.addSql("  from odssuws.rylsjgtz_org_detl a ");
  		de.addSql(" where a.piid = :piid      ");
  		de.addSql("   and a.orgno = :orgno      ");
		this.de.setString("piid", piid);
		this.de.setString("orgno", orgno);
		orginaccountds = this.de.query();

		if (orginaccountds.rowCount() > 0) {
			orginaccount = orginaccountds.getString(0, "orginaccount");
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
  			de.addSql("   set ishrbelongorg = :orgno  ");
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
//	public final DataObject fwResRoleAdjust(DataObject para) throws Exception {
//
//		DataObject vdo = DataObject.getInstance();
//		StringBuilder sb = new StringBuilder();
//		DataStore roleds, empds = DataStore.getInstance();
//		String empno, empname, piid, orgno, orgname;
//
//		empno = para.getString("empno");
//		empname = para.getString("empname");
//		orgno = para.getString("orgno");
//		orgname = para.getString("orgname");
//		piid = para.getString("piid");
//
//		DataObject orgvdo = DataObject.getInstance();
//		orgvdo.put("piid", piid);
//		orgvdo.put("orgno", "");
//
//		orgvdo = lovForSubOrgByOrgNo(orgvdo);
//
//		DataStore orgds = orgvdo.getDataStore("orgds");
//
//		int j = orgds.find("orgno == " + orgno);
//
//		if (j < 0) {
//			this.bizException("您所修改的机构不是当前业务隶属机构的下级机构,请选择符合条件的机构进行相关操作！");
//		}
//		empds.put(0, "piid", piid);
//		empds.put(0, "empno", empno);
//		empds.put(0, "empname", empname);
//		empds.put(0, "orgno", orgno);
//		empds.put(0, "orgname", orgname);
//
//		vdo.put("roleds", roleds);
//		vdo.put("empds", empds);
//		roleds = null;
//		empds = null;
//
//		return vdo;
//	}

	// lovForRoleNoOnOrg
//	public DataObject lovForRoleNoOnOrg(DataObject para) throws Exception {
//		String orgno = para.getString("orgno");
//		String roleno = para.getString("roleno");
//		String roleUpperCase = roleno.toUpperCase();
//		StringBuffer sqlStr = new StringBuffer();
//
//		this.sql.setSql(sqlStr.toString());
//		this.sql.setString(1, orgno);
//		this.sql.setString(2, orgno);
//		this.sql.setString(3, "%" + roleno + "%");
//		this.sql.setString(4, "%" + roleno + "%");
//		this.sql.setString(5, "%" + roleno + "%");
//		this.sql.setString(6, "%" + roleUpperCase + "%");
//		this.sql.setString(7, "%" + roleUpperCase + "%");
//
//		DataStore roleds = this.sql.executeQuery();
//
//		DataObject vdo = DataObject.getInstance();
//
//		vdo.put("roleds", roleds);
//
//		return vdo;
//	}

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
		DataStore roleds, empds = DataStore.getInstance();
		String orgno, orgname, empno, empname, piid;
		empno = para.getString("empno");
		empname = para.getString("empname");
		orgno = para.getString("orgno");
		orgname = para.getString("orgname");
		piid = para.getString("piid");

//		DataObject orgvdo = DataObject.getInstance();
//		orgvdo.put("piid", piid);
//		orgvdo.put("orgno", "");
//
//		orgvdo = lovForSubOrgByOrgNo(orgvdo);
//
//		DataStore orgds = orgvdo.getDataStore("orgds");
//
//		int j = orgds.find("orgno == " + orgno);
//
//		if (j < 0) {
//			this.bizException("您所修改的机构不是当前业务隶属机构的下级机构,请选择符合条件的机构进行相关操作！");
//		}

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

//		this.checkChange(piid);
		// 检测是否删除了全部机构，若已删除全部机构报错

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
			if (orgflag == null || "+".equals(orgflag)) {
				break;
			} else {
				flag++;
			}
		}

		if (flag == orgds.rowCount()) {
			this.bizException("您已删除所有的机构，请新增机构或撤销机构删除后在进行下一步操作！！");
		}

		de.clearSql();
  		de.addSql("update odssuws.rylsjgtz  ");
  		de.addSql("   set reviewer = null,reviewtime = null,spyj = null,spsm = null  ");
  		de.addSql(" where piid = :piid    ");
		this.de.setString("piid", piid);
		int result = this.de.update();
		if (result < 1) {
			this.bizException("更新工单信息失败，更新记录为0条,piid=" + piid);
		}

		return rdo;
	}

	/**
	 * 调整职务
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwHsuOdsRylsjgTzzw(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore orgds, gsdstmp, empds = DataStore.getInstance();
		String  piid, belongorgno = "", empno = "", empname = "";

		piid = para.getString("piid");

		// 查询人事隶属机构
		de.clearSql();
  		de.addSql("select a.hrbelong, a.empno, b.empname  	");
  		de.addSql("  from odssuws.rylsjgtz a,    ");
  		de.addSql("       odssu.empinfor b  	 ");
  		de.addSql(" where a.empno = b.empno   	 ");
  		de.addSql("   and a.piid = :piid   	    	 ");
		this.de.setString("piid", piid);
		gsdstmp = this.de.query();
		if (gsdstmp.rowCount() > 0) {
			belongorgno = gsdstmp.getString(0, "hrbelong");
			if (belongorgno == null) {
				belongorgno = "";
			}
			empno = gsdstmp.getString(0, "empno");
			empname = gsdstmp.getString(0, "empname");
		}
		
		// 查询隶属机构信息
		de.clearSql();
		de.addSql("select a.orgno, b.orgname ,a.orgflag , ' ' jobname ,:empno  empno ,a.ishrbelong,:belongorgno  hrbelong,' ' flag ");
		de.addSql("  from odssuws.rylsjgtz_org_detl a, ");
		de.addSql("       odssu.orginfor b  ");
		de.addSql(" where a.orgno = b.orgno ");
		de.addSql("   and a.piid = :piid 		");
		de.addSql("  AND (a.orgflag is NULL or a.orgflag ='(+)') 		");
		de.addSql(" and b.sleepflag = '0' ");
		de.addSql(" order by orgflag desc,orgno ");
		this.de.setString("piid", piid);
		this.de.setString("empno", empno);
		this.de.setString("belongorgno", belongorgno);
		orgds = this.de.query();
		//查询账表中除了工单表中-的职务信息
		de.clearSql();
		de.addSql("select a.empno,a.jobno,a.orgno,b.jobname,b.joborder ,' ' opflag  "); 
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
		de.addSql("select a.empno,a.jobno,a.orgno,b.jobname,b.joborder ,a.opflag  "); 
		de.addSql("from odssuws.emp_job a,odssu.jobinfor b  "); 
		de.addSql("where a.jobno = b.jobno "); 
		de.addSql("     and  a.empno = :empno");
		de.addSql("     and  a.piid = :piid");
		this.de.setString("empno", empno);
		this.de.setString("piid", piid);
		DataStore zwgdb = this.de.query();
		zwzb.combineDatastore(zwgdb);
		
		for(int i=0;i<zwzb.size();i++){
			String zworgno = zwzb.getString(i, "orgno");
			String zwjobname = zwzb.getString(i, "jobname");
			String opflag = zwzb.getString(i, "opflag");
			int index = orgds.find("orgno == "+zworgno);
			if("(-)".equals(opflag)){
				zwjobname = zwjobname+"(-)";
			}else if("(+)".equals(opflag)){
				zwjobname = zwjobname+"(+)";
			}
			if(index>=0){
				String orgjobname = orgds.getString(index, "jobname");
				if(" ".equals(orgjobname)){
					orgds.get(index).put("jobname", zwjobname);
				}else{
					orgds.get(index).put("jobname", orgjobname+","+zwjobname);
				}
			} 
		}
		if(!("".equals(belongorgno))&&!(null ==belongorgno) ){
			int local = orgds.find("orgno == "+belongorgno);
			if(local>=0){
				orgds.get(local).put("flag", "√");
			}
		}
		orgds.sortdesc("flag");
		orgds.sort("orgflag");
		empds.put(0, "empno", empno);
		empds.put(0, "empname", empname);
		empds.put(0, "username", OdssuUtil.getUserNameByEmpno(empno));
		vdo.put("empds", empds);
		vdo.put("oldbelong", belongorgno);
		vdo.put("orgds", orgds);
		empds = null;
		orgds = null;
		return vdo;
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
	public final DataObject fwHsuOdsRylsjgTzZdls(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore orgds, gsdstmp, empds = DataStore.getInstance();
		String  piid, belongorgno = "", empno = "", empname = "";

		piid = para.getString("piid");
		
		// 查询人事隶属机构
		de.clearSql();
  		de.addSql("select a.hrbelong, a.empno, b.empname  	");
  		de.addSql("  from odssuws.rylsjgtz a,    ");
  		de.addSql("       odssu.empinfor b  	 ");
  		de.addSql(" where a.empno = b.empno   	 ");
  		de.addSql("   and a.piid = :piid   	    	 ");
		this.de.setString("piid", piid);
		gsdstmp = this.de.query();
		if (gsdstmp.rowCount() > 0) {
			belongorgno = gsdstmp.getString(0, "hrbelong");
			if (belongorgno == null) {
				belongorgno = "";
			}
			empno = gsdstmp.getString(0, "empno");
			empname = gsdstmp.getString(0, "empname");
		}
		
		
		// 查询隶属机构信息
		de.clearSql();
		de.addSql("select a.orgno, b.orgname ,a.orgflag , ' ' jobname ,:empno  empno ,a.ishrbelong, :hrbelong hrbelong,' ' flag ");
		de.addSql("  from odssuws.rylsjgtz_org_detl a, ");
		de.addSql("       odssu.orginfor b  ");
		de.addSql(" where a.orgno = b.orgno ");
		de.addSql("   and a.piid = :piid 		");
		de.addSql("  AND (a.orgflag is NULL or a.orgflag ='(+)') 		");
		de.addSql(" and b.sleepflag = '0' ");
		de.addSql(" order by orgflag desc,orgno ");
		this.de.setString("piid", piid);
		this.de.setString("empno", empno);
		this.de.setString("hrbelong", belongorgno);

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
		if(!("".equals(belongorgno))&&!(null ==belongorgno) ){
			int local = orgds.find("orgno == "+belongorgno);
			if(local>=0){
				orgds.get(local).put("flag", "√");
			}
		}
		
		orgds.sortdesc("flag");
		orgds.sort("orgflag");
		empds.put(0, "empno", empno);
		empds.put(0, "empname", empname);
		empds.put(0, "username", OdssuUtil.getUserNameByEmpno(empno));
		vdo.put("empds", empds);
		vdo.put("oldbelong", belongorgno);
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
  		de.addSql("   set ishrbelongorg = :orgno, spyj = null , spsm = null , reviewer = null, reviewtime = null ");
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
		DataStore orgds, gddstmp,empds = DataStore.getInstance(), spds = DataStore.getInstance();
		String piid, belongorgno = "", empno = "", empname = "";

		piid = para.getString("piid");

		// 检测有没有指定人事隶属机构
		de.clearSql();
		de.addSql(" select hrbelong, empno , piid ");
  		de.addSql("  from   odssuws.rylsjgtz ");
  		de.addSql(" where    piid = :piid  ");
		de.setString("piid", piid);

		DataStore hrds = de.query();

		if (hrds.rowCount() == 0) {
			this.bizException(" 工单表中不存在【piid=" + piid + "】的数据！");
		}

		String hrorg = hrds.getString(0, "hrbelong");
		
		if (hrorg == null || hrorg.trim().isEmpty()) {
			this.bizException("请指定人员的人事隶属机构再进行下一步操作！！");
		}
		// 查询出工单中人事隶属机构
		de.clearSql();
  		de.addSql("select a.hrbelong ,b.empno, b.empname");
  		de.addSql("  from odssuws.rylsjgtz a ,  ");
  		de.addSql("       odssu.empinfor b      ");
  		de.addSql(" where a.empno = b.empno	    ");
  		de.addSql("   and a.piid = :piid   	   		");
		this.de.setString("piid", piid);
		gddstmp = this.de.query();

		if (gddstmp.rowCount() > 0) {
			belongorgno=gddstmp.getString(0, "hrbelong");
			empno = gddstmp.getString(0, "empno");
			empname = gddstmp.getString(0, "empname");
		}
		
		// 查询隶属机构信息
		de.clearSql();
		de.addSql("select a.orgno, b.orgname ,a.orgflag , ' ' jobname ,:empno  empno ,a.ishrbelong,:hrbelong hrbelong,' ' flag ");
		de.addSql("  from odssuws.rylsjgtz_org_detl a, ");
		de.addSql("       odssu.orginfor b  ");
		de.addSql(" where a.orgno = b.orgno ");
		de.addSql("   and a.piid = :piid 		");
		de.addSql("  AND (a.orgflag is NULL or a.orgflag ='(+)') 		");
		de.addSql(" and b.sleepflag = '0' ");
		de.addSql(" order by orgflag desc,orgno ");
		this.de.setString("piid", piid);
		this.de.setString("empno", empno);
		this.de.setString("hrbelong", belongorgno);

		orgds = this.de.query();
		if(!("".equals(belongorgno))&&!(null ==belongorgno) ){
			int local = orgds.find("orgno == "+belongorgno);
			if(local>=0){
				orgds.get(local).put("flag", "√");
			}
		}
		DataStore gdbxx = orgds;
		//查询账表中除了工单表中-的职务信息
		de.clearSql();
		de.addSql("select a.empno,a.jobno,a.orgno,b.jobname,b.joborder,' '  opflag   "); 
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
		de.addSql("select a.empno,a.jobno,a.orgno,b.jobname,b.joborder,a.opflag   "); 
		de.addSql("from odssuws.emp_job a,odssu.jobinfor b  "); 
		de.addSql("where a.jobno = b.jobno "); 
		de.addSql("     and  a.empno = :empno");
		de.addSql("     and  a.piid = :piid");
		this.de.setString("empno", empno);
		this.de.setString("piid", piid);
		DataStore zwgdb = this.de.query();
		zwzb.combineDatastore(zwgdb);
		for(int i=0;i<zwzb.size();i++){
			String zworgno = zwzb.getString(i, "orgno");
			String zwjobname = zwzb.getString(i, "jobname");
			String opflag=zwzb.getString(i, "opflag");
			if("(-)".equals(opflag)){
				zwjobname = zwjobname+"(-)";
			}else if("(+)".equals(opflag)){
				zwjobname = zwjobname+"(+)";
			}
			int index = gdbxx.find("orgno == "+zworgno);
			if(index>=0){
				String orgjobname = gdbxx.getString(index, "jobname");
				if(" ".equals(orgjobname)){
					gdbxx.get(index).put("jobname", zwjobname);
				}else{
					gdbxx.get(index).put("jobname", orgjobname+","+zwjobname);
				}
			} 
		}
		if(!("".equals(belongorgno))&&!(null ==belongorgno) ){
			int local1 = gdbxx.find("orgno == "+belongorgno);
			if(local1>=0){
				gdbxx.get(local1).put("flag", "√");
			}
		}
		gdbxx.sortdesc("flag");
		gdbxx.sort("orgflag");
		DataStore gdds = DataStore.getInstance();
		for(int i=0;i<gdbxx.size();i++){
			DataObject show= gdbxx.getRow(i);
			String orgflag1 = gdbxx.getString(i, "orgflag");
			if(!("(-)".equals(orgflag1))){
				gdds.addRow(show);
			}
		}
		gdbxx=gdds;
		orgds.sortdesc("flag");
		orgds.sort("orgflag");
		empds.put(0, "piid", piid);
		empds.put(0, "empno", empno);
		empds.put(0, "empname", empname);
		empds.put(0, "username", OdssuUtil.getUserNameByEmpno(empno));
		vdo.put("empds", empds);
		vdo.put("orgds", orgds);
		vdo.put("yjds", spds);
		vdo.put("gdbxx", gdbxx);
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
		this.de.setString("splbdm", "HsuOdsLsjgTz");
		this.de.update();
		
		String spyjdm = "pass";
		if (spyj.equals("1")) {
			spyjdm = "reject";
		}else if (spyj.equals("2")) {
			spyjdm = "revise";
		}
		
		de.clearSql();
  		de.addSql("insert into odssuws.spinfor (piid,splbdm,spyjdm,spr,spsj,spsm)");
  		de.addSql("  values (:piid,:splbdm,:spyjdm,:spr,:sprq,:spsm)");
		this.de.setString("piid", piid);
		this.de.setString("splbdm", "HsuOdsLsjgTz");
		this.de.setString("spyjdm", spyjdm);
		this.de.setString("spr", this.getUser().getUserid());
		this.de.setDateTime("sprq", sprq);
		this.de.setString("spsm", spsm);
		this.de.update();
				
		return rdo;
	}
	/**
	 * 判断隶属机构是否有修改
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject isChange(final DataObject para) throws Exception {
		DataObject rdo = DataObject.getInstance();
  		String oldbelong = para.getString("oldbelong", "");
  		String newbelong = para.getString("newbelong", "");
  		String oldorgname ="";
  		String neworgname = "";
  		rdo.put("ischange", "0");
  		if(!(oldbelong.equals(newbelong))&&(String)oldbelong!=""&&(String)newbelong!=""){
  			rdo.put("ischange", "1");
  			oldorgname = getOrgnameByOrgno(oldbelong);
  			neworgname = getOrgnameByOrgno(newbelong);
  		} 
  		rdo.put("oldorgname", oldorgname);
  		rdo.put("neworgname", neworgname);
		return rdo;
	}
	/**
	 * 通过机构no获取机构名称
	 * @param orgno
	 * @return
	 * @throws AppException
	 */
	public String getOrgnameByOrgno(String orgno) throws AppException{
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql("select orgname from odssu.orginfor where orgno = :orgno");
		de.addSql(" and sleepflag = '0' ");
		de.setString("orgno", orgno);
		DataStore vds = de.query();
		String orgname = vds.getString(0, "orgname");
		return orgname;
	}
	/**
	 * 得到原有隶属机构和修改之后的隶属机构信息
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject getChangeBelong(DataObject para) throws AppException{
		String piid = para.getString("piid");
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql("select a.hrbelong   from odssuws.rylsjgtz  a  where a.piid = :piid ");
		de.setString("piid", piid);
		DataStore ds = de.query();
		DataObject result = DataObject.getInstance();
//		result.put("oldbelong", ds.getString(0, "oldbelong"));
		result.put("newbelong", ds.getString(0, "hrbelong"));
		return result;
		
	}
}