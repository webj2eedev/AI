package com.dw.odssu.ws.emp.rybzxmxg;

import java.util.Date;

import com.dareway.apps.process.ProcessBPO;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;

/**
 * 人员修改标识姓名类描述
 * 
 * @author liuy
 * @version 1.0 创建时间 2014-05-07
 */
public final class RybzxmxgBPO extends BPO{
	/**
	 * 跳转进入修改标识姓名界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-20
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageEmpNameApply(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), result = DataObject.getInstance(), rdo;
		DataStore gdxxds = DataStore.getInstance();
  		de.clearSql();
		String piid, empno, empname;
		String userid = this.getUser().getUserid();
		Date sysdate = DateUtil.getDBTime();

		// 流程开始获取piid
		piid = para.getString("piid");

		// 查询工单信息
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");

		// 如果无工单，创建工单
		if (gdxxds.rowCount() == 0) {
			para.put("piid", piid);
			BPO ibpo = this.newBPO(ProcessBPO.class);
			result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
			
			empno = result.getString("empno");
			if (empno == null || empno.trim().isEmpty()) {
				this.bizException("从流程中获取人员编号时人员编号为空！");
			}

			de.clearSql();
  			de.addSql(" select empname,rname from odssu.empinfor where empno = :empno ");
			de.setString("empno", empno);
			DataStore vds = de.query();
			if (vds.rowCount() == 0) {
				this.bizException("编号为【" + empno + "】的人员信息不存在！");
			}
			empname = vds.getString(0, "empname");
			if (empname == null || empname.trim().isEmpty()) {
				this.bizException("编号为【" + empno + "】的人员标识姓名为空！");
			}

			// 创建工单表
			de.clearSql();
  			de.addSql("insert into odssuws.rybzxmxg ");
  			de.addSql("  ( piid, empno, oldempname, operator, operationtime) ");
  			de.addSql(" values(:piid, :empno, :empname, :userid, :sysdate) ");
			this.de.setString("piid", piid);
			this.de.setString("empno", empno);
			this.de.setString("empname", empname);
			this.de.setString("userid", userid);
			this.de.setDateTime("sysdate", sysdate);
			this.de.update();

			gdxxds.put(0, "piid", piid);
			gdxxds.put(0, "empno", empno);
			gdxxds.put(0, "oldempname", empname);
		} else {
			empno = gdxxds.getString(0, "empno");
			if (empno == null || empno.trim().isEmpty()) {
				this.bizException("从工单中获取人员编号时人员编号为空！");
			}

			de.clearSql();
  			de.addSql(" select empname,rname from odssu.empinfor where empno = :empno ");
			de.setString("empno", empno);
			DataStore vds = de.query();
			if(vds == null || vds.rowCount() == 0){
				this.bizException("获取编号为【" + empno + "】的人员标识姓名时出错！");
			}
			empname = vds.getString(0, "empname");
			if (empname == null || empname.trim().isEmpty()) {
				this.bizException("编号为【" + empno + "】的人员标识姓名为空！");
			}

		}
		de.clearSql();
  		de.addSql("select empno, rname, empname	");
  		de.addSql("  from odssu.empinfor  ");
  		de.addSql(" where empname = :empname and empno != :empno ");
		this.de.setString("empname", empname);
		this.de.setString("empno", empno);
		DataStore empnameds = this.de.query();

		vdo.put("empds", gdxxds);
		vdo.put("empnameds", empnameds);
		return vdo;
	}

	/**
	 * 查询工单信息 .
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-20
	 * @param piid
	 * @return
	 * @throws Exception
	 */
	public final DataObject getGdxx(String piid) throws Exception {
		DataObject rdo = DataObject.getInstance();
		DataStore gdxxds = DataStore.getInstance();
  		de.clearSql();

		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from odssuws.rybzxmxg ");
  		de.addSql(" where piid=:piid ");
		this.de.setString("piid", piid);
		gdxxds = this.de.query();

		rdo.put("gdxxds", gdxxds);
		gdxxds = null;

		return rdo;
	}

	/**
	 * 录入新标识姓名界面,点击下一步的操作
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-17
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveEmpnameEditInWS(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();
		String piid;
		String flag = "true";

		piid = para.getString("piid");
		String empno = para.getString("empno");
		String newempname = para.getString("newempname");
		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("保存修改信息到工单时，工单编号为空！");
		}
		if (newempname == null || newempname.trim().isEmpty()) {
			this.bizException("保存修改信息到工单时，newname为空！");
		}
		
		de.clearSql();
  		de.addSql(" select 1  from odssu.empinfor where empname = :newempname and empno != :empno ");
		this.de.setString("newempname", newempname);
		this.de.setString("empno", empno);
		
		DataStore empds = this.de.query();
		
		if(empds.rowCount() != 0 ){
			flag = "repeat";
			vdo.put("flag", flag);
			return vdo;
		}
		// 更新工单操作人信息
		de.clearSql();
  		de.addSql(" update odssuws.rybzxmxg ");
  		de.addSql("    set newempname = :newempname, reviewer =null ,reviewtime = null,spyj = null,spsm = null");
  		de.addSql(" where piid = :piid ");
		this.de.setString("newempname", newempname);
		this.de.setString("piid", piid);
		int result = this.de.update();

		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}
		vdo.put("flag", flag);
		return vdo;
	}

	/**
	 * 跳转到审批界面的查询
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-20
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageEmpnameApproval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), rdo;
		DataStore gdxxds = DataStore.getInstance();
		String piid;
		// 流程开始获取piid
		piid = para.getString("piid");
		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("跳转到审批界面时，工单编号为空！");
		}

		// 查询工单信息
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");

		String empno = gdxxds.getString(0, "empno");
		if (empno == null || empno.trim().isEmpty()) {
			this.bizException("跳转到审批界面时，从工单中获取的empno为空！");
		}
		String spr = gdxxds.getString(0, "reviewer");
		String sprq = gdxxds.getDateToString(0, "reviewtime", "yyyy-mm-dd");
		String spyj = gdxxds.getString(0, "spyj");
		String spsm = gdxxds.getString(0, "spsm");

		DataStore yjds = DataStore.getInstance();
		yjds.put(0, "spyj", spyj);
		yjds.put(0, "spsm", spsm);
		yjds.put(0, "spr", spr);
		yjds.put(0, "sprq", sprq);
		vdo.put("gdxxds", gdxxds);
		vdo.put("yjds", yjds);

		return vdo;
	}

	/**
	 * 审批新增机构界面，点击暂存的操作
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-11
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveApproved(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();
		String piid, spyj, spsm, spr;
		Date sprq;

		piid = para.getString("piid");
		spyj = para.getString("spyj");
		spsm = para.getString("spsm", "");
		spr = para.getString("spr");
		sprq = para.getDate("sprq");
		
		if(piid == null || piid.trim().isEmpty()){
			this.bizException("piid为空！");
		}
		
		// 保存审批意见
		de.clearSql();
  		de.addSql(" update odssuws.rybzxmxg ");
  		de.addSql("    set spyj = :spyj  ,spsm = :spsm,reviewer = :spr,reviewtime  =:sprq   ");
  		de.addSql("  where piid = :piid 	         ");
		this.de.setString("spyj", spyj);
		this.de.setString("spsm", spsm);
		this.de.setString("spr", spr);
		this.de.setDateTime("sprq", sprq);
		this.de.setString("piid", piid);
		int result2 = this.de.update();

		if (result2 == 0) {
			this.bizException("将审批意见更新到工单表中时出错，请联系开发人员！");
		}

		// 保存一条公共审批
		de.clearSql();
  		de.addSql("delete from odssuws.spinfor ");
  		de.addSql("  where piid = :piid and splbdm = :splbdm");
		this.de.setString("piid", piid);
		this.de.setString("splbdm", "rylz");
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
		this.de.setString("para2", "rylz");
		this.de.setString("spyjdm", spyjdm);
		this.de.setString("para4", this.getUser().getUserid());
		this.de.setDateTime("sprq", sprq);
		this.de.setString("spsm", spsm);
		this.de.update();
		
		return vdo;
	}

}
