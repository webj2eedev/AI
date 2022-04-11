package com.dareway.message;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dareway.apps.process.util.ProcessConstants;
import com.dareway.framework.dbengine.DE;
import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

public class O2AHandlerBPO extends BPO{

	/**
	 * ODS中有修改操作需要向activiti中同步数据时，调用此类
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-8-12
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public static DataObject autoSynData(DataObject para) throws Exception {
		DE de = DE.getInstance();
    	de.clearSql();
  		de.addSql(" select csm, csz from odssu.sys_para where csm = 'auto_syn' ");
		DataStore vds = de.query();

		String autoSyn = "false";
		if (vds != null && vds.rowCount() > 0) {
			autoSyn = vds.getString(0, "csz");
		}

		if (autoSyn == null || autoSyn.trim().isEmpty()) {
			autoSyn = "false";
		}

		if ("true".equals(autoSyn)) {
			dealWithNews(null);
		} else {
			return null;
		}

		return null;
	}

	public static DataObject dealWithNews(DataObject para) throws Exception {
		System.out.println("于" + new Date() + "开始数据同步!");

		System.out.println("同步人Start!");
		userManager(null);
		System.out.println("同步人完成!");

		System.out.println("同步group开始!");
		groupManager(null);
		System.out.println("同步group完成!");

		System.out.println("同步ship开始!");
		userGroupManager(null);
		System.out.println("同步ship完成!");

		System.out.println("于" + new Date()
				+ "开始进入ODS与Activiti同步的Adapter，完成数据同步!");
		return null;
	}

	/**
	 * 同步管理---同步activiti中的用户
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-3-3
	 * @param para
	 * @return
	 * @throws Exception
	 */
	public static DataObject userManager(DataObject para) throws Exception {
		DE de = DE.getInstance();

		// 处理user 开始========================================
		// view中的数据比activiti中的表中数据多的时候，此时只有两种情况：新增或者修改
		// -----------view 比old表多

		de.clearSql();
  		de.addSql(" select v.id_ ,v.last_ from o2a.act_id_user_view v ");
  		de.addSql(" where not exists (  ");
  		de.addSql(" select 1 from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_user o where v.id_=o.id_ and v.last_=o.last_ )");

		DataStore vds = de.query();

		for (int i = 0; i < vds.rowCount(); i++) {
			String id = vds.getString(i, "id_");
			String last = vds.getString(i, "last_");

			de.clearSql();
  			de.addSql(" select 1 from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_user where id_=:id ");
			de.setString("id", id);
			DataStore vds1 = de.query();

			if (vds1 == null || vds1.rowCount() == 0) {

				DataStore vds11 = de.getViewColumns("o2a", "act_id_user_view");
				// 向activiti中的表里插数据
				de.clearSql();
				de.addSql(" insert into "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_user ");
				de.addSql("   (id_, rev_, first_, last_, email_, pwd_, picture_id_) ");
				de.addSql(" select v.id_,c2n(v.rev_),v.first_,v.last_, ");
				de.addSql(" v.email_,v.pwd_,v.picture_id_ ");
				de.addSql(" from o2a.act_id_user_view v ");
				de.addSql(" where v.id_ =:id ");
				de.setString("id", id);
				de.update();
			} else {
				// 向activiti中的表中更新数据
				de.clearSql();
  				de.addSql(" update "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_user ");
  				de.addSql(" set last_ = :last ");
  				de.addSql(" where id_ = :id ");
				de.setString("last", last);
				de.setString("id", id);
				de.update();
			}
		}

		de.clearSql();
  		de.addSql(" select 1 from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_user where id_= :id_ ");
		de.setString("id_", "anonymous");
		DataStore anonymousDs = de.query();
		if(anonymousDs == null || anonymousDs.size() == 0){
			de.clearSql();
  			de.addSql(" insert into "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) ");
  			de.addSql("                        values ('anonymous', 2, null, '匿名用户', null, 'anonymous', null) ");
			de.update();
		}
		
		// 当activiti表中比视图中多的时候，删除activiti表中多余的数据；因为理论上执行完上面的程序，
		// activiti中的数据已经包含所有最新的了，如果还有不一致的，则有可能是
		// 垃圾数据，只能删除（当然，除了activiti自己一些必须的数据除外）。
		de.clearSql();
  		de.addSql(" select o.id_ id,o.last_ name_ from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_user o ");
  		de.addSql(" where not exists  (");
  		de.addSql(" select 1 from o2a.act_id_user_view v where o.id_ = v.id_ and o.last_=v.last_)");

		DataStore vdsold = de.query();

		for (int i = 0; i < vdsold.rowCount(); i++) {
			String id = vdsold.getString(i, "id");

			de.clearSql();
  			de.addSql(" select 1 from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_user where id_=:id ");
			de.setString("id", id);
			DataStore vds1 = de.query();

			if (vds != null && vds1.rowCount() > 0 && "kermit".equals(id) == false && "anonymous".equals(id) == false) {
				de.clearSql();
  				de.addSql(" delete from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership ");
  				de.addSql(" where user_id_ = :id ");
				de.setString("id", id);
				de.update();

				de.clearSql();
  				de.addSql(" delete from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_user where id_ = :id ");
				de.setString("id", id);
				de.update();
			}
		}

		return null;
	}

	/**
	 * odssu向activiti中同步数据，同步activiti中的组
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-31
	 * @throws Exception
	 */
	public static void groupManager(DataObject para) throws Exception {
		DE de = DE.getInstance();

		// 处理group 开始=======================================
		// 处理ODSSU中的view中比activiti中表中数据多的时候，新增或者修改。
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		System.out.println("查询所有group开始时间"+sFormat.format(new Date()));
		de.clearSql();
  		de.addSql(" select v.id_ id,v.name_ name_ from o2a.act_id_group_view v ");
  		de.addSql(" where not exists (");
  		de.addSql(" select 1 from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_group m where v.id_ =m.id_ and  v.name_= m.name_ ) ");

		DataStore vds1 = de.query();
		System.out.println("查询所有group结束时间"+sFormat.format(new Date()));

		for (int i = 0; i < vds1.rowCount(); i++) {
			String id = vds1.getString(i, "id");
			String name = vds1.getString(i, "name_");

			de.clearSql();
  			de.addSql(" select 1 from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_group where id_ = :id ");
			de.setString("id", id);
			DataStore vds11 = de.query();

			if (vds11 == null || vds11.rowCount() == 0) {
				System.out.println("插入group开始时间"+sFormat.format(new Date()));
				// 向activiti 中的group表中插数据
				de.clearSql();
				de.addSql(" insert into "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_group ");
				de.addSql("   (id_, rev_, name_, type_) ");
				de.addSql(" select v.id_,c2n(v.rev_),v.name_,v.type_ ");
				de.addSql(" from o2a.act_id_group_view v ");
				de.addSql(" where v.id_ = :id ");
				de.setString("id", id);
				de.update();
				System.out.println("插入group：" + id);
				System.out.println("插入group结束时间"+sFormat.format(new Date()));

			} else {
				System.out.println("更新group开始时间"+sFormat.format(new Date()));
				// 更新activiti中的group表更新
				de.clearSql();
  				de.addSql(" update "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_group ");
  				de.addSql(" set name_=:name ");
  				de.addSql(" where id_ = :id ");
				de.setString("name", name);
				de.setString("id", id);
				de.update();
				System.out.println("更新group：" + id);
				System.out.println("更新group结束时间"+sFormat.format(new Date()));
			}

		}
		// 处理activiti中的group表比odssu中view表中多的时候，这时候删除activiti中多余的数据
		// 因为执行完上述程序之后，理论上group表中的数据已经是最新的了，如果activiti中再有多余的数据，则被认为是垃圾数据，会被删除
//		de.clearSql();
//  		de.addSql(" select m.id_ id from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_group m ");
//  		de.addSql(" where not exists( ");
//  		de.addSql(" select 1 from o2a.act_id_group_view n where m.id_= n.id_ ) ");
//
//		DataStore vdsmin = de.query();
//
//		for (int i = 0; i < vdsmin.rowCount(); i++) {
//			String id = vdsmin.getString(i, "id");
//
//			if ("management".equals(id) == false && "sales".equals(id) == false
//					&& "marketing".equals(id) == false
//					&& "engineering".equals(id) == false
//					&& "user".equals(id) == false
//					&& "admin".equals(id) == false) {
//				de.clearSql();
//  				de.addSql(" delete from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership ");
//  				de.addSql(" where group_id_ = :id ");
//				de.setString("id", id);
//				de.update();
//
//				de.clearSql();
//  				de.addSql(" delete from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_group v ");
//  				de.addSql(" where v.id_ = :id ");
//				de.setString("id", id);
//				de.update();
//			}
//		}
	}

	/**
	 * 从ODSSU中向activiti中同步数据，处理关于关系的情况。
	 * 
	 * @Description:
	 * @author 刘维秋
	 * @date 2014-7-31
	 * @param para
	 * @throws Exception
	 */
	public static void userGroupManager(DataObject para) throws Exception {
		DE de = DE.getInstance();
		// 处理member 开始================================================
		// 处理at的形式=======================================================

		// view中比old表中的多 AT

		de.clearSql();
  		de.addSql(" select v.user_id_ userid,  ");
  		de.addSql(" v.group_id_ groupid ");
  		de.addSql(" from o2a.act_id_membership_at_view v ");
  		de.addSql(" where not exists  ");
  		de.addSql(" (select 1  ");
  		de.addSql(" from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership o where o.group_id_ like '%@%' and v.user_id_ = o.user_id_ and v.group_id_ = o.group_id_) ");


		DataStore vdsat = de.query();

		for (int i = 0; i < vdsat.rowCount(); i++) {
			String userid = vdsat.getString(i, "userid");
			String groupid = vdsat.getString(i, "groupid");

			// 判断此关系是否同步
			String[] orgrole = groupid.split("@");
			String roleno = orgrole[0];
			String orgno = orgrole[1];

			de.clearSql();
  			de.addSql(" select t.at ");
  			de.addSql(" from   o2a.ir_act_org_role_type t, ");
  			de.addSql("        odsv.roleinfor_view r, ");
  			de.addSql("        odsv.orginfor_view o ");
  			de.addSql(" where  r.roletype = t.roletypeid and o.orgtype = t.orgtypeid ");
  			de.addSql("        and r.roleno = :roleno and o.orgno = :orgno and at = '1' ");
			de.setString("roleno", roleno);
			de.setString("orgno", orgno);
			DataStore vds = de.query();

			if (vds == null || vds.rowCount() == 0) {
				continue;
			}

			de.clearSql();
  			de.addSql(" select 1 from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_user where id_=:userid ");
			de.setString("userid", userid);
			DataStore vds1 = de.query();

			de.clearSql();
  			de.addSql(" select 1 from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_group where id_ = :groupid ");
			de.setString("groupid", groupid);
			DataStore vds11 = de.query();

			de.clearSql();
  			de.addSql(" select 1 from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership where user_id_ = :userid and group_id_ = :groupid ");
			de.setString("userid", userid);
			de.setString("groupid", groupid);
			DataStore vdsa = de.query();

			if (vds1.rowCount() > 0 && vds11.rowCount() > 0
					&& vdsa.rowCount() == 0) {
				de.clearSql();
  				de.addSql(" insert into "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership ");
  				de.addSql("       (user_id_,group_id_) ");
  				de.addSql(" values(   :userid    ,   :groupid     ) ");
				de.setString("userid", userid);
				de.setString("groupid", groupid);
				de.update();
			}
		}

		// 处理activiti中关系表中数据比ODSSU中的view中多的时候,activiti中多的被认为是多余的，要删除
		de.clearSql();
  		de.addSql(" select o.user_id_ userid,   ");
  		de.addSql(" o.group_id_ groupid ");
  		de.addSql(" from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership o where o.group_id_ like '%@%' ");
  		de.addSql(" and not exists ");
  		de.addSql(" (select 1 ");
  		de.addSql(" from o2a.act_id_membership_at_view v where o.user_id_=v.user_id_ and o.group_id_=v.group_id_) ");


		DataStore vds3 = de.query();

		for (int i = 0; i < vds3.rowCount(); i++) {
			String userid = vds3.getString(i, "userid");
			String groupid = vds3.getString(i, "groupid");

			if ((userid.equals("kermit") && groupid.equals("admin"))
					|| (userid.equals("kermit") && groupid.equals("engineering"))
					|| (userid.equals("kermit") && groupid.equals("management"))
					|| (userid.equals("kermit") && groupid.equals("marketing"))
					|| (userid.equals("kermit") && groupid.equals("sales"))
					|| (userid.equals("kermit") && groupid.equals("user"))) {
			} else {
				de.clearSql();
  				de.addSql(" delete from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership  ");
  				de.addSql(" 	where user_id_ = :userid and group_id_ = :groupid ");
				de.setString("userid", userid);
				de.setString("groupid", groupid);
				de.update();
			}
		}

		// 处理upon的形式======================================================================

		// view中比old表中的多 UPON
		de.clearSql();
  		de.addSql(" select v.user_id_ userid,  ");
  		de.addSql(" v.group_id_ groupid ");
  		de.addSql(" from o2a.act_id_membership_upon_view v ");
  		de.addSql(" where not exists  ");
  		de.addSql(" (select 1 ");
  		de.addSql(" 	from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership o  where o.group_id_ like '%#%' and v.user_id_ =o.user_id_ and v.group_id_ = o.group_id_) ");

		DataStore vdsupon = de.query();

		for (int i = 0; i < vdsupon.rowCount(); i++) {
			String userid = vdsupon.getString(i, "userid");
			String groupid = vdsupon.getString(i, "groupid");

			// 判断此关系是否同步
			String[] orgrole = groupid.split("#");
			String roleno = orgrole[0];
			String orgno = orgrole[1];

			de.clearSql();
  			de.addSql(" select t.upon ");
  			de.addSql(" from   o2a.ir_act_org_role_type t, ");
  			de.addSql("        odsv.roleinfor_view r, ");
  			de.addSql("        odsv.orginfor_view o ");
  			de.addSql(" where  r.roletype = t.roletypeid and o.orgtype = t.orgtypeid ");
  			de.addSql("        and r.roleno = :roleno and o.orgno = :orgno and upon = '1' ");
			de.setString("roleno", roleno);
			de.setString("orgno", orgno);
			DataStore vds = de.query();

			if (vds == null || vds.rowCount() == 0) {
				continue;
			}

			de.clearSql();
  			de.addSql(" select 1 from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_user where id_=:userid ");
			de.setString("userid", userid);
			DataStore vds1 = de.query();

			de.clearSql();
  			de.addSql(" select 1 from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_group where id_ = :groupid ");
			de.setString("groupid", groupid);
			DataStore vds11 = de.query();

			de.clearSql();
  			de.addSql(" select 1 from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership where user_id_ = :userid and group_id_ = :groupid ");
			de.setString("userid", userid);
			de.setString("groupid", groupid);
			DataStore vdsa = de.query();

			if (vds1.rowCount() > 0 && vds11.rowCount() > 0
					&& vdsa.rowCount() == 0) {
				de.clearSql();
  				de.addSql(" insert into "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership ");
  				de.addSql("       (user_id_,group_id_) ");
  				de.addSql(" values(   :userid    ,   :groupid     ) ");
				de.setString("userid", userid);
				de.setString("groupid", groupid);
				de.update();
			}
		}

		// 处理old表比view中多的时候
		de.clearSql();
  		de.addSql(" 	select o.user_id_ userid,   ");
  		de.addSql(" o.group_id_ groupid ");
  		de.addSql(" from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership o where o.group_id_ like '%#%' ");
  		de.addSql(" and not exists ");
  		de.addSql(" (select 1  ");
  		de.addSql(" from o2a.act_id_membership_upon_view v where o.user_id_ =v.user_id_ and o.group_id_=v.group_id_) ");

		DataStore vds4 = de.query();

		for (int i = 0; i < vds4.rowCount(); i++) {
			String userid = vds4.getString(i, "userid");
			String groupid = vds4.getString(i, "groupid");

			if ((userid.equals("kermit") && groupid.equals("admin"))
					|| (userid.equals("kermit") && groupid.equals("engineering"))
					|| (userid.equals("kermit") && groupid.equals("management"))
					|| (userid.equals("kermit") && groupid.equals("marketing"))
					|| (userid.equals("kermit") && groupid.equals("sales"))
					|| (userid.equals("kermit") && groupid.equals("user"))) {
			} else {
				de.clearSql();
  				de.addSql(" delete from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership  ");
  				de.addSql(" 	where user_id_ = :userid and group_id_ = :groupid ");
				de.setString("userid", userid);
				de.setString("groupid", groupid);
				de.update();
			}
		}

		// view中比old表中的多 UNDER=============================================
		de.clearSql();
  		de.addSql(" select v.user_id_ userid,  ");
  		de.addSql(" v.group_id_ groupid ");
  		de.addSql(" from o2a.act_id_membership_under_view v where v.group_id_ like '%&%' ");
  		de.addSql(" and not exists  ");
  		de.addSql(" (select 1  ");
  		de.addSql(" 	from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership o where o.group_id_ like '%&%' and v.user_id_ =o.user_id_ and v.group_id_ =o.group_id_ )  ");

		DataStore vdsunder = de.query();

		for (int i = 0; i < vdsunder.rowCount(); i++) {
			String userid = vdsunder.getString(i, "userid");
			String groupid = vdsunder.getString(i, "groupid");

			// 判断此关系是否同步
			String[] orgrole = groupid.split("&");
			String roleno = orgrole[0];
			String orgno = orgrole[1];

			de.clearSql();
  			de.addSql(" select t.under ");
  			de.addSql(" from   o2a.ir_act_org_role_type t, ");
  			de.addSql("        odsv.roleinfor_view r, ");
  			de.addSql("        odsv.orginfor_view o ");
  			de.addSql(" where  r.roletype = t.roletypeid and o.orgtype = t.orgtypeid ");
  			de.addSql("        and r.roleno = :roleno and o.orgno = :orgno and under = '1' ");
			de.setString("roleno", roleno);
			de.setString("orgno", orgno);
			DataStore vds = de.query();

			if (vds == null || vds.rowCount() == 0) {
				continue;
			}

			de.clearSql();
  			de.addSql(" select 1 from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_user where id_=:userid ");
			de.setString("userid", userid);
			DataStore vds1 = de.query();

			de.clearSql();
  			de.addSql(" select 1 from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_group where id_ = :groupid ");
			de.setString("groupid", groupid);
			DataStore vds11 = de.query();

			de.clearSql();
  			de.addSql(" select 1 from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership where user_id_ = :userid and group_id_ = :groupid ");
			de.setString("userid", userid);
			de.setString("groupid", groupid);
			DataStore vdsa = de.query();

			if (vds1.rowCount() > 0 && vds11.rowCount() > 0
					&& vdsa.rowCount() == 0) {
				de.clearSql();
  				de.addSql(" insert into "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership ");
  				de.addSql("       (user_id_,group_id_) ");
  				de.addSql(" values(   :userid    ,   :groupid     ) ");
				de.setString("userid", userid);
				de.setString("groupid", groupid);
				de.update();
			}
		}

		// 处理old表比view中多的时候
		de.clearSql();
  		de.addSql(" 	select o.user_id_ userid,   ");
  		de.addSql(" o.group_id_ groupid ");
  		de.addSql(" from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership o where o.group_id_ like '%&%' ");
  		de.addSql(" and not exists ");
  		de.addSql(" (select 1  ");
  		de.addSql(" from o2a.act_id_membership_under_view v where o.user_id_ = v.user_id_ and o.group_id_=v.group_id_ ) ");

		DataStore vds5 = de.query();

		for (int i = 0; i < vds5.rowCount(); i++) {
			String userid = vds5.getString(i, "userid");
			String groupid = vds5.getString(i, "groupid");

			if ((userid.equals("kermit") && groupid.equals("admin"))
					|| (userid.equals("kermit") && groupid.equals("engineering"))
					|| (userid.equals("kermit") && groupid.equals("management"))
					|| (userid.equals("kermit") && groupid.equals("marketing"))
					|| (userid.equals("kermit") && groupid.equals("sales"))
					|| (userid.equals("kermit") && groupid.equals("user"))) {
			} else {
				de.clearSql();
  				de.addSql(" delete from "+ ProcessConstants.ACTIVITI_DB_NAME +".act_id_membership  ");
  				de.addSql(" 	where user_id_ = :userid and group_id_ = :groupid ");
				de.setString("userid", userid);
				de.setString("groupid", groupid);
				de.update();
			}
		}
	}
	
}
