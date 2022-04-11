package com.dw.hsuods.ws.emp.EmpAdjustOrg;


import java.util.Date;
import com.dareway.apps.process.util.ProcessUtil;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

public class OdsRylsjgtzBPO extends BPO{
	public final DataObject fwPageEmpOrgAdjust(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), rdo;
		DataStore gdxxds, empds = DataStore.getInstance();
		String piid;

		// 流程开始获取piid
		piid = para.getString("piid");
		
		String empno = (String)ProcessUtil.getTEEVarByPiid(piid, "empno");
		String empname = (String)ProcessUtil.getTEEVarByPiid(piid, "empname");
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");

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
		DataStore gdxxds = DataStore.getInstance(), orgds = DataStore.getInstance();
		String empno = "",  hrbelongorg;

		de.clearSql();
  		de.addSql("select empno,hrbelong,null orgno,null orgname,null orgflag,null flag ");
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
			// 查询隶属机构信息
			de.clearSql();
  			de.addSql("select a.orgno,b.orgname ,a.orgflag , ' ' jobname ,' ' flag ");
  			de.addSql("  from odssuws.rylsjgtz_org_detl a, ");
  			de.addSql("       odssu.orginfor b  ");
  			de.addSql(" where a.orgno = b.orgno ");
  			de.addSql("   and a.piid = :piid 		");
  			de.addSql(" and b.sleepflag = '0' ");
  			de.addSql(" order by orgflag desc,orgno ");
			this.de.setString("piid", piid);
			orgds = this.de.query();
			//查询账表中除了工单表中-的职务信息
			de.clearSql();
			de.addSql("select a.empno,a.jobno,a.orgno,b.jobname,b.joborder   "); 
			de.addSql("from odssu.emp_job a,odssu.jobinfor b  "); 
			de.addSql("where not exists (  ");
			de.addSql("           select *  from odssuws.emp_job c         ");
			de.addSql("           where c.opflag='(-)'   and  c.piid = :piid  and c.empno=a.empno   ");
			de.addSql("                                  and  c.jobno = a.jobno and c.orgno = a.orgno) ");
			de.addSql("     and a.jobno = b.jobno "); 
			de.addSql("     and  a.empno = :empno");
			this.de.setString("empno", empno);
			this.de.setString("piid", piid);
			DataStore zwzb = this.de.query();
			//查询工单表中+的职务信息
			de.clearSql();
			de.addSql("select a.empno,a.jobno,a.orgno,b.jobname,b.joborder   "); 
			de.addSql("   from odssuws.emp_job a,odssu.jobinfor b  "); 
			de.addSql("     where a.jobno = b.jobno "); 
			de.addSql("        and  a.empno = :empno");
			de.addSql("        and  a.piid = :piid");
			de.addSql("        and  a.opflag='(+)'");
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
		rdo.put("gdxxds", gdxxds);		
		return rdo;
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

		// 判断orgno在账中是否存在
		de.clearSql();
  		de.addSql("select  orginaccount");
  		de.addSql("  from odssuws.rylsjgtz_org_detl a ");
  		de.addSql("     where a.piid = :piid      ");
  		de.addSql("         and a.orgno = :orgno      ");
		this.de.setString("piid", piid);
		this.de.setString("orgno", orgno);
		orginaccountds = this.de.query();

		if (orginaccountds.rowCount() > 0) {
			orginaccount = orginaccountds.getString(0, "orginaccount");
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
		DataStore orginaccountds;
		String orgno, piid, orginaccount = "";

		orgno = para.getString("orgno");
		piid = para.getString("piid");

		// 判断orgno在账中是否存在
		de.clearSql();
  		de.addSql("select orginaccount ");
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
		String empno = (String)ProcessUtil.getTEEVarByPiid(piid, "empno");

		de.clearSql();
  		de.addSql(" select 1 from odssu.ir_emp_org  ");
		de.addSql("     where empno = :empno and  orgno = :orgno and ishrbelong = '1' ");
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
		de.addSql("select a.orgno, b.orgname ,a.orgflag , ' ' jobname ,' ' flag ");
		de.addSql("  from odssuws.rylsjgtz_org_detl a, ");
		de.addSql("       odssu.orginfor b  ");
		de.addSql(" where a.orgno = b.orgno ");
		de.addSql("   and a.piid = :piid 		");
		de.addSql("  AND (a.orgflag is NULL or a.orgflag ='(+)') 		");
		de.addSql(" and b.sleepflag = '0' ");
		de.addSql(" order by orgflag desc,orgno ");
		this.de.setString("piid", piid);
		orgds = this.de.query();
		//查询账表中除了工单表中-的职务信息
		de.clearSql();
		de.addSql("select a.empno,a.jobno,a.orgno,b.jobname,b.joborder ,' ' opflag  "); 
		de.addSql("from odssu.emp_job a,odssu.jobinfor b  "); 
		de.addSql("where not exists (  ");
		de.addSql("           select *  from odssuws.emp_job c         ");
		de.addSql("           where c.opflag='(-)'   and  c.piid = :piid  and c.empno=a.empno   ");
		de.addSql("                                  and  c.jobno = a.jobno and c.orgno = a.orgno ) ");
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
		vdo.put("orgds", orgds);

		return vdo;
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
		DataStore orgds, gddstmp,empds = DataStore.getInstance();
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
		de.addSql("select a.orgno, b.orgname ,a.orgflag , ' ' jobname ,' ' flag ");
		de.addSql("  from odssuws.rylsjgtz_org_detl a, ");
		de.addSql("       odssu.orginfor b  ");
		de.addSql(" where a.orgno = b.orgno ");
		de.addSql("   and a.piid = :piid 		");
		de.addSql("  AND (a.orgflag is NULL or a.orgflag ='(+)') 		");
		de.addSql(" and b.sleepflag = '0' ");
		de.addSql(" order by orgflag desc,orgno ");
		this.de.setString("piid", piid);

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
		de.addSql("           select c.empno,c.jobno,c.orgno,c.opflag  from odssuws.emp_job c         ");
		de.addSql("           where c.opflag='(-)'   and  c.piid = :piid  and c.empno=a.empno   ");
		de.addSql("                                  and  c.jobno = a.jobno and c.orgno = a.orgno )");
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
		vdo.put("gdbxx", gdbxx);

		return vdo;
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
		de.addSql("select a.hrbelong   from odssuws.rylsjgtz  a  ");
		de.addSql("   where a.piid = :piid ");
		de.setString("piid", piid);
		DataStore ds = de.query();
		DataObject result = DataObject.getInstance();
		result.put("newbelong", ds.getString(0, "hrbelong"));
		return result;
		
	}
	
	/**
	 * 判断隶属机构是否有修改
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject getOrgnameByOrgno(final DataObject para) throws Exception {
		DataObject rdo = DataObject.getInstance();
  		String newbelong = para.getString("newbelong", "");
  		String neworgname = "";
  		
  		DE de = DE.getInstance();
		de.clearSql();
		de.addSql("select orgname from odssu.orginfor where orgno = :orgno");
		de.addSql(" and sleepflag = '0' ");
		de.setString("orgno", newbelong);
		DataStore vds = de.query();
		
		neworgname = vds.getString(0, "orgname");
  		rdo.put("neworgname", neworgname);
		return rdo;
	}
	
	/**
	 * 通过机构no获取机构名称
	 * @param orgno
	 * @return
	 * @throws AppException
	 */
//	public String getOrgnameByOrgno(String orgno) throws AppException{
//		DE de = DE.getInstance();
//		de.clearSql();
//		de.addSql("select orgname from odssu.orginfor where orgno = :orgno");
//		de.addSql(" and sleepflag = '0' ");
//		de.setString("orgno", orgno);
//		DataStore vds = de.query();
//		String orgname = vds.getString(0, "orgname");
//		return orgname;
//	}
//	
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
	
}
