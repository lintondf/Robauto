package com.bluelightning;

import java.awt.AWTEvent;
import java.util.concurrent.ForkJoinPool;

import javax.swing.JFrame;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

public class Events {

	// System-wide event bus
	public static EventBus eventBus = new AsyncEventBus(new ForkJoinPool());
	
	public static class WebBrowserOpenEvent {
		WebBrowser browserCanvas;
		String href;
		public WebBrowserOpenEvent( WebBrowser browserCanvas, String href) {
			this.browserCanvas = browserCanvas;
			this.href = href;
		}
	}

	public static class UiEvent {
		String source;
		AWTEvent awtEvent;
		public UiEvent( String source, AWTEvent awtEvent ) {
			this.source = source;
			this.awtEvent = awtEvent;
		}
	}
}
