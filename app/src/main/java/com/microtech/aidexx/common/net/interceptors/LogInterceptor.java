package com.microtech.aidexx.common.net.interceptors;

import androidx.annotation.NonNull;

import com.microtech.aidexx.utils.LogUtil;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class LogInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long startTime = System.currentTimeMillis();
        Response response = chain.proceed(chain.request());
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        MediaType responseMediaType = null;
        String content = "Unknown response type";
        if (response.body() != null) {
            responseMediaType = response.body().contentType();
            if (canText(responseMediaType)) {
                content = response.body().string();
            } else {
                if (responseMediaType != null) {
                    content = responseMediaType.toString();
                }
            }
        }
        LogUtil.eAiDEX("\n");
        LogUtil.eAiDEX("---------- Start ----------");
        LogUtil.eAiDEX("| " + request);
        String method = request.method();
        if (method.equalsIgnoreCase("POST")) {
            RequestBody requestBody = request.body();
            if (requestBody != null) {
                MediaType requestMediaType = requestBody.contentType();
                if (canText(requestMediaType)) {
                    LogUtil.eAiDEX("| " + "Request Params:" + bodyToString(request));
                }
            }
        }
        LogUtil.eAiDEX("| Response:" + content);
        LogUtil.eAiDEX("---------- End:" + duration + "millis ----------");

        if (canText(responseMediaType)) {
            response.close();
            return response.newBuilder()
                    .body(okhttp3.ResponseBody.create(responseMediaType, content))
                    .build();
        } else {
            return response;
        }
    }

    private String bodyToString(final Request request) {
        final Request copy = request.newBuilder().build();
        final Buffer buffer = new Buffer();
        try {
            if (copy.body() != null) {
                copy.body().writeTo(buffer);
            }
        } catch (IOException e) {
            return "something error,when show requestBody";
        }
        return buffer.readUtf8();
    }

    private boolean canText(MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }
        if (mediaType.type().contains("text")) {
            return true;
        }
        return mediaType.subtype().contains("json") ||
                mediaType.subtype().contains("xml") ||
                mediaType.subtype().contains("html") ||
                mediaType.subtype().contains("webviewhtml");
    }

    /**
     * 读取参数
     *
     * @param requestBody
     * @return
     */
    private String getParam(RequestBody requestBody) {
        Buffer buffer = new Buffer();
        String logparm;
        try {
            requestBody.writeTo(buffer);
            logparm = buffer.readUtf8();
            logparm = URLDecoder.decode(logparm, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return logparm;
    }


    /**
     * unicode 转换成 utf-8
     *
     * @param theString
     * @return
     * @author fanhui
     * 2007-3-15
     */
    public static String unicodeToUtf8(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }

}
