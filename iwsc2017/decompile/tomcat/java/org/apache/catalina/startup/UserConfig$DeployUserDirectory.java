package org.apache.catalina.startup;
private static class DeployUserDirectory implements Runnable {
    private UserConfig config;
    private String user;
    private String home;
    public DeployUserDirectory ( final UserConfig config, final String user, final String home ) {
        this.config = config;
        this.user = user;
        this.home = home;
    }
    @Override
    public void run() {
        UserConfig.access$000 ( this.config, this.user, this.home );
    }
}
