package com.dw.odssu.ws.org.jgmcxg;

import java.util.Date;

import com.dareway.apps.process.ProcessBPO;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;

/**
 * 机构基本信息修改 类描述
 * 
 * @author liuy
 * @version 1.0 创建时间 2014-05-13
 */
public final class JgmcxgBPO extends BPO {
	/**
	 * 比较此次流程是否有角色信息发生变化，有一处发生变化即可return；
	 * 若此次流程无任何变动，则弹出businessException，提醒用户。
	 * wjn
     */
	public DataObject checkChange(DataObject para) throws AppException,BusinessException{
		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String orgname = para.getString("orgname");
		String xzqhdm = para.getString("xzqhdm");

		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空！");
		}
		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
		if (orgname == null || orgname.trim().isEmpty()) {
			this.bizException("机构名称为空！");
		}
		if (xzqhdm == null || xzqhdm.trim().isEmpty()) {
			this.bizException("行政区划为空！");
		}
		if(orgname.length()>35){
			this.bizException("机构名称的长度过长，请重新输入！");	
		}
		
		de.clearSql();
  		de.addSql(" select  orgname,  displayname, fullname,orgtype,xzqhdm ");
  		de.addSql("from odssu.orginfor ");
  		de.addSql(" where orgno=:orgno ");
		de.setString("orgno", orgno);
		DataStore vdsorg = de.query();
		if (vdsorg.rowCount() == 0) {
			this.bizException("没有找到编号为【" + orgno + "】的机构的信息！");
		}

		String oldorgname = vdsorg.getString(0, "orgname");
		String oldxzqhdm = vdsorg.getString(0, "xzqhdm");
		
		if ((!orgname.equals(oldorgname)) || (!xzqhdm.equals(oldxzqhdm))) {
			return null;
		}
		
		throw new BusinessException("您的本次操作没有对机构进行调整，建议您作废此流程或对机构调整后，再提交审批。");
	}
	/**
	 * 跳转到修改机构自然信息任务界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-2
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageOrgMsgAdjust(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), result = DataObject.getInstance(), rdo;
		DataStore gdxxds = DataStore.getInstance();
  		de.clearSql();
		String piid, orgno;
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

			orgno = result.getString("orgno");

			// 查询需要的数据
			de.clearSql();
  			de.addSql("select orgno, orgname , displayname,  ");
  			de.addSql("       fullname,  orgtype, xzqhdm ");
  			de.addSql("  from odssu.orginfor o ");
  			de.addSql(" where orgno=:orgno  ");
  			de.addSql("   and o.sleepflag = '0'  ");
			this.de.setString("orgno", orgno);
			DataStore vdsorg = this.de.query();
			if (vdsorg.rowCount() == 0) {
				this.bizException("没有找到编号为【" + orgno + "】的机构的信息！");
			}
			String orgname = vdsorg.getString(0, "orgname");
			String displayname = vdsorg.getString(0, "displayname");
			String fullname = vdsorg.getString(0, "fullname");
			String xzqhdm = vdsorg.getString(0, "xzqhdm");
			
			// 创建工单表
			de.clearSql();
  			de.addSql("insert into odssuws.jgjbxxxg ");
  			de.addSql("  ( piid, orgno,oldorgname,neworgname,oldfullname,newfullname,olddisplayname,newdisplayname, operator, operationtime,oldxzqhdm,newxzqhdm)");
  			de.addSql(" values(:piid, :orgno   ,   :orgname      ,    :orgname     ,     :fullname     ,    :fullname      ,      :displayname       ,       :displayname, :userid, :sysdate ,:xzqhdm ,:xzqhdm    ) ");
			de.setString("piid", piid);
			de.setString("orgno", orgno);
			de.setString("orgname", orgname);
			de.setString("orgname", orgname);
			de.setString("fullname", fullname);
			de.setString("fullname", fullname);
			de.setString("displayname", displayname);
			de.setString("displayname", displayname);
			de.setString("userid", userid);
			de.setDateTime("sysdate", sysdate);
			de.setString("xzqhdm", xzqhdm);

			this.de.update();

			vdsorg.put(0, "piid", piid);
			vdo.put("orgds", vdsorg);
			return vdo;
		} else {
			orgno = gdxxds.getString(0, "orgno");
			gdxxds.put(0, "orgname", gdxxds.getString(0, "neworgname"));
			gdxxds.put(0, "displayname", gdxxds.getString(0, "newdisplayname"));
			gdxxds.put(0, "fullname", gdxxds.getString(0, "newfullname"));
			gdxxds.put(0, "xzqhdm", gdxxds.getString(0, "newxzqhdm"));
			de.clearSql();
  			de.addSql(" select orgno, orgname,  displayname, fullname ");
  			de.addSql(" from odssu.orginfor");
  			de.addSql(" where orgno=:orgno and sleepflag = '0' ");
			de.setString("orgno", orgno);
			DataStore vdsorg = de.query();
			if (vdsorg.rowCount() == 0) {
				this.bizException("没有找到编号为【" + orgno + "】的人员的信息！");
			}

			String orgname = vdsorg.getString(0, "orgname");
			gdxxds.put(0, "orgname", orgname);
			vdo.put("orgds", gdxxds);
			return vdo;
		}
	}

	/**
	 * 查询工单信息 .
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-2
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
  		de.addSql("  from odssuws.jgjbxxxg ");
  		de.addSql(" where piid=:piid  ");
		this.de.setString("piid", piid);
		gdxxds = this.de.query();

		rdo.put("gdxxds", gdxxds);
		gdxxds = null;

		return rdo;
	}

	/**
	 * 修改机构自然信息保存
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-2
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveOrgMsgAdjust(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();

		String piid = para.getString("piid");
		String orgno = para.getString("orgno");
		String orgname = para.getString("orgname");
		String xzqhdm = para.getString("xzqhdm");

		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空！");
		}
		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
		if (orgname == null || orgname.trim().isEmpty()) {
			this.bizException("机构名称为空！");
		}
		if(orgname.length()>35){
			this.bizException("机构名称的长度过长，请重新输入！");	
		}
		if (xzqhdm == null || xzqhdm.trim().isEmpty()) {
			this.bizException("行政区划为空！");
		}

		// 更新工单操作机构信息
		de.clearSql();
  		de.addSql("update odssuws.jgjbxxxg ");
  		de.addSql(" set newdisplayname = :orgname, newfullname = :orgname, neworgname = :orgname,newxzqhdm =:newxzqhdm ");
  		de.addSql(" where piid = :piid ");
		de.setString("orgname", orgname);
		de.setString("piid", piid);
		de.setString("newxzqhdm", xzqhdm);

		int result = this.de.update();

		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}
		return vdo;
	}

	/**
	 * 跳转到确定新增机构的标识名称任务界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-11
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageESOrgname(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore orginfods = DataStore.getInstance(), dstmp, orgnameds;
  		de.clearSql();
		String fullname = "", orgno = "", neworgname = "";
		String piid = para.getString("piid");

		de.clearSql();
  		de.addSql("select *	");
  		de.addSql("  from odssuws.jgjbxxxg  ");
  		de.addSql(" where piid = :piid		       ");
		this.de.setString("piid", piid);
		dstmp = this.de.query();
		if (dstmp.rowCount() > 0) {
			fullname = dstmp.getString(0, "newfullname");
			orgno = dstmp.getString(0, "orgno");
			neworgname = dstmp.getString(0, "neworgname");
		}

		de.clearSql();
  		de.addSql("select orgno, fullname, displayname, orgname	");
  		de.addSql("  from odssu.orginfor  ");
  		de.addSql(" where orgname = :fullname and orgno != :orgno and sleepflag = '0' ");
		this.de.setString("fullname", fullname);
		this.de.setString("orgno", orgno);
		orgnameds = this.de.query();

		orginfods.put(0, "orgname", neworgname);
		orginfods.put(0, "orgno", orgno);
		orginfods.put(0, "piid", piid);

		vdo.put("orginfods", orginfods);
		vdo.put("orgnameds", orgnameds);
		return vdo;
	}

	/**
	 * 确定新增机构的标识名称任务界面，点击下一步后保存标识名称
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-11
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveOrgName(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String piid, orgname;
  		de.clearSql();
		String flag = "true";

		piid = para.getString("piid");
		orgname = para.getString("orgname");
		String orgno = para.getString("orgno");

		if (piid == null || piid.trim().isEmpty()) {
			this.bizException("piid为空！");
		}
		if (orgname == null || orgname.trim().isEmpty()) {
			this.bizException("机构名称为空！");
		}
		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("机构编号为空！");
		}
    		de.addSql("select 1 from odssu.orginfor where orgname = :orgname and orgno != :orgno and sleepflag = '0' ");
		this.de.setString("orgname", orgname);
		this.de.setString("orgno", orgno);

		DataStore orgds = this.de.query();

		if (orgds.rowCount() != 0) {
			flag = "repeat";
			vdo.put("flag", flag);
			return vdo;
		}

		// 保存到工单表
		de.clearSql();
  		de.addSql(" update odssuws.jgjbxxxg ");
  		de.addSql("    set neworgname = :orgname  ");
  		de.addSql("  where piid = :piid 	       ");
		this.de.setString("orgname", orgname);
		this.de.setString("piid", piid);
		int result = this.de.update();

		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}
		vdo.put("flag", flag);
		return vdo;
	}

	/**
	 * 跳转到修改机构审批界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-2
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageOrgAdjustApproval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), rdo;
		DataStore gdxxds = DataStore.getInstance();
		DataStore xgds = DataStore.getInstance();
		String piid;
		// 流程开始获取piid
		piid = para.getString("piid");

		// 查询工单信息
		rdo = getGdxx(piid);
		gdxxds = rdo.getDataStore("gdxxds");

		if (gdxxds.rowCount() == 0) {
			this.bizException("没有找到编号为【" + piid + "】的流程的工单信息！");
		}
		String orgno = gdxxds.getString(0, "orgno");
		String oldorgname = gdxxds.getString(0, "oldorgname");
		String neworgname = gdxxds.getString(0, "neworgname");

		String olddisplayname = gdxxds.getString(0, "olddisplayname");
		String newdisplayname = gdxxds.getString(0, "newdisplayname");

		String oldfullname = gdxxds.getString(0, "oldfullname");
		String newfullname = gdxxds.getString(0, "newfullname");

		String spr = gdxxds.getString(0, "reviewer");
		String sprq = gdxxds.getDateToString(0, "reviewtime", "yyyy-mm-dd");
		String spyj = gdxxds.getString(0, "spyj");
		String spsm = gdxxds.getString(0, "spsm");

		DataStore yjds = DataStore.getInstance();
		yjds.put(0, "spyj", spyj);
		yjds.put(0, "spsm", spsm);
		yjds.put(0, "spr", spr);
		yjds.put(0, "sprq", sprq);

		if (!oldfullname.equals(newfullname)) {
			xgds.put(xgds.rowCount(), "xgx", "机构全称");
			xgds.put(xgds.rowCount() - 1, "yz", oldfullname);
			xgds.put(xgds.rowCount() - 1, "xz", newfullname);
		}
		if (!olddisplayname.equals(newdisplayname)) {
			xgds.put(xgds.rowCount(), "xgx", "机构简称");
			xgds.put(xgds.rowCount() - 1, "yz", olddisplayname);
			xgds.put(xgds.rowCount() - 1, "xz", newdisplayname);
		}
		if (!oldorgname.equals(neworgname)) {
			xgds.put(xgds.rowCount(), "xgx", "机构标识名称");
			xgds.put(xgds.rowCount() - 1, "yz", oldorgname);
			xgds.put(xgds.rowCount() - 1, "xz", neworgname);
		}
    		de.clearSql();
  		de.addSql(" select  orgname,  displayname, fullname,orgtype ");
  		de.addSql("from odssu.orginfor ");
  		de.addSql(" where orgno=:orgno and sleepflag = '0' ");
		de.setString("orgno", orgno);
		DataStore vdsorg = de.query();
		if (vdsorg.rowCount() == 0) {
			this.bizException("没有找到编号为【" + orgno + "】的人员的信息！");
		}
		String orgname = vdsorg.getString(0, "orgname");

		gdxxds.put(0, "orgname", orgname);

		vdo.put("orgds", gdxxds);
		vdo.put("yjds", yjds);
		vdo.put("xgds", xgds);

		return vdo;
	}

	/**
	 * 修改机构审批界面 暂存方法
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-2
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveOrgMsgAdjustApproval(DataObject para) throws Exception {
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
		// 保存审批意见
		de.clearSql();
  		de.addSql(" update odssuws.jgjbxxxg ");
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

		return vdo;
	}

	/**
	 * 查询标志姓名重复的机构信息
	 * @Description:
	 * @author 王具然
	 * @date 2014-8-4
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject queryRepeatResult(DataObject para) throws Exception {
		String orgname = para.getString("orgname");
		String orgno = para.getString("orgno");

		if (orgname == null || orgname.trim().isEmpty()) {
			this.bizException("orgname 为空！");
		}
		if (orgno == null || orgno.trim().isEmpty()) {
			this.bizException("orgno 为空！");
		}
  		de.clearSql();
  		de.addSql(" select orgno , orgname , displayname  , fullname  from odssu.orginfor where orgname = :orgname and orgno != :orgno and sleepflag = '0' ");
		this.de.setString("orgname", orgname);
		this.de.setString("orgno", orgno);

		DataStore orgds = this.de.query();

		DataObject vdo = DataObject.getInstance();

		vdo.put("orgnameds", orgds);

		return vdo;
	}

}
