package com.djcx.sign;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import cn.org.bjca.anysign.android.api.Interface.OnSignatureResultListener;
import cn.org.bjca.anysign.android.api.core.OriginalContent;
import cn.org.bjca.anysign.android.api.core.SignRule;
import cn.org.bjca.anysign.android.api.core.SignatureAPI;
import cn.org.bjca.anysign.android.api.core.SignatureObj;
import cn.org.bjca.anysign.android.api.core.Signer;
import cn.org.bjca.anysign.android.api.core.domain.AnySignBuild;
import cn.org.bjca.anysign.android.api.core.domain.BJCAAnySignAVType;
import cn.org.bjca.anysign.android.api.core.domain.OriginalContentType;
import cn.org.bjca.anysign.android.api.core.domain.SignResult;
import cn.org.bjca.anysign.android.api.core.domain.SignatureType;
import cn.org.bjca.anysign.core.domain.BJCASignatureBoardType;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/*
 * @Desc: 手信签插件代理类
 * @Date:  2020/6/10 13:51
 * @Author: lizubing
 */
public class SignDelegate {

    private String TAG = "SignDelegate";

    private final Activity activity;
    private MethodChannel.Result pendingResult;
    private MethodCall methodCall;

    private SignatureAPI api;
    private byte[] bTemplate = null;

    private String encAlg;
    private String path_root = Environment.getExternalStorageDirectory() + "/anysign_2.5.0.txt";

    private int apiResult;

    private String cacheResult = "";

    public SignDelegate(
            final Activity activity) {
        this.activity = activity;
    }

    /**
     * 初始化信手签
     *
     * @param call
     * @param result
     */
    public void initSign(MethodCall call, MethodChannel.Result result) {
        if (!setPendingMethodCallAndResult(methodCall, result)) {
            finishWithAlreadyActiveError(result);
            return;
        }
        initApi(result);
    }

    /**
     * 开始信手签
     *
     * @param call
     * @param result
     */
    public void startSign(MethodCall call, MethodChannel.Result result) {
        if (!setPendingMethodCallAndResult(methodCall, result)) {
            finishWithAlreadyActiveError(result);
            return;
        }
        startSign(result);
    }


    private void finishWithAlreadyActiveError(MethodChannel.Result result) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", false);
        resultMap.put("errorCode", "already_active");
        resultMap.put("description", "sign is already active");
        result.success(resultMap);
//        result.error("already_active", "Meglive is already active", null);
    }


    private void startSign(MethodChannel.Result result) {
        if (api != null) {
            apiResult = api.showSignatureDialog(0);// 弹出单签签名框签名
            if (apiResult == SignatureAPI.SUCCESS) {
            } else {
                Log.v(TAG, "错误码：" + apiResult);
            }
        } else {
            Log.v(TAG, "请先初始化API");
        }
    }

    private void initApi(MethodChannel.Result result) {
        try {
            // 设置签名算法，默认为RSA，可以设置成SM2
            AnySignBuild.Default_Cert_EncAlg = encAlg;
            //所有签名提示隐私协议
            AnySignBuild.privacyTold = true;
            /*
             *  初始化API
             */
            api = new SignatureAPI(activity);
            /*
             *  设置渠道号
             */
//			所有api接口中设置成功返回 SignatureAPI.SUCCESS（0），其他错误
            apiResult = api.setChannel("999999");
            Log.e("XSS", "apiResult -- setChannel：" + apiResult);
            if (apiResult == SignatureAPI.SUCCESS) {
                Log.e("XSS", "apiResult -- setChannel：成功");
            } else {
                Log.e("XSS", "apiResult -- setChannel：失败");
            }
            /*
             * 设置模版数据
             */
            InputStream is = activity.getResources().openRawResource(R.raw.test);
            bTemplate = new byte[is.available()];
            is.read(bTemplate);

            /**
             * 配置此次签名对应的模板数据
             * 参数1：表示模板类型，不可为空：如果为PDF和HTML格式，调用下面构造函数
             *        ContextID.FORMDATA_PDF：PDF格式，ContextID.FORMDATA_HTML：HTML格式
             * 参数2：表示模板数据byte数组类型，不可为空
             * 参数3：业务流水号/工单号，不可为空
             */
            apiResult = api.setOrigialContent(new OriginalContent(OriginalContentType.CONTENT_TYPE_HTML, bTemplate, "111"));
            Log.e("XSS", "apiResult -- setOrigialContent：" + apiResult);
            /*
             * 注册手写签名对象，可注册多个
             */
//			 实例化签名规则，三种方式，任选其一
//			SignRule signRule = SignRule.getInstance(SignRuleType.TYPE_KEY_WORD);
//			 方式一：使用关键字方式定位签名图片位置(根据X轴偏移量+Y轴偏移量，定位)
//			 参数1：keyWord - 关键字
//			 参数2：XOffset -  签名图片相对于关键字X轴偏移量
//			 参数3：YOffset - 签名图片相对于关键字Y轴偏移量，单位dip
//			 参数4：从Pdf的第pageNo页开始搜索此关键字，直到找到或者pdf结束为止，从1开始，默认为1
//			 参数5：从Pdf的第pageNo页开始搜索第几个关键字，直到找到或者pdf结束为止，从第1页开始
//			signRule.setKWRule(new KWRule("申请人/经办人", 10, 10, 1, 1));

//			 方式二：使用坐标定位签名图片位置
//			 参数1：left - 签名图片最左边坐标值，相对于PDF当页最左下角(0,0)点
//			 参数2：top - 签名图片顶边坐标值，相对于PDF当页最左下角(0,0)点
//			 参数3：right - 签名图片最右边坐标值，相对于PDF当页最左下角(0,0)点
//			 参数4：bottom - 签名图片底边坐标值，相对于PDF当页最左下角(0,0)点
//			 参数5：pageNo - 签名在PDF中的页码
//			 参数5：unit - 坐标单位
            SignRule signRule = SignRule.getInstance(SignRule.SignRuleType.TYPE_XYZ);
            signRule.setXYZRule(new SignRule.XYZRule(84, 523, 200, 411, 0, "dp"));

//			 方式三：使用在服务器端配置好的信息定位签名图片位置
//			 参数1：123为服务器配置好的签名规则
//			signRule = SignRule.getInstance(SignRuleType.TYPE_USE_SERVER_SIDE_CONFIG);
//			rule.setServerConfigRule("123");

//			 实例化签名人信息
//			参数1：姓名
//			参数2：唯一id,身份证等
//			参数3：证件类型
//						SignerCardType.TYPE_IDENTITY_CARD 身份证
//						SignerCardType.TYPE_OFFICER_CARD	军官证
//						SignerCardType.TYPE_PASSPORT_CARD	护照
//						SignerCardType.TYPE_RESIDENT_CARD	户口页
            Signer signer = new Signer("李白", "111", Signer.SignerCardType.TYPE_IDENTITY_CARD);
//			实例化手写签名对象
//			参数1：手写签名对象索引值；参数2：签名规则；参数3：签名人信息
            SignatureObj obj = new SignatureObj(0, signRule, signer);
//			设置签名图片高度，单位dip
            obj.single_height = 100;
//			设置签名图片宽度，单位dip
            obj.single_width = 100;
//          手写识别开关，true为开启手写识别，false为关闭手写识别
            obj.isdistinguish = false;
//			签名是否必须,设置为true时必须进行签名，默认true
            obj.nessesary = false;
//			设置签名笔迹颜色，默认为黑色
            obj.penColor = Color.RED;

            obj.penSize = 15;
//			需要显示在签名框顶栏的标题
            obj.title = "请李凤国签字";
//			单字签名框中需要突出显示部分的起始位置和结束位置
            obj.titleSpanFromOffset = 1;
//			单字签名框中需要突出显示部分的起始位置和结束位置
            obj.titleSpanToOffset = 3;
//			识别错误提示语
            obj.distinguishErrorText = "识别错误";
//			显示框的类型
            obj.signatureBoardType = BJCASignatureBoardType.BJCAAnySignWordNumberTransformType;

//			 注册单签签名对象
            apiResult = api.addSignatureObj(obj);
            Log.e("XSS", "apiResult -- addSignatureObj：" + apiResult);

            Signer signer_1 = new Signer("李白", "222", Signer.SignerCardType.TYPE_IDENTITY_CARD);
            /**
             * 边拍边签对象初始化
             */
            SignRule signRule_1 = SignRule.getInstance(SignRule.SignRuleType.TYPE_XYZ);
            signRule_1.setXYZRule(new SignRule.XYZRule(84, 523, 200, 411, 0, "dp"));
//			实例化手写签名对象
//			参数1：手写签名对象索引值；参数2：签名规则；参数3：签名人信息
            SignatureObj obj_1 = new SignatureObj(1, signRule_1, signer_1);
//			设置签名图片高度，单位dip
            obj_1.single_height = 300;
//			设置签名图片宽度，单位dip
            obj_1.single_width = 300;
//          手写识别开关，true为开启手写识别，false为关闭手写识别
            obj_1.isdistinguish = false;
//			手写识别要识别的姓名，如不设置此属性则以Signer中的姓名为主
            obj_1.agentName = "李白";
//			签名是否必须,设置为true时必须进行签名，默认true
            obj_1.nessesary = false;
//			设置签名笔迹颜色，默认为黑色
            obj_1.penColor = Color.RED;
//			需要显示在签名框顶栏的标题
            obj_1.title = "请李凤国签字";
//			单字签名框中需要突出显示部分的起始位置和结束位置
            obj_1.titleSpanFromOffset = 1;
//			单字签名框中需要突出显示部分的起始位置和结束位置
            obj_1.titleSpanToOffset = 3;
//			是否开启多媒体功能（拍照/录像）
            obj_1.openCamera = true;
// 			是否在边拍边签中打开人脸识别
            obj_1.openFaceDetection = true;
//			人脸识别错误提示语
            obj_1.checkfaceMarkedwords = "人脸检测失败了";
//			区分边拍边签还是边录边签（BJCAAnySign_AVType_PHOTO 拍照/BJCAAnySign_AVType_VIDEO 录像）
            obj_1.bjcaAnySignAVType = BJCAAnySignAVType.BJCAAnySign_AVType_PHOTO;
//			是否把拍照/录音证据添加到证据列表中
            obj_1.isAddEvidence = true;
//			识别错误提示语
            obj_1.distinguishErrorText = "识别错误";

//			注册单签签名对象
            apiResult = api.addSignatureObj(obj_1);
            Log.e("XSS", "apiResult -- addSignatureObj：" + apiResult);

            /*
             * 注册签名结果回调函数
             */
            api.setOnSignatureResultListener(new OnSignatureResultListener() {

                @Override
                public void onSignResult(final SignResult signResult) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("success", true);
                    resultMap.put("delta", "delta");
                    Bitmap b = signResult.signature;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();
                    Map<String, byte[]> images = new HashMap<>();
                    images.put("image_best",data);
                    resultMap.put("images", images);
                    finishWithSuccess(resultMap);
                    Log.e("XSS", "images " + data.length+"----------"+b.getByteCount());

                    Log.e("XSS", "onSignResult signIndex : " + signResult.signIndex + "  resultCode : " + signResult.resultCode + "  signType : " + signResult.signType
                            + "  eviPic : " + signResult.eviPic);
                }

                @Override
                public void onCancel(int index, SignatureType signType) {
                    Log.e("XSS", "onCancel index : " + index + "  signType : " + signType);
                }

                @Override
                public void onDismiss(int index, SignatureType signType) {
                    Log.e("XSS", "onDismiss index : " + index + "  signType : " + signType);
                }
            });
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("success", true);
            finishWithSuccess(resultMap);
            Log.v(TAG, "初始化API成功");
        } catch (Exception e1) {
            e1.printStackTrace();
            //授权失败
            finishWithError("initFailure", "初始化API失败");
            Log.v(TAG, "初始化API失败");
        }
    }


    private void finishWithSuccess(Object result) {
        if (pendingResult == null) {
            return;
        }
        pendingResult.success(result);
        clearMethodCallAndResult();
    }

    private void showImgPreviewDlg(Bitmap img) {
        ImageView iv = new ImageView(activity);
        iv.setBackgroundColor(Color.WHITE);
        iv.setImageBitmap(img);
        new AlertDialog.Builder(activity).setView(iv).show();
    }


    private boolean setPendingMethodCallAndResult(
            MethodCall methodCall, MethodChannel.Result result) {
        if (pendingResult != null) {
            return false;
        }

        this.methodCall = methodCall;
        pendingResult = result;


        return true;
    }


    private void finishWithError(String errorCode, String errorMessage) {
        if (pendingResult == null) {
            return;
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", false);
        resultMap.put("errorCode", errorCode);
        resultMap.put("description", errorMessage);
        pendingResult.success(resultMap);
//        pendingResult.error(errorCode, errorMessage, null);
        clearMethodCallAndResult();
    }

    private void clearMethodCallAndResult() {
        methodCall = null;
        pendingResult = null;
    }


}
