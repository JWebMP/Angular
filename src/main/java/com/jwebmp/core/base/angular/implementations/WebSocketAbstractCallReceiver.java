package com.jwebmp.core.base.angular.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guicedee.client.CallScopeProperties;
import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedservlets.websockets.options.WebSocketMessageReceiver;
import com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver;
import com.guicedee.services.jsonrepresentation.IJsonRepresentation;
import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.utilities.EscapeChars;
import com.jwebmp.interception.services.AjaxCallIntercepter;
import io.smallrye.mutiny.Uni;
import lombok.extern.java.Log;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import static com.guicedee.client.IGuiceContext.get;
import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.DefaultObjectMapper;
import static com.jwebmp.interception.services.JWebMPInterceptionBinder.AjaxCallInterceptorKey;

@Log
public abstract class WebSocketAbstractCallReceiver<J extends WebSocketAbstractCallReceiver<J>>
        implements IWebSocketMessageReceiver<AjaxResponse<?>, J>
{
    public abstract String getMessageDirector();

    public abstract Uni<AjaxResponse<?>> action(AjaxCall<?> call, AjaxResponse<?> response);

    @Override
    public Set<String> messageNames()
    {
        Set<String> messageNames = new HashSet<>();
        messageNames.add(getMessageDirector());
        return messageNames;
    }

    @Override
    public Uni<AjaxResponse<?>> receiveMessage(WebSocketMessageReceiver message) throws SecurityException
    {
        return Uni.createFrom()
                  .item(message)
                  .onItem()
                  .transformToUni(m -> {
                      AjaxResponse<?> ajaxResponse = get(AjaxResponse.class);
                      try
                      {
                          AjaxCall<?> ajaxCall = get(AjaxCall.class);
                          ObjectMapper om = IJsonRepresentation.getObjectMapper();
                          String originalValues = om.writeValueAsString(m.getData());
                          CallScopeProperties properties = IGuiceContext.get(CallScopeProperties.class);
                          AjaxCall<?> call = om.readValue(originalValues, AjaxCall.class);
                          ajaxCall.fromCall(call);
                          for (AjaxCallIntercepter<?> ajaxCallIntercepter : get(AjaxCallInterceptorKey))
                          {
                              ajaxCallIntercepter.intercept(ajaxCall, ajaxResponse);
                          }
                          return action(ajaxCall, ajaxResponse)
                                  .onItem()
                                  .invoke(resp -> {
                                      if (resp != null && !ajaxCall.getSessionStorage()
                                                                   .containsKey("contextId"))
                                      {
                                          if (properties.getProperties()
                                                        .containsKey("RequestContextId"))
                                          {
                                              resp.getSessionStorage()
                                                  .put("contextId", properties.getProperties()
                                                                              .get("RequestContextId")
                                                                              .toString());
                                          }
                                      }
                                  })
                                  .replaceWith(ajaxResponse);
                      }
                      catch (Exception T)
                      {
                          ajaxResponse.setSuccess(false);
                          AjaxResponseReaction<?> arr = new AjaxResponseReaction<>("Unknown Error",
                                  "An AJAX call resulted in an unknown server error<br>" + T.getMessage() + "<br>" +
                                          EscapeChars.forHTML(ExceptionUtils.getStackTrace(T)), ReactionType.DialogDisplay);
                          arr.setResponseType(AjaxResponseType.Danger);
                          ajaxResponse.addReaction(arr);
                          WebSocketAbstractCallReceiver.log.log(Level.SEVERE, "Unknown in ajax reply\n", T);
                          return Uni.createFrom()
                                    .item(ajaxResponse);
                      }
                      catch (Throwable T)
                      {
                          ajaxResponse.setSuccess(false);
                          AjaxResponseReaction<?> arr = new AjaxResponseReaction<>("Unknown Error",
                                  "An AJAX call resulted in an internal server error<br>" + T.getMessage() + "<br>" +
                                          EscapeChars.forHTML(ExceptionUtils.getStackTrace(T)), ReactionType.DialogDisplay);
                          arr.setResponseType(AjaxResponseType.Danger);
                          ajaxResponse.addReaction(arr);
                          WebSocketAbstractCallReceiver.log.log(Level.SEVERE, "Unknown in ajax reply\n", T);
                          return Uni.createFrom()
                                    .item(ajaxResponse);
                      }
                  });
    }
}
