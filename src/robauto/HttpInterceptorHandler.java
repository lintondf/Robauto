package robauto;

import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import com.google.maps.OkHttpRequestHandler;
import com.google.gson.FieldNamingPolicy;
import com.google.maps.GeoApiContext.RequestHandler;
import com.google.maps.PendingResult;
import com.google.maps.internal.ApiResponse;
import com.google.maps.internal.ExceptionsAllowedToRetry;

public class HttpInterceptorHandler implements RequestHandler {
	
	private OkHttpRequestHandler proxy = new OkHttpRequestHandler();
	
	private String requestUrl;

	@Override
	public <T, R extends ApiResponse<T>> PendingResult<T> handle(String hostName, String url, String userAgent,
            Class<R> clazz, FieldNamingPolicy fieldNamingPolicy,
            long errorTimeout, Integer maxRetries,
            ExceptionsAllowedToRetry exceptionsAllowedToRetry) {
		
		requestUrl = url;
		return proxy.handle(hostName, url, userAgent, clazz, fieldNamingPolicy, errorTimeout, maxRetries, exceptionsAllowedToRetry);
	}

	@Override
	public <T, R extends ApiResponse<T>> PendingResult<T> handlePost(String hostName, String url, String payload,
            String userAgent, Class<R> clazz,
            FieldNamingPolicy fieldNamingPolicy,
            long errorTimeout, Integer maxRetries,
            ExceptionsAllowedToRetry exceptionsAllowedToRetry) {
		
		requestUrl = url;
		return proxy.handlePost(hostName, url, payload, userAgent, clazz, fieldNamingPolicy, errorTimeout, maxRetries, exceptionsAllowedToRetry);
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	@Override
	public void setConnectTimeout(long timeout, TimeUnit unit) {
		proxy.setConnectTimeout(timeout, unit);
	}

	@Override
	public void setProxy(Proxy proxy) {
		this.proxy.setProxy(proxy);
	}

	@Override
	public void setQueriesPerSecond(int arg0) {
		proxy.setQueriesPerSecond(arg0);
	}

	@Override
	public void setQueriesPerSecond(int maxQps, int minimumInterval) {
		proxy.setQueriesPerSecond(maxQps, minimumInterval);
	}

	@Override
	public void setReadTimeout(long timeout, TimeUnit unit) {
		proxy.setReadTimeout(timeout, unit);
	}

	@Override
	public void setWriteTimeout(long timeout, TimeUnit unit) {
		proxy.setWriteTimeout(timeout, unit);
	}

}
