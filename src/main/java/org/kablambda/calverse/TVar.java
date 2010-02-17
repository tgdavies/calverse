package org.kablambda.calverse;

import org.multiverse.api.Transaction;
import org.multiverse.datastructures.refs.manual.Ref;
/**
 * A transactional variable
 *
 */
public class TVar<T> {
	private final String name;
	private final Ref<T> ref;
	
	public TVar(Transaction tx, String name, T initialValue) {
		this.name = name;
		this.ref = new Ref<T>(tx, initialValue);
	}
	
	public TVar(String name, T initialValue) {
		this.name = name;
		this.ref = Ref.createCommittedRef(initialValue);
	}
	
	public String toString() {
		return "TVar: " + name;
	}
	
	public void set(Transaction tx, T newValue) {
		StmRuntime.log.fine("Setting " + name + " to " + newValue);
		ref.set(tx, newValue);
	}
	
	public T get(Transaction tx) {
		T v = ref.get(tx);
		if (v == null) {
			System.out.println("Null value in TVar " + this);
		}
		return v;
	}
}
