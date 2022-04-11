package com.dw.odssu.ws.emp.rylz;

import java.util.Date;

import com.dareway.apps.process.ProcessBPO;
import com.dareway.apps.process.util.ProcessUtil;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;

/**
 * 人员离职 类描述
 * 
 * @author liuy
 * @version 1.0 创建时间 2014-05-12
 */
public final class RylzBPO extends BPO{

	/**
	 * 跳转、查询 到办理人员离职节点
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-21
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageEmpResignApply(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), result = DataObject.getInstance(), rdo;
		DataStore gdxxds;
		String piid, empno;
		String userid = this.getUser().getUserid();
		Date sysdate = DateUtil.getDBTime();
  		DE de = DE.getInstance();

		// 流程开始获取piid
		piid = para.getString("piid");
		ProcessUtil.setTEEVarByPiid(piid, "managerno", userid);

		// 查询工单信息
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");

		// 如果无工单，创建工单
		if (gdxxds.rowCount() == 0) {
			para.put("piid", piid);
			BPO ibpo = this.newBPO(ProcessBPO.class);
			result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
			
			empno = result.getString("empno");


			// 创建工单表
			de.clearSql();
  			de.addSql("insert into odssuws.rylz ");
  			de.addSql("  (piid, empno, operator, operationtime) ");
  			de.addSql(" values(:piid, :empno, :userid, :sysdate ) ");
			de.setString("piid", piid);
			de.setString("empno", empno);
			de.setString("userid", userid);
			de.setDateTime("sysdate", sysdate);
			de.update();
		} else {
			empno = gdxxds.getString(0, "empno");
		}

		de.clearSql();
  		de.addSql(" select e.empno,e.empname,e.rname,e.loginname username,e.OFFICETEL,e.MPHONE,e.EMAIL, ");
  		de.addSql("        decode(e.sleepflag,'0','在职','1','离职') sleepflag, ");
  		de.addSql("        decode(e.gender,'1','男','2','女') gender, ");
  		de.addSql("        o.orgno,o.orgname,o.displayname,e.idcardno, ");
  		de.addSql(":piid piid");
  		de.addSql(" from   odssu.empinfor e, ");
  		de.addSql("        odssu.orginfor o ");
  		de.addSql(" where  e.hrbelong = o.orgno and empno = :empno ");
		de.setString("piid", piid);
		de.setString("empno", empno);


		DataStore vdsemp = de.query();
		if (vdsemp.rowCount() == 0) {
			this.bizException("编号为【" + empno + "】的人员信息不存在！");
		}

		vdo.put("empds", vdsemp);

		return vdo;
	}

	/**
	 * 查询工单信息 .
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-21
	 * @param piid
	 * @return
	 * @throws Exception
	 */
	public final DataObject getGdxx(String piid) throws Exception {
		DataObject rdo = DataObject.getInstance();
		DataStore gdxxds = DataStore.getInstance(), dstmp;
		String empno = "", empname = "";
  		DE de = DE.getInstance();

		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from odssuws.rylz ");
  		de.addSql(" where piid=:piid ");
		de.setString("piid", piid);
		gdxxds = de.query();

		if (gdxxds.rowCount() > 0) {
			empno = gdxxds.getString(0, "empno");

			de.clearSql();
  			de.addSql("select empname ");
  			de.addSql("  from odssu.empinfor ");
  			de.addSql(" where empno=:empno ");
			de.setString("empno", empno);
			dstmp = de.query();
			if (dstmp.rowCount() > 0) {
				empname = dstmp.getString(0, "empname");
			}
			gdxxds.put(0, "empname", empname);
		}

		rdo.put("gdxxds", gdxxds);
		gdxxds = null;

		return rdo;
	}

	/**
	 * 保存新离职信息到工单中
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-21
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveEmpResignInWS(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String piid, empno;
  		DE de = DE.getInstance();
		int result;

		// 流程开始获取piid
		piid = para.getString("piid");
		empno = para.getString("empno");
		if ("".equals(empno) || empno == null) {
			throw new Exception("人员编号为空！");
		}
		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("保存离职信息到工单时，工单编号为空！");
		}

		// 更新工单表
		de.clearSql();
  		de.addSql("update odssuws.rylz ");
  		de.addSql("   set reviewer =null,reviewtime = null,spyj = null,spsm = null ");
  		de.addSql(" where piid = :piid        ");
		de.setString("piid", piid);
		result = de.update();
		if (result == 0) {
			throw new Exception("工单信息更新失败！");
		}

		return vdo;
	}

	/**
	 * 跳转、查询 第二个节点 跳到审批界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-21
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageEmpResignApporval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), rdo;
		DataStore gdxxds = DataStore.getInstance();
		String piid;
		DE de = DE.getInstance();
		// 流程开始获取piid
		piid = para.getString("piid");

		// 查询工单信息
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");

		String empno = gdxxds.getString(0, "empno");
    	de.clearSql();
  		de.addSql(" select e.empno,e.empname,e.rname,e.loginname username,e.OFFICETEL,e.MPHONE,e.EMAIL, ");
  		de.addSql("        decode(e.sleepflag,'0','在职','1','离职') sleepflag, ");
  		de.addSql("        decode(e.gender,'1','男','2','女') gender, ");
  		de.addSql("        o.orgno,o.orgname,o.displayname,e.idcardno, ");
  		de.addSql("        :piid piid     ");
  		de.addSql(" from   odssu.empinfor e, ");
  		de.addSql("        odssu.orginfor o ");
  		de.addSql(" where  e.hrbelong = o.orgno and empno = :empno ");
		de.setString("piid", piid);
		de.setString("empno", empno);

		DataStore vdsemp = de.query();
		if (vdsemp.rowCount() == 0) {
			this.bizException("编号为【" + empno + "】的人员信息不存在！");
		}
		
		DataStore yjds  = DataStore.getInstance();
		String spr = gdxxds.getString(0, "reviewer");
		String sprq = gdxxds.getDateToString(0, "reviewtime", "yyyy-mm-dd hh:mm:ss");
		String spyj = gdxxds.getString(0, "spyj");
		String spsm = gdxxds.getString(0, "spsm");
		
		yjds.put(0, "spyj", spyj);
		yjds.put(0, "spsm", spsm);
		yjds.put(0, "spr", spr);
		yjds.put(0, "sprq", sprq);

		vdo.put("empds", vdsemp);
		vdo.put("yjds", yjds);
		return vdo;
	}

	/**
	 * 审批通过、记账
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-21
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveEmpResignInASO(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		DE de = DE.getInstance();
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
  		de.addSql(" update odssuws.rylz ");
  		de.addSql("    set spyj = :spyj  ,spsm = :spsm,reviewer = :spr,reviewtime  =:sprq   ");
  		de.addSql("  where piid = :piid 	         ");
		de.setString("spyj", spyj);
		de.setString("spsm", spsm);
		de.setString("spr", spr);
		de.setDateTime("sprq", sprq);
		de.setString("piid", piid);
		
		int result2 = de.update();

		if (result2 == 0) {
			this.bizException("将审批意见更新到工单表中时出错，请联系开发人员！");
		}
		
		// 保存一条公共审批
		de.clearSql();
  		de.addSql("delete from odssuws.spinfor ");
  		de.addSql("  where piid = :piid and splbdm = :splbdm");
		de.setString("piid", piid);
		de.setString("splbdm", "rylz");
		de.update();
		
		String spyjdm = "pass";
		if (spyj.equals("1")) {
			spyjdm = "reject";
		}else if (spyj.equals("2")) {
			spyjdm = "revise";
		}
		
		de.clearSql();
  		de.addSql("insert into odssuws.spinfor (piid,splbdm,spyjdm,spr,spsj,spsm)");
  		de.addSql("  values (:piid,:para2,:spyjdm,:para4,:sprq,:spsm)");
		de.setString("piid", piid);
		de.setString("para2", "rylz");
		de.setString("spyjdm", spyjdm);
		de.setString("para4", this.getUser().getUserid());
		de.setDateTime("sprq", sprq);
		de.setString("spsm", spsm);
		de.update();

		return vdo;
	}

}
