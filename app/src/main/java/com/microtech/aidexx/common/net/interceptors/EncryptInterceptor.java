package com.microtech.aidexx.common.net.interceptors;

import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.microtech.aidexx.common.net.RSAUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * @Description:
 * @Author: Hugh
 * @CreateDate: 2022/4/29 15:49
 */
public class EncryptInterceptor implements Interceptor {
    private final HashMap<String,String> hashMap = new HashMap<>();

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String method = request.method();
        RequestBody body = request.body();
        if (body != null && !body.toString().isEmpty()) {
            MediaType mediaType = body.contentType();
            boolean multipart = false;
            boolean formData = false;
            if (mediaType != null) {
                multipart = mediaType.type().equalsIgnoreCase("multipart");
                formData = mediaType.subtype().equalsIgnoreCase("form-data");
            }
            Request.Builder builder = request.newBuilder();
            if (!(multipart&&formData)){
                String encryptBody = null;
                hashMap.clear();
                try {
                    encryptBody = RSAUtil.encryptByPublicKey(
                            getContent(body),
                            RSAUtil.getPublicKey(RSAUtil.PUBLIC_KEY)
                    );
                    hashMap.put("data", encryptBody);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                RequestBody requestBody;
                if (!hashMap.isEmpty()) {
                    String mapBody = new Gson().toJson(hashMap);
                    requestBody = RequestBody.create(encryptBody != null ? encryptBody : "", body.contentType());
                    switch (method.toLowerCase(Locale.ROOT)) {
                        case "post":
                            builder.post(requestBody);
                            break;
                        case "put":
                            builder.put(requestBody);
                            break;
                        case "patch":
                            builder.patch(requestBody);
                            break;
                        case "delete":
                            builder.delete(requestBody);
                            break;
                        default:
                            break;
                    }
                    request = builder.build();
                }
            }else {
                builder.removeHeader("encryption");
                builder.addHeader("encryption","disable");
                builder.addHeader("data-decrypt","false");
                request = builder.build();
            }
        }
        return chain.proceed(request);
    }

    private String getContent(RequestBody body) {
        Buffer buffer = new Buffer();
        try {
            body.writeTo(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.readUtf8();
    }
}
