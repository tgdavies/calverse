package org.kablambda.calverse;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.RecoverableThrowable;
import org.multiverse.api.exceptions.RetryError;
import org.multiverse.templates.AtomicTemplate;
import org.multiverse.templates.OrElseTemplate;
import org.openquark.cal.runtime.CalFunction;
import org.openquark.cal.runtime.CalValue;

public class StmRuntime {
	static Logger log = Logger.getLogger(StmRuntime.class.getCanonicalName());
	
	static {
		log.setLevel(Level.FINE);
		Handler h = new ConsoleHandler();
		//h.setLevel(Level.FINE);
		log.addHandler(h);
	}
	
	public static TVar<Object> jNewNamedTVar(Transaction tx, Object value,
			String name) {
		log.fine("Creating TVar " + name + " with value " + value);
		return new TVar<Object>(tx, name, value);
	}

	public static TVar jNewUnsafeNamedTVar(Object value, String name) {
		log.fine("Creating TVar " + name + " with value " + value);
		return new TVar<Object>(name, value);
	}

	public static CalValue atomically(final CalFunction f) {
		return new AtomicTemplate<CalValue>(GlobalStmInstance
				.getGlobalStmInstance(), "", true, false, 100) {

			@Override
			public CalValue execute(Transaction tx) throws Exception {
				return StmRuntime.execute(f, tx);
			}

		}.execute();
	}

	private static CalValue execute(final CalFunction f, final Transaction tx) {
		try {
			return (CalValue) f.evaluate(tx);
		} catch (Throwable e) {
			if (e != null && e instanceof RecoverableThrowable
					|| e instanceof RetryError) {
				rethrow(e);
				return null; // never executed
			}
			throw new RuntimeException("Unexpected exception", e);
		}
	}

	private static void rethrow(Throwable ex) {
		if (ex instanceof RuntimeException) {
			throw (RuntimeException) ex;
		} else if (ex instanceof Error) {
			throw (Error) ex;
		} else {
			throw new RuntimeException("Unthrowable throwable", ex);
		}
	}

	public static CalValue orElse(final CalFunction f,
			final CalFunction f2, Transaction tx) {
		return new OrElseTemplate<CalValue>(tx) {

			@Override
			public CalValue orelserun(Transaction tx) {
				return StmRuntime.execute(f2, tx);
			}

			@Override
			public CalValue run(Transaction tx) {
				return StmRuntime.execute(f, tx);
			}

		}.execute();
	}

	public static void jRetry(Transaction tx, String reason) {
		log.fine("Retrying");
		throw new RetryError();
	}
}
