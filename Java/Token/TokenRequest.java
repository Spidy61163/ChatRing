package pk.edu.itu.bsai23023.chatring.Token;

public class TokenRequest {
    public String type;
    public String appId;
    public String certificate;
    public String uid;
    public String channel;
    public int expire;

    public TokenRequest(String type, String appId, String certificate, String uid, String channel, int expire) {
        this.type = type;
        this.appId = appId;
        this.certificate = certificate;
        this.uid = uid;
        this.channel = channel;
        this.expire = expire;
    }
}