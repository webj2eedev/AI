package com.dw.odssu.ws.org.wgpz;

import com.dareway.framework.exception.AppException;
import com.dareway.framework.taglib.multiselecttree.MultiSelectTreeDS;
import com.dareway.framework.taglib.multiselecttree.MultiSelectTreeItem;
import com.dareway.framework.util.DataObject;
import com.dareway.framework.util.DataStore;
import com.dareway.framework.workFlow.BPO;
import com.dw.hsuods.ws.org.util.FnDataSource;
import com.dw.hsuods.ws.org.util.OuterDutyPDDPDataSource;

public class GwlcrwGnrwMtreeBPO extends BPO{

	public DataObject createTreepddp(DataObject para) throws AppException {
		MultiSelectTreeDS mTreeDS = new MultiSelectTreeDS();
		String piid = para.getString("piid");

		OuterDutyPDDPDataSource odPdDpFolderTree = new OuterDutyPDDPDataSource();
		//  获取PD
		DataStore pdVds = odPdDpFolderTree.getPdFromWS(piid, null);
		pdVds.sort("pdid");
		//  获取DP
		DataStore dpVds = odPdDpFolderTree.getDpFromWS(piid,null);
		dpVds.sort("pdid");
		//  初始化FolderDS
		DataStore folderVds = odPdDpFolderTree.getPdFolderFromWS(piid);

		// 将工单表中的数据对应的选中信息编辑到为创建树而准备的datastore中(pdVds,dpVds,folderVds)
		for (int i = 0; i < dpVds.rowCount(); i++) {
			// 如果存在，则将其选中信息置为1
			if (dpVds.getString(i, "selectflag").equals("1")) {
				dpVds.put(i, "checkBoxValue", "2");
			} else {
				dpVds.put(i, "checkBoxValue", "0");
			}
		}
		for (int i = 0; i < folderVds.rowCount(); i++) {
			folderVds.put(i, "checkBoxValue", "0");
		}
		for (int i = 0; i < pdVds.rowCount(); i++) {
			pdVds.put(i, "checkBoxValue", "0");
		}

		// 添加根节点
		MultiSelectTreeItem rootitem = mTreeDS.addItem(OuterDutyPDDPDataSource.PDFOLDER_ROOT_FOLDERNO, null, "流程任务", null, "0");
		rootitem.setIconId("ods_dutymtree_lcrwml");

		// 添加目录各级节点
		for (int i = 0; i < folderVds.rowCount(); i++) {
			String folderno = folderVds.getString(i, "folderno");
			String folderlabel = folderVds.getString(i, "folderlabel");
			String pfolderno = folderVds.getString(i, "pfolderno");
			String checkBoxValue = folderVds.getString(i, "checkBoxValue");
			MultiSelectTreeItem item = mTreeDS.addItem(folderno, pfolderno, folderlabel, null, checkBoxValue);
			item.setIconId("ods_dutymtree_lcrwml");
		}

		// 添加流程和岗位任务节点
		int dprow = 0;
		for (int i = 0; i < pdVds.rowCount(); i++) {
			String pdid = pdVds.getString(i, "pdid");
			String folderno = pdVds.getString(i, "folderno");
			String pdalias = pdVds.getString(i, "pdalias");
			String checkBoxValue = pdVds.getString(i, "checkBoxValue");
			int k = dprow;
			// 查看dpid个数
			for (k = dprow; k < dpVds.rowCount(); k++) {
				if (!dpVds.getString(k, "pdid").equals(pdid)) {
					break;
				}
			}

			// 当只有一个流程岗位任务时，将其与流程任务合并
			if (k - dprow == 1) {
				
				String toccode = dpVds.getString(dprow, "toccode");
				String nodeId = pdid + "." + dpVds.getString(dprow, "dptdid");
				
				if (toccode != null&&!toccode.equals("")&&!toccode.equals("-")) {
					nodeId = nodeId+"@"+toccode;
				}
				
				String parentNodeId = folderno;
				String nodeLabel = pdalias + "."+ dpVds.getString(dprow, "dptdlabel");
				String dpcheckBoxValue = dpVds.getString(dprow, "checkBoxValue");
				MultiSelectTreeItem item = mTreeDS.addItem(nodeId, parentNodeId, nodeLabel, null, dpcheckBoxValue);
				item.setIconId("ods_dutymtree_lcgwrw");
			}
			// 多个岗位任务时，先将流程节点加上，然后再分别将岗位任务节点加上
			if (k - dprow > 1) {
				MultiSelectTreeItem itemlc = mTreeDS.addItem(pdid, folderno, pdalias, null, checkBoxValue);
				itemlc.setIconId("ods_dutymtree_lcrw");
				for (int j = dprow; j < k; j++) {
					String nodeId = dpVds.getString(j, "dptdid");
					String toccode = dpVds.getString(j, "toccode");
					
					if (toccode != null&&!toccode.equals("")&&!toccode.equals("-")) {
						nodeId = nodeId+"@"+toccode;
					}
					
					String parentNodeId = pdid;
					String nodeLabel = dpVds.getString(j, "dptdlabel");
					String dpcheckBoxValue = dpVds.getString(j, "checkBoxValue");
					MultiSelectTreeItem item = mTreeDS.addItem(nodeId, parentNodeId, nodeLabel, null, dpcheckBoxValue);
					item.setIconId("ods_dutymtree_gwrw");
				}
			}
			dprow = k;

		}

		DataObject result = DataObject.getInstance();

		result.put("pddpds", mTreeDS);

		return result;
	}

	/**
	 * 描述：岗位职能调整mtree加载烟台客户化
	 * author: sjn
	 * date: 2017年11月3日
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject createTreepddp_3706(DataObject para) throws AppException {
		MultiSelectTreeDS mTreeDS = new MultiSelectTreeDS();
		String piid = para.getString("piid");

		OuterDutyPDDPDataSource odPdDpFolderTree = new OuterDutyPDDPDataSource();
		//  获取PD
		DataStore pdVds = odPdDpFolderTree.getPdFromWS(piid, null);
		pdVds.sort("pdid");
		//  获取DP
		DataStore dpVds = odPdDpFolderTree.getDpFromWS_3706(piid,null);
		dpVds.sort("pdid");
		//  初始化FolderDS
		DataStore folderVds = odPdDpFolderTree.getPdFolderFromWS(piid);

		// 将工单表中的数据对应的选中信息编辑到为创建树而准备的datastore中(pdVds,dpVds,folderVds)
		for (int i = 0; i < dpVds.rowCount(); i++) {
			// 如果存在，则将其选中信息置为1
			if (dpVds.getString(i, "selectflag").equals("1")) {
				dpVds.put(i, "checkBoxValue", "2");
			} else {
				dpVds.put(i, "checkBoxValue", "0");
			}
		}
		for (int i = 0; i < folderVds.rowCount(); i++) {
			folderVds.put(i, "checkBoxValue", "0");
		}
		for (int i = 0; i < pdVds.rowCount(); i++) {
			pdVds.put(i, "checkBoxValue", "0");
		}

		// 添加根节点
		MultiSelectTreeItem rootitem = mTreeDS.addItem(OuterDutyPDDPDataSource.PDFOLDER_ROOT_FOLDERNO, null, "流程任务", null, "0");
		rootitem.setIconId("ods_dutymtree_lcrwml");

		// 添加目录各级节点
		for (int i = 0; i < folderVds.rowCount(); i++) {
			String folderno = folderVds.getString(i, "folderno");
			String folderlabel = folderVds.getString(i, "folderlabel");
			String pfolderno = folderVds.getString(i, "pfolderno");
			String checkBoxValue = folderVds.getString(i, "checkBoxValue");
			MultiSelectTreeItem item = mTreeDS.addItem(folderno, pfolderno, folderlabel, null, checkBoxValue);
			item.setIconId("ods_dutymtree_lcrwml");
		}

		// 添加流程和岗位任务节点
		int dprow = 0;
		for (int i = 0; i < pdVds.rowCount(); i++) {
			String pdid = pdVds.getString(i, "pdid");
			String folderno = pdVds.getString(i, "folderno");
			String pdalias = pdVds.getString(i, "pdalias");
			String checkBoxValue = pdVds.getString(i, "checkBoxValue");
			int k = dprow;
			// 查看dpid个数
			for (k = dprow; k < dpVds.rowCount(); k++) {
				if (!dpVds.getString(k, "pdid").equals(pdid)) {
					break;
				}
			}

			// 当只有一个流程岗位任务时，将其与流程任务合并
			if (k - dprow == 1) {
				
				String toccode = dpVds.getString(dprow, "toccode");
				String nodeId = pdid + "." + dpVds.getString(dprow, "dptdid");
				
				if (toccode != null&&!toccode.equals("")&&!toccode.equals("-")) {
					nodeId = nodeId+"@"+toccode;
				}
				
				String parentNodeId = folderno;
				String nodeLabel = "";
				nodeLabel = pdalias + "." + dpVds.getString(dprow, "dptdlabel");
				String dpcheckBoxValue = dpVds.getString(dprow, "checkBoxValue");
				MultiSelectTreeItem item = mTreeDS.addItem(nodeId, parentNodeId, nodeLabel, null, dpcheckBoxValue);
				item.setIconId("ods_dutymtree_lcgwrw");
			}
			// 多个岗位任务时，先将流程节点加上，然后再分别将岗位任务节点加上
			if (k - dprow > 1) {
				MultiSelectTreeItem itemlc = mTreeDS.addItem(pdid, folderno, pdalias, null, checkBoxValue);
				itemlc.setIconId("ods_dutymtree_lcrw");
				for (int j = dprow; j < k; j++) {
					String nodeId = dpVds.getString(j, "dptdid");
					String toccode = dpVds.getString(j, "toccode");
					
					if (toccode != null&&!toccode.equals("")&&!toccode.equals("-")) {
						nodeId = nodeId+"@"+toccode;
					}
					
					String parentNodeId = pdid;
					String nodeLabel = dpVds.getString(j, "dptdlabel");
					String dpcheckBoxValue = dpVds.getString(j, "checkBoxValue");
					MultiSelectTreeItem item = mTreeDS.addItem(nodeId, parentNodeId, nodeLabel, null, dpcheckBoxValue);
					item.setIconId("ods_dutymtree_gwrw");
				}
			}
			dprow = k;

		}

		DataObject result = DataObject.getInstance();

		result.put("pddpds", mTreeDS);

		return result;
	}
	
	
	public DataObject createTreefn(DataObject para) throws AppException {
		DataObject result = DataObject.getInstance();
		MultiSelectTreeDS mTreeDS = new MultiSelectTreeDS();
		String piid = para.getString("piid");

		FnDataSource fnDataSource = new FnDataSource();
		// 1 初始化 功能目录Vds
		DataStore dsfnfolder = fnDataSource.getFnFolderFromWS(piid);
		// 2 初始化 功能Vds
		DataStore fnVds = fnDataSource.getFnFromWS(piid, null,null);

		if (dsfnfolder == null||fnVds==null) {
			// 添加根节点
			MultiSelectTreeItem rootitem = mTreeDS.addItem("root", null, "功能任务", null, "0");
			rootitem.setIconId("ods_dutymtree_gnrwml");
			result.put("fnds", mTreeDS);

			return result;
		}

		 // 先将表中所有的checkBoxValue都设置为"0"
		 for (int i = 0; i < dsfnfolder.rowCount(); i++) {
		 dsfnfolder.put(i, "checkBoxValue", "0");
		 }

		// 将工单表中的数据对应的选中信息编辑到为创建树而准备的datastore中(pdVds,dpVds,folderVds)
		for (int i = 0; i < fnVds.rowCount(); i++) {
			// 找出表中对应的functionid所在位置
			if (fnVds.getString(i, "selectflag").equals("1")) {
				fnVds.put(i, "checkBoxValue", "2");
			}else {
				fnVds.put(i, "checkBoxValue", "0");
			}
		}

		// 添加根节点
		MultiSelectTreeItem rootitem = mTreeDS.addItem("root", null, "功能任务", null, "0");
		rootitem.setIconId("ods_dutymtree_gnrwml");

		// 添加目录各级节点
		for (int i = 0; i < dsfnfolder.rowCount(); i++) {
			String folderno = dsfnfolder.getString(i, "folderid");
			String folderlabel = dsfnfolder.getString(i, "folderlabel");
			String pfolderno = dsfnfolder.getString(i, "pfolderid");
			String checkBoxValue = dsfnfolder.getString(i, "checkBoxValue");
			if (pfolderno == null || pfolderno.equals("")) {
				MultiSelectTreeItem item = mTreeDS.addItem(folderno, "root", folderlabel, null, checkBoxValue);
				item.setIconId("ods_dutymtree_gnrwml");
				continue;
			}
			MultiSelectTreeItem item1 = mTreeDS.addItem(folderno, pfolderno, folderlabel, null, checkBoxValue);
			item1.setIconId("ods_dutymtree_gnrwml");
		}

		// 添加功能任务节点
		for (int i = 0; i < fnVds.rowCount(); i++) {
			String functionid = fnVds.getString(i, "functionid");
			String functionname = fnVds.getString(i, "functionname");
			String folderno = fnVds.getString(i, "folderid");
			String checkBoxValue = fnVds.getString(i, "checkBoxValue");
			MultiSelectTreeItem item = mTreeDS.addItem(functionid, folderno, functionname, null, checkBoxValue);
			item.setIconId("ods_dutymtree_gnrw");
		}
		result.put("fnds", mTreeDS);

		return result;
	}
	
	/**
	 * 描述：创建功能目录烟台客户化
	 * author: sjn
	 * date: 2018年1月3日
	 * @param para
	 * @return
	 * @throws AppException
	 */
	public DataObject createTreefn_3706(DataObject para) throws AppException {
		DataObject result = DataObject.getInstance();
		MultiSelectTreeDS mTreeDS = new MultiSelectTreeDS();
		String piid = para.getString("piid");
		
		FnDataSource fnDataSource = new FnDataSource();
		// 1 初始化 功能目录Vds
		DataStore dsfnfolder = fnDataSource.getFnFolderFromWS(piid);
		// 2 初始化 功能Vds
		DataStore fnVds = fnDataSource.getFnFromWS_3706(piid, null,null);
		
		if (dsfnfolder == null||fnVds==null) {
			// 添加根节点
			MultiSelectTreeItem rootitem = mTreeDS.addItem("root", null, "功能任务", null, "0");
			rootitem.setIconId("ods_dutymtree_gnrwml");
			result.put("fnds", mTreeDS);
			
			return result;
		}
		
		// 先将表中所有的checkBoxValue都设置为"0"
		for (int i = 0; i < dsfnfolder.rowCount(); i++) {
			dsfnfolder.put(i, "checkBoxValue", "0");
		}
		
		// 将工单表中的数据对应的选中信息编辑到为创建树而准备的datastore中(pdVds,dpVds,folderVds)
		for (int i = 0; i < fnVds.rowCount(); i++) {
			// 找出表中对应的functionid所在位置
			if (fnVds.getString(i, "selectflag").equals("1")) {
				fnVds.put(i, "checkBoxValue", "2");
			}else {
				fnVds.put(i, "checkBoxValue", "0");
			}
		}
		
		// 添加根节点
		MultiSelectTreeItem rootitem = mTreeDS.addItem("root", null, "功能任务", null, "0");
		rootitem.setIconId("ods_dutymtree_gnrwml");
		
		// 添加目录各级节点
		for (int i = 0; i < dsfnfolder.rowCount(); i++) {
			String folderno = dsfnfolder.getString(i, "folderid");
			String folderlabel = dsfnfolder.getString(i, "folderlabel");
			String pfolderno = dsfnfolder.getString(i, "pfolderid");
			String checkBoxValue = dsfnfolder.getString(i, "checkBoxValue");
			if (pfolderno == null || pfolderno.equals("")) {
				MultiSelectTreeItem item = mTreeDS.addItem(folderno, "root", folderlabel, null, checkBoxValue);
				item.setIconId("ods_dutymtree_gnrwml");
				continue;
			}
			MultiSelectTreeItem item1 = mTreeDS.addItem(folderno, pfolderno, folderlabel, null, checkBoxValue);
			item1.setIconId("ods_dutymtree_gnrwml");
		}
		
		// 添加功能任务节点
		for (int i = 0; i < fnVds.rowCount(); i++) {
			String functionid = fnVds.getString(i, "functionid");
			String functionname = fnVds.getString(i, "functionname");
			String folderno = fnVds.getString(i, "folderid");
			String checkBoxValue = fnVds.getString(i, "checkBoxValue");
			MultiSelectTreeItem item = mTreeDS.addItem(functionid, folderno, functionname, null, checkBoxValue);
			item.setIconId("ods_dutymtree_gnrw");
		}
		result.put("fnds", mTreeDS);
		
		return result;
	}
}
