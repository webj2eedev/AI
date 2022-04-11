package com.dw.odssu.ws.emp.ryzsxmxg;

import java.util.Date;

import com.dareway.apps.process.util.ProcessUtil;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;

/**
 * 人员修改真实姓名类描述
 * 
 * @author liuy
 * @version 1.0 创建时间 2014-05-07
 */
public final class RyzsxmxgBPO extends BPO{
	/**
	 * 跳转进入修改真实姓名界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-20
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageRnameEditApply(DataObject para) throws Exception {

		// 流程开始获取piid
		String piid = para.getString("piid");
		String userid = this.getUser().getUserid();
		Date sysdate = DateUtil.getDBTime();

		// 查询工单信息
		DataObject rdo = getGdxx(piid);
		DataStore gdxxds = rdo.getDataStore("gdxxds");

		String empno = (String) ProcessUtil.getTEEVarByPiid(piid, "empno");

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
		String rname = vds.getString(0, "rname");
		String empname = vds.getString(0, "empname");
		if (empname == null || empname.trim().isEmpty()) {
			this.bizException("编号为【" + empno + "】的人员标识姓名为空！");
		}
		if (rname == null || rname.trim().isEmpty()) {
			this.bizException("编号为【" + empno + "】的人员真实姓名为空！");
		}

		// 如果无工单，创建工单
		if (gdxxds.rowCount() == 0) {
			// 创建工单表
			de.clearSql();
  			de.addSql("insert into odssuws.ryzsxmxg ");
  			de.addSql("  ( piid, empno, oldrname, operator, operationtime) ");
  			de.addSql(" values(:piid, :empno, :rname, :userid, :sysdate) ");
			this.de.setString("piid", piid);
			this.de.setString("empno", empno);
			this.de.setString("rname", rname);
			this.de.setString("userid", userid);
			this.de.setDateTime("sysdate", sysdate);
			this.de.update();

			gdxxds.put(0, "piid", piid);
			gdxxds.put(0, "empno", empno);
			gdxxds.put(0, "oldrname", rname);
			gdxxds.put(0, "empname", empname);
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("gdxxds", gdxxds);
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
  		de.addSql("select piid,empno,newrname,oldrname,empname,reviewer,reviewtime,spyj,spsm ");
  		de.addSql("  from odssuws.ryzsxmxg ");
  		de.addSql(" where piid=:piid ");
		this.de.setString("piid", piid);
		gdxxds = this.de.query();

		rdo.put("gdxxds", gdxxds);
		gdxxds = null;

		return rdo;
	}

	/**
	 * 录入新真实姓名界面,点击下一步的操作
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-17
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveRnameEditInWS(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();
		String piid;

		piid = para.getString("piid");
		String newrname = para.getString("newrname");
		String empname = para.getString("empname");
		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("保存修改信息到工单时，工单编号为空！");
		}
		if (newrname == null || newrname.trim().isEmpty()) {
			this.bizException("保存修改信息到工单时，newname为空！");
		}
		// 更新工单操作人信息
		de.clearSql();
  		de.addSql(" update odssuws.ryzsxmxg ");
  		de.addSql("    set newrname = :newrname,empname = :empname, reviewer =' ',reviewtime = null,spyj = ' ',spsm = ' '");
  		de.addSql(" where piid = :piid ");
		this.de.setString("newrname", newrname);
		this.de.setString("empname", empname);
		this.de.setString("piid", piid);
		int result = this.de.update();

		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}

		return vdo;
	}

	/**
	 * 跳转到确定人员标识名称任务界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-17
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageEmpName(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore empds, empnameds, empinfods = DataStore.getInstance();
		String piid, rname = "", empname = "", empno = "";
  		de.clearSql();

		piid = para.getString("piid");

		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from odssuws.ryzsxmxg  ");
  		de.addSql(" where piid = :piid		  ");
		this.de.setString("piid", piid);
		empds = this.de.query();
		if (empds.rowCount() > 0) {
			rname = empds.getString(0, "newrname");
			empno = empds.getString(0, "empno");
			empname = empds.getString(0, "empname");
			if ("".equals(empname) || empname == null) {
				empname = rname;
			}
		}

		de.clearSql();
  		de.addSql("select empno,loginname username, rname, empname	");
  		de.addSql("  from odssu.empinfor  ");
  		de.addSql(" where rname = :rname and empno != :empno ");
		this.de.setString("rname", rname);
		this.de.setString("empno", empno);
		empnameds = this.de.query();
		empinfods.put(0, "empname", empname);
		empinfods.put(0, "newrname", rname);
		empinfods.put(0, "piid", piid);
		vdo.put("empds", empinfods);
		vdo.put("empnameds", empnameds);

		return vdo;
	}

	/**
	 * 保存新增人员的标识姓名
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-16
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveEmpName(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String piid, empname;
  		de.clearSql();
		String flag = "true";

		piid = para.getString("piid");
		empname = para.getString("empname");
		String empno = (String)ProcessUtil.getTEEVarByPiid(piid, "empno");
		// 判空
		if (empname == null || empname.trim().isEmpty()) {
			this.bizException("人员标识姓名不能为空，保存失败！");
		}

		de.clearSql();
  		de.addSql(" select 1  from odssu.empinfor where empname = :empname and empno <> :empno ");
		this.de.setString("empname", empname);
		this.de.setString("empno", empno);
		DataStore empds = this.de.query();

		if (empds.rowCount() != 0) {
			flag = "repeat";
			vdo.put("flag", flag);
			return vdo;
		}

		// 保存到工单表
		de.clearSql();
  		de.addSql(" update odssuws.ryzsxmxg ");
  		de.addSql("    set empname =:empname ");
  		de.addSql(" where piid = :piid ");
		this.de.setString("empname", empname);
		this.de.setString("piid", piid);
		int result = this.de.update();

		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}
		vdo.put("flag", flag);

		return vdo;
	}

	/**
	 * 查询同名信息
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-2
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject queryRepeatResult(DataObject para) throws Exception {
		String empname = para.getString("empname");

		if (empname == null || empname.trim().isEmpty()) {
			this.bizException("empname 为空！");
		}
  		de.clearSql();
  		de.addSql(" select empno , empname , rname  from odssu.empinfor where empname = :empname ");
		this.de.setString("empname", empname);

		DataStore empds = this.de.query();

		DataObject vdo = DataObject.getInstance();

		vdo.put("empnameds", empds);

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
	public final DataObject queryRname4Approval(DataObject para) throws Exception {
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
		if(gdxxds == null || gdxxds.rowCount() == 0){
			this.bizException("获取工单编号为【" + piid + "】的工单相关信息时出错！");
		}
		String empno = gdxxds.getString(0, "empno");
		if (empno == null || empno.trim().isEmpty()) {
			this.bizException("跳转到审批界面时，从工单中获取的empno为空！");
		}
  		de.clearSql();
  		de.addSql(" select empname from odssu.empinfor where empno = :empno ");
		de.setString("empno", empno);
		DataStore vds = de.query();
		if(vds == null || vds.rowCount() == 0){
			this.bizException("获取人员编号为【" + empno + "】的人员相关信息时出错！");
		}
		String empname = vds.getString(0, "empname");
		if (empname == null || empname.trim().isEmpty()) {
			this.bizException("编号为【" + empno + "】的人员标识姓名为空！");
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

		gdxxds.put(0, "oldempname", empname);
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
		String piid, spyj, spsm, spr,approve = "";
		Date sprq;

		piid = para.getString("piid");
		spyj = para.getString("spyj");
		spsm = para.getString("spsm", "");
		spr = para.getString("spr");
		sprq = para.getDate("sprq");
		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空！");
		}

		// 保存审批意见
		de.clearSql();
  		de.addSql(" update odssuws.ryzsxmxg ");
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
  		    		de.clearSql();
  		    		de.addSql("select spyj from odssuws.ryzsxmxg where piid=:piid");
		this.de.setString("piid", piid);
		DataStore ds = this.de.query();

		if (ds.rowCount() > 0) {
			spyj = ds.getString(0, "spyj");
		}
		if ("2".equals(spyj)) {
			approve = "sendback";
		} else if ("0".equals(spyj)) {
			approve = "pass";
		} else if ("1".equals(spyj)) {
			approve = "nopass";
		}
		ProcessUtil.setTEEVarByPiid(piid, "approve", approve);

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
