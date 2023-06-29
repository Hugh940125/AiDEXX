package com.microtech.aidexx.views.webview;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.microtech.aidexx.R;
import com.microtech.aidexx.ui.web.WebActivity;

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
    private WebActivity mContext;
    private TextView mToolbarTitle;
    private boolean isSet = true;
    private OnLoadingUrlListener onLoadingUrlListener;

    public FrameLayout getmFullscreenContainer() {
        return mFullscreenContainer;
    }

    public void setFullscreenContainer(FrameLayout mFullscreenContainer) {
        this.mFullscreenContainer = mFullscreenContainer;
        mWebView.setWebChromeClient(new VideoEnabledWebChromeClient(mFullscreenContainer));

    }

    public FrameLayout mFullscreenContainer;


    public ValueCallback<Uri[]> uploadMessageAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;


    public void setSet(boolean set) {
        isSet = set;
    }

    public void setmToolbarTitle(TextView mToolbarTitle) {
        this.mToolbarTitle = mToolbarTitle;
    }

    public BaseWebView(Context context) {
        super(context);
        init();
    }

    public void setOnLoadingUrlListener(OnLoadingUrlListener onLoadingUrlListener) {
        this.onLoadingUrlListener = onLoadingUrlListener;
    }

    public BaseWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        mContext = (WebActivity) getContext();
        View.inflate(mContext, R.layout.view_web_progress, this);
        mWebView = findViewById(R.id.web_view);
        mProgressBar = findViewById(R.id.progress_bar);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSavePassword(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (mFullscreenContainer != null) {
            mWebView.setWebChromeClient(new VideoEnabledWebChromeClient(mFullscreenContainer));
        } else {
            mWebView.setWebChromeClient(new MyWebChromeClient());
        }

        mWebView.addJavascriptInterface(new MJavascriptInterface(mContext), "imagelistener");
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
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (onReceivedError != null) {
                    onReceivedError.onReceivedError();
                }
            }

        });
    }

    public OnReceivedError onReceivedError;

    public interface OnReceivedError {
        void onReceivedError();
    }

    public void setOnReceivedError(OnReceivedError onReceivedError) {
        this.onReceivedError = onReceivedError;
    }

    private boolean isURl(String url) {
        String regex = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(url).matches();
    }


    private void openImageChooserActivity() {
        //调用自己的图库
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        mContext.startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    private void openFileChooserActivity() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        mContext.startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILE_CHOOSER_RESULT_CODE);
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
        mContext.startActivity(intent);
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

    public void loadDataWithBaseURL(String baseUrl, String data,
                                    String mimeType, String encoding, String historyUrl) {
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

    static class MJavascriptInterface {
        private final Context context;
        private String[] imageUrls, img;

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


    private class MyWebChromeClient extends WebChromeClient {


        @Override
        public void onReceivedTitle(WebView view, String _title) {
            super.onReceivedTitle(view, _title);
            if (mToolbarTitle != null && isSet) {
                mToolbarTitle.setText(_title);
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
            uploadMessageAboveL = filePathCallback;
//            Log.d(TAG, "onShowFileChooser: fileChooserParams " + Arrays.toString(fileChooserParams.getAcceptTypes()));
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

    public class VideoEnabledWebChromeClient extends MyWebChromeClient {
        private View mCustomView;
        private CustomViewCallback mCustomViewCallback;
        private final FrameLayout mFullscreenContainer;

        // 在 Activity 中设置一个 FrameLayout 作为全屏容器
        public VideoEnabledWebChromeClient(FrameLayout fullscreenContainer) {
            mFullscreenContainer = fullscreenContainer;
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (mContext != null) {
                mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mContext.fitWebOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            mWebView.setVisibility(View.GONE);
            // 如果一个视图已经存在，那么立刻终止并新建一个
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            mFullscreenContainer.addView(view);
            mCustomView = view;
            mCustomViewCallback = callback;
            mFullscreenContainer.setVisibility(View.VISIBLE);
        }

        @SuppressLint("SourceLockedOrientationActivity")
        @Override
        public void onHideCustomView() {
            // 不是全屏播放状态
            if (mCustomView == null) {
                return;
            }
            if (mContext != null) {
                mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mContext.fitWebOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            mCustomView.setVisibility(View.GONE);
            mFullscreenContainer.removeView(mCustomView);
            mCustomView = null;
            mFullscreenContainer.setVisibility(View.GONE);
            mCustomViewCallback.onCustomViewHidden();
            mWebView.setVisibility(View.VISIBLE);
        }
    }
}


