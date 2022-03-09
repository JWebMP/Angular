package com.jwebmp.core.base.angular.modules.services;

import com.google.inject.*;
import com.guicedee.guicedinjection.interfaces.*;
import com.guicedee.guicedservlets.websockets.services.*;
import com.jwebmp.core.base.angular.modules.services.angular.*;
import com.jwebmp.core.base.angular.modules.services.base.*;
import com.jwebmp.core.base.angular.modules.services.observable.*;
import com.jwebmp.core.base.angular.modules.services.storage.*;
import com.jwebmp.core.base.angular.modules.services.websocket.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

@NgProvider(singleton = true)
@NgModuleReference(InjectableModule.class)
@NgModuleReference(ObservableModule.class)
@NgModuleReference(NgRouterModule.class)
//@NgModuleReference(WebSocketSubjectModule.class)
@NgModuleReference(WebSocketModule.class)
@NgModuleReference(OnInitModule.class)
@NgModuleReference(OnDestroyModule.class)
@NgDataTypeReference(EnvironmentModule.class)
@NgModuleReference(NgLocationModule.class)
@NgModuleReference(ElementRefModule.class)
public class SocketClientService implements INgProvider<SocketClientService>
{
	public SocketClientService()
	{
	}
	
	@Override
	public Set<String> interfaces()
	{
		return Set.of("OnInit", "OnDestroy");
	}
	
	@Override
	public List<String> declarations()
	{
		return List.of("SocketClientService");
	}
	
	@Override
	public Set<String> providers()
	{
		//	return Set.of();
		return Set.of("SocketClientService");
	}
	
	@Override
	public Set<String> decorators()
	{
		return Set.of("@Injectable({\n" +
		              "  providedIn: 'root'\n" +
		              "})");
	}
	
	@Override
	public Set<String> globalFields()
	{
		return Set.of();
	}
	
	@Override
	public Set<String> fields()
	{
		return Set.of("websocket: any;" +
		              "");
	}
	
	@Override
	public Set<String> constructorParameters()
	{
		return Set.of("private routeLocation: Location",
				"private router: Router");
	}
	
	@Override
	public List<String> constructorBody()
	{
		List<String> l = new ArrayList<>(List.of(
		                               "const subject = webSocket((location.protocol + '//' + location.host).replace('http','ws') + '/wssocket');" +//      "alert('subject made');\n" +
		                                "this.websocket = subject; \n"
		
		));
		//"subject.subscribe();",
		//	"alert('ws?');" +
		
		/*		"this.myWebSocket = WebSocketSubject.create();",*/
		@SuppressWarnings({"unchecked", "rawtypes"})
		Set<IWebSocketAuthDataProvider> authProviders = IDefaultService.loaderToSet(ServiceLoader.load(IWebSocketAuthDataProvider.class));
		for (IWebSocketAuthDataProvider<?> authProvider : authProviders)
		{
			StringBuilder js = authProvider.getJavascriptToPopulate();
			l.add(js.toString());
		}
		
		l.add("subject.asObservable().subscribe(    \n" +
		      "   msg => this.processResult(msg), \n" +
		      "   // Called whenever there is a message from the server    \n" +
		      "   err => console.log(err), \n" +
		      "   // Called if WebSocket API signals some kind of error    \n" +
		      "   () => console.log('complete') \n" +
		      "   // Called when connection is closed (for whatever reason)  \n" +
		      ");" +
		      "" +
		      "" +
		      "" +
		      "" +
		      "\n");
		return l;
	}
	
	@Override
	public List<String> methods()
	{
		return List.of("ngOnInit() {\n" +
		               "alert('onInit');" +
		               "const subject = webSocket((location.protocol + '//' + location.host).replace('http','ws') + '/wssocket');\n" +
		               //      "alert('subject made');\n" +
		               "this.websocket = subject; \n" +
		               "}"
				,
				"ngOnDestroy() {" +
				"}",
				
				"getParametersObject() : object {\n" +
				"    try {\n" +
				"        var search = location.search.substring(1);\n" +
				"        return JSON.parse('{\"' + decodeURI(search).replace(/\"/g, '\\\\\"').replace(/&/g, '\",\"').replace(/=/g, '\":\"') + '\"}');\n" +
				"    } catch (err) {\n" +
				"        return {};\n" +
				"    }\n" +
				"}\n",
				
				"send(action:string,data:object, eventType :string,event? : Event, component? : ElementRef<any>) : void {\n" +
				"" +
				//"alert('sending...');" +
				"const news : any = {\n" +
				"};\n" +
				"news.data = data;\n" +
				"news.action = action;\n" +
				"news.data.localStorage = window.localStorage;\n" +
				"news.data.sessionStorage = window.sessionStorage;\n" +
				"news.data.parameters = this.getParametersObject();\n" +
				"news.data.hashbang = window.location.hash;\n" +
				"news.data.route = this.routeLocation.path();\n" +
				"news.data.datetime = new Date().getUTCDate();\n" +
				"news.data.eventType = eventType;\n" +
				"news.data.headers = {};\n" +
				"news.data.headers.useragent = navigator.userAgent;\n" +
				"news.data.headers.appClassName = EnvironmentModule.appClass;\n" +
				"news.data.headers.cookieEnabled = navigator.cookieEnabled ;\n" +
				"news.data.headers.appName = navigator.appName  ;\n" +
				"news.data.headers.appVersion = navigator.appVersion   ;\n" +
				"news.data.headers.language = navigator.language ;\n" +
				"" +
				"if(event)\n" +
				"{\n" +
				"   news.event = JSON.stringify(event);\n" +
				"}\n" +
				"" +
				"if(component)\n" +
				"{\n" +
				"   let elementId: string = (event?.target as Element).id; \n" +
				"   let attributeNames : string[] = (event?.target as Element).getAttributeNames();\n" +
				"   let ele = (event?.target as Element);\n" +
				//" alert(attributeNames);  " +
				" " +
				"  let attributes : any = {};\n" +
				//"  debugger; " +
				"   for(let attr of attributeNames)\n" +
				"\t\t\t\t  {\n" +
				"                      try {\n" +
				"\t\t\t\t     attributes[attr] = ele.getAttribute(attr);\n" +
				"                      }catch(error)\n" +
				"                      {\n" +
				"                          console.log(error);\n" +
				"                      }\n" +
				"\t\t\t\t   }  " +
				"news.data.attributes = attributes;\n" +
				"news.componentId = elementId;\n" +
				"" +
				"   " +
				"}\n" +
				"" +
				//	"alert('news : ' + JSON.stringify(news));" +
				"this.websocket.next(news);\n" +
				"}\n",
				
				"" +
				"" +
				"processResult(response:any)\n" +
				"{\n" +
				//	"   console.log('message received: ' + JSON.stringify(response));\n" +
				"   if(response.localStorage)\n" +
				"   {\n" +
				"      " +
				//	"       alert('update local storage');" +
				//	"       alert('ttt - ' + typeof response.localStorage);" +
				"       " +
				"      Object.keys(response.localStorage).forEach(prop => {\n" +
				"         window.localStorage.setItem(prop, response.localStorage[prop]);\n" +
				"           });\n" +
				"" +
				"   }\n" +
				"" +
				"   if(response.sessionStorage)\n" +
				"   {\n" +
				"      " +
				//	"       alert('update local storage');" +
				//	"       alert('ttt - ' + typeof response.localStorage);" +
				"       " +
				"      Object.keys(response.sessionStorage).forEach(prop => {\n" +
				"         window.sessionStorage.setItem(prop, response.sessionStorage[prop]);\n" +
				"           });\n" +
				"" +
				"   }\n" +
				"" +
				"if(response.features)\n" +
				"{\n" +
				"   " +
				"}\n" +
				"" +
				"" +
				//	"debugger;" +
				"if(response.reactions)\n" +
				"{\n" +
				"    for(let reaction of response.reactions)\n" +
				"   {\n" +
				"      const react : any = reaction; \n" +
				"      if(\"RedirectUrl\" == react.reactionType)\n" +
				"       {\n" +
				//	"          alert('redirect to - ' + react.reactionMessage);\n" +
				"            this.router.navigateByUrl(react.reactionMessage);\n " +
				"       }\n" +
				"   }\n" +
				"}\n" +
				"" +
				"" +
				"" +
				"" +
				"" +
				"}\n"
		);
	}
}
