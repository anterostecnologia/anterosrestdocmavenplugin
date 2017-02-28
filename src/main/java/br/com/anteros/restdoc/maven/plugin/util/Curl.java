package br.com.anteros.restdoc.maven.plugin.util;

import java.util.HashMap;
import java.util.Locale;

import br.com.anteros.core.utils.StringUtils;

public class Curl {
	@SuppressWarnings("unused")
	public static void main(String[] args) {

		Curl ac = new Curl("www.google.com");
		ac.header("mail", "mos@aaa.com");
		ac.header("userId", "7DDDgfsdf");
		ac.parameter("mm", "add&");
		ac.parameter("u", 12444);
		ac.parameter("m", 2002);
		ac.basicAuthentication("edson", "teste");

		ac.cookie("cookie1", "value1");
		ac.cookie("cookie2", "val2");
		ac.enableResponseLogging().enableVerbose();
		ac.timeOut(100);
		ac.type(Type.POST);

		System.out.println(ac.toCurl());
	}

	public enum Type {

		GET("GET"), POST("POST"), DELETE("DELETE"), PUT("PUT");

		public final String name;

		private Type(String name) {
			this.name = name;
		}
	}

	private final String mUrl;
	private final HashMap<String, String> mHeaders = new HashMap<String, String>();
	private final HashMap<String, Object> mParameters = new HashMap<String, Object>();
	private final HashMap<String, Object> mCookies = new HashMap<String, Object>();

	private static final String FORMATTED_HEADER = "-H \"%s: %s\" ";
	private static final String FORMATTED_URL = "\"%s\" ";
	private static final String FORMATTED_PARM = "%s=%s&";

	private boolean responseLogging = false;
	private boolean verbose = false;
	private String basicAuthentication = null;

	private Type mType = Type.GET;

	private long mTimeOut = 30000l;
	private String endPoint;
	private String body;

	public static Curl of(String mUrl) {
		return new Curl(mUrl);
	}

	private Curl(String mUrl) {
		super();
		this.mUrl = mUrl;
	}

	public Curl type(Type t) {
		mType = t;
		return this;
	}

	public Curl header(String k, String v) {
		mHeaders.put(k, v);
		return this;
	}

	public Curl headers(HashMap<String, String> hs) {
		if (hs != null && hs.size() > 0) {
			mHeaders.putAll(hs);
		}
		return this;
	}
	
	public Curl body(String body){
		this.body = body;
		return this;
	}

	public Curl parameter(String k, Object v) {
		mParameters.put(k, v);
		return this;
	}

	public Curl parameters(HashMap<String, Object> ps) {
		if (ps != null && ps.size() > 0) {
			mParameters.putAll(ps);
		}
		return this;
	}

	public Curl cookie(String k, Object v) {
		mCookies.put(k, v);
		return this;
	}

	public Curl cookies(HashMap<String, Object> cs) {
		if (cs != null && cs.size() > 0) {
			mCookies.putAll(cs);
		}
		return this;
	}

	public Curl timeOut(long mTimeOut) {
		this.mTimeOut = mTimeOut;
		return this;
	}

	public Curl enableResponseLogging() {
		responseLogging = true;
		return this;
	}

	public Curl enableVerbose() {
		verbose = true;
		return this;
	}

	public Curl basicAuthentication(String username, String password) {
		basicAuthentication = username + ":" + password;
		return this;
	}

	public Curl basicAuthentication(String auth) {
		basicAuthentication = auth;
		return this;
	}

	public String toCurl() {
		StringBuilder builder = new StringBuilder();
		builder.append("curl ");
		addTimeOut(builder);
		addHeaders(builder);
		addCookies(builder);
		addExtraParms(builder);
		addType(builder);
		addUrlAndParameters(builder);
		addBody(builder);
		return builder.toString();
	}
	
	
	public String toUrl() {
		StringBuilder builder = new StringBuilder();
		addUrlAndParameters(builder);
		return builder.toString();
	}

	// ##############################################################
	// INTERNAL METHODS
	// ##############################################################

	private void addBody(StringBuilder builder) {
		if (StringUtils.isNotEmpty(body)){
		    builder.append("-d \"");
		    builder.append(body);
		    builder.append("\" ");
		}		
	}

	private void addHeaders(StringBuilder builder) {
		if (!mHeaders.isEmpty()) {
			for (String key : mHeaders.keySet()) {
				builder.append(String.format(Locale.US, FORMATTED_HEADER, key, mHeaders.get(key)));
			}
		}
	}

	
	private void addCookies(StringBuilder builder) {
		if (mCookies.size() > 0) {
			builder.append("-c \"");
			for (String key : mCookies.keySet()) {
				builder.append(String.format(Locale.US, FORMATTED_PARM, key, mCookies.get(key).toString()));
			}
			builder.deleteCharAt(builder.length() - 1).append("\" ");
		}

	}

	private void addExtraParms(StringBuilder builder) {
		if (responseLogging) {
			builder.append("-i ");
		}
		if (verbose) {
			builder.append("-v ");
		}
		if (basicAuthentication != null) {
			builder.append("-u ").append(basicAuthentication).append(" ");
		}
	}

	private void addType(StringBuilder builder) {
		builder.append("-X ").append(mType.name).append(' ');

	}

	private void addUrlAndParameters(StringBuilder builder) {
		StringBuilder tmp = new StringBuilder();
		tmp.append(mUrl).append(endPoint==null?"":endPoint);
		
		if (!mParameters.isEmpty()) {
			tmp.append("?");
			for (String key : mParameters.keySet()) {
				tmp.append(String.format(Locale.US, FORMATTED_PARM, key, mParameters.get(key).toString()));
			}
			tmp.deleteCharAt(tmp.length() - 1);
		}
		
		builder.append(String.format(Locale.US, FORMATTED_URL, tmp.toString()));
	}

	private void addTimeOut(StringBuilder builder) {
		if (mTimeOut != 30000l) {
			builder.append("--max-time ").append(mTimeOut).append(" ");
		}
	}

	public Curl endPoint(String endPoint) {
		this.endPoint = endPoint;
		return this;
	}
}