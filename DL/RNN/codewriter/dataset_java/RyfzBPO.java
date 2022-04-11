package com.dw.odssu.ws.emp.ryfz;

import java.util.Date;

import com.dareway.apps.process.ProcessBPO;
import com.dareway.apps.process.util.ProcessUtil;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

/**
 * 人员离职 类描述
 * 
 * @author liuy
 * @version 1.0 创建时间 2014-05-12
 */
public final class RyfzBPO extends BPO{

	/**
	 * 跳转、查询 第一个节点
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-21
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageEmpRegainApply(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataObject result = DataObject.getInstance();
		DataObject rdo;
		DataStore gdxxds, vdsemp;
		String piid, empno, hrbelong, empname,hrbelongname,username,gender,idcardno,email ="",officetel ="",mphone ="";
		String userid = this.getUser().getUserid();
		Date sysdate = DateUtil.getDBTime();
		

		// 流程开始获取piid
		piid = para.getString("piid");

		
		hrbelong = (String)ProcessUtil.getTEEVarByPiid(piid, "hrbelong");
		hrbelongname =getOrgNameByOrgNO(hrbelong);
		empname = (String)ProcessUtil.getTEEVarByPiid(piid, "empname");
		username = (String)ProcessUtil.getTEEVarByPiid(piid, "username");
		gender = (String)ProcessUtil.getTEEVarByPiid(piid, "gender");
		idcardno = (String)ProcessUtil.getTEEVarByPiid(piid, "idcardno");
		email = (String)ProcessUtil.getTEEVarByPiid(piid, "email");
		officetel = (String)ProcessUtil.getTEEVarByPiid(piid, "officetel");
		mphone = (String)ProcessUtil.getTEEVarByPiid(piid, "mphone");
//		
//		if(""!=ProcessUtil.getTEEVarByPiid(piid, "email")){
//			email = ProcessUtil.getTEEVarByPiid(piid, "email").toString();
//		}
//		if(""!=ProcessUtil.getTEEVarByPiid(piid, "officetel")){
//			email = ProcessUtil.getTEEVarByPiid(piid, "officetel").toString();
//		}
//		if(""!=ProcessUtil.getTEEVarByPiid(piid, "mphone") ){
//			email = ProcessUtil.getTEEVarByPiid(piid, "mphone").toString();
//		}
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
			this.de.clearSql();
  			this.de.addSql("insert into odssuws.ryfz ");
  			this.de.addSql("  (piid, empno, operator, operationtime  ,username,gender,idcardno,email,officetel,mphone) ");
  			this.de.addSql(" values(:piid, :empno, :userid, :sysdate ,:username,:gender,:idcardno,:email,:officetel,:mphone) ");
			this.de.setString("piid", piid);
			this.de.setString("empno", empno);
			this.de.setString("userid", userid);
			this.de.setDateTime("sysdate", sysdate);
			this.de.setString("username", username);
			this.de.setString("gender", gender);
			this.de.setString("idcardno", idcardno);
			this.de.setString("email", email);
			this.de.setString("officetel", officetel);
			this.de.setString("mphone", mphone);
			this.de.update();

			this.de.clearSql();
			this.de.addSql(" select e.empno,e.rname,:piid piid,e.empname,e.loginname username,e.gender,e.idcardno,e.EMAIL,e.OFFICETEL,e.MPHONE ");
			this.de.addSql(" from   odssu.empinfor e ");
  			this.de.addSql(" where  empno = :empno ");
  			this.de.setString("piid",piid);
  			this.de.setString("empno", empno);
			vdsemp = this.de.query();
			if (vdsemp.rowCount() == 0) {
				this.bizException("编号为【" + empno + "】的人员信息不存在！");
			}
			vdsemp.put(0, "hrbelong", hrbelong);
			vdsemp.put(0, "belongorgname", hrbelongname);
		} else {
			empno = gdxxds.getString(0, "empno");
			this.de.clearSql();
			this.de.addSql(" select e.empno,e.empname,e.rname,:piid piid");
			this.de.addSql(" from   odssu.empinfor e ");
			this.de.addSql(" where  empno = :empno ");
			this.de.setString("piid",piid);
			this.de.setString("empno", empno);
			vdsemp = this.de.query();
			if (vdsemp.rowCount() == 0) {
				this.bizException("编号为【" + empno + "】的人员信息不存在！");
			}
			vdsemp.put(0, "hrbelong", hrbelong);
			vdsemp.put(0, "empname", empname);
			vdsemp.put(0, "belongorgname", hrbelongname);
			vdsemp.put(0, "username", username);
			vdsemp.put(0, "gender", gender);
			vdsemp.put(0, "idcardno", idcardno);
			vdsemp.put(0, "officetel", officetel);
			vdsemp.put(0, "mphone", mphone);
			vdsemp.put(0, "email", email);
			
		}
		vdo.put("empds", vdsemp);
		vdo.put("piid", piid);
		vdo.put("belongorgname", hrbelongname);
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
		String hrbleong = "", orgname = "";


		this.de.clearSql();
		this.de.addSql("select * ");
		this.de.addSql("  from odssuws.ryfz ");
		this.de.addSql(" where piid=:piid ");
		this.de.setString("piid", piid);
		gdxxds = this.de.query();

		if (gdxxds.rowCount() > 0) {
			hrbleong = gdxxds.getString(0, "hrbelong");

			this.de.clearSql();
			this.de.addSql("select orgname ");
			this.de.addSql("  from odssu.orginfor ");
			this.de.addSql(" where orgno=:hrbleong ");
			this.de.setString("hrbleong", hrbleong);
			dstmp = this.de.query();
			if (dstmp.rowCount() > 0) {
				orgname = dstmp.getString(0, "orgname");
			}
			gdxxds.put(0, "hrbelongname", orgname);
		}

		rdo.put("gdxxds", gdxxds);
		gdxxds = null;

		return rdo;
	}

	/**
	 * 保存新复职信息到工单中
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-21
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveEmpRegainInWS(DataObject para) throws Exception {
  		de.clearSql();
		int result;

		// 流程开始获取piid
		String piid = para.getString("piid");
		String empno = para.getString("empno");
		String empname = para.getString("empname");
		String hrbelong = para.getString("hrbelong");
		String belongorgname = para.getString("belongorgname");
		String username = para.getString("username");
		String gender = para.getString("gender");
		String idcardno = para.getString("idcardno");
		String email = para.getString("email");
		String officetel = para.getString("officetel");
		String mphone = para.getString("mphone");

		
		
		if ("".equals(empno) || empno == null) {
			this.bizException("人员编号为空！");
		}
		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("保存离职信息到工单时，工单编号为空！");
		}
		if (empname == null || empname.trim().isEmpty()) {
			this.bizException("保存离职信息到工单时，人员编号为空！");
		}
		if (hrbelong == null || hrbelong.trim().isEmpty()) {
			this.bizException("保存离职信息到工单时，隶属机构编号编号为空！");
		}
		if (belongorgname == null || belongorgname.trim().isEmpty()) {
			this.bizException("人员隶属机构名称为空！");
		}
		// 判断标识姓名是否已经存在、人事隶属机构正不正常
//		if (OdssuUtil.isEmpNameExist(empno, empname)) {
//			this.bizException("标识姓名【" + empname + "】已经存在，请重新输入！");
//		}
		if (OdssuUtil.isOrgExist(hrbelong) == false) {
			this.bizException("人事隶属机构【" + hrbelong + "】不存在，保存失败！");
		}
		if (OdssuUtil.isOrgOnWork(hrbelong) == false) {
			this.bizException("人事隶属机构【" + hrbelong + "】已经被注销，保存失败！");
		}
    		de.addSql(" select orgname from odssu.orginfor where orgno = :hrbelong ");
		this.de.setString("hrbelong", hrbelong);
		DataStore hrds = this.de.query();
		if(hrds == null || hrds.rowCount() == 0){
			this.bizException("获取机构编号为【" + hrbelong + "】的机构信息时出错！");
		}
		
		if (!hrds.getString(0, "orgname").equals(belongorgname)) {
			this.bizException("人事隶属机构编号与人事隶属机构名称对应出错！");
		}
		
		// 更新工单表
		de.clearSql();
  		de.addSql("update odssuws.ryfz ");
  		de.addSql("   set hrbelong = :hrbelong ,empname = :empname , spyj = null,spsm = null, reviewer = null, reviewtime = null ,username = :username,gender = :gender,idcardno = :idcardno,email = :email,officetel = :officetel,mphone = :mphone");
  		de.addSql(" where piid = :piid        ");
		this.de.setString("hrbelong", hrbelong);
		this.de.setString("empname", empname);
		this.de.setString("piid", piid);
		this.de.setString("username", username);
		this.de.setString("gender", gender);
		this.de.setString("idcardno", idcardno);
		this.de.setString("email", email);
		this.de.setString("officetel", officetel);
		this.de.setString("mphone", mphone);
		result = this.de.update();
		if (result == 0) {
			throw new Exception("工单信息更新失败！");
		}
		ProcessUtil.setTEEVarByPiid(piid, "hrbelong", hrbelong);
		ProcessUtil.setTEEVarByPiid(piid, "empname", empname);
		ProcessUtil.setTEEVarByPiid(piid, "username", username);
		ProcessUtil.setTEEVarByPiid(piid, "gender", gender);
		ProcessUtil.setTEEVarByPiid(piid, "idcardno", idcardno);
		ProcessUtil.setTEEVarByPiid(piid, "email", email);
		ProcessUtil.setTEEVarByPiid(piid, "officetel", officetel);
		ProcessUtil.setTEEVarByPiid(piid, "piid", piid);
		DataObject vdo = DataObject.getInstance();
		return vdo;
	}
	public String getOrgNameByOrgNO(String orgno) throws AppException{
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql("select orgname from odssu.orginfor a where a.orgno = :orgno");
		de.setString("orgno", orgno);
		DataStore sd = de.query();
		String orgname =sd.getString(0, "orgname");
		return orgname;
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
	public final DataObject fwPageEmpRegainApporval(DataObject para) throws Exception {
		// 流程开始获取piid
		String piid = para.getString("piid");
    		de.clearSql();
  		de.addSql(" select f.piid,e.empno,f.empname,e.rname,o.orgno, ");
  		de.addSql("        o.orgname,o.displayname ,f.username,f.gender,f.idcardno,f.EMAIL,f.OFFICETEL,f.MPHONE");
  		de.addSql(" from   odssuws.ryfz f, ");
  		de.addSql("        odssu.orginfor o, ");
  		de.addSql("        odssu.empinfor e ");
  		de.addSql(" where  f.empno = e.empno and f.hrbelong = o.orgno  ");
  		de.addSql("        and f.piid = :piid ");
		de.setString("piid", piid);
		DataStore vdsemp = de.query();

		// 获取审批意见DataStore
		DataStore gdds = getGdxx(piid).getDataStore("gdxxds");

		DataStore yjds = DataStore.getInstance();
		yjds.put(0, "spr", gdds.getString(0, "reviewer"));
		yjds.put(0, "spyj", gdds.getString(0, "spyj"));
		yjds.put(0, "spsm", gdds.getString(0, "spsm"));
		yjds.put(0, "sprq", gdds.getDate(0, "reviewtime"));

		DataObject vdo = DataObject.getInstance();
		vdo.put("piid", piid);
		vdo.put("empds", vdsemp);
		vdo.put("yjds", yjds);

		return vdo;
	}

	/**
	 * 记录审批信息
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-21
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveEmpRegainInASO(DataObject para) throws Exception {
  		de.clearSql();
		String piid, spr, spyj, spsm;
		Date sprq;
		DataStore vdstmp;

		piid = para.getString("piid");
		spr = para.getString("spr");
		spyj = para.getString("spyj");
		spsm = para.getString("spsm");
		sprq = para.getDate("sprq");

		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空！");
		}

		de.clearSql();
  		de.addSql(" select * ");
  		de.addSql("   from odssuws.ryfz  ");
  		de.addSql("  where piid = :piid  ");
		this.de.setString("piid", piid);
		vdstmp = this.de.query();

		if (vdstmp.rowCount() < 1) {
			this.bizException("未取到工单信息" + piid);
		}

		// 更新工单操作人信息
		de.clearSql();
  		de.addSql(" update odssuws.ryfz ");
  		de.addSql("    set  reviewer = :spr ,reviewtime = :sprq ,spyj = :spyj , spsm = :spsm ");
  		de.addSql(" where piid = :piid ");

		this.de.setString("spr", spr);
		this.de.setDateTime("sprq", sprq);
		this.de.setString("spyj", spyj);
		this.de.setString("spsm", spsm);
		this.de.setString("piid", piid);

		int result = this.de.update();

		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}
		
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

		return null;
	}

	/**
	 * 选择人事隶属机构的时候，查询符合条件的机构
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-7
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject queryHrOrgLov4EmpRegain(DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String piid = para.getString("piid");

		para.put("piid", piid);
		BPO ibpo = this.newBPO(ProcessBPO.class);
		DataObject varvdo = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);
		String lsjgid = varvdo.getString("_process_biz");
    		de.clearSql();
		de.clearSql();
  		de.addSql(" select o.orgno,o.displayname,o.orgname,o.sleepflag");
  		de.addSql(" from   odssu.ir_org_closure i, ");
  		de.addSql("        odssu.orginfor o, ");
  		de.addSql("        odssu.org_type t ");
  		de.addSql(" where  i.orgno = o.orgno ");
  		de.addSql(" and   o.orgtype = t.typeno ");
  		de.addSql("  and o.sleepflag = '0' and i.belongorgno =:lsjgid  ");
  		de.addSql("  and  (o.orgno like :orgno  ");
  		de.addSql("       or o.orgname like :orgno  ");
  		de.addSql("       or o.displayname like :orgno  ");
  		de.addSql("       or o.fullname like :orgno  ");
  		de.addSql("       or o.orgnamepy like :orgno  ");
  		de.addSql("       or o.fullnamepy like :orgno  ");
  		de.addSql("       or o.displaynamepy like :orgno)  ");
  		de.addSql("  and t.typenature in ('B','C') ");
		this.de.setString("lsjgid", lsjgid);
		this.de.setString("orgno", "%" + orgno + "%");
		DataStore orgds = this.de.query();

		DataObject vdo = DataObject.getInstance();

		vdo.put("orgds", orgds);

		return vdo;
	}

}
