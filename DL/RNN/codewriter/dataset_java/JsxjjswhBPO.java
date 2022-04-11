package com.dw.odssu.ws.role.jsxjjswh;

import java.util.Date;

import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class JsxjjswhBPO extends BPO{

	/**
	 * 查询工单信息 .
	 * <p>
	 * 方法详述
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	public final DataObject getGdxx(String piid) throws Exception {
		DataObject rdo = DataObject.getInstance();
		DataStore gdxxds = DataStore.getInstance();
		DE de = DE.getInstance();

		de.clearSql();
  		de.addSql("select * ");
  		de.addSql("  from odssuws.jsxjjsxg ");
  		de.addSql(" where piid=:piid ");
		de.setString("piid", piid);
		gdxxds = de.query();

		rdo.put("gdxxds", gdxxds);
		gdxxds = null;

		return rdo;
	}

	/**
	 * <p>
	 * 方法详述保存角色的上级角色
	 * </p>
	 * 
	 * @param 关键字
	 * @throws 异常说明
	 * @author liuy
	 * @date 创建时间 2014-05-07
	 * @since V1.0
	 */
	public final DataObject saveRoleSubRoleAdjust(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String piid, roleflag;
		DE de = DE.getInstance();
		
		int flag = 0 , result;
		piid = para.getString("piid");
		DataStore roleinfods = para.getDataStore("roleInfo");

		DataObject vdo1 = getGdxx(piid);
		String roleno = vdo1.getDataStore("gdxxds").getString(0, "roleno");
		
		de.clearSql();
  		de.addSql(" delete from odssuws.jsxjjsxg_detl ");
  		de.addSql("   where piid = :piid  ");
		de.setString("piid", piid);
		de.update();

		for (int i = 0; i < roleinfods.rowCount(); i++) {
			String cz = roleinfods.getString(i, "cz");
			if (!"".equals(cz)) {
				flag++;
				roleflag = "";
				String subroleno = roleinfods.getString(i, "roleno");
				if ("0".equals(cz)) {// cz 0代表删除，1代表增加
					roleflag = "-";
				} else if ("1".equals(cz)) {
					roleflag = "+";
				}

				// 保存到工单表
				de.clearSql();
  				de.addSql(" insert into odssuws.jsxjjsxg_detl ");
  				de.addSql("    (piid, roleno, roleflag, subroleno )  ");
  				de.addSql("  values(:piid, :roleno, :roleflag, :subroleno )  ");
				de.setString("piid", piid);
				de.setString("roleno", roleno);
				de.setString("roleflag", roleflag);
				de.setString("subroleno", subroleno);
				result = de.update();

				if (result == 0) {
					this.bizException("工单信息更新失败!");
				}
				// 更新工单主表的操作人信息
			}
		}
		if (flag == 0 ) {
			this.bizException("您没有进行任何修改操作，不能进行下一步或保存操作！");
		}
		de.clearSql();
  		de.addSql(" update odssuws.jsxjjsxg ");
  		de.addSql("    set spyj = null, spsm = null , reviewer= null , reviewtime = null");
  		de.addSql("  where piid = :piid ");
		de.setString("piid", piid);
		de.update();
		result = de.update();
		
		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}
		return vdo;
	}
	
	/**
	 * 角色下级角色修改--暂存
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-8-22
	 * @param request
	 * @param response
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveRoleSubRoleAdjust4doSave(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String piid, roleflag;
		DE de = DE.getInstance();

		
		int flag = 0 , result;
		piid = para.getString("piid");
		DataStore roleinfods = para.getDataStore("roleInfo");

		DataObject vdo1 = getGdxx(piid);
		String roleno = vdo1.getDataStore("gdxxds").getString(0, "roleno");
		
		de.clearSql();
  		de.addSql(" delete from odssuws.jsxjjsxg_detl ");
  		de.addSql("   where piid = :piid  ");
		de.setString("piid", piid);
		de.update();

		for (int i = 0; i < roleinfods.rowCount(); i++) {
			String cz = roleinfods.getString(i, "cz");
			if (!"".equals(cz)) {
				flag++;
				roleflag = "";
				String subroleno = roleinfods.getString(i, "roleno");
				if ("0".equals(cz)) {// cz 0代表删除，1代表增加
					roleflag = "-";
				} else if ("1".equals(cz)) {
					roleflag = "+";
				}

				// 保存到工单表
				de.clearSql();
  				de.addSql(" insert into odssuws.jsxjjsxg_detl ");
  				de.addSql("    (piid, roleno, roleflag, subroleno )  ");
  				de.addSql("  values(:piid, :roleno, :roleflag, :subroleno )  ");
				de.setString("piid", piid);
				de.setString("roleno", roleno);
				de.setString("roleflag", roleflag);
				de.setString("subroleno", subroleno);
				result = de.update();

				if (result == 0) {
					this.bizException("工单信息更新失败!");
				}
				// 更新工单主表的操作人信息
			}
		}
		de.clearSql();
  		de.addSql(" update odssuws.jsxjjsxg ");
  		de.addSql("    set spyj = null, spsm = null  , reviewer= null , reviewtime = null");
  		de.addSql("  where piid = :piid ");
		de.setString("piid", piid);
		de.update();
		result = de.update();
		
		if (result == 0) {
			this.bizException("工单信息更新失败!");
		}
		return vdo;
	}


	/**
	 * 保存角色的上级角色
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-5-16
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveRoleSubRoleAdjustApproval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore vds;
		String piid , spr , spyj , spsm ;
		Date sprq ;
		DE de = DE.getInstance();
		
		piid = para.getString("piid");
		spr = para.getString("spr");
		spyj = para.getString("spyj");
		spsm = para.getString("spsm");
		sprq = para.getDate("sprq");
		
		de.clearSql();
  		de.addSql(" select 1 ");
  		de.addSql("   from odssuws.jsxjjsxg  ");
  		de.addSql("  where piid = :piid  ");
		de.setString("piid", piid);
		vds = de.query();

		if (vds.rowCount() < 1) {
			throw new BusinessException("未取到工单信息" + piid);
		}



		// 工单记录记账标识
		de.clearSql();
  		de.addSql(" update odssuws.jsxjjsxg  ");
  		de.addSql("    set  reviewer = :spr, reviewtime =:sprq ,spyj = :spyj , spsm = :spsm ");
  		de.addSql("  where piid = :piid  ");
		
		de.setString("spr", spr);
		de.setDateTime("sprq", sprq);
		de.setString("spyj", spyj);
		de.setString("spsm", spsm);
		de.setString("piid", piid);

		de.update();

		vds = null;
		return vdo;
	}
}
