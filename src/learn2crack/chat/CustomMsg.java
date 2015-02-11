package learn2crack.chat;


public class CustomMsg {
	private String fromName, message;
	private boolean isSelf;

	public CustomMsg() {
	}

	public CustomMsg(String fromName, String message, boolean isSelf) {
		this.fromName = fromName;
		this.message = message;
		this.isSelf = isSelf;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSelf() {
		return isSelf;
	}

	public void setSelf(boolean isSelf) {
		this.isSelf = isSelf;
	}

}
