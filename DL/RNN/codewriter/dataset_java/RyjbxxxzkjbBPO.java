package com.dw.odssu.ws.emp.ryjbxxxzkjb;

import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import com.dareway.apps.process.util.ProcessUtil;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;
import com.dw.util.OdssuUtil;

/**
 * 人员基本信息新增 类描述
 * 
 * @author liuy
 * @version 1.0 创建时间 2014-05-07
 */
public final class RyjbxxxzkjbBPO extends BPO{
	/**
	 * 查询工单信息
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-13
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
  		de.addSql("  from odssuws.ryjbxxxzkjb ");
  		de.addSql(" where piid=:piid ");
		this.de.setString("piid", piid);
		gdxxds = this.de.query();

		rdo.put("gdxxds", gdxxds);
		gdxxds = null;

		return rdo;
	}
	/**
	 * 方法简介：检查标识姓名是否存在
	 * 郑海杰  2015-11-11
	 */
	public DataObject checkEmpname(DataObject para) throws AppException{
		String empname = para.getString("empname");
  		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql("   from odssu.empinfor a ");
  		de.addSql("  where empname = :empname ");
		this.de.setString("empname", empname);
		DataStore empVds = this.de.query();
		String isexistsflag = "0";
		if(empVds.size() > 0){
			isexistsflag = "1";
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("isexistsflag", isexistsflag);
		return vdo;
	}
	
	public DataObject checkUactId(DataObject para) throws AppException, BusinessException{
		String uactid = para.getString("uactid");

  		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql("   from odssu.empinfor a ");
  		de.addSql("  where uactid = :uactid ");
		this.de.setString("uactid", uactid);
		DataStore empVds = this.de.query();
		String isexistsflag = "0";
		if(empVds.size() > 0){
			isexistsflag = "1";
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("isexistsflag", isexistsflag);
		return vdo;
	}

	/**
	 * 方法简介：检查用户名是否存在
	 * 郑海杰  2015-11-11
	 */
	public DataObject checkLoginname(DataObject para) throws Exception {
  		
		String loginname = para.getString("loginname");
		String empno = para.getString("empno","");
		
		String isexistsflag = "0";
		
		de.clearSql();
  		de.addSql(" select 1                   ");
  		de.addSql("   from odssu.empinfor a    ");
  		de.addSql("  where a.loginname = :para1     ");
  		if(StringUtils.isNotBlank(empno)) {
  			de.addSql("    and a.empno <> :empno ");
  			de.setString("empno", empno);
  		}
		de.setString("para1", loginname.toUpperCase());
		DataStore vds_loginname=de.query();
		if(vds_loginname.rowCount()>0){
			isexistsflag = "1";
		}
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("isexistsflag", isexistsflag);
		return vdo;
	}
	
	

	/**
	 * 跳转进入新增人员,确定人员人事信息
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-13
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject fwPageEmpSinfo(DataObject para) throws Exception {
		// 流程开始获取piid
		String piid = para.getString("piid");
		// 查询工单信息
		DataObject rdo = getGdxx(piid);
		DataStore gdxxds = rdo.getDataStore("gdxxds");
		DataObject vdo = DataObject.getInstance();
		vdo.put("empds", gdxxds);
		vdo.put("piid", piid);
		return vdo;
	}

	/**
	 * 保存新增人员的人事信息
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-6-16
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveEmpSinfo(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String piid, empno, belongorgname;
		String rname, hrbelong, gender, officetel, mphone, email,loginname,idcardno,empname;
  		de.clearSql();

		piid = para.getString("piid");
		empno = para.getString("empno");
		empname = para.getString("empname");
		rname = para.getString("rname");
//		hrbelong = para.getString("hrbelong");
//		belongorgname = para.getString("belongorgname");
		gender = para.getString("gender");
		officetel = para.getString("officetel", "");
		mphone = para.getString("mphone", "");
		email = para.getString("email", "");
		loginname = para.getString("loginname","");
		idcardno = para.getString("idcardno");
		String uactid = para.getString("uactid","");

		
		// 判空
		if (empno == null || empno.trim().isEmpty()) {
			this.bizException("人员编号不能为空，保存失败！");
		}
		if (rname == null || rname.trim().isEmpty()) {
			this.bizException("人员姓名不能为空，保存失败！");
		}
		if (empname == null || empname.trim().isEmpty()) {
			this.bizException("人员姓名不能为空，保存失败！");
		}
		if (empname.length()>19){
			this.bizException("人员标识姓名的长度过长，请重新输入！");
		}
		if (loginname == null || loginname.trim().isEmpty()) {
			this.bizException("人员登录名不能为空，保存失败！");
		}
//		if (hrbelong == null || hrbelong.trim().isEmpty()) {
//			this.bizException("人员隶属机构不能为空，保存失败！");
//		}
		if (gender == null || gender.trim().isEmpty()) {
			this.bizException("人员性别不能为空,保存失败！");
		}

//		if (OdssuUtil.isOrgExist(hrbelong) == false) {
//			this.bizException("人事隶属机构【" + hrbelong + "】不存在，保存失败！");
//		}
//		if (OdssuUtil.isOrgOnWork(hrbelong) == false) {
//			this.bizException("人事隶属机构【" + hrbelong + "】已经被注销，保存失败！");
//		}

//		sb.setLength(0);
//		sb.append(" select orgno , orgname ");
//		sb.append("  from odssu.orginfor ");
//		sb.append(" where orgno = ? ");
//
//		this.sql.setSql(sb.toString());
//		this.sql.setString(1, hrbelong);
//
//		DataStore orgds = this.sql.executeQuery();
//		if(orgds == null || orgds.rowCount() == 0){
//			this.bizException("获取机构编号为【" + hrbelong + "】的机构相关信息时出错！");
//		}
//		String orgname = orgds.getString(0, "orgname");
//		if (!orgname.equals(belongorgname)) {
//			this.bizException("人事隶属机构编号【 " + hrbelong + "】与隶属机构名称【"
//					+ belongorgname + "】不对应！");
//		}

		// 判断参数合法性

		if ("[自动生成]".equals(empno) == false) {

			if (empno.matches("[0-9A-Za-z_]*") == false) {
				this.bizException("人员编号中含有除数字、字母、下划线之外的其他字符，生成机构编号失败！");
			}
		}
		
		//判断人员登录名的合法性
		if (loginname.matches("[0-9A-Za-z]*") == false) {
			this.bizException("人员登录名中只能含有数字、字母！");
		}

		// 判断机构编号的长度合不合法
		if (empno.length() < 1 || empno.length() > 20) {
			this.bizException("人员编号的长度不合法，长度应该在" + 1 + "与" + 20 + "之间!");
		}

		if (OdssuUtil.isEmpExist(empno)) {
			this.bizException("编号【" + empno + "】已存在！无法再次新增");
		}

		if(StringUtils.isNotBlank(uactid)) {
			de.clearSql();
	  		de.addSql(" select 1 ");
	  		de.addSql("   from odssu.empinfor a ");
	  		de.addSql("  where uactid = :uactid ");
			this.de.setString("uactid", uactid);
			DataStore empVds = this.de.query();
			if(empVds.size() > 0){
				this.bizException("选择的人员已存在，无法新增！");
			}
		}
		// 保存到工单表
		de.clearSql();
  		de.addSql(" update odssuws.ryjbxxxzkjb                         ");
  		de.addSql("    set empno =:empno,empname = :empname ,rname =:rname, gender=:gender,   ");
  		de.addSql("        officetel= :officetel,mphone=:mphone,email=:email,loginname = :loginname,idcardno = :idcardno ");
  		if(StringUtils.isNotBlank(uactid)) {
  			de.addSql(" , uactid=:uactid,uact =:uact,uactusername=:uactusername   ");
  			String uact = para.getString("uact","");
  			String uactusername = para.getString("username","");
  			this.de.setString("uactid", uactid);
  			this.de.setString("uact", uact);
  			this.de.setString("uactusername", uactusername);
  		}
  		de.addSql(" where piid = :piid ");
		this.de.setString("empno", empno);
		this.de.setString("empname", empname);
		this.de.setString("rname", rname);
		this.de.setString("gender", gender);
		this.de.setString("officetel", officetel);
		this.de.setString("mphone", mphone);
		this.de.setString("email", email);
		this.de.setString("loginname", loginname);
		this.de.setString("idcardno", idcardno);
		this.de.setString("piid", piid);

		int result = this.de.update();
		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}
		
		ProcessUtil.setTEEVarByPiid(piid, "empname", empname);
		
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
		String piid, rname = "", empname = "";
  		de.clearSql();

		piid = para.getString("piid");

		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from odssuws.ryjbxxxzkjb  ");
  		de.addSql(" where piid = :piid		  ");
		this.de.setString("piid", piid);
		empds = this.de.query();
		if (empds.rowCount() > 0) {
			rname = empds.getString(0, "rname");
			if ("".equals(empname) || empname == null) {
				empname = rname;
			}
		}

		de.clearSql();
  		de.addSql("select empno, rname, empname	");
  		de.addSql("  from odssu.empinfor  ");
  		de.addSql(" where rname = :rname ");
		this.de.setString("rname", rname);
		empnameds = this.de.query();

		empinfods.put(0, "empname", empname);
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
		Date today = DateUtil.getCurrentDate();
		String flag = "true";

		piid = para.getString("piid");
		empname = para.getString("empname");

		// 判空
		if (empname == null || empname.trim().isEmpty()) {
			this.bizException("人员标志姓名不能为空！");
		}

		de.clearSql();
  		de.addSql(" select 1  from odssu.empinfor where empname = :empname ");
		this.de.setString("empname", empname);

		DataStore empds = this.de.query();

		if (empds.rowCount() != 0) {
			flag = "repeat";
			vdo.put("flag", flag);
			return vdo;
		}

		de.clearSql();
  		de.addSql(" select empname from odssuws.ryjbxxxzkjb where piid = :piid ");
		de.setString("piid", piid);

		DataStore nameds = de.query();

		if (nameds.rowCount() > 0) {
			String oldname = nameds.getString(0, "empname");

			if (empname.equals(oldname)) {
				this.bizException("请修改人员标识姓名在进行保存！");
			}
		}

		de.clearSql();
  		de.addSql(" select 1 from odssu.empinfor where empname = :empname ");
		de.setString("empname", empname);

		nameds = de.query();

		if (nameds.rowCount() > 0) {
			this.bizException("人员标志姓名已存在请修改！");
		}

		// 保存到工单表
		de.clearSql();
  		de.addSql(" update odssuws.ryjbxxxzkjb ");
  		de.addSql("    set empname =:empname, operator = :operator, operationtime = :today ");
  		de.addSql(" where piid = :piid ");
		this.de.setString("empname", empname);
		this.de.setString("operator", this.getUser().getUserid());
		this.de.setDateTime("today", today);
		this.de.setString("piid", piid);
		int result = this.de.update();

		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}

		return vdo;
	}

	/**
	 * 描述：获取人员信息
	 * author: sjn
	 * date: 2018年1月24日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject getEmpInfor(DataObject para) throws AppException,BusinessException{
		String piid = para.getString("piid");
  		de.clearSql();
		DataStore vds = DataStore.getInstance();

		de.clearSql();
  		de.addSql("select a.piid, ");
  		de.addSql("       a.empname, ");
  		de.addSql("       a.rname, ");
  		de.addSql("       a.loginname, ");
  		de.addSql("       a.idcardno, ");
  		de.addSql("       a.gender, ");
  		de.addSql("       a.officetel, ");
  		de.addSql("       a.mphone, ");
  		de.addSql("       a.email ");
  		de.addSql("  from odssuws.ryjbxxxzkjb a ");
  		de.addSql(" where a.piid = :piid ");
		this.de.setString("piid",piid);
		vds = this.de.query();

		if (vds.rowCount() == 0) {
			throw new AppException("没有找到piid为【"+piid+"】的新增人员信息");
		}
		
		//查询申请原因
		de.clearSql();
  		de.addSql("select a.sqyy from odssuws.requestinfor a where a.piid = :piid ");
		this.de.setString("piid",piid);
		DataStore sqds = this.de.query();
		//如果有申请原因的话就拼上申请原因
		String sqyy = "";
		if(sqds != null && sqds.rowCount() != 0){
			sqyy = sqds.getString(0, "sqyy");
			vds.put(0, "sqyy", sqyy);
		}
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("empds", vds);
		vdo.put("sqyy", sqyy);
		
		return vdo;
	}

}
