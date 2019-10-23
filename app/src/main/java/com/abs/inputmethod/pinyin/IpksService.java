package com.abs.inputmethod.pinyin;

import android.text.TextUtils;
import android.util.Log;

import com.abs.inputmethod.pinyin.db.SessionKey;
import com.abs.inputmethod.pinyin.db.SessionKeyUtil;
import com.abs.inputmethod.pinyin.model.IpksSessionKey;
import com.abs.inputmethod.pinyin.utils.JsonUtil;

import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import config.ConfigPointer;
import config.NodeInfo;
import config.PeerInfo;
import ipks.Message;
import ipks.Messages;
import ipks.NewMessage;
import ipks.ServerInfo;

public class IpksService implements Runnable {
    private static final String THIS_FILE = "IpksService";

    private ServerInfo serverInfo;
    private boolean isRunning = false;
    /**
     * ipks未启动
     */
    private final int STATUS_STOP = 0;
    /**
     * ipks启动中
     */
    private final int STATUS_STARTING = 1;
    /**
     * ipks已启动
     */
    private final int STATUS_STARTED = 2;
    private int status = STATUS_STOP;

    @Override
    public void run() {
        Log.d(THIS_FILE, "run");
        String privateKey = InputMethodApplication.getInstance().getPrivateKey();
        if (TextUtils.isEmpty(privateKey)) {
            Log.d(THIS_FILE, "privateKey is empty");
            return;
        }
        if (status == STATUS_STARTING) {
            Log.d(THIS_FILE, "ipks is starting...");
            return;
        }
        status = STATUS_STARTING;
        //初始化配置
        ConfigPointer pointer = new ConfigPointer();
        pointer.setConfig_Nodes("");
        pointer.setConfig_PrivateKey(privateKey);
        pointer.setConfig_ForwarderMode(false);
        pointer.setConfig_Verbosity(1);
        pointer.setConfig_TTL(3 * 24 * 60 * 60);
        pointer.setConfig_WorkTime(0);
        pointer.setConfig_MaxPeerSize(0);
        pointer.setConfig_MaxMessageSize(0);
        pointer.setConfig_POW(0);

        //初始化服务
        serverInfo = new ServerInfo();
        serverInfo.setValue(pointer);

        ConfigPointer tmp = serverInfo.getValue();
        try {
            Log.d(THIS_FILE, "initialize start");
            serverInfo.initialize();
            Log.d(THIS_FILE, "initialize end");
        } catch (Exception e) {
            Log.e("Ipks", "initialize", e);
            e.printStackTrace();
        }
        //启动服务
        try {
            serverInfo.runServer();
            isRunning = true;
            status = STATUS_STARTED;
            Log.i(THIS_FILE, "server run complete");

            Messages ms = new Messages();

            while (isRunning) {
                //构建消息过滤器
                try {
                    // getSymFilterID 收取密码消息。 getAsymFilterID 收取公私钥消息
                    ms = serverInfo.getWapi().getFilterMessages(serverInfo.getAsymFilterID());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                NodeInfo nodeInfo = serverInfo.getNodeInfo();
                Log.d(THIS_FILE, "=======serverInfo.peersCount();:" + serverInfo.peersCount());
                Log.d(THIS_FILE, "getEncode:" + nodeInfo.getEnode());
                PeerInfo peerInfo = serverInfo.getPeerInfo(serverInfo.peersCount() - 1);
                if (peerInfo != null) {
                    Log.d(THIS_FILE, "======= peerInfo.getRemoteAddress():" + peerInfo.getRemoteAddress() + peerInfo.getLocalAddress());
                }

                //接受消息
                long si = ms.size();
                for (long i = 0; i < si; i++) {

                    try {
                        Message m = ms.get(i);
                        if (m.getPayload() != null) {
                            String str = new String(m.getPayload());
                            Log.d(THIS_FILE, "=======m.getPayload();:" + str);
                            Log.d(THIS_FILE, "=======m.getPayload();:" + m.getPayload().length);

                            byte[] newSign = new byte[64];
                            System.arraycopy(m.getSig(), 1, newSign, 0, 64);
                            // 消息发送者
                            String sender = Keys.toChecksumAddress(Numeric.prependHexPrefix(Keys.getAddress(Numeric.toBigInt(newSign))));
                            Log.e(THIS_FILE, "sender:" + sender);
                            IpksSessionKey ipksSessionKey = JsonUtil.StringToObject(new String(m.getPayload()), IpksSessionKey.class);
                            Log.d(THIS_FILE, "insert result:" +
                                    SessionKeyUtil.insertOrUpdateSessionKey(sender, ipksSessionKey.getVersion(), ipksSessionKey.getKey()));
                            InputMethodApplication.getInstance().setSessionKey(sender,
                                    new SessionKey(null, sender, ipksSessionKey.getKey(), ipksSessionKey.getVersion()));
                        }

                        Log.d(THIS_FILE, "=======serverInfo.peersCount();:" + serverInfo.peersCount());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                Thread.currentThread().sleep(5000);//毫秒
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(THIS_FILE, "thread is stop");
            if (serverInfo != null) {
                serverInfo.shutdown();
                serverInfo = null;
            }
            isRunning = false;
            status = STATUS_STOP;
        } catch (Exception e) {
            Log.e("Ipks", "runServer", e);
            e.printStackTrace();
        }
    }

    public void setStop() {
        if (serverInfo != null) {
            serverInfo.shutdown();
        }
        isRunning = false;
        status = STATUS_STOP;
    }

    public void sendMsg(String pubKey, byte[] text) {
        Log.d(THIS_FILE, "sendMsg pubKey:" + pubKey);
        if (serverInfo != null) {

            //构建消息体
            ipks.Context ctx = new ipks.Context();
            ctx.withTimeout(10000);
            NewMessage nms = new NewMessage();
            nms.setPayload(text);
            // nms.setSymKeyID(serverInfo.getSymKeyID());
            nms.setSig(serverInfo.getAsymKeyID());
            //设置接收人公钥
            // setSymKeyID/setPublicKey不能同时使用
            nms.setPublicKey(Numeric.hexStringToByteArray("0x04" + pubKey));
            nms.setTTL(3 * 24 * 60 * 60);
            //发送消息
            try {
                Log.d(THIS_FILE, "sendMsg:" + text);
                serverInfo.getWapi().post(ctx, nms);
                Log.d(THIS_FILE, "sendMsg complete.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}
