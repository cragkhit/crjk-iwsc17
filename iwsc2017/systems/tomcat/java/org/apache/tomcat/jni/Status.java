package org.apache.tomcat.jni;
public class Status {
    public static final int APR_OS_START_ERROR   = 20000;
    public static final int APR_OS_ERRSPACE_SIZE = 50000;
    public static final int APR_OS_START_STATUS  = ( APR_OS_START_ERROR + APR_OS_ERRSPACE_SIZE );
    public static final int APR_OS_START_USERERR  = ( APR_OS_START_STATUS + APR_OS_ERRSPACE_SIZE );
    public static final int APR_OS_START_USEERR    = APR_OS_START_USERERR;
    public static final int APR_OS_START_CANONERR  = ( APR_OS_START_USERERR + ( APR_OS_ERRSPACE_SIZE * 10 ) );
    public static final int APR_OS_START_EAIERR  = ( APR_OS_START_CANONERR + APR_OS_ERRSPACE_SIZE );
    public static final int APR_OS_START_SYSERR  = ( APR_OS_START_EAIERR + APR_OS_ERRSPACE_SIZE );
    public static final int APR_SUCCESS = 0;
    public static final int APR_ENOSTAT       = ( APR_OS_START_ERROR + 1 );
    public static final int APR_ENOPOOL       = ( APR_OS_START_ERROR + 2 );
    public static final int APR_EBADDATE      = ( APR_OS_START_ERROR + 4 );
    public static final int APR_EINVALSOCK    = ( APR_OS_START_ERROR + 5 );
    public static final int APR_ENOPROC       = ( APR_OS_START_ERROR + 6 );
    public static final int APR_ENOTIME       = ( APR_OS_START_ERROR + 7 );
    public static final int APR_ENODIR        = ( APR_OS_START_ERROR + 8 );
    public static final int APR_ENOLOCK       = ( APR_OS_START_ERROR + 9 );
    public static final int APR_ENOPOLL       = ( APR_OS_START_ERROR + 10 );
    public static final int APR_ENOSOCKET     = ( APR_OS_START_ERROR + 11 );
    public static final int APR_ENOTHREAD     = ( APR_OS_START_ERROR + 12 );
    public static final int APR_ENOTHDKEY     = ( APR_OS_START_ERROR + 13 );
    public static final int APR_EGENERAL      = ( APR_OS_START_ERROR + 14 );
    public static final int APR_ENOSHMAVAIL   = ( APR_OS_START_ERROR + 15 );
    public static final int APR_EBADIP        = ( APR_OS_START_ERROR + 16 );
    public static final int APR_EBADMASK      = ( APR_OS_START_ERROR + 17 );
    public static final int APR_EDSOOPEN      = ( APR_OS_START_ERROR + 19 );
    public static final int APR_EABSOLUTE     = ( APR_OS_START_ERROR + 20 );
    public static final int APR_ERELATIVE     = ( APR_OS_START_ERROR + 21 );
    public static final int APR_EINCOMPLETE   = ( APR_OS_START_ERROR + 22 );
    public static final int APR_EABOVEROOT    = ( APR_OS_START_ERROR + 23 );
    public static final int APR_EBADPATH      = ( APR_OS_START_ERROR + 24 );
    public static final int APR_EPATHWILD     = ( APR_OS_START_ERROR + 25 );
    public static final int APR_ESYMNOTFOUND  = ( APR_OS_START_ERROR + 26 );
    public static final int APR_EPROC_UNKNOWN = ( APR_OS_START_ERROR + 27 );
    public static final int APR_ENOTENOUGHENTROPY = ( APR_OS_START_ERROR + 28 );
    public static final int APR_INCHILD       = ( APR_OS_START_STATUS + 1 );
    public static final int APR_INPARENT      = ( APR_OS_START_STATUS + 2 );
    public static final int APR_DETACH        = ( APR_OS_START_STATUS + 3 );
    public static final int APR_NOTDETACH     = ( APR_OS_START_STATUS + 4 );
    public static final int APR_CHILD_DONE    = ( APR_OS_START_STATUS + 5 );
    public static final int APR_CHILD_NOTDONE = ( APR_OS_START_STATUS + 6 );
    public static final int APR_TIMEUP        = ( APR_OS_START_STATUS + 7 );
    public static final int APR_INCOMPLETE    = ( APR_OS_START_STATUS + 8 );
    public static final int APR_BADCH         = ( APR_OS_START_STATUS + 12 );
    public static final int APR_BADARG        = ( APR_OS_START_STATUS + 13 );
    public static final int APR_EOF           = ( APR_OS_START_STATUS + 14 );
    public static final int APR_NOTFOUND      = ( APR_OS_START_STATUS + 15 );
    public static final int APR_ANONYMOUS     = ( APR_OS_START_STATUS + 19 );
    public static final int APR_FILEBASED     = ( APR_OS_START_STATUS + 20 );
    public static final int APR_KEYBASED      = ( APR_OS_START_STATUS + 21 );
    public static final int APR_EINIT         = ( APR_OS_START_STATUS + 22 );
    public static final int APR_ENOTIMPL      = ( APR_OS_START_STATUS + 23 );
    public static final int APR_EMISMATCH     = ( APR_OS_START_STATUS + 24 );
    public static final int APR_EBUSY         = ( APR_OS_START_STATUS + 25 );
    public static final int TIMEUP            = ( APR_OS_START_USERERR + 1 );
    public static final int EAGAIN            = ( APR_OS_START_USERERR + 2 );
    public static final int EINTR             = ( APR_OS_START_USERERR + 3 );
    public static final int EINPROGRESS       = ( APR_OS_START_USERERR + 4 );
    public static final int ETIMEDOUT         = ( APR_OS_START_USERERR + 5 );
    private static native boolean is ( int err, int idx );
    public static final boolean APR_STATUS_IS_ENOSTAT ( int s )    {
        return is ( s, 1 );
    }
    public static final boolean APR_STATUS_IS_ENOPOOL ( int s )    {
        return is ( s, 2 );
    }
    public static final boolean APR_STATUS_IS_EBADDATE ( int s )   {
        return is ( s, 4 );
    }
    public static final boolean APR_STATUS_IS_EINVALSOCK ( int s ) {
        return is ( s, 5 );
    }
    public static final boolean APR_STATUS_IS_ENOPROC ( int s )    {
        return is ( s, 6 );
    }
    public static final boolean APR_STATUS_IS_ENOTIME ( int s )    {
        return is ( s, 7 );
    }
    public static final boolean APR_STATUS_IS_ENODIR ( int s )     {
        return is ( s, 8 );
    }
    public static final boolean APR_STATUS_IS_ENOLOCK ( int s )    {
        return is ( s, 9 );
    }
    public static final boolean APR_STATUS_IS_ENOPOLL ( int s )    {
        return is ( s, 10 );
    }
    public static final boolean APR_STATUS_IS_ENOSOCKET ( int s )  {
        return is ( s, 11 );
    }
    public static final boolean APR_STATUS_IS_ENOTHREAD ( int s )  {
        return is ( s, 12 );
    }
    public static final boolean APR_STATUS_IS_ENOTHDKEY ( int s )  {
        return is ( s, 13 );
    }
    public static final boolean APR_STATUS_IS_EGENERAL ( int s )   {
        return is ( s, 14 );
    }
    public static final boolean APR_STATUS_IS_ENOSHMAVAIL ( int s ) {
        return is ( s, 15 );
    }
    public static final boolean APR_STATUS_IS_EBADIP ( int s )     {
        return is ( s, 16 );
    }
    public static final boolean APR_STATUS_IS_EBADMASK ( int s )   {
        return is ( s, 17 );
    }
    public static final boolean APR_STATUS_IS_EDSOPEN ( int s )    {
        return is ( s, 19 );
    }
    public static final boolean APR_STATUS_IS_EABSOLUTE ( int s )  {
        return is ( s, 20 );
    }
    public static final boolean APR_STATUS_IS_ERELATIVE ( int s )  {
        return is ( s, 21 );
    }
    public static final boolean APR_STATUS_IS_EINCOMPLETE ( int s ) {
        return is ( s, 22 );
    }
    public static final boolean APR_STATUS_IS_EABOVEROOT ( int s ) {
        return is ( s, 23 );
    }
    public static final boolean APR_STATUS_IS_EBADPATH ( int s )   {
        return is ( s, 24 );
    }
    public static final boolean APR_STATUS_IS_EPATHWILD ( int s )  {
        return is ( s, 25 );
    }
    public static final boolean APR_STATUS_IS_ESYMNOTFOUND ( int s )      {
        return is ( s, 26 );
    }
    public static final boolean APR_STATUS_IS_EPROC_UNKNOWN ( int s )     {
        return is ( s, 27 );
    }
    public static final boolean APR_STATUS_IS_ENOTENOUGHENTROPY ( int s ) {
        return is ( s, 28 );
    }
    public static final boolean APR_STATUS_IS_INCHILD ( int s )    {
        return is ( s, 51 );
    }
    public static final boolean APR_STATUS_IS_INPARENT ( int s )   {
        return is ( s, 52 );
    }
    public static final boolean APR_STATUS_IS_DETACH ( int s )     {
        return is ( s, 53 );
    }
    public static final boolean APR_STATUS_IS_NOTDETACH ( int s )  {
        return is ( s, 54 );
    }
    public static final boolean APR_STATUS_IS_CHILD_DONE ( int s ) {
        return is ( s, 55 );
    }
    public static final boolean APR_STATUS_IS_CHILD_NOTDONE ( int s )  {
        return is ( s, 56 );
    }
    public static final boolean APR_STATUS_IS_TIMEUP ( int s )     {
        return is ( s, 57 );
    }
    public static final boolean APR_STATUS_IS_INCOMPLETE ( int s ) {
        return is ( s, 58 );
    }
    public static final boolean APR_STATUS_IS_BADCH ( int s )      {
        return is ( s, 62 );
    }
    public static final boolean APR_STATUS_IS_BADARG ( int s )     {
        return is ( s, 63 );
    }
    public static final boolean APR_STATUS_IS_EOF ( int s )        {
        return is ( s, 64 );
    }
    public static final boolean APR_STATUS_IS_NOTFOUND ( int s )   {
        return is ( s, 65 );
    }
    public static final boolean APR_STATUS_IS_ANONYMOUS ( int s )  {
        return is ( s, 69 );
    }
    public static final boolean APR_STATUS_IS_FILEBASED ( int s )  {
        return is ( s, 70 );
    }
    public static final boolean APR_STATUS_IS_KEYBASED ( int s )   {
        return is ( s, 71 );
    }
    public static final boolean APR_STATUS_IS_EINIT ( int s )      {
        return is ( s, 72 );
    }
    public static final boolean APR_STATUS_IS_ENOTIMPL ( int s )   {
        return is ( s, 73 );
    }
    public static final boolean APR_STATUS_IS_EMISMATCH ( int s )  {
        return is ( s, 74 );
    }
    public static final boolean APR_STATUS_IS_EBUSY ( int s )      {
        return is ( s, 75 );
    }
    public static final boolean APR_STATUS_IS_EAGAIN ( int s )     {
        return is ( s, 90 );
    }
    public static final boolean APR_STATUS_IS_ETIMEDOUT ( int s )  {
        return is ( s, 91 );
    }
    public static final boolean APR_STATUS_IS_ECONNABORTED ( int s ) {
        return is ( s, 92 );
    }
    public static final boolean APR_STATUS_IS_ECONNRESET ( int s )   {
        return is ( s, 93 );
    }
    public static final boolean APR_STATUS_IS_EINPROGRESS ( int s )  {
        return is ( s, 94 );
    }
    public static final boolean APR_STATUS_IS_EINTR ( int s )      {
        return is ( s, 95 );
    }
    public static final boolean APR_STATUS_IS_ENOTSOCK ( int s )   {
        return is ( s, 96 );
    }
    public static final boolean APR_STATUS_IS_EINVAL ( int s )     {
        return is ( s, 97 );
    }
}
