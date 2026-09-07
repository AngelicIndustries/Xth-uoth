package com.aurilux.xar;

public class XARModInfo {

    public static final String MOD_ID = "Xthuoth";
    public static final String MOD_NAME = "Xth'uoth Revived";
    public static final String MOD_TITLE = MOD_NAME + ": The Aberrant Realm";
    public static final String MOD_VERSION = Tags.VERSION;
    public static final String MOD_DEPEND = "required-after:Thaumcraft@[4.2.3.5,)";

    public static final String CLIENT_PROXY = "com.aurilux.xar.proxy.XARClientProxy";
    public static final String SERVER_PROXY = "com.aurilux.xar.proxy.XARServerProxy";
    public static final String GUI_FACTORY = "com.aurilux.xar.client.gui.config.XARGuiFactory";
}
