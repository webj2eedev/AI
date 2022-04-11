package com.dw.odssu.query.bpo;

import java.util.Date;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.taglib.lanePrompt.LanePromptUtil;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.workFlow.BPO;
import com.dareway.service.OdssuService;
import com.dw.util.IdcardnoTool;
import com.dw.util.OdssuUtil;
import com.dw.util.multiSortUtil.MultiSortUtil;

public class QueryBPO extends BPO{
	/**
	 * 方法简介：检查当前操作员是否有 给empno代表的预估 重置密码的权力
	 * 郑海杰  2016-3-24
	 * @throws BusinessException 
	 */
	public DataObject checkResetPasswdRight(DataObject para) throws AppException, BusinessException{
		String empno = para.getString("empno");
		String userid = this.getUser().getUserid();
		//当前操作员是否在empno所在的人社局或者二级单位拥有单位管理员角色。
		//如果有，则可以进行密码重置
		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql("   from odssu.empinfor a, ");
  		de.addSql("        odssu.ir_org_closure b, ");
  		de.addSql("        odssu.ir_emp_org_all_role e ");
  		de.addSql("  where a.empno = :empno ");
  		de.addSql("    and a.hrbelong = b.orgno ");
  		de.addSql("    and e.empno = :userid ");
  		de.addSql("    and e.orgno = b.belongorgno ");
  		de.addSql("    and e.roleno = '_ODS_ORGADMIN' ");
  		de.addSql("  union  ");
  		de.addSql(" select 1 ");
  		de.addSql("   from odssu.ir_emp_org_all_role m ");
  		de.addSql("  where m.empno = :userid ");
  		de.addSql("    and m.orgno = :orgno ");
  		de.addSql("    and m.roleno = :roleno ");
		de.setString("empno", empno);
		de.setString("userid", userid);
		de.setString("orgno", OdssuContants.ORGROOT);
		de.setString("roleno", OdssuContants.ROLE_ODS_SYSADMIN);
		DataStore checkRightVds = de.query();
		DataObject result = DataObject.getInstance();
		
		boolean hasRightFlag = false;
		if(checkRightVds.size() > 0){
			hasRightFlag = true;
			result.put("hasrihtflag", hasRightFlag);
			result.put("orgname", "");
			return result;
		}else{
			de.clearSql();
  			de.addSql(" select c.orgname ");
  			de.addSql("   from odssu.empinfor a, ");
  			de.addSql("        odssu.ir_org_closure b, ");
  			de.addSql("        odssu.orginfor c ");
  			de.addSql("  where a.empno = :empno ");
  			de.addSql("    and a.hrbelong = b.orgno ");
  			de.addSql("    and b.belongorgno = c.orgno ");
			de.setString("empno", empno);
			
			DataStore orgNameVds = de.query();
			StringBuffer dealMehodBF = new StringBuffer();
			if(orgNameVds == null || orgNameVds.size() == 0){
				this.bizException("未找到编号为【" + empno + "】人员的隶属机构所属的人社厅、局、二级单位");
			}else{
				dealMehodBF.append("能办理此业务的有:");
				for(int i = 0; i < orgNameVds.size(); i++){
					String orgname = orgNameVds.getString(i, "orgname");
					dealMehodBF.append("【" + orgname + "】");
					if(i < orgNameVds.size() - 1){
						dealMehodBF.append("或者");
					}
				}
				dealMehodBF.append("的【单位管理员】");
			}
			result.put("hasrihtflag", hasRightFlag);
			result.put("orgname", dealMehodBF.toString());
			return result;
		}
	}

	/**
	 * 重置用户名密码
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-17
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject resetYhmMm(final DataObject para) throws Exception {
		String empno = para.getString("empno");
		de.clearSql();
  		de.addSql(" select sleepflag,idcardno,empname from odssu.empinfor where empno = :empno ");
		de.setString("empno", empno);
		DataStore vds = de.query();

		if (vds == null||vds.rowCount() == 0) {
			this.bizException("没有找到编号为【" + empno + "】的人员的基本信息，重置失败！");
		}
		String sleepflag = vds.getString(0, "sleepflag");
		String idcardno = vds.getString(0, "idcardno");
		String empname = vds.getString(0, "empname");
		if ("1".equals(sleepflag)) {
			this.bizException("此人员已经离职，重置失败！");
		}
		Date lasttime = DateUtil.getCurrentDate();
		lasttime = DateUtil.addMonth(lasttime, -3);
		lasttime = DateUtil.addDay(lasttime, 3);
		if(idcardno == null || "".equals(idcardno)){
			String password = OdssuUtil.hex_md5("sa");
			password = OdssuUtil.hex_md5(password);
			password = OdssuUtil.hex_md5(empno + password);

			de.clearSql();
  			de.addSql(" update odssu.empinfor  ");
  			de.addSql("       set   password = :password,last_time =:lasttime ");
  			de.addSql(" where empno = :empno ");
			de.setString("password", password);
			de.setDateTime("lasttime", lasttime);
			de.setString("empno", empno);
			de.update();
			DataObject result = DataObject.getInstance();
			result.put("idcardno", "null");
			result.put("tipinfor", "因为【"+empname+"】的身份证号为空，重置密码为sa！");
			return result;
		}else{
			int passwordIndex = idcardno.length()>6 ? idcardno.length()-6 : 0;
			String password = OdssuUtil.hex_md5(idcardno.substring(passwordIndex));
			password = OdssuUtil.hex_md5(password);
			password = OdssuUtil.hex_md5(empno + password);

			de.clearSql();
  			de.addSql(" update odssu.empinfor  ");
  			de.addSql("    set password = :password,last_time =:lasttime ");
  			de.addSql("  where empno = :empno ");
			de.setString("password", password);
			de.setDateTime("lasttime", lasttime);
			de.setString("empno", empno);
			de.update();
			DataObject result = DataObject.getInstance();
 			result.put("idcardno", "notnull");
 			if(passwordIndex == 12) {
 				result.put("tipinfor", "重置密码为默认密码成功！");
 			}else {
 				result.put("tipinfor", "身份证号不合法，请及时修改！密码已重置为当前身份证号后六位。");
 			}
			return result;
		}
		
	}	
	
	/**
	 * 描述：重置密码高新政务客户化
	 * author: sjn
	 * date: 2017年9月25日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject resetYhmMm_3701GXGWH(final DataObject para) throws Exception {
		String empno = para.getString("empno");
    		de.clearSql();
  		de.addSql(" select * from odssu.empinfor where empno = :empno ");
		de.setString("empno", empno);
		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			this.bizException("没有找到编号为【" + empno + "】的人员的基本信息，重置失败！");
		}
		String sleepflag = vds.getString(0, "sleepflag");
		if ("1".equals(sleepflag)) {
			this.bizException("此人员已经离职，重置失败！");
		}
		
		String password = OdssuUtil.hex_md5("123456");
		password = OdssuUtil.hex_md5(password);
		password = OdssuUtil.hex_md5(empno + password);

		de.clearSql();
  		de.addSql(" update odssu.empinfor  ");
  		de.addSql("       set   password = :password ");
  		de.addSql(" where empno = :empno ");
		de.setString("password", password);
		de.setString("empno", empno);
		de.update();
		DataObject result = DataObject.getInstance();
		result.put("idcardno", "notnull");
		result.put("tipinfor", "重置密码为123456成功！");
		return result;
	}
	
	/**
	 * 
	 * @Description: 进入批量重置密码页面 - 组装code
	 * @author 能天宇
	 * @date 2016-9-29 下午3:48:15
	 */
	public DataObject resBatchResetPassword(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		de.clearSql();
  		de.addSql("select typeno value, typename content ");
  		de.addSql("  from odssu.org_type ");
  		de.addSql(" where typenature <> 'A' ");
		DataStore ds = de.query();
		if (ds== null || ds.rowCount() == 0) {
			throw new AppException("表【odssu.org_type】中找不到可用的机构类型！");
		}
		vdo.put("ds", ds);
		return vdo;
	}
	/**
	 * 
	 * @Description: 测试批量重置密码
	 * @author 能天宇
	 * @date 2016-9-29 下午5:19:28
	 */
	public DataObject testBatchResetPassword(final DataObject para) throws Exception {
		String orgtypemulti = para.getString("orgtypelist");
		String newpassword = para.getString("newpassword");
		boolean alteradmin = para.getBoolean("alteradmin");
		StringBuffer logBF = new StringBuffer();
		if(orgtypemulti == null || "".equals(orgtypemulti)){
			this.bizException("传入的机构类型为空！");
		}
		if(newpassword == null || "".equals(newpassword)){
			this.bizException("传入的新密码为空！");
		}		
		newpassword = newpassword.trim();
		String[] orgtypelist = orgtypemulti.split(",");
		StringBuffer typenameBF =  new StringBuffer();	//存储日志中用到的机构类型名称
		StringBuffer typenoBF =  new StringBuffer();	//存储查询中用到的机构类型编号
		
		//校验所选的机构类型是否合法
		for(int i=0;i<orgtypelist.length;i++){
			if(!OdssuUtil.canHaveEmp(orgtypelist[i])){
				this.bizException("机构类型【"+ orgtypelist[i]+"】不可能存在下属人员！");
			}
			if(i!=0){
				typenameBF.append("、");
				typenoBF.append(",");
			}
			typenoBF.append("'"+orgtypelist[i]+"'");
			typenameBF.append(OdssuUtil.getOrgTypeNameByTypeNo(orgtypelist[i]));
		}
		
		//查询要更新密码的用户总数
		
		de.clearSql();
  		de.addSql("select count(*) empsum ");
  		de.addSql("from odssu.ir_emp_org  a,odssu.orginfor b,odssu.empinfor c ");
  		de.addSql("where a.orgno = b.orgno and a.empno=c.empno and a.ishrbelong = '1' ");
  		de.addSql(" and b.orgtype in ("+typenoBF.toString()+") ");
		if(alteradmin ==false){
  			de.addSql(" and c.loginname <> 'ADMIN' ");
		};
		DataStore empsumVds = de.query();
		if (empsumVds == null || empsumVds.rowCount() == 0) {
			throw new AppException("无法从表【odssu.ir_emp_org】查询到用户总数！");
		}
		String empsum = String.valueOf(empsumVds.getInt(0, "empsum"));
		
		//查询更新包含的机构总数机构
		de.clearSql();
  		de.addSql("select count(*) orgsum from odssu.orginfor ");
  		de.addSql(" where  orgtype in ("+typenoBF.toString()+") ");
		DataStore orgsumVds = de.query();
		if (orgsumVds == null ||orgsumVds.rowCount() == 0) {
			throw new AppException("无法从表【odssu.orginfor】查询到机构总数！！");
		}
		String orgsum = String.valueOf(orgsumVds.getInt(0, "orgsum"));
		
		//每次大循环 即对一类机构的用户密码进行更新
		LanePromptUtil.promptToTrace("即将更新机构类型为【"+typenameBF.toString()+"】的"+orgsum+"个机构下的"+empsum+"个用户的密码为【"+newpassword+"】。");
		logBF.append("***************************************************************************\r\n");
		logBF.append("即将更新机构类型为【"+typenameBF.toString()+"】的"+orgsum+"个机构下的"+empsum+"个用户的密码为【"+newpassword+"】。\r\n");
		logBF.append("***************************************************************************\r\n");
		for(int i = 0 ; i < orgtypelist.length;i++){
			String orgcount = OdssuUtil.getOrgCountOfOrgType(orgtypelist[i]);
			String typename =  OdssuUtil.getOrgTypeNameByTypeNo(orgtypelist[i]);

			de.clearSql();
  			de.addSql("select a.empno ,a.orgno  ");
  			de.addSql("  from odssu.ir_emp_org  a,odssu.orginfor b,odssu.empinfor c ");
  			de.addSql(" where a.orgno = b.orgno and a.empno=c.empno  and a.ishrbelong = '1' ");
  			de.addSql("   and b.orgtype = :orgtypelist  ");
  			de.setString("orgtypelist", orgtypelist[i]);
			if(alteradmin == false ){
  				de.addSql(" and c.loginname <> 'ADMIN' ");
			};
			DataStore empVds = de.query();
			if (empVds == null || empVds.rowCount() == 0) {
				LanePromptUtil.promptToTrace("机构类型为【"+typename+"】的"+orgcount+"个机构下没有用户，跳过 。");			
				logBF.append("机构类型为【"+typename+"】的"+orgcount+"个机构下没有用户，跳过 。\r\n");
				continue;
			}
			LanePromptUtil.promptToTrace("即将更新机构类型为【"+typename+"】的"+orgcount+"个机构下的"+empVds.rowCount()+"个用户的密码为【"+newpassword+"】。");			
			logBF.append("即将更新机构类型为【"+typename+"】的"+orgcount+"个机构下的"+empVds.rowCount()+"个用户的密码为【"+newpassword+"】。\r\n");
			
			//每次小循环就是更新了一个用户的密码
			for (int j = 0; j < empVds.rowCount();j++) {
				String empno = empVds.getString(j, "empno");
				String orgno = empVds.getString(j, "orgno");
				String empname = OdssuUtil.getEmpNameByEmpno(empno);
				String orgname = OdssuUtil.getOrgNameByOrgno(orgno);
				
				String pw = OdssuUtil.hex_md5(newpassword);
				pw = OdssuUtil.hex_md5(pw);
				String realpassword = OdssuUtil.hex_md5(empno + pw);
  				de.clearSql();
  				de.addSql(" update odssu.empinfor set password = :realpassword where empno= :empno ");
				de.setString("realpassword", realpassword);
				de.setString("empno", empno);
				int rowcount =  de.update();
				if(rowcount != 1){
					throw new AppException("【"+(j+1)+"】更新机构["+orgname+"]的用户["+empname+"]的密码为["+newpassword+"]时，操作失败!");
				}
				LanePromptUtil.promptToTrace("【"+(j+1)+"】更新了机构["+orgname+"]用户["+empname+"]的密码为["+newpassword+"];");
				logBF.append("【"+(j+1)+"】更新了机构["+orgname+"]用户["+empname+"]的密码为["+newpassword+"];\r\n");
			}
			LanePromptUtil.promptToTrace("完成更新机构类型为【"+typename+"】的"+orgcount+"个机构下的"+empVds.rowCount()+"个用户的密码为【"+newpassword+"】。");
			logBF.append("完成更新机构类型为【"+typename+"】的"+orgcount+"个机构下的"+empVds.rowCount()+"个用户的密码为【"+newpassword+"】。\r\n");
			logBF.append("***************************************************************************\r\n");
		}
		LanePromptUtil.promptToTrace("完成更新机构类型为【"+typenameBF.toString()+"】的"+orgsum+"个机构下的"+empsum+"个用户的密码为【"+newpassword+"】。");
		logBF.append("完成更新机构类型为【"+typenameBF.toString()+"】的"+orgsum+"个机构下的"+empsum+"个用户的密码为【"+newpassword+"】。\r\n");
		logBF.append("***************************************************************************\r\n");
		de.rollback();
		LanePromptUtil.promptToTrace("测试完成 - 数据回滚！");
		logBF.append("测试完成 - 数据回滚！\r\n");
		LanePromptUtil.complete();
		DataObject result = DataObject.getInstance();
		result.put("plczmmlogstr",logBF.toString());
		return result;
	}
	/**
	 * 
	 * @Description: 正式执行-批量重置密码
	 * @author 能天宇
	 * @date 2016-9-29 下午5:19:28
	 */
	public DataObject confirmBatchResetPassword(final DataObject para) throws Exception {
		String orgtypemulti = para.getString("orgtypelist");
		String newpassword = para.getString("newpassword");
		
		boolean alteradmin = para.getBoolean("alteradmin");
		StringBuffer logBF = new StringBuffer();
		if(orgtypemulti == null || "".equals(orgtypemulti)){
			this.bizException("传入的机构类型为空！");
		}
		if(newpassword == null || "".equals(newpassword)){
			this.bizException("传入的新密码为空！");
		}		
		newpassword = newpassword.trim();
		String[] orgtypelist = orgtypemulti.split(",");
		StringBuffer typenameBF =  new StringBuffer();	//日志中用到的存储机构类型名称
		StringBuffer typenoBF =  new StringBuffer();	//查询中用到的存储机构类型编号
		
		//校验所选的机构类型是否合法
		for(int i=0;i<orgtypelist.length;i++){
			if(!OdssuUtil.canHaveEmp(orgtypelist[i])){
				this.bizException("机构类型【"+ orgtypelist[i]+"】不可能存在下属人员！");
			}
			if(i!=0){
				typenameBF.append("、");
				typenoBF.append(",");
			}
			typenoBF.append("'"+orgtypelist[i]+"'");
			typenameBF.append(OdssuUtil.getOrgTypeNameByTypeNo(orgtypelist[i]));
		}
		
		//查询要更新密码的用户总数

		de.clearSql();
  		de.addSql("select count(*) empsum ");
  		de.addSql("from odssu.ir_emp_org  a,odssu.orginfor b,odssu.empinfor c ");
  		de.addSql("where a.orgno = b.orgno and a.empno=c.empno and a.ishrbelong = '1' ");
  		de.addSql(" and b.orgtype in ("+typenoBF.toString()+") ");
  		de.addSql(" and c.loginname <> 'ADMIN' ");
		DataStore empsumVds = de.query();
		if (empsumVds == null || empsumVds.rowCount() == 0) {
			throw new AppException("无法从表【odssu.ir_emp_org】查询到用户总数！");
		}
		String empsum = String.valueOf(empsumVds.getInt(0, "empsum"));
		//查询更新包含的机构总数机构
		de.clearSql();
  		de.addSql("select count(*) orgsum from odssu.orginfor ");
  		de.addSql(" where  orgtype in ("+typenoBF.toString()+") ");
		DataStore orgsumVds = de.query();
		if (orgsumVds == null ||orgsumVds.rowCount() == 0) {
			throw new AppException("无法从表【odssu.orginfor】查询到机构总数！！");
		}
		String orgsum = String.valueOf(orgsumVds.getInt(0, "orgsum"));
		
		//每次大循环 即对一类机构的用户密码进行更新
		LanePromptUtil.promptToTrace("即将更新机构类型为【"+typenameBF.toString()+"】的"+orgsum+"个机构下的"+empsum+"个用户的密码为【"+newpassword+"】。");
		logBF.append("***************************************************************************\r\n");
		logBF.append("即将更新机构类型为【"+typenameBF.toString()+"】的"+orgsum+"个机构下的"+empsum+"个用户的密码为【"+newpassword+"】。\r\n");
		logBF.append("***************************************************************************\r\n");
		for(int i = 0 ; i < orgtypelist.length;i++){
			String orgcount = OdssuUtil.getOrgCountOfOrgType(orgtypelist[i]);
			String typename =  OdssuUtil.getOrgTypeNameByTypeNo(orgtypelist[i]);
			de.clearSql();
  			de.addSql("select a.empno ,a.orgno  ");
  			de.addSql("  from odssu.ir_emp_org  a,odssu.orginfor b,odssu.empinfor c ");
  			de.addSql(" where a.orgno = b.orgno and a.empno=c.empno  and a.ishrbelong = '1' ");
  			de.addSql("   and b.orgtype = :orgtypelist ");
  			de.setString("orgtypelist", orgtypelist[i]);
			if(alteradmin == false ){
  				de.addSql("   and c.loginname <> 'ADMIN' ");
			};
			DataStore empVds = de.query();
			if (empVds == null || empVds.rowCount() == 0) {
				LanePromptUtil.promptToTrace("机构类型为【"+typename+"】的"+orgcount+"个机构下没有用户，跳过 。");			
				logBF.append("机构类型为【"+typename+"】的"+orgcount+"个机构下没有用户，跳过 。\r\n");
				continue;
			}
			LanePromptUtil.promptToTrace("即将更新机构类型为【"+typename+"】的"+orgcount+"个机构下的"+empVds.rowCount()+"个用户的密码为【"+newpassword+"】。");			
			logBF.append("即将更新机构类型为【"+typename+"】的"+orgcount+"个机构下的"+empVds.rowCount()+"个用户的密码为【"+newpassword+"】。\r\n");
			
			//每次小循环就是更新了一个用户的密码
			for (int j = 0; j < empVds.rowCount();j++) {
				String empno = empVds.getString(j, "empno");
				String orgno = empVds.getString(j, "orgno");
				String empname = OdssuUtil.getEmpNameByEmpno(empno);
				String orgname = OdssuUtil.getOrgNameByOrgno(orgno);

				String pw = OdssuUtil.hex_md5(newpassword);
				pw = OdssuUtil.hex_md5(pw);
				String realpassword = OdssuUtil.hex_md5(empno + pw);
  				de.clearSql();
  				de.addSql(" update odssu.empinfor set password = :realpassword where empno= :empno ");
				de.setString("realpassword", realpassword);
				de.setString("empno", empno);
				int rowcount =  de.update();
				if(rowcount != 1){
					throw new AppException("【"+(j+1)+"】更新机构["+orgname+"]的用户["+empname+"]的密码为["+newpassword+"]时，操作失败!");
				}
				LanePromptUtil.promptToTrace("【"+(j+1)+"】更新了机构["+orgname+"]用户["+empname+"]的密码为["+newpassword+"];");
				logBF.append("【"+(j+1)+"】更新了机构["+orgname+"]用户["+empname+"]的密码为["+newpassword+"];\r\n");
			}
			LanePromptUtil.promptToTrace("完成更新机构类型为【"+typename+"】的"+orgcount+"个机构下的"+empVds.rowCount()+"个用户的密码为【"+newpassword+"】。");
			logBF.append("完成更新机构类型为【"+typename+"】的"+orgcount+"个机构下的"+empVds.rowCount()+"个用户的密码为【"+newpassword+"】。\r\n");
			logBF.append("***************************************************************************\r\n");
		}
		LanePromptUtil.promptToTrace("完成更新机构类型为【"+typenameBF.toString()+"】的"+orgsum+"个机构下的"+empsum+"个用户的密码为【"+newpassword+"】。");
		logBF.append("完成更新机构类型为【"+typenameBF.toString()+"】的"+orgsum+"个机构下的"+empsum+"个用户的密码为【"+newpassword+"】。\r\n");
		logBF.append("***************************************************************************\r\n");
		LanePromptUtil.complete();
		DataObject result = DataObject.getInstance();
		result.put("plczmmlogstr",logBF.toString());
		return result;
	}
	/*
	 * 首页查询机构下属人员的信息
	 * @author liuy
	 * @version 1.0 创建时间 2014-05-19
	 */
	public DataObject queryOrgSubEmpInfoView(final DataObject para) throws Exception {
		String orgno = para.getString("orgno");
    		de.clearSql();
		de.clearSql();
  		de.addSql(" select u.empno,u.empname,u.gender,u.email,u.loginname username, ");
  		de.addSql("        u.officetel, u.sleepflag,u.mphone,v.ishrbelong,' ' rolename ");
  		de.addSql("   from   odssu.empinfor u, ");
  		de.addSql("        odssu.ir_emp_org v ");
  		de.addSql("  where  v.empno = u.empno ");
  		de.addSql("    and v.orgno =:orgno ");
		de.setString("orgno", orgno);
		DataStore empvds = de.query();
		for(int i = 0; i < empvds.size(); i++){
			String empno = empvds.getString(i, "empno");
			de.clearSql();
  			de.addSql(" select rolename,b.rolesn ");
  			de.addSql("   from odssu.ir_emp_org_all_role a, ");
  			de.addSql("        odssu.roleinfor b ");
  			de.addSql("  where a.empno = :empno ");
  			de.addSql("    and a.orgno = :orgno ");
  			de.addSql("    and a.roleno = b.roleno ");
  			de.addSql("    and a.rolenature = :rolenature_cyjs ");
  			de.addSql("    and a.roleno <> 'MEMBER' ");
  			de.addSql("    and a.jsgn = :jsgn_post ");
			this.de.setString("empno", empno);
			this.de.setString("orgno", orgno);
			this.de.setString("rolenature_cyjs", OdssuContants.ROLENATURE_CYJS);
			this.de.setString("jsgn_post", OdssuContants.JSGN_POST);
			
			
			DataStore roleNameVds = this.de.query();
			StringBuffer roleNameBF = new StringBuffer();
			roleNameBF.append("");
			int rolesn = 100;
			for(int j = 0; j < roleNameVds.size(); j++){
				String roleName = roleNameVds.getString(j, "rolename");
				Integer rolesnInt = roleNameVds.getInt(j, "rolesn");
				if(rolesnInt != null && rolesnInt != 0){
					if(rolesnInt < rolesn){
						rolesn = rolesnInt;
					}
				}
				roleNameBF.append(roleName+",");
			}
			if(roleNameBF.length()>0){
				roleNameBF.deleteCharAt(roleNameBF.length()-1);
			}
			empvds.put(i, "rolename", roleNameBF.toString());
			empvds.put(i, "rolesn", rolesn);
		}

		de.clearSql();
  		de.addSql(" select * from odssu.ir_emp_org ieo  ");
  		de.addSql(" where ieo.orgno in  ");
  		de.addSql(" (select ioc.orgno  ");
  		de.addSql("  from  odssu.ir_org_closure ioc  ");
  		de.addSql(" where ioc.belongorgno = :orgno ) ");
		de.setString("orgno", orgno);
		DataStore vds2 = de.query();
		empvds.sort("rolename:desc");
		DataObject vdo = DataObject.getInstance();
		MultiSortUtil.multiSortDS(empvds, "rolesn:asc,username:asc");
		vdo.put("empds", empvds);
		vdo.put("rs", empvds.rowCount());
		vdo.put("syrs", vds2.rowCount());
		return vdo;
	}

	/**
	 * 根据belongorgno和orgtype查询机构信息
	 * 
	 * @Description:
	 * @author 叶军
	 * @date 2014-9-24
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject queryGatherInfo(final DataObject para) throws Exception {
		String belongorgno = para.getString("belongorgno");
		String typeno = para.getString("suborgtype");// 机构类型编号

		de.clearSql();
		DataObject vdo = DataObject.getInstance();
		DataStore vds;

		de.clearSql();
  		de.addSql("select a.orgno,a.displayname,a.orgname,c.typename,c.typenature,    ");
  		de.addSql("       b.orgname belongorgname                                     ");
  		de.addSql("  from odssu.orginfor a,                                           ");
  		de.addSql("       odssu.orginfor b,                                           ");
  		de.addSql("       odssu.org_type c                                            ");
  		de.addSql(" where a.belongorgno= b.orgno                                      ");
  		de.addSql("   and a.orgtype = c.typeno                                        ");
  		de.addSql("   and a.belongorgno = :belongorgno                                           ");
  		de.addSql("   and a.orgtype = :typeno                                               ");
  		de.addSql(" order by a.orgno ");
		de.setString("belongorgno", belongorgno);
		de.setString("typeno", typeno);
		vds = de.query();
		vdo.put("vds", vds);
		return vdo;
		
		
	}

	/*
	 * 首页人员查询
	 * @author liuy
	 * @version 1.0 创建时间 2014-05-19
	 */
	public DataObject queryEmpInfor(final DataObject para) throws Exception {
		String username = para.getString("username").toUpperCase();
		String empname = para.getString("empname");

		username = ((username == null || "".equals(username)) ? "%" : "%" + username + "%");
    		de.clearSql();
  		de.addSql(" select u.empno,u.empname,u.gender,u.email,u.officetel,u.loginname username,  ");
  		de.addSql("        u.sleepflag,u.mphone,o.orgno,u.idcardno,  ");
  		de.addSql("        o.orgname,o.displayname ");
  		de.addSql("   from odssu.empinfor u left outer join ");
  		de.addSql("        odssu.orginfor o ");
  		de.addSql("  		on u.hrbelong = o.orgno  ");
  		de.addSql("    where (u.loginname like  :username ");
  		de.addSql("     or u.idcardno like :username ");
  		de.addSql("     or u.empname like :username ");
  		de.addSql("     or u.rname like :username ");
  		de.addSql("     or u.empnamepy like :username ");
  		de.addSql("     or u.rnamepy like :username )");
		if (empname != null && !empname.isEmpty()) {
  		de.addSql("and u.empname = :empname  ");
  		de.setString("empname", empname);
		}
  		de.addSql("  order by u.sleepflag,u.empno,u.empname ");
		de.setString("username", username);

		DataStore vds = de.query();
		DataObject vdo = DataObject.getInstance();
		vdo.put("empds", vds);
		return vdo;
	}

	/*
	 * 首页机构查询
	 * @author liuy
	 * @version 1.0 创建时间 2014-05-19
	 */
	public DataObject queryOrgInfor(final DataObject para) throws Exception {
		String orgno = para.getString("orgno");
		String orgnamepy = para.getString("orgnamepy");
		orgnamepy = orgnamepy.toUpperCase();
		String orgname = para.getString("orgname");
		String sleepflag = para.getString("sleepflag");
		String orgtype = para.getString("typeno");
  		de.clearSql();
  		de.addSql(" select g.orgno,g.orgname,g.fullname,g.displayname,g.sleepflag,g.belongorgno,  ");
  		de.addSql("        g2.orgname belongorgname,t.typeno,t.typename ");
  		de.addSql(" from   odssu.orginfor g left outer join odssu.orginfor g2 ");
  		de.addSql("        on g.belongorgno = g2.orgno, ");
  		de.addSql("        odssu.orginfor g1 left outer join odssu.org_type t  ");
  		de.addSql("   		on g1.orgtype = t.typeno ");
  		de.addSql(" where  1=1  ");
		if (orgno != null && orgno.trim().isEmpty() == false) {
  			de.addSql("  and g.orgno like :orgno      ");
  			de.setString("orgno",'%' + orgno + '%');
		}
		if (orgnamepy != null && orgnamepy.trim().isEmpty() == false) {
  			de.addSql(" and (g.orgnamepy like :orgnamepy       ");
  			de.addSql(" or g.fullnamepy like :orgnamepy        ");
  			de.addSql(" or g.displaynamepy like :orgnamepy )   ");
  			de.setString("orgnamepy","'%' + orgnamepy + '%'");
		}
		if (orgname != null && orgname.trim().isEmpty() == false) {
  			de.addSql(" and ( g.orgname like :orgname ");
  			de.addSql(" or g.displayname like :orgname ");
  			de.addSql(" or g.fullname like :orgname) ");
  			de.setString("orgname",'%' + orgname + '%');
		}
		if (sleepflag != null && sleepflag.trim().isEmpty() == false) {
  			de.addSql(" and g.sleepflag = '" + sleepflag + "' ");
		}
		if (orgtype != null && orgtype.trim().isEmpty() == false) {
  			de.addSql(" and g.orgtype like :orgtype ");
  			de.setString("orgtype",'%' + orgtype + '%');
		}
  		de.addSql(" order by g.sleepflag,g.orgtype,g.orgno,orgname  ");

		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("orgds", vds);
		return vdo;
	}

	/**
	 * 首页上的角色查询的查询
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-18
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject queryRoleInfor(final DataObject para) throws Exception {
		String roleno = para.getString("roleno");
		String rolenamepy = para.getString("rolenamepy");
		rolenamepy = rolenamepy.toUpperCase();
		String rolename = para.getString("rolename");
		String sleepflag = para.getString("sleepflag");
		String roletype = para.getString("roletype");
		String typeno = para.getString("typeno");
		de.clearSql();
  		de.addSql(" select r.roleno,r.rolename,r.sleepflag,r.displayname,o.orgno,o.orgname, ");
  		de.addSql("    o.displayname orgdisplayname,t.typeno,t.typename,r.rolenature ");
  		de.addSql(" from odssu.roleinfor r left outer join odssu.orginfor o ");
  		de.addSql("      on r.deforgno = o.orgno, ");
  		de.addSql("      odssu.roleinfor r1 left outer join odssu.role_type t ");
  		de.addSql(" 	  on r1.roletype = t.typeno ");
  		de.addSql(" where 1=1 ");
		if (roleno != null && roleno.trim().isEmpty() == false) {
  			de.addSql("  and r.roleno like :roleno ");
  			de.setString("roleno",'%' + roleno + '%');
		}
		if (rolenamepy != null && rolenamepy.trim().isEmpty() == false) {
  			de.addSql(" and (r.rolenamepy like :rolenamepy ");
  			de.addSql(" or r.displaynamepy like :rolenamepy) ");
  			de.setString("rolenamepy",'%' + rolenamepy + '%');
		}
		if (rolename != null && rolename.trim().isEmpty() == false) {
  			de.addSql(" and ( r.rolename like :rolename ");
  			de.addSql(" or r.displayname like :rolename) ");
  			de.setString("rolename",'%' + rolename + '%');
  			
		}
		if (sleepflag != null && sleepflag.trim().isEmpty() == false) {
  			de.addSql(" and r.sleepflag = :sleepflag ");
  			de.setString("sleepflag",sleepflag);
		}
		if (roletype != null && roletype.trim().isEmpty() == false) {
  			de.addSql(" and r.roletype like :roletype ");
  			de.setString("roletype",'%' + roletype + '%');
		}
		if (typeno != null && typeno.trim().isEmpty() == false) {
  			de.addSql(" and t.typeno like :typeno ");
  			de.setString("typeno",'%' + typeno + '%');
		}
  		de.addSql(" order by r.roleno  ");

		DataStore vds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("roleds", vds);
		return vdo;
	}

	/*
	 * 首页机构类型一览
	 * @author liuy
	 * @version 1.0 创建时间 2014-05-19
	 */
	public DataObject queryOrgTypeView(final DataObject para) throws Exception {
    		de.clearSql();
  		de.addSql(" select typeno,typename,minlength,maxlength,comments,");
  		de.addSql("        allowletter,allowunderline,qzyfjgbhdt");
  		de.addSql(" from odssu.org_type");
  		de.addSql(" order by typeno ");
		DataStore vds = de.query();

		for (int i = 0; i < vds.rowCount(); i++) {
			String typeno = vds.getString(i, "typeno");

			// 算上级类别
			de.clearSql();
  			de.addSql(" select o.typeno,o.typename ");
  			de.addSql(" from   odssu.ir_org_type i, ");
  			de.addSql("        odssu.org_type o ");
  			de.addSql(" where  i.suptypeno = o.typeno and i.subtypeno = :typeno ");
			de.setString("typeno", typeno);
			DataStore vdssjlb = de.query();

			String sjlb = "";
			for (int k = 0; k < vdssjlb.rowCount(); k++) {
				String typename = vdssjlb.getString(k, "typename");
				if ("".equals(sjlb)) {
					sjlb = typename;
				} else {
					sjlb = sjlb + "；" + typename;
				}
			}
			vds.put(i, "sjlb", sjlb);

			// 下级类别
			de.clearSql();
  			de.addSql(" select o.typeno,o.typename ");
  			de.addSql(" from   odssu.ir_org_type i, ");
  			de.addSql("        odssu.org_type o ");
  			de.addSql(" where  i.subtypeno = o.typeno and i.suptypeno = :typeno ");
			de.setString("typeno", typeno);
			DataStore vdxsjlb = de.query();

			String xjlb = "";
			for (int k = 0; k < vdxsjlb.rowCount(); k++) {
				String typename = vdxsjlb.getString(k, "typename");
				if ("".equals(xjlb)) {
					xjlb = typename;
				} else {
					xjlb = xjlb + "；" + typename;
				}
			}
			vds.put(i, "xjlb", xjlb);

			// 允许的角色类型
			de.clearSql();
  			de.addSql(" select r.typeno,r.typename ");
  			de.addSql(" from   odssu.ir_org_role_type t, ");
  			de.addSql("        odssu.role_type r ");
  			de.addSql(" where  t.roletypeno= r.typeno and t.orgtypeno = :typeno ");
			de.setString("typeno", typeno);
			DataStore vdsjslx = de.query();

			String jslx = "";
			for (int k = 0; k < vdsjslx.rowCount(); k++) {
				String typename = vdsjslx.getString(k, "typename");
				if ("".equals(jslx)) {
					jslx = typename;
				} else {
					jslx = jslx + "；" + typename;
				}
			}
			vds.put(i, "jslx", jslx);

			// 机构数
			de.clearSql();
  			de.addSql(" select count(1) jgs  from odssu.orginfor where orgtype = :typeno ");
			de.setString("typeno", typeno);
			DataStore vdxjgs = de.query();
			int jgs = 0;
			if(vdxjgs != null && vdxjgs.rowCount() != 0 ){
				jgs = vdxjgs.getInt(0, "jgs");
			}
			vds.put(i, "jgs", jgs);

		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("jglbds", vds);
		return vdo;
	}

	/*
	 * 首页角色类型一览
	 * @author liuy
	 * @version 1.0 创建时间 2014-05-19
	 */
	public DataObject queryRoleTypeView(final DataObject para) throws Exception {
    		de.clearSql();
  		de.addSql(" select typeno,typename ,comments , minlength , maxlength , allowletter  from odssu.role_type ");
  		de.addSql(" order by typeno ");
		DataStore vds = de.query();

		for (int i = 0; i < vds.rowCount(); i++) {
			String typeno = vds.getString(i, "typeno");

			// 允许的机构类型
			de.clearSql();
  			de.addSql(" select r.typeno,r.typename ");
  			de.addSql(" from   odssu.ir_org_role_type t, ");
  			de.addSql("        odssu.org_type r ");
  			de.addSql(" where  t.orgtypeno= r.typeno and t.roletypeno = :typeno ");
			de.setString("typeno", typeno);
			DataStore vdsjslx = de.query();

			String jglx = "";
			for (int k = 0; k < vdsjslx.rowCount(); k++) {
				String typename = vdsjslx.getString(k, "typename");
				if ("".equals(jglx)) {
					jglx = typename;
				} else {
					jglx = jglx + "；" + typename;
				}
			}
			vds.put(i, "jglx", jglx);
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("jslxds", vds);
		return vdo;
	}

	public DataObject getEmpNo(DataObject para) throws Exception {
		String piid = para.getString("piid");
    		de.clearSql();
  		de.addSql(" select empno from odssuws.ryjbxxxg where piid = :piid ");
		de.setString("piid", piid);
		DataStore vds = de.query();

		if (vds.rowCount() == 0) {
			this.bizException("没有找到PIID为【" + piid + "】的信息！");
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("empno", vds.getString(0, "empno"));
		return vdo;
	}

	/**
	 * 保存个人联系方式修改
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-31
	 * @param para
	 * @return
	 */
	public DataObject savegrlxfsxg(DataObject para) throws Exception {

		String empno, officetel, mphone, email;

		empno = para.getString("empno");
		officetel = para.getString("officetel");
		mphone = para.getString("mphone");
		email = para.getString("email");

		if (empno == null || empno.trim().isEmpty()) {
			this.bizException("人员编号为空！！");
		}
    		de.clearSql();
    		de.addSql("select empno , sleepflag  from odssu.empinfor where empno = :empno  ");
		this.de.setString("empno", empno);

		DataStore empds = this.de.query();

		if (empds.rowCount() == 0) {
			this.bizException("不存在人员编号为【 " + empno + "】的人员信息！");
		}

		String sleepflag = empds.getString(0, "sleepflag");

		if (sleepflag == null || sleepflag.trim().isEmpty()
				|| "1".equals(sleepflag)) {
			this.bizException("人员不是在职状态，不能进行信息的修改！");
		}

		de.clearSql();
    		de.addSql(" update odssu.empinfor ");
  		de.addSql("    set officetel = :officetel , mphone = :mphone , email = :email ");
  		de.addSql("  where empno = :empno  ");
		this.de.setString("officetel", officetel);
		this.de.setString("mphone", mphone);
		this.de.setString("email", email);
		this.de.setString("empno", empno);

		int result = 0;

		result = this.de.update();

		if (result < 0) {
			this.bizException("保存人员联系方式修改失败！");
		}
		return null;
	}

	/**
	 * 修改登录名 ———— 获取自己的登录名
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-31
	 * @param para
	 * @return
	 */
	public DataObject queryMydlmByempno(DataObject para) throws Exception {

		String empno = para.getString("empno");

		if (empno == null || empno.trim().isEmpty()) {
			this.bizException("人员编号为空！");
		}
    		de.clearSql();
    		de.addSql(" select * from odssu.empinfor where empno = :empno   ");
		this.de.setString("empno", empno);

		DataStore empds = this.de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("empds", empds);

		return vdo;
	}

	/**
	 * 保存登录名的修改
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-31
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject savedlmxg(DataObject para) throws Exception {
		String empno = para.getString("empno");
		String loginname = para.getString("loginname");
		String newloginname = para.getString("newloginname");

		if (empno == null || empno.trim().isEmpty()) {
			this.bizException("人员编号不能为空！");
		}
		if (loginname == null || loginname.trim().isEmpty()) {
			this.bizException("原登录名为空！");
		}
		if (newloginname == null || newloginname.trim().isEmpty()) {
			this.bizException("新登录名为空！");
		}

		// 检测人员信息是否存在

		de.clearSql();
  		de.addSql("select empno, sleepflag,idcardno  from odssu.empinfor where empno = :empno  ");
		de.setString("empno", empno);
		DataStore empds = de.query();

		if (empds.rowCount() == 0) {
			this.bizException("不存在人员编号为【 " + empno + "】的人员信息！");
		}

		String sleepflag = empds.getString(0, "sleepflag");
		if (sleepflag == null || sleepflag.trim().isEmpty()
				|| "1".equals(sleepflag)) {
			this.bizException("人员不是在职状态，不能进行信息的修改！");
		}
		String idcardno = empds.getString(0, "idcardno");
		newloginname = newloginname.toUpperCase();

		if (IdcardnoTool.validateCard(newloginname)
				&& idcardno.equals(newloginname) == false) {
			this.bizException("您不能使用他人的身份证号作为登录名，保存失败！");
		}

		de.clearSql();
  		de.addSql(" update odssu.empinfor set loginname = :newloginname where empno = :empno ");
		de.setString("newloginname", newloginname);
		de.setString("empno", empno);
		int result = 0;
		result = de.update();

		if (result < 0) {
			this.bizException("登录名更新失败！！");
		}

		return null;
	}

	/**
	 * 保存密码修改
	 * 
	 * @Description:
	 * @author 王具然
	 * @date 2014-7-31
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject savemaxg(DataObject para) throws Exception {
		
		DataObject result = DataObject.getInstance();
		try {
			OdssuService osbpo = new OdssuService();
			result = osbpo.dSetMyPassWord(para);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		}
		
		boolean deal_result = result.getBoolean("deal_result");
		
		DataObject vdo = DataObject.getInstance();
		String flag = "";
		
		if(deal_result){
			flag = "true";
			vdo.put("flag", flag);
		}else {
			this.bizException(result.getString("deal_message"));
		}
		
		
		return vdo;
	}
	/**
	 * 
	 * 方法简介.
	 * @author 叶军     2015-4-15
	 * @throws AppException 
	 */
	public DataObject queryLogInfor(DataObject para) throws AppException{
		String ywztbh  = para.getString("ywztbh");
		String czybh  = para.getString("czybh");
		
		String ywztmc  = para.getString("ywztmc");
		String czyxm  = para.getString("czyxm");
  		  		  		de.clearSql();
		de.clearSql();
  		de.addSql(" select distinct c.pjbh,temp.ywztbh,temp.ywztmc,d.pdid,d.pdlabel,c.czybh,e.empname czyxm,c.cjsj");
  		de.addSql("   from odssu.pj_log c,");
  		de.addSql("        bpoverhaul.process_define_in_activiti d,");
  		de.addSql("        odssu.empinfor  e,");
  		de.addSql("       (select a.orgno ywztbh, a.orgname ywztmc, '1' bz");
  		de.addSql("           from odssu.orginfor a");
  		de.addSql("          where a.sleepflag = '0'");
  		de.addSql("         union");
  		de.addSql("         select b.empno ywztbh, b.empname ywztmc, '0' bz");
  		de.addSql("           from odssu.empinfor b");
  		de.addSql("          where b.sleepflag = '0') temp");
  		de.addSql("    where c.idno = temp.ywztbh");
  		de.addSql("      and c.pdid = d.pdid");
  		de.addSql("      and c.czybh = e.empno ");
		if(ywztbh!=null&&!ywztbh.trim().isEmpty()){
  			de.addSql("      and temp.ywztbh like :ywztbh       ");
  			de.setString("ywztbh", ywztbh+'%');
		}
		if(czybh!=null&&!czybh.trim().isEmpty()){
  			de.addSql("      and c.czybh like :czybh   ");
  			de.setString("czybh", czybh+'%');
		}
		
		if(ywztmc!=null&&!ywztmc.trim().isEmpty()){
  			de.addSql("      and temp.ywztmc = :ywztmc   ");
  			de.setString("ywztmc", ywztmc);
  			
		}
		
		
		if(czyxm!=null&&!czyxm.trim().isEmpty()){
  			de.addSql("      and e.empname = :czyxm  ");
  			de.setString("czyxm", czyxm);
		}
  		de.addSql("      order by temp.ywztbh,c.czybh,c.cjsj ");
		DataStore vds = de.query();
		DataObject vdo = DataObject.getInstance();
		vdo.put("logvds", vds);
		return vdo;
	}
	
	/**
	 * 描述：批量重置登录名
	 * author: sjn
	 * date: 2017年8月21日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject resetLoginname(DataObject para) throws Exception{
  		de.clearSql();
		DataStore vds = DataStore.getInstance();
		String empnamepy = "",empno = "";

		de.clearSql();
  		de.addSql("select a.empnamepy ");
  		de.addSql("  from odssu.empinfor a ");
  		de.addSql(" where a.sleepflag = '0' ");
  		de.addSql(" group by a.empnamepy ");
  		de.addSql("having count(*) > 1 ");
		vds = this.de.query();
		//如果没有重名的情况则批量修改
		if (vds.rowCount() == 0) {
			de.clearSql();
  			de.addSql("update odssu.empinfor a set loginname = upper(empnamepy) where a.sleepflag = '0' ");
			this.de.update();
			return null;
		}
		//修改所有重名的情况
		for (int i = 0; i < vds.rowCount();i++) {
			empnamepy = vds.getString(i, "empnamepy");
			de.clearSql();
  			de.addSql("select a.empno from odssu.empinfor a where a.sleepflag = '0' and a.empnamepy = :empnamepy ");
			this.de.setString("empnamepy",empnamepy);
			DataStore tmpvds = this.de.query();
			for (int j = 1; j < tmpvds.rowCount()+1; j++) {
				String empnamepytmp = "";
				empno = tmpvds.getString(j-1, "empno");
				if (j<10) {
					empnamepytmp = empnamepy + "0" + j;
				}else {
					empnamepytmp = empnamepy + j;
				}
				de.clearSql();
  				de.addSql("update odssu.empinfor a set loginname = :loginname where a.empno = :empno");
				this.de.setString("loginname",empnamepytmp.toUpperCase());
				this.de.setString("empno",empno);
				this.de.update();
			}
		}
		//修改剩下没有重名的情况
		de.clearSql();
  		de.addSql("update odssu.empinfor a ");
  		de.addSql("   set loginname = upper(empnamepy) ");
  		de.addSql(" where a.sleepflag = '0' ");
  		de.addSql("   and a.empnamepy not in (select b.empnamepy ");
  		de.addSql("          from odssu.empinfor b ");
  		de.addSql("         where b.sleepflag = '0' ");
  		de.addSql("         group by b.empnamepy ");
  		de.addSql("        having count(*) > 1) ");
		this.de.update();
		
		return null;
	}

	/**
	 * 描述：配置应用系统 zwh 2020-02-25
	 */
	public DataObject loadAppManage(DataObject para) throws AppException{
		DataObject vdo = DataObject.getInstance();
		de.clearSql();
  		de.addSql(" select b.appid, b.appname from odssu.appinfo b order by b.appid ");
		DataStore myappds = de.query();
		vdo.put("myappds", myappds);
		return vdo;
	}

	/**
	 * 描述：保存新增的应用系统 zwh 2020-02-25
	 */
	public DataObject saveApp(DataObject para) throws AppException,BusinessException{
		String appid = para.getString("appid");
		if(appid == null || appid.equals("")) {
			throw new BusinessException("未获取到新增的应用系统编号，请检查");
		}
		String appname = para.getString("appname");
		if(appname == null || appname.equals("")) {
			throw new BusinessException("未获取到新增的应用系统名称，请检查");
		}
		
		//先判重然后新增
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql("select a.appid, a.appname from odssu.appinfo a where a.appid = :appid ");
		de.setString("appid", appid);
		DataStore vds = de.query();
		if(vds != null && vds.rowCount() >0) {
			String yyappname = vds.getString(0, "appname");
			throw new BusinessException("应用编号【"+appid+"】已存在应用系统【"+yyappname+"】，请检查核对！");
		}
		de.clearSql();
  		de.addSql("insert into odssu.appinfo (appid, appname) values (:appid, :appname) ");
  		de.setString("appid", appid);
  		de.setString("appname", appname);
		de.update();
		
		de.clearSql();
		de.addSql(" select empno ");
		de.addSql("   from odssu.ir_emp_org_all_role ");
		de.addSql("  where orgno = :orgno ");
		de.addSql("    and roleno = :roleno  ");
		de.setString("orgno", OdssuContants.ORGROOT);
		de.setString("roleno", OdssuContants.ROLE_ODS_SYSADMIN);//系统管理员
		DataStore empds = de.query();
		
		for(int i = 0; i < empds.rowCount(); i++) {
			String empno = empds.getString(i, "empno");
			de.clearSql();
			de.addSql("select 1 from odssu.emp_app a where a.appid = :appid and a.empno = :empno ");
			de.setString("appid", appid);
			de.setString("empno", empno);
			DataStore tempds = de.query();
			if(tempds == null || tempds.rowCount() == 0) {
				de.clearSql();
		  		de.addSql("insert into odssu.emp_app (appid, empno) values (:appid, :empno) ");
		  		de.setString("appid", appid);
		  		de.setString("empno", empno);
				de.update();
			}
		}
		
		return null;
	}

	/**
	 * 描述：删除应用系统 zwh 2020-02-25
	 */
	public DataObject delMyApp(DataObject para) throws AppException,BusinessException{
		String appid = para.getString("appid");
		if(appid == null || appid.equals("")) {
			throw new BusinessException("未获取到要删除应用系统的编号，请检查");
		}
		String appname = para.getString("appname");
		if(appname == null || appname.equals("")) {
			throw new BusinessException("未获取到要删除应用系统的名称，请检查");
		}
		String userid = this.getUser().getUserid();
		
		//先校验然后删除
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql("select a.empno from odssu.emp_app a where a.appid = :appid and a.empno != :empno ");
		de.setString("appid", appid);
		de.setString("empno", userid);
		DataStore tempds = de.query();
		if(tempds != null && tempds.rowCount() >0) {
			throw new BusinessException("除系统管理员外，还有"+tempds.rowCount()+"人拥有应用系统"+appname+"的操作权限，请检查核对！");
		}else {
			de.clearSql();
	  		de.addSql("delete from odssu.appinfo where appid = :appid and appname = :appname ");
	  		de.setString("appid", appid);
	  		de.setString("appname", appname);
			de.update();
			
			de.clearSql();
	  		de.addSql("delete from odssu.emp_app where appid = :appid and empno = :empno ");
	  		de.setString("appid", appid);
	  		de.setString("empno", userid);
			de.update();
		}
		
		return null;
	}

	/**
	 * 描述：加载要修改的应用系统 zwh 2020-02-25
	 */
	public DataObject loadModApp(DataObject para) throws AppException,BusinessException{
		DataStore appds = DataStore.getInstance();
		String appid = para.getString("appid");
		if(appid == null || appid.equals("")) {
			throw new BusinessException("未获取到需要修改的应用系统编号，请检查");
		}
		de.clearSql();
  		de.addSql(" select b.appid, b.appname from odssu.appinfo b where b.appid = :appid order by b.appid ");
		de.setString("appid", appid);
		DataStore myappds = de.query();
		if(myappds.rowCount() > 0) {
			DataObject tempdo = myappds.getRow(0);
			appds.addRow(tempdo);
		}else {
			throw new BusinessException("未获取到需要修改的应用信息，请检查");
		}
		
		DataObject vdo = DataObject.getInstance();
		vdo.put("appds", appds);
		return vdo;
	}

	/**
	 * 描述：保存修改的应用系统 zwh 2020-02-25
	 */
	public DataObject saveModApp(DataObject para) throws AppException,BusinessException{
		String appid = para.getString("appid");
		if(appid == null || appid.equals("")) {
			throw new BusinessException("未获取到需要修改的应用系统编号，请检查");
		}
		String appname = para.getString("appname");
		if(appname == null || appname.equals("")) {
			throw new BusinessException("未获取到需要修改的应用系统名称，请检查");
		}
		
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql("update odssu.appinfo set appname = :appname where appid = :appid ");
		de.setString("appid", appid);
		de.setString("appname", appname);
		de.update();
		
		return null;
	}
}