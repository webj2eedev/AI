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
import com.dareway.framework.dbengine.DE;
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

/**
 * 类描述：批量功能 - 批量导入社区数据
 * @author 能天宇
 * @date 2016-10-24
 */
public class CommunityImpBPO extends BPO {
	ASO orgaddASO = this.newASO(OrgAddASO.class);
	ASO communitycombineASO = this.newASO(OrgCombineASO.class);
	/**
	 * 
	 * @Description:校验核三临时表数据 
	 * @author 能天宇
	 * @date 2016-10-21
	 */
	public DataObject checkTempCommunityData(DataObject para) throws AppException{
		DataObject vdo = DataObject.getInstance();
		
		//校验社区名称是否与ods中机构标识名称冲突，自身名称重复

		DE de = DE.getInstance();
		de.clearSql();
  		de.addSql("select n.sqid,n.sqmc ,n.jdid ,s.jdmc ");
  		de.addSql("  from odssu.temp_be3_community_natl n left outer join odssu.temp_be3_street_natl s ");
  		de.addSql("  	 on n.jdid = s.jdid ");
  		de.addSql(" where n.sqid in (select a.sqid ");
  		de.addSql("                    from odssu.temp_be3_community_natl a, odssu.orginfor b ");
  		de.addSql("                   where a.sqmc = b.orgname and (a.zxbz is null or a.zxbz <> '1' ) ");
  		de.addSql("                  union ");
  		de.addSql("                  select c.sqid ");
  		de.addSql("                    from odssu.temp_be3_community_natl c ");
  		de.addSql("                   where c.sqmc in (select d.sqmc ");
  		de.addSql("                                      from odssu.temp_be3_community_natl d ");
  		de.addSql("                                      where  (d.zxbz is null or d.zxbz <> '1' )");
  		de.addSql("                                     group by d.sqmc ");
  		de.addSql("                                    having count(d.sqmc) > 1) ");
  		de.addSql("                     and (c.zxbz is null or c.zxbz <> '1' )) ");
  		de.addSql(" order by n.sqmc  ");

		DataStore badsqmcVds = de.query();
		if (badsqmcVds.rowCount() > 0) {
			vdo.put("msg", "badsqmc");
			vdo.put("ds", badsqmcVds);
			return vdo;
		}
		
		//校验是否有社区没有对应的街道或对应街道在ODS不存在
		de.clearSql();
  		de.addSql("select a.sqid, a.sqmc ");
  		de.addSql("  from odssu.temp_be3_community_natl a left outer join odssu.temp_be3_street_natl s ");
  		de.addSql("       on a.jdid = s.jdid ");
  		de.addSql(" where a.sqid in ");
  		de.addSql("       (select b.sqid ");
  		de.addSql("          from odssu.temp_be3_community_natl b ");
  		de.addSql("         where b.jdid is null ");
  		de.addSql("        union ");
  		de.addSql("        select c.sqid ");
  		de.addSql("          from odssu.temp_be3_community_natl c, odssu.temp_be3_street_natl d ");
  		de.addSql("         where c.jdid = d.jdid ");
  		de.addSql("           and (d.belongorgno is null or d.isvalid = '0' or d.orgno is null )) ");
  		de.addSql("   		 and (a.zxbz is null or a.zxbz <> '1' )");
  		de.addSql(" order by a.sqmc ");
		DataStore noVds = de.query();
		if (noVds.rowCount() > 0) {
			vdo.put("msg", "nobelongorg");
			vdo.put("ds", noVds);
			return vdo;
		} 
		vdo.put("msg", "社区临时表数据校验成功！");
		return vdo;
	}
	/**
	 * 
	 * @Description: 保存对修改社区名称的处理结果
	 * @author 能天宇
	 * @date 2016-10-24
	 */
	public DataObject saveReNameCommunity(DataObject para) throws AppException, BusinessException{
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		DataStore gridVds = para.getDataStore("datagrid");	//前台Grid数据
		if(gridVds == null ){
			throw new AppException("提交更改时，获取grid数据失败！");
		}
		//校验是否仍有社区对应多个街道
		for(int i = 0 ;i < gridVds.rowCount(); i++){
			String sqid = gridVds.getString(i, "sqid");
			String sqmc = gridVds.getString(i, "sqmc");
			if(sqmc == null || "".equals(sqmc)){
				throw new AppException("填写的社区编号为【"+sqid+"】的社区名称为空");
			}
			de.clearSql();
  			de.addSql(" select sqmc from odssu.temp_be3_community_natl where sqid = :sqid ");
			de.setString("sqid",sqid );
			DataStore sqmcds = de.query();
			if(sqmcds == null || sqmcds.rowCount() == 0){
				throw new AppException("读取临时表【odssu.temp_be3_community_natl】中社区编号为【"+sqid+"】的社区名称失败");
			}
			String oldsqmc  = sqmcds.getString(0, "sqmc");
			if(!sqmc.equals(oldsqmc)){
				de.clearSql();
  				de.addSql(" update  odssu.temp_be3_community_natl set sqmc = :sqmc ,comments = :comments where sqid = :sqid ");
				de.setString("sqmc",sqmc );
				String para1 = "社区名称冲突,将社区名称【"+oldsqmc+"】修改为【"+sqmc+"】." ;
				de.setString("comments",para1 );
				de.setString("sqid",sqid );
				de.update();
			}
		}
		
		//校验社区名称是否与ods中机构标识名称冲突，自身名称重复

		de.clearSql();
  		de.addSql("select n.sqid,n.sqbh ,n.sqmc ,n.isvalid ,n.jdid ,s.jdmc ");
  		de.addSql("  from odssu.temp_be3_community_natl n left outer join odssu.temp_be3_street_natl s ");
  		de.addSql("  	 on n.jdid = s.jdid ");
  		de.addSql(" where n.sqid in (select a.sqid ");
  		de.addSql("                    from odssu.temp_be3_community_natl a, odssu.orginfor b ");
  		de.addSql("                   where a.sqmc = b.orgname and (a.zxbz is null or a.zxbz <> '1' ) ");
  		de.addSql("                  union ");
  		de.addSql("                  select c.sqid ");
  		de.addSql("                    from odssu.temp_be3_community_natl c ");
  		de.addSql("                   where c.sqmc in (select d.sqmc ");
  		de.addSql("                                      from odssu.temp_be3_community_natl d ");
  		de.addSql("                                      where  (d.zxbz is null or d.zxbz <> '1' )");
  		de.addSql("                                     group by d.sqmc ");
  		de.addSql("                                    having count(d.sqmc) > 1) ");
  		de.addSql("                     and (c.zxbz is null or c.zxbz <> '1' )) ");
  		de.addSql(" order by n.sqmc  ");

		DataStore badsqmcVds = de.query();
		
		vdo.put("msg", "提交更改成功！");
		vdo.put("ds", badsqmcVds);
		return vdo;
	}
	/**
	 * 
	 * @Description:为社区选择所属街道
	 * @author 能天宇
	 * @throws BusinessException 
	 * @date 2016-10-24
	 */
	public DataObject lovForChooseJD(DataObject para) throws AppException, BusinessException{
		String jdmc = para.getString("jdmc");
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		jdmc = "%"+jdmc+"%";
		de.clearSql();
  		de.addSql("select a.jdmc, a.jdid, a.belongorgno, b.orgname ");
  		de.addSql("  from odssu.temp_be3_street_natl a, odssu.orginfor b ");
  		de.addSql(" where a.isvalid = '1' ");
  		de.addSql("   and a.belongorgno = b.orgno ");
  		de.addSql("   and a.orgno is not null ");
  		de.addSql("   and (a.jdmc like :jdmc or a.jdid like :jdmc ) ");
  		de.setString("jdmc", jdmc);
		DataStore dstemp = de.query();
		vdo.put("jdds", dstemp);
		return vdo;
	}
	/**
	 * 
	 * @Description:保存对无隶属街道的处理结果 
	 * @author 能天宇
	 * @date 2016-10-24
	 */
	public DataObject saveChoseBelongJD(DataObject para) throws AppException, BusinessException{
		DataObject vdo = DataObject.getInstance();
		DE de = DE.getInstance();
		DataStore gridVds = para.getDataStore("datagrid");	//前台Grid数据
		if(gridVds == null ){
			throw new AppException("提交更改时，获取grid数据失败！");
		}
		
		//保存更改，将选择的隶属街道计入到数据库
		for(int i = 0 ;i < gridVds.rowCount(); i++){
			String jdid = gridVds.getString(i, "jdid");
			String sqid = gridVds.getString(i, "sqid");
			//锁表
			de.clearSql();
  			de.addSql(" select * from odssu.temp_be3_community_natl for update ");
  			de.addSql(" where sqid = :sqid  ");
			de.setString("sqid",sqid);
			de.query();
			
			if(jdid == null || "".equals(jdid)){
				//未选择街道 ，设为无效社区
				de.clearSql();
  				de.addSql("update odssu.temp_be3_community_natl ");
  				de.addSql("   set jdid = null ,isvalid = :isvalid, comments = :comments ");
  				de.addSql(" where sqid = :sqid ");
				de.setString("isvalid","0");
				de.setString("comments","此社区没有隶属街道或原隶属街道在ODS中不存在，故而成为无效社区，不向ODS中转入.");
				de.setString("sqid",sqid);
				de.update();
				
				continue;
			}
			//更新社区临时表
			de.clearSql();
  			de.addSql("update odssu.temp_be3_community_natl ");
  			de.addSql("   set jdid = :jdid, isvalid = :isvalid, comments = :comments ");
  			de.addSql(" where sqid = :sqid ");
			de.setString("jdid",jdid);
			de.setString("isvalid","1");
			de.setString("comments","原本此社区没有合法的隶属街道，故而给此社区人工选定了隶属街道.");
			de.setString("sqid",sqid);
			de.update();
		}
		vdo.put("msg", "提交更改成功！可以导入社区");
		return vdo;
	}

	/**
	 * 
	 * @Description: 批量转入社区 - 记账
	 * @author 能天宇
	 * @date 2016-10-22
	 */
	public DataObject saveCommunityDataImp(DataObject para) throws AppException{
		DE de = DE.getInstance();
		StringBuffer logBF = new StringBuffer();
		de.clearSql();
  		de.addSql("select * from odssu.temp_be3_community_natl where isvalid = '1' ");
		DataStore communityds = de.query();
		if (communityds ==null || communityds.rowCount() == 0) {
			throw new AppException("获取临时表【odssu.temp_be3_community_natl】数据失败！");
		}
		LanePromptUtil.promptToTrace("开始转入社区数据,共【"+communityds.rowCount()+"】个:");
		logBF.append("开始转入社区数据,共【"+communityds.rowCount()+"】个:\r\n");
		for (int i = 0; i < communityds.rowCount();i++) {
		//for (int i = 0; i < 10;i++) {
			String jdid = communityds.getString(i, "jdid");
			String sqmc = communityds.getString(i, "sqmc");
			String sqid = communityds.getString(i, "sqid");
			
			de.clearSql();
  			de.addSql("select * from odssu.temp_be3_street_natl where jdid = :jdid and orgno is not null ");
  			de.setString("jdid", jdid);
			DataStore streetds = de.query();
			if(streetds == null || streetds.rowCount() == 0){
				throw new AppException("查询表【odssu.temp_be3_street_natl】中社区【"+sqmc+"】的所属街道信息失败.");
			}
			String streetOrgno = streetds.getString(0, "orgno");
			String orgno ;
			orgno = ""+streetOrgno + de.getNextVal("odssu.seq_sbz");
			//1.生成orgno
			while(OdssuUtil.isOrgExist(orgno)){
				orgno = ""+streetOrgno + de.getNextVal("odssu.seq_sbz");
			}
			String orgname = sqmc;
			//2.更新临时表
			de.clearSql();
  			de.addSql("update odssu.temp_be3_community_natl set orgno = :orgno, orgname = :orgname  where sqid = :sqid ");
			de.setString("orgno",orgno);
			de.setString("orgname",orgname);
			de.setString("sqid",sqid);
			de.update();
			
			//3.导入机构(社区)
			DataObject communityPara = DataObject.getInstance();
			String userid = this.getUser().getUserid();
			Date today = DateUtil.getCurrentDate();
			String nyr = DateUtil.FormatDate(today, "yyyyMMdd");
			communityPara.put("pjbh", "F" + nyr);
			communityPara.put("orgno", orgno);
			communityPara.put("orgname", orgname);
			communityPara.put("displayname", orgname);
			communityPara.put("fullname", orgname);
			communityPara.put("belongorgno", streetOrgno);
			communityPara.put("orgtype", OdssuContants.ORGTYPE_SBZ);
			communityPara.put("_user", CurrentUser.getInstance());
			communityPara.put("pdid", "F" + nyr);
			communityPara.put("objectid", "cc");
			communityPara.put("userid", userid);
				//调用新增机构ASO
			try {
				orgaddASO.doEntry(communityPara);		
			} catch (ASOException e) {
				e.printStackTrace();
				throw new AppException("转入社区【"+orgname+"】执行ASO记账时出错！"+e.getMessage());
			}
			//4.生成对照表

			de.clearSql();
			de.addSql("select 1 from odssu.sq_dzb where  ysqid = :sqid ");
			de.setString("sqid", sqid);
			DataStore dzbVds = de.query();
			if(dzbVds == null || dzbVds.rowCount() == 0){
				de.clearSql();
  				de.addSql(" insert into odssu.sq_dzb (ysqid,xsqid) values(:sqid,:orgno) ");
				de.setString("sqid",sqid);
				de.setString("orgno",orgno);
				de.update();
			}else{
				de.clearSql();
  				de.addSql(" update odssu.sq_dzb set xsqid = :orgno where ysqid = :sqid ");
				de.setString("orgno",orgno);
				de.setString("sqid",sqid);
				de.update();
			}
			LanePromptUtil.promptToTrace("【"+(i+1)+"】转入了社区【"+sqmc+"】,新社区编号为【"+orgno+"】");
			logBF.append("【"+(i+1)+"】转入了社区【"+sqmc+"】,新社区编号为【"+orgno+"】\r\n");
		}
		LanePromptUtil.promptToTrace("批量转入社区完成！");
		logBF.append("批量转入社区完成！");
		LanePromptUtil.complete();
		DataObject vdo = DataObject.getInstance();
		vdo.put("msg", "社区数据转入完成！");
		vdo.put("logstr", logBF.toString());
		return vdo;
	}
	/**
	 * 
	 * @Description: 要合并的社区数据来自哪个人社系统
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
			throw new AppException("街道临时表【odssu.temp_be3_street_natl】中的街道所属的人社系统不存在！");
		}else if(rsxtds.rowCount() > 1){
			throw new AppException("街道临时表【odssu.temp_be3_street_natl】中的街道来自不同的人社系统！");
		}
		vdo.put("ds", rsxtds);
		vdo.put("orgno", rsxtds.getString(0, "orgno"));
		return vdo;
	}
	/**
	 * 
	 * @Description: 生成合并社区统计模板
	 * @author 能天宇
	 * @date 2016-10-22
	 */
	public DataObject createFileForCombineCommunity(DataObject para) throws Exception{
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
			prompt.prompt("【"+(i+1)+"】开始生成 【"+qxmc+"】的合并社区统计模板."); // 向PROMPT区输出提示信息

			//获取社区信息数据

			DE de = DE.getInstance();
			de.clearSql();
  			de.addSql("select :qxmc qxmc,a.sqid, a.sqmc, a.orgno, a.orgname,b.jdmc");
  			de.addSql("  from odssu.temp_be3_community_natl a,odssu.temp_be3_street_natl b ");
  			de.addSql(" where a.isvalid = '1' ");
  			de.addSql("   and a.jdid = b.jdid  ");
  			de.addSql("   and b.belongorgno = :orgno ");
  			de.setString("qxmc", qxmc);
			de.setString("orgno", orgno);
			DataStore communityds = de.query();
			
			//组装Excel
			DataObject dataobj = DataObject.getInstance();
			dataobj.put("communityds", communityds);
			dataobj.put("qxmc", qxmc);
			DataObject result = createSingleExcel(dataobj);
			
			// 获取HSSFWorkbook的编码串
			HSSFWorkbook wb = (HSSFWorkbook) result.get("wb");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			wb.write(out);
			WritableWorkbook workbook = ExcelTool.createWorkbook(out);
			ExcelTool.writeWbootAndClose(workbook);
			ExcelTool.closeOutputStream(out);
			String fileName = qxmc + "_社区合并统计表" + ".xls";
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
	 * @Description: 获取人社系统下的所有街道
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
			throw new AppException("无法获取人社系统下的街道信息!");
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

		DataStore communityds = para.getDataStore("communityds");
		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 1.生成表格 -社区合并统计表
		HSSFSheet sheet = workbook.createSheet("社区合并统计表");
		sheet.setColumnWidth(0, 3000);
		sheet.setColumnWidth(2, 7000);
		sheet.setColumnWidth(5, 7000);
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
		firstrow.put(columnNoOfFirstRow++, "name", "核三系统社区编号");
		firstrow.put(columnNoOfFirstRow++, "name", "核三系统社区名称");
		firstrow.put(columnNoOfFirstRow++, "name", "社区所属街道");
		firstrow.put(columnNoOfFirstRow++, "name", "对应新系统社区编号");
		firstrow.put(columnNoOfFirstRow++, "name", "对应新系统社区名称");
		firstrow.put(columnNoOfFirstRow++, "name", "新社区所属街道");
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
		for(int j=0; j < communityds.rowCount();j++){
			HSSFRow rowtemp = sheet.createRow(j+1);
			HSSFCell cell = rowtemp.createCell(0);
			HSSFRichTextString text = new HSSFRichTextString(communityds.getString(j, "qxmc"));
			cell.setCellValue(text);
			HSSFCell cell1 = rowtemp.createCell(1);
			HSSFRichTextString text1 = new HSSFRichTextString(communityds.getString(j, "sqid"));
			cell1.setCellValue(text1);
			HSSFCell cell2 = rowtemp.createCell(2);
			HSSFRichTextString text2 = new HSSFRichTextString(communityds.getString(j, "sqmc"));
			cell2.setCellValue(text2);
			HSSFCell cell3 = rowtemp.createCell(3);
			HSSFRichTextString text3 = new HSSFRichTextString(communityds.getString(j, "jdmc"));
			cell3.setCellValue(text3);
			HSSFCell cell4 = rowtemp.createCell(4);
			HSSFRichTextString text4 = new HSSFRichTextString(communityds.getString(j, "orgno"));
			cell4.setCellValue(text4);
			HSSFCell cell5 = rowtemp.createCell(5);
			HSSFRichTextString text5 = new HSSFRichTextString(communityds.getString(j, "orgname"));
			cell5.setCellValue(text5);
			HSSFCell cell6 = rowtemp.createCell(6);
			HSSFRichTextString text6 = new HSSFRichTextString(communityds.getString(j, "jdmc"));
			cell6.setCellValue(text6);
			HSSFCell cell7 = rowtemp.createCell(7);
			HSSFRichTextString text7 = new HSSFRichTextString("建议保留");
			cell7.setCellValue(text7);
		}
		
		// 2.生成一张工作表-可选新社区
		HSSFSheet sheetnew = workbook.createSheet("新系统社区参考");
		sheetnew.setDefaultColumnWidth(20);

		// 产生表格标题行
		DataStore newtitle = DataStore.getInstance();
		newtitle.put(0, "name", "区县名称");
		newtitle.put(1, "name", "新系统社区编号");
		newtitle.put(2, "name", "新系统社区名称");
		newtitle.put(3, "name", "新社区所属街道");
		
		// 产生表格标题行
		HSSFRow titlerow = sheetnew.createRow(0);
		
		for (int j = 0; j < newtitle.rowCount(); j++) {
			HSSFCell cell = titlerow.createCell(j);
			cell.setCellStyle(style);
			HSSFRichTextString text = new HSSFRichTextString(newtitle.getString(j, "name"));
			cell.setCellValue(text);
		}
		
		for(int j=0; j < communityds.rowCount();j++){
			// 产生数据事例行
			HSSFRow rowtemp = sheetnew.createRow(j+1);
			
			HSSFCell cell = rowtemp.createCell(0);
			HSSFRichTextString text = new HSSFRichTextString(communityds.getString(j, "qxmc"));
			cell.setCellValue(text);
			HSSFCell cell1 = rowtemp.createCell(1);
			HSSFRichTextString text1 = new HSSFRichTextString(communityds.getString(j, "orgno"));
			cell1.setCellValue(text1);
			HSSFCell cell2 = rowtemp.createCell(2);
			HSSFRichTextString text2 = new HSSFRichTextString(communityds.getString(j, "orgname"));
			cell2.setCellValue(text2);
			HSSFCell cell3 = rowtemp.createCell(3);
			HSSFRichTextString text3 = new HSSFRichTextString(communityds.getString(j, "jdmc"));
			cell3.setCellValue(text3);
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("wb", workbook);
		return vdo;
	}
	/**
	 * 
	 * @Description: 解析合并社区excel到ds
	 * @author 能天宇
	 * @date 2016-10-24
	 */
	public DataObject analyzeExcelToDsCommunity(DataObject para) throws Exception {
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

		int vi = 0;
		setTableInfo(tableInfo, vi++, "qxmc", "区县名称", "String");
		setTableInfo(tableInfo, vi++, "sqid", "核三系统社区编号", "String");
		setTableInfo(tableInfo, vi++, "sqmc", "核三系统社区名称", "String");
		setTableInfo(tableInfo, vi++, "jdmc", "社区所属街道", "String");
		setTableInfo(tableInfo, vi++, "orgno", "对应新系统社区编号", "String");
		setTableInfo(tableInfo, vi++, "orgname", "对应新系统社区名称", "String");
		setTableInfo(tableInfo, vi++, "newjdmc", "新社区所属街道", "String");
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
			if(dsExcel.getString(i, "sqid") != null)
				dsExcel.put(i, "sqid", dsExcel.getString(i, "sqid").trim());
			if(dsExcel.getString(i, "sqmc") != null)
				dsExcel.put(i, "sqmc", dsExcel.getString(i, "sqmc").trim());
			if(dsExcel.getString(i, "jdmc") != null)
				dsExcel.put(i, "jdmc", dsExcel.getString(i, "jdmc").trim());
			if(dsExcel.getString(i, "newjdmc") != null)
				dsExcel.put(i, "newjdmc", dsExcel.getString(i, "newjdmc").trim());
			if(dsExcel.getString(i, "orgno") != null)
				dsExcel.put(i, "orgno", dsExcel.getString(i, "orgno").trim());
			if(dsExcel.getString(i, "orgname") != null)
				dsExcel.put(i, "orgname", dsExcel.getString(i, "orgname").trim());
			if(dsExcel.getString(i, "cljy") != null)
				dsExcel.put(i, "cljy", dsExcel.getString(i, "cljy").trim());
		}
		DataObject result = DataObject.getInstance();
		result.put("communityds", dsExcel);
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
	 * @Description: 校验合并社区
	 * @author 能天宇
	 * @date 2016-10-24
	 */
	public DataObject checkCombineCommunity(DataObject para) throws AppException, BusinessException{
		StringBuffer logBF = new StringBuffer();
		String rsxtorgno = para.getString("orgno","");
		
		// 获取文件数据fileds
		DataObject dataobj = para.getDataObject("dataobj");
		if(dataobj == null || !dataobj.containsKey("communityds")){
			this.bizException("没有正确读入文件!");
		}
		DataStore fileds = dataobj.getDataStore("communityds");
		if(fileds == null || fileds.rowCount() == 0){
			this.bizException("没有社区可以合并。");
		}
		
		LanePromptUtil.promptToTrace("即将开始校验合并社区：共【"+fileds.rowCount()+"】个.");
		logBF.append(">>>即将开始校验合并社区：共【"+fileds.rowCount()+"】个.\r\n");
		boolean checkflag = true;
		for (int i = 0; i < fileds.rowCount(); i++) {
			DataObject row = fileds.getRow(i);
			row.put("jycw", "");
			
			String sqid = row.getString("sqid");
			String sqmc = row.getString("sqmc");
			LanePromptUtil.promptToTrace("【"+(i+1)+"】:校验社区【"+sqmc+"】的相关信息");
			logBF.append("【"+(i+1)+"】:校验社区【"+sqmc+"】的相关信息\r\n");

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

			//检查社区重复行
			DataStore tempds = fileds.findAll(" sqid == " + sqid+ " and sqmc == " +sqmc );
			int beginrowno = 0;
	        if( tempds.rowCount() > 1){
	        	StringBuffer rownoList =new StringBuffer();
	        	beginrowno  = fileds.find(" sqid == " + sqid+ " and sqmc == " +sqmc,beginrowno);
        		rownoList.append(""+(beginrowno+2));
	        	while(beginrowno > -1){
	        		beginrowno  = fileds.find(" sqid == " + sqid+ " and sqmc == " +sqmc,beginrowno+1);
	        		if(beginrowno > -1)
	        			rownoList.append("、"+(beginrowno+2));
	        	}
	        	checkflag = false;
	        	logBF.append("校验错误：【"+rownoList+"】行核三社区重复设置.\r\n");
	        	row.put("jycw", "【"+rownoList+"】行核三社区重复设置.");
				LanePromptUtil.promptToTrace("校验错误：【"+rownoList+"】行核三社区重复设置.");
	        }
	        //检查是否已经合并过

			DE de = DE.getInstance();
	        de.clearSql();
  	        de.addSql(" select xsqid from odssu.hbsq_dzb where ysqid = :sqid ");
	        de.setString("sqid",sqid);
	        DataStore dzbds = de.query();
	        if( dzbds.rowCount()>0 ){
	        	String xsqid= dzbds.getString(0, "xsqid");
	        	checkflag = false;
	        	logBF.append("校验错误：此社区已经合并过了，新社区编号为【"+xsqid+"】.\r\n");
	        	row.put("jycw", "此社区已经合并过了，新社区编号为【"+xsqid+"】.");
				LanePromptUtil.promptToTrace("校验错误：此社区已经合并过了，新社区编号为【"+xsqid+"】.");
	        }
		}
		
		//校验若不通过，返回excel让用户对照错误信息，修改完善
		if(checkflag == false){
			LanePromptUtil.promptToTrace("校验完成，发现错误信息，请修改后再次校验.");
			LanePromptUtil.promptToTrace("正在生成EXCEL.");
			logBF.append(">>>校验完成，发现错误信息，请修改文件数据.\r\n");
			LanePromptUtil.complete();
			DataObject result = DataObject.getInstance();
			result.put("excelds", fileds);
			result.put("returnfiletype", "excel");
			return result;
		}else{
			LanePromptUtil.promptToTrace("合并社区的信息校验成功.");
			LanePromptUtil.promptToTrace("正在生成日志文件.");
			logBF.append(">>>合并社区的信息校验成功.\r\n");
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
		String jdmc = row.getString("jdmc");
		String newjdmc = row.getString("newjdmc");
		String sqmc = row.getString("sqmc");
		String sqid = row.getString("sqid");
		String orgno = row.getString("orgno");
		String orgname = row.getString("orgname");

		if(qxmc == null || "".equals(qxmc))
			jycw.append("区县名称为空.");
		if(sqid == null || "".equals(sqid))
			jycw.append("核三社区编号为空.");
		if(sqmc == null || "".equals(sqmc))
			jycw.append("核三社区名称为空.");
		if(jdmc == null || "".equals(jdmc))
			jycw.append("社区所属街道为空.");
		if(newjdmc == null || "".equals(newjdmc))
			jycw.append("新社区所属街道为空.");
		if(orgno == null || "".equals(orgno))
			jycw.append("新社区编号为空.");
		if(orgname == null  || "".equals(orgname))
			jycw.append("新社区名称为空.");

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
	 * @Description: 检查各项目的正确性
	 * @author 能天宇
	 * @date 2016-10-27
	 */
	private boolean isRowHaveErrorColumn(DataStore communityds,String rsxtorgno,DataObject row,StringBuffer logBF) throws AppException{
		DE de = DE.getInstance();
		StringBuffer jycw = new StringBuffer();
		String qxmc = row.getString("qxmc");
		String jdmc = row.getString("jdmc");
		String newjdmc = row.getString("newjdmc");
		String sqmc = row.getString("sqmc");
		String sqid = row.getString("sqid");
		String orgno = row.getString("orgno");
		String orgname = row.getString("orgname");
		
		if(!OdssuUtil.isRsxt(rsxtorgno)){
			throw new AppException("获取人社系统编号失败！");
		}
		//校验区县名称
		de.clearSql();
  		de.addSql("select orgno from odssu.orginfor a  where a.orgname like :orgname ");
  		de.addSql(" and orgtype in ('HSDOMAIN_DSRSJ', 'HSDOMAIN_QXRSJ') and belongorgno = :rsxtorgno  ");
		de.setString("orgname", qxmc +"%");
		de.setString("rsxtorgno", rsxtorgno);
		DataStore qxds = de.query();
		String qxorgno = "";
		boolean qxIsValid = false;
		if (qxds == null || qxds.rowCount() == 0) {
			jycw.append("区县名称【"+qxmc+"】在ods中不存在！");
		}else if(qxds.rowCount() >1){
			jycw.append("区县名称【"+qxmc+"】不够精确或有重名，无法确认社区所属区县！");
		}else{
			qxorgno= qxds.getString(0, "orgno");
			qxIsValid = true;
		}
		//校验核三社区
		de.clearSql();
  		de.addSql("select sqmc, jdid, isvalid ");
  		de.addSql("  from odssu.temp_be3_community_natl ");
  		de.addSql(" where sqid = :sqid ");
		de.setString("sqid",sqid);
		DataStore jdVds = de.query();
		if (jdVds == null || jdVds.rowCount() == 0) {
			jycw.append("核三社区编号【"+sqid+"】不存在.");
		}else{
			String dsjdid = jdVds.getString(0, "jdid");
			String dssqmc = jdVds.getString(0, "sqmc");
			String dsisvalid = jdVds.getString(0, "isvalid");
			
			if(!sqmc.equals(dssqmc)){
				jycw.append("核三社区【"+sqmc+"】名称与编号不匹配.");
			}
			if("0".equals(dsisvalid)){
				jycw.append("核三社区【"+sqid+"】是无效社区.");
			}else if(dsjdid == null ||"".equals(dsjdid)){
				jycw.append("查询的核三社区【"+sqid+"】所属街道为空.");
			}else{
				//校验核三社区所属街道
				de.clearSql();
  				de.addSql("select b.jdmc,b.isvalid,b.belongorgno from odssu.temp_be3_street_natl b  ");
  				de.addSql(" where b.jdid = :dsjdid");
				de.setString("dsjdid", dsjdid);
				DataStore belongds = de.query();
				if(belongds == null|| belongds.rowCount() == 0){
					jycw.append("核三社区的所属街道在临时表中不存在.");
				}else if(!jdmc.equals(belongds.getString(0, "jdmc"))){
					jycw.append("核三社区的所属街道应当是【"+belongds.getString(0, "jdmc")+"】.");
				}else if(!"1".equals(belongds.getString(0, "isvalid"))){
					jycw.append("核三社区的所属街道【"+jdmc+"】是无效街道.");
				}else if(qxIsValid && !qxorgno.equals(belongds.getString(0, "belongorgno").substring(0, 6))){
					jycw.append("核三社区的所属街道【"+jdmc+"】不隶属于区县【"+qxmc+"】.");
				}
			}
		}
		
		//校验新系统社区
		de.clearSql();
  		de.addSql("select orgno, orgname ,jdid ");
  		de.addSql("  from odssu.temp_be3_community_natl ");
  		de.addSql(" where orgno = :orgno ");
		de.setString("orgno",orgno);
		DataStore newsqVds = de.query();
		if (newsqVds == null || newsqVds.rowCount() == 0) {
			jycw.append("对应的新社区编号【"+orgno+"】在临时表中不存在.");
		}else{  
			String dsjdid = newsqVds.getString(0, "jdid");
			String dsorgname = newsqVds.getString(0, "orgname");
			if(!orgname.equals(dsorgname)){
				jycw.append("对应的新社区【"+orgname+"】名称与编号不匹配.");
			}
			//校验核三社区所属街道
			de.clearSql();
  			de.addSql("select b.jdmc,b.isvalid,b.belongorgno from odssu.temp_be3_street_natl b  ");
  			de.addSql(" where b.jdid = :dsjdid");
			de.setString("dsjdid", dsjdid);
			DataStore belongds = de.query();
			if(belongds == null|| belongds.rowCount() == 0){
				jycw.append("对应的新社区的所属街道在临时表中不存在.");
			}else if(!newjdmc.equals(belongds.getString(0, "jdmc"))){
				jycw.append("对应的新社区的所属街道应当是【"+belongds.getString(0, "jdmc")+"】.");
			}else if(!"1".equals(belongds.getString(0, "isvalid"))){
				jycw.append("对应的新社区的所属街道【"+newjdmc+"】是无效街道.");
			}else if(qxIsValid && !qxorgno.equals(belongds.getString(0, "belongorgno").substring(0, 6))){
				jycw.append("对应的新社区的所属街道【"+newjdmc+"】不隶属于区县【"+qxmc+"】.");
			}
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
	 * @Description:合并社区 - 校验不通过时组装要返回的excel 
	 * @author 能天宇
	 * @date 2016-10-24
	 */
	public DataObject returnFileForCombineCommunity(DataObject para) throws IOException, AppException {
		DataStore communityds = para.getDataStore("excelds");
		// 声明一个工作薄
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 1.生成表格 -社区合并统计表
		HSSFSheet sheet = workbook.createSheet("社区合并统计表");
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
		firstrow.put(columnNoOfFirstRow++, "name", "核三系统社区编号");
		firstrow.put(columnNoOfFirstRow++, "name", "核三系统社区名称");
		firstrow.put(columnNoOfFirstRow++, "name", "社区所属街道");
		firstrow.put(columnNoOfFirstRow++, "name", "对应新系统社区编号");
		firstrow.put(columnNoOfFirstRow++, "name", "对应新系统社区名称");
		firstrow.put(columnNoOfFirstRow++, "name", "新社区所属街道");
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
		for(int j=0; j < communityds.rowCount();j++){
			HSSFRow rowtemp = sheet.createRow(j+1);
			HSSFCell cell = rowtemp.createCell(0);
			HSSFRichTextString text = new HSSFRichTextString(communityds.getString(j, "qxmc"));
			cell.setCellValue(text);
			HSSFCell cell1 = rowtemp.createCell(1);
			HSSFRichTextString text1 = new HSSFRichTextString(communityds.getString(j, "sqid"));
			cell1.setCellValue(text1);
			HSSFCell cell2 = rowtemp.createCell(2);
			HSSFRichTextString text2 = new HSSFRichTextString(communityds.getString(j, "sqmc"));
			cell2.setCellValue(text2);
			HSSFCell cell3 = rowtemp.createCell(3);
			HSSFRichTextString text3 = new HSSFRichTextString(communityds.getString(j, "jdmc"));
			cell3.setCellValue(text3);
			HSSFCell cell4 = rowtemp.createCell(4);
			HSSFRichTextString text4 = new HSSFRichTextString(communityds.getString(j, "orgno"));
			cell4.setCellValue(text4);
			HSSFCell cell5 = rowtemp.createCell(5);
			HSSFRichTextString text5 = new HSSFRichTextString(communityds.getString(j, "orgname"));
			cell5.setCellValue(text5);
			HSSFCell cell6 = rowtemp.createCell(6);
			HSSFRichTextString text6 = new HSSFRichTextString(communityds.getString(j, "newjdmc"));
			cell6.setCellValue(text6);
			HSSFCell cell7 = rowtemp.createCell(7);
			HSSFRichTextString text7 = new HSSFRichTextString(communityds.getString(j, "cljy"));
			cell7.setCellValue(text7);
			HSSFCell cell8 = rowtemp.createCell(8);
			cell8.setCellStyle(styleRed);
			HSSFRichTextString text8 = new HSSFRichTextString(communityds.getString(j, "jycw"));
			cell8.setCellValue(text8);
			if(communityds.getString(j, "jycw") != null && !"".equals(communityds.getString(j, "jycw"))){
				cell.setCellStyle(styleRed);
				cell1.setCellStyle(styleRed);
				cell2.setCellStyle(styleRed);
				cell3.setCellStyle(styleRed);
				cell4.setCellStyle(styleRed);
				cell5.setCellStyle(styleRed);
				cell6.setCellStyle(styleRed);
				cell7.setCellStyle(styleRed);
			}
		}
		DataObject vdo = DataObject.getInstance();
		vdo.put("wb", workbook);
		return vdo;
	}
	/**
	 * 
	 * @Description: 合并社区 -记账
	 * @author 能天宇
	 * @date 2016-10-25
	 */
	public DataObject saveCombineCommunity(DataObject para) throws AppException, BusinessException{
		DE de = DE.getInstance();
		StringBuffer logBF = new StringBuffer();

		// 获取文件数据communityds
		DataObject dataobj = para.getDataObject("dataobj");
		if(dataobj == null || !dataobj.containsKey("communityds")){
			this.bizException("没有正确读入文件!");
		}
		DataStore communityds = dataobj.getDataStore("communityds");
		if(communityds == null || communityds.rowCount() == 0){
			this.bizException("没有社区可以合并。");
		}
		//获取街道临时表数据
		de.clearSql();
  		de.addSql("select b.jdmc,b.neworgno belongorgno from odssu.temp_be3_street_natl b where b.isvalid ='1' and b.neworgno is not null ");
		DataStore streetVds = de.query();
		if (streetVds ==null || streetVds.rowCount() == 0) {
			throw new AppException("获取临时表【odssu.temp_be3_street_natl】数据失败！");
		}
		//获取社区临时表数据
		de.clearSql();
  		de.addSql("select b.sqid,b.orgno from odssu.temp_be3_community_natl b where b.isvalid ='1' ");
		DataStore sqtempVds = de.query();
		if (sqtempVds ==null || sqtempVds.rowCount() == 0) {
			throw new AppException("获取临时表【odssu.temp_be3_community_natl】数据失败！");
		}
				
		LanePromptUtil.promptToTrace("开始合并社区,共【"+communityds.rowCount()+"】个:");
		logBF.append("开始合并社区,共【"+communityds.rowCount()+"】个:\r\n");
		for (int i = 0; i < communityds.rowCount();i++) {
		//for (int i = 0; i < 10;i++) {
			String newjdmc = communityds.getString(i, "jdmc");
			String sqid = communityds.getString(i, "sqid");
			String sqmc = communityds.getString(i, "sqmc");
			String neworgno = communityds.getString(i, "orgno");
			String neworgname = communityds.getString(i, "orgname");
			
			//获取合并后的街道orgno作为合并新社区的上级机构
			DataStore belstreet =  streetVds.findAll(" jdmc == "+newjdmc);
			if(belstreet ==null|| belstreet.rowCount() == 0){
				throw new AppException("无法获取新社区所属街道【"+newjdmc+"】的相关信息");
			}
			String belongorgno = belstreet.getRow(0).getString("belongorgno");
			
			//获取核三社区转入后的(人社所)orgno 
			DataStore oldsqds = sqtempVds.findAll(" sqid == "+ sqid);
			if(oldsqds ==null|| oldsqds.rowCount() == 0){
				throw new AppException("无法获取核三社区【"+sqmc+"】的相关信息");
			}
			String oldorgno = oldsqds.getRow(0).getString("orgno", "");
			
			//1.更新临时表
			String comments = "";
			if(neworgno.equals(oldorgno) ){
				comments = "合并社区时，保留.";
			}else{		//暂时不考虑改新社区名称
				comments = "合并社区时，被合并为"+neworgname+".";
			}
			de.clearSql();
  			de.addSql("update odssu.temp_be3_community_natl set neworgno = :neworgno, neworgname = :neworgname,comments = :comments  where sqid = :sqid ");
			de.setString("neworgno",neworgno);
			de.setString("neworgname",neworgname);
			de.setString("comments",comments);
			de.setString("sqid",sqid);
			de.update();
			
			
			//2.如果已经存在，说明这个社区是多个合并成的，一个orgno只调用一次ASO
			de.clearSql();
  			de.addSql("select 1 from odssu.temp_orginfor  where orgno = :neworgno ");
			de.setString("neworgno",neworgno);
			DataStore existds = de.query();
			if(existds == null || existds.rowCount() == 0){
				//调用社区合并ASO(仿OrgAddASO)生成的机构在odssu.temp_orginfor 中
				//这种增加不会计算odssu.ir_org_closure的闭包关系
				DataObject communityPara = DataObject.getInstance();
				String userid = this.getUser().getUserid();
				Date today = DateUtil.getCurrentDate();
				String nyr = DateUtil.FormatDate(today, "yyyyMMdd");
				communityPara.put("pjbh", "F" + nyr);
				communityPara.put("orgno", neworgno);
				communityPara.put("orgname", neworgname);
				communityPara.put("displayname", neworgname);
				communityPara.put("fullname", neworgname);
				communityPara.put("belongorgno", belongorgno);
				communityPara.put("orgtype", OdssuContants.ORGTYPE_SBZ);
				communityPara.put("_user", CurrentUser.getInstance());
				communityPara.put("pdid", "F" + nyr);
				communityPara.put("objectid", "cc");
				communityPara.put("userid", userid);
				try {
					communitycombineASO.doEntry(communityPara);		
				} catch (ASOException e) {
					e.printStackTrace();
					throw new AppException("合并社区【"+neworgname+"】执行ASO记账时出错！"+e.getMessage());
				}
			}
			//3.生成对照表

			de.clearSql();
			de.addSql("select 1 from odssu.hbsq_dzb where  ysqid = :sqid ");
			de.setString("sqid", sqid);
			DataStore dzbVds = de.query();
			if(dzbVds == null || dzbVds.rowCount() == 0){
				de.clearSql();
  				de.addSql(" insert into odssu.hbsq_dzb (ysqid,xsqid) values(:sqid,:neworgno) ");
				de.setString("sqid",sqid);
				de.setString("neworgno",neworgno);
				de.update();
			}else{
				de.clearSql();
  				de.addSql(" update odssu.hbsq_dzb set xsqid = :neworgno where ysqid = :sqid ");
				de.setString("neworgno",neworgno);
				de.setString("sqid",sqid);
				de.update();
			}

			LanePromptUtil.promptToTrace("【"+(i+1)+"】合并社区【"+sqmc+"】,新社区编号为【"+neworgno+"】");
			logBF.append("【"+(i+1)+"】合并社区【"+sqmc+"】,新社区编号为【"+neworgno+"】\r\n");
		}
		LanePromptUtil.promptToTrace("合并社区已完成！");
		LanePromptUtil.promptToTrace("正在生成日志文件.");
		LanePromptUtil.complete();
		logBF.append(">>>合并社区已完成！");
		DataObject result = DataObject.getInstance();
		result.put("logstr", logBF.toString());
		result.put("returnfiletype", "txt");
		return result;
	}
}
