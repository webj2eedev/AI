package com.dw.odssu.ws.role.jssjjswh;

import java.util.Date;

import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.BusinessException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class JssjjswhBPO extends BPO{

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
  		de.addSql("  from odssuws.jssjjsxg ");
  		de.addSql(" where piid=:piid ");
		de.setString("piid", piid);
		gdxxds = de.query();

		rdo.put("gdxxds", gdxxds);
		gdxxds = null;

		return rdo;
	}

	/**
	 * 调整角色上级角色申请--下一步
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-8-22
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveRoleSupRoleAdjust(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String piid, roleflag;
		DE de = DE.getInstance();

		// String roleno = para.getString("roleno");
		// 标识有没有进行修改操作
		int flag = 0, result;
		piid = para.getString("piid");
		DataStore roleinfods = para.getDataStore("roleInfo");

		DataObject vdo1 = getGdxx(piid);
		String roleno = vdo1.getDataStore("gdxxds").getString(0, "roleno");

		// 清空当前piid的工单副表
		de.clearSql();
  		de.addSql(" delete from odssuws.jssjjsxg_detl ");
  		de.addSql("   where piid = :piid  ");
		de.setString("piid", piid);
		de.update();

		for (int i = 0; i < roleinfods.rowCount(); i++) {
			String cz = roleinfods.getString(i, "cz");
			if (!"".equals(cz)) {
				roleflag = "";
				flag++;
				String suproleno = roleinfods.getString(i, "roleno");
				if ("0".equals(cz)) {// cz 0代表删除，1代表增加
					roleflag = "-";
				} else if ("1".equals(cz)) {
					roleflag = "+";
				}

				// 保存到工单表
				de.clearSql();
  				de.addSql(" insert into odssuws.jssjjsxg_detl ");
  				de.addSql("    (piid, roleno, roleflag, suproleno )  ");
  				de.addSql("  values(:piid, :roleno, :roleflag, :suproleno )  ");
				de.setString("piid", piid);
				de.setString("roleno", roleno);
				de.setString("roleflag", roleflag);
				de.setString("suproleno", suproleno);
				result = de.update();

				if (result == 0) {
					this.bizException("工单信息更新失败!");
				}
				// 更新工单主表的操作人信息
			}
		}
		if (flag == 0) {
			this.bizException("您没有进行任何修改操作，不能进行下一步或保存操作！");
		}
		de.clearSql();
  		de.addSql(" update odssuws.jssjjsxg ");
  		de.addSql("    set spyj = null,spsm = null,reviewer = null , reviewtime = null  ");
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
	 * 调整角色上级角色申请--暂存
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-8-22
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public final DataObject saveRoleSupRoleAdjust4doSave(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		String piid, roleflag;
		DE de = DE.getInstance();
		// String roleno = para.getString("roleno");
		// 标识有没有进行修改操作
		int flag = 0, result;
		piid = para.getString("piid");
		DataStore roleinfods = para.getDataStore("roleInfo");

		DataObject vdo1 = getGdxx(piid);
		String roleno = vdo1.getDataStore("gdxxds").getString(0, "roleno");

		// 清空当前piid的工单副表
		de.clearSql();
  		de.addSql(" delete from odssuws.jssjjsxg_detl ");
  		de.addSql("   where piid = :piid  ");
		de.setString("piid", piid);
		de.update();

		for (int i = 0; i < roleinfods.rowCount(); i++) {
			String cz = roleinfods.getString(i, "cz");
			if (!"".equals(cz)) {
				roleflag = "";
				flag++;
				String suproleno = roleinfods.getString(i, "roleno");
				if ("0".equals(cz)) {// cz 0代表删除，1代表增加
					roleflag = "-";
				} else if ("1".equals(cz)) {
					roleflag = "+";
				}

				// 保存到工单表
				de.clearSql();
  				de.addSql(" insert into odssuws.jssjjsxg_detl ");
  				de.addSql("    (piid, roleno, roleflag, suproleno )  ");
  				de.addSql("  values(:piid, :roleno, :roleflag, :suproleno )  ");
				de.setString("piid", piid);
				de.setString("roleno", roleno);
				de.setString("roleflag", roleflag);
				de.setString("suproleno", suproleno);
				result = de.update();

				if (result == 0) {
					this.bizException("工单信息更新失败!");
				}
				// 更新工单主表的操作人信息
			}
		}

		de.clearSql();
  		de.addSql(" update odssuws.jssjjsxg ");
  		de.addSql("    set spyj = null,spsm = null,reviewer = null , reviewtime = null  ");
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
	public final DataObject saveRoleSupRoleAdjustApproval(DataObject para) throws Exception {
		DataObject vdo = DataObject.getInstance();
		DataStore vds;
		String piid, spr, spyj, spsm;
		Date sprq;
		DE de = DE.getInstance();

		piid = para.getString("piid");
		spr = para.getString("spr");
		spyj = para.getString("spyj");
		spsm = para.getString("spsm");
		sprq = para.getDate("sprq");

		de.clearSql();
  		de.addSql(" select 1");
  		de.addSql("   from odssuws.jssjjsxg  ");
  		de.addSql("  where piid = :piid  ");
		de.setString("piid", piid);
		vds = de.query();

		if (vds.rowCount() < 1) {
			throw new BusinessException("未取到工单信息" + piid);
		}

		// para.put("_user", CurrentUser.getInstance());
		// this.executeBKO(JssjjswhBKO.class.getName(),
		// "saveRoleSubRoleAdjustApproval", para);

		// 工单记录记账标识
		de.clearSql();
  		de.addSql(" update odssuws.jssjjsxg  ");
  		de.addSql("    set sjjsxgjzbz = '1', reviewer = :spr, reviewtime =:sprq , spyj = :spyj , spsm = :spsm  ");
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
