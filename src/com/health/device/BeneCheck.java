package com.health.device;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.Log;
import android.util.SparseArray;

import com.health.device.BeneCheck.Record;
import com.health.util.MyArrays;

/**
 * 百捷设备
 * 
 * @author jiqunpeng
 * 
 *         创建时间：2013-10-25 上午9:17:13
 */
public class BeneCheck {
	private static final String TAG = "BeneCheck";
	// 同步命令
	public static final byte[] ECHO = { '$', 'P', 'C', 'L', 0x01, 0x00, 0x00,
			0x00, 0x00, 0x00, (byte) 0xE0 };
	// 查询血糖记录数量的命令
	public static final byte[] QUERY_GLU_NUM = { '$', 'P', 'C', 'L', 0x40,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x1F };
	// 查询尿酸记录数量的命令
	public static final byte[] QUERY_UA_NUM = { '$', 'P', 'C', 'L', 0x50, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x2F };
	// 查询总胆固醇记录数量的命令
	public static final byte[] QUERY_CHOL_NUM = { '$', 'P', 'C', 'L', 0x60,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x3F };
	// 清除血糖记录命令
	public static final byte[] CLEAR_GLU_NUM = { '$', 'P', 'C', 'L', 0x20,
			0x00, 0x01, 0x00, 0x00, 0x00, 0x00 };
	// 清除尿酸记录命令
	public static final byte[] CLEAR_UA_NUM = { '$', 'P', 'C', 'L', 0x20, 0x00,
			0x02, 0x00, 0x00, 0x00, 0x01 };
	// 清除总胆固醇记录命令
	public static final byte[] CLEAR_CHOL_NUM = { '$', 'P', 'C', 'L', 0x20,
			0x00, 0x04, 0x00, 0x00, 0x00, 0x03 };
	// 查询血糖记录数量
	public static final byte TOKEN_GLU_NUM = 0x40;
	// 查询血糖记录
	public static final byte TOKEN_GLU_RECORD = 0x41;
	// 查询尿酸记录数量
	public static final byte TOKEN_UA_NUM = 0x50;
	// 查询尿酸记录
	public static final byte TOKEN_UA_RECORD = 0x51;
	// 查询总胆固醇记录数量
	public static final byte TOKEN_CHOL_NUM = 0x60;
	// 查询总胆固醇记录
	public static final byte TOKEN_CHOL_RECORD = 0x61;
	// 数据头，可用于数据分割
	private static final byte[] HEAD = { '$', 'P' };

	/**
	 * 获取检验和
	 * 
	 * @param data
	 */
	public static byte getCheckSum(byte[] data) {
		int sum = 0;
		for (int i = 1; i < data.length; i++) {
			sum += 0xff & data[i];
		}
		return (byte) (sum & 0xff);
	}

	/**
	 * 检验时间的校验和
	 * 
	 * @param data
	 * @return
	 */
	public static boolean check(byte[] data) {
		byte checkSum = getCheckSum(MyArrays.copyOfRange(data, 0,
				data.length - 1));
		boolean result = checkSum == data[data.length - 1];
		Log.i(TAG, "check:" + Arrays.toString(data) + "->" + result);
		return result;
	}

	/**
	 * 获取数据标志
	 * 
	 * @param data
	 */
	private static byte getToken(byte[] data) {
		return data[4];
	}

	/**
	 * 从查询数量的数据中获取数量的值
	 * 
	 * @param data
	 * @return
	 */
	public static int getNum(byte[] data) {
		// $PCL 0x40 0x00 0x00 0x00 0x02 0x00 N_REC
		// checksum
		return (data[10] & 0xff) + ((data[11] & 0xff) << 8);
	}

	/**
	 * 获取查询最新记录的命令
	 * 
	 * @param token
	 * @param num
	 * @return
	 */
	public static byte[] getLatestRecordCommand(int token, int num) {
		// $PCL 0x41 0x00 0x00 0x00 0x04 0x00 R_INDEX
		// checksum
		// $PCL 0x51 0x00 0x00 0x00 0x04 0x00 R_INDEX
		// checksum
		// $PCL 0x61 0x00 0x00 0x00 0x04 0x00 R_INDEX
		// checksum
		if (num <= 0)
			return null;
		byte[] command = { '$', 'P', 'C', 'L', 0x61, 0x00, 0x00, 0x00, 0x04,
				0x00, 0x1, 0x0, 0x0, 0x0, 0x0 };
		command[4] = (byte) (token + 1);
		command[10] = (byte) ((num) % 10);
		command[11] = (byte) ((num) / 10);
		command[12] = (byte) ((num) % 10);
		command[13] = (byte) ((num) / 10);
		command[14] = getCheckSum(MyArrays.copyOfRange(command, 0,
				command.length - 1));
		return command;
	}

	/**
	 * 从返回数据中获取测量记录
	 * 
	 * @param readBuf
	 * @return
	 */
	public static Record getRecord(byte[] data) {
		// 记录的data域有9个字节，index两个字节，年月日时分各一个字节,测量值两个字节
		// $PCL 0x41 0x00 0x00 0x00 0x09 0x00 RECORD
		// checksum
		String date = String.format("%04d-%02d-%02d %02d:%02d:00",
				2000 + data[12], data[13], data[14], data[15], data[16]);
		int value = (data[17] & 0xff) + ((data[18] & 0xff) << 8);
		byte token = getToken(data);
		if (token == TOKEN_GLU_RECORD || token == TOKEN_UA_RECORD
				|| token == TOKEN_CHOL_RECORD)
			System.out.println(Arrays.toString(data));
		return new Record(date, value);
	}

	/**
	 * 百捷血糖设备数据记录结构体
	 * 
	 * @author jiqunpeng
	 * 
	 *         创建时间：2013-10-26 上午10:06:01
	 */
	public static class Record {
		public final String date;
		public final int value;

		public Record(String date, int value) {
			this.date = date;
			this.value = value;
		}
	}

	/**
	 * 将读到数据按类型汇总
	 * 
	 * @param buffer
	 * @return
	 */
	public static SparseArray<List<byte[]>> getLegalPatternsFromBuffer(
			byte[] buffer) {
		List<byte[]> datas = HealthDevice.splitBufferData(buffer, HEAD);
		SparseArray<List<byte[]>> map = new SparseArray<List<byte[]>>();
		for (byte[] data : datas) {
			if (check(data) == true) {
				int token = getToken(data);
				if (map.get(token) == null) {
					List<byte[]> pattern = new ArrayList<byte[]>();
					pattern.add(data);
					map.put(token, pattern);
				} else {
					map.get(token).add(data);
				}
			}
		}
		return map;
	}

}
