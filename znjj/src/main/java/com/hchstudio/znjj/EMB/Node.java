package com.hchstudio.znjj.EMB;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

/*
 * 单个节点的数据表示 
 */
public class Node {

	private static final String TAG = "Node";

	/* 结点类型,协调器，路由，设备 */
	final static int ZB_NODE_TYPE_COORDINATOR = 0;		// 协调器
	final static int ZB_NODE_TYPE_ROUTER = 1;			// 路由器
	final static int ZB_NODE_TYPE_ENDDEVICE = 2;		// 终端结点

	/* 设备类型 */
	public static final byte DEV_NONE = 0x00;			// 未知
	public static final byte DEV_InforTemp = 0x01;		// 温湿度
	public static final byte DEV_LIGHT = 0x03;			// 继电器
	public static final byte DEV_InFRARED = 0x04;		// 人体红外
	public static final byte DEV_InforFire = 0x05;		// 可燃气体
	public static final byte DEV_InfrarCon = 0x10;		// 红外遥控

	public List<Node> _childNode; // 孩子结点链表，该节点下面包含结点
	int[] childs = {};

	int mHardVer; // 硬件商
	int mSoftVer; // 软件商

	byte[] mIEEEAddr; // MAC地址 ,字节数组
	public int mNetAddr; // 网络地址

	int mNodeType; // 节点类型
	public int mDevType;  // 设备类型

	public String state; //节点状态

	/* 显示相关数据 */
	int mDeepth; // 结点与子树的深度,包括顶点有多少层

	Node(int netAddr, int nodeType, byte[] ninfo){
		mNetAddr = netAddr;
		mNodeType = nodeType;
		mIEEEAddr = new byte[8]; // 包含8个字节的字节数组
		_childNode = new LinkedList<Node>();

		Log.i(TAG,"ninfo---"+ETool.byteTostring(ninfo));
		int tmp, off = 0;
		tmp = ETool.builduInt(ninfo[off], ninfo[off + 1]); // addr
		if (tmp != netAddr) {
			Log.d(TAG, "net add is not equl...");
			return;
		}
		off += 2;
		tmp = ETool.builduInt(ninfo[off], ninfo[off + 1]); // cmd
		if (tmp != 0x8001) {
			Log.d(TAG, "response cmd not euql...");
			return;
		}
		off += 2;
		if (ninfo[off] != 0) { // read status
			Log.d(TAG, "read status is not 0");
			return;
		}
		off += 1;
		while (off < ninfo.length) {
			tmp = ETool.builduInt(ninfo[off], ninfo[off + 1]);
			off += 2;
			switch (tmp) {
				case 0x0001: // 硬件商
					mHardVer = ETool.builduInt(ninfo[off], ninfo[off + 1]);
					off += 2;
					break;
				case 0x0002:// 软件商
					mSoftVer = ETool.builduInt(ninfo[off], ninfo[off + 1]);
					off += 2;
					break;
				case 0x0005:// 设备类型
					mDevType = ninfo[off];
					off += 1;
					break;
				case 0x0014: // M A C 地 址 ,字节数组
					for (int j = 0; j < 8; j++) {
						mIEEEAddr[j] = ninfo[off + j];
					}
					off += 8;

					break;
				case 0x0015:
					int assocCnt = ninfo[off];
					off += 1;
					if (assocCnt != 0) {
						mNodeType = Node.ZB_NODE_TYPE_ROUTER;
						childs = new int[assocCnt];
						for (int j = 0; j < assocCnt; j++) {
							childs[j] = ETool.builduInt(ninfo[off], ninfo[off + 1]);
							Log.i("ZbThread", "childs---"+String.valueOf(childs[j]));
							off += 2;
						}
					}
					break;
			}
		}
	}

	// 获得结点字符串描述
	static String getNodeTypeString(Node n) {
		switch (n.mNodeType) {
			case Node.ZB_NODE_TYPE_COORDINATOR:
				return "协调器";
			case Node.ZB_NODE_TYPE_ROUTER:
				return "路由器";
			default:
				return "终端结点";
		}
	}

	static String getHardVer(Node n) {
		return String.format("%02X.%02X", (n.mHardVer >> 8) & 0xff,
				n.mHardVer & 0xff);
	}

	static String getSoftVer(Node n) {
		return String.format("%02X.%02X", (n.mSoftVer >> 8) & 0xff,
				n.mSoftVer & 0xff);
	}

	@Override
	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this,Node.class);
	}

	public String toChildStr(){
		Gson gson = new Gson();
		Type type = new TypeToken<List<Node>>(){}.getType();
		for (Node nd : _childNode){
			Log.i(TAG,nd.mDevType + "---" + nd.mNetAddr);
		}
		return gson.toJson(_childNode, type);
	}

	public static String toChildStr(List<Node> nodes){
		Gson gson = new Gson();
		Type type = new TypeToken<List<Node>>(){}.getType();
		for (Node nd : nodes){
			Log.i(TAG,nd.mDevType + "---" + nd.mNetAddr);
		}
		return gson.toJson(nodes, type);
	}

	public static List<Node> fromChildStr(String gsonStr){
		Gson gson = new Gson();
		Type type = new TypeToken<List<Node>>(){}.getType();
		return gson.fromJson(gsonStr,type);
	}
}
