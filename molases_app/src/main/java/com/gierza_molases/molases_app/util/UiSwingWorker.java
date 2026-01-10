package com.gierza_molases.molases_app.util;

import java.util.List;

import javax.swing.SwingWorker;

public abstract class UiSwingWorker<T, P> extends SwingWorker<T, P> {

	@Override
	protected abstract T doInBackground() throws Exception;

	/**
	 * Called on the EDT when publish() is used
	 */
	@Override
	protected void process(List<P> chunks) {
		// Optional: override in subclass
	}

	/**
	 * 
	 * Always runs on the EDT after completion
	 */
	@Override
	protected void done() {
		try {
			onSuccess(get());
		} catch (Exception e) {
			onError(e.getCause() != null ? (Exception) e.getCause() : e);
		}
	}

	protected abstract void onSuccess(T result);

	protected void onError(Exception e) {
		e.printStackTrace();
	}
}
