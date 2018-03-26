/*
 * This class is made available under the Apache License, Version 2.0.
 *
 * See http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Author: Mark Lee
 *
 * (C)2013 Caprica Software (http://www.capricasoftware.co.uk)
 */

package com.bluelightning.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bluelightning.Events;
import com.bluelightning.Events.UiEvent;
import com.bluelightning.Events.WebBrowserOpenEvent;
import com.bluelightning.RobautoMain;
import com.google.common.eventbus.Subscribe;

/**
 * Implementation of an AWT {@link Canvas} that embeds an SWT {@link Browser}
 * component.
 * <p>
 * With contemporary versions of SWT, the Webkit browser is the default
 * implementation.
 * <p>
 * To embed an SWT component inside of a Swing component there are a number of
 * important considerations (all of which comprise this implementation):
 * <ul>
 * <li>A background thread must be created to process the SWT event dispatch
 * loop.</li>
 * <li>The browser component can not be created until after the hosting Swing
 * component (e.g. the JFrame) has been made visible - usually right after
 * <code>frame.setVisible(true).</code></li>
 * <li>To cleanly dispose the native browser component, it is necessary to
 * perform that clean shutdown from inside a
 * {@link WindowListener#windowClosing(WindowEvent)} implementation in a
 * listener registered on the hosting JFrame.</li>
 * <li>On Linux, the <code>sun.awt.xembedserver</code> system property must be
 * set.</li>
 * </ul>
 */
public final class WebBrowser extends Canvas {

	/**
	 * Required for Linux, harmless for other OS.
	 * <p>
	 * <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=161911">SWT
	 * Component Not Displayed Bug</a>
	 */
	static {
		System.setProperty("sun.awt.xembedserver", "true");
	}

	/**
	 * SWT browser component reference.
	 */
	private final AtomicReference<Browser> browserReference = new AtomicReference<>();

	/**
	 * SWT event dispatch thread reference.
	 */
	private final AtomicReference<SwtThread> swtThreadReference = new AtomicReference<>();

	private static WebBrowser browserCanvas;

	protected static WebBrowser mock;


	public WebBrowser() {}
	
	public static WebBrowser factory(MainPanel mainPanel) {
		browserCanvas = new WebBrowser();
		mainPanel.getRightTabbedPane().addTab("AllStays", null, browserCanvas, null);
		
		mock = new WebBrowser();
		//mainPanel.getRightTabbedPane().addTab("Mock", null, mock, null);
		mainPanel.getLeftPanel().add(mock);
		mock.setVisible(false);
		
		Events.eventBus.register(new WebBrowserOpenHandler());
		Events.eventBus.register(browserCanvas.new UiHandler() );
		return browserCanvas;
	}

	public void initialize(JFrame frame) {
		mock.setVisible(false);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Dispose of the native component cleanly
				RobautoMain.logger.info("dispose() on WB/Window Closing");
				browserCanvas.dispose();
				mock.dispose();
			}
		});

		if (mock.initialise()) {
			mock.addLocationListener(new LocationListener() {

				@Override
				public void changing(LocationEvent event) {
//					System.out.println(Thread.currentThread() + " MOCK CHANGING: " + event);
					//event.doit = false;  TODO
					Events.eventBus.post(new Events.WebBrowserOpenEvent(browserCanvas, event.location));
				}

				@Override
				public void changed(LocationEvent event) {
//					System.out.println(Thread.currentThread() + " MOCK CHANGED: " + event);
				}

			});
		}

		// Initialise the native browser component, and if successful...
		if (browserCanvas.initialise()) {
			// ...navigate to the desired URL

			browserCanvas.addLocationListener(new LocationListener() {

				@Override
				public void changing(LocationEvent event) {
//					System.out.println(Thread.currentThread() + " CHANGING: " + event);
				}

				@Override
				public void changed(LocationEvent event) {
//					System.out.println(Thread.currentThread() + " CHANGED: " + event);
					frame.setSize(1205, 805);
				}

			});
			
			browserCanvas.addOpenWindowListener(new OpenWindowListener() {

				@Override
				public void open(org.eclipse.swt.browser.WindowEvent event) {
//					System.out.println(Thread.currentThread() + " OPEN: " + event);
					event.required = true;
					event.browser = mock.getBrowser();
				}

			});
			
			browserCanvas.addProgressListener( new ProgressListener() {
				
				@Override
				public void changed(ProgressEvent event) {
				}

				@Override
				public void completed(ProgressEvent event) {
					loadGeoMock();
				}
				
			});

			browserCanvas.setUrl("https://maps.google.com"); //("https://www.allstays.com/pro"); // index.php
		} else {
			System.out.println("Failed to initialise browser");
		}
		
	}
	/**
	 * Get the native browser instance.
	 *
	 * @return browser, may be <code>null</code>
	 */
	public Browser getBrowser() {
		return browserReference.get();
	}

	/**
	 * Navigate to a URL.
	 *
	 * @param url
	 *            URL
	 */
	public void setUrl(final String url) {
		// This action must be executed on the SWT thread
		getBrowser().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				getBrowser().setUrl(url);
			}
		});
	}

	public void addLocationListener(final LocationListener listener) {
		// This action must be executed on the SWT thread
		getBrowser().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				getBrowser().addLocationListener(listener);
			}
		});
	}

	public void addOpenWindowListener(OpenWindowListener listener) {
		// This action must be executed on the SWT thread
		getBrowser().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				getBrowser().addOpenWindowListener(listener);
			}
		});
	}

	public void addProgressListener(ProgressListener listener) {
		// This action must be executed on the SWT thread
		getBrowser().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				getBrowser().addProgressListener(listener);
			}
		});
	}

	public void evaluateJavascript(final String js) {
		// This action must be executed on the SWT thread
		getBrowser().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				Object retval = getBrowser().evaluate(js);
//				System.out.println(retval);
			}
		});
	}

	/**
	 * Create the browser canvas component.
	 * <p>
	 * This must be called <strong>after</strong> the parent application Frame
	 * is made visible - usually directly after
	 * <code>frame.setVisible(true)</code>.
	 * <p>
	 * This method creates the background thread, which in turn creates the SWT
	 * components and handles the SWT event dispatch loop.
	 * <p>
	 * This method will block (for a very short time) until that thread has
	 * successfully created the native browser component (or an error occurs).
	 *
	 * @return <code>true</code> if the browser component was successfully
	 *         created; <code>false if it was not</code/
	 */
	protected boolean initialise() {
		CountDownLatch browserCreatedLatch = new CountDownLatch(1);
		SwtThread swtThread = new SwtThread(browserCreatedLatch);
		swtThreadReference.set(swtThread);
		swtThread.start();
		boolean result;
		try {
			browserCreatedLatch.await();
			result = browserReference.get() != null;
		} catch (InterruptedException e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * Dispose the browser canvas component.
	 * <p>
	 * This should be called from a
	 * {@link WindowListener#windowClosing(WindowEvent)} implementation.
	 */
	public void dispose() {
		browserReference.set(null);
		SwtThread swtThread = swtThreadReference.getAndSet(null);
		if (swtThread != null) {
			swtThread.interrupt();
		}
	}

	/**
	 * Implementation of a thread that creates the browser component and then
	 * implements an event dispatch loop for SWT.
	 */
	private class SwtThread extends Thread {

		/**
		 * Initialisation latch.
		 */
		private final CountDownLatch browserCreatedLatch;

		/**
		 * Create a thread.
		 *
		 * @param browserCreatedLatch
		 *            initialisation latch.
		 */
		private SwtThread(CountDownLatch browserCreatedLatch) {
			this.browserCreatedLatch = browserCreatedLatch;
		}

		@Override
		public void run() {
			// First prepare the SWT components...
//			System.out.println(Thread.currentThread() + " run()");
			Display display;
			Shell shell;
			try {
				display = new Display();
				shell = SWT_AWT.new_Shell(display, WebBrowser.this);
				shell.setLayout(new FillLayout());
				browserReference.set(new Browser(shell, SWT.NONE));
			} catch (Exception e) {
				e.printStackTrace();
				return;
			} finally {
				// Guarantee the count-down so as not to block the caller, even
				// in case of error -
				// there is a theoretical (rare) chance of failure to initialise
				// the SWT components
				browserCreatedLatch.countDown();
			}
			// Execute the SWT event dispatch loop...
			try {
				shell.open();
				boolean firstDispatch = true;
				while (!isInterrupted() && !shell.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
//				System.out.println(Thread.currentThread() + " run() exiting 1");
				browserReference.set(null);
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						RobautoMain.logger.info("dispose() on WB/Exiting");
//						System.out.println(Thread.currentThread() + " run() exiting 2 " + shell.isDisposed());
						if (!shell.isDisposed())
							shell.dispose();
//						System.out.println(Thread.currentThread() + " run() exiting 3 " + display.isDisposed());
						if (!display.isDisposed())
							display.dispose();
//						System.out.println(Thread.currentThread() + " run() exiting 4");
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				interrupt();
			}
		}
	}

	public static class WebBrowserOpenHandler {
		@Subscribe
		protected void handle(WebBrowserOpenEvent event) {
//			System.out.println("WB Open: " + event.href);
			String javascript = "var robauto_popups = document.getElementsByClassName('leaflet-popup-content');"
					+ "return(robauto_popups.item(0).innerHTML);";
			event.browserCanvas.evaluateJavascript(javascript);
		}
	}
	
	public static String tryCatchWrap(String js) {
		return String.format("try{%s} catch(err){console.log(err);}", js);
	}
	
	
	protected void loadGeoMock() {
//		try {
//			String geomock = IOUtils.toString( new FileReader("scripts/geomock.js") );
////			System.out.print("Loading geomock ");
//			evaluateJavascript(geomock);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}			
	}
	
	public void moveTo(double latitude, double longitude) {
		String coords = String.format( "navigator.geolocation.waypoints = [{coords: {" + 
				"latitude: %f," + 
				"longitude: %f," + 
				"accuracy: 150" + 
				"}}]; %s", latitude, longitude, 
					tryCatchWrap("document.getElementById('locate').click();") );
		evaluateJavascript(coords);
		
	}
	
	public class UiHandler {
		@Subscribe
		protected void handle( UiEvent event ) {
//			System.out.println(event.source + " " + event.awtEvent );
			switch (event.source) {
			case "ControlPanel.FireBug":
				browserCanvas.setUrl("javascript:(function(F,i,r,e,b,u,g,L,I,T,E){if(F.getElementById(b))return;E=F[i+'NS']&&F.documentElement.namespaceURI;E=E?F[i+'NS'](E,'script'):F[i]('script');E[r]('id',b);E[r]('src',I+g+T);E[r](b,u);(F[e]('head')[0]||F[e]('body')[0]).appendChild(E);E=new%20Image;E[r]('src',I+L);})(document,'createElement','setAttribute','getElementsByTagName','FirebugLite','4','firebug-lite.js','releases/lite/latest/skin/xp/sprite.png','https://getfirebug.com/','#startOpened');");
				break;
			case "ControlPanel.Geomock":
				String geomock = null;
				try {
					geomock = IOUtils.toString( new FileReader("scripts/geomock.js") );
					evaluateJavascript(geomock);
					String coords = "	navigator.geolocation.waypoints = [{coords: {\r\n" + 
							"	                                       latitude: 28,\r\n" + 
							"	                                       longitude: -80,\r\n" + 
							"	                                       accuracy: 150\r\n" + 
							"	                                     }}];";
					evaluateJavascript(coords);
					String click = tryCatchWrap("document.getElementById('locate').click();");
					evaluateJavascript(click);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}			
				break;
			default:
				break;
			}
		}
	}
	
	public static void run(Runnable r) {
		
	}
	
	

	/**
	 * Example implementation.
	 *
	 * @param args
	 *            command-line arguments (unused)
	 */
	public static void main(String[] args) {
		// Display the viewer in a JFrame
		JFrame frame = new JFrame("RobAuto RV Trip Planner");
//		frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
//		frame.setUndecorated(true);
		MainPanel mainPanel = new MainPanel();
		mainPanel.getLeftPanel().setLayout(new BorderLayout() );
		//mainPanel.getLeftPanel().add( new ControlPanel() );
		WebBrowser browserCanvas = WebBrowser.factory(mainPanel);
		//mainPanel.getRightLayeredPane().add(layer1, 1);
		//mainPanel.getRightLayeredPane().add(layer2, 2);
//		frame.setContentPane(mainPanel);
		frame.setContentPane(mainPanel);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		browserCanvas.initialize(frame);
	}
	
	
	public static void main1(String[] args) {
		//run( ()->{int i = 1;});
		Events.eventBus.register(new WebBrowserOpenHandler());
		JFrame frame = new JFrame("SWT Browser Embedded in JPanel");
		final WebBrowser browserCanvas = new WebBrowser();
		Events.eventBus.register(browserCanvas.new UiHandler() );

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(browserCanvas, BorderLayout.CENTER);

		frame.setBounds(150, 100, 1200, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(contentPane);

		JFrame frame1 = new JFrame("SWT Browser Embedded in JPanel");
		final WebBrowser mock = new WebBrowser();
		JPanel contentPane1 = new JPanel();
		contentPane1.setLayout(new BorderLayout());
		final MainControlPanel cpanel = new MainControlPanel();
		contentPane1.add(mock, BorderLayout.CENTER);
		contentPane1.add(cpanel, BorderLayout.CENTER);
		frame1.setBounds(10, 10, 120, 180);
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame1.setContentPane(contentPane1);

		frame.setVisible(true);
		frame1.setVisible(true);
		mock.setVisible(false);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Dispose of the native component cleanly
				browserCanvas.dispose();
				mock.dispose();
			}
		});

		if (mock.initialise()) {
			mock.addLocationListener(new LocationListener() {

				@Override
				public void changing(LocationEvent event) {
//					System.out.println(Thread.currentThread() + " MOCK CHANGING: " + event);
					event.doit = false;
					Events.eventBus.post(new Events.WebBrowserOpenEvent(browserCanvas, event.location));
				}

				@Override
				public void changed(LocationEvent event) {
//					System.out.println(Thread.currentThread() + " MOCK CHANGED: " + event);
				}

			});
		}

		// Initialise the native browser component, and if successful...
		if (browserCanvas.initialise()) {
			// ...navigate to the desired URL

			browserCanvas.addLocationListener(new LocationListener() {

				@Override
				public void changing(LocationEvent event) {
//					System.out.println(Thread.currentThread() + " CHANGING: " + event);
				}

				@Override
				public void changed(LocationEvent event) {
//					System.out.println(Thread.currentThread() + " CHANGED: " + event);
					frame.setSize(1205, 805);
				}

			});
			
			browserCanvas.addOpenWindowListener(new OpenWindowListener() {

				@Override
				public void open(org.eclipse.swt.browser.WindowEvent event) {
//					System.out.println(Thread.currentThread() + " OPEN: " + event);
					event.required = true;
					event.browser = mock.getBrowser();
				}

			});
			
			browserCanvas.addProgressListener( new ProgressListener() {

				@Override
				public void changed(ProgressEvent event) {
				}

				@Override
				public void completed(ProgressEvent event) {
//					browserCanvas.setUrl("javascript:(function(F,i,r,e,b,u,g,L,I,T,E){if(F.getElementById(b))return;E=F[i+'NS']&&F.documentElement.namespaceURI;E=E?F[i+'NS'](E,'script'):F[i]('script');E[r]('id',b);E[r]('src',I+g+T);E[r](b,u);(F[e]('head')[0]||F[e]('body')[0]).appendChild(E);E=new%20Image;E[r]('src',I+L);})(document,'createElement','setAttribute','getElementsByTagName','FirebugLite','4','firebug-lite.js','releases/lite/latest/skin/xp/sprite.png','https://getfirebug.com/','#startOpened');");
				}
				
			});

			browserCanvas.setUrl("https://www.allstays.com/pro/index.php");
		} else {
			System.out.println("Failed to initialise browser");
		}
	}
}