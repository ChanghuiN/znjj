package com.hchstudio.znjj.EMB;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ZbLink implements Runnable {
	private static final String TAG = "ZbWorker";

	private final Object mLockInit = new Object();
	private final Object mLockConn = new Object();

	//连接状态
	public static final int CONNECT_DISCONNECT = 0; // 已关闭
	public static final int CONNECT_CONNECTING = 1; // 正在连接
	public static final int CONNECT_CONNECTED = 2; // 已连接
	public static final int CONNECT_CONNECTCLOSEING = 3; // 正在关闭

	// 三种请求
	private static final int REQUEST_CONNECT = 1;
	private static final int REQUEST_DISCONNECT = 2;
	private static final int REQUEST_SENDDATA = 3;
	// 四种通知
	private static final int NOTIFY_CONNECT_ON = 0x81;
	private static final int NOTIFY_CONNECT_OFF = 0x82;
	private static final int NOTIFY_CONNECT_DATA = 0x83;
	private static final int NOTIFY_CONNECT_FAIL = 0x84;

	// 连接状态
	private int mConnectStatus = CONNECT_DISCONNECT;

	private Socket mSocket;
	private InputStream mReader;
	private OutputStream mWriter;

	private Receiver mReceiver;

	private ConnectListener mConnectListener;

	private WorkerHandler mWorkerHandler;

	/**
	 * Creates a worker thread with the given name. The thread then runs a
	 * {@link Looper}.
	 *
	 *            A name for the new thread
	 * @throws InterruptedException
	 * @throws InterruptedException
	 */
	ZbLink(ConnectListener li) {
		mConnectListener = li;
		Thread t = new Thread(null, this, "LinkHandlerThread");
		// t.setPriority(Thread.MIN_PRIORITY);
		t.start();

		synchronized (mLockInit) {
			while (mWorkerHandler == null) {
				try {
					mLockInit.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void run() {
		Looper.prepare();
		synchronized (mLockInit) {
			mWorkerHandler = new WorkerHandler(Looper.myLooper());
			mLockInit.notifyAll();
		}
		Looper.loop();
	}

	public int getConnectStatus(){
		return mConnectStatus;
	}

	/**
	 * 调用之后，会返回值，你在调用的地方判断返回值，来区分哪种错误，方便你做不同的提示
	 *
	 * @param host
	 * @param port
	 * @return
	 */
	private int linkOpen(String host, int port)// 建立连接
	{
		try {
			mSocket = new Socket();
			// 连接到...........
			mSocket.connect(new InetSocketAddress(host, port)/* , 30*1000 */);

		} catch (Exception e) {
			Log.e(TAG, "connect失败 host : " + host + ":" + port);
			return -1;
		}
		try {
			mReader = mSocket.getInputStream();
			mWriter = mSocket.getOutputStream();
			Log.e(TAG, "linkOpen---connect host : " + host + ":" + port);
		} catch (IOException e) {
			e.printStackTrace();
			return -2;
		}
		return 0;
	}

	private void linkClose()// 关闭连接
	{
		try {
			mSocket.shutdownInput();
			mSocket.shutdownOutput();
			mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mReader = null;
		mWriter = null;
	}

	// public void setConnectListener(ConnectListener li)
	// {
	// mConnectListener = li;
	// }
	/****** main thread request call ******/
	public int requestConnect(String host, int port)// 请求链接
	{
		Message msg;
		int ret;
		synchronized (mLockConn) {
			if (mConnectStatus == CONNECT_DISCONNECT) {
				msg = Message.obtain();
				msg.what = REQUEST_CONNECT;
				msg.arg1 = port;
				msg.obj = host;

				mConnectStatus = CONNECT_CONNECTING;
				mWorkerHandler.sendMessage(msg);
				ret = 0;
			} else
				ret = 1;
		}
		return ret;
	}

	public int requestDisConnect()// 请求关闭 连接
	{
		Message msg;
		int ret;
		synchronized (mLockConn) {
			if (mConnectStatus == CONNECT_CONNECTED) {
				msg = Message.obtain();
				msg.what = REQUEST_DISCONNECT;
				mConnectStatus = CONNECT_CONNECTCLOSEING;
				mWorkerHandler.sendMessage(msg);
				ret = 0;
			} else
				ret = 1;
		}
		return ret;
	}

	/**
	 * //请求发送数据 //不是CONNECT_CONNECTED状态，不能发送数据
	 *
	 * @param b
	 * @return
	 */
	public int requestSendData(byte[] b) {
		if (mConnectStatus != CONNECT_CONNECTED) {
			return 1;
		}
		Message msg;
		msg = Message.obtain();
		msg.what = REQUEST_SENDDATA;
		msg.obj = b;
		mWorkerHandler.sendMessage(msg);
		return 0;
	}

	/****** do main thread call ******/

	private void doRequestConnect(Message msg) {
		String host = (String) msg.obj;
		int port = msg.arg1;
		int ret;
		ret = linkOpen(host, port);
		msg = Message.obtain();
		if (ret < 0) {
			mConnectStatus = CONNECT_DISCONNECT;
			/* notify main thread */
			msg.what = NOTIFY_CONNECT_FAIL;
		} else {
			mConnectStatus = CONNECT_CONNECTED;
			mReceiver = new Receiver();
			msg.what = NOTIFY_CONNECT_ON;
		}
		/* notify main thread */
		mWorkerHandler.sendMessage(msg);
	}

	private void doRequestDisconnect() {
		Message msg;
		mConnectStatus = CONNECT_DISCONNECT;
		linkClose();
		/* notify main thread */
		msg = Message.obtain();
		msg.what = NOTIFY_CONNECT_OFF;
		mWorkerHandler.sendMessage(msg);
	}

	private void doRequestSendData(Message msg) {
		byte dat[] = (byte[]) msg.obj;

		if (mConnectStatus == CONNECT_CONNECTED) {
			try {
				// 写进socket
				Log.i(TAG,"数据发送---" + ETool.byteTostring(dat));
				mWriter.write(dat);
				mWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				/* notify link close */
				mConnectStatus = CONNECT_DISCONNECT;
				linkClose();
				/* notify main thread */
				msg = Message.obtain();
				msg.what = NOTIFY_CONNECT_OFF;
				mWorkerHandler.sendMessage(msg);
			}
		}
	}

	interface ConnectListener {
		// void onConnectOn();
		// void onConnectFail();
		void onConnectStatu(boolean st);

		void onConnectData(byte[] b);
	}

	/* request send thread */
	private class WorkerHandler extends Handler {
		private static final String TAG = "LinkWorkHandler";

		public WorkerHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {
			// 这里处理 mWorkerHandler.sendMessage(msg)发过来的东西
			switch (msg.what) {
				case REQUEST_CONNECT:
					doRequestConnect(msg);
					break;
				case REQUEST_DISCONNECT:
					doRequestDisconnect();
					break;
				case REQUEST_SENDDATA:
					doRequestSendData(msg);
					break;

			/* response call back */
				case NOTIFY_CONNECT_ON:
					// Log.d(TAG,
					// "thread("+Thread.currentThread().getName()+"):connect on");
					mConnectListener.onConnectStatu(true);
					break;
				case NOTIFY_CONNECT_FAIL:
					// Log.d(TAG,
					// "thread("+Thread.currentThread().getName()+"):connect fail");
					mConnectListener.onConnectStatu(false);
					break;
				case NOTIFY_CONNECT_OFF:
					// Log.d(TAG,
					// "thread("+Thread.currentThread().getName()+"):connect off");
					mConnectListener.onConnectStatu(false);
					break;
				case NOTIFY_CONNECT_DATA:
					byte dat[] = (byte[]) msg.obj;
					// Log.d(TAG,
					// "thread("+Thread.currentThread().getName()+"):connect recv:"+dat);
					mConnectListener.onConnectData(dat);
					break;

				default:
					Log.e(TAG, "unknow request or notify.");
					break;
			}
		}
	}

	/* data recv thread */
	private class Receiver implements Runnable {
		static final String TAG = "Receiver";
		byte[] dat = new byte[256];

		public Receiver() {
			Thread t = new Thread(null, this, "LinkReceiver");
			// t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}

		public void run() {
			for (; mConnectStatus == CONNECT_CONNECTED;) {
				int rlen = 0;
				try {
					rlen = mReader.read(dat);
				} catch (IOException e) {
					Log.d(TAG, "thread(" + Thread.currentThread().getName()
							+ ") terminal by read error.");
					// requestDisConnect();
					mConnectStatus = CONNECT_DISCONNECT;
					mConnectListener.onConnectStatu(false);
					break;
				}
				if (rlen < 0) {
					// requestDisConnect();
					mConnectStatus = CONNECT_DISCONNECT;
					mConnectListener.onConnectStatu(false);
					Log.d(TAG, "thread(" + Thread.currentThread().getName()
							+ ") terminal by read -1.");
					break;
				} else if (rlen > 0) {
					Message msg;
					byte[] d = new byte[rlen];
					for (int i = 0; i < rlen; i++)
						d[i] = dat[i];
					Log.i(TAG,"数据返回---"+ETool.byteTostring(d));
					mConnectListener.onConnectData(d);
				}
			}
			mReceiver = null;
			Log.d(TAG, "LinkReceiver terminal by disconnect...");
		}

	}
}
