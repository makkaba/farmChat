package learn2crack.chat;

/*
 * isOther가 true이면 다른 사람이 한 말. 좌측에 표시
 * 
 * */


public class OneComment {
	public boolean isOther;
	public String comment;

	
	//1:1채팅이니까 누구로부터 왔는지는 표시 하지 않아도 됨
	//누구로부터 왔는지 표시하려면 param 하나 더 만들어서 넣어줘야할듯
	
	public OneComment(boolean isOther, String comment) {
		super();
		this.isOther = isOther;
		this.comment = comment;
	}

}