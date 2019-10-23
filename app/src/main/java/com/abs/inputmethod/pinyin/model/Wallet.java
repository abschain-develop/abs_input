package com.abs.inputmethod.pinyin.model;

public class Wallet {
    private String keystore;
    private String address;
    private String publicKey;
    private String privateKey;

    public Wallet(String keystore, String address, String publicKey, String privateKey) {
        this.keystore = keystore;
        this.address = address;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public String getKeystore() {
        return keystore;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
