package com.dw.odssu.ws.org.wgpz;


import java.util.Date;

import com.dareway.framework.exception.AppException;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;

/**
 * 类描述
 * 
 * @author 叶军 2015-6-11
 */
public class WgpzBPO extends BPO{

	public DataObject getMainWSInfor(DataObject para) throws AppException {
  		de.clearSql();
		String piid = para.getString("piid");
		de.clearSql();
  		de.addSql(" select a.faceorgno, b.orgname faceorgname, a.inorgno, ");
  		de.addSql("        c.orgname inorgname, a.roletypeno,  ");
  		de.addSql("        d.typename roletypename,a.rolename ");
  		de.addSql("   from odssuws.outerduty a, ");
  		de.addSql("        odssu.orginfor b, ");
  		de.addSql("        odssu.orginfor c,  ");
  		de.addSql("        odssu.role_type d ");
  		de.addSql("  where a.faceorgno=b.orgno ");
  		de.addSql("        and a.inorgno=c.orgno ");
  		de.addSql("        and a.roletypeno=d.typeno ");
  		de.addSql("        and a.piid=:piid ");
		this.de.setString("piid", piid);
		DataStore dsform = this.de.query();
        DataObject vdo = DataObject.getInstance();
        vdo.put("dsform", dsform);
		return vdo;
	}
	
	/**
	 * 
	 * 方法简介.保存审批信息
	 * @author 叶军     2015-6-12
	 */
	public DataObject saveApprove(DataObject para) throws AppException {
		String tablename = para.getString("tablename");
		String piid = para.getString("piid");
		String spyj = para.getString("spyj");
		String spsm = para.getString("spsm");
		String reviewer = this.getUser().getUserid();
		Date reviewtime = new Date();
  		this.de.clearSql();
		this.de.addSql("update "+tablename+" a ");
  		this.de.addSql(" set spyj = :spyj, ");
  		this.de.addSql("     spsm = :spsm, ");
  		this.de.addSql("     reviewer = :reviewer, ");
  		this.de.addSql("     reviewtime = :reviewtime  ");
  		this.de.addSql(" where a.piid  =:piid ");
  		this.de.setString("spyj", spyj);
		this.de.setString("spsm", spsm);
		this.de.setString("reviewer", reviewer);
		this.de.setDateTime("reviewtime", reviewtime);
		this.de.setString("piid", piid);
		this.de.update();
		
		// 保存一条公共审批
		this.de.clearSql();
		this.de.addSql("delete from odssuws.spinfor ");
  		this.de.addSql("  where piid = :piid and splbdm = :splbdm");
		this.de.setString("piid", piid);
		this.de.setString("splbdm", "gbgw");
		this.de.update();
		
		String spyjdm = "pass";
		if (spyj.equals("1")) {
			spyjdm = "reject";
		}else if (spyj.equals("2")) {
			spyjdm = "revise";
		}
		this.de.clearSql();
		this.de.addSql("insert into odssuws.spinfor (piid,splbdm,spyjdm,spr,spsj,spsm)");
  		this.de.addSql("  values (:piid,:para2,:spyjdm,:reviewer,:reviewtime,:spsm)");
		this.de.setString("piid", piid);
		this.de.setString("para2", "gbgw");
		this.de.setString("spyjdm", spyjdm);
		this.de.setString("reviewer", reviewer);
		this.de.setDateTime("reviewtime", reviewtime);
		this.de.setString("spsm", spsm);
		this.de.update();
		
		return null;
	}
	
	public DataObject getSpInfor(DataObject para) throws AppException {
		String tablename = para.getString("tablename");
		String piid = para.getString("piid");
  		de.clearSql();
  		de.addSql(" select * ");
  		de.addSql("   from :tablename");
  		de.addSql("   where piid = :piid ");
		de.setString("tablename", tablename);
		de.setString("piid", piid);
		DataStore vds = de.query();
		DataObject vdo = DataObject.getInstance();
		vdo.put("yjinfor", vds);
		return vdo;
	}
	
	 DataStore getDs(DataStore dsold, DataStore dsnew, String key) throws AppException {
		DataStore dsSame = DataStore.getInstance();

		int oldCount = dsold.rowCount();
		int newCount = dsnew.rowCount();
		
		//得到相同的ds
		for (int m = 0; m < oldCount; m++) {
			String oldValue = dsold.getString(m, key);
			for (int n = 0; n < newCount; n++) {
				String newValue = dsold.getString(n, key);
				if (oldValue.equals(newValue)) {
					dsSame.addRow(dsnew.getRow(n));
					dsSame.put(dsSame.rowCount()-1, "comment","");
					dsSame.put(dsSame.rowCount()-1, "haveauth","1");
					break;
				}
			}
		}

		// 得到减少的ds
		DataStore dsSub = DataStore.getInstance();

		int sameCount = dsSame.rowCount();
		boolean flag = true;
		for (int m = 0; m < oldCount; m++) {
			flag = true;
			String oldValue = dsold.getString(m, key);
			for (int n = 0; n < sameCount; n++) {
				String sameValue = dsSame.getString(n, key);
				if (oldValue.equals(sameValue)) {
					flag = false;
					break;
				}
			}
			if (flag) {
				dsSub.addRow(dsold.getRow(m));
				dsSub.put(dsSub.rowCount() - 1, "comment", "（-）");
				dsSub.put(dsSub.rowCount()-1, "haveauth","0");
			}
		}
		// 得到增加的的ds
		DataStore dsadd = DataStore.getInstance();
		for (int m = 0; m < newCount; m++) {
			flag = true;
			String newValue = dsnew.getString(m, key);
			for (int n = 0; n < sameCount; n++) {
				String sameValue = dsSame.getString(n, key);
				if (newValue.equals(sameValue)) {
					flag = false;
					break;
				}
			}
			if (flag) {
				dsadd.addRow(dsnew.getRow(m));
				dsadd.put(dsadd.rowCount() - 1, "comment", "（+）");
				dsadd.put(dsadd.rowCount()-1, "haveauth","1");
			}
		}
        DataStore ds = DataStore.getInstance();
		ds.combineDatastore(dsSub);
		ds.combineDatastore(dsadd);
		ds.combineDatastore(dsSame);
		ds.multiSort("haveauth:asc;comment:desc");
		return ds;
	}
}
