package com.health.device;

import java.util.ArrayList;
import java.util.List;

import android.util.SparseArray;

import com.health.database.Tables;
import com.health.util.MyArrays;

/**
 * GMP urine analyzer GMP尿液分析仪
 * 
 * @author jiqunpeng
 * 
 *         创建时间：2013-10-30 上午10:14:50
 */
public class GmpUa {
	// 0x93 0x8e 0x4 0x00 0x08 0x04 无 0x0c
	// 获取单条数据的命令
	public static final byte[] COMMAND_SINGLE_DATA = { (byte) 0x93,
			(byte) 0x8e, 0x04, 0x00, 0x08, 0x04, 0x10 };
	// 确认命令
	public static final byte[] CONFIRM = { (byte) 0x93, (byte) 0x8e, 0x08,
			0x00, 0x08, 0x01, 0x43, 0x4f, 0x4e, 0x54, 0x45 };
	private static final byte[] HEAD = { (byte) 0x93, (byte) 0x8e };
	// 单条数据标志
	public static final byte TOKEN_SINGLE_DATA = 0x04;

	private GmpUa() {// 避免实例化

	}

	/**
	 * 获取数据标志
	 * 
	 * @param data
	 * @return
	 */
	public static byte getToken(byte[] data) {
		return data[5];
	}

	/**
	 * 获取数据的校验和
	 * 
	 * @param data
	 * @return
	 */
	private static byte getCheckSum(byte[] data) {
		byte sum = 0;
		for (int i = 2; i < data.length - 1; i++)
			sum += data[i];
		return sum;
	}

	/**
	 * 检验数据的校验和是否正确
	 * 
	 * @param data
	 * @return
	 */
	public static boolean check(byte[] data) {
		byte checkSum = getCheckSum(data);
		boolean result = checkSum == data[data.length - 1];
		return result;
	}

	/**
	 * 检查数据是否有存储数据
	 * 
	 * @param data
	 * @return
	 */
	public static boolean checkNull(byte[] data) {
		byte ones = (byte) 0xff;
		for (int i = 6; i < 17; i++)
			// [6,17]全一表示每次数据
			if (data[i] != ones)
				return true;
		return false;
	}

	/**
	 * 解析数据记录
	 * 
	 * @param data
	 */
	public static UaRecord parseRecord(byte[] data) {
		if (!checkNull(data))
			return null;
		// 数据记录从第6（以0为起点）个字节开始[6,7]表示序列号
		int effective = ((data[8] & 0x07) << 8) | (data[9] & 0xff);// [8,9]表示有效位
		// [10,11]表示年、月、日
		int year = data[11] & 0x7f;
		int month = ((data[10] & 0x07) << 1) | ((data[11] & 0x80) >> 7);
		int day = (data[10] & 0xf8) >> 3;
		// [12,13]表示时、分、LEU
		int hour = data[13] & 0x1f;
		int minute = ((data[12] & 0x07) << 3) | ((data[13] & 0xe0) >> 5);
		UaRecord.Builder builder = new UaRecord.Builder(year, month, day, hour,
				minute);
		if ((effective & 0x01) != 0) {
			int leu = (data[12] & 0x38) >> 3;
			builder.leu(leu);
		}
		// [14,15]表示NIT UBG PRO PH BLD
		if ((effective & (0x01 << 1)) != 0) {
			int bld = (data[14] & 0x70) >> 4;
			builder.bld(bld);
		}
		if ((effective & (0x01 << 2)) != 0) {
			int ph = (data[14] & 0x0e) >> 1;
			builder.ph(ph);
		}
		if ((effective & (0x01 << 3)) != 0) {
			int pro = ((data[14] & 0x01) << 2) | ((data[15] & 0xc0) >> 6);
			builder.pro(pro);
		}
		if ((effective & (0x01 << 4)) != 0) {
			int ubg = (data[15] & 0x38) >> 3;
			builder.ubg(ubg);
		}
		if ((effective & (0x01 << 5)) != 0) {
			int nit = data[15] & 0x7;
			builder.nit(nit);
		}
		// [16,17]表示VC GLU BIL KET SG
		if ((effective & (0x01 << 6)) != 0) {
			int sg = data[17] & 0x7;
			builder.sg(sg);
		}
		if ((effective & (0x01 << 7)) != 0) {
			int ket = (data[17] & 0x38) >> 3;
			builder.ket(ket);
		}
		if ((effective & (0x01 << 8)) != 0) {
			int bil = ((data[16] & 0x01) << 2) | ((data[17] & 0xc0) >> 6);
			builder.bil(bil);
		}
		if ((effective & (0x01 << 9)) != 0) {
			int glu = (data[16] & 0x0e) >> 1;
			builder.glu(glu);
		}
		if ((effective & (0x01 << 10)) != 0) {
			int vc = (data[16] & 0x70) >> 4;
			builder.vc(vc);
		}
		return builder.build();
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

	/**
	 * 尿液分析仪记录结构
	 * 
	 * @author jiqunpeng
	 * 
	 *         创建时间：2013-10-30 下午4:44:30
	 */
	public static class UaRecord {
		public final String date;
		public final String leu;// 白细胞
		public final String bld;// 潜血
		public final String ph;// pH值
		public final String pro;// 蛋白质
		public final String ubg;// 尿胆原
		public final String nit;// 亚硝酸盐
		public final String sg;// 比重
		public final String ket;// 酮体
		public final String bil;// 胆红素
		public final String glu;// 葡萄糖
		public final String vc;// 维生素C

		private UaRecord(Builder builder) {
			this.date = builder.date;
			this.leu = builder.leu;
			this.bld = builder.bld;
			this.ph = builder.ph;
			this.pro = builder.pro;
			this.ubg = builder.ubg;
			this.nit = builder.nit;
			this.sg = builder.sg;
			this.ket = builder.ket;
			this.bil = builder.bil;
			this.glu = builder.glu;
			this.vc = builder.vc;
		}

		public static class Builder {
			private static final String NEGTIVE = "-";// 阴性
			private static final String NORMAL = "+-";// 正常？
			private static final String[] PH = { "5.0", "6.0", "6.5", "7.0",
					"7.5", "8.0", "8.5" };
			private String date = null;
			private String leu = null;
			private String bld = null;
			private String ph = null;
			private String pro = null;
			private String ubg = null;
			private String nit = null;
			private String sg = null;
			private String ket = null;
			private String bil = null;
			private String glu = null;
			private String vc = null;

			public Builder(int year, int month, int day, int hour, int minute) {
				this.date = String.format("%04d/%02d/%02d %02d:%02d:00",
						2000 + year, month, day, hour, minute);

			}

			public UaRecord build() {
				return new UaRecord(this);
			}

			public Builder leu(int leu) {
				this.leu = getResult(leu);
				return this;
			}

			public Builder bld(int bld) {
				this.bld = getResult(bld);
				return this;
			}

			public Builder ph(int ph) {
				this.ph = PH[ph];
				return this;
			}

			public Builder pro(int pro) {
				this.pro = getResult(pro);
				return this;
			}

			public Builder ubg(int ubg) {
				if (ubg == 0)
					this.ubg = NEGTIVE;
				else
					this.ubg = "+" + ubg;
				return this;
			}

			public Builder nit(int nit) {
				if (nit == 0)
					this.nit = NEGTIVE;
				else
					this.nit = "+";
				return this;
			}

			public Builder sg(int sg) {
				this.sg = (sg * 0.005 + 1) + "";
				return this;
			}

			public Builder ket(int ket) {
				this.ket = getResult(ket);
				return this;

			}

			public Builder bil(int bil) {
				if (bil == 0)
					this.bil = NEGTIVE;
				else
					this.bil = "+" + bil;
				return this;

			}

			public Builder glu(int glu) {
				this.glu = getResult(glu);
				return this;

			}

			public Builder vc(int vc) {
				this.vc = getResult(vc);
				return this;

			}

			/**
			 * 结构体值到结果的转换
			 * 
			 * @param code
			 * @return
			 */
			private String getResult(int code) {
				if (code == 0)
					return NEGTIVE;
				if (code == 1)
					return NORMAL;
				return "+" + (code - 1);
			}
		}
	}

	/***
	 * 画图数据转换工具
	 * 
	 * @author jiqunpeng
	 * 
	 *         创建时间：2014-1-2 下午1:54:14
	 */
	public static class CharTranslator {
		private final static String[] itemValues = new String[] { "1", "1.005",
				"1.01", "1.015", "1.02", "1.025", "1.03", "sg", "5.0", "6.0",
				"6.5", "7.0", "7.5", "8.0", "8.5", "ph", "-", "+-", "+1", "+2",
				"+3", "vc", "-", "+-", "+1", "+2", "+3", "+4", "glu", "-",
				"+1", "+2", "+3", "bil", "-", "+-", "+1", "+2", "+-", "ket",
				"-", "+-", "+1", "+2", "+3", "bld", "-", "+-", "+1", "+2",
				"+3", "+4", "pro", "-", "+1", "+2", "+3", "ubg", "-", "+",
				"nit", "-", "+-", "+1", "+2", "+3", "leu" };
		private final static String[] sgItems = new String[] { "1", "1.005",
				"1.01", "1.015", "1.02", "1.025", "1.03", "sg" };
		private final static String[] phItems = new String[] { "5.0", "6.0",
				"6.5", "7.0", "7.5", "8.0", "8.5", "ph" };
		private final static String[] vcItems = new String[] { "-", "+-", "+1",
				"+2", "+3", "vc" };
		private final static String[] gluItems = new String[] { "-", "+-",
				"+1", "+2", "+3", "+4", "glu" };
		private final static String[] bilItems = new String[] { "-", "+1",
				"+2", "+3", "bil" };
		private final static String[] ketItems = new String[] { "-", "+-",
				"+1", "+2", "+-", "ket" };
		private final static String[] bldItems = new String[] { "-", "+-",
				"+1", "+2", "+3", "bld" };
		private final static String[] proItems = new String[] { "-", "+-",
				"+1", "+2", "+3", "+4", "pro" };
		private final static String[] ubgItems = new String[] { "-", "+1",
				"+2", "+3", "ubg" };
		private final static String[] nitItems = new String[] { "-", "+", "nit" };
		private final static String[] leuItems = new String[] { "-", "+-",
				"+1", "+2", "+3", "leu" };

		private final static int OFFSET_SG = 0;
		private final static int OFFSET_PH = sgItems.length + OFFSET_SG;
		private final static int OFFSET_VC = OFFSET_PH + phItems.length;
		private final static int OFFSET_GLU = OFFSET_VC + vcItems.length;;
		private final static int OFFSET_BIL = OFFSET_GLU + gluItems.length;
		private final static int OFFSET_KET = OFFSET_BIL + bilItems.length;
		private final static int OFFSET_BLD = OFFSET_KET + ketItems.length;
		private final static int OFFSET_PRO = OFFSET_BLD + bldItems.length;
		private final static int OFFSET_UBG = OFFSET_PRO + proItems.length;
		private final static int OFFSET_NIT = OFFSET_UBG + ubgItems.length;
		private final static int OFFSET_LEU = OFFSET_NIT + nitItems.length;

		public static String getMark(double value) {
			int iValue = (int) value;
			return itemValues[iValue];

		}

		public static double getValue(String token, String strValue) {
			int offset = 0;
			if (token.equals(Tables.LEU)) {
				offset = OFFSET_LEU;
			} else if (token.equals(Tables.BLD)) {
				offset = OFFSET_BLD;
			} else if (token.equals(Tables.PH)) {
				offset = OFFSET_PH;
			} else if (token.equals(Tables.PRO)) {
				offset = OFFSET_PRO;
			} else if (token.equals(Tables.UBG)) {
				offset = OFFSET_UBG;
			} else if (token.equals(Tables.NIT)) {
				offset = OFFSET_NIT;
			} else if (token.equals(Tables.KET)) {
				offset = OFFSET_KET;
			} else if (token.equals(Tables.BIL)) {
				offset = OFFSET_BIL;
			} else if (token.equals(Tables.UGLU)) {
				offset = OFFSET_GLU;
			} else if (token.equals(Tables.VC)) {
				offset = OFFSET_VC;
			} else if (token.equals(Tables.SG)) {
				offset = OFFSET_SG;
			}
			for (int i = offset; i < itemValues.length; i++) {
				if (itemValues[i].equals(strValue))
					return i;
			}
			return 0;
		}

	}
}
