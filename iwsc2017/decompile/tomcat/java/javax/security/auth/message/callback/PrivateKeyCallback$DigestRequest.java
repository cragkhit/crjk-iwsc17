package javax.security.auth.message.callback;
public static class DigestRequest implements Request {
    private final byte[] digest;
    private final String algorithm;
    public DigestRequest ( final byte[] digest, final String algorithm ) {
        this.digest = digest;
        this.algorithm = algorithm;
    }
    public byte[] getDigest() {
        return this.digest;
    }
    public String getAlgorithm() {
        return this.algorithm;
    }
}
