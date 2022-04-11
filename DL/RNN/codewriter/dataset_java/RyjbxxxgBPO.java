package com.dw.odssu.ws.emp.ryjbxxxg;

import java.util.Date;

import com.dareway.apps.odssu.OdssuNames;
import com.dareway.apps.process.ProcessBPO;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;

/**
 * 人员基本信息修改
 * 
 * @Description:
 * @author 刘维秋
 * @date 2014-6-24
 */
public final class RyjbxxxgBPO extends BPO{
	/**
	 * 比较此次流程是否有角色信息发生变化，有一处发生变化即可return；
	 * 若此次流程无任何变动，则弹出businessException，提醒用户。
	 * wjn
     */
	public DataObject checkChange(DataObject para) throws AppException,BusinessException{
		//得到修改后的信息 
		String empno = para.getString("empno");
		String gender = para.getString("gender");
		String officetel = para.getString("officetel");
		String email = para.getString("email");
		String mphone = para.getString("mphone");
		String username = para.getString("username");
		String empname = para.getString("empname");
		String idcardno = para.getString("idcardno");

		de.clearSql();
  		de.addSql(" select rname,gender,officetel,mphone,email,idcardno,EMPNAME,loginname username  from odssu.empinfor where empno = :empno ");
		de.setString("empno", empno);
		DataStore vdsemp = de.query();
		if (vdsemp.rowCount() == 0) {
			this.bizException("没有找到编号为【" + empno + "】的人员的信息！");
		}
		//查找的修改前的信息
		String oldgender = vdsemp.getString(0, "gender");
		String oldofficetel = vdsemp.getString(0, "officetel");
		String oldemail = vdsemp.getString(0, "email");
		String oldmphone = vdsemp.getString(0, "mphone");
		String oldusername = vdsemp.getString(0,"username");
		String oldempname = vdsemp.getString(0,"empname");
		String oldidcard = vdsemp.getString(0, "idcardno");
		
		if(!gender.equals(oldgender) || !officetel.equals(oldofficetel) || !email.equals(oldemail) || !mphone.equals(oldmphone) || 
				!username.equals(oldusername) || !empname.equals(oldempname) || !idcardno.equals(oldidcard)) {
			return null;
		}
		
		throw new BusinessException("您的本次操作没有对操作员进行调整，建议您作废此流程或对操作员调整后，再提交审批。");
	}
	/**
	 * 跳转到修改人员基本信息申请界面
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-24
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageEmpMsgAdjust(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance(), result = DataObject.getInstance(), rdo;
		DataStore gdxxds = DataStore.getInstance();
  		de.clearSql();
		String piid, empno;
		String userid = this.getUser().getUserid();
		Date sysdate = DateUtil.getDBTime();
		String dbid = GlobalNames.DEBUGMODE ? (String) this.getUser().getValue("dbid") : OdssuNames.DBID;


		// 流程开始获取piid
		piid = para.getString("piid");

		// 查询工单信息
		rdo = getGdxxAll(piid);
		gdxxds = rdo.getDataStore("gdxxds");

		// 如果无工单，创建工单
		if (gdxxds.rowCount() == 0) {
			para.put("piid", piid);
			BPO ibpo = this.newBPO(ProcessBPO.class);
			result = ibpo.doMethod(GlobalNames.DEFAULT_BIZ, "getProcessVars", para);

			empno = result.getString("empno");
			String uact = "",uactusername = "";
			de.clearSql();
			de.addSql("  select empno,                         ");
			de.addSql("         empname,                       ");
			de.addSql("         rname,                         ");
			de.addSql("         gender,                        ");
			de.addSql("         officetel,                     ");
			de.addSql("         mphone,                        ");
			de.addSql("         email,                         ");
			de.addSql("         loginname username,            ");
			de.addSql("         idcardno,                      ");
			de.addSql("         a.uactid                       ");
  			if("178".equals(dbid)) {
  	  			de.addSql("  ,b.username uactusername,b.uact   ");
   			}
			de.addSql("    from odssu.empinfor a               ");
  			if("178".equals(dbid)) {
  	  			de.addSql("  left join odssu.uactinfor b on b.uactid = a.uactid                ");
   			}
			de.addSql("   where a.empno = :empno                 ");

			de.setString("empno", empno);
			DataStore vdsemp = de.query();
			if (vdsemp.rowCount() == 0) {
				this.bizException("没有找到编号为【" + empno + "】的人员的信息！");
			}

			String gender = vdsemp.getString(0, "gender");
			String officetel = vdsemp.getString(0, "officetel");
			String mphone = vdsemp.getString(0, "mphone");
			String email = vdsemp.getString(0, "email");
			String idcardno = vdsemp.getString(0, "idcardno");
			String empname = vdsemp.getString(0, "empname");
			String username = vdsemp.getString(0, "username");
			String olduactid = vdsemp.getString(0, "uactid");
  			if("178".equals(dbid)) {
  				uact = vdsemp.getString(0, "uact");
  				uactusername = vdsemp.getString(0, "uactusername");
   			}
			// 创建工单表

			de.clearSql();
  			de.addSql("insert into odssuws.ryjbxxxgall");
  			de.addSql("  ( piid, empno  , oldgender , newgender , oldofficetel , newofficetel ,   ");
  			if("178".equals(dbid)) {
  	  			de.addSql("  olduactid ,uactid,uactusername,uact ,  ");
   			}
  			de.addSql("  oldmphone  , newmphone , oldemail , newemail , operator, operationtime,OLDIDCARD ,NEWIDCARD,OLDEMPNAME,NEWEMPNAME,OLDUSERNAME,NEWUSERNAME)");
  			de.addSql(" values(:piid, :empno , :gender , :gender , :officetel , :officetel ,  ");
  			if("178".equals(dbid)) {
  	  			de.addSql(" :olduactid, :olduactid, :uactusername, :uact ,    ");
  	  			de.setString("olduactid",olduactid );
  	  			de.setString("uactusername",uactusername );
  	  			de.setString("uact",uact );
  			}
  			de.addSql(" :mphone , :mphone  , :email , :email , :userid , :sysdate,:idcardno,:idcardno,:empname,:empname,:username,:username ) ");

  			de.setString("piid", piid);
			de.setString("empno", empno);
			de.setString("gender", gender);
			de.setString("gender", gender);
			de.setString("officetel", officetel);
			de.setString("officetel", officetel);
			de.setString("mphone", mphone);
			de.setString("mphone", mphone);
			de.setString("email", email);
			de.setString("email", email);
			de.setString("userid", userid);
			de.setDateTime("sysdate", sysdate);
			de.setString("idcardno", idcardno);
			de.setString("empname", empname);
			de.setString("username", username);

			int resultno = 0;

			resultno = de.update();

			if (resultno == 0) {
				this.bizException("工单信息保存失败！");
			}
			vdsemp.put(0, "piid", piid);

			vdo.put("empds", vdsemp);

			return vdo;
		} else {
			empno = gdxxds.getString(0, "empno");
			gdxxds.put(0, "gender", gdxxds.getString(0, "newgender"));
			gdxxds.put(0, "officetel", gdxxds.getString(0, "newofficetel"));
			gdxxds.put(0, "mphone", gdxxds.getString(0, "newmphone"));
			gdxxds.put(0, "email", gdxxds.getString(0, "newemail"));
			gdxxds.put(0, "idcardno", gdxxds.getString(0, "NEWIDCARD"));
			gdxxds.put(0, "empname", gdxxds.getString(0, "NEWEMPNAME"));
			gdxxds.put(0, "username", gdxxds.getString(0, "NEWUSERNAME"));
			gdxxds.put(0, "uactusername", gdxxds.getString(0, "uactusername"));
			gdxxds.put(0, "uact", gdxxds.getString(0, "uact"));
			gdxxds.put(0, "uactid", gdxxds.getString(0, "uactid"));
			de.clearSql();
  			de.addSql(" select empno,rname,empname,gender,officetel,mphone,email,idcardno from odssu.empinfor where empno = :empno ");
			de.setString("empno", empno);
			DataStore vdsemp = de.query();
			if (vdsemp.rowCount() == 0) {
				this.bizException("没有找到编号为【" + empno + "】的人员的信息！");
			}
			vdo.put("empds", gdxxds);

			return vdo;
		}
	}

	/**
	 * 查询工单信息
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-24
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
  		de.addSql("  from odssuws.ryjbxxxg ");
  		de.addSql(" where piid=:piid ");
		this.de.setString("piid", piid);
		gdxxds = this.de.query();

		rdo.put("gdxxds", gdxxds);
		gdxxds = null;

		return rdo;
	}
	
	public final DataObject getGdxxAll(String piid) throws Exception {
		DataObject rdo = DataObject.getInstance();
		DataStore gdxxds = DataStore.getInstance();
  		de.clearSql();

		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from odssuws.ryjbxxxgall ");
  		de.addSql(" where piid=:piid ");
		this.de.setString("piid", piid);
		gdxxds = this.de.query();

		rdo.put("gdxxds", gdxxds);
		gdxxds = null;

		return rdo;
	}

	/**
	 * 保存修改的人员基本信息
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-24
	 * @param request
	 * @param response
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveEmpMsgAdjust(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
  		de.clearSql();

		String piid = para.getString("piid");
		//得到修改后的信息 
		String empno = para.getString("empno");
		String gender = para.getString("gender");
		String officetel = para.getString("officetel");
		String email = para.getString("email");
		String mphone = para.getString("mphone");
		String username = para.getString("username");
		String empname = para.getString("empname");
		String idcardno = para.getString("idcardno");
		String uactid = para.getString("uactid","");
		String uact = para.getString("uact","");
		String uactusername = para.getString("uactusername","");
		
		de.clearSql();
  		de.addSql(" select rname,gender,officetel,mphone,email,idcardno,EMPNAME,loginname username,uactid  from odssu.empinfor where empno = :empno ");
		de.setString("empno", empno);
		DataStore vdsemp = de.query();
		if (vdsemp.rowCount() == 0) {
			this.bizException("没有找到编号为【" + empno + "】的人员的信息！");
		}
		//查找的修改前的信息
		String oldgender = vdsemp.getString(0, "gender");
		String oldofficetel = vdsemp.getString(0, "officetel");
		String oldmphone = vdsemp.getString(0, "mphone");
		String oldemail = vdsemp.getString(0, "email");
		String oldidcard = vdsemp.getString(0, "idcardno");
		String oldusername = vdsemp.getString(0,"username");
		String oldempname = vdsemp.getString(0,"empname");
		String olduactid = vdsemp.getString(0, "uactid");

		// 更新工单操作人信息
		de.clearSql();
  		de.addSql(" update odssuws.ryjbxxxgall ");
  		de.addSql("    set oldgender = :oldgender,newgender=:gender,oldofficetel = :oldofficetel,newofficetel = :officetel, ");
  		de.addSql("        oldmphone = :oldmphone,newmphone = :mphone,oldemail = :oldemail,newemail = :email,reviewer =null,reviewtime = null,spyj = null,spsm = null ,");
  		de.addSql("         OLDIDCARD = :oldidcard , NEWIDCARD = :idcardno ,OLDEMPNAME = :oldempname, NEWEMPNAME =:empname,OLDUSERNAME = :oldusername , NEWUSERNAME =:username");
//  		if(StringUtils.isNotBlank(uactid)) {
  		de.addSql("         ,uactid = :uactid,olduactid=:olduactid,uactusername= :uactusername,uact = :uact");
  		de.setString("uactid", uactid);
  		de.setString("olduactid", olduactid);
  		de.setString("uact", uact);
  		de.setString("uactusername", uactusername);
//  		}

  		de.addSql(" where piid = :piid ");
		de.setString("oldgender", oldgender);
		de.setString("gender", gender);
		de.setString("oldofficetel", oldofficetel);
		de.setString("officetel", officetel);
		de.setString("oldmphone", oldmphone);
		de.setString("mphone", mphone);
		de.setString("oldemail", oldemail);
		de.setString("email", email);
		de.setString("oldidcard", oldidcard);
		de.setString("idcardno", idcardno);
		de.setString("oldempname", oldempname);
		de.setString("empname", empname);
		de.setString("oldusername", oldusername);
		de.setString("username", username);
		de.setString("piid", piid);
		int result = this.de.update();

		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}

		return vdo;
	}


	/**
	 * 人员基本信息审批界面点击暂存的操作
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-24
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveEmpMsgAdjustApproval(DataObject para) throws Exception {
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
  		de.addSql(" update odssuws.ryjbxxxg ");
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

		return vdo;
	}

}
