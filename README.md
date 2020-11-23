# KbDeviceId

	![Download](https://api.bintray.com/packages/kingbogo/maven/GetDeviceId/images/download.svg)

获取Android设备唯一标识码。

```
implementation 'com.github.kingbogo:getDeviceId:{version}'
```

### 使用方法
```
AndroidDeviceId.getDeviceId(getApplicationContext(), deviceId -> {
	KbLogUtil.i("deviceId => " + deviceId);
	mTipsTv.setText(deviceId);
});
```

### 混淆规则
```
-keep class com.kingbogo.getdeviceid.** {*;}
```

