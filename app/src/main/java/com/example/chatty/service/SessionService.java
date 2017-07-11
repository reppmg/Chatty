package com.example.chatty.service;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatty.presenter.SessionCommunicator;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Service, dealing with communication with OpenTok.
 * Listens for events from OpenTok about another user connecting, disconnecting etc
 */

public class SessionService implements Session.SessionListener, PublisherKit.PublisherListener {
    private static final String TAG = SessionService.class.getSimpleName();
    private static final String appURL = "https://onetock.herokuapp.com";

    private String apiKey;
    private String sessionId;
    private String token;

    private final Context mContext;

    private Subscriber mSubscriber;
    private Publisher mPublisher;
    private Session mSession;

    private SessionCommunicator mSessionCommunicator;
    private RequestQueue mRequestQueue;
    private Pinger pinger;

    public SessionService(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(mContext);
    }


    /**
     * fetching data (apiKey, sessionId, token) from server, that puts the user in queue
     */
    public void fetchSessionConnectionData() {
        Log.d(TAG, "fetchSessionConnectionData: ");
        mRequestQueue.add(new JsonObjectRequest(Request.Method.GET,
                appURL + "/session",
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    apiKey = response.getString("apiKey");
                    sessionId = response.getString("sessionId");
                    token = response.getString("token");

                    Log.i(TAG, "API_KEY: " + apiKey);
                    Log.i(TAG, "SESSION_ID: " + sessionId);
                    Log.i(TAG, "TOKEN: " + token);

                    mSession = new Session.Builder(mContext, apiKey, sessionId).build();
                    mSession.setSessionListener(SessionService.this);
                    mSession.connect(token);

                    pinger = new Pinger(mSessionCommunicator, mSession);
                    mSession.setReconnectionListener(new ReconnectionListener(mSessionCommunicator, pinger));
                    mSession.setSignalListener(pinger);
                } catch (JSONException error) {
                    Log.e(TAG, "Web Service error: " + error.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Web Service error: " + error.getMessage());
                mSessionCommunicator.onError();
            }
        }));
    }

    @Override
    public void onConnected(Session session) {
        Log.i(TAG, "Session Connected");

        //we've connected to the session, so can explicitly set WaitingView
        mSessionCommunicator.dropSubscriberView();

        mPublisher = new Publisher.Builder(mContext).build();
        mPublisher.setPublisherListener(this);

        mPublisher.startPreview();
        mSession.publish(mPublisher);
        Log.d(TAG, "mPublisher: publishing the stream");
        mSessionCommunicator.showPublisher();
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(TAG, "Session Disconnected");
        if (mPublisher != null) {
            mPublisher.destroy();
        }
        if (mSubscriber != null) {
            mSubscriber.destroy();
        }
        mSession = null;
    }


    /**
     * Another user is connected to the session
     * passing the view with video to presenter
     */
    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(TAG, "Stream Received");
        mSubscriber = new Subscriber.Builder(mContext, stream).build();
        mSession.subscribe(mSubscriber);
        mSessionCommunicator.showSubscriber();
        //start pinging in case of internet failure
        pinger.sendPing();
    }


    /**
     * When opponent disconnects, put user back in waiting queue
     */
    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(TAG, "Stream Dropped");
        //put me in queue again
        pinger.stop();
        mSession.setSignalListener(null);
        session.disconnect();
        mSessionCommunicator.dropSubscriberView();
        fetchSessionConnectionData();
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(TAG, "Session error: " + opentokError.getMessage());
        session.disconnect();
        mSessionCommunicator.onError();
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.d(TAG, "onStreamCreated: ");

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.d(TAG, "onStreamDestroyed: ");
        unsubscribe();
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.d(TAG, "onError: " + opentokError.getMessage());
        pinger.stop();

        mSession.setSignalListener(null);
        mSessionCommunicator.onError();
    }

    public void setPresenter(SessionCommunicator presenter) {
        Log.d(TAG, "setPresenter: ");
        this.mSessionCommunicator = presenter;
    }

    /*When user is in queue, and application closes*/
    public void unsubscribe() {
        if (mSession != null) {
            RequestQueue request = Volley.newRequestQueue(mContext);
            request.add(new StringRequest(Request.Method.GET,
                    appURL + "/unsubscribe/" + mSession.getSessionId(),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "unsubscribe onResponse: " + response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "unsubscribe onErrorResponse: " + error.getMessage());

                        }
                    }));
        }
    }

    public void disconnect() {
        Log.d(TAG, "disconnect: ");
        if (mSession != null) {
            Log.d(TAG, "disconnect: sessionId: " + mSession.getSessionId());
            mSession.disconnect();
            mSession = null;
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getToken() {
        return token;
    }

    public View getPublisherView() {
        if (mPublisher == null)
            return null;
        Log.d(TAG, "getPublisherView: ");
        mPublisher.setPublishVideo(true);
        mPublisher.startPreview();
        return mPublisher.getView();
    }

    public View getSubscriberView() {
        if (mSubscriber == null) {
            return null;
        }
        return mSubscriber.getView();
    }


    public boolean isInSession() {
        return mSession != null;
    }

}
