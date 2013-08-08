package net.bicou.redmine.net;

import android.content.Context;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.bicou.redmine.R;
import net.bicou.redmine.data.Server;
import net.bicou.redmine.data.json.AbsObjectList;
import net.bicou.redmine.data.json.CalendarDeserializer;
import net.bicou.redmine.data.json.Version.VersionStatus;
import net.bicou.redmine.data.json.VersionStatusDeserializer;
import net.bicou.redmine.net.JsonDownloadError.ErrorType;
import net.bicou.redmine.util.L;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class JsonDownloader<T> extends JsonNetworkManager {
	T mObject;
	Class<T> mType;
	private boolean mDownloadAllIfList = true;
	boolean mStripJsonContainer = false;
	int mCurrentOffset;

	public JsonDownloader(final Class<T> type) {
		mType = type;
	}

	/**
	 * Prevent all objects from being downloaded if the remote JSON contains a list of objects (see {@link AbsObjectList})
	 */
	public JsonDownloader<T> setDownloadAllIfList(final boolean downloadAllIfList) {
		mDownloadAllIfList = downloadAllIfList;
		return this;
	}

	public JsonDownloader<T> setStripJsonContainer(final boolean stripJsonContainer) {
		mStripJsonContainer = stripJsonContainer;
		return this;
	}

	public String stripJsonContainer(final String json) {
		final int start = json.indexOf(":") + 1;
		final int end = json.lastIndexOf("}");
		return json.substring(start, end);
	}

	@Override
	protected List<NameValuePair> buildUriArgs() {
		List<NameValuePair> args = super.buildUriArgs();
		boolean addOffset = true;
		for (final NameValuePair arg : args) {
			if ("offset".equals(arg.getName())) {
				addOffset = false;
			}
		}
		if (addOffset && mCurrentOffset > 0) {
			args.add(new BasicNameValuePair("offset", Integer.toString(mCurrentOffset)));
		}

		return args;
	}

	/**
	 * Downloads a json object and convert it to a java object
	 *
	 * @param ctx    Required to retrieve the SSL certificates from the app keystore or Android's
	 * @param server Target server
	 * @param uri    Target URI
	 */
	public T fetchObject(final Context ctx, final Server server, final String uri) {
		init(ctx, server, uri);
		return downloadAndParse();
	}

	/**
	 * Downloads a json object and convert it to a java object
	 *
	 * @param ctx    Required to retrieve the SSL certificates from the app keystore or Android's
	 * @param server Target server
	 * @param uri    Target URI
	 * @param args   Optional HTTP GET arguments
	 */
	public T fetchObject(final Context ctx, final Server server, final String uri, final NameValuePair[] args) {
		init(ctx, server, uri, args);
		return downloadAndParse();
	}

	/**
	 * Downloads a json object and convert it to a java object
	 *
	 * @param ctx    Required to retrieve the SSL certificates from the app keystore or Android's
	 * @param server Target server
	 * @param uri    Target URI
	 * @param args   Optional HTTP GET arguments
	 */
	public T fetchObject(final Context ctx, final Server server, final String uri, final List<NameValuePair> args) {
		init(ctx, server, uri, args);
		return downloadAndParse();
	}

	@SuppressWarnings("unchecked")
	private T downloadAndParse() {
		if (AbsObjectList.class.isAssignableFrom(mType)) {
			String json;
			try {
				// T object = mType.newInstance();
				int downloadedObjects = 0, limit = 0, total = 0;
				AbsObjectList<T> mObjectAsList = null, object;
				mCurrentOffset = 0;

				do {
					json = downloadJson();
					if (mError != null) {
						return null;
					}
					object = (AbsObjectList<T>) parseJson(json);

					if (object != null) {
						limit = object.limit;
						total = object.total_count;

						mCurrentOffset += limit;

						if (mObject == null) {
							mObject = mType.newInstance();
							mObjectAsList = (AbsObjectList<T>) mObject;
							mObjectAsList.init(object);
						}

						downloadedObjects += object.getSize();
						mObjectAsList.addObjects(object.getObjects());
					} else {
						break;
					}

					if (!mDownloadAllIfList) {
						break;
					}
				} while (downloadedObjects < total);

				if (mObjectAsList != null) {
					mObjectAsList.downloadedObjects = downloadedObjects;
					mObjectAsList.total_count = total;
					mObjectAsList.limit = limit;
					mObjectAsList.offset = 0;
				}
			} catch (final InstantiationException e) {
				L.e("unable to instantiate object", e);
				mError = new JsonDownloadError(ErrorType.TYPE_ANDROID);
				mError.setMessage(R.string.err_unable_to_instantiate_object, e.getMessage());
			} catch (final IllegalAccessException e) {
				L.e("unable to instantiate object", e);
				mError = new JsonDownloadError(ErrorType.TYPE_ANDROID);
				mError.setMessage(R.string.err_unable_to_instantiate_object, e.getMessage());
			}
		} else {
			final String json = downloadJson();
			mObject = parseJson(json);
		}

		return mObject;
	}

	/**
	 * Actually downloads the JSON from the server. In case of failure, an object will be stored in {@link JsonDownloader#mError}.
	 *
	 * @return the JSON, as a {@code String}
	 */
	private String downloadJson() {
		BufferedReader reader = null;
		InputStream inputStream = null;
		final StringBuilder builder = new StringBuilder();

		try {
			buildURI();
			L.i("Loading JSON from: " + mURI);
			if (mURI == null) {
				return null;
			}

			final HttpClient httpClient = getHttpClient();
			final HttpGet get = new HttpGet(mURI);
			final HttpResponse resp = httpClient.execute(get);

			// Ask for UTF-8
			get.setHeader("Content-Type", "application/json; charset=utf-8");
			get.setHeader("Accept-Encoding", "gzip");

			if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				L.d("Got HTTP " + resp.getStatusLine().getStatusCode() + ": " + resp.getStatusLine().getReasonPhrase());
				mError = new JsonDownloadError(ErrorType.TYPE_NETWORK);
				mError.setMessage(R.string.err_http, "HTTP " + resp.getStatusLine().getStatusCode() + ": " + resp.getStatusLine().getReasonPhrase());
				return null;
			}

			// Handle proper incoming charset
			HttpEntity entity = resp.getEntity();
			Header contentEncoding = resp.getFirstHeader("Content-Encoding");
			String charset = contentEncoding == null ? null : contentEncoding.getValue();
			if (TextUtils.isEmpty(charset)) {
				charset = "UTF-8";
			}

			// Handle gzip decompression
			if (charset.equalsIgnoreCase("gzip")) {
				inputStream = new GZIPInputStream(entity.getContent());
			} else {
				inputStream = entity.getContent();
			}

			// Read response
			reader = new BufferedReader(new InputStreamReader(inputStream, charset));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line).append("\n");
			}
		} catch (final Exception e) {
			L.e("Unable to download " + mType.toString() + " from " + mURI + " because:" + e.toString());
			mError = new JsonDownloadError(ErrorType.TYPE_NETWORK, e);
			if (mSslSocketFactory != null) {
				mError.chain = mSSLTrustManager.getServerCertificates();
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return builder.toString();
	}

	private T parseJson(String json) {
		if (TextUtils.isEmpty(json)) {
			L.e("Received JSON for " + mType.toString() + " from " + mURI + " is empty");
			mError = new JsonDownloadError(ErrorType.TYPE_RESPONSE);
			mError.setMessage(R.string.err_empty_response);
			return null;
		}

		// Edit json?
		if (mStripJsonContainer) {
			L.d("The JSON was striped from its main container");
			json = stripJsonContainer(json);
		}

		T object = null;

		try {
			final GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(Calendar.class, new CalendarDeserializer());
			builder.registerTypeAdapter(VersionStatus.class, new VersionStatusDeserializer());
			final Gson gson = builder.create();

			object = gson.fromJson(json, mType);
		} catch (final JsonSyntaxException e) {
			L.e("Unparseable JSON is:");
			L.e(json);
		} catch (final IllegalStateException e) {
			L.e("Unparseable JSON is:");
			L.e(json);
		} catch (final Exception e) {
			L.e("Unparseable JSON is:");
			L.e(json);
			L.e("Unable to parse JSON", e);
		}

		if (object == null) {
			mError = new JsonDownloadError(ErrorType.TYPE_JSON);
			mError.setMessage(R.string.err_parse_error);
		}

		return object;
	}
}
