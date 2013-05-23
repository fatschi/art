package de.uni_potsdam.de.hpi.fgnaumann.art.util;

public class Bit extends Number {

	private static final long serialVersionUID = 8544897788145391434L;
	boolean state = false;

	public Bit(boolean b) {
		this.state = b;
	}

	@Override
	public double doubleValue() {
		return this.state ? 0d : 1d;
	}

	@Override
	public float floatValue() {
		return this.state ? 0f : 1f;
	}

	@Override
	public int intValue() {
		return this.state ? 0 : 1;
	}

	@Override
	public long longValue() {
		return this.state ? 0l : 1l;
	}

	public boolean getState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

}
