package com.dw.odssu.dataimp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import jxl.Sheet;
import jxl.Workbook;
import jxl.write.WritableWorkbook;

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
import com.dareway.framework.exception.ASOException;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.taglib.lanePrompt.LanePromptUtil;
import com.dareway.framework.taglib.sprompt.SPrompt;
import com.dareway.framework.taglib.sprompt.SPromptUtil;
import com.dareway.framework.util.CurrentUser;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.util.DateUtil;
import com.dareway.framework.util.ExcelTool;
import com.dareway.framework.workFlow.ASO;
import com.dareway.framework.workFlow.BPO;
import com.dw.odssu.acc.org.jgjbxxxz.aso.OrgAddASO;
import com.dw.odssu.acc.org.streetcombine.aso.OrgCombineASO;
import com.dw.util.FileUtil;
import com.dw.util.OdssuUtil;
import com.dareway.framework.dbengine.DE;

/**
 * 类描述：批量功能 - 批量导入街道数据
 * @author 能天宇
 * @date 2016-10-20
 */
public class StreetImpBPO extends BPO {
	ASO orgaddASO = this.newASO(OrgAddASO.class);
	ASO streetcombineASO = this.newASO(OrgCombineASO.class);
	/**
	 * 
	 * @Description:校验核三临时表数据 
	 * @author 能天宇
	 * @date 2016-10-21
	 */
	public DataObject checkTempStreetData(DataObject para) throws AppException{
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		String orgno = para.getString("orgno");

		//校验街道名称是否与ods中机构标识名称冲突，自身名称重复
		de.clearSql();
  		de.addSql("select jd.jdid, jd.jdmc, jd.jbjgid, org.orgname ");
  		de.addSql("  from odssu.orginfor org right outer join ");
  		de.addSql("       (select a.jdid, a.jdmc, c.jbjgid ");
  		de.addSql("          from odssu.temp_be3_street_natl a, ");
  		de.addSql("               odssu.orginfor             b, ");
  		de.addSql("               odssu.temp_be3_inst_config c ");
  		de.addSql("         where a.jdid = c.jgid ");
  		de.addSql("           and a.jdmc = b.orgname ");
  		de.addSql("        union ");
  		de.addSql("        select d.jdid, d.jdmc, f.jbjgid ");
  		de.addSql("          from odssu.temp_be3_street_natl d left outer join odssu.temp_be3_inst_config f ");
  		de.addSql("         		 on d.jdid = f.jgid ");
  		de.addSql("           where d.jdmc in (select e.jdmc ");
  		de.addSql("                            from odssu.temp_be3_street_natl e ");
  		de.addSql("                           group by e.jdmc ");
  		de.addSql("                          having count(e.jdmc) > 1)) jd ");
  		de.addSql(" 	on org.orgno = substr(jd.jbjgid, 0, 6) ");
  		de.addSql(" order by jd.jdmc ");

		DataStore jdmcds = de.query();
		if (jdmcds.rowCount() > 0) {
			vdo.put("msg", "badjdmc");
			vdo.put("ds", jdmcds);
			return vdo;
		}
		
		//校验是否有街道对应多个人社局
		de.clearSql();
  		de.addSql("select jd.jdid, jd.jdmc, jd.jbjgid, org.orgname ");
  		de.addSql("  from odssu.orginfor org right outer join ");
  		de.addSql("       (select b.jdid, b.jdmc, a.jbjgid ");
  		de.addSql("          from odssu.temp_be3_inst_config a right outer join odssu.temp_be3_street_natl b ");
  		de.addSql("         		 on a.jgid = b.jdid ");
  		de.addSql("           where b.jdid in (select b.jdid ");
  		de.addSql("                            from odssu.temp_be3_inst_config a right outer join ");
  		de.addSql("                                 odssu.temp_be3_street_natl b ");
  		de.addSql("                           on a.jgid = b.jdid ");
  		de.addSql("                           group by b.jdid ");
  		de.addSql("                          having count(substr(jbjgid, 0, 6)) > 1) ");
  		de.addSql("         order by b.jdid) jd ");
  		de.addSql(" 		on substr(jd.jbjgid, 0, 6) = org.orgno ");
  		de.addSql("  order by jd.jdmc ");

		DataStore muiltids = de.query();
		if ( muiltids.rowCount() > 0) {
			vdo.put("msg", "muiltibelongorg");
			vdo.put("ds", muiltids);
			return vdo;
		}
		//校验是否有街道没有对应的人社局或对应人社局在ODS不存在
		de.clearSql();
  		de.addSql("select b.jdid, b.jdbh, b.jdmc  ");
  		de.addSql("  from odssu.temp_be3_inst_config a right outer join odssu.temp_be3_street_natl b ");
  		de.addSql(" 		 on a.jgid = b.jdid ");
  		de.addSql("   where (a.jbjgid is null or ");
  		de.addSql("        substr(a.jbjgid, 0, 6) not in ");
  		de.addSql("        (select orgno ");
  		de.addSql("           from odssu.orginfor  ");
  		de.addSql("          where orgtype in ('HSDOMAIN_DSRSJ', 'HSDOMAIN_QXRSJ')) and belongorgno = :orgno ) ");
  		de.addSql("  order by b.jdid ");
		de.setString("orgno", orgno);

		DataStore noneds = de.query();
		if (noneds.rowCount() > 0) {
			de.clearSql();
  			de.addSql("select orgno value,displayname content from odssu.orginfor ");
  			de.addSql(" where orgtype in ('HSDOMAIN_DSRSJ','HSDOMAIN_QXRSJ') and sleepflag = '0' and belongorgno = :orgno ");
			de.setString("orgno",orgno);
			DataStore dscode = de.query();
			if (dscode == null || dscode.rowCount() == 0) {
				throw new AppException("无法从【odssu.orginfor】中获取人社局信息！");
			}
			vdo.put("msg", "nobelongorg");
			vdo.put("ds", noneds);
			vdo.put("codeds", dscode);
			return vdo;
		}
		
		vdo.put("msg", "临时表数据校验成功！");
		return vdo;
	}
	
	/**
	 * 
	 * @Description: 保存对修改街道名称的处理结果
	 * @author 能天宇
	 * @date 2016-10-24
	 */
	public DataObject saveReName(DataObject para) throws AppException, BusinessException{
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		DataStore gridVds = para.getDataStore("datagrid");	//前台Grid数据
		if(gridVds == null ){
			throw new AppException("提交更改时，获取grid数据失败！");
		}
		//校验是否仍有街道对应多个人社局
		for(int i = 0 ;i < gridVds.rowCount(); i++){
			String jdid = gridVds.getString(i, "jdid");
			String jdmc = gridVds.getString(i, "jdmc");
			if(jdmc == null || "".equals(jdmc)){
				throw new AppException("填写的街道编号为【"+jdid+"】的街道名称为空");
			}
			de.clearSql();
  			de.addSql(" select jdmc from odssu.temp_be3_street_natl where jdid = :jdid ");
			de.setString("jdid",jdid );
			DataStore jdmcds = de.query();
			if(jdmcds == null || jdmcds.rowCount() == 0){
				throw new AppException("读取临时表【odssu.temp_be3_street_natl】中街道编号为【"+jdid+"】的街道名称失败");
			}
			String oldjdmc  = jdmcds.getString(0, "jdmc");
			if(!jdmc.equals(oldjdmc)){
				de.clearSql();
  				de.addSql(" update  odssu.temp_be3_street_natl set jdmc = :jdmc ,comments = :comments where jdid = :jdid ");
				de.setString("jdmc",jdmc );
				de.setString("comments","街道名称冲突,将街道名称【"+oldjdmc+"】修改为【"+jdmc+"】." );
				de.setString("jdid",jdid );
				de.update();
			}
		}
		
		//校验 是否还有街道名称冲突
		de.clearSql();
  		de.addSql("select jd.jdid, jd.jdmc, jd.jbjgid, org.orgname  belongorgname");
  		de.addSql("  from odssu.orginfor org right outer join ");
  		de.addSql("       (select a.jdid, a.jdmc, c.jbjgid ");
  		de.addSql("          from odssu.temp_be3_street_natl a, ");
  		de.addSql("               odssu.orginfor             b, ");
  		de.addSql("               odssu.temp_be3_inst_config c ");
  		de.addSql("         where a.jdid = c.jgid ");
  		de.addSql("           and a.jdmc = b.orgname ");
  		de.addSql("        union ");
  		de.addSql("        select aa.jdid, aa.jdmc, ca.jbjgid ");
  		de.addSql("          from odssu.temp_be3_street_natl aa left outer join odssu.temp_be3_inst_config ca ");
  		de.addSql("         		 on aa.jdid = ca.jgid ");
  		de.addSql("          where aa.jdmc in (select da.jdmc ");
  		de.addSql("                            from odssu.temp_be3_street_natl da ");
  		de.addSql("                           group by da.jdmc ");
  		de.addSql("                          having count(da.jdmc) > 1)) jd ");
  		de.addSql(" 	on org.orgno = substr(jd.jbjgid, 0, 6) ");
  		de.addSql(" order by jd.jdmc ");

		DataStore jdmcds = de.query();
		
		vdo.put("msg", "提交更改成功！");
		vdo.put("ds", jdmcds);
		return vdo;
	}
	/**
	 * 
	 * @Description:保存对临时表错误数据的处理结果-删除多余的隶属关系
	 * @author 能天宇
	 * @throws BusinessException 
	 * @date 2016-10-21
	 */
	public DataObject saveDeleteRsj(DataObject para) throws AppException, BusinessException{
		DataObject vdo = DataObject.getInstance();
		DataStore gridVds = para.getDataStore("datagrid");	//前台Grid数据
		if(gridVds == null ){
			throw new AppException("提交更改时，获取grid数据失败！");
		}
		//校验是否仍有街道对应多个人社局
		for(int i = 0 ;i < gridVds.rowCount(); i++){
			String jdid = gridVds.getString(i, "jdid");
			if(gridVds.findAll(" jdid == "+jdid).rowCount() > 1){
				this.bizException("仍然有街道对应多个人社局，提交失败！");
			}
		}
		
		//得到临时表中原始的数据
		
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select jd.jdid, jd.jdmc, jd.jbjgid, org.orgname ");
  		de.addSql("  from odssu.orginfor org right outer join ");
  		de.addSql("       (select b.jdid, b.jdmc, a.jbjgid ");
  		de.addSql("          from odssu.temp_be3_inst_config a right outer join odssu.temp_be3_street_natl b ");
  		de.addSql("         		 on a.jgid = b.jdid ");
  		de.addSql("           and b.jdid in (select b.jdid ");
  		de.addSql("                            from odssu.temp_be3_inst_config a right outer join ");
  		de.addSql("                                 odssu.temp_be3_street_natl b ");
  		de.addSql("                           	   on a.jgid = b.jdid ");
  		de.addSql("                           group by b.jdid ");
  		de.addSql("                          having count(substr(jbjgid, 0, 6)) > 1) ");
  		de.addSql("         order by b.jdid) jd ");
  		de.addSql(" 		on substr(jd.jbjgid, 0, 6) = org.orgno ");
  		de.addSql("  order by jd.jdmc ");

		DataStore muiltiVds =de.query();

		//保存更改，将删除的隶属关系计入到数据库
		//要删除的就是    存在于muiltids中 却不存在于grid中的数据
		for(int i = 0 ;i < muiltiVds.rowCount(); i++){
			String jdid = muiltiVds.getString(i, "jdid");
			String jdmc = muiltiVds.getString(i, "jdmc");
			String jbjgid = muiltiVds.getString(i, "jbjgid");
			//锁表
			de.clearSql();
  			de.addSql("select * from  odssu.temp_be3_inst_config a where a.jbjgid = :jbjgid  and a.jgid = :jdid for update ");
			de.setString("jbjgid",jbjgid );
			de.setString("jdid",jdid );
			de.query();
			int index = gridVds.find(" jdid == "+jdid +" and jbjgid == "+jbjgid);
			if(index < 0){
				de.clearSql();
  				de.addSql("delete from  odssu.temp_be3_inst_config a where a.jbjgid = :jbjgid  and a.jgid = :jdid ");
				de.setString("jbjgid",jbjgid );
				de.setString("jdid",jdid );
				int rowSum = de.update();
				if(rowSum < 1){
					throw new AppException("删除街道【"+jdmc+"】对经办机构【"+jbjgid+"】的隶属关系时，操作失败！");
				}
				de.clearSql();
  				de.addSql(" update  odssu.temp_be3_street_natl set comments = :comments where jdid = :jdid ");
				de.setString("comments","街道属于多个人社局,故删除了部分隶属关系." );
				de.setString("jdid",jdid );
				de.update();
			}

		}
		vdo.put("msg", "提交更改成功！");
		return vdo;
	}
	/**
	 * 
	 * @Description:保存对无隶属人社局的处理结果  - 选择新的隶属人社局
	 * @author 能天宇
	 * @date 2016-10-21
	 */
	public DataObject saveChoseRsj(DataObject para) throws AppException, BusinessException{
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		DataStore gridVds = para.getDataStore("datagrid");	//前台Grid数据
		if(gridVds == null ){
			throw new AppException("提交更改时，获取grid数据失败！");
		}
		
		//保存更改，将选择的隶属人社局计入到数据库
		for(int i = 0 ;i < gridVds.rowCount(); i++){
			String jdid = gridVds.getString(i, "jdid");
			String belongorgno = gridVds.getString(i, "belongorgno");
			//锁表
			de.clearSql();
  			de.addSql(" select * from odssu.temp_be3_street_natl ");
  			de.addSql(" where jdid = :jdid for update  ");
			de.setString("jdid",jdid);
			DataStore tempds = de.query();
			if(tempds == null || tempds.rowCount() == 0){
				throw new AppException("无法获取街道编号为【"+jdid+"】的街道信息.");
			}
			
			if(belongorgno == null || "".equals(belongorgno)){
				//未选择人社局 ，设为无效街道
				de.clearSql();
  				de.addSql("update odssu.temp_be3_street_natl ");
  				de.addSql("   set isvalid = :isvalid, comments = :comments ");
  				de.addSql(" where jdid = :jdid ");
				de.setString("isvalid","0");
				de.setString("comments","此街道没有隶属人社局或原隶属人社局在ODS中不存在，故而成为无效街道，不向ODS中转入.");
				de.setString("jdid",jdid);
				de.update();
				
				continue;
			}
			
			if(isOriginalBelNotExistInODS(jdid)){  //1.原来的隶属 在现在的ods中不存在
				//更换隶属关系
				de.clearSql();
  				de.addSql("update odssu.temp_be3_inst_config ");
  				de.addSql("   set jbjgid = :belongorgno, ");
  				de.addSql("       jglx = 'C' ");
  				de.addSql(" where jdid = :jdid  ");
				de.setString("belongorgno",belongorgno);
				de.setString("jdid",jdid);
				de.update();
			}else{    								// 2.原来就没有隶属
				//插入隶属关系
				de.clearSql();
  				de.addSql("insert into odssu.temp_be3_inst_config ");
  				de.addSql("  (jbjgid, jglx, jgid) ");
  				de.addSql("values ");
  				de.addSql("  (:belongorgno, :para2, :jdid) ");
				de.setString("belongorgno",belongorgno);
				de.setString("para2","C");
				de.setString("jdid",jdid);
				de.update();
			}

			//更新街道临时表
			de.clearSql();
  			de.addSql("update odssu.temp_be3_street_natl ");
  			de.addSql("   set belongorgno = :belongorgno, isvalid = :isvalid, comments = :comments ");
  			de.addSql(" where jdid = :jdid ");
			de.setString("belongorgno",belongorgno);
			de.setString("isvalid","1");
			de.setString("comments","原本此街道没有合法的隶属人社局，故而给此街道人工选定了隶属人社局.");
			de.setString("jdid",jdid);
			de.update();
		}
		
		//为临时表中的所有 有效街道 更新隶属机构
		de.clearSql();
  		de.addSql("select a.jdid, substr(b.jbjgid, 0, 6) belongorgno ");
  		de.addSql("  from odssu.temp_be3_street_natl a, odssu.temp_be3_inst_config b ");
  		de.addSql(" where a.jdid = b.jgid ");
  		de.addSql("   and a.isvalid = '1' ");
		DataStore belongVds = de.query();
		if (belongVds == null || belongVds.rowCount() == 0) {
			throw new AppException("无法获取临时表【odssu.temp_be3_street_natl，odssu.temp_be3_inst_config】数据");
		}
		for (int i = 0; i < belongVds.rowCount();i++) {
			String jdid = belongVds.getString(i, "jdid");
			String belongorgno = belongVds.getString(i, "belongorgno");
			de.clearSql();
  			de.addSql("update odssu.temp_be3_street_natl a ");
  			de.addSql("   set belongorgno = :belongorgno");
  			de.addSql(" where a.jdid = :jdid ");
			de.setString("belongorgno", belongorgno);
			de.setString("jdid", jdid);
			de.update();
		}
		vdo.put("msg", "提交更改成功！可以导入街道");
		return vdo;
	}
	/**
	 * 
	 * @Description: 校验街道 （有对应人社局 但是对应人社局在ODS不存在）
	 * @author 能天宇
	 * @throws AppException 
	 * @date 2016-10-22
	 */
	private boolean isOriginalBelNotExistInODS(String jdid) throws AppException{
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select 1  ");
  		de.addSql("  from odssu.temp_be3_inst_config a right outer join odssu.temp_be3_street_natl b ");
  		de.addSql(" 		 on a.jgid = b.jdid ");
  		de.addSql("   where a.jbjgid is not null ");
  		de.addSql("   and b.jdid = :jdid ");
  		de.addSql("   and substr(a.jbjgid, 0, 6) not in ");
  		de.addSql("        (select orgno ");
  		de.addSql("           from odssu.orginfor ");
  		de.addSql("          where orgtype in ('HSDOMAIN_DSRSJ', 'HSDOMAIN_QXRSJ')) ");
		de.setString("jdid", jdid);


		DataStore vds = de.query();
		if ( vds == null || vds.rowCount() == 0) {
			return false;
		}else{
			return true;
		}
	}
	/**
	 * 
	 * @Description: 批量转入街道 - 记账
	 * @author 能天宇
	 * @date 2016-10-22
	 */
	public DataObject saveStreetDataImp(DataObject para) throws AppException{
		DE de = DE.getInstance();
		StringBuffer logBF = new StringBuffer();
		
		//操作对象 - 街道临时表中所有的有效街道
		de.clearSql();
  		de.addSql("select * from odssu.temp_be3_street_natl where isvalid = '1' ");
		DataStore streetds = de.query();
		if (streetds ==null || streetds.rowCount() == 0) {
			throw new AppException("获取临时表【odssu.temp_be3_street_natl】数据失败！");
		}
		
		LanePromptUtil.promptToTrace("开始转入街道数据,共【"+streetds.rowCount()+"】个:");
		logBF.append("开始转入街道数据,共【"+streetds.rowCount()+"】个:\r\n");
		for (int i = 0; i < streetds.rowCount();i++) {
			String jdid = streetds.getString(i, "jdid");
			String jdmc = streetds.getString(i, "jdmc");
			String belongorgno = streetds.getString(i, "belongorgno");
			String orgno ;
			orgno = ""+belongorgno + de.getNextVal("odssu.seq_sbs");
			//1.生成orgno
			while(OdssuUtil.isOrgExist(orgno)){
				orgno = ""+belongorgno + de.getNextVal("odssu.seq_sbs");
			}
			String orgname = jdmc;
			//2.更新临时表
			de.clearSql();
  			de.addSql("update odssu.temp_be3_street_natl set orgno = :orgno, orgname = :orgname where jdid = :jdid ");
			de.setString("orgno",orgno);
			de.setString("orgname",orgname);
			de.setString("jdid",jdid);
			de.update();
			
			//3.导入机构(街道)
			DataObject streePara = DataObject.getInstance();
			String userid = this.getUser().getUserid();
			Date today = DateUtil.getCurrentDate();
			String nyr = DateUtil.FormatDate(today, "yyyyMMdd");
			streePara.put("pjbh", "F" + nyr);
			streePara.put("orgno", orgno);
			streePara.put("orgname", orgname);
			streePara.put("displayname", orgname);
			streePara.put("fullname", orgname);
			streePara.put("belongorgno", belongorgno);
			streePara.put("orgtype", OdssuContants.ORGTYPE_SBS);
			streePara.put("_user", CurrentUser.getInstance());
			streePara.put("pdid", "F" + nyr);
			streePara.put("objectid", "cc");
			streePara.put("userid", userid);
				//调用新增机构ASO
			try {
				orgaddASO.doEntry(streePara);		
			} catch (ASOException e) {
				e.printStackTrace();
				throw new AppException("转入街道【"+orgname+"】执行ASO记账时出错！"+e.getMessage());
			}
			//4.生成对照表

			de.clearSql();
			de.addSql("select 1 from odssu.jd_dzb where  yjdid = :jdid ");
			de.setString("jdid", jdid);
			DataStore dzbVds = de.query();
			if(dzbVds == null || dzbVds.rowCount() == 0){
				de.clearSql();
  				de.addSql(" insert into odssu.jd_dzb (yjdid,xjdid) values(:jdid,:orgno) ");
				de.setString("jdid",jdid);
				de.setString("orgno",orgno);
				de.update();
			}else{
				de.clearSql();
  				de.addSql(" update odssu.hbjd_dzb set xjdid = :orgno where yjdid = :jdid ");
				de.setString("orgno",orgno);
				de.setString("jdid",jdid);
				de.update();
			}
			LanePromptUtil.promptToTrace("【"+(i+1)+"】转入了街道【"+jdmc+"】,新街道编号为【"+orgno+"】");
			logBF.append("【"+(i+1)+"】转入了街道【"+jdmc+"】,新街道编号为【"+orgno+"】\r\n");
		}
		LanePromptUtil.promptToTrace("批量转入街道完成！");
		logBF.append("批量转入街道完成！");
		LanePromptUtil.complete();
		DataObject vdo = DataObject.getInstance();
		vdo.put("msg", "街道数据转入完成！");
		vdo.put("logstr", logBF.toString());
		return vdo;
	}
	/**
	 * 
	 * @Description: 街道数据来自哪个人社系统
	 * @author 能天宇
	 * @date 2016-10-22
	 */
	public DataObject queryRsxt(DataObject para) throws AppException, BusinessException{
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select distinct b.orgno  ");
  		de.addSql("  from odssu.temp_be3_inst_config a, odssu.orginfor b  ");
  		de.addSql(" where substr(a.jbjgid, 0, 4) = b.orgno ");
		DataStore rsxtds = de.query();
		if (rsxtds == null || rsxtds.rowCount() == 0) {
			throw new AppException("临时表【odssu.temp_be3_street_natl】中的街道所属的人社系统不存在！");
		}else if(rsxtds.rowCount() > 1){
			throw new AppException("临时表【odssu.temp_be3_street_natl】中的街道来自不同的人社系统！");
		}
		vdo.put("ds", rsxtds);
		vdo.put("orgno", rsxtds.getString(0, "orgno"));
		return vdo;
	}
	/**
	 * 
	 * @Description: 生成合并街道统计模板
	 * @author 能天宇
	 * @date 2016-10-22
	 */
	public DataObject createFileForCombineStreet(DataObject para) throws Exception{
		DE de = DE.getInstance();
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
		SPrompt prompt = SPromptUtil.getSPrompt();
		DataStore rsjVds = getRsjInfoDs(rsxtorgno);
		prompt.setTotalSteps(rsjVds.rowCount());
		
		//为每个区县人社局生成一个自己的模板文件
		for (int i = 0; i < rsjVds.rowCount(); i++) {
			DataObject rsjInfo = rsjVds.getRow(i);
			String orgno   = rsjInfo.getString("orgno");
			String orgname = rsjInfo.getString("orgname");
			String qxmc = orgname.replace("人社局", "");
			prompt.prompt("【"+(i+1)+"】开始生成 【"+qxmc+"】的合并街道统计模板."); // 向PROMPT区输出提示信息

			//获取街道信息数据

			de.clearSql();
  			de.addSql("select :qxmc qxmc,a.jdid, a.jdmc, a.orgno, a.orgname");
  			de.addSql("  from odssu.temp_be3_street_natl a ");
  			de.addSql(" where a.belongorgno = :orgno ");
  			de.addSql("   and a.isvalid = '1' ");
  			de.setString("qxmc", qxmc);
			de.setString("orgno", orgno);
			DataStore streetds = de.query();
			
			//组装Excel
			DataObject dataobj = DataObject.getInstance();
			dataobj.put("streetds", streetds);
			dataobj.put("qxmc", qxmc);
			DataObject result = createSingleExcel(dataobj);
			
			// 获取HSSFWorkbook的编码串
			HSSFWorkbook wb = (HSSFWorkbook) result.get("wb");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			wb.write(out);
			WritableWorkbook workbook = ExcelTool.createWorkbook(out);
			ExcelTool.writeWbootAndClose(workbook);
			ExcelTool.closeOutputStream(out);
			String fileName = qxmc + "_街道合并统计表" + ".xls";
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
	 * @Description: 获取人社系统下的所有人社局
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
  		de.addSql("    and  orgtype in ('HSDOMAIN_DSRSJ','HSDOMAIN_QXRSJ')");
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
	 * @Description: 生成单个的Excel模板文件
	 * @author 能天宇
	 * @date 2016-10-22
	 */
	private DataObject createSingleExcel(DataObject para) throws IOException, AppException {

		DataStore streetds = para.getDataStore("streetds");
		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 1.生成表格 -街道合并统计表
		HSSFSheet sheet = workbook.createSheet("街道合并统计表");
		sheet.setColumnWidth(0, 3000);
		// 样式
		sheet.setDefaultColumnWidth(20);
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
		firstrow.put(columnNoOfFirstRow++ , "name", "区县名称");
		firstrow.put(columnNoOfFirstRow++, "name", "核三系统街道编号");
		firstrow.put(columnNoOfFirstRow++, "name", "核三系统街道名称");
		firstrow.put(columnNoOfFirstRow++, "name", "对应新系统街道编号");
		firstrow.put(columnNoOfFirstRow++, "name", "对应新系统街道名称");
		firstrow.put(columnNoOfFirstRow++, "name", "处理建议");
		// 产生表格标题行
		HSSFRow row = sheet.createRow(0);
		
		for (int i = 0; i < firstrow.rowCount(); i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(firstrow.getString(i, "name"));
			cell.setCellValue(text);
		}
		
		// 产生表格数据行
		for(int j=0; j < streetds.rowCount();j++){
			HSSFRow rowtemp = sheet.createRow(j+1);
			HSSFCell cell = rowtemp.createCell(0);
			HSSFRichTextString text = new HSSFRichTextString(streetds.getString(j, "qxmc"));
			cell.setCellValue(text);
			HSSFCell cell1 = rowtemp.createCell(1);
			HSSFRichTextString text1 = new HSSFRichTextString(streetds.getString(j, "jdid"));
			cell1.setCellValue(text1);
			HSSFCell cell2 = rowtemp.createCell(2);
			HSSFRichTextString text2 = new HSSFRichTextString(streetds.getString(j, "jdmc"));
			cell2.setCellValue(text2);
			HSSFCell cell3 = rowtemp.createCell(3);
			HSSFRichTextString text3 = new HSSFRichTextString(streetds.getString(j, "orgno"));
			cell3.setCellValue(text3);
			HSSFCell cell4 = rowtemp.createCell(4);
			HSSFRichTextString text4 = new HSSFRichTextString(streetds.getString(j, "orgname"));
			cell4.setCellValue(text4);
			HSSFCell cell5 = rowtemp.createCell(5);
			HSSFRichTextString text5 = new HSSFRichTextString("建议保留");
			cell5.setCellValue(text5);
		}
		
		// 2.生成一张工作表-可选新街道
		HSSFSheet sheetnew = workbook.createSheet("新系统街道参考");
		sheetnew.setDefaultColumnWidth(20);

		// 产生表格标题行
		DataStore newtitle = DataStore.getInstance();
		newtitle.put(0, "name", "区县名称");
		newtitle.put(1, "name", "新系统街道编号");
		newtitle.put(2, "name", "新系统街道名称");
		
		// 产生表格标题行
		HSSFRow titlerow = sheetnew.createRow(0);
		
		for (int j = 0; j < newtitle.rowCount(); j++) {
			HSSFCell cell = titlerow.createCell(j);
			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(newtitle.getString(j, "name"));
			cell.setCellValue(text);
		}
		
		for(int j=0; j < streetds.rowCount();j++){
			// 产生数据事例行
			HSSFRow rowtemp = sheetnew.createRow(j+1);
			
			HSSFCell cell = rowtemp.createCell(0);
			HSSFRichTextString text = new HSSFRichTextString(streetds.getString(j, "qxmc"));
			cell.setCellValue(text);
			HSSFCell cell1 = rowtemp.createCell(1);
			HSSFRichTextString text1 = new HSSFRichTextString(streetds.getString(j, "orgno"));
			cell1.setCellValue(text1);
			HSSFCell cell2 = rowtemp.createCell(2);
			HSSFRichTextString text2 = new HSSFRichTextString(streetds.getString(j, "orgname"));
			cell2.setCellValue(text2);
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("wb", workbook);
		return vdo;
	}
	/**
	 * 
	 * @Description: 解析合并街道excel到ds
	 * @author 能天宇
	 * @date 2016-10-24
	 */
	public DataObject analyzeExcelToDsStreet(DataObject para) throws Exception {
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
		setTableInfo(tableInfo, vi++, "qxmc", "区县名称", "String");
		setTableInfo(tableInfo, vi++, "jdid", "核三系统街道编号", "String");
		setTableInfo(tableInfo, vi++, "jdmc", "核三系统街道名称", "String");
		setTableInfo(tableInfo, vi++, "orgno", "对应新系统街道编号", "String");
		setTableInfo(tableInfo, vi++, "orgname", "对应新系统街道名称", "String");
		setTableInfo(tableInfo, vi++, "cljy", "处理建议", "String");

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
			if(dsExcel.getString(i, "qxmc") != null)
				dsExcel.put(i, "qxmc", dsExcel.getString(i, "qxmc").trim());
			if(dsExcel.getString(i, "jdid") != null)
				dsExcel.put(i, "jdid", dsExcel.getString(i, "jdid").trim());
			if(dsExcel.getString(i, "jdmc") != null)
				dsExcel.put(i, "jdmc", dsExcel.getString(i, "jdmc").trim());
			if(dsExcel.getString(i, "orgno") != null)
				dsExcel.put(i, "orgno", dsExcel.getString(i, "orgno").trim());
			if(dsExcel.getString(i, "orgname") != null)
				dsExcel.put(i, "orgname", dsExcel.getString(i, "orgname").trim());
			if(dsExcel.getString(i, "cljy") != null)
				dsExcel.put(i, "cljy", dsExcel.getString(i, "cljy").trim());
		}
		DataObject result = DataObject.getInstance();
		result.put("streetds", dsExcel);
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
	 * @Description: 校验合并街道
	 * @author 能天宇
	 * @date 2016-10-24
	 */
	public DataObject checkCombineStreet(DataObject para) throws AppException, BusinessException{
		StringBuffer logBF = new StringBuffer();
		String rsxtorgno = para.getString("orgno");
		DE de = DE.getInstance();

		
		// 获取文件数据fileds
		DataObject dataobj = para.getDataObject("dataobj");
		if(dataobj == null || !dataobj.containsKey("streetds")){
			this.bizException("没有正确读入文件!");
		}
		DataStore fileds = dataobj.getDataStore("streetds");
		if(fileds == null || fileds.rowCount() == 0){
			this.bizException("没有街道可以合并。");
		}
		
		LanePromptUtil.promptToTrace("即将开始校验合并街道：共【"+fileds.rowCount()+"】个.");
		logBF.append(">>>即将开始校验合并街道：共【"+fileds.rowCount()+"】个.\r\n");
		boolean checkflag = true;
		for (int i = 0; i < fileds.rowCount(); i++) {
			DataObject row = fileds.getRow(i);
			row.put("jycw", "");
			
			String jdmc = row.getString("jdmc");
			String jdid = row.getString("jdid");
			LanePromptUtil.promptToTrace("【"+(i+1)+"】:校验街道【"+jdmc+"】的相关信息");
			logBF.append("【"+(i+1)+"】:校验街道【"+jdmc+"】的相关信息\r\n");

			//检查是否有空值
			if(isRowHaveEmptyColumn(row, logBF) == true){
				checkflag = false;
				continue;
			}
			//先简单检查一次填写项目的存在性和正确性
			if(isRowHaveErrorColumn(fileds,rsxtorgno,row,logBF) == true){
				checkflag = false;
				continue;
			}

			DataStore tempds = fileds.findAll(" jdid == " + jdid+ " and jdmc == " +jdmc );
			int beginrowno = 0;
			//检查重复行
	        if( tempds.rowCount() > 1){
	        	StringBuffer rownoList =new StringBuffer();
	        	beginrowno  = fileds.find(" jdid == " + jdid+ " and jdmc == " +jdmc,beginrowno);
        		rownoList.append(""+(beginrowno+2));
	        	while(beginrowno > -1){
	        		beginrowno  = fileds.find(" jdid == " + jdid+ " and jdmc == " +jdmc,beginrowno+1);
	        		if(beginrowno > -1)
	        			rownoList.append("、"+(beginrowno+2));
	        	}
	        	checkflag = false;
	        	logBF.append("校验错误：【"+rownoList+"】行核三街道重复设置.\r\n");
	        	row.put("jycw", "【"+rownoList+"】行核三街道重复设置.");
				LanePromptUtil.promptToTrace("校验错误：【"+rownoList+"】行核三街道重复设置.");
	        }
	        //检查是否已经合并过

	        de.clearSql();
  	        de.addSql(" select xjdid from odssu.hbjd_dzb where yjdid = :jdid ");
	        de.setString("jdid",jdid);
	        DataStore dzbds = de.query();
	        if( dzbds.rowCount()>0 ){
	        	String xjdid= dzbds.getString(0, "xjdid");
	        	checkflag = false;
	        	logBF.append("校验错误：此街道已经合并过了，新街道编号为【"+xjdid+"】.\r\n");
	        	row.put("jycw", "此街道已经合并过了，新街道编号为【"+xjdid+"】.");
				LanePromptUtil.promptToTrace("校验错误：此街道已经合并过了，新街道编号为【"+xjdid+"】.");
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
			LanePromptUtil.promptToTrace("合并街道的信息校验成功.");
			logBF.append(">>>合并街道的信息校验成功.\r\n");
			LanePromptUtil.complete();
			DataObject result = DataObject.getInstance();
			result.put("logstr", logBF.toString());
			result.put("returnfiletype", "txt");
			return result;
		}
	}
	/**
	 * 
	 * @Description: 检查ds每行是否有不允许的空值
	 * @author 能天宇
	 * @date 2016-10-17
	 */
	private boolean isRowHaveEmptyColumn(DataObject row ,StringBuffer logBF) throws AppException{
		StringBuffer jycw = new StringBuffer();
		String qxmc = row.getString("qxmc");
		String jdid = row.getString("jdid");
		String jdmc = row.getString("jdmc");
		String orgno = row.getString("orgno");
		String orgname = row.getString("orgname");

		if(qxmc == null || "".equals(qxmc))
			jycw.append("区县名称为空.");
		if(jdid == null || "".equals(jdid))
			jycw.append("核三街道编号为空.");
		if(jdmc == null || "".equals(jdmc))
			jycw.append("核三街道名称为空.");
		if(orgno == null || "".equals(orgno))
			jycw.append("新街道编号为空.");
		if(orgname == null  || "".equals(orgname))
			jycw.append("新街道名称为空.");

		if(jycw.length() != 0  ){
			row.put("jycw", jycw.toString());
			logBF.append("校验错误:"+jycw+"\r\n");
			LanePromptUtil.promptToTrace("校验错误:"+jycw);
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
	private boolean isRowHaveErrorColumn(DataStore streetds,String rsxtorgno,DataObject row,StringBuffer logBF) throws AppException{
		DE de = DE.getInstance();
		StringBuffer jycw = new StringBuffer();
		String qxmc = row.getString("qxmc");
		String jdid = row.getString("jdid");
		String jdmc = row.getString("jdmc");
		String orgno = row.getString("orgno");
		String orgname = row.getString("orgname");
		String belongorgno ="";
		
		if(!OdssuUtil.isRsxt(rsxtorgno)){
			throw new AppException("获取人社系统编号失败！");
		}
		de.clearSql();
  		de.addSql("select orgno from odssu.orginfor a  where a.orgname like :orgname ");
  		de.addSql(" and orgtype in ('HSDOMAIN_DSRSJ', 'HSDOMAIN_QXRSJ') and belongorgno = :rsxtorgno  ");
		de.setString("orgname", qxmc +"%");
		de.setString("rsxtorgno", rsxtorgno);
		DataStore belongds = de.query();
		boolean qxIsValid = false;
		if (belongds == null || belongds.rowCount() == 0) {
			jycw.append("区县名称【"+qxmc+"】在ods中不存在！");
		}else if(belongds.rowCount() >1){
			jycw.append("区县名称【"+qxmc+"】不够精确或有重名，无法确认街道所属区县！");
		}else{
			belongorgno = belongds.getString(0, "orgno");
			qxIsValid = true;
		}
		
		de.clearSql();
  		de.addSql("select jdmc, belongorgno, isvalid ");
  		de.addSql("  from odssu.temp_be3_street_natl ");
  		de.addSql(" where jdid = :jdid ");
		de.setString("jdid",jdid);
		DataStore jdVds = de.query();
		if (jdVds == null || jdVds.rowCount() == 0) {
			jycw.append("核三街道编号【"+jdid+"】不存在.");
		}else{
			String dsbelongorgno = jdVds.getString(0, "belongorgno");
			String dsjdmc = jdVds.getString(0, "jdmc");
			String dsisvalid = jdVds.getString(0, "isvalid");
			
			if(!jdmc.equals(dsjdmc)){
				jycw.append("核三街道【"+jdmc+"】名称与编号不匹配.");
			}
			if("0".equals(dsisvalid)){
				jycw.append("核三街道【"+jdid+"】是无效街道.");
			}
			if(qxIsValid == true && !belongorgno.equals(dsbelongorgno)){
				jycw.append("核三街道【"+jdid+"】不属于区县【"+qxmc+"】.");
			}
		}
		
		de.clearSql();
  		de.addSql("select orgno, orgname,belongorgno ");
  		de.addSql("  from odssu.temp_be3_street_natl ");
  		de.addSql(" where orgno = :orgno ");
		de.setString("orgno",orgno);
		DataStore newjdVds = de.query();
		if (newjdVds == null || newjdVds.rowCount() == 0) {
			jycw.append("对应的新街道编号【"+orgno+"】在临时表中不存在.");
		}else  if(!orgname.equals(newjdVds.getString(0, "orgname"))){
			jycw.append("对应的新街道【"+orgname+"】名称与编号不匹配.");
		}else if(qxIsValid == true && !belongorgno.equals(newjdVds.getString(0, "belongorgno"))){
			jycw.append("对应的新街道【"+orgname+"】不属于区县【"+qxmc+"】.");
		}
		
		if(jycw.length() != 0 ){
			row.put("jycw", jycw.toString());
			logBF.append("校验错误:"+jycw+"\r\n");
			LanePromptUtil.promptToTrace("校验错误:"+jycw);
			return true;
		}
		return false;
	}
	
	/**
	 * @Description:合并街道 - 校验不通过时组装要返回的excel 
	 * @author 能天宇
	 * @date 2016-10-24
	 */
	public DataObject returnFileForCombineStreet(DataObject para) throws IOException, AppException {
		DataStore streetds = para.getDataStore("excelds");
		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 1.生成表格 -街道合并统计表
		HSSFSheet sheet = workbook.createSheet("街道合并统计表");
		sheet.setColumnWidth(0, 3000);
		// 样式
		sheet.setDefaultColumnWidth(20);
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
		fontred.setFontHeightInPoints((short) 10);
		fontred.setBold(true);
		HSSFCellStyle styleRed = workbook.createCellStyle();
		styleRed.setFont(fontred);
		// 把字体应用到当前的样式
		style.setFont(font);
		
		// 准备表格表头
		DataStore firstrow = DataStore.getInstance();
		int columnNoOfFirstRow = 0 ;
		firstrow.put(columnNoOfFirstRow++ ,"name", "区县名称");
		firstrow.put(columnNoOfFirstRow++, "name", "核三系统街道编号");
		firstrow.put(columnNoOfFirstRow++, "name", "核三系统街道名称");
		firstrow.put(columnNoOfFirstRow++, "name", "对应新系统街道编号");
		firstrow.put(columnNoOfFirstRow++, "name", "对应新系统街道名称");
		firstrow.put(columnNoOfFirstRow++, "name", "处理建议");
		firstrow.put(columnNoOfFirstRow++, "name", "校验错误");
		// 产生表格标题行
		HSSFRow row = sheet.createRow(0);
		for (int i = 0; i < firstrow.rowCount(); i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(firstrow.getString(i, "name"));
			cell.setCellValue(text);
		}
		// 产生表格数据行
		for(int j=0; j < streetds.rowCount();j++){
			HSSFRow rowtemp = sheet.createRow(j+1);
			HSSFCell cell = rowtemp.createCell(0);
			HSSFRichTextString text = new HSSFRichTextString(streetds.getString(j, "qxmc"));
			cell.setCellValue(text);
			HSSFCell cell1 = rowtemp.createCell(1);
			HSSFRichTextString text1 = new HSSFRichTextString(streetds.getString(j, "jdid"));
			cell1.setCellValue(text1);
			HSSFCell cell2 = rowtemp.createCell(2);
			HSSFRichTextString text2 = new HSSFRichTextString(streetds.getString(j, "jdmc"));
			cell2.setCellValue(text2);
			HSSFCell cell3 = rowtemp.createCell(3);
			HSSFRichTextString text3 = new HSSFRichTextString(streetds.getString(j, "orgno"));
			cell3.setCellValue(text3);
			HSSFCell cell4 = rowtemp.createCell(4);
			HSSFRichTextString text4 = new HSSFRichTextString(streetds.getString(j, "orgname"));
			cell4.setCellValue(text4);
			HSSFCell cell5 = rowtemp.createCell(5);
			HSSFRichTextString text5 = new HSSFRichTextString(streetds.getString(j, "cljy"));
			cell5.setCellValue(text5);
			HSSFCell cell6 = rowtemp.createCell(6);
			//cell6.setCellStyle(styleRed);
			HSSFRichTextString text6 = new HSSFRichTextString(streetds.getString(j, "jycw"));
			cell6.setCellValue(text6);
			if(streetds.getString(j, "jycw") != null && !"".equals(streetds.getString(j, "jycw"))){
				cell.setCellStyle(styleRed);
				cell1.setCellStyle(styleRed);
				cell2.setCellStyle(styleRed);
				cell3.setCellStyle(styleRed);
				cell4.setCellStyle(styleRed);
				cell5.setCellStyle(styleRed);
				cell6.setCellStyle(styleRed);
			}
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("wb", workbook);
		return vdo;
	}
	/**
	 * 
	 * @Description: 合并街道 -记账
	 * @author 能天宇
	 * @date 2016-10-25
	 */
	public DataObject saveCombineStreet(DataObject para) throws AppException, BusinessException{
		DE de = DE.getInstance();
		StringBuffer logBF = new StringBuffer();

		// 获取文件数据streetds
		DataObject dataobj = para.getDataObject("dataobj");
		if(dataobj == null || !dataobj.containsKey("streetds")){
			this.bizException("没有正确读入文件!");
		}
		DataStore streetds = dataobj.getDataStore("streetds");
		if(streetds == null || streetds.rowCount() == 0){
			this.bizException("没有街道可以合并。");
		}
		//获取临时表数据
		de.clearSql();
  		de.addSql("select * from odssu.temp_be3_street_natl where isvalid = '1' ");
		DataStore streetNatlds = de.query();
		if (streetNatlds ==null || streetNatlds.rowCount() == 0) {
			throw new AppException("获取临时表【odssu.temp_be3_street_natl】数据失败！");
		}
				
		LanePromptUtil.promptToTrace("开始合并街道数据,共【"+streetds.rowCount()+"】个:");
		logBF.append("开始合并街道,共【"+streetds.rowCount()+"】个:\r\n");
		for (int i = 0; i < streetds.rowCount();i++) {
			String jdid = streetds.getString(i, "jdid");
			String jdmc = streetds.getString(i, "jdmc");
			String neworgno = streetds.getString(i, "orgno");
			String neworgname = streetds.getString(i, "orgname");
			DataStore natl = streetNatlds.findAll(" jdid == "+jdid);
			if(natl == null || natl.rowCount() == 0){
				throw new AppException("获取街道【"+jdmc+"】信息失败");
			}
			String belongorgno  =natl.getString(0, "belongorgno");
			
			//1.更新临时表
			String comments = "";
			if(neworgno.equals(natl.getString(0,"orgno")) ){
				comments = "合并街道时，保留.";
			}else{		//暂时不考虑改新街道名称
				comments = "合并街道时，被合并为"+neworgname+".";
			}
			de.clearSql();
  			de.addSql("update odssu.temp_be3_street_natl set neworgno = :neworgno, neworgname = :neworgname,comments = :comments  where jdid = :jdid ");
			de.setString("neworgno",neworgno);
			de.setString("neworgname",neworgname);
			de.setString("comments",comments);
			de.setString("jdid",jdid);
			de.update();
			
			
			//2.如果已经存在，说明这个街道是多个合并成的，一个orgno只调用一次ASO
			de.clearSql();
  			de.addSql("select 1 from odssu.temp_orginfor  where orgno = :neworgno ");
			de.setString("neworgno",neworgno);
			DataStore existds = de.query();
			if(existds == null || existds.rowCount() == 0){
				// 调用街道合并ASO(仿OrgAddASO)生成的机构在odssu.temp_orginfor 中
				//这种增加不会计算odssu.ir_org_closure的闭包关系
				DataObject streePara = DataObject.getInstance();
				String userid = this.getUser().getUserid();
				Date today = DateUtil.getCurrentDate();
				String nyr = DateUtil.FormatDate(today, "yyyyMMdd");
				streePara.put("pjbh", "F" + nyr);
				streePara.put("orgno", neworgno);
				streePara.put("orgname", neworgname);
				streePara.put("displayname", neworgname);
				streePara.put("fullname", neworgname);
				streePara.put("belongorgno", belongorgno);
				streePara.put("orgtype", OdssuContants.ORGTYPE_SBS);
				streePara.put("_user", CurrentUser.getInstance());
				streePara.put("pdid", "F" + nyr);
				streePara.put("objectid", "cc");
				streePara.put("userid", userid);
				try {
					streetcombineASO.doEntry(streePara);		
				} catch (ASOException e) {
					e.printStackTrace();
					throw new AppException("合并街道【"+neworgname+"】执行ASO记账时出错！"+e.getMessage());
				}
			}
			//3.生成对照表

			de.clearSql();
			de.addSql("select 1 from odssu.hbjd_dzb where  yjdid = :jdid ");
			de.setString("jdid", jdid);
			DataStore dzbVds = de.query();
			if(dzbVds == null || dzbVds.rowCount() == 0){
				de.clearSql();
  				de.addSql(" insert into odssu.hbjd_dzb (yjdid,xjdid) values(:jdid,:neworgno) ");
				de.setString("jdid",jdid);
				de.setString("neworgno",neworgno);
				de.update();
			}else{
				de.clearSql();
  				de.addSql(" update odssu.hbjd_dzb set xjdid = :neworgno where yjdid = :jdid ");
				de.setString("neworgno",neworgno);
				de.setString("jdid",jdid);
				de.update();
			}

			LanePromptUtil.promptToTrace("【"+(i+1)+"】合并街道【"+jdmc+"】,新街道编号为【"+neworgno+"】");
			logBF.append("【"+(i+1)+"】合并街道【"+jdmc+"】,新街道编号为【"+neworgno+"】\r\n");
		}
		LanePromptUtil.promptToTrace("合并街道已完成！");
		LanePromptUtil.complete();
		logBF.append(">>>合并街道已完成！");
		DataObject result = DataObject.getInstance();
		result.put("logstr", logBF.toString());
		result.put("returnfiletype", "txt");
		return result;
	}
}
