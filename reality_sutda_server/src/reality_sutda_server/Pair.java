package reality_sutda_server;

public class Pair<U, V> {

	/**
	 * The first element of this <code>Pair</code>
	 */
	private U first;

	/**
	 * The second element of this <code>Pair</code>
	 */
	private V second;

	/**
	 * Constructs a new <code>Pair</code> with the given values.
	 * 
	 * @param first
	 *            the first element
	 * @param second
	 *            the second element
	 */
	public Pair(U first, V second) {

		this.first = first;
		this.second = second;
	}
	
	public U first() {
        return first;
    }

    public V second() {
        return second;
    }
}
