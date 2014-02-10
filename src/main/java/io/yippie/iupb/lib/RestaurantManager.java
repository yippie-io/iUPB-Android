package io.yippie.iupb.lib;

import android.content.Context;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RestaurantManager {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new RestaurantManager().getAllRestaurants();
	}

	private static final String BASE_URL = "http://beta.i-upb.de/api/v1/";
	private static final String RESTAURANTS_PATH = "restaurants";
	private static final String MENUS_PATH = "menus/";
	private static final String TAG = "RestaurantClass";

	private Context context;
	public Restaurant[] restaurants;

	public RestaurantManager(Context context) {
		this.context = context;
	}

	public RestaurantManager() {
		// TODO Auto-generated constructor stub
	}

	public Restaurant[] getAllRestaurants() {
		if (this.restaurants == null) {
			Restaurant[] restaurants = null;
			try {
				JSONArray jsonArray = readJSON(BASE_URL + RESTAURANTS_PATH);
				restaurants = new Restaurant[jsonArray.length()];
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					Restaurant currentRestaurant = new Restaurant();
					currentRestaurant.name = jsonObject.getString("name");
					restaurants[i] = currentRestaurant;
				}
			} catch (JSONException e) {
				Log.e(Restaurant.class.toString(), "JSONException: " + e.getMessage());
			} catch (ClientProtocolException e) {
				Log.e(Restaurant.class.toString(), "ClientProtocolException: " + e.getMessage());
			} catch (IOException e) {
				Log.e(Restaurant.class.toString(), "IOException: " + e.getMessage());
			}
			this.restaurants = restaurants;
		}
		return this.restaurants;
	}

	private static JSONArray readJSON(String url) throws JSONException, ClientProtocolException, IOException {
		return new JSONArray(readURL(url));
	}

	private static String readURL(String url) throws ClientProtocolException, IOException {
		Log.v(RestaurantManager.class.toString(), "Loading URL: " + url);
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response = client.execute(httpGet);
		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode == 200) {
			HttpEntity entity = response.getEntity();
			InputStream content = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					content));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} else {
			Log.e(Restaurant.class.toString(), "Failed to download file");
		}
		return builder.toString();
	}

}
