package com.letv.walletbiz.mobile.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.letv.shared.widget.LeBottomSheet;
import com.letv.tracker2.enums.EventType;
import com.letv.tracker2.enums.Key;
import com.letv.wallet.common.http.beans.BaseResponse;
import com.letv.wallet.common.util.AccountHelper;
import com.letv.wallet.common.util.CommonConstants;
import com.letv.wallet.common.util.DensityUtils;
import com.letv.wallet.common.util.DeviceUtils;
import com.letv.wallet.common.util.ExecutorHelper;
import com.letv.wallet.common.util.LogHelper;
import com.letv.wallet.common.util.ParseHelper;
import com.letv.wallet.common.util.PermissionCheckHelper;
import com.letv.wallet.common.util.PhoneNumberUtils;
import com.letv.wallet.common.widget.PhoneEditText;
import com.letv.walletbiz.R;
import com.letv.walletbiz.base.activity.ActivityConstant;
import com.letv.walletbiz.base.activity.ActivityConstant.MOBILE_PARAM;
import com.letv.walletbiz.base.activity.BaseWalletFragmentActivity;
import com.letv.walletbiz.base.http.client.BaseRequestParams;
import com.letv.walletbiz.base.util.Action;
import com.letv.walletbiz.base.util.WalletConstant;
import com.letv.walletbiz.coupon.CouponConstant;
import com.letv.walletbiz.main.BannerTask;
import com.letv.walletbiz.main.MainPanelHelper;
import com.letv.walletbiz.main.bean.WalletBannerListBean;
import com.letv.walletbiz.mobile.MobileConstant;
import com.letv.walletbiz.mobile.MobileConstant.PRODUCT_TYPE;
import com.letv.walletbiz.mobile.beans.DocPromptBean;
import com.letv.walletbiz.mobile.beans.HistoryRecordNumberBean;
import com.letv.walletbiz.mobile.beans.ProductBean;
import com.letv.walletbiz.mobile.pay.MobileProduct;
import com.letv.walletbiz.mobile.ui.BannerV;
import com.letv.walletbiz.mobile.ui.HistoryRecordNumberAdapter;
import com.letv.walletbiz.mobile.ui.HistoryRecordNumberV;
import com.letv.walletbiz.mobile.ui.ProductsPanel;
import com.letv.walletbiz.mobile.ui.ProductsPanelAdapter;
import com.letv.walletbiz.mobile.util.UiUtils;

import org.xutils.common.task.PriorityExecutor;
import org.xutils.xmain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.letv.walletbiz.mobile.MobileConstant.PRODUCT_TYPE.MOBILE_FEE;
import static com.letv.walletbiz.mobile.MobileConstant.PRODUCT_TYPE.MOBILE_FLOW;

/**
 * Created by linquan on 15-11-9.
 */
public class MobileActivity extends BaseWalletFragmentActivity implements
        PhoneEditText.ActionCallback, HistoryRecordNumberAdapter.EnventCallback {
    private static final String TAG = "MobileActivity";

    private static String INPUTNUMBER = "InputNumber";
    private static final int UPDATE_PRODUCT_LIST = 101;
    private static final int GET_UTOKEN_RET = 102;
    private static final int CLEAR_HISTORY_RET = 103;
    private static final int UPDATE_CONTACTNAME = 104;
    private static final int UPDATE_DOC_PROMPT = 105;
    private static final int MSG_LOAD_PRODUCT_LIST_FINISH = 106;

    private static final int CHECK_STATUS = 107;

    private static final int PICK_CONTACT = 200;
    private static final int CLEAR_FAILED_STATE = -1;

    private static final int MOBILE_LEN = 11;
    private static final int CHECK_MOBILE_INDEX = 10;

    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static final int PERMISSIONS_REQUEST_CODE2 = 2;

    private String mDividerPhoneNumberStr;
    private String mPromptInputRightNumber;
    private String mPromptNetConnectionFail;
    private String mPhoneNumDefDescFormatStr;

    private String mDividerNumber;
    private String mSimCard0;
    private String mSimCard1;
    private int mContactType;

    private LinearLayout mFlowEntrancell;
    private ProductsPanel mProductPanel;
    private ProductsPanel mFlowEntranceProductPanel;
    private PhoneEditText mPhoneEdittext;
    private TextView mViewMobileName;
    private TextView mViewMobileDesc;
    private View mLine1;
    private TextView mViewDeposite;
    private ProductBean mStubProducts;
    private DocPromptBean mDocPromptBean;
    private static long mCouponID;

    private LinearLayout mBannerLl;
    private BannerV mBannerV;
    private int mFeeOrFlow;
    private LeBottomSheet mUserSelectDialog;
    private boolean mIsShowDialog;
    private boolean mGotoOrderList;
    private boolean mHaveProductData = false;
    private boolean isChangedNumber = false;
    private boolean isLoadRecordNumber = false;
    private boolean isRequestingPermissin = false;
    private String mFrom;

    private RelativeLayout mRecordHistoryNumberRl;
    private HistoryRecordNumberV mRecordHistoryNumberV;

    private WalletBannerListBean mBannerListData;


    private ProductsTask mProductsTask;
    private DocPromptTask mDocPromptAsyncT;
    private ContactNameTask mContactTask;
    private BannerTask mBannerAsyncT;

    private Handler handler = new Handler() {
        // 在Handler中获取消息，重写handleMessage()方法
        @Override
        public void handleMessage(Message msg) {
            if (isFinishing()) return;
            // 判断消息码是否为1
            switch (msg.what) {
                case CHECK_STATUS:
                    checkAllStatus();
                    break;
                case UPDATE_DOC_PROMPT:
                    mDocPromptAsyncT = null;
                    if (msg.obj != null) {
                        mDocPromptBean = (DocPromptBean) msg.obj;
                        if (!TextUtils.isEmpty(mDocPromptBean.getDoc_content())) {
                            mViewDeposite.setText(mDocPromptBean.getDoc_content());
                            return;
                        }
                    }
                    mViewDeposite.setText(getDeposite(mFeeOrFlow));
                    break;
                case MSG_LOAD_PRODUCT_LIST_FINISH:
                    BaseResponse<ProductBean> response = null;
                    if (msg.obj != null) {
                        response = (BaseResponse<ProductBean>) msg.obj;
                    }
                    if (response != null) {
                        ProductBean product = (ProductBean) response.data;
                        updateProductList(product);
                    } else {
                        showUserSelectDialog(MobileActivity.this);
                        updateProductList(null);
                    }
                    break;
                case UPDATE_PRODUCT_LIST:
                    ProductBean result;
                    String desc = null;
                    if (msg.obj == null) {
                        result = mStubProducts;
                        mHaveProductData = false;
                    } else {
                        mHaveProductData = true;
                        result = (ProductBean) msg.obj;
                        desc = result.getNumberDesc();
                    }
                    updateNumberDesc(desc);
                    mProductPanel.setData(result);
                    mProductPanel.update();
                    break;
                case UPDATE_CONTACTNAME:
                    mContactTask = null;
                    String name = "";
                    if (msg.obj != null) {
                        name = msg.obj.toString();
                    }
                    if (TextUtils.isEmpty(name)) {
                        name = getString(R.string.mobile_phone_number_unknown);
                    }
                    setMobileName(name);
                    break;
                case CLEAR_HISTORY_RET:
                    if (msg.arg1 == CLEAR_FAILED_STATE) {
                        Toast.makeText(MobileActivity.this, R.string.mobile_clear_history_failed, Toast.LENGTH_LONG).show();
                    } else {
                        if (mRecordHistoryNumberV != null) {
                            mRecordHistoryNumberV.clearData();
                        }
                        hideHistoryNumberV();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void checkAllStatus() {
        AccountHelper accountHelper = AccountHelper.getInstance();
        boolean isLogin = accountHelper.isLogin(this);
        if (isLogin) {
            if (TextUtils.isEmpty(accountHelper.getToken(this))) {
                accountHelper.getTokenASync(this);
            }
        }
        if (mGotoOrderList) {
            if (isLogin) {
                goToOrderList();
            }
            mGotoOrderList = false;
        }
        getPhoneInfo();
        if (TextUtils.isEmpty(mDividerNumber)) {
            if (mPhoneEdittext != null && !TextUtils.isEmpty(mPhoneEdittext.getMobileNumber())) {
                // 检测编辑的号码为手机号获取联系人姓名
                String number = PhoneNumberUtils.checkPhoneNumber(mPhoneEdittext.getMobileNumber(), false);
                if (!TextUtils.isEmpty(number)) {
                    getContactNameAsyncTask(mPhoneEdittext.getMobileNumber());
                }
            }
        }
        if (!isLoadRecordNumber && !isChangedNumber) {
            setMobileNumberVData(mDividerNumber);
        }
        checkPermisss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerNetWorkReceiver();
        String inNumber = processExtraData();
        LogHelper.d("[%S] getStringExtra ucouponid = " + Long.toString(mCouponID), TAG);
        setTitle(getTitle(mFeeOrFlow));
        setContentView(R.layout.mobile_activity_main);
        initViewContent(savedInstanceState, inNumber);
        getBannerDataAsyncTask();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String inNumber = processExtraData();
        LogHelper.d("[%S] getStringExtra ucouponid = " + Long.toString(mCouponID), TAG);
        setTitle(getTitle(mFeeOrFlow));
        initViewContent(inNumber);
        getBannerDataAsyncTask();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null && mPhoneEdittext != null) {
            String number = mPhoneEdittext.getTextContent();
            outState.putString(INPUTNUMBER, number);
        }
    }

    private String processExtraData() {
        String inNumber = null;
        Intent in = getIntent();
        Uri uri = in.getData();
        if (uri == null) {
            String action = in.getAction();
            inNumber = in.getStringExtra(MOBILE_PARAM.MOBILENUMBER);
            mFrom = in.getStringExtra(WalletConstant.EXTRA_FROM);
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                mCouponID = bundle.getLong(CouponConstant.EXTRA_COUPON_BEAN_ID);
            }
            setChargeType(action);
        } else {
            String host = uri.getHost();
            if (MOBILE_PARAM.FEE.equals(host)) {
                mFeeOrFlow = MOBILE_FEE;
            } else {
                mFeeOrFlow = MOBILE_FLOW;
            }
            inNumber = uri.getQueryParameter(MOBILE_PARAM.MOBILENUMBER);
            mFrom = uri.getQueryParameter(WalletConstant.EXTRA_FROM);
            if (TextUtils.isEmpty(mFrom)) {
                mFrom = in.getStringExtra(WalletConstant.EXTRA_FROM);
            }
            try {
                mCouponID = Integer.valueOf(uri.getQueryParameter(CouponConstant.EXTRA_COUPON_BEAN_ID));
            } catch (Exception e) {
            }
        }
        if (mFeeOrFlow == MOBILE_FEE) {
            Action.uploadFeeExpose(mFrom);
        } else if (mFeeOrFlow == MOBILE_FLOW) {
            Action.uploadFlowExpose(mFrom);
        }
        LogHelper.d("[%S] getStringExtra number = " + inNumber, TAG);
        return inNumber;
    }

    @Override
    public boolean hasBlankAndLoadingView() {
        return false;
    }

    private int setChargeType(String type) {
        if (type.equals(MOBILE_PARAM.BASE_ACTION + MOBILE_PARAM.FEE)) {
            mFeeOrFlow = PRODUCT_TYPE.MOBILE_FEE;
        } else {
            mFeeOrFlow = PRODUCT_TYPE.MOBILE_FLOW;
        }
        return mFeeOrFlow;
    }

    private int getTitle(int type) {
        int id = type == PRODUCT_TYPE.MOBILE_FEE ?
                R.string.label_entity_mobile : R.string.label_entity_flow;
        return id;
    }

    private int getBusinessId(int type) {
        int businessId = (type == PRODUCT_TYPE.MOBILE_FEE) ?
                ActivityConstant.BUSINESS_ID.MOBILE_FEE_ID : ActivityConstant.BUSINESS_ID.MOBILE_FLOW_ID;
        return businessId;
    }

    private String getDeposite(int type) {
        int id = (type == MOBILE_FEE ?
                R.string.label_mobile_deposite : R.string.label_mobile_flow_deposite);
        String content = getString(id);
        return content;
    }

    private void initViewContent(String number) {
        initViewContent(null, number);
    }

    private void initViewContent(Bundle savedInstanceState, String number) {
        mPromptNetConnectionFail = getString(R.string.mobile_prompt_net_connection_fail);
        mPromptInputRightNumber = getString(com.letv.wallet.common.R.string.phonenumber_prompt_input_right_number);
        mDividerPhoneNumberStr = getString(R.string.mobile_divider_phone_number_show);
        mPhoneNumDefDescFormatStr = getString(R.string.label_mobile_def_desc);

        mBannerLl = (LinearLayout) findViewById(R.id.banner_ll);
        mBannerV = (BannerV) findViewById(R.id.mobile_banner_id);
        mProductPanel = (ProductsPanel) findViewById(R.id.rv_product_panel);
        mFlowEntrancell = (LinearLayout) findViewById(R.id.flow_entrance_ll);
        mFlowEntranceProductPanel = (ProductsPanel) findViewById(R.id.flow_entrance_product_panel);
        mRecordHistoryNumberRl = (RelativeLayout) findViewById(R.id.record_history_number_rl);
        mRecordHistoryNumberV = (HistoryRecordNumberV) findViewById(R.id.record_history_number);
        mRecordHistoryNumberV.setHistoryNumberClickListener(this);
        mPhoneEdittext = (PhoneEditText) findViewById(R.id.etv_mobile_number);
        mPhoneEdittext.setTextSize(DensityUtils.px2dip(24.0F));
        mPhoneEdittext.setTextColor(getColor(R.color.mobileSecondaryTvColor));
        mPhoneEdittext.setTextHintcolor(getColor(R.color.mobilePrimaryTvColor));
        mPhoneEdittext.setHint(R.string.movie_hint_input_phonenumber);
        mPhoneEdittext.setVerificationLevel(PhoneEditText.PHONENUMBER_RIGOROUS_VERIFICATION);
        mPhoneEdittext.setCallback(this);
        mViewDeposite = (TextView) findViewById(R.id.label_deposite);
        mViewDeposite.setText(getDeposite(mFeeOrFlow));
        mViewMobileDesc = (TextView) findViewById(R.id.tv_mobile_desc);
        mLine1 = findViewById(R.id.line1);
        mViewMobileName = (TextView) findViewById(R.id.tv_contact_name);


        mStubProducts = (ProductBean) ParseHelper.parseByGson(UiUtils.getProductJsonStub(mFeeOrFlow),
                ProductBean.class);
        mStubProducts.setAsStub();
        mProductPanel.setData(mStubProducts);

        ProductsPanelAdapter adapter = new ProductsPanelAdapter(mStubProducts);
        mProductPanel.setAdapter(adapter);
        mProductPanel.setType(mFeeOrFlow);
        adapter.setOnItemClickListener(new ProductsPanelAdapter.OnMobileProductItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (!isNetworkAvailable()) {
                    showNetFailToast();
                    return;
                }
                String phoneNumber = mPhoneEdittext.getMobileNumber();
                if (phoneNumber.length() == MOBILE_LEN) {
                    Map<String, Object> props = new HashMap<String, Object>();
                    props.put(Key.From.getKeyId(), mFrom);
                    if (mFeeOrFlow == PRODUCT_TYPE.MOBILE_FEE) {
                        Action.uploadCustom(EventType.Click, Action.MOBILE_FEE_PRODUCT_CLICK, props);
                    } else {
                        Action.uploadCustom(EventType.Click, Action.MOBILE_FLOW_PRODUCT_CLICK, props);
                    }
                    int id = position;
                    ProductBean.product productBean = mProductPanel.getProductItem(id);
                    if (AccountHelper.getInstance().loginLetvAccountIfNot(MobileActivity.this, null) == true) {
                        MobileProduct product = new MobileProduct(
                                productBean.getProductId(), productBean.getSkuSN(), productBean.getProductName(),
                                mPhoneEdittext.getMobileNumber(), productBean.getProductPrice());
                        product.showOrderSure(MobileActivity.this, mFrom, mFeeOrFlow, mCouponID, mContactType);
                    }
                }
            }

        });
        if (mFeeOrFlow == PRODUCT_TYPE.MOBILE_FEE) {
            mFlowEntrancell.setVisibility(View.VISIBLE);
            ProductsPanelAdapter flowAdapter = new ProductsPanelAdapter(null, getString(R.string.mobile_flow_entrance_content));
            mFlowEntranceProductPanel.setAdapter(flowAdapter);
            mFlowEntranceProductPanel.setType(MOBILE_FLOW);
            flowAdapter.setOnItemClickListener(new ProductsPanelAdapter.OnMobileProductItemClickListener() {
                                                   @Override
                                                   public void onItemClick(View view, int position) {
                                                       Object tag = view.getTag();
                                                       int type = 0;
                                                       if (tag != null) {
                                                           try {
                                                               type = (int) tag;
                                                           } catch (Exception e) {
                                                               e.printStackTrace();
                                                           }
                                                       }
                                                       if (type == ProductsPanelAdapter.VIEW_MORETYPE) {
                                                           Intent intent = new Intent(MobileConstant.ACTIVITY_ACTION.MOBILE_FLOW);
                                                           intent.putExtra(MOBILE_PARAM.MOBILENUMBER, getCurrentNumber());
                                                           intent.putExtra(WalletConstant.EXTRA_FROM, getPackageName());
                                                           startActivity(intent);
                                                       }
                                                   }
                                               }
            );
        } else {
            mFlowEntrancell.setVisibility(View.GONE);
        }
        getPhoneInfo();
        fillPhoneNumber(savedInstanceState, number);
    }

    private void checkPermisss() {
        if (!isRequestingPermissin) {
            if (PermissionCheckHelper.checkContactsPermission(this, PERMISSIONS_REQUEST_CODE2) == PackageManager.PERMISSION_GRANTED) {
                if (mPhoneEdittext != null) {
                    mPhoneEdittext.onNumberChanged();
                }
            } else {
                isRequestingPermissin = true;
            }
        }
    }

    private void fillPhoneNumber(Bundle savedInstanceState, String number) {
        LogHelper.i("[%S] fillPhoneNumber execute", TAG);
        boolean isCheckNumber = true;
        String savedNumber = null;
        if (savedInstanceState != null) {
            savedNumber = savedInstanceState.getString(INPUTNUMBER);
            if (!TextUtils.isEmpty(savedNumber)) {
                number = savedNumber;
                isCheckNumber = false;
            }
        }
        if (TextUtils.isEmpty(number)) {
            if (mRecordHistoryNumberV != null) {
                isLoadRecordNumber = true;
                mRecordHistoryNumberV.loadRecordNumber();
            }
        } else {
            setMobileNumberVData(number, isCheckNumber);
        }
        LogHelper.i("[%S] fillPhoneNumber execute", TAG);
    }

    private void loadData() {
        if (mPhoneEdittext != null && !mHaveProductData) {
            mPhoneEdittext.onNumberChanged();
        }
        if (mBannerListData == null) {
            getBannerDataAsyncTask();
        }
    }

    private boolean setMobileNumberVData(String number) {
        return setMobileNumberVData(number, true);
    }

    private boolean setMobileNumberVData(String number, boolean isCheckNumber) {
        boolean hasData = false;
        LogHelper.d("[%S] setMobileNumber == " + number, TAG);
        if (isCheckNumber) {
            number = PhoneNumberUtils.checkPhoneNumber(number, true);
        }
        if (!TextUtils.isEmpty(number)) {
            hasData = true;
            if (mPhoneEdittext != null) {
                mPhoneEdittext.setText(number);
                mPhoneEdittext.setSelection(number.length());
                mPhoneEdittext.onNumberChanged();
                LogHelper.d("[%S] setMobileNumber == " + number + " | success", TAG);
            }
        }
        return hasData;
    }

    private void showNetFailToast() {
        Toast.makeText(MobileActivity.this, mPromptNetConnectionFail, Toast.LENGTH_SHORT).show();
    }

    private void showInputRightNumberToast() {
        Toast.makeText(MobileActivity.this, mPromptInputRightNumber, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        sendCheckStatusMsg();
        if (mIsShowDialog) {
            showUserSelectDialog(this);
        }
    }

    @Override
    protected void onNetWorkChanged(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
            loadData();
        }
    }

    private void sendCheckStatusMsg() {
        if (handler != null) {
            handler.sendEmptyMessage(CHECK_STATUS);
        }
    }

    private void getPhoneInfo() {
        LogHelper.d("[%S] getPhoneInfo start time = " + System.currentTimeMillis(), TAG);
        mDividerNumber = UiUtils.getDevicePhoneNumber(MobileActivity.this);
        mSimCard0 = DeviceUtils.getPhoneNumber0(MobileActivity.this);
        mSimCard1 = DeviceUtils.getPhoneNumber1(MobileActivity.this);
        LogHelper.d("[%S] getPhoneInfo end time = " + System.currentTimeMillis(), TAG);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        hideSoftkeyboard(mPhoneEdittext);
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mUserSelectDialog != null && mUserSelectDialog.isShowing()) {
            mUserSelectDialog.dismiss();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        hideSoftkeyboard(mPhoneEdittext);
        mGotoOrderList = false;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mobile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_list_orders) {
            hideSoftkeyboard(mPhoneEdittext);
            goToOrderList();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean goToOrderList() {
        mGotoOrderList = true;
        if (AccountHelper.getInstance().loginLetvAccountIfNot(this, null) == true) {
            mGotoOrderList = false;
            startOrderListActivity();
            return true;
        }
        return false;
    }

    private void startOrderListActivity() {
        Intent intent = new Intent(MobileActivity.this, MobileOrderListActivity.class);
        intent.putExtra(MOBILE_PARAM.TYPE, mFeeOrFlow == PRODUCT_TYPE.MOBILE_FEE ?
                MOBILE_PARAM.FEE : MOBILE_PARAM.FLOW);
        startActivity(intent);
    }

    private void startContactActivity() {
        int result = PermissionCheckHelper.checkContactsPermission(MobileActivity.this, PERMISSIONS_REQUEST_CODE);
        if (result == PermissionCheckHelper.PERMISSION_ALLOWED) {
            goContacts();
        }
    }

    private void goContacts() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            ComponentName component = new ComponentName("com.android.contacts",
                    "com.android.contacts.activities.ContactSelectionActivity");
            intent.setComponent(component);
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            startActivityForResult(intent, PICK_CONTACT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getBannerDataAsyncTask() {
        if (mBannerAsyncT == null) {
            mBannerAsyncT = new BannerTask(getBaseContext(), bannerCallback
                    , getBusinessId(mFeeOrFlow));
            ExecutorHelper.getExecutor().runnableExecutor(mBannerAsyncT);
        }
    }

    private MainPanelHelper.Callback<WalletBannerListBean> bannerCallback = new MainPanelHelper.Callback<WalletBannerListBean>() {
        @Override
        public void onLoadFromLocalFinished(WalletBannerListBean result, int errorCode) {
            mBannerAsyncT = null;
            if (mBannerLl == null || mBannerV == null) {
                return;
            }
            mBannerListData = result;
            if (result != null && result.list.length > 0) {
                mBannerV.bindBannerView(result.list);
                if (mBannerLl.getVisibility() == View.GONE) {
                    mBannerLl.setVisibility(View.VISIBLE);
                }
                return;
            }
            if (mBannerLl.getVisibility() == View.VISIBLE) {
                mBannerLl.setVisibility(View.GONE);
            }
        }

        @Override
        public void onLoadFromNetworkFinished(WalletBannerListBean result, int errorCode, boolean needUpdate) {
            mBannerAsyncT = null;
            if (!needUpdate || mBannerLl == null || mBannerV == null) {
                return;
            }
            mBannerListData = result;
            if (errorCode == MainPanelHelper.NO_ERROR) {
                if (result != null && result.list.length > 0) {
                    mBannerV.bindBannerView(result.list);
                    if (mBannerLl.getVisibility() == View.GONE) {
                        mBannerLl.setVisibility(View.VISIBLE);
                    }
                    return;
                }
            }
            if (mBannerLl.getVisibility() == View.VISIBLE) {
                mBannerLl.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goContacts();
                }
                break;
            case PERMISSIONS_REQUEST_CODE2:
                isRequestingPermissin = false;
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mPhoneEdittext != null) {
                        mPhoneEdittext.onNumberChanged();
                    }
                    LogHelper.d("[%S] onRequestPermissionsResult PERMISSIONS_REQUEST_CODE2 execute", TAG);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = null;
                    String phoneNumber = null;
                    String name = null;
                    try {
                        c = this.getContentResolver().query(contactData, new String[]{
                                Data.RAW_CONTACT_ID, Data.DATA1}, null, null, null);
                        if (c != null && c.getCount() > 0) {
                            if (c.moveToFirst()) {
                                int rawContactId = c.getInt(0);
                                phoneNumber = c.getString(1);
                                LogHelper.d("[%S] onActivityResult rawContactId == [%S] | phoneNumber == [%S]", TAG, rawContactId, phoneNumber);
                                Cursor cursor = this.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, new String[]{
                                        ContactsContract.Contacts.DISPLAY_NAME}, "_ID" + " = " + rawContactId, null, null);
                                if (cursor != null && cursor.getCount() > 0) {
                                    if (cursor.moveToFirst()) {
                                        name = cursor.getString(0);
                                        LogHelper.d("[%S] onActivityResult name == [%S]", TAG, name);
                                        phoneNumber = PhoneNumberUtils.checkPhoneNumber(phoneNumber, true);
                                        if (!TextUtils.isEmpty(phoneNumber)) {
                                            if (name == null || name.equals("") || name.equals(phoneNumber)) {
                                                mContactType = MobileConstant.CONTACT_TYPE.UNCONTACT;
                                                name = getResources().getString(R.string.mobile_phone_number_no_name);
                                            } else {
                                                mContactType = MobileConstant.CONTACT_TYPE.CONTACTS;
                                            }
                                            if (mViewMobileDesc != null) {
                                                mViewMobileDesc.setText("");
                                            }
                                            setMobileName(name);
                                            updateProductList(null);
                                            if (mFeeOrFlow == PRODUCT_TYPE.MOBILE_FLOW) {
                                                updateDocPrompt(null);
                                            }
                                            setMobileNumberVData(phoneNumber, false);
                                        } else {
                                            showInputRightNumberToast();
                                        }
                                        cursor.close();
                                        hideSoftkeyboard(mPhoneEdittext);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        LogHelper.e(e);
                    } finally {
                        c.close();
                    }
                }
                break;
        }
    }


    public void onClick_Contact(View view) {
        hideSoftkeyboard(view);
        startContactActivity();
    }

    public void onClick_ClearAll(View view) {
        clearMobileNumberInfo();
        if (mPhoneEdittext != null) {
            mPhoneEdittext.setText("");
        }
        if (mRecordHistoryNumberV != null) {
            mRecordHistoryNumberV.show();
        }
    }

    public void onClick_matterAttention(View view) {
        Intent intent = new Intent(MobileActivity.this, MobileWebActivity.class);
        intent.putExtra(CommonConstants.EXTRA_URL,
                (mFeeOrFlow == PRODUCT_TYPE.MOBILE_FEE) ? MobileConstant.PATH.MOBILE_MATTER_ATTENTION_FEE : MobileConstant.PATH.MOBILE_MATTER_ATTENTION_FLOW);
        intent.putExtra(CommonConstants.EXTRA_TITLE_NAME, getString(R.string.label_matter_attention));
        startActivity(intent);
    }

    @Override
    public boolean onNumberChanged(String content, int length) {
        switch (length) {
            case MOBILE_LEN:
                isChangedNumber = true;
                LogHelper.d("[%S] onNumberChanged execute", TAG);
                if (content.equals(mSimCard0) || content.equals(mSimCard1)) {
                    mContactType = MobileConstant.CONTACT_TYPE.THIS_MACHINE;
                    setMobileName(mDividerPhoneNumberStr);
                } else {
                    String name = mViewMobileName.getText().toString();
                    if (name == null || name.equals("")) {
                        int result =
                                PermissionCheckHelper.checkContactsPermission(MobileActivity.this, -1);
                        if (result == PermissionCheckHelper.PERMISSION_ALLOWED) {
                            getContactNameAsyncTask(content);
                        }
                    }
                }
                hideHistoryNumberV();
                if (mFeeOrFlow == PRODUCT_TYPE.MOBILE_FLOW) {
                    queryFlowDocPrompt(MobileConstant.DOCKEY.DOC_FLOW_KEY);
                }
                queryMobileProducts(mFeeOrFlow, content);
                return true;
            case 0:
                mRecordHistoryNumberV.show();
                clearMobileNumberInfo();
                return false;
            case CHECK_MOBILE_INDEX:
                clearMobileNumberInfo();
            default:
                /**
                 * TODO: 筛选手机号
                 */
                HistoryRecordNumberBean allRecord;
                allRecord = mRecordHistoryNumberV.getData();
                if (allRecord != null) {
                    HistoryRecordNumberBean filtrateRecord = getFiltrateData(allRecord, content);
                    if (filtrateRecord.getRecordInfo().size() <= 0) {
                        hideHistoryNumberV();
                        return false;
                    }
                    showHistoryNumberV();
                    mRecordHistoryNumberV.updateData(getFiltrateData(allRecord, content));
                }
                return false;
        }
    }

    private String getCurrentNumber() {
        if (mPhoneEdittext != null) {
            return mPhoneEdittext.getMobileNumber();
        }
        return "";
    }

    private void setMobileName(String name) {
        if (mViewMobileName != null) {
            mViewMobileName.setText(name);
            if (!TextUtils.isEmpty(name) && mViewMobileName.getVisibility() == View.INVISIBLE) {
                mViewMobileName.setVisibility(View.VISIBLE);
            }
        }
        if (mViewMobileDesc != null && TextUtils.isEmpty(mViewMobileDesc.getText())) {
            mViewMobileDesc.setVisibility(View.INVISIBLE);
            if (mLine1 != null) {
                mLine1.setVisibility(View.INVISIBLE);
            }
        }
    }

    private HistoryRecordNumberBean getFiltrateData(HistoryRecordNumberBean allRecord, String number) {
        HistoryRecordNumberBean filtrateData = new HistoryRecordNumberBean();
        List<HistoryRecordNumberBean.RecordInfoBean> record_info = new ArrayList<HistoryRecordNumberBean.RecordInfoBean>();
        for (int i = 0; i < allRecord.getRecordInfo().size(); i++) {
            HistoryRecordNumberBean.RecordInfoBean recordInfo = allRecord.getRecordInfo().get(i);
            if (recordInfo.getPhoneNum().startsWith(number)) {
                record_info.add(recordInfo);
            }
        }
        filtrateData.record_info = record_info;
        return filtrateData;
    }

    @Override
    public void onHistoryRecordNumberItemClick(View view) {
        HistoryRecordNumberBean.RecordInfoBean record_info = (HistoryRecordNumberBean.RecordInfoBean) view.getTag();
        String number = record_info.getPhoneNum();
        if (!TextUtils.isEmpty(number)) {
            setMobileNumberVData(number, true);
        }
    }

    @Override
    public void onHistoryNumberStatus(int state) {
        handler.removeMessages(CLEAR_HISTORY_RET);
        Message msg = Message.obtain();
        msg.arg1 = state;
        msg.what = CLEAR_HISTORY_RET;
        // 发送这个消息到消息队列中
        handler.sendMessage(msg);
    }

    @Override
    public void hideV() {
        hideHistoryNumberV();
    }

    @Override
    public void showV() {
        showHistoryNumberV();
    }

    @Override
    public void setRecordNumber(HistoryRecordNumberBean numberBean) {
        LogHelper.d("[%S] setRecordNumber execute", TAG);
        if (numberBean == null || numberBean.getRecordInfo().size() <= 0) {
            getPhoneInfo();
            setMobileNumberVData(mDividerNumber);
        } else {
            HistoryRecordNumberBean.RecordInfoBean infoBean = numberBean.getRecordInfo().get(0);
            String recordNumber = infoBean.getPhoneNum();
            if (!TextUtils.isEmpty(recordNumber) && !recordNumber.equals(mDividerNumber)) {
                setMobileName(infoBean.getName());
                setMobileNumberVData(infoBean.getPhoneNum());
            }
        }
        isLoadRecordNumber = false;
    }

    private void showHistoryNumberV() {
        if (mRecordHistoryNumberV.getVisibility() == View.GONE) {
            mRecordHistoryNumberV.setVisibility(View.VISIBLE);
        }
        if (mRecordHistoryNumberRl.getVisibility() == View.GONE) {
            mRecordHistoryNumberRl.setVisibility(View.VISIBLE);
        }
    }

    private void hideHistoryNumberV() {
        if (mRecordHistoryNumberV.getVisibility() == View.VISIBLE) {
            mRecordHistoryNumberV.setVisibility(View.GONE);
        }
        if (mRecordHistoryNumberRl.getVisibility() == View.VISIBLE) {
            mRecordHistoryNumberRl.setVisibility(View.GONE);
        }
    }

    protected void hideSoftkeyboard(View view) {
        clearMobileNumberFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void hideUserSelectDialog() {
        if (mUserSelectDialog != null) {
            mIsShowDialog = false;
            if (mUserSelectDialog.isShowing()) {
                mUserSelectDialog.dismiss();
            }
        }
    }

    private void showUserSelectDialog(Context context) {
        if (context == null) return;
        if (mUserSelectDialog == null) {
            mUserSelectDialog = new LeBottomSheet(context);
            mUserSelectDialog.setStyle(LeBottomSheet.BUTTON_DEFAULT_STYLE,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            hideUserSelectDialog();
                            String newNumber = mPhoneEdittext.getMobileNumber();
                            if (PhoneNumberUtils.checkPhoneNumber(newNumber, true) == null) {
                                Toast.makeText(MobileActivity.this, mPromptInputRightNumber, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            loadData();
                        }
                    },
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            hideUserSelectDialog();
                        }
                    }, null,
                    new String[]{
                            context.getString(R.string.mobile_prompt_user_select_reload),
                            context.getString(R.string.mobile_prompt_user_select_cancel)
                    },
                    context.getString(R.string.mobile_prompt_user_select_load_title),
                    null, null, context.getResources().getColor(R.color.colorBtnBlue), false);
        }
        mUserSelectDialog.show();
        mIsShowDialog = true;
    }

    private void clearMobileNumberFocus() {
        if (mPhoneEdittext != null) {
            mPhoneEdittext.clearFocus();
        }
    }

    protected void updateProductList(ProductBean data) {
        handler.removeMessages(UPDATE_PRODUCT_LIST);
        Message msg = Message.obtain();
        msg.obj = data;
        msg.what = UPDATE_PRODUCT_LIST;
        // 发送这个消息到消息队列中
        handler.sendMessage(msg);
    }

    protected void updateDocPrompt(DocPromptBean data) {
        handler.removeMessages(UPDATE_DOC_PROMPT);
        Message msg = Message.obtain();
        msg.obj = data;
        msg.what = UPDATE_DOC_PROMPT;
        handler.sendMessage(msg);
    }

    private void clearMobileNumberInfo() {
        updateProductList(null);
        if (mFeeOrFlow == PRODUCT_TYPE.MOBILE_FLOW) {
            updateDocPrompt(null);
        }
        if (mViewMobileName != null) {
            mViewMobileName.setText("");
            mViewMobileName.setVisibility(View.INVISIBLE);
        }
        if (mLine1 != null) {
            mLine1.setVisibility(View.INVISIBLE);
        }
        if (mViewMobileDesc != null) {
            mViewMobileDesc.setVisibility(View.INVISIBLE);
        }
    }

    private void updateNumberDesc(String desc) {
        if (mViewMobileDesc == null) return;
        if (TextUtils.isEmpty(desc)) {
            mViewMobileDesc.setText("");
            mViewMobileDesc.setVisibility(View.INVISIBLE);
            if (mLine1.getVisibility() == View.VISIBLE) {
                mLine1.setVisibility(View.INVISIBLE);
            }
            return;
        }
        String label = String.format(mPhoneNumDefDescFormatStr, desc);
        mViewMobileDesc.setText(label);
        mViewMobileDesc.setVisibility(View.VISIBLE);
        if (mLine1.getVisibility() == View.INVISIBLE) {
            mLine1.setVisibility(View.VISIBLE);
        }
    }

    private void getContactNameAsyncTask(String number) {
        if (mContactTask == null) {
            mContactTask = new ContactNameTask();
        }
        mContactTask.setData(number);
        ExecutorHelper.getExecutor().runnableExecutor(mContactTask);
    }

    private class ContactNameTask implements Runnable {
        private String mPhoneNumber;

        public void setData(String phoneNumber) {
            mPhoneNumber = phoneNumber;
        }

        @Override
        public void run() {
            String name;
            try {
                if (TextUtils.isEmpty(mPhoneNumber))
                    return;
                name = UiUtils.getContactNameByNumber(MobileActivity.this, mPhoneNumber);
            } catch (Exception e) {
                e.printStackTrace();
                name = "";
            }
            if (!TextUtils.isEmpty(name)) {
                if (name.equals(mPhoneNumber)) {
                    name = getResources().getString(R.string.mobile_phone_number_no_name);
                    mContactType = MobileConstant.CONTACT_TYPE.UNCONTACT;
                } else {
                    mContactType = MobileConstant.CONTACT_TYPE.CONTACTS;
                }
            } else {
                mContactType = MobileConstant.CONTACT_TYPE.UNCONTACT;
            }
            Message msg = Message.obtain();
            msg.obj = name;
            msg.what = UPDATE_CONTACTNAME;
            handler.sendMessage(msg);
        }
    }

    private void queryMobileProducts(int type, String number) {
        if (!isNetworkAvailable()) {
            showNetFailToast();
            return;
        }
        LogHelper.d("[%S] queryMobileProducts execute", TAG);
        if (TextUtils.isEmpty(number)) return;
        if (mProductsTask == null) {
            mProductsTask = new ProductsTask();
        }
        mProductsTask.setData(type, number);
        ExecutorHelper.getExecutor().runnableExecutor(mProductsTask);
    }

    private class ProductsTask implements Runnable {
        private String queryNumber = "";
        private int type;

        public void setData(int type, String queryNumber) {
            this.type = type;
            this.queryNumber = queryNumber;
        }

        @Override
        public void run() {
            BaseResponse<ProductBean> response = null;
            try {
                String PATH = MobileConstant.PATH.PRODUCT;
                LogHelper.i("[%S] request data", TAG);
                BaseRequestParams reqParams = new BaseRequestParams(PATH);
                reqParams.addQueryStringParameter(MobileConstant.PARAM.NUMBER, queryNumber);
                reqParams.addQueryStringParameter(MobileConstant.PARAM.TYPE, String.valueOf(type));
                TypeToken typeToken = new TypeToken<BaseResponse<ProductBean>>() {
                };
                response = xmain.http().postSync(reqParams, typeToken.getType());
                handler.removeMessages(MSG_LOAD_PRODUCT_LIST_FINISH);
                Message msg = Message.obtain();
                msg.obj = response;
                msg.what = MSG_LOAD_PRODUCT_LIST_FINISH;
                handler.sendMessage(msg);
            } catch (Exception e) {
            } catch (Throwable throwable) {
            }
        }
    }

    private void queryFlowDocPrompt(String docKey) {
        if (!isNetworkAvailable()) return;
        if (mDocPromptBean == null || TextUtils.isEmpty(mDocPromptBean.getDoc_content())) {
            LogHelper.d("[%S] queryFlowDocPrompt execute", TAG);
            if (TextUtils.isEmpty(docKey)) return;
            if (mDocPromptAsyncT == null) {
                mDocPromptAsyncT = new DocPromptTask();
            }
            mDocPromptAsyncT.setDocKey(docKey);
            ExecutorHelper.getExecutor().runnableExecutor(mDocPromptAsyncT);
        } else {
            updateDocPrompt(mDocPromptBean);
        }
    }

    private class DocPromptTask implements Runnable {

        private String mDocKey;

        public void setDocKey(String docKey) {
            this.mDocKey = docKey;
        }

        @Override
        public void run() {
            if (TextUtils.isEmpty(mDocKey)) return;
            BaseResponse<DocPromptBean> response = null;
            try {
                String PATH = MobileConstant.PATH.DOC;
                LogHelper.i("[%S] request DocPrompt data", TAG);
                BaseRequestParams reqParams = new BaseRequestParams(PATH);
                reqParams.addQueryStringParameter(MobileConstant.PARAM.DOC_KEY, mDocKey);
                TypeToken typeToken = new TypeToken<BaseResponse<DocPromptBean>>() {
                };
                response = xmain.http().getSync(reqParams, typeToken.getType());
            } catch (Exception e) {
            } catch (Throwable throwable) {
            }
            LogHelper.i("[%S] Response data", TAG);
            DocPromptBean docPromptBean = null;
            if (response != null) {
                docPromptBean = response.data;
            }
            updateDocPrompt(docPromptBean);
        }
    }
}
