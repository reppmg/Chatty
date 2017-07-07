package com.example.chatty.service;

import android.content.Context;
import android.util.Log;

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
 *
 */

public class SessionService implements Session.SessionListener, PublisherKit.PublisherListener {
    private static final String LOG_TAG = Session.SessionListener.class.getSimpleName();
    private static final String appURL = "https://onetock.herokuapp.com";

    private String API_KEY;
    private String SESSION_ID;
    private String TOKEN;

    private final Context mContext;

    private Subscriber mSubscriber;
    private Publisher mPublisher;
    private Session mSession;

    private SessionCommunicator mSessionCommunicator;

    public SessionService(Context context) {
        mContext = context;
    }


    /**
     * fetching data (api_key, sessionId, token) from server, that puts the user in queue
     */
    public void fetchSessionConnectionData() {
        RequestQueue reqQueue = Volley.newRequestQueue(mContext);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                appURL + "/session",
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    API_KEY = response.getString("apiKey");
                    SESSION_ID = response.getString("sessionId");
                    TOKEN = response.getString("token");

                    Log.i(LOG_TAG, "API_KEY: " + API_KEY);
                    Log.i(LOG_TAG, "SESSION_ID: " + SESSION_ID);
                    Log.i(LOG_TAG, "TOKEN: " + TOKEN);

                    mSession = new Session.Builder(mContext, API_KEY, SESSION_ID).build();
                    mSession.setSessionListener(SessionService.this);
                    mSession.connect(TOKEN);

                } catch (JSONException error) {
                    Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
                mSessionCommunicator.onError();
            }
        }));
    }

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");

        mPublisher = new Publisher.Builder(mContext).build();
        mPublisher.setPublisherListener(this);

        mSessionCommunicator.onNewSubscriber(mPublisher.getView());
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Session Disconnected");
        if (mPublisher != null) {
            mPublisher.destroy();
        }
        if (mSubscriber != null) {
            mSubscriber.destroy();
        }
    }


    /**
     * Another user is connected to the session
     * passing the view with video to presenter
     */
    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");

        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(mContext, stream).build();
            mSession.subscribe(mSubscriber);
            mSessionCommunicator.streamReceived(mSubscriber.getView());
        }
    }


    /**
     * When opponent disconnects, put user back in waiting queue
     */
    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");
        //put me in queue again
        if (mSubscriber != null) {
            mSubscriber = null;
            mSessionCommunicator.dropView();
        }
        fetchSessionConnectionData();
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(LOG_TAG, "Session error: " + opentokError.getMessage());
        mSessionCommunicator.onError();
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        unsubscribe();
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        mSessionCommunicator.onError();
    }

    public void setPresenter(SessionCommunicator presenter) {
        this.mSessionCommunicator = presenter;
    }


    /*When user is in queue, and application closes*/
    public void unsubscribe() {
        if (mSession != null){
            mSession.disconnect();
        }
        RequestQueue request = Volley.newRequestQueue(mContext);
        request.add(new StringRequest(Request.Method.GET,
                appURL + "/unsubscribe/" + mSession.getSessionId(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }));
    }

}
