package com.dw.odssu.ws.emp.rysfzhxg;

import java.util.Date;

import com.dareway.apps.process.ProcessBPO;
import com.dareway.apps.process.util.ProcessUtil;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.IdcardnoTool;
import com.dw.util.OdssuUtil;

/**
 * 人员登录名修改 类描述
 * 
 * @author liuy
 * @version 1.0 创建时间 2014-05-12
 */
public final class RysfzhxgBPO extends BPO{

	/**
	 * 跳转到输入要修改的身份证号任务界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-16
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageAdjustIdcard(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), result = DataObject.getInstance(), rdo;
		DataStore gdxxds, dstmp;
		String piid, empno, idcardno = "", empname;
		String userid = this.getUser().getUserid();
		Date sysdate = DateUtil.getDBTime();
  		de.clearSql();

		// 流程开始获取piid
		piid = para.getString("piid");

		// 查询工单信息
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");
		empno = (String) ProcessUtil.getTEEVarByPiid(piid, "empno");
		empname = OdssuUtil.getEmpNameByEmpno(empno);

		// 如果无工单，创建工单
		if (gdxxds.rowCount() == 0) {
			para.put("piid", piid);
			BPO ibpo = this.newBPO(ProcessBPO.class);
			result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);

			empno = result.getString("empno");
			empname = result.getString("empname");

			de.clearSql();
  			de.addSql("select idcardno ");
  			de.addSql("  from odssu.empinfor ");
  			de.addSql(" where empno=:empno   ");
			this.de.setString("empno", empno);
			dstmp = this.de.query();
			if (dstmp.rowCount() > 0) {
				idcardno = dstmp.getString(0, "idcardno");
			}

			gdxxds.put(0, "piid", piid);
			gdxxds.put(0, "empno", empno);
			gdxxds.put(0, "empname", empname);
			gdxxds.put(0, "oldidcardno", idcardno);

			// 创建工单表
			de.clearSql();
  			de.addSql("insert into odssuws.rysfzhxg ");
  			de.addSql("  (piid, empno, oldidcardno, operator, operationtime) ");
  			de.addSql(" values(:piid, :empno ,:idcardno, :userid, :sysdate) ");
			this.de.setString("piid", piid);
			this.de.setString("empno", empno);
			this.de.setString("idcardno", idcardno);
			this.de.setString("userid", userid);
			this.de.setDateTime("sysdate", sysdate);
			this.de.update();
		}

		vdo.put("empname", empname);
		vdo.put("gdxxds", gdxxds);

		return vdo;
	}

	/**
	 * 查询工单信息
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-16
	 * @param piid
	 * @return
	 * @throws Exception
	 */
	public final DataObject getGdxx(String piid) throws Exception {
		DataObject rdo = DataObject.getInstance();
		DataStore gdxxds = DataStore.getInstance(), dstmp;
		String empno = "", empname = "";
  		de.clearSql();

		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from odssuws.rysfzhxg ");
  		de.addSql(" where piid=:piid ");
		this.de.setString("piid", piid);
		gdxxds = this.de.query();

		if (gdxxds.rowCount() > 0) {
			empno = gdxxds.getString(0, "empno");

			de.clearSql();
  			de.addSql("select empname ");
  			de.addSql("  from odssu.empinfor ");
  			de.addSql(" where empno=:empno ");
			this.de.setString("empno", empno);
			dstmp = this.de.query();
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
	 * 输入的身份证号界面 点击下一步或者暂存的操作
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-16
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveIdcard(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String piid, newidcardno, empno;
  		de.clearSql();
		int result;

		// 流程开始获取piid
		piid = para.getString("piid");
		newidcardno = para.getString("newidcardno");
		empno = para.getString("empno");
		if ("".equals(newidcardno) || newidcardno == null) {
			this.bizException("新身份证号码不能为空！");
		}
		// if (oldidcardno.equals(newidcardno)) {
		// this.bizException("新老身份证号相同，请检查！");
		// }

		if (OdssuUtil.isIdcardnoExist(empno, newidcardno)) {
			this.bizException("身份证号【" + newidcardno + "】已存在！");
		}
		// 检查身份证号是否合法
		if (IdcardnoTool.validateCard(newidcardno) == false) {
			this.bizException("身份证号码不合法！");
		}

		// 更新工单表
		de.clearSql();
  		de.addSql("update odssuws.rysfzhxg ");
  		de.addSql("   set newidcardno = :newidcardno ,reviewer =null,reviewtime = null,spyj = null,spsm = null ");
  		de.addSql(" where piid = :piid        ");
		this.de.setString("newidcardno", newidcardno);
		this.de.setString("piid", piid);
		result = this.de.update();
		if (result == 0) {
			this.bizException("工单信息更新失败！");
		}

		return vdo;
	}

	/**
	 * 跳转到审批修改的身份证号人物界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-16
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageAdjustIdcardApproval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), rdo;
		DataStore gdxxds = DataStore.getInstance(), yjds = DataStore.getInstance();
		String piid, sprq = "", spyj = "", spr = "", spsm = "";
		// 流程开始获取piid
		piid = para.getString("piid");

		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空！");
		}
		// 查询工单信息
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");

		if (gdxxds.rowCount() > 0) {
			spr = gdxxds.getString(0, "reviewer");
			sprq = gdxxds.getDateToString(0, "reviewtime", "yyyy-mm-dd");
			spyj = gdxxds.getString(0, "spyj");
			spsm = gdxxds.getString(0, "spsm");
		}

		yjds.put(0, "spyj", spyj);
		yjds.put(0, "spsm", spsm);
		yjds.put(0, "spr", spr);
		yjds.put(0, "sprq", sprq);

		vdo.put("yjds", yjds);

		vdo.put("gdxxds", gdxxds);

		return vdo;
	}

	/**
	 * 审批修改的身份证号界面，点击下一步或者暂存的操作
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-16
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveEmpIdcard(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();
		String piid, spyj, spsm, spr;
		Date sprq;

		piid = para.getString("piid");
		spyj = para.getString("spyj");
		spsm = para.getString("spsm", "");
		spr = para.getString("spr");
		sprq = para.getDate("sprq");

		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空！");
		}

		if (spyj == null || spyj.trim().isEmpty()) {
			this.bizException("审批意见为空！");
		}
		// 保存审批意见
		de.clearSql();
  		de.addSql(" update odssuws.rysfzhxg ");
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
