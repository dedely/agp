package business.model.transport;


public class Transport {
	
	private String type;
	private int speed;
	private int price;
	private int confort;
	
	public Transport(String type, int speed, int price, int confort) {
		this.type = type;
		this.speed = speed;
		this.price = price;
		this.confort = confort;
	}

	public Transport() {
		
	}
	
	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getConfort() {
		return confort;
	}

	public void setConfort(int confort) {
		this.confort = confort;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String toString() {
		return "TRANSPORT = Type : " + type + 
				", Price : " + price + 
				", Speed : " + speed +
				", Confort : " + confort;
	}

}
