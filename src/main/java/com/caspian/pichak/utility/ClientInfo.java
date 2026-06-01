package com.caspian.pichak.utility;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientInfo {
    private static final Logger log = LoggerFactory.getLogger(ClientInfo.class);
    private static final String MOBILE_ANDROID = "Andriod";
    private static final String DESKTOP_WEB = "Web";
    private static final String INTERNAL = "Internal";
    private final String referer;
    private final String fullURL;
    private final String clientIpAddr;
    private final String clientOS;
    private final String clientBrowser;
    private final String userAgent;

    public ClientInfo(HttpServletRequest request) {
        this.referer = this.getReferer(request);
        this.fullURL = this.getFullURL(request);
        this.clientIpAddr = this.getClientIpAddr(request);
        this.clientOS = this.getClientOS(request);
        this.clientBrowser = this.getClientBrowser(request);
        this.userAgent = this.getUserAgent(request);
        log.info("\nUser Agent \t" + this.userAgent + "\nOperating System\t" + this.clientOS + "\nBrowser Name\t" + this.clientBrowser + "\nIP Address\t" + this.clientIpAddr + "\nFull URL\t" + this.fullURL + "\nReferrer\t" + this.referer);
    }

    private String getReferer(HttpServletRequest request) {
        String referer = request.getHeader("referer");
        return referer;
    }

    private String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();
        String result = queryString == null ? requestURL.toString() : requestURL.append('?').append(queryString).toString();
        return result;
    }

    private String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    private String getClientOS(HttpServletRequest request) {
        String browserDetails = request.getHeader("User-Agent");
        String lowerCaseBrowser = browserDetails.toLowerCase();
        if (lowerCaseBrowser.contains("windows")) {
            return "Windows";
        } else if (lowerCaseBrowser.contains("mac")) {
            return "Mac";
        } else if (lowerCaseBrowser.contains("x11")) {
            return "Unix";
        } else if (lowerCaseBrowser.contains("android")) {
            return "Android";
        } else {
            return lowerCaseBrowser.contains("iphone") ? "IPhone" : "UnKnown, More-Info: " + browserDetails;
        }
    }

    private String getDevice(HttpServletRequest request) {
        String browserDetails = request.getHeader("User-Agent");
        String lowerCaseBrowser = browserDetails.toLowerCase();
        if (lowerCaseBrowser.contains("okhttp")) {
            return "Andriod";
        } else {
            return lowerCaseBrowser.contains("java") ? "Internal" : "Web";
        }
    }

    private String getClientBrowser(HttpServletRequest request) {
        String browserDetails = request.getHeader("User-Agent");
        String user = browserDetails.toLowerCase();
        String browser = "";
        if (user.contains("msie")) {
            String substring = browserDetails.substring(browserDetails.indexOf("MSIE")).split(";")[0];
            browser = substring.split(" ")[0].replace("MSIE", "IE") + "-" + substring.split(" ")[1];
        } else if (user.contains("safari") && user.contains("version")) {
            browser = browserDetails.substring(browserDetails.indexOf("Safari")).split(" ")[0].split("/")[0] + "-" + browserDetails.substring(browserDetails.indexOf("Version")).split(" ")[0].split("/")[1];
        } else if (!user.contains("opr") && !user.contains("opera")) {
            if (user.contains("chrome")) {
                browser = browserDetails.substring(browserDetails.indexOf("Chrome")).split(" ")[0].replace("/", "-");
            } else if (user.indexOf("mozilla/7.0") <= -1 && user.indexOf("netscape6") == -1 && user.indexOf("mozilla/4.7") == -1 && user.indexOf("mozilla/4.78") == -1 && user.indexOf("mozilla/4.08") == -1 && user.indexOf("mozilla/3") == -1) {
                if (user.contains("firefox")) {
                    browser = browserDetails.substring(browserDetails.indexOf("Firefox")).split(" ")[0].replace("/", "-");
                } else if (user.contains("rv")) {
                    browser = "IE";
                } else {
                    browser = "UnKnown, More-Info: " + browserDetails;
                }
            } else {
                browser = "Netscape-?";
            }
        } else if (user.contains("opera")) {
            browser = browserDetails.substring(browserDetails.indexOf("Opera")).split(" ")[0].split("/")[0] + "-" + browserDetails.substring(browserDetails.indexOf("Version")).split(" ")[0].split("/")[1];
        } else if (user.contains("opr")) {
            browser = browserDetails.substring(browserDetails.indexOf("OPR")).split(" ")[0].replace("/", "-").replace("OPR", "Opera");
        }

        return browser;
    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    public String toString() {
        return (new Gson()).toJson(this);
    }

    public String getReferer() {
        return this.referer;
    }

    public String getFullURL() {
        return this.fullURL;
    }

    public String getClientIpAddr() {
        return this.clientIpAddr;
    }

    public String getClientOS() {
        return this.clientOS;
    }

    public String getClientBrowser() {
        return this.clientBrowser;
    }

    public String getUserAgent() {
        return this.userAgent;
    }
}
