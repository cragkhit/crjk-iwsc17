package javax.security.auth.message.callback;
public static class SubjectKeyIDRequest implements Request {
    private final byte[] subjectKeyID;
    public SubjectKeyIDRequest ( final byte[] subjectKeyID ) {
        this.subjectKeyID = subjectKeyID;
    }
    public byte[] getSubjectKeyID() {
        return this.subjectKeyID;
    }
}
