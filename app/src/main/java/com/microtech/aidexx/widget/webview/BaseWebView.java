package com.microtech.aidexx.widget.webview;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.microtech.aidexx.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;


public class BaseWebView extends LinearLayout {
    private static final String TAG = "BaseWebView";

    public WebView mWebView;
    ProgressBar mProgressBar;
    Timer timer; // 定时器
    int progress = 0;
    int timeProgress = 0;
    public OnReceivedError onReceivedError;

    private OnLoadingUrlListener onLoadingUrlListener;

    public ValueCallback<Uri[]> valueCallback;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;
    private TextView titleView;
    private Activity mActivity;

    public void setTitleView(TextView titleView) {
        this.titleView = titleView;
    }

    public BaseWebView(Context context) {
        this(context, null);
    }

    public void setOnLoadingUrlListener(OnLoadingUrlListener onLoadingUrlListener) {
        this.onLoadingUrlListener = onLoadingUrlListener;
    }

    public BaseWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init(Context context) {
        mActivity = (Activity)context;
        View.inflate(context, R.layout.view_web_progress, this);
        mWebView = findViewById(R.id.web_view);
        mProgressBar = findViewById(R.id.progress_bar);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(false);
        settings.setSavePassword(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.addJavascriptInterface(new MJavascriptInterface(context), "imagelistener");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                if (onPageListener != null) {
                    onPageListener.onPageStarted(view, url);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                if (onLoadingUrlListener != null) {
                    return onLoadingUrlListener.onLoadingUrl(url);
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (onPageListener != null) {
                    onPageListener.onPageFinished(view, url);
                }
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (onReceivedError != null) {
                    onReceivedError.onReceivedError();
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                if (onReceivedError != null) {
                    onReceivedError.onReceivedError();
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                if (onReceivedError != null) {
                    onReceivedError.onReceivedError();
                }
            }
        });
    }

    public interface OnReceivedError {
        void onReceivedError();
    }

    public void setOnReceivedError(OnReceivedError onReceivedError) {
        this.onReceivedError = onReceivedError;
    }

    private boolean isUrl(String url) {
        String regex = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(url).matches();
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onReceivedTitle(WebView view, String _title) {
            super.onReceivedTitle(view, _title);
            if (titleView != null) {
                titleView.setText(_title);
            }
        }


        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progress = newProgress;
            setProgressBar();
            if (newProgress == 100) {
                mProgressBar.setVisibility(View.INVISIBLE);
                AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
                animation.setDuration(500);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        mProgressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mProgressBar.startAnimation(animation);
            } else {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            valueCallback = filePathCallback;
            if (fileChooserParams != null && fileChooserParams.getAcceptTypes().length > 0) {
                if (1 == fileChooserParams.getAcceptTypes().length && "image/*".equals(fileChooserParams.getAcceptTypes()[0])) {
                    openImageChooserActivity();
                } else {
                    openFileChooserActivity();
                }
            }
            return true;
        }
    }

    private void openImageChooserActivity() {
        //调用自己的图库
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        mActivity.startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    private void openFileChooserActivity() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        mActivity.startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILE_CHOOSER_RESULT_CODE);
    }


    // 停止
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    // 开始
    public void loading() {
        mProgressBar.setVisibility(View.VISIBLE);
        timeProgress = 0;
        stopTimer();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                BaseWebView.this.post(new Runnable() {
                    public void run() {

                        if (timeProgress >= 80) {
                            stopTimer();
                            return;
                        }

                        timeProgress += 5;
                        setProgressBar();
                    }
                });
            }
        }, 0, 50);
    }

    private void setProgressBar() {
        int _p = timeProgress;
        if (progress >= timeProgress) {
            _p = progress;
            stopTimer();
        }
        mProgressBar.setProgress(_p);
    }


    private void onTel(String str) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse(str));
    }

    public void loadData(String url, String type, String detail) {

        final Map<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put("os", "android");
//        extraHeaders.put("version", DeviceHelper.installVersion());
//        extraHeaders.put("User-Agent", "youxiake/android/" + DeviceHelper.installVersion() + "/" + DeviceHelper.deviceName());
//        String userToken = UserOp.getInstance().getUserToken();
//        extraHeaders.put("Authorization", "Bearer " + userToken);
        mWebView.loadData(url, type, detail);

    }


    public void loadUrl(String url) {

        final Map<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put("os", "android");
//        extraHeaders.put("version", DeviceHelper.installVersion());
//        extraHeaders.put("User-Agent", "youxiake/android/" + DeviceHelper.installVersion() + "/" + DeviceHelper.deviceName());
//        String userToken = UserOp.getInstance().getUserToken();
//        extraHeaders.put("Authorization", "Bearer " + userToken);
        mWebView.loadUrl(url, extraHeaders);
    }

    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        mWebView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    OnPageListener onPageListener;


    public void setOnPageListener(OnPageListener onPageListener) {
        this.onPageListener = onPageListener;
    }

    public interface OnPageListener {
        void onPageStarted(WebView view, String url);

        void onPageFinished(WebView view, String url);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopTimer();
    }

    class MJavascriptInterface {
        private final Context context;
        private String[] imageUrls;

        public MJavascriptInterface(Context context) {
            this.context = context;
        }

        @android.webkit.JavascriptInterface
        public void openImage(String img) {

        }

        @android.webkit.JavascriptInterface
        public void getArray(String[] imageUrls) {
            this.imageUrls = imageUrls;
        }
    }
}
