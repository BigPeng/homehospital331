package edu.hitsz.android;
//���˽�����¼
public class HealthRecord {
	public String bp;			//Ѫѹ
	public String bpAdvice;		//Ѫѹ����
	public String bo;			//Ѫ��
	public String boAdvice;		//Ѫ������
	public String fat;			//֬��
	public String fatAdvice;	//֬������
	public String pulse;		//����
	public String pulseAdvice;	//���ʽ���
	public String heart;		//̥��
	public String heartAdvice;	//̥�Ľ���
	public String glu;			//Ѫ��
	public String gluAdvice;	//Ѫ�ǽ���
	public String weight;		//����
	public String weightAdvice; //���ؽ���
	public String temperature;	//����
	public String temperatureAdvice; //���½���
	public HealthRecord(String bp, String bpAdvice, String bo, String boAdvice, String fat, String fatAdvice, String pulse, String pulseAdvice,
			String heart, String heartAdvice, String glu, String gluAdvice, String weight, String weightAdvice, String temperature, String temperatureAdvice){
		this.bp = bp;
		this.bpAdvice = bpAdvice;
		this.bo = bo;
		this.boAdvice = boAdvice;
		this.fat = fat;
		this.fatAdvice = fatAdvice;
		this.pulse = pulse;
		this.pulseAdvice = pulseAdvice;
		this.heart = heart;
		this.heartAdvice = heartAdvice;
		this.glu = glu;
		this.gluAdvice = gluAdvice;
		this.weight = weight;
		this.weightAdvice = weightAdvice;
		this.temperature = temperature;
		this.temperatureAdvice = temperatureAdvice;
	}
	
}
