package com.mlick.checkss.quartz.bean;

import java.io.Serializable;

public class NetBean implements Serializable{

    private String ip;
    private int port;


    public NetBean() {
    }

    public NetBean(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "NetBean{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
