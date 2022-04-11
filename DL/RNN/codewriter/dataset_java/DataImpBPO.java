package com.dw.odssu.dataimp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dareway.framework.util.*;
import com.dw.odssu.acc.role.roleadjustemp.aso.RoleAdjustEmpASO;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.dareway.apps.odssu.OdssuContants;
import com.dareway.apps.odssu.OdssuNames;
import com.dareway.apps.process.util.ProcessUtil;
import com.dareway.framework.common.GlobalNames;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.ASOException;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.taglib.lanePrompt.LanePromptUtil;
import com.dareway.framework.taglib.sprompt.SPrompt;
import com.dareway.framework.taglib.sprompt.SPromptUtil;
import com.dareway.framework.workFlow.ASO;
import com.dareway.framework.workFlow.BPO;
import com.dw.log.LogManageAPI;
import com.dw.odssu.acc.duty.outerDuty.outerDuty.aso.OuterDutyAddASO;
import com.dw.odssu.acc.duty.outerDuty.outerDuty.aso.OuterDutyRemoveByDutynoASO;
import com.dw.odssu.acc.emp.ryjbxxxg.aso.EmpEditASO;
import com.dw.odssu.acc.emp.ryjbxxxz.aso.EmpAddASO;
import com.dw.odssu.acc.org.jgjbxxxz.aso.OrgAddASO;
import com.dw.util.FileUtil;
import com.dw.util.OdssuUtil;
import com.dw.util.SendMsgUtil;

import jxl.Sheet;
import jxl.Workbook;
import jxl.write.WritableWorkbook;

public class DataImpBPO extends BPO{

	ASO empaddASO = this.newASO(EmpAddASO.class);
	ASO orgaddASO = this.newASO(OrgAddASO.class);
	ASO iASO = this.newASO(RoleAdjustEmpASO.class);
	/**
	 * 
	 * @Description: 获取人社系统编号
	 * @author 能天宇
	 * @date 2016-10-12 下午4:45:34
	 */
	public DataObject queryRsxtOrgno(DataObject para) throws Exception {
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select orgno value ,orgname content from odssu.orginfor ");
  		de.addSql("where orgtype in ('HSDOMAIN_DSRSXT','HSDOMAIN_SRSXT','ZGRSYLBXGS','YB_DSYBXT','YB_SYBXT')  ");
		de.addSql(" and sleepflag = '0' ");
		DataStore dstemp = de.query();
		if (dstemp == null ||dstemp.rowCount() == 0) {
			throw new AppException("无法读取人社系统的机构信息！");
		}
		DataObject result = DataObject.getInstance();
		result.put("orgnods",  dstemp);
		result.put("firstorgno", dstemp.getString(0, "value") );
		result.put("orgcount", ""+dstemp.rowCount() );
		return result;
	}
	/**
	 * 方法简介：解析excel文件,返回一个DataStore
	 * 
	 * @author fandq
	 * @date 创建时间 2015年9月6日
	 */
	public DataObject analyzeExcelToDs(DataObject para) throws Exception {
		DataStore dsExcel;
		DataStore tableInfo = DataStore.getInstance();
		InputStream is;
		Workbook wb;
		Sheet sheet;

		// 取参数
		CommonsMultipartFile file = (CommonsMultipartFile) para.getObject("file");
		// 参数判断
		if (null == file) {
			this.bizException("analyzeExcelToDs 传入的参数file为空!");
		}
		
		// 构建解析列
		int vi = 0;
		setTableInfo(tableInfo, vi++, "empno","人员编号","String");
		setTableInfo(tableInfo, vi++, "loginname", "登录名", "String");
		setTableInfo(tableInfo, vi++, "empname", "标识姓名", "String");
		setTableInfo(tableInfo, vi++, "rname", "真实姓名", "String");
		setTableInfo(tableInfo, vi++, "idcardno", "身份证号码", "String");
		setTableInfo(tableInfo, vi++, "orgno", "隶属机构", "String");
		setTableInfo(tableInfo, vi++, "gender", "性别", "String");
		setTableInfo(tableInfo, vi++, "officetel", "办公电话", "String");
		setTableInfo(tableInfo, vi++, "mphone", "移动电话", "String");
		setTableInfo(tableInfo, vi++, "email", "邮箱", "String");
		setTableInfo(tableInfo, vi++, "post", "职务", "String");
		setTableInfo(tableInfo, vi++, "uactid", "uactid", "String");
		// 开始解析
		is = ExcelTool.getStreamByFile(file);
		wb = ExcelTool.getExcelFileByStream(is);
		sheet = ExcelTool.getSheet(wb, 0);

		// 上传文件中的数据
		dsExcel = ExcelTool.getDataStoreBySheet(sheet, 0, 1, 70000, tableInfo);
		dsExcel = ExcelTool.removeBlankRowWithTrim(dsExcel);

		ExcelTool.closeWorkbook(wb);
		ExcelTool.closeInputStream(is);
		//去掉左右空格
		for (int i=0 ;i < dsExcel.rowCount(); i++){
			dsExcel.put(i, "loginnameUpper", "");
			if(dsExcel.getString(i, "empno") != null)
				dsExcel.put(i, "empno", dsExcel.getString(i, "empno").trim());
			if(dsExcel.getString(i, "loginname") != null){
				dsExcel.put(i, "loginname", dsExcel.getString(i, "loginname").trim());
				dsExcel.put(i, "loginnameUpper", dsExcel.getString(i, "loginname").trim().toUpperCase());
			}
			if(dsExcel.getString(i, "empname") != null)
				dsExcel.put(i, "empname", dsExcel.getString(i, "empname").trim());
			if(dsExcel.getString(i, "rname") != null)
				dsExcel.put(i, "rname", dsExcel.getString(i, "rname").trim());
			if(dsExcel.getString(i, "idcardno") != null)
				dsExcel.put(i, "idcardno", dsExcel.getString(i, "idcardno").trim());
			if(dsExcel.getString(i, "orgno") != null)
				dsExcel.put(i, "orgno", dsExcel.getString(i, "orgno").trim());
			if(dsExcel.getString(i, "gender") != null)
				dsExcel.put(i, "gender", dsExcel.getString(i, "gender").trim());
			if(dsExcel.getString(i, "officetel") != null)
				dsExcel.put(i, "officetel", dsExcel.getString(i, "officetel").trim());
			if(dsExcel.getString(i, "mphone") != null)
				dsExcel.put(i, "mphone", dsExcel.getString(i, "mphone").trim());
			if(dsExcel.getString(i, "email") != null)
				dsExcel.put(i, "email", dsExcel.getString(i, "email").trim());
			if(dsExcel.getString(i, "post") != null)
				dsExcel.put(i, "post", dsExcel.getString(i, "post").trim());
			if(dsExcel.getString(i, "uactid") != null)
				dsExcel.put(i, "uactid", dsExcel.getString(i, "uactid").trim());
			
		}
		DataObject result = DataObject.getInstance();
		result.put("fileemp", dsExcel);
		return result;
	}
	// 设置Excel每一列的格式
	private void setTableInfo(DataStore tableInfo, int num, String name,
			String columnName, String type) throws Exception {
		tableInfo.addRow();
		tableInfo.put(num, "name", name);
		tableInfo.put(num, "columnName", columnName);
		tableInfo.put(num, "type", type);
	}
	/**
	 * 
	 * @Description:导入岗位  - 解析excel文件,返回一个DataStore
	 * @author 能天宇
	 * @date 2016-10-17
	 */
	public DataObject analyzeExcelToDsRole(DataObject para) throws Exception {
		DataStore dsExcel;
		DataStore empds = DataStore.getInstance();
		DataStore tableInfo = DataStore.getInstance();
		InputStream is;
		Workbook wb;
		Sheet sheet;

		// 取参数
		CommonsMultipartFile file = (CommonsMultipartFile) para.getObject("file");
		// 参数判断
		if (null == file) {
			this.bizException("analyzeExcelToDsRole 传入的参数file为空!");
		}
		
		// 构建解析列
		int vi = 0;
		setTableInfo(tableInfo, vi++, "orgno","机构编号","String");
		setTableInfo(tableInfo, vi++, "orgname", "机构名称", "String");
		setTableInfo(tableInfo, vi++, "roleno", "角色编号", "String");
		setTableInfo(tableInfo, vi++, "rolename", "角色名称", "String");
		setTableInfo(tableInfo, vi++, "orgtypename", "适用的机构类型", "String");
		for(int i = 1;i <= 30; i++){
			setTableInfo(tableInfo, vi++, "loginname"+i, "操作员登录名"+i, "String");
			setTableInfo(tableInfo, vi++, "empname"+i, "操作员姓名"+i, "String");
		}
		// 开始解析
		is = ExcelTool.getStreamByFile(file);
		wb = ExcelTool.getExcelFileByStream(is);
		sheet = ExcelTool.getSheet(wb, 0);

		// 上传文件中的数据
		dsExcel = ExcelTool.getDataStoreBySheet(sheet, 0, 1, 70000, tableInfo);
		dsExcel = ExcelTool.removeBlankRowWithTrim(dsExcel);

		ExcelTool.closeWorkbook(wb);
		ExcelTool.closeInputStream(is);
		if(dsExcel.rowCount() == 0){
			this.bizException("上传文件中没有数据行！");
		}
		
		//去掉左右空格
		for (int i=0 ;i < dsExcel.rowCount(); i++){
			String uuid = StringUtil.getUUID();
			dsExcel.put(i, "uuid", uuid);
			dsExcel.put(i, "clqk", "");
			dsExcel.put(i, "orgno", dsExcel.getString(i, "orgno").trim());
			dsExcel.put(i, "orgname", dsExcel.getString(i, "orgname").trim());
			dsExcel.put(i, "roleno", dsExcel.getString(i, "roleno").trim());
			dsExcel.put(i, "rolename", dsExcel.getString(i, "rolename").trim());
			dsExcel.put(i, "orgtypename", dsExcel.getString(i, "orgtypename").trim());
			for (int j=1;j<=30;j++){
				String loginname = dsExcel.getString(i, "loginname"+j).trim();
				String empname = dsExcel.getString(i, "empname"+j).trim();
				dsExcel.put(i, "loginname"+j, loginname);
				dsExcel.put(i, "empname"+j, empname);	
				if("".equals(loginname) && "".equals(empname))
					continue;
				DataObject temprow = DataObject.getInstance();
				temprow.put("uuid", uuid);
				temprow.put("loginname", loginname);
				temprow.put("empno", "");
				temprow.put("empname",empname);
				empds.addRow(temprow);
				
			}
		}
		DataObject result = DataObject.getInstance();
		result.put("roleds", dsExcel);
		result.put("empds", empds);
		return result;
	}
	/**
	 * 方法简介：批量导入人员 - 校验 
	 * @author 能天宇
	 * @throws Exception 
	 * @date 创建时间 2016-10-10
	 */
	public DataObject checkBatchImportEmp(DataObject para) throws Exception{
		DE de = DE.getInstance();
		StringBuffer logBF = new StringBuffer();
		StringBuffer clqk = new StringBuffer();
		// 获取文件数据Fileds
		DataObject dataemp = para.getDataObject("dataemp");
		if(dataemp == null || !dataemp.containsKey("fileemp")){
			LanePromptUtil.end();
			this.bizException("没有正确读入文件!");
		}
		DataStore fileds = dataemp.getDataStore("fileemp");
		if(fileds == null || fileds.rowCount() == 0){
			LanePromptUtil.end();
			this.bizException("没有人员可以导入。");
		}
		LanePromptUtil.promptToTrace("即将开始校验导入人员的相关信息：共【"+fileds.rowCount()+"】个.");
		logBF.append(">>>即将开始校验导入人员的相关信息：共【"+fileds.rowCount()+"】个.\r\n");

		boolean checkflag = true;
		for (int i = 0; i < fileds.rowCount(); i++) {
			//每次校验尽可能多的发现错误，除非当前错误将会使得后续校验无法继续
			fileds.put(i, "clqk", " ");
			clqk.setLength(0);			
			// 判空
			String empname = fileds.getString(i, "empname");
			LanePromptUtil.promptToTrace("【"+(i+1)+"】:校验人员【"+empname+"】的相关信息.");
			logBF.append("【"+(i+1)+"】:校验人员【"+empname+"】的相关信息.\r\n");
			if (empname == null || "".equals(empname)) {
				clqk.append("标识姓名不能为空 .");
			}
			String empno = fileds.getString(i, "empno");
			if (empno == null || "".equals(empno)) {
				clqk.append("人员编号不能为空 .");
			}
			String rname = fileds.getString(i, "rname");
			if (rname == null || "".equals(rname)) {
				clqk.append("真实姓名不能为空 .");
			}
			String idcardno = fileds.getString(i, "idcardno");
			if (idcardno == null || "".equals(idcardno)) {
				clqk.append("身份证号码不能为空 .");
			}
			String orgno = fileds.getString(i, "orgno");
			if (orgno == null || "".equals(orgno)) {
				clqk.append("隶属机构编号不能为空 .");
			}
			String gender = fileds.getString(i, "gender");
			if (gender == null || "".equals(gender)) {
				clqk.append("性别不能为空 .");
			}
			String loginname = fileds.getString(i, "loginname");
			if (loginname == null || "".equals(loginname)) {
				clqk.append("登录名不能为空 .");
			}			
			//后面的校验都要求以上属性非空才能展开，所以如果前边有空值，只能continue
			if( clqk.length() != 0){
				checkflag = false;
				fileds.put(i, "clqk", ""+clqk.toString());
				logBF.append("处理情况："+clqk.toString()+"\r\n");
				LanePromptUtil.promptToTrace("处理情况："+clqk.toString());
				continue;
			}
			// 判断机构编号是否合法
			if (empno.matches("[0-9A-Za-z_]*") == false) {
				clqk.append("人员编号中只能包含数字、字母、下划线 .");
			}
			if (empno.length() < 1 || empno.length() > 20) {
				clqk.append("人员编号的长度须在" + 1 + "与" + 20 + "之间 .");
			}
			
			// 判断人员登录名是否合法
			if (loginname.matches("[0-9A-Za-z]*") == false) {
				clqk.append("登录名中只能含有数字、字母 .");
			}
//			// 检查身份证号是否合法
//			if (IdcardnoTool.validateCard(idcardno) == false) {
//				clqk.append("身份证号码不合法 .");
//			}
			// 检查隶属机构存在且未注销
			if (OdssuUtil.isOrgExist(orgno) == false) {
				clqk.append("隶属机构【"+orgno+"】不存在 .");
			}else if (OdssuUtil.isOrgOnWork(orgno) == false) {
				clqk.append("隶属机构【"+orgno+"】已经被注销 .");
			}else if ( !OdssuUtil.canHaveEmp(OdssuUtil.getOrgTypeByOrgNo(orgno)) ){
				clqk.append("所选隶属机构为虚拟机构，不能直接在此机构下新增人员.");
			}
			// 性别填写是否正确
			if( !"男".equals(gender) && !"女".equals(gender)){
				clqk.append("性别必须是 男 或者 女.");
			}
			
			// 判断是否已经在数据库中存在
			if (OdssuUtil.isEmpExist(empno)) {
				clqk.append("人员编号【" + empno + "】已存在 .");
			}
			if (OdssuUtil.isEmpNameExist(empno, empname)) {
				clqk.append("标识姓名【" + empname + "】已存在 .");
			}
			if (OdssuUtil.isIdcardnoExist(empno, idcardno)) {
				clqk.append("身份证号【" + idcardno + "】已存在 .");
			}
			String loginnameUpper = loginname.toUpperCase();
			fileds.put(i, "loginnameUpper", loginnameUpper);
			de.clearSql();
  			de.addSql(" select 1                   ");
  			de.addSql("   from odssu.empinfor a    ");
  			de.addSql("  where a.empno <> :empno        ");
  			de.addSql("    and (a.loginname = :loginname or a.loginname = :loginnameupper )    ");
			de.setString("empno", empno);
			de.setString("loginname", loginname);
			de.setString("loginnameupper", loginnameUpper);
			DataStore vdsloginname=de.query();
			if( vdsloginname.rowCount() > 0){
				clqk.append("登录名【" + loginname + "】已存在 .");
			}
			// excel里的某些项是否有重复
	        if(fileds.findAll("empno == " + empno).rowCount() > 1){
	        	clqk.append("人员编号有重复 .");
	        }
//	        if(fileds.findAll("empname == " + empname).rowCount() > 1){
//	        	clqk.append("标识姓名有重复 .");
//	        }
	        String empnametmp = "", empnotmp = "";
//	        for (int j = 0; j < fileds.rowCount(); j++) {
//				empnametmp = fileds.getString(j, "empname");
//				empnotmp = fileds.getString(j, "empno");
//				if (empnametmp.equals(empname)&&!empnotmp.equals(empno)) {
//					clqk.append("标识姓名有重复 .");
//				}
//			}
//	        if(fileds.findAll("idcardno == " + idcardno).rowCount() > 1){
//	        	clqk.append("身份证号有重复 .");
//	        }
	        if(fileds.findAll("loginname == " + loginname).rowCount() > 1){
	        	clqk.append("登录名有重复 .");
	        }
	        if(fileds.findAll("loginnameUpper == " + loginnameUpper ).rowCount() > 1){
	        	clqk.append("登录名有重复 .");
	        }
	        
	        //如果指定了职务，验证职务是否合法
	        String postnames = fileds.getString(i, "post");
	        if(postnames != null && !"".equals(postnames)){
	    		String[] postnameArr = postnames.split(",");
	    		for(int j = 0; j < postnameArr.length ; j++){
	    			String clearpostname = postnameArr[j].trim(); 
	    			if( !clearpostname.isEmpty()){
			        	de.clearSql();
  			 	        de.addSql("select * from odssu.roleinfor where roletype='HSDOMAIN_NBGLJS'  ");
  			 	        de.addSql("   and rolenature ='4' and jsgn='1' and rolename= :clearpostname ");
			 	        de.setString("clearpostname",  clearpostname);
			 	        DataStore roleds = de.query();
			 	        if (roleds == null ||  roleds.rowCount() == 0)
			 	        	clqk.append("职务【"+clearpostname+"】不存在 .");
	    			} 
	    		}
	    	}
	        //uactid验证是否重复
	        String uactid = fileds.getString(i, "uactid");
	        if(uactid != null && !"".equals(uactid)){
	        	de.clearSql();
	  			de.addSql(" select 1                   ");
	  			de.addSql("   from odssu.empinfor a    ");
	  			de.addSql("  where a.uactid = :uactid    ");	
				de.setString("uactid", uactid);				
				DataStore uactidds=de.query();
				if( uactidds.rowCount() > 0){
					clqk.append("uactid【" + uactid + "】已存在 .");
				}
	    	}
	        //处理情况 有内容，说明这一行校验未通过
			if( clqk.length() != 0){
				checkflag = false;
				logBF.append("处理情况："+clqk.toString()+"\r\n");
				fileds.put(i, "clqk", clqk.toString());
				LanePromptUtil.promptToTrace("处理情况："+clqk.toString());
			}
		}

		//校验若不通过，返回excel让用户对照错误信息，修改完善
		if(checkflag == false){
			LanePromptUtil.promptToTrace("校验完成，发现错误信息，请修改并重新校验.");
			logBF.append(">>>校验完成，发现错误信息，请修改并重新校验.\r\n");
			LanePromptUtil.complete();
			DataObject result = DataObject.getInstance();
			result.put("empexcelds", fileds);
			result.put("returnfiletype", "excel");
			return result;
		}else{
			LanePromptUtil.promptToTrace("导入人员的信息校验成功.");
			logBF.append(">>>导入人员的信息校验成功.\r\n");
			LanePromptUtil.complete();
			DataObject result = DataObject.getInstance();
			result.put("empimportlogstr", logBF.toString());
			result.put("returnfiletype", "txt");
			return result;
		}
	}
	
	/**
	 * 方法简介：批量导入人员- 记账
	 * @author 能天宇
	 * @date 创建时间 2016-10-10
	 */
	public DataObject saveBatchImportEmp(DataObject para) throws AppException, BusinessException{
		StringBuffer logBF = new StringBuffer();
		// 获取session里取来的文件数据
		DataObject dataemp = para.getDataObject("dataemp");
		if(dataemp == null || !dataemp.containsKey("fileemp")){
			LanePromptUtil.end();
			this.bizException("没有正确读入文件!");
		}
		DataStore fileds = dataemp.getDataStore("fileemp");
		if(fileds == null || fileds.rowCount() == 0){
			LanePromptUtil.end();
			this.bizException("文件里没有人员数据!");
		}
		logBF.append("开始批量导入人员,共【"+  fileds.rowCount()+"】个：\r\n");
		LanePromptUtil.promptToTrace("开始批量导入人员,共【"+  fileds.rowCount()+"】个：");
		//开始记账
		for (int i = 0; i < fileds.rowCount(); i++) {
			String empno = fileds.getString(i, "empno");
			String loginname = fileds.getString(i, "loginname");
			String empname = fileds.getString(i, "empname");
			String rname = fileds.getString(i, "rname");
			String idcardno = fileds.getString(i, "idcardno");
			String orgno = fileds.getString(i, "orgno");
			String gender = fileds.getString(i, "gender");
			gender = "男".equals(gender)? "1":"2";
			String officetel = fileds.getString(i, "officetel");
			String mphone = fileds.getString(i, "mphone");
			String email = fileds.getString(i, "email");
			String post = fileds.getString(i, "post");
			String orgname = OdssuUtil.getOrgNameByOrgno(orgno);
			String uactid = fileds.getString(i, "uactid");
			DataObject vdo = DataObject.getInstance();
			String userid = this.getUser().getUserid();
			// 凭据编号
			Date today = DateUtil.getCurrentDate();
			String nyr = DateUtil.FormatDate(today, "yyyyMMdd");
			String dbid = GlobalNames.DEBUGMODE ? (String) this.getUser().getValue("dbid") : OdssuNames.DBID;

			vdo.put("pjbh", "F" + nyr);

			vdo.put("empno", empno);
			vdo.put("empname", empname);
			vdo.put("rname", rname);
			vdo.put("idcardno", idcardno);
			vdo.put("hrbelong", orgno);
			vdo.put("loginname", loginname);
			vdo.put("gender", gender);
			vdo.put("officetel", officetel);
			vdo.put("mphone", mphone);
			vdo.put("email", email);
			vdo.put("empcreatedate", today);
			vdo.put("_user", CurrentUser.getInstance());
			vdo.put("pdid", "F" + nyr);
			vdo.put("objectid", "cc");
			vdo.put("userid", userid);
			vdo.put("dbid", dbid);
			vdo.put("uactid", uactid);
			vdo.put("uact", loginname);
			vdo.put("uactusername", empname);
			

			DataStore emp_org = DataStore.getInstance();
			emp_org.put(0, "orgno", orgno);
			vdo.put("emp_org", emp_org);
			
			try {
				empaddASO.doEntry(vdo);
				if(post!=null && !"".equals(post)){
					addPostToEmpNew(post, empno, empname, orgno, orgname);
				}
			} catch (Exception e) {
				e.printStackTrace();
				LanePromptUtil.promptToTrace("【"+(i+1)+"】导入人员【"+empname +"】时出错,记账异常中止 !");
				LanePromptUtil.complete();
				throw new AppException("导入人员【"+empname+"】时出错！"+e.getMessage());
			}
			logBF.append("【"+(i+1)+"】导入了人员【"+empname +"】,人员编号为【"+empno+"】.\r\n");
			LanePromptUtil.promptToTrace("【"+(i+1)+"】导入了人员【"+empname +"】,人员编号为【"+empno+"】.");
		}
		logBF.append("批量导入人员- 记账完成！\r\n");
		LanePromptUtil.promptToTrace("批量导入人员 - 记账完成！");
		LanePromptUtil.complete();
		DataObject result = DataObject.getInstance();
		result.put("empimportlogstr", logBF.toString());
		result.put("returnfiletype", "txt");
		return result;
	}
	/**
	 * 
	 * @Description:子方法 -为人员添加职务 
	 * @author 能天宇
	 * @date 2016-10-10 下午3:12:54
	 */
	private void addPostToEmpNew(String postnames, String empno, String empname, String orgno, String orgname) throws AppException{
		DE de = DE.getInstance();
		String[] postnameArr = postnames.split(",");
		for(int i = 0; i < postnameArr.length; i++){
			if(postnameArr[i].trim().isEmpty()){
				continue;
			}
			de.clearSql();
   	        de.addSql("select * from odssu.roleinfor where roletype='HSDOMAIN_NBGLJS'  ");
   	        de.addSql("   and rolenature = :rolenature and jsgn= :jsgn and rolename= :rolename ");
 	        de.setString("rolenature",OdssuContants.ROLENATURE_CYJS);
 	        de.setString("jsgn",OdssuContants.JSGN_POST);
 	        de.setString("rolename", postnameArr[i].trim());
 	        DataStore roleds = de.query();
 	        if (roleds == null ||  roleds.rowCount() == 0) {
 	        	throw new AppException("职务【"+postnameArr[i].trim()+"】不存在 .");
 	        }
			String postno = roleds.getString(0, "roleno");
			String postname = postnameArr[i].trim();
			
			de.clearSql();
  			de.addSql(" insert into odssu.ir_emp_org_all_role(empno,orgno,roleno,rolenature,jsgn) ");
  			de.addSql("                                values(:empno,   :orgno,   :postno,  :para4,  :para5) ");
			de.setString("empno", empno);
			de.setString("orgno", orgno);
			de.setString("postno", postno);
			de.setString("para4", OdssuContants.ROLENATURE_CYJS);
			de.setString("para5", OdssuContants.JSGN_POST);
			int	result = de.update();
			if(result!=1){
				throw new AppException("为人员【"+empname+"】增加在【"+orgname+"】的【"+postname+"】职务时出错！");
			}
		}
	}
	/**
	 * @Description:批量导入人员 - 校验不通过时组装要返回的excel 
	 * @author 能天宇
	 * @date 2016-10-10
	 */
	public DataObject returnFileForBatchImportEmp(DataObject para) throws IOException, AppException {
		DataObject vdo = DataObject.getInstance();// 存最终组成的excel并返回
		DataStore excelDS = para.getDataStore("empexcelds");
		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 生成一个表格
		HSSFSheet sheet = workbook.createSheet("人员信息");
		sheet.setColumnWidth(11, 15000);
		// 样式
		//sheet.setDefaultColumnWidth(15);
		// 生成一个样式
		HSSFCellStyle style = workbook.createCellStyle();
		// 设置这些样式
		style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);
		// 生成一个字体
		HSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.BLACK.index);
		font.setFontHeightInPoints((short) 12);
		font.setBold(true);
		// 生成一个字体
		HSSFFont fontred = workbook.createFont();
		fontred.setColor(HSSFColor.RED.index);
		fontred.setFontHeightInPoints((short) 12);
		fontred.setBold(true);
		HSSFCellStyle styleRed = workbook.createCellStyle();
		styleRed.setFont(fontred);
		// 把字体应用到当前的样式
		style.setFont(font);
		
		// 准备表格数据
		DataStore firstrow = DataStore.getInstance();
		firstrow.put(0, "name", "处理情况");
		firstrow.put(1, "name", "人员编号");
		firstrow.put(2, "name", "登录名");
		firstrow.put(3, "name", "标识姓名");
		firstrow.put(4, "name", "真实姓名");
		firstrow.put(5, "name", "身份证号码");
		firstrow.put(6, "name", "隶属机构");
		firstrow.put(7, "name", "性别");
		firstrow.put(8, "name", "办公电话");
		firstrow.put(9, "name", "移动电话");
		firstrow.put(10, "name", "邮箱");
		firstrow.put(11, "name", "职务");
		
		// 产生表格标题行
		HSSFRow row = sheet.createRow(0);
		
		for (int i = 0; i < firstrow.rowCount(); i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(firstrow.getString(i, "name"));
			cell.setCellValue(text);
		}
		
		for(int j=0; j <excelDS.rowCount();j++){
			// 产生数据事例行
			HSSFRow rowtemp = sheet.createRow(j+1);
			
			HSSFCell cell = rowtemp.createCell(0);
			cell.setCellStyle(styleRed);
			HSSFRichTextString text = new HSSFRichTextString(excelDS.getString(j, "clqk"));
			cell.setCellValue(text);
			HSSFCell cell1 = rowtemp.createCell(1);
			HSSFRichTextString text1 = new HSSFRichTextString(excelDS.getString(j, "empno"));
			cell1.setCellValue(text1);
			HSSFCell cell2 = rowtemp.createCell(2);
			HSSFRichTextString text2 = new HSSFRichTextString(excelDS.getString(j, "loginname"));
			cell2.setCellValue(text2);
			HSSFCell cell3 = rowtemp.createCell(3);
			HSSFRichTextString text3 = new HSSFRichTextString(excelDS.getString(j, "empname"));
			cell3.setCellValue(text3);
			HSSFCell cell4 = rowtemp.createCell(4);
			HSSFRichTextString text4 = new HSSFRichTextString(excelDS.getString(j, "rname"));
			cell4.setCellValue(text4);
			HSSFCell cell5 = rowtemp.createCell(5);
			HSSFRichTextString text5 = new HSSFRichTextString(excelDS.getString(j, "idcardno"));
			cell5.setCellValue(text5);
			HSSFCell cell6 = rowtemp.createCell(6);
			HSSFRichTextString text6 = new HSSFRichTextString(excelDS.getString(j, "orgno"));
			cell6.setCellValue(text6);
			HSSFCell cell7 = rowtemp.createCell(7);
			HSSFRichTextString text7 = new HSSFRichTextString(excelDS.getString(j, "gender"));
			cell7.setCellValue(text7);
			HSSFCell cell8 = rowtemp.createCell(8);
			HSSFRichTextString text8 = new HSSFRichTextString(excelDS.getString(j, "officetel"));
			cell8.setCellValue(text8);
			HSSFCell cell9 = rowtemp.createCell(9);
			HSSFRichTextString text9 = new HSSFRichTextString(excelDS.getString(j, "mphone"));
			cell9.setCellValue(text9);
			HSSFCell cell10 = rowtemp.createCell(10);
			HSSFRichTextString text10 = new HSSFRichTextString(excelDS.getString(j, "email"));
			cell10.setCellValue(text10);
			HSSFCell cell11 = rowtemp.createCell(11);
			HSSFRichTextString text11 = new HSSFRichTextString(excelDS.getString(j, "post"));
			cell11.setCellValue(text11);
		}
		vdo.put("wb", workbook);
		return vdo;
		
	}

	/**
	 * 方法简介：批量导入机构 - 校验数据
	 * @author 能天宇
	 * @date 创建时间 2016-9-26
	 */
	public DataObject checkBatchImportOrg(DataObject para) throws AppException, BusinessException{
		DE de = DE.getInstance();
		StringBuffer logBF = new StringBuffer();
		StringBuffer clqk = new StringBuffer();

		String orgtype = para.getString("orgtype");
		if(orgtype == null || "".equals(orgtype)){
			LanePromptUtil.end();
			this.bizException("导入机构的机构类型为空。");
		}else{
			//校验机构类型是否允许自建标志
			de.clearSql();
  			de.addSql("select 1	");
  			de.addSql("  from odssu.org_type a         ");
  			de.addSql(" where a.typeno = :orgtype    ");
  			de.addSql("   and a.yxzjjgbz = '1'  ");
			de.setString("orgtype", orgtype);
			DataStore orgtypeds = de.query();
			 if (orgtypeds.rowCount() == 0) {
				 LanePromptUtil.end();
				 this.bizException("机构类型不正确,不允许自建!");
			}
		}
		// 获取文件数据fileds
		DataObject dataorg = para.getDataObject("dataorg");
		if(dataorg == null || !dataorg.containsKey("fileorg")){
			LanePromptUtil.end();
			this.bizException("没有正确读入文件!");
		}
		DataStore fileds = dataorg.getDataStore("fileorg");
		if(fileds == null || fileds.rowCount() == 0){
			LanePromptUtil.end();
			this.bizException("没有机构可以导入。");
		}
		LanePromptUtil.promptToTrace("即将开始校验导入机构的相关信息：共【"+fileds.rowCount()+"】个.");
		logBF.append(">>>即将开始校验导入机构的相关信息：共【"+fileds.rowCount()+"】个.\r\n");

		boolean checkflag = true;
		for (int i = 0; i < fileds.rowCount(); i++) {
			//每次校验尽可能多的发现错误，除非当前错误将会使得后续校验无法继续
			fileds.put(i, "clqk", " ");
			fileds.put(i, "belongorgname", "");
			clqk.setLength(0);			
			// 机构标识名称
			String orgname = fileds.getString(i, "orgname");
			LanePromptUtil.promptToTrace("【"+(i+1)+"】:校验机构【"+orgname+"】的相关信息.");
			logBF.append("【"+(i+1)+"】:校验机构【"+orgname+"】的相关信息.\r\n");
			if (orgname == null  || "".equals(orgname)) {
				clqk.append("机构标识名称为空 .");
			}
			// 机构编号
			String orgno = fileds.getString(i, "orgno");
			if (orgno == null  || "".equals(orgno)) {
				clqk.append("机构编号为空 .");
			}

			// 机构简称
			String displayname = fileds.getString(i, "displayname");
			if (displayname == null  || "".equals(displayname)) {
				clqk.append("机构简称为空 .");
			}
			
			// 机构全称
			String fullname = fileds.getString(i, "fullname");
			if (fullname == null  || "".equals(fullname)) {
				clqk.append("机构全称为空 .");
			}
			
			// 隶属机构编号
			String belongorgno = fileds.getString(i, "belongorgno");
			if (belongorgno == null  || "".equals(belongorgno)) {
				clqk.append("所属机构为空 .");
			}
			
			//后面的校验都要求以上属性非空才能展开，所以如果前边有空值，只能continue
			if( clqk.length() != 0){
				checkflag = false;
				fileds.put(i, "clqk", ""+clqk.toString());
				logBF.append("处理情况："+clqk.toString()+"\r\n");
				LanePromptUtil.promptToTrace("处理情况："+clqk.toString());
				continue;
			}
			
			//校验隶属机构信息
			de.clearSql();
  			de.addSql(" select orgtype,orgname belongorgname from odssu.orginfor where orgno = :belongorgno ");
			de.addSql(" and sleepflag = '0' ");
			de.setString("belongorgno", belongorgno);
			DataStore belorgvds = de.query();
			if (belorgvds == null || belorgvds.rowCount() == 0)  {
				clqk.append("隶属机构【"+belongorgno+"】不存在 .");
			} else {
				fileds.put(i, "belongorgname", belorgvds.getString(0, "belongorgname"));
				String beltype = belorgvds.getString(0, "orgtype");
				de.clearSql();
  				de.addSql(" select 1 from odssu.ir_org_type where suptypeno = :beltype and subtypeno = :orgtype ");
				de.setString("beltype", beltype);
				de.setString("orgtype", orgtype);
				DataStore irorgvds = de.query();
				if (irorgvds == null || irorgvds.rowCount() == 0) {
					clqk.append("新增机构的类型与其上级机构的机构类型不匹配 .");
				}
			}

			// 判断机构编号是否已存在
			if (OdssuUtil.isOrgExist(orgno)) {
				checkflag = false;
				clqk.append("机构编号【"+orgno+"】已被占用 .");
				fileds.put(i, "clqk", ""+clqk.toString());
				logBF.append("处理情况："+clqk.toString()+"\r\n");
				LanePromptUtil.promptToTrace("处理情况："+clqk.toString());
				continue;
			}
			// 判断机构编号编码规则是否符合要求
			de.clearSql();
  			de.addSql(" select typeno,minlength,maxlength,allowletter,allowunderline,qzyfjgbhdt ");
  			de.addSql(" from odssu.org_type where typeno = :orgtype ");
			de.setString("orgtype", orgtype);
			DataStore orgtypevds = de.query();
			if (orgtypevds == null || orgtypevds.rowCount() == 0) {
				LanePromptUtil.end();		//前边已经用过一次，这次如果没能查出org_type ，应该爆红
				throw  new AppException("没有找到机构类型编号为【" + orgtype + "】的类型信息!");
			}
			int minlength = orgtypevds.getInt(0, "minlength");
			int maxlength = orgtypevds.getInt(0, "maxlength");
			String allowletter = orgtypevds.getString(0, "allowletter");
			String allowunderline = orgtypevds.getString(0, "allowunderline");
			String qzyfjgbhdt = orgtypevds.getString(0, "qzyfjgbhdt");

			// 判断是否只含有数字、字母、下划线
			if (orgno.matches("[0-9A-Za-z_]*") == false) {
				clqk.append("机构编号中不能含有除数字、字母、下划线之外的其他字符 .");
			}
			// 判断机构编号的长度合不合法
			if (orgno.length() < minlength || orgno.length() > maxlength) {
				clqk.append("机构编号的长度不合法，长度应该在" + minlength + "与" + maxlength+ "之间 .");
			}
			// 判断机构编号字母是否合法
			if ("0".equals(allowletter)) {
				Pattern p = Pattern.compile("[a-zA-Z]+");
				Matcher m = p.matcher(orgno);
				if (m.matches() == true) {
					clqk.append("该机构类型决定的机构编号中不能含有字母 .");
				}
			}
			// 判断机构编号下划线是否合法
			if ("0".equals(allowunderline)) {
				if (orgno.indexOf("_") >= 0) {
					clqk.append("该机构类型决定的机构编号中不能含有下划线 .");
				}
			}
			// 判断机构编号是否被强制以父机构打头
			if ("1".equals(qzyfjgbhdt)) {
				if (orgno.indexOf(belongorgno) < 0) {
					clqk.append("该机构类型决定的机构编号必须以父机构编号打头 .");
				}
			}
			//机构标识姓名是否已存在
			if (OdssuUtil.isOrgNameExist(orgno, orgname)){
				clqk.append("标识姓名【" + orgname + "】已存在 .");
			}
			//excel里的orgno和orgname是否有重复
	        if(fileds.findAll("orgno == " + orgno).rowCount() > 1){
	        	clqk.append("机构编号有重复 .");
	        }
	        if(fileds.findAll("orgname == " + orgname).rowCount() > 1){
	        	clqk.append("机构标识名称有重复 .");
	        }
			
			if( clqk.length() != 0){
				checkflag = false;
				logBF.append("处理情况："+clqk.toString()+"\r\n");
				fileds.put(i, "clqk", clqk.toString());
				LanePromptUtil.promptToTrace("处理情况："+clqk.toString());
			}
		}
		//校验若不通过，返回excel让用户对照错误信息，修改完善
		if(checkflag == false){
			LanePromptUtil.promptToTrace("校验完成，发现错误信息，请修改文件数据.");
			logBF.append(">>>校验完成，发现错误信息，请修改文件数据.\r\n");
			LanePromptUtil.complete();
			DataObject result = DataObject.getInstance();
			result.put("excelds", fileds);
			result.put("returnfiletype", "excel");
			return result;
		}else{
			LanePromptUtil.promptToTrace("导入机构的信息校验成功.");
			logBF.append(">>>导入机构的信息校验成功.\r\n");
			LanePromptUtil.complete();
			DataObject result = DataObject.getInstance();
			result.put("orgimportlogstr", logBF.toString());
			result.put("returnfiletype", "txt");
			return result;
		}
	}
	/**
	 * 方法简介：批量导入机构 - 记账
	 * @author 能天宇
	 * @date 创建时间 2016-9-26
	 */
	public DataObject saveBatchImportOrg(DataObject para) throws AppException, BusinessException{
		StringBuffer logBF = new StringBuffer();

		String orgtype = para.getString("orgtype");
		if(orgtype == null || "".equals(orgtype)){
			LanePromptUtil.end();
			this.bizException("导入机构的机构类型为空!");
		}
		// 获取session里取来的文件数据
		DataObject dataorg = para.getDataObject("dataorg");
		if(dataorg == null || !dataorg.containsKey("fileorg")){
			LanePromptUtil.end();
			this.bizException("没有正确读入文件!");
		}
		DataStore fileds = dataorg.getDataStore("fileorg");
		if(fileds == null || fileds.rowCount() == 0){
			LanePromptUtil.end();
			this.bizException("机构总数为0!");
		}
		logBF.append("开始批量导入机构,共【"+  fileds.rowCount()+"】个：\r\n");
		LanePromptUtil.promptToTrace("开始批量导入机构,共【"+  fileds.rowCount()+"】个：");
		//开始记账
		for (int i = 0; i < fileds.rowCount(); i++) {
			String orgno = fileds.getString(i, "orgno");
			String orgname = fileds.getString(i, "orgname");
			String displayname = fileds.getString(i, "displayname");
			String fullname = fileds.getString(i, "fullname");
			String belongorgno = fileds.getString(i, "belongorgno");
			String xzqhdm = fileds.getString(i, "xzqhdm");
			DataObject vdo = DataObject.getInstance();
			String userid = this.getUser().getUserid();
			// 凭据编号
			Date today = DateUtil.getCurrentDate();
			String nyr = DateUtil.FormatDate(today, "yyyyMMdd");
			vdo.put("pjbh", "F" + nyr);
			vdo.put("orgno", orgno);
			vdo.put("orgname", orgname);
			vdo.put("displayname", displayname);
			vdo.put("fullname", fullname);
			vdo.put("belongorgno", belongorgno);
			vdo.put("xzqhdm", xzqhdm);
			vdo.put("orgtype", orgtype);
			vdo.put("_user", CurrentUser.getInstance());
			vdo.put("pdid", "F" + nyr);
			vdo.put("objectid", "cc");
			vdo.put("userid", userid);
			vdo.put("detailtype", "9");
			String jgtz = "." + orgno;
			if (belongorgno!=null && !belongorgno.equals("")) {
				DE de = DE.getInstance();
				de.clearSql();
  				de.addSql("select a.jgtz from odssu.orginfor a where a.orgno = :belongorgno ");
  				de.addSql(" and a.sleepflag = '0' ");
				de.setString("belongorgno",belongorgno);
				DataStore vds = de.query();
				jgtz = vds.getString(0, "jgtz") + "." + orgno;
			}
			vdo.put("jgtz", jgtz);
			//调用新增机构ASO
			try {
				orgaddASO.doEntry(vdo);		
			} catch (ASOException e) {
				e.printStackTrace();
				LanePromptUtil.promptToTrace("【"+(i+1)+"】导入机构【"+orgname +"】时出错,记账异常中止 !");
				LanePromptUtil.complete();
				throw new AppException("批量导入机构【"+orgname+"】执行ASO记账时出错！"+e.getMessage());
			}
			logBF.append("【"+(i+1)+"】导入了机构【"+orgname +"】,机构编号为【"+orgno+"】.\r\n");
			LanePromptUtil.promptToTrace("【"+(i+1)+"】导入了机构【"+orgname +"】,机构编号为【"+orgno+"】.");
		}
		logBF.append("批量导入机构 - 记账完成！\r\n");
		LanePromptUtil.promptToTrace("批量导入机构 - 记账完成！");
		LanePromptUtil.complete();
		DataObject result = DataObject.getInstance();
		result.put("orgimportlogstr", logBF.toString());
		result.put("returnfiletype", "txt");
		return result;
	}
	/**
	 * @Description:批量导入机构 - 校验不通过时组装要返回的excel 
	 * @author 能天宇
	 * @date 2016-9-28 上午9:27:09
	 */
	public DataObject returnFileForBatchImportOrg(DataObject para) throws IOException, AppException {
		DataObject vdo = DataObject.getInstance();// 存最终组成的excel并返回
		DataStore excelDS = para.getDataStore("excelds");
		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 生成一个表格
		HSSFSheet sheet = workbook.createSheet("机构信息");
		sheet.setColumnWidth(6, 12000);
		// 样式
		//sheet.setDefaultColumnWidth(15);
		// 生成一个样式
		HSSFCellStyle style = workbook.createCellStyle();
		// 设置这些样式
		style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);
		// 生成一个字体
		HSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.BLACK.index);
		font.setFontHeightInPoints((short) 12);
		font.setBold(true);
		// 生成一个字体
		HSSFFont fontred = workbook.createFont();
		fontred.setColor(HSSFColor.RED.index);
		fontred.setFontHeightInPoints((short) 12);
		fontred.setBold(true);
		HSSFCellStyle styleRed = workbook.createCellStyle();
		styleRed.setFont(fontred);
		// 把字体应用到当前的样式
		style.setFont(font);
		
		// 准备表格数据
		DataStore firstrow = DataStore.getInstance();
		firstrow.put(0, "name", "处理情况");
		firstrow.put(1, "name", "机构编号");
		firstrow.put(2, "name", "标识名称");
		firstrow.put(3, "name", "简称");
		firstrow.put(4, "name", "全称");
		firstrow.put(5, "name", "所属机构");
		firstrow.put(6, "name", "所属机构名称");

		// 产生表格标题行
		HSSFRow row = sheet.createRow(0);
		
		for (int i = 0; i < firstrow.rowCount(); i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(firstrow.getString(i, "name"));
			cell.setCellValue(text);
		}
		
		for(int j=0; j <excelDS.rowCount();j++){
			// 产生数据事例行
			HSSFRow rowtemp = sheet.createRow(j+1);
			HSSFCell cell = rowtemp.createCell(0);
			cell.setCellStyle(styleRed);
			HSSFRichTextString text = new HSSFRichTextString(excelDS.getString(j, "clqk"));
			cell.setCellValue(text);
			HSSFCell cell1 = rowtemp.createCell(1);
			HSSFRichTextString text1 = new HSSFRichTextString(excelDS.getString(j, "orgno"));
			cell1.setCellValue(text1);
			HSSFCell cell2 = rowtemp.createCell(2);
			HSSFRichTextString text2 = new HSSFRichTextString(excelDS.getString(j, "orgname"));
			cell2.setCellValue(text2);
			HSSFCell cell3 = rowtemp.createCell(3);
			HSSFRichTextString text3 = new HSSFRichTextString(excelDS.getString(j, "displayname"));
			cell3.setCellValue(text3);
			HSSFCell cell4 = rowtemp.createCell(4);
			HSSFRichTextString text4 = new HSSFRichTextString(excelDS.getString(j, "fullname"));
			cell4.setCellValue(text4);
			HSSFCell cell5 = rowtemp.createCell(5);
			HSSFRichTextString text5 = new HSSFRichTextString(excelDS.getString(j, "belongorgno"));
			cell5.setCellValue(text5);
			HSSFCell cell6 = rowtemp.createCell(6);
			HSSFRichTextString text6 = new HSSFRichTextString(excelDS.getString(j, "belongorgname"));
			cell6.setCellValue(text6);
		}
		vdo.put("wb", workbook);
		return vdo;
	}
	/**
	 * 方法简介：解析excel文件,并上传到数据库
	 * 
	 * @author fandq
	 * @date 创建时间 2015年9月6日
	 * @since 2016-10-9  加入去空格
	 */
	public DataObject analyzeExcelToDsOrg(DataObject para) throws Exception {
		DataStore dsExcel;
		DataStore tableInfo = DataStore.getInstance();
		InputStream is;
		Workbook wb;
		Sheet sheet;

		// 取参数
		CommonsMultipartFile file = (CommonsMultipartFile) para.getObject("file");
		// 参数判断
		if (null == file) {
			this.bizException("analyzeExcelToDs 传入的参数file为空!");
		}

		// 构建解析列String orgno, orgname, displayname, fullname,
		// belongorgno,orgtype;
		int vi = 0;
		setTableInfo(tableInfo, vi++, "orgno", "机构编号", "String");
		setTableInfo(tableInfo, vi++, "orgname", "标识名称", "String");
		setTableInfo(tableInfo, vi++, "displayname", "简称", "String");
		setTableInfo(tableInfo, vi++, "fullname", "全称", "String");
		setTableInfo(tableInfo, vi++, "belongorgno", "所属机构", "String");
		setTableInfo(tableInfo, vi++, "xzqhdm", "行政区划", "String");

		// 开始解析
		is = ExcelTool.getStreamByFile(file);
		wb = ExcelTool.getExcelFileByStream(is);
		sheet = ExcelTool.getSheet(wb, 0);

		// 上传文件中的数据
		dsExcel = ExcelTool.getDataStoreBySheet(sheet, 0, 1, 70000, tableInfo);
		dsExcel = ExcelTool.removeBlankRowWithTrim(dsExcel);

		ExcelTool.closeWorkbook(wb);
		ExcelTool.closeInputStream(is);
		//去掉左右空格
		for (int i=0 ;i < dsExcel.rowCount(); i++){
			if(dsExcel.getString(i, "orgno") != null)
				dsExcel.put(i, "orgno", dsExcel.getString(i, "orgno").trim());
			if(dsExcel.getString(i, "orgname") != null)
				dsExcel.put(i, "orgname", dsExcel.getString(i, "orgname").trim());
			if(dsExcel.getString(i, "displayname") != null)
				dsExcel.put(i, "displayname", dsExcel.getString(i, "displayname").trim());
			if(dsExcel.getString(i, "fullname") != null)
				dsExcel.put(i, "fullname", dsExcel.getString(i, "fullname").trim());
			if(dsExcel.getString(i, "belongorgno") != null)
				dsExcel.put(i, "belongorgno", dsExcel.getString(i, "belongorgno").trim());
			if(dsExcel.getString(i, "xzqhdm") != null)
				dsExcel.put(i, "xzqhdm", dsExcel.getString(i, "xzqhdm").trim());
		}
		DataObject result = DataObject.getInstance();
		result.put("fileorg", dsExcel);
		return result;
	}

	/**
	 * 方法简介：创建人员批量增加文件模版
	 * 
	 * @author fandq
	 * @throws IOException
	 * @throws AppException
	 * @date 创建时间 2015年9月9日
	 */
	public DataObject createExcelEmp(DataObject para) throws IOException, AppException {

		DataObject vdo = DataObject.getInstance();// 存最终组成的excel并返回

		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 生成一个表格
		HSSFSheet sheet = workbook.createSheet("人员信息");
		// 样式
		sheet.setDefaultColumnWidth(15);
		// 生成一个样式
		HSSFCellStyle style = workbook.createCellStyle();
		// 设置这些样式
		style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);
		// 生成一个字体
		HSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.BLACK.index);
		font.setFontHeightInPoints((short) 12);
		font.setBold(true);
		// 把字体应用到当前的样式
		style.setFont(font);

		// 准备表格数据
		DataStore firstrow = DataStore.getInstance();
		firstrow.put(0, "name", "人员编号");
		firstrow.put(1, "name", "登录名");
		firstrow.put(2, "name", "标识姓名");
		firstrow.put(3, "name", "真实姓名");
		firstrow.put(4, "name", "身份证号码");
		firstrow.put(5, "name", "隶属机构");
		firstrow.put(6, "name", "性别");
		firstrow.put(7, "name", "办公电话");
		firstrow.put(8, "name", "移动电话");
		firstrow.put(9, "name", "邮箱");
		firstrow.put(10, "name", "职务");
		firstrow.put(11, "name", "uactid");

		DataStore nextrow = DataStore.getInstance();
		nextrow.put(0, "name", "原核三用户ID");
		nextrow.put(1, "name", "例：wangwu001");
		nextrow.put(2, "name", "例:王五");
		nextrow.put(3, "name", "例:王五");
		nextrow.put(4, "name", "例:51142219570705332X");
		nextrow.put(5, "name", "例:371001");
		nextrow.put(6, "name", "例:男/女");
		nextrow.put(7, "name", "例:88988898");
		nextrow.put(8, "name", "例:13111311131");
		nextrow.put(9, "name", "例:xxx@xxx.xxx");
		nextrow.put(10, "name", "例:科长,副科长");
		nextrow.put(11, "name", "uactid,可为空");
		// 产生表格标题行
		HSSFRow row = sheet.createRow(0);
		for (int i = 0; i < nextrow.rowCount(); i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(firstrow.getString(i, "name"));
			cell.setCellValue(text);
		}
		// 产生数据事例行
		HSSFRow row1 = sheet.createRow(1);
		for (int i = 0; i < nextrow.rowCount(); i++) {
			HSSFCell cell = row1.createCell(i);
			HSSFRichTextString text = new HSSFRichTextString(nextrow.getString(i, "name"));
			cell.setCellValue(text);
		}
		vdo.put("wb", workbook);
		return vdo;
	}
	/**
	 * 
	 * @Description:批量导入机构 -- 生成导入模板文件 
	 * @author 能天宇
	 * @date 2016-10-19
	 */
	public DataObject createExcelOrg(DataObject para) throws IOException, AppException {
		DataObject vdo = DataObject.getInstance();// 存最终组成的excel并返回

		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 生成一个表格
		HSSFSheet sheet = workbook.createSheet("机构信息");
		// 样式
		sheet.setDefaultColumnWidth(15);
		// 生成一个样式
		HSSFCellStyle style = workbook.createCellStyle();
		// 设置这些样式
		style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);
		// 生成一个字体
		HSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.BLACK.index);
		font.setFontHeightInPoints((short) 12);
		font.setBold(true);
		// 把字体应用到当前的样式
		style.setFont(font);

		// 准备表格数据
		DataStore firstrow = DataStore.getInstance();
		firstrow.put(0, "name", "机构编号");
		firstrow.put(1, "name", "标识名称");
		firstrow.put(2, "name", "简称");
		firstrow.put(3, "name", "全称");
		firstrow.put(4, "name", "所属机构");
		firstrow.put(5, "name", "行政区划");

		DataStore nextrow = DataStore.getInstance();
		nextrow.put(0, "name", "例:37100111");
		nextrow.put(1, "name", "例:**科");
		nextrow.put(2, "name", "例:**科");
		nextrow.put(3, "name", "例:**科");
		nextrow.put(4, "name", "例:371001");
		nextrow.put(5, "name", "例:371099");

		// 产生表格标题行
		HSSFRow row = sheet.createRow(0);

		for (int i = 0; i < nextrow.rowCount(); i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(firstrow.getString(i, "name"));
			cell.setCellValue(text);
		}

		// 产生数据事例行
		HSSFRow row1 = sheet.createRow(1);
		for (int i = 0; i < nextrow.rowCount(); i++) {
			HSSFCell cell = row1.createCell(i);
			HSSFRichTextString text = new HSSFRichTextString(nextrow.getString(i, "name"));
			cell.setCellValue(text);
		}
		vdo.put("wb", workbook);
		return vdo;

	}
	/**
	 * 
	 * @Description: 批量导入岗位模板文件 - 生成各人社局的Excel模板（之后再从controller里打包成zip）
	 * @author 能天宇
	 * @date 2016-10-12 上午10:46:45
	 */
	public DataObject createExcelsRole(DataObject para) throws Exception {
		String rsxtorgno = para.getString("orgno");
		String gwlbstr = para.getString("gwlbstr");
		String separator = File.separator;
		// 得到临时文件夹目录 工程+file+download+时间戳
		try {
			File fileFile = new File(FileUtil.getRootPath() + separator + "file"+ separator);
			if (!fileFile.exists()) {
				fileFile.mkdir();
			}
			File fileDownload = new File(FileUtil.getRootPath() + separator
					+ "file" + separator + "download" + separator);
			if (!fileDownload.exists()) {
				fileDownload.mkdir();
			}
		} catch (Exception e) {
			throw new AppException("文件操作失败！"+e.getMessage());
		}
		Date date = new Date();
		String path = FileUtil.getRootPath() + separator + "file" + separator
				+ "download" + separator + (date.getTime()) + separator;
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		SPrompt prompt = SPromptUtil.getSPrompt();
		DataStore rsjVds = getRsjInfoDs(rsxtorgno);
		prompt.setTotalSteps(rsjVds.rowCount());
		
		//为每个区县人社局生成一个自己的模板文件
		for (int i = 0; i < rsjVds.rowCount(); i++) {
			DataObject rsjInfo = rsjVds.getRow(i);
			String orgno   = rsjInfo.getString("orgno");
			String orgname = rsjInfo.getString("orgname");
			String qxmc = orgname;
			prompt.prompt("【"+(i+1)+"】开始生成 【"+qxmc+"】的岗位隶属科室和岗位人员信息统计模板."); // 向PROMPT区输出提示信息

			//获取角色信息数据
			DataObject dataobj = getDutyAndFaceVds(rsxtorgno,orgno);
			
			//组装Excel
			dataobj.put("gwlbstr", gwlbstr);
			dataobj.put("rsxtorgno", rsxtorgno);
			DataObject result = createSingleExcelDuty(dataobj);
			
			// 获取HSSFWorkbook的编码串
			HSSFWorkbook wb = (HSSFWorkbook) result.get("wb");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			wb.write(out);
			WritableWorkbook workbook = ExcelTool.createWorkbook(out);
			ExcelTool.writeWbootAndClose(workbook);
			ExcelTool.closeOutputStream(out);
			String fileName = qxmc + "_岗位隶属科室及人员信息上报" + ".xls";
			FileOutputStream fos = new FileOutputStream(path+"/"+fileName);
			fos.write(out.toByteArray());
			fos.close();

			prompt.prompt(fileName+" 已完成."); // 向PROMPT区输出提示信息
			prompt.moveForword(1);
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("path", path);
		return vdo;
	}
	/**
	 * 
	 * @Description: 批量导入岗位 - 获取人社系统下的所有人社局
	 * @author 能天宇
	 * @date 2016-10-12 下午1:49:55
	 */
	private DataStore getRsjInfoDs(String belongorgno) throws AppException {
		if(!OdssuUtil.isRsxt(belongorgno)){
			throw new AppException("所给的机构编号不是人社系统!");
		}
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql(" select  orgno,displayname orgname, orgtype ");
  		de.addSql("   from  odssu.orginfor  ");
  		de.addSql("  where  belongorgno = :belongorgno ");
//  		de.addSql("    and  orgtype in ('HSDOMAIN_DSRSJ','HSDOMAIN_QXRSJ')");
		de.addSql(" and sleepflag = '0' ");
  		de.addSql("  order  by orgtype asc, orgno asc ");
		de.setString("belongorgno", belongorgno);
		DataStore vdsorg = de.query();
		if (vdsorg == null || vdsorg.rowCount() == 0) {
			throw new AppException("无法获取人社系统下的人社局信息!");
		}
		return vdsorg;
	}
	/**
	 * 
	 * @Description: 批量导入岗位  - 获取某人社局Excel模板中的主要数据（岗位编号，岗位名称，面向机构）
	 * @author 能天宇
	 * @date 2016-10-12 下午1:39:49
	 */
	private DataObject getDutyAndFaceVds(String rsxtorgno,String rsjorgno) throws Exception {
		DE de = DE.getInstance();
		// 1.查询所有全地市角色
		de.clearSql();
  		de.addSql(" select rolename,roleno ");
  		de.addSql("   from odssu.roleinfor ");
  		de.addSql("  where deforgno = :rsxtorgno ");
  		de.addSql("    and sleepflag = '0' ");
  		de.addSql("    and rolename not like '%测试%' ");
		de.addSql("  order by rolename ");
		de.setString("rsxtorgno", rsxtorgno);
		DataStore rolevds = de.query();
		if(rolevds == null || rolevds.rowCount() == 0 ){
			throw new AppException("查询不到该人社系统下的角色信息！");
		}
		// 2.每个全地市角色 查出在本人社局下适用的机构类型
		DataStore resultds = DataStore.getInstance();	//存储最后的返回结果
		int rowno = 0;
		for (int i = 0; i < rolevds.rowCount();i++) {
			String rolename = rolevds.getString(i, "rolename");
			String roleno = rolevds.getString(i, "roleno");

			de.clearSql();
			de.addSql(" select a.orgtypeno,b.typename from odssu.role_orgtype a,");
			de.addSql("                      odssu.org_type b");
			de.addSql("  where a.roleno = :roleno ");
			de.addSql("    and b.typeno = a.orgtypeno");
			de.setString("roleno",roleno);
			DataStore orgtypeds = de.query();

			String orgtypename = "";
			for (int k = 0; k < orgtypeds.rowCount(); k++) {
				if(k==0) {
					orgtypename = orgtypename + orgtypeds.getString(k, "typename");
				}else{
					orgtypename = orgtypename + "," + orgtypeds.getString(k, "typename");
				}
			}
			resultds.put(rowno, "roleno", roleno);
			resultds.put(rowno, "rolename", rolename);
			resultds.put(rowno, "orgtypename",orgtypename);
			rowno++;
		}
		resultds.multiSort("rolename:asc,orgtypename:asc");
		DataObject vdo = DataObject.getInstance();
		vdo.put("roleds", resultds);
		return vdo;
	}
	/**
	 * 方法简介.判断两个ds是否有交集(getIntersection=getIts)
	 * 
	 * @author fandq
	 * @date 创建时间 2015年8月11日
	 */
	private boolean getIts(DataStore ds1, DataStore ds2) {
		boolean flag = false;
		DataStore dsnew = DataStore.getInstance(ds1);
		DataStore dsold = DataStore.getInstance(ds2);
		dsnew.retainAll(dsold);
		if (!dsnew.isEmpty()) {
			flag = true;
		}
		return flag;
	}
	/**
	 * 
	 * @Description: 批量导入岗位 -生成单个的Excel模板文件
	 * @author 能天宇
	 * @date 2016-10-12 下午3:06:10
	 */
	private DataObject createSingleExcelDuty(DataObject para) throws IOException, AppException {

		DataStore excelDS = para.getDataStore("roleds");
		String rsxtorgno = para.getString("rsxtorgno");
		String gwlbstr = para.getString("gwlbstr");
		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 1.生成表格 -角色及人员信息
		HSSFSheet sheet = workbook.createSheet("角色及人员信息");
		sheet.setColumnWidth(3, 6500);
		sheet.setColumnWidth(1, 5000);
		// 样式
		sheet.setDefaultColumnWidth(15);
		// 生成一个样式
		HSSFCellStyle style = workbook.createCellStyle();
		// 设置这些样式
		style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);
		// 生成一个字体
		HSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.BLACK.index);
		font.setFontHeightInPoints((short) 12);
		font.setBold(true);
		// 生成一个字体
		HSSFFont fontred = workbook.createFont();
		fontred.setColor(HSSFColor.RED.index);
		fontred.setFontHeightInPoints((short) 12);
		fontred.setBold(true);
		HSSFCellStyle styleRed = workbook.createCellStyle();
		styleRed.setFont(fontred);
		// 把字体应用到当前的样式
		style.setFont(font);
		
		// 准备表格表头
		DataStore firstrow = DataStore.getInstance();
		int columnNoOfFirstRow = 0 ;
		firstrow.put(columnNoOfFirstRow++, "name", "机构编号");
		firstrow.put(columnNoOfFirstRow++, "name", "机构名称");
		firstrow.put(columnNoOfFirstRow++, "name", "角色编号");
		firstrow.put(columnNoOfFirstRow++, "name", "角色名称");
		firstrow.put(columnNoOfFirstRow++, "name", "适用的机构类型");
		int empCount = 30;
		for(int i=1; i<= empCount; i++){
			firstrow.put(columnNoOfFirstRow++, "name", "操作员登录名"+i);
			firstrow.put(columnNoOfFirstRow++, "name", "操作员姓名"+i);
		}
		// 产生表格标题行
		HSSFRow row = sheet.createRow(0);
		
		for (int i = 0; i < firstrow.rowCount(); i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(firstrow.getString(i, "name"));
			cell.setCellValue(text);
		}
		
		// 产生表格数据行
		for(int j=0; j < excelDS.rowCount();j++){
			HSSFRow rowtemp = sheet.createRow(j+1);
			HSSFCell cell = rowtemp.createCell(0);
			HSSFRichTextString textEmpty = new HSSFRichTextString("");
			cell.setCellValue(textEmpty);
			HSSFCell cell1 = rowtemp.createCell(1);
			cell1.setCellValue(textEmpty);
			HSSFCell cell2 = rowtemp.createCell(2);
			HSSFRichTextString text2 = new HSSFRichTextString(excelDS.getString(j, "roleno"));
			cell2.setCellValue(text2);
			HSSFCell cell3 = rowtemp.createCell(3);
			HSSFRichTextString text3 = new HSSFRichTextString(excelDS.getString(j, "rolename"));
			cell3.setCellValue(text3);
			HSSFCell cell4 = rowtemp.createCell(4);
			HSSFRichTextString text4 = new HSSFRichTextString(excelDS.getString(j, "orgtypename"));
			cell4.setCellValue(text4);
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("wb", workbook);
		return vdo;
	}
	/**
	 * 
	 * @Description: 生成科室-人员信息excel 
	 * @author 能天宇
	 * @date 2016-10-12 上午10:46:45
	 */
	public DataObject createExcelsDepartAndEmp(DataObject para) throws Exception {
		String rsxtorgno = para.getString("orgno");
		String separator = File.separator;
		// 得到临时文件夹目录 工程+file+download+时间戳
		try {
			File fileFile = new File(FileUtil.getRootPath() + separator + "file"+ separator);
			if (!fileFile.exists()) {
				fileFile.mkdir();
			}
			File fileDownload = new File(FileUtil.getRootPath() + separator
					+ "file" + separator + "download" + separator);
			if (!fileDownload.exists()) {
				fileDownload.mkdir();
			}
		} catch (Exception e) {
			throw new AppException("文件操作失败！"+e.getMessage());
		}
		Date date = new Date();
		String path = FileUtil.getRootPath() + separator + "file" + separator
				+ "download" + separator + (date.getTime()) + separator;
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		DataStore rsjVds = getRsjInfoDs(rsxtorgno);
		//为每个区县人社局生成一个自己的excel文件
		for (int i = 0; i < rsjVds.rowCount(); i++) {
			DataObject rsjInfo = rsjVds.getRow(i);
			String orgno   = rsjInfo.getString("orgno");
			String orgname = rsjInfo.getString("orgname");
			String orgtype = rsjInfo.getString("orgtype");
			String qxmc = orgname.replace("人社局", "");
			if("HSDOMAIN_DSRSJ".equals(orgtype)){
				qxmc = "市直";
			}
			//获取岗位信息数据
			DataObject dataobj = getDepartAndEmpVds(orgno);
			
			//组装Excel
			dataobj.put("rsxtorgno", rsxtorgno);
			DataObject result = createSingleExcelDepartAndEmp(dataobj);
			
			// 获取HSSFWorkbook的编码串
			HSSFWorkbook wb = (HSSFWorkbook) result.get("wb");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			wb.write(out);
			WritableWorkbook workbook = ExcelTool.createWorkbook(out);
			ExcelTool.writeWbootAndClose(workbook);
			ExcelTool.closeOutputStream(out);
			String fileName = qxmc + "_科室及人员信息" + ".xls";
			FileOutputStream fos = new FileOutputStream(path+"/"+fileName);
			fos.write(out.toByteArray());
			fos.close();
			
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("path", path);
		return vdo;
	}
	/**
	 * 
	 * @Description: 导出科室-人员信息- 获取Excel中的科室-人员数据
	 * @author 能天宇
	 * @date 2016-10-14
	 */
	private DataObject getDepartAndEmpVds(String rsjorgno) throws Exception {
		// 1.查询所有科室，二级单位 及其上级机构信息

		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select a.orgno belongorgno, a.orgname belongorgname,t.orgno, t.orgname ");
  		de.addSql("  from odssu.orginfor t, odssu.orginfor a,odssu.ir_org_closure closure ");
  		de.addSql(" where t.belongorgno = a.orgno and a.orgno = closure.orgno and closure.belongorgno= :rsjorgno");
  		de.addSql("   and t.sleepflag = '0' ");
  		de.addSql("   and a.sleepflag = '0' ");
  		de.addSql("   and t.orgtype in ('HSDOMAIN_RSCKS', 'HS_DS_EJDW', 'HS_QX_EJDW') ");
  		de.addSql("   and t.orgname not like '%测试%' ");
  		de.addSql(" order by t.belongorgno, t.orgno ");
  		de.setString("rsjorgno", rsjorgno);
		DataStore departds = de.query();

		// 2.查询这些科室下的人员信息
		de.clearSql();
  		de.addSql("select c.orgno belongorgno, c.orgname belongorgname, b.orgno , b.orgname , a.loginname , a.empname ");
  		de.addSql("  from odssu.empinfor a , odssu.orginfor b ,   odssu.orginfor c,odssu.ir_org_closure closure ");
  		de.addSql(" where a.hrbelong = b.orgno    and b.belongorgno = c.orgno  ");
  		de.addSql("   and c.orgno = closure.orgno and closure.belongorgno= :rsjorgno");
  		de.addSql("   and a.sleepflag = '0'   and b.sleepflag = '0'   and c.sleepflag = '0' ");
  		de.addSql("   and b.orgtype in ('HSDOMAIN_RSCKS', 'HS_DS_EJDW', 'HS_QX_EJDW') ");
  		de.addSql("   and b.orgname not like '%测试%' ");
  		de.addSql(" order by c.orgno, b.orgno ");
  		de.setString("rsjorgno", rsjorgno);
		DataStore empds = de.query();

		DataObject vdo = DataObject.getInstance();
		vdo.put("departds", departds);
		vdo.put("empds", empds);
		return vdo;
	}
	/**
	 * 
	 * @Description: 导出科室-人员信息     生成单个的Excel文件
	 * @author 能天宇
	 * @date 2016-10-12 下午3:06:10
	 */
	private DataObject createSingleExcelDepartAndEmp(DataObject para) throws IOException, AppException {

		DataStore departds = para.getDataStore("departds");
		DataStore empds = para.getDataStore("empds");
		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 1.生成表格 人社局下属科室、二级单位信息
		HSSFSheet sheet = workbook.createSheet("本人社局下属科室、二级单位信息");
		sheet.setColumnWidth(1, 6500);
		sheet.setColumnWidth(3, 6500);
		// 样式
		sheet.setDefaultColumnWidth(15);
		// 生成一个样式
		HSSFCellStyle style = workbook.createCellStyle();
		// 设置这些样式
		style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);
		// 生成一个字体
		HSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.BLACK.index);
		font.setFontHeightInPoints((short) 12);
		font.setBold(true);
		// 生成一个字体
		HSSFFont fontred = workbook.createFont();
		fontred.setColor(HSSFColor.RED.index);
		fontred.setFontHeightInPoints((short) 12);
		fontred.setBold(true);
		HSSFCellStyle styleRed = workbook.createCellStyle();
		styleRed.setFont(fontred);
		// 把字体应用到当前的样式
		style.setFont(font);
		
		// 准备表格表头
		DataStore firstrow = DataStore.getInstance();
		int columnNoOfFirstRow = 0 ;
		firstrow.put(columnNoOfFirstRow++ , "name", "上级机构编号");
		firstrow.put(columnNoOfFirstRow++, "name", "上级机构名称");
		firstrow.put(columnNoOfFirstRow++, "name", "科室编号");
		firstrow.put(columnNoOfFirstRow++, "name", "科室名称");
		// 产生表格标题行
		HSSFRow row = sheet.createRow(0);
		
		for (int i = 0; i < firstrow.rowCount(); i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(firstrow.getString(i, "name"));
			cell.setCellValue(text);
		}
		
		// 产生表格数据行
		for(int j=0; j < departds.rowCount();j++){
			HSSFRow rowtemp = sheet.createRow(j+1);
			HSSFCell cell = rowtemp.createCell(0);
			HSSFRichTextString text = new HSSFRichTextString(departds.getString(j, "belongorgno"));
			cell.setCellValue(text);
			HSSFCell cell1 = rowtemp.createCell(1);
			HSSFRichTextString text1 = new HSSFRichTextString(departds.getString(j, "belongorgname"));
			cell1.setCellValue(text1);
			HSSFCell cell2 = rowtemp.createCell(2);
			HSSFRichTextString text2 = new HSSFRichTextString(departds.getString(j, "orgno"));
			cell2.setCellValue(text2);
			HSSFCell cell3 = rowtemp.createCell(3);
			HSSFRichTextString text3 = new HSSFRichTextString(departds.getString(j, "orgname"));
			cell3.setCellValue(text3);
		}
				
		// 2.生成一张表 “本人社局下属科室的人员信息”
		HSSFSheet sheetemp = workbook.createSheet("本人社局下属科室的人员信息");
		// 样式
		sheetemp.setColumnWidth(1, 6500);
		sheetemp.setColumnWidth(3, 6500);
		sheetemp.setColumnWidth(4, 5300);
		sheetemp.setDefaultColumnWidth(15);
		// 准备表格表头
		firstrow = DataStore.getInstance();
		columnNoOfFirstRow = 0 ;
		firstrow.put(columnNoOfFirstRow++ , "name", "上级机构编号");
		firstrow.put(columnNoOfFirstRow++, "name", "上级机构名称");
		firstrow.put(columnNoOfFirstRow++, "name", "科室编号");
		firstrow.put(columnNoOfFirstRow++, "name", "科室名称");
		firstrow.put(columnNoOfFirstRow++, "name", "用户名");
		firstrow.put(columnNoOfFirstRow++, "name", "人员姓名");
		// 产生表格标题行
		row = sheetemp.createRow(0);
		
		for (int i = 0; i < firstrow.rowCount(); i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(firstrow.getString(i, "name"));
			cell.setCellValue(text);
		}
		
		// 产生表格数据行
		for(int j=0; j < empds.rowCount();j++){
			HSSFRow rowtemp = sheetemp.createRow(j+1);
			HSSFCell cell = rowtemp.createCell(0);
			HSSFRichTextString text = new HSSFRichTextString(empds.getString(j, "belongorgno"));
			cell.setCellValue(text);
			HSSFCell cell1 = rowtemp.createCell(1);
			HSSFRichTextString text1 = new HSSFRichTextString(empds.getString(j, "belongorgname"));
			cell1.setCellValue(text1);
			HSSFCell cell2 = rowtemp.createCell(2);
			HSSFRichTextString text2 = new HSSFRichTextString(empds.getString(j, "orgno"));
			cell2.setCellValue(text2);
			HSSFCell cell3 = rowtemp.createCell(3);
			HSSFRichTextString text3 = new HSSFRichTextString(empds.getString(j, "orgname"));
			cell3.setCellValue(text3);
			HSSFCell cell4 = rowtemp.createCell(4);
			HSSFRichTextString text4 = new HSSFRichTextString(empds.getString(j, "loginname"));
			cell4.setCellValue(text4);
			HSSFCell cell5 = rowtemp.createCell(5);
			HSSFRichTextString text5 = new HSSFRichTextString(empds.getString(j, "empname"));
			cell5.setCellValue(text5);
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("wb", workbook);
		return vdo;
	}
	/**
	 * 方法简介：批量导入角色 - 校验
	 * @author 能天宇
	 * @date 创建时间 2016-10-17
	 */
	public DataObject checkBatchImportRole(DataObject para) throws Exception{
		StringBuffer logBF = new StringBuffer();
		DataObject dataobj = para.getDataObject("dataobj");
		if(dataobj == null || !dataobj.containsKey("roleds")|| !dataobj.containsKey("empds")){
			LanePromptUtil.end();
			this.bizException("没有正确读入文件!");
		}
		DataStore roleds = dataobj.getDataStore("roleds");
		DataStore empds = dataobj.getDataStore("empds");
		if(roleds == null || roleds.rowCount() == 0){
			LanePromptUtil.end();
			throw new AppException("没有角色可以导入。");
		}
		if(empds == null){
			LanePromptUtil.end();
			throw new AppException("获取人员信息失败。");
		}
		LanePromptUtil.promptToTrace("即将开始校验导入角色的相关信息：共【"+roleds.rowCount()+"】个.");
		logBF.append(">>>即将开始校验导入角色的相关信息：共【"+roleds.rowCount()+"】个.\r\n");
		//每次循环校验excel中的一行数据
		for (int i = 0; i < roleds.rowCount(); i++) {
			DataObject row = roleds.getRow(i);
			String orgno = row.getString("orgno");
			String orgname = row.getString("orgname");
			String roleno = row.getString("roleno");
			String rolename = row.getString("rolename");
			String orgtypename = row.getString("orgtypename");
			LanePromptUtil.promptToTrace("【"+(i+1)+"】:正在校验角色【"+rolename+"】.");
			logBF.append("【"+(i+1)+"】:正在校验角色【"+rolename+"】.\r\n");
			
			//检查是否有空值
			if(isRowHaveEmptyColumn(row, logBF) == true){
				continue;
			}
			//检查机构，角色，人员的正确性，以及角色是否适用于上传的文件中填写的机构
			if(isRowHaveErrorColumn(empds,row,logBF) == true){
				continue;
			}

			//检查人员是否已经在对应机构下拥有了该角色
			if(isEmpInRole(empds,row,logBF) == true){
				continue;
			}


			DataStore tempds = roleds.cloneFulfill(" roleno == " + roleno+ " and rolename == "
        			+rolename + " and orgname == "+orgname);
			int beginrowno = 0;
			//检查重复行
	        if( tempds.rowCount() > 1){
	        	StringBuffer rownoList =new StringBuffer();
	        	beginrowno  = roleds.find(" roleno == " + roleno+ " and rolename == " +rolename +  " and orgname == "+orgname,beginrowno);
        		rownoList.append(""+(beginrowno+2));
	        	while(beginrowno > -1){
	        		beginrowno  = roleds.find(" roleno == " + roleno+ " and rolename == " +rolename + " and orgname == "+orgname,beginrowno+1);
	        		if(beginrowno > -1)
	        			rownoList.append("、"+(beginrowno+2));
	        	}
	        	logBF.append("处理情况：【"+rownoList+"】行角色重复设置.\r\n");
	        	row.put("clqk", "【"+rownoList+"】行角色重复设置.");
				LanePromptUtil.promptToTrace("处理情况：【"+rownoList+"】行角色重复设置.");
	        }
		}
		boolean checkflag = true;
		for (int i=0;i< roleds.rowCount();i++){
			if(!"".equals(roleds.getString(i, "clqk"))){
				checkflag = false;
				break;
			}
		}
		//校验若不通过，返回excel让用户对照错误信息，修改完善
		if(checkflag == false){
			LanePromptUtil.promptToTrace("校验完成，发现错误信息，请根据excel修改并重新校验.");
			LanePromptUtil.promptToTrace("请稍候，正在生成Excel中.");
			logBF.append(">>>校验完成，发现错误信息，请修改并重新校验.\r\n");
			LanePromptUtil.complete();
			DataObject result = DataObject.getInstance();
			result.put("dutyexcelds", roleds);
			result.put("returnfiletype", "excel");
			return result;
		}else{
			LanePromptUtil.promptToTrace("批量导入角色的信息校验成功.");
			logBF.append(">>>批量导入角色的信息校验成功.\r\n");
			LanePromptUtil.complete();
			DataObject result = DataObject.getInstance();
			result.put("dutyimportlogstr", logBF.toString());
			result.put("returnfiletype", "txt");
			return result;
		}
	}
	/**
	 * @方法详述  检查角色在机构下是否已经存在该操作员
	 * @author fandq
	 * @创建时间 2017年2月20日
	 */
	private boolean isEmpInRole(DataStore empds,DataObject row ,StringBuffer logBF) throws AppException{
		String roleno = row.getString("roleno");
		String rolename = row.getString("rolename");
		String orgno = row.getString("orgno");
		String uuid = row.getString("uuid");
		DE de = DE.getInstance();
		
		//检查人员是否已经在对应机构下拥有了该角色
		DataStore uuempds = empds.cloneFulfill(" uuid == "+ uuid);
		StringBuffer clqk = new StringBuffer();
		String errorgname = "";
		for(int i=0;i<uuempds.rowCount();i++){
			String empno = uuempds.getString(i, "empno");
			String empname = uuempds.getString(i, "empname");
			de.clearSql();
  			de.addSql("select 1 ");
  			de.addSql("  from odssu.ir_emp_org_all_role a ");
  			de.addSql(" where a.empno = :empno ");
  			de.addSql("   and a.orgno = :orgno ");
  			de.addSql("   and a.roleno = :roleno ");
			de.setString("empno", empno);
			de.setString("orgno", orgno);
			de.setString("roleno", roleno);
			DataStore vds = de.query();

			if (vds.rowCount() != 0) {
				clqk.append("【"+empname+"】");
				errorgname = empname;
			}
		}
		if (clqk.toString().equals("")) {
			return false;
		}else {
			row.put("clqk", "机构"+errorgname+"下的人员"+clqk.toString()+"已经拥有了角色"+rolename+"请将这些操作员去掉\r\n");
			return true;
		}
	}
	/**
	 * @方法详述    岗位隶属机构是否变更 
	 * @author fandq
	 * @创建时间 2017年2月20日
	 */
	private boolean isInOrgChange(DataObject row ,StringBuffer logBF) throws AppException{
		String inorgno = row.getString("inorgno");
		String roleno = row.getString("roleno");
		String faceorgname = row.getString("faceorgname");
		DE de = DE.getInstance();
		de.clearSql();  		
		de.addSql("select orgno from odssu.orginfor where orgname = :faceorgname ");
		de.addSql(" and sleepflag = '0' ");
		de.setString("faceorgname", faceorgname);
		DataStore vds = de.query();
		
		if (vds == null||vds.rowCount() == 0) {
			return false;
		}
		String faceorgno = vds.getString(0, "orgno");
		de.clearSql();
  		de.addSql(" select inorgno ");
  		de.addSql("   from odssu.outer_duty ");
  		de.addSql("  where roleno = :roleno and faceorgno = :faceorgno ");
		de.setString("roleno", roleno);
		de.setString("faceorgno", faceorgno);
		DataStore dutynovds = de.query();
		
		if (dutynovds == null || dutynovds.rowCount() == 0) {
			return false;
		}
		
		String inorgnoTmp = dutynovds.getString(0, "inorgno");
		
		if (!inorgnoTmp.equals(inorgno)) {
			row.put("clqk", "岗位已经设置隶属机构，隶属机构不允许变更。\r\n");
			return true;
		}
		
		return false;
	}
	/**
	 * 
	 * @Description: 检查ds每行是否有不允许的空值
	 * @author 能天宇
	 * @date 2016-10-17
	 */
	private boolean isRowHaveEmptyColumn(DataObject row ,StringBuffer logBF) throws AppException{
		StringBuffer clqk = new StringBuffer();
		String orgno = row.getString("orgno");
		String orgname = row.getString("orgname");
		String roleno = row.getString("roleno");
		String rolename = row.getString("rolename");
		String orgtypename = row.getString("orgtypename");

		if(orgno == null || "".equals(orgno))
			clqk.append("机构编号为空.");
		if(orgname == null || "".equals(orgname))
			clqk.append("机构名称为空.");
		if(roleno == null || "".equals(roleno))
			clqk.append("角色编号为空.");
		if(rolename == null || "".equals(rolename))
			clqk.append("角色名称为空.");
		if(orgtypename == null  || "".equals(orgtypename))
			clqk.append("适用的机构类型为空.");
		
		for(int j=1;j<=10;j++){
			String loginname = row.getString("loginname"+j);
			String empname = row.getString("empname"+j);
			boolean noflag = true;
			boolean nameflag = true;
			if(loginname == null || "".equals(loginname)){
				noflag = false;
			}
			if(empname == null || "".equals(empname)){
				nameflag = false;
			}
			if( !noflag && nameflag ){
				clqk.append("操作员登录名"+j+"为空.");
			}else if( noflag && !nameflag){
				clqk.append("操作员姓名"+j+"为空.");
			}
		}

		if(clqk.length() != 0  ){
			row.put("clqk", clqk.toString());
			logBF.append("处理情况:"+clqk+"\r\n");
			LanePromptUtil.promptToTrace("处理情况:"+clqk);
			return true;
		}
		return false;
	}
	/**
	 * 
	 * @Description: 简单检查填写各项目的存在性
	 * @author 能天宇
	 * @date 2016-10-18
	 */
	private boolean isRowHaveErrorColumn(DataStore empds,DataObject row,StringBuffer logBF) throws AppException{
		StringBuffer clqk = new StringBuffer();
		DE de = DE.getInstance();
		String orgno = row.getString("orgno");
		String orgname = row.getString("orgname");
		String orgtypenames = row.getString("orgtypename");
		String roleno = row.getString("roleno");
		String rolename = row.getString("rolename");
		String uuid = row.getString("uuid");
		DataStore uuempds = empds.cloneFulfill(" uuid == "+ uuid);
		
		//检查用户上报填写的机构编号和机构名称是否有误
		if( !OdssuUtil.isOrgExist(orgno)){
			clqk.append("机构【"+orgno+"】不存在.");
		}else if(!OdssuUtil.isOrgOnWork(orgno)){
			clqk.append("机构【"+orgno+"】已被注销.");
		}else {
  			de.clearSql();
  			de.addSql("select orgno  from odssu.orginfor where orgname = :orgname ");
			de.addSql(" and sleepflag = '0' ");
			de.setString("orgname", orgname);
			DataStore vds = de.query();
			if (vds == null || vds.rowCount() == 0) {
				clqk.append("机构名称与机构编号不一致.");
			}else if(vds.rowCount() > 1){
				int index = vds.find("orgno == " +orgno);
				if(index < 0 )
					clqk.append("机构名称与机构编号不一致.");
			}else if(!orgno.equals(vds.getString(0, "orgno")) ){
				clqk.append("机构名称与机构编号不一致.");
			}
		}
		//检查角色填写是否正确
		if( !OdssuUtil.isRoleExist(roleno)){
			clqk.append("角色【"+roleno+"】不存在.");
		}else if(!OdssuUtil.isRoleOnWork(roleno)){
			clqk.append("角色【"+roleno+"】已被注销.");
		}else{
			de.clearSql();
  			de.addSql("select roleno from odssu.roleinfor where rolename = :rolename ");
			de.setString("rolename",rolename);
			DataStore dstemp = de.query();
			if (dstemp == null || dstemp.rowCount() == 0) {
				clqk.append("角色名称与角色编号不一致.");
			}else if(dstemp.rowCount() > 1){
				int index = dstemp.find("roleno == " +roleno);
				if(index < 0 )
					clqk.append("角色名称与角色编号不一致.");
			}else if( !roleno.equals(dstemp.getString(0, "roleno"))){
				clqk.append("角色名称与角色编号不一致.");
			}
		}

		//检查用户上报的人员填写是否有误
		for(int j=0;j<uuempds.rowCount();j++){
			String loginname = uuempds.getString(j, "loginname");		
			String empname = uuempds.getString(j, "empname");
			String empno = "";
			de.clearSql();
  			de.addSql(" select empno ");
  			de.addSql("   from odssu.empinfor ");
  			de.addSql("  where loginname = :loginname ");
			de.setString("loginname", loginname.toUpperCase());
			DataStore empVds = de.query();
			if(empVds == null || empVds.size() == 0){
				clqk.append("操作员【"+loginname+"】不存在.");
			}else{
				empno = empVds.getString(0, "empno");
				if(!OdssuUtil.isEmpOnWork(empno)){
					clqk.append("操作员【"+empname+"】已离职.");
				}else if(!empname.equals(OdssuUtil.getEmpNameByEmpno(empno))){
					clqk.append("操作员【"+empname+"】编号与姓名不一致.");
				}
			}
			int count = uuempds.findAll("loginname == " + loginname +" and empname == " + empname).rowCount();
			if(count > 1){
				clqk.append("操作员【"+empname+"】重复出现.");
			}
		}
		//检查角色是否适用于对应的机构
		de.clearSql();
		de.addSql(" select orgtype from odssu.orginfor ");
		de.addSql("  where orgno = :orgno");
		de.setString("orgno",orgno);
		DataStore orgtypeds = de.query();
		String orgtype = orgtypeds.getString(0,"orgtype");

		String [] orgtypename = orgtypenames.split(",");
		for (int i = 0;i<orgtypename.length;i++){
			de.clearSql();
			de.addSql(" select 1 from odssu.role_orgtype ");
			de.addSql("  where roleno = :roleno ");
			de.addSql("    and orgtypeno = :orgtypeno ");
			de.setString("roleno",roleno);
			de.setString("orgtypeno",orgtype);
			DataStore orgtype_result = de.query();
			if (orgtype_result.rowCount()==0){
				clqk.append("角色【"+rolename+"】不适用于机构【"+orgno+"】");
			}
		}
		if(clqk.length() != 0 ){
			row.put("clqk", clqk.toString());
			logBF.append("处理情况:"+clqk+"\r\n");
			LanePromptUtil.promptToTrace("处理情况:"+clqk);
			return true;
		}
		for(int j=0;j<uuempds.rowCount();j++){
			String loginname = uuempds.getString(j, "loginname");		
			String empname = uuempds.getString(j, "empname");
			String empno = OdssuUtil.getEmpNoByLoginName(loginname);
			int index = empds.find("loginname == " + loginname +" and empname == " + empname+" and uuid == " + uuid);
			empds.put(index, "empno", empno);
		}
		return false;
	}
	/**
	 * 
	 * @Description: 判断岗位是否符合岗位导入的要求
	 * @author 能天宇
	 * @date 2016-10-17
	 */
	private boolean isRoleValide(DataObject row ,StringBuffer logBF) throws AppException{
		StringBuffer clqk = new StringBuffer();
		DE de = DE.getInstance();
		String roleno = row.getString("roleno");
		String rolename = row.getString("rolename");
		
		// 岗位是不是全地市外岗，柜员岗
		de.clearSql();
  		de.addSql("select  1 ");
  		de.addSql("   from odssu.roleinfor a, ");
  		de.addSql("        ( select * from odssu.orginfor b where b.sleepflag = '0' and b.orgtype in('HSDOMAIN_DSRSXT','HSDOMAIN_SRSXT')) org ");
  		de.addSql("  where a.deforgno = org.orgno ");
  		de.addSql("    and a.roletype in ('HS_JBJYWJBL','HS_QDSYWGLL','HS_GYL') ");
  		de.addSql("    and a.sleepflag = '0'  ");
  		de.addSql("    and a.roleno= :roleno ");
  		de.addSql("  order by a.roletype,a.rolename ");
		de.setString("roleno",roleno);
		DataStore overallds = de.query();
		if (overallds == null || overallds.rowCount() == 0) {
			clqk.append("岗位【"+rolename+"】不是全地市外岗、柜员岗.");
			row.put("clqk", clqk.toString());
			logBF.append("处理情况:"+clqk+"\r\n");
			LanePromptUtil.promptToTrace("处理情况:"+clqk);
			return false;
		}
		return true;
	}
	/**
	 * 
	 * @Description: 批量导入岗位 - 返回的Excel文件（批量导入角色 sef2）
	 * @author 能天宇
	 * @date 2016-10-12 下午3:06:10
	 */
	public DataObject returnFileForBatchImportRole(DataObject para) throws IOException, AppException {
		DataStore excelDS = para.getDataStore("dutyexcelds");
		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 1.生成表格 -角色隶属科室及人员信息
		HSSFSheet sheet = workbook.createSheet("角色隶属科室及人员信息");
		sheet.setColumnWidth(4, 6500);
		sheet.setColumnWidth(2, 5000);
		// 样式
		sheet.setDefaultColumnWidth(15);
		// 生成一个样式
		HSSFCellStyle style = workbook.createCellStyle();
		// 设置这些样式
		style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);
		// 生成一个字体
		HSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.BLACK.index);
		font.setFontHeightInPoints((short) 12);
		font.setBold(true);
		// 生成一个字体
		HSSFFont fontred = workbook.createFont();
		fontred.setColor(HSSFColor.RED.index);
		fontred.setFontHeightInPoints((short) 12);
		fontred.setBold(true);
		HSSFCellStyle styleRed = workbook.createCellStyle();
		styleRed.setFont(fontred);
		// 把字体应用到当前的样式
		style.setFont(font);
		
		// 准备表格表头
		DataStore firstrow = DataStore.getInstance();
		int columnNoOfFirstRow = 0 ;
		firstrow.put(columnNoOfFirstRow++ , "name", "处理情况");
		firstrow.put(columnNoOfFirstRow++ , "name", "机构编号");
		firstrow.put(columnNoOfFirstRow++, "name", "机构名称");
		firstrow.put(columnNoOfFirstRow++, "name", "角色编号");
		firstrow.put(columnNoOfFirstRow++, "name", "角色名称");
		firstrow.put(columnNoOfFirstRow++, "name", "角色适用的机构类型");
		int dutyCount = 30;
		for(int i=1; i<= dutyCount; i++){
			firstrow.put(columnNoOfFirstRow++, "name", "操作员登录名"+i);
			firstrow.put(columnNoOfFirstRow++, "name", "操作员姓名"+i);
		}
		// 产生表格标题行
		HSSFRow row = sheet.createRow(0);
		
		for (int i = 0; i < firstrow.rowCount(); i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(firstrow.getString(i, "name"));
			cell.setCellValue(text);
		}
		
		// 产生表格数据行
		for(int j=0; j < excelDS.rowCount();j++){
			HSSFRow rowtduty = sheet.createRow(j+1);
			
			HSSFCell cell = rowtduty.createCell(0);
			cell.setCellStyle(styleRed);
			HSSFRichTextString text = new HSSFRichTextString(excelDS.getString(j, "clqk"));
			cell.setCellValue(text);
			HSSFCell cell1 = rowtduty.createCell(1);
			HSSFRichTextString text1 = new HSSFRichTextString(excelDS.getString(j, "orgno"));
			cell1.setCellValue(text1);
			HSSFCell cell2 = rowtduty.createCell(2);
			HSSFRichTextString text2 = new HSSFRichTextString(excelDS.getString(j, "orgname"));
			cell2.setCellValue(text2);
			HSSFCell cell3 = rowtduty.createCell(3);
			HSSFRichTextString text3 = new HSSFRichTextString(excelDS.getString(j, "roleno"));
			cell3.setCellValue(text3);
			HSSFCell cell4 = rowtduty.createCell(4);
			HSSFRichTextString text4 = new HSSFRichTextString(excelDS.getString(j, "rolename"));
			cell4.setCellValue(text4);
			HSSFCell cell5 = rowtduty.createCell(5);
			HSSFRichTextString text5 = new HSSFRichTextString(excelDS.getString(j, "orgtypename"));
			cell5.setCellValue(text5);
			for (int i = 1; i <= 30; i++) {
				HSSFCell cellempno = rowtduty.createCell(4+2*i);
				HSSFRichTextString textempno = new HSSFRichTextString(excelDS.getString(j, "loginname"+i));
				cellempno.setCellValue(textempno);
				HSSFCell cellempname = rowtduty.createCell(5+2*i);
				HSSFRichTextString textempname = new HSSFRichTextString(excelDS.getString(j, "empname"+i));
				cellempname.setCellValue(textempname);
			}
		}

		DataObject vdo = DataObject.getInstance();
		vdo.put("wb", workbook);
		return vdo;
	}
	/**
	 * 
	 * @Description: 批量导入岗位 - 记账（批量导入角色 sef2）
	 * @author 能天宇
	 * @date 2016-10-18
	 */
	public DataObject saveBatchImportRole(DataObject para) throws AppException, BusinessException{
		StringBuffer logBF = new StringBuffer();
		DE de = DE.getInstance();
		DataStore loginfods = DataStore.getInstance();
		int logi = 0;
		// 获取session里取来的文件数据
		DataObject dataobj = para.getDataObject("dataobj");
		if(dataobj == null || !dataobj.containsKey("roleds") || !dataobj.containsKey("empds")){
			LanePromptUtil.end();
			throw new AppException("无法获取文件数据!");
		}
		DataStore roleds = dataobj.getDataStore("roleds");
		logBF.append("开始批量导入角色,共【"+  roleds.rowCount()+"】个：\r\n");
		LanePromptUtil.promptToTrace("开始批量导入角色,共【"+  roleds.rowCount()+"】个：");
		//组装记账的入参
		for (int i = 0; i < roleds.rowCount(); i++) {
			DataStore empds = DataStore.getInstance();
			DataStore role_emporgds = DataStore.getInstance();
			DataStore role_empds = DataStore.getInstance();
			DataStore role_orgds = DataStore.getInstance();

			String orgno = roleds.getString(i, "orgno");
			String orgname = roleds.getString(i, "orgname");
			String roleno = roleds.getString(i, "roleno");
			String rolename = roleds.getString(i, "rolename");
			//取操作员
			int row = 0;
			for(int k = 1;k< 31 ;k++){
				String loginname = roleds.getString(i, "loginname"+k);
				if("".equals(loginname)){
					continue;
				}else{
					de.clearSql();
					de.addSql(" select empno,empname from odssu.empinfor ");
					de.addSql("  where loginname = :loginname ");
					de.setString("loginname",loginname);
					DataStore empnods = de.query();
					String empno= empnods.getString(0,"empno");
					String empname = empnods.getString(0,"empname");
					empds.put(row,"empno",empno);
					empds.put(row,"empname",empname);
					row++;
				}
			}

			for (int j =0; j<empds.rowCount();j++){
				String empno = empds.getString(j, "empno");
				String empname = empds.getString(j, "empname");

				role_emporgds.put(j,"orgno",orgno);
				role_emporgds.put(j,"empno",empno);
				role_emporgds.put(j,"epflag","(+)");

				role_empds.put(j,"empno",empno);

				loginfods.put(logi, "orgno",orgno );
				loginfods.put(logi, "empno", empno);
				loginfods.put(logi, "roleno",roleno );
				loginfods.put(logi, "empname", empname);
				loginfods.put(logi, "orgname", orgname);
				loginfods.put(logi, "rolename", rolename);
				loginfods.put(logi++, "opflag", "1");
				
			}
			role_orgds.put(0,"orgno",orgno);
			try {
				DataObject vdo = DataObject.getInstance();
				vdo.put("role_emporgds", role_emporgds);
				vdo.put("role_empds", role_empds);
				vdo.put("role_orgds", role_orgds);
				vdo.put("roleno", roleno);
				//调用记账的逻辑
				iASO.doEntry(vdo);
			} catch (ASOException e) {
				e.printStackTrace();
				LanePromptUtil.promptToTrace("【"+(i+1)+"】导入角色【"+rolename +"】时出错,记账异常中止 !");
				LanePromptUtil.complete();
				throw new AppException("批量导入角色时出错！"+e.getMessage());
			}
			logBF.append("【"+(i+1)+"】导入了角色【"+rolename +"】.\r\n");
			LanePromptUtil.promptToTrace("【"+(i+1)+"】导入了角色【"+rolename +"】.");
			
		}
		logBF.append("批量导入角色- 记账完成！\r\n");
		LanePromptUtil.promptToTrace("批量导入角色 - 记账完成！");
		LanePromptUtil.complete();
		
		DataObject logdo = DataObject.getInstance();
		String pdid = "batchImportRoles";
		String userid = this.getUser().getUserid();
		
		logdo.put("managerno",userid);
		logdo.put("sprno",userid);
		logdo.put("pdid",pdid);
		logdo.put("loginfods", loginfods);
		
		LogManageAPI LogAPI = new LogManageAPI();
		LogAPI.addAdjustEmpRoleLog(logdo);
		
		DataObject result = DataObject.getInstance();
		result.put("dutyimportlogstr", logBF.toString());
		result.put("returnfiletype", "txt");
		return result;
	}
	
	public DataObject importFtnData(DataObject para) throws Exception {
		DE de = DE.getInstance();
		DataObject result = DataObject.getInstance();
		// 获取数据
		String appid = para.getString("appid");
		DataStore ds_fn_bussiness_scope = para.getDataStore("ds_fn_bussiness_scope");
		DataStore ds_fn_roletype = para.getDataStore("ds_fn_roletype");
		DataStore ds_db_appFunction = para.getDataStore("ds_db_appFunction");
		DataStore ds_fn_Folder = para.getDataStore("ds_fn_Folder");
		DataStore ds_appFunction = para.getDataStore("ds_appFunction");
		DataStore ds_dbid = para.getDataStore("ds_dbid");

		// 进行参数校验
		if (appid == null || appid.equals("")) {
			result.put("msg","导入数据出错，传入的appid为空");
			result.put("importflag", false);
			return result;
		}
		if (ds_appFunction == null || ds_appFunction.rowCount() == 0) {
			result.put("msg","导入数据出错，function 基本信息不能为空！");
			result.put("importflag", false);
			return result;
		}
		if (ds_fn_roletype == null) {
			result.put("msg","导入数据出错，缺少角色关系");
			result.put("importflag", false);
			return result;
		}
		if (ds_db_appFunction == null) {
			result.put("msg","导入数据出错，缺少DBID关系");
			result.put("importflag", false);
			return result;
		}
		if (ds_fn_Folder == null || ds_fn_Folder.rowCount() == 0) {
			result.put("msg","导入数据出错，缺少目录关系");
			result.put("importflag", false);
			return result;
		}
		if (ds_fn_bussiness_scope == null) {
			result.put("msg","导入数据出错，缺少业务范畴关系");
			result.put("importflag", false);
			return result;
		}

		DataStore dbidinfo = DataStore.getInstance();
		de.clearSql();
  		de.addSql("select dbid from odssu.dbidinfo ");
		dbidinfo = de.query();
	
		if(dbidinfo.rowCount() !=ds_dbid.rowCount()){
			result.put("msg","数据库中dbid为"+dbidinfo.getColumn("dbid").toString()+",导入文件中的dbid为"+ds_dbid.getColumn("dbid").toString()+",dbid不匹配，不允许导入！");
		    result.put("importflag", false);
		    return result;
		}
		for (int i = 0; i < dbidinfo.rowCount(); i++) {
		    String dbid = dbidinfo.getString(i, "dbid");
		    if(ds_dbid.find("dbid == " + dbid,0)==-1){
		    	result.put("msg","数据库中dbid为"+dbidinfo.getColumn("dbid").toString()+",导入文件中的dbid为"+ds_dbid.getColumn("dbid").toString()+",dbid不匹配，不允许导入！");
		        result.put("importflag", false);
		        return result;
		    }
		}
		if (ds_appFunction.rowCount() != 0) {

			delFnbusinessscope(appid);
			delFnroletype(appid);
			delDbappfunction(appid);
			delFnfolder(appid);
			delAppfunction(appid);
			// 向基本信息表中插入信息
			de.clearSql();
	  		de.addSql("delete from odssu.fn_business_scope  ");
	  		de.addSql(" where functionid = :functionid ");
	  		DataStore dscopeps = DataStore.getInstance();
  			for (int i = 0; i < ds_appFunction.rowCount(); i++) {
				String functionid = ds_appFunction.getString(i, "functionid");
				dscopeps.put(i, "functionid", functionid);
			}
  			de.batchUpdate(dscopeps);

			de.clearSql();
	  		de.addSql("delete from odssu.fn_roletype  ");
	  		de.addSql(" where functionid = :functionid ");
	  		DataStore droletypeps = DataStore.getInstance();
  			for (int i = 0; i < ds_appFunction.rowCount(); i++) {
				String functionid = ds_appFunction.getString(i, "functionid");
				droletypeps.put(i, "functionid", functionid);
			}
  			de.batchUpdate(droletypeps);
  			

			de.clearSql();
	  		de.addSql("delete from odssu.db_appfunction  ");
	  		de.addSql(" where functionid = :functionid ");
	  		DataStore ddbappps = DataStore.getInstance();
  			for (int i = 0; i < ds_appFunction.rowCount(); i++) {
				String functionid = ds_appFunction.getString(i, "functionid");
				ddbappps.put(i, "functionid", functionid);
			}
  			de.batchUpdate(ddbappps);
  			
  			
  			for (int i = 0; i < ds_appFunction.rowCount(); i++) {
  				
  				String functionid = ds_appFunction.getString(i, "functionid");
  				String functionname = ds_appFunction.getString(i, "functionname");
				String pdid = ds_appFunction.getString(i, "pdid");
				String fnfolderid = ds_appFunction.getString(i, "fnfolderid");
				
				de.clearSql();
		  		de.addSql("delete from odssu.appfunction  ");
		  		de.addSql(" where functionid = :functionid ");
				de.setString("functionid", functionid);
	  			de.update();
				
				de.clearSql();
	  			de.addSql(" insert into odssu.appfunction (functionid,functionname,pdid,fnfolderid,appid)");
	  			de.addSql("  values (	 :functionid 	 ,   :functionname 	 ,  :pdid  ,  :fnfolderid,  :appid)   ");
	  			de.setString("functionid", functionid);
	  			de.setString("functionname", functionname);
	  			de.setString("pdid", pdid);
	  			de.setString("fnfolderid", fnfolderid);
	  			de.setString("appid", appid);
	  			de.update();
  			}
  			DataStore vps = DataStore.getInstance();
			// 业务范畴
			if (ds_fn_bussiness_scope.rowCount() != 0) {
				de.clearSql();
  				de.addSql(" insert into odssu.fn_business_scope (functionid,scopeid)");
  				de.addSql("  values (	 :functionid 	 ,   :scopeid 	 )");
  				vps.clear();
				for (int i = 0; i < ds_fn_bussiness_scope.rowCount(); i++) {
					String functionid = ds_fn_bussiness_scope.getString(i, "functionid");
					String scopeid = ds_fn_bussiness_scope.getString(i, "scopeid");
					vps.put(i, "functionid", functionid);
					vps.put(i, "scopeid", scopeid);
				}
	  			de.batchUpdate(vps);
			}
			// 角色类型
			if (ds_fn_roletype.rowCount() != 0) {
				de.clearSql();
  				de.addSql(" insert into odssu.fn_roletype (functionid,roletypeno)");
  				de.addSql("  values (	 :functionid 	 ,   :roletypeno 	 )");
  				vps.clear();
				for (int i = 0; i < ds_fn_roletype.rowCount(); i++) {
					String functionid = ds_fn_roletype.getString(i, "functionid");
					String roletypeno = ds_fn_roletype.getString(i, "roletypeno");
					vps.put(i, "functionid", functionid);
					vps.put(i, "roletypeno", roletypeno);
				}
	  			de.batchUpdate(vps);
			}
			// DBID
			if (ds_db_appFunction.rowCount() != 0) {
				de.clearSql();
				vps.clear();
  				de.addSql(" insert into odssu.db_appfunction (functionid,dbid)");
  				de.addSql("  values (	 :functionid 	 ,   :dbid 	 )");
				for (int i = 0; i < ds_db_appFunction.rowCount(); i++) {
					String dbid = ds_db_appFunction.getString(i, "dbid");
					String functionid = ds_db_appFunction.getString(i, "functionid");
					vps.put(i, "functionid", functionid);
					vps.put(i, "dbid",  dbid);
				}
	  			de.batchUpdate(vps);
			}
			// 修改目录
			de.clearSql();
	  		de.addSql("delete from odssu.fn_folder  ");
	  		de.addSql(" where fnfolderid = :fnfolderid ");
	  		DataStore dfolderps = DataStore.getInstance();
  			for (int i = 0; i < ds_fn_Folder.rowCount(); i++) {
				String folderid = ds_fn_Folder.getString(i, "fnfolderid");
				dfolderps.put(i, "fnfolderid", folderid);
			}
  			de.batchUpdate(dfolderps);
			
			if (ds_fn_Folder.rowCount() != 0) {
				de.clearSql();
				vps.clear();
  				de.addSql(" insert into odssu.fn_folder (fnfolderid,pfnfolderid,folderlabel)");
  				de.addSql("  values (	 :folderid 	 ,   :pfnfolderid 	 ,:folderlabel  ) ");
				for (int i = 0; i < ds_fn_Folder.rowCount(); i++) {
					String folderid = ds_fn_Folder.getString(i, "fnfolderid");
					String pfnfolderid = ds_fn_Folder.getString(i, "pfnfolderid");
					String folderlabel = ds_fn_Folder.getString(i, "folderlabel");
					vps.put(i, "folderid", folderid);
					vps.put(i, "pfnfolderid", pfnfolderid);
					vps.put(i, "folderlabel", folderlabel);
				}
	  			de.batchUpdate(vps);
			}

			// 清理fn与role联系
			de.clearSql();
  			de.addSql(" delete from odssu.role_function_manual a ");
  			de.addSql(" where not exists  ");
  			de.addSql(" ( select 1 from odssu.appfunction b where a.functionid = b.functionid ) ");
			de.update();
			
			if(GlobalNames.DEPLOY_IN_ECO) {
				SendMsgUtil.SynAllFN(appid);
			}
			
		}
		
		result.put("importflag", true);
		result.put("msg", "成功导入XML文件");
		return result;
	}
	
	public void delFnbusinessscope(String appid) throws AppException {
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("delete from odssu.fn_business_scope a ");
  		de.addSql(" where exists (select 1 ");
  		de.addSql("          from odssu.appfunction b ");
  		de.addSql("         where a.functionid = b.functionid ");
  		de.addSql("           and b.appid = :appid) ");
		de.setString("appid", appid);
		de.update();
	}
	public void delFnroletype(String appid) throws AppException {
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("delete from odssu.fn_roletype a ");
  		de.addSql(" where exists (select 1 ");
  		de.addSql("          from odssu.appfunction b ");
  		de.addSql("         where a.functionid = b.functionid ");
  		de.addSql("           and b.appid = :appid) ");
		de.setString("appid", appid);
		de.update();
	}
	public void delDbappfunction(String appid) throws AppException {
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("delete from odssu.db_appfunction a ");
  		de.addSql(" where exists (select 1 ");
  		de.addSql("          from odssu.appfunction b ");
  		de.addSql("         where a.functionid = b.functionid ");
  		de.addSql("           and b.appid = :appid) ");
		de.setString("appid", appid);
		de.update();
	}
	
	public void delFnfolder(String appid) throws AppException {
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select fnfolderid ");
  		de.addSql("  from odssu.fn_folder fo ");
  		de.addSql(" where fo.fnfolderid in (select b.pfnfolderid ");
  		de.addSql("                           from odssu.appfunction a, odssu.fn_folder b ");
  		de.addSql("                          where a.fnfolderid = b.fnfolderid ");
  		de.addSql("                            and a.appid = :appid) ");
  		de.addSql("   and fo.pfnfolderid is null ");
		de.setString("appid", appid);
		DataStore tmpds = de.query();

		if (tmpds != null && tmpds.rowCount() != 0) {

			for (int j = 0; j < tmpds.rowCount(); j++) {

				String rootFolderId = tmpds.getString(j, "fnfolderid");
				DataStore tmpds1  = DataStore.getInstance();
				getParentFolder(tmpds1,rootFolderId); 				

				for (int i = 0; i < tmpds1.rowCount(); i++) {
					String fnfolderid = tmpds1.getString(i, "fnfolderid");
  					de.clearSql();
  					de.addSql(" delete from odssu.fn_folder where fnfolderid = :fnfolderid");
  					de.setString("fnfolderid",fnfolderid);
  					de.update();
				}
			}
		}
	}
	
	private void getParentFolder(DataStore vds,String folderno) throws AppException {
		
		if(folderno == null || folderno.equals("")) {
			return;
		}
		DE de = DE.getInstance();
		de.clearSql();
		de.addSql("select * from odssu.fn_folder t where t.fnfolderid = :folderno "); 
		de.setString("folderno",folderno);
		DataStore vds_tmp = de.query();

		if(vds_tmp == null || vds_tmp.rowCount() == 0) {
			return;
		}
		
		DataObject vdo_tmp = vds_tmp.get(0);
		vds.addRow(vdo_tmp);
		
		String pfolderno = vdo_tmp.getString("pfnfolderid","");
		getParentFolder(vds,pfolderno);
		
	}

	
	public void delAppfunction(String appid) throws AppException {
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("delete from odssu.appfunction a ");
  		de.addSql(" where a.appid = :appid ");
		de.setString("appid", appid);
		de.update();
	}
	
	public DataObject getOrgType(DataObject para) throws AppException,BusinessException{
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select a.typeno, a.typename from odssu.org_type a where a.yxzjjgbz = '1' ");
		DataStore vds = de.query();
		if (vds == null || vds.rowCount() == 0) {
			throw new AppException("没有允许自建的机构类型");
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("orgtypeds", vds);
		return vdo;
	}
	
	/**
	 * 描述：将Excel转换成datastore
	 * author: sjn
	 * date: 2017年11月20日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject analyzeExcelToDsDptype(DataObject para) throws Exception {
		DataStore dsExcel;
		DataStore tableInfo = DataStore.getInstance();
		InputStream is;
		Workbook wb;
		Sheet sheet;

		// 取参数
		CommonsMultipartFile file = (CommonsMultipartFile) para.getObject("file");
		// 参数判断
		if (null == file) {
			this.bizException("传入的参数file为空!");
		}
		
		// 构建解析列
		int vi = 0;
		setTableInfo(tableInfo, vi++, "rolename","岗位","String");
		setTableInfo(tableInfo, vi++, "pdid","流程/功能ID","String");
		setTableInfo(tableInfo, vi++, "pdlabel", "流程/功能名称", "String");
		setTableInfo(tableInfo, vi++, "dptdid", "流程节点ID", "String");
		setTableInfo(tableInfo, vi++, "dptdlabel", "流程节点名称", "String");
		setTableInfo(tableInfo, vi++, "dptype", "流程节点标记", "String");
		// 开始解析
		is = ExcelTool.getStreamByFile(file);
		wb = ExcelTool.getExcelFileByStream(is); 
		sheet = ExcelTool.getSheet(wb, 0);

		// 上传文件中的数据
		dsExcel = ExcelTool.getDataStoreBySheet(sheet, 0, 1, 70000, tableInfo);
		dsExcel = ExcelTool.removeBlankRowWithTrim(dsExcel);

		ExcelTool.closeWorkbook(wb);
		ExcelTool.closeInputStream(is);
		//去掉左右空格
		for (int i=0 ;i < dsExcel.rowCount(); i++){
			if(dsExcel.getString(i, "rolename") != null)
				dsExcel.put(i, "rolename", dsExcel.getString(i, "rolename").trim());
			if(dsExcel.getString(i, "pdid") != null)
				dsExcel.put(i, "pdid", dsExcel.getString(i, "pdid").trim());
			if(dsExcel.getString(i, "pdlabel") != null)
				dsExcel.put(i, "pdlabel", dsExcel.getString(i, "pdlabel").trim());
			if(dsExcel.getString(i, "dptdid") != null)
				dsExcel.put(i, "dptdid", dsExcel.getString(i, "dptdid").trim());
			if(dsExcel.getString(i, "dptdlabel") != null)
				dsExcel.put(i, "dptdlabel", dsExcel.getString(i, "dptdlabel").trim());
			if(dsExcel.getString(i, "dptype") != null)
				dsExcel.put(i, "dptype", dsExcel.getString(i, "dptype").trim());
		}
		DataObject result = DataObject.getInstance();
		result.put("fileDptype", dsExcel);
		return result;
	}
	
	/**
	 * 描述：批量导入岗位类别校验
	 * author: sjn
	 * date: 2017年11月20日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject checkBatchImportDptype(DataObject para) throws AppException, BusinessException{
		DE de = DE.getInstance();
		StringBuffer logBF = new StringBuffer();
		StringBuffer clqk = new StringBuffer();

		// 获取文件数据fileds
		DataObject dataDptype = para.getDataObject("dataDptype");
		if(dataDptype == null || !dataDptype.containsKey("fileDptype")){
			LanePromptUtil.end();
			this.bizException("没有正确读入文件!");
		}
		DataStore fileds = dataDptype.getDataStore("fileDptype");
		if(fileds == null || fileds.rowCount() == 0){
			LanePromptUtil.end();
			this.bizException("没有流程类别可以导入。");
		}
		LanePromptUtil.promptToTrace("即将开始校验导入流程类别的相关信息：共【"+fileds.rowCount()+"】个.");
		logBF.append(">>>即将开始校验导入流程类别的相关信息：共【"+fileds.rowCount()+"】个.\r\n");

		boolean checkflag = true;
		for (int i = 0; i < fileds.rowCount(); i++) {
			//每次校验尽可能多的发现错误，除非当前错误将会使得后续校验无法继续
			fileds.put(i, "clqk", " ");
			clqk.setLength(0);		
			String rolename = fileds.getString(i, "rolename");
			String pdid = fileds.getString(i, "pdid");
			String pdlabel = fileds.getString(i, "pdlabel");
			String dptdid = fileds.getString(i, "dptdid");
			String dptdlabel = fileds.getString(i, "dptdlabel");
			LanePromptUtil.promptToTrace("【"+(i+1)+"】:校验流程任务【"+ pdlabel + "." + dptdlabel + "】的相关信息.");
			logBF.append("【"+(i+1)+"】:校验流程任务【"+ pdlabel + "." + dptdlabel + "】的相关信息.\r\n");
			if (rolename == null || rolename.equals("")) {
				clqk.append("岗位名称为空.");
			}
			if (pdid == null || pdid.equals("")) {
				clqk.append("流程/功能ID为空.");
			}
			if (pdlabel == null || pdlabel.equals("")) {
				clqk.append("流程/功能名称为空.");
			}
			if (dptdid == null || dptdid.equals("")) {
				clqk.append("流程节点ID为空.");
			}
			if (dptdlabel == null || dptdlabel.equals("")) {
				clqk.append("流程节点名称为空.");
			}
			
			//后面的校验都要求以上属性非空才能展开，所以如果前边有空值，只能continue
			if( clqk.length() != 0){
				checkflag = false;
				fileds.put(i, "clqk", ""+clqk.toString());
				logBF.append("处理情况："+clqk.toString()+"\r\n");
				LanePromptUtil.promptToTrace("处理情况："+clqk.toString());
				continue;
			}
			
			//校验岗位是否存在
			de.clearSql();
  			de.addSql("select 1 from odssu.roleinfor a where a.rolename = :rolename ");
			de.setString("rolename",rolename);
			DataStore rolevds = de.query();
			if (rolevds == null || rolevds.rowCount() == 0) {
				clqk.append("岗位【"+rolename+"】不存在 .");
			}
			
			//校验流程是否存在
			de.clearSql();
  			de.addSql("select 1 from bpzone.process_define a where a.pdid = :pdid and a.pdlabel = :pdlabel ");
			de.setString("pdid",pdid);
			de.setString("pdlabel",pdlabel);
			DataStore pdvds = de.query();
			if (pdvds == null || pdvds.rowCount() == 0) {
				clqk.append("不存在【pdid为"+pdid+"并且pdlabel为"+pdlabel +"】的流程 .");
			}
			
			//校验流程任务是否存在
			de.clearSql();
  			de.addSql("select 1 from bpzone.dutyposition_task a where a.pdid = :pdid and a.dptdid = :dptdid and a.dptdlabel = :dptdlabel ");
			de.setString("pdid",pdid);
			de.setString("dptdid",dptdid);
			de.setString("dptdlabel",dptdlabel);
			DataStore dpvds = de.query();
			if (dpvds == null || dpvds.rowCount() == 0) {
				clqk.append("不存在【pdid为"+pdid+",dptdid为"+dptdid +"并且dptdlabel为"+dptdlabel+"】的流程任务 .");
			}
			
			if( clqk.length() != 0){
				checkflag = false;
				logBF.append("处理情况："+clqk.toString()+"\r\n");
				fileds.put(i, "clqk", clqk.toString());
				LanePromptUtil.promptToTrace("处理情况："+clqk.toString());
			}
		}
		//校验若不通过，返回excel让用户对照错误信息，修改完善
		if(checkflag == false){
			LanePromptUtil.promptToTrace("校验完成，发现错误信息，请修改文件数据.");
			logBF.append(">>>校验完成，发现错误信息，请修改文件数据.\r\n");
			LanePromptUtil.complete();
			DataObject result = DataObject.getInstance();
			result.put("excelds", fileds);
			result.put("returnfiletype", "excel");
			return result;
		}else{
			LanePromptUtil.promptToTrace("导入流程类别的信息校验成功.");
			logBF.append(">>>导入流程类别的信息校验成功.\r\n");
			LanePromptUtil.complete();
			DataObject result = DataObject.getInstance();
			result.put("dptypeimportlogstr", logBF.toString());
			result.put("returnfiletype", "txt");
			return result;
		}
	}
	
	/**
	 * 描述：生成批量导入流程类别错误报告Excel
	 * author: sjn
	 * date: 2017年11月20日
	 * @param para
	 * @return
	 * @throws IOException
	 * @throws AppException
	 */
	public DataObject returnFileForBatchImportDptype(DataObject para) throws IOException, AppException {
		DataObject vdo = DataObject.getInstance();// 存最终组成的excel并返回
		DataStore excelDS = para.getDataStore("excelds");
		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 生成一个表格
		HSSFSheet sheet = workbook.createSheet("流程类别信息");
		sheet.setColumnWidth(6, 12000);
		// 样式
		//sheet.setDefaultColumnWidth(15);
		// 生成一个样式
		HSSFCellStyle style = workbook.createCellStyle();
		// 设置这些样式
		style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);
		// 生成一个字体
		HSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.BLACK.index);
		font.setFontHeightInPoints((short) 12);
		font.setBold(true);
		// 生成一个字体
		HSSFFont fontred = workbook.createFont();
		fontred.setColor(HSSFColor.RED.index);
		fontred.setFontHeightInPoints((short) 12);
		fontred.setBold(true);
		HSSFCellStyle styleRed = workbook.createCellStyle();
		styleRed.setFont(fontred);
		// 把字体应用到当前的样式
		style.setFont(font);
		
		// 准备表格数据
		DataStore firstrow = DataStore.getInstance();
		firstrow.put(0, "name", "处理情况");
		firstrow.put(1, "name", "岗位");
		firstrow.put(2, "name", "流程/功能ID");
		firstrow.put(3, "name", "流程/功能名称");
		firstrow.put(4, "name", "流程节点ID");
		firstrow.put(5, "name", "流程节点名称");
		firstrow.put(6, "name", "流程节点标记");

		// 产生表格标题行
		HSSFRow row = sheet.createRow(0);
		
		for (int i = 0; i < firstrow.rowCount(); i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(firstrow.getString(i, "name"));
			cell.setCellValue(text);
		}
		
		for(int j=0; j <excelDS.rowCount();j++){
			// 产生数据事例行
			HSSFRow rowtemp = sheet.createRow(j+1);
			HSSFCell cell = rowtemp.createCell(0);
			cell.setCellStyle(styleRed);
			HSSFRichTextString text = new HSSFRichTextString(excelDS.getString(j, "clqk"));
			cell.setCellValue(text);
			HSSFCell cell1 = rowtemp.createCell(1);
			HSSFRichTextString text1 = new HSSFRichTextString(excelDS.getString(j, "rolename"));
			cell1.setCellValue(text1);
			HSSFCell cell2 = rowtemp.createCell(2);
			HSSFRichTextString text2 = new HSSFRichTextString(excelDS.getString(j, "pdid"));
			cell2.setCellValue(text2);
			HSSFCell cell3 = rowtemp.createCell(3);
			HSSFRichTextString text3 = new HSSFRichTextString(excelDS.getString(j, "pdlabel"));
			cell3.setCellValue(text3);
			HSSFCell cell4 = rowtemp.createCell(4);
			HSSFRichTextString text4 = new HSSFRichTextString(excelDS.getString(j, "dptdid"));
			cell4.setCellValue(text4);
			HSSFCell cell5 = rowtemp.createCell(5);
			HSSFRichTextString text5 = new HSSFRichTextString(excelDS.getString(j, "dptdlabel"));
			cell5.setCellValue(text5);
			HSSFCell cell6 = rowtemp.createCell(6);
			HSSFRichTextString text6 = new HSSFRichTextString(excelDS.getString(j, "dptype"));
			cell6.setCellValue(text6);
		}
		vdo.put("wb", workbook);
		return vdo;
	}
	
	/**
	 * 描述：批量导入流程任务类别
	 * author: sjn
	 * date: 2017年11月7日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject saveBatchImportDptype(DataObject para) throws Exception{
		StringBuffer logBF = new StringBuffer();

		// 获取session里取来的文件数据
		DataObject dataDptype = para.getDataObject("dataDptype");
		if(dataDptype == null || !dataDptype.containsKey("fileDptype")){
			LanePromptUtil.end();
			this.bizException("没有正确读入文件!");
		}
		DataStore fileds = dataDptype.getDataStore("fileDptype");
		if(fileds == null || fileds.rowCount() == 0){
			LanePromptUtil.end();
			this.bizException("流程类别总数为0!");
		}
		logBF.append("开始批量导入流程任务类别,共【"+  fileds.rowCount()+"】个：\r\n");
		LanePromptUtil.promptToTrace("开始批量导入流程任务类别,共【"+  fileds.rowCount()+"】个：");
		DE de = DE.getInstance();
		//开始记账
		for (int i = 0; i < fileds.rowCount(); i++) {
			String rolename = fileds.getString(i, "rolename");
			String pdid = fileds.getString(i, "pdid");
			String pdlabel = fileds.getString(i, "pdlabel");
			String dptdid = fileds.getString(i, "dptdid");
			String dptdlabel = fileds.getString(i, "dptdlabel");
			String dptype = fileds.getString(i, "dptype");
			
			de.clearSql();
  			de.addSql("delete from odssu.dptype a where a.pdid = :pdid and a.dptdid = :dptdid ");
			de.setString("pdid",pdid);
			de.setString("dptdid",dptdid);
			de.update();
			
			de.clearSql();
  			de.addSql("insert into odssu.dptype ");
  			de.addSql("  (pdid, pdlabel, dptdid, dptdlabel, dptype) ");
  			de.addSql("values ");
  			de.addSql("  (:pdid, :pdlabel, :dptdid, :dptdlabel, :dptype) ");
			de.setString("pdid",pdid);
			de.setString("pdlabel",pdlabel);
			de.setString("dptdid",dptdid);
			de.setString("dptdlabel",dptdlabel);
			de.setString("dptype",dptype);
			de.update();
			
			de.clearSql();
  			de.addSql("select a.roleno from odssu.roleinfor a where a.rolename = :rolename ");
			de.setString("rolename",rolename);
			DataStore rolevds = de.query();
			if (rolevds == null || rolevds.rowCount() == 0) {
				throw new AppException("不存在【"+rolename+"】的岗位信息");
			}
			String roleno = rolevds.getString(0, "roleno");
			de.clearSql();
  			de.addSql("delete from bpzone.dutyposition_task_role a where a.roleid = :roleno and a.pdid = :pdid and a.dptdid = :dptdid ");
			de.setString("roleno",roleno);
			de.setString("pdid",pdid);
			de.setString("dptdid",dptdid);
			de.update();
			
			de.clearSql();
  			de.addSql("insert into bpzone.dutyposition_task_role ");
  			de.addSql("  (roleid, roleeffectmode, pdid, dptdid, rolelabel, toccode) ");
  			de.addSql("values ");
  			de.addSql("  (:roleno, '@', :pdid, :dptdid, :rolename,'-') ");
			de.setString("roleno",roleno);
			de.setString("pdid",pdid);
			de.setString("dptdid",dptdid);
			de.setString("rolename",rolename);
			de.update();
			
			LanePromptUtil.promptToTrace("【"+(i+1)+"】导入流程任务【"+ pdlabel + "." + dptdlabel + "】的类别记账成功,类别为【"+dptype+"】.");
		}
		//删除流程任务对应的角色类型与岗位的角色类型不匹配的流程任务
		de.clearSql();
  		de.addSql("delete from bpzone.dutyposition_task_role a ");
  		de.addSql(" where not exists (select 1 ");
  		de.addSql("          from bpzone.dproletype b, odssu.roleinfor c ");
  		de.addSql("         where a.pdid = b.pdid ");
  		de.addSql("           and a.dptdid = b.dptdid ");
  		de.addSql("           and a.roleid = c.roleno ");
  		de.addSql("           and c.roletype = b.roletypeid) ");
		int roletypenum = de.update();
		logBF.append("由于流程任务适用的岗位类型和导入岗位不符，删除【"+roletypenum+"】个流程任务！\r\n");
		//删除流程的业务范畴与岗位的业务范畴不匹配的流程任务
		de.clearSql();
  		de.addSql("delete from bpzone.dutyposition_task_role a ");
  		de.addSql(" where not exists (select 1 ");
  		de.addSql("          from bpzone.process_define_in_activiti b, ");
  		de.addSql("               bpzone.process_businesstype       c, ");
  		de.addSql("               odssu.ir_role_business_scope      d ");
  		de.addSql("         where a.pdid = b.pdid ");
  		de.addSql("           and b.pdaid = c.pdaid ");
  		de.addSql("           and c.ywlxid = d.scopeno ");
  		de.addSql("           and a.roleid = d.roleno) ");
		int scopenum = de.update();
		logBF.append("由于流程任务的业务范畴和导入岗位不符，删除【"+scopenum+"】个流程任务！\r\n");
		
		logBF.append("批量导入流程任务 - 记账完成！\r\n");
		LanePromptUtil.promptToTrace("批量导入流程任务 - 记账完成！");
		LanePromptUtil.complete();
		DataObject result = DataObject.getInstance();
		result.put("dptypeimportlogstr", logBF.toString());
		result.put("returnfiletype", "txt");
		return result;
	}
	
	/**
	 * 描述：将Excel转换成datastore
	 * author: sjn
	 * date: 2018年1月3日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject analyzeExcelToDsFntype(DataObject para) throws Exception {
		DataStore dsExcel;
		DataStore tableInfo = DataStore.getInstance();
		InputStream is;
		Workbook wb;
		Sheet sheet;
		
		// 取参数
		CommonsMultipartFile file = (CommonsMultipartFile) para.getObject("file");
		// 参数判断
		if (null == file) {
			this.bizException("传入的参数file为空!");
		}
		
		// 构建解析列
		int vi = 0;
		setTableInfo(tableInfo, vi++, "rolename","岗位名称","String");
		setTableInfo(tableInfo, vi++, "fnid","功能id","String");
		setTableInfo(tableInfo, vi++, "fnlabel", "功能名称", "String");
		setTableInfo(tableInfo, vi++, "fntype", "类别", "String");
		// 开始解析
		is = ExcelTool.getStreamByFile(file);
		wb = ExcelTool.getExcelFileByStream(is); 
		sheet = ExcelTool.getSheet(wb, 0);
		
		// 上传文件中的数据
		dsExcel = ExcelTool.getDataStoreBySheet(sheet, 0, 1, 70000, tableInfo);
		dsExcel = ExcelTool.removeBlankRowWithTrim(dsExcel);
		
		ExcelTool.closeWorkbook(wb);
		ExcelTool.closeInputStream(is);
		//去掉左右空格
		for (int i=0 ;i < dsExcel.rowCount(); i++){
			if(dsExcel.getString(i, "rolename") != null)
				dsExcel.put(i, "rolename", dsExcel.getString(i, "rolename").trim());
			if(dsExcel.getString(i, "fnid") != null)
				dsExcel.put(i, "fnid", dsExcel.getString(i, "fnid").trim());
			if(dsExcel.getString(i, "fnlabel") != null)
				dsExcel.put(i, "fnlabel", dsExcel.getString(i, "fnlabel").trim());
			if(dsExcel.getString(i, "fntype") != null)
				dsExcel.put(i, "fntype", dsExcel.getString(i, "fntype").trim());
		}
		DataObject result = DataObject.getInstance();
		result.put("fileFntype", dsExcel);
		return result;
	}
	
	/**
	 * 描述：批量导入功能类别校验
	 * author: sjn
	 * date: 2018年1月3日
	 * @param para
	 * @return
	 * @throws AppException
	 * @throws BusinessException
	 */
	public DataObject checkBatchImportFntype(DataObject para) throws AppException, BusinessException{
		DE de = DE.getInstance();
		StringBuffer logBF = new StringBuffer();
		StringBuffer clqk = new StringBuffer();
		
		// 获取文件数据fileds
		DataObject dataFntype = para.getDataObject("dataFntype");
		if(dataFntype == null || !dataFntype.containsKey("fileFntype")){
			LanePromptUtil.end();
			this.bizException("没有正确读入文件!");
		}
		DataStore fileds = dataFntype.getDataStore("fileFntype");
		if(fileds == null || fileds.rowCount() == 0){
			LanePromptUtil.end();
			this.bizException("没有功能类别可以导入。");
		}
		LanePromptUtil.promptToTrace("即将开始校验导入功能类别的相关信息：共【"+fileds.rowCount()+"】个.");
		logBF.append(">>>即将开始校验导入功能类别的相关信息：共【"+fileds.rowCount()+"】个.\r\n");
		
		boolean checkflag = true;
		for (int i = 0; i < fileds.rowCount(); i++) {
			//每次校验尽可能多的发现错误，除非当前错误将会使得后续校验无法继续
			fileds.put(i, "clqk", " ");
			clqk.setLength(0);		
			String rolename = fileds.getString(i, "rolename");
			String fnid = fileds.getString(i, "fnid");
			String fnlabel = fileds.getString(i, "fnlabel");
			LanePromptUtil.promptToTrace("【"+(i+1)+"】:校验功能任务【"+ fnlabel + "】的相关信息.");
			logBF.append("【"+(i+1)+"】:校验功能任务【"+ fnlabel + "】的相关信息.\r\n");
			if (rolename == null || rolename.equals("")) {
				clqk.append("岗位名称为空.");
			}
			if (fnid == null || fnid.equals("")) {
				clqk.append("功能ID为空.");
			}
			if (fnlabel == null || fnlabel.equals("")) {
				clqk.append("功能名称为空.");
			}
			
			//后面的校验都要求以上属性非空才能展开，所以如果前边有空值，只能continue
			if( clqk.length() != 0){
				checkflag = false;
				fileds.put(i, "clqk", ""+clqk.toString());
				logBF.append("处理情况："+clqk.toString()+"\r\n");
				LanePromptUtil.promptToTrace("处理情况："+clqk.toString());
				continue;
			}
			
			//校验岗位是否存在
			de.clearSql();
  			de.addSql("select 1 from odssu.roleinfor a where a.rolename = :rolename ");
			de.setString("rolename",rolename);
			DataStore rolevds = de.query();
			if (rolevds == null || rolevds.rowCount() == 0) {
				clqk.append("岗位【"+rolename+"】不存在 .");
			}
			
			//校验功能是否存在
			de.clearSql();
  			de.addSql(" select 1 from odssu.appfunction a where a.functionid = :fnid and a.functionname = :fnlabel ");
			de.setString("fnid",fnid);
			de.setString("fnlabel",fnlabel);
			DataStore pdvds = de.query();
			if (pdvds == null || pdvds.rowCount() == 0) {
				clqk.append("不存在【fnid为"+fnid+"并且fnlabel为"+fnlabel +"】的功能 .");
			}
			
			if( clqk.length() != 0){
				checkflag = false;
				logBF.append("处理情况："+clqk.toString()+"\r\n");
				fileds.put(i, "clqk", clqk.toString());
				LanePromptUtil.promptToTrace("处理情况："+clqk.toString());
			}
		}
		//校验若不通过，返回excel让用户对照错误信息，修改完善
		if(checkflag == false){
			LanePromptUtil.promptToTrace("校验完成，发现错误信息，请修改文件数据.");
			logBF.append(">>>校验完成，发现错误信息，请修改文件数据.\r\n");
			LanePromptUtil.complete();
			DataObject result = DataObject.getInstance();
			result.put("excelds", fileds);
			result.put("returnfiletype", "excel");
			return result;
		}else{
			LanePromptUtil.promptToTrace("导入功能类别的信息校验成功.");
			logBF.append(">>>导入功能类别的信息校验成功.\r\n");
			LanePromptUtil.complete();
			DataObject result = DataObject.getInstance();
			result.put("fntypeimportlogstr", logBF.toString());
			result.put("returnfiletype", "txt");
			return result;
		}
	}
	
	/**
	 * 描述：生成批量导入功能类别错误报告Excel
	 * author: sjn
	 * date: 2018年1月3日
	 * @param para
	 * @return
	 * @throws IOException
	 * @throws AppException
	 */
	public DataObject returnFileForBatchImportFntype(DataObject para) throws IOException, AppException {
		DataObject vdo = DataObject.getInstance();// 存最终组成的excel并返回
		DataStore excelDS = para.getDataStore("excelds");
		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 生成一个表格
		HSSFSheet sheet = workbook.createSheet("功能类别信息");
		sheet.setColumnWidth(6, 12000);
		// 样式
		//sheet.setDefaultColumnWidth(15);
		// 生成一个样式
		HSSFCellStyle style = workbook.createCellStyle();
		// 设置这些样式
		style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);
		// 生成一个字体
		HSSFFont font = workbook.createFont();
		font.setColor(HSSFColor.BLACK.index);
		font.setFontHeightInPoints((short) 12);
		font.setBold(true);
		// 生成一个字体
		HSSFFont fontred = workbook.createFont();
		fontred.setColor(HSSFColor.RED.index);
		fontred.setFontHeightInPoints((short) 12);
		fontred.setBold(true);
		HSSFCellStyle styleRed = workbook.createCellStyle();
		styleRed.setFont(fontred);
		// 把字体应用到当前的样式
		style.setFont(font);
		
		// 准备表格数据
		DataStore firstrow = DataStore.getInstance();
		firstrow.put(0, "name", "处理情况");
		firstrow.put(1, "name", "岗位名称");
		firstrow.put(2, "name", "功能id");
		firstrow.put(3, "name", "功能名称");
		firstrow.put(4, "name", "类别");
		
		// 产生表格标题行
		HSSFRow row = sheet.createRow(0);
		
		for (int i = 0; i < firstrow.rowCount(); i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(firstrow.getString(i, "name"));
			cell.setCellValue(text);
		}
		
		for(int j=0; j <excelDS.rowCount();j++){
			// 产生数据事例行
			HSSFRow rowtemp = sheet.createRow(j+1);
			HSSFCell cell = rowtemp.createCell(0);
			cell.setCellStyle(styleRed);
			HSSFRichTextString text = new HSSFRichTextString(excelDS.getString(j, "clqk"));
			cell.setCellValue(text);
			HSSFCell cell1 = rowtemp.createCell(1);
			HSSFRichTextString text1 = new HSSFRichTextString(excelDS.getString(j, "rolename"));
			cell1.setCellValue(text1);
			HSSFCell cell2 = rowtemp.createCell(2);
			HSSFRichTextString text2 = new HSSFRichTextString(excelDS.getString(j, "fnid"));
			cell2.setCellValue(text2);
			HSSFCell cell3 = rowtemp.createCell(3);
			HSSFRichTextString text3 = new HSSFRichTextString(excelDS.getString(j, "fnlabel"));
			cell3.setCellValue(text3);
			HSSFCell cell4 = rowtemp.createCell(4);
			HSSFRichTextString text4 = new HSSFRichTextString(excelDS.getString(j, "fntype"));
			cell4.setCellValue(text4);
		}
		vdo.put("wb", workbook);
		return vdo;
	}
	
	/**
	 * 描述：批量导入功能任务类别记账
	 * author: sjn
	 * date: 2018年1月3日
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public DataObject saveBatchImportFntype(DataObject para) throws Exception{
		StringBuffer logBF = new StringBuffer();
		
		// 获取session里取来的文件数据
		DataObject dataFntype = para.getDataObject("dataFntype");
		if(dataFntype == null || !dataFntype.containsKey("fileFntype")){
			LanePromptUtil.end();
			this.bizException("没有正确读入文件!");
		}
		DataStore fileds = dataFntype.getDataStore("fileFntype");
		if(fileds == null || fileds.rowCount() == 0){
			LanePromptUtil.end();
			this.bizException("流程类别总数为0!");
		}
		logBF.append("开始批量导入功能任务类别,共【"+  fileds.rowCount()+"】个：\r\n");
		LanePromptUtil.promptToTrace("开始批量导入功能任务类别,共【"+  fileds.rowCount()+"】个：");
		DE de = DE.getInstance();
		//开始记账
		for (int i = 0; i < fileds.rowCount(); i++) {
			String rolename = fileds.getString(i, "rolename");
			String fnid = fileds.getString(i, "fnid");
			String fnlabel = fileds.getString(i, "fnlabel");
			String fntype = fileds.getString(i, "fntype");
			
			de.clearSql();
  			de.addSql("delete from odssu.fntype a where a.fnid = :fnid ");
			de.setString("fnid",fnid);
			de.update();
			
			de.clearSql();
  			de.addSql("insert into odssu.fntype ");
  			de.addSql("  (fnid, fnlabel, fntype) ");
  			de.addSql("values ");
  			de.addSql("  (:fnid, :fnlabel, :fntype) ");
			de.setString("fnid",fnid);
			de.setString("fnlabel",fnlabel);
			de.setString("fntype",fntype);
			de.update();
			
			de.clearSql();
  			de.addSql("select a.roleno from odssu.roleinfor a where a.rolename = :rolename ");
			de.setString("rolename",rolename);
			DataStore rolevds = de.query();
			if (rolevds == null || rolevds.rowCount() == 0) {
				throw new AppException("不存在【"+rolename+"】的岗位信息");
			}
			String roleno = rolevds.getString(0, "roleno");
			de.clearSql();
  			de.addSql("delete from odssu.role_function_manual a where a.roleno = :roleno and a.functionid = :fnid ");
			de.setString("roleno",roleno);
			de.setString("fnid",fnid);
			de.update();
			
			de.clearSql();
  			de.addSql("insert into odssu.role_function_manual (roleno, functionid) values (:roleno, :fnid) ");
			de.setString("roleno",roleno);
			de.setString("fnid",fnid);
			de.update();
			
			LanePromptUtil.promptToTrace("【"+(i+1)+"】导入功能任务【"+ fnlabel + "】的类别记账成功,类别为【"+fntype+"】.");
		}
		//删除功能任务对应的角色类型与岗位的角色类型不匹配的流程任务
		de.clearSql();
  		de.addSql("delete from odssu.role_function_manual a ");
  		de.addSql(" where not exists (select 1 ");
  		de.addSql("          from odssu.fn_roletype b, odssu.roleinfor c ");
  		de.addSql("         where a.roleno = c.roleno ");
  		de.addSql("           and c.roletype = b.roletypeno ");
  		de.addSql("           and a.functionid = b.functionid) ");
		int roletypenum = de.update();
		logBF.append("由于功能任务适用的岗位类型和导入岗位不符，删除【"+roletypenum+"】个功能任务！\r\n");
		//删除功能的业务范畴与岗位的业务范畴不匹配的流程任务
		de.clearSql();
  		de.addSql("delete from odssu.role_function_manual a ");
  		de.addSql(" where not exists ");
  		de.addSql(" (select 1 ");
  		de.addSql("          from odssu.fn_business_scope b, odssu.ir_role_business_scope c ");
  		de.addSql("         where a.functionid = b.functionid ");
  		de.addSql("           and a.roleno = c.roleno ");
  		de.addSql("           and b.scopeid = c.scopeno) ");
		int scopenum = de.update();
		logBF.append("由于功能任务的业务范畴和导入岗位不符，删除【"+scopenum+"】个功能任务！\r\n");
		
		logBF.append("批量导入功能任务 - 记账完成！\r\n");
		LanePromptUtil.promptToTrace("批量导入功能任务 - 记账完成！");
		LanePromptUtil.complete();
		DataObject result = DataObject.getInstance();
		result.put("fntypeimportlogstr", logBF.toString());
		result.put("returnfiletype", "txt");
		return result;
	}

	public DataObject importSingleFn(DataObject para) throws AppException, BusinessException {
		// 定义变量
		DE de = DE.getInstance();
		DataObject result = DataObject.getInstance();

		try{
			String functionid = para.getString("fnno");
			String functionname = para.getString("fnname");
			String pdid = para.getString("pdid");
			String fnfolderid = para.getString("folderid");
			String appid = para.getString("appid");
			DataStore ds_related_FnFolder = para.getDataStore("ds_related_FnFolder");

			// 进行数据校验
			if (functionid == null || functionid.equals("")) {
				this.bizException("functionid不能为空！");
			}
			if (functionname == null || functionid.equals("")) {
				this.bizException("functionname不能为空！");
			}
			if (ds_related_FnFolder == null) {
				this.bizException("缺少目录关系");
			}

			// 删除表中信息
			delTableInfo("odssu.appfunction", functionid);

			// 向数据库表中插入相关数据
			// 基本信息
			de.clearSql();
			de.addSql(" insert into odssu.appfunction (functionid,functionname,pdid,fnfolderid,appid)");
			de.addSql("  values (	 :functionid 	 ,  :functionname 	 ,:pdid	 ,  :fnfolderid,:appid)");
			de.setString("functionid", functionid);
			de.setString("functionname", functionname);
			de.setString("pdid", pdid);
			de.setString("fnfolderid", fnfolderid);
			de.setString("appid", appid);
			de.update();

			// 修改目录联系
			if (ds_related_FnFolder.rowCount() != 0) {
				// 删除当前目录中对应的链信息
				DataStore vps = DataStore.getInstance();
				de.clearSql();
				de.addSql(" delete from odssu.fn_folder where fnfolderid = :folderid ");
				for (int i = 0; i < ds_related_FnFolder.rowCount(); i++) {
					String folderid = ds_related_FnFolder.getString(i, "fnfolderid");
					vps.put(i, "folderid", folderid);
				}
				de.batchUpdate(vps);

				// 向目录联系表中插入信息
				vps.clear();
				de.clearSql();
				de.addSql(" insert into odssu.fn_folder (fnfolderid,pfnfolderid,folderlabel)");
				de.addSql("  values (	 :fnfolderid 	 ,   :pfnfolderid 	 ,:folderlabel  ) ");
				de.batchUpdate(ds_related_FnFolder);
			}

			// BP相关,直接退出
			if (pdid != null && !pdid.equals("")) {
				de.commit();
				result.put("importflag",true);
				result.put("msg","成功");
				return result;
			}

			DataStore ds_fn_bussiness_scope = para.getDataStore("ds_fn_bussiness_scope");
			DataStore ds_fn_roletype = para.getDataStore("ds_fn_roletype");
			DataStore ds_db_appFunction = para.getDataStore("ds_db_appFunction");

			if (ds_fn_roletype == null) {
				this.bizException("缺少角色关系");
			}
			if (ds_db_appFunction == null) {
				this.bizException("缺少DBID关系");
			}
			if (ds_fn_bussiness_scope == null) {
				this.bizException("缺少业务范畴关系");
			}

			//清空历史数据
			delTableInfo("odssu.fn_business_scope", functionid);
			delTableInfo("odssu.fn_roletype", functionid);
			delTableInfo("odssu.db_appfunction", functionid);

			//DBID关系
			if (ds_db_appFunction != null && ds_db_appFunction.rowCount() != 0) {
				de.clearSql();
				de.addSql(" insert into odssu.db_appfunction (functionid,dbid)");
				de.addSql("  values (	 :functionid 	 ,   :dbid 	 )");
				de.setString("functionid",functionid);
				de.batchUpdate(ds_db_appFunction);
			}

			// 业务范畴联系
			if (ds_fn_bussiness_scope.rowCount() != 0) {
				// 向功能与业务范畴表中插入信息
				de.clearSql();
				de.addSql(" insert into odssu.fn_business_scope (functionid,scopeid)");
				de.addSql("  values (	 :functionid 	 ,   :scopeno 	 )");
				de.setString("functionid",functionid);
				de.batchUpdate(ds_fn_bussiness_scope);
			}
			// 角色类型联系
			if (ds_fn_roletype.rowCount() != 0) {
				// 向功能与角色类型表中插入信息
				de.clearSql();
				de.addSql(" insert into odssu.fn_roletype (functionid,roletypeno)");
				de.addSql("  values (	 :functionid 	 ,   :roletypeid 	 )");
				de.setString("functionid",functionid);
				de.batchUpdate(ds_fn_roletype);
			}

			de.commit();
			result.put("importflag",true);
			result.put("msg","成功");
			return result;
		}catch(Exception e){

			e.printStackTrace();

			de.rollback();

			result.put("importflag",false);
			result.put("msg",e.getMessage().replaceAll("'",""));
			return result;
		}
	}

	private void delTableInfo(String tablename, String functionid) throws AppException {

		if (functionid == null || functionid.equals("")) {
			return;
		}

		DE de = DE.getInstance();
		// 删除appfunction表中数据
		de.clearSql();
		de.addSql(" delete from " + tablename);			//lzpmark
		de.addSql(" where functionid = :functionid");
		de.setString("functionid", functionid);
		de.update();

	}
	
}