package com.hchstudio.znjj.EMB;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.graphics.ColorMatrixColorFilter;
import android.media.MediaPlayer;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * 工具函数
 *
 * @author Administrator
 *
 */
public class ETool {

	static final float[] BT_SELECTED = new float[] { 1, 0, 0, 0, 50, 0, 1, 0,
			0, 50, 0, 0, 1, 0, 50, 0, 0, 0, 1, 0 };
	static final float[] BT_NOT_SELECTED = new float[] { 1, 0, 0, 0, 0, 0, 1,
			0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0 };
	static final ColorMatrixColorFilter selfilter = new ColorMatrixColorFilter(
			BT_SELECTED);
	static final ColorMatrixColorFilter unselfilter = new ColorMatrixColorFilter(
			BT_NOT_SELECTED);

	/**
	 * 将byte转换成int
	 *
	 * @param b
	 * @return
	 */
	static int builduInt(byte b) { // 高16位用0补充
		return (b & 0xff);
	}

	/**
	 *
	 * @param b1
	 * @param b2
	 * @return
	 */
	public static int builduInt(byte b1, byte b2) { // 高位看齐，b1&0xff是16位，(b1&0xff)<<8，将b1&0xff这个16位的数值（高八位为0，低八位存放数值）的低八位通过左移八位转移到高八位，低八位用0补充
		// |或运算，得到一个新的16位，高8位是b1部分，低八位是b2部分，可理解为拼接
		return (((b1 & 0xff) << 8) | (b2 & 0xff));
	}

	/**
	 * 字节转换成String
	 *
	 * @param b
	 * @return
	 */
	public static String byteTostring(byte[] b) {
		String s = "";
		if (b == null) {
			return "<null>";
		}
		for (int i = 0; i < b.length; i++) {
			s += String.format("%02X ", b[i]);
//			s += Integer.toHexString(b[i]);
		}
		return s;
	}

	/***************************************************************/
	/*
	 * class TimerHandler extends Handler { public void handleMessage(Message
	 * msg) {
	 * 
	 * } } interface TimerListener {
	 * 
	 * } static TimerHandler mTimerHandler = new TimerHandler(); static void
	 * startTimer(TimerListener li, int ms) { Message msg = Message.obtain();
	 * msg.obj = li; mTimerHandler.sendMessageDelayed(msg, 300); }
	 */

	/***************************************************************
	 ************************** 通知处理
	 *****************************/
	static NotificationManager mNotificationManager;
	static SmsManager mSmsManager;
	static MediaPlayer mMdeiaPlayer = null;
//			MediaPlayer.create(ZigBeeTool.getInstance().getBaseContext(), R.raw.alarm_1);
	static MediaPlayer.OnCompletionListener onCompletion = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer arg0) {
			Log.d("player", "onCompletion...");
			mAlarmCnt--;
			try {
				mMdeiaPlayer.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (mAlarmCnt > 0) {
				mMdeiaPlayer.start();
			}
		}
	};
	static int mAlarmCnt = 0;

	// 当温度，湿度等不合理时发出通知
	static void playAlarm(int cnt) {
		mMdeiaPlayer.setOnCompletionListener(onCompletion);
		mAlarmCnt = cnt;
		if (cnt == 0) {
			mMdeiaPlayer.setLooping(true);
		} else {
			mAlarmCnt = cnt;
		}
		mMdeiaPlayer.start();
	}

	static void stopAlarm() {
		mAlarmCnt = 0;
		mMdeiaPlayer.stop();
		try {
			mMdeiaPlayer.prepare();
			mMdeiaPlayer.seekTo(0);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static int sNotifyId = 1001;

	// 当温度，湿度等不合理时发出通知
	static void notify(String title, String msg) {
		if (mNotificationManager == null) {
//			mNotificationManager = (NotificationManager) ZigBeeTool.getInstance().getSystemService(ZigBeeTool.NOTIFICATION_SERVICE);
		}

		Notification notification = null;
//				new Notification(R.drawable.icon, title,System.currentTimeMillis());
		PendingIntent intent = null;
//				PendingIntent.getActivity(ZigBeeTool.getInstance(), 0,
//				new Intent(ZigBeeTool.getInstance(), ZigBeeTool.class), 0);
//		notification.setLatestEventInfo(ZigBeeTool.getInstance(), title, msg,intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;// 两者按位或运算，把notification.flags除去Notification.FLAG_AUTO_CANCEL这种属性

		// notification.defaults |= Notification.DEFAULT_SOUND ; //通知的铃声
		// 设置音乐

		// noticed.sound = Uri.parse("file:///sdcard/notification/ringer.mp3");
		// noticed.sound =
		// Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6");
		// mNotificationManager.cancelAll();
		// 注意这种写法，管理器模式
		mNotificationManager.notify(sNotifyId++, notification);

		// m.notifyWithText(1001, "わたしはSHARETOPです。", NotificationManager, null);
	}

	static void sendShortMessage(String mobile, String content) {
		if (mSmsManager == null) {
			mSmsManager = SmsManager.getDefault();
		}
		if (content.length() > 70) {
			// 使用短信管理器进行短信内容的分段，返回分成的段
			ArrayList<String> contents = mSmsManager.divideMessage(content);
			for (String msg : contents) {
				// 使用短信管理器发送短信内容
				// 参数一为短信接收者
				// 参数三为短信内容
				// 其他可以设为null
				mSmsManager.sendTextMessage(mobile, null, msg, null, null);
			}
			// 否则一次过发送
		} else {
			mSmsManager.sendTextMessage(mobile, null, content, null, null);
		}
	}

	interface IterateIpAddressListener {
		void iterate(String name, String ip);
	}

	/**
	 * ?????????????????????????????????????????????????????????????????????????
	 *
	 * @param a
	 */
	static void IterateLocalIpAddress(IterateIpAddressListener a) {
		try { // 枚举集en,装网络接口
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				// 枚举集enumIpAddr，装ip地址
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					 if (!inetAddress.isLoopbackAddress())
					{
						Log.d(intf.getName(), inetAddress.getHostAddress().toString());
						// 接口变量调用接口的方法
						a.iterate(intf.getName(), inetAddress.getHostAddress().toString());
					}
				}
			}
		} catch (SocketException ex) {
//			Log.e("WifiPreference IpAddress", ex.toString());
		}

	}

}
