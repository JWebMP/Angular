package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedservlets.websockets.GuicedWebSocket;
import com.guicedee.guicedservlets.websockets.services.IWebSocketPreConfiguration;
import com.jwebmp.core.annotations.PageConfiguration;
import com.jwebmp.core.implementations.JWebMPSiteBinder;
import com.jwebmp.core.services.IPage;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import lombok.extern.java.Log;
import org.xnio.OptionMap;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.util.Map;
import java.util.logging.Level;

import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.websockets.jsr.WebSocketDeploymentInfo.ATTRIBUTE_NAME;
@Log
public class JWebMPGuicedUndertowWebSocketConfiguration implements IWebSocketPreConfiguration<JWebMPGuicedUndertowWebSocketConfiguration>
{
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
