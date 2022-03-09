package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedservlets.websockets.*;
import com.guicedee.guicedservlets.websockets.services.*;
import com.guicedee.logger.*;
import com.jwebmp.core.annotations.*;
import com.jwebmp.core.implementations.*;
import com.jwebmp.core.services.*;
import io.undertow.server.*;
import io.undertow.servlet.*;
import io.undertow.servlet.api.*;
import io.undertow.websockets.jsr.*;
import org.xnio.*;

import java.util.*;
import java.util.logging.*;

import static io.undertow.servlet.Servlets.*;
import static io.undertow.websockets.jsr.WebSocketDeploymentInfo.*;

public class JWebMPGuicedUndertowWebSocketConfiguration implements IWebSocketPreConfiguration<JWebMPGuicedUndertowWebSocketConfiguration>
{
	private static final Logger log = LogFactory.getLog("UndertowWebSockets");
	private static WebSocketDeploymentInfo webSocketDeploymentInfo;
	private static HttpHandler webSocketHandler;
	
	/**
	 * Returns the WebSocketDeploymentInfo for use in the Servlet Extension
	 *
	 * @return The Web Socket Deployment Info
	 */
	public static WebSocketDeploymentInfo getWebSocketDeploymentInfo()
	{
		return webSocketDeploymentInfo;
	}
	
	public static HttpHandler getWebSocketHandler()
	{
		return webSocketHandler;
	}
	
	@Override
	public void configure()
	{
		log.config("Setting up XNIO for Websockets at /wssocket for each page specified");
		Xnio xnio = Xnio.getInstance("nio");
		XnioWorker xnioWorker;
		try
		{
			xnioWorker = xnio.createWorker(OptionMap.builder()
			                                        .getMap());
			webSocketDeploymentInfo = new WebSocketDeploymentInfo()
					.addEndpoint(GuicedWebSocket.class)
					.setWorker(xnioWorker);
			
			for (Map.Entry<PageConfiguration, IPage<?>> entry : JWebMPSiteBinder.getPageConfigurations()
			                                                                    .entrySet())
			{
				PageConfiguration pc = entry.getKey();
				IPage<?> page = entry.getValue();
				String url = pc.url();
				if (url.endsWith("/"))
				{
					url = url.substring(0, url.length() - 1);
				}
				url = url + "/wssocket";
				
				DeploymentInfo websocketDeployment = deployment()
						.setContextPath(url.toString())
						.addServletContextAttribute(ATTRIBUTE_NAME, webSocketDeploymentInfo)
						.setDeploymentName("websocket-deployment-" + page.getClass()
						                                                 .getSimpleName())
						.setClassLoader(Thread.currentThread()
						                      .getContextClassLoader());
				
				DeploymentManager manager = Servlets.defaultContainer()
				                                    .addDeployment(websocketDeployment);
				
				manager.deploy();
				log.fine("Registering Page WebSockets in Undertow - [" + url + "]");
				webSocketHandler = manager.start();
				log.fine("Completed Page WebSocket [" + url + "]");
			}
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "Unable to configure XNIO with WebSocket Handler", e);
		}
	}
	
	@Override
	public Integer sortOrder()
	{
		return 5;
	}
	
	@Override
	public boolean enabled()
	{
		return true;
	}
}
