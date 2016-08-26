package com.alibaba.dubbo.remoting.httpinvoker;

import org.apache.commons.httpclient.params.HttpClientParams;
import org.springframework.remoting.httpinvoker.AbstractHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.support.RemoteInvocationResult;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by 齐龙 on 2016-03-26-0026.
 */
public class CommonsHttpInvokerRequestExecutor extends AbstractHttpInvokerRequestExecutor {
    private HttpClient httpClient;
    /**
     * Create a new CommonsHttpInvokerRequestExecutor with a default
     * HttpClient that uses a default MultiThreadedHttpConnectionManager.
     * @see org.apache.commons.httpclient.HttpClient
     * @see org.apache.commons.httpclient.MultiThreadedHttpConnectionManager
     */
    public CommonsHttpInvokerRequestExecutor() {
        this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
    }

    /**
     * 超时时间
     * @param second
     */
    public void setReadTimeout(int second){
        HttpClientParams clientParams = httpClient.getParams();
        clientParams.setParameter("http.connection.timeout", second);
    }

    /**
     * Create a new CommonsHttpInvokerRequestExecutor with the given
     * HttpClient instance.
     * @param httpClient the HttpClient instance to use for this request executor
     */
    public CommonsHttpInvokerRequestExecutor(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Set the HttpClient instance to use for this request executor.
     */
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Return the HttpClient instance that this request executor uses.
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }
    @Override
    protected RemoteInvocationResult doExecuteRequest(
            HttpInvokerClientConfiguration config, ByteArrayOutputStream baos)
            throws IOException, ClassNotFoundException {

        PostMethod postMethod = createPostMethod(config);
        try {
            postMethod.setRequestBody(new ByteArrayInputStream(baos.toByteArray()));
            executePostMethod(config, this.httpClient, postMethod);
            return readRemoteInvocationResult(postMethod.getResponseBodyAsStream(), config.getCodebaseUrl());
        }
        finally {
            // need to explicitly release because it might be pooled
            postMethod.releaseConnection();
        }
    }
    /**
     * Create a PostMethod for the given configuration.
     * @param config the HTTP invoker configuration that specifies the
     * target service
     * @return the PostMethod instance
     * @throws IOException if thrown by I/O methods
     */
    protected PostMethod createPostMethod(HttpInvokerClientConfiguration config) throws IOException {
        PostMethod postMethod = new PostMethod(config.getServiceUrl());
        postMethod.setRequestHeader(HTTP_HEADER_CONTENT_TYPE, CONTENT_TYPE_SERIALIZED_OBJECT);
        return postMethod;
    }

    /**
     * Execute the given PostMethod instance.
     * @param config the HTTP invoker configuration that specifies the
     * target service
     * @param httpClient the HttpClient to execute on
     * @param postMethod the PostMethod to execute
     * @throws IOException if thrown by I/O methods
     */
    protected void executePostMethod(
            HttpInvokerClientConfiguration config, HttpClient httpClient, PostMethod postMethod)
            throws IOException {
        this.httpClient.executeMethod(postMethod);
    }
}
