package javax.servlet.http;
class RFC6265Validator extends CookieNameValidator {
    private static final String RFC2616_SEPARATORS = "()<>@,;:\\\"/[]?={} \t";
    RFC6265Validator() {
        super ( "()<>@,;:\\\"/[]?={} \t" );
    }
}
