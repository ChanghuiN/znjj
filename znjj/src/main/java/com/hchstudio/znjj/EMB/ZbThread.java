package com.hchstudio.znjj.EMB;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ZbThread extends Thread implements ZbProx.ZbProxCallBack {

	private static final String TAG = "ZbThread";

	static final int MSG_CONNECT_STATUS = 1;
	static final int MSG_NEW_NETWORK = 2;
	static final int MSG_CONNECT_DATA = 3;
	static final int MSG_ENDPOINT_INFO = 4;
	private final Object mLockInit = new Object();

	static final int MSG_GET_APP_MSG = 5;

	static final int REQUEST_SEARCH_NETWORK = 0x0100;
	private ZbProx mProx = new ZbProx(this);

	static final int REQUEST_APP_MESSAGE = 0x0300;
	// mMyHandler是一个共享变量
	private MyWorkerHandler mMyHandler;
	private Handler mMainHandler;

	static final int REQUEST_NODE_ENDPOINT_INFO = 0x0200;

	void requestConnect(String host, int port) {
		mProx.connect(host, port);
	}

	void requestDisConnect() {
		mProx.disConnect();
	}

	public int getConnectStatus() {
		return mProx.getConnectStatus();
	}

	/**
	 * 发送“请求查询网络”消息
	 */
	public void requestSerachNetWrok() {
		Message msg = Message.obtain();
		msg.what = REQUEST_SEARCH_NETWORK;
		mMyHandler.sendMessage(msg);
	}

	class MyWorkerHandler extends Handler {
		private static final String TAG = "MyWorkHandler";

		MyWorkerHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) { // 状态机：msg种类
			switch (msg.what) {
				case REQUEST_SEARCH_NETWORK: // 响应“请求查询网络”消息
					doSearchNetWork();
					break;
				case REQUEST_NODE_ENDPOINT_INFO:
					doGetNodeEndPointInfo(msg.arg1, msg.arg2);
					break;
				case REQUEST_APP_MESSAGE:
					doAppMessage(msg.arg1, (byte[]) msg.obj);
					break;
			}
		}
	}

	public void requestAppMessage(int ep, byte[] dat) { // msg就是一个中介
		Message msg = Message.obtain();
		msg.what = REQUEST_APP_MESSAGE;
		msg.arg1 = ep;
		msg.obj = dat;
		mMyHandler.sendMessage(msg);
	}

	public ZbThread(Handler hd) {
		super();
		mMainHandler = hd;
		start();
		synchronized (mLockInit) {
			while (mMyHandler == null) {
				try {
					mLockInit.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void run() // 线程处理的内容
	{
		Looper.prepare();
		synchronized (mLockInit) {
			mMyHandler = new MyWorkerHandler(Looper.myLooper());
			mLockInit.notifyAll();
		}
		Looper.loop();
	}

	public void OnconnectCallBack(boolean st) {
		Log.i("ZbThread","OnconnectCallBack---");
		Message msg = Message.obtain();
		msg.what = this.MSG_CONNECT_STATUS;
		msg.arg1 = st ? 1 : 0;
		mMainHandler.sendMessage(msg);
	}

	public void OnDataCallBack(int req, byte[] dat) {
		Log.i("ZbThread","OnDataCallBack---");
		Log.d(TAG, "Data" + ETool.byteTostring(dat));
		Message msg = Message.obtain();
		msg.what = this.MSG_CONNECT_DATA;
		msg.arg1 = req;
		msg.obj = dat;
		mMainHandler.sendMessage(msg);
	}

	/**
	 * //建立ZigBee
	 *
	 * @param pa
	 * @param cli
	 */
	private void buildNetWork(Node pa, int[] cli) {
		for (int i = 0; i < cli.length; i++) {
			/* get child i info */
			try {
				Thread.currentThread();
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			byte[] ninfo = mProx.syncRequestSYS_APP_MSG(2,
					new byte[] {
							(byte) (cli[i] >> 8),(byte) cli[i], // addr
							0x00,0x01, // cmd
							0x00, 0x01, 0x00, 0x02, 0x00, 0x05, 0x00, 0x14,
							0x00, 0x15 });
			Log.i("ZbThread", "child-"+i+"---"+ ETool.byteTostring(ninfo));
			if (ninfo == null || ninfo.length < 29) {
				Log.d(TAG, "**** get node " + cli[i] + " info fail.");
				return;
			}

			Node nd = new Node(cli[i], Node.ZB_NODE_TYPE_ENDDEVICE, ninfo);
			pa._childNode.add(nd);
		}
	}

	/**
	 * 当用户第一次打开程序，或是从菜单中选择搜索的时候，Zigbee 网络 TOP图生成模块首先检 查网络连接，当网络正常连接到 Zigbee
	 * 智能网关后，首先发送获取协调器节点信息指令，获取到 协调器节点信息。之后将协调器节点通过绘图子程序在屏幕上把协调器显示出来
	 */
	private void doSearchNetWork() {
		// devinfo = mProx.syncRequestSYS_GET_DEVICE_INFO();
		// ninfo!=null，则 ninfo 中保存获取到的协调器信息器信息，
		// 否则获取协调器信息失败，zigbee 网络搜索结束
		byte[] ninfo = mProx.syncRequestSYS_APP_MSG(2, new byte[] {
				(byte) (0 >> 8), (byte) 0, // addr
				0x00, 0x01, // cmd
				0x00, 0x01, 0x00, 0x02, 0x00, 0x05, 0x00, 0x14, 0x00, 0x15 });
		Log.i("ZbThread","doSearchNetWork---"+ ETool.byteTostring(ninfo));
		if (ninfo == null || ninfo.length < 29) {
			/* get devinfo fail */
			Log.e(TAG, "Can't get the root device info.");// 协调器
			Message msg = Message.obtain();
			msg.what = MSG_NEW_NETWORK;
			msg.arg1 = -1;
			mMainHandler.sendMessage(msg);
			return;
		}

		Node nd = new Node(0, Node.ZB_NODE_TYPE_COORDINATOR, ninfo);

		/**
		 * 当找到协调器后，程序通过查找与协调器协调器直接连接的相关节点， 然后递归搜索，最终搜索完整个 网络并绘制出 Zigbee 网络的 TOP
		 * 结构。 mTree 代表的是协调器结点
		 */
		buildNetWork(nd, nd.childs);

		/* notify ui */
		Message msg = Message.obtain();
		msg.what = MSG_NEW_NETWORK;
		msg.arg1 = 0;
		msg.obj = nd;
		mMainHandler.sendMessage(msg);
	}

	private void doGetNodeEndPointInfo(int addr, int ep) {
		byte[] info;
		info = mProx.syncRequestZDO_SIMPLEDESCRIPTOR_REQUEST(addr, addr, ep);
		Message msg = Message.obtain();
		msg.what = MSG_ENDPOINT_INFO;
		if (info == null || info[0] != 0) {
			msg.arg1 = 1;
		} else {
			msg.arg1 = 0;
			msg.obj = info;
		}
		mMainHandler.sendMessage(msg);
	}

	/**
	 * 处理信息
	 *
	 * @param ep
	 * @param dat
	 */
	private void doAppMessage(int ep, byte[] dat) {
		byte[] info;
		info = mProx.syncRequestSYS_APP_MSG(ep, dat);
		if (info != null) {
			Log.d(TAG, "doAppMessage:" + ETool.byteTostring(info));
		} else {
			Log.d(TAG, "appMessage request timeout...");
		}
		Message msg = Message.obtain();
		msg.what = MSG_GET_APP_MSG;
		msg.obj = info;

		mMainHandler.sendMessage(msg);
	}

}
