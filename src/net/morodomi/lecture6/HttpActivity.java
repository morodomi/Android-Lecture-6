package net.morodomi.lecture6;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Activity for Android Lecture 6
 * Use Http Connections
 * @author Masahiro Morodomi <morodomi at gmail.com>
 *
 */
public class HttpActivity extends Activity implements OnClickListener{
	private static final int DIALOG_PROGRESS = 0;
	private static final String TWITTER_URL = "http://search.twitter.com/search.json?q=tokyo";
	private static final int TIMEOUT = 10000;

	private ArrayAdapter<String> adapter;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// set onc click listener
		findViewById(R.id.start).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// execute calculation asynchronously.
		new AsyncHttpConnect().execute(TWITTER_URL);
	}

	/** Called by Android Framework with showDialog(id) method */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		// if id matches DIALOG_PROGRESS, show progress dialog
		case DIALOG_PROGRESS:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setMessage(getString(R.string.loading));
			return progressDialog;
		}
		// if id does not match with any, return null
		return null;
	}

	/**
	 * private class that connect to web server asynchronously.
	 * @author Masahiro Morodomi <morodomi at gmail.com>
	 */
	class AsyncHttpConnect extends AsyncTask<String, Void, HttpResponse> {
		@Override
		protected void onPreExecute() {}

		@Override
		protected HttpResponse doInBackground(String... url) {
			// prepare return object
			HttpResponse response = null;
			try {
				HttpParams params = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
				HttpConnectionParams.setSoTimeout(params, TIMEOUT);
				HttpClient httpClient = new DefaultHttpClient(params);
				httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
				httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
				HttpGet get = new HttpGet(url[0]);
				get.setHeader("Accept", "text/html,application/xml,application/json,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5;");
				get.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
				response = httpClient.execute(get);
			} catch(Exception e) {
				Log.e("Lecture 6", e.getMessage(), e);
			}
			// passing the value to onPostExecute
			return response;
		}
	
		@Override
		protected void onPostExecute(HttpResponse response) {
			// display time difference
			if(response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				String result = null;
				try {
					result = EntityUtils.toString(response.getEntity(), "UTF-8");
				} catch (Exception e) {
					Log.e("Lecture 6", e.getMessage(), e);
				}
				/** parse JSON */
				// create list object
				List<String> list = new ArrayList<String>();
				try {
					// create json object
					JSONObject json = new JSONObject(result);
					// get results JSONArray
					JSONArray results = json.getJSONArray("results");
					for(int i = 0; i < results.length(); i++) {
						// initializing string list
						list.add("@" + results.getJSONObject(i).getString("from_user") + " - " + results.getJSONObject(i).getString("text"));
					}
					// create adapter
					adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listitem, R.id.tweet, list);
					// set adapter to the ListView.
					((ListView) findViewById(R.id.list)).setAdapter(adapter);
				} catch (Exception e) {
					Log.e("Lecture 6", e.getMessage(), e);
				}
			}
		}
	}
}