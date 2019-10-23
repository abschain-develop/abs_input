package com.abs.inputmethod.pinyin;

public class IpksManager extends Thread {

    private IpksService ipksService;

    private static IpksManager ipksManager;

    public static IpksManager getInstance() {
        if (ipksManager == null) {
            ipksManager = new IpksManager();
        }
        return ipksManager;
    }

    private IpksManager() {
        ipksService = new IpksService();
    }

    public void startService() {
        if (ipksService == null) {
            ipksService = new IpksService();
        }

        new Thread(ipksService).start();
    }

    public void stopService() {
        ipksService.setStop();
        ipksService = null;
    }

    public void sendMsg(String pubKey, byte[] text) {
        if (ipksService != null) {
            ipksService.sendMsg(pubKey, text);
        }
    }

    public boolean isRunning() {
        if (ipksService != null) {
            return ipksService.isRunning();
        }
        return false;
    }
}
