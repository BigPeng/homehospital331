package edu.hitsz.android;
//������Ϣ
public class PersonInfo {
	public String name;	//����
	public String cardNum;//����
	public String gender;//�Ա�
	public String age;//����
	public byte[] photo;
	public PersonInfo(String name, String cardNum, String gender, String age, byte[]photo){
		this.name = name;
		this.cardNum = cardNum;
		this.gender = gender;
		this.age = age;
		this.photo = photo;
	}
}
