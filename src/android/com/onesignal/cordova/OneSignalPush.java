/**
 * Modified MIT License
 *
 * Copyright 2021 OneSignal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * 1. The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * 2. All copies of substantial portions of the Software may only be used in connection
 * with services provided by OneSignal.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.onesignal.cordova;

import android.util.Log;

import com.onesignal.inAppMessages.IInAppMessage;
import com.onesignal.inAppMessages.IInAppMessageClickHandler;
import com.onesignal.inAppMessages.IInAppMessageClickResult;
import com.onesignal.inAppMessages.IInAppMessageLifecycleHandler;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal;
import com.onesignal.common.OneSignalUtils;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

public class OneSignalPush extends CordovaPlugin {
  private static final String TAG = "OneSignalPush";

  private static final String SET_NOTIFICATION_WILL_SHOW_IN_FOREGROUND_HANDLER = "setNotificationWillShowInForegroundHandler";
  private static final String SET_NOTIFICATION_OPENED_HANDLER = "setNotificationOpenedHandler";
  
  private static final String SET_CLICK_HANDLER = "setClickHandler";
  private static final String SET_LIFECYCLE_HANDLER = "setLifecycleHandler";
  private static final String SET_ON_WILL_DISPLAY_IN_APP_MESSAGE_HANDLER = "setOnWillDisplayInAppMessageHandler";
  private static final String SET_ON_DID_DISPLAY_IN_APP_MESSAGE_HANDLER = "setOnDidDisplayInAppMessageHandler";
  private static final String SET_ON_WILL_DISMISS_IN_APP_MESSAGE_HANDLER = "setOnWillDismissInAppMessageHandler";
  private static final String SET_ON_DID_DISMISS_IN_APP_MESSAGE_HANDLER = "setOnDidDismissInAppMessageHandler";

  private static final String COMPLETE_NOTIFICATION = "completeNotification";
  private static final String INIT = "init";

  private static final String GET_DEVICE_STATE = "getDeviceState";

  private static final String SET_LANGUAGE = "setLanguage";

  private static final String ADD_PERMISSION_OBSERVER = "addPermissionObserver";
  private static final String ADD_SUBSCRIPTION_OBSERVER = "addSubscriptionObserver";
  private static final String ADD_EMAIL_SUBSCRIPTION_OBSERVER = "addEmailSubscriptionObserver";
  private static final String ADD_SMS_SUBSCRIPTION_OBSERVER = "addSMSSubscriptionObserver";

  private static final String ADD_ALIASES = "addAliases";
  private static final String REMOVE_ALIASES = "removeAliases";

  private static final String REMOVE_TAGS = "removeTags";
  private static final String ADD_TAGS = "addTags";

  private static final String REGISTER_FOR_PROVISIONAL_AUTHORIZATION = "registerForProvisionalAuthorization";
  private static final String PROMPT_FOR_PUSH_NOTIFICATIONS_WITH_USER_RESPONSE = "promptForPushNotificationsWithUserResponse";
  private static final String UNSUBSCRIBE_WHEN_NOTIFICATIONS_DISABLED = "unsubscribeWhenNotificationsAreDisabled";

  private static final String CLEAR_ONESIGNAL_NOTIFICATIONS = "clearOneSignalNotifications";
  private static final String REMOVE_NOTIFICATION = "removeNotification";
  private static final String REMOVE_GROUPED_NOTIFICATIONS = "removeGroupedNotifications";

  private static final String DISABLE_PUSH = "disablePush";
  private static final String POST_NOTIFICATION = "postNotification";
  private static final String SET_LAUNCH_URLS_IN_APP = "setLaunchURLsInApp";

  private static final String ADD_EMAIL = "addEmail";
  private static final String REMOVE_EMAIL = "removeEmail";

  private static final String ADD_SMS_NUMBER = "addSmsNumber";
  private static final String REMOVE_SMS_NUMBER = "removeSmsNumber";

  private static final String SET_LOG_LEVEL = "setLogLevel";
  private static final String SET_ALERT_LEVEL = "setAlertLevel";

  private static final String SET_LOCATION_SHARED = "setLocationShared";
  private static final String IS_LOCATION_SHARED = "isLocationShared";
  private static final String PROMPT_LOCATION = "promptLocation";

  private static final String USER_PROVIDED_CONSENT = "userProvidedPrivacyConsent";
  private static final String REQUIRES_CONSENT = "requiresUserPrivacyConsent";
  private static final String SET_REQUIRES_CONSENT = "setRequiresUserPrivacyConsent";
  private static final String PROVIDE_USER_CONSENT = "provideUserConsent";

  private static final String ADD_TRIGGERS = "addTriggers";
  private static final String REMOVE_TRIGGERS = "removeTriggers";
  private static final String CLEAR_TRIGGERS = "clearTriggers";
  private static final String SET_PAUSED = "setPaused";
  private static final String IS_PAUSED = "isPaused";

  private static final String ADD_OUTCOME = "addOutcome";
  private static final String ADD_UNIQUE_OUTCOME = "addUniqueOutcome";
  private static final String ADD_OUTCOME_WITH_VALUE = "addOutcomeWithValue";

  private static final HashMap<String, OSNotificationReceivedEvent> notificationReceivedEventCache = new HashMap<>();

  private static CallbackContext jsInAppMessageWillDisplayCallback;
  private static CallbackContext jsInAppMessageDidDisplayCallBack;
  private static CallbackContext jsInAppMessageWillDismissCallback;
  private static CallbackContext jsInAppMessageDidDismissCallBack;

  public boolean setNotificationWillShowInForegroundHandler(CallbackContext callbackContext) {
    OneSignal.setNotificationWillShowInForegroundHandler(new CordovaNotificationInForegroundHandler(callbackContext));
    return true;
  }

  public boolean setNotificationOpenedHandler(CallbackContext callbackContext) {
    OneSignal.setNotificationOpenedHandler(new CordovaNotificationOpenHandler(callbackContext));
    return true;
  }

  public boolean setClickHandler(CallbackContext callbackContext) {
    OneSignal.getInAppMessages().setInAppMessageClickHandler(new CordovaInAppMessageClickHandler(callbackContext));
    return true;
  }

  public boolean setLifecycleHandler() {
    OneSignal.getInAppMessages().setInAppMessageLifecycleHandler(new IInAppMessageLifecycleHandler() {
      @Override
      public void onWillDisplayInAppMessage(IInAppMessage message) {
        try {
          JSONObject onWillDisplayResult = new JSONObject();
            
          onWillDisplayResult.put("messageId", message.getMessageId());
          
          if (jsInAppMessageWillDisplayCallback != null) {
            CallbackHelper.callbackSuccess(jsInAppMessageWillDisplayCallback, onWillDisplayResult);
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
      @Override
      public void onDidDisplayInAppMessage(IInAppMessage message) {
        try {
          JSONObject onDidDisplayResult = new JSONObject();

          onDidDisplayResult.put("messageId", message.getMessageId());
          
          if (jsInAppMessageDidDisplayCallBack != null) {
            CallbackHelper.callbackSuccess(jsInAppMessageDidDisplayCallBack, onDidDisplayResult);
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
      @Override
      public void onWillDismissInAppMessage(IInAppMessage message) {
        try {
          JSONObject onWillDismissResult = new JSONObject();

          onWillDismissResult.put("messageId", message.getMessageId());
          
          if (jsInAppMessageWillDismissCallback != null) {
            CallbackHelper.callbackSuccess(jsInAppMessageWillDismissCallback, onWillDismissResult);
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
      @Override
      public void onDidDismissInAppMessage(IInAppMessage message) {
        try {
          JSONObject onDidDismissResult = new JSONObject();

          onDidDismissResult.put("messageId", message.getMessageId());
          
          if (jsInAppMessageDidDismissCallBack != null) {
            CallbackHelper.callbackSuccess(jsInAppMessageDidDismissCallBack, onDidDismissResult);
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    });
    return true;
  }

  public boolean setOnWillDisplayInAppMessageHandler(CallbackContext callbackContext) {
    jsInAppMessageWillDisplayCallback = callbackContext;
    return true;
  }

  public boolean setOnDidDisplayInAppMessageHandler(CallbackContext callbackContext) {
    jsInAppMessageDidDisplayCallBack = callbackContext;
    return true;
  }

  public boolean setOnWillDismissInAppMessageHandler(CallbackContext callbackContext) {
    jsInAppMessageWillDismissCallback = callbackContext;
    return true;
  }

  public boolean setOnDidDismissInAppMessageHandler(CallbackContext callbackContext) {
    jsInAppMessageDidDismissCallBack = callbackContext;
    return true;
  }

  public boolean init(JSONArray data) {
    try {
      String appId = data.getString(0);

      OneSignalUtils.INSTANCE.setSdkType("cordova");

      OneSignal.initWithContext(this.cordova.getActivity(), appId);

      return true;
    } catch (JSONException e) {
      Log.e(TAG, "execute: Got JSON Exception " + e.getMessage());
      return false;
    }
  }

  @Override
  public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {
    boolean result = false;

    switch(action) {
      case SET_NOTIFICATION_OPENED_HANDLER:
        result = setNotificationOpenedHandler(callbackContext);
        break;

      case SET_NOTIFICATION_WILL_SHOW_IN_FOREGROUND_HANDLER:
        result = setNotificationWillShowInForegroundHandler(callbackContext);
        break;

      case SET_CLICK_HANDLER:
        result = setClickHandler(callbackContext);
        break;

      case SET_LIFECYCLE_HANDLER:
        result = setLifecycleHandler();
        break;

      case SET_ON_WILL_DISPLAY_IN_APP_MESSAGE_HANDLER:
        result = setOnWillDisplayInAppMessageHandler(callbackContext);
        break;

      case SET_ON_DID_DISPLAY_IN_APP_MESSAGE_HANDLER:
        result = setOnDidDisplayInAppMessageHandler(callbackContext);
        break;

      case SET_ON_WILL_DISMISS_IN_APP_MESSAGE_HANDLER:
        result = setOnWillDismissInAppMessageHandler(callbackContext);
        break;

      case SET_ON_DID_DISMISS_IN_APP_MESSAGE_HANDLER:
        result = setOnDidDismissInAppMessageHandler(callbackContext);
        break;

      case COMPLETE_NOTIFICATION:
        result = completeNotification(data);
        break;

      case INIT:
        result = init(data);
        break;

      case GET_DEVICE_STATE:
        result = OneSignalController.getDeviceState(callbackContext);
        break;

      case SET_LANGUAGE:
        result = OneSignalController.setLanguage(data);
        break;

      case ADD_PERMISSION_OBSERVER:
        result = OneSignalObserverController.addPermissionObserver(callbackContext);
        break;

      case ADD_SUBSCRIPTION_OBSERVER:
        result = OneSignalObserverController.addSubscriptionObserver(callbackContext);
        break;

      case ADD_EMAIL_SUBSCRIPTION_OBSERVER:
        result = OneSignalObserverController.addEmailSubscriptionObserver(callbackContext);
        break;

      case ADD_SMS_SUBSCRIPTION_OBSERVER:
        result = OneSignalObserverController.addSMSSubscriptionObserver(callbackContext);
        break;

      case ADD_ALIASES:
        result = OneSignalController.addAliases(data);
        break;

      case REMOVE_ALIASES:
        result = OneSignalController.removeAlias(data);
        break;

      case ADD_TAGS:
        result = OneSignalController.addTags(data);
        break;

      case REMOVE_TAGS:
        result = OneSignalController.removeTags(data);
        break;

      case REGISTER_FOR_PROVISIONAL_AUTHORIZATION:
        result = OneSignalController.registerForProvisionalAuthorization();
        break;

      case PROMPT_FOR_PUSH_NOTIFICATIONS_WITH_USER_RESPONSE:
        result = OneSignalController.promptForPushNotificationsWithUserResponse(callbackContext, data);
        break;

      case UNSUBSCRIBE_WHEN_NOTIFICATIONS_DISABLED:
        result = OneSignalController.unsubscribeWhenNotificationsAreDisabled(data);
        break;

      case CLEAR_ONESIGNAL_NOTIFICATIONS:
        result = OneSignalController.clearOneSignalNotifications();
        break;

      case REMOVE_NOTIFICATION:
        result = OneSignalController.removeNotification(data);
        break;

      case REMOVE_GROUPED_NOTIFICATIONS:
        result = OneSignalController.removeGroupedNotifications(data);
        break;

      case DISABLE_PUSH:
        result = OneSignalController.disablePush(data);
        break;

      case POST_NOTIFICATION:
        result = OneSignalController.postNotification(callbackContext, data);
        break;

      case SET_LAUNCH_URLS_IN_APP:
        result = OneSignalController.setLaunchURLsInApp();
        break;

      case SET_LOG_LEVEL:
        OneSignalController.setLogLevel(data);
        break;

      case SET_ALERT_LEVEL:
        OneSignalController.setAlertLevel(data);
        break;

      case ADD_EMAIL:
        result = OneSignalEmailController.addEmail(data);
        break;

      case REMOVE_EMAIL:
        result = OneSignalEmailController.removeEmail(data);
        break;

      case ADD_SMS_NUMBER:
        result = OneSignalSMSController.addSmsNumber(data);
        break;

      case REMOVE_SMS_NUMBER:
        result = OneSignalSMSController.removeSmsNumber(data);
        break;

      case PROMPT_LOCATION:
        OneSignalController.promptLocation();
        break;

      case SET_LOCATION_SHARED:
        OneSignalController.setLocationShared(data);
        break;

      case IS_LOCATION_SHARED:
        result = OneSignalController.isLocationShared(callbackContext);
        break;

      case USER_PROVIDED_CONSENT:
        result = OneSignalController.userProvidedConsent(callbackContext);
        break;

      case REQUIRES_CONSENT:
        result = OneSignalController.requiresUserPrivacyConsent(callbackContext);
        break;

      case SET_REQUIRES_CONSENT:
        result = OneSignalController.setRequiresConsent(callbackContext, data);
        break;

      case PROVIDE_USER_CONSENT:
        result = OneSignalController.provideUserConsent(data);
        break;

      case ADD_TRIGGERS:
        result = OneSignalInAppMessagingController.addTriggers(data);
        break;

      case REMOVE_TRIGGERS:
        result = OneSignalInAppMessagingController.removeTriggers(data);
        break;

      case CLEAR_TRIGGERS:
        result = OneSignalInAppMessagingController.clearTriggers();
        break;

      case SET_PAUSED:
        result = OneSignalInAppMessagingController.setPaused(data);
        break;

      case IS_PAUSED:
        result = OneSignalInAppMessagingController.isPaused(callbackContext);
        break;

      case ADD_OUTCOME:
        result = OneSignalOutcomeController.addOutcome(data);
        break;

      case ADD_UNIQUE_OUTCOME:
        result = OneSignalOutcomeController.addUniqueOutcome(data);
        break;

      case ADD_OUTCOME_WITH_VALUE:
        result = OneSignalOutcomeController.addOutcomeWithValue(data);
        break;

      default:
        Log.e(TAG, "Invalid action : " + action);
        CallbackHelper.callbackError(callbackContext, "Invalid action : " + action);
    }

    return result;
  }

  private boolean completeNotification(JSONArray data) {
    try {
      String notificationId = data.getString(0);
      boolean shouldDisplay = data.getBoolean(1);

      OSNotificationReceivedEvent notificationReceivedEvent = notificationReceivedEventCache.get(notificationId);

      if (notificationReceivedEvent == null) {
        OneSignal.onesignalLog(OneSignal.LOG_LEVEL.ERROR, "Could not find notification completion block with id: " + notificationId);
        return false;
      }

      if (shouldDisplay)
        notificationReceivedEvent.complete(notificationReceivedEvent.getNotification());
      else
        notificationReceivedEvent.complete(null);

      return true;
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Handlers
   */

  private static class CordovaNotificationInForegroundHandler implements OneSignal.OSNotificationWillShowInForegroundHandler {

    private CallbackContext jsNotificationInForegroundCallBack;

    public CordovaNotificationInForegroundHandler(CallbackContext inCallbackContext) {
      jsNotificationInForegroundCallBack = inCallbackContext;
    }

    @Override
    public void notificationWillShowInForeground(OSNotificationReceivedEvent notificationReceivedEvent) {
      try {
        OSNotification notification = notificationReceivedEvent.getNotification();
        notificationReceivedEventCache.put(notification.getNotificationId(), notificationReceivedEvent);

        CallbackHelper.callbackSuccess(jsNotificationInForegroundCallBack, notification.toJSONObject());
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  private static class CordovaNotificationOpenHandler implements OneSignal.OSNotificationOpenedHandler {

    private CallbackContext jsNotificationOpenedCallBack;

    public CordovaNotificationOpenHandler(CallbackContext inCallbackContext) {
      jsNotificationOpenedCallBack = inCallbackContext;
    }

    @Override
    public void notificationOpened(OSNotificationOpenedResult result) {
      try {
        if (jsNotificationOpenedCallBack != null)
          CallbackHelper.callbackSuccess(jsNotificationOpenedCallBack, result.toJSONObject());
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  private static class CordovaInAppMessageClickHandler implements IInAppMessageClickHandler {

    private CallbackContext jsInAppMessageClickedCallback;

    public CordovaInAppMessageClickHandler(CallbackContext inCallbackContext) {
      jsInAppMessageClickedCallback = inCallbackContext;
    }

    @Override
    public void inAppMessageClicked(IInAppMessageClickResult result) {
      try {
        JSONObject clickResults = new JSONObject();

        clickResults.put("isFirstClick", result.getAction().isFirstClick());
        clickResults.put("closesMessage", result.getAction().getClosesMessage());
        clickResults.put("clickName", result.getAction().getClickName());
        clickResults.put("clickUrl", result.getAction().getClickUrl());

        CallbackHelper.callbackSuccess(jsInAppMessageClickedCallback, clickResults);
      }
      catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onDestroy() {
    OneSignal.setNotificationOpenedHandler(null);
    OneSignal.setNotificationWillShowInForegroundHandler(null);
  }
}
